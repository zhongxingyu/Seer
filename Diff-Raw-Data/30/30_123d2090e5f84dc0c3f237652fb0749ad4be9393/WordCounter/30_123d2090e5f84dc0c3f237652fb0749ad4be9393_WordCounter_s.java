 package edu.nyu.trendingtopics.storm.bolts;
 
 import static edu.nyu.trendingtopics.storm.Constant.WORDS_WRITE_BATCH_SIZE;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import backtype.storm.task.TopologyContext;
 import backtype.storm.topology.BasicOutputCollector;
 import backtype.storm.topology.OutputFieldsDeclarer;
 import backtype.storm.topology.base.BaseBasicBolt;
 import backtype.storm.tuple.Fields;
 import backtype.storm.tuple.Tuple;
 import backtype.storm.tuple.Values;
 
 /**
  * Storm bolt that keeps track of the counters of each word. Once the size of the map
  * reaches a pre-defined limit, it emits the map to next bolt to be written to 
  * cassandra.
  * 
  * @author soobokshin
  * */
 public class WordCounter extends BaseBasicBolt {
 
 	private static final long serialVersionUID = 5968327715395972088L;
 
 	int id;
 	String name;
 	Map<String, Integer> counters;
 	
 	/**
 	 * Notes the basic configuration of this bolt when this bolt is prepared for execution.
 	 * This will be called only once at startup.
 	 * 
 	 * @param stormConf Storm Configuration.
	 * @param context Context of Strom Topology
 	 */
 	@SuppressWarnings("rawtypes")
 	@Override
 	public void prepare(Map stormConf, TopologyContext context) {
 		this.counters = new HashMap<String, Integer>();
 		this.name = context.getThisComponentId();
 		this.id = context.getThisTaskId();
 	}
 
 	/**
 	 * Declares the output fields of this bolt.
 	 * 
 	 * @param declarer {@link OutputFieldsDeclarer}
 	 * */
 	@Override
 	public void declareOutputFields(OutputFieldsDeclarer declarer) {
 		declarer.declare(new Fields("wordCountsMap"));
 	}
 
 
 	/**
 	 * Maintains the count of each word. Emits the map to next bolt when the 
 	 * size of the map exceeds a predefined limit.
 	 * 
 	 * @param input Tuple that is feed to this bolt
 	 * @param collector Collector that collects and passes the data to next
 	 * bolt.
 	 * */
 	@Override
 	public void execute(Tuple input, BasicOutputCollector collector) {
 
 		String str = input.getString(0);
 		/**
 		 * If the word doesn't exist in the map we will create
 		 * this, if not we will add 1 
 		 */
 		if(!counters.containsKey(str)){
 			counters.put(str, 1);
 		}else{
 			int c = counters.get(str) + 1;
 			counters.put(str, c);
 		}
 		
 		//when the size exceeds the limit, write it cassandra.
 		if(counters.size() > WORDS_WRITE_BATCH_SIZE){
 			collector.emit(new Values(counters));
 			counters = new HashMap<String, Integer>();
 		}
 		
 	}
 }
