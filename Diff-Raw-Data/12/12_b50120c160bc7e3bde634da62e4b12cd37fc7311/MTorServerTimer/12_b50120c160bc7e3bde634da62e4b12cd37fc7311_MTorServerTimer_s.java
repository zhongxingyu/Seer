 package nl.bhit.mtor.server.timer;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.BeanFactory;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.stereotype.Component;
 
 @Component(
 		value = "mTorServerTimer")
 public class MTorServerTimer {
 	protected final Log log = LogFactory.getLog(MTorServerTimer.class);
 
 	protected static String[] getConfigLocations() {
 		return new String[] { "classpath:/applicationContext-resources.xml", "classpath:/applicationContext-dao.xml",
 				"classpath:/applicationContext-service.xml" };
 	}
 
 	/**
 	 * is called from the timer.
 	 */
 	public void process() {
 		log.debug("starting up the MTorServerTimer processor.");
		final BeanFactory factory = new ClassPathXmlApplicationContext(getConfigLocations());
		AlertSender alertSender = (AlertSender) factory.getBean("alertSender");
 		alertSender.process();
		alertSender = null;
		System.gc();
 	}
 }
