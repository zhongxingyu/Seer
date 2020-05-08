 import java.io.*;
 import java.net.*;
 import static java.lang.System.*;
 
 public class Server_App {
 	public static void main(String[] args) throws IOException
 	{
 		while(true){
			System.out.println("Watinting...");
 			ServerSocket ss = new ServerSocket(1988);
 			//making socket for server with port number 1988 which is my birth year kk sorry to the young
 			
 			Socket sock = ss.accept();
 			//wait until completion of making connection with client
 			System.out.println("Server has connected "+sock.getInetAddress()+
 					"to the client with port number "+sock.getLocalPort());
 			
 			BufferedReader br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
 			//buffered reader which gets messages from socket
 			
 			PrintWriter pw = new PrintWriter(sock.getOutputStream(), true);
 			//print writer which sends messages via socket
 			
 			
 			String reader = br.readLine();
 			System.out.println("Rcvd : "+reader);
 			//get actual messages from buffered reader
 			//and print that
 			
 			pw.println(reader);
 			System.out.println("Sent : "+reader);
 			//send the message from the client to test
 			
 			pw.close();
 			br.close();
 			sock.close();
 			ss.close();
 			//all should be closed after working
 	
 		}	
 
 	}
 }
