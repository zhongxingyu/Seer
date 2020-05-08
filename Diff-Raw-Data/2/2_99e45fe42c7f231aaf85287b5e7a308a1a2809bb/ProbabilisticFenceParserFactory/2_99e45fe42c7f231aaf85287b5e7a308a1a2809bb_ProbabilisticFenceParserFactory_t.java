 /*
  * ModelCC, under ModelCC Shared Software License, www.modelcc.org. Luis Quesada Torres.
  */
 
 
 package org.modelcc.parser.fence.adapter;
 
 import java.io.Serializable;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.modelcc.language.LanguageSpecification;
 import org.modelcc.language.factory.CannotGenerateLanguageSpecificationException;
 import org.modelcc.metamodel.BasicModelElement;
 import org.modelcc.metamodel.ChoiceModelElement;
 import org.modelcc.metamodel.ComplexModelElement;
 import org.modelcc.metamodel.Model;
 import org.modelcc.metamodel.ModelElement;
 import org.modelcc.parser.fence.Fence;
 import org.modelcc.parser.fence.ProbabilisticFence;
 import org.modelcc.language.factory.LanguageSpecificationFactory;
 import org.modelcc.lexer.Lexer;
 import org.modelcc.lexer.lamb.ProbabilisticLamb;
 import org.modelcc.lexer.lamb.adapter.LambLexer;
 import org.modelcc.lexer.recognizer.PatternRecognizer;
 import org.modelcc.parser.CannotCreateParserException;
 import org.modelcc.parser.ParserFactory;
 
 /**
  * ModelCC Probabilistic Fence Parser Generator
  * @author elezeta
  * @serial
  */
 public final class ProbabilisticFenceParserFactory extends FenceParserFactory implements Serializable {
 
     /**
      * Serial Version ID
      */
     private static final long serialVersionUID = 31415926535897932L;
 
     /**
      * Creates a probabilistic parser
      * @param m the model
      * @param lexer the lexer
      * @return the parser
      * @throws CannotCreateParserException
      */
     public static ProbabilisticFenceParser create(Model m,Lexer lexer) throws CannotCreateParserException {
         try {
             //Type erasure does not allow comparing the generated parser with a specific parser type. Check http://serdom.eu/ser/2007/03/25/java-generics-instantiating-objects-of-type-parameter-without-using-class-literal
             LanguageSpecificationFactory lsf = new LanguageSpecificationFactory();
             LanguageSpecification ls = lsf.create(m);
             ProbabilisticFence gp = new ProbabilisticFence();
 
             ProbabilisticFenceParser parser = new ProbabilisticFenceParser(lexer,gp,ls.getSyntacticSpecification());
             return parser;
         } catch (CannotGenerateLanguageSpecificationException e) {
             throw new CannotCreateParserException(e);
         }
     }
 
     /**
      * Creates a probabilistic parser
      * @param m the model
      * @param skip the skip model
      * @return the parser
      * @throws CannotCreateParserException
      */
     public static ProbabilisticFenceParser create(Model m,Model skip) throws CannotCreateParserException {
         Set<PatternRecognizer> ignore = new HashSet<PatternRecognizer>();
         if (skip != null)
             fillIgnore(ignore,skip,skip.getStart());
         return create(m,ignore);
     }
 
     /**
     * Creates a probabilistic parser
      * @param m the model
      * @param ignore the ignore set
      * @return the parser
      * @throws CannotCreateParserException
      */
     public static ProbabilisticFenceParser create(Model m,Set<PatternRecognizer> ignore) throws CannotCreateParserException {
         try {
             //Type erasure does not allow comparing the generated parser with a specific parser type. Check http://serdom.eu/ser/2007/03/25/java-generics-instantiating-objects-of-type-parameter-without-using-class-literal
             LanguageSpecificationFactory lsf = new LanguageSpecificationFactory();
             LanguageSpecification ls = lsf.create(m);
             LambLexer gl = new LambLexer(ls.getLexicalSpecification(),ignore,new ProbabilisticLamb());
             ProbabilisticFenceParser parser = new ProbabilisticFenceParser(gl,new ProbabilisticFence(),ls.getSyntacticSpecification());
             return parser;
         } catch (CannotGenerateLanguageSpecificationException e) {
             throw new CannotCreateParserException(e);
         }
     }
 
 }
