 package stv6;
 
 import java.io.IOException;
 
 import stv6.http.Client;
 import stv6.http.HttpSocket;
 import stv6.http.request.Request;
 import stv6.http.request.RequestHandlerManager;
 import stv6.http.request.Response;
 
 public class STClient implements Client {
 	
 	/**
 	 * Cookie name that holds the user_id
 	 */
 	public static final String COOKIE_USERID = "st_userid";
 
 	public static final String DEFAULT_USERNAME = "Default";
 	
 	private final HttpSocket skt;
 	private final RequestHandlerManager callback;
 	private boolean isDead = false;
 	
 	public STClient(HttpSocket skt, RequestHandlerManager callback) {
 		this.skt = skt;
 		this.callback = callback;
 	}
 
 	@Override
 	public boolean isDead() {
 		return isDead;
 	}
 
 	@Override
 	public void process() {
 		// let someone else work until we're ready
		if (!skt.ready())
			return;
 		
 		// see what they want to do
 		Request r;
 		try {
 			r = Request.readFrom(skt);
 		} catch (IOException e) {
 			e.printStackTrace();
 			isDead = true;
 			skt.close();
 			return;
 		}
 		
 		// handle the request
 		Response resp = callback.handle(r);		
 		skt.write(resp);
 		
 		// clean up
 		skt.close();		
 		isDead = true;
 	}
 
 }
