 
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 
 public class Client2 {
 	protected static DatagramSocket sock;
 	protected static InetAddress addr;
 	protected static int port;
 	protected static String port2;
 	private static String clientId;
 
 	protected Client2() {
 	}
 
 	public static void main(String[] args) throws IOException {
 		if (args.length != 2) {
 			System.err.println("Usage: <Server Address> <Port>");
 			System.exit(1);
 		}
 		try {
 			addr = InetAddress.getByName(args[0]);
 			port = Integer.parseInt(args[1]);
 		} catch (UnknownHostException e) {
 			System.err.println("Invalid hostname.");
 			e.printStackTrace();
 		}
 
 		/* Create socket; connect to given server */
 		sock = new DatagramSocket();
 		sock.connect(addr, port);
 
 		/* Greet server */
 		byte[] g = "GREETING2".getBytes();
 		DatagramPacket greetingPacket = new DatagramPacket(g, g.length, addr, port);
 		sock.send(greetingPacket);
 
 		/* get client id from server */
 		byte[] data = new byte[1024];
 		DatagramPacket pack = new DatagramPacket(data, data.length);
 		sock.receive(pack);
 		String msg = new String(pack.getData());
 		clientId = msg;
 		System.out.println("Received Client ID: " + clientId);
 
 		/* get port from server */
 		sock.receive(pack);
		port2 = new String(pack.getData());
 		System.out.println("Recv Port: " + port2);
 
 		String s = null;
 		try {
 			Process p = Runtime.getRuntime().exec("/usr/bin/ssh -R " + port2 + ":localhost:22 jumpbug.ccs.neu.edu");
 			System.out.print("Reverse SSH running" );
 			BufferedReader stdInput = new BufferedReader(new 
 					InputStreamReader(p.getInputStream()));
 
 			BufferedReader stdError = new BufferedReader(new 
 					InputStreamReader(p.getErrorStream()));
 
 			// read the output from the command
 			System.out.println("Here is the standard output of the command:\n");
 			while ((s = stdInput.readLine()) != null) {
 				System.out.println(s);
 			}
 
 			// read any errors from the attempted command
 			System.out.println("Here is the standard error of the command (if any):\n");
 			while ((s = stdError.readLine()) != null) {
 				System.out.println(s);
 			}
 		} catch (Exception e) {
 			System.err.println("(22) Could not send packet.");
 			e.printStackTrace();
 		}
 
 		/* Create thread */
 		Client2 client = new Client2();
 		Thread listenThread = new Thread(client.new ClientListener());
 		listenThread.start();
 		client.heartbeat();
 
 	}
 
 	protected void heartbeat() throws IOException {
 		while (true) {
 			String hb;
 			try {
 				hb = "HEARTBEAT" + clientId;
 				byte[] hbmsg = hb.getBytes();
 				DatagramPacket messagePacket = new DatagramPacket(hbmsg,
 						hbmsg.length, addr, port);
 				sock.send(messagePacket);
 				Thread.currentThread().sleep(10000); /* sleep for 10 sec. */
 				System.out.println("Sent hearbeat\n");
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	protected class ClientListener implements Runnable {
 		protected ClientListener() {
 		}
 
 		@Override
 		public void run() {
 			byte[] data = new byte[1024];
 			DatagramPacket pack = new DatagramPacket(data, data.length);
 			try {
 				while (true) {
 					sock.receive(pack);
 					String msg = new String(data);
 					/* TODO: Parse message; open connection*/
 					if (msg.startsWith("RECONNECT")) {
 						System.out.println(msg);
 						Process p = Runtime.getRuntime().exec("ssh -R " + port2 + ":localhost:22 dataz@login.ccs.neu.edu");
 					}
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 }
