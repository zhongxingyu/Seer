 package edu.cs319.connectionmanager.clientside;
 
 import java.io.IOException;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 import edu.cs319.client.IClient;
 import edu.cs319.server.IServer;
 

 public class ConnectionFactory {
 
	public static Proxy connect(String host, int port, IClient actualClient) throws UnknownHostException, IOException {
		return new ProxyImpl(new Socket(host,port), actualClient);
 	}
	
	private class ProxyImpl implements Proxy {
 
 		private IServer server;
 		private ClientDecoder client;
 		private Socket socket;
 
 		public ProxyImpl(Socket socket, IClient actualClient) throws IOException {
 			this.socket = socket;
 			this.server = new ServerEncoder(socket.getOutputStream());
 			this.client = new ClientDecoder(actualClient, socket.getInputStream());
 		}
 
 		public IServer getServer() {
 			return server;
 		}
 
 		public void close() throws IOException {
				socket.close();
 		}
 
 	}
 
 }

