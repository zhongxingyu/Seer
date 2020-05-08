 
 
 
 package overwatch.security;
 
 import java.util.Vector;
 
 
 
 
 
 /**
  * Runs in the background in a separate thread.
  * Give it things to check with addBackgroundCheck.
  * 
  * @author  Lee Coakley
  * @version 1
  * @see     BackgroundCheck
  */
 
 
 
 
 
 public class BackgroundMonitor
 {
 	private Vector<BackgroundCheck> checks;
 	private Thread                  thread;
 	private boolean					threadTerminate;
 	
 	
 	
 	
 	
 	/**
 	 * Create and begin monitoring immediately in a separate thread.
 	 * There are no default BackgroundCheck objects, you must add some.
 	 */
 	public BackgroundMonitor()
 	{
 		thread = createThread();
 		thread.start();
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Stop monitoring.  Terminates the thread.
 	 */
 	public void stop()
 	{
 		for (;;) {
 			try {
 				threadTerminate = true;
 				thread.join();
 			} catch ( InterruptedException ex) {
 				ex.printStackTrace();
 			}
 		}
 	}
 	
 	
 	
 	
 	
 	public void addBackgroundCheck( BackgroundCheck bc ) {
 		checks.add( bc );
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	///////////////////////////////////////////////////////////////////////////
 	// Internals
 	/////////////////////////////////////////////////////////////////////////
 	
 	private Thread createThread()
 	{
 		return new Thread( new Runnable() {
 			public void run() {
 				doChecks();				
 			}
 		});
 	}
 	
 	
 	
 	
 	
 	private void doChecks()
 	{
 		while ( ! threadTerminate)
 		{
 			for (BackgroundCheck check: checks) {
 				check.onCheck();
 			}
 			
 			try { Thread.sleep( 1000 );
 			} catch (InterruptedException ex) {
 				ex.printStackTrace();
 			}
 		}
 	}
 	
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
