 package org.instructionexecutor;
 
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.Deque;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.instructionexecutor.InstructionUtil.Closure;
 import org.instructionexecutor.InstructionUtil.Insn;
 import org.instructionexecutor.InstructionUtil.Instruction;
 import org.suite.node.Node;
 import org.suite.node.Tree;
 
 public class InstructionAnalyzer {
 
 	private List<Integer> frames = new ArrayList<>();
 	private Map<Integer, Integer> parentFramesByFrame = new HashMap<>();
 	private Map<Integer, Class<?>[]> registerTypesByFrame = new HashMap<>();
 	private Set<Integer> requireParentFrame = new HashSet<>();
 
 	public void analyze(List<Instruction> instructions) {
 
 		// Identify frame regions
 		analyzeFrames(instructions);
 
 		// Find out register types in each frame
 		analyzeFrameRegisters(instructions);
 	}
 
 	private void analyzeFrames(List<Instruction> instructions) {
 		Deque<Integer> lastEnterIps = new ArrayDeque<>();
 
 		// Find out the parent of closures.
 		// Assumes every ENTER has a ASSIGN-CLOSURE referencing it.
 		for (int ip = 0; ip < instructions.size(); ip++) {
 			Instruction insn = instructions.get(ip);
 
 			if (insn.insn == Insn.ENTER_________)
 				lastEnterIps.push(ip);
 
 			Integer frame = !lastEnterIps.isEmpty() ? lastEnterIps.peek() : null;
 			frames.add(frame);
 
 			// Recognize frames and their parents.
 			// Assumes ENTER instruction should be after LABEL.
 			if (insn.insn == Insn.ASSIGNCLOSURE_)
 				parentFramesByFrame.put(insn.op1 + 1, frame);
 
 			if (insn.insn == Insn.LEAVE_________)
 				lastEnterIps.pop();
 		}
 	}
 
 	private void analyzeFrameRegisters(List<Instruction> instructions) {
 		Class<?> registerTypes[] = null;
 		int ip = 0;
 
 		while (ip < instructions.size()) {
 			int currentIp = ip;
 			Instruction insn = instructions.get(ip++);
 			int op0 = insn.op0, op1 = insn.op1, op2 = insn.op2;
 
 			switch (insn.insn) {
 			case EVALEQ________:
 			case EVALGE________:
 			case EVALGT________:
 			case EVALLE________:
 			case EVALLT________:
 			case EVALNE________:
 			case ISTREE________:
 				registerTypes[op0] = boolean.class;
 				break;
 			case ASSIGNCLOSURE_:
 				registerTypes[op0] = Closure.class;
 				break;
 			case ASSIGNINT_____:
 			case BINDMARK______:
 			case COMPARE_______:
 			case EVALADD_______:
 			case EVALDIV_______:
 			case EVALMOD_______:
 			case EVALMUL_______:
 			case EVALSUB_______:
 				registerTypes[op0] = int.class;
 				break;
 			case ASSIGNCONST___:
 			case CONS__________:
 			case FGETC_________:
 			case HEAD__________:
 			case LOG1__________:
 			case LOG2__________:
 			case NEWNODE_______:
 			case POP___________:
 			case POPEN_________:
 			case PROVE_________:
 			case SETRESULT_____:
 			case SETCLOSURERES_:
 			case SUBST_________:
 			case TAIL__________:
 			case TOP___________:
 				registerTypes[op0] = Node.class;
 				break;
 			case FORMTREE1_____:
 				registerTypes[insn.op1] = Tree.class;
 				break;
 			case ASSIGNFRAMEREG:
 				int f = frames.get(currentIp);
 				for (int i = op1; i < 0; i++) {
 					requireParentFrame.add(f);
 					f = parentFramesByFrame.get(f);
 				}
 				Class<?> clazz1 = registerTypesByFrame.get(f)[op2];
 				if (registerTypes[op0] != clazz1) // Merge into Node if clashed
 					registerTypes[op0] = registerTypes[op0] != null ? Node.class : clazz1;
 				break;
 			case BACKUPCSPDSP__:
				registerTypes[op0] = Node.class;
 				registerTypes[op1] = int.class;
 				break;
 			case DECOMPOSETREE1:
 				registerTypes[op1] = registerTypes[op2] = Node.class;
 				break;
 			case ENTER_________:
 				registerTypesByFrame.put(frames.get(currentIp), registerTypes = new Class<?>[op0]);
 				break;
 			default:
 			}
 		}
 	}
 
 	public Integer getFrame(Integer ip) {
 		return frames.get(ip);
 	}
 
 	public Integer getParentFrame(Integer frame) {
 		return parentFramesByFrame.get(frame);
 	}
 
 	public Class<?>[] getRegisterTypes(Integer frame) {
 		return registerTypesByFrame.get(frame);
 	}
 
 	public boolean isRequireParent(Integer frame) {
 		return requireParentFrame.contains(frame);
 	}
 
 }
