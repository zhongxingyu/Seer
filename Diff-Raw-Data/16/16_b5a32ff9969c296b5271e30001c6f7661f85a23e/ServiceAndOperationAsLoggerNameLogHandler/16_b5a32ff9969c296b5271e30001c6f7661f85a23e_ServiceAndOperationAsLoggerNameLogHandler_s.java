 package dynamiclogging.ws.handler;
 
 import java.io.ByteArrayOutputStream;
 import java.util.Set;
 
 import javax.xml.namespace.QName;
 import javax.xml.ws.handler.MessageContext;
 import javax.xml.ws.handler.soap.SOAPHandler;
 import javax.xml.ws.handler.soap.SOAPMessageContext;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class ServiceAndOperationAsLoggerNameLogHandler implements SOAPHandler<SOAPMessageContext>{
 
     private static final String LOGGERNAME_PREFIX = "dynamiclogging.ws.";
     private static final String UNKNOWN = "UNKNOWN";
     private static final String SEPARATOR = ".";
     private static final String OUT = "out";
     private static final String IN = "in";
 
     private Logger getLogger(final SOAPMessageContext ctx) {
         final QName service = (QName) ctx.get(SOAPMessageContext.WSDL_SERVICE);
         final QName operation = (QName) ctx.get(SOAPMessageContext.WSDL_OPERATION);
 
         final StringBuffer loggerName = new StringBuffer(LOGGERNAME_PREFIX);
        final Boolean isOutboundMesssage = (Boolean) ctx.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        loggerName.append(Boolean.TRUE.equals(isOutboundMesssage) ? OUT : IN);
         loggerName.append(SEPARATOR);
         loggerName.append(service != null ? service.getLocalPart() : UNKNOWN);
         loggerName.append(SEPARATOR);
         loggerName.append(operation != null ? operation.getLocalPart() : UNKNOWN);
         return LoggerFactory.getLogger(loggerName.toString());
 
     }
 
     @Override
     public boolean handleMessage(SOAPMessageContext ctx) {
 
         Logger logger = getLogger(ctx);
         ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
         try {
             ctx.getMessage().writeTo(byteArrayOutputStream);
         } catch (Exception e) {
             logger.error("HandleMessage failed", e);
         }
         
         logger.info(byteArrayOutputStream.toString());
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
