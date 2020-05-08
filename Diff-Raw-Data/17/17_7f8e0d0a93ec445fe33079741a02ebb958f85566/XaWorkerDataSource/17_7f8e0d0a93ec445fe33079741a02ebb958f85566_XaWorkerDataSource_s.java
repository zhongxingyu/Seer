 package org.neo4j.util.xaworker;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.channels.FileChannel;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import javax.transaction.RollbackException;
 import javax.transaction.SystemException;
 import javax.transaction.xa.XAException;
 import javax.transaction.xa.XAResource;
 import org.neo4j.impl.transaction.TxManager;
 import org.neo4j.impl.transaction.xaframework.LogBuffer;
 import org.neo4j.impl.transaction.xaframework.XaCommand;
 import org.neo4j.impl.transaction.xaframework.XaCommandFactory;
 import org.neo4j.impl.transaction.xaframework.XaConnection;
 import org.neo4j.impl.transaction.xaframework.XaConnectionHelpImpl;
 import org.neo4j.impl.transaction.xaframework.XaContainer;
 import org.neo4j.impl.transaction.xaframework.XaDataSource;
 import org.neo4j.impl.transaction.xaframework.XaLogicalLog;
 import org.neo4j.impl.transaction.xaframework.XaResourceHelpImpl;
 import org.neo4j.impl.transaction.xaframework.XaResourceManager;
 import org.neo4j.impl.transaction.xaframework.XaTransaction;
 import org.neo4j.impl.transaction.xaframework.XaTransactionFactory;
 
 public class XaWorkerDataSource extends XaDataSource
 {
 	private XaContainer container;
 	private Map<String, String> parameters;
 	private XaWorker worker;
 	private XaWorkerHook hook;
 	
 	public XaWorkerDataSource( Map<String, String> parameters )
 		throws InstantiationException
 	{
 		super( parameters );
 		this.parameters = parameters;
 		try
 		{
 			this.hook = instantiateWorkerHook( getParameter( "hook" ) );
 			String logPath = getParameter( "log" );
 			this.container = XaContainer.create( logPath + "-logical",
 				new XaWorkerCommandFactory(),
 				new XaWorkerTransactionFactory() );
 			this.container.openLogicalLog();
 
 			this.worker = hook.newXaWorker();
 			this.worker.setHook( hook );
 			this.worker.prepareStartUp( logPath );
 		}
 		catch ( IOException e )
 		{
 			throw new InstantiationException( "Unable to create XaContainer" );
 		}
 	}
 	
 	public void startUp()
 	{
 		this.worker.startUp();
 	}
 	
 	private XaWorkerHook instantiateWorkerHook( String name )
 		throws InstantiationException
 	{
 		try
 		{
 			Class<? extends XaWorkerHook> clazz =
 				( Class<? extends XaWorkerHook> ) Class.forName( name );
 			return clazz.newInstance();
 		}
 		catch ( Exception e )
 		{
 			throw new InstantiationException( e.toString() );
 		}
 	}
 	
 	public XaWorkerHook getHook()
 	{
 		return this.hook;
 	}
 	
 	private String getParameter( String key ) throws InstantiationException
 	{
 		if ( !parameters.containsKey( key ) )
 		{
 			throw new InstantiationException( "Must supply parameter '" +
 				key + "'" );
 		}
 		return parameters.get( key );
 	}
 	
 	@Override
 	public XaConnection getXaConnection()
 	{
 		return new XaWorkerConnection( this.container.getResourceManager() );
 	}
 	
 	@Override
 	public void close()
 	{
 		this.container.close();
 		this.worker.shutDown();
 	}
 
 	public class XaWorkerResource extends XaResourceHelpImpl
 	{
 		public XaWorkerResource( XaResourceManager xaRm ) 
 		{
 			super( xaRm );
 		}
 		
 		@Override
 		public boolean isSameRM( XAResource resource )
 		{
 			return ( resource instanceof XaWorkerResource );
 		}
 	}
 
 	public class XaWorkerConnection extends XaConnectionHelpImpl
 	{
 		private XaWorkerResource resource;
 		
 		public XaWorkerConnection( XaResourceManager manager )
 		{
 			super( manager );
 			this.resource = new XaWorkerResource( manager );
 		}
 		
 		@Override
 		public XAResource getXaResource()
 		{
 			return this.resource;
 		}
 		
 		public void perform( XaWorkerEntry entry ) throws XaWorkerException
 		{
 			try
 			{
 				TxManager.getManager().getTransaction().enlistResource(
 					this.resource );
 				boolean success = false;
 				try
 				{
 					XaTransaction transaction = getTransaction();
 					XaWorkerLogEntry logEntry = getHook().newPreparedLogEntry();
 					logEntry.setEntry( entry );
 					logEntry.setTransactionId( transaction.getIdentifier() );
 					XaWorkerCommand command = new XaWorkerCommand( logEntry );
 					getTransaction().addCommand( command );
 					success = true;
 				}
 				finally
 				{
 					TxManager.getManager().getTransaction().delistResource(
 						this.resource, success ? XAResource.TMSUCCESS :
 						XAResource.TMFAIL );
 				}
 			}
 			catch ( SystemException e )
 			{
 				throw new XaWorkerException( e );
 			}
 			catch ( RollbackException e )
 			{
 				throw new XaWorkerException( e );
 			}
 			catch ( XAException e )
 			{
 				throw new XaWorkerException( e );
 			}
 		}
 	}
 
 	public class XaWorkerTransactionFactory extends XaTransactionFactory
 	{
 		@Override
 		public XaTransaction create( int id )
 		{
 			return new XaWorkerTransaction( id, getLogicalLog() );
 		}
 
 		@Override
 		public void lazyDoneWrite( List<Integer> arg0 ) throws XAException
 		{
 			throw new RuntimeException( "lazy done not supported" );
 		}
 	}
 
 	public class XaWorkerTransaction extends XaTransaction
 	{
 		private List < XaWorkerCommand > commands;
 		
 		public XaWorkerTransaction( int id, XaLogicalLog log )
 		{
 			super( id, log );
 		}
 		
 		@Override
 		public boolean isReadOnly()
 		{
 			return commands == null;
 		}
 		
 		@Override
 		protected void doAddCommand( XaCommand command )
 		{
 			if ( commands == null )
 			{
				commands = new ArrayList();
 			}
 			this.commands.add( ( XaWorkerCommand ) command );
 		}
 		
 		@Override
 		protected void doRollback() throws XAException
 		{
 			this.commands = null;
 		}
 		
 		@Override
 		protected void doCommit() throws XAException
 		{
 			if ( this.commands == null )
 			{
 				return;
 			}
 			
 			for ( XaWorkerCommand command : commands )
 			{
 				command.execute();
 			}
 			try
 			{
 				worker.flushLog();
 			}
 			catch ( IOException e )
 			{
 				throw new XAException( "Unable to flush log: " + e );
 			}
 		}
 
 		@Override
 		protected void doPrepare() throws XAException
 		{
 		}
 	}
 
 	public class XaWorkerCommandFactory extends XaCommandFactory
 	{
 		@Override
 		public XaCommand readCommand( FileChannel channel, ByteBuffer buffer )
 			throws IOException
 		{
 			XaWorkerCommand result = new XaWorkerCommand();
 			result.readFromFile( channel, buffer );
 			return result;
 		}
 	}
 
 	public class XaWorkerCommand extends XaCommand
 	{
 		private XaWorkerLogEntry entry;
 		
 		public XaWorkerCommand( XaWorkerLogEntry entry )
 		{
 			this.entry = entry;
 		}
 		
 		public XaWorkerCommand()
 		{
 			entry = getHook().newPreparedLogEntry();
 		}
 		
 		@Override
 		public void execute()
 		{
 			try
 			{
 				worker.add( this.entry );
 			}
 			catch ( IOException e )
 			{
 				throw new RuntimeException( "This can't be happening", e );
 			}
 		}
 		
 		XaWorkerLogEntry getEntry()
 		{
 			return this.entry;
 		}
 		
 		void readFromFile( FileChannel channel, ByteBuffer buffer )
 			throws IOException
 		{
 			entry.readFromFile( channel, buffer );
 		}
 
 		@Override
         public void writeToFile( LogBuffer buffer ) throws IOException
         {
 			entry.writeToFile( buffer );
         }
 	}
 }
