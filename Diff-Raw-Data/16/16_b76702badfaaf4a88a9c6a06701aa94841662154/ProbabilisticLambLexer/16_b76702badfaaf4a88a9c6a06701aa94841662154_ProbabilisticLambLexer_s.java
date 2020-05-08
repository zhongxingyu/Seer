 /*
  * ModelCC, under ModelCC Shared Software License, www.modelcc.org. Luis Quesada Torres.
  */
 
 
 package org.modelcc.lexer.lamb.adapter;
 
 import org.modelcc.lexer.lamb.*;
 import java.io.Reader;
 import java.io.Serializable;
 import java.util.Set;
 import org.modelcc.language.lexis.LexicalSpecification;
 import org.modelcc.lexer.Lexer;
 import org.modelcc.lexer.recognizer.PatternRecognizer;
 
 /**
 * Lamb - SyntaxGraphLexer with AMBiguity Support.
  * @author elezeta
  * @serial
  */
 public final class ProbabilisticLambLexer extends LambLexer implements Serializable {
 
     /**
      * Serial Version ID
      */
     private static final long serialVersionUID = 31415926535897932L;
 
     /**
      * Constructor.
      * @param ls the lexical specification.
      */
     public ProbabilisticLambLexer(LexicalSpecification ls) {
         this.ls = ls;
         this.ignore = null;
     }
     
     /**
      * Constructor.
      * @param ls the lexical specification.
      * @param ignore the ignore pattern set.
      */
     public ProbabilisticLambLexer(LexicalSpecification ls,Set<PatternRecognizer> ignore) {
         this.ls = ls;
         this.ignore = ignore;
     }
     
     /**
      * Scans an input string.
      * @param input the input string.
      * @return the obtained lexical graph.
      */    
     @Override
     public LexicalGraph scan(Reader input) {
         ProbabilisticLamb gl = new ProbabilisticLamb();
         LexicalGraph sg = gl.scan(ls,ignore,input);
         return sg;
     }
 
 }
