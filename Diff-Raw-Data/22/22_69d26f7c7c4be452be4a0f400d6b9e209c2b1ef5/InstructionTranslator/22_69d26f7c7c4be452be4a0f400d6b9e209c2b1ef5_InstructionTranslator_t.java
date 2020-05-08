 package org.instructionexecutor;
 
 import java.io.Closeable;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import javax.tools.JavaCompiler;
 import javax.tools.StandardJavaFileManager;
 import javax.tools.ToolProvider;
 
 import org.instructionexecutor.InstructionUtil.FunComparer;
 import org.instructionexecutor.InstructionUtil.Insn;
 import org.instructionexecutor.InstructionUtil.Instruction;
 import org.instructionexecutor.TranslatedRunUtil.TranslatedRun;
 import org.suite.doer.Formatter;
 import org.suite.doer.TermParser.TermOp;
 import org.suite.node.Atom;
 import org.suite.node.Node;
 import org.util.IoUtil;
 import org.util.LogUtil;
 
 import com.google.common.collect.BiMap;
 import com.google.common.collect.HashBiMap;
 
 /**
  * Possible register types: boolean, closure, int, node
  * (atom/number/reference/tree)
  */
 public class InstructionTranslator {
 
 	private static AtomicInteger counter = new AtomicInteger();
 
 	private BiMap<Integer, Node> constantPool = HashBiMap.create();
 
 	private String basePathName;
 	private String packageName;
 	private String filename;
 	private String className;
 
 	private StringBuilder clazzsec = new StringBuilder();
 	private StringBuilder localsec = new StringBuilder();
 	private StringBuilder switchsec = new StringBuilder();
 
 	private InstructionAnalyzer analyzer = new InstructionAnalyzer();
 
 	private int currentIp;
 
 	private String compare = "comparer.compare(#{reg-node}, #{reg-node})";
 
 	public InstructionTranslator(String basePathName) {
 		this.basePathName = basePathName;
 		this.packageName = getClass().getPackage().getName();
 	}
 
 	public TranslatedRun translate(Node node) throws IOException {
 		InstructionExtractor extractor = new InstructionExtractor(constantPool);
 		List<Instruction> instructions = extractor.extractInstructions(node);
 
 		int exitPoint = instructions.size();
 		instructions.add(new Instruction(Insn.LABEL_________, 0, 0, 0));
 		instructions.add(new Instruction(Insn.EXIT__________, 0, 0, 0));
 
 		analyzer.analyze(instructions);
 		translateInstructions(instructions);
 
 		className = "TranslatedRun" + counter.getAndIncrement();
 
 		String java = String.format("" //
 				+ "package %s; \n" //
 				+ "import org.instructionexecutor.io.*; \n" //
 				+ "import org.suite.*; \n" //
 				+ "import org.suite.doer.*; \n" //
 				+ "import org.suite.kb.*; \n" //
 				+ "import org.suite.node.*; \n" //
 				+ "import org.suite.predicates.*; \n" //
 				+ "import org.util.*; \n" //
 				+ "import org.util.FunUtil.*; \n" //
 				+ "import " + Closeable.class.getCanonicalName() + "; \n" //
 				+ "import " + FunComparer.class.getCanonicalName() + "; \n" //
 				+ "import " + TranslatedRun.class.getCanonicalName() + "; \n" //
 				+ "import " + TranslatedRunUtil.class.getCanonicalName() + ".*; \n" //
 				+ "import " + IOException.class.getCanonicalName() + "; \n" //
 				+ "import " + TermOp.class.getCanonicalName() + "; \n" //
 				+ "\n" //
 				+ "public class %s implements TranslatedRun { \n" //
 				+ "private static final int stackSize = 4096; \n" //
 				+ "\n" //
 				+ "private static final Atom FALSE = Atom.FALSE; \n" //
 				+ "private static final Atom TRUE = Atom.TRUE; \n" //
 				+ "\n" //
 				+ "private IndexedIo indexedIo = new IndexedIo(); \n" //
 				+ "\n" //
 				+ "private Closeable closeable; \n" //
 				+ "\n" //
 				+ "public %s(Closeable closeable) { this.closeable = closeable; } \n" //
 				+ "\n" //
 				+ "public void close() throws IOException { closeable.close(); } \n" //
 				+ "\n" //
 				+ "%s" //
 				+ "\n" //
 				+ "public Node exec(TranslatedRunConfig config, Closure closure) { \n" //
 				+ "Frame frame = closure.frame; \n" //
 				+ "int ip = closure.ip; \n" //
 				+ "Node returnValue = null; \n" //
 				+ "int cs[] = new int[stackSize]; \n" //
 				+ "Node ds[] = new Node[stackSize]; \n" //
 				+ "Object fs[] = new Object[stackSize]; \n" //
 				+ "int csp = 0, dsp = 0, cpsp = 0; \n" //
 				+ "int n; \n" //
 				+ "Node node, n0, n1, var; \n" //
 				+ "\n" //
 				+ "Prover prover = new Prover(config.ruleSet); \n" //
 				+ "Journal journal = prover.getJournal(); \n" //
 				+ "SystemPredicates systemPredicates = new SystemPredicates(prover); \n" //
 				+ "Fun<Node, Node> unwrapper = TranslatedRunUtil.getUnwrapper(config, this); \n" //
 				+ "Comparer comparer = new FunComparer(unwrapper); \n" //
 				+ "\n" //
 				+ "%s \n" //
 				+ "\n" //
 				+ "cs[csp++] = " + exitPoint + "; \n" //
 				+ "\n" //
 				+ "while (true) { \n" //
 				// + "System.out.println(ip); \n" //
 				+ "switch(ip) { \n" //
 				+ "%s \n" //
 				+ "default: \n" //
 				+ "} \n" //
 				+ "} \n" //
 				+ "} \n" //
 				+ "} \n" //
 		, packageName, className, className, clazzsec, localsec, switchsec);
 
 		String pathName = basePathName + "/" + packageName.replace('.', '/');
 		filename = pathName + "/" + className + ".java";
 		new File(pathName).mkdirs();
 
 		try (OutputStream os = new FileOutputStream(filename)) {
 			os.write(java.getBytes(IoUtil.charset));
 		}
 
 		// Compile the Java, load the class, return an instantiated object
 		return getTranslatedRun();
 	}
 
 	private void translateInstructions(List<Instruction> instructions) {
 		Node constant;
 		String s;
 		int ip = 0;
 		boolean isGenerateLabel = true;
 
 		while (ip < instructions.size()) {
 			Instruction insn = instructions.get(currentIp = ip++);
 			int op0 = insn.op0, op1 = insn.op1, op2 = insn.op2;
 
 			app("// (#{num}) #{str}", currentIp, insn);
 
 			if (isGenerateLabel || insn.insn == Insn.LABEL_________) {
 				app("case #{num}:", currentIp);
 				isGenerateLabel = false;
 			}
 
 			switch (insn.insn) {
 			case ASSIGNCLOSURE_:
 				app("#{reg} = new Closure(#{fr}, #{num})", op0, op1);
 				break;
 			case ASSIGNFRAMEREG:
 				s = "";
 				for (int i = op1; i < 0; i++)
 					s += ".previous";
 				app("#{reg} = TranslatedRunUtil.toNode(#{fr}#{str}.r#{num})", op0, s, op2);
 				break;
 			case ASSIGNCONST___:
 				constant = constantPool.get(op1);
 				app("#{reg} = #{str}", op0, defineConstant(constant));
 				break;
 			case ASSIGNINT_____:
 				app("#{reg} = #{num}", op0, op1);
 				break;
 			case BACKUPCSP_____:
 				app("#{reg} = csp", op0);
 				break;
 			case BACKUPDSP_____:
 				app("#{reg} = dsp", op0);
 				break;
 			case BIND__________:
 				app("if (!Binder.bind(#{reg-node}, #{reg-node}, journal)) #{jump}", op0, op1, op2);
 				break;
 			case BINDMARK______:
 				app("#{reg} = journal.getPointInTime()", op0);
 				break;
 			case BINDUNDO______:
 				app("journal.undoBinds(#{reg-num})", op0);
 				break;
 			case CALL__________:
				backupFrame();
 				pushCallee(ip);
 				app("#{jump}", op0);
 				isGenerateLabel = true;
 				break;
 			case CALLCLOSURE___:
 				app("if (#{reg-clos}.result == null) {", op0);
				backupFrame();
 				pushCallee(ip);
 				app("frame = #{reg-clos}.frame", op0);
 				app("ip = #{reg-clos}.ip", op0);
 				app("continue");
 				app("} else returnValue = #{reg-clos}.result", op0);
 				isGenerateLabel = true;
 				break;
 			case CALLREG_______:
				backupFrame();
 				pushCallee(ip);
 				app("ip = #{reg-num}", op0);
 				app("continue");
 				isGenerateLabel = true;
 				break;
 			case COMPARE_______:
 				app("n0 = (Node) ds[--dsp]");
 				app("n1 = (Node) ds[--dsp]");
 				app("#{reg} = comparer.compare(n0, n1)", op0);
 				break;
 			case CONS__________:
 				app("n0 = (Node) ds[--dsp]");
 				app("n1 = (Node) ds[--dsp]");
 				app("#{reg} = Tree.create(TermOp.AND___, n0, n1)", op0);
 				break;
 			case DECOMPOSETREE0:
 				app("node = #{reg-node}.finalNode()", op0);
 				insn = instructions.get(ip++);
 				app("if (node instanceof Tree) {");
 				app("Tree tree = (Tree) node;");
 				app("if (tree.getOperator() == TermOp.#{str}) {", insn.op0);
 				app("#{reg} = tree.getLeft()", insn.op1);
 				app("#{reg} = tree.getRight()", insn.op2);
 				app("} else #{jump}", op1);
 				app("} else if (node instanceof Reference) {");
 				app("Tree tree = Tree.create(op, #{reg} = new Reference(), #{reg} = new Reference())", insn.op1, insn.op2);
 				app("journal.addBind((Reference) node, tree)");
 				app("} else #{jump}", op1);
 				break;
 			case ENTER_________:
 				boolean isRequireParent = analyzer.isRequireParent(analyzer.getFrame(currentIp));
 				String previousFrame = isRequireParent ? "(#{prev-fr-class}) frame" : "null";
 				app("#{fr} = new #{fr-class}(" + previousFrame + ")");
 				break;
 			case ERROR_________:
 				app("throw new RuntimeException(\"Error termination\")");
 				break;
 			case EVALADD_______:
 				app("#{reg} = #{reg-num} + #{reg-num}", op0, op1, op2);
 				break;
 			case EVALDIV_______:
 				app("#{reg} = #{reg-num} / #{reg-num}", op0, op1, op2);
 				break;
 			case EVALEQ________:
 				app("#{reg} = " + compare + " == 0", op0, op1, op2);
 				break;
 			case EVALGE________:
 				app("#{reg} = " + compare + " >= 0", op0, op1, op2);
 				break;
 			case EVALGT________:
 				app("#{reg} = " + compare + " > 0", op0, op1, op2);
 				break;
 			case EVALLE________:
 				app("#{reg} = " + compare + " <= 0", op0, op1, op2);
 				break;
 			case EVALLT________:
 				app("#{reg} = " + compare + " < 0", op0, op1, op2);
 				break;
 			case EVALNE________:
 				app("#{reg} = " + compare + " != 0", op0, op1, op2);
 				break;
 			case EVALMOD_______:
 				app("#{reg} = #{reg-num} % #{reg-num}", op0, op1, op2);
 				break;
 			case EVALMUL_______:
 				app("#{reg} = #{reg-num} * #{reg-num}", op0, op1, op2);
 				break;
 			case EVALSUB_______:
 				app("#{reg} = #{reg-num} - #{reg-num}", op0, op1, op2);
 				break;
 			case EXIT__________:
 				Integer frame = currentFrame();
 				if (frame != null)
 					app("return #{reg}", op0);
 				else
 					app("return returnValue"); // Grand exit point
 				break;
 			case FGETC_________:
 				app("{");
 				app("n0 = (Node) ds[--dsp]");
 				app("int p = ((Int) ds[--dsp]).getNumber()");
 				app("#{reg} = Int.create(indexedIo.get(n0).read(p))", op0);
 				app("}");
 				break;
 			case FORMTREE0_____:
 				insn = instructions.get(ip++);
 				app("#{reg} = Tree.create(TermOp.#{str}, #{reg-node}, #{reg-node})", insn.op1,
 						TermOp.find(((Atom) constantPool.get(insn.op0)).getName()), op0, op1);
 				break;
 			case HEAD__________:
 				app("#{reg} = Tree.decompose((Node) ds[--dsp]).getLeft()", op0);
 				break;
 			case IFFALSE_______:
 				app("if (!#{reg-bool}) #{jump}", op1, op0);
 				break;
 			case IFNOTEQUALS___:
 				app("if (#{reg} != #{reg}) #{jump}", op1, op2, op0);
 				break;
 			case ISTREE________:
 				app("#{reg} = Tree.decompose((Node) ds[--dsp]) != null", op0);
 				break;
 			case JUMP__________:
 				app("#{jump}", op0);
 				isGenerateLabel = true;
 				break;
 			case JUMPREG_______:
 				app("{ ip = #{reg-num}; continue; }", op0);
 				isGenerateLabel = true;
 				break;
 			case LABEL_________:
 				break;
 			case LEAVE_________:
 				generateFrame();
 				break;
 			case LOG___________:
 				constant = constantPool.get(op0);
 				app("LogUtil.info(#{str}.toString())", defineConstant(constant));
 				break;
 			case LOG1__________:
 				app("n0 = (Node) ds[--dsp]", op0);
 				app("LogUtil.info(n0.toString())");
 				app("#{reg} = n0", op0);
 				break;
 			case LOG2__________:
 				app("LogUtil.info(((Node) ds[--dsp]).toString())");
 				app("#{reg} = (Node) ds[--dsp]", op0);
 				break;
 			case NEWNODE_______:
 				app("#{reg} = new Reference()", op0);
 				break;
 			case POP___________:
 				app("#{reg} = ds[--dsp]", op0);
 				break;
 			case POPANY________:
 				app("--dsp");
 				break;
 			case POPEN_________:
 				app("n0 = ds[--dsp]");
 				app("n1 = ds[--dsp]");
 				app("#{reg} = InstructionUtil.execPopen(n0, n1, indexedIo, unwrapper)", op0);
 				break;
 			case PROVE_________:
 				app("#{reg} = InstructionUtil.execProve((Node) ds[--dsp], prover.config())", op0);
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
 			case RESTORECSP____:
 				app("csp = #{reg-num}", op0);
 				break;
 			case RESTOREDSP____:
 				app("dsp = #{reg-num}", op0);
 				break;
 			case RETURN________:
 				popCaller();
 				break;
 			case RETURNVALUE___:
 				app("returnValue = #{reg-node}", op0);
 				popCaller();
 				break;
 			case SETRESULT_____:
				restoreFrame();
 				app("#{reg} = returnValue", op0);
 				break;
 			case SETCLOSURERES_:
				restoreFrame();
 				app("#{reg} = returnValue", op0);
 				app("#{reg-clos}.result = #{reg}", op1, op0);
 				break;
 			case SUBST_________:
 				app("n0 = (Node) ds[--dsp]");
 				app("n1 = (Node) ds[--dsp]");
 				app("#{reg} = InstructionUtil.execSubst(n1, n0)", op0);
 				break;
 			case TAIL__________:
 				app("#{reg} = Tree.decompose((Node) ds[--dsp]).getRight()", op0);
 				break;
 			case TOP___________:
 				app("#{reg} = ds[dsp + #{num}]", op0, op1);
 				break;
 			default:
 				throw new RuntimeException("Unknown instruction " + insn);
 			}
 		}
 	}
 
 	private void generateFrame() {
 		Integer frameNo = currentFrame();
 		Class<?> registerTypes[] = frameNo != null ? registerTypes = analyzer.getRegisterTypes(frameNo) : null;
 
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
 
 	private void pushCallee(int ip) {
 		app("cs[csp] = " + ip);
 		app("csp++");
 	}
 
 	private void popCaller() {
 		app("--csp");
 		app("ip = cs[csp]");
 		app("continue");
 	}
 
	private void backupFrame() {
		app("fs[csp] = #{fr}");
	}

	private void restoreFrame() {
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
 			section.append(decode(s1, iter));
 			fmt = s2;
 		}
 
 		char lastChar = section.charAt(section.length() - 1);
 
 		if (lastChar != ';' && lastChar != '{' && lastChar != '}')
 			section.append(";");
 
 		section.append("\n");
 	}
 
 	private String decode(String s, Iterator<Object> iter) {
 		int reg;
 		Integer frameNo = currentFrame();
 		Integer parentFrameNo = frameNo != null ? analyzer.getParentFrame(frameNo) : null;
 		Class<?> registerTypes[] = frameNo != null ? analyzer.getRegisterTypes(frameNo) : null;
 
 		switch (s) {
 		case "fr":
 			s = String.format("f%d", frameNo);
 			break;
 		case "fr-class":
 			s = String.format("Frame%d", frameNo);
 			break;
 		case "jump":
 			s = String.format("{ ip = %d; continue; }", iter.next());
 			break;
 		case "num":
 			s = String.format("%d", iter.next());
 			break;
 		case "prev-fr":
 			s = parentFrameNo != null ? String.format("f%d", parentFrameNo) : "frame";
 			break;
 		case "prev-fr-class":
 			s = parentFrameNo != null ? String.format("Frame%d", parentFrameNo) : "Frame";
 			break;
 		case "reg":
 			s = reg((int) iter.next());
 			break;
 		case "reg-bool":
 			reg = (int) iter.next();
 			s = reg(reg);
 			if (Node.class.isAssignableFrom(registerTypes[reg]))
 				s = "(" + s + " == TRUE)";
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
 			Class<?> sourceClazz = registerTypes[reg];
 			if (sourceClazz == boolean.class)
 				s = "(" + s + " ? TRUE : FALSE)";
 			else if (sourceClazz == int.class)
 				s = "Int.create(" + s + ")";
 			break;
 		case "reg-num":
 			reg = (int) iter.next();
 			s = reg(reg);
 			if (Node.class.isAssignableFrom(registerTypes[reg]))
 				s = "((Int) " + s + ").getNumber()";
 			break;
 		case "str":
 			s = String.format("%s", iter.next());
 		}
 
 		return s;
 	}
 
 	private String reg(int reg) {
 		return String.format("f%d.r%d", currentFrame(), reg);
 	}
 
 	private Integer currentFrame() {
 		return analyzer.getFrame(currentIp);
 	}
 
 	private TranslatedRun getTranslatedRun() throws IOException {
 		LogUtil.info("Translating run " + filename);
 
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
 
 		LogUtil.info("Loading class " + className);
 
 		URLClassLoader ucl = new URLClassLoader(new URL[] { new URL("file://" + binDir + "/") });
 		TranslatedRun translatedRun;
 
 		try {
 			@SuppressWarnings("unchecked")
 			Class<? extends TranslatedRun> clazz = (Class<? extends TranslatedRun>) ucl.loadClass(packageName + "." + className);
 			translatedRun = clazz.getConstructor(new Class<?>[] { Closeable.class }).newInstance(ucl);
 		} catch (ReflectiveOperationException ex) {
 			ucl.close();
 			throw new RuntimeException(ex);
 		}
 
 		return translatedRun;
 	}
 
 }
