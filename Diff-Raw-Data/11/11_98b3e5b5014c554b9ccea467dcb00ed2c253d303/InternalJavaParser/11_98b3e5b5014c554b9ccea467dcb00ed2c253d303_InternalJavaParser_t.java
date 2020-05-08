 // $ANTLR 2.7.4: "java15.g" -> "InternalJavaParser.java"$
 
 package de.hunsicker.jalopy.language.antlr;
 
 import de.hunsicker.jalopy.language.JavaNode;
 import de.hunsicker.jalopy.language.JavaNodeHelper;
 
 import antlr.TokenBuffer;
 import antlr.TokenStreamException;
 import antlr.TokenStreamIOException;
 import antlr.ANTLRException;
 import antlr.LLkParser;
 import antlr.Token;
 import antlr.TokenStream;
 import antlr.RecognitionException;
 import antlr.NoViableAltException;
 import antlr.MismatchedTokenException;
 import antlr.SemanticException;
 import antlr.ParserSharedInputState;
 import antlr.collections.impl.BitSet;
 import antlr.collections.AST;
 import java.util.Hashtable;
 import antlr.ASTFactory;
 import antlr.ASTPair;
 import antlr.collections.impl.ASTArray;
 
 /** Java 1.5 Recognizer
  *
  * Run 'java Main [-showtree] directory-full-of-java-files'
  *
  * [The -showtree option pops up a Swing frame that shows
  *  the JavaNode constructed from the parser.]
  *
  * Run 'java Main <directory full of java files>'
  *
  * Contributing authors:
  *		John Mitchell		johnm@non.net
  *		Terence Parr		parrt@magelang.com
  *		John Lilley		jlilley@empathy.com
  *		Scott Stanchfield	thetick@magelang.com
  *		Markus Mohnen		mohnen@informatik.rwth-aachen.de
  *		Peter Williams		pete.williams@sun.com
  *		Allan Jacobs		Allan.Jacobs@eng.sun.com
  *		Steve Messick		messick@redhills.com
  *		John Pybus		john@pybus.org
  *
  * Version 1.00 December 9, 1997 -- initial release
  * Version 1.01 December 10, 1997
  *		fixed bug in octal def (0..7 not 0..8)
  * Version 1.10 August 1998 (parrt)
  *		added tree construction
  *		fixed definition of WS,comments for mac,pc,unix newlines
  *		added unary plus
  * Version 1.11 (Nov 20, 1998)
  *		Added "shutup" option to turn off last ambig warning.
  *		Fixed inner class def to allow named class defs as statements
  *		synchronized requires compound not simple statement
  *		add [] after builtInType DOT class in primaryExpression
  *		"const" is reserved but not valid..removed from modifiers
  * Version 1.12 (Feb 2, 1999)
  *		Changed LITERAL_xxx to xxx in tree grammar.
  *		Updated java.g to use tokens {...} now for 2.6.0 (new feature).
  *
  * Version 1.13 (Apr 23, 1999)
  *		Didn't have (stat)? for else clause in tree parser.
  *		Didn't gen ASTs for interface extends.  Updated tree parser too.
  *		Updated to 2.6.0.
  * Version 1.14 (Jun 20, 1999)
  *		Allowed final/abstract on local classes.
  *		Removed local interfaces from methods
  *		Put instanceof precedence where it belongs...in relationalExpr
  *			It also had expr not type as arg; fixed it.
  *		Missing ! on SEMI in classBlock
  *		fixed: (expr) + "string" was parsed incorrectly (+ as unary plus).
  *		fixed: didn't like Object[].class in parser or tree parser
  * Version 1.15 (Jun 26, 1999)
  *		Screwed up rule with instanceof in it. :(  Fixed.
  *		Tree parser didn't like (expr).something; fixed.
  *		Allowed multiple inheritance in tree grammar. oops.
  * Version 1.16 (August 22, 1999)
  *		Extending an interface built a wacky tree: had extra EXTENDS.
  *		Tree grammar didn't allow multiple superinterfaces.
  *		Tree grammar didn't allow empty var initializer: {}
  * Version 1.17 (October 12, 1999)
  *		ESC lexer rule allowed 399 max not 377 max.
  *		java.tree.g didn't handle the expression of synchronized
  *		statements.
  * Version 1.18 (August 12, 2001)
  *	  	Terence updated to Java 2 Version 1.3 by
  *		observing/combining work of Allan Jacobs and Steve
  *		Messick.  Handles 1.3 src.  Summary:
  *		o  primary didn't include boolean.class kind of thing
  *	  	o  constructor calls parsed explicitly now:
  * 		   see explicitConstructorInvocation
  *		o  add strictfp modifier
  *	  	o  missing objBlock after new expression in tree grammar
  *		o  merged local class definition alternatives, moved after declaration
  *		o  fixed problem with ClassName.super.field
  *	  	o  reordered some alternatives to make things more efficient
  *		o  long and double constants were not differentiated from int/float
  *		o  whitespace rule was inefficient: matched only one char
  *		o  add an examples directory with some nasty 1.3 cases
  *		o  made Main.java use buffered IO and a Reader for Unicode support
  *		o  supports UNICODE?
  *		   Using Unicode charVocabulay makes code file big, but only
  *		   in the bitsets at the end. I need to make ANTLR generate
  *		   unicode bitsets more efficiently.
  * Version 1.19 (April 25, 2002)
  *		Terence added in nice fixes by John Pybus concerning floating
  *		constants and problems with super() calls.  John did a nice
  *		reorg of the primary/postfix expression stuff to read better
  *		and makes f.g.super() parse properly (it was METHOD_CALL not
  *		a SUPER_CTOR_CALL).  Also:
  *
  *		o  "finally" clause was a root...made it a child of "try"
  *		o  Added stuff for asserts too for Java 1.4, but *commented out*
  *		   as it is not backward compatible.
  *
  * Version 1.20 (October 27, 2002)
  *
  *	  Terence ended up reorging John Pybus' stuff to
  *	  remove some nondeterminisms and some syntactic predicates.
  *	  Note that the grammar is stricter now; e.g., this(...) must
  *	be the first statement.
  *
  *	  Trinary ?: operator wasn't working as array name:
  *		  (isBig ? bigDigits : digits)[i];
  *
  *	  Checked parser/tree parser on source for
  *		  Resin-2.0.5, jive-2.1.1, jdk 1.3.1, Lucene, antlr 2.7.2a4,
  *		and the 110k-line jGuru server source.
  *
  * Version 1.21 (October 17, 2003)
  *  Fixed lots of problems including:
  *  Ray Waldin: add typeDefinition to interfaceBlock in java.tree.g
  *  He found a problem/fix with floating point that start with 0
  *  Ray also fixed problem that (int.class) was not recognized.
  *  Thorsten van Ellen noticed that \n are allowed incorrectly in strings.
  *  TJP fixed CHAR_LITERAL analogously.
  *
  * Version 1.21.2 (March, 2003)
  *	  Changes by Matt Quail to support generics (as per JDK1.5/JSR14)
  *	  Notes:
  *	  o We only allow the "extends" keyword and not the "implements"
  *		keyword, since thats what JSR14 seems to imply.
  *	  o Thanks to Monty Zukowski for his help on the antlr-interest
  *		mail list.
  *	  o Thanks to Alan Eliasen for testing the grammar over his
  *		Fink source base
  *
  * Version 1.22 (July, 2004)
  *	  Changes by Michael Studman to support Java 1.5 language extensions
  *	  Notes:
  *	  o Added support for annotations types
  *	  o Finished off Matt Quail's generics enhancements to support bound type arguments
  *	  o Added support for new for statement syntax
  *	  o Added support for static import syntax
  *	  o Added support for enum types
  *	  o Tested against JDK 1.5 source base and source base of jdigraph project
  *	  o Thanks to Matt Quail for doing the hard part by doing most of the generics work
  *
  * Version 1.22.1 (July 28, 2004)
  *	  Bug/omission fixes for Java 1.5 language support
  *	  o Fixed tree structure bug with classOrInterface - thanks to Pieter Vangorpto for
  *		spotting this
  *	  o Fixed bug where incorrect handling of SR and BSR tokens would cause type
  *		parameters to be recognised as type arguments.
  *	  o Enabled type parameters on constructors, annotations on enum constants
  *		and package definitions
  *	  o Fixed problems when parsing if ((char.class.equals(c))) {} - solution by Matt Quail at Cenqua
  *
  * Version 1.22.2 (July 28, 2004)
  *	  Slight refactoring of Java 1.5 language support
  *	  o Refactored for/"foreach" productions so that original literal "for" literal
  *	    is still used but the for sub-clauses vary by token type
  *	  o Fixed bug where type parameter was not included in generic constructor's branch of AST
  *
  * Version 1.22.3 (August 26, 2004)
  *	  Bug fixes as identified by Michael Stahl; clean up of tabs/spaces
  *        and other refactorings
  *	  o Fixed typeParameters omission in identPrimary and newStatement
  *	  o Replaced GT reconcilliation code with simple semantic predicate
  *	  o Adapted enum/assert keyword checking support from Michael Stahl's java15 grammar
  *	  o Refactored typeDefinition production and field productions to reduce duplication
  *
  * Version 1.22.4 (October 21, 2004)
  *    Small bux fixes
  *    o Added typeArguments to explicitConstructorInvocation, e.g. new <String>MyParameterised()
  *    o Added typeArguments to postfixExpression productions for anonymous inner class super
  *      constructor invocation, e.g. new Outer().<String>super()
  *    o Fixed bug in array declarations identified by Geoff Roy
  *
  * This grammar is in the PUBLIC DOMAIN
  */
 public abstract class InternalJavaParser extends antlr.LLkParser       implements JavaTokenTypes
  {
 
 	/**
 	 * Counts the number of LT seen in the typeArguments production.
 	 * It is used in semantic predicates to ensure we have seen
 	 * enough closing '>' characters; which actually may have been
 	 * either GT, SR or BSR tokens.
 	 */
 	private int ltCounter = 0;
 
 	protected abstract void attachStuff(JavaNode[] nodes)  throws TokenStreamIOException;
 
 
 protected InternalJavaParser(TokenBuffer tokenBuf, int k) {
   super(tokenBuf,k);
   tokenNames = _tokenNames;
   buildTokenTypeASTClassMap();
   astFactory = new ASTFactory(getTokenTypeToASTClassMap());
 }
 
 public InternalJavaParser(TokenBuffer tokenBuf) {
   this(tokenBuf,2);
 }
 
 protected InternalJavaParser(TokenStream lexer, int k) {
   super(lexer,k);
   tokenNames = _tokenNames;
   buildTokenTypeASTClassMap();
   astFactory = new ASTFactory(getTokenTypeToASTClassMap());
 }
 
 public InternalJavaParser(TokenStream lexer) {
   this(lexer,2);
 }
 
 public InternalJavaParser(ParserSharedInputState state) {
   super(state,2);
   tokenNames = _tokenNames;
   buildTokenTypeASTClassMap();
   astFactory = new ASTFactory(getTokenTypeToASTClassMap());
 }
 
 	public final void parse() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode parse_AST = null;
 		
 		JavaNode root = new JavaNode();
 		root.setType(JavaTokenTypes.ROOT);
 		root.setText(getFilename());
 		currentAST.root = root;
 		
 		
 		{
 		boolean synPredMatched4 = false;
 		if (((LA(1)==LITERAL_package||LA(1)==AT) && (LA(2)==IDENT))) {
 			int _m4 = mark();
 			synPredMatched4 = true;
 			inputState.guessing++;
 			try {
 				{
 				annotations();
 				match(LITERAL_package);
 				}
 			}
 			catch (RecognitionException pe) {
 				synPredMatched4 = false;
 			}
 			rewind(_m4);
 			inputState.guessing--;
 		}
 		if ( synPredMatched4 ) {
 			packageDefinition();
 			astFactory.addASTChild(currentAST, returnAST);
 		}
 		else if ((_tokenSet_0.member(LA(1))) && (_tokenSet_1.member(LA(2)))) {
 		}
 		else {
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		
 		}
 		{
 		_loop6:
 		do {
 			if ((LA(1)==LITERAL_import)) {
 				importDefinition();
 				astFactory.addASTChild(currentAST, returnAST);
 			}
 			else {
 				break _loop6;
 			}
 			
 		} while (true);
 		}
 		{
 		_loop8:
 		do {
 			if ((_tokenSet_2.member(LA(1)))) {
 				typeDefinition();
 				astFactory.addASTChild(currentAST, returnAST);
 			}
 			else {
 				break _loop8;
 			}
 			
 		} while (true);
 		}
 		match(Token.EOF_TYPE);
 		parse_AST = (JavaNode)currentAST.root;
 		returnAST = parse_AST;
 	}
 	
 	public final void annotations() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode annotations_AST = null;
 		
 		{
 		_loop62:
 		do {
 			if ((LA(1)==AT)) {
 				annotation();
 				astFactory.addASTChild(currentAST, returnAST);
 			}
 			else {
 				break _loop62;
 			}
 			
 		} while (true);
 		}
 		if ( inputState.guessing==0 ) {
 			annotations_AST = (JavaNode)currentAST.root;
 			annotations_AST = (JavaNode)astFactory.make( (new ASTArray(2)).add((JavaNode)astFactory.create(ANNOTATIONS,"ANNOTATIONS")).add(annotations_AST));
 			currentAST.root = annotations_AST;
 			currentAST.child = annotations_AST!=null &&annotations_AST.getFirstChild()!=null ?
 				annotations_AST.getFirstChild() : annotations_AST;
 			currentAST.advanceChildToEnd();
 		}
 		annotations_AST = (JavaNode)currentAST.root;
 		returnAST = annotations_AST;
 	}
 	
 	public final void packageDefinition() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode packageDefinition_AST = null;
 		Token  p = null;
 		JavaNode p_AST = null;
 		
 		try {      // for error handling
 			annotations();
 			astFactory.addASTChild(currentAST, returnAST);
 			p = LT(1);
 			p_AST = (JavaNode)astFactory.create(p);
 			astFactory.makeASTRoot(currentAST, p_AST);
 			match(LITERAL_package);
 			if ( inputState.guessing==0 ) {
 				p_AST.setType(PACKAGE_DEF);
 			}
 			identifier();
 			astFactory.addASTChild(currentAST, returnAST);
 			JavaNode tmp2_AST = null;
 			tmp2_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp2_AST);
 			match(SEMI);
 			packageDefinition_AST = (JavaNode)currentAST.root;
 		}
 		catch (RecognitionException ex) {
 			if (inputState.guessing==0) {
 				reportError(ex);
 				consume();
 				consumeUntil(_tokenSet_0);
 			} else {
 			  throw ex;
 			}
 		}
 		returnAST = packageDefinition_AST;
 	}
 	
 	public final void importDefinition() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode importDefinition_AST = null;
 		Token  i = null;
 		JavaNode i_AST = null;
 		boolean isStatic = false;
 		
 		try {      // for error handling
 			i = LT(1);
 			i_AST = (JavaNode)astFactory.create(i);
 			astFactory.makeASTRoot(currentAST, i_AST);
 			match(LITERAL_import);
 			if ( inputState.guessing==0 ) {
 				i_AST.setType(IMPORT);
 			}
 			{
 			switch ( LA(1)) {
 			case LITERAL_static:
 			{
 				match(LITERAL_static);
 				if ( inputState.guessing==0 ) {
 					i_AST.setType(STATIC_IMPORT);
 				}
 				break;
 			}
 			case IDENT:
 			{
 				break;
 			}
 			default:
 			{
 				throw new NoViableAltException(LT(1), getFilename());
 			}
 			}
 			}
 			identifierStar();
 			astFactory.addASTChild(currentAST, returnAST);
 			JavaNode tmp4_AST = null;
 			tmp4_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp4_AST);
 			match(SEMI);
 			importDefinition_AST = (JavaNode)currentAST.root;
 		}
 		catch (RecognitionException ex) {
 			if (inputState.guessing==0) {
 				reportError(ex);
 				consume();
 				consumeUntil(_tokenSet_0);
 			} else {
 			  throw ex;
 			}
 		}
 		returnAST = importDefinition_AST;
 	}
 	
 	public final void typeDefinition() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode typeDefinition_AST = null;
 		JavaNode m_AST = null;
 		
 		try {      // for error handling
 			switch ( LA(1)) {
 			case FINAL:
 			case ABSTRACT:
 			case STRICTFP:
 			case LITERAL_static:
 			case LITERAL_private:
 			case LITERAL_public:
 			case LITERAL_protected:
 			case LITERAL_transient:
 			case LITERAL_native:
 			case LITERAL_threadsafe:
 			case LITERAL_synchronized:
 			case LITERAL_volatile:
 			case AT:
 			case LITERAL_class:
 			case LITERAL_interface:
 			case LITERAL_enum:
 			{
 				modifiers();
 				m_AST = (JavaNode)returnAST;
 				typeDefinitionInternal(m_AST);
 				astFactory.addASTChild(currentAST, returnAST);
 				typeDefinition_AST = (JavaNode)currentAST.root;
 				break;
 			}
 			case SEMI:
 			{
 				JavaNode tmp5_AST = null;
 				tmp5_AST = (JavaNode)astFactory.create(LT(1));
 				astFactory.addASTChild(currentAST, tmp5_AST);
 				match(SEMI);
 				typeDefinition_AST = (JavaNode)currentAST.root;
 				break;
 			}
 			default:
 			{
 				throw new NoViableAltException(LT(1), getFilename());
 			}
 			}
 		}
 		catch (RecognitionException ex) {
 			if (inputState.guessing==0) {
 				reportError(ex);
 				consume();
 				consumeUntil(_tokenSet_3);
 			} else {
 			  throw ex;
 			}
 		}
 		returnAST = typeDefinition_AST;
 	}
 	
 	public final void identifier() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode identifier_AST = null;
 		
 		JavaNode tmp6_AST = null;
 		tmp6_AST = (JavaNode)astFactory.create(LT(1));
 		astFactory.addASTChild(currentAST, tmp6_AST);
 		match(IDENT);
 		{
 		_loop48:
 		do {
 			if ((LA(1)==DOT)) {
 				JavaNode tmp7_AST = null;
 				tmp7_AST = (JavaNode)astFactory.create(LT(1));
 				astFactory.makeASTRoot(currentAST, tmp7_AST);
 				match(DOT);
 				JavaNode tmp8_AST = null;
 				tmp8_AST = (JavaNode)astFactory.create(LT(1));
 				astFactory.addASTChild(currentAST, tmp8_AST);
 				match(IDENT);
 			}
 			else {
 				break _loop48;
 			}
 			
 		} while (true);
 		}
 		identifier_AST = (JavaNode)currentAST.root;
 		returnAST = identifier_AST;
 	}
 	
 	public final void identifierStar() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode identifierStar_AST = null;
 		
 		JavaNode tmp9_AST = null;
 		tmp9_AST = (JavaNode)astFactory.create(LT(1));
 		astFactory.addASTChild(currentAST, tmp9_AST);
 		match(IDENT);
 		{
 		_loop51:
 		do {
 			if ((LA(1)==DOT) && (LA(2)==IDENT)) {
 				JavaNode tmp10_AST = null;
 				tmp10_AST = (JavaNode)astFactory.create(LT(1));
 				astFactory.makeASTRoot(currentAST, tmp10_AST);
 				match(DOT);
 				JavaNode tmp11_AST = null;
 				tmp11_AST = (JavaNode)astFactory.create(LT(1));
 				astFactory.addASTChild(currentAST, tmp11_AST);
 				match(IDENT);
 			}
 			else {
 				break _loop51;
 			}
 			
 		} while (true);
 		}
 		{
 		switch ( LA(1)) {
 		case DOT:
 		{
 			JavaNode tmp12_AST = null;
 			tmp12_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.makeASTRoot(currentAST, tmp12_AST);
 			match(DOT);
 			JavaNode tmp13_AST = null;
 			tmp13_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp13_AST);
 			match(STAR);
 			break;
 		}
 		case SEMI:
 		{
 			break;
 		}
 		default:
 		{
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		}
 		}
 		identifierStar_AST = (JavaNode)currentAST.root;
 		returnAST = identifierStar_AST;
 	}
 	
 	public final void modifiers() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode modifiers_AST = null;
 		
 		{
 		_loop55:
 		do {
 			if ((_tokenSet_4.member(LA(1)))) {
 				modifier();
 				astFactory.addASTChild(currentAST, returnAST);
 			}
 			else if (((LA(1)==AT) && (LA(2)==IDENT))&&(LA(1)==AT && !LT(2).getText().equals("interface"))) {
 				annotation();
 				astFactory.addASTChild(currentAST, returnAST);
 			}
 			else {
 				break _loop55;
 			}
 			
 		} while (true);
 		}
 		if ( inputState.guessing==0 ) {
 			modifiers_AST = (JavaNode)currentAST.root;
 			modifiers_AST = (JavaNode)astFactory.make( (new ASTArray(2)).add((JavaNode)astFactory.create(MODIFIERS,"MODIFIERS")).add(modifiers_AST));
 			currentAST.root = modifiers_AST;
 			currentAST.child = modifiers_AST!=null &&modifiers_AST.getFirstChild()!=null ?
 				modifiers_AST.getFirstChild() : modifiers_AST;
 			currentAST.advanceChildToEnd();
 		}
 		modifiers_AST = (JavaNode)currentAST.root;
 		returnAST = modifiers_AST;
 	}
 	
 	protected final void typeDefinitionInternal(
 		JavaNode mods
 	) throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode typeDefinitionInternal_AST = null;
 		
 		switch ( LA(1)) {
 		case LITERAL_class:
 		{
 			classDefinition(mods);
 			astFactory.addASTChild(currentAST, returnAST);
 			typeDefinitionInternal_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case LITERAL_interface:
 		{
 			interfaceDefinition(mods);
 			astFactory.addASTChild(currentAST, returnAST);
 			typeDefinitionInternal_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case LITERAL_enum:
 		{
 			enumDefinition(mods);
 			astFactory.addASTChild(currentAST, returnAST);
 			typeDefinitionInternal_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case AT:
 		{
 			annotationDefinition(mods);
 			astFactory.addASTChild(currentAST, returnAST);
 			typeDefinitionInternal_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		default:
 		{
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		}
 		returnAST = typeDefinitionInternal_AST;
 	}
 	
 	public final void classDefinition(
 		JavaNode modifiers
 	) throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode classDefinition_AST = null;
 		Token  c = null;
 		JavaNode c_AST = null;
 		JavaNode tp_AST = null;
 		JavaNode sc_AST = null;
 		JavaNode ic_AST = null;
 		JavaNode cb_AST = null;
 		
 		c = LT(1);
 		c_AST = (JavaNode)astFactory.create(c);
 		match(LITERAL_class);
 		JavaNode tmp14_AST = null;
 		tmp14_AST = (JavaNode)astFactory.create(LT(1));
 		match(IDENT);
 		{
 		switch ( LA(1)) {
 		case LT:
 		{
 			typeParameters();
 			tp_AST = (JavaNode)returnAST;
 			break;
 		}
 		case LCURLY:
 		case LITERAL_extends:
 		case LITERAL_implements:
 		{
 			break;
 		}
 		default:
 		{
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		}
 		}
 		superClassClause();
 		sc_AST = (JavaNode)returnAST;
 		implementsClause();
 		ic_AST = (JavaNode)returnAST;
 		classBlock();
 		cb_AST = (JavaNode)returnAST;
 		if ( inputState.guessing==0 ) {
 			classDefinition_AST = (JavaNode)currentAST.root;
 			classDefinition_AST = (JavaNode)astFactory.make( (new ASTArray(7)).add((JavaNode)astFactory.create(CLASS_DEF,"CLASS_DEF")).add(modifiers).add(tmp14_AST).add(tp_AST).add(sc_AST).add(ic_AST).add(cb_AST));
 					attachStuff(new JavaNode[] {classDefinition_AST, modifiers, c_AST});
 					
 			currentAST.root = classDefinition_AST;
 			currentAST.child = classDefinition_AST!=null &&classDefinition_AST.getFirstChild()!=null ?
 				classDefinition_AST.getFirstChild() : classDefinition_AST;
 			currentAST.advanceChildToEnd();
 		}
 		returnAST = classDefinition_AST;
 	}
 	
 	public final void interfaceDefinition(
 		JavaNode modifiers
 	) throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode interfaceDefinition_AST = null;
 		Token  i = null;
 		JavaNode i_AST = null;
 		JavaNode tp_AST = null;
 		JavaNode ie_AST = null;
 		JavaNode ib_AST = null;
 		
 		i = LT(1);
 		i_AST = (JavaNode)astFactory.create(i);
 		match(LITERAL_interface);
 		JavaNode tmp15_AST = null;
 		tmp15_AST = (JavaNode)astFactory.create(LT(1));
 		match(IDENT);
 		{
 		switch ( LA(1)) {
 		case LT:
 		{
 			typeParameters();
 			tp_AST = (JavaNode)returnAST;
 			break;
 		}
 		case LCURLY:
 		case LITERAL_extends:
 		{
 			break;
 		}
 		default:
 		{
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		}
 		}
 		interfaceExtends();
 		ie_AST = (JavaNode)returnAST;
 		interfaceBlock();
 		ib_AST = (JavaNode)returnAST;
 		if ( inputState.guessing==0 ) {
 			interfaceDefinition_AST = (JavaNode)currentAST.root;
 			interfaceDefinition_AST = (JavaNode)astFactory.make( (new ASTArray(6)).add((JavaNode)astFactory.create(INTERFACE_DEF,"INTERFACE_DEF")).add(modifiers).add(tmp15_AST).add(tp_AST).add(ie_AST).add(ib_AST));
 					attachStuff(new JavaNode[] {interfaceDefinition_AST, modifiers, i_AST});
 												
 			currentAST.root = interfaceDefinition_AST;
 			currentAST.child = interfaceDefinition_AST!=null &&interfaceDefinition_AST.getFirstChild()!=null ?
 				interfaceDefinition_AST.getFirstChild() : interfaceDefinition_AST;
 			currentAST.advanceChildToEnd();
 		}
 		returnAST = interfaceDefinition_AST;
 	}
 	
 	public final void enumDefinition(
 		JavaNode modifiers
 	) throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode enumDefinition_AST = null;
 		Token  e = null;
 		JavaNode e_AST = null;
 		JavaNode ic_AST = null;
 		JavaNode eb_AST = null;
 		
 		e = LT(1);
 		e_AST = (JavaNode)astFactory.create(e);
 		match(LITERAL_enum);
 		JavaNode tmp16_AST = null;
 		tmp16_AST = (JavaNode)astFactory.create(LT(1));
 		match(IDENT);
 		implementsClause();
 		ic_AST = (JavaNode)returnAST;
 		enumBlock();
 		eb_AST = (JavaNode)returnAST;
 		if ( inputState.guessing==0 ) {
 			enumDefinition_AST = (JavaNode)currentAST.root;
 			enumDefinition_AST = (JavaNode)astFactory.make( (new ASTArray(5)).add((JavaNode)astFactory.create(ENUM_DEF,"ENUM_DEF")).add(modifiers).add(tmp16_AST).add(ic_AST).add(eb_AST));
 					attachStuff(new JavaNode[] {enumDefinition_AST, modifiers, e_AST});
 											
 			currentAST.root = enumDefinition_AST;
 			currentAST.child = enumDefinition_AST!=null &&enumDefinition_AST.getFirstChild()!=null ?
 				enumDefinition_AST.getFirstChild() : enumDefinition_AST;
 			currentAST.advanceChildToEnd();
 		}
 		returnAST = enumDefinition_AST;
 	}
 	
 	public final void annotationDefinition(
 		JavaNode modifiers
 	) throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode annotationDefinition_AST = null;
 		Token  a = null;
 		JavaNode a_AST = null;
 		JavaNode ab_AST = null;
 		
 		a = LT(1);
 		a_AST = (JavaNode)astFactory.create(a);
 		match(AT);
 		match(LITERAL_interface);
 		JavaNode tmp18_AST = null;
 		tmp18_AST = (JavaNode)astFactory.create(LT(1));
 		match(IDENT);
 		annotationBlock();
 		ab_AST = (JavaNode)returnAST;
 		if ( inputState.guessing==0 ) {
 			annotationDefinition_AST = (JavaNode)currentAST.root;
 			annotationDefinition_AST = (JavaNode)astFactory.make( (new ASTArray(4)).add((JavaNode)astFactory.create(ANNOTATION_DEF,"ANNOTATION_DEF")).add(modifiers).add(tmp18_AST).add(ab_AST));
 					attachStuff(new JavaNode[] {annotationDefinition_AST, modifiers, a_AST});
 												
 			currentAST.root = annotationDefinition_AST;
 			currentAST.child = annotationDefinition_AST!=null &&annotationDefinition_AST.getFirstChild()!=null ?
 				annotationDefinition_AST.getFirstChild() : annotationDefinition_AST;
 			currentAST.advanceChildToEnd();
 		}
 		returnAST = annotationDefinition_AST;
 	}
 	
 	public final void declaration() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode declaration_AST = null;
 		JavaNode m_AST = null;
 		JavaNode t_AST = null;
 		JavaNode v_AST = null;
 		
 		modifiers();
 		m_AST = (JavaNode)returnAST;
 		typeSpec(false);
 		t_AST = (JavaNode)returnAST;
 		variableDefinitions(m_AST,t_AST);
 		v_AST = (JavaNode)returnAST;
 		if ( inputState.guessing==0 ) {
 			declaration_AST = (JavaNode)currentAST.root;
 			declaration_AST = v_AST;
 			currentAST.root = declaration_AST;
 			currentAST.child = declaration_AST!=null &&declaration_AST.getFirstChild()!=null ?
 				declaration_AST.getFirstChild() : declaration_AST;
 			currentAST.advanceChildToEnd();
 		}
 		returnAST = declaration_AST;
 	}
 	
 	public final void typeSpec(
 		boolean addImagNode
 	) throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode typeSpec_AST = null;
 		
 		switch ( LA(1)) {
 		case IDENT:
 		{
 			classTypeSpec(addImagNode);
 			astFactory.addASTChild(currentAST, returnAST);
 			typeSpec_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case LITERAL_void:
 		case LITERAL_boolean:
 		case LITERAL_byte:
 		case LITERAL_char:
 		case LITERAL_short:
 		case LITERAL_int:
 		case LITERAL_float:
 		case LITERAL_long:
 		case LITERAL_double:
 		{
 			builtInTypeSpec(addImagNode);
 			astFactory.addASTChild(currentAST, returnAST);
 			typeSpec_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		default:
 		{
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		}
 		returnAST = typeSpec_AST;
 	}
 	
 	public final void variableDefinitions(
 		JavaNode mods, JavaNode t
 	) throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode variableDefinitions_AST = null;
 		
 		variableDeclarator((JavaNode)getASTFactory().dupTree(mods),
                                                            (JavaNode)getASTFactory().dupTree(t));
 		astFactory.addASTChild(currentAST, returnAST);
 		{
 		_loop157:
 		do {
 			if ((LA(1)==COMMA)) {
 				match(COMMA);
 				variableDeclarator((JavaNode)getASTFactory().dupTree(mods),
                                                            (JavaNode)getASTFactory().dupTree(t));
 				astFactory.addASTChild(currentAST, returnAST);
 			}
 			else {
 				break _loop157;
 			}
 			
 		} while (true);
 		}
 		variableDefinitions_AST = (JavaNode)currentAST.root;
 		returnAST = variableDefinitions_AST;
 	}
 	
 	public final void classTypeSpec(
 		boolean addImagNode
 	) throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode classTypeSpec_AST = null;
 		Token  lb = null;
 		JavaNode lb_AST = null;
 		
 		classOrInterfaceType(false);
 		astFactory.addASTChild(currentAST, returnAST);
 		{
 		_loop18:
 		do {
 			if ((LA(1)==LBRACK) && (LA(2)==RBRACK)) {
 				lb = LT(1);
 				lb_AST = (JavaNode)astFactory.create(lb);
 				astFactory.makeASTRoot(currentAST, lb_AST);
 				match(LBRACK);
 				if ( inputState.guessing==0 ) {
 					lb_AST.setType(ARRAY_DECLARATOR);
 				}
 				match(RBRACK);
 			}
 			else {
 				break _loop18;
 			}
 			
 		} while (true);
 		}
 		if ( inputState.guessing==0 ) {
 			classTypeSpec_AST = (JavaNode)currentAST.root;
 			
 						if ( addImagNode ) {
 							classTypeSpec_AST = (JavaNode)astFactory.make( (new ASTArray(2)).add((JavaNode)astFactory.create(TYPE,"TYPE")).add(classTypeSpec_AST));
 						}
 					
 			currentAST.root = classTypeSpec_AST;
 			currentAST.child = classTypeSpec_AST!=null &&classTypeSpec_AST.getFirstChild()!=null ?
 				classTypeSpec_AST.getFirstChild() : classTypeSpec_AST;
 			currentAST.advanceChildToEnd();
 		}
 		classTypeSpec_AST = (JavaNode)currentAST.root;
 		returnAST = classTypeSpec_AST;
 	}
 	
 	public final void builtInTypeSpec(
 		boolean addImagNode
 	) throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode builtInTypeSpec_AST = null;
 		Token  lb = null;
 		JavaNode lb_AST = null;
 		
 		builtInType();
 		astFactory.addASTChild(currentAST, returnAST);
 		{
 		_loop43:
 		do {
 			if ((LA(1)==LBRACK)) {
 				lb = LT(1);
 				lb_AST = (JavaNode)astFactory.create(lb);
 				astFactory.makeASTRoot(currentAST, lb_AST);
 				match(LBRACK);
 				if ( inputState.guessing==0 ) {
 					lb_AST.setType(ARRAY_DECLARATOR);
 				}
 				match(RBRACK);
 			}
 			else {
 				break _loop43;
 			}
 			
 		} while (true);
 		}
 		if ( inputState.guessing==0 ) {
 			builtInTypeSpec_AST = (JavaNode)currentAST.root;
 			
 						if ( addImagNode ) {
 							builtInTypeSpec_AST = (JavaNode)astFactory.make( (new ASTArray(2)).add((JavaNode)astFactory.create(TYPE,"TYPE")).add(builtInTypeSpec_AST));
 						}
 					
 			currentAST.root = builtInTypeSpec_AST;
 			currentAST.child = builtInTypeSpec_AST!=null &&builtInTypeSpec_AST.getFirstChild()!=null ?
 				builtInTypeSpec_AST.getFirstChild() : builtInTypeSpec_AST;
 			currentAST.advanceChildToEnd();
 		}
 		builtInTypeSpec_AST = (JavaNode)currentAST.root;
 		returnAST = builtInTypeSpec_AST;
 	}
 	
 	public final void classOrInterfaceType(
 		boolean addImagNode
 	) throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode classOrInterfaceType_AST = null;
 		
 		JavaNode tmp22_AST = null;
 		tmp22_AST = (JavaNode)astFactory.create(LT(1));
 		astFactory.makeASTRoot(currentAST, tmp22_AST);
 		match(IDENT);
 		{
 		if ((LA(1)==LT) && (_tokenSet_5.member(LA(2)))) {
 			typeArguments();
 			astFactory.addASTChild(currentAST, returnAST);
 		}
 		else if ((_tokenSet_6.member(LA(1))) && (_tokenSet_7.member(LA(2)))) {
 		}
 		else {
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		
 		}
 		{
 		_loop23:
 		do {
 			if ((LA(1)==DOT) && (LA(2)==IDENT)) {
 				JavaNode tmp23_AST = null;
 				tmp23_AST = (JavaNode)astFactory.create(LT(1));
 				astFactory.makeASTRoot(currentAST, tmp23_AST);
 				match(DOT);
 				JavaNode tmp24_AST = null;
 				tmp24_AST = (JavaNode)astFactory.create(LT(1));
 				astFactory.addASTChild(currentAST, tmp24_AST);
 				match(IDENT);
 				{
 				if ((LA(1)==LT) && (_tokenSet_5.member(LA(2)))) {
 					typeArguments();
 					astFactory.addASTChild(currentAST, returnAST);
 				}
 				else if ((_tokenSet_6.member(LA(1))) && (_tokenSet_7.member(LA(2)))) {
 				}
 				else {
 					throw new NoViableAltException(LT(1), getFilename());
 				}
 				
 				}
 			}
 			else {
 				break _loop23;
 			}
 			
 		} while (true);
 		}
 		if ( inputState.guessing==0 ) {
 			classOrInterfaceType_AST = (JavaNode)currentAST.root;
 			
 						if ( addImagNode ) {
 							classOrInterfaceType_AST = (JavaNode)astFactory.make( (new ASTArray(2)).add((JavaNode)astFactory.create(TYPE,"TYPE")).add(classOrInterfaceType_AST));
 						}
 					
 			currentAST.root = classOrInterfaceType_AST;
 			currentAST.child = classOrInterfaceType_AST!=null &&classOrInterfaceType_AST.getFirstChild()!=null ?
 				classOrInterfaceType_AST.getFirstChild() : classOrInterfaceType_AST;
 			currentAST.advanceChildToEnd();
 		}
 		classOrInterfaceType_AST = (JavaNode)currentAST.root;
 		returnAST = classOrInterfaceType_AST;
 	}
 	
 	public final void typeArguments() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode typeArguments_AST = null;
 		int currentLtLevel = 0;
 		
 		if ( inputState.guessing==0 ) {
 			currentLtLevel = ltCounter;
 		}
 		match(LT);
 		if ( inputState.guessing==0 ) {
 			ltCounter++;
 		}
 		typeArgument();
 		astFactory.addASTChild(currentAST, returnAST);
 		{
 		_loop33:
 		do {
 			if (((LA(1)==COMMA) && (_tokenSet_5.member(LA(2))))&&(inputState.guessing !=0 || ltCounter == currentLtLevel + 1)) {
 				match(COMMA);
 				typeArgument();
 				astFactory.addASTChild(currentAST, returnAST);
 			}
 			else {
 				break _loop33;
 			}
 			
 		} while (true);
 		}
 		{
 		if (((LA(1) >= GT && LA(1) <= BSR)) && (_tokenSet_6.member(LA(2)))) {
 			typeArgumentsOrParametersEnd();
 			astFactory.addASTChild(currentAST, returnAST);
 		}
 		else if ((_tokenSet_6.member(LA(1))) && (_tokenSet_7.member(LA(2)))) {
 		}
 		else {
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		
 		}
 		if (!((currentLtLevel != 0) || ltCounter == currentLtLevel))
 		  throw new SemanticException("(currentLtLevel != 0) || ltCounter == currentLtLevel");
 		if ( inputState.guessing==0 ) {
 			typeArguments_AST = (JavaNode)currentAST.root;
 			typeArguments_AST = (JavaNode)astFactory.make( (new ASTArray(2)).add((JavaNode)astFactory.create(TYPE_ARGUMENTS,"TYPE_ARGUMENTS")).add(typeArguments_AST));
 			currentAST.root = typeArguments_AST;
 			currentAST.child = typeArguments_AST!=null &&typeArguments_AST.getFirstChild()!=null ?
 				typeArguments_AST.getFirstChild() : typeArguments_AST;
 			currentAST.advanceChildToEnd();
 		}
 		typeArguments_AST = (JavaNode)currentAST.root;
 		returnAST = typeArguments_AST;
 	}
 	
 	public final void typeArgumentSpec() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode typeArgumentSpec_AST = null;
 		
 		switch ( LA(1)) {
 		case IDENT:
 		{
 			classTypeSpec(true);
 			astFactory.addASTChild(currentAST, returnAST);
 			typeArgumentSpec_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case LITERAL_void:
 		case LITERAL_boolean:
 		case LITERAL_byte:
 		case LITERAL_char:
 		case LITERAL_short:
 		case LITERAL_int:
 		case LITERAL_float:
 		case LITERAL_long:
 		case LITERAL_double:
 		{
 			builtInTypeArraySpec(true);
 			astFactory.addASTChild(currentAST, returnAST);
 			typeArgumentSpec_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		default:
 		{
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		}
 		returnAST = typeArgumentSpec_AST;
 	}
 	
 	public final void builtInTypeArraySpec(
 		boolean addImagNode
 	) throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode builtInTypeArraySpec_AST = null;
 		Token  lb = null;
 		JavaNode lb_AST = null;
 		
 		builtInType();
 		astFactory.addASTChild(currentAST, returnAST);
 		{
 		int _cnt40=0;
 		_loop40:
 		do {
 			if ((LA(1)==LBRACK) && (LA(2)==RBRACK)) {
 				lb = LT(1);
 				lb_AST = (JavaNode)astFactory.create(lb);
 				astFactory.makeASTRoot(currentAST, lb_AST);
 				match(LBRACK);
 				if ( inputState.guessing==0 ) {
 					lb_AST.setType(ARRAY_DECLARATOR);
 				}
 				match(RBRACK);
 			}
 			else {
 				if ( _cnt40>=1 ) { break _loop40; } else {throw new NoViableAltException(LT(1), getFilename());}
 			}
 			
 			_cnt40++;
 		} while (true);
 		}
 		if ( inputState.guessing==0 ) {
 			builtInTypeArraySpec_AST = (JavaNode)currentAST.root;
 			
 						if ( addImagNode ) {
 							builtInTypeArraySpec_AST = (JavaNode)astFactory.make( (new ASTArray(2)).add((JavaNode)astFactory.create(TYPE,"TYPE")).add(builtInTypeArraySpec_AST));
 						}
 					
 			currentAST.root = builtInTypeArraySpec_AST;
 			currentAST.child = builtInTypeArraySpec_AST!=null &&builtInTypeArraySpec_AST.getFirstChild()!=null ?
 				builtInTypeArraySpec_AST.getFirstChild() : builtInTypeArraySpec_AST;
 			currentAST.advanceChildToEnd();
 		}
 		builtInTypeArraySpec_AST = (JavaNode)currentAST.root;
 		returnAST = builtInTypeArraySpec_AST;
 	}
 	
 	public final void typeArgument() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode typeArgument_AST = null;
 		
 		{
 		switch ( LA(1)) {
 		case IDENT:
 		case LITERAL_void:
 		case LITERAL_boolean:
 		case LITERAL_byte:
 		case LITERAL_char:
 		case LITERAL_short:
 		case LITERAL_int:
 		case LITERAL_float:
 		case LITERAL_long:
 		case LITERAL_double:
 		{
 			typeArgumentSpec();
 			astFactory.addASTChild(currentAST, returnAST);
 			break;
 		}
 		case QUESTION:
 		{
 			wildcardType();
 			astFactory.addASTChild(currentAST, returnAST);
 			break;
 		}
 		default:
 		{
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		}
 		}
 		if ( inputState.guessing==0 ) {
 			typeArgument_AST = (JavaNode)currentAST.root;
 			typeArgument_AST = (JavaNode)astFactory.make( (new ASTArray(2)).add((JavaNode)astFactory.create(TYPE_ARGUMENT,"TYPE_ARGUMENT")).add(typeArgument_AST));
 			currentAST.root = typeArgument_AST;
 			currentAST.child = typeArgument_AST!=null &&typeArgument_AST.getFirstChild()!=null ?
 				typeArgument_AST.getFirstChild() : typeArgument_AST;
 			currentAST.advanceChildToEnd();
 		}
 		typeArgument_AST = (JavaNode)currentAST.root;
 		returnAST = typeArgument_AST;
 	}
 	
 	public final void wildcardType() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode wildcardType_AST = null;
 		Token  q = null;
 		JavaNode q_AST = null;
 		
 		q = LT(1);
 		q_AST = (JavaNode)astFactory.create(q);
 		astFactory.makeASTRoot(currentAST, q_AST);
 		match(QUESTION);
 		if ( inputState.guessing==0 ) {
 			q_AST.setType(WILDCARD_TYPE);
 		}
 		{
 		boolean synPredMatched30 = false;
 		if (((LA(1)==LITERAL_extends||LA(1)==LITERAL_super) && (LA(2)==IDENT))) {
 			int _m30 = mark();
 			synPredMatched30 = true;
 			inputState.guessing++;
 			try {
 				{
 				switch ( LA(1)) {
 				case LITERAL_extends:
 				{
 					match(LITERAL_extends);
 					break;
 				}
 				case LITERAL_super:
 				{
 					match(LITERAL_super);
 					break;
 				}
 				default:
 				{
 					throw new NoViableAltException(LT(1), getFilename());
 				}
 				}
 				}
 			}
 			catch (RecognitionException pe) {
 				synPredMatched30 = false;
 			}
 			rewind(_m30);
 			inputState.guessing--;
 		}
 		if ( synPredMatched30 ) {
 			typeArgumentBounds();
 			astFactory.addASTChild(currentAST, returnAST);
 		}
 		else if ((_tokenSet_6.member(LA(1))) && (_tokenSet_7.member(LA(2)))) {
 		}
 		else {
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		
 		}
 		wildcardType_AST = (JavaNode)currentAST.root;
 		returnAST = wildcardType_AST;
 	}
 	
 	public final void typeArgumentBounds() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode typeArgumentBounds_AST = null;
 		boolean isUpperBounds = false;
 		
 		{
 		switch ( LA(1)) {
 		case LITERAL_extends:
 		{
 			match(LITERAL_extends);
 			if ( inputState.guessing==0 ) {
 				isUpperBounds=true;
 			}
 			break;
 		}
 		case LITERAL_super:
 		{
 			match(LITERAL_super);
 			break;
 		}
 		default:
 		{
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		}
 		}
 		classOrInterfaceType(false);
 		astFactory.addASTChild(currentAST, returnAST);
 		if ( inputState.guessing==0 ) {
 			typeArgumentBounds_AST = (JavaNode)currentAST.root;
 			
 						if (isUpperBounds)
 						{
 							typeArgumentBounds_AST = (JavaNode)astFactory.make( (new ASTArray(2)).add((JavaNode)astFactory.create(TYPE_UPPER_BOUNDS,"TYPE_UPPER_BOUNDS")).add(typeArgumentBounds_AST));
 						}
 						else
 						{
 							typeArgumentBounds_AST = (JavaNode)astFactory.make( (new ASTArray(2)).add((JavaNode)astFactory.create(TYPE_LOWER_BOUNDS,"TYPE_LOWER_BOUNDS")).add(typeArgumentBounds_AST));
 						}
 					
 			currentAST.root = typeArgumentBounds_AST;
 			currentAST.child = typeArgumentBounds_AST!=null &&typeArgumentBounds_AST.getFirstChild()!=null ?
 				typeArgumentBounds_AST.getFirstChild() : typeArgumentBounds_AST;
 			currentAST.advanceChildToEnd();
 		}
 		typeArgumentBounds_AST = (JavaNode)currentAST.root;
 		returnAST = typeArgumentBounds_AST;
 	}
 	
 	protected final void typeArgumentsOrParametersEnd() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode typeArgumentsOrParametersEnd_AST = null;
 		
 		switch ( LA(1)) {
 		case GT:
 		{
 			match(GT);
 			if ( inputState.guessing==0 ) {
 				ltCounter-=1;
 			}
 			typeArgumentsOrParametersEnd_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case SR:
 		{
 			match(SR);
 			if ( inputState.guessing==0 ) {
 				ltCounter-=2;
 			}
 			typeArgumentsOrParametersEnd_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case BSR:
 		{
 			match(BSR);
 			if ( inputState.guessing==0 ) {
 				ltCounter-=3;
 			}
 			typeArgumentsOrParametersEnd_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		default:
 		{
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		}
 		returnAST = typeArgumentsOrParametersEnd_AST;
 	}
 	
 	public final void builtInType() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode builtInType_AST = null;
 		
 		switch ( LA(1)) {
 		case LITERAL_void:
 		{
 			JavaNode tmp33_AST = null;
 			tmp33_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp33_AST);
 			match(LITERAL_void);
 			builtInType_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case LITERAL_boolean:
 		{
 			JavaNode tmp34_AST = null;
 			tmp34_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp34_AST);
 			match(LITERAL_boolean);
 			builtInType_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case LITERAL_byte:
 		{
 			JavaNode tmp35_AST = null;
 			tmp35_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp35_AST);
 			match(LITERAL_byte);
 			builtInType_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case LITERAL_char:
 		{
 			JavaNode tmp36_AST = null;
 			tmp36_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp36_AST);
 			match(LITERAL_char);
 			builtInType_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case LITERAL_short:
 		{
 			JavaNode tmp37_AST = null;
 			tmp37_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp37_AST);
 			match(LITERAL_short);
 			builtInType_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case LITERAL_int:
 		{
 			JavaNode tmp38_AST = null;
 			tmp38_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp38_AST);
 			match(LITERAL_int);
 			builtInType_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case LITERAL_float:
 		{
 			JavaNode tmp39_AST = null;
 			tmp39_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp39_AST);
 			match(LITERAL_float);
 			builtInType_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case LITERAL_long:
 		{
 			JavaNode tmp40_AST = null;
 			tmp40_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp40_AST);
 			match(LITERAL_long);
 			builtInType_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case LITERAL_double:
 		{
 			JavaNode tmp41_AST = null;
 			tmp41_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp41_AST);
 			match(LITERAL_double);
 			builtInType_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		default:
 		{
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		}
 		returnAST = builtInType_AST;
 	}
 	
 	public final void type() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode type_AST = null;
 		
 		switch ( LA(1)) {
 		case IDENT:
 		{
 			classOrInterfaceType(false);
 			astFactory.addASTChild(currentAST, returnAST);
 			type_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case LITERAL_void:
 		case LITERAL_boolean:
 		case LITERAL_byte:
 		case LITERAL_char:
 		case LITERAL_short:
 		case LITERAL_int:
 		case LITERAL_float:
 		case LITERAL_long:
 		case LITERAL_double:
 		{
 			builtInType();
 			astFactory.addASTChild(currentAST, returnAST);
 			type_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		default:
 		{
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		}
 		returnAST = type_AST;
 	}
 	
 	public final void modifier() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode modifier_AST = null;
 		
 		switch ( LA(1)) {
 		case LITERAL_private:
 		{
 			JavaNode tmp42_AST = null;
 			tmp42_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp42_AST);
 			match(LITERAL_private);
 			modifier_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case LITERAL_public:
 		{
 			JavaNode tmp43_AST = null;
 			tmp43_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp43_AST);
 			match(LITERAL_public);
 			modifier_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case LITERAL_protected:
 		{
 			JavaNode tmp44_AST = null;
 			tmp44_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp44_AST);
 			match(LITERAL_protected);
 			modifier_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case LITERAL_static:
 		{
 			JavaNode tmp45_AST = null;
 			tmp45_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp45_AST);
 			match(LITERAL_static);
 			modifier_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case LITERAL_transient:
 		{
 			JavaNode tmp46_AST = null;
 			tmp46_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp46_AST);
 			match(LITERAL_transient);
 			modifier_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case FINAL:
 		{
 			JavaNode tmp47_AST = null;
 			tmp47_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp47_AST);
 			match(FINAL);
 			modifier_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case ABSTRACT:
 		{
 			JavaNode tmp48_AST = null;
 			tmp48_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp48_AST);
 			match(ABSTRACT);
 			modifier_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case LITERAL_native:
 		{
 			JavaNode tmp49_AST = null;
 			tmp49_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp49_AST);
 			match(LITERAL_native);
 			modifier_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case LITERAL_threadsafe:
 		{
 			JavaNode tmp50_AST = null;
 			tmp50_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp50_AST);
 			match(LITERAL_threadsafe);
 			modifier_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case LITERAL_synchronized:
 		{
 			JavaNode tmp51_AST = null;
 			tmp51_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp51_AST);
 			match(LITERAL_synchronized);
 			modifier_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case LITERAL_volatile:
 		{
 			JavaNode tmp52_AST = null;
 			tmp52_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp52_AST);
 			match(LITERAL_volatile);
 			modifier_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case STRICTFP:
 		{
 			JavaNode tmp53_AST = null;
 			tmp53_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp53_AST);
 			match(STRICTFP);
 			modifier_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		default:
 		{
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		}
 		returnAST = modifier_AST;
 	}
 	
 	public final void annotation() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode annotation_AST = null;
 		JavaNode i_AST = null;
 		Token  lp = null;
 		JavaNode lp_AST = null;
 		JavaNode args_AST = null;
 		Token  rp = null;
 		JavaNode rp_AST = null;
 		
 		match(AT);
 		identifier();
 		i_AST = (JavaNode)returnAST;
 		{
 		switch ( LA(1)) {
 		case LPAREN:
 		{
 			lp = LT(1);
 			lp_AST = (JavaNode)astFactory.create(lp);
 			match(LPAREN);
 			{
 			switch ( LA(1)) {
 			case LCURLY:
 			case IDENT:
 			case LITERAL_super:
 			case LT:
 			case LITERAL_void:
 			case LITERAL_boolean:
 			case LITERAL_byte:
 			case LITERAL_char:
 			case LITERAL_short:
 			case LITERAL_int:
 			case LITERAL_float:
 			case LITERAL_long:
 			case LITERAL_double:
 			case AT:
 			case LPAREN:
 			case LITERAL_this:
 			case PLUS:
 			case MINUS:
 			case INC:
 			case DEC:
 			case BNOT:
 			case LNOT:
 			case LITERAL_true:
 			case LITERAL_false:
 			case LITERAL_null:
 			case LITERAL_new:
 			case NUM_INT:
 			case CHAR_LITERAL:
 			case STRING_LITERAL:
 			case NUM_FLOAT:
 			case NUM_LONG:
 			case NUM_DOUBLE:
 			{
 				annotationArguments();
 				args_AST = (JavaNode)returnAST;
 				break;
 			}
 			case RPAREN:
 			{
 				break;
 			}
 			default:
 			{
 				throw new NoViableAltException(LT(1), getFilename());
 			}
 			}
 			}
 			rp = LT(1);
 			rp_AST = (JavaNode)astFactory.create(rp);
 			match(RPAREN);
 			break;
 		}
 		case RCURLY:
 		case FINAL:
 		case ABSTRACT:
 		case STRICTFP:
 		case LITERAL_package:
 		case SEMI:
 		case LITERAL_static:
 		case IDENT:
 		case LT:
 		case COMMA:
 		case LITERAL_void:
 		case LITERAL_boolean:
 		case LITERAL_byte:
 		case LITERAL_char:
 		case LITERAL_short:
 		case LITERAL_int:
 		case LITERAL_float:
 		case LITERAL_long:
 		case LITERAL_double:
 		case LITERAL_private:
 		case LITERAL_public:
 		case LITERAL_protected:
 		case LITERAL_transient:
 		case LITERAL_native:
 		case LITERAL_threadsafe:
 		case LITERAL_synchronized:
 		case LITERAL_volatile:
 		case AT:
 		case RPAREN:
 		case LITERAL_class:
 		case LITERAL_interface:
 		case LITERAL_enum:
 		{
 			break;
 		}
 		default:
 		{
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		}
 		}
 		if ( inputState.guessing==0 ) {
 			annotation_AST = (JavaNode)currentAST.root;
 			annotation_AST = (JavaNode)astFactory.make( (new ASTArray(5)).add((JavaNode)astFactory.create(ANNOTATION,"ANNOTATION")).add(i_AST).add(lp_AST).add(args_AST).add(rp_AST));
 			currentAST.root = annotation_AST;
 			currentAST.child = annotation_AST!=null &&annotation_AST.getFirstChild()!=null ?
 				annotation_AST.getFirstChild() : annotation_AST;
 			currentAST.advanceChildToEnd();
 		}
 		returnAST = annotation_AST;
 	}
 	
 	public final void annotationArguments() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode annotationArguments_AST = null;
 		
 		if ((_tokenSet_8.member(LA(1))) && (_tokenSet_9.member(LA(2)))) {
 			annotationMemberValueInitializer();
 			astFactory.addASTChild(currentAST, returnAST);
 			annotationArguments_AST = (JavaNode)currentAST.root;
 		}
 		else if ((LA(1)==IDENT) && (LA(2)==ASSIGN)) {
 			anntotationMemberValuePairs();
 			astFactory.addASTChild(currentAST, returnAST);
 			annotationArguments_AST = (JavaNode)currentAST.root;
 		}
 		else {
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		
 		returnAST = annotationArguments_AST;
 	}
 	
 	public final void annotationMemberValueInitializer() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode annotationMemberValueInitializer_AST = null;
 		
 		switch ( LA(1)) {
 		case IDENT:
 		case LITERAL_super:
 		case LT:
 		case LITERAL_void:
 		case LITERAL_boolean:
 		case LITERAL_byte:
 		case LITERAL_char:
 		case LITERAL_short:
 		case LITERAL_int:
 		case LITERAL_float:
 		case LITERAL_long:
 		case LITERAL_double:
 		case LPAREN:
 		case LITERAL_this:
 		case PLUS:
 		case MINUS:
 		case INC:
 		case DEC:
 		case BNOT:
 		case LNOT:
 		case LITERAL_true:
 		case LITERAL_false:
 		case LITERAL_null:
 		case LITERAL_new:
 		case NUM_INT:
 		case CHAR_LITERAL:
 		case STRING_LITERAL:
 		case NUM_FLOAT:
 		case NUM_LONG:
 		case NUM_DOUBLE:
 		{
 			conditionalExpression();
 			astFactory.addASTChild(currentAST, returnAST);
 			annotationMemberValueInitializer_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case AT:
 		{
 			annotation();
 			astFactory.addASTChild(currentAST, returnAST);
 			annotationMemberValueInitializer_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case LCURLY:
 		{
 			annotationMemberArrayInitializer();
 			astFactory.addASTChild(currentAST, returnAST);
 			annotationMemberValueInitializer_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		default:
 		{
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		}
 		returnAST = annotationMemberValueInitializer_AST;
 	}
 	
 	public final void anntotationMemberValuePairs() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode anntotationMemberValuePairs_AST = null;
 		
 		annotationMemberValuePair();
 		astFactory.addASTChild(currentAST, returnAST);
 		{
 		_loop66:
 		do {
 			if ((LA(1)==COMMA)) {
 				JavaNode tmp55_AST = null;
 				tmp55_AST = (JavaNode)astFactory.create(LT(1));
 				astFactory.addASTChild(currentAST, tmp55_AST);
 				match(COMMA);
 				annotationMemberValuePair();
 				astFactory.addASTChild(currentAST, returnAST);
 			}
 			else {
 				break _loop66;
 			}
 			
 		} while (true);
 		}
 		anntotationMemberValuePairs_AST = (JavaNode)currentAST.root;
 		returnAST = anntotationMemberValuePairs_AST;
 	}
 	
 	public final void annotationMemberValuePair() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode annotationMemberValuePair_AST = null;
 		Token  i = null;
 		JavaNode i_AST = null;
 		JavaNode v_AST = null;
 		
 		i = LT(1);
 		i_AST = (JavaNode)astFactory.create(i);
 		match(IDENT);
 		match(ASSIGN);
 		annotationMemberValueInitializer();
 		v_AST = (JavaNode)returnAST;
 		if ( inputState.guessing==0 ) {
 			annotationMemberValuePair_AST = (JavaNode)currentAST.root;
 			annotationMemberValuePair_AST = (JavaNode)astFactory.make( (new ASTArray(3)).add((JavaNode)astFactory.create(ANNOTATION_MEMBER_VALUE_PAIR,"ANNOTATION_MEMBER_VALUE_PAIR")).add(i_AST).add(v_AST));
 			currentAST.root = annotationMemberValuePair_AST;
 			currentAST.child = annotationMemberValuePair_AST!=null &&annotationMemberValuePair_AST.getFirstChild()!=null ?
 				annotationMemberValuePair_AST.getFirstChild() : annotationMemberValuePair_AST;
 			currentAST.advanceChildToEnd();
 		}
 		returnAST = annotationMemberValuePair_AST;
 	}
 	
 	public final void conditionalExpression() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode conditionalExpression_AST = null;
 		
 		logicalOrExpression();
 		astFactory.addASTChild(currentAST, returnAST);
 		{
 		switch ( LA(1)) {
 		case QUESTION:
 		{
 			JavaNode tmp57_AST = null;
 			tmp57_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.makeASTRoot(currentAST, tmp57_AST);
 			match(QUESTION);
 			assignmentExpression();
 			astFactory.addASTChild(currentAST, returnAST);
 			JavaNode tmp58_AST = null;
 			tmp58_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp58_AST);
 			match(COLON);
 			conditionalExpression();
 			astFactory.addASTChild(currentAST, returnAST);
 			break;
 		}
 		case LCURLY:
 		case RCURLY:
 		case FINAL:
 		case ABSTRACT:
 		case STRICTFP:
 		case SEMI:
 		case LITERAL_static:
 		case RBRACK:
 		case IDENT:
 		case LT:
 		case COMMA:
 		case LITERAL_void:
 		case LITERAL_boolean:
 		case LITERAL_byte:
 		case LITERAL_char:
 		case LITERAL_short:
 		case LITERAL_int:
 		case LITERAL_float:
 		case LITERAL_long:
 		case LITERAL_double:
 		case LITERAL_private:
 		case LITERAL_public:
 		case LITERAL_protected:
 		case LITERAL_transient:
 		case LITERAL_native:
 		case LITERAL_threadsafe:
 		case LITERAL_synchronized:
 		case LITERAL_volatile:
 		case AT:
 		case RPAREN:
 		case ASSIGN:
 		case LITERAL_class:
 		case LITERAL_interface:
 		case LITERAL_enum:
 		case COLON:
 		case PLUS_ASSIGN:
 		case MINUS_ASSIGN:
 		case STAR_ASSIGN:
 		case DIV_ASSIGN:
 		case MOD_ASSIGN:
 		case SR_ASSIGN:
 		case BSR_ASSIGN:
 		case SL_ASSIGN:
 		case BAND_ASSIGN:
 		case BXOR_ASSIGN:
 		case BOR_ASSIGN:
 		{
 			break;
 		}
 		default:
 		{
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		}
 		}
 		conditionalExpression_AST = (JavaNode)currentAST.root;
 		returnAST = conditionalExpression_AST;
 	}
 	
 	public final void annotationMemberArrayInitializer() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode annotationMemberArrayInitializer_AST = null;
 		Token  lc = null;
 		JavaNode lc_AST = null;
 		
 		lc = LT(1);
 		lc_AST = (JavaNode)astFactory.create(lc);
 		astFactory.makeASTRoot(currentAST, lc_AST);
 		match(LCURLY);
 		if ( inputState.guessing==0 ) {
 			lc_AST.setType(ANNOTATION_ARRAY_INIT);
 		}
 		{
 		switch ( LA(1)) {
 		case IDENT:
 		case LITERAL_super:
 		case LT:
 		case LITERAL_void:
 		case LITERAL_boolean:
 		case LITERAL_byte:
 		case LITERAL_char:
 		case LITERAL_short:
 		case LITERAL_int:
 		case LITERAL_float:
 		case LITERAL_long:
 		case LITERAL_double:
 		case AT:
 		case LPAREN:
 		case LITERAL_this:
 		case PLUS:
 		case MINUS:
 		case INC:
 		case DEC:
 		case BNOT:
 		case LNOT:
 		case LITERAL_true:
 		case LITERAL_false:
 		case LITERAL_null:
 		case LITERAL_new:
 		case NUM_INT:
 		case CHAR_LITERAL:
 		case STRING_LITERAL:
 		case NUM_FLOAT:
 		case NUM_LONG:
 		case NUM_DOUBLE:
 		{
 			annotationMemberArrayValueInitializer();
 			astFactory.addASTChild(currentAST, returnAST);
 			{
 			_loop72:
 			do {
 				if ((LA(1)==COMMA) && (_tokenSet_10.member(LA(2)))) {
 					JavaNode tmp59_AST = null;
 					tmp59_AST = (JavaNode)astFactory.create(LT(1));
 					astFactory.addASTChild(currentAST, tmp59_AST);
 					match(COMMA);
 					annotationMemberArrayValueInitializer();
 					astFactory.addASTChild(currentAST, returnAST);
 				}
 				else {
 					break _loop72;
 				}
 				
 			} while (true);
 			}
 			{
 			switch ( LA(1)) {
 			case COMMA:
 			{
 				JavaNode tmp60_AST = null;
 				tmp60_AST = (JavaNode)astFactory.create(LT(1));
 				astFactory.addASTChild(currentAST, tmp60_AST);
 				match(COMMA);
 				break;
 			}
 			case RCURLY:
 			{
 				break;
 			}
 			default:
 			{
 				throw new NoViableAltException(LT(1), getFilename());
 			}
 			}
 			}
 			break;
 		}
 		case RCURLY:
 		{
 			break;
 		}
 		default:
 		{
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		}
 		}
 		JavaNode tmp61_AST = null;
 		tmp61_AST = (JavaNode)astFactory.create(LT(1));
 		astFactory.addASTChild(currentAST, tmp61_AST);
 		match(RCURLY);
 		annotationMemberArrayInitializer_AST = (JavaNode)currentAST.root;
 		returnAST = annotationMemberArrayInitializer_AST;
 	}
 	
 	public final void annotationMemberArrayValueInitializer() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode annotationMemberArrayValueInitializer_AST = null;
 		
 		switch ( LA(1)) {
 		case IDENT:
 		case LITERAL_super:
 		case LT:
 		case LITERAL_void:
 		case LITERAL_boolean:
 		case LITERAL_byte:
 		case LITERAL_char:
 		case LITERAL_short:
 		case LITERAL_int:
 		case LITERAL_float:
 		case LITERAL_long:
 		case LITERAL_double:
 		case LPAREN:
 		case LITERAL_this:
 		case PLUS:
 		case MINUS:
 		case INC:
 		case DEC:
 		case BNOT:
 		case LNOT:
 		case LITERAL_true:
 		case LITERAL_false:
 		case LITERAL_null:
 		case LITERAL_new:
 		case NUM_INT:
 		case CHAR_LITERAL:
 		case STRING_LITERAL:
 		case NUM_FLOAT:
 		case NUM_LONG:
 		case NUM_DOUBLE:
 		{
 			conditionalExpression();
 			astFactory.addASTChild(currentAST, returnAST);
 			annotationMemberArrayValueInitializer_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case AT:
 		{
 			annotation();
 			astFactory.addASTChild(currentAST, returnAST);
 			annotationMemberArrayValueInitializer_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		default:
 		{
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		}
 		returnAST = annotationMemberArrayValueInitializer_AST;
 	}
 	
 	public final void superClassClause() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode superClassClause_AST = null;
 		JavaNode c_AST = null;
 		
 		{
 		switch ( LA(1)) {
 		case LITERAL_extends:
 		{
 			match(LITERAL_extends);
 			classOrInterfaceType(false);
 			c_AST = (JavaNode)returnAST;
 			break;
 		}
 		case LCURLY:
 		case LITERAL_implements:
 		{
 			break;
 		}
 		default:
 		{
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		}
 		}
 		if ( inputState.guessing==0 ) {
 			superClassClause_AST = (JavaNode)currentAST.root;
 			superClassClause_AST = (JavaNode)astFactory.make( (new ASTArray(2)).add((JavaNode)astFactory.create(EXTENDS_CLAUSE,"EXTENDS_CLAUSE")).add(c_AST));
 			currentAST.root = superClassClause_AST;
 			currentAST.child = superClassClause_AST!=null &&superClassClause_AST.getFirstChild()!=null ?
 				superClassClause_AST.getFirstChild() : superClassClause_AST;
 			currentAST.advanceChildToEnd();
 		}
 		returnAST = superClassClause_AST;
 	}
 	
 	public final void typeParameters() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode typeParameters_AST = null;
 		int currentLtLevel = 0;
 		
 		if ( inputState.guessing==0 ) {
 			currentLtLevel = ltCounter;
 		}
 		JavaNode tmp63_AST = null;
 		tmp63_AST = (JavaNode)astFactory.create(LT(1));
 		astFactory.addASTChild(currentAST, tmp63_AST);
 		match(LT);
 		if ( inputState.guessing==0 ) {
 			ltCounter++;
 		}
 		typeParameter();
 		astFactory.addASTChild(currentAST, returnAST);
 		{
 		_loop85:
 		do {
 			if ((LA(1)==COMMA)) {
				JavaNode tmp64_AST = null;
				tmp64_AST = (JavaNode)astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp64_AST);
 				match(COMMA);
 				typeParameter();
 				astFactory.addASTChild(currentAST, returnAST);
 			}
 			else {
 				break _loop85;
 			}
 			
 		} while (true);
 		}
 		{
 		switch ( LA(1)) {
 		case GT:
 		case SR:
 		case BSR:
 		{
 			typeArgumentsOrParametersEnd();
 			astFactory.addASTChild(currentAST, returnAST);
 			break;
 		}
 		case LCURLY:
 		case IDENT:
 		case LITERAL_extends:
 		case LITERAL_void:
 		case LITERAL_boolean:
 		case LITERAL_byte:
 		case LITERAL_char:
 		case LITERAL_short:
 		case LITERAL_int:
 		case LITERAL_float:
 		case LITERAL_long:
 		case LITERAL_double:
 		case LITERAL_implements:
 		{
 			break;
 		}
 		default:
 		{
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		}
 		}
 		if (!((currentLtLevel != 0) || ltCounter == currentLtLevel))
 		  throw new SemanticException("(currentLtLevel != 0) || ltCounter == currentLtLevel");
 		if ( inputState.guessing==0 ) {
 			typeParameters_AST = (JavaNode)currentAST.root;
 			typeParameters_AST = (JavaNode)astFactory.make( (new ASTArray(2)).add((JavaNode)astFactory.create(TYPE_PARAMETERS,"TYPE_PARAMETERS")).add(typeParameters_AST));
 			currentAST.root = typeParameters_AST;
 			currentAST.child = typeParameters_AST!=null &&typeParameters_AST.getFirstChild()!=null ?
 				typeParameters_AST.getFirstChild() : typeParameters_AST;
 			currentAST.advanceChildToEnd();
 		}
 		typeParameters_AST = (JavaNode)currentAST.root;
 		returnAST = typeParameters_AST;
 	}
 	
 	public final void implementsClause() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode implementsClause_AST = null;
 		Token  i = null;
 		JavaNode i_AST = null;
 		
 		{
 		switch ( LA(1)) {
 		case LITERAL_implements:
 		{
 			i = LT(1);
 			i_AST = (JavaNode)astFactory.create(i);
 			match(LITERAL_implements);
 			classOrInterfaceType(false);
 			astFactory.addASTChild(currentAST, returnAST);
 			{
 			_loop133:
 			do {
 				if ((LA(1)==COMMA)) {
 					JavaNode tmp65_AST = null;
 					tmp65_AST = (JavaNode)astFactory.create(LT(1));
 					astFactory.addASTChild(currentAST, tmp65_AST);
 					match(COMMA);
 					classOrInterfaceType(false);
 					astFactory.addASTChild(currentAST, returnAST);
 				}
 				else {
 					break _loop133;
 				}
 				
 			} while (true);
 			}
 			break;
 		}
 		case LCURLY:
 		{
 			break;
 		}
 		default:
 		{
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		}
 		}
 		if ( inputState.guessing==0 ) {
 			implementsClause_AST = (JavaNode)currentAST.root;
 			implementsClause_AST = (JavaNode)astFactory.make( (new ASTArray(2)).add((JavaNode)astFactory.create(IMPLEMENTS_CLAUSE,"IMPLEMENTS_CLAUSE")).add(implementsClause_AST));
 			currentAST.root = implementsClause_AST;
 			currentAST.child = implementsClause_AST!=null &&implementsClause_AST.getFirstChild()!=null ?
 				implementsClause_AST.getFirstChild() : implementsClause_AST;
 			currentAST.advanceChildToEnd();
 		}
 		implementsClause_AST = (JavaNode)currentAST.root;
 		returnAST = implementsClause_AST;
 	}
 	
 	public final void classBlock() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode classBlock_AST = null;
 		Token  lc = null;
 		JavaNode lc_AST = null;
 		
 		lc = LT(1);
 		lc_AST = (JavaNode)astFactory.create(lc);
 		astFactory.makeASTRoot(currentAST, lc_AST);
 		match(LCURLY);
 		{
 		_loop95:
 		do {
 			switch ( LA(1)) {
 			case LCURLY:
 			case FINAL:
 			case ABSTRACT:
 			case STRICTFP:
 			case LITERAL_static:
 			case IDENT:
 			case LT:
 			case LITERAL_void:
 			case LITERAL_boolean:
 			case LITERAL_byte:
 			case LITERAL_char:
 			case LITERAL_short:
 			case LITERAL_int:
 			case LITERAL_float:
 			case LITERAL_long:
 			case LITERAL_double:
 			case LITERAL_private:
 			case LITERAL_public:
 			case LITERAL_protected:
 			case LITERAL_transient:
 			case LITERAL_native:
 			case LITERAL_threadsafe:
 			case LITERAL_synchronized:
 			case LITERAL_volatile:
 			case AT:
 			case LITERAL_class:
 			case LITERAL_interface:
 			case LITERAL_enum:
 			{
 				classField();
 				astFactory.addASTChild(currentAST, returnAST);
 				break;
 			}
 			case SEMI:
 			{
 				JavaNode tmp66_AST = null;
 				tmp66_AST = (JavaNode)astFactory.create(LT(1));
 				astFactory.addASTChild(currentAST, tmp66_AST);
 				match(SEMI);
 				break;
 			}
 			default:
 			{
 				break _loop95;
 			}
 			}
 		} while (true);
 		}
 		JavaNode tmp67_AST = null;
 		tmp67_AST = (JavaNode)astFactory.create(LT(1));
 		astFactory.addASTChild(currentAST, tmp67_AST);
 		match(RCURLY);
 		if ( inputState.guessing==0 ) {
 			lc_AST.setType(OBJBLOCK);
 		}
 		classBlock_AST = (JavaNode)currentAST.root;
 		returnAST = classBlock_AST;
 	}
 	
 	public final void interfaceExtends() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode interfaceExtends_AST = null;
 		Token  e = null;
 		JavaNode e_AST = null;
 		
 		{
 		switch ( LA(1)) {
 		case LITERAL_extends:
 		{
 			e = LT(1);
 			e_AST = (JavaNode)astFactory.create(e);
 			match(LITERAL_extends);
 			classOrInterfaceType(false);
 			astFactory.addASTChild(currentAST, returnAST);
 			{
 			_loop129:
 			do {
 				if ((LA(1)==COMMA)) {
 					JavaNode tmp68_AST = null;
 					tmp68_AST = (JavaNode)astFactory.create(LT(1));
 					astFactory.addASTChild(currentAST, tmp68_AST);
 					match(COMMA);
 					classOrInterfaceType(false);
 					astFactory.addASTChild(currentAST, returnAST);
 				}
 				else {
 					break _loop129;
 				}
 				
 			} while (true);
 			}
 			break;
 		}
 		case LCURLY:
 		{
 			break;
 		}
 		default:
 		{
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		}
 		}
 		if ( inputState.guessing==0 ) {
 			interfaceExtends_AST = (JavaNode)currentAST.root;
 			interfaceExtends_AST = (JavaNode)astFactory.make( (new ASTArray(2)).add((JavaNode)astFactory.create(EXTENDS_CLAUSE,"EXTENDS_CLAUSE")).add(interfaceExtends_AST));
 			currentAST.root = interfaceExtends_AST;
 			currentAST.child = interfaceExtends_AST!=null &&interfaceExtends_AST.getFirstChild()!=null ?
 				interfaceExtends_AST.getFirstChild() : interfaceExtends_AST;
 			currentAST.advanceChildToEnd();
 		}
 		interfaceExtends_AST = (JavaNode)currentAST.root;
 		returnAST = interfaceExtends_AST;
 	}
 	
 	public final void interfaceBlock() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode interfaceBlock_AST = null;
 		Token  lc = null;
 		JavaNode lc_AST = null;
 		
 		lc = LT(1);
 		lc_AST = (JavaNode)astFactory.create(lc);
 		astFactory.makeASTRoot(currentAST, lc_AST);
 		match(LCURLY);
 		{
 		_loop98:
 		do {
 			switch ( LA(1)) {
 			case FINAL:
 			case ABSTRACT:
 			case STRICTFP:
 			case LITERAL_static:
 			case IDENT:
 			case LT:
 			case LITERAL_void:
 			case LITERAL_boolean:
 			case LITERAL_byte:
 			case LITERAL_char:
 			case LITERAL_short:
 			case LITERAL_int:
 			case LITERAL_float:
 			case LITERAL_long:
 			case LITERAL_double:
 			case LITERAL_private:
 			case LITERAL_public:
 			case LITERAL_protected:
 			case LITERAL_transient:
 			case LITERAL_native:
 			case LITERAL_threadsafe:
 			case LITERAL_synchronized:
 			case LITERAL_volatile:
 			case AT:
 			case LITERAL_class:
 			case LITERAL_interface:
 			case LITERAL_enum:
 			{
 				interfaceField();
 				astFactory.addASTChild(currentAST, returnAST);
 				break;
 			}
 			case SEMI:
 			{
 				JavaNode tmp69_AST = null;
 				tmp69_AST = (JavaNode)astFactory.create(LT(1));
 				astFactory.addASTChild(currentAST, tmp69_AST);
 				match(SEMI);
 				break;
 			}
 			default:
 			{
 				break _loop98;
 			}
 			}
 		} while (true);
 		}
 		JavaNode tmp70_AST = null;
 		tmp70_AST = (JavaNode)astFactory.create(LT(1));
 		astFactory.addASTChild(currentAST, tmp70_AST);
 		match(RCURLY);
 		if ( inputState.guessing==0 ) {
 			lc_AST.setType(OBJBLOCK);
 		}
 		interfaceBlock_AST = (JavaNode)currentAST.root;
 		returnAST = interfaceBlock_AST;
 	}
 	
 	public final void enumBlock() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode enumBlock_AST = null;
 		Token  lc = null;
 		JavaNode lc_AST = null;
 		
 		lc = LT(1);
 		lc_AST = (JavaNode)astFactory.create(lc);
 		astFactory.makeASTRoot(currentAST, lc_AST);
 		match(LCURLY);
 		{
 		switch ( LA(1)) {
 		case IDENT:
 		case AT:
 		{
 			enumConstant();
 			astFactory.addASTChild(currentAST, returnAST);
 			{
 			_loop105:
 			do {
 				if ((LA(1)==COMMA) && (LA(2)==IDENT||LA(2)==AT)) {
 					JavaNode tmp71_AST = null;
 					tmp71_AST = (JavaNode)astFactory.create(LT(1));
 					astFactory.addASTChild(currentAST, tmp71_AST);
 					match(COMMA);
 					enumConstant();
 					astFactory.addASTChild(currentAST, returnAST);
 				}
 				else {
 					break _loop105;
 				}
 				
 			} while (true);
 			}
 			{
 			switch ( LA(1)) {
 			case COMMA:
 			{
 				JavaNode tmp72_AST = null;
 				tmp72_AST = (JavaNode)astFactory.create(LT(1));
 				astFactory.addASTChild(currentAST, tmp72_AST);
 				match(COMMA);
 				break;
 			}
 			case RCURLY:
 			case SEMI:
 			{
 				break;
 			}
 			default:
 			{
 				throw new NoViableAltException(LT(1), getFilename());
 			}
 			}
 			}
 			break;
 		}
 		case RCURLY:
 		case SEMI:
 		{
 			break;
 		}
 		default:
 		{
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		}
 		}
 		{
 		switch ( LA(1)) {
 		case SEMI:
 		{
 			JavaNode tmp73_AST = null;
 			tmp73_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp73_AST);
 			match(SEMI);
 			{
 			_loop109:
 			do {
 				switch ( LA(1)) {
 				case LCURLY:
 				case FINAL:
 				case ABSTRACT:
 				case STRICTFP:
 				case LITERAL_static:
 				case IDENT:
 				case LT:
 				case LITERAL_void:
 				case LITERAL_boolean:
 				case LITERAL_byte:
 				case LITERAL_char:
 				case LITERAL_short:
 				case LITERAL_int:
 				case LITERAL_float:
 				case LITERAL_long:
 				case LITERAL_double:
 				case LITERAL_private:
 				case LITERAL_public:
 				case LITERAL_protected:
 				case LITERAL_transient:
 				case LITERAL_native:
 				case LITERAL_threadsafe:
 				case LITERAL_synchronized:
 				case LITERAL_volatile:
 				case AT:
 				case LITERAL_class:
 				case LITERAL_interface:
 				case LITERAL_enum:
 				{
 					classField();
 					astFactory.addASTChild(currentAST, returnAST);
 					break;
 				}
 				case SEMI:
 				{
 					JavaNode tmp74_AST = null;
 					tmp74_AST = (JavaNode)astFactory.create(LT(1));
 					astFactory.addASTChild(currentAST, tmp74_AST);
 					match(SEMI);
 					break;
 				}
 				default:
 				{
 					break _loop109;
 				}
 				}
 			} while (true);
 			}
 			break;
 		}
 		case RCURLY:
 		{
 			break;
 		}
 		default:
 		{
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		}
 		}
 		JavaNode tmp75_AST = null;
 		tmp75_AST = (JavaNode)astFactory.create(LT(1));
 		astFactory.addASTChild(currentAST, tmp75_AST);
 		match(RCURLY);
 		if ( inputState.guessing==0 ) {
 			lc_AST.setType(OBJBLOCK);
 		}
 		enumBlock_AST = (JavaNode)currentAST.root;
 		returnAST = enumBlock_AST;
 	}
 	
 	public final void annotationBlock() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode annotationBlock_AST = null;
 		Token  lc = null;
 		JavaNode lc_AST = null;
 		
 		lc = LT(1);
 		lc_AST = (JavaNode)astFactory.create(lc);
 		astFactory.makeASTRoot(currentAST, lc_AST);
 		match(LCURLY);
 		{
 		_loop101:
 		do {
 			switch ( LA(1)) {
 			case FINAL:
 			case ABSTRACT:
 			case STRICTFP:
 			case LITERAL_static:
 			case IDENT:
 			case LITERAL_void:
 			case LITERAL_boolean:
 			case LITERAL_byte:
 			case LITERAL_char:
 			case LITERAL_short:
 			case LITERAL_int:
 			case LITERAL_float:
 			case LITERAL_long:
 			case LITERAL_double:
 			case LITERAL_private:
 			case LITERAL_public:
 			case LITERAL_protected:
 			case LITERAL_transient:
 			case LITERAL_native:
 			case LITERAL_threadsafe:
 			case LITERAL_synchronized:
 			case LITERAL_volatile:
 			case AT:
 			case LITERAL_class:
 			case LITERAL_interface:
 			case LITERAL_enum:
 			{
 				annotationField();
 				astFactory.addASTChild(currentAST, returnAST);
 				break;
 			}
 			case SEMI:
 			{
 				JavaNode tmp76_AST = null;
 				tmp76_AST = (JavaNode)astFactory.create(LT(1));
 				astFactory.addASTChild(currentAST, tmp76_AST);
 				match(SEMI);
 				break;
 			}
 			default:
 			{
 				break _loop101;
 			}
 			}
 		} while (true);
 		}
 		JavaNode tmp77_AST = null;
 		tmp77_AST = (JavaNode)astFactory.create(LT(1));
 		astFactory.addASTChild(currentAST, tmp77_AST);
 		match(RCURLY);
 		if ( inputState.guessing==0 ) {
 			lc_AST.setType(OBJBLOCK);
 		}
 		annotationBlock_AST = (JavaNode)currentAST.root;
 		returnAST = annotationBlock_AST;
 	}
 	
 	public final void typeParameter() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode typeParameter_AST = null;
 		Token  id = null;
 		JavaNode id_AST = null;
 		
 		{
 		id = LT(1);
 		id_AST = (JavaNode)astFactory.create(id);
 		astFactory.addASTChild(currentAST, id_AST);
 		match(IDENT);
 		}
 		{
 		if ((LA(1)==LITERAL_extends) && (LA(2)==IDENT)) {
 			typeParameterBounds();
 			astFactory.addASTChild(currentAST, returnAST);
 		}
 		else if ((_tokenSet_11.member(LA(1))) && (_tokenSet_12.member(LA(2)))) {
 		}
 		else {
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		
 		}
 		if ( inputState.guessing==0 ) {
 			typeParameter_AST = (JavaNode)currentAST.root;
 			typeParameter_AST = (JavaNode)astFactory.make( (new ASTArray(2)).add((JavaNode)astFactory.create(TYPE_PARAMETER,"TYPE_PARAMETER")).add(typeParameter_AST));
 			currentAST.root = typeParameter_AST;
 			currentAST.child = typeParameter_AST!=null &&typeParameter_AST.getFirstChild()!=null ?
 				typeParameter_AST.getFirstChild() : typeParameter_AST;
 			currentAST.advanceChildToEnd();
 		}
 		typeParameter_AST = (JavaNode)currentAST.root;
 		returnAST = typeParameter_AST;
 	}
 	
 	public final void typeParameterBounds() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode typeParameterBounds_AST = null;
 		
 		match(LITERAL_extends);
 		classOrInterfaceType(false);
 		astFactory.addASTChild(currentAST, returnAST);
 		{
 		_loop92:
 		do {
 			if ((LA(1)==BAND)) {
 				JavaNode tmp79_AST = null;
 				tmp79_AST = (JavaNode)astFactory.create(LT(1));
 				astFactory.addASTChild(currentAST, tmp79_AST);
 				match(BAND);
 				classOrInterfaceType(false);
 				astFactory.addASTChild(currentAST, returnAST);
 			}
 			else {
 				break _loop92;
 			}
 			
 		} while (true);
 		}
 		if ( inputState.guessing==0 ) {
 			typeParameterBounds_AST = (JavaNode)currentAST.root;
 			typeParameterBounds_AST = (JavaNode)astFactory.make( (new ASTArray(2)).add((JavaNode)astFactory.create(TYPE_UPPER_BOUNDS,"TYPE_UPPER_BOUNDS")).add(typeParameterBounds_AST));
 			currentAST.root = typeParameterBounds_AST;
 			currentAST.child = typeParameterBounds_AST!=null &&typeParameterBounds_AST.getFirstChild()!=null ?
 				typeParameterBounds_AST.getFirstChild() : typeParameterBounds_AST;
 			currentAST.advanceChildToEnd();
 		}
 		typeParameterBounds_AST = (JavaNode)currentAST.root;
 		returnAST = typeParameterBounds_AST;
 	}
 	
 	public final void classField() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode classField_AST = null;
 		JavaNode mods_AST = null;
 		JavaNode td_AST = null;
 		JavaNode tp_AST = null;
 		JavaNode h_AST = null;
 		JavaNode s_AST = null;
 		JavaNode t_AST = null;
 		JavaNode param_AST = null;
 		JavaNode rt_AST = null;
 		JavaNode tc_AST = null;
 		JavaNode s2_AST = null;
 		JavaNode v_AST = null;
 		Token  semi = null;
 		JavaNode semi_AST = null;
 		JavaNode s3_AST = null;
 		JavaNode s4_AST = null;
 		
 		if ((_tokenSet_13.member(LA(1))) && (_tokenSet_14.member(LA(2)))) {
 			modifiers();
 			mods_AST = (JavaNode)returnAST;
 			{
 			switch ( LA(1)) {
 			case AT:
 			case LITERAL_class:
 			case LITERAL_interface:
 			case LITERAL_enum:
 			{
 				typeDefinitionInternal(mods_AST);
 				td_AST = (JavaNode)returnAST;
 				if ( inputState.guessing==0 ) {
 					classField_AST = (JavaNode)currentAST.root;
 					classField_AST = td_AST;
 					currentAST.root = classField_AST;
 					currentAST.child = classField_AST!=null &&classField_AST.getFirstChild()!=null ?
 						classField_AST.getFirstChild() : classField_AST;
 					currentAST.advanceChildToEnd();
 				}
 				break;
 			}
 			case IDENT:
 			case LT:
 			case LITERAL_void:
 			case LITERAL_boolean:
 			case LITERAL_byte:
 			case LITERAL_char:
 			case LITERAL_short:
 			case LITERAL_int:
 			case LITERAL_float:
 			case LITERAL_long:
 			case LITERAL_double:
 			{
 				{
 				switch ( LA(1)) {
 				case LT:
 				{
 					typeParameters();
 					tp_AST = (JavaNode)returnAST;
 					break;
 				}
 				case IDENT:
 				case LITERAL_void:
 				case LITERAL_boolean:
 				case LITERAL_byte:
 				case LITERAL_char:
 				case LITERAL_short:
 				case LITERAL_int:
 				case LITERAL_float:
 				case LITERAL_long:
 				case LITERAL_double:
 				{
 					break;
 				}
 				default:
 				{
 					throw new NoViableAltException(LT(1), getFilename());
 				}
 				}
 				}
 				{
 				if ((LA(1)==IDENT) && (LA(2)==LPAREN)) {
 					ctorHead();
 					h_AST = (JavaNode)returnAST;
 					constructorBody();
 					s_AST = (JavaNode)returnAST;
 					if ( inputState.guessing==0 ) {
 						classField_AST = (JavaNode)currentAST.root;
 						classField_AST = (JavaNode)astFactory.make( (new ASTArray(5)).add((JavaNode)astFactory.create(CTOR_DEF,"CTOR_DEF")).add(mods_AST).add(tp_AST).add(h_AST).add(s_AST));
 										
 						currentAST.root = classField_AST;
 						currentAST.child = classField_AST!=null &&classField_AST.getFirstChild()!=null ?
 							classField_AST.getFirstChild() : classField_AST;
 						currentAST.advanceChildToEnd();
 					}
 				}
 				else if ((_tokenSet_15.member(LA(1))) && (_tokenSet_16.member(LA(2)))) {
 					typeSpec(false);
 					t_AST = (JavaNode)returnAST;
 					{
 					if ((LA(1)==IDENT) && (LA(2)==LPAREN)) {
 						JavaNode tmp80_AST = null;
 						tmp80_AST = (JavaNode)astFactory.create(LT(1));
 						match(IDENT);
 						JavaNode tmp81_AST = null;
 						tmp81_AST = (JavaNode)astFactory.create(LT(1));
 						match(LPAREN);
 						parameterDeclarationList();
 						param_AST = (JavaNode)returnAST;
 						JavaNode tmp82_AST = null;
 						tmp82_AST = (JavaNode)astFactory.create(LT(1));
 						match(RPAREN);
 						declaratorBrackets(t_AST);
 						rt_AST = (JavaNode)returnAST;
 						{
 						switch ( LA(1)) {
 						case LITERAL_throws:
 						{
 							throwsClause();
 							tc_AST = (JavaNode)returnAST;
 							break;
 						}
 						case LCURLY:
 						case SEMI:
 						{
 							break;
 						}
 						default:
 						{
 							throw new NoViableAltException(LT(1), getFilename());
 						}
 						}
 						}
 						{
 						switch ( LA(1)) {
 						case LCURLY:
 						{
 							compoundStatement();
 							s2_AST = (JavaNode)returnAST;
 							break;
 						}
 						case SEMI:
 						{
 							JavaNode tmp83_AST = null;
 							tmp83_AST = (JavaNode)astFactory.create(LT(1));
 							match(SEMI);
 							break;
 						}
 						default:
 						{
 							throw new NoViableAltException(LT(1), getFilename());
 						}
 						}
 						}
 						if ( inputState.guessing==0 ) {
 							classField_AST = (JavaNode)currentAST.root;
 							classField_AST = (JavaNode)astFactory.make( (new ASTArray(10)).add((JavaNode)astFactory.create(METHOD_DEF,"METHOD_DEF")).add(mods_AST).add(tp_AST).add((JavaNode)astFactory.make( (new ASTArray(2)).add((JavaNode)astFactory.create(TYPE,"TYPE")).add(rt_AST))).add(tmp80_AST).add(tmp81_AST).add(param_AST).add(tmp82_AST).add(tc_AST).add(s2_AST));
 									attachStuff(new JavaNode[] {classField_AST, mods_AST, t_AST});
 																
 							currentAST.root = classField_AST;
 							currentAST.child = classField_AST!=null &&classField_AST.getFirstChild()!=null ?
 								classField_AST.getFirstChild() : classField_AST;
 							currentAST.advanceChildToEnd();
 						}
 					}
 					else if ((LA(1)==IDENT) && (_tokenSet_17.member(LA(2)))) {
 						variableDefinitions(mods_AST,t_AST);
 						v_AST = (JavaNode)returnAST;
 						semi = LT(1);
 						semi_AST = (JavaNode)astFactory.create(semi);
 						match(SEMI);
 						if ( inputState.guessing==0 ) {
 							classField_AST = (JavaNode)currentAST.root;
 							
 																classField_AST = v_AST;									
 																
 							classField_AST.addChild(semi_AST);
 							
 							AST next = classField_AST.getNextSibling();
 							// HACK for multiple variable declaration in one statement
 							//      e.g float  x, y, z;
 							// the semicolon will only be added to the first statement so
 							// we have to add it manually to all others
 							if (next != null)
 							{
 							AST ssemi = JavaNodeHelper.getFirstChild(classField_AST, JavaTokenTypes.SEMI);
 							
 							for (AST var = next; var != null; var = var.getNextSibling())
 							{
 							var.addChild(astFactory.create(ssemi));
 							}
 							}
 																
 							
 													
 							currentAST.root = classField_AST;
 							currentAST.child = classField_AST!=null &&classField_AST.getFirstChild()!=null ?
 								classField_AST.getFirstChild() : classField_AST;
 							currentAST.advanceChildToEnd();
 						}
 					}
 					else {
 						throw new NoViableAltException(LT(1), getFilename());
 					}
 					
 					}
 				}
 				else {
 					throw new NoViableAltException(LT(1), getFilename());
 				}
 				
 				}
 				break;
 			}
 			default:
 			{
 				throw new NoViableAltException(LT(1), getFilename());
 			}
 			}
 			}
 		}
 		else if ((LA(1)==LITERAL_static) && (LA(2)==LCURLY)) {
 			match(LITERAL_static);
 			compoundStatement();
 			s3_AST = (JavaNode)returnAST;
 			if ( inputState.guessing==0 ) {
 				classField_AST = (JavaNode)currentAST.root;
 				classField_AST = (JavaNode)astFactory.make( (new ASTArray(2)).add((JavaNode)astFactory.create(STATIC_INIT,"STATIC_INIT")).add(s3_AST));
 						attachStuff(new JavaNode[] {classField_AST, s3_AST});
 						
 				currentAST.root = classField_AST;
 				currentAST.child = classField_AST!=null &&classField_AST.getFirstChild()!=null ?
 					classField_AST.getFirstChild() : classField_AST;
 				currentAST.advanceChildToEnd();
 			}
 		}
 		else if ((LA(1)==LCURLY)) {
 			compoundStatement();
 			s4_AST = (JavaNode)returnAST;
 			if ( inputState.guessing==0 ) {
 				classField_AST = (JavaNode)currentAST.root;
 				classField_AST = (JavaNode)astFactory.make( (new ASTArray(2)).add((JavaNode)astFactory.create(INSTANCE_INIT,"INSTANCE_INIT")).add(s4_AST));
 						attachStuff(new JavaNode[] {classField_AST, s4_AST});
 						
 				currentAST.root = classField_AST;
 				currentAST.child = classField_AST!=null &&classField_AST.getFirstChild()!=null ?
 					classField_AST.getFirstChild() : classField_AST;
 				currentAST.advanceChildToEnd();
 			}
 		}
 		else {
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		
 		returnAST = classField_AST;
 	}
 	
 	public final void interfaceField() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode interfaceField_AST = null;
 		JavaNode mods_AST = null;
 		JavaNode td_AST = null;
 		JavaNode tp_AST = null;
 		JavaNode t_AST = null;
 		JavaNode param_AST = null;
 		JavaNode rt_AST = null;
 		JavaNode tc_AST = null;
 		JavaNode v_AST = null;
 		Token  semi = null;
 		JavaNode semi_AST = null;
 		
 		modifiers();
 		mods_AST = (JavaNode)returnAST;
 		{
 		switch ( LA(1)) {
 		case AT:
 		case LITERAL_class:
 		case LITERAL_interface:
 		case LITERAL_enum:
 		{
 			typeDefinitionInternal(mods_AST);
 			td_AST = (JavaNode)returnAST;
 			if ( inputState.guessing==0 ) {
 				interfaceField_AST = (JavaNode)currentAST.root;
 				interfaceField_AST = td_AST;
 				currentAST.root = interfaceField_AST;
 				currentAST.child = interfaceField_AST!=null &&interfaceField_AST.getFirstChild()!=null ?
 					interfaceField_AST.getFirstChild() : interfaceField_AST;
 				currentAST.advanceChildToEnd();
 			}
 			break;
 		}
 		case IDENT:
 		case LT:
 		case LITERAL_void:
 		case LITERAL_boolean:
 		case LITERAL_byte:
 		case LITERAL_char:
 		case LITERAL_short:
 		case LITERAL_int:
 		case LITERAL_float:
 		case LITERAL_long:
 		case LITERAL_double:
 		{
 			{
 			switch ( LA(1)) {
 			case LT:
 			{
 				typeParameters();
 				tp_AST = (JavaNode)returnAST;
 				break;
 			}
 			case IDENT:
 			case LITERAL_void:
 			case LITERAL_boolean:
 			case LITERAL_byte:
 			case LITERAL_char:
 			case LITERAL_short:
 			case LITERAL_int:
 			case LITERAL_float:
 			case LITERAL_long:
 			case LITERAL_double:
 			{
 				break;
 			}
 			default:
 			{
 				throw new NoViableAltException(LT(1), getFilename());
 			}
 			}
 			}
 			typeSpec(false);
 			t_AST = (JavaNode)returnAST;
 			{
 			if ((LA(1)==IDENT) && (LA(2)==LPAREN)) {
 				JavaNode tmp85_AST = null;
 				tmp85_AST = (JavaNode)astFactory.create(LT(1));
 				match(IDENT);
 				JavaNode tmp86_AST = null;
 				tmp86_AST = (JavaNode)astFactory.create(LT(1));
 				match(LPAREN);
 				parameterDeclarationList();
 				param_AST = (JavaNode)returnAST;
 				JavaNode tmp87_AST = null;
 				tmp87_AST = (JavaNode)astFactory.create(LT(1));
 				match(RPAREN);
 				declaratorBrackets(t_AST);
 				rt_AST = (JavaNode)returnAST;
 				{
 				switch ( LA(1)) {
 				case LITERAL_throws:
 				{
 					throwsClause();
 					tc_AST = (JavaNode)returnAST;
 					break;
 				}
 				case SEMI:
 				{
 					break;
 				}
 				default:
 				{
 					throw new NoViableAltException(LT(1), getFilename());
 				}
 				}
 				}
 				JavaNode tmp88_AST = null;
 				tmp88_AST = (JavaNode)astFactory.create(LT(1));
 				match(SEMI);
 				if ( inputState.guessing==0 ) {
 					interfaceField_AST = (JavaNode)currentAST.root;
 					interfaceField_AST = (JavaNode)astFactory.make( (new ASTArray(10)).add((JavaNode)astFactory.create(METHOD_DEF,"METHOD_DEF")).add(mods_AST).add(tp_AST).add((JavaNode)astFactory.make( (new ASTArray(2)).add((JavaNode)astFactory.create(TYPE,"TYPE")).add(rt_AST))).add(tmp85_AST).add(tmp86_AST).add(param_AST).add(tmp87_AST).add(tc_AST).add(tmp88_AST));
 							attachStuff(new JavaNode[] {interfaceField_AST, mods_AST, t_AST});
 												
 					currentAST.root = interfaceField_AST;
 					currentAST.child = interfaceField_AST!=null &&interfaceField_AST.getFirstChild()!=null ?
 						interfaceField_AST.getFirstChild() : interfaceField_AST;
 					currentAST.advanceChildToEnd();
 				}
 			}
 			else if ((LA(1)==IDENT) && (_tokenSet_17.member(LA(2)))) {
 				variableDefinitions(mods_AST,t_AST);
 				v_AST = (JavaNode)returnAST;
 				semi = LT(1);
 				semi_AST = (JavaNode)astFactory.create(semi);
 				match(SEMI);
 				if ( inputState.guessing==0 ) {
 					interfaceField_AST = (JavaNode)currentAST.root;
 					
 										interfaceField_AST = v_AST;
 										
 										interfaceField_AST.addChild(semi_AST);
 					
 										AST next = interfaceField_AST.getNextSibling();
 										// HACK for multiple variable declaration in one statement
 										//      e.g float  x, y, z;
 										// the semicolon will only be added to the first statement so
 										// we have to add it manually to all others
 										if (next != null)
 										{
 											AST ssemi = JavaNodeHelper.getFirstChild(interfaceField_AST, JavaTokenTypes.SEMI);
 					
 											for (AST var = next; var != null; var = var.getNextSibling())
 											{
 												var.addChild(astFactory.create(ssemi));
 											}
 										}
 										
 					
 									
 					currentAST.root = interfaceField_AST;
 					currentAST.child = interfaceField_AST!=null &&interfaceField_AST.getFirstChild()!=null ?
 						interfaceField_AST.getFirstChild() : interfaceField_AST;
 					currentAST.advanceChildToEnd();
 				}
 			}
 			else {
 				throw new NoViableAltException(LT(1), getFilename());
 			}
 			
 			}
 			break;
 		}
 		default:
 		{
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		}
 		}
 		returnAST = interfaceField_AST;
 	}
 	
 	public final void annotationField() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode annotationField_AST = null;
 		JavaNode mods_AST = null;
 		JavaNode td_AST = null;
 		JavaNode t_AST = null;
 		Token  i = null;
 		JavaNode i_AST = null;
 		JavaNode rt_AST = null;
 		JavaNode amvi_AST = null;
 		JavaNode v_AST = null;
 		
 		modifiers();
 		mods_AST = (JavaNode)returnAST;
 		{
 		switch ( LA(1)) {
 		case AT:
 		case LITERAL_class:
 		case LITERAL_interface:
 		case LITERAL_enum:
 		{
 			typeDefinitionInternal(mods_AST);
 			td_AST = (JavaNode)returnAST;
 			if ( inputState.guessing==0 ) {
 				annotationField_AST = (JavaNode)currentAST.root;
 				annotationField_AST = td_AST;
 				currentAST.root = annotationField_AST;
 				currentAST.child = annotationField_AST!=null &&annotationField_AST.getFirstChild()!=null ?
 					annotationField_AST.getFirstChild() : annotationField_AST;
 				currentAST.advanceChildToEnd();
 			}
 			break;
 		}
 		case IDENT:
 		case LITERAL_void:
 		case LITERAL_boolean:
 		case LITERAL_byte:
 		case LITERAL_char:
 		case LITERAL_short:
 		case LITERAL_int:
 		case LITERAL_float:
 		case LITERAL_long:
 		case LITERAL_double:
 		{
 			typeSpec(false);
 			t_AST = (JavaNode)returnAST;
 			{
 			if ((LA(1)==IDENT) && (LA(2)==LPAREN)) {
 				i = LT(1);
 				i_AST = (JavaNode)astFactory.create(i);
 				match(IDENT);
 				JavaNode tmp89_AST = null;
 				tmp89_AST = (JavaNode)astFactory.create(LT(1));
 				match(LPAREN);
 				JavaNode tmp90_AST = null;
 				tmp90_AST = (JavaNode)astFactory.create(LT(1));
 				match(RPAREN);
 				declaratorBrackets(t_AST);
 				rt_AST = (JavaNode)returnAST;
 				{
 				switch ( LA(1)) {
 				case LITERAL_default:
 				{
 					match(LITERAL_default);
 					annotationMemberValueInitializer();
 					amvi_AST = (JavaNode)returnAST;
 					break;
 				}
 				case SEMI:
 				{
 					break;
 				}
 				default:
 				{
 					throw new NoViableAltException(LT(1), getFilename());
 				}
 				}
 				}
 				JavaNode tmp92_AST = null;
 				tmp92_AST = (JavaNode)astFactory.create(LT(1));
 				match(SEMI);
 				if ( inputState.guessing==0 ) {
 					annotationField_AST = (JavaNode)currentAST.root;
 					annotationField_AST =
 										(JavaNode)astFactory.make( (new ASTArray(8)).add((JavaNode)astFactory.create(ANNOTATION_FIELD_DEF,"ANNOTATION_FIELD_DEF")).add(mods_AST).add((JavaNode)astFactory.make( (new ASTArray(2)).add((JavaNode)astFactory.create(TYPE,"TYPE")).add(rt_AST))).add(i_AST).add(tmp89_AST).add(tmp90_AST).add(amvi_AST).add(tmp92_AST));
 							attachStuff(new JavaNode[] {annotationField_AST, mods_AST, t_AST});
 											
 					currentAST.root = annotationField_AST;
 					currentAST.child = annotationField_AST!=null &&annotationField_AST.getFirstChild()!=null ?
 						annotationField_AST.getFirstChild() : annotationField_AST;
 					currentAST.advanceChildToEnd();
 				}
 			}
 			else if ((LA(1)==IDENT) && (_tokenSet_18.member(LA(2)))) {
 				variableDefinitions(mods_AST,t_AST);
 				v_AST = (JavaNode)returnAST;
 				if ( inputState.guessing==0 ) {
 					annotationField_AST = (JavaNode)currentAST.root;
 					
 					annotationField_AST = v_AST;
 					
 					currentAST.root = annotationField_AST;
 					currentAST.child = annotationField_AST!=null &&annotationField_AST.getFirstChild()!=null ?
 						annotationField_AST.getFirstChild() : annotationField_AST;
 					currentAST.advanceChildToEnd();
 				}
 			}
 			else {
 				throw new NoViableAltException(LT(1), getFilename());
 			}
 			
 			}
 			break;
 		}
 		default:
 		{
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		}
 		}
 		returnAST = annotationField_AST;
 	}
 	
 	public final void enumConstant() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode enumConstant_AST = null;
 		JavaNode an_AST = null;
 		Token  i = null;
 		JavaNode i_AST = null;
 		Token  lp = null;
 		JavaNode lp_AST = null;
 		JavaNode a_AST = null;
 		Token  rp = null;
 		JavaNode rp_AST = null;
 		JavaNode b_AST = null;
 		
 		annotations();
 		an_AST = (JavaNode)returnAST;
 		i = LT(1);
 		i_AST = (JavaNode)astFactory.create(i);
 		match(IDENT);
 		{
 		switch ( LA(1)) {
 		case LPAREN:
 		{
 			lp = LT(1);
 			lp_AST = (JavaNode)astFactory.create(lp);
 			match(LPAREN);
 			argList();
 			a_AST = (JavaNode)returnAST;
 			rp = LT(1);
 			rp_AST = (JavaNode)astFactory.create(rp);
 			match(RPAREN);
 			break;
 		}
 		case LCURLY:
 		case RCURLY:
 		case SEMI:
 		case COMMA:
 		{
 			break;
 		}
 		default:
 		{
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		}
 		}
 		{
 		switch ( LA(1)) {
 		case LCURLY:
 		{
 			enumConstantBlock();
 			b_AST = (JavaNode)returnAST;
 			break;
 		}
 		case RCURLY:
 		case SEMI:
 		case COMMA:
 		{
 			break;
 		}
 		default:
 		{
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		}
 		}
 		if ( inputState.guessing==0 ) {
 			enumConstant_AST = (JavaNode)currentAST.root;
 			enumConstant_AST = (JavaNode)astFactory.make( (new ASTArray(7)).add((JavaNode)astFactory.create(ENUM_CONSTANT_DEF,"ENUM_CONSTANT_DEF")).add(an_AST).add(i_AST).add(lp_AST).add(a_AST).add(rp_AST).add(b_AST));
 			currentAST.root = enumConstant_AST;
 			currentAST.child = enumConstant_AST!=null &&enumConstant_AST.getFirstChild()!=null ?
 				enumConstant_AST.getFirstChild() : enumConstant_AST;
 			currentAST.advanceChildToEnd();
 		}
 		returnAST = enumConstant_AST;
 	}
 	
 	public final void declaratorBrackets(
 		JavaNode typ
 	) throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode declaratorBrackets_AST = null;
 		Token  lb = null;
 		JavaNode lb_AST = null;
 		
 		if ( inputState.guessing==0 ) {
 			declaratorBrackets_AST = (JavaNode)currentAST.root;
 			declaratorBrackets_AST=typ;
 			currentAST.root = declaratorBrackets_AST;
 			currentAST.child = declaratorBrackets_AST!=null &&declaratorBrackets_AST.getFirstChild()!=null ?
 				declaratorBrackets_AST.getFirstChild() : declaratorBrackets_AST;
 			currentAST.advanceChildToEnd();
 		}
 		{
 		_loop161:
 		do {
 			if ((LA(1)==LBRACK)) {
 				lb = LT(1);
 				lb_AST = (JavaNode)astFactory.create(lb);
 				astFactory.makeASTRoot(currentAST, lb_AST);
 				match(LBRACK);
 				if ( inputState.guessing==0 ) {
 					lb_AST.setType(ARRAY_DECLARATOR);
 				}
 				match(RBRACK);
 			}
 			else {
 				break _loop161;
 			}
 			
 		} while (true);
 		}
 		declaratorBrackets_AST = (JavaNode)currentAST.root;
 		returnAST = declaratorBrackets_AST;
 	}
 	
 	public final void argList() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode argList_AST = null;
 		
 		{
 		switch ( LA(1)) {
 		case IDENT:
 		case LITERAL_super:
 		case LT:
 		case LITERAL_void:
 		case LITERAL_boolean:
 		case LITERAL_byte:
 		case LITERAL_char:
 		case LITERAL_short:
 		case LITERAL_int:
 		case LITERAL_float:
 		case LITERAL_long:
 		case LITERAL_double:
 		case LPAREN:
 		case LITERAL_this:
 		case PLUS:
 		case MINUS:
 		case INC:
 		case DEC:
 		case BNOT:
 		case LNOT:
 		case LITERAL_true:
 		case LITERAL_false:
 		case LITERAL_null:
 		case LITERAL_new:
 		case NUM_INT:
 		case CHAR_LITERAL:
 		case STRING_LITERAL:
 		case NUM_FLOAT:
 		case NUM_LONG:
 		case NUM_DOUBLE:
 		{
 			expressionList();
 			astFactory.addASTChild(currentAST, returnAST);
 			break;
 		}
 		case RPAREN:
 		{
 			if ( inputState.guessing==0 ) {
 				argList_AST = (JavaNode)currentAST.root;
 				argList_AST = (JavaNode)astFactory.create(ELIST,"ELIST");
 				currentAST.root = argList_AST;
 				currentAST.child = argList_AST!=null &&argList_AST.getFirstChild()!=null ?
 					argList_AST.getFirstChild() : argList_AST;
 				currentAST.advanceChildToEnd();
 			}
 			break;
 		}
 		default:
 		{
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		}
 		}
 		argList_AST = (JavaNode)currentAST.root;
 		returnAST = argList_AST;
 	}
 	
 	public final void enumConstantBlock() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode enumConstantBlock_AST = null;
 		
 		JavaNode tmp94_AST = null;
 		tmp94_AST = (JavaNode)astFactory.create(LT(1));
 		astFactory.addASTChild(currentAST, tmp94_AST);
 		match(LCURLY);
 		{
 		_loop119:
 		do {
 			switch ( LA(1)) {
 			case LCURLY:
 			case FINAL:
 			case ABSTRACT:
 			case STRICTFP:
 			case LITERAL_static:
 			case IDENT:
 			case LT:
 			case LITERAL_void:
 			case LITERAL_boolean:
 			case LITERAL_byte:
 			case LITERAL_char:
 			case LITERAL_short:
 			case LITERAL_int:
 			case LITERAL_float:
 			case LITERAL_long:
 			case LITERAL_double:
 			case LITERAL_private:
 			case LITERAL_public:
 			case LITERAL_protected:
 			case LITERAL_transient:
 			case LITERAL_native:
 			case LITERAL_threadsafe:
 			case LITERAL_synchronized:
 			case LITERAL_volatile:
 			case AT:
 			case LITERAL_class:
 			case LITERAL_interface:
 			case LITERAL_enum:
 			{
 				enumConstantField();
 				astFactory.addASTChild(currentAST, returnAST);
 				break;
 			}
 			case SEMI:
 			{
 				JavaNode tmp95_AST = null;
 				tmp95_AST = (JavaNode)astFactory.create(LT(1));
 				astFactory.addASTChild(currentAST, tmp95_AST);
 				match(SEMI);
 				break;
 			}
 			default:
 			{
 				break _loop119;
 			}
 			}
 		} while (true);
 		}
 		JavaNode tmp96_AST = null;
 		tmp96_AST = (JavaNode)astFactory.create(LT(1));
 		astFactory.addASTChild(currentAST, tmp96_AST);
 		match(RCURLY);
 		if ( inputState.guessing==0 ) {
 			enumConstantBlock_AST = (JavaNode)currentAST.root;
 			enumConstantBlock_AST = (JavaNode)astFactory.make( (new ASTArray(2)).add((JavaNode)astFactory.create(OBJBLOCK,"OBJBLOCK")).add(enumConstantBlock_AST));
 			currentAST.root = enumConstantBlock_AST;
 			currentAST.child = enumConstantBlock_AST!=null &&enumConstantBlock_AST.getFirstChild()!=null ?
 				enumConstantBlock_AST.getFirstChild() : enumConstantBlock_AST;
 			currentAST.advanceChildToEnd();
 		}
 		enumConstantBlock_AST = (JavaNode)currentAST.root;
 		returnAST = enumConstantBlock_AST;
 	}
 	
 	public final void enumConstantField() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode enumConstantField_AST = null;
 		JavaNode mods_AST = null;
 		JavaNode td_AST = null;
 		JavaNode tp_AST = null;
 		JavaNode t_AST = null;
 		JavaNode param_AST = null;
 		JavaNode rt_AST = null;
 		JavaNode tc_AST = null;
 		JavaNode s2_AST = null;
 		JavaNode v_AST = null;
 		JavaNode s4_AST = null;
 		
 		switch ( LA(1)) {
 		case FINAL:
 		case ABSTRACT:
 		case STRICTFP:
 		case LITERAL_static:
 		case IDENT:
 		case LT:
 		case LITERAL_void:
 		case LITERAL_boolean:
 		case LITERAL_byte:
 		case LITERAL_char:
 		case LITERAL_short:
 		case LITERAL_int:
 		case LITERAL_float:
 		case LITERAL_long:
 		case LITERAL_double:
 		case LITERAL_private:
 		case LITERAL_public:
 		case LITERAL_protected:
 		case LITERAL_transient:
 		case LITERAL_native:
 		case LITERAL_threadsafe:
 		case LITERAL_synchronized:
 		case LITERAL_volatile:
 		case AT:
 		case LITERAL_class:
 		case LITERAL_interface:
 		case LITERAL_enum:
 		{
 			modifiers();
 			mods_AST = (JavaNode)returnAST;
 			{
 			switch ( LA(1)) {
 			case AT:
 			case LITERAL_class:
 			case LITERAL_interface:
 			case LITERAL_enum:
 			{
 				typeDefinitionInternal(mods_AST);
 				td_AST = (JavaNode)returnAST;
 				if ( inputState.guessing==0 ) {
 					enumConstantField_AST = (JavaNode)currentAST.root;
 					enumConstantField_AST = td_AST;
 					currentAST.root = enumConstantField_AST;
 					currentAST.child = enumConstantField_AST!=null &&enumConstantField_AST.getFirstChild()!=null ?
 						enumConstantField_AST.getFirstChild() : enumConstantField_AST;
 					currentAST.advanceChildToEnd();
 				}
 				break;
 			}
 			case IDENT:
 			case LT:
 			case LITERAL_void:
 			case LITERAL_boolean:
 			case LITERAL_byte:
 			case LITERAL_char:
 			case LITERAL_short:
 			case LITERAL_int:
 			case LITERAL_float:
 			case LITERAL_long:
 			case LITERAL_double:
 			{
 				{
 				switch ( LA(1)) {
 				case LT:
 				{
 					typeParameters();
 					tp_AST = (JavaNode)returnAST;
 					break;
 				}
 				case IDENT:
 				case LITERAL_void:
 				case LITERAL_boolean:
 				case LITERAL_byte:
 				case LITERAL_char:
 				case LITERAL_short:
 				case LITERAL_int:
 				case LITERAL_float:
 				case LITERAL_long:
 				case LITERAL_double:
 				{
 					break;
 				}
 				default:
 				{
 					throw new NoViableAltException(LT(1), getFilename());
 				}
 				}
 				}
 				typeSpec(false);
 				t_AST = (JavaNode)returnAST;
 				{
 				if ((LA(1)==IDENT) && (LA(2)==LPAREN)) {
 					JavaNode tmp97_AST = null;
 					tmp97_AST = (JavaNode)astFactory.create(LT(1));
 					match(IDENT);
 					JavaNode tmp98_AST = null;
 					tmp98_AST = (JavaNode)astFactory.create(LT(1));
 					match(LPAREN);
 					parameterDeclarationList();
 					param_AST = (JavaNode)returnAST;
 					JavaNode tmp99_AST = null;
 					tmp99_AST = (JavaNode)astFactory.create(LT(1));
 					match(RPAREN);
 					declaratorBrackets(t_AST);
 					rt_AST = (JavaNode)returnAST;
 					{
 					switch ( LA(1)) {
 					case LITERAL_throws:
 					{
 						throwsClause();
 						tc_AST = (JavaNode)returnAST;
 						break;
 					}
 					case LCURLY:
 					case SEMI:
 					{
 						break;
 					}
 					default:
 					{
 						throw new NoViableAltException(LT(1), getFilename());
 					}
 					}
 					}
 					{
 					switch ( LA(1)) {
 					case LCURLY:
 					{
 						compoundStatement();
 						s2_AST = (JavaNode)returnAST;
 						break;
 					}
 					case SEMI:
 					{
 						JavaNode tmp100_AST = null;
 						tmp100_AST = (JavaNode)astFactory.create(LT(1));
 						match(SEMI);
 						break;
 					}
 					default:
 					{
 						throw new NoViableAltException(LT(1), getFilename());
 					}
 					}
 					}
 					if ( inputState.guessing==0 ) {
 						enumConstantField_AST = (JavaNode)currentAST.root;
 						enumConstantField_AST = (JavaNode)astFactory.make( (new ASTArray(8)).add((JavaNode)astFactory.create(METHOD_DEF,"METHOD_DEF")).add(mods_AST).add(tp_AST).add((JavaNode)astFactory.make( (new ASTArray(2)).add((JavaNode)astFactory.create(TYPE,"TYPE")).add(rt_AST))).add(tmp97_AST).add(param_AST).add(tc_AST).add(s2_AST));
 								attachStuff(new JavaNode[] {enumConstantField_AST, mods_AST, t_AST});
 													
 						currentAST.root = enumConstantField_AST;
 						currentAST.child = enumConstantField_AST!=null &&enumConstantField_AST.getFirstChild()!=null ?
 							enumConstantField_AST.getFirstChild() : enumConstantField_AST;
 						currentAST.advanceChildToEnd();
 					}
 				}
 				else if ((LA(1)==IDENT) && (_tokenSet_19.member(LA(2)))) {
 					variableDefinitions(mods_AST,t_AST);
 					v_AST = (JavaNode)returnAST;
 					if ( inputState.guessing==0 ) {
 						enumConstantField_AST = (JavaNode)currentAST.root;
 						enumConstantField_AST = v_AST;
 						
 						currentAST.root = enumConstantField_AST;
 						currentAST.child = enumConstantField_AST!=null &&enumConstantField_AST.getFirstChild()!=null ?
 							enumConstantField_AST.getFirstChild() : enumConstantField_AST;
 						currentAST.advanceChildToEnd();
 					}
 				}
 				else {
 					throw new NoViableAltException(LT(1), getFilename());
 				}
 				
 				}
 				break;
 			}
 			default:
 			{
 				throw new NoViableAltException(LT(1), getFilename());
 			}
 			}
 			}
 			break;
 		}
 		case LCURLY:
 		{
 			compoundStatement();
 			s4_AST = (JavaNode)returnAST;
 			if ( inputState.guessing==0 ) {
 				enumConstantField_AST = (JavaNode)currentAST.root;
 				enumConstantField_AST = (JavaNode)astFactory.make( (new ASTArray(2)).add((JavaNode)astFactory.create(INSTANCE_INIT,"INSTANCE_INIT")).add(s4_AST));
 						attachStuff(new JavaNode[] {enumConstantField_AST, s4_AST});
 						
 				currentAST.root = enumConstantField_AST;
 				currentAST.child = enumConstantField_AST!=null &&enumConstantField_AST.getFirstChild()!=null ?
 					enumConstantField_AST.getFirstChild() : enumConstantField_AST;
 				currentAST.advanceChildToEnd();
 			}
 			break;
 		}
 		default:
 		{
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		}
 		returnAST = enumConstantField_AST;
 	}
 	
 	public final void parameterDeclarationList() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode parameterDeclarationList_AST = null;
 		
 		{
 		boolean synPredMatched178 = false;
 		if (((_tokenSet_20.member(LA(1))) && (_tokenSet_21.member(LA(2))))) {
 			int _m178 = mark();
 			synPredMatched178 = true;
 			inputState.guessing++;
 			try {
 				{
 				parameterDeclaration();
 				}
 			}
 			catch (RecognitionException pe) {
 				synPredMatched178 = false;
 			}
 			rewind(_m178);
 			inputState.guessing--;
 		}
 		if ( synPredMatched178 ) {
 			parameterDeclaration();
 			astFactory.addASTChild(currentAST, returnAST);
 			{
 			_loop182:
 			do {
 				boolean synPredMatched181 = false;
 				if (((LA(1)==COMMA) && (_tokenSet_20.member(LA(2))))) {
 					int _m181 = mark();
 					synPredMatched181 = true;
 					inputState.guessing++;
 					try {
 						{
 						match(COMMA);
 						parameterDeclaration();
 						}
 					}
 					catch (RecognitionException pe) {
 						synPredMatched181 = false;
 					}
 					rewind(_m181);
 					inputState.guessing--;
 				}
 				if ( synPredMatched181 ) {
					JavaNode tmp101_AST = null;
					tmp101_AST = (JavaNode)astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp101_AST);
 					match(COMMA);
 					parameterDeclaration();
 					astFactory.addASTChild(currentAST, returnAST);
 				}
 				else {
 					break _loop182;
 				}
 				
 			} while (true);
 			}
 			{
 			switch ( LA(1)) {
 			case COMMA:
 			{
				JavaNode tmp102_AST = null;
				tmp102_AST = (JavaNode)astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp102_AST);
 				match(COMMA);
 				variableLengthParameterDeclaration();
 				astFactory.addASTChild(currentAST, returnAST);
 				break;
 			}
 			case RPAREN:
 			{
 				break;
 			}
 			default:
 			{
 				throw new NoViableAltException(LT(1), getFilename());
 			}
 			}
 			}
 		}
 		else if ((_tokenSet_20.member(LA(1))) && (_tokenSet_22.member(LA(2)))) {
 			variableLengthParameterDeclaration();
 			astFactory.addASTChild(currentAST, returnAST);
 		}
 		else if ((LA(1)==RPAREN)) {
 		}
 		else {
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		
 		}
 		if ( inputState.guessing==0 ) {
 			parameterDeclarationList_AST = (JavaNode)currentAST.root;
 			parameterDeclarationList_AST = (JavaNode)astFactory.make( (new ASTArray(2)).add((JavaNode)astFactory.create(PARAMETERS,"PARAMETERS")).add(parameterDeclarationList_AST));
 			currentAST.root = parameterDeclarationList_AST;
 			currentAST.child = parameterDeclarationList_AST!=null &&parameterDeclarationList_AST.getFirstChild()!=null ?
 				parameterDeclarationList_AST.getFirstChild() : parameterDeclarationList_AST;
 			currentAST.advanceChildToEnd();
 		}
 		parameterDeclarationList_AST = (JavaNode)currentAST.root;
 		returnAST = parameterDeclarationList_AST;
 	}
 	
 	public final void throwsClause() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode throwsClause_AST = null;
 		
 		JavaNode tmp103_AST = null;
 		tmp103_AST = (JavaNode)astFactory.create(LT(1));
 		astFactory.makeASTRoot(currentAST, tmp103_AST);
 		match(LITERAL_throws);
 		identifier();
 		astFactory.addASTChild(currentAST, returnAST);
 		{
 		_loop174:
 		do {
 			if ((LA(1)==COMMA)) {
 				JavaNode tmp104_AST = null;
 				tmp104_AST = (JavaNode)astFactory.create(LT(1));
 				astFactory.addASTChild(currentAST, tmp104_AST);
 				match(COMMA);
 				identifier();
 				astFactory.addASTChild(currentAST, returnAST);
 			}
 			else {
 				break _loop174;
 			}
 			
 		} while (true);
 		}
 		throwsClause_AST = (JavaNode)currentAST.root;
 		returnAST = throwsClause_AST;
 	}
 	
 	public final void compoundStatement() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode compoundStatement_AST = null;
 		Token  lc = null;
 		JavaNode lc_AST = null;
 		
 		lc = LT(1);
 		lc_AST = (JavaNode)astFactory.create(lc);
 		astFactory.makeASTRoot(currentAST, lc_AST);
 		match(LCURLY);
 		if ( inputState.guessing==0 ) {
 			lc_AST.setType(SLIST);
 		}
 		{
 		_loop194:
 		do {
 			if ((_tokenSet_23.member(LA(1)))) {
 				statement();
 				astFactory.addASTChild(currentAST, returnAST);
 			}
 			else {
 				break _loop194;
 			}
 			
 		} while (true);
 		}
 		JavaNode tmp105_AST = null;
 		tmp105_AST = (JavaNode)astFactory.create(LT(1));
 		astFactory.addASTChild(currentAST, tmp105_AST);
 		match(RCURLY);
 		compoundStatement_AST = (JavaNode)currentAST.root;
 		returnAST = compoundStatement_AST;
 	}
 	
 	public final void ctorHead() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode ctorHead_AST = null;
 		
 		JavaNode tmp106_AST = null;
 		tmp106_AST = (JavaNode)astFactory.create(LT(1));
 		astFactory.addASTChild(currentAST, tmp106_AST);
 		match(IDENT);
 		JavaNode tmp107_AST = null;
 		tmp107_AST = (JavaNode)astFactory.create(LT(1));
 		astFactory.addASTChild(currentAST, tmp107_AST);
 		match(LPAREN);
 		parameterDeclarationList();
 		astFactory.addASTChild(currentAST, returnAST);
 		JavaNode tmp108_AST = null;
 		tmp108_AST = (JavaNode)astFactory.create(LT(1));
 		astFactory.addASTChild(currentAST, tmp108_AST);
 		match(RPAREN);
 		{
 		switch ( LA(1)) {
 		case LITERAL_throws:
 		{
 			throwsClause();
 			astFactory.addASTChild(currentAST, returnAST);
 			break;
 		}
 		case LCURLY:
 		{
 			break;
 		}
 		default:
 		{
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		}
 		}
 		ctorHead_AST = (JavaNode)currentAST.root;
 		returnAST = ctorHead_AST;
 	}
 	
 	public final void constructorBody() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode constructorBody_AST = null;
 		Token  lc = null;
 		JavaNode lc_AST = null;
 		
 		lc = LT(1);
 		lc_AST = (JavaNode)astFactory.create(lc);
 		astFactory.makeASTRoot(currentAST, lc_AST);
 		match(LCURLY);
 		if ( inputState.guessing==0 ) {
 			lc_AST.setType(SLIST);
 		}
 		{
 		if ((_tokenSet_24.member(LA(1))) && (_tokenSet_25.member(LA(2)))) {
 			explicitConstructorInvocation();
 			astFactory.addASTChild(currentAST, returnAST);
 		}
 		else if ((_tokenSet_26.member(LA(1))) && (_tokenSet_27.member(LA(2)))) {
 		}
 		else {
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		
 		}
 		{
 		_loop149:
 		do {
 			if ((_tokenSet_23.member(LA(1)))) {
 				statement();
 				astFactory.addASTChild(currentAST, returnAST);
 			}
 			else {
 				break _loop149;
 			}
 			
 		} while (true);
 		}
 		JavaNode tmp109_AST = null;
 		tmp109_AST = (JavaNode)astFactory.create(LT(1));
 		astFactory.addASTChild(currentAST, tmp109_AST);
 		match(RCURLY);
 		constructorBody_AST = (JavaNode)currentAST.root;
 		returnAST = constructorBody_AST;
 	}
 	
 /** Catch obvious constructor calls, but not the expr.super(...) calls */
 	public final void explicitConstructorInvocation() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode explicitConstructorInvocation_AST = null;
 		Token  lp1 = null;
 		JavaNode lp1_AST = null;
 		Token  lp2 = null;
 		JavaNode lp2_AST = null;
 		
 		{
 		switch ( LA(1)) {
 		case LT:
 		{
 			typeArguments();
 			astFactory.addASTChild(currentAST, returnAST);
 			break;
 		}
 		case LITERAL_super:
 		case LITERAL_this:
 		{
 			break;
 		}
 		default:
 		{
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		}
 		}
 		{
 		switch ( LA(1)) {
 		case LITERAL_this:
 		{
 			match(LITERAL_this);
 			lp1 = LT(1);
 			lp1_AST = (JavaNode)astFactory.create(lp1);
 			astFactory.makeASTRoot(currentAST, lp1_AST);
 			match(LPAREN);
 			argList();
 			astFactory.addASTChild(currentAST, returnAST);
 			JavaNode tmp111_AST = null;
 			tmp111_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp111_AST);
 			match(RPAREN);
 			JavaNode tmp112_AST = null;
 			tmp112_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp112_AST);
 			match(SEMI);
 			if ( inputState.guessing==0 ) {
 				lp1_AST.setType(CTOR_CALL);
 			}
 			break;
 		}
 		case LITERAL_super:
 		{
 			match(LITERAL_super);
 			lp2 = LT(1);
 			lp2_AST = (JavaNode)astFactory.create(lp2);
 			astFactory.makeASTRoot(currentAST, lp2_AST);
 			match(LPAREN);
 			argList();
 			astFactory.addASTChild(currentAST, returnAST);
 			JavaNode tmp114_AST = null;
 			tmp114_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp114_AST);
 			match(RPAREN);
 			JavaNode tmp115_AST = null;
 			tmp115_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp115_AST);
 			match(SEMI);
 			if ( inputState.guessing==0 ) {
 				lp2_AST.setType(SUPER_CTOR_CALL);
 			}
 			break;
 		}
 		default:
 		{
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		}
 		}
 		explicitConstructorInvocation_AST = (JavaNode)currentAST.root;
 		returnAST = explicitConstructorInvocation_AST;
 	}
 	
 	public final void statement() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode statement_AST = null;
 		JavaNode decl_AST = null;
 		Token  semi1 = null;
 		JavaNode semi1_AST = null;
 		JavaNode e_AST = null;
 		Token  semi2 = null;
 		JavaNode semi2_AST = null;
 		JavaNode m_AST = null;
 		Token  c = null;
 		JavaNode c_AST = null;
 		Token  synBlock = null;
 		JavaNode synBlock_AST = null;
 		Token  s = null;
 		JavaNode s_AST = null;
 		
 		switch ( LA(1)) {
 		case LCURLY:
 		{
 			compoundStatement();
 			astFactory.addASTChild(currentAST, returnAST);
 			statement_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case LITERAL_if:
 		{
 			JavaNode tmp116_AST = null;
 			tmp116_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.makeASTRoot(currentAST, tmp116_AST);
 			match(LITERAL_if);
 			JavaNode tmp117_AST = null;
 			tmp117_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp117_AST);
 			match(LPAREN);
 			expression();
 			astFactory.addASTChild(currentAST, returnAST);
 			JavaNode tmp118_AST = null;
 			tmp118_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp118_AST);
 			match(RPAREN);
 			statement();
 			astFactory.addASTChild(currentAST, returnAST);
 			{
 			if ((LA(1)==LITERAL_else) && (_tokenSet_23.member(LA(2)))) {
 				JavaNode tmp119_AST = null;
 				tmp119_AST = (JavaNode)astFactory.create(LT(1));
 				astFactory.addASTChild(currentAST, tmp119_AST);
 				match(LITERAL_else);
 				statement();
 				astFactory.addASTChild(currentAST, returnAST);
 			}
 			else if ((_tokenSet_28.member(LA(1))) && (_tokenSet_29.member(LA(2)))) {
 			}
 			else {
 				throw new NoViableAltException(LT(1), getFilename());
 			}
 			
 			}
 			statement_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case LITERAL_for:
 		{
 			forStatement();
 			astFactory.addASTChild(currentAST, returnAST);
 			statement_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case LITERAL_while:
 		{
 			JavaNode tmp120_AST = null;
 			tmp120_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.makeASTRoot(currentAST, tmp120_AST);
 			match(LITERAL_while);
 			JavaNode tmp121_AST = null;
 			tmp121_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp121_AST);
 			match(LPAREN);
 			expression();
 			astFactory.addASTChild(currentAST, returnAST);
 			JavaNode tmp122_AST = null;
 			tmp122_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp122_AST);
 			match(RPAREN);
 			statement();
 			astFactory.addASTChild(currentAST, returnAST);
 			statement_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case LITERAL_do:
 		{
 			JavaNode tmp123_AST = null;
 			tmp123_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.makeASTRoot(currentAST, tmp123_AST);
 			match(LITERAL_do);
 			statement();
 			astFactory.addASTChild(currentAST, returnAST);
 			JavaNode tmp124_AST = null;
 			tmp124_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp124_AST);
 			match(LITERAL_while);
 			JavaNode tmp125_AST = null;
 			tmp125_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp125_AST);
 			match(LPAREN);
 			expression();
 			astFactory.addASTChild(currentAST, returnAST);
 			JavaNode tmp126_AST = null;
 			tmp126_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp126_AST);
 			match(RPAREN);
 			JavaNode tmp127_AST = null;
 			tmp127_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp127_AST);
 			match(SEMI);
 			statement_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case LITERAL_break:
 		{
 			JavaNode tmp128_AST = null;
 			tmp128_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.makeASTRoot(currentAST, tmp128_AST);
 			match(LITERAL_break);
 			{
 			switch ( LA(1)) {
 			case IDENT:
 			{
 				JavaNode tmp129_AST = null;
 				tmp129_AST = (JavaNode)astFactory.create(LT(1));
 				astFactory.addASTChild(currentAST, tmp129_AST);
 				match(IDENT);
 				break;
 			}
 			case SEMI:
 			{
 				break;
 			}
 			default:
 			{
 				throw new NoViableAltException(LT(1), getFilename());
 			}
 			}
 			}
 			JavaNode tmp130_AST = null;
 			tmp130_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp130_AST);
 			match(SEMI);
 			statement_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case LITERAL_continue:
 		{
 			JavaNode tmp131_AST = null;
 			tmp131_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.makeASTRoot(currentAST, tmp131_AST);
 			match(LITERAL_continue);
 			{
 			switch ( LA(1)) {
 			case IDENT:
 			{
 				JavaNode tmp132_AST = null;
 				tmp132_AST = (JavaNode)astFactory.create(LT(1));
 				astFactory.addASTChild(currentAST, tmp132_AST);
 				match(IDENT);
 				break;
 			}
 			case SEMI:
 			{
 				break;
 			}
 			default:
 			{
 				throw new NoViableAltException(LT(1), getFilename());
 			}
 			}
 			}
 			JavaNode tmp133_AST = null;
 			tmp133_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp133_AST);
 			match(SEMI);
 			statement_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case LITERAL_return:
 		{
 			JavaNode tmp134_AST = null;
 			tmp134_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.makeASTRoot(currentAST, tmp134_AST);
 			match(LITERAL_return);
 			{
 			switch ( LA(1)) {
 			case IDENT:
 			case LITERAL_super:
 			case LT:
 			case LITERAL_void:
 			case LITERAL_boolean:
 			case LITERAL_byte:
 			case LITERAL_char:
 			case LITERAL_short:
 			case LITERAL_int:
 			case LITERAL_float:
 			case LITERAL_long:
 			case LITERAL_double:
 			case LPAREN:
 			case LITERAL_this:
 			case PLUS:
 			case MINUS:
 			case INC:
 			case DEC:
 			case BNOT:
 			case LNOT:
 			case LITERAL_true:
 			case LITERAL_false:
 			case LITERAL_null:
 			case LITERAL_new:
 			case NUM_INT:
 			case CHAR_LITERAL:
 			case STRING_LITERAL:
 			case NUM_FLOAT:
 			case NUM_LONG:
 			case NUM_DOUBLE:
 			{
 				expression();
 				astFactory.addASTChild(currentAST, returnAST);
 				break;
 			}
 			case SEMI:
 			{
 				break;
 			}
 			default:
 			{
 				throw new NoViableAltException(LT(1), getFilename());
 			}
 			}
 			}
 			JavaNode tmp135_AST = null;
 			tmp135_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp135_AST);
 			match(SEMI);
 			statement_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case LITERAL_switch:
 		{
 			JavaNode tmp136_AST = null;
 			tmp136_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.makeASTRoot(currentAST, tmp136_AST);
 			match(LITERAL_switch);
 			JavaNode tmp137_AST = null;
 			tmp137_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp137_AST);
 			match(LPAREN);
 			expression();
 			astFactory.addASTChild(currentAST, returnAST);
 			JavaNode tmp138_AST = null;
 			tmp138_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp138_AST);
 			match(RPAREN);
 			JavaNode tmp139_AST = null;
 			tmp139_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp139_AST);
 			match(LCURLY);
 			{
 			_loop203:
 			do {
 				if ((LA(1)==LITERAL_default||LA(1)==LITERAL_case)) {
 					casesGroup();
 					astFactory.addASTChild(currentAST, returnAST);
 				}
 				else {
 					break _loop203;
 				}
 				
 			} while (true);
 			}
 			JavaNode tmp140_AST = null;
 			tmp140_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp140_AST);
 			match(RCURLY);
 			statement_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case LITERAL_try:
 		{
 			tryBlock();
 			astFactory.addASTChild(currentAST, returnAST);
 			statement_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case LITERAL_throw:
 		{
 			JavaNode tmp141_AST = null;
 			tmp141_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.makeASTRoot(currentAST, tmp141_AST);
 			match(LITERAL_throw);
 			expression();
 			astFactory.addASTChild(currentAST, returnAST);
 			JavaNode tmp142_AST = null;
 			tmp142_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp142_AST);
 			match(SEMI);
 			statement_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case LITERAL_assert:
 		{
 			JavaNode tmp143_AST = null;
 			tmp143_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.makeASTRoot(currentAST, tmp143_AST);
 			match(LITERAL_assert);
 			expression();
 			astFactory.addASTChild(currentAST, returnAST);
 			{
 			switch ( LA(1)) {
 			case COLON:
 			{
 				match(COLON);
 				expression();
 				astFactory.addASTChild(currentAST, returnAST);
 				break;
 			}
 			case SEMI:
 			{
 				break;
 			}
 			default:
 			{
 				throw new NoViableAltException(LT(1), getFilename());
 			}
 			}
 			}
 			JavaNode tmp145_AST = null;
 			tmp145_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp145_AST);
 			match(SEMI);
 			statement_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case SEMI:
 		{
 			s = LT(1);
 			s_AST = (JavaNode)astFactory.create(s);
 			astFactory.addASTChild(currentAST, s_AST);
 			match(SEMI);
 			if ( inputState.guessing==0 ) {
 				s_AST.setType(EMPTY_STAT);
 			}
 			statement_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		default:
 			boolean synPredMatched197 = false;
 			if (((_tokenSet_30.member(LA(1))) && (_tokenSet_31.member(LA(2))))) {
 				int _m197 = mark();
 				synPredMatched197 = true;
 				inputState.guessing++;
 				try {
 					{
 					declaration();
 					}
 				}
 				catch (RecognitionException pe) {
 					synPredMatched197 = false;
 				}
 				rewind(_m197);
 				inputState.guessing--;
 			}
 			if ( synPredMatched197 ) {
 				declaration();
 				decl_AST = (JavaNode)returnAST;
 				astFactory.addASTChild(currentAST, returnAST);
 				semi1 = LT(1);
 				semi1_AST = (JavaNode)astFactory.create(semi1);
 				match(SEMI);
 				if ( inputState.guessing==0 ) {
 					
 					// add semicolon to the AST
 					decl_AST.addChild(semi1_AST);
 					
 					AST next = currentAST.root.getNextSibling();
 					
 					// HACK for multiple variable declaration in one statement
 					//      e.g float x, y, z;
 					// the semicolon will only be added to the first statement so
 					// we have to add it manually to all others
 					if (next != null)
 					{
 					AST semi = JavaNodeHelper.getFirstChild(currentAST.root, JavaTokenTypes.SEMI);
 					
 					for (AST var = next; var != null; var = var.getNextSibling())
 					{
 					var.addChild(astFactory.create(semi));
 					}
 					}
 					
 				}
 				statement_AST = (JavaNode)currentAST.root;
 			}
 			else if ((_tokenSet_32.member(LA(1))) && (_tokenSet_33.member(LA(2)))) {
 				expression();
 				e_AST = (JavaNode)returnAST;
 				astFactory.addASTChild(currentAST, returnAST);
 				semi2 = LT(1);
 				semi2_AST = (JavaNode)astFactory.create(semi2);
 				match(SEMI);
 				if ( inputState.guessing==0 ) {
 					e_AST.addChild(semi2_AST);
 									
 				}
 				statement_AST = (JavaNode)currentAST.root;
 			}
 			else if ((_tokenSet_34.member(LA(1))) && (_tokenSet_35.member(LA(2)))) {
 				modifiers();
 				m_AST = (JavaNode)returnAST;
 				classDefinition(m_AST);
 				astFactory.addASTChild(currentAST, returnAST);
 				statement_AST = (JavaNode)currentAST.root;
 			}
 			else if ((LA(1)==IDENT) && (LA(2)==COLON)) {
 				JavaNode tmp146_AST = null;
 				tmp146_AST = (JavaNode)astFactory.create(LT(1));
 				astFactory.addASTChild(currentAST, tmp146_AST);
 				match(IDENT);
 				c = LT(1);
 				c_AST = (JavaNode)astFactory.create(c);
 				astFactory.makeASTRoot(currentAST, c_AST);
 				match(COLON);
 				if ( inputState.guessing==0 ) {
 					c_AST.setType(LABELED_STAT);
 				}
 				statement();
 				astFactory.addASTChild(currentAST, returnAST);
 				statement_AST = (JavaNode)currentAST.root;
 			}
 			else if ((LA(1)==LITERAL_synchronized) && (LA(2)==LPAREN)) {
 				synBlock = LT(1);
 				synBlock_AST = (JavaNode)astFactory.create(synBlock);
 				astFactory.makeASTRoot(currentAST, synBlock_AST);
 				match(LITERAL_synchronized);
 				JavaNode tmp147_AST = null;
 				tmp147_AST = (JavaNode)astFactory.create(LT(1));
 				astFactory.addASTChild(currentAST, tmp147_AST);
 				match(LPAREN);
 				expression();
 				astFactory.addASTChild(currentAST, returnAST);
 				JavaNode tmp148_AST = null;
 				tmp148_AST = (JavaNode)astFactory.create(LT(1));
 				astFactory.addASTChild(currentAST, tmp148_AST);
 				match(RPAREN);
 				compoundStatement();
 				astFactory.addASTChild(currentAST, returnAST);
 				if ( inputState.guessing==0 ) {
 					synBlock_AST.setType(SYNBLOCK);
 				}
 				statement_AST = (JavaNode)currentAST.root;
 			}
 		else {
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		}
 		returnAST = statement_AST;
 	}
 	
 /** Catch variable definitions but add the semicolon ???*/
 	public final void typeVariableDefinitions(
 		JavaNode mods, JavaNode t
 	) throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode typeVariableDefinitions_AST = null;
 		JavaNode v_AST = null;
 		Token  semi = null;
 		JavaNode semi_AST = null;
 		
 		variableDefinitions(mods,t);
 		v_AST = (JavaNode)returnAST;
 		astFactory.addASTChild(currentAST, returnAST);
 		{
 		semi = LT(1);
 		semi_AST = (JavaNode)astFactory.create(semi);
 		astFactory.addASTChild(currentAST, semi_AST);
 		match(SEMI);
 		if ( inputState.guessing==0 ) {
 			
 			v_AST.addChild(semi_AST);
 			
 		}
 		}
 		typeVariableDefinitions_AST = (JavaNode)currentAST.root;
 		returnAST = typeVariableDefinitions_AST;
 	}
 	
 /** Declaration of a variable. This can be a class/instance variable,
  *  or a local variable in a method
  *  It can also include possible initialization.
  */
 	public final void variableDeclarator(
 		JavaNode mods, JavaNode t
 	) throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode variableDeclarator_AST = null;
 		Token  id = null;
 		JavaNode id_AST = null;
 		JavaNode d_AST = null;
 		JavaNode v_AST = null;
 		
 		id = LT(1);
 		id_AST = (JavaNode)astFactory.create(id);
 		match(IDENT);
 		declaratorBrackets(t);
 		d_AST = (JavaNode)returnAST;
 		varInitializer();
 		v_AST = (JavaNode)returnAST;
 		if ( inputState.guessing==0 ) {
 			variableDeclarator_AST = (JavaNode)currentAST.root;
 			variableDeclarator_AST = (JavaNode)astFactory.make( (new ASTArray(5)).add((JavaNode)astFactory.create(VARIABLE_DEF,"VARIABLE_DEF")).add(mods).add((JavaNode)astFactory.make( (new ASTArray(2)).add((JavaNode)astFactory.create(TYPE,"TYPE")).add(d_AST))).add(id_AST).add(v_AST));
 					attachStuff(new JavaNode[] {variableDeclarator_AST,mods, t});
 					
 			currentAST.root = variableDeclarator_AST;
 			currentAST.child = variableDeclarator_AST!=null &&variableDeclarator_AST.getFirstChild()!=null ?
 				variableDeclarator_AST.getFirstChild() : variableDeclarator_AST;
 			currentAST.advanceChildToEnd();
 		}
 		returnAST = variableDeclarator_AST;
 	}
 	
 	public final void varInitializer() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode varInitializer_AST = null;
 		
 		{
 		switch ( LA(1)) {
 		case ASSIGN:
 		{
 			JavaNode tmp149_AST = null;
 			tmp149_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.makeASTRoot(currentAST, tmp149_AST);
 			match(ASSIGN);
 			initializer();
 			astFactory.addASTChild(currentAST, returnAST);
 			break;
 		}
 		case LCURLY:
 		case RCURLY:
 		case FINAL:
 		case ABSTRACT:
 		case STRICTFP:
 		case SEMI:
 		case LITERAL_static:
 		case IDENT:
 		case LT:
 		case COMMA:
 		case LITERAL_void:
 		case LITERAL_boolean:
 		case LITERAL_byte:
 		case LITERAL_char:
 		case LITERAL_short:
 		case LITERAL_int:
 		case LITERAL_float:
 		case LITERAL_long:
 		case LITERAL_double:
 		case LITERAL_private:
 		case LITERAL_public:
 		case LITERAL_protected:
 		case LITERAL_transient:
 		case LITERAL_native:
 		case LITERAL_threadsafe:
 		case LITERAL_synchronized:
 		case LITERAL_volatile:
 		case AT:
 		case LITERAL_class:
 		case LITERAL_interface:
 		case LITERAL_enum:
 		{
 			break;
 		}
 		default:
 		{
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		}
 		}
 		varInitializer_AST = (JavaNode)currentAST.root;
 		returnAST = varInitializer_AST;
 	}
 	
 	public final void initializer() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode initializer_AST = null;
 		
 		switch ( LA(1)) {
 		case IDENT:
 		case LITERAL_super:
 		case LT:
 		case LITERAL_void:
 		case LITERAL_boolean:
 		case LITERAL_byte:
 		case LITERAL_char:
 		case LITERAL_short:
 		case LITERAL_int:
 		case LITERAL_float:
 		case LITERAL_long:
 		case LITERAL_double:
 		case LPAREN:
 		case LITERAL_this:
 		case PLUS:
 		case MINUS:
 		case INC:
 		case DEC:
 		case BNOT:
 		case LNOT:
 		case LITERAL_true:
 		case LITERAL_false:
 		case LITERAL_null:
 		case LITERAL_new:
 		case NUM_INT:
 		case CHAR_LITERAL:
 		case STRING_LITERAL:
 		case NUM_FLOAT:
 		case NUM_LONG:
 		case NUM_DOUBLE:
 		{
 			expression();
 			astFactory.addASTChild(currentAST, returnAST);
 			initializer_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case LCURLY:
 		{
 			arrayInitializer();
 			astFactory.addASTChild(currentAST, returnAST);
 			initializer_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		default:
 		{
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		}
 		returnAST = initializer_AST;
 	}
 	
 	public final void arrayInitializer() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode arrayInitializer_AST = null;
 		Token  lc = null;
 		JavaNode lc_AST = null;
 		
 		lc = LT(1);
 		lc_AST = (JavaNode)astFactory.create(lc);
 		astFactory.makeASTRoot(currentAST, lc_AST);
 		match(LCURLY);
 		if ( inputState.guessing==0 ) {
 			lc_AST.setType(ARRAY_INIT);
 		}
 		{
 		switch ( LA(1)) {
 		case LCURLY:
 		case IDENT:
 		case LITERAL_super:
 		case LT:
 		case LITERAL_void:
 		case LITERAL_boolean:
 		case LITERAL_byte:
 		case LITERAL_char:
 		case LITERAL_short:
 		case LITERAL_int:
 		case LITERAL_float:
 		case LITERAL_long:
 		case LITERAL_double:
 		case LPAREN:
 		case LITERAL_this:
 		case PLUS:
 		case MINUS:
 		case INC:
 		case DEC:
 		case BNOT:
 		case LNOT:
 		case LITERAL_true:
 		case LITERAL_false:
 		case LITERAL_null:
 		case LITERAL_new:
 		case NUM_INT:
 		case CHAR_LITERAL:
 		case STRING_LITERAL:
 		case NUM_FLOAT:
 		case NUM_LONG:
 		case NUM_DOUBLE:
 		{
 			initializer();
 			astFactory.addASTChild(currentAST, returnAST);
 			{
 			_loop167:
 			do {
 				if ((LA(1)==COMMA) && (_tokenSet_36.member(LA(2)))) {
 					JavaNode tmp150_AST = null;
 					tmp150_AST = (JavaNode)astFactory.create(LT(1));
 					astFactory.addASTChild(currentAST, tmp150_AST);
 					match(COMMA);
 					initializer();
 					astFactory.addASTChild(currentAST, returnAST);
 				}
 				else {
 					break _loop167;
 				}
 				
 			} while (true);
 			}
 			{
 			switch ( LA(1)) {
 			case COMMA:
 			{
 				JavaNode tmp151_AST = null;
 				tmp151_AST = (JavaNode)astFactory.create(LT(1));
 				astFactory.addASTChild(currentAST, tmp151_AST);
 				match(COMMA);
 				break;
 			}
 			case RCURLY:
 			{
 				break;
 			}
 			default:
 			{
 				throw new NoViableAltException(LT(1), getFilename());
 			}
 			}
 			}
 			break;
 		}
 		case RCURLY:
 		{
 			break;
 		}
 		default:
 		{
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		}
 		}
 		JavaNode tmp152_AST = null;
 		tmp152_AST = (JavaNode)astFactory.create(LT(1));
 		astFactory.addASTChild(currentAST, tmp152_AST);
 		match(RCURLY);
 		arrayInitializer_AST = (JavaNode)currentAST.root;
 		returnAST = arrayInitializer_AST;
 	}
 	
 	public final void expression() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode expression_AST = null;
 		
 		assignmentExpression();
 		astFactory.addASTChild(currentAST, returnAST);
 		if ( inputState.guessing==0 ) {
 			expression_AST = (JavaNode)currentAST.root;
 			expression_AST = (JavaNode)astFactory.make( (new ASTArray(2)).add((JavaNode)astFactory.create(EXPR,"EXPR")).add(expression_AST));
 			currentAST.root = expression_AST;
 			currentAST.child = expression_AST!=null &&expression_AST.getFirstChild()!=null ?
 				expression_AST.getFirstChild() : expression_AST;
 			currentAST.advanceChildToEnd();
 		}
 		expression_AST = (JavaNode)currentAST.root;
 		returnAST = expression_AST;
 	}
 	
 	public final void parameterDeclaration() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode parameterDeclaration_AST = null;
 		JavaNode pm_AST = null;
 		JavaNode t_AST = null;
 		Token  id = null;
 		JavaNode id_AST = null;
 		JavaNode pd_AST = null;
 		
 		parameterModifier();
 		pm_AST = (JavaNode)returnAST;
 		typeSpec(false);
 		t_AST = (JavaNode)returnAST;
 		id = LT(1);
 		id_AST = (JavaNode)astFactory.create(id);
 		match(IDENT);
 		declaratorBrackets(t_AST);
 		pd_AST = (JavaNode)returnAST;
 		if ( inputState.guessing==0 ) {
 			parameterDeclaration_AST = (JavaNode)currentAST.root;
 			parameterDeclaration_AST = (JavaNode)astFactory.make( (new ASTArray(4)).add((JavaNode)astFactory.create(PARAMETER_DEF,"PARAMETER_DEF")).add(pm_AST).add((JavaNode)astFactory.make( (new ASTArray(2)).add((JavaNode)astFactory.create(TYPE,"TYPE")).add(pd_AST))).add(id_AST));
 			currentAST.root = parameterDeclaration_AST;
 			currentAST.child = parameterDeclaration_AST!=null &&parameterDeclaration_AST.getFirstChild()!=null ?
 				parameterDeclaration_AST.getFirstChild() : parameterDeclaration_AST;
 			currentAST.advanceChildToEnd();
 		}
 		returnAST = parameterDeclaration_AST;
 	}
 	
 	public final void variableLengthParameterDeclaration() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode variableLengthParameterDeclaration_AST = null;
 		JavaNode pm_AST = null;
 		JavaNode t_AST = null;
 		Token  id = null;
 		JavaNode id_AST = null;
 		JavaNode pd_AST = null;
 		
 		parameterModifier();
 		pm_AST = (JavaNode)returnAST;
 		typeSpec(false);
 		t_AST = (JavaNode)returnAST;
 		JavaNode tmp153_AST = null;
 		tmp153_AST = (JavaNode)astFactory.create(LT(1));
 		match(TRIPLE_DOT);
 		id = LT(1);
 		id_AST = (JavaNode)astFactory.create(id);
 		match(IDENT);
 		declaratorBrackets(t_AST);
 		pd_AST = (JavaNode)returnAST;
 		if ( inputState.guessing==0 ) {
 			variableLengthParameterDeclaration_AST = (JavaNode)currentAST.root;
 			variableLengthParameterDeclaration_AST = (JavaNode)astFactory.make( (new ASTArray(4)).add((JavaNode)astFactory.create(VARIABLE_PARAMETER_DEF,"VARIABLE_PARAMETER_DEF")).add(pm_AST).add((JavaNode)astFactory.make( (new ASTArray(2)).add((JavaNode)astFactory.create(TYPE,"TYPE")).add(pd_AST))).add(id_AST));
 			currentAST.root = variableLengthParameterDeclaration_AST;
 			currentAST.child = variableLengthParameterDeclaration_AST!=null &&variableLengthParameterDeclaration_AST.getFirstChild()!=null ?
 				variableLengthParameterDeclaration_AST.getFirstChild() : variableLengthParameterDeclaration_AST;
 			currentAST.advanceChildToEnd();
 		}
 		returnAST = variableLengthParameterDeclaration_AST;
 	}
 	
 	public final void parameterModifier() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode parameterModifier_AST = null;
 		Token  f = null;
 		JavaNode f_AST = null;
 		
 		{
 		_loop188:
 		do {
 			if ((LA(1)==AT) && (LA(2)==IDENT)) {
 				annotation();
 				astFactory.addASTChild(currentAST, returnAST);
 			}
 			else {
 				break _loop188;
 			}
 			
 		} while (true);
 		}
 		{
 		switch ( LA(1)) {
 		case FINAL:
 		{
 			f = LT(1);
 			f_AST = (JavaNode)astFactory.create(f);
 			astFactory.addASTChild(currentAST, f_AST);
 			match(FINAL);
 			break;
 		}
 		case IDENT:
 		case LITERAL_void:
 		case LITERAL_boolean:
 		case LITERAL_byte:
 		case LITERAL_char:
 		case LITERAL_short:
 		case LITERAL_int:
 		case LITERAL_float:
 		case LITERAL_long:
 		case LITERAL_double:
 		case AT:
 		{
 			break;
 		}
 		default:
 		{
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		}
 		}
 		{
 		_loop191:
 		do {
 			if ((LA(1)==AT)) {
 				annotation();
 				astFactory.addASTChild(currentAST, returnAST);
 			}
 			else {
 				break _loop191;
 			}
 			
 		} while (true);
 		}
 		if ( inputState.guessing==0 ) {
 			parameterModifier_AST = (JavaNode)currentAST.root;
 			parameterModifier_AST = (JavaNode)astFactory.make( (new ASTArray(2)).add((JavaNode)astFactory.create(MODIFIERS,"MODIFIERS")).add(parameterModifier_AST));
 			currentAST.root = parameterModifier_AST;
 			currentAST.child = parameterModifier_AST!=null &&parameterModifier_AST.getFirstChild()!=null ?
 				parameterModifier_AST.getFirstChild() : parameterModifier_AST;
 			currentAST.advanceChildToEnd();
 		}
 		parameterModifier_AST = (JavaNode)currentAST.root;
 		returnAST = parameterModifier_AST;
 	}
 	
 	public final void forStatement() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode forStatement_AST = null;
 		Token  f = null;
 		JavaNode f_AST = null;
 		
 		f = LT(1);
 		f_AST = (JavaNode)astFactory.create(f);
 		astFactory.makeASTRoot(currentAST, f_AST);
 		match(LITERAL_for);
 		JavaNode tmp154_AST = null;
 		tmp154_AST = (JavaNode)astFactory.create(LT(1));
 		astFactory.addASTChild(currentAST, tmp154_AST);
 		match(LPAREN);
 		{
 		boolean synPredMatched208 = false;
 		if (((_tokenSet_37.member(LA(1))) && (_tokenSet_38.member(LA(2))))) {
 			int _m208 = mark();
 			synPredMatched208 = true;
 			inputState.guessing++;
 			try {
 				{
 				forInit();
 				match(SEMI);
 				}
 			}
 			catch (RecognitionException pe) {
 				synPredMatched208 = false;
 			}
 			rewind(_m208);
 			inputState.guessing--;
 		}
 		if ( synPredMatched208 ) {
 			traditionalForClause();
 			astFactory.addASTChild(currentAST, returnAST);
 		}
 		else if ((_tokenSet_20.member(LA(1))) && (_tokenSet_21.member(LA(2)))) {
 			forEachClause();
 			astFactory.addASTChild(currentAST, returnAST);
 		}
 		else {
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		
 		}
 		JavaNode tmp155_AST = null;
 		tmp155_AST = (JavaNode)astFactory.create(LT(1));
 		astFactory.addASTChild(currentAST, tmp155_AST);
 		match(RPAREN);
 		statement();
 		astFactory.addASTChild(currentAST, returnAST);
 		forStatement_AST = (JavaNode)currentAST.root;
 		returnAST = forStatement_AST;
 	}
 	
 	public final void casesGroup() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode casesGroup_AST = null;
 		
 		{
 		int _cnt213=0;
 		_loop213:
 		do {
 			if ((LA(1)==LITERAL_default||LA(1)==LITERAL_case) && (_tokenSet_39.member(LA(2)))) {
 				aCase();
 				astFactory.addASTChild(currentAST, returnAST);
 			}
 			else {
 				if ( _cnt213>=1 ) { break _loop213; } else {throw new NoViableAltException(LT(1), getFilename());}
 			}
 			
 			_cnt213++;
 		} while (true);
 		}
 		caseSList();
 		astFactory.addASTChild(currentAST, returnAST);
 		if ( inputState.guessing==0 ) {
 			casesGroup_AST = (JavaNode)currentAST.root;
 			casesGroup_AST = (JavaNode)astFactory.make( (new ASTArray(2)).add((JavaNode)astFactory.create(CASE_GROUP,"CASE_GROUP")).add(casesGroup_AST));
 			currentAST.root = casesGroup_AST;
 			currentAST.child = casesGroup_AST!=null &&casesGroup_AST.getFirstChild()!=null ?
 				casesGroup_AST.getFirstChild() : casesGroup_AST;
 			currentAST.advanceChildToEnd();
 		}
 		casesGroup_AST = (JavaNode)currentAST.root;
 		returnAST = casesGroup_AST;
 	}
 	
 	public final void tryBlock() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode tryBlock_AST = null;
 		
 		JavaNode tmp156_AST = null;
 		tmp156_AST = (JavaNode)astFactory.create(LT(1));
 		astFactory.makeASTRoot(currentAST, tmp156_AST);
 		match(LITERAL_try);
 		compoundStatement();
 		astFactory.addASTChild(currentAST, returnAST);
 		{
 		_loop229:
 		do {
 			if ((LA(1)==LITERAL_catch)) {
 				handler();
 				astFactory.addASTChild(currentAST, returnAST);
 			}
 			else {
 				break _loop229;
 			}
 			
 		} while (true);
 		}
 		{
 		switch ( LA(1)) {
 		case LITERAL_finally:
 		{
 			finallyClause();
 			astFactory.addASTChild(currentAST, returnAST);
 			break;
 		}
 		case LCURLY:
 		case RCURLY:
 		case FINAL:
 		case ABSTRACT:
 		case STRICTFP:
 		case SEMI:
 		case LITERAL_static:
 		case IDENT:
 		case LITERAL_super:
 		case LT:
 		case LITERAL_void:
 		case LITERAL_boolean:
 		case LITERAL_byte:
 		case LITERAL_char:
 		case LITERAL_short:
 		case LITERAL_int:
 		case LITERAL_float:
 		case LITERAL_long:
 		case LITERAL_double:
 		case LITERAL_private:
 		case LITERAL_public:
 		case LITERAL_protected:
 		case LITERAL_transient:
 		case LITERAL_native:
 		case LITERAL_threadsafe:
 		case LITERAL_synchronized:
 		case LITERAL_volatile:
 		case AT:
 		case LPAREN:
 		case LITERAL_class:
 		case LITERAL_default:
 		case LITERAL_this:
 		case LITERAL_if:
 		case LITERAL_else:
 		case LITERAL_while:
 		case LITERAL_do:
 		case LITERAL_break:
 		case LITERAL_continue:
 		case LITERAL_return:
 		case LITERAL_switch:
 		case LITERAL_throw:
 		case LITERAL_assert:
 		case LITERAL_for:
 		case LITERAL_case:
 		case LITERAL_try:
 		case PLUS:
 		case MINUS:
 		case INC:
 		case DEC:
 		case BNOT:
 		case LNOT:
 		case LITERAL_true:
 		case LITERAL_false:
 		case LITERAL_null:
 		case LITERAL_new:
 		case NUM_INT:
 		case CHAR_LITERAL:
 		case STRING_LITERAL:
 		case NUM_FLOAT:
 		case NUM_LONG:
 		case NUM_DOUBLE:
 		{
 			break;
 		}
 		default:
 		{
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		}
 		}
 		tryBlock_AST = (JavaNode)currentAST.root;
 		returnAST = tryBlock_AST;
 	}
 	
 	public final void forInit() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode forInit_AST = null;
 		
 		{
 		boolean synPredMatched222 = false;
 		if (((_tokenSet_30.member(LA(1))) && (_tokenSet_31.member(LA(2))))) {
 			int _m222 = mark();
 			synPredMatched222 = true;
 			inputState.guessing++;
 			try {
 				{
 				declaration();
 				}
 			}
 			catch (RecognitionException pe) {
 				synPredMatched222 = false;
 			}
 			rewind(_m222);
 			inputState.guessing--;
 		}
 		if ( synPredMatched222 ) {
 			declaration();
 			astFactory.addASTChild(currentAST, returnAST);
 		}
 		else if ((_tokenSet_32.member(LA(1))) && (_tokenSet_40.member(LA(2)))) {
 			expressionList();
 			astFactory.addASTChild(currentAST, returnAST);
 		}
 		else if ((LA(1)==SEMI)) {
 		}
 		else {
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		
 		}
 		if ( inputState.guessing==0 ) {
 			forInit_AST = (JavaNode)currentAST.root;
 			forInit_AST = (JavaNode)astFactory.make( (new ASTArray(2)).add((JavaNode)astFactory.create(FOR_INIT,"FOR_INIT")).add(forInit_AST));
 			currentAST.root = forInit_AST;
 			currentAST.child = forInit_AST!=null &&forInit_AST.getFirstChild()!=null ?
 				forInit_AST.getFirstChild() : forInit_AST;
 			currentAST.advanceChildToEnd();
 		}
 		forInit_AST = (JavaNode)currentAST.root;
 		returnAST = forInit_AST;
 	}
 	
 	public final void traditionalForClause() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode traditionalForClause_AST = null;
 		
 		forInit();
 		astFactory.addASTChild(currentAST, returnAST);
 		JavaNode tmp157_AST = null;
 		tmp157_AST = (JavaNode)astFactory.create(LT(1));
 		astFactory.addASTChild(currentAST, tmp157_AST);
 		match(SEMI);
 		forCond();
 		astFactory.addASTChild(currentAST, returnAST);
 		JavaNode tmp158_AST = null;
 		tmp158_AST = (JavaNode)astFactory.create(LT(1));
 		astFactory.addASTChild(currentAST, tmp158_AST);
 		match(SEMI);
 		forIter();
 		astFactory.addASTChild(currentAST, returnAST);
 		traditionalForClause_AST = (JavaNode)currentAST.root;
 		returnAST = traditionalForClause_AST;
 	}
 	
 	public final void forEachClause() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode forEachClause_AST = null;
 		JavaNode p_AST = null;
 		
 		parameterDeclaration();
 		p_AST = (JavaNode)returnAST;
 		astFactory.addASTChild(currentAST, returnAST);
 		match(COLON);
 		expression();
 		astFactory.addASTChild(currentAST, returnAST);
 		if ( inputState.guessing==0 ) {
 			forEachClause_AST = (JavaNode)currentAST.root;
 			forEachClause_AST = (JavaNode)astFactory.make( (new ASTArray(2)).add((JavaNode)astFactory.create(FOR_EACH_CLAUSE,"FOR_EACH_CLAUSE")).add(forEachClause_AST));
 			currentAST.root = forEachClause_AST;
 			currentAST.child = forEachClause_AST!=null &&forEachClause_AST.getFirstChild()!=null ?
 				forEachClause_AST.getFirstChild() : forEachClause_AST;
 			currentAST.advanceChildToEnd();
 		}
 		forEachClause_AST = (JavaNode)currentAST.root;
 		returnAST = forEachClause_AST;
 	}
 	
 	public final void forCond() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode forCond_AST = null;
 		
 		{
 		switch ( LA(1)) {
 		case IDENT:
 		case LITERAL_super:
 		case LT:
 		case LITERAL_void:
 		case LITERAL_boolean:
 		case LITERAL_byte:
 		case LITERAL_char:
 		case LITERAL_short:
 		case LITERAL_int:
 		case LITERAL_float:
 		case LITERAL_long:
 		case LITERAL_double:
 		case LPAREN:
 		case LITERAL_this:
 		case PLUS:
 		case MINUS:
 		case INC:
 		case DEC:
 		case BNOT:
 		case LNOT:
 		case LITERAL_true:
 		case LITERAL_false:
 		case LITERAL_null:
 		case LITERAL_new:
 		case NUM_INT:
 		case CHAR_LITERAL:
 		case STRING_LITERAL:
 		case NUM_FLOAT:
 		case NUM_LONG:
 		case NUM_DOUBLE:
 		{
 			expression();
 			astFactory.addASTChild(currentAST, returnAST);
 			break;
 		}
 		case SEMI:
 		{
 			break;
 		}
 		default:
 		{
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		}
 		}
 		if ( inputState.guessing==0 ) {
 			forCond_AST = (JavaNode)currentAST.root;
 			forCond_AST = (JavaNode)astFactory.make( (new ASTArray(2)).add((JavaNode)astFactory.create(FOR_CONDITION,"FOR_CONDITION")).add(forCond_AST));
 			currentAST.root = forCond_AST;
 			currentAST.child = forCond_AST!=null &&forCond_AST.getFirstChild()!=null ?
 				forCond_AST.getFirstChild() : forCond_AST;
 			currentAST.advanceChildToEnd();
 		}
 		forCond_AST = (JavaNode)currentAST.root;
 		returnAST = forCond_AST;
 	}
 	
 	public final void forIter() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode forIter_AST = null;
 		
 		{
 		switch ( LA(1)) {
 		case IDENT:
 		case LITERAL_super:
 		case LT:
 		case LITERAL_void:
 		case LITERAL_boolean:
 		case LITERAL_byte:
 		case LITERAL_char:
 		case LITERAL_short:
 		case LITERAL_int:
 		case LITERAL_float:
 		case LITERAL_long:
 		case LITERAL_double:
 		case LPAREN:
 		case LITERAL_this:
 		case PLUS:
 		case MINUS:
 		case INC:
 		case DEC:
 		case BNOT:
 		case LNOT:
 		case LITERAL_true:
 		case LITERAL_false:
 		case LITERAL_null:
 		case LITERAL_new:
 		case NUM_INT:
 		case CHAR_LITERAL:
 		case STRING_LITERAL:
 		case NUM_FLOAT:
 		case NUM_LONG:
 		case NUM_DOUBLE:
 		{
 			expressionList();
 			astFactory.addASTChild(currentAST, returnAST);
 			break;
 		}
 		case RPAREN:
 		{
 			break;
 		}
 		default:
 		{
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		}
 		}
 		if ( inputState.guessing==0 ) {
 			forIter_AST = (JavaNode)currentAST.root;
 			forIter_AST = (JavaNode)astFactory.make( (new ASTArray(2)).add((JavaNode)astFactory.create(FOR_ITERATOR,"FOR_ITERATOR")).add(forIter_AST));
 			currentAST.root = forIter_AST;
 			currentAST.child = forIter_AST!=null &&forIter_AST.getFirstChild()!=null ?
 				forIter_AST.getFirstChild() : forIter_AST;
 			currentAST.advanceChildToEnd();
 		}
 		forIter_AST = (JavaNode)currentAST.root;
 		returnAST = forIter_AST;
 	}
 	
 	public final void aCase() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode aCase_AST = null;
 		
 		{
 		switch ( LA(1)) {
 		case LITERAL_case:
 		{
 			JavaNode tmp160_AST = null;
 			tmp160_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.makeASTRoot(currentAST, tmp160_AST);
 			match(LITERAL_case);
 			expression();
 			astFactory.addASTChild(currentAST, returnAST);
 			break;
 		}
 		case LITERAL_default:
 		{
 			JavaNode tmp161_AST = null;
 			tmp161_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp161_AST);
 			match(LITERAL_default);
 			break;
 		}
 		default:
 		{
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		}
 		}
 		JavaNode tmp162_AST = null;
 		tmp162_AST = (JavaNode)astFactory.create(LT(1));
 		astFactory.addASTChild(currentAST, tmp162_AST);
 		match(COLON);
 		aCase_AST = (JavaNode)currentAST.root;
 		returnAST = aCase_AST;
 	}
 	
 	public final void caseSList() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode caseSList_AST = null;
 		
 		{
 		_loop218:
 		do {
 			if ((_tokenSet_23.member(LA(1)))) {
 				statement();
 				astFactory.addASTChild(currentAST, returnAST);
 			}
 			else {
 				break _loop218;
 			}
 			
 		} while (true);
 		}
 		if ( inputState.guessing==0 ) {
 			caseSList_AST = (JavaNode)currentAST.root;
 			caseSList_AST = (JavaNode)astFactory.make( (new ASTArray(2)).add((JavaNode)astFactory.create(CASESLIST,"CASESLIST")).add(caseSList_AST));
 			currentAST.root = caseSList_AST;
 			currentAST.child = caseSList_AST!=null &&caseSList_AST.getFirstChild()!=null ?
 				caseSList_AST.getFirstChild() : caseSList_AST;
 			currentAST.advanceChildToEnd();
 		}
 		caseSList_AST = (JavaNode)currentAST.root;
 		returnAST = caseSList_AST;
 	}
 	
 	public final void expressionList() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode expressionList_AST = null;
 		
 		expression();
 		astFactory.addASTChild(currentAST, returnAST);
 		{
 		_loop236:
 		do {
 			if ((LA(1)==COMMA)) {
 				JavaNode tmp163_AST = null;
 				tmp163_AST = (JavaNode)astFactory.create(LT(1));
 				astFactory.addASTChild(currentAST, tmp163_AST);
 				match(COMMA);
 				expression();
 				astFactory.addASTChild(currentAST, returnAST);
 			}
 			else {
 				break _loop236;
 			}
 			
 		} while (true);
 		}
 		if ( inputState.guessing==0 ) {
 			expressionList_AST = (JavaNode)currentAST.root;
 			expressionList_AST = (JavaNode)astFactory.make( (new ASTArray(2)).add((JavaNode)astFactory.create(ELIST,"ELIST")).add(expressionList_AST));
 			currentAST.root = expressionList_AST;
 			currentAST.child = expressionList_AST!=null &&expressionList_AST.getFirstChild()!=null ?
 				expressionList_AST.getFirstChild() : expressionList_AST;
 			currentAST.advanceChildToEnd();
 		}
 		expressionList_AST = (JavaNode)currentAST.root;
 		returnAST = expressionList_AST;
 	}
 	
 	public final void handler() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode handler_AST = null;
 		
 		JavaNode tmp164_AST = null;
 		tmp164_AST = (JavaNode)astFactory.create(LT(1));
 		astFactory.makeASTRoot(currentAST, tmp164_AST);
 		match(LITERAL_catch);
 		JavaNode tmp165_AST = null;
 		tmp165_AST = (JavaNode)astFactory.create(LT(1));
 		astFactory.addASTChild(currentAST, tmp165_AST);
 		match(LPAREN);
 		parameterDeclaration();
 		astFactory.addASTChild(currentAST, returnAST);
 		JavaNode tmp166_AST = null;
 		tmp166_AST = (JavaNode)astFactory.create(LT(1));
 		astFactory.addASTChild(currentAST, tmp166_AST);
 		match(RPAREN);
 		compoundStatement();
 		astFactory.addASTChild(currentAST, returnAST);
 		handler_AST = (JavaNode)currentAST.root;
 		returnAST = handler_AST;
 	}
 	
 	public final void finallyClause() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode finallyClause_AST = null;
 		
 		JavaNode tmp167_AST = null;
 		tmp167_AST = (JavaNode)astFactory.create(LT(1));
 		astFactory.makeASTRoot(currentAST, tmp167_AST);
 		match(LITERAL_finally);
 		compoundStatement();
 		astFactory.addASTChild(currentAST, returnAST);
 		finallyClause_AST = (JavaNode)currentAST.root;
 		returnAST = finallyClause_AST;
 	}
 	
 	public final void assignmentExpression() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode assignmentExpression_AST = null;
 		
 		conditionalExpression();
 		astFactory.addASTChild(currentAST, returnAST);
 		{
 		switch ( LA(1)) {
 		case ASSIGN:
 		case PLUS_ASSIGN:
 		case MINUS_ASSIGN:
 		case STAR_ASSIGN:
 		case DIV_ASSIGN:
 		case MOD_ASSIGN:
 		case SR_ASSIGN:
 		case BSR_ASSIGN:
 		case SL_ASSIGN:
 		case BAND_ASSIGN:
 		case BXOR_ASSIGN:
 		case BOR_ASSIGN:
 		{
 			{
 			switch ( LA(1)) {
 			case ASSIGN:
 			{
 				JavaNode tmp168_AST = null;
 				tmp168_AST = (JavaNode)astFactory.create(LT(1));
 				astFactory.makeASTRoot(currentAST, tmp168_AST);
 				match(ASSIGN);
 				break;
 			}
 			case PLUS_ASSIGN:
 			{
 				JavaNode tmp169_AST = null;
 				tmp169_AST = (JavaNode)astFactory.create(LT(1));
 				astFactory.makeASTRoot(currentAST, tmp169_AST);
 				match(PLUS_ASSIGN);
 				break;
 			}
 			case MINUS_ASSIGN:
 			{
 				JavaNode tmp170_AST = null;
 				tmp170_AST = (JavaNode)astFactory.create(LT(1));
 				astFactory.makeASTRoot(currentAST, tmp170_AST);
 				match(MINUS_ASSIGN);
 				break;
 			}
 			case STAR_ASSIGN:
 			{
 				JavaNode tmp171_AST = null;
 				tmp171_AST = (JavaNode)astFactory.create(LT(1));
 				astFactory.makeASTRoot(currentAST, tmp171_AST);
 				match(STAR_ASSIGN);
 				break;
 			}
 			case DIV_ASSIGN:
 			{
 				JavaNode tmp172_AST = null;
 				tmp172_AST = (JavaNode)astFactory.create(LT(1));
 				astFactory.makeASTRoot(currentAST, tmp172_AST);
 				match(DIV_ASSIGN);
 				break;
 			}
 			case MOD_ASSIGN:
 			{
 				JavaNode tmp173_AST = null;
 				tmp173_AST = (JavaNode)astFactory.create(LT(1));
 				astFactory.makeASTRoot(currentAST, tmp173_AST);
 				match(MOD_ASSIGN);
 				break;
 			}
 			case SR_ASSIGN:
 			{
 				JavaNode tmp174_AST = null;
 				tmp174_AST = (JavaNode)astFactory.create(LT(1));
 				astFactory.makeASTRoot(currentAST, tmp174_AST);
 				match(SR_ASSIGN);
 				break;
 			}
 			case BSR_ASSIGN:
 			{
 				JavaNode tmp175_AST = null;
 				tmp175_AST = (JavaNode)astFactory.create(LT(1));
 				astFactory.makeASTRoot(currentAST, tmp175_AST);
 				match(BSR_ASSIGN);
 				break;
 			}
 			case SL_ASSIGN:
 			{
 				JavaNode tmp176_AST = null;
 				tmp176_AST = (JavaNode)astFactory.create(LT(1));
 				astFactory.makeASTRoot(currentAST, tmp176_AST);
 				match(SL_ASSIGN);
 				break;
 			}
 			case BAND_ASSIGN:
 			{
 				JavaNode tmp177_AST = null;
 				tmp177_AST = (JavaNode)astFactory.create(LT(1));
 				astFactory.makeASTRoot(currentAST, tmp177_AST);
 				match(BAND_ASSIGN);
 				break;
 			}
 			case BXOR_ASSIGN:
 			{
 				JavaNode tmp178_AST = null;
 				tmp178_AST = (JavaNode)astFactory.create(LT(1));
 				astFactory.makeASTRoot(currentAST, tmp178_AST);
 				match(BXOR_ASSIGN);
 				break;
 			}
 			case BOR_ASSIGN:
 			{
 				JavaNode tmp179_AST = null;
 				tmp179_AST = (JavaNode)astFactory.create(LT(1));
 				astFactory.makeASTRoot(currentAST, tmp179_AST);
 				match(BOR_ASSIGN);
 				break;
 			}
 			default:
 			{
 				throw new NoViableAltException(LT(1), getFilename());
 			}
 			}
 			}
 			assignmentExpression();
 			astFactory.addASTChild(currentAST, returnAST);
 			break;
 		}
 		case LCURLY:
 		case RCURLY:
 		case FINAL:
 		case ABSTRACT:
 		case STRICTFP:
 		case SEMI:
 		case LITERAL_static:
 		case RBRACK:
 		case IDENT:
 		case LT:
 		case COMMA:
 		case LITERAL_void:
 		case LITERAL_boolean:
 		case LITERAL_byte:
 		case LITERAL_char:
 		case LITERAL_short:
 		case LITERAL_int:
 		case LITERAL_float:
 		case LITERAL_long:
 		case LITERAL_double:
 		case LITERAL_private:
 		case LITERAL_public:
 		case LITERAL_protected:
 		case LITERAL_transient:
 		case LITERAL_native:
 		case LITERAL_threadsafe:
 		case LITERAL_synchronized:
 		case LITERAL_volatile:
 		case AT:
 		case RPAREN:
 		case LITERAL_class:
 		case LITERAL_interface:
 		case LITERAL_enum:
 		case COLON:
 		{
 			break;
 		}
 		default:
 		{
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		}
 		}
 		assignmentExpression_AST = (JavaNode)currentAST.root;
 		returnAST = assignmentExpression_AST;
 	}
 	
 	public final void logicalOrExpression() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode logicalOrExpression_AST = null;
 		
 		logicalAndExpression();
 		astFactory.addASTChild(currentAST, returnAST);
 		{
 		_loop244:
 		do {
 			if ((LA(1)==LOR)) {
 				JavaNode tmp180_AST = null;
 				tmp180_AST = (JavaNode)astFactory.create(LT(1));
 				astFactory.makeASTRoot(currentAST, tmp180_AST);
 				match(LOR);
 				logicalAndExpression();
 				astFactory.addASTChild(currentAST, returnAST);
 			}
 			else {
 				break _loop244;
 			}
 			
 		} while (true);
 		}
 		logicalOrExpression_AST = (JavaNode)currentAST.root;
 		returnAST = logicalOrExpression_AST;
 	}
 	
 	public final void logicalAndExpression() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode logicalAndExpression_AST = null;
 		
 		inclusiveOrExpression();
 		astFactory.addASTChild(currentAST, returnAST);
 		{
 		_loop247:
 		do {
 			if ((LA(1)==LAND)) {
 				JavaNode tmp181_AST = null;
 				tmp181_AST = (JavaNode)astFactory.create(LT(1));
 				astFactory.makeASTRoot(currentAST, tmp181_AST);
 				match(LAND);
 				inclusiveOrExpression();
 				astFactory.addASTChild(currentAST, returnAST);
 			}
 			else {
 				break _loop247;
 			}
 			
 		} while (true);
 		}
 		logicalAndExpression_AST = (JavaNode)currentAST.root;
 		returnAST = logicalAndExpression_AST;
 	}
 	
 	public final void inclusiveOrExpression() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode inclusiveOrExpression_AST = null;
 		
 		exclusiveOrExpression();
 		astFactory.addASTChild(currentAST, returnAST);
 		{
 		_loop250:
 		do {
 			if ((LA(1)==BOR)) {
 				JavaNode tmp182_AST = null;
 				tmp182_AST = (JavaNode)astFactory.create(LT(1));
 				astFactory.makeASTRoot(currentAST, tmp182_AST);
 				match(BOR);
 				exclusiveOrExpression();
 				astFactory.addASTChild(currentAST, returnAST);
 			}
 			else {
 				break _loop250;
 			}
 			
 		} while (true);
 		}
 		inclusiveOrExpression_AST = (JavaNode)currentAST.root;
 		returnAST = inclusiveOrExpression_AST;
 	}
 	
 	public final void exclusiveOrExpression() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode exclusiveOrExpression_AST = null;
 		
 		andExpression();
 		astFactory.addASTChild(currentAST, returnAST);
 		{
 		_loop253:
 		do {
 			if ((LA(1)==BXOR)) {
 				JavaNode tmp183_AST = null;
 				tmp183_AST = (JavaNode)astFactory.create(LT(1));
 				astFactory.makeASTRoot(currentAST, tmp183_AST);
 				match(BXOR);
 				andExpression();
 				astFactory.addASTChild(currentAST, returnAST);
 			}
 			else {
 				break _loop253;
 			}
 			
 		} while (true);
 		}
 		exclusiveOrExpression_AST = (JavaNode)currentAST.root;
 		returnAST = exclusiveOrExpression_AST;
 	}
 	
 	public final void andExpression() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode andExpression_AST = null;
 		
 		equalityExpression();
 		astFactory.addASTChild(currentAST, returnAST);
 		{
 		_loop256:
 		do {
 			if ((LA(1)==BAND)) {
 				JavaNode tmp184_AST = null;
 				tmp184_AST = (JavaNode)astFactory.create(LT(1));
 				astFactory.makeASTRoot(currentAST, tmp184_AST);
 				match(BAND);
 				equalityExpression();
 				astFactory.addASTChild(currentAST, returnAST);
 			}
 			else {
 				break _loop256;
 			}
 			
 		} while (true);
 		}
 		andExpression_AST = (JavaNode)currentAST.root;
 		returnAST = andExpression_AST;
 	}
 	
 	public final void equalityExpression() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode equalityExpression_AST = null;
 		
 		relationalExpression();
 		astFactory.addASTChild(currentAST, returnAST);
 		{
 		_loop260:
 		do {
 			if ((LA(1)==NOT_EQUAL||LA(1)==EQUAL)) {
 				{
 				switch ( LA(1)) {
 				case NOT_EQUAL:
 				{
 					JavaNode tmp185_AST = null;
 					tmp185_AST = (JavaNode)astFactory.create(LT(1));
 					astFactory.makeASTRoot(currentAST, tmp185_AST);
 					match(NOT_EQUAL);
 					break;
 				}
 				case EQUAL:
 				{
 					JavaNode tmp186_AST = null;
 					tmp186_AST = (JavaNode)astFactory.create(LT(1));
 					astFactory.makeASTRoot(currentAST, tmp186_AST);
 					match(EQUAL);
 					break;
 				}
 				default:
 				{
 					throw new NoViableAltException(LT(1), getFilename());
 				}
 				}
 				}
 				relationalExpression();
 				astFactory.addASTChild(currentAST, returnAST);
 			}
 			else {
 				break _loop260;
 			}
 			
 		} while (true);
 		}
 		equalityExpression_AST = (JavaNode)currentAST.root;
 		returnAST = equalityExpression_AST;
 	}
 	
 	public final void relationalExpression() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode relationalExpression_AST = null;
 		
 		shiftExpression();
 		astFactory.addASTChild(currentAST, returnAST);
 		{
 		switch ( LA(1)) {
 		case LCURLY:
 		case RCURLY:
 		case FINAL:
 		case ABSTRACT:
 		case STRICTFP:
 		case SEMI:
 		case LITERAL_static:
 		case RBRACK:
 		case IDENT:
 		case QUESTION:
 		case LT:
 		case COMMA:
 		case GT:
 		case LITERAL_void:
 		case LITERAL_boolean:
 		case LITERAL_byte:
 		case LITERAL_char:
 		case LITERAL_short:
 		case LITERAL_int:
 		case LITERAL_float:
 		case LITERAL_long:
 		case LITERAL_double:
 		case LITERAL_private:
 		case LITERAL_public:
 		case LITERAL_protected:
 		case LITERAL_transient:
 		case LITERAL_native:
 		case LITERAL_threadsafe:
 		case LITERAL_synchronized:
 		case LITERAL_volatile:
 		case AT:
 		case RPAREN:
 		case ASSIGN:
 		case LITERAL_class:
 		case LITERAL_interface:
 		case LITERAL_enum:
 		case BAND:
 		case COLON:
 		case PLUS_ASSIGN:
 		case MINUS_ASSIGN:
 		case STAR_ASSIGN:
 		case DIV_ASSIGN:
 		case MOD_ASSIGN:
 		case SR_ASSIGN:
 		case BSR_ASSIGN:
 		case SL_ASSIGN:
 		case BAND_ASSIGN:
 		case BXOR_ASSIGN:
 		case BOR_ASSIGN:
 		case LOR:
 		case LAND:
 		case BOR:
 		case BXOR:
 		case NOT_EQUAL:
 		case EQUAL:
 		case LE:
 		case GE:
 		{
 			{
 			_loop265:
 			do {
 				if ((_tokenSet_41.member(LA(1))) && (_tokenSet_32.member(LA(2)))) {
 					{
 					switch ( LA(1)) {
 					case LT:
 					{
 						JavaNode tmp187_AST = null;
 						tmp187_AST = (JavaNode)astFactory.create(LT(1));
 						astFactory.makeASTRoot(currentAST, tmp187_AST);
 						match(LT);
 						break;
 					}
 					case GT:
 					{
 						JavaNode tmp188_AST = null;
 						tmp188_AST = (JavaNode)astFactory.create(LT(1));
 						astFactory.makeASTRoot(currentAST, tmp188_AST);
 						match(GT);
 						break;
 					}
 					case LE:
 					{
 						JavaNode tmp189_AST = null;
 						tmp189_AST = (JavaNode)astFactory.create(LT(1));
 						astFactory.makeASTRoot(currentAST, tmp189_AST);
 						match(LE);
 						break;
 					}
 					case GE:
 					{
 						JavaNode tmp190_AST = null;
 						tmp190_AST = (JavaNode)astFactory.create(LT(1));
 						astFactory.makeASTRoot(currentAST, tmp190_AST);
 						match(GE);
 						break;
 					}
 					default:
 					{
 						throw new NoViableAltException(LT(1), getFilename());
 					}
 					}
 					}
 					shiftExpression();
 					astFactory.addASTChild(currentAST, returnAST);
 				}
 				else {
 					break _loop265;
 				}
 				
 			} while (true);
 			}
 			break;
 		}
 		case LITERAL_instanceof:
 		{
 			JavaNode tmp191_AST = null;
 			tmp191_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.makeASTRoot(currentAST, tmp191_AST);
 			match(LITERAL_instanceof);
 			typeSpec(true);
 			astFactory.addASTChild(currentAST, returnAST);
 			break;
 		}
 		default:
 		{
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		}
 		}
 		relationalExpression_AST = (JavaNode)currentAST.root;
 		returnAST = relationalExpression_AST;
 	}
 	
 	public final void shiftExpression() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode shiftExpression_AST = null;
 		
 		additiveExpression();
 		astFactory.addASTChild(currentAST, returnAST);
 		{
 		_loop269:
 		do {
 			if ((_tokenSet_42.member(LA(1)))) {
 				{
 				switch ( LA(1)) {
 				case SL:
 				{
 					JavaNode tmp192_AST = null;
 					tmp192_AST = (JavaNode)astFactory.create(LT(1));
 					astFactory.makeASTRoot(currentAST, tmp192_AST);
 					match(SL);
 					break;
 				}
 				case SR:
 				{
 					JavaNode tmp193_AST = null;
 					tmp193_AST = (JavaNode)astFactory.create(LT(1));
 					astFactory.makeASTRoot(currentAST, tmp193_AST);
 					match(SR);
 					break;
 				}
 				case BSR:
 				{
 					JavaNode tmp194_AST = null;
 					tmp194_AST = (JavaNode)astFactory.create(LT(1));
 					astFactory.makeASTRoot(currentAST, tmp194_AST);
 					match(BSR);
 					break;
 				}
 				default:
 				{
 					throw new NoViableAltException(LT(1), getFilename());
 				}
 				}
 				}
 				additiveExpression();
 				astFactory.addASTChild(currentAST, returnAST);
 			}
 			else {
 				break _loop269;
 			}
 			
 		} while (true);
 		}
 		shiftExpression_AST = (JavaNode)currentAST.root;
 		returnAST = shiftExpression_AST;
 	}
 	
 	public final void additiveExpression() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode additiveExpression_AST = null;
 		
 		multiplicativeExpression();
 		astFactory.addASTChild(currentAST, returnAST);
 		{
 		_loop273:
 		do {
 			if ((LA(1)==PLUS||LA(1)==MINUS)) {
 				{
 				switch ( LA(1)) {
 				case PLUS:
 				{
 					JavaNode tmp195_AST = null;
 					tmp195_AST = (JavaNode)astFactory.create(LT(1));
 					astFactory.makeASTRoot(currentAST, tmp195_AST);
 					match(PLUS);
 					break;
 				}
 				case MINUS:
 				{
 					JavaNode tmp196_AST = null;
 					tmp196_AST = (JavaNode)astFactory.create(LT(1));
 					astFactory.makeASTRoot(currentAST, tmp196_AST);
 					match(MINUS);
 					break;
 				}
 				default:
 				{
 					throw new NoViableAltException(LT(1), getFilename());
 				}
 				}
 				}
 				multiplicativeExpression();
 				astFactory.addASTChild(currentAST, returnAST);
 			}
 			else {
 				break _loop273;
 			}
 			
 		} while (true);
 		}
 		additiveExpression_AST = (JavaNode)currentAST.root;
 		returnAST = additiveExpression_AST;
 	}
 	
 	public final void multiplicativeExpression() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode multiplicativeExpression_AST = null;
 		
 		unaryExpression();
 		astFactory.addASTChild(currentAST, returnAST);
 		{
 		_loop277:
 		do {
 			if ((_tokenSet_43.member(LA(1)))) {
 				{
 				switch ( LA(1)) {
 				case STAR:
 				{
 					JavaNode tmp197_AST = null;
 					tmp197_AST = (JavaNode)astFactory.create(LT(1));
 					astFactory.makeASTRoot(currentAST, tmp197_AST);
 					match(STAR);
 					break;
 				}
 				case DIV:
 				{
 					JavaNode tmp198_AST = null;
 					tmp198_AST = (JavaNode)astFactory.create(LT(1));
 					astFactory.makeASTRoot(currentAST, tmp198_AST);
 					match(DIV);
 					break;
 				}
 				case MOD:
 				{
 					JavaNode tmp199_AST = null;
 					tmp199_AST = (JavaNode)astFactory.create(LT(1));
 					astFactory.makeASTRoot(currentAST, tmp199_AST);
 					match(MOD);
 					break;
 				}
 				default:
 				{
 					throw new NoViableAltException(LT(1), getFilename());
 				}
 				}
 				}
 				unaryExpression();
 				astFactory.addASTChild(currentAST, returnAST);
 			}
 			else {
 				break _loop277;
 			}
 			
 		} while (true);
 		}
 		multiplicativeExpression_AST = (JavaNode)currentAST.root;
 		returnAST = multiplicativeExpression_AST;
 	}
 	
 	public final void unaryExpression() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode unaryExpression_AST = null;
 		
 		switch ( LA(1)) {
 		case INC:
 		{
 			JavaNode tmp200_AST = null;
 			tmp200_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.makeASTRoot(currentAST, tmp200_AST);
 			match(INC);
 			unaryExpression();
 			astFactory.addASTChild(currentAST, returnAST);
 			unaryExpression_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case DEC:
 		{
 			JavaNode tmp201_AST = null;
 			tmp201_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.makeASTRoot(currentAST, tmp201_AST);
 			match(DEC);
 			unaryExpression();
 			astFactory.addASTChild(currentAST, returnAST);
 			unaryExpression_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case MINUS:
 		{
 			JavaNode tmp202_AST = null;
 			tmp202_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.makeASTRoot(currentAST, tmp202_AST);
 			match(MINUS);
 			if ( inputState.guessing==0 ) {
 				tmp202_AST.setType(UNARY_MINUS);
 			}
 			unaryExpression();
 			astFactory.addASTChild(currentAST, returnAST);
 			unaryExpression_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case PLUS:
 		{
 			JavaNode tmp203_AST = null;
 			tmp203_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.makeASTRoot(currentAST, tmp203_AST);
 			match(PLUS);
 			if ( inputState.guessing==0 ) {
 				tmp203_AST.setType(UNARY_PLUS);
 			}
 			unaryExpression();
 			astFactory.addASTChild(currentAST, returnAST);
 			unaryExpression_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case IDENT:
 		case LITERAL_super:
 		case LT:
 		case LITERAL_void:
 		case LITERAL_boolean:
 		case LITERAL_byte:
 		case LITERAL_char:
 		case LITERAL_short:
 		case LITERAL_int:
 		case LITERAL_float:
 		case LITERAL_long:
 		case LITERAL_double:
 		case LPAREN:
 		case LITERAL_this:
 		case BNOT:
 		case LNOT:
 		case LITERAL_true:
 		case LITERAL_false:
 		case LITERAL_null:
 		case LITERAL_new:
 		case NUM_INT:
 		case CHAR_LITERAL:
 		case STRING_LITERAL:
 		case NUM_FLOAT:
 		case NUM_LONG:
 		case NUM_DOUBLE:
 		{
 			unaryExpressionNotPlusMinus();
 			astFactory.addASTChild(currentAST, returnAST);
 			unaryExpression_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		default:
 		{
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		}
 		returnAST = unaryExpression_AST;
 	}
 	
 	public final void unaryExpressionNotPlusMinus() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode unaryExpressionNotPlusMinus_AST = null;
 		Token  lpb = null;
 		JavaNode lpb_AST = null;
 		Token  lp = null;
 		JavaNode lp_AST = null;
 		
 		switch ( LA(1)) {
 		case BNOT:
 		{
 			JavaNode tmp204_AST = null;
 			tmp204_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.makeASTRoot(currentAST, tmp204_AST);
 			match(BNOT);
 			unaryExpression();
 			astFactory.addASTChild(currentAST, returnAST);
 			unaryExpressionNotPlusMinus_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case LNOT:
 		{
 			JavaNode tmp205_AST = null;
 			tmp205_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.makeASTRoot(currentAST, tmp205_AST);
 			match(LNOT);
 			unaryExpression();
 			astFactory.addASTChild(currentAST, returnAST);
 			unaryExpressionNotPlusMinus_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case IDENT:
 		case LITERAL_super:
 		case LT:
 		case LITERAL_void:
 		case LITERAL_boolean:
 		case LITERAL_byte:
 		case LITERAL_char:
 		case LITERAL_short:
 		case LITERAL_int:
 		case LITERAL_float:
 		case LITERAL_long:
 		case LITERAL_double:
 		case LPAREN:
 		case LITERAL_this:
 		case LITERAL_true:
 		case LITERAL_false:
 		case LITERAL_null:
 		case LITERAL_new:
 		case NUM_INT:
 		case CHAR_LITERAL:
 		case STRING_LITERAL:
 		case NUM_FLOAT:
 		case NUM_LONG:
 		case NUM_DOUBLE:
 		{
 			{
 			boolean synPredMatched282 = false;
 			if (((LA(1)==LPAREN) && ((LA(2) >= LITERAL_void && LA(2) <= LITERAL_double)))) {
 				int _m282 = mark();
 				synPredMatched282 = true;
 				inputState.guessing++;
 				try {
 					{
 					match(LPAREN);
 					builtInTypeSpec(true);
 					match(RPAREN);
 					unaryExpression();
 					}
 				}
 				catch (RecognitionException pe) {
 					synPredMatched282 = false;
 				}
 				rewind(_m282);
 				inputState.guessing--;
 			}
 			if ( synPredMatched282 ) {
 				lpb = LT(1);
 				lpb_AST = (JavaNode)astFactory.create(lpb);
 				astFactory.makeASTRoot(currentAST, lpb_AST);
 				match(LPAREN);
 				if ( inputState.guessing==0 ) {
 					lpb_AST.setType(TYPECAST);
 				}
 				builtInTypeSpec(true);
 				astFactory.addASTChild(currentAST, returnAST);
 				JavaNode tmp206_AST = null;
 				tmp206_AST = (JavaNode)astFactory.create(LT(1));
 				astFactory.addASTChild(currentAST, tmp206_AST);
 				match(RPAREN);
 				unaryExpression();
 				astFactory.addASTChild(currentAST, returnAST);
 			}
 			else {
 				boolean synPredMatched284 = false;
 				if (((LA(1)==LPAREN) && (LA(2)==IDENT))) {
 					int _m284 = mark();
 					synPredMatched284 = true;
 					inputState.guessing++;
 					try {
 						{
 						match(LPAREN);
 						classTypeSpec(true);
 						match(RPAREN);
 						unaryExpressionNotPlusMinus();
 						}
 					}
 					catch (RecognitionException pe) {
 						synPredMatched284 = false;
 					}
 					rewind(_m284);
 					inputState.guessing--;
 				}
 				if ( synPredMatched284 ) {
 					lp = LT(1);
 					lp_AST = (JavaNode)astFactory.create(lp);
 					astFactory.makeASTRoot(currentAST, lp_AST);
 					match(LPAREN);
 					if ( inputState.guessing==0 ) {
 						lp_AST.setType(TYPECAST);
 					}
 					classTypeSpec(true);
 					astFactory.addASTChild(currentAST, returnAST);
 					match(RPAREN);
 					unaryExpressionNotPlusMinus();
 					astFactory.addASTChild(currentAST, returnAST);
 				}
 				else if ((_tokenSet_44.member(LA(1))) && (_tokenSet_45.member(LA(2)))) {
 					postfixExpression();
 					astFactory.addASTChild(currentAST, returnAST);
 				}
 				else {
 					throw new NoViableAltException(LT(1), getFilename());
 				}
 				}
 				}
 				unaryExpressionNotPlusMinus_AST = (JavaNode)currentAST.root;
 				break;
 			}
 			default:
 			{
 				throw new NoViableAltException(LT(1), getFilename());
 			}
 			}
 			returnAST = unaryExpressionNotPlusMinus_AST;
 		}
 		
 	public final void postfixExpression() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode postfixExpression_AST = null;
 		Token  lp = null;
 		JavaNode lp_AST = null;
 		Token  lp3 = null;
 		JavaNode lp3_AST = null;
 		Token  lps = null;
 		JavaNode lps_AST = null;
 		Token  lb = null;
 		JavaNode lb_AST = null;
 		Token  in = null;
 		JavaNode in_AST = null;
 		Token  de = null;
 		JavaNode de_AST = null;
 		
 		primaryExpression();
 		astFactory.addASTChild(currentAST, returnAST);
 		{
 		_loop293:
 		do {
 			if ((LA(1)==DOT) && (_tokenSet_46.member(LA(2)))) {
 				JavaNode tmp208_AST = null;
 				tmp208_AST = (JavaNode)astFactory.create(LT(1));
 				astFactory.makeASTRoot(currentAST, tmp208_AST);
 				match(DOT);
 				{
 				switch ( LA(1)) {
 				case LT:
 				{
 					typeArguments();
 					astFactory.addASTChild(currentAST, returnAST);
 					break;
 				}
 				case IDENT:
 				case LITERAL_super:
 				{
 					break;
 				}
 				default:
 				{
 					throw new NoViableAltException(LT(1), getFilename());
 				}
 				}
 				}
 				{
 				switch ( LA(1)) {
 				case IDENT:
 				{
 					JavaNode tmp209_AST = null;
 					tmp209_AST = (JavaNode)astFactory.create(LT(1));
 					astFactory.addASTChild(currentAST, tmp209_AST);
 					match(IDENT);
 					{
 					switch ( LA(1)) {
 					case LPAREN:
 					{
 						lp = LT(1);
 						lp_AST = (JavaNode)astFactory.create(lp);
 						astFactory.makeASTRoot(currentAST, lp_AST);
 						match(LPAREN);
 						if ( inputState.guessing==0 ) {
 							lp_AST.setType(METHOD_CALL);
 						}
 						argList();
 						astFactory.addASTChild(currentAST, returnAST);
 						JavaNode tmp210_AST = null;
 						tmp210_AST = (JavaNode)astFactory.create(LT(1));
 						astFactory.addASTChild(currentAST, tmp210_AST);
 						match(RPAREN);
 						break;
 					}
 					case LCURLY:
 					case RCURLY:
 					case FINAL:
 					case ABSTRACT:
 					case STRICTFP:
 					case SEMI:
 					case LITERAL_static:
 					case LBRACK:
 					case RBRACK:
 					case IDENT:
 					case DOT:
 					case QUESTION:
 					case LT:
 					case COMMA:
 					case GT:
 					case SR:
 					case BSR:
 					case LITERAL_void:
 					case LITERAL_boolean:
 					case LITERAL_byte:
 					case LITERAL_char:
 					case LITERAL_short:
 					case LITERAL_int:
 					case LITERAL_float:
 					case LITERAL_long:
 					case LITERAL_double:
 					case STAR:
 					case LITERAL_private:
 					case LITERAL_public:
 					case LITERAL_protected:
 					case LITERAL_transient:
 					case LITERAL_native:
 					case LITERAL_threadsafe:
 					case LITERAL_synchronized:
 					case LITERAL_volatile:
 					case AT:
 					case RPAREN:
 					case ASSIGN:
 					case LITERAL_class:
 					case LITERAL_interface:
 					case LITERAL_enum:
 					case BAND:
 					case COLON:
 					case PLUS_ASSIGN:
 					case MINUS_ASSIGN:
 					case STAR_ASSIGN:
 					case DIV_ASSIGN:
 					case MOD_ASSIGN:
 					case SR_ASSIGN:
 					case BSR_ASSIGN:
 					case SL_ASSIGN:
 					case BAND_ASSIGN:
 					case BXOR_ASSIGN:
 					case BOR_ASSIGN:
 					case LOR:
 					case LAND:
 					case BOR:
 					case BXOR:
 					case NOT_EQUAL:
 					case EQUAL:
 					case LE:
 					case GE:
 					case LITERAL_instanceof:
 					case SL:
 					case PLUS:
 					case MINUS:
 					case DIV:
 					case MOD:
 					case INC:
 					case DEC:
 					{
 						break;
 					}
 					default:
 					{
 						throw new NoViableAltException(LT(1), getFilename());
 					}
 					}
 					}
 					break;
 				}
 				case LITERAL_super:
 				{
 					JavaNode tmp211_AST = null;
 					tmp211_AST = (JavaNode)astFactory.create(LT(1));
 					astFactory.addASTChild(currentAST, tmp211_AST);
 					match(LITERAL_super);
 					{
 					switch ( LA(1)) {
 					case LPAREN:
 					{
 						lp3 = LT(1);
 						lp3_AST = (JavaNode)astFactory.create(lp3);
 						astFactory.makeASTRoot(currentAST, lp3_AST);
 						match(LPAREN);
 						argList();
 						astFactory.addASTChild(currentAST, returnAST);
 						JavaNode tmp212_AST = null;
 						tmp212_AST = (JavaNode)astFactory.create(LT(1));
 						astFactory.addASTChild(currentAST, tmp212_AST);
 						match(RPAREN);
 						if ( inputState.guessing==0 ) {
 							lp3_AST.setType(SUPER_CTOR_CALL);
 						}
 						break;
 					}
 					case DOT:
 					{
 						JavaNode tmp213_AST = null;
 						tmp213_AST = (JavaNode)astFactory.create(LT(1));
 						astFactory.makeASTRoot(currentAST, tmp213_AST);
 						match(DOT);
 						{
 						switch ( LA(1)) {
 						case LT:
 						{
 							typeArguments();
 							astFactory.addASTChild(currentAST, returnAST);
 							break;
 						}
 						case IDENT:
 						{
 							break;
 						}
 						default:
 						{
 							throw new NoViableAltException(LT(1), getFilename());
 						}
 						}
 						}
 						JavaNode tmp214_AST = null;
 						tmp214_AST = (JavaNode)astFactory.create(LT(1));
 						astFactory.addASTChild(currentAST, tmp214_AST);
 						match(IDENT);
 						{
 						switch ( LA(1)) {
 						case LPAREN:
 						{
 							lps = LT(1);
 							lps_AST = (JavaNode)astFactory.create(lps);
 							astFactory.makeASTRoot(currentAST, lps_AST);
 							match(LPAREN);
 							if ( inputState.guessing==0 ) {
 								lps_AST.setType(METHOD_CALL);
 							}
 							argList();
 							astFactory.addASTChild(currentAST, returnAST);
 							JavaNode tmp215_AST = null;
 							tmp215_AST = (JavaNode)astFactory.create(LT(1));
 							astFactory.addASTChild(currentAST, tmp215_AST);
 							match(RPAREN);
 							break;
 						}
 						case LCURLY:
 						case RCURLY:
 						case FINAL:
 						case ABSTRACT:
 						case STRICTFP:
 						case SEMI:
 						case LITERAL_static:
 						case LBRACK:
 						case RBRACK:
 						case IDENT:
 						case DOT:
 						case QUESTION:
 						case LT:
 						case COMMA:
 						case GT:
 						case SR:
 						case BSR:
 						case LITERAL_void:
 						case LITERAL_boolean:
 						case LITERAL_byte:
 						case LITERAL_char:
 						case LITERAL_short:
 						case LITERAL_int:
 						case LITERAL_float:
 						case LITERAL_long:
 						case LITERAL_double:
 						case STAR:
 						case LITERAL_private:
 						case LITERAL_public:
 						case LITERAL_protected:
 						case LITERAL_transient:
 						case LITERAL_native:
 						case LITERAL_threadsafe:
 						case LITERAL_synchronized:
 						case LITERAL_volatile:
 						case AT:
 						case RPAREN:
 						case ASSIGN:
 						case LITERAL_class:
 						case LITERAL_interface:
 						case LITERAL_enum:
 						case BAND:
 						case COLON:
 						case PLUS_ASSIGN:
 						case MINUS_ASSIGN:
 						case STAR_ASSIGN:
 						case DIV_ASSIGN:
 						case MOD_ASSIGN:
 						case SR_ASSIGN:
 						case BSR_ASSIGN:
 						case SL_ASSIGN:
 						case BAND_ASSIGN:
 						case BXOR_ASSIGN:
 						case BOR_ASSIGN:
 						case LOR:
 						case LAND:
 						case BOR:
 						case BXOR:
 						case NOT_EQUAL:
 						case EQUAL:
 						case LE:
 						case GE:
 						case LITERAL_instanceof:
 						case SL:
 						case PLUS:
 						case MINUS:
 						case DIV:
 						case MOD:
 						case INC:
 						case DEC:
 						{
 							break;
 						}
 						default:
 						{
 							throw new NoViableAltException(LT(1), getFilename());
 						}
 						}
 						}
 						break;
 					}
 					default:
 					{
 						throw new NoViableAltException(LT(1), getFilename());
 					}
 					}
 					}
 					break;
 				}
 				default:
 				{
 					throw new NoViableAltException(LT(1), getFilename());
 				}
 				}
 				}
 			}
 			else if ((LA(1)==DOT) && (LA(2)==LITERAL_this)) {
 				JavaNode tmp216_AST = null;
 				tmp216_AST = (JavaNode)astFactory.create(LT(1));
 				astFactory.makeASTRoot(currentAST, tmp216_AST);
 				match(DOT);
 				JavaNode tmp217_AST = null;
 				tmp217_AST = (JavaNode)astFactory.create(LT(1));
 				astFactory.addASTChild(currentAST, tmp217_AST);
 				match(LITERAL_this);
 			}
 			else if ((LA(1)==DOT) && (LA(2)==LITERAL_new)) {
 				JavaNode tmp218_AST = null;
 				tmp218_AST = (JavaNode)astFactory.create(LT(1));
 				astFactory.makeASTRoot(currentAST, tmp218_AST);
 				match(DOT);
 				newExpression();
 				astFactory.addASTChild(currentAST, returnAST);
 			}
 			else if ((LA(1)==LBRACK)) {
 				lb = LT(1);
 				lb_AST = (JavaNode)astFactory.create(lb);
 				astFactory.makeASTRoot(currentAST, lb_AST);
 				match(LBRACK);
 				if ( inputState.guessing==0 ) {
 					lb_AST.setType(INDEX_OP);
 				}
 				expression();
 				astFactory.addASTChild(currentAST, returnAST);
 				JavaNode tmp219_AST = null;
 				tmp219_AST = (JavaNode)astFactory.create(LT(1));
 				astFactory.addASTChild(currentAST, tmp219_AST);
 				match(RBRACK);
 			}
 			else {
 				break _loop293;
 			}
 			
 		} while (true);
 		}
 		{
 		switch ( LA(1)) {
 		case INC:
 		{
 			in = LT(1);
 			in_AST = (JavaNode)astFactory.create(in);
 			astFactory.makeASTRoot(currentAST, in_AST);
 			match(INC);
 			if ( inputState.guessing==0 ) {
 				in_AST.setType(POST_INC);
 			}
 			break;
 		}
 		case DEC:
 		{
 			de = LT(1);
 			de_AST = (JavaNode)astFactory.create(de);
 			astFactory.makeASTRoot(currentAST, de_AST);
 			match(DEC);
 			if ( inputState.guessing==0 ) {
 				de_AST.setType(POST_DEC);
 			}
 			break;
 		}
 		case LCURLY:
 		case RCURLY:
 		case FINAL:
 		case ABSTRACT:
 		case STRICTFP:
 		case SEMI:
 		case LITERAL_static:
 		case RBRACK:
 		case IDENT:
 		case QUESTION:
 		case LT:
 		case COMMA:
 		case GT:
 		case SR:
 		case BSR:
 		case LITERAL_void:
 		case LITERAL_boolean:
 		case LITERAL_byte:
 		case LITERAL_char:
 		case LITERAL_short:
 		case LITERAL_int:
 		case LITERAL_float:
 		case LITERAL_long:
 		case LITERAL_double:
 		case STAR:
 		case LITERAL_private:
 		case LITERAL_public:
 		case LITERAL_protected:
 		case LITERAL_transient:
 		case LITERAL_native:
 		case LITERAL_threadsafe:
 		case LITERAL_synchronized:
 		case LITERAL_volatile:
 		case AT:
 		case RPAREN:
 		case ASSIGN:
 		case LITERAL_class:
 		case LITERAL_interface:
 		case LITERAL_enum:
 		case BAND:
 		case COLON:
 		case PLUS_ASSIGN:
 		case MINUS_ASSIGN:
 		case STAR_ASSIGN:
 		case DIV_ASSIGN:
 		case MOD_ASSIGN:
 		case SR_ASSIGN:
 		case BSR_ASSIGN:
 		case SL_ASSIGN:
 		case BAND_ASSIGN:
 		case BXOR_ASSIGN:
 		case BOR_ASSIGN:
 		case LOR:
 		case LAND:
 		case BOR:
 		case BXOR:
 		case NOT_EQUAL:
 		case EQUAL:
 		case LE:
 		case GE:
 		case LITERAL_instanceof:
 		case SL:
 		case PLUS:
 		case MINUS:
 		case DIV:
 		case MOD:
 		{
 			break;
 		}
 		default:
 		{
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		}
 		}
 		postfixExpression_AST = (JavaNode)currentAST.root;
 		returnAST = postfixExpression_AST;
 	}
 	
 	public final void primaryExpression() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode primaryExpression_AST = null;
 		Token  lbt = null;
 		JavaNode lbt_AST = null;
 		
 		switch ( LA(1)) {
 		case IDENT:
 		case LT:
 		{
 			identPrimary();
 			astFactory.addASTChild(currentAST, returnAST);
 			{
 			if ((LA(1)==DOT) && (LA(2)==LITERAL_class)) {
 				JavaNode tmp220_AST = null;
 				tmp220_AST = (JavaNode)astFactory.create(LT(1));
 				astFactory.makeASTRoot(currentAST, tmp220_AST);
 				match(DOT);
 				JavaNode tmp221_AST = null;
 				tmp221_AST = (JavaNode)astFactory.create(LT(1));
 				astFactory.addASTChild(currentAST, tmp221_AST);
 				match(LITERAL_class);
 			}
 			else if ((_tokenSet_47.member(LA(1))) && (_tokenSet_48.member(LA(2)))) {
 			}
 			else {
 				throw new NoViableAltException(LT(1), getFilename());
 			}
 			
 			}
 			primaryExpression_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case NUM_INT:
 		case CHAR_LITERAL:
 		case STRING_LITERAL:
 		case NUM_FLOAT:
 		case NUM_LONG:
 		case NUM_DOUBLE:
 		{
 			constant();
 			astFactory.addASTChild(currentAST, returnAST);
 			primaryExpression_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case LITERAL_true:
 		{
 			JavaNode tmp222_AST = null;
 			tmp222_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp222_AST);
 			match(LITERAL_true);
 			primaryExpression_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case LITERAL_false:
 		{
 			JavaNode tmp223_AST = null;
 			tmp223_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp223_AST);
 			match(LITERAL_false);
 			primaryExpression_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case LITERAL_null:
 		{
 			JavaNode tmp224_AST = null;
 			tmp224_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp224_AST);
 			match(LITERAL_null);
 			primaryExpression_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case LITERAL_new:
 		{
 			newExpression();
 			astFactory.addASTChild(currentAST, returnAST);
 			primaryExpression_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case LITERAL_this:
 		{
 			JavaNode tmp225_AST = null;
 			tmp225_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp225_AST);
 			match(LITERAL_this);
 			primaryExpression_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case LITERAL_super:
 		{
 			JavaNode tmp226_AST = null;
 			tmp226_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp226_AST);
 			match(LITERAL_super);
 			primaryExpression_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case LPAREN:
 		{
 			JavaNode tmp227_AST = null;
 			tmp227_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp227_AST);
 			match(LPAREN);
 			assignmentExpression();
 			astFactory.addASTChild(currentAST, returnAST);
 			JavaNode tmp228_AST = null;
 			tmp228_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp228_AST);
 			match(RPAREN);
 			primaryExpression_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case LITERAL_void:
 		case LITERAL_boolean:
 		case LITERAL_byte:
 		case LITERAL_char:
 		case LITERAL_short:
 		case LITERAL_int:
 		case LITERAL_float:
 		case LITERAL_long:
 		case LITERAL_double:
 		{
 			builtInType();
 			astFactory.addASTChild(currentAST, returnAST);
 			{
 			_loop298:
 			do {
 				if ((LA(1)==LBRACK)) {
 					lbt = LT(1);
 					lbt_AST = (JavaNode)astFactory.create(lbt);
 					astFactory.makeASTRoot(currentAST, lbt_AST);
 					match(LBRACK);
 					if ( inputState.guessing==0 ) {
 						lbt_AST.setType(ARRAY_DECLARATOR);
 					}
 					match(RBRACK);
 				}
 				else {
 					break _loop298;
 				}
 				
 			} while (true);
 			}
 			JavaNode tmp230_AST = null;
 			tmp230_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.makeASTRoot(currentAST, tmp230_AST);
 			match(DOT);
 			JavaNode tmp231_AST = null;
 			tmp231_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp231_AST);
 			match(LITERAL_class);
 			primaryExpression_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		default:
 		{
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		}
 		returnAST = primaryExpression_AST;
 	}
 	
 /** object instantiation.
  *  Trees are built as illustrated by the following input/tree pairs:
  *
  *  new T()
  *
  *  new
  *   |
  *   T --  ELIST
  *		   |
  *		  arg1 -- arg2 -- .. -- argn
  *
  *  new int[]
  *
  *  new
  *   |
  *  int -- ARRAY_DECLARATOR
  *
  *  new int[] {1,2}
  *
  *  new
  *   |
  *  int -- ARRAY_DECLARATOR -- ARRAY_INIT
  *								  |
  *								EXPR -- EXPR
  *								  |	  |
  *								  1	  2
  *
  *  new int[3]
  *  new
  *   |
  *  int -- ARRAY_DECLARATOR
  *				|
  *			  EXPR
  *				|
  *				3
  *
  *  new int[1][2]
  *
  *  new
  *   |
  *  int -- ARRAY_DECLARATOR
  *			   |
  *		 ARRAY_DECLARATOR -- EXPR
  *			   |			  |
  *			 EXPR			 1
  *			   |
  *			   2
  *
  */
 	public final void newExpression() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode newExpression_AST = null;
 		
 		JavaNode tmp232_AST = null;
 		tmp232_AST = (JavaNode)astFactory.create(LT(1));
 		astFactory.makeASTRoot(currentAST, tmp232_AST);
 		match(LITERAL_new);
 		{
 		switch ( LA(1)) {
 		case LT:
 		{
 			typeArguments();
 			astFactory.addASTChild(currentAST, returnAST);
 			break;
 		}
 		case IDENT:
 		case LITERAL_void:
 		case LITERAL_boolean:
 		case LITERAL_byte:
 		case LITERAL_char:
 		case LITERAL_short:
 		case LITERAL_int:
 		case LITERAL_float:
 		case LITERAL_long:
 		case LITERAL_double:
 		{
 			break;
 		}
 		default:
 		{
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		}
 		}
 		type();
 		astFactory.addASTChild(currentAST, returnAST);
 		{
 		switch ( LA(1)) {
 		case LPAREN:
 		{
 			JavaNode tmp233_AST = null;
 			tmp233_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp233_AST);
 			match(LPAREN);
 			argList();
 			astFactory.addASTChild(currentAST, returnAST);
 			JavaNode tmp234_AST = null;
 			tmp234_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp234_AST);
 			match(RPAREN);
 			{
 			if ((LA(1)==LCURLY) && (_tokenSet_49.member(LA(2)))) {
 				classBlock();
 				astFactory.addASTChild(currentAST, returnAST);
 			}
 			else if ((_tokenSet_47.member(LA(1))) && (_tokenSet_48.member(LA(2)))) {
 			}
 			else {
 				throw new NoViableAltException(LT(1), getFilename());
 			}
 			
 			}
 			break;
 		}
 		case LBRACK:
 		{
 			newArrayDeclarator();
 			astFactory.addASTChild(currentAST, returnAST);
 			{
 			if ((LA(1)==LCURLY) && (_tokenSet_50.member(LA(2)))) {
 				arrayInitializer();
 				astFactory.addASTChild(currentAST, returnAST);
 			}
 			else if ((_tokenSet_47.member(LA(1))) && (_tokenSet_48.member(LA(2)))) {
 			}
 			else {
 				throw new NoViableAltException(LT(1), getFilename());
 			}
 			
 			}
 			break;
 		}
 		default:
 		{
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		}
 		}
 		newExpression_AST = (JavaNode)currentAST.root;
 		returnAST = newExpression_AST;
 	}
 	
 /** Match a, a.b.c refs, a.b.c(...) refs, a.b.c[], a.b.c[].class,
  *  and a.b.c.class refs. Also this(...) and super(...). Match
  *  this or super.
  */
 	public final void identPrimary() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode identPrimary_AST = null;
 		JavaNode ta1_AST = null;
 		JavaNode ta2_AST = null;
 		Token  lp = null;
 		JavaNode lp_AST = null;
 		Token  lbc = null;
 		JavaNode lbc_AST = null;
 		
 		{
 		switch ( LA(1)) {
 		case LT:
 		{
 			typeArguments();
 			ta1_AST = (JavaNode)returnAST;
 			break;
 		}
 		case IDENT:
 		{
 			break;
 		}
 		default:
 		{
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		}
 		}
 		JavaNode tmp235_AST = null;
 		tmp235_AST = (JavaNode)astFactory.create(LT(1));
 		astFactory.addASTChild(currentAST, tmp235_AST);
 		match(IDENT);
 		{
 		_loop306:
 		do {
 			boolean synPredMatched304 = false;
 			if (((LA(1)==DOT) && (LA(2)==IDENT||LA(2)==LT))) {
 				int _m304 = mark();
 				synPredMatched304 = true;
 				inputState.guessing++;
 				try {
 					{
 					match(DOT);
 					{
 					switch ( LA(1)) {
 					case LT:
 					{
 						typeArguments();
 						break;
 					}
 					case IDENT:
 					{
 						break;
 					}
 					default:
 					{
 						throw new NoViableAltException(LT(1), getFilename());
 					}
 					}
 					}
 					match(IDENT);
 					}
 				}
 				catch (RecognitionException pe) {
 					synPredMatched304 = false;
 				}
 				rewind(_m304);
 				inputState.guessing--;
 			}
 			if ( synPredMatched304 ) {
 				JavaNode tmp236_AST = null;
 				tmp236_AST = (JavaNode)astFactory.create(LT(1));
 				astFactory.makeASTRoot(currentAST, tmp236_AST);
 				match(DOT);
 				{
 				switch ( LA(1)) {
 				case LT:
 				{
 					typeArguments();
 					ta2_AST = (JavaNode)returnAST;
 					break;
 				}
 				case IDENT:
 				{
 					break;
 				}
 				default:
 				{
 					throw new NoViableAltException(LT(1), getFilename());
 				}
 				}
 				}
 				JavaNode tmp237_AST = null;
 				tmp237_AST = (JavaNode)astFactory.create(LT(1));
 				astFactory.addASTChild(currentAST, tmp237_AST);
 				match(IDENT);
 			}
 			else if (((_tokenSet_51.member(LA(1))) && (_tokenSet_48.member(LA(2))))&&(false)) {
 			}
 			else {
 				break _loop306;
 			}
 			
 		} while (true);
 		}
 		{
 		if ((LA(1)==LPAREN)) {
 			{
 			lp = LT(1);
 			lp_AST = (JavaNode)astFactory.create(lp);
 			astFactory.makeASTRoot(currentAST, lp_AST);
 			match(LPAREN);
 			if ( inputState.guessing==0 ) {
 				lp_AST.setType(METHOD_CALL);
 			}
 			if ( inputState.guessing==0 ) {
 				if (ta2_AST != null) astFactory.addASTChild(currentAST, ta2_AST);
 			}
 			if ( inputState.guessing==0 ) {
 				if (ta2_AST == null) astFactory.addASTChild(currentAST, ta1_AST);
 			}
 			argList();
 			astFactory.addASTChild(currentAST, returnAST);
 			JavaNode tmp238_AST = null;
 			tmp238_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp238_AST);
 			match(RPAREN);
 			}
 		}
 		else if ((LA(1)==LBRACK) && (LA(2)==RBRACK)) {
 			{
 			int _cnt310=0;
 			_loop310:
 			do {
 				if ((LA(1)==LBRACK) && (LA(2)==RBRACK)) {
 					lbc = LT(1);
 					lbc_AST = (JavaNode)astFactory.create(lbc);
 					astFactory.makeASTRoot(currentAST, lbc_AST);
 					match(LBRACK);
 					if ( inputState.guessing==0 ) {
 						lbc_AST.setType(ARRAY_DECLARATOR);
 					}
 					JavaNode tmp239_AST = null;
 					tmp239_AST = (JavaNode)astFactory.create(LT(1));
 					astFactory.addASTChild(currentAST, tmp239_AST);
 					match(RBRACK);
 				}
 				else {
 					if ( _cnt310>=1 ) { break _loop310; } else {throw new NoViableAltException(LT(1), getFilename());}
 				}
 				
 				_cnt310++;
 			} while (true);
 			}
 		}
 		else if ((_tokenSet_47.member(LA(1))) && (_tokenSet_48.member(LA(2)))) {
 		}
 		else {
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		
 		}
 		identPrimary_AST = (JavaNode)currentAST.root;
 		returnAST = identPrimary_AST;
 	}
 	
 	public final void constant() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode constant_AST = null;
 		
 		switch ( LA(1)) {
 		case NUM_INT:
 		{
 			JavaNode tmp240_AST = null;
 			tmp240_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp240_AST);
 			match(NUM_INT);
 			constant_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case CHAR_LITERAL:
 		{
 			JavaNode tmp241_AST = null;
 			tmp241_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp241_AST);
 			match(CHAR_LITERAL);
 			constant_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case STRING_LITERAL:
 		{
 			JavaNode tmp242_AST = null;
 			tmp242_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp242_AST);
 			match(STRING_LITERAL);
 			constant_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case NUM_FLOAT:
 		{
 			JavaNode tmp243_AST = null;
 			tmp243_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp243_AST);
 			match(NUM_FLOAT);
 			constant_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case NUM_LONG:
 		{
 			JavaNode tmp244_AST = null;
 			tmp244_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp244_AST);
 			match(NUM_LONG);
 			constant_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		case NUM_DOUBLE:
 		{
 			JavaNode tmp245_AST = null;
 			tmp245_AST = (JavaNode)astFactory.create(LT(1));
 			astFactory.addASTChild(currentAST, tmp245_AST);
 			match(NUM_DOUBLE);
 			constant_AST = (JavaNode)currentAST.root;
 			break;
 		}
 		default:
 		{
 			throw new NoViableAltException(LT(1), getFilename());
 		}
 		}
 		returnAST = constant_AST;
 	}
 	
 	public final void newArrayDeclarator() throws RecognitionException, TokenStreamException {
 		
 		returnAST = null;
 		ASTPair currentAST = new ASTPair();
 		JavaNode newArrayDeclarator_AST = null;
 		Token  lb = null;
 		JavaNode lb_AST = null;
 		
 		{
 		int _cnt321=0;
 		_loop321:
 		do {
 			if ((LA(1)==LBRACK) && (_tokenSet_52.member(LA(2)))) {
 				lb = LT(1);
 				lb_AST = (JavaNode)astFactory.create(lb);
 				astFactory.makeASTRoot(currentAST, lb_AST);
 				match(LBRACK);
 				if ( inputState.guessing==0 ) {
 					lb_AST.setType(ARRAY_DECLARATOR);
 				}
 				{
 				switch ( LA(1)) {
 				case IDENT:
 				case LITERAL_super:
 				case LT:
 				case LITERAL_void:
 				case LITERAL_boolean:
 				case LITERAL_byte:
 				case LITERAL_char:
 				case LITERAL_short:
 				case LITERAL_int:
 				case LITERAL_float:
 				case LITERAL_long:
 				case LITERAL_double:
 				case LPAREN:
 				case LITERAL_this:
 				case PLUS:
 				case MINUS:
 				case INC:
 				case DEC:
 				case BNOT:
 				case LNOT:
 				case LITERAL_true:
 				case LITERAL_false:
 				case LITERAL_null:
 				case LITERAL_new:
 				case NUM_INT:
 				case CHAR_LITERAL:
 				case STRING_LITERAL:
 				case NUM_FLOAT:
 				case NUM_LONG:
 				case NUM_DOUBLE:
 				{
 					expression();
 					astFactory.addASTChild(currentAST, returnAST);
 					break;
 				}
 				case RBRACK:
 				{
 					break;
 				}
 				default:
 				{
 					throw new NoViableAltException(LT(1), getFilename());
 				}
 				}
 				}
 				match(RBRACK);
 			}
 			else {
 				if ( _cnt321>=1 ) { break _loop321; } else {throw new NoViableAltException(LT(1), getFilename());}
 			}
 			
 			_cnt321++;
 		} while (true);
 		}
 		newArrayDeclarator_AST = (JavaNode)currentAST.root;
 		returnAST = newArrayDeclarator_AST;
 	}
 	
 	
 	public static final String[] _tokenNames = {
 		"<0>",
 		"EOF",
 		"<2>",
 		"NULL_TREE_LOOKAHEAD",
 		"<4>",
 		"<5>",
 		"JAVADOC_COMMENT",
 		"LCURLY",
 		"RCURLY",
 		"BLOCK",
 		"MODIFIERS",
 		"OBJBLOCK",
 		"SLIST",
 		"CTOR_DEF",
 		"METHOD_DEF",
 		"VARIABLE_DEF",
 		"INSTANCE_INIT",
 		"STATIC_INIT",
 		"TYPE",
 		"CLASS_DEF",
 		"INTERFACE_DEF",
 		"PACKAGE_DEF",
 		"ARRAY_DECLARATOR",
 		"EXTENDS_CLAUSE",
 		"IMPLEMENTS_CLAUSE",
 		"PARAMETERS",
 		"PARAMETER_DEF",
 		"LABELED_STAT",
 		"TYPECAST",
 		"INDEX_OP",
 		"POST_INC",
 		"POST_DEC",
 		"METHOD_CALL",
 		"EXPR",
 		"ARRAY_INIT",
 		"IMPORT",
 		"UNARY_MINUS",
 		"UNARY_PLUS",
 		"CASE_GROUP",
 		"ELIST",
 		"FOR_INIT",
 		"FOR_CONDITION",
 		"FOR_ITERATOR",
 		"EMPTY_STAT",
 		"\"final\"",
 		"\"abstract\"",
 		"\"strictfp\"",
 		"SUPER_CTOR_CALL",
 		"CTOR_CALL",
 		"VARIABLE_PARAMETER_DEF",
 		"STATIC_IMPORT",
 		"ENUM_DEF",
 		"ENUM_CONSTANT_DEF",
 		"FOR_EACH_CLAUSE",
 		"ANNOTATION_DEF",
 		"ANNOTATIONS",
 		"ANNOTATION",
 		"ANNOTATION_MEMBER_VALUE_PAIR",
 		"ANNOTATION_FIELD_DEF",
 		"ANNOTATION_ARRAY_INIT",
 		"TYPE_ARGUMENTS",
 		"TYPE_ARGUMENT",
 		"TYPE_PARAMETERS",
 		"TYPE_PARAMETER",
 		"WILDCARD_TYPE",
 		"TYPE_UPPER_BOUNDS",
 		"TYPE_LOWER_BOUNDS",
 		"ROOT",
 		"CASESLIST",
 		"SEPARATOR_COMMENT",
 		"BOF",
 		"SYNBLOCK",
 		"SPECIAL_COMMENT",
 		"\"package\"",
 		"SEMI",
 		"\"import\"",
 		"\"static\"",
 		"LBRACK",
 		"RBRACK",
 		"IDENT",
 		"DOT",
 		"QUESTION",
 		"\"extends\"",
 		"\"super\"",
 		"LT",
 		"COMMA",
 		"GT",
 		"SR",
 		"BSR",
 		"\"void\"",
 		"\"boolean\"",
 		"\"byte\"",
 		"\"char\"",
 		"\"short\"",
 		"\"int\"",
 		"\"float\"",
 		"\"long\"",
 		"\"double\"",
 		"STAR",
 		"\"private\"",
 		"\"public\"",
 		"\"protected\"",
 		"\"transient\"",
 		"\"native\"",
 		"\"threadsafe\"",
 		"\"synchronized\"",
 		"\"volatile\"",
 		"AT",
 		"LPAREN",
 		"RPAREN",
 		"ASSIGN",
 		"\"class\"",
 		"\"interface\"",
 		"\"enum\"",
 		"BAND",
 		"\"default\"",
 		"\"implements\"",
 		"\"this\"",
 		"\"throws\"",
 		"TRIPLE_DOT",
 		"COLON",
 		"\"if\"",
 		"\"else\"",
 		"\"while\"",
 		"\"do\"",
 		"\"break\"",
 		"\"continue\"",
 		"\"return\"",
 		"\"switch\"",
 		"\"throw\"",
 		"\"assert\"",
 		"\"for\"",
 		"\"case\"",
 		"\"try\"",
 		"\"finally\"",
 		"\"catch\"",
 		"PLUS_ASSIGN",
 		"MINUS_ASSIGN",
 		"STAR_ASSIGN",
 		"DIV_ASSIGN",
 		"MOD_ASSIGN",
 		"SR_ASSIGN",
 		"BSR_ASSIGN",
 		"SL_ASSIGN",
 		"BAND_ASSIGN",
 		"BXOR_ASSIGN",
 		"BOR_ASSIGN",
 		"LOR",
 		"LAND",
 		"BOR",
 		"BXOR",
 		"NOT_EQUAL",
 		"EQUAL",
 		"LE",
 		"GE",
 		"\"instanceof\"",
 		"SL",
 		"PLUS",
 		"MINUS",
 		"DIV",
 		"MOD",
 		"INC",
 		"DEC",
 		"BNOT",
 		"LNOT",
 		"\"true\"",
 		"\"false\"",
 		"\"null\"",
 		"\"new\"",
 		"NUM_INT",
 		"CHAR_LITERAL",
 		"STRING_LITERAL",
 		"NUM_FLOAT",
 		"NUM_LONG",
 		"NUM_DOUBLE",
 		"WS",
 		"SL_COMMENT",
 		"COMMENT",
 		"ML_COMMENT",
 		"ESC",
 		"HEX_DIGIT",
 		"VOCAB",
 		"EXPONENT",
 		"FLOAT_SUFFIX"
 	};
 	
 	protected void buildTokenTypeASTClassMap() {
 		tokenTypeToASTClassMap=null;
 	};
 	
 	private static final long[] mk_tokenSet_0() {
 		long[] data = { 123145302310914L, 1002720244800512L, 0L, 0L};
 		return data;
 	}
 	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
 	private static final long[] mk_tokenSet_1() {
 		long[] data = { 123145302310914L, 1002720244831232L, 0L, 0L};
 		return data;
 	}
 	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
 	private static final long[] mk_tokenSet_2() {
 		long[] data = { 123145302310912L, 1002720244798464L, 0L, 0L};
 		return data;
 	}
 	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
 	private static final long[] mk_tokenSet_3() {
 		long[] data = { 123145302310914L, 1002720244798464L, 0L, 0L};
 		return data;
 	}
 	public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());
 	private static final long[] mk_tokenSet_4() {
 		long[] data = { 123145302310912L, 8761733287936L, 0L, 0L};
 		return data;
 	}
 	public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());
 	private static final long[] mk_tokenSet_5() {
 		long[] data = { 0L, 17146478592L, 0L, 0L};
 		return data;
 	}
 	public static final BitSet _tokenSet_5 = new BitSet(mk_tokenSet_5());
 	private static final long[] mk_tokenSet_6() {
 		long[] data = { 123145302311296L, 123848972572816384L, 33554176L, 0L, 0L, 0L};
 		return data;
 	}
 	public static final BitSet _tokenSet_6 = new BitSet(mk_tokenSet_6());
 	private static final long[] mk_tokenSet_7() {
 		long[] data = { 123145302311298L, -18014398509484544L, 140737488355135L, 0L, 0L, 0L};
 		return data;
 	}
 	public static final BitSet _tokenSet_7 = new BitSet(mk_tokenSet_7());
 	private static final long[] mk_tokenSet_8() {
 		long[] data = { 128L, 9033604681728000L, 140730509033472L, 0L, 0L, 0L};
 		return data;
 	}
 	public static final BitSet _tokenSet_8 = new BitSet(mk_tokenSet_8());
 	private static final long[] mk_tokenSet_9() {
 		long[] data = { 256L, 10194706170093568L, 140737487831040L, 0L, 0L, 0L};
 		return data;
 	}
 	public static final BitSet _tokenSet_9 = new BitSet(mk_tokenSet_9());
 	private static final long[] mk_tokenSet_10() {
 		long[] data = { 0L, 9033604681728000L, 140730509033472L, 0L, 0L, 0L};
 		return data;
 	}
 	public static final BitSet _tokenSet_10 = new BitSet(mk_tokenSet_10());
 	private static final long[] mk_tokenSet_11() {
 		long[] data = { 128L, 4503616805437440L, 0L, 0L};
 		return data;
 	}
 	public static final BitSet _tokenSet_11 = new BitSet(mk_tokenSet_11());
 	private static final long[] mk_tokenSet_12() {
 		long[] data = { 123145302311296L, 5523929205945344L, 0L, 0L};
 		return data;
 	}
 	public static final BitSet _tokenSet_12 = new BitSet(mk_tokenSet_12());
 	private static final long[] mk_tokenSet_13() {
 		long[] data = { 123145302310912L, 1002737392193536L, 0L, 0L};
 		return data;
 	}
 	public static final BitSet _tokenSet_13 = new BitSet(mk_tokenSet_13());
 	private static final long[] mk_tokenSet_14() {
 		long[] data = { 123145302310912L, 1020329578311680L, 0L, 0L};
 		return data;
 	}
 	public static final BitSet _tokenSet_14 = new BitSet(mk_tokenSet_14());
 	private static final long[] mk_tokenSet_15() {
 		long[] data = { 0L, 17146347520L, 0L, 0L};
 		return data;
 	}
 	public static final BitSet _tokenSet_15 = new BitSet(mk_tokenSet_15());
 	private static final long[] mk_tokenSet_16() {
 		long[] data = { 0L, 1155072L, 0L, 0L};
 		return data;
 	}
 	public static final BitSet _tokenSet_16 = new BitSet(mk_tokenSet_16());
 	private static final long[] mk_tokenSet_17() {
 		long[] data = { 0L, 70368746284032L, 0L, 0L};
 		return data;
 	}
 	public static final BitSet _tokenSet_17 = new BitSet(mk_tokenSet_17());
 	private static final long[] mk_tokenSet_18() {
 		long[] data = { 123145302311168L, 1073106137428992L, 0L, 0L};
 		return data;
 	}
 	public static final BitSet _tokenSet_18 = new BitSet(mk_tokenSet_18());
 	private static final long[] mk_tokenSet_19() {
 		long[] data = { 123145302311296L, 1073106138477568L, 0L, 0L};
 		return data;
 	}
 	public static final BitSet _tokenSet_19 = new BitSet(mk_tokenSet_19());
 	private static final long[] mk_tokenSet_20() {
 		long[] data = { 17592186044416L, 8813239369728L, 0L, 0L};
 		return data;
 	}
 	public static final BitSet _tokenSet_20 = new BitSet(mk_tokenSet_20());
 	private static final long[] mk_tokenSet_21() {
 		long[] data = { 0L, 8813240492032L, 0L, 0L};
 		return data;
 	}
 	public static final BitSet _tokenSet_21 = new BitSet(mk_tokenSet_21());
 	private static final long[] mk_tokenSet_22() {
 		long[] data = { 0L, 36037610259456000L, 0L, 0L};
 		return data;
 	}
 	public static final BitSet _tokenSet_22 = new BitSet(mk_tokenSet_22());
 	private static final long[] mk_tokenSet_23() {
 		long[] data = { 123145302311040L, -423162460324195328L, 140730509033519L, 0L, 0L, 0L};
 		return data;
 	}
 	public static final BitSet _tokenSet_23 = new BitSet(mk_tokenSet_23());
 	private static final long[] mk_tokenSet_24() {
 		long[] data = { 0L, 9007199256313856L, 0L, 0L};
 		return data;
 	}
 	public static final BitSet _tokenSet_24 = new BitSet(mk_tokenSet_24());
 	private static final long[] mk_tokenSet_25() {
 		long[] data = { 0L, 17609332523008L, 0L, 0L};
 		return data;
 	}
 	public static final BitSet _tokenSet_25 = new BitSet(mk_tokenSet_25());
 	private static final long[] mk_tokenSet_26() {
 		long[] data = { 123145302311296L, -423162460324195328L, 140730509033519L, 0L, 0L, 0L};
 		return data;
 	}
 	public static final BitSet _tokenSet_26 = new BitSet(mk_tokenSet_26());
 	private static final long[] mk_tokenSet_27() {
 		long[] data = { 123145302311296L, -349064155495681024L, 140737488355119L, 0L, 0L, 0L};
 		return data;
 	}
 	public static final BitSet _tokenSet_27 = new BitSet(mk_tokenSet_27());
 	private static final long[] mk_tokenSet_28() {
 		long[] data = { 123145302311296L, -132680284358798336L, 140730509033535L, 0L, 0L, 0L};
 		return data;
 	}
 	public static final BitSet _tokenSet_28 = new BitSet(mk_tokenSet_28());
 	private static final long[] mk_tokenSet_29() {
 		long[] data = { 123145302311296L, -58581979530284032L, 140737488355327L, 0L, 0L, 0L};
 		return data;
 	}
 	public static final BitSet _tokenSet_29 = new BitSet(mk_tokenSet_29());
 	private static final long[] mk_tokenSet_30() {
 		long[] data = { 123145302310912L, 17574972657664L, 0L, 0L};
 		return data;
 	}
 	public static final BitSet _tokenSet_30 = new BitSet(mk_tokenSet_30());
 	private static final long[] mk_tokenSet_31() {
 		long[] data = { 123145302310912L, 17574973779968L, 0L, 0L};
 		return data;
 	}
 	public static final BitSet _tokenSet_31 = new BitSet(mk_tokenSet_31());
 	private static final long[] mk_tokenSet_32() {
 		long[] data = { 0L, 9024808588705792L, 140730509033472L, 0L, 0L, 0L};
 		return data;
 	}
 	public static final BitSet _tokenSet_32 = new BitSet(mk_tokenSet_32());
 	private static final long[] mk_tokenSet_33() {
 		long[] data = { 0L, 10221094449161216L, 140737488355072L, 0L, 0L, 0L};
 		return data;
 	}
 	public static final BitSet _tokenSet_33 = new BitSet(mk_tokenSet_33());
 	private static final long[] mk_tokenSet_34() {
 		long[] data = { 123145302310912L, 158295314665472L, 0L, 0L};
 		return data;
 	}
 	public static final BitSet _tokenSet_34 = new BitSet(mk_tokenSet_34());
 	private static final long[] mk_tokenSet_35() {
 		long[] data = { 123145302310912L, 158295314698240L, 0L, 0L};
 		return data;
 	}
 	public static final BitSet _tokenSet_35 = new BitSet(mk_tokenSet_35());
 	private static final long[] mk_tokenSet_36() {
 		long[] data = { 128L, 9024808588705792L, 140730509033472L, 0L, 0L, 0L};
 		return data;
 	}
 	public static final BitSet _tokenSet_36 = new BitSet(mk_tokenSet_36());
 	private static final long[] mk_tokenSet_37() {
 		long[] data = { 123145302310912L, 9042366415016960L, 140730509033472L, 0L, 0L, 0L};
 		return data;
 	}
 	public static final BitSet _tokenSet_37 = new BitSet(mk_tokenSet_37());
 	private static final long[] mk_tokenSet_38() {
 		long[] data = { 123145302310912L, 10238652277568512L, 140737488355072L, 0L, 0L, 0L};
 		return data;
 	}
 	public static final BitSet _tokenSet_38 = new BitSet(mk_tokenSet_38());
 	private static final long[] mk_tokenSet_39() {
 		long[] data = { 0L, 81082402626633728L, 140730509033472L, 0L, 0L, 0L};
 		return data;
 	}
 	public static final BitSet _tokenSet_39 = new BitSet(mk_tokenSet_39());
 	private static final long[] mk_tokenSet_40() {
 		long[] data = { 0L, 10221094451258368L, 140737488355072L, 0L, 0L, 0L};
 		return data;
 	}
 	public static final BitSet _tokenSet_40 = new BitSet(mk_tokenSet_40());
 	private static final long[] mk_tokenSet_41() {
 		long[] data = { 0L, 5242880L, 100663296L, 0L, 0L, 0L};
 		return data;
 	}
 	public static final BitSet _tokenSet_41 = new BitSet(mk_tokenSet_41());
 	private static final long[] mk_tokenSet_42() {
 		long[] data = { 0L, 25165824L, 268435456L, 0L, 0L, 0L};
 		return data;
 	}
 	public static final BitSet _tokenSet_42 = new BitSet(mk_tokenSet_42());
 	private static final long[] mk_tokenSet_43() {
 		long[] data = { 0L, 17179869184L, 6442450944L, 0L, 0L, 0L};
 		return data;
 	}
 	public static final BitSet _tokenSet_43 = new BitSet(mk_tokenSet_43());
 	private static final long[] mk_tokenSet_44() {
 		long[] data = { 0L, 9024808588705792L, 140600049401856L, 0L, 0L, 0L};
 		return data;
 	}
 	public static final BitSet _tokenSet_44 = new BitSet(mk_tokenSet_44());
 	private static final long[] mk_tokenSet_45() {
 		long[] data = { 123145302311296L, 83316593106088960L, 140737488355072L, 0L, 0L, 0L};
 		return data;
 	}
 	public static final BitSet _tokenSet_45 = new BitSet(mk_tokenSet_45());
 	private static final long[] mk_tokenSet_46() {
 		long[] data = { 0L, 1605632L, 0L, 0L};
 		return data;
 	}
 	public static final BitSet _tokenSet_46 = new BitSet(mk_tokenSet_46());
 	private static final long[] mk_tokenSet_47() {
 		long[] data = { 123145302311296L, 74291801664779264L, 34359738112L, 0L, 0L, 0L};
 		return data;
 	}
 	public static final BitSet _tokenSet_47 = new BitSet(mk_tokenSet_47());
 	private static final long[] mk_tokenSet_48() {
 		long[] data = { 123145302311298L, -58546795156081152L, 140737488355135L, 0L, 0L, 0L};
 		return data;
 	}
 	public static final BitSet _tokenSet_48 = new BitSet(mk_tokenSet_48());
 	private static final long[] mk_tokenSet_49() {
 		long[] data = { 123145302311296L, 1002737392194560L, 0L, 0L};
 		return data;
 	}
 	public static final BitSet _tokenSet_49 = new BitSet(mk_tokenSet_49());
 	private static final long[] mk_tokenSet_50() {
 		long[] data = { 384L, 9024808588705792L, 140730509033472L, 0L, 0L, 0L};
 		return data;
 	}
 	public static final BitSet _tokenSet_50 = new BitSet(mk_tokenSet_50());
 	private static final long[] mk_tokenSet_51() {
 		long[] data = { 123145302311296L, 74309393850823680L, 34359738112L, 0L, 0L, 0L};
 		return data;
 	}
 	public static final BitSet _tokenSet_51 = new BitSet(mk_tokenSet_51());
 	private static final long[] mk_tokenSet_52() {
 		long[] data = { 0L, 9024808588722176L, 140730509033472L, 0L, 0L, 0L};
 		return data;
 	}
 	public static final BitSet _tokenSet_52 = new BitSet(mk_tokenSet_52());
 	
 	}
