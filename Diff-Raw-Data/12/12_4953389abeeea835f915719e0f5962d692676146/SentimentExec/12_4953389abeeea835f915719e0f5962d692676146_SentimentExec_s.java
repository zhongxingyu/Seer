 /**
  * OOSE Project - Group 4
  * SentimentExec.java
  */
 package edu.jhu.twacker.model.query;
 
 
 import java.net.URLEncoder;
 import java.util.LinkedList;
 import java.util.List;
 
 import com.google.gson.Gson;
 
 import edu.jhu.twacker.model.query.alchemy.AlchemyReponseWrapper;
 import edu.jhu.twacker.model.query.alchemy.AlchemyResponse;
 import edu.jhu.twacker.model.query.twitter.SearchTerm;
 import edu.jhu.twacker.model.query.twitter.tweet.Tweet;
 
 /**
  * This class is responsible for executing the query to obtain the data
  * to create the sentiment analysis graphs. 
  * 
  * The way it works is by creating a <code>Streamer</code> object to interact
  * with the Twitter API. It lets the Streamer gather Tweets for a given
  * amount of time. It then retrieves the Tweets and analyzes them for their
  * sentiment using the Alchemy API.
  * 
  * The result is a list of counts in the form of [positive neutral negative total].
  * 
  * The Alchemy API documentation can be found here {@link http://www.alchemyapi.com/api/sentiment/}
  * 
  * @author Daniel Deutsch
  */
 public class SentimentExec extends QueryExec
 {	
 	/**
 	 * The term to search for.
 	 */
 	private String search;
 	
 	/**
 	 * The number of positive Tweets.
 	 */
 	private int positive;
 	
 	/**
 	 * The number of neutral Tweets.
 	 */
 	private int neutral;
 	
 	/**
 	 * The number of negative Tweets.
 	 */
 	private int negative;
 	
 	/**
 	 * The total number of Tweets analyzed not including errors.
 	 */
 	private int total;
 	
 	/**
 	 * The number of Tweets that produced an error from the Alchemy API.
 	 */
 	private int errors;
 	
 	/**
 	 * The class that contains the result data.
 	 */
 	private AlchemyReponseWrapper response;
 	
 	public SentimentExec(String search)
 	{
 		this.search = search;
 	}
 	
 	/**
 	 * Executes the query to search for the given String.
 	 */
 	public void run()
 	{
		analyzeTweets(getTweets());
 		this.response = new AlchemyReponseWrapper(this.positive, this.negative, this.neutral, this.errors);
 	}
 	
 	/**
 	 * Retrieves the results of the query.
 	 * @return The results object.
 	 */
 	public AlchemyReponseWrapper getResults()
 	{
 		return this.response;
 	}
 	
 	/**
 	 * Gathers the Tweets to be analyzed and converts them into a list
 	 * of their text.
 	 * @return The list of the text.
 	 */
 	private List<String> getTweets()
 	{
 		SearchTerm searcher = new SearchTerm(this.search);
 		List<Tweet> tweets = searcher.getTweets();
 		
 		List<String> text = new LinkedList<String>();
 		for (Tweet tweet : tweets)
 		{
 			if (tweet.getText() != null)
 				text.add(tweet.getText());
 		}
 		
 		return text;
 	}
 	
 	/**
 	 * Analyzes a list of Tweets for sentiment using the Alchemy API and stores
 	 * the counts of positive, neutral, and negative Tweets in their respective data members.
 	 * @param tweets The list of Tweets.
 	 */
 	public void analyzeTweets(List<String> tweets)
 	{
 		this.negative = 0;
 		this.neutral = 0;
 		this.positive = 0;
 		this.errors = 0;
 		
 		for (String tweet : tweets)
 		{
 			int result = getSentiment(tweet);
 			switch (result)
 			{
 				case -1:
 					this.negative++;
 					break;
 				case 0:
 					this.neutral++;
 					break;
 				case 1:
 					this.positive++;
 					break;
 				case -2:
 					this.errors++;
 				default:
 					break;
 			}
 		}	
 		
 		this.total = this.positive + this.neutral + this.negative;
 	}
 	
 	/**
 	 * Retrieves the sentiment of the text string. The sentiment analysis is done
 	 * through the Alchemy API ({@link http://www.alchemyapi.com/api/sentiment/}). The
 	 * result of the analysis is either -1 for negative, 0 for neutral, or 1 for positive.
 	 * If something goes wrong during analysis and it can't be completed, -2 will be
 	 * returned.
 	 * @param text The text to analyze. 
 	 * @return The code of the analysis.
 	 */
 	private int getSentiment(String text)
 	{	
 		AlchemyResponse response = alchemyAnalysis(text);
 		
 		if (response.getStatus().equals("ERROR"))
 			return -2;
 		
 		if (response.getDocSentiment() == null)
 			System.out.println("null");
 		
 		String result = response.getDocSentiment().getType();
 		
 		if (result.equals("positive"))
 			return 1;
 		else if (result.equals("neutral"))
 			return 0;
 		else
 			return -1;
 	}
 	
 	/**
 	 * Performs the analysis with the Alchemy API. 
 	 * @param tweets The String to analyze.
 	 * @return The result of the analysis in the form of the xml String
 	 * provided by the API.
 	 */
 	private AlchemyResponse alchemyAnalysis(String tweet)
 	{
 		try
 		{
 			String encoded = URLEncoder.encode(tweet, "UTF-8");
 
 			HttpGetWrapper get = new HttpGetWrapper("http://access.alchemyapi.com/calls/text/TextGetTextSentiment?apikey=fd4235d2b727b25906b6427509178a9c86911876&text=" +
 					encoded + "&outputMode=json");
 			
 			Gson gson = new Gson();
 			return gson.fromJson(get.get(), AlchemyResponse.class);
 		}
 		catch (Exception e)
 		{
 			
 		}
 		return null;
 	} 
 
 	/**
 	 * Creates a JSON representation of the results from this query.
 	 */
 	public String toString()
 	{
 		return "\"sentiment\": { \"positive\" : \"" + this.positive + "\", \"neutral\" : \"" + this.neutral +
 				"\", \"negative\" : \"" + this.negative + "\", \"total\" : \"" + this.total + "\", " +
 				"\"errors\": \"" + this.errors + "\" }";
 	}
 	
 	/**
 	 * Tests the <code>SentimentExec</code> class.
 	 */
 	public static void main(String[] args)
 	{
 		SentimentExec sentiment = new SentimentExec("Obama");
 		sentiment.run();
 		
 		System.out.println(sentiment);
 	}
 }
