 package thesaurus.spring;
  import java.awt.geom.Point2D;
 //import java.util.LinkedList;
 //import java.util.List;
 import java.util.ArrayList;
 
 public class FrSpring {
 	private int length;
 	private int width;
 	private int area;
 	private int size;
 	private double k;
 	private double temprature;
 	// private Dimension d;
 	private double[][] dis;
 	private ArrayList<Point2D> pos;
 	private double EPSILON = 0.000001D;
 
 	
 
 	public FrSpring(int len, int wid, int syn) {
 		this.length = len;
 		this.width = wid;
 		this.temprature = ((double) this.width / 10);
 		this.area = len * wid;
 		this.size = syn;
 		this.dis = new double[size][2];
 		this.pos = new ArrayList<Point2D>(size);
 
 		for (int i = 0; i < this.size; i++) {
 			if (i == 0) {
 				
 				pos.add(0,
 						create((double) this.length / 2.0,
 								((double) this.width) / 2.0));
 				continue;													                 	// this is the center of the canvas
 			}
 			
 			double myX = Math.random() * this.width;    
 			double myY = Math.random() * this.length;							//place vertices at random
 			pos.add(i, create(myX, myY));
 			dis[i][0] = 0; dis[i][1] = 0;										//initialize displacement of every vertex to 0
 
 			k = Math.sqrt(((double) this.area / (double) this.size)); k *= 0.75; // compute optimal pairwise distance
 		}
 		mySpring();
 	}
 
 	private void mySpring() {
 		for (int ite = 0; ite < 100; ite++) {
 			for (int i = 0; i < this.size; i++) {
 				dis[i][0]=0; dis[i][1]=0;
 				for (int j = 1; j < this.size; j++) {
 					if (i != j) {
 
 						double disX = pos.get(i).getX() - pos.get(j).getX();   // difference of x coordinate
 						double disY = pos.get(i).getY() - pos.get(j).getY();   // difference of y coordinate
 						double deltaLength = Math.max(EPSILON, pos.get(i)	   // if distance between vertices is zero, since  
 								.distanceSq(pos.get(j)));				       // couldn't divided by zero use EPSILON  
 												  
 						double rforce = repulsionF(Math.abs(deltaLength));     // repulsion force (distance)
 
 						dis[i][0] = (dis[i][0] + (disX * rforce));             // displacement of x
 						dis[i][1] = (dis[i][1] + (disY * rforce));			  //  displacement of y
 
 					}
 				}
 			} // this is the end of the first inner loop
 			for (int i = 1; i < this.size; i++) { // the edges of the graph
 				double disX = pos.get(0).getX() - pos.get(i).getX();
 				double disY = pos.get(0).getY() - pos.get(i).getY();
 
 				double deltaLength = Math.max(EPSILON,
 						pos.get(0).distanceSq(pos.get(i)));
 				double aforce = attractionF(Math.abs(deltaLength));          //compute attraction force
 
 				dis[i][0] = (dis[i][0] + (disX * aforce));					// displacement  edge x coordinate
 				dis[i][1] = (dis[i][1] + (disY * aforce));					// displacement edge y coordinate
 			}
 
 			for (int j = 1; j < this.size; j++) {
 								double newXDisp = dis[j][0] / Math.abs(dis[j][0])           //use temperature to scale x
 						* Math.min(Math.abs(dis[j][0]), temprature);
 
 				double newYDisp = dis[j][1] / Math.abs(dis[j][1])							// use temperature to scale y
 						* Math.min(Math.abs(dis[j][1]), temprature);
 
 				double newX = pos.get(j).getX() + newXDisp;					// adjust position  using displacement scaled by temperature
 				double newY = pos.get(j).getY() + newYDisp;
 
 				newX = Math.max(0, Math.min(newX, width));					// limit max displacement to frame
 				newY = Math.max(0, Math.min(newY, length));
 				pos.get(j).setLocation(newX, newY); 
 
 			}
 			temprature *= (1.0 - ite / (double) 100); // reduce temperature
 		}
 		/* the is for test only, Begin testing */
 		double[][] tmp = new double[size][2];
 		for (int i = 0; i < this.size; i++) {
 				tmp[i][0]= pos.get(i).getX();
 				tmp[i][1] = pos.get(i).getY();
 				
 		}
 		int count = 0;
 		for (int i = 0; i < this.size; i++) {
 			for (int j = 0; j < this.size; j++){
 				if (i!=j){
 					if(tmp[i][0] == tmp[j][0]  && tmp[i][1] == tmp[j][1]){
 						count++;
 					}
 				}
 			}
 			
 		}
 		System.out.println("Replicated coordinates =" +count);
 		/* end of the test. */
 		
 	}
 	
 	/* calculates repulsion force between non-adjacent vertices x is a distance calculated by pythagoras   */
 	private double repulsionF(double x) {
 		
 		return ((k * k) / x);
 	}
 	
 	/* calculates attraction force between edges y is length of the edge*/
 	private double attractionF(double y) {
 		return ((y * y) / k);
 	}
 	
      /* create and returns coordinate points */
 	private Point2D create(double x, double y) {
 		return new Point2D.Double(x, y);
 	}
 
 	public int getSize() {
 		return size;
 	}
 
 	
 
	public ArrayList<Point2D> getcoordinate() {
 		return pos;
 	}
 
 	
 }
