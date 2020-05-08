 package org.hydra.ant;
 
 import org.apache.tools.ant.BuildEvent;
 import org.apache.tools.ant.DefaultLogger;
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.util.StringUtils;
 
 public class HydraLogger extends DefaultLogger {
 
 	private static String NEST_START  = Character.toString((char)27) + "[p";
 	private static String NEST_STOP   = Character.toString((char)27) + "[q";
 	
 	@Override
 	public void targetFinished(BuildEvent event) {
 		printMessage(NEST_STOP, out, event.getPriority());
         log(NEST_STOP);
 	}
 	
 	@Override
 	public void targetStarted(BuildEvent event) {
         if (Project.MSG_INFO <= msgOutputLevel && !event.getTarget().getName().equals("")) {
         	String msg = StringUtils.LINE_SEP + NEST_START + event.getTarget().getName() + ":";
         	printMessage(msg, out, event.getPriority());
             log(msg);
         }
 	}
 	
 }
