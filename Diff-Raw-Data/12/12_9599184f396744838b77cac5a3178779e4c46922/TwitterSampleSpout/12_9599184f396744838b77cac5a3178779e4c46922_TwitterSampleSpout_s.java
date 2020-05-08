 package com.yahoo.swc.spout;
 
 import java.util.Map;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import twitter4j.StallWarning;
 import twitter4j.Status;
 import twitter4j.StatusDeletionNotice;
 import twitter4j.StatusListener;
 import twitter4j.TwitterStream;
 import twitter4j.TwitterStreamFactory;
 
 import backtype.storm.Config;
 import backtype.storm.spout.SpoutOutputCollector;
 import backtype.storm.task.TopologyContext;
 import backtype.storm.topology.OutputFieldsDeclarer;
 import backtype.storm.topology.base.BaseRichSpout;
 import backtype.storm.tuple.Fields;
 import backtype.storm.tuple.Values;
 import backtype.storm.utils.Utils;
 
 /**
  *  Spout that get the Twitter sample stream and emit them
  */
 
 public class TwitterSampleSpout extends BaseRichSpout {
 	
 	private static final long serialVersionUID = -5878104600899840638L;
 	private SpoutOutputCollector _collector = null;
 	private LinkedBlockingQueue<Status> _statusQueue = null;
 	private TwitterStream _twitterStream = null;
 	
 	//prefer long over BigInteger, 
 	//however need mechanism to handle overflow case (long.MAX_VALUE)
 	private long _lostStatus;
 	private long _receivedStatus;
 	private long _emittedStatus;
 	
 	public void open(@SuppressWarnings("rawtypes") Map conf, TopologyContext context,
 		SpoutOutputCollector collector) {
 		_collector = collector;
 		_statusQueue = new LinkedBlockingQueue<Status>();
 		_twitterStream = new TwitterStreamFactory().getInstance();
 		_lostStatus = 0;
 		_receivedStatus = 0;
 		_emittedStatus = 0;
 		
 		//use anonymous class to simplify the program
 		//early observation: 300 to 400 tweets per 10 seconds (30 to 40 tps)
 		//could be caused by 
 		//1. overhead of twitter4J (?), event subscription
 		//2. locking mechanism in LinkedBlockingQueue
 		
 		StatusListener listener = new StatusListener() {
 
 			public void onException(Exception ex) {
 				//do nothing, we are not interested in this event
 			}
 
 			//TODO: check whether StatusListener is threadsafe
 			//If not, we need to lock the object to obtain "valid" count
 			//Ultimate question: do we need the count?
 			//Is stream rate important in this project?
 			public void onStatus(Status status) {
 				_receivedStatus++;
 				if(!_statusQueue.offer(status)){
 					_lostStatus++; 
 				}			
 			}
 
 			public void onDeletionNotice(
 					StatusDeletionNotice statusDeletionNotice) {
 				//do nothing, we are not interested in this event
 			}
 
 			public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
 				//do nothing, we are not interested in this event				
 			}
 
 			public void onScrubGeo(long userId, long upToStatusId) {
 				//do nothing, we are not interested in this event				
 			}
 
 			public void onStallWarning(StallWarning warning) {
 				//do nothing, we are not interested in this event				
 			}
 		};
 		
 		_twitterStream.addListener(listener);
		_twitterStream.sample();
 	}
 
 	public void nextTuple() {
 		
 		Status currentStatus = _statusQueue.poll();
 		if(currentStatus != null){
 			_emittedStatus++;
 			_collector.emit(new Values(currentStatus.getText()));
 		} else {
 			Utils.sleep(50);
 		}
 	}
 
 	public void declareOutputFields(OutputFieldsDeclarer declarer) {
 		declarer.declare(new Fields("status"));
 	}
 	
 	@Override
 	public void close(){
 		_twitterStream.shutdown();
 		System.out.println(
 				String.format("Tweets summary; rcvd = %d; " +
 						"lost = %d; emitted = %d",
 				_receivedStatus, _lostStatus, _emittedStatus));
 	}
 	
 	@Override
 	public Map<String, Object> getComponentConfiguration(){
 		Config conf = new Config();
 		conf.setMaxTaskParallelism(1);
 		return conf;
 	}
 	
 	public long getLostStatusCounter(){
 		return _lostStatus;
 	}
 }
