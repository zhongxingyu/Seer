 package com.badlogic.dcpu;
 
 import com.badlogic.dcpu.Cpu.Opcode;
 
 /**
  * Disassembler for <a href="http://0x10c.com/doc/dcpu-16.txt">dcpu-16</a>, some
  * assumptions have to be made since 
  * @author mzechner
  *
  */
 public class Disassembler {
 	String[] registerNames = { "a", "b", "c", "x", "y", "z", "i", "j" };
 	
 	private int decodeArgument(int arg, int nextWord, StringBuffer buffer) {		
 		switch(arg) {
 		case 0x0:
 		case 0x1:
 		case 0x2:
 		case 0x3:
 		case 0x4:
 		case 0x5:
 		case 0x6:
 		case 0x7:
 			buffer.append(registerNames[arg]);
 			return 0;
 		case 0x8:
 		case 0x9:
 		case 0xa:
 		case 0xb:
 		case 0xc:
 		case 0xd:
 		case 0xe:
 		case 0xf:
 			buffer.append("["); 
 			buffer.append(registerNames[arg - 0x8]); 
 			buffer.append("]");
 			return 0;
 		case 0x10:
 		case 0x11:
 		case 0x12:
 		case 0x13:
 		case 0x14:
 		case 0x15:
 		case 0x16:
 		case 0x17:
 			buffer.append("[0x"); 
 			buffer.append(Integer.toHexString(nextWord));
 			buffer.append("+");
 			buffer.append(registerNames[arg - 0x10]);
 			buffer.append("]");
 			return 1;
 		case 0x18:
 			buffer.append("pop");
 			return 0;
 		case 0x19:
 			buffer.append("peek");
 			return 0;
 		case 0x1a:
 			buffer.append("push");
 			return 0;
 		case 0x1b:
 			buffer.append("sp");
 			return 0;
 		case 0x1c:
 			buffer.append("pc");
 			return 0;
 		case 0x1d:
 			buffer.append("0");
 			return 0;
 		case 0x1e:
 			buffer.append("[0x");
 			buffer.append(Integer.toHexString(nextWord));
 			buffer.append("]");
 			return 1;
 		case 0x1f:
 			buffer.append("0x");
 			buffer.append(Integer.toHexString(nextWord));
 			return 1;
 		default:
 			if(arg >= 0x20 && arg <= 0x3f) {
 				buffer.append("0x");
 				buffer.append(Integer.toHexString(arg - 0x20));
 				return 0;
 			}
 			throw new RuntimeException("Unkown load operator 0x" + Integer.toHexString(arg));
 		}		
 	}
 		
 	private String pad(String hex) {
 		if(hex.length() == 4) return hex;
 		StringBuffer buffer = new StringBuffer();
 		for(int i = 0; i < 4 - hex.length(); i++) buffer.append("0");
 		buffer.append(hex);
 		return buffer.toString();
 	}
 	
 	public String disassemble(int[] mem, int offset, int len) {
 		StringBuffer buffer = new StringBuffer();
 		
 		boolean lastWasJump = false;
 		int end = offset + len;
 		for(int pc = offset; pc < end;) {
 			int v = mem[pc++];
 			int oc = v & 0xf;
 			if(oc > Cpu.OPCODES.length) throw new RuntimeException("Unkown opcode 0x" + Integer.toHexString(oc) + " at address " + (pc - 1));
 			Opcode opcode = Cpu.OPCODES[oc];
 			int a = (v & 0x3f0) >>> 4;
 			int b = (v & 0xfc00) >>> 10;
 
			buffer.append(pad(Integer.toHexString(pc - 1)));
 			buffer.append(":     ");
 			if(lastWasJump) buffer.append("   ");
 			lastWasJump = opcode.mnemonic.charAt(0) == 'i';
 			buffer.append(opcode.mnemonic);
 			buffer.append(" ");
 			pc += decodeArgument(a, pc < end? mem[pc]: 0, buffer);
 			buffer.append(", ");
 			pc += decodeArgument(b, pc < end? mem[pc]: 0, buffer);
  			buffer.append("\n");
 		}
 		
 		return buffer.toString();
 	}
 	
 	public static void main(String[] args) {
 		int[] dump = Cpu.loadDump("data/simple.dcpu");
 		System.out.println(new Disassembler().disassemble(dump, 0, dump.length)); 
 	}
 }
