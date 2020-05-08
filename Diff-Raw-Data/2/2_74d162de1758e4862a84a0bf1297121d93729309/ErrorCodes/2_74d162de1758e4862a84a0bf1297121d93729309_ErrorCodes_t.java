 package edu.osu.cse.mmxi.asm.error;
 
 import edu.osu.cse.mmxi.common.error.ErrorLevels;
 
 public enum ErrorCodes {
     // simpleLoaderErrors IO
     IO_BAD_PATH(100, "File path does not refer to a file.", ErrorLevels.FATAL),
 
     IO_BAD_READ(101, "Failed to read file.", ErrorLevels.FATAL),
 
     IO_BAD_FILE(102, "File was empty.", ErrorLevels.FATAL),
 
    IO_BAD_INPUT(103, "No assembly file was given.", ErrorLevels.FATAL),

     P1_BAD_LINE(200, "Failed to parse line on Pass 1.", ErrorLevels.FATAL),
 
     P1_INST_WRONG_PARAMS(201, "Invalid parameter count", ErrorLevels.FATAL),
 
     P1_INST_BAD_LABEL(202, "Numbers can not be labels.", ErrorLevels.FATAL),
 
     P1_INST_BAD_REG(203, "Registers cannot be labels.", ErrorLevels.FATAL),
 
     P1_INST_BAD_ARGS(204, "Incorrect number of arguments.", ErrorLevels.FATAL),
 
     P1_INST_BAD_LINE_FORMAT(205, "Instruction lines must begin with whitespace",
         ErrorLevels.FATAL),
 
     P1_INST_BAD_OP_CODE(206, "Unknow op-code.", ErrorLevels.FATAL),
 
     P1_INST_BAD_ORIG_ARGS(207, "Too many args for .ORIG", ErrorLevels.FATAL),
 
     P1_INST_BAD_ORIG_LABEL(208, "Invalid segment name for .ORIG", ErrorLevels.FATAL),
 
     P1_INST_BAD_ORIG_ADDR(209, "Invalid address given.", ErrorLevels.FATAL),
 
     P1_INST_BAD_ORIG_TYPE(210, "Argument must be an immediate or expression",
         ErrorLevels.FATAL),
 
     P1_INST_BAD_SYMBOL(211, "Invalid Symbol.", ErrorLevels.FATAL),
 
     P1_INST_BAD_EQU_LABEL(220, ".EQU requires a label", ErrorLevels.FATAL),
 
     P1_INST_BAD_EQU_IMM(221, "Argument must be an immediate or expression.",
         ErrorLevels.FATAL),
 
     P1_INST_BAD_END_IMM(230, "Argument must be an immediate or expression.",
         ErrorLevels.FATAL),
 
     P1_INST_BAD_STRZ(240, "Argument must be a string.", ErrorLevels.FATAL),
 
     P1_INST_BAD_FILL(245, "Argument must be an immediate or expressing.",
         ErrorLevels.FATAL),
 
     P1_INST_BAD_BLKW(250, "Argument must be an immediate or expression",
         ErrorLevels.FATAL),
 
     EXEC_END_OF_FILE(402, "End of File reached prematurely.", ErrorLevels.FATAL),
 
     // unknown error
     UNKNOWN(999, "Unknown Error", ErrorLevels.WARN);
 
     private int         code;
     private String      str;
     private ErrorLevels level;
 
     ErrorCodes(final int code, final String str, final ErrorLevels level) {
         this.str = str;
         this.code = code;
         this.level = level;
     }
 
     public String getMsg() {
         return str;
     }
 
     public int getCode() {
         return code;
     }
 
     public ErrorLevels getLevel() {
         return level;
     }
 
     @Override
     public String toString() {
         return level + " " + code + (str == null ? "" : ": " + str);
     }
 }
