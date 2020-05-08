 package com.nexus.event.events.server;
 
 import com.nexus.main.ShutdownReason;
 
 
 
public class ServerCloseEvent extends ServerEvent{
 	
 	public ShutdownReason Reason;
 	
 	public ServerCloseEvent(ShutdownReason reason){
 		this.Reason = reason;
 	}
 
 	public static class Shutdown extends ServerCloseEvent{
 		
 		public Shutdown(ShutdownReason reason){
 			super(reason);
 		}
 	}
 	
 	public static class Restart extends ServerCloseEvent{
 		
 		public Restart(){
 			super(ShutdownReason.RESTART);
 		}
 	}
 }
