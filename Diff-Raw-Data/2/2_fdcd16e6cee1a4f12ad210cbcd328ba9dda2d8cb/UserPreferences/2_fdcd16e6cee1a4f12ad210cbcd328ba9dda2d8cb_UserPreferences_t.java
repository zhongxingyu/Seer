 package DataMining;
 
 import java.net.UnknownHostException;
 import java.util.Arrays;
 import com.mongodb.Mongo;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBObject;
 import com.mongodb.DBCursor;
 import com.mongodb.MongoException;
 
 public class UserPreferences {
 
 	private int userId;
 	private int[] itemIds;
 	private double[] ratings;
 
 	public UserPreferences(int id) {
 		this.userId = id;
 		this.itemIds = new int[0];
 		this.ratings = new double[0];
 	}
 
 	public void addRating(int newId, Double newRating) {
 			// zoek de eerstvolgende logische locatie X
 			int newIndex = Arrays.binarySearch(itemIds, newId);
 						
 			if (itemIds.length == 0){					// De eerste keer maken we gewoon een nieuwe array aan
 				itemIds = new int[]{newId};
 				ratings = new double[]{newRating};
 			} else if (newIndex >= 0){ 					// Als de itemId al bestaat slaan we alleen de nieuwe rating op
 				ratings[newIndex] = newRating;
 			} else {									// Anders wordt een nieuw item tussen gevoegd
 				
 				newIndex *= -1;
 				newIndex -= 1;
 								
				// Kopien met extra ruimte
 				int[] newItemIds = Arrays.copyOf(itemIds, itemIds.length+1);
 				double[] newRatings = Arrays.copyOf(ratings, ratings.length+1);
 				
 				if (newIndex+1 < newItemIds.length){	// Opschuiven, tenzij de nieuwe op het einde moet
 					System.arraycopy(itemIds, newIndex, newItemIds, newIndex+1, newItemIds.length-newIndex-1);
 					System.arraycopy(ratings, newIndex, newRatings, newIndex+1, newRatings.length-newIndex-1);
 				}
 				
 				// Beschikbaar gekomen ruimte vullen
 				newItemIds[newIndex] = newId;
 				newRatings[newIndex] = newRating;
 				
 				// nieuwe lijsten terugplaatsen
 				itemIds = newItemIds;
 				ratings = newRatings;
 			}
 
 	}
 	
 	public int[] getItemIds(){
 		return itemIds;
 	}
 	
 	public int getUserId(){
 		return userId;
 	}
 	
 	public double getRating(int itemId){
 		int index = Arrays.binarySearch(itemIds, itemId);
 		if (index < 0) return index;
 		return ratings[index];
 	}
 	
 	@Override
 	public String toString(){
 		String result = "Ratings for user "+userId+"\n";
 		result += "--------------------\n";
 		for (int i=0; i<itemIds.length; i++){
 			result += itemIds[i] +" - "+ ratings[i] +"\n";
 		}
 		result += "\n";
 		
 		return result;
 	}
 
 }
