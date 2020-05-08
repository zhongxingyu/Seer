 package annahack.dglconnection;
 
 import org.apache.commons.net.telnet.*;
 import java.net.SocketException;
 import java.io.IOException;
 import org.apache.commons.net.telnet.InvalidTelnetOptionException;
 import annahack.telnetconnection.*;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 
 abstract class GeneralDGLConnector implements DGLServer
 {
 	protected ApacheBasedTelnetInterface connection;
 	protected boolean spectating, mainMenu, inGame, loggedIn, dead;
 	public GeneralDGLConnector()
 		throws SocketException, IOException, InvalidTelnetOptionException
 	{
 		TelnetClient tc;
 		tc = new TelnetClient();
 
         TerminalTypeOptionHandler ttopt = new TerminalTypeOptionHandler("VT100", false, false, true, false);
         EchoOptionHandler echoopt = new EchoOptionHandler(true, false, true, false);
         SuppressGAOptionHandler gaopt = new SuppressGAOptionHandler(true, true, true, true);
 
         tc.addOptionHandler(ttopt);
         tc.addOptionHandler(echoopt);
         tc.addOptionHandler(gaopt);
         
 		tc.connect(server(), 23);
 		
 		connection=new ApacheBasedTelnetInterface(tc);
 		
 		spectating = false;
 		mainMenu=true;
 		inGame=false;
 		loggedIn=false;
 		dead=false;
 	}
 	
 	//This should handle logging in on all dgl servers
 	public boolean login()
 	{
 		if (!loggedIn() && mainMenu())
 		{
 			try
 			{
 				BufferedReader up=new BufferedReader(new FileReader("botauth"));
 				String u=up.readLine();
 				String p=up.readLine();
 				up.close();
 				
 				connection.send('l');
 				connection.send(u.getBytes());
 				connection.send('\n');
 				connection.send(p.getBytes());
 				connection.send('\n');
 				return true;
 			}catch(IOException e)
 			{
 				return false;
 			}
 		}else{
 			return false;
 		}
 	}
 	
 	abstract String server();
 	
 	public boolean inGame()
 	{
 		return inGame;
 	}
 	
 	public boolean loggedIn()
 	{
 		return loggedIn;
 	}
 	
 	public boolean mainMenu()
 	{
 		return mainMenu;
 	}
 	
 	public boolean spectating()
 	{
 		return spectating;
 	}
 	
 	public boolean startGame()
 	{
 		if(loggedIn() && mainMenu())
 		{
 			try
 			{
 				connection.send('p');
 				inGame=true;
 				return true;
 			}
 			catch(IOException e)
 			{
 				inGame=false;
 				return false;
 			}
 		}
 		else
 		{
 			inGame=false;
 			return false;
 		}
 	}
 	
 	public boolean watch(String name) throws IOException
 	{
 		try
 		{
 			connection.send('w');
 			System.out.println(connection.peekLine(1));
 		}
 		catch(IOException e)
 		{
 			throw e;
 			//Is this what you meant by "return false if target is not playing (other errors should be an exception)?
 		}
 		
 		
 		return false;
 	}
 	
 	public TelnetInterface dumpInterface()
 	{
 		if(loggedIn() && (inGame() || spectating()))
 		{
 			dead=true;
 			TelnetInterface returno=connection;
 			connection=null;
 			return returno;
 		}
 		else
 		{
 			return null;
 		}
 	}
 }
