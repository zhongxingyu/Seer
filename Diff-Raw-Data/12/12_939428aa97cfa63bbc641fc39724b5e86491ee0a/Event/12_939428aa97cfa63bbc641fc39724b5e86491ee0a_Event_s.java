 package com.example.formidable;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.codehaus.jackson.annotate.JsonProperty;
 import org.ektorp.support.CouchDbDocument;
 
 public class Event extends CouchDbDocument implements Comparable<Event> {
 	private static final long serialVersionUID = 1L;
 	
 	@JsonProperty
 	private int epoch;
 	
 	@JsonProperty
 	private String recordId;
 	
 	@JsonProperty
 	private Map<String, Object> data = new HashMap<String, Object>();
 	
 	public Event(int epoch, String recordId, Map<String, Object> data) {
 		this.epoch = epoch;
 		this.recordId = recordId;
         this.data.putAll(data);
 	}	
 	
 	public int getEpoch() {
 		return epoch;
 	}
 	
 	public String getRecordId() {
 		return recordId;
 	}
 
     public Event appliedOnto(Event older) {
        Map<String, Object> merged = new MapMerger().of(older.data, this.data);
         return new Event(this.epoch, this.recordId, merged);
     }
 
     @Override
     public int compareTo(Event another) {
         return this.epoch - another.epoch;
     }
 }
