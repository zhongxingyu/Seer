 package network;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.Socket;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import enums.NetworkInputType;
 
 public class ClientOnServer extends Thread {
 
 	private int playernumber;
 	private Socket socket;
 	private BufferedReader inStream;
 	private DataOutputStream outStream;
 	private CopyOnWriteArrayList<Input> queue;
 
 	public ClientOnServer(Socket socket, int playernumber, CopyOnWriteArrayList<Input> queue) throws IOException {
 		this.socket = socket;
 		this.playernumber = playernumber;
 		this.inStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
 		this.outStream = new DataOutputStream(socket.getOutputStream());
 		this.queue = queue;
 	}
 
 	public void sendMap(String map) throws IOException {
 		this.outStream.write(("me:" + this.playernumber + ";\n").getBytes());
		this.outStream.write(("m:" + map + ";\n").getBytes());
 	}
 
 	public void sendInput(Input in) throws IOException {
 		this.outStream.write(("input:" + in.playerID + "," + in.type + "," + in.x + "," + in.y + ";\n").getBytes());
 	}
 
 	@Override
 	public void run() {
 		while (true) {
 			try {
 				String command = this.inStream.readLine();
 				Input in = null;
 				if (command.startsWith("input")) {
 					in = new Input();
 					command = command.replace("input:", "").replace(";", "");
 					String[] parts = command.split(",");
 					in.playerID = Integer.valueOf(parts[0]);
 					in.type = NetworkInputType.valueOf(parts[1]);
 					if ((in.type == NetworkInputType.PLAYER) || (in.type == NetworkInputType.BOMB)) {
 						in.x = Integer.valueOf(parts[2]);
 						in.y = Integer.valueOf(parts[3]);
 					}
 				}
 
 				if (in != null) {
 					this.queue.add(in);
 				}
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				// e.printStackTrace();
 			}
 		}
 	}
 }
