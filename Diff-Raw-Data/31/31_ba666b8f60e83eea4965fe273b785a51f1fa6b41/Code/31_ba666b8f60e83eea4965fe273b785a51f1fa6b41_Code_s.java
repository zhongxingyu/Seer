 /**
  * Translates code symbols to binary.
  *
  * @author Ben "the Pyrate" Mendis
  */
 
 package org.hacdc.tecs.project_6.assembler;
 
 import java.util.HashMap;
 
 public class Code {
 	protected static HashMap<String, String> comp_table;
 	protected static HashMap<String, String> dest_table;
 	protected static HashMap<String, String> jump_table;
 
 	public Code () {
 		this.comp_table = new HashMap<String, String>();
 		this.comp_table.put("0", "0101010");
 		this.comp_table.put("1", "0111111");
 		this.comp_table.put("-1", "0111010");
 		this.comp_table.put("D", "0001100");
 		this.comp_table.put("A", "0110000");
 		this.comp_table.put("!D", "0001101");
 		this.comp_table.put("!A", "0110001");
 		this.comp_table.put("-D", "0001111");
 		this.comp_table.put("-A", "0110011");
 		this.comp_table.put("D+1", "0011111");
 		this.comp_table.put("A+1", "0110111");
 		this.comp_table.put("D-1", "0001110");
 		this.comp_table.put("A-1", "0110010");
 		this.comp_table.put("D+A", "0000010");
 		this.comp_table.put("D-A", "0010011");
 		this.comp_table.put("A-D", "0000111");
 		this.comp_table.put("D&A", "0000000");
 		this.comp_table.put("D|A", "0010101");
 		this.comp_table.put("M", "1110000");
 		this.comp_table.put("!M", "1110001");
 		this.comp_table.put("-M", "1110011");
 		this.comp_table.put("M+1", "1110111");
 		this.comp_table.put("M-1", "1110010");
 		this.comp_table.put("D+M", "1000010");
 		this.comp_table.put("D-M", "1010011");
 		this.comp_table.put("M-D", "1000111");
 		this.comp_table.put("D&M", "1000000");
 		this.comp_table.put("D|M", "1010101");
 
 		this.dest_table = new HashMap<String, String>();
		this.dest_table.put("null", "000");
 		this.dest_table.put("M", "001");
 		this.dest_table.put("D", "010");
 		this.dest_table.put("MD", "011");
 		this.dest_table.put("A", "100");
 		this.dest_table.put("AM", "101");
 		this.dest_table.put("AD", "110");
 		this.dest_table.put("AMD", "111");
 
 		this.jump_table = new HashMap<String, String>();
		this.jump_table.put("null", "000");
 		this.jump_table.put("JGT", "001");
 		this.jump_table.put("JEQ", "010");
 		this.jump_table.put("JGE", "011");
 		this.jump_table.put("JLT", "100");
 		this.jump_table.put("JNE", "101");
 		this.jump_table.put("JLE", "110");
 		this.jump_table.put("JMP", "111");
 	}
 
 	public String translate (Command command) {
 		if (command instanceof ACommand) {
 			return this.translateACommand((ACommand) command);
 		} else if (command instanceof CCommand) {
 			return this.translateCCommand((CCommand) command);
 		} else {
 			throw new IllegalArgumentException("Unkown command type.");
 		}
 	}
 
 	protected String translateACommand (ACommand command) {
 		String binary = Integer.toBinaryString(Integer.parseInt(command.getAddress()));
 		if (binary.length() > 15) {
 			throw new IllegalCommandException("Address is too large.");
 		}
 		while (binary.length() < 16) {
 			binary = "0" + binary;
 		}
 		return binary;
 	}
 
 	protected String translateCCommand (CCommand command) {
 		String binary = "111";
 		if (!this.comp_table.containsKey(command.getComp())) {
 			throw new IllegalCommandException("Unknown comp symbol.");
 		}
 		binary = binary + this.comp_table.get(command.getComp());
 		if (!this.dest_table.containsKey(command.getDest())) {
 			throw new IllegalCommandException("Unknown dest symbol.");
 		}
 		binary = binary + this.dest_table.get(command.getDest());
 		if (!this.jump_table.containsKey(command.getJump())) {
 			throw new IllegalCommandException("Unknown jump symbol.");
 		}
 		binary = binary + this.jump_table.get(command.getJump());
 		return binary;
 	}
 }
 
