 package com.badlogic.dcpu;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 
 import com.badlogic.dcpu.Assembler.Arg;
 import com.badlogic.dcpu.Cpu.Opcode;
 import com.badlogic.dcpu.Cpu.Register;
 
 public class Debugger {
 	private Cpu cpu;
 	private final PrintStream out;
 	private final BufferedReader in;
 	
 	public Debugger(Cpu cpu) {
 		this.cpu = cpu;
 		out = System.out;
 		in = new BufferedReader(new InputStreamReader(System.in));
 	}
 	
 	public void printNextInstr() {
		out.println(Disassembler.disassembleInstr(cpu.getMemory(), cpu.getRegValue(Register.PC)));
 	}
 	
 	private String pad(String hex) {
 		if(hex.length() == 4) return hex;
 		StringBuffer buffer = new StringBuffer();
 		for(int i = 0; i < 4 - hex.length(); i++) buffer.append("0");
 		buffer.append(hex);
 		return buffer.toString();
 	}
 	
 	public void printRegs() {
 		for(Register reg: Register.values()) {
 			out.print(reg.mnemonic + ":" + pad(Integer.toHexString(cpu.getRegValue(reg))) + " ");
 		}
 		out.println();
 	}
 	
 	public void printMem(int offset, int len) {
 		for(int i = offset, j = 1; i < offset + len; i++, j++) {
 			out.print(pad(Integer.toHexString(cpu.getMemValue(i))) + " ");
 			if(j != 0 && j % 8 == 0) out.println();
 		}
 		out.println();
 	}
 	
 	public void step() {
 		cpu.tick();
 		if(cpu.isNextSkipped()) cpu.tick();
 	}
 	
 	private int parseInt(String value) {
 		try {
 			return Integer.parseInt(value);
 		} catch(NumberFormatException e) {
 			return Integer.parseInt(value.replace("0x", ""), 16);
 		}
 	}
 	
 	public void run() {		
 		
 		do {
 			try {
 				printNextInstr();				
 				out.print("> ");
 				String[] tokens = in.readLine().trim().split("\\s");				
 				if(tokens[0].equals("step") || tokens[0].equals("s")) {
 					step();
 					continue;
 				}
 				if(tokens[0].equals("regs") || tokens[0].equals("r")) {
 					printRegs();
 					continue;
 				}
 				if(tokens[0].equals("mem") || tokens[0].equals("m")) {
 					if(tokens.length != 2 && tokens.length != 3) {
 						out.println("expected 'mem <address>', or mem <address> <num-words>");
 						continue;
 					}
 					if(tokens.length == 2) printMem(parseInt(tokens[1]), 1);
 					else printMem(parseInt(tokens[1]), parseInt(tokens[2]));
 					continue;
 				}
 				if(tokens[0].equals("set")) {
 					if(tokens.length != 3) {
 						out.println("expected 'set <address> <value>'");
 						continue;
 					}
 					cpu.getMemory()[parseInt(tokens[1])] = (short)(parseInt(tokens[2]) & 0xffff);
 				}
 				if(tokens[0].equals("load") || tokens[0].equals("l")) {
 					if(tokens.length != 2) {
 						out.println("expected 'load <file-name>'");
 						continue;
 					}
 					cpu = new Cpu(Disassembler.loadDump(tokens[2]));
 					continue;
 				}
 				if(tokens[0].equals("quit") || tokens[0].equals("q")) {
 					System.exit(-1);
 				}
 			} catch (IOException e) {
 				throw new RuntimeException(e);
 			} catch (NumberFormatException e) {
 				out.println("Number not well formatted");
 			}
 		} while(true);
 	}
 	
 	public static void main(String[] args) {
 		Assembler asm = new Assembler();
 		asm.eop(Opcode.JSR, asm.label("main"));
 		
 		asm.markLabel("func");
 		asm.op(Opcode.SHR, Arg.reg(Register.X), Arg.lit(0x4));
 		asm.op(Opcode.SET, Arg.pc(), Arg.pop());
 		
 		asm.markLabel("main");
 		asm.op(Opcode.SET, Arg.reg(Register.X), Arg.lit(0xf0));
 		asm.eop(Opcode.JSR, asm.label("func"));
 		
 		
 		short[] dump = asm.getDump();
		System.out.println(Disassembler.disassemble(dump, 0, dump.length));
 		
 		Cpu cpu = new Cpu(dump);
 //		Cpu cpu = new Cpu(Disassembler.loadDump("data/simple.dcpu"));
 		Debugger debugger = new Debugger(cpu);
 		debugger.run();
 	}
 }
