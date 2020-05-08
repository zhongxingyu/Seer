 package psb;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
import java.net.URLConnection;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 public final class HttpStreaming {
 
 	private Action<String> onReceived;
 	private final ExecutorService threadPool = 
     		Executors.newSingleThreadExecutor(new StreamThreadFactory());
 	private BufferedReader bf;
 	private InputStream in;
 	private volatile Boolean running = false;
 	private String url;
 	
 	public HttpStreaming(String url){
 		this.url = url;
 	}
 	
 	private void setup(){
 		try {
			URLConnection connection = new URL(url).openConnection();
			in = connection.getInputStream();
 		} catch (MalformedURLException e) {} 
 		catch (IOException e) {}
 		if(in == null)
 			throw new RuntimeException("Could not connect to url: " +
 					url);
 		bf = new BufferedReader(new InputStreamReader(in));
 	}
 	
 	public void start(){
 		if(running)
 			throw new RuntimeException("Streaming is already in progress");
 		running = true;
 		threadPool.execute(new Runnable() {			
 			@Override
 			public void run() {
 				setup();
 				poll();
 			}
 		});
 	}
 	
 	public void stop(){
 		running = false;
 		try {
 			bf.close();
 		} 
 		catch (IOException e) {}
 		finally{
 			threadPool.shutdown();
 		}
 	}
 	
 	private int read(){
 		try {
 			return bf.read();
 		} catch (IOException e) {}
 		return -1;
 	}
 	
 	public void poll() {
 		StringBuffer reader = new StringBuffer();
 		int end = 0, begin = 7, read = 0;
 		while(running){			
 			if((read = read()) == -1){
 				break;
 			}
 			reader.append((char)read);						
 			if((end = reader.indexOf("</comet>")) != -1){	
 				if(onReceived != null)
 					onReceived.execute(reader.substring(begin, end));
 				reader.delete(0, reader.length());				
 			}
 		}
 	}
 	
 	public void setOnReceived(Action<String> action){
 		onReceived = action;
 	}
 }
