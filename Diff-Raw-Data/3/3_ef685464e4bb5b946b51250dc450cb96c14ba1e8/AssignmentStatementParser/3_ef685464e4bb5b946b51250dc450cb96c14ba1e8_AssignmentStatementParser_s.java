 package de.weltraumschaf.caythe.frontend.pascal.parsers;
 
 import de.weltraumschaf.caythe.intermediate.TypeSpecification;
 import de.weltraumschaf.caythe.intermediate.SymbolTableEntry;
 import de.weltraumschaf.caythe.frontend.Token;
 import de.weltraumschaf.caythe.frontend.pascal.PascalTokenType;
 import de.weltraumschaf.caythe.frontend.pascal.PascalTopDownParser;
 import de.weltraumschaf.caythe.intermediate.CodeFactory;
 import de.weltraumschaf.caythe.intermediate.CodeNode;
 
 import de.weltraumschaf.caythe.intermediate.symboltableimpl.Predefined;
 import de.weltraumschaf.caythe.intermediate.typeimpl.TypeChecker;
 import java.util.EnumSet;
 import static de.weltraumschaf.caythe.intermediate.codeimpl.CodeNodeTypeImpl.*;
 import static de.weltraumschaf.caythe.intermediate.codeimpl.CodeKeyImpl.*;
 import static de.weltraumschaf.caythe.frontend.pascal.PascalTokenType.*;
 import static de.weltraumschaf.caythe.frontend.pascal.PascalErrorCode.*;
 
 /**
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  * @license http://www.weltraumschaf.de/the-beer-ware-license.txt THE BEER-WARE LICENSE
  */
 public class AssignmentStatementParser extends StatementParser {
 
     public AssignmentStatementParser(PascalTopDownParser parent) {
         super(parent);
     }
 
     private static final EnumSet<PascalTokenType> COLON_EQUALS_SET =
             ExpressionParser.EXPRESSION_START_SET.clone();
 
     static {

     }
 
     @Override
     public CodeNode parse(Token token) throws Exception {
         // Create the ASSIGN node.
         CodeNode assignNode = CodeFactory.createCodeNode(ASSIGN);
         // Parse the target variable.
         VariableParser variableParser = new VariableParser(this);
         CodeNode targetNode = variableParser.parse(token);
         TypeSpecification targetType = targetNode != null
                 ? targetNode.getTypeSpecification()
                 : Predefined.undefinedType;
         // The ASSIGN node adopts the variable node as its first child.
         assignNode.addChild(targetNode);
         // Synchronize on the := token.
         token = synchronize(COLON_EQUALS_SET);
 
         if (token.getType() == COLON_EQUALS) {
             token = nextToken();  // consume the :=
         }
         else {
             errorHandler.flag(token, MISSING_COLON_EQUALS, this);
         }
 
         // Parse the expression. The ASSIGN node adopts the expression's
         // node as its second child.
         ExpressionParser expressionParser = new ExpressionParser(this);
         CodeNode exprNode = expressionParser.parse(token);
         assignNode.addChild(exprNode);
 
         // Type check: Assignement compatible?
         TypeSpecification exprType = exprNode != null
                 ? exprNode.getTypeSpecification()
                 : Predefined.undefinedType;
 
         if (!TypeChecker.areAssignmentCompatible(targetType, exprType)) {
             errorHandler.flag(token, INCOMPATIBLE_TYPES, this);
         }
 
         assignNode.setTypeSpecification(targetType);
         return assignNode;
     }
 
 }
