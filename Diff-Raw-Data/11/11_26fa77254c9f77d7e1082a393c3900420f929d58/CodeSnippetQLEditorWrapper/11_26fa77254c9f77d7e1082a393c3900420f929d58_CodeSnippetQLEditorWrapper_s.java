 package org.eclipse.recommenders.internal.codesearch.rcp.views;
 
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.lucene.search.TopDocs;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.recommenders.codesearch.rcp.index.searcher.SearchResult;
 import org.eclipse.recommenders.codesearch.rcp.index.searcher.SearchResultHelper;
 import org.eclipse.recommenders.codesearch.rcp.index.searcher.converter.DotNotationTypeConverter;
 import org.eclipse.recommenders.codesearch.rcp.index.termvector.FilteredJavaMethodProvider;
 import org.eclipse.recommenders.codesearch.rcp.index.termvector.JavaTypeProvider;
 import org.eclipse.recommenders.codesearch.rcp.searcher.imageProvider.TypeImageProvider;
 import org.eclipse.recommenders.codesearch.rcp.searcher.proposalProvider.GenericQueryProposalProvider;
 import org.eclipse.xtext.parser.IParseResult;
 import org.eclipse.xtext.resource.XtextResource;
 import org.eclipse.xtext.serializer.ISerializer;
 import org.eclipse.xtext.ui.editor.embedded.EmbeddedEditorFactory;
 import org.eclipse.xtext.ui.editor.embedded.IEditedResourceProvider;
 import org.eclipse.xtext.util.concurrent.IUnitOfWork;
 import org.eclipselabs.recommenders.codesearch.rcp.dsl.extractors.LuceneQueryExtractor;
 import org.eclipselabs.recommenders.codesearch.rcp.dsl.extractors.ParseResultExtractor;
 import org.eclipselabs.recommenders.codesearch.rcp.dsl.ui.contentassist.QueryProposalType;
 import org.eclipselabs.recommenders.codesearch.rcp.dsl.ui.internal.LuceneQueryActivator;
 import org.eclipselabs.recommenders.codesearch.rcp.dslQL1.QL1StandaloneSetup;
 import org.eclipselabs.recommenders.codesearch.rcp.dslQL2.QL2QueryExtractor;
 import org.eclipselabs.recommenders.codesearch.rcp.dslQL2.VariableExtractor;
 import org.eclipselabs.recommenders.codesearch.rcp.dslQL2.VariableUsage;
 import org.eclipselabs.recommenders.codesearch.rcp.dslQL2.ui.contentassist.QL2ProposalProvider;
 import org.eclipselabs.recommenders.codesearch.rcp.dslQL2.ui.internal.QL2Activator;
 
 import com.google.common.collect.Lists;
 import com.google.inject.Injector;
 
 @SuppressWarnings("restriction")
 public class CodeSnippetQLEditorWrapper extends AbstractEmbeddedEditorWrapper {
 
     private Injector luceneInjector = null;
 
     public CodeSnippetQLEditorWrapper() {
 
         final LuceneQueryActivator activator = LuceneQueryActivator.getInstance();
         luceneInjector = activator
                 .getInjector(LuceneQueryActivator.ORG_ECLIPSELABS_RECOMMENDERS_CODESEARCH_RCP_DSL_LUCENEQUERY);
     }
 
     @Override
     void createQueryEditorInternal() {
 
         QL2ProposalProvider.addQueryProposalProvider(QueryProposalType.TYPE, new GenericQueryProposalProvider(
                 new JavaTypeProvider(), new DotNotationTypeConverter(), new TypeImageProvider()));
 
         QL2ProposalProvider.addQueryProposalProvider(QueryProposalType.METHOD, new GenericQueryProposalProvider(
                 new FilteredJavaMethodProvider(), new DotNotationTypeConverter(), new TypeImageProvider()));
 
         final IEditedResourceProvider resourceProvider = new IEditedResourceProvider() {
 
             @Override
             public XtextResource createResource() {
                 try {
                     QL1StandaloneSetup.doSetup();
                     final ResourceSet resourceSet = new ResourceSetImpl();
                     final Resource resource = resourceSet.createResource(URI.createURI("embedded.ql2"));
 
                     return (XtextResource) resource;
                 } catch (final Exception e) {
                     return null;
                 }
             }
         };
 
         final QL2Activator activator = QL2Activator.getInstance();
         final Injector injector = activator
                 .getInjector(QL2Activator.ORG_ECLIPSELABS_RECOMMENDERS_CODESEARCH_RCP_DSLQL2_QL2);
         final EmbeddedEditorFactory factory = injector.getInstance(EmbeddedEditorFactory.class);
         handle = factory.newEditor(resourceProvider).withParent(parent);
 
         // keep the partialEditor as instance var to read / write the edited
         // text
         partialEditor = handle.createPartialEditor(true);
     }
 
     @Override
     public SearchResult search() throws Exception {
 
         IParseResult r = handle.getDocument().readOnly(new ParseResultExtractor());
 
         Map<String, VariableUsage> map = new VariableExtractor().getVars(r.getRootASTElement());
 
         List<TopDocs> validScoreDocs = Lists.newArrayList();
         SearchResult result = null;
 
         QL2QueryExtractor extr = new QL2QueryExtractor();
 
         for (int i = 0; i < map.values().size(); i++) {
 
             EObject o = extr.transform((VariableUsage) map.values().toArray()[i]);
 
             LuceneQueryExtractor lextr = new LuceneQueryExtractor();
             lextr.process(o.eAllContents());
 
             ISerializer s = luceneInjector.getInstance(ISerializer.class);
             String searchQuery = s.serialize(o);
 
             System.out.println("Search: " + searchQuery);
             result = codeSearcher.lenientSearch(searchQuery);
 
             validScoreDocs.add(result.docs);
         }
 
         TopDocs l = SearchResultHelper.getIntersection(validScoreDocs, result.searcher);
 
         return new SearchResult(null, l, result.searcher);
     }
 
     @Override
     public String[] getExampleQueriesInternal() {
 
         return new String[] {
                String.format("{%nvar *SomeType X%ncall X.%n}"),
                 String.format("{%nvar java.lang.String X%nvar java.util.List Y%n}"),
                 String.format("{%n//Variable declaration/initialization%nvar A varA = *%nvar B varB = *%n}"),
                 String.format("{%n//Method invocation%nvar java.lang.String varA = *%ncall varA.toString()%n}"),
                 String.format("{%n//Method invocation%nvar org.test.SomeType varA%n%n}"),
                 String.format("{%n//Method proposal%nvar org.test.SomeTestClass varA = *%n//call varA.%n}"),
                 String.format("{%n//Call of static method%nvar *String varA = *%nscall java.lang.String.format(varA)%n}"),
                 String.format("(*List listVar)%n{%n//Different kinds of variable declaration%nvar *String stringVar1 = *%nvar *String stringVar2%nvar *String stringVar3 = null%n}") };
     }
 
     public static String getName() {
         return "Code Snippet QL";
     }
 
     @Override
     IUnitOfWork<Set<String>, XtextResource> getSearchTermExtractor() {
         return null;
     }
 }
