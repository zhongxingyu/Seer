 package edu.nus.tp.engine.utils;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import edu.nus.tp.engine.BayesClassifier;
 import edu.nus.tp.engine.saver.RedisPersistence;
 import edu.nus.tp.web.tweet.ClassifiedTweet;
 import static edu.nus.tp.engine.utils.Constants.*;
 
 public class RedisTrainUtil {
 
	public static void populateRedis() throws InterruptedException{
 		
 		List<ClassifiedTweet> preLearnedTweets = new ArrayList<ClassifiedTweet>();
 		
 		BufferedReader br = null;
 		try
 		{
 			String sCurrentLine;
 			
 			/*br = new BufferedReader(new FileReader(TEMP_FILES+"Positive.txt"));
 			while ((sCurrentLine = br.readLine()) != null) {
 				preLearnedTweets.add(new ClassifiedTweet(sCurrentLine, Category.POSITIVE, ""));		
 			}*/
 			
 			br = new BufferedReader(new FileReader(TEMP_FILES+"Negative.txt"));
 			int count=0;
 			while ((sCurrentLine = br.readLine()) != null) {
 				count++;
 				if (count%1000==0){
 					System.out.println("Current processed tweet count is : "+count);
 				}
 				preLearnedTweets.add(new ClassifiedTweet(sCurrentLine, Category.NEGATIVE, ""));		
 			}
 			
 			/*br = new BufferedReader(new FileReader(TEMP_FILES+"Neutral.txt"));
 			while ((sCurrentLine = br.readLine()) != null) {
 				preLearnedTweets.add(new ClassifiedTweet(sCurrentLine, Category.NEUTRAL, ""));		
 			}*/
  
 		} catch (IOException e) {
 			e.printStackTrace();
 		} 
 		
 				
 		BayesClassifier classifier=new BayesClassifier(new RedisPersistence());
 		classifier.train(preLearnedTweets);
 		
 	}
 	
 	
	public static void main(String[] args) throws InterruptedException {
 		
 		populateRedis();
 		
 	}
 	
 	
 }
