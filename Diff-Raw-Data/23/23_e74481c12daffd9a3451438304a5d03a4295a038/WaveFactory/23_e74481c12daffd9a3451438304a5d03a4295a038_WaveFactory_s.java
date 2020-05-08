 package se.chalmers.tda367.std.factories;
 
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import se.chalmers.tda367.std.core.Wave;
 import se.chalmers.tda367.std.core.WaveItem;
import se.chalmers.tda367.std.core.tiles.enemies.BasicEnemy;
 
 /**
  * A wave factory class. 
  * @author Unchanged
  * @date Mar 22, 2012
  */
 public class WaveFactory implements IFactory<Wave> {
 
 	/**
 	 * Creates a wave from the specified parameter(s).
 	 */
 	@Override
 	public Wave create(Object param) {
 		// TODO Auto-generated method stub
 		int n = 5;
 		ConcurrentLinkedQueue<WaveItem> q = new ConcurrentLinkedQueue<WaveItem>();
 		for(int i = 0; i<n; i++){
 			q.add(new WaveItem(new BasicEnemy(), i*1000));
 		}
 		return new Wave(q);
 	}
 }
