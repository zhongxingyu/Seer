 package danger_zone;
 import java.util.HashMap;
 import java.io.*;
 
 /**
 *@author Ethan Eldridge <ejayeldridge @ gmail.com>
 *@version 0.0
 *@since 2012-10-17
 *
 * Naive Bayes classifier to classify a tweet as a danger or not. 
 */
 public class NaiveBayes{
 	/**
 	*Constant for the category danger
 	*/
 	public static final int CAT_DANGER = 0;
 	/**
 	*Constant for the category safe
 	*/
 	public static final int CAT_SAFE = 1;
 
 
 	/**
 	*Holds The two categories of the data being classified
 	*/
	private HashMap<int, HashMap<String,int>> category_count = new HashMap<int, HashMap<String,int>>();
 
 	public NaiveBayes(){
		HashMap<String,int> danger = HashMap<String,int>();
		HashMap<String,int> safe = HashMap<String,int>();
 		category_count.put(CAT_DANGER, danger);
 		category_count.put(CAT_SAFE, safe);
 	}
 
 	/**
	*Trains the algorithm on the dataset
 	*/
 	public void train(int category, String tweet){
 		Lemmatizer tweetStripper = new Lemmatizer();
 
 		//Get the words out of the tweet
 		String [] parsedTweet = tweetStripper.parseTweet(tweet).split(" ");
 
 		//Associate these words with the category
 		for(String pt : parsedTweet){
 			if(!category_count.get(category).containsKey(pt)){
 				category_count.get(category).put(pt,1);
 			}
 			int numW = category_count.get(category).get(pt);
			category_count.get(category).put(pt,numW);
 
 		}
 
 
 
 
 	}
 
 }
