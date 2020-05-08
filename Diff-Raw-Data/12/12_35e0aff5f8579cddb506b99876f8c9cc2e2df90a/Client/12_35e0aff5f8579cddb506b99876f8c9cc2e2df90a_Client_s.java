 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.io.Serializable;
 import java.net.ConnectException;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Dictionary;
 import java.util.Hashtable;
 
 /**
  * Client class provide methods to create socket connection to server and other
  * clients. Client can send find, download, getLoad, updatelist requests to
  * clients and server correspondingly. Ping server, generate checksum value for
  * every file under it's directory, and upload files are also provided. Multiple
  * threads are being used for the socket connection, so that client can download
  * and upload multiple files
  * 
  * Client listen two ports number according different request. The default port
  * number is used for file transfer. Another port is used for rend getload and
  * download requests
  * 
  * @author Fan Zhang, Zhiqi Chen
  * 
  */
 public class Client implements Serializable {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 6795028045843900305L;
 	public int load;
 	public String ip;
 	public int port;
 	public FileList list;
 	public String files;
 	public String filepath;
 	public String tempName;
 	public FileSender sender = null;
 	public Socket clientSocket;
 	public Socket trackingServerSocket;
 	public ArrayList<ClientModel> targetClients = new ArrayList<ClientModel>();
 	public Dictionary checksums = new Hashtable();
 
 	public Client(int port, String filepath) {
 		try {
 			ip = InetAddress.getLocalHost().getHostAddress();
 			this.port = port;
 			this.filepath = filepath;
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		}
 		load = 0;
 		tempName = "";
 		list = new FileList(filepath);
 		setFiles();
 		new Checksum(this).setAll();
 		new PingServer(this);
 		// Start listening incoming socket on given port for file transfer
 		// purpose
 		new ListenSocket(port, this);
 		// Start listening incoming socket on given port for get load or
 		// download command purpose
 		new ListenSocket(port + 1, this);
 	}
 
 	/**
 	 * Create a socket connection with requested client, and send file by class
 	 * FileSender
 	 */
 	public void sendFile(String ip, int port, String filename) {
 		try {
 			// Create a connected socket to specified IP and port
 			clientSocket = new Socket(ip, port);
 		} catch (ConnectException e) {
 			System.out
 					.println("Client is unable to provide service at this time, please try later");
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		sender = new FileSender(clientSocket, list.getList(filename), this);
 		sender.send();
 	}
 
 	/**
 	 * Update client's information to server, include client's ip, port, file
 	 * path, and file list
 	 */
 	public void reportToServer() {
 		try {
 			// Create a connected socket to tracking server's IP and port
 			trackingServerSocket = new Socket(Config.serverIP,
 					Config.serverPort);
 			OutputStream os = trackingServerSocket.getOutputStream();
 			ObjectOutputStream oos = new ObjectOutputStream(os);
 			// client may receive new files, so need to update the file list
 			// before update to server
 			setFiles();
 			// The client model represents current client's information
 			ClientModel cm = new ClientModel(ip, port, filepath, files);
 			cm.setChecksums(checksums);
 			oos.writeObject(cm);
 			oos.close();
 			os.close();
 			trackingServerSocket.close();
 			System.out.println("Client information has been updated to server");
 		} catch (ConnectException e) {
 			System.out
 					.println("Server is unable to provide service at this time, please try later");
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Find the given file name on the server's record, return all the client
 	 * who has the file Suggest to specify the file extension
 	 * 
 	 * @param fileName
 	 * @return targetClients
 	 */
 	public ArrayList<ClientModel> find(String fileName) {
 		tempName = fileName;
 		try {
 			// Create a connected socket to tracking server's IP and port
 			trackingServerSocket = new Socket(Config.serverIP,
 					Config.serverPort);
 			// Send out the file name
 			OutputStream os = trackingServerSocket.getOutputStream();
 			ObjectOutputStream oos = new ObjectOutputStream(os);
 			oos.writeObject(fileName);
 
 			// Receive the result: ArrayList<ClientModel>
 			InputStream is = trackingServerSocket.getInputStream();
 			ObjectInputStream ois = new ObjectInputStream(is);
 			ArrayList<ClientModel> newObj = (ArrayList<ClientModel>) ois
 					.readObject();
 			if (newObj.isEmpty()) {
 				System.out.println("No result found for: " + fileName);
 			} else {
 				System.out.println("Receive client list who has: " + fileName);
 				for (ClientModel cm : newObj) {
 					System.out.println(cm.ip + ":" + cm.port);
 				}
 			}
 			ois.close();
 			is.close();
 			oos.close();
 			os.close();
 			trackingServerSocket.close();
 			targetClients = newObj;
 			return newObj;
 		} catch (ConnectException e) {
 			System.out
 					.println("Server is unable to provide service at this time, please try later");
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	// Set or update the file list
 	public void setFiles() {
 		files = list.getList();
 	}
 
 	// set current load
 	public int load() {
 		System.out.println("Current client load: " + load);
 		return load;
 	}
 
 	// PEER SELECTION: get load of the clients and combine with the latency
 	// table, find the 'best' client
 	public ClientModel getLoad() {
 		ClientModel freeClient = null;
 		double mixLoadLatency = 5000.0;
 		// Create a socket connection to every client in the targetClients and
 		// get load of each client
 		for (ClientModel target : targetClients) {
 			// Get the Latency from latency table
 			int currentLatency = Config.latency[Math.abs(this.port
 					- Config.clientPort) / 2][Math.abs(target.port
 					- Config.clientPort) / 2];
 			try {
 				// Get load use another port number
 				Socket s = new Socket(target.ip, target.port + 1);
 				OutputStream os = s.getOutputStream();
 				ObjectOutputStream oos = new ObjectOutputStream(os);
 				oos.writeObject("load");
 				InputStream is = s.getInputStream();
 				ObjectInputStream ois = new ObjectInputStream(is);
 				int clientLoad = (Integer) ois.readObject();
 				target.setLoad(clientLoad);
 				// Get the min load/latency client
 				if (clientLoad / 10.0 * 0.5 + currentLatency / 4900.0 * 0.5 < mixLoadLatency) {
 					mixLoadLatency = (clientLoad / 10 * 0.5 + currentLatency / 4900 * 0.5);
 					freeClient = target;
 				}
 				ois.close();
 				is.close();
 				oos.close();
 				os.close();
 				s.close();
 			} catch (ConnectException e) {
 				System.out
 						.println("Client is unable to provide service at this time, please try later");
 			} catch (UnknownHostException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			} catch (ClassNotFoundException e) {
 				e.printStackTrace();
 			}
 
 		}
 		return freeClient;
 	}
 
 	// send download request
 	public void download() {
 		if (!targetClients.isEmpty()) {
 			ClientModel target = getLoad();
 			// Create a socket connection to the target client and request
 			// sendFile()
 			try {
 				Thread.sleep(1000);
 				Socket s = new Socket(target.ip, target.port + 1);
 				OutputStream os = s.getOutputStream();
 				ObjectOutputStream oos = new ObjectOutputStream(os);
 				oos.writeObject(tempName);
 				oos.close();
 				os.close();
 				s.close();
 			} catch (ConnectException e) {
 				System.out
 						.println("Client is unable to provide service at this time, please try later");
 			} catch (UnknownHostException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		} else {
 			System.out.println("Don't find a client has specified file");
 		}
 	}
 
 }
