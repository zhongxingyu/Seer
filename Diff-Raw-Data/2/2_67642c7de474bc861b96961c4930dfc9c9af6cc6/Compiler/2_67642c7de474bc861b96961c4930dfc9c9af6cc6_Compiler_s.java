 package project;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Nathanael
  * Date: 4/27/13
  * Time: 8:40 AM
  */
 public class Compiler {
     private static final String ILLEGAL_EXPRESSION = "Illegal expression.";
     private static final String ILLEGAL_NEW_DECLARATION = "Illegal new_declaration";
     private static final String ILLEGAL_NEW_OPERATION = "Illegal new operation.";
     private static final String ILLEGAL_ATOI_OPERATION = "Illegal atoi operation.";
     private static final String ILLEGAL_ITOA_OPERATION = "Illegal itoa operation.";
 
     private static final String INVALID_ARGUMENT_LIST = "Invalid argument list.";
     private static final String INVALID_FUNCTION = "Invalid function.";
     private static final String INVALID_TYPE = "Invalid type.";
     private static final String INVALID_STATEMENT = "Invalid statement.";
     private static final String MISSING_ARRAY_CLOSE = "Array element missing array close.";
 
     private static final String MISSING_CLOSING_PARENTHESIS = "Missing closing parenthesis.";
     private static final String MISSING_OPENING_PARENTHESIS = "Missing opening parenthesis.";
     private static final String LINE = " Line: ";
 
     private static final String OPERATION = " operation.";
     private static final String ARGUMENT_LIST = " argument_list.";
     private static final String EXPRESSION = " expression.";
 
     private LexicalAnalyzer lexicalAnalyzer;
     private String errorList = "";
 
     public Compiler(LexicalAnalyzer lexicalAnalyzer) {
         this.lexicalAnalyzer = lexicalAnalyzer;
     }
 
     public LexicalAnalyzer getLexicalAnalyzer() {
         return lexicalAnalyzer;
     }
 
     public String getErrorList() {
         return errorList;
     }
 
     public void evaluate() {
         // pass one
         if (!compiliation_unit()) {
             System.out.print(errorList);
             System.exit(0);
         }
 
         lexicalAnalyzer.resetList();
 
         System.out.print("Success!");
     }
 
     private boolean new_declaration() {
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
 
             // check format: "(" [argument_list] ")"
             lexicalAnalyzer.nextToken();
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 errorList += ILLEGAL_NEW_DECLARATION + OPERATION + " " + MISSING_CLOSING_PARENTHESIS + LINE + lexicalAnalyzer.peekPreviousToken().getLineNum() + "\n";
                 return false;
             }
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                 lexicalAnalyzer.nextToken();
                 return true;
             }
 
             String errorCheck = errorList;
 
             if (!argument_list()) {
                 if (!errorCheck.equals(errorList)) {
                     errorList += ILLEGAL_NEW_DECLARATION + ARGUMENT_LIST + LINE + lexicalAnalyzer.peekPreviousToken().getLineNum() + "\n";
                     return false;
                 }
             }
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                 errorList += ILLEGAL_NEW_DECLARATION + OPERATION + " " + MISSING_CLOSING_PARENTHESIS + LINE + lexicalAnalyzer.peekPreviousToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             return true;
         }
 
         if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_BEGIN.name())) {
 
             // check format: "[" expression "]"
             lexicalAnalyzer.nextToken();
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 errorList += ILLEGAL_NEW_DECLARATION + OPERATION + LINE + lexicalAnalyzer.peekPreviousToken().getLineNum() + "\n";
                 return false;
             }
 
             if (!expression()) {
                 errorList += ILLEGAL_NEW_DECLARATION + EXPRESSION + LINE + lexicalAnalyzer.peekPreviousToken().getLineNum() + "\n";
                 return false;
             }
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_END.name())) {
                 errorList += ILLEGAL_NEW_DECLARATION + OPERATION + ", " + MISSING_CLOSING_PARENTHESIS + LINE + lexicalAnalyzer.peekPreviousToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             return true;
         }
 
         return false;
     }
 
     public boolean assignment_expression() {
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         if (lexicalAnalyzer.getToken().getType().equals(KeyConst.NEW.getKey())) {
 
             // check format: "new" type new_declaration
             lexicalAnalyzer.nextToken();
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !type(lexicalAnalyzer.getToken().getType())) {
                 errorList += ILLEGAL_NEW_OPERATION + " " + INVALID_TYPE + LINE + lexicalAnalyzer.peekPreviousToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !new_declaration()) {
                 errorList += ILLEGAL_NEW_OPERATION + LINE + lexicalAnalyzer.peekPreviousToken().getLineNum() + "\n";
                 return false;
             }
 
             return true;
         } else if (lexicalAnalyzer.getToken().getType().equals(KeyConst.ATOI.getKey())) {
 
             // check format: "atoi" "(" expression ")"
             lexicalAnalyzer.nextToken();
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 errorList += ILLEGAL_ATOI_OPERATION + " " + MISSING_OPENING_PARENTHESIS + LINE + lexicalAnalyzer.peekPreviousToken().getLineNum() + "\n";
                 return false;
             }
 
             if (!lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
                 errorList += ILLEGAL_ATOI_OPERATION + " " + MISSING_OPENING_PARENTHESIS + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 errorList += ILLEGAL_ATOI_OPERATION + " " + MISSING_CLOSING_PARENTHESIS + LINE + lexicalAnalyzer.peekPreviousToken().getLineNum() + "\n";
                 return false;
             }
 
             if (!expression()) {
                 errorList += ILLEGAL_ATOI_OPERATION + " Invalid Expression." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 errorList += ILLEGAL_ATOI_OPERATION + " atoi can only contain one parameter. " + MISSING_CLOSING_PARENTHESIS + LINE + lexicalAnalyzer.peekPreviousToken().getLineNum() + "\n";
                 return false;
             }
 
             if (!lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                 errorList += ILLEGAL_ATOI_OPERATION + " atoi can only contain one parameter. " + MISSING_CLOSING_PARENTHESIS + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             return true;
         } else if (lexicalAnalyzer.getToken().getType().equals(KeyConst.ITOA.getKey())) {
 
             // check format: "itoa" "(" expression ")"
             lexicalAnalyzer.nextToken();
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 errorList += ILLEGAL_ITOA_OPERATION + " " + MISSING_OPENING_PARENTHESIS + LINE + lexicalAnalyzer.peekPreviousToken().getLineNum() + "\n";
                 return false;
             }
 
             if (!lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
                 errorList += ILLEGAL_ITOA_OPERATION + " " + MISSING_OPENING_PARENTHESIS + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 errorList += ILLEGAL_ITOA_OPERATION + " " + MISSING_CLOSING_PARENTHESIS + LINE + lexicalAnalyzer.peekPreviousToken().getLineNum() + "\n";
                 return false;
             }
 
             if (!expression()) {
                 errorList += ILLEGAL_ITOA_OPERATION + " Invalid Expression." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 errorList += ILLEGAL_ITOA_OPERATION + " itoa can only contain one parameter. " + MISSING_CLOSING_PARENTHESIS + LINE + lexicalAnalyzer.peekPreviousToken().getLineNum() + "\n";
                 return false;
             }
 
             if (!lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                 errorList += ILLEGAL_ITOA_OPERATION + " itoa can only contain one parameter. " + MISSING_CLOSING_PARENTHESIS + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             return true;
         } else if (lexicalAnalyzer.getToken().getType().equals(KeyConst.THIS.getKey())) {
             lexicalAnalyzer.nextToken();
             return true;
         } else {
             if (!expression()) {
                 errorList += "Invalid assignment expression." + LINE + lexicalAnalyzer.peekPreviousToken().getLineNum() + "\n";
                 return false;
             }
             return true;
         }
     }
 
     public boolean expressionz() {
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         Tuple token = lexicalAnalyzer.getToken();
         if (!(token.getType().equals(LexicalAnalyzer.tokenTypesEnum.ASSIGNMENT_OPR.name()) || isLogicalConnectiveExpression(token.getType()) || isBooleanExpression(token.getType()) || isMathematicalExpression(token.getType()))) {
             errorList += "Invalid expressionz." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         lexicalAnalyzer.nextToken();
         if (lexicalAnalyzer.getToken() instanceof NullTuple || isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             errorList += "expressionz missing right hand expression." + LINE + lexicalAnalyzer.peekPreviousToken().getLineNum() + "\n";
             lexicalAnalyzer.previousToken();
             return false;
         }
 
         if (token.getType().equals(LexicalAnalyzer.tokenTypesEnum.ASSIGNMENT_OPR.name())) {
             if (!assignment_expression()) {
                 errorList += "Invalid assignment expression." + LINE + lexicalAnalyzer.peekPreviousToken().getLineNum() + "\n";
                 return false;
             }
         } else {
             if (!expression()) {
                 errorList += "Invalid expressionz expression." + LINE + lexicalAnalyzer.peekPreviousToken().getLineNum() + "\n";
                 return false;
             }
         }
 
         return true;
     }
 
     public boolean expression() {
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
 
             // check format: "(" expression ")" [expressionz]
             lexicalAnalyzer.nextToken();
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !expression()) {
                 errorList += ILLEGAL_EXPRESSION + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                 errorList += ILLEGAL_EXPRESSION + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 return true;
             }
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ASSIGNMENT_OPR.name()) || isLogicalConnectiveExpression(lexicalAnalyzer.getToken().getType()) || isBooleanExpression(lexicalAnalyzer.getToken().getType()) || isMathematicalExpression(lexicalAnalyzer.getToken().getType())) {
                 return expressionz();
             }
 
             return true;
 
         } else if (lexicalAnalyzer.getToken().getType().equals(KeyConst.TRUE.getKey()) || lexicalAnalyzer.getToken().getType().equals(KeyConst.FALSE.getKey()) || lexicalAnalyzer.getToken().getType().equals(KeyConst.NULL.getKey()) || lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.NUMBER.name()) || lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.CHARACTER.name())) {
 
             // check format: "value" [expressionz]
             lexicalAnalyzer.nextToken();
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 return true;
             }
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ASSIGNMENT_OPR.name()) || isLogicalConnectiveExpression(lexicalAnalyzer.getToken().getType()) || isBooleanExpression(lexicalAnalyzer.getToken().getType()) || isMathematicalExpression(lexicalAnalyzer.getToken().getType())) {
                 return expressionz();
             }
 
             return true;
 
         } else if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name())) {
 
             // check format: identifier [ fn_arr_member ] [ member_refz ] [ expressionz ]
             lexicalAnalyzer.nextToken();
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 return true;
             }
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             String errCheck = errorList;
 
             fn_arr_member();
             if (!errCheck.equals(errorList)) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 return true;
             }
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             member_refz();
             if (!errCheck.equals(errorList)) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 return true;
             }
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ASSIGNMENT_OPR.name()) || isLogicalConnectiveExpression(lexicalAnalyzer.getToken().getType()) || isBooleanExpression(lexicalAnalyzer.getToken().getType()) || isMathematicalExpression(lexicalAnalyzer.getToken().getType())) {
                 return expressionz();
             }
 
             return true;
         } else if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.UNKNOWN.name())) {
             return !isUnknownSymbol(lexicalAnalyzer.getToken().getType());
         }
         return false;
     }
 
     private boolean argument_list() {
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         // check format: expression { "," expression}
         if (!expression()) {
             errorList += INVALID_ARGUMENT_LIST + LINE + lexicalAnalyzer.peekPreviousToken().getLineNum() + "\n";
             return false;
         }
 
         while (!(lexicalAnalyzer.getToken() instanceof NullTuple) && lexicalAnalyzer.getToken().getLexi().equals(",")) {
             lexicalAnalyzer.nextToken();
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (!expression()) {
                 errorList += INVALID_ARGUMENT_LIST + LINE + lexicalAnalyzer.peekPreviousToken().getLineNum() + "\n";
                 return false;
             }
         }
         return true;
     }
 
     private boolean fn_arr_member() {
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         // check format: "(" [ argument_list ] ")" | "[" expression "]"
         if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
 
             //check format: "(" [ argument_list ] ")"
             lexicalAnalyzer.nextToken();
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 errorList += INVALID_FUNCTION + " " + MISSING_CLOSING_PARENTHESIS + LINE + lexicalAnalyzer.peekPreviousToken().getLineNum() + "\n";
                 return false;
             }
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                 return true;
             }
 
             if (!argument_list()) {
                 errorList += INVALID_ARGUMENT_LIST + " in function parameter." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                 errorList += INVALID_FUNCTION + LINE + lexicalAnalyzer.peekPreviousToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             return true;
 
         } else if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_BEGIN.name())) {
 
             //check format: "[" expression "]"
             lexicalAnalyzer.nextToken();
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !expression()) {
                 errorList += "Invalid array expression." + LINE + lexicalAnalyzer.peekPreviousToken().getLineNum() + "\n";
                 return false;
             }
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_END.name())) {
                 errorList += MISSING_ARRAY_CLOSE + LINE + lexicalAnalyzer.peekPreviousToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             return true;
         }
         return false;
     }
 
     private boolean member_refz() {
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         // check format: "." identifier [ fn_arr_member ] [ member_refz ]
         if (!lexicalAnalyzer.getToken().getLexi().equals(".")) {
             return false;
         }
 
         lexicalAnalyzer.nextToken();
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name())) {
             errorList += "Invalid member ref." + LINE + lexicalAnalyzer.peekPreviousToken().getLineNum() + "\n";
             return false;
         }
 
         lexicalAnalyzer.nextToken();
         if (lexicalAnalyzer.getToken() instanceof NullTuple) {
             return true;
         }
 
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         String errCheck = errorList;
 
         fn_arr_member();
         if (!errCheck.equals(errorList)) {
             return false;
         }
 
         if (lexicalAnalyzer.getToken() instanceof NullTuple) {
             return true;
         }
 
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         member_refz();
         return errCheck.equals(errorList);
     }
 
     public boolean statement() {
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_BEGIN.name())) {
 
             // check format: "{" {statement} "}"
             String errorCheck = errorList;
 
             lexicalAnalyzer.nextToken();
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             while (statement()) {
                 lexicalAnalyzer.nextToken();
                 if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                     return false;
                 }
             }
 
             if (!errorCheck.equals(errorList)) {
                 return false;
             }
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_END.name())) {
                 errorList += INVALID_STATEMENT + " Missing a closing block." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             return true;
 
         } else if (lexicalAnalyzer.getToken().getLexi().equals(KeyConst.IF.getKey())) {
 
             // check format: "if" "(" expression ")" statement [ "else" statement ]
             lexicalAnalyzer.nextToken();
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
                 errorList += INVALID_STATEMENT + " " + MISSING_OPENING_PARENTHESIS + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !expression()) {
                 errorList += "'if' statement requires a valid expression." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                 errorList += INVALID_STATEMENT + " " + MISSING_CLOSING_PARENTHESIS + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (!statement()) {
                 errorList += INVALID_STATEMENT + " 'if' statement." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             if (!(lexicalAnalyzer.peek() instanceof NullTuple) && lexicalAnalyzer.peek().getLexi().equals(KeyConst.ELSE.getKey())) {
 
                 lexicalAnalyzer.nextToken();
                 lexicalAnalyzer.nextToken();
 
                 if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                     return false;
                 }
 
                 if (lexicalAnalyzer.getToken() instanceof NullTuple || !statement()) {
                     errorList += INVALID_STATEMENT + " a valid statement is required after an else statement." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                     return false;
                 }
             }
 
             return true;
 
         } else if (lexicalAnalyzer.getToken().getLexi().equals(KeyConst.WHILE.getKey())) {
 
             // check format: "while" "(" expression ")" statement
             lexicalAnalyzer.nextToken();
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
                 errorList += INVALID_STATEMENT + " " + MISSING_OPENING_PARENTHESIS + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !expression()) {
                 errorList += "'while' statement requires a valid expression." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                 errorList += INVALID_STATEMENT + " " + MISSING_CLOSING_PARENTHESIS + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (!statement()) {
                 errorList += INVALID_STATEMENT + " 'while' statement." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             return true;
 
         } else if (lexicalAnalyzer.getToken().getLexi().equals(KeyConst.RETURN.getKey())) {
 
             // check format: "return" [ expression ] ";"
             lexicalAnalyzer.nextToken();
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 errorList += INVALID_STATEMENT + " 'return' statement must end with a ';'." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.EOT.name())) {
                 return true;
             }
 
             if (!expression()) {
                 errorList += "Invalid 'return' statement expression." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.EOT.name())) {
                 errorList += INVALID_STATEMENT + " 'return' statement must end with a ';'." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             return true;
 
         } else if (lexicalAnalyzer.getToken().getLexi().equals(KeyConst.COUT.getKey())) {
 
             // check format: "cout" "<<" expression ";"
             lexicalAnalyzer.nextToken();
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getLexi().equals("<<")) {
                 errorList += INVALID_STATEMENT + "'cout' statement missing extraction operator." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !expression()) {
                 errorList += "'cout' statement requires a valid expression." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.EOT.name())) {
                 errorList += INVALID_STATEMENT + " 'cout' statement must end with a ';'." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             return true;
 
         } else if (lexicalAnalyzer.getToken().getLexi().equals(KeyConst.CIN.getKey())) {
 
             // check format: "cin" ">>" expression ";"
             lexicalAnalyzer.nextToken();
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getLexi().equals(">>")) {
                 errorList += INVALID_STATEMENT + "'cin' statement missing extraction operator." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !expression()) {
                 errorList += "'cin' statement requires a valid expression." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.EOT.name())) {
                 errorList += INVALID_STATEMENT + " 'cin' statement must end with a ';'." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             return true;
 
         } else {
 
             // check format: expression ";"
             if (!expression()) {
                 return false;
             }
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.EOT.name())) {
                 errorList += INVALID_STATEMENT + " 'expression' statement must end with a ';'." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             return true;
         }
     }
 
     public boolean parameter() {
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         // check format: type identifier ["[" "]"]
 
         if (lexicalAnalyzer.getToken() instanceof NullTuple || !type(lexicalAnalyzer.getToken().getType())) {
             errorList += "Parameter declarations must start with a valid type." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         lexicalAnalyzer.nextToken();
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name())) {
             errorList += "Parameter declarations require a valid identifier." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         lexicalAnalyzer.nextToken();
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_BEGIN.name())) {
             return true;
         }
 
         lexicalAnalyzer.getToken();
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_END.name())) {
             errorList += "Invalid parameter declaration. Missing closing array bracket." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         return true;
     }
 
     public boolean parameter_list() {
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         // check format: parameter { "," parameter}
         if (!parameter()) {
             errorList += "Invalid parameter_list" + LINE + lexicalAnalyzer.peekPreviousToken().getLineNum() + "\n";
             return false;
         }
 
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         while (!(lexicalAnalyzer.getToken() instanceof NullTuple) && lexicalAnalyzer.getToken().getLexi().equals(",")) {
             lexicalAnalyzer.nextToken();
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (!parameter()) {
                 errorList += "Invalid parameter_list" + LINE + lexicalAnalyzer.peekPreviousToken().getLineNum() + "\n";
                 return false;
             }
         }
         return true;
     }
 
     public boolean variable_declaration() {
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         // check format: type identifier ["[" "]"] ["=" assignment_expression ] ";"
         if (lexicalAnalyzer.getToken() instanceof NullTuple || !type(lexicalAnalyzer.getToken().getType())) {
             errorList += "Variable declarations must start with a valid type." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         lexicalAnalyzer.nextToken();
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name())) {
             errorList += "Variable declarations require a valid identifier." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         lexicalAnalyzer.nextToken();
         if (lexicalAnalyzer.getToken() instanceof NullTuple) {
             errorList += "Invalid variable declaration. Missing semi-colon at end." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_BEGIN.name())) {
 
             lexicalAnalyzer.getToken();
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_END.name())) {
                 errorList += "Invalid parameter declaration. Missing closing array bracket." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 errorList += "Invalid variable declaration. Missing semi-colon at end." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
         }
 
         if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ASSIGNMENT_OPR.name())) {
 
             lexicalAnalyzer.nextToken();
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !assignment_expression()) {
                 errorList += "Invalid variable declaration. Invalid assignment expression." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 errorList += "Invalid variable declaration. Missing semi-colon at end." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
         }
 
         if (!lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.EOT.name())) {
             errorList += "Invalid variable declaration. Missing semi-colon at end." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         lexicalAnalyzer.nextToken();
         return true;
     }
 
     public boolean method_body() {
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         // check format: "{" {variable_declaration} {statement} "}"
         if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_BEGIN.name())) {
             errorList += "Method body must begin with an open block '{'." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         lexicalAnalyzer.nextToken();
         if (lexicalAnalyzer.getToken() instanceof NullTuple) {
             errorList += "Method body must end with a closing block '}'." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_END.name())) {
             lexicalAnalyzer.nextToken();
             return true;
         }
 
         String errorCheck = errorList;
 
         if (type(lexicalAnalyzer.getToken().getType())) {
             while (variable_declaration()) {
                 if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                     errorList += "Method body must end with a closing block '}'." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                     return false;
                 }
 
                 if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                     return false;
                 }
 
                 if (!type(lexicalAnalyzer.getToken().getType())) break;
             }
 
             if (!errorCheck.equals(errorList)) {
                 errorList += "Invalid variable_declaration in method body." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
         }
 
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_END.name())) {
             lexicalAnalyzer.nextToken();
             return true;
         }
 
         while (statement()) {
             lexicalAnalyzer.nextToken();
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 errorList += "Method body must end with a closing block '}'." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
         }
 
         if (!errorCheck.equals(errorList)) {
             errorList += "Invalid statement in method body." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_END.name())) {
             errorList += "Method body must end with a closing block '}'." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         lexicalAnalyzer.nextToken();
         return true;
     }
 
     public boolean constructor_declaration() {
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         // check format: class_name "(" [parameter_list] ")" method_body
         if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(KeyConst.CLASS_NAME.getKey())) {
             errorList += "Invalid class name in constructor declaration." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         String className = lexicalAnalyzer.getToken().getLexi();
 
         lexicalAnalyzer.nextToken();
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
             errorList += MISSING_OPENING_PARENTHESIS + " for constructor declaration. 'class " + className + "'" + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         lexicalAnalyzer.nextToken();
         if (lexicalAnalyzer.getToken() instanceof NullTuple) {
             errorList += MISSING_CLOSING_PARENTHESIS + " for constructor declaration. 'class " + className + "'\n";
             return false;
         }
 
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         if (!lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
             if (!parameter_list()) {
                 errorList += "Invalid parameter list for constructor declaration. 'class " + className + "'" + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
         }
 
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
             errorList += MISSING_CLOSING_PARENTHESIS + " for constructor declaration. 'class " + className + "'" + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         lexicalAnalyzer.nextToken();
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         return method_body();
     }
 
     public boolean field_declaration() {
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
             // check format: "(" [parameter_list] ")" method_body
 
             lexicalAnalyzer.nextToken();
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 errorList += "Invalid field declaration. NullTuple exception. " + MISSING_CLOSING_PARENTHESIS + "\n";
                 return false;
             }
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                 lexicalAnalyzer.nextToken();
                 return method_body();
             }
 
             if (!parameter_list()) {
                 errorList += "Invalid parameter list in field declaration." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                 errorList += "Invalid field declaration." + MISSING_CLOSING_PARENTHESIS + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             return method_body();
 
         } else {
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             // check format: ["[" "]"] ["=" assignment_expression ] ";"
             if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_BEGIN.name())) {
 
                lexicalAnalyzer.getToken();
                 if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                     return false;
                 }
 
                 if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_END.name())) {
                     errorList += "Invalid field declaration. Missing closing array bracket." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                     return false;
                 }
 
                 lexicalAnalyzer.nextToken();
                 if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                     errorList += "Invalid field declaration. Missing semi-colon at end." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                     return false;
                 }
 
                 if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                     return false;
                 }
             }
 
             if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ASSIGNMENT_OPR.name())) {
 
                 lexicalAnalyzer.nextToken();
                 if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                     return false;
                 }
 
                 if (lexicalAnalyzer.getToken() instanceof NullTuple || !assignment_expression()) {
                     errorList += "Invalid field declaration. Invalid assignment expression." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                     return false;
                 }
 
                 if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                     errorList += "Invalid field declaration. Missing semi-colon at end." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                     return false;
                 }
 
                 if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                     return false;
                 }
             }
 
             if (!lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.EOT.name())) {
                 errorList += "Invalid field declaration. Missing semi-colon at end." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             return true;
         }
     }
 
     public boolean class_member_declaration() {
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         if (lexicalAnalyzer.getToken().getType().equals(KeyConst.MODIFIER.getKey())) {
             // check format: modifier type identifier field_declaration
 
             lexicalAnalyzer.nextToken();
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !type(lexicalAnalyzer.getToken().getType())) {
                 errorList += "Invalid class member declaration. Missing a valid type." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name())) {
                 errorList += "Invalid class member declaration. Missing a valid identifier." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !field_declaration()) {
                 errorList += "Invalid class member declaration. Invalid field declaration." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             return true;
 
         } else if (lexicalAnalyzer.getToken().getType().equals(KeyConst.CLASS_NAME.getKey())) {
             // check format: constructor_declaration
             if (!constructor_declaration()) {
                 errorList += "Invalid class member declaration. Invalid constructor declaration." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             return true;
         }
 
         return false;
     }
 
     public boolean class_declaration() {
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         // check format: "class" class_name "{" {class_member_declaration} "}"
         if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(KeyConst.CLASS.getKey())) {
             errorList += "Invalid class declaration. Missing 'class' tag." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         lexicalAnalyzer.nextToken();
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(KeyConst.CLASS_NAME.getKey())) {
             errorList += "Invalid class declaration. Missing a valid class name." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         lexicalAnalyzer.nextToken();
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_BEGIN.name())) {
             errorList += "Invalid class declaration. Missing opening block." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         lexicalAnalyzer.nextToken();
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         while (class_member_declaration()) {
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 errorList += "Method body must end with a closing block '}'." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
         }
 
         if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_END.name())) {
             errorList += "Invalid class declaration. Missing closing block." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         lexicalAnalyzer.nextToken();
         return true;
     }
 
     public boolean compiliation_unit() {
         // check format: {class_declaration} "void" "main" "(" ")" method_body
         if (lexicalAnalyzer.getToken() instanceof NullTuple) {
             errorList += "Invalid compilation unit. Missing 'main' method.\n";
             return false;
         }
 
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         if (!lexicalAnalyzer.getToken().getLexi().equals(KeyConst.VOID.getKey())) {
             if (!class_declaration()) {
                 errorList += "Invalid compilation unit. Invalid class declaration." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 errorList += "Invalid compilation unit. Missing 'main' method.\n";
                 return false;
             }
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             while (lexicalAnalyzer.getToken().getLexi().equals(KeyConst.CLASS.getKey())) {
                 if (!class_declaration()) {
                     errorList += "Invalid compilation unit. Invalid class declaration." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                     return false;
                 }
 
                 if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                     return false;
                 }
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getLexi().equals(KeyConst.VOID.getKey())) {
                 errorList += "Invalid compilation unit. Missing 'main' method.\n";
                 return false;
             }
         }
 
         lexicalAnalyzer.nextToken();
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getLexi().equals(KeyConst.MAIN.getKey())) {
             errorList += "Invalid compilation unit. Invalid name for method 'main'." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         lexicalAnalyzer.nextToken();
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
             errorList += "Invalid compilation unit. Invalid 'main' method. " + MISSING_OPENING_PARENTHESIS + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         lexicalAnalyzer.nextToken();
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
             errorList += "Invalid compilation unit. Invalid 'main' method. " + MISSING_CLOSING_PARENTHESIS + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         lexicalAnalyzer.nextToken();
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         // at this point we have declared classes and "void main()"
         return method_body();
     }
 
     private boolean type(String itemType) {
         return (itemType.equals(KeyConst.INT.getKey()) || itemType.equals(KeyConst.CHAR.getKey()) || itemType.equals(KeyConst.BOOL.getKey()) || itemType.equals(KeyConst.VOID.getKey()) || itemType.equals(KeyConst.CLASS_NAME.getKey()));
     }
 
     private boolean isLogicalConnectiveExpression(String itemType) {
         return itemType.equals(LexicalAnalyzer.tokenTypesEnum.LOGICAL_OPR.name());
     }
 
     private boolean isBooleanExpression(String itemType) {
         return itemType.equals(LexicalAnalyzer.tokenTypesEnum.BOOLEAN_OPR.name());
     }
 
     private boolean isMathematicalExpression(String itemType) {
         return itemType.equals(LexicalAnalyzer.tokenTypesEnum.MATH_OPR.name());
     }
 
     private boolean isUnknownSymbol(String type) {
         if (type.equals(LexicalAnalyzer.tokenTypesEnum.UNKNOWN.name())) {
             errorList += "Unknown Symbol on" + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return true;
         }
 
         return false;
     }
 }
