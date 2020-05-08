 import java.net.URL;
 
 import org.apache.xmlrpc.client.XmlRpcClient;
 import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
 
 public class XmlRPCExample {
 
     public static void main( String args[] ) throws Exception {
 
     	XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
     	config.setServerURL(new URL("http://127.0.0.1:8000/xmlrpc/"));
     	XmlRpcClient client = new XmlRpcClient();
     	client.setConfig(config);
     	Object[] params = new Object[]{new String("Test String")};
    	String result = (String) client.execute("test", params);
     	
         if (result != null)
             System.out.println(result);
     }
 }
        
