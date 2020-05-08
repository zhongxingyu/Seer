 package org.instructionexecutor;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.parser.Operator;
 import org.suite.doer.TermParser.TermOp;
 import org.suite.node.Node;
 import org.suite.node.Tree;
 
 import com.google.common.collect.BiMap;
 import com.google.common.collect.HashBiMap;
 
 public class InstructionUtil {
 
 	private static final BiMap<Insn, String> insnNames = HashBiMap.create();
 	private static final Map<Operator, Insn> evalInsns = new HashMap<>();
 
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
 
 	public enum Insn {
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
 		EXIT__________("EXIT"), //
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
 		SETCLOSURERES_("SET-CLOSURE-RESULT"), //
 		STOREGLOBAL___("STORE-GLOBAL"), //
 		SERVICE_______("SERVICE"), //
 		TOP___________("TOP"), //
 		;
 
 		String name;
 
 		private Insn(String name) {
 			this.name = name;
 		}
 	}
 
 	protected static class Instruction {
 		protected Insn insn;
 		protected int op1, op2, op3;
 
 		protected Instruction(Insn insn, int op1, int op2, int op3) {
 			this.insn = insn;
 			this.op1 = op1;
 			this.op2 = op2;
 			this.op3 = op3;
 		}
 
 		public String toString() {
 			return insn.name + " " + op1 + ", " + op2 + ", " + op3;
 		}
 	}
 
 	protected static class Activation extends Closure {
 		protected Activation previous;
 
 		protected Activation(Closure closure, Activation previous) {
 			this(closure.frame, closure.ip, previous);
 		}
 
 		protected Activation(Frame frame, int ip, Activation previous) {
 			super(frame, ip);
 			this.previous = previous;
 		}
 	}
 
 	// Indicates a function call with a specified set of framed environment.
 	// Closure must extend Node in order to be put in a list (being cons-ed).
 	protected static class Closure extends Node {
 		protected Frame frame;
 		protected int ip;
 		protected Node result;
 
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
 		protected Activation activation;
 		protected int journalPointer;
 
 		protected CutPoint(Activation activation, int journalPointer) {
 			this.activation = activation;
 			this.journalPointer = journalPointer;
 		}
 	}
 
 	public static List<Node> extractTuple(Node node) {
 		List<Node> rs = new ArrayList<>(5);
 		Tree tree;
 
 		while ((tree = Tree.decompose(node, TermOp.TUPLE_)) != null) {
 			rs.add(tree.getLeft());
 			node = tree.getRight();
 		}
 
 		rs.add(node);
 		return rs;
 	}
 
 	public static Insn getEvalInsn(TermOp operator) {
 		return InstructionUtil.evalInsns.get(operator);
 	}
 
 	public static Insn getInsn(String insnName) {
 		return InstructionUtil.insnNames.inverse().get(insnName);
 	}
 
 }
