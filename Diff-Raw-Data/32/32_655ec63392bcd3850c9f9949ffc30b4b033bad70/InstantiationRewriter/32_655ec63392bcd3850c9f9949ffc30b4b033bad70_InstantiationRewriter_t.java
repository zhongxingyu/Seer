 package javaworld;
 
 import AST.PTDecl;
 import AST.PTInstDecl;
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
     int dumpStage = 1;
     boolean dumpTrees = false;
 
 	public InstantiationRewriter() {
 	}
     
     void dumpTree( PTDecl decl ) {
         if( dumpTrees ) {
             System.out.println( "[debug tree dump " + dumpStage + "] " + decl.dumpTree() );
             dumpStage += 1;
         }
     }
 
 	public void run(PTDecl decl) { 
         System.out.println( "beginning InstantiationRewriter: " + decl.getID() );
 
         dumpTree( decl );
 		
 		// Går igjennom decl sine ClassDecls for å beregne hva som er
 		// "virtuelle" metoder som det siste som skjer før omskrivinga starter.
 		for (ClassDecl c: decl.getClassList()) {
 			c.findVirtualMethods();
 		}
 
         for(PTInstDecl ptid : decl.getPTInstDecls()) {
             System.out.println( "instantiation " + ptid.dumpTree() + " is adopting or has adopted internal name: " + ptid.getInternalName() );
         }
 
         // could we use clone and then _mutate_ the objects we're
         // _copying_ from, to subject them to extensive treatment, e.g.
         // parametrization?
 
         decl.flushCaches();
 
         dumpTree( decl );
 
 		PTDeclRew target = new PTDeclRew(decl);
 
         target.getParameterRewriter();
         // this creates the mappings at a well-defined time
 
         dumpTree( decl );
 
         target.createRenamedEnums();
 
         dumpTree( decl );
 
         target.createEmptyMissingAddClasses();
 
         dumpTree( decl );
 

         target.createMergedInterfaces();
 
         dumpTree( decl );
 
         target.createMergedRequiredTypes();
 
         dumpTree( decl );
 
            target.extendAddClassesWithInstantiatons();

            dumpTree( decl );

            // fields are now present and it should be okay to rewrite ambiguous accesses
            target.markReadyForNameResolution();
            target.flushCaches();

            dumpTree( decl );
 
 
         target.updateAccessesToInternalRenames();
 
         dumpTree( decl );
 
 		target.copyImportDecls();
 
         dumpTree( decl );
 
 		target.createInitIfPackage();
 
         dumpTree( decl );
 
         target.concretifyRequiredTypes();
 
         dumpTree( decl );
 
 //        target.removeInstStatements();
 		target.flushCaches();
 
         dumpTree( decl );
 
         decl.checkRedundantExtends();
 
         dumpTree( decl );
 
         decl.flushCaches();
 
         dumpTree( decl );
 
         System.out.println( "finishing InstantiationRewriter: " + decl.getID() );
 	}
 }
