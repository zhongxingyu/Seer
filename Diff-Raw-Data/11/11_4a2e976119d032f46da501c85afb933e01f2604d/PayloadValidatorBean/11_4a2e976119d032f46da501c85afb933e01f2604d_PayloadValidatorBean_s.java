 package gov.nih.nci.caxchange.servicemix.bean.validation;
 
 import gov.nih.nci.caXchange.CaxchangeErrors;
 import gov.nih.nci.caxchange.jdbc.CaxchangeMetadata;
 import gov.nih.nci.caxchange.persistence.CaxchangeMetadataDAO;
 import gov.nih.nci.caxchange.persistence.DAOFactory;
 import gov.nih.nci.caxchange.servicemix.bean.CaXchangeMessagingBean;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import javax.jbi.messaging.Fault;
 import javax.jbi.messaging.MessageExchange;
 import javax.jbi.messaging.MessagingException;
 import javax.jbi.messaging.NormalizedMessage;
 import javax.xml.validation.Schema;
 
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 import org.apache.servicemix.jbi.util.MessageUtil;
 import org.w3c.dom.Node;
 /**
  * MessageExchange for validating caXchange payloads. This uses the PayloadValidator, GMESchemaFactory
  * to validate the input payloads. The payloads are extracted from the caXchang request for validation.
  * 
  * @author marwahah
  *
  */
 public class PayloadValidatorBean extends CaXchangeMessagingBean {
 	
     private Logger logger = LogManager.getLogger(PayloadValidatorBean.class);
     private PayloadValidator payloadValidator = null;
     private GMESchemaFactory gmeSchemaFactory = null;
     static private java.util.Map<String, CaxchangeMetadata> metadataCache = new java.util.HashMap<String, CaxchangeMetadata>(6);
     
 
 	public PayloadValidator getPayloadValidator() {
 		return payloadValidator;
 	}
 
 	public void setPayloadValidator(PayloadValidator payloadValidator) {
 		this.payloadValidator = payloadValidator;
 	}
 
 	public void processMessageExchange(MessageExchange exchange)
 			throws MessagingException {
         logger.debug("Received exchange: " + exchange);
         NormalizedMessage in = exchange.getMessage("in");
         NormalizedMessage out = exchange.createMessage();
         MessageUtil.transfer(in,out);
         try {
         	  String messageType = caXchangeDataUtil.getMessageType();
               String namespace = getPayloadNamespace(messageType);
               Schema schema = null;
               if (gmeSchemaFactory == null) {
             	  throw new PayloadValidationException("GME schema factory not initialized for payload validation.");
               }
               if (namespace != null){
         	     schema = gmeSchemaFactory.getSchema(namespace);
               } else {
             	  throw new PayloadValidationException("Namespace not configured for this message type:"+messageType+". Please configure the namespace to get the validating schema from GME.");
               }
               if (schema != null){
                  Node payload = caXchangeDataUtil.getBusinessPayload();
                  long timeBefore = new java.util.Date().getTime();
         	     payloadValidator.validatePayload(payload, schema);
         	     long timeAfter = new java.util.Date().getTime();
        	     logger.warn("Time for validation:"+(timeAfter-timeBefore));
               }else {
             	  throw new PayloadValidationException("Schema not found for namespace:"+namespace+"  and message type:"+messageType+" GME url:"+gmeSchemaFactory.getGMEGridServiceLocation());
               }
         }catch(PayloadValidationException pve) {
             logger.error("Payload validation error.", pve);
             logger.debug(escape(pve.getMessage()));
 			Fault fault = getFault(CaxchangeErrors.PAYLOAD_VALIDATION_EXCEPTION,"Invalid payload error: "+escape(pve.getMessage()), exchange);
 			exchange.setFault(fault);
 			channel.send(exchange);
 			return;       	
 		}catch(Exception e){
             logger.error("An error occurred getting metadata.", e);
 			Fault fault = getFault(CaxchangeErrors.PAYLOAD_VALIDATION_EXCEPTION,"Error occurred validating payload."+e.getMessage(), exchange);
 			exchange.setFault(fault);
 			channel.send(exchange);
 			return;
         }
         exchange.setMessage(out, "out");
         channel.send(exchange);		
 
 	}
 	
 	public String getPayloadNamespace(String messageType) throws Exception {
 		CaxchangeMetadata metadata = metadataCache.get(messageType);
 		if (metadata == null){
 		    CaxchangeMetadataDAO metadataDAO = DAOFactory.getCaxchangeMetadataDAO();
 		    metadata =metadataDAO.getMetadata(messageType);
 		    metadataCache.put(messageType, metadata);
 		}
 		if (metadata != null) {
 			return metadata.getPayloadNamespace();
 		}
 		return null;
 	}
 	
 	
 	public GMESchemaFactory getGmeSchemaFactory() {
 		return gmeSchemaFactory;
 	}
 
 	public void setGmeSchemaFactory(GMESchemaFactory gmeSchemaFactory) {
 		this.gmeSchemaFactory = gmeSchemaFactory;
 	}
 
 	
 	public String escape(String content)
 	{
 	    StringBuffer buffer = new StringBuffer();
 	    for(int i = 0;i < content.length();i++)
 	    {
 	       char c = content.charAt(i);
 	       if(c == '<')
 	          buffer.append("&lt;");
 	       else if(c == '>')
 	          buffer.append("&gt;");
 	       else if(c == '&')
 	          buffer.append("&amp;");
 	       else if(c == '"')
 	          buffer.append("&quot;");
 	       else if(c == '\'')
 	          buffer.append("&apos;");
 	       else
 	          buffer.append(c);
 	    }
 	    return buffer.toString();
 	}	
 
 }
