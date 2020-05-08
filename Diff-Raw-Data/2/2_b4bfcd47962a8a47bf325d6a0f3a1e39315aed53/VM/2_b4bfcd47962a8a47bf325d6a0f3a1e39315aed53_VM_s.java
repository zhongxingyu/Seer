 package vm;
 
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.TreeMap;
 
 public class VM {
 	
 	Map<Byte, Instruction> instructions = new TreeMap<Byte, Instruction>();
 	ArrayList<Process> processes = new ArrayList<Process>(256);
 	
 	private final static int defaultStackSize = 1024;
 	
 	public VM() {
 		createInstructions();
 	}
 	
 	public void iterate() {
 		for(Process process : processes) {
 			if(process.isRunning()) {
 				Instruction i = selectInstruction(process);
 				i.execute(this, process);
 			}
 		}
 	}
 	
 	public Process getProcess(int processID) {
 		if(processID < processes.size()) {
 			return processes.get(processID);
 		}
 		
 		return null;
 	}
 	
 	private void createInstructions() {
 		Instruction instruction = new Instruction("add", (byte)0x01, (byte)0);
 		
 		instructions.put(instruction.getOpcode(), instruction);
 		
 		instruction = new Instruction("sub", (byte)0x11, (byte)0);
 		instructions.put(instruction.getOpcode(), instruction);
 		
 		instruction = new Instruction("mul", (byte)0x21, (byte)0);
 		instructions.put(instruction.getOpcode(), instruction);
 		
 		instruction = new Instruction("div", (byte)0x31, (byte)0);
 		instructions.put(instruction.getOpcode(), instruction);
 
 		instruction = new Instruction("or", (byte)0x41, (byte)0);
 		instructions.put(instruction.getOpcode(), instruction);
 		
 		instruction = new Instruction("not", (byte)0x51, (byte)0);
 		instructions.put(instruction.getOpcode(), instruction);
 		
 		instruction = new Instruction("and", (byte)0x61, (byte)0);
 		instructions.put(instruction.getOpcode(), instruction);
 		
 		instruction = new Instruction("xor", (byte)0x71, (byte)0);
 		instructions.put(instruction.getOpcode(), instruction);
 		
 		
 		
 		instruction = new Instruction("load", (byte)0x02, (byte)1);
 		instructions.put(instruction.getOpcode(), instruction);
 				
 		instruction = new Instruction("loadm", (byte)0x12, (byte)4);
 		instructions.put(instruction.getOpcode(), instruction);
 
 		instruction = new Instruction("loadind", (byte)0x22, (byte)0);
 		instructions.put(instruction.getOpcode(), instruction);
 				
 		instruction = new Instruction("loadindm", (byte)0x32, (byte)0);
 		instructions.put(instruction.getOpcode(), instruction);
 		
 		instruction = new Instruction("loadf", (byte)0x42, (byte)0);
 		instructions.put(instruction.getOpcode(), instruction);
 
 		
 		instruction = new Instruction("stores", (byte)0x03, (byte)1);
 		instructions.put(instruction.getOpcode(), instruction);
 		
 		instruction = new Instruction("storem", (byte)0x13, (byte)4);
 		instructions.put(instruction.getOpcode(), instruction);
 		
 		instruction = new Instruction("storeinds", (byte)0x23, (byte)0);
 		instructions.put(instruction.getOpcode(), instruction);
 		
 		instruction = new Instruction("storeindm", (byte)0x33, (byte)0);
 		instructions.put(instruction.getOpcode(), instruction);
 		
 		instruction = new Instruction("storef", (byte)0x43, (byte)0);
 		instructions.put(instruction.getOpcode(), instruction);
 		
 		
 		
 		instruction = new Instruction("cmp", (byte)0x04, (byte)0);
 		instructions.put(instruction.getOpcode(), instruction);
 		
 		
 		
 		instruction = new Instruction("jmp", (byte)0x15, (byte)0);
 		instructions.put(instruction.getOpcode(), instruction);
 		
 		instruction = new Instruction("jmpz", (byte)0x35, (byte)0);
 		instructions.put(instruction.getOpcode(), instruction);
 		
		instruction = new Instruction("jmpnz", (byte)0xD5, (byte)0);
 		instructions.put(instruction.getOpcode(), instruction);
 		
 		instruction = new Instruction("jmpgt", (byte)0x45, (byte)0);
 		instructions.put(instruction.getOpcode(), instruction);
 		
 		instruction = new Instruction("jmpl", (byte)0xC5, (byte)0);
 		instructions.put(instruction.getOpcode(), instruction);
 		
 		instruction = new Instruction("jmpgte", (byte)0x65, (byte)0);
 		instructions.put(instruction.getOpcode(), instruction);
 		
 		instruction = new Instruction("jmpleq", (byte)0x25, (byte)0);
 		instructions.put(instruction.getOpcode(), instruction);
 		
 		instruction = new Instruction("jmpnleq", (byte)0xA5, (byte)0);
 		instructions.put(instruction.getOpcode(), instruction);
 		
 		
 		
 		instruction = new Instruction("send", (byte)0x06, (byte)0);
 		instructions.put(instruction.getOpcode(), instruction);
 		
 		instruction = new Instruction("halt", (byte)0x16, (byte)0);
 		instructions.put(instruction.getOpcode(), instruction);
 		
 		
 		
 		instruction = new Instruction("memsize", (byte)0x07, (byte)0);
 		instructions.put(instruction.getOpcode(), instruction);
 		
 		instruction = new Instruction("memset", (byte)0x17, (byte)0);
 		instructions.put(instruction.getOpcode(), instruction);
 
 		instruction = new Instruction("stacksize", (byte)0x27, (byte)0);
 		instructions.put(instruction.getOpcode(), instruction);
 
 		instruction = new Instruction("stackset", (byte)0x37, (byte)0);
 		instructions.put(instruction.getOpcode(), instruction);
 	}
 
 	private Instruction selectInstruction(Process process) {
 		
 		int pc = process.getPc();
 
 		byte opcode = process.getMemory()[pc++];
 		process.setPc(pc);
 		
 		return instructions.get(opcode);
 	}
 	
 	public int createProcess(byte[] memoryImage) {
 		Process process = new Process(memoryImage.length, defaultStackSize);
 		
 		System.arraycopy(memoryImage, 0, process.getMemory(), 0, memoryImage.length);
 		
 		processes.add(process);
 		
 		return processes.size() - 1;
 	}
 }
