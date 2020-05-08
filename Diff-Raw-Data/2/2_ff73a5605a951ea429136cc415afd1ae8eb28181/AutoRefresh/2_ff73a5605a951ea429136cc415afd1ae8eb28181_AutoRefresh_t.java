 package sta.andswtch.extensionLead;
 
 
 public class AutoRefresh implements Runnable {
 	
 	private ExtensionLead extLead;
 	private boolean isRunning = false;
 	private Thread thread;
 	
 	AutoRefresh(ExtensionLead extLead) {
 		this.extLead = extLead;
 	}
 	
 	public boolean isAutoRefreshRunning() {
 		return this.isRunning;
 	}
 	
 	public void startAutoRefresh() {
 		if(this.thread == null) 
 			this.thread = new Thread(this);
 		if(!this.thread.isAlive())
 			if(!this.isRunning)
 				this.thread.start();
 	}
 	
 	public void stopAutoRefresh() {
 		this.isRunning = false;
 	}
 	
 	@Override
 	public void run() {
 		this.isRunning = true;
 		while(this.isRunning) {
 			int seconds = this.extLead.getUpdateInterval();
 			try {
 				Thread.sleep(seconds * 1000);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}	
			if(seconds != 0 && this.isRunning) {
 				this.extLead.sendUpdateMessage();
 			}
 		}
 		this.thread = null;
 	}
 	
 }
