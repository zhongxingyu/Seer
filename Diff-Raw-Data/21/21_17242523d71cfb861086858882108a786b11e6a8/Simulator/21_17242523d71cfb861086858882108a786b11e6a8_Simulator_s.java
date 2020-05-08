 import java.util.Scanner;
 
 public class Simulator {
 	private int[] memory;
 	private int instruction;
 	private int accumulator;
 
 	public Simulator(int[] memory) {
 		this.memory = memory;
 		instruction = 0;
 		accumulator = 0;
 	}
 
	public boolean step() throws OutOfMemoryException, DivisionByZeroException, InvalidOpcodeException, AccumulatorOverflowException {
 		if(instruction > 99)
 			throw new OutOfMemoryException();
 
 		int opcode = memory[instruction] / 100;
 		int argument = memory[instruction] % 100;
 
 		switch(opcode) {
 			case 10:
 				System.out.print("> ");
				memory[argument] = (new Scanner(System.in)).nextByte();
 				break;
 			case 11:
 				System.out.println(memory[argument]);
 				break;
 			case 20:
 				accumulator = memory[argument];
 				break;
 			case 21:
 				memory[argument] = accumulator;
 				break;
 			case 30:
 				accumulator += memory[argument];
 				break;
 			case 31:
 				accumulator -= memory[argument];
 				break;
 			case 32:
 				if(memory[argument] == 0)
 					throw new DivisionByZeroException();
 				accumulator /= memory[argument];
 				break;
 			case 33:
 				accumulator *= memory[argument];
 				break;
 			case 40:
 				instruction = argument;
 				return true;
 			case 41:
 				if(accumulator < 0) {
 					instruction = argument;
 					return true;
 				}
 				break;
 			case 42:
 				if(accumulator == 0) {
 					instruction = argument;
 					return true;
 				}
 				break;
 			case 43:
 				return false;
 			default:
 				throw new InvalidOpcodeException();
 		}
 
 		if(accumulator > 9999 || accumulator < -9999)
 			throw new AccumulatorOverflowException();
 
 		instruction++;
 
 		return true;
 	}
 
 	public void print() {
 		System.err.println("Instruction Pointer: " + instruction);
 		System.err.println("Instruction Register: " + memory[instruction]);
 
 		System.err.println("Accumulator: " + accumulator);
 
 		System.err.println("Memory:");
 		for(int i = 0; i < 100; i += 0) { //i += 0 because Java does it's expressions weirdly
 			System.err.printf("%2d |", i);
 			for(int ii = 0; ii < 10; ii++, i++) {
 				System.err.printf(" %4d", memory[i]);
 			}
 			System.err.println();
 		}
 	}
 
 	public void print(int n) {
 		System.err.println(memory[n]);
 	}
 
 	public void set(int n, int val) {
 		memory[n] = val;
 	}
 
 	public int get(int n) {
 		return memory[n];
 	}
 
 	public int getInstruction() {
 		return instruction;
 	}
 
 	public void setInstruction(int instruction) {
 		this.instruction = instruction;
 	}
 }
