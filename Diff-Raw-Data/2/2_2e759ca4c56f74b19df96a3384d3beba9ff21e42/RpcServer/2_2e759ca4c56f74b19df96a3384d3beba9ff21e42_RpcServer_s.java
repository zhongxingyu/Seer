 package rpc.server;
 
 import org.apache.xmlrpc.server.*;
 import org.apache.xmlrpc.webserver.*;
 
 public class RpcServer {
 	
 	public static void main(String []args){
 		try{
 			// start the webserver
 			WebServer webServer = new WebServer(4444);
 			
 			// get the xmlrpc server stream
 			XmlRpcServer xmlServer = webServer.getXmlRpcServer();
 			
 			// define our class definitions. for us, just the one class for finding the average
 			PropertyHandlerMapping properties = new PropertyHandlerMapping();
 			properties.addHandler("Worker", rpc.server.Worker.class);
 			
 			// set our handler mappings to the contents of our property handling mapping object
 			xmlServer.setHandlerMapping(properties);
 			
 			XmlRpcServerConfigImpl config = (XmlRpcServerConfigImpl) xmlServer.getConfig();
 			config.setEnabledForExtensions(true);
 			webServer.start();
 		}catch (Exception e){
			System.out.println("rpc server error");
 		}
 	
 	}
 }
