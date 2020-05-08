 package net.sf.ictalive.eventbus.dashboard;
 
 import java.io.File;
 
 public class StartEventBus
 {	
 	public static EventBusDaemon	ebd;
 
 	static
 	{
 		ebd = null;
 	}
 	
 	public String getProblems()
 	{
 		String res;
 		
 		if(ebd == null || !ebd.isRunning())
 		{
 			res = null;
 		}
 		else
 		{
 			res = "EventBus instance already running. Please stop before starting again.";
 		}
 		
 		return res;
 	}
 
 	public void run()
 	{
 		try
 		{
 			ebd = new EventBusDaemon(File.createTempFile("test", "test").getParentFile().getAbsolutePath());
 			ebd.start();
 		}
 		catch (Exception e)
 		{
 			System.out.println(e.getMessage());
 			e.printStackTrace();
 			ebd = null;
 		}
 	}
	
	public static void main(String[] args)
	{
		StartEventBus	seb;
		
		seb = new StartEventBus();
		seb.run();
		(new Thread()).start();
	}
 }
