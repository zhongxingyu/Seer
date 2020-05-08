 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Nathanael
  * Date: 1/22/13
  * Time: 8:59 PM
  */
 public class Compiler {
     public static final String BINARY = "binary";
     public static final String URINARY = "urinary";
 
     private LexicalAnalyzer lexicalAnalyzer;
     private List<Tuple> openParens = new ArrayList<Tuple>();
     private List<Tuple> openBlocks = new ArrayList<Tuple>();
     private LinkedHashMap<String, Symbol> symbolTable;
     private List<String> paramIdList = new ArrayList<String>();
     private String errorList = "";
     private String scope = "g";
     private int symIdInr = 100;
     private int eIndex = 0;
     private boolean useConditionInReturn = false;
     private boolean useConditionInReturnWhile = false;
 
     private Stack<String> ifStack = new Stack<String>();
     private Stack<String> whileStack = new Stack<String>();
     private boolean canPop = false;
 
     private String condLabel = "";
     private String condTypeScope = "";
 
     private Queue<String> ifList = new ArrayDeque<String>();
 
     private List<ICode> iCodeList = new ArrayList<ICode>();
 
     public Compiler(LexicalAnalyzer lexicalAnalyzer, LinkedHashMap<String, Symbol> symbolTable) {
         this.lexicalAnalyzer = lexicalAnalyzer;
         this.symbolTable = symbolTable;
     }
 
     public void evaluate() {
         iCodeList.add(new ICode("", "JMP", "", "", "", "; jump to main"));
         iCodeList.add(new ICode("", "TRP", "0", "", "", "; program end"));
         Tuple currentLex;
         Tuple previousLex = null;
 
         while (lexicalAnalyzer.hasNext()) {
             Tuple temp;
             List<Tuple> tempList = new ArrayList<Tuple>();
 
             temp = lexicalAnalyzer.getNext();
             while (canAddToList(temp)) {
                 tempList.add(temp);
                 temp = lexicalAnalyzer.getNext();
             }
             tempList.add(temp);
 
             if (tempList.isEmpty()) {
                 continue;
             }
 
             for (int i = 0; i < tempList.size(); i++) {
                 currentLex = tempList.get(i);
 
                 if (currentLex.type.equals(LexicalAnalyzer.tokenTypesEnum.UNKNOWN.name())) {
                     errorList += "Unknown object. Line: " + currentLex.lineNum + "\n";
                     previousLex = currentLex;
                     continue;
                 }
 
                 if (currentLex.lexi.equals("object") || currentLex.lexi.equals("string")) {
                     errorList += currentLex.lexi + " is a reserved word not currently in use by the language. Line: " + currentLex.lineNum + "\n";
                     previousLex = currentLex;
                     continue;
                 }
 
                 Tuple nextLexi = (i + 1 == tempList.size() ? null : tempList.get(i + 1));
                 Tuple peekPrevious = (i - 2 < 0 ? null : tempList.get(i - 2));
 
 
                 // validate expressions
                 if (currentLex.type.equals(LexicalAnalyzer.tokenTypesEnum.ASSIGNMENT_OPR.name())) {
                     validateAssignmentOpr(currentLex, previousLex, nextLexi);
                 } else if (currentLex.type.equals(LexicalAnalyzer.tokenTypesEnum.MATH_OPR.name())) {
                     validateMathOpr(currentLex, previousLex, nextLexi);
                     if (nextLexi.lexi.equals("-")) {
                         if (tempList.get(i + 2) != null && (tempList.get(i + 2).type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name()) || tempList.get(i + 2).type.equals(LexicalAnalyzer.tokenTypesEnum.NUMBER.name()) || tempList.get(i + 2).lexi.equals("("))) {
                             // all good
                         } else {
                             errorList += "Both side of mathematical operation must be either an Identifier or a Number. Line: " + previousLex.lineNum + "\n";
                         }
                     }
                 } else if (currentLex.type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
                     if (previousLex.type.equals(LexicalAnalyzer.tokenTypesEnum.NUMBER.name()) || previousLex.type.equals(LexicalAnalyzer.tokenTypesEnum.CHARACTER.name())) {
                         errorList += "Invalid function call on line: " + currentLex.lineNum + "\n";
                     }
                     openParens.add(currentLex);
                 } else if (currentLex.type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                     if (nextLexi.type.equals(LexicalAnalyzer.tokenTypesEnum.NUMBER.name()) || nextLexi.type.equals(LexicalAnalyzer.tokenTypesEnum.CHARACTER.name())) {
                         errorList += "Invalid argument after closing paran. Line: " + currentLex.lineNum + "\n";
                     }
                     if (openParens.size() == 0) {
                         errorList += "Invalid closing paren on line: " + currentLex.lineNum + "\n";
                     } else {
                         openParens.remove(openParens.size() - 1);
                     }
                 } else if (currentLex.type.equals(LexicalAnalyzer.tokenTypesEnum.RELATIONAL_OPR.name())) {
                     validateRelationalOpr(currentLex, previousLex, nextLexi);
                 } else if (currentLex.type.equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_BEGIN.name())) {
                     openBlocks.add(currentLex);
                 } else if (currentLex.type.equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_END.name())) {
                     if (openBlocks.size() == 0) {
                         errorList += "Invalid closing block on line: " + currentLex.lineNum + "\n";
                     } else {
                         if (!scope.equals("g")) {
                             scope = scope.substring(0, scope.lastIndexOf('.'));
                         }
                         openBlocks.remove(openBlocks.size() - 1);
                         previousLex = currentLex;
                         i++;
                         continue;
                     }
                 } else if (currentLex.type.equals(LexicalAnalyzer.tokenTypesEnum.BOOLEAN_OPR.name())) {
                     validateBooleanOpr(currentLex, previousLex, nextLexi);
                 } else if (currentLex.type.equals(LexicalAnalyzer.tokenTypesEnum.IO_OPR.name())) {
                     validateIOOpr(currentLex, previousLex, nextLexi, peekPrevious);
                 } else if (currentLex.lexi.equals(",")) {
                     validateComma(currentLex, previousLex, nextLexi);
                 } else if (currentLex.lexi.equals(".")) {
                     if (i == 0) {
                         errorList += "Invalid dot operator. Line: " + currentLex.lineNum + "\n";
                         previousLex = currentLex;
                         continue;
                     }
                     if (!isLHSofDotValid(tempList, i - 1)) {
                         errorList += "LHS of dot operator is invalid. Line: " + currentLex.lineNum + "\n";
                         previousLex = currentLex;
                         continue;
                     }
                     if (!nextLexi.type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name()) || Character.isUpperCase(nextLexi.lexi.toCharArray()[0])) {
                         errorList += "RHS of dot operator is invalid. Line: " + currentLex.lineNum + "\n";
                         previousLex = currentLex;
                         continue;
                     }
                 } else if (currentLex.type.equals(LexicalAnalyzer.tokenTypesEnum.NUMBER.name())) {
                     addToSymbolTable("Literal", new ArrayList<String>(), "int", "public", "x" + currentLex.lexi, currentLex.lineNum);
                     previousLex = currentLex;
                     continue;
                 } else if (currentLex.type.equals(LexicalAnalyzer.tokenTypesEnum.CHARACTER.name())) {
                     addToSymbolTable("Literal", new ArrayList<String>(), "char", "public", currentLex.lexi, currentLex.lineNum);
                     previousLex = currentLex;
                     continue;
                 } else if (currentLex.lexi.equals("true") || currentLex.lexi.equals("false")) {
                     addToSymbolTable("Literal", new ArrayList<String>(), "bool", "public", currentLex.lexi, currentLex.lineNum);
                     previousLex = currentLex;
                     continue;
                 }
 
 
                 /**\
                  * validate keywords
                  */
                 if (currentLex.type.equals(LexicalAnalyzer.tokenTypesEnum.KEYWORD.name())) {
                     if (currentLex.lexi.equals("return")) {
                         if (!tempList.get(tempList.size() - 1).type.equals(LexicalAnalyzer.tokenTypesEnum.EOT.name())) {
                             errorList += "return statement must end with a end of line token (;). Line: " + currentLex.lineNum + "\n";
                         }
                         validateReturnStatement(currentLex, nextLexi);
                     }
 
                     if (currentLex.lexi.equals("true") || currentLex.lexi.equals("false")) {
                         if (tempList.get(i + 1).lexi.equals("(")) {
                             errorList += "'true' and 'false' are primitive values and cannot be used as a function. Line: " + currentLex.lineNum + "\n";
                             previousLex = currentLex;
                             continue;
                         }
                     }
 
                     if (currentLex.lexi.equals("class")) {
                         int errCount = errorList.length();
                         if (!tempList.get(0).lexi.equals("class")) {
                             errorList += "class declaration must be the first statement on the line. Line: " + currentLex.lineNum + "\n";
                         }
                         if (!tempList.get(tempList.size() - 1).type.equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_BEGIN.name())) {
                             errorList += "class declaration line must end with a begin block token ({). Line: " + currentLex.lineNum + "\n";
                         }
                         if (tempList.size() < 3) {
                             errorList += "Missing arguments in class declaration. Line: " + currentLex.lineNum + "\n";
                         } else if (!tempList.get(i + 1).type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name())) {
                             errorList += "class name must be an Identifier. Line: " + currentLex.lineNum + "\n";
                         } else if (!Character.isUpperCase(tempList.get(i + 1).lexi.toCharArray()[0])) {
                             errorList += "class name must start with an uppercase letter. Line: " + currentLex.lineNum + "\n";
                         }
                         if (errCount != errorList.length()) {
                             previousLex = currentLex;
                             continue;
                         }
                         addToSymbolTable("Class", new ArrayList<String>(), "", "", nextLexi.lexi, currentLex.lineNum);
                     }
 
                     /**
                      *  check for valid function, object and class declarations
                      **/
                     if (currentLex.lexi.equals("private") || currentLex.lexi.equals("public")) {
                         if (i != 0) {
                             errorList += "Modifiers must be at the beginning of function declarations. Line: " + currentLex.lineNum + "\n";
                         } else {
                             if (nextLexi.type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name()) && i == 0) {
                                 if (tempList.get(tempList.size() - 1).lexi.equals("{")) {
                                     if (tempList.get(i + 2).type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
                                         if (Character.isLowerCase(nextLexi.lexi.toCharArray()[0])) {
                                             errorList += "Constructor name must start with a capital letter. Line: " + currentLex.lineNum + "\n";
                                         }
 
                                         if (!tempList.get(i + 3).lexi.equals(")")) {
                                             paramIdList = new ArrayList<String>();
                                             if (!isValidParameterDeclarationType(tempList, 3)) {
                                                 errorList += "Constructor '" + tempList.get(1).lexi + "' contains an invalid argument list. Line: " + currentLex.lineNum + "\n";
                                             } else {
                                                 addToSymbolTable("Function", paramIdList, tempList.get(1).lexi, tempList.get(0).lexi, tempList.get(1).lexi, currentLex.lineNum);
                                             }
                                         } else {
                                             if (!tempList.get(i + 4).lexi.equals("{")) {
                                                 errorList += "Constructor '" + tempList.get(1).lexi + "' cannot contain anything between the parameter list and the start block. Line: " + currentLex.lineNum + "\n";
                                             }
 
                                             addToSymbolTable("Function", new ArrayList<String>(), tempList.get(1).lexi, tempList.get(0).lexi, tempList.get(1).lexi, currentLex.lineNum);
                                         }
                                         i += tempList.size() - 1;
                                         openBlocks.add(tempList.get(i));
                                         previousLex = tempList.get(i);
                                         continue;
                                     }
                                 }
                             }
 
                             if (tempList.size() < 4) {
                                 errorList += "There are to few arguments in declaration. Line: " + currentLex.lineNum + "\n";
                             } else {
                                 if (tempList.get(i + 1).lexi.equals("void") && tempList.get(i + 2).lexi.equals("main")) {
                                     if (!tempList.get(i + 3).type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
                                         errorList += "Main function is missing the argument list. Line: " + currentLex.lineNum + "\n";
                                         previousLex = currentLex;
                                         continue;
                                     } else {
                                         if (tempList.get(i + 4).type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                                             if (!tempList.get(i + 5).type.equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_BEGIN.name())) {
                                                 errorList += "Main function declaration must end with an block begin token ({). Line: " + currentLex.lineNum + "\n";
                                                 previousLex = currentLex;
                                                 continue;
                                             }
                                         } else {
                                             if (!isValidParameterDeclarationType(tempList, i + 4)) {
                                                 errorList += "Function 'main' contains an invalid argument list. Line: " + currentLex.lineNum + "\n";
                                             }
                                             paramIdList = new ArrayList<String>();
                                             iCodeList.get(0).setArg1("F_MAIN" + symIdInr);
                                             addToSymbolTable("Function", paramIdList, tempList.get(2).lexi, tempList.get(0).lexi, "main", currentLex.lineNum);
                                             previousLex = currentLex;
                                             continue;
                                         }
                                     }
                                     addToSymbolTable("Function", new ArrayList<String>(), tempList.get(2).lexi, tempList.get(0).lexi, "main", currentLex.lineNum);
                                     previousLex = currentLex;
                                     continue;
                                 }
                                 if (!isValidReturnType(tempList.get(1).lexi, tempList.get(i + 1).type)) {
                                     errorList += "Function or object requires a valid type. Line: " + currentLex.lineNum + "\n";
                                 } else {
                                     if (!isStepValid(tempList.get(2).type, tempList.get(2).lexi)) {
                                         errorList += "Missing function name or argument list(also check that functions and variables start with lower case letters). Line: " + currentLex.lineNum + "\n";
                                     } else if (tempList.get(2).type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name()) && !tempList.get(3).type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                                         paramIdList = new ArrayList<String>();
                                         if (!isValidParameterDeclarationType(tempList, 3)) {
                                             errorList += "Function '" + tempList.get(1).lexi + "' contains an invalid argument list. Line: " + currentLex.lineNum + "\n";
                                         } else {
                                             addToSymbolTable("Function", paramIdList, tempList.get(1).lexi, tempList.get(0).lexi, tempList.get(2).lexi, currentLex.lineNum);
                                         }
                                     } else if (tempList.get(2).type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name()) && tempList.get(3).type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name()) && tempList.get(4).type.equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_BEGIN.name())) {
                                         addToSymbolTable("Function", new ArrayList<String>(), tempList.get(1).lexi, tempList.get(0).lexi, tempList.get(1).lexi, currentLex.lineNum);
                                         // all good
                                     } else if (tempList.get(3).type.equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_BEGIN.name())) {
                                         if (!tempList.get(4).type.equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_END.name())) {
                                             errorList += "invalid array declaration. Line: " + currentLex.lineNum + "\n";
                                         } else {
                                             addToSymbolTable("@:", new ArrayList<String>(), tempList.get(1).lexi, tempList.get(0).lexi, tempList.get(2).lexi, currentLex.lineNum);
                                         }
                                     } else {
                                         if (tempList.get(3).type.equals(LexicalAnalyzer.tokenTypesEnum.EOT.name())) {
                                             addToSymbolTable("ivar", new ArrayList<String>(), tempList.get(1).lexi, tempList.get(0).lexi, tempList.get(2).lexi, currentLex.lineNum);
                                             // all good
                                         } else if (tempList.get(3).type.equals(LexicalAnalyzer.tokenTypesEnum.ASSIGNMENT_OPR.name())) {
                                             addToSymbolTable("ivar", new ArrayList<String>(), tempList.get(1).lexi, tempList.get(0).lexi, tempList.get(2).lexi, currentLex.lineNum);
                                             // all good
                                         } else if (tempList.get(3).type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name()) && !tempList.get(4).type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                                             paramIdList = new ArrayList<String>();
                                             if (!isValidParameterDeclarationType(tempList, 4)) {
                                                 errorList += "Function '" + tempList.get(2).lexi + "' contains an invalid argument list. Line: " + currentLex.lineNum + "\n";
                                             } else {
                                                 addToSymbolTable("Function", paramIdList, tempList.get(1).lexi, tempList.get(0).lexi, tempList.get(2).lexi, currentLex.lineNum);
                                             }
                                         } else if (tempList.get(3).type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name()) && tempList.get(4).type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name()) && tempList.get(5).type.equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_BEGIN.name())) {
                                             addToSymbolTable("Function", new ArrayList<String>(), tempList.get(1).lexi, tempList.get(0).lexi, tempList.get(2).lexi, currentLex.lineNum);
                                             // all good
                                         } else {
                                             if (tempList.get(i + 3).lexi.equals("[")) {
                                                 // arrays are handled below
                                             } else {
                                                 errorList += "Function or object has been incorrectly formatted. Line: " + currentLex.lineNum + "\n";
                                             }
                                         }
                                     }
                                 }
                             }
                         }
                     }
 
                     /**
                      * validate new operator
                      */
                     if (currentLex.lexi.equals("new")) {
                         if (i == 0) {
                             errorList += "New must be used as an assignment. Line: " + currentLex.lineNum + "\n";
                             previousLex = currentLex;
                             continue;
                         }
 
                         if (tempList.get(i - 1).lexi.equals("(") || tempList.get(i - 1).lexi.equals(",") || tempList.get(i - 1).lexi.equals("=") || tempList.get(i - 1).type.equals(LexicalAnalyzer.tokenTypesEnum.RELATIONAL_OPR.name())) {
 
                             if (!isValidObjectTypeExpression(nextLexi)) {
                                 if (nextLexi.type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name()) && Character.isLowerCase(nextLexi.lexi.toCharArray()[0])) {
                                     errorList += "Object must start with a capital letter. Line: " + currentLex.lineNum + "\n";
                                 }
                                 errorList += "Invalid use of the new operator. Line: " + currentLex.lineNum + "\n";
                                 previousLex = currentLex;
                                 continue;
                             }
 
                             // handle arrays
                             if (tempList.get(i + 2).type.equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_BEGIN.name())) {
 
                                 if (tempList.get(i + 3).type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name()) || tempList.get(i + 3).type.equals(LexicalAnalyzer.tokenTypesEnum.NUMBER.name())) {
                                     if (!tempList.get(i + 4).type.equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_END.name())) {
                                         // check for post to pre
                                     } else {
 
                                         if (tempList.get(i + 5).type.equals(LexicalAnalyzer.tokenTypesEnum.EOT.name()) && (i + 5) == tempList.size() - 1) {
                                             i += 4;
                                             previousLex = currentLex;
                                             continue;
                                         }
                                     }
 
                                 } else {
                                     errorList += "Invalid array initialization. Line: " + currentLex.lineNum + "\n";
                                     previousLex = currentLex;
                                     continue;
                                 }
 
                                 // handle objects
                             } else if (tempList.get(i + 2).type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
                                 if (!tempList.get(i + 3).type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                                     if (!isValidCalledParameterType(tempList, i + 3)) {
                                         errorList += "Function '" + tempList.get(i + 1).lexi + "' contains an invalid argument list. Line: " + currentLex.lineNum + "\n";
                                         previousLex = currentLex;
                                         continue;
                                     } else {
                                         // all good
                                     }
                                 }
 
                             } else {
                                 errorList += "Invalid object initialization. Line: " + currentLex.lineNum + "\n";
                                 previousLex = currentLex;
                                 continue;
                             }
                         } else {
                             errorList += "New must be used as an assignment. Line: " + currentLex.lineNum + "\n";
                             previousLex = currentLex;
                             continue;
                         }
                     }
 
                     /**
                      * validate itoa and atoi
                      */
                     if (currentLex.lexi.equals("atoi")) {
                         if (nextLexi.type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
                             if (tempList.get(i + 2).type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name()) && Character.isLowerCase(tempList.get(i + 2).lexi.toCharArray()[0])) {
                                 if (!tempList.get(i + 3).type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                                     errorList += "Incorrect parameter list for atoi function. Line: " + currentLex.lineNum + "\n";
                                     previousLex = currentLex;
                                     continue;
                                 }
                             } else {
                                 errorList += "Incorrect parameter list for atoi function. Line: " + currentLex.lineNum + "\n";
                                 previousLex = currentLex;
                                 continue;
                             }
                         } else {
                             errorList += "Incorrect parameter list for atoi function. Line: " + currentLex.lineNum + "\n";
                             previousLex = currentLex;
                             continue;
                         }
                     }
                     if (currentLex.lexi.equals("itoa")) {
                         if (nextLexi.type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
                             if (!tempList.get(i + 2).lexi.equals(")")) {
                                 if (!isValidCalledParameterTypeITOA(tempList, i + 2)) {
                                     errorList += "Incorrect parameter list for atoi function. Line: " + currentLex.lineNum + "\n";
                                     previousLex = currentLex;
                                     continue;
                                 }
                                 if (tempList.get(i + 3).lexi.equals(",") && isValidCalledParameterTypeITOA(tempList, i + 4) && tempList.get(i + 5).lexi.equals(",") && isValidCalledParameterTypeITOALast(tempList, i + 6)) {
                                     // all good
                                 } else {
                                     errorList += "Incorrect parameter list for atoi function. Line: " + currentLex.lineNum + "\n";
                                     previousLex = currentLex;
                                     continue;
                                 }
                             }
                         } else {
                             errorList += "Incorrect parameter list for atoi function. Line: " + currentLex.lineNum + "\n";
                             previousLex = currentLex;
                             continue;
                         }
                     }
 
                     /**
                      * validate if statement
                      */
                     if (currentLex.lexi.equals("if")) {
                         if (!tempList.get(i + 1).type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
                             errorList += "Invalid if statement argument. Line: " + currentLex.lineNum + "\n";
                             previousLex = currentLex;
                             continue;
                         }
 
                         if (tempList.get(i + 2).type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                             errorList += "Missing if statement arguments. Line: " + currentLex.lineNum + "\n";
                             previousLex = currentLex;
                             continue;
                         }
 
                         if (!isValidRelationParameterType(tempList, i + 2)) {
                             errorList += "If statement contains an invalid argument list. Line: " + currentLex.lineNum + "\n";
                             previousLex = currentLex;
                             continue;
                         }
 
                         // todo: area to change is allowing single line no brakit
                         if (!tempList.get(tempList.size() - 1).lexi.equals("{")) {
                             errorList += "If statement blocks must be contained within block brackets '{}'. Line: " + currentLex.lineNum + "\n";
                             previousLex = currentLex;
                             continue;
                         }
 
                         ifList.add("if" + symIdInr);
                         addToSymbolTable("Condition", new ArrayList<String>(), "", "private", "if" + symIdInr, currentLex.lineNum);
                         previousLex = currentLex;
                         continue;
                     }
 
                     if (currentLex.lexi.equals("else")) {
 
                         if (tempList.get(i + 1).lexi.equals("if")) {
                             if (!tempList.get(i + 2).type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
                                 errorList += "Invalid if statement argument. Line: " + currentLex.lineNum + "\n";
                                 previousLex = currentLex;
                                 continue;
                             }
 
                             if (tempList.get(i + 3).type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                                 errorList += "Missing if statement arguments. Line: " + currentLex.lineNum + "\n";
                                 previousLex = currentLex;
                                 continue;
                             }
 
                             if (!isValidRelationParameterType(tempList, i + 3)) {
                                 errorList += "If statement contains an invalid argument list. Line: " + currentLex.lineNum + "\n";
                                 previousLex = currentLex;
                                 continue;
                             }
                             previousLex = currentLex;
                             continue;
                         }
 
                         if (!tempList.get(i + 1).type.equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_BEGIN.name())) {
                             errorList += "Else statement blocks must be contained within block brackets '{}'. Line: " + currentLex.lineNum + "\n";
                             previousLex = currentLex;
                             continue;
                         }
 
                         ifList.add("else" + symIdInr);
                         addToSymbolTable("Condition", new ArrayList<String>(), "", "private", "else" + symIdInr, currentLex.lineNum);
                         previousLex = currentLex;
                         continue;
                     }
 
                     if (currentLex.lexi.equals("while")) {
                         if (!tempList.get(i + 1).type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
                             errorList += "Invalid while argument statement. Line: " + currentLex.lineNum + "\n";
                             previousLex = currentLex;
                             continue;
                         }
 
                         if (tempList.get(i + 2).type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                             errorList += "Missing while statement arguments. Line: " + currentLex.lineNum + "\n";
                             previousLex = currentLex;
                             continue;
                         }
 
                         if (!isValidRelationParameterType(tempList, i + 2)) {
                             errorList += "While statement contains an invalid argument list. Line: " + currentLex.lineNum + "\n";
                             previousLex = currentLex;
                             continue;
                         }
 
                         if (!tempList.get(tempList.size() - 1).lexi.equals("{")) {
                             errorList += "While statement blocks must be contained within block brackets '{}'. Line: " + currentLex.lineNum + "\n";
                             previousLex = currentLex;
                             continue;
                         }
 
                         ifList.add("while" + symIdInr);
                         addToSymbolTable("Condition", new ArrayList<String>(), "", "private", "while" + symIdInr, currentLex.lineNum);
                         previousLex = currentLex;
                         continue;
                     }
 
                 }
 
 
                 /**
                  * validate object type declaration
                  */
                 if (currentLex.type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name()) && i == 0) {
                     if (tempList.get(tempList.size() - 1).lexi.equals("{")) {
                         if (tempList.get(i + 1).type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
                             if (Character.isLowerCase(currentLex.lexi.toCharArray()[0])) {
                                 errorList += "Constructor name must start with a capital letter. Line: " + currentLex.lineNum + "\n";
                             }
 
                             if (!tempList.get(i + 2).lexi.equals(")")) {
                                 if (!isValidParameterDeclarationType(tempList, 2)) {
                                     errorList += "Constructor '" + tempList.get(0).lexi + "' contains an invalid argument list. Line: " + currentLex.lineNum + "\n";
                                 }
 
                                 if (tempList.get(tempList.size() - 1).lexi.equals("{") && tempList.get(tempList.size() - 2).lexi.equals(")")) {
                                     // all good
                                 } else {
                                     errorList += "Constructor '" + tempList.get(0).lexi + "' cannot contain anything between the parameter list and the start block. Line: " + currentLex.lineNum + "\n";
                                 }
 
                                 paramIdList = new ArrayList<String>();
                                 if (!isValidParameterDeclarationType(tempList, i + 3)) {
                                     errorList += "Constructor '" + tempList.get(0).lexi + "' contains an invalid argument list. Line: " + currentLex.lineNum + "\n";
                                 } else {
                                     addToSymbolTable("Function", paramIdList, tempList.get(0).lexi, "private", tempList.get(0).lexi, currentLex.lineNum);
                                 }
                             } else {
                                 if (!tempList.get(i + 3).lexi.equals("{")) {
                                     errorList += "Constructor '" + tempList.get(0).lexi + "' cannot contain anything between the parameter list and the start block. Line: " + currentLex.lineNum + "\n";
                                 }
 
                                 addToSymbolTable("Function", new ArrayList<String>(), tempList.get(0).lexi, "private", tempList.get(0).lexi, currentLex.lineNum);
                             }
                             i += tempList.size() - 1;
                             openBlocks.add(tempList.get(i));
                             previousLex = tempList.get(i);
                             continue;
                         }
                     }
                 }
 
                 if (i == 0 && (currentLex.lexi.equals("void") || currentLex.lexi.equals("int") || currentLex.lexi.equals("char") || currentLex.lexi.equals("bool") || (currentLex.type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name()) && Character.isUpperCase(currentLex.lexi.toCharArray()[0])))) {
                     if (tempList.size() < 3) {
                         errorList += "Incorrect format, too few arguments. Line: " + currentLex.lineNum + "\n";
                         previousLex = currentLex;
                         continue;
                     }
                     if (tempList.size() < 3) {
                         errorList += "Missing arguments in type declaration. Line: " + currentLex.lineNum + "\n";
                     } else {
                         if (tempList.get(i + 1).type.equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_BEGIN.name())) {
                             if (!tempList.get(i + 2).type.equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_END.name())) {
                                 errorList += "Incorrectly defined array. Line: " + currentLex.lineNum + "\n";
                                 previousLex = currentLex;
                                 continue;
                             }
                             if (tempList.get(i + 3).type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name()) && Character.isLowerCase(tempList.get(i + 3).lexi.toCharArray()[0])) {
                                 if (tempList.get(i + 4).type.equals(LexicalAnalyzer.tokenTypesEnum.EOT.name()) || tempList.get(i + 4).type.equals(LexicalAnalyzer.tokenTypesEnum.ASSIGNMENT_OPR.name())) {
                                     addToSymbolTable("@:", new ArrayList<String>(), tempList.get(0).lexi, "private", tempList.get(3).lexi, currentLex.lineNum);
                                     // all good
                                 } else {
                                     errorList += "Incorrectly defined array. Line: " + currentLex.lineNum + "\n";
                                     previousLex = currentLex;
                                     continue;
                                 }
                             }
                         } else if (tempList.get(i + 1).type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name()) && Character.isLowerCase(tempList.get(i + 1).lexi.toCharArray()[0])) {
                             if (tempList.get(i + 2).type.equals(LexicalAnalyzer.tokenTypesEnum.EOT.name()) || tempList.get(i + 2).type.equals(LexicalAnalyzer.tokenTypesEnum.ASSIGNMENT_OPR.name())) {
                                 addToSymbolTable("lvar", new ArrayList<String>(), tempList.get(0).lexi, "private", tempList.get(1).lexi, currentLex.lineNum);
                                 // all good
                             } else if (tempList.get(i + 2).type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
                                 if (!tempList.get(i + 3).type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                                     paramIdList = new ArrayList<String>();
                                     if (!isValidParameterDeclarationType(tempList, i + 3)) {
                                         errorList += "Function '" + tempList.get(1).lexi + "' contains an invalid argument list. Line: " + currentLex.lineNum + "\n";
                                     } else {
                                         addToSymbolTable("Function", paramIdList, tempList.get(0).lexi, "private", tempList.get(1).lexi, currentLex.lineNum);
                                     }
                                 } else if (tempList.get(i + 3).type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name()) && (tempList.get(i + 4).type.equals(LexicalAnalyzer.tokenTypesEnum.EOT.name()) || tempList.get(i + 4).type.equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_BEGIN.name()))) {
                                     addToSymbolTable("Function", new ArrayList<String>(), tempList.get(0).lexi, "private", tempList.get(1).lexi, currentLex.lineNum);
                                     // all good
                                 } else {
                                     errorList += "Incorrect function definition format. Line: " + currentLex.lineNum + "\n";
                                 }
                             } else if (tempList.get(2).type.equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_BEGIN.name())) {
                                 if (!tempList.get(3).type.equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_END.name())) {
                                     errorList += "invalid array declaration. Line: " + currentLex.lineNum + "\n";
                                 } else {
                                     addToSymbolTable("@:", new ArrayList<String>(), tempList.get(0).lexi, "private", tempList.get(1).lexi, currentLex.lineNum);
                                 }
                             } else {
                                 errorList += "Incorrectly formatted object definition. Line: " + currentLex.lineNum + "\n";
                             }
                         } else {
                             errorList += "Incorrectly defined object. Line: " + currentLex.lineNum + "\n";
                         }
                     }
                 }
 
                 /**
                  * validates function and variable use
                  */
                 if (currentLex.type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name()) && i == 0 && Character.isLowerCase(currentLex.lexi.toCharArray()[0])) {
                     if (tempList.get(i + 1).type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name())) {
                         if (Character.isLowerCase(currentLex.lexi.toCharArray()[0])) {
                             errorList += "Variable type must either be a pre-defined type or start with a capital letter. Line: " + currentLex.lineNum + "\n";
                             previousLex = currentLex;
                             continue;
                         }
                     }
 
                     if (tempList.size() < 4) {
                         errorList += "Too few arguments. Line: " + currentLex.lineNum + "\n";
                         previousLex = currentLex;
                         continue;
                     }
                     if (tempList.get(i + 1).type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
                         if (!tempList.get(i + 2).type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                             if (!isValidCalledParameterType(tempList, i + 2)) {
                                 errorList += "Function '" + tempList.get(0).lexi + "' contains an invalid argument list. Line: " + currentLex.lineNum + "\n";
                                 previousLex = currentLex;
                                 continue;
                             }
                         } else if (tempList.get(i + 2).type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name()) && tempList.get(i + 3).type.equals(LexicalAnalyzer.tokenTypesEnum.EOT.name())) {
                             // all good
                         } else {
                             if (tempList.get(i + 1).lexi.equals("[")) {
                                 // arrays are handled else where
                             } else {
                                 errorList += "Syntax error on function call format. Line: " + currentLex.lineNum + "\n";
                                 previousLex = currentLex;
                                 continue;
                             }
                         }
                     } else if (tempList.get(i + 1).type.equals(LexicalAnalyzer.tokenTypesEnum.ASSIGNMENT_OPR.name())) {
                         // all good
                     } else if (tempList.get(i + 1).type.equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_BEGIN.name())) {
                         if (tempList.size() < 6) {
                             errorList += "Too few arguments. Line: " + currentLex.lineNum + "\n";
                             previousLex = currentLex;
                             continue;
                         }
                         if (tempList.get(i + 2).type.equals(LexicalAnalyzer.tokenTypesEnum.NUMBER.name()) || tempList.get(i + 2).type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name()) && (tempList.get(i + 3).type.equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_END.name()) && tempList.get(i + 4).type.equals(LexicalAnalyzer.tokenTypesEnum.ASSIGNMENT_OPR.name()))) {
                             // all good
                         } else {
                             if (tempList.get(i + 1).lexi.equals("[")) {
                                 // arrays are handled else where
                             } else {
                                 errorList += "Syntax error on function call format. Line: " + currentLex.lineNum + "\n";
                                 previousLex = currentLex;
                                 continue;
                             }
                         }
                     } else if (tempList.get(i + 1).lexi.equals(".") && (tempList.get(i + 2).type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name()) && Character.isLowerCase(tempList.get(i + 2).lexi.toCharArray()[0]))) {
                         if (tempList.get(i + 3).type.equals(LexicalAnalyzer.tokenTypesEnum.ASSIGNMENT_OPR.name())) {
                             // all good
                         }
                     }
                 }
                 previousLex = currentLex;
             }
 
         }
 
         if (openParens.size() > 0) {
             String errorMessage = "Incomplete statements (missing closing parens) on the following lines: ";
 
             for (Tuple item : openParens) {
                 errorMessage += item.lineNum + ", ";
             }
             errorList += errorMessage + "\n";
         }
 
         if (openBlocks.size() > 0) {
             String errorMessage = "Incomplete statements (missing closing blocks) on the following lines: ";
 
             for (Tuple item : openBlocks) {
                 errorMessage += item.lineNum + ", ";
             }
             errorList += errorMessage + "\n";
         }
 
         try {
             if (!errorList.isEmpty()) {
                 throw new IllegalArgumentException(errorList);
             }
         } catch (IllegalArgumentException e) {
             System.out.print(e.getMessage());
             System.exit(0);
         }
 
         /**
          *  pass two
          **/
         lexicalAnalyzer.resetList();
         String scopePassTwo = "g";
 
 
         while (lexicalAnalyzer.hasNext()) {
             // setting up items to be parsed
             Tuple temp;
             List<Tuple> tempList = new ArrayList<Tuple>();
 
             Stack<SAR> SAS = new Stack<SAR>();
             Stack<SAR> OS = new Stack<SAR>();
 
             temp = lexicalAnalyzer.getNext();
             while (canAddToList(temp)) {
                 tempList.add(temp);
                 temp = lexicalAnalyzer.getNext();
             }
             tempList.add(temp);
 
             if (tempList.isEmpty()) {
                 continue;
             }
 
             int lastOprPrecedence = 0;
 
             // evaluate semantics
             int i = 0;
             boolean isCalled = false;
 
             while (i < tempList.size()) {
                 Tuple item = tempList.get(i);
                 // setup scope
                 if (tempList.get(i).type.equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_BEGIN.name())) {
                     openBlocks.add(tempList.get(i));
                     if (tempList.get(0).lexi.equals("public") || tempList.get(0).lexi.equals("private")) {
                         if (!tempList.get(2).type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
                             scopePassTwo += "." + tempList.get(2).lexi;
                         } else {
                             scopePassTwo += "." + tempList.get(1).lexi;
                         }
                     } else if (tempList.get(0).lexi.trim().equals("if") || tempList.get(0).lexi.trim().equals("else") || tempList.get(0).lexi.trim().equals("while")) {
 
                         if (tempList.get(0).lexi.trim().equals("else") && tempList.get(1).lexi.trim().equals("if")) {
                             condTypeScope = "." + tempList.get(1).lexi + symIdInr;
                             scopePassTwo += "." + tempList.get(1).lexi + symIdInr++;
                         } else {
                             condTypeScope = "." + tempList.get(0).lexi + symIdInr;
                             scopePassTwo += "." + tempList.get(0).lexi + symIdInr++;
                         }
 
                     } else {
                         if (isValidReturnType(tempList.get(0).lexi, tempList.get(0).type) && tempList.get(1).lexi.equals("(")) {
                             scopePassTwo += "." + tempList.get(0).lexi;
                         } else if (isValidReturnType(tempList.get(0).lexi, tempList.get(0).type) || tempList.get(0).lexi.equals("class")) {
                             scopePassTwo += "." + tempList.get(1).lexi;
                         }
                     }
                 }
 
                 if (item.type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name()) && (tempList.get(0).lexi.equals("public") || tempList.get(0).lexi.equals("private") || isValidReturnType(tempList.get(0).lexi, tempList.get(0).type))) {
                     if (tempList.get(1).type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name()) && Character.isUpperCase(tempList.get(1).lexi.toCharArray()[0])) {
                         scopePassTwo += "." + tempList.get(1).lexi;
                     } else {
                         scopePassTwo += "." + tempList.get(2).lexi;
                     }
                 }
 
                 if (item.lexi.equals(".")) {
                     isCalled = true;
                     i++;
                     continue;
                 }
 
                 if (item.lexi.equals("if")) {
                     eIndex = i + 1;
                     Tuple tempItem = new Tuple(ifList.remove(), item.type, item.lineNum);
                     SAR controlSar = new SAR(tempItem, scopePassTwo, "", "");
                     if (!iExist(controlSar, SAS, tempList, scopePassTwo, OS)) {
                         errorList += "invalid if statement. Line: " + item.lineNum + "\n";
                     }
                     validateRelationalParametersSemantic(tempList, SAS, OS, scopePassTwo, tempItem, 0, controlSar.getKey(), "SKIPIF");
                     i = eIndex + 1;
                     continue;
                 } else if (item.lexi.equals("else")) {
                     Tuple tempItem = new Tuple(ifList.remove(), item.type, item.lineNum);
                     SAR controlSar = new SAR(tempItem, scopePassTwo, "", "");
                     canPop = true;
                     if (!iExist(controlSar, SAS, tempList, scopePassTwo, OS)) {
                         errorList += "invalid if statement. Line: " + item.lineNum + "\n";
                     }
                     eIndex = i + 1;
 
                     condLabel = "SKIPELSE" + controlSar.getKey();
                     iCodeList.add(new ICode("", "JMP", "SKIPELSE" + controlSar.getKey(), "", "", ""));
 
                     if (ifStack.size() > 0 && ifStack.peek().startsWith("SKIPELSEIF")) {
                         String replaceLabel = ifStack.peek();
 
                         for (int u = iCodeList.size() - 1; u >= 0; u--) {
                             if (iCodeList.get(u).getArg1().equals((replaceLabel))) {
                                 iCodeList.get(u).setArg1("SKIPELSE" + controlSar.getKey());
                             }
                         }
                     }
 
                     if (tempList.get(i + 1).lexi.equals("if")) {
                         eIndex = i + 2;
 
                         iCodeList.get(iCodeList.size() - 1).setArg1("SKIPELSEIF" + controlSar.getKey());
 
                         validateRelationalParametersSemantic(tempList, SAS, OS, scopePassTwo, tempItem, 0, controlSar.getKey(), "SKIPELSEIF");
                         i = eIndex + 1;
                         continue;
                     }
 
                     eIndex = i;
 
                     ifStack.push("SKIPELSE" + controlSar.getKey());
                     i++;
                     continue;
 
                 } else if (item.lexi.equals("while")) {
                     eIndex = i + 1;
                     Tuple tempItem = new Tuple(ifList.remove(), item.type, item.lineNum);
                     SAR controlSar = new SAR(tempItem, scopePassTwo, "", "");
 
                     if (!iExist(controlSar, SAS, tempList, scopePassTwo, OS)) {
                         errorList += "invalid while statement. Line: " + item.lineNum + "\n";
                     }
 
 
                     if (!iCodeList.isEmpty()) {
                         if (iCodeList.get(iCodeList.size() - 1).getArg1().contains("BEGINWHILE")) {
                             String tempArg = iCodeList.get(iCodeList.size() - 1).getArg1();
                             String removeLabel = "ENDWHILE" + tempArg.substring(tempArg.length() - 4, tempArg.length());
 
                             for (ICode cd : iCodeList) {
                                 if (cd.getArg2().equals(removeLabel)) {
                                     cd.setArg2("BEGINWHILE" + controlSar.getKey());
                                 }
                                 if (cd.getArg1().equals(removeLabel)) {
                                     cd.setArg1("BEGINWHILE" + controlSar.getKey());
                                 }
                             }
                             whileStack.pop();
                         }
                     }
 
                     whileStack.push("BEGINWHILE" + controlSar.getKey());
                     validateRelationalParametersSemanticWhile(tempList, SAS, OS, scopePassTwo, tempItem, 0, controlSar.getKey(), 0, 0);
                     i = eIndex + 1;
                     continue;
                 }
 
                 if (item.lexi.equals("return")) {
                     String returnScope = scopePassTwo;
 
                     String sub = returnScope.substring(returnScope.lastIndexOf("."), returnScope.length());
                     while (sub.contains("if") || sub.contains("else") || sub.contains("while")) {
                         returnScope = returnScope.substring(0, returnScope.lastIndexOf(sub));
                         sub = returnScope.substring(returnScope.lastIndexOf("."), returnScope.length());
                     }
 
                     if (!returnScope.contains(".")) {
                         errorList += "invalid return statement, cannot call return in base scope. Line: " + item.lineNum + "\n";
                         i++;
                         continue;
                     }
 
                     String returnTypeName = returnScope.substring(returnScope.lastIndexOf(".") + 1, returnScope.length());
                     String returnTypeScope = returnScope.substring(0, returnScope.lastIndexOf("."));
                     String returnType = "";
 
                     // find return type
                     boolean found = false;
                     while (!returnTypeScope.isEmpty()) {
                         for (String key : symbolTable.keySet()) {
                             Symbol s = symbolTable.get(key);
                             if (returnTypeScope.equals("g")) {
                                 if (s.getValue().equals(returnTypeName) && !(s.getData() instanceof ClassData)) {
                                     if (s.getData() instanceof VaribleData) {
                                         returnType = ((VaribleData) s.getData()).getType();
                                         found = true;
                                     } else if (s.getData() instanceof FunctionData) {
                                         returnType = ((FunctionData) s.getData()).getReturnType();
                                         found = true;
                                     }
                                 }
                             }
 
                             if (s.getValue().equals(returnTypeName) && s.getScope().equals(returnTypeScope) && !(s.getData() instanceof ClassData)) {
                                 if (s.getData() instanceof VaribleData) {
                                     returnType = ((VaribleData) s.getData()).getType();
                                     found = true;
                                 } else if (s.getData() instanceof FunctionData) {
                                     returnType = ((FunctionData) s.getData()).getReturnType();
                                     found = true;
                                 }
                             }
                             if (found) break;
                         }
 
                         if (found) break;
 
                         if (returnTypeScope.contains(".")) {
                             returnTypeScope = returnTypeScope.substring(0, returnTypeScope.lastIndexOf("."));
                         } else {
                             returnTypeScope = "";
                         }
                     }
                     SAS.push(new SAR(item, scopePassTwo, returnType, ""));
 
                 }
 
                 if (isLiteralExpression(tempList.get(i))) {
                     addLiteralExpressionToSAS(scopePassTwo, tempList.get(i), SAS);
                 } else if (isIdentifierExpression(tempList.get(i))) {
                     SAR sar = new SAR(tempList.get(i), scopePassTwo, "", "");
                     if (isCalled) {
                         eIndex = i;
                         rExist(sar, SAS, tempList, scopePassTwo, OS);
                         i = eIndex + 1;
 
                         if (tempList.get(i).lexi.equals(";") || tempList.get(i).lexi.equals("{")) {
                             cleanupSAS(SAS, OS, scopePassTwo);
                             i++;
                             continue;
                         }
 
                         if (tempList.get(i + 1).lexi.equals(".")) {
                             i++;
                             continue;
                         } else {
                             isCalled = false;
                             i++;
                             continue;
                         }
                     }
                     eIndex = i;
                     if (iExist(sar, SAS, tempList, scopePassTwo, OS)) {
                         if (sar.getType().equals("main")) {
                             iCodeList.get(0).setArg1(sar.getKey());
                             iCodeList.add(new ICode(sar.getKey(), "MAINST", sar.getKey(), "this", "", ""));
                         }
                         SAS.push(sar);
                     } else {
 
                         System.out.print(errorList);
                         System.exit(0);
                     }
                     i = eIndex;
                 } else if (isExpressionZ(tempList.get(i))) {
                     int precedence = setPrecedence(tempList.get(i).lexi);
 
                     if (item.lexi.equals(")")) {
                         while (!OS.peek().getLexi().lexi.equals("(")) {
                             if (OS.peek() == null) {
                                 errorList += "Missing opening paren. Line: " + item.lineNum + "\n";
                             }
                             addTempToSAS(OS.pop(), SAS, scopePassTwo, false);
                         }
                         if (tempList.get(0).lexi.equals("public") || tempList.get(0).lexi.equals("private") || isValidReturnType(tempList.get(0).lexi, tempList.get(0).type) || tempList.get(0).lexi.equals("return")) {
                             scopePassTwo = scopePassTwo.substring(0, scopePassTwo.lastIndexOf("."));
                         }
 
                         if (!OS.isEmpty()) {
                             OS.pop();
                         }
 
                     } else if (item.lexi.equals("]")) {
                         while (!OS.peek().getLexi().lexi.equals("[")) {
                             if (OS.peek() == null) {
                                 errorList += "Missing opening array. Line: " + item.lineNum + "\n";
                             }
                             addTempToSAS(OS.pop(), SAS, scopePassTwo, false);
                         }
                         if (!OS.isEmpty()) {
                             OS.pop();
                         }
                     } else if (precedence > lastOprPrecedence) {
                         pushOS(scopePassTwo, tempList, OS, i, item, SAS, scopePassTwo);
                         i = eIndex;
                         lastOprPrecedence = precedence;
                     } else if (precedence <= lastOprPrecedence) {
                         if (!OS.isEmpty()) {
                             if (!OS.peek().getLexi().lexi.equals("(")) {
                                 addTempToSAS(OS.pop(), SAS, scopePassTwo, false);
                             }
                         }
                         pushOS(scopePassTwo, tempList, OS, i, item, SAS, scopePassTwo);
                         i = eIndex;
                         lastOprPrecedence = precedence;
                     } else {
                         if (item.type.equals(LexicalAnalyzer.tokenTypesEnum.EOT.name())) {
                             cleanupSAS(SAS, OS, scopePassTwo);
                             i++;
                             continue;
                         }
 
                         if (item.lexi.equals("(") || item.lexi.equals("[")) {
                             lastOprPrecedence = 0;
                         } else {
                             lastOprPrecedence = precedence;
                         }
                     }
                 } else if (item.type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name()) && Character.isUpperCase(item.lexi.toCharArray()[0]) && tempList.get(tempList.size() - 1).lexi.equals(";")) {
                     if (!ClassExist(item, scopePassTwo)) {
                         errorList += "Class does not exist in scope. Line: " + item.lineNum + "\n";
                     }
                 } else if (isStreamOpr(item.lexi)) {
                     if (item.lexi.equals("cout")) {
                         if (i + 1 == tempList.size() - 1) {
                             errorList += "'cout' must have an int or char passed to it via '<<'. Line: " + item.lineNum + "\n";
                             i++;
                             continue;
                         }
 
                         if (!tempList.get(i + 1).lexi.equals("<<")) {
                             errorList += "cout must be followed with the extraction operator. Line: " + item.lineNum + "\n";
                             i++;
                             continue;
                         }
                     }
 
                     if (item.lexi.equals("cin")) {
                         if (i + 1 == tempList.size() - 1) {
                             errorList += "'cin' must have an int or char passed to it via '<<'. Line: " + item.lineNum + "\n";
                             i++;
                             continue;
                         }
 
                         if (!tempList.get(i + 1).lexi.equals(">>")) {
                             errorList += "cin must be followed with the insertion operator. Line: " + item.lineNum + "\n";
                             i++;
                             continue;
                         }
                     }
 
                 } else if (item.type.equals(LexicalAnalyzer.tokenTypesEnum.EOT.name()))
                     cleanupSAS(SAS, OS, scopePassTwo);
 
                 // take down scope
                 if (tempList.get(i).type.equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_END.name())) {
                     if (openBlocks.size() == 0) {
                         errorList += "Invalid closing block on line: " + tempList.get(i).lineNum + "\n";
                     } else {
                         if (!scopePassTwo.equals("g")) {
 
                             String subScope = scopePassTwo.substring(scopePassTwo.lastIndexOf("."), scopePassTwo.length());
                             if ((subScope.startsWith(".if") && Character.isDigit(subScope.toCharArray()[3])) || (subScope.startsWith(".else") && Character.isDigit(subScope.toCharArray()[5])) || (subScope.startsWith(".while") && Character.isDigit(subScope.toCharArray()[6]))) {
                                 if ((subScope.startsWith(".while") && Character.isDigit(subScope.toCharArray()[6]))) {
 
                                     if (whileStack.size() > 1) {
 
                                         String tempItem = whileStack.pop();
                                         iCodeList.add(new ICode("", "JMP", whileStack.pop(), "", "", ""));
                                         whileStack.push(tempItem);
 
                                         LexicalAnalyzer tempLex = new LexicalAnalyzer();
                                         tempLex.setLexicalList(lexicalAnalyzer.getLexicalList());
                                         tempLex.setLexPtr(lexicalAnalyzer.getLexPtr());
 
                                         Tuple lexC = null;
                                         if (tempLex.hasNext()) {
                                             lexC = tempLex.getNext();
                                         }
 
                                         if (lexC != null && lexC.lexi.equals("}")) {
                                             if (whileStack.size() > 1) {
                                                 String itemToBeReplaced = whileStack.pop();
 
                                                 if (!whileStack.isEmpty()) {
 
                                                     String tempHolder = whileStack.pop();
                                                     String replace = whileStack.peek();
                                                     whileStack.push(tempHolder);
 
                                                     for (int u = iCodeList.size() - 1; u >= 0; u--) {
                                                         if (iCodeList.get(u).getArg2().equals((itemToBeReplaced))) {
                                                             iCodeList.get(u).setArg2(replace);
                                                         }
                                                     }
                                                 }
                                             } else {
                                                 useConditionInReturnWhile = true;
                                             }
                                         }
                                     } else if (whileStack.size() == 1) {
                                         LexicalAnalyzer tempLex = new LexicalAnalyzer();
                                         tempLex.setLexicalList(lexicalAnalyzer.getLexicalList());
                                         tempLex.setLexPtr(lexicalAnalyzer.getLexPtr());
 
                                         Tuple lexC = null;
                                         if (tempLex.hasNext()) {
                                             lexC = tempLex.getNext();
                                         }
 
                                         if (lexC != null && lexC.lexi.equals("}")) {
                                             if (whileStack.size() > 1) {
                                                 String itemToBeReplaced = whileStack.pop();
 
                                                 if (!whileStack.isEmpty()) {
                                                     for (int u = iCodeList.size() - 1; u >= 0; u--) {
                                                         if (iCodeList.get(u).getArg2().equals((itemToBeReplaced))) {
                                                             iCodeList.get(u).setArg2(whileStack.peek());
                                                         }
                                                     }
                                                 }
                                             } else {
                                                 useConditionInReturnWhile = true;
                                             }
                                         }
                                     }
 
                                 }
 
                                 if (subScope.startsWith(".if") && Character.isDigit(subScope.toCharArray()[3])) {
                                     LexicalAnalyzer tempLex = new LexicalAnalyzer();
                                     tempLex.setLexicalList(lexicalAnalyzer.getLexicalList());
                                     tempLex.setLexPtr(lexicalAnalyzer.getLexPtr());
 
                                     Tuple lexC = null;
                                     if (tempLex.hasNext()) {
                                         lexC = tempLex.getNext();
                                     }
 
                                     if (lexC != null && lexC.lexi.equals("}")) {
                                         String inScope = scopePassTwo.substring(0, scopePassTwo.lastIndexOf("."));
                                         boolean whileInScope = inScope.substring(inScope.lastIndexOf("."), inScope.length()).contains("while");
 
 
                                         if (whileInScope) {
                                             if (whileStack.size() > 1) {
                                                 String itemToBeReplaced = ifStack.pop();
                                                 String replacer;
 
                                                 if (whileStack.peek().startsWith("ENDWHILE")) {
                                                     String end = whileStack.pop();
                                                     replacer = whileStack.peek();
                                                     whileStack.push(end);
                                                 } else {
                                                     replacer = whileStack.peek();
                                                 }
 
 
                                                 if (!whileStack.isEmpty()) {
                                                     for (int u = iCodeList.size() - 1; u >= 0; u--) {
                                                         if (iCodeList.get(u).getArg2().equals((itemToBeReplaced))) {
                                                             iCodeList.get(u).setArg2(replacer);
                                                         }
                                                     }
                                                 }
                                             } else {
                                                 useConditionInReturnWhile = true;
                                             }
 
                                         } else if (ifStack.size() > 1) {
                                             String itemToBeReplaced = ifStack.pop();
                                             String replacer;
 
                                             if (ifStack.peek().startsWith("ENDWHILE")) {
                                                 String end = ifStack.pop();
                                                 replacer = ifStack.peek();
                                                 ifStack.push(end);
                                             } else {
                                                 replacer = ifStack.peek();
                                             }
 
                                             if (!ifStack.isEmpty()) {
                                                 for (int u = iCodeList.size() - 1; u >= 0; u--) {
                                                     if (iCodeList.get(u).getArg2().equals((itemToBeReplaced))) {
                                                         iCodeList.get(u).setArg2(replacer);
                                                     }
                                                 }
                                             }
                                         } else {
                                             useConditionInReturn = true;
                                         }
                                     }
                                 }
 
                                 if (subScope.startsWith(".else") && Character.isDigit(subScope.toCharArray()[5])) {
                                     LexicalAnalyzer tempLex = new LexicalAnalyzer();
                                     tempLex.setLexicalList(lexicalAnalyzer.getLexicalList());
                                     tempLex.setLexPtr(lexicalAnalyzer.getLexPtr());
 
                                     Tuple lexC = null;
                                     if (tempLex.hasNext()) {
                                         lexC = tempLex.getNext();
                                     }
 
                                     if (lexC != null && lexC.lexi.equals("}")) {
                                         if (ifStack.size() > 1) {
                                             String itemToBeReplaced = ifStack.pop();
                                             String replacer;
 
                                             if (ifStack.peek().startsWith("ENDWHILE")) {
                                                 String end = ifStack.pop();
                                                 replacer = ifStack.peek();
                                                 ifStack.push(end);
                                             } else {
                                                 replacer = ifStack.peek();
                                             }
 
                                             if (!ifStack.isEmpty()) {
                                                 for (int u = iCodeList.size() - 1; u >= 0; u--) {
                                                     if (iCodeList.get(u).getArg1().equals((itemToBeReplaced))) {
                                                         iCodeList.get(u).setArg1(replacer);
                                                     }
                                                 }
                                             }
                                         } else {
                                             useConditionInReturn = true;
                                         }
                                     }
                                 }
 
                             } else {
                                 condLabel = "";
                                 condTypeScope = "";
 
                                 if (useConditionInReturn) {
                                     String itemToBeReplaced = ifStack.pop();
                                     for (int u = iCodeList.size() - 1; u >= 0; u--) {
                                         if (iCodeList.get(u).getArg2().equals((itemToBeReplaced))) {
                                             iCodeList.get(u).setArg2("FINISH");
                                         }
                                         if (iCodeList.get(u).getArg1().equals((itemToBeReplaced))) {
                                             iCodeList.get(u).setArg1("FINISH");
                                         }
                                     }
 
                                     useConditionInReturn = false;
                                 }
 
                                 if (useConditionInReturnWhile) {
                                     String itemToBeReplaced = whileStack.pop();
                                     for (int u = iCodeList.size() - 1; u >= 0; u--) {
                                         if (iCodeList.get(u).getArg2().equals((itemToBeReplaced))) {
                                             iCodeList.get(u).setArg2("FINISH");
                                         }
                                         if (iCodeList.get(u).getArg1().equals((itemToBeReplaced))) {
                                             iCodeList.get(u).setArg1("FINISH");
                                         }
                                     }
 
                                     useConditionInReturnWhile = false;
                                 }
 
                                 iCodeList.add(new ICode("", "RTN", "", "", "", "; always return from a function"));
                             }
 
                             scopePassTwo = scopePassTwo.substring(0, scopePassTwo.lastIndexOf('.'));
                         }
                         openBlocks.remove(openBlocks.size() - 1);
                     }
                 }
                 i++;
             }
 
         }
 
         if (iCodeList.get(0).getArg1().isEmpty()) {
             errorList += "program requires a 'main' function\n";
         }
 
         for (ICode o : iCodeList)
             System.out.println(o);
 
         try {
             if (!errorList.isEmpty()) {
                 throw new IllegalArgumentException(errorList);
             }
         } catch (IllegalArgumentException e) {
             System.out.print(e.getMessage());
             System.exit(0);
         }
 
         TCode targetCode = new TCode(symbolTable, iCodeList);
         targetCode.buildCode();
     }
 
     private boolean validateRelationalParametersSemantic(List<Tuple> tempList, Stack<SAR> sas, Stack<SAR> os, String scopePassTwo, Tuple item, int argCount, String type, String condType) {
         while (tempList.get(eIndex).lexi.equals("(")) {
             pushOS(scopePassTwo, tempList, os, eIndex, tempList.get(eIndex), sas, scopePassTwo);
             eIndex++;
         }
 
         // todo: need to add better checking, called variables and such
         if (!validRelationType(tempList, eIndex)) {
             errorList += "invalid parameter type. Line: " + tempList.get(eIndex).lineNum + "\n";
             return false;
         }
         int index = eIndex - 1;
         SAR sar = new SAR(tempList.get(eIndex), scopePassTwo, "", "");
 
         if (tempList.get(eIndex).type.equals(LexicalAnalyzer.tokenTypesEnum.NUMBER.name()) || tempList.get(eIndex).type.equals(LexicalAnalyzer.tokenTypesEnum.CHARACTER.name()) || tempList.get(eIndex).lexi.equals("true") || tempList.get(eIndex).lexi.equals("false")) {
             addLiteralExpressionToSAS(scopePassTwo, tempList.get(eIndex), sas);
             sar = sas.peek();
         } else if (tempList.get(eIndex).type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name())) {
             if (!iExist(sar, sas, tempList, scopePassTwo, os)) {
                 return false;
             }
             sas.push(sar);
         }
         eIndex++;
 
         argCount++;
 
         if (argCount == 1 && tempList.get(eIndex).lexi.equals(")") && tempList.get(index).lexi.equals("(")) {
             if (!sar.getType().equals("bool")) {
                 errorList += "illegal single argument, expected a bool type but was: '" + sar.getType() + "'. Line: " + tempList.get(eIndex).lineNum + "\n";
                 return false;
             }
 
         }
 
         // check if item is an array
         // todo: check for valid identifiers
         if (tempList.get(eIndex).type.equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_BEGIN.name())) {
             if (!tempList.get(eIndex + 1).type.equals(LexicalAnalyzer.tokenTypesEnum.NUMBER.name())) {
                 errorList += "invalid array indexer. Line: " + tempList.get(eIndex).lineNum + "\n";
                 return false;
             }
             if (!tempList.get(eIndex + 3).type.equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_END.name())) {
                 errorList += "arrays accessing requires both open and close square brakets. Line: " + tempList.get(eIndex).lineNum + "\n";
                 return false;
             }
             eIndex += 3;
         }
 
         if (tempList.get(eIndex).lexi.equals(")")) {
             while (tempList.get(eIndex).lexi.equals(")")) {
                 while (!os.peek().getLexi().lexi.equals("(")) {
                     if (os.peek() == null) {
                         errorList += "Missing opening paren. Line: " + item.lineNum + "\n";
                         return false;
                     }
                     addTempToSAS(os.pop(), sas, scopePassTwo, false);
                 }
 
                 if (!os.isEmpty()) {
                     os.pop();
                 }
                 if (os.isEmpty()) {
                     if (sas.isEmpty()) {
                         iCodeList.add(new ICode("", "BF", sar.getType(), condType + type, "", ""));
                     } else {
                         iCodeList.add(new ICode("", "BF", sas.peek().getKey(), condType + type, "", ""));
                         sas.pop();
                     }
                     condLabel = condType + type;
                     ifStack.push(condType + type);
                 }
 
                 if (tempList.get(eIndex + 1).lexi.equals("{")) {
                     return true;
                 }
                 eIndex++;
             }
         }
 
         if (tempList.get(eIndex).type.equals(LexicalAnalyzer.tokenTypesEnum.MATH_OPR.name()) || tempList.get(eIndex).type.equals(LexicalAnalyzer.tokenTypesEnum.RELATIONAL_OPR.name())) {
             pushOS(scopePassTwo, tempList, os, eIndex, tempList.get(eIndex), sas, scopePassTwo);
             eIndex++;
             return validateRelationalParametersSemantic(tempList, sas, os, scopePassTwo, tempList.get(eIndex), argCount, type, condType);
         } else if (tempList.get(eIndex).type.equals(LexicalAnalyzer.tokenTypesEnum.BOOLEAN_OPR.name())) {
             while (os.peek().getLexi().type.equals(LexicalAnalyzer.tokenTypesEnum.MATH_OPR.name()) || os.peek().getLexi().type.equals(LexicalAnalyzer.tokenTypesEnum.RELATIONAL_OPR.name())) {
                 addTempToSAS(os.pop(), sas, scopePassTwo, false);
             }
             pushOS(scopePassTwo, tempList, os, eIndex, tempList.get(eIndex), sas, scopePassTwo);
             eIndex++;
             return validateRelationalParametersSemantic(tempList, sas, os, scopePassTwo, tempList.get(eIndex), argCount, type, condType);
         } else {
             errorList += "Invalid parameter. Line: " + tempList.get(eIndex).lineNum + "\n";
             return false;
         }
     }
 
     private boolean validateRelationalParametersSemanticWhile(List<Tuple> tempList, Stack<SAR> sas, Stack<SAR> os, String scopePassTwo, Tuple item, int argCount, String type, int passCount, int boolOprCount) {
         while (tempList.get(eIndex).lexi.equals("(")) {
             pushOS(scopePassTwo, tempList, os, eIndex, tempList.get(eIndex), sas, scopePassTwo);
             eIndex++;
         }
 
         // todo: need to add better checking, called variables and such
         if (!validRelationType(tempList, eIndex)) {
             errorList += "invalid parameter type. Line: " + tempList.get(eIndex).lineNum + "\n";
             return false;
         }
         int index = eIndex - 1;
         SAR sar = new SAR(tempList.get(eIndex), scopePassTwo, "", "");
 
         if (tempList.get(eIndex).type.equals(LexicalAnalyzer.tokenTypesEnum.NUMBER.name()) || tempList.get(eIndex).type.equals(LexicalAnalyzer.tokenTypesEnum.CHARACTER.name()) || tempList.get(eIndex).lexi.equals("true") || tempList.get(eIndex).lexi.equals("false")) {
             addLiteralExpressionToSAS(scopePassTwo, tempList.get(eIndex), sas);
             sar = sas.peek();
         } else if (tempList.get(eIndex).type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name())) {
             if (!iExist(sar, sas, tempList, scopePassTwo, os)) {
                 return false;
             }
             sas.push(sar);
         }
         eIndex++;
 
         argCount++;
 
         if (argCount == 1 && tempList.get(eIndex).lexi.equals(")") && tempList.get(index).lexi.equals("(")) {
             if (!sar.getType().equals("bool")) {
                 errorList += "illegal single argument, expected a bool type but was: '" + sar.getType() + "'. Line: " + tempList.get(eIndex).lineNum + "\n";
                 return false;
             }
 
         }
 
         // check if item is an array
         // todo: check for valid identifiers
         if (tempList.get(eIndex).type.equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_BEGIN.name())) {
             if (!tempList.get(eIndex + 1).type.equals(LexicalAnalyzer.tokenTypesEnum.NUMBER.name())) {
                 errorList += "invalid array indexer. Line: " + tempList.get(eIndex).lineNum + "\n";
                 return false;
             }
             if (!tempList.get(eIndex + 3).type.equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_END.name())) {
                 errorList += "arrays accessing requires both open and close square brakets. Line: " + tempList.get(eIndex).lineNum + "\n";
                 return false;
             }
             eIndex += 3;
         }
 
         if (tempList.get(eIndex).lexi.equals(")")) {
             while (tempList.get(eIndex).lexi.equals(")")) {
                 while (!os.peek().getLexi().lexi.equals("(")) {
                     if (os.peek() == null) {
                         errorList += "Missing opening paren. Line: " + item.lineNum + "\n";
                         return false;
                     }
                     if (passCount == 0) {
                         condLabel = "BEGINWHILE" + type;
                         whileStack.push(condLabel);
                     }
                     addTempToSAS(os.pop(), sas, scopePassTwo, true);
                 }
 
                 if (!os.isEmpty()) {
                     os.pop();
                 }
                 if (os.isEmpty()) {
                     if (sas.isEmpty()) {
                         iCodeList.add(new ICode(condLabel, "BF", sar.getType(), "ENDWHILE" + type, "", ""));
                     } else {
                         iCodeList.add(new ICode(condLabel, "BF", sas.peek().getKey(), "ENDWHILE" + type, "", ""));
                         sas.pop();
                     }
                     condLabel = "ENDWHILE" + type;
                     whileStack.push(condLabel);
                 }
 
                 if (tempList.get(eIndex + 1).lexi.equals("{")) {
                     return true;
                 }
                 eIndex++;
             }
         }
 
         if (tempList.get(eIndex).type.equals(LexicalAnalyzer.tokenTypesEnum.MATH_OPR.name()) || tempList.get(eIndex).type.equals(LexicalAnalyzer.tokenTypesEnum.RELATIONAL_OPR.name())) {
             pushOS(scopePassTwo, tempList, os, eIndex, tempList.get(eIndex), sas, scopePassTwo);
             eIndex++;
             return validateRelationalParametersSemanticWhile(tempList, sas, os, scopePassTwo, tempList.get(eIndex), argCount, type, passCount++, boolOprCount);
         } else if (tempList.get(eIndex).type.equals(LexicalAnalyzer.tokenTypesEnum.BOOLEAN_OPR.name())) {
             while (os.peek().getLexi().type.equals(LexicalAnalyzer.tokenTypesEnum.MATH_OPR.name()) || os.peek().getLexi().type.equals(LexicalAnalyzer.tokenTypesEnum.RELATIONAL_OPR.name())) {
                 if (boolOprCount++ == 0) {
                     condLabel = "BEGINWHILE" + type;
                 }
                 addTempToSAS(os.pop(), sas, scopePassTwo, false);
             }
             pushOS(scopePassTwo, tempList, os, eIndex, tempList.get(eIndex), sas, scopePassTwo);
             eIndex++;
             return validateRelationalParametersSemanticWhile(tempList, sas, os, scopePassTwo, tempList.get(eIndex), argCount, type, passCount++, boolOprCount++);
         } else {
             errorList += "Invalid parameter. Line: " + tempList.get(eIndex).lineNum + "\n";
             return false;
         }
     }
 
     private void pushOS(String scopePassTwo, List<Tuple> tempList, Stack<SAR> OS, int i, Tuple item, Stack<SAR> SAS, String globalScope) {
         eIndex = i;
         if (item.lexi.equals("<<") || item.lexi.equals(">>")) {
             OS.push(new SAR(item, scopePassTwo, URINARY, ""));
         } else if (item.lexi.equals("=")) {
             if (tempList.get(i + 1).lexi.equals("new")) {
 
                 if (tempList.get(i + 2).lexi.equals("int") || tempList.get(i + 2).lexi.equals("char")) {
                     if (!tempList.get(i + 3).type.equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_BEGIN.name())) {
                         errorList += "primitive datatypes cannot be called as a function. Line: " + tempList.get(i).lineNum + "\n";
                         return;
                     }
 
                     if (tempList.get(i + 4).type.equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_END.name())) {
                         errorList += "must specify size when initializing an array. Line: " + tempList.get(i).lineNum + "\n";
                         return;
                     }
 
                     if (!isValidArrayIndexingType(tempList, i + 4, globalScope, SAS, OS)) {
                         return;
                     }
 
                     if (!tempList.get(i + 5).type.equals(LexicalAnalyzer.tokenTypesEnum.MATH_OPR.name())) {
                         if (!tempList.get(i + 5).type.equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_END.name())) {
                             errorList += "Invalid array declaration. Line: " + tempList.get(i).lineNum + "\n";
                             return;
                         }
                         i += 5;
                         return;
                     }
 
                     if (!isValidArrayIndexingType(tempList, i + 6, globalScope, SAS, OS)) {
                         return;
                     }
 
                     if (!tempList.get(i + 7).type.equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_END.name())) {
                         errorList += "Invalid array declaration. Line: " + tempList.get(i).lineNum + "\n";
                         return;
                     }
 
                     addLiteralExpressionToSAS(globalScope, tempList.get(i + 2), SAS);
                     i += 7;
 
                 } else {
 
                     SAR sar = new SAR(tempList.get(i + 2), globalScope, "", "");
                     eIndex = i + 2;
                     int ii = eIndex;
                     if (!iExist(sar, SAS, tempList, globalScope, OS)) {
                         errorList += "Function '" + sar.getLexi().lexi + "' does not exist. Line: " + item.lineNum + "\n";
                         return;
                     }
                     if (!SAS.peek().getType().equals(sar.getType())) {
                         errorList += "Incompatible types, invalid new operation. Line: " + item.lineNum + "\n";
                         return;
                     }
                     if (ii == eIndex) {
                         eIndex += 2;
                     } else {
                         eIndex++;
                     }
                     SAS.push(sar);
                 }
             }
 
             OS.push(new SAR(item, scopePassTwo, BINARY, ""));
         } else {
             OS.push(new SAR(item, scopePassTwo, BINARY, ""));
         }
     }
 
     private void cleanupSAS(Stack<SAR> SAS, Stack<SAR> OS, String scopePassTwo) {
         while (!OS.isEmpty()) addTempToSAS(OS.pop(), SAS, scopePassTwo, false);
         if (!SAS.isEmpty()) {
             evaluateReturnStatement(SAS, scopePassTwo);
         }
     }
 
     private void evaluateReturnStatement(Stack<SAR> sas, String scopePassTwo) {
         String newLabel = "";
         if (scopePassTwo.contains(".")) {
             if (!scopePassTwo.substring(scopePassTwo.lastIndexOf("."), scopePassTwo.length()).equals(condTypeScope)) {
                 newLabel = condLabel;
             }
         }
 
         if (sas.size() == 1) {
             SAR RHS = sas.pop();
 
             if (RHS.getLexi().lexi.equals("return")) {
                 if (!RHS.getType().equals("void")) {
                     if (!RHS.getType().equals("main")) {
                         errorList += "invalid return statement. Line: " + RHS.getLexi().lineNum + "\n";
                         return;
                     }
                 }
 
                 iCodeList.add(new ICode(newLabel, "RTN", "", "", "", "; return void"));
                 addToSymbolTable("lvar", new ArrayList<String>(), RHS.getType(), "private", "T" + symIdInr++, RHS.getLexi().lineNum);
                 if (!newLabel.isEmpty()) {
                     condLabel = "";
                 }
             }
             return;
         }
 
         if (sas.size() == 2) {
             SAR RHS = sas.pop();
             SAR LHS = sas.pop();
 
             if (LHS.getLexi().lexi.equals("return")) {
                 if (!LHS.getType().equals(RHS.getType())) {
                     errorList += "invalid return type. Line: " + RHS.getLexi().lineNum + "\n";
                     return;
                 }
                 iCodeList.add(new ICode(newLabel, "RETURN", RHS.getKey(), "", "", "; return " + RHS.getLexi().lexi));
                 addToSymbolTable("lvar", new ArrayList<String>(), RHS.getType(), "private", "T" + symIdInr++, RHS.getLexi().lineNum);
                 if (!newLabel.isEmpty()) {
                     condLabel = "";
                 }
             }
         }
     }
 
     private void addLiteralExpressionToSAS(String scopePassTwo, Tuple tuple, Stack<SAR> SAS) {
         SAR litSar = new SAR(tuple, scopePassTwo, "", "");
         String searchLit;
 
         if (tuple.type.equals(LexicalAnalyzer.tokenTypesEnum.NUMBER.name())) {
             searchLit = "x" + tuple.lexi;
         } else {
             searchLit = tuple.lexi;
         }
 
 
         for (String key : symbolTable.keySet()) {
             Symbol s = symbolTable.get(key);
 
             if (s.getValue().equals(searchLit) && s.getKind().equals("Literal")) {
                 litSar.setKey(key);
                 break;
             }
         }
 
         if (tuple.lexi.equals("true") || tuple.lexi.equals("false")) {
             litSar.setType("bool");
             SAS.push(litSar);
         } else if (tuple.type.equals(LexicalAnalyzer.tokenTypesEnum.NUMBER.name())) {
             litSar.setType("int");
             SAS.push(litSar);
         } else if (tuple.type.equals(LexicalAnalyzer.tokenTypesEnum.CHARACTER.name())) {
             litSar.setType("char");
             SAS.push(litSar);
         } else if (tuple.lexi.equals("null")) {
             litSar.setType("null");
             SAS.push(litSar);
         }
     }
 
     private boolean rExist(SAR sar, Stack<SAR> SAS, List<Tuple> tempList, String globalScope, Stack<SAR> OS) {
         SAR caller = SAS.pop();
         boolean found = false;
         String foundScope = "";
         String sarScope = caller.getScope();
 
         while (!sarScope.isEmpty()) {
 
             for (String key : symbolTable.keySet()) {
                 Symbol s = symbolTable.get(key);
                 if (s.getValue().equals(caller.getType()) && s.getScope().equals("g")) {
                     found = true;
                     foundScope = s.getScope() + "." + caller.getType();
                     break;
                 }
 
                 if (s.getValue().equals(caller.getType()) && s.getScope().equals(sarScope)) {
                     found = true;
                     foundScope = s.getScope();
                     break;
                 }
 
             }
 
             if (found) break;
 
             if (sarScope.contains(".")) {
                 sarScope = sarScope.substring(0, sarScope.lastIndexOf("."));
             } else {
                 sarScope = "";
             }
         }
 
         return found && evaluateCallies(foundScope, sar, SAS, tempList, globalScope, OS);
     }
 
     private boolean evaluateCallies(String foundScope, SAR sar, Stack<SAR> SAS, List<Tuple> tempList, String globalScope, Stack<SAR> OS) {
         for (String key : symbolTable.keySet()) {
             Symbol s = symbolTable.get(key);
 
             if (s.getValue().equals(sar.getLexi().lexi) && s.getScope().equals(foundScope)) {
                 if (s.getData() instanceof VaribleData) {
                     if (((VaribleData) s.getData()).getAccessMod().equals("public")) {
                         sar.setType(((VaribleData) s.getData()).getType());
                         sar.setKey(key);
                         String value = "t" + symIdInr;
                         addToSymbolTable("tVar", new ArrayList<String>(), ((VaribleData) s.getData()).getType(), "private", value, sar.getLexi().lineNum);
 
                         Tuple tempLexi = new Tuple(value, LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name(), sar.getLexi().lineNum);
                         SAS.push(new SAR(tempLexi, sar.getScope(), sar.getType(), ""));
 
                         if (tempList.get(eIndex + 1).lexi.equals(".")) {
                             eIndex = eIndex + 2;
                             foundScope = foundScope.substring(0, foundScope.lastIndexOf("."));
                             SAS.pop();
                             return evaluateCallies(foundScope + "." + sar.getType(), new SAR(tempList.get(eIndex), sar.getScope(), "", ""), SAS, tempList, globalScope, OS);
                         }
                         return true;
                     } else {
                         errorList += "Access Error: " + sar.getLexi().lexi + " is a private variable. Line: " + sar.getLexi().lineNum + "\n";
                         return false;
                     }
                 } else if (s.getData() instanceof FunctionData) {
                     if (!((FunctionData) s.getData()).getAccessMod().equals("public")) {
                         errorList += "Access Error: " + sar.getLexi().lexi + " is a private function. Line: " + sar.getLexi().lineNum + "\n";
                         return false;
                     }
 
                     if (((FunctionData) s.getData()).getParameters().size() == 0) {
                         sar.setType(((FunctionData) s.getData()).getReturnType());
                         sar.setKey(key);
                         String value = "t" + symIdInr;
                         addToSymbolTable("tVar", new ArrayList<String>(), ((FunctionData) s.getData()).getReturnType(), "private", value, sar.getLexi().lineNum);
 
                         Tuple tempLexi = new Tuple(value, LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name(), sar.getLexi().lineNum);
                         SAS.push(new SAR(tempLexi, sar.getScope(), sar.getType(), ""));
 
                         if (tempList.get(eIndex + 3).lexi.equals(".")) {
                             eIndex = eIndex + 4;
                             foundScope = foundScope.substring(0, foundScope.lastIndexOf("."));
                             SAS.pop();
                             return evaluateCallies(foundScope + "." + sar.getType(), new SAR(tempList.get(eIndex), sar.getScope(), "", ""), SAS, tempList, globalScope, OS);
                         } else {
                             eIndex = eIndex + 2;
                         }
 
                         return true;
 
                     } else {
                         sar.setType(((FunctionData) s.getData()).getReturnType());
                         sar.setKey(key);
                         int index = eIndex + 2;
                         String value = "t" + symIdInr;
 
                         if (!doParametersExist(SAS, tempList, globalScope, index, ((FunctionData) s.getData()).getParameters(), ((FunctionData) s.getData()).getParameters().size() - 1, false, OS)) {
                             errorList += "Invalid parameter. Line: " + tempList.get(index).lineNum + "\n";
                             eIndex = tempList.size() - 2;
                             return false;
                         }
                         addToSymbolTable("tVar", new ArrayList<String>(), ((FunctionData) s.getData()).getReturnType(), "private", value, sar.getLexi().lineNum);
 
                         Tuple tempLexi = new Tuple(value, LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name(), sar.getLexi().lineNum);
                         SAS.push(new SAR(tempLexi, sar.getScope(), sar.getType(), ""));
 
                         if (tempList.get(eIndex + 2).lexi.equals(".")) {
                             eIndex += 3;
                             foundScope = foundScope.substring(0, foundScope.lastIndexOf("."));
                             SAS.pop();
                             return evaluateCallies(foundScope + "." + sar.getType(), new SAR(tempList.get(eIndex), sar.getScope(), "", ""), SAS, tempList, globalScope, OS);
                         }
                         return true;
                     }
                 }
             }
         }
         errorList += sar.getLexi().lexi + " does not exist. Line: " + sar.getLexi().lineNum + "\n";
         return false;
     }
 
     private boolean doParametersExist(Stack<SAR> SAS, List<Tuple> tempList, String globalScope, int index, List<String> paramList, int pId, boolean isMath, Stack<SAR> OS) {
         eIndex = index;
 
         Tuple item = tempList.get(index);
         if (!isLegalValue(item)) {
             errorList += "the function being called has too few parameters. Line: " + item.lineNum + "\n";
 
             System.out.print(errorList);
             System.exit(0);
         }
 
         if (pId < 0) {
             errorList += "the function being called has too many parameters. Line: " + item.lineNum + "\n";
             return false;
         }
 
         SAR sar = new SAR(item, globalScope, "", "");
         if (isLiteralExpression(item)) {
             boolean isGood = true;
             String actualType = "";
 
             if (item.lexi.equals("true") || item.lexi.equals("false")) {
                 if (!paramList.get(pId).equals("bool")) {
                     isGood = false;
                     actualType = item.lexi;
                 }
                 sar.setType("bool");
                 sar.setKey("L" + symIdInr);
             } else if (item.type.equals(LexicalAnalyzer.tokenTypesEnum.NUMBER.name())) {
                 if (!paramList.get(pId).equals("int")) {
                     isGood = false;
                     actualType = item.type;
                 }
                 sar.setType("int");
                 sar.setKey("L" + symIdInr);
             } else if (item.type.equals(LexicalAnalyzer.tokenTypesEnum.CHARACTER.name())) {
                 if (!paramList.get(pId).equals("char")) {
                     isGood = false;
                     actualType = item.type;
                 }
                 sar.setType("char");
                 sar.setKey("L" + symIdInr);
             } else if (item.lexi.equals("null")) {
                 if (!paramList.get(pId).equals("null")) {
                     isGood = false;
                     actualType = item.lexi;
                 }
                 sar.setType("null");
                 sar.setKey("L" + symIdInr);
             }
 
             if (!isGood) {
                 errorList += "incompatible parameter type. expected '" + paramList.get(pId) + "' but was '" + actualType + "'. Line: " + item.lineNum + "\n";
                 return false;
             }
 
             if (sar.getType().equals("int")) {
                 boolean done = false;
                 if (index >= 3) {
                     if (tempList.get(index - 1).lexi.equals("-") && tempList.get(index - 2).type.equals(LexicalAnalyzer.tokenTypesEnum.MATH_OPR.name())) {
                         addToSymbolTable("Literal", new ArrayList<String>(), "int", "public", "x-" + item, item.lineNum);
                         done = true;
                     }
                 }
                 if (!done) {
                     addToSymbolTable("Literal", new ArrayList<String>(), "int", "public", "x" + item.lexi, item.lineNum);
 
                 }
             } else if (sar.getType().equals("char") || sar.getType().equals("null")) {
                 addToSymbolTable("Literal", new ArrayList<String>(), "char", "public", item.lexi, item.lineNum);
             } else if (sar.getType().equals("bool")) {
                 addToSymbolTable("Literal", new ArrayList<String>(), "int", "public", item.lexi, item.lineNum);
             }
 
         } else if (isIdentifierExpression(item)) {
             String sarScope = globalScope;
             boolean found = false;
             while (!sarScope.isEmpty()) {
                 for (String key : symbolTable.keySet()) {
                     Symbol s = symbolTable.get(key);
                     if (sarScope.equals("g")) {
                         if (s.getValue().equals(sar.getLexi().lexi) && s.getScope().equals(sarScope) && !(s.getData() instanceof ClassData)) {
                             if (s.getData() instanceof VaribleData) {
                                 sar.setType(((VaribleData) s.getData()).getType());
                                 sar.setKey(key);
 
                                 if (((VaribleData) s.getData()).getAccessMod().equals("private") && !globalScope.equals("g")) {
                                     errorList += "Access Error: " + sar.getLexi().lexi + " is a private variable. Line: " + sar.getLexi().lineNum + "\n";
                                     return false;
                                 }
 
                                 if (tempList.get(index + 1).lexi.equals(".")) {
                                     SAS.push(sar);
                                     eIndex = index + 2;
                                     if (!rExist(new SAR(tempList.get(index + 2), globalScope, "", ""), SAS, tempList, globalScope, OS)) {
                                         errorList += "Fail. Line: " + sar.getLexi().lineNum + "\n";
                                         return false;
                                     }
 
                                     if (!paramList.get(pId).equals(SAS.peek().getType())) {
                                         errorList += "incompatible parameter type. expected '" + paramList.get(pId) + "' but was '" + sar.getType() + "'. Line: " + item.lineNum + "\n";
                                         return false;
                                     }
                                     index = eIndex;
                                 } else {
 
                                     if (!paramList.get(pId).equals(sar.getType())) {
                                         errorList += "incompatible parameter type. expected '" + paramList.get(pId) + "' but was '" + sar.getType() + "'. Line: " + item.lineNum + "\n";
                                         return false;
                                     }
                                 }
 
                                 if (!paramList.get(pId).equals(sar.getType())) {
                                     errorList += "incompatible parameter type. expected '" + paramList.get(pId) + "' but was '" + sar.getType() + "'. Line: " + item.lineNum + "\n";
                                     return false;
                                 }
 
                             } else if (s.getData() instanceof FunctionData) {
                                 sar.setType(((FunctionData) s.getData()).getReturnType());
                                 sar.setKey(key);
 
                                 if (((FunctionData) s.getData()).getAccessMod().equals("private") && !globalScope.equals("g")) {
                                     errorList += "Access Error: " + sar.getLexi().lexi + " is a private function. Line: " + sar.getLexi().lineNum + "\n";
                                     return false;
                                 }
 
                                 if (!paramList.get(pId).equals(sar.getType())) {
                                     errorList += "incompatible parameter type. expected '" + paramList.get(pId) + "' but was '" + sar.getType() + "'. Line: " + item.lineNum + "\n";
                                     return false;
                                 }
 
                                 if (((FunctionData) s.getData()).getParameters().size() == 0) {
                                     index += 2;
                                 } else {
                                     return doParametersExist(SAS, tempList, globalScope, index + 2, paramList, ((FunctionData) s.getData()).getParameters().size() - 1, false, OS);
                                 }
                             }
                             found = true;
                             break;
                         }
                     }
 
                     if (s.getValue().equals(sar.getLexi().lexi) && s.getScope().equals(sarScope) && !(s.getData() instanceof ClassData)) {
                         if (s.getData() instanceof VaribleData) {
                             sar.setType(((VaribleData) s.getData()).getType());
                             sar.setKey(key);
 
                             if (((VaribleData) s.getData()).getAccessMod().equals("private") && !globalScope.contains(sarScope)) {
                                 errorList += "Access Error: " + sar.getLexi().lexi + " is a private variable. Line: " + sar.getLexi().lineNum + "\n";
                                 return false;
                             }
 
                             if (tempList.get(index + 1).lexi.equals(".")) {
                                 SAS.push(sar);
                                 eIndex = index + 2;
                                 if (!rExist(new SAR(tempList.get(index + 2), globalScope, "", ""), SAS, tempList, globalScope, OS)) {
                                     errorList += "Fail. Line: " + sar.getLexi().lineNum + "\n";
                                     return false;
                                 }
 
                                 if (!paramList.get(pId).equals(SAS.peek().getType())) {
                                     errorList += "incompatible parameter type. expected '" + paramList.get(pId) + "' but was '" + sar.getType() + "'. Line: " + item.lineNum + "\n";
                                     return false;
                                 }
                                 index = eIndex;
                             } else {
 
                                 if (!paramList.get(pId).equals(sar.getType())) {
                                     errorList += "incompatible parameter type. expected '" + paramList.get(pId) + "' but was '" + sar.getType() + "'. Line: " + item.lineNum + "\n";
                                     return false;
                                 }
                             }
 
                         } else if (s.getData() instanceof FunctionData) {
                             sar.setType(((FunctionData) s.getData()).getReturnType());
                             sar.setKey(key);
 
                             if (((FunctionData) s.getData()).getAccessMod().equals("private") && !globalScope.equals(sarScope)) {
                                 errorList += "Access Error: " + sar.getLexi().lexi + " is a private function. Line: " + sar.getLexi().lineNum + "\n";
                                 return false;
                             }
 
                             if (!paramList.get(pId).equals(sar.getType())) {
                                 errorList += "incompatible parameter type. expected '" + paramList.get(pId) + "' but was '" + sar.getType() + "'. Line: " + item.lineNum + "\n";
                                 return false;
                             }
 
                             if (((FunctionData) s.getData()).getParameters().size() == 0) {
                                 index += 2;
                             } else {
                                 return doParametersExist(SAS, tempList, globalScope, index + 2, paramList, ((FunctionData) s.getData()).getParameters().size() - 1, false, OS);
                             }
                         }
                         found = true;
                         break;
                     }
                 }
 
                 if (found) {
                     break;
                 }
 
                 if (sarScope.contains(".")) {
                     sarScope = sarScope.substring(0, sarScope.lastIndexOf("."));
                 } else {
                     sarScope = "";
                 }
             }
 
             if (!found) {
                 errorList += "variable '" + item.lexi + "' does not exist in scope. Line: " + item.lineNum + "\n";
                 return false;
             }
         }
 
         if (tempList.get(index + 1).type.equals(LexicalAnalyzer.tokenTypesEnum.MATH_OPR.name())) {
             if (!sar.getType().equals("int")) {
                 errorList += "Mathematical operations require variable to be type int. Line: " + tempList.get(index).lineNum + "\n";
                 return false;
             }
             OS.push(new SAR(tempList.get(index + 1), globalScope, BINARY, ""));
             if (tempList.get(index + 2).lexi.equals("-")) {
                 SAS.push(sar);
                 OS.push(new SAR(tempList.get(index + 2), globalScope, URINARY, ""));
                 return doParametersExist(SAS, tempList, globalScope, index + 3, paramList, pId, true, OS);
             }
             SAS.push(sar);
             return doParametersExist(SAS, tempList, globalScope, index + 2, paramList, pId, true, OS);
 
         }
 
         if (tempList.get(index + 1).lexi.equals(",")) {
             if (isMath) {
                 SAS.push(sar);
                 int errCount = errorList.length();
                 while (OS.peek().getLexi().type.equals(LexicalAnalyzer.tokenTypesEnum.MATH_OPR.name())) {
                     addTempToSAS(OS.pop(), SAS, globalScope, false);
                 }
                 if (errCount != errorList.length()) {
                     return false;
                 }
 
                 if (!paramList.get(pId).equals(sar.getType())) {
                     errorList += "incompatible parameter type. expected '" + paramList.get(pId) + "' but was '" + sar.getType() + "'. Line: " + item.lineNum + "\n";
                     return false;
                 }
 
             }
             return doParametersExist(SAS, tempList, globalScope, index + 2, paramList, pId - 1, false, OS);
         } else if (tempList.get(index + 1).lexi.equals(")") && pId != 0) {
             errorList += "function has too few parameters. Line: " + item.lineNum + "\n";
             return false;
         } else if (tempList.get(index + 1).lexi.equals(")")) {
             if (isMath) {
                 SAS.push(sar);
                 int errCount = errorList.length();
                 while (OS.peek().getLexi().type.equals(LexicalAnalyzer.tokenTypesEnum.MATH_OPR.name())) {
                     addTempToSAS(OS.pop(), SAS, globalScope, false);
                 }
                 if (errCount != errorList.length()) {
                     return false;
                 }
 
                 if (!paramList.get(pId).equals(SAS.peek().getType())) {
                     errorList += "incompatible parameter type. expected '" + paramList.get(pId) + "' but was '" + sar.getType() + "'. Line: " + item.lineNum + "\n";
                     return false;
                 }
 
             }
             return true;
         }
 
         return false;
     }
 
     private boolean isStreamOpr(String lexi) {
         return lexi.equals("cout") || lexi.equals("cin");
     }
 
     private boolean isLegalValue(Tuple item) {
         return ((item.type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name()) && Character.isLowerCase(item.lexi.toCharArray()[0])) || item.type.equals(LexicalAnalyzer.tokenTypesEnum.NUMBER.name()) || item.type.equals(LexicalAnalyzer.tokenTypesEnum.CHARACTER.name()) || item.lexi.equals("true") || item.lexi.equals("false"));
     }
 
     private boolean ClassExist(Tuple item, String sscope) {
         for (String key : symbolTable.keySet()) {
             Symbol s = symbolTable.get(key);
             String sarScope = sscope;
             if (s.getValue().equals(item.lexi) && s.getScope().equals("g"))
                 return true;
 
             while (!sarScope.equals("g")) {
                 if (s.getValue().equals(item.lexi) && s.getScope().equals(sarScope)) {
                     return true;
                 }
                 sarScope = sarScope.substring(0, sarScope.lastIndexOf("."));
             }
         }
         return false;
     }
 
     private void addTempToSAS(SAR opr, Stack<SAR> SAS, String scopePassTwo, boolean isParamter) {
         String newLabel = "";
         boolean containsElse = scopePassTwo.substring(scopePassTwo.lastIndexOf("."), scopePassTwo.length()).contains("else");
         boolean containsWhile = scopePassTwo.substring(scopePassTwo.lastIndexOf("."), scopePassTwo.length()).contains("while");
         String whileKind = scopePassTwo.substring(scopePassTwo.lastIndexOf("."), scopePassTwo.length());
         if (scopePassTwo.contains(".") && (!ifStack.isEmpty() || !whileStack.isEmpty())) {
             if (!scopePassTwo.substring(scopePassTwo.lastIndexOf("."), scopePassTwo.length()).equals(condTypeScope) || containsElse || isParamter) {
                 if (containsElse && canPop && !ifStack.isEmpty()) {
                     if (ifStack.size() > 1) {
                         String temp = ifStack.pop();
                         newLabel = ifStack.pop();
                         ifStack.push(temp);
                     } else if (ifStack.size() == 1) {
                         ifStack.push(ifStack.pop());
                     } else {
                         canPop = false;
                         errorList += "else statements must be paired with if statements. Line: " + opr.getLexi().lineNum + "\n";
                         return;
                     }
                     canPop = false;
                 } else if (containsElse) {
                     // do nothing
                 } else if (isParamter || (!containsWhile && condTypeScope.contains("while")) || (containsWhile && condTypeScope.contains("while") && (!whileKind.equals(condTypeScope)))) {
                     if (!whileStack.isEmpty())
                         newLabel = whileStack.pop();
                 } else {
                     if (!ifStack.isEmpty())
                         newLabel = ifStack.pop();
                 }
             }
         }
 
         if (opr.getLexi().lexi.equals("=")) {
             if (SAS.size() < 2) {
                 errorList += "missing an operand. Line: " + opr.getLexi().lineNum + "\n";
                 return;
             }
 
             SAR RHS = SAS.pop();
             SAR LHS = SAS.pop();
 
 
             if (!sarEqualAssignment(RHS, LHS)) {
                 errorList += "left and right operand types are incompatible. Line: " + opr.getLexi().lineNum + "\n";
             }
 
            if (RHS.getKey().startsWith("L")) {
                if (RHS.getType().equals("int")) {
                    iCodeList.add(new ICode(newLabel, "MOVI", LHS.getKey(), RHS.getKey(), "", "; " + LHS.getLexi().lexi + " = " + RHS.getLexi().lexi));
                } else if (RHS.getType().equals("char")) {
                    iCodeList.add(new ICode(newLabel, "MOVC", LHS.getKey(), RHS.getKey(), "", "; " + LHS.getLexi().lexi + " = " + RHS.getLexi().lexi));
                }
            } else {
                iCodeList.add(new ICode(newLabel, "MOV", LHS.getKey(), RHS.getKey(), "", "; " + LHS.getLexi().lexi + " = " + RHS.getLexi().lexi));
            }
 
         } else if (opr.getLexi().type.equals(LexicalAnalyzer.tokenTypesEnum.MATH_OPR.name())) {
             if (SAS.size() < 2) {
                 errorList += "missing an operand. Line: " + opr.getLexi().lineNum + "\n";
                 return;
             }
 
             SAR RHS = SAS.pop();
             SAR LHS = SAS.pop();
 
             if (!LHS.getType().equals(RHS.getType())) {
                 errorList += "left and right operand types are incompatible. Line: " + opr.getLexi().lineNum + "\n";
                 return;
             }
 
 
             SAR temp = new SAR(new Tuple("T" + symIdInr, RHS.getLexi().type, RHS.getLexi().lineNum), RHS.getScope(), RHS.getType(), "T" + symIdInr);
             addToSymbolTable("tvar", new ArrayList<String>(), RHS.getType(), "private", "T" + symIdInr, RHS.getLexi().lineNum);
             SAS.push(temp);
 
             String oprName = opr.getLexi().lexi;
             if (oprName.equals("+")) {
                 if (RHS.getKey().startsWith("L") || LHS.getKey().startsWith("L")) {
                     iCodeList.add(new ICode(newLabel, "ADI", LHS.getKey(), RHS.getKey(), temp.getKey(), "; " + LHS.getLexi().lexi + " + " + RHS.getLexi().lexi + " -> " + temp.getLexi().lexi));
                 } else {
                     iCodeList.add(new ICode(newLabel, "ADD", LHS.getKey(), RHS.getKey(), temp.getKey(), "; " + LHS.getLexi().lexi + " + " + RHS.getLexi().lexi + " -> " + temp.getLexi().lexi));
                 }
             } else if (oprName.equals("-")) {
                 iCodeList.add(new ICode(newLabel, "SUB", LHS.getKey(), RHS.getKey(), temp.getKey(), "; " + LHS.getLexi().lexi + " - " + RHS.getLexi().lexi + " -> " + temp.getLexi().lexi));
             } else if (oprName.equals("*")) {
                 iCodeList.add(new ICode(newLabel, "MUL", LHS.getKey(), RHS.getKey(), temp.getKey(), "; " + LHS.getLexi().lexi + " * " + RHS.getLexi().lexi + " -> " + temp.getLexi().lexi));
             } else if (oprName.equals("/")) {
                 iCodeList.add(new ICode(newLabel, "DIV", LHS.getKey(), RHS.getKey(), temp.getKey(), "; " + LHS.getLexi().lexi + " / " + RHS.getLexi().lexi + " -> " + temp.getLexi().lexi));
             } else if (oprName.equals("%")) {
                 iCodeList.add(new ICode(newLabel, "MOD", LHS.getKey(), RHS.getKey(), temp.getKey(), "; " + LHS.getLexi().lexi + " % " + RHS.getLexi().lexi + " -> " + temp.getLexi().lexi ));
             }
 
         } else if (opr.getLexi().type.equals(LexicalAnalyzer.tokenTypesEnum.IO_OPR.name())) {
             if (SAS.size() < 1) {
                 errorList += "missing an operand. Line: " + opr.getLexi().lineNum + "\n";
                 return;
             }
 
             SAR RHS = SAS.pop();
 
             if (RHS.getType().equals("int") || RHS.getType().equals("char")) {
                 if (opr.getLexi().lexi.equals("<<")) {
                     if (RHS.getType().equals("int")) {
                         //todo: need to find the items real key value to put in here
                         iCodeList.add(new ICode(newLabel, "WRTI", RHS.getKey(), "", "", "; cout << " + RHS.getLexi().lexi));
                     } else {
                         iCodeList.add(new ICode(newLabel, "WRTC", RHS.getKey(), "", "", "; cout << " + RHS.getLexi().lexi));
                     }
                 } else if (opr.getLexi().lexi.equals(">>")) {
                     if (RHS.getType().equals("int")) {
                         iCodeList.add(new ICode(newLabel, "RDI", RHS.getKey(), "", "", "; cin >> " + RHS.getLexi().lexi));
                     } else {
                         iCodeList.add(new ICode(newLabel, "RDC", RHS.getKey(), "", "", "; cin >> " + RHS.getLexi().lexi));
                     }
                 }
                 addToSymbolTable("iovar", new ArrayList<String>(), RHS.getType(), "private", "IO" + symIdInr, RHS.getLexi().lineNum);
             } else {
                 errorList += "invalid variable type. insertion and extraction operators only deal with int and char. Line: " + opr.getLexi().lineNum + "\n";
             }
 
         } else if (opr.getLexi().type.equals(LexicalAnalyzer.tokenTypesEnum.BOOLEAN_OPR.name())) {
             if (SAS.size() < 2) {
                 errorList += "missing an operand. Line: " + opr.getLexi().lineNum + "\n";
                 return;
             }
 
             SAR RHS = SAS.pop();
             SAR LHS = SAS.pop();
             String oprName = opr.getLexi().lexi;
 
             SAR temp = new SAR(new Tuple("T" + symIdInr, RHS.getLexi().type, RHS.getLexi().lineNum), RHS.getScope(), RHS.getType(), "T" + symIdInr);
             addToSymbolTable("tvar", new ArrayList<String>(), "bool", "private", "T" + symIdInr, RHS.getLexi().lineNum);
             SAS.push(temp);
 
             if (RHS.getType().equals("bool") && LHS.getType().equals("bool")) {
                 if (oprName.equals("&&")) {
                     iCodeList.add(new ICode(newLabel, "AND", LHS.getKey(), RHS.getKey(), temp.getKey(), "; " + LHS.getLexi().lexi + " && " + RHS.getLexi().lexi + " -> " + temp.getLexi().lexi));
                 } else if (oprName.equals("||")) {
                     iCodeList.add(new ICode(newLabel, "OR", LHS.getKey(), RHS.getKey(), temp.getKey(), "; " + LHS.getLexi().lexi + " || " + RHS.getLexi().lexi + " -> " + temp.getLexi().lexi));
                 }
                 return;
             }
 
             errorList += "both side of a boolean statement must be of type 'bool'. Line: " + RHS.getLexi().lineNum + "\n";
         } else if (opr.getLexi().type.equals(LexicalAnalyzer.tokenTypesEnum.RELATIONAL_OPR.name())) {
             if (SAS.size() < 2) {
                 errorList += "missing an operand. Line: " + opr.getLexi().lineNum + "\n";
                 return;
             }
 
             SAR RHS = SAS.pop();
             SAR LHS = SAS.pop();
             String oprName = opr.getLexi().lexi;
 
             if (RHS.getLexi().lexi.equals("null")) {
                 if (LHS.getType().equals("int") || LHS.getType().equals("char") || LHS.getType().equals("bool")) {
                     errorList += "cannot set primitive types to null. Line:" + LHS.getLexi().lineNum + "\n";
                     return;
                 } else {
                     addToSymbolTable("lvar", new ArrayList<String>(), RHS.getType(), "private", "T" + symIdInr, RHS.getLexi().lineNum);
 
                     SAR temp = new SAR(new Tuple("T" + symIdInr, RHS.getLexi().type, RHS.getLexi().lineNum), RHS.getScope(), "bool", "");
                     SAS.push(temp);
 
                     if (oprName.equals("<=")) {
                         iCodeList.add(new ICode(newLabel, "LE", LHS.getKey(), RHS.getKey(), temp.getKey(), "; " + LHS.getLexi().lexi + " <= " + RHS.getLexi().lexi + " -> " + temp.getLexi().lexi));
                     } else if (oprName.equals(">=")) {
                         iCodeList.add(new ICode(newLabel, "GE", LHS.getKey(), RHS.getKey(), temp.getKey(), "; " + LHS.getLexi().lexi + " >= " + RHS.getLexi().lexi + " -> " + temp.getLexi().lexi));
                     } else if (oprName.equals("==")) {
                         iCodeList.add(new ICode(newLabel, "EQ", LHS.getKey(), RHS.getKey(), temp.getKey(), "; " + LHS.getLexi().lexi + " == " + RHS.getLexi().lexi + " -> " + temp.getLexi().lexi));
                     } else if (oprName.equals("<")) {
                         iCodeList.add(new ICode(newLabel, "LT", LHS.getKey(), RHS.getKey(), temp.getKey(), "; " + LHS.getLexi().lexi + " < " + RHS.getLexi().lexi + " -> " + temp.getLexi().lexi));
                     } else if (oprName.equals(">")) {
                         iCodeList.add(new ICode(newLabel, "GT", LHS.getKey(), RHS.getKey(), temp.getKey(), "; " + LHS.getLexi().lexi + " > " + RHS.getLexi().lexi + " -> " + temp.getLexi().lexi));
                     } else if (oprName.equals("!=")) {
                         iCodeList.add(new ICode(newLabel, "NE", LHS.getKey(), RHS.getKey(), temp.getKey(), "; " + LHS.getLexi().lexi + " != " + RHS.getLexi().lexi + " -> " + temp.getLexi().lexi));
                     }
                     return;
                 }
 
             }
 
             if (!LHS.getType().equals(RHS.getType())) {
                 errorList += "left and right operand types are incompatible. Line: " + opr.getLexi().lineNum + "\n";
                 return;
             }
 
             SAR temp = new SAR(new Tuple("T" + symIdInr, RHS.getLexi().type, RHS.getLexi().lineNum), RHS.getScope(), "bool", "T" + symIdInr);
             addToSymbolTable("tvar", new ArrayList<String>(), "bool", "private", "T" + symIdInr, RHS.getLexi().lineNum);
             SAS.push(temp);
 
             if (oprName.equals("<=")) {
                 iCodeList.add(new ICode(newLabel, "LE", LHS.getKey(), RHS.getKey(), temp.getKey(), "; " + LHS.getLexi().lexi + " <= " + RHS.getLexi().lexi + " -> " + temp.getLexi().lexi));
             } else if (oprName.equals(">=")) {
                 iCodeList.add(new ICode(newLabel, "GE", LHS.getKey(), RHS.getKey(), temp.getKey(), "; " + LHS.getLexi().lexi + " >= " + RHS.getLexi().lexi + " -> " + temp.getLexi().lexi));
             } else if (oprName.equals("==")) {
                 iCodeList.add(new ICode(newLabel, "EQ", LHS.getKey(), RHS.getKey(), temp.getKey(), "; " + LHS.getLexi().lexi + " == " + RHS.getLexi().lexi + " -> " + temp.getLexi().lexi));
             } else if (oprName.equals("<")) {
                 iCodeList.add(new ICode(newLabel, "LT", LHS.getKey(), RHS.getKey(), temp.getKey(), "; " + LHS.getLexi().lexi + " < " + RHS.getLexi().lexi + " -> " + temp.getLexi().lexi));
             } else if (oprName.equals(">")) {
                 iCodeList.add(new ICode(newLabel, "GT", LHS.getKey(), RHS.getKey(), temp.getKey(), "; " + LHS.getLexi().lexi + " > " + RHS.getLexi().lexi + " -> " + temp.getLexi().lexi));
             } else if (oprName.equals("!=")) {
                 iCodeList.add(new ICode(newLabel, "NE", LHS.getKey(), RHS.getKey(), temp.getKey(), "; " + LHS.getLexi().lexi + " != " + RHS.getLexi().lexi + " -> " + temp.getLexi().lexi));
             }
 
 
         }
         if (!newLabel.isEmpty()) {
             condLabel = "";
             condTypeScope = "";
         }
     }
 
     private boolean sarEqualAssignment(SAR rhs, SAR lhs) {
         if (lhs.getLexi().type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name())) {
             if (Character.isUpperCase(lhs.getLexi().lexi.toCharArray()[0])) {
                 errorList += "left hand side must be an object of a class or type. Line:" + lhs.getLexi().lineNum + "\n";
                 return false;
             }
         }
 
         if (rhs.getLexi().lexi.equals("null")) {
             if (lhs.getType().equals("int") || lhs.getType().equals("char") || lhs.getType().equals("bool")) {
                 errorList += "cannot set raw types to null. Line:" + lhs.getLexi().lineNum + "\n";
                 return false;
             } else {
                 return true;
             }
         }
 
         return lhs.getType().equals(rhs.getType());
     }
 
     private int setPrecedence(String opr) {
         if (opr.equals(".") || opr.equals("(") || opr.equals("[")) {
             return 15;
         } else if (opr.equals(")") || opr.equals("]")) {
             return 0;
         } else if (opr.equals("*") || opr.equals("/") || opr.equals("%")) {
             return 13;
         } else if (opr.equals("+") || opr.equals("-")) {
             return 11;
         } else if (opr.equals("<") || opr.equals(">") || opr.equals("<=") || opr.equals(">=")) {
             return 9;
         } else if (opr.equals("==") || opr.equals("!=")) {
             return 7;
         } else if (opr.equals("&&")) {
             return 5;
         } else if (opr.equals("||")) {
             return 3;
         } else if (opr.equals("=")) {
             return 1;
         }
         return 0;
     }
 
     private boolean isExpressionZ(Tuple lexi) {
         return (lexi.type.equals(LexicalAnalyzer.tokenTypesEnum.BOOLEAN_OPR.name()) || lexi.type.equals(LexicalAnalyzer.tokenTypesEnum.MATH_OPR.name()) || lexi.type.equals(LexicalAnalyzer.tokenTypesEnum.RELATIONAL_OPR.name()) || lexi.type.equals(LexicalAnalyzer.tokenTypesEnum.ASSIGNMENT_OPR.name()) || lexi.type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name()) || lexi.type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name()) || lexi.type.equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_BEGIN.name()) || lexi.type.equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_END.name()) || lexi.lexi.equals(".") || lexi.type.equals(LexicalAnalyzer.tokenTypesEnum.IO_OPR.name()));
     }
 
     private boolean iExist(SAR sar, Stack<SAR> SAS, List<Tuple> tempList, String globalScope, Stack<SAR> OS) {
         String sarScope = sar.getScope();
         while (!sarScope.isEmpty()) {
             for (String key : symbolTable.keySet()) {
                 Symbol s = symbolTable.get(key);
                 if (sarScope.equals("g")) {
                     if (s.getValue().equals(sar.getLexi().lexi) && !(s.getData() instanceof ClassData)) {
                         if (s.getData() instanceof VaribleData) {
                             sar.setType(((VaribleData) s.getData()).getType());
                             sar.setKey(key);
 
                             // check for array
                             if (tempList.get(eIndex + 1).type.equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_BEGIN.name())) {
                                 if (tempList.get(eIndex + 2).type.equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_END.name())) {
                                     eIndex += 2;
                                     return true;
                                 }
 
                                 if (!isValidArrayIndexingType(tempList, eIndex + 2, globalScope, SAS, OS)) {
                                     errorList += "Invalid array operation. Line: " + tempList.get(eIndex).lineNum + "\n";
                                     return false;
                                 }
 
                                 if (tempList.get(eIndex + 1).type.equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_END.name())) {
                                     return true;
                                 }
 
                                 if (!tempList.get(eIndex + 1).type.equals(LexicalAnalyzer.tokenTypesEnum.MATH_OPR.name())) {
                                     errorList += "Invalid array operation. Line: " + tempList.get(eIndex).lineNum + "\n";
                                     return false;
                                 }
 
                                 if (!isValidArrayIndexingType(tempList, eIndex + 2, globalScope, SAS, OS)) {
                                     errorList += "Invalid array operation. Line: " + tempList.get(eIndex).lineNum + "\n";
                                     return false;
                                 }
                                 return true;
                             }
 
 
                         } else if (s.getData() instanceof FunctionData) {
 
                             if (globalScope.equals("g") && !sar.getLexi().lexi.equals("main")) {
                                 errorList += "function cannot be declared outside of a class. Line: " + sar.getLexi().lineNum + "\n";
                                 return false;
                             }
 
                             sar.setType(((FunctionData) s.getData()).getReturnType());
                             sar.setKey(key);
                             if (((FunctionData) s.getData()).getParameters().size() == 0) {
                                 if (!tempList.get(eIndex + 1).lexi.equals("(")) {
                                     errorList += "Invalid function call. Line: " + sar.getLexi().lineNum + "\n";
                                     return false;
                                 }
 
                                 if (!tempList.get(eIndex + 2).lexi.equals(")")) {
                                     errorList += "Invalid function call. '" + sar.getLexi().lexi + "' has no parameters. Line: " + sar.getLexi().lineNum + "\n";
                                     return false;
                                 }
                             } else {
                                 if (!tempList.get(tempList.size() - 1).type.equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_BEGIN.name())) {
                                     if (!doParametersExist(SAS, tempList, globalScope, eIndex + 2, ((FunctionData) s.getData()).getParameters(), ((FunctionData) s.getData()).getParameters().size() - 1, false, OS)) {
                                         errorList += "Invalid parameter list. Line: " + sar.getLexi().lineNum + "\n";
                                         return false;
                                     }
                                 }
                             }
                         }
                         return true;
                     }
                 }
 
                 if (s.getValue().equals(sar.getLexi().lexi) && s.getScope().equals(sarScope) && !(s.getData() instanceof ClassData)) {
                     if (s.getData() instanceof VaribleData) {
                         sar.setType(((VaribleData) s.getData()).getType());
                         sar.setKey(key);
 
                         // check for array
                         if (tempList.get(eIndex + 1).type.equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_BEGIN.name())) {
                             if (tempList.get(eIndex + 2).type.equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_END.name())) {
                                 eIndex += 2;
                                 return true;
                             }
 
                             int index = eIndex + 2;
                             if (!isValidArrayIndexingType(tempList, index, globalScope, SAS, OS)) {
                                 errorList += "Invalid array operation. Line: " + tempList.get(eIndex).lineNum + "\n";
                                 return false;
                             }
 
                             if (tempList.get(eIndex + 1).type.equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_END.name())) {
                                 return true;
                             }
 
                             if (!tempList.get(eIndex + 1).type.equals(LexicalAnalyzer.tokenTypesEnum.MATH_OPR.name())) {
                                 errorList += "Invalid array operation. Line: " + tempList.get(eIndex).lineNum + "\n";
                                 return false;
                             }
                             index = eIndex + 2;
 
                             if (!isValidArrayIndexingType(tempList, index, globalScope, SAS, OS)) {
                                 errorList += "Invalid array operation. Line: " + tempList.get(eIndex).lineNum + "\n";
                                 return false;
                             }
                             eIndex = index;
                             return true;
                         }
 
 
                     } else if (s.getData() instanceof FunctionData) {
                         // need to do work here
                         sar.setType(((FunctionData) s.getData()).getReturnType());
                         sar.setKey(key);
 
                         if (((FunctionData) s.getData()).getParameters().size() == 0) {
                             if (!tempList.get(eIndex + 1).lexi.equals("(")) {
                                 errorList += "Invalid function call. Line: " + sar.getLexi().lineNum + "\n";
                                 return false;
                             }
 
                             if (!tempList.get(eIndex + 2).lexi.equals(")")) {
                                 errorList += "Invalid function call. '" + sar.getLexi().lexi + "' has no parameters. Line: " + sar.getLexi().lineNum + "\n";
                                 return false;
                             }
                         } else {
                             if (!tempList.get(tempList.size() - 1).type.equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_BEGIN.name())) {
                                 int index = eIndex;
                                 if (!doParametersExist(SAS, tempList, globalScope, index + 2, ((FunctionData) s.getData()).getParameters(), ((FunctionData) s.getData()).getParameters().size() - 1, false, OS)) {
                                     errorList += "Invalid parameter list. Line: " + sar.getLexi().lineNum + "\n";
                                     return false;
                                 }
                                 eIndex++;
                             }
                         }
                     }
                     return true;
                 }
             }
 
             if (sarScope.contains(".")) {
                 sarScope = sarScope.substring(0, sarScope.lastIndexOf("."));
             } else {
                 sarScope = "";
             }
         }
         errorList += "'" + sar.getLexi().lexi + "' has not been declared in scope. Line: " + tempList.get(eIndex).lineNum + "\n";
         return false;
     }
 
     private boolean isValidArrayIndexingType(List<Tuple> tempList, int index, String globalScope, Stack<SAR> SAS, Stack<SAR> OS) {
         eIndex = index;
         if (tempList.get(index).type.equals(LexicalAnalyzer.tokenTypesEnum.NUMBER.name())) {
             return true;
         }
 
         if (tempList.get(index).type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name())) {
             if (Character.isUpperCase(tempList.get(index).lexi.toCharArray()[0])) {
                 errorList += "Can not pass a class name as a variable" + tempList.get(index).lineNum + "\n";
                 return false;
             }
 
             SAR sar = new SAR(tempList.get(index), globalScope, "", "");
             if (!iExist(sar, SAS, tempList, globalScope, OS)) {
                 errorList += "Invalid array indexing type. Line: " + tempList.get(index).lineNum + "\n";
                 return false;
             }
 
             if (!sar.getType().equals("int")) {
                 errorList += "Indexing objects must be of type 'int'. Line: " + tempList.get(index).lineNum + "\n";
                 return false;
             }
 
             return true;
         }
 
         errorList += "Invalid array indexing type. Line: " + tempList.get(index).lineNum + "\n";
         return false;
     }
 
     private boolean isIdentifierExpression(Tuple lexi) {
         return (Character.isLowerCase(lexi.lexi.toCharArray()[0]) && lexi.type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name()) || lexi.lexi.equals("main"));
     }
 
     private boolean isLiteralExpression(Tuple lexi) {
         return (lexi.lexi.equals("true") || lexi.lexi.equals("false") || lexi.type.equals(LexicalAnalyzer.tokenTypesEnum.NUMBER.name()) || lexi.type.equals(LexicalAnalyzer.tokenTypesEnum.CHARACTER.name()) || lexi.lexi.equals("null"));
     }
 
     private boolean isLHSofDotValid(List<Tuple> tempList, int index) {
         return (tempList.get(index).lexi.equals("this") || tempList.get(index).type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name()) || tempList.get(index).type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name()) || tempList.get(index).type.equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_END.name()));
     }
 
     private boolean isValidObjectTypeExpression(Tuple lexi) {
         return (lexi.lexi.equals("char") || lexi.lexi.equals("int") || lexi.lexi.equals("bool") || (lexi.type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name()) && Character.isUpperCase(lexi.lexi.toCharArray()[0])));
     }
 
     private boolean isStepValid(String type, String name) {
         return ((type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name()) && Character.isLowerCase(name.toCharArray()[0])) || type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name()));
     }
 
     private void validateComma(Tuple currentLex, Tuple previousLex, Tuple nextLexi) {
         if (previousLex == null || nextLexi == null || nextLexi.type.equals(LexicalAnalyzer.tokenTypesEnum.EOT.name())) {
             errorList += "There must be a value on both sides of comma. Line: " + currentLex.lineNum + "\n";
             return;
         }
 
         if ((previousLex.type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name())
                 || previousLex.lexi.equals("true") || previousLex.lexi.equals("false")
                 || previousLex.type.equals(LexicalAnalyzer.tokenTypesEnum.NUMBER.name())
                 || previousLex.type.equals(LexicalAnalyzer.tokenTypesEnum.CHARACTER.name())
                 || previousLex.type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) &&
                 (nextLexi.type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name())
                         || nextLexi.lexi.equals("true")
                         || nextLexi.lexi.equals("false")
                         || nextLexi.type.equals(LexicalAnalyzer.tokenTypesEnum.NUMBER.name())
                         || nextLexi.type.equals(LexicalAnalyzer.tokenTypesEnum.CHARACTER.name()))
                 || nextLexi.lexi.equals("int")
                 || nextLexi.lexi.equals("char")
                 || nextLexi.lexi.equals("bool")) {
             return;
         }
         errorList += "There must be a value on both sides of comma. Line: " + currentLex.lineNum + "\n";
     }
 
     private boolean isValidParameterDeclarationType(List<Tuple> tempList, int index) {
         if (tempList.get(index).lexi.equals("bool") || tempList.get(index).lexi.equals("int") || tempList.get(index).lexi.equals("char") || (tempList.get(index).type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name()) && Character.isUpperCase(tempList.get(index).lexi.toCharArray()[0]))) {
             if (!tempList.get(index + 1).type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name())) {
                 if (!tempList.get(index + 1).type.equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_BEGIN.name())) {
                     return false;
                 }
                 if (!tempList.get(index + 2).type.equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_END.name())) {
                     return false;
                 }
 
                 if (!tempList.get(index + 3).type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name())) {
                     return false;
                 }
 
                 index += 2;
             }
             if (!Character.isLowerCase(tempList.get(index + 1).lexi.toCharArray()[0])) {
                 errorList += "Parameter names must be lowercase. Line: " + tempList.get(index).lineNum + "\n";
                 return false;
             }
             if (tempList.get(index + 2).lexi.equals(",")) {
                 if (isValidParameterDeclarationType(tempList, index + 3)) {
                     scope += "." + tempList.get(2).lexi;
                     addToSymbolTable("pvar", new ArrayList<String>(), tempList.get(index).lexi, "private", tempList.get(index + 1).lexi, tempList.get(0).lineNum);
                     paramIdList.add(tempList.get(index).lexi);
                     scope = scope.substring(0, scope.lastIndexOf("."));
                     return true;
                 }
                 return false;
             } else if (tempList.get(index + 2).type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                 if (tempList.get(1).type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name()) && Character.isUpperCase(tempList.get(1).lexi.toCharArray()[0])) {
                     scope += "." + tempList.get(1).lexi;
                 } else {
                     scope += "." + tempList.get(2).lexi;
                 }
                 addToSymbolTable("pvar", new ArrayList<String>(), tempList.get(index).lexi, "private", tempList.get(index + 1).lexi, tempList.get(0).lineNum);
                 paramIdList.add(tempList.get(index).lexi);
                 scope = scope.substring(0, scope.lastIndexOf("."));
                 return true;
             } else {
                 return false;
             }
         }
         return false;
     }
 
     private boolean isValidCalledParameterType(List<Tuple> tempList, int index) {
         if ((tempList.get(index).type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name()) && Character.isLowerCase(tempList.get(index).lexi.toCharArray()[0])) || tempList.get(index).type.equals(LexicalAnalyzer.tokenTypesEnum.NUMBER.name()) || tempList.get(index).type.equals(LexicalAnalyzer.tokenTypesEnum.CHARACTER.name())) {
             if (tempList.get(index + 1).lexi.equals(".")) {
                 index = index + 2;
                 if (!isValidCalledParameterType(tempList, index)) {
                     return false;
                 }
             }
             if (tempList.get(index + 1).lexi.equals("(")) {
                 if (!tempList.get(index + 2).lexi.equals(")")) {
                     index = index + 2;
                     if (!isValidCalledParameterType(tempList, index)) {
                         return false;
                     }
                 } else {
                     index++;
                 }
             }
             if (tempList.get(index + 1).lexi.equals(",")) {
                 return isValidCalledParameterType(tempList, index + 2);
             } else return tempList.get(index + 1).type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name());
         }
         return false;
     }
 
     private boolean isValidRelationParameterType(List<Tuple> tempList, int index) {
         if (!validRelationType(tempList, index)) {
             return false;
         }
 
         if (tempList.size() == 5) {
             if (tempList.get(index).type.equals(LexicalAnalyzer.tokenTypesEnum.NUMBER.name()) || tempList.get(index).type.equals(LexicalAnalyzer.tokenTypesEnum.CHARACTER.name())) {
                 return false;
             }
         }
 
         if (tempList.get(index).lexi.equals("(")) {
             index++;
         }
 
         // check if item is an array
         if (tempList.get(index + 1).type.equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_BEGIN.name())) {
             if (!tempList.get(index + 2).type.equals(LexicalAnalyzer.tokenTypesEnum.NUMBER.name())) {
                 return false;
             }
             if (!tempList.get(index + 3).type.equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_END.name())) {
                 return false;
             }
             index = index + 3;
         }
 
         if (tempList.get(index + 1).lexi.equals(".")) {
             index = index + 2;
             if (!isValidRelationParameterType(tempList, index)) {
                 return false;
             }
         }
 
         if (tempList.get(index + 1).lexi.equals("(")) {
             if (!tempList.get(index + 2).lexi.equals(")")) {
                 index = index + 2;
                 if (!isValidRelationParameterType(tempList, index)) {
                     return false;
                 }
             } else {
                 index++;
             }
         }
 
         if (tempList.get(index + 1).type.equals(LexicalAnalyzer.tokenTypesEnum.BOOLEAN_OPR.name()) || tempList.get(index + 1).type.equals(LexicalAnalyzer.tokenTypesEnum.RELATIONAL_OPR.name()) || tempList.get(index + 1).type.equals(LexicalAnalyzer.tokenTypesEnum.MATH_OPR.name())) {
             return isValidRelationParameterType(tempList, index + 2);
         } else if (tempList.get(index + 1).lexi.equals(",")) {
             return isValidRelationParameterType(tempList, index + 2);
         } else
             return tempList.get(index + 1).type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name());
 
     }
 
     private boolean validRelationType(List<Tuple> tempList, int index) {
         return (tempList.get(index).type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name()) && Character.isLowerCase(tempList.get(index).lexi.toCharArray()[0])) || tempList.get(index).type.equals(LexicalAnalyzer.tokenTypesEnum.NUMBER.name()) || tempList.get(index).type.equals(LexicalAnalyzer.tokenTypesEnum.CHARACTER.name()) || tempList.get(index).type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name()) || tempList.get(index).lexi.equals("true") || tempList.get(index).lexi.equals("false") || tempList.get(index).lexi.equals("null");
     }
 
     private boolean isValidCalledParameterTypeITOA(List<Tuple> tempList, int index) {
         if ((tempList.get(index).type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name()) && Character.isLowerCase(tempList.get(index).lexi.toCharArray()[0])) || tempList.get(index).type.equals(LexicalAnalyzer.tokenTypesEnum.NUMBER.name()) || tempList.get(index).type.equals(LexicalAnalyzer.tokenTypesEnum.CHARACTER.name())) {
             if (tempList.get(index + 1).lexi.equals(".")) {
                 index = index + 2;
                 if (!isValidCalledParameterTypeITOA(tempList, index)) {
                     return false;
                 }
             }
             if (tempList.get(index + 1).lexi.equals("(")) {
                 if (!tempList.get(index + 2).lexi.equals(")")) {
                     index = index + 2;
                     if (!isValidCalledParameterTypeITOA(tempList, index)) {
                         return false;
                     }
                 } else {
                     index++;
                 }
             } else if (tempList.get(index + 1).lexi.equals(",")) {
                 return true;
             }
             if (tempList.get(index + 1).type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                 return true;
             }
         }
         return false;
     }
 
     private boolean isValidCalledParameterTypeITOALast(List<Tuple> tempList, int index) {
         if ((tempList.get(index).type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name()) && Character.isLowerCase(tempList.get(index).lexi.toCharArray()[0])) || tempList.get(index).type.equals(LexicalAnalyzer.tokenTypesEnum.NUMBER.name()) || tempList.get(index).type.equals(LexicalAnalyzer.tokenTypesEnum.CHARACTER.name())) {
             if (tempList.get(index + 1).lexi.equals(".")) {
                 index = index + 2;
                 if (!isValidCalledParameterTypeITOALast(tempList, index)) {
                     return false;
                 }
             }
             if (tempList.get(index + 1).lexi.equals("(")) {
                 if (!tempList.get(index + 2).lexi.equals(")")) {
                     index = index + 2;
                     if (!isValidCalledParameterTypeITOALast(tempList, index)) {
                         return false;
                     }
                 } else {
                     index++;
                 }
             }
             if (tempList.get(index + 1).type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                 return true;
             }
         }
         return false;
     }
 
     private boolean isValidReturnType(String retName, String retType) {
         return (retName.equals("bool") || retName.equals("int") || retName.equals("char") || retName.equals("void") || (retType.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name()) && Character.isUpperCase(retName.toCharArray()[0])));
     }
 
     private void validateIOOpr(Tuple currentLex, Tuple previousLex, Tuple nextLex, Tuple peekPrevious) {
         if (currentLex.lexi.equals("<<")) {
             if (previousLex == null) {
                 errorList += "Must start an ostream operation with 'cout'. Line: " + currentLex.lineNum + "\n";
                 return;
             }
 
             if (!previousLex.lexi.equals("cout")) {
                 errorList += "Must start an ostream operation with 'cout'. Line: " + currentLex.lineNum + "\n";
             }
         }
 
         if (currentLex.lexi.equals(">>")) {
             if (previousLex == null) {
                 errorList += "Must start an istream operation with 'cin'. Line: " + currentLex.lineNum + "\n";
                 return;
             }
 
             if (!previousLex.lexi.equals("cin")) {
                 errorList += "Must start an istream operation with 'cin'. Line: " + currentLex.lineNum + "\n";
             }
         }
 
         if (nextLex == null || nextLex.type.equals(LexicalAnalyzer.tokenTypesEnum.EOT.name())) {
             errorList += "IO operators requires a right hand value. Line: " + currentLex.lineNum + "\n";
         }
 
         if (!isLHSinValidFormat(peekPrevious)) {
             errorList += "Only the IO statement can occupy the line (there should not be anything before it). Line: " + currentLex.lineNum + "\n";
         }
 
         if (currentLex.lexi.equals(">>"))
             if (nextLex == null || (!nextLex.type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name()) && !Character.isLowerCase(nextLex.lexi.toCharArray()[0]))) {
                 errorList += "Right hand side of istream operator must be an Identifier. Line: " + currentLex.lineNum + "\n";
             } else {
                 if (isRHSinValidFormatAssignment(nextLex)) {
                     return;
                 }
                 errorList += "Right hand side of ostream operator must be either an Identifier, Number, or Character. Line: " + currentLex.lineNum + "\n";
             }
     }
 
     private boolean canAddToList(Tuple temp) {
         return !(temp.type.equals(LexicalAnalyzer.tokenTypesEnum.EOT.name()) || temp.type.equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_END.name()) || temp.type.equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_BEGIN.name()));
     }
 
     private void validateBooleanOpr(Tuple currentLex, Tuple previousLex, Tuple nextLex) {
         if (nextLex == null || nextLex.type.equals(LexicalAnalyzer.tokenTypesEnum.EOT.name()) || previousLex == null) {
             errorList += "There must be a valid type on both sides of the boolean operator. Line: " + currentLex.lineNum + "\n";
             return;
         }
 
         if (!isLHSinValidFormatRelationShip(previousLex)) {
             errorList += "Left hand side of the boolean operator must be an Identifier, Number, or Character. Line: " + previousLex.lineNum + "\n";
         }
 
         if (isRHSinValidFormatAssignment(nextLex)) {
             return;
         }
         errorList += "Right hand side of boolean operator must be either an Identifier, Number, or Character. Line: " + currentLex.lineNum + "\n";
     }
 
     private void validateReturnStatement(Tuple currentLex, Tuple nextLex) {
         if (!isReturnValueValid(nextLex)) {
             errorList += "Return statement must either be followed by a value or an end of line token (;). Line: " + currentLex.lineNum + "\n";
         }
     }
 
     private boolean isReturnValueValid(Tuple nextLex) {
         if (nextLex == null)
             return false;
         else if (nextLex.type.equals(LexicalAnalyzer.tokenTypesEnum.NUMBER.name()) || nextLex.type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name()) || nextLex.type.equals(LexicalAnalyzer.tokenTypesEnum.CHARACTER.name()) || nextLex.type.equals(LexicalAnalyzer.tokenTypesEnum.EOT.name()) || nextLex.type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name()) || nextLex.lexi.equals("true") || nextLex.lexi.equals("false"))
             return true;
         return false;
     }
 
     private void validateRelationalOpr(Tuple currentLex, Tuple previousLex, Tuple nextLex) {
         if (nextLex == null || nextLex.type.equals(LexicalAnalyzer.tokenTypesEnum.EOT.name()) || previousLex == null) {
             errorList += "There must be a valid type on both sides of the relational operator. Line: " + currentLex.lineNum + "\n";
             return;
         }
 
         if (!isLHSinValidFormatRelationShip(previousLex)) {
             errorList += "Left hand side of the relational operator must be an Identifier, Number, or Character. Line: " + previousLex.lineNum + "\n";
         }
 
         if (isRHSinValidFormatAssignment(nextLex)) {
             return;
         }
         errorList += "Right hand side of relational operatior must be either an Identifier, Number, or Character. Line: " + currentLex.lineNum + "\n";
     }
 
     private void validateMathOpr(Tuple currentLex, Tuple previousLex, Tuple nextLex) {
         if (nextLex == null || nextLex.type.equals(LexicalAnalyzer.tokenTypesEnum.EOT.name())) {
             errorList += "Mathematical operators require a right hand value. Line: " + currentLex.lineNum + "\n";
             return;
         }
 
         if (currentLex.lexi.equals("-") && previousLex == null) {
             return;
         }
 
         if (previousLex == null) {
             errorList += "Mathematical operators require a right hand value. Line: " + currentLex.lineNum + "\n";
             return;
         }
 
         if (((previousLex.type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name()) || previousLex.type.equals(LexicalAnalyzer.tokenTypesEnum.NUMBER.name()) || previousLex.lexi.equals(")")) && (nextLex.lexi.equals("-") || nextLex.type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name()) || nextLex.type.equals(LexicalAnalyzer.tokenTypesEnum.NUMBER.name()) || nextLex.lexi.equals("(")))) {
             return;
         }
 
         if (currentLex.lexi.equals("-") && (previousLex.type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name()) || previousLex.type.equals(LexicalAnalyzer.tokenTypesEnum.NUMBER.name()) || previousLex.lexi.equals(")") || previousLex.type.equals(LexicalAnalyzer.tokenTypesEnum.MATH_OPR.name()))) {
             return;
         }
 
         errorList += "Both side of mathematical operation must be either an Identifier or a Number. Line: " + currentLex.lineNum + "\n";
     }
 
     private void validateAssignmentOpr(Tuple currentLex, Tuple previousLex, Tuple nextLex) throws IllegalArgumentException {
         if (nextLex == null || nextLex.type.equals(LexicalAnalyzer.tokenTypesEnum.EOT.name()) || previousLex == null) {
             errorList += "There must be a valid type on both sides of the assignment operator. Line: " + currentLex.lineNum + "\n";
             return;
         }
 
         if (!isPreviousLexiValidAssignment(previousLex)) {
             errorList += "Left hand side of assignment operation must be an Identifier. Line: " + previousLex.lineNum + "\n";
         }
 
         if (isRHSinValidFormatAssignment(nextLex)) {
             return;
         }
         errorList += "Right hand side of assignment operation must be either an Identifier, Number, or Character. Line: " + currentLex.lineNum + "\n";
     }
 
     private boolean isRHSinValidFormatAssignment(Tuple nextLex) {
         if (nextLex.type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name()))
             return true;
         else if (nextLex.type.equals(LexicalAnalyzer.tokenTypesEnum.NUMBER.name()))
             return true;
         else if (nextLex.type.equals(LexicalAnalyzer.tokenTypesEnum.CHARACTER.name()))
             return true;
         else if (nextLex.lexi.equals("true") || nextLex.lexi.equals("false") || nextLex.lexi.equals("null") || nextLex.lexi.equals("this") || nextLex.lexi.equals("new") || nextLex.lexi.equals("atoi") || nextLex.lexi.equals("itoa"))
             return true;
         else if (nextLex.type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
             return true;
         } else if (nextLex.type.equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_BEGIN.name())) {
             return true;
         }
 
         return false;
     }
 
     private boolean isLHSinValidFormatRelationShip(Tuple nextLex) {
         if (nextLex.type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name()))
             return true;
         else if (nextLex.type.equals(LexicalAnalyzer.tokenTypesEnum.NUMBER.name()))
             return true;
         else if (nextLex.type.equals(LexicalAnalyzer.tokenTypesEnum.CHARACTER.name()))
             return true;
         else if (nextLex.type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name()))
             return true;
         else if (nextLex.type.equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_END.name()))
             return true;
 
         return false;
     }
 
     private boolean isPreviousLexiValidAssignment(Tuple previousLex) {
         if (previousLex.type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name()))
             return true;
         else if (previousLex.type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name()))
             return true;
         else if (previousLex.type.equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_END.name()))
             return true;
 
         return false;
     }
 
     private boolean isLHSinValidFormat(Tuple peekPrevious) {
         return (peekPrevious == null || peekPrevious.type.equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_END.name()) || peekPrevious.type.equals(LexicalAnalyzer.tokenTypesEnum.EOT.name()));
     }
 
     private void addToSymbolTable(String type, List<String> params, String returnType, String accessMod, String value, int lineNum) {
         for (String key : symbolTable.keySet()) {
             Symbol s = symbolTable.get(key);
             if (s.getScope().equals(scope) && s.getValue().equals(value) && s.getKind().equals(type) && !type.equals("Literal")) {
                 if (value.length() > 1) {
                     char[] test = value.toCharArray();
                     if (test[0] == 'x' && Character.isDigit(test[1])) {
                         return;
                     }
                     if (test[0] == '\'' && test[1] == '\\' && value.length() == 4) {
                         return;
                     }
                 }
                 errorList += "Found duplicate declaration of '" + value + "'. Line: " + lineNum + "\n";
                 return;
             }
         }
 
         if (type.equals("Class")) {
             symbolTable.put("C" + symIdInr, new Symbol(scope, "C" + symIdInr++, value, type, new ClassData()));
             scope += "." + value;
 
         } else if (type.equals("Function")) {
             if (value.equals("main")) {
                 symbolTable.put("F_MAIN" + symIdInr, new Symbol(scope, "F" + symIdInr++, value, type, new FunctionData(accessMod, params, returnType)));
             } else {
                 iCodeList.add(new ICode("F" + symIdInr, "CREATE", ".BYT", "", "", "; function " + value));
                 symbolTable.put("F" + symIdInr, new Symbol(scope, "F" + symIdInr++, value, type, new FunctionData(accessMod, params, returnType)));
             }
             scope += "." + value;
 
         } else if (type.equals("pvar")) {
             if (returnType.equals("int")) {
                 iCodeList.add(new ICode("P" + symIdInr, "CREATE", ".INT", "", "", "; int " + value));
             } else {
                 iCodeList.add(new ICode("P" + symIdInr, "CREATE", ".BYT", "", "", "; char " + value));
             }
             symbolTable.put("P" + symIdInr, new Symbol(scope, "P" + symIdInr++, value, type, new VaribleData(returnType, accessMod)));
 
         } else if (type.equals("Literal")) {
             if (returnType.equals("int")) {
                 iCodeList.add(new ICode("L" + symIdInr, "CREATE", ".INT", "", "", "; int " + value));
             } else {
                 iCodeList.add(new ICode("L" + symIdInr, "CREATE", ".BYT", "", "", "; char " + value));
             }
             symbolTable.put("L" + symIdInr, new Symbol(scope, "L" + symIdInr++, value, type, new VaribleData(returnType, accessMod)));
 
         } else if (type.equals("tvar")) {
             if (returnType.equals("int")) {
                 iCodeList.add(new ICode("T" + symIdInr, "CREATE", ".INT", "", "", "; int " + value));
             } else {
                 iCodeList.add(new ICode("T" + symIdInr, "CREATE", ".BYT", "", "", "; char " + value));
             }
             symbolTable.put("T" + symIdInr, new Symbol(scope, "T" + symIdInr++, value, type, new VaribleData(returnType, accessMod)));
 
         } else if (type.equals("iovar")) {
             symbolTable.put("IO" + symIdInr, new Symbol(scope, "IO" + symIdInr++, value, type, new VaribleData(returnType, accessMod)));
 
         } else if (type.equals("Condition")) {
             symbolTable.put("C" + symIdInr, new Symbol(scope, "C" + symIdInr++, value, type, new VaribleData(returnType, accessMod)));
             scope += "." + value;
 
         } else {
             if (returnType.equals("int")) {
                 iCodeList.add(new ICode("V" + symIdInr, "CREATE", ".INT", "", "", "; int " + value));
             } else {
                 iCodeList.add(new ICode("V" + symIdInr, "CREATE", ".BYT", "", "", "; char " + value));
             }
             symbolTable.put("V" + symIdInr, new Symbol(scope, "V" + symIdInr++, value, type, new VaribleData(returnType, accessMod)));
         }
     }
 }
