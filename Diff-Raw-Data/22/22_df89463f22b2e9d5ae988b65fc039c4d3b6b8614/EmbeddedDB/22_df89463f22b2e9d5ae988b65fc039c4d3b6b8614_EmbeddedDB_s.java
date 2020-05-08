 package de.walware.ecommons.edb;
 
 import java.sql.Connection;
 import java.sql.Driver;
 
 import javax.sql.DataSource;
 
 import org.apache.commons.dbcp.ConnectionFactory;
 import org.apache.commons.dbcp.DriverConnectionFactory;
 import org.apache.commons.dbcp.PoolableConnectionFactory;
 import org.apache.commons.dbcp.PoolingDataSource;
 import org.apache.commons.pool.ObjectPool;
 import org.apache.commons.pool.impl.GenericObjectPool;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 
 import de.walware.ecommons.edb.internal.Activator;
 
 
 public class EmbeddedDB {
 	
 	
 	public static DataSource createConnectionPool(final String uri) throws CoreException {
 		if (System.getProperty("derby.system.home") == null) {
 			final IPath location = Activator.getDefault().getStateLocation();
 			System.setProperty("derby.system.home", location.toOSString());
 		}
 		
 		try {
			final java.sql.Driver driver = (Driver) Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
 			
			final Connection connection = driver.connect("jdbc:derby:"+uri+";create=true", null);
 			connection.close();
 			
 //			final EmbeddedConnectionPoolDataSource dataSource = new EmbeddedConnectionPoolDataSource();
 //			dataSource.setDatabaseName(uri);
 //			return dataSource;
 			final ObjectPool connectionPool = new GenericObjectPool(null);
 			final ConnectionFactory connectionFactory = new DriverConnectionFactory(driver, "jdbc:derby:"+uri, null);
 			new PoolableConnectionFactory(connectionFactory, connectionPool, null, null, false, true); // registers itself
 			return new PoolingDataSource(connectionPool);
 		}
 		catch (final Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, -1, "An error occurred when loading embedded DB (Derby + DBCP, URI="+uri+".", e));
 		}
 	}
 	
 }
