 /*
  * Copyright 2012 Robert Philipp
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.microtitan.diffusive.diffuser.restful;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 
 import org.apache.abdera.parser.ParseException;
 import org.apache.log4j.Logger;
 import org.microtitan.diffusive.Constants;
 import org.microtitan.diffusive.diffuser.AbstractDiffuser;
 import org.microtitan.diffusive.diffuser.LocalDiffuser;
 import org.microtitan.diffusive.diffuser.restful.client.RestfulDiffuserManagerClient;
 import org.microtitan.diffusive.diffuser.restful.response.ExecuteDiffuserResponse;
 import org.microtitan.diffusive.diffuser.serializer.Serializer;
 import org.microtitan.diffusive.diffuser.serializer.SerializerFactory;
 import org.microtitan.diffusive.diffuser.strategy.DiffuserStrategy;
 import org.microtitan.diffusive.diffuser.strategy.load.DiffuserLoadCalc;
 
 /**
  * A diffuser that uses REST to diffuser methods to remote RESTful diffusers, or runs the task locally.
  * To diffuse to a remote diffuser, this diffuser instantiates a RESTful client 
  * ({@link RestfulDiffuserManagerClient}) that is then used to send the task to the remote server.
  *  
  * @author Robert Philipp
  */
 public class RestfulDiffuser extends AbstractDiffuser {
 
 	private static final Logger LOGGER = Logger.getLogger( RestfulDiffuser.class );
 	
 	public static final int MAX_REDUNDANCY = 20;
 	public static final int POLLING_TIME_OUT = 50;
 	public static final TimeUnit POLLING_TIME_UNIT = TimeUnit.MILLISECONDS;
 	
 	// used to serialize objects for making requests across the network
 	private final Serializer serializer;
 	private final DiffuserStrategy strategy;
 	private final List< URI > classPaths;
 	private final double loadThreshold;
 	
 	// the maximum number of threads in the thread pool (ExecutorService) to account 
 	// for redundant diffusion
 	private int maxRedundancy = MAX_REDUNDANCY;
 	private int pollingTimeout = POLLING_TIME_OUT;
 	private TimeUnit pollingTimeUnit = POLLING_TIME_UNIT;
 	
 	/**
 	 * Constructs the RESTful diffuser that runs methods either locally or sends them on to a remote
 	 * RESTful diffuser.
 	 * @param serializer The object that converts the object into and out of the form that is transmitted across
 	 * the wire.
 	 * @param strategy The diffuser strategy that determines which end-point will get called next
 	 * @param classPaths The class paths on a remote server needed for loading classes that aren't 
 	 * locally available 
 	 * @param loadThreshold The load threshold above which the {@link RestfulDiffuser} will forward execution
 	 * of the task to remote diffuser. The load threshold must be in the interval {@code (0.0, infinity]}
 	 * @see DiffuserLoadCalc
 	 */
 	public RestfulDiffuser( final Serializer serializer, 
 							final DiffuserStrategy strategy, 
 							final List< URI > classPaths,
 							final double loadThreshold )
 	{
 		this.serializer = serializer;
 		this.strategy = strategy;
 		this.classPaths = classPaths;
 		
 		if( loadThreshold <= 0.0 )
 		{
 			final StringBuffer message = new StringBuffer();
 			message.append( "The load threshold must be greater than 0.0" + Constants.NEW_LINE );
 			message.append( "  Specified Load Threshold: " + loadThreshold );
 			throw new IllegalArgumentException( message.toString() );
 		}
 		this.loadThreshold = loadThreshold;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.microtitan.diffusive.diffuser.Diffuser#runObject(double, java.lang.Class, java.lang.Object, java.lang.String, java.lang.Object[])
 	 */
 	@Override
 	public Object runObject( final double load, final Class< ? > returnType, final Object object, final String methodName, final Object... arguments )
 	{
 		// if the load is less than the threshold, then we can compute this task locally, or if there are no
 		// end-points to which to diffuse the task further. Otherwise, the task is diffused to an end-point
 		// based on the strategy that selects the end-point
 		Object result = null;
 		if( load < loadThreshold || strategy.isEmpty() )
 		{
 			if( LOGGER.isInfoEnabled() )
 			{
 				final StringBuffer message = new StringBuffer();
 				message.append( RestfulDiffuser.class.getName() + " will call the runObject(...) method on the local diffuser: " + Constants.NEW_LINE );
 				message.append( "  Diffuser Name: " + LocalDiffuser.class.getName() + Constants.NEW_LINE + "  Reason: " );
 				if( load < loadThreshold )
 				{
 					message.append( "the load (" + load + ") was less than the load threshold (" + loadThreshold + ")." );
 				}
 				else if( strategy.isEmpty() )
 				{
 					message.append( "the RESTful diffuser was not assigned any client end-points." );
 				}
 				message.append( Constants.NEW_LINE );
 				message.append( "  Containing Class: " + object.getClass().getName() + Constants.NEW_LINE );
 				message.append( "  Method Name: " + methodName + Constants.NEW_LINE );
 				message.append( "  Argument Types: " );
				if( arguments != null && arguments.length > 0 )
 				{
 					for( final Object argument : arguments )
 					{
 						message.append( Constants.NEW_LINE + "    " + argument.getClass().getName() );
 					}
 				}
 				else
 				{
 					message.append( "[none]" );
 				}
 
 				LOGGER.info( message.toString() );
 			}
 			
 			// execute the method on the local diffuser
 			result = new LocalDiffuser().runObject( load, returnType, object, methodName, arguments );
 		}
 		else
 		{
 			// create the client manager for the next end points from the strategy
 			final List< URI > endpoints = strategy.getEndpoints();
 
 			if( LOGGER.isInfoEnabled() )
 			{
 				final StringBuffer message = new StringBuffer();
 				message.append( RestfulDiffuser.class.getName() + " will call the runObject(...) method on the remote diffusers: " + Constants.NEW_LINE );
 				message.append( Constants.NEW_LINE );
 				message.append( "  Containing Class: " + object.getClass().getName() + Constants.NEW_LINE );
 				message.append( "  Method Name: " + methodName + Constants.NEW_LINE );
 				message.append( "  Argument Types: " );
 				if( arguments.length > 0 )
 				{
 					for( final Object argument : arguments )
 					{
 						message.append( Constants.NEW_LINE + "    " + argument.getClass().getName() );
 					}
 				}
 				else
 				{
 					message.append( "[none]" );
 				}
 				message.append( Constants.NEW_LINE + "  End-points: " );
 				if( endpoints.size() > 1 )
 				{
 					message.append( "[redundant calls to diffusers]" );
 				}
 				for( URI endpoint : endpoints )
 				{
 					message.append( Constants.NEW_LINE + "    " + endpoint.toString() );
 				}
 
 				LOGGER.info( message.toString() );
 			}
 			
 			// create the list of futures that are waiting for the task to return from 
 			// the first end point
 			final List< Future< ? > > futures = new ArrayList<>( endpoints.size() );
 			
 			// create the executor service for running the tasks.
 			final ExecutorService executor = Executors.newFixedThreadPool( maxRedundancy );
 			
 			// diffuse to each of the end-points, adding the reference to the result in the list of futures.
 			// there are multiple end-points in case there is to be redundancy. usually, there is just one end-point
 			for( URI endpoint : endpoints )
 			{
 				// create a client with which to interact with the remote diffuser manager (RestfulDiffuserManagerResource) 
 				final RestfulDiffuserManagerClient client = new RestfulDiffuserManagerClient( endpoint );
 	
 				// create the diffuser on the server
 				final int numArguments = (arguments == null ? 0 : arguments.length);
 				final Class< ? >[] argumentTypes = new Class< ? >[ numArguments ];
 				for( int i = 0; i < numArguments; ++i )
 				{
 					argumentTypes[ i ] = arguments[ i ].getClass();
 				}
 				/*final CreateDiffuserResponse response = */
 				client.createDiffuser( classPaths, returnType, object.getClass(), methodName, argumentTypes );
 				
 				// execute the method on the diffuser
 				ExecuteDiffuserResponse executeResponse = null;
 				try( final ByteArrayOutputStream out = new ByteArrayOutputStream() )
 				{
 					// serialize the object into the byte[] output stream and flush it
 					serializer.serialize( object, out );
 					out.flush();
 					
 					//
 					// call the client to execute the method on the object
 					//
 					// we need to serialize the object containing the method we are calling, and then we need to serialize each
 					// of the arguments passed into the method.
 					if( numArguments == 0 )
 					{
 						executeResponse = client.executeMethod( returnType, object.getClass(), methodName, out.toByteArray(), serializer );
 					}
 					else
 					{
 						// serialize the argument values
 						final List< byte[] > serializedArgs = new ArrayList<>();
 						for( Object argument : arguments )
 						{
 							try( final ByteArrayOutputStream outArg = new ByteArrayOutputStream() )
 							{
 								serializer.serialize( argument, outArg );
 								outArg.flush();
 								
 								// add the byte[] to the list of serialized arguments
 								serializedArgs.add( outArg.toByteArray() );
 							}
 							catch( IOException e )
 							{
 								final StringBuffer message = new StringBuffer();
 								message.append( "I/O error occured attempting to flush the byte[] output stream holding a serialized argument in" + Constants.NEW_LINE );
 								message.append( "preparation for calling the execute(...) method on the client." + Constants.NEW_LINE );
 								message.append( "  Client Endpoint: " + endpoint.toString() + Constants.NEW_LINE );
 								message.append( "  Method Name: " + methodName + Constants.NEW_LINE );
 								message.append( "  Argument Value: " + argument + Constants.NEW_LINE );
 								message.append( "  Argument Type: " + argument.getClass().getName() + Constants.NEW_LINE );
 								message.append( "  Argument Types: " + Constants.NEW_LINE );
 								for( int i = 0; i < numArguments; ++i )
 								{
 									message.append( "    " + argumentTypes[ i ].getName() + Constants.NEW_LINE );
 								}
 								message.append( "  Return Type: " + returnType.getName() + Constants.NEW_LINE );
 								message.append( "  Containing Class: " + object.getClass().getName() + Constants.NEW_LINE );
 								message.append( "  Serializer: " + serializer.getClass().getName() + Constants.NEW_LINE );
 								
 								LOGGER.error( message.toString(), e );
 								throw new IllegalArgumentException( message.toString(), e );
 							}
 						}
 						
 						final List< Class< ? > > argTypes = Arrays.asList( argumentTypes );
 						final String serializerName = SerializerFactory.getSerializerName( serializer.getClass() );
 						executeResponse = client.executeMethod( returnType, object.getClass(), methodName, argTypes, serializedArgs, out.toByteArray(), serializerName );
 					}
 				}
 				catch( IOException e )
 				{
 					final StringBuffer message = new StringBuffer();
 					message.append( "I/O error occured attempting to flush the byte[] output stream holding the serialized object in" + Constants.NEW_LINE );
 					message.append( "preparation for calling the execute(...) method on the client." + Constants.NEW_LINE );
 					message.append( "  Client Endpoint: " + endpoint.toString() + Constants.NEW_LINE );
 					message.append( "  Method Name: " + methodName + Constants.NEW_LINE );
 					message.append( "  Argument Types: " + (numArguments == 0 ? "[none]" : "" ) + Constants.NEW_LINE );
 					for( int i = 0; i < numArguments; ++i )
 					{
 						message.append( "    " + argumentTypes[ i ].getName() + Constants.NEW_LINE );
 					}
 					message.append( "  Return Type: " + returnType.getName() + Constants.NEW_LINE );
 					message.append( "  Containing Class: " + object.getClass().getName() + Constants.NEW_LINE );
 					message.append( "  Serializer: " + serializer.getClass().getName() + Constants.NEW_LINE );
 					
 					LOGGER.error( message.toString() );
 					throw new IllegalArgumentException( message.toString() );
 				}
 
 				// create a task that makes a blocking call to get the result of the calc, and then
 				// submit that task to the executor service to run it
 				final ExecuteDiffuserResponse executeResponseCopy = executeResponse;
 				final Callable< ? > task = new Callable< Object >() {
 
 					@Override
 					public Object call() throws Exception
 					{
 						final DiffuserSignature diffuserId = DiffuserSignature.parse( executeResponseCopy.getSignature() );
 						final Class< ? > clazz = diffuserId.getClazz();
 						return client.getResult( returnType, clazz, methodName, executeResponseCopy.getRequestId(), serializer );
 					}
 				};
 				futures.add( executor.submit( task ) );
 			}
 			
 			// now that all the tasks have been submitted, we wait for the first result to return,
 			// and when it does, then we accept it, ignore the rest, and return
 			// TOOO issue a cancel order to the tasks that aren't yet finished.
 			boolean isDone = false;
 			int i = 0;
 			do
 			{
 				final int index = i % futures.size();
 				final Future< ? > future = futures.get( index );
 				try
 				{
 					result = future.get( pollingTimeout, pollingTimeUnit );
 					isDone = true;
 				}
 				catch( ParseException | InterruptedException | ExecutionException e )
 				{
 					// execution crapped out or was interrupted, so remove the future and keep checking the others
 					futures.remove( index );
 				}
 				catch( TimeoutException e )
 				{
 					// increment the index, timed out, get the next future and see if it is done.
 					++i;
 				}
 			}
 			while( !isDone && !futures.isEmpty() );
 			
 			// shutdown the executor service
 			executor.shutdownNow();
 		}
 		return result;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString()
 	{
 		final StringBuffer buffer = new StringBuffer();
 		buffer.append( "Serializer: " + serializer.toString() + Constants.NEW_LINE );
 		buffer.append( "Strategy: " + strategy.getClass().getName() + Constants.NEW_LINE );
 		buffer.append( "  " + strategy.toString() );
 		return buffer.toString();
 	}
 }
