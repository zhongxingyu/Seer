 package edu.wm.service;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.UUID;
  
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Bean;
 import org.springframework.data.mongodb.MongoDbFactory;
 import org.springframework.data.mongodb.core.MongoOperations;
 import org.springframework.data.mongodb.core.MongoTemplate;
 import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
 import org.springframework.data.mongodb.core.geo.GeoResult;
 import org.springframework.data.mongodb.core.geo.GeoResults;
 import org.springframework.data.mongodb.core.geo.Metrics;
 import org.springframework.data.mongodb.core.query.BasicQuery;
 import org.springframework.data.mongodb.core.query.Criteria;
 import org.springframework.data.mongodb.core.query.NearQuery;
 import org.springframework.data.mongodb.core.query.Query;
 import org.springframework.data.mongodb.core.query.Update;
 import org.springframework.stereotype.Repository;
 
 import Exceptions.NoPlayerFoundException;
 import Exceptions.NoPlayersException;
 
 import com.mongodb.DB;
 import com.mongodb.MongoClient;
 
 import edu.wm.something.domain.GPSLocation;
 import edu.wm.something.domain.Player;
  
  
 @Repository
 public class PlayerService {
      
     @Autowired
     private static MongoTemplate mongoTemplate;
      
     public static final String COLLECTION_NAME = "Players";  
     public static final GPSLocation gpsLocation = new GPSLocation();
     public static final double maxKillDistance = 0.005;//26.4 feet (1/150th of a mile)
 
      
     public void addplayer(Player player) {
         if (!mongoTemplate.collectionExists(Player.class)) {
             mongoTemplate.createCollection(Player.class);
         }       
         player.setId(UUID.randomUUID().toString());
         mongoTemplate.insert(player, COLLECTION_NAME);
     }
      
     public static List<Player> getAllPlayers() throws NoPlayersException{
         List<Player> resultsList = mongoTemplate.findAll(Player.class, COLLECTION_NAME);
         if (resultsList.size()== 0){
         	throw new NoPlayersException();
         }
         else{
         	return resultsList;
         }
     }
      
     public void deletePlayer(Player player) throws NoPlayerFoundException {
         try {
         	mongoTemplate.remove(player, COLLECTION_NAME);
         }
         catch (Exception e){
         	throw new NoPlayerFoundException(player.getId());
         }
     }
      
     public void updateplayer(Player player) throws NoPlayerFoundException {
         try {
         	mongoTemplate.insert(player, COLLECTION_NAME);
         }
         catch (Exception e){
         	throw new NoPlayerFoundException(player.getId());
         }
     }
     
     
     public Player getPlayerFromDbByID(Integer ownerId){
     	List<Player> listOfPlayers = mongoTemplate.findAll(Player.class, COLLECTION_NAME);
     	int length = listOfPlayers.size();
     	for(int i=0; i<length; i++){
     		Player p = listOfPlayers.get(i);
     		if (p.getId().equals(ownerId.toString())){
     			return p;
     		}
     	}
     	//If the player isn't there
     	return null;
     }
 
 	public List<Player> getAllNear(long lat, long lng) {
 		List<Player> listOfPlayers = mongoTemplate.findAll(Player.class, COLLECTION_NAME);
 		List<Player> listOfPlayersNear = (List<Player>) new ArrayList<Player>();
 		int length = listOfPlayers.size();
 		for (int i=0;i<length;i++){
 			Player player = listOfPlayers.get(i);
 			double playerLat = player.getLat();
 			double playerLng = player.getLng();
 			double distance = gpsLocation.distance(lat, lng, playerLat, playerLng);
 			if (distance < maxKillDistance){
 				listOfPlayersNear.add(player);
 			}
 		}
 		return listOfPlayersNear;
 		
 	}
 	
 	public void voteOnPlayer(Player p){
 		Query query = new Query();
 		query.addCriteria(Criteria.where("_id").is(p.getId()));
 		int voteCount = (p.getVoteCount()+1);
 		Update update = new Update();
 		update.set("voteCount", voteCount);
		mongoTemplate.findOne(query, Player.class);
 	}
     
     
 
     
 }
