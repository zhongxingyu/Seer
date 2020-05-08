 package net.volus.ronwalf.phs2010.networking.echocanyon.threaded;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 public class ThreadedEchoServer implements Runnable {

	
 	
 	public static void main(String args[]) throws IOException {
 		ServerSocket acceptor = new ServerSocket(new Integer(args[0]));
 		
 		while (true) {
 			Socket client = acceptor.accept();
 			new Thread(new ThreadedEchoServer(client)).start();
 		}
 		
 	}
 	
 	private Socket socket;
 	
 	public ThreadedEchoServer(Socket client) {
 		socket = client;
 	}
 
 	public void run() {
 		try {
 			readLines();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				socket.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public void readLines() throws IOException {
 		BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()) );
 		Writer writer = new OutputStreamWriter( socket.getOutputStream() );
 		while (true) {
 			String line = reader.readLine();
 			writer.write("Msg #: " + line + "\r\n");
 		}
 	}
 }
