 package org.rotarysource.inputadapter.jmsinputadapter;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.jms.JMSException;
 import javax.jms.Message;
 import javax.jms.MessageListener;
 import javax.jms.TextMessage;
 
 import org.rotarysource.core.CepEngine;
 import org.rotarysource.events.BasicEvent;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.jms.support.converter.MessageConversionException;
 import org.springframework.jms.support.converter.MessageConverter;
 import org.springframework.jmx.export.annotation.ManagedAttribute;
 import org.springframework.jmx.export.annotation.ManagedOperation;
 import org.springframework.jmx.export.annotation.ManagedResource;
 
 
 
 /** 
  * This class provides the Input Adapter for ProdEvent XML messages
  * through a JMS queue communication.
  * 
  * XMLInputAdapter implements a MessageListener that get XML events from
  * the queue, transform it in a ProdEvent object and injects it in the
  * Esper core.
  * 
  * Spring configuration for this class is definded in StatementProcessor-config.xml
  * @author J.L. Canales
  */ 
 @ManagedResource(objectName="org.rotarysource.mbean.inputadapters:name=BasicEventInputAdapter", description="XML Input Adapter managed counters", log=true,
 	    logFile="jmx.log", currencyTimeLimit=15, persistPolicy="OnUpdate", persistPeriod=200,
 	    persistLocation="foo", persistName="bar")
 public class BasicEventIA implements MessageListener
 {
     /** 
      * Apache commons login logger instance
      */	
 	private static Logger log = LoggerFactory.getLogger(BasicEventIA.class);
     
     /** 
      * Reference to Cep Engine that events will be sent
      */ 
     private CepEngine cepEngine;
     /** 
      * MessageConverter to convert JMS Messages in ProdEvent objects
      */
     private MessageConverter    msgConverter;
 
     /** 
      * For JMX management. Number of processed Messages 
      */
     private int        			countMessages;
     /** 
      * For JMX management. Number of bad formed Messages 
      */
     private int        			countBadFormed;
 
     /** 
      * Create a new XMLInputAdapter, given a StatementProcessor
      * @param aiStProcessor Statement processor class to get the esper instance that process events.
      */
 	public BasicEventIA(CepEngine aiCepEngine)
     {
         this.cepEngine      = aiCepEngine;
         this.countMessages  = 0;
         this.countBadFormed = 0;
     }
     
     /** 
      * Implements MessageListener onMessage method. This operation is called by
      * spring every time a message is available in the queue.
      * 
      * @param message JMS message received by the JMS listener
      */    
     @Override
 	public void onMessage(Message message) {
 
     	TextMessage bytesMsg = (TextMessage) message;
     	BasicEvent 	basEvent = new BasicEvent();
     	
     	try {
     		basEvent = (BasicEvent) msgConverter.fromMessage(bytesMsg);
 		} catch (MessageConversionException e) {
 			String 	  errorText  = "Msg Converter error. Bad Formed message";
 			BasicEvent errorEvent = new BasicEvent();
             Date      date       = new Date();  
 			log.error(errorText, e);
 			
 			errorEvent.setEventId(Integer.toString((int)date.getTime()));
 			errorEvent.setEventCode("EX001");
 			errorEvent.setEventTimestamp(date);
 			errorEvent.setEventType("ERROR");
 			errorEvent.setSystemId("cepInputAdapter");
             
             Map<String, String> compDataMap = new HashMap<String, String>();
             compDataMap.put("ERRTEXT", errorText);
             
             errorEvent.setCompData( (HashMap<String, String>) compDataMap);
 			
 	        cepEngine.getCepEngine().getEPRuntime().sendEvent(errorEvent);
 	        countBadFormed++;
 	        log.error(errorText);
 	        log.debug("Bad formed message {}", bytesMsg);
 
 		} catch (JMSException e) {
 			String 	  errorText  = "JMS Conection problem";
 			log.error(errorText, e);
 		}
 
        cepEngine.getCepEngine().getEPRuntime().sendEvent(basEvent);
        countMessages++;
 	}
 
     
 	/**
 	 * Return Menssage Converter Object set up for this item
 	 * @return MessageConverter Message Converter
 	 */
     public MessageConverter getMsgConverter() {
 		return msgConverter;
 	}
 
     /**
      * Set the Message Converter needed to convert from/to EventProd xml events
      * into ProdEvent objects
      * @param aiMsgConverter Message Converter for this item
      */
 	public void setMsgConverter(MessageConverter aiMsgConverter) {
 		this.msgConverter = aiMsgConverter;
 	}    
  
 	/**
 	 * Return the number of message processed by XMLInputAdapter instance
 	 * @return int Message nunber
 	 */
     @ManagedAttribute(description="Processed Messages counter",persistPeriod=300)
 	public int getCountMessages() {
         return countMessages;
 	}
     
     /**
      * Return the Message Number Failed while they was been processed 
      * by XMLInputAdapter
      * @return int Bad Formed Messages Counter
      */
     @ManagedAttribute(description="Bad Formed Messages counter",persistPeriod=300)
 	public int getCountBadFormed() {
         return countBadFormed;
 	}
     
     /**
      * Reset Statistical counters for This item
      */
     @ManagedOperation(description="Reset all Message counters")
 	public void resetCounters(){
         this.countMessages  = 0;
         this.countBadFormed = 0;	
 	}
 
 	
 
 }
