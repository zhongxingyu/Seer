 package CS247;
 
 import java.io.*;
 import java.sql.ResultSet;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class AndroidClient extends Thread {
 	private final Socket socket;
 	private final AndroidServer owner;
 	private final DataOutputStream out;
 	private final DataInputStream in;
 	private final Logger logger;
 	private final String addr;
 	private final Database database;
 	public boolean run;
 	
 	public AndroidClient(Socket socket, Database database, AndroidServer owner) {
 		super("AndroidClient: "+socket.getInetAddress().getHostAddress());
 		this.database = database;
 		addr = socket.getInetAddress().getHostAddress();
 		this.socket = socket;
 		try
 		{
 			out = new DataOutputStream(socket.getOutputStream());
 			in = new DataInputStream(socket.getInputStream());
 		}
 		catch (IOException e)
 		{throw new RuntimeException("Android client ("+addr+") could not get socket stream.");}
 		logger = Logger.getLogger("global");
 		this.owner = owner;
 		run = true;
 	}
 	
 	public void run() {
 		while(socket.isConnected()){
 			try
 			{
 				switch(in.read())
 				{
 				case 0://register
 					out.write(database.removeRegistrationID(in.readUTF()) ? 1 : 0);
 					break;
 				case 1://deregister
 					out.write(database.removeRegistrationID(in.readUTF()) ? 1 : 0);
 					break;
 				case 2://push
 					int alert_id = in.readInt();
 					String[] results = database.getAlertByIDAsArray(alert_id);
 					if(results == null){
 						out.writeInt(0);
 					} else {
 						out.writeInt(results.length);
 						for(int i = 0; i < results.length; i++) {
 							out.writeUTF(results[i]);
 						}
 					}
 					break;
 				case 3: // pull
 					try {
 						String timestamp = in.readUTF();
 						ResultSet rs = database.getAllAlertsSinceXAsResultSetObject(timestamp);
 						int i = 0;
 						while(rs.next()){
 							++i;
 						}
 						rs.close();
 						out.writeInt(i);
 						rs = database.getAllAlertsSinceXAsResultSetObject(timestamp);
 						while(rs.next()){
 							out.writeUTF(rs.getString("title"));
 							out.writeUTF(rs.getString("link"));
 							out.writeUTF(rs.getString("description"));
 							out.writeUTF(rs.getString("suggestions"));
							out.writeUTF(rs.getString("time_stamp"));
 						}
 						rs.close();
 					} catch(Exception e){
 						throw new IOException(e);
 					}
 					break;
 				}
 				try {
 					Thread.sleep(50);
 				} catch(Exception e){
 					throw new IOException(e);
 				}
 			} catch (IOException e){
 				logger.log(Level.INFO, "Android client ("+addr+") dropped due to IOException.");
 			}
 		}
 	}
 	
 	private String readRegistrationID() throws IOException
 	{
 		StringBuilder id = new StringBuilder();
 		int c = 0;
 
 		while((c = in.read()) > 0)
 		{id.append((char)c);}
 		
 		return id.toString();
 	}
 
 }
