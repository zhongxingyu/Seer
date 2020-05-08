 package mips;
 import java.util.List;
 import java.util.ListIterator;
 
 
 
 public class Processor {
 
 	private Instruction[] instructions = {};
 	private int pc;
 
 	private Control control;
 	private RegisterFile register;
 	private MemoryFile memory;
 	private ALU alu;
 
 	public Processor() {
 		control = new Control();
 		register = new RegisterFile();
 		memory = new MemoryFile();
 		alu = new ALU();
 
 	}
 
 	public void setInstructionSet(List<Instruction> instructions) {
 		this.instructions = new Instruction[instructions.size()];
 		ListIterator<Instruction> iterator = instructions.listIterator();
 		for(int i = 0; i < instructions.size(); i++) {
 			this.instructions[i] = iterator.next();
 		}
 	}
 
 	public void reset() {
 		pc = 0;
 		register.reset();
 		memory.reset();
 	}
 
 	public void step() {
 		Instruction i;
 		int alu_out = 0;
 		int data_out = 0;
 		int rtv = 0;
 		int rsv = 0;
 		int new_pc = pc;
 		int branch_pc;
 
 		if(isDone()) {
 			return;
 		}
 		i = instructions[pc/4];
 		control.setInstruction(i);
 
 
 		rtv = register.get(i.getRt());
 		rsv = register.get(i.getRs());
 
 
 		alu_out = alu.operation(
 				ALUControl.getControl(control.isALUOp1(), control.isALUOp0(), i.getFunct()),
 				mux(rtv, i.getAddr(), control.isALUsrc()),
 				rsv);
 
 
 		if(control.isMemRead()) {
 			data_out = memory.get(alu_out);
 		}
 
 		if(control.isMemWrite()) {
 			memory.set(alu_out, rtv);
 		}
 
 		if(control.isRegWrite()) {
 			register.set(
 					mux(i.getRt(), i.getRd(), control.isRegDist()),
 					(byte)mux(alu_out, data_out, control.isMemtoReg()));
 		}
 
 		new_pc += 4;
		branch_pc = new_pc + i.getAddr() << 2;
 
 		pc = mux(new_pc, branch_pc, control.isBranch());
 	}
 
 	private int mux(int value1, int value2, boolean getSecond) {
 		if(getSecond) {
 			return value2;
 		}
 		return value1;
 	}
 
 	public boolean isDone() {
 		return pc/4 >= instructions.length || instructions[pc/4].isExit();
 	}
 
 	/**
 	 * @return the pc
 	 */
 	public int getPc() {
 		return pc;
 	}
 
 	public int[] getRegisters() {
 		return register.getRawData();
 	}
 
 	public int[] getMemory() {
 		return memory.getRawData();
 	}
 
 	public List<Integer> getChangedRegisters() {
 		return register.getChangedIndices();
 	}
 
 	public List<Integer> getChangedMemory() {
 		return memory.getChangedIndices();
 	}
 
 
 }
