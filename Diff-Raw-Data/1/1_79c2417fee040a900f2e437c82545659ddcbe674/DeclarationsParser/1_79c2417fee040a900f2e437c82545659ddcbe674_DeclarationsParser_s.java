 package de.weltraumschaf.caythe.frontend.pascal.parsers;
 
 import de.weltraumschaf.caythe.frontend.Token;
 import de.weltraumschaf.caythe.frontend.TokenType;
 import de.weltraumschaf.caythe.frontend.pascal.PascalTokenType;
 import de.weltraumschaf.caythe.frontend.pascal.PascalTopDownParser;
 import de.weltraumschaf.caythe.intermediate.SymbolTableEntry;
 import de.weltraumschaf.caythe.intermediate.symboltableimpl.DefinitionImpl;
 import java.util.EnumSet;
 
 import static de.weltraumschaf.caythe.frontend.pascal.PascalTokenType.*;
 
 /**
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  * @license http://www.weltraumschaf.de/the-beer-ware-license.txt THE BEER-WARE LICENSE
  */
 public class DeclarationsParser extends PascalTopDownParser {
 
     public DeclarationsParser(PascalTopDownParser parent) {
         super(parent);
     }
 
     static final EnumSet<PascalTokenType> DECLARATION_START_SET =
         EnumSet.of(CONST, TYPE, VAR, PROCEDURE, FUNCTION, BEGIN);
 
     static final EnumSet<PascalTokenType> TYPE_START_SET =
         DECLARATION_START_SET.clone();
     static {
         TYPE_START_SET.remove(CONST);
     }
 
     static final EnumSet<PascalTokenType> VAR_START_SET =
         TYPE_START_SET.clone();
     static {
         VAR_START_SET.remove(TYPE);
     }
 
     static final EnumSet<PascalTokenType> ROUTINE_START_SET =
         VAR_START_SET.clone();
     static {
         ROUTINE_START_SET.remove(VAR);
     }
 
     public SymbolTableEntry parse(Token token, SymbolTableEntry parentId) throws Exception {
         token = synchronize(DECLARATION_START_SET);
 
         if (token.getType() == CONST) {
             token = nextToken();  // consume CONST
 
             ConstantDefinitionsParser constantDefinitionsParser =
                 new ConstantDefinitionsParser(this);
             constantDefinitionsParser.parse(token, null);
         }
 
         token = synchronize(TYPE_START_SET);
 
         if (token.getType() == TYPE) {
             token = nextToken();  // consume TYPE
 
             TypeDefinitionsParser typeDefinitionsParser =
                 new TypeDefinitionsParser(this);
             typeDefinitionsParser.parse(token, null);
         }
 
         token = synchronize(VAR_START_SET);
 
         if (token.getType() == VAR) {
             token = nextToken();  // consume VAR
 
             VariableDeclarationsParser variableDeclarationsParser =
                 new VariableDeclarationsParser(this);
             variableDeclarationsParser.setDefinition(DefinitionImpl.VARIABLE);
             variableDeclarationsParser.parse(token, null);
         }
 
         token = synchronize(ROUTINE_START_SET);
         TokenType tokenType = token.getType();
 
         while ((tokenType == PROCEDURE) || (tokenType == FUNCTION)) {
             DeclaredRoutineParser routineParser = new DeclaredRoutineParser(this);
             routineParser.parse(token, parentId);
 
             // Look for one or more semicolons after a definition.
             if (token.getType() == SEMICOLON) {
                 while (token.getType() == SEMICOLON) {
                     token = nextToken(); // consume the ;
                 }
             }
 
             token = synchronize(ROUTINE_START_SET);
             tokenType = token.getType();
         }
 
         return null;
     }
 
 }
