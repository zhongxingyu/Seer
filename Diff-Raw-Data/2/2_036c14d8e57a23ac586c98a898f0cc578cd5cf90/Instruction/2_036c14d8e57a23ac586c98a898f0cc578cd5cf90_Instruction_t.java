 package titocc.compiler;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * Intermediate presentation of a machine instruction, using virtual registers.
  *
  * TTK-91 instructions used in intermediate code:
  * nop
  * store R, R/M (*)
  * load R, R/M
  * in R, R/M
  * out R, R/M
  * add/sub/mul/div/mod/and/or/xor/shl/shr/shra R, R/M
  * comp R, R/M
  * jump [R,] R/M (*)
  * jneg/jnneg/jzer/jnzer/jpos/jnneg R, R/M (*)
  * jles/jnles/jequ/jnequ/jgre/jngre [R,] R/M (*)
  * call R, R/M (*)
  * push R, R/M
  * pop R, R
  *
  * (*) : RHS has reduced addressing mode (i.e. one less memory fetch):
  * <pre>
  *           "="  ""   "at"
  * normal:   0    1    2
  * reduced:  0    0    1
  * </pre>
  *
  * Pseudo instructions:
  * equ value
  * dc value
  * ds size
  *
  * Instructions that are guaranteed not to appear in the intermediate code:
  * (exit)
  * (svc)
  * (pushr)
  * (popr)
  * (def)
  * (not)
  */
 class Instruction
 {
 	/**
 	 * Prefixs corresponging to nominal addressing modes.
 	 */
 	private static final String[] addressingModePrefixes = new String[]{"=", "", "@"};
 
 	/**
 	 * Set of all pseudo-instructions.
 	 */
 	static final Set<String> pseudoInstructions = new HashSet<String>(
 			Arrays.asList("dc", "ds", "equ"));
 
 	/**
 	 * Optional label.
 	 */
 	String label = null;
 
 	/**
 	 * Mnemonic. ("load", "add" etc.)
 	 */
 	String mnemonic = null;
 
 	/**
 	 * The operand of single-operand pseudo instruction ("ds", "dc", "equ").
 	 */
 	int pseudoOperand = 0;
 
 	/**
 	 * Left operand.
 	 */
 	VirtualRegister leftReg = null;
 
 	/**
 	 * Right register operand.
 	 */
 	VirtualRegister rightReg = null;
 
 	/**
 	 * Right immediate operand, including addressing mode.
 	 */
 	private String immediateValue = null;
 
 	/**
 	 * Real addressing mode (0-2).
 	 */
 	private int realAddressingMode = 0;
 
 	/**
 	 * Constructs an instruction that has no operands ("nop").
 	 *
 	 * @param label optional label; null if not used
 	 * @param mnemonic mnemonic
 	 */
 	Instruction(String label, String mnemonic)
 	{
 		if (!mnemonic.equals("nop"))
 			throw new InternalCompilerException("Constructing an illegal 0-operand instruction.");
 		this.label = label;
 		this.mnemonic = mnemonic;
 	}
 
 	/**
 	 * Constructs a pseudo instruction with one operand.
 	 *
 	 * @param label required label (name of declared data area or constant)
 	 * @param mnemonic mnemonic
 	 * @param pseudoOperand value ("equ", "dc") or size ("ds")
 	 */
 	Instruction(String label, String mnemonic, int pseudoOperand)
 	{
 		if (!pseudoInstructions.contains(mnemonic) || label == null)
 			throw new InternalCompilerException("Constructing an illegal pseudo instruction.");
 		this.label = label;
 		this.mnemonic = mnemonic;
 		this.pseudoOperand = pseudoOperand;
 	}
 
 	/**
 	 * Constructs a normal instruction. Left operand is a register and right operand consists of
 	 * immediate value or register, one of which can be omitted.
 	 *
 	 * @param label optinoal label; null if not used
 	 * @param mnemonic mnemonic
 	 * @param leftReg required LHS register operand
 	 * @param realAddressingMode real addressing mode (0-2)
 	 * @param immediateOperand RHS immediate value; null if not used
 	 * @param rightReg RHS register operand; null if not used
 	 */
 	Instruction(String label, String mnemonic, VirtualRegister leftReg, int realAddressingMode,
 			String immediateValue, VirtualRegister rightReg)
 	{
 		if (mnemonic == null)
 			throw new InternalCompilerException("Missing mnemonic for instruction.");
 		if (pseudoInstructions.contains(mnemonic))
 			throw new InternalCompilerException("Too many operands for pseudo instruction.");
 		if (leftReg == null || (immediateValue == null && rightReg == null))
 			throw new InternalCompilerException("Missing operand for instruction.");
 		if (immediateValue != null && immediateValue.isEmpty())
 			throw new InternalCompilerException("Empty immediate operand for instruction.");
 		// Only allow SP as LHS for pop.
 		if (mnemonic.equals("pop") && (rightReg == null || leftReg != VirtualRegister.SP))
 			throw new InternalCompilerException("Invalid pop instruction.");
 		// Titokone does not allow memory fetches with call.
 		if (mnemonic.equals("call") && realAddressingMode > 0)
 			throw new InternalCompilerException("Invalid call instruction.");
 		if (hasReducedAddressingMode(mnemonic, immediateValue) && realAddressingMode >= 2
 				|| realAddressingMode < 0)
 			throw new InternalCompilerException("Invalid addressing mode.");
 
 		this.label = label;
 		this.mnemonic = mnemonic;
 		this.leftReg = leftReg;
 		this.realAddressingMode = realAddressingMode;
 		this.immediateValue = immediateValue;
 		this.rightReg = rightReg;
 
 		// Prevent jumps to non-const addresses to make flow analysis possible.
 		if (isJumpInstruction() && (rightReg != null || realAddressingMode > 0))
 			throw new InternalCompilerException("Non-constant address in jump instruction.");
 	}
 
 	/**
 	 * Get the register that is modified by this instructions.
 	 *
 	 * @return modified register or null if doesn't modify any registers.
 	 */
 	VirtualRegister getModifiedRegister()
 	{
 		if (mnemonic.equals("pop"))
 			return rightReg;
 		if (mnemonic.equals("store") || mnemonic.equals("out") || mnemonic.equals("comp")
 				|| isJumpInstruction())
 			return null;
 		return leftReg;
 	}
 
 	/**
 	 * Checks if the behavior of this instruction depends on earlier value of the LHS register.
 	 *
 	 * @return true if the earlier value of LHS register is ignored
 	 */
 	boolean discardsLhs()
 	{
 		return mnemonic.equals("load") || mnemonic.equals("in");
 	}
 
 	/**
 	 * Checks id the behavior of this instruction depends on earlier value of the RHS register.
 	 *
 	 * @return true if the earlier value of RHS register is ignored
 	 */
 	boolean discardsRhs()
 	{
 		return mnemonic.equals("load") || mnemonic.equals("in");
 	}
 
 	/**
 	 * Checks whether the instruction is a jump instruction.
 	 *
 	 * @return true if jump instruction
 	 */
 	boolean isJumpInstruction()
 	{
 		return mnemonic.charAt(0) == 'j';
 	}
 
 	/**
 	 * Get the full RHS operand as string.
 	 *
 	 * @return string representation of RHS
 	 */
 	String getRhsString()
 	{
 		String ret = addressingModePrefixes[getNominalAddressingMode()];
 		if (immediateValue != null)
 			ret += immediateValue;
 		if (rightReg != null) {
 			if (immediateValue == null)
 				ret += rightReg.realRegister.toString();
 			else
 				ret += "(" + rightReg.realRegister.toString() + ")";
 		}
 		return ret;
 	}
 
 	/**
 	 * Creates a "nop" instruction with the same label.
 	 *
 	 * @return new instruction
 	 */
 	Instruction makeNop()
 	{
 		return new Instruction(label, "nop");
 	}
 
 	/**
 	 * Get the real addressing mode for this instruction.
 	 *
 	 * @return real addressing mode
 	 */
 	int getRealAddressingMode()
 	{
 		return realAddressingMode;
 	}
 
 	/**
 	 * Get the immediate value.
 	 *
 	 * @return immediate value or null if there isn't one
 	 */
 	String getImmediateValue()
 	{
 		return immediateValue;
 	}
 
 	/**
 	 * Creates a new instruction where RHS register operand is replaced using a value loaded to
 	 * that register in earlier load instruction.
 	 *
 	 * @param loadInstruction instruction that loads a value to register
 	 * @param addrMode required addressing mode for the load instruction
 	 * @return new instruction with replaced RHS operand
 	 */
 	Instruction propagateRhsValue(Instruction loadInstruction, int addrMode)
 	{
 		if (!loadInstruction.isConstantLoad(addrMode))
 			throw new InternalCompilerException("Invalid load instruction for RHS propagation.");
 		VirtualRegister reg = loadInstruction.leftReg;
 		if (mnemonic.equals("popr"))
 			return null;
 		if ((leftReg == reg && !discardsLhs()) || rightReg != reg)
 			return null;
 		if (immediateValue != null && !immediateValue.equals("0"))
 			return null;
 		int maxNewAddrMode = getMaxAddressingMode(mnemonic, loadInstruction.immediateValue);
 		if (realAddressingMode + addrMode > maxNewAddrMode)
 			return null;
 		return new Instruction(label, mnemonic, leftReg, realAddressingMode + addrMode,
 				loadInstruction.immediateValue, loadInstruction.rightReg);
 	}
 
 	/**
 	 * Checks if the instruction loads a value using a specified addressing mode.
 	 *
 	 * @param addrMode real addressing mode
 	 * @return true if constant load instruction
 	 */
 	boolean isConstantLoad(int addrMode)
 	{
 		return mnemonic.equals("load") && (rightReg == null || rightReg == VirtualRegister.FP)
 				&& realAddressingMode == addrMode;
 	}
 
 	/**
 	 * Extracts the addressing mode from
 	 *
 	 * @param mnemonic instruction mnemonic
 	 * @param immediateOperand immediate operand, including addressing mode
 	 * @return real addressing mode (0-2)
 	 */
 	static int extractRealAddressingMode(String mnemonic, String immediateOperand)
 	{
 		int mode;
 		if (immediateOperand == null)
 			mode = 1;
 		else if (immediateOperand.charAt(0) == '=')
 			mode = 0;
 		else if (immediateOperand.charAt(0) == '@')
 			mode = 2;
 		else
 			mode = 1;
 
 		if (hasReducedAddressingMode(mnemonic, extractImmediateValue(immediateOperand)))
 			mode = Math.max(mode - 1, 0);
 
 		return mode;
 	}
 
 	/**
 	 * Removes the addressing mode from immediate operand.
 	 *
 	 * @param immediateOperand immediate operand, including addressing mode
 	 * @return immediate value without the addressing mode
 	 */
 	static String extractImmediateValue(String immediateOperand)
 	{
 		if (immediateOperand == null)
 			return null;
 		if (immediateOperand.charAt(0) == '=' || immediateOperand.charAt(0) == '@')
			return immediateOperand.length() > 1 ? immediateOperand.substring(1) : null;
 		return immediateOperand;
 	}
 
 	/**
 	 * Increases the addressing mode by one for store, call, jump etc.
 	 *
 	 * @return nominal addressing mode (0-2)
 	 */
 	private int getNominalAddressingMode()
 	{
 		int nominalMode = realAddressingMode;
 		if (hasReducedAddressingMode(mnemonic, immediateValue))
 			++nominalMode;
 		return nominalMode;
 	}
 
 	private static int getMaxAddressingMode(String mnemonic, String immediateValue)
 	{
 		if (mnemonic.equals("call"))
 			return 0;
 		else
 			return hasReducedAddressingMode(mnemonic, immediateValue) ? 1 : 2;
 	}
 
 	private static boolean hasReducedAddressingMode(String mnemonic, String immediateValue)
 	{
 		return mnemonic.equals("store") || mnemonic.equals("call") || mnemonic.charAt(0) == 'j'
 				|| immediateValue == null;
 	}
 }
