 /**
  * 
  */
 package edu.thu.keg.mdap_impl;
 
 import java.sql.SQLException;
 import java.util.HashMap;
 
 import edu.thu.keg.mdap.DataProviderManager;
 import edu.thu.keg.mdap.provider.DataProvider;
 import edu.thu.keg.mdap_impl.provider.HiveProvider;
 import edu.thu.keg.mdap_impl.provider.JdbcProvider;
 import edu.thu.keg.mdap_impl.provider.OracleProvider;
 
 /**
  * @author Yuanchao Ma, Bozhi Yuan
  * 
  */
 public class DataProviderManagerImpl implements DataProviderManager {
 
 	private static DataProviderManagerImpl instance = null;
 
 	public synchronized static DataProviderManagerImpl getInstance() {
 		// TODO multi-thread
 		if (instance == null)
 			instance = new DataProviderManagerImpl();
 		return instance;
 	}
 
 	private HashMap<String, DataProvider> providers;
 
 	private DataProviderManagerImpl() {
 		this.providers = new HashMap<String, DataProvider>();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see edu.thu.keg.mdap.DataProviderManager#getProvider(java.lang.String)
 	 */
 	@Override
 	public DataProvider getProvider(String connString) {
 		if (providers.containsKey(connString)) {
 			return providers.get(connString);
 		} else {
 			DataProvider p = null;
 			if (connString.startsWith("jdbc:oracle:thin:"))
 				p = new OracleProvider(connString);
			else if (connString.startsWith("jdbc:microsoft:"))
 				p = new JdbcProvider(connString);
 			providers.put(connString, p);
 			return p;
 		}
 	}
 
 	@Override
 	public DataProvider getDefaultSQLProvider(String dbName, String user,
 			String password) {
 		String address = Config.getProperty(Config.SqlAddress);
		String conn = "jdbc:microsoft:sqlserver:" + address + ";databaseName="
				+ dbName + ";user=" + user + ";password=" + password
				+ ";integratedSecurity=true;";
 		return getProvider(conn);
 	}
 
 	@Override
 	public DataProvider getDefaultOracleProvider(String dbName,
 			String userName, String password) {
 		String address = Config.getProperty(Config.OracleAddress);
 		String conn = "jdbc:oracle:thin:" + userName + "/" + password + "@"
 				+ address + ":" + dbName;
 		return getProvider(conn);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * edu.thu.keg.mdap.DataProviderManager#getDefaultHiveProvider(java.lang
 	 * .String, java.lang.String, java.lang.String)
 	 */
 	@Override
 	public DataProvider getDefaultHiveProvider(String dbName, String userName,
 			String password) {
 		HiveProvider hiveProvider = null;
 		try {
 			String address = Config.getProperty(Config.HiveAddress);
 			hiveProvider = new HiveProvider("jdbc:hive://" + address + "/"
 					+ dbName);
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return hiveProvider;
 	}
 
 }
