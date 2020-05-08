 package minijava.typechecker.implementation;
 
 import java.util.ArrayList;
 
 import minijava.ast.*;
 import minijava.util.FunTable;
 import minijava.visitor.Visitor;
 
 public class ClassTableVisitor implements Visitor<FunTable<Info>>
 {
   @Override
   public <T extends AST> FunTable<Info> visit(NodeList<T> n)
   {
     FunTable<Info> t = FunTable.theEmpty();
     for(int i = 0; i < n.size(); ++i)
     {
       t = t.merge(n.elementAt(i).accept(this));
     }
     return t;
   }
 
   @Override
   public FunTable<Info> visit(Program p)
   {
     return p.mainClass.accept(this).merge(p.classes.accept(this));
   }
 
   @Override
   public FunTable<Info> visit(MainClass n)
   {
     FunTable<Info> t = FunTable.theEmpty();
     
     // Add main() to ClassInfo
     MethodInfo m = new MethodInfo();
     m.formals = t.insert(n.argName, null);
     m.locals = t;
    m.formalsList = new ArrayList<VarInfo>();
     
     ClassInfo c = new ClassInfo();
     c.fields = t;
     c.methods = t.insert("main", m);
     
     return t.insert("Main", c).merge(n.statement.accept(this));
   }
 
   @Override
   public FunTable<Info> visit(ClassDecl d)
   {
     ClassInfo c = new ClassInfo();
     c.superClass = d.superName;
     c.fields = d.vars.accept(this);
     c.methods = d.methods.accept(this);
     
     FunTable<Info> t = FunTable.theEmpty();
     return t.insert(d.name, c);
   }
 
   @Override
   public FunTable<Info> visit(VarDecl d)
   {
     VarInfo v = new VarInfo();
     v.kind = d.kind;
     v.type = d.type;
     
     FunTable<Info> t = FunTable.theEmpty();
     return t.insert(d.name, v);
   }
 
   @Override
   public FunTable<Info> visit(MethodDecl d)
   {
     FunTable<Info> t = FunTable.theEmpty();
     
     MethodInfo m = new MethodInfo();
     m.formals = d.formals.accept(this);
     m.locals = d.vars.accept(this);
     m.returnType = d.returnType;
     m.formalsList = new ArrayList<VarInfo>();
     for(int i = 0; i < d.formals.size(); i++)
     {
       VarInfo v = new VarInfo();
       v.kind = d.formals.elementAt(i).kind;
       v.type = d.formals.elementAt(i).type;
       m.formalsList.add(v);
     }
     
     return t.insert(d.name, m).merge(d.statements.accept(this).merge(d.returnExp.accept(this)));
   }
 
   @Override
   public FunTable<Info> visit(IntArrayType n) { return FunTable.theEmpty(); }
 
   @Override
   public FunTable<Info> visit(BooleanType n) { return FunTable.theEmpty(); }
 
   @Override
   public FunTable<Info> visit(IntegerType n) { return FunTable.theEmpty(); }
 
   @Override
   public FunTable<Info> visit(ObjectType n) { return FunTable.theEmpty(); }
 
   @Override
   public FunTable<Info> visit(Block b)
   {
     return b.statements.accept(this);
   }
 
   @Override
   public FunTable<Info> visit(If i)
   {
     return i.tst.accept(this).merge(i.thn.accept(this).merge(i.els.accept(this)));
   }
 
   @Override
   public FunTable<Info> visit(While w)
   {
     return w.tst.accept(this).merge(w.body.accept(this));
   }
 
   @Override
   public FunTable<Info> visit(Print p)
   {
     return p.exp.accept(this);
   }
 
   @Override
   public FunTable<Info> visit(Assign a)
   {
     return a.value.accept(this);
   }
 
   @Override
   public FunTable<Info> visit(ArrayAssign a)
   {
     return a.index.accept(this).merge(a.value.accept(this));
   }
 
   @Override
   public FunTable<Info> visit(And a)
   {
     return a.e1.accept(this).merge(a.e2.accept(this));
   }
 
   @Override
   public FunTable<Info> visit(LessThan l)
   {
     return l.e1.accept(this).merge(l.e2.accept(this));
   }
 
   @Override
   public FunTable<Info> visit(Plus p)
   {
     return p.e1.accept(this).merge(p.e2.accept(this));
   }
 
   @Override
   public FunTable<Info> visit(Minus m)
   {
     return m.e1.accept(this).merge(m.e2.accept(this));
   }
 
   @Override
   public FunTable<Info> visit(Times t)
   {
     return t.e1.accept(this).merge(t.e2.accept(this));
   }
 
   @Override
   public FunTable<Info> visit(ArrayLookup a)
   {
     return a.index.accept(this).merge(a.array.accept(this));
   }
 
   @Override
   public FunTable<Info> visit(ArrayLength a)
   {
     return a.array.accept(this);
   }
 
   @Override
   public FunTable<Info> visit(Call c)
   {
     return c.receiver.accept(this).merge(c.rands.accept(this));
   }
 
   @Override
   public FunTable<Info> visit(IntegerLiteral n) { return FunTable.theEmpty(); }
 
   @Override
   public FunTable<Info> visit(BooleanLiteral n) { return FunTable.theEmpty(); }
 
   @Override
   public FunTable<Info> visit(IdentifierExp n) { return FunTable.theEmpty(); }
 
   @Override
   public FunTable<Info> visit(This n) { return FunTable.theEmpty(); }
 
   @Override
   public FunTable<Info> visit(NewArray n)
   {
     return n.size.accept(this);
   }
 
   @Override
   public FunTable<Info> visit(NewObject n) { return FunTable.theEmpty(); }
 
   @Override
   public FunTable<Info> visit(Not n)
   {
     return n.e.accept(this);
   }
 }
