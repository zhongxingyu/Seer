 package edu.ucdenver.ccp.PhenoGen.tools.promoter;
 
 /* for handling exceptions in Threads */
 import au.com.forward.threads.ThreadReturn;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 
 import javax.mail.MessagingException;
 import javax.mail.SendFailedException;
 
 import javax.servlet.http.HttpSession;
 
 import edu.ucdenver.ccp.PhenoGen.web.mail.Email;
 import edu.ucdenver.ccp.PhenoGen.data.GeneList;
 import edu.ucdenver.ccp.PhenoGen.data.GeneListAnalysis;
 import edu.ucdenver.ccp.PhenoGen.data.User;
 import edu.ucdenver.ccp.PhenoGen.driver.ExecHandler;
 import edu.ucdenver.ccp.util.FileHandler;
 import edu.ucdenver.ccp.util.sql.PropertiesConnection;
 
 import edu.ucdenver.ccp.util.Debugger;
 
 /* for logging messages */
 import org.apache.log4j.Logger;
 
 public class AsyncMeme implements Runnable{
 
 	private Logger log = null;
 	private HttpSession session = null;
 	private String memeFileName = "";
 	private String sequenceFileName = "";
 	private String distribution = "";
 	private String maxMotifs = "";
 	private String minWidth = "";
 	private String maxWidth = "";
 	private User userLoggedIn = null;
 	private String dbPropertiesFile = null;
 	private String perlDir = null;
 	private GeneListAnalysis myGeneListAnalysis = null;
 	private String mainURL = null;
 	private Thread waitThread = null;
 
 	public AsyncMeme(HttpSession session,
 				String memeFileName,
 				String sequenceFileName,
 				String distribution,
 				String maxMotifs,
 				String minWidth,
 				String maxWidth,
 				GeneListAnalysis myGeneListAnalysis,
                                 Thread waitThread) {
 
                 log = Logger.getRootLogger();
 
 		this.session = session;
 
 		this.memeFileName = memeFileName;
 		this.sequenceFileName = sequenceFileName;
 		this.distribution = distribution;
 		this.maxMotifs = maxMotifs;
 		this.minWidth = minWidth;
 		this.maxWidth = maxWidth;
 	        this.userLoggedIn = (User) session.getAttribute("userLoggedIn");
 		this.dbPropertiesFile = (String) session.getAttribute("dbPropertiesFile");
 		this.perlDir = (String) session.getAttribute("perlDir");
 		this.myGeneListAnalysis = myGeneListAnalysis;
         	this.mainURL = (String) session.getAttribute("mainURL");
                 this.waitThread = waitThread;
         }
 
 	public void run() throws RuntimeException {
 
 	        log.debug("Starting run method of AsyncMeme " );
 
 		Thread thisThread = Thread.currentThread();
 		Email myEmail = new Email();
 		myEmail.setTo(userLoggedIn.getEmail());
 		GeneList thisGeneList = myGeneListAnalysis.getAnalysisGeneList();
 
 		String mainContent = userLoggedIn.getFormal_name() + ",\n\n" + 	
 				"Thank you for using the PhenoGen Informatics website.  "+
 				"The MEME process called '"+
 				myGeneListAnalysis.getDescription() + "' that you initiated ";
 
 		String memeDir = perlDir + "MEME/meme490";
 		String[] envVariables = new String[4];
 		envVariables[0] = "MEME_DIRECTORY=" + memeDir;
 		envVariables[1] = "MEME_BIN=" + memeDir + "/bin";
 		envVariables[2] = "MEME_LOGS=" + memeDir + "/LOGS";
		envVariables[3] = "PATH=/usr/bin:/bin:$PATH:$MEME_BIN";
 
 		String functionDir = perlDir + "MEME/meme490/bin/meme";
                 String [] functionArgs = new String[] {
                 			functionDir,
                                         sequenceFileName,
                                         "-mod", distribution,
                                         "-nmotifs", maxMotifs,
                                         "-minw", minWidth,
                                         "-maxw", maxWidth,
                                         "-maxsize", "100000"
                                 };
 		log.debug("functionArgs = "); new Debugger().print(functionArgs);
 
 		ExecHandler myExecHandler = new ExecHandler(memeDir + "/bin",
                         	functionArgs,
 				envVariables,
                         	memeFileName);
 
 		try {
                 	//
                 	// If this thread is interrupted, throw an Exception
                 	//
                 	ThreadReturn.ifInterruptedStop();
 			// 
 			// If waitThread threw an exception, then ThreadReturn will
 			// detect it and throw the same exception 
 			// Otherwise, the join will happen, and will continue to the 
 			// next statement.
 			//
 			if (waitThread != null) {
 				log.debug("waiting on thread "+waitThread.getName());
 				ThreadReturn.join(waitThread);
 				log.debug("just finished waiting on thread "+waitThread.getName());
 			}
 			myExecHandler.runExec();
 			//new FileHandler().copyFile(new File(memeDir + "/bin/meme_out/meme.html"), new File(memeFileName + ".html"));
                         File src=new File(memeDir+"/bin/meme_out/");
                         File dest=new File(memeFileName);
                         
                         new FileHandler().copyDir(src,dest);
                         
 			String successContent = mainContent + "has completed.  " +
 						"You may now view the results on the website at " + mainURL + ". ";
 			myEmail.setSubject("MEME process has completed"); 
                 	myEmail.setContent(successContent);
 
 		        Connection conn = new PropertiesConnection().getConnection(dbPropertiesFile);
 
 			try {
 				myGeneListAnalysis.createGeneListAnalysis(conn);
 				myGeneListAnalysis.updateVisible(conn);
        	                	myEmail.sendEmail();
 			} catch (SendFailedException e) {
 				log.error("in exception of AsyncMeme while sending email", e);
 			}
 
 			conn.close();
 		} catch (Exception e) {
 			log.error("in exception of AsyncMeme", e);
 			myEmail.setSubject("MEME process had errors"); 
 			String errorContent = mainContent + "was not completed successfully.  "+
 					"The system administrator has been notified of the error. " +
 					"\n" +
 					"You will be contacted via email once the problem is resolved.";
 
 			String adminErrorContent = "The following email was sent to " + userLoggedIn.getEmail() + ":\n" +
 					errorContent + "\n" +
 					"The file is " + memeFileName + 
 					"\n" + "The error type was "+e.getClass(); 
 	                try {
                 		myEmail.setContent(errorContent);
        	                	myEmail.sendEmail();
                 		myEmail.setContent(adminErrorContent);
        	                	myEmail.sendEmailToAdministrator((String) session.getAttribute("adminEmail"));
 				log.debug("just sent email to administrator notifying of MEME errors");
 			} catch (MessagingException e2) {
 				log.error("in exception of AsyncMEME while sending email", e2);
 				throw new RuntimeException();
 			}
 			throw new RuntimeException(e.getMessage());
 		} finally {
 	        	log.debug("executing finally clause in AsyncMeme");
 		}
 	        log.debug("done with AsyncMeme run method");
 	}
 }
  
