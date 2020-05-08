 package edu.nyu.trendingtopics.storm.bolts;
 
 import java.util.List;
 import twitter4j.Status;
 import backtype.storm.topology.BasicOutputCollector;
 import backtype.storm.topology.OutputFieldsDeclarer;
 import backtype.storm.topology.base.BaseBasicBolt;
 import backtype.storm.tuple.Fields;
 import backtype.storm.tuple.Tuple;
 import backtype.storm.tuple.Values;
 import edu.nyu.trendingtopics.util.Tokenizer;
 
 /**
  * Storm bolt that tokenizes the text of tweet into tokens. It uses the {@link Tokenizer} 
  * class to tokenize it using lucene analyzers.
  * 
  * @author samitpatel
  * */
 public class TokenExtractor extends BaseBasicBolt {
 
 	private static final long serialVersionUID = 8812885839655720870L;
 	private Tokenizer tokenizer = new Tokenizer();
 	
 	/**
 	 * Processes the {@link Status} object contained in the tuple and extracts
 	 * tokens. Emits these tokens out to be passed to next bolt.
 	 * 
 	 * @param tuple Tuple containing tweet Status object
 	 * @param collector Collector object which emits the data to next bolt
 	 * */
 	@Override
     public void execute(Tuple tuple, BasicOutputCollector collector) {
 		Status tweet = (Status) tuple.getValueByField("tweet");
		
 		List<String> tweetTokens = parseKeywords(tweet.getText());
 		for(String token : tweetTokens)	{
 			collector.emit(new Values(token));
 		}
     }
 
 	/**
 	 * Declares the output fields of this bolt.
 	 * 
 	 * @param ofd {@link OutputFieldsDeclarer}
 	 * */
     @Override
     public void declareOutputFields(OutputFieldsDeclarer declarer) {
     	declarer.declare(new Fields("tweetToken"));
     }
     
     /**
      * Parses the tweet text and extracts the token using lucene analyzer.
      * 
      * @param tweetText tweet
      * */
     private List<String> parseKeywords(String tweetText) {
     	return tokenizer.tokenize(tweetText);
 	}  
 
 }
