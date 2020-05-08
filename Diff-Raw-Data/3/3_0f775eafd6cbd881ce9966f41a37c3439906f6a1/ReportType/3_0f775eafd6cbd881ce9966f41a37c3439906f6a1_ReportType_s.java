 package swp_compiler_ss13.common.report;
 
 /**
  * ReportType defines a categorization for problems in the source
  * code processed by the compiler module implementations. 
  */
 public enum ReportType {
 	UNRECOGNIZED_TOKEN,
 	DIVISION_BY_ZERO,
 	DOUBLE_DECLARATION,
 	UNDECLARED_VARIABLE_USAGE,
 	TYPE_MISMATCH,
 	WORD_NOT_IN_GRAMMAR,
 	UNDEFINED;
 }
