 /*
  *   StatementBlockImpl.java
  * 	 @Author Oleg Gorobets
  *   Created: 24.07.2007
  *   CVS-ID: $Id: 
  *************************************************************************/
 
 package org.swfparser;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.EmptyStackException;
 import java.util.HashMap;
 import java.util.IdentityHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Stack;
 
 import org.springframework.util.StringUtils;
 
 import org.apache.log4j.Logger;
 
 import org.swfparser.annotations.DoNotWritePop;
 import org.swfparser.annotations.NewAnalyzer;
 import org.swfparser.exception.StatementBlockException;
 import org.swfparser.operation.*;
 import org.swfparser.operation.StoreRegisterOperation.RegisterHandle;
 import org.swfparser.pattern.BreakPattern;
 import org.swfparser.pattern.ContinuePattern;
 import org.swfparser.pattern.DoWhilePattern;
 import org.swfparser.pattern.ForInPattern;
 import org.swfparser.pattern.IfElsePattern;
 import org.swfparser.pattern.IfPattern;
 import org.swfparser.pattern.Pattern;
 import org.swfparser.pattern.SkipForDoWhilePattern;
 import org.swfparser.pattern.SkipPattern;
 import org.swfparser.pattern.SwitchPattern;
 import org.swfparser.pattern.TellTargetPattern;
 import org.swfparser.pattern.WhilePattern;
 import org.swfparser.util.PrintfFormat;
 
 import com.jswiff.io.OutputBitStream;
 import com.jswiff.swfrecords.actions.Action;
 import com.jswiff.swfrecords.actions.ActionConstants;
 import com.jswiff.swfrecords.actions.Branch;
 import com.jswiff.swfrecords.actions.ConstantPool;
 import com.jswiff.swfrecords.actions.DefineFunction;
 import com.jswiff.swfrecords.actions.DefineFunction2;
 import com.jswiff.swfrecords.actions.GetURL2;
 import com.jswiff.swfrecords.actions.GoToFrame;
 import com.jswiff.swfrecords.actions.GoToFrame2;
 import com.jswiff.swfrecords.actions.GoToLabel;
 import com.jswiff.swfrecords.actions.NullStackValue;
 import com.jswiff.swfrecords.actions.Pop;
 import com.jswiff.swfrecords.actions.Push;
 import com.jswiff.swfrecords.actions.SetTarget;
 import com.jswiff.swfrecords.actions.SetTarget2;
 import com.jswiff.swfrecords.actions.StackValue;
 import com.jswiff.swfrecords.actions.StoreRegister;
 import com.jswiff.swfrecords.actions.Try;
 import com.jswiff.swfrecords.actions.UndefinedStackValue;
 import com.jswiff.swfrecords.actions.WaitForFrame;
 import com.jswiff.swfrecords.actions.WaitForFrame2;
 import com.jswiff.swfrecords.actions.With;
 
 public class StatementBlockImpl implements StatementBlock {
 
 	private static Logger logger = Logger.getLogger(StatementBlockImpl.class);
 	
 	
 	private List<Operation> statements = new ArrayList<Operation>();
 	
 	private static PrintfFormat actionFormat = new PrintfFormat("A:0x%02X (%s) label:%s");
 	
 	private ExecutionContext context;
 	private StatementBlockMoment moment = new StatementBlockMoment();
 	private boolean canAddStatements = true;
 	
 	public void read(List<Action> actions) throws StatementBlockException {
 		
 		boolean isRootBlock = context.getOperationStack().isEmpty();
 		Operation enclosingOperation=null;
 		String blockName;
 		moment.setActions(actions);
 
 		boolean newLabelsWereBuilt = false;
 		if (isRootBlock) {
 			blockName = "$" ;
 			newLabelsWereBuilt=true;
 //			context.setPatternAnalyzer(new PatternAnalyzer(actions));
 			PatternAnalyzerEx patternAnalyzerEx = new PatternAnalyzerEx(new PatternContext(), actions);
 			patternAnalyzerEx.analyze();
 			context.setPatternAnalyzerEx(patternAnalyzerEx);
 		} else {
 			enclosingOperation = context.getOperationStack().peek();
 			blockName = enclosingOperation.getClass().getSimpleName();
 			if (enclosingOperation.getClass().getAnnotation(NewAnalyzer.class)!=null) {
 //				context.setPatternAnalyzer(new PatternAnalyzer(actions));
 				PatternAnalyzerEx patternAnalyzerEx = new PatternAnalyzerEx(new PatternContext(), actions);
 				patternAnalyzerEx.analyze();
 				context.setPatternAnalyzerEx(patternAnalyzerEx);
 				newLabelsWereBuilt=true;
 			}
 		}
 		
 		// read all labels
 		logger.debug("");
 		logger.debug("");
 		logger.debug("");		
 		logger.debug("BLOCK START::: "+blockName+" ::: Total actions : "+actions.size()+" Stack size : "/*+stack.size()*/);
 		String regInfo="REGS:";
 		String labelInfo = newLabelsWereBuilt ? "LABELS BUILT:" : "LABELS INHERITED:";
 		int yui=1;
 		for (Operation op : context.getRegisters()) {
 			regInfo+=(yui++)+"=>"+op+",";
 		}
 		if (context.getPatternAnalyzerEx()!=null) {
 			for (String lab : context.getPatternAnalyzerEx().getLabels().keySet()) {
 				labelInfo += lab + ",";
 			}
 		}
 		logger.debug(regInfo);
 		logger.debug(labelInfo);
 		logger.debug("");
 		logger.debug("");
 		logger.debug("");
 
 //		for (Action action : actions) {
 //			
 //			if (action instanceof Branch) {
 //				Branch branch = (Branch) action;
 //				logger.debug(
 //						actionFormat.sprintf(new Object[]{action.getCode(),ActionConstants.getActionName(action.getCode())}) +
 //						" L:"+action.getLabel()+" BL:"+branch.getBranchLabel()
 //						);
 //			} else {
 //				logger.debug(
 //						actionFormat.sprintf(new Object[]{action.getCode(),ActionConstants.getActionName(action.getCode())}) +
 //						" L:"+action.getLabel()
 //						);
 //			}
 //		}
 		
 		
 //		actionIndex = actions.size();
 		int actionIndex = 0;
 		Map<Operation, Action> stackOperationToActionMap = new IdentityHashMap<Operation, Action>();
 		try {
 			while (actionIndex < actions.size()) {
 				
 				Action action = actions.get(actionIndex);
 				
 				// set context
 				moment.setActionIndex(actionIndex);
 				moment.setStatements(statements);
 				context.getMomentStack().push(moment);
 				
 				String label = action.getLabel();
 				logger.debug(actionFormat.sprintf(new Object[]{action.getCode(),ActionConstants.getActionName(action.getCode()),label!=null ? label : "[[[null]]]"}));
 				
 				int actionIndexShift=1;
 				if (context.getPatternAnalyzerEx()!=null && context.getPatternAnalyzerEx().getPatternByLabel(action.getLabel())!=null) {
 					Pattern branchPattern = context.getPatternAnalyzerEx().getPatternByLabel(action.getLabel());
 					logger.debug("Branch pattern found: "+branchPattern);
 					handleByPattern(actionIndex, action);
 					actionIndexShift+=branchPattern.size();
 				} else {
 					actionIndexShift+=handleByActionCode(action);
 				}
 				
 				// map newly added operations to this action
 				for (Operation operation : context.getExecStack()) {
 					if (!stackOperationToActionMap.containsKey(operation)) {
 						stackOperationToActionMap.put(operation, action);
 					}
 				}
 				
 				context.getMomentStack().pop();
 				
 				actionIndex += actionIndexShift;
 				
 				logger.debug("@@@STACK="+context.getExecStack().size());
 			}
 		} catch (EmptyStackException e) {
 			e.printStackTrace();
 			
 			Stack<Operation> operationStack = context.getOperationStack();
 			while (!operationStack.isEmpty()) {
 				logger.debug("OP_STACK_TRACE: "+operationStack.pop());
 			}
 
 			logger.debug("~~~~~ Writing UNFINISHED BLOCK statements ~~~");
 			for (Operation op : statements) {
 				String endOfStatement = CodeUtil.endOfStatement(op);
 				logger.debug(op.getStringValue(0)+endOfStatement+" // "+op+"\n");
 			}
 			
 			try {
 				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
 				OutputBitStream outputBitStream = new OutputBitStream(byteArrayOutputStream);
 				for (Action a : actions) {
 					a.write(outputBitStream);
 				}
 				statements.clear();
 				ByteCodeOperation byteCodeOperation = new ByteCodeOperation(byteArrayOutputStream.toByteArray()); 
 				statements.add(byteCodeOperation);
 				logger.debug("Adding bytecode operation\n"+byteCodeOperation.getStringValue(0));
 			} catch (IOException e1) {
 				e1.printStackTrace();
 			}
 			
 			throw new StatementBlockException(e);
 			
 		} catch (StatementBlockException e) {
 			e.printStackTrace();
 			
 			Stack<Operation> operationStack = context.getOperationStack();
 			while (!operationStack.isEmpty()) {
 				logger.debug("OP_STACK_TRACE: "+operationStack.pop());
 			}
 
 			logger.debug("~~~~~ Writing UNFINISHED BLOCK statements ~~~");
 			for (Operation op : statements) {
 				String endOfStatement = CodeUtil.endOfStatement(op);
                 logger.debug(op.getStringValue(0)+endOfStatement+" // "+op.getClass().getName()+"\n");
 			}
 			
 			throw new StatementBlockException(e);
 		}
 
 		postProcessStatements();
 		
 		if (isRootBlock) {
 			checkStackInTheEndOfTheBlock(stackOperationToActionMap);
 			logger.debug("~~~~~ Writing statements ~~~");
 			for (Operation op : statements) {
 				String endOfStatement = CodeUtil.endOfStatement(op);
                 logger.debug(op.getStringValue(0)+endOfStatement+" // "+op.getClass().getName()+"\n");
 			}
 		}
 		
 		logger.debug("");
 		logger.debug("");
 		logger.debug("");
 		logger.debug("BLOCK FINISHED ::: "+blockName);
 		logger.debug("");
 		logger.debug("");
 		logger.debug("");
 
 	}
 
 	protected void checkStackInTheEndOfTheBlock(Map<Operation, Action> stackOperationToActionMap) {
 		logger.debug("Checking stack before writing statements...");
 		Stack<Operation> stack = context.getExecStack();
 		if (stack.isEmpty()) {
 			logger.debug("Stack is empty... OK");
 		} else {
 			logger.debug("Stack size is "+stack.size());
 			for (Operation operation : stack) {
 				logger.debug("Checking "+operation+". Maps to action "+stackOperationToActionMap.get(operation));
 			}
 		}
 		
 	}
 
 	private void handleByPattern(int actionIndex, Action action) throws StatementBlockException {
 //		Pattern branchPattern = context.getPatternAnalyzer().getPatternByLabel(action.getLabel());
 		Pattern branchPattern = context.getPatternAnalyzerEx().getPatternByLabel(action.getLabel());
 		Class branchPatternClass = branchPattern.getClass();
 		Stack<Operation> stack = context.getExecStack();
 		
 		if (branchPatternClass.equals( SwitchPattern.class )) {
 			addStatement( new SwitchOperation(context, (SwitchPattern) branchPattern), action);
 		}
 		
 		if (branchPatternClass.equals( TellTargetPattern.class )) {
 			if (action instanceof SetTarget) {
 				SetTarget setTarget = (SetTarget) action;
 				addStatement( new SetTargetOperation(stack,context,((TellTargetPattern)branchPattern).getActions(),setTarget), action );
 			} else if (action instanceof SetTarget2) {
 				SetTarget2 setTarget = (SetTarget2) action;
 				addStatement( new SetTarget2Operation(stack,context,((TellTargetPattern)branchPattern).getActions(),setTarget), action );
 			}
 			return;
 		}
 		
 		if (branchPatternClass.equals( WhilePattern.class )) {
 			Operation op = new WhileOperation(stack,((WhilePattern)branchPattern).getActions(),context);
 			addStatement(op, action);
 			return;
 		}
 		
 		if (branchPatternClass.equals( DoWhilePattern.class )) {
 			canAddStatements = true;
 			Operation op = new DoWhileOperation(stack,((DoWhilePattern)branchPattern).getActions(),context);
 			logger.debug("Adding "+op+" to statement");
 			addStatement(op, action);
 			return;
 		}
 		
 		if (branchPatternClass.equals( IfElsePattern.class )) {
 			Operation op = new IfElseOperation(stack,context,((IfElsePattern)branchPattern).getIfActions(),((IfElsePattern)branchPattern).getElseActions());
 			addStatement(op, action);
 			return;
 		}
 		
 		if (branchPatternClass.equals( ContinuePattern.class )) {
 			addStatement(new SimpleOperation("continue"), action);
 			return;
 		}
 		if (branchPatternClass.equals( BreakPattern.class )) {
 			addStatement(new SimpleOperation("break"), action);
 			return;
 		}
 		
 		// should go before SkipPattern
 		if (branchPatternClass.equals( SkipForDoWhilePattern.class )) {
 			canAddStatements = false;
 //			context.getPatternAnalyzer().clearBranchPattern(action.getLabel());
 			context.getPatternAnalyzerEx().clearBranchPattern(action.getLabel());
 			handleByActionCode(action);
 			return;
 		}
 		
 		if (branchPatternClass.equals( SkipPattern.class )) {
 			return;
 		}
 		
 		if (branchPatternClass.equals( ForInPattern.class )) {
 			addStatement(new ForInOperation(context,((ForInPattern)branchPattern).getActions(),((ForInPattern)branchPattern).getVarActions()), action);
 			
 			return;
 		}
 		
 		//
 		// If pattern should go the last!!!
 		//
 		if (branchPatternClass.equals( IfPattern.class )) {
 			Operation op = new IfOperation(stack,((IfPattern)branchPattern).getActions(),context);
 			addStatement(op, action);
 			return;
 		}
 		
 	}
 
 	private int handleByActionCode(Action action) throws StatementBlockException {
 		int additionalActionShift = 0;
 		Stack<Operation> stack = context.getExecStack();
 		Operation op;
 //		int actionIndexShift = 1; // default
 		switch (action.getCode()) {
 		
 			case ActionConstants.CONSTANT_POOL:
 				ConstantPool constantPool = (ConstantPool)action;
 				context.getConstants().addAll(constantPool.getConstants());
 				logger.debug("Loaded constants:"+context.getConstants().size());
 				for (String constant : context.getConstants()) {
 					logger.debug(constant);
 				}
 				break;
 		
 			case ActionConstants.PUSH :
 				handlePush((Push)action,stack);
 				break;
 			
 			case ActionConstants.PUSH_DUPLICATE :
 				stack.push(stack.peek());
 				break;
 			
 			case ActionConstants.POP :
 				if (stack.isEmpty()) {
 					logger.error("Empty stack and POP() found");
 					break;
 				} else {
					boolean writePop = stack.peek() instanceof DualUse; 
 					if (writePop && !statements.isEmpty()) {
 						// check last statement
 						// if it is StoreRegister than do NOT write this POP()
 						Operation lastStatement = statements.get(statements.size()-1);
						writePop = lastStatement.getClass().getAnnotation(DoNotWritePop.class) == null;
 					}
 					if (writePop) {
 						logger.debug("Writing POP()");
 						((DualUse)stack.peek()).markAsStatement();
 						addStatement(stack.pop(),action);
 					} else {
 						logger.debug("Skipping POP()");
 						stack.pop();
 					}
 					
 				}
 				break;
 			
 			case ActionConstants.STACK_SWAP:
 				Operation item1 = stack.pop();
 				Operation item2 = stack.pop();
 				stack.push(item1);
 				stack.push(item2);
 				break;
 			
 			case ActionConstants.DEFINE_LOCAL :
 				op = new DefineLocalOperation(stack);
 				addStatement(op, action);
 				break;
 
 			case ActionConstants.DEFINE_LOCAL_2 :
 				op = new DefineLocal2Operation(stack);
 				addStatement(op, action);
 				break;
 				
 			//
 			// TODO: group simple operations
 			//
 				
 			
 			case ActionConstants.PREVIOUS_FRAME :
 				addStatement(new SimpleOperation("prevFrame()"), action);
 				break;
 			case ActionConstants.NEXT_FRAME:
 				stack.push(new SimpleOperation("nextFrame()"));
 				break;
 			case ActionConstants.PLAY:
 				if (!statements.isEmpty() && (statements.get(statements.size()-1) instanceof ActionAware)) {
 					((ActionAware)statements.get(statements.size()-1)).setAction(GotoFrameOperation.ACTION_PLAY);
 				} else {
 					addStatement(new SimpleOperation("play()"), action);
 				}
 				break;
 			case ActionConstants.STOP :
 				addStatement(new SimpleOperation("stop()"), action);
 				break;	
 			case ActionConstants.STOP_SOUNDS:
 				addStatement(new SimpleOperation("stopAllSounds()"), action);
 				break;
 			case ActionConstants.GET_TIME:
 				stack.push(new SimpleFunctionOperation("getTimer()"));
 				break;
             case ActionConstants.THROW:
                 addStatement(new ThrowOperation(stack), action);
                 break;
 
 			//
 			// Arithmetic operations
 			//				
 
 				
 			case ActionConstants.ADD:
 			case ActionConstants.ADD_2:
 				stack.push(new AddOperation(stack,action));
 				break;
 			case ActionConstants.SUBTRACT:
 				stack.push(new SubtractOperation(stack,action));
 				break;
 			case ActionConstants.DIVIDE:
 				stack.push(new DivideOperation(stack,action));
 				break;
 			case ActionConstants.MULTIPLY:
 				stack.push(new MultiplyOperation(stack,action));
 				break;
 			case ActionConstants.MODULO:
 				op = new ModuloOperation(stack,action);
 				stack.push(op);
 				break;
 				
 			case ActionConstants.INCREMENT:
 				op = new SimpleIncrementOperation(stack);
 				stack.push(op);
 				break;
 			case ActionConstants.DECREMENT:
 				op = new SimpleDecrementOperation(stack);
 				stack.push(op);
 				break;
 				
 			case ActionConstants.STRING_ADD:
 				stack.push(new StringAddOperation(stack));
 				break;
 				
 			
 				
 			//
 			// Boolean operations
 			//				
 				
 			case ActionConstants.LESS:
 			case ActionConstants.LESS_2:
 				op = new LessOperation(stack);
 				stack.push(op);
 				break;
 			
 			case ActionConstants.EQUALS:
 			case ActionConstants.EQUALS_2:
 				stack.push(new EqualsOperation(stack));
 				break;
 			case ActionConstants.STRICT_EQUALS:
 				stack.push(new StrictEqualsOperation(stack));
 				break;
 			
 			case ActionConstants.GREATER:
 				op = new GreaterOperation(stack);
 				stack.push(op);
 				break;
 				
 				
 			case ActionConstants.AND:
 				stack.push(new AndOperation(stack));
 				break;
 			case ActionConstants.OR:
 				stack.push(new OrOperation(stack));
 				break;
 			
 			case ActionConstants.GET_VARIABLE:
 				op = new GetVariableOperation(stack);
 				stack.push(op);
 				break;
 			
 			case ActionConstants.CALL_METHOD:
 				stack.push(new CallMethodOperation(stack));
 				break;
 				
 			case ActionConstants.SET_MEMBER:
 				op = new SetMemberOperation(stack);
 				addStatement(op, action);
 				break;
 				
 				
 			case ActionConstants.DEFINE_FUNCTION:
 				DefineFunction defineFunction = (DefineFunction)action;
 				op = new DefineFunctionOperation(stack,context,defineFunction);
 				if (StringUtils.hasText(defineFunction.getName())) {
 					addStatement(op, action); // function as statement
 				} else {
 					stack.push(op); // function as operation, put it to stack
 				}
 				break;
 				
 			case ActionConstants.DEFINE_FUNCTION_2:
 				DefineFunction2 defineFunction2 = (DefineFunction2)action;
 				op = new DefineFunction2Operation(stack,defineFunction2,context);
 				if (StringUtils.hasText(defineFunction2.getName())) {
 					addStatement(op, action); // function as statement
 				} else {
 					stack.push(op); // function as operation, put it to stack
 				}
 				break;
 			
 			case ActionConstants.GET_MEMBER:
 				stack.push( new GetMemberOperation(stack) );
 				break;
 			case ActionConstants.GET_PROPERTY:
 				stack.push( new GetPropertyOperation(stack) );
 				break;
 			
 			//
 			// Type convertion funcs
 			//
 			
 			case ActionConstants.TO_INTEGER:
 				stack.push(new ToIntegerOperation(stack));	
 				break;
 			case ActionConstants.TO_NUMBER:
 				stack.push(new ToNumberOperation(stack));	
 				break;
 			case ActionConstants.TO_STRING:
 				stack.push(new ToStringOperation(stack));	
 				break;
 				
 			// TODO: Check LABEL_OUT
 			case ActionConstants.JUMP:
 			case ActionConstants.IF:
 				Branch branch = (Branch) action;
 				throw new StatementBlockException("Should be handled by pattern: "+action);
 				
 			case ActionConstants.DELETE:	
 				addStatement(new DeleteOperation(stack), action);
                 stack.push(new TrueOperation()); // TODO should be the result of the DeleteOperation?
 				break;
 			case ActionConstants.DELETE_2:
 				addStatement(new Delete2Operation(stack), action);
 				break;
 				
 				
 				
 			case ActionConstants.NOT:
 				op = new NotOperation(stack);
 //				op = NotOperation.createNotOperation(stack);
 				stack.push(op);
 				break;
 			
 			
 			case ActionConstants.GET_URL:
 				op = new GetURLOperation(action);
 				addStatement(op, action); // ???
 //				finishThisBlock = true;
 				break;
 			
 			case ActionConstants.GET_URL_2:
 				op = new GetURL2Operation(stack,(GetURL2)action);
 				addStatement(op, action); // ???
 //				finishThisBlock = true;
 				break;
 				
 
 				
 
 			case ActionConstants.SET_VARIABLE:
 				op = new SetVariableOperation(context);
 				addStatement(op, action);
 				break;
 
 			case ActionConstants.STORE_REGISTER:
 				op = new StoreRegisterOperation(context, ( StoreRegister ) action);
 				addStatement(op, action);
 				break;
 				
 			case ActionConstants.INIT_ARRAY:
 				stack.push(new InitArrayOperation(stack));
 				break;
 				
 			case ActionConstants.INIT_OBJECT:
 				stack.push(new InitObjectOperation(stack));
 				break;
 				
 			case ActionConstants.NEW_OBJECT:
 				stack.push( new NewObjectOperation(context) );
 				break;
 			
 			case ActionConstants.NEW_METHOD:
 				stack.push( new NewMethodOperation(context) );
 				break;
 				
 			case ActionConstants.CALL_FUNCTION:
 				op = new CallFunctionOperation(stack);
 				stack.push(op);
 				break;
 			
 			case ActionConstants.GO_TO_FRAME:
 				op = new GotoFrameOperation((GoToFrame)action);
 //				stack.push(op);
 				addStatement(op, action);
 				break;
 				
 			case ActionConstants.GO_TO_FRAME_2:
 				op = new GotoFrame2Operation(stack,(GoToFrame2)action);
 //				stack.push(op);
 				addStatement(op, action);
 				break;
 				
 			case ActionConstants.GO_TO_LABEL:
 				addStatement(new GotoLabelOperation(stack,(GoToLabel)action), action);
 				break;
 				
 			case ActionConstants.TRACE:
 				addStatement( new TraceOperation(stack), action );
 				break;
 				
 			case ActionConstants.RANDOM_NUMBER:
 				op = new RandomOperation(stack);
 				stack.push(op);
 				break;
 			case ActionConstants.REMOVE_SPRITE:
 				addStatement(new RemoveMovieClipOperation(stack), action);
 				break;
 			case ActionConstants.RETURN:
 				addStatement( new ReturnOperation(stack), action );
 				break;
 			case ActionConstants.SET_PROPERTY:
 				addStatement( new SetPropertyOperation(stack), action );
 				break;
 
 			case ActionConstants.START_DRAG:
 				addStatement(new StartDragOperation(stack), action);
 				break;
 				
 			case ActionConstants.END_DRAG:
 				addStatement(new SimpleOperation("stopDrag()"), action);
 				break;
 				
 			case ActionConstants.STRING_GREATER:
 				stack.push(new GreaterOperation(stack));
 				break;
 			case ActionConstants.STRING_LESS:
 				stack.push(new LessOperation(stack));
 				break;
 			
 				/*
 				ActionTargetPath
 				If the object in the stack is of type MovieClip, the object's target path is pushed on the stack
 				in dot notation. If the object is not a MovieClip, the result is undefined rather than the
 				movie clip target path.
 				ActionTargetPath does the following:
 				1. Pops the object off the stack.
 				2. Pushes the target path onto the stack.
 				*/
 			
 			case ActionConstants.TARGET_PATH:
 				stack.push( new TargetPathOperation(stack) );
 				break;
 			
 			case ActionConstants.TYPE_OF:
 				stack.push(new TypeOfOperation(stack));
 				break;
 			
 			case ActionConstants.WITH:
 				addStatement( new WithOperation(stack,context,(With) action), action );
 				break;
 				
 			case ActionConstants.TRY:
 				addStatement( new TryCatchOperation(stack,context,(Try) action), action );
 				break;
 			
 			case ActionConstants.CAST_OP:
 				stack.push( new CastOperation(stack) );
 				break;
 
 			case ActionConstants.CLONE_SPRITE:
 				addStatement( new CloneSpriteOperation(stack), action );
 				break;
 			
 			case ActionConstants.ENUMERATE:
 			case ActionConstants.ENUMERATE_2:
 				throw new StatementBlockException("ENUMERATE should be handled by pattern: "+action);
 
 			case ActionConstants.EXTENDS:
 				addStatement( new ExtendsOperation(stack), action );
 				break;	
 			case ActionConstants.IMPLEMENTS_OP:
 				addStatement( new ImplementsOperation(stack), action );
 				break;	
 			case ActionConstants.INSTANCE_OF:
 				stack.push( new InstanceOfOperation(stack) );
 				break;	
 				
 			//
 			// Bitwise operations
 			//
 			case ActionConstants.BIT_AND:
 				stack.push( new BitwiseAndOperation(stack) );
 				break;
 			case ActionConstants.BIT_OR:
 				stack.push( new BitwiseOrOperation(stack) );
 				break;
 			case ActionConstants.BIT_L_SHIFT:
 				stack.push( new BitwiseLShiftOperation(stack) );
 				break;
 			case ActionConstants.BIT_R_SHIFT:
 				stack.push( new BitwiseRShiftOperation(stack) );
 				break;
 			case ActionConstants.BIT_XOR:
 				stack.push( new BitwiseXorOperation(stack) );
 				break;
 			case ActionConstants.BIT_U_R_SHIFT:
 				stack.push( new BitwiseURShiftOperation(stack) );
 				break;
 				
 				
 			//
 			// Deprecated since SWF 5
 			//
 			case ActionConstants.ASCII_TO_CHAR:
 				stack.push(new ChrOperation(stack));
 				break;
 			case ActionConstants.CHAR_TO_ASCII:
 				stack.push(new OrdOperation(stack));
 				break;
 			case ActionConstants.M_B_ASCII_TO_CHAR:
 				stack.push(new MbChrOperation(stack));
 				break;
 			case ActionConstants.M_B_CHAR_TO_ASCII:
 				stack.push(new MbOrdOperation(stack));
 				break;
 			case ActionConstants.M_B_STRING_EXTRACT:
 				stack.push(new MbSubstringOperation(stack));
 				break;
 			case ActionConstants.M_B_STRING_LENGTH:
 				stack.push(new MbLengthOperation(stack));
 				break;
 			case ActionConstants.SET_TARGET:
 			case ActionConstants.SET_TARGET_2:
 				throw new StatementBlockException("Should be handled by pattern: "+action);
 //				SetTarget setTarget = (SetTarget) action;
 //				addStatement( new SetTargetOperation(stack,context,setTarget) );
 //				break;
 			
 //				SetTarget2 setTarget2 = (SetTarget2) action;
 //				addStatement( new SetTarget2Operation(stack,context,setTarget2) );
 //				break;
 			case ActionConstants.STRING_EQUALS:
 				stack.push(new EqualsOperation(stack));
 				break;
 			case ActionConstants.STRING_EXTRACT:
 				stack.push(new StringExtractOperation(stack));
 				break;
 			case ActionConstants.STRING_LENGTH:
 				stack.push(new StringLengthOperation(stack));
 				break;
 			case ActionConstants.TOGGLE_QUALITY:
 				addStatement(new SimpleOperation("toggleHighQuality()"), action);
 				break;
 			case ActionConstants.WAIT_FOR_FRAME:
 				WaitForFrame waitForFrame = (WaitForFrame) action;
 				additionalActionShift = waitForFrame.getSkipCount();
 				List<Action> skipActions = new ArrayList<Action>(additionalActionShift);
 				int actionIndex = moment.getActionIndex();
 				for (int j=1;j<=additionalActionShift;j++) {
 					skipActions.add(moment.getActions().get(actionIndex+j));
 				}
 				addStatement( new WaitForFrameOperation(stack,context,waitForFrame,skipActions), action );
 				break;
 			case ActionConstants.WAIT_FOR_FRAME_2:
 				WaitForFrame2 waitForFrame2 = (WaitForFrame2) action;
 				additionalActionShift = waitForFrame2.getSkipCount();
 				skipActions = new ArrayList<Action>(additionalActionShift);
 				actionIndex = moment.getActionIndex();
 				for (int j=1;j<=additionalActionShift;j++) {
 					skipActions.add(moment.getActions().get(actionIndex+j));
 				}
 				addStatement( new WaitForFrame2Operation(stack,context,waitForFrame2,skipActions), action );
 				break;
 				
 			case ActionConstants.CALL:
 				addStatement( new CallOperation(stack), action );
 				break;
 
 			
 			case ActionConstants.END:
 				// just skip this action
 				break;
 	
 				
 			default:
 				logger.error("UNSUPPORTED ACTION " + action.getCode());
 				
 			
 		}
 	
 		return additionalActionShift;
 		
 	}
 
 	private Map<Action,Operation> actionStatementmap = new HashMap<Action, Operation>(); 
 	
 	protected void addStatement(Operation op, Action action) {
 		if (canAddStatements) {
 			if (!(op instanceof SkipOperation) || 
 					(op instanceof SkipOperation && !((SkipOperation)op).skip())) {
 				
 				if (op instanceof OperationFactory) {
 					op = ((OperationFactory)op).getObject();
 				}
 				
 				actionStatementmap.put(action, op);
 				statements.add(op);
 				checkStackAfterStatement();
 				
 			}
 		}
 	}
 	
 	private static int modifiedVarIndex = 1;
 	private static int modifiedRegIndex = 1000;
 	
 	protected void checkStackAfterStatement() {
 		Operation statement = statements.get(statements.size()-1);
 		logger.debug("Checking stack after "+statement);
 		Stack<Operation> stack = context.getExecStack();
 		logger.debug("Stack size is "+stack.size());
 		
 		// First try handle ++x 
 		if (handlePlusPlusX(statement,stack)) {
 			return;
 		}
 		
 		if (stack.isEmpty()) {
 			return;
 		}
 		
 		if (statement instanceof AssignOperation) {
 			Operation leftOp = ((AssignOperation)statement).getLeftPart();
 			while (!leftOp.getOperations().isEmpty()) {
 				leftOp = leftOp.getOperations().get(0);
 			}
 
 			// Try handle x++
 			if (handleXPlusPlus(statement,stack)) {
 				return;
 			}
 			
 			// Check strings
 			if (leftOp instanceof StackValue && StackValue.TYPE_STRING == ((StackValue)leftOp).getType()) {
 				// Retrieve variable name
 				String variableName = ((StackValue)leftOp).getString();
 				
 				for (Operation operation : stack) {
 					// Check if there is use of GetVariable(variableName) in the stack
 					Operation operationToFind = new GetVariableOperation(new StackValue(variableName));
 					List<Operation> operationsToCheck = getAllUnderlyingOperations(operation);
 					boolean stackContainsAssignmentVariable = false;
 					List<Operation> operationsToChange = new ArrayList<Operation>();
 					for (Operation underOp : operationsToCheck) {
 						if (operationToFind.equals(underOp)) {
 							logger.debug("The stack contains "+operation+" which itself contains "+underOp+". Fixing it...");
 							stackContainsAssignmentVariable = true;
 							operationsToChange.add(underOp);
 						}
 					}
 					
 					if (stackContainsAssignmentVariable) {
 						/*
 						 * x in stack
 						 * change to:
 						 * 1) x__m = x;
 						 * 2) push getVariable(x__m)
 						 * 
 						*/
 						String newVariableName = variableName+"__m_"+modifiedVarIndex++;
 						for (Operation chOp : operationsToChange) {
 							logger.debug("Changing the name of variable "+variableName+" to "+newVariableName);
 							((GetVariableOperation)chOp).setOp(new StackValue(newVariableName));
 						}
 						
 						statements.add(statements.size()-1, new DefineLocalOperation(new StackValue(newVariableName),new GetVariableOperation(new StackValue(variableName))));
 					}
 				}
 			}
 			
 			// Check registers
 			if (leftOp instanceof RegisterHandle) {
 				RegisterHandle registerHandle = (RegisterHandle) leftOp;
 				int startStackIdx = stack.size()-1;
 				
 				
 				// get next action
 				int thisActionIndex = context.getMomentStack().peek().getActionIndex();
 				int nextActionIndex = thisActionIndex + 1;
 				
 				if (nextActionIndex < context.getMomentStack().peek().getActions().size()) {
 					Action nextAction = context.getMomentStack().peek().getActions().get(nextActionIndex);
 					if (nextAction instanceof Pop) {
 						// do not check the top of stack as this value will be discarded
 						startStackIdx--;
 					}
 				}
 				
 				if (startStackIdx == stack.size()-1) {
 					Action thisAction = context.getMomentStack().peek().getActions().get(thisActionIndex);
 					if (thisAction instanceof StoreRegister) {
 						startStackIdx--;
 					}
 				}
 				
 				// find this register handle in stack
 				for (int stackIdx = startStackIdx; stackIdx>=0; stackIdx--) {
 					Operation operation = stack.get(stackIdx);
 					logger.debug("STACK_VAL = "+operation);
 					List<Operation> operationsToCheck = getAllUnderlyingOperations(operation);
 					List<Operation> operationsToChange = new ArrayList<Operation>();
 					boolean stackContainsAssignmentVariable = false;
 					for (Operation underOp : operationsToCheck) {
 						if (registerHandle.equals(underOp)) {
 							logger.debug("The stack contains "+operation+" which itself contains "+underOp+". Fixing it...");
 							stackContainsAssignmentVariable = true;
 							operationsToChange.add(underOp);
 						}
 					}
 					
 					if (stackContainsAssignmentVariable) {
 						/*
 						 * __Rn in stack
 						 * change to:
 						 * 1) __R__m = x;
 						 * 2) push getVariable(x__m)
 						 * 
 						*/
 						int oldRegisterNumber = registerHandle.getRegisterNumber();
 						int newRegisterNumber = modifiedRegIndex++;
 						for (Operation chOp : operationsToChange) {
 							logger.debug("Changing name of register variable "+oldRegisterNumber+" to "+newRegisterNumber);
 							((RegisterHandle)chOp).setRegisterNumber(newRegisterNumber);
 						}
 						
 						statements.add(statements.size()-1, new DefineLocalOperation(new RegisterHandle(newRegisterNumber),new RegisterHandle(oldRegisterNumber)));
 					}
 				}
 				
 			}
 		}
 		
 		
 	}
 	
 	private boolean handlePlusPlusX(Operation statement, Stack<Operation> stack) {
 		statement = getRealStatement(statement);
 		
 		// get all operations except stack top
 		List<Operation> allStackOperations = new ArrayList<Operation>();
 				
 		if (!stack.isEmpty()) {
 			for (int i = 0; i<stack.size()-1; i++) {
 				allStackOperations.addAll(getAllUnderlyingOperations(stack.get(i)));
 			}
 		}
 		
 		boolean inc = statement instanceof PostIncrementOperation;
 		boolean dec = statement instanceof PostDecrementOperation;
 		
 		if (inc || dec) {
 			Operation incOperation = ((UnaryOperation)statement).getOp();
 			
 			if (stack.isEmpty() || !allStackOperations.contains(incOperation)) {
 				List<Operation> registerOperations = context.getRegisters();
 				if (!registerOperations.isEmpty() && registerOperations.get(0) instanceof RegisterHandle) {
 					RegisterHandle registerHandle = (RegisterHandle) registerOperations.get(0);
 					if (registerHandle.getUndelrlyingOp() instanceof SimpleIncrementOperation) {
 						SimpleIncrementOperation simpleIncrementOperation = (SimpleIncrementOperation) registerHandle.getUndelrlyingOp();
 						if (incOperation.equals(simpleIncrementOperation.getOp())) {
 							logger.debug("Simplified to " + (inc ? "++x" : "--x"));
 							// remove last statement and change register(0)
 							statements.remove(statements.size()-1);
 							statements.remove(registerHandle.getStoreRegisterOp());
 							context.getRegisters().set(0, inc ? new PreIncrementOperation(incOperation) : new PreDecrementOperation(incOperation));
 							return true;
 						}
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 	protected boolean handleXPlusPlus(Operation statement, Stack<Operation> stack) {
 		
 		// get top of stack
 		Operation stackTop = stack.peek();
 		
 		// get all operations except stack top
 		List<Operation> allStackOperations = new ArrayList<Operation>();
 		for (int i = 0; i<stack.size()-1; i++) {
 			allStackOperations.addAll(getAllUnderlyingOperations(stack.get(i)));
 		}
 		
 		boolean inc = statement instanceof AssignIncrementOperation;
 		boolean dec = statement instanceof AssignDecrementOperation;
 		
 		if (inc || dec) {
 			Operation incOperation = ((UnaryOperation)statement).getOp();
 			if (!allStackOperations.contains(incOperation)) {
 				if (stackTop.equals(incOperation)) {
 					logger.debug("Simplified to "+ (inc ? "x++" : "x--"));
 					// remove last statement and change top of stack
 					statements.remove(statements.size()-1);
 					stack.pop();
 					stack.push(inc ? new PostIncrementOperation(incOperation) : new PostDecrementOperation(incOperation));
 					return true;
 				}
 				
 				// check registers
 //				List<Operation> registerOperations = context.getRegisters();
 //				if (!registerOperations.isEmpty() && registerOperations.get(0) instanceof RegisterHandle) {
 //					RegisterHandle registerHandle = (RegisterHandle) registerOperations.get(0);
 //					if (registerHandle.getUndelrlyingOp() instanceof SimpleIncrementOperation) {
 //						SimpleIncrementOperation simpleIncrementOperation = (SimpleIncrementOperation) registerHandle.getUndelrlyingOp();
 //						if (incOperation.equals(simpleIncrementOperation.getOp())) {
 //							logger.debug("Simplified to ++x");
 //							// remove last statement and change register(0)
 //							statements.remove(statements.size()-1);
 //							statements.remove(registerHandle.getStoreRegisterOp());
 //							context.getRegisters().set(0, new PreIncrementOperation(incOperation));
 //							return true;
 //						}
 //					}
 //				}
 
 			}
 			
 			int getVariableCount = 0;
 			int getVariableIdx = 0;
 			Operation getVariableOp = null;
 			for (int stackIdx = 0; stackIdx < stack.size() ; stackIdx++) {
 				Operation stackOperation = stack.get(stackIdx);
 				if (stackOperation instanceof GetVariableOperation) {
 					GetVariableOperation variableOp = (GetVariableOperation) stackOperation; 
 					if (incOperation.equals(variableOp)) {
 						getVariableCount++;
 						getVariableIdx = stackIdx;
 						getVariableOp = variableOp; 
 					}
 				}
 			}
 			
 			if (getVariableCount == 1) {
 				logger.debug("One getVariable found. Replacing it with x++ and removing last statement.");
 				statements.remove(statements.size()-1);
 				stack.set(getVariableIdx, inc ?  new PostIncrementOperation(getVariableOp) : new PostDecrementOperation(getVariableOp));
 				return true;
 			}
 				
 		}
 		return false;
 	}
 
 	protected List<Operation> getAllUnderlyingOperations(Operation operation) {
 		List<Operation> underlyingOperations = new ArrayList<Operation>();
 		underlyingOperations.add(operation);
 		for (Operation op : operation.getOperations()) {
 			underlyingOperations.addAll(getAllUnderlyingOperations(op));
 		}
 		return underlyingOperations;
 		
 	}
 
 	protected void handlePush(Push action, Stack<Operation> stack) {
 		List<StackValue> stackValues = action.getValues();
 		for (StackValue stackValue : stackValues) {
 			
 			switch (stackValue.getType()) {
 				case StackValue.TYPE_STRING :
 				case StackValue.TYPE_FLOAT : 
 				case StackValue.TYPE_NULL : 
 				case StackValue.TYPE_UNDEFINED :
 				case StackValue.TYPE_BOOLEAN :
 				case StackValue.TYPE_DOUBLE :
 				case StackValue.TYPE_INTEGER :
 					logger.debug("V:"+stackValue);
 					stack.push(stackValue);
 					break;
 					
 				case StackValue.TYPE_CONSTANT_8 :
 					int index8 = stackValue.getConstant8();
 					Operation constant8 = (context.getConstants().size() > index8) ? new StackValue(context.getConstants().get(index8)) : new UndefinedStackValue();
 					logger.debug("V:"+stackValue+" => "+constant8);
 					stack.push(constant8);
 					break;
 				case StackValue.TYPE_CONSTANT_16 :
 					int index16 = stackValue.getConstant16();
 					Operation constant16 = (context.getConstants().size() > index16) ? new StackValue(context.getConstants().get(index16)) : new UndefinedStackValue();
 					logger.debug("V:"+stackValue+" => "+constant16);
 					stack.push(constant16);
 					break;
 					
 				case StackValue.TYPE_REGISTER :
 //					logger.debug("V:"+stackValue);
 					Operation registerValue;
 					if (context.getRegisters().size()>stackValue.getRegisterNumber()) { 
 						registerValue = context.getRegisters().get(stackValue.getRegisterNumber());
 						if (registerValue == null) {
 							registerValue = new NullStackValue();
 						}
 					} else {
 						logger.error("Reference to register #"+stackValue.getRegisterNumber()+", but registers size is "+context.getRegisters().size()+". Pushing undefined. (Fixed by breaking out here, to be verified!!!) This error causes a subsequent error POPing");
						//registerValue = new UndefinedStackValue();
                        break;
 					}
 
                     logger.debug("V:"+stackValue+" => "+registerValue);
 					stack.push(registerValue);
                     // Let's only do this for "simple" stack values
                     // Without it we would get all DefinedFunction2 parameters wrapped in an `eval`.
                     if (registerValue instanceof StackValue) {
                         stack.push(new GetVariableOperation(stack)); // This treats everything that is in a register as a variable, works in all tests I wrote, but that's all the proof I have (wk).
                     }
 					break;
 					
 					
 				default:
 					logger.error("Unknown stack value type = "+stackValue.getType());
 			}
 		}
 		
 	}
 	
 	private void postProcessStatements() {
 		postProcessIncrements();
 		postProcessFor();
 	}
 
 	private void postProcessFor() {
 		// TODO Check "for" statements
 		for (int j=0; j<statements.size(); j++) {
 			Operation statement = statements.get(j);
 			if (statement.getClass().equals(WhileOperation.class) && j>0) {
 				WhileOperation whileOperation = (WhileOperation)statement;
 				
 				// get condition
 				Operation condition = whileOperation.getCondition();
 				
 				// get previous statement
 				Operation prevStatement = statements.get(j-1);
 				
 				// get last while operation
 				if (!whileOperation.getInlineOperations().isEmpty()) {
 					Operation lastWhileOp = whileOperation.getInlineOperations().get(whileOperation.getInlineOperations().size()-1); 
 				}
 				
 			}
 		}
 	}
 
 	private void postProcessIncrements() {
 		for (int j=0; j<statements.size(); j++) {
 			Operation statement = statements.get(j);
 			
 		}
 		
 	}
 	
 	private static Operation getRealStatement(Operation statement) {
 		return (statement instanceof OperationFactory) ? ((OperationFactory)statement).getObject() : statement; 
 	}
 
 	public List<Operation> getOperations() {
 		return statements;
 	}
 
 
 
 	public void setExecutionContext(ExecutionContext context) {
 		this.context = context;
 	}
 	
 }
