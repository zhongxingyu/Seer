xpackage org.apache.camel.component.riak;
 import java.util.Map;
 import org.apache.camel.Endpoint;
 import org.apache.camel.Exchange;
 import org.apache.camel.impl.DefaultProducer;
 import com.basho.riak.client.*;
 import com.basho.riak.client.bucket.*;
 import com.basho.riak.client.cap.UnresolvedConflictException;
 import com.basho.riak.client.convert.ConversionException;
 
 
 public class RiakProducer extends DefaultProducer {
 	private final Bucket bucket;
 	private final RiakComponentHelper helper = new RiakComponentHelper();
 	
 
 	public RiakProducer(Endpoint endpoint, String mapName, IRiakClient pbClient)
 			throws RiakRetryFailedException {
 		super(endpoint);
 		Bucket bucket = pbClient.fetchBucket(mapName).execute();
 		this.bucket = bucket;
 		// TODO Auto-generated constructor stub
 	}
 
 	public void process(Exchange exchange) throws Exception {
 		Map<String, Object> headers = exchange.getIn().getHeaders();
 
 		// get header parameters
 		String oid = null;
 		int operation = -1;
 
 		if (headers.containsKey(RiakConstants.OBJECT_ID)) {
 			oid = (String) headers.get(RiakConstants.OBJECT_ID);
 		}
 
 		if (headers.containsKey(RiakConstants.OPERATION)) {
 
 			// producer allows int (RiakConstants) and string values
 			if (headers.get(RiakConstants.OPERATION) instanceof String) {
 				operation = helper.lookupOperationNumber((String) headers
 						.get(RiakConstants.OPERATION));
 			} else {
 				operation = (Integer) headers.get(RiakConstants.OPERATION);
 			}
 		}
 
 		
 		switch (operation) {
 
 		case RiakConstants.PUT_OPERATION:
 			this.put(oid, exchange);
 			break;
 
 		case RiakConstants.GET_OPERATION:
 			this.get(oid, exchange);
 			break;
 
 		case RiakConstants.DELETE_OPERATION:
 			this.delete(oid);
 			break;
 
 		default:
 			throw new IllegalArgumentException(
 					String.format(
 							"The value '%s' is not allowed for parameter '%s' on the MAP cache.",
 							operation, RiakConstants.OPERATION));
 		}
 
 		// finally copy headers
 		RiakComponentHelper.copyHeaders(exchange);
 
 	}
 
 	/**
 	 * remove an object from the cache
 	 * 
 	 * @throws RiakException
 	 */
 	private void delete(String oid) throws RiakException {
 		 bucket.delete(oid).execute();
 	}
 
 	/**
 	 * find an object by the given id and give it back
 	 */
 	private void get(String oid, Exchange exchange) throws RiakException {
 		Object res= bucket.fetch(oid).execute();		
 		String stringResponse = res!=null? bucket.fetch(oid).execute().getValueAsString():null;
 		exchange.getOut().setBody(stringResponse);
 	}
 
 	/**
 	 * put a new object into the bucket
 	 * @throws ConversionException 
 	 * @throws UnresolvedConflictException 
 	 * @throws RiakRetryFailedException 
 	 */
 	private void put(String oid, Exchange exchange) throws RiakRetryFailedException, UnresolvedConflictException, ConversionException {
 		Object body = exchange.getIn().getBody();
 		bucket.store(oid, body).execute();
 		
 	}
 }
