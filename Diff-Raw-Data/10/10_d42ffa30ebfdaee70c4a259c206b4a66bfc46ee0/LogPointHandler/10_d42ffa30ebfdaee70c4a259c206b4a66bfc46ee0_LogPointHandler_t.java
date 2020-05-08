 package org.ow2.ipojo.toolkit.log.internal;
 
 import org.apache.felix.ipojo.ConfigurationException;
 import org.apache.felix.ipojo.PrimitiveHandler;
 import org.apache.felix.ipojo.annotations.Handler;
 import org.apache.felix.ipojo.metadata.Element;
 import org.apache.felix.ipojo.parser.FieldMetadata;
 
 import java.util.Dictionary;
 
 /**
  * Created by IntelliJ IDEA.
  * User: guillaume
  * Date: 15/11/10
  * Time: 21:41
  * To change this template use File | Settings | File Templates.
  */
 @Handler(name = "logpoint",
          namespace = "org.ow2.ipojo.toolkit.log")
 public class LogPointHandler extends PrimitiveHandler {
     @Override
     public void configure(Element metadata, Dictionary configuration) throws ConfigurationException {
        Element[] logPointElements = metadata.getElements("logpoint", "org.ow2.ipojo.toolkit.log");
         if (logPointElements.length != 1) {
             throw new ConfigurationException("Cannot have more than 1 <logpoint> element");
         }
 
         Element logPointElement = logPointElements[0];
         String fieldName = getFieldName(logPointElement);
         String name = getLoggerName(logPointElement);
 
         if (name == null) {
             name = getInstanceManager().getClazz().getPackage().getName();
         }
 
         FieldMetadata field = getFieldMetadata(fieldName);
         getInstanceManager().register(field, new LoggerInjector(this, name));
     }
 
     private FieldMetadata getFieldMetadata(String fieldName) {
         return getPojoMetadata().getField(fieldName);
     }
 
     private String getLoggerName(Element logPointElement) {
         return logPointElement.getAttribute("value");
     }
 
     private String getFieldName(Element logPointElement) throws ConfigurationException {
        return logPointElement.getAttribute("field");
 
     }
 
     @Override
     public void stop() {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void start() {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 }
