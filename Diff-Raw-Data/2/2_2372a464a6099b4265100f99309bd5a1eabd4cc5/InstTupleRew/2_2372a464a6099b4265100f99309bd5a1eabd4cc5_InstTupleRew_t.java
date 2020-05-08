 package javaworld;
 
 import java.util.Map;
 
 import AST.TypeDecl;
 import AST.ClassDecl;
 import AST.PTInstTuple;
 import AST.PTDummyRename;
 import AST.InterfaceDecl;
 import AST.PTEnumDecl;
 import AST.BodyDecl;
 import AST.ASTNode;
 import AST.PTMethodRename;
 import AST.PTMethodRenameAll;
 import AST.PTFieldRename;
 import AST.Access;
 import AST.MethodDecl;
 import AST.SimpleSet;
 import AST.RequiredType;
 import AST.FieldDeclaration;
 import AST.PTDecl;
 
 
 import java.util.Iterator;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 
 import com.google.common.collect.Maps;
 import com.google.common.base.Joiner;
 
 class InstTupleRew {
 
 	private final PTInstTuple instantiator;
 
 	public InstTupleRew(PTInstTuple dummy) {
 		this.instantiator = dummy;
 	}
 
     protected boolean isInterface() {
         return instantiator.getOriginator() instanceof AST.InterfaceDecl;
     }
 
     protected static TypeDecl lookupUnambiguousTypeIn( TypeDecl root, String name ) {
         SimpleSet matches = root.lookupType( name );
         if( matches.size() != 1 ) {
             throw new RuntimeException( "unexpectedly unable to find supposedly unambiguous type -- " + name + "(" + matches.size() + " matches)" );
         }
         return (TypeDecl) matches.iterator().next();
     }
 
     public void createVirtualRenamingDeclarations( Map<BodyDecl, BodyDecl> virtualsToReals ) {
         Map<ASTNode, String> internalRenames = findInternalRenames( instantiator.getOriginator() );
         PTDecl parentPTDecl = (PTDecl) instantiator.getParentClass( PTDecl.class );
         TypeDecl parentType = parentPTDecl.ptLookupSpecificType( instantiator.getID() );
 
         for( ASTNode originalNode : internalRenames.keySet() ) {
             String newID = internalRenames.get( originalNode );
             ASTNode dummyCopy = originalNode.fullCopy();
             BodyDecl dummyDecl = (BodyDecl) dummyCopy; // justified since it's a copy of == BodyDecl
             parentType.addBodyDecl( dummyDecl );
 
             BodyDecl realThing = null;
 
             if( dummyCopy instanceof MethodDecl ) {
                 MethodDecl copycopy = (MethodDecl) dummyCopy.fullCopy();
                 copycopy.setParent(dummyCopy.getParent());
                 copycopy.setID( newID );
                 realThing = (BodyDecl) parentType.localMethodsSignatureMap().get( copycopy.signature() );
             }
 
             virtualsToReals.put( dummyDecl, realThing );
         }
     }
 
     protected HashMap<ASTNode,String> findInternalRenames(TypeDecl target) {
         // XXX what about types that can only be found in the parent, are these renamed correctly?
         // do any such exist? is the parent always ptdecl?
 
         HashMap< ASTNode, String > rv = new LinkedHashMap< ASTNode, String >();
 
         for( PTDummyRename ptdr : instantiator.getPTDummyRenameList() ) {
             String originalId = ptdr.getOrgID();
             String destinationId = ptdr.getID();
 
             if( ptdr instanceof PTFieldRename ) {
                 // TODO
             } else if( ptdr instanceof PTMethodRename ) {
                 PTMethodRename ptmr = (PTMethodRename) ptdr;
                 AST.List<Access> args = ptmr.getAccessList();
 
                 boolean foundMethod = false;
                 for( Object declo : target.memberMethods( originalId ) ) {
                     MethodDecl mdecl = (MethodDecl) declo;
                     if( mdecl.arity() != args.getNumChild() ) continue;
                     boolean ok = true;
                     for(int i=0;i<mdecl.arity();i++) {
                         TypeDecl formalParamTypeDecl = mdecl.getParameter(i).type();
                        TypeDecl fPTDinCopy = lookupUnambiguousTypeIn(target, formalParamTypeDecl.name());
                         Access myAcc = args.getChild(i);
 
                         if( myAcc.type() != fPTDinCopy ) {
                             ok = false;
                             continue;
                         }
                     }
 
                     if( ok ) {
                         foundMethod = true;
                         rv.put( mdecl, destinationId );
                     }
                 }
 
                 if( !foundMethod ) {
                     ptmr.error( "cannot find method matching rename: " + ptmr );
                 }
             } else if( ptdr instanceof PTMethodRenameAll ) {
                 for( Object declo : target.memberMethods( originalId ) ) {
                     ASTNode declNode = (ASTNode) declo;
                     rv.put( declNode, destinationId );
                 }
             } else {
                 throw new RuntimeException( "program error -- unexpected PTDummyRename" );
             }
         }
 
         return rv;
     }
 
     protected RequiredType getRenamedSourceRequiredType() {
         TypeDecl x = instantiator.getOriginator();
 
 		RequiredType ext = ((RequiredType)x).fullCopy();
         ext.setParent(x.getParent());
 
         ext.visitRename( instantiator.getInstDecl().getRenamedClasses() );
 		DefinitionsRenamer.renameDefinitions( ext, getExplicitlyRenamedDefinitions());
         ext.renameTypes( instantiator.getInstDecl().getRenamedClasses() );
 
         ext.flushCaches();
 
         return ext;
     }
 
     protected InterfaceDecl getRenamedSourceInterface() {
         TypeDecl x = instantiator.getOriginator();
 
 		InterfaceDecl ext = ((InterfaceDecl)x).fullCopy();
         ext.setParent(x.getParent());
 
 /*
         HashMap<ASTNode,String> internalRenames = findInternalRenames( ext );
 
         ext.visitRenameAccesses( internalRenames );
         ext.visitRenameDeclarations( internalRenames );
         */
 
 //        ext.renameMethods()
 
             // is this a wise way to do this? seems clumsy.
             // renameTypes should evidently NOT automatically visitRename
             //  as well, this breaks several tests -- should investigate why
         ext.visitRename( instantiator.getInstDecl().getRenamedClasses() );
 
 		DefinitionsRenamer.renameDefinitions( ext, getExplicitlyRenamedDefinitions());
 
         ext.renameTypes( instantiator.getInstDecl().getRenamedClasses() );
 
         ext.flushCaches();
 
         return ext;
     }
 
     protected PTEnumDecl getRenamedSourceEnum() {
         // straight rewrite of getRenamedSourceInterface, above concerns apply
 
         TypeDecl x = instantiator.getOriginator();
 		PTEnumDecl ext = ((PTEnumDecl)x).fullCopy();
         ext.setParent(x.getParent());
 
         ext.fixupAfterCopy();
 
             // do we need both?
         ext.visitRename( instantiator.getInstDecl().getRenamedClasses() );
         ext.renameTypes( instantiator.getInstDecl().getRenamedClasses() );
 
         ext.fixupAfterCopy();
 
         return ext;
     }
 
 	protected ClassDeclRew getRenamedSourceClass() {
         TypeDecl x = instantiator.getOriginator();
 
 		ClassDecl ext = ((ClassDecl)x).fullCopy();
         ext.setParent(x.getParent());
         // be aware that after this, going upwards into the parent and then
         // (apparently) backwards into the child will NOT find the copy, but
         // the original child, thus it will not reflect changes.
         // fullCopy sanely copies down but not up, yet this can be an
         // unintended consequencem.. ext.getParent() does NOT simply give some
         // context for ext!
 
         new IntroduceExplicitCastsRewriter().mutate( ext );
         ext.getParentClass( PTDecl.class ).flushCaches();
 
             /* problem:
                 the copy is shallow -- it contains references to types in the original.
                 these are not == to the corresponding ones in the copy.
                 so e.g.:
 
                 class A {
                     int getFoo(A x) { return 42; }
                 }
                     ->
                 class *copy*A {
                     int getFoo( *original*A ) { return 42; }
                 }
             */
 
 
 		ClassDeclRew rewriteClass = new ClassDeclRew(ext, getSourceTemplateName());
 
         HashMap<ASTNode,String> internalRenames = findInternalRenames( ext );
 
         ext.visitRenameAccesses( internalRenames );
 //        ext.visitRenameDeclarations( internalRenames );
 
 		rewriteClass.renameConstructors(instantiator);
 		rewriteClass.renameDefinitions(getExplicitlyRenamedDefinitions());
 		rewriteClass.renameTypes(instantiator.getInstDecl().getRenamedClasses());
 
 		return rewriteClass;
 	}
 
 	private Map<String, String> getExplicitlyRenamedDefinitions() {
 		// TODO addsselfto... move it here!!!
 		Map<String, String> map = Maps.newHashMap();
 		for (PTDummyRename entry : instantiator.getPTDummyRenameList()) {
 			entry.addSelfTo(map);
 		}
 		return map;
 	}
 
 	private String getSourceTemplateName() {
 		return instantiator.getTemplate().getID();
 	}
 
 }
