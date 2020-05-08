 package com.algorithms.coverage.random;
 
 import java.util.Random;
 
 import com.algorithms.coverage.WriteMessage;
 
 public class RandomWriteMessage extends WriteMessage {
 
 	public int id; 
 	public double rand;
 	
 	
	public RandomWriteMessage(int id, double rand) {
 		super();
 		this.id = id;
		this.rand = rand;
 	}
 
 	
 	@Override
 	public String toString() {
 		return "RandomWriteMessage [id=" + id + ", rand=" + rand + "]";
 	} 
 	
 	
 	
 	
 }
