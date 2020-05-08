 package Server;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.Vector;
 
 import Library.Library;
 
 /**
  * Purpose is to serve as the protocol of commands between server and client for
  * each client. This thread starts other threads as well.
  * 
  * @author James Harris
  * @version November 2 2012
  */
 public class ClientThread extends Thread {
 
 	private Socket socket;
 	private DataInputStream in;
 	private DataOutputStream out;
 	private Library lib;
 	private Vector<ClientThread> clients;
 	private String host;
 	private int port;
 	private NotifyChangeToLib notifier;
 	private int clientID;
 
 	public ClientThread(Socket sock, Library lib, Vector<ClientThread> clients,
 			int portNo, int id) {
 
 		try {
 			this.setClientID(id);
 			this.port = portNo;
 			this.setSocket(sock);
 			this.setClients(clients);
 			this.setIn(new DataInputStream(this.socket.getInputStream()));
 			this.setOut(new DataOutputStream(this.socket.getOutputStream()));
 			this.setLib(lib);
 			out.write(Integer.toString(clientID).getBytes());
 			setNotifier(new NotifyChangeToLib(out));
 			notifier.setPort((port + 3));
 			notifier.start();
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	public Socket getSocket() {
 		return socket;
 	}
 
 	public void setSocket(Socket socket) {
 		this.socket = socket;
 	}
 
 	public int getPort() {
 		return port;
 	}
 
 	public void setPort(int port) {
 		this.port = port;
 	}
 
 	public String getHost() {
 		return host;
 	}
 
 	public void setHost(String host) {
 		this.host = host;
 	}
 
 	public DataOutputStream getOut() {
 		return out;
 	}
 
 	public void setOut(DataOutputStream out) {
 		this.out = out;
 	}
 
 	public NotifyChangeToLib getNotifier() {
 		return notifier;
 	}
 
 	public void setNotifier(NotifyChangeToLib notifier) {
 		this.notifier = notifier;
 	}
 
 	public int getClientID() {
 		return clientID;
 	}
 
 	public void setClientID(int clientID) {
 		this.clientID = clientID;
 	}
 
 	public Vector<ClientThread> getClients() {
 		return clients;
 	}
 
 	public void setClients(Vector<ClientThread> clients) {
 		this.clients = clients;
 	}
 
 	public Library getLib() {
 		return lib;
 	}
 
 	public void setLib(Library lib) {
 		this.lib = lib;
 	}
 
 	public DataInputStream getIn() {
 		return in;
 	}
 
 	public void setIn(DataInputStream in) {
 		this.in = in;
 	}
 
 	public void run() {
 		byte[] bytesRecieved = new byte[1024];
 		while (!this.socket.isClosed()) {
 			try {
 
 				int size = in.read(bytesRecieved);
 				String command = new String(bytesRecieved, 0, size);
 				if (command.equalsIgnoreCase("add")) {
					
 					System.out.println("Download Start!");
 					size = in.read(bytesRecieved);
 					String song = new String(bytesRecieved,0,size);
 					String[] splitsong = song.split("\\Q$");
					ServerSocket fileAddServer = new ServerSocket((port + 1));
 					FileRecieveThreadServer fileAddThread = new FileRecieveThreadServer(
 							fileAddServer.accept(), splitsong[0], splitsong[1], splitsong[2], this);
 					fileAddThread.start();
 					fileAddServer.close();
 				} else if (command.equalsIgnoreCase("remove")) {
 					size = in.read(bytesRecieved);
 					String title = new String(bytesRecieved, 0, size);
 					this.lib.removeSong(title);
 					System.out.println("Removed " + title);
 					changeNotify();
 
 				} else if (command.equalsIgnoreCase("play")) {
 					size = in.read(bytesRecieved);
 					String title = new String(bytesRecieved, 0, size);
 					String filename = lib.findSong(title).getFile();
 					ServerSocket fileSendServer = new ServerSocket((port + 2));
 					FileSendThreadServer fileSendThread = new FileSendThreadServer(
 							fileSendServer.accept());
 					fileSendThread.setFileName(filename);
 					fileSendThread.start();
 					fileSendServer.close();
 				} else if (command.equalsIgnoreCase("save")) {
 					lib.save(System.getProperty("user.dir") + "/Library/"
 							+ "serverLib.xml");
 				} else if (command.equalsIgnoreCase("restore")) {
 					lib.restore(System.getProperty("user.dir") + "/Library/"
 							+ "serverLib.xml");
 					sendSongs();
 				} else if (command.equalsIgnoreCase("exit")) {
 					this.socket.close();
 					lib.save(System.getProperty("user.dir") + "/Library/"
 							+ "serverLib.xml");
 					System.out.println("Client has Disconnected with exit.");
 					System.out.println("Thread Finished");
 				} else if (command.equalsIgnoreCase("getSongs")) {
 					sendSongs();
 				} else if (command.equalsIgnoreCase("aSong")) {
 					size = in.read(bytesRecieved);
 					String song = new String(bytesRecieved, 0, size);
 					sendSong(song);
 				}
 			} catch (IOException | InterruptedException e) {
 				try {
 					this.socket.close();
 					System.out.println("Thread Dead");
 				} catch (IOException e1) {
 					e1.printStackTrace();
 				}
 			}
 		}
 		this.notifier.setConnected(false);
 	}
 
 	public void changeNotify() {
 		for (ClientThread client : clients) {
 			client.getNotifier().setRefreshFlag(true);
 		}
 	}
 
 	@SuppressWarnings("unused")
 	@Deprecated
 	private void sendLibrary() {
 		try {
 			ServerSocket tempServer = new ServerSocket((port + 4));
 			Socket tempSocket = tempServer.accept();
 			tempServer.close();
 			DataOutputStream outPut = new DataOutputStream(
 					tempSocket.getOutputStream());
 			File file = new File(System.getProperty("user.dir") + "/Library/"
 					+ "serverLib.xml");
 			System.out.print("Keep Going....");
 			FileInputStream fileStream = new FileInputStream(file);
 			byte[] buffer = new byte[1024];
 			int size = fileStream.read(buffer);
 			while (size > 0) {
 				outPut.write(buffer, 0, size);
 				size = fileStream.read(buffer);
 			}
 			outPut.close();
 			fileStream.close();
 			System.out.print("Done....");
 			tempSocket.close();
 		} catch (IOException e2) {
 			e2.printStackTrace();
 		}
 
 	}
 
 	private void sendSongs() throws IOException, InterruptedException {
 		ServerSocket tempServer = new ServerSocket((port + 100 + clientID));
 		Socket tempSocket = tempServer.accept();
 		tempServer.close();
 		DataOutputStream outPut = new DataOutputStream(
 				tempSocket.getOutputStream());
 		for (String song : lib.getAllSongs()) {
 			outPut.write(song.getBytes());
 		}
 		tempSocket.close();
 	}
 
 	private void sendSong(String title) throws IOException,
 			InterruptedException {
 		ServerSocket tempServer = new ServerSocket((port + 10 + clientID));
 		Socket tempSocket = tempServer.accept();
 		tempServer.close();
 		DataOutputStream outPut = new DataOutputStream(
 				tempSocket.getOutputStream());
 		outPut.write(lib.findSong(title).toString().getBytes());
 		tempSocket.close();
 	}
 }
