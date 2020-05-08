 package clustering;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 public class ClusteringDriver {
 	static Centroid[] centroids = new Centroid[2];
 	static Random gen = new Random();
 	
 	public static void main(String[] args) {
 		Point[] data = {
 				new Point(3, 2),			//Cluster 1
 				new Point(5, 3.54),
 				new Point(3.89, 4.034),
 				new Point(2.475, 6.198),
 				new Point(4.93, 6.0),
 				new Point(6.132, 5.8974),
 				new Point(4.768, 7.94),		//END Cluster 1
 				
				new Point(7, 2.52),			//Cluster 2
				new Point(4.112, 4.941),
				new Point(5.978, 5.0),
				new Point(7, 5.785),
				new Point(2.967, 7.789),
				new Point(7.17, 10)			//END Cluster 2
 		};
 		
 		
 		
 		double maxX = getMaxX(data);
 		double maxY = getMaxY(data);
 		
 		
 		centroids[0] = new Centroid(gen.nextInt((int) (maxX*2))- maxX, gen.nextInt((int) (maxY*2))- maxY);
 		centroids[1] = new Centroid(gen.nextInt((int) (maxX*2))- maxX, gen.nextInt((int) (maxY*2))- maxY);
 		
 		int index = 0;
 		
 		while(hasCentroidMoved()) {
 			if(index > data.length - 1)
 				index = 0;
 			
 			Centroid c = findClosestCentroid(data[index]);
 			c.addPoint(data[index]);
 			data[index].setVisited(true);
 			index++;
 		}
 		
 		System.out.println(centroids[0].x + " " + centroids[0].getY());
 		System.out.println(centroids[1].x + " " + centroids[1].getY());
 	}
 	
 	private static boolean hasCentroidMoved() {
 		boolean moved = false;
 		
 		for(Centroid c : centroids){
 			if(c.hasMoved()) {
 				moved = true;
 				break;
 			}
 		}
 		
 		return moved;
 	}
 	
 	private static Centroid findClosestCentroid(Point p) {
 		ArrayList<Centroid> shortest = new ArrayList<>();
 		double shortestDistance = Double.MAX_VALUE;
 		
 		for(Centroid c : centroids) {
 			double distance = c.getDistance(p);
 			
 			if(distance < shortestDistance){
 				shortest.clear();
 				shortest.add(c);
 				shortestDistance = distance;
 			}else if(distance == shortestDistance){
 				shortest.add(c);
 			}
 		}
 		
 		Centroid toReturn;
 		if(shortest.size() > 1){
 			toReturn = shortest.get(gen.nextInt(shortest.size()));
 		} else toReturn = shortest.get(0);
 		
 		return toReturn;
 	}
 	
 	private static double getMaxX(Point[] a){
 		double max = Double.MIN_VALUE;
 		
 		for(Point p : a) {
 			double pXVal = p.getX();
 			if(pXVal > max)
 				max = pXVal;
 		}
 		
 		return max;
 	}
 	
 	private static double getMaxY(Point[] a) {
 		double max = Double.MIN_VALUE;
 		
 		for(Point p : a) {
 			double pYVal = p.getY();
 			if(pYVal > max)
 				max = pYVal;
 		}
 		
 		return max;
 	}
 	
 	private static double getMinX(Point[] a) {
 		double min = Double.MAX_VALUE;
 		
 		for(Point p : a) {
 			double pXVal = p.getX();
 			if(pXVal < min)
 				min = pXVal;
 		}
 		
 		return min;
 	}
 	
 	private static double getMinY(Point[] a) {
 		double min = Double.MAX_VALUE;
 		
 		for(Point p : a) {
 			double pYVal = p.getY();
 			if(pYVal < min)
 				min = pYVal;
 		}
 		
 		return min;
 	}
 }
