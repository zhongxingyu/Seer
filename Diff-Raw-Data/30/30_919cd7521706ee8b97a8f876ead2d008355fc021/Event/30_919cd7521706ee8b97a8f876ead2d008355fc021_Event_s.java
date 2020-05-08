 package ch.cern.atlas.apvs.domain;
 
 import java.io.Serializable;
 import java.util.Date;
 
 public class Event implements Message, Serializable {
 
 	private static final long serialVersionUID = -5549380949230727273L;
 	
 	private String ptuId;
 	private String name;
 	private String eventType;
 	private Number value;
 	private Date date;
 
 	public Event() {
 	}
 
 	public Event(String ptuId, String name, String eventType, Number value, Date date) {
 		this.ptuId = ptuId;
 		this.name = name;
 		this.eventType = eventType;
 		this.value = value;
 		this.date = date;
 	}
 
     @Override
 	public String getPtuId() {
 		return ptuId;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public String getEventType() {
 		return eventType;
 	}
 
 	public Number getValue() {
 		return value;
 	}
 
 	public Date getDate() {
 		return date;
 	}
 
 	@Override
 	public String getType() {
		return "event";
 	}
 }
