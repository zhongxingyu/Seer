 package de.skuzzle.polly.parsing;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 public class PrecedenceTable {
 
     public enum PrecedenceLevel {
         RELATION, DISJUNCTION, CONJUNCTION, EXPRESSION, TERM, FACTOR, POSTFIX, DOTDOT;
     }
     
     private Map<PrecedenceLevel, Set<TokenType>> levels;
     
     
     
     public PrecedenceTable() {
         this.levels = new HashMap<PrecedenceLevel, Set<TokenType>>();
         for (PrecedenceLevel lvl : PrecedenceLevel.values()) {
             this.levels.put(lvl, new TreeSet<TokenType>());
         }
         
         this.add(PrecedenceLevel.RELATION, TokenType.EGT);
         this.add(PrecedenceLevel.RELATION, TokenType.ELT);
         this.add(PrecedenceLevel.RELATION, TokenType.EQ);
         this.add(PrecedenceLevel.RELATION, TokenType.NEQ);
         this.add(PrecedenceLevel.RELATION, TokenType.GT);
         this.add(PrecedenceLevel.RELATION, TokenType.LT);
         
         this.add(PrecedenceLevel.DISJUNCTION, TokenType.BOOLEAN_OR);
         this.add(PrecedenceLevel.DISJUNCTION, TokenType.INT_OR);
         this.add(PrecedenceLevel.DISJUNCTION, TokenType.XOR);
         this.add(PrecedenceLevel.DISJUNCTION, TokenType.INT_XOR);
         
         this.add(PrecedenceLevel.CONJUNCTION, TokenType.BOOLEAN_AND);
         this.add(PrecedenceLevel.CONJUNCTION, TokenType.INT_AND);
         
         this.add(PrecedenceLevel.EXPRESSION, TokenType.ADD);
         this.add(PrecedenceLevel.EXPRESSION, TokenType.ADDWAVE);
         this.add(PrecedenceLevel.EXPRESSION, TokenType.SUB);
         this.add(PrecedenceLevel.EXPRESSION, TokenType.WAVE);
         this.add(PrecedenceLevel.EXPRESSION, TokenType.RIGHT_SHIFT);
         this.add(PrecedenceLevel.EXPRESSION, TokenType.URIGHT_SHIFT);
         this.add(PrecedenceLevel.EXPRESSION, TokenType.LEFT_SHIFT);
         
        // ISSUE 0000099: Add open brace and identifier to TERM precedence level so they
        //                are recognized as multiplications
        this.add(PrecedenceLevel.TERM, TokenType.OPENBR);
        this.add(PrecedenceLevel.TERM, TokenType.IDENTIFIER);
        
         this.add(PrecedenceLevel.TERM, TokenType.MUL);
         this.add(PrecedenceLevel.TERM, TokenType.DIV);
         this.add(PrecedenceLevel.TERM, TokenType.INTDIV);
         this.add(PrecedenceLevel.TERM, TokenType.MOD);
         
         this.add(PrecedenceLevel.FACTOR, TokenType.POWER);
         
         this.add(PrecedenceLevel.POSTFIX, TokenType.OPENSQBR);
         this.add(PrecedenceLevel.POSTFIX, TokenType.QUESTION);
         
         this.add(PrecedenceLevel.DOTDOT, TokenType.DOTDOT);
     }
     
     
     
     public boolean match(Token t, PrecedenceLevel level) {
         return this.levels.get(level).contains(t.getType());
     }
     
     
     public void add(PrecedenceLevel level, TokenType t) {
         this.levels.get(level).add(t);
     }
 }
