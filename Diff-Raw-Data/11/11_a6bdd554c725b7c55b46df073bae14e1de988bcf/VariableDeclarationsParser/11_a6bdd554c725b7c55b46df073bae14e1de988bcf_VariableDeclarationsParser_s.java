 package de.weltraumschaf.caythe.frontend.pascal.parsers;
 
 import de.weltraumschaf.caythe.intermediate.TypeSpecification;
 import de.weltraumschaf.caythe.intermediate.SymbolTableEntry;
 import java.util.ArrayList;
 import de.weltraumschaf.caythe.frontend.TokenType;
 import de.weltraumschaf.caythe.frontend.Token;
 import de.weltraumschaf.caythe.frontend.pascal.PascalTokenType;
 import de.weltraumschaf.caythe.frontend.pascal.PascalTopDownParser;
 import de.weltraumschaf.caythe.intermediate.Definition;
 import de.weltraumschaf.caythe.intermediate.symboltableimpl.DefinitionImpl;
 import java.util.EnumSet;
 
 import static de.weltraumschaf.caythe.frontend.pascal.PascalTokenType.*;
 import static de.weltraumschaf.caythe.frontend.pascal.PascalErrorCode.*;
 
 /**
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  * @license http://www.weltraumschaf.de/the-beer-ware-license.txt THE BEER-WARE LICENSE
  */
 public class VariableDeclarationsParser extends DeclarationsParser {
 
     private Definition definition;  // how to define the identifier
 
     public VariableDeclarationsParser(PascalTopDownParser parent) {
         super(parent);
     }
 
     public void setDefinition(Definition definition) {
         this.definition = definition;
     }
 
     // Synchronization set for a variable identifier.
     static final EnumSet<PascalTokenType> IDENTIFIER_SET =
         DeclarationsParser.VAR_START_SET.clone();
     static {
         IDENTIFIER_SET.add(IDENTIFIER);
         IDENTIFIER_SET.add(END);
         IDENTIFIER_SET.add(SEMICOLON);
     }
 
     // Synchronization set for the start of the next definition or declaration.
     static final EnumSet<PascalTokenType> NEXT_START_SET =
         DeclarationsParser.ROUTINE_START_SET.clone();
     static {
         NEXT_START_SET.add(IDENTIFIER);
         NEXT_START_SET.add(SEMICOLON);
     }
 
     @Override
     public SymbolTableEntry parse(Token token, SymbolTableEntry parentId) throws Exception {
         token = synchronize(IDENTIFIER_SET);
 
         // Loop to parse a sequence of variable declarations
         // separated by semicolons.
         while (token.getType() == IDENTIFIER) {
 
             // Parse the identifier sublist and its type specification.
             parseIdentifierSublist(token, IDENTIFIER_FOLLOW_SET, COMMA_SET);
 
             token = currentToken();
             TokenType tokenType = token.getType();
 
             // Look for one or more semicolons after a definition.
             if (tokenType == SEMICOLON) {
                 while (token.getType() == SEMICOLON) {
                     token = nextToken();  // consume the ;
                 }
             }
             // If at the start of the next definition or declaration,
             // then missing a semicolon.
            else if (NEXT_START_SET.contains(tokenType)) {
                 errorHandler.flag(token, MISSING_SEMICOLON, this);
             }
 
             token = synchronize(IDENTIFIER_SET);
         }
 
         return null;
     }
 
     // Synchronization set to start a sublist identifier.
     static final EnumSet<PascalTokenType> IDENTIFIER_START_SET =
         EnumSet.of(IDENTIFIER, COMMA);
 
     // Synchronization set to follow a sublist identifier.
     private static final EnumSet<PascalTokenType> IDENTIFIER_FOLLOW_SET =
         EnumSet.of(COLON, SEMICOLON);
     static {
         IDENTIFIER_FOLLOW_SET.addAll(DeclarationsParser.VAR_START_SET);
     }
 
     // Synchronization set for the , token.
     private static final EnumSet<PascalTokenType> COMMA_SET =
         EnumSet.of(COMMA, COLON, IDENTIFIER, SEMICOLON);
 
     protected ArrayList<SymbolTableEntry> parseIdentifierSublist(Token token, EnumSet<PascalTokenType> followSet, EnumSet<PascalTokenType> commaSet) throws Exception {
         ArrayList<SymbolTableEntry> sublist = new ArrayList<SymbolTableEntry>();
 
         do {
             token = synchronize(IDENTIFIER_START_SET);
             SymbolTableEntry id = parseIdentifier(token);
 
             if (id != null) {
                 sublist.add(id);
             }
 
             token = synchronize(commaSet);
             TokenType tokenType = token.getType();
 
             // Look for the comma.
             if (tokenType == COMMA) {
                 token = nextToken();  // consume the comma
 
                if (followSet.contains(token.getType())) {
                     errorHandler.flag(token, MISSING_IDENTIFIER, this);
                 }
             }
            else if (IDENTIFIER_START_SET.contains(tokenType)) {
                 errorHandler.flag(token, MISSING_COMMA, this);
             }
        } while (!followSet.contains(token.getType()));
 
         if (definition != DefinitionImpl.PROGRAM_PARAM) {
             // Parse the type specification.
             TypeSpecification type = parseTypeSpec(token);
 
             // Assign the type specification to each identifier in the list.
             for (SymbolTableEntry variableId : sublist) {
                 variableId.setTypeSpecification(type);
             }
         }
 
         return sublist;
     }
 
     private SymbolTableEntry parseIdentifier(Token token) throws Exception {
         SymbolTableEntry id = null;
 
         if (token.getType() == IDENTIFIER) {
             String name = token.getText().toLowerCase();
             id = symbolTableStack.lookupLocal(name);
 
             // Enter a new identifier into the symbol table.
             if (id == null) {
                 id = symbolTableStack.enterLocal(name);
                 id.setDefinition(definition);
                 id.appendLineNumber(token.getLineNumber());
             }
             else {
                 errorHandler.flag(token, IDENTIFIER_REDEFINED, this);
             }
 
             token = nextToken();   // consume the identifier token
         }
         else {
             errorHandler.flag(token, MISSING_IDENTIFIER, this);
         }
 
         return id;
     }
 
     // Synchronization set for the : token.
     private static final EnumSet<PascalTokenType> COLON_SET =
         EnumSet.of(COLON, SEMICOLON);
 
     protected TypeSpecification parseTypeSpec(Token token) throws Exception {
         // Synchronize on the : token.
         token = synchronize(COLON_SET);
 
         if (token.getType() == COLON) {
             token = nextToken(); // consume the :
         }
         else {
             errorHandler.flag(token, MISSING_COLON, this);
         }
 
         // Parse the type specification.
         TypeSpecificationParser typeSpecificationParser = new TypeSpecificationParser(this);
         TypeSpecification type = typeSpecificationParser.parse(token);
 
         return type;
     }
 
 }
