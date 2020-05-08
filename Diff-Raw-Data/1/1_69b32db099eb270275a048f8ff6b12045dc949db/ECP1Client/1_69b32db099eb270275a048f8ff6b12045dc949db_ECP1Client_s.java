 package de.ptb.epics.eve.ecp1.client;
 
 import java.io.ByteArrayInputStream;
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.net.Socket;
 import java.net.SocketAddress;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import de.ptb.epics.eve.ecp1.client.interfaces.IChainStatusListener;
 import de.ptb.epics.eve.ecp1.client.interfaces.IConnectionStateListener;
 import de.ptb.epics.eve.ecp1.client.interfaces.IEngineStatusListener;
 import de.ptb.epics.eve.ecp1.client.interfaces.IErrorListener;
 import de.ptb.epics.eve.ecp1.client.interfaces.IMeasurementDataListener;
 import de.ptb.epics.eve.ecp1.client.interfaces.IPlayController;
 import de.ptb.epics.eve.ecp1.client.interfaces.IPlayListController;
 import de.ptb.epics.eve.ecp1.client.interfaces.IRequestListener;
 import de.ptb.epics.eve.ecp1.client.model.MeasurementData;
 import de.ptb.epics.eve.ecp1.client.model.Request;
 import de.ptb.epics.eve.ecp1.intern.CancelRequestCommand;
 import de.ptb.epics.eve.ecp1.intern.ChainStatusCommand;
 import de.ptb.epics.eve.ecp1.intern.CurrentXMLCommand;
 import de.ptb.epics.eve.ecp1.intern.EngineStatusCommand;
 import de.ptb.epics.eve.ecp1.intern.ErrorCommand;
 import de.ptb.epics.eve.ecp1.intern.GenericRequestCommand;
 import de.ptb.epics.eve.ecp1.intern.IECP1Command;
 import de.ptb.epics.eve.ecp1.intern.MeasurementDataCommand;
 import de.ptb.epics.eve.ecp1.intern.PlayListCommand;
 
 public class ECP1Client {
 	
 	private Socket socket;
 	private Queue< byte[] > inQueue;
 	private Queue< IECP1Command > outQueue;
 	private InHandler inHandler;
 	private OutHandler outHandler;
 	private Thread inThread;
 	private Thread outThread;
 	private Thread dispatchThread;
 	private ECP1Client.InDispatcher dispatchHandler;
 	
 	private String loginName;
 	
 	private ECP1Client self;
 	
 	private PlayListController playListController;
 	
 	private final Queue< IEngineStatusListener > engineStatusListener;
 	private final Queue< IChainStatusListener > chainStatusListener;
 	private final Queue< IErrorListener > errorListener;
 	private final Queue< IMeasurementDataListener > measurementDataListener;
 	private final Queue< IRequestListener > requestListener;
 	private final Queue< IConnectionStateListener > connectionStateListener;
 	
 	private PlayController playController;
 	
 	private final Map< Integer, Request > requestMap;
 	
 	private List< String > classNames;
 	
 	private Map< Character, Constructor< ? extends IECP1Command > > commands;
 	
 	private boolean running;
 	
 	@SuppressWarnings("unchecked")
 	public ECP1Client() {
 		this.self = this;
 		
 		this.inQueue = new ConcurrentLinkedQueue< byte[] >();
 		this.outQueue = new ConcurrentLinkedQueue< IECP1Command >();
 		
 		this.playController = new PlayController( this );
 		this.playListController = new PlayListController( this );
 		
 		this.engineStatusListener = new ConcurrentLinkedQueue< IEngineStatusListener >();
 		this.chainStatusListener = new ConcurrentLinkedQueue< IChainStatusListener >();
 		this.errorListener = new ConcurrentLinkedQueue< IErrorListener >();
 		this.measurementDataListener = new ConcurrentLinkedQueue< IMeasurementDataListener >();
 		this.requestListener = new ConcurrentLinkedQueue< IRequestListener >();
 		this.connectionStateListener = new ConcurrentLinkedQueue< IConnectionStateListener >();
 		
 		this.requestMap = new HashMap< Integer, Request >();
 		
 		this.classNames = new ArrayList< String >();
 		final String packageName = "de.ptb.epics.eve.ecp1.intern.";
 		
 		this.classNames.add( packageName + "AddToPlayListCommand" );
 		this.classNames.add( packageName + "AnswerRequestCommand" );
 		this.classNames.add( packageName + "AutoPlayCommand" );
 		this.classNames.add( packageName + "BreakCommand" );
 		this.classNames.add( packageName + "CancelRequestCommand" );
 		
 		this.classNames.add( packageName + "ChainStatusCommand" );
 		this.classNames.add( packageName + "CurrentXMLCommand" );
 		this.classNames.add( packageName + "EndProgramCommand" );
 		this.classNames.add( packageName + "EngineStatusCommand" );
 		this.classNames.add( packageName + "ErrorCommand" );
 		
 		this.classNames.add( packageName + "GenericRequestCommand" );
 		this.classNames.add( packageName + "HaltCommand" );
 		this.classNames.add( packageName + "LiveDescriptionCommand" );
 		this.classNames.add( packageName + "MeasurementDataCommand" );
 		this.classNames.add( packageName + "PauseCommand" )
 		;
 		this.classNames.add( packageName + "PlayListCommand" );
 		this.classNames.add( packageName + "RemoveFromPlayListCommand" );
 		this.classNames.add( packageName + "ReorderPlayListCommand" );
 		this.classNames.add( packageName + "StartCommand" );
 		this.classNames.add( packageName + "StopCommand" );
 		
 		this.commands = new HashMap< Character, Constructor< ? extends IECP1Command > >();
 		
 		final Iterator< String > it = this.classNames.iterator();
 		final Class< IECP1Command > ecp1CommandInterface = IECP1Command.class;
 		
 		
 		final Class< byte[] > byteArrayClass = byte[].class;
 		final Class< ? >[] constructorParameterArray = new Class< ? >[1];
 		constructorParameterArray[0] = byteArrayClass;
 		while( it.hasNext() ) {
 			final String className = it.next();
 			try {
 				Class< ? extends IECP1Command > classObject = (Class< ? extends IECP1Command >) Class.forName( className );
 				if( !ecp1CommandInterface.isAssignableFrom( classObject ) ) {
 					System.err.println( "Error: " + className + " is not implementing IECP1Command!" );
 					continue;
 				}
 				final Field commandTypeIDField = classObject.getField( "COMMAND_TYPE_ID" );
 				final char id = commandTypeIDField.getChar( null );
 				final Constructor< ? extends IECP1Command > constructor = classObject.getConstructor( constructorParameterArray );
 				if( this.commands.get( id ) == null ) {
 					this.commands.put( id, constructor );
 				} else {
 					System.err.println( "Error: " + className + " and " + this.commands.get( id ).getDeclaringClass().getName() + "does have the same id = " + Integer.toHexString(id) + "!" );
 				}
 				//System.out.println( "The ID for " + className + " is " + Integer.toHexString(id) + "!" );
 			} catch( final ClassNotFoundException exception ) {
 				System.err.println( "Error: Can't find Class " + className + "!" );
 			} catch( final SecurityException exception ) {
 				exception.printStackTrace();
 			} catch( final NoSuchFieldException exception ) {
 				System.err.println( "Error: Can't find static Field COMMAND_TYPE_ID in " + className + "!" );
 			} catch( final IllegalArgumentException exception ) {
 				// TODO Auto-generated catch block
 				exception.printStackTrace();
 			} catch( final IllegalAccessException exception ) {
 				// TODO Auto-generated catch block
 				exception.printStackTrace();
 			} catch( final NoSuchMethodException exception ) {
 				System.err.println( "Error: Can't find Constructor for byte Array as Parameter in " + className + "!" );
 			}
 		}
 	}
 
 	public void close() throws IOException {
 		if( this.running ) {
 			this.inHandler.quit();
 			this.outHandler.quit();
 			this.running = false;
 			this.socket.shutdownInput();
 			this.socket.shutdownOutput();
 			this.socket.close();
 			Iterator< IConnectionStateListener > it = this.connectionStateListener.iterator();
 			while( it.hasNext() ) {
 				it.next().stackDisconnected();
 			}
 		}
 	}
 
 	public void connect( SocketAddress socketAddress, final String loginName ) throws IOException {
 		this.loginName = loginName;
 		this.socket = new Socket();
 		this.socket.connect( socketAddress );
 		
 		this.inQueue.clear();
 		this.outQueue.clear();
 		
 		this.running = true;
 		
 		this.inHandler = new InHandler( this, this.socket.getInputStream(), this.inQueue );
 		this.outHandler = new OutHandler( this.socket.getOutputStream(), this.outQueue );
 		this.dispatchHandler = new ECP1Client.InDispatcher();
 		this.inThread = new Thread( this.inHandler );
 		this.outThread = new Thread( this.outHandler );	
 		this.dispatchThread = new Thread( this.dispatchHandler );
 		
 		this.inThread.start();
 		this.outThread.start();
 		this.dispatchThread.start();
 		Iterator< IConnectionStateListener > it = this.connectionStateListener.iterator();
 		while( it.hasNext() ) {
 			it.next().stackConnected();
 		}
 	}
 
 	public String getLoginName() {
 		return this.loginName;
 	}
 	
 	public void addToOutQueue( final IECP1Command ecp1Command ) {
 		this.outQueue.add( ecp1Command );
 	}
 	
 	public IPlayListController getPlayListController() {
 		return this.playListController;
 	}
 	
 	public boolean addEngineStatusListener( final IEngineStatusListener engineStatusListener ) {
 		return this.engineStatusListener.add( engineStatusListener );
 	}
 
 	public boolean removeEngineStatusListener( final IEngineStatusListener engineStatusListener ) {
 		return this.engineStatusListener.remove( engineStatusListener );
 	}
 	
 	public boolean addChainStatusListener( final IChainStatusListener chainStatusListener ) {
 		return this.chainStatusListener.add( chainStatusListener );
 	}
 
 	public boolean removeChainStatusListener( final IChainStatusListener chainStatusListener ) {
 		return this.chainStatusListener.remove( chainStatusListener );
 	}
 	
 	public boolean addErrorListener( final IErrorListener errorListener ) {
 		return this.errorListener.add( errorListener );
 	}
 
 	public boolean removeErrorListener( final IErrorListener errorListener ) {
 		return this.errorListener.remove( errorListener );
 	}
 	
 	public boolean addMeasurementDataListener( final IMeasurementDataListener measurementDataListener ) {
 		return this.measurementDataListener.add( measurementDataListener );
 	}
 
 	public boolean removeMeasurementDataListener( final IMeasurementDataListener measurementDataListener ) {
 		return this.measurementDataListener.remove( measurementDataListener );
 	}
 	
 	public boolean addRequestListener( final IRequestListener requestListener ) {
 		return this.requestListener.add( requestListener );
 	}
 
 	public boolean removeRequestListener( final IRequestListener requestListener ) {
 		return this.requestListener.remove( requestListener );
 	}
 	
 	public boolean addConnectionStateListener( final IConnectionStateListener requestListener ) {
 		return this.connectionStateListener.add( requestListener );
 	}
 
 	public boolean removeConnectionStateListener( final IConnectionStateListener requestListener ) {
 		return this.connectionStateListener.remove( requestListener );
 	}
 
 	private class InDispatcher implements Runnable {
 	
 		public void run() {
 		
 			while( running ) {
 				
 				if( !inQueue.isEmpty() ) {
 					
 					final byte[] packageArray = inQueue.poll();
 					final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream( packageArray );
 					final DataInputStream dataInputStream = new DataInputStream( byteArrayInputStream );
 					try {
 						dataInputStream.skip( 6 );
 						final char commandId = dataInputStream.readChar();
 						//System.err.println( "The ID of the current Command is: " + Integer.toHexString( commandId ) );
 						Constructor< ? extends IECP1Command > commandConstructor = commands.get( commandId );
 						
 						if( commandConstructor != null ) {
 							final Object[] parameters = new Object[1];
 							parameters[0] = packageArray;
 						
 							IECP1Command command = commandConstructor.newInstance( packageArray );
 							
 							if( command instanceof EngineStatusCommand ) {
 								final Iterator< IEngineStatusListener > it = engineStatusListener.iterator();
 								final EngineStatusCommand engineStatusCommand = (EngineStatusCommand) command;
 								while( it.hasNext() ) {
 									it.next().engineStatusChanged( engineStatusCommand.getEngineStatus() );
 								}
 								playListController.reportAutoplay( engineStatusCommand.isAutoplay() );
 							} else if( command instanceof ChainStatusCommand ) {
 								final Iterator< IChainStatusListener > it = chainStatusListener.iterator();
 								final ChainStatusCommand chainStatusCommand = (ChainStatusCommand) command;
 								while( it.hasNext() ) {
 									it.next().chainStatusChanged( chainStatusCommand );
 								}
 							} else if( command instanceof ErrorCommand ) {
 								final Iterator< IErrorListener > it = errorListener.iterator();
 								final ErrorCommand errorCommand = (ErrorCommand) command;
 								final de.ptb.epics.eve.ecp1.client.model.Error error = new de.ptb.epics.eve.ecp1.client.model.Error( errorCommand );
 								while( it.hasNext() ) {
 									it.next().errorOccured( error );
 								}
 							} else if( command instanceof MeasurementDataCommand ) {
 								final Iterator< IMeasurementDataListener > it = measurementDataListener.iterator();
 								final MeasurementDataCommand measurementDataCommand = (MeasurementDataCommand) command;
 								final MeasurementData measurementData = new MeasurementData( measurementDataCommand );
 								while( it.hasNext() ) {
 									it.next().measurementDataTransmitted( measurementData );
 								}
 								
 							} else if( command instanceof CurrentXMLCommand ) {
 								final CurrentXMLCommand currentXMLCommand = (CurrentXMLCommand)command;
 								playListController.reportCurrentXMLCommand( currentXMLCommand );
 							} else if( command instanceof PlayListCommand ) {
 								final PlayListCommand playListCommand = (PlayListCommand)command;
 								playListController.reportPlayListCommand( playListCommand );
 							} else if( command instanceof GenericRequestCommand ) {
 								final Iterator< IRequestListener > it = requestListener.iterator();
 								final GenericRequestCommand genericRequestCommand = (GenericRequestCommand)command;
 								final Request request = new Request( genericRequestCommand, self );
 								requestMap.put( request.getRequestId(), request );
 								while( it.hasNext() ) {
 									it.next().request( request );
 								}
 								
 							} else if( command instanceof CancelRequestCommand ) {
 								final CancelRequestCommand cancelRequestCommand = (CancelRequestCommand)command;
 								final Request request = requestMap.get( cancelRequestCommand.getRequestId() );
 								if( request != null ) {
 									request.cancelMessage();
 								}
 							} else {
 								System.err.println( "Undispatchable Package with the Type " + command.getClass().getName() );
 							}
 						}
 						else {
 							System.err.println( "Unknown package type with the id: " + Integer.toHexString( commandId ) );
 						}
 						
						
 					} catch( final IOException exception ) {
 						// TODO Auto-generated catch block
 						exception.printStackTrace();
 					} catch( final IllegalArgumentException exception ) {
 						// TODO Auto-generated catch block
 						exception.printStackTrace();
 					} catch( final InstantiationException exception ) {
 						// TODO Auto-generated catch block
 						exception.printStackTrace();
 					} catch( final IllegalAccessException exception ) {
 						// TODO Auto-generated catch block
 						exception.printStackTrace();
 					} catch( final InvocationTargetException exception ) {
 						// TODO Auto-generated catch block
 						exception.printStackTrace();
 					}	
 					
 					
 				} else {
 					try {
 						Thread.sleep( 10 );
 					} catch( final InterruptedException exception ) {
 						// TODO Auto-generated catch block
 						exception.printStackTrace();
 					}
 				}
 				
 			}
 
 		}
 		
 	}
 	
 	public boolean isRunning() {
 		return this.running;
 	}
 	
 	public IPlayController getPlayController() {
 		return this.playController;
 	}
 
 }
