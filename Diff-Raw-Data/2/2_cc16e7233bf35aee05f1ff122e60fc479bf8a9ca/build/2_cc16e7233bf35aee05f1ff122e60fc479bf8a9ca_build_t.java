 
 package org.de.metux.briegel.cmd;
 
 import org.de.metux.briegel.base.EBriegelError;
 import org.de.metux.briegel.robots.RecursiveBuild;
 
 /* this command builds a given port */
 
 public class build extends CommandBase
 {
     public void cmd_main(String argv[]) throws EBriegelError
     {
	if ((argv == null) || (argv.length == 0) || (argv[0] == null) || (argv[0].isEmpty()))
 	{
 	    System.err.println(myname+": missing port name");
 	    System.exit(exitcode_err_missing_port);
 	}
 	RecursiveBuild bot = new RecursiveBuild(argv[0],getPortConfig(argv[0]));
 	bot.run();
     }
 
     public build()
     {
 	super("build");
     }
 
     public static void main(String argv[])
     {
 	new build().run(argv);
     }
 }
