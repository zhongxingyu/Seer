 package ru.ifmo.cis.mrp.back.ejb;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.Resource;
 import javax.ejb.*;
 import javax.jms.*;
 
 /**
  * User: Igor
  * Date: 16.11.11
  * Time: 16:12
  */
 @Startup
 @Singleton
 public class BackTimer {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(BackTimer.class);
 
     private static final String BACK_TIMER = "Back_timer";
 
     @Resource
     private SessionContext sessionContext;
 
     @Resource(mappedName = "java:/ConnectionFactory")
     private ConnectionFactory connectionFactory;
 
     @Resource(mappedName = "/topic/tickTopic")
    private Topic tickTopic;
 
     @EJB
     private SequenceOptimizer sequenceOptimizer;
 
     @PostConstruct
     public void scheduleTimer() {
         LOGGER.info("[Timer] Scheduling timer after 5 seconds...");
         for (Timer t : sessionContext.getTimerService().getTimers()) {
             t.cancel();
         }
         sessionContext.getTimerService().createIntervalTimer(5000, 5000, new TimerConfig(BACK_TIMER, true));
     }
 
     @Timeout
     public void timeout(final Timer timer) {
         if ((timer.getInfo() != null) && (timer.getInfo().equals(BACK_TIMER))) {
             LOGGER.info("[Back] Tick");
            TopicConnection con;
            TopicSession ses;
            TopicPublisher sender;
             try {
                TopicConnectionFactory qcf = (TopicConnectionFactory) connectionFactory;
                con = qcf.createTopicConnection();
                ses = con.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
                sender = ses.createPublisher(tickTopic);
                 ObjectMessage msg = ses.createObjectMessage(null);
                 //ObjectMessage msg = ses.createObjectMessage(sequenceOptimizer.getSequence().toArray());
                sender.publish(msg);
                 sender.close();
                 ses.close();
                 con.close();
             } catch (JMSException e) {
                 LOGGER.error("Exception while sending order from front to back", e);
             }
         }
     }
 }
