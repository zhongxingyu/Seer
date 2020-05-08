 package edu.nouri.photoReX.crawler;
 
 import java.util.ArrayList;
 
 import edu.nouri.photoReX.picture.PictureInfo;
 import redis.clients.jedis.Jedis;
 import redis.clients.jedis.Pipeline;
 import redis.clients.jedis.exceptions.JedisConnectionException;
 
 
 /*
  *  This abstract class represents crawlers that fetch photos in batch on a regular basis from various websites. 
  */
 
 
 public abstract class Crawler {
 
 	protected Jedis redis; 
	public String redisSetName; 	//this is the name of the set in redis that will contain the picture set (each crawler has its own)
 	
 	public Crawler()
 	{
 		redis = new Jedis("localhost"); 
 		redisSetName = ""; 	//this should be filled by each crawler 
 	}
 	
 
 	protected int putInRedis(ArrayList<PictureInfo> pics)
 	{
 		if (pics == null)
 			return -1; 
 
 		if (redisSetName.length()==0)
 			return -3; 
 		
 		try{
 			Pipeline r = redis.pipelined(); 
 			for(PictureInfo pic: pics)
 			{
 				//add pic info to redis
 				String key = "pic:" + pic.hash ; 
 				r.set(key, pic.toJson()); 
 				
 				//add pic hash to the corresponding set in redis 
 				r.sadd(redisSetName, pic.hash);
 				
 			}
 			r.sync(); 				//wait for commands to execute
 		}catch(JedisConnectionException jce)
 		{
 			return -2; //redis error 
 		}
 
 		return 0; //success
 	}
 	
 	
 	
 	
 	/* fetches photos from the web and puts them into the redis structure. Returns completion code: 
 	 * 0 means no error.
 	 * 
 	 */
 	public int fetch()
 	{
 		ArrayList<PictureInfo> pics = getFromWebsite(); 
 		
 		return putInRedis(pics); 
 	}
 	
 	
 	
 	public abstract ArrayList<PictureInfo> getFromWebsite(); 
 	
 	
 }
