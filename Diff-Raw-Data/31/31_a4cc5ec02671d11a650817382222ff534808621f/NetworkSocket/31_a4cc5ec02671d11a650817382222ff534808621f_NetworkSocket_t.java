 package battlechallenge.network;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 
 /**
  * The Class NetworkSocket.
  */
 public class NetworkSocket {
 	
 	/** The socket. */
 	private Socket socket;
 	
 	/** The oos. */
 	private ObjectOutputStream oos;
 	
 	/** The ois. */
 	private ObjectInputStream ois;
 	
 	/** The bis. */
 	private BufferedInputStream bis;
 	
 	/**
 	 * Checks if is open.
 	 *
 	 * @return true, if is open
 	 */
 	public boolean isOpen() {
 		return socket != null;
 	}
 
 	/**
 	 * Instantiates a new network socket.
 	 *
 	 * @param ip the ip
 	 * @param port the port
 	 */
 	public NetworkSocket(String ip, int port) {
 		// TODO: validate port and IP
 		try {
 			// open a socket connection
 			socket = new Socket(ip, port);
 			// open I/O streams for objects
 			oos = new ObjectOutputStream(socket.getOutputStream());
 			bis = new BufferedInputStream(socket.getInputStream());
 			ois = new ObjectInputStream(bis);
 		} catch (IOException e) {
 			// TODO: Handle client cannot connect to server
 			System.err.println("Application Exception: Invalid network connection parameters.");
 			socket = null;
 		}
 	}
 
 	/**
 	 * Instantiates a new network socket.
 	 *
 	 * @param socket the socket
 	 */
 	public NetworkSocket(Socket socket) {
 		try {
 			this.socket = socket;
 			if (socket != null) {
 				// open I/O streams for objects
 				oos = new ObjectOutputStream(socket.getOutputStream());
 				bis = new BufferedInputStream(socket.getInputStream());
 				ois = new ObjectInputStream(bis);
 			}
 		} catch (IOException e) {
 			System.err.println("Application Exception: Invalid network connection parameters.");
 			socket = null;
 		}
 	}
 
 	/**
 	 * Closes all I/O streams then socket. Does not check if 
 	 * sent bytes have been received.
 	 */
 	public void kill() {
 		if (socket == null) // Socket already closed
 			return;
 		try {
 			oos.close();
 		} catch (IOException e) { /* ignore exceptions */
 		}
 		try {
 			ois.close();
 		} catch (IOException e) { /* ignore exceptions */
 		}try {
 			bis.close();
 		} catch (IOException e) { /* ignore exceptions */
 		}
 		try {
 			socket.close();
 			socket = null;
 		} catch (IOException e) { /* ignore exceptions */
 		}
 	}
 	
 	/**
 	 * Write object to socket. Handles socket exceptions by closing socket.
 	 * After exception this network socket cannot be used for communication.
 	 *
 	 * @param o the o
 	 * @return true, if successful
 	 * @throws ConnectionLostException the connection lost exception
 	 */
 	public boolean writeObject(Object o) throws ConnectionLostException {
 		if (socket == null)
 			throw new ConnectionLostException();
 		if (o != null) {
 			try {
 				// clear object cache
 				oos.reset();
 				oos.writeObject(o);
 				// Ensure objects are sent.
 				oos.flush();
 				return true;
 			} catch (IOException e) {
 				System.err.println("Socket Exception: Cannot write to socket.");
 				socket = null;
 				throw new ConnectionLostException("Socket Exception: Cannot write to socket.");
 			}
 		}
 		return false;
 	}
 	
 	public boolean writeObject(Object o, int timeout) throws ConnectionLostException {
 		if (socket == null)
 			throw new ConnectionLostException();
 		if (o != null) {
 			try {
 				// clear object cache
 				oos.reset();
 				oos.writeObject(o);
 				// Ensure objects are sent.
 				oos.flush();
 				Thread.sleep(timeout);
 				return true;
 			} catch (IOException e) {
 				System.err.println("Socket Exception: Cannot write to socket.");
 				socket = null;
 				throw new ConnectionLostException("Socket Exception: Cannot write to socket.");
 			} catch (InterruptedException e) {
 				/* Ignore. Message was sent. */
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * Write int to socket. Handles socket exceptions by closing socket.
 	 * After exception this network socket cannot be used for communication.
 	 *
 	 * @param i the i
 	 * @return true, if successful
 	 * @throws ConnectionLostException the connection lost exception
 	 */
 	public boolean writeInt(int i) throws ConnectionLostException {
 		if (socket == null)
 			throw new ConnectionLostException();
 		try {
 			oos.writeInt(i);
 			// Ensure objects are sent.
 			oos.flush();
 			return true;
 		} catch (IOException e) {
 			System.err.println("Socket Exception: Cannot write to socket.");
 			socket = null;
 			throw new ConnectionLostException("Socket Exception: Cannot write to socket.");
 		}
 	}
 	
 	/**
 	 * Reads an object from the socket. If nothing is in the buffer and the
 	 * read is not blocking then null is returned.
 	 *
 	 * @param blocking defines if the read should be blocking or not.
 	 * @return the object, null if not blocking and nothing received.
 	 * @throws ConnectionLostException the connection lost exception
 	 */
 	public Object readObject(boolean blocking) throws ConnectionLostException {
 		if (socket == null)
 			throw new ConnectionLostException();
 		try {
 			/*
 			 * check to see if something is arrived in time. Otherwise, assume
 			 * no input. 
 			 */
 			return bis.available() > 0 || blocking ? ois.readObject() : null;
 		} catch (IOException e) {
 			System.err.println("Socket Exception: Cannot read from socket.");
 			socket = null;
 			throw new ConnectionLostException("Socket Exception: Cannot write to socket.");
 		} catch (ClassNotFoundException e) {
 			System.err.println("Network Exception: Cannot receive game object from server. Check server version.");
 		}
 		return null;
 	}
 	
 	/**
 	 * Reads an object from the socket. If something has been received then the 
 	 * object is returned immediately. If nothing is found then this thread will
 	 * will sleep for timeout milliseconds then return a non-blocking read.
 	 *
 	 * @param timeout the milliseconds to wait if nothing received.
 	 * @return the object, null if time expired and nothing received.
 	 * @throws ConnectionLostException the connection lost exception
 	 */
 	public Object readObject(int timeout) throws ConnectionLostException {
 		if (socket == null)
 			throw new ConnectionLostException();
 		try {
 			/*
 			 * check to see if something is arrived in time. Otherwise, assume
 			 * no input. 
 			 */
 			int count = 0;
 			int pause = timeout / 10;
 			while(count * pause < timeout) {
 				if (bis.available() > 0)
 					return ois.readObject();
 				Thread.sleep(pause);
 			}
 			return bis.available() > 0 ? ois.readObject() : null;
 		} catch (IOException e) {
 			System.err.println("Socket Exception: Cannot read from socket.");
 			socket = null;
 			throw new ConnectionLostException("Socket Exception: Cannot write to socket.");
 		} catch (ClassNotFoundException e) {
 			System.err.println("Network Exception: Cannot receive game object from server. Check server version.");
 		} catch (InterruptedException e) {
 			return readObject(false);
 		}
 		return null;
 	}
 	
 	/**
 	 * Reads an Integer from the socket. If nothing is in the buffer and the
 	 * read is not blocking then null is returned.
 	 *
 	 * @param blocking the blocking
 	 * @return the Integer, null if time expired and nothing received.
 	 * @throws ConnectionLostException the connection lost exception
 	 */
 	public Integer readInt(boolean blocking) throws ConnectionLostException {
 		if (socket == null)
 			throw new ConnectionLostException();
 		try {
 			/*
 			 * check to see if something is arrived in time. Otherwise, assume
 			 * no input. 
 			 */
 			return bis.available() > 0 || blocking ? ois.readInt(): null;
 		} catch (IOException e) {
 			System.err.println("Socket Exception: Cannot read from socket.");
 			socket = null;
 			throw new ConnectionLostException("Socket Exception: Cannot write to socket.");
 		}
 	}
 }
