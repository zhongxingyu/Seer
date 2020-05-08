 package edu.nouri.photoReX;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat; 
 import java.util.*; 
 import redis.clients.jedis.*; 
 import redis.clients.jedis.exceptions.*; 
 import edu.nouri.photoReX.recommender.*; 
 
 
 
 
 
 public class Server implements LearnerDelegate {
 
 	private JedisPool redisPool; 						//this is the redis client that receives recommendation publishes
 	private Learner learner; 
 	
 	public static void main(String[] args) {
 		
 		Server server = new Server(); 
 		server.run();  
 	}
 
 	public Server()
 	{
 		learner = new Learner(this); 
 		redisPool = new JedisPool("localhost");
 	}	
 	
 	//delete me
 	public void testRecommender()
 	{
 //		FHPFeaturedRecommender r = new FHPFeaturedRecommender("popular",this); 
 		
 //		r.recommend("alidoon", 12); 
 	}
 	
 	public void run()
 	{
 		System.out.println("Learning server started..."); 
 		
 		while (true)
 		{
 			//if we already have too many jobs running at the same time, wait
 			if (learner.outstandingJobs.size() > 3)
 			{
 				try{
 					Thread.sleep(1000); 
 				}catch(Exception e)
 				{}
 			}
 			RecommendationTask task = getNextRecommendationTask(); 
 			learner.recommend(task);	//this is nonblocking
 		}
 		
 /*		
 		System.out.println("Learning server started..."); 
 				
 		long sleepTime = 1000; 		//how many miliseconds to wait before reconnecting to redis server
 		while(true)
 		{
 	
 			try{
 				RecommendationTask task = new RecommendationTask( redis.blpop(0, "users:recommend:queue")); 
 				System.out.print("recommendingg " + task.pageCount + " pics for '" + task.username + "' ... "); 
 				long start = System.currentTimeMillis();
 				ArrayList<RecommendationPage> recs = learner.recommend(task); 
 				
 				for(int i=0; i< recs.size(); i++)
 				{
 					long page = redis.incr("counter:user:" + task.username + ":page" ); 
 					RecommendationPage rec = recs.get(i); 
 					rec.pageid = page; 
 					
 					
 					//add info to various structures in redis: 
 					Pipeline p = redis.pipelined(); 
 					p.rpush("user:"+task.username + ":queue", rec.toJson()); 	//add json rep to the end of user queu
 	
 					//add structured data to redis: 
 					String userVisitedKey = "user:" + task.username + ":visited";
 					String pageKey        = "user:" + task.username + ":page:" + rec.pageid + ":pics"; 
 					for(int j=0; j< rec.pics.size(); j++)
 					{
 						String hash = rec.pics.get(j).picture.toHash(); 
 						p.sadd(userVisitedKey, hash);						//user visited this photo
 						
 						String picVisitedKey  = "pic:" +  hash + ":visited"; 
 						p.sadd(picVisitedKey, task.username); 				//picture was visited by this user
 						
 						p.rpush(pageKey, hash); 								//page contains this picture
 						p.set("pic:" + hash, rec.pics.get(j).picture.toJson()); //remember to call toHash before calling toJson (bad design really)
 					}
 					
 					p.sync(); 				//wait for commands to execute
 				}
 				long end = System.currentTimeMillis(); 
 				System.out.println("done in " + (end-start)/1000.0 + "seconds."); 
 				
 				sleepTime = 1000; //reset sleepTime because we were successful
 				//send pic to redis both as complete json to be passed to client and detailed structured for analysis 
 			}
 			catch(JedisConnectionException jce)
 			{
 				try
 				{
 					DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
 					Date date = new Date();
 					redis.disconnect(); 
 					System.out.println(dateFormat.format(date) + ": redis was closed. trying to reconnect in " + (int)(sleepTime/1000) + " seconds..."); 
 					Thread.sleep(sleepTime);
 					sleepTime = (long) Math.min(60000, sleepTime*1.5); 
 					redis.connect(); 
 				}
 				catch(Exception e)
 				{
 					//wait some time 
 //					Thread.sleep(10000); 
 				}
 			}
 		}
 */
 		}
 	
 	public RecommendationTask getNextRecommendationTask()
 	{
 		return getNextRecommendationTask(1000); 
 	}
 	
 	//blockingly retrieves the next task from redis 
 	public RecommendationTask getNextRecommendationTask(long sleepTime)
 	{
 		Jedis redis = redisPool.getResource(); 
 		try{
 			RecommendationTask task = new RecommendationTask( redis.blpop(0, "users:recommend:queue")); 
 			System.out.print("recommendingg " + task.pageCount + " pics for '" + task.username + "' ... "); 
 			return task; 
 		}
 		catch(JedisConnectionException jce)
 		{
 			try
 			{
 				DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
 				Date date = new Date();
 				redis.disconnect(); 
 				System.out.println(dateFormat.format(date) + ": redis was closed. trying to reconnect in " + (int)(sleepTime/1000) + " seconds..."); 
 				Thread.sleep(sleepTime);
 				sleepTime = (long) Math.min(60000, sleepTime*1.5); 
 				redis.connect(); 
				return getNextRecommendationTask(sleepTime); 
 			}
 			catch(Exception e)
 			{
 				return null; 
				//wait some time 
//				Thread.sleep(10000); 
 			}
		}finally{
			redisPool.returnResource(redis); 
 		}
 		
 	}
 	
 	//delegate method: 
 	//remember that this method is always called from other threads (not main thread)
 	public void recommendationDidComplete(Learner sender, RecommendationTask task)
 	{
 		ArrayList<RecommendationPage> recs = task.recomms; 
 		
 		Jedis redis = null;  
 		 try{
 			redis = redisPool.getResource(); 
 			for(int i=0; i< recs.size(); i++)	//for each page 
 			{
 				long page = redis.incr("counter:user:" + task.username + ":page" ); 
 				RecommendationPage rec = recs.get(i); 
 				rec.pageid = page; 
 				
 				
 				//add info to various structures in redis: 
 				Pipeline p = redis.pipelined(); 
 				p.rpush("user:"+task.username + ":queue", rec.toJson()); 	//add json rep to the end of user queu
 	
 				//add structured data to redis: 
 				String userVisitedKey = "user:" + task.username + ":visited";
 				String pageKey        = "user:" + task.username + ":page:" + rec.pageid + ":pics"; 
 				for(int j=0; j< rec.pics.size(); j++)
 				{
 					String hash = rec.pics.get(j).picture.toHash(); 
 					p.sadd(userVisitedKey, hash);						//user visited this photo
 					
 					String picVisitedKey  = "pic:" +  hash + ":visited"; 
 					p.sadd(picVisitedKey, task.username); 				//picture was visited by this user
 					
 					p.rpush(pageKey, hash); 								//page contains this picture
 					p.set("pic:" + hash, rec.pics.get(j).picture.toJson()); //remember to call toHash before calling toJson (bad design really)
 				}
 				
 				p.sync(); 				//wait for commands to execute
 			}
 			long end = System.currentTimeMillis(); 
 			System.out.println("done in " + (end-task.startTime)/1000.0 + "seconds."); 
 		}
 		catch(JedisConnectionException jce)
 		{
 			//TODO: cleanup here 
 		}
 		finally
 		{
 			redisPool.returnResource(redis); 
 		}
 	
 	}
 }
 	
 	
