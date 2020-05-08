 import java.io.*;
 import java.net.*;
 
 public class Client {
 
 	int portNumber;
 	Socket theSocket;
 	PrintWriter out;
  	BufferedReader in;
 	String inMsg;
 
 	public Client(int aPort) {
 		portNumber = aPort;
 	}
 
 	public void run() {
 		try {
 			theSocket = new Socket("localhost", portNumber);
 			out = new PrintWriter(theSocket.getOutputStream(), true);
			//out.flush();
 			in = new BufferedReader(
 					new InputStreamReader(theSocket.getInputStream()));
 			try {
				//sendMessage("This is a test from Java");
				//sendMessage("put(foo,bar,public)");
				sendMessage("get(foo)");
 				say("message sent");
 				inMsg = in.readLine();
 				say("got: "+inMsg);
 				sendMessage("Second test with rubish");
 				say("message sent");
 				inMsg = in.readLine();
 				say("got: "+inMsg);
 				for (int i = 0; i < 3; i++) {
 					say("Testing put message");
 					sendMessage("put(foo,"+i+",public)");
 					say("got: "+in.readLine());
 				}
 				for (int i = 0; i < 3; i++) {
 					say("Testing get message");
 					sendMessage("get("+i+")");
 					say("got: "+in.readLine());
 				}
 			}
 			catch (IOException e) {
 				System.err.println("read failed");
 			}
 		}
 		catch (IOException e) {
 			System.err.println("creation of socket failed");
 		}
 		finally{
 			try {
 				in.close();
 				out.close();
 				theSocket.close();
 				System.exit(0);
 			}
 			catch (IOException e) {
 				System.err.println("closing socket failed");
 			}
 		}
 	}
 
 	private void say(String aText) {
 		System.out.println(aText);
 	}
 
 	private void sendMessage(String aMsg) {
 		out.println(aMsg);
		//out.flush();
 	}
 
 	public static void main(String[] args) {
 		System.out.println("going to connect to the socket server");
 		Client aClient = new Client(Integer.parseInt(args[0]));
 		aClient.run();
 	}
 }
