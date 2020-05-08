 package controller;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.Vector;
 
 /**
  * ObjectReceiveServer.java
  * 
  * @author			Devin Barry
  * @date			24.10.2012
  * @lastModified	25.10.2012
  * 
  * A basic object receive server, modified from code provided for
  * COMPSYS.704 Assignment 1 (during the labs).
  * 
  * The server is parameterized and is setup at creation time to
  * receive objects of a specific type.
  *
  */

//TODO rename type<E> to type type<T> and rename use of T further down by <S>
 public class ObjectReceiveServer<E> implements Runnable {
 	
 	Class<E> type; 
 	private E objectOut;
 	private String port;
 	private SimpleServerQueue<E> q;
 	//private Socket socket;
 
 	@Override
 	public void run() {
 		System.out.println("Starting ObjectReceiveServer...");
 		createObjectReceiveServer();
 	}
 	
 	public ObjectReceiveServer(String port, SimpleServerQueue<E> q, Class<E> type) {
 		this.port = port.trim();
 		this.q = q;
 		this.type = type;
 		//new Thread(this, "ObjectReceiveServer").start();
 	}
 	
 	//Static factory pattern for storing Type inside this object
 	public static <T> ObjectReceiveServer<T> createMyObject(String port, SimpleServerQueue<T> q, Class<T> type) {
 		return new ObjectReceiveServer<T>(port, q, type);
 	}
 	
 	private void createObjectReceiveServer() {
 		ServerSocket serverSocket = null;
 		try {
 			serverSocket = new ServerSocket(Integer.parseInt(port));
 			while (true) {
 				byte[] data = readData(serverSocket);
 				// process data from here
 				if ((data[0] == -84) && (data[1] == -19)) {
 					ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
 					Object o = ois.readObject();
 					
 					//System.out.println(o.getClass());
 					//System.out.println("Received an object of " + o.getClass().getName());
 					//System.out.println(this.type.isAssignableFrom(o.getClass()));
 					
 					if (o != null && this.type.isAssignableFrom(o.getClass())) {
 						//This cast is unchecked and could be improved by looking into object output stream
 						objectOut = (E) o;
 						q.put(objectOut);
 						//System.out.println("Received with value of " + output);
 					}
 				}
 				// finish processing data
 			}
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			if (serverSocket != null) {
 				try {
 					serverSocket.close();
 				} catch (Exception ee) {
 					ee.printStackTrace();
 				}
 			}
 		}
 	}
 
 	private byte[] readData(ServerSocket serverSocket) {
 		Socket clientSocket = null;
 		InputStream in = null;
 		try {
 			int bufferSize = 100;
 			clientSocket = serverSocket.accept();
 			in = clientSocket.getInputStream();
 			byte[] data = new byte[bufferSize];
 			int count = in.read(data, 0, bufferSize);
 			System.out.println("Read " + count + " bytes");
 			//Vector cannot be parameterized because it stores primitive types
 			Vector v = new Vector();
 			int totalLength = 0;
 			while (count != -1) {
 				v.addElement(data);
 				v.addElement(count);
 				totalLength = totalLength + count;
 				data = new byte[bufferSize];
 				count = in.read(data, 0, bufferSize);
 			}
 
 			// use totalLength to create the overall byte string
 			data = new byte[totalLength];
 			int currentPosition = 0;
 			for (int i = 0; i < v.size(); i = i + 2) {
 				int currentLength = ((Integer) v.elementAt(i + 1)).intValue();
 				System.arraycopy(v.elementAt(i), 0, data, currentPosition,
 						currentLength);
 				currentPosition = currentPosition + currentLength;
 			}
 			in.close();
 			clientSocket.close();
 			return data;
 		} catch (Exception e) {
 			e.printStackTrace();
 			if (clientSocket != null) {
 				try {
 					clientSocket.close();
 				} catch (Exception ee) {
 					ee.printStackTrace();
 				}
 			}
 		}
 		return null;
 	}
 }
