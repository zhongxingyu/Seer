 /*
  * Created on Nov 24, 2004
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package no.ntnu.fp.net.co;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.net.ConnectException;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketTimeoutException;
 import java.net.UnknownHostException;
 
 /**
  * @author sebjorns
  * 
  */
 public class SimpleConnection implements Connection {
 
 	private Socket mySocket;
 	private DataOutputStream os;
 	private DataInputStream is;
 	private boolean stop = true;
 	private int myPort;
 
 	public SimpleConnection(int myPort) {
 		this.myPort = myPort;
 	}
 
 	private SimpleConnection(Socket mySocket, int myPort) throws IOException {
 		this(myPort);
 		this.mySocket = mySocket;
 		os = new DataOutputStream(mySocket.getOutputStream());
 		is = new DataInputStream(mySocket.getInputStream());
 
 		stop = false;
 
 		System.out.println("Connection established!");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see no.ntnu.fp.net.co.Connection#connect(java.net.InetAddress, int)
 	 */
 	public void connect(InetAddress remoteAddress, int remotePort)
 			throws IOException, SocketTimeoutException {
 
 		System.out.println("Trying to connect to: "
 				+ remoteAddress.getHostAddress() + " : " + remotePort);
 		mySocket = new Socket(remoteAddress, remotePort);
 
 		os = new DataOutputStream(mySocket.getOutputStream());
 		is = new DataInputStream(mySocket.getInputStream());
 
 		stop = false;
 
 		System.out.println("Connection established!");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see no.ntnu.fp.net.co.Connection#accept(java.net.InetAddress, int)
 	 */
 	public Connection accept() throws IOException, SocketTimeoutException {
 
 		ServerSocket myServerSocket = new ServerSocket(myPort);
 		
 
 		System.out.println("Serversocket lytter p: "
 				+ myServerSocket.getLocalPort());
 		mySocket = myServerSocket.accept();
 		System.out.println("Fikk en oppkobling p: "
 				+ myServerSocket.getLocalPort());
 
 		// skal denne lukkes med en gang? hvis ikke - nr?
 		myServerSocket.close();
 
 		return new SimpleConnection(mySocket, myPort);
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see no.ntnu.fp.net.co.Connection#send(java.lang.String)
 	 */
 	public void send(String msg) throws ConnectException, IOException {
 		if (!mySocket.isClosed()) {
 			os.writeUTF(msg);
 		}else{
 			System.out.println("Socket closed.");
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see no.ntnu.fp.net.co.Connection#receive()
 	 */
 	public String receive() throws ConnectException, IOException {
 		while (!stop) {
 			String s = " ";
 			try {
 				s = is.readUTF();
 			} catch (Exception e) {
 				close();
 			}
 			return s;
 		}
 		throw new IOException(
 				"Can't receive. The connection is not established!");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see no.ntnu.fp.net.co.Connection#close()
 	 */
 	public void close() throws IOException {
 		// clean up:
 		// close the output stream
 		// close the input stream
 		// close the socket
 
 		stop = true;
 		try {
 			os.close();
 			is.close();
 			mySocket.close();
 		} catch (UnknownHostException e) {
 			System.err.println("Trying to connect to unknown host: " + e);
 		}
 	}
 
 }
