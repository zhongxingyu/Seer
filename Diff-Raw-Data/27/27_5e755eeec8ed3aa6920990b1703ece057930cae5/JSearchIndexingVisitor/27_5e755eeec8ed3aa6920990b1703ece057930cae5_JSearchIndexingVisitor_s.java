 package org.codemucker.jmutate.ast;
 
 import org.codemucker.jfind.Root;
 import org.codemucker.jfind.RootResource;
 import org.codemucker.jfind.RootVisitor;
 import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
 import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
 import org.eclipse.jdt.core.dom.EnumDeclaration;
 import org.eclipse.jdt.core.dom.FieldDeclaration;
 import org.eclipse.jdt.core.dom.ImportDeclaration;
 import org.eclipse.jdt.core.dom.MethodDeclaration;
 import org.eclipse.jdt.core.dom.TypeDeclaration;
 
 import com.google.common.base.Preconditions;
import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
 import com.orientechnologies.orient.core.record.impl.ODocument;
 
 //TODO:remove existing entries if exists
 public class JSearchIndexingVisitor extends BaseASTVisitor implements RootVisitor {
 	
 	static enum Mode {
 		CREATE,UPDATE;
 	}
 	
	private final OGraphDatabase db;
 	
 	//Reuse docs for faster indexing
 	private ODocument rootDoc = new ODocument(); 
 	private ODocument resourceDoc = new ODocument(); 
 	private ODocument sourceDoc = new ODocument(); 
 	private ODocument typeDoc = new ODocument(); 
 	private ODocument methodDoc = new ODocument(); 
 	private ODocument fieldDoc = new ODocument(); 
 	
 	private Root currentRoot;
 	private RootResource currentResource;
 	private JSourceFile currentSource;
 	
 	private int saveCount = 0;
 	
 	private final JAstParser parser;
 	
 	private static final String JAVA_EXTENSION = "java";
 		
	public JSearchIndexingVisitor(OGraphDatabase db, JAstParser parser){
 		this(db,parser, Mode.CREATE);
 	}
 	
	private JSearchIndexingVisitor(OGraphDatabase db, JAstParser parser, JSearchIndexingVisitor.Mode mode){
 		Preconditions.checkNotNull(db,"db");
 		Preconditions.checkNotNull(parser,"parser");
 		this.db = db;
 		this.parser = parser;
 	}
 	
 	@Override
 	public boolean visit(Root root) {
 		currentRoot = root;
 		
 		rootDoc.reset();
 		rootDoc.setClassName("Root");
 		rootDoc.field("path", root.getPathName());
 
 		return true;
 	}
 
 	@Override
 	public void endVisit(Root root) {
 		save(rootDoc, root.getPathName());
 		currentRoot = null;
 	}
 
 	@Override
 	public boolean visit(RootResource resource) {
 		currentResource = resource;
 			
 		resourceDoc.reset();
 		resourceDoc.addOwner(rootDoc);
 		resourceDoc.setClassName("Resource");
 		resourceDoc.field("path", resource.getRelPath());
 		
 		if( resource.hasExtension(JAVA_EXTENSION)){
 			JSourceFile source = JSourceFile.fromResource(resource, parser);
 			if( visit(source) ){
 				source.getAstNode().accept(this);
 			}
 			endVisit(source);
 		}
 		return true;
 	}
 
 	@Override
 	public void endVisit(RootResource resource) {
 		save(resourceDoc, resource.getRelPath());
 		currentResource = null;
 	}
 
 	private boolean visit(JSourceFile source) {
 		currentSource = source;
 		
 		sourceDoc.reset();
 		sourceDoc.addOwner(resourceDoc);
 		sourceDoc.setClassName("Source");
 		sourceDoc.field("root", currentRoot.getPathName());	
 		sourceDoc.field("resource", currentResource.getRelPath());
 		sourceDoc.field("classname", source.getClassnameBasedOnPath());
 	
 		return true;
 	}
 
 	private void endVisit(JSourceFile source) {
 		save(sourceDoc, source.getClassnameBasedOnPath());
 		currentSource = null;
 	}
 	
 	@Override
 	public boolean visit(AnnotationTypeDeclaration node) {
 		indexType(JType.from(node));
 		return true;
 	}
 
 	@Override
 	public boolean visit(AnonymousClassDeclaration node) {
 		indexType(JType.from(node));
 		return true;
 	}
 
 	@Override
 	public boolean visit(EnumDeclaration node) {
 		indexType(JType.from(node));
 		return true;
 	}
 
 	@Override
 	public boolean visit(TypeDeclaration node) {
 		indexType(JType.from(node));
 		return true;
 	}
 
 	//TODO:types,fields,methods
 	private void indexType(JType type){
 		typeDoc.reset();
 		//typeDoc.addOwner(sourceDoc);
 		typeDoc.setClassName("Type");
 		
 		typeDoc.field("fqdn", type.getFullName());
 		typeDoc.field("resource",currentResource.getRelPath());
 		typeDoc.field("source",currentSource.getClassnameBasedOnPath());
 		
 		typeDoc.field("simpleName", type.getSimpleName());
 		typeDoc.field("package", type.getPackageName());
 		
 		typeDoc.field("isAbstract", type.isAbstract());
 		typeDoc.field("isAnonymnous", type.isAnonymousClass());
 		typeDoc.field("isAnnonation", type.isAnnotation());
 		typeDoc.field("isConcrete", type.isConcreteClass());
 		typeDoc.field("isEnum", type.isEnum());
 		typeDoc.field("isInner", type.isInnerClass());
 		typeDoc.field("isInterface", type.isInterface());
 		typeDoc.field("isTop", type.isTopLevelClass());
 		setModifiersFlags(typeDoc, type.getModifiers());
 
 		save(typeDoc, type.getFullName());
 	}
 
 	@Override
 	public boolean visit(ImportDeclaration node) {
 		return super.visit(node);
 	}
 	
 	@Override
 	public boolean visit(MethodDeclaration node) {
 		JMethod method = JMethod.from(node);
 		methodDoc.reset();
 			
 		//methodDoc.addOwner(typeDoc);
 		methodDoc.setClassName("Method");
 		//methodDoc.field("fqdn", type.getFullName());
 		methodDoc.field("resource", currentResource.getRelPath());
 		methodDoc.field("name", method.getName());
 		methodDoc.field("numArgs", method.getParameters().size());
 		methodDoc.field("isConstructor", method.isConstructor());
 		methodDoc.field("isVoid", method.isVoid());
 		
 		//hasReturns
 		//Simple patterns:
 		//	isGetter
 		//	isSetter			
 		setModifiersFlags(methodDoc, method.getJavaModifiers());
 
 		return true;
 	}
 	@Override
 	public void endVisit(MethodDeclaration node) {
 		save(methodDoc, node.getName().getIdentifier());
 	}
 	
 
 	@Override
 	public boolean visit(FieldDeclaration node) {
 		JField field = JField.from(node);
 		//fieldDoc.addOwner(typeDoc);
 		fieldDoc.setClassName("Field");
 		for(String name:field.getNames()){
 			fieldDoc.field("name", name);
 		}
 		setModifiersFlags(fieldDoc, field.getJavaModifiers());
 
 		save(fieldDoc, field.getName());
 		
 		return true;
 	}
 
 	private void save(ODocument doc, String id){
 		saveCount++;
 		System.out.println( "JSearchEngineIndexer:" + saveCount +" saving " + doc.getClassName() + ":" + id);
 		//TODO:add listener here for extension/
 		db.save(doc);
 	}
 	
 	private void setModifiersFlags(ODocument doc, JModifiers mods){
 		doc.field("access", mods.asAccess().name());
 		doc.field("isFinal", mods.isFinal());
 		doc.field("isStatic", mods.isStatic());
 		doc.field("isSynchronized", mods.isSynchronized());
 	}
 	
 	
 }
