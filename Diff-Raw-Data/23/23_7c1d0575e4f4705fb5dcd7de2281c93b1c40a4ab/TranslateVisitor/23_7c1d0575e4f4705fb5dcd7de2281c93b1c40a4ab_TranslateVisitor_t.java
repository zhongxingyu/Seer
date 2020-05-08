 package minijava.translate.implementation;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.Stack;
 
 import junit.framework.Assert;
 
 import minijava.ast.*;
 import minijava.ir.frame.Access;
 import minijava.ir.frame.Frame;
 import minijava.ir.temp.Label;
 import minijava.ir.temp.Temp;
 import minijava.ir.tree.CJUMP.RelOp;
 import minijava.ir.tree.IR;
 import minijava.ir.tree.IRExp;
 import minijava.ir.tree.IRStm;
 import minijava.ir.tree.BINOP.Op;
 import minijava.translate.Fragment;
 import minijava.translate.Fragments;
 import minijava.translate.ProcFragment;
 import minijava.translate.Translator;
 import minijava.util.List;
 import minijava.visitor.Visitor;
 
 public class TranslateVisitor implements Visitor<TranslateExp>
 {
   private Fragments     fragments,
                         classFragments;
   
   private Stack<Frame>  frames = new Stack<Frame>();
   private SymbolTable   symbols = new SymbolTable();
   private String        currentClass,
                         currentMethod;
   
   public TranslateVisitor(Frame frameFactory, Fragments fragments)
   {
     this.frames.push(frameFactory);
     this.fragments = fragments;
     this.classFragments = new Fragments(frameFactory);
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
     // Make sure class declarations are defined before mainClass is visited
     n.classes.accept(this);
     n.mainClass.accept(this);
     
     // Make sure mainClass Fragment from is defined before class Fragments
     Iterator<Fragment> it = this.classFragments.iterator();
     while(it.hasNext())
     {
       this.fragments.add(it.next());
     }
     
     return null;
   }
   
   @Override
   public TranslateExp visit(MainClass n)
   {
     List<Boolean> frameParams = List.empty();
     this.frames.push(this.createNewFrame(Translator.L_MAIN, frameParams));
     this.currentClass = n.className;
     this.currentMethod = "main";
     this.fragments.add(new ProcFragment(this.frames.peek(),
                                         this.procEntryExit(n.statement.accept(this))));
     this.currentClass = this.currentMethod = null;
     this.frames.pop();
     
     return null;
   }
 
   @Override
   public TranslateExp visit(ClassDecl n)
   {
     this.currentClass = n.name;
     
     n.vars.accept(this);
     n.methods.accept(this);
 
     this.currentClass = null;
 
     return null;
   }
 
   @Override
   public TranslateExp visit(VarDecl n)
   {
     return new TranslateEx(this.addVar(n.name).exp(this.frames.peek().FP()));
   }
 
   @Override
   public TranslateExp visit(MethodDecl n)
   {
     this.currentMethod = n.name;
     
     this.addClassMethod(n.name);
     
     List<Boolean> frameParams = List.empty();
    int numFormals = n.formals.size() + 1;
     for(int i = 0; i < numFormals; ++i)
     {
      frameParams.add(true);
     }
     this.frames.push(this.createNewFrame(Label.get(n.name), frameParams));
     
    // Keep track of formal variables
    this.addFormal(0, "this");
    for(int j = 0; j < numFormals - 1; ++j)
    {
      this.addFormal(j + 1, n.formals.elementAt(j).name);
    }
     
     n.vars.accept(this);
     
     IRExp e = null;
     
     // Check if block is empty, a single statement, or multiple statements
     int numStatements = n.statements.size();
     if(numStatements > 0)
     {
       IRStm s = n.statements.elementAt(0).accept(this).unNx();
       
       // Generate SEQ instructions chain if method contains multiple statements
       if(numStatements > 1)
       {
         for(int i = 1; i < numStatements; ++i)
         {
           s = IR.SEQ(s, n.statements.elementAt(i).accept(this).unNx());
         }
       }
       
       e = IR.ESEQ(s, n.returnExp.accept(this).unEx());
     }
     else
     {
       e = n.returnExp.accept(this).unEx();
     }
     
     this.classFragments.add(new ProcFragment( this.frames.peek(),
                                               this.procEntryExit(new TranslateEx(e))));
     this.currentMethod = null;
     this.frames.pop();
     
     return null;
   }
 
   @Override
   public TranslateExp visit(IntArrayType n)
   {
     // No translation required
     return null;
   }
 
   @Override
   public TranslateExp visit(BooleanType n)
   {
     // No translation required
     return null;
   }
 
   @Override
   public TranslateExp visit(IntegerType n)
   {
     // No translation required
     return null;
   }
 
   @Override
   public TranslateExp visit(ObjectType n)
   {
     // No translation required
     return null;
   }
 
   @Override
   public TranslateExp visit(Block n)
   {
     IRStm s     = IR.NOP;
     int length  = n.statements.size();
     
     // Check if block is empty, a single statement, or multiple statements
     // An empty block defaults to a No-Op
     if(length > 0)
     {
       s = n.statements.elementAt(0).accept(this).unNx();
       
       // Generate SEQ instructions chain if block contains multiple statements
       for(int i = 1; i < length; ++i)
       {
         s = IR.SEQ(s, n.statements.elementAt(i).accept(this).unNx());
       }
     }
     
     return new TranslateNx(s);
   }
 
   @Override
   public TranslateExp visit(If n)
   {
     return new IfThenElseExp( n.tst.accept(this), 
                               n.thn.accept(this), 
                               n.els.accept(this));
   }
 
   @Override
   public TranslateExp visit(While n)
   {
     Label done = Label.generate("done");
     Label test = Label.generate("test");
     TranslateExp loop = new IfThenElseExp(n.tst.accept(this), 
                                           new TranslateNx(IR.SEQ( n.body.accept(this).unNx(),
                                                                   IR.JUMP(test))), 
                                           new TranslateNx(IR.JUMP(done)));
     return new TranslateNx(IR.SEQ(IR.LABEL(test),
                                   loop.unNx(),
                                   IR.LABEL(done)));
   }
 
   @Override
   public TranslateExp visit(Print n)
   {
     return new TranslateNx(IR.EXP(IR.CALL(Translator.L_PRINT,
                                           n.exp.accept(this).unEx())));
   }
 
   @Override
   public TranslateExp visit(Assign n)
   {
     Access var = this.lookupVar(n.name);
     if(var == null) { return null; }
     
     IRExp e = n.value.accept(this).unEx();
     return (this.currentMethod != null) ? new TranslateNx(IR.MOVE(var.exp(this.frames.peek().FP()), e)) :
                                           new TranslateNx(IR.MOVE(IR.MEM(var.exp(this.frames.peek().FP())),
                                                                   e));
   }
 
   @Override
   public TranslateExp visit(ArrayAssign n)
   {
     Access var = this.lookupVar(n.name);
     if(var == null) { return null; }
     
     return new TranslateNx(IR.MOVE(IR.PLUS( IR.MEM(var.exp(this.frames.peek().FP())),
                                             n.index.accept(this).unEx()),
                                             n.value.accept(this).unEx()));
   }
 
   @Override
   public TranslateExp visit(And n)
   {
     return new AndCx( n.e1.accept(this),
                       n.e2.accept(this));
   }
 
   @Override
   public TranslateExp visit(LessThan n)
   {
     return new RelCx( RelOp.LT,  
                       n.e1.accept(this).unEx(), 
                       n.e2.accept(this).unEx());
   }
 
   @Override
   public TranslateExp visit(Plus n)
   {
     return new TranslateEx(IR.PLUS( n.e1.accept(this).unEx(),
                                     n.e2.accept(this).unEx()));
   }
 
   @Override
   public TranslateExp visit(Minus n)
   {
     return new TranslateEx(IR.BINOP(Op.MINUS,
                                     n.e1.accept(this).unEx(),
                                     n.e2.accept(this).unEx()));
   }
 
   @Override
   public TranslateExp visit(Times n)
   {
     return new TranslateEx(IR.BINOP(Op.MUL,
                                     n.e1.accept(this).unEx(),
                                     n.e2.accept(this).unEx()));
   }
 
   @Override
   public TranslateExp visit(ArrayLookup n)
   {
     return new TranslateEx(IR.MEM(IR.PLUS(n.array.accept(this).unEx(),
                                           n.index.accept(this).unEx())));
   }
 
   @Override
   public TranslateExp visit(ArrayLength n)
   {
     return new TranslateEx(IR.MEM(this.frames.peek().FP()));
   }
 
   @Override
   public TranslateExp visit(Call n)
   {
     List<IRExp> args = List.list(n.receiver.accept(this).unEx());
     int length = n.rands.size();
     for(int i = 0; i < length; ++i)
     {
       args.add(n.rands.elementAt(i).accept(this).unEx());
     }
     
     return new TranslateEx(IR.CALL(Label.get(n.name), args));
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
     Access var = this.lookupVar(n.name);
     return (var != null) ? new TranslateEx(var.exp(this.frames.peek().FP())) : null;
   }
 
   @Override
   public TranslateExp visit(This n)
   {
     // TODO Auto-generated method stub
     Assert.assertTrue("This not implemented", false);
     return null;
   }
 
   @Override
   public TranslateExp visit(NewArray n)
   {
     IRExp r       = IR.TEMP(new Temp()),
           callNew = IR.CALL(Translator.L_NEW_OBJECT,
                             List.list(IR.PLUS(IR.MUL(n.size.accept(this).unEx(), this.frames.peek().wordSize()),
                                               IR.CONST(this.frames.peek().wordSize()))));
     
     return new TranslateEx(IR.ESEQ(IR.MOVE(r, callNew), r));
   }
 
   @Override
   public TranslateExp visit(NewObject n)
   {
     Set<String> vars = this.lookupClassVars(n.typeName);
     int varsSize = (vars != null) ? ((vars.size() + 1) * this.frames.peek().wordSize()) : 0;
     IRExp r = IR.TEMP(new Temp()),
           callNew = IR.CALL(Translator.L_NEW_OBJECT, List.list(IR.CONST(varsSize)));
     
     // Initialize object
     IRStm s = IR.NOP;
 //    int i = 0;
 //    while(it.hasNext())
 //    {
 //      IRStm m = IR.MOVE(IR.MEM(IR.PLUS(r, IR.CONST(wordSize * i++))), n.);
 //      s = (s != null) ? IR.SEQ(s, m) : m;
 //    }
     
     return new TranslateEx(IR.ESEQ(IR.SEQ(IR.MOVE(r, callNew), s), r));
   }
 
   @Override
   public TranslateExp visit(Not not)
   {
     // Subtracting 1 by a boolean bit results in the bit being flipped
     return new TranslateEx(IR.BINOP(Op.MINUS, IR.CONST(1), not.e.accept(this).unEx()));
   }
   
   private Frame createNewFrame(Label name, List<Boolean> formalsEscape)
   {
     return this.frames.peek().newFrame(name, formalsEscape);
   }
   
   // Helper method for updating the current frame's state upon exiting a method
   private IRStm procEntryExit(TranslateExp body)
   {
     Frame currentFrame = this.frames.peek();
     
     // Treat empty method blocks as No-Ops
     IRStm s = IR.NOP;
     if(body != null)
     {
       // Set method return value (if necessary)
       IRExp e = body.unEx();
       s = (e != null) ? IR.MOVE(currentFrame.RV(), e) : body.unNx();
     }
     
     return currentFrame.procEntryExit1(s);
   }
   
   private void addClassMethod(String methodName)
   {
     this.symbols.addClassMethod(this.currentClass, methodName);
   }
   
  private Access addFormal(int i, String id)
   {
     Assert.assertNotNull(this.currentMethod);
     
    Access var = this.frames.peek().getFormal(i);
     this.symbols.addClassMethodVar( this.currentClass,
                                     this.currentMethod,
                                     id,
                                     var);
     return var;
   }
  
   private Access addVar(String id)
   {
     Access var;
     if(this.currentMethod != null)
     {
       // Allocate method variable on current frame
       var = this.frames.peek().allocLocal(false);
       this.symbols.addClassMethodVar( this.currentClass,
                                       this.currentMethod,
                                       id,
                                       var);
     }
     else
     {
       var = this.frames.peek().allocLocal(true);
       // Allocate class variable on current frame
       this.symbols.addClassVar( this.currentClass,
                                 id,
                                 var);
     }
     
     return var;
   }
   
   private Set<String> lookupClassVars(String className)
   {
     return this.symbols.lookupClassVars(className);
   }
   
   private Access lookupVar(String id)
   {
     Access var = null;
     if(this.currentMethod != null)
     {
       var = this.symbols.lookupMethodVar(this.currentClass, this.currentMethod, id);
     }
     
     if(var == null)
     {
       var = this.symbols.lookupClassVar(this.currentClass, id);
     }
     
     return var;
   }
   
   // ---------------------------------------------------------------------------
   
   private class SymbolTable
   {
     private HashMap<String, ClassTable> table = new HashMap<String, ClassTable>();
     
     public void addClassMethod(String className, String methodName)
     {
       if(!this.table.containsKey(className))
       {
         this.table.put(className, new ClassTable());
       }
       
       this.table.get(className).addMethod(methodName);
     }
     
     public void addClassMethodVar(String className, String methodName, String id, Access var)
     {
       if(!this.table.containsKey(className))
       {
         this.table.put(className, new ClassTable());
       }
       
       this.table.get(className).addMethodVar(methodName, id, var);
     }
     
     public void addClassVar(String className, String id, Access var)
     {
       if(!this.table.containsKey(className))
       {
         this.table.put(className, new ClassTable());
       }
       
       this.table.get(className).addClassVar(id, var);
     }
     
     private Set<String> lookupClassVars(String className)
     {
       if(!this.table.containsKey(className)) { return null; }
       
       return this.table.get(className).classVars();
     }
     
     public Access lookupClassVar(String className, String id)
     {
       if(!this.table.containsKey(className)) { return null; }
       
       return this.table.get(className).lookupClassVar(id);
     }
     
     public Access lookupMethodVar(String className, String methodName, String id)
     {
       if(!this.table.containsKey(className)) { return null; }
       
       return this.table.get(className).lookupMethodVar(methodName, id);
     }
     
     // -------------------------------------------------------------------------
     
     private class ClassTable
     {
       private HashMap<String, Access>                  vars     = new HashMap<String, Access>();
       private HashMap<String, HashMap<String, Access>> methods  = new HashMap<String, HashMap<String, Access>>();
       
       public void addClassVar(String id, Access var)
       {
         this.vars.put(id, var);
       }
       
       public void addMethod(String methodName)
       {
         if(!this.methods.containsKey(methodName))
         {
           this.methods.put(methodName, new HashMap<String, Access>());
         }
       }
       
       public void addMethodVar(String methodName, String id, Access var)
       {
         this.addMethod(methodName);
         this.methods.get(methodName).put(id, var);
       }
       
       public Set<String> classVars()
       {
         return this.vars.keySet();
       }
       
       public Access lookupClassVar(String id)
       {
         return this.vars.get(id);
       }
       
       public Access lookupMethodVar(String methodName, String id)
       {
         if(!this.methods.containsKey(methodName)) { return null; }
         
         return this.methods.get(methodName).get(id);
       }
     }
   }
 }
