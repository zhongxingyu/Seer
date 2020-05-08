 package main;
 
 import java.sql.ResultSet;
 
 import org.pircbotx.PircBotX;
 
 import Listeners.*;
 
 public class Main
 {
 	public static PircBotX bot;
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) throws Exception
 	{
 		Globals.nickservPW = args[1];
 		new Thread(new Runnable()
 		{
 			public void run()
 			{
 				try
 				{
 					Thread.sleep(25 * 1000);
 				} catch (InterruptedException e) { }
 				Globals.verbose = true;
 			}
 		}).start();
 		
 		bot = new PircBotX();
 		bot.setVerbose(true);
 		bot.setName("Neko-chan");
 		bot.setLogin("Neko-chan");
 		bot.setVersion("Neko-chan v1.0 made by " + Globals.BOTMASTER + " GIT: https://github.com/C0Rt3X1337/Neko-chan");
 		bot.setMessageDelay(500);
 		
 		DB.init(args[0]);
 		
 		bot.getListenerManager().addListener(new FishListener());
 		bot.getListenerManager().addListener(new GeneralListener());
 		bot.getListenerManager().addListener(new GreetListener());
 		bot.getListenerManager().addListener(new HelpListener());
 		bot.getListenerManager().addListener(new PMAdminListener());
 		bot.getListenerManager().addListener(new TriggerListener());
 		
        bot.connect("127.0.0.1", 1337, args[2]);
         
         ResultSet result = DB.get("SELECT * FROM Channels");
         while (result.next()) bot.joinChannel(result.getString("Channel"));
 	}
 
 }
