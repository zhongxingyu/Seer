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
 	
 	public NetHackParser(TelnetInterface com)
 	{
 		this.com=com;
 		messageBuf=new java.util.LinkedList<String>();
 		pw=new PlayerWriter(); //TODO: Implement ability to choose race, gender, etc.
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
 	
 	
 	private static final Pattern r_hp = Pattern.compile("HP:([\\d]+)\\(([\\d]+)\\)");
 	private static final Pattern r_mp = Pattern.compile("Pw:([\\d]+)\\(([\\d]+)\\)");
 	private static final Pattern r_dlvl = Pattern.compile("Dlvl:([\\d]+)");
 	private static final Pattern r_gold = Pattern.compile("\\$:([\\d]+)");
 	private static final Pattern r_ac = Pattern.compile("AC:([\\d]+)");
 	private static final Pattern r_turn = Pattern.compile("T:([\\d]+)");
 	private static final Pattern r_xp = Pattern.compile("Xp:([\\d]+)/([\\d]+)");
 	
 	private static final Pattern r_st = Pattern.compile("St:([\\d]+)");
 	private static final Pattern r_dx = Pattern.compile("Dx:([\\d]+)");
 	private static final Pattern r_co = Pattern.compile("Co:([\\d]+)");
 	private static final Pattern r_in = Pattern.compile("In:([\\d]+)");
 	private static final Pattern r_wi = Pattern.compile("Wi:([\\d]+)");
 	private static final Pattern r_ch = Pattern.compile("Ch:([\\d]+)");
 	
 	private static final Pattern r_align = Pattern.compile("(Lawful|Chaotic|Neutral)");
 	
 	private static final Pattern r_score = Pattern.compile("S:([\\d]+)");
 	/**
 	 * Parses the status line, updates if all is found
 	 * @returns false if all of objects on status line are not found
 	 */
 	private boolean parseStatusLine() throws IOException
 	{
 		String topline = new String(com.peekLine(22));
 		String bottomline = new String(com.peekLine(23));
 		
 		Matcher m_st=r_st.matcher(topline);
 		if(!m_st.find())
 			return false;
 		
 		Matcher m_dx=r_dx.matcher(topline);
 		if(!m_dx.find())
 			return false;
 		
 		Matcher m_co=r_co.matcher(topline);
 		if(!m_co.find())
 			return false;
 		
 		Matcher m_in=r_in.matcher(topline);
 		if(!m_in.find())
 			return false;
 		
 		Matcher m_wi=r_wi.matcher(topline);
 		if(!m_wi.find())
 			return false;
 		
 		Matcher m_ch=r_ch.matcher(topline);
 		if(!m_ch.find())
 			return false;
 		
 		Matcher m_align=r_align.matcher(topline);
 		if(!m_align.find())
 			return false;
 		
 		Matcher m_score=r_score.matcher(topline);
 		if(!m_score.find())
 			return false;
 		
 		Matcher m_hp=r_hp.matcher(bottomline);
 		if(!m_hp.find())
 			return false;
 		
 		Matcher m_mp=r_mp.matcher(bottomline);
 		if(!m_mp.find())
 			return false;
 		
 		Matcher m_gold=r_gold.matcher(bottomline);
 		if(!m_gold.find())
 			return false;
 		
 		Matcher m_xp=r_xp.matcher(bottomline);
 		if(!m_xp.find())
 			return false;
 		
 		Matcher m_dlvl=r_dlvl.matcher(bottomline);
 		if(!m_dlvl.find())
 			return false;
 		
 		Matcher m_ac=r_ac.matcher(bottomline);
 		if(!m_ac.find())
 			return false;
 		
 		Matcher m_turn=r_turn.matcher(bottomline);
 		if(!m_turn.find())
 			return false;
 		
 		try
 		{
 			//Begin top line
 			pw.setSt(Integer.parseInt(m_st.group(1)));
 			pw.setCo(Integer.parseInt(m_co.group(1)));
 			pw.setDx(Integer.parseInt(m_dx.group(1)));
 			pw.setIn(Integer.parseInt(m_in.group(1)));
 			pw.setWi(Integer.parseInt(m_wi.group(1)));
 			pw.setCh(Integer.parseInt(m_ch.group(1)));
 			
 			pw.setAlignment(m_align.group());
 			
 			score=Integer.parseInt(m_score.group(1));
 			//End top line
 			
 			//Begin bottom line
 			//TODO: Add gold to inventory
 			dlvl= Integer.parseInt(m_dlvl.group(1));
 			turn= Integer.parseInt(m_turn.group(1));
 			pw.setHp(Integer.parseInt(m_hp.group(1)));
 			pw.setMaxHp(Integer.parseInt(m_hp.group(2)));
 			pw.setMp(Integer.parseInt(m_mp.group(1)));
 			pw.setMaxMp(Integer.parseInt(m_mp.group(2)));
			pw.setAC(Integer.parseInt(m_ac.group(1)));
 			pw.setXp(Integer.parseInt(m_xp.group(1)));
 			pw.setLevel(Integer.parseInt(m_xp.group(2)));
 			//End bottom line
 		}
 		catch (NumberFormatException e)
 		{
 			return false;
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
 		try{return parseStatusLine();}catch(Exception e){return false;}
 	}
 }
