 package edu.osu.cse.mmxi.loader.parser;
 
 public enum ErrorCodes {
     // unknown error
     UNKNOWN(999, "Unknow Error Occurred", ErrorLevels.WARN),
 
     // simpleLoaderErrors IO
     IO_BAD_PATH(100, "File path does not refer to a file", ErrorLevels.FATAL),
     IO_BAD_READ(101, "Failed to read the file.", ErrorLevels.FATAL),
     IO_BAD_FILE(102, "File was empty.", ErrorLevels.FATAL),
 
     // simpleLoader errors
     ADDR_OUT_BOUNDS(200, "Text address out of bounds", ErrorLevels.FATAL),
     ADDR_EXEC_OUT_BOUNDS(201, "Execution address out of bounds", ErrorLevels.FATAL),
 
     // parser errors
     PARSE_EXECPTION(300, "Parsing Exception", ErrorLevels.WARN),
    PARSE_EMPTY(301,"Parsing completed, no tokens found.", ErrorLevels.FATAL),
     PARSE_NO_HEADER(302,"Object File did not contain a Header record.", ErrorLevels.FATAL),
     PARSE_NO_RECORDS(303, "Object File did not contain any Text records.", ErrorLevels.FATAL),
    PARSE_NO_EXEC(304, "Object File did not contain an exec record.", ErrorLevels.FATAL),
    PARSE_BAD_TEXT(399, "Malformed Record.", ErrorLevels.FATAL),
 
     // execution errors
     EXEC_TRAP_UNKN(400, "Unknown TRAP vector", ErrorLevels.WARN),
     EXEC_TRAP_OUT(401, "R0 does not contain a character.", ErrorLevels.WARN),
 
     // UI errors
     UI_MAX_CLOCK(500, "--max-clock-count argument in invalid format.", ErrorLevels.WARN),
    UI_UNKN_CMD(599, "Unknown command.", ErrorLevels.WARN),
     UI_MULTI_FILE(502, "Multiple files given.", ErrorLevels.WARN),
     UI_NO_FILE(503, "No file was given.", ErrorLevels.FATAL),
     UI_MULTI_SETTINGS(504, "Multiple settings given.", ErrorLevels.WARN);
 
 
     private int         code;
     private String      str;
     private ErrorLevels level;
 
     ErrorCodes(final int code, final String str, final ErrorLevels level) {
         this.str = str;
         this.code = code;
         this.level = level;
     }
 
     public String getErrMsg() {
         return str;
     }
 
     public int getErrCode() {
         return code;
     }
 
     public ErrorLevels getErrLevel() {
         return level;
     }
 
     @Override
     public String toString() {
         return code + " : " + level + " : " + str;
     }
 }
