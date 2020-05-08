 package watch;
 
 import java.io.IOException;
 import java.nio.file.Path;
 import java.nio.file.WatchEvent;
 import java.util.Map;
 
 import com.rabbitmq.client.Channel;
 import com.rabbitmq.client.Connection;
 import com.rabbitmq.client.ConnectionFactory;
 import com.rabbitmq.client.AMQP.Exchange.DeclareOk;
 
 public class Publisher {
 	ExchangeManager exchangeManager;
 
 	static <T> WatchEvent<T> cast(WatchEvent<?> event) {
 		return (WatchEvent<T>) event;
 	}
 
 	public Publisher() throws IOException {
 		exchangeManager = new ExchangeManager();
 	}
 
	public boolean publish(Path path, WatchEvent<?> event) throws IOException {
		String jsonized = (new SerializableFileEvent(event)).toJson();
		if (exchangeManager.sendPassively(exchangeNameForPath(path), event
 				.kind().name(), jsonized.getBytes()))
			System.out.format("Server says: I sent %s --> %s \n", jsonized,
					exchangeNameForPath(path));
 		return true;
 	}
 
 	public String exchangeNameForPath(Path path) {
 		return path.resolve(path).toString();
 		// String exchangeName = path.resolve((Path) cast(event).context())
 		// .toString();
 	}
 
 }
