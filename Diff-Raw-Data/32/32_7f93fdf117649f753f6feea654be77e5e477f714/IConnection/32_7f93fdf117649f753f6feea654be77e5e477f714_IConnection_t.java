 package uk.sean.connect;
 
 public interface IConnection {
 	public abstract void connect(ConnectionOptions config) throws ConnectionException;
 	public abstract String sendCommand(String command) throws ConnectionException;
 	public abstract void disconnect();
	public boolean isConnected();
 }
