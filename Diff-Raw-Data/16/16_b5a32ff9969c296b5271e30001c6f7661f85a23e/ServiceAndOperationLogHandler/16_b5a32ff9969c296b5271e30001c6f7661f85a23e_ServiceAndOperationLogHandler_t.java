 package dynamiclogging.ws.handler;
 
 import java.io.ByteArrayOutputStream;
 import java.util.Set;
 
 import javax.xml.namespace.QName;
 import javax.xml.ws.handler.MessageContext;
 import javax.xml.ws.handler.soap.SOAPHandler;
 import javax.xml.ws.handler.soap.SOAPMessageContext;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class ServiceAndOperationLogHandler implements SOAPHandler<SOAPMessageContext>{
 
     private static final String UNKNOWN = "UNKNOWN";
    private static final String OUT = "out";
    private static final String IN = "in";
 
     private static final Logger LOGGER = LoggerFactory.getLogger(ServiceAndOperationLogHandler.class);
 
     @Override
     public boolean handleMessage(SOAPMessageContext ctx) {
 
         ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
         try {
             ctx.getMessage().writeTo(byteArrayOutputStream);
         } catch (Exception e) {
             LOGGER.error("HandleMessage failed", e);
         }
         
         final QName service = (QName) ctx.get(SOAPMessageContext.WSDL_SERVICE);
         final QName operation = (QName) ctx.get(SOAPMessageContext.WSDL_OPERATION);
        final Boolean isOutboundMessage = (Boolean) ctx.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
 
         String serviceName = service != null ? service.getLocalPart() : UNKNOWN;
         String operationName = operation != null ? operation.getLocalPart() : UNKNOWN;
 
        LOGGER.info("[{}] [{}] [{}] " + byteArrayOutputStream.toString(), serviceName, operationName, Boolean.TRUE.equals(isOutboundMessage) ? OUT : IN);
         return true;
     }
 
     @Override
     public void close(MessageContext arg0) {
     }
 
     @Override
     public boolean handleFault(SOAPMessageContext arg0) {
         return false;
     }
 
     @Override
     public Set<QName> getHeaders() {
         return null;
     }
 
 }
