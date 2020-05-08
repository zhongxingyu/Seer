 import java.io.*;
 import java.net.*;
 import java.util.ArrayList;
 
 public class ServerConnection implements Runnable {
 	
 	private Socket connection;
 
 	ServerConnection(Socket s){
 		this.connection = s;
 	}
 	
 	public static String[] authenticate() {
 		String host = "localhost";
 		int port = 8149;
 		
 		StringBuffer instr = new StringBuffer();
 		StringBuffer instr1 = new StringBuffer();
 		String instrBuffer[] = null;
 		
 		try {
 			// gets server address and attempts to establish a connection
 			InetAddress address = InetAddress.getByName(host);
 			Socket connection = new Socket(address, port);
 			BufferedOutputStream bos = new BufferedOutputStream(connection.getOutputStream());
 			OutputStreamWriter osw = new OutputStreamWriter(bos, "US-ASCII");
 		
 			// tell server we want to auth
 			osw.write("auth" + (char) 13);
 			osw.flush();
 			
 			BufferedInputStream bis = new BufferedInputStream(connection.getInputStream());
 			InputStreamReader isr = new InputStreamReader(bis, "US-ASCII");
 			int c;
 			while((c = isr.read()) != 13)
 				instr.append((char) c);
 			
 			if (Integer.parseInt(instr.toString()) == 1) {
 				System.out.println("Authenticated successfully!");
 				
 				// TODO: this needs to come from authentication part however NYI in client
 				System.out.println("Sending userid now...");
 				//Random rand = new Random();
 				//int Id = rand.nextInt(6-1+1)+1;
				osw.write("4" + (char) 13);
 				osw.flush();
 				
 				while ((c = isr.read()) != 13) {
 					instr1.append((char) c);
 				}
 				if (instr1.toString().contentEquals("create")) {
 					System.out.println("Please create an account first!");
 				}
 				else { // we have character info
 					System.out.println("Success!");
 					instrBuffer = instr1.toString().split(",");
 					System.out.println("Your character info is as follows:");
 					System.out.println("ID = " + instrBuffer[0]);
 					System.out.println("Name = " + instrBuffer[1]);
 					System.out.println("X-coord = " + instrBuffer[2]);
 					System.out.println("Y-coord = " + instrBuffer[3]);
 					Player.setId(instrBuffer[0]);
 				}
 			}
 			else {
 				System.out.println("not authenticated");
 				return null;
 			}
 			
 			Runnable runnable = new ServerConnection(connection);
 			Thread thread = new Thread(runnable);
 			thread.start();
 			return instrBuffer;
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 	/*
 	private static void receiveOnlinePlayers() {
 		//ObjectInputStream inputStream = new ObjectInputStream();
 		//outputStream.(Player.onlinePlayers);
 	}
 	*/
 	@SuppressWarnings("unchecked")
 	public void run () {
 		try {
			int id;
 			BufferedInputStream bos = new BufferedInputStream(connection.getInputStream());
 			ObjectInputStream inputStream = new ObjectInputStream(bos);
			BufferedInputStream bis = new BufferedInputStream(connection.getInputStream());
			InputStreamReader isr = new InputStreamReader(bis, "US-ASCII");
 			while (true) {
 				if (Main.exitRequest) {
 					connection.shutdownInput();
 					return;
 				}
				
				// read position of player in list here
				while((id = isr.read()) != 13);
				Player.listPosition = id;
				// read all players and their positions
 				Player.onlinePlayers = (ArrayList<Player>) inputStream.readObject();
 				Thread.sleep(3000);
 			}
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 		finally {
 			try {
 				connection.close();
 			}
 			catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 }
