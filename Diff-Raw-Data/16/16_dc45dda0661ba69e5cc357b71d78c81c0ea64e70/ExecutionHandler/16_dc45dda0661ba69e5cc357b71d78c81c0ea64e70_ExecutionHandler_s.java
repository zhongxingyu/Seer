 package misc;
 
 import java.util.ArrayList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.antlr.runtime.RecognitionException;
 
 import ast.FunctionParameter;
 import ast.Program;
 
 import parser.ParserInterface;
 import interpreter.Interpreter;
 import interpreter.ProgramExecution;
 import interpreter.GlobalBreakpoint;
 import interpreter.StatementBreakpoint;
 
 public class ExecutionHandler {
 	private ProgramExecution execution;
 	private Interpreter interpreter;
 	private MessageSystem messagesystem;
 	private ParserInterface parser;
 	
 	private FunctionParameter[] parameters;
 	private int[] parameterValues;
 	
 	public ExecutionHandler(MessageSystem messagesystem) {
 		this.messagesystem = messagesystem;
 		this.interpreter = new Interpreter();
 		this.parser = new ParserInterface();
 	}
 	public ProgramExecution getProgramExecution() {
 		return this.execution;
 	}
 
 	public Program parse(String source) {
 		Program ast = null;
 		this.messagesystem.clear(MessageCategories.ERROR);
 		try {
 			ast = this.parser.parseProgram(source);
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
 		return ast;
 	}
 	
 	public int run(String source, ArrayList<StatementBreakpoint> sbreakpoints, 
 			ArrayList<GlobalBreakpoint> gbreakpoints) {
 		int status = this.singleStep(source, sbreakpoints, gbreakpoints);
 		return status;
 	}
 	
 	public int singleStep(String source, ArrayList<StatementBreakpoint> sbreakpoints, 
 			ArrayList<GlobalBreakpoint> gbreakpoints) {
		int status = 0;
 		if (this.execution == null) {
 			Program ast = this.parse(source);
 			if (ast != null) {
 				this.execution = new ProgramExecution(ast, this.interpreter, 
 						sbreakpoints, gbreakpoints, this.parameterValues);
 				status = 1;
 				this.parameters = ast.getMainFunction().getParameters();
 			}
 		}
 		if (status == 1) {
 			this.interpreter.step(this.execution.getCurrentState());
 		}
 		return status;
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
 	
 	public void destroyProgramExecution() {
 		this.execution = null;
 	}
 	
 	public void setParameterValues(int[] values) {
 		this.parameterValues = values;
 	}
 	
 	public FunctionParameter[] getParameters() {
 		return this.parameters;
 	}
 	
 	public ParserInterface getParserInterface() {
 		return this.parser;
 	}
 }
