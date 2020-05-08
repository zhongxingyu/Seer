 package ch.zhaw.inf3.emulator;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 @SuppressWarnings("unused")
 public class MiniPPC_CPU {
 	private int cycle_count = 0;
 	private short register[] = new short[4];
 	private short RAM[];
 	private short IP; // Next Instruction Pointer
 	private short IR; // Instruction Register
 	private int carry;
 	private InstructionSetArchitecture isa;
 	private InsImplAnnotationParser annotaion_parser;
 
 	public boolean isHalted = false;
 
 	public MiniPPC_CPU(int memory){
 		this.RAM = new short[memory];
 		this.isa = new InstructionSetArchitecture();
 		this.IP = isa.start_offset;
 		
 		annotaion_parser = new InsImplAnnotationParser();
 		annotaion_parser.parse(MiniPPC_CPU.class);
 	}
 	
 	public void runCycle(){
 		// Instruction Fetch
 		IR = RAM[IP];
 		
 		// Instruction Decode
 		Instruction op = isa.decodeWord(IR);
 		
 		// Increment IP
 		IP = (short) (IP + 1);
 		
 		// Execute
 		executeInstruction(op);
 		
 		cycle_count = cycle_count + 1;
 	}
 	
 	public void reset(){
 		IP = isa.start_offset;
 		isHalted = false;
		carry = 0;
 	}
 
 	public void loadCode(short[] words){
 		for (int i = 0; i < words.length; i++) {
 			RAM[100+i] = words[i];
 		}
 	}
 	
 	public void loadDataAtOffset(short[] words, int offset){
 		for (int i = 0; i < words.length; i++) {
 			RAM[offset+i] = words[i];
 		}
 	}
 	
 	public void printRegisters(){
 		for (int i = 0; i < register.length; i++) {
 			System.out.println("R"+i+": "+ register[i]);
 		}
 		System.out.println("IR: "+ IR );
		System.out.println(String.format("IP: %3s  carry: %s  cycles: %6s", IP, carry, cycle_count) );
 	}
 	
 	public void printRam(){
		System.out.print("RAM[500:]:");
 		for (int i = 500; i < RAM.length; i++) {
			System.out.print(String.format(" 0x%04x",RAM[i]));
 		}
 		System.out.println();
 	}
 
 	private void executeInstruction(Instruction ins) {
 		if (ins != null){
 			System.out.print("execute " + ins.mnemonic);
 			for (int i = 0; i < ins.num_operands; i++) {
 				System.out.print(" " +ins.operands[i]);
 			}
 			System.out.println();
 			
 			String method_name = annotaion_parser.instruction_map.get(ins.mnemonic);
 			
 			if (method_name != null)
 				try {
 					Method m = this.getClass().getDeclaredMethod(method_name, Instruction.class);
 					m.invoke(this, ins);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			else {
 				System.err.println("Instruction not implemented: " + ins.mnemonic);
 			}
 		}
 	}
 	
 	
 	// Instruction Implementations
 	
 	@InstructionImpl("CLR")
 	private void clr(Instruction ins){
 		register[ins.operands[0]] = 0;
 		carry = 0;
 	}
 	
 	@InstructionImpl("ADDD")
 	private void addd(Instruction ins){
 		register[0] += ins.operands[0];
 		// check for overflow, set carry
 	}
 	
 	@InstructionImpl("ADD")
 	private void add(Instruction ins){
 		short sh = register[ins.operands[0]];
 		int result = register[0] + sh; // cheap&dirty solution ;-)
 		register[0] += register[ins.operands[0]];
 		if (result != register[0]) carry = 1;
 		else carry = 0;
 	}
 
 	@InstructionImpl("INC")
 	private void inc(Instruction ins) {
 		short sh = register[0];
 		register[0] += 1;
 		if (sh > register[0]) carry = 1;
 		else carry = 0;
 	}
 
 	@InstructionImpl("DEC")
 	private void dec(Instruction ins) {
 		short sh = register[0];
 		register[0] -= 1;
 		if (sh < register[0]) carry = 1;
 		else carry = 0;
 	}
 
 	@InstructionImpl("LWDD")
 	private void loadWordDirect(Instruction ins){
 		//System.out.println("invoked lwdd with op1:" + ins.operands[0] + " op2:" + ins.operands[1]);
 		register[ins.operands[0] ] = RAM[ins.operands[1]];
 	}
 
 	@InstructionImpl("SWDD")
 	private void storeWordDirect(Instruction ins){
 		RAM[ins.operands[1]] = register[ins.operands[0]];
 	}
 	
 	@InstructionImpl("SRA")
 	private void sra(Instruction ins){
 		carry = (register[0] & 1);
 		register[0] >>>= 1;
 	}
 	
 	@InstructionImpl("SLA")
 	private void shiftLeftArithmetic(Instruction ins){
 		carry = (register[0] >> 15);
 		int sign = register[0] & 2^15;
 		register[0] = (short)(register[0] << 1);
 		register[0] |= sign;
 	}
 
 	@InstructionImpl("SRL")
 	private void srl(Instruction ins){
 		carry = (register[0] & 1);
 		register[0] >>= 1;
 	}
 	
 	@InstructionImpl("SLL")
 	private void shiftLeftLogical(Instruction ins){
 		carry = (register[0] >> 15);
 		register[0] = (short)(register[0] << 1);
 	}
 
 	@InstructionImpl("AND")
 	private void and(Instruction ins){
 		register[0] &= register[ins.operands[0]];
 	}
 	@InstructionImpl("OR")
 	private void or(Instruction ins){
 		register[0] |= register[ins.operands[0]];
 	}
 	
 	@InstructionImpl("NOT")
 	private void not(Instruction ins){
 		//register[0] = (short)~register[0];
 		register[0] = (short)(register[0] ^ 0xffff);
 	}
 	
 	@InstructionImpl("BZ")
 	private void branchZero(Instruction ins){
 		if(register[0] == 0){
 			IP = register[ins.operands[0]];
 		}
 	}
 	
 	@InstructionImpl("BNZ")
 	private void branchNotZero(Instruction ins){
 		if(register[0] != 0){
 			IP = register[ins.operands[0]];
 		}
 	}
 
 	@InstructionImpl("BC")
 	private void branchCarry(Instruction ins){
 		if(carry == 1){
 			IP = register[ins.operands[0]];
 		}
 	}
 
 	@InstructionImpl("B")
 	private void branch(Instruction ins){
 		IP = register[ins.operands[0]];
 	}
 
 	@InstructionImpl("BZD")
 	private void branchZeroDirect(Instruction ins){
 		if(register[0] == 1){
 			IP = ins.operands[0];
 		}
 	}	
 	
 	@InstructionImpl("BNZD")
 	private void branchNotZeroDirect(Instruction ins){
 		if(register[0] != 0){
 			IP = ins.operands[0];
 		}
 	}
 
 	@InstructionImpl("BCD")
 	private void branchCarryDirect(Instruction ins){
 		if(carry == 1){
 			IP = ins.operands[0];		}
 	}
 
 	@InstructionImpl("BD")
 	private void branchDirect(Instruction ins){
 		IP = ins.operands[0];
 	}
 	
 	@InstructionImpl("END")
 	private void end(Instruction ins) {
 		isHalted = true;
 	}
 
 }
