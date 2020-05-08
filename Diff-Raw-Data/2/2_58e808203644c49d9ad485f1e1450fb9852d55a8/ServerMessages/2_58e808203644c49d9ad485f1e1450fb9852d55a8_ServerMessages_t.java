 package ecologylab.oodss.distributed.server;
 
 public interface ServerMessages {
 
 	//This is called when adding a server object that the class that implements this will save
 	public void putServerObject(Object o);
 	
 	public String getAPushFromWebSocket(String s, String sessionId);

	public void newClientAdded(String sessionId);
 }
