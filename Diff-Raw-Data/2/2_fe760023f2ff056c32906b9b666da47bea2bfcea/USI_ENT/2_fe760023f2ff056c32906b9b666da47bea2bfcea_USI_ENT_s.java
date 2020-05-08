 package instructions;
 
 import static assemblernator.ErrorReporting.makeError;
 import assemblernator.AbstractDirective;
 import assemblernator.ErrorReporting.ErrorHandler;
 import assemblernator.IOFormat;
 import assemblernator.Instruction;
 import assemblernator.Module;
 import assemblernator.OperandChecker;
 
 /**
  * The ENT instruction.
  * 
  * @author Generate.java
  * @date Apr 08, 2012; 08:26:19
  * @specRef D5
  */
 public class USI_ENT extends AbstractDirective {
 	/**
 	 * The operation identifier of this instruction; while comments should not
 	 * be treated as an instruction, specification says they must be included in
 	 * the user report. Hence, we will simply give this class a semicolon as its
 	 * instruction ID.
 	 */
 	private static final String opId = "ENT";
 	
 	/** The static instance for this instruction. */
 	static USI_ENT staticInstance = new USI_ENT(true);
 
 	/** @see assemblernator.Instruction#getNewLC(int, Module) */
 	@Override
 	public int getNewLC(int lc, Module mod) {
 		return lc;
 	}
 	/**
 	 * The type of operand specifying the source for this operation.
 	 */
 	String src = "";
 
 	/** @see assemblernator.Instruction#check(ErrorHandler, Module) */
 	@Override
 	public boolean check(ErrorHandler hErr, Module module) {
 		//check for ent LR label is correct
		for(int i = 0;this.countOperand("LR") != i;i++){
 		if (!module.getSymbolTable().hasLocalEntry(this.getOperand("LR", i))){
 			hErr.reportError(makeError("OORlabel", "LR", this.getOpId()), this.lineNum, -1);
 			return false;
 		}
 		}
 		return true;
 	}
 
 	/** @see assemblernator.Instruction#assemble() */
 	@Override
 	public int[] assemble() {
 		return null; // TODO: IMPLEMENT
 	}
 
 	/** @see assemblernator.Instruction#immediateCheck(assemblernator.ErrorReporting.ErrorHandler, Module) */
 	@Override public boolean immediateCheck(ErrorHandler hErr, Module module) {
 		boolean isValid = true;
 		//checks to make sure ENT does not have a label
 		if(this.label != null){
 			hErr.reportError(makeError("noLabel", this.getOpId()), this.lineNum, -1);
 			isValid = false;
 		}
 		//checks if there isnt enogh operands
 		if(this.operands.size() < 1){
 			isValid = false;
 			hErr.reportError(
 					makeError("directiveMissingOp", this.getOpId(), "LR"),
 					this.lineNum, -1);
 			//checks that there are not to many operands
 		}else if(this.operands.size() <= 4){
 			//makes sure only lr's are used
 			if(this.countOperand("LR") == this.operands.size()){
 				//checks for valid label
 				for(int i=0; this.countOperand("LR") > i; i++){
 					if(!IOFormat.isValidLabel(this.getOperand("LR", i))){
 						hErr.reportError(makeError("OORlabel", "LR", this.getOpId()), this.lineNum, -1);
 						isValid = false;
 					}
 				}
 			}
 		}else{
 			isValid = false;
 			hErr.reportError(makeError("extraOperandsDir", this.getOpId()),
 					this.lineNum, -1);
 		}
 		return isValid;
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
 	@Override
 	public String getOpId() {
 		return opId;
 	}
 	
 	/** @see assemblernator.Instruction#getNewInstance() */
 	@Override
 	public Instruction getNewInstance() {
 		return new USI_ENT();
 	}
 
 	/**
 	 * Calls the Instance(String,int) constructor to track this instruction.
 	 * 
 	 * @param ignored
 	 *            Unused parameter; used to distinguish the constructor for the
 	 *            static instance.
 	 */
 	private USI_ENT(boolean ignored) {
 		super(opId);
 		usage = Usage.ENTRY;
 	}
 
 	/** Default constructor; does nothing. */
 	private USI_ENT() {
 	}
 }
