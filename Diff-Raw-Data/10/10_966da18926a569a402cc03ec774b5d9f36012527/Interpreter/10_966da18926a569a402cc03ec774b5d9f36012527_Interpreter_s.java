 package cudl;
 
 import java.io.IOException;
 import java.lang.Thread.UncaughtExceptionHandler;
 import java.util.List;
 import java.util.Properties;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.xml.sax.SAXException;
 
import test.Session;
 import cudl.node.Script;
 import cudl.node.Var;
 import cudl.node.VoiceXmlNode;
 import cudl.node.Vxml;
 
 public class Interpreter {
 	protected InterpreterContext interpreterContext;
 	private UserInput userInput;
 	private SystemOutput outPut;
 	FormInterpretationAlgorithm fia;
 	private Vxml vxml;
 	private String currentFileName;
 	private Exception exceptionTothrow;
 	private boolean isHangup;
 	private static DocumentAcces documentAcces;
 	private boolean inSubdialog = false;
 	
 	private static final String APPLICATION_VARIABLES = "lastresult$[0].confidence = 1; " + "lastresult$[0].utterance = undefined;"
 			+ "lastresult$[0].inputmode = undefined;" + "lastresult$[0].interpretation = undefined;";
 
 	public Interpreter(String url) throws IOException, ParserConfigurationException, SAXException {
 		this(url, Session.getSessionScript());
 	}
 
 	public Interpreter(String url, String sessionVariables) throws IOException, ParserConfigurationException, SAXException {
 		this.outPut = new SystemOutput();
 		this.userInput = new UserInput();
 		this.currentFileName = url;
 		this.interpreterContext = new InterpreterContext(url);
 		this.documentAcces =interpreterContext.getDocumentAcces();
 		this.vxml = new Vxml(interpreterContext.getDocumentAcces().get(this.currentFileName, null).getDocumentElement());
 		VoiceXmlNode dialog;
 		if (!url.contains("#")) {
 			dialog = vxml.getFirstDialog();
 		} else {
 			dialog = vxml.getDialogById(url.split("#")[1]);
 		}
 		this.fia = new FormInterpretationAlgorithm(dialog, interpreterContext.getScripting(), outPut, userInput , documentAcces);
 		interpreterContext.getScripting().eval(sessionVariables);
 		interpreterContext.getScripting().enterScope(); // in scope application
 		try {
 			initializeApplicationVariables();
 		} catch (InterpreterException e) {
 			throw new RuntimeException(e);
 		}
 		interpreterContext.getScripting().enterScope(); // in scope document
 		try {
 			initializeDocumentVariables();
 		} catch (InterpreterException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	Interpreter(String url, String sessionVariables, SystemOutput output, UserInput userInput) throws IOException, ParserConfigurationException,
 			SAXException {
 		this.currentFileName = url;
 		this.outPut = output;
 		this.userInput = userInput;
 		this.interpreterContext = new InterpreterContext(url);
 		this.vxml = new Vxml(documentAcces.get(this.currentFileName, null).getDocumentElement());
 		VoiceXmlNode dialog;
 		if (!url.contains("#")) {
 			dialog = vxml.getFirstDialog();
 		} else {
 			dialog = vxml.getDialogById(url.split("#")[1]);
 		}
 		this.fia = new FormInterpretationAlgorithm(dialog, interpreterContext.getScripting(), outPut, userInput, null);
 		this.fia.setUncaughtExceptionHandler(getUncaughtExceptionHandler());
 		interpreterContext.getScripting().eval(sessionVariables);
 		interpreterContext.getScripting().enterScope(); // in scope application
 		try {
 			initializeApplicationVariables();
 		} catch (InterpreterException e) {
 			throw new RuntimeException(e);
 		}
 		interpreterContext.getScripting().enterScope(); // in scope document
 		try {
 			initializeDocumentVariables();
 		} catch (InterpreterException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void start() throws IOException, SAXException {
 		this.fia.start();
 		if (exceptionTothrow != null) {
 			throw new RuntimeException(exceptionTothrow);
 		}
 		try {
 			Thread.sleep(300);
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void noInput() throws IOException, SAXException, ParserConfigurationException {
 		try {
 			enterInput("noinput", "event$");
 			this.fia.join(300);
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void noMatch() throws IOException, SAXException, ParserConfigurationException {
 		try {
 			enterInput("nomatch", "event$");
 			this.fia.join(300);
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void disconnect() throws IOException, SAXException, ParserConfigurationException {
 		try {
 			enterInput("connection.disconnect.hangup", "event$");
 			this.fia.join(300);
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void blindTransferSuccess() throws IOException, SAXException, ParserConfigurationException {
 		try {
 			interpreterContext.getScripting().eval("connection.protocol.isdnvn6.transferresult= '0'");
 			enterInput("connection.disconnect.transfer", "event$");
 			this.fia.join(300);
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void noAnswer() throws IOException, SAXException, ParserConfigurationException {
 		try {
 			interpreterContext.getScripting().eval("connection.protocol.isdnvn6.transferresult= '2'");
 			enterInput("'noanswer'", "transfer$");
 			this.fia.join(300);
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void callerHangupDuringTransfer() throws IOException, SAXException, ParserConfigurationException {
 		try {
 			enterInput("'near_end_disconnect'", "transfer$");
 			this.fia.join(300);
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void networkBusy() throws IOException, SAXException, ParserConfigurationException {
 		try {
 			interpreterContext.getScripting().eval("connection.protocol.isdnvn6.transferresult= '5'");
 			enterInput("'network_busy'", "transfer$");
 			this.fia.join(300);
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void destinationBusy() throws IOException, SAXException, ParserConfigurationException {
 		try {
 			enterInput("'busy'", "transfer$");
 			this.fia.join(300);
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void transferTimeout() throws IOException, SAXException, ParserConfigurationException {
 		try {
 			enterInput("'maxtime_disconnect'", "transfer$");
 			this.fia.join(300);
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void talk(String sentence) {
 		try {
 			enterInput(sentence, "voice$");
 			this.fia.join(300);
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	private void enterInput(String sentence, String inputType) throws InterruptedException {
 		Speaker speaker = new Speaker(userInput);
 		speaker.setUtterance(inputType + sentence);
 		speaker.start();
 		speaker.join();
 	}
 
 	public void submitDtmf(String dtmf) throws IOException, SAXException, ParserConfigurationException {
 		try {
 			enterInput(dtmf, "dtmf$");
 			this.fia.join(300);
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public List<String> getLogsWithLabel(String... label) {
 		return outPut.getLogs(label);
 	}
 
 	public List<String> getLogs() {
 		return outPut.getLogs();
 	}
 
 	public boolean hungup() {
 		return fia.isHangup;
 	}
 
 	public List<Prompt> getPrompts() {
 		return outPut.getPrompts();
 	}
 
 	public String getTranferDestination() {
 		return outPut.getTranferDestination();
 	}
 
 	public String getActiveGrammar() {
 		return outPut.getActiveGrammar();// Utils.getNodeAttributeValue(context.getGrammarActive().get(0),
 		// "src").trim();
 	}
 
 	public Properties getCurrentDialogProperties() {
 		return null;// internalInterpreter.getCurrentDialogProperties();
 	}
 
 	public void destinationHangup() throws IOException, SAXException, ParserConfigurationException {
 		throw new RuntimeException("destinationHangup not implemented");
 		// internalInterpreter.interpret(DESTINATION_HANGUP, null);
 	}
 
 	private UncaughtExceptionHandler getUncaughtExceptionHandler() {
 		return new UncaughtExceptionHandler() {
 
 			@Override
 			public void uncaughtException(Thread t, Throwable e) {
 				Throwable exception = e.getCause();
 
 				try {
 					if (exception instanceof DialogChangeException) {
 						String id = ((DialogChangeException) exception).getNextDialogId();
 						fia = new FormInterpretationAlgorithm(vxml.getDialogById(id), interpreterContext.getScripting(), outPut, userInput, null);
 						fia.start();
 						fia.join();
 					} else if (exception instanceof DocumentChangeException) {
 						interpreterContext.getScripting().exitScope();
 						interpreterContext.getScripting().exitScope();
 						currentFileName = currentFileName.subSequence(0, currentFileName.lastIndexOf("/")) + "/"
 								+ ((DocumentChangeException) exception).getNextDocumentFileName();
 						vxml = new Vxml(interpreterContext.getDocumentAcces().get(currentFileName, null).getDocumentElement());
 						System.err.println(currentFileName);
 						if (currentFileName.contains("#")) {
 							fia = new FormInterpretationAlgorithm(vxml.getDialogById(currentFileName.split("#")[1]),
 									interpreterContext.getScripting(), outPut, userInput,null);
 						} else {
 							fia = new FormInterpretationAlgorithm(vxml.getFirstDialog(), interpreterContext.getScripting(), outPut, userInput, null);
 						}
 						fia.start();
 						fia.join();
 					} else if (exception instanceof ChoiceException) {
 						String next = ((ChoiceException) exception).getChoice().getAttribute("next");
 						if (next != null) {
 							currentFileName = currentFileName.subSequence(0, currentFileName.lastIndexOf("/")) + "/" + next;
 							vxml = new Vxml(interpreterContext.getDocumentAcces().get(currentFileName, null).getDocumentElement());
 							if (next.contains("#")) {
 								next = next.substring(next.lastIndexOf("#") + 1);
 								fia = new FormInterpretationAlgorithm(vxml.getDialogById(next), interpreterContext.getScripting(), outPut, userInput,null);
 							} else {
 								fia = new FormInterpretationAlgorithm(vxml.getFirstDialog(), interpreterContext.getScripting(), outPut, userInput,null);
 							}
 							fia.start();
 							fia.join();
 						}
 					} else if (exception instanceof ExitException) {
 						isHangup = true;
 					} else {
 						throw new RuntimeException(e);
 					}
 				} catch (Exception e1) {
 					throw new RuntimeException(e);
 				}
 			}
 
 		};
 	}
 
 	protected void initializeApplicationVariables() throws IOException, SAXException, InterpreterException {
 		interpreterContext.getScripting().put("lastresult$", "new Array()");
 		interpreterContext.getScripting().eval("lastresult$[0] = new Object()");
 		interpreterContext.getScripting().eval(APPLICATION_VARIABLES);
 		String rootName = vxml.getApplication();
 
 		if (rootName != null) {
 			Vxml root = new Vxml(interpreterContext.getDocumentAcces().get(rootName, null).getDocumentElement());
 			for (VoiceXmlNode voiceXmlNode : root.getChilds()) {
 				if (voiceXmlNode instanceof Var || voiceXmlNode instanceof Script) {
 					new Executor(interpreterContext.getScripting(), null).execute(voiceXmlNode);
 				}
 			}
 		}
 	}
 
 	protected void initializeDocumentVariables() throws InterpreterException {
 		for (VoiceXmlNode voiceXmlNode : vxml.getChilds()) {
 			if (voiceXmlNode instanceof Var || voiceXmlNode instanceof Script) {
 				new Executor(interpreterContext.getScripting(), null).execute(voiceXmlNode);
 			}
 		}
 	}
 }
