 package org.veiset.coffew8.coffeepi;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 
 
 import com.phidgets.PhidgetException;
 
 public class CoffeeManager extends TimerTask {
 
 	private final UnixtimeRingBuffer unixRing;
 	private CoffeeReader reader;
 
 	public CoffeeManager(int bufferSize, int interval) {
 		unixRing = new UnixtimeRingBuffer(bufferSize);
 		try {
 			this.reader = new CoffeeReader();
 		} catch (PhidgetException e) {
 			// TODO fix this
 			e.printStackTrace();
 		}
 		Timer timer = new Timer();
 		timer.scheduleAtFixedRate(this, 0, (int) interval * 1000);
 	}
 
 	public CoffeeState[] get(long unixtime) {
		return getUnixRing().getElementsAfter(new CoffeeState(unixtime, 0));
 	}
 
 	public CoffeeState mostRecent() {
		return getUnixRing().current();
 	}
 
 	public void read() {
 		try {
			getUnixRing().add(getReader().readWeight());
 		} catch (PhidgetException e) {
 			e.printStackTrace();
 		}

	}

	private synchronized UnixtimeRingBuffer getUnixRing(){
		return unixRing;
	}

	private synchronized CoffeeReader getReader(){
		return reader;
 	}
 
 	@Override
 	public void run() {
 		read();
 	}
 
 }
