 package FRC_Score_Sys;
 
 import FRC_Score_Sys.WebServer.myWebSvr;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.io.PrintStream;
 
 public class Main {
 	final static Logger logger = LoggerFactory.getLogger(Main.class);
 	
 	static PrintStream out;
 	static MainMenu MM;
 	static SqlDB SqlTalk;
 	static myWebSvr web;
 	
 	static File webRoot = new File("wwwroot"); 
 
 	static PopupGenerator Pops = new PopupGenerator();
 	
 	public static void main(String[] args) {
 		logger.info("You've started Matt's 2013 FRC Scoring App Version: 1.2.3");
		logger.info("Report Issues at: https://bitbucket.org/crazysane/frc2013score/issues");
 		
 		SqlTalk = new SqlDB();
 		
 		web = new myWebSvr(null,8080,webRoot);
 		
 		try{
 			web.start();
 		} catch (Exception e){
 			logger.error("Webserver start failed: {}", e.getMessage());
 			Pops.Exception("Constructor", e, "We couldn't start the webserver", false);
 		}
 		
 		// ProgWindow pb = new ProgWindow();
 		// pb.go();
 
 		logger.info("Creating Communications Handler to tie it all together!");
 		SubSysCommHandler CH = new SubSysCommHandler(SqlTalk, web);
 		logger.debug("Opening Main menu.");
 		MM = new MainMenu(CH);
 		// MM.pack();
 		MM.setLocationRelativeTo(null);
 		MM.setVisible(true);
 	}
 }
