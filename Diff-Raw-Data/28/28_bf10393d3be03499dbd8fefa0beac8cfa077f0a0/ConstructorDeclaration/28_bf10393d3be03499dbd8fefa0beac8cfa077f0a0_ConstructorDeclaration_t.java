 /**
  * Modifiers null Identifier FormalParameters ThrowsClause? Block
  */
 package translator;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import xtc.tree.GNode;
 import xtc.tree.Visitor;
 
 public class ConstructorDeclaration extends Declaration implements Translatable {
   
   private Block body;
   private String name;
   private FormalParameters parameters;
   private ThrowsClause throwsClause;
   private Visibility visibility;
   
   public ConstructorDeclaration(GNode n) {
     name = n.getString(2);
     throwsClause = null;
     name = n.getString(2);
     visit(n);
   }
 
   public String getHeaderDeclaration() {
     return "__" + name + "(" + parameters.getParameters() + ");";
   }
   
   public void visitBlock(GNode n) {
     body = new Block(n);
   }
   
   public void visitFormalParameters(GNode n) {
     parameters = new FormalParameters(n);
   }
   
   public void visitModifiers(GNode n) {
     Modifiers modifiers = new Modifiers(n);
     for (String m : modifiers) {
       if (m.equals("public"))
         visibility = Visibility.PUBLIC;
       else if (m.equals("private"))
         visibility = Visibility.PRIVATE;
       else if (m.equals("protected"))
         visibility = Visibility.PROTECTED;
     }
   }
   
   public void visitThrowsClause(GNode n) {
     throwsClause = new ThrowsClause(n);
   }
 
   public String getCC(int indent, String className, List<Variable> variables) {
     StringBuilder s = new StringBuilder();
     String in = getIndent(indent);
     s.append(in + "__" + name + "::__" + name + "(");
    s.append(parameters.getParameters() + ")\n" + in + ": __vptr(&__vtable)");
     List<Variable> v = new ArrayList<Variable>();
    for (FormalParameter f : parameters) {
       v.add(new Variable(f.getType(), f.getName()));
     }
     s.append(" {\n" + body.getCC(++indent, className, v) + in + "}");
     return s.toString();
   }
 }
