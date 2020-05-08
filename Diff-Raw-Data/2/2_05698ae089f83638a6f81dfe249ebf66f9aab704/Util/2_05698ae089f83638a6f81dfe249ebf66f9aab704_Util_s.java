 package javaworld;
 
 import java.util.NoSuchElementException;
 
 import AST.Expr;
 import AST.List;
 import AST.PTClassDecl;
 import AST.PTInstDecl;
 import AST.TemplateMethodAccess;
 import AST.TemplateMethodAccessShort;
 import AST.ImportDecl;
 import AST.TypeDecl;
 import AST.PTDecl;
 
 
 import com.google.common.base.Preconditions;
 
 public class Util {
 	final static boolean debugMode = true;
 
 	public static String toName(String id) {
 		Preconditions.checkArgument(id != null);
 		return String.format("tsuper[%s]", id);
 	}
 
 	public static String toName(String templateName, String className) {
 		Preconditions.checkArgument(templateName != null && className != null);
 		return toName(templateName + "." + className);
 	}
 
 	public static String toName(String templateName, String className,
 			String methodName) {
 		Preconditions.checkArgument(methodName != null);
 		return String.format("%s.%s", toName(templateName, className),
 				methodName);
 	}
 
 	public static String getName(String methodSignature) {
 		int splitIdx = methodSignature.indexOf('(');
 		return methodSignature.substring(0, splitIdx);
 	}
 
 	/*
 	 * Is called from InstantiationRewrite.jrag
 	 * Try to unambiguously resolve a tsuper call in short from to standard form.
 	 * e.g. tsuper[<ClassID>].f() --> tsuper[<TemplateID>.<ClassID>].f()
 	 */
 	public static TemplateMethodAccess rewriteMethodAccess(
 			TemplateMethodAccessShort from) {
 		String templateID = "";
 		PTClassDecl host = (PTClassDecl) from.getParentClass(PTClassDecl.class);
 		String methodName = from.getID();
 		List<Expr> argList = from.getArgList(); // getArgListNoTransform??
 		String tclassID = from.getTClassID();
 		try {
 			templateID = host.getClassDecl().lookupTemplateForTClass(tclassID);
 		} catch (NoSuchElementException e) {
 			from.error("Unknown template superclass for " + tclassID);
 		} catch (IllegalArgumentException e) {
 			from.error(String.format(
 					"Multiple possible templates with class %s "
 							+ "in template method call %s. msg: ", tclassID,
 					from, e.getMessage()));
 		}
 		return new TemplateMethodAccess(
 				toName(templateID, tclassID, methodName), argList, tclassID,
 				templateID);
 	}
 
     public static String toUniqueMinitName( PTInstDecl instantiation, String oldName ) {
         // System.out.println( "new minit is producing " + instantiation.getInternalName() + oldName );
 
         return instantiation.getInternalName() + oldName;
     }
 
 
 	public static String toMinitName(String templateID, String tclassID) {
 		String rv = String.format("minit$%s$%s", templateID, tclassID);
 
         // System.out.println( "WARNING old minit produced " + rv );
         new Exception().printStackTrace();
 
         return rv;
 	}
 
 	public static String toMinitName(String tclassID) {
         String rv = String.format("minit$%s", tclassID);
 
         // System.out.println( "WARNING old minit produced " + rv );
         new Exception().printStackTrace();
 
 		return rv;
 	}
 
     public static AST.TypeDecl declarationFromTypeAccess( AST.Access a ) {
         // TODO lots of code assuming instanceof TypeAccess should instead call this
         if( a instanceof AST.TypeAccess ) {
             AST.TypeDecl rv = ((AST.TypeAccess)a).decl();
 /* // this is really really really insanely verbose
             try {
                 AST.ASTNode everHigher = a;
                 int i = 1;
                 while( everHigher != null ) {
                     System.out.println( "context" + i + ": " + everHigher.dumpTree() );
                     i++;
                     everHigher = everHigher.getParent();
                 }
             }
             catch( Exception e ) {}
 */
             return rv;
         } else if( a instanceof AST.ParTypeAccess ) {
             AST.ParTypeAccess pta = (AST.ParTypeAccess) a;
             AST.TypeDecl generic = pta.genericDecl();
 
             if( generic instanceof AST.GenericTypeDecl ) {
                 AST.GenericTypeDecl genericDecl = (AST.GenericTypeDecl) generic;
                 AST.TypeDecl lookedUpType = genericDecl.lookupParTypeDecl( pta );
                 AST.ParTypeDecl ptd = (AST.ParTypeDecl) lookedUpType;
 
                 return lookedUpType;
             }
             return generic;
         } else {
             return null; // oops
         }
     }
 
     /* Note: this cannot be done as a rewrite -- Program (the root node in the AST)
        doesn't get rewritten. We need to manually make this happen.
        In fact, even adding methods to Program seems to be a no-op!
 
     public static int getNumberOfPTCompilationUnits( AST.Program program ) {
         int rv = 0;
         for(int i=0;i<program.getNumCompilationUnit();i++) {
             if( program.getCompilationUnit(i) instanceof AST.PTCompilationUnit ) {
                 rv++;
             }
         }
         System.out.println( "number of ptcompilationunits: " + rv );
         return rv;
     }
 
     */
     public static AST.List<AST.CompilationUnit> mergePtCompilationUnits( AST.Program program ) {
 
 /*
             if( Util.getNumberOfPTCompilationUnits( program ) <= 1 ) {
                 return;
             }
 
 */
             String name = "$mergedPTCU$";
             List<ImportDecl> p1 = new List<ImportDecl>();
             List<TypeDecl> p2 = new List<TypeDecl>();
             List<PTDecl> p3 = new List<PTDecl>();
 
             AST.List cul = (AST.List) program.getChildNoTransform(0);
 
             AST.List<AST.CompilationUnit> rv = new AST.List<AST.CompilationUnit>();
             
             for(int i=0;i<cul.getNumChildNoTransform();) {
                 // System.out.println( "child " + i + " is " + cul.getChildNoTransform(i).dumpString() );
                 if( cul.getChildNoTransform(i) instanceof AST.PTCompilationUnit ) {
                     AST.PTCompilationUnit ptcu = (AST.PTCompilationUnit) cul.getChildNoTransform(i);
 
                     /* Warning: iterating through these with foreach _will_ transform! */
 
                     AST.List<AST.ImportDecl> ids = ptcu.getImportDeclListNoTransform();
                     for(int j=0;j<ids.getNumChildNoTransform();j++) {
                         p1 = p1.add( ids.getChildNoTransform(j) );
                     }
                     AST.List<AST.TypeDecl> tds = ptcu.getTypeDeclListNoTransform();
                     for(int j=0;j<tds.getNumChildNoTransform();j++) {
                         p2 = p2.add( tds.getChildNoTransform(j) );
                     }
                     AST.List<AST.PTDecl> pds = ptcu.getPTDeclListNoTransform();
                     for(int j=0;j<pds.getNumChildNoTransform();j++) {
                         p3 = p3.add( pds.getChildNoTransform(j) );
                     }
 
                     cul.removeChild(i);
 
                     rv = rv.add( ptcu );
                 } else {
                     i++;
                 }
             }
 
             AST.PTCompilationUnit ptuc = new AST.PTCompilationUnit( name, p1, p2, p3 );
             ptuc.setFromSource( true );
             cul.addChild( ptuc );
 
             return rv;
     }
 
 }
