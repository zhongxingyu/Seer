 /*
  * Licensed under the MIT license.
  * Copyright (c) 2007 David Holroyd
  */
 package uk.co.badgersinfoil.e4x;
 
 import org.antlr.runtime.*;
 
 import java.io.StringReader;
 import java.io.IOException;
 import java.io.Reader;
 
 import uk.co.badgersinfoil.e4x.antlr.*;
 
 /**
  * Helper class to use in your main grammar to delegate the E4X snippet parsing. See 
  * the <i>parseXMLLiteral</i> method for more details.
  */
 public class E4XHelper {
 
     private E4XExpressionParser expressionParser;
 
     /**
      * Should be called by your main grammar to delegate the parsing to the E4X island grammar. You
      * can, for example, include the following code in your parent grammar:
      * <pre>
      * \@parser::members {
      *     private SimPELLexer lexer;
      *     private CharStream cs;
      *     public void setInput(SimPELLexer lexer, CharStream cs) {
      *         this.lexer = lexer;
      *         this.cs = cs;
      *     }
      *     private LinkedListTree parseXMLLiteral() throws RecognitionException {
      *         return E4XHelper.parseXMLLiteral(lexer, cs, (LinkedListTokenStream)input);
      *     }
      * }
      * ...
      * xml_literal
      * \@init {
      *     LinkedListTree xml = null;
      * }
      *     : '<' { xml=parseXMLLiteral(); } -> { xml };
      * </pre>
      *
      * Note that you would have to call the setInput method of your main grammar parser before
      * invoking it so that the lexer and CharStream can be available.
      *
      * @param lexer main grammar lexer
      * @param cs main grammar char stream
      * @param stream the token stream as a LinkedListTokenStream
      * @return the AST as a LinkedListTree
      * @throws RecognitionException
      */
     public LinkedListTree parseXMLLiteral(TokenSource lexer, CharStream cs, LinkedListTokenStream stream)
             throws RecognitionException {
         String tail = cs.substring(cs.index(), cs.size()-1);
         int initialTailLength = tail.length();
         E4XParser parser;
         try {
             parser = xmlextParserOn(new StringReader(tail), stream);
         } catch (IOException e) {
            throw new RecognitionException();
        }
        LinkedListTree ast;
        try {
            ast = (LinkedListTree) parser.xmlPrimary().getTree();
        } catch (MismatchedTokenException mte) {
            throw new E4XRecognitionException(mte.line, mte.charPositionInLine, "Failed to parse XML, probably malformed");
         }
         tail = parser.getInputTail();
         // skip over the XML in the original, underlying CharStream
         cs.seek(cs.index() + (initialTailLength - tail.length()));
         LinkedListTokenSource source = (LinkedListTokenSource)stream.getTokenSource();
         stream.setTokenSource(source);  // cause any remembered E4X state to be dropped
         stream.scrub(1); // erase the subsequent token that the E4X parser got from this stream
         source.setDelegate(lexer);
         return ast;
     }
 
     private E4XParser xmlextParserOn(Reader in, LinkedListTokenStream stream) throws IOException {
         ANTLRReaderStream cs = new ANTLRReaderStream(in);
         E4XLexer lexer = new E4XLexer(cs);
         LinkedListTokenSource source = (LinkedListTokenSource)stream.getTokenSource();
         source.setDelegate(lexer);
 
         // The main grammar will see the initial '<' as an LT (less-than)
         // token, and lookahead in the AS3Parser will have already
         // grabbed references to that token in order to make it the
         // startToken for various AST subtrees, so we can't just delete
         // it.  We therefore find the LT token and change its type to
         // match the E4X vocabulary, and then rewind the token stream
         // so that this will be the first token that the E4XParser will
         // see.
         LinkedListToken startMarker = (LinkedListToken)stream.LT(-1);
         startMarker.setType(E4XParser.XML_LCHEVRON);
         stream.seek(stream.index()-1);
 
         E4XParser parser = new E4XParser(stream);
         if (expressionParser != null) parser.setExpressionParser(expressionParser);
         parser.setTreeAdaptor(new LinkedListTreeAdaptor());
         parser.setInput(lexer, cs);
         return parser;
     }
 
     public void setExpressionParser(E4XExpressionParser expressionParser) {
         this.expressionParser = expressionParser;
     }
 }
