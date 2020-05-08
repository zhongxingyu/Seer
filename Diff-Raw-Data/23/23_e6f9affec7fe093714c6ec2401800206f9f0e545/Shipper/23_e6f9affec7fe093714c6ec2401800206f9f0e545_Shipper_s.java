 package org.devnull.statsd_client;
 
 import org.codehaus.jackson.JsonNode;
 
 /**
  * Created with IntelliJ IDEA.
  * User: dhawth
  * Date: 10/2/13
  * Time: 9:26 PM
  * To change this template use File | Settings | File Templates.
  */
 public interface Shipper extends Runnable
 {
 	//
 	// this assumes that timers are all sent as they occur
 	// from within the StatsObject class as a result of registering the
 	// UDPStatsdClient object with the StatsObject class.
 	// So the only stats we need to box up and ship out are the counters.
 	//
	public void configure(final JsonNode node) throws Exception;
 
 	public void run();
 
 	public void shutdown();
 }
