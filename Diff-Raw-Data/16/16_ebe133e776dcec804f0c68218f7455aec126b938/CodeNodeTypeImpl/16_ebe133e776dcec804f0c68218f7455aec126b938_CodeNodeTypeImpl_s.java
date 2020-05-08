 package de.weltraumschaf.caythe.intermediate.codeimpl;
 
 import de.weltraumschaf.caythe.intermediate.CodeNodeType;
 
 /**
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  * @license http://www.weltraumschaf.de/the-beer-ware-license.txt THE BEER-WARE LICENSE
  */
 public enum CodeNodeTypeImpl implements CodeNodeType {
     // Program structure
     PROGRAM, PROCEDURE, FUNCTION,
 
     // Statements
     COMPOUND, ASSIGN, LOOP, TEST, CALL, PARAMETERS,
     IF, SELECT, SELECT_BRANCH, SELECT_CONSTANTS, NO_OP,
 
     // Relational operators
     EQ, NE, LT, LE, GT, GE, NOT,
 
     // Additive operators
     ADD, SUBTRACT, OR, NEGATE,
 
     // Multiplicative operators
     MULTIPLY, INTEGER_DIVIDE, FLOAT_DIVIDE, MOD, AND,
 
     // Operands
     VARIABLE, SUBSCRIPTS, FIELD,
     INTEGER_CONSTANT, REAL_CONSTANT,
     STRING_CONSTANT, BOOLEAN_CONSTANT,
 
     // WRITE parameter
    WRITE_PARM,
 }
