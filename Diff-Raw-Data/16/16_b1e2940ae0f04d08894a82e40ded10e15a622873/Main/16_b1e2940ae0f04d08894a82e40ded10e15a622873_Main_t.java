 package wIRC;
 import java.io.*;
 import java.net.*;
 import javax.swing.JOptionPane;
 import javax.swing.UIManager;
 
 /**
  * Root structural-object
  * <br><br>
  * This class serves as the root structure of the client but 
  * is gradually forming a specialized socket class which will 
  * eventually branch off and become an IRCSocket class.
  * <br><br>
  * @author	wisteso@gmail.com
  */
 public class Main
 {
 	private static Socket mainSock = null;
 	private static PrintWriter out = null;
 	private static BufferedReader in = null;
 	
 	protected static String hostName = "st0rage.org";
 	protected static String nickName = "Nullname" + (int)(Math.random() * 9000 + 999);
 	protected static String realName = "Anonymous";
 	protected static String userInfo = "No info set.";
 	
 	protected static boolean reconnect = true;
 	protected static int mode = 0;
 	
 	public static void main(String[] args)
 	{
 		try 
 		{
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         }
 		catch (Exception e)
 		{
             System.out.println("Cannot find resources for default style interface.");
         }
 		
 		if (args.length == 3)
 		{
 			hostName = args[0];
 			nickName = args[1];
 			realName = args[2];
 		}
 		else if (args.length == 2)
 		{
 			hostName = args[0];
 			nickName = args[1];
 		}
 		else if (args.length == 1)
 		{
 			hostName = args[0];
 		}
 		else
 		{
 			String arg = JOptionPane.showInputDialog("Enter the host-name:", hostName);
 			if (arg == null) System.exit(0);
 			else hostName = arg;
 			
 			arg = JOptionPane.showInputDialog("Enter your nick-name:", nickName);
 			if (arg == null) System.exit(0);
 			else nickName = arg;
 			
 			arg = JOptionPane.showInputDialog("Enter your real-name:", realName);
 			if (arg == null) System.exit(0);
 			else realName = arg;
 		}
 		
 		C.init();
 		
 		Manager m = new Manager();
		
		connect(m, true);
 	}
 	
 	protected static void sendData(String output)
 	{
 		if (!mainSock.isClosed())
 			out.println(output);
 	}
 	
 	protected static void connect(Manager m, boolean retry)
 	{
 		mode = 1;
 		
 		do
 		{
 			try
 			{
 				mainSock = new Socket(hostName, 6667);
 			} 
 			catch (UnknownHostException e)
 			{
 				if (retry)
 				{
 					mainSock = null;
 					
 					String arg = JOptionPane.showInputDialog("Invalid Host. Re-enter the host-name:", hostName);
 					if (arg == null) System.exit(0);
 					else hostName = arg;
 				}
 				else
 				{
 					System.err.println(e.toString() + hostName);
 		            System.exit(1);
 				}
 			}
 			catch (IOException e)
 			{
 				System.err.println(e.toString());
 	            System.exit(1);
 			}
 		}
 		while (mainSock == null);
 		
 		try
 		{
 			out = new PrintWriter(mainSock.getOutputStream(), true);
             in = new BufferedReader(new InputStreamReader(mainSock.getInputStream()));
 		} 
 		catch (IOException e)
 		{
 			System.err.println("Could not bind I/O to the socket.");
 			System.exit(1);
 		}
 		
 		sendData("NICK " + nickName);
 		sendData("USER " + nickName + " 0 * :" + realName);
 		
 		cycle(m);
 		
 		if (reconnect && mode > -2)
 			connect(m, retry);
 	}
 	
 	protected static void cycle(Manager m)
 	{
 		mode = 2;
 		
 		try
 		{
 			String dataIn;
 			
 			while (true)
 			{
 				if ((dataIn = in.readLine()) != null)
 					m.ProcessMessage(dataIn);  // so this can access it.
 			}
 		}
 		catch (Exception e)
 		{
 			disconnect(e.toString());
 			m.window.println("(NOTICE) You have been disconnected.", C.ORANGE);
 		}
 	}
 	
 	protected static void disconnect(String reason)
 	{
 		if (mode > 0)
 		{
 			mode = -1;
 			
 			if (reason.equals("termination via interface"))
 				mode = -2;
 			
 			System.out.println("Connection closed: " + reason);
 			
 			try
 			{
 				out.close();
 				in.close();
 				mainSock.close();
 			} 
 			catch (IOException e)
 			{
 				System.err.println("SHUTDOWN ERROR: " + e.toString());
 			}
 		}
 	}
 }
