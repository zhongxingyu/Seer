 package net.esiade.client;
 
 import com.google.gwt.user.client.Random;
 
 public class Poisson {
 	private int lambda;
 	
	public Poisson(int lambda){
 		this.lambda = lambda;
 	}
 	
 	public int getNumber() {
 		int k=0;
 		double p=1;
 		double L = Math.pow(Math.E, -lambda);
 		while (p > L){
 		         k += 1;
 		         p = p*Random.nextDouble();
 		}
 		return k;
 	}
 	
 	public Vector2D getVector(){
		return new Vector2D(getNumber(), getNumber());	
 	}
 }
