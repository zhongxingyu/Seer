 package misc;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.antlr.runtime.RecognitionException;
 
 import ast.Program;
 
 import parser.ParserInterface;
 import verifier.VerifierInterface;
 import verifier.smtlib.SMTLibTranslator;
 import interpreter.AssertionFailureException;
 import interpreter.Interpreter;
 import interpreter.ProgramExecution;
 import interpreter.GlobalBreakpoint;
 import interpreter.StatementBreakpoint;
 
 public class ExecutionHandler {
 	private ProgramExecution execution;
 	private Interpreter interpreter;
 	private MessageSystem messagesystem;
 	private ParserInterface parser;
 	
 	private Program ast;
 	private ArrayList<GlobalBreakpoint> globalBreakpoints;
 	private String[] parameterValues;
 	private String[] assertionFailureMessage;
 	private boolean paused;
 	
 	public ExecutionHandler(MessageSystem messagesystem) {
 		this.messagesystem = messagesystem;
 		this.interpreter = new Interpreter();
 		this.parser = new ParserInterface();
 		this.globalBreakpoints = new ArrayList<GlobalBreakpoint>();
 	}
 
 	public void parse(String source) {
 		this.messagesystem.clear(MessageCategories.ERROR);
 		try {
 			this.ast = this.parser.parseProgram(source);
 		} catch (RecognitionException re) {
 			this.messagesystem.addMessage(MessageCategories.ERROR, 0, re.getMessage());
 		} catch(NullPointerException npe) {
 			npe.printStackTrace();
 			this.messagesystem.addMessage(MessageCategories.ERROR, -1, "AST creation not possible!");
 		}
 		for(String error : parser.getErrors()) {
 			String[] parsedError = parseParserError(error);
 			this.messagesystem.addMessage(MessageCategories.ERROR, Integer.parseInt(parsedError[0]), parsedError[1]);
 		}
 	}
 	
 	public void run(ArrayList<StatementBreakpoint> sbreakpoints, 
 			ArrayList<GlobalBreakpoint> gbreakpoints) {
 		if (this.ast == null) {
 			return;
 		}
 		boolean finished = false;
 		boolean success = true;
 		while (!paused && !finished && success) {
 			if (this.execution != null && this.execution.checkBreakpoints() != null) {
 				paused = true;
 				break;
 			}
 			if (this.execution != null && this.execution.getCurrentState().getCurrentStatement() == null) {
 				finished = true;
 			}
 			success = this.singleStep(sbreakpoints, gbreakpoints);
 		}
 	}
 	
 	public boolean singleStep(ArrayList<StatementBreakpoint> sbreakpoints, 
 			ArrayList<GlobalBreakpoint> gbreakpoints) {
 		boolean success = false;
 		if (this.ast == null) {
 			return false;
 		}
 		if (this.execution == null) {
 			this.execution = new ProgramExecution(this.ast, this.interpreter, 
 					sbreakpoints, gbreakpoints, this.parameterValues);
 		}
 		try {
 			if (this.execution.getCurrentState().getCurrentStatement() != null) {
 				this.interpreter.step(this.execution.getCurrentState());
 				success = true;
 			}
 		}
 		catch (AssertionFailureException e) {
 			this.assertionFailureMessage = new String[2];
 			success = false;
 			this.assertionFailureMessage[0] = "" + e.getPosition().getLine();
 			this.assertionFailureMessage[1] = e.getMessage();
 			this.destroyProgramExecution();
 	    }
 		return success;
 	}
 	
 	public void verify(String source) {
 		if(this.ast == null) {
 			this.parse(source);
 		}
 		/* VerifierInterface verifier = new VerifierInterface(new SMTLibTranslator());
 		try {
 			verifier.verify(this.ast);
 		} catch (IOException e) {
 			this.messagesystem.addMessage(MessageCategories.VERIFYERROR, -1, "IOE: "+e.getMessage());
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			this.messagesystem.addMessage(MessageCategories.VERIFYERROR, -1, "IE: "+e.getMessage());
 			e.printStackTrace();
 		} catch (RecognitionException e) {
 			this.messagesystem.addMessage(MessageCategories.VERIFYERROR, -1, "RE: "+e.getMessage());
 			e.printStackTrace();
 		} */
 	}
 	
 	private String[] parseParserError(String error) {
 		String[] parsedError = new String[2];
 		Pattern p = Pattern.compile("^line (\\d+):(\\d+) (.*)$");
 		Matcher m = p.matcher(error); 
 		boolean isvalid = m.matches();
 		if(!isvalid) {
 			System.out.println("ERROR");
 			return new String[]{"-1", "ERROR"};
 		}
 		parsedError[0] = m.group(1);
 		parsedError[1] = m.group(3);
 		return parsedError;
 	}
 	
 	public void printAssertionFailureMessage() {
 		int position;
 		try {
 			position = Integer.parseInt(this.assertionFailureMessage[0]);
 		}
 		catch (NumberFormatException e) {
 			position = -1;
 		}
 		this.messagesystem.addMessage(MessageCategories.ERROR, position, 
 				this.assertionFailureMessage[1]);
 		this.assertionFailureMessage = null;
 	}
 	
 	public void clearAssertionFailureMessage() {
 		this.assertionFailureMessage = null;
 	}
 	
 	public void destroyProgramExecution() {
 		this.execution = null;
 	}
 	
 	public void setParameterValues(String[] values) {
 		this.parameterValues = values;
 	}
 
 	public void setAST(Program ast) {
 		this.ast = ast;
 	}
 	
 	public void setPaused(boolean paused) {
 		this.paused = paused;
 	}
 	
 	public ProgramExecution getProgramExecution() {
 		return this.execution;
 	}
 	
 	public String[] getParameterValues() {
 		return this.parameterValues;
 	}
 	
 	public MessageSystem getMessageSystem() {
 		return this.messagesystem;
 	}
 	
 	public String[] getAssertionFailureMessage() {
 		return this.assertionFailureMessage;
 	}
 	
 	public Program getAST() {
 		return this.ast;
 	}
 	
 	public ParserInterface getParserInterface() {
 		return this.parser;
 	}
 	
 	public ArrayList<GlobalBreakpoint> getGlobalBreakpoints() {
 		return this.globalBreakpoints;
 	}
 	
 	public boolean getPaused() {
 		return this.paused;
 	}
 }
