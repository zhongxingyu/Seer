 package org.strategoxt.imp.testing;
 
 import static org.spoofax.interpreter.core.Tools.asJavaString;
 import static org.spoofax.interpreter.core.Tools.isTermAppl;
 import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getLeftToken;
 import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getRightToken;
 import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getTokenizer;
 import static org.spoofax.terms.Term.termAt;
 import static org.spoofax.terms.Term.tryGetConstructor;
 
 import java.io.IOException;
 import java.util.Iterator;
 
 import org.eclipse.imp.language.Language;
 import org.eclipse.imp.language.LanguageRegistry;
 import org.spoofax.interpreter.terms.IStrategoAppl;
 import org.spoofax.interpreter.terms.IStrategoConstructor;
 import org.spoofax.interpreter.terms.IStrategoList;
 import org.spoofax.interpreter.terms.IStrategoString;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 import org.spoofax.interpreter.terms.ITermFactory;
 import org.spoofax.jsglr.client.imploder.ImploderAttachment;
 import org.spoofax.jsglr.client.imploder.Tokenizer;
 import org.spoofax.jsglr.shared.BadTokenException;
 import org.spoofax.jsglr.shared.SGLRException;
 import org.spoofax.jsglr.shared.TokenExpectedException;
 import org.spoofax.terms.StrategoListIterator;
 import org.spoofax.terms.TermTransformer;
 import org.spoofax.terms.TermVisitor;
 import org.spoofax.terms.attachments.ParentAttachment;
 import org.spoofax.terms.attachments.ParentTermFactory;
 import org.strategoxt.imp.runtime.Debug;
 import org.strategoxt.imp.runtime.Environment;
 import org.strategoxt.imp.runtime.dynamicloading.Descriptor;
 import org.strategoxt.imp.runtime.parser.JSGLRI;
 
 public class SpoofaxTestingJSGLRI extends JSGLRI {
 	
 	private static final int PARSE_TIMEOUT = 20 * 1000;
 	
 	private static final IStrategoConstructor INPUT_4 =
 		Environment.getTermFactory().makeConstructor("Input", 4);
 	
 	private static final IStrategoConstructor OUTPUT_4 =
 		Environment.getTermFactory().makeConstructor("Output", 4);
 	
 	private static final IStrategoConstructor ERROR_1 =
 		Environment.getTermFactory().makeConstructor("Error", 1);
 	
 	private static final IStrategoConstructor LANGUAGE_1 =
 		Environment.getTermFactory().makeConstructor("Language", 1);
 	
 	private final FragmentParser fragmentParser = new FragmentParser();
 	
 	private final SelectionFetcher selections = new SelectionFetcher();
 
 	public SpoofaxTestingJSGLRI(JSGLRI template) {
 		super(template.getParseTable(), template.getStartSymbol(), template.getController());
 		setTimeout(PARSE_TIMEOUT);
 		setUseRecovery(true);
 	}
 	
 	@Override
 	protected IStrategoTerm doParse(String input, String filename)
 			throws TokenExpectedException, BadTokenException, SGLRException,
 			IOException {
 
 		IStrategoTerm ast = super.doParse(input, filename);
 		return parseTestedFragments(ast);
 	}
 
 	private IStrategoTerm parseTestedFragments(final IStrategoTerm root) {
 		final Tokenizer oldTokenizer = (Tokenizer) getTokenizer(root);
 		final Retokenizer retokenizer = new Retokenizer(oldTokenizer);
 		final ITermFactory nonParentFactory = Environment.getTermFactory();
 		final ITermFactory factory = new ParentTermFactory(nonParentFactory);
 		final FragmentParser testedParser = getFragmentParser(root);
 		assert !(nonParentFactory instanceof ParentTermFactory);
 		if (testedParser == null || !testedParser.isInitialized())
 			return root;
 		
 		IStrategoTerm result = new TermTransformer(factory, true) {
 			@Override
 			public IStrategoTerm preTransform(IStrategoTerm term) {
 				IStrategoConstructor cons = tryGetConstructor(term);
 				if (cons == INPUT_4 || cons == OUTPUT_4) {
 					IStrategoTerm fragmentHead = termAt(term, 1);
 					IStrategoTerm fragmentTail = termAt(term, 2);
 					retokenizer.copyTokensUpToIndex(getLeftToken(fragmentHead).getIndex() - 1);
 					try {
 						IStrategoTerm parsed = testedParser.parse(oldTokenizer, term, cons == OUTPUT_4);
 						int oldFragmentEndIndex = getRightToken(fragmentTail).getIndex();
 						retokenizer.copyTokensFromFragment(fragmentHead, fragmentTail, parsed,
 								getLeftToken(fragmentHead).getStartOffset(), getRightToken(fragmentTail).getEndOffset());
 						if (!testedParser.isLastSyntaxCorrect())
							parsed = nonParentFactory.makeAppl(ERROR_1, parsed);
 						ImploderAttachment implodement = ImploderAttachment.get(term);
 						IStrategoList selected = selections.fetch(parsed);
 						term = factory.annotateTerm(term, nonParentFactory.makeListCons(parsed, selected));
 						term.putAttachment(implodement.clone());
 						retokenizer.skipTokensUpToIndex(oldFragmentEndIndex);
 					} catch (IOException e) {
 						Debug.log("Could not parse tested code fragment", e);
 					} catch (SGLRException e) {
 						Debug.log("Could not parse tested code fragment", e);
 					} catch (CloneNotSupportedException e) {
 						Environment.logException("Could not parse tested code fragment", e);
 					} catch (RuntimeException e) {
 						Environment.logException("Could not parse tested code fragment", e);
 					}
 				}
 				return term;
 			}
 			
 			@Override
 			public IStrategoTerm postTransform(IStrategoTerm term) {
 				Iterator<IStrategoTerm> iterator = TermVisitor.tryGetListIterator(term); 
 				for (int i = 0, max = term.getSubtermCount(); i < max; i++) {
 					IStrategoTerm kid = iterator == null ? term.getSubterm(i) : iterator.next();
 					ParentAttachment.putParent(kid, term, null);
 				}
 				return term;
 			}
 		}.transform(root);
 		retokenizer.copyTokensAfterFragments();
 		retokenizer.getTokenizer().setAst(result);
 		retokenizer.getTokenizer().initAstNodeBinding();
 		return result;
 	}
 	
 	private FragmentParser getFragmentParser(IStrategoTerm root) {
 		Language language = getLanguage(root);
 		if (language == null) return null;
 		Descriptor descriptor = Environment.getDescriptor(language);
 		fragmentParser.configure(descriptor, getController().getRelativePath(), getController().getProject(), root);
 		return fragmentParser;
 	}
 
 	private Language getLanguage(IStrategoTerm root) {
 		if (isTermAppl(root) && "EmptyFile".equals(((IStrategoAppl) root).getName()))
 			return null;
 		IStrategoList headers = termAt(root, 0);
 		for (IStrategoTerm header : StrategoListIterator.iterable(headers)) {
 			if (tryGetConstructor(header) == LANGUAGE_1) {
 				IStrategoString name = termAt(header, 0);
 				return LanguageRegistry.findLanguage(asJavaString(name));
 			}
 		}
 		return null;
 	}
 
 }
