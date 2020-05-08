 package translator;
 
import java.util.ArrayList;
 import java.util.HashMap;
import java.util.List;
 import java.util.Map;
 
 import xtc.tree.GNode;
 import xtc.tree.Visitor;
 
 public class ClassBody extends TranslationVisitor implements Translatable {
   
   private ConstructorDeclaration constructor;
   private Map<Visibility, List<FieldDeclaration>> fields;
   private Map<Visibility, List<MethodDeclaration>> methods;
   
   public ClassBody(GNode n) {
     fields = new HashMap<Visibility, List<FieldDeclaration>>();
     methods = new HashMap<Visibility, List<MethodDeclaration>>();
     visit(n);
   }
   
   public String getConstructorDeclaration() {
     return constructor.getHeaderDeclaration();
   }
   
   public void visitBlockDeclaration(GNode n) {
     
   }
   
   public void visitConstructorDeclaration(GNode n) {
     constructor = new ConstructorDeclaration(n);
   }
   
   public void visitEmptyDeclaration(GNode n) {
     
   }
   
   public void visitFieldDeclaration(GNode n) {
     FieldDeclaration f = new FieldDeclaration(n);
     Visibility v = f.getVisibility();
     if (!fields.containsKey(v))
       fields.put(v, new ArrayList<FieldDeclaration>());
     fields.get(v).add(f);
   }
   
   public void visitInterfaceDeclaration(GNode n) {
     
   }
   
   public void visitMethodDeclaration(GNode n) {
     MethodDeclaration m = new MethodDeclaration(n);
     Visibility v = m.getVisibility();
     if (!methods.containsKey(v))
       methods.put(v, new ArrayList<MethodDeclaration>());
     methods.get(v).add(m);
   }
   
   public List<FieldDeclaration> getFields(Visibility v) {
     if (fields.containsKey(v))
       return fields.get(v);
     else
       return null;
   }
   
   public List<MethodDeclaration> getMethods(Visibility v) {
     if (methods.containsKey(v))
       return methods.get(v);
     else
       return null;
   }
 
   public String getCC(int indent, String className, List<Variable> variables) {
     StringBuilder s = new StringBuilder();
     s.append(constructor.getCC(indent, className, variables) + "\n\n");
     List<MethodDeclaration> l = methods.get(Visibility.PUBLIC);
     if (l != null) {
       for (MethodDeclaration m : l) {
         if (!m.isStatic() && !m.isFinal() && !m.isAbstract())
           s.append(m.getCC(indent, className, variables) + "\n");
       }
     }
     s.append(getIndent(indent) + "__" + className + "_VT __" + className +
              "::__vtable;\n");
     return s.toString();
   }
 }
