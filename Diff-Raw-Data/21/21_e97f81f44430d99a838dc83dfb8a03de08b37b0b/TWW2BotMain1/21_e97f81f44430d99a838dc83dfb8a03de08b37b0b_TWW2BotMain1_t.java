 /**
  * @(#)TWW2Bot.java
  *
  *
  * @author 
  * @version 1.00 2010/12/1
  */
 import java.util.*;
 import java.io.*;
 import java.net.*;
 import java.text.*;
 
 class TWW2BotListener extends Thread {
 
 	private static final String OWNER = "TWW2";
 	private static final String OWNER2 = "Twuwutu";
 	private List<String> spojadayUsers = new ArrayList<String>();
   	private Scanner input;
   	private PrintStream output;
 
 	public TWW2BotListener( Scanner input, PrintStream output ) {
    		this.input = input;
    		this.output = output;
 		
 		spojadayUsers.add("tboyd");
 		spojadayUsers.add("hua");
 		spojadayUsers.add("naonao1234");
 		spojadayUsers.add("mauriciofmar3");
 		spojadayUsers.add("rsalazar");
 		spojadayUsers.add("jason_fisher");
 		spojadayUsers.add("guth");
 		spojadayUsers.add("random_guy");
 		spojadayUsers.add("spastic");
 		spojadayUsers.add("wcornnell");
 		spojadayUsers.add("zaz76");
 		spojadayUsers.add("cricycle");
 		Collections.sort(spojadayUsers);
   	}
   
   	public void run() {
   		String readMessage;
   		boolean notDone = true;
     	while(notDone && input.hasNextLine() && !(readMessage = input.nextLine()).equals("CLOSE") ) {
     		String[] readArray = readMessage.split(":");
     		if(readArray[0].equals("PING "))
     		{
     			output.println("PONG :" + readArray[1]);
     		} else if (readArray.length > 1) {
     			String[] cmdLine = readArray[1].split(" ");
     			if(cmdLine.length > 1)
     			{
     				String sender = cmdLine[0];
     				String command = cmdLine[1];
     				
     				/********************************************************
     				 *														*
     				 *						COMMANDS						*
     				 *														*
     				 ********************************************************/
     				if(command.equals("PRIVMSG"))
     				{
     					sender = sender.substring(0, sender.indexOf('!'));
     					String target = cmdLine[2];
     					String mes = "";
     					for(int i=2; i<readArray.length; ++i)
     					{
     						mes += readArray[i];
     					}
     					
     					System.out.println(sender + "@" + target + ": " + mes);
 						notDone = this.processMessage(sender, target, mes);
     				} else {
     				    System.out.println(command + " ~~~ " + readMessage);
     				}
     				
     				/******************END OF COMMANDS *********************/
     				
     			} else {
 					System.out.println("NOCOMMAND! ~~~ " + readMessage);
     			}
     		} else {
     			System.out.println("NOCOLON! ~~~ " + readMessage);
     		}
     	}//End While
     	input.close();
     }
 	
 	private boolean processMessage(String sender, String target, String message)
 	{
 		if(target.equalsIgnoreCase("tww2bot1"))
 		{
 			/*********** THIS IS A WHISPER *****************/
 			if(sender.equalsIgnoreCase(OWNER) && message.equalsIgnoreCase("KILL"))
 			{
 				return false;
 			}
 			target = sender;
 		}
 		if(message.charAt(0) == '!')
 		{
 			String[] param = message.substring(1).split(" +");
 			
 			/************************************************
 			*				TWW2Bot Commands				*
 			************************************************/
 			if(param[0].equalsIgnoreCase("help"))
 			{
 				if(param.length == 1)
 					this.sendMessage(target, "My commands are: HELP, SPOJ, SPOJADAY, SPOJLINK and KARMABOMB; type \"!help [cmd]\" for more help");
 				else 
 				{
 					//SO META
 					if(param[1].equalsIgnoreCase("help")) {
 						this.sendMessage(target, "I think you already got it...");
 						
 					//HELP SPOJ
 					}else if(param[1].equalsIgnoreCase("spoj")) {
 						if(param.length > 1)
 						{
 							this.sendMessage(target,"Syntax is \"!spoj [username] [info]\", supported info parameters are: LASTSUB and LASTSOLVE");
 							//LASTSOLVE, SCORE, RANK and UFRANK
 						}
 					
 					}else if(param[1].equalsIgnoreCase("spojaday")) {
 						this.sendMessage(target, "Shows a list of ufpters who have and who have not solved a spoj problem today.");
 					
 					}else if(param[1].equalsIgnoreCase("spojlink")) {
 						this.sendMessage(target, "For lazy people who don't want to have to type out simple problem URLs.");
 					
 					//TROLL HELP OPTIONS
 					}else if(param[1].equalsIgnoreCase("karmabomb")) {
 						this.sendMessage(target, "Pretty self explanatory really...");
 					}else if(param[1].equalsIgnoreCase("huabot")) {
 						this.sendMessage(target, "~help He can do that himself");
 					} else if(param[1].equalsIgnoreCase("maubot")) {
 						this.sendMessage(target, "LOL");
 					} else if(param[1].equalsIgnoreCase("[cmd]")) {
 						this.sendMessage(target, "Well aren't you mr. literal?");
 					} else {
 						this.sendMessage(target, "I can't help you with " + param[1]);
 					}
 				}
 				
 			} else if(param[0].equalsIgnoreCase("spoj"))
 			{
 				if(param.length < 2)
 				{
 					this.sendMessage(target, "Syntax is \"!spoj [username] [info]\", enter \"!help spoj\" for more info");
 				} else {
 					String user = param[1];
 					if(param.length < 3 || param[2].equalsIgnoreCase("LASTSUB"))
 					{
 						this.sendMessage(target, this.getLastSpojSub(user));
 					} else if(param[2].equalsIgnoreCase("LASTSOLVE")){
 						this.sendMessage(target, this.getLastSpojSolve(user));
 					} else {
 						this.sendMessage(target, "not yet supported!");
 					}
 				}
 			} else if(param[0].equalsIgnoreCase("spojaday"))
 			{
 				String[] lists = spojaday();
 				this.sendMessage(target, lists[0].toString() + " have spojadayed!");
 				this.sendMessage(target, lists[1].toString() + " only have " + getTimeTillMidnight() + " left!");
 			} else if(param[0].equalsIgnoreCase("spojlink"))
 			{
 				if(param.length < 2)
 				{
 					this.sendMessage( target, "Syntax is \"!spojlink [problem ID]\"");
 				} else {
 					this.sendMessage( target, "http://www.spoj.pl/problems/" + param[1].toUpperCase() + "/");
 				}
 			} else if(param[0].equalsIgnoreCase("karmabomb"))
 			{
 				if(param.length < 2)
 				{
 					this.sendMessage( target, "Syntax is \"!karmabomb [target]\"" );
 				} else {
 					this.sendMessage( target, "Not cool man... --" + sender);
 				}
 			}
 		}
 		return true;
 	}
 	
 	private void sendMessage(String target, String message)
 	{
 		output.println("PRIVMSG " + target + " :" + message);
 	}
 	
 	private BufferedReader getUserSolveStream(String user) throws IOException
 	{
 		BufferedReader in;
 		try {
 			// Create a URL for the desired page
 			URL url = new URL("http://www.spoj.pl/status/"+ user.toLowerCase() + "/signedlist/");
 		
 			// Read all the text returned by the server
 			in = new BufferedReader(new InputStreamReader(url.openStream()));
 			
 		} catch (MalformedURLException e) {
 			in = null;
 			throw new IOException();
 		}
 		return in;
 	}
 	
 	private String getLastSpojSub(String user)
 	{
 		try {
 			BufferedReader in = getUserSolveStream(user);
 			String line = "";
 			for(int i=0; i<10; ++i)
 			{
 				line = in.readLine();
 			}
 			String[] info = line.split(" *\\| *");
 			if(info.length < 5)
 			{
 				return("User " + user + " has no submissions.");
 			}
 			String retCode = info[4];
 			String prob = info[3];
 			String strDate = info[2];
 			in.close();
 			
 			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 			Calendar c = new GregorianCalendar();
 			try
 			{
 				c.setTime(df.parse(strDate));
 				c.add(Calendar.HOUR_OF_DAY, -6);
 				strDate = c.getTime().toString();
 			} catch(ParseException e)
 			{}	
 			
 			return(user + "'s last submission got " + retCode + " on " + prob + " on " + strDate);
 		} catch(IOException e) {
 			return("User " + user + " was not found on SPOJ.");
 		}
 		
 	}
 	
 	private String getLastSpojSolve(String user)
 	{
 		try {
 			BufferedReader in = getUserSolveStream(user);
 			String line = "";
 			for(int i=0; i<10; ++i)
 			{
 				line = in.readLine();
 			}
 			String[] info = line.split(" *\\| *");
 			
 			while(info.length >= 5 && !info[4].equalsIgnoreCase("AC"))
 			{
 				line = in.readLine();
 				info = line.split(" *\\| *");
 			}
 			
 			if(info.length < 5)
 			{
 				return("User " + user + " has no correct submissions.");
 			}
 			
 			String retCode = info[4];
 			String prob = info[3];
 			String strDate = info[2];
 			in.close();
 			
 			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 			Calendar c = new GregorianCalendar();
 			try
 			{
 				c.setTime(df.parse(strDate));
 				c.add(Calendar.HOUR_OF_DAY, -6);
 				strDate = c.getTime().toString();
 			} catch(ParseException e)
 			{}	
 			
 			return(user + "'s last classical problem solved was " + prob + " on " + strDate);
 		} catch(IOException e) {
 			return("User " + user + " was not found on SPOJ.");
 		}
 		
 	}
 	
 	private String[] spojaday()
 	{
 		List<String> haveSpojadayed = new ArrayList<String>();
 		List<String> notSpojadayed = new ArrayList<String>();
 		Date rightNow = new Date();
 		
 		for(String s : spojadayUsers)
 		{
 			Date dateOfSolve = getDateOfLastSolve(s);
 			
 			if(checkIfSameDay(rightNow, dateOfSolve))
 			{
 				haveSpojadayed.add(s);
 			} else {
 				notSpojadayed.add(s);
 			}
 		}
 		String[] ret = new String[2];
 		ret[0] = "";
 		ret[1] = "";
 		for(String s : haveSpojadayed)
 		{
 			ret[0] += s + ", ";
 		}
 		for(String s : notSpojadayed)
 		{
 			ret[1] += s + ", ";
 		}
 		return ret;
 		
 	}
 	
 	private boolean checkIfSameDay(Date d1, Date d2)
 	{
 		Calendar calc = new GregorianCalendar();
 		calc.setTime(d1);
 		int d1Day = calc.get(Calendar.DAY_OF_YEAR);
 		int d1Year = calc.get(Calendar.YEAR);
 		calc.setTime(d2);
 		int d2Day = calc.get(Calendar.DAY_OF_YEAR);
 		int d2Year = calc.get(Calendar.YEAR);
 		return(d1Day == d2Day && d1Year == d2Year);
 	}
 	
 	
 	private Date getDateOfLastSolve(String user)
 	{
 		try {
 			BufferedReader in = getUserSolveStream(user);
 			String line = "";
 			for(int i=0; i<10; ++i)
 			{
 				line = in.readLine();
 			}
 			String[] info = line.split(" *\\| *");
 			
 			while(info.length >= 5 && !info[4].equalsIgnoreCase("AC"))
 			{
 				line = in.readLine();
 				info = line.split(" *\\| *");
 			}
 			
 			if(info.length < 5)
 			{
 				return(new Date(0));
 			}
 			
 			String strDate = info[2];
 			in.close();
 			
 			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 			Calendar c = new GregorianCalendar();
 			Date ret = new Date(0);
 			try
 			{
 				c.setTime(df.parse(strDate));
 				c.add(Calendar.HOUR_OF_DAY, -6);
 				ret = c.getTime();
 			} catch(ParseException e)
 			{}	
 			
 			return(ret);
 		} catch(IOException e) {
 			return(new Date(0));
 		}
 	}
 	
 	private String getTimeTillMidnight()
 	{
 		Calendar now = new GregorianCalendar();
 		now.setTime(new Date());
 		int hr = 23 - now.get(Calendar.HOUR_OF_DAY);
 		int min = 59 - now.get(Calendar.MINUTE);
 		int sec = 60 - now.get(Calendar.SECOND);
 		String ret = "";
 		System.out.println("It is " + hr + ":" + min + ":" + sec);
		
		if(sec >= 60)
		{
			sec -= 60;
			++min;
		}
		
		if(min >= 60)
		{
			min -= 60;
			++hr;
		}
		
		if(hr >= 2)
 		{
 			ret += " " + hr + " hours";
 		} else if(hr == 1)
 		{	
 			ret += " " + hr + " hour";
 		}
 		
		if(min >= 2)
 		{
 			ret += " " + min + " minutes";
 		} else if(min == 1)
 		{	
 			ret += " " + min + " minute";
 		}
 		
		if(sec >= 2)
 		{
 			ret += " " + sec + " seconds";
 		} else if(sec == 1)
 		{	
 			ret += " " + sec + " second";
 		}
 		
 		return ret;
 	}
 	
 }
 
 
 public class TWW2BotMain1 {
 	
 	private static final String HOST_NAME = "chat.freenode.net";
 	private static final int PORT_NUMBER = 8001;
 	private static final String NICK = "TWW2bot1";
 	private static final String FULL_NAME = "Programmed by TJ Boyd";
 	private static final String CHANNEL = "#ufpt";
 	//private static final String PASSWORD = "not_getting_this";
 	
     public static void main(String[] args) {
     	
     	Socket clientSocket;
     	//Scanner stdIn;
     	Scanner input;
     	PrintStream output;
     	TWW2BotListener outThread;
     	try {
        		clientSocket = new Socket(HOST_NAME, PORT_NUMBER);
        		//stdIn = new Scanner(System.in);
        		input = new Scanner(clientSocket.getInputStream());
        		output = new PrintStream(clientSocket.getOutputStream());
        		outThread = new TWW2BotListener(input, output);
        		
        		outThread.start();
 			
 			output.println("NICK " + NICK);
 			output.println("USER " + NICK + " 0 * :" + FULL_NAME);
 			output.println("JOIN " + CHANNEL);
 			//output.println("PRIVMSG nickserv :identify " + PASSWORD);
 			
 			String sendMsg = "";
         	//while(!(sendMsg = stdIn.nextLine()).equals("CLOSE"))
         	//{
         	//	if(sendMsg.length() > 0 && sendMsg.charAt(0) == ':')
         	//		output.println("PRIVMSG " + CHANNEL + " " + sendMsg);
         	//	else
         	//		output.println(sendMsg);
         	//}
 			//output.println(sendMsg);
 			
 			//stdIn.close();
 			
         }
         catch (IOException e) {
         	System.out.println(e);
         }
     	
     }
 }
