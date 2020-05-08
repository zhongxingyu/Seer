 package ch.cern.atlas.apvs.server.nettosphere;
 
 
 import org.atmosphere.nettosphere.Config;
 import org.atmosphere.nettosphere.Nettosphere;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Start the Netty server.
  * @author nishant
  *
  */
 public class APVSServer {
 	private static Logger log = LoggerFactory.getLogger(APVSServer.class.getName());
 	
 	private static final int DEFAULT_PORT_NO = 8095;
 //    private static final Logger logger = Logger.getLogger(APVSServer.class);
 
     public static void main(String[] args) {
 		int port = Integer.parseInt(System.getProperty("port",
 				"" + DEFAULT_PORT_NO));
 		
 		Config.Builder builder = new Config.Builder();
 		builder.resource(".")
             .port(port)
             .host("127.0.0.1")
 //            .configFile("META-INF/atmosphere.xml")
             .build();
         Nettosphere server = new Nettosphere.Builder().config(builder.build()).build();
 
         server.start();
 
 //        logger.info("Server started on port: " + port);
		log.info("APVS started on http://localhost:"+port+"/apvs/index.html");
     }
 }
 
