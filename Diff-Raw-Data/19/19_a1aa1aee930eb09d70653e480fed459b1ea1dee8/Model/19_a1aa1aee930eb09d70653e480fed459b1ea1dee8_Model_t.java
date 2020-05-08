 package backend;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 
 /**
  * Manages the state of the waterfall
  * @author Merlin
  *
  * Created:  Apr 28, 2011
  */
 public class Model
 {
 	private static Model singleton;
 	private PortManager portManager;
 	private PrintWriter toWaterfall;
 	private BufferedReader fromWaterfall;
 	private static final boolean IS_DEBUGGING = true;
 	/**
 	 * The width of the waterfall
	 * updated 10/15/13 by Armstrong for Wonderfall 2.0
 	 */
	//public static final int NUMBER_OF_VALVES = 16;
	public static final int NUMBER_OF_VALVES = 32;
 	
 	private Model() throws IOException
 	{
 		portManager = new PortManagerDebug();
 		((PortManagerDebug)portManager).start();
 		toWaterfall = new PrintWriter(portManager.getOutputStream(),true);
 		fromWaterfall = new BufferedReader(new InputStreamReader(portManager.getInputStream()));
 	}
 
 	/**
 	 * @return the only one of these that should exist
 	 */
 	public static Model getSingleton()
 	{
 		if (singleton == null)
 		{
 			try
 			{
 				singleton = new Model();
 			} catch (IOException e)
 			{
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 			
 		}
 		return singleton;
 	}
 	
 	
 	protected PortManager getPortManager()
 	{
 		return portManager;
 	}
 
 	/**
 	 * @return true if the waterfall responded correctly
 	 */
 	public boolean setDebug()
 	{
 		//toWaterfall.println("");
 		toWaterfall.println("DEBUG");
 		try
 		{
 			String response = fromWaterfall.readLine( );
 			System.out.println("Debug response: " + response);
 			if (response.equals("ENTER CMD"))
 				return true;
 		} catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	/**
 	 * 
 	 */
 	public void shutDownConnection()
 	{
 		if (IS_DEBUGGING )
 		{
 			PortManagerDebug portManagerDebug = (PortManagerDebug)portManager;
 			portManagerDebug.close();
 		}
 		
 	}
 }
