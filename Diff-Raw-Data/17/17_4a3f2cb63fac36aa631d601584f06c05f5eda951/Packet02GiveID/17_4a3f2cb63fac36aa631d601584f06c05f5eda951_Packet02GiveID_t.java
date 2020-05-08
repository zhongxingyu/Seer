 package multiplayer.packet;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 
 import monopoly.Game;
 
 public class Packet02GiveID extends Packet
 {
 
 	public byte id;
 	
 	public Packet02GiveID()
 	{
 	}
 	
 	@Override
 	public void readData(DataInputStream d) throws IOException
 	{
 		id = d.readByte();
 	}
 
 	@Override
 	public void writeData(DataOutputStream d) throws IOException
 	{
 		d.writeByte(id);
 	}
 
 	@Override
 	public void handle()
 	{
 		Game.Log("Recieved ID: " + id);
		Game.Monopoly().setMyID(id);
 	}
 	
 	public int size()
 	{
 		return 1;
 	}
 
 }
