 package de.flower.common.ui.serialize;
 
 import com.thoughtworks.xstream.XStream;
 import de.flower.common.util.xstream.ClassEmittingReflectionConverter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * @author flowerrrr
  */
 public class LoggingSerializer implements ISerializerListener {
 
     private final static Logger log = LoggerFactory.getLogger(LoggingSerializer.class);
 
     private final static Logger xstreamLog = LoggerFactory.getLogger("xstream");
 
     private XStream xstream;
 
     private String pattern;
 
     public LoggingSerializer(String pattern) {
         xstream = new XStream();
         // by default xstream does not output the classname of serialized fields.
         xstream.registerConverter(new ClassEmittingReflectionConverter(xstream), XStream.PRIORITY_VERY_LOW);
 
         this.pattern = pattern;
     }
 
     @Override
     public void notify(Object object, byte[] data) {
         final String xml = xstream.toXML(object);
        long length = data.length;
        log.info("Size of serialized page: " + (length / 1024) + " KB.");
         xstreamLog.trace(xml);
         checkSerializedString(xml);
     }
 
     private void checkSerializedString(String xml) {
         Matcher m = Pattern.compile(pattern).matcher(xml);
         boolean error = false;
         while (m.find()) {
             error = true;
             String matched = m.group();
             log.error("Serialized class [" + matched + "].");
         }
         if (error) {
             log.error("Turn on TRACE level for 'xstream' logger and check serialized xml output for domain objects.");
             throw new RuntimeException("Serialized domain objects in your wicket pages! Use AbstractEntityModels instead.");
         }
     }
 }
