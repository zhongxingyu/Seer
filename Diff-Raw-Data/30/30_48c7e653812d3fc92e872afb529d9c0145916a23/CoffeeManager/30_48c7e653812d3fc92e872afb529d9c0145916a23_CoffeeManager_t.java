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
		return unixRing.getElementsAfter(new CoffeeState(unixtime, 0));
 	}
 
 	public CoffeeState mostRecent() {
		return unixRing.current();
 	}
 
 	public void read() {
		// TODO we should fix this also, maybe add log4j 
 		try {
			unixRing.add(reader.readWeight());
 		} catch (PhidgetException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void run() {
 		read();
 	}
 
 }
