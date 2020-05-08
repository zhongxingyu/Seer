 package net.esiade.client;
 
 import com.google.gwt.user.client.Random;
 
 public class Poisson {
 	private int lambda;
	private Vector2D position;
 	
	public Poisson(int lambda, Vector2D position){
 		this.lambda = lambda;
		this.position = position;
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
		return new Vector2D(position.x + getNumber(), position.y + getNumber());	
	}

	public void changePosition(Vector2D move){
		position.add(move);
	}
	
	public void setPosition(Vector2D newPosition){
		position = newPosition;
	}

	public void setLambda(int newLambda){
		lambda = newLambda;
	}
	
	public int getLambda(){
		return lambda;
 	}
 }
