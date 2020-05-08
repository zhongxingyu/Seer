 package de.weltraumschaf.caythe.frontend.pascal.parsers;
 
 import de.weltraumschaf.caythe.intermediate.symboltableimpl.DefinitionImpl;
 import de.weltraumschaf.caythe.intermediate.Definition;
 import de.weltraumschaf.caythe.intermediate.TypeSpecification;
 import de.weltraumschaf.caythe.intermediate.codeimpl.CodeNodeTypeImpl;
 import de.weltraumschaf.caythe.frontend.TokenType;
 import de.weltraumschaf.caythe.intermediate.CodeNodeType;
 import java.util.HashMap;
 import de.weltraumschaf.caythe.frontend.pascal.PascalTokenType;
 import java.util.EnumSet;
 import de.weltraumschaf.caythe.intermediate.SymbolTableEntry;
 import de.weltraumschaf.caythe.frontend.Token;
 import de.weltraumschaf.caythe.frontend.pascal.PascalTopDownParser;
 import de.weltraumschaf.caythe.intermediate.CodeFactory;
 import de.weltraumschaf.caythe.intermediate.CodeNode;
 import de.weltraumschaf.caythe.intermediate.TypeFactory;
 import de.weltraumschaf.caythe.intermediate.symboltableimpl.Predefined;
 import de.weltraumschaf.caythe.intermediate.typeimpl.TypeChecker;
 
 import static de.weltraumschaf.caythe.intermediate.codeimpl.CodeNodeTypeImpl.*;
 import static de.weltraumschaf.caythe.intermediate.codeimpl.CodeKeyImpl.*;
 import static de.weltraumschaf.caythe.frontend.pascal.PascalTokenType.*;
 import static de.weltraumschaf.caythe.frontend.pascal.PascalErrorCode.*;
 import static de.weltraumschaf.caythe.intermediate.symboltableimpl.SymbolTableKeyImpl.*;
 
 /**
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  * @license http://www.weltraumschaf.de/the-beer-ware-license.txt THE BEER-WARE LICENSE
  */
 public class ExpressionParser extends StatementParser {
 
     public ExpressionParser(PascalTopDownParser parent) {
         super(parent);
     }
 
     @Override
     public CodeNode parse(Token token) throws Exception {
         return parseExpression(token);
     }
 
     // Set of relational operators.
     private static final EnumSet<PascalTokenType> REL_OPS =
             EnumSet.of(EQUALS, NOT_EQUALS, LESS_THAN, LESS_EQUALS,
             GREATER_THAN, GREATER_EQUALS);
     // Map relational operator tokens to node types.
    private static final HashMap<PascalTokenType, CodeNodeType> REL_OPS_MAP
            = new HashMap<PascalTokenType, CodeNodeType>();
 
     static {
         REL_OPS_MAP.put(EQUALS, EQ);
         REL_OPS_MAP.put(NOT_EQUALS, NE);
         REL_OPS_MAP.put(LESS_THAN, LT);
         REL_OPS_MAP.put(LESS_EQUALS, LE);
         REL_OPS_MAP.put(GREATER_THAN, GT);
         REL_OPS_MAP.put(GREATER_EQUALS, GE);
     }
 
     // Synchronization set for starting an expression.
     static final EnumSet<PascalTokenType> EXPRESSION_START_SET =
         EnumSet.of(PLUS, MINUS, IDENTIFIER, INTEGER, REAL, STRING,
                    PascalTokenType.NOT, LEFT_PAREN);
     /**
      * Parse an expression.
      * @param token the initial token.
      * @return the root of the generated parse subtree.
      * @throws Exception if an error occurred.
      */
     private CodeNode parseExpression(Token token) throws Exception {
         // Parse a simple expression and make the root of its tree
         // the root node.
         CodeNode rootNode = parseSimpleExpression(token);
         TypeSpecification resultType = rootNode != null
                 ? rootNode.getTypeSpecification()
                 : Predefined.undefinedType;
         token = currentToken();
         TokenType tokenType = token.getType();
 
         // Look for a relational operator.
         if (REL_OPS.contains(tokenType)) {
             // Create a new operator node and adopt the current tree
             // as its first child.
             CodeNodeType nodeType = REL_OPS_MAP.get(tokenType);
             CodeNode opNode = CodeFactory.createCodeNode(nodeType);
             opNode.addChild(rootNode);
 
             token = nextToken();  // consume the operator
 
             // Parse the second simple expression.  The operator node adopts
             // the simple expression's tree as its second child.
             CodeNode simExprNode = parseSimpleExpression(token);
             opNode.addChild(simExprNode);
 
             // The operator node becomes the new root node.
             rootNode = opNode;
 
             // Type check: The operands must be comparison compatible.
             TypeSpecification simExprType = simExprNode != null
                     ? simExprNode.getTypeSpecification()
                     : Predefined.undefinedType;
 
             if (TypeChecker.areComparisonCompatible(resultType, simExprType)) {
                 resultType = Predefined.booleanType;
             } else {
                 errorHandler.flag(token, INCOMPATIBLE_TYPES, this);
                 resultType = Predefined.undefinedType;
             }
 
         }
 
         if (null != rootNode) {
             rootNode.setTypeSpecification(resultType);
         }
 
         return rootNode;
     }
 
     // Set of additive operators.
     private static final EnumSet<PascalTokenType> ADD_OPS = EnumSet.of(PLUS, MINUS, PascalTokenType.OR);
     // Map additive operator tokens to node types.
     private static final HashMap<PascalTokenType, CodeNodeTypeImpl> ADD_OPS_MAP = new HashMap<PascalTokenType, CodeNodeTypeImpl>();
 
     static {
         ADD_OPS_MAP.put(PLUS, ADD);
         ADD_OPS_MAP.put(MINUS, SUBTRACT);
         ADD_OPS_MAP.put(PascalTokenType.OR, CodeNodeTypeImpl.OR);
     }
 
     /**
      * Parse a simple expression.
      * @param token the initial token.
      * @return the root of the generated parse subtree.
      * @throws Exception if an error occurred.
      */
     private CodeNode parseSimpleExpression(Token token) throws Exception {
         Token signToken    = null;
         TokenType signType = null;  // type of leading sign (if any)
 
         // Look for a leading + or - sign.
         TokenType tokenType = token.getType();
 
         if (( tokenType == PLUS ) || ( tokenType == MINUS )) {
             signType  = tokenType;
             signToken = token;
             token     = nextToken();  // consume the + or -
         }
 
         // Parse a term and make the root of its tree the root node.
         CodeNode rootNode = parseTerm(token);
         TypeSpecification resultType = rootNode != null
                 ? rootNode.getTypeSpecification()
                 : Predefined.undefinedType;
 
         // Type check: Leading sign.
         if ((signType != null) && (!TypeChecker.isIntegerOrReal(resultType))) {
             errorHandler.flag(signToken, INCOMPATIBLE_TYPES, this);
         }
 
         // Was there a leading - sign?
         if (signType == MINUS) {
             // Create a NEGATE node and adopt the current tree
             // as its child. The NEGATE node becomes the new root node.
             CodeNode negateNode = CodeFactory.createCodeNode(NEGATE);
             negateNode.addChild(rootNode);
             negateNode.setTypeSpecification(rootNode.getTypeSpecification());
             rootNode = negateNode;
         }
 
         token     = currentToken();
         tokenType = token.getType();
 
         // Loop over additive operators.
         while (ADD_OPS.contains(tokenType)) {
             TokenType operator = tokenType;
             // Create a new operator node and adopt the current tree
             // as its first child.
             CodeNodeType nodeType = ADD_OPS_MAP.get(tokenType);
             CodeNode opNode = CodeFactory.createCodeNode(nodeType);
             opNode.addChild(rootNode);
 
             token = nextToken();  // consume the operator
 
             // Parse another term.  The operator node adopts
             // the term's tree as its second child.
             CodeNode termNode = parseTerm(token);
             opNode.addChild(termNode);
             TypeSpecification termType = termNode != null
                     ? termNode.getTypeSpecification()
                     : Predefined.undefinedType;
 
             // The operator node becomes the new root node.
             rootNode = opNode;
 
             //Determine the result type.
             switch ((PascalTokenType) operator) {
                 case PLUS: case MINUS: {
                     // Both operands integer ==> integer result.
                     if (TypeChecker.areBothInteger(resultType, termType)) {
                         resultType = Predefined.integerType;
                     }
                     // Both real operands or one real and one integr operand
                     // ==> real result.
                     else if (TypeChecker.isAtLeastOneReal(resultType, termType)) {
                         resultType = Predefined.realType;
                     }
                     else {
                         errorHandler.flag(token, INCOMPATIBLE_TYPES, this);
                     }
 
                     break;
                 }
                 case OR: {
                     // Both operands boolean ==> boolean result.
                     if (TypeChecker.areBothBoolean(resultType, termType)) {
                         resultType = Predefined.booleanType;
                     } else {
                         errorHandler.flag(token, INCOMPATIBLE_TYPES, this);
                     }
 
                     break;
                 }
             }
 
             rootNode.setTypeSpecification(resultType);
             token     = currentToken();
             tokenType = token.getType();
         }
 
         return rootNode;
     }
 
     // Set of multiplicative operators.
     private static final EnumSet<PascalTokenType> MULT_OPS =
             EnumSet.of(STAR, SLASH, DIV, PascalTokenType.MOD, PascalTokenType.AND);
     // Map multiplicative operator tokens to node types.
     private static final HashMap<PascalTokenType, CodeNodeType> MULT_OPS_MAP = new HashMap<PascalTokenType, CodeNodeType>();
 
     static {
         MULT_OPS_MAP.put(STAR, MULTIPLY);
         MULT_OPS_MAP.put(SLASH, FLOAT_DIVIDE);
         MULT_OPS_MAP.put(DIV, INTEGER_DIVIDE);
         MULT_OPS_MAP.put(PascalTokenType.MOD, CodeNodeTypeImpl.MOD);
         MULT_OPS_MAP.put(PascalTokenType.AND, CodeNodeTypeImpl.AND);
     };
 
     /**
      * Parse a term.
      * @param token the initial token.
      * @return the root of the generated parse subtree.
      * @throws Exception if an error occurred.
      */
     private CodeNode parseTerm(Token token) throws Exception {
         // Parse a factor and make its node the root node.
         CodeNode rootNode = parseFactor(token);
         TypeSpecification resultType = rootNode != null
                 ? rootNode.getTypeSpecification()
                 : Predefined.undefinedType;
         token = currentToken();
         TokenType tokenType = token.getType();
 
         // Loop over multiplicative operators.
         while (MULT_OPS.contains(tokenType)) {
             TokenType operator = tokenType;
             // Create a new operator node and adopt the current tree
             // as its first child.
             CodeNodeType nodeType = MULT_OPS_MAP.get(tokenType);
             CodeNode opNode = CodeFactory.createCodeNode(nodeType);
             opNode.addChild(rootNode);
 
             token = nextToken();  // consume the operator
 
             // Parse another factor.  The operator node adopts
             // the term's tree as its second child.
             CodeNode factorNode = parseFactor(token);
             opNode.addChild(factorNode);
             TypeSpecification factorType = factorNode != null
                     ? factorNode.getTypeSpecification()
                     : Predefined.undefinedType;
 
             // The operator node becomes the new root node.
             rootNode = opNode;
 
             switch ((PascalTokenType) operator) {
                 case STAR: {
                     // Both operands integer ==> integer result.
                     if (TypeChecker.areBothInteger(resultType, factorType)) {
                        resultType = Predefined.integerType;
                    }
                    // Both real operands or one real and one integer operand
                    // ==> real result.
                    else if (TypeChecker.isAtLeastOneReal(resultType, factorType)) {
                         resultType = Predefined.realType;
                    }
                    else {
                         errorHandler.flag(token, INCOMPATIBLE_TYPES, this);
                     }
 
                     break;
                 }
 
                 case SLASH: {
                      // All integer and real operand combinations
                     // ==> real result.
                     if (TypeChecker.areBothInteger(resultType, factorType) || TypeChecker.isAtLeastOneReal(resultType, factorType)) {
                         resultType = Predefined.realType;
                     }
                     else {
                         errorHandler.flag(token, INCOMPATIBLE_TYPES, this);
                     }
 
                     break;
                 }
 
                 case DIV: case MOD: {
                     // Both operands integer ==> integer result.
                     if (TypeChecker.areBothInteger(resultType, factorType)) {
                         resultType = Predefined.integerType;
                     } else {
                         errorHandler.flag(token, INCOMPATIBLE_TYPES, this);
                     }
 
                     break;
                 }
 
                 case AND: {
                     // Both operands boolean ==> boolean result.
                     if (TypeChecker.areBothBoolean(resultType, factorType)) {
                         resultType = Predefined.booleanType;
                     } else {
                         errorHandler.flag(token, INCOMPATIBLE_TYPES, this);
                     }
 
                     break;
                 }
             }
 
             if (null != rootNode) {
                 rootNode.setTypeSpecification(resultType);
             }
 
             token     = currentToken();
             tokenType = token.getType();
         }
 
         return rootNode;
     }
 
     /**
      * Parse a factor.
      * @param token the initial token.
      * @return the root of the generated parse subtree.
      * @throws Exception if an error occurred.
      */
     private CodeNode parseFactor(Token token) throws Exception {
         TokenType tokenType = token.getType();
         CodeNode rootNode = null;
 
         switch ((PascalTokenType) tokenType) {
 
             case IDENTIFIER: {
                 return parseIdentifier(token);
             }
 
             case INTEGER: {
                 // Create an INTEGER_CONSTANT node as the root node.
                 rootNode = CodeFactory.createCodeNode(INTEGER_CONSTANT);
                 rootNode.setAttribute(VALUE, token.getValue());
                 token = nextToken();  // consume the number
                 rootNode.setTypeSpecification(Predefined.integerType);
                 break;
             }
 
             case REAL: {
                 // Create an REAL_CONSTANT node as the root node.
                 rootNode = CodeFactory.createCodeNode(REAL_CONSTANT);
                 rootNode.setAttribute(VALUE, token.getValue());
                 token = nextToken();  // consume the number
                 rootNode.setTypeSpecification(Predefined.realType);
                 break;
             }
 
             case STRING: {
                 String value = (String) token.getValue();
                 // Create a STRING_CONSTANT node as the root node.
                 rootNode = CodeFactory.createCodeNode(STRING_CONSTANT);
                 rootNode.setAttribute(VALUE, value);
                 TypeSpecification resultType = value.length() == 1
                         ? Predefined.charType
                         : TypeFactory.createType(value);
                 token = nextToken();  // consume the string
                 rootNode.setTypeSpecification(resultType);
                 break;
             }
 
             case NOT: {
                 token = nextToken();  // consume the NOT
                 // Create a NOT node as the root node.
                 rootNode = CodeFactory.createCodeNode(CodeNodeTypeImpl.NOT);
                 // Parse the factor.  The NOT node adopts the
                 // factor node as its child.
                 CodeNode factorNode = parseFactor(token);
                 rootNode.addChild(factorNode);
                 // Type check: The factor must be boolean.
                 TypeSpecification factorType = factorNode != null
                                           ? factorNode.getTypeSpecification()
                                           : Predefined.undefinedType;
 
                 if (!TypeChecker.isBoolean(factorType)) {
                     errorHandler.flag(token, INCOMPATIBLE_TYPES, this);
                 }
 
                 rootNode.setTypeSpecification(Predefined.booleanType);
                 break;
             }
 
             case LEFT_PAREN: {
                 token = nextToken();      // consume the (
                 // Parse an expression and make its node the root node.
                 rootNode = parseExpression(token);
                 TypeSpecification resultType = rootNode != null
                                           ? rootNode.getTypeSpecification()
                                           : Predefined.undefinedType;
 
                 // Look for the matching ) token.
                 token = currentToken();
                 if (token.getType() == RIGHT_PAREN) {
                     token = nextToken();  // consume the )
                 } else {
                     errorHandler.flag(token, MISSING_RIGHT_PAREN, this);
                 }
 
                 rootNode.setTypeSpecification(resultType);
                 break;
             }
 
             default: {
                 errorHandler.flag(token, UNEXPECTED_TOKEN, this);
                 break;
             }
         }
 
         return rootNode;
     }
 
     private CodeNode parseIdentifier(Token token) throws Exception {
         CodeNode rootNode = null;
         // Look up the identifier in the symbol table stack.
         String name = token.getText().toLowerCase();
         SymbolTableEntry id = symbolTableStack.lookup(name);
 
         // Undefined.
         if (id == null) {
             errorHandler.flag(token, IDENTIFIER_UNDEFINED, this);
             id = symbolTableStack.enterLocal(name);
             id.setDefinition(DefinitionImpl.UNDEFINED);
             id.setTypeSpecification(Predefined.undefinedType);
         }
 
         Definition defnCode = id.getDefinition();
 
         switch ((DefinitionImpl) defnCode) {
 
             case CONSTANT: {
                 Object value = id.getAttribute(CONSTANT_VALUE);
                 TypeSpecification type = id.getTypeSpecification();
 
                 if (value instanceof Integer) {
                     rootNode = CodeFactory.createCodeNode(INTEGER_CONSTANT);
                     rootNode.setAttribute(VALUE, value);
                 }
                 else if (value instanceof Float) {
                     rootNode = CodeFactory.createCodeNode(REAL_CONSTANT);
                     rootNode.setAttribute(VALUE, value);
                 }
                 else if (value instanceof String) {
                     rootNode = CodeFactory.createCodeNode(STRING_CONSTANT);
                     rootNode.setAttribute(VALUE, value);
                 }
 
                 id.appendLineNumber(token.getLineNumber());
                 token = nextToken();  // consume the constant identifier
 
                 if (rootNode != null) {
                     rootNode.setTypeSpecification(type);
                 }
 
                 break;
             }
 
             case ENUMERATION_CONSTANT: {
                 Object value = id.getAttribute(CONSTANT_VALUE);
                 TypeSpecification type = id.getTypeSpecification();
 
                 rootNode = CodeFactory.createCodeNode(INTEGER_CONSTANT);
                 rootNode.setAttribute(VALUE, value);
 
                 id.appendLineNumber(token.getLineNumber());
                 token = nextToken();  // consume the enum constant identifier
 
                 rootNode.setTypeSpecification(type);
                 break;
             }
 
             case FUNCTION: {
                 CallParser callParser = new CallParser(this);
                 rootNode = callParser.parse(token);
                 break;
             }
 
             default: {
                 VariableParser variableParser = new VariableParser(this);
                 rootNode = variableParser.parse(token, id);
                 break;
             }
         }
 
         return rootNode;
     }
 }
