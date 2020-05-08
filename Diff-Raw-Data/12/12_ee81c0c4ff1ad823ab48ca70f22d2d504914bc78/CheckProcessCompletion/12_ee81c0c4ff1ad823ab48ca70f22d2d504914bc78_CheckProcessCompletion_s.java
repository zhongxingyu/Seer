 package gov.usgs.cida.gdp.wps.completion;
 
 import java.io.File;
 import gov.usgs.cida.gdp.communication.EmailHandler;
 import gov.usgs.cida.gdp.communication.EmailMessage;
 import gov.usgs.cida.gdp.constants.AppConstant;
 import gov.usgs.cida.gdp.utilities.HTTPUtils;
 import gov.usgs.cida.gdp.utilities.XMLUtils;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringReader;
 import java.io.UnsupportedEncodingException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 import javax.mail.MessagingException;
 import javax.mail.internet.AddressException;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.xpath.XPathExpressionException;
 import org.apache.commons.io.IOUtils;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Document;
 import org.xml.sax.SAXException;
 
 /**
  *
  * @author jwalker
  */
 public class CheckProcessCompletion {
     
 	static org.slf4j.Logger log = LoggerFactory.getLogger(CheckProcessCompletion.class);
     
     private static final long serialVersionUID = 1L;
     
     final static String URLENCODE_CHARSET = "UTF-8";
     
 	private static CheckProcessCompletion singleton = null;
 	private Timer timer;
 	private long recheckTime;
 
 	private CheckProcessCompletion() {
 		timer = new Timer("ProcessEmailCheck", true);
 		recheckTime = Long.parseLong(AppConstant.CHECK_COMPLETE_MILLIS.getValue());
 	}
 
 	public synchronized static CheckProcessCompletion getInstance() {
 		if (singleton == null) {
 			singleton = new CheckProcessCompletion();
 		}
 		return singleton;
 	}
 
 	public void addProcessToCheck(String wpsCheckPoint, String emailAddr, String callbackBaseURL, Boolean breakOnSyserr, Boolean emailOnSyserr, Integer checkProcErrLimit) {
 		timer.scheduleAtFixedRate(new EmailCheckTask(wpsCheckPoint, emailAddr, callbackBaseURL, breakOnSyserr, emailOnSyserr, checkProcErrLimit), 0l, recheckTime);
 		cleanupTimer();
 	}
 
 	public void cleanupTimer() {
 		int purged = timer.purge();
 		log.debug("Purged " + purged + " tasks from timer.");
 	}
 
 	public void destroy() {
 		timer.cancel();
 	}
 
 	public static Document parseDocument(InputStream is) throws IOException, SAXException, ParserConfigurationException {
 		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
 		documentBuilderFactory.setNamespaceAware(true);
 		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
 		return documentBuilder.parse(is);
 	}
 
 }
 
 class EmailCheckTask extends TimerTask {
 	static org.slf4j.Logger log = LoggerFactory.getLogger(EmailCheckTask.class);
     private static final long serialVersionUID = 1L;
 
 	private String wpsCheckPoint;
 	private String addr;
     private String callbackBaseURL;
 	private Boolean breakOnSyserr;
 	private Boolean emailOnSyserr;
 	private Boolean isCancelled;
 	private Integer errorCount;
 	private Integer checkProcErrLimit;
 	private final String taskStarted;
 
 	public EmailCheckTask(String wpsCheckPoint, String emailAddr, String callbackBaseURL, Boolean breakOnSyserr, Boolean emailOnSyserr, Integer checkProcErrLimit) {
 		this.wpsCheckPoint = wpsCheckPoint;
 		this.addr = emailAddr;
         this.callbackBaseURL = callbackBaseURL;
 		this.breakOnSyserr = breakOnSyserr;
 		this.emailOnSyserr = emailOnSyserr;
 		this.checkProcErrLimit = checkProcErrLimit;
 		this.errorCount = 0;
 		this.isCancelled = false;
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
 		this.taskStarted = sdf.format(new Date());
 	}
 
 	@Override
 	public void run() {
 		InputStream is = null;
 		try {
			is = HTTPUtils.sendPacket(new URL(wpsCheckPoint), "GET");
 			Document document = CheckProcessCompletion.parseDocument(is);
 			checkAndSend(document);
 		} catch (Exception ex) {
 			String error = "Error in process checking/sending email: " + ex.getMessage();
 			log.error(error);
 
 			if (checkProcErrLimit != -1 && ++this.errorCount >= this.checkProcErrLimit) {
 				if (this.breakOnSyserr) {
 					if (this.emailOnSyserr) {
 						try {
							sendFailedEmail(error);
 						} catch (Exception ex2) {
 							log.error("Also, email was bad, cannot send " + ex2.getMessage());
 						}
 					}
 					this.cancel();
 					this.isCancelled = true;
 				}
 			}
 
 		} finally {
 			IOUtils.closeQuietly(is);
 		}
 	}
 	
 	public Boolean isCancelled() {
 		return this.isCancelled;
 	}
 	
 	public Integer getErrorCount() {
 		return this.errorCount;
 	}
 
 	public void checkAndSend(Document document) throws URISyntaxException, XPathExpressionException, AddressException, MessagingException, IOException, TransformerConfigurationException, TransformerException {
 
 		ProcessStatus procStat = new ProcessStatus(document);
 		if (procStat.isAccepted()) {
 			log.debug("Process accepted at " + taskStarted);
 		}
 		else if (procStat.isStarted()) {
 			log.debug("Process started (" + taskStarted + ") currently running");
 		}
 		else if (procStat.isPaused()) {
 			log.debug("Process paused (started " + taskStarted + ")");
 		}
 		else if (procStat.isSuccess()) {
 			log.debug("Process (started " + taskStarted + ") complete, sending email");
 			sendCompleteEmail(procStat.getOutputReference(), XMLUtils.createPrettyXML(document));
 			this.cancel();
 		}
 		else if (procStat.isFailed()) {
 			log.debug("Processing (started " + taskStarted + ") failed, sending email");
 			sendFailedEmail(procStat.getFailureMessage());
 			this.cancel();
 		}
 		else {
 			log.debug("Status not valid, something went wrong");
 		}
 	}
 
 	private void sendCompleteEmail(String fileLocation, String prettyXML) throws AddressException, MessagingException, TransformerConfigurationException, TransformerException, URISyntaxException {
             // Set up the StreamSource input
             ClassLoader cl = Thread.currentThread().getContextClassLoader();
             File xslFile = null;
             try {
                 URL xsltFileLocation = cl.getResource("wps-result.xsl");
                 xslFile = new File(xsltFileLocation.toURI());
             } catch (URISyntaxException ex) {
                 // Could not load the XSL. We will default to the prettyXML
                 log.warn(ex.getMessage());
             }
             
             String processInfo;
             if (xslFile != null && xslFile.exists()) {
                 // Do the transformation
                 TransformerFactory tFact = TransformerFactory.newInstance();
                 Transformer trans = tFact.newTransformer(new StreamSource(xslFile));
                 ByteArrayOutputStream bos = new ByteArrayOutputStream();
                 trans.transform(
                         new StreamSource(new StringReader(prettyXML)),
                         new StreamResult(bos)
                     );
                 processInfo = bos.toString();
             } else {
                 processInfo = prettyXML;
             }
             
             String from = AppConstant.FROM_EMAIL.getValue();
             String subject = "Processing Complete";
             String content = "The processing has completed on your request."
                             + " You can retrieve your file at " + fileLocation
                             + "\n\n\nProcess Information Follows:\n"
                             + processInfo;
             if (callbackBaseURL != null) {
                 try {
                     content = content.concat("\n\nAdd result to ScienceBase: " + callbackBaseURL + URLEncoder.encode(wpsCheckPoint, CheckProcessCompletion.URLENCODE_CHARSET));
                 } catch (UnsupportedEncodingException e) {
                     log.error("Unable to encode Callback URL arguments", e);
                 }
             }
             List<String> bcc = new ArrayList<String>();
             String bccAddr = AppConstant.TRACK_EMAIL.getValue();
             if (!"".equals(bccAddr)) {
                     bcc.add(bccAddr);
             }
 
             EmailMessage message = new EmailMessage(from, addr, null, bcc, subject, content);
             EmailHandler.sendMessage(message);
 	}
 
 	private void sendFailedEmail(String errorMsg) throws AddressException, MessagingException {
 		String from = AppConstant.FROM_EMAIL.getValue();
 		String subject = "Processing Failed";
 		String content = "The processing has failed on your request."
 				+ " The following errors occured: " + errorMsg;
 
 		List<String> bcc = new ArrayList<String>();
 		String bccAddr = AppConstant.TRACK_EMAIL.getValue();
 		if (!"".equals(bccAddr)) {
 			bcc.add(bccAddr);
 		}
 
 		EmailMessage message = new EmailMessage(from, addr, null, bcc, subject, content);
 		EmailHandler.sendMessage(message);
 	}
 }
