 package com.jboss.demo.mrg.messaging.data;
 
 import java.io.InputStream;
 import java.util.StringTokenizer;
 
 import com.jboss.demo.mrg.messaging.data.DataSourceConsumer;
 
 /**
  * Data source for the qpid-queue-stats command.
  * @author Mike Darretta
  */
 public class QpidQueueStatsOutputDataSource extends OutputDataSource {
 	
 	/** The column to process */
 	protected int columnToProcess;
 	
 	/** The queue name column number */
	public static final int QUEUE_NAME_COLUMN = 1;
 	
 	/** The seconds column number */
	public static final int SEC_COLUMN = 2;
 	
 	/** The queue depth column number */
	public static final int DEPTH_COLUMN = 3;
 	
 	/** The enqueue rate column number */
	public static final int ENQ_RATE_COLUMN = 4;
 	
 	/** The dequeue rate column number */
	public static final int DEQ_RATE_COLUMN = 5;
 	
 	/**
 	 * Constructor.
 	 * @param inputStream The input stream.
 	 */
 	public QpidQueueStatsOutputDataSource(InputStream inputStream, int columnToProcess) {
 		super(inputStream);
 		this.columnToProcess = columnToProcess;
 	}
 	
 	/**
 	 * Extension of the <code>manage</code> method to add processing
 	 * instructions for this class.
 	 * @param consumer The consumer.
 	 * @param line The line to process.
 	 */
 	@Override
 	public void processLine(DataSourceConsumer consumer, String line) {
 		StringTokenizer tokenizer = new StringTokenizer(line);
 		String token = null;
 		
		if (tokenizer.countTokens() >= (columnToProcess)) {
 			token = tokenizer.nextToken();
 			if (token.contains("qpid-perftest0")) {
 				for (int x = 1; x < columnToProcess; x++) {
 					token = tokenizer.nextToken();
 				}
 
 				consumer.update(Double.valueOf(token).intValue());
 			}
 		}
 	}
 }
