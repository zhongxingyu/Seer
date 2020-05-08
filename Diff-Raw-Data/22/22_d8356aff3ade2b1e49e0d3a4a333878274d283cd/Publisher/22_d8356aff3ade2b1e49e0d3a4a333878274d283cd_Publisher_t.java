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
 
	public boolean publish(Path dir, WatchEvent<?> event) throws IOException {
		String jsonized = (new SerializableFileEvent(event, dir)).toJson();
		if (exchangeManager.sendPassively(exchangeNameForPath(dir), event
 				.kind().name(), jsonized.getBytes()))
			System.out.format("Server says: I sent %s --> %s in directory %s\n", jsonized,
					exchangeNameForPath(dir), dir.resolve((Path) cast(event).context()));
 		return true;
 	}
 
 	public String exchangeNameForPath(Path path) {
 		return path.resolve(path).toString();
 		// String exchangeName = path.resolve((Path) cast(event).context())
 		// .toString();
 	}
 
 }
