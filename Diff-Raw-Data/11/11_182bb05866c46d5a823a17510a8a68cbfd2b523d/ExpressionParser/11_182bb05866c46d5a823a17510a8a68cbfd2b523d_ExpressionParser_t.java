 package wci.frontend.pascal.parsers;
 
 import java.util.EnumSet;
 import java.util.HashMap;
 import java.util.HashSet;
 
 import wci.frontend.*;
 import wci.frontend.pascal.*;
 import wci.intermediate.symtabimpl.*;
 import wci.intermediate.*;
 import wci.intermediate.icodeimpl.*;
 import wci.intermediate.typeimpl.*;
 import static wci.frontend.pascal.PascalTokenType.*;
 import static wci.frontend.pascal.PascalErrorCode.*;
 import static wci.intermediate.symtabimpl.SymTabKeyImpl.*;
 import static wci.intermediate.symtabimpl.DefinitionImpl.*;
 import static wci.intermediate.typeimpl.TypeFormImpl.ENUMERATION;
 import static wci.intermediate.typeimpl.TypeFormImpl.SUBRANGE;
 import static wci.intermediate.typeimpl.TypeKeyImpl.*;
 import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.*;
 import static wci.intermediate.icodeimpl.ICodeKeyImpl.*;
 
 
 /**
  * <h1>ExpressionParser</h1>
  *
  * <p>Parse a Pascal expression.</p>
  *
  * <p>Copyright (c) 2009 by Ronald Mak</p>
  * <p>For instructional purposes only.  No warranties.</p>
  */
 public class ExpressionParser extends StatementParser
 {
     /**
      * Constructor.
      * @param parent the parent parser.
      */
     public ExpressionParser(PascalParserTD parent)
     {
         super(parent);
     }
 
     // Synchronization set for starting an expression.
     static final EnumSet<PascalTokenType> EXPR_START_SET =
         EnumSet.of(PLUS, MINUS, IDENTIFIER, INTEGER, REAL, STRING,
                    PascalTokenType.NOT, LEFT_PAREN);
 
     /**
      * Parse an expression.
      * @param token the initial token.
      * @return the root node of the generated parse tree.
      * @throws Exception if an error occurred.
      */
     public ICodeNode parse(Token token)
         throws Exception
     {
         return parseExpression(token);
     }
 
     // Set of relational operators.
     private static final EnumSet<PascalTokenType> REL_OPS =
         EnumSet.of(EQUALS, NOT_EQUALS, LESS_THAN, LESS_EQUALS,
                    GREATER_THAN, GREATER_EQUALS);
 
     // Map relational operator tokens to node types.
     private static final HashMap<PascalTokenType, ICodeNodeType>
         REL_OPS_MAP = new HashMap<PascalTokenType, ICodeNodeType>();
     static {
         REL_OPS_MAP.put(EQUALS, EQ);
         REL_OPS_MAP.put(NOT_EQUALS, NE);
         REL_OPS_MAP.put(LESS_THAN, LT);
         REL_OPS_MAP.put(LESS_EQUALS, LE);
         REL_OPS_MAP.put(GREATER_THAN, GT);
         REL_OPS_MAP.put(GREATER_EQUALS, GE);
     };
     
     
     private static final HashSet<TypeSpec> VALID_SET_ELEMENT_TYPES = new HashSet<>();
     static {
         VALID_SET_ELEMENT_TYPES.add(Predefined.booleanType);
         VALID_SET_ELEMENT_TYPES.add(Predefined.integerType);
         VALID_SET_ELEMENT_TYPES.add(Predefined.charType);
     }
 
     
     
     
     
     
     
     
 
     /**
      * Parse an expression.
      * @param token the initial token.
      * @return the root node of the generated parse tree.
      * @throws Exception if an error occurred.
      */
     private ICodeNode parseExpression(Token token)
         throws Exception
     {
         // Parse a simple expression and make the root of its tree
         // the root node.
         ICodeNode rootNode = parseSimpleExpression(token);
         TypeSpec resultType = rootNode != null ? rootNode.getTypeSpec()
                                                : Predefined.undefinedType;
 
         token = currentToken();
         TokenType tokenType = token.getType();
 
         // Look for a relational operator.
         if (REL_OPS.contains(tokenType)) {
 
             // Create a new operator node and adopt the current tree
             // as its first child.
             ICodeNodeType nodeType = REL_OPS_MAP.get(tokenType);
             ICodeNode opNode = ICodeFactory.createICodeNode(nodeType);
             opNode.addChild(rootNode);
 
             token = nextToken();  // consume the operator
 
             // Parse the second simple expression.  The operator node adopts
             // the simple expression's tree as its second child.
             ICodeNode simExprNode = parseSimpleExpression(token);
             opNode.addChild(simExprNode);
 
             // The operator node becomes the new root node.
             rootNode = opNode;
 
             // Type check: The operands must be comparison compatible.
             TypeSpec simExprType = simExprNode != null
                                        ? simExprNode.getTypeSpec()
                                        : Predefined.undefinedType;
             if (TypeChecker.areComparisonCompatible(resultType, simExprType)) {
                 resultType = Predefined.booleanType;
             }
             else {
                 errorHandler.flag(token, INCOMPATIBLE_TYPES, this);
                 resultType = Predefined.undefinedType;
             }
             
             rootNode.setTypeSpec(resultType);
         }
         
         
         else if (tokenType == DOT_DOT) {
         	rootNode = parseRestOfSubrange(token, rootNode);
         }
         
 
 
 
         return rootNode;
     }
     
     
     /**
      * Check a value of a type specification.
      * @param token the current token.
      * @param value the value.
      * @param type the type specifiction.
      * @return the value.
      */
     private Object checkValueAndType(Token token, ICodeNode subrangeOperand)
     {
     	TypeSpec type = subrangeOperand.getTypeSpec();
     	Object value = subrangeOperand.getAttribute(VALUE);
     	
     	if (type == null) {
             return value;
         }
     	else if (type == Predefined.integerType) {
             return value;
         }
         else if (type == Predefined.charType) {
             char ch = ((String) value).charAt(0);
             return Character.getNumericValue(ch);
         }
         else if (type.getForm() == ENUMERATION) {
             return value;
         }
         else {
             errorHandler.flag(token, INVALID_SUBRANGE_TYPE, this);
             return value;
         }
     }
     
 
     private ICodeNode parseRestOfSubrange(Token firstTokenOfMinValueNode, ICodeNode minValueNode)
             throws Exception
     {
     	TypeSpec minType = minValueNode.getTypeSpec();
     	Object minValue = checkValueAndType(firstTokenOfMinValueNode, minValueNode);
     	
     	
     	Token firstTokenOfMaxValueNode = nextToken(); //consume the ..
     	ICodeNode maxValueNode = parseSimpleExpression(firstTokenOfMaxValueNode);
     	TypeSpec maxType = maxValueNode.getTypeSpec();
     	Object maxValue = checkValueAndType(firstTokenOfMaxValueNode, maxValueNode);
     	
     	//assume undefined type(assume an error condition) for the moment
     	TypeSpec subrangeBaseType = Predefined.undefinedType;
     	
         // Are the min and max value types valid?
         if (minType == null) {
             errorHandler.flag(firstTokenOfMinValueNode, INCOMPATIBLE_TYPES, this);
         }
         
         else if (maxType == null) {
             errorHandler.flag(firstTokenOfMaxValueNode, INCOMPATIBLE_TYPES, this);
         }
 
         // Are the min and max value types the same?
         else if (minType != maxType) {
             errorHandler.flag(firstTokenOfMaxValueNode, INVALID_SUBRANGE_TYPE, this);
         }
 
         // anything but false..true is invalid
         else if (minType == Predefined.booleanType && !(!(Boolean) minValue && (Boolean) maxValue)) {
             errorHandler.flag(firstTokenOfMaxValueNode, MIN_GT_MAX, this);
         }
         
         // Min value > max value? this should cover ints, enum values, and chars 
         else if ((minValue != null) && (maxValue != null) &&
                  ((Integer) minValue >= (Integer) maxValue)) {
             errorHandler.flag(firstTokenOfMaxValueNode, MIN_GT_MAX, this);
         }
         
         else {
         	//both types are the same and both valid, so adopt one of their types
         	subrangeBaseType = minType;
         }
         
         
         TypeSpec subrangeTypeSpec = TypeFactory.createType(SUBRANGE);
         subrangeTypeSpec.setAttribute(SUBRANGE_BASE_TYPE, subrangeBaseType);
         subrangeTypeSpec.setAttribute(SUBRANGE_MIN_VALUE, minValue);
         subrangeTypeSpec.setAttribute(SUBRANGE_MAX_VALUE, maxValue);
 
         ICodeNode rangeNode = ICodeFactory.createICodeNode(RANGE);
         rangeNode.addChild(minValueNode);
         rangeNode.addChild(maxValueNode);
         rangeNode.setTypeSpec(subrangeTypeSpec);
         
 
     	return rangeNode;
     	
     }
     
     /**
      * Parse a set expression.
      * @param token the initial token.
      * @return the root of the generated parse subtree
      * @throws Exception if an error occurred
      */
     private ICodeNode parseSetExpression(Token token) throws Exception
     {   
     	// Create the rootNode with type SET, and adopt a blank SET_VALUES node
         ICodeNode rootNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.SET);
         ICodeNode valuesNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.SET_VALUES);
         
         //the VALUE node of the set needs to be a list of ICodeNodes because there could be variables that can only be evaluated at runtime.
         HashSet<ICodeNode> valuesSet = new HashSet<>();
         valuesNode.setAttribute(VALUE, valuesSet);
         rootNode.addChild(valuesNode);
         
         
         /**
          * The strategy is to assume the comma separated elements in a set literal like [2+4, myvar, 4+5..50, 4<6]
          * are full blown expressions because lazarus seems to allow it. So, we will just loop and defer the chunks of
          * work to parseExpression().
          * 
          * Any undefined types encountered will be flagged, and wont participate in the set.
          * 
          * We will assume the first non undefinedType value in the set determines the type of values in the entire set, 
          * although we will check subsequent values, skipping non matching types. So, [5, 'a', true] 
          * would throw errors, and produce [5].
          * 
          */
         
         HashSet<TypeSpec> setLiterals = new HashSet<>();
         setLiterals.add(Predefined.booleanType);
         setLiterals.add(Predefined.integerType);
         setLiterals.add(Predefined.charType);
 
         TypeSpec setElementTypeSpec = Predefined.undefinedType;
         boolean setTypeEstablished = false;
         while (EXPR_START_SET.contains(token.getType()))
         {
             ICodeNode setValueNode = parseExpression(token);
             TypeSpec t = setValueNode.getTypeSpec();
             TypeSpec baseType = t.baseType();
             
             //if the type COULD be a set element in some pascal set. 
             //eg an int is valid, although our set may be char. well do the homogenous check later.
             boolean validSetElementType = setLiterals.contains(t) 
             		                || (t.getForm() == SUBRANGE && setLiterals.contains(t.baseType()));
         	
             
             if (validSetElementType) {
         		if (!setTypeEstablished) {
         			setElementTypeSpec = t.baseType();
         			setTypeEstablished = true;
         		}
         		
         		//make sure this element is homogenous with the rest of the set
         		if (setElementTypeSpec == t.baseType()) {
         			addToValuesUnlessCompileTimeDuplicate(setValueNode, valuesSet);
         		} else {
         			//this was a valid type of set element, but just isnt homogenous with the other set members.
                 	errorHandler.flag(token, INVALID_SET_ELEMENT_TYPE, this);
         		}
             } else {
             	///this type can never be in any kind of pascal set
             	errorHandler.flag(token, INVALID_SET_ELEMENT_TYPE, this);
             }
         	
 
             token = currentToken();
         	if (token.getType() == COMMA) {
         		token = nextToken();
         	}
             
         }
         
         TypeSpec typeSpec = TypeFactory.createType(TypeFormImpl.SET);
         typeSpec.setAttribute(SET_ELEMENT_TYPE, setElementTypeSpec);
         rootNode.setTypeSpec(typeSpec);
           
 
         return rootNode;
     }
     
     private void addToValuesUnlessCompileTimeDuplicate(ICodeNode setValueNode, HashSet<ICodeNode> valuesSet) {
     	TypeForm form = setValueNode.getTypeSpec().getForm();
     	switch ((TypeFormImpl) form) {
     	
     	case SCALAR:
     		/*intentional fallthrough*/
     	case ENUMERATION:
     		
     		
     		break;
     		
     	case SUBRANGE:
     		break;
 
     	}
 
     }
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
 
     // Set of additive operators.
     private static final EnumSet<PascalTokenType> ADD_OPS =
         EnumSet.of(PLUS, MINUS, PascalTokenType.OR);
 
     // Map additive operator tokens to node types.
     private static final HashMap<PascalTokenType, ICodeNodeTypeImpl>
         ADD_OPS_OPS_MAP = new HashMap<PascalTokenType, ICodeNodeTypeImpl>();
     static {
         ADD_OPS_OPS_MAP.put(PLUS, ADD);
         ADD_OPS_OPS_MAP.put(MINUS, SUBTRACT);
         ADD_OPS_OPS_MAP.put(PascalTokenType.OR, ICodeNodeTypeImpl.OR);
     };
 
     /**
      * Parse a simple expression.
      * @param token the initial token.
      * @return the root node of the generated parse tree.
      * @throws Exception if an error occurred.
      */
     private ICodeNode parseSimpleExpression(Token token)
         throws Exception
     {
         Token signToken = null;
         TokenType signType = null;  // type of leading sign (if any)
 
         // Look for a leading + or - sign.
         TokenType tokenType = token.getType();
         if ((tokenType == PLUS) || (tokenType == MINUS)) {
             signType = tokenType;
             signToken = token;
             token = nextToken();  // consume the + or -
         }
 
         // Parse a term and make the root of its tree the root node.
         ICodeNode rootNode = parseTerm(token);
         TypeSpec resultType = rootNode != null ? rootNode.getTypeSpec()
                                                : Predefined.undefinedType;
 
         // Type check: Leading sign.
         if ((signType != null) && (!TypeChecker.isIntegerOrReal(resultType))) {
             errorHandler.flag(signToken, INCOMPATIBLE_TYPES, this);
         }
 
         // Was there a leading - sign?
         if (signType == MINUS) {
 
             // Create a NEGATE node and adopt the current tree
             // as its child. The NEGATE node becomes the new root node.
             ICodeNode negateNode = ICodeFactory.createICodeNode(NEGATE);
             negateNode.addChild(rootNode);
             negateNode.setTypeSpec(rootNode.getTypeSpec());
             rootNode = negateNode;
         }
 
         token = currentToken();
         tokenType = token.getType();
 
        // tokenType should be in ADD_OPS, MULT_OPS or .. , ; ] )
        // so assume there was supposed to be a comma here in the likely case of a set
        if (tokenType == INTEGER)
            errorHandler.flag(token, MISSING_COMMA, this);
        
         // Loop over additive operators.
         while (ADD_OPS.contains(tokenType)) {
             TokenType operator = tokenType;
 
             // Create a new operator node and adopt the current tree
             // as its first child.
             ICodeNodeType nodeType = ADD_OPS_OPS_MAP.get(operator);
             ICodeNode opNode = ICodeFactory.createICodeNode(nodeType);
             opNode.addChild(rootNode);
 
             token = nextToken();  // consume the operator
 
             // Parse another term.  The operator node adopts
             // the term's tree as its second child.
             ICodeNode termNode = parseTerm(token);
             opNode.addChild(termNode);
             TypeSpec termType = termNode != null ? termNode.getTypeSpec()
                                                  : Predefined.undefinedType;
 
             // The operator node becomes the new root node.
             rootNode = opNode;
 
             // Determine the result type.
             switch ((PascalTokenType) operator) {
 
                 case PLUS:
                 case MINUS: {
                     // Both operands integer ==> integer result.
                     if (TypeChecker.areBothInteger(resultType, termType)) {
                         resultType = Predefined.integerType;
                     }
 
                     // Both real operands or one real and one integer operand
                     // ==> real result.
                     else if (TypeChecker.isAtLeastOneReal(resultType,
                                                           termType)) {
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
                     }
                     else {
                         errorHandler.flag(token, INCOMPATIBLE_TYPES, this);
                     }
 
                     break;
                 }
             }
 
             rootNode.setTypeSpec(resultType);
 
             token = currentToken();
             tokenType = token.getType();
         }
 
         return rootNode;
     }
 
     // Set of multiplicative operators.
     private static final EnumSet<PascalTokenType> MULT_OPS =
         EnumSet.of(STAR, SLASH, DIV, PascalTokenType.MOD, PascalTokenType.AND);
 
     // Map multiplicative operator tokens to node types.
     private static final HashMap<PascalTokenType, ICodeNodeType>
         MULT_OPS_OPS_MAP = new HashMap<PascalTokenType, ICodeNodeType>();
     static {
         MULT_OPS_OPS_MAP.put(STAR, MULTIPLY);
         MULT_OPS_OPS_MAP.put(SLASH, FLOAT_DIVIDE);
         MULT_OPS_OPS_MAP.put(DIV, INTEGER_DIVIDE);
         MULT_OPS_OPS_MAP.put(PascalTokenType.MOD, ICodeNodeTypeImpl.MOD);
         MULT_OPS_OPS_MAP.put(PascalTokenType.AND, ICodeNodeTypeImpl.AND);
     };
 
     /**
      * Parse a term.
      * @param token the initial token.
      * @return the root node of the generated parse tree.
      * @throws Exception if an error occurred.
      */
     private ICodeNode parseTerm(Token token)
         throws Exception
     {
         // Parse a factor and make its node the root node.
         ICodeNode rootNode = parseFactor(token);
         TypeSpec resultType = rootNode != null ? rootNode.getTypeSpec()
                                                : Predefined.undefinedType;
        
         token = currentToken();
         TokenType tokenType = token.getType();
        
         // Loop over multiplicative operators.
         while (MULT_OPS.contains(tokenType)) {
             TokenType operator = tokenType;
 
             // Create a new operator node and adopt the current tree
             // as its first child.
             ICodeNodeType nodeType = MULT_OPS_OPS_MAP.get(operator);
             ICodeNode opNode = ICodeFactory.createICodeNode(nodeType);
             opNode.addChild(rootNode);
 
             token = nextToken();  // consume the operator
 
             // Parse another factor.  The operator node adopts
             // the term's tree as its second child.
             ICodeNode factorNode = parseFactor(token);
             opNode.addChild(factorNode);
             TypeSpec factorType = factorNode != null ? factorNode.getTypeSpec()
                                                      : Predefined.undefinedType;
 
             // The operator node becomes the new root node.
             rootNode = opNode;
 
             // Determine the result type.
             switch ((PascalTokenType) operator) {
 
                 case STAR: {
                     // Both operands integer ==> integer result.
                     if (TypeChecker.areBothInteger(resultType, factorType)) {
                         resultType = Predefined.integerType;
                     }
 
                     // Both real operands or one real and one integer operand
                     // ==> real result.
                     else if (TypeChecker.isAtLeastOneReal(resultType,
                                                           factorType)) {
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
                     if (TypeChecker.areBothInteger(resultType, factorType) ||
                         TypeChecker.isAtLeastOneReal(resultType, factorType))
                     {
                         resultType = Predefined.realType;
                     }
                     else {
                         errorHandler.flag(token, INCOMPATIBLE_TYPES, this);
                     }
 
                     break;
                 }
 
                 case DIV:
                 case MOD: {
                     // Both operands integer ==> integer result.
                     if (TypeChecker.areBothInteger(resultType, factorType)) {
                         resultType = Predefined.integerType;
                     }
                     else {
                         errorHandler.flag(token, INCOMPATIBLE_TYPES, this);
                     }
 
                     break;
                 }
 
                 case AND: {
                     // Both operands boolean ==> boolean result.
                     if (TypeChecker.areBothBoolean(resultType, factorType)) {
                         resultType = Predefined.booleanType;
                     }
                     else {
                         errorHandler.flag(token, INCOMPATIBLE_TYPES, this);
                     }
 
                     break;
                 }
             }
 
             rootNode.setTypeSpec(resultType);
 
             token = currentToken();
             tokenType = token.getType();
         }
 
         return rootNode;
     }
 
     /**
      * Parse a factor.
      * @param token the initial token.
      * @return the root node of the generated parse tree.
      * @throws Exception if an error occurred.
      */
     private ICodeNode parseFactor(Token token)
         throws Exception
     {
         TokenType tokenType = token.getType();
         ICodeNode rootNode = null;
 
         switch ((PascalTokenType) tokenType) {
 
             case IDENTIFIER: {
                 return parseIdentifier(token);
             }
 
             case INTEGER: {
                 // Create an INTEGER_CONSTANT node as the root node.
                 rootNode = ICodeFactory.createICodeNode(INTEGER_CONSTANT);
                 rootNode.setAttribute(VALUE, token.getValue());
 
                 token = nextToken();  // consume the number
 
                 rootNode.setTypeSpec(Predefined.integerType);
                 break;
             }
 
             case REAL: {
                 // Create an REAL_CONSTANT node as the root node.
                 rootNode = ICodeFactory.createICodeNode(REAL_CONSTANT);
                 rootNode.setAttribute(VALUE, token.getValue());
 
                 token = nextToken();  // consume the number
 
                 rootNode.setTypeSpec(Predefined.realType);
                 break;
             }
 
             case STRING: {
                 String value = (String) token.getValue();
 
                 // Create a STRING_CONSTANT node as the root node.
                 rootNode = ICodeFactory.createICodeNode(STRING_CONSTANT);
                 rootNode.setAttribute(VALUE, value);
 
                 TypeSpec resultType = value.length() == 1
                                           ? Predefined.charType
                                           : TypeFactory.createStringType(value);
 
                 token = nextToken();  // consume the string
 
                 rootNode.setTypeSpec(resultType);
                 break;
             }
 
             case NOT: {
                 token = nextToken();  // consume the NOT
 
                 // Create a NOT node as the root node.
                 rootNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.NOT);
 
                 // Parse the factor.  The NOT node adopts the
                 // factor node as its child.
                 ICodeNode factorNode = parseFactor(token);
                 rootNode.addChild(factorNode);
 
                 // Type check: The factor must be boolean.
                 TypeSpec factorType = factorNode != null
                                           ? factorNode.getTypeSpec()
                                           : Predefined.undefinedType;
                 if (!TypeChecker.isBoolean(factorType)) {
                     errorHandler.flag(token, INCOMPATIBLE_TYPES, this);
                 }
 
                 rootNode.setTypeSpec(Predefined.booleanType);
                 break;
             }
 
             case LEFT_PAREN: {
                 token = nextToken();      // consume the (
 
                 // Parse an expression and make its node the root node.
                 rootNode = parseExpression(token);
                 TypeSpec resultType = rootNode != null
                                           ? rootNode.getTypeSpec()
                                           : Predefined.undefinedType;
 
                 // Look for the matching ) token.
                 token = currentToken();
                 if (token.getType() == RIGHT_PAREN) {
                     token = nextToken();  // consume the )
                 }
                 else {
                     errorHandler.flag(token, MISSING_RIGHT_PAREN, this);
                 }
 
                 rootNode.setTypeSpec(resultType);
                 break;
             }
             
             case LEFT_BRACKET: {
                 token = nextToken(); // consume the [
                 
                 // parse the set expression and make its node the root node
                 rootNode = parseSetExpression(token);
                 
                 // look for matching ] token
                 token = currentToken();
                 if (token.getType() == RIGHT_BRACKET)
                     token = nextToken(); // consume the ]
                 break;
             }
 
             default: {
                 errorHandler.flag(token, UNEXPECTED_TOKEN, this);
             }
         }
 
         return rootNode;
     }
 
     /**
      * Parse an identifier.
      * @param token the current token.
      * @return the root node of the generated parse tree.
      * @throws Exception if an error occurred.
      */
     private ICodeNode parseIdentifier(Token token)
         throws Exception
     {
         ICodeNode rootNode = null;
 
         // Look up the identifier in the symbol table stack.
         String name = token.getText().toLowerCase();
         SymTabEntry id = symTabStack.lookup(name);
 
         // Undefined.
         if (id == null) {
             errorHandler.flag(token, IDENTIFIER_UNDEFINED, this);
             id = symTabStack.enterLocal(name);
             id.setDefinition(UNDEFINED);
             id.setTypeSpec(Predefined.undefinedType);
         }
 
         Definition defnCode = id.getDefinition();
 
         switch ((DefinitionImpl) defnCode) {
 
             case CONSTANT: {
                 Object value = id.getAttribute(CONSTANT_VALUE);
                 TypeSpec type = id.getTypeSpec();
 
                 if (value instanceof Integer) {
                     rootNode = ICodeFactory.createICodeNode(INTEGER_CONSTANT);
                     rootNode.setAttribute(VALUE, value);
                 }
                 else if (value instanceof Float) {
                     rootNode = ICodeFactory.createICodeNode(REAL_CONSTANT);
                     rootNode.setAttribute(VALUE, value);
                 }
                 else if (value instanceof String) {
                     rootNode = ICodeFactory.createICodeNode(STRING_CONSTANT);
                     rootNode.setAttribute(VALUE, value);
                 }
 
                 id.appendLineNumber(token.getLineNumber());
                 token = nextToken();  // consume the constant identifier
 
                 if (rootNode != null) {
                     rootNode.setTypeSpec(type);
                 }
 
                 break;
             }
 
             case ENUMERATION_CONSTANT: {
                 Object value = id.getAttribute(CONSTANT_VALUE);
                 TypeSpec type = id.getTypeSpec();
 
                 rootNode = ICodeFactory.createICodeNode(INTEGER_CONSTANT);
                 rootNode.setAttribute(VALUE, value);
 
                 id.appendLineNumber(token.getLineNumber());
                 token = nextToken();  // consume the enum constant identifier
 
                 rootNode.setTypeSpec(type);
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
