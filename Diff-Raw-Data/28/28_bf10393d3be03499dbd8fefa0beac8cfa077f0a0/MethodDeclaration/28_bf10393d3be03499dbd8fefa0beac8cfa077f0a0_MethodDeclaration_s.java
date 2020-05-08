 /**
  * Modifiers null (VoidType/Type) Identifier FormalParameters Dimensions? ThrowsClause? Block/null
  */
 package translator;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import xtc.tree.GNode;
 import xtc.tree.Visitor;
 
 public class MethodDeclaration extends Declaration implements Translatable {
 
   private String name;
   private Block body;
   private Type returnType;
   private Visibility visibility;
   private FormalParameters parameters;
   private boolean isAbstract;
   private boolean isFinal;
   private boolean isStatic;
   
   public MethodDeclaration(GNode n) {
     isAbstract = false;
     isFinal = false;
     isStatic = false;
     body = null;
     visibility = Visibility.PRIVATE;
     name = n.getString(3);
     visit(n);
   }
   
   public String getHeaderDeclaration(String className) {
     StringBuilder s = new StringBuilder("static " + returnType.getType() + " " + name + "(" + className);
     for (FormalParameter parameter : parameters)
       s.append(", " + parameter.getType());
     s.append(");");
     return s.toString();
   }
 
   public String getHeaderVTConstructor(String className) {
     return name + "(&__" + className + "::" + name + ")"; 
   }
 
   public String getHeaderVTDeclaration(String className) {
     StringBuilder s = new StringBuilder();
     s.append(returnType.getType() + " (*" + name + ")(" + className);
     for (FormalParameter parameter : parameters)
       s.append(", " + parameter.getType());
     s.append(");");
     return s.toString();
   }
   
   public String getCC(int indent, String className, List<Variable> variables) {
     StringBuilder s = new StringBuilder();
     String in = getIndent(indent);
     s.append(in + returnType.getType() + " __" + className + "::" + name + "(");
     s.append(className + " __this");
    List<FormalParameter> p = parameters.getParameters();
    if (p.size() > 0)
      s.append(", " + p);
     s.append(") {\n");
     if (body != null) {
       List<Variable> v = new ArrayList<Variable>();
      for (FormalParameter f : p) {
         v.add(new Variable(f.getType(), f.getName()));
       }
       s.append(body.getCC(++indent, className, v));
       in = getIndent(--indent);
     }
     s.append(in + "}\n");
     return s.toString();
   }
 
   public String getName() {
     return name;
   }
   
   public FormalParameters getParameters() {
     return parameters;
   }
   
   public Type getReturnType() {
     return returnType;
   }
   
   public Visibility getVisibility() {
     return visibility;
   }
   
   public boolean isAbstract() {
     return isAbstract;
   }
   
   public boolean isFinal() {
     return isFinal;
   }
   
   public boolean isStatic() {
     return isStatic;
   }
   
   public void visitBlock(GNode n) {
     body = new Block(n);
   }
   
   public void visitFormalParameters(GNode n) {
     parameters = new FormalParameters(n);
   }
 
   public void visitModifiers(GNode n) {
     Modifiers modifiers = new Modifiers(n);
     for (String s : modifiers) {
       if (s.equals("public"))
         visibility = Visibility.PUBLIC;
       else if (s.equals("private"))
         visibility = Visibility.PRIVATE;
       else if (s.equals("protected"))
         visibility = Visibility.PROTECTED;
       else if (s.equals("final"))
         isFinal = true;
       else if (s.equals("static"))
         isStatic = true;
       else if (s.equals("abstract"))
         isAbstract = true;
     }
   }
   
   public void visitType(GNode n) {
     returnType = new Type(n);
   }
   
   public void visitVoidType(GNode n) {
     returnType = new VoidType(n);
   }
 }
