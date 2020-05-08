 package amp.topology.core;
 
 import java.util.Map;
 import java.util.UUID;
 
import amp.bus.rabbit.topology.Exchange;
 
 public class ExtendedExchange extends Exchange {
 
 	private String id;
 	private String description;
 	
 	public ExtendedExchange(){
 		super(null, null, null, -1, null, null, null, false, false, null);
 	}
 	
 	public ExtendedExchange(
 			String description,
 			String name, String hostName, String vHost,
 			int port, String routingKey, String queueName, String exchangeType,
 			boolean isDurable, boolean autoDelete, 
 			@SuppressWarnings("rawtypes") Map arguments) {
 		
 		super(name, hostName, vHost, port, routingKey, queueName, exchangeType,
 				isDurable, autoDelete, arguments);
 		
 		this.id = UUID.randomUUID().toString();
 		this.description = description;
 	}
 	
 	public ExtendedExchange(
 			String id, String description,
 			String name, String hostName, String vHost,
 			int port, String routingKey, String queueName, String exchangeType,
 			boolean isDurable, boolean autoDelete, 
 			@SuppressWarnings("rawtypes") Map arguments) {
 		
 		super(name, hostName, vHost, port, routingKey, queueName, exchangeType,
 				isDurable, autoDelete, arguments);
 		
 		this.id = id;
 		this.description = description;
 	}
 
 	public String getId() {
 		return id;
 	}
 	
 	public String getDescription() {
 		return description;
 	}
 	
 	public void setQueueName(String queueName){
 		
 		this.queueName = queueName;
 	}
 
 	public Exchange toExchange(){
 		
 		return new Exchange(
 				this.getName(), 
 				this.getHostName(), 
 				this.getVirtualHost(), 
 				this.getPort(), 
 				this.getRoutingKey(), 
 				this.getQueueName(),
 				this.getExchangeType(), 
 				this.getIsDurable(), 
 				this.getIsAutoDelete(), 
 				this.getArguments());
 	}
 	
 	@Override
 	public String toString() {
 		return "ExtendedExchange [id=" + id + ", arguments=" + arguments
 				+ ", exchangeType=" + exchangeType + ", hostName=" + hostName
 				+ ", isAutoDelete=" + isAutoDelete + ", isDurable=" + isDurable
 				+ ", name=" + name + ", port=" + port + ", queueName="
 				+ queueName + ", routingKey=" + routingKey + ", virtualHost="
 				+ virtualHost + "]";
 	}
 	
 	
 }
