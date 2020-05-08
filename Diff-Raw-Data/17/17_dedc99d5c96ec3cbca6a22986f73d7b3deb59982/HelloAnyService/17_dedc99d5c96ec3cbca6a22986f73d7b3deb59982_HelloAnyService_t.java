 package larvae.jaxb.hello;
 
 import java.io.InputStream;
 
 import larvae.jaxb.JaxbHelper;
import larvae.jaxb.hello.generated.HelloAny;
import larvae.jaxb.hello.generated.HelloAnyResponse;
import larvae.jaxb.hello.generated.ObjectFactory;
 
 import org.apache.commons.lang.builder.ToStringBuilder;
 import org.apache.commons.lang.builder.ToStringStyle;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 public class HelloAnyService {
   private static final Logger LOGGER        = LoggerFactory.getLogger(HelloAnyService.class);
   private JaxbHelper          jaxbHelper    = new JaxbHelper(ObjectFactory.class.getPackage().getName());
   private ObjectFactory       objectFactory = new ObjectFactory();
 
   public String say(InputStream helloXml) {
     HelloAny hello = jaxbHelper.unmarshal(helloXml);
     LOGGER.info("Unmarshalled request:\n{}", ToStringBuilder.reflectionToString(hello, ToStringStyle.MULTI_LINE_STYLE));
     HelloAnyResponse response = createResponse(hello.getAny());
     LOGGER.info("Raw response:\n{}", ToStringBuilder.reflectionToString(response, ToStringStyle.MULTI_LINE_STYLE));
     String responseXml = jaxbHelper.marshal(response);
     LOGGER.info("Marshalled response:\n{}", responseXml);
     return responseXml;
   }
 
   private HelloAnyResponse createResponse(Object any) {
     HelloAnyResponse response = objectFactory.createHelloAnyResponse();
     Object message = createMessage(any);
     response.setAny(message);
     return response;
   }
 
   private static Object createMessage(Object any) {
     if (!(any instanceof Element)) {
       return String.format("Hello, %s!", any);
     }
     Element name = Element.class.cast(any);
     Node text = name.getFirstChild();
     text.setNodeValue(String.format("Hello, %s!", text.getNodeValue()));
     Element message = name.getOwnerDocument().createElement("message");
     message.appendChild(text);
     return message;
   }
 }
