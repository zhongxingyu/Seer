 import java.net.URL;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
  
 import javax.xml.namespace.QName;
 import javax.xml.ws.BindingProvider;
 import javax.xml.ws.Service;
 import javax.xml.ws.handler.MessageContext;
  
 import automation.api.PowerController;
 import automation.api.authenticator.Authenticator;
 import automation.api.authenticator.Password;
  
 public class TestClient{
  
 	private static final String WS_URL = "http://localhost:9999/ws/relay?wsdl";
  
 	public static void main(String[] args) throws Exception {
  
 		URL url = new URL(WS_URL);
        QName qname = new QName("http://van-goethem.co.uk/", "PowerControllerImplService");
  
         Service service = Service.create(url, qname);
         PowerController powerSocket = service.getPort(PowerController.class);
  
         // TEST
         Authenticator authentication = new Authenticator(new Password("HomeAutomation12"));
         
         /*******************UserName & Password ******************************/
         Map<String, Object> req_ctx = ((BindingProvider)powerSocket).getRequestContext();
         req_ctx.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, WS_URL);
  
         Map<String, List<byte[]>> headers = new HashMap<String, List<byte[]>>();
         headers.put("Password", Collections.singletonList(authentication.getEncryptedPassword()));
         headers.put("IV", Collections.singletonList(authentication.getInitialisationVector()));
         req_ctx.put(MessageContext.HTTP_REQUEST_HEADERS, headers);
         /**********************************************************************/
  
         while(true) {
         	System.out.println("Turn relay on/off?");
         	System.out.println("Turn Relay ON/OFF?");
         	String event = System.console().readLine();
         	
         	if (event.equalsIgnoreCase("on")) {
         		powerSocket.turnOn();
         	}
         	if (event.equalsIgnoreCase("off")) {
         		powerSocket.turnOff();
         	}
         } 
     }
 }
