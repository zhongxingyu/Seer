 package chatsession.impl;
 
 import lwtrt.LWTRTConnection;
 import lwtrt.ex.LWTRTException;
 import chatsession.ex.ChatServiceException;
 
 public class Threader {
 
 	private Thread thread;
 	protected LWTRTConnection lwtrtconnection;
 	
 	
 	public Threader(LWTRTConnection lwtrtconnection) {
 
 		this.lwtrtconnection = lwtrtconnection;
 	}
 
 
 	public void startThread() throws ChatServiceException {
 		if (lwtrtconnection == null) {
 			throw new ChatServiceException(
					"Konnte Poller nicht starten, Connection ist null ");
 		}
 		if (thread == null) {
 			thread = new Thread((Runnable) lwtrtconnection);
 			thread.setPriority(Thread.MIN_PRIORITY);
 			thread.start();
 		}
 	}
 	
 
 	public void stopThread() {
 		try {
 			lwtrtconnection.disconnect();
 		} catch (LWTRTException e) {
 			e.printStackTrace();
 		}
 		if ((thread !=null) && thread.isAlive())
 		{
 			thread.stop();
 			thread = null;
 		}
 	}
 
 }
