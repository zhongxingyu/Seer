 package nl.tue.fingerpaint.client;
 
 import com.google.gwt.http.client.RequestBuilder;
 import com.google.gwt.user.client.rpc.RpcRequestBuilder;
 import com.google.gwt.user.client.rpc.ServiceDefTarget;
 
 /**
  * Class that creates a {@link ServiceDefTarget} class class of your choice
  * that fails after a given amount of time before the request returns.
  * 
  * @author Group Fingerpaint
  */
 public class TimeoutRpcRequestBuilder extends RpcRequestBuilder {
 
	/**
	 * The default timeout for a request.
	 */
	public static final int DEFAULT_TIMEOUT = 60000;
	
 	/** The number of milliseconds after which the class fails */
 	private int timeoutMillis;
 
 	/**
	 * Constructs a TimeOutRpcRequestBuilder object with the default timeout.
	 */
	public TimeoutRpcRequestBuilder() {
		this.timeoutMillis = DEFAULT_TIMEOUT;
	}
	
	/**
 	 * Constructs a TimeOutRpcRequestBuilder object.
 	 * 
 	 * @param timeoutMillis
 	 *            The number of milliseconds after which the class fails
 	 */
 	public TimeoutRpcRequestBuilder(int timeoutMillis) {
 		this.timeoutMillis = timeoutMillis;
 	}
 
 	/**
 	 * Returns the number of milliseconds after which the class fails.
 	 * 
 	 * @return The number of milliseconds after which the class fails
 	 */
 	public int getTimeoutMillis() {
 		return timeoutMillis;
 	}
 
 	@Override
 	protected RequestBuilder doCreate(String serviceEntryPoint) {
 		RequestBuilder builder = new RequestBuilder(RequestBuilder.POST,
 				serviceEntryPoint);
 		builder.setTimeoutMillis(timeoutMillis);
 		return builder;
 	}
 }
