 package compiler.front;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 
 import compiler.back.regAloc.VirtualRegister;
 import compiler.back.regAloc.VirtualRegisterFactory;
 import compiler.ir.cfg.BasicBlock;
 import compiler.ir.cfg.CFG;
 import compiler.ir.instructions.ArithmeticBinary;
 import compiler.ir.instructions.ArithmeticUnary;
 import compiler.ir.instructions.Call;
 import compiler.ir.instructions.Index;
 import compiler.ir.instructions.Instruction;
 import compiler.ir.instructions.Move;
 import compiler.ir.instructions.Phi;
 import compiler.ir.instructions.Return;
 import compiler.ir.instructions.Scalar;
 import compiler.ir.instructions.StoreValue;
 
 public class SSAGenerator {
 
 	public List<CFG> CFGs;
 	public int phiCount = 0;
 
 	public SSAGenerator(List<CFG> CFGs) {
 		this.CFGs = CFGs;
 	}
 
 	/**
 	 * Generates SSA by mutating the input CFGs.
 	 */
 	public void generateSSA() {
 		for (CFG cfg : this.CFGs) {
 
 			// skip main since is does not have any local variables
 			if(cfg.label.equals("main")) {
 				continue;
 			}
 
 			// Iterate over BBs and initialize entry and exit states
 			// TODO update the iteration from linear to reverse post order
 			Iterator<BasicBlock> blockIterator = cfg.topDownIterator();
 			while (blockIterator.hasNext()) {
 				BasicBlock bb = blockIterator.next();
 				// create empty entry state (instruction place holders)
 				bb.entryState = createStateVector(cfg.frame);
 				// copy the entry state slots inside exit state slots
 				// this is required to maintain the link between entry and exit state
 				// for the slots that are not mutated in current basic block
 				bb.exitState.addAll(bb.entryState); 
 				constructSSA(bb);
 			}
 
 			// link entry and exit states
 			blockIterator = cfg.topDownIterator();
 			while (blockIterator.hasNext()) {
 				BasicBlock bb = blockIterator.next();
 				int slot = 0;
 
 				for (Instruction val : bb.entryState) {
 					// for each phi value remember the block it came from
 					Hashtable<BasicBlock, Instruction> values = new Hashtable<BasicBlock, Instruction>(); 
 					for (BasicBlock pred : bb.pred) {
 						values.put(pred, pred.exitState.get(slot));
 					}
 					if (values.size() == 1) { // only one predecessor
 						Instruction.forward(val, values.get(bb.pred.get(0)));
 					} else if (values.size() > 1) { // multiple predecessors
 						Phi phi = new Phi(values);
 						//						phi.setInstrNumber(parser.instructionCnt++); //TODO
 						phi.setInstrNumber(phiCount++);
 						Instruction.forward(val, phi);
 						bb.prependInstruction(phi);
 					}						
 					slot++;
 				}
 			}
 
 			// simplify PHIs
 			blockIterator = cfg.topDownIterator();
 			while (blockIterator.hasNext()) {
 				BasicBlock bb = blockIterator.next();
 				List<Instruction> removablePHIs = new ArrayList<Instruction>();
 				for (Instruction inst : bb.getInstructions()) {
 					if (inst instanceof Phi) {
 						Phi phi = (Phi) inst;
 						Instruction value = testPhi(phi);
 						if (value != null) {
 							// this PHI is redundant and 
 							// value holds the instruction it should be forwarded to
 							Instruction.forward(phi, value);
 							removablePHIs.add(phi);
 							VirtualRegisterFactory.removeRegister(phi.outputOp);
 						}
 					}
 				}
 				bb.removeInstructions(removablePHIs);					
 			}
 		}
 	}
 
 
 	private List<Instruction> createStateVector(List<Instruction> frame) {
 		List<Instruction> state = new ArrayList<Instruction>();
 		for (int i = 0; i < frame.size(); i++) {
 			state.add(new Instruction(-1));
 		}
 		return state;
 	}
 
 	private void constructSSA(BasicBlock bb) {
 		// TODO the abstract interpretation should be extended to all
 		// the other IR operations that either read or write scalars
 
 		// perform abstract interpretation
 		if (!bb.isInstructionsEmpty()) {
 			List<Instruction> moves = new ArrayList<Instruction>();
 			for (Instruction instr : bb.getInstructions()) {
 				if (instr instanceof Move) {
 					moves.add(instr);
 					// mutate state vector, replace dest with src in the state vector
 
 					// src can be a scalar, belonging to the frame, or the result of another instruction
 					// we use resolve() to get the right value for it
 					Instruction src = resolve(((Move) instr).src, bb);
 
 					// dest is always a scalar
 					Scalar dest = (Scalar)((Move) instr).dest; // a frame slot
 					bb.exitState.set(dest.symbol.slot, src);
 				} else if (instr instanceof ArithmeticBinary) {
 					// read from local variables
 					((ArithmeticBinary) instr).right = resolve(((ArithmeticBinary) instr).right, bb);
 					((ArithmeticBinary) instr).left = resolve(((ArithmeticBinary) instr).left, bb);
 				} else if (instr instanceof ArithmeticUnary) {
 					((ArithmeticUnary) instr).operand = resolve(((ArithmeticUnary) instr).operand, bb);
 				} else if (instr instanceof Return) {
					((Return) instr).returnValue = resolve(((Return) instr).returnValue, bb);
 				} else if (instr instanceof Call) {
 					List<Instruction> ssaArgs = new ArrayList<Instruction>();
 					for(Instruction arg : ((Call)instr).args) {
 						ssaArgs.add(resolve(arg, bb));
 					}
 					((Call)instr).args = ssaArgs;
 				} else if (instr instanceof StoreValue) {
 					((StoreValue) instr).value = resolve(((StoreValue) instr).value, bb);
 				} else if (instr instanceof Index) {
 					((Index) instr).offset = resolve(((Index) instr).offset, bb);
 				}
 
 
 			}
 			// eliminate move instructions
 			bb.removeInstructions(moves);
 		}
 	}
 
 	// resolve the value of a read
 	private Instruction resolve(Instruction inst, BasicBlock bb) {
 		if (inst instanceof Scalar) {
 			// if the instruction is a scalar then this is a read and
 			// we need to actually get it's value from the state vector
 			return bb.exitState.get(((Scalar)inst).symbol.slot);
 		}
 		// a value generating instruction (e.g. IMM)
 		return inst;
 	}
 
 
 	/**
 	 * Determine if PHI instruction is redundant.
 	 * Functions of the form v2 = PHI(v1, v1, ..., v1) 
 	 * 		are forwarded to their common operand (v2 → v1). 
 	 * Functions of the form v2 = PHI(v1, v2, ..., v2) 
 	 * 		are forwarded to their incoming operand (v2 → v1). 
 	 * 
 	 * @param phi
 	 * @return the instruction the PHI should be forwarded to if redundant, null otherwise
 	 */
 	private Instruction testPhi(Phi phi) {
 
 		// PHI's have at least two operands
 		assert phi.getValues().size() >= 2;
 
 		Instruction firstVal = null;
 
 		// pick first value not equal to the PHI instruction itself
 		for(int i = 0; i < phi.getValues().size(); i++) {
 			firstVal = Instruction.resolve(phi.getValues().get(i));
 			if (!firstVal.equals(phi)) {
 				break;
 			}
 		}
 
 		// all values should never be equal to PHI
 		if (firstVal.equals(phi)) {
 			System.err.println("All PHI values equals to PHI itself!");
 		}
 
 		// test 
 		for(int i = 0; i < phi.getValues().size(); i++) {
 			Instruction value = Instruction.resolve(phi.getValues().get(i));
 			if(!value.equals(firstVal) && !value.equals(phi)) {
 				return null; // this PHI is not redundant
 			}
 		}
 		return firstVal; // this PHI is redundant, return the instruction it should be forwarded to
 	}
 
 
 
 
 	public void deconstructSSA() {
 
 		for (CFG cfg : this.CFGs) {
 
 			// skip main since is does not have any local variables
 			if(cfg.label.equals("main")) {
 				continue;
 			}
 
 			Iterator<BasicBlock> blockIterator = cfg.topDownIterator();
 			while (blockIterator.hasNext()) {
 				BasicBlock bb = blockIterator.next();
 				for(BasicBlock pred : bb.pred) {
 					List<Instruction> moves = new ArrayList<Instruction>();
 					for(Phi phi : bb.getPHIs()){
 						Move move = new Move(phi.getInputValue(pred), Instruction.resolve(phi));
 						moves.add(move);
 						//TODO topoSortMoves(moves);
 					}
 					pred.appendInstructions(moves);				
 				}
 				bb.removePHIs(bb.getPHIs());
 			}
 		}
 
 //		renumberInstructions();
 	}
 
 	private void topoSortMoves(List<Instruction> moves) {
 
 	}
 
 	static class Graph {
 		HashMap<VirtualRegister, Edges> graph;
 
 		public Graph() {
 			graph = new HashMap<VirtualRegister, SSAGenerator.Edges>();
 		}
 
 		public void addEdge(VirtualRegister src, VirtualRegister dest) {
 			if (graph.get(src) == null) {
 				graph.put(src, new Edges());
 			}
 			if (graph.get(dest) == null) {
 				graph.put(dest, new Edges());
 			}
 			graph.get(src).addOutgoingEdge(dest);
 			graph.get(dest).addIncomingEdge(src);
 		}
 
 
 		public void topoSort() {
 			ArrayList<VirtualRegister> L = new ArrayList<VirtualRegister>();
 			HashSet<VirtualRegister> S = new HashSet<VirtualRegister>();
 			for (VirtualRegister n : graph.keySet()) {
 				if(graph.get(n).inEdges.size() == 0) {
 					S.add(n);
 				}
 			}
 
 			//while S is non-empty do
 			while(!S.isEmpty()) {
 				//remove a node n from S
 				VirtualRegister n = S.iterator().next();
 				S.remove(n);
 				
 				//insert n into L
 			      L.add(n);
 
 				//for each node m with an edge e from n to m do
 				for(Iterator<VirtualRegister> it = graph.get(n).outEdges.iterator(); it.hasNext(); ) {
 					VirtualRegister m = it.next();
 					graph.get(m).inEdges.remove(n);
 					if(graph.get(m).inEdges.isEmpty()) {
 						S.add(m);
 					}
 
 				}
 			}
 			
 			boolean cycle = false;
 			for (VirtualRegister n : graph.keySet()) {
 				if(!graph.get(n).inEdges.isEmpty()) {
 					System.out.println(graph.get(n).inEdges + " -> " + n);
 					cycle = true;
 //					break;
 				}
 			}
 
 			if(cycle) {
 				System.out.println("Cycle present, topological sort not possible");
 			} else {
 				System.out.println("Topological Sort: " + Arrays.toString(L.toArray()));
 			}
 		}
 
 	}
 
 	static class Edges {
 		List<VirtualRegister> inEdges; // in edges
 		List<VirtualRegister> outEdges; // out edges
 
 		public Edges() {
 			inEdges = new ArrayList<VirtualRegister>();
 			outEdges = new ArrayList<VirtualRegister>();
 		}
 
 		public void addIncomingEdge(VirtualRegister in) {
 			inEdges.add(in);
 		}
 
 		public void addOutgoingEdge(VirtualRegister out) {
 			outEdges.add(out);
 		}
 	}
 	
 	public static void main(String[] args) {
 		
 		Graph graph = new Graph();
 		VirtualRegister vr1 = new VirtualRegister(1);
 		VirtualRegister vr2 = new VirtualRegister(2);
 		VirtualRegister vr3 = new VirtualRegister(3);
 		VirtualRegister vr4 = new VirtualRegister(4);
 		VirtualRegister vr5 = new VirtualRegister(5);
 		
 		graph.addEdge(vr1, vr2);
 		graph.addEdge(vr2, vr5);
 		graph.addEdge(vr2, vr4);
 		graph.addEdge(vr4, vr1);
 		graph.addEdge(vr4, vr3);
 		
 		graph.topoSort();
 
 		
 		
 	}
 
 }
