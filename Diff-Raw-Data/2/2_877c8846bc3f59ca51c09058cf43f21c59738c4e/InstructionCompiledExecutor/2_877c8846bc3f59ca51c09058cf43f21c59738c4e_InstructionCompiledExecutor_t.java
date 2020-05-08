 package org.instructionexecutor;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.ArrayDeque;
 import java.util.Arrays;
 import java.util.Deque;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.instructionexecutor.InstructionUtil.Closure;
 import org.instructionexecutor.InstructionUtil.Insn;
 import org.instructionexecutor.InstructionUtil.Instruction;
 import org.suite.Suite;
 import org.suite.doer.Formatter;
 import org.suite.node.Atom;
 import org.suite.node.Int;
 import org.suite.node.Node;
 import org.suite.node.Reference;
 import org.suite.node.Tree;
 import org.util.IoUtil;
 import org.util.LogUtil;
 
 import com.google.common.collect.BiMap;
 import com.google.common.collect.HashBiMap;
 
 /**
  * TODO variant type for closure invocation return value
  * 
  * TODO variant type for stack-popped/-topped value
  * 
  * TODO unknown register for passing return value
  * 
  * Possible types: closure, int, node (atom/reference/tree)
  */
 public class InstructionCompiledExecutor {
 
 	protected BiMap<Integer, Node> constantPool = HashBiMap.create();
 	private AtomicInteger counter = new AtomicInteger();
 
 	private StringBuilder clazzsec = new StringBuilder();
 	private StringBuilder methodsec = new StringBuilder();
 	private StringBuilder swsec = new StringBuilder();
 
 	private int ip;
 	private Deque<Integer> lastEnterIps = new ArrayDeque<>();
 	private Class<?> registerTypes[];
 
 	private String compare = "comparer.compare(#{reg-node}, #{reg-node})";
 
 	private Atom COMPARE = Atom.create("COMPARE");
 
 	public InstructionCompiledExecutor(Node node) {
 		InstructionExtractor extractor = new InstructionExtractor(constantPool);
 		List<Instruction> instructions = extractor.extractInstructions(node);
 
 		// Find out the parent of closures.
 		// Assumes every ENTER has a ASSIGN-CLOSURE referencing it.
 		Map<Integer, Integer> parentFrames = new HashMap<>();
 		for (int ip = 0; ip < instructions.size(); ip++) {
 			Instruction insn = instructions.get(ip);
 
 			if (insn.insn == Insn.ASSIGNCLOSURE_)
 				parentFrames.put(insn.op2, lastEnterIps.peek());
 			else if (insn.insn == Insn.ENTER_________)
 				lastEnterIps.push(ip);
 			else if (insn.insn == Insn.LEAVE_________)
 				lastEnterIps.pop();
 		}
 
 		Node constant;
 		String var0, s;
 
 		for (ip = 0; ip < instructions.size(); ip++) {
 			Instruction insn = instructions.get(ip);
 			int op0 = insn.op0, op1 = insn.op1, op2 = insn.op2;
 			app("case #{num}: ", ip);
 
 			switch (insn.insn) {
 			case ASSIGNCLOSURE_:
 				registerTypes[op0] = Closure.class;
 				app("#{reg} = new Closure(#{fr}, #{num})", op0, op1);
 				break;
 			case ASSIGNFRAMEREG:
 				s = "";
 				for (int i = 0; i < op1; i++)
 					s += ".previous";
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
 			case CALL__________:
 				pushCallee();
 				app("ip = #{reg-num}", op0);
 				app("continue");
 				break;
 			case CALLCONST_____:
 				pushCallee();
 				app("#{jump}", op0);
 				break;
 			case CALLCLOSURE___:
 				registerTypes[op0] = Node.class;
 				app("if(#{reg-clos}.result == null) {", op1);
 				pushCallee();
 				app("ip = #{reg-clos}.ip", op1);
 				app("#{fr} = #{reg-clos}.frame", op1);
 				app("continue");
 				app("} else #{reg} = #{reg-clos}.result", op0, op1);
 				break;
 			case ENTER_________:
 				lastEnterIps.push(ip);
 				registerTypes = new Class<?>[op0];
 				var0 = "oldFrame" + counter.getAndIncrement();
 				app("#{fr-class} #{str} = #{fr}", parentFrames.get(ip), var0);
 				app("#{fr} = new #{fr-class}(#{fr})", ip);
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
 				app("return #{reg}", op0);
 				break;
 			case FORMTREE0_____:
 				registerTypes[op0] = Tree.class;
 				insn = instructions.get(ip++);
 				app("#{reg} = Tree.create(TermOp.#{str}, #{reg-node}, #{reg-node})", insn.op2,
 						((Atom) constantPool.get(op0)).getName(), op0, op1);
 				break;
 			case IFFALSE_______:
 				app("if (!#{reg-bool}) #{jump}", op1, op0);
 				break;
 			case IFNOTEQUALS___:
 				app("if (#{reg} != #{reg}) #{jump}", op1, op2, op0);
 				break;
 			case JUMP__________:
 				app("#{jump}", op0);
 				break;
 			case LABEL_________:
 				break;
 			case LEAVE_________:
 				app(clazzsec, "private static class #{fr-class} {", lastEnterIps.pop());
 				for (int r = 0; r < registerTypes.length; r++) {
 					String typeName = registerTypes[r].getSimpleName();
 					app(clazzsec, "private #{str} r#{num}", typeName, r);
 				}
 				app(clazzsec, "}");
 				app(methodsec, "#{fr-class} #{fr}");
 				break;
 			case LOG___________:
 				constant = constantPool.get(op0);
 				app("LogUtil.info(#{str}.toString())", defineConstant(constant));
 				break;
 			case NEWNODE_______:
 				registerTypes[op0] = Reference.class;
 				app("#{reg} = new Reference()", op0);
 				break;
 			case PUSH__________:
 				app("ds[dsp++] = #{reg-node}", op0);
 				break;
 			case PUSHCONST_____:
 				app("ds[dsp++] = Int.create(#{num})", op0);
 				break;
 			case POP___________:
 				registerTypes[op0] = Node.class;
 				app("#{reg} = ds[--dsp]", op0);
 				break;
 			case REMARK________:
 				break;
 			case RETURN________:
 				popCaller();
 				app("continue");
 				break;
 			case RETURNVALUE___:
 				s = registerTypes[op0].getSimpleName(); // Return value type
 				var0 = "returnValue" + counter.getAndIncrement();
 				app("#{str} #{str} = #{reg}", s, var0, op0);
 				popCaller();
 				app("returnValue = #{str}", var0);
 				break;
 			case SERVICE_______:
 				app("dsp -= #{num}", op2);
 				node = constantPool.get(op1);
 				// TODO FunInstructionExecutor.sys()
 				if (node == COMPARE) {
 					registerTypes[op0] = int.class;
 					app("Node left = (Node) ds[dsp + 1]");
 					app("Node right = (Node) ds[dsp]");
 					app("#{reg} = comparer.compare(left, right)", op0);
 				}
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
 			case TOP___________:
 				registerTypes[op0] = Node.class;
 				app("#{reg} = ds[dsp + #{num}]", op0, op1);
 				break;
 			default:
 				throw new RuntimeException("Unknown instruction " + insn);
 				// TODO LogicInstructionExecutor.execute()
 			}
 		}
 
 		String className = "CompiledRun" + counter.getAndIncrement();
 
 		String java = String.format("" //
 				+ "package org.compiled; \n" //
 				+ "import org.suite.node.*; \n" //
 				+ "import " + Closure.class.getCanonicalName() + "; \n" //
 				+ "import " + Int.class.getCanonicalName() + "; \n" //
 				+ "import " + LogUtil.class.getCanonicalName() + "; \n" //
 				+ "import " + Node.class.getCanonicalName() + "; \n" //
 				+ "import " + Suite.class.getCanonicalName() + "; \n" //
 				+ "public class %s { \n" //
 				+ "%s" //
 				+ "public static void exec() { \n" //
 				+ "int ip = 0; \n" //
 				+ "Node returnValue = null; \n" //
 				+ "%s \n" //
 				+ "while(true) switch(ip) { \n" //
 				+ "%s \n" //
 				+ "default: \n" //
 				+ "} \n" //
 				+ "} \n" //
 				+ "} \n" //
 		, className, clazzsec, methodsec, swsec);
 
		String filename = "src/main/java/org/instructionexecutor/" + className + ".java";
 		try (OutputStream os = new FileOutputStream(filename)) {
 			os.write(java.getBytes(IoUtil.charset));
 		} catch (IOException ex) {
 			throw new RuntimeException(ex);
 		}
 	}
 
 	private void pushCallee() {
 		app("cs[csp] = ip");
 		app("fs[csp] = #{fr}");
 		app("csp++");
 	}
 
 	private void popCaller() {
 		app("--csp");
 		app("ip = cs[csp]");
 		app("#{fr} = fs[csp]");
 	}
 
 	private String defineConstant(Node node) {
 		node = node.finalNode();
 		String result = "const" + counter.getAndIncrement();
 		String decl = "private static final Node #{str} = Suite.parse(\"#{str}\")";
 		app(clazzsec, decl, result, Formatter.dump(node));
 		return result;
 	}
 
 	private void app(String fmt, Object... ps) {
 		app(swsec, fmt, ps);
 	}
 
 	private void app(StringBuilder section, String fmt, Object... ps) {
 		List<Object> list = Arrays.asList(ps);
 		Iterator<Object> iter = list.iterator();
 
 		while (!fmt.isEmpty()) {
 			int pos0 = fmt.indexOf("#{");
 			int pos1 = fmt.indexOf("}", pos0);
 
 			String s0, s1, s2;
 
 			if (pos0 > 0 && pos1 > 0) {
 				s0 = fmt.substring(0, pos0);
 				s1 = fmt.substring(pos0 + 2, pos1);
 				s2 = fmt.substring(pos1 + 1);
 			} else {
 				s0 = fmt;
 				s1 = s2 = "";
 			}
 
 			int reg;
 
 			switch (s1) {
 			case "fr":
 				s1 = String.format("f%d", lastEnterIps.peek());
 				break;
 			case "fr-class":
 				s1 = String.format("Frame%d", iter.next());
 				break;
 			case "jump":
 				s1 = String.format("{ ip = %d; continue; }", iter.next());
 				break;
 			case "num":
 				s1 = String.format("%d", iter.next());
 				break;
 			case "reg":
 				s1 = reg((int) iter.next());
 				break;
 			case "reg-clos":
 				reg = (int) iter.next();
 				s1 = reg(reg);
 				if (registerTypes[reg] == Node.class)
 					s1 = "((Closure) " + s1 + ")";
 				break;
 			case "reg-node":
 				reg = (int) iter.next();
 				s1 = reg(reg);
 				if (registerTypes[reg] == int.class)
 					s1 = "Int.create(" + s1 + ")";
 				break;
 			case "reg-num":
 				reg = (int) iter.next();
 				s1 = reg(reg);
 				if (registerTypes[reg] == Node.class)
 					s1 = "((Int) " + s1 + ").getValue()";
 				break;
 			case "str":
 				s1 = String.format("%s", iter.next());
 			}
 
 			section.append(s0);
 			section.append(s1);
 			fmt = s2;
 		}
 
 		section.append(";\n");
 	}
 
 	private String reg(int reg) {
 		String s1;
 		s1 = String.format("f%d.r%d", lastEnterIps.peek(), reg);
 		return s1;
 	}
 
 }
