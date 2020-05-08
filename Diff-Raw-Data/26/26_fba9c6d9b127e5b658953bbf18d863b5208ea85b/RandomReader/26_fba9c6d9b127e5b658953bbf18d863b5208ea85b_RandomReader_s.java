 package com.algorithms.coverage.random;
 
 import com.algorithms.coverage.SingleRoundReader;
 import com.algorithms.coverage.WriteMessage;
 import com.simulator.SimSystem;
 
 public class RandomReader extends SingleRoundReader {
 
 	public RandomReader(SimSystem sim, int id) {
 		super(sim, id);
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	protected WriteMessage getWriteMessage() {
		RandomWriteMessage msg = new RandomWriteMessage(this.id);
 		return msg;
 	}
 
 	
 }
