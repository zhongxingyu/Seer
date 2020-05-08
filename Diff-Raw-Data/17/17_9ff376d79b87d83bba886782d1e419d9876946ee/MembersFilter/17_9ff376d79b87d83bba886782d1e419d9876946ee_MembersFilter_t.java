 package cn.shenyanchao.utils;
 
 import japa.parser.ast.body.BodyDeclaration;
 import japa.parser.ast.body.FieldDeclaration;
 import japa.parser.ast.body.MethodDeclaration;
 import japa.parser.ast.body.TypeDeclaration;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  *
  * @author shenyanchao
  *         Date:  6/18/13
  *         Time:  1:33 PM
  */
 public class MembersFilter {
 
 
     public static List<MethodDeclaration> findMethodsFrom(TypeDeclaration typeDeclaration) {
         List<BodyDeclaration> members = typeDeclaration.getMembers();
         List<MethodDeclaration> methodDeclarations = new ArrayList<MethodDeclaration>();
         for (BodyDeclaration body : members) {
             if (body instanceof MethodDeclaration) {
                 methodDeclarations.add((MethodDeclaration) body);
             } else if (body instanceof FieldDeclaration) {
                  continue;
             }
         }
         return methodDeclarations;
     }
 }
