 package jlogic.read;
 
import java.util.ArrayList;
 import java.io.IOException;
 
import jlogic.*;
import jlogic.term.*;
 
 public final class Parser {
     private final Lexer lexer;
 
     private Token current;
 
     public Parser(Lexer lexer) throws ReadException, IOException {
         this.lexer = lexer;
 
         current = lexer.read();
     }
 
     public Term parseTerm() throws ReadException, IOException {
         switch (current.getType()) {
             case Identifier:
                 if (Character.isUpperCase(current.getString().charAt(0))) {
                     // TODO: Check for valid variable name
                     Term result = new Variable(current.getString());
                     advance();
                     return result;
                 } else {
                     Atom atom = parseAtom();
 
                     if (current.getType() == TokenType.LeftParen) {
                         advance();
 
                         ArrayList<Term> arguments = new ArrayList<Term>();
 
                         while (current.getType() != TokenType.RightParen) {
                             if (current.getType() == TokenType.EndOfFile)
                                 throw new ReadException(current.getLocation(), "Unexpected end of file in parameter list");
 
                             arguments.add(parseTerm());
                             if (current.getType() == TokenType.Comma)
                                 advance();
                             // TODO: This allows trailing commas
                         }
 
                         advance();
 
                         Term[] argumentArray = new Term[arguments.size()];
                         return new Structure(atom, arguments.toArray(argumentArray));
                     } else {
                         return atom;
                     }
                 }
             case Underscore:
                 return new AnonymousVariable();
             default:
                 throw new ReadException(current.getLocation(), "Expected term");
         }
     }
 
     public Atom parseAtom() throws ReadException, IOException {
         String name = current.getString();
         checkAdvance(TokenType.Identifier);
         // TODO: Check for valid atom name
 
         return new Atom(name);
     }
 
     public Structure parseStructure() throws ReadException, IOException {
         Term term = parseTerm();
         if (!(term instanceof Structure))
             throw new ReadException(current.getLocation(), "Expected structure");
 
         return (Structure) term;
     }
 
     public Rule parseRule() throws ReadException, IOException {
         Structure head = parseStructure();
 
         if (current.getType() == TokenType.Colon) {
             advanceExpect(TokenType.Hyphen);
             advance();
 
             ArrayList<Term> body = new ArrayList<Term>();
 
             while (current.getType() != TokenType.Period) {
                 if (current.getType() == TokenType.EndOfFile)
                     throw new ReadException(current.getLocation(), "Unexpected end of file in rule");
 
                 body.add(parseTerm());
                 if (current.getType() == TokenType.Comma)
                     advance();
                 // TODO: This allows trailing commas
             }
             // TODO: This allows empty bodies
 
             checkAdvance(TokenType.Period);
 
             Term[] bodyArray = new Term[body.size()];
             return new Rule(head, body.toArray(bodyArray));
         } else {
             checkAdvance(TokenType.Period);
 
             return new Rule(head);
         }
     }
 
     public Knowledge parseKnowledge() throws ReadException, IOException {
         ArrayList<Rule> rules = new ArrayList<Rule>();
 
         while (current.getType() == TokenType.Identifier)
             rules.add(parseRule());
 
         Rule[] ruleArray = new Rule[rules.size()];
        return new Knowledge(ruleArray);
     }
 
     private void advance() throws ReadException, IOException {
         current = lexer.read();
     }
 
     private void checkAdvance(TokenType currentType) throws ReadException, IOException {
         if (current.getType() != currentType)
             throw new ReadException(current.getLocation(), "Expected " + currentType.toString() + ", got " + current.getType().toString());
         advance();
     }
 
     private void advanceExpect(TokenType expectedType) throws ReadException, IOException {
         advance();
         if (current.getType() != expectedType)
             throw new ReadException(current.getLocation(), "Expected " + expectedType.toString() + ", got " + current.getType().toString());
     }
 }
