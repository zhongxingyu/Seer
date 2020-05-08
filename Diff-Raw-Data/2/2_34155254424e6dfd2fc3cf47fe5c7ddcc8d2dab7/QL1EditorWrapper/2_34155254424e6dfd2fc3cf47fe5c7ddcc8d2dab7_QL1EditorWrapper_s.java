 package org.eclipse.recommenders.internal.codesearch.rcp.views;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.lucene.document.Document;
 import org.apache.lucene.index.CorruptIndexException;
 import org.apache.lucene.queryParser.ParseException;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.xtext.resource.XtextResource;
 import org.eclipse.xtext.ui.editor.embedded.EmbeddedEditorFactory;
 import org.eclipse.xtext.ui.editor.embedded.IEditedResourceProvider;
 import org.eclipse.xtext.util.concurrent.IUnitOfWork;
 import org.eclipselabs.recommenders.codesearch.rcp.dslQL1.QL1StandaloneSetup;
 import org.eclipselabs.recommenders.codesearch.rcp.dslQL1.ui.internal.QL1Activator;
 
 import com.google.common.collect.Lists;
 import com.google.inject.Injector;
 
 @SuppressWarnings("restriction")
 public class QL1EditorWrapper extends AbstractEmbeddedEditorWrapper {
 
     @Override
     void createQueryEditorInternal() {
         final IEditedResourceProvider resourceProvider = new IEditedResourceProvider() {
 
             @Override
             public XtextResource createResource() {
                 try {
                     QL1StandaloneSetup.doSetup();
                     final ResourceSet resourceSet = new ResourceSetImpl();
                     final Resource resource = resourceSet.createResource(URI.createURI("embedded.ql1"));
 
                     return (XtextResource) resource;
                 } catch (final Exception e) {
                     return null;
                 }
             }
         };
 
         final QL1Activator activator = QL1Activator.getInstance();
         final Injector injector = activator
                .getInjector(QL1Activator.ORG_ECLIPSELABS_RECOMMENDERS_codesearch_RCP_DSLQL1_QL1);
         final EmbeddedEditorFactory factory = injector.getInstance(EmbeddedEditorFactory.class);
         handle = factory.newEditor(resourceProvider).withParent(parent);
 
         // keep the partialEditor as instance var to read / write the edited
         // text
         partialEditor = handle.createPartialEditor(true);
 
         searchView.setSearchEnabled(false); // No translation to lucene query
                                             // yet :-(
     }
 
     @Override
     List<Document> search() throws ParseException, CorruptIndexException, IOException {
         return Lists.newArrayList();
     }
 
     @Override
     String[] getExampleQueriesInternal() {
         return new String[] {};
     }
 
     public static String getName() {
         return "Query Language 1";
     }
 
     @Override
     IUnitOfWork<Set<String>, XtextResource> getSEarchTermExtractor() {
         return null;
     }
 }
