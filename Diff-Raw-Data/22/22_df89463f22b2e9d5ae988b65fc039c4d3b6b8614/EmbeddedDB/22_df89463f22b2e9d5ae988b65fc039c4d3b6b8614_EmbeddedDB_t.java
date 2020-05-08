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
import org.apache.derby.jdbc.EmbeddedDriver;
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
 		
		final String driverName = "org.apache.derby.jdbc.EmbeddedDriver";
		final String dbUrl = "jdbc:derby:"+uri+";create=true";
		Driver driver = null;
 		try {
//			driver = (Driver) Class.forName(driverName, true, EmbeddedDB.class.getClassLoader()).newInstance();
			driver = new EmbeddedDriver();
 			
			final Connection connection = driver.connect(dbUrl, null);
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
			final StringBuilder message = new StringBuilder("An error occurred when loading embedded DB");
			message.append(" (Derby + DBCP)");
			message.append("\n\tDB ConnectionURL=").append(uri);
			message.append("\n\tDriver Name=").append(driverName);
			if (driver != null) {
				message.append(", Version=").append(driver.getMajorVersion()).append(".").append(driver.getMinorVersion());
			}
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, -1, message.toString(), e));
 		}
 	}
 	
 }
