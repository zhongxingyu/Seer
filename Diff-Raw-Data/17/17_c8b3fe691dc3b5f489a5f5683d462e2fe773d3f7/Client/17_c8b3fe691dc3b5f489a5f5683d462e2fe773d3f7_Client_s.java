 import java.io.*;
 import java.net.Socket;
 import java.util.*;

 public class Client{
 
	public static void main(String args[]) throws IOException {
 		System.out.println("what is the server ip address");
 		Scanner in = new Scanner(System.in);
 		final String host = in.nextLine();
 		//final String host = "localhost";
 		final int portNumber = 8123;
		System.out.println("Creating socket to '" + host + "' on port " + portNumber);
 
 		while (true) {
 			Socket socket = new Socket(host, portNumber);
 			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
 			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
 
 			System.out.println("server says:" + br.readLine());
 
 			BufferedReader userInputBR = new BufferedReader(new InputStreamReader(System.in));
 			String userInput = userInputBR.readLine();
 
 			out.println(userInput);
 
 			System.out.println("server says:" + br.readLine());
 
 			if ("exit".equalsIgnoreCase(userInput)) {
 				socket.close();
 				break;
			}
 		}
 	}
 }
