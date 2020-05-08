 package consistent;
 
 import java.io.*;
 import java.net.*;
 
 class Connection {
 
 	DataInputStream in;
 	DataOutputStream out;
 	Socket clientSocket;
 
 	private String clientName;
 	private InputStream rawIn;
 
 	private boolean errorOccured;
 
 	public Connection(Socket aClientSocket) throws IOException {
 		errorOccured = false;
 		clientSocket = aClientSocket;
 		rawIn = clientSocket.getInputStream();
 		in = new DataInputStream(rawIn);
 		out = new DataOutputStream(clientSocket.getOutputStream());
 
 		clientName = in.readUTF();
 		System.out.println("Connected: " + clientName);
 	}
 
 	public boolean hasMessage() {
 		try {
 			return error() || rawIn.available() > 0;
 		} catch (IOException e) {
 			return false;
 		}
 	}
 
 	public String readMessage() {
 		try {
 			return String.format("%s: %s", clientName, in.readUTF());
 		} catch (IOException e) {
 			errorOccured = true;
 			return null;
 		}
 	}
 
 	public void writeMessage(String message) {
		if (message == null) {
			throw new IllegalArgumentException("Message Is Null!");
		}
 		if (!error()) {
 			try {
 				out.writeUTF(message);
 			} catch (IOException e) {
 				errorOccured = true;
 			}
 		}
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		return clientName.equals(((Connection) obj).clientName);
 	}
 
 	public void close() throws IOException {
 		clientSocket.close();
 		in.close();
 		out.close();
 	}
 
 	public boolean error() {
 		return errorOccured;
 	}
 }
