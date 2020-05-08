 package com.github.tosdan.utils.sql;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.Savepoint;
 import java.util.List;
 import java.util.Map;
 
 import javax.sql.DataSource;
 
 import org.apache.commons.dbutils.DbUtils;
 import org.apache.commons.dbutils.ResultSetHandler;
 import org.apache.commons.dbutils.handlers.ArrayHandler;
 import org.apache.commons.dbutils.handlers.ArrayListHandler;
 import org.apache.commons.dbutils.handlers.KeyedHandler;
 import org.apache.commons.dbutils.handlers.MapHandler;
 import org.apache.commons.dbutils.handlers.MapListHandler;
 
 
 /**
  * 
  * @author Daniele
  * @version 0.2.0-b2013-07-11
  */
 public class BasicDAO
 {
 	private DataSource dataSource;
 	private ConnectionProvider provider;
 	private Connection conn;
 	private boolean closeConn;
 	
 	/**
 	 * 
 	 * @param dataSource
 	 */
 	@Deprecated
 	public BasicDAO( DataSource dataSource ) {
 		this.dataSource = dataSource;
 		this.setCloseConn( true );
 	}
 
 	/**
 	 * 
 	 * @param provider
 	 */
 	public BasicDAO( ConnectionProvider provider ) {
 		this.provider = provider;
 		this.setCloseConn( true );
 	}
 	
 	/**
 	 * 
 	 * @param conn
 	 */
 	public BasicDAO( Connection conn ) {
 		this.conn = conn;
 		this.setCloseConn( true );
 	}
 	
 	/**
 	 * 
 	 * @param closeConn
 	 * @return Restituisce l'oggetto stesso su cui si esegue il metodo.
 	 */
 	public BasicDAO setCloseConn( boolean closeConn ) {
 		this.closeConn = closeConn;
 		return this;
 	}
 
 	/**
 	 * 
 	 * @param b
 	 * @return
 	 * @throws BasicDAOException
 	 */
 	public BasicDAO setAutoCommit(boolean b) throws BasicDAOException {
 		try {
 			if (this.conn != null)
 				this.conn.setAutoCommit( b );
 		} catch ( SQLException e ) {
 			throw new BasicDAOException( "Errore durante il setAutoCommit().", e );
 		}
 		
 		return this;
 	}
 	
 	/**
 	 * 
 	 * @throws BasicDAOException
 	 */
 	public void commit() throws BasicDAOException {
 		try {
 			this.conn.commit();
 		} catch ( SQLException e ) {
 			throw new BasicDAOException( "Errore durante il commit().", e );
 		}
 	}
 	
 	/**
 	 * 
 	 * @throws BasicDAOException
 	 */
 	public void rollBack() throws BasicDAOException {
 		try {
 			this.conn.rollback();
 		} catch ( SQLException e ) {
 			throw new BasicDAOException( "Errore durante il rollBack().", e );
 		}
 	}
 	
 	/**
 	 * 
 	 * @param savepoint 
 	 * @throws BasicDAOException
 	 */
 	public void rollBack(Savepoint savepoint) throws BasicDAOException {
 		try {
 			this.conn.rollback( savepoint );
 		} catch ( SQLException e ) {
 			throw new BasicDAOException( "Errore durante il rollBack().", e );
 		}
 	}
 	
 	/**
 	 * 
 	 * @return 
 	 * @throws BasicDAOException
 	 */
 	public Savepoint setSavepoint() throws BasicDAOException {
 		try {
 			return this.conn.setSavepoint();
 		} catch ( SQLException e ) {
 			throw new BasicDAOException( "Errore durante il setSavepoint().", e );
 		}
 	}
 	
 	/**
 	 * 
 	 * @param name
 	 * @return 
 	 * @throws BasicDAOException
 	 */
 	public Savepoint setSavepoint(String name) throws BasicDAOException {
 		try {
 			return this.conn.setSavepoint( name );
 		} catch ( SQLException e ) {
 			throw new BasicDAOException( "Errore durante il setSavepoint(String name).", e );
 		}
 	}
 	
 	/**
 	 * 
 	 * @param sql
 	 * @return
 	 * @throws BasicDAOException
 	 */
 	public int update( String sql ) throws BasicDAOException {
 		Connection conn = this.getConnection();
 		MyQueryRunner run = new MyQueryRunner();
 		
 		try {
 			return run.update( conn, sql );
 		} catch ( SQLException e ) {
 			throw new BasicDAOException( "Errore di accesso al database.", e );
 		} finally {
			DbUtils.closeQuietly( conn );
 		}
 	}
 	
 
 	/**
 	 * 
 	 * @param sql
 	 * @return
 	 * @throws BasicDAOException
 	 */
 	public List<Map<String, Object>> runAndGetMapList(String sql)
 			throws BasicDAOException 
 	{
 		ResultSetHandler<List<Map<String, Object>>> rsh = new MapListHandler( new BasicRowProcessorMod() );
 		return ( List<Map<String, Object>> ) runAndGetSomething( sql, rsh );
 	}
 	
 	/**
 	 * 
 	 * @param sql
 	 * @return
 	 * @throws BasicDAOException
 	 */
 	public Map<String, Object> runAndGetMap(String sql) 
 			throws BasicDAOException 
 	{
 		ResultSetHandler<Map<String, Object>> rsh = new MapHandler( new BasicRowProcessorMod() );
 		return ( Map<String, Object> ) runAndGetSomething( sql, rsh );
 	}
 	
 
 	/**
 	 * 
 	 * @param sql
 	 * @return
 	 * @throws BasicDAOException
 	 */
 	public List<Object[]> runAndGetArrayList(String sql) 
 			throws BasicDAOException 
 	{
 		ResultSetHandler<List<Object[]>> rsh = new ArrayListHandler( new BasicRowProcessorMod() );
 		return ( List<Object[]> ) runAndGetSomething( sql, rsh );
 	}
 	
 
 	/**
 	 * 
 	 * @param sql
 	 * @return
 	 * @throws BasicDAOException
 	 */
 	public Object[] runAndGetArray(String sql) 
 			throws BasicDAOException 
 	{
 		ResultSetHandler<Object[]> rsh = new ArrayHandler( new BasicRowProcessorMod() );
 		return ( Object[] ) runAndGetSomething( sql, rsh );
 	}
 	
 	/**
 	 * 
 	 * @param sql
 	 * @param columnToBeKey
 	 * @return
 	 * @throws BasicDAOException
 	 */
 	public Map<String, Map<String, Object>> runAndGetKeyedMap(String sql, String columnToBeKey) throws BasicDAOException 
 	{
 		ResultSetHandler<Map<String, Map<String, Object>>> rsh = new KeyedHandler<String>( columnToBeKey );
 		return ( Map<String, Map<String, Object>> ) runAndGetSomething( sql, rsh );
 	}
 	
 	
 	/**
 	 * 
 	 * @param sql
 	 * @param rsh
 	 * @return
 	 * @throws BasicDAOException
 	 */
 	public Object runAndGetSomething(String sql, ResultSetHandler<? extends Object> rsh ) 
 			throws BasicDAOException 
 	{
 		Connection conn = this.getConnection();
 		MyQueryRunner run = new MyQueryRunner();
 		
 		try {
 			return run.query( conn, sql, rsh );
 		} catch ( SQLException e ) {
 			throw new BasicDAOException( "Errore di accesso al database.", e );
 		} finally {
 			if (closeConn)
 				DbUtils.closeQuietly( conn );
 		}
 		
 	}
 	
 	/**
 	 * 
 	 * @return
 	 * @throws BasicDAOException 
 	 */
 	private Connection getConnection() throws BasicDAOException
 	{
 		if (this.conn == null) {
 			
 			if (this.dataSource != null) {
 				
 				try {
 					return this.dataSource.getConnection();
 				} catch ( SQLException e ) {
 					throw new BasicDAOException( "Errore durante la connessione.", e );
 				}
 				
 			} else if (this.provider != null) {
 				
 				try {
 					return provider.stabilisciConnessione();
 				} catch ( ConnectionProviderException e ) {
 					throw new BasicDAOException( "Errore durante la connessione.", e );
 				}
 				
 			} else {
 				throw new BasicDAOException("Non  stata fornita una sorgente valida per stabilire una connessione. Datasource, ConnectinProvider o Connection sono null.");
 			}
 		}
 
 		return conn;
 	}
 	
 	/**
 	 * Chiude la connessione (se non nulla) nascondendo evenutali eccezioni.
 	 */
 	public void closeQuietly() {
 		DbUtils.closeQuietly( this.conn );
 		
 	}
 	
 	/**
 	 * Chiude la connessione senza alcun controllo preventivo. Eventuali controlli di valore null sono lasciato al chiamante.
 	 * @throws SQLException
 	 */
 	public void close() throws SQLException {
 		this.conn.close();
 		
 	}
 	
 }
