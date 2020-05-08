 package org.instructionexecutor;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.instructionexecutor.InstructionUtil.Activation;
 import org.instructionexecutor.InstructionUtil.CutPoint;
 import org.instructionexecutor.InstructionUtil.Frame;
 import org.instructionexecutor.InstructionUtil.Instruction;
 import org.suite.Journal;
 import org.suite.doer.Binder;
 import org.suite.doer.Prover;
 import org.suite.doer.TermParser.TermOp;
 import org.suite.node.Atom;
 import org.suite.node.Node;
 import org.suite.node.Reference;
 import org.suite.node.Tree;
 import org.suite.predicates.SystemPredicates;
 import org.util.Util;
 
 public class LogicInstructionExecutor extends InstructionExecutor {
 
 	private Prover prover;
 	private Journal journal;
 	private SystemPredicates systemPredicates;
 
 	private static final int stackSize = 4096;
 
 	private int bindPoints[] = new int[stackSize];
 	private int bsp = 0;
 
 	private List<CutPoint> cutPoints = new ArrayList<>();
 
 	public LogicInstructionExecutor(Node node, Prover prover) {
 		super(node);
 		this.prover = prover;
 		journal = prover.getJournal();
 		systemPredicates = new SystemPredicates(prover);
 	}
 
 	@Override
 	protected void handle(Exec exec, Instruction insn) {
 		Activation current = exec.current;
 		Frame frame = current.frame;
 		Node regs[] = frame != null ? frame.registers : null;
 
 		switch (insn.insn) {
 		case BIND__________:
 			bindPoints[bsp++] = journal.getPointInTime();
 			if (!Binder.bind(regs[insn.op0], regs[insn.op1], journal))
 				current.ip = insn.op2; // Fail
 			break;
 		case CUTBEGIN______:
 			regs[insn.op0] = number(cutPoints.size());
 			cutPoints.add(new CutPoint(current, bsp, journal.getPointInTime()));
 			break;
 		case CUTFAIL_______:
 			int cutPointIndex = i(regs[insn.op0]);
 			CutPoint cutPoint = cutPoints.get(cutPointIndex);
 			Util.truncate(cutPoints, cutPointIndex);
 			exec.current = cutPoint.activation;
 			exec.current.ip = insn.op1;
 			bsp = cutPoint.bindStackPointer;
 			journal.undoBinds(cutPoint.journalPointer);
 			break;
 		case DECOMPOSETREE0:
 			bindPoints[bsp++] = journal.getPointInTime();
 			Node node = regs[insn.op0].finalNode();
 			TermOp op = TermOp.find(((Atom) constantPool.get(insn.op1)).getName());
 
 			Instruction insn1 = getInstructions()[current.ip++];
 			int rl = insn1.op0;
 			int rr = insn1.op1;
 
 			if (node instanceof Tree) {
 				Tree tree = (Tree) node;
 
 				if (tree.getOperator() == op) {
 					regs[rl] = tree.getLeft();
 					regs[rr] = tree.getRight();
 				} else
 					current.ip = insn.op2;
 			} else if (node instanceof Reference) {
 				Tree tree = Tree.create(op, regs[rl] = new Reference(), regs[rr] = new Reference());
				((Reference) node).bound(tree);
 			} else
 				current.ip = insn.op2;
 			break;
 		case BINDUNDO______:
 			journal.undoBinds(bindPoints[--bsp]);
 			break;
 		case PROVEINTERPRET:
 			if (!prover.prove(regs[insn.op0]))
 				current.ip = insn.op1;
 			break;
 		case PROVESYS______:
 			if (!systemPredicates.call(regs[insn.op0]))
 				current.ip = insn.op1;
 			break;
 		default:
 			super.handle(exec, insn);
 		}
 	}
 
 }
