 package server;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.net.Socket;
 import java.util.NoSuchElementException;
 import java.util.Scanner;
 
 public class SingleServer implements Runnable {
 	private InputStream in;
 	private PrintStream out;
 	private Socket sock;
 	public SingleServer(Socket s) throws IOException {
 		in=(sock=s).getInputStream();
 		out=new PrintStream(sock.getOutputStream());
 	}
 	public SingleServer(Socket s, long tim) throws IOException {
 		this(s);
 	}
 	public void run() {
 		Scanner s=new Scanner(in);
 		String k="";
 		try {
 			handleRequest(k=s.nextLine());
 			sock.close();
 		} catch (NoSuchElementException e) {
			System.out.println(e.getMessage()+" (are we getting spammed with requests?) "+k);
 		} catch (IOException e) {
 			//run around with out hair on fire
 			throw new Error(e);
 		}
 
 	}
 	private void handleRequest(String line) throws IOException {
 		Scanner sc = new Scanner(line);
 		//get the HTTP method of this request (we handle GET and POST)
 		String httpMethod = sc.next();
 		//the rest of the line contains the path and then the HTTP/x.y version-string
 		String path = sc.nextLine();
 		//remove the aforementioned version-string (and any whitespace)
 		path = path.substring(0, path.lastIndexOf(" HTTP/")).trim();
 		DynamicGuts.processRequest(httpMethod, path, out);
 	}
 }
