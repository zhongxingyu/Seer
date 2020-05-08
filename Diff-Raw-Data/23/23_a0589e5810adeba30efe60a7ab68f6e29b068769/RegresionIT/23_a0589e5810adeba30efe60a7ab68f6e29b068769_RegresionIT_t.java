 package edu.msu.nscl.olog.api;
 
 import org.junit.BeforeClass;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.xml.sax.SAXException;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import java.io.*;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
 
 /**
  * Created by eschuhmacher on 3/14/14.
  */
 public class RegresionIT {
 
     private static OlogClient client;
     private static String path;
 
     @BeforeClass
     public static void prepareTest() throws Exception {
         client = OlogClientImpl.OlogClientBuilder.serviceURL().withHTTPAuthentication(true).username("olog").password("olog")
                 .create();
         client = OlogClientImpl.OlogClientBuilder.serviceURL().withHTTPAuthentication(true).username("olog").password("olog")
                 .create();
        String filePath = OlogClientIT.class.getResource("RegresionIT.class").getPath();
         path = filePath.substring(0,filePath.indexOf("target")) + "src/test/java/edu/msu/nscl/olog/api/";
     }
 
     @Test
     public void findLogByDate() throws JAXBException, IOException, SAXException {
         Map<String,String> map = new HashMap<String, String>();
         map.put("start", "1332892800");
         map.put("end", "1332979200");
         map.put("page", "1");
         map.put("limit", "10");
         Collection<Log> logs = client.findLogs(map);
         JAXBContext jaxbCtx = JAXBContext.newInstance(XmlLogs.class);
         Marshaller marshaller = jaxbCtx.createMarshaller();
         Iterator<Log> iter = logs.iterator();
         XmlLogs xmlLogs = new XmlLogs();
         while (iter.hasNext()) {
             XmlLog log = LogBuilder.log(iter.next()).toXml();
             xmlLogs.addXmlLog(log);
         }
         File file = new File(path + "Compare.xml");
         marshaller.marshal(xmlLogs , file);
         FileReader reader = new FileReader(file);
         FileReader reader2 = new FileReader(new File(path + "LogByDate.xml"));
         assertXMLEqual("comaparing regresion call", reader, reader2);
     }
 
     @Test
     public void findLogByEntryId() throws JAXBException, IOException, SAXException {
         Map<String,String> map = new HashMap<String, String>();
         map.put("id", "63034");
         map.put("limit", "20");
         map.put("page", "1");
         Collection<Log> logs = client.findLogs(map);
         JAXBContext jaxbCtx = JAXBContext.newInstance(XmlLogs.class);
         Marshaller marshaller = jaxbCtx.createMarshaller();
         Iterator<Log> iter = logs.iterator();
         XmlLogs xmlLogs = new XmlLogs();
         while (iter.hasNext()) {
             XmlLog log = LogBuilder.log(iter.next()).toXml();
             xmlLogs.addXmlLog(log);
         }
         File file = new File(path + "Compare.xml");
         marshaller.marshal(xmlLogs , file);
         FileReader reader = new FileReader(file);
         FileReader reader2 = new FileReader(new File(path + "LogByEntry.xml"));
         assertXMLEqual("comaparing regresion call", reader, reader2);
     }
 
     @Test
     public void findLogByEntryIdWithHistory() throws JAXBException, IOException, SAXException {
         Map<String,String> map = new HashMap<String, String>();
         map.put("id", "63034");
         map.put("limit", "20");
         map.put("page", "1");
         map.put("history","true");
         Collection<Log> logs = client.findLogs(map);
         JAXBContext jaxbCtx = JAXBContext.newInstance(XmlLogs.class);
         Marshaller marshaller = jaxbCtx.createMarshaller();
         Iterator<Log> iter = logs.iterator();
         XmlLogs xmlLogs = new XmlLogs();
         while (iter.hasNext()) {
             XmlLog log = LogBuilder.log(iter.next()).toXml();
             xmlLogs.addXmlLog(log);
         }
         File file = new File(path + "Compare.xml");
         marshaller.marshal(xmlLogs , file);
         FileReader reader = new FileReader(file);
         FileReader reader2 = new FileReader(new File(path + "LogByEntryWithHistory.xml"));
         assertXMLEqual("comaparing regresion call", reader, reader2);
     }
 }
