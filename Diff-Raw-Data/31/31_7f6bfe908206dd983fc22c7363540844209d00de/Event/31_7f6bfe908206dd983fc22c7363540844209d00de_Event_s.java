 package com.cs310.ubc.meetupscheduler.server;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Map;
 
 import javax.jdo.annotations.IdGeneratorStrategy;
 import javax.jdo.annotations.IdentityType;
 import javax.jdo.annotations.PersistenceCapable;
 import javax.jdo.annotations.Persistent;
 import javax.jdo.annotations.PrimaryKey;
 
 /**
  * Persistent class to hold data about events.
  */
 @PersistenceCapable(identityType = IdentityType.APPLICATION)
 public class Event extends DataObject {
 
 	public Event(Map<String, String> myFields) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
 		super(myFields);
 		creation_date = new Date(); //Keep track of creation_date for recent events table.
 	}
 	
 	//Can be used to access event fields.
 	public enum EventField {
 		ID {
 		    public String toString() {
 		        return "id";
 		    }
 		},
 		 
 		NAME {
 		    public String toString() {
 		        return "name";
 		    }
 		},
 		PID {
 		    public String toString() {
 		        return "park_id";
 		    }
 		},
 		ATTND {
 		    public String toString() {
 		        return "num_attending";
 		    }
 		},
 		ATTND_NAMES {
 			public String toString() {
 				return "attending_names";
 			}
 		},
		CREATOR {
 		    public String toString() {
		        return "creator";
 		    }
 		},
 		CAT {
 		    public String toString() {
 		        return "category";
 		    }
 		},
 		DATE {
 			public String toString() {
 				return "date";
 			}
 		},
 		
 		START {
 			public String toString() {
 				return "start_time";
 			}
 		},
 		END {
 			public String toString() {
 				return "end_time";
 			}
 		}
 	}
 	
 	@PrimaryKey
 	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
 	protected Long id; //Automatically generated pk
 	@Persistent
 	protected String park_id;
 	@Persistent
 	protected String name;
 	@Persistent
 	protected String num_attending;
 	@Persistent
	protected String creator;
 	@Persistent
 	protected String category;
 	@Persistent
 	protected String date;
 	@Persistent
 	protected String start_time;
 	@Persistent
 	protected String end_time;
 	@Persistent
 	public ArrayList<String> attending_names;
 	@Persistent
 	protected Date creation_date;
 	
 	public static String getDefaultOrdering() {
 		return "creation_date";
 	}
 }
