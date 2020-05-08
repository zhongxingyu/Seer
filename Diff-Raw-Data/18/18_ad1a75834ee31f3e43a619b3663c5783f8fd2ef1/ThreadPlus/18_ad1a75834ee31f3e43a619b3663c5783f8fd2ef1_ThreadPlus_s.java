 //==============================================================================
 // Date Created:		10 October 2009
// Last Updated:		29 August 2012
 //
// File name:			KleptoChanCore.java
 // File author:			Matthew Hydock
 // File description:	An intermediate abstract class, adding minor
 //						functionality to the original Thread class, 
 //==============================================================================
 
 public abstract class ThreadPlus extends Thread
 {
	KleptoChanCore core = KleptoChanCore.getInstance();
 
 	public abstract void run();
 	
 	protected void safeSleep(long ms)
 	// Since I use it more than once, figured it'd be cleaner this way.
 	{
 		try
 		{
 			Thread.sleep(ms);
 		}catch (Exception e)
 		{
 			Debugger.report(e.toString());
 		}
 	}
 }
