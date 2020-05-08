 package ru.clericj.callmenotifications;
 
 import java.io.InputStream;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import android.telephony.SmsMessage;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 
 public class SmsParser {
 
     private SmsMessage message = null;
     private InputStream settings = null;
     private Boolean isCallMeSmsFlag = false;
     private String subscriberPhoneNumber = null;
 
     public SmsParser(SmsMessage message, InputStream settings) {
         this.message = message;
         this.settings = settings;
         parse();
     }
     private String xmlGetItem(Element element, String name) {
         return element.getElementsByTagName(name).item(0).getChildNodes()
                 .item(0).getNodeValue();
     }
     //public void parse(Context c) {
     private void parse() {
         try {
             DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
             DocumentBuilder builder = factory.newDocumentBuilder();
             Document doc = builder.parse(this.settings);
             doc.getDocumentElement().normalize();
             this.settings.close();
 
             NodeList nodes = doc.getElementsByTagName(doc.getDocumentElement()
                     .getChildNodes().item(1).getNodeName());
 
             for (int i = 0; i < nodes.getLength(); i++) {
                 Node node = nodes.item(i);
 
                 if (node.getNodeType() == Node.ELEMENT_NODE) {
 
                     Element element = (Element) node;
                     Pattern pattern = Pattern.compile(xmlGetItem(element,
                             "match-pattern"));
 
                     Integer useSenderNumber = new Integer(xmlGetItem(element,
                             "use-sender-number"));
 
                     String smsBody = message.getMessageBody().toString();
                     String fromNumber = message.getOriginatingAddress();
                     Matcher match = pattern.matcher(smsBody);
 
                     if (match.find()) {
                         isCallMeSmsFlag = true;
 
                        if (!useSenderNumber.equals(0)) {
                             subscriberPhoneNumber = fromNumber;
                         } else {
                             // При составлении регулярного выражения, для
                             // определения номера телефона используется группа 1
                             subscriberPhoneNumber = match.group(1);
                         }
                         return;
                     }
                 }
             }
         } catch (Exception e) {
             throw new RuntimeException(e.getLocalizedMessage());
         }
 
     }
     public String getPhoneNumber() {
         if (subscriberPhoneNumber != null) {
             return subscriberPhoneNumber.replace(" ", "");
         } else {
             return null;
         }
     }
     public boolean isCallMeSms() {
         return isCallMeSmsFlag;
     }
 }
