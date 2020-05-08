 package commons.io;
 
 import java.util.StringTokenizer;
 
 import commons.cloud.Request;
 
 /**
  * GEIST parser. A GEIST workload file contains one request per line.
  * Each line contains nine tab separated columns ordered as follow:<br>
  * <ul>
  * 	<li>User ID</li>
  * 	<li>Request ID</li>
  * 	<li>Time</li>
  * 	<li>Request size in Bytes</li>
  * 	<li>Response size in Bytes (Size of requested file)</li>
  * 	<li> Array of demand in millis </li>
  * </ul>
  * @author Ricardo Ara&uacute;jo Santos - ricardo@lsd.ufcg.edu.br
  */
 public class GEISTMultiFileWorkloadParser extends AbstractWorkloadParser{
 	
	private final int saasClientID;
 //	private static final Pattern pattern = Pattern.compile("( +|\t+)+");
 

 	/**
 	 * Default constructor
 	 * @param workloads 
 	 */
 	public GEISTMultiFileWorkloadParser(String workload, int saasclientID) {
 		super(workload, saasclientID);
		this.saasClientID = saasclientID;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected Request parseRequest(String line) {
 //		String[] eventData = pattern.split(line);
 		StringTokenizer tokenizer = new StringTokenizer(line, "( +|\t+)+");
 		
 //		long [] demand = new long[4];
 //		demand[0] = 10;
 //		demand[1] = 10;
 //		demand[2] = 10;
 //		demand[3] = 10;
 		
 //		return new Request(Long.valueOf(eventData[1]), saasClientID, Integer.valueOf(eventData[0]), Long
 //				.valueOf(eventData[2])+(periodsAlreadyRead * TickSize.DAY.getTickInMillis()), Long.valueOf(eventData[3]),
 //				Long.valueOf(eventData[4]), demand);
 		int userID = Integer.parseInt(tokenizer.nextToken());
 		long reqID = Long.parseLong(tokenizer.nextToken());
 		long arrivalTimeInMillis = Long.parseLong(tokenizer.nextToken());
 		long requestSizeInBytes = Long.parseLong(tokenizer.nextToken());
 		long responseSizeInBytes = Long.parseLong(tokenizer.nextToken());
 		
 		long [] demand = new long[tokenizer.countTokens()];
 		int index = 0;
 		while(tokenizer.hasMoreTokens()){
 			demand[index++] = Long.parseLong(tokenizer.nextToken());
 //			tokenizer.nextToken();
 //			demand[index++] = 200;
 		}
 		
 		return new Request(userID, reqID, saasClientID, 
 				arrivalTimeInMillis, requestSizeInBytes,
 				responseSizeInBytes, demand);
 	}
 
 	@Override
 	public void applyError(double error) {
 		throw new RuntimeException("Not yet implemented!");
 	}
 }
