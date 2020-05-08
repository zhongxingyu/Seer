 package lobecompiler;
 
 import java.util.ArrayList;
 import java.util.Stack;
 
 public class LOBECompiler {
 
 	private LOBESymbolTable mSymbols;
 	public String mOutput;
 	private Variable[] mWorkingMemory;
 	private Stack<Variable> mIfs;
 	private Stack<Evaluable> mWhileConditions;
 	private Stack<Variable> mWhileVars;
 	private ArrayList<Integer> mCommandStartPositions;
 
 	public LOBECompiler() {
 		reset();
 	}
 	
 	/**
 	 * Resets state to that of a new compiler.
 	 */
 	private void reset() {
 		mSymbols = new LOBESymbolTable();
 		initWorkingMemory(10);
 		mOutput = "";
 		mIfs = new Stack<Variable>();
 		mWhileConditions = new Stack<Evaluable>();
 		mWhileVars = new Stack<Variable>();
 		mCommandStartPositions = new ArrayList<Integer>();
 	}
 	
 	public ArrayList<Integer> getCommandStartPositions() {
 		return mCommandStartPositions;
 	}
 	
 	/**
 	 * Compiles the given LOBE code and returns the compiled EAR code as a string.
 	 * @param LOBECode Code to compile.
 	 * @return The Compiled EAR code.
 	 * @throws InvalidParameterException
 	 */
 	public String compile(String LOBECode) 
 	    throws LobeCompilationException 
 	{
 		reset();
 		LOBEParser parser = new LOBEParser();
 		LOBEInstruction[] instructions = parser.parseAll(LOBECode);
 		for (LOBEInstruction instruction : instructions) {
 			execute(instruction);
 		}
 		return mOutput;
 	}
 
 	public void execute(LOBEInstruction instruction)
 	    throws LobeCompilationException 
 	{
 		mCommandStartPositions.add(mOutput.split("\\n+").length);
 		
 		// 'Case' statements don't have their own scope, so generic names are initialised here.
 		// This makes me sad. It's like global variables are a thing again.
         Value argval;
         Variable target;
         Variable ifVar;
         Evaluable whileCondition;
         Variable whileVar;
         Value whileValue;
         
 		switch(instruction.mCommand) {				   
 		     
 		    // -- PRINT statement --
 		    // Outputs the value of the given variable / expression.
     		case PRINT:
     		    
     			if (instruction.mArguments.length != 1) {
     				throw new InvalidParameterException("PRINT takes exactly one parameter.");
     			}
     			
     			// Reduce the expression in the argument to either a variable or a constant.
     			argval = instruction.mArguments[0].evaluate(this);
     			
     			if (argval instanceof Variable) {
     			    // If we're printing a variable, we only need to output the
     			    // value of that variable - which is just an OUT statement.
     				target = (Variable)argval;
     			}
     			else
     			{
     			    // If we're printing the value of a constant, we have to put it
     			    // into a cell first, as EAR's 'OUT' instruction only works on cells.
     				target = mSymbols.getNewInternalVariable(this);
     				mOutput += "MOV " + argval.getRef(this) + " " + target.getRef(this) + "\n";				
     			}			
     			
     			mOutput += "OUT " + target.getRef(this) + "\n";
     			break;
     		
     		// -- INPUT statement --
     		// Takes a variable name, prompts the user for input, and stores it in that variable.
     		case INPUT:
     			if (instruction.mArguments.length != 1) {
     				throw new InvalidParameterException("INPUT takes exactly one parameter.");
     			}    			   		
     			if (!(instruction.mArguments[0] instanceof Variable)) {
                     throw new InvalidParameterException(
                             "The argument to INPUT must be a variable.");
                 }    			    					
     			
     			target = (Variable)instruction.mArguments[0];
     			mOutput += "IN " + target.getRef(this) + "\n";    			
     			break;
     		
     		// -- SET statement --
     		// Assigns to the variable in the first argument the value of the expression in the
     		// second argument.
     		case SET:
     			if (instruction.mArguments.length != 2) {
     				throw new InvalidParameterException(
     						"SET takes exactly two parameters.");
     			}    			
     			if (!(instruction.mArguments[0] instanceof Variable)) {
     				throw new InvalidParameterException(
     						"The first argument to SET must be a variable.");
     			}			
     			
     			// Interpret the first argument as a variable, and reduce the
     			// second to either a Variable or a Constant.
     			target = (Variable) instruction.mArguments[0];    			    		
     			argval = instruction.mArguments[1].evaluate(this);
     			
     			if (!mSymbols.containsKey(target)) {
     			    // The given variable is not yet defined, so add an entry
     			    // in the symbol table.
     				mSymbols.addVariable(target);
     			}
     			
     			// The format of EAR's 'IN' instruction is such that if the 
     			// second argument is a variable, it must be preceded by an '@'.
     			String maybeAt = (argval instanceof Variable) ? "@" : "";
     			
     			mOutput += "COPY " + maybeAt + argval.getRef(this) 
     					+ " " + target.getRef(this) 
     					+ " " + mWorkingMemory[0].getRef(this) 
     					+ "\n\n";
     			break;
     	
     		// -- IF statement --
     		// The code within the IF block (i.e. until the matching ENDIF)
     		// is executed if and only if the conditional expression provided 
     		// as an argument is true.
     	    case IF:
     			if (instruction.mArguments.length != 1) {
     				throw new InvalidParameterException(
     						"IF takes exactly one parameter.");
     			}
     			
     			// Evaluate the conditional expression.
     			// It returns a value, which will be nonzero iff the
     			// condition is true.
     			Value conditional = instruction.mArguments[0].evaluate(this);
     			    			
     			// We need to copy said value into a cell that is protected
     			// throughout the course of the IF block - as we need to ZERO
     			// it to ENDIF.
     			ifVar = mSymbols.getNewInternalVariable(this);
     			maybeAt = (conditional instanceof Variable) ? "@" : "";
     			mOutput += "COPY " + maybeAt + conditional.getRef(this)
     					+ " " + ifVar.getRef(this) 
     					+ " " + mWorkingMemory[0].getRef(this) 
     					+ "\n";
     			mIfs.add(ifVar); // Remember for later ENDIF.
     			mSymbols.lockVariable(ifVar); // Don't garbage collect yet!
     			mOutput += "WHILE " + ifVar.getRef(this) + "\n\n";
     			break;
     			
     		// -- ENDIF statement --
     		// Signals the end of an IF block.
     	    case ENDIF:
     			if (instruction.mArguments.length != 0) {
     				throw new InvalidParameterException(
     						"ENDIF takes no parameters.");
     			}
     			
     			// ZERO the variable on which we were conditioning, so that the
     			// underlying EAR 'WHILE' block terminates.
     			ifVar = mIfs.pop();			
     			mOutput += "ZERO " + ifVar.getRef(this) + "\n";
     			mOutput += "ENDWHILE\n\n";
     			
     			// Unlock the variable so that it can be garbage collected.
     			mSymbols.unlockVariable(ifVar);
     			break;
     		
     		// -- WHILE statement --
     		// All the code in a WHILE block (i.e. until the matching ENDWHILE)
     		// is evaluated only if the given conditional expression is true;
     		// and is repeatedly evaluated until it happens that we reach the 
     		// end of the block and the conditional happens to be false.
     	    case WHILE:
     			if (instruction.mArguments.length != 1) {
     				throw new InvalidParameterException(
     						"WHILE takes exactly one parameter.");
     			} 
     			
     			whileCondition = instruction.mArguments[0];
     			whileValue = whileCondition.evaluate(this);
     			if (whileValue instanceof Constant) {
     			    // Our conditional comes out to a fixed constant.
     			    // So we save it into a variable so we can loop on it.
     			    whileVar = mSymbols.getNewInternalVariable(this);
     			    mOutput += "MOV " + whileValue.getRef(this) 
     			            + " " + whileVar.getRef(this) 
     			            + "\n";
     			}
     			else {
     			    whileVar = (Variable)whileValue;
     			}
     			
     			// Don't garbage collect the loop variable!
                 mSymbols.lockVariable(whileVar);    		
     		
                 // Remember the condition of the loop so we can re-evaluate it
                 // when we come to repeat. 
                 // Also remember the variable we're conditioning on, as we'll want
                 // to re-evaluate the conditional to that cell.
     			mWhileConditions.add(whileCondition);
     			mWhileVars.add(whileVar);
     			
     			mOutput += "WHILE " + whileVar.getRef(this) + "\n\n";
     			break;
     			
     		// -- ENDWHILE --
     		// Signals the end of a WHILE block.
     	    case ENDWHILE:
     			if (instruction.mArguments.length != 0) {
     				throw new InvalidParameterException(
     						"ENDWHILE takes no parameters.");
     			}
     			
     			// Re-evaluate the conditional to the same variable as before.
     			whileCondition = mWhileConditions.pop();
     			whileVar = mWhileVars.pop();    			
     			whileValue = whileCondition.evaluate(this, whileVar);
     			
     			// We can garbage collect the loop variable now.    			
     			mSymbols.unlockVariable(whileVar);
     			
     			mOutput += "ENDWHILE\n\n";
     			break;
     		
     		default:
     			throw new InvalidOperationTokenException("Invalid command "
     					+ instruction.mCommand);		
 		}
 		
 		mSymbols.clearInternalVars();
 	}
 
 	private void initWorkingMemory(int size) {
 		mWorkingMemory = new Variable[size];
 		for (int i = 0; i < size; i++) {
 			Variable v = new Variable("!w" + i);
 			mSymbols.put(v, -i);
 			mWorkingMemory[i] = v;
 		}
 	}
 
 	public String getRef(Constant c) {
 		return Integer.toString(c.getValue());
 	}
 
 	public String getRef(Variable v) {
 		return Integer.toString(getPointer(v));
 	}
 
 	public int getPointer(Variable v) {
 		if (!mSymbols.containsKey(v)) {
 		    // The variable isn't in our symbol table - so create it.
 			mSymbols.addVariable(v);
 		}
 		return mSymbols.get(v);
 	}
 	
 	public Variable backup(Variable v, Variable target, Variable workingCell) {
 		mOutput += "COPY @" + v.getRef(this) + " " 
 	                        + target.getRef(this) + " " 
 				            + workingCell.getRef(this) + "\n";
 		return target;
 	}
 
 	/**
 	 * Apply the operator 'op' to val1 and val2, and return the resulting 
 	 * value.
 	 * @param op The Operator to use.
 	 * @param val1 The LHS argument.
 	 * @param val2 The RHS argument.
 	 * @param targetVar If we yield a Variable as our answer, use this variable
 	 *        to store it.
 	 * @param forceVariable Always return a Variable, even if we could use a 
 	 *        literal constant.
 	 * @return
 	 */
 	public Value evaluate(Operator op, 
 	                      Value val1, 
 	                      Value val2, 
 	                      Variable targetVar, 
 	                      boolean forceVariable) 
 	    throws InvalidOperationTokenException {
 		Value result;
 		
 		// We require values to be either variables or constants.
 		assert(val1 instanceof Variable || val1 instanceof Constant);
 		assert(val2 instanceof Variable || val2 instanceof Constant);
 		
 		switch (op) {
 			case ADD:
 				if (val2 instanceof Variable) {
 					// 2nd arg is destroyed by ADD, so use a temp copy.
 					if (targetVar == null) {
 						targetVar = mSymbols.getNewInternalVariable(this);
 					}
 					val2 = backup((Variable)val2, targetVar, mWorkingMemory[0]);					
 				}				
 				if (val1 instanceof Variable) {
 					if (val2 instanceof Constant) {
 						// Can't use a constant as the second argument, so back 1st arg up
 						// and switch the order of the arguments.
 						if (targetVar == null) {
 							targetVar = mSymbols.getNewInternalVariable(this);
 						}
 						Value temp = backup((Variable)val1, targetVar, mWorkingMemory[0]);
 						val1 = val2;
 						val2 = temp;
 					}
 					else {
 						// Back up the first argument.						
 						val1 = backup((Variable)val1, 
 								      mSymbols.getNewInternalVariable(this),
 								      mWorkingMemory[0]);
 					}				
 				}
 				if (val1 instanceof Variable && val2 instanceof Constant) {
 					// 2nd arg is a constant - this isn't allowed in EAR but since 1st arg is a 
 					// variable we can swap them over.
 					Value temp = val1;
 					val1 = val2;
 					val2 = temp;					
 				}
 				if (val1 instanceof Constant && val2 instanceof Constant) {
 					// Both arguments are constants - we just return their sum as a constant.
 					int num1 = ((Constant)val1).getValue();
 					int num2 = ((Constant)val2).getValue();
 					result = new Constant(num1 + num2);
 				}
 				else {
 					String maybeAt = (val1 instanceof Variable) ? "@" : "";
 					mOutput += "ADD " + maybeAt + val1.getRef(this) +
 				                  " " + val2.getRef(this) +
 				                  "\n";
 					result = val2;
 				}
 				break;
 				
 			case SUB:
 				if (val2 instanceof Variable) {
 					// 2nd arg is destroyed by SUB, so use a temp copy.
 					if (targetVar == null) {
 						targetVar = mSymbols.getNewInternalVariable(this);
 					}
 					val2 = backup((Variable)val2, targetVar, mWorkingMemory[0]);					
 				}				
 				if (val1 instanceof Variable) {
 					if (val2 instanceof Constant) {
 						// Can't use a constant as the second argument, so back 1st arg up
 						// and switch the order of the arguments.
						Value temp = backup((Variable)val1, targetVar, mWorkingMemory[0]);
 						if (targetVar == null) {
 							targetVar = mSymbols.getNewInternalVariable(this);
 						}
 						val1 = val2;
 						val2 = temp;
 					}
 					else {
 						// Back up the first argument.						
 						val1 = backup((Variable)val1, 
 								      mSymbols.getNewInternalVariable(this),
 								      mWorkingMemory[0]);
 					}				
 				}
 				if (val1 instanceof Variable && val2 instanceof Constant) {
 					// 2nd arg is a constant - this isn't allowed in EAR but since 1st arg is a 
 					// variable we can swap them over.
 					Value temp = val1;
 					val1 = val2;
 					val2 = temp;					
 				}
 				if (val1 instanceof Constant && val2 instanceof Constant) {
 					// Both arguments are constants - we just return the answer as a constant.
 					int num1 = ((Constant)val1).getValue();
 					int num2 = ((Constant)val2).getValue();
 					result = new Constant(num1 - num2);
 				}
 				else {
 					String maybeAt = (val1 instanceof Variable) ? "@" : "";
 					mOutput += "SUB " + maybeAt + val1.getRef(this) +
 				                  " " + val2.getRef(this) +
 				                  "\n";
 					result = val2;
 				}
 				break;
 				
 			case MUL:
 				if (val1 instanceof Variable) {
 					// 1st arg is destroyed by MUL, so use a temp copy.
 					val1 = backup((Variable)val1, mWorkingMemory[0], mWorkingMemory[2]);					
 				}
 				if (val2 instanceof Variable) {
 					// 2nd arg is destroyed by MUL, so use a temp copy.
 					val2 = backup((Variable)val2, mWorkingMemory[1], mWorkingMemory[2]);					
 				}
 				if (val1 instanceof Constant && val2 instanceof Constant) {
 					// Both arguments are constants - we just return the product as a constant.
 					int num1 = ((Constant)val1).getValue();
 					int num2 = ((Constant)val2).getValue();
 					result = new Constant(num1 * num2);
 				}
 				else {
 					if (targetVar == null) {
 						targetVar = mSymbols.getNewInternalVariable(this);
 					}
 					String maybeAt1 = (val1 instanceof Variable) ? "@" : "";
 					String maybeAt2 = (val2 instanceof Variable) ? "@" : "";
 					mOutput += "MUL " + maybeAt1 + val1.getRef(this) +
 				                  " " + maybeAt2 + val2.getRef(this) +
 				                   " " + targetVar.getRef(this) +
 				                   " " + mWorkingMemory[2].getRef(this) +
 				                   "\n";
 					result = targetVar;
 				}
 				break;
 			default:
 				throw new InvalidOperationTokenException("Unknown operation token "
 						+ op.name());		
 		}	
 		if (forceVariable && !(result instanceof Variable)) {
 			if (targetVar == null) {
 				targetVar = mSymbols.getNewInternalVariable(this);
 			}
 		    storeValue(result, targetVar);
 		    result = targetVar;
 		}
 		return result;
 	}
 
 	public Variable evaluate(Predicate pred, Value val1, Value val2, Variable target) 
 	    throws InvalidOperationTokenException {
 		String arg1Name = val1.getRef(this);
 		String arg2Name = val2.getRef(this);
 		String resultCell;
 
 		String earCommand = "";
 		String maybeAt1 = (val1 instanceof Variable) ? "@" : "";
 		String maybeAt2 = (val2 instanceof Variable) ? "@" : "";
 		earCommand += "COPY " + maybeAt1 + arg1Name + " [[0]] [[6]] \n";
 		earCommand += "COPY " + maybeAt2 + arg2Name + " [[1]] [[6]] \n";
 
 		switch (pred) {
 		    case EQ:
     			earCommand += "MOV 1 [[4]]\n" + 
     		                  "WHILE [[0]]\n" + 
     					      "MOV 0 [[4]]\n" +
     					      "COPY @[[1]] [[2]] [[5]]\n" + 
     					      "MOV 1 [[3]]\n" +
     					      "WHILE [[2]]\n" + 
     					      "MOV 0 [[3]]\n" + 
     					      "MOV 1 [[4]]\n" +
     					      "SUB 1 [[0]] [[1]]\n" + 
     					      "ZERO [[2]]\n" + 
     					      "ENDWHILE\n" +
     					      "WHILE [[3]]\n" + 
     					      "ZERO [[0]]\n" + 
     					      "MOV 1 [[1]]\n" +
     					      "ZERO [[3]]\n" + 
     					      "ENDWHILE\n" + 
     					      "ENDWHILE\n" +
     					      "WHILE [[1]]\n" + 
     					      "MOV 0 [[4]]\n" + 
     					      "ZERO [[1]]\n" +
     					      "ENDWHILE\n";
     			resultCell = "[[4]]";
     			break;
 		
 		    case NEQ:
     			earCommand += "MOV 0 [[4]]\n" + 
     		                  "WHILE [[0]]\n" + 
     					      "MOV 1 [[4]]\n" +
     					      "COPY @[[1]] [[2]] [[5]]\n" + 
     					      "MOV 1 [[3]]\n" +
     					      "WHILE [[2]]\n" + 
     					      "MOV 0 [[3]]\n" + 
     					      "MOV 0 [[4]]\n" +
     					      "SUB 1 [[0]] [[1]]\n" + 
     					      "ZERO [[2]]\n" + 
     					      "ENDWHILE\n" +
     					      "WHILE [[3]]\n" + 
     					      "ZERO [[0]]\n" + 
     					      "MOV 1 [[1]]\n" +
     					      "ZERO [[3]]\n" + 
     					      "ENDWHILE\n" + 
     					      "ENDWHILE\n" +
     					      "WHILE [[1]]\n" + 
     					      "MOV 1 [[4]]\n" + 
     					      "ZERO [[1]]\n" +
     					      "ENDWHILE\n";
     			resultCell = "[[4]]";
     			break;
     		
 		    case LEQ:
     			earCommand += "MOV 1 [[4]]\n" + 
     		                  "WHILE [[0]]\n" + 
     					      "MOV 0 [[4]]\n" +
     					      "COPY @[[1]] [[2]] [[5]]\n" + 
     					      "MOV 1 [[3]]\n" +
     					      "WHILE [[2]]\n" + 
     					      "MOV 0 [[3]]\n" + 
     					      "MOV 1 [[4]]\n" +
     					      "SUB 1 [[0]] [[1]]\n" + 
     					      "ZERO [[2]]\n" + 
     					      "ENDWHILE\n" +
     					      "WHILE [[3]]\n" + 
     					      "ZERO [[0]]\n" + 
     					      "ZERO [[1]]\n" +
     					      "ZERO [[3]]\n" + 
     					      "ENDWHILE\n" + 
     					      "ENDWHILE\n" +
     					      "WHILE [[1]]\n" + 
     					      "MOV 1 [[4]]\n" + 
     					      "ZERO [[1]]\n" +
     					      "ENDWHILE\n";
     			resultCell = "[[4]]";
     			break;
     			
 		    case LT:
     			earCommand += "MOV 0 [[4]]\n" + 
     		                  "WHILE [[0]]\n" + 
     					      "MOV 0 [[4]]\n" +
     					      "COPY @[[1]] [[2]] [[5]]\n" + 
     					      "MOV 1 [[3]]\n" +
     					      "WHILE [[2]]\n" + 
     					      "MOV 0 [[3]]\n" + 
     					      "MOV 1 [[4]]\n" +
     					      "SUB 1 [[0]] [[1]]\n" + 
     					      "ZERO [[2]]\n" + 
     					      "ENDWHILE\n" +
     					      "WHILE [[3]]\n" + 
     					      "ZERO [[0]]\n" + 
     					      "ZERO [[1]]\n" +
     					      "ZERO [[3]]\n" + 
     					      "ZERO [[4]]\n" + 
     					      "ENDWHILE\n" +
     					      "MOV 0 [[4]]\n" +
     					      "ENDWHILE\n" + 
     					      "WHILE [[1]]\n" + 
     					      "MOV 1 [[4]]\n" +
     					      "ZERO [[1]]\n" + 
     					      "ENDWHILE\n";
     			resultCell = "[[4]]";
     			break;
     		
     		default:
     			resultCell = "";
     			throw new InvalidOperationTokenException("Unknown operation token "
     					+ pred.name());
 		}
 
 		String targetPointer = target.getRef(this);
 
 		earCommand += "COPY @" + resultCell + " " + targetPointer + " [[5]]\n";
 
 		for (int i = 0; i < 10; i++) {
 			String s = Integer.toString(i);
 			earCommand = earCommand.replaceAll("\\[\\[" + s + "\\]\\]",
 					mWorkingMemory[i].getRef(this));
 		}
 
 		mOutput += earCommand;
 
 		return target; // TODO;
 	}
 	
 	public Variable getNewInternalVariable() {
 	    return mSymbols.getNewInternalVariable(this);
 	}
 	
 	public void storeValue(Value val, Variable var) {
 	    String maybeAt = (val instanceof Variable) ? "@" : "";
 	    mOutput += "COPY " + maybeAt + val.getRef(this) 
 	            + " " + var.getRef(this)
 	            + " " + mWorkingMemory[0].getRef(this)
 	            + "\n";
 	}
 }
