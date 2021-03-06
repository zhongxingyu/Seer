 /*
  *************************************************************************
  * Copyright (c) 2004, 2009 Actuate Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *  Actuate Corporation  - initial API and implementation
  *  
  *************************************************************************
  */ 
 package org.eclipse.birt.data.engine.executor;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.eclipse.birt.data.engine.api.IShutdownListener;
 import org.eclipse.birt.data.engine.core.DataException;
 import org.eclipse.birt.data.engine.core.security.PropertySecurity;
 import org.eclipse.birt.data.engine.core.security.ThreadSecurity;
 import org.eclipse.birt.data.engine.i18n.ResourceConstants;
 import org.eclipse.birt.data.engine.impl.DataEngineSession;
 import org.eclipse.birt.data.engine.odaconsumer.Connection;
 import org.eclipse.birt.data.engine.odaconsumer.ConnectionManager;
 import org.eclipse.birt.data.engine.odaconsumer.PreparedStatement;
 import org.eclipse.birt.data.engine.odi.ICandidateQuery;
 import org.eclipse.birt.data.engine.odi.IDataSource;
 import org.eclipse.birt.data.engine.odi.IDataSourceQuery;
 import org.eclipse.datatools.connectivity.oda.spec.QuerySpecification;
 
 /**
  * Implementation of ODI's IDataSource interface
  */
 class DataSource implements IDataSource
 {
 	private String 		driverName;
     private Map			appContext;
     private Properties	connectionProps = PropertySecurity.createProperties( );
     
     // A pool of open odaconsumer.Connection. Since each connection may support a limited
 	// # of statements, we may need to use more than one connection to handle concurrent statements
 	// This is a set of OpenConnection
 
 	
 	private static Map<DataEngineSession, Map<ConnectionProp, Set<CacheConnection>>> dataEngineLevelConnectionPool = PropertySecurity.createHashMap( );
 	
 	// Currently active oda Statements. This is a map from PreparedStatement to OpenConnection
 	private HashMap statementMap = new HashMap();
 	
 	private static String className = DataSource.class.getName();
 	private static Logger logger = Logger.getLogger( className ); 
 
 	private DataEngineSession session;
 	
 	/**
 	 * 
 	 * @param driverName
 	 * @param connProperties
 	 * @param session
 	 * @param info
 	 */
     public DataSource( String driverName, Map connProperties,
 			DataEngineSession session )
 	{
     	this.driverName = driverName;
     	if ( connProperties != null )
     		this.connectionProps.putAll( connProperties );
     	
     	this.session = session;
     	
     	this.session.getEngine( ).addShutdownListener( new ShutdownListener( session ));
     	
 	}
     private class ShutdownListener implements IShutdownListener
     {
     	private DataEngineSession session;
     	
     	public ShutdownListener( DataEngineSession session )
     	{
     		this.session = session;
     	}
     	
     	public void dataEngineShutdown( )
 		{
     		Thread thread = ThreadSecurity.createThread( new ConnectionReleaser( this.session) );
     		thread.start( );
 		}
 	}
     
     private Set<CacheConnection> getOdaConnections( boolean populateToCache )
 	{
 		synchronized ( DataSource.dataEngineLevelConnectionPool )
 		{
 			if ( DataSource.dataEngineLevelConnectionPool.get( this.session ) == null )
 			{
 				if ( populateToCache )
 				{
 					DataSource.dataEngineLevelConnectionPool.put( this.session,
 							new HashMap<ConnectionProp, Set<CacheConnection>>( ) );
 				}
 				else
 				{
 					return new HashSet<CacheConnection>( );
 				}
 			}
 
 			Map<ConnectionProp, Set<CacheConnection>> odaConnectionsMap = DataSource.dataEngineLevelConnectionPool.get( this.session );
 			ConnectionProp connProp = new ConnectionProp( this.driverName,
 					this.connectionProps,
 					this.appContext );
 			if ( odaConnectionsMap.get( connProp ) == null )
 			{
 				odaConnectionsMap.put( connProp, new HashSet<CacheConnection>( ) );
 			}
 			return odaConnectionsMap.get( connProp );
 		}
 	}
 	/**
 	 * Returns the driverName.
 	 * 
 	 * @return
 	 */
     String getDriverName()
     {
         return driverName;
     }
 
     /**
      * @see org.eclipse.birt.data.engine.odi.IDataSource#addProperty(java.lang.String, java.lang.String)
      */
     public void addProperty(String name, String value) throws DataException
     {
         // Cannot change connection properties if connection is open
     	if ( isOpen( ) )
 			throw new DataException( ResourceConstants.DS_HAS_OPENED );
     	
         connectionProps.put( name, value );
     }
 
 	/*
 	 * @see org.eclipse.birt.data.engine.odi.IDataSource#setAppContext(java.util.Map)
 	 */
 	public void setAppContext( Map context ) throws DataException
 	{
 	    appContext = context;
 	}
 
     /*
      * @see org.eclipse.birt.data.engine.odi.IDataSource#isOpen()
      */
     public boolean isOpen( )
 	{
 		return this.getOdaConnections( false ).size( ) > 0;
 	}
 
     /*
 	 * @see org.eclipse.birt.data.engine.odi.IDataSource#open()
 	 */
     public void open( ) throws DataException
 	{
 		// No op if we are already open
 		if ( isOpen( ) )
 			return;
 
 		// If no driver name is specified, this is an empty data source used
 		// Sole for
 		// processing candidate queries. Open() is a no-op.
 		if ( driverName == null || driverName.length( ) == 0 )
 			return;
 
 		// Create first open connection
 		newConnection( );
 	}
     
     /**
      * Opens a new Connection and add it to the pool
      * 
      * @return
      * @throws DataException
      */
     private CacheConnection newConnection() throws DataException
     {
     	CacheConnection conn = new CacheConnection();
     	conn.odaConn = ConnectionManager.getInstance().openConnection( 
     			driverName, connectionProps, appContext );
     	int max = conn.odaConn.getMaxQueries();
     	if ( max != 0 )		//	0 means no limit
     		conn.maxStatements = max;
     	this.getOdaConnections( true ).add( conn );
     	return conn;
     }
     
     /** 
      * Find a connection available for new statements in the pool, or create
      * a new one if none available 
      */
     private CacheConnection getAvailableConnection() throws DataException
 	{
     	Iterator it = this.getOdaConnections( true ).iterator();
     	while ( it.hasNext() )
     	{
     		CacheConnection c = (CacheConnection) (it.next());
     		if ( c.currentStatements < c.maxStatements )
     			return c;
     	}
     	
     	// No more available connections; create a new one
     	return newConnection();
 	}
 
     /*
      * @see org.eclipse.birt.data.engine.odi.IDataSource#newQuery(java.lang.String, java.lang.String)
      */
     public IDataSourceQuery newQuery(String queryType, String queryText, boolean fromCache ) throws DataException
     {
     	if ( fromCache )
 		{
 			return new org.eclipse.birt.data.engine.executor.dscache.DataSourceQuery( this.session );
 		}
 		else
 		{// Allow a query to be created on an unopened data source
 			return new DataSourceQuery( this,
 					queryType,
 					queryText,
 					this.session );
 		}
     }
 
     /*
      * @see org.eclipse.birt.data.engine.odi.IDataSource#newCandidateQuery()
      */
     public ICandidateQuery newCandidateQuery( boolean fromCache ) throws DataException
 	{	
 		if ( fromCache )
 		{
 			return new org.eclipse.birt.data.engine.executor.dscache.CandidateQuery( session );
 		}
 		else
 		{
 			// Allow a query to be created on an unopened data source
 			return new CandidateQuery( this.session );
 		}
 	}
 
     /**
      * Prepares an ODA Statement. May use an existing Connection from the pool
      * which has free active statements, or a new connection if all connections
      * in pool have readed their maximum active statements.
      * Returned PreparedStatement must be closed by calling closeStatement.
      */
     @SuppressWarnings("restriction")
     synchronized PreparedStatement prepareStatement ( String queryText, String dataSetType, 
             QuerySpecification querySpec )
     	throws DataException
     {
         assert isOpen();
         CacheConnection conn = getAvailableConnection();
         assert conn.currentStatements < conn.maxStatements;
         ++ conn.currentStatements;
         PreparedStatement stmt = conn.odaConn.prepareStatement( queryText, dataSetType, querySpec );
         
         // Map statement to the open connection, so we can release the connection
         // when statement is closed
         this.statementMap.put( stmt, conn );
         return stmt;
     }
     
     /**
      * Closes a PreparedStatement returned by the prepareStatement call. Frees the associated
      * ODA Connection and make it available for new statements. 
      */
     synchronized void closeStatement ( PreparedStatement stmt )
     {
     	assert stmt != null;
     	// Find the associated connection
     	CacheConnection conn = (CacheConnection) statementMap.remove( stmt );
     	if ( conn == null )
     	{
     		// unexpected error: stmt not created by us
     		logger.logp( Level.WARNING, className, "closeStatement",
     				"statement not found");
     		// Fall through and call close() on stmt any way
     	}
     	else
     	{
     		-- conn.currentStatements;
     		if ( conn.currentStatements < 0 )
         		logger.warning( DataSource.class.getName() + ".closeStatement: negative statement count for connection.");
     		
     		// TODO: consider releasing connections here if we have more than 1 free connections
     	}
     	
     	try
 		{
     		stmt.close();
 		}
         catch ( DataException e )
         {
     		logger.logp( Level.FINE, className, "closeStatement",
     					"Exception at PreparedStatement.close()", e );
         }
     }
     
     /*
      * @see org.eclipse.birt.data.engine.odi.IDataSource#canClose()
      */
     public boolean canClose( )
 	{
 		return statementMap.size( ) == 0;
 	};
     
     /*
      * force to close all statements and connections
      * 
      * @see org.eclipse.birt.data.engine.odi.IDataSource#close()
      */
     public void close( )
 	{
 		Thread thread = ThreadSecurity.createThread( new DataSourceReleaser(
 				this ) );
 		thread.start( );
 	}
     
     /*
 	 * @see java.lang.Object#finalize()
 	 */
     public void finalize( )
 	{
 		// Makes sure no connection is leaked
 		if ( isOpen( ) )
 		{
 			close( );
 		}
 	}
     
     private static final class DataSourceReleaser implements Runnable
     {
     	private DataSource source = null;
     	    	
     	public DataSourceReleaser( DataSource st )
     	{
     		source = st;
     	}
     
     	public void run( )
 		{
 			try
 			{
 				// in normal case, canClose needs to be called to make sure there is no
 				// statement which is under use. but in the end of service of data
 				// engine, all
 				// statemens or connections needs to be forced to close.
 				if ( source.statementMap.size( ) > 0 )
 				{
 					Iterator keySet = source.statementMap.keySet( ).iterator( );
 					while ( keySet.hasNext( ) )
 					{
 						PreparedStatement stmt = (PreparedStatement) keySet.next( );
 						try
 						{
 							stmt.close( );
 						}
 						catch ( Exception e )
 						{
 							logger.logp( Level.FINE,
 									className,
 									"close",
 									"Exception at PreparedStatement.close()",
 									e );
 						}
 					}
 
 					source.statementMap.clear( );
 				}
 
 				// Close all open connections
 				Set<CacheConnection> it = source.getOdaConnections( false );
 
 				if ( it.size( ) > 1 )
 				{
 					CacheConnection conn = it.iterator( ).next( );
 					conn.currentStatements = 0;
 
 					it.remove( conn );
 					for ( CacheConnection connections : it )
 					{
 						try
 						{
 							connections.odaConn.close( );
 						}
 						catch ( Exception e )
 						{
 							logger.logp( Level.FINE,
 									className,
 									"close",
 									"Exception at Connection.close()",
 									e );
 						}
 					}
 
 					it.clear( );
 					it.add( conn );
 				}
 			}
 			catch ( Exception e )
 			{
 				// TODO Auto-generated catch block
 				e.printStackTrace( );
 			}
 
 		}
     	
     	
     }
     
     private static final class ConnectionReleaser implements Runnable
     {
     	private DataEngineSession session = null;
     	    	
     	public ConnectionReleaser( DataEngineSession st )
     	{
     		session = st;
     	}
     
		public synchronized void run( )
 		{
 			try
 			{
 				Map<ConnectionProp, Set<CacheConnection>> odaConnectionsMap = DataSource.dataEngineLevelConnectionPool
 						.remove( session );
 				if ( odaConnectionsMap == null )
 					return;
 
 				for ( Set<CacheConnection> set : odaConnectionsMap.values( ) )
 				{
 					for ( CacheConnection conn : set )
 					{
 						try
 						{
 							conn.odaConn.close( );
 						}
 						catch ( DataException e )
 						{
 							// TODO Auto-generated catch block
 							e.printStackTrace( );
 						}
 					}
 				}
 			}
 			catch ( Exception e )
 			{
 				// TODO Auto-generated catch block
 				e.printStackTrace( );
 			}
 
 		}
 
     }
     // Information about an open oda connection
 	static private final class CacheConnection
 	{
 		Connection odaConn;
 		int maxStatements = Integer.MAX_VALUE; // max # of supported concurrent statements
 		int currentStatements = 0; // # of currently active statements
 	}
 	
 	static private final class ConnectionProp
 	{
 		private String driverName;
 		private Properties props;
 		private Map appContext;
 		
 		public ConnectionProp( String driverName, Properties connectionProps, Map appContext )
 		{
 			this.driverName = driverName;
 			this.props = connectionProps;
 			this.appContext = appContext;
 		}
 		
 		/**
 		 * 
 		 * @param m1
 		 * @param m2
 		 * @return
 		 */
 		private boolean twoMapEquals( Map<?, ?> m1, Map<?, ?> m2 )
 		{
 			if ( m1.size( ) != m2.size( ) )
 				return false;
 
 			for ( Object key : m1.keySet( ) )
 			{
 				Object o1 = m1.get( key );
 				Object o2 = m2.get( key );
 				if ( o1 == o2 )
 					continue;
 				else if( o1 == null || o2 == null )
 					return false;
 				else if ( !o1.equals( o2 ) )
 					return false;
 			}
 			return true;
 
 		}
 
 		@Override
 		public int hashCode( )
 		{
 			return this.driverName == null ?0:this.driverName.hashCode( );
 		}
 
 		@Override
 		public boolean equals( Object obj )
 		{
 			if ( this == obj )
 				return true;
 			if ( obj == null )
 				return false;
 			if ( getClass( ) != obj.getClass( ) )
 				return false;
 			ConnectionProp other = (ConnectionProp) obj;
 			if ( appContext == null )
 			{
 				if ( other.appContext != null )
 					return false;
 			}
 			else if ( !twoMapEquals(appContext, other.appContext ) )
 				return false;
 			if ( driverName == null )
 			{
 				if ( other.driverName != null )
 					return false;
 			}
 			else if ( !driverName.equals( other.driverName ) )
 				return false;
 			if ( props == null )
 			{
 				if ( other.props != null )
 					return false;
 			}
 			else if ( !twoMapEquals( props, other.props ) )
 				return false;
 			return true;
 		}
 	}
 }
