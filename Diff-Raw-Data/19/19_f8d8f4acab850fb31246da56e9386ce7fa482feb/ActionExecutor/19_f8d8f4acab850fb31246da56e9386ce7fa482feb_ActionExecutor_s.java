 package pt.com.broker.monitorization.http;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pt.com.broker.monitorization.actions.DeleteQueue;
 import pt.com.broker.monitorization.collectors.JsonEncodable;
 
 public class ActionExecutor
 {
 	private static final Logger log = LoggerFactory.getLogger(ActionExecutor.class);
 
 	public interface Action
 	{
 		Collection<JsonEncodable> execute(String resource, Map<String, String> arguments);
 	}
 
 	private static Map<String, Action> executors = new HashMap<String, Action>();
 
 	static
 	{
 		executors.put("deletequeue", new Action()
 		{
 			final String QUEUE_NAME = "queuename";
 
 			@Override
 			public Collection<JsonEncodable> execute(String resource, Map<String, String> arguments)
 			{
 				Collection<JsonEncodable> queuesInfo = new ArrayList<JsonEncodable>();
 				String queueName = arguments.get(QUEUE_NAME);
 				if (queueName == null)
 					return queuesInfo;
 
 				Collection<JsonEncodable> results = new ArrayList<JsonEncodable>(DeleteQueue.execute(queueName));
 
 				return results;
 			}
 		});
 	}
 
 	public static String execute(String resource, Map<String,String> arguments)
 	{
		String[] parts = resource.split("/");
 		Action executor = executors.get(parts[0]);
 		if (executor == null)
 		{
 			return String.format("{\"Unknown data identifier - %s\"}", resource);
 		}
 
 		int offset = parts.length > 1 ? 1 : 0;
 
 		Collection<JsonEncodable> dataItems = executor.execute(resource.substring(parts[0].length() + offset), arguments);
 		String encoded = JsonUtil.getJsonEncodedCollection(dataItems);
 		return encoded;
 	}
 }
