 /*
  *   DefineFunctionOperation.java
  * 	 @Author Oleg Gorobets
  *   Created: 24.07.2007
  *   CVS-ID: $Id: 
  *************************************************************************/
 
 package org.swfparser.operation;
 
 import java.util.Collections;
 import java.util.List;
 import java.util.Stack;
 
 import org.springframework.util.StringUtils;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.apache.log4j.Logger;
 
 import org.swfparser.AutoSizeArrayList;
 import org.swfparser.BooleanOperation;
 import org.swfparser.CodeUtil;
 import org.swfparser.ExecutionContext;
 import org.swfparser.Operation;
 import org.swfparser.PatternAnalyzerEx;
 import org.swfparser.Priority;
 import org.swfparser.annotations.NewAnalyzer;
 import org.swfparser.exception.StatementBlockException;
 import com.jswiff.swfrecords.RegisterParam;
 import com.jswiff.swfrecords.actions.DefineFunction2;
 import com.jswiff.swfrecords.actions.StackValue;
 
 @NewAnalyzer
 public class DefineFunction2Operation extends AbstractCompoundOperation {
 
 	private static Logger logger = Logger.getLogger(DefineFunction2Operation.class);
 	
 	private List<Operation> operations;
 	private DefineFunction2 thisFunction;
 	
 	public DefineFunction2Operation(Stack<Operation> stack, DefineFunction2 action, ExecutionContext context) throws StatementBlockException {
 		super(context);
 		thisFunction = action;
 
 		short registerCount = action.getRegisterCount();
 		RegisterParam[] parameters = action.getParameters();
 		
 		List<Operation> registers = new AutoSizeArrayList<Operation>();
 
 		for (int j=0;j<parameters.length;j++) {
 			RegisterParam registerParam = parameters[j];
 			logger.debug("registerParam = "+registerParam +" "+registerParam.getClass().getName());
 			registers.set(registerParam.getRegister(),new RegisterParamOperation(registerParam));
 		}
 		
 		/////
 		logger.debug("name = "+action.getName());
 		logger.debug("registerCount = "+registerCount+",parameters.length="+parameters.length);
 		
 		logger.debug("action.preloadsThis()="+action.preloadsThis());
 		int preloadVariableIndex = 1;
 		if (action.preloadsThis()) {
 			registers.set(preloadVariableIndex++, new StackValue("this"));
 		}
 		
 		logger.debug("action.preloadsArguments()="+action.preloadsArguments());
 		if (action.preloadsArguments()) {
 			registers.set(preloadVariableIndex++, new StackValue("arguments"));
 		}
 		
 		logger.debug("action.preloadsSuper()="+action.preloadsSuper());
 		if (action.preloadsSuper()) {
 			registers.set(preloadVariableIndex++, new StackValue("super"));
 		}
 
 		logger.debug("action.preloadsRoot()="+action.preloadsRoot());
 		if (action.preloadsRoot()) {
 			registers.set(preloadVariableIndex++, new StackValue("_root"));
 		}
 		
 		logger.debug("action.preloadParent()="+action.preloadsParent());
 		if (action.preloadsParent()) {
 			registers.set(preloadVariableIndex++, new StackValue("_parent"));
 		}
 		
 		logger.debug("action.preloadsGlobal()="+action.preloadsGlobal());
 		if (action.preloadsGlobal()) {
 			registers.set(preloadVariableIndex++, new StackValue("_global"));
 		}
 
 		
 		//
 		// Create new execution context
 		//
 //		ExecutionContext newContext = CodeUtil.getExecutionContext();
 //		newContext.setOp
 		
 		context.getOperationStack().push(this);
 		List<Operation> currentRegisters = context.getRegisters();
 		for (int j=0;j<registers.size();j++) {
 			currentRegisters.set(j, registers.get(j));
 		}
 		
 		// save execution stack before calling function2
 		Stack<Operation> executionStack = context.getExecStack();
 		
 		// save labels before calling function2
 //		Map<String,Action> labels = context.getLabels();
 //		PatternAnalyzer patternAnalyzer = context.getPatternAnalyzer();
 		PatternAnalyzerEx patternAnalyzer = context.getPatternAnalyzerEx();
 		
 		// create new execution stack and labels stack
 		context.setExecStack( createEmptyExecutionStack() );
 //		context.setLabels(new HashMap<String,Action>());
 		context.setPatternAnalyzerEx(null);
 		
 		statementBlock.setExecutionContext(context);
 		statementBlock.read(action.getBody().getActions());
 		operations = statementBlock.getOperations();
 		
 		// restore execution stack
 		context.setExecStack(executionStack);
 		
 		// restore labels
 //		context.setLabels(labels);
 		context.setPatternAnalyzerEx(patternAnalyzer);
 
 		
 		context.getOperationStack().pop();
 	}
 
 	public int getArgsNumber() {
 		return 0;
 	}
 
 	public String getStringValue(int level) {
 		StringBuffer buf = new StringBuffer();
 		if (StringUtils.hasText(thisFunction.getName())) { 
 			buf
 			.append(CodeUtil.getIndent(level))
 			.append("function ")
 			.append(thisFunction.getName());
 		} else {
 			buf
 //			.append(CodeUtil.getIndent(level))
 			.append("function");
 		}
 			
 		buf
 			.append("()")
			.append("\n");
 		
 		for (Operation op : operations) {
 //			logger.debug("DF2:OP::: "+op);
 			buf
 			.append(op.getStringValue(level+1))
 			.append(CodeUtil.endOfStatement(op))
 			.append("\n");
 		}
 		
 		buf.append(CodeUtil.getIndent(level));
 		buf.append("}");
 		
 		return buf.toString();
 	}
 
 	private static class RegisterParamOperation implements Operation, BooleanOperation {
 
 		private RegisterParam registerParam;
 		
 		public RegisterParamOperation(RegisterParam registerParam) {
 			super();
 			this.registerParam = registerParam;
 		}
 
 		public int getArgsNumber() {
 			return 1;
 		}
 
 		public String getStringValue(int level) {
 			return registerParam.getParamName();
 		}
 
 		public int getPriority() {
 			return Priority.HIGHEST;
 		}
 
 		public List<Operation> getOperations() {
 			return Collections.EMPTY_LIST;
 		}
 		
 		@Override
 		public boolean equals(Object obj) {
 			if (!(obj instanceof RegisterParamOperation)) {
 				return false;
 			}
 			if (obj == this) {
 				return true;
 			}
 			
 			RegisterParamOperation otherOp = (RegisterParamOperation) obj;
 			return new EqualsBuilder()
 				.append(this.registerParam.getRegister(), otherOp.registerParam.getRegister())
 				.append(this.registerParam.getParamName(), otherOp.registerParam.getParamName())
 				.isEquals();
 		}
 		
 		@Override
 		public int hashCode() {
 			return new HashCodeBuilder()
 			.append(registerParam.getRegister())
 			.append(registerParam.getParamName())
 			.toHashCode();
 		}
 
 		public Operation getInvertedOperation() {
 			return new SimpleInvertedOperation(this);
 		}
 		
 	}
 	
 	@Override
 	public String toString() {
 		return "DefineFunction2("+thisFunction.getName()+")";
 	}
 	
 }
