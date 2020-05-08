 package ar.edu.itba.pdc.filters;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import ar.edu.itba.pdc.utils.ConfigurationCommands;
 
 public class Multiplexing{
 	
	private String defaultServer = "hermes.jabber.org";
 	private Map<String, String> usersOwnServers = null;
 	
 	private static Multiplexing instance = null;
 	
 	public static Multiplexing getInstance() {
 		if (instance == null)
 			instance = new Multiplexing();
 		return instance;
 	}
 	
 	/* Desde archivo de configuracion */
 	private Multiplexing() {
 		usersOwnServers = new HashMap<String, String>();
 		String multiplexed = ConfigurationCommands.getInstance().getProperty("multiplex");
 //		defaultServer = ConfigurationCommands.getInstance().getProperty("defaultServer");
 		updateMultiplexedUsers(multiplexed);
 	}
 	
 	/* Para cambios desde consola */
 	public void updateMultiplexedUsers (String rawUsers) {
 		if (rawUsers != null && !rawUsers.equals("")) {
 			for (String s : rawUsers.split(";")) {
 				String[] jid = s.split("@");
 				String[] domain = jid[1].split("/");
 				usersOwnServers.put(jid[0], domain[0]);
 			}
 		}
 	}
 	
 	public String getUserServer(String user) {
 		if (usersOwnServers.containsKey(user))
 			return usersOwnServers.get(user);
 		return defaultServer;
 	}
 	
 	
 
 }
