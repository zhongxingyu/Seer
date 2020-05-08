 package org.jackie.jclassfile.code.impl;
 
 import org.jackie.jclassfile.code.Instruction;
 import org.jackie.utils.ChainImpl;
 
 import java.io.DataOutput;
 import java.io.IOException;
 import java.io.DataInput;
 import java.util.List;
 import java.util.ArrayList;
 
 /**
  * @author Patrik Beno
  */
 public abstract class AbstractInstruction extends ChainImpl<Instruction> implements Instruction {
 
 	int opcode;
 
 	protected AbstractInstruction(int opcode, DataInput in, Instruction previous) throws IOException {
 		this.opcode = opcode;
 		if (previous != null) { previous.append(this); }
 		load(in);
 	}
 
 	protected AbstractInstruction(int opcode) {
 		this.opcode = opcode;
 	}
 
 	public int opcode() {
 		return opcode;
 	}
 
 	public int size() {
 		return 1;
 	}
 
	// todo optimize this (this is HUGE performance bottleneck)
 	public int offset() {
 		int offset = 0;
 		Instruction insn = previous();
 		while (insn != null) {
 			offset += insn.size();
 			insn = insn.previous();
 		}
 		return offset;
 	}
 
 	public List<Instruction> asList() {
 		ArrayList<Instruction> list = new ArrayList<Instruction>(length());
 		for (Instruction insn : this) {
 			list.add(insn);
 		}
 		return list;
 	}
 
 	protected void load(DataInput in) throws IOException {
 		// opcode we already have
 		loadOperands(in);
 	}
 
 	public final void save(DataOutput out) throws IOException {
 		out.writeByte(opcode);
 		saveOperands(out);
 	}
 
 	protected abstract void loadOperands(DataInput in) throws IOException;
 
 	protected abstract void saveOperands(DataOutput out) throws IOException;
 
 	public String toString() {
 		return String.format("#%d (@%d) %s", index(), offset(), Opcode.forOpcode(opcode()));
 	}
 }
