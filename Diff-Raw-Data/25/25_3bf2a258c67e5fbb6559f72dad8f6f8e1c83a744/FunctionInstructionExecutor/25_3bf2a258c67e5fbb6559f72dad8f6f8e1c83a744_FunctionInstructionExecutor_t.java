 package org.instructionexecutor;
 
 import java.io.IOException;
 
 import org.suite.doer.TermParser.TermOp;
 import org.suite.node.Atom;
 import org.suite.node.Int;
 import org.suite.node.Node;
 import org.suite.node.Tree;
 import org.suite.node.Vector;
 
 public class FunctionInstructionExecutor extends InstructionExecutor {
 
 	private static final Atom CONS = Atom.create("CONS");
 	private static final Atom EMPTY = Atom.create("EMPTY");
 	private static final Atom GETC = Atom.create("GETC");
 	private static final Atom HEAD = Atom.create("HEAD");
 	private static final Atom ISTREE = Atom.create("IS-TREE");
 	private static final Atom ISVECTOR = Atom.create("IS-VECTOR");
 	private static final Atom LOG = Atom.create("LOG");
 	private static final Atom LOG2 = Atom.create("LOG2");
 	private static final Atom TAIL = Atom.create("TAIL");
 	private static final Atom VCONCAT = Atom.create("VCONCAT");
 	private static final Atom VELEM = Atom.create("VELEM");
 	private static final Atom VEMPTY = Atom.create("VEMPTY");
 	private static final Atom VHEAD = Atom.create("VHEAD");
 	private static final Atom VRANGE = Atom.create("VRANGE");
 	private static final Atom VTAIL = Atom.create("VTAIL");
 
 	private StringBuilder inBuffer = new StringBuilder();
 
 	public FunctionInstructionExecutor(Node node) {
 		super(node);
 	}
 
 	@Override
 	protected int[] execute(Closure current, Instruction insn,
 			Closure callStack[], int csp, Object dataStack[], int dsp) {
 		Frame frame = current.frame;
 		Object regs[] = frame != null ? frame.registers : null;
 
 		switch (insn.insn) {
 		case SYS___________:
 			dsp -= insn.op3;
 			regs[insn.op1] = sys(constantPool.get(insn.op2), dataStack, dsp);
 			break;
 		default:
 			return super.execute(current, insn, callStack, csp, dataStack, dsp);
 		}
 
 		return new int[] { csp, dsp };
 	}
 
 	private Node sys(Node command, Object dataStack[], int dsp) {
 		Node result;
 
 		if (command == CONS) {
 			Node left = (Node) dataStack[dsp + 1];
 			Node right = (Node) dataStack[dsp];
 			result = new Tree(TermOp.AND___, left, right);
 		} else if (command == EMPTY)
 			result = Atom.nil;
 		else if (command == GETC)
 			try {
 				int n = ((Int) dataStack[dsp]).getNumber();
 
				while (n >= inBuffer.length()) {
					int c = System.in.read();
					if (c >= 0)
						inBuffer.append((char) c);
					else
						break;
				}
 
				int ch = n < inBuffer.length() ? inBuffer.charAt(n) : -1;
 				result = Int.create(ch);
 			} catch (IOException ex) {
 				throw new RuntimeException(ex);
 			}
 		else if (command == HEAD)
 			result = Tree.decompose((Node) dataStack[dsp]).getLeft();
 		else if (command == ISTREE)
 			result = a(Tree.decompose((Node) dataStack[dsp]) != null);
 		else if (command == ISVECTOR)
 			result = a(dataStack[dsp] instanceof Vector);
 		else if (command == LOG)
 			System.err.println(result = (Node) dataStack[dsp]);
 		else if (command == LOG2) {
 			System.err.println((Node) dataStack[dsp + 1]);
 			result = (Node) dataStack[dsp];
 		} else if (command == TAIL)
 			result = Tree.decompose((Node) dataStack[dsp]).getRight();
 		else if (command == VCONCAT) {
 			Vector left = (Vector) dataStack[dsp + 1];
 			Vector right = (Vector) dataStack[dsp];
 			result = Vector.concat(left, right);
 		} else if (command == VELEM)
 			result = new Vector((Node) dataStack[dsp]);
 		else if (command == VEMPTY)
 			result = Vector.EMPTY;
 		else if (command == VHEAD)
 			result = ((Vector) dataStack[dsp]).get(0);
 		else if (command == VRANGE) {
 			Vector vector = (Vector) dataStack[dsp + 2];
 			int s = ((Int) dataStack[dsp + 1]).getNumber();
 			int e = ((Int) dataStack[dsp]).getNumber();
 			return vector.subVector(s, e);
 		} else if (command == VTAIL)
 			result = ((Vector) dataStack[dsp]).subVector(1, 0);
 		else
 			throw new RuntimeException("Unknown system call " + command);
 
 		return result;
 	}
 }
