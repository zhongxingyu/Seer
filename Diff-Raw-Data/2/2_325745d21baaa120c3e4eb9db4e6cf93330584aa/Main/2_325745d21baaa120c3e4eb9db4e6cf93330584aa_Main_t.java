 package edu.ntnu.idi;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import org.apache.mahout.cf.taste.common.TasteException;
 import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
 import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
 import org.apache.mahout.cf.taste.impl.recommender.CachingRecommender;
 import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
 import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
 import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
 import org.apache.mahout.cf.taste.recommender.RecommendedItem;
 import org.apache.mahout.cf.taste.recommender.Recommender;
 import org.apache.mahout.cf.taste.similarity.UserSimilarity;
 
 public class Main {
 
 	/**
 	 * @param args
 	 * @throws IOException 
 	 * @throws TasteException 
 	 */
 	public static void main(String[] args) throws IOException, TasteException {
 		// TODO Auto-generated method stub
 		
		FileDataModel movielens = new FileDataModel(new File("data/movielens-1m/ratings.dat"));
 		System.out.println("koko");
 		
 		UserSimilarity userSimilarity = new PearsonCorrelationSimilarity(movielens);
 		
 		UserNeighborhood neighborhood = new NearestNUserNeighborhood(3, userSimilarity, movielens);
 		
 		Recommender recommender = new GenericUserBasedRecommender(movielens, neighborhood, userSimilarity);
 		Recommender cachingRecommender = new CachingRecommender(recommender);
 		
 		List<RecommendedItem> recommendations = cachingRecommender.recommend(1, 10);
 		
 		for (RecommendedItem recommendedItem : recommendations) {
 			System.out.print(recommendedItem.getItemID());
 			System.out.print(" : ");
 			System.out.println(recommendedItem.getValue());
 			
 		}
 	}
 }
