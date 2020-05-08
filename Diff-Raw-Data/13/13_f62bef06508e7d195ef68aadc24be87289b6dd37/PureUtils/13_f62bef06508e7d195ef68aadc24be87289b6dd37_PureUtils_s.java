 package utils;
 
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import javax.xml.datatype.DatatypeConfigurationException;
 import javax.xml.datatype.DatatypeFactory;
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import play.Logger;
 
 public class PureUtils {
     public static XMLGregorianCalendar convertToXMLGregorianCalendar(Date date) {
         GregorianCalendar cal = new GregorianCalendar();
         cal.setTime(date);
         XMLGregorianCalendar gc = null;
         try {
             gc = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
         } catch (DatatypeConfigurationException e) {
             Logger.error(e.getMessage());
         }
         return gc;
     }
 }
