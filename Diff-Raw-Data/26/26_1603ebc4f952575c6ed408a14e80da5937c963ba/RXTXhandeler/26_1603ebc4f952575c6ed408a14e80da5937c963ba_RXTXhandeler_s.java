 package IOStream;
 
 import java.io.IOException;
 import java.net.Socket;
 import java.util.ArrayList;
 
 import GUI.RecieveWindow;
 import Parcers.RockSimParcer;
 
 public class RXTXhandeler 
 {
 	private SendStreamOut sendStreamOut;
 	private GetStreamIn getStreamIn;
 	private String streamInString;
 	private Socket socket;
 	private String deviceName;
 	private int available;
 	private long lastReadTime = System.currentTimeMillis();
 	private long start = System.currentTimeMillis();
 	private RecieveWindow reciveWindow;
 	public ArrayList<String> gpsData;
 	public int gpsDataLength;
 	public int gpsDataIndex = 1;
 	
 	
 	public RXTXhandeler(SendStreamOut sendStreamOut,GetStreamIn getStreamIn,Socket socket, String deviceName)
 	{
 		this.sendStreamOut = sendStreamOut;
 		this.getStreamIn = getStreamIn;
 		this.socket = socket;
 		this.deviceName = deviceName;
 		reciveWindow = new RecieveWindow();
 	}
 	
 	public void NameTimeRXTX()
 	{
 		try 
 		{
 			available = socket.getInputStream().available();
 		} 
 		catch (IOException e) 
 		{
 			e.printStackTrace();
 		}
 		
 		if(available > 0)
 		{
 			  streamInString = getStreamIn.StreamIn(socket);
 			  
 			  if(!streamInString.equals("#"))
 			  {
 				  reciveWindow.updateText(streamInString);
 			  }
 			  else
 			  {
 				  sendStreamOut.streamOut("Pong");
 			  }
 		}
		if((System.currentTimeMillis() - lastReadTime) > 2000)
 		{
			String tempString = deviceName + ": " + Long.toString((System.currentTimeMillis()-start)/1000);
 			sendStreamOut.streamOut(tempString);
 			lastReadTime = System.currentTimeMillis();
 		}
 	}
 	public void getRockSimData()
 	{
 		RockSimParcer rockSimParcer = new RockSimParcer();
 		gpsData = rockSimParcer.parcer();
 		gpsDataLength = gpsData.size()-1;
 	}
 	public void RockSimRXTX()
 	{
 		try 
 		{
 			available = socket.getInputStream().available();
 		} 
 		catch (IOException e) 
 		{
 			e.printStackTrace();
 		}
 		
 		if(available > 0)
 		{
 			  streamInString = getStreamIn.StreamIn(socket);
 			  
 			  if(!streamInString.equals("#"))
 			  {
 				  reciveWindow.updateText(streamInString);
 			  }
 			  else
 			  {
 				  sendStreamOut.streamOut("Pong");
 			  }
 		}
 		if((System.currentTimeMillis() - lastReadTime) > 1000)
 		{
 			if(gpsDataIndex < gpsDataLength )
 			{
				String tempString = gpsData.get(gpsDataIndex)+ "\r\n";
 				sendStreamOut.streamOut(tempString);
 				lastReadTime = System.currentTimeMillis();
 				gpsDataIndex++;
 			}
 			else
 			{
 				gpsDataIndex = 1;
 			}
 		}
 	}
 }
