 
 public class World {
 	double[][] positions;
 	double[][] matrix;
 	int nextIndex;
 	int size;
 	
 	public World(int size) {
 		positions = new double[size][2];
 		matrix = new double[size][size];
 		nextIndex = 1;
 		this.size = size;
 	}
 	
 	public void add(double x, double y) {
 		positions[nextIndex][0] = x;
 		positions[nextIndex][0] = x;
 		
 		for (int i = 0; i < nextIndex; i++) {
 			double dist = calculateDistance(i, nextIndex);
 			matrix[i][nextIndex] = dist;
 			matrix[nextIndex][i] = dist;
 		}
 		nextIndex++;
 	}
 	
 	private double calculateDistance(int p1, int p2) {
 		
 		return 0;
 	}
 	
 	public void printSolution(int[] answer) {
 		for (int i = 0; i < answer.length; i++) {
 			
 		}
 	}
 	
 	public void printWorldMatrix() {
 		for (int i = 0; i < nextIndex - 1; i++) {
 			for (int j = 0; j < nextIndex - 1; j++) {
 				System.out.println("[i][j] " + matrix[i][j]);
 			}
 		}
 	}
 	
	public int getNearestCity (int city, boolean[] visited){
 		double shortestDist = Double.MAX_VALUE;
 		int nearest = 0;
 		for (int i=0;i<size;i++){
 			if (i == city)
 				continue;
 			double dist = matrix[city][i];
			if (dist < shortestDist && !visited[i]){
 				shortestDist = dist;
 				nearest = i;
 			}
 		}
 		return nearest;
 	}
	
	public int getSize (){
		return size;
	}
 }
