 /*
  * ModelCC, under ModelCC Shared Software License, www.modelcc.org. Luis Quesada Torres.
  */
 
 
 package org.modelcc.parser.fence.adapter;
 
 import java.io.Reader;
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 import org.modelcc.language.syntax.SyntacticSpecification;
 import org.modelcc.parser.Parser;
 import org.modelcc.parser.ParserException;
 import org.modelcc.lexer.Lexer;
 import org.modelcc.parser.fence.Symbol;
 import org.modelcc.parser.fence.SyntaxGraph;
 import org.modelcc.parser.fence.Fence;
 
 /**
  * ModelCC FenceParser
  * @param <T> the results type of the parser
  * @author elezeta
  * @serial
  */
 public class FenceParser<T> extends Parser implements Serializable {
 
	//TODO ParserException
     /**
      * Serial Version ID
      */
     private static final long serialVersionUID = 31415926535897932L;
 
     /**
      * The generic parser.
      */
     private Fence gp;
 
     /**
      * The generic lexer.
      */
     private Lexer gl;
 
     /**
      * The syntactic specification.
      */
     private SyntacticSpecification ls;
 
     /**
      * Constructor
      * @param gl the generic lexer
      * @param gp the generic parser
      * @param ls the language specification
      */
     public FenceParser(Lexer gl,Fence gp,SyntacticSpecification ls) {
         this.gl = gl;
         this.gp = gp;
         this.ls = ls;
     }
 
     /**
      * Parses an input string
      * @param input the input string
      * @return a collection of parsed objects
      */
     @Override
      public Collection<T> parseAll(Reader input) throws ParserException {
         SyntaxGraph sg = gp.parse(ls,gl.scan(input));
         Set<T> out = new HashSet<T>();
         for (Iterator<Symbol> ite = sg.getRoots().iterator();ite.hasNext();) {
             out.add((T)ite.next().getUserData());
         }
         if (out.isEmpty()) {
         	throw new ParserException();
         }
         return out;
     }
 
     /**
      * Parses an input string
      * @param input the input string
      * @return a parsed object
      */
     @Override
     public T parse(String input) throws ParserException {
         Iterator<T> ite = parseIterator(input);
         if (ite.hasNext())
             return ite.next();
         else
             return null;
     }
 
     /**
      * Parses an input reader
      * @param input the input reader
      * @return a parsed object
      */
     @Override
     public T parse(Reader input) throws ParserException {
         return parseIterator(input).next();
     }
 
     /**
      * Parses an input string
      * @param input the input string
      * @return an iterator to the collection of parsed objects
      */
     @Override
     public Iterator<T> parseIterator(String input) throws ParserException {
         return parseAll(input).iterator();
     }
 
     /**
      * Parses an input reader
      * @param input the input reader
      * @return an iterator to the collection of parsed objects
      */
     @Override
     public Iterator<T> parseIterator(Reader input) throws ParserException {
         return parseAll(input).iterator();
     }
 
 }
