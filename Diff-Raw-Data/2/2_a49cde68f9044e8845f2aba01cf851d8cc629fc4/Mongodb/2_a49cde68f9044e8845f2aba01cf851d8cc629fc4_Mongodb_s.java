 /**
  **/
 package com.admob.rocksteady.reactor;
 
 import org.springframework.beans.factory.annotation.Autowired;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.admob.rocksteady.util.MongodbInterface;
 import com.espertech.esper.client.EventBean;
 import com.espertech.esper.client.UpdateListener;
 import com.espertech.esper.client.PropertyAccessException;
 
 
 /**
  *
  * @author Implement Esper UpdateListener to be used when event is triggered.
  *         This is testing the base form.
  *
  */
 public class Mongodb implements UpdateListener {
     private static final Logger logger = LoggerFactory.getLogger(Mongodb.class);
 
     private String type;
     private String tag;
     private String cname;
 
     private String suffix;
 
     @Autowired
     private MongodbInterface mongodbInterface;
 
 
     public void setType(String type) {
 	this.type = type;
     }
 
     public void setTag(String tag) {
 	this.tag = tag;
     }
 
     public void setCname(String cname) {
 	this.cname = cname;
     }
 
     public String getTag() {
 	return tag;
     }
 
 
     public String getSuffix() {
 	return suffix;
     }
 
     public void setSuffix(String suffix) {
 	this.suffix = suffix;
     }
 
     /**
      * Handle the triggered event
      *
      * @param newEvents the new events in the window
      * @param oldEvents the old events in the window
      */
     public void update(EventBean[] newEvents, EventBean[] oldEvents) {
 
 	if (newEvents == null) {
 	    return;
 	}
 	for (EventBean newEvent : newEvents) {
 	    try {
 		String retention;
 		String app;
 		String name;
 		String colo;
 		String value;
 		String hostname;
 		String cluster;
 		String timestamp;
 		retention = newEvent.get("retention").toString();
 		app = newEvent.get("app").toString();
 		// get name
 		if (cname != null) {
 		    name = newEvent.get("name").toString();
 		} else {
 		    name = cname;
 		}
 		colo = newEvent.get("colo").toString();
 		String[] splitName = name.split("\\.");
 		if (splitName.length > 2) {
 		    StringBuffer sb = new StringBuffer();
 		    for (int i = 0; i < splitName.length-1; i++) {
 			if (sb.length() > 0) {
 			    sb.append(".");
 			}
 			sb.append(splitName[i]);
 		    }
 
 		    colo = sb.toString();
 		    cluster = splitName[splitName.length-1];
 		} else {
 		    cluster = new String("");
 		}
 
 		value = newEvent.get("value").toString();
 		try {
 		    timestamp = newEvent.get("timestamp").toString();
 		} catch (Exception e) {
 		    timestamp = null;
 		}
 
 		if ( (type != null) && (type.equals("uniq_host"))) {
 		    hostname = newEvent.get("hostname").toString();
 		} else {
 		    hostname = new String("");
 		}
 
 		if (retention.isEmpty()) {
 		    retention = new String("");
 		}
 		if (suffix == null) {
 		    suffix = new String("");
 		}
 
		logger.debug("mogodb string: " + retention + "." + app + "." + name + "." + colo + "." + cluster + "." + hostname  + "." + suffix + " " + value + " " + timestamp);
 
 		// Send the data
 		mongodbInterface.send(mongodbInterface.mongodbObject(retention, app, name, colo, cluster, hostname, suffix, value, timestamp));
 	    } catch (Exception e) {
 		logger.error("Problem with sending metric to mongodb: " +
 			     e.toString());
 	    }
 	}
     }
 }
