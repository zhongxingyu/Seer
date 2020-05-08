 package ch.cern.atlas.apvs.server;
 
 import java.beans.PropertyVetoException;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 import javax.sql.DataSource;
 
 import com.mchange.v2.c3p0.ComboPooledDataSource;
 
 public class DbCallback {
 	private ComboPooledDataSource datasource;
 	private ExecutorService executorService;
 	private Future<?> connectFuture;
 	private Future<?> disconnectFuture;
 
 	private final static boolean LOG_DB = false;
 
 	public DbCallback() {
 		executorService = Executors.newSingleThreadExecutor();
 	}
 
 	public void dbConnected(DataSource datasource) throws SQLException {
 	}
 
 	public void dbDisconnected() throws SQLException {
 	}
 
 	public void exceptionCaught(Exception e) {
 	}
 
 	public boolean isConnected() {
 		return datasource != null;
 	}
 
 	public boolean checkConnection() {
 		return isConnected();
 	}
 
 	public void connect(final String url) {
 		disconnect();
 
 		connectFuture = executorService.submit(new Runnable() {
 
 			@Override
 			public void run() {
 				try {
 					datasource = new ComboPooledDataSource();
 					datasource
 							.setDriverClass(LOG_DB ? "net.sf.log4jdbc.DriverSpy"
 									: "oracle.jdbc.OracleDriver");
 					datasource.setJdbcUrl("jdbc:" + (LOG_DB ? "log4jdbc:" : "")
 							+ "oracle:thin:" + url);
 					
 					// FIXME check if this helps...
 					datasource.setMaxStatementsPerConnection(30);
 
 					dbConnected(datasource);
 				} catch (SQLException e) {
 					exceptionCaught(e);
 				} catch (PropertyVetoException e) {
 					exceptionCaught(e);
 				}
 			}
 		});
 
 	}
 
 	public void disconnect() {
 		if (connectFuture != null) {
 			connectFuture.cancel(true);
 		}
 
 		if (datasource == null) {
 			return;
 		}
 
 		if (disconnectFuture != null) {
 			return;
 		}
 
 		disconnectFuture = executorService.submit(new Runnable() {
 
 			@Override
 			public void run() {
 				try {
 					datasource.close();
 					datasource = null;
 					dbDisconnected();
 				} catch (SQLException e) {
 					exceptionCaught(e);
 				}
 			}
 		});
 	}
 
 	protected Connection getConnection() throws SQLException {
 		return datasource.getConnection();
 	}
 }
