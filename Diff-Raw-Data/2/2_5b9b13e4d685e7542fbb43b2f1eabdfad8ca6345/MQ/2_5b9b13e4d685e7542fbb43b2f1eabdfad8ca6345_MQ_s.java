 package monopoly.web.util;
 
 import java.util.concurrent.LinkedBlockingQueue;
 
 public class MQ<T> implements IMQ<T> {
 	
	private final LinkedBlockingQueue<T> q = new LinkedBlockingQueue<>();
 	
 	@Override
 	public T getNextRequest() {
 		try {
 			return q.take();
 		} catch (InterruptedException e) {
 			return null;
 		}
 	}
 	
 	@Override
 	public void submitRequest(T req) {
 		q.add(req);
 	}
 
 }
