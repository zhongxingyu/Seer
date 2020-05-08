 package net.ian.dcpu;
 
 public class DCPU {
 	public Cell[] register;
 	public Cell[] memory;
 	public Cell SP, PC, O;
 	public boolean running;
 	public int instructionCount = 0;
 	
 	public enum Register { A, B, C, X, Y, Z, I, J };
 	
 	public DCPU() {
 		this(new int[0]);
 	}
 
 	public DCPU(int[] mem) {
 		register = new Cell[11];
 		for (int i = 0; i < 11; i++)
 			register[i] = new Cell(0);
 		memory = new Cell[0x10000]; // 0x10000 words in size
 		for (int i = 0; i < 0x10000; i++)
 			memory[i] = new Cell(i < mem.length ? mem[i] : 0);
 
 		SP = new Cell(0xffff);
 		PC = new Cell(0);
 		O = new Cell(0);
 	}
 	
 	private void debug(Object o) {
 		System.out.println(o);
 	}
 
 	public Cell getRegister(Register r) {
 		return register[r.ordinal()];
 	}
 	
 	public void setRegister(Register r, int value) {
 		register[r.ordinal()].value = value;
 	}
 	
 	private Cell handleArgument(int code) {
 		System.out.printf("0x%s: ", Integer.toHexString(code));
 		if (code >= 0x0 && code <= 0x7) {
 			debug(Register.values()[code]);
 			return register[code];
 		} else if (code >= 0x8 && code <= 0xf) {
 			System.out.printf("[%s]\n", Register.values()[code - 0x8]);
 			return memory[register[code - 0x8].value];
 		} else if (code >= 0x10 && code <= 0x17) {
 			System.out.printf("[next word + %s]\n", Register.values()[code - 0x10]);
 			return memory[memory[++PC.value].value + register[code - 0x10].value];
 		} else if (code == 0x18) {
 			debug("POP");
 			return memory[SP.value++];
 		} else if (code == 0x19) {
 			debug("PEEK");
 			return memory[SP.value];
 		} else if (code == 0x1a) {
 			debug("PUSH");
 			return memory[--SP.value];
 		} else if (code == 0x1b) {
 			debug("SP");
 			return SP;
 		} else if (code == 0x1c) {
 			debug("PC");
 			return PC;
 		} else if (code == 0x1d) {
 			debug("O");
 			return O;
 		} else if (code == 0x1e) {
 			debug("[next word]");
 			return memory[memory[++PC.value].value];
 		} else if (code == 0x1f) {
 			debug("next word (literal)");
			return memory[++PC.value];
 		}
 		debug("literal: " + (code - 0x20));
 		return new Cell(code - 0x20);
 	}
 	
 	private void skipInstruction() {
 		// This skips to the end of the next instruction - used in IF operations.
 		int instruction = memory[++PC.value].value;
 		int a, b = -1;
 		if ((instruction & 0x3) == 0) {
 			a = instruction >> 6 & 0x3f;
 		} else {
 			a = instruction >> 4 & 0x3;
 			b = instruction >> 8 & 0x3;
 		}
 		if ((a >= 0x10 && a <= 0x17) || a == 0x1e || a == 0x1f)
 			PC.value++;
 		if ((b >= 0x10 && b <= 0x17) || b == 0x1e || b == 0x1f)
 			PC.value++;
 	}
 	
 	private void processBasic(int opcode, Cell cellA, Cell cellB) {
 		int a = cellA.value;
 		int b = cellB.value;
 		switch (opcode) {
 		case 0x1: // SET a to b
 			debug("SET");
 			a = b;
 			break;
 		case 0x2: // ADD b to a
 			debug("ADD");
 			O.value = (a += b) > 0xffff ? 1 : 0;
 			a &= 0xffff;
 			break;
 		case 0x3: // SUBTRACT b from a
 			debug("SUB");
 			O.value = (a -= b) < 0 ? 0xffff : 0;
 			a = a < 0 ? 0 : a;
 			break;
 		case 0x4: // MUL multiplies a by b
 			debug("MUL");
 			O.value = (a *= b) >> 16 & 0xffff;
 			a &= 0xffff;
 			break;
 		case 0x5: // DIV divides a by b
 			debug("DIV");
 			if (b == 0) {
 				a = 0;
 				O.value = 0;
 			} else {
 				O.value = ((a << 16) / b) & 0xffff;
 				a /= b;
 			}
 			break;
 		case 0x6: // MOD (sets a to a % b)
 			debug("MOD");
 			a = (b == 0) ? 0 : a % b;
 			break;
 		case 0x7: // SHL shifts a left by b
 			debug("SHL");
 			O.value = a << b >> 16 & 0xffff;
 			a = a << b & 0xffff;
 			break;
 		case 0x8: // SHR shifts a right by b
 			debug("SHR");
 			O.value = a << 16 >>b & 0xffff;
 			a >>= b;
 			break;
 		case 0x9: // AND sets a to a & b
 			debug("AND");
 			a &= b;
 			break;
 		case 0xa: // BOR sets a to a | b
 			debug("BOR");
 			a |= b;
 			break;
 		case 0xb: // XOR sets a to a ^ b
 			debug("XOR");
 			a ^= b;
 			break;
 		case 0xc: // IFE performs next instruction if a == b
 			debug("IFE");
 			if (a != b)
 				skipInstruction();
 			break;
 		case 0xd: // IFN performs next instruction if a != b
 			debug("IFN");
 			if (a == b)
 				skipInstruction();
 			break;
 		case 0xe: // IFG performs next instruction if a > b
 			debug("IFG");
 			if (a <= b)
 				skipInstruction();
 			break;
 		case 0xf: // IFB performs next instructions if (a & b) != 0
 			debug("IFB");
 			if ((a & b) == 0)
 				skipInstruction();
 			break;
 		default:
 			debug("INVALID BASIC OPERATION");
 		}
 		cellA.value = a;
 		cellB.value = b;
 	}
 
 	private void processSpecial(int opcode, Cell a) {
 		switch (opcode) {
 		case 0x0: // EXIT custom code, makes the processor stop.
 			debug("EXIT (custom)");
 			running = false;
 			break;
 		case 0x1: // JSR pushes the address of the next instruction to the stack, sets PC to a
 			debug("JSR");
 			memory[--SP.value].value = a.value;
 			break;
 		default:
 			debug("INVALID SPECIAL OPERATION");
 		}
 	}
 	
 	public void cycle() {
 		debug("Instruction #" + instructionCount);
 		
 		int instruction = memory[PC.value].value;
 		int opcode;
 		int rawA, rawB = -1;
 		if ((instruction & 0x3) == 0) {
 			// Non-basic opcode
 			opcode = instruction & 0x3f;
 			rawA = instruction >> 6 & 0x3f;
 		} else {
 			// Basic opcode
 			opcode = instruction & 0xf;
 			rawA = instruction >> 4 & 0x3f;
 			rawB = instruction >> 10 & 0x3f;
 		}
 		
 		//System.out.println(Integer.toBinaryString(instruction));
 		//System.out.printf("opcode: %s\n", Integer.toBinaryString(opcode));
 		//System.out.printf("argument A: %s; argument B: %s\n", Integer.toBinaryString(rawA), rawB == -1 ? null : Integer.toBinaryString(rawB));
 		
 		System.out.print("A: ");
 		Cell a = handleArgument(rawA), b = null;
 		if (rawB != -1) {
 			System.out.print("B: ");
 			b = handleArgument(rawB);
 		}
 				
 		if (b != null)
 			processBasic(opcode, a, b);
 		else
 			processSpecial(opcode, a);
 
 		PC.value++;
 		instructionCount++;
 	}
 }
