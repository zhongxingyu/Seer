 package cs444.cfgrulesgenerator;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 import java.util.Set;
 import java.util.TreeSet;
 
 import cs444.cfgrulesgenerator.exceptions.UnexpectedTokenException;
 import cs444.cfgrulesgenerator.lexer.ILexer;
 import cs444.cfgrulesgenerator.lexer.LexerException;
 import cs444.cfgrulesgenerator.lexer.Token;
 import cs444.cfgrulesgenerator.lexer.Token.Parse;
 
 public class RulesFactory implements IRulesFactory {
     private final ILexer lexer;
     private Token currentLHS = null;
     private final Queue<Rule> buffer;
 
     public RulesFactory(ILexer lexer){
         this.lexer = lexer;
         this.buffer = new LinkedList<Rule>();
     }
 
     // return null when no more rules
     public Rule getNextRule() throws UnexpectedTokenException, LexerException, IOException{
         Rule rule = null;
 
         // TODO: check if buffer is not empty
         // extract next rule from buffer, expand many times, add them
         // to buffer
         // return expanded rule
 
         Token token = getNextRelevantToken();
 
         if (token.type == Token.Type.EOF) return null;
 
        List<Token> rightHandSide = new ArrayList<Token>();
         if(token.type == Token.Type.LHS){
             this.currentLHS = token;
            token = getNextRelevantToken(); // get first symbol of RHS
         }
 
        rightHandSide.add(token); // add first symbol of rightHandSide
         if(this.currentLHS != null){
            Rule initialRule = getNextBNFRule(rightHandSide); // add rest of symbols
             // TODO: expand initial rule until no more expansions and add new rules to buffer
             rule = initialRule;
         }else{                  // right hand side without left hand side => ERROR
             Set<String> expected = new TreeSet<String>();
             expected.add(Token.Type.LHS.toString());
             throw new UnexpectedTokenException(token, expected);
         }
         return rule;
     }
 
    private Rule getNextBNFRule(List<Token> rightHandSide) throws LexerException, IOException {
        Token token;
 
         // there is at most a rule per line
        while((token = lexer.getNextToken()).type != Token.Type.EOF &&
               token.type != Token.Type.NEWLINE){
 
            if(Token.typeToParse.get(token.type) == Parse.IGNORE) continue;

             rightHandSide.add(token);
         }
 
        return new Rule(currentLHS, rightHandSide);
     }
 
     private Token getNextRelevantToken() throws LexerException, IOException {
         Token token;
 
         // skip all whitespace and newline until first symbol
         while((token = lexer.getNextToken()).type != Token.Type.EOF &&
               (Token.typeToParse.get(token.type) == Parse.IGNORE ||
                token.type == Token.Type.NEWLINE)) ;
 
         return token;
     }
 }
