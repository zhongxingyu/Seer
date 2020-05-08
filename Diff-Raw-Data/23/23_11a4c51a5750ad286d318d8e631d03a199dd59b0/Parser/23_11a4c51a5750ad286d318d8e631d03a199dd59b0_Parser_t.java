 package descent.internal.compiler.parser;
 
 import static descent.internal.compiler.parser.LINK.LINKc;
 import static descent.internal.compiler.parser.LINK.LINKcpp;
 import static descent.internal.compiler.parser.LINK.LINKd;
 import static descent.internal.compiler.parser.LINK.LINKdefault;
 import static descent.internal.compiler.parser.LINK.LINKpascal;
 import static descent.internal.compiler.parser.LINK.LINKwindows;
 import static descent.internal.compiler.parser.TOK.TOKalias;
 import static descent.internal.compiler.parser.TOK.TOKand;
 import static descent.internal.compiler.parser.TOK.TOKandand;
 import static descent.internal.compiler.parser.TOK.TOKassert;
 import static descent.internal.compiler.parser.TOK.TOKassign;
 import static descent.internal.compiler.parser.TOK.TOKauto;
 import static descent.internal.compiler.parser.TOK.TOKcase;
 import static descent.internal.compiler.parser.TOK.TOKcatch;
 import static descent.internal.compiler.parser.TOK.TOKclass;
 import static descent.internal.compiler.parser.TOK.TOKcolon;
 import static descent.internal.compiler.parser.TOK.TOKcomma;
 import static descent.internal.compiler.parser.TOK.TOKdefault;
 import static descent.internal.compiler.parser.TOK.TOKdelegate;
 import static descent.internal.compiler.parser.TOK.TOKdot;
 import static descent.internal.compiler.parser.TOK.TOKdotdotdot;
 import static descent.internal.compiler.parser.TOK.TOKelse;
 import static descent.internal.compiler.parser.TOK.TOKenum;
 import static descent.internal.compiler.parser.TOK.TOKeof;
 import static descent.internal.compiler.parser.TOK.TOKequal;
 import static descent.internal.compiler.parser.TOK.TOKfinally;
 import static descent.internal.compiler.parser.TOK.TOKfunction;
 import static descent.internal.compiler.parser.TOK.TOKidentifier;
 import static descent.internal.compiler.parser.TOK.TOKidentity;
 import static descent.internal.compiler.parser.TOK.TOKif;
 import static descent.internal.compiler.parser.TOK.TOKimport;
 import static descent.internal.compiler.parser.TOK.TOKinout;
 import static descent.internal.compiler.parser.TOK.TOKint32v;
 import static descent.internal.compiler.parser.TOK.TOKinterface;
 import static descent.internal.compiler.parser.TOK.TOKis;
 import static descent.internal.compiler.parser.TOK.TOKlbracket;
 import static descent.internal.compiler.parser.TOK.TOKlcurly;
 import static descent.internal.compiler.parser.TOK.TOKlparen;
 import static descent.internal.compiler.parser.TOK.TOKmodule;
 import static descent.internal.compiler.parser.TOK.TOKnew;
 import static descent.internal.compiler.parser.TOK.TOKnot;
 import static descent.internal.compiler.parser.TOK.TOKnotidentity;
 import static descent.internal.compiler.parser.TOK.TOKon_scope_exit;
 import static descent.internal.compiler.parser.TOK.TOKon_scope_failure;
 import static descent.internal.compiler.parser.TOK.TOKon_scope_success;
 import static descent.internal.compiler.parser.TOK.TOKor;
 import static descent.internal.compiler.parser.TOK.TOKoror;
 import static descent.internal.compiler.parser.TOK.TOKplusplus;
 import static descent.internal.compiler.parser.TOK.TOKquestion;
 import static descent.internal.compiler.parser.TOK.TOKrbracket;
 import static descent.internal.compiler.parser.TOK.TOKrcurly;
 import static descent.internal.compiler.parser.TOK.TOKreserved;
 import static descent.internal.compiler.parser.TOK.TOKrparen;
 import static descent.internal.compiler.parser.TOK.TOKsemicolon;
 import static descent.internal.compiler.parser.TOK.TOKslice;
 import static descent.internal.compiler.parser.TOK.TOKstring;
 import static descent.internal.compiler.parser.TOK.TOKstruct;
 import static descent.internal.compiler.parser.TOK.TOKthis;
 import static descent.internal.compiler.parser.TOK.TOKtilde;
 import static descent.internal.compiler.parser.TOK.TOKtypedef;
 import static descent.internal.compiler.parser.TOK.TOKtypeof;
 import static descent.internal.compiler.parser.TOK.TOKunion;
 import static descent.internal.compiler.parser.TOK.TOKwhile;
 import static descent.internal.compiler.parser.TOK.TOKxor;
 import static descent.internal.compiler.parser.TY.Taarray;
 import static descent.internal.compiler.parser.TY.Tfunction;
 import static descent.internal.compiler.parser.TY.Tsarray;
 
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import org.eclipse.core.runtime.Assert;
 
 import descent.core.compiler.IProblem;
 import descent.core.dom.AST;
 import descent.core.dom.CodeComment;
 import descent.core.dom.Comment;
 import descent.core.dom.DDocComment;
 import descent.core.dom.Pragma;
 
 public class Parser extends Lexer {
 	
 	public final static boolean LTORARRAYDECL = true;
 	
 	public final static int PSsemi = 1;		// empty ';' statements are allowed
 	public final static int PSscope = 2;	// start a new scope
 	public final static int PScurly = 4;	// { } statement is required
 	public final static int PScurlyscope = 8;	// { } starts a new scope
 
 	private ModuleDeclaration md;
 	private int inBrackets;	
 	
 	AST ast;
 	List<Comment> comments;
 	List<Pragma> pragmas;
 	private int lastDocCommentRead = 0;
 	private boolean appendLeadingComments = true;
 	
 	private LINK linkage = LINK.LINKd;
 
 	public Parser(AST ast, String source) {
 		this(ast, source, 0, source.length());
 	}
 	
 	public Parser(AST ast, char[] source) {
 		this(ast, source, 0, source.length);
 	}
 	
 	public Parser(AST ast, String source, int offset, int length) {
 		this(ast, source.toCharArray(), offset, length);
 	}
 	
 	public Parser(AST ast, char[] source, int offset, 
 			int length) {
 		super(source, offset, length, 
 				true /* tokenize comments */, 
 				true /* tokenize pragmas */,
 				false /* don't tokenize whitespace */, 
 				true /* record line separators */,
 				ast.apiLevel());
 		
 		this.ast = ast;
 		comments = new ArrayList<Comment>();
 		pragmas = new ArrayList<Pragma>();
 		nextToken();
 	}
 	
 	public Module parseModuleObj() {
 		Module module = new Module(loc);
 		module.members = parseModule();
 		module.md = md;
 		module.comments = comments.toArray(new Comment[comments.size()]);
 		module.pragmas = pragmas.toArray(new Pragma[pragmas.size()]);
 		module.lineEnds = getLineEnds();
 		module.problems = problems;
 		module.ast = ast;
 		return module;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<Dsymbol> parseModule() {
 	    List<Dsymbol> decldefs = new ArrayList<Dsymbol>();
 
 		// ModuleDeclation leads off
 		if (token.value == TOKmodule) {
 			int start = token.ptr;
 			
 			List<DDocComment> moduleDocComments = getLastDocComments();
 			
 			nextToken();
 			if (token.value != TOKidentifier) {
 				parsingErrorDeleteToken(prevToken);
 				
 				decldefs = parseDeclDefs(false);
 				if (token.value != TOKeof) {
 					parsingErrorDeleteToken(token);
 				}
 				return decldefs;
 			} else {
 				List<IdentifierExp> a = null;
 				IdentifierExp id = null;
 				id = newIdentifierExp();
 				while (nextToken() == TOKdot) {
 					if (a == null) {
 						a = new ArrayList<IdentifierExp>();
 					}
 					a.add(id);
 					nextToken();
 					if (token.value != TOKidentifier) {
 						parsingErrorInsertTokenAfter(prevToken, ";");
 						
 						decldefs = parseDeclDefs(false);
 						if (token.value != TOKeof) {
 							parsingErrorDeleteToken(token);
 						}
 						return decldefs;
 					}
 					id = newIdentifierExp();
 				}
 
 				md = new ModuleDeclaration(a, id);
 				md.setSourceRange(start, token.ptr + token.len - start);
 				md.preDdocs = moduleDocComments;
 				adjustLastDocComment();
 
 				if (token.value != TOKsemicolon) {
 					setMalformed(md);
 					// TODO ast nodes flags
 					// setRecovered(md.name);
 					parsingErrorInsertTokenAfter(prevToken, ";");
 				} else {
 					nextToken();
 				}
 				
 				attachLeadingComments(md);
 				adjustPossitionAccordingToComments(md, md.preDdocs, md.postDdoc);
 			}
 		}
 
 		decldefs = parseDeclDefs(false);
 		if (token.value != TOKeof) {
 			parsingErrorDeleteToken(token);
 			// goto Lerr;
 			return parseModule_LErr(decldefs);
 		}
 		return decldefs;
 	}
 	
 	private List parseModule_LErr(List decldefs) {
 		while (token.value != TOKsemicolon && token.value != TOKeof)
 	    	nextToken();
 	    nextToken();
 	    return decldefs;
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<Dsymbol> parseDeclDefs(boolean once) {
 		Dsymbol s = null;
 		List<Dsymbol> decldefs;
 		List<Dsymbol> a = new ArrayList<Dsymbol>();
 		List<Dsymbol> aelse;
 		PROT prot;
 		int stc;
 		boolean[] isSingle = new boolean[1];
 		
 		decldefs = new ArrayList<Dsymbol>();
 		do {
 			List<DDocComment> lastComments = getLastDocComments();
			isSingle[0] = false;
 			
 			int start = token.ptr;
 			switch (token.value) {
 			case TOKenum:
 				s = parseEnum();
 				break;
 
 			case TOKstruct:
 			case TOKunion:
 			case TOKclass:
 			case TOKinterface:
 				s = parseAggregate();
 				break;
 
 			case TOKimport:
 				s = parseImport(false);
 				break;
 
 			case TOKtemplate:
 				s = parseTemplateDeclaration();
 				break;
 
 			case TOKmixin:
 				if (peek(token).value == TOKlparen)
 				{   // mixin(string)
 				    nextToken();
 				    check(TOKlparen);
 				    Expression e = parseAssignExp();
 				    check(TOKrparen);
 				    check(TOKsemicolon);
 				    s = new CompileDeclaration(loc, e);
 				    break;
 				}
 
 				s = parseMixin();
 				break;
 
 			// begin CASE_BASIC_TYPES
 			case TOKwchar:
 			case TOKdchar:
 			case TOKbit:
 			case TOKbool:
 			case TOKchar:
 			case TOKint8:
 			case TOKuns8:
 			case TOKint16:
 			case TOKuns16:
 			case TOKint32:
 			case TOKuns32:
 			case TOKint64:
 			case TOKuns64:
 			case TOKfloat32:
 			case TOKfloat64:
 			case TOKfloat80:
 			case TOKimaginary32:
 			case TOKimaginary64:
 			case TOKimaginary80:
 			case TOKcomplex32:
 			case TOKcomplex64:
 			case TOKcomplex80:
 			case TOKvoid:
 				// end CASE_BASIC_TYPES
 			case TOKalias:
 			case TOKtypedef:
 			case TOKidentifier:
 			case TOKtypeof:
 			case TOKdot:
 				// Ldeclaration:
 				a = parseDeclarations(lastComments);
 				decldefs.addAll(a);
 				continue;
 
 			case TOKthis:
 				s = parseCtor();
 				break;
 
 			case TOKtilde:
 				s = parseDtor();
 				break;
 
 			case TOKinvariant:
 				s = parseInvariant();
 				break;
 
 			case TOKunittest:
 				s = parseUnitTest();
 				break;
 
 			case TOKnew:
 				s = parseNew();
 				break;
 
 			case TOKdelete:
 				s = parseDelete();
 				break;
 
 			case TOKeof:
 			case TOKrcurly:
 				return decldefs;
 
 			case TOKstatic:
 				int staticTokenStart = token.ptr;
 				int staticTokenLength = token.len;
 				nextToken();
 				if (token.value == TOKthis) {
 					s = parseStaticCtor();
 				}
 				else if (token.value == TOKtilde) {
 					s = parseStaticDtor();
 				}
 				else if (token.value == TOKassert)
 					s = parseStaticAssert();
 				else if (token.value == TOKif) {
 					StaticIfCondition condition = parseStaticIfCondition();
 					a = parseBlock();
 					aelse = null;
 					if (token.value == TOKelse) {
 						nextToken();
 						aelse = parseBlock();
 					}					
 					s = new StaticIfDeclaration(loc, condition, a, aelse);
 					break;
 				} else if (token.value == TOKimport) {
 					s = parseImport(true);
 				} else {
 					stc = STC.STCstatic;
 					
 					// goto Lstc2;
 					
 					Modifier modifier = new Modifier(TOK.TOKstatic);
 					modifier.setSourceRange(staticTokenStart, staticTokenLength);
 					
 					s = parseDeclDefs_Lstc2(isSingle, modifier, stc);
 				}
 				break;
 
 			case TOKconst:
 			case TOKfinal:
 			case TOKauto:
 			case TOKscope:
 			case TOKoverride:
 			case TOKabstract:
 			case TOKsynchronized:
 			case TOKdeprecated:
 				stc = STC.fromTOK(token.value);
 				
 				Modifier modifier = new Modifier(token.value);
 				modifier.setSourceRange(token.ptr, token.len);
 				
 				// goto Lstc;
 				nextToken();
 				
 				s= parseDeclDefs_Lstc2(isSingle, modifier, stc);
 				break;
 
 			case TOKextern:
 				if (peek(token).value != TOKlparen) {
 					stc = STC.STCextern;
 					
 					modifier = new Modifier(token.value);
 					modifier.setSourceRange(token.ptr, token.len);
 					
 					// goto Lstc;
 					nextToken();
 					
 					s = parseDeclDefs_Lstc2(isSingle, modifier, stc);
 					break;
 				}
 				{
 					LINK linksave = linkage;
 					LINK linkage = parseLinkage();
 					a = parseBlock();
 					s = new LinkDeclaration(loc, linkage, a);
 					linkage = linksave;
 					break;
 				}
 			case TOKprivate:
 			case TOKpackage:
 			case TOKprotected:
 			case TOKpublic:
 			case TOKexport:
 				
 				prot = PROT.fromTOK(token.value);
 				
 				modifier = new Modifier(token.value);
 				modifier.setSourceRange(token.ptr, token.len);
 				
 				// goto Lprot;
 				nextToken();
 				
 				a = parseBlock(isSingle);
 				s = new ProtDeclaration(loc, prot, a, modifier, isSingle[0]);
 				break;
 				
 			case TOKalign: {
 				long n;
 
 				s = null;
 				nextToken();
 				if (token.value == TOKlparen) {
 					nextToken();
 					if (token.value == TOKint32v)
 						n = Integer.parseInt(token.string);
 					else {
 						parsingErrorInsertTokenAfter(prevToken, "Integer");
 						n = 1;
 					}
 					nextToken();
 					check(TOKrparen);
 				} else {
 					n = new Global().structalign; // default
 				}
 				a = parseBlock();
 				s = new AlignDeclaration(loc, (int) n, a);
 				break;
 			}
 
 			case TOKpragma: {
 				IdentifierExp ident;
 				List<Expression> args = null;
 
 				nextToken();
 				check(TOKlparen);
 				if (token.value != TOKidentifier) {
 					parsingErrorInsertTokenAfter(prevToken, "Identifier");
 					// goto Lerror;
 					s = parseDeclDefs_Lerror();
 					continue;
 				}
 				ident = newIdentifierExp();
 				nextToken();
 				if (token.value == TOKcomma) {
 					args = parseArguments(); // pragma(identifier, args...)
 				} else {
 					check(TOKrparen); // pragma(identifier)
 				}
 
 				if (token.value == TOKsemicolon) {
 					a = null;
 				} else {
 					a = parseBlock();
 				}
 				
 				s = new PragmaDeclaration(loc, ident, args, a);
 				break;
 			}
 
 			case TOKdebug:
 				nextToken();
 				if (token.value == TOKassign) {
 					nextToken();
 					if (token.value == TOKidentifier || token.value == TOKint32v) {
 						s = newDebugAssignmentForCurrentToken();
 					} else {
 						parsingErrorInsertTokenAfter(prevToken, "Identifier or Integer");
 						s = null;
 					}
 					nextToken();
 					if (token.value != TOKsemicolon) {
 						parsingErrorInsertTokenAfter(prevToken, ";");
 					}
 					nextToken();
 					break;
 				}
 
 				DebugCondition debugCondition = parseDebugCondition();
 				
 				a = parseBlock();
 				aelse = null;
 				if (token.value == TOKelse) {
 					nextToken();
 					aelse = parseBlock();
 				}
 				
 				s = new ConditionalDeclaration(loc, debugCondition, a, aelse);
 				break;
 
 			case TOKversion:
 				nextToken();
 				if (token.value == TOKassign) {
 					nextToken();
 					if (token.value == TOKidentifier || token.value == TOKint32v) {
 						s = newVersionAssignmentForCurrentToken();
 					} else {
 						parsingErrorInsertTokenAfter(prevToken, "Identifier or Integer");
 						s = null;
 					}
 					nextToken();
 					if (token.value != TOKsemicolon) {
 						parsingErrorInsertTokenAfter(prevToken, ";");
 					}
 					nextToken();
 					break;
 				}
 				
 				VersionCondition versionCondition = parseVersionCondition();
 				
 				a = parseBlock();
 				aelse = null;
 				if (token.value == TOKelse) {
 					nextToken();
 					aelse = parseBlock();
 				}
 				
 				s = new ConditionalDeclaration(loc, versionCondition, a, aelse);
 				break;
 
 			case TOKiftype:		
 				IftypeCondition iftypeCondition = parseIftypeCondition();
 				
 				a = parseBlock();
 				aelse = null;
 				if (token.value == TOKelse) {
 					nextToken();
 					aelse = parseBlock();
 				}				
 
 				s = new ConditionalDeclaration(loc, iftypeCondition, a, aelse);
 				break;
 
 			case TOKsemicolon: // empty declaration
 				nextToken();
 				continue;
 
 			default:
 				parsingErrorDeleteToken(token);
 				nextToken();
 				continue;
 			}
 			if (s != null) {
 				s.setSourceRange(start, prevToken.ptr + prevToken.len - start);
 				s.preDdocs = lastComments;
 				adjustLastDocComment();
 				attachLeadingComments(s);				
 				adjustPossitionAccordingToComments(s, s.preDdocs, s.postDdoc);
 				decldefs.add(s);
 			}
 		} while (!once);
 		return decldefs;
 	}
 	
 	private Dsymbol parseDeclDefs_Lerror() {
 		while (token.value != TOKsemicolon && token.value != TOKeof)
 		    nextToken();
 		nextToken();
 		return null;
 	}
 	
 	private Dsymbol parseDeclDefs_Lstc2(boolean[] isSingle, Modifier modifier, int stc) {
 		Token firstToken = new Token(prevToken);
 		
 		List<Modifier> modifiers = new ArrayList<Modifier>();
 		modifiers.add(modifier);
 		
 		boolean repeat = true;
 		while(repeat) {
 			switch (token.value)
 			{
 			    case TOKconst:
 			    case TOKfinal:
 			    case TOKauto:
 			    case TOKscope:
 			    case TOKoverride:
 			    case TOKabstract:
 			    case TOKsynchronized:
 			    case TOKdeprecated:
 			    	stc |= STC.fromTOK(token.value);
 			    	
 			    	modifier = new Modifier(token.value);
 			    	modifier.setSourceRange(token.ptr, token.len);
 			    	modifiers.add(modifier);
 			    	nextToken();
 			    	break;
 			    default:
 			    	repeat = false;
 				break;
 			}
 		}
 
 		/* Look for auto initializers:
 		 *	storage_class identifier = initializer;
 		 */
 		if (token.value == TOKidentifier &&
 		    peek(token).value == TOKassign)
 		{
 			isSingle[0] = true;
 			
 		    IdentifierExp ident = newIdentifierExp();
 		    nextToken();
 		    nextToken();
 		    Initializer init = parseInitializer();
 		    VarDeclaration v = new VarDeclaration(loc, null, ident, init);
 		    v.storage_class = stc;
 		    v.addModifiers(modifiers);
 		    v.storage_class = stc;
 		    v.last = true;
 		    
 		    check(TOKsemicolon);
 		    
 		    attachLeadingComments(v);
 		    adjustPossitionAccordingToComments(v, v.preDdocs, v.postDdoc);
 		    return v; 
 		}
 		else
 		{  
 			List<Dsymbol> a = parseBlock(isSingle);
 			
 			if (isSingle[0] && a.size() == 0) {
 				parsingErrorDeleteToken(firstToken);
 				return null;
 			}
 			
 			StorageClassDeclaration s = new StorageClassDeclaration(loc, stc, a, modifier, isSingle[0]);
 			modifiers.remove(modifier);
 			s.modifiers = modifiers;
 			return s;
 		}
 	}
 	
 	private List<Dsymbol> parseBlock() {
 		return parseBlock(null);
 	}
 	
 	private List<Dsymbol> parseBlock(boolean[] isSingle) {
 		List<Dsymbol> a = null;
 
 		switch (token.value) {
 		case TOKsemicolon:
 			parsingErrorInsertToComplete(prevToken, "Declaration", "Declaration");
 			nextToken();
 			break;
 
 		case TOKlcurly:
 			nextToken();
 			a = parseDeclDefs(false);
 			if (token.value != TOKrcurly) { /* { */
 				parsingErrorInsertTokenAfter(prevToken, "}");
 			} else
 				nextToken();
 			break;
 
 		case TOKcolon:
 			nextToken();
 			// #if 1
 			// a = null;
 			// #else
 			a = parseDeclDefs(false);   // grab declarations up to closing curly
 										// bracket
 			// #endif
 			break;
 
 		default:
 			a = parseDeclDefs(true);
 			if (isSingle != null) {
 				isSingle[0] = true;
 			}
 			break;
 		}
 		return a;
 	}
 	
 	private StaticAssert parseStaticAssert() {
 		Expression exp;
 		Expression msg = null;
 
 		nextToken();
 		check(TOKlparen);
 		exp = parseAssignExp();
 		if (token.value == TOKcomma) {
 			nextToken();
 			msg = parseAssignExp();
 		}
 		check(TOKrparen);
 		check(TOKsemicolon);
 
 		return new StaticAssert(loc, exp, msg);
 	}
 	
 	private LINK parseLinkage() {
 		LINK link = LINKdefault;
 		nextToken();
 		
 		Assert.isTrue(token.value == TOKlparen);
 		
 		nextToken();
 		if (token.value == TOKidentifier) {
 			String id = token.string;
 			int start = token.ptr;
 			int length = token.len;
 			int lineNumber = token.lineNumber;
 
 			nextToken();
 			if (id.equals(Id.Windows.string))
 				link = LINKwindows;
 			else if (id.equals(Id.Pascal.string))
 				link = LINKpascal;
 			else if (id.equals(Id.D.string))
 				link = LINKd;
 			else if (id.equals(Id.C.string)) {
 				link = LINKc;
 				if (token.value == TOKplusplus) {
 					link = LINKcpp;
 					nextToken();
 				}
 			} else {
 				error("Valid linkage identifiers are D, C, C++, Pascal, Windows", IProblem.InvalidLinkageIdentifier, lineNumber, start, length);
 				link = LINKd;
 			}
 		} else {
 			link = LINKdefault; // default
 		}
 		check(TOKrparen);
 		return link;
 	}
 	
 	private DebugCondition parseDebugCondition() {
 		DebugCondition c;
 		Identifier id = null;
 		int idTokenStart = -1;
 		int idTokenLength = -1;
 		long level = 1;
 
 		if (token.value == TOKlparen) {
 			nextToken();
 			if (token.value == TOKidentifier) {
 				id = new Identifier(token);
 			} else if (token.value == TOKint32v) {
 				idTokenStart = token.ptr;
 				idTokenLength = token.len;
 				level = Integer.parseInt(token.string);
 			} else {
 				parsingErrorInsertTokenAfter(prevToken, "Identifier or Integer");
 			}
 			nextToken();
 			check(TOKrparen);
 			c = new DebugCondition(level, id);
 		} else {
 			c = new DebugCondition(1, null);
 		}
 		if (id == null && idTokenStart != -1) {
 			c.id = new Identifier(String.valueOf(level), TOK.TOKint32);
 			c.id.startPosition = idTokenStart;
 			c.id.length = idTokenLength;
 		}
 		return c;
 	}
 	
 	private VersionCondition parseVersionCondition() {
 		VersionCondition c;
 		long level = 1;
 		Identifier id = null;
 
 		int idTokenStart = -1;
 		int idTokenLength = -1;
 
 		if (token.value == TOKlparen) {
 			nextToken();
 			if (token.value == TOKidentifier) {
 				id = new Identifier(token);
 			} else if (token.value == TOKint32v) {
 				idTokenStart = token.ptr;
 				idTokenLength = token.len;
 				level = Integer.parseInt(token.string);
 			} else {
 				parsingErrorInsertTokenAfter(prevToken, "Identifier or Integer");
 			}
 			nextToken();
 			check(TOKrparen);
 		} else {
 			parsingErrorInsertToComplete(prevToken, "(condition)", "VersionDeclaration");
 		}
 		c = new VersionCondition(level, id);
 		if (id == null && idTokenStart != -1) {
 			c.id = new Identifier(String.valueOf(level), TOK.TOKint32);
 			c.id.startPosition = idTokenStart;
 			c.id.length = idTokenLength;
 		}
 		return c;
 	}
 	
 	private StaticIfCondition parseStaticIfCondition() {
 		Expression exp;
 		nextToken();
 		if (token.value == TOKlparen) {
 			nextToken();
 			exp = parseAssignExp();
 			check(TOKrparen);
 		} else {
 			parsingErrorInsertToComplete(prevToken, "(expression)", "StaticIfDeclaration");
 			exp = null;
 		}
 		return new StaticIfCondition(exp);
 	}
 	
 	private IftypeCondition parseIftypeCondition() {
 		Type targ;
 		IdentifierExp[] ident = new IdentifierExp[1];
 		Type tspec = null;
 		TOK tok = TOKreserved;
 
 		int firstTokenStart = token.ptr;
 		int firstTokenLength = token.len;
 		int firstTokenLine = token.lineNumber;
 
 		nextToken();
 		if (token.value == TOKlparen) {
 			nextToken();
 			targ = parseBasicType();
 			targ = parseDeclarator(targ, ident);
 			if (token.value == TOKcolon || token.value == TOKequal) {
 				tok = token.value;
 				nextToken();
 				tspec = parseBasicType();
 				tspec = parseDeclarator(tspec, null);
 			}
 			check(TOKrparen);
 		} else {
 			parsingErrorInsertToComplete(prevToken, "(type identifier : specialization)", "IftypeDeclaration");
 			return new IftypeCondition(null, null, null, null);
 		}
 
 		error(
 				"iftype(condition) is deprecated, use static if (is(condition))",
 				IProblem.IftypeDeprecated, firstTokenLine,
 				firstTokenStart, firstTokenLength);
 
 		return new IftypeCondition(targ, ident[0], tok, tspec);
 	}
 	
 	private CtorDeclaration parseCtor() {
 		int start = token.ptr;
 		
 		int[] varargs = new int[1];
 	    nextToken();
 	    List<Argument> arguments = parseParameters(varargs);
 	    CtorDeclaration f = new CtorDeclaration(loc, arguments, varargs[0]);
 	    parseContracts(f);
 	    f.setSourceRange(start, prevToken.ptr + prevToken.len - start);
 	    return f;
 	}
 	
 	private DtorDeclaration parseDtor() {
 		int start = token.ptr;
 		nextToken();
 		
 		check(TOKthis);
 	    check(TOKlparen);
 	    check(TOKrparen);
 		
 		DtorDeclaration f = new DtorDeclaration(loc);
 	    parseContracts(f);
 	    f.setSourceRange(start, prevToken.ptr + prevToken.len - start);
 	    return f;
 	}
 	
 	private StaticCtorDeclaration parseStaticCtor() {
 		int start = token.ptr;
 		
 		nextToken();
 	    check(TOKlparen);
 	    check(TOKrparen);
 
 	    StaticCtorDeclaration f = new StaticCtorDeclaration(loc);
 		f.setSourceRange(start, 0);
 	    parseContracts(f);
 	    return f;
 	}
 	
 	private StaticDtorDeclaration parseStaticDtor() {
 		int start = token.ptr;
 		nextToken();
 		
 		check(TOKthis);
 	    check(TOKlparen);
 	    check(TOKrparen);
 		
 		StaticDtorDeclaration f = new StaticDtorDeclaration(loc);
 		f.setSourceRange(start, 0);
 	    parseContracts(f);
 	    return f;
 	}
 	
 	private InvariantDeclaration parseInvariant() {
 		int start = token.ptr;
 	    nextToken();
 
 	    InvariantDeclaration invariant = new InvariantDeclaration(loc);
 	    invariant.fbody = parseStatement(PScurly);
 	    invariant.setSourceRange(start, prevToken.ptr + prevToken.len - start);
 	    return invariant;
 	}
 	
 	private UnitTestDeclaration parseUnitTest() {
 		int start = token.ptr;
 		nextToken();
 		
 		UnitTestDeclaration unitTest = new UnitTestDeclaration(loc);
 		unitTest.fbody = parseStatement(PScurly);
 	    unitTest.setSourceRange(start, prevToken.ptr + prevToken.len - start);
 	    return unitTest;
 	}
 	
 	private NewDeclaration parseNew() {
 		int start = token.ptr;
 		
 		nextToken();
 		int[] varargs = new int[1];
 		List<Argument> arguments = parseParameters(varargs);
 		
 		NewDeclaration f = new NewDeclaration(loc, arguments, varargs[0]);
 	    parseContracts(f);
 	    f.setSourceRange(start, prevToken.ptr + prevToken.len - start);
 	    return f;
 	}
 	
 	private FuncDeclaration parseDelete() {
 		int start = token.ptr;
 		int startLine = token.lineNumber;
 		
 		IdentifierExp name = newIdentifierExp();
 		
 		nextToken();
 		int[] varargs = new int[1];
 		List<Argument> arguments = parseParameters(varargs);
 		
 		if (varargs[0] != 0) {
 	    	error("... not allowed in delete function parameter list", 
 	    			
 	    			IProblem.VariadicNotAllowedInDelete, 
 	    			startLine, name);
 	    }
 		
 		DeleteDeclaration f = new DeleteDeclaration(loc, arguments, varargs[0]);
 	    parseContracts(f);
 	    f.setSourceRange(start, prevToken.ptr + prevToken.len - start);
 	    return f;
 	}
 	
 	@SuppressWarnings("unchecked")
 	private List<Argument> parseParameters(int[] pvarargs) {
 		List<Argument> arguments = new ArrayList<Argument>();
 		int varargs = 0;
 		boolean hasdefault = false;
 
 		check(TOKlparen);
 		while (true) {
 			Type tb;
 			IdentifierExp ai;
 			Type at;
 			Argument a;
 			InOut inout;
 			Expression ae;
 			
 			int firstTokenStart = token.ptr;
 			
 			ai = null;
 			inout = InOut.None;
 			
 			if (token.value == TOKrparen) {
 				break;
 			} else if (token.value == TOKdotdotdot) {
 				varargs = 1;
 				nextToken();
 				break;
 			} else {
 				int inoutTokenStart = token.ptr;
 				int inoutTokenLength = token.len;
 				int inoutTokenLine = token.lineNumber;
 				
 				switch(token.value) {
 					case TOKin:
 						inout = InOut.In;
 						nextToken();
 						break;
 					case TOKout:
 						inout = InOut.Out;
 						nextToken();
 						break;
 					case TOKinout:
 						inout = InOut.InOut;
 						nextToken();
 						break;
 					case TOKlazy:
 						inout = InOut.Lazy;
 						nextToken();
 						break;
 				}
 				
 				tb = parseBasicType();
 
 				IdentifierExp[] pointer2_ai = { ai };
 				at = parseDeclarator(tb, pointer2_ai);
 				ai = pointer2_ai[0];
 
 				ae = null;
 				if (token.value == TOKassign) // = defaultArg
 				{
 					nextToken();
 					ae = parseAssignExp();
 					hasdefault = true;
 				} else {
 					if (hasdefault) {
 						parsingErrorInsertTokenAfter(prevToken, "default argument");
 					}
 				}
 				if (token.value == TOKdotdotdot) { 
 					/*
 					 * This is: at ai ...
 					 */
 					if (inout == InOut.Out || inout == InOut.InOut) {
 						error("Variadic argument cannot be out or inout", IProblem.VariadicArgumentCannotBeOutOrInout, inoutTokenLine, inoutTokenStart, inoutTokenLength);
 					}
 					varargs = 2;
 					
 					a = new Argument(inout, at, ai, ae);
 					a.setSourceRange(firstTokenStart, prevToken.ptr + prevToken.len - firstTokenStart);
 					arguments.add(a);
 					nextToken();
 					break;
 				}
 				
 				if (at != null || ai != null || ae != null) {
 					a = new Argument(inout, at, ai, ae);
 					a.setSourceRange(firstTokenStart, prevToken.ptr + prevToken.len - firstTokenStart);
 					arguments.add(a);
 				}
 				if (token.value == TOKcomma) {
 					nextToken();
 				} else {
 					break;
 				}
 			}
 		}
 		check(TOKrparen);
 		pvarargs[0] = varargs;
 		return arguments;
 	}
 	
 	private EnumDeclaration parseEnum() {
 		int enumTokenStart = token.ptr;
 		int enumTokenLength = token.len;
 		int enumTokenLineNumber = token.lineNumber;
 
 		IdentifierExp id;
 		Type t;
 		
 		nextToken();
 		if (token.value == TOKidentifier) {
 			id = newIdentifierExp();
 			nextToken();
 		} else {
 			id = null;
 		}
 
 		if (token.value == TOKcolon) {
 			nextToken();
 			t = parseBasicType();
 		} else {
 			t = null;
 		}
 		
 		EnumDeclaration e = new EnumDeclaration(loc, id, t);
 		
 		if (token.value == TOKsemicolon && id != null) {
 			e.setSourceRange(enumTokenStart, token.ptr + token.len - enumTokenStart);
 			nextToken();			  
 		} else if (token.value == TOKlcurly) {
 			e.members = new ArrayList<Dsymbol>();
 			nextToken();
 			while (token.value != TOKrcurly) {
 				if (token.value == TOKeof) {
 					error("Enum declaration is invalid", IProblem.EnumDeclarationIsInvalid, enumTokenLineNumber, enumTokenStart, enumTokenLength);
 					break;
 				}
 				
 				if (token.value == TOKidentifier) {
 					Expression value;
 					IdentifierExp ident = newIdentifierExp();
 					value = null;
 					nextToken();
 					if (token.value == TOKassign) {
 						nextToken();
 						value = parseAssignExp();
 					}
 					
 					EnumMember em = new EnumMember(loc, ident, value);
 					e.addMember(em);
 					if (token.value == TOKrcurly) {
 						;
 					} else {
 						check(TOKcomma);
 					}
 				} else {
 					parsingErrorInsertToComplete(prevToken, "EnumMember", "EnumDeclaration");
 					nextToken();
 				}
 			}
 			e.setSourceRange(enumTokenStart, token.ptr + token.len - enumTokenStart);
 			
 			nextToken();
 		} else {
 			error("Enum declaration is invalid", IProblem.EnumDeclarationIsInvalid, enumTokenLineNumber, enumTokenStart, enumTokenLength);
 		}
 		
 		return e;
 	}
 	
 	@SuppressWarnings("unchecked")
 	private Dsymbol parseAggregate() {
 		AggregateDeclaration a = null;
 		IdentifierExp id;
 		List<TemplateParameter> tpl = null;
 		List<BaseClass> baseClasses = null;
 		int anon = 0;
 		boolean[] malformed = { false };
 		
 		Token firstToken = new Token(token);
 		
 		nextToken();
 		if (token.value != TOKidentifier) {
 			// Change from DMD
 			id = null;
 		} else {
 			id = newIdentifierExp();
 			nextToken();
 			if (token.value == TOKlparen) {				
 				// Gather template parameter list
 				tpl = parseTemplateParameterList(malformed);
 			}
 		}
 
 		switch (firstToken.value) {
 		case TOKclass:
 		case TOKinterface:
 			if (id == null) {
 				parsingErrorInsertTokenAfter(firstToken, "Identifier");
 			}
 
 			// Collect base class(es)
 			if (token.value == TOKcolon) {
 				nextToken();				
 				baseClasses = parseBaseClasses();
 				
 				if (token.value != TOKlcurly) {
 					parsingErrorInsertToComplete(prevToken, "AggregateBody", "AggregateDeclaration");
 				}
 			}
 			
 			if (firstToken.value == TOKclass) {
 				a = new ClassDeclaration(loc, id, baseClasses);
 			} else {
 				a = new InterfaceDeclaration(loc, id, baseClasses);
 			}
 			break;			
 		case TOKstruct:
 			if (id != null) {
 				a = new StructDeclaration(loc, id);
 			} else {
 				anon = 2;
 			}
 			break;
 		case TOKunion:
 			if (id != null) {
 				a = new UnionDeclaration(loc, id);
 			} else {
 				anon = 1;
 			}
 			break;
 
 		default:
 			throw new IllegalStateException("Can't happen");
 		}
 		
 		if (token.value == TOKsemicolon) {
 			nextToken();
 		} else if (token.value == TOKlcurly) {
 			nextToken();
 			List decl = parseDeclDefs(false);
 			if (token.value != TOKrcurly) {
 				parsingErrorInsertTokenAfter(prevToken, "}");
 			}
 			if (anon != 0) {
 				nextToken();
 			    return new AnonDeclaration(loc, anon == 1, decl);
 			}
 			a.members = decl;
 			
 			nextToken();			
 		} else {
 			if (id == null) {
 				// A single "class" makes no declaration
 				if (firstToken.value == TOKstruct || firstToken.value == TOKunion) {
 					String word = toWord(firstToken.value.toString());
 					parsingErrorInsertToComplete(firstToken,  word + "Body", word + "Declaration");
 				}
 				a = null;
 			} else {
 				// We've got at least "class Identifier", make a declaration out of it
 				String word = toWord(firstToken.value.toString());
 				parsingErrorInsertToComplete(prevToken,  word + "Body", word + "Declaration");
 				switch(firstToken.value) {
 				case TOKclass:
 					a = new ClassDeclaration(loc, id, baseClasses);
 					break;
 				case TOKinterface:
 					a = new InterfaceDeclaration(loc, id, baseClasses);
 					break;
 				case TOKstruct:
 					a = new StructDeclaration(loc, id);
 					break;
 				case TOKunion:
 					a = new UnionDeclaration(loc, id);
 					break;
 				}
 			}		
 		}
 		
 		if (malformed[0]) {
 			setMalformed(a);
 		}
 		
 		if (tpl != null)
 		{	
 			List<Dsymbol> decldefs;
 			TemplateDeclaration tempdecl;
 	
 			// Wrap a template around the aggregate declaration
 			decldefs = new ArrayList<Dsymbol>();
 			decldefs.add(a);
 			tempdecl = new TemplateDeclaration(loc, id, tpl, decldefs);
 			tempdecl.wrapper = true;
 			return tempdecl;
 	    }
 
 		return a;
 	}
 	
 	private List<BaseClass> parseBaseClasses() {
 		PROT protection = PROT.PROTpublic;
 		List<BaseClass> baseclasses = new ArrayList<BaseClass>();
 		Modifier modifier = null;
 
 		for (; true; nextToken()) {
 			switch (token.value) {
 			case TOKidentifier:
 				break;
 			case TOKprivate:
 			case TOKpackage:
 			case TOKprotected:
 			case TOKpublic:
 				protection = PROT.fromTOK(token.value);
 				
 				modifier = new Modifier(token.value);
 				modifier.setSourceRange(token.ptr, token.len);
 				continue;
 			default:
 				parsingErrorInsertTokenAfter(prevToken, "Type");
 				return null;
 			}
 			BaseClass b = new BaseClass(parseBasicType(), modifier, protection);
 			baseclasses.add(b);
 			if (token.value != TOKcomma) {
 				break;
 			}
 		}
 		return baseclasses;
 	}
 	
 	private TemplateDeclaration parseTemplateDeclaration() {
 		IdentifierExp id;
 		List<TemplateParameter> tpl;
 		List<Dsymbol> decldefs;
 		boolean[] malformed = { false };
 
 		int start = token.ptr;
 		nextToken();
 		if (token.value != TOKidentifier) {
 			parsingErrorInsertTokenAfter(prevToken, "Identifier");
 			return null;
 			// goto Lerr;
 		}
 		id = newIdentifierExp();
 		nextToken();
 		tpl = parseTemplateParameterList(malformed);
 		if (tpl == null)
 			// goto Lerr;
 			return null;
 
 		if (token.value != TOKlcurly) {
 			parsingErrorInsertToComplete(prevToken, "TemplateBodyDeclaration",
 					"TemplateDeclaration");
 			// goto Lerr;
 			return null;
 		} else {
 			nextToken();
 			decldefs = parseDeclDefs(false);
 			if (token.value != TOKrcurly) {
 				parsingErrorInsertToComplete(prevToken,
 						"TemplateBodyDeclaration", "TemplateDeclaration");
 				// goto Lerr;
 				return null;
 			}
 			nextToken();
 		}
 
 		TemplateDeclaration tempdecl = new TemplateDeclaration(loc, id, tpl, decldefs);
 		tempdecl.setSourceRange(start, prevToken.ptr + prevToken.len - start);
 		
 		if (malformed[0]) {
 			setMalformed(tempdecl);
 		}
 		
 		return tempdecl;
 
 		// Lerr:
 		// return NULL;
 	}
 	
 	@SuppressWarnings("unchecked")
 	private List<TemplateParameter> parseTemplateParameterList(boolean[] malformed) {
 		// Change from DMD: empty template parameter list is returned in case
 		// of a syntax error
 		
 		List<TemplateParameter> tpl = new ArrayList<TemplateParameter>();
 
 		if (token.value != TOKlparen) {
 			parsingErrorInsertToComplete(prevToken, "Parenthesized TemplateParameterList", "TemplateDeclaration");
 			
 			// goto Lerr;
 			malformed[0] = true;
 			return tpl;
 		}
 		
 		nextToken();
 
 		// Get array of TemplateParameters
 		if (token.value != TOKrparen) {
 			
 			boolean isvariadic = false;
 			TemplateParameter tp = null;
 			
 			while (true) {
 				IdentifierExp tp_ident = null;
 				Type tp_spectype = null;
 				Type tp_valtype = null;
 				Type tp_defaulttype = null;
 				Expression tp_specvalue = null;
 				Expression tp_defaultvalue = null;
 				Token t;
 
 				int firstTokenStart = token.ptr;
 
 				// Get TemplateParameter
 
 				// First, look ahead to see if it is a TypeParameter or a
 				// ValueParameter
 				t = peek(token);
 				if (token.value == TOKalias) { // AliasParameter
 					nextToken();
 					if (token.value != TOKidentifier) {
 						parsingErrorInsertTokenAfter(prevToken, "Identifier");
 						// goto Lerr;
 						malformed[0] = true;
 						return tpl;
 					}
 					tp_ident = newIdentifierExp();
 					nextToken();
 					if (token.value == TOKcolon) // : Type
 					{
 						nextToken();
 						tp_spectype = parseBasicType();
 						tp_spectype = parseDeclarator(tp_spectype, null);
 					}
 					if (token.value == TOKassign) // = Type
 					{
 						nextToken();
 						tp_defaulttype = parseBasicType();
 						tp_defaulttype = parseDeclarator(tp_defaulttype, null);
 					}
 					
 					tp = new TemplateAliasParameter(loc, tp_ident, tp_spectype, tp_defaulttype);
 				} else if (t.value == TOKcolon || t.value == TOKassign
 						|| t.value == TOKcomma || t.value == TOKrparen) { // TypeParameter
 					if (token.value != TOKidentifier) {
 						parsingErrorInsertTokenAfter(prevToken, "Identifier");
 						// goto Lerr;
 						malformed[0] = true;
 						return tpl;
 					}
 					tp_ident = newIdentifierExp();
 					nextToken();
 					if (token.value == TOKcolon) // : Type
 					{
 						nextToken();
 						tp_spectype = parseBasicType();
 						tp_spectype = parseDeclarator(tp_spectype, null);
 					}
 					if (token.value == TOKassign) // = Type
 					{
 						nextToken();
 						tp_defaulttype = parseBasicType();
 						tp_defaulttype = parseDeclarator(tp_defaulttype, null);
 					}
 					
 					tp = new TemplateTypeParameter(loc, tp_ident, tp_spectype, tp_defaulttype);
 				}
 			    else if (token.value == TOKidentifier && t.value == TOKdotdotdot)
 			    {	// ident...
 			    	if (isvariadic) {
 			    		error("Variadic template parameter must be last one", IProblem.VariadicTemplateParameterMustBeTheLastOne, 
 			    				t.lineNumber, token.ptr, 
 				    			t.ptr + t.len - token.ptr);
 			    	}
 					isvariadic = true;
 					tp_ident = newIdentifierExp();
 					nextToken();
 					nextToken();
 					
 					tp = new TemplateTupleParameter(loc, tp_ident);
 				} else { // ValueParameter
 					tp_valtype = parseBasicType();
 
 					IdentifierExp[] pointer2_tp_ident = new IdentifierExp[] { tp_ident };
 					tp_valtype = parseDeclarator(tp_valtype, pointer2_tp_ident);
 					tp_ident = pointer2_tp_ident[0];
 					if (tp_ident == null) {
 						error("No identifier for template value parameter", IProblem.NoIdentifierForTemplateValueParameter, t.lineNumber, t.ptr, t.len);
 						// goto Lerr;
 						malformed[0] = true;
 						return tpl;
 					}
 					if (token.value == TOKcolon) // : CondExpression
 					{
 						nextToken();
 						tp_specvalue = parseCondExp();
 					}
 					if (token.value == TOKassign) // = CondExpression
 					{
 						nextToken();
 						tp_defaultvalue = parseCondExp();
 					}
 					
 					tp = new TemplateValueParameter(loc, tp_ident, tp_valtype, tp_specvalue, tp_defaultvalue);
 				}
 				tp.setSourceRange(firstTokenStart, prevToken.ptr + prevToken.len - firstTokenStart);
 				
 				tpl.add(tp);
 				if (token.value != TOKcomma)
 					break;
 				nextToken();
 			}
 		}
 		check(TOKrparen);
 		return tpl;
 
 	// Lerr:
 	// return NULL;
 	}
 	
 	@SuppressWarnings("unchecked")
 	private Dsymbol parseMixin() {
 		TemplateMixin tm;
 		IdentifierExp id = null;
 		TypeTypeof tqual;
 		List<ASTNode> tiargs;
 		List<IdentifierExp> idents;
 
 		nextToken();
 		
 		int typeStart = token.ptr;
 
 		tqual = null;
 		if (token.value == TOKdot) {
 			id = new IdentifierExp(loc, Id.empty); // Id::empty
 		} else {
 			if (token.value == TOKtypeof) {
 				Expression exp;
 
 				nextToken();
 				check(TOKlparen);
 				exp = parseExpression();
 				check(TOKrparen);
 				tqual = new TypeTypeof(loc, exp);
 				tqual.setSourceRange(typeStart, prevToken.ptr + prevToken.len - typeStart);
 				
 				check(TOKdot);				
 			}
 			if (token.value != TOKidentifier) {
 				parsingErrorDeleteToken(prevToken);
 				// goto Lerr;
 				return null;
 			}
 			id = newIdentifierExp();
 			nextToken();
 		}
 		
 		idents = new ArrayList<IdentifierExp>();
 		while (true) {
 			int thisStart = prevToken.ptr;
 			
 			tiargs = null;
 			if (token.value == TOKnot) {
 				nextToken();
 				tiargs = parseTemplateArgumentList();
 			}
 			
 			if (token.value != TOKdot)
 				break;
 			
 			if (tiargs != null) {
 				TemplateInstance tempinst = new TemplateInstance(id);
 			    tempinst.tiargs = tiargs;
 			    tempinst.start = thisStart;
 			    tempinst.length = prevToken.ptr + prevToken.len - thisStart;
 			    id = new TemplateInstanceWrapper(loc, tempinst); // "(PIdentifier *)tempinst;" cant work in Java 
 			    tiargs = null;
 			}
 			idents.add(id);
 
 			nextToken();
 			if (token.value != TOKidentifier) {
 				parsingErrorInsertTokenAfter(prevToken, "Identifier");
 				break;
 			}
 			id = newIdentifierExp();
 			nextToken();
 		}
 		idents.add(id);
 		
 		int typeLength = prevToken.ptr + prevToken.len - typeStart;
 
 		if (token.value == TOKidentifier) {
 			id = newIdentifierExp();
 			nextToken();
 		} else {
 			id = null;
 		}
 		
 		tm = new TemplateMixin(id, tqual, idents, tiargs);
 		tm.setTypeSourceRange(typeStart, typeLength);
 
 		//tm = new MixinDeclaration(ast, id, tqual, idents, tiargs);
 		if (token.value != TOKsemicolon) {
 			parsingErrorInsertTokenAfter(prevToken, ";");
 		} else {
 			nextToken();
 		}
 
 		return tm;
 	    
 	    // Lerr:
 	    // return NULL;
 	}
 
 	@SuppressWarnings("unchecked")
 	private List<ASTNode> parseTemplateArgumentList() {
 	    List<ASTNode> tiargs = new ArrayList<ASTNode>();
 	    if (token.value != TOKlparen)
 	    {   
 	    	parsingErrorInsertToComplete(prevToken, "!(TemplateArgumentList)", "TemplateType");
 	    	return tiargs;
 	    }
 	    nextToken();
 
 	    // Get TemplateArgumentList
 	    if (token.value != TOKrparen)
 	    {
 		while (true)
 		{
 		    // See if it is an Expression or a Type
 		    if (isDeclaration(token, 0, TOKreserved, null))
 		    {	// Type
 			Type ta;
 
 			// Get TemplateArgument
 			ta = parseBasicType();
 			ta = parseDeclarator(ta, null);
 			tiargs.add(ta);
 		    }
 		    else
 		    {	// Expression
 			Expression ea;
 
 			ea = parseAssignExp();
 			tiargs.add(ea);
 		    }
 		    if (token.value != TOKcomma)
 			break;
 		    nextToken();
 		}
 	    }
 	    check(TOKrparen);
 	    return tiargs;
 	}
 	
 	private MultiImport parseImport(boolean isstatic) {
 		MultiImport multiImport = new MultiImport(loc);
 
 		Import s = null;
 		IdentifierExp id = null;
 		IdentifierExp aliasid = null;
 		List<IdentifierExp> a = null;
 		int start = token.ptr;
 		int sStart = 0;
 		
 		boolean repeat = true;
 		while (repeat) {
 			repeat = false;
 		
 		// ---	
 		the_do:
 		// ---
 			do {
 				// L1:
 				nextToken();
 				
 				if (aliasid == null) {
 					sStart = token.ptr;
 				}
 
 				if (token.value != TOKidentifier) {
 					parsingErrorInsertTokenAfter(prevToken, "Identifier");
 					break;
 				}
 				
 				a = null;
 				id = newIdentifierExp();
 
 				nextToken();
 
 				if (aliasid == null && token.value == TOKassign) {
 					aliasid = id;
 					// goto L1;
 					repeat = true;
 					break the_do;
 				}
 
 				while (token.value == TOKdot) {
 					if (a == null) {
 						a = new ArrayList<IdentifierExp>();
 					}
 					a.add(id);					
 					nextToken();
 					if (token.value != TOKidentifier) {
 						parsingErrorInsertTokenAfter(prevToken, "Identifier");
 						break;
 					}
 					id = newIdentifierExp();
 					nextToken();
 				}
 
 				s = new Import(loc, a, id, aliasid);
 				multiImport.addImport(s);
 
 				/*
 				 * Look for : alias=name, alias=name; syntax.
 				 */
 				if (token.value == TOKcolon) {
 					do {
 						IdentifierExp name = null;
 						IdentifierExp alias = null;
 						
 						nextToken();
 
 						if (token.value != TOKidentifier) {
 							parsingErrorInsertTokenAfter(prevToken, "Identifier");
 							break;
 						}
 						
 						alias = newIdentifierExp();
 						nextToken();						
 						if (token.value == TOKassign) {
 							nextToken();
 							if (token.value != TOKidentifier) {
 								parsingErrorInsertTokenAfter(prevToken, "Identifier");
 								break;
 							}
 							name = newIdentifierExp();
 							nextToken();
 						} else {
 							name = alias;
 							alias = null;
 						}
 						s.addAlias(name, alias);
 						s.setSourceRange(sStart, prevToken.ptr + prevToken.len - sStart);
 					} while (token.value == TOKcomma);
 					break; // no comma-separated imports of this form
 				} else {
 					s.setSourceRange(sStart, prevToken.ptr + prevToken.len - sStart);
 				}
 
 				aliasid = null;
 			} while (token.value == TOKcomma);
 		}
 		
 		multiImport.isstatic = isstatic;
 		multiImport.setSourceRange(start, token.ptr + token.len - start);
 
 		check(TOKsemicolon);
 
 		return multiImport;
 	}
 	
 	private Type parseBasicType() {
 		Type t = null;
 		IdentifierExp id = null;
 		TypeQualified tid = null;
 		TemplateInstance tempinst = null;
 		
 		switch (token.value) {
 		// CASE_BASIC_TYPES_X(t):
 		case TOKvoid: case TOKint8: case TOKuns8: case TOKint16:
 		case TOKuns16: case TOKint32: case TOKuns32: case TOKint64:
 		case TOKuns64: case TOKfloat32: case TOKfloat64: case TOKfloat80:
 		case TOKimaginary32: case TOKimaginary64: case TOKimaginary80:
 		case TOKcomplex32: case TOKcomplex64: case TOKcomplex80:
 		case TOKbit: case TOKbool: 
 		case TOKchar: case TOKwchar: case TOKdchar: 
 			t = newTypeBasicForCurrentToken(); 
 			t.setSourceRange(token.ptr, token.len); 
 			nextToken(); 
 			break;
 
 		case TOKidentifier:
 			id = newIdentifierExp();
 			nextToken();
 			if (token.value == TOKnot) {
 				nextToken();
 				tempinst = new TemplateInstance(id);
 				tempinst.tiargs = parseTemplateArgumentList();
 				tempinst.setSourceRange(id.start, prevToken.ptr + prevToken.len - id.start);
 				tid = new TypeInstance(loc, tempinst);
 				tid.setSourceRange(id.start, prevToken.ptr + prevToken.len - id.start);
 				// goto Lident2;
 				{
 				Type[] p_t = { t };
 				IdentifierExp[] p_id = { id };
 				TypeQualified[] p_tid = { tid };
 				TemplateInstance[] p_tempinst = { tempinst };
 				
 				parseBasicType_Lident2(p_t, p_id, p_tid, p_tempinst, id.start);
 				t = p_t[0];
 				id = p_id[0];
 				tid = p_tid[0];
 				tempinst = p_tempinst[0];
 				}
 				break;
 
 			}
 			// Lident:
 			tid = new TypeIdentifier(loc, id);
 			// Lident2:
 			{
 			Type[] p_t = { t };
 			IdentifierExp[] p_id = { id };
 			TypeQualified[] p_tid = { tid };
 			TemplateInstance[] p_tempinst = { tempinst };
 			
 			parseBasicType_Lident2(p_t, p_id, p_tid, p_tempinst, id.start);
 			t = p_t[0];
 			id = p_id[0];
 			tid = p_tid[0];
 			tempinst = p_tempinst[0];
 			}
 			break;
 
 		case TOKdot:
 			id = new IdentifierExp(loc, Id.empty);
 			// goto Lident;
 			tid = new TypeIdentifier(loc, id);
 			{
 			Type[] p_t = { t };
 			IdentifierExp[] p_id = { id };
 			TypeQualified[] p_tid = { tid };
 			TemplateInstance[] p_tempinst = { tempinst };
 			
 			parseBasicType_Lident2(p_t, p_id, p_tid, p_tempinst, token.ptr);
 			t = p_t[0];
 			id = p_id[0];
 			tid = p_tid[0];
 			tempinst = p_tempinst[0];
 			}
 			break;
 
 		case TOKtypeof: {
 			int start = token.ptr;
 			Expression exp;
 
 			nextToken();
 			check(TOKlparen);
 			exp = parseExpression();
 			check(TOKrparen);
 			
 			tid = new TypeTypeof(loc, exp);
 			tid.setSourceRange(start, prevToken.ptr + prevToken.len - start);
 			((TypeTypeof) tid).setTypeofSourceRange(start, prevToken.ptr + prevToken.len - start);
 			
 			// goto Lident2;
 			{
 			Type[] p_t = { t };
 			IdentifierExp[] p_id = { id };
 			TypeQualified[] p_tid = { tid };
 			TemplateInstance[] p_tempinst = { tempinst };
 			
 			parseBasicType_Lident2(p_t, p_id, p_tid, p_tempinst, start);
 			t = p_t[0];
 			id = p_id[0];
 			tid = p_tid[0];
 			tempinst = p_tempinst[0];
 			}
 			break;
 		}
 
 		default:
 			parsingErrorInsertTokenAfter(prevToken, "Type");
 			break;
 		}
 		return t;
 	}
 
 	private void parseBasicType_Lident2(Type[] t, IdentifierExp[] id, TypeQualified[] tid, TemplateInstance[] tempinst, int start) {
 		while (token.value == TOKdot) {
 			nextToken();
 			int tempinstStart = token.ptr;
 			if (token.value != TOKidentifier) {
 				parsingErrorInsertTokenAfter(prevToken, "Identifier");
 				break;
 			}
 			id[0] = newIdentifierExp();
 			nextToken();
 			if (token.value == TOKnot) {
 				nextToken();
 				tempinst[0] = new TemplateInstance(id[0]);
 				tempinst[0].tiargs = parseTemplateArgumentList();
 				tempinst[0].setSourceRange(tempinstStart, prevToken.ptr + prevToken.len - tempinstStart);
 				tid[0].addIdent(new TemplateInstanceWrapper(loc, tempinst[0]));
 			} else {
 				tid[0].addIdent(id[0]);
 			}
 		}
 		tid[0].setSourceRange(start, prevToken.ptr + prevToken.len - start);
 		t[0] = tid[0];
 	}
 
 	private Type parseBasicType2(Type t) {
 		Type ts;
 		Type ta;
 		Type subType;
 
 		while (true) {
 			switch (token.value) {
 			case TOKmul:
 				subType = t;
 				
 				t = new TypePointer(subType);
 				t.setSourceRange(subType.start, token.ptr + token.len - subType.start);
 				
 				nextToken();
 				continue;
 
 			case TOKlbracket:
 				if (LTORARRAYDECL) {
 					// Handle []. Make sure things like
 					// int[3][1] a;
 					// is (array[1] of array[3] of int)
 					nextToken();
 					if (token.value == TOKrbracket) {
 						subType = t;
 						
 						t = new TypeDArray(subType);
 						t.setSourceRange(subType.start, token.ptr + token.len - subType.start);
 						
 						nextToken();
 					} else if (isDeclaration(token, 0, TOKrbracket, null)) { // It's
 																				// an
 																				// associative
 																				// array
 																				// declaration
 						subType = t;
 						Type index;
 
 						index = parseBasicType();
 						index = parseDeclarator(index, null); // [ type ]
 						
 						t = newTypeAArray(t, index);
 						check(TOKrbracket);
 					} else {
 						subType = t;
 
 						inBrackets++;
 						Expression e = parseExpression(); // [ expression ]
 					    if (token.value == TOKslice) {
 							Expression e2;
 
 							nextToken();
 							e2 = parseExpression(); // [ exp .. exp ]
 							
 							t = new TypeSlice(t, e, e2);
 						} else {
 							t = new TypeSArray(t, e);
 						}
 					    t.setSourceRange(subType.start, token.ptr + token.len - subType.start);
 						
 						inBrackets--;
 						
 						check(TOKrbracket);
 					}
 					continue;
 				} else {
 					// Handle []. Make sure things like
 					// int[3][1] a;
 					// is (array[3] of array[1] of int)
 					ts = t;
 					while (token.value == TOKlbracket) {
 						nextToken();
 						if (token.value == TOKrbracket) {// []
 							ta = new TypeDArray(t);
 							nextToken();
 						} else if (isDeclaration(token, 0, TOKrbracket, null)) { // It's
 																					// an
 																					// associative
 																					// array
 																					// declaration
 							Type index;
 
 							index = parseBasicType();
 							index = parseDeclarator(index, null); // [ type ]
 							
 							ta = newTypeAArray(t, index);
 							
 							check(TOKrbracket);
 						} else {
 							Expression e = parseExpression(); // [ expression
 																// ]
 							ta = new TypeSArray(t, e);
 							check(TOKrbracket);
 						}
 
 						if (ts != t) {
 							Type pt = ts;
 							while(pt.next != t) {
 								pt = pt.next;
 							}
 							pt.next = ta;
 						} else {
 							ts = ta;
 						}
 					}
 					t = ts;
 					continue;
 				}
 
 			case TOKdelegate:
 			case TOKfunction: { // Handle delegate declaration:
 				// t delegate(parameter list)
 				// t function(parameter list)
 				List<Argument> arguments;
 				int varargs = 0;
 				TOK save = token.value;
 
 				nextToken();
 
 				int[] pointer2_varargs = { varargs };
 				arguments = parseParameters(pointer2_varargs);
 				varargs = pointer2_varargs[0];
 
 				int saveStart = t.start;
 
 				t = new TypeFunction(arguments, t, varargs, linkage);
 				if (save == TOKdelegate) {
 					t = new TypeDelegate(t);
 				} else {
 					t = new TypePointer(t);
 				}
 				t.setSourceRange(saveStart, prevToken.ptr + prevToken.len - saveStart);
 				continue;
 			}
 
 			default:
 				ts = t;
 				break;
 			}
 			break;
 		}
 		return ts;
 	}
 	
 	private Type parseDeclarator(Type targ, IdentifierExp[] ident) {
 		return parseDeclarator(targ, ident, null, null);
 	}
 
 	private Type parseDeclarator(Type t, IdentifierExp[] pident, List<TemplateParameter>[] tpl, int[] identStart) {
 		// This may happen if a basic type was not find
 		if (t == null) {
 			return null;
 		}
 		
 		Type ts;
 	    Type ta;
 
 	    t = parseBasicType2(t);
 
 	    switch (token.value)
 	    {
 
 		case TOKidentifier:
 		    if (pident != null) {
 		    	pident[0] = newIdentifierExp();
 		    	if (identStart != null) identStart[0] = token.ptr;
 		    } else {
 		    	error("Unexpected identifier in declarator", IProblem.UnexpectedIdentifierInDeclarator, token.lineNumber, token.ptr, token.len);
 		    }
 		    ts = t;
 		    nextToken();
 		    break;
 
 		case TOKlparen:
 			int oldStart = t.start;
 		    nextToken();
 		    ts = parseDeclarator(t, pident, null, identStart);
 		    ts.setSourceRange(oldStart, token.ptr + token.len - oldStart);
 		    check(TOKrparen);
 		    break;
 
 		default:
 		    ts = t;
 		    break;
 	    }
 
 	    while (true)
 	    {
 		switch (token.value)
 		{
 	//#if CARRAYDECL
 		    case TOKlbracket:
 		    {	// This is the old C-style post [] syntax.
 			nextToken();
 			if (token.value == TOKrbracket) // []
 			{
 			    ta = new TypeDArray(t);
 			    ta.setSourceRange(t.start, token.ptr + token.len - t.start);
 			    
 			    nextToken();
 			}
 			else if (isDeclaration(token, 0, TOKrbracket, null))
 			{   // It's an associative array declaration
 			    Type index;
 
 			    index = parseBasicType();
 			    index = parseDeclarator(index, null, null, identStart);	// [ type ]
 			    
 			    ta = newTypeAArray(t, index);
 			    
 			    check(TOKrbracket);
 			}
 			else
 			{
 			    Expression e = parseExpression();		// [ expression ]
 			    
 			    ta = new TypeSArray(t, e);
 			    ta.setSourceRange(t.start, token.ptr + token.len - ta.start);
 			    check(TOKrbracket);
 			}
 			
 			if (ts != t) {
 				Type pt = ts;
 				while(pt.next != t) {
 					pt = pt.next;
 				}
 				pt.next = ta;
 			} else {
 				ts = ta;
 			}
 			t = ts;
 			continue;
 		    }
 	//#endif
 		    case TOKlparen:
 		    {	List<Argument> arguments;
 			int varargs = 0;
 
 			if (tpl != null)
 			{
 			    /* Look ahead to see if this is (...)(...),
 			     * i.e. a function template declaration
 			     */
 			    if (peekPastParen(token).value == TOKlparen)
 			    {   // It's a function template declaration
 
 			    	// Gather template parameter list
 			    	boolean[] malformed = { false };
 			    	tpl[0] = parseTemplateParameterList(malformed);
 			    }
 			}
 
 			int[] pointer2_varargs = { varargs };
 			arguments = parseParameters(pointer2_varargs);
 			varargs = pointer2_varargs[0];
 			
 			ta = new TypeFunction(arguments, t, varargs, linkage);
 			ta.setSourceRange(t.start, t.length);
 			
 			if (ts != t) {
 				Type pt = ts;
 				while(pt.next != t) {
 					pt = pt.next;
 				}
 				pt.next = ta;
 			} else {
 				ts = ta;
 			}
 			t = ts;
 			break;
 		    }
 		}
 		break;
 	    }
 
 	    return ts;
 	}
 	
 	@SuppressWarnings("unchecked")
 	private List<Dsymbol> parseDeclarations(List<DDocComment> lastComments) {
 		int storage_class;
 		int stc;
 		Type ts;
 		Type t;
 		Type tfirst;
 		IdentifierExp ident;
 		int lineNumber = 0;
 		List a;
 		TOK tok;
 		LINK link = linkage;
 		
 		List<Modifier> modifiers = new ArrayList<Modifier>();
 		
 		int start = token.ptr;
 
 		switch (token.value) {
 		case TOKtypedef:
 		case TOKalias:
 			tok = token.value;
 			nextToken();
 			break;
 
 		default:
 			tok = TOKreserved;
 			break;
 		}
 
 		storage_class = STC.STCundefined;
 		while (true) {
 			switch (token.value) {
 			case TOKconst:
 			case TOKstatic:
 			case TOKfinal:
 			case TOKauto:
 			case TOKscope:
 			case TOKoverride:
 			case TOKabstract:
 			case TOKsynchronized:
 			case TOKdeprecated:
 				stc = STC.fromTOK(token.value);
 				
 				Modifier currentModifier = new Modifier(token.value);
 				currentModifier.setSourceRange(token.ptr, token.len);
 				if ((storage_class & stc) != 0) {
 					error("Redundant storage class", IProblem.RedundantStorageClass, token.lineNumber, currentModifier);
 				}
 				storage_class = storage_class | stc;
 				modifiers.add(currentModifier);
 				nextToken();
 				continue;
 
 			case TOKextern:
 				if (peek(token).value != TOKlparen) {
 					stc = STC.fromTOK(token.value);
 					
 					currentModifier = new Modifier(token.value);
 					currentModifier.setSourceRange(token.ptr, token.len);
 					if ((storage_class & stc) != 0) {
 						error("Redundant storage class", IProblem.RedundantStorageClass, token.lineNumber, currentModifier);
 					}
 					storage_class = storage_class | stc;
 					modifiers.add(currentModifier);
 					nextToken();
 					continue;
 				}
 
 				link = parseLinkage();
 				continue;
 
 			default:
 				break;
 			}
 			break;
 		}
 
 		a = new ArrayList();
 
 		/*
 		 * Look for auto initializers: storage_class identifier = initializer;
 		 */
 		if (storage_class != 0 && token.value == TOKidentifier
 				&& peek(token).value == TOKassign) {
 			ident = newIdentifierExp();
 			lineNumber = token.lineNumber;
 			
 			nextToken();
 			nextToken();
 			Initializer init = parseInitializer();
 
 			VarDeclaration v = new VarDeclaration(loc, null, ident, init);
 			v.storage_class = storage_class;
 			v.addModifiers(modifiers);
 			v.last = true;
 			a.add(v);
 			
 			
 			if (token.value == TOKsemicolon) {
 				nextToken();
 				v.preDdocs = lastComments;
 				adjustLastDocComment();
 			} else {
 				parsingErrorInsertTokenAfter(prevToken, ";");
 			}
 			
 			attachLeadingComments(v);
 			adjustPossitionAccordingToComments(v, v.preDdocs, v.postDdoc);
 			
 			return a;
 		}
 
 		if (token.value == TOKclass) {
 			AggregateDeclaration s;
 
 			s = (AggregateDeclaration) parseAggregate();
 			s.storage_class = storage_class;
 			s.addModifiers(modifiers);
 			a.add(s);
 			s.preDdocs = lastComments;
 			adjustLastDocComment();
 			return a;
 		}
 		
 		int nextVarStart = token.ptr;
 		int nextTypdefOrAliasStart = start;
 
 		ts = parseBasicType();
 		if (ts == null) {
 			return a;
 		}
 		ts = parseBasicType2(ts);
 		tfirst = null;
 		
 		int[] identStart = new int[1];
 		
 		while (true) {
 			List<TemplateParameter> tpl = null;
 
 			ident = null;
 			IdentifierExp[] pointer2_ident = { ident };
 			List[] pointer2_tpl = { tpl };
 			t = parseDeclarator(ts, pointer2_ident, pointer2_tpl, identStart);
 			ident = pointer2_ident[0];
 			tpl = pointer2_tpl[0];
 			Assert.isTrue(t != null);
 			if (tfirst == null)
 				tfirst = t;
 			else if (t != tfirst) {
 				error("Multiple declarations must have the same type", IProblem.MultipleDeclarationsMustHaveTheSameType,
 						 lineNumber, ident.start, ident.length);
 			}
 			if (ident == null) {
 				parsingErrorInsertTokenAfter(prevToken, "Identifier");
 				return a;
 			}
 
 			if (tok == TOKtypedef || tok == TOKalias) {
 				Declaration v;
 				Initializer init;
 				
 				init = null;
 				
 				int assignTokenStart = token.ptr;
 				int assignTokenLine = token.lineNumber;
 				if (token.value == TOKassign) {
 					nextToken();
 					init = parseInitializer();
 				}
 				TypedefDeclaration td = null;
 				AliasDeclaration ad = null;
 				if (tok == TOKtypedef) {
 					td = new TypedefDeclaration(loc, ident, t, init);
 					v = td;
 				} else {
 					if (init != null) {
 						error("Alias cannot have initializer", IProblem.AliasCannotHaveInitializer, assignTokenLine, assignTokenStart,  init.start + init.length - assignTokenStart);
 					}
 					
 					ad = new AliasDeclaration(loc, ident, t);
 					v = ad;
 				}
 				v.addModifiers(modifiers);
 				v.storage_class = storage_class;
 				a.add(v);
 				switch (token.value) {
 				case TOKsemicolon:
 					if (td != null) td.last = true;
 					if (ad != null) ad.last = true;					
 					v.setSourceRange(nextTypdefOrAliasStart, token.ptr + token.len - nextTypdefOrAliasStart);
 					nextToken();
 					v.preDdocs = lastComments;
 					adjustLastDocComment();
 					attachLeadingComments(v);				
 					adjustPossitionAccordingToComments(v, v.preDdocs, v.postDdoc);
 					break;
 
 				case TOKcomma:
 					v.setSourceRange(nextTypdefOrAliasStart, prevToken.ptr + prevToken.len - nextTypdefOrAliasStart);
 					nextToken();
 					continue;
 
 				default:
 					if (td != null) td.last = true;
 					if (ad != null) ad.last = true;
 					v.setSourceRange(nextTypdefOrAliasStart, prevToken.ptr + prevToken.len - nextTypdefOrAliasStart);
 					parsingErrorInsertTokenAfter(prevToken, ";");
 					break;
 				}
 			} else if (t.ty == Tfunction) {
 				Dsymbol s;
 				
 				TypeFunction typeFunction = (TypeFunction) t;
 				
 				FuncDeclaration f = new FuncDeclaration(loc, ident, storage_class, typeFunction);
 				parseContracts(f);
 				f.setSourceRange(t.start, prevToken.ptr + prevToken.len - t.start);
 				
 				if (link == linkage) {
 					s = f;
 				} else {
 					List<Dsymbol> ax = new ArrayList<Dsymbol>();
 					ax.add(f);
 					s = new LinkDeclaration(loc, link, ax);
 				}
 					
 				if (tpl != null) { // it's a function template
 					List decldefs;
 					TemplateDeclaration tempdecl;
 
 					// Wrap a template around the aggregate declaration
 					decldefs = new ArrayList();
 					decldefs.add(s);
 					tempdecl = new TemplateDeclaration(loc, s.ident, tpl, decldefs);
 					tempdecl.wrapper = true;
 					s = tempdecl;
 				}
 				s.preDdocs = lastComments;
 				adjustLastDocComment();
 				attachLeadingComments(s);
 				adjustPossitionAccordingToComments(s, s.preDdocs, s.postDdoc);
 				a.add(s);
 			} else {
 				VarDeclaration v;
 				Initializer init;
 
 				init = null;
 				if (token.value == TOKassign) {
 					nextToken();
 					init = parseInitializer();
 				}
 				
 				v = new VarDeclaration(loc, t, ident, init);
 				v.storage_class = storage_class;
 				v.modifiers = modifiers;
 				a.add(v);
 				
 				switch (token.value) {
 				case TOKsemicolon:
 					v.last = true;
 					v.setSourceRange(nextVarStart, token.ptr + token.len - nextVarStart);
 					nextToken();
 					v.preDdocs = lastComments;
 					adjustLastDocComment();
 					attachLeadingComments(v);
 					adjustPossitionAccordingToComments(v, v.preDdocs, v.postDdoc);
 					break;
 
 				case TOKcomma:
 					v.setSourceRange(nextVarStart, prevToken.ptr + prevToken.len - nextVarStart);
 					nextToken();
 					continue;
 
 				default:
 					v.last = true;
 					v.setSourceRange(nextVarStart, prevToken.ptr + prevToken.len - nextVarStart);
 					parsingErrorInsertTokenAfter(prevToken, ";");
 					break;
 				}
 			}
 			break;
 		}
 		
 		return a;
 	}
 	
 	private void parseContracts(FuncDeclaration f) {
 	    LINK linksave = linkage;
 
 	    // The following is irrelevant, as it is overridden by sc->linkage in
 	    // TypeFunction::semantic
 	    linkage = LINKd;		// nested functions have D linkage
 		
 		boolean repeat = true;
 		while (repeat) {
 			repeat = false;
 			// L1:
 			switch (token.value) {
 			case TOKlcurly:
 				if (f.frequire != null || f.fensure != null) {
 					parsingErrorInsertToComplete(prevToken, "body { ... }", "FunctionDeclaration");
 				}
 				f.setFbody(parseStatement(PSsemi));
 				break;
 
 			case TOKbody:
 				nextToken();
 				f.setFbody(parseStatement(PScurly));
 				break;
 
 			case TOKsemicolon:
 				if (f.frequire != null || f.fensure != null) {
 					parsingErrorInsertToComplete(prevToken, "body { ... }", "FunctionDeclaration");
 				}
 				//f.length = token.ptr + token.len - f.startPosition;
 				nextToken();
 				break;
 
 			/*
 			 * #if 0 // Do we want this for function declarations, so we can do: //
 			 * int x, y, foo(), z; case TOKcomma: nextToken(); continue; #endif
 			 */
 
 			/*
 			 * #if 0 // Dumped feature case TOKthrow: if (!f->fthrows)
 			 * f->fthrows = new Array(); nextToken(); check(TOKlparen); while
 			 * (1) { tb = parseBasicType(); f->fthrows->push(tb); if
 			 * (token.value == TOKcomma) { nextToken(); continue; } break; }
 			 * check(TOKrparen); goto L1; #endif
 			 */
 
 			case TOKin:
 				if (f.frequire != null) {
 					error("Redundant 'in' statement", IProblem.RedundantInStatement,
 							token);
 				}
 				nextToken();
 				
 				f.setFrequire(parseStatement(PScurly | PSscope));
 				repeat = true;
 				break;
 
 			case TOKout:
 				// parse: out (identifier) { statement }
 				
 				if (f.fensure != null) {
 					error("Redundant 'out' statement", IProblem.RedundantOutStatement,
 							token.lineNumber, token.ptr, token.len);
 				}
 				
 				nextToken();
 				if (token.value != TOKlcurly) {
 					check(TOKlparen);
 					if (token.value != TOKidentifier) {
 						parsingErrorInsertTokenAfter(prevToken, "Identifier");
 					} else {
 						f.outId = newIdentifierExp();
 						nextToken();
 					}
 					
 					check(TOKrparen);
 				}
 				
 				f.setFensure(parseStatement(PScurly | PSscope));
 				repeat = true;
 				break;
 
 			default:
 				parsingErrorInsertTokenAfter(prevToken, ";");
 				break;
 			}
 		}
 		
 		linkage = linksave;
 	}
 	
 	public Initializer parseInitializer() {
 		StructInitializer is;
 		ArrayInitializer ia;
 		ExpInitializer ie;
 		Expression e;
 		IdentifierExp id;
 		Initializer value;
 		int comma;
 		Token t;
 		int braces = 0;
 		
 		int start = token.ptr;
 
 		switch (token.value) {
 		case TOKlcurly:
 		    /* Scan ahead to see if it is a struct initializer or
 		     * a function literal.
 		     * If it contains a ';', it is a function literal.
 		     * Treat { } as a struct initializer.
 		     */
 		    braces = 1;
 			for (t = peek(token); true; t = peek(t)) {
 				switch (t.value) {
 				case TOKsemicolon:
 				case TOKreturn:
 					// goto Lexpression;
 					e = parseAssignExp();
 					ie = new ExpInitializer(loc, e);
 					return ie;
 
 				case TOKlcurly:
 					braces++;
 					continue;
 
 				case TOKrcurly:
 					if (--braces == 0) {
 						break;
 					}
 					continue;
 
 				default:
 					continue;
 				}
 				break;
 			}
 			
 			is = new StructInitializer(loc);
 			nextToken();
 			comma = 0;
 			while (true) {
 				switch (token.value) {
 				case TOKidentifier:
 					if (comma == 1) {
 						parsingErrorInsertTokenAfter(prevToken, ",");
 					}
 					t = peek(token);
 					if (t.value == TOKcolon) {
 						id = newIdentifierExp();
 						nextToken();
 						nextToken(); // skip over ':'
 					} else {
 						id = null;
 					}
 					value = parseInitializer();
 					
 					is.addInit(id, value);
 					
 					comma = 1;
 					continue;
 
 				case TOKcomma:
 					nextToken();
 					comma = 2;
 					continue;
 
 				case TOKrcurly: // allow trailing comma's
 					is.setSourceRange(start, token.ptr + token.len - start);
 					nextToken();
 					break;
 
 				default:
 					value = parseInitializer();
 				
 					is.addInit(null, value);
 					
 					comma = 1;
 					continue;
 				}
 				break;
 			}
 			
 			return is;
 
 		case TOKlbracket:
 			ia = new ArrayInitializer(loc);
 			nextToken();
 			comma = 0;
 			while (true) {
 				switch (token.value) {
 				default:
 					if (comma == 1) {
 						parsingErrorInsertTokenAfter(prevToken, ",");
 						nextToken();
 						ia.setSourceRange(start, token.ptr + token.len - start);
 						break;
 					}
 					e = parseAssignExp();
 					if (e == null)
 						break;
 					if (token.value == TOKcolon) {
 						nextToken();
 						value = parseInitializer();
 					} else {
 						value = new ExpInitializer(loc, e);
 						e = null;
 					}
 					ia.addInit(e, value);
 					comma = 1;
 					continue;
 
 				case TOKlcurly:
 				case TOKlbracket:
 					if (comma == 1) {
 						parsingErrorInsertTokenAfter(prevToken, ",");
 					}
 					
 					value = parseInitializer();
 					
 					ia.addInit(null, value);
 					comma = 1;
 					continue;
 
 				case TOKcomma:
 					nextToken();
 					comma = 2;
 					continue;
 
 				case TOKrbracket: // allow trailing comma's
 					ia.setSourceRange(start, token.ptr + token.len - start);
 					nextToken();					
 					break;
 
 				case TOKeof:
 					parsingErrorInsertTokenAfter(prevToken, "ArryaInitializer");
 					break;
 				}
 				break;
 			}
 			return ia;
 
 		case TOKvoid:
 			t = peek(token);
 			if (t.value == TOKsemicolon) {
 				nextToken();
 				return newVoidInitializerForToken(prevToken);
 			}
 			// goto Lexpression;
 
 		default:
 			// Lexpression:
 			e = parseAssignExp();
 			e.setSourceRange(start, prevToken.ptr + prevToken.len - start);
 			ie = new ExpInitializer(loc, e);
 			return ie;
 		}
 	}
 	
 	@SuppressWarnings("unchecked") 
 	public Statement parseStatement(int flags) {
 		Statement s = null;
 		Token t;
 		Statement ifbody;
 	    Statement elsebody;
 
 		if ((flags & PScurly) != 0 && token.value != TOKlcurly) {
 			error("Statement expected to be { }",
 					IProblem.StatementExpectedToBeCurlies, token);
 		}
 		
 		int start = token.ptr;
 
 		switch (token.value) {
 		case TOKidentifier:
 			// Need to look ahead to see if it is a declaration, label, or
 			// expression
 			t = peek(token);
 			if (t.value == TOKcolon) { // It's a label
 				IdentifierExp ident = newIdentifierExp();
 				nextToken();
 				nextToken();
 				Statement body = parseStatement(PSsemi);
 				
 				s = new LabelStatement(loc, ident, body);
 				break;
 			}
 			// fallthrough to TOKdot
 		case TOKdot:
 		case TOKtypeof:
 			if (isDeclaration(token, 2, TOKreserved, null)) {
 				// goto Ldeclaration;
 				Statement[] ps = { s };
 				parseStatement_Ldeclaration(ps, flags);
 				s = ps[0];
 				break;
 			} else {
 				// goto Lexp;
 				Expression exp = parseExpression();
 				check(TOKsemicolon);
 				s = new ExpStatement(loc, exp);
 				break;
 			}
 			// break;
 
 		case TOKassert:
 		case TOKthis:
 		case TOKsuper:
 		case TOKint32v:
 		case TOKuns32v:
 		case TOKint64v:
 		case TOKuns64v:
 		case TOKfloat32v:
 		case TOKfloat64v:
 		case TOKfloat80v:
 		case TOKimaginary32v:
 		case TOKimaginary64v:
 		case TOKimaginary80v:
 		case TOKcharv:
 		case TOKwcharv:
 		case TOKdcharv:
 		case TOKnull:
 		case TOKtrue:
 		case TOKfalse:
 		case TOKstring:
 		case TOKlparen:
 		case TOKcast:
 		case TOKmul:
 		case TOKmin:
 		case TOKadd:
 		case TOKplusplus:
 		case TOKminusminus:
 		case TOKnew:
 		case TOKdelete:
 		case TOKdelegate:
 		case TOKfunction:
 		case TOKtypeid:
 		case TOKis:
 		case TOKlbracket:
 			// Lexp:
 		{
 			Expression exp = parseExpression();
 			check(TOKsemicolon);
 			s = new ExpStatement(loc, exp);
 			s.setSourceRange(exp.start, prevToken.ptr + prevToken.len - exp.start);
 			break;
 		}
 
 		case TOKstatic: { // Look ahead to see if it's static assert() or
 							// static if()
 			Token t2;
 			
 			t2 = peek(token);
 			if (t2.value == TOKassert) {
 				nextToken();
 				
 				s = new StaticAssertStatement(parseStaticAssert());
 				break;
 			}
 			if (t2.value == TOKif) {
 				nextToken();
 				
 				StaticIfCondition staticIfCondition = parseStaticIfCondition();
 				
 				// goto Lcondition
 				ifbody = parseStatement(0 /*PSsemi*/);
 				elsebody = null;			
 				if (token.value == TOKelse) {
 					nextToken();
 					elsebody = parseStatement(0 /*PSsemi*/);
 				}
 				
 				s = new ConditionalStatement(loc, staticIfCondition, ifbody, elsebody);
 				break;
 			}
 			// goto Ldeclaration;
 			Statement[] ps = { s };
 			parseStatement_Ldeclaration(ps, flags);
 			s = ps[0];
 			break;
 		}
 
 			// CASE_BASIC_TYPES:
 		case TOKwchar:
 		case TOKdchar:
 		case TOKbit:
 		case TOKbool:
 		case TOKchar:
 		case TOKint8:
 		case TOKuns8:
 		case TOKint16:
 		case TOKuns16:
 		case TOKint32:
 		case TOKuns32:
 		case TOKint64:
 		case TOKuns64:
 		case TOKfloat32:
 		case TOKfloat64:
 		case TOKfloat80:
 		case TOKimaginary32:
 		case TOKimaginary64:
 		case TOKimaginary80:
 		case TOKcomplex32:
 		case TOKcomplex64:
 		case TOKcomplex80:
 		case TOKvoid:
 		case TOKtypedef:
 		case TOKalias:
 		case TOKconst:
 		case TOKauto:
 		case TOKextern:
 			// case TOKtypeof:
 			// Ldeclaration:
 		{
 			Statement[] ps = { s };
 			parseStatement_Ldeclaration(ps, flags);
 			s = ps[0];
 			break;
 		}
 
 		case TOKstruct:
 		case TOKunion:
 		case TOKclass:
 		case TOKinterface: {
 			Dsymbol d;
 
 			d = parseAggregate();
 			d.setSourceRange(start, prevToken.ptr + prevToken.len - start);
 			s = new DeclarationStatement(loc, d);
 			break;
 		}
 
 		case TOKenum: {
 			Dsymbol d;
 
 			d = parseEnum();
 			d.setSourceRange(start, prevToken.ptr + prevToken.len - start);
 			s = new DeclarationStatement(loc, d);
 			break;
 		}
 
 		case TOKmixin: {
 			Dsymbol d;
 			t = peek(token);
 		    if (t.value == TOKlparen)
 		    {	// mixin(string)
 				nextToken();
 				check(TOKlparen);
 				Expression e = parseAssignExp();
 				check(TOKrparen);
 				check(TOKsemicolon);
 				s = new CompileStatement(loc, e);
 				break;
 		    } else {			
 		    	d = parseMixin();
 		    	s = new DeclarationStatement(loc, d);
 		    	break;
 		    }
 		}
 
 		case TOKlcurly: {
 			List<Statement> statements;
 
 			nextToken();
 			statements = new ArrayList<Statement>();
 			
 			while (token.value != TOKrcurly) {
 				if (token.value == TOKeof) {
 					parsingErrorInsertTokenAfter(prevToken, "}");
 					break;
 				}
 				statements.add(parseStatement(PSsemi | PScurlyscope));
 			}
 			
 			s = newBlock(statements);
 			/*
 			if ((flags & (PSscope | PScurlyscope)) != 0) {
 				s = new ScopeStatement(s);
 			}
 			*/
 			nextToken();
 			break;
 		}
 
 		case TOKwhile: {
 			Expression condition2;
 			Statement body;
 			
 			nextToken();
 			check(TOKlparen);
 			condition2 = parseExpression();
 			check(TOKrparen);
 			
 			body = parseStatement(PSscope);
 			
 			s = new WhileStatement(loc, condition2, body);
 			break;
 		}
 
 		case TOKsemicolon:
 			if ((flags & PSsemi) == 0) {
 				error("Use '{ }' for an empty statement, not a ';'", IProblem.UseBracesForAnEmptyStatement, token);
 			}
 			nextToken();
 			
 			s = new ExpStatement(loc, null);
 			break;
 
 		case TOKdo: {
 			Statement body;
 			Expression condition2;
 			
 			nextToken();
 			
 			body = parseStatement(PSscope);
 			
 			check(TOKwhile);
 			check(TOKlparen);
 			condition2 = parseExpression();
 			check(TOKrparen);
 			s = new DoStatement(loc, body, condition2);
 			break;
 		}
 
 		case TOKfor: {
 			Statement init;
 			Expression condition2;
 			Expression increment;
 			Statement body;
 			
 			nextToken();
 			check(TOKlparen);
 			if (token.value == TOKsemicolon) {
 				init = null;
 				nextToken();
 			} else {
 				init = parseStatement(0);
 			}
 			if (token.value == TOKsemicolon) {
 				condition2 = null;
 				nextToken();
 			} else {
 				condition2 = parseExpression();
 				check(TOKsemicolon);
 			}
 			if (token.value == TOKrparen) {
 				increment = null;
 				nextToken();
 			} else {
 				increment = parseExpression();
 				check(TOKrparen);
 			}
 			body = parseStatement(PSscope);
 			s = new ForStatement(loc, init, condition2, increment, body);
 			/*
 			if (init != null) {
 				s = new ScopeStatement(s);
 				s.startPosition = saveToken.ptr;
 				s.length = prevToken.ptr + prevToken.len - s.startPosition;
 			}
 			*/
 			break;
 		}
 
 		case TOKforeach:
 		case TOKforeach_reverse:
 		{
 			TOK op = token.value;
 			List arguments;
 
 			// Statement d; // <-- not used
 			Statement body;
 			Expression aggr;
 			
 			nextToken();
 			check(TOKlparen);
 
 			arguments = new ArrayList();
 
 			while (true) {
 				Type tb;
 				IdentifierExp ai = null;
 				Type at;
 				InOut inout;
 				Argument a;
 				
 				int argumentStart = token.ptr;
 
 				inout = InOut.None;
 				if (token.value == TOKinout) {
 					inout = InOut.InOut;
 					nextToken();
 				}
 				if (token.value == TOKidentifier) {
 					Token t2 = peek(token);
 					if (t2.value == TOKcomma || t2.value == TOKsemicolon) {
 						ai = newIdentifierExp();
 						at = null; // infer argument type
 						nextToken();
 						// goto Larg;
 						a = new Argument(inout, at, ai, null);
 						a.setSourceRange(argumentStart, prevToken.ptr + prevToken.len - argumentStart);
 						arguments.add(a);
 						if (token.value == TOKcomma) {
 							nextToken();
 							continue;
 						}
 						break;
 					}
 				}
 				tb = parseBasicType();
 
 				IdentifierExp[] pointer2_ai = { ai };
 				
 				int lineNumber = token.lineNumber;
 				at = parseDeclarator(tb, pointer2_ai);
 				ai = pointer2_ai[0];
 				if (ai == null) {
 					if (at == null) {
 						error("No identifier for declarator", IProblem.NoIdentifierForDeclarator, lineNumber, prevToken.ptr, prevToken.len);
 					} else {
 						error("No identifier for declarator", IProblem.NoIdentifierForDeclarator, lineNumber, at);
 					}
 				}
 				// Larg:
 				a = new Argument(inout, at, ai, null);
 				a.setSourceRange(argumentStart, prevToken.ptr + prevToken.len - argumentStart);
 				arguments.add(a);
 				if (token.value == TOKcomma) {
 					nextToken();
 					continue;
 				}
 				break;
 			}
 			check(TOKsemicolon);
 
 			aggr = parseExpression();
 			check(TOKrparen);
 			body = parseStatement(0);
 			
 			s = new ForeachStatement(loc, op, arguments, aggr, body);
 			break;
 		}
 
 		case TOKif: {
 			Argument arg = null;
 			Expression condition2;
 			Statement ifbody2;
 			Statement elsebody2;
 			
 			nextToken();
 			check(TOKlparen);
 
 			if (token.value == TOKauto) {
 				int autoTokenStart = token.ptr;
 				
 				nextToken();
 				if (token.value == TOKidentifier) {
 					Token t2 = peek(token);
 					if (t2.value == TOKassign) {
 						arg = new Argument(InOut.None, null, newIdentifierExp(), null);
 						arg.setSourceRange(autoTokenStart, token.ptr + token.len - autoTokenStart);
 						
 						nextToken();
 						nextToken();
 					} else {
 						parsingErrorInsertTokenAfter(prevToken, "=");
 						// goto Lerror;
 						while (token.value != TOKrcurly
 								&& token.value != TOKsemicolon
 								&& token.value != TOKeof)
 							nextToken();
 						if (token.value == TOKsemicolon)
 							nextToken();
 						s = null;
 						break;
 					}
 				} else {
 					parsingErrorInsertTokenAfter(prevToken, "Identifier");
 					// goto Lerror;
 					while (token.value != TOKrcurly
 							&& token.value != TOKsemicolon
 							&& token.value != TOKeof)
 						nextToken();
 					if (token.value == TOKsemicolon)
 						nextToken();
 					s = null;
 					break;
 				}
 			} else {
 				int argTokenStart = token.ptr;
 				int argTokenLine = token.lineNumber;
 				if (isDeclaration(token, 2, TOKassign, null)) {
 					Type tb;
 					Type at;
 					IdentifierExp ai = null;
 
 					tb = parseBasicType();
 					IdentifierExp[] pointer2_ai = { ai };
 					at = parseDeclarator(tb, pointer2_ai);
 					ai = pointer2_ai[0];
 
 					arg = new Argument(InOut.In, at, ai, null);
 					arg.setSourceRange(argTokenStart, prevToken.ptr + prevToken.len - argTokenStart);
 					
 					check(TOKassign);					
 				}
 
 				// Check for " ident;"
 				else if (token.value == TOKidentifier) {
 					Token t2 = peek(token);
 					if (t2.value == TOKcomma || t2.value == TOKsemicolon) {
 						arg = new Argument(InOut.In, null, newIdentifierExp(), null);
 						arg.setSourceRange(argTokenStart, token.ptr + token.len - argTokenStart);
 						
 						nextToken();
 						nextToken();
 						
 						// if (!global.params.useDeprecated)
 						error("if (v; e) is deprecated, use if (auto v = e)", IProblem.IfAutoDeprecated, argTokenLine, argTokenStart, token.ptr + token.len - argTokenStart);
 					}
 				}
 			}
 
 			condition2 = parseExpression();
 			check(TOKrparen);
 			ifbody2 = parseStatement(PSscope);
 			if (token.value == TOKelse) {
 				nextToken();
 				elsebody2 = parseStatement(PSscope);
 			} else
 				elsebody2 = null;
 			s = new IfStatement(loc, arg, condition2, ifbody2, elsebody2);
 			break;
 		}
 
 		case TOKscope:
 		    if (peek(token).value != TOKlparen) {
 		    	// goto Ldeclaration
 		    	// scope used as storage class
 				Statement[] ps = { s };
 				parseStatement_Ldeclaration(ps, flags);
 				s = ps[0];
 				break;
 		    }
 			
 			nextToken();
 			check(TOKlparen);
 			if (token.value != TOKidentifier) {
 				parsingErrorInsertTokenAfter(prevToken, "Identifier");
 				// goto Lerror;
 				while (token.value != TOKrcurly && token.value != TOKsemicolon
 						&& token.value != TOKeof)
 					nextToken();
 				if (token.value == TOKsemicolon)
 					nextToken();
 				s = null;
 				break;
 			} else {
 				TOK t2 = TOKon_scope_exit;
 
 				String id = token.string;
 				
 				if (id.equals(Id.exit.string))
 					t2 = TOKon_scope_exit;
 				else if (id.equals(Id.failure.string))
 					t2 = TOKon_scope_failure;
 				else if (id.equals(Id.success.string))
 					t2 = TOKon_scope_success;
 				else {
 					error("Valid scope identifiers are exit, failure, or success", IProblem.InvalidScopeIdentifier, token);
 				}
 				nextToken();
 				check(TOKrparen);
 				Statement st = parseStatement(PScurlyscope);
 				
 				s = new OnScopeStatement(loc, t2, st);
 				break;
 			}
 
 		case TOKon_scope_exit:
 		case TOKon_scope_failure:
 		case TOKon_scope_success: {
 			
 			TOK t2 = token.value;
 			
 			// if (!global.params.useDeprecated)
 			error(token.toString() + " is deprecated, use scope", IProblem.OnScopeDeprecated, token);
 			nextToken();
 			Statement st = parseStatement(PScurlyscope);
 			
 			s = new OnScopeStatement(loc, t2, st);
 			break;
 		}
 
 		case TOKdebug:
 			nextToken();
 			
 			DebugCondition condition = parseDebugCondition();
 			
 			// goto Lcondition
 			ifbody = parseStatement(0 /*PSsemi*/);
 			elsebody = null;			
 			if (token.value == TOKelse) {
 				nextToken();
 				elsebody = parseStatement(0 /*PSsemi*/);
 			}
 			
 			s = new ConditionalStatement(loc, condition, ifbody, elsebody);
 			break;
 
 		case TOKversion:
 			nextToken();
 			
 			VersionCondition versionCondition = parseVersionCondition();
 			
 			// goto Lcondition
 			ifbody = parseStatement(0 /*PSsemi*/);
 			elsebody = null;			
 			if (token.value == TOKelse) {
 				nextToken();
 				elsebody = parseStatement(0 /*PSsemi*/);
 			}
 			
 			s = new ConditionalStatement(loc, versionCondition, ifbody, elsebody);
 			break;
 
 		case TOKiftype:
 			IftypeCondition iftypeCondition = parseIftypeCondition();
 			
 			// goto Lcondition
 			ifbody = parseStatement(0 /*PSsemi*/);
 			elsebody = null;			
 			if (token.value == TOKelse) {
 				nextToken();
 				elsebody = parseStatement(0 /*PSsemi*/);
 			}
 			
 			s = new ConditionalStatement(loc, iftypeCondition, ifbody, elsebody);
 			break;
 
 		case TOKpragma: {
 			IdentifierExp ident;
 			List<Expression> args = null;
 			Statement body;
 			
 			nextToken();
 			check(TOKlparen);
 			if (token.value != TOKidentifier) {
 				parsingErrorInsertTokenAfter(prevToken, "Identifier");
 				// goto Lerror;
 				while (token.value != TOKrcurly && token.value != TOKsemicolon
 						&& token.value != TOKeof)
 					nextToken();
 				if (token.value == TOKsemicolon)
 					nextToken();
 				s = null;
 				break;
 			}
 			ident = newIdentifierExp();
 			nextToken();
 			if (token.value == TOKcomma)
 				args = parseArguments(); // pragma(identifier, args...);
 			else
 				check(TOKrparen); // pragma(identifier);
 			if (token.value == TOKsemicolon) {
 				nextToken();
 				body = null;
 			} else {
 				body = parseStatement(PSsemi);
 			}
 			
 			s = new PragmaStatement(loc, ident, args, body);
 			break;
 		}
 
 		case TOKswitch: {
 			Expression condition2;
 			Statement body;
 
 			nextToken();
 			check(TOKlparen);
 			condition2 = parseExpression();
 			check(TOKrparen);
 			body = parseStatement(PSscope);
 			
 			s = new SwitchStatement(loc, condition2, body);
 			break;
 		}
 
 		case TOKcase: {
 			Expression exp;
 			List<Statement> statements;
 			List cases = new ArrayList(); // array of Expression's
 
 			while (true) {
 				nextToken();
 				exp = parseAssignExp();
 				cases.add(exp);
 				if (token.value != TOKcomma)
 					break;
 			}
 			check(TOKcolon);
 
 			statements = new ArrayList<Statement>();
 			while (token.value != TOKcase && token.value != TOKdefault
 					&& token.value != TOKrcurly) {
 				statements.add(parseStatement(PSsemi | PScurlyscope));
 			}
 			
 			s = newBlock(statements);
 			
 			/*
 			s = new ScopeStatement(s);
 			*/
 
 			// Keep cases in order by building the case statements backwards
 			for (int i = cases.size(); i != 0; i--) {
 				exp = (Expression) cases.get(i - 1);
 				s = new CaseStatement(loc, exp, s);
 			}
 			break;
 		}
 
 		case TOKdefault: {
 			List<Statement> statements;
 			
 			nextToken();
 			check(TOKcolon);
 
 			statements = new ArrayList<Statement>();
 			while (token.value != TOKcase && token.value != TOKdefault
 					&& token.value != TOKrcurly) {
 				statements.add(parseStatement(PSsemi | PScurlyscope));
 			}
 			
 			s = newBlock(statements);
 			s.setSourceRange(start, prevToken.ptr + prevToken.len - start);
 			
 			/*
 			s = new ScopeStatement(s);
 			s.startPosition = saveToken.ptr;
 			s.length = prevToken.ptr + prevToken.len - s.startPosition;
 			*/
 			
 			s = new DefaultStatement(loc, s);
 			break;
 		}
 
 		case TOKreturn: {
 			Expression exp;
 
 			nextToken();
 			if (token.value == TOKsemicolon) {
 				exp = null;
 			} else {
 				exp = parseExpression();
 			}
 			check(TOKsemicolon);
 			
 			s = new ReturnStatement(loc, exp);
 			break;
 		}
 
 		case TOKbreak: {
 			IdentifierExp ident;
 
 			nextToken();
 			if (token.value == TOKidentifier) {
 				ident = newIdentifierExp();
 				nextToken();
 			} else
 				ident = null;
 			check(TOKsemicolon);
 			
 			s = new BreakStatement(loc, ident);
 			break;
 		}
 
 		case TOKcontinue: {
 			IdentifierExp ident;
 
 			nextToken();
 			if (token.value == TOKidentifier) {
 				ident = newIdentifierExp();
 				nextToken();
 			} else {
 				ident = null;
 			}
 			
 			check(TOKsemicolon);
 			
 			s = new ContinueStatement(loc, ident);
 			break;
 		}
 
 		case TOKgoto: {
 			IdentifierExp ident;
 			
 			nextToken();
 			if (token.value == TOKdefault) {
 				nextToken();
 				s = new GotoDefaultStatement(loc);
 			} else if (token.value == TOKcase) {
 				Expression exp = null;
 
 				nextToken();
 				if (token.value != TOKsemicolon) {
 					exp = parseExpression();
 				}
 				
 				s = new GotoCaseStatement(loc, exp);
 			} else {
 				if (token.value != TOKidentifier) {
 					parsingErrorInsertTokenAfter(prevToken, "Identifier");
 					ident = null;
 				} else {
 					ident = newIdentifierExp();
 					nextToken();
 				}
 				
 				s = new GotoStatement(loc, ident);
 			}
 			check(TOKsemicolon);
 			break;
 		}
 
 		case TOKsynchronized: {
 			Expression exp;
 			Statement body;
 			
 			nextToken();
 			if (token.value == TOKlparen) {
 				nextToken();
 				exp = parseExpression();
 				check(TOKrparen);
 			} else
 				exp = null;
 			body = parseStatement(PSscope);
 			
 			s = new SynchronizedStatement(loc, exp, body);
 			break;
 		}
 
 		case TOKwith: {
 			Expression exp;
 			Statement body;
 			
 			nextToken();
 			check(TOKlparen);
 			exp = parseExpression();
 			check(TOKrparen);
 			
 			body = parseStatement(PSscope);
 			
 			s = new WithStatement(loc, exp, body);
 			break;
 		}
 
 		case TOKtry: {
 			Statement body;
 			List catches = null;
 			Statement finalbody = null;
 			
 			nextToken();
 			body = parseStatement(PSscope);
 			while (token.value == TOKcatch) {
 				Statement handler;
 				Catch c;
 				Type t2;
 				IdentifierExp id;
 				
 				int firstTokenStart = token.ptr;
 
 				nextToken();
 				if (token.value == TOKlcurly) {
 					t2 = null;
 					id = null;
 				} else {
 					check(TOKlparen);
 					t2 = parseBasicType();
 					id = null;
 					IdentifierExp[] pointer2_id = { id };
 					t2 = parseDeclarator(t2, pointer2_id);
 					id = pointer2_id[0];
 					check(TOKrparen);
 				}
 				handler = parseStatement(0);
 				
 				c = new Catch(loc, t2, id, handler);
 				c.setSourceRange(firstTokenStart, prevToken.ptr + prevToken.len - firstTokenStart);
 				
 				if (catches == null) {
 					catches = new ArrayList();
 				}
 				
 				catches.add(c);
 			}
 
 			if (token.value == TOKfinally) {
 				nextToken();
 				finalbody = parseStatement(0);
 			}
 
 			s = body;
 			if (catches == null && finalbody == null) {
 				parsingErrorInsertToComplete(prevToken, "Catch or finally", "TryStatement");
 			} else {
 				if (catches != null) {
 					s = new TryCatchStatement(loc, body, catches);
 				}
 				if (finalbody != null) {
 					if (catches != null) {
 						s.setSourceRange(start, prevToken.ptr + prevToken.len - start);
 					}
 					s = new TryFinallyStatement(loc, s, finalbody, catches != null);
 				}
 			}
 			break;
 		}
 
 		case TOKthrow: {
 			Expression exp;
 			
 			nextToken();
 			exp = parseExpression();
 			check(TOKsemicolon);
 			
 			s = new ThrowStatement(loc, exp);
 			break;
 		}
 
 		case TOKvolatile:
 			nextToken();
 			s = parseStatement(PSsemi | PScurlyscope);
 			s = new VolatileStatement(loc, s);
 			break;
 
 		case TOKasm: {
 			List<Statement> statements;
 			IdentifierExp label;
 			List<Token> toklist = new ArrayList<Token>(6);
 			
 			// Parse the asm block into a sequence of AsmStatements,
 			// each AsmStatement is one instruction.
 			// Separate out labels.
 			// Defer parsing of AsmStatements until semantic processing.
 
 			nextToken();
 			check(TOKlcurly);
 			label = null;
 			statements = new ArrayList<Statement>();
 			while (true) {
 				switch (token.value) {
 				case TOKidentifier:
 					if (toklist.isEmpty()) {
 						// Look ahead to see if it is a label
 						t = peek(token);
 						if (t.value == TOKcolon) { // It's a label
 							label = newIdentifierExp();
 							nextToken();
 							nextToken();
 							continue;
 						}
 					}
 					// goto Ldefault;
 					toklist.add(new Token(token));
 					nextToken();
 					continue;
 
 				case TOKrcurly:
 					if (!toklist.isEmpty() || label != null) {
 						parsingErrorInsertTokenAfter(prevToken, ";");
 					}
 					break;
 
 				case TOKsemicolon:
 					// Create AsmStatement from list of tokens we've saved
 					s = new AsmStatement(loc, toklist);
 					if (toklist.isEmpty()) {
 						s.setSourceRange(token.ptr, token.len);
 					} else {
 						s.setSourceRange(toklist.get(0).ptr, token.ptr + token.len - toklist.get(0).ptr);
 					}
 					
 					toklist = new ArrayList<Token>(6);
 					
 					if (label != null) {
 						s = new LabelStatement(loc, label, s);
 						label = null;
 					}
 					statements.add(s);
 					nextToken();
 					continue;
 
 				case TOKeof:
 					/* { */
 					parsingErrorInsertTokenAfter(prevToken, "}");
 					break;
 
 				default:
 					toklist.add(new Token(token));
 					nextToken();
 					continue;
 				}
 				break;
 			}
 			
 			s = new AsmBlock(loc, statements);;
 			
 			nextToken();
 			break;
 		}
 
 		default:
 			parsingErrorInsertTokenAfter(prevToken, "Statement");
 			// goto Lerror;
 
 			// Lerror:
 			while (token.value != TOKrcurly && token.value != TOKsemicolon
 					&& token.value != TOKeof)
 				nextToken();
 			if (token.value == TOKsemicolon)
 				nextToken();
 			s = null;
 			break;
 		}
 		
 		if (s != null) {
 			s.setSourceRange(start, prevToken.ptr + prevToken.len - start);
 		} else {
 			// Changed from DMD... TODO support better syntax recovery and remove it!
 			s = newBlock(null);
 		}
 		
 		return s;
 	}
 
 	private void parseStatement_Ldeclaration(Statement[] s, int flags) {
 		List a;
 
 		a = parseDeclarations(new ArrayList<DDocComment>());
 		if (a.size() > 1) {
 			List<Statement> as = new ArrayList<Statement>(a.size());
 			for (int i = 0; i < a.size(); i++) {
 				Dsymbol d = (Dsymbol) a.get(i);
 				s[0] = new DeclarationStatement(loc, d);
 				as.add(s[0]);
 			}
 			
 			s[0] = newManyVarsBlock(as);
 		} else if (a.size() == 1) {
 			Dsymbol d = (Dsymbol) a.get(0);
 			s[0] = new DeclarationStatement(loc, d);
 		} else {
 			Assert.isTrue(false);
 			s[0] = null;
 		}
 		/*
 		if ((flags & PSscope) != 0) {
 			s[0] = new ScopeStatement(s[0]);
 		}
 		*/
 	}
 	
 	private void check(TOK value) {
 		if (token.value != value) {
 			parsingErrorInsertTokenAfter(prevToken, value.toString());
 		} else {
 			nextToken();
 		}
 	}
 	
 	private boolean isDeclaration(Token t, int needId, TOK endtok, Token[] pt) {
 		int haveId = 0;
 
 		Token[] pointer2_t = { t };
 
 		if (!isBasicType(pointer2_t)) {
 			return false;
 		}
 
 		t = pointer2_t[0];
 
 		int[] pointer2_haveId = { haveId };
 		if (!isDeclarator(pointer2_t, pointer2_haveId, endtok)) {
 			return false;
 		}
 
 		t = pointer2_t[0];
 		haveId = pointer2_haveId[0];
 
 		if (needId == 1 || (needId == 0 && haveId == 0)
 				|| (needId == 2 && haveId != 0)) {
 			if (pt != null) {
 				pt[0] = t;
 			}
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	private boolean isBasicType(Token[] pt) {
 		// This code parallels parseBasicType()
 		Token t = pt[0];
 		// Token t2; // <-- not used
 		// int parens = 0; // <-- not used
 
 		switch (t.value) {
 		case TOKwchar: case TOKdchar: case TOKbit: case TOKbool:
 		case TOKchar: case TOKint8: case TOKuns8: case TOKint16:
 		case TOKuns16: case TOKint32: case TOKuns32: case TOKint64:
 		case TOKuns64: case TOKfloat32: case TOKfloat64: case TOKfloat80:
 		case TOKimaginary32: case TOKimaginary64: case TOKimaginary80:
 		case TOKcomplex32: case TOKcomplex64: case TOKcomplex80:
 		case TOKvoid:
 			t = peek(t);
 			break;
 
 		case TOKidentifier:
 			t = peek(t);
 			if (t.value == TOKnot) {
 				// goto L4
 				Token[] pointer2_t2 = { t };
 				boolean semiResult = isBasicType_L4(pointer2_t2);
 				t = pointer2_t2[0];
 				if (semiResult) {
 					pt[0] = t;
 				}
 				return semiResult;
 			}
 			
 			// goto L3
 			Token[] pointer2_t2 = { t };
 			boolean semiResult = isBasicType_L3(pointer2_t2);
 			t = pointer2_t2[0];
 			if (semiResult) {
 				pt[0] = t;
 			}
 			return semiResult;
 
 		case TOKdot: {
 			// goto Ldot;
 			pointer2_t2 = new Token[] { t };
 			semiResult = isBasicType_Ldot(pointer2_t2);
 			t = pointer2_t2[0];
 			if (semiResult) {
 				pt[0] = t;
 			}
 			return semiResult;
 		}
 
 		case TOKtypeof:
 			/*
 			 * typeof(exp).identifier...
 			 */
 			t = peek(t);
 			if (t.value != TOKlparen) {
 				return false;
 			}
 
 			Token[] pointer2_t = { t };
 			if (!skipParens(t, pointer2_t)) {
 				return false;
 			}
 
 			t = pointer2_t[0];
 
 			// goto L2;
 			pointer2_t2 = new Token[] { t };
 			semiResult = isBasicType_L2(pointer2_t2);
 			t = pointer2_t2[0];
 			if (semiResult) {
 				pt[0] = t;
 			}
 			return semiResult;			
 
 		default:
 			return false;
 		}
 		pt[0] = t;
 		return true;
 	}
 	
 	private boolean isBasicType_L2(Token[] pt) {
 		while (true) {
 			/* L2: */
 			pt[0] = peek(pt[0]);
 			/* L3: */
 			if (pt[0].value == TOKdot) {
 				/* Ldot: */
 				pt[0] = peek(pt[0]);
 				if (pt[0].value != TOKidentifier) {
 					return false;
 				}
 				pt[0] = peek(pt[0]);
 				if (pt[0].value != TOKnot) {
 					// goto L3
 					return isBasicType_L3(pt);
 				}
 				/* L4: */
 				pt[0] = peek(pt[0]);
 				if (pt[0].value != TOKlparen) {
 					return false;
 				}
 
 				if (!skipParens(pt[0], pt)) {
 					return false;
 				}
 			} else {
 				break;
 			}
 		}
 		
 		return true;
 	}
 	
 	private boolean isBasicType_L3(Token[] pt) {
 		
 		boolean firstTime = true;
 		while (true) {
 			/* L2: */
 			if (!firstTime) {
 				pt[0] = peek(pt[0]);
 			}
 			firstTime = false;
 			
 			/* L3: */
 			if (pt[0].value == TOKdot) {
 				/* Ldot: */
 				pt[0] = peek(pt[0]);
 				if (pt[0].value != TOKidentifier) {
 					return false;
 				}
 				pt[0] = peek(pt[0]);
 				if (pt[0].value != TOKnot) {
 					// goto L3;
 					return isBasicType_L3(pt);
 				}
 				/* L4: */
 				pt[0] = peek(pt[0]);
 				if (pt[0].value != TOKlparen) {
 					return false;
 				}
 
 				if (!skipParens(pt[0], pt)) {
 					return false;
 				}
 			} else {
 				break;
 			}
 		}
 		
 		return true;
 	}
 	
 	private boolean isBasicType_Ldot(Token[] pt) {
 		
 		boolean firstTime = true;
 		while (true) {
 			/* L2: */
 			if (!firstTime) {
 				pt[0] = peek(pt[0]);
 			}
 			/* L3: */
 			if (pt[0].value == TOKdot || firstTime) {
 				/* Ldot: */
 				firstTime = false;
 				pt[0] = peek(pt[0]);
 				if (pt[0].value != TOKidentifier) {
 					return false;
 				}
 				pt[0] = peek(pt[0]);
 				if (pt[0].value != TOKnot) {
 					// goto L3;
 					return isBasicType_L3(pt);
 				}
 				/* L4: */
 				pt[0] = peek(pt[0]);
 				if (pt[0].value != TOKlparen) {
 					return false;
 				}
 
 				if (!skipParens(pt[0], pt)) {
 					return false;
 				}
 			} else {
 				break;
 			}
 		}
 		
 		return true;
 	}
 	
 	private boolean isBasicType_L4(Token[] pt) {
 		
 		boolean firstTime = true;
 		while (true) {
 			/* L2: */
 			if (!firstTime) {
 				pt[0] = peek(pt[0]);
 			}
 			/* L3: */
 			if (pt[0].value == TOKdot || firstTime) {
 				/* Ldot: */
 				if (!firstTime) {
 					pt[0] = peek(pt[0]);
 					if (pt[0].value != TOKidentifier) {
 						return false;
 					}
 					pt[0] = peek(pt[0]);
 					if (pt[0].value != TOKnot) {
 						// goto L3;
 						return isBasicType_L3(pt);
 					}
 				}
 				/* L4: */
 				firstTime = false;
 				pt[0] = peek(pt[0]);
 				if (pt[0].value != TOKlparen) {
 					return false;
 				}
 
 				if (!skipParens(pt[0], pt)) {
 					return false;
 				}
 			} else {
 				break;
 			}
 		}
 		
 		return true;
 	}
 	
 	private boolean isDeclarator(Token[] pt, int[] haveId, TOK endtok) {
 		// This code parallels parseDeclarator()
 	    Token t = pt[0];
 	    int parens;
 
 	    if (t.value == TOKassign)
 		return false;
 
 	    while (true)
 	    {
 		parens = 0;
 		switch (t.value)
 		{
 		    case TOKmul:
 		    case TOKand:
 			t = peek(t);
 			continue;
 
 		    case TOKlbracket:
 			t = peek(t);
 			if (t.value == TOKrbracket)
 			{
 			    t = peek(t);
 			}
 			else { 
 				Token[] pointer2_t = { t };
 				if (isDeclaration(t, 0, TOKrbracket, pointer2_t))
 				{   // It's an associative array declaration
 					t = pointer2_t[0];
 				    t = peek(t);
 				}
 				else
 				{
 					t = pointer2_t[0];
 				    // [ expression ]
 				    // [ expression .. expression ]
 				    if (!isExpression(pointer2_t)) {
 				    	t = pointer2_t[0];
 				    	return false;
 				    }
 				    t = pointer2_t[0];
 				    
 				    if (t.value == TOKslice)
 				    {	
 				    	t = peek(t);
 				    	pointer2_t[0] = t;
 						if (!isExpression(pointer2_t)) {
 							t = pointer2_t[0];
 						    return false;
 					    }
 						t = pointer2_t[0];
 					}
 				    
 				    if (t.value != TOKrbracket) {
 				    	return false;
 				    }
 				    
 				    t = peek(t);
 				}
 				
 			}
 			continue;
 
 		    case TOKidentifier:
 			if (haveId[0] != 0)
 			    return false;
 			haveId[0] = 1;
 			t = peek(t);
 			break;
 
 		    case TOKlparen:
 			t = peek(t);
 
 			if (t.value == TOKrparen)
 			    return false;		// () is not a declarator
 
 			/* Regard ( identifier ) as not a declarator
 			 * BUG: what about ( *identifier ) in
 			 *	f(*p)(x);
 			 * where f is a class instance with overloaded () ?
 			 * Should we just disallow C-style function pointer declarations?
 			 */
 			if (t.value == TOKidentifier)
 			{   Token t2 = peek(t);
 			    if (t2.value == TOKrparen)
 				return false;
 			}
 
 
 			Token[] pointer2_t = { t };
 			if (!isDeclarator(pointer2_t, haveId, TOKrparen))
 			    return false;
 			
 			t = pointer2_t[0];
 			
 			t = peek(t);
 			parens = 1;
 			break;
 
 		    case TOKdelegate:
 		    case TOKfunction:
 			t = peek(t);
 			
 			pointer2_t = new Token[] { t };
 			
 			if (!isParameters(pointer2_t))
 			    return false;
 			
 			t = pointer2_t[0];
 			
 			continue;
 		}
 		break;
 	    }
 
 	    while (true)
 	    {
 		switch (t.value)
 		{
 	//#if CARRAYDECL
 		    case TOKlbracket:
 			parens = 0;
 			t = peek(t);
 			if (t.value == TOKrbracket)
 			{
 			    t = peek(t);
 			}
 			else {
 				Token[] pointer2_t = { t };
 				if (isDeclaration(t, 0, TOKrbracket, pointer2_t))
 				{   // It's an associative array declaration
 					t = pointer2_t[0];
 				    t = peek(t);
 				}
 				else
 				{
 					t = pointer2_t[0];
 					
 				    // [ expression ]
 				    if (!isExpression(pointer2_t))
 				    	return false;
 				    
 				    t = pointer2_t[0];
 				    
 				    
 				    if (t.value != TOKrbracket)
 					return false;
 				    t = peek(t);
 				}
 			}
 			continue;
 	//#endif
 
 		    case TOKlparen:
 			parens = 0;
 			
 			
 			Token[] pointer2_t = { t };
 			if (!isParameters(pointer2_t))
 			    return false;
 			
 			t = pointer2_t[0];
 			
 			continue;
 
 		    // Valid tokens that follow a declaration
 		    case TOKrparen:
 		    case TOKrbracket:
 		    case TOKassign:
 		    case TOKcomma:
 		    case TOKsemicolon:
 		    case TOKlcurly:
 		    case TOKin:
 			// The !parens is to disallow unnecessary parentheses
 			if (parens == 0 && (endtok == TOKreserved || endtok == t.value))
 			{   pt[0] = t;
 			    return true;
 			}
 			return false;
 
 		    default:
 			return false;
 		}
 	    }
 	}
 	
 	private boolean isParameters(Token[] pt)
 	{   // This code parallels parseParameters()
 	    Token t = pt[0];
 	    int tmp;
 
 	    if (t.value != TOKlparen)
 		return false;
 
 	    t = peek(t);
 	    while (true)
 	    {
 		switch (t.value)
 		{
 		    case TOKrparen:
 			break;
 
 		    case TOKdotdotdot:
 			t = peek(t);
 			break;
 
 		    case TOKin:
 		    case TOKout:
 		    case TOKinout:
 			t = peek(t);
 		    default:
 		    	
 		    Token[] pointer2_t = { t };
 			if (!isBasicType(pointer2_t))
 			    return false;
 			
 			t = pointer2_t[0];
 			    
 			tmp = 0;
 			
 			int[] pointer2_tmp = { tmp };
 			
 			if (t.value != TOKdotdotdot &&
 			    !isDeclarator(pointer2_t, pointer2_tmp, TOKreserved))
 			    return false;
 			
 			t = pointer2_t[0];
 			tmp = pointer2_tmp[0];
 			
 			if (t.value == TOKassign)
 			{   t = peek(t);
 				pointer2_t[0] = t;
 			    if (!isExpression(pointer2_t))
 			    	return false;
 			    
 			    t = pointer2_t[0];
 			}
 			if (t.value == TOKdotdotdot)
 			{
 			    t = peek(t);
 			    break;
 			}
 			if (t.value == TOKcomma)
 			{   t = peek(t);
 			    continue;
 			}
 			break;
 		}
 		break;
 	    }
 	    if (t.value != TOKrparen)
 		return false;
 	    t = peek(t);
 	    pt[0] = t;
 	    return true;
 	}
 	
 	private boolean isExpression(Token[] pt)
 	{
 	    // This is supposed to determine if something is an expression.
 	    // What it actually does is scan until a closing right bracket
 	    // is found.
 
 	    Token t = pt[0];
 	    int brnest = 0;
 	    int panest = 0;
 
 	    for (;; t = peek(t))
 	    {
 		switch (t.value)
 		{
 		    case TOKlbracket:
 			brnest++;
 			continue;
 
 		    case TOKrbracket:
 			if (--brnest >= 0)
 			    continue;
 			break;
 
 		    case TOKlparen:
 			panest++;
 			continue;
 
 		    case TOKcomma:
 			if (brnest != 0 || panest != 0)
 			    continue;
 			break;
 
 		    case TOKrparen:
 			if (--panest >= 0)
 			    continue;
 			break;
 
 		    case TOKslice:
 			if (brnest != 0)
 			    continue;
 			break;
 
 		    case TOKeof:
 			return false;
 
 		    default:
 			continue;
 		}
 		break;
 	    }
 
 	    pt[0] = t;
 	    return true;
 	}
 	
 	/**********************************************
 	 * Skip over
 	 *	instance foo.bar(parameters...)
 	 * Output:
 	 *	if (pt), *pt is set to the token following the closing )
 	 * Returns:
 	 *	1	it's valid instance syntax
 	 *	0	invalid instance syntax
 	 */
 
 	/* <-- Not used
 	private boolean isTemplateInstance(Token t, Token[] pt)
 	{
 	    t = peek(t);
 	    if (t.value != TOKdot)
 	    {
 		if (t.value != TOKidentifier)
 		    //goto Lfalse;
 			return false;
 		
 		t = peek(t);
 	    }
 	    while (t.value == TOKdot)
 	    {
 		t = peek(t);
 		if (t.value != TOKidentifier)
 		    //goto Lfalse;
 			return false;
 		
 		t = peek(t);
 	    }
 	    if (t.value != TOKlparen)
 		//	goto Lfalse;
 	    	return false;
 
 	    // Skip over the template arguments
 	    while (true)
 	    {
 		while (true)
 		{
 		    t = peek(t);
 		    switch (t.value)
 		    {
 			case TOKlparen:
 				
 				Token[] pointer2_t = { t };
 				
 			    if (!skipParens(t, pointer2_t))
 			    	//goto Lfalse;
 			    	return false;
 			    
 			    t = pointer2_t[0];
 			    
 			    continue;
 			case TOKrparen:
 			    break;
 			case TOKcomma:
 			    break;
 			case TOKeof:
 			case TOKsemicolon:
 			    //goto Lfalse;
 				return false;
 			default:
 			    continue;
 		    }
 		    break;
 		}
 
 		if (t.value != TOKcomma)
 		    break;
 	    }
 	    if (t.value != TOKrparen)
 	    	//goto Lfalse;
 	    	return false;
 	    
 	    t = peek(t);
 	    if (pt != null)
 		pt[0] = t;
 	    return true;
 
 	//Lfalse:
 	//    return 0;
 	}
 	*/
 	
 	/*******************************************
 	 * Skip parens, brackets.
 	 * Input:
 	 *	t is on opening (
 	 * Output:
 	 *	*pt is set to closing token, which is ')' on success
 	 * Returns:
 	 *	!=0	successful
 	 *	0	some parsing error
 	 */
 
 	private boolean skipParens(Token t, Token[] pt)
 	{
 	    int parens = 0;
 
 	    while (true)
 	    {
 		switch (t.value)
 		{
 		    case TOKlparen:
 			parens++;
 			break;
 
 		    case TOKrparen:
 			parens--;
 			if (parens < 0)
 			    //goto Lfalse;
 				return false;
 			if (parens == 0) {
 			    //goto Ldone;
 				if (pt[0] != null)
 				pt[0] = t;
 				return true;
 			}
 			break;
 
 		    case TOKeof:
 		    case TOKsemicolon:
 		    	//goto Lfalse;
 		    	return false;
 
 		     default:
 			break;
 		}
 		t = peek(t);
 	    }
 
 	  //Ldone:
 	    //if (pt[0] != null)
 		//pt[0] = t;
 	    //return true;
 
 	  //Lfalse:
 	  //  return 0;
 	}
 	
 	/********************************* Expression Parser ***************************/
 	
 	private Expression parsePrimaryExp()
 	{   Expression e = null;
 	    Type t;
 	    IdentifierExp id;
 	    TOK save;
 
 	    switch (token.value)
 	    {
 		case TOKidentifier:
 		    id = newIdentifierExp();
 		    nextToken();
 		    if (token.value == TOKnot && peek(token).value == TOKlparen)
 		    {	// identifier!(template-argument-list)
 		    	TemplateInstance tempinst;
 		    	
 		    	tempinst = new TemplateInstance(id);		    	
 		    	nextToken();
 		    	tempinst.tiargs = parseTemplateArgumentList();
 		    	tempinst.setSourceRange(id.start, prevToken.ptr + prevToken.len - id.start);
 				e = new ScopeExp(loc, tempinst);
 		    }
 		    else {
 		    	e = id;
 		    }
 		    break;
 
 		case TOKdollar:
 		    if (inBrackets == 0) {
 		    	error("'$' is valid only inside [] of index or slice", IProblem.DollarInvalidOutsideBrackets, token);
 		    }
 		    e = new DollarExp(loc);
 		    e.setSourceRange(token.ptr, token.len);
 		    nextToken();
 		    break;
 
 		case TOKdot:
 		    // Signal global scope '.' operator with "" identifier
 			e = new IdentifierExp(loc, Id.empty);
 		    break;
 
 		case TOKthis:
 		    e = new ThisExp(loc);
 		    e.setSourceRange(token.ptr, token.len);
 		    nextToken();
 		    break;
 
 		case TOKsuper:
 			e = new SuperExp(loc);
 		    e.setSourceRange(token.ptr, token.len);
 		    nextToken();
 		    break;
 
 		case TOKint32v: e = new IntegerExp(loc, token.string, token.intValue, Type.tint32); e.setSourceRange(token.ptr, token.len); nextToken(); break;
 		case TOKuns32v: e = new IntegerExp(loc, token.string, token.intValue, Type.tuns32); e.setSourceRange(token.ptr, token.len); nextToken(); break;
 		case TOKint64v: e = new IntegerExp(loc, token.string, token.intValue, Type.tint64); e.setSourceRange(token.ptr, token.len); nextToken(); break;
 		case TOKuns64v: e = new IntegerExp(loc, token.string, token.intValue, Type.tuns64); e.setSourceRange(token.ptr, token.len); nextToken(); break;
 		case TOKfloat32v: e = new RealExp(loc, token.string, new Real(token.floatValue), Type.tfloat32); e.setSourceRange(token.ptr, token.len); nextToken(); break;
 		case TOKfloat64v: e = new RealExp(loc, token.string, new Real(token.floatValue), Type.tfloat64); e.setSourceRange(token.ptr, token.len); nextToken(); break;
 		case TOKfloat80v: e = new RealExp(loc, token.string, new Real(token.floatValue), Type.tfloat80); e.setSourceRange(token.ptr, token.len); nextToken(); break;
 		case TOKimaginary32v: e = new RealExp(loc, token.string, new Real(token.floatValue), Type.timaginary32); e.setSourceRange(token.ptr, token.len); nextToken(); break;
 		case TOKimaginary64v: e = new RealExp(loc, token.string, new Real(token.floatValue), Type.timaginary64); e.setSourceRange(token.ptr, token.len); nextToken(); break;
 		case TOKimaginary80v: e = new RealExp(loc, token.string, new Real(token.floatValue), Type.timaginary80); e.setSourceRange(token.ptr, token.len); nextToken(); break;
 
 		case TOKnull:
 		    e = new NullExp(loc);
 		    e.setSourceRange(token.ptr, token.len);
 		    nextToken();
 		    break;
 
 		case TOKtrue:
 			e = new IntegerExp(loc, token.string, BigInteger.ONE, Type.tbool);
 		    nextToken();
 		    break;
 		case TOKfalse:
 			e = new IntegerExp(loc, token.string, BigInteger.ZERO, Type.tbool);
 		    nextToken();
 		    break;
 
 		case TOKcharv: e = new IntegerExp(loc, token.string, token.intValue, Type.tchar); e.setSourceRange(token.ptr, token.len); nextToken(); break;
 		case TOKwcharv:  e = new IntegerExp(loc, token.string, token.intValue, Type.twchar); e.setSourceRange(token.ptr, token.len); nextToken(); break;
 		case TOKdcharv:  e = new IntegerExp(loc, token.string, token.intValue, Type.tdchar); e.setSourceRange(token.ptr, token.len); nextToken(); break;
 
 		case TOKstring: {
 			
 			int start = token.ptr;
 			int startLine = token.lineNumber;
 			MultiStringExp stringsExpression = new MultiStringExp(loc);
 			StringExp stringExp = newStringExpForCurrentToken();
 			stringsExpression.strings.add(stringExp);
 			
 			int postfix;
 			boolean moreThanOne = false;
 
 			// cat adjacent strings
 			postfix = token.postfix;
 			while (true) {
 				nextToken();
 				if (token.value == TOKstring) {
 					moreThanOne = true;
 					if (token.postfix != 0) {
 						if (token.postfix != postfix) {
 							error("Mismatched string literal postfixes '" + (char) postfix + "' and '" + (char) token.postfix + "'",
 									IProblem.MismatchedStringLiteralPostfixes,
 									startLine, stringExp.start, token.ptr + token.len - stringExp.start);
 						}							
 						postfix = token.postfix;
 					}
 
 					if (token.string != null) {
 						stringExp = newStringExpForCurrentToken();
 						stringsExpression.strings.add(stringExp);
 					}
 				} else
 					break;
 			}
 			
 			if (moreThanOne) {
 				stringsExpression.doneParsing();
 				stringsExpression.setSourceRange(start, prevToken.ptr + prevToken.len - start);	
 				e = stringsExpression;
 			} else {
 				e = stringExp;
 			}
 			break;
 		}
 		
 		// CASE_BASIC_TYPES_X(t):
 		case TOKvoid: case TOKint8: case TOKuns8: case TOKint16:
 		case TOKuns16: case TOKint32: case TOKuns32: case TOKint64:
 		case TOKuns64: case TOKfloat32: case TOKfloat64:
 		case TOKfloat80: case TOKimaginary32: case TOKimaginary64:
 		case TOKimaginary80: case TOKcomplex32: case TOKcomplex64:
 		case TOKcomplex80: case TOKbit: case TOKbool:
 		case TOKchar: case TOKwchar: case TOKdchar:
 			t = newTypeBasicForCurrentToken();
 			nextToken();
 			// L1:
 			    check(TOKdot);
 			    if (token.value != TOKidentifier)
 			    {
 			    	parsingErrorInsertTokenAfter(prevToken, "Identifier");
 			    	// goto Lerr;
 		    		// Anything for e, as long as it's not NULL
 			    	e = new IntegerExp(loc, "0", BigInteger.ZERO, Type.tint32);
 			    	e.setSourceRange(token.ptr, token.len);
 		    		nextToken();
 		    		break;
 			    }
 			    e = new TypeDotIdExp(loc, t, newIdentifierExp());
 			    e.setSourceRange(t.start, token.ptr + token.len - t.start);
 			    nextToken();
 			    break;
 
 		case TOKtypeof:
 		{   
 			Expression exp;
 			int start = token.ptr;
 
 		    nextToken();
 		    check(TOKlparen);
 		    exp = parseExpression();
 		    check(TOKrparen);
 		    
 			t = new TypeTypeof(loc, exp);
 			t.setSourceRange(start, prevToken.ptr + prevToken.len - start);
 			((TypeTypeof) t).setTypeofSourceRange(start, prevToken.ptr + prevToken.len - start);
 			
 		    if (token.value == TOKdot) {
 		    	// goto L1;
 		    	check(TOKdot);
 			    if (token.value != TOKidentifier)
 			    {   
 			    	parsingErrorInsertTokenAfter(prevToken, "Identifier");
 					// goto Lerr;
 			    	// Anything for e, as long as it's not NULL
 			    	e = new IntegerExp(loc, "0", BigInteger.ZERO, Type.tint32);
 			    	e.setSourceRange(token.ptr, token.len);
 			    	nextToken();
 			    	break;
 			    }
 			    e = new TypeDotIdExp(loc, t, newIdentifierExp());
 			    e.setSourceRange(t.start, token.ptr + token.len - t.start);
 			    nextToken();
 			    break;
 		    }
 		    	
 		    e = new TypeExp(loc, t);
 		    break;
 		}
 
 		case TOKtypeid:
 		{   Type t2;
 			int start = token.ptr;
 
 		    nextToken();
 		    check(TOKlparen);
 		    t2 = parseBasicType();
 		    t2 = parseDeclarator(t2, null);	// ( type )
 		    check(TOKrparen);
 		    e = new TypeidExp(loc, t2);
 		    e.setSourceRange(start, prevToken.ptr + prevToken.len - start);
 		    break;
 		}
 
 		case TOKis:
 		{
 			Type targ = null;
 			IdentifierExp ident = null;
 			Type tspec = null;
 			TOK tok = TOKreserved;
 			TOK tok2 = TOKreserved;
 
 			nextToken();
 			if (token.value == TOKlparen) {
 				nextToken();
 				targ = parseBasicType();
 
 				IdentifierExp[] pointer2_ident = { ident };
 				targ = parseDeclarator(targ, pointer2_ident);
 				ident = pointer2_ident[0];
 
 				if (token.value == TOKcolon || token.value == TOKequal) {
 					tok = token.value;
 					nextToken();
 					if (tok == TOKequal
 							&& (token.value == TOKtypedef
 									|| token.value == TOKstruct
 									|| token.value == TOKunion
 									|| token.value == TOKclass
 									|| token.value == TOK.TOKsuper
 									|| token.value == TOKenum
 									|| token.value == TOKinterface
 									|| token.value == TOKfunction
 									|| token.value == TOKdelegate || token.value == TOK.TOKreturn)) {
 						tok2 = token.value;
 						nextToken();
 					} else {
 						tspec = parseBasicType();
 						tspec = parseDeclarator(tspec, null);
 					}
 				}
 				check(TOKrparen);
 			} else {
 				parsingErrorInsertToComplete(prevToken, "(type identifier : specialization)", "IftypeDeclaration");
 				// goto Lerr;
 				// Anything for e, as long as it's not NULL
 				e = new IntegerExp(loc, "0", BigInteger.ZERO, Type.tint32);
 		    	e.setSourceRange(token.ptr, token.len);
 				nextToken();
 				break;
 			}
 			e = new IftypeExp(loc, targ, ident, tok, tspec, tok2);
 			break;
 		}
 
 		case TOKassert: {
 			Expression msg = null;
 
 			int start = token.ptr;
 			nextToken();
 			check(TOKlparen);
 			e = parseAssignExp();
 			if (token.value == TOKcomma) {
 				nextToken();
 				msg = parseAssignExp();
 			}
 			check(TOKrparen);
 			
 			e = new AssertExp(loc, e, msg);
 			e.setSourceRange(start, prevToken.ptr + prevToken.len - start);
 			break;
 		}
 		
 		case TOKmixin:
 		{
 			int start = token.ptr;
 		    nextToken();
 		    check(TOKlparen);
 		    e = parseAssignExp();
 		    check(TOKrparen);
 		    e = new CompileExp(loc, e);
 		    e.setSourceRange(start, prevToken.ptr + prevToken.len - start);
 		    break;
 		}
 
 		case TOKimport:
 		{
 			int start = token.ptr;
 		    nextToken();
 		    check(TOKlparen);
 		    e = parseAssignExp();
 		    check(TOKrparen);
 		    e = new FileExp(loc, e);
 		    e.setSourceRange(start, prevToken.ptr + prevToken.len - start);
 		    break;
 		}
 
 
 		case TOKlparen:
 		    if (peekPastParen(token).value == TOKlcurly) { // (arguments) {
 															// statements... }
 				save = TOKreserved;
 				// goto case_delegate;
 				{
 				Expression[] pe = { e };
 				parsePrimaryExp_case_delegate(pe, save);
 				e = pe[0];
 				}
 				break;
 			}
 			// ( expression )
 			int start = token.ptr;
 			nextToken();
 			e = parseExpression();
 
 			int end = token.ptr + token.len;
 			check(TOKrparen);
 
 			e = new ParenExp(loc, e);
 			e.setSourceRange(start, end - start);
 			break;
 
 		case TOKlbracket:
 		{   List<Expression> elements = parseArguments();
 
 		    e = new ArrayLiteralExp(loc, elements);
 		    break;
 		}
 		
 		case TOKlcurly:
 		    // { statements... }
 			save = TOKreserved;
 			// goto case_delegate;
 			{
 			Expression[] pe = { e };
 			parsePrimaryExp_case_delegate(pe, save);
 			e = pe[0];
 			}
 			break;
 
 		case TOKfunction:
 		case TOKdelegate:
 		    save = token.value;
 			nextToken();
 			// case_delegate:
 			{
 				Expression[] pe = { e };
 				parsePrimaryExp_case_delegate(pe, save);
 				e = pe[0];
 				break;
 			}
 
 		default:
 			parsingErrorInsertTokenAfter(prevToken, "Expression");
 		// Lerr:
 		    // Anything for e, as long as it's not NULL
 			e = new IntegerExp(loc, "0", BigInteger.ZERO, Type.tint32);
 	    	e.setSourceRange(token.ptr, token.len);
 		    nextToken();
 		    break;
 	    }
 	    return parsePostExp(e);
 	}
 
 	@SuppressWarnings("unchecked")
 	private Expression parsePostExp(Expression e) {
 		int start = e == null ? prevToken.ptr : e.start;
 		
 		while (true) {
 			switch (token.value) {
 			case TOKdot:
 				nextToken();
 				if (token.value == TOKidentifier) {
 					IdentifierExp id = newIdentifierExp();
 
 					nextToken();
 					if (token.value == TOKnot && peek(token).value == TOKlparen) { 
 						// identifier!(template-argument-list)
 						TemplateInstance tempinst;
 						
 						tempinst = new TemplateInstance(id);						
 						nextToken();
 						
 						tempinst.tiargs = parseTemplateArgumentList();
 						tempinst.setSourceRange(id.start, prevToken.ptr + prevToken.len - id.start);
 						e = new DotTemplateInstanceExp(loc, e, tempinst);
 					} else {
 						e = new DotIdExp(loc, e, id);
 					}
 					continue;
 				} else if (token.value == TOKnew) {
 					e = parseNewExp(e);
 					continue;
 				} else {
 					parsingErrorInsertTokenAfter(prevToken, "Identifier");
 				}
 				break;
 
 			case TOKplusplus:
 			case TOKminusminus:
 				e = new PostExp(loc, token.value, e);
 				e.setSourceRange(start, prevToken.ptr + prevToken.len - start);
 				break;
 
 			case TOKlparen:
 				e = new CallExp(loc, e, parseArguments());
 				e.setSourceRange(start, prevToken.ptr + prevToken.len - start);
 				continue;
 
 			case TOKlbracket: { // array dereferences:
 				// array[index]
 				// array[]
 				// array[lwr .. upr]
 				Expression index;
 				Expression upr;
 
 				inBrackets++;
 				nextToken();
 				if (token.value == TOKrbracket) { // array[]
 					e = new SliceExp(loc, e, null, null);
 					nextToken();
 				} else {
 					index = parseAssignExp();
 					if (token.value == TOKslice) { // array[lwr .. upr]
 						nextToken();
 						upr = parseAssignExp();
 						e = new SliceExp(loc, e, index, upr);
 					} else { // array[index, i2, i3, i4, ...]
 						List<Expression> arguments = new ArrayList<Expression>();
 						arguments.add(index);
 						if (token.value == TOKcomma) {
 							nextToken();
 							while (true) {
 								Expression arg;
 
 								arg = parseAssignExp();
 								arguments.add(arg);
 								if (token.value == TOKrbracket)
 									break;
 								check(TOKcomma);
 							}
 						}
 						
 						e = new ArrayExp(loc, e, arguments);
 					}
 					check(TOKrbracket);
 					inBrackets--;
 				}
 				
 				e.setSourceRange(start, prevToken.ptr + prevToken.len - start);
 				continue;
 			}
 
 			default:
 				return e;
 			}
 			nextToken();
 	    }
 	}
 
 	private Expression parseUnaryExp() {
 		Expression e;
 
 		int start = token.ptr;
 
 		switch (token.value) {
 		case TOKand:
 			nextToken();
 			e = parseUnaryExp();
 			e = new AddrExp(loc, e);
 			break;
 
 		case TOKplusplus:
 			nextToken();
 			e = parseUnaryExp();
 			e = new IncrementExp(loc, e);
 			break;
 
 		case TOKminusminus:
 			nextToken();
 			e = parseUnaryExp();
 			e = new DecrementExp(loc, e);
 			break;
 
 		case TOKmul:
 			nextToken();
 			e = parseUnaryExp();
 			e = new PtrExp(loc, e);
 			break;
 
 		case TOKmin:
 			nextToken();
 			e = parseUnaryExp();
 			e = new NegExp(loc, e);
 			break;
 
 		case TOKadd:
 			nextToken();
 			e = parseUnaryExp();
 			e = new UAddExp(loc, e);
 			break;
 
 		case TOKnot:
 			nextToken();
 			e = parseUnaryExp();
 			e = new NotExp(loc, e);
 			break;
 
 		case TOKtilde:
 			nextToken();
 			e = parseUnaryExp();
 			e = new ComExp(loc, e);
 			break;
 
 		case TOKdelete:
 			nextToken();
 			e = parseUnaryExp();
 			e = new DeleteExp(loc, e);
 			break;
 
 		case TOKnew:
 			e = parseNewExp(null);
 			break;
 
 		case TOKcast: // cast(type) expression
 		{
 			Type t;
 
 			nextToken();
 			check(TOKlparen);
 			t = parseBasicType();
 			t = parseDeclarator(t, null); // ( type )
 			check(TOKrparen);
 
 			e = parseUnaryExp();
 			e = new CastExp(loc, e, t);
 			break;
 		}
 
 		case TOKlparen: {
 			Token tk;
 			
 			int firstTokenStart = token.ptr;
 			int firstTokenLine = token.lineNumber;
 
 			tk = peek(token);
 			// #if CCASTSYNTAX
 			// If cast
 			Token[] pointer2_tk = { tk };
 			if (isDeclaration(tk, 0, TOKrparen, pointer2_tk)) {
 				tk = pointer2_tk[0];
 
 				tk = peek(tk); // skip over right parenthesis
 				switch (tk.value) {
 				case TOKdot:
 				case TOKplusplus:
 				case TOKminusminus:
 				case TOKnot:
 				case TOKdelete:
 				case TOKnew:
 				case TOKlparen:
 				case TOKidentifier:
 				case TOKthis:
 				case TOKsuper:
 				case TOKint32v:
 				case TOKuns32v:
 				case TOKint64v:
 				case TOKuns64v:
 				case TOKfloat32v:
 				case TOKfloat64v:
 				case TOKfloat80v:
 				case TOKimaginary32v:
 				case TOKimaginary64v:
 				case TOKimaginary80v:
 				case TOKnull:
 				case TOKtrue:
 				case TOKfalse:
 				case TOKcharv:
 				case TOKwcharv:
 				case TOKdcharv:
 				case TOKstring:
 					/*
 					 * #if 0 case TOKtilde: case TOKand: case TOKmul: case
 					 * TOKmin: case TOKadd: #endif
 					 */
 				case TOKfunction:
 				case TOKdelegate:
 				case TOKtypeof:
 					// CASE_BASIC_TYPES: // (type)int.size
 				case TOKwchar:
 				case TOKdchar:
 				case TOKbit:
 				case TOKbool:
 				case TOKchar:
 				case TOKint8:
 				case TOKuns8:
 				case TOKint16:
 				case TOKuns16:
 				case TOKint32:
 				case TOKuns32:
 				case TOKint64:
 				case TOKuns64:
 				case TOKfloat32:
 				case TOKfloat64:
 				case TOKfloat80:
 				case TOKimaginary32:
 				case TOKimaginary64:
 				case TOKimaginary80:
 				case TOKcomplex32:
 				case TOKcomplex64:
 				case TOKcomplex80:
 				case TOKvoid: { // (type) una_exp
 					Type t;
 
 					nextToken();
 					t = parseBasicType();
 					t = parseDeclarator(t, null);
 					check(TOKrparen);
 
 					// if .identifier
 					if (token.value == TOKdot) {
 						nextToken();
 						if (token.value != TOKidentifier) {
 							parsingErrorInsertTokenAfter(prevToken, "Identifier");
 							// Change from DMD
 							e = new TypeDotIdExp(loc, t, null);
 							e.setSourceRange(start, prevToken.ptr + prevToken.len - start);
 							return e;
 						}
 						e = new TypeDotIdExp(loc, t, newIdentifierExp());
 						e.setSourceRange(start, prevToken.ptr + prevToken.len - start);
 						nextToken();
 						e = parsePostExp(e);
 					} else {
 						e = parseUnaryExp();
 						e = new CastExp(loc, e, t);
 						e.setSourceRange(start, prevToken.ptr + prevToken.len - start);
 						error("C style cast illegal, use cast(...)", IProblem.CStyleCastIllegal, firstTokenLine, firstTokenStart, prevToken.ptr + prevToken.len - firstTokenStart);
 					}
 					return e;
 				}
 				}
 			}
 			// #endif
 			e = parsePrimaryExp();
 			break;
 		}
 		default:
 			e = parsePrimaryExp();
 			break;
 		}
 		Assert.isTrue(e != null);
 
 		e.setSourceRange(start, prevToken.ptr + prevToken.len - start);
 
 		return e;
 	}
 
 	private void parsePrimaryExp_case_delegate(Expression[] e, TOK save) {
 		List<Argument> arguments;
 		int varargs = 0;
 		FuncLiteralDeclaration fd;
 		Type t;
 
 		if (token.value == TOKlcurly) {
 			t = null;
 			varargs = 0;
 			arguments = new ArrayList<Argument>();
 		} else {
 			if (token.value == TOKlparen)
 				t = null;
 			else {
 				t = parseBasicType();
 				t = parseBasicType2(t); // function return type
 			}
 			
 			int[] pointer2_varargs = { varargs };
 			arguments = parseParameters(pointer2_varargs);
 			varargs = pointer2_varargs[0];
 		}
 		
 		t = new TypeFunction(arguments, t, varargs, linkage);
 		fd = new FuncLiteralDeclaration(loc, t, save, null);
 		parseContracts(fd);
 		e[0] = new FuncExp(loc, fd);
 	}
 	
 	private Expression parseMulExp()
 	{   Expression e;
 	    Expression e2;
 
 	    e = parseUnaryExp();
 	    while (true)
 	    {
 		switch (token.value)
 		{
 		    case TOKmul: nextToken(); e2 = parseUnaryExp(); e = new MulExp(loc, e, e2); continue;
 		    case TOKdiv:   nextToken(); e2 = parseUnaryExp(); e = new DivExp(loc, e, e2); continue;
 		    case TOKmod:  nextToken(); e2 = parseUnaryExp(); e = new ModExp(loc, e, e2); continue;
 
 		    default:
 			break;
 		}
 		break;
 	    }
 	    return e;
 	}
 	
 	private Expression parseAddExp()
 	{   Expression e;
 	    Expression e2;
 
 	    e = parseMulExp();
 	    while (true)
 	    {
 		switch (token.value)
 		{
 		    case TOKadd:    nextToken(); e2 = parseMulExp(); e = new AddExp(loc, e, e2); continue;
 		    case TOKmin:    nextToken(); e2 = parseMulExp(); e = new MinExp(loc, e, e2); continue;
 		    case TOKtilde:  nextToken(); e2 = parseMulExp(); e = new CatExp(loc, e, e2); continue;
 
 		    default:
 			break;
 		}
 		break;
 	    }
 	    return e;
 	}
 	
 	private Expression parseShiftExp()
 	{   Expression e;
 	    Expression e2;
 
 	    e = parseAddExp();
 	    while (true)
 	    {
 		switch (token.value)
 		{
 		    case TOKshl:  nextToken(); e2 = parseAddExp(); e = new ShlExp(loc, e, e2);  continue;
 		    case TOKshr:  nextToken(); e2 = parseAddExp(); e = new ShrExp(loc, e, e2);  continue;
 		    case TOKushr: nextToken(); e2 = parseAddExp(); e = new UshrExp(loc, e, e2); continue;
 
 		    default:
 			break;
 		}
 		break;
 	    }
 	    return e;
 	}
 	
 	private Expression parseRelExp()
 	{   Expression e;
 	    Expression e2;
 	    TOK op;
 
 	    e = parseShiftExp();
 	    while (true)
 	    {
 		switch (token.value)
 		{
 		    case TOKlt:
 		    case TOKle:
 		    case TOKgt:
 		    case TOKge:
 		    case TOKunord:
 		    case TOKlg:
 		    case TOKleg:
 		    case TOKule:
 		    case TOKul:
 		    case TOKuge:
 		    case TOKug:
 		    case TOKue: 
 		    	op = token.value;
 		    	nextToken(); 
 		    	e2 = parseShiftExp();
 		    	e = new CmpExp(loc, op, e, e2); continue;
 		    case TOKin: 
 		    	nextToken(); 
 		    	e2 = parseShiftExp(); 
 		    	e = new InExp(loc, e, e2); 
 		    	continue;
 
 		    default:
 			break;
 		}
 		break;
 	    }
 	    return e;
 	}
 	
 	private Expression parseEqualExp()
 	{   Expression e;
 	    Expression e2;
 	    Token t;
 
 	    e = parseRelExp();
 	    while (true)
 	    {	TOK value = token.value;
 
 		switch (value)
 		{
 		    case TOKequal:
 		    case TOKnotequal:
 		    	nextToken();
 				e2 = parseRelExp();
 				e = new EqualExp(loc, value, e, e2);
 				continue;
 
 		    case TOKidentity:
 		    	error("'===' is no longer legal, use 'is' instead",
 		    			IProblem.ThreeEqualsIsNoLongerLegal, token.lineNumber, token.ptr, token.len);
 			//goto L1;
 			nextToken();
 			e2 = parseRelExp();
 			e = new IdentityExp(loc, value, e, e2);
 			continue;
 
 		    case TOKnotidentity:
 		    	error("'!==' is no longer legal, use 'is' instead",
 		    			IProblem.NotTwoEqualsIsNoLongerLegal, token.lineNumber, token.ptr, token.len);
 			//goto L1;
 			nextToken();
 			e2 = parseRelExp();
 			e = new IdentityExp(loc, value, e, e2);
 			continue;
 
 		    case TOKis:
 			value = TOKidentity;
 			//goto L1;
 			nextToken();
 			e2 = parseRelExp();
 			e = new IdentityExp(loc, value, e, e2);
 			continue;
 
 		    case TOKnot:
 			// Attempt to identify '!is'
 			t = peek(token);
 			if (t.value != TOKis)
 			    break;
 			nextToken();
 			value = TOKnotidentity;
 			//goto L1;
 			nextToken();
 			e2 = parseRelExp();
 			e = new IdentityExp(loc, value, e, e2);
 			continue;
 
 		    // L1:
 			//nextToken();
 			//e2 = parseRelExp();
 			//e = new IdentityExp(value, loc, e, e2);
 			//continue;
 
 		    default:
 			break;
 		}
 		break;
 	    }
 	    return e;
 	}
 	
 	Expression parseCmpExp()
 	{   Expression e;
 	    Expression e2;
 	    Token t;
 
 	    e = parseShiftExp();
 	    TOK op = token.value;
 
 	    switch (op)
 	    {
 		case TOKequal:
 		case TOKnotequal:
 			 nextToken();
 			    e2 = parseShiftExp();
 			    e = new EqualExp(loc, op, e, e2);
 			    break;
 
 		case TOKis:
 		    //op = TOKidentity;
 		    nextToken();
 		    e2 = parseShiftExp();
 		    e = new IdentityExp(loc, op, e, e2);
 		    break;
 
 		case TOKnot:
 		    // Attempt to identify '!is'
 		    t = peek(token);
 		    if (t.value != TOKis)
 		    	break;
 		    nextToken();
 		    op = TOK.TOKnotis;
 		    nextToken();
 		    e2 = parseShiftExp();
 		    e = new IdentityExp(loc, op, e, e2);
 		    break;
 		    
 		case TOKlt:
 	    case TOKle:
 	    case TOKgt:
 	    case TOKge:
 	    case TOKunord:
 	    case TOKlg:
 	    case TOKleg:
 	    case TOKule:
 	    case TOKul:
 	    case TOKuge:
 	    case TOKug:
 	    case TOKue:
 	    	nextToken(); 
 	    	e2 = parseShiftExp(); 
 	    	e = new CmpExp(loc, op, e, e2); 
 	    	break;
 
 		case TOKin:
 		    nextToken();
 		    e2 = parseShiftExp();
 		    e = new InExp(loc, e, e2);
 		    break;
 
 		default:
 		    break;
 	    }
 	    return e;
 	}
 
 	
 	private Expression parseAndExp() {
 		Expression e;
 		Expression e2;
 
 		if (apiLevel == AST.D1) {
 			e = parseEqualExp();
 			while (token.value == TOKand) {
 				nextToken();
 				e2 = parseEqualExp();
 				e = new AndExp(loc, e, e2);
 			}
 		} else {
 			e = parseCmpExp();
 			while (token.value == TOKand)
 			{
 			    nextToken();
 			    e2 = parseCmpExp();
 			    e = new AndExp(loc, e, e2);
 			}
 
 		}
 		return e;
 	}
 	
 	private Expression parseXorExp() {
 		Expression e;
 		Expression e2;
 
 		e = parseAndExp();
 		while (token.value == TOKxor) {
 			nextToken();
 			e2 = parseAndExp();
 			e = new XorExp(loc, e, e2);
 		}
 		return e;
 	}
 	
 	private Expression parseOrExp() {
 		Expression e;
 		Expression e2;
 
 		e = parseXorExp();
 		while (token.value == TOKor) {
 			nextToken();
 			e2 = parseXorExp();
 			e = new OrExp(loc, e, e2);
 		}
 		return e;
 	}
 	
 	private Expression parseAndAndExp() {
 		Expression e;
 		Expression e2;
 
 		e = parseOrExp();
 		while (token.value == TOKandand) {
 			nextToken();
 			e2 = parseOrExp();
 			e = new AndAndExp(loc, e, e2);
 		}
 		return e;
 	}
 	
 	private Expression parseOrOrExp() {
 		Expression e;
 		Expression e2;
 
 		e = parseAndAndExp();
 		while (token.value == TOKoror) {
 			nextToken();
 			e2 = parseAndAndExp();
 			e = new OrOrExp(loc, e, e2);
 		}
 		return e;
 	}
 
 	private Expression parseCondExp() {
 		Expression e;
 		Expression e1;
 		Expression e2;
 
 		e = parseOrOrExp();
 		if (token.value == TOKquestion) {
 			nextToken();
 			e1 = parseExpression();
 			check(TOKcolon);
 			e2 = parseCondExp();
 			e = new CondExp(loc, e, e1, e2);
 		}
 		return e;
 	}
 	
 	private Expression parseAssignExp()
 	{   Expression e;
 	    Expression e2;
 
 	    e = parseCondExp();
 	    while (true)
 	    {
 		switch (token.value)
 		{
 		case TOKassign:  nextToken(); e2 = parseAssignExp(); e = new AssignExp(loc, e, e2); continue;
 		case TOKaddass:  nextToken(); e2 = parseAssignExp(); e = new AddAssignExp(loc, e, e2); continue;
 		case TOKminass:  nextToken(); e2 = parseAssignExp(); e = new MinAssignExp(loc, e, e2); continue;
 		case TOKmulass:  nextToken(); e2 = parseAssignExp(); e = new MulAssignExp(loc, e, e2); continue;
 		case TOKdivass:  nextToken(); e2 = parseAssignExp(); e = new DivAssignExp(loc, e, e2); continue;
 		case TOKmodass:  nextToken(); e2 = parseAssignExp(); e = new ModAssignExp(loc, e, e2); continue;
 		case TOKandass:  nextToken(); e2 = parseAssignExp(); e = new AndAssignExp(loc, e, e2); continue;
 		case TOKorass:  nextToken(); e2 = parseAssignExp(); e = new OrAssignExp(loc, e, e2); continue;
 		case TOKxorass:  nextToken(); e2 = parseAssignExp(); e = new XorAssignExp(loc, e, e2); continue;
 		case TOKshlass:  nextToken(); e2 = parseAssignExp(); e = new ShlAssignExp(loc, e, e2); continue;
 		case TOKshrass:  nextToken(); e2 = parseAssignExp(); e = new ShrAssignExp(loc, e, e2); continue;
 		case TOKushrass:  nextToken(); e2 = parseAssignExp(); e = new UshrAssignExp(loc, e, e2); continue;
 		case TOKcatass:  nextToken(); e2 = parseAssignExp(); e = new CatAssignExp(loc, e, e2); continue;
 	    default:
 			break;
 		}
 		break;
 	    }
 	    return e;
 	}
 	
 	public Expression parseExpression() {
 		Expression e;
 		Expression e2;
 
 		e = parseAssignExp();
 		while (token.value == TOKcomma) {
 			nextToken();
 			e2 = parseAssignExp();
 			e = new CommaExp(loc, e, e2);
 		}
 		return e;
 	}
 	
 	/***************************************************************************
 	 * Collect argument list. Assume current token is '('.
 	 */
 	
 	@SuppressWarnings("unchecked")
 	private List<Expression> parseArguments() {
 		// function call
 		List<Expression> arguments;
 		Expression arg;
 		TOK endtok;
 
 		arguments = new ArrayList<Expression>();
 		if (token.value == TOKlbracket) {
 			endtok = TOKrbracket;
 		} else {
 			endtok = TOKrparen;
 		}
 
 		{
 			nextToken();
 			if (token.value != endtok) {
 				while (true) {
 					arg = parseAssignExp();
 					arguments.add(arg);
 					if (token.value == endtok)
 						break;
 					if (token.value == TOKeof) 
 						return arguments;
 					check(TOKcomma);
 				}
 			}
 			check(endtok);
 		}
 		return arguments;
 	}
 	
 	@SuppressWarnings("unchecked")
 	private Expression parseNewExp(Expression thisexp) {
 		Type t;
 		List<Expression> newargs = null;
 		List<Expression> arguments = null;
 		Expression e;
 
 		nextToken();
 		if (token.value == TOKlparen) {
 			newargs = parseArguments();
 		}
 
 		// An anonymous nested class starts with "class"
 		if (token.value == TOKclass) {
 			nextToken();
 			if (token.value == TOKlparen)
 				arguments = parseArguments();
 
 			List<BaseClass> baseClasses = null;
 			if (token.value != TOKlcurly)
 				baseClasses = parseBaseClasses();
 
 			IdentifierExp id = null;
 			ClassDeclaration cd = new ClassDeclaration(loc, id, baseClasses);
 
 			if (token.value != TOKlcurly) {
 				parsingErrorInsertToComplete(prevToken, "{ members }", "AnnonymousClassDeclaration");
 				cd.members = null;
 			} else {
 				nextToken();
 				List decl = parseDeclDefs(false);
 				if (token.value != TOKrcurly) {
 					parsingErrorInsertToComplete(prevToken, "ClassBody", "AnnonymousClassDeclaration");
 				}
 				nextToken();
 				cd.members = decl;
 			}
 			
 			e = new NewAnonClassExp(loc, thisexp, newargs, cd, arguments);
 
 			return e;
 		}
 
 		// #if LTORARRAYDECL
 		int lineNumber = token.lineNumber;
 		t = parseBasicType();
 		t = parseBasicType2(t);
 		if (t != null) {
 			if (t.ty == Taarray) {
 				Type index = (Type) ((TypeAArray) t).index;
 				
 				Expression e2 = index.toExpression();
 				if (e2 != null) {
 					arguments = new ArrayList<Expression>();
 					arguments.add(e2);				
 					t = new TypeDArray(t.next);
 				} else {
 					error("Need size of rightmost array", IProblem.NeedSizeOfRightmostArray, lineNumber, index);
 					NullExp nullExp = new NullExp(loc);
 					nullExp.setSourceRange(token.ptr, token.len);
 					return nullExp;
 				}
 			} else if (t.ty == Tsarray) {
 				TypeSArray tsa = (TypeSArray) t;
 				Expression e2 = tsa.dim;
 	
 				arguments = new ArrayList<Expression>();
 				arguments.add(e2);
 				
 				t = new TypeDArray(t.next);
 			} else if (token.value == TOKlparen) {
 				arguments = parseArguments();
 			}
 		}
 		/*
 		 * #else t = parseBasicType(); while (token.value == TOKmul) { t = new
 		 * TypePointer(t); nextToken(); } if (token.value == TOKlbracket) {
 		 * Expression *e;
 		 * 
 		 * nextToken(); e = parseAssignExp(); arguments = new Array();
 		 * arguments->push(e); check(TOKrbracket); t = parseDeclarator(t, NULL);
 		 * t = new TypeDArray(t); } else if (token.value == TOKlparen) arguments =
 		 * parseArguments(); #endif
 		 */
 		e = new NewExp(loc, thisexp, newargs, t, arguments);
 		return e;
 	}
 	
 	private StringExp newStringExpForCurrentToken() {
 		StringExp string = new StringExp(loc, token.string, (char) token.postfix);
 		string.setSourceRange(token.ptr, token.len);
 		return string;
 	}
 	
 	private TypeAArray newTypeAArray(Type componentType, Type keyType) {
 		TypeAArray associativeArray = new TypeAArray(componentType, keyType);
 	    associativeArray.setSourceRange(componentType.start, token.ptr + token.len - componentType.start);
 	    return associativeArray;
 	}
 	
 	private CompoundStatement newBlock(List<Statement> statements) {
 		return new CompoundStatement(loc, statements);
 	}
 	
 	private CompoundStatement newManyVarsBlock(List<Statement> statements) {
 		CompoundStatement statement = new CompoundStatement(loc, statements);
 		statement.manyVars = true;
 		return statement;
 	}
 	
 	private DebugSymbol newDebugAssignmentForCurrentToken() {
 		if (token.value == TOKint32v) {
 			return new DebugSymbol(loc, token.intValue.longValue(), newVersionForCurrentToken());
 		} else if (token.value == TOKidentifier) {
 			return new DebugSymbol(loc, new IdentifierExp(loc, token.string), newVersionForCurrentToken());
 		} else {
 			throw new RuntimeException("Can't happen");
 		}
 	}
 	
 	private Version newVersionForCurrentToken() {
 		Version version;
 		
 		if (token.value == TOKint32v) {
 			version = new Version(loc, token.string);
 			version.setSourceRange(token.ptr, token.len);
 		} else if (token.value == TOKidentifier) {
 			version = new Version(loc, token.string);
 			version.setSourceRange(token.ptr, token.len);
 		} else {
 			throw new RuntimeException("Can't happen");
 		}
 		
 		return version;
 	}
 	
 	private VersionSymbol newVersionAssignmentForCurrentToken() {
 		if (token.value == TOKint32v) {
 			return new VersionSymbol(loc, token.intValue.longValue(), newVersionForCurrentToken());
 		} else if (token.value == TOKidentifier) {
 			return new VersionSymbol(loc, new IdentifierExp(loc, token.string), newVersionForCurrentToken());
 		} else {
 			throw new RuntimeException("Can't happen");
 		}
 	}
 	
 	private VoidInitializer newVoidInitializerForToken(Token token) {
 		VoidInitializer voidInitializer = new VoidInitializer(loc);
 		voidInitializer.setSourceRange(token.ptr, token.len);
 		return voidInitializer;
 	}
 	
 	private TypeBasic newTypeBasicForCurrentToken() {
 		TypeBasic t = null;
 		switch(token.value) {
 		case TOKvoid: t = new TypeBasic(TY.Tvoid); break;
 		case TOKint8: t = new TypeBasic(TY.Tint8); break;
 		case TOKuns8: t = new TypeBasic(TY.Tuns8); break;
 		case TOKint16: t = new TypeBasic(TY.Tint16); break;
 		case TOKuns16: t = new TypeBasic(TY.Tuns16); break;
 		case TOKint32: t = new TypeBasic(TY.Tint32); break;
 		case TOKuns32: t = new TypeBasic(TY.Tuns32); break;
 		case TOKint64: t = new TypeBasic(TY.Tint64); break;
 		case TOKuns64: t = new TypeBasic(TY.Tuns64); break;
 		case TOKfloat32: t = new TypeBasic(TY.Tfloat32); break;
 		case TOKfloat64: t = new TypeBasic(TY.Tfloat64); break;
 		case TOKfloat80: t = new TypeBasic(TY.Tfloat80); break;
 		case TOKimaginary32: t = new TypeBasic(TY.Timaginary32); break;
 		case TOKimaginary64: t = new TypeBasic(TY.Timaginary64); break;
 		case TOKimaginary80: t = new TypeBasic(TY.Timaginary80); break;
 		case TOKcomplex32: t = new TypeBasic(TY.Tcomplex32); break;
 		case TOKcomplex64: t = new TypeBasic(TY.Tcomplex64); break;
 		case TOKcomplex80: t = new TypeBasic(TY.Tcomplex80); break;
 		case TOKbit: t = new TypeBasic(TY.Tbit); break;
 		case TOKbool: t = new TypeBasic(TY.Tbool); break;
 		case TOKchar: t = new TypeBasic(TY.Tchar); break;
 		case TOKwchar: t = new TypeBasic(TY.Twchar); break;
 		case TOKdchar: t = new TypeBasic(TY.Tdchar); break;
 		}
 		t.setSourceRange(token.ptr, token.len);
 		return t;
 	}
 	
 	private List<DDocComment> getLastDocComments() {
 		LinkedList<DDocComment> toReturn = new LinkedList<DDocComment>();
 		for(int i = comments.size() - 1; i >= lastDocCommentRead; i--) {
 			Comment comment = comments.get(i);
 			if (comment.isDDocComment()) {
 				toReturn.addFirst((DDocComment) comment);
 			} else {
 				break;
 			}
 		}
 		
 		lastDocCommentRead = comments.size();
 		
 		return toReturn;
 	}
 	
 	private void adjustLastDocComment() {
 		// not used, see if this is needed
 	}
 	
 	private void attachLeadingComments(ASTNode declaration) {
 		if (prevToken.leadingComment != null) {
 			declaration.postDdoc = prevToken.leadingComment;
 		}
 	}
 	
 	private void parsingErrorInsertTokenAfter(Token targetToken, String expected) {
 		error("Syntax error on token \"" + targetToken + "\", " + expected + " expected after this token", IProblem.ParsingErrorInsertTokenAfter, targetToken.lineNumber, targetToken.ptr, targetToken.len);
 	}
 	
 	private void parsingErrorDeleteToken(Token targetToken) {
 		error("Syntax error on token \"" + targetToken + "\", delete this token", IProblem.ParsingErrorDeleteToken, targetToken.lineNumber, targetToken.ptr, targetToken.len);
 	}
 	
 	private void parsingErrorInsertToComplete(Token targetToken, String insert, String toComplete) {
 		error("Syntax error, insert \"" + insert + "\" to complete " + toComplete, IProblem.ParsingErrorInsertToComplete, targetToken.lineNumber, targetToken.ptr, targetToken.len);
 	}
 	
 	private String toWord(String s) {
 		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
 	}
 	
 	private void adjustPossitionAccordingToComments(ASTNode node, List<DDocComment> preDDocs, DDocComment postDDoc) {
 		if ((preDDocs == null || preDDocs.isEmpty()) && postDDoc == null) {
 			return;
 		}
 		
 		int start = node.start;
 		int end = start + node.length;
 		
 		if (preDDocs != null && !preDDocs.isEmpty()) {
 			start = preDDocs.get(0).getStartPosition();
 		}
 		
 		if (postDDoc != null) {
 			end = postDDoc.getStartPosition() + postDDoc.getLength();
 		}
 		
 		node.setSourceRange(start, end - start);
 	}
 	
 	@Override
 	public TOK nextToken() {
 		TOK tok = super.nextToken();
 		while(tok == TOK.TOKlinecomment || tok == TOK.TOKdoclinecomment ||
 			  tok == TOK.TOKblockcomment || tok == TOK.TOKdocblockcomment ||
 			  tok == TOK.TOKpluscomment || tok == TOK.TOKdocpluscomment) {
 			
 			Comment comment;
 			switch(tok) {
 			case TOKlinecomment:
 				comment = ast.newCodeComment();
 				comment.setKind(CodeComment.Kind.LINE_COMMENT);
 				break;
 			case TOKblockcomment:
 				comment = ast.newCodeComment();
 				comment.setKind(CodeComment.Kind.BLOCK_COMMENT);
 				break;
 			case TOKpluscomment:
 				comment = ast.newCodeComment();
 				comment.setKind(CodeComment.Kind.PLUS_COMMENT);
 				break;
 			case TOKdoclinecomment:
 				comment = ast.newDDocComment(token.string);
 				comment.setKind(CodeComment.Kind.LINE_COMMENT);
 				break;
 			case TOKdocblockcomment:
 				comment = ast.newDDocComment(token.string);
 				comment.setKind(CodeComment.Kind.BLOCK_COMMENT);
 				break;
 			case TOKdocpluscomment:
 				comment = ast.newDDocComment(token.string);
 				comment.setKind(CodeComment.Kind.PLUS_COMMENT);
 				break;
 			default:
 				throw new IllegalStateException("Can't happen");
 			}
 			
 			comment.setSourceRange(token.ptr, token.len);
 			
 			comments.add(comment);
 			attachCommentToCurrentToken(comment);
 			
 			tok = super.nextToken();
 		}
 		
 		while(tok == TOK.TOKPRAGMA) {
 			if (token.ptr == 0 && token.string.length() > 1 && token.string.charAt(1) == '!') {
 				// Script line
 				Pragma pragma = ast.newPragma();
 				pragma.setSourceRange(0, token.len);
 				pragmas.add(pragma);
 			} else {
 				Pragma pragma = ast.newPragma();
 				pragma.setSourceRange(token.ptr, token.len);
 				pragmas.add(pragma);
 				
 				// Let's see if it's correct
 				StringTokenizer st = new StringTokenizer(token.string.substring(1));
 				if (st.countTokens() != 3) {
 					error("#line integer [\"filespec\"]\\n expected", IProblem.InvalidPragmaSyntax, token);
 					setMalformed(pragma);
 				} else {
 					String value = st.nextToken();
 					if (!"line".equals(value)) {
 						error("#line integer [\"filespec\"]\\n expected", IProblem.InvalidPragmaSyntax, token);
 						setMalformed(pragma);
 					} else {
 						value = st.nextToken();
 						try {
 							int num = Integer.parseInt(value);
 							if (num <= 0) throw new NumberFormatException();
 							
 							value = st.nextToken();
 							if (!"__FILE__".equals(value)) {
 								if (value.length() < 2 || value.charAt(0) != '"' || value.charAt(value.length() - 1) != '"') {
 									error("#line integer [\"filespec\"]\\n expected", IProblem.InvalidPragmaSyntax, token);
 									setMalformed(pragma);
 								}
 							}
 						} catch (NumberFormatException e) {
 							error("#line integer [\"filespec\"]\\n expected", IProblem.InvalidPragmaSyntax, token);
 							setMalformed(pragma);
 						}
 					}
 				}
 			}
 			
 			tok = nextToken();
 		}
 		
 		return tok;
 	}
 	
 	@Override
 	protected void newline(boolean inComment) {
 		super.newline(inComment);
 		if (!inComment) {
 			appendLeadingComments = false;
 		}
 	}
 	
 	private IdentifierExp newIdentifierExp() {
 		return new IdentifierExp(loc, token);
 	}
 	
 	private void attachCommentToCurrentToken(Comment comment) {
 		if (!appendLeadingComments || !comment.isDDocComment()) return;
 		
 		prevToken.leadingComment = (DDocComment) comment;
 	}
 	
 }
