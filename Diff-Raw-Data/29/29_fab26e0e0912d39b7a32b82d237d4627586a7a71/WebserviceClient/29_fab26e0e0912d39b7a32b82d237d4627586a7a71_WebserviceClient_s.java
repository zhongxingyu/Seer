 package org.testingsoftware.client;
 /*
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 */
 import java.io.StringReader;
 
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.transform.Source;
 
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.core.io.FileSystemResource;
 import org.springframework.ws.WebServiceMessageFactory;
//import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
 import org.springframework.xml.transform.ResourceSource;
 import org.springframework.xml.transform.StringResult;
 import org.springframework.ws.client.core.WebServiceTemplate;
 
public class WebserviceClient {
     private static final String MESSAGE = "<soapenv:Envelope "
         + "xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" "
         + "xmlns:calc=\"http://www.testing-software.org/calc\">"
         + "<soapenv:Header/>"
         + "   <soapenv:Body>"
         + "      <calc:addRequest>"
         + "         <calc:a>20</calc:a>"
         + "         <calc:b>10</calc:b>"
         + "      </calc:addRequest>"
         + "   </soapenv:Body>"
         + "</soapenv:Envelope>";
 
    public static void main(String[] args) {
 
         // ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
         //     "webservice-config.xml");
         // WebserviceClient client = (WebserviceClient) context.getBean(
         //     "webServiceTemplate");
         if (args.length != 0) {
             System.out.println("does not process any arguments!");
             System.exit(2);
         }
        WebserviceClient.execute(MESSAGE);
     }
 
    private static void execute(String msg) {
 
         WebServiceTemplate webServiceTemplate = new WebServiceTemplate();
 
         StreamSource source = new StreamSource(new StringReader(msg));
         StreamResult result = new StreamResult(System.out);
   
        webServiceTemplate.sendSourceAndReceiveToResult(
            "http://localhost:8080/demo_server.0.1/calc-service",
            source, result);
     }
 }
 
 /*
 
 
 
 
 
 
 
 
 
 
 import java.io.StringReader;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 
 import org.springframework.ws.WebServiceMessageFactory;
 import org.springframework.ws.client.core.WebServiceTemplate;
 import org.springframework.ws.transport.WebServiceMessageSender;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 public class WebserviceClient {
 
     private static final String MESSAGE =
         "<message xmlns=\"http://tempuri.org\">Hello Web Service World</message>";
 
     private final WebServiceTemplate webServiceTemplate = new WebServiceTemplate();
 
     // send to an explicit URI
     public void sendAndReceive() {
         StreamSource source = new StreamSource(new StringReader(MESSAGE));
         StreamResult result = new StreamResult(System.out);
         webServiceTemplate.sendSourceAndReceiveToResult(
             "http://localhost:8080/demo_server.0.1/calc-service",
             source, result);
     }
 
     public static void main(String[] args) {
 
         ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
             "webservice-config.xml");
         WebserviceClient client = (WebserviceClient) context.getBean(
             "webServiceTemplate");
         if (args.length != 0) {
             System.out.println("does not process any arguments!");
             System.exit(2);
         }
         client.sendAndReceive();
     }
 }
 */
