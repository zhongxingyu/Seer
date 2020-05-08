 package utils;
 
import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import javax.xml.datatype.DatatypeConfigurationException;
 import javax.xml.datatype.DatatypeFactory;
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import play.Logger;
 
 public class PureUtils {
     public static XMLGregorianCalendar convertToXMLGregorianCalendar(Date date) {
        if (date == null) {
            Calendar cl = Calendar.getInstance();
            cl.add(Calendar.YEAR, -30);
            date = cl.getTime();
        }
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
