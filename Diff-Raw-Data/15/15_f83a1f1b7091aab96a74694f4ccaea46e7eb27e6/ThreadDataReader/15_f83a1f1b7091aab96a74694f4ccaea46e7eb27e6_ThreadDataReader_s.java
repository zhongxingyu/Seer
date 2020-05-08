 package multiplayer;
 
 import java.io.DataInputStream;
 import java.io.InputStream;
 
 import monopoly.Game;
 import multiplayer.packet.Packet;
 
 public class ThreadDataReader extends Thread
 {
 
 	public ThreadDataReader(InputStream socketIn)
 	{
 		setDaemon(true);
 		inStream = new DataInputStream(socketIn);
 	}
 
 	public void run()
 	{
 		while(true)
 		{
 			try
 			{
 				if (inStream.available() > 1) //Atleast the ID is there
 				{
					Game.Log("Reading a packet! (" + inStream.available() + ")");
 					Packet p = Packet.readPacket(inStream);
					Game.Log("Read packet " + p.getPacketID());
 					p.handle();
 				}
 				sleep(1);
 			} catch (Exception e)
 			{
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	private DataInputStream inStream;
 }
