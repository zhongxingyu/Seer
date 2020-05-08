 package instructions;
 
 import static assemblernator.ErrorReporting.makeError;
 import simulanator.Deformatter;
 import simulanator.Machine;
 import assemblernator.AbstractInstruction;
 import assemblernator.ErrorReporting.ErrorHandler;
 import assemblernator.Instruction;
 import assemblernator.Module;
 import assemblernator.Module.Value.BitLocation;
 
 /**
  * The ISRG instruction.
  * 
  * @author Generate.java
  * @date Apr 08, 2012; 08:26:19
  * @specRef UL1
  */
 public class USI_SLEEP extends AbstractInstruction {
 	/**
 	 * The operation identifier of this instruction; while comments should not
 	 * be treated as an instruction, specification says they must be included in
 	 * the user report. Hence, we will simply give this class a semicolon as its
 	 * instruction ID.
 	 */
 	private static final String opId = "SLP";
 
 	/** This instruction's identifying opcode. */
 	private static final int opCode = 0x3B; // 0b111011
 
 	/** The static instance for this instruction. */
 	static USI_SLEEP staticInstance = new USI_SLEEP(true);
 
 	/** @see assemblernator.Instruction#getNewLC(int, Module) */
 	@Override public int getNewLC(int lc, Module mod) {
 		return lc + 1;
 	}
 
 	/** @see assemblernator.Instruction#check(ErrorHandler, Module) */
 	@Override public boolean check(ErrorHandler hErr, Module module) {
 		if (operands.size() < 1) {
 			hErr.reportError(makeError("instructionMissingOpS", opId, "FC"), lineNum, 0);
 			return false;
 		}
 		if (operands.size() > 1) {
 			hErr.reportError(makeError("extraOperandsIns", opId), lineNum, 0);
 			return false;
 		}
 		
 		Operand op = getOperandData("FC");
 		if (op == null) {
 			hErr.reportError(
 					makeError("instructionMissingOp", opId, "FC"), lineNum, 0);
 			return false;
 		}
 		op.value = module.evaluate(op.expression, false, BitLocation.Other,
 				hErr, this, op.valueStartPosition);
		if (op.value.value < 1 || op.value.value > 65535) {
 			hErr.reportError(
					makeError("OORconstant", "FC", opId, "1", "65535"),
 					lineNum, op.valueStartPosition);
 			return false;
 		}
 
 		return true;
 	}
 
 	/** @see assemblernator.Instruction#assemble() */
 	@Override public int[] assemble() {
 		return new int[] { opCode << 26 | 0x01800000 | getOperandData("FC").value.value };
 	}
 
 	/** @see assemblernator.Instruction#execute(int, Machine) */
 	@Override public void execute(int instruction, Machine machine) {
 		int dur = Deformatter.breakDownOther(instruction).source;
 		System.out.println("Sleeping for " + dur + " ms");
 		try {
 			Thread.sleep(dur);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 	// =========================================================
 	// === Redundant code ======================================
 	// =========================================================
 	// === This code's the same in all instruction classes, ====
 	// === But Java lacks the mechanism to allow stuffing it ===
 	// === in super() where it belongs. ========================
 	// =========================================================
 
 	/**
 	 * @see Instruction
 	 * @return The static instance of this instruction.
 	 */
 	public static Instruction getInstance() {
 		return staticInstance;
 	}
 
 	/** @see assemblernator.Instruction#getOpId() */
 	@Override public String getOpId() {
 		return opId;
 	}
 
 	/** @see assemblernator.Instruction#getOpcode() */
 	@Override public int getOpcode() {
 		return opCode;
 	}
 
 	/** @see assemblernator.Instruction#getNewInstance() */
 	@Override public Instruction getNewInstance() {
 		return new USI_SLEEP();
 	}
 
 	/**
 	 * Calls the Instance(String,int) constructor to track this instruction.
 	 * 
 	 * @param ignored
 	 *            Unused parameter; used to distinguish the constructor for the
 	 *            static instance.
 	 */
 	private USI_SLEEP(boolean ignored) {
 		super(opId, opCode);
 	}
 
 	/** Default constructor; does nothing. */
 	private USI_SLEEP() {}
 }
