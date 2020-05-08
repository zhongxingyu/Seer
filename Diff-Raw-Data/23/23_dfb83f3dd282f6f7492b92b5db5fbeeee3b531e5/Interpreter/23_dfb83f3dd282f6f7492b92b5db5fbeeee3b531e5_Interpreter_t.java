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
	private static final int JOIN_TIME = 1000;
 	
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
 		try {
 			this.formInterpretationAlgorithm.start();
 			LOGGER.debug("wait for interpretation ...");
 			Thread.sleep(JOIN_TIME);
 			LOGGER.debug("... en of interpretation");
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void noInput() throws IOException, SAXException, ParserConfigurationException {
 		try {
 			enterInput("noinput", EVENT_INPUT_TYPE);
 			this.formInterpretationAlgorithm.join(JOIN_TIME);
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void noMatch() throws IOException, SAXException, ParserConfigurationException {
 		try {
 			enterInput("nomatch", EVENT_INPUT_TYPE);
 			this.formInterpretationAlgorithm.join(JOIN_TIME);
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void disconnect() throws IOException, SAXException, ParserConfigurationException {
 		try {
 			enterInput("connection.disconnect.hangup", EVENT_INPUT_TYPE);
 			this.formInterpretationAlgorithm.join(JOIN_TIME);
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void blindTransferSuccess() throws IOException, SAXException, ParserConfigurationException {
 		try {
 			interpreterContext.getScripting().eval("connection.protocol.isdnvn6.transferresult= '0'");
 			enterInput("connection.disconnect.transfer", EVENT_INPUT_TYPE);
 			this.formInterpretationAlgorithm.join(JOIN_TIME);
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void noAnswer() throws IOException, SAXException, ParserConfigurationException {
 		try {
 			interpreterContext.getScripting().eval("connection.protocol.isdnvn6.transferresult= '2'");
 			enterInput("'noanswer'", TRANSFER_INPUT_TYPE);
 			this.formInterpretationAlgorithm.join(JOIN_TIME);
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void callerHangupDuringTransfer() throws IOException, SAXException, ParserConfigurationException {
 		try {
 			enterInput("'near_end_disconnect'", TRANSFER_INPUT_TYPE);
 			this.formInterpretationAlgorithm.join(JOIN_TIME);
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void networkBusy() throws IOException, SAXException, ParserConfigurationException {
 		try {
 			interpreterContext.getScripting().eval("connection.protocol.isdnvn6.transferresult= '5'");
 			enterInput("'network_busy'", TRANSFER_INPUT_TYPE);
 			this.formInterpretationAlgorithm.join(JOIN_TIME);
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void destinationBusy() throws IOException, SAXException, ParserConfigurationException {
 		try {
 			enterInput("'busy'", TRANSFER_INPUT_TYPE);
 			this.formInterpretationAlgorithm.join(JOIN_TIME);
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void transferTimeout() throws IOException, SAXException, ParserConfigurationException {
 		try {
 			enterInput("'maxtime_disconnect'", TRANSFER_INPUT_TYPE);
 			this.formInterpretationAlgorithm.join(JOIN_TIME);
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void talk(String sentence) {
 		try {
 			enterInput(sentence, "voice$");
 			this.formInterpretationAlgorithm.join(JOIN_TIME);
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	private void enterInput(String sentence, String inputType) throws InterruptedException {
 		Speaker speaker = new Speaker(interpreterContext.getInput());
 		speaker.setUtterance(inputType + sentence);
 		speaker.start();
 		speaker.join();
 	}
 
 	public void submitDtmf(String dtmf) throws IOException, SAXException, ParserConfigurationException {
 		try {
 			enterInput(dtmf, DTMF_INPUT_TYPE);
 			this.formInterpretationAlgorithm.join(JOIN_TIME);
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public List<String> getLogsWithLabel(String... label) {
 		return this.interpreterContext.getOutput().getLogs(label);
 	}
 
 	public List<String> getLogs() {
 		return this.interpreterContext.getOutput().getLogs();
 	}
 
 	public boolean hungup() {
 		return this.formInterpretationAlgorithm.isHangup;
 	}
 
 	public List<Prompt> getPrompts() {
 		return this.interpreterContext.getOutput().getPrompts();
 	}
 
 	public String getTranferDestination() {
 		return this.interpreterContext.getOutput().getTranferDestination();
 	}
 
 	public String getActiveGrammar() {
 		return this.interpreterContext.getOutput().getActiveGrammar();
 	}
 
 	public Properties getCurrentDialogProperties() {
 		return this.formInterpretationAlgorithm.getProperties();
 	}
 
 	public void destinationHangup() throws IOException, SAXException, ParserConfigurationException {
 		throw new RuntimeException("destinationHangup not implemented");
 		// internalInterpreter.interpret(DESTINATION_HANGUP, null);
 	}
 }
