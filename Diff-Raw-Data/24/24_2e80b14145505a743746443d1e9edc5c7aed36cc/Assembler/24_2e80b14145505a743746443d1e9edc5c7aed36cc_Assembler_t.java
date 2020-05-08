 package com.badlogic.dcpu;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.badlogic.dcpu.Cpu.Opcode;
 import com.badlogic.dcpu.Cpu.Register;
 
 /**
  * Programmatic assembler for <a href="http://0x10c.com/doc/dcpu-16.txt">dcpu-16</a>. To
  * be used by high level language compilers.
  * @author mzechner
  *
  */
 public class Assembler {
 	ShortArray mem = new ShortArray();
 	Map<String, Label> labels = new HashMap<String, Label>();
 	
 	/**
 	 * @return the binary data for the opcodes assembled so far.
 	 */
 	public short[] getDump() {
 		patchLabels();
 		short[] dump = new short[mem.size];
 		System.arraycopy(mem.elements, 0, dump, 0, mem.size());
 		return dump;
 	}
 	
 	private void patchLabels() {
 		for(Label label: labels.values()) {
 			for(int i = 0; i < label.addresses.size; i++) {
 				mem.set(label.addresses.get(i), (short)label.targetAddress);
 			}
 		}
 	}
 	
 	/**
 	 * Writes the opcode plus its arguments. Extended
 	 * opcodes should be written with {@link #eop(Opcode, Arg)}.
 	 * 
 	 * @param op the {@link Opcode}
 	 * @param a argument a
 	 * @param b argument b
 	 */
 	void op(Opcode op, Arg a, Arg b) {
 		if(op.code != 0) {
 			int v = (b.bits << 10) | (a.bits << 4) | op.code;
 			mem.add((short)v);
 			a.writeNextWord(mem);
 			b.writeNextWord(mem);
 		} else {
 			throw new RuntimeException("Use Assembler#eop() for extended Opcodes " + op);
 		}
 	}
 	/**
 	 * Writes the extended opcode plus its arguments. Non-extended
 	 * opcodes should be written with {@link #op(Opcode, Arg, Arg)}.
 	 * 
 	 * @param op the {@link Opcode}
 	 * @param a argument a
 	 */
 	void eop(Opcode eop, Arg a) {
 		if(eop.code == 0) {
 			int v = (a.bits << 10) | (eop.extended << 4);
 			mem.add((short)v);
 			a.writeNextWord(mem);
 		} else {
 			throw new RuntimeException("Use Assembler#op() for non-extended Opcodes like " + eop);
 		}
 	}
 	
 	/**
 	 * Writes the given value to the next memory location-
 	 * @param val
 	 */
 	public void val(short val) {
 		mem.add(val);
 	}
 	
 	/**
 	 * Creates a label to be used with {@link #op(Opcode, Arg, Arg)} which
 	 * is pack patched in {@link #getDump()}.
 	 * @param name
 	 * @return
 	 */
 	public Label label(String name) {
 		Label label = labels.get(name);
 		if(label == null) {
 			label = new Label(0x1f, name, this);
 			labels.put(name, label);
 		}
 		return label;
 	}
 	
 	/**
 	 * Marks the location of a label.
 	 * @param name the name of the label.
 	 */
 	public Label markLabel(String name) {
 		Label label = labels.get(name);
 		if(label == null) {
 			label = new Label(0x1f, name, this);
 			labels.put(name, label);
 		}
 		label.targetAddress = mem.size;
 		return label;
 	}
 	
 	public static class Label extends Arg {
 		final Assembler assembler;
 		final ShortArray addresses = new ShortArray();
 		int targetAddress = 0;
 		final String name;
 		
 		Label (int bits, String name, Assembler assembler) {
 			super(bits);
 			this.name = name;
 			this.assembler = assembler;
 		}
 		
 		@Override
 		void writeNextWord (ShortArray array) {
 			addresses.add((short)(array.size));
 			array.add((short)0xdead);
 		}
 	}
 	
 	public static class Arg {
 		protected int bits = 0;
 		protected int nextWord = 0;
 		protected boolean hasNextWord;
 		
 		Arg(int bits) {
 			this.bits = bits;
 		}
 		
 		Arg(int bits, int nextWord) {
 			this.bits = bits;
 			this.nextWord = nextWord & 0xffff;
 			this.hasNextWord = true;
 		}
 		
 		int getBits() {
 			return bits;
 		}
 		
 		void writeNextWord(ShortArray array) {
 			if(hasNextWord) array.add((short)nextWord);
 		}
 		
 		/** register **/
 		public static Arg reg(Cpu.Register reg) {
 			return new Arg(reg.index);
 		}
 		
 		/** pop **/
 		public static Arg pop() {
 			return new Arg(0x18);
 		}
 		
 		/** peek **/
 		public static Arg peek() {
 			return new Arg(0x19);
 		}
 		
 		/** push **/
 		public static Arg push() {
 			return new Arg(0x1a);
 		}
 		
 		/** sp **/
 		public static Arg sp() {
 			return new Arg(0x1b);
 		}
 		
 		/** pc **/
 		public static Arg pc() {
 			return new Arg(0x1c);
 		}
 		
 		/** o **/
 		public static Arg o() {
 			return new Arg(0x1d);
 		}
 		
 		/** [register] **/
 		public static Arg mem(Cpu.Register reg) {
 			return new Arg(0x8 + reg.index);
 		}
 		
 		/** [next word + register] **/
 		public static Arg mem(Cpu.Register reg, int nextWord) {
 			return new Arg(0x10 + reg.index, nextWord);
 		}
 		
 		/** [address] **/
 		public static Arg mem(int address) {
 			return new Arg(0x1e, address);
 		}
 		
 		/** literal (if > 0x1f then its stored in the next word) **/
 		public static Arg lit(int literal) {
 			if(literal >= 0 && literal <= 0x1f) return new Arg(0x20 + literal);
 			else return new Arg(0x1f, literal);
 		}
 	}
 	
 	public static void main (String[] args) {
 		Assembler asm = new Assembler();
 		asm.op(Opcode.SET, Arg.reg(Register.A), Arg.lit(0x30));
 		asm.op(Opcode.SET, Arg.mem(0x1000), Arg.lit(0x20));
 		asm.op(Opcode.SUB, Arg.reg(Register.A), Arg.mem(0x1000));
 		asm.op(Opcode.IFN, Arg.reg(Register.A), Arg.lit(0x10));
 		asm.op(Opcode.SET, Arg.pc(), asm.label("crash"));
 		
 		asm.markLabel("loop");
 		asm.op(Opcode.SET, Arg.reg(Register.I), Arg.lit(10));
 		asm.op(Opcode.SET, Arg.reg(Register.A), Arg.lit(0x2000));
 		asm.op(Opcode.SET, Arg.mem(Register.I, 0x2000), Arg.mem(Register.A));
 		asm.op(Opcode.SUB, Arg.reg(Register.I), Arg.lit(1));
 		asm.op(Opcode.IFN, Arg.reg(Register.I), Arg.lit(0));
 		asm.op(Opcode.SET, Arg.pc(), asm.label("loop"));
 		
 		asm.op(Opcode.SET, Arg.reg(Register.X), Arg.lit(0x4));
 		asm.eop(Opcode.JSR, asm.label("testsub"));
 		asm.op(Opcode.SET, Arg.pc(), asm.label("crash"));
 		
 		asm.markLabel("testsub");
 		asm.op(Opcode.SHL, Arg.reg(Register.X), Arg.lit(0x4));
 		asm.op(Opcode.SET, Arg.pc(), Arg.pop());
 
 		asm.markLabel("crash");
 		asm.op(Opcode.SET, Arg.pc(), asm.label("crash"));
 		
 		short[] dump = asm.getDump();
		System.out.println(Disassembler.disassemble(dump, 0, dump.length));
 	}
 }
