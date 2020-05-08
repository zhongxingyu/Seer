 package sfs2x.extensions.projectsasha.game.utils;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import sfs2x.extensions.projectsasha.game.entities.gateways.Gateway;
 import sfs2x.extensions.projectsasha.game.entities.software.Software;
 
 public class TimerHelper {
     Timer timer;
     Gateway from, to;
     Software callerClass;
 
 	
 	public TimerHelper(int seconds, Gateway from, Gateway to)
     {
         timer = new Timer();
         this.from = from;
         this.to = to;
        timer.schedule(new RemindFromGateway(Gateway from, Gateway to), seconds*1000);
 	}
 	
     public TimerHelper(int seconds, Software caller, Gateway from, Gateway to)
     {
         timer = new Timer();
         this.callerClass = caller;
         this.from = from;
         this.to = to;
         timer.schedule(new RemindTaskWithParams(from, to), seconds*1000);
 	}
 	
     public TimerHelper(int seconds, Software caller) {
         timer = new Timer();
         this.callerClass = caller;
         timer.schedule(new RemindTask(), seconds*1000);
 	}
 	
     class RemindFromGateway extends TimerTask
 	{
 		Gateway from, to;
         public RemindFromGateway(Gateway from, Gateway to) {
         	this.from = from;
         	this.to = to;
 		}
 		
 		public void run() {
             from.startTimedEvent();
         }
     }
 	
     class RemindTask extends TimerTask 
 	{
 		public void run() {
             callerClass.startTimedEvent();
         }
     }
     
     class RemindTaskWithParams extends TimerTask 
 	{
     	Gateway from, to;
         public RemindTaskWithParams(Gateway from, Gateway to) {
         	this.from = from;
         	this.to = to;
 		}
 
 		public void run() {
             callerClass.startTimedEvent(this.from, this.to);
         }
     }
 }
