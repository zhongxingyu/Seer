 /*
  * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
  *
  * This software is distributable under the BSD license. See the terms of the
  * BSD license in the documentation provided with this software.
  */
 package de.hunsicker.jalopy.language;
 
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import antlr.RecognitionException;
 import antlr.Token;
 import antlr.TokenBuffer;
 import antlr.TokenStreamException;
 import antlr.TokenStreamHiddenTokenFilter;
 import antlr.TokenStreamRecognitionException;
 import antlr.collections.AST;
 import de.hunsicker.jalopy.language.antlr.JavaTokenTypes;
 import de.hunsicker.jalopy.storage.Convention;
 import de.hunsicker.jalopy.storage.ConventionDefaults;
 import de.hunsicker.jalopy.storage.ConventionKeys;
 import de.hunsicker.jalopy.storage.Loggers;
 
 import org.apache.log4j.Level;
 
 
 /**
  * The Java-specific recognizer.
  *
  * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
  * @version $Revision$
  */
 public final class JavaRecognizer
     extends Recognizer
 {
     //~ Static variables/initializers ----------------------------------------------------
 
     /** Delimeter for encoded tag string. */
     private static final String DELIMETER = "|";
 
     /** Indicates JDK version 1.3. */
     public static final int JDK_1_3 = JavaLexer.JDK_1_3;
 
     /** Indicates JDK version 1.4. */
     public static final int JDK_1_4 = JavaLexer.JDK_1_4;
 
     //~ Instance variables ---------------------------------------------------------------
 
     /** The code convention. */
     private Convention _settings;
 
     /** List with the annotations for the current input source. */
     List _annotations = Collections.EMPTY_LIST; // List of <Annotation>
 
     /** The position that needs to be tracked. */
     Position _position;
 
     /** Resolves wildcard imports. */
     private Transformation _importTrans;
 
     /** Checks whether debug logging calls are enclosing with a boolean expression . */
     private Transformation _loggingTransformation;
 
     /** Inserts a serial version UID for serializable classes. */
     private Transformation _serialTrans;
 
     /** Sorts the AST tree. */
     private Transformation _sortTrans;
     boolean _trackPosition;
 
     /** Were the transformations applied to the generated AST? */
     private boolean _transformed;
     
     protected AST root = null;
 
     //~ Constructors ---------------------------------------------------------------------
 
     /**
      * Creates a new JavaRecognizer object.
      */
     public JavaRecognizer()
     {
         _settings = Convention.getInstance();
 
         JavaLexer l = new JavaLexer();
         this.lexer = l;
 
         JavaParser p = (JavaParser) l.getParser();
         this.parser = p;
 
         _importTrans =
             new ImportTransformation(p.getQualifiedIdents(), p.getUnqualifiedIdents());
         _sortTrans = new SortTransformation();
         _serialTrans = new SerializableTransformation();
         _loggingTransformation = new LoggerTransformation();
     }
 
     //~ Methods --------------------------------------------------------------------------
 
     /**
      * Returns the package name of the parsed source file.
      *
      * @return the package name of the parsed source file. Returns the empty String if
      *         the source file contains no package information.
      *
      * @throws IllegalStateException if the parser is still running or wasn't started
      *         yet.
      */
     public String getPackageName()
     {
         if (!this.finished)
         {
             throw new IllegalStateException("parser not started or still running");
         }
 
         return ((JavaParser) this.parser).getPackageName();
     }
 
 
     /**
      * Returns the root node of the generated parse tree. Note that every call to this
      * method triggers the tree transformations, which could be quite expensive. So make
      * sure to avoid unnecessary calls.
      * 
      * <p>
      * As we don't use checked exceptions to indicate runtime failures, one may check
      * successful execution of the transformations prior to perform further processing:
      * <pre class="snippet">
      * if (myJalopyInstance.getState() == Jalopy.State.ERROR)
      * {
      *     // transformation failed, errors were already issued; perform
      *     // any custom error handling code you need
      * }
      * else
      * {
      *     // perform further logic
      * }
      * </pre>
      * </p>
      *
      * @return root node of the generated AST (of type {@link
      *         de.hunsicker.jalopy.language.JavaNode &lt;JavaNode&gt;}).
      *
      * @throws IllegalStateException if the parser is still running or wasn't started
      *         yet.
      *
      * @see de.hunsicker.jalopy.Jalopy#getState
      */
     public AST getParseTree()
     {
         if (!this.finished)
         {
             throw new IllegalStateException("parser not started or still running");
         }
 
         if (!_transformed)
         {
             boolean trackAnnotations = !_annotations.isEmpty();
 
             if (_trackPosition || trackAnnotations)
             {
                 PositionTracker tracker = new PositionTracker();
 
                 if (trackAnnotations)
                 {
                     tracker.annotation = (Annotation) _annotations.get(0);
                 }
 
                 tracker.walk(super.getParseTree());
             }
 
             transform();
             _transformed = true;
         }
 
         return super.getParseTree();
     }
 
 
     /**
      * Sets a position in the given input source that should be tracked.
      *
      * @param line a valid line number (<code>&gt;= 1</code>).
      * @param column a valid column offset (<code>&gt;= 1</code>).
      *
      * @throws IllegalArgumentException if either <em>line</em> or <em>column</em><code>
      *         &lt; 1</code>
      *
      * @since 1.0b9
      */
     public void setPosition(
         int line,
         int column)
     {
         if (line < 1)
         {
             throw new IllegalArgumentException("line < 1 -- " + line);
         }
 
         if (column < 1)
         {
             throw new IllegalArgumentException("column < 1 -- " + column);
         }
 
         _trackPosition = true;
         _position = new Position(line, column);
     }
 
 
     /**
      * Returns the tracked position information.
      *
      * @return the tracked position or <code>null</code> if no position should have been
      *         tracked.
      *
      * @since 1.0b9
      */
     public Position getPosition()
     {
         return _position;
     }
 
 
     /**
      * Attaches the given annotations to the current input source. All annotations will
      * be associated with the parse tree node that matches their locations.
      *
      * @param annotations list with annotations (of type {@link
      *        de.hunsicker.jalopy.language.Annotation &lt;Annotation&gt;}).
      *
      * @see de.hunsicker.jalopy.language.Annotation
      * @see #detachAnnotations
      * @since 1.0b9
      */
     public void attachAnnotations(List annotations)
     {
         _annotations = annotations;
     }
 
 
     /**
      * Detaches all annotations.
      *
      * @return list with annotations (of type {@link
      *         de.hunsicker.jalopy.language.Annotation &lt;Annotation&gt;}). Returns an
      *         empty list in case no annotations were attached for the input source.
      *
      * @see #attachAnnotations
      * @since 1.0b9
      */
     public List detachAnnotations()
     {
         try
         {
             return _annotations;
         }
         finally
         {
             if (_annotations != Collections.EMPTY_LIST)
             {
                 _annotations = Collections.EMPTY_LIST;
             }
         }
     }
 
 
     /**
      * Indicates whether the current tree contains annotations.
      *
      * @return <code>true</code> if the tree contains annotations.
      *
      * @since 1.0b9
      */
     public boolean hasAnnotations()
     {
         return !_annotations.isEmpty();
     }
 
 
     /**
      * Determines whether the current tree contains a node that needs its position to be
      * tracked.
      *
      * @return <code>true</code> if the tree contains a node that needs its position to
      *         be tracked.
      *
      * @since 1.0b9
      */
     public boolean hasPosition()
     {
         return _position != null;
     }
 
 
     /**
      * {@inheritDoc}
      */
     public void parse(
         Reader in,
         String filename)
     {
         if (this.running)
         {
             throw new IllegalStateException("parser currently running");
         }
 
         this.finished = false;
         this.running = true;
         _transformed = false;
 
         // update the parsers/lexer driving settings prior to parsing
         JavaParser javaParser = (JavaParser) this.parser;
         javaParser.stripQualification =
             _settings.getBoolean(
                 ConventionKeys.STRIP_QUALIFICATION, ConventionDefaults.STRIP_QUALIFICATION);
 
         JavaLexer javaLexer = (JavaLexer) this.lexer;
         javaLexer.setTabSize(
             _settings.getInt(
                 ConventionKeys.INDENT_SIZE_TABS, ConventionDefaults.INDENT_SIZE_TABS));
         javaLexer.sourceVersion =
             _settings.getInt(
                 ConventionKeys.SOURCE_VERSION, ConventionDefaults.SOURCE_VERSION);
         javaLexer.parseJavadocComments =
             _settings.getBoolean(
                 ConventionKeys.COMMENT_JAVADOC_PARSE,
                 ConventionDefaults.COMMENT_JAVADOC_PARSE);
         javaLexer.removeJavadocComments =
             _settings.getBoolean(
                 ConventionKeys.COMMENT_JAVADOC_REMOVE,
                 ConventionDefaults.COMMENT_JAVADOC_REMOVE);
         javaLexer.removeSLComments =
             _settings.getBoolean(
                 ConventionKeys.COMMENT_REMOVE_SINGLE_LINE,
                 ConventionDefaults.COMMENT_REMOVE_SINGLE_LINE);
         javaLexer.removeMLComments =
             _settings.getBoolean(
                 ConventionKeys.COMMENT_REMOVE_MULTI_LINE,
                 ConventionDefaults.COMMENT_REMOVE_MULTI_LINE);
         javaLexer.formatMLComments =
             _settings.getBoolean(
                 ConventionKeys.COMMENT_FORMAT_MULTI_LINE,
                 ConventionDefaults.COMMENT_FORMAT_MULTI_LINE);
 
         JavadocParser javadocParser = javaLexer.getJavadocParser();
         javadocParser.setCustomStandardTags(
             decodeTags(
                 _settings.get(
                     ConventionKeys.COMMENT_JAVADOC_TAGS_STANDARD,
                     ConventionDefaults.COMMENT_JAVADOC_TAGS_STANDARD)));
         javadocParser.setCustomInlineTags(
             decodeTags(
                 _settings.get(
                     ConventionKeys.COMMENT_JAVADOC_TAGS_INLINE,
                     ConventionDefaults.COMMENT_JAVADOC_TAGS_INLINE)));
 
         this.lexer.setInputBuffer(in);
 
         /**
          * This private class slightly skews the way comments appear in the token chain
          * For example if a comment follows node variable def rather then attaching to the 
          * commentAfter of the variable def it attaches itself to the commentBefor of the next token.
          * ****KEY IMPORTANT****
          * See also JavaParser for more comment skewing.
          * 
          */
         TokenStreamHiddenTokenFilter filter =
             new TokenStreamHiddenTokenFilter(this.lexer){
             private void consumeFirst() throws TokenStreamException {
                     do {
                         consume();
                     } while (LA(1).getType()==Token.SKIP);
 
                 // Handle situation where hidden or discarded tokens
                 // appear first in input stream
                 ExtendedToken p = null;
                 // while hidden or discarded scarf tokens
                 while (hideMask.member(LA(1).getType()) || discardMask.member(LA(1).getType())) {
                     if (hideMask.member(LA(1).getType())) {
                         if (p == null) {
                             p = (ExtendedToken) LA(1);
                         }
                         else {
                             p.setHiddenAfter(LA(1));
                             ((ExtendedToken)LA(1)).setHiddenBefore(p); // double-link
                             p = (ExtendedToken) LA(1);
                         }
                         lastHiddenToken = p;
                         if (firstHidden == null) {
                             firstHidden = p; // record hidden token if first
                         }
                     }
                     do {
                         consume();
                     } while (LA(1).getType()==Token.SKIP);
                 }
             }   
             public Token nextToken() throws TokenStreamException {
                 // handle an initial condition; don't want to get lookahead
                 // token of this splitter until first call to nextToken
                 if (LA(1) == null) {
                     consumeFirst();
                 }
 
                 // we always consume hidden tokens after monitored, thus,
                 // upon entry LA(1) is a monitored token.
                 ExtendedToken monitored = (ExtendedToken) LA(1);
                 // point to hidden tokens found during last invocation
                 if (lastHiddenToken!=null && !((ExtendedToken)lastHiddenToken).attached) {
                     monitored.setHiddenBefore(lastHiddenToken);
                 }
                 else {
                     monitored.setHiddenBefore(null);
                 }
                 lastHiddenToken = null;
 
                 // Look for hidden tokens, hook them into list emanating
                 // from the monitored tokens.
                 consume();
                 ExtendedToken p = monitored;
                 ExtendedToken next = (ExtendedToken) LA(1);
                 // while hidden or discarded scarf tokens
                 while (hideMask.member(next.getType()) || discardMask.member(next.getType())) {
                     if (hideMask.member(next.getType())) {
                         // attach the hidden token to the monitored in a chain
                         // link forwards
                         if (!attachBefore && !next.attached) {
                            p.setHiddenAfter(next);
                            next.attached = true;
                            // Attach last hidden token to top
                            if (p!=monitored)
                                lastHiddenToken = p;
                         }
                         // link backwards
                         if (p != monitored) { //hidden cannot point to monitored tokens
                             if (attachBefore && !p.attached) {
                                 next.setHiddenBefore(p);
                                 p.setHiddenAfter(next);
                                 p.attached = true;
                                 // Attach last hidden token to bottom
                                 lastHiddenToken = next;
                             }
                         }
                         // Attach last hidden token to current hide mask
                         if (lastHiddenToken==null) {
                             lastHiddenToken = next;
                         }
                         // Current token points to next
                         p = next;
                     }
                     else if (next.getType()==JavaTokenTypes.WS) {
                         if (next.getText().indexOf("\n")>-1) {
                             attachBefore = true;
                         }
                         else {
                             attachBefore = false;
                         }
                     }
                     do {
                         consume();
                         next =(ExtendedToken) LA(1);
                     } while (next.getType()==Token.SKIP);
                 }
                 return monitored;
             }            
             protected boolean attachBefore = false;
         };
 
         /**
          * @todo keep WS
          */
         filter.discard(JavaTokenTypes.WS);
         filter.discard(JavaTokenTypes.SEPARATOR_COMMENT);
         
         if (javaLexer.removeJavadocComments) {
             filter.discard(JavaTokenTypes.JAVADOC_COMMENT);
         }
         else {
             filter.hide(JavaTokenTypes.JAVADOC_COMMENT);
         }
         if (javaLexer.removeMLComments) {
             filter.discard(JavaTokenTypes.ML_COMMENT);
         }
         else {
         filter.hide(JavaTokenTypes.ML_COMMENT);
         }
         filter.hide(JavaTokenTypes.SPECIAL_COMMENT);
         if (javaLexer.removeSLComments) {
             filter.discard(JavaTokenTypes.SL_COMMENT);
         }
         else {
             filter.hide(JavaTokenTypes.SL_COMMENT);
         }
 
         this.lexer.setFilename(filename);
         this.parser.setFilename(filename);
         this.parser.setTokenBuffer(new TokenBuffer(filter));
 
         try
         {
             this.parser.parse();
             
             root = ((JavaParser)this.parser).getAST();
         }
 
         // the parsers/lexers should never throw any checked exception as we
         // intercept them and print logging messages prior to attempt
         // further parsing, but we want to provide a savety net
         catch (RecognitionException ex)
         {
             ex.printStackTrace();
             throw new ParseException(ex);
         }
         catch (TokenStreamRecognitionException ex)
         {
             ex.printStackTrace();
             throw new ParseException(ex);
         }
         catch (TokenStreamException ex)
         {
             ex.printStackTrace();
             throw new ParseException(ex);
         }
         finally
         {
             this.finished = true;
             this.running = false;
         }
     }
     public AST getRoot() {
         return root;
     }
 
     /**
      * Decodes the given encoded tags string.
      *
      * @param tags encoded tags string.
      *
      * @return collection of the tags.
      *
      * @since 1.0b7
      */
     private Collection decodeTags(String tags)
     {
         List result = new ArrayList();
 
         for (StringTokenizer i = new StringTokenizer(tags, DELIMETER);
             i.hasMoreElements();)
         {
             result.add(i.nextToken());
         }
 
         return result;
     }
 
 
     /**
      * Applies the registered transformations to the AST.
      */
     private void transform()
     {
         AST tree = this.parser.getParseTree();
 
         if (tree != null)
         {
             try
             {
                 _importTrans.apply(tree);
 
                 if (
                     _settings.getBoolean(
                         ConventionKeys.INSERT_SERIAL_UID,
                         ConventionDefaults.INSERT_SERIAL_UID))
                 {
                     _serialTrans.apply(tree);
                 }
 
                 if (_settings.getBoolean(ConventionKeys.SORT, ConventionDefaults.SORT))
                 {
                     _sortTrans.apply(tree);
                 }
 
                 if (
                     _settings.getBoolean(
                         ConventionKeys.INSERT_LOGGING_CONDITIONAL,
                         ConventionDefaults.INSERT_LOGGING_CONDITIONAL))
                 {
                     _loggingTransformation.apply(tree);
                 }
             }
             catch (TransformationException ex)
             {
                 Object[] args = { this.parser.getFilename() };
                 Loggers.IO.l7dlog(Level.ERROR, "TRANS_ERROR", args, ex);
             }
         }
     }
 
     //~ Inner Classes --------------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @todo provide custom walkNode method, only link in children if span contains line
      */
     private final class PositionTracker
         extends TreeWalker
     {
         /** The current annotation that is tracked. */
         Annotation annotation;
 
         /** The current index of the annotation that is tracked. */
         int index;
 
         public void visit(AST node)
         {
             // forget about most imaginary nodes, operators and the like to make
             // implementing the tracking logic easier (as only a few printer classes need
             // to implement it)
             switch (node.getType())
             {
                 case JavaTokenTypes.CLASS_DEF :
                 case JavaTokenTypes.INTERFACE_DEF :
                 case JavaTokenTypes.METHOD_DEF :
                 case JavaTokenTypes.CTOR_DEF :
                 case JavaTokenTypes.STATIC_INIT :
                 case JavaTokenTypes.INSTANCE_INIT :
                 case JavaTokenTypes.VARIABLE_DEF :
                 case JavaTokenTypes.EXTENDS_CLAUSE :
                 case JavaTokenTypes.IMPLEMENTS_CLAUSE :
                 case JavaTokenTypes.MODIFIERS :
                 case JavaTokenTypes.ELIST :
                 case JavaTokenTypes.TYPE :
                 case JavaTokenTypes.ASSIGN :
                 case JavaTokenTypes.DOT :
                 case JavaTokenTypes.ARRAY_DECLARATOR :
                 case JavaTokenTypes.PARAMETERS :
                 case JavaTokenTypes.PARAMETER_DEF :
                 case JavaTokenTypes.LABELED_STAT :
                 case JavaTokenTypes.TYPECAST :
                 case JavaTokenTypes.INDEX_OP :
                 case JavaTokenTypes.POST_INC :
                 case JavaTokenTypes.POST_DEC :
                 case JavaTokenTypes.METHOD_CALL :
                 case JavaTokenTypes.EXPR :
                 case JavaTokenTypes.ARRAY_INIT :
                 case JavaTokenTypes.CASE_GROUP :
                 case JavaTokenTypes.FOR_INIT :
                 case JavaTokenTypes.FOR_ITERATOR :
                 case JavaTokenTypes.FOR_CONDITION :
                 case JavaTokenTypes.CTOR_CALL :
                 case JavaTokenTypes.SUPER_CTOR_CALL :
                 case JavaTokenTypes.LITERAL_new :
                 case JavaTokenTypes.LPAREN :
                 case JavaTokenTypes.RPAREN :
                 case JavaTokenTypes.LBRACK :
                 case JavaTokenTypes.RBRACK :
                 case JavaTokenTypes.CASESLIST :
                 case JavaTokenTypes.PLUS_ASSIGN :
                 case JavaTokenTypes.MINUS_ASSIGN :
                 case JavaTokenTypes.STAR_ASSIGN :
                 case JavaTokenTypes.DIV_ASSIGN :
                 case JavaTokenTypes.MOD_ASSIGN :
                 case JavaTokenTypes.SR_ASSIGN :
                 case JavaTokenTypes.BSR_ASSIGN :
                 case JavaTokenTypes.SL_ASSIGN :
                 case JavaTokenTypes.BAND_ASSIGN :
                 case JavaTokenTypes.BXOR_ASSIGN :
                 case JavaTokenTypes.BOR_ASSIGN :
                 case JavaTokenTypes.QUESTION :
                 case JavaTokenTypes.LOR :
                 case JavaTokenTypes.LAND :
                 case JavaTokenTypes.BOR :
                 case JavaTokenTypes.BXOR :
                 case JavaTokenTypes.BAND :
                 case JavaTokenTypes.NOT_EQUAL :
                 case JavaTokenTypes.EQUAL :
                 case JavaTokenTypes.LT :
                 case JavaTokenTypes.GT :
                 case JavaTokenTypes.LE :
                 case JavaTokenTypes.GE :
                 case JavaTokenTypes.LITERAL_instanceof :
                 case JavaTokenTypes.SL :
                 case JavaTokenTypes.SR :
                 case JavaTokenTypes.BSR :
                 case JavaTokenTypes.PLUS :
                 case JavaTokenTypes.MINUS :
                 case JavaTokenTypes.DIV :
                 case JavaTokenTypes.MOD :
                 case JavaTokenTypes.INC :
                 case JavaTokenTypes.DEC :
                 case JavaTokenTypes.BNOT :
                 case JavaTokenTypes.LNOT :
                 case JavaTokenTypes.COLON :
                     return;
             }
 
             JavaNode n = (JavaNode) node;
 
             if (this.annotation != null)
             {
                 if (n.getStartLine() == this.annotation.getLine())
                 {
                     n.attachAnnotation(annotation);
                     this.index++;
 
                     if (_annotations.size() > this.index)
                     {
                         this.annotation = (Annotation) _annotations.get(index);
                     }
                     else
                     {
                         this.annotation = null;
 
                         if (!_trackPosition)
                         {
                             stop();
                         }
                     }
                 }
             }
 
             if (_trackPosition)
             {
                 int line = n.getStartLine();
 
                 if (line == _position.line)
                 {
                     n.setPosition(_position);
                     _trackPosition = false;
 
                     if (this.annotation == null)
                     {
                         stop();
                     }
                 }
                 else if (line > _position.line)
                 {
                     // no code in the current caret line, move the caret to the
                     // first line with code
                     n.setPosition(_position);
                     _trackPosition = false;
 
                     if (this.annotation == null)
                     {
                         stop();
                     }
                 }
             }
         }
     }
 }
