 package descent.internal.compiler.parser;
 
 import static descent.internal.compiler.parser.LINK.LINKc;
 import static descent.internal.compiler.parser.LINK.LINKcpp;
 import static descent.internal.compiler.parser.LINK.LINKd;
 import static descent.internal.compiler.parser.LINK.LINKdefault;
 import static descent.internal.compiler.parser.LINK.LINKpascal;
 import static descent.internal.compiler.parser.LINK.LINKsystem;
 import static descent.internal.compiler.parser.LINK.LINKwindows;
 import static descent.internal.compiler.parser.STC.STCconst;
 import static descent.internal.compiler.parser.STC.STCextern;
 import static descent.internal.compiler.parser.STC.STCfinal;
 import static descent.internal.compiler.parser.STC.STCgshared;
 import static descent.internal.compiler.parser.STC.STCimmutable;
 import static descent.internal.compiler.parser.STC.STCin;
 import static descent.internal.compiler.parser.STC.STCinvariant;
 import static descent.internal.compiler.parser.STC.STClazy;
 import static descent.internal.compiler.parser.STC.STCmanifest;
 import static descent.internal.compiler.parser.STC.STCout;
 import static descent.internal.compiler.parser.STC.STCref;
 import static descent.internal.compiler.parser.STC.STCscope;
 import static descent.internal.compiler.parser.STC.STCshared;
 import static descent.internal.compiler.parser.STC.STCstatic;
 import static descent.internal.compiler.parser.STC.STCtls;
 import static descent.internal.compiler.parser.TOK.*;
 import static descent.internal.compiler.parser.TY.Taarray;
 import static descent.internal.compiler.parser.TY.Tfunction;
 import static descent.internal.compiler.parser.TY.Tident;
 import static descent.internal.compiler.parser.TY.Tsarray;
 import static descent.internal.compiler.parser.Type.MODconst;
 import static descent.internal.compiler.parser.Type.MODinvariant;
 import static descent.internal.compiler.parser.Type.MODshared;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import descent.core.compiler.CharOperation;
 import descent.core.compiler.IProblem;
 
 public class Parser extends Lexer {
 	
 	// Tokens expected in several parse locations
 	protected final static char[][] moduleExpectations =  { TOKmodule.charArrayValue };
 	protected final static char[][] declDefsExpectations = toCharArray(new TOK[] { 
 		TOKenum, TOKstruct, TOKunion, TOKclass, TOKinterface, TOKimport,
 		TOKtemplate, TOKmixin, TOKwchar, TOKdchar, /* TOKbit, */ TOKbool,
 		TOKchar, TOKint8, TOKuns8, TOKint16, TOKuns16, TOKint32,
 		TOKuns32, TOKint64, TOKuns64, TOKfloat32, TOKfloat64,
 		TOKfloat80, TOKimaginary32, TOKimaginary64, TOKimaginary80, 
 		TOKcomplex32, TOKcomplex64, TOKcomplex80, TOKvoid, TOKalias,
 		TOKtypedef, TOKtypeof, TOKthis, TOKinvariant, TOKunittest,
 		TOKnew, TOKdelete, TOKstatic, TOKconst, TOKfinal, TOKauto, TOKpure, TOKshared, TOKtls, TOKgshared,
 		TOKscope, TOKoverride, TOKabstract, TOKsynchronized, TOKdeprecated,
 		TOKextern, TOKprivate, TOKpackage, TOKprotected, TOKpublic,
 		TOKexport, TOKalign, TOKpragma, TOKdebug, TOKversion
 		/*, TOKiftype */ 
 	});
 	protected final static char[][] declDefs_Lstc2Expectations = toCharArray(new TOK[] {
 		TOKconst, TOKinvariant, TOKfinal, TOKauto, TOKscope,
 		TOKoverride, TOKabstract, TOKsynchronized, TOKdeprecated, TOKpure, TOKshared, TOKtls, TOKgshared,
 	});
 	protected final static char[][] afterStaticExpectations = { 
 		TOKthis.charArrayValue, TOKassert.charArrayValue, TOKimport.charArrayValue
 	};
 	protected final static char[][] elseExpectations = { TOKelse.charArrayValue };
 	protected final static char[][] thisExpectations = { TOKthis.charArrayValue };
 	protected final static char[][] aliasExpectations = { TOKalias.charArrayValue };
 	protected final static char[][] typedefAliasExpectations = { TOKtypedef.charArrayValue, TOKalias.charArrayValue };
 	protected final static char[][] typeofExpectations = { TOKtypeof.charArrayValue };
 	protected final static char[][] classExpectations = { TOKclass.charArrayValue };
 	protected final static char[][] voidExpectations = { TOKvoid.charArrayValue };
 	protected final static char[][] traitsExpectations = { TOKtraits.charArrayValue };
 	protected final static char[][] whileExpectations = { TOKwhile.charArrayValue };
 	protected final static char[][] autoExpectations = { TOKauto.charArrayValue };
 	protected final static char[][] catchExpectations = { TOKcatch.charArrayValue };
 	protected final static char[][] finallyExpectations = { TOKfinally.charArrayValue };
 	protected final static char[][] contractsExpectations = { TOKin.charArrayValue, TOKout.charArrayValue, TOKbody.charArrayValue };
 	protected final static char[][] parameters1Expectations = toCharArray(new TOK[] { 
 		TOKin, TOKout, TOKinout, TOKref, TOKlazy
 	});
 	protected final static char[][] parameters2Expectations = toCharArray(new TOK[] { 
 		TOKconst, TOKinvariant, TOKin, TOKout, TOKinout, TOKref,
 		TOKlazy, TOKscope, TOKfinal, TOKstatic
 	});
 	protected final static char[][] basicTypeExpectations = toCharArray(new TOK[] { 
 		TOKwchar, TOKdchar, /* TOKbit, */ TOKbool,
 		TOKchar, TOKint8, TOKuns8, TOKint16, TOKuns16, TOKint32,
 		TOKuns32, TOKint64, TOKuns64, TOKfloat32, TOKfloat64,
 		TOKfloat80, TOKimaginary32, TOKimaginary64, TOKimaginary80, 
 		TOKcomplex32, TOKcomplex64, TOKcomplex80, TOKvoid,
 		TOKtypeof, TOKconst, TOKinvariant
 	});
 	protected final static char[][] delegateFunctionExpectations = toCharArray(new TOK[] { 
 		TOKdelegate, TOKfunction
 	});
 	protected final static char[][] modifierExpectations = toCharArray(new TOK[] { 
 		TOKconst, TOKinvariant, TOKstatic, TOKfinal, TOKauto, TOKscope,
 		TOKoverride, TOKabstract, TOKsynchronized, TOKdeprecated,
 		TOKextern, TOKpure, TOKtls, TOKshared, TOKgshared,
 	});
 	protected final static char[][] statementExpectations = toCharArray(new TOK[] { 
 		TOKtypeof, TOKassert, TOKthis, TOKsuper, TOKnull, TOKtrue,
 		TOKfalse, TOKcast, TOKnew, TOKdelete, TOKdelegate, TOKfunction,
 		TOKtypeid, TOKis, TOKstatic, TOKwchar, TOKdchar, /* TOKbit, */ TOKbool,
 		TOKchar, TOKint8, TOKuns8, TOKint16, TOKuns16, TOKint32,
 		TOKuns32, TOKint64, TOKuns64, TOKfloat32, TOKfloat64,
 		TOKfloat80, TOKimaginary32, TOKimaginary64, TOKimaginary80, 
 		TOKcomplex32, TOKcomplex64, TOKcomplex80, TOKvoid,
 		TOKtypeof, TOKconst, TOKinvariant, TOKalias, TOKtypedef, TOKauto, TOKpure,
 		TOKextern, TOKstruct, TOKunion, TOKclass, TOKinterface,
 		TOKenum, TOKmixin, TOKwhile, TOKdo, TOKfor, TOKforeach,
 		TOKforeach_reverse, TOKif, TOKscope, TOKdebug, TOKversion,
 		TOKpragma, TOKswitch, TOKcase, TOKreturn, TOKdefault,
 		TOKbreak, TOKcontinue, TOKgoto, TOKsynchronized, TOKwith,
 		TOKtry, TOKthrow, TOKvolatile, TOKasm
 	});
 	protected final static char[][] primaryExpExpectations = toCharArray(new TOK[] { 
 		TOKthis, TOKsuper, TOKnull, TOKtrue, TOKfalse, TOKvoid,
 		TOKint8, TOKuns8, TOKint16, TOKuns16, TOKint32, TOKuns32,
 		TOKint64, TOKuns64, TOKfloat32, TOKfloat64, TOKfloat80,
 		TOKimaginary32, TOKimaginary64, TOKimaginary80,
 		TOKcomplex32, TOKcomplex64, TOKcomplex80, /* TOKbit, */
 		TOKbool, TOKchar, TOKwchar, TOKdchar, TOKtypeof, 
 		TOKtypeid, TOKis, TOKassert, TOKmixin, TOKimport,
 		TOKfunction, TOKdelegate
 	});
 	protected final static char[][] unaryExpExpectations = { TOKdelete.charArrayValue, TOKnew.charArrayValue, TOKcast.charArrayValue };
 	protected final static char[][] traitsArgsExpectations = { 
 		Id.isArithmetic, Id.isFloating, Id.isIntegral, Id.isScalar, 
 		Id.isUnsigned, Id.isAssociativeArray, Id.isStaticArray,
 		Id.isAbstractClass, Id.isFinalClass, Id.isAbstractFunction, 
 		Id.isVirtualFunction, Id.isFinalFunction, 
 		Id.hasMember, Id.getMember, Id.getVirtualFunctions,
 		Id.classInstanceSize, Id.allMembers, Id.derivedMembers,
 		Id.isSame, Id.compiles
 	};
 	protected final static char[][] scopeArgsExpectations = { 
 		Id.exit, Id.failure, Id.success
 	};
 	protected final static char[][] externArgsExpectations = { 
 		Id.C, Id.Cpp, Id.D, Id.Pascal, Id.System, Id.Windows
 	};
 	protected final static char[][] pragmaArgsExpectations = { 
 		Id.msg, Id.lib
 	};
 	
 	private static char[][] toCharArray(TOK[] toks) {
 		char[][] expect = new char[toks.length][];
 		for (int i = 0; i < toks.length; i++) {
 			expect[i] = toks[i].charArrayValue;
 		}
 		return expect;
 	}
 	
 	private final static boolean LTORARRAYDECL = true;
 	
 	public final static int PSsemi = 1;		// empty ';' statements are allowed
 	public final static int PSscope = 2;	// start a new scope
 	public final static int PScurly = 4;	// { } statement is required
 	public final static int PScurlyscope = 8;	// { } starts a new scope
 	
 	/**
 	 * Wheter to do skip parsing of function bodies if they do not
 	 * contain inner declarations.
 	 */
 	public boolean diet;
 
 	public Module module;
 	private ModuleDeclaration md;
 	private int inBrackets;	
 	
 	private List<Comment> comments;
 	private List<Pragma> pragmas;
 	private int lastCommentRead = 0;
 	private boolean appendLeadingComments = true;
 	
 	private LINK linkage = LINKd;
 	
 	public IStringTableHolder holder = new StringTableHolder();
 
 	public Parser(int apiLevel, String source) {
 		this(apiLevel, source.toCharArray(), 0, source.length(), null);
 	}
 
 	public Parser(int apiLevel, char[] source) {
 		this(apiLevel, source, 0, source.length);
 	}
 	
 	public Parser(int apiLevel, char[] source, char[] filename) {
 		this(apiLevel, source, 0, source.length, filename);
 	}
 	
 	public Parser(int apiLevel, char[] source, int offset, 
 			int length) {
 		this(apiLevel, source, offset, length, null, null, false, null);
 	}
 	
 	public Parser(int apiLevel, char[] source, int offset, 
 			int length, char[] filename) {
 		this(apiLevel, source, offset, length, filename, true);
 	}
 	
 	public Parser(int apiLevel, char[] source, int offset, 
 			int length, char[] filename, boolean recordLineSeparator) {
 		this(apiLevel, source, offset, length, null, null, recordLineSeparator, false, filename);
 	}
 	
 	public Parser(int apiLevel, char[] source, int offset, 
 			int length, char[][] taskTags, char[][] taskPriorities, boolean isTaskCaseSensitive, char[] filename) {
 		this(apiLevel, source, offset, length, taskTags, taskPriorities, isTaskCaseSensitive, filename, new ASTNodeEncoder(apiLevel));
 	}
 	
 	public Parser(int apiLevel, char[] source, int offset, 
 			int length, char[][] taskTags, char[][] taskPriorities, boolean recordLineSeparator, boolean isTaskCaseSensitive, char[] filename) {
 		this(apiLevel, source, offset, length, taskTags, taskPriorities, recordLineSeparator, isTaskCaseSensitive, filename, new ASTNodeEncoder(apiLevel));
 	}
 	
 	public Parser(int apiLevel, char[] source, int offset, 
 			int length, char[][] taskTags, char[][] taskPriorities, boolean isTaskCaseSensitive, char[] filename, ASTNodeEncoder encoder) {
 		this(apiLevel, source, offset, length, taskTags, taskPriorities, true, isTaskCaseSensitive, filename, encoder);
 	}
 	
 	public Parser(int apiLevel, char[] source, int offset, 
 			int length, char[][] taskTags, char[][] taskPriorities, boolean recordLineSeparator, boolean isTaskCaseSensitive, char[] filename, ASTNodeEncoder encoder) {
 		this(source, offset, length, 
 				true /* tokenize comments */, 
 				true /* tokenize pragmas */,
 				false /* don't tokenize whitespace */, 
 				recordLineSeparator,
 				apiLevel,
 				taskTags, taskPriorities, isTaskCaseSensitive,
 				filename, encoder);
 	}
 	
 	public Parser(char[] source, int offset, int length,
 			boolean tokenizeComments, boolean tokenizePragmas,
 			boolean tokenizeWhiteSpace, boolean recordLineSeparator,
 			int apiLevel,
 			char[][] taskTags, char[][] taskPriorities, boolean isTaskCaseSensitive,
 			char[] filename) {
 		this(source, offset, length, tokenizeComments, tokenizePragmas,
 				tokenizeWhiteSpace, recordLineSeparator, apiLevel,
 				taskTags, taskPriorities, isTaskCaseSensitive, filename, new ASTNodeEncoder(apiLevel));
 	}
 	
 	public Parser(char[] source, int offset, int length,
 			boolean tokenizeComments, boolean tokenizePragmas,
 			boolean tokenizeWhiteSpace, boolean recordLineSeparator,
 			int apiLevel,
 			char[][] taskTags, char[][] taskPriorities, boolean isTaskCaseSensitive,
 			char[] filename, ASTNodeEncoder encoder) {
 		super(source, offset, length, tokenizeComments, tokenizePragmas,
 				tokenizeWhiteSpace, recordLineSeparator,
 				apiLevel, filename, encoder);
 		if (tokenizeComments) {
 			this.comments = new ArrayList<Comment>(0);
 		}
 		if (tokenizePragmas) {
 			this.pragmas = new ArrayList<Pragma>();
 		}
 		this.taskTags = taskTags;
 		this.taskPriorities = taskPriorities;
 		this.isTaskCaseSensitive = isTaskCaseSensitive;
 		
 		// nextToken();
 	}
 	
 	public Module parseModuleObj() {
 		char[] theFilename = filename == null ? null : getName(filename);
 		module = new Module(theFilename == null ? null : new String(theFilename), theFilename == null ? null : new IdentifierExp(theFilename));
 		parseModuleObj(module);
 		return module;
 	}
 	
 	private char[] getName(char[] filename) {
 		int indexOfSlash = CharOperation.lastIndexOf('/', filename);
 		if (indexOfSlash < 0) {
 			indexOfSlash = CharOperation.lastIndexOf('\\', filename);
 		}
 		if (indexOfSlash < 0) {
 			indexOfSlash = -1;
 		}
 		int indexOfDot = CharOperation.lastIndexOf('.', filename);
 		if (indexOfDot < 0) {
 			indexOfDot = filename.length;
 		}		
 		return CharOperation.subarray(filename, indexOfSlash + 1, indexOfDot);
 	}
 
 	public void parseModuleObj(Module module) {
 		this.module = module;
 		module.members = parseModule();
 		module.sourceMembers = new Dsymbols(module.members);
 		module.md = md;
 		if (comments != null) {
 			module.comments = comments.toArray(new Comment[comments.size()]);
 		}
 		if (pragmas != null) {
 			module.pragmas = pragmas.toArray(new Pragma[pragmas.size()]);
 		}
 		module.lineEnds = getLineEnds();
 		
 		if (taskTags != null) {
 			addTaskTagsToProblems();
 		}
 		
 		module.apiLevel = apiLevel;
 		module.problems = problems;
 		module.start = 0;
 		module.length = this.end;
 	}
 	
 	private void addTaskTagsToProblems() {
 		for(int i = 0; i < foundTaskCount; i++) {
 			IProblem problem = Problem.newTask(
 					new String(CharOperation.concat(foundTaskTags[i], foundTaskMessages[i], ' ')), 
 					getLineNumber(foundTaskPositions[i][0]), 
 					foundTaskPositions[i][0], 
 					foundTaskPositions[i][1] - foundTaskPositions[i][0]);
 			reportProblem(problem);
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	protected Dsymbols parseModule() {
 		boolean safe = false;
 		
 		if (token.value == null) {
 			nextToken();
 		}
 		
 	    Dsymbols decldefs = new Dsymbols();
 	    
 	    // Special treatment if the file starts with Ddoc
 	    if (token.value == TOKidentifier && CharOperation.equals(token.sourceString, Id.Ddoc)) {
 	    	return decldefs;
 	    }
 
 		// ModuleDeclation leads off
 	    expect(moduleExpectations);
 		if (token.value == TOKmodule) {
 			int start = token.ptr;
 			
 			List<Comment> moduleDocComments = getLastComments();
 			
 			nextToken();
 			
 			if (apiLevel >= D2) {
 				if (token.value == TOKlparen)
 				{
 				    nextToken();
 				    if (token.value != TOKidentifier)
 				    {
 				    	parsingErrorInsertToComplete(prevToken, "module (system) identifier", "module declaration");
 						
 				    	decldefs = parseDeclDefs(false);
 						if (token.value != TOKeof) {
 							parsingErrorDeleteToken(token);
 						}
 						return decldefs;
 				    }
 
 				    if (CharOperation.equals(token.sourceString, Id.system)) {
 				    	safe = true;
 				    } else {
 				    	parsingErrorInsertToComplete(prevToken, "module (system) identifier", "module declaration");
 				    }
 				    nextToken();
 				    check(TOKrparen);
 				}
 			}
 			
 			if (token.value != TOKidentifier) {
 				
 				// Issue a creation of an empty module declaration
 				newModuleDeclaration(null, null, safe);
 				
 				parsingErrorDeleteToken(prevToken);
 				
 				decldefs = parseDeclDefs(false);
 				if (token.value != TOKeof) {
 					parsingErrorDeleteToken(token);
 				}
 				return decldefs;
 			} else {
 				Identifiers a = null;
 				IdentifierExp id = null;
 				id = newIdentifierExp();
 				while (nextToken() == TOKdot) {
 					if (a == null) {
 						a = new Identifiers(3);
 					}
 					a.add(id);
 					nextToken();
 					if (token.value != TOKidentifier) {
 						
 						// Issue a creation of an empty module declaration
 						newModuleDeclaration(a, null, safe);
 						
 						parsingErrorInsertTokenAfter(prevToken, ";");
 						
 						decldefs = parseDeclDefs(false);
 						if (token.value != TOKeof) {
 							parsingErrorDeleteToken(token);
 						}
 						return decldefs;
 					}
 					id = newIdentifierExp();
 				}
 
 				md = newModuleDeclaration(a, id, safe);
 				md.setSourceRange(start, token.ptr + token.sourceLen - start);
 				md.safe = safe;
 				setPreComments(md, moduleDocComments);
 
 				if (token.value != TOKsemicolon) {
 					parsingErrorInsertTokenAfter(prevToken, ";");
 				} else {
 					nextToken();
 				}
 				
 				attachLeadingComments(md);
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
 
 	private Dsymbols parseModule_LErr(Dsymbols decldefs) {
 		while (token.value != TOKsemicolon && token.value != TOKeof) {
 			nextToken();
 		}
 	    nextToken();
 	    return decldefs;
 	}
 	
 	public Dsymbols parseDeclDefs(boolean once) {
 		return parseDeclDefs(once, false);
 	}
 
 	@SuppressWarnings("unchecked")
 	public Dsymbols parseDeclDefs(boolean once, boolean thinksItsD2) {
 		if (token.value == null) {
 			nextToken();
 		}
 		
 		Dsymbol s = null;
 		Dsymbols decldefs;
 		Dsymbols a = new Dsymbols();
 		Dsymbols aelse;
 		PROT prot;
 		int stc;
 		boolean[] isSingle = new boolean[1];
 		boolean attachLeadingComments = true;
 		
 		decldefs = new Dsymbols();
 		do {
 			List<Comment> lastComments = getLastComments();
 			isSingle[0] = false;
 			attachLeadingComments = true;
 			
 			expect(declDefsExpectations);
 			
 			int start = token.ptr;
 			switch (token.value) {
 			case TOKenum:
 				if (apiLevel >= D2) {
 					/* Determine if this is a manifest constant declaration,
 					 * or a conventional enum.
 					 */
 					Token t = peek(token);
 					if (t.value == TOKlcurly || t.value == TOKcolon) {
 						s = parseEnum();
 					} else if (t.value != TOKidentifier) {
 						// goto Ldeclaration;
 						a = parseDeclarations(lastComments);
 						decldefs.addAll(a);
 						continue;
 					} else {
 						t = peek(t);
 						if (t.value == TOKlcurly || t.value == TOKcolon
 								|| t.value == TOKsemicolon) {
 							s = parseEnum();
 						} else {
 							// goto Ldeclaration;
 							a = parseDeclarations(lastComments);
 							decldefs.addAll(a);
 							continue;
 						}
 					}
 				} else {
 					s = parseEnum();
 				}
 				break;
 
 			case TOKstruct:
 			case TOKunion:
 			case TOKclass:
 			case TOKinterface:
 				s = parseAggregate();
 				break;
 
 			case TOKimport:
 				s = parseImport(decldefs, false, token.ptr, lastComments);
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
 				    s = newCompileDeclaration(filename, lineNumber, e);
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
 				a = parseDeclarations(lastComments, thinksItsD2);
 				decldefs.addAll(a);
 				continue;
 
 			case TOKthis:
 				s = parseCtor();
 				break;
 				
 			case TOKassign:
 				if (apiLevel >= D2) {
 					s = parsePostBlit();
 					break;
 				} else {
 					parsingErrorDeleteToken(token);
 					nextToken();
 					continue;
 				}
 			case TOKtilde:
 				s = parseDtor();
 				break;
 
 			case TOKinvariant:
 				if (apiLevel >= D2) {
 				    Token t;
 					t = peek(token);
 					if (t.value == TOKlparen) {
 						if (peek(t).value == TOKrparen) {
 							// invariant() forms start of class invariant
 							s = parseInvariant();
 						} else {
 							// invariant(type)
 							// goto Ldeclaration;
 							a = parseDeclarations(lastComments);
 							decldefs.addAll(a);
 							continue;
 						}
 					} else {
 						stc = STCimmutable;
 						
 						Modifier modifier = newModifier();
 						
 						// goto Lstc;
 						nextToken();
 						
 						s= parseDeclDefs_Lstc2(isSingle, modifier, stc, decldefs);
 					}
 				} else {
 					s = parseInvariant();
 				}
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
 				nextToken();
 				
 				expect(afterStaticExpectations);
 				if (token.value == TOKthis) {
 					s = parseStaticCtor();
 				} else if (token.value == TOKtilde) {
 					s = parseStaticDtor();
 				} else if (token.value == TOKassert) {
 					s = parseStaticAssert();
 				} else if (token.value == TOKif) {
 					StaticIfCondition condition = parseStaticIfCondition();
 					a = parseBlock();
 					aelse = null;
 					
 					expect(elseExpectations);
 					if (token.value == TOKelse) {
 						nextToken();
 						aelse = parseBlock();
 					}					
 					s = newStaticIfDeclaration(condition, a, aelse);
 					attachLeadingComments = prevToken.value == TOKrcurly;
 					break;
 				} else if (token.value == TOKimport) {
 					s = parseImport(decldefs, true, prevToken.ptr, lastComments);
 				} else {
 					stc = STCstatic;
 					
 					// goto Lstc2;
 					
 					Modifier modifier = newModifier(prevToken);
 					
 					s = parseDeclDefs_Lstc2(isSingle, modifier, stc, decldefs);
 				}
 				break;
 
 			case TOKconst:
 				if (apiLevel >= D2 && peek(token).value == TOKlparen) {
 					// goto Ldeclaration
 					a = parseDeclarations(lastComments);
 					decldefs.addAll(a);
 					continue;
 				} else {
 					stc = STCconst;
 					
 					Modifier modifier = newModifier();
 					
 					// goto Lstc;
 					nextToken();
 					
 					s= parseDeclDefs_Lstc2(isSingle, modifier, stc, decldefs);
 					break;
 				}
 			case TOKimmutable:
 				if (apiLevel >= D2) {
 					if (peek(token).value == TOKlparen) {
 						// goto Ldeclaration
 						a = parseDeclarations(lastComments);
 						decldefs.addAll(a);
 						continue;
 					} else {
 						stc = STCinvariant;
 						
 						Modifier modifier = newModifier();
 						
 						// goto Lstc;
 						nextToken();
 						
 						s= parseDeclDefs_Lstc2(isSingle, modifier, stc, decldefs);
 						break;
 					}
 				} else {
 					parsingErrorDeleteToken(token);
 					nextToken();
 					continue;
 				}
 			case TOKshared:
 				if (apiLevel >= D2) {
 					if (peek(token).value == TOKlparen) {
 						// goto Ldeclaration
 						a = parseDeclarations(lastComments);
 						decldefs.addAll(a);
 						continue;
 					} else {
 						stc = STCshared;
 						
 						Modifier modifier = newModifier();
 						
 						// goto Lstc;
 						nextToken();
 						
 						s= parseDeclDefs_Lstc2(isSingle, modifier, stc, decldefs);
 						break;
 					}
 				} else {
 					parsingErrorDeleteToken(token);
 					nextToken();
 					continue;
 				}
 			case TOKfinal:
 			case TOKauto:
 			case TOKscope:
 			case TOKoverride:
 			case TOKabstract:
 			case TOKsynchronized:
 			case TOKdeprecated:
 			case TOKnothrow:
 			case TOKpure:
 			case TOKref:
 			case TOKgshared:
 			case TOKtls:
 				if (apiLevel < D2 && (
 						token.value == TOKnothrow ||
 						token.value == TOKpure ||
 						token.value == TOKref ||
 						token.value == TOKgshared ||
 						token.value == TOKtls
 						)) {
 					parsingErrorDeleteToken(token);
 					nextToken();
 					continue;
 				}
 				
 				stc = STC.fromTOK(token.value);
 				
 				Modifier modifier = newModifier();
 				
 				// goto Lstc;
 				nextToken();
 				
 				s= parseDeclDefs_Lstc2(isSingle, modifier, stc, decldefs);
 				break;
 
 			case TOKextern:
 				if (peek(token).value != TOKlparen) {
 					stc = STCextern;
 					
 					modifier = newModifier();
 					
 					// goto Lstc;
 					nextToken();
 					
 					s = parseDeclDefs_Lstc2(isSingle, modifier, stc, decldefs);
 					break;
 				}
 				{
 					LINK linksave = linkage;
 					LINK linkage = parseLinkage();
 					a = parseBlock();
 					s = newLinkDeclaration(linkage, a);
 					attachLeadingComments = prevToken.value == TOKrcurly;
 					linkage = linksave;
 					break;
 				}
 			case TOKprivate:
 			case TOKpackage:
 			case TOKprotected:
 			case TOKpublic:
 			case TOKexport:
 				
 				prot = PROT.fromTOK(token.value);
 				
 				modifier = newModifier();
 				
 				// goto Lprot;
 				nextToken();
 				
 				if (apiLevel >= D2) {
 					switch (token.value) {
 					case TOKprivate:
 					case TOKpackage:
 					case TOKprotected:
 					case TOKpublic:
 					case TOKexport:
 						if (!muteErrors) {
 							acceptProblem(Problem.newSyntaxError(
 									IProblem.RedundantProtectionAttribute,
 									token.lineNumber, token.ptr, token.sourceLen));
 						}
 						break;
 					}
 				}
 				
 				boolean isColon = token.value == TOKcolon;
 				a = parseBlock(isSingle);
 				s = new ProtDeclaration(prot, a, modifier, isSingle[0], isColon);
 				attachLeadingComments = prevToken.value == TOKrcurly;
 				break;
 				
 			case TOKalign: {
 				long n;
 
 				s = null;
 				nextToken();
 				if (token.value == TOKlparen) {
 					nextToken();
 					if (token.value == TOKint32v) {
 						n = token.intValue.longValue();
 					} else {
 						parsingErrorInsertTokenAfter(prevToken, "Integer");
 						n = 1;
 					}
 					nextToken();
 					check(TOKrparen);
 				} else {
 					n = new Global().structalign; // default
 				}
 				a = parseBlock();
 				s = newAlignDeclaration((int) n, a);
 				attachLeadingComments = prevToken.value == TOKrcurly;
 				break;
 			}
 
 			case TOKpragma: {
 				IdentifierExp ident;
 				Expressions args = null;
 
 				nextToken();
 				check(TOKlparen);
 				
 				expect(pragmaArgsExpectations);
 				if (token.value != TOKidentifier) {
 					parsingErrorInsertTokenAfter(prevToken, "Identifier");
 					// goto Lerror;
 					s = parseDeclDefs_Lerror();
 					continue;
 				}
 				ident = newIdentifierExp();
 				nextToken();
 				if (token.value == TOKcomma && peekNext() != TOKrparen) {
 					args = parseArguments(); // pragma(identifier, args...)
 				} else {
 					check(TOKrparen); // pragma(identifier)
 				}
 
 				if (token.value == TOKsemicolon) {
 					a = null;
 				} else {
 					a = parseBlock();
 				}
 				
 				s = newPragmaDeclaration(filename, lineNumber, ident, args, a);
 				attachLeadingComments = prevToken.value == TOKrcurly;
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
 				
 				s = newConditionalDeclaration(debugCondition, a, aelse);
 				attachLeadingComments = prevToken.value == TOKrcurly;
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
 				
 				s = newConditionalDeclaration(versionCondition, a, aelse);
 				attachLeadingComments = prevToken.value == TOKrcurly;
 				break;
 
 			case TOKiftype:		
 				IftypeCondition iftypeCondition = parseIftypeCondition();
 				
 				a = parseBlock();
 				aelse = null;
 				if (token.value == TOKelse) {
 					nextToken();
 					aelse = parseBlock();
 				}				
 
 				s = newConditionalDeclaration(iftypeCondition, a, aelse);
 				attachLeadingComments = prevToken.value == TOKrcurly;
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
 				s.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 				if (s instanceof TemplateDeclaration 
 						&& ((TemplateDeclaration) s).wrapper) {
 					((TemplateDeclaration) s).members.get(0).setSourceRange(s.start, s.length);
 				}
 				
 				setPreComments(s, lastComments);
 				if (attachLeadingComments) {
 					attachLeadingComments(s);
 				}
 				
 				// Hook method for subclasses
 				if (s instanceof AggregateDeclaration) {
 					s = endAggregateDeclaration((AggregateDeclaration) s);
 				}
 				
 				decldefs.add(s);
 			}
 		} while (!once);
 		return decldefs;
 	}
 
 	/*****************************************
 	 * Parse a postblit definition:
 	 *	=this() { body }
 	 * Current token is '='.
 	 */
 	private PostBlitDeclaration parsePostBlit() {
 		int lineNumber = this.lineNumber;
 
 		nextToken();
 
 		int start = token.ptr;
 
 		check(TOKthis);
 		check(TOKlparen);
 		check(TOKrparen);
 
 		PostBlitDeclaration f = newPostBlitDeclaration(filename, lineNumber);
 		f.thisStart = start;
 		parseContracts(f);
 		return f;
 	}
 
 	private Modifier newModifier() {
 		return new Modifier(token, lineNumber);
 	}
 	
 	private Modifier newModifier(Token token) {
 		return new Modifier(token, lineNumber);
 	}
 
 	private Dsymbol parseDeclDefs_Lerror() {
 		while (token.value != TOKsemicolon && token.value != TOKeof) {
 			nextToken();
 		}
 		nextToken();
 		return null;
 	}
 	
 	private Dsymbol parseDeclDefs_Lstc2(boolean[] isSingle, Modifier modifier, int stc, Dsymbols decldefs) {
 		int start = prevToken.ptr;
 		int firstTokenStart = start;
 		int firstTokenLinnum = prevToken.lineNumber;
 		int firstTokenSourceLen = prevToken.sourceLen;
 		char[] firstTokenString = prevToken.getRawTokenSource();
 		
 		List<Modifier> modifiers = new ArrayList<Modifier>();
 		modifiers.add(modifier);
 
 		// Descent: better error reporting 
 		boolean thinksItsD2 = apiLevel < D2 && token.value == TOKlparen && (prevToken.value == TOKinvariant || prevToken.value == TOKimmutable || prevToken.value == TOKconst);
 		if (thinksItsD2) {
 			error(prevToken.value == TOKinvariant ? IProblem.InvariantAsAttributeIsOnlySupportedInD2 : IProblem.ConstAsAttributeIsOnlySupportedInD2, prevToken);
 			nextToken();
 		}
 		
 		Dsymbol s = null;
 		int storage_class = 0;
 		
 		boolean repeat = true;
 		while(repeat) {
 			
 			expect(declDefs_Lstc2Expectations);
 			switch (token.value)
 			{
 			    case TOKconst:
 			    case TOKinvariant:
 			    case TOKimmutable:
 			    case TOKshared:
 			    	if (apiLevel >= D2) {
 						// If followed by a (, it is not a storage class
 						if (peek(token).value == TOKlparen) {
 							repeat = false;
 						    break;
 						}
 						if (token.value == TOKconst) {
 						    stc |= STCconst;
 						} else if (token.value == TOKshared) {
 							stc |= STCshared;
 						} else {
 						    stc |= STCinvariant;
 						}
 						
 						modifier = newModifier();
 						
 						if ((storage_class & stc) != 0) {
 							error(IProblem.RedundantStorageClass, token.lineNumber, modifier);
 						}
 						composeStorageClass(storage_class | stc);
 						storage_class |= stc;
 						
 				    	modifiers.add(modifier);
 				    	nextToken();
 				    	break;
 			    	} else {
 			    		if (token.value == TOKinvariant) {
 			    			repeat = false;
 			    			break;
 			    		}
 			    		// fall to next case
 			    	}
 			    case TOKfinal:
 			    case TOKauto:
 			    case TOKscope:
 			    case TOKoverride:
 			    case TOKabstract:
 			    case TOKsynchronized:
 			    case TOKdeprecated:
 			    case TOKnothrow:
 			    case TOKpure:
 			    case TOKref:
 			    case TOKtls:
 			    case TOKgshared:
 			    	stc |= STC.fromTOK(token.value);
 			    	
 			    	modifier = newModifier();
 			    	modifiers.add(modifier);
 			    	nextToken();
 			    	break;
 			    default:
 			    	repeat = false;
 					break;
 			}
 		}
 		
 		if (apiLevel >= D2) {
 			Dsymbols a;
 			
 			/* Look for auto initializers:
 			 *	storage_class identifier = initializer;
 			 */
 			if (token.value == TOKidentifier &&
 			    peek(token).value == TOKassign)
 			{
 			    a = parseAutoDeclarations(stc, firstTokenStart, start, modifiers);
 			    decldefs.addAll(a);
 			    return null;
 			}
 
 			/* Look for return type inference for template functions.
 			 */
 			Token[] tk = { null };
 			if (token.value == TOKidentifier &&
 			    (tk[0] = peek(token)).value == TOKlparen &&
 			    skipParens(tk[0], tk) &&
 			    (peek(tk[0]).value == TOKlparen ||
 			     peek(tk[0]).value == TOKlcurly
 			    ))
 			{
 			    a = parseDeclarations(stc);
 			    decldefs.addAll(a);
 			    return null;
 			}
 			
 			boolean isColon = token.value == TOKcolon;
 			a = parseBlock(isSingle);
 			s = new StorageClassDeclaration(stc, a, modifier, isSingle[0], isColon);
 			modifiers.remove(modifier);
 			
 			setModifiers(s, modifiers);
 		} else {
 			VarDeclaration previous = null;
 	
 			/* Look for auto initializers:
 			 *	storage_class identifier = initializer;
 			 */
 			if (token.value == TOKidentifier &&
 			    peek(token).value == TOKassign)
 			{
 				boolean first = true;
 				while(true) {
 				    IdentifierExp ident = newIdentifierExp();
 				    nextToken();
 				    nextToken();
 				    Initializer init = parseInitializer();
 				    VarDeclaration v = newVarDeclaration(filename, lineNumber, null, ident, init);
 				    v.first = first;
 				    first = false;
 				    
 				    v.storage_class = stc;
 				    addModifiers(v, modifiers);
 				    
 				    attachLeadingComments(v);
 				    
 				    s = v;
 				    if (previous != null) {
 				    	previous.next = v;
 				    }
 				    previous = v;
 				    
 				    if (token.value == TOKsemicolon) {
 				    	v.setSourceRange(start, token.ptr + token.sourceLen - start);
 						nextToken();
 					} else if (token.value == TOKcomma) {
 						v.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 						nextToken();
 						start = token.ptr;
 						if (token.value == TOKidentifier
 								&& peek(token).value == TOKassign) {
 							decldefs.add(s);
 							continue;
 						} else {
 							parsingErrorInsertTokenAfter(prevToken, "identifier");
 						}
 					} else {
 						parsingErrorInsertTokenAfter(prevToken, TOKsemicolon
 								.toString());
 						v.setSourceRange(firstTokenStart, prevToken.ptr + prevToken.sourceLen - firstTokenStart);
 					}
 					break;
 				}
 			}
 			else
 			{  
 				boolean isColon = token.value == TOKcolon;
 				Dsymbols a = parseBlock(isSingle, true /* thinks it's D2 */);
 				
 				if (isSingle[0] && a.size() == 0) {
 					parsingErrorDeleteToken(firstTokenLinnum, firstTokenStart, firstTokenSourceLen, firstTokenString);
 					return null;
 				}
 				
 				s = new StorageClassDeclaration(stc, a, modifier, isSingle[0], isColon);
 				modifiers.remove(modifier);
 				setModifiers(s, modifiers);
 			}
 		}
 		
 		return s;
 	}
 	
 	/*********************************************
 	 * Give error on conflicting storage classes.
 	 */
 	private void composeStorageClass(int stc) {
 	    int u = stc;
 		u &= STCconst | STCimmutable | STCmanifest;
 		if ((u & (u - 1)) != 0) {
 			error(IProblem.ConflictingStorageClass, token);
 		}
 		u = stc;
 		u &= STCgshared | STCshared | STCtls;
 		if ((u & (u - 1)) != 0) {
 			error(IProblem.ConflictingStorageClass, token);
 		}
 	}
 
 	private Dsymbols parseAutoDeclarations(int storageClass, int firstTokenStart, int start, List<Modifier> modifiers) {
 		Dsymbols a = new Dsymbols(3);
 		
 		VarDeclaration previous = null;
 		boolean first = true;
 		
 		while (true) {
 			IdentifierExp ident = newIdentifierExp();
 			nextToken(); // skip over ident
 			nextToken(); // skip over '='
 			Initializer init = parseInitializer();
 			VarDeclaration v = new VarDeclaration(filename, lineNumber, null, ident, init);
 			v.first = first;
 		    first = false;
 			v.storage_class = storageClass;
 			addModifiers(v, modifiers);
 			
 			attachLeadingComments(v);
 			
 			if (previous != null) {
 		    	previous.next = v;
 		    }
 		    previous = v;
 			
 			a.add(v);
 			
 			if (token.value == TOKsemicolon) {
 				v.setSourceRange(start, token.ptr + token.sourceLen - start);
 				nextToken();
 			} else if (token.value == TOKcomma) {
 				nextToken();
 				if (token.value == TOKidentifier
 						&& peek(token).value == TOKassign) {
 					v.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 					start = peek(token).ptr;
 					continue;
 				} else {
 					parsingErrorInsertTokenAfter(prevToken, "Identifier");
 				}
 			} else {
 				parsingErrorInsertTokenAfter(prevToken, TOKsemicolon
 						.toString());
 				v.setSourceRange(firstTokenStart, prevToken.ptr + prevToken.sourceLen - firstTokenStart);
 			}
 			break;
 		}
 		return a;
 	}
 	
 	private final void addModifiers(ASTDmdNode node, List<Modifier> modifiers) {
 		if (modifiers == null || modifiers.size() == 0)
 			return;
 		
 		List<Modifier> mods = getModifiers(node);
 		if (mods == null) {
 			mods = new ArrayList<Modifier>(modifiers);
 			setModifiers(node, mods);
 		} else {
 			mods.addAll(modifiers);
 		}
 	}
 
 	private Dsymbols parseBlock() {
 		return parseBlock(null, false);
 	}
 	
 	private Dsymbols parseBlock(boolean[] isSingle) {
 		return parseBlock(isSingle, false);
 	}
 	
 	private Dsymbols parseBlock(boolean[] isSingle, boolean thinksItsD2) {
 		Dsymbols a = null;
 
 		switch (token.value) {
 		case TOKsemicolon:
 			parsingErrorInsertToComplete(prevToken, "Declaration", "Declaration");
 			nextToken();
 			break;
 			
 		case TOKeof:
 			parsingErrorInsertToComplete(prevToken, "Declaration", "Declaration");
 		    break;
 
 		case TOKlcurly:
 			nextToken();
 			a = parseDeclDefs(false);
 			if (token.value != TOKrcurly) { /* { */
 				parsingErrorInsertTokenAfter(prevToken, "}");
 			} else {
 				nextToken();
 			}
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
 			a = parseDeclDefs(true, thinksItsD2);
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
 
 		return newStaticAssert(filename, lineNumber, exp, msg);
 	}
 
 	/***********************************
 	 * Parse typeof(expression).
 	 * Current token is on the 'typeof'.
 	 */
 	private TypeQualified parseTypeof() {
 		TypeQualified t;
 		int lineNumber = this.lineNumber;
 		
 		int start = token.ptr;
 
 		nextToken();
 		check(TOKlparen);
 		if (token.value == TOKreturn) // typeof(return)
 		{
 			nextToken();
 			t = new TypeReturn(filename, lineNumber);
 		} else {
 			Expression exp = parseExpression(); // typeof(expression)
 			t = new TypeTypeof(filename, lineNumber, exp, encoder);
 		}
 		check(TOKrparen);
 		
 		t.start = start;
 		t.length = prevToken.ptr + prevToken.sourceLen - t.start;
 		
 		return t;
 	}
 	
 	private LINK parseLinkage() {
 		LINK link = LINKdefault;
 		nextToken();
 		
 		if(token.value != TOKlparen) {
 			throw new IllegalStateException();
 		}
 		
 		nextToken();
 		
 		expect(externArgsExpectations);
 		if (token.value == TOKidentifier) {
 			char[] id = token.sourceString;
 			int start = token.ptr;
 			int length = token.sourceLen;
 			int lineNumber = token.lineNumber;
 			nextToken();
 			
 			// We can compare by equality because Lexer reuses the 
 			// char[] instances for this special tokens
 			if (id == Id.Windows) {
 				link = LINKwindows;
 			} else if (id == Id.Pascal) {
 				link = LINKpascal;
 			} else if (id == Id.D) {
 				link = LINKd;
 			} else if (id == Id.C) {
 				link = LINKc;
 				if (token.value == TOKplusplus) {
 					link = LINKcpp;
 					nextToken();
 				}
 			} else if (id == Id.System) {
 				link = LINKsystem;
 			} else {
 				error(IProblem.InvalidLinkageIdentifier, lineNumber, start, length);
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
 		long level = 1;
 		char[] id = null;
 		
 		int idTokenStart = -1;
 		int idTokenLength = -1;
 
 		if (token.value == TOKlparen) {
 			nextToken();
 			if (token.value == TOKidentifier) {
 				id = token.sourceString;
 				idTokenStart = token.ptr;
 				idTokenLength = token.sourceLen;
 				
 				nextToken();			
 				c = newDebugCondition(module, filename, lineNumber, level, id);			
 				check(TOKrparen);
 			} else if (token.value == TOKint32v) {
 				id = token.sourceString;
 				level = token.intValue.longValue();
 				idTokenStart = token.ptr;
 				idTokenLength = token.sourceLen;
 				
 				nextToken();
 				c = newDebugCondition(module, filename, lineNumber, level, id);
 				check(TOKrparen);
 			} else {
 				parsingErrorInsertTokenAfter(prevToken, "Identifier or Integer");
 				
 				c = newDebugCondition(module, filename, lineNumber, level, id);
 				
 				// For improved syntax error recovery
 				if (token.value != TOKrparen) {
 					nextToken();
 				}
 				nextToken();
 			}
 		} else {
 			c = newDebugCondition(module, filename, lineNumber, level, id);
 		}
 		
 		// Don't bring the "c = ..." statement here: it needs to be
 		// created after the identifier for completion parser
 		
 		c.startPosition = idTokenStart;
 		c.length = idTokenLength;
 		return c;
 	}
 	
 	private VersionCondition parseVersionCondition() {
 		VersionCondition c;
 		long level = 1;
 		char[] id = null;
 
 		int idTokenStart = -1;
 		int idTokenLength = -1;
 
 		if (token.value == TOKlparen) {
 			nextToken();
 			if (token.value == TOKidentifier) {
 				id = token.sourceString;
 				idTokenStart = token.ptr;
 				idTokenLength = token.sourceLen;
 				
 				nextToken();			
 				c = newVersionCondition(module, filename, lineNumber, level, id);			
 				check(TOKrparen);
 			} else if (token.value == TOKint32v) {
 				id = token.sourceString;
 				level = token.intValue.longValue();
 				idTokenStart = token.ptr;
 				idTokenLength = token.sourceLen;
 				
 				nextToken();
 				c = newVersionCondition(module, filename, lineNumber, level, id);
 				check(TOKrparen);		
 			} else {
 				/* Allow:
 				 *    version (unittest)
 				 * even though unittest is a keyword
 				 */
 				if (apiLevel >= D2 &&
 						token.value == TOKunittest) {
 					id = token.value.charArrayValue;
 					idTokenStart = token.ptr;
 					idTokenLength = token.sourceLen;
 				} else {
 					parsingErrorInsertTokenAfter(prevToken, "Identifier or Integer");
 				}
 				
 				c = newVersionCondition(module, filename, lineNumber, level, id);
 					
 				// For improved syntax error recovery
 				if (token.value != TOKrparen) {
 					nextToken();
 				}
 				nextToken();
 			}			
 		} else {
 			c = newVersionCondition(module, filename, lineNumber, level, id);
 			
 			parsingErrorInsertToComplete(prevToken, "(condition)", "VersionDeclaration");
 		}
 		
 		// Don't bring the "c = ..." statement here: it needs to be
 		// created after the identifier for completion parser
 		
 		c.startPosition = idTokenStart;
 		c.length = idTokenLength;
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
 		return new StaticIfCondition(filename, lineNumber, exp);
 	}
 	
 	private IftypeCondition parseIftypeCondition() {
 		Type targ;
 		IdentifierExp[] ident = new IdentifierExp[1];
 		Type tspec = null;
 		TOK tok = TOKreserved;
 
 		int firstTokenStart = token.ptr;
 		int firstTokenLength = token.sourceLen;
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
 			return new IftypeCondition(filename, lineNumber, null, null, null, null);
 		}
 
 		error(
 				IProblem.IftypeDeprecated, firstTokenLine,
 				firstTokenStart, firstTokenLength);
 
 		return new IftypeCondition(filename, lineNumber, targ, ident[0], tok, tspec);
 	}
 	
 	private Dsymbol parseCtor() {
 		int start = token.ptr;
 		
 	    nextToken();
 	    
 	    if (apiLevel >= D2) {
 	    	if (token.value == TOKlparen && peek(token).value == TOKthis) {
 	    		// this(this) { ... }
 	        	nextToken();
 	        	nextToken();
 	        	check(TOKrparen);
 	        	PostBlitDeclaration f = newPostBlitDeclaration(filename, lineNumber);
 	        	f.thisStart = start;
 	        	parseContracts(f);
 	        	return f;
 	    	}
 	    	
 	        /* Look ahead to see if:
 	         *   this(...)(...)
 	         * which is a constructor template
 	         */
 	        TemplateParameters tpl = null;
 			if (token.value == TOKlparen
 					&& peekPastParen(token).value == TOKlparen) {
 				tpl = parseTemplateParameterList();
 
 				int[] varargs = { 0 };
 				Arguments arguments = parseParameters(varargs);
 
 				Expression constraint = null;
 				if (tpl != null)
 					constraint = parseConstraint();
 
 				CtorDeclaration f = new CtorDeclaration(filename, lineNumber, arguments, varargs[0]);
 				parseContracts(f);
 				f.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 
 				// Wrap a template around it
 				Dsymbols decldefs = new Dsymbols(1);
 				decldefs.add(f);
 				f.templated = true;
 				
 				TemplateDeclaration tempdecl = new TemplateDeclaration(filename, lineNumber, f.ident, tpl, constraint, decldefs);
 				tempdecl.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 				tempdecl.wrapper = true;
 				return tempdecl;
 			}
 	    }
 	    
 	    /*
 		 * Just a regular constructor
 		 */
 	    int[] varargs = { 0 };	    
 	    Arguments arguments = parseParameters(varargs);
 	    CtorDeclaration f = newCtorDeclaration(filename, lineNumber, arguments, varargs[0]);
 	    f.thisStart = start;
 	    parseContracts(f);
 	    f.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 	    return f;
 	}	
 
 	private DtorDeclaration parseDtor() {
 		int start = token.ptr;
 		nextToken();
 		
 		int thisStart = token.ptr;
 		
 		expect(thisExpectations);
 		check(TOKthis);
 	    check(TOKlparen);
 	    check(TOKrparen);
 		
 		DtorDeclaration f = newDtorDeclaration(filename, lineNumber);
 		f.thisStart = thisStart;
 	    parseContracts(f);
 	    f.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 	    return f;
 	}
 
 	private StaticCtorDeclaration parseStaticCtor() {
 		int start = token.ptr;
 		
 		nextToken();
 	    check(TOKlparen);
 	    check(TOKrparen);
 
 	    StaticCtorDeclaration f = newStaticCtorDeclaration(filename, lineNumber);
 	    f.thisStart = start;
 	    
 		f.setSourceRange(start, 0);
 	    parseContracts(f);
 	    return f;
 	}
 
 	private StaticDtorDeclaration parseStaticDtor() {
 		int start = token.ptr;
 		nextToken();
 		
 		int thisStart = token.ptr;
 		
 		expect(thisExpectations);
 		check(TOKthis);
 	    check(TOKlparen);
 	    check(TOKrparen);
 		
 		StaticDtorDeclaration f = newStaticDtorDeclaration(filename, lineNumber);
 		f.thisStart = thisStart;
 		f.setSourceRange(start, 0);
 	    parseContracts(f);
 	    return f;
 	}
 
 	private InvariantDeclaration parseInvariant() {
 		int start = token.ptr;
 	    nextToken();
 	    
 	    if (token.value == TOKlparen)	// optional ()
 	    {
 			nextToken();
 			check(TOKrparen);
 	    }
 
 	    InvariantDeclaration invariant = newInvariantDeclaration(filename, lineNumber);
 	    invariant.invariantStart = start;
 	    invariant.setFbody(dietParseStatement(invariant));
 	    invariant.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 	    return invariant;
 	}
 
 	private UnitTestDeclaration parseUnitTest() {
 		int start = token.ptr;
 		nextToken();
 		
 		UnitTestDeclaration unitTest = newUnitTestDeclaration(filename, lineNumber);
 		unitTest.setFbody(dietParseStatement(unitTest));
 	    unitTest.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 	    return unitTest;
 	}
 
 	private NewDeclaration parseNew() {
 		int start = token.ptr;
 		
 		nextToken();
 		int[] varargs = new int[1];
 		Arguments arguments = parseParameters(varargs);
 		
 		NewDeclaration f = newNewDeclaration(filename, lineNumber, arguments, varargs[0]);
 		f.newStart = start;
 	    parseContracts(f);
 	    f.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 	    return f;
 	}
 
 	private FuncDeclaration parseDelete() {
 		int start = token.ptr;
 		int startLine = token.lineNumber;
 		
 		IdentifierExp name = newIdentifierExp();
 		
 		nextToken();
 		int[] varargs = new int[1];
 		Arguments arguments = parseParameters(varargs);
 		
 		if (varargs[0] != 0) {
 	    	error(
 	    			IProblem.VariadicNotAllowedInDelete, 
 	    			startLine, name);
 	    }
 		
 		DeleteDeclaration f = newDeleteDeclaration(filename, lineNumber, arguments);
 		f.deleteStart = start;
 	    parseContracts(f);
 	    f.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 	    return f;
 	}
 
 	@SuppressWarnings("unchecked")
 	private Arguments parseParameters(int[] pvarargs) {
 		if (apiLevel < D2) {
 			return parseParametersD1(pvarargs);
 		} else {
 			return parseParametersD2(pvarargs);
 		}
 	}
 	
 	private Arguments parseParametersD1(int[] pvarargs) {
 		Arguments arguments = new Arguments(3);
 		int varargs = 0;
 		boolean hasdefault = false;
 
 		check(TOKlparen);
 		while (true) {
 			Type tb;
 			IdentifierExp ai;
 			Type at;
 			Argument a;
 			int storageClass;
 			Expression ae;
 			List<Modifier> modifiers = new ArrayList<Modifier>(1);
 			
 			int firstTokenStart = token.ptr;
 			
 			ai = null;
 			storageClass = STCin;
 			
 			if (token.value == TOKrparen) {
 				break;
 			} else if (token.value == TOKdotdotdot) {
 				varargs = 1;
 				nextToken();
 				break;
 			} else {
 				int inoutTokenStart = token.ptr;
 				int inoutTokenLength = token.sourceLen;
 				int inoutTokenLine = token.lineNumber;
 				
 				expect(parameters1Expectations);
 				switch(token.value) {
 					case TOKin:
 						storageClass = STCin;
 						modifiers.add(newModifier());
 						nextToken();
 						break;
 					case TOKout:
 						storageClass = STCout;
 						modifiers.add(newModifier());
 						nextToken();
 						break;
 					case TOKinout:
 						storageClass = STCref;
 						modifiers.add(newModifier());
 						nextToken();
 						break;
 					case TOKref:
 						storageClass = STCref;
 						modifiers.add(newModifier());
 						nextToken();
 						break;
 					case TOKlazy:
 						storageClass = STClazy;
 						modifiers.add(newModifier());
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
 					if ((storageClass & (STCout | STCref)) != 0) {
 						error(IProblem.VariadicArgumentCannotBeOutOrRef, inoutTokenLine, inoutTokenStart, inoutTokenLength);
 					}
 					varargs = 2;
 					
 					a = newArgument(storageClass, at, ai, ae);
 					setModifiers(a, modifiers);
 					a.setSourceRange(firstTokenStart, prevToken.ptr + prevToken.sourceLen - firstTokenStart);
 					arguments.add(a);
 					nextToken();
 					break;
 				}
 				
 				if (at != null || ai != null || ae != null) {
 					a = newArgument(storageClass, at, ai, ae);
 					setModifiers(a, modifiers);
 					a.setSourceRange(firstTokenStart, prevToken.ptr + prevToken.sourceLen - firstTokenStart);
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
 	
 	private Arguments parseParametersD2(int[] pvarargs) {
 		Arguments arguments = new Arguments(3);
 		int varargs = 0;
 		boolean hasdefault = false;
 
 		check(TOKlparen);
 	loopWhile:
 		while (true) {
 			IdentifierExp ai;
 			Type at;
 			Argument a;
 			int storageClass;
 			int stc;
 			Expression ae;
 			
 			int firstTokenStart = token.ptr;
 			
 			ai = null;
 			storageClass = 0;
 			stc = 0;
 			
 			List<Modifier> modifiers = new ArrayList<Modifier>(2);
 			
 		loopFor:
 			for(; true; nextToken()) {
 				int inoutTokenStart = token.ptr;
 				int inoutTokenLength = token.sourceLen;
 				int inoutTokenLine = token.lineNumber;
 				
 				expect(parameters2Expectations);
 				switch(token.value) {
 				case TOKrparen:
 					break loopWhile;
 				case TOKdotdotdot:
 					varargs = 1;
 					nextToken();
 					break loopWhile;
 				case TOKconst:
 				    if (peek(token).value == TOKlparen) {
 						// goto Ldefault;
 				    	break;
 				    } else {
 					    stc = STCconst;
 					    modifiers.add(newModifier());
 					    // goto L2;
 					    storageClass = parseParametersD2_L2(storageClass, stc);
 					    continue loopFor;
 				    }
 				case TOKinvariant:
 				case TOKimmutable:
 				    if (peek(token).value == TOKlparen) {
 				    	// goto Ldefault;
 				    	break;
 				    } else {
 					    stc = STCinvariant;
 					    modifiers.add(newModifier());
 					    // goto L2;
 					    storageClass = parseParametersD2_L2(storageClass, stc);
 					    continue loopFor;
 				    }
 				case TOKshared:
 				    if (peek(token).value == TOKlparen) {
 				    	// goto Ldefault;
 				    	break;
 				    } else {
 					    stc = STCshared;
 					    modifiers.add(newModifier());
 					    // goto L2;
 					    storageClass = parseParametersD2_L2(storageClass, stc);
 					    continue loopFor;
 				    }
 				case TOKin:
 					stc = STCin;
 					modifiers.add(newModifier());
 					// goto L2;
 					storageClass = parseParametersD2_L2(storageClass, stc);
 					continue loopFor;
 				case TOKout:
 					stc = STCout;
 					modifiers.add(newModifier());
 					// goto L2;
 					storageClass = parseParametersD2_L2(storageClass, stc);
 					continue loopFor;
 				case TOKinout:
 					stc = STCref;
 					modifiers.add(newModifier());
 					// goto L2;
 					storageClass = parseParametersD2_L2(storageClass, stc);
 					continue loopFor;
 				case TOKref:
 					stc = STCref;
 					modifiers.add(newModifier());
 					// goto L2;
 					storageClass = parseParametersD2_L2(storageClass, stc);
 					continue loopFor;
 				case TOKlazy:
 					stc = STClazy;
 					modifiers.add(newModifier());
 					// goto L2;
 					storageClass = parseParametersD2_L2(storageClass, stc);
 					continue loopFor;
 				case TOKscope:
 					stc = STCscope;
 					modifiers.add(newModifier());
 					// goto L2;
 					storageClass = parseParametersD2_L2(storageClass, stc);
 					continue loopFor;
 				case TOKfinal:
 					stc = STCfinal;
 					modifiers.add(newModifier());
 					// goto L2;
 					storageClass = parseParametersD2_L2(storageClass, stc);
 					continue loopFor;
 				case TOKstatic:
 					stc = STCstatic;
 					modifiers.add(newModifier());
 					// goto L2;
 					storageClass = parseParametersD2_L2(storageClass, stc);
 					continue loopFor;
 					
 				default:
 					// Ldefault:
 					break;
 				}
 				
 				stc = storageClass
 						& (STCin | STCout | STCref | STClazy);
 				if ((stc & (stc - 1)) != 0) { // if stc is not a power of 2
 					error(IProblem.IncompatibleParameterStorageClass, token);
 				}
 				if ((storageClass & (STCconst | STCout)) == (STCconst | STCout)) {
 					error(IProblem.OutCannotBeConst, token);
 				}
 				if ((storageClass & (STCimmutable | STCout)) == (STCimmutable | STCout)) {
 					error(IProblem.OutCannotBeInvariant, token);
 				}
 				if ((storageClass & STCscope) != 0
 						&& (storageClass & (STCref | STCout)) != 0) {
 					error(IProblem.ScopeCannotBeRefOrOut, token);
 				}
 				
 				IdentifierExp[] pointer2_ai = { ai };
 				at = parseType(pointer2_ai);
 				ai = pointer2_ai[0];
 				
 				ae = null;
 				if (token.value == TOKassign) // = defaultArg
 				{
 					nextToken();
 					ae = parseDefaultInitExp();
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
 					if ((storageClass & (STCout | STCref)) != 0) {
 						error(IProblem.VariadicArgumentCannotBeOutOrRef, inoutTokenLine, inoutTokenStart, inoutTokenLength);
 					}
 					varargs = 2;
 					
 					a = newArgument(storageClass, at, ai, ae);
 					setModifiers(a, modifiers);
 					a.setSourceRange(firstTokenStart, prevToken.ptr + prevToken.sourceLen - firstTokenStart);
 					arguments.add(a);
 					nextToken();
 					break;
 				}
 				
 				if (at != null || ai != null || ae != null) {
 					a = newArgument(storageClass, at, ai, ae);
 					setModifiers(a, modifiers);
 					a.setSourceRange(firstTokenStart, prevToken.ptr + prevToken.sourceLen - firstTokenStart);
 					arguments.add(a);
 				}
 				if (token.value == TOKcomma) {
 					nextToken();
 					// goto L1;
 					continue loopWhile;
 				}
 				break;
 			}
 			break;
 			
 			// L1: ;
 		}
 		check(TOKrparen);
 		pvarargs[0] = varargs;
 		return arguments;
 	}
 
 	private int parseParametersD2_L2(int storageClass, int stc) {
 		if ((storageClass & stc) != 0
 				|| ((storageClass & STCin) != 0 && (stc & (STCconst | STCscope)) != 0)
 				|| ((stc & STCin) != 0 && (storageClass & (STCconst | STCscope)) != 0)) {			
 			error(IProblem.RedundantStorageClass, token);
 		}
 		storageClass |= stc;
 		composeStorageClass(storageClass);
 		return storageClass;
 	}
 	
 	private EnumDeclaration parseEnum() {
 		int enumTokenStart = token.ptr;
 		int enumTokenLength = token.sourceLen;
 		int enumTokenLineNumber = token.lineNumber;
 
 		IdentifierExp id;
 		Type memtype;
 		
 		nextToken();
 		
 		int[] identStart = { token.ptr };
 		
 		if (token.value == TOKidentifier) {
 			id = newIdentifierExp();
 			nextToken();
 		} else {
 			id = null;
 		}
 
 		if (token.value == TOKcolon) {
 			nextToken();
 			memtype = parseBasicType();
 			if (apiLevel >= D2) {
 				memtype = parseDeclarator(memtype, null, null, identStart);
 			}
 		} else {
 			memtype = null;
 		}
 		
 		EnumDeclaration e = newEnumDeclaration(filename, lineNumber, id, memtype);
 		
 		if (token.value == TOKsemicolon && id != null) {
 			e.setSourceRange(enumTokenStart, token.ptr + token.sourceLen - enumTokenStart);
 			nextToken();			  
 		} else if (token.value == TOKlcurly) {
 			e.members = new Dsymbols(6);
 			nextToken();
 			while (token.value != TOKrcurly) {
 				if (token.value == TOKeof) {
 					error(IProblem.EnumDeclarationIsInvalid, enumTokenLineNumber, enumTokenStart, enumTokenLength);
 					break;
 				}
 				
 				List<Comment> lastComments = null;
 				EnumMember em = null;
 				if (apiLevel >= D2) {
 				    /* Can take the following forms:
 				     *	1. ident
 				     *	2. ident = value
 				     *	3. type ident = value
 				     */
 				    Type type = null;
 					IdentifierExp[] ident = { null };
 					Token tp = peek(token);
 					if (token.value == TOKidentifier
 							&& (tp.value == TOKassign || tp.value == TOKcomma || tp.value == TOKrcurly)) {
 						IdentifierExp id2 = newIdentifierExp();
 						id2.start = token.ptr;
 						id2.length = token.len;
 
 						ident = new IdentifierExp[] { id2 };
 						type = null;
 						nextToken();
 					} else {
 						type = parseType(ident, null);
 						if (id != null || memtype != null) {
 							error(IProblem.TypeOnlyAllowedIfAnonymousEnumAndNoEnumType, memtype == null ? id.getLineNumber() : memtype.getLineNumber(), memtype == null ? id.start : memtype.start, memtype == null ? id.length : memtype.length);
 						}
 					}
 					
 					Expression value;
 					if (token.value == TOKassign) {
 						nextToken();
 						value = parseAssignExp();
 					} else {
 						value = null;
 						if (type != null) {
 							error(IProblem.IfTypeThereMustBeAnInitializer, type.getLineNumber(), type.start, type.length);
 						}
 					}
 					
 					lastComments = getLastComments();
 					
 					if (ident[0] == null) {
 						nextToken();
 					} else {
 						em = newEnumMember(filename, lineNumber, ident[0], value, type);
 					}
 				} else {
 					if (token.value == TOKidentifier) {
 						Expression value;
 						IdentifierExp ident = newIdentifierExp();
 						value = null;
 						nextToken();
 						if (token.value == TOKassign) {
 							nextToken();
 							value = parseAssignExp();
 						}
 						
 //						if (token.value != TOK.TOKrcurly) {
 							lastComments = getLastComments();
 //						} else {
 //							lastComments = null;
 //						}
 						
 						em = newEnumMember(filename, lineNumber, ident, value);
 					} else {
 						parsingErrorInsertToComplete(prevToken, "EnumMember", "EnumDeclaration");
 						nextToken();
 					}
 				}
 				
 				if (em != null) {
 					e.addMember(em);
 					
 					this.setPreComments(em, lastComments);
 					
 					if (token.value == TOKrcurly) {
 						;
 					} else {
 						check(TOKcomma);
 						if (token.value == TOK.TOKsemicolon) {
 							nextToken();
 						}
 					}
 					
 					attachLeadingComments(em);
 				}
 			}
 			e.setSourceRange(enumTokenStart, token.ptr + token.sourceLen - enumTokenStart);
 			
 			// Discard any previous comments
 			if (comments != null) {
 				lastCommentRead = comments.size();
 			}
 			
 			nextToken();
 		} else {
 			error(IProblem.EnumDeclarationIsInvalid, enumTokenLineNumber, enumTokenStart, enumTokenLength);
 			return null;
 		}
 		
 		return e;
 	}
 
 	/********************************
 	 * Parse struct, union, interface, class.
 	 */
 	@SuppressWarnings("unchecked")
 	private Dsymbol parseAggregate() {
 		AggregateDeclaration a = null;
 		IdentifierExp id;
 		TemplateParameters tpl = null;
 		Expression constraint = null;
 		BaseClasses baseClasses = null;
 		int anon = 0;
 		
 		int firstTokenStart = token.ptr;
 		int firstTokenLength = token.sourceLen;
 		int firstTokenLineNumber = token.lineNumber;
 		char[] firstTokenString = token.getRawTokenSource();
 		TOK firstTokenValue = token.value;
 		
 		nextToken();
 		if (token.value != TOKidentifier) {
 			// Change from DMD
 			id = null;
 		} else {
 			id = newIdentifierExp();
 			nextToken();
 			if (token.value == TOKlparen) {				
 				// Gather template parameter list
 				tpl = parseTemplateParameterList();
 				if (apiLevel >= D2) {
 					constraint = parseConstraint();
 				}
 			}
 		}
 
 		// Don't need to expect(...), it was called previously
 		switch (firstTokenValue) {
 		case TOKclass:
 		case TOKinterface:
 			if (id == null) {
 				parsingErrorInsertTokenAfter(firstTokenLineNumber, firstTokenStart, firstTokenLength, firstTokenString, "Identifier");
 			}
 
 			// Collect base class(es)
 			if (token.value == TOKcolon) {
 				nextToken();				
 				baseClasses = parseBaseClasses();
 				
 				if (token.value != TOKlcurly) {
 					parsingErrorInsertToComplete(prevToken, "AggregateBody", "AggregateDeclaration");
 				}
 			}
 			
 			if (firstTokenValue == TOKclass) {
 				a = newClassDeclaration(filename, lineNumber, id, baseClasses);
 			} else {
 				a = newInterfaceDeclaration(filename, lineNumber, id, baseClasses);
 			}
 			break;			
 		case TOKstruct:
 			if (id != null) {
 				a = newStructDeclaration(filename, lineNumber, id);
 			} else {
 				anon = 2;
 			}
 			break;
 		case TOKunion:
 			if (id != null) {
 				a = newUnionDeclaration(filename, lineNumber, id);
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
 			Dsymbols decl = parseDeclDefs(false);
 			
 			if (token.value != TOKrcurly) {
 				parsingErrorInsertTokenAfter(prevToken, "}");
 			}
 			if (anon != 0) {
 				nextToken();
 			    return newAnonDeclaration(filename, lineNumber, anon == 1, decl);
 			}
 			a.members = decl;
 			a.sourceMembers = new Dsymbols(decl);
 			
 			nextToken();
 		} else {
 			if (id == null) {
 				// A single "class" makes no declaration
 				if (firstTokenValue == TOKstruct || firstTokenValue == TOKunion) {
 					// Signal the creation of the struct or union, anyway
 					if (firstTokenValue == TOKstruct) {
 						newStructDeclaration(filename, lineNumber, null);
 					} else {
 						newUnionDeclaration(filename, lineNumber, null);
 					}
 					
 					String word = toWord(firstTokenValue.toString());
 					parsingErrorInsertToComplete(firstTokenLineNumber, firstTokenStart, firstTokenLength,  word + "Body", word + "Declaration");
 				}
 				a = null;
 			} else {
 				// We've got at least "class Identifier", make a declaration out of it
 				String word = toWord(firstTokenValue.toString());
 				parsingErrorInsertToComplete(prevToken,  word + "Body", word + "Declaration");
 				
 				if (a == null) {
 					switch(firstTokenValue) {
 					case TOKclass:
 						a = newClassDeclaration(filename, lineNumber, id, baseClasses);
 						break;
 					case TOKinterface:
 						a = newInterfaceDeclaration(filename, lineNumber, id, baseClasses);
 						break;
 					case TOKstruct:
 						a = newStructDeclaration(filename, lineNumber, id);
 						break;
 					case TOKunion:
 						a = newUnionDeclaration(filename, lineNumber, id);
 						break;
 					}
 				}
 			}		
 		}
 		
 		if (tpl != null)
 		{	
 			Dsymbols decldefs;
 			TemplateDeclaration tempdecl;
 	
 			// Wrap a template around the aggregate declaration
 			decldefs = new Dsymbols(1);
 			decldefs.add(a);
 			tempdecl = newTemplateDeclaration(filename, lineNumber, id, tpl, constraint, decldefs);
 			tempdecl.setSourceRange(a.start, a.length);
 			tempdecl.wrapper = true;
 			a.templated = true;
 			return tempdecl;
 	    }
 
 		return a;
 	}	
 
 	private BaseClasses parseBaseClasses() {
 		PROT protection = PROT.PROTpublic;
 		BaseClasses baseclasses = new BaseClasses(2);
 		Modifier modifier = null;
 		
 		int start = token.ptr;
 
 		// These modifiers currently have no effect, so we don't expect them
 		// expect(TOKprivate, TOKpackage, TOKprotected, TOKpublic);
 		for (; true; nextToken()) {			
 			switch (token.value) {
 			case TOKidentifier:
 				break;
 			case TOKprivate:
 			case TOKpackage:
 			case TOKprotected:
 			case TOKpublic:
 				protection = PROT.fromTOK(token.value);
 				
 				modifier = newModifier();
 				continue;
 			default:
 				parsingErrorInsertTokenAfter(prevToken, "Type");
 				return null;
 			}
 			BaseClass b = new BaseClass(parseBasicType(), modifier, protection);
 			baseclasses.add(b);
 			
 			b.start = start;
 			b.length = prevToken.ptr + prevToken.sourceLen - b.start;
 			
 			if (token.value != TOKcomma) {
 				break;
 			} else {
 				start = peek(token).ptr;
 			}
 		}
 		return baseclasses;
 	}
 	
 	private Expression parseConstraint() {
 		Expression e = null;
 
 		if (token.value == TOKif) {
 			nextToken(); // skip over 'if'
 			check(TOKlparen);
 			e = parseExpression();
 			check(TOKrparen);
 		}
 		return e;
 	}
 	
 	private TemplateDeclaration parseTemplateDeclaration() {
 		IdentifierExp id;
 		TemplateParameters tpl;
 		Dsymbols decldefs;
 		Expression constraint = null;
 
 		int start = token.ptr;
 		nextToken();
 		if (token.value != TOKidentifier) {
 			parsingErrorInsertTokenAfter(prevToken, "Identifier");
 			return null;
 			// goto Lerr;
 		}
 		id = newIdentifierExp();
 		nextToken();
 		tpl = parseTemplateParameterList();
 		if (tpl == null) {
 			// goto Lerr;
 			return null;
 		}
 		
 		if (apiLevel >= D2) {
 			constraint = parseConstraint();
 		}
 
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
 
 		TemplateDeclaration tempdecl = newTemplateDeclaration(filename, lineNumber, id, tpl, constraint,  decldefs);
 		tempdecl.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 		return tempdecl;
 
 		// Lerr:
 		// return NULL;
 	}
 	
 	private TemplateParameters parseTemplateParameterList() {
 		return parseTemplateParameterList(0);
 	}
 	
 	@SuppressWarnings("unchecked")
 	private TemplateParameters parseTemplateParameterList(int flag) {
 		// Change from DMD: empty template parameter list is returned in case
 		// of a syntax error
 		
 		TemplateParameters tpl = new TemplateParameters(3);
 
 		if ((apiLevel < D2 && token.value != TOKlparen) || (apiLevel >= D2 && 0 == flag && token.value != TOKlparen)) {
 			parsingErrorInsertToComplete(prevToken, "Parenthesized TemplateParameterList", "TemplateDeclaration");
 			
 			// goto Lerr;
 			return tpl;
 		}
 		
 		nextToken();
 		
 		expect(aliasExpectations);
 
 		// Get array of TemplateParameters
 		if ((apiLevel < D2 && token.value != TOKrparen) || (apiLevel >= D2 && (flag != 0 || token.value != TOKrparen))) {
 			
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
 					
 					Type spectype = null;
 					if (apiLevel >= D2) {						
 						if (isDeclaration(token, 2, TOKreserved, null))
 						{
 							IdentifierExp[] p_tp_ident = { tp_ident };
 						    spectype = parseType(p_tp_ident);
 						    tp_ident = p_tp_ident[0];
 						}
 						else
 						{
 							if (token.value != TOKidentifier) {
 								parsingErrorInsertTokenAfter(prevToken, "Identifier");
 								// goto Lerr;
 								return tpl;
 							}
 							tp_ident = newIdentifierExp();
 							nextToken();
 						}
 					} else {
 						if (token.value != TOKidentifier) {
 							parsingErrorInsertTokenAfter(prevToken, "Identifier");
 							// goto Lerr;
 							return tpl;
 						}
 						tp_ident = newIdentifierExp();
 						nextToken();
 					}
 					
 					ASTDmdNode spec = null;
 					if (token.value == TOKcolon) // : Type
 					{
 						nextToken();
 						if (apiLevel < D2) {
 							tp_spectype = parseBasicType();
 							tp_spectype = parseDeclarator(tp_spectype, null);	
 						} else {
 							if (isDeclaration(token, 0, TOKreserved, null)) {
 								spec = parseType();
 							} else {
 								spec = parseCondExp();
 							}
 						}
 					}
 					
 					ASTDmdNode def = null;
 					if (token.value == TOKassign) // = Type
 					{
 						nextToken();
 						if (apiLevel < D2) {
 							tp_defaulttype = parseBasicType();
 							tp_defaulttype = parseDeclarator(tp_defaulttype, null);
 						} else {
 							if (isDeclaration(token, 0, TOKreserved, null)) {
 								def = parseType();
 							} else {
 								def = parseCondExp();
 							}
 						}
 					}
 					
 					if (apiLevel < D2) {
 						tp = new TemplateAliasParameter(filename, lineNumber, tp_ident, null, tp_spectype, tp_defaulttype);
 					} else {
 						tp = new TemplateAliasParameter(filename, lineNumber, tp_ident, spectype, spec, def);
 					}
 				} else if (t.value == TOKcolon || t.value == TOKassign
 						|| t.value == TOKcomma || t.value == TOKrparen) { // TypeParameter
 					if (token.value != TOKidentifier) {
 						parsingErrorInsertTokenAfter(prevToken, "Identifier");
 						// goto Lerr;
 						return tpl;
 					}
 					tp_ident = newIdentifierExp();
 					nextToken();
 					if (token.value == TOKcolon) // : Type
 					{
 						nextToken();
 						if (apiLevel < D2) {
 							tp_spectype = parseBasicType();
 							tp_spectype = parseDeclarator(tp_spectype, null);
 						} else {
 							tp_spectype = parseType();
 						}
 					}
 					if (token.value == TOKassign) // = Type
 					{
 						nextToken();
 						if (apiLevel < D2) {
 							tp_defaulttype = parseBasicType();
 							tp_defaulttype = parseDeclarator(tp_defaulttype, null);
 						} else {
 							tp_defaulttype = parseType();
 						}
 					}
 					
 					tp = new TemplateTypeParameter(filename, lineNumber, tp_ident, tp_spectype, tp_defaulttype);
 				} else if (token.value == TOKidentifier
 						&& t.value == TOKdotdotdot) { // ident...
 					if (isvariadic) {
 						error(
 								IProblem.VariadicTemplateParameterMustBeTheLastOne,
 								t.lineNumber, token.ptr, t.ptr + t.sourceLen
 										- token.ptr);
 					}
 					isvariadic = true;
 					tp_ident = newIdentifierExp();
 					nextToken();
 					nextToken();
 
 					tp = new TemplateTupleParameter(filename, lineNumber, tp_ident);
 				} else if (apiLevel >= D2 && token.value == TOKthis) { // ThisParameter
 					nextToken();
 					if (token.value != TOKidentifier) {
 						parsingErrorInsertToComplete(token, "Identifier", "TemplateParameter");
 						// goto Lerr;
 						return tpl;
 					}
 					tp_ident = newIdentifierExp();
 					nextToken();
 					if (token.value == TOKcolon) // : Type
 					{
 						nextToken();
 						tp_spectype = parseType();
 					}
 					if (token.value == TOKassign) // = Type
 					{
 						nextToken();
 						tp_defaulttype = parseType();
 					}
 					tp = new TemplateThisParameter(filename, lineNumber, tp_ident, tp_spectype,
 							tp_defaulttype);
 				} else {
 
 					// ValueParameter
 					if (apiLevel < D2) {
 						tp_valtype = parseBasicType();
 						IdentifierExp[] pointer2_tp_ident = new IdentifierExp[] { tp_ident };
 						tp_valtype = parseDeclarator(tp_valtype,
 								pointer2_tp_ident);
 						tp_ident = pointer2_tp_ident[0];
 					} else {
 						IdentifierExp[] pointer2_tp_ident = new IdentifierExp[] { tp_ident };
 						tp_valtype = parseType(pointer2_tp_ident);
 						tp_ident = pointer2_tp_ident[0];
 					}
 
 					if (tp_ident == null) {
 						error(IProblem.NoIdentifierForTemplateValueParameter,
 								t.lineNumber, t.ptr, t.sourceLen);
 						// TODO update to DMD 1.028 behaviour? What's the point?
 						// goto Lerr;
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
 						if (apiLevel >= D2) {
 							tp_defaultvalue = parseDefaultInitExp();
 						} else {
 							tp_defaultvalue = parseCondExp();
 						}
 					}
 
 					tp = new TemplateValueParameter(filename, lineNumber, tp_ident,
 							tp_valtype, tp_specvalue, tp_defaultvalue, encoder);
 				}
 				tp.setSourceRange(firstTokenStart, prevToken.ptr
 						+ prevToken.sourceLen - firstTokenStart);
 
 				tpl.add(tp);
 				if (token.value != TOKcomma) {
 					break;
 				}
 				nextToken();
 			}
 		}
 		check(TOKrparen);
 		return tpl;
 
 	// Lerr:
 	// return NULL;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public TemplateMixin parseMixin() {
 		TemplateMixin tm;
 		IdentifierExp id = null;
 		Type tqual;
 		Objects tiargs;
 		Identifiers idents;
 		
 		int start = token.ptr;
 
 		nextToken();
 		
 		expect(typeofExpectations);
 		
 		int typeStart = token.ptr;
 		tqual = null;
 		if (token.value == TOKdot) {
 			id = new IdentifierExp(filename, lineNumber, Id.empty); // Id.empty
 		} else {
 			if (token.value == TOKtypeof) {
 				if (apiLevel >= D2) {
 					tqual = parseTypeof();
 				} else {
 					Expression exp;
 	
 					nextToken();
 					check(TOKlparen);
 					exp = parseExpression();
 					check(TOKrparen);
 					tqual = new TypeTypeof(filename, lineNumber, exp, encoder);
 					tqual.setSourceRange(typeStart, prevToken.ptr + prevToken.sourceLen - typeStart);
 					
 					check(TOKdot);
 				}
 			}
 			if (token.value != TOKidentifier) {
 				parsingErrorDeleteToken(prevToken);
 				
 				// goto Lerr;
 				return newTemplateMixin(filename, lineNumber, null, null, null, null);
 			}
 			id = newIdentifierExp();
 			nextToken();
 		}
 		
 		idents = new Identifiers(3);
 		while (true) {
 			int thisStart = prevToken.ptr;
 			
 			tiargs = null;
 			if (token.value == TOKnot) {
 				nextToken();
 				
 				if (apiLevel >= D2) {
 				    if (token.value == TOKlparen) {
 						tiargs = parseTemplateArgumentList();
 				    } else {
 						tiargs = parseTemplateArgument();
 				    }
 				} else {
 					tiargs = parseTemplateArgumentList();	
 				}
 			}
 			
 			if (token.value != TOKdot) {
 				break;
 			}
 			
 			if (tiargs != null) {
 				TemplateInstance tempinst = newTemplateInstance(filename, lineNumber, id, encoder);
 			    tempinst.tiargs(tiargs);
 			    tempinst.start = thisStart;
 			    tempinst.length = prevToken.ptr + prevToken.sourceLen - thisStart;
 			    id = new TemplateInstanceWrapper(filename, lineNumber, tempinst); // "(PIdentifier *)tempinst;" cant work in Java 
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
 		
 		int typeLength = prevToken.ptr + prevToken.sourceLen - typeStart;
 
 		if (token.value == TOKidentifier) {
 			id = newIdentifierExp();
 			nextToken();
 		} else {
 			id = null;
 		}
 		
 		tm = newTemplateMixin(filename, lineNumber, id, tqual, idents, tiargs);
 		tm.setTypeSourceRange(typeStart, typeLength);
 		tm.setSourceRange(start, token.ptr + token.sourceLen - start);
 
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
 	private Objects parseTemplateArgumentList() {
 	    if (token.value != TOKlparen)
 	    {   
 	    	parsingErrorInsertToComplete(prevToken, "!(TemplateArgumentList)", "TemplateType");
 	    	return new Objects(0);
 	    }
 	    return parseTemplateArgumentList2();
 	}
 	
 	private Objects parseTemplateArgumentList2() {
 		Objects tiargs = new Objects(3);
 		nextToken();
 
 		// Get TemplateArgumentList
 		if (token.value != TOKrparen) {
 			while (true) {
 				// See if it is an Expression or a Type
 				if (isDeclaration(token, 0, TOKreserved, null)) { // Type
 					Type ta;
 
 					// Get TemplateArgument
 					if (apiLevel < D2) {
 						ta = parseBasicType();
 						ta = parseDeclarator(ta, null);
 					} else {
 						ta = parseType();
 					}
 					tiargs.add(ta);
 				} else { 
 					// Expression
 					Expression ea = parseAssignExp();
 
 					if (apiLevel >= D2) {
 						if (ea.op == TOKfunction) {
 							FuncLiteralDeclaration fd = ((FuncExp) ea).fd;
 							if (fd.type.ty == Tfunction) {
 								TypeFunction tf = (TypeFunction) fd.type;
 								/*
 								 * If there are parameters that consist of only
 								 * an identifier, rather than assuming the
 								 * identifier is a type, as we would for regular
 								 * function declarations, assume the identifier
 								 * is the parameter name, and we're building a
 								 * template with a deduced type.
 								 */
 								TemplateParameters tpl = null;
 								for (int i = 0; i < ASTDmdNode
 										.size(tf.parameters); i++) {
 									Argument param = (Argument) tf.parameters
 											.get(i);
 									if (param.ident == null
 											&& param.type != null
 											&& param.type.ty == Tident
 											&& ASTDmdNode
 													.size(((TypeIdentifier) param.type).idents) == 0) {
 										/*
 										 * Switch parameter type to parameter
 										 * identifier, parameterize with
 										 * template type parameter _T
 										 */
 										TypeIdentifier pt = (TypeIdentifier) param.type;
 										param.ident = pt.ident;
 										IdentifierExp id = uniqueId("__T");
 										param.type = new TypeIdentifier(pt.filename, pt.lineNumber,
 												id);
 										TemplateParameter tp = new TemplateTypeParameter(
 												fd.filename, fd.lineNumber, id, null, null);
 										if (null == tpl)
 											tpl = new TemplateParameters(3);
 										tpl.add(tp);
 									}
 								}
 
 								if (tpl != null) { 
 									// Wrap a template around function fd
 									Dsymbols decldefs = new Dsymbols(1);
 									decldefs.add(fd);
 									TemplateDeclaration tempdecl = new TemplateDeclaration(
 											fd.filename, fd.lineNumber, fd.ident, tpl, null,
 											decldefs);
 									tempdecl.literal = true; // it's a template
 															// 'literal'
 									tiargs.add(tempdecl);
 									// goto L1;
 									if (token.value != TOKcomma) {
 										break;
 									}
 									nextToken();
 									continue;
 								}
 							}
 						}
 					}
 					tiargs.add(ea);
 				}
 				if (token.value != TOKcomma) {
 					break;
 				}
 				nextToken();
 			}
 		}
 		check(TOKrparen);
 		return tiargs;
 	}
 	
 	/*****************************
 	 * Parse single template argument, to support the syntax:
 	 *	foo!arg
 	 * Input:
 	 *	current token is the arg
 	 */
 	private Objects parseTemplateArgument()
 	{
 	    Objects tiargs = new Objects(3);
 	    Type ta;
 	    switch (token.value)
 	    {
 		case TOKidentifier:
 		    ta = new TypeIdentifier(filename, lineNumber, newIdentifierExp());
 		    // goto LabelX;
 		    tiargs.add(ta);
 		    nextToken();
 		    break;
 
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
 			ta = newTypeBasicForCurrentToken();
 		    tiargs.add(ta);
 		    nextToken();
 		    break;
 
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
 		case TOKfile:
 		case TOKline:
 		{   // Template argument is an expression
 		    Expression ea = parsePrimaryExp();
 		    tiargs.add(ea);
 		    break;
 		}
 		default:
 			parsingErrorInsertToComplete(token, "TemplateArgument", "TemplateInstantiation");
 		    break;
 	    }
 	    if (token.value == TOKnot) {
 	    	parsingErrorDeleteToken(token);
 	    }
 	    return tiargs;
 	}
 
 	
 	private Import parseImport(Dsymbols decldefs, boolean isstatic, int start, List<Comment> lastComments) {
 		Import s = null;
 		IdentifierExp id = null;
 		IdentifierExp aliasid = null;
 		Identifiers a = null;
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
 					
 					// Issue a creation of an empty import declaration
 					newImport(filename, lineNumber, null, null, aliasid, isstatic);
 					
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
 						a = new Identifiers(3);
 					}
 					a.add(id);
 					nextToken();
 					if (token.value != TOKidentifier) {
 						// Issue a creation of an empty import declaration
 						newImport(filename, lineNumber, a, null, aliasid, isstatic);
 						
 						parsingErrorInsertTokenAfter(prevToken, "Identifier");
 						break;
 					}
 					id = newIdentifierExp();
 					nextToken();
 				}
 
 				Import prev = s;
 				s = newImport(filename, lineNumber, a, id, aliasid, isstatic);
 				this.setPreComments(s, lastComments);
 				s.first = prev == null;
 				//decldefs.add(s);
 				if (prev == null) {
 					s.firstStart = start;
 				} else {
 					prev.next = s;
 				}
 
 				/*
 				 * Look for : alias=name, alias=name; syntax.
 				 */
 				if (token.value == TOKcolon) {
 					do {
 						IdentifierExp name = null;
 						IdentifierExp alias = null;
 						
 						nextToken();
 
 						if (token.value != TOKidentifier) {
 							// Signal an empty alias added
 							s = addImportAlias(s, null, null);
 							
 							parsingErrorInsertTokenAfter(prevToken, "Identifier");							
 							
 							decldefs.add(s);
 							break;
 						}
 						
 						alias = newIdentifierExp();
 						nextToken();						
 						if (token.value == TOKassign) {
 							nextToken();
 							if (token.value != TOKidentifier) {
 								// Signal an empty alias added
 								s = addImportAlias(s, newIdentifierExp(), null);
 								
 								parsingErrorInsertTokenAfter(prevToken, "Identifier");
 								
 								decldefs.add(s);
 								break;
 							}
 							name = newIdentifierExp();
 							nextToken();
 						} else {
 							name = alias;
 							alias = null;
 						}
 						s = addImportAlias(s, name, alias);
 						s.setSourceRange(sStart, prevToken.ptr + prevToken.sourceLen - sStart);
 					} while (token.value == TOKcomma);
 					
 					decldefs.add(s);
 					
 					break; // no comma-separated imports of this form
 				} else {
 					decldefs.add(s);
 					s.setSourceRange(sStart, prevToken.ptr + prevToken.sourceLen - sStart);
 				}
 
 				aliasid = null;
 			} while (token.value == TOKcomma);
 		}
 		
 		if (s != null) {
 			s.lastLength = token.ptr + token.sourceLen - s.start;
 		}
 
 		check(TOKsemicolon);
 
 		return null;
 	}
 	
 	
 
 	public Type parseType() {
 		return parseType(null, null);
 	}
 	
 	private Type parseType(IdentifierExp[] pident) {
 		return parseType(pident, null);
 	}
 	
 	private Type parseType(IdentifierExp[] pident, TemplateParameters[] tpl) {   
 		Type t;
 
 	    /* Take care of the storage class prefixes that
 	     * serve as type attributes:
 	     *  const shared, shared const, const, invariant, shared
 	     */
 	    if (token.value == TOKconst && peekNext() == TOKshared
 				&& peekNext2() != TOKlparen || token.value == TOKshared
 				&& peekNext() == TOKconst && peekNext2() != TOKlparen) {
 			nextToken();
 			nextToken();
 			/*
 			 * shared const type
 			 */
 			t = parseType(pident, tpl);
 			t = t.makeSharedConst(holder);
 			return t;
 		} else if (token.value == TOKconst && peek(token).value != TOKlparen) {
 			int start = token.ptr;
 			nextToken();
 			/* const type
 			 */
 			t = parseType(pident, tpl);
 			t = t.makeConst(start, prevToken.ptr + prevToken.sourceLen - start, holder);
 			return t;
 		} else if ((token.value == TOKinvariant || token.value == TOKimmutable)
 				&& peek(token).value != TOKlparen) {
 			int start = token.ptr;
 			nextToken();
 			/* invariant type
 			 */
 			t = parseType(pident, tpl);
 			t = t.makeInvariant(start, prevToken.ptr + prevToken.sourceLen - start, holder);
 			return t;
 		} else if (token.value == TOKshared && peekNext() != TOKlparen)
 		    {
 			nextToken();
 			/* shared type
 			 */
 			t = parseType(pident, tpl);
 			t = t.makeShared(holder);
 			return t;
 		} else {
 			t = parseBasicType();
 		}
 		t = parseDeclarator(t, pident, tpl, new int[1]);
 		return t;
 	}
 	
 	private Type parseBasicType() {
 		Type t = null;
 		IdentifierExp id = null;
 		TypeQualified tid = null;
 		TemplateInstance tempinst = null;
 		
 		expect(basicTypeExpectations);
 		
 		switch (token.value) {
 		// CASE_BASIC_TYPES_X(t):
 		case TOKvoid:
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
 		case TOKbit:
 		case TOKbool:
 		case TOKchar:
 		case TOKwchar:
 		case TOKdchar: 
 			t = newTypeBasicForCurrentToken(); 
 			t.setSourceRange(token.ptr, token.sourceLen); 
 			nextToken(); 
 			break;
 
 		case TOKidentifier:
 			id = newIdentifierExp();
 			nextToken();
 			if (token.value == TOKnot) {
 				if (apiLevel < D2) {
 					nextToken();
 					tempinst = newTemplateInstance(filename, lineNumber, id, encoder);
 					tempinst.tiargs(parseTemplateArgumentList());
 				} else {
 				    // ident!(template_arguments)
 					tempinst = newTemplateInstance(filename, lineNumber, id, encoder);
 					nextToken();
 					if (token.value == TOKlparen) {
 					    // ident!(template_arguments)
 					    tempinst.tiargs(parseTemplateArgumentList());
 					} else {
 					    // ident!template_argument
 					    tempinst.tiargs(parseTemplateArgument());
 					}
 				}
 				tempinst.setSourceRange(id.start, prevToken.ptr + prevToken.sourceLen - id.start);
 				tid = new TypeInstance(filename, lineNumber, tempinst);
 				tid.setSourceRange(id.start, prevToken.ptr + prevToken.sourceLen - id.start);
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
 			tid = newTypeIdentifier(filename, lineNumber, id);
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
 			id = new IdentifierExp(filename, lineNumber, Id.empty);
 			// goto Lident;
 			tid = newTypeIdentifier(filename, lineNumber, id);
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
 			
 			if (apiLevel >= D2) {
 				tid = parseTypeof();
 			} else {
 				Expression exp;
 	
 				nextToken();
 				check(TOKlparen);
 				exp = parseExpression();
 				check(TOKrparen);
 				
 				tid = new TypeTypeof(filename, lineNumber, exp, encoder);
 				tid.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 				((TypeTypeof) tid).setTypeofSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 			}
 			
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
 			
 		case TOKconst: {
 			if (apiLevel < D2) {
 				parsingErrorInsertTokenAfter(prevToken, "Type");
 				break;
 			}
 			
 			int start = token.ptr;
 		    nextToken();
 		    check(TOKlparen);
 		    t = parseType();
 		    check(TOKrparen);
 		    if (t.isShared()) {
 				t = t.makeSharedConst(holder);
 		    } else {
 				t = t.makeConst(start, prevToken.ptr + prevToken.sourceLen - start, holder);
 		    }
 		    break;
 		}
 
 		case TOKinvariant:
 		case TOKimmutable:
 		{
 			if (apiLevel < D2) {
 				parsingErrorInsertTokenAfter(prevToken, "Type");
 				break;
 			}
 			
 			int start = token.ptr;
 		    nextToken();
 		    check(TOKlparen);
 		    t = parseType();
 		    check(TOKrparen);
 		    t = t.makeInvariant(start, prevToken.ptr + prevToken.sourceLen - start, holder);
 		    break;
 		}
 		
 		case TOKshared: {
 			if (apiLevel < D2) {
 				parsingErrorInsertTokenAfter(prevToken, "Type");
 				break;
 			}
 			
 		    // shared(type)
 		    nextToken();
 			check(TOKlparen);
 			t = parseType();
 			check(TOKrparen);
 			if (t.isConst()) {
 				t = t.makeSharedConst(holder);
 			} else {
 				t = t.makeShared(holder);
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
 				if (apiLevel < D2) {
 					nextToken();
 					tempinst[0] = newTemplateInstance(filename, lineNumber, id[0], encoder);
 					tempinst[0].tiargs(parseTemplateArgumentList());
 					tempinst[0].setSourceRange(tempinstStart, prevToken.ptr + prevToken.sourceLen - tempinstStart);
 					
 				} else {
 				    tempinst[0] = newTemplateInstance(filename, lineNumber, id[0], encoder);
 					nextToken();
 					if (token.value == TOKlparen) {
 						// ident!(template_arguments)
 						tempinst[0].tiargs(parseTemplateArgumentList());
 					} else {
 						// ident!template_argument
 						tempinst[0].tiargs(parseTemplateArgument());
 					}
 				}
 				tid[0].addIdent(new TemplateInstanceWrapper(filename, lineNumber, tempinst[0]));
 			} else {
 				tid[0].addIdent(id[0]);
 			}
 		}
 		tid[0].setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 		t[0] = tid[0];
 	}
 
 	private Type parseBasicType2(Type t) {
 		if (t == null)
 			return null;
 		
 		Type ts;
 		Type ta;
 		Type subType;
 
 		expect(delegateFunctionExpectations);
 		while (true) {
 			switch (token.value) {
 			case TOKmul:
 				subType = t;
 				
 				t = new TypePointer(subType);
 				t.setSourceRange(subType.start, token.ptr + token.sourceLen - subType.start);
 				
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
 						t.setSourceRange(subType.start, token.ptr + token.sourceLen - subType.start);
 						
 						nextToken();
 					} else if (isDeclaration(token, 0, TOKrbracket, null)) { // It's
 																				// an
 																				// associative
 																				// array
 																				// declaration
 						subType = t;
 						Type index;
 
 						if (apiLevel < D2) {
 							index = parseBasicType();
 							index = parseDeclarator(index, null); // [ type ]
 						} else {
 							index = parseType();                  // [ type ]
 						}
 						
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
 							
 							t = new TypeSlice(t, e, e2, encoder);
 						} else {
 							t = new TypeSArray(t, e, encoder);
 						}
 					    t.setSourceRange(subType.start, token.ptr + token.sourceLen - subType.start);
 						
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
 							ta = new TypeSArray(t, e, encoder);
 							check(TOKrbracket);
 						}
 
 						if (ts != t) {
 							Type pt = ts;
 							while(pt.nextOf() != t) {
 								pt = pt.nextOf();
 							}
 							((TypeNext)pt).next = ta;
 							((TypeNext)pt).sourceNext = ta;
 						} else {
 							ts = ta;
 						}
 					}
 					continue;
 				}
 
 			case TOKdelegate:
 			case TOKfunction: { 
 				// Handle delegate declaration:
 				// t delegate(parameter list) nothrow pure
 				// t function(parameter list) nothrow pure
 				Arguments arguments;
 				int varargs = 0;
 				boolean ispure = false;
 				boolean isnothrow = false;
 				TOK save = token.value;
 
 				nextToken();
 
 				int[] pointer2_varargs = { varargs };
 				arguments = parseParameters(pointer2_varargs);
 				varargs = pointer2_varargs[0];
 
 				int saveStart = t.start;
 				
 				if (apiLevel >= D2) {
 					while (true) {
 						if (token.value == TOKpure) {
 							ispure = true;
 						} else if (token.value == TOKnothrow) {
 							isnothrow = true;
 						} else {
 							break;
 						}
 						nextToken();
 					}
 				}
 				
 				t = new TypeFunction(arguments, t, varargs, linkage);
 				
 				((TypeFunction) t).ispure = ispure;
 				((TypeFunction) t).isnothrow = isnothrow;
 				
 				// Assign parent of arguments
 				if (arguments != null) {
 					for(int i = 0; i < arguments.size(); i++) {
 						arguments.get(i).parentType = (TypeFunction) t;
 					}
 				}
 				
 				if (save == TOKdelegate) {
 					t = new TypeDelegate(t);
 				} else {
 					t = new TypePointer(t);
 				}
 				t.setSourceRange(saveStart, prevToken.ptr + prevToken.sourceLen - saveStart);
 				continue;
 			}
 
 			default:
 				if (apiLevel < D2) {
 					ts = t;
 				} else {
 					return t;
 				}
 				break;
 			}
 			break;
 		}
 		
 		if (apiLevel < 2) {
 			return ts;
 		} else {
 			return null;
 		}
 	}
 	
 	private Type parseDeclarator(Type targ, IdentifierExp[] ident) {
 		return parseDeclarator(targ, ident, null, null);
 	}
 
 	private Type parseDeclarator(Type t, IdentifierExp[] pident,
 			TemplateParameters[] tpl, int[] identStart) {
 		Type ts;
 		Type ta;
 
 		t = parseBasicType2(t);
 
 		switch (token.value) {
 
 		case TOKidentifier:
 			if (pident != null) {
 				pident[0] = newIdentifierExp();
 				if (identStart != null) {
 					identStart[0] = token.ptr;
 				}
 			} else {
 				error(IProblem.UnexpectedIdentifierInDeclarator,
 						token.lineNumber, token.ptr, token.sourceLen);
 			}
 			ts = t;
 			nextToken();
 			break;
 
 		case TOKlparen:
 		    /* Parse things with parentheses around the identifier, like:
 		     *	int (*ident[3])[]
 		     * although the D style would be:
 		     *	int[]*[3] ident
 		     */
 			int oldStart = t == null ? 0 : t.start;
 			nextToken();
 			ts = parseDeclarator(t, pident, null, identStart);
 			if (ts != null) {
 				ts.setSourceRange(oldStart, token.ptr + token.sourceLen - oldStart);
 			}
 			check(TOKrparen);
 			break;
 
 		default:
 			ts = t;
 			break;
 		}
 
 	    // parse DeclaratorSuffixes
 		while (true) {
 			switch (token.value) {
 			//#if CARRAYDECL
 			case TOKlbracket: { // This is the old C-style post [] syntax.
 				nextToken();
 				if (token.value == TOKrbracket) // []
 				{
 					
 					// BM DDT BUGFIX: added null check for t
 					if(t == null) {
 						// t can be null on certain files with syntax errors
 						// try to workaround by supplying a dummy type. Not sure this is correct.
 						ta = new TypeDArray(new TypeBasic(TY.Tvoid));
 						ta.setSourceRange(token.ptr, token.sourceLen);
 					} else {
 						ta = new TypeDArray(t);
 						ta.setSourceRange(t.start, token.ptr + token.sourceLen
 								- t.start);
 					}
 
 					nextToken();
 				} else if (isDeclaration(token, 0, TOKrbracket, null)) { // It's an associative array declaration
 					Type index;
 
 					if (apiLevel < D2) {
 						index = parseBasicType();
 						index = parseDeclarator(index, null, null, identStart); // [ type ]
 					} else {
 						index = parseType(); // [ type ]
 					}
 
 					ta = newTypeAArray(t, index);
 
 					check(TOKrbracket);
 				} else {
 					Expression e = parseExpression(); // [ expression ]
 
 					ta = new TypeSArray(t, e, encoder);
 					ta.setSourceRange(t.start, token.ptr + token.sourceLen
 							- t.start);
 					check(TOKrbracket);
 				}
 
 				if (ts != t) {
 					Type pt = ts;
 					while (((TypeNext)pt).nextOf() != t) {
 						pt = pt.nextOf();
 					}
 					((TypeNext)pt).next = ta;
 					((TypeNext)pt).sourceNext = ta;
 				} else {
 					ts = ta;
 				}
 				continue;
 			}
 				//#endif
 			case TOKlparen: {
 				Arguments arguments;
 				int varargs = 0;
 				
 				int funcIdentStart = prevToken.ptr;
 
 				if (tpl != null) {
 					/* Look ahead to see if this is (...)(...),
 					 * i.e. a function template declaration
 					 */
 					if (peekPastParen(token).value == TOKlparen) { // It's a function template declaration
 						// Gather template parameter list
 						tpl[0] = parseTemplateParameterList();
 					}
 				}
 
 				int[] pointer2_varargs = { varargs };
 				arguments = parseParameters(pointer2_varargs);
 				varargs = pointer2_varargs[0];
 
 				ta = new TypeFunction(arguments, t, varargs, linkage);
 
 				// Assign parent of arguments
 				if (arguments != null) {
 					for (int i = 0; i < arguments.size(); i++) {
 						arguments.get(i).parentType = (TypeFunction) ta;
 					}
 				}
 				
 				if (apiLevel >= D2) {
 					/* Parse const/invariant/nothrow postfix
 					 */
 					while (true)
 					{
 					    switch (token.value)
 					    {
 						case TOKconst:
 						    ta = ta.makeConst(token.ptr, token.sourceLen, holder);
 						    nextToken();
 						    continue;
 
 						case TOKinvariant:
 						case TOKimmutable:
 						    ta = ta.makeInvariant(token.ptr, token.sourceLen, holder);
 						    nextToken();
 						    continue;
 						    
 						case TOKshared:
 						    if (ta.isConst()) {
 								ta = ta.makeSharedConst(holder);
 							} else {
 								ta = ta.makeShared(holder);
 							}
 						    nextToken();
 						    continue;
 
 						case TOKnothrow: {
 							TypeFunction tf = (TypeFunction) ta;
 						    tf.isnothrow = true;
 						    if (tf.postModifiers == null) {
 						    	tf.postModifiers = new ArrayList<Modifier>();
 						    }
 						    tf.postModifiers.add(new Modifier(token, lineNumber));
 						    nextToken();
 						    continue;
 						}
 
 						case TOKpure: {
 							TypeFunction tf = (TypeFunction) ta;
 						    tf.ispure = true;
 						    if (tf.postModifiers == null) {
 						    	tf.postModifiers = new ArrayList<Modifier>();
 						    }
 						    tf.postModifiers.add(new Modifier(token, lineNumber));
 						    nextToken();
 						    continue;
 						}
 					    }
 					    break;
 					}
 				}
 
 				if (t == null) {
 					ta.setSourceRange(funcIdentStart, prevToken.ptr + prevToken.len);
 				} else {
 					ta.setSourceRange(t.start, t.length);
 				}
 
 				if (ts != t) {
 					Type pt = ts;
 					while (pt.nextOf() != t) {
 						pt = pt.nextOf();
 					}
 					((TypeNext)pt).next = ta;
 					((TypeNext)pt).sourceNext = ta;
 				} else {
 					ts = ta;
 				}
 				break;
 			}
 			}
 			break;
 		}
 
 		return ts;
 	}
 	
 	private Dsymbols parseDeclarations(List<Comment> lastComments) {
 		return parseDeclarations(0, lastComments);
 	}
 	
 	private Dsymbols parseDeclarations(int stc) {
 		return parseDeclarations(stc, new ArrayList<Comment>());
 	}
 	
 	private Dsymbols parseDeclarations(int stc, List<Comment> lastComments) {
 		return parseDeclarations(stc, lastComments, false);
 	}
 	
 	private Dsymbols parseDeclarations(List<Comment> lastComments, boolean alreadyThinksItsD2) {
 		return parseDeclarations(0, lastComments, alreadyThinksItsD2);
 	}
 	
 	@SuppressWarnings("unchecked")
 	private Dsymbols parseDeclarations(int stc, List<Comment> lastComments, boolean alreadyThinksItsD2) {
 		int storage_class = 0;
 		Type ts = null;
 		Type t = null;
 		Type tfirst = null;
 		IdentifierExp ident = null;
 		int lineNumber = 0;
 		Dsymbols a = null;
 		TOK tok = TOK.TOKreserved;
 		LINK link = linkage;
 		
 		List<Modifier> modifiers = new ArrayList<Modifier>();
 		
 		int start = token.ptr;
 		int prevStart = prevToken.ptr;
 		
 		boolean gotoL2 = false;
 		if (stc != 0) {
 			gotoL2 = true; // infer type
 		}
 		
 		boolean first = true;
 		int nextVarStart = token.ptr;
 		int nextTypdefOrAliasStart = start;
 		VarDeclaration previousVar = null;
 
 		if (!gotoL2) {
 			expect(typedefAliasExpectations);
 			switch (token.value) {
 			case TOKalias:
 				tok = token.value;
 				nextToken();
 			    if (token.value == TOKidentifier && peek(token).value == TOKthis) {
 			    	// XXX comments for AliasThis
 					AliasThis s = newAliasThis(this.filename, this.lineNumber, newIdentifierExp());
 					nextToken();
 					check(TOKthis);
 					check(TOKsemicolon);
 					s.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 					this.setPreComments(s, lastComments);
 					a = new Dsymbols(1);
 					a.add(s);
 					return a;
 				}
 				break;
 			case TOKtypedef:
 				tok = token.value;
 				nextToken();
 				break;
 			default:
 				tok = TOKreserved;
 				break;
 			}
 	
 			expect(modifierExpectations);
 			storage_class = stc;
 			
 			while (true) {
 				switch (token.value) {
 				case TOKconst:
 				case TOKinvariant:
 				case TOKimmutable:
 				case TOKshared:
 					if (apiLevel >= D2 && peek(token).value == TOKlparen) {
 						break;
 					}
 					// fall
 				case TOKstatic:
 				case TOKfinal:
 				case TOKauto:
 				case TOKscope:
 				case TOKoverride:
 				case TOKabstract:
 				case TOKsynchronized:
 				case TOKdeprecated:
 				case TOKnothrow:
 				case TOKpure:
 				case TOKref:
 				case TOKtls:
 				case TOKgshared:
 				case TOKenum:
 					if (apiLevel < D2 && (
 						token.value == TOKnothrow ||
 						token.value == TOKpure ||
 						token.value == TOKref ||
 						token.value == TOKtls ||
 						token.value == TOKgshared ||
 						token.value == TOKenum
 						)) {
 						break;
 					}
 					
 					stc = STC.fromTOK(token.value);
 					
 					Modifier currentModifier = newModifier();
 					if ((storage_class & stc) != 0) {
 						error(IProblem.RedundantStorageClass, token.lineNumber, currentModifier);
 					}
 					storage_class = storage_class | stc;
 					if (apiLevel >= D2) {
 						composeStorageClass(storage_class);
 					}
 					modifiers.add(currentModifier);
 					
 					nextToken();
 					continue;
 	
 				case TOKextern:
 					if (peek(token).value != TOKlparen) {
 						stc = STC.fromTOK(token.value);
 						
 						currentModifier = newModifier(token);
 						if ((storage_class & stc) != 0) {
 							error(IProblem.RedundantStorageClass, token.lineNumber, currentModifier);
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
 			
 			// Descent: better error reporting if source level is D1 but
 			// we find "invariant(...)" or "const(...)"
 			boolean thinksItsD2 = alreadyThinksItsD2;
 			if (!thinksItsD2 && apiLevel < D2 && token.value == TOKlparen && (prevToken.value == TOKinvariant || prevToken.value == TOKconst)) {
 				thinksItsD2 = true;
 				error(prevToken.value == TOKinvariant ? 
 						IProblem.InvariantAsAttributeIsOnlySupportedInD2 :
 						IProblem.ConstAsAttributeIsOnlySupportedInD2, prevToken);
 				nextToken();
 			}
 	
 			a = new Dsymbols(2);
 			
 			first = true;
 			previousVar = null;
 	
 			/*
 			 * Look for auto initializers: storage_class identifier = initializer;
 			 */
 			
 			if (apiLevel >= D2) {
 				if (storage_class != 0 && token.value == TOKidentifier
 						&& peek(token).value == TOKassign) {
 					return parseAutoDeclarations(stc, token.ptr, start, modifiers);
 				}
 			} else {
 				while (storage_class != 0 && token.value == TOKidentifier
 						&& peek(token).value == TOKassign) {
 					ident = newIdentifierExp();
 					lineNumber = token.lineNumber;
 					
 					nextToken();
 					nextToken();
 					Initializer init = parseInitializer();
 		
 					VarDeclaration v = newVarDeclaration(filename, lineNumber, null, ident, init);
 					v.first = first;
 					first = false;
 					
 					v.storage_class = storage_class;
 					addModifiers(v, modifiers);
 					a.add(v);
 					
 					if (previousVar != null) {
 						previousVar.next = v;
 					}
 					previousVar = v;
 					
 					if (token.value == TOKsemicolon) {
 						v.setSourceRange(start, token.ptr + token.sourceLen - start);
 						
 						// Discard any previous comments
 						if (comments != null) {
 							lastCommentRead = comments.size();
 						}
 						
 						nextToken();
 						this.setPreComments(v, lastComments);
 						attachLeadingComments(v);
 					} else if (token.value == TOKcomma) {
 						v.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 						
 						// Discard any previous comments
 						if (comments != null) {
 							lastCommentRead = comments.size();
 						}
 						
 						nextToken();
 						if (!(token.value == TOKidentifier && peek(token).value == TOKassign)) {
 							parsingErrorInsertTokenAfter(prevToken, "identifier");
 						} else {
 							start = token.ptr;
 							continue;
 						}
 					} else {
 						parsingErrorInsertTokenAfter(prevToken, ";");
 					}
 					
 					return a;
 				}
 			}
 	
 			expect(classExpectations);
 			if (token.value == TOKclass) {
 				AggregateDeclaration s;
 	
 				s = (AggregateDeclaration) parseAggregate();
 				s.storage_class = storage_class;
 				addModifiers(s, modifiers);
 				a.add(s);
 				this.setPreComments(s, lastComments);
 				return a;
 			}
 			
 			nextVarStart = token.ptr;
 			nextTypdefOrAliasStart = start;
 			
 			if (apiLevel >= D2) {
 				/*
 				 * Look for return type inference for template functions.
 				 */
 				{
 					Token[] tk = { null };
 					if (storage_class != 0 && token.value == TOKidentifier
 							&& (tk[0] = peek(token)).value == TOKlparen
 							&& skipParens(tk[0], tk)
 							&& peek(tk[0]).value == TOKlparen) {
 						ts = null;
 					} else {
 						ts = parseBasicType();
 						ts = parseBasicType2(ts);
 					}
 				}
 			} else {
 				ts = parseBasicType();
 				
 				if (thinksItsD2 && token.value == TOKrparen) {
 					nextToken();
 				}
 				
 				if (ts == null) {
 					return a;
 				}
 				ts = parseBasicType2(ts);
 			}
 		}
 		
 		// L2:
 		tfirst = null;
 		a = new Dsymbols();
 		
 		int[] identStart = new int[1];
 		
 		AliasDeclaration previousAlias = null;
 		TypedefDeclaration previousTypedef = null;
 		first = true;
 		while (true) {
 			TemplateParameters tpl = null;
 
 			ident = null;
 			IdentifierExp[] pointer2_ident = { ident };
 			TemplateParameters[] pointer2_tpl = { tpl };
 			t = parseDeclarator(ts, pointer2_ident, pointer2_tpl, identStart);
 			ident = pointer2_ident[0];
 			tpl = pointer2_tpl[0];
 			
 			if(t == null) {
 				break;
 			}
 			if (tfirst == null) {
 				tfirst = t;
 			} else if (t.singleton != tfirst.singleton) {
 				// TODO check this, should be doing this
 				if (ident != null) {
 					error(IProblem.MultipleDeclarationsMustHaveTheSameType,
 							 lineNumber, ident.start, ident.length);
 				}
 			}
 			if (ident == null) {
 				parsingErrorInsertTokenAfter(prevToken, "Identifier");
 				//return a;
 			}
 
 			if (tok == TOKtypedef || tok == TOKalias) {
 				Declaration v;
 				Initializer init = null;
 				
 				int assignTokenStart = token.ptr;
 				int assignTokenLine = token.lineNumber;
 				if (token.value == TOKassign) {
 					nextToken();
 					init = parseInitializer();
 				}
 				TypedefDeclaration td = null;
 				AliasDeclaration ad = null;
 				if (tok == TOKtypedef) {
 					td = newTypedefDeclaration(filename, lineNumber, ident, t, init);
 					td.first = first;
 					v = td;
 					if (previousTypedef != null) {
 						previousTypedef.next = td;
 					}
 					previousTypedef = td;
 				} else {
 					if (init != null) {
 						error(IProblem.AliasCannotHaveInitializer, assignTokenLine, assignTokenStart,  init.start + init.length - assignTokenStart);
 					}
 					
 					ad = newAliasDeclaration(filename, lineNumber, ident, t);
 					ad.first = first;
 					v = ad;
 					if (previousAlias != null) {
 						previousAlias.next = ad;
 					}
 					previousAlias = ad;
 				}
 				first = false;
 				
 				addModifiers(v, modifiers);
 				v.storage_class = storage_class;
 				
 			    if (link == linkage) {
 					a.add(v);
 			    } else {
 			    	// TODO: this is never reached by tests
 			    	Dsymbols ax = new Dsymbols(1);
 			    	ax.add(v);
 			    	Dsymbol s = newLinkDeclaration(link, ax);
 			    	a.add(s);
 			    }
 				
 				switch (token.value) {
 				case TOKsemicolon:			
 					v.setSourceRange(nextTypdefOrAliasStart, token.ptr + token.sourceLen - nextTypdefOrAliasStart);
 					
 					// Discard any previous comments
 					if (comments != null) {
 						lastCommentRead = comments.size();
 					}
 					
 					nextToken();
 					this.setPreComments(v, lastComments);
 					attachLeadingComments(v);
 					break;
 
 				case TOKcomma:
 					// Discard any previous comments
 					if (comments != null) {
 						lastCommentRead = comments.size();
 					}
 					
 					v.setSourceRange(nextTypdefOrAliasStart, prevToken.ptr + prevToken.sourceLen - nextTypdefOrAliasStart);
 					nextToken();
 					continue;
 
 				default:
 					// Discard any previous comments
 					if (comments != null) {
 						lastCommentRead = comments.size();
 					}
 					
 					v.setSourceRange(nextTypdefOrAliasStart, prevToken.ptr + prevToken.sourceLen - nextTypdefOrAliasStart);
 					parsingErrorInsertTokenAfter(prevToken, ";");
 					break;
 				}
 			} else if (t.ty == Tfunction) {
 				TypeFunction typeFunction = (TypeFunction) t;
 				Expression constraint = null;
 				
 				FuncDeclaration f = newFuncDeclaration(filename, lineNumber, ident, storage_class, typeFunction);
 				
 				if (apiLevel >= 2) {
 					if (tpl != null) {
 						constraint = parseConstraint();
 					}
 				}
 				
 				parseContracts(f);
 				
 				int theStart = t.start;
 				// was type inference, adjust position to "auto"
 				if (gotoL2) {
 					theStart = prevStart;
 				}
 				
 				f.setSourceRange(theStart, prevToken.ptr + prevToken.sourceLen - theStart);
 				
 				// it's a function template
 				if (tpl != null) {
 					f.templated = true;
 				}
 				
 				Dsymbol s;
 				if (link == linkage) {
 					s = f;
 				} else {
 					Dsymbols ax = new Dsymbols(1);
 					ax.add(f);
 					s = newLinkDeclaration(link, ax);
 				}
 					
 				if (tpl != null) { // it's a function template
 					Dsymbols decldefs;
 					TemplateDeclaration tempdecl;
 
 					// Wrap a template around the aggregate declaration
 					decldefs = new Dsymbols(1);
 					decldefs.add(s);
 					tempdecl = newTemplateDeclaration(filename, lineNumber, s.ident, tpl, constraint, decldefs);
 					tempdecl.setSourceRange(s.start, s.length);
 					tempdecl.wrapper = true;
 					s = tempdecl;
 				}
 				setModifiers(s, modifiers);
 				this.setPreComments(s, lastComments);
 				attachLeadingComments(s);
 				a.add(s);
 			} else {
 				VarDeclaration v;
 				Initializer init = null;
 				if (token.value == TOKassign) {
 					nextToken();
 					init = parseInitializer();
 				}
 				
 				v = newVarDeclaration(filename, lineNumber, t, ident, init);
 				v.first = first;
 				first = false;
 				
 				v.storage_class = storage_class;
 				setModifiers(v, modifiers);
 				a.add(v);
 				
 				if (previousVar != null) {
 					previousVar.next = v;
 				}
 				previousVar = v;
 				
 				switch (token.value) {
 				case TOKsemicolon:
 					v.setSourceRange(nextVarStart, token.ptr + token.sourceLen - nextVarStart);
 					
 					// Discard any previous comments
 					if (comments != null) {
 						lastCommentRead = comments.size();
 					}
 					
 					nextToken();
 					this.setPreComments(v, lastComments);
 					attachLeadingComments(v);
 					break;
 
 				case TOKcomma:
 					v.setSourceRange(nextVarStart, prevToken.ptr + prevToken.sourceLen - nextVarStart);
 					
 					// Discard any previous comments
 					if (comments != null) {
 						lastCommentRead = comments.size();
 					}
 					
 					nextToken();
 					continue;
 
 				default:
 					// Discard any previous comments
 					if (comments != null) {
 						lastCommentRead = comments.size();
 					}
 					
 					v.setSourceRange(nextVarStart, prevToken.ptr + prevToken.sourceLen - nextVarStart);
 					parsingErrorInsertTokenAfter(prevToken, ";");
 					break;
 				}
 			}
 			break;
 		}
 		
 		return a;
 	}
 
 	/*****************************************
 	 * Parses default argument initializer expression that is an assign expression,
 	 * with special handling for __FILE__ and __LINE__.
 	 */
 	public Expression parseDefaultInitExp() {
 		if (token.value == TOKfile || token.value == TOKline) {
 			Token t = peek(token);
 			if (t.value == TOKcomma || t.value == TOKrparen) {
 				Expression e;
 
 				if (token.value == TOKfile) {
 					e = new FileInitExp(filename, lineNumber);
 				} else {
 					e = new LineInitExp(filename, lineNumber);
 				}
 				nextToken();
 				return e;
 			}
 		}
 
 		Expression e = parseAssignExp();
 		return e;
 	}
 	
 	private void parseContracts(final FuncDeclaration f) {
 	    LINK linksave = linkage;
 
 	    // The following is irrelevant, as it is overridden by sc.linkage in
 	    // TypeFunction.semantic
 	    linkage = LINKd;		// nested functions have D linkage
 		
 		boolean repeat = true;
 		while (repeat) {
 			repeat = false;
 			// L1:
 			
 			expect(contractsExpectations);
 			
 			switch (token.value) {
 			case TOKlcurly:
 				if (f.frequire != null || f.fensure != null) {
 					parsingErrorInsertToComplete(prevToken, "body { ... }", "FunctionDeclaration");
 				}
 				
 				/*
 				 * Diet parsing: skip the function body if possible.
 				 * But... the function's body might be needed later if we need to interpret it.
 				 * So... assign the "rest" of it by parsing the actual body when needed.
 				 */
 				final int startSkip = token.ptr;
 				
 				Statement body = dietParseStatement(f);
 				f.setFbody(body);
 				
 				// Diet successful
 				if (body == null) {
 					f.setDiet(input, startSkip, p);
 				}
 				
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
 			 * #if 0 // Dumped feature case TOKthrow: if (!f.fthrows)
 			 * f.fthrows = new Array(); nextToken(); check(TOKlparen); while
 			 * (1) { tb = parseBasicType(); f.fthrows.push(tb); if
 			 * (token.value == TOKcomma) { nextToken(); continue; } break; }
 			 * check(TOKrparen); goto L1; #endif
 			 */
 
 			case TOKin:
 				if (f.frequire != null) {
 					error(IProblem.RedundantInStatement,
 							token);
 				}
 				nextToken();
 				
 				f.setFrequire(parseStatement(PScurly | PSscope));
 				repeat = true;
 				break;
 
 			case TOKout:
 				// parse: out (identifier) { statement }
 				
 				if (f.fensure != null) {
 					error(IProblem.RedundantOutStatement,
 							token.lineNumber, token.ptr, token.sourceLen);
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
 	    int brackets = 0;
 		
 		int start = token.ptr;
 
 		expect(voidExpectations);
 		switch (token.value) {
 		case TOKlcurly:
 		    /* Scan ahead to see if it is a struct initializer or
 		     * a function literal.
 		     * If it contains a ';', it is a function literal.
 		     * Treat { } as a struct initializer.
 		     */
 		    braces = 1;
 		    loop:
 			for (t = peek(token); true; t = peek(t)) {
 				switch (t.value) {
 				case TOKsemicolon:
 				case TOKreturn:
 					// goto Lexpression;
 					e = parseAssignExp();
 					ie = new ExpInitializer(filename, lineNumber, e);
 					return ie;
 
 				case TOKlcurly:
 					braces++;
 					continue;
 
 				case TOKrcurly:
 					if (--braces == 0) {
 						break;
 					}
 					continue;
 				case TOKeof:
 					break loop;
 				default:
 					continue;
 				}
 				break;
 			}
 			
 			is = new StructInitializer(filename, lineNumber);
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
 					is.setSourceRange(start, token.ptr + token.sourceLen - start);
 					nextToken();
 					break;
 				
 				case TOKeof:
 					parsingErrorInsertTokenAfter(prevToken, "}");
 					return null;
 					//break;
 
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
 		    /* Scan ahead to see if it is an array initializer or
 		     * an expression.
 		     * If it ends with a ';', ',' or '}' it is an array initializer.
 		     */
 		    brackets = 1;
 			for (t = peek(token); true; t = peek(t)) {
 				switch (t.value) {
 				case TOKlbracket:
 					brackets++;
 					continue;
 
 				case TOKrbracket:
 					if (--brackets == 0) {
 						t = peek(t);
 						if (t.value != TOKsemicolon && t.value != TOKcomma
 								&& t.value != TOKrcurly) {
 							// goto Lexpression;
 						    e = parseAssignExp();
 						    ie = new ExpInitializer(filename, lineNumber, e);
 						    return ie;
 						}
 						break;
 					}
 					continue;
 
 				case TOKeof:
 					break;
 
 				default:
 					continue;
 				}
 				break;
 			}
 			
 			ia = new ArrayInitializer(filename, lineNumber);
 			nextToken();
 			comma = 0;
 			while (true) {
 				switch (token.value) {
 				default:
 					if (comma == 1) {
 						parsingErrorInsertTokenAfter(prevToken, ",");
 						nextToken();
 						ia.setSourceRange(start, token.ptr + token.sourceLen - start);
 						break;
 					}
 					e = parseAssignExp();
 					if (e == null) {
 						break;
 					}
 					if (token.value == TOKcolon) {
 						nextToken();
 						value = parseInitializer();
 					} else {
 						value = new ExpInitializer(filename, lineNumber, e);
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
 					ia.setSourceRange(start, token.ptr + token.sourceLen - start);
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
 			if (t.value == TOKsemicolon || t.value == TOKcomma) {
 				nextToken();
 				return newVoidInitializerForToken(prevToken);
 			}
 			// goto Lexpression;
 
 		default:
 			// Lexpression:
 			e = parseAssignExp();
 			e.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 			ie = new ExpInitializer(filename, lineNumber, e);
 			return ie;
 		}
 	}
 	
 	@SuppressWarnings("unchecked") 
 	public Statement parseStatement(int flags) {
 		if (token.value == null) {
 			nextToken();
 		}
 		
 		Statement s = null;
 		Token t;
 		Statement ifbody;
 	    Statement elsebody;
 
 		if ((flags & PScurly) != 0 && token.value != TOKlcurly) {
 			error(
 					IProblem.StatementExpectedToBeCurlies, token);
 		}
 		
 		int start = token.ptr;
 
 		if (token.value != TOKlcurly) {
 			expect(statementExpectations);
 			if (apiLevel >= D2) {
 				expect(traitsExpectations);
 			}
 		}
 		
 		switch (token.value) {
 		case TOKidentifier:
 		    /* A leading identifier can be a declaration, label, or expression.
 		     * The easiest case to check first is label:
 		     */
 			t = peek(token);
 			if (t.value == TOKcolon) { // It's a label
 				IdentifierExp ident = newIdentifierExp();
 				nextToken();
 				nextToken();
 				Statement body = parseStatement(PSsemi);
 				
 				s = newLabelStatement(filename, lineNumber, ident, body);
 				s.start = ident.start;
 				s.length = prevToken.ptr + prevToken.sourceLen - ident.start;
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
 				
 				// Signal a new variable declaration if it's an identifier... this is nice
 				// to autocomplete Object | -. object
 				if (exp != null && exp.getNodeType() == ASTDmdNode.IDENTIFIER_EXP) {
 					newVarDeclaration(filename, lineNumber, new TypeIdentifier(filename, lineNumber, ((IdentifierExp) exp).ident), null, null);
 				}
 				
 				check(TOKsemicolon);
 				s = newExpStatement(filename, lineNumber, exp);
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
 		case TOKtraits:
 		case TOKfile:
 		case TOKline:
 		{
 			Expression exp = parseExpression();
 			check(TOKsemicolon);
 			s = newExpStatement(filename, lineNumber, exp);
 			s.setSourceRange(exp.start, prevToken.ptr + prevToken.sourceLen - exp.start);
 			break;
 		}
 
 		case TOKstatic: { // Look ahead to see if it's static assert() or
 							// static if()
 			Token t2;
 			
 			t2 = peek(token);
 			if (t2.value == TOKassert) {
 				nextToken();
 				
 				s = newStaticAssertStatement(parseStaticAssert());
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
 				
 				s = newConditionalStatement(filename, lineNumber, staticIfCondition, ifbody, elsebody);
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
 		case TOKfinal:
 		case TOKinvariant:
 		case TOKimmutable:
 		case TOKshared:
 		case TOKnothrow:
 		case TOKpure:
 		case TOKtls:
 		case TOKgshared:
 			
 		// case TOKtypeof:
 			// Ldeclaration:
 		{
 			if (apiLevel == D2 && token.value == TOKfinal && peekNext() == TOKswitch) {
 				nextToken();
 				s = parseStatement_Lswitch(true);
 			} else {
 				Statement[] ps = { s };
 				parseStatement_Ldeclaration(ps, flags);
 				s = ps[0];
 			}
 			break;
 		}
 
 		case TOKstruct:
 		case TOKunion:
 		case TOKclass:
 		case TOKinterface: {
 			List<Comment> lastComments = getLastComments();
 			Dsymbol d = parseAggregate();
 			if (d != null) {
 				d.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 				this.setPreComments(d, lastComments);
 				s = newDeclarationStatement(filename, lineNumber, d);
 			}
 			break;
 		}
 
 		case TOKenum: {
 			Dsymbol d;
 
 			if (apiLevel >= D2) {
 				/* Determine if this is a manifest constant declaration,
 				 * or a conventional enum.
 				 */
 				Token t2 = peek(token);
 				if (t2.value == TOKlcurly || t2.value == TOKcolon) {
 					d = parseEnum();
 				} else if (t2.value != TOKidentifier) {
 					// goto Ldeclaration;
 					Statement[] ps = { s };
 					parseStatement_Ldeclaration(ps, flags);
 					s = ps[0];
 				} else {
 					t2 = peek(t2);
 					if (t2.value == TOKlcurly || t2.value == TOKcolon
 							|| t2.value == TOKsemicolon) {
 						d = parseEnum();
 					} else {
 						// goto Ldeclaration;
 						Statement[] ps = { s };
 						parseStatement_Ldeclaration(ps, flags);
 						s = ps[0];
 					}
 				}
 			} else {
 				List<Comment> lastComments = getLastComments();
 				d = parseEnum();
 				if (d != null) {
 					d.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 					this.setPreComments(d, lastComments);
 					s = newDeclarationStatement(filename, lineNumber, d);
 				}
 			}
 			break;
 		}
 
 		case TOKmixin: {
 			Dsymbol d;
 			t = peek(token);
 		    if (t.value == TOKlparen)
 		    {
 		    	// mixin(string)
 		    	if (peekPastParen(t).value == TOKsemicolon) {
 		    		nextToken();
 					check(TOKlparen);
 					Expression e = parseAssignExp();
 					check(TOKrparen);
 					check(TOKsemicolon);
 					s = newCompileStatement(filename, lineNumber, e);
 		    	} else {
 		    		Expression e = parseAssignExp();
 			    	s = new ExpStatement(filename, lineNumber, e);
 		    	}
 				break;
 		    } else {			
 		    	d = parseMixin();
 		    	s = newDeclarationStatement(filename, lineNumber, d);
 		    	break;
 		    }
 		}
 
 		case TOKlcurly: {
 			Statements statements;
 			
 			expect(statementExpectations);
 			if (apiLevel >= D2) {
 				expect(traitsExpectations);
 			}
 
 			nextToken();
 			statements = new Statements();
 			
 			while (token.value != TOKrcurly) {
 				if (token.value == TOKeof) {
 					parsingErrorInsertTokenAfter(prevToken, "}");
 					break;
 				}
 				
 				Statement subStatement = parseStatement(PSsemi | PScurlyscope);
 				if (subStatement != null) {
 					statements.add(subStatement);
 				}
 			}
 			
 			s = newBlock(statements, start, token.ptr + token.sourceLen - start);
 			
 			if ((flags & (PSscope | PScurlyscope)) != 0) {
 				s = newScopeStatement(filename, lineNumber, s);
 				s.setSourceRange(start, token.ptr + token.sourceLen - start);
 			}
 			
 			discardLastComments();
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
 			
 			s = newWhileStatement(filename, lineNumber, condition2, body);
 			break;
 		}
 
 		case TOKsemicolon:
 			if ((flags & PSsemi) == 0) {
 				error(IProblem.UseBracesForAnEmptyStatement, token);
 			}
 			nextToken();
 			
 			s = newExpStatement(filename, lineNumber, null);
 			break;
 
 		case TOKdo: {
 			Statement body;
 			Expression condition2;
 			
 			nextToken();
 			
 			body = parseStatement(PSscope);
 			
 			expect(whileExpectations);
 			
 			check(TOKwhile);
 			check(TOKlparen);
 			condition2 = parseExpression();
 			check(TOKrparen);
 			s = newDoStatement(filename, lineNumber, body, condition2);
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
 			s = newForStatement(filename, lineNumber, init, condition2, increment, body);
 			if (init != null) {
 				s.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 				
 				s = newScopeStatement(filename, lineNumber, s);
 				s.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 			}
 			break;
 		}
 
 		case TOKforeach:
 		case TOKforeach_reverse:
 		{
 			TOK op = token.value;
 			Arguments arguments;
 
 			// Statement d; // <-- not used
 			Statement body;
 			Expression aggr;
 			
 			nextToken();
 			check(TOKlparen);
 
 			arguments = new Arguments(2);
 
 			while (true) {
 				Type tb;
 				IdentifierExp ai = null;
 				Type at;
 				int storageClass;
 				Argument a;
 				List<Modifier> modifiers = new ArrayList<Modifier>(1);
 				
 				int argumentStart = token.ptr;
 
 				if (apiLevel >= D2) {
 					storageClass = 0;
 				} else {
 					storageClass = STCin;	
 				}
 				if (token.value == TOKinout || token.value == TOKref) {
 					storageClass = STCref;
 					modifiers.add(newModifier());
 					nextToken();
 				}
 				if (token.value == TOKidentifier) {
 					Token t2 = peek(token);
 					if (t2.value == TOKcomma || t2.value == TOKsemicolon) {
 						ai = newIdentifierExp();
 						at = null; // infer argument type
 						nextToken();
 						// goto Larg;
 						a = new Argument(storageClass, at, ai, null);
 						setModifiers(a, modifiers);
 						a.setSourceRange(argumentStart, prevToken.ptr + prevToken.sourceLen - argumentStart);
 						arguments.add(a);
 						if (token.value == TOKcomma) {
 							nextToken();
 							continue;
 						}
 						break;
 					}
 				}
 				
 				int lineNumber = token.lineNumber;
 				
 				IdentifierExp[] pointer2_ai = { ai };
 				if (apiLevel < D2) {
 					tb = parseBasicType();
 					at = parseDeclarator(tb, pointer2_ai);
 				} else {
 					at = parseType(pointer2_ai);
 				}
 				ai = pointer2_ai[0];
 				
 				if (ai == null) {
 					if (at == null) {
 						error(IProblem.NoIdentifierForDeclarator, lineNumber, prevToken.ptr, prevToken.sourceLen);
 					} else {
 						error(IProblem.NoIdentifierForDeclarator, lineNumber, at);
 					}
 				}
 				// Larg:
 				if (at != null && ai != null) {
 					a = new Argument(storageClass, at, ai, null);
 					setModifiers(a, modifiers);
 					a.setSourceRange(argumentStart, prevToken.ptr + prevToken.sourceLen - argumentStart);
 					arguments.add(a);
 				}
 				if (token.value == TOKcomma) {
 					nextToken();
 					continue;
 				}
 				break;
 			}
 			check(TOKsemicolon);
 
 			aggr = parseExpression();
 			
 			if (apiLevel < D2) {
 				check(TOKrparen);
 				body = parseStatement(0);
 				
 				s = newForeachStatement(filename, lineNumber, op, arguments, aggr, body);
 			} else {
 			    if (token.value == TOKslice && arguments.size() == 1) {
 					Argument a = arguments.get(0);
 					nextToken();
 					Expression upr = parseExpression();
 					check(TOKrparen);
 					body = parseStatement(0);
 					s = newForeachRangeStatement(filename, lineNumber, op, a, aggr, upr, body);
 				} else {
 					check(TOKrparen);
 					body = parseStatement(0);
 					s = newForeachStatement(filename, lineNumber, op, arguments, aggr, body);
 				}
 			}
 			break;
 		}
 
 		case TOKif: {
 			Argument arg = null;
 			Expression condition2;
 			Statement ifbody2;
 			Statement elsebody2;
 			
 			nextToken();
 			check(TOKlparen);
 			
 			expect(autoExpectations);
 			if (token.value == TOKauto) {
 				int autoTokenStart = token.ptr;
 				List<Modifier> modifiers = new ArrayList<Modifier>(1);
 				modifiers.add(newModifier());
 				
 				nextToken();
 				if (token.value == TOKidentifier) {
 					Token t2 = peek(token);
 					if (t2.value == TOKassign) {
 						arg = new Argument(apiLevel >= D2 ? 0 : STCin, null, newIdentifierExp(), null);
 						setModifiers(arg, modifiers);
 						arg.setSourceRange(autoTokenStart, token.ptr + token.sourceLen - autoTokenStart);
 						
 						nextToken();
 						nextToken();
 					} else {
 						parsingErrorInsertTokenAfter(prevToken, "=");
 						// goto Lerror;
 						while (token.value != TOKrcurly
 								&& token.value != TOKsemicolon
 								&& token.value != TOKeof) {
 							nextToken();
 						}
 						if (token.value == TOKsemicolon) {
 							nextToken();
 						}
 						s = null;
 						break;
 					}
 				} else {
 					parsingErrorInsertTokenAfter(prevToken, "Identifier");
 					// goto Lerror;
 					while (token.value != TOKrcurly
 							&& token.value != TOKsemicolon
 							&& token.value != TOKeof) {
 						nextToken();
 					}
 					if (token.value == TOKsemicolon) {
 						nextToken();
 					}
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
 					
 					IdentifierExp[] pointer2_ai = { ai };
 					if (apiLevel < D2) {
 						tb = parseBasicType();
 						at = parseDeclarator(tb, pointer2_ai);
 					} else {
 						at = parseType(pointer2_ai);
 					}
 					ai = pointer2_ai[0];
 
 					arg = new Argument(apiLevel >= D2 ? 0 : STCin, at, ai, null);
 					arg.setSourceRange(argTokenStart, prevToken.ptr + prevToken.sourceLen - argTokenStart);
 					
 					check(TOKassign);					
 				}
 
 				// Check for " ident;"
 				else if (token.value == TOKidentifier) {
 					Token t2 = peek(token);
 					if (t2.value == TOKcomma || t2.value == TOKsemicolon) {
 						arg = new Argument(apiLevel >= D2 ? 0 : STCin, null, newIdentifierExp(), null);
 						arg.setSourceRange(argTokenStart, token.ptr + token.sourceLen - argTokenStart);
 						
 						nextToken();
 						nextToken();
 						
 						// if (!global.params.useDeprecated)
 						error(IProblem.IfAutoDeprecated, argTokenLine, argTokenStart, token.ptr + token.sourceLen - argTokenStart);
 					}
 				}
 			}
 
 			condition2 = parseExpression();
 			check(TOKrparen);
 			ifbody2 = parseStatement(PSscope);
 			if (token.value == TOKelse) {
 				nextToken();
 				elsebody2 = parseStatement(PSscope);
 			} else {
 				elsebody2 = null;
 			}
 			s = newIfStatement(filename, lineNumber, arg, condition2, ifbody2, elsebody2);
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
 			
 			expect(scopeArgsExpectations);
 			
 			if (token.value != TOKidentifier) {
 				parsingErrorInsertTokenAfter(prevToken, "Identifier");
 				// goto Lerror;
 				while (token.value != TOKrcurly && token.value != TOKsemicolon
 						&& token.value != TOKeof) {
 					nextToken();
 				}
 				if (token.value == TOKsemicolon) {
 					nextToken();
 				}
 				s = null;
 				break;
 			} else {
 				TOK t2 = TOKon_scope_exit;
 
 				// This char[] instances are reused by the Lexer
 				char[] id = token.sourceString;			
 				if (id == Id.exit) {
 					t2 = TOKon_scope_exit;
 				} else if (id == Id.failure) {
 					t2 = TOKon_scope_failure;
 				} else if (id == Id.success) {
 					t2 = TOKon_scope_success;
 				} else {
 					error(IProblem.InvalidScopeIdentifier, token);
 				}
 				nextToken();
 				check(TOKrparen);
 				Statement st = parseStatement(PScurlyscope);
 				
 				s = newOnScopeStatement(filename, lineNumber, t2, st);
 				break;
 			}
 
 		case TOKon_scope_exit:
 		case TOKon_scope_failure:
 		case TOKon_scope_success: {
 			
 			TOK t2 = token.value;
 			
 			// if (!global.params.useDeprecated)
 			error(IProblem.OnScopeDeprecated, token, new String[] { token.toString() });
 			nextToken();
 			Statement st = parseStatement(PScurlyscope);
 			
 			s = newOnScopeStatement(filename, lineNumber, t2, st);
 			break;
 		}
 
 		case TOKdebug:
 			nextToken();
 			
 			DebugCondition condition = parseDebugCondition();
 			
 			// goto Lcondition
 			ifbody = parseStatement(0 /*PSsemi*/);
 			elsebody = null;
 			
 			expect(elseExpectations);			
 			if (token.value == TOKelse) {
 				nextToken();
 				elsebody = parseStatement(0 /*PSsemi*/);
 			}
 			
 			s = newConditionalStatement(filename, lineNumber, condition, ifbody, elsebody);
 			break;
 
 		case TOKversion:
 			nextToken();
 			
 			VersionCondition versionCondition = parseVersionCondition();
 			
 			// goto Lcondition
 			ifbody = parseStatement(0 /*PSsemi*/);
 			elsebody = null;
 			
 			expect(elseExpectations);
 			if (token.value == TOKelse) {
 				nextToken();
 				elsebody = parseStatement(0 /*PSsemi*/);
 			}
 			
 			s = newConditionalStatement(filename, lineNumber, versionCondition, ifbody, elsebody);
 			break;
 
 		case TOKiftype:
 			IftypeCondition iftypeCondition = parseIftypeCondition();
 			
 			// goto Lcondition
 			ifbody = parseStatement(0 /*PSsemi*/);
 			elsebody = null;
 			
 			expect(elseExpectations);
 			if (token.value == TOKelse) {
 				nextToken();
 				elsebody = parseStatement(0 /*PSsemi*/);
 			}
 			
 			s = newConditionalStatement(filename, lineNumber, iftypeCondition, ifbody, elsebody);
 			break;
 
 		case TOKpragma: {
 			IdentifierExp ident;
 			Expressions args = null;
 			Statement body;
 			
 			nextToken();
 			check(TOKlparen);
 			
 			expect(pragmaArgsExpectations);
 			if (token.value != TOKidentifier) {
 				parsingErrorInsertTokenAfter(prevToken, "Identifier");
 				// goto Lerror;
 				while (token.value != TOKrcurly && token.value != TOKsemicolon
 						&& token.value != TOKeof) {
 					nextToken();
 				}
 				if (token.value == TOKsemicolon) {
 					nextToken();
 				}
 				s = null;
 				break;
 			}
 			ident = newIdentifierExp();
 			nextToken();
 			if (token.value == TOKcomma && peekNext() != TOKrparen) {
 				args = parseArguments(); // pragma(identifier, args...);
 			} else {
 				check(TOKrparen); // pragma(identifier);
 			}
 			if (token.value == TOKsemicolon) {
 				nextToken();
 				body = null;
 			} else {
 				body = parseStatement(PSsemi);
 			}
 			
 			s = newPragmaStatement(filename, lineNumber, ident, args, body);
 			break;
 		}
 
 		case TOKswitch: {
 			s = parseStatement_Lswitch(false);
 			break;
 		}
 
 		case TOKcase: {
 			Expression exp;
 			Statements statements;
 			// List cases = new ArrayList(); // array of Expression's
 			
 			// Note: this code was changed a little from DMD to support
 			// better code completion
 			List<CaseStatement> caseStatements = new ArrayList<CaseStatement>();
 			Expression last = null;
 
 			while (true) {
 				int caseEnd = token.ptr + token.sourceLen;
 				
 				nextToken();
 				
 				int expStart = token.ptr;
 				int expLength = token.sourceLen;
 				
 				exp = parseAssignExp();
 				caseStatements.add(newCaseStatement(filename, lineNumber, exp, null, caseEnd, expStart, expLength));			
 				// cases.add(exp);
 				
 				if (token.value != TOKcomma) {
 					break;
 				}
 			}
 			check(TOKcolon);
 			
 			if (apiLevel == D2) {
 				/*
 				 * case exp: .. case last:
 				 */
 				if (token.value == TOKslice) {
 					if (caseStatements.size() > 1) {
 						error(IProblem.OnlyOneCaseAllowedForStartOfCaseRange, lineNumber, token.ptr, token.sourceLen);
 					}
 					nextToken();
 					check(TOKcase);
 					last = parseAssignExp();
 					check(TOKcolon);
 				}
 			}
 
 			statements = new Statements();
 			while (token.value != TOKcase && token.value != TOKdefault
 					&& token.value != TOKrcurly) {
 				
 				if (token.value == TOKeof) {
 					break;
 				}
 				
 				statements.add(parseStatement(PSsemi | PScurlyscope));
 			}
 			
 			s = newBlock(statements, start, prevToken.ptr + prevToken.sourceLen - start);
 			
 			s = newScopeStatement(filename, lineNumber, s);
 			s.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 
 			if (last != null) {
 				s = newCaseRangeStatement(filename, lineNumber, exp, last, s);
 			} else {
 				// Keep cases in order by building the case statements backwards
 				for(int i = caseStatements.size(); i != 0; i--) {
 					CaseStatement cs = caseStatements.get(i - 1);
 					cs.setStatement(s);
 					s = cs;
 				}
 			}
 			
 			break;
 		}
 
 		case TOKdefault: {
 			Statements statements;
 			
 			nextToken();
 			check(TOKcolon);
 
 			statements = new Statements();
 			while (token.value != TOKcase && token.value != TOKdefault
 					&& token.value != TOKrcurly && token.value != TOKeof) {
 				statements.add(parseStatement(PSsemi | PScurlyscope));
 			}
 			
 			s = newBlock(statements, start, prevToken.ptr + prevToken.sourceLen - start);
 			
 			s = newScopeStatement(filename, lineNumber, s);
 			s.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 			
 			s = newDefaultStatement(filename, lineNumber, s);
 			break;
 		}
 
 		case TOKreturn: {
 			Expression exp;
 
 			nextToken();
 			if (token.value == TOKsemicolon) {
 				exp = null;
 			// Descent: added to improve autocompletion
 			} else if (token.value == TOKrcurly) {
 				exp = null;
 			} else {
 				exp = parseExpression();
 			}
 			s = newReturnStatement(filename, lineNumber, exp);
 			
 			check(TOKsemicolon);
 			break;
 		}
 
 		case TOKbreak: {
 			IdentifierExp ident;
 
 			nextToken();
 			if (token.value == TOKidentifier) {
 				ident = newIdentifierExp();
 				nextToken();
 			} else {
 				ident = null;
 			}
 			check(TOKsemicolon);
 			
 			s = newBreakStatement(filename, lineNumber, ident);
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
 			
 			s = newContinueStatement(filename, lineNumber, ident);
 			break;
 		}
 
 		case TOKgoto: {
 			IdentifierExp ident;
 			
 			nextToken();
 			if (token.value == TOKdefault) {
 				nextToken();
 				s = newGotoDefaultStatement(filename, lineNumber);
 			} else if (token.value == TOKcase) {
 				Expression exp = null;
 
 				nextToken();
 				if (token.value != TOKsemicolon) {
 					exp = parseExpression();
 				}
 				
 				s = newGotoCaseStatement(filename, lineNumber, exp);
 			} else {
 				if (token.value != TOKidentifier) {
 					parsingErrorInsertTokenAfter(prevToken, "Identifier");
 					ident = null;
 				} else {
 					ident = newIdentifierExp();
 					nextToken();
 				}
 				
 				s = newGotoStatement(filename, lineNumber, ident);
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
 			} else {
 				exp = null;
 			}
 			body = parseStatement(PSscope);
 			
 			s = newSynchronizedStatement(filename, lineNumber, exp, body);
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
 			
 			s = newWithStatement(filename, lineNumber, exp, body);
 			break;
 		}
 
 		case TOKtry: {
 			Statement body;
 			Array catches = null;
 			Statement finalbody = null;
 			
 			nextToken();
 			body = parseStatement(PSscope);
 			
 			expect(catchExpectations);
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
 					id = null;
 					IdentifierExp[] pointer2_id = { id };
 					if (apiLevel < D2) {
 						t2 = parseBasicType();
 						t2 = parseDeclarator(t2, pointer2_id);
 					} else {
 						t2 = parseType(pointer2_id);
 					}
 					id = pointer2_id[0];
 					check(TOKrparen);
 				}
 				handler = parseStatement(0);
 				
 				c = new Catch(filename, lineNumber, t2, id, handler);
 				c.setSourceRange(firstTokenStart, prevToken.ptr + prevToken.sourceLen - firstTokenStart);
 				
 				if (catches == null) {
 					catches = new Array();
 				}
 				
 				catches.add(c);
 			}
 
 			expect(finallyExpectations);
 			if (token.value == TOKfinally) {
 				nextToken();
 				finalbody = parseStatement(0);
 			}
 
 			s = body;
 			if (catches == null && finalbody == null) {
 				parsingErrorInsertToComplete(prevToken, "Catch or finally", "TryStatement");
 			} else {
 				if (catches != null) {
 					s = newTryCatchStatement(filename, lineNumber, body, catches);
 				}
 				if (finalbody != null) {
 					if (catches != null) {
 						s.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 					}
 					s = newTryFinallyStatement(filename, lineNumber, s, finalbody, catches != null);
 				}
 			}
 			break;
 		}
 
 		case TOKthrow: {
 			Expression exp;
 			
 			nextToken();
 			exp = parseExpression();
 			check(TOKsemicolon);
 			
 			s = newThrowStatement(filename, lineNumber, exp);
 			break;
 		}
 
 		case TOKvolatile:
 			nextToken();
 			s = parseStatement(PSsemi | PScurlyscope);
 			// TODO Descent parser use deprecated
 //		    if (!global.params.useDeprecated)
 //				error("volatile statements deprecated; used synchronized statements instead");
 			s = newVolatileStatement(filename, lineNumber, s);
 			break;
 
 		case TOKasm: {
 			Statements statements;
 			IdentifierExp label;
 			List<Token> toklist = new ArrayList<Token>(6);
 			
 			// Parse the asm block into a sequence of AsmStatements,
 			// each AsmStatement is one instruction.
 			// Separate out labels.
 			// Defer parsing of AsmStatements until semantic processing.
 
 			nextToken();
 			check(TOKlcurly);
 			label = null;
 			statements = new Statements();
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
 					s = new AsmStatement(filename, lineNumber, toklist);
 					if (toklist.isEmpty()) {
 						s.setSourceRange(token.ptr, token.sourceLen);
 					} else {
 						s.setSourceRange(toklist.get(0).ptr, token.ptr + token.sourceLen - toklist.get(0).ptr);
 					}
 					
 					toklist = new ArrayList<Token>(6);
 					
 					if (label != null) {
 						s = newLabelStatement(filename, lineNumber, label, s);
 						s.start = label.start;
 						s.length = token.ptr + token.sourceLen - label.start;
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
 			
 			s = new AsmBlock(filename, lineNumber, statements);;
 			
 			nextToken();
 			break;
 		}
 
 		default:
 			parsingErrorInsertTokenAfter(prevToken, "Statement");
 			// goto Lerror;
 
 			// Lerror:
 			while (token.value != TOKrcurly && token.value != TOKsemicolon
 					&& token.value != TOKeof) {
 				nextToken();
 			}
 			if (token.value == TOKsemicolon) {
 				nextToken();
 			}
 			s = null;
 			break;
 		}
 		
 		if (s != null) {
 			s.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 		}
 		
 		return s;
 	}	
 
 	private Statement parseStatement_Lswitch(boolean isfinal) {
 		Expression condition2;
 		Statement body;
 
 		nextToken();
 		check(TOKlparen);
 		condition2 = parseExpression();
 		check(TOKrparen);
 		body = parseStatement(PSscope);
 		
 		return newSwitchStatement(filename, lineNumber, condition2, body, isfinal);
 	}
 
 	private void parseStatement_Ldeclaration(Statement[] s, int flags) {
 		List<Comment> lastComments = getLastComments();
 		
 		List a = parseDeclarations(new ArrayList<Comment>());
 		if (a.size() > 1) {
 			Statements as = new Statements(a.size());
 			for (int i = 0; i < a.size(); i++) {
 				Dsymbol d = (Dsymbol) a.get(i);
 				this.setPreComments(d, lastComments);
 				s[0] = newDeclarationStatement(filename, lineNumber, d);
 				as.add(s[0]);
 			}
 			
 			s[0] = newManyVarsBlock(as);
 		} else if (a.size() == 1) {
 			Dsymbol d = (Dsymbol) a.get(0);
 			this.setPreComments(d, lastComments);
 			s[0] = newDeclarationStatement(filename, lineNumber, d);
 		} else {
 			parsingErrorDeleteToken(token);
 			nextToken();
 			s[0] = null;
 		}
 		if ((flags & PSscope) != 0) {
 			s[0] = newScopeStatement(filename, lineNumber, s[0]);
 		}
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
 		
 		if (apiLevel >= D2) {
 		    if ((t.value == TOKconst || 
 		    	t.value == TOKinvariant || 
 		    	t.value == TOKimmutable || 
 		    	t.value == TOKshared)
 					&& peek(t).value != TOKlparen) {
 		    /* const type
 			 * invariant type
 			 * shared type
 			 */
 				t = peek(t);
 			}
 		}
 
 
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
 			t = peek(t);
 			break;
 
 		case TOKidentifier: {
 			return isBasicType_L5(t, pt);
 		}
 
 		case TOKdot: {
 			// goto Ldot;
 			Token[] pointer2_t2 = new Token[] { t };
 			boolean semiResult = isBasicType_Ldot(pointer2_t2);
 			t = pointer2_t2[0];
 			if (semiResult) {
 				pt[0] = t;
 			}
 			return semiResult;
 		}
 
 		case TOKtypeof: {
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
 			boolean semiResult = isBasicType_L2(pointer2_t);
 			t = pointer2_t[0];
 			if (semiResult) {
 				pt[0] = t;
 			}
 			return semiResult;
 		}
 			
 		case TOKconst:
 		case TOKinvariant:
 		case TOKimmutable:
 		case TOKshared:
 		{
 			if (apiLevel < D2) {
 				return false;
 			}
 			
 		    // const(type)  or  invariant(type)
 		    t = peek(t);
 		    if (t.value != TOKlparen) {
 		    	return false;
 		    }
 		    t = peek(t);
 		    
 		    Token[] pointer2_t = { t };
 		    if (!isDeclaration(t, 0, TOKrparen, pointer2_t)) {
 		    	return false;
 		    }
 		    t = pointer2_t[0];
 		    
 		    t = peek(t);
 		    break;
 		}
 
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
 				if (apiLevel < D2) {
 					pt[0] = peek(pt[0]);
 					if (pt[0].value != TOKlparen) {
 						return false;
 					}
 	
 					if (!skipParens(pt[0], pt)) {
 						return false;
 					}
 				} else {
 				    /* Seen a !
 				     * Look for:
 				     * !( args ), !identifier, etc.
 				     */
 				    pt[0] = peek(pt[0]);
 				    switch (pt[0].value)
 				    {	case TOKidentifier:
 					    	// goto L5;
 				    		return isBasicType_L5(pt[0], pt);
 					case TOKlparen:
 					    if (!skipParens(pt[0], pt)) {
 					    	// goto Lfalse;
 					    	return false;
 					    }
 					    break;
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
 					case TOKfile:
 					case TOKline:
 					    // goto L2;
 						return isBasicType_L2(pt);
 					default:
 					    // goto Lfalse;
 						return false;
 				    }
 				}
 			} else {
 				break;
 			}
 		}
 		
 		return true;
 	}
 	
 	private boolean isBasicType_L5(Token t, Token[] pt) {
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
 	}
 	
 	private boolean isDeclarator(Token[] pt, int[] haveId, TOK endtok) {
 		// This code parallels parseDeclarator()
 	    Token t = pt[0];
 	    int parens;
 
 	    if (t.value == TOKassign) {
 			return false;
 		}
 
 	    while (true)
 	    {
 		parens = 0;
 		switch (t.value)
 		{
 		    case TOKmul:
 //		    case TOKand:
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
 			if (haveId[0] != 0) {
 				return false;
 			}
 			haveId[0] = 1;
 			t = peek(t);
 			break;
 
 		    case TOKlparen:
 			t = peek(t);
 
 			if (t.value == TOKrparen) {
 				return false;		// () is not a declarator
 			}
 
 			/* Regard ( identifier ) as not a declarator
 			 * BUG: what about ( *identifier ) in
 			 *	f(*p)(x);
 			 * where f is a class instance with overloaded () ?
 			 * Should we just disallow C-style function pointer declarations?
 			 */
 			if (t.value == TOKidentifier)
 			{   Token t2 = peek(t);
 			    if (t2.value == TOKrparen) {
 					return false;
 				}
 			}
 
 			Token[] pointer2_t = { t };
 			if (!isDeclarator(pointer2_t, haveId, TOKrparen)) {
 				return false;
 			}
 			
 			t = pointer2_t[0];
 			
 			t = peek(t);
 			parens = 1;
 			break;
 
 		    case TOKdelegate:
 		    case TOKfunction:
 			t = peek(t);
 			
 			pointer2_t = new Token[] { t };
 			
 			if (!isParameters(pointer2_t)) {
 				return false;
 			}
 			
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
 				if (t.value == TOKrbracket) {
 					t = peek(t);
 				} else {
 					Token[] pointer2_t = { t };
 					if (isDeclaration(t, 0, TOKrbracket, pointer2_t)) { // It's
 																		// an
 																		// associative
 																		// array
 																		// declaration
 						t = pointer2_t[0];
 						t = peek(t);
 					} else {
 						t = pointer2_t[0];
 
 						// [ expression ]
 						if (!isExpression(pointer2_t)) {
 							return false;
 						}
 
 						t = pointer2_t[0];
 
 						if (t.value != TOKrbracket) {
 							return false;
 						}
 						t = peek(t);
 					}
 				}
 				continue;
 	// #endif
 
 		    case TOKlparen:
 				parens = 0;
 
 				Token[] pointer2_t = { t };
 				if (!isParameters(pointer2_t)) {
 					return false;
 				}
 
 				if (apiLevel >= D2) {
 					while (true) {
 						switch (t.value) {
 						case TOKconst:
 						case TOKinvariant:
 						case TOKimmutable:
 						case TOKshared:
 						case TOKpure:
 						case TOKnothrow:
 							t = peek(t);
 							continue;
 						default:
 							break;
 						}
 						break;
 					}
 				}
 
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
 	
 	private boolean isParameters(Token[] pt) {
 		if (apiLevel < D2) {
 			return isParametersD1(pt);
 		} else {
 			return isParametersD2(pt);
 		}
 	}
 	
 	private boolean isParametersD1(Token[] pt) {
 		// This code parallels parseParameters()
 		Token t = pt[0];
 		int tmp;
 
 		if (t.value != TOKlparen) {
 			return false;
 		}
 
 		t = peek(t);
 		while (true) {
 			switch (t.value) {
 			case TOKrparen:
 				break;
 
 			case TOKdotdotdot:
 				t = peek(t);
 				break;
 
 			case TOKin:
 			case TOKout:
 			case TOKinout:
 			case TOKref:
 			case TOKlazy:
 				t = peek(t);
 			default:
 
 				Token[] pointer2_t = { t };
 				if (!isBasicType(pointer2_t)) {
 					return false;
 				}
 
 				t = pointer2_t[0];
 
 				tmp = 0;
 
 				int[] pointer2_tmp = { tmp };
 
 				if (t.value != TOKdotdotdot
 						&& !isDeclarator(pointer2_t, pointer2_tmp, TOKreserved)) {
 					return false;
 				}
 
 				t = pointer2_t[0];
 				tmp = pointer2_tmp[0];
 
 				if (t.value == TOKassign) {
 					t = peek(t);
 					pointer2_t[0] = t;
 					if (!isExpression(pointer2_t)) {
 						return false;
 					}
 
 					t = pointer2_t[0];
 				}
 				if (t.value == TOKdotdotdot) {
 					t = peek(t);
 					break;
 				}
 				if (t.value == TOKcomma) {
 					t = peek(t);
 					continue;
 				}
 				break;
 			}
 			break;
 		}
 		if (t.value != TOKrparen) {
 			return false;
 		}
 		t = peek(t);
 		pt[0] = t;
 		return true;
 	}
 	
 	private boolean isParametersD2(Token[] pt) {
 		// This code parallels parseParameters()
 		Token t = pt[0];
 		int tmp;
 		boolean gotoL2 = false;
 
 		if (t.value != TOKlparen) {
 			return false;
 		}
 
 		t = peek(t);
 	loop:
 		for(; true; t = peek(t)) {
 			
 			boolean repeat = true;
 			while(repeat) {
 				repeat = false;
 				switch (t.value) {
 				case TOKrparen:
 					break;
 	
 				case TOKdotdotdot:
 					t = peek(t);
 					break;
 	
 				case TOKin:
 				case TOKout:
 				case TOKinout:
 				case TOKref:
 				case TOKlazy:
 				case TOKfinal:
 					continue loop;
 					
 				case TOKconst:
 				case TOKinvariant:
 				case TOKimmutable:
 				case TOKshared:
 					t = peek(t);
 					if (t.value == TOKlparen)
 					{
 					    t = peek(t);
 					    Token[] pt2 = { t };
 					    if (!isDeclaration(t, 0, TOKrparen, pt2)) {
 					    	return false;
 					    }
 					    t = pt2[0];
 					    t = peek(t);	// skip past closing ')'
 					    // goto L2;
 					    gotoL2 = true;
 					}
 					if (!gotoL2) {
 						//goto L1;
 						repeat = true;
 						continue;
 					}
 					
 				default:
 					
 					Token[] pointer2_t = { t };
 	
 					if (!gotoL2) {
 						if (!isBasicType(pointer2_t)) {
 							return false;
 						}
 		
 						t = pointer2_t[0];
 					}
 	
 					tmp = 0;
 	
 					int[] pointer2_tmp = { tmp };
 	
 					if (t.value != TOKdotdotdot
 							&& !isDeclarator(pointer2_t, pointer2_tmp, TOKreserved)) {
 						return false;
 					}
 	
 					t = pointer2_t[0];
 					tmp = pointer2_tmp[0];
 	
 					if (t.value == TOKassign) {
 						t = peek(t);
 						pointer2_t[0] = t;
 						if (!isExpression(pointer2_t)) {
 							return false;
 						}
 	
 						t = pointer2_t[0];
 					}
 					if (t.value == TOKdotdotdot) {
 						t = peek(t);
 						break;
 					}
 					if (t.value == TOKcomma) {
 						continue loop;
 					}
 					break;
 				}
 			}
 			break;
 		}
 		if (t.value != TOKrparen) {
 			return false;
 		}
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
 	    int curlynest = 0;
 
 	    for (;; t = peek(t))
 	    {
 		switch (t.value)
 		{
 		    case TOKlbracket:
 			brnest++;
 			continue;
 
 		    case TOKrbracket:
 			if (--brnest >= 0) {
 				continue;
 			}
 			break;
 
 		    case TOKlparen:
 			panest++;
 			continue;
 
 		    case TOKcomma:
 			if (brnest != 0 || panest != 0) {
 				continue;
 			}
 			break;
 
 		    case TOKrparen:
 			if (--panest >= 0) {
 				continue;
 			}
 			break;
 			
 		    case TOKlcurly:
 		    	if (apiLevel >= D2) {
 		    		curlynest++;
 		    	}
 		    	continue;
 
 		    case TOKrcurly:
 		    	if (apiLevel >= D2) {
 					if (--curlynest >= 0) {
 						continue;
 					}
 					return false;
 		    	} else {
 		    		continue;
 		    	}
 		    case TOKslice:
 			if (brnest != 0) {
 				continue;
 			}
 			break;
 			
 		    case TOKsemicolon:
 		    	if (apiLevel >= D2) {
 					if (curlynest != 0) {
 						continue;
 					}
 					return false;
 		    	} else {
 		    		continue;
 		    	}
 
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
 			if (parens < 0) {
 				//goto Lfalse;
 				return false;
 			}
 			if (parens == 0) {
 			    //goto Ldone;
 				if (pt[0] != null) {
 					pt[0] = t;
 				}
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
 
 	    expect(primaryExpExpectations);	    
 	    if (apiLevel >= D2) {
 	    	expect(traitsExpectations);
 	    }
 	    
 	    switch (token.value)
 	    {
 		case TOKidentifier:
 		    id = newIdentifierExp();
 		    nextToken();
 		    
 		    boolean condition = apiLevel < D2 ?
 		    	token.value == TOKnot && peek(token).value == TOKlparen
 		    :
 			    token.value == TOKnot && peekNext() != TOKis;
 		    
 		    if (condition)
 		    {	// identifier!(template-argument-list)
 		    	TemplateInstance tempinst;
 		    	
 		    	tempinst = newTemplateInstance(filename, lineNumber, id, encoder);		    	
 		    	nextToken();
 		    	if (apiLevel < D2) {
 		    		tempinst.tiargs(parseTemplateArgumentList());
 		    	} else {
 		    		if (token.value == TOKlparen) {
 		    		    // ident!(template_arguments)
 		    		    tempinst.tiargs(parseTemplateArgumentList());
 		    		} else {
 		    		    // ident!template_argument
 		    		    tempinst.tiargs(parseTemplateArgument());
 		    		}
 		    	}
 		    	tempinst.setSourceRange(id.start, prevToken.ptr + prevToken.sourceLen - id.start);
 				e = new ScopeExp(filename, lineNumber, tempinst);
 				e.setSourceRange(tempinst.start, tempinst.length);
 		    }
 		    else {
 		    	e = id;
 		    }
 		    break;
 
 		case TOKdollar:
 		    if (inBrackets == 0) {
 		    	error(IProblem.DollarInvalidOutsideBrackets, token);
 		    }
 		    e = new DollarExp(filename, lineNumber);
 		    e.setSourceRange(token.ptr, token.sourceLen);
 		    nextToken();
 		    break;
 
 		case TOKdot:
 		    // Signal global scope '.' operator with "" identifier
 			e = new IdentifierExp(filename, lineNumber, Id.empty);
 			e.start = token.ptr;
 			e.length = 0;
 		    break;
 
 		case TOKthis:
 		    e = newThisExp(filename, lineNumber);
 		    e.setSourceRange(token.ptr, token.sourceLen);
 		    nextToken();
 		    break;
 
 		case TOKsuper:
 			e = newSuperExp(filename, lineNumber);
 		    e.setSourceRange(token.ptr, token.sourceLen);
 		    nextToken();
 		    break;
 
 		case TOKint32v: e = new IntegerExp(filename, lineNumber, token.sourceString, token.intValue, Type.tint32); e.setSourceRange(token.ptr, token.sourceLen); nextToken(); break;
 		case TOKuns32v: e = new IntegerExp(filename, lineNumber, token.sourceString, token.intValue, Type.tuns32); e.setSourceRange(token.ptr, token.sourceLen); nextToken(); break;
 		case TOKint64v: e = new IntegerExp(filename, lineNumber, token.sourceString, token.intValue, Type.tint64); e.setSourceRange(token.ptr, token.sourceLen); nextToken(); break;
 		case TOKuns64v: e = new IntegerExp(filename, lineNumber, token.sourceString, token.intValue, Type.tuns64); e.setSourceRange(token.ptr, token.sourceLen); nextToken(); break;
 		case TOKfloat32v: e = new RealExp(filename, lineNumber, token.sourceString, token.floatValue, Type.tfloat32); e.setSourceRange(token.ptr, token.sourceLen); nextToken(); break;
 		case TOKfloat64v: e = new RealExp(filename, lineNumber, token.sourceString, token.floatValue, Type.tfloat64); e.setSourceRange(token.ptr, token.sourceLen); nextToken(); break;
 		case TOKfloat80v: e = new RealExp(filename, lineNumber, token.sourceString, token.floatValue, Type.tfloat80); e.setSourceRange(token.ptr, token.sourceLen); nextToken(); break;
 		case TOKimaginary32v: e = new RealExp(filename, lineNumber, token.sourceString, token.floatValue, Type.timaginary32); e.setSourceRange(token.ptr, token.sourceLen); nextToken(); break;
 		case TOKimaginary64v: e = new RealExp(filename, lineNumber, token.sourceString, token.floatValue, Type.timaginary64); e.setSourceRange(token.ptr, token.sourceLen); nextToken(); break;
 		case TOKimaginary80v: e = new RealExp(filename, lineNumber, token.sourceString, token.floatValue, Type.timaginary80); e.setSourceRange(token.ptr, token.sourceLen); nextToken(); break;
 
 		case TOKnull:
 		    e = new NullExp(filename, lineNumber);
 		    e.setSourceRange(token.ptr, token.sourceLen);
 		    nextToken();
 		    break;
 		    
 		case TOKfile: {
 			// Id.empty might happen if we are decoding an expression.
 			// TODO fix this (low priority)
 
 			/* BM DDT BUGFIX: added null check for module.ident
 			 * The false condition here shows the original code. */
 			if(false) {
 			char[] s = filename != null ? filename : (module != null ? module.ident.ident : Id.empty);
 			}
 			char[] s = filename != null ? filename : (module != null && module.ident != null? module.ident.ident : Id.empty);
 			e = new StringExp(filename, lineNumber, s, s.length, (char) 0);
 			nextToken();
 			break;
 		}
 
 		case TOKline:
 			e = new IntegerExp(filename, lineNumber, filename, lineNumber, Type.tint32);
 			nextToken();
 			break;
 
 
 		case TOKtrue:
 			e = new IntegerExp(filename, lineNumber, token.sourceString, 1, Type.tbool);
 		    nextToken();
 		    break;
 		case TOKfalse:
 			e = new IntegerExp(filename, lineNumber, token.sourceString, 0, Type.tbool);
 		    nextToken();
 		    break;
 
 		case TOKcharv: e = new IntegerExp(filename, lineNumber, token.sourceString, token.intValue, Type.tchar); e.setSourceRange(token.ptr, token.sourceLen); nextToken(); break;
 		case TOKwcharv:  e = new IntegerExp(filename, lineNumber, token.sourceString, token.intValue, Type.twchar); e.setSourceRange(token.ptr, token.sourceLen); nextToken(); break;
 		case TOKdcharv:  e = new IntegerExp(filename, lineNumber, token.sourceString, token.intValue, Type.tdchar); e.setSourceRange(token.ptr, token.sourceLen); nextToken(); break;
 
 		case TOKstring: {
 			int startLine = token.lineNumber;
 			
 			List<StringExp> nextStringExps = null;
 			StringExp stringExp = newStringExpForCurrentToken();
 			StringExp lastStringExp = stringExp;
 			
 			char[] s = token.ustring;
 			int len = token.len;
 			int postfix;
 
 			// cat adjacent strings
 			postfix = token.postfix;
 			while (true) {
 				nextToken();
 				if (token.value == TOKstring) {
 					int len1;
 				    int len2;
 					
 					if (token.postfix != 0) {
 						if (token.postfix != postfix) {
 							error(
 									IProblem.MismatchedStringLiteralPostfixes,
 									startLine, lastStringExp.start, token.ptr + token.sourceLen - lastStringExp.start,
 									new String[] { String.valueOf((char) postfix) , String.valueOf((char) token.postfix) });
 						}							
 						postfix = token.postfix;
 					}
 
 				    len1 = len;
 				    len2 = token.len;
 				    len = len1 + len2;
 				    s = CharOperation.concat(s, token.ustring);
 
 					if (token.ustring != null) {
 						if (nextStringExps == null) {
 							nextStringExps = new ArrayList<StringExp>();
 							nextStringExps.add(newStringExpForPreviousToken());
 						}
 						lastStringExp = newStringExpForCurrentToken();
 						nextStringExps.add(lastStringExp);
 					}
 				} else {
 					break;
 				}
 			}
 			
 			stringExp.allStringExps = nextStringExps;
 			stringExp.string = s;
 			stringExp.len = len;
 			e = stringExp;
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
 			    	// Change from DMD
 					e = newTypeDotIdExp(filename, lineNumber, t, new IdentifierExp(Id.empty));
 			    	//e = new IntegerExp(filename, lineNumber, Id.ZERO, 0, Type.tint32);
 			    	e.setSourceRange(token.ptr, token.sourceLen);
 		    		nextToken();
 		    		break;
 			    }
 			    e = newTypeDotIdExp(filename, lineNumber, t, newIdentifierExp());
 			    e.setSourceRange(t.start, token.ptr + token.sourceLen - t.start);
 			    nextToken();
 			    break;
 
 		case TOKtypeof:
 		{   
 			if (apiLevel >= D2) {
 				t = parseTypeof();
 			} else {
 				Expression exp;
 				int start = token.ptr;
 	
 			    nextToken();
 			    check(TOKlparen);
 			    exp = parseExpression();
 			    check(TOKrparen);
 			    
 				t = new TypeTypeof(filename, lineNumber, exp, encoder);
 				t.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 				((TypeTypeof) t).setTypeofSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 			}
 			
 		    if (token.value == TOKdot) {
 		    	// goto L1;
 		    	check(TOKdot);
 			    if (token.value != TOKidentifier)
 			    {   
 			    	parsingErrorInsertTokenAfter(prevToken, "Identifier");
 					// goto Lerr;
 			    	// Anything for e, as long as it's not NULL
 			    	// Change from DMD
 					e = newTypeDotIdExp(filename, lineNumber, t, new IdentifierExp(Id.empty));
 			    	//e = new IntegerExp(filename, lineNumber, Id.ZERO, 0, Type.tint32);
 			    	e.setSourceRange(token.ptr, token.sourceLen);
 			    	nextToken();
 			    	break;
 			    }
 			    e = newTypeDotIdExp(filename, lineNumber, t, newIdentifierExp());
 			    e.setSourceRange(t.start, token.ptr + token.sourceLen - t.start);
 			    nextToken();
 			    break;
 		    }
 		    	
 		    e = new TypeExp(filename, lineNumber, t);
 		    break;
 		}
 
 		case TOKtypeid:
 		{   Type t2;
 			int start = token.ptr;
 
 		    nextToken();
 		    check(TOKlparen);
 		    if (apiLevel < D2) {
 			    t2 = parseBasicType();
 			    t2 = parseDeclarator(t2, null);	// ( type )
 		    } else {
 		    	t2 = parseType();				// ( type 
 		    }
 		    check(TOKrparen);
 		    e = new TypeidExp(filename, lineNumber, t2);
 		    e.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 		    break;
 		}
 		
 		case TOKtraits:
 		{   
 			/* __traits(identifier, args...)
 			 */
 			IdentifierExp ident;
 			Objects args = null;
 
 			nextToken();
 			check(TOKlparen);
 			
 			expect(traitsArgsExpectations);
 			
 			if (token.value != TOKidentifier) {
 				parsingErrorInsertToComplete(prevToken, "__traits(identifier, args...) expected", "traits expression");
 				//goto Lerr;
 				// Anything for e, as long as it's not NULL
 				e = new IntegerExp(filename, lineNumber, Id.ZERO, 0, Type.tint32);
 		    	e.setSourceRange(token.ptr, token.sourceLen);
 				nextToken();
 				break;
 			}
 			ident = newIdentifierExp();
 			nextToken();
 			if (token.value == TOKcomma) {
 				args = parseTemplateArgumentList2(); // __traits(identifier, args...)
 			} else {
 				check(TOKrparen); // __traits(identifier)
 			}
 
 			e = new TraitsExp(filename, lineNumber, ident, args);
 			break;
 		}
 
 		case TOKis:
 		{
 			Type targ = null;
 			IdentifierExp ident = null;
 			Type tspec = null;
 			TOK tok = TOKreserved;
 			TOK tok2 = TOKreserved;
 		    TemplateParameters tpl = null;
 		    int lineNumber = this.lineNumber;
 
 			nextToken();
 			if (token.value == TOKlparen) {
 				nextToken();
 
 				IdentifierExp[] pointer2_ident = { ident };
 				if (apiLevel < D2) {
 					targ = parseBasicType();
 					targ = parseDeclarator(targ, pointer2_ident);
 				} else {
 					targ = parseType(pointer2_ident);
 				}
 				ident = pointer2_ident[0];
 
 				if (token.value == TOKcolon || token.value == TOKequal) {
 					tok = token.value;
 					nextToken();
 					
 					
 					
 					if (tok == TOKequal
 							&& (token.value == TOKtypedef
 									|| token.value == TOKstruct
 									|| token.value == TOKunion
 									|| token.value == TOKclass
 									|| token.value == TOKsuper
 									|| token.value == TOKenum
 									|| token.value == TOKinterface
 									|| (apiLevel >= D2 && token.value == TOKconst && peek(token).value == TOKrparen)
 									|| (apiLevel >= D2 && token.value == TOKinvariant && peek(token).value == TOKrparen)
 									|| (apiLevel >= D2 && token.value == TOKimmutable && peek(token).value == TOKrparen)
 									|| (apiLevel >= D2 && token.value == TOKshared && peek(token).value == TOKrparen)
 									|| token.value == TOKfunction
 									|| token.value == TOKdelegate || token.value == TOKreturn)) {
 						tok2 = token.value;
 						nextToken();
 					} else {
 						if (apiLevel < D2) {
 							tspec = parseBasicType();
 							tspec = parseDeclarator(tspec, null);
 						} else {
 							tspec = parseType();
 						}
 					}
 				}
 				
 				if (apiLevel >= D2 && ident != null && tspec != null) {
 					if (token.value == TOKcomma)
 						tpl = parseTemplateParameterList(1);
 					else {
 						tpl = new TemplateParameters(1);
 						check(TOKrparen);
 					}
 					TemplateParameter tp = new TemplateTypeParameter(filename, lineNumber,
 							ident, null, null);
 					tpl.add(0, tp);
 				} else {
 					check(TOKrparen);
 				}
 			} else {
 				parsingErrorInsertToComplete(prevToken, "(type identifier : specialization)", "IftypeDeclaration");
 				// goto Lerr;
 				// Anything for e, as long as it's not NULL
 				e = new IntegerExp(filename, lineNumber, Id.ZERO, 0, Type.tint32);
 		    	e.setSourceRange(token.ptr, token.sourceLen);
 				nextToken();
 				break;
 			}
 			if (apiLevel >= D2) {
 				e = new IsExp(filename, lineNumber, targ, ident, tok, tspec, tok2, tpl);
 			} else {
 				e = new IsExp(filename, lineNumber, targ, ident, tok, tspec, tok2);
 			}
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
 			
 			e = new AssertExp(filename, lineNumber, e, msg);
 			e.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 			break;
 		}
 		
 		case TOKmixin:
 		{
 			int start = token.ptr;
 		    nextToken();
 		    check(TOKlparen);
 		    e = parseAssignExp();
 		    check(TOKrparen);
 		    e = new CompileExp(filename, lineNumber, e);
 		    e.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 		    break;
 		}
 
 		case TOKimport:
 		{
 			int start = token.ptr;
 		    nextToken();
 		    check(TOKlparen);
 		    e = parseAssignExp();
 		    check(TOKrparen);
 		    e = new FileExp(filename, lineNumber, e);
 		    e.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 		    break;
 		}
 
 
 		case TOKlparen:
 		    if (peekPastParen(token).value == TOKlcurly) { // (arguments) {
 															// statements... }
 				save = TOKdelegate;
 				// goto case_delegate;
 				{
 				Expression[] pe = { e };
 				parsePrimaryExp_case_delegate(pe, save, true /* empty syntax */);
 				e = pe[0];
 				}
 				break;
 			}
 			// ( expression )
 			int start = token.ptr;
 			nextToken();
 			e = parseExpression();
 
 			int end = token.ptr + token.sourceLen;
 			check(TOKrparen);
 
 			e.addParenthesis(start, end - start);
 			break;
 
 		case TOKlbracket:
 		{   
 			/* Parse array literals and associative array literals:
 		     *	[ value, value, value ... ]
 		     *	[ key:value, key:value, key:value ... ]
 		     */
 			Expressions values = new Expressions();
 			Expressions keys = null;
 
 			nextToken();
 			if (token.value != TOKrbracket) {
 				while (token.value != TOK.TOKeof) {
 					Expression e2 = parseAssignExp();
 					if (e2 instanceof ErrorExp) {
 						break;
 					}
 					
 					if (token.value == TOKcolon
 							&& (keys != null || values.size() == 0)) {
 						nextToken();
 						if (keys == null) {
 							keys = new Expressions(1);
 						}
 						keys.add(e2);
 						e2 = parseAssignExp();
 					} else if (keys != null) {
 						parsingErrorInsertToComplete(token, "key:value", "associative array literal");
 						keys = null;
 					}
 					values.add(e2);
 					if (token.value == TOKrbracket) {
 						break;
 					}
 					check(TOKcomma);
 				}
 			}
 			check(TOKrbracket);
 
 			if (keys != null) {
 				e = new AssocArrayLiteralExp(filename, lineNumber, keys, values);
 			} else {
 				e = new ArrayLiteralExp(filename, lineNumber, values);
 			}
 		    break;
 		}
 		
 		case TOKlcurly:
 		    // { statements... }
 			save = TOKdelegate;
 			// goto case_delegate;
 			{
 			Expression[] pe = { e };
 			parsePrimaryExp_case_delegate(pe, save, true /* empty syntax */);
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
 				parsePrimaryExp_case_delegate(pe, save, false /* not empty syntax */);
 				e = pe[0];
 				break;
 			}
 
 		default:
 			parsingErrorInsertTokenAfter(prevToken, "Expression");
 		// Lerr:
 		    // Anything for e, as long as it's not NULL
 			e = new ErrorExp();
 	    	e.setSourceRange(token.ptr, token.sourceLen);
 		    nextToken();
 		    break;
 	    }
 	    
 	    if (apiLevel < D2) {
 	    	return parsePostExp(e);
 	    } else {
 	    	return e;
 	    }
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
 					
 					boolean condition = apiLevel < D2 ?
 							token.value == TOKnot && peek(token).value == TOKlparen
 						:
 						    token.value == TOKnot && peekNext() != TOKis;
 					if (condition) { 
 						// identifier!(template-argument-list)
 						TemplateInstance tempinst;
 						
 						tempinst = newTemplateInstance(filename, lineNumber, id, encoder);						
 						nextToken();
 
 						if (apiLevel < D2) {
 							tempinst.tiargs(parseTemplateArgumentList());
 						} else {
 							if (token.value == TOKlparen) {
 							    // ident!(template_arguments)
 							    tempinst.tiargs(parseTemplateArgumentList());
 							} else {
 							    // ident!template_argument
 							    tempinst.tiargs(parseTemplateArgument());
 							}
 						}
 						tempinst.setSourceRange(id.start, prevToken.ptr + prevToken.sourceLen - id.start);
 						e = new DotTemplateInstanceExp(filename, lineNumber, e, tempinst);
 					} else {
 						e = newDotIdExp(filename, lineNumber, e, id);
 						e.start = start;
 						e.length = id.start + id.length - start;
 					}
 					continue;
 				} else if (token.value == TOKnew) {
 					e = parseNewExp(e);
 					
 					// Descent: If it was only foo.new and nothing else,
 					// treat this as a DotIdExp (for autocompletion)
 					if (prevToken.value == TOK.TOKnew) {
 						IdentifierExp fakeId = new IdentifierExp(prevToken.value.charArrayValue);
 						fakeId.start = prevToken.ptr;
 						fakeId.length = prevToken.sourceLen;
 						
 						e = newDotIdExp(filename, lineNumber, ((NewExp) e).sourceThisexp, fakeId);
 					}
 					continue;
 				} else {
 					// signal a new DotIdExp anyway with the token's string representation
 					IdentifierExp fakeId = new IdentifierExp(token.value.charArrayValue);
 					fakeId.start = token.ptr;
 					fakeId.length = token.sourceLen;
 					
 					e = newDotIdExp(filename, lineNumber, e, fakeId);
 					
 					parsingErrorInsertTokenAfter(prevToken, "Identifier");
 				}
 				break;
 
 			case TOKplusplus:
 			case TOKminusminus:
 				e = new PostExp(filename, lineNumber, token.value, e);
 				e.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 				break;
 
 			case TOKlparen:
 				e = newCallExp(filename, lineNumber, e, parseArguments());
 				e.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
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
 					e = newSliceExp(filename, lineNumber, e, null, null);
 					nextToken();
 				} else {
 					index = parseAssignExp();
 					if (token.value == TOKslice) { // array[lwr .. upr]
 						nextToken();
 						upr = parseAssignExp();
 						e = newSliceExp(filename, lineNumber, e, index, upr);
 					} else { // array[index, i2, i3, i4, ...]
 						Expressions arguments = new Expressions(2);
 						arguments.add(index);
 						if (token.value == TOKcomma) {
 							nextToken();
							while (true) {
 								Expression arg;
 
 								arg = parseAssignExp();
 								arguments.add(arg);
 								if (token.value == TOKrbracket) {
 									break;
 								}
 								check(TOKcomma);
 							}
 						}
 						
 						e = new ArrayExp(filename, lineNumber, e, arguments);
 					}
 					check(TOKrbracket);
 					inBrackets--;
 				}
 				
 				e.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 				continue;
 			}
 
 			default:
 				return e;
 			}
 			
 			// This if is for Descent, for improved statement recovery
 			if (token.value != TOK.TOKrcurly) {
 				nextToken();
 			}
 	    }
 	}	
 
 	private Expression parseUnaryExp() {
 		Expression e;
 
 		int start = token.ptr;
 
 		expect(unaryExpExpectations);
 		switch (token.value) {
 		case TOKand:
 			nextToken();
 			e = parseUnaryExp();
 			e = newAddrExp(filename, lineNumber, e);
 			break;
 
 		case TOKplusplus:
 			nextToken();
 			e = parseUnaryExp();
 			AddAssignExp aae = new AddAssignExp(filename, lineNumber, e, new IntegerExp(filename, lineNumber, 1, Type.tint32));
 			aae.isPreIncrement = true;
 			e = aae;
 			break;
 
 		case TOKminusminus:
 			nextToken();
 			e = parseUnaryExp();
 			MinAssignExp mae = new MinAssignExp(filename, lineNumber, e, new IntegerExp(filename, lineNumber, 1, Type.tint32));
 			mae.isPreDecrement = true;
 			e = mae;
 			break;
 
 		case TOKmul:
 			nextToken();
 			e = parseUnaryExp();
 			e = new PtrExp(filename, lineNumber, e);
 			break;
 
 		case TOKmin:
 			nextToken();
 			e = parseUnaryExp();
 			e = new NegExp(filename, lineNumber, e);
 			break;
 
 		case TOKadd:
 			nextToken();
 			e = parseUnaryExp();
 			e = new UAddExp(filename, lineNumber, e);
 			break;
 
 		case TOKnot:
 			nextToken();
 			e = parseUnaryExp();
 			e = new NotExp(filename, lineNumber, e);
 			break;
 
 		case TOKtilde:
 			nextToken();
 			e = parseUnaryExp();
 			e = new ComExp(filename, lineNumber, e);
 			break;
 
 		case TOKdelete:
 			nextToken();
 			e = parseUnaryExp();
 			e = new DeleteExp(filename, lineNumber, e);
 			break;
 
 		case TOKnew:
 			e = parseNewExp(null);
 			break;
 
 		case TOKcast: // cast(type) expression
 		{
 			Type t;
 
 			nextToken();
 			check(TOKlparen);
 			
 			if (apiLevel < D2) {
 				t = parseBasicType();
 				t = parseDeclarator(t, null); // ( type )
 				check(TOKrparen);
 	
 				e = parseUnaryExp();
 				e = new CastExp(filename, lineNumber, e, t);
 			} else {				
 				int modifierStart = token.ptr;
 				TOK tok = token.value;
 				
 			    /*
 				 * Look for cast(), cast(const), cast(immutable), cast(shared),
 				 * cast(shared const)
 				 */
 				int m;
 				if (token.value == TOKrparen) {
 					m = 0;
 					// goto Lmod1;
 					nextToken();
 					e = parseUnaryExp();
 					e = new CastExp(filename, lineNumber, e, m, tok, modifierStart);
 				} else if (token.value == TOKconst && peekNext() == TOKrparen) {
 					m = MODconst;
 					// goto Lmod2;
 					nextToken();
 					nextToken();
 					e = parseUnaryExp();
 					e = new CastExp(filename, lineNumber, e, m, tok, modifierStart);
 				} else if ((token.value == TOKimmutable || token.value == TOKinvariant)
 						&& peekNext() == TOKrparen) {
 					m = MODinvariant;
 					// goto Lmod2;
 					nextToken();
 					nextToken();
 					e = parseUnaryExp();
 					e = new CastExp(filename, lineNumber, e, m, tok, modifierStart);
 				} else if (token.value == TOKshared && peekNext() == TOKrparen) {
 					m = MODshared;
 					// goto Lmod2;
 					nextToken();
 					nextToken();
 					e = parseUnaryExp();
 					e = new CastExp(filename, lineNumber, e, m, tok, modifierStart);
 				} else if ((token.value == TOKconst && peekNext() == TOKshared
 						&& peekNext2() == TOKrparen) || (token.value == TOKshared
 						&& peekNext() == TOKconst && peekNext2() == TOKrparen)) {
 					m = MODshared | MODconst;
 					nextToken();
 					
 					int modifier2Start = token.ptr;
 					TOK tok2 = token.value;
 					
 					// Lmod2:
 					nextToken();
 					// Lmod1:
 					nextToken();
 					e = parseUnaryExp();
 					e = new CastExp(filename, lineNumber, e, m, tok, modifierStart, tok2, modifier2Start);
 				} else {
 					Type t2 = parseType(); // ( type )
 					check(TOKrparen);
 					e = parseUnaryExp();
 					e = new CastExp(filename, lineNumber, e, t2);
 				}
 			}
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
 				case TOKnot:
 					tk = peek(tk);
 					if (tk.value == TOKis)	// !is
 					    break;
 				case TOKdot:
 				case TOKplusplus:
 				case TOKminusminus:
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
 				case TOKfile:
 				case TOKline:
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
 					
 					if (apiLevel < D2) {
 						t = parseBasicType();
 						t = parseDeclarator(t, null);
 					} else {
 						t = parseType();
 					}
 					check(TOKrparen);
 
 					// if .identifier
 					if (token.value == TOKdot) {
 						nextToken();
 						if (token.value != TOKidentifier) {
 							parsingErrorInsertTokenAfter(prevToken, "Identifier");
 							// Change from DMD
 							e = newTypeDotIdExp(filename, lineNumber, t, null);
 							e.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 							return e;
 						}
 						e = newTypeDotIdExp(filename, lineNumber, t, newIdentifierExp());
 						e.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 						nextToken();
 						e = parsePostExp(e);
 					} else {
 						e = parseUnaryExp();
 						e = new CastExp(filename, lineNumber, e, t);
 						e.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 						error(IProblem.CStyleCastIllegal, firstTokenLine, firstTokenStart, prevToken.ptr + prevToken.sourceLen - firstTokenStart);
 					}
 					return e;
 				}
 				}
 			}
 			// #endif
 			e = parsePrimaryExp();
 			if (apiLevel >= D2) {
 			    e = parsePostExp(e);
 			}
 			break;
 		}
 		default:
 			e = parsePrimaryExp();
 			if (apiLevel >= D2) {
 			    e = parsePostExp(e);
 			}
 			break;
 		}
 		
 		if (e == null) {
 			throw new IllegalStateException();
 		}
 
 		e.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 		if (e instanceof FuncExp) {
 			((FuncExp) e).fd.setSourceRange(e.start, e.length);
 		}
 
 		return e;
 	}	
 
 	private void parsePrimaryExp_case_delegate(Expression[] e, TOK save, boolean isEmptySyntax) {
 		Arguments arguments;
 		int varargs = 0;
 		FuncLiteralDeclaration fd;
 		Type t;
 	    boolean isnothrow = false;
 	    boolean ispure = false;
 
 		if (token.value == TOKlcurly) {
 			t = null;
 			varargs = 0;
 			arguments = new Arguments(3);
 		} else {
 			if (token.value == TOKlparen) {
 				t = null;
 			} else {
 				t = parseBasicType();
 				t = parseBasicType2(t); // function return type
 			}
 			
 			int[] pointer2_varargs = { varargs };
 			arguments = parseParameters(pointer2_varargs);
 			varargs = pointer2_varargs[0];
 			
 			if (apiLevel >= D2) {
 				while (true) {
 					if (token.value == TOKpure)
 						ispure = true;
 					else if (token.value == TOKnothrow)
 						isnothrow = true;
 					else
 						break;
 					nextToken();
 				}
 			}
 		}
 		
 		t = new TypeFunction(arguments, t, varargs, linkage);
 	    ((TypeFunction) t).ispure = ispure;
 	    ((TypeFunction) t).isnothrow = isnothrow;
 		
 		fd = new FuncLiteralDeclaration(filename, lineNumber, t, save, null);
 		
 		parseContracts(fd);
 		e[0] = new FuncExp(filename, lineNumber, fd);
 		((FuncExp) e[0]).isEmptySyntax = isEmptySyntax;
 	}
 	
 	private Expression parseMulExp()
 	{   Expression e;
 	    Expression e2;
 
 	    e = parseUnaryExp();
 	    while (true)
 	    {
 		switch (token.value)
 		{
 		    case TOKmul: nextToken(); e2 = parseUnaryExp(); e = newMulExp(filename, lineNumber, e, e2); continue;
 		    case TOKdiv:   nextToken(); e2 = parseUnaryExp(); e = newDivExp(filename, lineNumber, e, e2); continue;
 		    case TOKmod:  nextToken(); e2 = parseUnaryExp(); e = newModExp(filename, lineNumber, e, e2); continue;
 
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
 		    case TOKadd:    nextToken(); e2 = parseMulExp(); e = newAddExp(filename, lineNumber, e, e2); continue;
 		    case TOKmin:    nextToken(); e2 = parseMulExp(); e = newMinExp(filename, lineNumber, e, e2); continue;
 		    case TOKtilde:  nextToken(); e2 = parseMulExp(); e = newCatExp(filename, lineNumber, e, e2); continue;
 
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
 		    case TOKshl:  nextToken(); e2 = parseAddExp(); e = newShlExp(filename, lineNumber, e, e2);  continue;
 		    case TOKshr:  nextToken(); e2 = parseAddExp(); e = newShrExp(filename, lineNumber, e, e2);  continue;
 		    case TOKushr: nextToken(); e2 = parseAddExp(); e = newUshrExp(filename, lineNumber, e, e2); continue;
 
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
 		    	e = newCmpExp(filename, lineNumber, op, e, e2); continue;
 		    case TOKin: 
 		    	nextToken(); 
 		    	e2 = parseShiftExp(); 
 		    	e = newInExp(filename, lineNumber, e, e2); 
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
 				e = newEqualExp(filename, lineNumber, value, e, e2);
 				continue;
 
 		    case TOKidentity:
 		    	error(
 		    			IProblem.ThreeEqualsIsNoLongerLegal, token.lineNumber, token.ptr, token.sourceLen);
 			//goto L1;
 			nextToken();
 			e2 = parseRelExp();
 			e = newIdentityExp(filename, lineNumber, value, e, e2);
 			continue;
 
 		    case TOKnotidentity:
 		    	error(
 		    			IProblem.NotTwoEqualsIsNoLongerLegal, token.lineNumber, token.ptr, token.sourceLen);
 			//goto L1;
 			nextToken();
 			e2 = parseRelExp();
 			e = newIdentityExp(filename, lineNumber, value, e, e2);
 			continue;
 
 		    case TOKis:
 			value = TOKidentity;
 			//goto L1;
 			nextToken();
 			e2 = parseRelExp();
 			e = newIdentityExp(filename, lineNumber, value, e, e2);
 			continue;
 
 		    case TOKnot:
 			// Attempt to identify '!is'
 			t = peek(token);
 			if (t.value != TOKis) {
 				break;
 			}
 			nextToken();
 			value = TOKnotidentity;
 			//goto L1;
 			nextToken();
 			e2 = parseRelExp();
 			e = newIdentityExp(filename, lineNumber, value, e, e2);
 			continue;
 
 		    // L1:
 			//nextToken();
 			//e2 = parseRelExp();
 			//e = new IdentityExp(value, filename, lineNumber, e, e2);
 			//continue;
 
 		    default:
 			break;
 		}
 		break;
 	    }
 	    return e;
 	}
 	
 	private Expression parseCmpExp()
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
 			    e = newEqualExp(filename, lineNumber, op, e, e2);
 			    break;
 
 		case TOKis:
 		    //op = TOKidentity;
 		    nextToken();
 		    e2 = parseShiftExp();
 		    e = newIdentityExp(filename, lineNumber, op, e, e2);
 		    break;
 
 		case TOKnot:
 		    // Attempt to identify '!is'
 		    t = peek(token);
 		    if (t.value != TOKis) {
 				break;
 			}
 		    nextToken();
 		    op = TOKnotis;
 		    nextToken();
 		    e2 = parseShiftExp();
 		    e = newIdentityExp(filename, lineNumber, op, e, e2);
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
 	    	e = newCmpExp(filename, lineNumber, op, e, e2); 
 	    	break;
 
 		case TOKin:
 		    nextToken();
 		    e2 = parseShiftExp();
 		    e = newInExp(filename, lineNumber, e, e2);
 		    break;
 
 		default:
 		    break;
 	    }
 	    return e;
 	}
 
 	private Expression parseAndExp() {
 		Expression e;
 		Expression e2;
 
 		if (apiLevel == D0) {
 			e = parseEqualExp();
 			while (token.value == TOKand) {
 				nextToken();
 				e2 = parseEqualExp();
 				e = newAndExp(filename, lineNumber, e, e2);
 			}
 		} else {
 			e = parseCmpExp();
 			while (token.value == TOKand)
 			{
 			    nextToken();
 			    e2 = parseCmpExp();
 			    e = newAndExp(filename, lineNumber, e, e2);
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
 			e = newXorExp(filename, lineNumber, e, e2);
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
 			e = newOrExp(filename, lineNumber, e, e2);
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
 			e = newAndAndExp(filename, lineNumber, e, e2);
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
 			e = newOrOrExp(filename, lineNumber, e, e2);
 		}
 		return e;
 	}
 
 	public Expression parseCondExp() {
 		Expression e;
 		Expression e1;
 		Expression e2;
 
 		e = parseOrOrExp();
 		if (token.value == TOKquestion) {
 			nextToken();
 			e1 = parseExpression();
 			check(TOKcolon);
 			e2 = parseCondExp();
 			e = newCondExp(filename, lineNumber, e, e1, e2);
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
 		case TOKassign:  nextToken(); e2 = parseAssignExp(); e = newAssignExp(filename, lineNumber, e, e2); continue;
 		case TOKaddass:  nextToken(); e2 = parseAssignExp(); e = newAddAssignExp(filename, lineNumber, e, e2); continue;
 		case TOKminass:  nextToken(); e2 = parseAssignExp(); e = newMinAssignExp(filename, lineNumber, e, e2); continue;
 		case TOKmulass:  nextToken(); e2 = parseAssignExp(); e = newMulAssignExp(filename, lineNumber, e, e2); continue;
 		case TOKdivass:  nextToken(); e2 = parseAssignExp(); e = newDivAssignExp(filename, lineNumber, e, e2); continue;
 		case TOKmodass:  nextToken(); e2 = parseAssignExp(); e = newModAssignExp(filename, lineNumber, e, e2); continue;
 		case TOKandass:  nextToken(); e2 = parseAssignExp(); e = newAndAssignExp(filename, lineNumber, e, e2); continue;
 		case TOKorass:  nextToken(); e2 = parseAssignExp(); e = newOrAssignExp(filename, lineNumber, e, e2); continue;
 		case TOKxorass:  nextToken(); e2 = parseAssignExp(); e = newXorAssignExp(filename, lineNumber, e, e2); continue;
 		case TOKshlass:  nextToken(); e2 = parseAssignExp(); e = newShlAssignExp(filename, lineNumber, e, e2); continue;
 		case TOKshrass:  nextToken(); e2 = parseAssignExp(); e = newShrAssignExp(filename, lineNumber, e, e2); continue;
 		case TOKushrass:  nextToken(); e2 = parseAssignExp(); e = newUshrAssignExp(filename, lineNumber, e, e2); continue;
 		case TOKcatass:  nextToken(); e2 = parseAssignExp(); e = newCatAssignExp(filename, lineNumber, e, e2); continue;
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
 			e = new CommaExp(filename, lineNumber, e, e2);
 		}
 		return e;
 	}
 	
 	/***************************************************************************
 	 * Collect argument list. Assume current token is ',', '(' or '['.
 	 */
 	
 	@SuppressWarnings("unchecked")
 	private Expressions parseArguments() {
 		// function call
 		Expressions arguments;
 		Expression arg;
 		TOK endtok;
 
 		arguments = new Expressions(3);
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
 					if (token.value == endtok) {
 						break;
 					}
 					if (token.value == TOKeof) {
 						return arguments;
 					}
 					check(TOKcomma);
 					
 					// Descent: added for better error reporting 
 					if (token.value == TOK.TOKsemicolon) {
 						return arguments;
 					}
 				}
 			}
 			check(endtok);
 		}
 		return arguments;
 	}
 	
 	@SuppressWarnings("unchecked")
 	private Expression parseNewExp(Expression thisexp) {
 		int start = token.ptr;
 		
 		Type t;
 		Expressions newargs = null;
 		Expressions arguments = null;
 		Expression e;
 
 		nextToken();
 		if (token.value == TOKlparen) {
 			newargs = parseArguments();
 		}
 		
 		expect(classExpectations);
 
 		// An anonymous nested class starts with "class"
 		if (token.value == TOKclass) {
 			int anonStart = token.ptr;
 			
 			nextToken();
 			if (token.value == TOKlparen) {
 				arguments = parseArguments();
 			}
 
 			BaseClasses baseClasses = null;
 			if (token.value != TOKlcurly) {
 				baseClasses = parseBaseClasses();
 			}
 
 			IdentifierExp id = null;
 			ClassDeclaration cd = newClassDeclaration(filename, lineNumber, id, baseClasses);
 
 			if (token.value != TOKlcurly) {
 				parsingErrorInsertToComplete(prevToken, "{ members }", "AnnonymousClassDeclaration");
 				cd.members = null;
 			} else {
 				nextToken();
 				Dsymbols decl = parseDeclDefs(false);
 				if (token.value != TOKrcurly) {
 					parsingErrorInsertToComplete(prevToken, "ClassBody", "AnnonymousClassDeclaration");
 				}
 				nextToken();
 				cd.members = decl;
 				cd.sourceMembers = new Dsymbols(decl);
 			}
 			
 			cd.start = anonStart;
 			cd.length = prevToken.ptr + prevToken.sourceLen - anonStart;
 			
 			e = new NewAnonClassExp(filename, lineNumber, thisexp, newargs, cd, arguments);
 
 			return e;
 		}
 
 		// #if LTORARRAYDECL
 		int lineNumber = token.lineNumber;
 		t = parseBasicType();
 		t = parseBasicType2(t);
 		if (t != null) {
 			if (t.ty == Taarray) {
 				Type index = ((TypeAArray) t).index;
 				
 				Expression e2 = index.toExpression();
 				if (e2 != null) {
 					arguments = new Expressions(1);
 					arguments.add(e2);				
 					t = new TypeDArray(t.nextOf());
 				} else {
 					error(IProblem.NeedSizeOfRightmostArray, lineNumber, index);
 					NullExp nullExp = new NullExp(filename, lineNumber);
 					nullExp.setSourceRange(token.ptr, token.sourceLen);
 					return nullExp;
 				}
 			} else if (t.ty == Tsarray) {
 				TypeSArray tsa = (TypeSArray) t;
 				Expression e2 = tsa.dim;
 	
 				arguments = new Expressions(1);
 				arguments.add(e2);
 				
 				t = new TypeDArray(t.nextOf());
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
 		 * arguments.push(e); check(TOKrbracket); t = parseDeclarator(t, NULL);
 		 * t = new TypeDArray(t); } else if (token.value == TOKlparen) arguments =
 		 * parseArguments(); #endif
 		 */
 		e = newNewExp(filename, lineNumber, thisexp, newargs, t, arguments, start);
 		return e;
 	}
 
 	private StringExp newStringExpForCurrentToken() {
 		StringExp string = new StringExp(filename, lineNumber, token.ustring, token.len, (char) token.postfix);
 		string.sourceString = token.sourceString;
 		string.setSourceRange(token.ptr, token.sourceLen);
 		return string;
 	}
 	
 	private StringExp newStringExpForPreviousToken() {
 		StringExp string = new StringExp(filename, lineNumber, prevToken.ustring, prevToken.len, (char) prevToken.postfix);
 		string.sourceString = prevToken.sourceString;
 		string.setSourceRange(prevToken.ptr, prevToken.sourceLen);
 		return string;
 	}
 	
 	private TypeAArray newTypeAArray(Type componentType, Type keyType) {
 		TypeAArray associativeArray = new TypeAArray(componentType, keyType);
 	    associativeArray.setSourceRange(componentType.start, token.ptr + token.sourceLen - componentType.start);
 	    return associativeArray;
 	}
 	
 	protected CompoundStatement newBlock(Statements statements, int start, int length) {
 		CompoundStatement cs = newCompoundStatement(filename, lineNumber, statements);
 		cs.setSourceRange(start, length);
 		return cs;
 	}
 	
 	private CompoundStatement newManyVarsBlock(Statements statements) {
 		CompoundDeclarationStatement statement = newCompoundDeclarationStatement(filename, lineNumber, statements);
 		statement.manyVars = true;
 		return statement;
 	}
 
 	private DebugSymbol newDebugAssignmentForCurrentToken() {
 		if (token.value == TOKint32v) {
 			return new DebugSymbol(filename, lineNumber, token.intValue.longValue(), newVersionForCurrentToken());
 		} else if (token.value == TOKidentifier) {
 			return newDebugSymbol(filename, lineNumber, new IdentifierExp(filename, lineNumber, token.sourceString), newVersionForCurrentToken());
 		} else {
 			throw new RuntimeException("Can't happen");
 		}
 	}
 	
 	private Version newVersionForCurrentToken() {
 		Version version;
 		
 		if (token.value == TOKint32v) {
 			version = newVersion(filename, lineNumber, token.sourceString);
 			version.setSourceRange(token.ptr, token.sourceLen);
 		} else if (token.value == TOKidentifier) {
 			version = newVersion(filename, lineNumber, token.sourceString);
 			version.setSourceRange(token.ptr, token.sourceLen);
 		} else {
 			throw new RuntimeException("Can't happen");
 		}
 		
 		return version;
 	}
 
 	private VersionSymbol newVersionAssignmentForCurrentToken() {
 		if (token.value == TOKint32v) {
 			return newVersionSymbol(filename, lineNumber, token.intValue.longValue(), newVersionForCurrentToken());
 		} else if (token.value == TOKidentifier) {
 			return newVersionSymbol(filename, lineNumber, newIdentifierExp(), newVersionForCurrentToken());
 		} else {
 			throw new RuntimeException("Can't happen");
 		}
 	}	
 
 	private VoidInitializer newVoidInitializerForToken(Token token) {
 		VoidInitializer voidInitializer = new VoidInitializer(filename, lineNumber);
 		voidInitializer.setSourceRange(token.ptr, token.sourceLen);
 		return voidInitializer;
 	}
 	
 	protected Type newTypeBasicForCurrentToken() {
 		TypeBasic t = null;
 		switch(token.value) {
 		case TOKvoid: t = new TypeBasic(Type.tvoid); break;
 		case TOKint8: t = new TypeBasic(Type.tint8); break;
 		case TOKuns8: t = new TypeBasic(Type.tuns8); break;
 		case TOKint16: t = new TypeBasic(Type.tint16); break;
 		case TOKuns16: t = new TypeBasic(Type.tuns16); break;
 		case TOKint32: t = new TypeBasic(Type.tint32); break;
 		case TOKuns32: t = new TypeBasic(Type.tuns32); break;
 		case TOKint64: t = new TypeBasic(Type.tint64); break;
 		case TOKuns64: t = new TypeBasic(Type.tuns64); break;
 		case TOKfloat32: t = new TypeBasic(Type.tfloat32); break;
 		case TOKfloat64: t = new TypeBasic(Type.tfloat64); break;
 		case TOKfloat80: t = new TypeBasic(Type.tfloat80); break;
 		case TOKimaginary32: t = new TypeBasic(Type.timaginary32); break;
 		case TOKimaginary64: t = new TypeBasic(Type.timaginary64); break;
 		case TOKimaginary80: t = new TypeBasic(Type.timaginary80); break;
 		case TOKcomplex32: t = new TypeBasic(Type.tcomplex32); break;
 		case TOKcomplex64: t = new TypeBasic(Type.tcomplex64); break;
 		case TOKcomplex80: t = new TypeBasic(Type.tcomplex80); break;
 		case TOKbit: t = new TypeBasic(Type.tbit); break;
 		case TOKbool: t = new TypeBasic(Type.tbool); break;
 		case TOKchar: t = new TypeBasic(Type.tchar); break;
 		case TOKwchar: t = new TypeBasic(Type.twchar); break;
 		case TOKdchar: t = new TypeBasic(Type.tdchar); break;
 		}
 		t.setSourceRange(token.ptr, token.sourceLen);
 		return t;
 	}
 	
 	private List<Comment> getLastComments() {
 		if (comments == null) {
 			return Collections.emptyList();
 		}
 		
 		LinkedList<Comment> toReturn = new LinkedList<Comment>();
 		for(int i = comments.size() - 1; i >= lastCommentRead; i--) {
 			Comment comment = comments.get(i);
 			toReturn.addFirst(comment);
 		}
 		
 		lastCommentRead = comments.size();		
 		return toReturn;
 	}
 	
 	private void attachLeadingComments(ASTDmdNode declaration) {
 		if (prevToken.leadingComment != null) {
 			this.setPostComment(declaration, prevToken.leadingComment);
 		}
 	}
 	
 	private void parsingErrorInsertTokenAfter(Token targetToken, String expected) {
 		error(IProblem.ParsingErrorInsertTokenAfter, targetToken.lineNumber, targetToken.ptr, targetToken.sourceLen, new String[] { new String(targetToken.toString()), expected });
 	}
 	
 	private void parsingErrorInsertTokenAfter(int lineNumber, int start, int length, char[] tokenName, String expected) {
 		error(IProblem.ParsingErrorInsertTokenAfter, lineNumber, start, length, new String[] { new String(tokenName), expected });
 	}
 	
 	private void parsingErrorDeleteToken(Token targetToken) {
 		error(IProblem.ParsingErrorDeleteToken, targetToken.lineNumber, targetToken.ptr, targetToken.sourceLen, new String[] { targetToken.toString() });
 	}
 	
 	private void parsingErrorDeleteToken(int lineNumber, int start, int length, char[] tokenName) {
 		error(IProblem.ParsingErrorDeleteToken, lineNumber, start, length, new String[] { new String(tokenName) });
 	}
 	
 	private void parsingErrorInsertToComplete(Token targetToken, String insert, String toComplete) {
 		error(IProblem.ParsingErrorInsertToComplete, targetToken.lineNumber, targetToken.ptr, targetToken.sourceLen, new String[] { insert, toComplete });
 	}
 	
 	private void parsingErrorInsertToComplete(int lineNumber, int start, int length, String insert, String toComplete) {
 		error(IProblem.ParsingErrorInsertToComplete, lineNumber, start, length, new String[] { insert, toComplete });
 	}
 	
 	private String toWord(String s) {
 		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
 	}
 	
 	@Override
 	public TOK nextToken() {
 		// In diet mode we don't care about comments and pragmas
 		if (inDiet) {
 			TOK tok = super.nextToken();
 			while(tok == TOKlinecomment || tok == TOKdoclinecomment ||
 				  tok == TOKblockcomment || tok == TOKdocblockcomment ||
 				  tok == TOKpluscomment || tok == TOKdocpluscomment ||
 				  tok == TOKPRAGMA) {
 				tok = super.nextToken();
 			}
 			return tok;
 		}
 		
 		appendLeadingComments = true;
 		
 		// If many comments come in a rush, only the first
 		// one is to be attached to the previous token as a
 		// leading comment
 		boolean first = true;
 		TOK tok = super.nextToken();
 		while(tok == TOKlinecomment || tok == TOKdoclinecomment ||
 			  tok == TOKblockcomment || tok == TOKdocblockcomment ||
 			  tok == TOKpluscomment || tok == TOKdocpluscomment) {
 			
 			inComment();
 			
 			Comment comment;
 			switch(tok) {
 			case TOKlinecomment:
 				comment = new Comment(Comment.LINE_COMMENT, token.sourceString);
 				break;
 			case TOKblockcomment:
 				comment = new Comment(Comment.BLOCK_COMMENT, token.sourceString);
 				break;
 			case TOKpluscomment:
 				comment = new Comment(Comment.PLUS_COMMENT, token.sourceString);
 				break;
 			case TOKdoclinecomment:
 				comment = new Comment(Comment.DOC_LINE_COMMENT, token.sourceString);
 				break;
 			case TOKdocblockcomment:
 				comment = new Comment(Comment.DOC_BLOCK_COMMENT, token.sourceString);
 				break;
 			case TOKdocpluscomment:
 				comment = new Comment(Comment.DOC_PLUS_COMMENT, token.sourceString);
 				break;
 			default:
 				throw new IllegalStateException("Can't happen");
 			}
 			
 			comment.setSourceRange(token.ptr, token.sourceLen);
 			
 			comments.add(comment);
 			if (first) {
 				attachCommentToCurrentToken(comment);
 			}
 			
 			tok = super.nextToken();
 			first = false;
 		}
 		
 		while(tok == TOKPRAGMA) {
 			if (token.ptr == 0 && token.sourceString.length > 1 && token.sourceString[1] == '!') {
 				// Script line
 				Pragma pragma = new Pragma();
 				pragma.setSourceRange(0, token.sourceLen);
 				pragmas.add(pragma);
 			} else {
 				Pragma pragma = new Pragma();
 				pragma.setSourceRange(token.ptr, token.sourceLen);
 				pragmas.add(pragma);
 				
 				// Let's see if it's correct
 				// TODO improve performance
 				StringTokenizer st = new StringTokenizer(new String(token.sourceString).substring(1));
 				int count = st.countTokens();
 				if (count <= 1 || count >= 4) {
 					error(IProblem.InvalidPragmaSyntax, token);
 				} else {
 					String value = st.nextToken();
 					if (!"line".equals(value)) {
 						error(IProblem.InvalidPragmaSyntax, token);
 					} else {
 						value = st.nextToken();
 						try {
 							if (!"__LINE__".equals(value)) {
 								int num = Integer.parseInt(value);
 								if (num < 0) {
 									throw new NumberFormatException();
 								}
 							}
 							
 							if (st.hasMoreTokens()) {
 								value = st.nextToken();
 								if (!"__FILE__".equals(value)) {
 									if (value.length() < 2 || value.charAt(0) != '"' || value.charAt(value.length() - 1) != '"') {
 										error(IProblem.InvalidPragmaSyntax, token);
 									}
 								}
 							}
 						} catch (NumberFormatException e) {
 							error(IProblem.InvalidPragmaSyntax, token);
 						}
 					}
 				}
 			}
 			
 			tok = nextToken();
 		}
 		
 		return tok;
 	}
 	
 	@Override
 	public Token peek(Token ct) {
 		Token tok = super.peek(ct);
 		while(tok != null && 
 				(tok.value == TOK.TOKlinecomment || tok.value == TOK.TOKblockcomment || tok.value == TOKpluscomment ||
 						tok.value == TOK.TOKdoclinecomment || tok.value == TOK.TOKdocblockcomment || tok.value == TOKdocpluscomment)) {
 			tok = super.peek(tok);
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
 	
 	protected IdentifierExp newIdentifierExp() {
 		return new IdentifierExp(filename, lineNumber, token);
 	}
 	
 	private void attachCommentToCurrentToken(Comment comment) {
 		if (!appendLeadingComments || prevToken.value == null ||
 				prevToken.value == TOK.TOKlcurly) {
 			return;
 		}
 		
 		if (prevToken.leadingComment == null) {
 			lastCommentRead = comments.size();	
 		}
 		prevToken.leadingComment = comment;		
 	}
 	
 	private void discardLastComments() {
 		if (this.comments != null) {
 			this.lastCommentRead = this.comments.size();
 		}
 	}
 	
 	/**
 	 * Creates a ModuleDeclaration with the given packages and module name. The
 	 * current token is the token following the last token of the module declaration.
 	 * packages and module can be null at the same time, meaning "module " is written
 	 * in the source code.
 	 */
 	protected ModuleDeclaration newModuleDeclaration(Identifiers packages, IdentifierExp module, boolean safe) {
 		return new ModuleDeclaration(packages, module, safe);
 	}
 	
 	protected Import newImport(char[] filename, int lineNumber, Identifiers packages, IdentifierExp module, IdentifierExp aliasid, boolean isstatic) {
 		return new Import(filename, lineNumber, packages, module, aliasid, isstatic);
 	}
 	
 	protected Argument newArgument(int storageClass, Type at, IdentifierExp ai, Expression ae) {
 		return new Argument(storageClass, at, ai, ae);
 	}
 	
 	protected GotoStatement newGotoStatement(char[] filename, int lineNumber, IdentifierExp ident) {
 		return new GotoStatement(filename, lineNumber, ident);
 	}
 	
 	protected ContinueStatement newContinueStatement(char[] filename, int lineNumber, IdentifierExp ident) {
 		return new ContinueStatement(filename, lineNumber, ident);
 	}
 
 	protected BreakStatement newBreakStatement(char[] filename, int lineNumber, IdentifierExp ident) {
 		return new BreakStatement(filename, lineNumber, ident);
 	}
 	
 	protected VersionCondition newVersionCondition(Module module, char[] filename, int lineNumber, long level, char[] id) {
 		return new VersionCondition(module, filename, lineNumber, level, id);
 	}
 	
 	protected VersionSymbol newVersionSymbol(char[] filename, int lineNumber, IdentifierExp id, Version version) {
 		return new VersionSymbol(filename, lineNumber, id, version);
 	}
 	
 	protected VersionSymbol newVersionSymbol(char[] filename, int lineNumber, long l, Version version) {
 		return new VersionSymbol(filename, lineNumber, l, version);
 	}
 	
 	protected DebugCondition newDebugCondition(Module module, char[] filename, int lineNumber, long level, char[] id) {
 		return new DebugCondition(module, filename, lineNumber, level, id);
 	}
 	
 	protected DebugSymbol newDebugSymbol(char[] filename, int lineNumber, IdentifierExp id, Version version) {
 		return new DebugSymbol(filename, lineNumber, id, version);
 	}
 	
 	protected CaseStatement newCaseStatement(char[] filename, int lineNumber, Expression exp, Statement statement, int caseEnd, int expStart, int expLength) {
 		return new CaseStatement(filename, lineNumber, exp, statement);
 	}
 	
 	protected CaseRangeStatement newCaseRangeStatement(char[] filename, int lineNumber,
 			Expression first, Expression last, Statement s) {
 		return new CaseRangeStatement(filename, lineNumber, first, last, s);
 	}
 	
 	private final DotIdExp newTypeDotIdExp(char[] filename, int lineNumber, Type t, IdentifierExp exp) {
 		return newDotIdExp(filename, lineNumber, new TypeExp(filename, lineNumber, t), exp);
 	}
 	
 	protected DotIdExp newDotIdExp(char[] filename, int lineNumber, Expression e, IdentifierExp id) {
 		return new DotIdExp(filename, lineNumber, e, id);
 	}
 	
 	protected TypeQualified newTypeIdentifier(char[] filename, int lineNumber, IdentifierExp id) {
 		return new TypeIdentifier(filename, lineNumber, id);
 	}
 	
 	protected ExpStatement newExpStatement(char[] filename, int lineNumber, Expression exp) {
 		return new ExpStatement(filename, lineNumber, exp);
 	}
 	
 	protected SuperExp newSuperExp(char[] filename, int lineNumber) {
 		return new SuperExp(filename, lineNumber);
 	}
 
 	protected ThisExp newThisExp(char[] filename, int lineNumber) {
 		return new ThisExp(filename, lineNumber);
 	}
 	
 	protected Expression newAssignExp(char[] filename, int lineNumber, Expression e, Expression e2) {
 		return new AssignExp(filename, lineNumber, e, e2);
 	}
 
 	protected Expression newAddAssignExp(char[] filename, int lineNumber, Expression e, Expression e2) {
 		return new AddAssignExp(filename, lineNumber, e, e2);
 	}
 
 	protected Expression newMinAssignExp(char[] filename, int lineNumber, Expression e, Expression e2) {
 		return new MinAssignExp(filename, lineNumber, e, e2);
 	}
 
 	protected Expression newMulAssignExp(char[] filename, int lineNumber, Expression e, Expression e2) {
 		return new MulAssignExp(filename, lineNumber, e, e2);
 	}
 
 	protected Expression newDivAssignExp(char[] filename, int lineNumber, Expression e, Expression e2) {
 		return new DivAssignExp(filename, lineNumber, e, e2);
 	}
 
 	protected Expression newModAssignExp(char[] filename, int lineNumber, Expression e, Expression e2) {
 		return new ModAssignExp(filename, lineNumber, e, e2);
 	}
 
 	protected Expression newAndAssignExp(char[] filename, int lineNumber, Expression e, Expression e2) {
 		return new AndAssignExp(filename, lineNumber, e, e2);
 	}
 
 	protected Expression newOrAssignExp(char[] filename, int lineNumber, Expression e, Expression e2) {
 		return new OrAssignExp(filename, lineNumber, e, e2);
 	}
 
 	protected Expression newXorAssignExp(char[] filename, int lineNumber, Expression e, Expression e2) {
 		return new XorAssignExp(filename, lineNumber, e, e2);
 	}
 
 	protected Expression newShlAssignExp(char[] filename, int lineNumber, Expression e, Expression e2) {
 		return new ShlAssignExp(filename, lineNumber, e, e2);
 	}
 
 	protected Expression newShrAssignExp(char[] filename, int lineNumber, Expression e, Expression e2) {
 		return new ShrAssignExp(filename, lineNumber, e, e2);
 	}
 
 	protected Expression newUshrAssignExp(char[] filename, int lineNumber, Expression e, Expression e2) {
 		return new UshrAssignExp(filename, lineNumber, e, e2);
 	}
 
 	protected Expression newCatAssignExp(char[] filename, int lineNumber, Expression e, Expression e2) {
 		return new CatAssignExp(filename, lineNumber, e, e2);
 	}
 	
 	protected Expression newCondExp(char[] filename, int lineNumber, Expression e, Expression e1, Expression e2) {
 		return new CondExp(filename, lineNumber, e, e1, e2);
 	}
 	
 	protected Expression newOrOrExp(char[] filename, int lineNumber, Expression e, Expression e2) {
 		return new OrOrExp(filename, lineNumber, e, e2);
 	}
 	
 	protected Expression newAndExp(char[] filename, int lineNumber, Expression e, Expression e2) {
 		return new AndExp(filename, lineNumber, e, e2);
 	}
 	
 	protected Expression newOrExp(char[] filename, int lineNumber, Expression e, Expression e2) {
 		return new OrExp(filename, lineNumber, e, e2);
 	}
 	
 	protected Expression newAndAndExp(char[] filename, int lineNumber, Expression e, Expression e2) {
 		return new AndAndExp(filename, lineNumber, e, e2);
 	}
 	
 	protected Expression newXorExp(char[] filename, int lineNumber, Expression e, Expression e2) {
 		return new XorExp(filename, lineNumber, e, e2);
 	}
 	
 	protected Expression newEqualExp(char[] filename, int lineNumber, TOK op, Expression e, Expression e2) {
 		return new EqualExp(filename, lineNumber, op, e, e2);
 	}
 	
 	protected Expression newIdentityExp(char[] filename, int lineNumber, TOK op, Expression e, Expression e2) {
 		return new IdentityExp(filename, lineNumber, op, e, e2);
 	}
 	
 	protected Expression newCmpExp(char[] filename, int lineNumber, TOK op, Expression e, Expression e2) {
 		return new CmpExp(filename, lineNumber, op, e, e2);
 	}
 	
 	protected Expression newInExp(char[] filename, int lineNumber, Expression e, Expression e2) {
 		return new InExp(filename, lineNumber, e, e2);
 	}
 	
 	protected Expression newShlExp(char[] filename, int lineNumber, Expression e, Expression e2) {
 		return new ShlExp(filename, lineNumber, e, e2);
 	}
 
 	protected Expression newShrExp(char[] filename, int lineNumber, Expression e, Expression e2) {
 		return new ShrExp(filename, lineNumber, e, e2);
 	}
 
 	protected Expression newUshrExp(char[] filename, int lineNumber, Expression e, Expression e2) {
 		return new UshrExp(filename, lineNumber, e, e2);
 	}
 	
 	protected Expression newAddExp(char[] filename, int lineNumber, Expression e, Expression e2) {
 		return new AddExp(filename, lineNumber, e, e2);
 	}
 
 	protected Expression newMinExp(char[] filename, int lineNumber, Expression e, Expression e2) {
 		return new MinExp(filename, lineNumber, e, e2);
 	}
 
 	protected Expression newCatExp(char[] filename, int lineNumber, Expression e, Expression e2) {
 		return new CatExp(filename, lineNumber, e, e2);
 	}
 	
 	protected Expression newModExp(char[] filename, int lineNumber, Expression e, Expression e2) {
 		return new ModExp(filename, lineNumber, e, e2);
 	}
 
 	protected Expression newDivExp(char[] filename, int lineNumber, Expression e, Expression e2) {
 		return new DivExp(filename, lineNumber, e, e2);
 	}
 
 	protected Expression newMulExp(char[] filename, int lineNumber, Expression e, Expression e2) {
 		return new MulExp(filename, lineNumber, e, e2);
 	}
 	
 	protected Expression newAddrExp(char[] filename, int lineNumber, Expression e) {
 		return new AddrExp(filename, lineNumber, e);
 	}
 	
 	protected VarDeclaration newVarDeclaration(char[] filename, int lineNumber, Type type, IdentifierExp ident, Initializer init) {
 		return new VarDeclaration(filename, lineNumber, type, ident, init);
 	}
 	
 	protected Expression newCallExp(char[] filename, int lineNumber, Expression e, Expressions expressions) {
 		return new CallExp(filename, lineNumber, e, expressions);
 	}
 	
 	protected Expression newNewExp(char[] filename, int lineNumber, Expression thisexp, Expressions newargs, Type t, Expressions arguments, int start) {
 		return new NewExp(filename, lineNumber, thisexp, newargs, t, arguments);
 	}
 	
 	protected Statement newReturnStatement(char[] filename, int lineNumber, Expression exp) {
 		return new ReturnStatement(filename, lineNumber, exp);
 	}
 	
 	protected UnionDeclaration newUnionDeclaration(char[] filename, int lineNumber, IdentifierExp id) {
 		return new UnionDeclaration(filename, lineNumber, id);
 	}
 
 	protected StructDeclaration newStructDeclaration(char[] filename, int lineNumber, IdentifierExp id) {
 		return new StructDeclaration(filename, lineNumber, id);
 	}
 
 	protected InterfaceDeclaration newInterfaceDeclaration(char[] filename, int lineNumber, IdentifierExp id, BaseClasses baseClasses) {
 		return new InterfaceDeclaration(filename, lineNumber, id, baseClasses);
 	}
 
 	protected ClassDeclaration newClassDeclaration(char[] filename, int lineNumber, IdentifierExp id, BaseClasses baseClasses) {
 		return new ClassDeclaration(filename, lineNumber, id, baseClasses);
 	}
 	
 	protected TemplateMixin newTemplateMixin(char[] filename, int lineNumber, IdentifierExp id, Type tqual, Identifiers idents, Objects tiargs) {
 		return new TemplateMixin(filename, lineNumber, id, tqual, idents, tiargs, encoder);
 	}
 	
 	protected TemplateInstance newTemplateInstance(char[] filename, int lineNumber, IdentifierExp id, ASTNodeEncoder encoder) {
 		return new TemplateInstance(filename, lineNumber, id, encoder);
 	}
 	
 	protected ConditionalDeclaration newConditionalDeclaration(Condition condition, Dsymbols a, Dsymbols aelse) {
 		return new ConditionalDeclaration(condition, a, aelse);
 	}
 	
 	protected StaticIfDeclaration newStaticIfDeclaration(StaticIfCondition condition, Dsymbols a, Dsymbols aelse) {
 		return new StaticIfDeclaration(condition, a, aelse);
 	}
 	
 	protected FuncDeclaration newFuncDeclaration(char[] filename, int lineNumber, IdentifierExp ident, int storage_class, TypeFunction typeFunction) {
 		return new FuncDeclaration(filename, lineNumber, ident, storage_class, typeFunction);
 	}
 	
 	protected IfStatement newIfStatement(char[] filename, int lineNumber, Argument arg, Expression condition, Statement ifbody, Statement elsebody) {
 		return new IfStatement(filename, lineNumber, arg, condition, ifbody, elsebody);
 	}
 	
 	protected Statement newDeclarationStatement(char[] filename, int lineNumber, Dsymbol d) {
 		return new DeclarationStatement(filename, lineNumber, d);
 	}
 	
 	protected CompileStatement newCompileStatement(char[] filename, int lineNumber, Expression e) {
 		return new CompileStatement(filename, lineNumber, e);
 	}
 	
 	protected AggregateDeclaration endAggregateDeclaration(AggregateDeclaration a) {
 		return a;
 	}
 	
 	protected CompoundStatement newCompoundStatement(char[] filename, int lineNumber, Statements statements) {
 		return new CompoundStatement(filename, lineNumber, statements);
 	}
 	
 	protected CompoundDeclarationStatement newCompoundDeclarationStatement(char[] filename, int lineNumber, Statements statements) {
 		return new CompoundDeclarationStatement(filename, lineNumber, statements);
 	}
 	
 	protected ConditionalStatement newConditionalStatement(char[] filename, int lineNumber, Condition condition, Statement ifbody, Statement elsebody) {
 		return new ConditionalStatement(filename, lineNumber, condition, ifbody, elsebody);
 	}
 	
 	protected DefaultStatement newDefaultStatement(char[] filename, int lineNumber, Statement s) {
 		return new DefaultStatement(filename, lineNumber, s);
 	}
 	
 	protected DoStatement newDoStatement(char[] filename, int lineNumber, Statement body, Expression condition2) {
 		return new DoStatement(filename, lineNumber, body, condition2);
 	}
 	
 	protected ForeachRangeStatement newForeachRangeStatement(char[] filename, int lineNumber, TOK op, Argument a, Expression aggr, Expression upr, Statement body) {
 		return new ForeachRangeStatement(filename, lineNumber, op, a, aggr, upr, body);
 	}
 	
 	protected ForeachStatement newForeachStatement(char[] filename, int lineNumber, TOK op, Arguments arguments, Expression aggr, Statement body) {
 		return new ForeachStatement(filename, lineNumber, op, arguments, aggr, body);
 	}
 	
 	protected ForStatement newForStatement(char[] filename, int lineNumber, Statement init, Expression condition2, Expression increment, Statement body) {
 		return new ForStatement(filename, lineNumber, init, condition2, increment, body);
 	}
 	
 	protected GotoCaseStatement newGotoCaseStatement(char[] filename, int lineNumber, Expression exp) {
 		return new GotoCaseStatement(filename, lineNumber, exp);
 	}
 	
 	protected GotoDefaultStatement newGotoDefaultStatement(char[] filename, int lineNumber) {
 		return new GotoDefaultStatement(filename, lineNumber);
 	}
 	
 	protected LabelStatement newLabelStatement(char[] filename, int lineNumber, IdentifierExp label, Statement s) {
 		return new LabelStatement(filename, lineNumber, label, s);
 	}
 	
 	protected OnScopeStatement newOnScopeStatement(char[] filename, int lineNumber, TOK t2, Statement st) {
 		return new OnScopeStatement(filename, lineNumber, t2, st);
 	}
 	
 	protected PragmaStatement newPragmaStatement(char[] filename, int lineNumber, IdentifierExp ident, Expressions args, Statement body) {
 		return new PragmaStatement(filename, lineNumber, ident, args, body);
 	}
 	
 	protected ScopeStatement newScopeStatement(char[] filename, int lineNumber, Statement statement) {
 		return new ScopeStatement(filename, lineNumber, statement);
 	}
 	
 	protected StaticAssertStatement newStaticAssertStatement(StaticAssert assert1) {
 		return new StaticAssertStatement(assert1);
 	}
 	
 	protected SwitchStatement newSwitchStatement(char[] filename, int lineNumber, Expression condition2, Statement body, boolean isfinal) {
 		return new SwitchStatement(filename, lineNumber, condition2, body, isfinal);
 	}
 	
 	protected SynchronizedStatement newSynchronizedStatement(char[] filename, int lineNumber, Expression exp, Statement body) {
 		return new SynchronizedStatement(filename, lineNumber, exp, body);
 	}
 	
 	protected ThrowStatement newThrowStatement(char[] filename, int lineNumber, Expression exp) {
 		return new ThrowStatement(filename, lineNumber, exp);
 	}
 	
 	protected TryCatchStatement newTryCatchStatement(char[] filename, int lineNumber, Statement body, Array catches) {
 		return new TryCatchStatement(filename, lineNumber, body, catches);
 	}
 	
 	protected TryFinallyStatement newTryFinallyStatement(char[] filename, int lineNumber, Statement s, Statement finalbody, boolean b) {
 		return new TryFinallyStatement(filename, lineNumber, s, finalbody, b);
 	}
 	
 	protected VolatileStatement newVolatileStatement(char[] filename, int lineNumber, Statement s) {
 		return new VolatileStatement(filename, lineNumber, s);
 	}
 	
 	protected WhileStatement newWhileStatement(char[] filename, int lineNumber, Expression condition2, Statement body) {
 		return new WhileStatement(filename, lineNumber, condition2, body);
 	}
 	
 	protected WithStatement newWithStatement(char[] filename, int lineNumber, Expression exp, Statement body) {
 		return new WithStatement(filename, lineNumber, exp, body);
 	}
 	
 	protected PragmaDeclaration newPragmaDeclaration(char[] filename, int lineNumber, IdentifierExp ident, Expressions args, Dsymbols a) {
 		return new PragmaDeclaration(filename, lineNumber, ident, args, a);
 	}
 	
 	protected AlignDeclaration newAlignDeclaration(int i, Dsymbols a) {
 		return new AlignDeclaration(i, a);
 	}
 	
 	protected AnonDeclaration newAnonDeclaration(char[] filename, int lineNumber, boolean b, Dsymbols decl) {
 		return new AnonDeclaration(filename, lineNumber, b, decl);
 	}
 	
 	protected CompileDeclaration newCompileDeclaration(char[] filename, int lineNumber, Expression e) {
 		return new CompileDeclaration(filename, lineNumber, e);
 	}
 	
 	protected LinkDeclaration newLinkDeclaration(LINK link, Dsymbols ax) {
 		return new LinkDeclaration(link, ax);
 	}
 	
 	protected AliasDeclaration newAliasDeclaration(char[] filename, int lineNumber, IdentifierExp ident, Type t) {
 		return new AliasDeclaration(filename, lineNumber, ident, t);
 	}
 	
 	protected CtorDeclaration newCtorDeclaration(char[] filename, int lineNumber, Arguments arguments, int i) {
 		return new CtorDeclaration(filename, lineNumber, arguments, i);
 	}
 	
 	protected DeleteDeclaration newDeleteDeclaration(char[] filename, int lineNumber, Arguments arguments) {
 		return new DeleteDeclaration(filename, lineNumber, arguments);
 	}
 	
 	protected DtorDeclaration newDtorDeclaration(char[] filename, int lineNumber) {
 		return new DtorDeclaration(filename, lineNumber);
 	}
 	
 	protected InvariantDeclaration newInvariantDeclaration(char[] filename, int lineNumber) {
 		return new InvariantDeclaration(filename, lineNumber);
 	}
 	
 	protected NewDeclaration newNewDeclaration(char[] filename, int lineNumber, Arguments arguments, int i) {
 		return new NewDeclaration(filename, lineNumber, arguments, i);
 	}
 	
 	protected PostBlitDeclaration newPostBlitDeclaration(char[] filename, int lineNumber) {
 		return new PostBlitDeclaration(filename, lineNumber);
 	}
 	
 	protected StaticCtorDeclaration newStaticCtorDeclaration(char[] filename, int lineNumber) {
 		return new StaticCtorDeclaration(filename, lineNumber);
 	}
 	
 	protected StaticDtorDeclaration newStaticDtorDeclaration(char[] filename, int lineNumber) {
 		return new StaticDtorDeclaration(filename, lineNumber);
 	}
 	
 	protected UnitTestDeclaration newUnitTestDeclaration(char[] filename, int lineNumber) {
 		return new UnitTestDeclaration(filename, lineNumber);
 	}
 	
 	protected TypedefDeclaration newTypedefDeclaration(char[] filename, int lineNumber, IdentifierExp ident, Type t, Initializer init) {
 		return new TypedefDeclaration(filename, lineNumber, ident, t, init);
 	}
 	
 	protected EnumMember newEnumMember(char[] filename, int lineNumber, IdentifierExp ident, Expression value) {
 		return new EnumMember(filename, lineNumber, ident, value);
 	}
 	
 	protected EnumMember newEnumMember(char[] filename, int lineNumber, IdentifierExp exp, Expression value, Type type) {
 		return new EnumMember(filename, lineNumber, exp, value, type);
 	}
 	
 	protected StaticAssert newStaticAssert(char[] filename, int lineNumber, Expression exp, Expression msg) {
 		return new StaticAssert(filename, lineNumber, exp, msg);
 	}
 	
 	protected Version newVersion(char[] filename, int lineNumber, char[] sourceString) {
 		return new Version(filename, lineNumber, sourceString);
 	}
 	
 	protected EnumDeclaration newEnumDeclaration(char[] filename, int lineNumber, IdentifierExp id, Type t) {
 		return new EnumDeclaration(filename, lineNumber, id, t);
 	}
 	
 	protected TemplateDeclaration newTemplateDeclaration(char[] filename, int lineNumber, IdentifierExp ident, TemplateParameters tpl, Expression constraint, Dsymbols decldefs) {
 		return new TemplateDeclaration(filename, lineNumber, ident, tpl, constraint, decldefs);
 	}
 	
 	protected AliasThis newAliasThis(char[] filename, int lineNumber, IdentifierExp id) {
 		return new AliasThis(filename, lineNumber, id);
 	}
 	
 	protected SliceExp newSliceExp(char[] filename, int lineNumber, Expression e, Expression lwr, Expression upr) {
 		return new SliceExp(filename, lineNumber, e, lwr, upr);
 	}
 	
 	protected Import addImportAlias(Import s, IdentifierExp name, IdentifierExp alias) {
 		if (name != null || alias != null) {
 			s.addAlias(name, alias);
 		}
 		return s;
 	}
 	
 	private Token dietSaveToken = new Token(); 
 	
 	Statement dietParseStatement(FuncDeclaration f) {
 		if (diet) {
 			int saveP = p;
 			Token.assign(dietSaveToken, token);
 			
 			inDiet = true;
 			boolean success = dietParse(f);					
 			inDiet = false;
 			
 			if (success) {
 				return null;
 			}
 			
 			p = saveP;
 			Token.assign(token, dietSaveToken);
 		}
 		
 		return parseStatement(PSsemi);
 	}
 	
 	/**
 	 * Tries to skip a function body. The current token is '{'.
 	 * @param f the target function whoes body is being skipped
 	 * @return <code>true</code> if the body could be skipped,
 	 * or <code>false</code> if not, and thus function body
 	 * will be parsed (by the method that invoked this method).
 	 * 
 	 * TODO try doing this at the character level to improve performance further
 	 */
 	protected boolean dietParse(FuncDeclaration f) {
 		int curlyCount = 1;
 		int parenCount = 0;
 		TOK save = null;
 		TOK prev;
 		TOK tok = token.value;
 		do {
 			prev = tok;
 			tok = nextToken();
 			switch(tok) {
 			// Any of this implies an inner-declaration
 			case TOKclass:
 			case TOKinterface:
 			case TOKstruct:
 			case TOKunion:
 				return false;
 				
 			// Parenthesis counting:
 			// If we found a parenthesis for the first time, remember
 			// which token was before it. If after the closing parenthesis
 			// comes '{', we check what that token was. If it is an
 			// identifier, then this is probably an inner-function declaration.
 			case TOKlparen:
 				if (parenCount == 0) {
 					save = prev;
 				}
 				parenCount++;
 				break;
 			case TOKrparen:
 				parenCount--;
 				if (parenCount == 0) {
 					if (save == TOKidentifier &&
 						peek(token).value == TOK.TOKlcurly) {
 						return false;
 					}
 				}
 				break;
 			
 			// Curly counting
 			case TOKlcurly:
 				curlyCount++;
 				break;
 			case TOKrcurly:
 				curlyCount--;
 				break;
 			}
 		} while(curlyCount != 0 && tok != TOK.TOKeof);
 		
 		inDiet = false;
 		
 		nextToken();
 		
 		return true;
 	}
 	
 	/**
 	 * The parser expects the current token to be
 	 * any of given tokens. Does nothing. Subclases may override.
 	 */
 	protected void expect(char[][] tok) {
 		
 	}
 	
 	/**
 	 * The current token is a comment.
 	 *
 	 */
 	protected void inComment() {
 	}
 	
 	private final void setPreComments(ASTDmdNode node, List<Comment> comms) {
 		if (this.module != null)
 			this.module.setPreComments(node, comms);
 	}
 	
 	private final void setPostComment(ASTDmdNode node, Comment comment) {
 		if (this.module != null)
 			module.setPostComment(node, comment);
 	}
 	
 	public final void setModifiers(ASTDmdNode node, List<Modifier> mods) {
 		if (this.module != null)
 			module.setModifiers(node, mods);
 	}
 	
 	private final List<Modifier> getModifiers(ASTDmdNode node) {
 		if (module == null)
 			return null;
 		return module.getModifiers(node);
 	}
 	
 	private int uniqueIdCount = 0;
 	private IdentifierExp uniqueId(String prefix) {
 		return uniqueId(prefix, uniqueIdCount++);
 	}
 	
 	private IdentifierExp uniqueId(String prefix, int i) {
 		return new IdentifierExp((prefix + i).toCharArray());
 	}
 	
 }
