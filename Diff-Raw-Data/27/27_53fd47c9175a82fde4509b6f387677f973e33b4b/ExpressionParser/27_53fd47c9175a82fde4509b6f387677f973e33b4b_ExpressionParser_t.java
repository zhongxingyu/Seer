 package parser;
 
 import com.google.common.collect.Lists;
 import lexer.DiamondLexer.Lexeme;
 import lexer.Token;
 
 import java.math.BigInteger;
 import java.util.Deque;
 import java.util.EnumSet;
 import java.util.List;
 import java.util.Set;
 
 final class ExpressionParser {
     /*
      * Intermediate expression parse states can consist of a heterogeneous stream of tokens and sub-expressions. As a
      * result, we are forced to store the state in a List<Object>. At the beginning, the list consists entirely of
      * tokens; at the end, if everything goes as planned, it should consist entirely of expressions.
      */
     private List<Object> stream;
 
     public ExpressionParser() {
     }
 
     public List<Expression> parseExpression(List<Token<Lexeme>> tokens) throws ParseException {
         stream = Lists.<Object>newArrayList(tokens);
         parseGroupedExpressions(tokens); // recursively parse parenthetical and bracketed expressions first
         parseGroupingOperators();
         for (int precedence = 14; precedence > 0; precedence--) {
             parseOperators(precedence);
         }
        parseVariableDeclarations();
         parseLists();
 
         // verify that the stream now consists entirely of expressions
         List<Expression> toReturn = Lists.newArrayListWithCapacity(stream.size());
         for (int i = 0; i < stream.size(); i++) {
             toReturn.add(marshalExpression(i));
         }
         return toReturn;
     }
 
     private static enum GroupingSymbol {
         PARENTHESIS, BRACKET
     }
 
     private void parseGroupedExpressions(List<Token<Lexeme>> tokens) throws ParseException {
         // use the TOKEN stream, since the state stream will change if we encounter grouping symbols
         Deque<GroupingSymbol> groupingStack = Lists.newLinkedList(); // stack to handle nested grouping symbols
         int startIndex = -1; // the starting index of the current outermost group
         int streamOffset = 0; // offset between the state and token streams, increases as we replace tokens with expressions in the state stream
         for (int i = 0; i < tokens.size(); i++) {
             Token<Lexeme> token = tokens.get(i);
             GroupingSymbol symbol;
             switch (token.lexeme) {
                 case LEFT_PAREN: case RIGHT_PAREN: symbol = GroupingSymbol.PARENTHESIS; break;
                 case LEFT_BRACKET: case RIGHT_BRACKET: symbol = GroupingSymbol.BRACKET; break;
                 default: symbol = null;
             }
             if (symbol == null) {
                 continue;
             }
             switch (token.lexeme) {
                 case LEFT_PAREN: case LEFT_BRACKET:
                     if (startIndex < 0) {
                         startIndex = i + 1;
                     }
                     groupingStack.push(symbol);
                     break;
                 case RIGHT_PAREN: case RIGHT_BRACKET:
                     if (groupingStack.pop() != symbol) {
                         throw new ParseException("mismatched grouping symbols");
                     } else if (groupingStack.isEmpty()) {
                         List<Token<Lexeme>> group = tokens.subList(startIndex, i);
                         List<Expression> subExpressions = parseExpression(group);
                         stream.subList(startIndex - streamOffset, i - streamOffset).clear();
                         stream.addAll(startIndex - streamOffset, subExpressions);
                         streamOffset += (group.size() - subExpressions.size());
                         startIndex = -1;
                     }
                     break;
             }
         }
 
         // if the stack has anything left, syntax error
         if (!groupingStack.isEmpty()) {
             switch (groupingStack.pop()) {
                 case PARENTHESIS:
                     throw new ParseException("expected ')'");
                 case BRACKET:
                     throw new ParseException("expected ']'");
             }
         }
     }
 
     private ExpressionType getTypeEndingAt(int index) throws ParseException {
         switch (marshalToken(index).lexeme) {
             case IDENTIFIER:
                 return new UserDefinedType(marshalToken(index).contents);
             case RIGHT_BRACKET:
                 return new ArrayType(getTypeEndingAt(index - 2));
             case BOOLEAN:
                 return BuiltInType.BOOLEAN;
             case SHORT:
                 return BuiltInType.SHORT;
             case INT:
                 return BuiltInType.INT;
             case LONG:
                 return BuiltInType.LONG;
             default:
                 throw new ParseException("expected identifier, primitive type, or ']'");
         }
     }
 
     private Set<Modifier> getModifiersEndingAt(int index) throws ParseException {
         Set<Modifier> modifiers = EnumSet.noneOf(Modifier.class);
         boolean flag = true;
         while (flag) {
             if (stream.get(index) instanceof Token) {
                 Token<Lexeme> token = marshalToken(index--);
                 switch (token.lexeme) {
                     case PRIVATE: modifiers.add(Modifier.PRIVATE); break;
                     case STATIC: modifiers.add(Modifier.STATIC); break;
                     case UNSAFE: modifiers.add(Modifier.UNSAFE); break;
                     default: flag = false;
                 }
             } else {
                 flag = false;
             }
         }
         return modifiers;
     }
 
     private void parseGroupingOperators() throws ParseException {
         for (int i = 0; i < stream.size(); i++) {
             if (stream.get(i) instanceof Token) {
                 Token<Lexeme> token = marshalToken(i);
                 Expression target = null;
                 List<Object> subList;
                 switch (token.lexeme) {
                     case LEFT_BRACKET:
                         if (stream.get(i + 1) instanceof Token && marshalToken(i + 1).lexeme == Lexeme.RIGHT_BRACKET) {
                             if (marshalToken(i + 2).lexeme == Lexeme.IDENTIFIER) {
                                 // variable declaration with an array type
                                 ExpressionType type = getTypeEndingAt(i + 1);
                                 int typeLength = type.getNumberOfLexemes();
                                 Set<Modifier> modifiers = getModifiersEndingAt((i + 1) - typeLength);
                                 Expression expression = new VariableDeclaration(type, marshalToken(i + 2).contents, modifiers);
                                 subList = stream.subList(i - typeLength - modifiers.size(), i + 3);
                                 subList.clear();
                                 subList.add(expression);
                                 i -= (typeLength + modifiers.size());
                                 break;
                             } else if (marshalToken(i + 2).lexeme == Lexeme.LEFT_BRACKET) {
                                 // well, we're in the middle of a multi-dimensional array type token
                                 // get out of here and let the next iteration do it
                                 i += 1;
                                 break;
                             } else if (marshalToken(i + 2).lexeme == Lexeme.PERIOD) {
                                 // member access on our array type
                                 // turn our (compound) array type into an identifier and bounce off of PERIOD
                                 ExpressionType type = getTypeEndingAt(i + 1);
                                 int typeLength = type.getNumberOfLexemes();
                                subList = stream.subList((i + 2) - typeLength, i + 2);
                                 subList.clear();
                                 subList.add(new Token<Lexeme>(Lexeme.IDENTIFIER, type.toString()));
                                i -= (typeLength - 2);
                             } else {
                                 // not sure what else could be here?
                                 throw new ParseException("expected identifier, '[', or '.'");
                             }
                         } else {
                             // array access
                             if (marshalToken(i + 2).lexeme != Lexeme.RIGHT_BRACKET) {
                                 throw new ParseException("expected ']'");
                             }
                             Expression array = marshalExpression(i - 1);
                             Expression index = marshalExpression(i + 1);
                             Expression arrayAccess = new ArrayAccess(array, index);
                             subList = stream.subList(i - 1, i + 3);
                             subList.clear();
                             subList.add(arrayAccess);
                             i -= 1;
                             break;
                         }
                     case PERIOD:
                         // has to be a member access, let's figure out what kind
                         if (marshalToken(i - 1).lexeme != Lexeme.IDENTIFIER) {
                             throw new ParseException("expected identifier");
                         } else if (marshalToken(i + 1).lexeme != Lexeme.IDENTIFIER && marshalToken(i + 1).lexeme != Lexeme.NEW) {
                             throw new ParseException("expected identifier or \"new\"");
                         }
                         Token<Lexeme> farRightToken = marshalToken(i + 2);
                         if (farRightToken.lexeme == Lexeme.LEFT_PAREN) {
                             // method member access
                             // deliberately no break; let it be handled as a method invocation below
                             target = marshalExpression(i - 1);
                             i += 2;
                         } else {
                             // field member access
                             // the field had better not be called new!
                             if (marshalToken(i + 1).lexeme != Lexeme.IDENTIFIER) {
                                 throw new ParseException("expected identifier");
                             }
                             String field = marshalToken(i + 1).contents;
                             Expression reference = new FieldReference(marshalExpression(i - 1), field);
                             subList = stream.subList(i - 1, i + 2);
                             subList.clear();
                             subList.add(reference);
                             i -= 1;
                             break;
                         }
                     case LEFT_PAREN:
                         Token<Lexeme> leftToken = marshalToken(i - 1);
                         if (leftToken.lexeme == Lexeme.IDENTIFIER || leftToken.lexeme == Lexeme.NEW) {
                             // method or constructor invocation
                             List<Expression> parameters = Lists.newArrayList();
                             int j = i + 1;
                             while (true) {
                                 Object obj = stream.get(j++);
                                 if (obj instanceof Expression) {
                                     parameters.add((Expression) obj);
                                 } else {
                                     break;
                                 }
                             }
                             if (marshalToken(j).lexeme != Lexeme.RIGHT_PAREN) {
                                 throw new ParseException("expected ')'");
                             }
                             Expression invocation;
                             if (leftToken.lexeme == Lexeme.IDENTIFIER) {
                                 if (target == null) {
                                     target = new ThisExpression();
                                     subList = stream.subList(i - 1, j + 1);
                                     i -= 1;
                                 } else {
                                     subList = stream.subList(i - 3, j + 1);
                                     i -= 3;
                                 }
                                 invocation = new MethodInvocation(leftToken.contents, target, parameters);
                             } else if (leftToken.lexeme == Lexeme.NEW) {
                                 if (target == null) {
                                     invocation = new ConstructorInvocation(null, parameters);
                                     subList = stream.subList(i - 1, j + 1);
                                     i -= 1;
                                 } else {
                                     invocation = new ConstructorInvocation(((IdentifierReference) target).getName(), parameters);
                                     subList = stream.subList(i - 3, j + 1);
                                     i -= 3;
                                 }
                             } else {
                                 throw new AssertionError("Left token MUST either be an identifier or lexeme!");
                             }
                             subList.clear();
                             subList.add(invocation);
                         } else {
                             // simple parenthetical expression
                             Expression expression = marshalExpression(i + 1);
                             if (marshalToken(i + 2).lexeme != Lexeme.RIGHT_PAREN) {
                                 throw new ParseException("expected ')'");
                             }
                             subList = stream.subList(i, i + 3);
                             subList.clear();
                             subList.add(expression);
                         }
                         break;
                 }
             }
         }
     }
 
     private void parseOperators(int precedence) throws ParseException {
         if (precedence == 1 || precedence == 2 || precedence == 13 || precedence == 14) {
             // right associative
             for (int i = stream.size() - 1; i >= 0; i--) {
                 parseOperatorAtIndex(precedence, i);
             }
         } else {
             // left associative
             for (int i = 0; i < stream.size(); i++) {
                 parseOperatorAtIndex(precedence, i);
             }
         }
     }
 
     private void parseOperatorAtIndex(int precedence, int i) throws ParseException {
         if (stream.get(i) instanceof Token) {
             Token<Lexeme> token = marshalToken(i);
             Operator operator = Operator.getForLexeme(token.lexeme);
             if (operator != null && operator.getPrecedence() == precedence) {
                 List<Object> subList;
                 Expression expression;
                 switch (operator.getType()) {
                     case POSTFIX:
                         subList = stream.subList(i - 1, i + 1);
                         expression = new OperatorExpression(marshalExpression(i - 1), operator);
                         i -= 1;
                         break;
                     case UNARY:
                         subList = stream.subList(i, i + 2);
                         expression = new OperatorExpression(operator, marshalExpression(i + 1));
                         break;
                     case BINARY:
                         subList = stream.subList(i - 1, i + 2);
                         expression = new OperatorExpression(marshalExpression(i - 1), marshalExpression(i + 1), operator);
                         i -= 1;
                         break;
                     default:
                         throw new AssertionError();
                 }
                 subList.clear();
                 subList.add(expression);
             }
         }
     }
 
    private void parseVariableDeclarations() throws ParseException {
        for (int i = 0; i < stream.size(); i++) {
            if (stream.get(i) instanceof Token) {
                Token<Lexeme> token = marshalToken(i);
                if (token.lexeme == Lexeme.IDENTIFIER) {
                    // there should be a type immediately before us
                    ExpressionType type = getTypeEndingAt(i - 1);
                    int typeLength = type.getNumberOfLexemes();
                    Set<Modifier> modifiers = getModifiersEndingAt((i - 1) - typeLength);
                    Expression expression = new VariableDeclaration(type, token.contents, modifiers);
                    List<Object> subList = stream.subList(i - typeLength - modifiers.size(), i);
                    subList.clear();
                    subList.add(expression);
                    i -= (typeLength + modifiers.size());
                }
            }
        }
    }

     private void parseLists() throws ParseException {
         for (int i = 0; i < stream.size(); i++) {
             if (stream.get(i) instanceof Token) {
                 Token<Lexeme> token = marshalToken(i);
                 if (token.lexeme == Lexeme.COMMA) {
                     // ensure that we're between two expressions
                     stream.set(i - 1, marshalExpression(i - 1));
                     stream.set(i + 1, marshalExpression(i + 1));
                     // then we can delete this token so that the main parsing succeeds
                     stream.remove(i);
                     i -= 1;
                 }
             }
         }
     }
 
     private Token<Lexeme> marshalToken(int index) throws ParseException {
         Object obj = stream.get(index);
         if (obj instanceof Token) {
             @SuppressWarnings("unchecked")
             Token<Lexeme> token = (Token<Lexeme>) stream.get(index);
             return token;
         } else {
             throw new ParseException("expected free token");
         }
     }
 
     private Expression marshalExpression(int index) throws ParseException {
         Object obj = stream.get(index);
         if (obj instanceof Expression) {
             return (Expression) obj;
         } else {
             @SuppressWarnings("unchecked")
             Token<Lexeme> token = (Token<Lexeme>) obj;
             switch (token.lexeme) {
                 case IDENTIFIER:
                     return new IdentifierReference(token.contents);
                 case NUMBER:
                     BigInteger value;
                     if (token.contents.startsWith("0x")) {
                         value = new BigInteger(token.contents.substring(2), 16);
                     } else {
                         value = new BigInteger(token.contents);
                     }
                     return new IntegralLiteral(value);
                 case STRING: // TODO: there have to be string literals
                     return new StringLiteral(token.contents);
                 default:
                     throw new ParseException("expected expression");
             }
         }
     }
 }
