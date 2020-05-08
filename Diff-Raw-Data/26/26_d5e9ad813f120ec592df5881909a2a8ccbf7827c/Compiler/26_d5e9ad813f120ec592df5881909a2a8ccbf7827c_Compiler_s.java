 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Stack;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Nathanael
  * Date: 1/22/13
  * Time: 8:59 PM
  */
 public class Compiler {
     private LexicalAnalyzer lexicalAnalyzer;
     private List<Tuple<String, String, Integer>> openParens = new ArrayList<Tuple<String, String, Integer>>();
     private List<Tuple<String, String, Integer>> openBlocks = new ArrayList<Tuple<String, String, Integer>>();
     private LinkedHashMap<String, Symbol> symbolTable;
     private List<String> paramIdList = new ArrayList<String>();
     private String errorList = "";
     private String scope = "g";
     private int statementInr = 1;
     private int symIdInr = 100;
     private int eIndex = 0;
 
     public Compiler(LexicalAnalyzer lexicalAnalyzer, LinkedHashMap<String, Symbol> symbolTable) {
         this.lexicalAnalyzer = lexicalAnalyzer;
         this.symbolTable = symbolTable;
     }
 
     public void evaluate() {
         Tuple<String, String, Integer> currentLex;
         Tuple<String, String, Integer> previousLex = null;
 
         while (lexicalAnalyzer.hasNext()) {
             Tuple<String, String, Integer> temp;
             List<Tuple<String, String, Integer>> tempList = new ArrayList<Tuple<String, String, Integer>>();
 
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
 
                 Tuple<String, String, Integer> nextLexi = (i + 1 == tempList.size() ? null : tempList.get(i + 1));
                 Tuple<String, String, Integer> peekPrevious = (i - 2 < 0 ? null : tempList.get(i - 2));
 
 
                 // validate expressions
                 if (currentLex.type.equals(LexicalAnalyzer.tokenTypesEnum.ASSIGNMENT_OPR.name())) {
                     validateAssignmentOpr(currentLex, previousLex, nextLexi);
                 } else if (currentLex.type.equals(LexicalAnalyzer.tokenTypesEnum.MATH_OPR.name())) {
                     validateMathOpr(currentLex, previousLex, nextLexi);
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
 
                     if (currentLex.lexi.equals("class")) {
                         int errCount = errorList.length();
                         if (!tempList.get(0).lexi.equals("class")) {
                             errorList += "class declaration must be the first statement on the line. Line: " + currentLex.lineNum + "\n";
                         }
                         if (!tempList.get(tempList.size() - 1).type.equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_BEGIN.name())) {
                             errorList += "class declaration line must end with a begin block token ({). Line: " + currentLex.lineNum + "\n";
                         }
                         if (tempList.size() > 3) {
                             errorList += "two many arguments in class declaration. Line: " + currentLex.lineNum + "\n";
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
                                         if (!isValidParameterDeclarationType(tempList, 3)) {
                                             errorList += "Function '" + tempList.get(1).lexi + "' contains an invalid argument list. Line: " + currentLex.lineNum + "\n";
                                         }
                                     } else if (tempList.get(2).type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name()) && tempList.get(3).type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name()) && tempList.get(4).type.equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_BEGIN.name())) {
                                         addToSymbolTable("Function", new ArrayList<String>(), tempList.get(1).lexi, tempList.get(0).lexi, tempList.get(1).lexi, currentLex.lineNum);
                                         // all good
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
                                             errorList += "Function or object has been incorrectly formatted. Line: " + currentLex.lineNum + "\n";
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
                         if (!isValidObjectTypeExpression(nextLexi)) {
                             errorList += "Invalid use of the new operator. Line: " + currentLex.lineNum + "\n";
                             previousLex = currentLex;
                             continue;
                         }
                         if (tempList.get(i + 2).type.equals(LexicalAnalyzer.tokenTypesEnum.EOT.name())) {
                             // all good
                             previousLex = currentLex;
                             continue;
                         }
                         if (tempList.get(i + 2).type.equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_BEGIN.name())) {
                             if (tempList.size() < 11) {
                                 errorList += "Invalid array initialization. Line: " + currentLex.lineNum + "\n";
                                 previousLex = currentLex;
                                 continue;
                             }
                             if ((tempList.get(i + 3).type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name()) || tempList.get(i + 3).type.equals(LexicalAnalyzer.tokenTypesEnum.NUMBER.name())) && tempList.get(i + 4).type.equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_END.name()) && tempList.get(i + 5).type.equals(LexicalAnalyzer.tokenTypesEnum.EOT.name())) {
                                 // all good
                             } else {
                                 errorList += "Invalid array initialization. Line: " + currentLex.lineNum + "\n";
                                 previousLex = currentLex;
                                 continue;
                             }
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
 
                         if (!tempList.get(tempList.size() -1).lexi.equals("{")) {
                             errorList += "If statement blocks must be contained within block brackets '{}'. Line: " + currentLex.lineNum + "\n";
                             previousLex = currentLex;
                             continue;
                         }
 
                         scope += ".if" + statementInr++;
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
                             errorList += "Invalid else statement. Line: " + currentLex.lineNum + "\n";
                             previousLex = currentLex;
                             continue;
                         }
                         scope += ".else" + statementInr++;
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
                         scope += ".while" + statementInr++;
                         previousLex = currentLex;
                         continue;
                     }
 
                 }
 
 
                 /**
                  * validate object type declaration
                  */
                 if (currentLex.type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name()) && i == 0) {
                     if (tempList.get(tempList.size() -1).lexi.equals("{")) {
                         if (tempList.get(i + 1).type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
                             if (Character.isLowerCase(currentLex.lexi.toCharArray()[0])) {
                                 errorList += "Constructor name must start with a capital letter. Line: " + currentLex.lineNum + "\n";
                             }
 
                             if (!tempList.get(i + 2).lexi.equals(")")) {
                                 if (!isValidParameterDeclarationType(tempList, 2)) {
                                     errorList += "Constructor '" + tempList.get(0).lexi + "' contains an invalid argument list. Line: " + currentLex.lineNum + "\n";
                                 }
                             } else {
                                 if (!tempList.get(i + 3).lexi.equals("{")) {
                                     errorList += "Constructor '" + tempList.get(0).lexi + "' cannot contain anything between the parameter list and the start block. Line: " + currentLex.lineNum + "\n";
                                 }
                             }
 
                             i += 2;
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
                     if (tempList.get(i + 1).lexi.equals("static") && tempList.get(i + 2).lexi.equals("void")) {
                         if (tempList.get(i + 3).lexi.equals("main")) {
                             if (!tempList.get(i + 4).type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
                                 errorList += "Main function is missing the argument list. Line: " + currentLex.lineNum + "\n";
                                 previousLex = currentLex;
                                 continue;
                             } else {
                                 if (tempList.get(i + 5).type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                                     if (!tempList.get(i + 6).type.equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_BEGIN.name())) {
                                         errorList += "Main function declaration must end with an block begin token ({). Line: " + currentLex.lineNum + "\n";
                                         previousLex = currentLex;
                                         continue;
                                     }
                                 } else {
                                     errorList += "Main function declaration must end with an block begin token ({). Line: " + currentLex.lineNum + "\n";
                                     previousLex = currentLex;
                                     continue;
                                 }
                             }
                             previousLex = currentLex;
                             continue;
                         }
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
                             errorList += "Syntax error on function call format. Line: " + currentLex.lineNum + "\n";
                             previousLex = currentLex;
                             continue;
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
                             errorList += "Syntax error on array call format. Line: " + currentLex.lineNum + "\n";
                             previousLex = currentLex;
                             continue;
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
 
             for (Tuple<String, String, Integer> item : openParens) {
                 errorMessage += item.lineNum + ", ";
             }
             errorList += errorMessage + "\n";
         }
 
         if (openBlocks.size() > 0) {
             String errorMessage = "Incomplete statements (missing closing blocks) on the following lines: ";
 
             for (Tuple<String, String, Integer> item : openBlocks) {
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
         statementInr = 1;
 
         while (lexicalAnalyzer.hasNext()) {
             // setting up items to be parsed
             Tuple<String, String, Integer> temp;
             List<Tuple<String, String, Integer>> tempList = new ArrayList<Tuple<String, String, Integer>>();
 
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
                 Tuple<String, String, Integer> item = tempList.get(i);
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
                         scopePassTwo += "." + tempList.get(0).lexi + statementInr++;
                     } else {
                         if (isValidReturnType(tempList.get(0).lexi, tempList.get(0).type) || tempList.get(0).lexi.equals("class")) {
                             scopePassTwo += "." + tempList.get(1).lexi;
                         }
                     }
                 }
 
                 if (item.type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name()) && (tempList.get(0).lexi.equals("public") || tempList.get(0).lexi.equals("private") || isValidReturnType(tempList.get(0).lexi, tempList.get(0).type))) {
                     scopePassTwo += "." + tempList.get(2).lexi;
                 }
 
                 if (item.lexi.equals(".")) {
                     isCalled = true;
                     i++;
                     continue;
                 }
 
                 if (isLiteralExpression(tempList.get(i))) {
                     addLiteralExpressionToSAS(scopePassTwo, tempList.get(i), SAS);
                 } else if (isIdentifierExpression(tempList.get(i))) {
                     // todo: need to valid functions as well
                     // functions can not be declared outside of a class
                     SAR sar = new SAR(tempList.get(i), scopePassTwo, "");
                     if (isCalled) {
                         eIndex = i;
                         rExist(sar, SAS, tempList, scopePassTwo);
                         i = eIndex + 1;
 
                         if (tempList.get(i).lexi.equals(";")) {
                             cleanupSAS(SAS, OS);
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
                     if (iExist(sar)) {
                         SAS.push(sar);
                     } else {
                        errorList += "identifier has not been declared in scope. Line: " + tempList.get(i).lineNum + "\n";
                     }
                 } else if (isExpressionZ(tempList.get(i))) {
                     int precedence = setPrecedence(tempList.get(i).lexi);
 
                     if (item.lexi.equals(")")) {
                         while (!OS.peek().getLexi().lexi.equals("(")) {
                             if (OS.peek() == null) {
                                 errorList += "Missing opening paren. Line: " + item.lineNum + "\n";
                             }
                             addTempToSAS(OS.pop(), SAS);
                         }
                         if (tempList.get(0).lexi.equals("public") || tempList.get(0).lexi.equals("private") || isValidReturnType(tempList.get(0).lexi, tempList.get(0).type)) {
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
                             addTempToSAS(OS.pop(), SAS);
                         }
                         if (!OS.isEmpty()) {
                             OS.pop();
                         }
                     } else if (precedence > lastOprPrecedence) {
                         OS.push(new SAR(item, scopePassTwo, ""));
                     } else if (precedence <= lastOprPrecedence) {
                         if (!OS.isEmpty()) {
                             addTempToSAS(OS.pop(), SAS);
                         }
                         OS.push(new SAR(item, scopePassTwo, ""));
                     } else {
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
                 } else if (item.type.equals(LexicalAnalyzer.tokenTypesEnum.EOT.name()))
                     cleanupSAS(SAS, OS);
 
                 // take down scope
                 if (tempList.get(i).type.equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_END.name())) {
                     if (openBlocks.size() == 0) {
                         errorList += "Invalid closing block on line: " + tempList.get(i).lineNum + "\n";
                     } else {
                         if (!scopePassTwo.equals("g")) {
                             scopePassTwo = scopePassTwo.substring(0, scopePassTwo.lastIndexOf('.'));
                         }
                         openBlocks.remove(openBlocks.size() - 1);
                     }
                 }
                 i++;
             }
 
         }
 
         try {
             if (!errorList.isEmpty()) {
                 throw new IllegalArgumentException(errorList);
             }
         } catch (IllegalArgumentException e) {
             System.out.print(e.getMessage());
             System.exit(0);
         }
     }
 
     private void cleanupSAS(Stack<SAR> SAS, Stack<SAR> OS) {
         while (!OS.isEmpty()) addTempToSAS(OS.pop(), SAS);
     }
 
     private void addLiteralExpressionToSAS(String scopePassTwo, Tuple<String, String, Integer> tuple, Stack<SAR> SAS) {
         if (tuple.lexi.equals("true") || tuple.lexi.equals("false")) {
             SAS.push(new SAR(tuple, scopePassTwo, "bool"));
         } else if (tuple.type.equals(LexicalAnalyzer.tokenTypesEnum.NUMBER.name())) {
             SAS.push(new SAR(tuple, scopePassTwo, "int"));
         } else if (tuple.type.equals(LexicalAnalyzer.tokenTypesEnum.CHARACTER.name())) {
             SAS.push(new SAR(tuple, scopePassTwo, "char"));
         } else if (tuple.lexi.equals("null")) {
             SAS.push(new SAR(tuple, scopePassTwo, "null"));
         }
     }
 
     private boolean rExist(SAR sar, Stack<SAR> SAS, List<Tuple<String, String, Integer>> tempList, String globalScope) {
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
 
         return found && evaluateCallies(foundScope, sar, SAS, tempList, globalScope);
     }
 
     private boolean evaluateCallies(String foundScope, SAR sar, Stack<SAR> SAS, List<Tuple<String, String, Integer>> tempList, String globalScope) {
         for (String key : symbolTable.keySet()) {
             Symbol s = symbolTable.get(key);
 
             if (s.getValue().equals(sar.getLexi().lexi) && s.getScope().equals(foundScope)) {
                 if (s.getData() instanceof VaribleData) {
                     if (((VaribleData) s.getData()).getAccessMod().equals("public")) {
                         sar.setType(((VaribleData) s.getData()).getType());
                         String value = "t" + symIdInr;
                         addToSymbolTable("tVar", new ArrayList<String>(), ((VaribleData) s.getData()).getType(), "private", value, sar.getLexi().lineNum);
 
                         Tuple<String, String, Integer> tempLexi = new Tuple<String, String, Integer>(value, LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name(), sar.getLexi().lineNum);
                         SAS.push(new SAR(tempLexi, sar.getScope(), sar.getType()));
 
                         if (tempList.get(eIndex + 1).lexi.equals(".")) {
                             eIndex = eIndex + 2;
                             foundScope = foundScope.substring(0, foundScope.lastIndexOf("."));
                             SAS.pop();
                             return evaluateCallies(foundScope + "." + sar.getType(), new SAR(tempList.get(eIndex), sar.getScope(), ""), SAS, tempList, globalScope);
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
                         String value = "t" + symIdInr;
                         addToSymbolTable("tVar", new ArrayList<String>(), ((FunctionData) s.getData()).getReturnType(), "private", value, sar.getLexi().lineNum);
 
                         Tuple<String, String, Integer> tempLexi = new Tuple<String, String, Integer>(value, LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name(), sar.getLexi().lineNum);
                         SAS.push(new SAR(tempLexi, sar.getScope(), sar.getType()));
 
                         if (tempList.get(eIndex + 3).lexi.equals(".")) {
                             eIndex = eIndex + 4;
                             foundScope = foundScope.substring(0, foundScope.lastIndexOf("."));
                             SAS.pop();
                             return evaluateCallies(foundScope + "." + sar.getType(), new SAR(tempList.get(eIndex), sar.getScope(), ""), SAS, tempList, globalScope);
                         } else {
                             eIndex = eIndex + 2;
                         }
 
                         return true;
 
                     } else {
                         sar.setType(((FunctionData) s.getData()).getReturnType());
                         int index = eIndex + 2;
                         String value = "t" + symIdInr;
                         addToSymbolTable("tVar", new ArrayList<String>(), ((FunctionData) s.getData()).getReturnType(), "private", value, sar.getLexi().lineNum);
 
                         if (!doParametersExist(SAS, tempList, globalScope, index, ((FunctionData) s.getData()).getParameters(), ((FunctionData) s.getData()).getParameters().size() - 1)) {
                             return false;
                         }
 
                         Tuple<String, String, Integer> tempLexi = new Tuple<String, String, Integer>(value, LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name(), sar.getLexi().lineNum);
                         SAS.push(new SAR(tempLexi, sar.getScope(), sar.getType()));
 
                         if (tempList.get(eIndex + 2).lexi.equals(".")) {
                             eIndex += 3;
                             foundScope = foundScope.substring(0, foundScope.lastIndexOf("."));
                             SAS.pop();
                             return evaluateCallies(foundScope + "." + sar.getType(), new SAR(tempList.get(eIndex), sar.getScope(), ""), SAS, tempList, globalScope);
                         }
                         return true;
                     }
                 }
             }
         }
         errorList += sar.getLexi().lexi + " does not exist. Line: " + sar.getLexi().lineNum + "\n";
         return false;
     }
 
     private boolean doParametersExist(Stack<SAR> SAS, List<Tuple<String, String, Integer>> tempList, String globalScope, int index, List<String> paramList, int pId) {
         eIndex = index;
 
         Tuple<String, String, Integer> item = tempList.get(index);
         if (!isLegalValue(item)) {
             errorList += "the function being called has too few parameters. Line: " + item.lineNum + "\n";
 
             System.out.print(errorList);
             System.exit(0);
         }
 
         if (pId < 0) {
             errorList += "the function being called has too many parameters. Line: " + item.lineNum + "\n";
             return false;
         }
 
         if (isLiteralExpression(item)) {
             boolean isGood = true;
             String actualType = "";
 
             if (item.lexi.equals("true") || item.lexi.equals("false")) {
                 if (!paramList.get(pId).equals("bool")) {
                     isGood = false;
                     actualType = item.lexi;
                 }
             } else if (item.type.equals(LexicalAnalyzer.tokenTypesEnum.NUMBER.name())) {
                 if (!paramList.get(pId).equals("int")) {
                     isGood = false;
                     actualType = item.type;
                 }
             } else if (item.type.equals(LexicalAnalyzer.tokenTypesEnum.CHARACTER.name())) {
                 if (!paramList.get(pId).equals("char")) {
                     isGood = false;
                     actualType = item.type;
                 }
             } else if (item.lexi.equals("null")) {
                 if (!paramList.get(pId).equals("null")) {
                     isGood = false;
                     actualType = item.lexi;
                 }
             }
 
             if (!isGood) {
                 errorList += "incompatible parameter type. expected '" + paramList.get(pId) + "' but was '" + actualType + "'. Line: " + item.lineNum + "\n";
                 return false;
             }
         } else if (isIdentifierExpression(item)) {
             String sarScope = globalScope;
             boolean found = false;
             SAR sar = new SAR(item, globalScope, "");
 
             while (!sarScope.isEmpty()) {
                 for (String key : symbolTable.keySet()) {
                     Symbol s = symbolTable.get(key);
                     if (sarScope.equals("g")) {
                         if (s.getValue().equals(sar.getLexi().lexi) && s.getScope().equals(sarScope)) {
                             if (s.getData() instanceof VaribleData) {
                                 sar.setType(((VaribleData) s.getData()).getType());
 
                                 if (((VaribleData) s.getData()).getAccessMod().equals("private") && !globalScope.equals("g")) {
                                     errorList += "Access Error: " + sar.getLexi().lexi + " is a private variable. Line: " + sar.getLexi().lineNum + "\n";
                                     return false;
                                 }
 
                                 if (tempList.get(index + 1).lexi.equals(".")) {
                                     // todo need to check for '.'
                                     //return evaluateCallies(sarScope, sar, SAS, tempList, globalScope);
                                 }
 
                                 if (!paramList.get(pId).equals(sar.getType())) {
                                     errorList += "incompatible parameter type. expected '" + paramList.get(pId) + "' but was '" + sar.getType() + "'. Line: " + item.lineNum + "\n";
                                     return false;
                                 }
 
                             } else if (s.getData() instanceof FunctionData) {
                                 sar.setType(((FunctionData) s.getData()).getReturnType());
 
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
                                     return doParametersExist(SAS, tempList, globalScope, index + 2, paramList, ((FunctionData) s.getData()).getParameters().size() - 1);
                                 }
                             }
                             found = true;
                             break;
                         }
                     }
 
                     if (s.getValue().equals(sar.getLexi().lexi) && s.getScope().equals(sarScope)) {
                         if (s.getData() instanceof VaribleData) {
                             sar.setType(((VaribleData) s.getData()).getType());
 
                             if (((VaribleData) s.getData()).getAccessMod().equals("private") && !globalScope.equals(sarScope)) {
                                 errorList += "Access Error: " + sar.getLexi().lexi + " is a private variable. Line: " + sar.getLexi().lineNum + "\n";
                                 return false;
                             }
 
                             if (tempList.get(index + 1).lexi.equals(".")) {
                                 // todo need to check for '.'
 //                                String newScope = sarScope.substring(0, sarScope.lastIndexOf("."));
 //                                newScope =  newScope + "." + sar.getType();
 //                                return evaluateCallies(newScope, new SAR(tempList.get(index + 2), newScope, ""), SAS, tempList, globalScope);
                             }
 
                             if (!paramList.get(pId).equals(sar.getType())) {
                                 errorList += "incompatible parameter type. expected '" + paramList.get(pId) + "' but was '" + sar.getType() + "'. Line: " + item.lineNum + "\n";
                                 return false;
                             }
 
                         } else if (s.getData() instanceof FunctionData) {
                             sar.setType(((FunctionData) s.getData()).getReturnType());
 
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
                                 return doParametersExist(SAS, tempList, globalScope, index + 2, paramList, ((FunctionData) s.getData()).getParameters().size() - 1);
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
 
         if (tempList.get(index + 1).lexi.equals(",")) {
             return doParametersExist(SAS, tempList, globalScope, index + 2, paramList, pId - 1);
         } else if (tempList.get(index + 1).lexi.equals(")") && pId != 0) {
             errorList += "function has too few parameters. Line: " + item.lineNum + "\n";
             return false;
         } else if (tempList.get(index + 1).lexi.equals(")")) {
             return true;
         }
 
         return false;
     }
 
     private boolean isLegalValue(Tuple<String, String, Integer> item) {
         return ((item.type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name()) && Character.isLowerCase(item.lexi.toCharArray()[0])) || item.type.equals(LexicalAnalyzer.tokenTypesEnum.NUMBER.name()) || item.type.equals(LexicalAnalyzer.tokenTypesEnum.CHARACTER.name()) || item.lexi.equals("true") || item.lexi.equals("false"));
     }
 
     private boolean ClassExist(Tuple<String, String, Integer> item, String sscope) {
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
 
     private void addTempToSAS(SAR opr, Stack<SAR> SAS) {
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
 
             addToSymbolTable("lvar", new ArrayList<String>(), RHS.getType(), "private", "T" + symIdInr++, RHS.getLexi().lineNum);
 
             SAR temp = new SAR(new Tuple<String, String, Integer>("T" + (symIdInr - 2), RHS.getLexi().type, RHS.getLexi().lineNum), RHS.getScope(), RHS.getType());
             SAS.push(temp);
         }
 
     }
 
     private boolean sarEqualAssignment(SAR rhs, SAR lhs) {
         if (lhs.getLexi().type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name())) {
             if (Character.isUpperCase(lhs.getLexi().lexi.toCharArray()[0])) {
                 errorList += "left hand side must be an object of a class or type. Line:" + lhs.getLexi().lineNum + "\n";
                 return false;
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
 
     private boolean isExpressionZ(Tuple<String, String, Integer> lexi) {
         return (lexi.type.equals(LexicalAnalyzer.tokenTypesEnum.BOOLEAN_OPR.name()) || lexi.type.equals(LexicalAnalyzer.tokenTypesEnum.MATH_OPR.name()) || lexi.type.equals(LexicalAnalyzer.tokenTypesEnum.RELATIONAL_OPR.name()) || lexi.type.equals(LexicalAnalyzer.tokenTypesEnum.ASSIGNMENT_OPR.name()) || lexi.type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name()) || lexi.type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name()) || lexi.type.equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_BEGIN.name()) || lexi.type.equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_END.name()) || lexi.lexi.equals("."));
     }
 
     private boolean iExist(SAR sar) {
         String sarScope = sar.getScope();
         while (!sarScope.isEmpty()) {
             for (String key : symbolTable.keySet()) {
                 Symbol s = symbolTable.get(key);
                 if (sarScope.equals("g")) {
                     if (s.getValue().equals(sar.getLexi().lexi) && s.getScope().equals(sarScope)) {
                         if (s.getData() instanceof VaribleData) {
                             sar.setType(((VaribleData) s.getData()).getType());
                         } else if (s.getData() instanceof FunctionData) {
                             sar.setType(((FunctionData) s.getData()).getReturnType());
                         }
                         return true;
                     }
                 }
 
                 if (s.getValue().equals(sar.getLexi().lexi) && s.getScope().equals(sarScope)) {
                     if (s.getData() instanceof VaribleData) {
                         sar.setType(((VaribleData) s.getData()).getType());
                     } else if (s.getData() instanceof FunctionData) {
                         sar.setType(((FunctionData) s.getData()).getReturnType());
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
         return false;
     }
 
     private boolean isIdentifierExpression(Tuple<String, String, Integer> lexi) {
         return (Character.isLowerCase(lexi.lexi.toCharArray()[0]) && lexi.type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name()));
     }
 
     private boolean isLiteralExpression(Tuple<String, String, Integer> lexi) {
         return (lexi.lexi.equals("true") || lexi.lexi.equals("false") || lexi.type.equals(LexicalAnalyzer.tokenTypesEnum.NUMBER.name()) || lexi.type.equals(LexicalAnalyzer.tokenTypesEnum.CHARACTER.name()) || lexi.lexi.equals("null"));
     }
 
     private boolean isLHSofDotValid(List<Tuple<String, String, Integer>> tempList, int index) {
         return (tempList.get(index).lexi.equals("this") || tempList.get(index).type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name()) || tempList.get(index).type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name()) || tempList.get(index).type.equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_END.name()));
     }
 
     private boolean isValidObjectTypeExpression(Tuple<String, String, Integer> lexi) {
         return (lexi.lexi.equals("char") || lexi.lexi.equals("int") || lexi.lexi.equals("bool") || (lexi.type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name()) && Character.isUpperCase(lexi.lexi.toCharArray()[0])));
     }
 
     private boolean isStepValid(String type, String name) {
         return ((type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name()) && Character.isLowerCase(name.toCharArray()[0])) || type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name()));
     }
 
     private void validateComma(Tuple<String, String, Integer> currentLex, Tuple<String, String, Integer> previousLex, Tuple<String, String, Integer> nextLexi) {
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
 
     private boolean isValidParameterDeclarationType(List<Tuple<String, String, Integer>> tempList, int index) {
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
                 scope += "." + tempList.get(2).lexi;
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
 
     private boolean isValidCalledParameterType(List<Tuple<String, String, Integer>> tempList, int index) {
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
 
     private boolean isValidRelationParameterType(List<Tuple<String, String, Integer>> tempList, int index) {
         if ((tempList.get(index).type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name()) && Character.isLowerCase(tempList.get(index).lexi.toCharArray()[0])) || tempList.get(index).type.equals(LexicalAnalyzer.tokenTypesEnum.NUMBER.name()) || tempList.get(index).type.equals(LexicalAnalyzer.tokenTypesEnum.CHARACTER.name()) || tempList.get(index).type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name()) || tempList.get(index).lexi.equals("true") || tempList.get(index).lexi.equals("false")) {
             if (tempList.get(index).lexi.equals("(")) {
                 index++;
             }
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
             } else return tempList.get(index + 1).type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name());
         }
         return false;
     }
 
     private boolean isValidCalledParameterTypeITOA(List<Tuple<String, String, Integer>> tempList, int index) {
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
 
     private boolean isValidCalledParameterTypeITOALast(List<Tuple<String, String, Integer>> tempList, int index) {
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
 
     private void validateIOOpr(Tuple<String, String, Integer> currentLex, Tuple<String, String, Integer> previousLex, Tuple<String, String, Integer> nextLex, Tuple<String, String, Integer> peekPrevious) {
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
 
     private boolean canAddToList(Tuple<String, String, Integer> temp) {
         return !(temp.type.equals(LexicalAnalyzer.tokenTypesEnum.EOT.name()) || temp.type.equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_END.name()) || temp.type.equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_BEGIN.name()));
     }
 
     private void validateBooleanOpr(Tuple<String, String, Integer> currentLex, Tuple<String, String, Integer> previousLex, Tuple<String, String, Integer> nextLex) {
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
 
     private void validateReturnStatement(Tuple<String, String, Integer> currentLex, Tuple<String, String, Integer> nextLex) {
         if (!isReturnValueValid(nextLex)) {
             errorList += "Return statement must either be followed by a value or an end of line token (;). Line: " + currentLex.lineNum + "\n";
         }
     }
 
     private boolean isReturnValueValid(Tuple<String, String, Integer> nextLex) {
         if (nextLex == null)
             return false;
         else if (nextLex.type.equals(LexicalAnalyzer.tokenTypesEnum.NUMBER.name()) || nextLex.type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name()) || nextLex.type.equals(LexicalAnalyzer.tokenTypesEnum.CHARACTER.name()) || nextLex.type.equals(LexicalAnalyzer.tokenTypesEnum.EOT.name()) || nextLex.type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name()) || nextLex.lexi.equals("true") || nextLex.lexi.equals("false"))
             return true;
         return false;
     }
 
     private void validateRelationalOpr(Tuple<String, String, Integer> currentLex, Tuple<String, String, Integer> previousLex, Tuple<String, String, Integer> nextLex) {
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
 
     private void validateMathOpr(Tuple<String, String, Integer> currentLex, Tuple<String, String, Integer> previousLex, Tuple<String, String, Integer> nextLex) {
        if (nextLex == null || nextLex.type.equals(LexicalAnalyzer.tokenTypesEnum.EOT.name()) || (previousLex == null)) {
             errorList += "Mathematical operators require a right hand value. Line: " + currentLex.lineNum + "\n";
             return;
         }
 
        if ((previousLex.type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name()) || previousLex.type.equals(LexicalAnalyzer.tokenTypesEnum.NUMBER.name()) || previousLex.lexi.equals(")")) && (nextLex.type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name()) || nextLex.type.equals(LexicalAnalyzer.tokenTypesEnum.NUMBER.name()) || nextLex.lexi.equals("("))) {
             return;
         }
         errorList += "Both side of mathematical operation must be either an Identifier or a Number. Line: " + previousLex.lineNum + "\n";
     }
 
     private void validateAssignmentOpr(Tuple<String, String, Integer> currentLex, Tuple<String, String, Integer> previousLex, Tuple<String, String, Integer> nextLex) throws IllegalArgumentException {
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
 
     private boolean isRHSinValidFormatAssignment(Tuple<String, String, Integer> nextLex) {
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
 
     private boolean isLHSinValidFormatRelationShip(Tuple<String, String, Integer> nextLex) {
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
 
     private boolean isPreviousLexiValidAssignment(Tuple<String, String, Integer> previousLex) {
         if (previousLex.type.equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name()))
             return true;
         else if (previousLex.type.equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name()))
             return true;
         else if (previousLex.type.equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_END.name()))
             return true;
 
         return false;
     }
 
     private boolean isLHSinValidFormat(Tuple<String, String, Integer> peekPrevious) {
         return (peekPrevious == null || peekPrevious.type.equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_END.name()) || peekPrevious.type.equals(LexicalAnalyzer.tokenTypesEnum.EOT.name()));
     }
 
     private void addToSymbolTable(String type, List<String> params, String returnType, String accessMod, String value, int lineNum) {
         for (String key : symbolTable.keySet()) {
             Symbol s = symbolTable.get(key);
             if (s.getScope().equals(scope) && s.getValue().equals(value) && s.getKind().equals(type)) {
                 errorList += "Found duplicate declaration of '" + value + "'. Line: " + lineNum + "\n";
                 return;
             }
         }
 
         if (type.equals("Class")) {
             symbolTable.put("C" + symIdInr++, new Symbol(scope, "C" + symIdInr, value, type, new ClassData()));
             scope += "." + value;
         } else if (type.equals("Function")) {
             symbolTable.put("F" + symIdInr++, new Symbol(scope, "F" + symIdInr, value, type, new FunctionData(accessMod, params, returnType)));
             scope += "." + value;
         } else if (type.equals("pvar")) {
             symbolTable.put("P" + symIdInr++, new Symbol(scope, "P" + symIdInr, value, type, new VaribleData(returnType, accessMod)));
         } else {
             symbolTable.put("V" + symIdInr++, new Symbol(scope, "V" + symIdInr, value, type, new VaribleData(returnType, accessMod)));
         }
     }
 }
