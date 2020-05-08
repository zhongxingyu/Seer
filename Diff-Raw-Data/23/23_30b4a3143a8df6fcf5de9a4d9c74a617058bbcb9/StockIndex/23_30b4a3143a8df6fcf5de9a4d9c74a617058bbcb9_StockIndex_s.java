 package pl.edu.agh.iosr.brokers;
 
 import java.io.Serializable;
 import java.math.BigDecimal;
 
 public class StockIndex implements Serializable {
 	private static final long serialVersionUID = 1L;
 
 	private Long id;
 	
 	private String key;
 	private String name;
 
 	private BigDecimal value;
 	private BigDecimal change;
 	private long timestamp;
 	
 	public StockIndex(){
 	}
 	
 	public StockIndex(String key, String name, BigDecimal value, BigDecimal change, long timestamp) {
 		this.key = key;
 		this.name = name;
 		this.value = value;
 		this.change = change;
 		this.timestamp = timestamp;
 	}
 	public StockIndex(String key, String name, String value, String change, long timestamp) {
 		this(key, name, new BigDecimal(value), new BigDecimal(change), timestamp);
 	}
 	public Long getId() {
 		return id;
 	}
 	public void setId(Long id) {
 		this.id = id;
 	}
 	public String getKey() {
 		return key;
 	}
 	public void setKey(String key) {
 		this.key = key;
 	}
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 	public BigDecimal getValue() {
 		return value;
 	}
 	public void setValue(BigDecimal value) {
 		this.value = value;
 	}
 	public BigDecimal getChange() {
 		return change;
 	}
 	public void setChange(BigDecimal change) {
 		this.change = change;
 	}
 	public long getTimestamp() {
 		return timestamp;
 	}
 	public void setTimestamp(long timestamp) {
 		this.timestamp = timestamp;
 	}
 	
 	public String toString() {
		return "StockIndex[name="+key+",value="+value+",timestamp="+timestamp+"]";
 	}
 }
