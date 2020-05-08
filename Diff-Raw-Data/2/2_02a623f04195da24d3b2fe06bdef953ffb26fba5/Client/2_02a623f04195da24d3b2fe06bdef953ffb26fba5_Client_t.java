 // Attribution-Noncommercial-Share Alike 3.0 Unported
 // (see more at http://creativecommons.org/licenses/by-nc-sa/3.0/)
 // (c) 2009 Maxim Kirillov <max630@gmail.com>
 
 import java.io.IOException;
 
 import java.net.Socket;
 import java.net.InetAddress;
 
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import java.lang.ref.WeakReference;
 
 public class Client {
 	Socket client_socket;
 	ConcurrentHashMap<Integer, WeakReference<EventsQueue>> listeners;
 	AtomicInteger counter;
 
 	Client(String hostname, int port)
 	{
 		try {
			this.client_socket = new Socket(InetAddress.getByName(hostname), port);
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 		this.listeners = new ConcurrentHashMap<Integer, WeakReference<EventsQueue>>();
 		this.counter = new AtomicInteger(0);
 
 		final Client reader = this;
 		new Thread(new Runnable() {
 			public void run() {
 				try {
 					while(true) {
 						String data = reader.read();
 						for (Integer q_idx: reader.listeners.keySet()) {
 							EventsQueue q = reader.listeners.get(q_idx).get();
 							if (q != null) {
 								q.add(data);
 							} else {
 								reader.listeners.remove(q_idx);
 							}
 						}
 					}
 				} catch (Exception e) {
 					for (Integer q_idx: reader.listeners.keySet()) {
 						EventsQueue q = reader.listeners.get(q_idx).get();
 						if (q != null) {
 							q.setFailure(e);
 							q.add(";"); // to wake this thread
 						}
 					}
 					throw new RuntimeException(e);
 				}
 			}
 		}).start();
 	}
 
 	public interface Events {
 		public String take() throws InterruptedException;
 	}
 
 	private interface EventsQueue extends Events {
 		public void add(String data);
 		public void setFailure(Exception failure);
 	}
 
 	public Events addListener() {
 		EventsQueue res = new EventsQueue() {
 			final BlockingQueue<String> queue;
 			Exception client_failure;
 
 			{
 				queue = new LinkedBlockingQueue<String>();
 				client_failure = null;
 			}
 
 			public void add(String data) {
 				this.queue.add(data);
 			}
 
 			public String take()
 				throws InterruptedException
 			{
 				String res = this.queue.take();
 				if (this.client_failure != null) {
 					throw new RuntimeException(this.client_failure);
 				}
 				return res;
 			}
 
 			public void setFailure(Exception failure) {
 				this.client_failure = failure;
 			}
 		};
 		this.listeners.put(new Integer(this.counter.addAndGet(1)), new WeakReference<EventsQueue>(res));
 		return res;
 	}
 
 	private String read() {
 		byte buf[] = new byte[1024];
 		int read_cnt;
 		try {
 			read_cnt = this.client_socket.getInputStream().read(buf);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 		if (read_cnt <= 0) {
 			throw new RuntimeException("EOF");
 		}
 		return new String(buf, 0, read_cnt);
 	}
 
 	public void write(String data) {
 		try {
 			this.client_socket.getOutputStream().write(data.getBytes());
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 }
