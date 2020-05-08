 package net.contentcube.robot.nxt.commands;
 
 import java.io.IOException;
 import java.io.OutputStream;
 
 import android.util.Log;
 
 import net.contentcube.robot.nxt.NXTCommand;
 
 public class PingCommand implements NXTCommand
 {
 	@Override
 	public void run(OutputStream stream)
 	{
		byte[] buffer = Factory.ping();
 		
 		try
 		{
			stream.write(buffer);
 			stream.flush();
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 		
 		Log.e("NXT", "Ping");
 	}
 
 	@Override
 	public void setOnCompleteListener(OnCompleteListener listener) {}
 }
