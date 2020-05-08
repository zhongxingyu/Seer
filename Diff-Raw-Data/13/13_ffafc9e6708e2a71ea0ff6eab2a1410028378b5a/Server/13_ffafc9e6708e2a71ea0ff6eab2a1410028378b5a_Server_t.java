 package server;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.Scanner;
 
 public class Server implements Runnable {
 
 	private int port;
 
 	public Server() {
 		this(4860);
 	}
 
 	public Server(int port) {
 		this.port = port;
 	}
 
 	@Override
 	public void run() {
		ServerSocket serverSock = null;
		try {
			serverSock = new ServerSocket(port);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
 		System.out.println("Server is now listening on port " + port);
 		while (true) {
 			Socket connection = null;

 			try {
 				connection = serverSock.accept();
 			} catch (IOException e) {
 				e.printStackTrace();
 				break;
 			}
 			try {
 				handleConnection(connection);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void handleConnection(Socket s) throws IOException {
 		Scanner clientReader = new Scanner(s.getInputStream());
 		PrintStream clientWriter = new PrintStream(s.getOutputStream());
 
 		String requestedDir = clientReader.nextLine();
 		System.out.println("Requested directory: " + requestedDir);
 		File dir = new File(requestedDir);
 		if (!dir.isDirectory())
 			throw new IllegalArgumentException("The requested directory needs to be a directory.");
 		String listOfDirs = combineArr(dir.listFiles(new FileFilter() {
 			@Override
 			public boolean accept(File pathname) {
 				return !pathname.isDirectory();
 			}
 		}), ' ');
 		clientWriter.println(listOfDirs);
 		clientWriter.flush();
 
 		String getReq = clientReader.nextLine();
 		System.err.println("File requested: " + getReq);
 		String[] twoHalves = getReq.split(" ");
 		if (twoHalves.length != 2)
 			throw new IllegalArgumentException("The command needs to be in the form: GET FileName");
 		if (!twoHalves[0].equalsIgnoreCase("GET"))
 			throw new IllegalArgumentException("The command needs to be in the form: GET FileName");
 		String requestedFile = requestedDir + twoHalves[1];
 		File fileToWrite = new File(requestedFile);
 		Scanner fileReader = new Scanner(fileToWrite);
 		String entireFile = fileReader.useDelimiter("\\Z").next();
 		clientWriter.println(entireFile);
 		clientWriter.flush();
 
 		while (true) {
 			String quitReq = clientReader.nextLine();
 			if (quitReq.equalsIgnoreCase("QUIT")) {
 				s.close();
 				break;
 			} else
 				continue;
 		}
		s.close();
 	}
 
 	private static String combineArr(File[] arr, char separator) {
 		StringBuilder ret = new StringBuilder();
 		for (File s : arr) {
 			if (s.getName().contains(" "))
 				ret.append("\"" + s.getName() + "\"").append(separator);
 			else
 				ret.append(s.getName()).append(separator);
 		}
 		return ret.substring(0, ret.length() - 1);
 	}
 
 	public static void main(String[] args) {
 		Server s = new Server();
 		s.run();
 	}
 }
