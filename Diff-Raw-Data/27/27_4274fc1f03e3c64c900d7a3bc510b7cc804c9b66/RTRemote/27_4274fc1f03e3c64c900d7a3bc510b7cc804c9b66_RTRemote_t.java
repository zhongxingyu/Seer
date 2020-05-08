 package li.rudin.rt.remote;
 
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import li.rudin.rt.api.observable.Observable;
 import li.rudin.rt.core.container.ObjectContainer;
 import li.rudin.rt.remote.listener.RTListenerWrapper;
 import li.rudin.rt.remote.observable.ObservableUpdater;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.fasterxml.jackson.databind.ObjectMapper;
 
 public class RTRemote implements Runnable
 {
 
 	/**
 	 * Local logger
 	 */
 	private static final Logger logger = LoggerFactory.getLogger(RTRemote.class);
 
 	/**
 	 * Creates a remote to given url
 	 * @param url
 	 */
 	public RTRemote(String url)
 	{
		httpclient = HttpClientBuilder.create().build();
 		httpGet = new HttpGet(url);
 		thread.start();
 	}
 
 	private Thread thread = new Thread(this);
 
 	/**
 	 * JSON Mapper
 	 */
 	private static final ObjectMapper mapper = new ObjectMapper();
 
 	/**
 	 * Stops the remote
 	 */
 	public void stop()
 	{
 		thread = null;
 	}
 
 
	private final CloseableHttpClient httpclient;
 	private final HttpGet httpGet;
 
 	/**
 	 * Adds a listener for the eventId
 	 * @param eventId
 	 * @param listener
 	 */
 	public <T> void addListener(String eventId, RTEventListener<T> listener)
 	{
 		List<RTListenerWrapper<?>> list = listenerMap.get(eventId);
 
 		if (list == null)
 		{
 			list = new CopyOnWriteArrayList<>();
 			listenerMap.put(eventId, list);
 		}
 
 		list.add(new RTListenerWrapper<>(listener));
 	}
 
 	/**
 	 * Removes the listener
 	 * @param eventId
 	 * @param listener
 	 */
 	public <T> void removeListener(String eventId, RTEventListener<T> listener)
 	{
 		List<RTListenerWrapper<?>> list = listenerMap.get(eventId);
 		if (list == null)
 			return;
 
 		list.remove(listener);
 	}
 
 	private final Map<String, List<RTListenerWrapper<?>>> listenerMap = new ConcurrentHashMap<>();
 
 	@Override
 	public void run()
 	{
 		logger.debug("Started RTRemote thread");
 		
 		while (thread != null)
 		{
 			try
 			{				
 				HttpResponse response = httpclient.execute(httpGet);
 				HttpEntity entity = response.getEntity();
 
 				ObjectContainer[] data = mapper.readValue(entity.getContent(), ObjectContainer[].class);
 
 				httpGet.reset();
 
 				logger.debug("Got {} new event(s)", data.length);
 				
 				for (ObjectContainer c: data)
 				{
 					logger.debug("	- event: type({})", c.getType());
 					
 					List<RTListenerWrapper<?>> list = listenerMap.get(c.getType());
 					if (list == null)
 						continue;
 
 					for (RTListenerWrapper<?> listener: list)
 						listener.onData(c.getData());
 				}
 
 			}
 			catch (Exception e)
 			{
 				logger.error("run", e);
 			}
 		}
 		
 		logger.debug("Stopped RTRemote thread");
 
 	}
 	
 	/**
 	 * Creates an observable
 	 * @param changeId
 	 * @param type
 	 * @return
 	 */
 	public <T> Observable<T> observable(String changeId, Class<T> type)
 	{
 		Observable<T> observable = new Observable<>();
 		
 		addListener(changeId, new ObservableUpdater<>(observable, type));
 		
 		return observable;
 	}
 
 }
