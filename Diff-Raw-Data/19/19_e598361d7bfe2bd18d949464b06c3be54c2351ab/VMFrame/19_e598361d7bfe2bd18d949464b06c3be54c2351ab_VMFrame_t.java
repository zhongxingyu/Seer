 package ash.vm;
 
 import java.io.Serializable;
 import java.util.ArrayDeque;
 import java.util.Deque;
 import java.util.List;
 import java.util.Map;
 
 import ash.lang.BasicType;
 import ash.lang.ListUtils;
 import ash.lang.PersistentList;
 import ash.lang.Symbol;
 import bruce.common.utils.CommonUtils;
 
 public final class VMFrame implements Serializable {
 	private static final long serialVersionUID = 3322385890943332297L;
 	public static final boolean debugging = false;
 	private static final InstructionSetEnum[] INST_ARR = InstructionSetEnum.values();
 	
 	private static final Map<Symbol, Object> tempVar = VM.tempVar;
 	private Scope myScope;
 	private Object[] callArgs;
 	
 	private final Deque<Object> workingStack = new ArrayDeque<>();
 	private final List<Instruction> executingInsts;
 	private int runIndex;
 	boolean frameChanged = false;
 	VMFrame prevFrame;
 	VMFrame nextFrame;
 
 	public VMFrame(List<Instruction> executingInstructions, Scope parentScope) {
 		executingInsts = executingInstructions;
 		myScope = parentScope;
 	}
 
 	private void pushWorkingStack(Object ser) { workingStack.push(ser); }
 
 	public Object popWorkingStack() { return workingStack.pop(); }
 	
 	public Object popReturnValue() { return workingStack.isEmpty() ? BasicType.NIL : workingStack.pop(); }
 
 	public void prepareNextFrame(Closure closure, int paramsCount) {
 		nextFrame = new VMFrame(closure.ins, closure.env);
 		nextFrame.callArgs = createCallingArgs(paramsCount);
 		frameChanged = true;
 	}
 
 	private Object[] createCallingArgs(int paramsCount) {
 		Object[] args = new Object[paramsCount];
 		for (int i = args.length - 1; -1 < i; i--)
 			args[i] = popWorkingStack();
 		return args;
 	}
 
 	private Closure makeSubClosure(List<Instruction> rtn) {
 		return new Closure(rtn, new Scope(myScope, callArgs));
 	}
 
 	public void execUntilStackChange() {
 		while (!frameChanged) {
 			Instruction i = executingInsts.get(runIndex++);
 			if (debugging) {
 				if (i.args != null)
 					System.out.print(CommonUtils.buildString(makeIndent(this),
 							INST_ARR[i.ins], ' ', CommonUtils.displayArray(i.args, " ")));
 				else
 					System.out.print(CommonUtils.buildString(makeIndent(this), INST_ARR[i.ins]));
 			}
 			
 			exec(i.ins, i.args);
 			
 			if (debugging) {
 				System.out.print('\t');
 				System.out.println(workingStack);
 			}
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	private void exec(int ordinal, Object[] args) {
 		if (ordinal < 16) {
 			if (ordinal < 8) {
 				if (ordinal < 4) { // 0...4
 					if (ordinal < 2) {
 						if (ordinal == 0) { // ldp
 							int varIndex = (Integer) args[0]; // params
 							if (varIndex < callArgs.length)
 								pushWorkingStack(callArgs[varIndex]);
 							else
 								pushWorkingStack(myScope.queryArg(varIndex - callArgs.length));
 						} else { // ldv
 							pushWorkingStack(tempVar.get(args[0])); // symbol
 						}
 					} else { // ldc, quote
 						pushWorkingStack(args[0]);
 					}
 				} else { // 4...8
 					if (ordinal < 6) {
 						if (ordinal == 4) { //asn
 							if (tempVar.containsKey(args[0]))
 								System.out.println("Warnning: Redefining " + args[0]);
 							tempVar.put((Symbol) args[0], popWorkingStack());
 						} else { // cons_args
 							int dotIndex = (Integer) args[0];
 							Object[] newArgs = new Object[dotIndex + 1];
 							System.arraycopy(callArgs, 0, newArgs, 0, dotIndex);
 							newArgs[dotIndex] = ListUtils.toSeq(dotIndex, callArgs);
 							callArgs = newArgs;
 						}
 					} else {
 						if (ordinal == 6) { // closure
 							pushWorkingStack(makeSubClosure((List<Instruction>) args[0]));
 						} else { // jmp
 							runIndex = (Integer) args[0];
 						}
 					}
 				}
 			} else {
 				if (ordinal < 12) { // 8...12
 					if (ordinal < 10) {
 						if (ordinal == 8) { // jz
 							if (!ListUtils.transformBoolean(popWorkingStack()))
 								runIndex = (Integer) args[0];
 						} else { // tail
 							callArgs = createCallingArgs((Integer) args[0]);
 							workingStack.clear();
 							runIndex = 0;
 						}
 					} else {
 						if (ordinal == 10) { // java_call
 							pushWorkingStack(((JavaMethod) popWorkingStack()).call(createCallingArgs((Integer) args[0])));
 						} else { // call
 							Object func = popWorkingStack();
 							if (func instanceof Closure) { // symbol
 								prepareNextFrame((Closure) func, (Integer) args[0]);
 							} else if (func instanceof Instruction) { // instruction
 								exec(((Instruction) func).ins, args);
							} else if (func instanceof JavaMethod) { // java method
 								pushWorkingStack(((JavaMethod) func).call(createCallingArgs((Integer) args[0])));
							} else {
								throw new RuntimeException("Undifined method:" + func);
 							}
 						}
 					}
 				} else { // 12...16
 					if (ordinal < 14) {
 						if (ordinal == 12) { // ret
 							prevFrame.pushWorkingStack(popWorkingStack());
 							prevFrame.frameChanged = false;
 							prevFrame = null;
 						} else { // halt
 							if (workingStack.isEmpty())
 								pushWorkingStack(BasicType.NIL);
 							if (prevFrame != null) {
 								prevFrame.pushWorkingStack(popWorkingStack());
 								prevFrame.frameChanged = false;
 								prevFrame = null;
 							}
 						}
 						frameChanged = true;
 					} else {
 						if (ordinal == 14) { //atom
 							pushWorkingStack(ListUtils.atom(popWorkingStack()));
 						} else { // car
 							pushWorkingStack(ListUtils.car(PersistentList.cast(popWorkingStack())));
 						}
 					}
 				}
 			}
 		} else {
 			if (ordinal < 24) {
 				if (ordinal < 20) { // 16...20
 					if (ordinal < 18) {
 						if (ordinal == 16) { //cdr
 							pushWorkingStack(ListUtils.cdr(PersistentList.cast(popWorkingStack())));
 						} else { // cons
 							Object elem2 = popWorkingStack();
 							Object elem = popWorkingStack();
 							pushWorkingStack(ListUtils.cons(elem, PersistentList.cast(elem2)));
 						}
 					} else {
 						if (ordinal == 18) { // eq
 							Object elem2 = popWorkingStack();
 							Object elem = popWorkingStack();
 							pushWorkingStack(ListUtils.eq(elem, elem2));
 						} else { // neq
 							Object elem2 = popWorkingStack();
 							Object elem = popWorkingStack();
 							pushWorkingStack(ListUtils.transformBoolean(!elem.equals(elem2)));
 						}
 					}
 				} else { // 20...24
 					if (ordinal < 22) {
 						if (ordinal == 20) { // and
 							boolean elem2 = ListUtils.transformBoolean(popWorkingStack());
 							boolean elem = ListUtils.transformBoolean(popWorkingStack());
 							pushWorkingStack(ListUtils.transformBoolean(elem && elem2));
 						} else { // or
 							boolean elem2 = ListUtils.transformBoolean(popWorkingStack());
 							boolean elem = ListUtils.transformBoolean(popWorkingStack());
 							pushWorkingStack(ListUtils.transformBoolean(elem || elem2));
 						}
 					} else {
 						if (ordinal == 22) { // not
 							boolean elem = ListUtils.transformBoolean(popWorkingStack());
 							pushWorkingStack(ListUtils.transformBoolean(!elem));
 						} else { // add
 							Number n2 = (Number) popWorkingStack();
 							Number n1 = (Number) popWorkingStack();
 							pushWorkingStack(Maths.add(n1, n2));
 						}
 					}
 				}
 			} else {
 				if (ordinal < 28) { // 24...28
 					if (ordinal < 26) {
 						if (ordinal == 24) { // sub
 							Number n2 = (Number) popWorkingStack();
 							Number n1 = (Number) popWorkingStack();
 							pushWorkingStack(Maths.subtract(n1, n2));
 						} else { // mul
 							Number n2 = (Number) popWorkingStack();
 							Number n1 = (Number) popWorkingStack();
 							pushWorkingStack(Maths.multiply(n1, n2));
 						}
 					} else {
 						if (ordinal == 26) { // div
 							Number n2 = (Number) popWorkingStack();
 							Number n1 = (Number) popWorkingStack();
 							pushWorkingStack(Maths.divide(n1, n2));
 						} else { // mod
 							Number n2 = (Number) popWorkingStack();
 							Number n1 = (Number) popWorkingStack();
 							pushWorkingStack(Maths.modulus(n1, n2));
 						}
 					}
 				} else { // 28...32
 					if (ordinal < 30) {
 						if (ordinal == 28) { // gt
 							Number n2 = (Number) popWorkingStack();
 							Number n1 = (Number) popWorkingStack();
 							pushWorkingStack(ListUtils.transformBoolean(Maths.greaterThan(n1, n2)));
 						} else { // ge
 							Number n2 = (Number) popWorkingStack();
 							Number n1 = (Number) popWorkingStack();
 							pushWorkingStack(ListUtils.transformBoolean(Maths.greaterEqual(n1, n2)));
 						}
 					} else {
 						if (ordinal == 30) { // lt
 							Number n2 = (Number) popWorkingStack();
 							Number n1 = (Number) popWorkingStack();
 							pushWorkingStack(ListUtils.transformBoolean(Maths.lessThan(n1, n2)));
 						} else { // le
 							Number n2 = (Number) popWorkingStack();
 							Number n1 = (Number) popWorkingStack();
 							pushWorkingStack(ListUtils.transformBoolean(Maths.lessEqual(n1, n2)));
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	protected static final String makeIndent(VMFrame headFrame) {
 		int indent = calcIndent(headFrame, -1);
 		StringBuilder sb = new StringBuilder();
 		for (int i = 0; i < indent; i++)
 			sb.append('\t');
 		return sb.toString();
 	}
 
 	protected static final int calcIndent(VMFrame headFrame, int base) {
 		if (headFrame.prevFrame == null) return base;
 		return calcIndent(headFrame.prevFrame, base + 1);
 	}
 }
