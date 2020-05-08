 package technical;
 
 enum EServerType {
 	SERVER_MASTER,
 	SERVER_DELEGATION,
 };
 
 public abstract class ServerConfiguration {
 	static ServerConfiguration localConfiguration = null;
	String hostname;
	int port;
 	
 	static int defaultPort = 8080;
 	
 	ServerConfiguration(String hostname, int port)
 	{
 		this.hostname = hostname;
 		this.port = port;
 	}
 	
 	public static ServerConfiguration getLocalConfiguration()
 	{
 		// TODO This is simply for now; when we have actual servers, we should have the
 		// type of the server determined at startup.
 		if (localConfiguration == null)
 			localConfiguration = new MasterConfiguration("localhost", defaultPort, 0, 0, 90, 180);
 		return localConfiguration;
 	}
 	
 	public String getHostname()	{
 		return hostname;
 	}
 	
 	public int getPort() {
 		return port;
 	}
 	
 	public void sendServer(IServerSendable data) {
 		// TODO Work on sending things between servers.
 	}
 	
 	abstract public EServerType getType();
 	abstract public boolean isResponsible(double latitude, double longitude);
 }
