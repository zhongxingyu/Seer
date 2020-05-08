 import java.util.Timer;
 import java.util.TimerTask;
 
 public class ACKTimer {
 	private Timer timer;
 	private boolean timeOut;
 	
 	public ACKTimer() {
 		timer = new Timer();
 		timeOut = false;
 	}
 	
	public ACKTimer(int sec) {
		timer = new Timer();
		timeOut = false;
 		timer.schedule(new ACKTask(), sec*1000); // sets a delay of "sec" seconds before changing the status of timeOut to true
 	}
 	
 	public boolean isTimeOut() {
 		return timeOut;
 	}
 	
 	public void StopTimer() {
 		timer.cancel();
 	}
 	
 	public class ACKTask extends TimerTask {
 		public void run() {
 			timeOut = true;
 			timer.cancel();
 		}
 	}
 }
