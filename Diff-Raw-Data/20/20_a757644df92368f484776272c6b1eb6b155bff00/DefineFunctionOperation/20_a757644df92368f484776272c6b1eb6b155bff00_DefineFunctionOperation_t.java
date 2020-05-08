 /*
  *   DefineFunctionOperation.java
  * 	 @Author Oleg Gorobets
  *   Created: Jul 29, 2007
  *   CVS-ID: $Id: 
  *************************************************************************/
 
 package org.swfparser.operation;
 
import java.util.ArrayList;
 import java.util.List;
 import java.util.Stack;
 
import com.jswiff.swfrecords.RegisterParam;
 import org.springframework.util.StringUtils;
 
 import org.apache.log4j.Logger;
 
 import org.swfparser.CodeUtil;
 import org.swfparser.ExecutionContext;
 import org.swfparser.Operation;
 import org.swfparser.PatternAnalyzerEx;
 import org.swfparser.annotations.NewAnalyzer;
 import org.swfparser.exception.StatementBlockException;
 import com.jswiff.swfrecords.actions.Action;
 import com.jswiff.swfrecords.actions.DefineFunction;
 
 @NewAnalyzer
 public class DefineFunctionOperation extends AbstractCompoundOperation {
 
 	private static Logger logger = Logger.getLogger(DefineFunctionOperation.class);
 	private List<Operation> operations;
 	private DefineFunction defineFunction;
 	
 	public DefineFunctionOperation(Stack<Operation> stack, ExecutionContext context, DefineFunction defineFunction) throws StatementBlockException {
 		super(context);
 		this.defineFunction = defineFunction;
 		String functionName = defineFunction.getName();
 		logger.debug(functionName+"()");
 		logger.debug("params = "+defineFunction.getParameters().length);
 		logger.debug("code size = "+defineFunction.getCode());
 		
 		List<Action> actions = defineFunction.getBody().getActions();
 		
 		// Saving context before calling function
 		context.getOperationStack().push(this);
 		
 		// save execution stack & branch analyzer before calling function
 		Stack<Operation> executionStack = context.getExecStack();
 //		PatternAnalyzer patternAnalyzer = context.getPatternAnalyzer();
 		PatternAnalyzerEx patternAnalyzer = context.getPatternAnalyzerEx();
 		
 		// create new execution stack and branch analyzer
 		context.setExecStack( createEmptyExecutionStack() );
 //		context.setPatternAnalyzer(null);
 		context.setPatternAnalyzerEx(null);
 		
 		statementBlock.setExecutionContext(context);
 		statementBlock.read(actions);
 		operations = statementBlock.getOperations();
 		
 		
 		// restore execution stack
 		context.setExecStack(executionStack);
 		
 		// restore branch analyzer
 //		context.setPatternAnalyzer(patternAnalyzer);
 		context.setPatternAnalyzerEx(patternAnalyzer);
 		
 		context.getOperationStack().pop();
 		
 		
 	}
 
 	public String getStringValue(int level) {
 		StringBuffer buf = new StringBuffer();
 		if (StringUtils.hasText(defineFunction.getName())) { 
 			buf
 			.append(CodeUtil.getIndent(level))
 			.append("function ")
 			.append(defineFunction.getName());
 		} else {
 			buf
 //			.append(CodeUtil.getIndent(level))
 			.append("function");
 		}

        List<String> params = new ArrayList<>();
        for (String param: defineFunction.getParameters()) {
            params.add(param);
        }

        buf.append("(");
        buf.append(org.apache.commons.lang.StringUtils.join(params, ","));
		buf.append(")")
			.append("{\n");

 		for (Operation op : operations) {
 			buf.append(op.getStringValue(level+1)+CodeUtil.endOfStatement(op)+"\n");
 		}
 		
 		buf.append(CodeUtil.getIndent(level));
 		buf.append("}");
 		
 		return buf.toString();
 	}
 	
 	@Override
 	public String toString() {
 		return "DefineFunction("+defineFunction.getName()+")";
 	}
 
 }
