 ////////////////////////////////////////////////////////////////////////////////
 //
 //  ADOBE SYSTEMS INCORPORATED
 //  Copyright 2004-2007 Adobe Systems Incorporated
 //  All Rights Reserved.
 //
 //  NOTICE: Adobe permits you to use, modify, and distribute this file
 //  in accordance with the terms of the license agreement accompanying it.
 //
 ////////////////////////////////////////////////////////////////////////////////
 
 /*
  * Written by Jeff Dyer
  * Copyright (c) 1998-2003 Mountain View Compiler Company
  * All rights reserved.
  */
 
 package macromedia.asc.parser;
 
 import java.io.*;
 import java.util.HashSet;
 import java.util.ListIterator;
 
 import macromedia.asc.semantics.NamespaceValue;
 import macromedia.asc.semantics.ReferenceValue;
 import macromedia.asc.util.*;
 import static macromedia.asc.parser.Tokens.*;
 import static macromedia.asc.embedding.avmplus.RuntimeConstants.*;
 import static macromedia.asc.embedding.avmplus.Features.*;
 import static macromedia.asc.embedding.ErrorConstants.*;
 import macromedia.asc.embedding.CompilerHandler;
 import static macromedia.asc.parser.States.*;
 
 /**
  * Parse ECMAScript programs.
  *
  * @author Jeff Dyer
  */
 public final class Parser
 {
     private static final boolean debug = false;
 
     private static final String PUBLIC = "public".intern();
     private static final String PRIVATE = "private".intern();
     private static final String PROTECTED = "protected".intern();
     private static final String ASTERISK = "*".intern();
     private static final String DEFAULT = "default".intern();
     private static final String AS3 = "AS3".intern();
     public static final String CONFIG = "CONFIG".intern();
     private static final String GET = "get".intern();
     private static final String NAMESPACE = "namespace".intern();
     private static final String SET = "set".intern();
     private static final String VEC = "vec".intern();
     private static final String VECTOR = "Vector".intern();
     private static final String __AS3__ = "__AS3__".intern();
 
     private static final int abbrevIfElse_mode       = ELSE_TOKEN;  // lookahead uses this value. don't change.
     private static final int abbrevDoWhile_mode      = WHILE_TOKEN; // ditto.
     private static final int abbrevFunction_mode     = FUNCTION_TOKEN;
     private static final int abbrev_mode             = LAST_TOKEN - 1;
     private static final int full_mode               = abbrev_mode - 1;
 
     private static final int allowIn_mode            = full_mode - 1;
     private static final int noIn_mode               = allowIn_mode - 1;
     private static final int catch_parameter_error   = 1;
     private static final int syntax_xml_error        = 2;
 
     private static final int syntax_error            = 7;
     private static final int expression_syntax_error = 8;
     private static final int syntax_eos_error        = 9;
 
     private static final int xmlid_tokens[] = 
               { // include all keyword tokens and the identifier token
                 // I moved IDENTIFIER to the top because it's the most common
                   IDENTIFIER_TOKEN,
                   ABSTRACT_TOKEN,
                   AS_TOKEN,
                   BREAK_TOKEN,
                   CASE_TOKEN,
                   CATCH_TOKEN,
                   CLASS_TOKEN,
                   CONST_TOKEN,
                   CONTINUE_TOKEN,
                   DEBUGGER_TOKEN,
                   DEFAULT_TOKEN,
                   DELETE_TOKEN,
                   DO_TOKEN,
                   ELSE_TOKEN,
                   ENUM_TOKEN,
                   EXTENDS_TOKEN,
                   FALSE_TOKEN,
                   FINAL_TOKEN,
                   FINALLY_TOKEN,
                   FOR_TOKEN,
                   FUNCTION_TOKEN,
                   GET_TOKEN,
                   GOTO_TOKEN,
                   IF_TOKEN,
                   IMPLEMENTS_TOKEN,
                   IMPORT_TOKEN,
                   IN_TOKEN,
                   INCLUDE_TOKEN,
                   INSTANCEOF_TOKEN,
                   INTERFACE_TOKEN,
                   IS_TOKEN,
                   NAMESPACE_TOKEN,
                   CONFIG_TOKEN,
                   NATIVE_TOKEN,
                   NEW_TOKEN,
                   NULL_TOKEN,
                   PACKAGE_TOKEN,
                   PRIVATE_TOKEN,
                   PROTECTED_TOKEN,
                   PUBLIC_TOKEN,
                   RETURN_TOKEN,
                   SET_TOKEN,
                   STATIC_TOKEN,
                   SUPER_TOKEN,
                   SWITCH_TOKEN,
                   SYNCHRONIZED_TOKEN,
                   THIS_TOKEN,
                   THROW_TOKEN,
                   THROWS_TOKEN,
                   TRANSIENT_TOKEN,
                   TRUE_TOKEN,
                   TRY_TOKEN,
                   TYPEOF_TOKEN,
                   USE_TOKEN,
                   VAR_TOKEN,
                   VOID_TOKEN,
                   VOLATILE_TOKEN,
                   WHILE_TOKEN,
                   WITH_TOKEN,
               };
     static final int xmlid_tokens_count = xmlid_tokens.length;
 
     private int lastToken;
     private int nextToken;
     private int nextTokenClass;
     private Context ctx;
     private NodeFactory nodeFactory;
     private boolean create_doc_info;
     private boolean save_comment_nodes;
     public Scanner scanner;
 
     private String encoding;
 
     // keeps track of the type of the currently evaluating member expressions base
     public ObjectList<Node>    comments = new ObjectList<Node>(); // all comments encountered while parsing are placed here, rather than in the parse tree
     public IntList block_kind_stack = new IntList();
     public String current_class_name = null;
     
     private boolean within_package;
 
     private boolean parsing_include = false;
 
     public ObjectList< HashSet<String> > config_namespaces = new ObjectList< HashSet<String> >();
 
     /*
      * Log a syntax error and recover
      */
 
     Node error(int errCode)                          { return error(syntax_error, errCode,"","",-1); }
     private Node error(int kind, int errCode)                { return error(kind,errCode,"","",-1); }
     private Node error(int kind, int errCode, String arg1) { return error(kind,errCode,arg1,"",-1); }
     private Node error(int kind, int errCode, String arg1, String arg2) { return error(kind,errCode,arg1,arg2,-1); }
     private Node error(int kind, int errCode, String arg1, String arg2,int pos)
     {
         String origin = this.scanner.input.origin;
         StringBuilder out = new StringBuilder();
         
         if(debug) out.append("[Parser] ");
         
         // Just the arguments for sanities, no message (since they chang often)
         if(ContextStatics.useSanityStyleErrors)
         {
             out.append("code=" + errCode + "; arg1=" + arg1 + "; arg2=" + arg2);
         }
         else
         {
             String msg = ctx.shellErrorString(errCode);
             int nextLoc = Context.replaceStringArg(out, msg, 0, arg1);
             nextLoc = Context.replaceStringArg(out, msg, nextLoc, arg2);
             if (nextLoc != -1) // append msg remainder after replacement point, if any
                 out.append(msg.substring(nextLoc, msg.length()));
         }
 
         int lineno;
         int column;
         if( pos < 0 )
         {
             pos = scanner.input.positionOfMark();
         }
         lineno = scanner.input.getLnNum(pos);
         column = scanner.input.getColPos(pos);
 
         if (kind == syntax_error || kind == syntax_eos_error )
         {
             ctx.localizedError(origin, lineno, column, out.toString(), scanner.input.getLineText(pos), errCode);
         }
         else
         {
             ctx.localizedError(origin, lineno, column, out.toString(), "", errCode);
         }
         return null;
     }
 
     /*
      * skip ahead after an error is detected. this simply goes until the next
      * whitespace or end of input.
      */
 
     private void skiperror(int kind)
     {
         // If kind is < 0 then it is a token class to be advanced to,
         // Otherwise it is a error category.
 
         if (kind < 0)
         {
             while (true)
             {
                 // nextToken is the same variable used by lookahead
 
                 if (nextToken == kind)
                 {
                     if( kind == RIGHTBRACE_TOKEN )
                     {
                         int size = block_kind_stack.size();
                         block_kind_stack.clear();
                         for( ; size > 0; --size)
                         {
                            block_kind_stack.add(ERROR_TOKEN);    // ignore attribute errors since blocks are probably messed up
                         }
                     }
                     break;
                 }
                 else
                 if( nextToken == EOS_TOKEN )
                 {
                     break;
                 }
                 else
                 if( nextToken == SEMICOLON_TOKEN && kind != RIGHTBRACE_TOKEN ) // don't stop eating until right brace is found
                 {
                     break;
                 }
                 else
                 if( nextToken == LEFTBRACE_TOKEN && kind != RIGHTBRACE_TOKEN ) // don't stop eating until right brace is found
                 {
                     scanner.retract();
                     break;
                 }
                 else
                 if( nextToken == RIGHTBRACE_TOKEN )
                 {
                     scanner.retract();
                     break;
                 }
                 getNextToken();
             }
         }
         else
         {
             switch (kind)
             {
                 case catch_parameter_error:
                     while (true)
                     {
                         getNextToken();
                         if (nextToken == RIGHTPAREN_TOKEN || nextToken == EOS_TOKEN)
                         {
                             break;
                         }
                     }
                     break;
                 case syntax_xml_error:
                 case syntax_eos_error:
                     while (true)
                     {
                         getNextToken();
                         if (nextToken == EOS_TOKEN)
                         {
                             break;
                         }
                     }
                     break;
                 case syntax_error:
                 case expression_syntax_error:
                 default:
                     do
                     {
                         if (nextToken == LEFTPAREN_TOKEN || nextToken == RIGHTPAREN_TOKEN ||
                             nextToken == LEFTBRACE_TOKEN || nextToken == RIGHTBRACE_TOKEN ||
                             nextToken == LEFTBRACKET_TOKEN || nextToken == RIGHTBRACKET_TOKEN ||
                             nextToken == COMMA_TOKEN || nextToken == SEMICOLON_TOKEN ||
                             nextToken == EOS_TOKEN)
                         {
                             break;
                         }
                         getNextToken();
                     }
                     while (true);
                     break;
             }
         }
         lastToken = ERROR_TOKEN;
     }
 
 	private void init(Context cx, String origin, boolean emit_doc_info, boolean save_comment_nodes, IntList block_kind_stack)
 	{
 		ctx = cx;
 		lastToken = EMPTY_TOKEN;
 		nextToken = EMPTY_TOKEN;
 		create_doc_info = emit_doc_info;
         within_package = false;
 		this.save_comment_nodes = save_comment_nodes;
 		nodeFactory = cx.getNodeFactory();
 		nodeFactory.createDefaultDocComments(emit_doc_info); // for now, always create defualt comments for uncommented nodes in -doc
 		if( block_kind_stack != null )
 		{
 		    this.block_kind_stack.addAll(block_kind_stack);
 		}
 		else
 		{
 		    this.block_kind_stack.add(EMPTY_TOKEN);  // set initial state
 		}
 		cx.parser = this;
 		cx.setOrigin(origin);
 	}
 
     public Parser(Context cx, InputStream in, String origin)
     {
         this(cx, in, origin, null);
     }
 
     public Parser(Context cx, InputStream in, String origin, String encoding)
     {
         init(cx, origin, false, false, null);
         scanner = new Scanner(cx, in, encoding, origin);
         this.encoding = encoding;
     }
 
     public Parser(Context cx, InputStream in, String origin, boolean emit_doc_info, boolean save_comment_nodes)
     {
         this(cx, in, origin, null, emit_doc_info, save_comment_nodes,null);
     }
 
     public Parser(Context cx, InputStream in, String origin, String encoding, boolean emit_doc_info, boolean save_comment_nodes)
     {
         this(cx, in, origin, encoding, emit_doc_info, save_comment_nodes,null);
     }
 
     public Parser(Context cx, InputStream in, String origin, String encoding, boolean emit_doc_info, boolean save_comment_nodes, IntList block_kind_stack)
     {
 	    init(cx, origin, emit_doc_info, save_comment_nodes, block_kind_stack);
         scanner = new Scanner(cx, in, encoding, origin, save_comment_nodes|emit_doc_info);
         this.encoding = encoding;
     }
 
     public Parser(Context cx, InputStream in, String origin, String encoding, boolean emit_doc_info, boolean save_comment_nodes, IntList block_kind_stack, boolean is_include)
     {
         this(cx, in, origin,encoding, emit_doc_info, save_comment_nodes, block_kind_stack);
         this.parsing_include = is_include;
     }
 
 	public Parser(Context cx, String in, String origin)
 	{
 		init(cx, origin, false, false, null);
 	    scanner = new Scanner(cx, in, origin, false);
 	}
 
 	public Parser(Context cx, String in, String origin, boolean emit_doc_info, boolean save_comment_nodes)
 	{
 	    this(cx, in, origin, emit_doc_info, save_comment_nodes,null);
 	}
 
 	public Parser(Context cx, String in, String origin, boolean emit_doc_info, boolean save_comment_nodes, IntList block_kind_stack)
 	{
 		init(cx, origin, emit_doc_info, save_comment_nodes, block_kind_stack);
 	    scanner = new Scanner(cx, in, origin, save_comment_nodes|emit_doc_info);
 	}
 
     public Parser(Context cx, String in, String origin, boolean emit_doc_info, boolean save_comment_nodes, IntList block_kind_stack, boolean is_include)
     {
         this(cx, in, origin, emit_doc_info, save_comment_nodes, block_kind_stack);
         this.parsing_include = is_include;
     }
 
     /**
      * This contructor is used by Flex direct AST generation.  It
      * allows Flex to pass in a specialized InputBuffer.
      */
     public Parser(Context cx, InputBuffer inputBuffer, String origin, boolean emit_doc_info)
     {
         init(cx, origin, emit_doc_info, false, null);
         scanner = new Scanner(cx, inputBuffer);
     }
 
     /*
      * shift: -- like match, but we already know the token
      */
     
     private final void shift()
     {
     	lastToken = nextToken;
     	nextToken = EMPTY_TOKEN;
     }
 
     /*
      * Match the current input with an expected token. lookahead is managed by
      * setting the state of this.nexttoken to EMPTY_TOKEN after an match is
      * attempted. the next lookahead will initialize it again.
      */
    
     private final int match(int expectedTokenClass)
     {
         int result = ERROR_TOKEN;
 
         if (!lookahead(expectedTokenClass))
         {
             if (!lookahead(ERROR_TOKEN))
             {
                 if (expectedTokenClass == EOS_TOKEN)
                 {
                     if (ctx.errorCount() == 0)  // only if this is the first error.
                     {
                         error(syntax_error, kError_Parser_ExtraCharsAfterEndOfProgram);
                     }
                     // otherwise, don't say anything.
                     skiperror(expectedTokenClass);
                     result = nextToken;
                     nextToken = EMPTY_TOKEN;
                 }
                 else if (nextToken == EOS_TOKEN)
                 {
                     error(syntax_eos_error, kError_Parser_ExpectedToken,
                           Token.getTokenClassName(expectedTokenClass),
                           scanner.getTokenText(nextToken));
                     skiperror(expectedTokenClass);
                     result = nextToken;
                 }
                 else
                 {
                     error(syntax_eos_error, kError_Parser_ExpectedToken,
                         Token.getTokenClassName(expectedTokenClass),
                         scanner.getTokenText(nextToken));
                     skiperror(expectedTokenClass);
                     result = nextToken;
                     nextToken = EMPTY_TOKEN;
                 }
             }
             else
             {
                 result = nextToken;
                 nextToken = EMPTY_TOKEN;
             }
         }
         else
         {
             result = nextToken;
             lastToken = nextToken;
             nextToken = EMPTY_TOKEN;
         }
 
         return result;
     }
 
     private final int match( final int expectedTokenClasses[], int count )
     {
         int result = ERROR_TOKEN;
 
         if( !lookahead( expectedTokenClasses, count ) )
         {
             if( !lookahead(ERROR_TOKEN) )
             {
                 error(syntax_error, kError_Parser_ExpectedToken,
                 Token.getTokenClassName(expectedTokenClasses[0]),
                 scanner.getTokenText(nextToken));
                 skiperror(expectedTokenClasses[0]);
             }
             result    = nextToken;
             nextToken = EMPTY_TOKEN;
         }
         else
         {
             result    = nextToken;
             lastToken = nextToken;
             nextToken = EMPTY_TOKEN;
         }
 
         return result;
     }
 
     /*
      * Match the current input with an expected token. lookahead is managed by
      * setting the state of this.nexttoken to EMPTY_TOKEN after an match is
      * attempted. the next lookahead will initialize it again.
      */
 
     private final boolean lookaheadSemicolon(int mode)
     {
         final int lt = lookahead();
         
         if (lt==SEMICOLON_TOKEN || lt==EOS_TOKEN || lt==RIGHTBRACE_TOKEN || lookahead(mode) || scanner.followsLineTerminator())
         {
         	return true;
         }
         return false;
     }
 
     private final int matchSemicolon(int mode)
     {
 
         int result = ERROR_TOKEN;
 
         if (lookahead(SEMICOLON_TOKEN))
         {
             result = match(SEMICOLON_TOKEN);
         }
         else if (scanner.followsLineTerminator() || lookahead(EOS_TOKEN) || lookahead(RIGHTBRACE_TOKEN))
         {
             result = SEMICOLON_TOKEN;
         }
         else if ((mode == abbrevIfElse_mode || mode == abbrevDoWhile_mode) && lookahead(mode))
         {
             result = SEMICOLON_TOKEN;
         }
         else if (!lookahead(ERROR_TOKEN))
         {
             if (mode == abbrevFunction_mode)
             {
                 error(syntax_error, kError_Parser_ExpectedLeftBrace);
                 skiperror(LEFTBRACE_TOKEN);
             }
             else
             {
                 error(syntax_error, kError_Parser_ExpectedSemicolon, scanner.getTokenText(nextToken));
                 skiperror(SEMICOLON_TOKEN);
             }
         }
 
         return result;
     }
 
     /*
      * Match a non-insertable semi-colon. This function looks for
      * a SEMICOLON_TOKEN or other grammatical markers that indicate
      * that the EMPTY_TOKEN is equivalent to a semicolon.
      */
 
     private final int matchNoninsertableSemicolon(int mode)
     {
 
         switch ( lookahead() )
         {
         case SEMICOLON_TOKEN:
             shift();
         case EOS_TOKEN: 
         case RIGHTBRACE_TOKEN:
             return SEMICOLON_TOKEN;
         }
  
         if ((mode == abbrevIfElse_mode || mode == abbrevDoWhile_mode) && lookahead(mode))
         {
             return SEMICOLON_TOKEN;
         }
         else
         {
             error(syntax_error, kError_Parser_ExpectedSemicolon, scanner.getTokenText(nextToken));
             skiperror(SEMICOLON_TOKEN);
             return ERROR_TOKEN;
         }
     }
 
     /*
      * This wrapper to scanner->nextToken filters out all non-doccomment comments from the
      *  scanner output and collects all comments (including doccomments) in the commentTable.
      *  The commentTable can be used by external compiler toolchain tools to detect where
      *  comments are located in the source while walking the parse-tree.
      */
     
     private final void getNextToken()
     {
         int tok = scanner.nexttoken(true);
         int tok_type = scanner.getTokenClass(tok);
         
         while(tok_type == SLASHSLASHCOMMENT_TOKEN || tok_type == BLOCKCOMMENT_TOKEN || tok_type == DOCCOMMENT_TOKEN)
         {
             if( save_comment_nodes && (!ctx.scriptAssistParsing || tok_type != DOCCOMMENT_TOKEN))
             {
                 Node newComment = nodeFactory.comment(scanner.getTokenText(tok), tok_type, ctx.input.positionOfMark());
                 comments.push_back(newComment);
             }
 
             if (tok_type == DOCCOMMENT_TOKEN && create_doc_info) // return doccomment tokens if create_doc_info, skip normal comment tokens)
                 break;
 
             tok = scanner.nexttoken(false);
             tok_type = scanner.getTokenClass(tok);
         }
         nextTokenClass = tok_type;
         nextToken = tok;
     }
 
 
     /*
      * Change the lookahead token.
      */
     
     private final void changeLookahead(int tok)
     {
         nextToken = tok;
         nextTokenClass = scanner.getTokenClass(nextToken);
     }
     /*
      * Lookahead to the next token.
      */
 
     private final boolean lookahead(int expectedTokenClass)
     {
         // If the nextToken is EMPTY_TOKEN, then fetch another. This is the first
         // lookahead since the last match.
         if (nextToken == EMPTY_TOKEN)
         {
             getNextToken();
             //printf("%s ",Token::getTokenClassName(scanner.getTokenClass(nextToken)).c_str());
             if (debug)
             {
                 System.err.println("\tnextToken() returning " + Token.getTokenClassName(scanner.getTokenClass(nextToken)));
             }
         }
 
         if (debug)
         {
             // System.err.println("\t" + Token::getTokenClassName(scanner.getTokenClass(nextToken)) + " lookahead(" + Token::getTokenClassName(expectedTokenClass) + ")");
             System.err.println("\t" + Token.getTokenClassName(scanner.getTokenClass(nextToken)) + " lookahead(" + Token.getTokenClassName(expectedTokenClass) + ")");
         }
 
         // Compare the expected token class against the token class of
         // the nextToken.
 
         if (expectedTokenClass != nextTokenClass) // scanner.getTokenClass(nextToken))
         {
             return false;
         }
         return true;
     }    
 
     /*
      * Lookahead (simpler version)
      */
 
     private final int lookahead()
     {
          if (nextToken == EMPTY_TOKEN)
          {
              getNextToken();
          }
         
         if (debug)
         {
             System.err.println("\t" + Token.getTokenClassName(scanner.getTokenClass(nextToken)));
         }
         return nextTokenClass; // scanner.getTokenClass(nextToken);
     }
     
     private final boolean lookahead( final int expectedTokenClasses[], int count )
     {
         // If the nextToken is empty_token, then fetch another. This is the first
         // lookahead since the last match.
 
         if( nextToken == EMPTY_TOKEN )
         {
             getNextToken();
         }
 
         // Compare the expected token class against the token class of
         // the nextToken.
 
         final int tokenclass = nextTokenClass; //scanner.getTokenClass(nextToken);
         int i = 0;
         for( ; i < count; ++i )
         {
             if( expectedTokenClasses[i] == tokenclass )
             {
                 return true;
             }
         }
 
         return false;
     }
 
     /**
      * Set regexp allowed context and force read of next token
      *
      */
     
     private final void lookaheadInSlashRegExpContext()
     {
         scanner.enterSlashRegExpContext();
             lookahead();
         scanner.exitSlashRegExpContext();
     }
     
     /*
      * Start grammar.
      */
 
     /*
      * Identifier
      */
 
     private IdentifierNode parseIdentifier()
     {
         if (debug)
         {
             System.err.println("begin parseIdentifier");
         }
 
         String name = parseIdentifierString();
         IdentifierNode result = nodeFactory.identifier(name, false, scanner.input.positionOfMark());
 
         if (debug)
         {
             System.err.println("finish parseIdentifier");
         }
 
         return result;
     }
 
     public String parseIdentifierString()
     {
         if (debug)
         {
             System.err.println("begin parseIdentifierString");
         }
 
         String result = null;
         final int lt = lookahead();
         
         switch ( lt ) 
         {
         case GET_TOKEN:
             shift();
             result = GET;
             break;
             
         case SET_TOKEN:
             shift();
             result = SET;
             break;
             
         case NAMESPACE_TOKEN:
             shift();
             result = NAMESPACE;
             break;
             
         default:
             result = scanner.getTokenText(match(IDENTIFIER_TOKEN)).intern(); //???hmmm all identifiers are interned....
         }
 
         if (debug)
         {
             System.err.println("finish parseIdentifierString");
         }
 
         return result;
     }
 
     /*
      * PropertyIdentifier
      */
 
     private IdentifierNode parsePropertyIdentifier()
     {
         if (debug)
         {
             System.err.println("begin parsePropertyIdentifier");
         }
 
         int pos = scanner.input.positionOfMark();
         String name = parsePropertyIdentifierString();
         IdentifierNode result = nodeFactory.identifier(name, false, pos);
 
         if (debug)
         {
             System.err.println("finish parsePropertyIdentifier");
         }
 
         return result;
     }
 
     private String parsePropertyIdentifierString()
     {
         if (debug)
         {
             System.err.println("begin parsePropertyIdentifierString");
         }
 
         String result = null;
         final int lt = lookahead();
         
         switch(lt)
         {
         case DEFAULT_TOKEN:
             shift();
             result = DEFAULT;
             break;
             
         case GET_TOKEN:
         	shift();
             result = GET;
         	break;
         	
         case SET_TOKEN:
         	shift();
             result = SET;
         	break;
         	
         case NAMESPACE_TOKEN:
         	shift();
         	result = NAMESPACE;
         	break;
         	
         default:
         	if (HAS_WILDCARDSELECTOR && lt==MULT_TOKEN)
         	{
         		shift();
         		result = ASTERISK;
         	}
         	else
         	{
         	    result = scanner.getTokenText(match(IDENTIFIER_TOKEN)).intern();
         	}
         }
 
         if (debug)
         {
             System.err.println("finish parsePropertyIdentifier");
         }
 
         return result;
     }
 
     /*
      * Qualifier
      */
 
     private IdentifierNode parseQualifier()
     {
 
         if (debug)
         {
             System.err.println("begin parseQualifier");
         }
 
         IdentifierNode result;
 
         switch ( lookahead() )
         {
         case PUBLIC_TOKEN:
             shift();
             result = nodeFactory.identifier(PUBLIC,false,ctx.input.positionOfMark());
             break;
             
         case PRIVATE_TOKEN:
             shift();
             result = nodeFactory.identifier(PRIVATE,false,ctx.input.positionOfMark());
             break;
             
         case PROTECTED_TOKEN:
             shift();
             result = nodeFactory.identifier(PROTECTED,false,ctx.input.positionOfMark());
             break;
             
         default:
             result = parsePropertyIdentifier();
         }
 
         if (debug)
         {
             System.err.println("finish parseQualifier");
         }
 
         return result;
     }
 
     /*
      * SimpleQualifiedIdentifier
      */
 
     private IdentifierNode parseSimpleQualifiedIdentifier()
     {
 
         if (debug)
         {
             System.err.println("begin parseSimpleQualifiedIdentifier");
         }
 
         IdentifierNode result = null;
         IdentifierNode first;
 
         boolean is_attr;
         if (HAS_ATTRIBUTEIDENTIFIERS && lookahead()==AMPERSAND_TOKEN)
         {
             shift();
             is_attr = true;
         }
         else
         {
             is_attr = false;
         }
 
         if( is_attr && lookahead()==LEFTBRACKET_TOKEN )   // @[expr]
         {
             MemberExpressionNode men = parseBrackets(null);
             GetExpressionNode gen = men.selector instanceof GetExpressionNode ? (GetExpressionNode) men.selector : null;
             result = nodeFactory.qualifiedExpression(null,gen.expr,gen.expr.pos());
         }
         else
         {
             first = parseQualifier();
             if (HAS_QUALIFIEDIDENTIFIERS && lookahead()==DOUBLECOLON_TOKEN)
             {
                 shift();
                 MemberExpressionNode temp;
                 temp = nodeFactory.memberExpression(null,nodeFactory.getExpression(first));
                 if( lookahead()==LEFTBRACKET_TOKEN )  // @ns::[expr]
                 {
                     MemberExpressionNode men = parseBrackets(null);
                     GetExpressionNode gen = men.selector instanceof GetExpressionNode ? (GetExpressionNode) men.selector : null;
                     result = nodeFactory.qualifiedExpression(temp,gen.expr,gen.expr.pos());
                 }
                 else
                 {
                     QualifiedIdentifierNode qualid = nodeFactory.qualifiedIdentifier(temp,parsePropertyIdentifierString(),ctx.input.positionOfMark());
                     assert config_namespaces.size() > 0; 
                     if( config_namespaces.last().contains(first.name) )
                     	qualid.is_config_name = true;
                     result = qualid; 
                     result.setOrigTypeToken(DOUBLECOLON_TOKEN);
                 }
             }
             else
             {
                 result = first;
             }
         }
 
         result.setAttr(is_attr);
 
         if (debug)
         {
             System.err.println("finish parseSimpleQualifiedIdentifier");
         }
 
         return result;
     }
 
     /*
      * ExpressionQualifiedIdentifier
      */
 
     private IdentifierNode parseExpressionQualifiedIdentifier()
     {
 
         if (debug)
         {
             System.err.println("begin parseExpressionQualifiedIdentifier");
         }
 
         IdentifierNode result;
         Node first;
 
         boolean is_attr;
         if (HAS_ATTRIBUTEIDENTIFIERS && lookahead()==AMPERSAND_TOKEN)
         {
             shift();
             is_attr = true;
         }
         else
         {
             is_attr = false;
         }
 
         first = parseParenExpression();
         match(DOUBLECOLON_TOKEN);
         result = nodeFactory.qualifiedIdentifier(first, parsePropertyIdentifierString(),ctx.input.positionOfMark());
         result.setAttr(is_attr);
 
         if (debug)
         {
             System.err.println("finish parseExpressionQualifiedIdentifier");
         }
 
         return result;
     }
 
     /*
      * QualifiedIdentifier
      */
 
     private IdentifierNode parseQualifiedIdentifier()
     {
 
         if (debug)
         {
             System.err.println("begin parseQualifiedIdentifier");
         }
 
         IdentifierNode result = (HAS_EXPRESSIONQUALIFIEDIDS && lookahead()==LEFTPAREN_TOKEN)
             ? parseExpressionQualifiedIdentifier()
             : parseSimpleQualifiedIdentifier();
 
         if (debug)
         {
             System.err.println("finish parseQualifiedIdentifier");
         }
 
         return result;
     }
 
     /*
      * PrimaryExpression
      */
 
     private Node parsePrimaryExpression()
     {
 
         if (debug)
         {
             System.err.println("begin parsePrimaryExpression");
         }
 
         Node result;
         final int lt = lookahead();
 
         switch ( lt )
         {
         case NULL_TOKEN:
             shift();
             result = nodeFactory.literalNull(ctx.input.positionOfMark());
             break;
             
         case TRUE_TOKEN:    
             shift();
             result = nodeFactory.literalBoolean(true,ctx.input.positionOfMark());
             break;
             
         case FALSE_TOKEN:
             shift();
             result = nodeFactory.literalBoolean(false,ctx.input.positionOfMark());
             break;
             
         case PRIVATE_TOKEN:
             shift();
             result = nodeFactory.identifier(PRIVATE,false,ctx.input.positionOfMark());
             break;
             
         case PUBLIC_TOKEN:
             shift();
             result = nodeFactory.identifier(PUBLIC,false,ctx.input.positionOfMark());
             break;
             
         case PROTECTED_TOKEN:
             shift();
             result = nodeFactory.identifier(PROTECTED,false,ctx.input.positionOfMark());
             break;
         
         case NUMBERLITERAL_TOKEN:
             result = nodeFactory.literalNumber(scanner.getTokenText(match(NUMBERLITERAL_TOKEN)),ctx.input.positionOfMark());
             break;
             
         case STRINGLITERAL_TOKEN:
         {
             boolean[] is_single_quoted = new boolean[1];
             String enclosedText = scanner.getStringTokenText(match(STRINGLITERAL_TOKEN), is_single_quoted);
             result = nodeFactory.literalString(enclosedText, ctx.input.positionOfMark(), is_single_quoted[0] );
             break;
         }
         
         case THIS_TOKEN:
             shift();
             result = nodeFactory.thisExpression(ctx.input.positionOfMark());
             break;
             
         case LEFTPAREN_TOKEN:
             result = parseParenListExpression();
             break;
             
         case LEFTBRACKET_TOKEN:
             result = parseArrayLiteral();
             break;
             
         case LEFTBRACE_TOKEN:
             result = parseObjectLiteral();
             break;
             
         case FUNCTION_TOKEN:
         {
             shift();
             IdentifierNode first = null;
             if (lookahead()==IDENTIFIER_TOKEN)
             {
                 first = parseIdentifier();
             }
             result = parseFunctionCommon(first);
             break;
         }
   
         case PACKAGE_TOKEN:
             if (within_package)
             {
                 error(syntax_error, kError_NestedPackage);
                 result = nodeFactory.error(ctx.input.positionOfMark(), kError_NestedPackage);
             }
             else
             {
                 error(syntax_error,kError_Parser_ExpectedPrimaryExprBefore,scanner.getTokenText(nextToken));
                 result = nodeFactory.error(ctx.input.positionOfMark(), kError_Parser_ExpectedPrimaryExprBefore);
             }
             skiperror(LEFTBRACE_TOKEN);
             skiperror(RIGHTBRACE_TOKEN);
             break;
             
         case CATCH_TOKEN: case FINALLY_TOKEN: case ELSE_TOKEN:
             error(syntax_error, kError_Parser_ExpectedPrimaryExprBefore,
                       scanner.getTokenText(nextToken));
             skiperror(LEFTBRACE_TOKEN);
             skiperror(RIGHTBRACE_TOKEN);
             result = nodeFactory.error(ctx.input.positionOfMark(), kError_Parser_ExpectedPrimaryExprBefore);
             break;
         
         case XMLMARKUP_TOKEN: case LESSTHAN_TOKEN:
             result = (HAS_XMLLITERALS)
                 ? parseXMLLiteral()
                 : parseTypeIdentifier();
             break;
             
         case REGEXPLITERAL_TOKEN:
             result = (HAS_REGULAREXPRESSIONS) 
                 ? nodeFactory.literalRegExp(scanner.getTokenText(match(REGEXPLITERAL_TOKEN)),ctx.input.positionOfMark())
                 : parseTypeIdentifier();
             break;
             
         default:    
                 result = parseTypeIdentifier();
         }
 
         if (debug)
         {
             System.err.println("finish parsePrimaryExpression");
         }
 
         return result;
     }
     
     
     private boolean is_xmllist = false;
     
     private LiteralXMLNode parseXMLLiteral()
     {
         is_xmllist = false;
         LiteralXMLNode result = null;
         Node first;
         int pos = ctx.input.positionOfMark();
         if(lookahead()==XMLMARKUP_TOKEN)
         {
             first  = nodeFactory.list(null,nodeFactory.literalString(scanner.getTokenText(match(XMLMARKUP_TOKEN)),pos));
         }
         else
         {
             scanner.pushState();
             first = parseXMLElement();
             scanner.popState();
         }
         if( first != null )
         {
             ListNode list = nodeFactory.list(null,first,pos);
             result = nodeFactory.literalXML(list,is_xmllist,ctx.input.positionOfMark());
         }
         return result;
     }
 
     Node parseXMLElement()
     {
         Node result;
         boolean is_xmllist = false;
 
         match(LESSTHAN_TOKEN);
         scanner.enterSlashDivContext();  // after the initial < tokenize in slashdiv mode to avoid /> conflict
         if(lookahead()==GREATERTHAN_TOKEN)
         {
             is_xmllist = true;
             result = nodeFactory.list(null,nodeFactory.literalString("",0));
         }
         else
         {
             result = nodeFactory.list(null,nodeFactory.literalString("<",0));
             result = parseXMLName(result);
             result = parseXMLAttributes(result);
         }
 
         if(lookahead()==GREATERTHAN_TOKEN)
         {
             shift();
             if( !is_xmllist )
             {
                 result = concatXML(result,nodeFactory.literalString(">",0));
             }
             result = parseXMLElementContent(result);
             if(lookahead()==EOS_TOKEN)
             {
                 scanner.exitSlashDivContext();
                 error(kError_Lexical_NoMatchingTag);
                 return nodeFactory.error(ctx.input.positionOfMark(),kError_Lexical_NoMatchingTag);
             }
             match(XMLTAGSTARTEND_TOKEN); // "</"
             if( lookahead(GREATERTHAN_TOKEN) )
             {
                 if( !is_xmllist )
                 {
                     ctx.error(ctx.input.positionOfMark(),kError_MissingXMLTagName);
                 }
             }
             else
             {
                 result = concatXML(result,nodeFactory.literalString("</",0));
                 result = parseXMLName(result);
                 result = concatXML(result,nodeFactory.literalString(">",0));
             }
             match(GREATERTHAN_TOKEN);
         }
         else
         {
             match(XMLTAGENDEND_TOKEN);  // "/>"
             result = concatXML(result,nodeFactory.literalString("/>",0));
         }
         scanner.exitSlashDivContext();
         this.is_xmllist = is_xmllist;
         return result;
     }
 
     Node concatXML(Node left, Node right)
     {
     	Node tmpLeft = left;
         
     	if (left instanceof ListNode && ((ListNode)left).size() == 1)
     	{
     		tmpLeft = ((ListNode)left).items.first();
     	}
 
     	// Concatenate strings
         
     	if (tmpLeft instanceof LiteralStringNode && right instanceof LiteralStringNode)
     	{
     		return nodeFactory.literalString(((LiteralStringNode)tmpLeft).value + ((LiteralStringNode)right).value, left.pos());
     	}
 
     	// If the lhs is a + expression with a string literal rhs,
     	// and the rhs is a string literal, turn (x+y)+z into x+(y+z)
     	if (left instanceof BinaryExpressionNode && right instanceof LiteralStringNode)
     	{
     		BinaryExpressionNode leftBinary = (BinaryExpressionNode)left;
     		if (leftBinary.op == PLUS_TOKEN && leftBinary.rhs instanceof LiteralStringNode)
     		{
         		return nodeFactory.binaryExpression(PLUS_TOKEN, leftBinary.lhs,
         				nodeFactory.literalString(((LiteralStringNode)leftBinary.rhs).value + ((LiteralStringNode)right).value), leftBinary.pos());
     		}
     	}
 
     	// Couldn't optimize, return ordinary binary expression
     	return nodeFactory.binaryExpression(PLUS_TOKEN,left,right);
     }
 
      Node parseXMLName(Node first)
     {
         Node result;
         
         if(lookahead()==LEFTBRACE_TOKEN)
         {
             shift();
             scanner.pushState();     // save the state of the scanner
             result = concatXML(first,parseListExpression(allowIn_mode));
             match(RIGHTBRACE_TOKEN);
             scanner.popState();      // restore the state of the scanner
         }
         else
         {
             if( lookahead(xmlid_tokens,xmlid_tokens_count) )
             {
                 result  = concatXML(first,nodeFactory.literalString(scanner.getTokenText(match(xmlid_tokens,xmlid_tokens_count)),ctx.input.positionOfMark()));
             }
             else
             {
                 error(syntax_error,kError_ErrorNodeError,"invalid xml name");
                 skiperror(syntax_xml_error);
                 result = nodeFactory.error(ctx.input.positionOfMark(),kError_MissingXMLTagName);
             }
             
             while( true )
             {
                 final int lt = lookahead();
                 String separator_text;
                 
                 if( lt == DOT_TOKEN )
                 {   
                     separator_text = ".";
                 }
                 else if( lt == MINUS_TOKEN )
                 {   
                     separator_text = "-";
                 }
                 else if( lt == COLON_TOKEN )
                 {   
                     separator_text = ":";
                 }
                 else    break;
 
                 shift();
                 
                 if( lookahead(xmlid_tokens,xmlid_tokens_count) )
                 {
                     result = concatXML(result,nodeFactory.literalString(separator_text,0));
                     result = concatXML(result,nodeFactory.literalString(scanner.getTokenText(match(xmlid_tokens,xmlid_tokens_count)),ctx.input.positionOfMark()));
                 }
                 else
                 {
                     error(syntax_error,kError_MissingXMLTagName);
                     skiperror(syntax_xml_error);
                     result = nodeFactory.error(ctx.input.positionOfMark(),kError_MissingXMLTagName);
                 }
             }
         }
         return result;
     }
 
     Node parseXMLAttributes(Node first)
     {
         Node result = first;
         while( !(lookahead(GREATERTHAN_TOKEN)||lookahead(XMLTAGENDEND_TOKEN)||lookahead(EOS_TOKEN) ) )
         {
             result = concatXML(result,nodeFactory.literalString(" ",0));
             result = parseXMLAttribute(result);
         }
         return result;
     }
 
     Node parseXMLAttribute(Node first)
     {
         Node result = parseXMLName(first);
         if(lookahead()==ASSIGN_TOKEN)
         {
             shift();
 
             Node value = null;
             boolean single_quote = false;
 
             if(lookahead()==STRINGLITERAL_TOKEN)
             {
                 boolean[] is_single_quoted = new boolean[1];
                 String enclosedText = scanner.getStringTokenText(match(STRINGLITERAL_TOKEN), is_single_quoted);
                 value = nodeFactory.literalString( enclosedText, ctx.input.positionOfMark(), is_single_quoted[0] );
                 single_quote = is_single_quoted[0];
             }
             else
             if(lookahead()==LEFTBRACE_TOKEN)
             {
                 shift();
                 scanner.pushState();     // save the state of the scanner
                 Node expr = parseListExpression(allowIn_mode);
                 value = nodeFactory.invoke("[[ToXMLAttrString]]",nodeFactory.argumentList(null,expr),0);
                 match(RIGHTBRACE_TOKEN);
                 scanner.popState();      // restore the state of the scanner
             }
             else
             {
                 error(syntax_error,kError_ParserExpectingLeftBraceOrStringLiteral);
             }
 
             result = concatXML(result,nodeFactory.literalString(single_quote ? "='" : "=\"",0));
 
             result = concatXML(result,value);
 
             result = concatXML(result,nodeFactory.literalString(single_quote ? "'" : "\"",0));
         }
 
         return result;
     }
 
     Node parseXMLElementContent(Node first)
     {
         Node result = first;
 
         // begin xmlelementcontent
 
         scanner.pushState();
 
         scanner.state = xmltext_state;
         while( !(lookahead()==XMLTAGSTARTEND_TOKEN || lookahead()==EOS_TOKEN) )  // </
         {
             int lt = lookahead();
             
             if(lt==LEFTBRACE_TOKEN)
             {
                 shift();
                 scanner.pushState();     // save the state of the scanner
                 Node expr = parseListExpression(allowIn_mode);
                 expr = nodeFactory.invoke("[[ToXMLString]]",nodeFactory.argumentList(null,expr),0);
                 result = concatXML(result,expr);
                 match(RIGHTBRACE_TOKEN);
                 scanner.popState();      // restore the state of the scanner
             }
             else
             if(lt==LESSTHAN_TOKEN)
             {
                 scanner.state = start_state;
                 result = concatXML(result,parseXMLElement());
             }
             else
             if(lt==XMLMARKUP_TOKEN)
             {
                 result  = concatXML(result,nodeFactory.literalString(scanner.getTokenText(match(XMLMARKUP_TOKEN)),ctx.input.positionOfMark()));
             }
             else
             if(lt==XMLTEXT_TOKEN)
             {
                 result  = concatXML(result,nodeFactory.literalString(scanner.getTokenText(match(XMLTEXT_TOKEN)),ctx.input.positionOfMark()));
             }
             else
             {
                 error(syntax_error,kError_MissingXMLTagName);
                 skiperror(RIGHTBRACE_TOKEN);
                 scanner.popState();
                 return result;
             }
             scanner.state = xmltext_state;
         }
         scanner.popState();
         return result;
     }
 
 /*
 XMLElement
     <  XMLTagContent  />
     <  XMLTagContent  >  XMLElementContentopt  <  XMLTagContent  />
 
 XMLTagContent
     XMLTagCharacters XMLTagContentopt
     =  Whitespaceopt  {  Expression  }  XMLTagContentopt
     {  Expression  }  XMLTagContentopt
 
 XMLElementContent
     XMLMarkup  XMLElementContentopt
     XMLText  XMLElementContentopt
     {  Expression  }  XMLElementContentopt
 */
 
     /*
     public LiteralXMLNode parseXMLLiteral()
     {
         LiteralXMLNode result;
         ListNode first;
         if (lookahead(LESSTHAN_TOKEN))
         {
             match(LESSTHAN_TOKEN);
             first = nodeFactory.list(null, nodeFactory.literalString(scanner.getTokenText(match(XMLPART_TOKEN))));
             scanner.pushState();     // save the state of the scanner
             first = nodeFactory.list(first, parseListExpression(allowIn_mode));
             match(RIGHTBRACE_TOKEN);  // eat the expression escape character
             scanner.popState();      // restore the state of the scanner
 
             while (lookahead(XMLPART_TOKEN))
             {
                 first = nodeFactory.list(first, nodeFactory.literalString(scanner.getTokenText(match(XMLPART_TOKEN))));
                 scanner.pushState();
                 first = nodeFactory.list(first, parseListExpression(allowIn_mode));
                 match(RIGHTBRACE_TOKEN);  // eat the expression escape character
                 scanner.popState();
             }
             first = nodeFactory.list(first, nodeFactory.literalString(scanner.getTokenText(match(XMLLITERAL_TOKEN))));
             result = nodeFactory.literalXML(first,ctx.input.positionOfMark());
         }
         else // xml markup
         {
 //            first  = nodeFactory.List(0,nodeFactory.literalString(scanner.getTokenText(match(XMLMARKUP_TOKEN))));
 //            result = nodeFactory.LiteralXML(first,ctx.input.positionOfMark());
             result = null;
         }
         return result;
     }
     */
 
     /*
      * ParenExpression
      */
 
     private Node parseParenExpression()
     {
 
         if (debug)
         {
             System.err.println("begin parseParenExpression");
         }
 
         Node result;
 
         scanner.enterSlashRegExpContext();
         match(LEFTPAREN_TOKEN);
         int mark = scanner.input.positionOfMark();
         result = parseAssignmentExpression(allowIn_mode);
         result.setPosition(mark);
         match(RIGHTPAREN_TOKEN);
         scanner.exitSlashRegExpContext();
 
         if (debug)
         {
             System.err.println("finish parseParenExpression");
         }
 
         return result;
     }
 
     /*
      * ParenListExpression
      */
 
     private ListNode parseParenListExpression()
     {
         
         if (debug)
         {
             System.err.println("begin parseParenListExpression");
         }
 
         scanner.enterSlashRegExpContext();
         match(LEFTPAREN_TOKEN);
         ListNode result = parseListExpression(allowIn_mode);
         match(RIGHTPAREN_TOKEN);
         scanner.exitSlashRegExpContext();
 
         if (debug)
         {
             System.err.println("finish parseParenListExpression");
         }
 
         return result;
     }
 
     /*
      * PrimaryExpressionOrExpressionQualifiedIdentifier
      */
 
     private Node parsePrimaryExpressionOrExpressionQualifiedIdentifier()
     {
 
         if (debug)
         {
             System.err.println("begin parsePrimaryExpressionOrExpressionQualifiedIdentifier");
         }
 
         Node result;
 
         if (lookahead()==LEFTPAREN_TOKEN)
         {
             ListNode first = parseParenListExpression();
             if (HAS_EXPRESSIONQUALIFIEDIDS && lookahead()==DOUBLECOLON_TOKEN)
             {
                 shift();
                 if (first.size() != 1)
                 {
                     result = error(kError_Parser_ExpectingASingleExpressionWithinParenthesis);
                     skiperror(expression_syntax_error);
                 }
                 else
                 {
                     result = nodeFactory.qualifiedIdentifier(first.items.last(), parseIdentifierString(), ctx.input.positionOfMark());
                     result = nodeFactory.memberExpression(null, nodeFactory.getExpression(result));
                 }
             }
             else
             {
                 result = first;
             }
         }
         else
         {
             result = parsePrimaryExpression();
         }
 
         if (debug)
         {
             System.err.println("finish parsePrimaryExpressionOrExpressionQualifiedIdentifier");
         }
 
         return result;
     }
 
     /*
      * FunctionExpression
      */
 
     private FunctionCommonNode parseFunctionCommon(IdentifierNode first)
     {
 
         if (debug)
         {
             System.err.println("begin parseFunctionCommon");
         }
 
         FunctionCommonNode result;
         FunctionSignatureNode second;
         StatementListNode third = null;
 
         boolean saved_has_arguments = nodeFactory.has_arguments;  // for nested functions
         boolean saved_has_rest = nodeFactory.has_rest;  // for nested functions
         boolean saved_has_dxns = nodeFactory.has_dxns;  //
 
         nodeFactory.has_arguments = false;  // set by Identifier, captured by FunctionCommon
         nodeFactory.has_rest = false;  // set by RestParameterNode, captured by FunctionCommon
         nodeFactory.has_dxns = false;
 
         boolean is_ctor = false;
         if (ctx.statics.es4_nullability && block_kind_stack.last() == CLASS_TOKEN
                 && first != null && first.name.equals(current_class_name)) {
             is_ctor = true;
         }
 
         block_kind_stack.add(FUNCTION_TOKEN);
 
         second = (is_ctor) 
             ? parseConstructorSignature() 
             : parseFunctionSignature();
 
         DefaultXMLNamespaceNode saved_dxns = nodeFactory.dxns;
 
         //nodeFactory.StartClassDefs();
 
         if (lookahead()==LEFTBRACE_TOKEN)
         {
             third = parseBlock();
 
             if (!AVMPLUS)
             {
                 third = nodeFactory.statementList(third, nodeFactory.returnStatement(null, scanner.input.positionOfMark()));
             }
 
             if ( third == null )
             {
                 // Function had an empty body.  Create an empty list to represent { }
                 third = nodeFactory.statementList(null, null);
             }
         }
         else
         {
             matchSemicolon(abbrevFunction_mode);
         }
 
         // Append a default return statement with the line number at the position of the } (happens in nodeFactory->FunctionCommon)
         result = nodeFactory.functionCommon(ctx,first,second,third,first!=null?first.pos():second.pos());
         result.default_dxns = nodeFactory.has_dxns?null:saved_dxns;
 
         nodeFactory.has_arguments = saved_has_arguments;
         nodeFactory.has_rest = saved_has_rest;
         nodeFactory.has_dxns = saved_has_dxns;
 
         //nodeFactory.FinishClassDefs();
         block_kind_stack.removeLast();
 
 
         if (debug)
         {
             System.err.println("finish parseFunctionCommon");
         }
 
         return result;
     }
 
     /*
      * ObjectLiteral
      */
 
     private Node parseObjectLiteral()
     {
 
         if (debug)
         {
             System.err.println("begin parseObjectLiteral");
         }
 
         Node result;
         ArgumentListNode first;
 
         int pos = ctx.input.positionOfMark();
 
 		scanner.enterSlashRegExpContext();
         match(LEFTBRACE_TOKEN);
 
         if (lookahead()==RIGHTBRACE_TOKEN)
         {
             first = null;
             shift();
         }
         else
         {
             // Inlining parseFieldList:
             //     FieldList: LiteralField FieldListPrime
 
             first = parseFieldListPrime(nodeFactory.argumentList(null, parseLiteralField()));
             match(RIGHTBRACE_TOKEN);
         }
 
 		scanner.exitSlashRegExpContext();			
         result = nodeFactory.literalObject(first,pos);
 
         if (debug)
         {
             System.err.println("finish parseObjectLiteral with result = ");
         }
 
         return result;
     }
 
     /*
      * FieldListPrime
      */
 
     private ArgumentListNode parseFieldListPrime(ArgumentListNode first)
     {
 
         if (debug)
         {
             System.err.println("begin parseFieldListPrime");
         }
 
         ArgumentListNode result;
 
         if (lookahead()==COMMA_TOKEN)
         {
             shift();
             Node second = parseLiteralField();
             result = parseFieldListPrime(nodeFactory.argumentList(first, second));
         }
         else
         {
             result = first;
         }
 
         if (debug)
         {
             System.err.println("finish parseFieldListPrime with result = ");
         }
 
         return result;
     }
 
     /*
      * LiteralField
      */
 
     private Node parseLiteralField()
     {
 
         if (debug)
         {
             System.err.println("begin parseLiteralField");
         }
 
         Node result;
         Node first;
         Node second;
         ListNode l = null;
 
         first = parseFieldOrConfigName();
         if( first.isConfigurationName() && lookahead()!=COLON_TOKEN )
         {
         	// If we got back a configuration name, then 
         	// we need to parse the actual name
         	// e.g. {... CONFIG::Debug y:val ... }
         	l = nodeFactory.list(null, first);
         	first = parseFieldOrConfigName();
         }
         match(COLON_TOKEN);
         second = parseAssignmentExpression(allowIn_mode);
 
         result = nodeFactory.literalField(first, second);
 
         if( l != null )
         {
         	result = nodeFactory.list(l, result);
         }
         if (debug)
         {
             System.err.println("finish parseLiteralField");
         }
 
         return result;
     }
 
     /*
      * FieldName
      */
 
     private Node parseFieldOrConfigName()
     {
 
         if (debug)
         {
             System.err.println("begin parseFieldName");
         }
 
         Node result;
         final int lt = lookahead();
         
         if (HAS_NONIDENTFIELDNAMES && lt==STRINGLITERAL_TOKEN)
         {
             boolean[] is_single_quoted = new boolean[1];
             String enclosedText = scanner.getStringTokenText(match(STRINGLITERAL_TOKEN), is_single_quoted);
             result = nodeFactory.literalString( enclosedText, ctx.input.positionOfMark(), is_single_quoted[0] );
         }
         else if (HAS_NONIDENTFIELDNAMES && lt==NUMBERLITERAL_TOKEN)
         {
             result = nodeFactory.literalNumber(scanner.getTokenText(match(NUMBERLITERAL_TOKEN)),ctx.input.positionOfMark());
         }
         else if (HAS_NONIDENTFIELDNAMES && lt==LEFTPAREN_TOKEN)
         {
             result = parseParenExpression();
         }
         else
         {
         	IdentifierNode ident = parseIdentifier();
             assert config_namespaces.size() > 0; 
             if( config_namespaces.last().contains(ident.name) && lookahead()==DOUBLECOLON_TOKEN)
             {
             	shift();
             	QualifiedIdentifierNode qualid = nodeFactory.qualifiedIdentifier(ident, parseIdentifierString(),ctx.input.positionOfMark());
             	qualid.is_config_name = true;
             	result = qualid;
             }
             else
             {
             	result = ident;
             }
         }
 
         if (debug)
         {
             System.err.println("finish parseFieldName");
         }
 
         return result;
     }
 
     /*
      * ArrayLiteral
      *     [ ElementList ]
      */
 
     private LiteralArrayNode parseArrayLiteral()
     {
 
         if (debug)
         {
             System.err.println("begin parseArrayLiteral");
         }
 		
         LiteralArrayNode result;
         ArgumentListNode first;
 
         int pos = ctx.input.positionOfMark();
 
         scanner.enterSlashRegExpContext();
         match(LEFTBRACKET_TOKEN);
 
         // ElementList : LiteralElement ElementListPrime (inlined)
 
         first = (lookahead()==RIGHTBRACKET_TOKEN)
             ? null
             : parseElementListPrime(nodeFactory.argumentList(null, parseLiteralElement()));
 
         result = nodeFactory.literalArray(first,pos);
         match(RIGHTBRACKET_TOKEN);
 		scanner.exitSlashRegExpContext();
 
         if (debug)
         {
             System.err.println("finish parseArrayLiteral");
         }
         
         return result;
     }
 
     /*
      * ElementListPrime
      *     , LiteralElement ElementListPrime
      *     empty
      */
 
     private ArgumentListNode parseElementListPrime(ArgumentListNode first)
     {
 
         if (debug)
         {
             System.err.println("begin parseElementListPrime");
         }
 
         ArgumentListNode result;
 
         while (lookahead()==COMMA_TOKEN)
         {
             shift();
             Node second;
             second = parseLiteralElement(); // May be empty.
             if( second != null )
             {
                 first = nodeFactory.argumentList(first, second);
             }
         }
 
         result = first;
 
         if (debug)
         {
             System.err.println("finish parseElementListPrime");
         }
 
         return result;
     }
 
     /*
      * LiteralElement
      *     AssignmentExpression
      *     empty
      */
 
     private Node parseLiteralElement()
     {
 
         if (debug)
         {
             System.err.println("begin parseLiteralElement");
         }
 
         Node result;
         Node first;
         int lt = lookahead();
         
         if(lt==COMMA_TOKEN)
         {
             result = nodeFactory.emptyElement(ctx.input.positionOfMark());
         }
         else if(lt==RIGHTBRACKET_TOKEN)
         {
             result = null;
         }
         else
         {
         	first = parseAssignmentExpression(allowIn_mode);
         	lt = lookahead();
         	if( first.isConfigurationName() && lt!=COMMA_TOKEN && lt!=RIGHTBRACKET_TOKEN ) //??? squirrelly logic --check {pmd}
         	{
         		// We have an element with a config attr i.e. 
         		// [..., CONFIG::Debug 4, ...]
         		ListNode list = nodeFactory.list(null, first);
         		result = nodeFactory.list(list, parseAssignmentExpression(allowIn_mode));
         	}
         	else
         	{
         		result = first;
         	}
         }
 
         if (debug)
         {
             System.err.println("finish parseLiteralElement");
         }
 
         return result;
     }
 
     /*
      * SuperExpression
      *     super
      *     super ParenExpression
      */
 
     private Node parseSuperExpression()
     {
 
         if (debug)
         {
             System.err.println("begin parseSuperExpression");
         }
 
         Node result;
 
         match(SUPER_TOKEN);
         Node first = null;
         if (lookahead()==LEFTPAREN_TOKEN)
         {
             first = parseParenExpression();
         }
         result = nodeFactory.superExpression(first,scanner.input.positionOfMark()-5);
 
         if (debug)
         {
             System.err.println("finish parseSuperExpression");
         }
 
         return result;
     }
 
     /*
      * PostfixExpression
      */
 
     private Node parsePostfixExpression()
     {
 
         if (debug)
         {
             System.err.println("begin parsePostfixExpression");
         }
 
         Node first;
         Node result;
         final int lt = lookahead();
 
         switch ( lt )
         {
         case PUBLIC_TOKEN: case PRIVATE_TOKEN: case PROTECTED_TOKEN: case DEFAULT_TOKEN: 
         case GET_TOKEN: case SET_TOKEN: case IDENTIFIER_TOKEN: case NAMESPACE_TOKEN: 
         case MULT_TOKEN: case AMPERSAND_TOKEN:
             first = parseAttributeExpression();
             if ( !lookaheadSemicolon(full_mode) && (lookahead() == PLUSPLUS_TOKEN || lookahead() == MINUSMINUS_TOKEN) )
             {
                 first = parsePostfixIncrement(first);
             }
             break;
             
         case NULL_TOKEN: case TRUE_TOKEN: case FALSE_TOKEN: case NUMBERLITERAL_TOKEN:
         case STRINGLITERAL_TOKEN: case THIS_TOKEN: case REGEXPLITERAL_TOKEN: case LEFTPAREN_TOKEN: 
         case LEFTBRACKET_TOKEN: case LEFTBRACE_TOKEN: case FUNCTION_TOKEN:
             first = parsePrimaryExpressionOrExpressionQualifiedIdentifier();
             break;
             
         case SUPER_TOKEN:    
             first = parseSuperExpression();
             break;
             
         case NEW_TOKEN:
             first = parseShortNewExpression();
             if (lookahead()==LEFTPAREN_TOKEN)
             {
                 first = parseArguments(first);
             }
             else
             {
                 first = nodeFactory.callExpression(first, null);
             }
 
             first = nodeFactory.newExpression(first);  // translates call to new
             if ( !lookaheadSemicolon(full_mode) && ((lookahead()==PLUSPLUS_TOKEN || lookahead()==MINUSMINUS_TOKEN)))
             {
                 first = parsePostfixIncrement(first);
             }
             break;
             
         default:
             first = parseFullNewSubexpression();
             if (lookahead()==LEFTPAREN_TOKEN)
             {
                 first = parseArguments(first);
             }    
         }
  
         result = parseFullPostfixExpressionPrime(first);
 
         if (debug)
         {
             System.err.println("finish parsePostfixExpression");
         }
 
         return result;
     }
 
     /*
      * PostfixIncrement
      */
 
     private Node parsePostfixIncrement(Node first)
     {
 
         if (debug)
         {
             System.err.println("begin parsePostfixIncrement");
         }
 
         Node result;
         final int lt = lookahead();
         
         if (lt==PLUSPLUS_TOKEN || lt==MINUSMINUS_TOKEN)
         {
             result = nodeFactory.postfixExpression(match(lt), first,ctx.input.positionOfMark());
         }
         else
         {
             result = error(kError_Parser_ExpectingIncrOrDecrOperator);
             skiperror(syntax_error);
         }
 
         if (debug)
         {
             System.err.println("finish parsePostfixIncrement");
         }
 
         return result;
     }
 
     /*
      * FullPostfixExpressionPrime
      *     PropertyOperator FullPostfixExpressionPrime
      *     Arguments FullPostfixExpressionPrime
      *     FullPostfixExpressionIncrementExpressionSuffix
      *     empty
      */
 
     private Node parseFullPostfixExpressionPrime(Node first)
     {
 
         if (debug)
         {
             System.err.println("begin parseFullPostfixExpressionPrime");
         }
 
         Node result;
         final int lt = lookahead();
 
         if (lt==DOT_TOKEN || lt==LEFTBRACKET_TOKEN || lt==DOTLESSTHAN_TOKEN || (HAS_DESCENDOPERATORS && lt==DOUBLEDOT_TOKEN))
         {
             first = parsePropertyOperator(first);
             result = parseFullPostfixExpressionPrime(first);
         }
         else if (lt==LEFTPAREN_TOKEN)
         {
             first = parseArguments(first);
             result = parseFullPostfixExpressionPrime(first);
         }
         else if ( !lookaheadSemicolon(full_mode) && ((lookahead()==PLUSPLUS_TOKEN || lookahead()==MINUSMINUS_TOKEN)))
         {
             first = parsePostfixIncrement(first);
             result = parseFullPostfixExpressionPrime(first);
         }
         else
         {
             result = first;
         }
 
         if (debug)
         {
             System.err.println("finish parseFullPostfixExpressionPrime");
         }
 
         return result;
     }
 
     /*
      * AttributeExpression
      *    SimpleQualifiedIdentifier
      *    AttributeExpression PropertyOperator
      *    AttributeExpression Arguments
      */
 
     private Node parseAttributeExpression()
     {
 
         if (debug)
         {
             System.err.println("begin parseAttributeExpression");
         }
 
         Node result;
         Node first;
 
         IdentifierNode ident = parseSimpleQualifiedIdentifier();
     /*
     if( ident.name.equals("*") )
         {
             ctx.error(scanner.input.positionOfMark()-2,kError_InvalidWildcardIdentifier);
         }
     */
         first = nodeFactory.memberExpression(null, nodeFactory.getExpression(ident));
         
         final int lt = lookahead();
         
         if (lt==DOT_TOKEN || lt==LEFTBRACKET_TOKEN || lt==DOUBLEDOT_TOKEN || lt==LEFTPAREN_TOKEN || lt==AMPERSAND_TOKEN)    
         {
             result = parseAttributeExpressionPrime(first);
         }
         else
         {
 //            MemberExpressionNode memb = first instanceof MemberExpressionNode ? (MemberExpressionNode) first : null;
 //            if( memb != null && memb.selector.is_package )
 //            {
 //                IdentifierNode id = memb.selector.expr instanceof IdentifierNode ? (IdentifierNode) memb.selector.expr : null;
 //                if( id != null )
 //                {
 //                    ctx.error(memb.selector.expr.pos(),kError_IllegalPackageReference, id.name);
 //                }
 //                memb.selector.is_package = false; // hack, to avoid reporting same error later
 //            }
             result = first;
         }
 
         if (debug)
         {
             System.err.println("finish parseAttributeExpression");
         }
 
         return result;
     }
 
     private Node parseAttributeExpressionPrime(Node first)
     {
 
         if (debug)
         {
             System.err.println("begin parseAttributeExpressionPrime");
         }
 
         Node result;
         final int lt = lookahead();
         
         if (lt==DOT_TOKEN || lt==DOUBLEDOT_TOKEN || lt==LEFTBRACKET_TOKEN || lt==DOTLESSTHAN_TOKEN)
         {
             result = parseAttributeExpressionPrime(parsePropertyOperator(first));
         }
         else
         if (lt==LEFTPAREN_TOKEN) // Arguments
         {
 /*
             MemberExpressionNode memb = first instanceof MemberExpressionNode ? (MemberExpressionNode) first : null;
             if( memb != null && memb.selector.is_package )
             {
                 IdentifierNode ident = memb.selector.expr instanceof IdentifierNode ? (IdentifierNode) memb.selector.expr : null;
                 if( ident != null )
                 {
                     ctx.error(memb.selector.expr.pos(),kError_IllegalPackageReference, ident.name);
                     memb.selector.is_package = false; // to avoid redundant errors
                 }
             }
 */
             result = parseAttributeExpressionPrime(parseArguments(first));
         }
         else  // empty
         {
 /*
             MemberExpressionNode memb = first instanceof MemberExpressionNode ? (MemberExpressionNode) first : null;
             if( memb != null && memb.selector.is_package )
             {
                 IdentifierNode ident = memb.selector.expr instanceof IdentifierNode ? (IdentifierNode) memb.selector.expr : null;
                 if( ident != null )
                 {
                     ctx.error(memb.selector.expr.pos(),kError_IllegalPackageReference, ident.name);
                 }
             }
 */
             result = first;
         }
 
         if (debug)
         {
             System.err.println("finish parseAttributeExpressionPrime");
         }
 
         return result;
     }
 
  
     /*
      * FullNewExpression
      *     new FullNewSubexpression Arguments
      */
 
     private Node parseFullNewExpression()
     {
 
         if (debug)
         {
             System.err.println("begin parseFullNewExpression");
         }
 
         Node result;
         Node first;
 
         match(NEW_TOKEN);
         first = parseFullNewSubexpression();
         result = nodeFactory.newExpression(parseArguments(first));
 
         if (debug)
         {
             System.err.println("finish parseFullNewExpression");
         }
 
         return result;
     }
 
     /*
      * FullNewSubexpression
      *     PrimaryExpression FullNewSubexpressionPrime
      *     QualifiedIdentifier FullNewSubexpressionPrime
      *     FullNewExpression FullNewSubexpressionPrime
      *     SuperExpression PropertyOperator FullNewSubexpressionPrime
      */
 
     private Node parseFullNewSubexpression()
     {
 
         if (debug)
         {
             System.err.println("begin parseFullNewSubexpression");
         }
 
         Node result;
         Node first;
 
         switch ( lookahead() )
         {
         case NEW_TOKEN:
             first = parseFullNewExpression();
             result = parseFullNewSubexpressionPrime(first);
             break;
             
         case SUPER_TOKEN:
             first = parseSuperExpression();
             first = parsePropertyOperator(first);
             result = parseFullNewSubexpressionPrime(first);
             break;
             
         case LEFTPAREN_TOKEN:
             first = parsePrimaryExpressionOrExpressionQualifiedIdentifier();
             result = parseFullNewSubexpressionPrime(first);
             break;
             
         case PUBLIC_TOKEN: case PRIVATE_TOKEN: case PROTECTED_TOKEN: case DEFAULT_TOKEN:
         case GET_TOKEN: case SET_TOKEN: case NAMESPACE_TOKEN: case MULT_TOKEN:
         case IDENTIFIER_TOKEN: case AMPERSAND_TOKEN:
             first = nodeFactory.memberExpression(null, nodeFactory.getExpression(parseQualifiedIdentifier()));
             result = parseFullNewSubexpressionPrime(first);
             break;
             
         default:
             first = parsePrimaryExpression();
             result = parseFullNewSubexpressionPrime(first);      
         }
 
         if (debug)
         {
             System.err.println("finish parseFullNewSubexpression");
         }
 
         return result;
     }
 
     /*
      * FullNewSubexpressionPrime
      *     PropertyOperator FullNewSubexpressionPrime
      *     empty
      */
 
     private Node parseFullNewSubexpressionPrime(Node first)
     {
 
         if (debug)
         {
             System.err.println("begin parseFullNewSubexpressionPrime");
         }
 
         Node result;
         final int lt = lookahead();
 
         if (lt==DOT_TOKEN || lt==LEFTBRACKET_TOKEN || lt==DOTLESSTHAN_TOKEN ||
             (HAS_DESCENDOPERATORS && lt==DOUBLEDOT_TOKEN) || lt==AMPERSAND_TOKEN )
         {
             first = parsePropertyOperator(first);
             result = parseFullNewSubexpressionPrime(first);
         }
         else
         {
 /*
             MemberExpressionNode memb = first instanceof MemberExpressionNode ? (MemberExpressionNode) first : null;
             if( memb != null && memb.selector.is_package )
             {
                 IdentifierNode ident = memb.selector.expr instanceof IdentifierNode ? (IdentifierNode) memb.selector.expr : null;
                 if( ident != null )
                 {
                     ctx.error(memb.selector.expr.pos(),kError_IllegalPackageReference, ident.name);
                 }
             }
 */
             result = first;
         }
 
         if (debug)
         {
             System.err.println("finish parseFullNewSubexpressionPrime");
         }
 
         return result;
     }
 
     /*
      * ShortNewExpression
      *     new ShortNewSubexpression
      */
 
     private Node parseShortNewExpression()
     {
 
         if (debug)
         {
             System.err.println("begin parseShortNewExpression");
         }
 
         Node result;
 
         match(NEW_TOKEN);
         result = parseShortNewSubexpression();
 
         if (debug)
         {
             System.err.println("finish parseShortNewExpression");
         }
 
         return result;
     }
 
     /*
      * ShortNewSubexpression
      *     FullNewSubexpression
      *     ShortNewExpression
      */
 
     private Node parseShortNewSubexpression()
     {
 
         if (debug)
         {
             System.err.println("begin parseShortNewSubexpression");
         }
 
         Node result;
 
         // Implement branch into ShortNewExpression
         if (lookahead()==NEW_TOKEN)
         {
             result = parseShortNewExpression();
             if (lookahead()==LEFTPAREN_TOKEN)
             {
                 result = parseArguments(result);
             }
             result = nodeFactory.newExpression(result);
         }
         else
         {
             result = parseFullNewSubexpression();
         }
 
         if (debug)
         {
             System.err.println("finish parseShortNewSubexpression");
         }
 
         return result;
     }
 
     /*
      * PropertyOperator
      *     . QualifiedIdentifier
      *     .. QualifiedIdentifier
      *     . ( ListExpressionAllowIn )
      *     Brackets
      *
      *     TypeApplication
      *         .<  TypeExpressionList  >
      */
 
     private Node parsePropertyOperator(Node first)
     {
 
         if (debug)
         {
             System.err.println("begin parsePropertyOperator");
         }
 
         Node result;
         final int lt = lookahead();
         
         if (lt==DOT_TOKEN)
         {
             shift();
             if (lookahead()==LEFTPAREN_TOKEN)
             {
                 scanner.enterSlashRegExpContext();
                 shift();
                 result = nodeFactory.filterOperator(first,parseListExpression(allowIn_mode),first.pos());
                 match(RIGHTPAREN_TOKEN);
                 scanner.exitSlashRegExpContext();
             }
             else
             {
                 result = nodeFactory.memberExpression(first, nodeFactory.getExpression(parseQualifiedIdentifier()));
             }
         }
         else if (lt==LEFTBRACKET_TOKEN)
         {
             result = parseBrackets(first);
         }
         else if (lt==DOUBLEDOT_TOKEN)
         {
             shift();
             SelectorNode selector = nodeFactory.getExpression(parseQualifiedIdentifier());
             selector.setMode(DOUBLEDOT_TOKEN);
             result = nodeFactory.memberExpression(first, selector);
         }
         else if (lt==DOTLESSTHAN_TOKEN)
         {
             shift();
             result = nodeFactory.applyTypeExpr(first, parseTypeExpressionList(),ctx.input.positionOfMark());
             if(lookahead()==UNSIGNEDRIGHTSHIFT_TOKEN)
             {
                 // Transform >>> to >> and eat one >
                 changeLookahead(RIGHTSHIFT_TOKEN);
             }
             else if(lookahead()==RIGHTSHIFT_TOKEN)
             {
                 // Transform >> to > and eat one >
                 changeLookahead(GREATERTHAN_TOKEN);
             }
             else
             {
                 match(GREATERTHAN_TOKEN);
             }
         }
         else
         {
             // C: this else-part not in the C++ code...
             result = null;
         }
 
         if (debug)
         {
             System.err.println("finish parsePropertyOperator");
         }
 
         return result;
     }
 
     /*
      * Brackets
      *     [ ListExpressionallowIn ]
      *     [ ExpressionsWithRest ]
      */
 
     private MemberExpressionNode parseBrackets(Node first)
     {
 
         if (debug)
         {
             System.err.println("begin parseBrackets");
         }
 
         MemberExpressionNode result;
         ArgumentListNode second;
 
         match(LEFTBRACKET_TOKEN);
 
         if (lookahead()==RIGHTBRACKET_TOKEN)
         {
             error(kError_Lexical_SyntaxError);
             second = null;
         }
         else
         {
             second = parseArgumentsWithRest(allowIn_mode);
         }
 
         match(RIGHTBRACKET_TOKEN);
         result = nodeFactory.indexedMemberExpression(first, nodeFactory.getExpression(second));
         
         if (debug)
         {
             System.err.println("finish parseBrackets");
         }
 
         return result;
     }
 
     /*
      * Arguments
      *     ( )
      *     ( ListExpressionallowIn )
      *     ( NamedArgumentList )
      */
 
     private Node parseArguments(Node first)
     {
 
         if (debug)
         {
             System.err.println("begin parseArguments");
         }
 
         Node result;
         ArgumentListNode second;
 
         scanner.enterSlashRegExpContext();
         match(LEFTPAREN_TOKEN);
 
         second = (lookahead()==RIGHTPAREN_TOKEN)
             ? null
             : parseArgumentsWithRest(allowIn_mode);
 
         match(RIGHTPAREN_TOKEN);
         scanner.exitSlashRegExpContext();
         result = nodeFactory.callExpression(first, second);
 
         if (debug)
         {
             System.err.println("finish parseArguments");
         }
 
         return result;
     }
 
     /*
      *  ArgumentsWithRest
      *      RestExpression
      *      AssignmentExpression ListExpressionWithRestPrime
      */
 
     private ArgumentListNode parseArgumentsWithRest(int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseArgumentsWithRest");
         }
 
         ArgumentListNode result;
         Node first;
 
         first = (lookahead()==TRIPLEDOT_TOKEN)
             ? parseRestExpression()
             : parseAssignmentExpression(mode);
 
         result = parseArgumentsWithRestPrime(mode, nodeFactory.argumentList(null, first));
 
         if (debug)
         {
             System.err.println("finish parseArgumentsWithRest with " + ((result != null) ? result.toString() : ""));
         }
 
         return result;
     }
 
     /*
         *  ArgumentsWithRestPrime
         *      empty
         *      , RestExpression
         *      , AssignmentExpression ListExpressionPrime
         */
 
     private ArgumentListNode parseArgumentsWithRestPrime(int mode, ArgumentListNode first)
     {
 
         if (debug)
         {
             System.err.println("begin parseArgumentsWithRestPrime");
         }
 
         ArgumentListNode result;
 
         // , RestExpression
         // , AssignmentExpression ListExpressionWithRest
 
         if (lookahead()==COMMA_TOKEN)
         {
             shift();
 
             Node second;
 
             if (lookahead()==TRIPLEDOT_TOKEN)
             {
                 result = nodeFactory.argumentList(first, parseRestExpression());
             }
             else
             {
                 second = parseAssignmentExpression(mode);
                 result = parseArgumentsWithRestPrime(mode, nodeFactory.argumentList(first, second));
             }
         }
         else
         {
             result = first;
         }
 
         if (debug)
         {
             System.err.println("finish parseArgumentsWithRestPrime with " + ((result != null) ? result.toString() : ""));
         }
 
         return result;
     }
 
     /*
      * RestExpression
      *     ... AssignmentExpression[allowIn]
      */
 
     private Node parseRestExpression()
     {
 
         if (debug)
         {
             System.err.println("begin parseRestExpression");
         }
 
         Node result;
 
         match(TRIPLEDOT_TOKEN);
 
         result = nodeFactory.restExpression(parseAssignmentExpression(allowIn_mode));
 
         if (debug)
         {
             System.err.println("finish parseRestExpression");
         }
 
         return result;
     }
 
     /*
      * UnaryExpression
      *     PostfixExpression
      *     delete PostfixExpression
      *     void UnaryExpression
      *     typeof UnaryExpression
      *     ++ PostfixExpression
      *     -- PostfixExpression
      *     + UnaryExpression
      *     - UnaryExpression
      *     - NegatedMinLong
      *     ~ UnaryExpression
      *     ! UnaryExpression
      */
 
     private Node parseUnaryExpression()
     {
 
         if (debug)
         {
             System.err.println("begin parseUnaryExpression");
         }
 
         Node result = null;
         int pos;
         final int lt = lookahead();
         
         switch ( lt )
         {
         case DELETE_TOKEN:
             shift();
             lookaheadInSlashRegExpContext();
             result = nodeFactory.unaryExpression(lt, parsePostfixExpression(),scanner.input.positionOfMark());
             break;
                    
         case TYPEOF_TOKEN:
         case PLUSPLUS_TOKEN:
         case MINUSMINUS_TOKEN:
         case PLUS_TOKEN: 
         case BITWISENOT_TOKEN:
         case NOT_TOKEN:    
             shift();
             lookaheadInSlashRegExpContext();
             result = nodeFactory.unaryExpression(lt, parseUnaryExpression(), scanner.input.positionOfMark());
             break;
             
         case VOID_TOKEN:
             shift();
             lookaheadInSlashRegExpContext();
             pos = scanner.input.positionOfMark();
             result = ( lookahead()==COMMA_TOKEN || lookahead()==SEMICOLON_TOKEN || lookahead()==RIGHTPAREN_TOKEN )
                 ? nodeFactory.unaryExpression(lt, nodeFactory.literalNumber("0",pos),pos)
                 : nodeFactory.unaryExpression(lt, parseUnaryExpression(), pos);
             break;
  
         case MINUS_TOKEN:
             shift();
             lookaheadInSlashRegExpContext();
             result = (lookahead()==NEGMINLONGLITERAL_TOKEN)
                 ? nodeFactory.unaryExpression(lt,nodeFactory.literalNumber(scanner.getTokenText(match(NEGMINLONGLITERAL_TOKEN)),ctx.input.positionOfMark()),scanner.input.positionOfMark())
                 : nodeFactory.unaryExpression(lt, parseUnaryExpression(), scanner.input.positionOfMark());
             break;
 
         default:
         	result = parsePostfixExpression();
         }
 
         if (debug)
         {
             System.err.println("finish parseUnaryExpression");
         }
 
         return result;
     }
 
     /*
      * MultiplicativeExpression
      */
 
     private Node parseMultiplicativeExpression()
     {
 
         if (debug)
         {
             System.err.println("begin parseMultiplicativeExpression");
         }
 
         Node result;
         Node first = null;
 
         lookahead(); // force lookahead read in current context
         scanner.enterSlashDivContext();
         first = parseUnaryExpression();
         scanner.exitSlashDivContext();
         
         result = parseMultiplicativeExpressionPrime(first);
 
         if (debug)
         {
             System.err.println("finish parseMultiplicativeExpression");
         }
 
         return result;
     }
 
     private Node parseMultiplicativeExpressionPrime(Node first)
     {
 
         if (debug)
         {
             System.err.println("begin parseMultiplicativeExpressionPrime");
         }
 
         Node result;
         final int lt = lookahead();
         
         switch ( lt )
         {
         case MULT_TOKEN: case DIV_TOKEN: case MODULUS_TOKEN:
             shift();
             lookahead(); // force lookahead read in current context
             scanner.enterSlashDivContext();
             first = nodeFactory.binaryExpression(lt, first, parseUnaryExpression());
             scanner.exitSlashDivContext();
             result = parseMultiplicativeExpressionPrime(first);
             break;
             
             default:
                 result = first;
         }
  
         if (debug)
         {
             System.err.println("finish parseMultiplicativeExpressionPrime");
         }
 
         return result;
     }
 
     /*
      * AdditiveExpression
      *     MultiplicativeExpression AdditiveExpressionPrime
      *
      * AdditiveExpressionPrime
      *     + MultiplicativeExpression AdditiveExpressionPrime
      *     - MultiplicativeExpression AdditiveExpressionPrime
      *     empty
      */
 
     private Node parseAdditiveExpression()
     {
 
         if (debug)
         {
             System.err.println("begin parseAdditiveExpression");
         }
 
         Node first = parseMultiplicativeExpression();;
         Node result = parseAdditiveExpressionPrime(first);
 
         if (debug)
         {
             System.err.println("finish parseAdditiveExpression");
         }
 
         return result;
     }
 
     private Node parseAdditiveExpressionPrime(Node first)
     {
 
         if (debug)
         {
             System.err.println("begin parseAdditiveExpressionPrime");
         }
 
         Node result;
         final int lt = lookahead();
 
         if (lt == PLUS_TOKEN || lt == MINUS_TOKEN )
         {
             shift();
             first = nodeFactory.binaryExpression(lt, first, parseMultiplicativeExpression());
             result = parseAdditiveExpressionPrime(first);
         }
         else
         {
             result = first;
         }
 
         if (debug)
         {
             System.err.println("finish parseAdditiveExpressionPrime");
         }
 
         return result;
     }
 
     /*
      * ShiftExpression
      *     AdditiveExpression ShiftExpressionPrime
      *
      * ShiftExpressionPrime
      *     << AdditiveExpression ShiftExpressionPrime
      *     >> AdditiveExpression ShiftExpressionPrime
      *     >>> AdditiveExpression ShiftExpressionPrime
      *     empty
      */
 
     private Node parseShiftExpression()
     {
 
         if (debug)
         {
             System.err.println("begin parseShiftExpression");
         }
 
         Node result;
         Node first;
 
         first = parseAdditiveExpression();
         result = parseShiftExpressionPrime(first);
 
         if (debug)
         {
             System.err.println("finish parseShiftExpression");
         }
 
         return result;
     }
 
     private Node parseShiftExpressionPrime(Node first)
     {
 
         if (debug)
         {
             System.err.println("begin parseShiftExpressionPrime");
         }
 
         Node result;
         final int lt = lookahead();
 
         switch ( lt )
         {
         case LEFTSHIFT_TOKEN: case RIGHTSHIFT_TOKEN: case UNSIGNEDRIGHTSHIFT_TOKEN:
             shift();
             first = nodeFactory.binaryExpression(lt, first, parseAdditiveExpression());
             result = parseShiftExpressionPrime(first);
             break;
             
         default:    
             result = first;
         }
 
         if (debug)
         {
             System.err.println("finish parseShiftExpressionPrime");
         }
 
         return result;
     }
 
     /*
      * RelationalExpression
      *     ShiftExpression RelationalExpressionPrime
      *
      * RelationalExpressionPrime
      *     < ShiftExpression RelationalExpressionPrime
      *     > ShiftExpression RelationalExpressionPrime
      *     <= ShiftExpression RelationalExpressionPrime
      *     >= ShiftExpression RelationalExpressionPrime
      *     instanceof ShiftExpression RelationalExpressionPrime
      *     as ShiftExpression RelationalExpressionPrime
      *     in ShiftExpression RelationalExpressionPrime
      *     empty
      */
 
     private Node parseRelationalExpression(int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseRelationalExpression");
         }
 
         Node result;
         Node first;
 
         first = parseShiftExpression();
         result = parseRelationalExpressionPrime(mode, first);
 
         if (debug)
         {
             System.err.println("finish parseRelationalExpression");
         }
 
         return result;
     }
 
     private Node parseRelationalExpressionPrime(int mode, Node first)
     {
 
         if (debug)
         {
             System.err.println("begin parseRelationalExpressionPrime");
         }
 
         Node result = first;
         final int lt = lookahead();
 
         switch ( lt )
         {
         case IN_TOKEN:
             if ( mode != allowIn_mode )
                 break;
             //fall thru
         case LESSTHAN_TOKEN: case GREATERTHAN_TOKEN: case LESSTHANOREQUALS_TOKEN: case GREATERTHANOREQUALS_TOKEN:
         case IS_TOKEN: case AS_TOKEN: case INSTANCEOF_TOKEN:
             shift();
             first = nodeFactory.binaryExpression(lt, first, parseShiftExpression());
             result = parseRelationalExpressionPrime(mode, first);      
         }
   
         if (debug)
         {
             System.err.println("finish parseRelationalExpressionPrime");
         }
 
         return result;
     }
 
     /*
      * EqualityExpression(mode)
      *     RelationalExpression(mode)
      *     EqualityExpression(mode) == RelationalExpression(mode)
      *     EqualityExpression(mode) != RelationalExpression(mode)
      *     EqualityExpression(mode) === RelationalExpression(mode)
      *     EqualityExpression(mode) !== RelationalExpression(mode)
      */
 
     private Node parseEqualityExpression(int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseEqualityExpression");
         }
 
         Node first = parseRelationalExpression(mode);
         Node result = parseEqualityExpressionPrime(mode, first);
 
         if (debug)
         {
             System.err.println("finish parseEqualityExpression with " + ((result != null) ? result.toString() : ""));
         }
 
         return result;
     }
 
     private Node parseEqualityExpressionPrime(int mode, Node first)
     {
 
         if (debug)
         {
             System.err.println("begin parseEqualityExpressionPrime");
         }
 
         Node result = first;
         final int lt = lookahead();
         
         switch ( lt )
         {
         case STRICTNOTEQUALS_TOKEN:
             if (HAS_STRICTNOTEQUALS==false)
                 break; // else fallthru 
         case EQUALS_TOKEN: case NOTEQUALS_TOKEN: case STRICTEQUALS_TOKEN:
             shift();
             first = nodeFactory.binaryExpression(lt, first, parseRelationalExpression(mode));
             result = parseEqualityExpressionPrime(mode, first);
         }
 
         if (debug)
         {
             System.err.println("finish parseEqualityExpressionPrime with " + ((result != null) ? result.toString() : ""));
         }
 
         return result;
     }
 
     /*
      * BitwiseAndExpression(mode)
      *     EqualityExpression(mode)
      *     BitwiseAndExpression(mode) & EqualityExpression(mode)
      */
 
     private Node parseBitwiseAndExpression(int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseBitwiseAndExpression");
         }
 
         Node first = parseEqualityExpression(mode);
         Node result = parseBitwiseAndExpressionPrime(mode, first);
 
         if (debug)
         {
             System.err.println("finish parseBitwiseAndExpression");
         }
 
         return result;
     }
 
     private Node parseBitwiseAndExpressionPrime(int mode, Node first)
     {
 
         if (debug)
         {
             System.err.println("begin parseBitwiseAndExpressionPrime");
         }
 
         Node result;
         final int lt = lookahead();
 
         if (lt==BITWISEAND_TOKEN)
         {
             shift();
             first = nodeFactory.binaryExpression(lt, first, parseEqualityExpression(mode));
             result = parseBitwiseAndExpressionPrime(mode, first);
         }
         else
         {
             result = first;
         }
 
         if (debug)
         {
             System.err.println("finish parseBitwiseAndExpressionPrime with " + ((result != null) ? result.toString() : ""));
         }
 
         return result;
     }
 
     /*
      * BitwiseXorExpression(mode)
      *     BitwiseAndExpression(mode)
      *     BitwiseXorExpression(mode) ^ BitwiseAndExpression(mode)
      */
 
     private Node parseBitwiseXorExpression(int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseBitwiseXorExpression");
         }
 
         Node first = parseBitwiseAndExpression(mode);
         Node result = parseBitwiseXorExpressionPrime(mode, first);
 
         if (debug)
         {
             System.err.println("finish parseBitwiseXorExpression");
         }
 
         return result;
     }
 
     private Node parseBitwiseXorExpressionPrime(int mode, Node first)
     {
 
         if (debug)
         {
             System.err.println("begin parseBitwiseXorExpressionPrime");
         }
 
         Node result;
 
         if (lookahead()==BITWISEXOR_TOKEN)
         {
             shift();
             first = nodeFactory.binaryExpression(BITWISEXOR_TOKEN, first, parseBitwiseAndExpression(mode));
             result = parseBitwiseXorExpressionPrime(mode, first);
         }
         else
         {
             result = first;
         }
 
         if (debug)
         {
             System.err.println("finish parseBitwiseXorExpressionPrime with " + ((result != null) ? result.toString() : ""));
         }
 
         return result;
     }
 
     /*
      * BitwiseOrExpression(mode)
      *     BitwiseXorExpression(mode)
      *     BitwiseOrExpression(mode) | BitwiseXorExpression(mode)
      */
 
     private Node parseBitwiseOrExpression(int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseBitwiseOrExpression");
         }
 
         Node first = parseBitwiseXorExpression(mode);
         Node result = parseBitwiseOrExpressionPrime(mode, first);
 
         if (debug)
         {
             System.err.println("finish parseBitwiseOrExpression");
         }
 
         return result;
     }
 
     private Node parseBitwiseOrExpressionPrime(int mode, Node first)
     {
 
         if (debug)
         {
             System.err.println("begin parseBitwiseOrExpressionPrime");
         }
 
         Node result;
 
         if (lookahead()==BITWISEOR_TOKEN)
         {
             shift();
             first = nodeFactory.binaryExpression(BITWISEOR_TOKEN, first, parseBitwiseXorExpression(mode));
             result = parseBitwiseOrExpressionPrime(mode, first);
         }
         else
         {
             result = first;
         }
 
         if (debug)
         {
             System.err.println("finish parseBitwiseOrExpressionPrime with " + ((result != null) ? result.toString() : ""));
         }
 
         return result;
     }
 
     /*
      * LogicalAndExpression
      *     BitwiseOrExpression
      *     LogicalAndExpression && BitwiseOrExpression
      */
 
     private Node parseLogicalAndExpression(int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseLogicalAndExpression");
         }
 
         Node first = parseBitwiseOrExpression(mode);
         Node result = parseLogicalAndExpressionPrime(mode, first);
 
         if (debug)
         {
             System.err.println("finish parseLogicalAndExpression");
         }
 
         return result;
     }
 
     private Node parseLogicalAndExpressionPrime(int mode, Node first)
     {
 
         if (debug)
         {
             System.err.println("begin parseLogicalAndExpressionPrime");
         }
 
         Node result = first;
 
         if (lookahead()==LOGICALAND_TOKEN)
         {
             shift();
             first = nodeFactory.binaryExpression(LOGICALAND_TOKEN, first, parseBitwiseOrExpression(mode));
             result = parseLogicalAndExpressionPrime(mode, first);
         }
 
         if (debug)
         {
             System.err.println("finish parseLogicalAndExpressionPrime with " + ((result != null) ? result.toString() : ""));
         }
 
         return result;
     }
 
     /*
      * LogicalXorExpression
      *     LogicalAndExpression
      *     LogicalXorExpression ^^ LogicalAndExpression
      */
 
     private Node parseLogicalXorExpression(int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseLogicalXorExpression");
         }
 
         Node first = parseLogicalAndExpression(mode);
         Node result = parseLogicalXorExpressionPrime(mode, first);
 
         if (debug)
         {
             System.err.println("finish parseLogicalXorExpression with " + ((result != null) ? result.toString() : ""));
         }
 
         return result;
     }
 
     private Node parseLogicalXorExpressionPrime(int mode, Node first)
     {
 
         if (debug)
         {
             System.err.println("begin parseLogicalXorExpressionPrime");
         }
 
         Node result = first;
 
         if (lookahead()==LOGICALXOR_TOKEN)
         {
             shift();
             first = nodeFactory.binaryExpression(LOGICALXOR_TOKEN, first, parseLogicalAndExpression(mode));
             result = parseLogicalXorExpressionPrime(mode, first);
         }
 
         if (debug)
         {
             System.err.println("finish parseLogicalXorExpressionPrime with " + ((result != null) ? result.toString() : ""));
         }
 
         return result;
     }
 
     /*
      * LogicalOrExpression
      *     LogicalXorExpression
      *     LogicalOrExpression || LogicalXorExpression
      */
 
     private Node parseLogicalOrExpression(int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseLogicalOrExpression");
         }
 
         Node first = parseLogicalXorExpression(mode);
         Node result = parseLogicalOrExpressionPrime(mode, first);
 
         if (debug)
         {
             System.err.println("finish parseLogicalOrExpression with " + ((result != null) ? result.toString() : ""));
         }
 
         return result;
     }
 
     private Node parseLogicalOrExpressionPrime(int mode, Node first)
     {
 
         if (debug)
         {
             System.err.println("begin parseLogicalOrExpressionPrime");
         }
 
         Node result = first;
 
         if (lookahead()==LOGICALOR_TOKEN)
         {
             shift();
             first = nodeFactory.binaryExpression(LOGICALOR_TOKEN, first, parseLogicalXorExpression(mode));
             result = parseLogicalOrExpressionPrime(mode, first);
         }
 
         if (debug)
         {
             System.err.println("finish parseLogicalOrExpressionPrime");
         }
 
         return result;
     }
 
     /*
      * ConditionalExpression
      *     LogicalOrExpression
      *     LogicalOrExpression ? AssignmentExpression : AssignmentExpression
      */
 
     private Node parseConditionalExpression(int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseConditionalExpression");
         }
 
         Node result;
         Node first;
 
         first = parseLogicalOrExpression(mode);
 
         if (lookahead()==QUESTIONMARK_TOKEN)
         {
             shift();
             
             Node second;
             Node third;
             second = parseAssignmentExpression(mode);
             match(COLON_TOKEN);
             third = parseAssignmentExpression(mode);
             result = nodeFactory.conditionalExpression(first, second, third);
         }
         else
         {
             result = first;
         }
 
         if (debug)
         {
             System.err.println("finish parseConditionalExpression with " + ((result != null) ? result.toString() : ""));
         }
 
         return result;
     }
 
     /*
      * NonAssignmentExpression
      *     LogicalOrExpression
      *     LogicalOrExpression ? NonAssignmentExpression : NonAssignmentExpression
      */
 
     private Node parseNonAssignmentExpression(int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseNonAssignmentExpression");
         }
 
         Node result;
         Node first;
 
         first = parseLogicalOrExpression(mode);
 
         if (lookahead()==QUESTIONMARK_TOKEN)
         {
             shift();
             
             Node second;
             Node third;
             second = parseNonAssignmentExpression(mode);
             match(COLON_TOKEN);
             third = parseNonAssignmentExpression(mode);
             result = nodeFactory.conditionalExpression(first, second, third);
         }
         else
         {
             result = first;
         }
 
 
         if (debug)
         {
             System.err.println("finish parseNonAssignmentExpression with " + ((result != null) ? result.toString() : ""));
         }
 
         return result;
     }
 
     /*
      * AssignmentExpression(mode)
      *     ConditionalExpression
      *     SuperExpression CompoundAssignmentExpressionSuffix(mode)
      *     PostfixExpression AssignmentExpressionSuffix(mode)
      *
      * AssignmentExpressionSuffix(mode)
      *       CompoundAssignmentExpressionSuffix(mode)
      *       LogicalAssignment AssignmentExpression(mode)
      *       = AssigmentExpression(mode)
      *
      * CompoundAssignmentExpressionSuffix(mode)
      *     CompoundAssignment SuperExpression
      *     CompoundAssignment AssignmentExpression(mode)
      */
 
     private Node parseAssignmentExpression(int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseAssignmentExpression");
         }
 
         Node first = parseConditionalExpression(mode);
         Node result = parseAssignmentExpressionSuffix(mode, first);
 
         if (debug)
         {
             System.err.println("finish parseAssignmentExpression with " + ((result != null) ? result.toString() : ""));
         }
 
         return result;
     }
 
     private Node parseAssignmentExpressionSuffix(int mode, Node first)
     {
 
         if (debug)
         {
             System.err.println("begin parseAssignmentExpressionSuffix");
         }
 
         Node result;
         final int lt = lookahead();
         
         switch ( lt )
         {
         case LOGICALANDASSIGN_TOKEN: case LOGICALXORASSIGN_TOKEN: case LOGICALORASSIGN_TOKEN:
             if (HAS_LOGICALASSIGNMENT==false)
             {
                 result = first;
                 break;
             }
         case ASSIGN_TOKEN: case MULTASSIGN_TOKEN: case DIVASSIGN_TOKEN: 
         case MODULUSASSIGN_TOKEN: case PLUSASSIGN_TOKEN: case MINUSASSIGN_TOKEN: 
         case LEFTSHIFTASSIGN_TOKEN: case RIGHTSHIFTASSIGN_TOKEN: case UNSIGNEDRIGHTSHIFTASSIGN_TOKEN: 
         case BITWISEANDASSIGN_TOKEN: case BITWISEXORASSIGN_TOKEN: case BITWISEORASSIGN_TOKEN:
             shift();
             
             Node third = parseAssignmentExpression(mode);
             result = nodeFactory.assignmentExpression(first, lt, third);
             break;
             
             default:
                 result = first;
         }
       
         if (debug)
         {
             System.err.println("finish parseAssignmentExpressionSuffix with " + ((result != null) ? result.toString() : ""));
         }
 
         return result;
     }
 
     /*
      * ListExpression
      *     AssignmentExpression
      *     ListExpression , AssignmentExpression
      */
 
     private ListNode parseListExpression(int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseListExpression");
         }
 
         Node first = parseAssignmentExpression(mode);
         ListNode result = parseListExpressionPrime(mode, nodeFactory.list(null, first));
 
         if (debug)
         {
             System.err.println("finish parseListExpression with " + ((result != null) ? result.toString() : ""));
         }
 
         return result;
     }
 
     private ListNode parseListExpressionPrime(int mode, ListNode first)
     {
 
         if (debug)
         {
             System.err.println("begin parseListExpressionPrime");
         }
 
         ListNode result;
 
         // , AssignmentExpression ListExpressionPrime
 
         if (lookahead()==COMMA_TOKEN)
         {
             shift();
 
             Node second;
 
             second = parseAssignmentExpression(mode);
             result = parseListExpressionPrime(mode, nodeFactory.list(first, second));
         }
         else
         {
             result = first;
         }
 
         if (debug)
         {
             System.err.println("finish parseListExpression with " + ((result != null) ? result.toString() : ""));
         }
 
         return result;
     }
 
     private IdentifierNode parseNonAttributeQualifiedExpression()
     {
         if (debug)
         {
             System.err.println("begin parseNonAttributeQualifiedExpression");
         }
         
         IdentifierNode result = (lookahead()==LEFTPAREN_TOKEN)
             ? parseExpressionQualifiedIdentifier()
             : parseSimpleQualifiedIdentifier();
 
         if (debug)
         {
             System.err.println("finish parseNonAttributeQualifiedExpression");
         }
 
         return result;
     }
 
     private Node parseTypeName()
     {
         Node first = parseTypeIdentifier();
         return first;//nodeFactory.memberExpression(null, nodeFactory.getExpression(first, first.pos()),first.pos());
     }
 
     private ListNode parseTypeNameList()
     {
         ListNode type_list = null;
 
         type_list = nodeFactory.list(type_list, parseTypeName());
         while( lookahead()==COMMA_TOKEN )
         {
             shift();
             type_list = nodeFactory.list(type_list, parseTypeName());
         }
 
         return type_list;
     }
 
     private Node parseSimpleTypeIdentifier()
     {
         if (debug)
         {
             System.err.println("begin parseSimpleTypeIdentifier");
         }
 
         Node result;
         Node first;
         // TODO: handle packageidentifier.identifier
 //        if( lookahead(PACKAGEIDENTIFIER_TOKEN))
         {
 
         }
 //        else
         {
             first = parseNonAttributeQualifiedExpression();
             result = nodeFactory.memberExpression(null, nodeFactory.getExpression(first, first.pos()),first.pos());
 
             while( lookahead()==DOT_TOKEN )
             {
                 // TODO: this should only be for real package identifiers
                 shift();
                 result = nodeFactory.memberExpression(result, nodeFactory.getExpression(parseNonAttributeQualifiedExpression()));
             }
         }
         if (debug)
         {
             System.err.println("finish parseSimpleTypeIdentifier");
         }
 
         return result;
     }
 
     private Node parseTypeIdentifier()
     {
         if (debug)
         {
             System.err.println("begin parseTypeIdentifier");
         }
 
         Node result;
         Node first = parseSimpleTypeIdentifier();
 
         // TODO: parse <...> type expression list
         if(lookahead()==DOTLESSTHAN_TOKEN)
         {
             shift();
             result = nodeFactory.applyTypeExpr(first, parseTypeExpressionList(),ctx.input.positionOfMark());
             if( lookahead(UNSIGNEDRIGHTSHIFT_TOKEN) )
             {
                 // Transform >>> to >> and eat one >
                 changeLookahead(RIGHTSHIFT_TOKEN);
             }
             else if(lookahead()==RIGHTSHIFT_TOKEN)
             {
                 // Transform >> to > and eat one >
                 changeLookahead(GREATERTHAN_TOKEN);
             }
             else
             {
                 match(GREATERTHAN_TOKEN);
             }
         }
 
         result = first;  // ??? was this meant? - clobbers result
 
         if (debug)
         {
             System.err.println("finish parseTypeIdentifier");
         }
 
         return result;
     }
     /*
      * TypeExpression
      *     NonassignmentExpression
      */
 
     private Node parseTypeExpression(int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseTypeExpression");
         }
 
         Node result;
 
         if( ctx.dialect(7) && nextToken == IDENTIFIER_TOKEN && scanner.getTokenText(nextToken).equals("Object") )
         {
             match(IDENTIFIER_TOKEN);
             result = null;  // means *
         }
         else
         {
             Node first = parseTypeIdentifier();
             boolean is_nullable = true;
             boolean is_explicit = false;
             final int lt = lookahead();
             
             if(lt==NOT_TOKEN)
             {
                 shift();
                 is_nullable = false;
                 is_explicit = true;
             }
             else if(lt==QUESTIONMARK_TOKEN)
             {
                 shift();
                 is_nullable = true;
                 is_explicit = true;
             }
 
             result = nodeFactory.typeExpression(first, is_nullable, is_explicit, -1);
         }
 
         if (debug)
         {
             System.err.println("finish parseTypeExpression");
         }
 
         return result;
     }
 
     /*
      * Statementw
      *     SuperStatement Semicolonw
      *     Block
      *     IfStatementw
      *     SwitchStatement
      *     DoStatement Semicolonw
      *     WhileStatementw
      *     ForStatementw
      *     WithStatementw
      *     ContinueStatement Semicolonw
      *     BreakStatement Semicolonw
      *     ReturnStatement Semicolonw
      *     ThrowStatement Semicolonw
      *     TryStatement
      *     LabeledStatementw
      *     ExpressionStatement Semicolonw
      */
 
     private Node parseStatement(int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseStatement");
         }
 
         Node result;
         final int lt = lookahead();
         
         switch(lt)
         {
         case SUPER_TOKEN:
             result = parseSuperStatement(mode);
             // Moved into parseSuperStatement to allow for AS3 super expressions:
             //     matchSemicolon(mode);
         	break;
         	
         case LEFTBRACE_TOKEN:
             StatementListNode sln = parseBlock();
             result = sln;
         	break;
         	
         case LEFTBRACKET_TOKEN:
         case XMLLITERAL_TOKEN:
         case DOCCOMMENT_TOKEN:
         	result = parseMetaData();
         	break;
         	
         case IF_TOKEN:
         	result = parseIfStatement(mode);
         	break;
         		
         case SWITCH_TOKEN:
             result = parseSwitchStatement();
             break;
                 
         case DO_TOKEN:
         	result = parseDoStatement();
         	matchSemicolon(mode);
         	break;
                 
         case WHILE_TOKEN:
         	result = parseWhileStatement(mode);
         	break;
                 
         case FOR_TOKEN:
         	result = parseForStatement(mode);
         	break;
                 
         case WITH_TOKEN:
         	result = parseWithStatement(mode);
         	break;
         	
         case CONTINUE_TOKEN:
         	result = parseContinueStatement();
             matchSemicolon(mode);
             break;
             
         case BREAK_TOKEN:
         	result = parseBreakStatement();
             matchSemicolon(mode);
             break;
             
         case RETURN_TOKEN:
         	result = parseReturnStatement();
             matchSemicolon(mode);
             break;
             
         case THROW_TOKEN:
             result = parseThrowStatement();
             matchSemicolon(mode);
             break;
             
         case TRY_TOKEN:
             result = parseTryStatement();
             break;
             
         default:
             result = parseLabeledOrExpressionStatement(mode);
             if (!result.isLabeledStatement())
             {
             	matchSemicolon(mode);
             }
         }
 
         if (debug)
         {
             System.err.println("finish parseStatement");
         }
 
         return result;
     }
 
     /*
      * Substatementw
      *     EmptyStatement
      *     Statementw
      *     SimpleVariableDefinition Semicolonw
      *     Attributes [no line break] { Substatements }
      */
 
     private Node parseSubstatement(int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseSubstatement");
         }
 
         Node result;
         int lt = lookahead();
         
         if (lt==SEMICOLON_TOKEN)
         {
             shift();
             result = nodeFactory.emptyStatement();
         }
         else if (lt==VAR_TOKEN)
         {
             result = parseVariableDefinition(null/*attrs*/, mode);
             matchSemicolon(mode);
         }
         else
         {
             result = parseAnnotatedSubstatementsOrStatement(mode);
         }
 
         if (debug)
         {
             System.err.println("finish parseSubstatement");
         }
 
         return result;
     }
 
 
 
     /*
      * SuperStatement
      *     super Arguments
      */
 
     private Node parseSuperStatement(int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseSuperStatement");
         }
 
         Node result;
         Node first;
 
         match(SUPER_TOKEN);
         first = nodeFactory.superExpression(null,ctx.input.positionOfMark() );
 
         if (lookahead()==LEFTPAREN_TOKEN)  // super statement
         {
             Node n = parseArguments(first);
             CallExpressionNode call = (n instanceof CallExpressionNode) ? (CallExpressionNode) n : null;  // this returns a call expression node
             if (call == null)
             {
                 ctx.internalError("Internal error in parseSuperStatement()");
             }
             if (lookaheadSemicolon(mode))
             {
                 result = nodeFactory.superStatement(call);
                 matchSemicolon(mode);
             }
             else // super expression
             {
                 if (!(call != null && call.args != null && call.args.size() == 1))
                 {
                     error(kError_Parser_WrongNumberOfSuperArgs);
                 }
                 else
                 {
                     SuperExpressionNode se = first instanceof SuperExpressionNode ? (SuperExpressionNode)first : null;
                     if( se != null )
                     {
                         se.expr = call.args.items.get(0);
                     }
                     else
                     {
                         ctx.internalError("internal error in super expression");
                     }
                 }
                 first = parseFullPostfixExpressionPrime(first);
                 result = nodeFactory.expressionStatement(parseListExpressionPrime(mode, nodeFactory.list(null, first)));
                 matchSemicolon(mode);
             }
         }
         else  // super expression
         {
             first = parseFullPostfixExpressionPrime(first);
             if (lookahead()==ASSIGN_TOKEN) // ISSUE: what about compound assignment?
             {
                 first = parseAssignmentExpressionSuffix(mode, first);
             }
             result = nodeFactory.expressionStatement(parseListExpressionPrime(mode, nodeFactory.list(null, first)));
             matchSemicolon(mode);
         }
 
         if (debug)
         {
             System.err.println("finish parseSuperStatement");
         }
 
         return result;
     }
 
     /*
      * LabeledOrExpressionStatement
      *     Identifier : Statement
      *     [lookahead?{function, {, const }] ListExpressionallowIn ;
      */
 
     private Node parseLabeledOrExpressionStatement(int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseLabeledOrExpressionStatement");
         }
 
         Node result;
 
         {
             ListNode first;
             first = parseListExpression(allowIn_mode);
             if (lookahead()==COLON_TOKEN)
             {
                 if (!first.isLabel())
                 {
                     error(kError_Parser_LabelIsNotIdentifier);
                 }
                 shift();
                 result = nodeFactory.labeledStatement(first, parseSubstatement(mode));
             }
             else
             {
                 result = nodeFactory.expressionStatement(first);
                 // leave matchSemicolon(mode) for caller
             }
         }
 
 
         if (debug)
         {
             System.err.println("finish parseLabeledOrExpressionStatement with " + ((result != null) ? result.toString() : ""));
         }
 
         return result;
     }
 
     /*
      * Block
      *     { Directives }
      */
     private StatementListNode parseBlock()
     {
         return parseBlock(null);
     }
 
     private StatementListNode parseBlock(AttributeListNode first)
     {
 
         if (debug)
         {
             System.err.println("begin parseBlock");
         }
 
         StatementListNode result;
 
         match(LEFTBRACE_TOKEN);
         result = parseDirectives(first, null/*stmts*/);
         match(RIGHTBRACE_TOKEN);
 
         if (debug)
         {
             System.err.println("finish parseBlock");
         }
 
         return result;
     }
 
     public MetaDataNode parseMetaData()
     {
         if (debug)
         {
             System.err.println("begin parseMetaData");
         }
 
         int pos = scanner.input.positionOfMark();
         MetaDataNode result;
 
         if( lookahead()==DOCCOMMENT_TOKEN)
         {
             ListNode list = nodeFactory.list(null,nodeFactory.literalString(
                     scanner.getTokenText(match(DOCCOMMENT_TOKEN)),ctx.input.positionOfMark()));
             LiteralXMLNode first = nodeFactory.literalXML(list,false/*is_xmllist*/,ctx.input.positionOfMark());
             Node x = nodeFactory.memberExpression(null,nodeFactory.getExpression(first),pos);
             result = nodeFactory.docComment(nodeFactory.literalArray(nodeFactory.argumentList(null,x),pos),pos);
         }
         else if(lookahead()==XMLLITERAL_TOKEN)
         {
             result = nodeFactory.metaData(nodeFactory.literalArray(
                                             nodeFactory.argumentList(null,parseXMLLiteral())),pos);
         }
         else
         {
             result = nodeFactory.metaData(parseArrayLiteral(),pos);
         }
 
         if (debug)
         {
             System.err.println("end parseMetaData");
         }
 
         return result;
     }
 
     /*
      * IfStatement
      *     if ParenExpressionNode Statement
      *     if ParenExpressionNode Statement else Statement
      */
 
     private Node parseIfStatement(int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseIfStatement");
         }
 
         Node result;
         ListNode first;
         Node second;
         Node third = null;
 
         match(IF_TOKEN);
         first = parseParenListExpression();
         second = parseSubstatement(abbrevIfElse_mode);
         if (lookahead()==ELSE_TOKEN)
         {
             shift();
             third = parseSubstatement(mode);
         }
 
         result = nodeFactory.ifStatement(first, second, third);
 
         if (debug)
         {
             System.err.println("finish parseIfStatement");
         }
 
         return result;
     }
 
     /*
      * SwitchStatement
      *     switch ParenListExpression { CaseStatements }
      */
 
     private Node parseSwitchStatement()
     {
 
         if (debug)
         {
             System.err.println("begin parseSwitchStatement");
         }
 
         Node result;
         Node first;
         StatementListNode second;
 
         match(SWITCH_TOKEN);
         first = parseParenListExpression();
         match(LEFTBRACE_TOKEN);
         second = parseCaseStatements();
         match(RIGHTBRACE_TOKEN);
 
         result = nodeFactory.switchStatement(first, second);
 
         if (debug)
         {
             System.err.println("finish parseSwitchStatement");
         }
 
         return result;
     }
 
     /*
      * CaseStatement
      *     Statement
      *     CaseLabel
      */
 
     private Node parseCaseStatement(int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseCaseStatement");
         }
 
         Node result;
         final int lt = lookahead();
 
         result = (lt==CASE_TOKEN || lt==DEFAULT_TOKEN)
             ? parseCaseLabel()
             : parseDirective(null, mode);
       
         if (debug)
         {
             System.err.println("finish parseCaseStatement");
         }
 
         return result;
     }
 
     /*
      * CaseLabel
      *     case ListExpressionallowIn :
      *     default :
      */
 
     private Node parseCaseLabel()
     {
 
         if (debug)
         {
             System.err.println("begin parseCaseLabel");
         }
 
         Node result = null;
         final int lt = lookahead();
         
         if (lt==CASE_TOKEN)
         {
             shift();
             result = nodeFactory.caseLabel(parseListExpression(allowIn_mode),ctx.input.positionOfMark());
         }
         else if (lt==DEFAULT_TOKEN)
         {
             shift();
             result = nodeFactory.caseLabel(null,ctx.input.positionOfMark()); // 0 argument means default case.
         }
         else
         {
             error(kError_Parser_ExpectedCaseLabel);
             skiperror(RIGHTBRACE_TOKEN);
         }
         match(COLON_TOKEN);
 
         if (debug)
         {
             System.err.println("finish parseCaseLabel");
         }
 
         return result;
     }
 
     /*
      * CaseStatements
      *     empty
      *     CaseLabel
      *     CaseLabel CaseStatementsPrefix CaseStatementabbrev
      */
 
     private StatementListNode parseCaseStatements()
     {
 
         if (debug)
         {
             System.err.println("begin parseCaseStatements");
         }
 
         StatementListNode result = null;
         int lt = lookahead();
 
         if (!(lt==RIGHTBRACE_TOKEN || lt==EOS_TOKEN))
         {
             Node first = parseCaseLabel();
             lt=lookahead();
             
             result = (!(lt==RIGHTBRACE_TOKEN || lt==EOS_TOKEN))
                 ? parseCaseStatementsPrefix(nodeFactory.statementList(null, first))
                 : nodeFactory.statementList(null, first);
         }
   
         if (debug)
         {
             System.err.println("finish parseCaseStatements");
         }
 
         return result;
     }
 
     /*
      * CaseStatementsPrefix
      *     empty
      *     CaseStatement[full] CaseStatementsPrefix
      */
 
     private StatementListNode parseCaseStatementsPrefix(StatementListNode first)
     {
 
         if (debug)
         {
             System.err.println("begin parseCaseStatementsPrefix");
         }
 
         StatementListNode result = null;
 
         if (!(lookahead()==RIGHTBRACE_TOKEN || lookahead()==EOS_TOKEN))
         {
             first = nodeFactory.statementList(first, parseCaseStatement(full_mode));
             while (lookahead()!=RIGHTBRACE_TOKEN && lookahead()!=EOS_TOKEN)
             {
                 first = nodeFactory.statementList(first, parseCaseStatement(full_mode));
             }
             result = first;
         }
 
 
         if (debug)
         {
             System.err.println("finish parseCaseStatementsPrefix");
         }
 
         return result;
     }
 
     /*
      * DoStatement
      *     do Statement[abbrev] while ParenListExpression
      */
 
     private Node parseDoStatement()
     {
 
         if (debug)
         {
             System.err.println("begin parseDoStatement");
         }
 
         Node result;
         Node first;
 
         match(DO_TOKEN);
         first = parseSubstatement(abbrevDoWhile_mode);
         match(WHILE_TOKEN);
         result = nodeFactory.doStatement(first, parseParenListExpression());
 
         if (debug)
         {
             System.err.println("finish parseDoStatement");
         }
 
         return result;
     }
 
     /*
      * WhileStatement
      *     while ParenListExpression Statement
      */
 
     private Node parseWhileStatement(int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseWhileStatement");
         }
 
         Node result;
 
         match(WHILE_TOKEN);
         Node first = parseParenListExpression();
         Node second = parseSubstatement(mode);
         result = nodeFactory.whileStatement(first, second);
 
         if (debug)
         {
             System.err.println("finish parseWhileStatement");
         }
 
         return result;
     }
 
     /*
      * ForStatementw
      *     for ( ForInitializer ; OptionalExpression ; OptionalExpression ) Substatementw
      *     for ( ForInBinding in ListExpressionallowIn ) Substatementw
      *
      * ForInitializer
      *     empty
      *     ListExpressionnoIn
      *     VariableDefinitionnoIn
      *     //Attributes [no line break] VariableDefinitionnoIn
      *
      * ForInBinding
      *     PostfixExpression
      *     VariableDefinitionKind VariableBindingnoIn
      *     //Attributes [no line break] VariableDefinitionKind VariableBindingnoIn
      */
 
     private Node parseForStatement(int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseForStatement");
         }
 
         Node result;
         boolean is_each = false;
 
         match(FOR_TOKEN);
 
         if(lookahead()==IDENTIFIER_TOKEN)
         {
             String id = scanner.getTokenText(match(IDENTIFIER_TOKEN));
             if( !id.equals("each") )
             {
                 error(syntax_error,kError_Parser_ExpectedLeftParen);
             }
             is_each = true;
         }
 
         match(LEFTPAREN_TOKEN);
 
         Node first;
         final int lt = lookahead();
         
         if (lt==SEMICOLON_TOKEN)
         {
             first = null;
         }
         else if (lt==CONST_TOKEN || lt==VAR_TOKEN)
         {
             first = parseVariableDefinition(null/*attrs*/, noIn_mode);
         }
         else if( is_each )
         {
             first = nodeFactory.list(null,parsePostfixExpression());
         }
         else
         {
             first = parseListExpression(noIn_mode);
         }
 
         Node second;
         if (lookahead()==IN_TOKEN)
         {
             // ISSUE: verify that first is a single expression or variable definition
 
             if( first instanceof VariableDefinitionNode && ((VariableDefinitionNode)first).list.size() > 1)
             {
                 error(syntax_error, kError_ParserInvalidForInInitializer);
             }
             else if( first instanceof ListNode && ((ListNode)first).size() > 1 )
             {
                 error(syntax_error, kError_ParserInvalidForInInitializer);
             }
             match(IN_TOKEN);
             second = parseListExpression(allowIn_mode);
             match(RIGHTPAREN_TOKEN);
             int pos = ctx.input.positionOfMark();
             result = nodeFactory.forInStatement(is_each, first, second, parseSubstatement(mode), pos);
         }
         else if(lookahead()==COLON_TOKEN)
         {
             match(IN_TOKEN); //error(syntax_error,kError_Parser_EachWithoutIn);
             skiperror(LEFTBRACE_TOKEN);
             result = parseSubstatement(mode);
         }
         else
         {
             if( is_each )
             {
                 error(syntax_error,kError_Parser_EachWithoutIn);
             }
             Node third;
             match(SEMICOLON_TOKEN);
             
             second = (lookahead()==SEMICOLON_TOKEN)
                 ? null
                 : parseListExpression(allowIn_mode);
             match(SEMICOLON_TOKEN);
 
             third = (lookahead()==RIGHTPAREN_TOKEN)
                 ? null
                 : parseListExpression(allowIn_mode);
             match(RIGHTPAREN_TOKEN);
             
             int pos = ctx.input.positionOfMark();
             result = nodeFactory.forStatement(first, second, third, parseSubstatement(mode),false/*is_forin*/, pos);
         }
         return result;
     }
 
     /*
      * WithStatement
      *     with ParenListExpression Statement
      */
 
     private Node parseWithStatement(int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseWithStatement");
         }
 
         Node result;
 
         match(WITH_TOKEN);
         Node first = parseParenListExpression();
         result = nodeFactory.withStatement(first, parseSubstatement(mode));
 
         if (debug)
         {
             System.err.println("finish parseWithStatement");
         }
 
         return result;
     }
 
     /*
      * ContinueStatement
      *     continue
      *     continue [no line break] Identifier
      */
 
     private Node parseContinueStatement()
     {
 
         if (debug)
         {
             System.err.println("begin parseContinueStatement");
         }
 
         Node result;
         IdentifierNode first = null;
 
         match(CONTINUE_TOKEN);
         if (!lookaheadSemicolon(full_mode))
         {
             first = parseIdentifier();
         }
 
         result = nodeFactory.continueStatement(first,ctx.input.positionOfMark());
 
         if (debug)
         {
             System.err.println("finish parseContinueStatement");
         }
 
         return result;
     }
 
     /*
      * BreakStatement
      *     break
      *     break [no line break] Identifier
      */
 
     private Node parseBreakStatement()
     {
 
         if (debug)
         {
             System.err.println("begin parseBreakStatement");
         }
 
         Node result;
         IdentifierNode first = null;
 
         match(BREAK_TOKEN);
         if (!lookaheadSemicolon(full_mode))
         {
             first = parseIdentifier();
         }
 
         result = nodeFactory.breakStatement(first,ctx.input.positionOfMark());
 
         if (debug)
         {
             System.err.println("finish parseBreakStatement");
         }
 
         return result;
     }
 
     /*
      * ReturnStatement
      *     return
      *     return [no line break] ExpressionallowIn
      */
 
     private Node parseReturnStatement()
     {
 
         if (debug)
         {
             System.err.println("begin parseReturnStatement");
         }
 
         Node result;
         Node first = null;
 
         match(RETURN_TOKEN);
 
         // ACTION: check for VirtualSemicolon
 
         if (!lookaheadSemicolon(full_mode))
         {
             first = parseListExpression(allowIn_mode);
         }
 
         result = nodeFactory.returnStatement(first, ctx.input.positionOfMark());
 
         if (debug)
         {
             System.err.println("finish parseReturnStatement");
         }
 
         return result;
     }
 
     /*
      * ThrowStatement
      *     throw [no line break] ListExpression[allowIn]
      */
 
     private Node parseThrowStatement()
     {
 
         if (debug)
         {
             System.err.println("begin parseThrowStatement");
         }
 
         Node result;
 
         match(THROW_TOKEN);
         lookahead(); // force read of next token
      
         if (scanner.followsLineTerminator())
         {
             error(syntax_error, kError_Parser_ThrowWithoutExpression,"","",ctx.input.positionOfMark()-8);   // -8 gets us to the first char of throw
             result = nodeFactory.throwStatement(null,ctx.input.positionOfMark());    // make a dummy node
         }
         else
         {
             result = nodeFactory.throwStatement(parseListExpression(allowIn_mode),ctx.input.positionOfMark());
         }
 
         if (debug)
         {
             System.err.println("finish parseThrowStatement");
         }
 
         return result;
     }
 
     /*
      * TryStatement
      *     try AnnotatedBlock CatchClauses
      *     try AnnotatedBlock FinallyClause
      *     try AnnotatedBlock CatchClauses FinallyClause
      */
 
     private Node parseTryStatement()
     {
 
         if (debug)
         {
             System.err.println("begin parseTryStatement");
         }
 
         Node result;
         StatementListNode first;
 
         match(TRY_TOKEN);
         first = parseBlock();
         if (lookahead()==CATCH_TOKEN)
         {
             StatementListNode second = parseCatchClauses();
             if (lookahead()==FINALLY_TOKEN)
             {
                 result = nodeFactory.tryStatement(first, second, parseFinallyClause());
             }
             else
             {
                 result = nodeFactory.tryStatement(first, second, null);
             }
         }
         else if (lookahead()==FINALLY_TOKEN)
         {
             // finally with no catch clause
             // force feed the list a default catch clause so finally works
             StatementListNode catchClause = nodeFactory.statementList(null,
                           nodeFactory.catchClause(null,
                           nodeFactory.statementList(null,
                           nodeFactory.throwStatement(
                           null, 0 ))));
 
             result = nodeFactory.tryStatement(first, catchClause, parseFinallyClause());
         }
         else
         {
             error(kError_Parser_ExpectingCatchOrFinally);
             skiperror(SEMICOLON_TOKEN);
             result = null;
         }
 
         if (debug)
         {
             System.err.println("finish parseTryStatement");
         }
 
         return result;
     }
 
     /*
      * CatchClauses
      *     CatchClause
      *     CatchClauses CatchClause
      */
 
     private StatementListNode parseCatchClauses()
     {
 
         if (debug)
         {
             System.err.println("begin parseCatchClauses");
         }
 
         StatementListNode result;
 
         result = nodeFactory.statementList(null, parseCatchClause());
         while (lookahead()==CATCH_TOKEN)
         {
             result = nodeFactory.statementList(result, parseCatchClause());
         }
 
         if (debug)
         {
             System.err.println("finish parseCatchClauses");
         }
 
         return result;
     }
 
     /*
      * CatchClause
      *     catch ( Parameter ) AnnotatedBlock
      */
 
     private Node parseCatchClause()
     {
 
         if (debug)
         {
             System.err.println("begin parseCatchClause");
         }
 
         Node result;
         Node first;
 
         match(CATCH_TOKEN);
         match(LEFTPAREN_TOKEN);
         first = parseParameter();
         match(RIGHTPAREN_TOKEN);
 
         result = nodeFactory.catchClause(first, parseBlock());
 
         if (debug)
         {
             System.err.println("finish parseCatchClause");
         }
 
         return result;
     }
 
     /*
      * FinallyClause
      *     finally AnnotatedBlock
      */
 
     private FinallyClauseNode parseFinallyClause()
     {
 
         if (debug)
         {
             System.err.println("begin parseFinallyClause");
         }
 
         FinallyClauseNode result;
 
         match(FINALLY_TOKEN);
 
         // No line break.
 
         result = nodeFactory.finallyClause(parseBlock());
 
         if (debug)
         {
             System.err.println("finish parseFinallyClause");
         }
 
         return result;
     }
 
 
     // Directives
 
 
     /*
      * Directivew
      *     DefaultXMLNamespaceDirective
      *     EmptyStatement
      *     AnnotatableDirectivew
      *     Pragma Semicolonw
      *     Attributes [no line break] AnnotatableDirectivew
      *     Attributes [no line break] { Directives }
      *     Statementw
      */
 
     private Node parseDirective(AttributeListNode first, int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseDirective");
         }
 
         Node result = null;
 
         try
         {
         	final int lt = lookahead();
         	
             switch ( lt )
             {
             case SEMICOLON_TOKEN:
                 matchNoninsertableSemicolon(mode);
                 result = nodeFactory.emptyStatement();
                 break;
                 
             case INTERFACE_TOKEN:
                 if (HAS_INTERFACEDEFINITIONS==false)
                 {
                     result = parseAnnotatedDirectiveOrStatement(mode);
                     break;
                 }
                 //fall through
             case VAR_TOKEN: case CONST_TOKEN: case FUNCTION_TOKEN: case CLASS_TOKEN:
             case NAMESPACE_TOKEN: case IMPORT_TOKEN: case INCLUDE_TOKEN: case USE_TOKEN:
                 result = parseAnnotatableDirectiveOrPragmaOrInclude(first, mode);
                 break;
                 
             case DEFAULT_TOKEN:
             {
                 shift();
                 String id = scanner.getTokenText(match(IDENTIFIER_TOKEN));
                 if( !id.equals("xml") && lookahead()==NAMESPACE_TOKEN )
                 {
                     error(kError_Parser_ExpectedXMLBeforeNameSpace);
                 }
                 match(NAMESPACE_TOKEN);
                 match(ASSIGN_TOKEN);
                 result = nodeFactory.defaultXMLNamespace(parseNonAssignmentExpression(allowIn_mode),0);
                 break;
             }
             default:
                 result = parseAnnotatedDirectiveOrStatement(mode);
             }
 
             if (debug)
             {
                 System.err.println("finish parseDirective with " + ((result != null) ? result.toString() : ""));
             }
 
         }
         catch (Exception ex)
         {
 	        result = null;
 	        //assert false : ex.getMessage();
             // ex.printStackTrace();
             // System.err.println("\nInternal error: exception caught in parseDirective()");
         }
 
         return result;
     }
 
     /*
      *  AnnotatedDirectiveOrStatement
      *   [ true, false, private, public, Identifier ] Attributes [no line break] AnnotatableDirectivew
      *   [ true, false, private, public, Identifier ] Attributes [no line break] { Directives }
      *   Statementw
      */
 
     private Node parseAnnotatedDirectiveOrStatement(int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseDefinition");
         }
 
         Node result = null;
         int lt = lookahead();
 
         if (lt==SUPER_TOKEN    || lt==LEFTBRACE_TOKEN   || lt==IF_TOKEN     || lt==SWITCH_TOKEN ||
             lt==DO_TOKEN       || lt==WHILE_TOKEN       || lt==FOR_TOKEN    || lt==WITH_TOKEN ||
             lt==CONTINUE_TOKEN || lt==BREAK_TOKEN       || lt==RETURN_TOKEN || lt==THROW_TOKEN ||
             lt==TRY_TOKEN      || lt==LEFTBRACKET_TOKEN || lt==DOCCOMMENT_TOKEN)
         {
             result = parseStatement(mode);
         }
         else 
         {
             Node temp;
             if (HAS_SQUAREBRACKETATTRS && lt==LEFTBRACKET_TOKEN)
             {
                 shift();
                 temp = parseAssignmentExpression(allowIn_mode);
                 match(RIGHTBRACKET_TOKEN);
             }
             else
             {
                 temp = parseLabeledOrExpressionStatement(mode);
             }
             
             String attributeString = this.scanner.getTokenText(lastToken); //??? this seems to be the only use of lastToken {pmd}
             String directiveString = this.scanner.getTokenText(nextToken);
 
             if (temp.isLabeledStatement())
             {
                     result = temp; // no semi-colon necessary if labeled statement.
             }
             else 
             {
                 if (lookaheadSemicolon(mode) && !temp.isConfigurationName())
                 {
                     // If full mode then cannot be an attribute
                     matchSemicolon(mode);
                     
                     result = temp;
 
                     ExpressionStatementNode estmt;
                     
                     if (temp instanceof ExpressionStatementNode)
                     {
                         estmt = (ExpressionStatementNode) temp;
                         if (estmt != null)
                         {
                             temp = estmt.expr;
                         }
                     }
                     boolean is_attribute_keyword = checkAttribute(block_kind_stack.last(),temp);
                     if( is_attribute_keyword )
                     {
                         error(syntax_error, kError_Parser_ExpectingAnnotatableDirectiveAfterAttributes, attributeString, directiveString);
                     }
                 }
                 else if (temp.isAttribute())
                 {
                     // If its an expression statement, then extract the expression
                     
                     ExpressionStatementNode estmt = (temp instanceof ExpressionStatementNode) 
                         ? (ExpressionStatementNode) temp : null;
                         
                     if (estmt != null)
                     {
                         temp = estmt.expr;
                     }
 
                     boolean is_attribute_keyword = checkAttribute(block_kind_stack.last(),temp);
 
                     AttributeListNode first;
                     lt = lookahead();
                     switch ( lt )
                     {
                     case TRUE_TOKEN: case FALSE_TOKEN: case PRIVATE_TOKEN: case PUBLIC_TOKEN: 
                     case PROTECTED_TOKEN: case IDENTIFIER_TOKEN:
                         first = nodeFactory.attributeList(temp, parseAttributes());
                         break;
                         
                     default:
                         first = nodeFactory.attributeList(temp, null);
                     }
 
                     lt = lookahead();
                     switch ( lt )
                     {
                     case VAR_TOKEN: case CONST_TOKEN: case FUNCTION_TOKEN: case CLASS_TOKEN:
                     case NAMESPACE_TOKEN: case IMPORT_TOKEN: case USE_TOKEN: case INTERFACE_TOKEN:
                     case DOCCOMMENT_TOKEN:
                         result = parseAnnotatableDirectiveOrPragmaOrInclude(first, mode);
                         break;
                         
                     default:
                         if ( temp.isConfigurationName() && lt==LEFTBRACE_TOKEN )
                         {
                             result = parseAnnotatableDirectiveOrPragmaOrInclude(first, mode);
                         }
                         else if(lt == PACKAGE_TOKEN)
                         {
                              error(kError_AttributesOnPackage);
                              result = parsePackageDefinition();
                         }
                         else if( is_attribute_keyword || first.size() > 1 )
                         {
                             error(syntax_error, kError_Parser_ExpectingAnnotatableDirectiveAfterAttributes,
                                     attributeString, directiveString);
                             skiperror(SEMICOLON_TOKEN);
                         }
                         else if ( temp.isConfigurationName() )
                         {
                             // Flex gets here when it parses a code fragment like: CONFIG::foo                       
                             result = estmt;
                         }
                         else
                         {
                             // It's not known what code would reach this                                             
                             // state, so this is a best guess at how to                                              
                             // handle it.
                             
                             // Note that asc-tests have an error case e.g. the expression statement "x::y::z;"
                             // that reaches this code, as a result, behavior with/without
                             // this assert enabled differs.
                             // So I shut it off. {pmd 9/28/2008}
                            // assert false : "Unexpected state";
                             result = estmt;
                         }
                     }
                 }
             }
         }
 
         if (debug)
         {
             System.err.println("finish parseDefinition");
         }
         return result;
     }
 
     /*
      *  AnnotatedSubstatementsOrStatement
      */
 
     private Node parseAnnotatedSubstatementsOrStatement(int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseDefinition");
         }
 
         Node result = null;
         final int lt = lookahead();
         
         switch ( lt )
         {
         case SUPER_TOKEN: case LEFTBRACE_TOKEN: case IF_TOKEN: case SWITCH_TOKEN: case DO_TOKEN:
         case WHILE_TOKEN: case FOR_TOKEN: case WITH_TOKEN: case CONTINUE_TOKEN: case BREAK_TOKEN: case RETURN_TOKEN:
         case THROW_TOKEN: case TRY_TOKEN: case LEFTBRACKET_TOKEN: case DOCCOMMENT_TOKEN:
             result = parseStatement(mode);
             break;
             
         default:
             {
                 Node temp;
                 temp = parseLabeledOrExpressionStatement(mode);
                 if (!temp.isLabeledStatement())
                 {
                     if (lookaheadSemicolon(mode))
                     {
                         // If full mode then cannot be an attribute
                         matchSemicolon(mode);
                         result = temp;
                     }
                     else if (temp.isAttribute())
                     {
                         error(syntax_error,kError_InvalidAttribute);
 
                         /* cn: this is for the unimplemented named block feature
                          *
                         AttributeListNode first;
                         if (lookahead(TRUE_TOKEN) || lookahead(FALSE_TOKEN) ||
                             lookahead(PRIVATE_TOKEN) || lookahead(PUBLIC_TOKEN) ||
                             lookahead(PROTECTED_TOKEN) ||
                             lookahead(STATIC_TOKEN) || lookahead(IDENTIFIER_TOKEN))
                         {
                             first = nodeFactory.attributeList(temp, parseAttributes());
                         }
                         else
                         {
                             first = nodeFactory.attributeList(temp, null);
                         }
 
                         if (lookahead(LEFTBRACE_TOKEN))
                         {
                             result = parseSubstatements(first, null);
                         }
                         else
                         {
                             error(syntax_error, kError_Parser_ExpectingBlockStatement);
                             skiperror(SEMICOLON_TOKEN);
                         }
 
                          */
                     }
                     else
                     {
                         match(SEMICOLON_TOKEN); // force syntax error
                         skiperror(SEMICOLON_TOKEN);
                     }
                 }
                 else
                 {
                     result = temp;
                     // no semi-colon necessary if labeled statement.
                 }
             }
         }
 
         if (debug)
         {
             System.err.println("finish parseDefinition");
         }
 
         return result;
     }
 
     /*
      *  AnnotatableDirectivew
      *      VariableDefinitionallowIn Semicolonw
      *      FunctionDefinition
      *      ClassDefinition
      *      NamespaceDefinition Semicolonw
      *      ImportDirective Semicolonw
      *      UseDirective Semicolonw
      *      IncludeDirective Semicolonw
      */
 
     private Node parseAnnotatableDirectiveOrPragmaOrInclude(AttributeListNode first, int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseDefinition");
         }
 
         Node result = null;
         final int lt = lookahead();
         
         switch ( lt )
         {
         case CONST_TOKEN:
         case VAR_TOKEN:
             result = parseVariableDefinition(first, mode);
             matchSemicolon(mode);
             break;
    
         case FUNCTION_TOKEN:
             result = parseFunctionDefinition(first);
             break;
             
         case CLASS_TOKEN:
             result = parseClassDefinition(first, mode);
             break;
             
         case INTERFACE_TOKEN:
             result = parseInterfaceDefinition(first, mode);
             break;
             
         case NAMESPACE_TOKEN:
             result = parseNamespaceDefinition(first);
             matchSemicolon(mode);
             break;
 
 //        case CONFIG_TOKEN:
 //            result = parseConfigNamespaceDefinition(first);
 //            matchSemicolon(mode);
 //            break;
 
         case IMPORT_TOKEN:
             result = parseImportDirective(first);
             matchSemicolon(mode);
             break;
             
         case USE_TOKEN:
             result = parseUseDirectiveOrPragma(first);
             matchSemicolon(mode);
             break;
             
         case INCLUDE_TOKEN:
             result = parseIncludeDirective();
             matchSemicolon(mode);
             break;
             
         case LEFTBRACE_TOKEN:
         	StatementListNode stmts = parseBlock();
         	stmts.config_attrs = first;
         	result = stmts;
         	matchSemicolon(mode);
         	break;
         
         default:
             error(kError_Parser_DefinitionOrDirectiveExpected);
             skiperror(SEMICOLON_TOKEN);
             break;
         }
 
         if (debug)
         {
             System.err.println("finish parseDefinition");
         }
         return result;
     }
 
     public StatementListNode parseDirectives(AttributeListNode first, StatementListNode second)
     {
 
         if (debug)
         {
             System.err.println("begin parseDirectives");
         }
 
         StatementListNode result = null;
         Node third;
         int lt;
 
         while (!( (lt=lookahead())==RIGHTBRACE_TOKEN || lt==EOS_TOKEN))
         {
             third = parseDirective(first, abbrev_mode);
 	        if (third == null)
 	        {
 		        break;
 	        }
             else if( third instanceof StatementListNode )
             {
                 StatementListNode sln = (StatementListNode) third;     // remove any gratuitous nestings
                 if( sln.config_attrs == null && !sln.has_pragma )
                 {
                     if( second == null )
                     {
                         second = sln;
                     }
                     else
                     {
                         second.items.addAll(sln.items);
                     }
                 }
                 else
                 {
                 	// Can't collapse into one statementlist node if the inner one might be compiled
                 	// out due to conditional compilation
                 	second = nodeFactory.statementList(second, third);
                 }
             }
             else if (third instanceof PragmaNode) {
                 // make sure it's at the top of the block
             	if (second != null) {
             		for (int i = 0; i < second.items.size(); i++) {
             			Node kid = second.items.at(i);
             			if (!(kid instanceof PragmaNode)) {
             				error(syntax_error,kError_Parser_NumericUseMisplaced);
             				break;
             			}
             		}
             	}
             	second = nodeFactory.statementList(second, third);
             	second.has_pragma = true;
             }
             else
             {
                 second = nodeFactory.statementList(second, third);
             }
         }
 
         result = second;
 
         if (debug)
         {
             System.err.println("finish parseDirectives");
         }
 
         return result;
     }
 
     /*
      * UseDirective
      *  use namespace ParenListExpression
      *
      * Pragma
      *  use PragmaItems
      *
      * PragmaItems
      *  PragmaItem
      *  PragmaItems , PragmaItem
      *
      * PragmaItem
      *  PragmaExpr
      *  PragmaExpr ?
      *
      * PragmaExpr
      *  Identifier
      *  Identifier ( PragmaArgument )
      *
      * PragmaArgument
      *  true
      *  false
      *  Number
      *  - Number
      *  - NegatedMinLong
      * String
      */
 
     private Node parseUseDirectiveOrPragma(AttributeListNode first)
     {
 
         if (debug)
         {
             System.err.println("begin parseUseDirectiveOrPragma");
         }
 
         Node result = null;
 
         match(USE_TOKEN);
         
         if (lookahead()==NAMESPACE_TOKEN)
         {
             shift();
             result = nodeFactory.useDirective(first, parseNonAssignmentExpression(allowIn_mode));
         }
         else if (lookahead()==INCLUDE_TOKEN)  // for AS3 #include
         {
             result = parseIncludeDirective();
         }
         else
         {
             if( ctx.statics.es4_numerics )
                 result = nodeFactory.pragma(parsePragmaItems(allowIn_mode),ctx.input.positionOfMark());
             else
                 error(kError_UndefinedNamespace);  // Do what we used to do... better than an unlocalized internal error of feature not implemented
         }
 
         if (debug)
         {
             System.err.println("finish parseUseDirectiveOrPragma");
         }
 
         return result;
     }
 
 	private Node parsePragmaItem(int mode) {
         Node id;
         Node argument;
         if (debug)
         {
             System.err.println("begin parsePragmaItem");
         }
 
         id = parseIdentifier();
         if (lookahead()==COMMA_TOKEN || lookaheadSemicolon(mode)) {
             argument = null;
         } 
         else if (lookahead()==TRUE_TOKEN) {
             shift();
             argument = nodeFactory.literalBoolean(true,ctx.input.positionOfMark());
         }
         else if (lookahead()==FALSE_TOKEN)
         {
             shift();
             argument = nodeFactory.literalBoolean(false,ctx.input.positionOfMark());
         }
         else if (lookahead()==NUMBERLITERAL_TOKEN)
         {
             argument = nodeFactory.literalNumber(scanner.getTokenText(match(NUMBERLITERAL_TOKEN)),
                                                ctx.input.positionOfMark());
         }
         else if (lookahead()==STRINGLITERAL_TOKEN)
         {
             boolean[] is_single_quoted = new boolean[1];
             String enclosedText = scanner.getStringTokenText(match(STRINGLITERAL_TOKEN), is_single_quoted);
             argument = nodeFactory.literalString(enclosedText, ctx.input.positionOfMark(), is_single_quoted[0] );
         }
         else argument = parseIdentifier();
         Node result = nodeFactory.usePragma(id, argument, ctx.input.positionOfMark());
 
         if (debug)
         {
             System.err.println("finish parsePragmaItem");
         }
         return result;
     }
 
     private ListNode parsePragmaItems(int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parsePragmaItems");
         }
 
         ListNode result = null;
 
         Node first = parsePragmaItem(mode);
 
         result = parsePragmaItemsPrime(nodeFactory.list(null, first), mode); 
 
         if (debug)
         {
             System.err.println("finish parsePragmaItems");
         }
         return result;
     }
 
 	private ListNode parsePragmaItemsPrime(ListNode first, int mode) {
         if (debug)
         {
             System.err.println("begin parsePragmaItemsPrime");
         }
 
         ListNode result;
 
         if (lookahead()==COMMA_TOKEN) {
             shift();
             Node second;
             second = parsePragmaItem(mode);
             result =  parsePragmaItemsPrime(nodeFactory.list(first, second), mode);
         }
         else
             result = first;
 
         if (debug)
         {
             System.err.println("finish parsePragmaItemsPrime");
         }
         return result;
     }
 
     /*
      * IncludeDirective
      *     include [no line break] String
      */
 
     private Node parseIncludeDirective()
     {
 
         if (debug)
         {
             System.err.println("begin parseIncludeDirective");
         }
 
         IncludeDirectiveNode result;
         LiteralStringNode first;
 
         match(INCLUDE_TOKEN);
 
         boolean[] is_single_quoted = new boolean[1];
         String filespec = scanner.getStringTokenText(match(STRINGLITERAL_TOKEN), is_single_quoted).trim();
 
         CompilerHandler.FileInclude incl = null;
         if (ctx.handler != null)
         {
             incl = ctx.handler.findFileInclude(ctx.path(), filespec);
         }
 
 	    // The input could be an input stream or an in-memory string.
         InputStream in = null;
 	    String text = null;
 
         String fixed_filespec = null, parentPath = null;
         if (incl == null)
         {
             filespec = filespec.replace('/', File.separatorChar);
 
             File inc_file = new File(filespec);
             if (inc_file.isAbsolute())
             {
                 // absolute path
                	fixed_filespec = inc_file.getAbsolutePath();
             }
             else
             {
                 // must be a relative path
                 fixed_filespec = (ctx.path() == null ? "" : ctx.path()) + File.separator + filespec;
             }
 
             try
             {
             	fixed_filespec = new File(fixed_filespec).getCanonicalPath();
             }
             catch (IOException ex)
             {
             	fixed_filespec = new File(fixed_filespec).getAbsolutePath();
             }
             
             parentPath = fixed_filespec.substring(0, fixed_filespec.lastIndexOf(File.separator));
         	if (!ctx.scriptAssistParsing){
 	            try
 	            {
 	                in = new BufferedInputStream(new FileInputStream(fixed_filespec));
 	            }
 	            catch (FileNotFoundException ex)
 	            {
 	                error(syntax_error, kError_Parser_UnableToOpenFile, fixed_filespec);
 	                return null;
 	            }
         	}
         }
         else
         {
             fixed_filespec = incl.fixed_filespec;
             parentPath = incl.parentPath;
             in = incl.in;
 	        text = incl.text;
         }
 
         // make sure that we check the include path trail. This is to stop infinite recursion.
         if (ctx.statics.includePaths.contains(fixed_filespec))
         {
             error(syntax_error, kError_Parser_FileIncludesItself, fixed_filespec);
             try { in.close(); } catch (IOException ex) {}
             return null;
         }
         else
         {
             // add the file name to the include path trail.
             ctx.statics.includePaths.push_back(fixed_filespec);
         }
 
         // To get proper path resolution for included files inside of include directives,
         // temporarily update ctx.pathspec with spec (minus file name) for this file
 
         String oldCtxPathSpec = ctx.path();
         ctx.setPath(parentPath);
 
         // Reset to oldCtxPathSpec once we are finished parsing this file
 
         first  = nodeFactory.literalString(fixed_filespec,ctx.input.positionOfMark(), is_single_quoted[0]);
 
         ProgramNode second = null;
     	if (!ctx.scriptAssistParsing){
 	        Context cx = new Context(ctx.statics);
 	        try
 	        {
 	            // cx.setEmitter(ctx.getEmitter());
 	            // cx.statics.nodeFactory = ctx.statics.nodeFactory;
 	            // cx.statics.global = ctx.statics.global;
             Parser p = null;
 		        if (in != null)
 		        {
                 p = new Parser(cx, in, fixed_filespec, encoding, create_doc_info, save_comment_nodes,block_kind_stack, true);
                 p.config_namespaces = this.config_namespaces;
                 second = p.parseProgram();
 		        }
 		        else
 		        {
                 p = new Parser(cx, text, fixed_filespec, create_doc_info, save_comment_nodes,block_kind_stack, true);
                 p.config_namespaces = this.config_namespaces;
 	            second = p.parseProgram();
 		        }
 	        }
 	        finally
 	        {
 	            ctx.setPath(oldCtxPathSpec);
 	            // now we can remove the filename...
 	            ctx.statics.includePaths.removeLast();
 	            if (in != null)
 		        {
 			        try { in.close(); } catch (IOException ex) {}
 		        }
 	        }
 	        result = nodeFactory.includeDirective(cx, first, second);
 	    } else
 	    	result = nodeFactory.includeDirective(ctx, first, second);
 
         if (debug)
         {
             System.err.println("finish parseIncludeDirective");
         }
 
         return result;
     }
 
     /*
      * Attributes
      *     Attribute
      *     Attribute [no line break] Attributes
      */
 
     private boolean checkAttribute( int block_kind, Node node )
     {
         if( block_kind_stack.last() != ERROR_TOKEN  && block_kind_stack.last() != CLASS_TOKEN  && block_kind_stack.last() != INTERFACE_TOKEN)
         {
             if( node.hasAttribute("private") )
             {
                 ctx.error(node.pos(), kError_InvalidPrivate);
             }
             else
             if( node.hasAttribute("protected") )
             {
                 ctx.error(node.pos(), kError_InvalidProtected);
             }
             else
             if( node.hasAttribute("static") )
             {
                 ctx.error(node.pos(), kError_InvalidStatic);
             }
             else
             if( block_kind_stack.last() != PACKAGE_TOKEN && block_kind_stack.last() != EMPTY_TOKEN && node.hasAttribute("internal") )
             {
                 ctx.error(node.pos(), kError_InvalidInternal);
             }
             else
             if( block_kind_stack.last() != PACKAGE_TOKEN && node.hasAttribute("public") )
             {
                 ctx.error(node.pos(), kError_InvalidPublic);
             }
         }
 
         if( node.hasAttribute("prototype") )
         {
             ctx.error(node.pos(),kError_PrototypeIsAnInvalidAttribute);
         }
 
         return node.hasAttribute("static") ||
                 node.hasAttribute("public") ||
                 node.hasAttribute("private") ||
                 node.hasAttribute("protected") ||
                 node.hasAttribute("internal") ||
                 node.hasAttribute("native") ||
                 node.hasAttribute("final") ||
                 node.hasAttribute("override") ||
                 node.hasAttribute("prototype");
     }
 
     private AttributeListNode parseAttributes()
     {
 
         if (debug)
         {
             System.err.println("begin parseAttributes");
         }
 
         AttributeListNode result;
 
         Node first = parseAttribute();
 
         checkAttribute(block_kind_stack.last(),first);
 
         AttributeListNode second = null;
         final int lt=lookahead();
         
         switch ( lt )
         {
         case IDENTIFIER_TOKEN: case PRIVATE_TOKEN: case PROTECTED_TOKEN: 
         case PUBLIC_TOKEN: case FALSE_TOKEN: case TRUE_TOKEN:
             second = parseAttributes();
         }
 
         result = nodeFactory.attributeList(first, second);
 
         if (debug)
         {
             System.err.println("finish parseAttributes");
         }
 
         return result;
     }
 
     /*
      * Attribute
      *     AttributeExpression
      *     true
      *     false
      *     private
      *     public
      */
 
     private Node parseAttribute()
     {
 
         if (debug)
         {
             System.err.println("begin parseAttribute");
         }
 
         Node result;
         final int lt = lookahead();
         
         switch ( lt )
         {
         case TRUE_TOKEN:
             shift();
             result = nodeFactory.literalBoolean(true, ctx.input.positionOfMark());
             break;
             
         case FALSE_TOKEN:
             shift();
             result = nodeFactory.literalBoolean(false, ctx.input.positionOfMark());
             break;
             
         case PRIVATE_TOKEN:
             shift();
             result = nodeFactory.identifier(PRIVATE, false, ctx.input.positionOfMark());
             if (lookahead()==DOUBLECOLON_TOKEN)
             {
                 shift();
                 result = nodeFactory.qualifiedIdentifier(result, parseIdentifierString(),ctx.input.positionOfMark());
             }
             break;
             
         case PROTECTED_TOKEN:
             shift();
             result = nodeFactory.identifier(PROTECTED, false, ctx.input.positionOfMark());
             if (lookahead()==DOUBLECOLON_TOKEN)
             {
                 shift();
                 result = nodeFactory.qualifiedIdentifier(result, parseIdentifierString(),ctx.input.positionOfMark());
             }
             break;
             
         case PUBLIC_TOKEN:
             shift();
             result = nodeFactory.identifier(PUBLIC, false, ctx.input.positionOfMark());
             if (lookahead()==DOUBLECOLON_TOKEN)
             {
                 shift();
                 result = nodeFactory.qualifiedIdentifier(result, parseIdentifierString(),ctx.input.positionOfMark());
             }
             break;
             
         case LEFTBRACKET_TOKEN:
             result = parseArrayLiteral();
             break;
         
         default:
             result = parseSimpleTypeIdentifier();
         }
 
         if (debug)
         {
             System.err.println("finish parseAttribute");
         }
 
         return result;
     }
 
     /*
      * ImportDirective
      *     import PackageName
      *     import Identifier = PackageName
      */
 
     private Node parseImportDirective(AttributeListNode first)
     {
 
         if (debug)
         {
             System.err.println("begin parseImportDirective");
         }
 
         Node result;
         PackageNameNode second;
         PackageNameNode third = null;
 
         int stmtPos = scanner.input.positionOfMark();
         match(IMPORT_TOKEN);
         second = parsePackageName(true);
         if (lookahead()==ASSIGN_TOKEN)
         {
             // ISSUE: second should be a simple identifier
             third = parsePackageName(true);
         }
 
         result = nodeFactory.importDirective(first, second, third, stmtPos, ctx);
 
         if (debug)
         {
             System.err.println("finish parseImportDirective");
         }
 
         return result;
     }
 
     // Definitions
 
 
     /*
      * VariableDefinition
      *     VariableDefinitionKind VariableBindingList[allowIn]
      */
 
     private Node parseVariableDefinition(AttributeListNode first, int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseVariableDefinition");
         }
 
         int second;
         ListNode third;
         Node result;
 
         // The following logic goes something like this: If it is a
         // CONST_TOKEN, then first is a CONST_TOKEN. If it is a VAR_TOKEN
         // then first is VAR_TOKEN. If it is anything else then first is
         // the default (VAR_TOKEN).
 
         second = (lookahead()==CONST_TOKEN) ? match(CONST_TOKEN) :
             (lookahead()==VAR_TOKEN) ? match(VAR_TOKEN) : VAR_TOKEN;
         third = parseVariableBindingList(first, second, mode);
         result = nodeFactory.variableDefinition(first, second, third);
 
         if (debug)
         {
             System.err.println("finish parseVariableDefinition");
         }
 
         return result;
     }
 
     /*
      * VariableBindingList
      *     VariableBinding VariableBindingListPrime
      *
      * VariableBindingListPrime
      *     , VariableBinding VariableBindingListPrime
      *     empty
      */
 
     private ListNode parseVariableBindingList(AttributeListNode attrs, int kind, int mode)
     {
         if (debug)
         {
             System.err.println("begin parseVariableBindingList");
         }
 
         ListNode result;
         Node first;
 
         first = parseVariableBinding(attrs, kind, mode);
         result = parseVariableBindingListPrime(attrs, kind, mode, nodeFactory.list(null, first));
 
         if (debug)
         {
             System.err.println("finish parseVariableBindingList");
         }
 
         return result;
     }
 
     private ListNode parseVariableBindingListPrime(AttributeListNode attrs, int kind, int mode, ListNode first)
     {
         if (debug)
         {
             System.err.println("begin parseVariableBindingListPrime");
         }
 
         ListNode result;
 
         if (lookahead()==COMMA_TOKEN)
         {
             shift();
             Node second;
             second = parseVariableBinding(attrs, kind, mode);
             result = parseVariableBindingListPrime(attrs, kind, mode, nodeFactory.list(first, second));
         }
         else
         {
             result = first;
         }
 
         if (debug)
         {
             System.err.println("finish parseVariableBindingListPrime'");
         }
 
         return result;
     }
 
     /*
      * VariableBindingb
      *     TypedIdentifierb VariableInitialisationb
      */
 
     private Node parseVariableBinding(AttributeListNode attrs, int kind, int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseVariableBinding");
         }
 
         Node result;
         TypedIdentifierNode first;
         Node second;
 
         first = parseTypedIdentifier(mode);
         second = parseVariableInitialization(mode);
         result = nodeFactory.variableBinding(attrs, kind, first, second);
 
         if (debug)
         {
             System.err.println("finish parseVariableBinding");
         }
 
         return result;
     }
 
     /*
      * VariableInitialisationb
      *     empty
      *     = VariableInitialiserb
      *
      * VariableInitialiserb
      *     AssignmentExpressionb
      *     AttributeCombination
      */
 
     private Node parseVariableInitialization(int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseVariableInitialization");
         }
 
         Node result = null;
         Node first;
 
         if (lookahead()==ASSIGN_TOKEN)
         {
             shift();
             lookaheadInSlashRegExpContext();
             first = parseAssignmentExpression(mode);
             
             if (lookahead()==COMMA_TOKEN || 
                 lookaheadSemicolon(mode) ||
                 (ctx.scriptAssistParsing && lookahead()==IN_TOKEN))
             {
                 result = first;
             }
         }
  
         if (debug)
         {
             System.err.println("finish parseVariableInitialization");
         }
 
         return result;
     }
 
     /*
      * TypedIdentifier
      *         Identifier
      *         Identifier : TypeExpression
      */
 
     private TypedIdentifierNode parseTypedIdentifier(int mode)
     {
         if (debug)
         {
             System.err.println("begin parseTypedIdentifier");
         }
 
         TypedIdentifierNode result;
         Node first;
         Node second;
         boolean no_anno;
 
         first = parseIdentifier();
 
         if (lookahead()==COLON_TOKEN)
         {
             shift();
             if(lookahead()==MULT_TOKEN)
             {
                 shift();
                 if (ctx.scriptAssistParsing){
                 	second = nodeFactory.identifier(ASTERISK, false);
                 }
                 else
                 	second = null;
             }
             else
             if(lookahead()==MULTASSIGN_TOKEN)
             {
                 changeLookahead(ASSIGN_TOKEN);  // morph into ordinary looking intializer
                 second = null;
             }
             else
             {
                 // if it's a keyword, that's invalid, throws an error
                 if( errorIfNextTokenIsKeywordInsteadOfTypeExpression() )
                 {
                     second = null; // skip
                 }
                 else
                 {
                     second = parseTypeExpression(mode);
                 }
             }
             no_anno = false;
         }
         else
         {
             second = null;
             no_anno = true;
         }
 
         result = nodeFactory.typedIdentifier(first, second);
         result.no_anno = no_anno;
 
         if (debug)
         {
             System.err.println("finish parseTypedIdentifier");
         }
 
         return result;
     }
 
     /*
      * FunctionDefinition
      *     function FunctionName FunctionCommon
      */
 
     private Node parseFunctionDefinition(AttributeListNode first)
     {
 
         if (debug)
         {
             System.err.println("begin parseFunctionDefinition");
         }
 
         Node result;
         FunctionNameNode second;
         FunctionCommonNode third;
 
         match(FUNCTION_TOKEN);
         second = parseFunctionName();
         third = parseFunctionCommon(second.identifier);
         result = nodeFactory.functionDefinition(ctx, first, second, third);
 
         if (debug)
         {
             System.err.println("finish parseFunctionDefinition");
         }
 
         return result;
     }
 
     /*
      * FunctionName
      *     Identifier
      *     get [no line break] Identifier
      *     set [no line break] Identifier
      */
 
     private FunctionNameNode parseFunctionName()
     {
 
         if (debug)
         {
             System.err.println("begin parseFunctionName");
         }
 
         FunctionNameNode result;
 
         if (lookahead()==GET_TOKEN)
         {
             shift();
             if (lookahead()==LEFTPAREN_TOKEN)  // function get(...) {...}
             {
                 result = nodeFactory.functionName(EMPTY_TOKEN, nodeFactory.identifier(GET,false,ctx.input.positionOfMark()));
             }
             else
             {
                 result = nodeFactory.functionName(GET_TOKEN, parseIdentifier());
             }
         }
         else if (lookahead()==SET_TOKEN)
         {
             shift();
             if (lookahead()==LEFTPAREN_TOKEN)  // function set(...) {...}
             {
                 result = nodeFactory.functionName(EMPTY_TOKEN, nodeFactory.identifier(SET,false,ctx.input.positionOfMark()));
             }
             else
             {
                 result = nodeFactory.functionName(SET_TOKEN, parseIdentifier());
             }
         }
         else
         {
             result = nodeFactory.functionName(EMPTY_TOKEN, parseIdentifier());
         }
 
         if (debug)
         {
             System.err.println("finish parseFunctionName");
         }
         return result;
     }
 
     /*
      * FunctionSignature
      *     ParameterSignature ResultSignature
      *
      * ParameterSignature
      *     ( Parameters )
      */
 
     private FunctionSignatureNode parseFunctionSignature()
     {
 
         if (debug)
         {
             System.err.println("begin parseFunctionSignature");
         }
 
         FunctionSignatureNode result;
         ParameterListNode first;
         Node second;
         boolean no_anno[] = new boolean[1];
         boolean void_anno[] = new boolean[1];
 
         // inlined: parseParameterSignature
 
         match(LEFTPAREN_TOKEN);
         first = parseParameters();
         match(RIGHTPAREN_TOKEN);
         second = parseResultSignature(no_anno,void_anno);
 
         result = nodeFactory.functionSignature(first, second,ctx.input.positionOfMark());
         result.no_anno = no_anno[0];
         result.void_anno = void_anno[0];
 
         if (debug)
         {
             System.err.println("finish parseFunctionSignature");
         }
 
         return result;
     }
 
     /**
      * FunctionSignature ParameterSignature ResultSignature
      * 
      * ParameterSignature ( Parameters )
      */
 
     private FunctionSignatureNode parseConstructorSignature() {
 
         if (debug) {
             System.err.println("begin parseConstructorSignature");
         }
 
         FunctionSignatureNode result;
         ParameterListNode first;
         ListNode second;
         boolean no_anno[] = new boolean[1];
         boolean void_anno[] = new boolean[1];
 
         // inlined: parseParameterSignature
 
         match(LEFTPAREN_TOKEN);
         first = parseParameters();
         match(RIGHTPAREN_TOKEN);
         second = parseConstructorInitializer(no_anno, void_anno);
 
         if( void_anno[0] )
         {
             result = nodeFactory.functionSignature(first, null, ctx.input.positionOfMark());
             result.no_anno = false;
             result.void_anno = true;
         }
         else
         {
             result = nodeFactory.constructorSignature(first, second, ctx.input
                 .positionOfMark());
             result.no_anno = true;
             result.void_anno = false;
         }
         
         if (debug) {
             System.err.println("finish parseConstructorSignature");
         }
 
         return result;
     }
 
     private ListNode parseConstructorInitializer(boolean no_anno[], boolean void_anno[]) {
         if (debug) {
             System.err.println("begin parseConstructorInitializer");
         }
 
         ListNode result = null;
 
         no_anno[0] = true;
         void_anno[0] = false;
         
         if (lookahead()==COLON_TOKEN)
         {
             shift();
             if(lookahead()==VOID_TOKEN)
             {
                 // allow :void, since we allowed that in Flex2.0/AS3
                 shift();
                 no_anno[0] = false;
                 void_anno[0] = true;
                 return null;
             }
             result = parseInitializerList();
             if(lookahead()==SUPER_TOKEN)
             {
                 result = nodeFactory.list(result, parseSuperInitializer() );
             }
         } 
         else 
         {
             result = null;
         }
 
         if (debug) {
             System.err.println("finish parseConstructorInitializer");
         }
 
         return result;
     }
 
     private Node parseSuperInitializer()
     {
         if (debug) {
             System.err.println("begin parseSuperInitializer");
         }
         
         match(SUPER_TOKEN);
         Node result = null;
         Node first = nodeFactory.superExpression(null, ctx.input.positionOfMark());
 
         Node n = parseArguments(first);
         CallExpressionNode call = (n instanceof CallExpressionNode) ? (CallExpressionNode) n
                 : null; // this returns a call expression node
         if (call == null) {
             ctx.internalError("Internal error in parseSuperInitializer()");
         }
         result = nodeFactory.superStatement(call);
 
         if (debug) {
             System.err.println("end parseSuperInitializer");
         }
                 
         return result;
     }
     
     private ListNode parseInitializerList() {
         if (debug) {
             System.err.println("begin parseInitiliazerList");
         }
 
         ListNode result = null;
 
         Node first;
         if(lookahead()==SUPER_TOKEN)
         {
             result = null;
         }
         else
         {
             first = parseInitializer();
             result = parseInitializerListPrime(nodeFactory.list(null, first));
         }
         
         if (debug) {
             System.err.println("end parseInitializerList");
         }
 
         return result;
     }
 
     private ListNode parseInitializerListPrime(ListNode first) {
         if (debug) {
             System.err.println("begin parseInitializerListPrime");
         }
 
         ListNode result;
 
         if (lookahead()==COMMA_TOKEN)
         {
             shift();
             if(lookahead()==SUPER_TOKEN)
             {
                 result = first;
             }
             else
             {
                 Node second;
     
                 second = parseInitializer();
                 result = parseInitializerListPrime(nodeFactory.list(first, second));
             }
         } else {
             result = first;
         }
 
         if (debug) {
             System.err.println("end parseInitializerListPrime");
         }
 
         return result;
     }
 
     private Node parseInitializer()
     {
         if (debug)
         {
             System.err.println("begin parseInitializer");
         }
 
         Node result = null;
 
         // Todo: this should be parsePattern();
         Node first = parseIdentifier();
         Node second = null;
         if (lookahead()==ASSIGN_TOKEN)
         {
             second = parseVariableInitialization(allowIn_mode);
         }
         else
         {
             // error
             ctx.error(first.pos(), kError_Parser_DefinitionOrDirectiveExpected);
         }
         
         result = nodeFactory.assignmentExpression(first, ASSIGN_TOKEN, second);
         
         if (debug)
         {
             System.err.println("end parseInitializer");
         }
 
         return result;
     }
 
     /*
      * Parameters
      *         empty
      *         NonemptyParameters
      */
 
     private ParameterListNode parseParameters()
     {
 
         if (debug)
         {
             System.err.println("begin parseParameters");
         }
 
         ParameterListNode result;
 
         if (lookahead()==RIGHTPAREN_TOKEN)
         {
             result = null;
         }
         else
         {
             result = parseNonemptyParameters(null);
         }
 
         if (debug)
         {
             System.err.println("finish parseParameters");
         }
 
         return result;
     }
 
     /*
      * NonemptyParameters
      *     ParameterInit
      *     ParameterInit , NonemptyParameters
      *     RestParameters
      */
 
     private ParameterListNode parseNonemptyParameters(ParameterListNode first)
     {
 
         if (debug)
         {
             System.err.println("begin parseNonemptyParameters");
         }
 
         ParameterListNode result;
         ParameterNode second;
 
         if (lookahead()==TRIPLEDOT_TOKEN)
         {
             result = nodeFactory.parameterList(first, parseRestParameter());
         }
         else
         {
             second = parseParameter();
             if (lookahead()==COMMA_TOKEN)
             {
                 shift();
                 result = parseNonemptyParameters(nodeFactory.parameterList(first, second));
             }
             else if (lookahead()==ASSIGN_TOKEN)
             {
                 shift();
                 second.init = parseNonAssignmentExpression(allowIn_mode);
                 if (lookahead()==COMMA_TOKEN)
                 {
                     shift();
                     result = parseNonemptyParameters(nodeFactory.parameterList(first, second));
                 }
                 else
                 {
                     result = nodeFactory.parameterList(first, second);
                 }
             }
             else
             {
                 result = nodeFactory.parameterList(first, second);
             }
         }
 
         if (debug)
         {
             System.err.println("finish parseNonemptyParameters");
         }
 
         return result;
     }
 
     /*
      * RestParameter
      *     ...
      *     ... ParameterAttributes Identifier
      */
 
     private ParameterNode parseRestParameter()
     {
 
         if (debug)
         {
             System.err.println("begin parseRestParameter");
         }
 
         ParameterNode result;
         ParameterNode first;
 
         match(TRIPLEDOT_TOKEN);
 
         final int lt = lookahead();
         if (lt==CONST_TOKEN || lt==IDENTIFIER_TOKEN || lt==GET_TOKEN || lt==SET_TOKEN)
         {
             first = parseParameter();
         }
         else
         {
             first = null;
         }
 
         result = nodeFactory.restParameter(first,ctx.input.positionOfMark());
 
         if (debug)
         {
             System.err.println("finish parseRestParameter");
         }
 
         return result;
     }
 
     /*
      * Parameter
      *     Identifier
      *     Identifier : TypeExpression[allowIn]
      */
 
     private ParameterNode parseParameter()
     {
 
         if (debug)
         {
             System.err.println("begin parseParameter");
         }
 
         ParameterNode result;
         int first;
         IdentifierNode second;
         Node third;
         boolean no_anno = false;
 
         if (HAS_CONSTPARAMETERS && lookahead()==CONST_TOKEN)
         {
             first = match(CONST_TOKEN);
         }
         else
         {
             first = VAR_TOKEN;
         }
 
         second = parseIdentifier();
         if (lookahead()==COLON_TOKEN)
         {
             shift();
             if(lookahead()==MULT_TOKEN)
             {
                 shift();
                 second.setOrigTypeToken(MULT_TOKEN);
                 third = null;
             }
             else
             if(lookahead()==MULTASSIGN_TOKEN)
             {
                 changeLookahead(ASSIGN_TOKEN);  // morph into ordinary looking intializer
                 second.setOrigTypeToken(MULTASSIGN_TOKEN);
                 third = null;
             }
             else
             {
                 // if it's a keyword, that's invalid, throws an error
                 if( errorIfNextTokenIsKeywordInsteadOfTypeExpression() )
                 {
                     third = null; // skip
                 }
                 else
                 {
                     third = parseTypeExpression(allowIn_mode);
                 }
             }
         }
         else
         {
             third = null;
             no_anno = true;
         }
 
         result = nodeFactory.parameter(first, second, third);
         result.no_anno = no_anno;
 
         if (debug)
         {
             System.err.println("finish parseParameter");
         }
 
         return result;
     }
 
     /*
      * ResultSignature
      *     empty
      *     : TypeExpression[allowIn]
      */
 
     private Node parseResultSignature(boolean no_anno[],boolean void_anno[])
     {
 
         if (debug)
         {
             System.err.println("begin parseResultSignature");
         }
 
         Node result;
 
         no_anno[0] = false;
         void_anno[0] = false;
 
         if (lookahead()==COLON_TOKEN)
         {
             shift();
             if(lookahead()==MULT_TOKEN)
             {
                 shift();
                 if (ctx.scriptAssistParsing)
                 	result = nodeFactory.identifier(ASTERISK,false,ctx.input.positionOfMark());
                 else
                 	result = null;
             }
             else if(lookahead()==MULTASSIGN_TOKEN) // do this here for better (potential) syntax error reporting
             {         
                 changeLookahead(ASSIGN_TOKEN); // morph into an assign token
                 result = null;  // same as no annotation
             }
             else
             if(lookahead()==VOID_TOKEN)
             {
                 shift();
                 result = null;
                 void_anno[0] = true;
             }
             else
             {
                 // if it's a keyword, that's invalid, throws an error
                 errorIfNextTokenIsKeywordInsteadOfTypeExpression();
                 result = parseTypeExpression(allowIn_mode);
             }
         }
         else
         {
             result = null;
             no_anno[0] = true;
         }
 
         if (debug)
         {
             System.err.println("finish parseResultSignature");
         }
 
         return result;
     }
 
     /**
      * This looks to see if the next token is a reserved keyword, if so then an error is thrown
      * for a keyword being used where we expected a type expression/identifier
      */
     
     private boolean errorIfNextTokenIsKeywordInsteadOfTypeExpression() 
     {
         if (lookahead()==VOID_TOKEN)
         {
             error(syntax_error, kError_Parser_keywordInsteadOfTypeExpr, scanner.getTokenText(nextToken));
             match(VOID_TOKEN);
             return true;
         }
         else
         if (lookahead(xmlid_tokens, xmlid_tokens_count) && lookahead() != IDENTIFIER_TOKEN)
         {
             error(syntax_error, kError_Parser_keywordInsteadOfTypeExpr, scanner.getTokenText(nextToken));
             match(xmlid_tokens, xmlid_tokens_count);
             return true;
         }
         else
         {
             if( ctx.dialect(8) && lookahead() == IDENTIFIER_TOKEN && scanner.getTokenText(nextToken).equals("Object") )
             {
                 error(syntax_error, kError_ColonObjectAnnoOutOfService);
                 return true;
             }
             return false;
         }
     }
 
     /*
      * ClassDefinition
      *     class Identifier Inheritance Block
      */
 
     private Node parseClassDefinition(AttributeListNode attrs, int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseClassDefinition");
         }
 
         Node result;
         ClassNameNode first;
 
         match(CLASS_TOKEN);
 
         if( block_kind_stack.last() != PACKAGE_TOKEN && block_kind_stack.last() != EMPTY_TOKEN )
         {
             error(syntax_error,kError_InvalidClassNesting);
         }
 
         block_kind_stack.add(CLASS_TOKEN);
 
         first = parseClassName();
 
         String temp_class_name = current_class_name;
         current_class_name = first.ident.name;
 
         {
             InheritanceNode second;
             StatementListNode third;
 
             if( first.pkgname != null )
             {
                 nodeFactory.startPackage(ctx,null,first.pkgname);
             }
 
             nodeFactory.StartClassDefs();
             second = parseInheritance();
             third  = parseBlock();
             result = nodeFactory.classDefinition(ctx, attrs, first.ident, second, third, first.non_nullable);
             block_kind_stack.removeLast();
 
             current_class_name = temp_class_name;
             
             if( first.pkgname != null )
             {
                 nodeFactory.finishPackage(ctx,null);
             }
         }
 
         if (debug)
         {
             System.err.println("finish parseClassDefinition");
         }
 
         return result;
     }
 
     /*
      * ClassName
      *     Identifier
      *     PackageName . Identifier
      */
 
     private ClassNameNode parseClassName()
     {
 
         if (debug)
         {
             System.err.println("begin parseClassName");
         }
 
         ClassNameNode result;
         IdentifierNode first;
         first = parseIdentifier();
         if (HAS_COMPOUNDCLASSNAMES)
         {
             result = parseClassNamePrime(first);
         }
         else
         {
             result = nodeFactory.className(null, first);
         }
 
         if( ctx.statics.es4_nullability )
         {
             if(lookahead()==NOT_TOKEN)
             {
                 shift();
                 result.non_nullable = true;
             }
             else if (lookahead()==QUESTIONMARK_TOKEN)
             {
                 shift();
                 // do nothing, classes are by default nullable.
             }
         }
         
         if (debug)
         {
             System.err.println("finish parseClassName");
         }
 
         return result;
     }
 
     private ClassNameNode parseClassNamePrime(IdentifierNode second)
     {
 
         if (debug)
         {
             System.err.println("begin parseClassNamePrime");
         }
 
         ClassNameNode result;
         PackageIdentifiersNode first = null;
 
         while (lookahead()==DOT_TOKEN)
         {
             shift();
             first = nodeFactory.packageIdentifiers(first, second, true);
             second = parseIdentifier();
         }
 
         PackageNameNode pkgname = first!=null?nodeFactory.packageName(first):null;
         result = nodeFactory.className(pkgname,second);
 
         if (debug)
         {
             System.err.println("finish parseClassNamePrime");
         }
 
         return result;
     }
 
     /*
      * Inheritance
      *     empty
      *     extends TypeExpressionallowIn
      *     implements TypeExpressionList
      *     extends TypeExpressionallowIn implements TypeExpressionList
      */
 
     private InheritanceNode parseInheritance()
     {
 
         if (debug)
         {
             System.err.println("begin parseInheritance");
         }
 
         InheritanceNode result = null;
         Node first;
         ListNode second;
 
         if (lookahead()==EXTENDS_TOKEN)
         {
             shift();
             first = parseTypeName();
         }
         else
         {
             first = null;
         }
 
         if (lookahead()==IMPLEMENTS_TOKEN)
         {
             shift();
             second = parseTypeNameList();
         }
         else
         {
             second = null;
         }
 
         if (!(first == null && second == null))
         {
             result = nodeFactory.inheritance(first, second);
         }
 
         if (debug)
         {
             System.err.println("finish parseInheritance");
         }
 
         return result;
     }
 
     /*
      * TypeExpressionList
      *     TypeExpression[allowin] TypeExpressionListPrime
      *
      * TypeExpressionListPrime:
      *     , TypeExpression[allowin] TypeExpressionListPrime
      *     empty
      */
 
     private ListNode parseTypeExpressionList()
     {
 
         if (debug)
         {
             System.err.println("begin parseTypeExpressionList");
         }
 
         ListNode result;
         Node first;
 
         first = parseTypeExpression(allowIn_mode);
         result = parseTypeExpressionListPrime(nodeFactory.list(null, first));
 
         if (debug)
         {
             System.err.println("finish parseTypeExpressionList");
         }
 
         return result;
     }
 
     private ListNode parseTypeExpressionListPrime(ListNode first)
     {
 
         if (debug)
         {
             System.err.println("begin parseTypeExpressionListPrime");
         }
 
         ListNode result;
 
         if (lookahead()==COMMA_TOKEN)
         {
             shift();
 
             Node second;
 
             second = parseTypeExpression(allowIn_mode);
             result = parseTypeExpressionListPrime(nodeFactory.list(first, second));
 
         }
         else
         {
             result = first;
         }
 
         if (debug)
         {
             System.err.println("finish parseTypeExpressionListPrime'");
         }
 
         return result;
     }
 
     /*
      * ExtendsList
      *     empty
      *     extends TypeExpressionList
      */
 
     private ListNode parseExtendsList()
     {
 
         if (debug)
         {
             System.err.println("begin parseExtendsList");
         }
 
         ListNode result;
 
         if (lookahead()==EXTENDS_TOKEN)
         {
             shift();
             result = parseTypeNameList();
         }
         else
         {
             result = null;
         }
 
         if (debug)
         {
             System.err.println("finish parseExtendsList");
         }
 
         return result;
     }
 
     /*
      * InterfaceDefinition
      *     interface ClassName ExtendsList Block
 
      */
 
     private Node parseInterfaceDefinition(AttributeListNode attrs, int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseInterfaceDefinition");
         }
 
         Node result;
         ClassNameNode first;
 
         match(INTERFACE_TOKEN);
 
         block_kind_stack.add(INTERFACE_TOKEN);
 
         first = parseClassName();
 
         {
             ListNode second;
             StatementListNode third;
 
             if( first.pkgname != null )
             {
                 nodeFactory.startPackage(ctx,null,first.pkgname);
             }
 
             second = parseExtendsList();
             third  = parseBlock();
             result = nodeFactory.interfaceDefinition(ctx, attrs, first.ident, second, third);
 
             block_kind_stack.removeLast();
 
             if( first.pkgname != null )
             {
                 nodeFactory.finishPackage(ctx,null);
             }
 
         }
 
         if (debug)
         {
             System.err.println("finish parseInterfaceDefinition");
         }
 
         return result;
     }
 
     /*
      * NamespaceDefinition
      *     namespace Identifier
      */
 
     private NamespaceDefinitionNode parseNamespaceDefinition(AttributeListNode first)
     {
 
         if (debug)
         {
             System.err.println("begin parseNamespaceDefinition");
         }
 
         if( first != null && first.items.size() == 1 && first.hasAttribute("config"))
         {
         	first = null;
         	return parseConfigNamespaceDefinition(first);
         }
 
         NamespaceDefinitionNode result;
         IdentifierNode second = null;
         Node third = null;
 
         match(NAMESPACE_TOKEN);
 //        if (!lookahead(ASSIGN_TOKEN))
         {
             second = parseIdentifier();
         }
 /*        else
         {
             // ISSUE: error if not the default namespace
         }
 */
         if (lookahead()==ASSIGN_TOKEN)
         {
             shift();
             if( ctx.statics.es4_nullability )
             {
                 if(lookahead()==STRINGLITERAL_TOKEN)
                 {
                     boolean[] is_single_quoted = new boolean[1];
                     String enclosedText = scanner.getStringTokenText(match(STRINGLITERAL_TOKEN), is_single_quoted);
                     third = nodeFactory.literalString(enclosedText, ctx.input.positionOfMark(), is_single_quoted[0] );
                 }
                 else
                 {
                     third = parseSimpleTypeIdentifier();
                 }
             }
             else
             {
                 third = parseAssignmentExpression(allowIn_mode);
             }
         }
         
         result = nodeFactory.namespaceDefinition(first, second, third);
 
         if (debug)
         {
             System.err.println("finish parseNamespaceDefinition");
         }
 
         return result;
     }
 
     /*
      * NamespaceDefinition
      *     namespace Identifier
      */
 
     private NamespaceDefinitionNode parseConfigNamespaceDefinition(AttributeListNode first)
     {
 
         if (debug)
         {
             System.err.println("begin parseConfigNamespaceDefinition");
         }
 
         NamespaceDefinitionNode result;
 
         match(NAMESPACE_TOKEN);
         IdentifierNode second = parseIdentifier();
         
         result = nodeFactory.configNamespaceDefinition(first, second, -1);
 
         assert config_namespaces.size() > 0; 
         config_namespaces.last().add(result.name.name);
         
         if (debug)
         {
             System.err.println("finish parseConfigNamespaceDefinition");
         }
 
         return result;
     }
     
     public static UseDirectiveNode generateAs3UseDirective(Context ctx)
     {
         NodeFactory nodeFactory = ctx.getNodeFactory();
         IdentifierNode as3Identifier = nodeFactory.identifier(AS3, false);
         Namespaces namespaces = new Namespaces();
         NamespaceValue namespaceValue = new NamespaceValue();
         namespaces.add(namespaceValue);
         ReferenceValue referenceValue = new ReferenceValue(ctx, null, AS3, namespaces);
         referenceValue.setIsAttributeIdentifier(false);
         as3Identifier.ref = referenceValue;
         return nodeFactory.useDirective(null,nodeFactory.memberExpression(null,nodeFactory.getExpression(as3Identifier)));
     }
 
     /*
      * PackageDefinition
      *     package Block
      *     package PackageName Block
      */
     private PackageDefinitionNode parsePackageDefinition()
     {
 
         if (debug)
         {
             System.err.println("begin parsePackageDefinition");
         }
 
         if (within_package)
             error(kError_NestedPackage);
 
         within_package = true;
 
         PackageDefinitionNode result;
 
         // Init the default xml namespace
 
         nodeFactory.dxns = null;
 
         block_kind_stack.add(PACKAGE_TOKEN);
 
         match(PACKAGE_TOKEN);
 
         assert config_namespaces.size() > 0; 
         HashSet<String> conf_ns = new HashSet<String>(config_namespaces.last().size());
         conf_ns.addAll(config_namespaces.last());
         config_namespaces.push_back(conf_ns);
         
         if (lookahead()==LEFTBRACE_TOKEN)
         {
             result = nodeFactory.startPackage(ctx, null, null);
             // Careful, when adding synthetic UseDirectiveNodes they must be created
             // in between calls to start/finishPackage.  Otherwise they won't have their
             // pkgdef ptr set up correctly, and things will go mysteriously awry later.
             Node udn = generateAs3UseDirective(ctx);
 
             ObjectList<UseDirectiveNode> udns = null;
             if ( !ctx.statics.use_namespaces.isEmpty() )
             {
                 udns = new ObjectList<UseDirectiveNode>();
                 for (String useName : ctx.statics.use_namespaces )
                 {
                     udns.add(nodeFactory.useDirective(null,nodeFactory.memberExpression(null,nodeFactory.getExpression(nodeFactory.identifier(useName)))));
                 }
             }
             Node idn = null;
             if( ctx.statics.es4_vectors )
             {
                 PackageIdentifiersNode pin = nodeFactory.packageIdentifiers(null, nodeFactory.identifier(__AS3__, false), true);
                 pin = nodeFactory.packageIdentifiers(pin, nodeFactory.identifier(VEC, false), true);
                 pin = nodeFactory.packageIdentifiers(pin, nodeFactory.identifier(VECTOR, false), true);
                 idn = nodeFactory.importDirective(null, nodeFactory.packageName(pin), null, ctx);
             }
             result = nodeFactory.finishPackage(ctx, parseBlock());
             if ((ctx.dialect(10) /*|| ctx.dialect(11)*/) && result != null)
         	{
         		result.statements.items.add(1,udn);  // insert after the first statment, which is the starting package definition
         	}
             if( ctx.statics.es4_vectors && result != null)
             {
                 result.statements.items.add(1, idn);
             }
             if( udns != null && result != null)
             {
                 for( UseDirectiveNode usenode : udns )
                 {
                     result.statements.items.add(1,usenode);
                 }
             }
 
         }
         else
         {
             PackageNameNode first = parsePackageName(false);
             result = nodeFactory.startPackage(ctx, null, first);
             // Careful, when adding synthetic UseDirectiveNodes they must be created
             // in between calls to start/finishPackage.  Otherwise they won't have their
             // pkgdef ptr set up correctly, and things will go mysteriously awry later.
             Node udn = generateAs3UseDirective(ctx);
             ObjectList<UseDirectiveNode> udns = null;
             if ( !ctx.statics.use_namespaces.isEmpty() )
             {
                 udns = new ObjectList<UseDirectiveNode>();
                 for (String useName : ctx.statics.use_namespaces )
                 {
                     udns.add(nodeFactory.useDirective(null,nodeFactory.memberExpression(null,nodeFactory.getExpression(nodeFactory.identifier(useName)))));
                 }
             }
             Node idn = null;
             if( ctx.statics.es4_vectors )
             {
                 PackageIdentifiersNode pin = nodeFactory.packageIdentifiers(null, nodeFactory.identifier(__AS3__, false), true);
                 pin = nodeFactory.packageIdentifiers(pin, nodeFactory.identifier(VEC, false), true);
                 pin = nodeFactory.packageIdentifiers(pin, nodeFactory.identifier(VECTOR, false), true);
                 idn = nodeFactory.importDirective(null, nodeFactory.packageName(pin), null, ctx);
             }
             result = nodeFactory.finishPackage(ctx, parseBlock());
             if ((ctx.dialect(10) /*|| ctx.dialect(11)*/) && result != null)
             {
         		result.statements.items.add(1,udn);  // insert after the first statment, which is the starting package definition
             }
             if( udns != null && result != null)
             {
                 for( UseDirectiveNode usenode : udns )
                 {
                     result.statements.items.add(1,usenode);
                 }
             }
             if( ctx.statics.es4_vectors && result != null)
             {
                 result.statements.items.add(1, idn);
             }
        }
 
         block_kind_stack.removeLast();
         config_namespaces.pop_back();
         
         if (debug)
         {
             System.err.println("finish parsePackageDefinition");
         }
 
         within_package = false;
 
         return result;
     }
 
    public StatementListNode parseConfigValues()
     {
         StatementListNode configs = null;
         String config_code = ctx.getConfigVarCode();
         if( config_code != null )
         {
             Scanner orig = scanner;
             InputBuffer input = ctx.input;
             int orig_lastToken = lastToken;
             int orig_nextToken = nextToken;
             
             scanner = new Scanner(ctx, config_code, "");
             scanner.input.report_pos = false;  // Don't give position to generated nodes, since they don't have corresponding source
 
             configs = parseDirectives(null, null);
 
             scanner = orig;
             ctx.input = input;
             lastToken = orig_lastToken;
             nextToken = orig_nextToken;
         }
         return configs;
     }
     
     /*
      * PackageName
      *     Identifier
      *     PackageName . Identifier
      */
 
     private PackageNameNode parsePackageName(boolean isDefinition)
     {
         if (debug)
         {
             System.err.println("begin parsePackageName");
         }
 
         PackageNameNode result;
 
         if (lookahead()==STRINGLITERAL_TOKEN)
         {
             boolean[] is_single_quoted = new boolean[1];
             String enclosedText = scanner.getStringTokenText(match(STRINGLITERAL_TOKEN), is_single_quoted);
             LiteralStringNode first = nodeFactory.literalString(enclosedText, ctx.input.positionOfMark(), is_single_quoted[0]);
             result = nodeFactory.packageName(first);
         }
         else
         {
             PackageIdentifiersNode first;
             first = nodeFactory.packageIdentifiers(null, parseIdentifier(), isDefinition);
             result = parsePackageNamePrime(first, isDefinition);
         }
 
         if (debug)
         {
             System.err.println("finish parsePackageName");
         }
 
         return result;
     }
 
     private PackageNameNode parsePackageNamePrime(PackageIdentifiersNode first, boolean isDefinition)
     {
         if (debug)
         {
             System.err.println("begin parsePackageNamePrime");
         }
 
         while (lookahead()==DOT_TOKEN)
         {
             shift();
             first = nodeFactory.packageIdentifiers(first, parsePropertyIdentifier(), isDefinition);
         }
 
         PackageNameNode result = nodeFactory.packageName(first);
 
         if (debug)
         {
             System.err.println("finish parsePackageNamePrime");
         }
 
         return result;
     }
 
     /** <PRE>
      * Program
      * Directives
      * PackageDefinition Program
      * 
      * <B>CLEARS unused buffers before returning</B></PRE>
      */
     public ProgramNode parseProgram()
     {
         if (debug)
         {
             System.err.println("begin parseProgram");
         }
 
         StatementListNode first = null;
         StatementListNode second = null;
 
         StatementListNode configs = null;
         if( !parsing_include )
         {
             config_namespaces.push_back(new HashSet<String>());
             config_namespaces.last().add(CONFIG);
             configs = parseConfigValues();
         }
 
         // Default config namespace
         //config_namespaces.last().add("CONFIG");
         
         while (lookahead()==PACKAGE_TOKEN || lookahead()==DOCCOMMENT_TOKEN)
         {
             MetaDataNode meta=null;
             if( lookahead()==DOCCOMMENT_TOKEN || lookahead()==LEFTBRACKET_TOKEN || lookahead()==XMLLITERAL_TOKEN )
             {
                 meta = parseMetaData();
                 second = nodeFactory.statementList(second,meta); 
             }
 
             if (lookahead()==PACKAGE_TOKEN)
             {
                 PackageDefinitionNode pkgdef = parsePackageDefinition();
                 first = nodeFactory.statementList(first, pkgdef );
                 if (meta != null)
                 {
                     meta.def = pkgdef;
                     pkgdef.addMetaDataNode(meta);
                 }
                 
                 // Merge the package statements with the existing statements
                 if (pkgdef != null) // package is NULL if there was an erronously nested package.  Error has already been generated
                 {
                     second = nodeFactory.statementList(second,pkgdef.statements);
                 }
             }
         }
 
         second = parseDirectives(null/*attrs*/,second);
 
         if( (ctx.dialect(10) /*|| ctx.dialect(11)*/) && !parsing_include && second != null )  // don't insert a statement for includes because will screw up metadata parsing
         {
             Node udn = generateAs3UseDirective(ctx);
             second.items.add(0,udn);
         }
         if ( !ctx.statics.use_namespaces.isEmpty() && !parsing_include && second != null)
         {
             for (String useName : ctx.statics.use_namespaces )
             {
                 Node udn2 = nodeFactory.useDirective(null,nodeFactory.memberExpression(null,nodeFactory.getExpression(nodeFactory.identifier(useName))));
                 second.items.add(0,udn2);
             }
         }
         if( ctx.statics.es4_vectors && !parsing_include && second != null)
         {
             PackageIdentifiersNode pin = nodeFactory.packageIdentifiers(null, nodeFactory.identifier(__AS3__, false), true);
             pin = nodeFactory.packageIdentifiers(pin, nodeFactory.identifier(VEC, false), true);
             pin = nodeFactory.packageIdentifiers(pin, nodeFactory.identifier(VECTOR, false), true);
             Node idn = nodeFactory.importDirective(null, nodeFactory.packageName(pin), null, ctx);
             second.items.add(0, idn);
         }
         ProgramNode result = nodeFactory.program(ctx,second,ctx.input.positionOfMark());
         match(EOS_TOKEN);
 
         if (ctx.scriptAssistParsing){//the parser comments are needed after the parser is gone
     		for (ListIterator<Node> it = comments.listIterator(); it.hasNext(); )
     		{
     			ctx.comments.add(it.next());
     		}
         }
         
         clearUnusedBuffers();
         
 
         if( !parsing_include )
         {
             if( result != null )
             {
             	// Add tool chain added values
             	if( configs != null )
             		result.statements.items.addAll(0, configs.items);
             	NamespaceDefinitionNode configdef = nodeFactory.configNamespaceDefinition(null, nodeFactory.identifier(CONFIG, false), -1);
             	// Add the default CONFIG namespace
             	result.statements.items.add(0, configdef);
             }
             config_namespaces.pop_back();
         }
         
         if (debug)
         {
             System.err.println("finish parseProgram");
         }
         
         return result;
     }
 
     private void clearUnusedBuffers() {
         comments.clear();
         scanner.clearUnusedBuffers();
         scanner = null;
         comments = null;
     }
 }
