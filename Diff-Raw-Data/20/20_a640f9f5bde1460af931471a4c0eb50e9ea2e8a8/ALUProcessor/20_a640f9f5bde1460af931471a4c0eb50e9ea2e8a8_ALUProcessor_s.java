 /*******************************************************************************
  * Copyright (c) 2012 Stephen Wan.
  * All rights reserved.
  * 
  * This program is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  * 
  *     This program is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  * 
  *     You should have received a copy of the GNU General Public License
  *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package com.stephenwan.ljavac3.core;
 
 public class ALUProcessor {
 
 	public ALUProcessor(ALU alu)
 	{
 		cache = Instruction.values();
 		this.alu = alu;
 	}
 	
 	public enum Instruction
 	{
 		BR, ADD, LD, ST, JSR, AND, LDR, STR, RTI, NOT, LDI, STI, JMP, INVALID, LEA, TRAP
 	}
 	public static String[] InstructionT = { "BR", "ADD", "LD", "ST", "JSR", "AND", "LDR", "STR", "RTI", "NOT", "LDI", "STI", "JMP", "INVALID", "LEA", "TRAP" };
	public static String[] InstructionMatchers = { "BRn{0-1}z{0-1}p{0-1}", "ADD", "LD", "ST", "JSR", "AND", "LDR", "STR", "RTI", "NOT", "LDI", "STI", "JMP", "INVALID", "LEA", "TRAP" };
 	
 	public ALU alu;
 	public Instruction[] cache;
 	
 	public void execInstruction(String content) throws LC3Exception
 	{
 		Instruction opcode = cache[Integer.parseInt(content.substring(0, 4), 2)];
 		String sOperands = content.substring(4);
 		
 		switch(opcode)
 		{
 			case ADD:
 			{
 				
 				// ADD has two different formats with common destination/source 1 registers
 				
 				int dr = Tools.bin2int(sOperands.substring(0, 3));
 				int sr = Tools.bin2int(sOperands.substring(3, 6));
 				int sr1c = alu.core.getRegister(sr);
 				
 				// 0001 [DR, SR1, 000, SR2]
 				if (sOperands.charAt(6) == '0')
 				{
 					int sr2 = Tools.bin2int(sOperands.substring(9));	
 					int sr2c = alu.core.getRegister(sr2);
 					String result = Integer.toBinaryString(sr1c + sr2c);
 					
 					alu.core.writeRegister(dr, result);
 				}
 				
 				// 0001 [DR, SR1, 1, imm5]
 				else
 				{
 					int immediate = Tools.bin2int(Tools.sext(sOperands.substring(7)));
 					String result = Integer.toBinaryString(sr1c + immediate);
 					alu.core.writeRegister(dr, result);
 				}
 				break;
 				
 			}
 			case AND:
 			{
 				
 				// AND has two different instruction formats, similar to ADD
 				
 				int dr = Tools.bin2int(sOperands.substring(0, 3));
 				int sr = Tools.bin2int(sOperands.substring(3, 6));
 				int sr1c = alu.core.getRegister(sr);
 				
 				// 0101 [DR, SR1, 000, SR2]
 				if (sOperands.charAt(6) == '0')
 				{
 					int sr2 = Tools.bin2int(sOperands.substring(9));	
 					int sr2c = alu.core.getRegister(sr2);
 					String result = Integer.toBinaryString(sr1c & sr2c);
 					
 					alu.core.writeRegister(dr, result);
 				}
 				
 				// 0101 [DR, SR1, 1, imm5]
 				else
 				{
 					int immediate = Tools.bin2int(Tools.sext(sOperands.substring(7)));
 					String result = Integer.toBinaryString(sr1c & immediate);
 					
 					alu.core.writeRegister(dr, result);
 				}
 				
 				break;
 			}
 			case BR:
 			{
 				int offset = Tools.bin2int(Tools.sext(sOperands.substring(3)));
 				if ((Integer.parseInt(sOperands.substring(0,3), 2) & alu.core.nzpFlags) > 0) // check nzp via bitmasks
 					alu.core.movePCRelative(offset);
 				break;
 			}
 			case JMP:
 			{
 				int br = Tools.bin2int(sOperands.substring(3,6));
 				int brc = alu.core.getRegister(br);
 				alu.core.pc = brc;
 				break;
 			}
 			case JSR:
 			{
 				boolean check = (sOperands.charAt(0) == '1');
 				if (check)
 				{
 					// JSR
 					alu.core.pc += Tools.bin2int(Tools.sext(sOperands.substring(1)));
 				}
 				else
 				{
 					// JSRR
 					int br = Tools.bin2int(sOperands.substring(3,6));
 					int brc = alu.core.getRegister(br);
 					alu.core.pc = brc;
 				}
 				break;
 			}
 			case LD:
 			{
 				int dr = Tools.bin2int(sOperands.substring(0, 3));
 				int offset = Tools.bin2int(Tools.sext(sOperands.substring(3)));
 				
 				int result = alu.core.getMemory(offset + alu.core.pc);
 				
 				alu.core.writeRegister(dr, result);
 				break;
 			}
 			case LDI:
 			{
 				int dr = Tools.bin2int(sOperands.substring(0, 3));
 				int offset = Tools.bin2int(Tools.sext(sOperands.substring(3)));
 				int result = alu.core.getMemory(alu.core.getMemory(offset + alu.core.pc));
 				
 				alu.core.writeRegister(dr, result);
 				break;
 			}
 			case LDR:
 			{
 				int dr = Tools.bin2int(sOperands.substring(0, 3));
 				int baser = Tools.bin2int(sOperands.substring(3, 6));
 				int offset = Tools.bin2int(Tools.sext(sOperands.substring(6)));
 				
 				int result = alu.core.getMemory(baser + offset);
 			
 				alu.core.writeRegister(dr, result);
 				break;
 			}
 			case LEA:
 			{
 				int dr = Tools.bin2int(sOperands.substring(0, 3));
 				int offset = Tools.bin2int(Tools.sext(sOperands).substring(3));
 				String result = Integer.toBinaryString(alu.core.pc + offset);
 				
 				alu.core.writeRegister(dr, result);
 				break;
 			}
 			case NOT:
 			{
 				int dr = Tools.bin2int(sOperands.substring(0, 3));
 				int sr = Tools.bin2int(sOperands.substring(3, 6));
 				int data = ~alu.core.getRegister(sr);
 				
 				String result = Integer.toBinaryString(data);
 				
 				alu.core.writeRegister(dr, result);
 				break;
 			}
 			case RTI:
 			{
 				// not implemented
 				if (alu.core.supervisorMode)
 				{
 					
 				}
 				else
 					throw new LC3Exception("This command can only be invoked in Supervisor mode");
 				break;
 			}
 			case ST:
 			{
 				int sr = Tools.bin2int(sOperands.substring(0, 3));
 				int src = alu.core.getRegister(sr);
 				int offset = Tools.bin2int(Tools.sext(sOperands.substring(3)));
 				
 				alu.core.writeMemory(alu.core.pc + offset, src);
 				break;
 			}
 			case STI:
 			{
 				int sr = Tools.bin2int(sOperands.substring(0, 3));
 				int src = alu.core.getRegister(sr);
 				int offset = Tools.bin2int(Tools.sext(sOperands.substring(3)));
 				
				int location = alu.core.getMemory(alu.core.pc + offset);
 				
 				alu.core.writeMemory(location, src);
 				break;
 			}
 			case STR:
 			{
 				int sr = Tools.bin2int(sOperands.substring(0, 3));
 				int src = alu.core.getRegister(sr);
 				int baser = Tools.bin2int(sOperands.substring(3, 6));
 				int basercontent = alu.core.getRegister(baser);
 				int offset = Tools.bin2int(sOperands.substring(6));
 				
 				alu.core.writeMemory(basercontent + offset, src);
 				
 				break;
 			}
 			case TRAP:
 			{
 				int vector = Tools.bin2int(sOperands.substring(4));
 				// interrupts
 				switch (vector)
 				{
 					case 0x20:
 						break;
 					case 0x21:
 						break;
 					case 0x22:
 						break;
 					case 0x23:
 						break;
 					case 0x24:
 						break;
 					
 					// HALT
 					case 0x25:
 						alu.executing = false;
 						break;
 					
 				}
 				break;
 			}
 			default:
 			{
 				// uh oh
 				throw new LC3Exception("Unknown opcode '" + opcode + "' was received by the processing unit");
 			}
 		}
 		
 	}
 }
