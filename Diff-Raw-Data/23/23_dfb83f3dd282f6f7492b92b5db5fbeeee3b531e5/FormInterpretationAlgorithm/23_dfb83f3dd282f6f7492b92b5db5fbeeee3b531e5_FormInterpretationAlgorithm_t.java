 package cudl;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Queue;
 import java.util.StringTokenizer;
 
 import org.apache.log4j.Logger;
 import org.mozilla.javascript.Undefined;
 import org.xml.sax.SAXException;
 
 import cudl.node.Audio;
 import cudl.node.Block;
 import cudl.node.Choice;
 import cudl.node.Field;
 import cudl.node.Filled;
 import cudl.node.Form;
 import cudl.node.Initial;
 import cudl.node.Param;
 import cudl.node.Prompt;
 import cudl.node.Property;
 import cudl.node.Record;
 import cudl.node.Script;
 import cudl.node.Subdialog;
 import cudl.node.Transfer;
 import cudl.node.Value;
 import cudl.node.Var;
 import cudl.node.VoiceXmlNode;
 import cudl.node.Vxml;
 import cudl.script.Scripting;
 import cudl.script.Scripting.Scope;
 import cudl.script.Utils;
 import cudl.utils.VxmlElementType;
 
 public class FormInterpretationAlgorithm extends Thread implements FormItemVisitor {
 	private static final String APPLICATION_VARIABLES = "lastresult$[0].confidence = 1; " + "lastresult$[0].utterance = undefined;"
 			+ "lastresult$[0].inputmode = undefined;" + "lastresult$[0].interpretation = undefined;";
 	private VoiceXmlNode currentDialog;
 	private final Executor executor;
 	private String nextItem;
 	private boolean lastIterationReprompt;
 	private boolean dialogChanged;
 	private Queue<VoiceXmlNode> promptQueue;
 	private VoiceXmlNode selectedFormItem;
 	private Map<String, Integer> eventCounterMap = new HashMap<String, Integer>();
 	boolean isHangup;
 	boolean init = true;
 	private Logger LOGGER = Logger.getRootLogger();
 	private InterpreterContext interpreterContext;
 	
 	protected FormInterpretationAlgorithm(InterpreterContext interpreterContext) {
 		this.interpreterContext = interpreterContext;
 		this.executor = new Executor(getScription(), getOutput(),  getDocumentAccess());
 		this.promptQueue = new LinkedList<VoiceXmlNode>();
 		this.setCurrentDialog(interpreterContext.getCurrentVxml().getFirstDialog());
 	}
 
 	private DocumentAcces getDocumentAccess() {
 		return this.interpreterContext.getDocumentAcces();
 	}
 
 	protected FormInterpretationAlgorithm with(String sessionVariables) throws IOException, SAXException {
 		this.getScription().eval(sessionVariables);
 		this.initializeApplicationVariables();
 		this.initializeDocumentVariables();
 		
 		return this;
 	}
 
 	protected void initialize() {
 		getScription().enterScope(Scope.DIALOG); // enter scope dialog
 		LOGGER.debug("debut initialisation des variables");
 		initialiseVariables(getCurrentDialog(),true);
 		LOGGER.debug("fin initialisation des variables");
 	}
 	
 	@Override
 	public void visit(Block block) throws InterpreterException {
 		getScription().set(block.getName(), "true");
 		List<VoiceXmlNode> childs = block.getChilds();
 		for (VoiceXmlNode voiceXmlNode : childs) {
 			executor.execute(voiceXmlNode);
 		}
 	}
 
 	@Override
 	public void visit(Field field) throws InterpreterException {
 		this.playPrompt();
 		String input = getUserInput().readData();
 		while (input == null || !"voice dtmf".contains(input.split("\\$")[0])) {
 			if (input != null) {
 				String eventType = input.split("\\$")[1];
 				InterpreterEventHandler.doEvent(field, executor, eventType, getEventCount(eventType));
 				updateEventCount(eventType);
 			}
 			input = getUserInput().readData();
 			Thread.yield();
 		}
 		if ("voice dtmf".contains(input.split("\\$")[0])) {
 			setUterrance(field, input);
 			executeFilled(field);
 		}
 	}
 
 
 	private UserInput getUserInput() {
 		return interpreterContext.getInput();
 	}
 
 	@Override
 	public void visit(Subdialog subdialog) {
 		LOGGER.debug("debut subdialog");
 		getScription().set(subdialog.getName(), "true");
 		String src = subdialog.getSrc();
 		if (subdialog.getSrc() == null) {
 			src = getScription().eval(subdialog.getSrcexpr()).toString();
 		}
 		try {
 			LOGGER.info("subdialog "+src);
 			Interpreter interpreter = new Interpreter(src);
 			declareParam(interpreter.interpreterContext.getScripting());
 			interpreter.formInterpretationAlgorithm.setUncaughtExceptionHandler(new SubdialogUncaughtExceptionHandler(subdialog,
 					interpreter.interpreterContext.getScripting(), this));
 			interpreter.start();
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public void visit(Transfer transfer) throws InterpreterException {
 		String dest = transfer.getAttribute("dest");
 		getOutput().setTransfertDestination(dest);
 
 		String input = getInput();
 
 		if (input.split("\\$")[0].equals("transfer")) {
 			getScription().set(transfer.getName(), input.split("\\$")[1]);
 
 			for (VoiceXmlNode voiceXmlNode : transfer.getChilds()) {
 				if (voiceXmlNode instanceof Filled) {
 					executor.execute(voiceXmlNode);
 				}
 			}
 		}
 	}
 
 	private SystemOutput getOutput() {
 		return interpreterContext.getOutput();
 	}
 
 	@Override
 	public void visit(Record record) {
 		getScription().set(record.getName(), "true");
 	}
 
 	@Override
 	public void visit(Initial initial) {
 		getScription().set(initial.getName(), "true");
 	}
 
 	@Override
 	public void visit(cudl.node.Object object) {
 		getScription().set(object.getName(), "true");
 	}
 
 	public VoiceXmlNode select() throws ExitException {
 		if (getNextItem() != null) {
 			return getNamedFormItem();
 		}
 
 		VoiceXmlNode firstUndefinedFormItem = getFirstUndefinedFormItem();
 		if (firstUndefinedFormItem == null) {
 			throw new ExitException();
 		}
 		return firstUndefinedFormItem;
 	}
 
 	public void setNextItem(String nextItem) {
 		this.nextItem = nextItem;
 	}
 
 	public void collect() throws InterpreterException, IOException, SAXException {
 		// Queue up prompts for the form item.
 		if (!lastIterationReprompt || !dialogChanged) {
 			if (selectedFormItem instanceof InputFormItem) {
 				selectAppropriate();
 			}
 		}
 
 		// Activate grammars for the form item.
 		// if ( the form item is modal )
 		// Set the active grammar set to the form item grammars,
 		// if any. (Note that some form items, e.g. <block>,
 		// cannot have any grammars).
 		// else
 		// Set the active grammar set to the form item
 		// grammars and any grammars scoped to the form,
 		// the current document, and the application root
 		// document.
 		activateGrammar(selectedFormItem);
 		// Execute the form item.
 		try {
 			setNextItem(null);
 			getScription().enterScope(Scope.ANONYME);
 			((FormItem) selectedFormItem).accept(this);
 		} catch (FormItemChangeException e) {
 			setNextItem(e.getNextFormItemName());
 		} catch (SemanticException e) {
 			InterpreterEventHandler.doEvent(e.node, executor, "error.semantic", getEventCount("error.semanic"));
 		} catch (DialogChangeException dialogChangeException) {
 			catchDialogChangeException(dialogChangeException);
 			return;
 		} catch (DocumentChangeException documentChangeException) {
 			catchDocumentChangeException(documentChangeException);
 			return;
 		}
 
 	}
 
 	private void activateGrammar(VoiceXmlNode currentFormItem) {
 		List<VoiceXmlNode> activedGrammars = new ArrayList<VoiceXmlNode>();
 		if (currentFormItem instanceof InputFormItem)  {
 			if (VxmlElementType.isAModalItem(currentFormItem)){
 				activedGrammars.addAll(Utils.serachItems(currentFormItem, "grammar"));
 			} else {
 				VoiceXmlNode parent = currentFormItem;
 				while (null != parent) {
 					activedGrammars.addAll(Utils.serachItems(parent, "grammar"));
 					parent = parent.getParent();
 				}
 			}
 		}
 		
 		LOGGER.info("actived grammars : "+ activedGrammars.toString());
 		interpreterContext.getOutput().setActivedGrammars(activedGrammars);
 	}
 	
 	public void setSelectedFormItem(VoiceXmlNode select) {
 		this.selectedFormItem = select;
 	}
 
 	@Override
 	public void run() {
 		if (currentDialog instanceof Form) {
 			executeForm();
 		} else {
 			executeMenu();
 		}
 
 		isHangup = true;
 	}
 	
 	private void catchDocumentChangeException(DocumentChangeException documentChangeException) throws IOException, SAXException,
 			InterpreterException {
 		documentChangeException.getNextDocumentFileName();
 		String url = documentChangeException.getNextDocumentFileName();
 		Vxml vxml = new Vxml(getDocumentAccess().get(url, null).getDocumentElement());
 		List<VoiceXmlNode> childs = vxml.getChilds();
 		getScription().enterScope(Scope.DOCUMENT);
 		for (VoiceXmlNode child : childs) {
 			if (child instanceof Var) {
 				executor.execute(child);
 			}
 		}
 		if (!url.contains("#")) {
 			currentDialog = vxml.getFirstDialog();
 		} else {
 			currentDialog = vxml.getDialogById(url.split("#")[1]);
 		}
 		selectedFormItem = null;
 		initialize();
 	}
 
 	private void catchDialogChangeException(DialogChangeException dialogChangeException) throws InterpreterException {
 		Vxml vxml = (Vxml) currentDialog.getParent();
 		currentDialog = vxml.getDialogById(dialogChangeException.getNextDialogId());
 		if (currentDialog == null) {
 			InterpreterEventHandler.doEvent(selectedFormItem, executor, "error.badfetch", 1);
 		}
 		selectedFormItem = null;
 		initialize();
 	}
 
 	private void selectAppropriate() {
 		List<VoiceXmlNode> childs = selectedFormItem.getChilds();
 		for (VoiceXmlNode child : childs) {
 			if (child instanceof Prompt || child instanceof Audio || child instanceof Value) {
 				promptQueue.add(child);
 			}
 		}
 	}
 
 	private VoiceXmlNode getNamedFormItem() {
 		for (VoiceXmlNode formItem : getCurrentDialog().getChilds()) {
 			if (formItem instanceof FormItem && getNextItem().equals(((FormItem) formItem).getName())) {
 				return formItem;
 			}
 		}
 
 		return null;
 	}
 
 	private VoiceXmlNode getFirstUndefinedFormItem() {
 		for (VoiceXmlNode formItem : getCurrentDialog().getChilds()) {
 			if (formItem instanceof FormItem) {
 				if (formItem instanceof Subdialog) {
 					String cond = formItem.getAttribute("cond");
 					String string = getScription().eval(cond == null? "true": cond).toString();
 					if (!Boolean.parseBoolean(string)) {
 						continue;
 					}
 				}
 				if (Undefined.instance.equals(getScription().get(((FormItem) formItem).getName()))) {
 					return formItem;
 				}
 			}
 		}
 		return null;
 	}
 
 
 	
 	public VoiceXmlNode getCurrentDialog() {
 		return currentDialog;
 	}
 
 	public void setCurrentDialog(VoiceXmlNode currentDialog) {
 		this.currentDialog = currentDialog;
 	}
 	
 	private String getNextItem() {
 		return nextItem;
 	}
 	
 	private void executeMenu() {
 		try {
 			for (VoiceXmlNode node : currentDialog.getChilds()) {
 				if (node instanceof Prompt) {
 					executor.execute(node);
 				}
 			}
 
 			String input = getInput();
 			if (input != null) {
 				if ("dtmf".equals(input.split("\\$")[0])) {
 					executeChoiceByDtmf(input);
 				} else {
 					executeChoiceByVoice(input);
 				}
 			}
 		} catch (DocumentChangeException e) {
 			try {
 				catchDocumentChangeException(e);
 				run();
 			} catch (Exception e1) {
 				e1.printStackTrace();
 			}
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	private void executeChoiceByVoice(String input) throws InterpreterException {
 		for (VoiceXmlNode node : currentDialog.getChilds()) {
 			if (node instanceof Choice) {
 				if (((Choice) node).getTextContent().contains(input.split("\\$")[1])) {
 					executor.execute(node);
 				}
 			}
 		}
 	}
 
 	private void executeChoiceByDtmf(String input) throws InterpreterException {
 		int i = 1;
 		for (VoiceXmlNode node : currentDialog.getChilds()) {
 			if (node instanceof Choice) {
 				if (i == Integer.parseInt(input.split("\\$")[1])) {
 					executor.execute(node);
 				}
 				i++;
 			}
 		}
 	}
 
 	private String getInput() throws InterpreterException {
 		String input = getUserInput().readData();
 		while (input == null || !"voice dtmf transfer".contains(input.split("\\$")[0])) {
 			if (input != null) {
 				String eventType = input.split("\\$")[1];
 				InterpreterEventHandler.doEvent(currentDialog, executor, eventType, getEventCount(eventType));
 				updateEventCount(eventType);
 			}
 			input = getUserInput().readData();
 
 			Thread.yield();
 		}
 		return input;
 	}
 
 	private void executeForm() {
 		if (init) {
 			initialize();
 		}
 		while (true) {
 			try {
 				VoiceXmlNode tmp = selectedFormItem;
 				selectedFormItem = select();
 				if (selectedFormItem == null) {
 					InterpreterEventHandler.doEvent(tmp, executor, "error.badfetch", 0);
 				} else {
 					collect();
 				}
 			} catch (ExitException e) {
 				isHangup = true;
 				return;
 			} catch (Exception e) {
 				if (e instanceof FileNotFoundException) {
 					try {
 						InterpreterEventHandler.doEvent(selectedFormItem, executor, "error.badfetch.http.404", 0);
 					} catch (ExitException e1) {
 						isHangup = true;
 						return;
 					} catch (InterpreterException e1) {
 						throw new RuntimeException(e1);
 					}
 				} else {
 					throw new RuntimeException(e);
 				}
 			}
 		}
 
 	}
 
 	class SubdialogUncaughtExceptionHandler implements UncaughtExceptionHandler {
 		private final VoiceXmlNode subdialog;
 		private final Scripting subScripting;
 		private FormInterpretationAlgorithm fia;
 
 		public SubdialogUncaughtExceptionHandler(VoiceXmlNode subdialog, Scripting scripting, FormInterpretationAlgorithm fia) {
 			this.subdialog = subdialog;
 			this.subScripting = scripting;
 			this.fia = fia;
 		}
 
 		@Override
 		public void uncaughtException(Thread t, Throwable e) {
 			Throwable cause = e.getCause();
 			if (cause instanceof InterpreterException) {
 				if (cause instanceof ReturnException) {
 					ReturnException returnException = (ReturnException) cause;
 					String namelist = returnException.getReturn().getAttribute("namelist");
 					interpreterContext.getScripting().eval(((FormItem) selectedFormItem).getName() + "= new Object();");
 					if (namelist != null) {
 						StringTokenizer tokenizer = new StringTokenizer(namelist);
 						while (tokenizer.hasMoreElements()) {
 							String nextToken = tokenizer.nextToken();
 							interpreterContext.getScripting().eval(((FormItem) selectedFormItem).getName() + "." + nextToken + "="
 									+ Utils.scriptableObjectToString(subScripting.eval(nextToken)));
 						}
 					}
 					try {
 						executeFilled(subdialog);
 						fia.init = false;
 						fia.run();
 					} catch (InterpreterException e1) {
 						throw new RuntimeException(e1);
 					}
 				} else {
 					throw new RuntimeException(e);
 				}
 			} else {
 				throw new RuntimeException(e);
 			}
 		}
 
 	}
 	
 	private void initializeDocumentVariables() {
 		getScription().enterScope(Scope.DOCUMENT);
 		initialiseVariables(interpreterContext.getCurrentVxml(), false);
 	}
 	
 	private void initializeApplicationVariables() throws IOException, SAXException {
 		getScription().enterScope(Scope.APPLICATION);
 		getScription().put("lastresult$", "new Array()");
 		getScription().eval("lastresult$[0] = new Object()");
 		getScription().eval(APPLICATION_VARIABLES);
 		String rootName = this.interpreterContext.getCurrentVxml().getApplication();
 		
 		if (rootName != null) {
 			Vxml root = new Vxml(interpreterContext.getDocumentAcces().get(rootName, null).getDocumentElement());
 			initialiseVariables(root, false);
 		}
 	}
 	
 	private void initialiseVariables(VoiceXmlNode voiceXmlNode, Boolean isFormVariable) {
 		for (VoiceXmlNode formChild : voiceXmlNode.getChilds()) {
 			if (isFormVariable && formChild instanceof FormItem ) {
 				String name = ((FormItem) formChild).getName();
 				String expr = formChild.getAttribute("expr");
 				expr = ((expr == null) ? "undefined" : expr);
 				getScription().put(name, expr);
 				if (formChild instanceof InputFormItem) {
 					((InputFormItem) formChild).setPromptCounter(new PromptCounter());
 				}
 			}
 			if (formChild instanceof Var || formChild instanceof Script) {
 				try {
 					this.executor.execute(formChild);
 				} catch (InterpreterException e) {
 					throw new RuntimeException(e);
 				}
 			}
 		}
 	}
 	
 	
 	private void updateEventCount(String eventType) {
 		eventCounterMap.put(eventType, getEventCount(eventType) + 1);
 	}
 	
 	private int getEventCount(String eventType) {
 		Integer integer = eventCounterMap.get(eventType);
 		if (integer == null) {
 			return 1;
 		}
 		return integer;
 	}
 	
 	private void executeFilled(VoiceXmlNode field) throws InterpreterException {
 		for (VoiceXmlNode voiceXmlNode : field.getChilds()) {
 			if (voiceXmlNode instanceof Filled) {
 				executor.execute(voiceXmlNode);
 				return;
 			}
 		}
 	}
 	
 	private void setUterrance(Field field, String input) {
 		String[] split = input.split("\\$");
 		String utterance = "'" + split[1] + "'";
		String inputmode = "'" + split[0] + "'";
 		getScription().set(field.getName(), utterance);
 		getScription().put(field.getName() + "$", "new Object()");
 		getScription().eval("application.lastresult$.utterance =" + field.getName() + "$.utterance=" + utterance);
		getScription().eval("application.lastresult$[0].utterance =" + utterance);
 		getScription().eval("application.lastresult$.inputmode =" + field.getName() + "$.inputmode=" + inputmode);
		getScription().eval("application.lastresult$[0].inputmode =" + inputmode);
 		getScription().eval(field.getName() + "$.confidence=" + 1);
 	}
 
 	private Scripting getScription() {
 		return interpreterContext.getScripting();
 	}
 	
 	private void playPrompt() throws InterpreterException {
 		Queue<VoiceXmlNode> pNodes = promptQueue;
 		for (VoiceXmlNode prompt : pNodes) {
 			executor.execute(prompt);
 		}
 	}
 
 	private void declareParam(Scripting scripting) {
 		assert (selectedFormItem instanceof Subdialog);
 		for (VoiceXmlNode voiceXmlNode : selectedFormItem.getChilds()) {
 			if (voiceXmlNode instanceof Param) {
 				scripting.eval("dialog." + ((Param) voiceXmlNode).getAttribute("name") + "="
 						+ ((Param) voiceXmlNode).getAttribute("expr"));
 			}
 		}
 	}
 
 	protected Properties getProperties() {
 		Properties properties = new Properties();
 		for (VoiceXmlNode node : selectedFormItem.getChilds()) {
 			if (node instanceof Property) {
 				properties.put(node.getAttribute("name"), node.getAttribute("value"));
 			}
 		}
 		LOGGER.info("properties : " + properties);
 		return properties;
 	}
 
 }
