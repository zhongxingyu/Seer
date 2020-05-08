 package edu.nyu.trendingtopics.storm.bolts;
 
 import static edu.nyu.trendingtopics.storm.Constant.CL;
 import static edu.nyu.trendingtopics.storm.Constant.TOP_WORDS_COUNT;
 import static edu.nyu.trendingtopics.storm.Constant.UTF8;
 
 import java.io.UnsupportedEncodingException;
 import java.nio.ByteBuffer;
 import java.util.Comparator;
 import java.util.Map;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import org.apache.cassandra.thrift.Cassandra.Client;
 import org.apache.cassandra.thrift.Column;
 import org.apache.cassandra.thrift.ColumnParent;
 
 import backtype.storm.topology.BasicOutputCollector;
 import backtype.storm.topology.OutputFieldsDeclarer;
 import backtype.storm.topology.base.BaseBasicBolt;
 import backtype.storm.tuple.Tuple;
 import edu.nyu.trendingtopics.storm.Connector;
 
 /**
  * Storm bolt that writes the word count data to Cassandra. It writes only 
  * a predefined top words to the cassandra and ignore the words with count
  * 1.
  * 
  * @author soobokshin
  * */
 public class WriteToCassandra extends BaseBasicBolt {
 	
 
 		private static final long serialVersionUID = 5168687340899971753L;
 		private long _batchNumber = 1;
 		
 		/**
 		 * Writes the map containing words and its count to Cassandra.
 		 * 
 		 * @param tuple Tuple containing the map of words to its count
 		 * @param collector Collector
 		 * */
 		@Override
 		public void execute(Tuple tuple, BasicOutputCollector collector) {
 			
 			@SuppressWarnings("unchecked")
 			Map<String, Integer> counters = (Map<String, Integer>) tuple.getValueByField("wordCountsMap");
 			writeToCassandra(counters);
 		}
 
 		/**
 		 * Declares the output fields of this bolt.
 		 * 
 		 * @param declarer {@link OutputFieldsDeclarer}
 		 * */
 		@Override
 		public void declareOutputFields(OutputFieldsDeclarer declarer) {}
 		
 		/**
 		 * Creates a Client to Cassandra server and writes the data.
 		 * 
 		 * @param counters Map containing mapping from word to its count. 
 		 * 					Map should be sorted based on count.
 		 * */
 		private void writeToCassandra(Map<String, Integer> counters){
 			
 			Connector connector = new Connector();
 			try {
 				Client client = connector.connect();
 				
 				//creates a sorted map based on the count.
 				ValueComparator comparator = new ValueComparator(counters);
 				SortedMap<String, Integer> sortedCounter = new TreeMap<String, Integer>(comparator);
 				sortedCounter.putAll(counters);
 				
 				long timestamp = System.currentTimeMillis();
 				ColumnParent parent = new ColumnParent("wordCount");
 
 				//writes top TOP_WORDS_COUNT words to Cassandra.
 				//the loop breaks when it encounters a word with count 1.
 				//This is enforced to reduce number of rare words.
 				int count=0; 
 				for(Map.Entry<String, Integer> entry : sortedCounter.entrySet()){
 					if(count < TOP_WORDS_COUNT) count++;
 					else break;
 
 					Column idColumn;
 					try {
 						//loop breaks when it encounters a word with count 1.
 						if(entry.getValue() == 1) break;
 						
 						//create a new column with column name as word and value as count
 						idColumn = new Column(toByteBuffer(entry.getKey()));
 						idColumn.setValue(toByteBuffer(entry.getValue().toString()));
 						idColumn.setTimestamp(timestamp);
 						
 						//insert the column to cassandra with row key as current timestamp
 						client.insert(toByteBuffer(timestamp+""), parent, idColumn, CL);
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 				
 				System.out.println("Batch "+_batchNumber+" Written");
 			} catch (Exception e1) {
 				e1.printStackTrace();
 			}finally{
 				connector.close();
 			}
 		}
 
 		/**
 		 * Converts string to ByteBuffer.
 		 * 
 		 * @param value Value to converted to ByteBuffer.
 		 * @return Return the ByteBuffer containing the value.
 		 * */
 		private ByteBuffer toByteBuffer(String value) throws UnsupportedEncodingException{
 			return ByteBuffer.wrap(value.getBytes(UTF8));
 		}
 
 		
 		/**
 		 * Value comparator to compare the map entry based on its value.
 		 * 
 		 * @author samitpatel
 		 * */
 		class ValueComparator implements Comparator<String> {
 
 		    Map<String, Integer> base;
 		    public ValueComparator(Map<String, Integer> base) {
 		        this.base = base;
 		    }
 
 		    /**
 		     * Compares the values of two keys. Enforces the descending order.
 		     * 
 		     * @param key1 Key of the first value to be compared.
 		     * @param key2 Key of the second value to be compared.
 		     * 
 		     * @return Returns -1 if first value is greater than or equal to second value, otherwise 1
 		     * */
 		    public int compare(String key1, String key2) {
 		       if(base.get(key1) >= base.get(key2)) 
 		    	   return -1; //descending order
 		       else
 		    	   return 1;
 		    }
 		}
 
 }
