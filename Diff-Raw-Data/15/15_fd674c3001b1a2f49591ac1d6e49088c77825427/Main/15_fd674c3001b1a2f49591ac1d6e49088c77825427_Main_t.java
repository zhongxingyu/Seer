 package se.ithuset.greenbean.supplier;
 
 import org.apache.ftpserver.FtpServer;
 import org.apache.ftpserver.ftplet.FtpException;
import org.apache.log4j.Logger;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 public class Main {
 
 	/**
 	 * @param args
 	 * @throws FtpException 
 	 */
 	public static void main(String[] args) throws FtpException {
        final Logger logger = Logger.getLogger(Main.class);
        logger.info("The supplier system is starting up...");
 		ApplicationContext ctx = new ClassPathXmlApplicationContext("META-INF/spring/app-context.xml");
		logger.info("Spring context loaded");
 		
 		FtpServer server = ctx.getBean(FtpServer.class);
		logger.info("Ftp server fetched from context");
 		server.start();
 	}
 
 }
