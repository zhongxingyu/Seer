 package de.skuzzle.polly.parsing;
 
 import java.util.EnumMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 /**
  * This class maps token types which represent operators to a certain precedence level.
  * 
  * @author Simon Taddiken
  */
 public class PrecedenceTable {
 
     public enum PrecedenceLevel {
         RELATION, DISJUNCTION, CONJUNCTION, SECTERM, TERM, FACTOR, POSTFIX, DOTDOT, UNARY;
     }
     
     private Map<PrecedenceLevel, Set<TokenType>> levels;
     
     
     
     public PrecedenceTable() {
         this.levels = new EnumMap<PrecedenceLevel, Set<TokenType>>(PrecedenceLevel.class);
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
         
         this.add(PrecedenceLevel.CONJUNCTION, TokenType.BOOLEAN_AND);
         this.add(PrecedenceLevel.CONJUNCTION, TokenType.INT_AND);
         
         this.add(PrecedenceLevel.SECTERM, TokenType.ADD);
         this.add(PrecedenceLevel.SECTERM, TokenType.ADDWAVE);
         this.add(PrecedenceLevel.SECTERM, TokenType.SUB);
         this.add(PrecedenceLevel.SECTERM, TokenType.WAVE);
         this.add(PrecedenceLevel.SECTERM, TokenType.GT); // ISSUE #12: parser makes effort to recognize a rightshift here
        this.add(PrecedenceLevel.SECTERM, TokenType.URIGHT_SHIFT);
         this.add(PrecedenceLevel.SECTERM, TokenType.LEFT_SHIFT);
         
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
         this.add(PrecedenceLevel.POSTFIX, TokenType.QUEST_EXCALAMTION);
         
         this.add(PrecedenceLevel.DOTDOT, TokenType.DOTDOT);
         
         this.add(PrecedenceLevel.UNARY, TokenType.SUB);
         this.add(PrecedenceLevel.UNARY, TokenType.EXCLAMATION);
     }
     
     
     
     public boolean match(Token t, PrecedenceLevel level) {
         return this.levels.get(level).contains(t.getType());
     }
     
     
     public void add(PrecedenceLevel level, TokenType t) {
         this.levels.get(level).add(t);
     }
 }
