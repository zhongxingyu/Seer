 package vm;
 
 public class Instruction {
 	
 	private String name;
 	private byte opcode;
 	private byte parameterLength;
 	private byte stackBytes;
 	
 	public Instruction(String name, byte opcode, byte parameterLength) {
 		this.name = name;
 		this.opcode = opcode;
 		this.parameterLength = parameterLength;
 	}
 	
 	public byte getOpcode() {
 		return opcode;
 	}
 	
 	public void setOpcode(byte opcode) {
 		this.opcode = opcode;
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public byte getParameterLength() {
 		return parameterLength;
 	}
 
 	public void setParameterLength(byte parameterLength) {
 		this.parameterLength = parameterLength;
 	}
 		
 	private void add(Process process) {
 		int result;
 		
 		int sp = process.getSp();
 		byte flags = process.getFlags();
 		byte[] stack = process.getStack();
 		
 		byte p1 = stack[sp--];
 		byte p2 = stack[sp--];
 		
 		result = p1 + p2;
 		if(result > Byte.MAX_VALUE) {
 			flags |= (byte)0x01;
 		}
 		
 		stack[++sp] = (byte)result;
 		
 		process.setFlags(flags);
 		process.setSp(sp);
 	}
 	
 	private void sub(Process process) {
 		int result;
 		
 		int sp = process.getSp();
 		byte flags = process.getFlags();
 		byte[] stack = process.getStack();
 		
 		byte p1 = stack[sp--];
 		byte p2 = stack[sp--];
 		
 		result = p1 - p2;
 		if(result < 0) {
 			flags &= (byte)0x01;
 		}
 		
 		stack[++sp] = (byte)result;
 		
 		process.setFlags(flags);
 		process.setSp(sp);
 	}
 	
 	private void mul(Process process) {
 		int result;
 		
 		int sp = process.getSp();
 		byte flags = process.getFlags();
 		byte[] stack = process.getStack();
 		
 		byte p1 = stack[sp--];
 		byte p2 = stack[sp--];
 		
 		result = p1 * p2;
 		if(result > 0) {
 			flags &= (byte)0x01;
 		}
 		
 		stack[++sp] = (byte)result;
 		
 		process.setFlags(flags);
 		process.setSp(sp);
 	}
 	
 	private void div(Process process) {
 		int result;
 		
 		int sp = process.getSp();
 		byte[] stack = process.getStack();
 		
 		byte p1 = stack[sp--];
 		byte p2 = stack[sp--];
 		
 		result = p1 / p2;
 		
 		stack[++sp] = (byte)result;
 		
 		process.setSp(sp);
 	}
 	
 	private void or(Process process) {
 		int result;
 		
 		int sp = process.getSp();
 		byte[] stack = process.getStack();
 		
 		byte p1 = stack[sp--];
 		byte p2 = stack[sp--];
 		
 		result = p1 | p2;
 		
 		stack[++sp] = (byte)result;
 		
 		process.setSp(sp);
 	}
 	
 	private void not(Process process) {
 		int result;
 		
 		int sp = process.getSp();
 		byte[] stack = process.getStack();
 		
 		byte p1 = stack[sp--];
 		
 		result = (byte)(~ p1);
 		
 		stack[++sp] = (byte)result;
 		
 		process.setSp(sp);
 	}
 	
 	private void and(Process process) {
 		int result;
 		
 		int sp = process.getSp();
 		byte[] stack = process.getStack();
 		
 		byte p1 = stack[sp--];
 		byte p2 = stack[sp--];
 		
 		result = p1 & p2;
 		
 		stack[++sp] = (byte)result;
 		
 		process.setSp(sp);
 	}
 	
 	private void xor(Process process) {
 		int result;
 		
 		int sp = process.getSp();
 		byte[] stack = process.getStack();
 		
 		byte p1 = stack[sp--];
 		byte p2 = stack[sp--];
 		
 		result = p1 ^ p2;
 		
 		stack[++sp] = (byte)result;
 		
 		process.setSp(sp);
 	}
 	
 	private void alu(Process process) {
 		switch(getOpcode() & 0xF0) {
 		case 0x00:
 			add(process);
 			return;
 		case 0x01:
 			sub(process);
 			return;
 		case 0x02:
 			mul(process);
 			return;
 		case 0x03:
 			div(process);
 			return;
 		case 0x04:
 			or(process);
 			return;
 		case 0x05:
 			not(process);
 			return;
 		case 0x06:
 			and(process);
 			return;
 		case 0x07:
 			xor(process);
 			return;
 		}
 	}
 	
 	private void load(Process process) {
 		
 		int pc = process.getPc();
 		int sp = process.getSp();
 		byte[] memory = process.getMemory();
 		byte[] stack = process.getStack();
 		
 		int memPtr = 0;
 		byte value = 0;
 		
 		if((getOpcode() & 0x20) != 0) { //indirect
 			if((getOpcode() & 0x10) != 0) { //memory address
 				memPtr =  ((stack[sp--] << 24) + (stack[sp--] << 16) + (stack[sp--] << 8)  + stack[sp]);
 				value = memory[memPtr];
 			} else { //relative stack address
 				value = stack[stack[sp--]];
 			}
 			
 			stack[++sp] = value;
 			
 		} else if((getOpcode() &0x40) != 0) { //flags
 			value = process.getFlags();
 			stack[++sp] = value;
 		} else { 
 			if((getOpcode() & 0x10) != 0) {
 				memPtr =  (memory[pc++] + (memory[pc++] << 8) + (memory[pc++] << 16)  + (memory[pc++] << 24));
 				value = memory[memPtr];
 			} else {
 				value = memory[pc++];
 			}
 			
 			stack[++sp] = value;
 		}
 		
 		process.setSp(sp);
 		process.setPc(pc);
 	}
 	
 	private void store(Process process) {
 		
 		int pc = process.getPc();
 		int sp = process.getSp();
 		byte[] memory = process.getMemory();
 		byte[] stack = process.getStack();
 		
 		int memPtr = 0;
 		int stackPtr = 0;
 		byte value = 0;
 		
 		if((getOpcode() & 0x10) != 0) { //target = memory
 			if((getOpcode() & 0x20) != 0) { //indirect 
 				memPtr =  ((stack[sp--] << 24) + (stack[sp--] << 16) + (stack[sp--] << 8)  + stack[sp]);
 				value = stack[sp--];
 			} else { //direct
 				memPtr =  (memory[pc++] + (memory[pc++] << 8) + (memory[pc++] << 16)  + (memory[pc++] << 24));
 				value = stack[sp--];
 			}
 			
 			memory[memPtr] = value;
 		} else if ((getOpcode() & 0x40) != 0) { //target = flags
 			value = stack[sp--];
 			process.setFlags(value);
 		} else { //target = stack
 		
 			if((getOpcode() & 0x20) != 0) { //indirect 
 				stackPtr =  stack[sp--];
 				value = stack[sp--];
 			} else { //direct
 				stackPtr = memory[pc++]; 
 				value = stack[sp--];
 			}
 			
 			stack[stackPtr] = value;
 		}
 		
 		process.setSp(sp);
 		process.setPc(pc);
 	}
 	
 	private void cmp(Process process) {
 		int sp = process.getSp();
 		byte flags = process.getFlags();
 		byte[] stack = process.getStack();
 		
 		byte a = stack[sp--];
 		byte b = stack[sp--];
 		
 		if(a == b) {
 			flags |= 0x1;
 		} else {
 			flags &= 0xE; //clear 0x1
 		}
 		
 		if(a > b) {
 			flags |= 0x2;
 		} else {
 			flags &= 0xD; //clear 0x2
 		}
 		
 		process.setFlags(flags);
 	}
 	
 	private void nop(Process process) {
 	}
 	
 	private void jmp(Process process) {
 
 		int sp = process.getSp();
 		byte flags = process.getFlags();
 		byte[] stack = process.getStack();
 		
 		int newPC =  ((stack[sp--] << 24) + (stack[sp--] << 16) + (stack[sp--] << 8)  + stack[sp]);
 		boolean shouldJump = false;
 		
 
 		if((getOpcode() & 0xF) == 0x1) { // unconditional jump
 			shouldJump = true;
 		} else {
 			if((getOpcode() & 0x1) != 0) {	// conditional jump - equal only
 				if((getOpcode() & 0x2) != 0 && (flags & 0x1) != 0) {
 					shouldJump = true;
 				}
 			} else {	// conditional jump - combined equal and overflow flag
 				if((getOpcode() & 0x4) != 0 && (flags & 0x2) != 0) { // greater
 					shouldJump = true;
 				}
 				
 				if((getOpcode() & 0x4) == 0 && (flags & 0x2) == 0) { // lesser
 					shouldJump = true;
 				}
 				
 				if((getOpcode() & 0x2) != 0 && (flags & 0x1) != 0) { // equal
 					shouldJump = true;
 				}
 			}
 		}
 		
 		if((getOpcode() & 0x8) != 0) { // not
 			shouldJump = !shouldJump;
 		}
 
 		if(shouldJump) {
 			process.setPc(newPC);
 		}
 	}
 	
 	private void rpc(VM vm, Process process) {
 		
		if((getOpcode() & 0x1) != 0) {
 			process.setRunning(false);
 			return;
 		}
 			
 		int sp = process.getSp();
 		int pc = process.getPc();
 		byte[] stack = process.getStack();
 		
 		int processID = stack[sp--];
 		
 		Process otherProcess = vm.getProcess(processID);
 		if(otherProcess != null) {
 			int osp = otherProcess.getSp();
 			int opc = otherProcess.getPc();
 			byte[] oStack = otherProcess.getStack();
 			byte[] memory = otherProcess.getMemory();
 			
 			oStack[sp++] = (byte)(opc & 0xF);
 			oStack[sp++] = (byte)(opc >> 8 & 0xF);
 			oStack[sp++] = (byte)(opc >> 16 & 0xF);
 			oStack[sp++] = (byte)(opc >> 24 & 0xF);
 			oStack[sp++] = memory[pc++];
 			
 			otherProcess.setSp(osp);
 			
 			int newPc =  (memory[0] + (memory[1] << 8) + (memory[2] << 16)  + (memory[3] << 24));
 			otherProcess.setPc(newPc);
 		}
 	}
 	
 	private void mem(Process process) {
 		
 		if((getOpcode() & 0x10) == 0) { //get size
 			int size = 0;
 			
 			if((getOpcode() & 0x2) == 1) { //stack
 				size = process.getStackSize();
 			} else {
 				size = process.getMemSize();
 			}
 			
 			byte[] stack = process.getStack();
 			int sp = process.getSp();
 			
 			stack[sp++] = (byte)(size & 0xF);
 			stack[sp++] = (byte)(size >> 8 & 0xF);
 			stack[sp++] = (byte)(size >> 16 & 0xF);
 			stack[sp++] = (byte)(size >> 24 & 0xF);
 			
 			process.setSp(sp);
 		}
 
 		if((getOpcode() & 0x10) != 0) { //set size
 			
 			int size = 0;
 			
 			byte[] stack = process.getStack();
 			int sp = process.getSp();
 			
 			size = ((stack[sp--] << 24) + (stack[sp--] << 16) + (stack[sp--] << 8) + stack[sp--]);
 			
 			process.setSp(sp);
 			
 			if((getOpcode() & 0x2) == 1) { //stack
 				process.setStackSize(size);
 			} else {
 				process.setMemSize(size);
 			}
 		}
 	}
 	
 	public void execute(VM vm, Process process) {
 		switch(getOpcode() & 0x0F) {
 		case 0x00:
 			nop(process);
 			return;
 		case 0x01:
 			alu(process);
 			return;
 		case 0x02:
 			load(process);
 			return;
 		case 0x03:
 			store(process);
 			return;
 		case 0x04:
 			cmp(process);
 			return;
 		case 0x05:
 			jmp(process);
 			return;
 		case 0x06:
 			rpc(vm, process);
 			return;
 		case 0x07:
 			mem(process);
 			return;
 		}
 	}
 
 	public byte getStackBytes() {
 		return stackBytes;
 	}
 
 	public void setStackBytes(byte stackBytes) {
 		this.stackBytes = stackBytes;
 	}
 }
