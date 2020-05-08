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
   public FunTable<Info> visit(Program n)
   {
     return n.mainClass.accept(this)
                       .merge(n.classes.accept(this));
   }
 
   @Override
   public FunTable<Info> visit(MainClass n)
   {
     FunTable<Info> t = FunTable.theEmpty();
     
     // Add main() to ClassInfo
     MethodInfo m  = new MethodInfo();
     m.formals     = t.insert(n.argName, null);
     m.locals      = t;
     m.formalsList = new ArrayList<VarInfo>();
     
     ClassInfo c = new ClassInfo();
     c.fields    = t;
     c.methods   = t.insert("main", m);
     
    return t.insert(n.className, c)
            .merge(n.statement.accept(this));
   }
 
   @Override
   public FunTable<Info> visit(ClassDecl n)
   {
     ClassInfo c   = new ClassInfo();
     c.superClass  = n.superName;
     c.fields      = n.vars.accept(this);
     c.methods     = n.methods.accept(this);
     
     FunTable<Info> t = FunTable.theEmpty();
     return t.insert(n.name, c);
   }
 
   @Override
   public FunTable<Info> visit(VarDecl n)
   {
     VarInfo v = new VarInfo();
     v.kind    = n.kind;
     v.type    = n.type;
     
     FunTable<Info> t = FunTable.theEmpty();
     return t.insert(n.name, v);
   }
 
   @Override
   public FunTable<Info> visit(MethodDecl n)
   {
     MethodInfo m  = new MethodInfo();
     m.formals     = n.formals.accept(this);
     m.formalsList = new ArrayList<VarInfo>();
     m.locals      = n.vars.accept(this);
     m.returnType  = n.returnType;
     
     for(int i = 0; i < n.formals.size(); ++i)
     {
       VarDecl d   = n.formals.elementAt(i);
       VarInfo v   = new VarInfo();
       v.kind      = d.kind;
       v.type      = d.type;
       m.formalsList.add(v);
     }
     
     FunTable<Info> t = FunTable.theEmpty();
     return t.insert(n.name, m)
             .merge( n.statements.accept(this)
                                 .merge(n.returnExp.accept(this)));
   }
 
   @Override
   public FunTable<Info> visit(IntArrayType n) { return FunTable.theEmpty(); }
 
   @Override
   public FunTable<Info> visit(BooleanType n)  { return FunTable.theEmpty(); }
 
   @Override
   public FunTable<Info> visit(IntegerType n)  { return FunTable.theEmpty(); }
 
   @Override
   public FunTable<Info> visit(ObjectType n)   { return FunTable.theEmpty(); }
 
   @Override
   public FunTable<Info> visit(Block n)
   {
     return n.statements.accept(this);
   }
 
   @Override
   public FunTable<Info> visit(If n)
   {
     return n.tst.accept(this)
                 .merge(n.thn.accept(this)
                             .merge(n.els.accept(this)));
   }
 
   @Override
   public FunTable<Info> visit(While n)
   {
     return n.tst.accept(this)
                 .merge(n.body.accept(this));
   }
 
   @Override
   public FunTable<Info> visit(Print n)
   {
     return n.exp.accept(this);
   }
 
   @Override
   public FunTable<Info> visit(Assign n)
   {
     return n.value.accept(this);
   }
 
   @Override
   public FunTable<Info> visit(ArrayAssign n)
   {
     return n.index.accept(this)
                   .merge(n.value.accept(this));
   }
 
   @Override
   public FunTable<Info> visit(And n)
   {
     return  n.e1.accept(this)
                 .merge(n.e2.accept(this));
   }
 
   @Override
   public FunTable<Info> visit(LessThan n)
   {
     return  n.e1.accept(this)
                 .merge(n.e2.accept(this));
   }
 
   @Override
   public FunTable<Info> visit(Plus n)
   {
     return  n.e1.accept(this)
                 .merge(n.e2.accept(this));
   }
 
   @Override
   public FunTable<Info> visit(Minus n)
   {
     return  n.e1.accept(this)
                 .merge(n.e2.accept(this));
   }
 
   @Override
   public FunTable<Info> visit(Times n)
   {
     return  n.e1.accept(this)
                 .merge(n.e2.accept(this));
   }
 
   @Override
   public FunTable<Info> visit(ArrayLookup n)
   {
     return n.index.accept(this)
                   .merge(n.array.accept(this));
   }
 
   @Override
   public FunTable<Info> visit(ArrayLength n)
   {
     return n.array.accept(this);
   }
 
   @Override
   public FunTable<Info> visit(Call n)
   {
     return  n.receiver.accept(this)
                       .merge(n.rands.accept(this));
   }
 
   @Override
   public FunTable<Info> visit(IntegerLiteral n) { return FunTable.theEmpty(); }
 
   @Override
   public FunTable<Info> visit(BooleanLiteral n) { return FunTable.theEmpty(); }
 
   @Override
   public FunTable<Info> visit(IdentifierExp n)  { return FunTable.theEmpty(); }
 
   @Override
   public FunTable<Info> visit(This n)           { return FunTable.theEmpty(); }
 
   @Override
   public FunTable<Info> visit(NewArray n)
   {
     return n.size.accept(this);
   }
 
   @Override
   public FunTable<Info> visit(NewObject n)      { return FunTable.theEmpty(); }
 
   @Override
   public FunTable<Info> visit(Not n)
   {
     return n.e.accept(this);
   }
 }
