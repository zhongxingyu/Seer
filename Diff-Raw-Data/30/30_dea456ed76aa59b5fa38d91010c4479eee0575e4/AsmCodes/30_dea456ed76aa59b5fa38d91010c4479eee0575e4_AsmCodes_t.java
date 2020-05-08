 package edu.osu.cse.mmxi.asm.error;
 
 import edu.osu.cse.mmxi.common.error.ErrorCodes;
 import edu.osu.cse.mmxi.common.error.ErrorLevels;
 
 /**
  * This is an enumeration of all error codes thrown during the assembly process.
  * Additional information is pass to the error handler at run-time regarding details to
  * the error.
  * 
  */
 public enum AsmCodes implements ErrorCodes {
     // simpleLoaderErrors IO
     IO_BAD_PATH(100, "File path does not refer to a file", ErrorLevels.FATAL),
 
     IO_BAD_READ(101, "Failed to read file", ErrorLevels.FATAL),
 
     IO_BAD_WRITE(102, "Failed to write file", ErrorLevels.FATAL),
 
     IO_NO_INPUT(110, "No assembly file was given", ErrorLevels.FATAL),
 
     IO_MANY_INPUT(111, "Multiple assembly files given", ErrorLevels.WARN),
 
     P1_INST_NO_SPACE(200, "Instruction lines must begin with whitespace",
         ErrorLevels.FATAL),
 
     P1_INVALID_SYMB(210, "Invalid symbol name", ErrorLevels.FATAL),
 
     P2_INST_BAD_SYMBOL(211, "Undefined symbol", ErrorLevels.FATAL),
 
     P1_SYMB_RESET(212, "Symbols can only be defined once", ErrorLevels.FATAL),
 
     P1_INST_ARG_NOT_EXP(220, "Argument must be an immediate or expression",
         ErrorLevels.FATAL),
 
     P1_INST_BAD_STRZ(221, "Argument must be a string", ErrorLevels.FATAL),
 
     AP_BAD_EXPR(230, "Syntax error in expression", ErrorLevels.FATAL),
 
     P2_GENERAL_ERROR(303, "An error occurred on pass 2.", ErrorLevels.WARN),
 
     P2_NO_ORIG(300, "No .ORIG record found", ErrorLevels.FATAL),
 
    P2_MANY_ORIG(301, "Multiple .ORIG records found", ErrorLevels.FATAL),

    P2_NO_EXEC(302, "No .END record found", ErrorLevels.FATAL),

    P2_MANY_EXEC(303, "Multiple .END records found", ErrorLevels.FATAL),
 
     P2_LEN_CMX(310, "Program length too complex to encode", ErrorLevels.FATAL),
 
     P2_EXEC_CMX(311, "Execution address too complex to encode", ErrorLevels.FATAL),
 
     P2_FILL_CMX(312, "Relation too complex to encode", ErrorLevels.FATAL),
 
     P2_BLK_CMX(313, "Block length too complex to encode", ErrorLevels.FATAL),
 
     IF_ARG_CMX(314, "Arguments too complex to encode", ErrorLevels.FATAL),
 
     P1_INST_BAD_OP_CODE(500, "Unknown op-code", ErrorLevels.FATAL),
 
     IF_BAD_ARG_NUM(501, "Incorrect number of arguments", ErrorLevels.FATAL),
 
     IF_SIG_INVALID(502, "Invalid signature for opcode", ErrorLevels.FATAL),
 
     IF_ARG_RANGE(503, "Argument out of range for type", ErrorLevels.FATAL),
 
     IF_ABS_ADDR(504, "Absolute page address used in relative program", ErrorLevels.FATAL),
 
     IF_OFF_PAGE(505, "Label dereferences to incorrect page", ErrorLevels.FATAL);
 
     private int         code;
     private String      str;
     private ErrorLevels level;
 
     AsmCodes(final int code, final String str, final ErrorLevels level) {
         this.str = str;
         this.code = code;
         this.level = level;
     }
 
     @Override
     public String getMsg() {
         return str;
     }
 
     @Override
     public int getCode() {
         return code;
     }
 
     @Override
     public ErrorLevels getLevel() {
         return level;
     }
 
     @Override
     public String toString() {
         return level + " " + code + (str == null ? "" : ": " + str);
     }
 }
