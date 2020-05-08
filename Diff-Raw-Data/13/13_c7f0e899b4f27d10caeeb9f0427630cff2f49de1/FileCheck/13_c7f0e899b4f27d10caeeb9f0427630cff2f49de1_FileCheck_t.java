 package cn.shenyanchao.utils;
 
 import cn.shenyanchao.common.Consts;
 import japa.parser.ast.CompilationUnit;
 import japa.parser.ast.body.MethodDeclaration;
 import japa.parser.ast.body.TypeDeclaration;
 
 import java.io.File;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  *
  * @author shenyanchao
  *         Date:  6/18/13
  *         Time:  3:25 PM
  */
 public class FileCheck {
 
     public boolean isTestJavaClassExist(File file) {
         if (!file.isDirectory()) {
             return file.exists();
         }
         return false;
     }
 
 
     public boolean isTestCaseExist(File testJavaFile, MethodDeclaration method) {
         CompilationUnit testCompilationUnit = JavaParserFactory.getCompilationUnit(testJavaFile, Consts.DEFAULT_ENCODE);
         List<TypeDeclaration> types = testCompilationUnit.getTypes();
         for (TypeDeclaration type : types) {
             List<MethodDeclaration> methodDeclarations = MembersFilter.findMethodsFrom(type);
             for (MethodDeclaration methodDeclaration : methodDeclarations) {
                 if ((method.getName() + Consts.TEST_SUFFIX).equals(methodDeclaration.getName())) {
                     return true;
                 }
             }
         }
         return false;
     }
 
 }
