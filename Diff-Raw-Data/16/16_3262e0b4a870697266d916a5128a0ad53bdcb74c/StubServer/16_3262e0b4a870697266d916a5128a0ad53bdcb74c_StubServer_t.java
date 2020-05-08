 package com.lorent.lvmc.ucs.test;
 
 import org.apache.log4j.Logger;
 import org.apache.xmlrpc.server.PropertyHandlerMapping;
 import org.apache.xmlrpc.server.XmlRpcServer;
 import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
 import org.apache.xmlrpc.webserver.WebServer;
 
 import com.lorent.util.LCCUtil;
 
 public class StubServer {
 	private static int port;
 	private static Logger log = Logger.getLogger(StubServer.class);
 	
 	public static void init(int port)throws Exception{
 		log.info("init : " + port);
 		StubServer.port = port;
         WebServer webServer = new WebServer(port);
         
         XmlRpcServer xmlRpcServer = webServer.getXmlRpcServer();
         
         PropertyHandlerMapping phm = new PropertyHandlerMapping();
         /* Load handler definitions from a property file.
          * The property file might look like:
          *   Calculator=org.apache.xmlrpc.demo.Calculator
          *   org.apache.xmlrpc.demo.proxy.Adder=org.apache.xmlrpc.demo.proxy.AdderImpl
          */
         phm.addHandler("ucscb", StubServer.class);
         /* You may also provide the handler classes directly,
          * like this:
          * phm.addHandler("Calculator",
          *     org.apache.xmlrpc.demo.Calculator.class);
          * phm.addHandler(org.apache.xmlrpc.demo.proxy.Adder.class.getName(),
          *     org.apache.xmlrpc.demo.proxy.AdderImpl.class);
          */
         xmlRpcServer.setHandlerMapping(phm);
       
         XmlRpcServerConfigImpl serverConfig =
             (XmlRpcServerConfigImpl) xmlRpcServer.getConfig();
         serverConfig.setEnabledForExtensions(true);
         serverConfig.setContentLengthOptional(false);
 
         webServer.start();
 	}
 	
 	public boolean registerstate(int type){
 		log.info("registerstate : type = " + type);
 		return true;
 	}	
 	
 	public boolean lvmcstate(int type){
 		log.info("lvmcstate : type = " + type);
 		return true;
 	}
 	
 	public boolean incoming(String username){
 		log.info("incoming : username = " + username);
 		return true;
 	}
 	
 	public boolean connected(String username){
 		log.info("connected : username = " + username);
 		return true;
 	}
 	
 	public boolean updatemessage(String msg, String username, int type, int status){
 		log.info("sendDataMessage msg = " + msg + " & username = " + username + " & type = " + type + " & status = " + status);
 		return true;
 	}
 	
 	public boolean hangup(String username){
 		log.info("hangup : username = " + username);
 		return true;
 	}
 	
 	
 	public boolean outgoing(String username){
 		log.info("sendOutgoing username = " + username);
 		return true;
 	}
 	
 	public boolean peerhangup(String username){
 		log.info("peerhangup username = " + username);
 		return true;
 	}
 	
	public boolean callerror(String username){
		log.info("callerror username = " + username);
		return true;
	}
	
 }
