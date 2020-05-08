 package cz.cvut.fit.hybljan2.apitestingcg.scanner;
 
 import com.sun.tools.javac.code.Flags;
 import com.sun.tools.javac.code.Symbol.ClassSymbol;
 import com.sun.tools.javac.code.Symbol.MethodSymbol;
 import com.sun.tools.javac.code.Symbol.VarSymbol;
 import com.sun.tools.javac.tree.JCTree.JCClassDecl;
 import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
 import com.sun.tools.javac.tree.JCTree.JCImport;
 import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
 import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
 import com.sun.tools.javac.tree.TreeScanner;
 import cz.cvut.fit.hybljan2.apitestingcg.apimodel.API;
 import cz.cvut.fit.hybljan2.apitestingcg.apimodel.APIClass;
 import cz.cvut.fit.hybljan2.apitestingcg.apimodel.APIField;
 import cz.cvut.fit.hybljan2.apitestingcg.apimodel.APIMethod;
 import cz.cvut.fit.hybljan2.apitestingcg.apimodel.APIPackage;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Stack;
 
 /**
  *
  * @author Jan Hýbl
  */
 public class SourceTreeScanner extends TreeScanner{
     private Map<String,String> currentClassImports = new HashMap<String, String>();
     private API api;
     private APIPackage currentPackage;
     private APIClass currentClass;
     private Stack<APIClass> classes = new Stack<APIClass>();    
     private Map<String, APIPackage> pkgs = new HashMap<String, APIPackage>();    
     
     public API getAPI() {
         api = new API("");
         // Add all packages to API. We don't want default package in API, we can't import it!
         for(APIPackage p : pkgs.values()) if(!p.getName().equals("")) api.addPackage(p);
         return api;
     }
     
     @Override
     public void visitTopLevel(JCCompilationUnit jccu) {
         String n = jccu.packge.fullname.toString();
         currentPackage = pkgs.get(n);
         if (currentPackage == null) {
             currentPackage = new APIPackage(n);
             pkgs.put(n, currentPackage);
         }
         super.visitTopLevel(jccu);
         currentPackage = null;        
     }      
     
     @Override
     public void visitClassDef(JCClassDecl jccd) {
         ClassSymbol cs = jccd.sym;
         if ((cs.flags() & (Flags.PUBLIC | Flags.PROTECTED)) != 0) {            
             classes.push(currentClass);
            currentClass = new APIClass(jccd);
             super.visitClassDef(jccd);
             currentPackage.addClass(currentClass);
             currentClass = classes.pop(); 
             currentClassImports = new HashMap<String, String>();
         }
     }
 
     @Override
     public void visitMethodDef(JCMethodDecl jcmd) {
         MethodSymbol ms = jcmd.sym;
         if ((ms.flags() & (Flags.PUBLIC | Flags.PROTECTED)) != 0) {
             if ((ms.flags() & Flags.GENERATEDCONSTR) == 0) {
                 currentClass.addMethod(new APIMethod(jcmd, currentClassImports));
                 super.visitMethodDef(jcmd);
             }
         }
     }
 
     @Override
     public void visitVarDef(JCVariableDecl jcvd) {
         VarSymbol vs = jcvd.sym;
         if ((vs.flags() & (Flags.PUBLIC | Flags.PROTECTED)) != 0) {
             currentClass.addField(new APIField(jcvd, currentClassImports));
             super.visitVarDef(jcvd);            
         }
     }
 
     @Override
     public void visitImport(JCImport jci) {        
         String importClassName = jci.getQualifiedIdentifier().toString();
         String simpleClassName = importClassName.substring(importClassName.lastIndexOf('.')+1);
         currentClassImports.put(simpleClassName, importClassName);        
         super.visitImport(jci);
     }
     
     
     }
