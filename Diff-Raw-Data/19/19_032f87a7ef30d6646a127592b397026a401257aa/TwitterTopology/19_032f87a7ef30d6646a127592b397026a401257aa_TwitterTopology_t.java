 package topology;
 
 import socket.Socket;
 import spout.TwitterSpout;
 import backtype.storm.Config;
 import backtype.storm.LocalCluster;
 import backtype.storm.topology.TopologyBuilder;
 import backtype.storm.utils.Utils;
 import bolt.CategoryTweetBolt;
 import bolt.NationalityTweetBolt;
 
 public class TwitterTopology {
 	
 	
 	public static void main (String[] args){
 		
 		TwitterTopology twitterTopology = new TwitterTopology();
		final Socket socket = new Socket(twitterTopology);
 		
 	}
 
 	public static void executeStorm(String category) throws Exception {
 
 		final TopologyBuilder builder = new TopologyBuilder();
     
 		CategoryTweetBolt categoryBolt = new CategoryTweetBolt();
 		NationalityTweetBolt nationBolt = new NationalityTweetBolt();
 		categoryBolt.setCategory(category);
 		
 		
 		builder.setSpout("tweetLL", new TwitterSpout());
 		builder.setBolt("categoryTweet",categoryBolt).shuffleGrouping("tweetLL");
 		builder.setBolt("nationalityTweet",nationBolt).shuffleGrouping("categoryTweet");
 	
 		final Config conf = new Config();
 		final LocalCluster cluster = new LocalCluster();
 
 		cluster.submitTopology("test", conf, builder.createTopology());
 
 		
 		Utils.sleep(5 * 60 * 1000);
 		cluster.shutdown();
 
 	}
 
 
 }
