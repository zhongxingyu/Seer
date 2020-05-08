 package minijava.translate.implementation;
 
 import minijava.ast.*;
 import minijava.ir.frame.Frame;
 import minijava.ir.temp.Temp;
 import minijava.ir.tree.IR;
 import minijava.ir.tree.IRExp;
 import minijava.ir.tree.IRStm;
 import minijava.ir.tree.BINOP.Op;
 import minijava.translate.Fragments;
 import minijava.translate.ProcFragment;
 import minijava.translate.Translator;
 import minijava.util.List;
 import minijava.visitor.Visitor;
 
 public class TranslateVisitor implements Visitor<TranslateExp>
 {
   private Frame     frameFactory;
   private Fragments fragments;
   
   public TranslateVisitor(Frame frameFactory, Fragments fragments)
   {
     this.frameFactory = frameFactory;
     this.fragments    = fragments;
   }
   
   @Override
   public <T extends AST> TranslateExp visit(NodeList<T> ns)
   {
     // Iterate all nodes and generate IR
     for(int i = 0; i < ns.size(); ++i) { ns.elementAt(i).accept(this); }
     return null;
   }
   
   @Override
   public TranslateExp visit(Program n)
   {
     n.mainClass.accept(this);
     n.classes.accept(this);
     
     return null;
   }
   
   @Override
   public TranslateExp visit(MainClass n)
   {
     List<Boolean> formals = List.empty();
     Frame frame = this.frameFactory.newFrame(Translator.L_MAIN, formals);
     this.fragments.add(new ProcFragment(frame,
                                         this.procEntryExit( frame,
                                                             n.statement.accept(this))));
     return null;
   }
 
   @Override
   public TranslateExp visit(ClassDecl n)
   {
     n.vars.accept(this);
     n.methods.accept(this);
     
     return null;
   }
 
   @Override
   public TranslateExp visit(VarDecl n)
   {
     // TODO Auto-generated method stub
     return null;
   }
 
   @Override
   public TranslateExp visit(MethodDecl n)
   {
     // TODO Auto-generated method stub
     return null;
   }
 
   @Override
   public TranslateExp visit(IntArrayType n)
   {
     // TODO Auto-generated method stub
     return null;
   }
 
   @Override
   public TranslateExp visit(BooleanType n)
   {
     // TODO Auto-generated method stub
     return null;
   }
 
   @Override
   public TranslateExp visit(IntegerType n)
   {
     // TODO Auto-generated method stub
     return null;
   }
 
   @Override
   public TranslateExp visit(ObjectType n)
   {
     // TODO Auto-generated method stub
     return null;
   }
 
   @Override
   public TranslateExp visit(Block n)
   {
     // TODO Auto-generated method stub
     return null;
   }
 
   @Override
   public TranslateExp visit(If n)
   {
     // TODO Auto-generated method stub
     return null;
   }
 
   @Override
   public TranslateExp visit(While n)
   {
     // TODO Auto-generated method stub
     return null;
   }
 
   @Override
   public TranslateExp visit(Print n)
   {
     return new TranslateNx(IR.MOVE( IR.TEMP(new Temp()),
                                     IR.CALL(Translator.L_PRINT,
                                             n.exp.accept(this).unEx())));
   }
 
   @Override
   public TranslateExp visit(Assign n)
   {
     // TODO Auto-generated method stub
     return null;
   }
 
   @Override
   public TranslateExp visit(ArrayAssign n)
   {
     // TODO Auto-generated method stub
     return null;
   }
 
   @Override
   public TranslateExp visit(And n)
   {
     // TODO Auto-generated method stub
     return null;
   }
 
   @Override
   public TranslateExp visit(LessThan n)
   {
     // TODO Auto-generated method stub
     return null;
   }
 
   @Override
   public TranslateExp visit(Plus n)
   {
     return new TranslateEx(IR.BINOP(Op.PLUS,
                                     n.e1.accept(this).unEx(),
                                     n.e2.accept(this).unEx()));
   }
 
   @Override
   public TranslateExp visit(Minus n)
   {
     // TODO Auto-generated method stub
     return null;
   }
 
   @Override
   public TranslateExp visit(Times n)
   {
     // TODO Auto-generated method stub
     return null;
   }
 
   @Override
   public TranslateExp visit(ArrayLookup n)
   {
     // TODO Auto-generated method stub
     return null;
   }
 
   @Override
   public TranslateExp visit(ArrayLength n)
   {
     // TODO Auto-generated method stub
     return null;
   }
 
   @Override
   public TranslateExp visit(Call n)
   {
     // TODO Auto-generated method stub
     return null;
   }
 
   @Override
   public TranslateExp visit(IntegerLiteral n)
   {
     return new TranslateEx(IR.CONST(n.value));
   }
 
   @Override
   public TranslateExp visit(BooleanLiteral n)
   {
     return new TranslateEx((n.value) ? IR.TRUE : IR.FALSE);
   }
 
   @Override
   public TranslateExp visit(IdentifierExp n)
   {
     // TODO Auto-generated method stub
     return null;
   }
 
   @Override
   public TranslateExp visit(This n)
   {
     // TODO Auto-generated method stub
     return null;
   }
 
   @Override
   public TranslateExp visit(NewArray n)
   {
     // TODO Auto-generated method stub
     return null;
   }
 
   @Override
   public TranslateExp visit(NewObject n)
   {
     // TODO Auto-generated method stub
     return null;
   }
 
   @Override
   public TranslateExp visit(Not not)
   {
    // Subtracting 1 from a boolean bit results in the bit being flipped
     return new TranslateEx(IR.BINOP(Op.MINUS, IR.CONST(1), not.e.accept(this).unEx()));
   }
   
   // Helper method for setting frame state for exiting methods
   private IRStm procEntryExit(Frame frame, TranslateExp body)
   {
     // Treat empty method blocks as No-Ops
     if(body == null) { return IR.NOP; }
     
     // Set method return value (if necessary)
     IRExp e = body.unEx();
     return (e != null) ? IR.MOVE(frame.RV(), e) : body.unNx();
   }
 }
