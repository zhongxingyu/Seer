 /**
  * ("final":Word)? Modifiers Type Declarators
  */
 package translator;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import xtc.tree.GNode;
 import xtc.tree.Visitor;
 
 public class FieldDeclaration extends Declaration implements Translatable {
   
   private boolean isAbstract;
   private boolean isFinal;
   private boolean isStatic;
   private Visibility visibility;
   private Type type;
   private Declarators declarators;
   
   public FieldDeclaration(GNode n) {
     isAbstract = false;
     isFinal = false;
     isStatic = false;
     visibility = Visibility.PRIVATE;
     visit(n);
   }
   
   public String getDeclaration() {
     String declaration = type.getType() + " " + declarators.get(0).getName();
     if (type.isArray()) {
       declaration += "[]";
     }
     return declaration;
   }
 
   public String getName() {
     Declarator d = declarators.get(0);
     return d.getName();
   }
   
   public Visibility getVisibility() {
     return visibility;
   }
   
   public void visitDeclarators(GNode n) {
     declarators = new Declarators(n);
   }
   
   public void visitModifiers(GNode n) {
     Modifiers modifiers = new Modifiers(n);
     for (String s : modifiers) {
       if (s.equals("public"))
         visibility = visibility.PUBLIC;
       else if (s.equals("private"))
         visibility = visibility.PRIVATE;
       else if (s.equals("protected"))
         visibility = visibility.PROTECTED;
       else if (s.equals("final"))
         isFinal = true;
       else if (s.equals("static"))
         isStatic = true;
       else if (s.equals("abstract"))
         isAbstract = true;
     }
   }
   
   public void visitType(GNode n) {
     type = new Type(n);
   }
   
   public void visitWord(GNode n) {
     isFinal = true;
   }
 
   public String getCC(int indent, String className, List<Variable> variables) {
     StringBuilder s = new StringBuilder(getIndent(indent));
     if (isFinal)
       s.append("const ");
     if (isStatic)
       s.append("static ");
     if (isAbstract)
       s.append("abstract ");
     s.append(type.getType() + " ");
     s.append(declarators.getCC(indent, className, variables) + ";\n");
     return s.toString();
   }
 
 }
