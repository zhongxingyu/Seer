 package server;
 
 import java.io.IOException;
 import java.net.Socket;
 
 import server.game.PlayerMP;
 
 public class ThreadPlayerAccept extends Thread
 {
 
 	public ThreadPlayerAccept()
 	{
 		super("Player Accepter");
 		setDaemon(true);
		run();
 	}
 	
 	public void run()
 	{
 		int nextId = 0;
 		while(nextId < 6)
 		{
 			try
 			{
 				Socket s = MMP.getServer().getSocket().accept();
 				MMP.getServer().addPlayer(s);
 				++nextId;
 			} catch (IOException e)
 			{
 			}
 		}
 	}
 }
