 package ash.compiler;
 
 import java.io.Serializable;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 import bruce.common.functional.Func1;
 
 import ash.lang.BasicType;
 import ash.lang.PersistentList;
 import ash.lang.ListUtils;
 import ash.lang.MacroExpander;
 import ash.lang.Node;
 import ash.lang.Symbol;
 import ash.vm.Instruction;
 import ash.vm.InstructionSetEnum;
 import ash.vm.JavaMethod;
 
 public final class Compiler {
 	private static final Symbol MULTI_ARGS_SIGNAL = Symbol.create(".");
 	private static final Set<String> NORMAL_INSTRUCTION_SET = new HashSet<>(Arrays.asList(
 			"def", "quote", "cond", "lambda",
 			"atom", "car", "cdr", "not",
 			"neq", "eq", "cons", "add", "sub", "mul", "div", "mod",
 			"gt", "ge", "lt", "le", "and", "or"));
 	
 	private Compiler() {}
 	
 	public static PersistentList astsToInsts(PersistentList parseResult) {
 		if (parseResult.isEndingNode()) return BasicType.NIL;
 		Object instlist = compile(parseResult.head(), BasicType.NIL, 0);
 		return new Node(instlist, astsToInsts(parseResult.rest()));
 	}
 	
 	private static Serializable compile(final Object exp, Node lambdaArgs, int startIndex) {
 		if (exp instanceof Node) {
 			Node node = (Node) exp; // (operation ...)
 			final Object op = node.head();
 			if (NORMAL_INSTRUCTION_SET.contains(op.toString())) {
 				switch (op.toString()) {
 				case "def":
 					return listInstruction(
 							compile(node.rest().rest().head(), lambdaArgs, startIndex),
 							InstructionSetEnum.asn.create(node.rest().head()));
 				case "quote":
 					return listInstruction(InstructionSetEnum.quote.create(node.rest().head()));
 				case "cond":
 					return compileCond(node.rest(), lambdaArgs, startIndex);
 				case "lambda":
 					int dotIndex = ListUtils.indexOf((Node) node.rest().head(), MULTI_ARGS_SIGNAL, 0);
 					boolean notCombineArgs = dotIndex == -1;
					PersistentList argsList = notCombineArgs
							? (PersistentList) node.rest().head()
							: removeDot((PersistentList) node.rest().head());
 					return listInstruction(
 							InstructionSetEnum.closure.create(
 									expand(listInstructionRecur(notCombineArgs ? 1 : 0,
 											InstructionSetEnum.cons_args.create(dotIndex),
 											compile(node.rest().rest().head(),
													ListUtils.append(argsList, lambdaArgs),
 													notCombineArgs ? 0 : 1),
 													InstructionSetEnum.ret.create()))));
 				default:
 					return listInstruction(
 							compileArgs(node.rest(), lambdaArgs, startIndex),
 							InstructionSetEnum.valueOf(op.toString()).create());
 				}
 			} else if (op instanceof Symbol && MacroExpander.hasMacro((Symbol) op, node)) { // (let ...)
 				return compile(MacroExpander.expand(node), lambdaArgs, startIndex);
 			} else { // (func ...) | (.str ...) | ((lambda ...) ...) | (closure@1a2b3c ...) <- only adapt for this situation (apply + '(...))
 				int argsCount = ListUtils.count(node.rest());
 				InstructionSetEnum callMethod = op instanceof Symbol && isJavaCallSymbol(((Symbol) op).name)
 						? InstructionSetEnum.java_call
 						: InstructionSetEnum.call;
 				return listInstruction(
 						compileArgs(node.rest(), lambdaArgs, startIndex),
 						compile(op, lambdaArgs, startIndex),
 						callMethod.create(argsCount));
 			}
 		} else if (exp instanceof Symbol) {
 			return listInstruction(compileSymbol((Symbol) exp, lambdaArgs)); // (... a add .puts ...)
 		} else
 			return listInstruction(InstructionSetEnum.ldc.create(exp)); // (... 1 2 3.4 \a "b" ...)
 	}
 
 	private static PersistentList removeDot(PersistentList seq) {
 		return ListUtils.filter(seq, new Func1<Boolean, Object>() {
 			@Override
 			public Boolean call(Object symbol) { return !MULTI_ARGS_SIGNAL.equals(symbol); }
 		});
 	}
 
 	protected static Serializable compileSymbol(final Symbol symbol, Node lambdaArgs) {
 		String methodName = symbol.name;
 		if (isJavaCallSymbol(methodName))
 			return InstructionSetEnum.ldc.create(JavaMethod.create(symbol)); // java method
 		else if (isJavaClassPathSymbol(methodName)) {
 			return InstructionSetEnum.ldc.create(symbol); // java class path
 		}
 
 		int symbolIndexOfArgs = findArgIndex(lambdaArgs, symbol);
 		if (symbolIndexOfArgs == -1) {
 			if (InstructionSetEnum.contains(methodName))
 				return InstructionSetEnum.ldc.create(InstructionSetEnum.valueOf(methodName).create()); // instruction
 			else
 				return InstructionSetEnum.ldv.create(symbol); // symbol
 		} else {
 			return InstructionSetEnum.ldp.create(symbolIndexOfArgs); // symbol index of params
 		}
 	}
 
 	private static boolean isJavaCallSymbol(final String op) {
 		return op.charAt(0) == '.' || op.indexOf('/') != -1;
 	}
 	
 	private static boolean isJavaClassPathSymbol(final String op) {
 		return Character.isUpperCase(op.charAt(0)) ||
 				op.charAt(0) != '.' && op.indexOf('.') != -1;
 	}
 
 	protected static int findArgIndex(Node lambdaArgs, final Symbol op) {
 		return ListUtils.indexOf(lambdaArgs, op, 0);
 	}
 
 	public static Serializable expand(Serializable instrNodes) {
 		//make sure only Instruction List appear in runtime
 		return (Serializable) ((Node) instrNodes).toList(Instruction.class);
 	}
 	
 	private static Node compileArgs(PersistentList args, Node lambdaArgs, int startIndex) {
 		if (args.isEndingNode()) return BasicType.NIL;
 		return listInstruction(compile(args.head(), lambdaArgs, startIndex), compileArgs(args.rest(), lambdaArgs, startIndex));
 	}
 
 	private static Serializable compileCond(PersistentList pairList, Node lambdaArgs, int startIndex) {
 		if (pairList.isEndingNode()) return listInstruction(InstructionSetEnum.quote.create(BasicType.NIL));
 		
 		Node headPair = (Node) pairList.head();
 		
 		Serializable condition = compile(headPair.head(), lambdaArgs, startIndex);
 		int condInstCount = countInstruction(condition) + 1;
 		
 		Serializable exp = compile(headPair.rest().head(), lambdaArgs, startIndex + condInstCount);
 		int nextCaseStartIndex = startIndex + condInstCount + countInstruction(exp) + 1;
 		
 		Serializable nextCase = compileCond(pairList.rest(), lambdaArgs, nextCaseStartIndex);
 		
 		return listInstruction(
 				condition,
 				InstructionSetEnum.jz.create(nextCaseStartIndex),
 				exp,
 				InstructionSetEnum.jmp.create(nextCaseStartIndex + countInstruction(nextCase)),
 				nextCase);
 	}
 
 	private static int countInstruction(Serializable ins) {
 		if (ins instanceof Instruction) return 1;
 		return ListUtils.count((Node) ins);
 	}
 	
 	public static Node listInstruction(Serializable... ins) { return listInstructionRecur(0, ins); }
 
 	public static Node listInstructionRecur(int skipParams, Serializable... ins) {
 		if (ins.length == skipParams) return BasicType.NIL;
 		Serializable s = ins[skipParams];
 		if (s instanceof Node)
 			return ListUtils.append((Node) s, listInstructionRecur(skipParams + 1, ins));
 		else
 			return new Node(s, listInstructionRecur(skipParams + 1, ins));
 	}
 }
