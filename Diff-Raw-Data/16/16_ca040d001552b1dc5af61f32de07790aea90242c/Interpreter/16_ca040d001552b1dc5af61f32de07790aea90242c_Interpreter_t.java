 package cudl;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Properties;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.log4j.BasicConfigurator;
 import org.apache.log4j.Logger;
 import org.xml.sax.SAXException;
 
 import cudl.utils.Session;
 
 public class Interpreter {
 	private static final String TRANSFER_INPUT_TYPE = "transfer$";
 	private static final String EVENT_INPUT_TYPE = "event$";
 	private static final String DTMF_INPUT_TYPE = "dtmf$";
	private static final int JOIN_TIME = 50;
 	
 	protected InterpreterContext interpreterContext;
 	protected FormInterpretationAlgorithm formInterpretationAlgorithm;
 
 	private Logger LOGGER = Logger.getRootLogger();
 	
 	public Interpreter(String url) throws IOException, ParserConfigurationException, SAXException {
 		this(url, Session.getSessionScript());
 	}
 
 	public Interpreter(String url, String sessionVariables) throws IOException, ParserConfigurationException, SAXException {
 		BasicConfigurator.configure();
 		this.interpreterContext = new InterpreterContext(url);
 		this.formInterpretationAlgorithm = new FormInterpretationAlgorithm(interpreterContext).with(sessionVariables);
 	}
 
 	public void start() throws IOException, SAXException {
 		this.formInterpretationAlgorithm.start();
 		LOGGER.error("wait for interpretation ...");
 		sleep();
 		LOGGER.error("... end of interpretation");
 	}
 
 	private void sleep()  {
 		try {
			Thread.sleep(JOIN_TIME * 5);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void noInput() throws IOException, SAXException, ParserConfigurationException {
 		doUserAction("noinput", EVENT_INPUT_TYPE);
 	}
 	
 
 	public void noMatch() throws IOException, SAXException, ParserConfigurationException {
 		doUserAction("nomatch", EVENT_INPUT_TYPE);
 	}
 
 	public void disconnect() throws IOException, SAXException, ParserConfigurationException {
 		doUserAction("connection.disconnect.hangup", EVENT_INPUT_TYPE);
 	}
 
 	public void blindTransferSuccess() throws IOException, SAXException, ParserConfigurationException {
 		interpreterContext.getScripting().eval("connection.protocol.isdnvn6.transferresult= '0'");
 		doUserAction("connection.disconnect.transfer", EVENT_INPUT_TYPE);
 	}
 
 	public void noAnswer() throws IOException, SAXException, ParserConfigurationException {
 		interpreterContext.getScripting().eval("connection.protocol.isdnvn6.transferresult= '2'");
 		doUserAction("'noanswer'", TRANSFER_INPUT_TYPE);
 	}
 
 	public void callerHangupDuringTransfer() throws IOException, SAXException, ParserConfigurationException {
 		doUserAction("'near_end_disconnect'", TRANSFER_INPUT_TYPE);
 	}
 
 	public void networkBusy() throws IOException, SAXException, ParserConfigurationException {
 		interpreterContext.getScripting().eval("connection.protocol.isdnvn6.transferresult= '5'");
 		doUserAction("'network_busy'", TRANSFER_INPUT_TYPE);
 	}
 
 	public void destinationBusy() throws IOException, SAXException, ParserConfigurationException {
 		doUserAction("'busy'", TRANSFER_INPUT_TYPE);
 	}
 
 	public void transferTimeout() throws IOException, SAXException, ParserConfigurationException {
 		doUserAction("'maxtime_disconnect'", TRANSFER_INPUT_TYPE);
 	}
 
 	public void talk(String sentence) {
 		doUserAction(sentence, "voice$");
 	}
 
 	public void submitDtmf(String dtmf) throws IOException, SAXException, ParserConfigurationException {
 		doUserAction(dtmf, DTMF_INPUT_TYPE);
 	}
 
 	public List<String> getLogsWithLabel(String... label) {
 		sleep();
 		return this.interpreterContext.getOutput().getLogs(label);
 	}
 
 	public List<String> getLogs() {
 		sleep();
 		return this.interpreterContext.getOutput().getLogs();
 	}
 
 	public boolean hangup() {
 		sleep();
 		return this.formInterpretationAlgorithm.isHangup;
 	}
 
 	public List<Prompt> getPrompts() {
 		sleep();
 		return this.interpreterContext.getOutput().getPrompts();
 	}
 
 	public String getTranferDestination() {
 		sleep();
 		return this.interpreterContext.getOutput().getTranferDestination();
 	}
 
 	public String getActiveGrammar() {
 		sleep();
 		return this.interpreterContext.getOutput().getActiveGrammar();
 	}
 
 	public Properties getCurrentDialogProperties() {
 		sleep();
 		return this.formInterpretationAlgorithm.getProperties();
 	}
 
 	public void destinationHangup() throws IOException, SAXException, ParserConfigurationException {
 		sleep();
 		doUserAction("'far_end_disconnect'", TRANSFER_INPUT_TYPE);
 	}
 
 	private void doUserAction(String sentence, String action) {
 		Speaker speaker = new Speaker(interpreterContext.getInput());
 		speaker.setUtterance(action + sentence);
 		speaker.start();
 		try {
 			speaker.join();
 			formInterpretationAlgorithm.join(JOIN_TIME);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 }
