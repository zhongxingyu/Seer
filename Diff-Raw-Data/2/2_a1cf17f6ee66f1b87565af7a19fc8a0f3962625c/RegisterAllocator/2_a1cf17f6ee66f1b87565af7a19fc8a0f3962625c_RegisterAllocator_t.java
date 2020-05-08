 package compiler.back.regAloc;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 
 import compiler.ir.cfg.BasicBlock;
 import compiler.ir.cfg.CFG;
 import compiler.ir.instructions.Instruction;
 
 public class RegisterAllocator {
 
 	public List<CFG> CFGs;
 	public int stackOffset = 0;
 	List<VirtualRegister> active = new ArrayList<VirtualRegister>();
 	
 	public RegisterAllocator(List<CFG> CFGs) {
 		this.CFGs = CFGs;
 	}
 
 	public void allocateRegisters() {
 	    RealRegisterPool.resetPool();
 		buildLiveRangesSimplified();
 		linearScanRegAlloc();
 		VirtualRegisterFactory.printAllVirtualRegisters();
 	}
 
 	public int noOfReg = 8; 
 	
 	void linearScanRegAlloc() {
 		stackOffset = 0;
 		Collections.sort(VirtualRegisterFactory.virtualRegisters, increasingStartCmp);
 		for(VirtualRegister vReg : VirtualRegisterFactory.virtualRegisters) {
 			expireOldIntervals(vReg);
 			if (active.size() == RealRegisterPool.MAX_REGS) {
 				spillAtInterval(vReg);
 			} else {
 				vReg.rReg = RealRegisterPool.getFreeRegister();
 				active.add(vReg);
 				Collections.sort(active, increasingEndCmp);
 			}
 		}
 	}
 	
 	void expireOldIntervals(VirtualRegister vRegI) {
 		List<VirtualRegister> expiredIntervals = new ArrayList<VirtualRegister>();
 		for(VirtualRegister vRegJ : active) {
 			if(vRegJ.range.end >= vRegI.range.begin) {
				break;
 			}
 			expiredIntervals.add(vRegJ);
 			RealRegisterPool.freeRegister(vRegJ.rReg);
 		}
 		active.removeAll(expiredIntervals);
 	}
 	
 	void spillAtInterval(VirtualRegister vRegI) {
 
 		// spilling heuristic chooses last one in active, active is always sorted by increasing end point
 		VirtualRegister spill = active.get(active.size() - 1);
 		if (spill.range.end > vRegI.range.end) {
 			vRegI.rReg = spill.rReg;
 			spill.spillLocation = stackOffset++;
 			spill.rReg = null; // the register is used for vRegI now
 			active.remove(spill);
 			active.add(vRegI);
 			Collections.sort(active, increasingEndCmp);
 		} else {
 			vRegI.spillLocation = stackOffset++;
 		}
 	}	
 
 	public static Comparator<VirtualRegister> increasingStartCmp =  new Comparator<VirtualRegister>() {
 		public int compare(VirtualRegister o1, VirtualRegister o2) {
 			return o1.range.begin < o2.range.begin ? -1 : o1.range.begin == o2.range.begin ? 0 : 1;  
 		}};
 
 	public static Comparator<VirtualRegister> increasingEndCmp =  new Comparator<VirtualRegister>() {
 		public int compare(VirtualRegister o1, VirtualRegister o2) {
 			return o1.range.end < o2.range.end ? -1 : o1.range.end == o2.range.end ? 0 : 1;  
 		}};
 
 	
 	/**
 	 * Builds a single life time interval for each virtual register.
 	 * The SSA must be deconstructed first.
 	 */
 	public void buildLiveRangesSimplified() {
 
 		for (CFG cfg : this.CFGs) {
 			Iterator<BasicBlock> blockIterator = cfg.topDownIterator();
 
 			while (blockIterator.hasNext()) {
 				BasicBlock bb = blockIterator.next();
 				ListIterator<Instruction> instIterator = bb.getInstructionsIterator();			
 				while(instIterator.hasNext()) {
 					Instruction inst = instIterator.next();
 
 					// we only have at most one output operand
 					VirtualRegister outputOpd = inst.getOutputOperand();
 					if (outputOpd != null) {
 						outputOpd.setSingleRangeBegin(inst.getInstrNumber());
 					}
 
 					// iterate over the input operands, if any	
 					List<VirtualRegister> inputOpds = inst.getInputOperands();
 					if (inputOpds != null) {
 						for (VirtualRegister opd : inputOpds) {
 							if (opd != null) {
 								opd.setSingleRangeEnd(inst.getInstrNumber());
 							}
 						}
 					}
 				}
 			}
 		}	
 	}
 
 	
 	/**
 	 * Builds non continuous life time intervals on SSA ir.
 	 */
 	/*
 	public void buildLiveRangesOpt() {
 
 		for (CFG cfg : this.CFGs) {
 
 			// BUILDINTERVALS
 			// for each block b in reverse order do
 			Iterator<BasicBlock> blockIterator = cfg.bottomUpIterator();
 			HashSet<VirtualRegister> live = new HashSet<>();
 
 			while (blockIterator.hasNext()) {
 				BasicBlock bb = blockIterator.next();
 
 				// live = union of successor.liveIn for each successor of bb
 				live = new HashSet<VirtualRegister>();
 				for (BasicBlock succ : bb.succ){
 					live.addAll(succ.liveIn);
 				}
 
 				// for each phi function phi of successors of b do
 				// 		live.add(phi.inputOf(b))
 				for (BasicBlock succ : bb.succ) {
 					for(Phi phi : succ.getPHIs()) {
 						// live.add(phi.inputOf(b))
 						live.add(phi.getInputOperand(bb));
 					}
 				}
 
 				// for each opd in live do
 				// 		intervals[opd].addRange(b.from, b.to)
 				if(!bb.isInstructionsEmpty()) {
 					// start and exit bb's are empty
 					// we should probably have an iterator that skips them
 					for (VirtualRegister opd : live) {
 						if (bb.begin() >= 0 && bb.end() >= 0){
 							if (opd != null){
 								opd.addRange(bb.begin(), bb.end());
 							}
 						}
 					}
 				}
 
 				// for each operation op of b in reverse order do
 				// 		for each output operand opd of op do
 				// 			intervals[opd].setFrom(op.id)
 				// 			live.remove(opd)
 				// 		for each input operand opd of op do
 				// 			intervals[opd].addRange(b.from, op.id)
 				// 			live.add(opd)
 
 				ListIterator<Instruction> revInstIterator = bb.getReverseInstructionsIterator();			
 				while(revInstIterator.hasPrevious()) {
 					Instruction inst = revInstIterator.previous();
 					if (inst instanceof Phi) {
 						continue;
 					}
 
 					// we only have at most one output operand
 					VirtualRegister outputOpd = inst.getOutputOperand();
 					if (outputOpd != null) {
 						outputOpd.setRangeBegin(inst.getInstrNumber());
 						live.remove(outputOpd);
 					}
 
 					// iterate over the input operands, if any	
 					List<VirtualRegister> inputOpds = inst.getInputOperands();
 					if (inputOpds != null) {
 						for (VirtualRegister opd : inputOpds) {
 							if (opd != null){
 								opd.addRange(bb.begin(), inst.getInstrNumber());
 								if (live != null){
 									live.add(opd);
 								}
 							}
 						}
 					}
 				}
 
 				// for each phi function phi of b do 
 				// 		live.remove(phi.output)
 				for(Phi phi : bb.getPHIs()) {
 					live.remove(phi.outputOp);
 				}
 
 
 				// if b is loop header then
 				// 		loopEnd = last block of the loop starting at b
 				//		for each opd in live do
 				//			intervals[opd].addRange(b.from, loopEnd.to)			
 				if(bb.label.equals("while-cond")) {
 					// "Using the property that all blocks of a loop are contiguous 
 					// in the linear block order, it is sufficient to add one live range, 
 					// spanning the entire loop, for each register that is live at the beginning 
 					// of the loop header."
 					// TODO make sure that "that all blocks of a loop are contiguous 
 					// in the linear block order" 
 
 					// the loop header should have two predecessors,
 					// 		first one, the block before the loop
 					//		second one the last bb of the loop, hence:
 					BasicBlock loopEnd = bb.pred.get(1);
 					for (VirtualRegister opd : live) {
 						opd.addRange(bb.begin(), loopEnd.end());
 					}
 				}
 
 				// b.liveIn = live
 				bb.liveIn = live;
 			}
 		}
 	}
 	*/
 }
