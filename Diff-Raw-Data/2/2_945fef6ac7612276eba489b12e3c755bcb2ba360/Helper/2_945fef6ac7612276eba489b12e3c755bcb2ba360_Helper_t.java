 package org.megatome.frame2.util;
 
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.TimeZone;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.megatome.frame2.log.Logger;
 import org.megatome.frame2.log.LoggerFactory;
 import org.w3c.dom.Element;
 
 /**
 * This class holds static methods helpful to writing Frame2 tests.
  */
 public class Helper {
 
     private static Logger LOGGER = LoggerFactory.instance(Helper.class.getName());
 
     /**
      * Returns a string for the calendar with the format '2002-12-25'.
      * @param cal
      * @return String
      */
     public static String calendarToString(Calendar cal) {
        Calendar now = Calendar.getInstance();
        TimeZone tz = now.getTimeZone();
        int offset = tz.getRawOffset();
 
        if (tz.inDaylightTime(now.getTime())) {
           offset -= tz.getDSTSavings();
        }
        
        if (tz.inDaylightTime(cal.getTime())) {
           offset += tz.getDSTSavings();
        }
 
        Date d = new Date(cal.getTimeInMillis() - offset);
        return new SimpleDateFormat("yyyy-MM-dd").format(d);
     }
 
     static public Object unmarshall(String path,String pkg,ClassLoader loader) throws Exception {
         LOGGER.debug("Unmarshalling " + path + " in package " + pkg);
         JAXBContext jc = JAXBContext.newInstance(pkg, loader);
         Unmarshaller u = jc.createUnmarshaller();
 		  InputStream istream = ClassLoader.getSystemResourceAsStream(path);
 
         return u.unmarshal(istream);
     }
 
     static public OutputStream marshall(Object obj,String pkg,ClassLoader loader) throws Exception {
         LOGGER.debug("Marshalling " + obj + " in package " + pkg);
         JAXBContext jc = JAXBContext.newInstance(pkg, loader);
         Marshaller m = jc.createMarshaller();
         OutputStream ostream = new ByteArrayOutputStream();   
       
         m.marshal(obj, ostream);
 
         return ostream;
     }
 
 	static public InputStream getInputStreamFor(String path,Class clazz) {
       return clazz.getClassLoader().getResourceAsStream(path);
    }
 
 	static public Element[] loadEvents(String path,Class clazz) throws Exception {
       Element[] result = new Element[1];
       DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
 
       result[0] = builder.parse(Helper.getInputStreamFor(path,clazz)).getDocumentElement();
 
       return result;
    }
 
 
 
 
 
 
 }
