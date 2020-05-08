 package utils.xmlrpc.client;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.apache.log4j.Logger;
 import org.apache.xmlrpc.client.XmlRpcClient;
 import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
 
 /**
  * xml rpc client , use apache xmlrpc
  * 
  * @author tt
  * 
  */
 public class XmlrpcClient {
 	private static final Logger logger = Logger.getLogger(XmlrpcClient.class);
 	private XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
 
 	public XmlrpcClient(String host) {
 		try {
 			config.setServerURL(new URL(host));
 			config.setEncoding("utf-8");
 		} catch (MalformedURLException e) {
 			logger.error("init xmlrpc client for <" + host + "> error");
 			config = null;
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	public <T> T execute(String action, Object[] args) {
 
 		if (config == null) {
 			return null;
 		}
 		try {
 			XmlRpcClient client = new XmlRpcClient();
 			client.setConfig(config);
 			T result = (T) client.execute(action, args);
 			return result;
 		} catch (Exception e) {
 			logger.error(e);
 			return null;
 		}
 	}
 }
