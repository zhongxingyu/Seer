 import java.net.URL;
 import javax.xml.namespace.QName;
 import javax.xml.ws.Service;
 
 import automation.api.interfaces.ConnectedDevice;
 
 public class TestClient{
  
	private static final String WS_URL = "http://localhost:8080/relay-control-1.0.0/SocketController?wsdl";
  
 	public static void main(String[] args) throws Exception {
  
 		URL url = new URL(WS_URL);
         QName qname = new QName("http://relay.pi.com/", "SocketControllerService");
  
         Service service = Service.create(url, qname);
         ConnectedDevice powerSocket = service.getPort(ConnectedDevice.class);
  
         while(true) {
         	System.out.println("Turn Relay ON/OFF or get STATE?");
         	String event = System.console().readLine();
         	
         	if (event.equalsIgnoreCase("on")) {
         		powerSocket.processInput("powerOn");
         	}
         	if (event.equalsIgnoreCase("off")) {
         		powerSocket.processInput("powerOff");
         	}
         	if (event.equalsIgnoreCase("state")) {
         		System.out.println(powerSocket.processInput("getState"));
         	}
         } 
     }
 }
