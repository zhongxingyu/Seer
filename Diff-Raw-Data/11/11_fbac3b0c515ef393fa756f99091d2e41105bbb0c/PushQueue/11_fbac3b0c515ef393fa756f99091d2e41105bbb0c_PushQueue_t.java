 package de.kile.zapfmaster2000.rest.impl.core.push;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.ws.rs.core.Response;
 
 import org.apache.log4j.Logger;
 import org.jboss.resteasy.spi.AsynchronousResponse;
 
 /**
  * The pushed queue is used to delay rapidly incoming push notifications.
  * 
  * <p>
  * If we would send all push notifications with too little delay, some might get
  * lost: When the client receives an answer, it needs some time to re-establish
  * the connection to this service.
  * </p>
  * 
  * <p>
  * TODO: Optimize this! We use fixed delay-times here, however since we receive
  * the token for each request, we know for sure which data we did already sent
  * to each client. However, this would require some additional work...
  * </p>
  * 
  * @author Thomas Kipar
  */
 public class PushQueue extends Thread {
 
 	private static final Logger LOG = Logger.getLogger(PushQueue.class);
 
 	private static final long MIN_DELAY = 500; // in ms
 
 	private final List<AsynchronousResponse> pendingRequests = new ArrayList<>();
 
 	private final List<Response> responsesToPush = new LinkedList<>();
 
 	private long lastPush;
 
 	public PushQueue() {
 		start();
 	}
 
 	@Override
 	public void run() {
 		while (true) {
 
 			synchronized (this) {
 				try {
 					if (responsesToPush.isEmpty()) {
 						wait();
 					} else {
 						long delay = System.currentTimeMillis() - lastPush;
 						if (delay < MIN_DELAY) {
 							wait(MIN_DELAY - delay);
 						} else {
 							Response response;
 							synchronized (responsesToPush) {
 								response = responsesToPush.get(0);
 								responsesToPush.remove(0);
 							}
 							performPush(response);
 						}
 					}
 				} catch (InterruptedException e) {
 					LOG.error(e);
 				}
 			}
 
 		}
 	}
 
 	/**
 	 * Pushes the response to all asynchronous http requests.
 	 * 
 	 * @param pResponse
 	 *            the response to push to pending requests
 	 * @param pDelay
 	 *            if <code>true</code>, the response will be delayed in case
 	 *            that too many pushes occurr in a certain amount of time. If
 	 *            <code>false</code>, the push will be ignored in such cases.
 	 */
 	public void push(Response pResponse, boolean pDelay) {
 		if (pDelay) {
 			synchronized (responsesToPush) {
 				responsesToPush.add(pResponse);
 			}
 			synchronized (this) {
 				notify();
 			}
 		} else {
 			if (responsesToPush.isEmpty()
 					&& (System.currentTimeMillis() - lastPush) >= MIN_DELAY) {
 				performPush(pResponse);
 			} // else the response is ignored
 		}
 	}
 	
 	public void addRequest(AsynchronousResponse pRequest) {
 		if (pRequest != null) {
 			pendingRequests.add(pRequest);
 		}
 	}
 
 	private void performPush(Response pResponse) {
 		if (pResponse != null) {
 			lastPush = System.currentTimeMillis();
 			synchronized (pendingRequests) {
 				for (AsynchronousResponse request : pendingRequests) {
 					Response response = Response.fromResponse(pResponse)
 							.build();
 					request.setResponse(response);
					System.out.println("DID PUSH " + request);
 				}
 				pendingRequests.clear();
 			}
 		}
 	}
 }
