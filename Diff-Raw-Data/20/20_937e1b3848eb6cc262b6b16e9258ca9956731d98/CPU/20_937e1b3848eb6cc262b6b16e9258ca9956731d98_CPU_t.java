 package com.tnes;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class CPU {
 	private MMC mmc;
 	private CpuTables cpuTables = CpuTables.getInstance();
 	private Debugger debugger = Debugger.getInstance();
 
 	private short sp;
 	private int pc;
 	private short flags;
 	private short a;
 	private short x;
 	private short y;
 	private boolean pageBoundaryCrossed = false;
 
 	// Debug stuff
 	private boolean debugRead = false;
 	private boolean step = false;
 	private boolean logCPUState = false;
 	private List<Integer> breakpoints = new ArrayList<Integer>();
 
 	public CPU(MMC mmc) {
 		this.mmc = mmc;
 
 		// CPU is not responsible for initializing the stack
 		sp = (short) 0xFF;
 
 		// Start PC at reset vector
 		pc = (((int) mmc.readCPUMem(Constants.RESET_HI)) << 8) + mmc.readCPUMem(Constants.RESET_LO);
 
 		// Initialize registers
 		a = 0;
 		x = 0;
 		y = 0;
 		// All but the unused flag are cleared to 0
 		flags = (short) 0x20;
 
 		// Debug stuff
 		breakpoints = new ArrayList<Integer>();
 		step = false;
 		logCPUState = false;
 		debugRead = false;
 
 		addDebugCommands();
 	}
 
 	/*
 	 * Getters and Setters
 	 */
 	public MMC getMMC() {
 		return mmc;
 	}
 
 	public void setMMC(MMC mmc) {
 		this.mmc = mmc;
 	}
 
 	public short getSP() {
 		return sp;
 	}
 
 	public void setSP(short sp) {
 		this.sp = sp;
 	}
 
 	public int getPC() {
 		return pc;
 	}
 
 	public void setPC(int pc) {
 		this.pc = pc;
 	}
 
 	public short getFlags() {
 		return flags;
 	}
 
 	public void setFlags(short flags) {
 		this.flags = flags;
 	}
 
 	public short getA() {
 		return a;
 	}
 
 	public void setA(short a) {
 		this.a = a;
 	}
 
 	public short getX() {
 		return x;
 	}
 
 	public void setX(short x) {
 		this.x = x;
 	}
 
 	public short getY() {
 		return y;
 	}
 
 	public void setY(short y) {
 		this.y = y;
 	}
 
 	public boolean isPageBoundaryCrossed() {
 		return pageBoundaryCrossed;
 	}
 
 	// Status Flag Methods
 	// 7 = Sign flag, 6 = Overflow flag, 4 = Break flag, 3 = Decimal mode flag
 	// 2 = Interrupt disable flag, 1 = Zero flag, 0 = Carry flag
 	public boolean isSignFlagSet() {
 		return (flags & Constants.CPU_STAT_NEGATIVE) != 0 ? true : false;
 	}
 
 	public void setSignFlag(boolean val) {
 		if (val)
 			flags |= Constants.CPU_STAT_NEGATIVE;
 		else
 			flags &= ~Constants.CPU_STAT_NEGATIVE;
 	}
 
 	public boolean isOverflowFlagSet() {
 		return (flags & Constants.CPU_STAT_OVERFLOW) != 0 ? true : false;
 	}
 
 	public void setOverflowFlag(boolean val) {
 		if (val)
 			flags |= Constants.CPU_STAT_OVERFLOW;
 		else
 			flags &= ~Constants.CPU_STAT_OVERFLOW;
 	}
 
 	public boolean isBreakFlagSet() {
 		return (flags & Constants.CPU_STAT_BREAK) != 0 ? true : false;
 	}
 
 	public void setBreakFlag(boolean val) {
 		if (val)
 			flags |= Constants.CPU_STAT_BREAK;
 		else
 			flags &= ~Constants.CPU_STAT_BREAK;
 	}
 
 	public boolean isDecimalFlagSet() {
 		return (flags & Constants.CPU_STAT_DECIMAL) != 0 ? true : false;
 	}
 
 	public void setDecimalFlag(boolean val) {
 		if (val)
 			flags |= Constants.CPU_STAT_DECIMAL;
 		else
 			flags &= ~Constants.CPU_STAT_DECIMAL;
 	}
 
 	public boolean isInterruptFlagSet() {
 		return (flags & Constants.CPU_STAT_INTERRUPT_DISABLE) != 0 ? true : false;
 	}
 
 	public void setInterruptFlag(boolean val) {
 		if (val)
 			flags |= Constants.CPU_STAT_INTERRUPT_DISABLE;
 		else
 			flags &= ~Constants.CPU_STAT_INTERRUPT_DISABLE;
 	}
 
 	public boolean isZeroFlagSet() {
 		return (flags & Constants.CPU_STAT_ZERO) != 0 ? true : false;
 	}
 
 	public void setZeroFlag(boolean val) {
 		if (val)
 			flags |= Constants.CPU_STAT_ZERO;
 		else
 			flags &= ~Constants.CPU_STAT_ZERO;
 	}
 
 	public boolean isCarryFlagSet() {
 		return (flags & Constants.CPU_STAT_CARRY) != 0 ? true : false;
 	}
 
 	public void setCarryFlag(boolean val) {
 		if (val)
 			flags |= Constants.CPU_STAT_CARRY;
 		else
 			flags &= ~Constants.CPU_STAT_CARRY;
 	}
 
 	/*
 	 * CPU Logic
 	 */
 	public void reset() {
 		// Reset - load PC with with appropriate location from reset vector
 		pc = (((int) mmc.readCPUMem(Constants.RESET_HI)) << 8) | mmc.readCPUMem(Constants.RESET_LO);
 
 		if (logCPUState)
 			debugger.debugLog("\nReset.");
 	}
 
 	public void NMI() {
 		/*
 		 * Non-Maskable interrupt - Load the PC with the appropraite location
 		 * from the NMI vector First prepare the CPU
 		 */
 		// Push program counter on stack, high short first
 		push((short) ((pc >> 8) & 0xFF));
 		push((short) (pc & 0xFF));
 
 		// Push status register on the stack
 		push(flags);
 
 		int hi = ((int) mmc.readCPUMem(Constants.NMIB_HI)) << 8;
 		pc = hi | mmc.readCPUMem(Constants.NMIB_LO);
 
 		if (logCPUState)
 			debugger.debugLog("\nNMI.");
 	}
 
 	public int execute() {
 		// Execute a single instruction and return cycle count
 		int cycleCount = 0;
 		short opcode = mmc.readCPUMem(pc);
 		Operation operation = cpuTables.Operations.get(opcode);
 		AddressingMode addressingMode = cpuTables.AdressingModes.get(opcode);
 		int address = getInstructionAddress(operation, addressingMode);
 
 		/*
 		 * Note!!! - We are only calculating this for the addressing modes for
 		 * which it actually matters in terms of cycle counting, namely AbsX,
 		 * AbsY, IndY and Rel
 		 */
 		pageBoundaryCrossed = false;
 		int cycleOffset = 0;
 
 		// Handle debugging stuff
 		if (step || breakpoints.contains(pc)) {
 			// Write out CPU state to screen or log
 			debugRead = true;
 			short debugData = getInstructionData(operation, addressingMode);
 
 			if (!step)
 				debugger.debugPrint("\nBreakpoint hit.");
 
 			debugger.execCommand("getCPUState", String.format("%d,%d", address, debugData));
 			debugger.readCommands();
 		}
 
 		// Perform logging if enabled
 		if (logCPUState) {
 			debugRead = true;
 			short debugData = getInstructionData(operation, addressingMode);
 			debugger.execCommand("logCPUState", String.format("%d,%d", address, debugData));
 		}
 
 		// Disable debug reading (reads will affect registers, etc.)
 		debugRead = false;
 
 		short data = 0;
 		short temp = 0;
 		switch (operation) {
 		case ADC:
 			data = getInstructionData(operation, addressingMode);
 			temp = (short) (data + a + (isCarryFlagSet() ? 1 : 0));
 			calcZeroFlag(temp);
 
 			/*
 			 * Note, most of the following logic comes directly from the VICE
 			 * emulator. Need to understand it better.
 			 */
 			if (isDecimalFlagSet()) {
 				if (((a & 0xF) + (temp & 0xF) + (isCarryFlagSet() ? 1 : 0)) > 9) {
 					temp += 6;
 				}
 
 				calcSignFlag(temp);
 				setOverflowFlag((((a ^ data) & 0x80) == 0) && (((a ^ temp) & 0x80) != 0));
 
 				if (temp > 0x99) {
 					temp += 96;
 				}
 
 				setCarryFlag(temp > 0x99);
 			} else {
 				calcSignFlag(temp);
 				setOverflowFlag((((a ^ data) & 0x80) == 0) && (((a ^ temp) & 0x80) != 0));
 				setCarryFlag(temp > 0xFF);
 			}
 
 			a = (short) (temp & 0xFF);
 			pc += cpuTables.ByteCounts.get(operation).get(addressingMode);
 			cycleOffset = pageBoundaryCrossed ? 1 : 0;
 			break;
 
 		case AND:
 			data = getInstructionData(operation, addressingMode);
 			temp = (short) (data & a);
 			calcZeroFlag(temp);
 			calcSignFlag(temp);
 			a = temp;
 			pc += cpuTables.ByteCounts.get(operation).get(addressingMode);
 
 			cycleOffset = pageBoundaryCrossed ? 1 : 0;
 			break;
 
 		case ASL:
 			data = getInstructionData(operation, addressingMode);
 			setCarryFlag((data & 0x80) != 0);
 			data <<= 1;
 			calcZeroFlag(data);
 			calcSignFlag(data);
 			if (addressingMode == AddressingMode.ACCUMULATOR) {
 				a = data;
 			} else {
 				mmc.writeCPUMem(address, data);
 			}
 			pc += cpuTables.ByteCounts.get(operation).get(addressingMode);
 
 			break;
 
 		case BCC:
 			if (!isCarryFlagSet()) {
 				pc = address;
 				cycleOffset = pageBoundaryCrossed ? 2 : 1;
 			}
 			pc += cpuTables.ByteCounts.get(operation).get(addressingMode);
 			break;
 
 		case BCS:
 			if (isCarryFlagSet()) {
 				pc = address;
 				cycleOffset = pageBoundaryCrossed ? 2 : 1;
 			}
 			pc += cpuTables.ByteCounts.get(operation).get(addressingMode);
 			break;
 
 		case BEQ:
 			if (isZeroFlagSet()) {
 				pc = address;
 				cycleOffset = pageBoundaryCrossed ? 2 : 1;
 			}
 			pc += cpuTables.ByteCounts.get(operation).get(addressingMode);
 			break;
 
 		case BIT:
 			data = getInstructionData(operation, addressingMode);
 
 			calcSignFlag(data);
 			setOverflowFlag((data & 0x40) != 0);
 			calcZeroFlag((short) (data & a));
 			pc += cpuTables.ByteCounts.get(operation).get(addressingMode);
 
 			break;
 
 		case BMI:
 			if (isSignFlagSet()) {
 				pc = address;
 				cycleOffset = pageBoundaryCrossed ? 2 : 1;
 			}
 
 			pc += cpuTables.ByteCounts.get(operation).get(addressingMode);
 			break;
 
 		case BNE:
 			if (!isZeroFlagSet()) {
 				pc = address;
 				cycleOffset = pageBoundaryCrossed ? 2 : 1;
 			}
 
 			pc += cpuTables.ByteCounts.get(operation).get(addressingMode);
 			break;
 
 		case BPL:
 			if (!isSignFlagSet()) {
 				pc = address;
 				cycleOffset = pageBoundaryCrossed ? 2 : 1;
 			}
 
 			pc += cpuTables.ByteCounts.get(operation).get(addressingMode);
 			break;
 
 		case BRK:
 			pc += 1;
 			// Push program counter on stack, high byte first
 			push((short) ((pc >> 8) & 0xFF));
 			push((short) (pc & 0xFF));
 
 			// Set the break flag then push the status register on the stack
 			setBreakFlag(true);
 			push(flags);
 
 			address = (mmc.readCPUMem(Constants.IRQ_BRK_HI) << 8) + mmc.readCPUMem(Constants.IRQ_BRK_LO);
 			pc = address;
 
 			// Lastly set the interrupt disable flag
 			setInterruptFlag(true);
 			break;
 
 		case BVC:
 			if (!isOverflowFlagSet()) {
 				pc = address;
 				cycleOffset = pageBoundaryCrossed ? 2 : 1;
 			}
 
 			pc += cpuTables.ByteCounts.get(operation).get(addressingMode);
 			break;
 
 		case BVS:
 			if (isOverflowFlagSet()) {
 				pc = address;
 				cycleOffset = pageBoundaryCrossed ? 2 : 1;
 			}
 
 			pc += cpuTables.ByteCounts.get(operation).get(addressingMode);
 			break;
 
 		case CLC:
 			setCarryFlag(false);
 			pc += cpuTables.ByteCounts.get(operation).get(addressingMode);
 			break;
 
 		case CLD:
 			setDecimalFlag(false);
 			pc += cpuTables.ByteCounts.get(operation).get(addressingMode);
 			break;
 
 		case CLI:
 			setInterruptFlag(false);
 			pc += cpuTables.ByteCounts.get(operation).get(addressingMode);
 			break;
 
 		case CLV:
 			setOverflowFlag(false);
 			pc += cpuTables.ByteCounts.get(operation).get(addressingMode);
 			break;
 
 		case CMP:
 			data = getInstructionData(operation, addressingMode);
 
 			temp = (short) (a - data);
 			setCarryFlag(temp < 0x100);
 			calcSignFlag(temp);
 			calcZeroFlag(temp);
 
 			pc += cpuTables.ByteCounts.get(operation).get(addressingMode);
 
 			cycleOffset = pageBoundaryCrossed ? 1 : 0;
 			break;
 
 		case CPX:
 			data = getInstructionData(operation, addressingMode);
 
 			temp = (short) (x - data);
 			setCarryFlag(temp < 0x100);
 			calcSignFlag(temp);
 			calcZeroFlag(temp);
 			pc += cpuTables.ByteCounts.get(operation).get(addressingMode);
 			break;
 
 		case CPY:
 			data = getInstructionData(operation, addressingMode);
 
 			temp = (short) (y - data);
 			setCarryFlag(temp < 0x100);
 			calcSignFlag(temp);
 			calcZeroFlag(temp);
 			pc += cpuTables.ByteCounts.get(operation).get(addressingMode);
 			break;
 
 		case DEC:
 			data = getInstructionData(operation, addressingMode);
 
 			data = (short) ((data - 1) & 0xFF);
 			calcSignFlag(data);
 			calcZeroFlag(data);
 			mmc.writeCPUMem(address, data);
 			pc += cpuTables.ByteCounts.get(operation).get(addressingMode);
 			break;
 
 		case DEX:
 			x = (short) ((x - 1) & 0xFF);
 			calcSignFlag(x);
 			calcZeroFlag(x);
 			pc += cpuTables.ByteCounts.get(operation).get(addressingMode);
 			break;
 
 		case DEY:
 			y = (short) ((y - 1) & 0xFF);
 			calcSignFlag(y);
 			calcZeroFlag(y);
 			pc += cpuTables.ByteCounts.get(operation).get(addressingMode);
 			break;
 
 		case EOR:
 			data = getInstructionData(operation, addressingMode);
 
 			temp = (short) (data ^ a);
 			calcZeroFlag(temp);
 			calcSignFlag(temp);
 			a = temp;
 			pc += cpuTables.ByteCounts.get(operation).get(addressingMode);
 
 			cycleOffset = pageBoundaryCrossed ? 1 : 0;
 			break;
 
 		case INC:
 			data = getInstructionData(operation, addressingMode);
 
 			data = (short) ((data + 1) & 0xFF);
 			calcSignFlag(data);
 			calcZeroFlag(data);
 			mmc.writeCPUMem(address, data);
 			pc += cpuTables.ByteCounts.get(operation).get(addressingMode);
 			break;
 
 		case INX:
 			x = (short) ((x + 1) & 0xFF);
 			calcSignFlag(x);
 			calcZeroFlag(x);
 			pc += cpuTables.ByteCounts.get(operation).get(addressingMode);
 			break;
 
 		case INY:
 			y = (short) ((y + 1) & 0xFF);
 			calcSignFlag(y);
 			calcZeroFlag(y);
 			pc += cpuTables.ByteCounts.get(operation).get(addressingMode);
 			break;
 
 		case JMP:
 			pc = address;
 			break;
 
 		case JSR:
 			pc += 2;
 			// Push the high byte first
 			push((short) ((pc >> 8) & 0xFF));
 			push((short) (pc & 0xFF));
 			pc = address;
 			break;
 
 		case LDA:
 			data = getInstructionData(operation, addressingMode);
 
 			calcSignFlag(data);
 			calcZeroFlag(data);
 			a = data;
 			pc += cpuTables.ByteCounts.get(operation).get(addressingMode);
 
 			cycleOffset = pageBoundaryCrossed ? 1 : 0;
 			break;
 
 		case LDX:
 			data = getInstructionData(operation, addressingMode);
 
 			calcSignFlag(data);
 			calcZeroFlag(data);
 			x = data;
 			pc += cpuTables.ByteCounts.get(operation).get(addressingMode);
 
 			cycleOffset = pageBoundaryCrossed ? 1 : 0;
 			break;
 
 		case LDY:
 			data = getInstructionData(operation, addressingMode);
 
 			calcSignFlag(data);
 			calcZeroFlag(data);
 			y = data;
 			pc += cpuTables.ByteCounts.get(operation).get(addressingMode);
 
 			cycleOffset = pageBoundaryCrossed ? 1 : 0;
 			break;
 
 		case LSR:
 			data = getInstructionData(operation, addressingMode);
 
 			setCarryFlag((data & 0x01) != 0);
 			data >>= 1;
 			// Reduce to 1-byte data
 			data &= 0xFF;
 			calcZeroFlag(data);
 			setSignFlag(false);
 			if (addressingMode == AddressingMode.ACCUMULATOR) {
 				a = data;
 			} else {
 				mmc.writeCPUMem(address, data);
 			}
 			pc += cpuTables.ByteCounts.get(operation).get(addressingMode);
 			break;
 
 		case NOP:
 			pc += cpuTables.ByteCounts.get(operation).get(addressingMode);
 			break;
 
 		case ORA:
 			data = getInstructionData(operation, addressingMode);
 
 			temp = (short) (data | a);
 			calcZeroFlag(temp);
 			calcSignFlag(temp);
 			a = temp;
 			pc += cpuTables.ByteCounts.get(operation).get(addressingMode);
 
 			cycleOffset = pageBoundaryCrossed ? 1 : 0;
 			break;
 
 		case PHA:
 			push(a);
 			pc += cpuTables.ByteCounts.get(operation).get(addressingMode);
 			break;
 
 		case PHP:
 			push(flags);
 			pc += cpuTables.ByteCounts.get(operation).get(addressingMode);
 			break;
 
 		case PLA:
 			a = pop();
 			/*
 			 * Note: VICE sets these flags, but that functionality isn't in the
 			 * documentation. What's the right thing to do? Do it their way for
 			 * now.
 			 */
 			calcSignFlag(a);
 			calcZeroFlag(a);
 			pc += cpuTables.ByteCounts.get(operation).get(addressingMode);
 			break;
 
 		case PLP:
 			flags = pop();
 			pc += cpuTables.ByteCounts.get(operation).get(addressingMode);
 			break;
 
 		case ROL:
 			data = getInstructionData(operation, addressingMode);
 
 			boolean carry = (data & 0x80) != 0 ? true : false;
 			data <<= 1;
 			data &= 0xFF;
 
 			if (isCarryFlagSet())
 				data |= 0x01;
 
 			setCarryFlag(carry);
 			calcZeroFlag(data);
 			calcSignFlag(data);
 
 			if (addressingMode == AddressingMode.ACCUMULATOR)
 				a = data;
 			else
 				mmc.writeCPUMem(address, data);
 
 			pc += cpuTables.ByteCounts.get(operation).get(addressingMode);
 			break;
 
 		case ROR:
 			data = getInstructionData(operation, addressingMode);
 
 			carry = (data & 0x01) != 0 ? true : false;
 			data >>= 1;
 			data &= 0xFF;
 
 			if (isCarryFlagSet())
 				data |= 0x80;
 
 			setCarryFlag(carry);
 			calcZeroFlag(data);
 			calcSignFlag(data);
 
 			if (addressingMode == AddressingMode.ACCUMULATOR)
 				a = data;
 			else
 				mmc.writeCPUMem(address, data);
 
 			pc += cpuTables.ByteCounts.get(operation).get(addressingMode);
 			break;
 
 		case RTI:
 			flags = pop();
 			// Pop low byte first
 			data = (short) (pop() & 0xFF);
 			data |= (pop() << 8);
 			pc = data;
 			break;
 
 		case RTS:
 			// Pop low byte first
 			data = (short) (pop() & 0xFF);
 			data |= (pop() << 8);
 			pc = data + 1;
 			break;
 
 		case SBC:
 			data = getInstructionData(operation, addressingMode);
 
 			temp = (short) (a - data - (isCarryFlagSet() ? 1 : 0));
 			calcZeroFlag(temp);
 			calcSignFlag(temp);
 
 			/*
 			 * Note, most of the following logic comes directly from the VICE
 			 * emulator. Need to understand it better.
 			 */
 			setOverflowFlag((((a ^ data) & 0x80) != 0) && (((a ^ temp) & 0x80) != 0));
 
 			if (isDecimalFlagSet()) {
 				if (((a & 0x0F) - (isCarryFlagSet() ? 0 : 1)) < (data & 0x0F))
 					temp -= 6;
 
 				if (temp > 0x99)
 					temp -= 0x60;
 			}
 
 			setCarryFlag(temp < 0x100);
 
 			a = (short) (temp & 0xFF);
 			pc += cpuTables.ByteCounts.get(operation).get(addressingMode);
 
 			cycleOffset = pageBoundaryCrossed ? 1 : 0;
 			break;
 
 		case SEC:
 			setCarryFlag(true);
 			pc += cpuTables.ByteCounts.get(operation).get(addressingMode);
 			break;
 
 		case SED:
 			setDecimalFlag(true);
 			pc += cpuTables.ByteCounts.get(operation).get(addressingMode);
 			break;
 
 		case SEI:
 			setInterruptFlag(true);
 			pc += cpuTables.ByteCounts.get(operation).get(addressingMode);
 			break;
 
 		case STA:
 			mmc.writeCPUMem(address, a);
 			pc += cpuTables.ByteCounts.get(operation).get(addressingMode);
 			break;
 
 		case STX:
 			mmc.writeCPUMem(address, x);
 			pc += cpuTables.ByteCounts.get(operation).get(addressingMode);
 			break;
 
 		case STY:
 			mmc.writeCPUMem(address, y);
 			pc += cpuTables.ByteCounts.get(operation).get(addressingMode);
 			break;
 
 		case TAX:
 			calcSignFlag(a);
 			calcZeroFlag(a);
 			x = a;
 			pc += cpuTables.ByteCounts.get(operation).get(addressingMode);
 			break;
 
 		case TAY:
 			calcSignFlag(a);
 			calcZeroFlag(a);
 			y = a;
 			pc += cpuTables.ByteCounts.get(operation).get(addressingMode);
 			break;
 
 		case TSX:
 			calcSignFlag(sp);
 			calcZeroFlag(sp);
 			x = sp;
 			pc += cpuTables.ByteCounts.get(operation).get(addressingMode);
 			break;
 
 		case TXA:
 			calcSignFlag(x);
 			calcZeroFlag(x);
 			a = x;
 			pc += cpuTables.ByteCounts.get(operation).get(addressingMode);
 			break;
 
 		case TXS:
 			sp = x;
 			pc += cpuTables.ByteCounts.get(operation).get(addressingMode);
 			break;
 
 		case TYA:
 			calcSignFlag(y);
 			calcZeroFlag(y);
 			a = y;
 			pc += cpuTables.ByteCounts.get(operation).get(addressingMode);
 			break;
 
 		}
 
 		try {
 			cycleCount = cpuTables.CycleCounts.get(operation).get(addressingMode) + cycleOffset;
 		} catch (Exception e) {
 			debugger.debugPrint("\nSomething's Wrong! Invalid operation or addressing mode");
 			debugger.readCommands();
 		}
 
 		return cycleCount;
 	}
 
 	/*
 	 * Stack Operations
 	 */
 	private void push(short val) {
 		if (sp > 0) {
 			mmc.writeCPUMem(Constants.CPU_STACK_LO + sp, val);
 			sp--;
 		} else {
 			// Error occurred, stack overflow
 			debugger.debugPrint("\nStack Overflow Occurred");
 			debugger.debugLog("\nStack Overflow Occurred");
			debugger.readCommands();
 		}
 	}
 
 	private short pop() {
 		short result = (short) 0xFF;
 		if (sp < 0xFF) {
 			sp++;
 			result = mmc.readCPUMem(Constants.CPU_STACK_LO + sp);
 		} else {
 			// Error occurred, stack underflow
 			debugger.debugPrint("\nStack Underflow Occurred");
 			debugger.debugLog("\nStack Underflow Occurred");
			debugger.readCommands();
 		}
 
 		return result;
 	}
 
 	/*
 	 * Utility Methods
 	 */
 	private int getInstructionAddress(Operation operation, AddressingMode addressingMode) {
 
 		switch (addressingMode) {
 		case IMMEDIATE:
 			return getImmediateAddress();
 		case ABSOLUTE:
 			return getAbsoluteAddress(false, false);
 		case ZERO_PAGE:
 			return getZeroPageAddress(false, false);
 		case ZERO_PAGE_X_INDEXED:
 			return getZeroPageAddress(true, false);
 		case ZERO_PAGE_Y_INDEXED:
 			return getZeroPageAddress(false, true);
 		case ABSOLUTE_X_INDEXED:
 			return getAbsoluteAddress(true, false);
 		case ABSOLUTE_Y_INDEXED:
 			return getAbsoluteAddress(false, true);
 		case INDIRECT:
 			return getIndirectAddress();
 		case PRE_INDEXED_INDIRECT:
 			return getPreIndexedIndirectAddress();
 		case POST_INDEXED_INDIRECT:
 			return getPostIndexedIndirectAddress();
 		case RELATIVE:
 			return getRelativeAddress(cpuTables.ByteCounts.get(operation).get(addressingMode));
 		}
 
 		return 0;
 	}
 
 	private short getInstructionData(Operation operation, AddressingMode addressingMode) {
 		switch (addressingMode) {
 		case IMMEDIATE:
 			return getImmediateValue();
 		case ABSOLUTE:
 			return getAbsoluteValue(false, false);
 		case ZERO_PAGE:
 			return getZeroPageValue(false, false);
 		case ACCUMULATOR:
 			return a;
 		case ZERO_PAGE_X_INDEXED:
 			return getAbsoluteValue(true, false);
 		case ZERO_PAGE_Y_INDEXED:
 			return getAbsoluteValue(false, true);
 		case ABSOLUTE_X_INDEXED:
 			return getAbsoluteValue(true, false);
 		case ABSOLUTE_Y_INDEXED:
 			return getAbsoluteValue(false, true);
 		case PRE_INDEXED_INDIRECT:
 			return getPreIndexedIndirectValue();
 		case POST_INDEXED_INDIRECT:
 			return getPostIndexedIndirectValue();
 		}
 
 		return (short) 0;
 	}
 
 	private void calcSignFlag(short val) {
 		setSignFlag((val & 80) != 0);
 	}
 
 	private void calcZeroFlag(short val) {
 		setZeroFlag(val == 0);
 	}
 
 	/*
 	 * Methods to get address / data based on addressing mode
 	 */
 	private int getImmediateAddress() {
 		return pc + 1;
 	}
 
 	private short getImmediateValue() {
 		return debugRead ? mmc.readCPUMemSafe(getImmediateAddress()) : mmc.readCPUMem(getImmediateAddress());
 	}
 
 	private int getZeroPageAddress(boolean xIndexed, boolean yIndexed) {
 		int address = mmc.readCPUMem(pc + 1);
 
 		address += xIndexed ? x : 0;
 		address += yIndexed ? y : 0;
 
 		return address;
 	}
 
 	private short getZeroPageValue(boolean xIndexed, boolean yIndexed) {
 		return debugRead ? mmc.readCPUMemSafe(getZeroPageAddress(xIndexed, yIndexed)) : mmc.readCPUMem(getZeroPageAddress(xIndexed, yIndexed));
 	}
 
 	private int getAbsoluteAddress(boolean xIndexed, boolean yIndexed) {
 		// Address is stored low short first
 		int hi = ((int) (mmc.readCPUMem(pc + 2))) << 8;
 		int address = hi + mmc.readCPUMem(pc + 1);
 
 		if (xIndexed) {
 			pageBoundaryCrossed = ((address & 0xFF00) != ((address + x) & 0xFF00));
 			address += x;
 		} else if (yIndexed) {
 			pageBoundaryCrossed = ((address & 0xFF00) != ((address + y) & 0xFF00));
 			address += y;
 		}
 
 		return address;
 	}
 
 	private short getAbsoluteValue(boolean xIndexed, boolean yIndexed) {
 		return debugRead ? mmc.readCPUMemSafe(getAbsoluteAddress(xIndexed, yIndexed)) : mmc.readCPUMem(getAbsoluteAddress(xIndexed, yIndexed));
 	}
 
 	private int getIndirectAddress() {
 		// Address Location and actual address are stored low short first
 		int addressLocationHi = ((int) (mmc.readCPUMem(pc + 2))) << 8;
 		int addressLocation = addressLocationHi + mmc.readCPUMem(pc + 1);
 		int hi = ((int) mmc.readCPUMem(addressLocation + 1)) << 8;
 		return hi + mmc.readCPUMem(addressLocation);
 	}
 
 	private int getPreIndexedIndirectAddress() {
 		// Address location is added to (indexed by) the X register
 		int addressLocation = mmc.readCPUMem(pc + 1) + x;
 
 		// Address is stored low short first
 		int hi = ((int) mmc.readCPUMem(addressLocation + 1)) << 8;
 		return hi + mmc.readCPUMem(addressLocation);
 	}
 
 	private short getPreIndexedIndirectValue() {
 		return debugRead ? mmc.readCPUMemSafe(getPreIndexedIndirectAddress()) : mmc.readCPUMem(getPreIndexedIndirectAddress());
 	}
 
 	private int getPostIndexedIndirectAddress() {
 		int addressLocation = mmc.readCPUMem(pc + 1);
 
 		// Address is stored low short first, and is added to (indexed by) the Y
 		// register
 		int hi = ((int) mmc.readCPUMem(addressLocation + 1)) << 8;
 		int address = hi + mmc.readCPUMem(addressLocation);
 
 		pageBoundaryCrossed = ((address & 0xFF00) != ((address + y) & 0xFF00));
 
 		return address + y;
 	}
 
 	private short getPostIndexedIndirectValue() {
 		return debugRead ? mmc.readCPUMemSafe(getPostIndexedIndirectAddress()) : mmc.readCPUMem(getPostIndexedIndirectAddress());
 	}
 
 	private int getRelativeAddress(int operandOffset) {
 		short addressOffset = mmc.readCPUMem(pc + 1);
 		int address = 0;
 
 		// Address offset is treated as a signed number
 		if ((addressOffset & 0x80) == 0) {
 			pageBoundaryCrossed = (((pc + operandOffset) & 0xFF00) != ((pc + addressOffset) & 0xFF00));
 			address = pc + addressOffset;
 		} else {
 			pageBoundaryCrossed = (((pc + operandOffset) & 0xFF00) != ((pc + ~(0xFF - addressOffset)) & 0xFF00));
 			address = pc + (~(0xFF - addressOffset));
 		}
 
 		return address;
 	}
 
 	private void addDebugCommands() {
 		try {
 			debugger.addCommand("go", CPU.class.getMethod("__go", String.class), this);
 			debugger.addCommand("step", CPU.class.getMethod("__step", String.class), this);
 			debugger.addCommand("setBreakpoint", CPU.class.getMethod("__setBreakpoint", String.class), this);
 			debugger.addCommand("clearBreakpoint", CPU.class.getMethod("__clearBreakpoint", String.class), this);
 			debugger.addCommand("clearBreakpoints", CPU.class.getMethod("__clearBreakpoints", String.class), this);
 			debugger.addCommand("getStackPointer", CPU.class.getMethod("__getStackPointer", String.class), this);
 			debugger.addCommand("getProgramCounter", CPU.class.getMethod("__getProgramCounter", String.class), this);
 			debugger.addCommand("getProcessorStatus", CPU.class.getMethod("__getProcessorStatus", String.class), this);
 			debugger.addCommand("getAccumulator", CPU.class.getMethod("__getAccumulator", String.class), this);
 			debugger.addCommand("getX", CPU.class.getMethod("__getX", String.class), this);
 			debugger.addCommand("getY", CPU.class.getMethod("__getY", String.class), this);
 			debugger.addCommand("setStackPointer", CPU.class.getMethod("__setStackPointer", String.class), this);
 			debugger.addCommand("setProgramCounter", CPU.class.getMethod("__setProgramCounter", String.class), this);
 			debugger.addCommand("setProcessorStatus", CPU.class.getMethod("__setProcessorStatus", String.class), this);
 			debugger.addCommand("setAccumulator", CPU.class.getMethod("__setAccumulator", String.class), this);
 			debugger.addCommand("setX", CPU.class.getMethod("__setX", String.class), this);
 			debugger.addCommand("setY", CPU.class.getMethod("__setY", String.class), this);
 			debugger.addCommand("getCPUState", CPU.class.getMethod("__getCPUState", String.class), this);
 			debugger.addCommand("logCPUState", CPU.class.getMethod("__logCPUState", String.class), this);
 			debugger.addCommand("enableCPULogging", CPU.class.getMethod("__enableCPULogging", String.class), this);
 			debugger.addCommand("disableCPULogging", CPU.class.getMethod("__disableCPULogging", String.class), this);
 
 		} catch (NoSuchMethodException e) {
 			System.out.println(e.getMessage());
 		}
 	}
 
 	/*
 	 * Debugger methods
 	 */
 	public void __go(String param) {
 		step = false;
 	}
 
 	public void __step(String param) {
 		step = true;
 	}
 
 	public void __setBreakpoint(String param) {
 		this.breakpoints.add(new Integer(Debugger.hexToInt(param)));
 	}
 
 	public void __clearBreakpoint(String param) {
 		this.breakpoints.remove(new Integer(Debugger.hexToInt(param)));
 	}
 
 	public void __clearBreakpoints(String param) {
 		this.breakpoints.clear();
 	}
 
 	public void __getStackPointer(String param) {
 		debugger.debugPrint(String.format("\n%s", Debugger.shortToHex(sp)));
 	}
 
 	public void __getProgramCounter(String param) {
 		debugger.debugPrint(String.format("\n%s", Debugger.intToHex(pc)));
 	}
 
 	public void __getProcessorStatus(String param) {
 		debugger.debugPrint(String.format("\n%s", Debugger.shortToHex(flags)));
 	}
 
 	public void __getAccumulator(String param) {
 		debugger.debugPrint(String.format("\n%s", Debugger.shortToHex(a)));
 	}
 
 	public void __getX(String param) {
 		debugger.debugPrint(String.format("\n%s", Debugger.shortToHex(x)));
 	}
 
 	public void __getY(String param) {
 		debugger.debugPrint(String.format("\n%s", Debugger.shortToHex(y)));
 	}
 
 	public void __setStackPointer(String param) {
 		sp = Debugger.hexToByte(param);
 	}
 
 	public void __setProgramCounter(String param) {
 		pc = Debugger.hexToInt(param);
 	}
 
 	public void __setProcessorStatus(String param) {
 		flags = Debugger.hexToByte(param);
 	}
 
 	public void __setAccumulator(String param) {
 		a = Debugger.hexToByte(param);
 	}
 
 	public void __setX(String param) {
 		x = Debugger.hexToByte(param);
 	}
 
 	public void __setY(String param) {
 		y = Debugger.hexToByte(param);
 	}
 
 	public void __getCPUState(String param) {
 		short opcode = mmc.readCPUMemSafe(pc);
 		Operation operation = cpuTables.Operations.get(opcode);
 		AddressingMode addressingMode = cpuTables.AdressingModes.get(opcode);
 
 		/*
 		 * Address and Data have to be passed in, because reading from the mem
 		 * map can change what's in memory.
 		 */
 		int address = Integer.parseInt(param.split(",")[0]);
 		short data = Byte.parseByte(param.split(",")[1]);
 
		debugger.debugPrint(String.format("\nOperation: %s  Addressing Mode: %s  Address: %s  Data: %s", operation.toString(), addressingMode.toString(), Debugger.intToHex(address), Debugger.shortToHex(data)));
 		debugger.debugPrint(String.format("\nPC: %s  SP: %s  A: %s  X: %s  Y: %s", Debugger.intToHex(pc), Debugger.shortToHex(sp), Debugger.shortToHex(a), Debugger.shortToHex(x), Debugger.shortToHex(y)));
 		debugger.debugPrint(String.format("\nStatus: S-%d  V-%d  B-%d  D-%d  I-%d  Z-%d  C-%d", isSignFlagSet() ? 1 : 0, isOverflowFlagSet() ? 1 : 0, isBreakFlagSet() ? 1 : 0, isDecimalFlagSet() ? 1 : 0, isInterruptFlagSet() ? 1 : 0, isZeroFlagSet() ? 1 : 0, isCarryFlagSet() ? 1 : 0));
 	}
 
 	public void __logCPUState(String param) {
 		short opcode = mmc.readCPUMemSafe(pc);
 		Operation operation = cpuTables.Operations.get(opcode);
 		AddressingMode addressingMode = cpuTables.AdressingModes.get(opcode);
 
 		/*
 		 * Address and Data have to be passed in, because reading from the mem
 		 * map can change what's in memory.
 		 */
 		int address = Integer.parseInt(param.split(",")[0]);
 		short data = Byte.parseByte(param.split(",")[1]);
 
		debugger.debugLog(String.format("\nOperation: %s  Addressing Mode: %s  Address: %s  Data: %s", operation.toString(), addressingMode.toString(), Debugger.intToHex(address), Debugger.shortToHex(data)));
 		debugger.debugLog(String.format("\nPC: %s  SP: %s  A: %s  X: %s  Y: %s", Debugger.intToHex(pc), Debugger.shortToHex(sp), Debugger.shortToHex(a), Debugger.shortToHex(x), Debugger.shortToHex(y)));
 		debugger.debugLog(String.format("\nStatus: S-%d  V-%d  B-%d  D-%d  I-%d  Z-%d  C-%d", isSignFlagSet() ? 1 : 0, isOverflowFlagSet() ? 1 : 0, isBreakFlagSet() ? 1 : 0, isDecimalFlagSet() ? 1 : 0, isInterruptFlagSet() ? 1 : 0, isZeroFlagSet() ? 1 : 0, isCarryFlagSet() ? 1 : 0));
 	}
 
 	public void __enableCPULogging(String param) {
 		logCPUState = true;
 	}
 
 	public void __disableCPULogging(String param) {
 		logCPUState = false;
 	}
 }
