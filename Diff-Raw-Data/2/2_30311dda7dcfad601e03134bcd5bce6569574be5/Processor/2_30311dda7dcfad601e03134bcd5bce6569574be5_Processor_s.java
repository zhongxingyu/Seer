 package nl.tomsanders.processenprocessoren.emulator;
 
 import java.util.Arrays;
 
 public class Processor {
 	private static final int RAM_SIZE_BYTES = 256 * 1024;
 	private static final int RAM_SIZE_WORDS = RAM_SIZE_BYTES / 4;
 	private static final int REGISTER_COUNT = 16;
 	
 	private static final int PROGRAM_COUNTER_REGISTER = 15;
 	private static final int ZERO_REGISTER = 0;
 	
 	private static final int FLAG_COUNT = 4;
 	private static final int FLAG_NEGATIVE = 0;
 	private static final int FLAG_OVERFLOW = 1;
 	private static final int FLAG_ZERO = 2;
 	private static final int FLAG_CARRY = 3;
 	
 	private static final int OPC_HALT = 0;
 	private static final int OPC_TEMP1 = 2; // AVAILABLE; INT
 	private static final int OPC_LOADFLAGS = 4;
 	private static final int OPC_TEMP2 = 6; // AVAILABLE; IRET
 	private static final int OPC_READ = 8;
 	private static final int OPC_WRITE = 12;
 	private static final int OPC_ARITH = 16;
 	private static final int OPC_LOADCONST = 32;
 	
 	private static final int ALU_OPC_ROL = 0;
 	private static final int ALU_OPC_AND = 1;
 	private static final int ALU_OPC_OR = 2;
 	private static final int ALU_OPC_XOR = 3;
 	private static final int ALU_OPC_ADD = 4;
 	private static final int ALU_OPC_SUB = 7;
 	
 	private static final int CONDITION_T = 0;
 	private static final int CONDITION_F = 1;
 	private static final int CONDITION_C = 2;
 	private static final int CONDITION_NC = 3;
 	private static final int CONDITION_GE = 4;
 	private static final int CONDITION_L = 5;
 	private static final int CONDITION_NO = 6;
 	private static final int CONDITION_O = 7;
 	private static final int CONDITION_NZ = 8;
 	private static final int CONDITION_Z = 9;
 	private static final int CONDITION_GU = 10;
 	private static final int CONDITION_LEU = 11;
 	private static final int CONDITION_G = 12;
 	private static final int CONDITION_LE = 13;
 	private static final int CONDITION_NN = 14;
 	private static final int CONDITION_N = 15;
 	
 	private boolean halted;
 	private int ram[];
 	private int registers[];
 	private boolean flags[];
 	
 	private AsyncInputBuffer inputBuffer;
 	
 	public Processor() {
 		this.ram = new int[RAM_SIZE_WORDS];
 		this.registers = new int[REGISTER_COUNT];
 		this.flags = new boolean[FLAG_COUNT];
 		
 		this.reset();
 	}
 
 	public void reset() {
 		this.halted = false;
 		this.inputBuffer = new AsyncInputBuffer();
 		
 		Arrays.fill(this.ram, 0);
 		Arrays.fill(this.registers, 0);
 		Arrays.fill(this.flags, false);
 	}
 	
 	public boolean isHalted() {
 		return this.halted;
 	}
 	
 	public void loadIntoRam(byte[] bytes) {
 		int[] instructions = ByteHelper.toIntArray(bytes);
 		System.arraycopy(instructions, 0, 
 				this.ram, 0, instructions.length);
 	}
 	
 	public void loadIntoRam(int[] words) {
 		System.arraycopy(words, 0, this.ram, 0, words.length);
 	}
 	
 	public void cycle() {
 		if (!this.halted) {
 			int instruction = this.fetchInstruction();
 			this.registers[PROGRAM_COUNTER_REGISTER] += 4;
 		
 			int opcode = ByteHelper.getBits(instruction, 18, 23);
 			if (opcode == OPC_HALT) 
 				this.halt();
 			else if (opcode == OPC_LOADFLAGS)
 				this.loadFlags(instruction);
 			else if (opcode == OPC_READ)
 				this.readRamInstruction(instruction);
 			else if (opcode == OPC_WRITE)
 				this.writeRamInstruction(instruction);
 			else if (opcode >= OPC_ARITH && opcode < OPC_LOADCONST)
 				this.processArith(instruction);
 			else if (opcode >= OPC_LOADCONST)
 				this.loadConstant(instruction);
 		}
 	}
 
 	private int fetchInstruction() {
 		int address = this.registers[PROGRAM_COUNTER_REGISTER];
 		System.out.println("Fetching instruction at line " + ((address / 4) + 1));
 		return this.readAddress(address);
 	}
 
 	public void halt() {
 		this.halted = true;
 		this.inputBuffer.close();
 	}
 	
 	public void loadFlags(int instruction) {
 		this.flags[FLAG_NEGATIVE] = ByteHelper.getBit(instruction, 17) == 1;
 		this.flags[FLAG_OVERFLOW] = ByteHelper.getBit(instruction, 16) == 1;
 		this.flags[FLAG_ZERO] = ByteHelper.getBit(instruction, 9) == 1;
 		this.flags[FLAG_CARRY] = ByteHelper.getBit(instruction, 8) == 1;
 	}
 	
 	public void readRamInstruction(int instruction) {
 		int destRegister = ByteHelper.getBits(instruction, 18, 31);
 		int addressRegister = ByteHelper.getBits(instruction, 0, 3);
 		int offset = ByteHelper.getSignedBits(instruction, 8, 17);
 		
 		int readAddress = this.registers[addressRegister] + offset;
 		this.writeRegister(destRegister, this.readAddress(readAddress));
 	}
 	
 	private void writeRamInstruction(int instruction) {
 		int srcRegister = ByteHelper.getBits(instruction, 4, 7);
 		int addressRegister = ByteHelper.getBits(instruction, 0, 3);
 		int offset = ByteHelper.getSignedBits(instruction, 8, 17);
 		
 		int writeAddress = this.registers[addressRegister] + offset;
 		this.writeAddress(writeAddress, this.registers[srcRegister]);
 	}
 	
 	private void processArith(int instruction) {
 		int registerB = ByteHelper.getBits(instruction, 0, 3);
 		int registerA = ByteHelper.getBits(instruction, 4, 7);
 		int constant = ByteHelper.getSignedBits(instruction, 8, 17);
 		boolean useConstant = ByteHelper.getBit(instruction, 18) == 0;
 		
 		int valueB = this.registers[registerB];
 		int valueA = useConstant ? constant : this.registers[registerA];
 		
 		if (this.testCondition(instruction)) {
 			int opcode = ByteHelper.getBits(instruction, 19, 21);
 			
 			int result = 0;
 			this.flags[FLAG_OVERFLOW] = false;
 			this.flags[FLAG_CARRY] = false;
 			
 			if (opcode == ALU_OPC_ROL) {
 				result = Integer.rotateLeft(valueB, valueA);
 				this.flags[FLAG_OVERFLOW] = (ByteHelper.getBit(valueB, 31) ^ ByteHelper.getBit(result, 31)) == 1;
 				this.flags[FLAG_CARRY] = ByteHelper.getBit(result, 0) == 1;
 			} else if (opcode == ALU_OPC_AND) {
 				result = valueA & valueB;
 			} else if (opcode == ALU_OPC_OR) {
 				result = valueA | valueB;
 			} else if (opcode == ALU_OPC_XOR) {
 				result = valueA ^ valueB;
 			} else if (opcode == ALU_OPC_ADD) {
 				result = valueA + valueB;
 				this.flags[FLAG_OVERFLOW] = ((long)valueA) + valueB > Integer.MAX_VALUE;
 				this.flags[FLAG_CARRY] = valueA < 0 && result >= 0;
 			} else if (opcode == ALU_OPC_SUB) {
 				result = valueA - valueB;
 				this.flags[FLAG_OVERFLOW] = ((long)valueA) - valueB < Integer.MIN_VALUE;
 				this.flags[FLAG_CARRY] = valueA < 0 && result >= 0;
 			}
 			this.flags[FLAG_ZERO] = (result == 0);
 			this.flags[FLAG_NEGATIVE] = ByteHelper.getBit(result, 31) == 1;
 			
 			int destRegister = ByteHelper.getBits(instruction, 28, 31);
 			this.writeRegister(destRegister, result);
 		}
 	}
 
 	private boolean testCondition(int instruction) {
 		int condition = ByteHelper.getBits(instruction, 24, 27);
 		if (condition == CONDITION_T)
 			return true;
 		else if (condition == CONDITION_F)
 			return false;
 		else if (condition == CONDITION_C)
 			return this.flags[FLAG_CARRY];
 		else if (condition == CONDITION_NC)
 			return !this.flags[FLAG_CARRY];
 		else if (condition == CONDITION_GE)
 			return (this.flags[FLAG_NEGATIVE] ^ this.flags[FLAG_OVERFLOW]) == false;
 		else if (condition == CONDITION_L)
 			return this.flags[FLAG_NEGATIVE] ^ this.flags[FLAG_OVERFLOW];
 		else if (condition == CONDITION_NO)
 			return !this.flags[FLAG_OVERFLOW];
 		else if (condition == CONDITION_O)
 			return this.flags[FLAG_OVERFLOW];
 		else if (condition == CONDITION_NZ)
 			return !this.flags[FLAG_ZERO];
 		else if (condition == CONDITION_Z)
 			return this.flags[FLAG_ZERO];
 		else if (condition == CONDITION_GU)
 			return !(this.flags[FLAG_ZERO] | this.flags[FLAG_CARRY]);
 		else if (condition == CONDITION_LEU)
 			return this.flags[FLAG_ZERO] | this.flags[FLAG_CARRY];
 		else if (condition == CONDITION_G)
 			return (this.flags[FLAG_ZERO] | (this.flags[FLAG_NEGATIVE] ^ this.flags[FLAG_OVERFLOW])) == false;
 		else if (condition == CONDITION_LE)
 			return this.flags[FLAG_ZERO] | (this.flags[FLAG_NEGATIVE] ^ this.flags[FLAG_OVERFLOW]);
 		else if (condition == CONDITION_NN)
 			return !this.flags[FLAG_NEGATIVE];
 		else if (condition == CONDITION_N)
 			return this.flags[FLAG_NEGATIVE];
 		else
 			throw new RuntimeException("Condition not recognized");
 	}
 	
 	private void loadConstant(int instruction) {
 		if (testCondition(instruction)) {
 			int destRegister = ByteHelper.getBits(instruction, 28, 31);
 			int constant = ByteHelper.getSignedBits(instruction, 0, 21);
 			
 			boolean loadHigh = ByteHelper.getBit(instruction, 22) == 1;
 			if (loadHigh)
 				constant <<= 10;
 			
 			this.writeRegister(destRegister, constant);
 		}
 	}
 
 	public void writeRegister(int register, int value) {
 		if (register >= 0 && register < this.registers.length) {
 			if (register != ZERO_REGISTER) {
 				System.out.println("Wrote to R" + register + " value " + value);
 				this.registers[register] = value;
 			}
 		} else {
 			throw new RuntimeException("Register doesn't exist");
 		}
 	}
 	
 	public void writeAddress(int address, int value) {
 		if (address % 4 == 0) {
 			if (address >= 0 && address < RAM_SIZE_BYTES) {
 				int realAddress = address / 4;
 				this.ram[realAddress] = value;
 				
 				System.out.println("Wrote to RAM at " + address + " value " + value);
 			} else if (address >= 0xFFFFFF00 && address <= 0xFFFFFFFC) {
				System.out.println("Printing ASCII char: " + (char)(48 + value));
 			} else {
 				System.out.println("Tried writing to " + address + 
 						", but no device was found");
 			}
 		} else {
 			throw new RuntimeException("Address is not a multiple of 4");
 		}
 	}
 	
 	public int readAddress(int address) {
 		if (address % 4 == 0) {
 			if (address >= 0 && address < RAM_SIZE_BYTES) {
 				int realAddress = address / 4;
 				return this.ram[realAddress];
 			} else if (address >= 0xFFFFFF00 && address <= 0xFFFFFFFC) {
 				return this.inputBuffer.getNext();
 			} else {
 				System.out.println("Tried reading from " + address + 
 						", but no device was found");
 				return 0;
 			}
 		}  else {
 			throw new RuntimeException("Address is not a multiple of 4");
 		}
 	}
 	
 }
