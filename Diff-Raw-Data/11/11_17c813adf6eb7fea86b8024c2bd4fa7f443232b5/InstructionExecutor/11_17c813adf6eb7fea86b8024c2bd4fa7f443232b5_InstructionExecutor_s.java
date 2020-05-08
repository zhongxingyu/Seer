 package org.instructionexecutor;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.parser.Operator;
 import org.suite.doer.TermParser.TermOp;
 import org.suite.node.Atom;
 import org.suite.node.Int;
 import org.suite.node.Node;
 import org.suite.node.Reference;
 import org.suite.node.Tree;
 import org.util.LogUtil;
 import org.util.Util;
 
 import com.google.common.collect.BiMap;
 import com.google.common.collect.HashBiMap;
 
 public class InstructionExecutor {
 
 	private static final BiMap<Insn, String> insnNames = HashBiMap.create();
 	private static final Map<Operator, Insn> evalInsns = Util.createHashMap();
 
 	protected BiMap<Integer, Node> constantPool = HashBiMap.create();
 
 	private static final Atom trueAtom = Atom.create("true");
 	private static final Atom falseAtom = Atom.create("false");
 
 	private static final int stackSize = 4096;
 
 	static {
 		for (Insn insn : Insn.values())
 			insnNames.put(insn, insn.name);
 
 		evalInsns.put(TermOp.PLUS__, Insn.EVALADD_______);
 		evalInsns.put(TermOp.DIVIDE, Insn.EVALDIV_______);
 		evalInsns.put(TermOp.EQUAL_, Insn.EVALEQ________);
 		evalInsns.put(TermOp.GE____, Insn.EVALGE________);
 		evalInsns.put(TermOp.GT____, Insn.EVALGT________);
 		evalInsns.put(TermOp.LE____, Insn.EVALLE________);
 		evalInsns.put(TermOp.LT____, Insn.EVALLT________);
 		evalInsns.put(TermOp.MULT__, Insn.EVALMUL_______);
 		evalInsns.put(TermOp.MINUS_, Insn.EVALSUB_______);
 		evalInsns.put(TermOp.MODULO, Insn.EVALMOD_______);
 		evalInsns.put(TermOp.NOTEQ_, Insn.EVALNE________);
 	}
 
 	protected enum Insn {
 		ASSIGNCLOSURE_("ASSIGN-CLOSURE"), //
 		ASSIGNCONST___("ASSIGN-CONSTANT"), //
 		ASSIGNFRAMEREG("ASSIGN-FRAME-REG"), //
 		ASSIGNGLOBAL__("ASSIGN-GLOBAL"), //
 		ASSIGNINT_____("ASSIGN-INT"), //
 		BIND__________("BIND"), //
 		BINDUNDO______("BIND-UNDO"), //
 		CALL__________("CALL"), //
 		CALLCONST_____("CALL-CONSTANT"), //
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
 		EVALMOD_______("EVAL-MOD"), //
 		EVALMUL_______("EVAL-MUL"), //
 		EVALNE________("EVAL-NE"), //
 		EVALSUB_______("EVAL-SUB"), //
 		FORMTREE0_____("FORM-TREE0"), //
 		FORMTREE1_____("FORM-TREE1"), //
 		IFFALSE_______("IF-FALSE"), //
 		IFGE__________("IF-GE"), //
 		IFGT__________("IF-GT"), //
 		IFLE__________("IF-LE"), //
 		IFLT__________("IF-LT"), //
 		IFNOTEQUALS___("IF-NOT-EQ"), //
 		JUMP__________("JUMP"), //
 		LABEL_________("LABEL"), //
 		LOG___________("LOG"), //
 		LEAVE_________("LEAVE"), //
 		NEWNODE_______("NEW-NODE"), //
 		POP___________("POP"), //
 		PROVESYS______("PROVE-SYS"), //
 		PUSH__________("PUSH"), //
 		PUSHCONST_____("PUSH-CONSTANT"), //
 		REMARK________("REMARK"), //
 		RETURN________("RETURN"), //
 		RETURNVALUE___("RETURN-VALUE"), //
 		STOREGLOBAL___("STORE-GLOBAL"), //
 		SYS___________("SYS"), //
 		TOP___________("TOP"), //
 		;
 
 		private String name;
 
 		private Insn(String name) {
 			this.name = name;
 		}
 	};
 
 	protected static class Instruction {
 		protected Insn insn;
 		protected int op1, op2, op3;
 
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
 
 	public InstructionExecutor(Node node) {
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
 
 			Atom instNode = (Atom) rs.get(1).finalNode();
 			String instName = instNode.getName();
 			Insn insn;
 
 			if ("ASSIGN-BOOL".equals(instName))
 				insn = Insn.ASSIGNCONST___;
 			else if ("ASSIGN-STR".equals(instName))
 				insn = Insn.ASSIGNCONST___;
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
 				else if (insn == Insn.LEAVE_________)
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
 
 	private int allocateInPool(Node node) {
 		Integer pointer = constantPool.inverse().get(node);
 
 		if (pointer == null) {
 			int pointer1 = constantPool.size();
 			constantPool.put(pointer1, node);
 			return pointer1;
 		} else
 			return pointer;
 	}
 
 	// Indicates a function call with a specified set of framed environment.
 	// Closure must extend Node in order to be put in a list (being cons-ed).
 	protected static class Closure extends Node {
 		protected Frame frame;
 		protected int ip;
 
 		protected Closure(Frame frame, int ip) {
 			this.frame = frame;
 			this.ip = ip;
 		}
 
 		protected Closure clone() {
 			return new Closure(frame, ip);
 		}
 
 		public String toString() {
 			return "frameSize = " + frame.registers.length + ", IP = " + ip;
 		}
 	}
 
 	protected static class Frame {
 		protected Frame previous;
 		protected Node registers[];
 
 		protected Frame(Frame previous, int frameSize) {
 			this.previous = previous;
 			registers = new Node[frameSize];
 		}
 	}
 
 	protected static class CutPoint {
 		protected int journalPointer;
 		protected int callStackPointer;
 
 		protected CutPoint(int journalPointer, int callStackPointer) {
 			this.journalPointer = journalPointer;
 			this.callStackPointer = callStackPointer;
 		}
 	}
 
 	public Node execute() {
 		Closure current = new Closure(null, 0);
 		Closure callStack[] = new Closure[stackSize];
 		Node dataStack[] = new Node[stackSize];
 		int i, csp = 0, dsp = 0;
 
 		for (;;) {
 			Frame frame = current.frame;
 			Node regs[] = frame != null ? frame.registers : null;
 			int ip = current.ip++;
 			Instruction insn = instructions[ip];
 
 			// org.util.LogUtil.info("TRACE", ip + "> " + insn);
 
 			switch (insn.insn) {
 			case ASSIGNCLOSURE_:
 				regs[insn.op1] = new Closure(frame, insn.op2);
 				break;
 			case ASSIGNFRAMEREG:
 				i = insn.op2;
 				while (i++ < 0)
 					frame = frame.previous;
 				regs[insn.op1] = frame.registers[insn.op3];
 				break;
 			case ASSIGNCONST___:
 				regs[insn.op1] = constantPool.get(insn.op2);
 				break;
 			case ASSIGNINT_____:
 				regs[insn.op1] = i(insn.op2);
 				break;
 			case CALL__________:
 				callStack[csp++] = current;
 				current = new Closure(frame, g(regs[insn.op1]));
 				break;
 			case CALLCONST_____:
 				callStack[csp++] = current;
 				current = new Closure(frame, insn.op1);
 				break;
 			case CALLCLOSURE___:
 				callStack[csp++] = current;
 				current = ((Closure) regs[insn.op2]).clone();
 				break;
 			case ENTER_________:
 				current.frame = new Frame(frame, insn.op1);
 				break;
 			case EVALADD_______:
 				regs[insn.op1] = i(g(regs[insn.op2]) + g(regs[insn.op3]));
 				break;
 			case EVALDIV_______:
 				regs[insn.op1] = i(g(regs[insn.op2]) / g(regs[insn.op3]));
 				break;
 			case EVALEQ________:
 				regs[insn.op1] = a(g(regs[insn.op2]) == g(regs[insn.op3]));
 				break;
 			case EVALGE________:
 				regs[insn.op1] = a(g(regs[insn.op2]) >= g(regs[insn.op3]));
 				break;
 			case EVALGT________:
 				regs[insn.op1] = a(g(regs[insn.op2]) > g(regs[insn.op3]));
 				break;
 			case EVALLE________:
 				regs[insn.op1] = a(g(regs[insn.op2]) <= g(regs[insn.op3]));
 				break;
 			case EVALLT________:
 				regs[insn.op1] = a(g(regs[insn.op2]) < g(regs[insn.op3]));
 				break;
 			case EVALNE________:
 				regs[insn.op1] = a(g(regs[insn.op2]) != g(regs[insn.op3]));
 				break;
 			case EVALMOD_______:
 				regs[insn.op1] = i(g(regs[insn.op2]) % g(regs[insn.op3]));
 				break;
 			case EVALMUL_______:
 				regs[insn.op1] = i(g(regs[insn.op2]) * g(regs[insn.op3]));
 				break;
 			case EVALSUB_______:
 				regs[insn.op1] = i(g(regs[insn.op2]) - g(regs[insn.op3]));
 				break;
 			case EXIT__________:
 				return (Node) regs[insn.op1];
 			case EXITVALUE_____:
 				return constantPool.get(insn.op1);
 			case FORMTREE0_____:
 				Node left = (Node) regs[insn.op1];
 				Node right = (Node) regs[insn.op2];
 				insn = instructions[current.ip++];
 				String operator = ((Atom) constantPool.get(insn.op1)).getName();
 				regs[insn.op2] = new Tree(TermOp.find(operator), left, right);
 				break;
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
 			case LABEL_________:
 				break;
 			case LOG___________:
				LogUtil.info("EXEC", constantPool.get(insn.op2).toString());
 				break;
 			case NEWNODE_______:
 				regs[insn.op1] = new Reference();
 				break;
 			case PUSH__________:
 				dataStack[dsp++] = regs[insn.op1];
 				break;
 			case PUSHCONST_____:
 				dataStack[dsp++] = i(insn.op1);
 				break;
 			case POP___________:
 				regs[insn.op1] = dataStack[--dsp];
 				break;
 			case REMARK________:
 				break;
 			case RETURN________:
 				current = callStack[--csp];
 				break;
 			case RETURNVALUE___:
 				Node returnValue = regs[insn.op1]; // Saves return value
 				current = callStack[--csp];
 				current.frame.registers[instructions[current.ip - 1].op1] = returnValue;
 				break;
 			case TOP___________:
 				regs[insn.op1] = dataStack[dsp + insn.op2];
 				break;
 			default:
 				int pair[] = execute( //
 						current, insn, callStack, csp, dataStack, dsp);
 				csp = pair[0];
 				dsp = pair[1];
 			}
 		}
 	}
 
 	protected int[] execute(Closure current, Instruction insn,
 			Closure callStack[], int csp, Object dataStack[], int dsp) {
 		throw new RuntimeException("Unknown instruction " + insn);
 	}
 
 	protected static Int i(int n) {
 		return Int.create(n);
 	}
 
 	protected static Atom a(boolean b) {
 		return b ? trueAtom : falseAtom;
 	}
 
 	protected static int g(Object node) {
 		return ((Int) node).getNumber();
 	}
 
 }
