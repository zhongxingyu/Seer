 package com.aidanns.streams.assignment.two.bolt;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.StringTokenizer;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import com.clearspring.analytics.stream.Counter;
 import com.clearspring.analytics.stream.StreamSummary;
 
 import backtype.storm.task.OutputCollector;
 import backtype.storm.task.TopologyContext;
 import twitter4j.Status;
 import twitter4j.internal.logging.Logger;
 
 /**
  * Bolt that will record the top K words in statuses that it processes and will
 * output them to output/top_statuses.
  * @author Aidan Nagorcka-Smith (aidann@student.unimelb.edu.au)
  */
 @SuppressWarnings("serial")
 public class TopKWordsBolt extends BaseStatusBolt {
 
 	/** Delay before first write in ms. */
 	private long FILE_WRITE_DELAY = 0;
 
 	/** Delay between writes in ms. */
 	private long FILE_WRITE_PERIOD = 1000;
 
 	/** File to write output to. */
 	private String OUTPUT_FILE_NAME = "output/words.txt";
 	
 	/** File to read stop words from. */
 	private String STOP_WORDS_FILE_NAME = "conf/stop_words.txt";
 
 	/** Counter for Tweets processed. */
 	private long _numberOfWordsProcessesd = 0;
 
 	/** Time that the bolt was started. */
 	private Date _startDate;
 
 	/** Data structure implementing the TopK algorithm. */
 	private StreamSummary<String> _topWords;
 	
 	/** Number of words we're interested in. */
 	private int _numWords;
 	
 	/** If we're only interested in hashtags. */
 	private boolean _onlyHashtags;
 	
 	/** Stop words that shouldn't be counted because they are too common and
 	 * don't really have meaning. Read from a file.
 	 */
 	private Set<String> _stopWords = new HashSet<String>();
 	
 	/**
 	 * Create a new TopKWordsBolt.
 	 * @param numWords The number of words that we want to know about.
 	 * @param onlyHashtags Set to true if you only want to consider words starting
 	 *    with the # symbole (known on Twitter as Hashtags).
 	 */
 	public TopKWordsBolt(int numWords, boolean onlyHashtags) {
 		_numWords = numWords;
 		_onlyHashtags = onlyHashtags;
 	}
 
 	@SuppressWarnings("rawtypes")
 	@Override
 	public void prepare(Map stormConf, TopologyContext context,
 			OutputCollector collector) {
 		super.prepare(stormConf, context, collector);
 		
 		 _topWords = new StreamSummary<String>(_numWords * 50);
 
 		// Setup output writing at a fixed interval.
 		Timer outputToFileTimer = new Timer();
 		outputToFileTimer.scheduleAtFixedRate(new TimerTask() {
 
 			@Override
 			public void run() {
 				if (_startDate != null) {
 					float numSecondsSinceStart = 
 							(new Date().getTime() - _startDate.getTime()) / 1000f;
 					long throughput = 
 							(long) (_numberOfWordsProcessesd / numSecondsSinceStart);
 
 					Writer writer = null;
 
 					try {
 						writer = new BufferedWriter(new OutputStreamWriter(
 								new FileOutputStream(OUTPUT_FILE_NAME), "utf-8"));
 						writer.write("Top Words Statistics:\n");
 						writer.write("\n");
 						writer.write("# Words processed: " 
 								+ _numberOfWordsProcessesd + "\n");
 						writer.write("Time elapsed:       " 
 								+ numSecondsSinceStart + "\n");
 						writer.write("Throughput:         " 
 								+ throughput + " wps\n");
 						
 						writer.write("\n");
 						for (Counter<String> counter : _topWords.topK(20)) {
 							writer.write("Item: " + counter.getItem() + " Count: " 
 									+ counter.getCount() + " Error: " 
 									+ counter.getError() + "\n");
 						}
 						
 					} catch (IOException ex) {
 						Logger.getLogger(StatusThroughputRecorderBolt.class).error(
 								"Error while writing words statistics.");
 					} finally {
 						try {
 							writer.close();
 						} catch (Exception ex) {}
 					}
 				}
 			}
 		}, FILE_WRITE_DELAY, FILE_WRITE_PERIOD);
 		
 		// Read in all the stop words from the file.
 		BufferedReader reader = null;
 		try {
 			reader = new BufferedReader(new FileReader(STOP_WORDS_FILE_NAME));
 			String line;
 			while ((line = reader.readLine()) != null) {
 				_stopWords.add(line.replaceAll("\\s+", ""));
 			}
 		} catch (IOException e) {
 			
 		} finally {
 			if (reader != null) {
 				try { reader.close(); } catch (IOException e) { /* Ensure close happens. */ }
 			}
 		}
 	}
 
 	@Override
 	void processStatus(Status status) {
 		if (_startDate == null) {
 			 _startDate = new Date();
 		}
 		StringTokenizer tokenizer = new StringTokenizer(status.getText());
 		while (tokenizer.hasMoreElements()) {
 			String word = tokenizer.nextToken();
 			if ((_onlyHashtags == false && _stopWords.contains(word))
 					|| (_onlyHashtags == true && word.startsWith("#"))) {
 				_topWords.offer(word);
 			}
 			++_numberOfWordsProcessesd;
 		}
 
 	}
 
 }
