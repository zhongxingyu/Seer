 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.SocketException;
 import java.util.List;
 import java.util.regex.Pattern;
 
 
 public class Server {
 	
 	private Integer port;
 	private Boolean alive;
 	private Database database;
 	
 	public Server(Integer port, String databasePath) {
 		this.port = port;
 		this.alive = true;
 		if (databasePath == null){
 			databasePath = "datafile";
 		}
 		database = new Database(databasePath);
 		try {
 			DatagramSocket socket = new DatagramSocket(port);
 			while(alive){
 				//read
 				byte[] buf = new byte[256];
 				DatagramPacket packet = new DatagramPacket(buf, buf.length);
 				socket.receive(packet);
 				String command = new String(packet.getData(), 0, packet.getLength());
 				
 				//process
 				String result = processCommand(command);
 				
 				//write
 				buf = result.getBytes();
 				packet = new DatagramPacket(buf, buf.length, packet.getAddress(), packet.getPort());
 				socket.send(packet);
 			}
 			socket.close();
 		} catch (SocketException e) {
 			System.out.println("Error opening a socket on port " + port);
 			e.printStackTrace();
 		} catch (IOException e) {
 			System.out.println("Error reading form socket");
 			e.printStackTrace();
 		}
 	}
 
 	private String processCommand(String command) {
 		String[] parts = command.split("\\s");
 		if ("Kill".equals(parts[0].trim())){
 			alive = false;
 			return database.save() + "\nServer dying"; 
		} else if (parts[0].trim().equals("Insert")){
 			if (parts.length != 4)
 				return "ERROR: Wrong number of parameters";
 			return database.insert(parts[1], parts[2], new Integer(parts[3]));
		} else if (parts[0].trim().equals("Find")){
 			if (parts.length != 3)
 				return "ERROR: Wrong number of parameters";
 			List<Record> records = database.retrieve(parts[1], parts[2], null);
 			StringBuffer buffer = new StringBuffer(records.size());
 			for (Record record : records){
 				buffer.append("\n").append(record.toString());
 			}
 			return buffer.toString();
		} else if (parts[0].trim().equals("Delete")){
 			if (parts.length < 2 || parts.length > 4)
 				return "ERROR: Wrong number of parameters";
 			String name = parts[1];
 			String ipAddress = parts.length < 3 ? null : parts[2];
 			Integer port = parts.length < 4 ? null : new Integer(parts[3]);
 			return database.delete(name, ipAddress, port);
 		} else{
 			return "ERROR: Unknown command";
 		}
 	}
 	
 }
