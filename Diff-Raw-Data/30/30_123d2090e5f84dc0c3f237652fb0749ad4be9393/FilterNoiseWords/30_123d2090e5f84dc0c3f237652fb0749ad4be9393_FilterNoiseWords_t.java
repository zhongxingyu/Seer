 package edu.nyu.trendingtopics.storm.bolts;
 
 import edu.nyu.trendingtopics.util.StopWords;
 import backtype.storm.topology.BasicOutputCollector;
 import backtype.storm.topology.OutputFieldsDeclarer;
 import backtype.storm.topology.base.BaseBasicBolt;
 import backtype.storm.tuple.Fields;
 import backtype.storm.tuple.Tuple;
 import backtype.storm.tuple.Values;
 
 
 /**
  * Filters out the noise words based on the stopwords list and the regular expression.
  * It filters out all words that are of length less than 3 and greater than 14. This is 
  * just a random boundaries. This bolt also filters the words that are present in the 
  * stopwords list provided to {@link StopWords}.
  * 
  * @author samitpatel
  * */
 public class FilterNoiseWords extends BaseBasicBolt{
 
 	private static final long serialVersionUID = 5078004842335973173L;
 	private static String nonWords = "(\\W+|\\d+)";
 	private static String englishWords = "^[a-zA-Z#]+$"; 
 	
 	/**
 	 * Filters out unwanted words.
 	 * 
 	 * @param tuple Tuple containing the tweet token.
 	 * @param  collector Collector object which emits the data to next bolt.
 	 * */
 	@Override
 	public void execute(Tuple tuple, BasicOutputCollector collector) {
 
 		String tweetToken = tuple.getStringByField("tweetToken");
 		
 		if(tweetToken.length() > 2 && 
 				tweetToken.length() < 15 &&
 				!tweetToken.matches(nonWords) && 
 				tweetToken.matches(englishWords) && 
				!StopWords.isStopWord(tweetToken))
 			collector.emit(new Values(tweetToken));
 		
 	}
 
 	/**
 	 * Declares the output fields of this bolt.
 	 * 
 	 * @param ofd {@link OutputFieldsDeclarer}
 	 * */
 	@Override
 	public void declareOutputFields(OutputFieldsDeclarer declarer) {
 		declarer.declare(new Fields("filteredToken"));
 	}
 
 }
