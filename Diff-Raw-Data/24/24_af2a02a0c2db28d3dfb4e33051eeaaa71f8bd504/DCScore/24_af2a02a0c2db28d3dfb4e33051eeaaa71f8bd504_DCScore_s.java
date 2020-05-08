 package util.depthcharger;
 
 public class DCScore {
 	private int reached;
 	private double total;
	private int depth;
 	public DCScore(){
		this(0);
	}
	public DCScore(int d) {
		depth = d;
 		reached = 0;
		depth = 0;
 	}
 	public double value() {
 		if(total == 0)
 			return 0.5; // I dunno man, whatevs
		return reached/total;
 	}
 	public void addScore(double s) {
 		reached ++;
 		total += s;
 	}
 }
