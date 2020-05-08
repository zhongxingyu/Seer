 package visitor;
 
 import error.MethodOverrideException;
 import error.ErrorMsg;
 import symbol.Symbol;
 import symbol.SymbolTable;
 import syntaxtree.*;
 
 /**
  * A visitor that handles type definitions
  */
 public class TypeDefVisitor implements Visitor {
 
     private ErrorMsg error;
     private SymbolTable st = new SymbolTable();
 
     public TypeDefVisitor(ErrorMsg e) {
         error = e;
     }
 
     public void visit(Program n) {
 
         //Add the main class as a class without methods or variables
         n.addClass(n.m.i1, new ClassDeclSimple(n, n.m.i1, new VarDeclList(), new MethodDeclList()));
 
         n.m.accept(this);
         
         //Pass one: declare classes
         for (ClassDecl c : n.cl.getList()) {
             if (!n.addClass(c.i, c)) {
                 error.complain("Class " + c.i + " is already defined.", c.line_number);
             }
         }
         
         //Pass two: decend into classes
         for (ClassDecl c : n.cl.getList()) {
             c.accept(this);
         }
     }
 
     public void visit(MainClass n) {
         st.pushScope(n);
        st.addVariable(n.i2, new Symbol(new VoidType(-1))); //the param to main is a hack...
         for (VarDecl v : n.vl.getList()) {
             v.accept(this);
         }
         for (Statement s : n.sl.getList()) {
             s.accept(this);
         }
         st.popScope();
     }
 
     public void visit(ClassDeclSimple n) {
         class_decl_visit(n);
     }
 
     public void class_decl_visit(ClassDecl n) {
         st.pushScope(n);
         for (VarDecl v : n.vl.getList()) {
             v.accept(this);
         }
 
         for (MethodDecl m : n.ml.getList()) {
             try {
                 if (!n.addMethod(m.i, m.fl.getTypeList(), m)) {
                     error.complain(n.toString(), "Method " + m.signature() + " is already defined in class " + n, m.line_number);
                 }
             } catch (MethodOverrideException moe) {
                 error.complain(n.toString(), m.signature() + " in " + n.toString() + " cannot override " + moe.parent.signature() + " in " + moe.parent.cls + "\n"
                         + "found    :  " + moe.found + "\n"
                         + "required :  " + moe.parent.t, m.line_number);
             } catch (NullPointerException e) {
                 System.err.println("Caught exception when adding method "+m.signature()+" on line "+m.line_number);
                 throw e;
             }
             m.accept(this);
         }
 
         st.popScope();
     }
 
     public void visit(ClassDeclExtends n) {
         class_decl_visit(n);
     }
 
     public void visit(VarDecl n) {
         if(n.t instanceof VoidType) {
             error.complain("Can't declare a variable as void", n.line_number);
         }
         n.i.sym = new Symbol(n.t);
         if (!st.addVariable(n.i, n.i.sym)) {
             error.complain(n.i + " is already defined in current scope (" + st + ")", n.line_number);
         }
     }
 
     public void visit(MethodDecl n) {
         st.pushScope(n);
         for (Formal f : n.fl.getList()) {
             f.accept(this);
         }
         for (VarDecl v : n.vl.getList()) {
             v.accept(this);
         }
         for (Statement s : n.sl.getList()) {
             s.accept(this);
         }
         //return statement
         if(! (n.t instanceof VoidType)) {
             n.e.accept(this);
         }
         st.popScope();
     }
 
     public void visit(Formal n) {
         n.i.sym = new Symbol(n.t);
         if (!st.addVariable(n.i, n.i.sym)) {
             error.complain(n.i + " in formal list is already defined in current scope (" + st + ")", n.line_number);
         }
     }
 
     public void visit(Block n) {
         st.pushScope(n);
         for (VarDecl v : n.vl.getList()) {
             v.accept(this);
         }
         for (Statement s : n.sl.getList()) {
             s.accept(this);
         }
         st.popScope();
     }
     
     public void visit(ExpressionStatement n) {
         n.exp.accept(this);
     }
 
     public void visit(If n) {
         n.s1.accept(this);
     }
     
     public void visit(IfElse n) {
         n.s1.accept(this);
         n.else_statement.accept(this);
     }
 
     public void visit(While n) {
         n.s.accept(this);
     }
 
     public void visit(TypeCast n) {
         n.exp.accept(this);
         
     }
     
     public void visit(ArrayType n) {}
     public void visit(BooleanType n) {}
     public void visit(IntegerType n) {}
     public void visit(VoidType n) {}
     public void visit(LongType n) {}
     public void visit(IdentifierType n) {}
     public void visit(Print n) {}
     public void visit(Assign n) { }
     public void visit(ArrayAssign n) { }
     public void visit(Or n) {}
     public void visit(And n) {}
     public void visit(Compare n) {}
     public void visit(Plus n) { }
     public void visit(Minus n) {}
     public void visit(Times n) { }
     public void visit(ArrayLookup n) { }
     public void visit(ArrayLength n) { }
     public void visit(Call n) { }
     public void visit(IntegerLiteral n) { }
     public void visit(LongLiteral n) { }
     public void visit(True n) {}
     public void visit(False n) {}
     public void visit(IdentifierExp n) {}
     public void visit(This n) {}
     public void visit(NewArray n) {}
     public void visit(NewObject n){}
     public void visit(Not n) {}
     public void visit(Identifier n) {}
 
 }
