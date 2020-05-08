 package com.algorithms.coverage.random;
 
import java.util.Random;

 import com.algorithms.coverage.SingleRoundReader;
 import com.algorithms.coverage.WriteMessage;
 import com.simulator.SimSystem;
 
 public class RandomReader extends SingleRoundReader {
 
	public double rand;
	private static final boolean D = true;
	
 	public RandomReader(SimSystem sim, int id) {
 		super(sim, id);
		rand = new Random().nextDouble();
		
		if (D) { 
			log.printf("Tag %d is set with rand val: %f \n", 
					id, rand);
		}
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	protected WriteMessage getWriteMessage() {
		RandomWriteMessage msg = new RandomWriteMessage(this.id, this.rand);
 		return msg;
 	}
 
 	
 }
