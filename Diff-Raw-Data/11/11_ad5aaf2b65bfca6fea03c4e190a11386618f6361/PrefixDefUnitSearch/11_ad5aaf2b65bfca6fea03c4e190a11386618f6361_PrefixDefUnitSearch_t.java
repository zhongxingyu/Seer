 package dtool.refmodel;
 
 import static melnorme.utilbox.core.Assert.AssertNamespace.assertFail;
 import static melnorme.utilbox.core.Assert.AssertNamespace.assertNotNull;
 import static melnorme.utilbox.core.Assert.AssertNamespace.assertTrue;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.eclipse.dltk.core.ISourceModule;
 
 import descent.internal.compiler.parser.TOK;
 import descent.internal.compiler.parser.Token;
 import descent.internal.compiler.parser.ast.TokenUtil;
 import dtool.ast.ASTNeoNode;
 import dtool.ast.ASTNodeFinder;
 import dtool.ast.declarations.DeclarationAttrib;
 import dtool.ast.definitions.DefUnit;
 import dtool.ast.definitions.Module;
 import dtool.ast.expressions.Expression;
 import dtool.ast.references.CommonRefQualified;
 import dtool.ast.references.NamedReference;
 import dtool.ast.references.RefIdentifier;
 import dtool.ast.references.RefImportSelection;
 import dtool.ast.references.RefModule;
 import dtool.ast.references.Reference;
 import dtool.contentassist.CompletionSession;
 import dtool.contentassist.CompletionSession.ECompletionSessionResults;
 import dtool.descentadapter.DescentASTConverter;
 
 /** 
  * Class that does a scoped name lookup for matches that start with a given prefix name. 
  * TODO: The matches with the same name as matches in a scope with higher 
  * priority are not added.
  */
 public class PrefixDefUnitSearch extends CommonDefUnitSearch {
 	
 	public PrefixSearchOptions searchOptions;
 	private IDefUnitMatchAccepter defUnitAccepter;
 	
 	private Set<String> addedDefUnits = new HashSet<String>();
 	
 	public PrefixDefUnitSearch(PrefixSearchOptions searchOptions, IScopeNode refScope, int refOffset,
 			IDefUnitMatchAccepter defUnitAccepter) {
 		super(refScope, refOffset);
 		this.searchOptions = searchOptions;
 		this.defUnitAccepter = defUnitAccepter;
 	}
 	
 	@Override
 	public boolean matches(DefUnit defUnit) {
 		return matchesName(defUnit.getName());
 	}
 	
 	@Override
 	public boolean matchesName(String defName) {
 		return defName.startsWith(searchOptions.searchPrefix);
 	}
 	
 	@Override
 	public boolean isFinished() {
 		return false;
 	}
 	
 	@Override
 	public void addMatch(DefUnit defUnit) {
 		if(notOccluded(defUnit)) {
 			defUnitAccepter.accept(defUnit, searchOptions);
 		}
 	}
 	
 	public boolean notOccluded(DefUnit newDefUnit) {
 		String newDefUnitName = newDefUnit.toStringAsElement();
 		
 		if(addedDefUnits.contains(newDefUnitName)) {
 			return false;
 		}
 		addedDefUnits.add(newDefUnitName);
 		return true;
 	};
 	
 	public static interface IDefUnitMatchAccepter {
 		void accept(DefUnit defUnit, PrefixSearchOptions searchOptions);
 	}
 	
 	public static PrefixDefUnitSearch doCompletionSearch(final int offset, ISourceModule moduleUnit, String source,
 			CompletionSession session, IDefUnitMatchAccepter defUnitAccepter) 
 	{
 		assertTrue(offset >= 0 && offset <= source.length());
 		assertTrue(session.errorMsg == null);
 		session.resultCode = ECompletionSessionResults.RESULT_OK;
 		
 		Token tokenList = ParserAdapter.tokenizeSource(source);
 		
 		Token lastTokenNonWS = null;
 		Token lastToken = null;
 		
 		// : Find last non-white token before offset
 		Token newtoken = tokenList;
 		while (newtoken.ptr < offset) {
 			lastToken = newtoken;
 			if(!TokenUtil.isWhiteToken(newtoken.value)) {
 				lastTokenNonWS = newtoken;
 			}
 			
 			newtoken = newtoken.next;
 		}
 		
 		// : Check if completion request is *inside* the token
 		if(lastToken != null && lastToken.ptr < offset && (lastToken.ptr + lastToken.sourceLen) > offset) {
 			// if so then check if it's an allowed token
 			if(!isValidReferenceToken(lastToken)) {
 				CompletionSession.assignResult(session, ECompletionSessionResults.INVALID_LOCATION_INTOKEN, 
 						"Invalid location (inside token)");
 				return null;
 			}
 		}
 		
 		// : Parse source and do syntax error recovery
 		ParserAdapter parserAdapter = ParserAdapter.parseSource(source);
 		parserAdapter.recoverForCompletion(source, offset, lastTokenNonWS);
 		
 		Module neoModule = DescentASTConverter.convertModule(parserAdapter.mod);
 		neoModule.setModuleUnit(moduleUnit);
 		
 		/* ============================================== */
 		// : Do actual completion search
 		
 		
 		ASTNeoNode node = ASTNodeFinder.findElement(neoModule, offset);
 		assertNotNull(node);
 		session.invokeNode = node;
 		PrefixSearchOptions searchOptions = new PrefixSearchOptions();
 		IScopeNode refScope = NodeUtil.getScopeNode(node);
 		PrefixDefUnitSearch search;
 		search = new PrefixDefUnitSearch(searchOptions, refScope, offset, defUnitAccepter);
 		
 		
 		if(node instanceof NamedReference)  {
 			NamedReference namedRef = (NamedReference) node;
 			
 			if(node instanceof RefIdentifier) {
 				RefIdentifier refIdent = (RefIdentifier) node;
 				if(!parserAdapter.isQualifiedDotFixSearch) {
 					setupPrefixedSearchOptions(searchOptions, offset, refIdent.getOffset(), refIdent.name);
 				}
 			} else if(node instanceof CommonRefQualified) {
 				//CommonRefQualified refQual = (CommonRefQualified) node;
 				assertTrue(!parserAdapter.isQualifiedDotFixSearch);
 				
 				if(lastTokenNonWS.value != TOK.TOKdot) {
 					CompletionSession.assignResult(session, ECompletionSessionResults.WEIRD_LOCATION_REFQUAL, 
 							"Invalid Location: before qualifier dot but not next to id.");
 					return search;
 				}
 			} else if(node instanceof RefModule) {
 				RefModule refMod = (RefModule) node;
 				String refModString = refMod.toStringAsElement();
 				if(parserAdapter.isQualifiedDotFixSearch) {
 					refModString = refModString.substring(0, refModString.length() - 1);
 				}
 				setupPrefixedSearchOptions(searchOptions, offset, refMod.getOffset(), refModString);
 			} else if (node instanceof RefImportSelection) {
 				RefImportSelection refImpSel = (RefImportSelection) node;
 				setupPrefixedSearchOptions(searchOptions, offset, refImpSel.getOffset(), refImpSel.name);
 			} else {
 				throw assertFail();
 			}
 			
 			namedRef.doSearch(search);
 		} else if(node instanceof Reference) {
 			CompletionSession.assignResult(session, ECompletionSessionResults.NOTIMPLEMENTED, 
 					"Don't know how to complete for node: "+node+" (DDT TODO)");
 			return search;
 		} else {
 			// Since picked node was not a reference,
 			// determine appropriate scope search parameters
 			IScopeNode scope;
 			while(true) {
 				assertNotNull(node); 
 				scope = isValidCompletionScope(node);
 				if(scope != null)
 					break;
 				
 				if(isInsideNonScopeBlock(node, offset, source)) {
 					scope = NodeUtil.getScopeNode(node);
 					break;
 				}
 				
 				if(offset == node.getStartPos() || offset == node.getEndPos()) {
 					node = node.getParent();
 					session.invokeNode = node;
 				} else {
 					CompletionSession.assignResult(session, ECompletionSessionResults.INVALID_LOCATION_INSCOPE, 
 							"Invalid location in scope");
 					return search;
 				}
 			}
 			
 			ReferenceResolver.findDefUnitInExtendedScope(scope, search);
 		}
 		
 		assertTrue(session.errorMsg == null);
 		return search;
 	}
 	
 	private static boolean isInsideNonScopeBlock(ASTNeoNode node, int offset, String sourceStr) {
 		if(!(node instanceof INonScopedBlock)) {
 			return false;
 		}
 		INonScopedBlock nonScopedBlock = (INonScopedBlock) node;
 		nonScopedBlock.getMembersIterator(); // Need proper way to determine CC context
 		
 		if(node instanceof DeclarationAttrib) {
 			int blockContentsStart = sourceStr.indexOf(":", node.getStartPos());
 			if(blockContentsStart != -1 && offset > blockContentsStart) {
 				return true;
 			} else {
 				return false;
 			}
 		}
 		
 		return true;
 	}
 	
 	private static void setupPrefixedSearchOptions(PrefixSearchOptions searchOptions, 
 			final int offset, int nameOffset, String name) {
 		int prefixLen = offset - nameOffset; 
		assertTrue(prefixLen >= 0);
		if(name.length() < prefixLen) {
			// This case shouldnt happen, but can happen due to parser source range bugs, so workaround
			prefixLen = name.length();
		}
 		searchOptions.prefixLen = prefixLen;
 		searchOptions.rplLen = name.length() - prefixLen;
 		searchOptions.searchPrefix = name.substring(0, prefixLen);
 	}
 	
 	
 	private static IScopeNode isValidCompletionScope(ASTNeoNode node) {
 		if(node instanceof IScopeNode) {
 			return (IScopeNode) node;
 		} else if(node instanceof Expression) {
 			return NodeUtil.getOuterScope(node);
 		} 
 		return null;
 	}
 	
 	private static boolean isValidReferenceToken(Token token) {
 		return token.value == TOK.TOKidentifier
 		|| token.value == TOK.TOKbool
 		|| token.value == TOK.TOKchar
 		|| token.value == TOK.TOKdchar
 		|| token.value == TOK.TOKfloat32
 		|| token.value == TOK.TOKfloat64
 		|| token.value == TOK.TOKfloat80
 		|| token.value == TOK.TOKint8
 		|| token.value == TOK.TOKint16
 		|| token.value == TOK.TOKint32
 		|| token.value == TOK.TOKint64
 		//|| token.value == TOK.TOKnull
 		//|| token.value == TOK.TOKthis
 		//|| token.value == TOK.TOKsuper
 		|| token.value == TOK.TOKuns8
 		|| token.value == TOK.TOKuns16
 		|| token.value == TOK.TOKuns32
 		|| token.value == TOK.TOKuns64
 		|| token.value == TOK.TOKvoid
 		|| token.value == TOK.TOKwchar
 		;
 	}
 	
 }
