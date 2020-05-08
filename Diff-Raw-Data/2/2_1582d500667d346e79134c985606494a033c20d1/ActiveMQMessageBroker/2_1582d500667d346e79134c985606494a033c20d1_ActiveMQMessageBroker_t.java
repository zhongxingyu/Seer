 package eu.alertproject.iccs.events.api;
 
 import eu.alertproject.iccs.events.activemq.TextMessageCreator;
 import org.apache.activemq.command.ActiveMQTopic;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.jms.core.JmsTemplate;
 
 import javax.annotation.PostConstruct;
 import javax.jms.JMSException;
 import javax.jms.Message;
 import javax.jms.MessageListener;
 import javax.jms.TextMessage;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.concurrent.atomic.AtomicReference;
 
 /**
  * User: fotis
  * Date: 16/12/11
  * Time: 22:14
  */
 public class ActiveMQMessageBroker implements MessageListener{
 
     public static final String SEND = "send";
     public static final String TOTAL = "total";
     private Logger logger = LoggerFactory.getLogger(ActiveMQMessageBroker.class);
 
 
     private boolean processDisabled= false;
     private boolean recordIncoming = true;
     private boolean recordOutgoing = true;
 
     private JmsTemplate jmsTemplate;
 
     private Map<String,AbstractActiveMQHandler> listenerMap = new HashMap<String, AbstractActiveMQHandler>();
 
     private Map<String,AtomicInteger> listenerCounts;
 
     private final AtomicInteger eventId = new AtomicInteger();
     private final AtomicInteger sequence = new AtomicInteger();
 
 
     @PostConstruct
     public void init(){
 
        logger.debug("void init([]) Initializing instance");

         Set<String> strings = listenerMap.keySet();
         for(String key:strings){
             logger.info("Handling listener for {} ",key);
         }
 
 
     }
 
     @Override
     public void onMessage(Message message) {
 
 
         logger.trace("void onMessage() {} ",message);
 
         if(!(message instanceof TextMessage)){
 
             logger.warn("I can't handle this message {} ",message);
             return;
         }
 
 
         //save
         //store the message
         FileOutputStream output = null;
         try {
 
             TextMessage tm = (TextMessage) message;
             String topic = ((ActiveMQTopic)tm.getJMSDestination()).getPhysicalName();
 
 
             String messageStr = tm.getText();
             int messageCount = listenerCounts.get(SEND).get();
 
             if(StringUtils.isEmpty(messageStr)){
                 logger.warn("Message {} was empty, not creating file ", messageCount);
                 return;
             }
 
             if(recordIncoming){
                 String fileName = String.format("/tmp/iccs/in-%s-%s.txt",
                         topic,
                         messageCount);
 
                 File file = new File("/tmp/iccs");
                 if(!file.isDirectory()){
                     file.mkdir();
                 }
 
                 logger.debug("Message written to {} ",fileName);
 
                 output = new FileOutputStream(new File(fileName));
                 IOUtils.write(messageStr, output);
             }
 
             int count = listenerCounts.get(SEND).incrementAndGet();
 
             if(!StringUtils.isEmpty(messageStr) && !processDisabled){
                logger.debug("Processing message {} ",count);
                logger.trace("void onMessage([message]) {}",messageStr);
 
                 //check if a listener is registered
                 String realTopic = isListeningFor(topic);
                 if(listenerMap != null && realTopic !=null ){
                     listenerCounts.get(realTopic).incrementAndGet();
                     listenerMap.get(realTopic).process(this,message);
                 }
             }
 
         } catch (IOException e) {
             logger.warn("Couldn't handle and translate the message content {}",e);
         } catch (JMSException e) {
             logger.warn("Couldn't retrieve the message content {}", e);
         } finally {
 
             IOUtils.closeQuietly(output);
             listenerCounts.get(TOTAL).incrementAndGet();
         }
     }
 
 
     public void sendTextMessage(String topic, String message){
         sendMessage(topic, new TextMessageCreator(message));
     }
 
     public void sendMessage(String topic, DataMessageCreator message){
 
         if(recordOutgoing){
 
             String rawMessage = message.getRawData();
             try {
 
                 String fileName = String.format("/tmp/iccs/out-%s-%s.txt",
                         topic,
                         listenerCounts.get(SEND).get());
 
 
                 File file = new File("/tmp/iccs");
                 if(!file.isDirectory()){
                     file.mkdir();
                 }
 
                 IOUtils.write(rawMessage, new FileOutputStream(new File(fileName)));
             } catch (IOException e) {
                 logger.warn("Couldn't save the outgoing message ");
             }
         }
 
         if(     !StringUtils.isEmpty(topic)
             &&  message !=null
             &&  !processDisabled){
 
             listenerCounts.get(SEND).incrementAndGet();
 
             jmsTemplate.send(
                     topic,
                     message
             );
 
             logger.trace("void sendMessage([topic, message]) Sent message {} to topic {} ",
                     eventId.get(), topic);
 
         }
 
     }
 
     public String isListeningFor(String topic){
 
         if(listenerMap == null){
             return null;
         }
 
         Set<String> strings = listenerMap.keySet();
 
         for(String key : strings){
 
             if(topic.matches(key)){
                 return key;
             }
 
 
         }
 
         return null;
 
     }
 
     public boolean isProcessDisabled() {
         return processDisabled;
     }
 
     public void setProcessDisabled(boolean processDisabled) {
         this.processDisabled = processDisabled;
     }
 
     public boolean isRecordIncoming() {
         return recordIncoming;
     }
 
     public void setRecordIncoming(boolean recordIncoming) {
         this.recordIncoming = recordIncoming;
     }
 
     public boolean isRecordOutgoing() {
         return recordOutgoing;
     }
 
     public void setRecordOutgoing(boolean recordOutgoing) {
         this.recordOutgoing = recordOutgoing;
     }
 
 
     public Map<String, AbstractActiveMQHandler> getListenerMap() {
         return listenerMap;
     }
 
     public void setListenerMap(Map<String, AbstractActiveMQHandler> listenerMap) {
         this.listenerMap = listenerMap;
         this.listenerCounts= new HashMap<String, AtomicInteger>();
 
         listenerCounts.put(TOTAL,new AtomicInteger(0));
         listenerCounts.put(SEND,new AtomicInteger(0));
         Set<String> strings = listenerMap.keySet();
         for(String key:strings){
 
 
             if(!listenerCounts.containsKey(key)){
                 listenerCounts.put(key,new AtomicInteger(0));
             }
 
         }
     }
 
     public Map<String, AtomicInteger> getListenerCounts() {
         return listenerCounts;
     }
 
     public JmsTemplate getJmsTemplate() {
         return jmsTemplate;
     }
 
     public void setJmsTemplate(JmsTemplate jmsTemplate) {
         this.jmsTemplate = jmsTemplate;
     }
 
     public Integer requestSequence(){
         return sequence.incrementAndGet();
     }
 
     public Integer requestEventId(){
         return eventId.incrementAndGet();
     }
 }
