 package thesaurus.spring;
 import java.awt.geom.Point2D;
 //import java.util.LinkedList;
 //import java.util.List;
 //import java.util.ArrayList;
 import java.util.LinkedList;
 //import thesaurus.parser.*;
 public class FrSpring2 {
 	private int length;
 	private int width;
 	private int area;
 	private int size;
 	private double k;
 	private double temprature;
 	private LinkedList<Vertex> lstVertices  ;
 	private double EPSILON = 0.000001D;
 	private Vertex myWord;
 	private int layerIndex = 1;
 
 
 	public FrSpring2(Vertex myVer) {
 		this.length = 900;
 		this.width = 1200;
 		this.temprature = ((double) this.width / 10);
 		this.area = (this.width) * (this.length);
 		this.myWord = myVer;
 		lstVertices = new LinkedList<Vertex>();
 		layerIndex = getVertex(this.myWord);			           	//add the vertices into arraylist
 		this.size =  this.lstVertices.size();
 		
 
 		for (int i = 0; i < this.size; i++) {
 			if (i == 0) {
 
 				lstVertices.get(i).setPos(create((double) (this.width / 2.0),((double) this.length / 3.0)));
 				lstVertices.get(i).setPDis(create(0, 0));
 				continue;													                 	// this is the center of the canvas
 			}
 
 			double myX = Math.random() * this.width;    
 			double myY = Math.random() * this.length;							
 			lstVertices.get(i).setPos (create(myX, myY));    				//place vertices at random
 			lstVertices.get(i).setPDis(create(0, 0));						//initialize displacement of every vertex to 0
 
 			k = Math.sqrt(((double) this.area / (double) this.size)); k*=200; // compute optimal pairwise distance
 		}
 		mySpring();
 	}
 
 
 
 	private void mySpring() {
 		for (int ite = 0; ite < 10000; ite++) {
 			for (Vertex v : this.lstVertices) {
 				v.getDis().setLocation(0, 0);
 				for (Vertex u : this.lstVertices) {
 					if (!(u.equals(v))) {
 
 						double disX = v.getPos().getX() - u.getPos().getX();   // difference of x coordinate
 						double disY = v.getPos().getY() - u.getPos().getY();   // difference of y coordinate
 						double deltaLength = Math.max(EPSILON,  v.getPos()	   // if distance between vertices is zero, since  
 								.distanceSq(u.getPos()));				       // couldn't divided by zero use EPSILON  
 						double rforce = repulsionF(Math.abs(deltaLength));     // repulsion force (distance)
 						assert Double.isNaN(rforce) == false : "Unexpected mathematical result in FRSpring Layout:Spring [Repulsion force]";
 						
 						disX = (v.getDis().getX() + (disX * rforce));             // displacement of x
 						disY = (v.getDis().getY() + (disY * rforce));			  //  displacement of y
 						v.getDis().setLocation(disX,disY);
 					}
 				}
 			} // this is the end of the the loop that repulse every every vertex
 			for (int i = 0; i < this.size; i++) { // the edges of the graph
 				Vertex source = this.lstVertices.get(i);
 				if (source.getPos() == null) continue;
 				for (Vertex target : source.getAdjList()){
 					if (target.equals(lstVertices.get(i)))continue;
 					if (target.getPos() == null) continue;
 					double disX = source.getPos().getX() - target.getPos().getX();
 					double disY = source.getPos().getY() - target.getPos().getY();
 									
 					double deltaLength = Math.max(EPSILON,
 							source.getPos().distanceSq(target.getPos()));
 					
 					double aforce = attractionF(Math.abs(deltaLength));          //compute attraction force
 					assert Double.isNaN(aforce) == false : "Unexpected mathematical result in FRSpring Layout:Spring[Attraction force]";
 					
 					double myAForce = 1;
 					if (i==0)  myAForce = aforce*50000;
 					if (i != 0) myAForce = aforce *25000;
 				//if(!(source.equals(lstVertices.get(0)))){
 						double sdisX = (source.getDis().getX() - (disX * myAForce));					// displacement  edge x coordinate
 						double sdisY = (source.getDis().getY() - (disY * myAForce));					// displacement edge y coordinate
 						source.getDis().setLocation(sdisX, sdisY);//}
 
 					double tdisX = (target.getDis().getX() + (disX * myAForce));					// displacement  edge x coordinate
 					double tdisY = (target.getDis().getY() + (disY * myAForce));	
 					target.getDis().setLocation(tdisX, tdisY);
 				}
 			}
 
 			for (int j = 1; j < this.size; j++) {
 

 				double deltaforce = Math.sqrt((this.lstVertices.get(j).getDis().getX()*this.lstVertices.get(j).getDis().getX())
 									+(this.lstVertices.get(j).getDis().getY()*this.lstVertices.get(j).getDis().getY()));
 				
				double myDelta = Math.max(deltaforce, EPSILON);
				double newXDisp = (this.lstVertices.get(j).getDis().getX() / myDelta)          //use temperature to scale x
 									* Math.min(deltaforce, temprature);
				assert Double.isNaN(newXDisp) == false : "Unexpected mathematical result in FRSpring Layout:Spring [newXDisp]";
 				
 				
				double newYDisp = (this.lstVertices.get(j).getDis().getY() / myDelta)          //use temperature to scale x
				* Math.min(deltaforce, temprature);
				assert Double.isNaN(newYDisp) == false : "Unexpected mathematical result in FRSpring Layout:Spring [newYDisp]";
				
 				double newX = this.lstVertices.get(j).getPos().getX() + Math.max(-100, Math.min(100,newXDisp));				// adjust position  using displacement scaled by temperature
 				double newY = this.lstVertices.get(j).getPos().getY() + Math.max(-100, Math.min(100,newYDisp));
 
 				newX = Math.max(0, Math.min(newX, width));					// limit max displacement to frame
 				newY = Math.max(0, Math.min(newY, length));
 				this.lstVertices.get(j).getPos().setLocation(newX, newY); 
 
 			}
 			temprature *= (1.0 - ite / (double) 10000); // reduce temperature
 		}
 		/* the is for test only, Begin testing */
 		double[][] tmp = new double[size][2];
 		for (int i = 0; i < this.size; i++) {
 			tmp[i][0]= this.lstVertices.get(i).getPos().getX();
 			tmp[i][1] = this.lstVertices.get(i).getPos().getY();
 			System.out.println(tmp[i][0]+ "   "+tmp[i][1]);
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
 
 		return (((k * k) / x)*1000);
 	}
 
 	/* calculates attraction force between edges y is length of the edge*/
 	private double attractionF(double y) {
 		return (((y * y) / (k)));
 	}
 
 	/* create and returns coordinate points */
 	private Point2D create(double x, double y) {
 		return new Point2D.Double(x, y);
 	}
 
 	/* this method adds all vertices into lsVisited which is used for repulsion of vertices 
 	 * and also helps to identify the actual number of vertices (size)*/
 	private int getVertex(Vertex word) {
 		int count = 1;
 		this.lstVertices.addFirst(word);
 		for (Vertex ver : word.getAdjList()){
 			if (ver.isVisited())continue;
 			this.lstVertices.add(ver); ver.setVisited(true);count++;
 			for (Vertex inVer : ver.getAdjList()){
 				if (inVer.isVisited()) continue;
 				this.lstVertices.add(inVer);inVer.setVisited(true);
 			}
 		}
 		return count;
 	}
 
 
 
 	public Vertex getCoordinates() {
 		return myWord;
 	}
 public LinkedList<Vertex> getVertices(){
 	return this.lstVertices;
 }
 
 }
 
 
