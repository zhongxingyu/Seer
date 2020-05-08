 package knn;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.List;
 
 import database.RatingStore;
 
 import knn.NaiveKNN.DistanceTable;
 
 public class NaiveKNNTest extends junit.framework.TestCase {
 	
 	public void testNearestNeighbors() {
 		NaiveKNN knn = new NaiveKNN();
 		DistanceTable distances = new DistanceTable();
 		
 		int idLimit = 6;
 		knn.setMovieIDLimit(idLimit);
 		
 		for(int i = 1; i < idLimit; i++) {
 			for(int j = 1; j < idLimit; j++) {
 				if(i == j){
 					distances.put(i, i, 0);
 				}
 				else {
 					distances.put(i, j, Double.MAX_VALUE);
 				}
 			}
 		}
 		
 		distances.put(1, 5, 1.0);
 		distances.put(1, 2, 2.0);
 		
 		List<Neighbor> nearest = knn.nearestNeighbors(2, 1, distances);
 		
 		Neighbor first = nearest.get(0);
 		Neighbor second = nearest.get(1);
 		
 		assertEquals(5, first.id);
 		assertEquals(1.0, first.distance);
 		assertEquals(2, second.id);
 		assertEquals(2.0, second.distance);
 		
 	}
 	
 	public void testLoadData() throws Exception {
 		File indexFile = new File("/tmp/test.index");
 		RatingStore database = new RatingStore(indexFile);
 		database.createFromFile(new File("data"));
 		
 		NaiveKNN knn = new NaiveKNN(indexFile);
 		List<Neighbor> neighbors = knn.nearestNeighbors(5, 1);
 		
 		for(int i=0; i < 5; i++)
 			System.out.println(neighbors.get(i));
 	}
 	
 	public void testFakeData() throws Exception {
 		File indexFile = new File("/tmp/test.index");
 		RatingStore database = new RatingStore(indexFile);
 		database.createFromFile(new File("fake_data"));
 		
 		NaiveKNN knn = new NaiveKNN(indexFile);
 		List<Neighbor> neighbors = knn.nearestNeighbors(2, 1);
 		
 		assertEquals(1.0, knn.distanceTable.get(1, 2));
 		assertEquals(1.0, knn.distanceTable.get(2, 1));
 		assertEquals(4.0, knn.distanceTable.get(1, 3));
 		assertEquals(1.0, knn.distanceTable.get(2, 3));
 	}
 	
 	public void testFullData() throws Exception {
 		File indexFile = new File("/tmp/test.index");
 		RatingStore database = new RatingStore(indexFile);
		database.createFromFile(new File("training_se"));
 	}
 }
