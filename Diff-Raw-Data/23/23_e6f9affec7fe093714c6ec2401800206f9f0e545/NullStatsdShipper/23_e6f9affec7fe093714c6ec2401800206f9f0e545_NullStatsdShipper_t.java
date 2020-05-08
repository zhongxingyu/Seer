 package org.devnull.statsd_client;
 
 import org.apache.log4j.*;
 import org.codehaus.jackson.JsonNode;
 
 /**
  * Created with IntelliJ IDEA.
  * User: dhawth
  * Date: 10/3/13
  * Time: 10:23 AM
  * To change this template use File | Settings | File Templates.
  */
 public class NullStatsdShipper implements Shipper
 {
 	private static final Logger log = Logger.getLogger(NullStatsdShipper.class);
 
 	private final StatsObject so = StatsObject.getInstance();
 	private boolean done = false;
 
 	public NullStatsdShipper() throws Exception
 	{
 	}
 
	public void configure(final String node) throws Exception
 	{
 	}
 
 	public void run()
 	{
 		while(!done)
 		{
 			try
 			{
 				Thread.sleep(1000);
 				so.clear();
 			}
 			catch (InterruptedException ie)
 			{
 			}
 		}
 	}
 
 	public void shutdown()
 	{
 		done = true;
 	}
 }
