 package cudl;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
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
 
 public class FormInterpretationAlgorithm extends Thread implements FormItemVisitor {
 	private VoiceXmlNode currentDialog;
 	private final Scripting scripting;
 	private String nextItem;
 	private boolean lastIterationReprompt;
 	private boolean dialogChanged;
 	private Queue<VoiceXmlNode> promptQueue;
 	private VoiceXmlNode selectedFormItem;
 	private final SystemOutput outPut;
 	private Executor executor;
 	private final UserInput userInput;
 	private Map<String, Integer> eventCounterMap = new HashMap<String, Integer>();
 	private final DocumentAcces documentAcces;
 	boolean isHangup;
 	boolean init = true;
 	Logger LOGGER = Logger.getRootLogger();
 	
 	public FormInterpretationAlgorithm(VoiceXmlNode dialog, Scripting scripting) {
 		this(dialog, scripting, null, null, null);
 	}
 
 	public FormInterpretationAlgorithm(VoiceXmlNode dialog, Scripting scripting, SystemOutput outPut, UserInput userInput,
 			DocumentAcces documentAcces) {
 		this.documentAcces = documentAcces;
 		this.setCurrentDialog(dialog);
 		this.outPut = outPut;
 		this.scripting = scripting;
 		this.userInput = userInput;
 		this.executor = new Executor(scripting, this.outPut, documentAcces);
 		this.promptQueue = new LinkedList<VoiceXmlNode>();
 	}
 
 	public void initialize() {
 		scripting.enterScope(Scope.DIALOG); // enter scope dialog
 		LOGGER.debug("debut initialisation des variables");
 		for (VoiceXmlNode formChild : getCurrentDialog().getChilds()) {
 			if (formChild instanceof FormItem) {
 				String name = ((FormItem) formChild).getName();
 				String expr = formChild.getAttribute("expr");
 				expr = ((expr == null) ? "undefined" : expr);
 				scripting.put(name, expr);
 				if (formChild instanceof InputFormItem) {
 					((InputFormItem) formChild).setPromptCounter(new PromptCounter());
 				}
 			}
 			if (formChild instanceof Var) {
 				String name = formChild.getAttribute("name");
 				String expr = formChild.getAttribute("expr");
 				expr = ((expr == null) ? "undefined" : expr);
				scripting.put(name, expr);
 			}
 			if (formChild instanceof Script) {
 				scripting.eval(((Script) formChild).getTextContent());
 			}
 		}
 		LOGGER.debug("fin initialisation des variables");
 	}
 
 	@Override
 	public void visit(Block block) throws InterpreterException {
 		scripting.set(block.getName(), "true");
 		List<VoiceXmlNode> childs = block.getChilds();
 		for (VoiceXmlNode voiceXmlNode : childs) {
 			executor.execute(voiceXmlNode);
 		}
 	}
 
 	@Override
 	public void visit(Field field) throws InterpreterException {
 		this.playPrompt();
 		String input = userInput.readData();
 		while (input == null || !"voice dtmf".contains(input.split("\\$")[0])) {
 			if (input != null) {
 				String eventType = input.split("\\$")[1];
 				InterpreterEventHandler.doEvent(field, executor, eventType, getEventCount(eventType));
 				updateEventCount(eventType);
 			}
 			input = userInput.readData();
 			Thread.yield();
 		}
 		if ("voice dtmf".contains(input.split("\\$")[0])) {
 			setUterrance(field, input);
 			executeFilled(field);
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
 		String inputmode = "'" + split[1] + "'";
 		scripting.set(field.getName(), utterance);
 		scripting.put(field.getName() + "$", "new Object()");
 		scripting.eval("application.lastresult$.utterance =" + field.getName() + "$.utterance=" + utterance);
 		scripting.eval("application.lastresult$.inputmode =" + field.getName() + "$.inputmode=" + inputmode);
 		scripting.eval(field.getName() + "$.confidence=" + 1);
 	}
 
 	private void playPrompt() throws InterpreterException {
 		Queue<VoiceXmlNode> pNodes = promptQueue;
 		for (VoiceXmlNode prompt : pNodes) {
 			executor.execute(prompt);
 		}
 		return;
 	}
 
 	@Override
 	public void visit(Subdialog subdialog) {
 		LOGGER.debug("debut subdialog");
 		scripting.set(subdialog.getName(), "true");
 		String src = subdialog.getSrc();
 		if (subdialog.getSrc() == null) {
 			src = scripting.eval(subdialog.getSrcexpr()).toString();
 		}
 		try {
			Interpreter interpreter = new Interpreter(src, "", outPut, userInput, documentAcces);
 			declareParam(interpreter.interpreterContext.getScripting());
 			interpreter.fia.setUncaughtExceptionHandler(new SubdialogUncaughtExceptionHandler(subdialog,
 					interpreter.interpreterContext.getScripting(), this));
 			interpreter.start();
 		} catch (Exception e) {
 			throw new RuntimeException(e);
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
 
 	@Override
 	public void visit(Transfer transfer) throws InterpreterException {
 		String dest = transfer.getAttribute("dest");
 		outPut.setTransfertDestination(dest);
 
 		String input = getInput();
 
 		if (input.split("\\$")[0].equals("transfer")) {
 			scripting.set(transfer.getName(), input.split("\\$")[1]);
 
 			for (VoiceXmlNode voiceXmlNode : transfer.getChilds()) {
 				if (voiceXmlNode instanceof Filled) {
 					executor.execute(voiceXmlNode);
 				}
 			}
 		}
 	}
 
 	@Override
 	public void visit(Record record) {
 		scripting.set(record.getName(), "true");
 	}
 
 	@Override
 	public void visit(Initial initial) {
 		scripting.set(initial.getName(), "true");
 	}
 
 	@Override
 	public void visit(cudl.node.Object object) {
 		scripting.set(object.getName(), "true");
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
 
 		// Execute the form item.
 		try {
 			setNextItem(null);
 			scripting.enterScope(Scope.ANONYME);
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
 
 	private void catchDocumentChangeException(DocumentChangeException documentChangeException) throws IOException, SAXException,
 			InterpreterException {
 		documentChangeException.getNextDocumentFileName();
 		String url = documentChangeException.getNextDocumentFileName();
 		Vxml vxml = new Vxml(documentAcces.get(url, null).getDocumentElement());
 		List<VoiceXmlNode> childs = vxml.getChilds();
 		scripting.enterScope(Scope.DOCUMENT);
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
					String string = scripting.eval(cond == null? "true": cond).toString();
 					if (!Boolean.parseBoolean(string)) {
 						continue;
 					}
 				}
 				if (Undefined.instance.equals(scripting.get(((FormItem) formItem).getName()))) {
 					return formItem;
 				}
 			}
 		}
 		return null;
 	}
 
 	private String getNextItem() {
 		return nextItem;
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
 		String input = userInput.readData();
 		while (input == null || !"voice dtmf transfer".contains(input.split("\\$")[0])) {
 			if (input != null) {
 				String eventType = input.split("\\$")[1];
 				InterpreterEventHandler.doEvent(currentDialog, executor, eventType, getEventCount(eventType));
 				updateEventCount(eventType);
 			}
 			input = userInput.readData();
 
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
 
 	public VoiceXmlNode getCurrentDialog() {
 		return currentDialog;
 	}
 
 	public void setCurrentDialog(VoiceXmlNode currentDialog) {
 		this.currentDialog = currentDialog;
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
 					scripting.eval(((FormItem) selectedFormItem).getName() + "= new Object();");
 					if (namelist != null) {
 						StringTokenizer tokenizer = new StringTokenizer(namelist);
 						while (tokenizer.hasMoreElements()) {
 							String nextToken = tokenizer.nextToken();
 							scripting.eval(((FormItem) selectedFormItem).getName() + "." + nextToken + "="
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
 }
