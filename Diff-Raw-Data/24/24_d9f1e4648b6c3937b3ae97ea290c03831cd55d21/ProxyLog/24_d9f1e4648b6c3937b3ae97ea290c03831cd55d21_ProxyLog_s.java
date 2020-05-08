 package networking;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.concurrent.atomic.AtomicInteger;
 import javax.swing.JTextArea;
 
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.handler.codec.http.HttpChunk;
 import org.jboss.netty.handler.codec.http.HttpRequest;
 import org.jboss.netty.handler.codec.http.HttpResponse;
 
 /**
  * ProxyLog Class to write out logs for the network system
  * @author Zong Xiong
  */
 public class ProxyLog {
 	public static final int		WROTE_REQUEST	= 0, READ_REQUEST = 1, WROTE_RESPONSE = 2, READ_RESPONSE = 3;
 	public static final Charset	DEFAULT_ENCODING = Charset.defaultCharset();
 	/********************************************************
 	 * Fields
 	 ********************************************************/
 	private volatile static boolean				isLogEnabled = false;
 	private volatile static String				root		= "./proxyLog";
 	private static AtomicInteger				connectionCount = new AtomicInteger(0);
 	private static final Object					connectionLock = new Object(), dialogLock = new Object(), fileLock = new Object();
 	private volatile static JTextArea			countText = null;
 	private volatile static JTextArea 			dialogText = null;
 	private volatile static JTextArea			connectionText = null;
 	private volatile static ArrayList<String>	connectionList = new ArrayList<String>();
 
 	/********************************************************
 	 * Public Methods
 	 ********************************************************/
 	public static void setRootFolder(String path) {
 		root = path;
 	}
 	
 	public static void setLogEnabled(Boolean value) {
 		isLogEnabled = true;
 	}
 	
 	public static void setCountText(JTextArea inText) {
 		countText = inText;
 	}
 	
 	public static void setConnectionText(JTextArea inText) {
 		connectionText = inText;
 	}
 	
 	public static void setDialogText(JTextArea inText) {
 		dialogText = inText;
 	}
 	
 	/**
 	 * clearDialog
 	 * A way to clear the dialogText from the ProxyLog
 	 */
 	public static void clearDialog() {
 		if (dialogText != null) {
 			dialogText.setText("");
 		}
 	}
 	
 	/**
 	 * addConnection
 	 * Adds a connection updating a text area if set and increments 
 	 * the counter of connections also writing that to a text area if set.
 	 * 
 	 * @param client, string representation of client channel
 	 * @param server, string representation of server channel
 	 * 
 	 * @return true if successfully added
 	 */
 	public static boolean addConnection(String client, String server) {
 		synchronized (connectionLock) {
 			// add connection to list
 			connectionList.add(client);
 			connectionList.add(server);
 			
 			// write the new connection to the text
 			// if it has been set
 			if (connectionText != null) {
 				String updatedList = "";
 
 				for(int i = 0; i < connectionList.size(); i += 2) {
 					updatedList += connectionList.get(i);
 					updatedList += " connected to ";
 					updatedList += connectionList.get(i + 1);
 					updatedList += "\n";
 				}
 
 				connectionText.setText(updatedList);
 			}
 			
 			// increase connection count
 			connectionCount.incrementAndGet();
 			
 			// write the new connection count to the text 
 			// if it has been set
 			if (countText != null) {
 				countText.setText(Integer.toString(connectionCount.get()));
 			}
 			return true;
 			
 		}
 	}
 	
 	/**
 	 * removeConnection
 	 * Removes the first instance of the connection and updates the 
 	 * text if set, and decrements the counter also updating the text if set.
 	 * 
 	 * @param client, string representation of client channel
 	 * @param server, string representation of server channel
 	 * 
 	 * @return true if successfully found and removed
 	 */
 	public static boolean removeConnection(String client, String server) {
 		synchronized (connectionLock) {
 			// check connection list for the first instance of client and server
 			for(int i = 0; i < connectionList.size(); i += 2) {
 				if(connectionList.get(i).equals(client)&&connectionList.get(i+1).equals(server)) {
 					// since connection is found we remove it
 					connectionList.remove(i + 1);
 					connectionList.remove(i);
 					
 					// write the new connection to the text
 					// if it has been set
 					if (connectionText != null) {
 						String updatedList = "";
 
 						for(int j = 0; j < connectionList.size(); j += 2) {
 							updatedList += connectionList.get(j);
 							updatedList += " connected to ";
 							updatedList += connectionList.get(j + 1);
 							updatedList += "\n";
 						}
 
 						connectionText.setText(updatedList);
 					}
 					
 					// decrease connection counter
 					connectionCount.decrementAndGet();
 					
 					// write the new connection count to the label 
 					// if it has been set
 					if (countText != null) {
 						countText.setText(Integer.toString(connectionCount.get()));
 					}
 					return true;
 				} // end connection check
 			} // end for loop through connection list
 			return false;
 			
 		}
 	}
 	
 	/**
 	 * appendDialog
 	 * Adds dialog of a particular action to text if set
 	 * 
 	 * @param client, String representation of client
 	 * @param server, String representation of server
 	 * @param action, Integer representation of an action
 	 * 
 	 * @return true if dialog is successfully added
 	 */
 	public static boolean appendDialog(String client, String server, int action) {
 		// check if dialogText is set
 		if (dialogText != null) {
 			String update = "";
 			
 			switch (action) {
 			case WROTE_REQUEST:
 				update += "Wrote a request to ";
 				update += server;
				update += " to ";
				update += server;
 				break;
 			case WROTE_RESPONSE:
 				update += "Wrote a response to ";
 				update += client;
 				update += " from ";
 				update += server;
 				break;
 			case READ_REQUEST:
 				update += "Read a request from ";
 				update += client;
 				update += " to ";
 				update += server;
 				break;
 			case READ_RESPONSE:
 				update += "Read a response from ";
 				update += server;
 				update += " to ";
 				update += client;
 				break;
 			}
 			update += "\n";
 			synchronized (dialogLock) {
 				dialogText.append(update);
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * clientToString
 	 * Used to convert a client channel to a readable string representation
 	 * @param client, channel
 	 * 
 	 * @return readable string representation
 	 */
 	public static String clientToString(Channel client) {
 		String clientString = client.getRemoteAddress().toString().replaceAll(":", ".");
 		int index = clientString.lastIndexOf(".");
 		clientString = clientString.substring(0, index);
 		return clientString;
 	}
 
 	/**
 	 * serverToString
 	 * Used to convert a server channel to a readable string representation
 	 * @param server, channel
 	 * 
 	 * @return readable string representation
 	 */
 	public static String serverToString(Channel server) {
 		return server.getRemoteAddress().toString().split("/")[0];
 	}
 
 	/**
 	 * write
 	 * Writes to log file about transaction
 	 * 
 	 * @param client, String representation of client channel
 	 * @param server, String representation of server channel
 	 * @param msg, HttpResponse
 	 * 
 	 * @return true if written successfully
 	 */
 	public static boolean write(String client, String server, HttpResponse msg) {
 		if (isLogEnabled) {
 			synchronized (fileLock) {
 				Date date = new Date();
 				DateFormat dateformat = new SimpleDateFormat("MM-dd-yyyy");
 				String path = root + "/" + dateformat.format(date) + "/" + client;
 
 				try {
 					File file = new File(path);
 					file.mkdirs();
 
 					if(!file.exists()) {
 						file.createNewFile();
 					}
 
 					FileWriter out = new FileWriter(new File(path + "/" + server + ".txt"), true);
 					out.write("Server writes to client\r\n---------\r\n");
 					out.write(msg.toString());
 					out.write("\r\n---Content---\r\n");
 					//String encoding = msg.getHeader("Content-Encoding");
 					//if (encoding != null&&encoding.equals("gzip")) {
 						//GZIPInputStream gin = new GZIPInputStream(new ByteArrayInputStream(msg.getContent().toByteBuffer().array()));
 						//ByteArrayOutputStream gout = new ByteArrayOutputStream();
 						//IOUtils.copy(gin, gout);
 						//out.write(gout.toString("UTF-8"));
 					//} else {
 						out.write(msg.getContent().toString(DEFAULT_ENCODING));
 					//}
 					out.write("\r\n---End---\r\n\r\n");
 					out.close();
 					return true;
 				}
 				catch(IOException e) {
 					e.printStackTrace();
 					return false;
 				}
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * write
 	 * Writes to log file about transaction
 	 * 
 	 * @param client, String representation of client channel
 	 * @param server, String representation of server channel
 	 * @param msg, HttpChunk
 	 * 
 	 * @return true if written successfully
 	 */
 	public static boolean write(String client, String server, HttpChunk msg) {
 		if (isLogEnabled) {
 			synchronized (fileLock) {
 				Date date = new Date();
 				DateFormat dateformat = new SimpleDateFormat("MM-dd-yyyy");
 				String path = root + "/" + dateformat.format(date) + "/" + client;
 
 				try {
 					File file = new File(path);
 					file.mkdirs();
 
 					if(!file.exists()) {
 						file.createNewFile();
 					}
 
 					FileWriter out = new FileWriter(new File(path + "/" + server + ".txt"), true);
 					out.write("Server writes a chunk to client\r\n---------\r\n");
 					out.write(msg.toString());
 					out.write("\r\n---Content---\r\n");
 					out.write(msg.getContent().toString(DEFAULT_ENCODING));
 					out.write("\r\n---End---\r\n\r\n");
 					out.close();
 					return true;
 				}
 				catch(IOException e) {
 					e.printStackTrace();
 					return false;
 				}
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * write
 	 * Writes to log file about transaction
 	 * 
 	 * @param client, String representation of client channel
 	 * @param server, String representation of server channel
 	 * @param msg, HttpRequest
 	 * 
 	 * @return true if written successfully
 	 */
 	public static boolean write(String client, String server, HttpRequest msg) {
 		if (isLogEnabled) {
 			synchronized (fileLock) {
 				Date date = new Date();
 				DateFormat dateformat = new SimpleDateFormat("MM-dd-yyyy");
 				String path = root + "/" + dateformat.format(date) + "/" + client;
 
 				try {
 					File file = new File(path);
 					file.mkdirs();
 
 					if(!file.exists()) {
 						file.createNewFile();
 					}
 
 					FileWriter out = new FileWriter(new File(path + "/" + server + ".txt"), true);
 					out.write("Client writes to server\r\n---------\r\n");
 					out.write(msg.toString());
 					out.write("\r\n---Content---\r\n");
 					out.write(msg.getContent().toString(DEFAULT_ENCODING));
 					out.write("\r\n---End---\r\n\r\n");
 					out.close();
 					return true;
 				}
 				catch(IOException e) {
 					e.printStackTrace();
 					return false;
 				}
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * write
 	 * Writes to log file with passed String
 	 * 
 	 * @param client, String representation of client channel
 	 * @param server, String representation of server channel
 	 * @param msg, String message to write to log
 	 * 
 	 * @return true if written successfully
 	 */
 	public static boolean write(String client, String server, String msg) {
 		if (isLogEnabled) {
 			synchronized (fileLock) {
 				Date date = new Date();
 				DateFormat dateformat = new SimpleDateFormat("MM-dd-yyyy");
 				String path = root + "/" + dateformat.format(date) + "/" + client;
 
 				try {
 					File file = new File(path);
 					file.mkdirs();
 
 					if(!file.exists()) {
 						file.createNewFile();
 					}
 
 					FileWriter out = new FileWriter(new File(path + "/" + server + ".txt"), true);
 					out.write("Client and Server Message");
 					out.write(msg.toString());
 					out.write("\r\n---End---\r\n\r\n");
 					out.close();
 					return true;
 				}
 				catch(IOException e) {
 					e.printStackTrace();
 					return false;
 				}
 			}
 		}
 		return false;
 	}
 }
