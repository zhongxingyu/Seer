 package org.dsa.amq.amqstats.camel;
 
 
 import java.io.File;
 import java.util.Map;
 import java.util.Set;
 
 import javax.management.ObjectName;
 
 import org.apache.camel.Exchange;
 import org.apache.camel.Handler;
 import org.apache.camel.Message;
import org.apache.camel.Processor;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.dsa.amq.amqstats.jmx.JmxCamel;
 import org.dsa.amq.amqstats.jmx.MessageSizeStatistics;
 
 public class AddMessageSizeToStats {
 	private static final Log log = LogFactory.getLog(AddMessageSizeToStats.class);
 	private static final String FILE_LENGTH = "camelfilelength";
 
 	JmxCamel jmxCamel;
 	String mBeanName;
 
 	public AddMessageSizeToStats() {
 		
 	}
 	
 	public AddMessageSizeToStats(JmxCamel jmxCamel, String name) {
 		this.jmxCamel = jmxCamel;
 		this.mBeanName = MessageSizeStatistics.MESSAGESTATS_MBEAN + name;
 		//Check whether there is a file size bean already registered
 		Set<ObjectName> names = this.jmxCamel.getBeanNames(this.mBeanName);
 		if (names == null || names.size() == 0) {
 			//Add Mbean to server
 			MessageSizeStatistics stats = new MessageSizeStatistics();
 			try {
 				this.jmxCamel.registerMBean(stats, new ObjectName(this.mBeanName));
 			} catch (Exception e) {
 				log.error("Failed to register new MessageSizeStatistics MBean: " + this.mBeanName);
 			}
 		}
 	}
 
 	private void setSize(long length) {
 		if (length != -1) {
 			this.jmxCamel.invokeCommand(this.mBeanName, "addSize", 
 					new Object[] {new Long(length) },
 					new String[]{"long"});
 		}
 	}
 	
	@Handler
	public void addFileSize(File file) {
		long length = file.length();
		log.debug("Got file length: " + length);
		this.setSize(length);
	}
 	
 	@Handler
 	public void process(Exchange exchange) {
 		log.debug("process: start()");
 		Message message = exchange.getIn();
 		if (message == null) {
 			log.warn("Passed an exchange without an In message - cannot add stats");
 		}
 		Map<String, Object> headers = message.getHeaders();
 		try {
 				boolean gotSize = false;
 				if (headers != null && headers.keySet() != null) {
 					if (log.isDebugEnabled()) {
 						for (String key : headers.keySet()) {
 							Object val = headers.get(key);
 							log.debug(String.format("Got Header %s=%s", key, val == null ? null : val.toString()));
 						}
 					}
 					Object obj = headers.get(FILE_LENGTH);
 					if (obj != null) {
 						long length = ((Long)obj).longValue();
 						log.debug("Got message length from header: " + length);
 						this.setSize(length);
 						gotSize = true;
 					}
 				} else {
 					log.warn("Received message without headers");
 				}
 				if (!gotSize) {
 					//Use body
 					byte[] body = message.getBody(byte[].class);
 					if (body != null) {
 						this.setSize(body.length);
 						log.debug("Got message body length: " + body.length);
 						gotSize = true;
 					}
 				}
 		} catch (Exception e) {
 			log.error("Caught exception processing exchange: ", e);
 		}
 		log.debug("process: end()");
 	}
 }
