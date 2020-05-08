 package javaworld;
 
 import AST.PTDecl;
 import AST.PTPackage;
 import AST.ClassDecl;
 import AST.InterfaceDecl;
 import AST.MethodDecl;
 
 import java.util.Map;
 
 /* The point of this class is the run() method, which mutates/rewrites
    the object PTDecl in-place (it does not create a copy and modify
    that). PTDecl represents either a "template" node or a "package"
    node. */
 
 public class InstantiationRewriter {
 
 	public InstantiationRewriter() {
 	}
 
 	public void run(PTDecl decl) { 
 		
 		// Går igjennom decl sine ClassDecls for å beregne hva som er
 		// "virtuelle" metoder som det siste som skjer før omskrivinga starter.
 		for (ClassDecl c: decl.getClassList()) {
 			c.findVirtualMethods();
 		}
 
         // could we use clone and then _mutate_ the objects we're
         // _copying_ from, to subject them to extensive treatment, e.g.
         // parametrization?
 
         decl.flushCaches();
 
 		PTDeclRew target = new PTDeclRew(decl);
 
         target.getParameterRewriter();
         // this creates the mappings at a well-defined time
 
         target.createRenamedEnums();
         target.createEmptyMissingAddClasses();
         
         target.createMergedInterfaces();
         target.createMergedRequiredTypes();
 
 		target.extendAddClassesWithInstantiatons();
 
         target.updateAccessesToInternalRenames();
 
 		target.copyImportDecls();
 		target.createInitIfPackage();
 
         target.concretifyRequiredTypes();
 
 //        target.removeInstStatements();
 		target.flushCaches();
 
         decl.checkRedundantExtends();
 
         decl.flushCaches();
 
 	}
 }
