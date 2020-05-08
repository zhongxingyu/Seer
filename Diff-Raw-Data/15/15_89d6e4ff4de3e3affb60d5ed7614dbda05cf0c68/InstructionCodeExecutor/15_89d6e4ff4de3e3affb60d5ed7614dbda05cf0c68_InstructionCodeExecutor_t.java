 package org.instructioncode;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.parser.Operator;
 import org.suite.Binder;
 import org.suite.Journal;
 import org.suite.doer.TermParser.TermOp;
 import org.suite.node.Atom;
 import org.suite.node.Int;
 import org.suite.node.Node;
 import org.suite.node.Reference;
 import org.suite.node.Tree;
 import org.util.Util;
 
 import com.google.common.collect.BiMap;
 import com.google.common.collect.HashBiMap;
 
 public class InstructionCodeExecutor {
 
 	private enum Insn {
 		ASSIGNFRAMEREG("ASSIGN-FRAME-REG"), //
 		ASSIGNINT_____("ASSIGN-INT"), //
 		ASSIGNOBJECT__("ASSIGN-OBJECT"), //
 		ASSIGNCLOSURE_("ASSIGN-CLOSURE"), //
 		BIND__________("BIND"), //
 		BINDUNDO______("BIND-UNDO"), //
 		CALL__________("CALL"), //
 		CALLCLOSURE___("CALL-CLOSURE"), //
 		CUTBEGIN______("CUT-BEGIN"), //
 		CUTEND________("CUT-END"), //
 		CUTFAIL_______("CUT-FAIL"), //
 		ENTER_________("ENTER"), //
 		EXIT__________("EXIT"), //
 		EXITVALUE_____("EXIT-VALUE"), //
 		EVALADD_______("EVAL-ADD"), //
 		EVALDIV_______("EVAL-DIV"), //
 		EVALEQ________("EVAL-EQ"), //
 		EVALGE________("EVAL-GE"), //
 		EVALGT________("EVAL-GT"), //
 		EVALLE________("EVAL-LE"), //
 		EVALLT________("EVAL-LT"), //
 		EVALMUL_______("EVAL-MUL"), //
 		EVALNE________("EVAL-NE"), //
 		EVALSUB_______("EVAL-SUB"), //
 		IFFALSE_______("IF-FALSE"), //
 		IFGE__________("IF-GE"), //
 		IFGT__________("IF-GT"), //
 		IFLE__________("IF-LE"), //
 		IFLT__________("IF-LT"), //
 		IFNOTEQUALS___("IF-NOT-EQ"), //
 		JUMP__________("JUMP"), //
 		LABEL_________("LABEL"), //
 		POP___________("POP"), //
 		PUSH__________("PUSH"), //
 		RETURN________("RETURN"), //
 		RETURNVALUE___("RETURN-VALUE"), //
 		SYS___________("SYS"), //
 		;
 
 		private String name;
 
 		private Insn(String name) {
 			this.name = name;
 		}
 	};
 
 	private final static BiMap<Insn, String> insnNames = HashBiMap.create();
 	static {
 		for (Insn insn : Insn.values())
 			insnNames.put(insn, insn.name);
 	}
 
 	private final static Map<Operator, Insn> evalInsns = Util.createHashMap();
 	static {
 		evalInsns.put(TermOp.PLUS__, Insn.EVALADD_______);
 		evalInsns.put(TermOp.DIVIDE, Insn.EVALDIV_______);
 		evalInsns.put(TermOp.EQUAL_, Insn.EVALEQ________);
 		evalInsns.put(TermOp.GE____, Insn.EVALGE________);
 		evalInsns.put(TermOp.GT____, Insn.EVALGT________);
 		evalInsns.put(TermOp.LE____, Insn.EVALLE________);
 		evalInsns.put(TermOp.LT____, Insn.EVALLT________);
 		evalInsns.put(TermOp.MULT__, Insn.EVALMUL_______);
 		evalInsns.put(TermOp.MINUS_, Insn.EVALSUB_______);
 		evalInsns.put(TermOp.NOTEQ_, Insn.EVALNE________);
 	}
 
 	private Map<Integer, Node> constantPool = new HashMap<Integer, Node>();
 
 	private final static Atom trueAtom = Atom.create("true");
 	private final static Atom falseAtom = Atom.create("false");
 
 	private final static int STACKSIZE = 256;
 
 	private static class Instruction {
 		private Insn insn;
 		private int op1, op2, op3;
 
 		public Instruction(Insn insn, int op1, int op2, int op3) {
 			this.insn = insn;
 			this.op1 = op1;
 			this.op2 = op2;
 			this.op3 = op3;
 		}
 
 		public String toString() {
 			return insn.name + " " + op1 + ", " + op2 + ", " + op3;
 		}
 	}
 
 	private Instruction instructions[];
 
 	public InstructionCodeExecutor(Node node) {
 		Tree tree;
 		List<Instruction> list = new ArrayList<Instruction>();
 		InstructionExtractor extractor = new InstructionExtractor();
 
 		while ((tree = Tree.decompose(node, TermOp.AND___)) != null) {
 			Instruction instruction = extractor.extract(tree.getLeft());
 			list.add(instruction);
 			node = tree.getRight();
 		}
 
 		instructions = list.toArray(new Instruction[list.size()]);
 	}
 
 	private class InstructionExtractor {
 		private List<Instruction> enters = new ArrayList<Instruction>();
 
 		private Instruction extract(Node node) {
 			List<Node> rs = new ArrayList<Node>(5);
 			Tree tree;
 
 			while ((tree = Tree.decompose(node, TermOp.SEP___)) != null) {
 				rs.add(tree.getLeft());
 				node = tree.getRight();
 			}
 
 			rs.add(node);
 
 			Atom instNode = (Atom) rs.get(1);
 			String instName = instNode.getName();
 			Insn insn;
 
 			if ("ASSIGN-BOOL".equals(instName))
 				insn = Insn.ASSIGNOBJECT__;
 			else if ("ASSIGN-STR".equals(instName))
 				insn = Insn.ASSIGNOBJECT__;
 			else if ("EVALUATE".equals(instName)) {
 				Atom atom = (Atom) rs.remove(4).finalNode();
 				TermOp operator = TermOp.find((atom).getName());
 				insn = evalInsns.get(operator);
 			} else if ("EXIT-FAIL".equals(instName)) {
 				rs.set(0, falseAtom);
 				insn = Insn.EXIT__________;
 			} else if ("EXIT-OK".equals(instName)) {
 				rs.set(0, trueAtom);
 				insn = Insn.EXIT__________;
 			} else
 				insn = insnNames.inverse().get(instName);
 
 			if (insn != null) {
 				Instruction instruction = new Instruction(insn //
 						, getRegisterNumber(rs, 2) //
 						, getRegisterNumber(rs, 3) //
 						, getRegisterNumber(rs, 4));
 
 				if (insn == Insn.ENTER_________)
 					enters.add(instruction);
 				else if (insn == Insn.RETURNVALUE___)
 					enters.remove(enters.size() - 1);
 
 				return instruction;
 			} else
 				throw new RuntimeException("Unknown opcode " + instName);
 		}
 
 		private int getRegisterNumber(List<Node> rs, int index) {
 			if (rs.size() > index) {
 				Node node = rs.get(index).finalNode();
 
 				if (node instanceof Int)
 					return ((Int) node).getNumber();
 				else if (node instanceof Reference) { // Transient register
 
 					// Allocates new register in current local frame
 					Instruction enter = enters.get(enters.size() - 1);
 					int registerNumber = enter.op1++;
 
 					((Reference) node).bound(Int.create(registerNumber));
 					return registerNumber;
 				} else
 					// ASSIGN-BOOL, ASSIGN-STR, PROVE
 					return allocateInPool(node);
 			} else
 				return 0;
 		}
 	}
 
 	private static class Frame {
 		private Frame previous;
 		private Object registers[];
 
 		private Frame(Frame previous, int frameSize) {
 			this.previous = previous;
 			registers = new Object[frameSize];
 		}
 	}
 
 	private static class Closure {
 		private Frame frame;
 		private int ip;
 
 		private Closure(Frame frame, int ip) {
 			this.frame = frame;
 			this.ip = ip;
 		}
 
 		public Closure clone() {
 			return new Closure(frame, ip);
 		}
 	}
 
 	private static class CutPoint {
 		private int journalPointer;
 		private int callStackPointer;
 
 		public CutPoint(int journalPointer, int callStackPointer) {
 			this.journalPointer = journalPointer;
 			this.callStackPointer = callStackPointer;
 		}
 	}
 
 	public Node execute() throws IOException {
 		Closure current = new Closure(null, 0);
 		Closure callStack[] = new Closure[STACKSIZE];
 		Object dataStack[] = new Object[STACKSIZE];
 		int csp = 0, dsp = 0;
 
 		Journal journal = new Journal();
 		int bindPoints[] = new int[STACKSIZE];
 		int bsp = 0;
 		List<CutPoint> cutPoints = new ArrayList<CutPoint>();
 
 		for (;;) {
 			Frame frame = current.frame;
 			Object regs[] = frame != null ? frame.registers : null;
 			int ip = current.ip++;
 			Instruction insn = instructions[ip];
 
 			// LogUtil.info("TRACE", ip + "> " + insn);
 
 			switch (insn.insn) {
 			case ASSIGNFRAMEREG:
 				int i = insn.op2;
 				while (i++ < 0)
 					frame = frame.previous;
 				regs[insn.op1] = frame.registers[insn.op3];
 				break;
 			case ASSIGNINT_____:
 				regs[insn.op1] = n(insn.op2);
 				break;
 			case ASSIGNOBJECT__:
 				regs[insn.op1] = constantPool.get(insn.op2);
 				break;
 			case ASSIGNCLOSURE_:
 				regs[insn.op1] = new Closure(frame, insn.op2);
 				break;
 			case BIND__________:
 				bindPoints[bsp++] = journal.getPointInTime();
 				if (!Binder.bind( //
 						(Node) regs[insn.op1], (Node) regs[insn.op2], journal))
 					current.ip = insn.op3; // Fail
 				break;
 			case BINDUNDO______:
 				journal.undoBinds(bindPoints[--bsp]);
 				break;
 			case CALL__________:
 				callStack[csp++] = current;
				current.ip = insn.op1;
 				break;
 			case CALLCLOSURE___:
 				callStack[csp++] = current;
 				current = ((Closure) regs[insn.op2]).clone();
 				break;
 			case CUTBEGIN______:
 				regs[insn.op1] = cutPoints.size();
 				cutPoints.add(new CutPoint(journal.getPointInTime(), csp));
 				break;
 			case CUTEND________:
 				journal.cutTo(cutPoints.get(insn.op1).journalPointer);
 				Util.truncate(cutPoints, insn.op1);
 				break;
 			case CUTFAIL_______:
 				CutPoint cutPoint = cutPoints.get(insn.op1);
 				journal.undoBinds(cutPoint.journalPointer);
 				csp = cutPoint.callStackPointer;
 				Util.truncate(cutPoints, insn.op1);
 				current.ip = insn.op2;
 				break;
 			case ENTER_________:
 				current.frame = new Frame(frame, insn.op1);
 				break;
 			case EVALADD_______:
 				regs[insn.op1] = n(g(regs[insn.op2]) + g(regs[insn.op3]));
 				break;
 			case EVALDIV_______:
 				regs[insn.op1] = n(g(regs[insn.op2]) / g(regs[insn.op3]));
 				break;
 			case EVALEQ________:
 				regs[insn.op1] = b(g(regs[insn.op2]) == g(regs[insn.op3]));
 				break;
 			case EVALGE________:
 				regs[insn.op1] = b(g(regs[insn.op2]) >= g(regs[insn.op3]));
 				break;
 			case EVALGT________:
 				regs[insn.op1] = b(g(regs[insn.op2]) > g(regs[insn.op3]));
 				break;
 			case EVALLE________:
 				regs[insn.op1] = b(g(regs[insn.op2]) <= g(regs[insn.op3]));
 				break;
 			case EVALLT________:
 				regs[insn.op1] = b(g(regs[insn.op2]) < g(regs[insn.op3]));
 				break;
 			case EVALNE________:
 				regs[insn.op1] = b(g(regs[insn.op2]) != g(regs[insn.op3]));
 				break;
 			case EVALMUL_______:
 				regs[insn.op1] = n(g(regs[insn.op2]) * g(regs[insn.op3]));
 				break;
 			case EVALSUB_______:
 				regs[insn.op1] = n(g(regs[insn.op2]) - g(regs[insn.op3]));
 				break;
 			case EXIT__________:
 				return (Node) regs[insn.op1];
 			case EXITVALUE_____:
 				return constantPool.get(insn.op1);
 			case IFFALSE_______:
 				if (regs[insn.op2] != trueAtom)
 					current.ip = insn.op1;
 				break;
 			case IFNOTEQUALS___:
 				if (regs[insn.op2] != regs[insn.op3])
 					current.ip = insn.op1;
 				break;
 			case JUMP__________:
 				current.ip = insn.op1;
 				break;
 			case PUSH__________:
 				dataStack[dsp++] = regs[insn.op1];
 				break;
 			case POP___________:
 				regs[insn.op1] = dataStack[--dsp];
 				break;
 			case RETURN________:
 				current = callStack[--csp];
 				break;
 			case RETURNVALUE___:
 				Object returnValue = regs[insn.op1]; // Saves return value
 				current = callStack[--csp];
 				current.frame.registers[instructions[current.ip - 1].op1] = returnValue;
 				break;
 			case SYS___________:
 				dsp -= insn.op3;
 				regs[insn.op2] = sys(constantPool.get(insn.op1), dataStack, dsp);
 			}
 		}
 	}
 
 	private Node sys(Node command, Object dataStack[], int dsp) {
 		Node result;
 
 		if (command == Atom.create("CONS")) {
 			Node left = (Node) dataStack[dsp + 1];
 			Node right = (Node) dataStack[dsp];
 			result = new Tree(TermOp.COLON_, left, right);
 		} else if (command == Atom.create("EMPTY"))
 			result = Atom.nil;
 		else if (command == Atom.create("IS-TREE"))
 			result = b(Tree.decompose((Node) dataStack[dsp]) != null);
 		else if (command == Atom.create("HEAD"))
 			result = Tree.decompose((Node) dataStack[dsp]).getLeft();
 		else if (command == Atom.create("TAIL"))
 			result = Tree.decompose((Node) dataStack[dsp]).getRight();
 		else
 			throw new RuntimeException("Unknown system call " + command);
 
 		return result;
 	}
 
 	private int allocateInPool(Node node) {
 		int pointer = constantPool.size();
 		constantPool.put(pointer, node);
 		return pointer;
 	}
 
 	private static Int n(int n) {
 		return Int.create(n);
 	}
 
 	private static Atom b(boolean b) {
 		return b ? trueAtom : falseAtom;
 	}
 
 	private static int g(Object node) {
 		return ((Int) node).getNumber();
 	}
 
 }
