 package annahack.nethackparser;
 
 import java.util.Queue;
 import java.io.IOException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import annahack.nethackinformation.nethackplayer.PlayerWriter;
 import annahack.nethackinformation.nethackplayer.Player;
 import annahack.telnetconnection.TelnetInterface;
 
 public class NetHackParser
 {
 	private TelnetInterface com;
 	private Queue<String> messageBuf;
 	private Queue<String> itemsBuf;
 	private PlayerWriter pw;
 	private int score;
 	private int turn;
 	private int dlvl;
 	private StatusLineParser statusLine;
 	
 	public NetHackParser(TelnetInterface com)
 	{
 		this.com=com;
 		messageBuf=new java.util.LinkedList<String>();
 		pw=new PlayerWriter(); //TODO: Implement ability to choose race, gender, etc.
 		statusLine = new StatusLineParser(com, pw);
 	}
 	
 	/**
 	 * @return
 	 * 0 if there were no messages,
 	 * 1 if there were messages
 	 * 2 if there were messages and space needs to be sent (--More--)
 	 * 3 if the last message is a prompt
 	 */
 	public byte checkMessages() throws IOException
 	{
 		String line;
 		line=new String(com.peekLine(0));
 		
 		if (line.equals("                        "))
 		{
 			//Nothing
 			return 0;
 		}else{
 			//Something
 			int search=line.indexOf("--More--");
 			if (search!=-1)
 			{
 				//Messages and --More--
 				String[] msgs=line.substring(0, search).split("  ");
 				for (int i=0; i<msgs.length; i++)
 				{
 					messageBuf.add(msgs[i].trim());
 				}
 				return 2;
 			}else{
 				//No --More-- on first line
 				
 				if (thingsThatAreHere(0))
 					return 2;
 				
 				if (line.matches("There is an? ([a-z ]+) here."))
 				{
 					messageBuf.add(line);
 					
 					if (thingsThatAreHere(2))
 						return 2;
 					
 					System.err.println("Unable to figure out line:\n"+line);
 					throw new UnparseableBullshitException(line);
 				}else{
 					StringBuffer messageBlob=new StringBuffer(line);
 					for (int i=1; i<23; i++)
 						messageBlob.append(new String(com.peekLine(0)));
 					return 0;
 				}
 				
 			}
 		}
 		
 	}
 	
 	public String popMessage()
 	{
 		return messageBuf.poll();
 	}
 	
 	public boolean hasMessages()
 	{
 		return !messageBuf.isEmpty();
 	}
 	
 	/**
 	 * Checks for a "things that are here" starting on the given line.
 	 * Line should be 0 or 2.
 	 * Returns true if things are here.
 	 **/
 	private boolean thingsThatAreHere(int linenum) throws IOException
 	{
 		String line=new String(com.peekLine(linenum));
 		
 		int search=line.indexOf("Things that are here");
 		
 		if (search==-1)
 			return false;	//Clearly, things are not here
 		
 		//Item list
 		for (int i=1;
 		(line=new String(com.peekLine(i), search, 80-search).trim()).
 			indexOf("--More--")==-1; i++)
 		{
 			itemsBuf.add(line);
 		}
 		
 		return true;
 	}
 	
 	
 	public Player getPlayer()
 	{
 		return pw.getPlayer();
 	}
 	
 	public int getScore()
 	{
 		return score;
 	}
 	
 	public int getDLvl()
 	{
 		return dlvl;
 	}
 	
 	public int getTurn()
 	{
 		return turn;
 	}
 	
 	public boolean debug_parseStatusLine()
 	{
 		try{
 			if(!statusLine.parseStatusLine())
 			{
				return false;
 			}
 			else
 			{
 				dlvl = statusLine.getDlvl();
 				turn = statusLine.getTurn();
 				score = statusLine.getScore();
 				return true;
 			}
 		}catch(Exception e)
 		{
 			System.err.println("Status Line Parsing Error");
 			e.printStackTrace();
 			return false;
 		}
 	}
 }
