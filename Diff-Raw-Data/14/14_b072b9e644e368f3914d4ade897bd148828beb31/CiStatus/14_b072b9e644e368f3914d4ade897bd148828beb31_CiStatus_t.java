 import java.io.IOException;
 import java.util.Vector;
 
 public class CiStatus
 {
 
 	static String jenkins_addr = "http://ci.jenkins-ci.org/view/Infrastructure/";
 	static int update_delay = 20000;
 	
 	
 	Vector<Build> builds;
 	Scraper scraper;
 	IconManager im;
	boolean should_notify = true;
 	
 	public static void main (String [] args) throws Exception 
 	{
 		int i = 0, j;
 		String arg;
 		char flag;
 
 	    while (i < args.length && args[i].startsWith("-")) {
 	        arg = args[i++];
 
 	// use this type of check for arguments that require arguments
 	        if (arg.equals("--delay"))
 	        {
 	            if (i < args.length)
 	            {
 	                update_delay = Integer.parseInt(args[i++]) * 1000;
 	            	System.out.println("Delay: " + update_delay);
 	            }
 	            else
 	                System.err.println("-delay requires the number of seconds between update");
 	        }
 	        // use this type of check for arguments that require arguments
             else if (arg.equals("--addr")) {
                 if (i < args.length)
                 {
                     jenkins_addr = args[i++];
                     System.out.println("Address: " + jenkins_addr);
                 }
                 else
                     System.err.println("-output requires a filename");
             }
 	
 	// use this type of check for a series of flag arguments
 	        else {
 	            for (j = 1; j < arg.length(); j++) {
 	                flag = arg.charAt(j);
 	                switch (flag) {
 	                case 'j':
 	                    //set mode as jenkins
 	                    break;
 	                case 'h':
 	                    //set mode as hudson
 	                    break;
 	                default:
 	                    System.err.println("CiStatus: illegal option " + flag);
 	                    break;
 	                }
 	            }
 	        }
 	    }
 	    
 	    //if (i == args.length && i > 0)
 	    //    System.err.println("Usage: CiStatus [-addr <address>] [-delay <delay>]");
 	 
 		new CiStatus();
 	}
 	
 	CiStatus() throws IOException, InterruptedException
 	{
 		should_notify = System.getProperty("os.name").equals("Linux");
 		System.out.println("Should show notifications: " + should_notify);
 		
 		scraper = new Scraper();		
 		builds = getBuilds();
 		System.out.println("Found " + builds.size() + " builds.");
 		im = new IconManager(builds);
 		
 		while(true)
 		{
 			update();
 			Thread.sleep(update_delay);
 		}
 	}
 	
 	private Vector<String> getBuildDiff(Vector<Build> newBuilds) throws IOException
 	{
 		Vector<String> buildDiffs = new Vector<String>();	
 		for(Build b : builds)
 		{
 			for (Build nb :newBuilds)
 			{
 				if (b.name.equals(nb.name) && b.condition != nb.condition)
 				{
 					String msg = nb.name + " has changed from " + b.status + " to " + nb.status;
 					System.out.println(msg);
 					buildDiffs.add(msg);
 				}
 			}
 		}
 		return buildDiffs;
 	}
 	
 	private Vector<Build> getBuilds() throws IOException
 	{
 		System.out.println("fetching builds...");
 		return scraper.getBuilds(jenkins_addr);
 	}
 
 	private void displayBuilds(Vector<Build> newBuilds)
 	{
 		im.updateIcons(newBuilds);
 	}
 
 	private void displayChanges(Vector<String> buildDiffs) throws InterruptedException, IOException {
 		Runtime rt = Runtime.getRuntime();
 		String notifications = "";
 		for (String s :buildDiffs)
 		{
 			notifications += s + "\n";
 		}
 		if (buildDiffs.size() > 0)
 		{
      String[] cmd = new String[3];
      cmd[0] = "notify-send";
      cmd[1] = "Jenkins";
      cmd[2] = notifications;

			Process p = rt.exec(cmd);
 			p.waitFor();
 		}
 	}
 	
 	private void update()
 	{
 		try {
 			Vector<Build> newBuilds = getBuilds(); 
 			//scraper.printBuilds(builds);
 			displayBuilds(newBuilds);
 			
 			if (should_notify)
 			displayChanges(getBuildDiff(newBuilds));
 			//TODO builds = newBuilds; // Update builds
 		}
 		catch(Exception e)
 		{
 			System.err.println(e);
 		}
 	}
}
