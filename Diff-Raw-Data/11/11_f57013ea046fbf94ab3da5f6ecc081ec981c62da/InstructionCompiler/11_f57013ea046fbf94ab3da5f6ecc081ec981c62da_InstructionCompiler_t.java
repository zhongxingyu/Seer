 package org.instructionexecutor;
 
 import java.io.Closeable;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayDeque;
 import java.util.Arrays;
 import java.util.Deque;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import javax.tools.JavaCompiler;
 import javax.tools.StandardJavaFileManager;
 import javax.tools.ToolProvider;
 
 import org.instructionexecutor.CompiledRunUtil.CompiledRun;
 import org.instructionexecutor.InstructionUtil.Closure;
 import org.instructionexecutor.InstructionUtil.Insn;
 import org.instructionexecutor.InstructionUtil.Instruction;
 import org.suite.doer.Formatter;
 import org.suite.doer.TermParser.TermOp;
 import org.suite.node.Atom;
 import org.suite.node.Node;
 import org.suite.node.Tree;
 import org.util.IoUtil;
 import org.util.LogUtil;
 
 import com.google.common.collect.BiMap;
 import com.google.common.collect.HashBiMap;
 
 /**
  * Possible types: closure, int, node (atom/number/reference/tree)
  */
 public class InstructionCompiler {
 
 	protected BiMap<Integer, Node> constantPool = HashBiMap.create();
 	private AtomicInteger counter = new AtomicInteger();
 
 	private String basePathName;
 	private String packageName;
 	private String filename;
 	private String className;
 
 	private StringBuilder clazzsec = new StringBuilder();
 	private StringBuilder localsec = new StringBuilder();
 	private StringBuilder switchsec = new StringBuilder();
 
 	private int ip;
 	private Map<Integer, Integer> parentFrames = new HashMap<>();
 	private Deque<Integer> lastEnterIps = new ArrayDeque<>();
 	private Map<Integer, Class<?>[]> registerTypesByFrame = new HashMap<>();
 	private Class<?> registerTypes[];
 
 	private String compare = "comparer.compare(#{reg-node}, #{reg-node})";
 
 	public InstructionCompiler(String basePathName) {
 		this.basePathName = basePathName;
 		this.packageName = getClass().getPackage().getName();
 	}
 
 	public CompiledRun compile(Node node) throws IOException {
 		InstructionExtractor extractor = new InstructionExtractor(constantPool);
 		List<Instruction> instructions = extractor.extractInstructions(node);
 
 		// Find out the parent of closures.
 		// Assumes every ENTER has a ASSIGN-CLOSURE referencing it.
 		for (int ip = 0; ip < instructions.size(); ip++) {
 			Instruction insn = instructions.get(ip);
 
			// Recognize frames and their parents.
			// Assumes ENTER instruction should be after LABEL.
 			if (insn.insn == Insn.ASSIGNCLOSURE_)
				parentFrames.put(insn.op1 + 1, lastEnterIps.peek());
 			else if (insn.insn == Insn.ENTER_________)
 				lastEnterIps.push(ip);
 			else if (insn.insn == Insn.LEAVE_________)
 				lastEnterIps.pop();
 		}
 
 		Node constant;
 		String var0, s;
 
 		while (ip < instructions.size()) {
 			Instruction insn = instructions.get(ip);
 			int op0 = insn.op0, op1 = insn.op1, op2 = insn.op2;
 			app("case #{num}: // #{str}", ip, insn);
 
 			LogUtil.info("Compiling instruction (IP = " + ip + ") " + insn);
 			ip++;
 
 			switch (insn.insn) {
 			case ASSIGNCLOSURE_:
 				registerTypes[op0] = Closure.class;
 				app("#{reg} = new Closure(#{fr}, #{num})", op0, op1);
 				break;
 			case ASSIGNFRAMEREG:
 				int f = lastEnterIps.peek();
 				s = "";
				for (int i = op1; i < 0; i++) {
 					f = parentFrames.get(f);
 					s += ".previous";
 				}
 				registerTypes[op0] = registerTypesByFrame.get(f)[op2];
 				app("#{reg} = #{fr}#{str}.r#{num}", op0, s, op2);
 				break;
 			case ASSIGNCONST___:
 				registerTypes[op0] = Node.class;
 				constant = constantPool.get(op1);
 				app("#{reg} = #{str}", op0, defineConstant(constant));
 				break;
 			case ASSIGNINT_____:
 				registerTypes[op0] = int.class;
 				app("#{reg} = #{num}", op0, op1);
 				break;
 			case BIND__________:
 				app("bindPoints[bsp++] = journal.getPointInTime()");
 				app("if (!Binder.bind(#{reg-node}, #{reg-node}, journal)) #{jump}", op0, op1, op2);
 				break;
 			case BINDUNDO______:
 				app("journal.undoBinds(bindPoints[--bsp])");
 				break;
 			case CALL__________:
 				pushCallee();
 				app("ip = #{reg-num}", op0);
 				app("if (true) continue");
 				break;
 			case CALLCONST_____:
 				pushCallee();
 				app("#{jump}", op0);
 				break;
 			case CALLCLOSURE___:
 				app("if (#{reg-clos}.result == null) {", op0);
 				pushCallee();
 				app("frame = #{reg-clos}.frame", op0);
 				app("ip = #{reg-clos}.ip", op0);
 				app("if (true) continue");
 				app("} else returnValue = #{reg-clos}.result", op0);
 				break;
 			case COMPARE_______:
 				registerTypes[op0] = int.class;
 				app("left = (Node) ds[--dsp]");
 				app("right = (Node) ds[--dsp]");
 				app("#{reg} = comparer.compare(left, right)", op0);
 				break;
 			case CONS__________:
 				registerTypes[op0] = Node.class;
 				app("left = (Node) ds[--dsp]");
 				app("right = (Node) ds[--dsp]");
 				app("#{reg} = Tree.create(TermOp.AND___, left, right)", op0);
 				break;
 			case CUTBEGIN______:
 				registerTypes[op0] = int.class;
 				app("#{reg} = cpsp", op0);
 				app("cutPoint = new CutPoint()");
 				app("cutPoint.frame = #{fr}");
 				app("cutPoint.ip = ip");
 				app("cutPoint.bsp = bsp");
 				app("cutPoint.csp = csp");
 				app("cutPoint.jp = journal.getPointInTime()");
 				app("cutPoints[cpsp++] = cutPoint");
 				break;
 			case CUTFAIL_______:
 				app("int cpsp1 = #{reg}", op0);
 				app("cutPoint = cutPoints[cpsp1]");
 				app("while (cpsp > cpsp1) cutPoints[--cpsp] = null");
 				app("${fr} = (${fr-class}) cutPoint.frame");
 				app("bsp = cutPoint.bsp");
 				app("csp = cutPoint.csp");
 				app("journal.undoBinds(cutPoint.jp)");
 				app("ip = #{reg}", op1);
 				app("#{jump}", op1);
 				break;
 			case ENTER_________:
				lastEnterIps.push(ip - 1);
 				registerTypes = new Class<?>[op0];
 				registerTypesByFrame.put(lastEnterIps.peek(), registerTypes);
 				app("#{fr} = new #{fr-class}((#{prev-fr-class}) frame)");
 				break;
 			case ERROR_________:
 				app("throw new RuntimeException(\"Error termination\")");
 				break;
 			case EVALADD_______:
 				registerTypes[op0] = int.class;
 				app("#{reg} = #{reg-num} + #{reg-num}", op0, op1, op2);
 				break;
 			case EVALDIV_______:
 				registerTypes[op0] = int.class;
 				app("#{reg} = #{reg-num} / #{reg-num}", op0, op1, op2);
 				break;
 			case EVALEQ________:
 				registerTypes[op0] = int.class;
 				app("#{reg} = " + compare + " == 0", op0, op1, op2);
 				break;
 			case EVALGE________:
 				registerTypes[op0] = Node.class;
 				app("#{reg} = " + compare + " >= 0", op0, op1, op2);
 				break;
 			case EVALGT________:
 				registerTypes[op0] = Node.class;
 				app("#{reg} = " + compare + " > 0", op0, op1, op2);
 				break;
 			case EVALLE________:
 				registerTypes[op0] = Node.class;
 				app("#{reg} = " + compare + " <= 0", op0, op1, op2);
 				break;
 			case EVALLT________:
 				registerTypes[op0] = Node.class;
 				app("#{reg} = " + compare + " < 0", op0, op1, op2);
 				break;
 			case EVALNE________:
 				registerTypes[op0] = int.class;
 				app("#{reg} = " + compare + " != 0", op0, op1, op2);
 				break;
 			case EVALMOD_______:
 				registerTypes[op0] = int.class;
 				app("#{reg} = #{reg-num} %% #{reg-num}", op0, op1, op2);
 				break;
 			case EVALMUL_______:
 				registerTypes[op0] = int.class;
 				app("#{reg} = #{reg-num} * #{reg-num}", op0, op1, op2);
 				break;
 			case EVALSUB_______:
 				registerTypes[op0] = int.class;
 				app("#{reg} = #{reg-num} - #{reg-num}", op0, op1, op2);
 				break;
 			case EXIT__________:
 				app("if (true) return #{reg}", op0);
 				break;
 			case FORMTREE0_____:
 				insn = instructions.get(ip++);
 				registerTypes[insn.op1] = Tree.class;
 				app("#{reg} = Tree.create(TermOp.#{str}, #{reg-node}, #{reg-node})", insn.op1,
 						TermOp.find(((Atom) constantPool.get(insn.op0)).getName()), op0, op1);
 				break;
 			case HEAD__________:
 				registerTypes[op0] = Node.class;
 				app("#{reg} = Tree.decompose((Node) ds[--dsp]).getLeft()", op0);
 				break;
 			case IFFALSE_______:
 				app("if (!#{reg-bool}) #{jump}", op1, op0);
 				break;
 			case IFNOTEQUALS___:
 				app("if (#{reg} != #{reg}) #{jump}", op1, op2, op0);
 				break;
 			case ISTREE________:
 				registerTypes[op0] = Node.class;
 				app("#{reg} = Tree.decompose((Node) ds[--dsp]) != null ? TRUE : FALSE", op0);
 				break;
 			case JUMP__________:
 				app("#{jump}", op0);
 				break;
 			case LABEL_________:
 				break;
 			case LEAVE_________:
 				generateFrame();
 				lastEnterIps.pop();
 				break;
 			case LOG___________:
 				constant = constantPool.get(op0);
 				app("LogUtil.info(#{str}.toString())", defineConstant(constant));
 				break;
 			case LOG1__________:
 				registerTypes[op0] = Node.class;
 				app("node = (Node) ds[--dsp]", op0);
 				app("LogUtil.info(node.toString())");
 				app("#{reg} = node", op0);
 				break;
 			case LOG2__________:
 				registerTypes[op0] = Node.class;
 				app("LogUtil.info(((Node) ds[--dsp]).toString())");
 				app("#{reg} = (Node) ds[--dsp]", op0);
 				break;
 			case NEWNODE_______:
 				registerTypes[op0] = Node.class;
 				app("#{reg} = new Reference()", op0);
 				break;
 			case POP___________:
 				registerTypes[op0] = Node.class;
 				app("#{reg} = ds[--dsp]", op0);
 				break;
 			case PROVE_________:
 				registerTypes[op0] = Node.class;
 				app("#{reg} = InstructionUtil.execProve(proverConfig, (Node) ds[--dsp])", op0);
 				break;
 			case PROVESYS______:
 				app("if (!systemPredicates.call(#{reg-node})) #{jump}", op0, op1);
 				break;
 			case PUSH__________:
 				app("ds[dsp++] = #{reg-node}", op0);
 				break;
 			case PUSHCONST_____:
 				app("ds[dsp++] = Int.create(#{num})", op0);
 				break;
 			case REMARK________:
 				break;
 			case RETURN________:
 				popCaller();
 				app("if (true) continue");
 				break;
 			case RETURNVALUE___:
 				s = registerTypes[op0].getSimpleName(); // Return value type
 				var0 = "returnValue" + counter.getAndIncrement();
 				app("#{str} #{str} = #{reg}", s, var0, op0);
 				popCaller();
 				app("returnValue = #{str}", var0);
 				break;
 			case SETRESULT_____:
 				registerTypes[op0] = Node.class;
 				app("#{reg} = returnValue", op0);
 				break;
 			case SETCLOSURERES_:
 				registerTypes[op0] = Node.class;
 				app("#{reg} = returnValue", op0);
 				app("#{reg-clos}.result = #{reg}", op1, op0);
 				break;
 			case SUBST_________:
 				registerTypes[op0] = Node.class;
 				app("var = (Node) ds[--dsp])");
 				app("node = (Node) ds[--dsp])");
 				app("#{reg} = InstructionUtil.execSubst(node, var)", op0);
 				break;
 			case TAIL__________:
 				registerTypes[op0] = Node.class;
 				app("#{reg} = Tree.decompose((Node) ds[--dsp]).getRight()", op0);
 				break;
 			case TOP___________:
 				registerTypes[op0] = Node.class;
 				app("#{reg} = ds[dsp + #{num}]", op0, op1);
 				break;
 			default:
 				// TODO LogicInstructionExecutor.execute()
 				throw new RuntimeException("Unknown instruction " + insn);
 			}
 
 			app("break");
 		}
 
 		className = "CompiledRun" + counter.getAndIncrement();
 
 		String java = String.format("" //
 				+ "package " + packageName + "; \n" //
 				+ "import org.suite.*; \n" //
 				+ "import org.suite.doer.*; \n" //
 				+ "import org.suite.kb.*; \n" //
 				+ "import org.suite.node.*; \n" //
 				+ "import org.suite.predicates.*; \n" //
 				+ "import org.util.*; \n" //
 				+ "import " + Closeable.class.getCanonicalName() + "; \n" //
 				+ "import " + CompiledRun.class.getCanonicalName() + "; \n" //
 				+ "import " + CompiledRunUtil.class.getCanonicalName() + ".*; \n" //
 				+ "import " + IOException.class.getCanonicalName() + "; \n" //
 				+ "import " + TermOp.class.getCanonicalName() + "; \n" //
 				+ "\n" //
 				+ "public class %s implements CompiledRun { \n" //
 				+ "private static final int stackSize = 4096; \n" //
 				+ "\n" //
 				+ "private static final Atom FALSE = Atom.create(\"false\"); \n" //
 				+ "private static final Atom TRUE = Atom.create(\"true\"); \n" //
 				+ "\n" //
 				+ "private Closeable closeable; \n" //
 				+ "\n" //
 				+ "public %s(Closeable closeable) { this.closeable = closeable; } \n" //
 				+ "\n" //
 				+ "public void close() throws IOException { closeable.close(); } \n" //
 				+ "\n" //
 				+ "%s" //
 				+ "\n" //
 				+ "public Node exec(RuleSet ruleSet) { \n" //
 				+ "int ip = 0; \n" //
 				+ "Node returnValue = null; \n" //
 				+ "int cs[] = new int[stackSize]; \n" //
 				+ "Node ds[] = new Node[stackSize]; \n" //
 				+ "Object fs[] = new Object[stackSize]; \n" //
 				+ "int bindPoints[] = new int[stackSize]; \n" //
 				+ "CutPoint cutPoints[] = new CutPoint[stackSize]; \n" //
 				+ "int bsp = 0, csp = 0, dsp = 0, cpsp = 0; \n" //
 				+ "int n; \n" //
 				+ "Node node, var, left, right; \n" //
 				+ "Frame frame = null; \n" //
 				+ "CutPoint cutPoint; \n" //
 				+ "\n" //
 				+ "Comparer comparer = new Comparer(); \n" //
 				+ "Prover prover = new Prover(ruleSet); \n" //
 				+ "Journal journal = prover.getJournal(); \n" //
 				+ "SystemPredicates systemPredicates = new SystemPredicates(prover); \n" //
 				+ "\n" //
 				+ "%s \n" //
 				+ "\n" //
 				+ "while (true) { \n" //
 				// + "System.out.println(ip); \n" //
 				+ "switch(ip++) { \n" //
 				+ "%s \n" //
 				+ "default: \n" //
 				+ "} \n" //
 				+ "} \n" //
 				+ "} \n" //
 				+ "} \n" //
 		, className, className, clazzsec, localsec, switchsec);
 
 		String pathName = basePathName + "/" + packageName.replace('.', '/');
 		filename = pathName + "/" + className + ".java";
 		new File(pathName).mkdirs();
 
 		try (OutputStream os = new FileOutputStream(filename)) {
 			os.write(java.getBytes(IoUtil.charset));
 		}
 
 		return getCompiledRun();
 	}
 
 	private void generateFrame() {
 		app(clazzsec, "private static class #{fr-class} implements Frame {");
 		app(clazzsec, "private #{prev-fr-class} previous");
 		app(clazzsec, "private #{fr-class}(#{prev-fr-class} previous) { this.previous = previous; }");
 
 		for (int r = 0; r < registerTypes.length; r++) {
 			String typeName = registerTypes[r].getSimpleName();
 			app(clazzsec, "private #{str} r#{num}", typeName, r);
 		}
 
 		app(clazzsec, "}");
 
 		app(localsec, "#{fr-class} #{fr} = null");
 	}
 
 	private void pushCallee() {
 		app("cs[csp] = ip");
 		app("fs[csp] = #{fr}");
 		app("csp++");
 	}
 
 	private void popCaller() {
 		app("--csp");
 		app("ip = cs[csp]");
 		app("#{fr} = (#{fr-class}) fs[csp]");
 	}
 
 	private String defineConstant(Node node) {
 		node = node.finalNode();
 		String result = "const" + counter.getAndIncrement();
 		String decl = "private static final Node #{str} = Suite.parse(\"#{str}\")";
 		app(clazzsec, decl, result, Formatter.dump(node));
 		return result;
 	}
 
 	private void app(String fmt, Object... ps) {
 		app(switchsec, fmt, ps);
 	}
 
 	private void app(StringBuilder section, String fmt, Object... ps) {
 		List<Object> list = Arrays.asList(ps);
 		Iterator<Object> iter = list.iterator();
 
 		while (!fmt.isEmpty()) {
 			int pos0 = fmt.indexOf("#{");
 			int pos1 = fmt.indexOf("}", pos0);
 
 			String s0, s1, s2;
 
 			if (pos0 >= 0 && pos1 >= 0) {
 				s0 = fmt.substring(0, pos0);
 				s1 = fmt.substring(pos0 + 2, pos1);
 				s2 = fmt.substring(pos1 + 1);
 			} else {
 				s0 = fmt;
 				s1 = s2 = "";
 			}
 
 			section.append(s0);
 			section.append(substitute(s1, iter));
 			fmt = s2;
 		}
 
 		section.append(";\n");
 	}
 
 	private String substitute(String s, Iterator<Object> iter) {
 		int reg;
 		Integer frameNo = !lastEnterIps.isEmpty() ? lastEnterIps.peek() : null;
 		Integer parentFrameNo = frameNo != null ? parentFrames.get(frameNo) : null;
 
 		switch (s) {
 		case "fr":
 			s = String.format("f%d", frameNo);
 			break;
 		case "fr-class":
 			s = String.format("Frame%d", frameNo);
 			break;
 		case "jump": // Dummy if for suppressing dead code error
 			s = String.format("{ ip = %d; if (true) continue; }", iter.next());
 			break;
 		case "num":
 			s = String.format("%d", iter.next());
 			break;
 		case "prev-fr":
 			s = parentFrameNo != null ? String.format("f%d", parentFrameNo) : null;
 			break;
 		case "prev-fr-class":
 			s = parentFrameNo != null ? String.format("Frame%d", parentFrameNo) : "Object";
 			break;
 		case "reg":
 			s = reg((int) iter.next());
 			break;
 		case "reg-clos":
 			reg = (int) iter.next();
 			s = reg(reg);
 			if (Node.class.isAssignableFrom(registerTypes[reg]))
 				s = "((Closure) " + s + ")";
 			break;
 		case "reg-node":
 			reg = (int) iter.next();
 			s = reg(reg);
 			if (registerTypes[reg] == int.class)
 				s = "Int.create(" + s + ")";
 			break;
 		case "reg-num":
 			reg = (int) iter.next();
 			s = reg(reg);
 			if (Node.class.isAssignableFrom(registerTypes[reg]))
 				s = "((Int) " + s + ").getValue()";
 			break;
 		case "str":
 			s = String.format("%s", iter.next());
 		}
 
 		return s;
 	}
 
 	private String reg(int reg) {
 		return String.format("f%d.r%d", lastEnterIps.peek(), reg);
 	}
 
 	private CompiledRun getCompiledRun() throws IOException {
 		String binDir = basePathName;
 		new File(binDir).mkdirs();
 
 		JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
 
 		try (StandardJavaFileManager sjfm = jc.getStandardFileManager(null, null, null)) {
 			File file = new File(filename);
 
 			if (!jc.getTask(null //
 					, null //
 					, null //
 					, Arrays.asList("-d", binDir) //
 					, null //
 					, sjfm.getJavaFileObjects(file)).call())
 				throw new RuntimeException("Java compilation error");
 		}
 
 		URLClassLoader ucl = new URLClassLoader(new URL[] { new URL("file://" + binDir + "/") });
 		CompiledRun compiledRun;
 
 		try {
 			@SuppressWarnings("unchecked")
 			Class<? extends CompiledRun> clazz = (Class<? extends CompiledRun>) ucl.loadClass(packageName + "." + className);
 			LogUtil.info("Class " + clazz.getSimpleName() + " has been successfully loaded");
 			compiledRun = clazz.getConstructor(new Class<?>[] { Closeable.class }).newInstance(ucl);
 		} catch (ReflectiveOperationException ex) {
 			ucl.close();
 			throw new RuntimeException(ex);
 		}
 
 		return compiledRun;
 	}
 
 }
