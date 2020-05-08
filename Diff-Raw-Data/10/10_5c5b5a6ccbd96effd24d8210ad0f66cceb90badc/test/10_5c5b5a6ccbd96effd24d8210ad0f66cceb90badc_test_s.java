 import com.microsoft.schemas.office.outlook._2006.oms.*;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.datatype.DatatypeConfigurationException;
 import javax.xml.datatype.DatatypeFactory;
 import javax.xml.datatype.XMLGregorianCalendar;
 import javax.xml.namespace.QName;
 import java.io.StringWriter;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.TimeZone;
 
/**
 * Created by IntelliJ IDEA.
 * User: petrov
 * Date: 21.11.12
 * Time: 18:38
 * To change this template use File | Settings | File Templates.
 */
 public class test
 {
   public static void main(String[] argv) throws DatatypeConfigurationException, JAXBException {
       try
       {
           final QName qName = new QName("http://schemas.microsoft.com/office/Outlook/2006/OMS", "OMS");
           //final URL url = new URL("https://sms.megafon.ru/oms/service.asmx?WSDL");
 
 
          final URL url = new URL("file:/D:/Petrov/NetByNet/ECM/megafon.wsdl"); // rewrite
 
           OMS service = new OMS(url, qName);
 
           String resp00 = service.getOMSSoap().getServiceInfo();
 
           System.out.print(resp00 + "\n");
 
           TUser user = new TUser();
           user.setUserId("my-number");
           user.setPassword("my-pass");
 
           TXmsHeader header = new TXmsHeader();
           header.setRequiredService("SMS_SENDER");
           GregorianCalendar gc = new GregorianCalendar();
           gc.setTime(new Date());
           gc.setTimeZone(TimeZone.getTimeZone("GMT-00:00"));
           header.setScheduled(DatatypeFactory.newInstance().newXMLGregorianCalendar(gc));
 
           TTo to = new TTo();
           ///to.getRecipient().add("79646262477");
           to.getRecipient().add("my-number");
           header.setTo(to);
 
           TXmsBody body = new TXmsBody();
           body.setFormat("SMS");
 
           TContent content1 = new TContent();
           content1.setContentId("12526373567567");
           content1.setContentLocation("1.txt");
           content1.setContentType("text/plain");
           content1.setValue("Олололо");
 
           body.getContent().add(content1);
 
           TXmsData data = new TXmsData();
           data.setClient("Microsoft Office Outlook 14.0");
           data.setUser(user);
           data.setXmsHead(header);
           data.setXmsBody(body);
 
           final JAXBContext context = JAXBContext.newInstance(TXmsData.class);
 
           final Marshaller marshaller = context.createMarshaller();
 
           final StringWriter stringWriter = new StringWriter();
           
           marshaller.marshal(data, stringWriter);
 
           String xmsDataString = stringWriter.toString();
 
           String resp2 = service.getOMSSoap().deliverXms(xmsDataString);
           System.out.print(resp2 + "\n");
       }
       catch (MalformedURLException e)
       {
           e.printStackTrace();
       }
 
   }
 }
