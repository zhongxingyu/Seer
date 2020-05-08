 package util.depthcharger;
 
 public class DCScore {
 	private int reached;
 	private double total;
 	public DCScore(){
 		reached = 0;
		total = 0;
 	}
 	public double value() {
 		if(total == 0)
 			return 0.5; // I dunno man, whatevs
		return reached/total/100.;
 	}
 	public void addScore(double s) {
 		reached ++;
 		total += s;
 	}
 }
