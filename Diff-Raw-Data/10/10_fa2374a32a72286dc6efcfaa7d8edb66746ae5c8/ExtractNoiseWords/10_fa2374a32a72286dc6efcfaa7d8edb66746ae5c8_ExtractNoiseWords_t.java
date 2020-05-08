 package edu.nyu.storm.bolts;
 
 import backtype.storm.topology.BasicOutputCollector;
 import backtype.storm.topology.OutputFieldsDeclarer;
 import backtype.storm.topology.base.BaseBasicBolt;
 import backtype.storm.tuple.Fields;
 import backtype.storm.tuple.Tuple;
 import backtype.storm.tuple.Values;
 
 public class ExtractNoiseWords extends BaseBasicBolt{
 
 	private static final long serialVersionUID = 5078004842335973173L;
	private static String noiseRegex = "\\W+";
 	
 	@Override
 	public void execute(Tuple tuple, BasicOutputCollector collector) {
 
 		String tweetToken = (String) tuple.getValueByField("tweetToken");
 		
		if(!tweetToken.matches(noiseRegex)) 
 			collector.emit(new Values(tweetToken));
		
 	}
 
 	@Override
 	public void declareOutputFields(OutputFieldsDeclarer ofd) {
 		ofd.declare(new Fields("filteredToken"));
 	}
 
 }
