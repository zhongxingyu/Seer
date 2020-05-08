 import org.jibble.pircbot.*;
 import java.io.*;
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Scanner;
 import java.net.*;
 import java.util.Random;
 public class baseBot extends PircBot {
 	private String owner = null;
 	private String botName = null;
 	private String ircNet = null;
 	private ArrayList<String> channels = new ArrayList<String>();
 	private Module m = null;
 	public baseBot(){
 		try{
 
 			File settings = new File("settings.txt");
 			if(!settings.exists())
 			{
 				firstRun();
 			}
 
 			FileReader fr = new FileReader(settings);
 			BufferedReader br = new BufferedReader(fr);
 
 			owner = br.readLine();
 			botName = br.readLine();
 			ircNet = br.readLine();
 			String channel = br.readLine();
 			while(channel != null) {
 				channels.add(channel);
 				log(channel, "", "====Bot restarted====");
 				channel = br.readLine();
 			}
 		}
 		catch(Exception e){System.out.println(e.getMessage());}
 			
 
 		setName(botName);
 		m = new Module(owner, botName);
 		log("","","=====Bot started=====");
 	}
 	
 	public static void main(String[] args){
 		baseBot bot = new baseBot();
 		bot.setVerbose(true);
 		try{
 			bot.connect(bot.ircNet);
 		}
 		catch (Exception e){
 			System.out.println("Can't connect: "+e);
 		}
 		bot.setVerbose(false);
 		for(String c : bot.channels) {
 			bot.joinChannel(c);
 		}
 	}
 
 	public void firstRun()
 	{
 		Scanner scan = new Scanner(System.in);
 		System.out.println("It appears that you haven't set up your bots settings. Please do that now.");
 		System.out.println("First, enter you IRC nick. This will set the owner of the bot, used for some commands.");
 		String owner = scan.nextLine();
 		System.out.println("Now, enter a name for your bot. It needs to be unique on the IRC network you are using.");
 		String botName = scan.nextLine();
 		System.out.println("Thirdly, enter the network URL of the IRC network you are using (eg irc.freenode.net).");
 		String ircNet = scan.nextLine();
 		System.out.println("now enter the names of the chatrooms you want to include, with all leading characters, seperated by spaces.");
 		String[] channel = scan.nextLine().split(" ");
 
 		try{
 			File f = new File("settings.txt");
 			FileWriter fw = new FileWriter(f, true);
 			fw.write(owner +"\n");
 			fw.write(botName+"\n");
 			fw.write(ircNet+"\n");
 			for(int i = 0; i < channel.length; i++)
 			{
 				fw.write(channel[i] + "\n");
 			}
 			fw.close();
 		}
 		catch(Exception e){System.out.println(e.getMessage());}
 	}
 		 
 
 	public void onMessage(String channel, String sender, String login, String hostname, String message)
 	{
 		DateFormat df = DateFormat.getInstance();
 
 		String time = df.format(new java.util.Date());
 		System.out.println(channel + ", " + time + " | " + sender + ": " + message);
 	
 		log(channel, sender, message);
 		if(sender.equalsIgnoreCase(owner) && message.indexOf("quit") > -1)
 		{
 			quit();
 		}
 		else if(sender.equalsIgnoreCase(owner) && message.indexOf("say") > -1)
 		{
 			say(message);
 		}
 		else if(sender.equalsIgnoreCase(owner) && message.indexOf("join") > -1)
 		{
 			join(message);
 		}
		else if((message.indexOf(botName) < botName.length() && message.indexOf(botName) > -1) || message.indexOf("http://") > -1 || message.indexOf("https://") > -1) 
 		{
 			sendMessage(channel, sender+": "+m.doMod(message, sender));
 		}
 	}
 	public void onPart(String channel, String sender, String login, String hostname)
 	{
 		System.out.println(channel+": ====="+sender+" has left====");
 		log(channel, sender, "--Has quit--");
 	}
 
 	public void onJoin(String channel, String sender, String login, String hostname)
 	{
 		System.out.println(channel+": ===="+sender+" has joined====");
 		log(channel, sender, "--Has joined--");
 	}
 
 	
 	public void onDisconnect()
 	{
 
 		System.exit(0);
 	}
 	
 	public void onKick(String channel, String kicker, String kickerLogin, String kickerHost,String nick, String reason)
 	{
 		if(nick.equalsIgnoreCase(botName))
 		{
 			System.out.println("======Bot kicked by "+kicker+" because "+reason+"=====");
 			System.out.println("======Rejoining=======");
 			joinChannel(channel);
 		}
 	}
 
 	public void log(String channel, String sender, String message)
 	{
 		DateFormat df = DateFormat.getInstance();
 
 		String time = df.format(new java.util.Date());
 		try{
 			boolean s = (new File("logs").mkdir());
 			File f = new File("logs/"+channel+".txt");
 			FileWriter fw = new FileWriter(f, true);
 			fw.write(time + "| " + sender + ": " + message + "\n");
 			fw.close();
 		}
 		catch(Exception e){System.out.println(e.getMessage());}
 	}
 
 
 /***********
 * Core admin functions
 */
 	public void quit()
 	{
 			disconnect();
 	}
 
 	public void say(String message)
 	{
 
 			String[] parts = message.split(" ");
 			String chan = parts[2];
 			String toSend = "";
 			String temp = "";
 			for(int i = 3; i < parts.length; i++)
 			{
 				toSend += " "+parts[i];
 			}
 			sendMessage(chan, toSend);
 	}
 	
 	public void join(String message)
 	{
 			String newChan = message.split("join ")[1];
 			joinChannel(newChan);
 	}
 	
 }
