 /**
  * Copyright (c) 2012 Tobias Boehm.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *    Tobias Boehm - initial API and implementation.
  *    Kavith Thiranga - Refactorings to support new Recommenders API
  */
 
 package org.eclipse.recommenders.codesearch.rcp.index.apidoc;
 
 import static com.google.common.base.Optional.absent;
 import static com.google.common.base.Optional.of;
 import static org.eclipse.recommenders.codesearch.rcp.index.indexer.BindingHelper.getIdentifier;
 import static org.eclipse.recommenders.codesearch.rcp.index.searcher.CodeSearcher.prepareSearchTerm;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.inject.Inject;
 
 import org.apache.lucene.index.Term;
 import org.apache.lucene.search.BooleanClause.Occur;
 import org.apache.lucene.search.BooleanQuery;
 import org.apache.lucene.search.TermQuery;
 import org.eclipse.jdt.core.IField;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.ILocalVariable;
 import org.eclipse.jdt.core.IMember;
 import org.eclipse.jdt.core.IMethod;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.core.dom.ASTNode;
 import org.eclipse.jdt.core.dom.CastExpression;
 import org.eclipse.jdt.core.dom.ClassInstanceCreation;
 import org.eclipse.jdt.core.dom.Expression;
 import org.eclipse.jdt.core.dom.IMethodBinding;
 import org.eclipse.jdt.core.dom.MarkerAnnotation;
 import org.eclipse.jdt.core.dom.MethodDeclaration;
 import org.eclipse.jdt.core.dom.MethodInvocation;
 import org.eclipse.jdt.core.dom.SimpleName;
 import org.eclipse.jdt.core.dom.SimpleType;
 import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
 import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
 import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
 import org.eclipse.jdt.core.dom.SuperMethodInvocation;
 import org.eclipse.jdt.core.dom.TypeDeclaration;
 import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
 import org.eclipse.jdt.internal.corext.dom.LinkedNodeFinder;
 import org.eclipse.recommenders.apidocs.rcp.ApidocProvider;
 import org.eclipse.recommenders.codesearch.rcp.index.Fields;
 import org.eclipse.recommenders.codesearch.rcp.index.indexer.BindingHelper;
 import org.eclipse.recommenders.codesearch.rcp.index.searcher.CodeSearcher;
 import org.eclipse.recommenders.codesearch.rcp.index.searcher.SearchResult;
 import org.eclipse.recommenders.internal.codesearch.rcp.CodesearchIndexModule;
 import org.eclipse.recommenders.internal.codesearch.rcp.CodesearchIndexPlugin;
 import org.eclipse.recommenders.internal.codesearch.rcp.PreferencePage;
 import org.eclipse.recommenders.apidocs.rcp.JavaSelectionSubscriber;
 import org.eclipse.recommenders.rcp.JavaElementSelectionEvent;
 import org.eclipse.recommenders.rcp.JavaElementSelectionEvent.JavaElementSelectionLocation;
 import org.eclipse.recommenders.utils.Pair;
 import org.eclipse.recommenders.rcp.JavaElementResolver;
 import org.eclipse.recommenders.rcp.utils.JdtUtils;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 
 import com.google.common.base.Optional;
 import com.google.common.base.Stopwatch;
 import com.google.common.collect.Lists;
 
 @SuppressWarnings("restriction")
 public class LocalExamplesProvider extends ApidocProvider {
 
     public static final String VAR_USAGE_SEARCH = "variable usage";
     public static final String METHOD_INVOCATION_SEARCH = "similar method calls";
     public static final String USED_ANNOTATION_SEARCH = "annotation usage";
     public static final String EXTENDED_TYPE_SEARCH = "extended type";
     public static final String IMPLEENTED_TYPE_SEARCH = "implemented type";
     public static final String RETURN_TYPE_SEARCH = "return type";
     public static final String CLASS_FIELD_SEARCH = "class variable";
     public static final String METHOD_PARAMETER_SEARCH = "method parameter";
     public static final String CHECKED_EXCEPTION_SEARCH = "checked exception";
     
     private final JavaElementResolver jdtResolver;
     private final CodeSearcher searcher;
     private Stopwatch watch;
     private JavaElementSelectionEvent event;
 
     private MethodDeclaration enclosingMethod;
     private TypeDeclaration enclosingType;
     private SimpleName simpleNode;
     private String varType;
     private ASTNode selectedNode;
     private ASTNode parentNode;
     private String searchType;
     private int maxHits;
 
 
     List<String> searchterms;
     private IType jdtVarType;
 
     @Inject
     public LocalExamplesProvider(final CodeSearcher searcher, final JavaElementResolver jdtResolver) throws IOException
     {
         this.searcher = searcher;
         this.jdtResolver = jdtResolver;
         this.maxHits = CodesearchIndexPlugin.getDefault().getPreferenceStore().getInt(PreferencePage.P_MAX_HITS);
     }
 
     @JavaSelectionSubscriber
     public void onFieldSelection(final IField var, final JavaElementSelectionEvent event, final Composite parent)
             throws IOException, JavaModelException
     {
         clear();
         this.event = event;
         startMeasurement();
         if (!findAstNodes())
         {
             return;
         }
 
         if (!findVariableType(var.getTypeSignature()))
         {
             return;
         }
 
        final BooleanQuery query = createQuery();
         final SearchResult searchResult = searcher.lenientSearch(query, maxHits);
         stopMeasurement();
 
         runSyncInUiThread(new Renderer(searchResult, parent, searchType, varType, watch.toString(), jdtResolver, searchterms));
         
     }
 
     @JavaSelectionSubscriber
     public void onVariableSelection(final ILocalVariable var, final JavaElementSelectionEvent event, final Composite parent)
             throws IOException, JavaModelException
     {
         clear();
         this.event = event;
         startMeasurement();
         if (!findAstNodes()) {
             return;
         }
 
         if (!findVariableType(var.getTypeSignature())) {
             return;
         }
 
        final BooleanQuery query = createQuery();
         final SearchResult searchResults = searcher.lenientSearch(query, maxHits);
         stopMeasurement();
 
         runSyncInUiThread(new Renderer(searchResults, parent, searchType, varType, watch.toString(), jdtResolver, searchterms));
         
     }
     
     @JavaSelectionSubscriber
     public void onTypeSelection(final IType type, final JavaElementSelectionEvent event, final Composite parent)
             throws IOException, JavaModelException
     {
         clear();
         this.event = event;
         
         startMeasurement();
         if (!findAstNodes()) {
             return;
         }
 
         jdtVarType = type;
         varType = jdtResolver.toRecType(type).getIdentifier();
         BooleanQuery query = null;
         
         switch (selectedNode.getNodeType()) {
             case ASTNode.MARKER_ANNOTATION:
             case ASTNode.SINGLE_MEMBER_ANNOTATION:
             case ASTNode.ANNOTATION_TYPE_MEMBER_DECLARATION:
             case ASTNode.ANNOTATION_TYPE_DECLARATION:
                 query = createAnnotationQuery();
                 break;
             case ASTNode.SIMPLE_TYPE:
             case ASTNode.SIMPLE_NAME://Checked exceptions
                 query = createTypeQuery();
                 break;           
             default:
                 break;
         }
         if(query == null){
             return;
         }
         final SearchResult searchResults = searcher.lenientSearch(query, maxHits);
         stopMeasurement();
         
         runSyncInUiThread(new Renderer(searchResults, parent, searchType, varType, watch.toString(), jdtResolver, searchterms));
         
     }
 
     // Catch all
     @JavaSelectionSubscriber
     public void onElementSelection(final IJavaElement element,
             final JavaElementSelectionEvent event, final Composite parent)
             throws IOException, JavaModelException {
         clear();
         this.event = event;
         BooleanQuery query = null;
         startMeasurement();
         
         if (!findAstNodes()) {
             return;
         }
         switch (selectedNode.getNodeType()) {
             case ASTNode.METHOD_INVOCATION:
                 varType = jdtResolver.toRecMethod((IMethod) element).get().getIdentifier();
                 query = createMethodQuery();
                 break;
 
         default:
             break;
         }
         if(query == null){
             return;
         }
         final SearchResult searchResults = searcher.lenientSearch(query, maxHits);
         stopMeasurement();
         
         runSyncInUiThread(new Renderer(searchResults, parent, searchType, varType, watch.toString(), jdtResolver, searchterms));
     }
 
     private boolean findAstNodes()
     {
         final Optional<ASTNode> astNode = event.getSelectedNode();
         if (!astNode.isPresent()) {
             return false;
         }
         
         final ASTNode node = astNode.get();
         if (node.getNodeType() == ASTNode.SIMPLE_NAME) {
             simpleNode = (SimpleName) node;
         }
         
         if(isNameProperty(simpleNode)){
             selectedNode = node.getParent();
         }
         else
         {
             selectedNode = node;            
         }
 
         parentNode = selectedNode.getParent();
 
         for (ASTNode parent = simpleNode; parent != null; parent = parent.getParent())
         {
             if (parent instanceof MethodDeclaration) {
                 enclosingMethod = (MethodDeclaration) parent;
             } else if (parent instanceof TypeDeclaration) {
                 enclosingType = (TypeDeclaration) parent;
                 break;
             }
         }
         
         return simpleNode != null && ((enclosingMethod != null) || (enclosingType != null));
         
     }
 
     private boolean findVariableType(final String typeSignature)
     {
         final Optional<IMethod> method = JdtUtils.resolveMethod(enclosingMethod);
         //final Optional<IType> type = jdtVarType.getDeclaringType()
         if (!method.isPresent()) {
             return false;
         }
 
         final Optional<IType> opt = JdtUtils.findTypeFromSignature(typeSignature, method.get());
         if (!opt.isPresent()) {
             return false;
         }
         jdtVarType = opt.get();
         varType = jdtResolver.toRecType(opt.get()).getIdentifier();
         
         return varType != null;
         
     }
     
     private BooleanQuery createMethodQuery() {
         final BooleanQuery query = new BooleanQuery();
         searchterms = new ArrayList<String>();
         Term term;
         searchType = METHOD_INVOCATION_SEARCH;
         if(!isSearchTypeEnabled())
             return null;
         term = prepareSearchTerm(Fields.USED_METHODS, BindingHelper.getIdentifier((MethodInvocation) selectedNode).get());
         query.add(new TermQuery(term), Occur.MUST);
         searchterms.add(simpleNode.getIdentifier());
         
         return query;
     }
     
     private BooleanQuery createAnnotationQuery()
     {
         final BooleanQuery query = new BooleanQuery();
         searchterms = new ArrayList<String>();
         Term term;
         searchType = USED_ANNOTATION_SEARCH;
         if(!isSearchTypeEnabled())
             return null;
         
         term = prepareSearchTerm(Fields.ANNOTATIONS, BindingHelper.getTypeIdentifier(simpleNode).get());
         query.add(new TermQuery(term), Occur.MUST);
         searchterms.add(simpleNode.getIdentifier());
         
         return query;
     }
     
     private BooleanQuery createTypeQuery()
     {        
         final BooleanQuery query = new BooleanQuery();
         searchterms = new ArrayList<String>();
         
         StructuralPropertyDescriptor location = selectedNode.getLocationInParent();
         switch(parentNode.getNodeType())
         {            
             case ASTNode.TYPE_DECLARATION:
                 if(location == TypeDeclaration.SUPERCLASS_TYPE_PROPERTY)
                 {
                     searchType = EXTENDED_TYPE_SEARCH;
                     if(!isSearchTypeEnabled())
                         return null;
                     
                     Term term = prepareSearchTerm(Fields.ALL_EXTENDED_TYPES, BindingHelper.getTypeIdentifier(simpleNode).get());
                     query.add(new TermQuery(term), Occur.MUST);
                     searchterms.add(simpleNode.getIdentifier());
                 }
                 else if(location == TypeDeclaration.SUPER_INTERFACE_TYPES_PROPERTY)
                 {
                     searchType = IMPLEENTED_TYPE_SEARCH;
                     if(!isSearchTypeEnabled())
                         return null;
                     
                     Term term = prepareSearchTerm(Fields.ALL_IMPLEMENTED_TYPES, BindingHelper.getTypeIdentifier(simpleNode).get());
                     query.add(new TermQuery(term), Occur.MUST);
                     searchterms.add(simpleNode.getIdentifier());
                 }
                 break;
             case ASTNode.METHOD_DECLARATION:                
                if(location == MethodDeclaration.THROWN_EXCEPTIONS_PROPERTY)
                {
                    searchType = CHECKED_EXCEPTION_SEARCH;
                    if(!isSearchTypeEnabled())
                        return null;
                    
                    Term term = prepareSearchTerm(Fields.CHECKED_EXCEPTIONS, BindingHelper.getTypeIdentifier(simpleNode).get());
                    query.add(new TermQuery(term), Occur.MUST);
                    searchterms.add(simpleNode.getIdentifier());                   
                }
                else if(location == MethodDeclaration.RETURN_TYPE2_PROPERTY || location == MethodDeclaration.RETURN_TYPE_PROPERTY)
                {
                    searchType = RETURN_TYPE_SEARCH;
                    if(!isSearchTypeEnabled())
                        return null;
                    
                    Term term = prepareSearchTerm(Fields.RETURN_TYPE, BindingHelper.getTypeIdentifier(simpleNode).get());
                    query.add(new TermQuery(term), Occur.MUST);
                    searchterms.add(simpleNode.getIdentifier());    
                }
                break;
             case ASTNode.SINGLE_VARIABLE_DECLARATION:
                 // Method Parameter Declaration
                 if (parentNode.getLocationInParent() == MethodDeclaration.PARAMETERS_PROPERTY) {
                     searchType = METHOD_PARAMETER_SEARCH;
                     if(!isSearchTypeEnabled())
                         return null;
                     
                     Term term = prepareSearchTerm(
                             Fields.PARAMETER_TYPES, BindingHelper
                                     .getTypeIdentifier(simpleNode).get());
                     query.add(new TermQuery(term), Occur.MUST);
                     searchterms.add(simpleNode.getIdentifier());
                 }
             case ASTNode.FIELD_DECLARATION:
                 if (parentNode.getLocationInParent() == TypeDeclaration.BODY_DECLARATIONS_PROPERTY) {
                     searchType = CLASS_FIELD_SEARCH;
                     if(!isSearchTypeEnabled())
                         return null;
                     
                     Term term = prepareSearchTerm(
                             Fields.FIELD_TYPE, BindingHelper
                                     .getTypeIdentifier(simpleNode).get());
                     query.add(new TermQuery(term), Occur.MUST);
                     searchterms.add(simpleNode.getIdentifier());
                 }
 
         }
         
         return query;
     }
 
    private BooleanQuery createQuery()
     {
         // TODO: cleanup needed
         searchType = VAR_USAGE_SEARCH;
         if(!isSearchTypeEnabled())
             return null;
         final BooleanQuery query = new BooleanQuery();
         final Term typeTerm = prepareSearchTerm(Fields.VARIABLE_TYPE, varType);
         final TermQuery typeQuery = new TermQuery(typeTerm);
         query.add(typeQuery, Occur.MUST);
         searchterms = Lists.newArrayList();
         searchterms.add(simpleNode.getIdentifier());
         searchterms.add(jdtVarType.getElementName());
 
         for (final SimpleName use : LinkedNodeFinder.findByNode(enclosingMethod, simpleNode)) {
 
             final ASTNode astParent = use.getParent();
             Term term = null;
             switch (astParent.getNodeType()) {
             case ASTNode.CLASS_INSTANCE_CREATION: {
                 final ClassInstanceCreation targetMethod = (ClassInstanceCreation) astParent;
                 final IMethodBinding methodBinding = targetMethod.resolveConstructorBinding();
                 final Optional<String> optMethod = BindingHelper.getIdentifier(methodBinding);
                 if (!optMethod.isPresent()) {
                     break;
                 }
                 // matches more than the method itself, but that'S a minor thing
                 searchterms.add(targetMethod.getType().toString());
                 if (isUsedInArguments(use, targetMethod.arguments())) {
                     term = prepareSearchTerm(Fields.USED_AS_TAGET_FOR_METHODS, optMethod.get());
                 } else {
                     term = prepareSearchTerm(Fields.USED_AS_TAGET_FOR_METHODS, optMethod.get());
                 }
                 break;
             }
             case ASTNode.METHOD_INVOCATION:
                 final MethodInvocation targetMethod = (MethodInvocation) astParent;
                 final IMethodBinding methodBinding = targetMethod.resolveMethodBinding();
                 final Optional<String> optMethod = BindingHelper.getIdentifier(methodBinding);
                 if (!optMethod.isPresent()) {
                     break;
                 }
                 searchterms.add(targetMethod.getName().toString());
                 if (isUsedInArguments(use, targetMethod.arguments())) {
                     term = prepareSearchTerm(Fields.USED_AS_TAGET_FOR_METHODS, optMethod.get());
                 } else {
                     term = prepareSearchTerm(Fields.USED_AS_TAGET_FOR_METHODS, optMethod.get());
                 }
                 break;
             case ASTNode.SINGLE_VARIABLE_DECLARATION:
                 term = prepareSearchTerm(Fields.VARIABLE_DEFINITION, Fields.DEFINITION_PARAMETER);
                 break;
             case ASTNode.VARIABLE_DECLARATION_FRAGMENT:
                 final VariableDeclarationFragment declParent = (VariableDeclarationFragment) use.getParent();
 
                 final Expression initializer = declParent.getInitializer();
                 Optional<Pair<IMethod, String>> def = absent();
                 if (initializer == null) {
                     term = prepareSearchTerm(Fields.VARIABLE_DEFINITION, Fields.DEFINITION_UNINITIALIZED);
                     break;
                 } else {
 
                     switch (initializer.getNodeType()) {
                     case ASTNode.NULL_LITERAL:
                         term = prepareSearchTerm(Fields.VARIABLE_DEFINITION, Fields.DEFINITION_NULLLITERAL);
                         break;
                     case ASTNode.SUPER_METHOD_INVOCATION:
                         term = prepareSearchTerm(Fields.VARIABLE_DEFINITION, Fields.DEFINITION_ASSIGNMENT);
                         def = findMethod((SuperMethodInvocation) initializer);
                         break;
                     case ASTNode.METHOD_INVOCATION:
                         term = prepareSearchTerm(Fields.VARIABLE_DEFINITION, Fields.DEFINITION_ASSIGNMENT);
                         def = findMethod((MethodInvocation) initializer);
                         break;
                     case ASTNode.CLASS_INSTANCE_CREATION: {
                         term = prepareSearchTerm(Fields.VARIABLE_DEFINITION, Fields.DEFINITION_INSTANCE_CREATION);
                         def = findMethod((ClassInstanceCreation) initializer);
                         break;
                     }
 
                     case ASTNode.CAST_EXPRESSION:
                         // look more deeply into this here:
                         final Expression expression = ((CastExpression) initializer).getExpression();
 
                         switch (expression.getNodeType()) {
                         case ASTNode.METHOD_INVOCATION:
                             def = findMethod((MethodInvocation) expression);
                             break;
                         case ASTNode.SUPER_METHOD_INVOCATION:
                             def = findMethod((SuperMethodInvocation) expression);
                             break;
                         }
                     }
                     if (def.isPresent()) {
                         searchterms.add(def.get().getFirst().getElementName());
                         final TermQuery subquery = new TermQuery(prepareSearchTerm(Fields.VARIABLE_DEFINITION, def
                                 .get().getSecond()));
                         subquery.setBoost(2);
                         query.add(subquery, Occur.SHOULD);
                     }
                 }
                 break;
             default:
                 break;
             }
             if (term != null) {
                 query.add(new TermQuery(term), Occur.SHOULD);
             }
 
         }
         return query;
     }
 
     private static Optional<Pair<IMethod, String>> findMethod(final MethodInvocation s) {
         return findMethod(s.resolveMethodBinding());
     }
 
     private static Optional<Pair<IMethod, String>> findMethod(final SuperMethodInvocation s) {
         return findMethod(s.resolveMethodBinding());
     }
 
 //    private static Optional<Tuple<IMethod, String>> findMethod(final ConstructorInvocation s) {
 //        return findMethod(s.resolveConstructorBinding());
 //    }
 
     private static Optional<Pair<IMethod, String>> findMethod(final ClassInstanceCreation s) {
         return findMethod(s.resolveConstructorBinding());
     }
 
     private static Optional<Pair<IMethod, String>> findMethod(final IMethodBinding b) {
         if (b == null) {
             return absent();
         }
         final IMethod method = (IMethod) b.getJavaElement();
         final Optional<String> opt = getIdentifier(b);
         if (method == null || !opt.isPresent()) {
             return absent();
         }
         return of(Pair.newPair(method, opt.get()));
     }
 
     private boolean isUsedInArguments(final SimpleName uses, @SuppressWarnings("rawtypes") final List arguments) {
         return arguments.size() == 0 || arguments.indexOf(uses) == -1;
     }
 
     private void startMeasurement() {
         watch = new Stopwatch();
         watch.start();
     }
 
     private void stopMeasurement() {
         watch.stop();
     }
     private boolean isNameProperty(SimpleName node){
         StructuralPropertyDescriptor loc = node.getLocationInParent();
         return node.isDeclaration() || (loc == SimpleType.NAME_PROPERTY) || (loc == MarkerAnnotation.TYPE_NAME_PROPERTY)
                 || (loc == SingleMemberAnnotation.TYPE_NAME_PROPERTY) || (loc == MethodInvocation.NAME_PROPERTY);
     }
     private boolean isSearchTypeEnabled(){
         return CodesearchIndexPlugin.getDefault().getPreferenceStore().getBoolean(searchType);
     }
     private void clear() {
         event = null;
         enclosingMethod = null;
         enclosingType = null;
         simpleNode = null;
         varType = null;
         searchterms = null;
         jdtVarType = null;
         selectedNode=null;
         selectedNode=null;
         maxHits = CodesearchIndexPlugin.getDefault().getPreferenceStore().getInt(PreferencePage.P_MAX_HITS);
     }
 }
