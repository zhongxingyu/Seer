 package nl.bhit.mtor.server.timer;
 
import nl.bhit.mtor.client.MessageServiceSender;

 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.BeanFactory;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.stereotype.Component;
 
 @Component(
 		value = "mTorServerTimer")
 public class MTorServerTimer {
	protected final Log log = LogFactory.getLog(MessageServiceSender.class);
 
 	protected static String[] getConfigLocations() {
 		return new String[] { "classpath:/applicationContext-resources.xml", "classpath:/applicationContext-dao.xml",
				"classpath:/applicationContext-service.xml", "classpath:/applicationContext-timer.xml", "classpath:**/applicationContext*.xml" };
 	}
 
 	/**
 	 * is called from the timer.
 	 */
 	public void process() {
		log.debug("starting up the timed processor.");
 		final BeanFactory factory = new ClassPathXmlApplicationContext(getConfigLocations());
 		AlertSender alertSender = (AlertSender) factory.getBean("alertSender");
 		alertSender.process();
 	}
 }
