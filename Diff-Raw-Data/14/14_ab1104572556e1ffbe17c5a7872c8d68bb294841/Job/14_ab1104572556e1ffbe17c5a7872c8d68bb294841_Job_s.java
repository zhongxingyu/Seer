 package sbc.job;
 
 import java.io.Serializable;
 import java.util.UUID;
 
 import org.mozartspaces.capi3.Index;
 import org.mozartspaces.capi3.Queryable;
 
 @Queryable
 public class Job implements Serializable {
 	private static final long serialVersionUID = -4128559480849344740L;
 	
 	@Index
 	private UUID uuid;
 	private int quantity;
 	private Configuration configuration;
 	private boolean started;
 
 	public Job(){
 		this.uuid = UUID.randomUUID();
 	}
 	
 	public Job(Configuration configuration, int quantity) {
 		this();
 		this.configuration = configuration;
 		this.quantity = quantity;
 	}
 	
 	public UUID getUuid() {
 		return uuid;
 	}
 
 	public int getQuantity() {
 		return quantity;
 	}
 
 	public void setQuantity(int quantity) {
 		this.quantity = quantity;
 	}
 
 	public Configuration getConfiguration() {
 		return configuration;
 	}
 
 	public void setConfiguration(Configuration configuration) {
 		this.configuration = configuration;
 	}
 
 	public boolean isStarted() {
 		return started;
 	}
 
 	public void setStarted(boolean started) {
 		this.started = started;
 	}
 	
 	@Override
 	public String toString(){
 		String s = "uuid: " + getUuid();
 		s = s + "\nquantity: " + getQuantity();
 		s = s + "\nstarted: " + isStarted();
 		s = s + "\nconfiguration: \n" + getConfiguration().toString();
 		return s;
 	}
 }
