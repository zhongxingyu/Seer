 package de.fu.nbi.gr13;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 public class Webserver {
 
 	int port = 0;
 	String workspace = "";
 
 	public Webserver(int port, String workspace) {
 		this.port = port;
 		this.workspace = workspace;
 	}
 
 	public void start() {
 		try {
 			ServerSocket serverSocket = new ServerSocket(port);
 			while (true) {
 				Socket connection = null;
 				try {
 					connection = serverSocket.accept();
 					BufferedReader br = new BufferedReader(
 							new InputStreamReader(connection.getInputStream()));
 
 					String httpString = br.readLine();
 					System.out.println(httpString);
 					String path = "";
					if ((httpString.split(" ")[1]).compareTo("/") == 0) path = workspace + "/index.html";
 					else {
 						path = workspace + "/" + (httpString.split(" ")[1]);
 					}
 					
 
 					printPage(connection, path);
 
 					connection.close();
 				} catch (FileNotFoundException e) {
 					printPage(connection, workspace + "/error/404.html");
 					System.out.println(e.getMessage());
 				} catch (Exception e) {
 					System.out.println(e.getMessage());
 				}
 			}
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 		}
 	}
 
 	private void printPage(Socket connection, String path) throws IOException {
 		PrintWriter pw = new PrintWriter(connection.getOutputStream());
 		BufferedReader br_file = new BufferedReader(new FileReader(path));
 
 		String currZeile = "";
 		while ((currZeile = br_file.readLine()) != null)
 			pw.print(currZeile);
 
 		br_file.close();
 		pw.flush();
 	}
 }
