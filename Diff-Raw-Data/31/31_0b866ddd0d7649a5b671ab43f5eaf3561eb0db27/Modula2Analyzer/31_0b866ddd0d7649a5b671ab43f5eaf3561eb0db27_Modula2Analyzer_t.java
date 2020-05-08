 package ru.jcorp.m2parse.analyze;
 
 import org.apache.commons.lang3.StringUtils;
 import ru.jcorp.m2parse.exceptions.*;
 
 import java.util.Arrays;
 import java.util.HashSet;
 
 /**
  * Modula-2 Syntax analyzer
  *
  * @author artamonov
  */
 public class Modula2Analyzer {
 
     public static final String[] EXPECTED_ENDS_ASSIGMENT = new String[]{":="};
     public static final String[] EXPECTED_ENDS_TO = new String[]{" TO"};
     public static final String[] EXPECTED_ENDS_BY_DO = new String[]{" BY", " DO"};
     public static final String[] EXPECTED_ENDS_DO = new String[]{" DO"};
     public static final String[] EXPECTED_ENDS_IDENTITY = new String[]{"[", " ", ":"};
     public static final String[] EXPECTED_SPECIAL_ENDS_IDENTITY = new String[]{"[", " ", ":", "+", "-", "*", " MOD ", " DIV "};
     public static final String[] EXPECTED_ENDS_OPERAND = new String[]
             {" ", "\t", "\n", "\r", "+", "-", "*", " MOD ", " DIV ", ",", "]"};
 
     public static final String[] PATTERN_FULL_OPERATIONS = new String[]{" +", " -", " *", " MOD ", " DIV "};
     public static final String[] PATTERN_ELEMENTARY_OPERATIONS = new String[]{"+", "-", "*"};
     public static final String[] PATTERN_SKIP_OPERATIONS = new String[]{"+", "-", "*", "MOD ", "DIV "};
 
    public static final int MAX_CONST_VALUE = 32768;
    public static final int MAX_IDENTITY_NAME_LENGTH = 8;

     private enum CycleState {
         START, PARAMETER, ASSIGNMENT, VALUE, TO, BY, DO, END
     }
 
     private enum ParameterState {
         START, IDENTITY, INDEX_LIST, END
     }
 
     private enum IndexListState {
         START, INDEX, END
     }
 
     private enum IndexState {
         START, OPERAND_1, OPERATION, END
     }
 
     private enum Operand1State {
         START, IDENTITY, CONST, END
     }
 
     private enum OperationState {
         START, END
     }
 
     private enum ValueState {
         START, OPERAND_2, OPERATION, END
     }
 
     private enum Operand2State {
         START, PARAMETER, CONST, END
     }
 
     private enum ConstState {
         START, NUMBER, END
     }
 
     private enum IdentityState {
         START, NAME, END
     }
 
     public SemanticResult analyze(String expression) throws AnalyzeException {
 
         if (StringUtils.isBlank(expression))
             throw new EmptyExpressionException();
 
         // trim end of expression
         expression = StringUtils.stripEnd(expression, " \t\n\r");
 
         expression = expression
                 .replaceAll("\n", "")
                 .replaceAll("\t", "")
                 .replaceAll("\t", " ")
                 .replaceAll("\r", "");
 
         expression = expression
                 .replaceAll(" mod$", " MOD")
                 .replaceAll(" mod ", " MOD ")
                 .replaceAll(" div ", " DIV ")
                 .replaceAll(" div$", " DIV")
                 .replaceAll("^for ", "FOR ")
                 .replaceAll(" for ", "FOR ")
                 .replaceAll(" to ", " TO ")
                 .replaceAll(" to$", " TO")
                 .replaceAll(" do ", " DO ")
                 .replaceAll(" do$", " DO")
                 .replaceAll(" by ", " BY ")
                 .replaceAll(" by$", " BY");
 
         // start syntax analyze
         AnalyzeCursor cursor = new AnalyzeCursor(expression, 0);
         return matchCycle(cursor);
     }
 
     public SemanticResult matchCycle(AnalyzeCursor cursor) throws AnalyzeException {
         CycleState state = CycleState.START;
 
         SemanticResult result = new SemanticResult();
 
         while ((cursor.inExpression()) && (state != CycleState.END)) {
             switch (state) {
 
                 case START:
                     state = cycleStart(cursor, state);
                     break;
 
                 case PARAMETER:
                     state = cycleParameter(cursor, state, result);
                     break;
 
                 case ASSIGNMENT:
                     state = cycleAssigment(cursor);
                     break;
 
                 case VALUE:
                     state = cycleValue(cursor, state, result);
                     break;
 
                 case TO:
                     state = cycleToValue(cursor, state, result);
                     break;
 
                 case BY:
                     state = cycleByValue(cursor, state, result);
                     break;
 
                 case DO:
                     state = cycleDo(cursor);
                     break;
 
                 default:
                     break;
             }
             if (state != CycleState.END)
                 cursor.incrementPosition();
         }
 
         if (state != CycleState.END)
             throw new IncompleteExpressionException(cursor);
 
         return result;
     }
 
     private CycleState cycleDo(AnalyzeCursor cursor) throws AnalyzeException {
         CycleState state;
         if (cursor.matchNext("DO"))
             state = CycleState.END;
         else
             throw new ExpectKeywordException("DO", cursor);
         return state;
     }
 
     private CycleState cycleByValue(AnalyzeCursor cursor, CycleState state, SemanticResult result) throws AnalyzeException {
         if (!cursor.isWhitespaceChar()) {
             if (cursor.skipNextPatterns(new String[]{"BY "})) {
                 SemanticResult byResult = matchValue(cursor, EXPECTED_ENDS_DO);
                 result.addResult(byResult);
                 result.setDeltaValue(byResult.getBeginValue());
                 state = CycleState.DO;
             }
         }
         return state;
     }
 
     private CycleState cycleToValue(AnalyzeCursor cursor, CycleState state, SemanticResult result) throws AnalyzeException {
         if (!cursor.isWhitespaceChar()) {
             if (cursor.skipNextPatterns(new String[]{"TO "})) {
                 SemanticResult destResult = matchValue(cursor, EXPECTED_ENDS_BY_DO);
                 result.addResult(destResult);
                 result.setEndValue(destResult.getBeginValue());
                 cursor.skipWhitespace();
                 if (cursor.matchNext("BY")) {
                     cursor.decrementPosition();
                     state = CycleState.BY;
                 } else {
                     cursor.decrementPosition();
                     state = CycleState.DO;
                 }
             }
         }
         return state;
     }
 
     private CycleState cycleValue(AnalyzeCursor cursor, CycleState state, SemanticResult result) throws AnalyzeException {
         if (!cursor.isWhitespaceChar()) {
             SemanticResult valueResult = matchValue(cursor, EXPECTED_ENDS_TO);
             result.addResult(valueResult);
             result.setBeginValue(valueResult.getBeginValue());
             cursor.decrementPosition();
             state = CycleState.TO;
         }
         return state;
     }
 
     private CycleState cycleAssigment(AnalyzeCursor cursor) throws AnalyzeException {
         CycleState state;
         if (cursor.skipNextPatterns(new String[]{":="})) {
             cursor.decrementPosition();
             state = CycleState.VALUE;
         } else
             throw new ExpectOperationException(":=", cursor);
         return state;
     }
 
     private CycleState cycleParameter(AnalyzeCursor cursor, CycleState state, SemanticResult result) throws AnalyzeException {
         if (!cursor.isWhitespaceChar()) {
             AnalyzeResult assigmentResult = matchParameter(cursor, EXPECTED_ENDS_ASSIGMENT, EXPECTED_ENDS_IDENTITY);
             result.addResult(assigmentResult);
             cursor.decrementPosition();
             state = CycleState.ASSIGNMENT;
         }
         return state;
     }
 
     private CycleState cycleStart(AnalyzeCursor cursor, CycleState state) throws AnalyzeException {
         if (!cursor.isWhitespaceChar()) {
             String forKeyword = "FOR";
             if (cursor.matchNext(forKeyword)) {
                 cursor.incrementPosition(forKeyword.length());
                 if (cursor.inExpression()) {
                     if (cursor.isWhitespaceChar()) {
                         state = CycleState.PARAMETER;
                     }
                 } else
                     throw new IncompleteExpressionException(cursor);
             } else
                 throw new ExpectKeywordException("FOR", cursor);
         }
         return state;
     }
 
     public AnalyzeResult matchParameter(AnalyzeCursor cursor, String[] expectedEnds, String[] expectedEndsIndentity) throws AnalyzeException {
         AnalyzeResult result = new AnalyzeResult();
         ParameterState state = ParameterState.START;
 
         while ((cursor.inExpression()) && (state != ParameterState.END)) {
             switch (state) {
                 case START:
                     if (!cursor.isWhitespaceChar()) {
                         cursor.decrementPosition();
                         state = ParameterState.IDENTITY;
                     }
                     break;
 
                 case IDENTITY:
                     if (!cursor.isWhitespaceChar()) {
                         AnalyzeResult identityResult = matchIdentity(cursor, expectedEndsIndentity);
                         result.addResult(identityResult);
 
                         if (cursor.getChar() == '[')
                             state = ParameterState.INDEX_LIST;
                         else {
                             boolean hasWhiteSpace = cursor.isWhitespaceChar();
                             cursor.skipWhitespace();
                             if (cursor.matchNextPatterns(expectedEnds))
                                 state = ParameterState.END;
                             else if (hasWhiteSpace) {
                                 cursor.decrementPosition();
                                 if (cursor.matchNextPatterns(expectedEnds))
                                     state = ParameterState.END;
                             }
 
                             if (state != ParameterState.END)
                                 throw new UnknownOperationException(cursor);
                         }
                     }
                     break;
 
                 case INDEX_LIST:
                     AnalyzeResult indexListResult = matchIndexList(cursor);
                     result.addResult(indexListResult);
                     boolean hasWhiteSpace = cursor.isWhitespaceChar();
                     cursor.skipWhitespace();
                     if (cursor.inExpression()) {
                         if (cursor.matchNextPatterns(expectedEnds))
                             state = ParameterState.END;
                         else if (hasWhiteSpace) {
                             cursor.decrementPosition();
                             if (cursor.matchNextPatterns(expectedEnds))
                                 state = ParameterState.END;
                         }
                     } else
                         throw new IncompleteExpressionException(cursor);
                     break;
 
                 default:
                     break;
             }
             if (state != ParameterState.END)
                 cursor.incrementPosition();
         }
 
         if (state != ParameterState.END)
             throw new IncompleteExpressionException(cursor);
 
         return result;
     }
 
     public AnalyzeResult matchIndexList(AnalyzeCursor cursor) throws AnalyzeException {
         AnalyzeResult result = new AnalyzeResult();
         IndexListState state = IndexListState.START;
 
         while ((cursor.inExpression()) && (state != IndexListState.END)) {
             switch (state) {
                 case START:
                     if (!cursor.isWhitespaceChar()) {
                         cursor.decrementPosition();
                         state = IndexListState.INDEX;
                     }
                     break;
 
                 case INDEX:
                     if (!cursor.isWhitespaceChar()) {
                         AnalyzeResult indexResult = matchIndex(cursor);
                         result.addResult(indexResult);
                         cursor.skipWhitespace();
                         if (cursor.inExpression()) {
                             if (cursor.getChar() != ',') {
                                 if (cursor.getChar() == ']') {
                                     cursor.incrementPosition();
                                     state = IndexListState.END;
                                 } else
                                     throw new ExpectBracesException(cursor);
                             }
                         } else
                             throw new IncompleteExpressionException(cursor);
                     }
                     break;
 
                 default:
                     break;
             }
             if (state != IndexListState.END)
                 cursor.incrementPosition();
         }
 
         if (state != IndexListState.END)
             throw new IncompleteExpressionException(cursor);
 
         return result;
     }
 
     public AnalyzeResult matchIndex(AnalyzeCursor cursor) throws AnalyzeException {
         AnalyzeResult result = new AnalyzeResult();
         IndexState state = IndexState.START;
 
         while ((cursor.inExpression()) && (state != IndexState.END)) {
             switch (state) {
                 case START:
                     if (!cursor.isWhitespaceChar()) {
                         state = IndexState.OPERAND_1;
                         cursor.decrementPosition();
                     }
                     break;
 
                 case OPERAND_1:
                     AnalyzeResult operand1Result = matchOperand1(cursor,
                             EXPECTED_ENDS_OPERAND);
                     result.addResult(operand1Result);
                     boolean hasWhitespaces = cursor.isWhitespaceChar();
                     cursor.skipWhitespace();
                     if (cursor.inExpression()) {
                         if (cursor.matchNextPatterns(new String[]{",", "]"}))
                             state = IndexState.END;
                         else {
                             if (hasWhitespaces) {
                                 cursor.decrementPosition();
                                 if (cursor.matchNextPatterns(PATTERN_FULL_OPERATIONS)) {
                                     state = IndexState.OPERATION;
                                 }
                             } else if (cursor.matchNextPatterns(PATTERN_ELEMENTARY_OPERATIONS)) {
                                 cursor.decrementPosition();
                                 state = IndexState.OPERATION;
                             }
                         }
                     } else
                         throw new IncompleteExpressionException(cursor);
                     break;
 
                 case OPERATION:
                     matchOperation(cursor);
                     if (cursor.isIdentifierStartChar() || cursor.isNumberStartChar()) {
                         cursor.decrementPosition();
                         state = IndexState.OPERAND_1;
                     } else
                         throw new ExpectOperandException(cursor);
                     break;
 
                 default:
                     break;
             }
             if (state != IndexState.END)
                 cursor.incrementPosition();
         }
 
         if (state != IndexState.END)
             throw new IncompleteExpressionException(cursor);
 
         return result;
     }
 
     public AnalyzeResult matchOperand1(AnalyzeCursor cursor, String[] expectedEnds) throws AnalyzeException {
         AnalyzeResult result = new AnalyzeResult();
         Operand1State state = Operand1State.START;
 
         while ((cursor.inExpression()) && (state != Operand1State.END)) {
             switch (state) {
                 case START:
                     if (!cursor.isWhitespaceChar()) {
                         if (cursor.isIdentifierStartChar()) {
                             cursor.decrementPosition();
                             state = Operand1State.IDENTITY;
                         } else if (cursor.isNumberStartChar()) {
                             cursor.decrementPosition();
                             state = Operand1State.CONST;
                         } else {
                             throw new ExpectOperandException(cursor);
                         }
                     }
                     break;
 
                 case IDENTITY:
                     if (!cursor.isWhitespaceChar()) {
                         HashSet<String> expectedEndsIdenity = new HashSet<String>(Arrays.asList(expectedEnds));
                         expectedEndsIdenity.addAll(Arrays.asList(EXPECTED_ENDS_IDENTITY));
 
                         String[] specialExpectedEnds = expectedEndsIdenity.toArray(new String[expectedEndsIdenity.size()]);
                         AnalyzeResult identityResult = matchIdentity(cursor, specialExpectedEnds);
                         result.addResult(identityResult);
                         if (cursor.matchNextPatterns(expectedEnds))
                             state = Operand1State.END;
                         else
                             throw new UnexpectedOperandException(cursor);
                     }
                     break;
 
                 case CONST:
                     if (!cursor.isWhitespaceChar()) {
                         AnalyzeResult constResult = matchConst(cursor, expectedEnds);
                         result.addResult(constResult);
                         if (cursor.matchNextPatterns(expectedEnds))
                             state = Operand1State.END;
                         else
                             throw new UnexpectedOperandException(cursor);
                     }
                     break;
 
                 default:
                     break;
             }
             if (state != Operand1State.END)
                 cursor.incrementPosition();
         }
 
         if (state != Operand1State.END)
             throw new IncompleteExpressionException(cursor);
 
         return result;
     }
 
     public AnalyzeResult matchOperation(AnalyzeCursor cursor) throws AnalyzeException {
         AnalyzeResult result = new AnalyzeResult();
         OperationState state = OperationState.START;
 
         while ((cursor.inExpression()) && (state != OperationState.END)) {
             switch (state) {
                 case START:
                     if (cursor.skipNextPatterns(PATTERN_SKIP_OPERATIONS)) {
                         cursor.skipWhitespace();
                         if (cursor.inExpression()) {
                             state = OperationState.END;
                         } else
                             throw new IncompleteExpressionException(cursor);
                     } else
                         throw new UnknownOperationException(cursor);
                     break;
             }
 
             if (state != OperationState.END)
                 cursor.incrementPosition();
         }
 
         if (state != OperationState.END)
             throw new IncompleteExpressionException(cursor);
 
         return result;
     }
 
     public SemanticResult matchValue(AnalyzeCursor cursor, String[] expectedEnds) throws AnalyzeException {
         SemanticResult result = new SemanticResult();
         ValueState state = ValueState.START;
         int startIndex = cursor.getPosition();
         while ((cursor.inExpression()) && (state != ValueState.END)) {
             switch (state) {
                 case START:
                     if (!cursor.isWhitespaceChar()) {
                         state = ValueState.OPERAND_2;
                         startIndex = cursor.getPosition();
                         cursor.decrementPosition();
                     }
                     break;
 
                 case OPERAND_2:
                     AnalyzeResult operand2Result = matchOperand2(cursor, mergeVariants(EXPECTED_ENDS_OPERAND, expectedEnds));
                     result.addResult(operand2Result);
                     boolean hasWhitespaces = cursor.isWhitespaceChar();
                     cursor.skipWhitespace();
                     if (cursor.inExpression()) {
                         if (hasWhitespaces) {
                             cursor.decrementPosition();
                             if (cursor.matchNextPatterns(expectedEnds))
                                 state = ValueState.END;
                             else if (cursor.matchNextPatterns(PATTERN_FULL_OPERATIONS)) {
                                 state = ValueState.OPERATION;
                             } else
                                 throw new ExpectOperationException(cursor);
                         } else {
                             if (cursor.matchNextPatterns(PATTERN_ELEMENTARY_OPERATIONS)) {
                                 cursor.decrementPosition();
                                 state = ValueState.OPERATION;
                             }
                         }
                     } else
                         throw new IncompleteExpressionException(cursor);
                     break;
 
                 case OPERATION:
                     matchOperation(cursor);
                     if (cursor.isIdentifierStartChar() || cursor.isNumberStartChar()) {
                         cursor.decrementPosition();
                         state = ValueState.OPERAND_2;
                     } else
                         throw new ExpectOperandException(cursor);
                     break;
 
                 default:
                     break;
             }
 
             if (state != ValueState.END)
                 cursor.incrementPosition();
         }
 
         if (state != ValueState.END)
             throw new IncompleteExpressionException(cursor);
 
         int endIndex = cursor.getPosition();
         result.setBeginValue(cursor.getSource().substring(startIndex, endIndex).intern());
 
         return result;
     }
 
     public AnalyzeResult matchOperand2(AnalyzeCursor cursor, String[] expectedEnds) throws AnalyzeException {
         AnalyzeResult result = new AnalyzeResult();
         Operand2State state = Operand2State.START;
 
         while ((cursor.inExpression()) && (state != Operand2State.END)) {
             switch (state) {
                 case START:
                     if (!cursor.isWhitespaceChar()) {
                         if (cursor.isNumberStartChar()) {
                             cursor.decrementPosition();
                             state = Operand2State.CONST;
                         } else if (cursor.isIdentifierStartChar()) {
                             cursor.decrementPosition();
                             state = Operand2State.PARAMETER;
                         } else
                             throw new UnexpectedOperandException(cursor);
                     }
                     break;
 
                 case CONST:
                     AnalyzeResult constResult = matchConst(cursor, expectedEnds);
                     result.addResult(constResult);
                     state = Operand2State.END;
                     break;
 
                 case PARAMETER:
                     AnalyzeResult paramResult = matchParameter(cursor, expectedEnds, EXPECTED_SPECIAL_ENDS_IDENTITY);
                     result.addResult(paramResult);
                     state = Operand2State.END;
                     break;
 
                 default:
                     break;
             }
 
             if (state != Operand2State.END)
                 cursor.incrementPosition();
         }
 
         if (state != Operand2State.END)
             throw new IncompleteExpressionException(cursor);
 
         return result;
     }
 
     public AnalyzeResult matchConst(AnalyzeCursor cursor, String[] expectedEnds) throws AnalyzeException {
         AnalyzeResult result = new AnalyzeResult();
         ConstState state = ConstState.START;
 
         int startIndex = cursor.getPosition();
 
         while ((cursor.inExpression()) && (state != ConstState.END)) {
             switch (state) {
                 case START:
                     if (!cursor.isWhitespaceChar()) {
                         if (cursor.isNumberStartChar()) {
                             state = ConstState.NUMBER;
                             startIndex = cursor.getPosition();
                         } else
                             throw new ConstantFormatException(cursor);
                     }
                     break;
 
                 case NUMBER:
                     if (!cursor.isNumberBodyChar()) {
                         if (cursor.matchNextPatterns(expectedEnds)) {
                             state = ConstState.END;
                             int endIndex = cursor.getPosition();
                             String substring = cursor.getSource().substring(startIndex, endIndex);
 
                             try {
                                int constVal = Integer.parseInt(substring);
                                if (constVal > MAX_CONST_VALUE)
                                    throw new ConstantLengthException(String.valueOf(constVal));

                             } catch (Exception ex) {
                                 throw new ConstantFormatException(cursor);
                             }
 
                             result.getConstants().add(substring.intern());
                         } else
                             throw new ConstantFormatException(cursor);
                     }
                     break;
 
                 default:
                     break;
             }
             if (state != ConstState.END)
                 cursor.incrementPosition();
         }
 
         if (state != ConstState.END)
             throw new IncompleteExpressionException(cursor);
 
         return result;
     }
 
     public AnalyzeResult matchIdentity(AnalyzeCursor cursor, String[] expectedEnds) throws AnalyzeException {
         AnalyzeResult result = new AnalyzeResult();
         IdentityState state = IdentityState.START;
 
         int startIndex = cursor.getPosition();
 
         while ((cursor.inExpression()) && (state != IdentityState.END)) {
             switch (state) {
                 case START:
                     if (!cursor.isWhitespaceChar()) {
                         if (cursor.isIdentifierStartChar()) {
                             startIndex = cursor.getPosition();
                             cursor.decrementPosition();
                             state = IdentityState.NAME;
                         } else
                             throw new IncorrectIndetifierName(cursor);
                     }
                     break;
 
                 case NAME:
                     if (!cursor.isIdendtifierBodyChar()) {
                         if (cursor.matchNextPatterns(expectedEnds)) {
                             state = IdentityState.END;
                             int endIndex = cursor.getPosition();

                            int length = endIndex - startIndex;
                            String varName = cursor.getSource().substring(startIndex, endIndex);
                            if (length > MAX_IDENTITY_NAME_LENGTH)
                                throw new IncorrectIdentifierLength(cursor, varName);

                            result.getVariables().add(varName.intern());
                         } else
                             throw new IncorrectIndetifierName(cursor);
                     }
                     break;
 
                 default:
                     break;
             }
             if (state != IdentityState.END)
                 cursor.incrementPosition();
         }
 
         if (state != IdentityState.END)
             throw new IncompleteExpressionException(cursor);
 
         return result;
     }
 
     public String[] mergeVariants(String[] aVariant, String[] bVariant) {
         HashSet<String> expectedEndsIdenity = new HashSet<String>(Arrays.asList(aVariant));
         expectedEndsIdenity.addAll(Arrays.asList(bVariant));
 
         return expectedEndsIdenity.toArray(new String[expectedEndsIdenity.size()]);
     }
 }
