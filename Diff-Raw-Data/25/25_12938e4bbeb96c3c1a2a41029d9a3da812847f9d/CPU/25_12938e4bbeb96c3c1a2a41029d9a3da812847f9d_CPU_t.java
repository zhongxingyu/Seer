 package no.jckf.gameboy;
 
 import java.lang.reflect.InvocationTargetException;
 
 import static no.jckf.gameboy.Utils.*;
 
 public class CPU {
 	// Constants.
 	private static final int Z = 0x80;
 	private static final int N = 0x40;
 	private static final int H = 0x20;
 	private static final int C = 0x10;
 
 	// Other components.
 	private MMU mmu;
 
 	// System variables.
 	private boolean halted = false;
 	private int cycles = 0;
 
 	// Registers.
 	private int a = 1;
 	private int f = Z | H | C;
 	private int b = 0;
 	private int c = 0x13;
 	private int d = 0;
 	private int e = 0xD8;
 	private int h = 1;
 	private int l = 0x4D;
 
 	// Pointers.
 	private int sp = 0xFFFE;
 	private int pc = 0x0100;
 
 	// Flag short-hand methods.
 	private boolean z() { return (f & Z) != 0; }
 	private boolean n() { return (f & N) != 0; }
 	private boolean h() { return (f & H) != 0; }
 	private boolean c() { return (f & C) != 0; }
 
 	private void z(boolean state) { if (state) { f |= Z; } else { f &= ~Z; } }
 	private void n(boolean state) { if (state) { f |= N; } else { f &= ~N; } }
 	private void h(boolean state) { if (state) { f |= H; } else { f &= ~H; } }
 	private void c(boolean state) { if (state) { f |= C; } else { f &= ~C; } }
 
 	// Register short-hand methods.
 	private int bc() { return ((c & 0xFF) << 8) | (b & 0xFF); }
 	private int hl() { return ((l & 0xFF) << 8) | (h & 0xFF); }
 
 	private void bc(int data) { c = data >>> 8; b = data & 0xFF; }
 	private void hl(int data) { l = data >>> 8; h = data & 0xFF; }
 
 	public CPU() {
 		mmu = MMU.getInstance();
 
 		// Initial values for various IO ports.
 		mmu.writeByte(0xFF10,0x80);
 		mmu.writeByte(0xFF11,0xBF);
 		mmu.writeByte(0xFF12,0xF3);
 		mmu.writeByte(0xFF14,0xBF);
 		mmu.writeByte(0xFF16,0x3F);
 		mmu.writeByte(0xFF19,0xBF);
 		mmu.writeByte(0xFF1A,0x7F);
 		mmu.writeByte(0xFF1B,0xFF);
 		mmu.writeByte(0xFF1C,0x9F);
 		mmu.writeByte(0xFF1E,0xBF);
 		mmu.writeByte(0xFF20,0xFF);
 		mmu.writeByte(0xFF23,0xBF);
 		mmu.writeByte(0xFF24,0x77);
 		mmu.writeByte(0xFF25,0xF3);
 		mmu.writeByte(0xFF26,0xF1);
 		mmu.writeByte(0xFF40,0x91);
 		mmu.writeByte(0xFF42,0x91);
 		mmu.writeByte(0xFF43,0x91);
 		mmu.writeByte(0xFF44,0x91); // LCD y. Should really be 0x00, but that will get us stuck in a loop scrolling the Nintendo logo, as we have no GPU yet.
 		mmu.writeByte(0xFF45,0x91);
 		mmu.writeByte(0xFF47,0xFC);
 		mmu.writeByte(0xFF48,0xFF);
 		mmu.writeByte(0xFF49,0xFF);
 	}
 
 	public boolean cycle() {
 		if (cycles != 0) {
 			cycles--;
 			return true;
 		}
 
		if (halted) return true;

 		int opcode = mmu.readByte(pc++) & 0xFF;
 
		println(
			hexw(pc - 1) + " " +
			hexb(opcode) + " " +
 			(z() ? "Z" : "-") +
 			(n() ? "N" : "-") +
 			(h() ? "H" : "-") +
 			(c() ? "C" : "-")
 		);
 
 		// Try to invoke the method for this operation.
		// Print a stack trace and stop execution if something goes wrong.
 		try {
 			getClass().getDeclaredMethod("_" + hexb(opcode)).invoke(this);
 		} catch (NoSuchMethodException exception) {
 			exception.printStackTrace();
			return false;
 		} catch (IllegalAccessException exception) {
 			exception.printStackTrace();
			return false;
 		} catch (InvocationTargetException exception) {
 			exception.printStackTrace();
			return false;
 		}
 
 		// Clamp. Overhead?
 		a &= 0xFF;
 		f &= 0xFF;
 		b &= 0xFF;
 		c &= 0xFF;
 		d &= 0xFF;
 		e &= 0xFF;
 		h &= 0xFF;
 		l &= 0xFF;
 		sp &= 0xFFFF;
 		pc &= 0xFFFF;
 
 		return true;
 	}
 
 	// NOP = No operation.
 	private void _0x00() {
 		cycles += 4;
 	}
 
 	// LD bc d16 = Copy the following 2 bytes into the b and c registers.
 	private void _0x01() {
 		cycles += 12;
 
 		c = mmu.readByte(pc++);
 		b = mmu.readByte(pc++);
 	}
 
 	// DEC bc = Decrement 16 bit bc register.
 	private void _0x0B() {
 		cycles += 8;
 
 		int _bc = bc() - 1;
 		bc(_bc);
 
 		if (_bc <= 0) {
 			z(true);
 		} else {
 			z(false);
 		}
 	}
 
 	// JR r8 = Jump relative to current PC as defined by the following byte.
 	private void _0x18() {
 		cycles += 12;
 
 		pc += mmu.readByte(pc) + 1;
 	}
 
 	// JR nz r8 = Jump relative to current PC as defined by the following byte if zero flag isn't set.
 	private void _0x20() {
 		cycles += 8;
 
 		if (!z()) {
 			cycles += 4;
 			pc += mmu.readByte(pc);
 		}
 
 		pc++;
 	}
 
 	// LD hl d16 = Copy the following two bytes (word) into high and low registers (16 bit hl register).
 	private void _0x21() {
 		cycles += 12;
 
 		l = mmu.readByte(pc++);
 		h = mmu.readByte(pc++);
 	}
 
 	// INC hl = Increment the 16 bit hl register.
 	private void _0x23() {
 		cycles += 8;
 
 		hl(hl() + 1);
 	}
 
 	// JR z r8 = Jump relative to current PC as defined by next byte if zero flag is set.
 	private void _0x28() {
 		cycles += 8;
 
 		if (z()) {
 			cycles += 4;
 			pc += mmu.readByte(pc);
 		}
 
 		pc++;
 	}
 
 	// LD sp d16 = Copy the following word into the stack pointer.
 	private void _0x31() {
 		cycles += 12;
 
 		sp = mmu.readWord(pc);
 		pc += 2;
 	}
 
 	// LD (hl) d8 = Copy the following byte into the address defined by the high and low registers.
 	private void _0x36() {
 		cycles += 12;
 
 		mmu.writeByte(hl(),mmu.readByte(pc++));
 	}
 
 	// LD a d8 = Copy the following byte into the accumulator.
 	private void _0x3E() {
 		cycles += 8;
 
 		a = mmu.readByte(pc++);
 	}
 
 	// LD b a = Copy the accumulator's value into the b register.
 	private void _0x47() {
 		cycles += 4;
 
 		b = a;
 	}
 
 	// HALT = Stop execution.
 	private void _0x76() {
 		cycles += 4;
 
 		halted = true;
 	}
 
 	// LD a b = Copy register b's value into the accumulator.
 	private void _0x78() {
 		cycles += 4;
 
 		a = b;
 	}
 
 	// XOR a = "Exclusive or" accumulator against itself, effectively zeroing it out.
 	private void _0xAF() {
 		cycles = 4;
 
 		a = 0;
 		z(true);
 	}
 
 	// OR c = "Or" register c against the accumulator.
 	private void _0xB1() {
 		cycles += 4;
 
 		a |= c;
 
 		if (a == 0) {
 			f = Z; // Set the zero flag, unset all others.
 		} else {
 			f = 0; // Unset the zero flag, and all others.
 		}
 	}
 
 	// JP a16 = Jump to absolute address defined in the following word.
 	private void _0xC3() {
 		cycles += 16;
 
 		pc = mmu.readWord(pc);
 	}
 
 	// 0xCB prefixed opcodes.
 	private void _0xCB() {
 		cycles += 4;
 
 		int opcode = mmu.readByte(pc++) & 0xFF;
 
 		println(hexw(pc - 1) + " 0xCB " + hexb(opcode));
 
 		switch (opcode) {
 			case 0x87: _0xCB87(); break;
 			default: _0x76(); break;
 		}
 	}
 
 	// RES 0 a = Reset bit 0 of accumulator.
 	private void _0xCB87() {
 		// Is it 8 including the 4 from 0xCB?
 		cycles += 8;
 
 		a &= 0xFE;
 	}
 
 	// CALL a16 = Move down the stack and jump to address defined in the following word.
 	private void _0xCD() {
 		cycles += 24;
 
 		sp -= 2; // Move back to make room for PC.
 		mmu.writeWord(sp,pc + 2); // Copy old PC to stack.
 		pc = mmu.readWord(pc); // Jump to wherever.
 	}
 
 	// LDH (a8) a = Copy accumulator value to IO port defined in the following byte.
 	private void _0xE0() {
 		cycles += 12;
 
 		mmu.writeByte(0xFF00 + mmu.readByte(pc++),a);
 	}
 
 	// LD a16 a = Copy accumulator value into address defined in the following word.
 	private void _0xEA() {
 		cycles = 16;
 
 		mmu.writeByte(mmu.readWord(pc),a);
 		pc += 2;
 	}
 
 	// LDH a (a8) = Copy from given IO port into accumulator.
 	private void _0xF0() {
 		cycles += 12;
 
 		a = mmu.readByte(0xFF00 + mmu.readByte(pc++));
 	}
 
 	// DI = Disable interrupts.
 	private void _0xF3() {
 		cycles += 4;
 	}
 
 	// CP d8 = Compare the following byte to accumulator.
 	private void _0xFE() {
 		cycles += 8;
 
 		int b = mmu.readByte(pc++) & 0xFF;
 		if (b == a) { z(true); } else { z(false); }
 		n(true);
 	}
 }
