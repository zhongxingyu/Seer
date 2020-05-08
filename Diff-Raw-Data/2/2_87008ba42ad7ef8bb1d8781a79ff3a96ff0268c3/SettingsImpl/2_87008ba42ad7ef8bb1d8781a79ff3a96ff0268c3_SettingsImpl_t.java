 package com.opower.util.powerpool;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 
 import com.opower.util.powerpool.provider.GenericSqlConnectionProvider;
 import com.opower.util.powerpool.provider.NewConnectionProvider;
 
   
 /**
  * @author jennifer_andre
  *
  */
 public class SettingsImpl implements Settings {
 	  
  
 	private int maximumIdleTimeSeconds = DEFAULT_MAXIMUM_IDLE_SECONDS;
 	 
 	private int maximumConnections = UNLIMITED_CONNECTIONS;
    
  	private NewConnectionProvider newConnectionProvider;
 	
 	private ConnectionTester validConnectionTester = new ConnectionTester() {
 		
 		@Override
 		public boolean isConnectionValid(Connection c) {
 			
 			try {
 				return !c.isClosed();
 			} catch (SQLException e){
 				// TODO?  swallow this?  i guess it may be useful to know if a connection gets returned.
 			}
 			return false;
 		}
 	};;;
 	  
 	
 	public SettingsImpl(NewConnectionProvider newConnectionProvider) { this.newConnectionProvider = newConnectionProvider; }
 	
 	public SettingsImpl(String jdbcDriver, String connectionString, String userName, String password) throws ClassNotFoundException 
 	{
 		this.newConnectionProvider = new GenericSqlConnectionProvider(jdbcDriver, connectionString, userName, password);
 	}
 	 
 	
 	/* (non-Javadoc)
 	 * @see com.opower.util.powerpool.SettingsI#setValidConnectionTester(com.opower.util.powerpool.ConnectionTester)
 	 */
 	@Override
 	public void setValidConnectionTester(ConnectionTester validConnectionTester) {
 		this.validConnectionTester = validConnectionTester;
 	}
  	
 	/* (non-Javadoc)
 	 * @see com.opower.util.powerpool.SettingsI#getValidConnectionTester()
 	 */
 	@Override
 	public ConnectionTester getValidConnectionTester() {
 		return validConnectionTester;
 	}
 
 	/*
 	 * Set the maximum connections.  Anything >= 0 is a valid value,
 	 * anything less than that will reset the maximumConnections to the default (Integer.MAX_VALUE).
 	 */
 	/* (non-Javadoc)
 	 * @see com.opower.util.powerpool.SettingsI#setMaximumConnections(int)
 	 */
 	@Override
 	public void setMaximumConnections(int maximumConnections) {
		if (maximumConnections > 0)
 			this.maximumConnections = maximumConnections;
 		else
 			this.maximumConnections = UNLIMITED_CONNECTIONS;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.opower.util.powerpool.SettingsI#getMaximumConnections()
 	 */
 	@Override
 	public int getMaximumConnections() {
 		return maximumConnections;
 	}
 
 	
 	/* (non-Javadoc)
 	 * @see com.opower.util.powerpool.SettingsI#setMaximumIdleTimeSeconds(int)
 	 */
 	@Override
 	public void setMaximumIdleTimeSeconds(int maximumIdleTimeSeconds) {
 		this.maximumIdleTimeSeconds = maximumIdleTimeSeconds;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.opower.util.powerpool.SettingsI#getMaximumIdleTimeSeconds()
 	 */
 	@Override
 	public int getMaximumIdleTimeSeconds() {
 		return maximumIdleTimeSeconds;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.opower.util.powerpool.SettingsI#setNewConnectionProvider(com.opower.util.powerpool.provider.NewConnectionProvider)
 	 */
 	@Override
 	public void setNewConnectionProvider(NewConnectionProvider newConnectionProvider) {
 		this.newConnectionProvider = newConnectionProvider;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.opower.util.powerpool.SettingsI#getNewConnectionProvider()
 	 */
 	@Override
 	public NewConnectionProvider getNewConnectionProvider() {
 		return newConnectionProvider;
 	}
  
  	
 }
