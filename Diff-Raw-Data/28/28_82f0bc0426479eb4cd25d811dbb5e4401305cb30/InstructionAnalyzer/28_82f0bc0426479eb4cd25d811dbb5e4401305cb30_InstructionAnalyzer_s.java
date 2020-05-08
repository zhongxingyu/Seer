 package suite.instructionexecutor;
 
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.Deque;
 import java.util.List;
 
 import suite.instructionexecutor.InstructionUtil.Closure;
 import suite.instructionexecutor.InstructionUtil.Insn;
 import suite.instructionexecutor.InstructionUtil.Instruction;
 import suite.node.Node;
 import suite.node.Tree;
 
 public class InstructionAnalyzer {
 
 	private List<AnalyzedFrame> framesByIp = new ArrayList<>();
 
 	public static class AnalyzedFrame {
 		private int id;
 		private boolean isRequireParent = false;
 		private AnalyzedFrame parent;
 		private List<AnalyzedRegister> registers;
 
 		public AnalyzedFrame(int id) {
 			this.id = id;
 		}
 
 		public int getId() {
 			return id;
 		}
 
 		public boolean isRequireParent() {
 			return isRequireParent;
 		}
 
 		public AnalyzedFrame getParent() {
 			return parent;
 		}
 
 		public List<AnalyzedRegister> getRegisters() {
 			return registers;
 		}
 	}
 
 	public static class AnalyzedRegister {
 		private Class<?> clazz;
 		private boolean isUsedExternally = false;
 
 		public Class<?> getClazz() {
 			return clazz;
 		}
 
 		public boolean isUsedExternally() {
 			return isUsedExternally;
 		}
 	}
 
 	public void analyze(List<Instruction> instructions) {
 
 		// Identify frame regions
 		analyzeFrames(instructions);
 
 		// Discover frame hierarchy
 		analyzeParentFrames(instructions);
 
 		// Find out register types in each frame
 		analyzeFrameRegisters(instructions);
 	}
 
 	private void analyzeFrames(List<Instruction> instructions) {
 		Deque<AnalyzedFrame> analyzedFrames = new ArrayDeque<>();
 
 		// Find out the parent of closures.
 		// Assumes every ENTER has a ASSIGN-CLOSURE referencing it.
 		for (int ip = 0; ip < instructions.size(); ip++) {
 			Instruction insn = instructions.get(ip);
 
 			if (insn.insn == Insn.ENTER_________)
 				analyzedFrames.push(new AnalyzedFrame(ip));
 
 			AnalyzedFrame frame = !analyzedFrames.isEmpty() ? analyzedFrames.peek() : null;
 			framesByIp.add(frame);
 
 			if (insn.insn == Insn.LEAVE_________)
 				analyzedFrames.pop();
 		}
 	}
 
 	private void analyzeParentFrames(List<Instruction> instructions) {
 		for (int ip = 0; ip < instructions.size(); ip++) {
 			Instruction insn = instructions.get(ip);
 
 			// Recognize frames and their parents.
 			// Assumes ENTER instruction should be after LABEL.
 			if (insn.insn == Insn.ASSIGNCLOSURE_)
 				framesByIp.get(insn.op1 + 1).parent = framesByIp.get(ip);
 		}
 	}
 
 	private void analyzeFrameRegisters(List<Instruction> instructions) {
 		int ip = 0;
 
 		while (ip < instructions.size()) {
 			int currentIp = ip;
 			Instruction insn = instructions.get(ip++);
 			int op0 = insn.op0, op1 = insn.op1, op2 = insn.op2;
 			AnalyzedFrame frame = framesByIp.get(currentIp);
 			List<AnalyzedRegister> registers = frame != null ? frame.registers : null;
 
 			switch (insn.insn) {
 			case EVALEQ________:
 			case EVALGE________:
 			case EVALGT________:
 			case EVALLE________:
 			case EVALLT________:
 			case EVALNE________:
 			case ISCONS________:
 				registers.get(op0).clazz = boolean.class;
 				break;
 			case ASSIGNCLOSURE_:
 				registers.get(op0).clazz = Closure.class;
 				break;
 			case ASSIGNINT_____:
 			case BACKUPCSP_____:
 			case BACKUPDSP_____:
 			case BINDMARK______:
 			case COMPARE_______:
 			case EVALADD_______:
 			case EVALDIV_______:
 			case EVALMOD_______:
 			case EVALMUL_______:
 			case EVALSUB_______:
 				registers.get(op0).clazz = int.class;
 				break;
 			case ASSIGNCONST___:
 			case CONSPAIR______:
 			case CONSLIST______:
 			case HEAD__________:
 			case LOG1__________:
 			case NEWNODE_______:
 			case POP___________:
 			case SETRESULT_____:
 			case SETCLOSURERES_:
 			case TAIL__________:
 			case TOP___________:
 				registers.get(op0).clazz = Node.class;
 				break;
 			case FORMTREE1_____:
 				registers.get(insn.op1).clazz = Tree.class;
 				break;
 			case ASSIGNFRAMEREG:
 				AnalyzedFrame frame1 = frame;
 				for (int i = op1; i < 0; i++) {
 					frame1.isRequireParent = true;
 					frame1 = frame1.parent;
 				}
 
 				AnalyzedRegister op0register = registers.get(op0);
 				AnalyzedRegister op2Register = frame1.registers.get(op2);
 
 				if (frame != frame1)
 					op2Register.isUsedExternally = true;
 
 				// Merge into Node if clashed
 				if (op0register.clazz != op2Register.clazz)
 					op0register.clazz = op0register.clazz != null ? Node.class : op2Register.clazz;
 				break;
 			case DECOMPOSETREE1:
 				registers.get(op1).clazz = registers.get(op2).clazz = Node.class;
 				break;
 			case ENTER_________:
 				registers = frame.registers = new ArrayList<>();
 				for (int i = 0; i < op0; i++)
 					registers.add(new AnalyzedRegister());
 				break;
 			default:
 			}
 		}
 	}
 
 	public AnalyzedFrame getFrame(Integer ip) {
 		return framesByIp.get(ip);
 	}
 
 }
