 package cs444.codegen;
 
 import java.io.PrintStream;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import cs444.codegen.InstructionArg.Size;
 import cs444.codegen.instructions.Add;
 import cs444.codegen.instructions.Call;
 import cs444.codegen.instructions.Cmp;
 import cs444.codegen.instructions.Comment;
 import cs444.codegen.instructions.Global;
 import cs444.codegen.instructions.Instruction;
 import cs444.codegen.instructions.Int;
 import cs444.codegen.instructions.Jmp;
 import cs444.codegen.instructions.Jne;
 import cs444.codegen.instructions.Label;
 import cs444.codegen.instructions.Leave;
 import cs444.codegen.instructions.Mov;
 import cs444.codegen.instructions.Movsx;
 import cs444.codegen.instructions.Movzx;
 import cs444.codegen.instructions.Neg;
 import cs444.codegen.instructions.Pop;
 import cs444.codegen.instructions.Push;
 import cs444.codegen.instructions.Ret;
 import cs444.codegen.instructions.Sar;
 import cs444.codegen.instructions.Xor;
 import cs444.codegen.instructions.factories.AddOpMaker;
 import cs444.codegen.instructions.factories.AndOpMaker;
 import cs444.codegen.instructions.factories.BinOpMaker;
 import cs444.codegen.instructions.factories.BinUniOpMaker;
 import cs444.codegen.instructions.factories.CmpMaker;
 import cs444.codegen.instructions.factories.IDivMaker;
 import cs444.codegen.instructions.factories.IMulMaker;
 import cs444.codegen.instructions.factories.OrOpMaker;
 import cs444.codegen.instructions.factories.SeteMaker;
 import cs444.codegen.instructions.factories.SetlMaker;
 import cs444.codegen.instructions.factories.SetleMaker;
 import cs444.codegen.instructions.factories.SetneMaker;
 import cs444.codegen.instructions.factories.SubOpMaker;
 import cs444.codegen.instructions.factories.UniOpMaker;
 import cs444.parser.symbols.ANonTerminal;
 import cs444.parser.symbols.ATerminal;
 import cs444.parser.symbols.ISymbol;
 import cs444.parser.symbols.JoosNonTerminal;
 import cs444.parser.symbols.ast.AInterfaceOrClassSymbol;
 import cs444.parser.symbols.ast.AModifiersOptSymbol.ProtectionLevel;
 import cs444.parser.symbols.ast.BooleanLiteralSymbol;
 import cs444.parser.symbols.ast.ByteLiteralSymbol;
 import cs444.parser.symbols.ast.CharacterLiteralSymbol;
 import cs444.parser.symbols.ast.DclSymbol;
 import cs444.parser.symbols.ast.FieldAccessSymbol;
 import cs444.parser.symbols.ast.IntegerLiteralSymbol;
 import cs444.parser.symbols.ast.MethodInvokeSymbol;
 import cs444.parser.symbols.ast.MethodOrConstructorSymbol;
 import cs444.parser.symbols.ast.NameSymbol;
 import cs444.parser.symbols.ast.NullSymbol;
 import cs444.parser.symbols.ast.ShortLiteralSymbol;
 import cs444.parser.symbols.ast.StringLiteralSymbol;
 import cs444.parser.symbols.ast.SuperSymbol;
 import cs444.parser.symbols.ast.ThisSymbol;
 import cs444.parser.symbols.ast.TypeSymbol;
 import cs444.parser.symbols.ast.Typeable;
 import cs444.parser.symbols.ast.expressions.AddExprSymbol;
 import cs444.parser.symbols.ast.expressions.AndExprSymbol;
 import cs444.parser.symbols.ast.expressions.ArrayAccessExprSymbol;
 import cs444.parser.symbols.ast.expressions.AssignmentExprSymbol;
 import cs444.parser.symbols.ast.expressions.BinOpExpr;
 import cs444.parser.symbols.ast.expressions.CastExpressionSymbol;
 import cs444.parser.symbols.ast.expressions.CreationExpression;
 import cs444.parser.symbols.ast.expressions.DivideExprSymbol;
 import cs444.parser.symbols.ast.expressions.EAndExprSymbol;
 import cs444.parser.symbols.ast.expressions.EOrExprSymbol;
 import cs444.parser.symbols.ast.expressions.EqExprSymbol;
 import cs444.parser.symbols.ast.expressions.ForExprSymbol;
 import cs444.parser.symbols.ast.expressions.IfExprSymbol;
 import cs444.parser.symbols.ast.expressions.InstanceOfExprSymbol;
 import cs444.parser.symbols.ast.expressions.LeExprSymbol;
 import cs444.parser.symbols.ast.expressions.LtExprSymbol;
 import cs444.parser.symbols.ast.expressions.MultiplyExprSymbol;
 import cs444.parser.symbols.ast.expressions.NeExprSymbol;
 import cs444.parser.symbols.ast.expressions.NegOpExprSymbol;
 import cs444.parser.symbols.ast.expressions.NotOpExprSymbol;
 import cs444.parser.symbols.ast.expressions.OrExprSymbol;
 import cs444.parser.symbols.ast.expressions.RemainderExprSymbol;
 import cs444.parser.symbols.ast.expressions.ReturnExprSymbol;
 import cs444.parser.symbols.ast.expressions.SubtractExprSymbol;
 import cs444.parser.symbols.ast.expressions.WhileExprSymbol;
 import cs444.types.APkgClassResolver;
 
 public class CodeGenVisitor implements ICodeGenVisitor {
     private final SelectorIndexedTable sit;
     private final List<Instruction> instructions = new LinkedList<Instruction>();
     private boolean hasEntry = false;
     private boolean getVal = true;
 
     private boolean lastWasFunc = false;
 
     //FFFFFFFF is easy to see if something went wrong
     private long lastOffset = 0xFFFFFFFF;
 
     private Size lastSize = Size.DWORD;
 
     private int nextLblnum = 0;
 
     private int getNewLblNum(){
         return nextLblnum++;
     }
 
     public CodeGenVisitor(SelectorIndexedTable sit) {
         this.sit = sit;
     }
 
     @Override
     public void visit(MethodInvokeSymbol invoke) {
         if(invoke.hasFirst){
           //TODO visit first and make this like a recusive lookup for name during that part
         }
 
         for(ISymbol arg : invoke.getArgs()){
             arg.accept(this);
            instructions.add(new Push(Register.ACCUMULATOR, lastSize));
         }
 
         InstructionArg arg = new Immediate(APkgClassResolver.generateFullId(invoke.getCallSymbol()));
         instructions.add(new Call(arg));
 
         if(invoke.getStackSize() != 0){
             long size = (invoke.getStackSize() - SizeHelper.DEFAULT_STACK_SIZE) / 8;
             Immediate by = new Immediate(String.valueOf(size));
             instructions.add(new Add(Register.STACK, by));
         }
     }
 
     @Override
     public void visit(FieldAccessSymbol field) {
         // TODO Auto-generated method stub
     }
 
     @Override
     public void visit(AInterfaceOrClassSymbol aInterfaceOrClassSymbol) {
         // TODO More?
         for(ISymbol child : aInterfaceOrClassSymbol.children) child.accept(this);
     }
 
     @Override
     public void visit(MethodOrConstructorSymbol method) {
         String methodName = APkgClassResolver.generateFullId(method);
         Runtime.externAll(instructions);
 
         try{
             if(APkgClassResolver.generateUniqueName(method, method.dclName).equals(JoosNonTerminal.ENTRY)
                     && method.isStatic() && !hasEntry){
 
                 hasEntry = true;
                 instructions.add(new Global("_start"));
                 instructions.add(new Label("_start"));
                 //TODO any static init needs to happen here
                 instructions.add(new Call(new Immediate(methodName)));
                 instructions.add(new Mov(Register.BASE, Register.ACCUMULATOR));
                 instructions.add(new Mov(Register.ACCUMULATOR, Immediate.EXIT));
                 instructions.add(new Int(Immediate.SOFTWARE_INTERUPT));
             }
         }catch(Exception e){
             //Should never get here.
             e.printStackTrace();
         }
 
         if(method.getProtectionLevel() != ProtectionLevel.PRIVATE) instructions.add(new Global(methodName));
         instructions.add(new Label(methodName));
 
         lastWasFunc = true;
 
         instructions.add(Push.STACK_FRAME);
         instructions.add(new Mov(Register.FRAME, Register.STACK));
 
         for(ISymbol child : method.children) child.accept(this);
 
         instructions.add(new Comment("End of method " + method.dclName));
     }
 
     @Override
     public void visit(CreationExpression creationExpression) {
         APkgClassResolver typeDclNode = creationExpression.getType().getTypeDclNode();
 
         if (!creationExpression.getType().isArray){
             long bytes = typeDclNode.getObjectSize();
 
             instructions.add(new Comment("Allocate " + bytes + " bytes for " + typeDclNode.fullName));
             Runtime.malloc(bytes, instructions);
             ObjectLayout.initialize(typeDclNode, instructions);
         }else{
             // TODO: do array creation here and change instruction comment
             instructions.add(new Comment("Allocate for " + typeDclNode.fullName + " no done yet"));
         }
         // TODO call constructor
     }
 
     @Override
     public void visit(ANonTerminal aNonTerminal) {
         boolean isBlock = aNonTerminal.getName().equals(JoosNonTerminal.BLOCK);
         boolean lastFunc = lastWasFunc;
         lastWasFunc = false;
 
         for(ISymbol child : aNonTerminal.children) child.accept(this);
 
         if(isBlock && !lastFunc){
             long size = aNonTerminal.getStackSize();
             if(0 != size){
                 Immediate by = new Immediate(String.valueOf(size));
                 instructions.add(new Add(Register.STACK, by));
             }
         }
         lastWasFunc = lastFunc;
     }
 
     @Override
     public void visit(WhileExprSymbol whileExprSymbol) {
         int mynum = getNewLblNum();
         instructions.add(new Comment("while start " + mynum));
         String loopStart = "loopStart" + mynum;
         String loopEnd = "loopEnd" + mynum;
 
         instructions.add(new Label(loopStart));
         whileExprSymbol.getConditionSymbol().accept(this);
 
         setupJumpNe(Register.ACCUMULATOR, Immediate.TRUE, loopEnd);
 
         whileExprSymbol.getBody().accept(this);
 
         instructions.add(new Jmp(new Immediate(loopStart)));
         instructions.add(new Label(loopEnd));
         instructions.add(new Comment("while end " + mynum));
     }
 
     @Override
     public void visit(ForExprSymbol forExprSymbol) {
         int mynum = getNewLblNum();
         instructions.add(new Comment("for start " + mynum));
         String loopStart = "loopStart" + mynum;
         String loopEnd = "loopEnd" + mynum;
 
         instructions.add(new Comment("Init for " + mynum));
         forExprSymbol.getForInit().accept(this);
         instructions.add(new Label(loopStart));
         instructions.add(new Comment("Compare for " + mynum));
         forExprSymbol.getConditionExpr().accept(this);
 
         setupJumpNe(Register.ACCUMULATOR, Immediate.TRUE, loopEnd);
 
         instructions.add(new Comment("for body" + mynum));
 
         forExprSymbol.getBody().accept(this);
 
         instructions.add(new Comment("for update " + mynum));
         forExprSymbol.getForUpdate().accept(this);
 
         instructions.add(new Jmp(new Immediate(loopStart)));
         instructions.add(new Label(loopEnd));
 
         //This takes care of the init if they dcl something there
         long size = forExprSymbol.getStackSize();
         if(0 != size){
             Immediate by = new Immediate(String.valueOf(size));
             instructions.add(new Comment("for stack " + mynum));
             instructions.add(new Add(Register.STACK, by));
         }
 
         instructions.add(new Comment("for end " + mynum));
     }
 
     @Override
     public void visit(IfExprSymbol ifExprSymbol) {
         int myid = getNewLblNum();
         instructions.add(new Comment("if start" + myid));
         String falseLbl = "false" + myid;
         String trueLbl = "true" + myid;
         ifExprSymbol.getConditionSymbol().accept(this);
 
         setupJumpNe(Register.ACCUMULATOR, Immediate.TRUE, falseLbl);
 
         ifExprSymbol.getifBody().accept(this);
 
         instructions.add(new Jmp(new Immediate(trueLbl)));
         instructions.add(new Label(falseLbl));
 
         ISymbol elseSymbol = ifExprSymbol.getElseBody();
 
         if(elseSymbol != null) elseSymbol.accept(this);
 
         instructions.add(new Label(trueLbl));
         instructions.add(new Comment("if end" + myid));
     }
 
     @Override
     public void visit(ReturnExprSymbol retSymbol) {
         if(retSymbol.children.size() == 1) retSymbol.children.get(0).accept(this);
         //value is already in eax from the rule therefore we just need to ret
         instructions.add(Leave.LEAVE);
         instructions.add(Ret.RET);
     }
 
     @Override
     public void visit(TypeSymbol typeSymbol) {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void visit(NameSymbol nameSymbol) {
         final DclSymbol lastDcl = nameSymbol.getLastLookupDcl();
         long lastDclOffset = lastDcl.getOffset();
         long stackSize = lastDcl.getType().getTypeDclNode().realSize;
 
         if(lastDcl.isLocal()){
             genCodeForLocalVar(nameSymbol, lastDcl, lastDclOffset, stackSize);
         }else{
             // instance fields
             Iterator<Typeable> lookup = nameSymbol.getLookupPath().iterator();
             Typeable type = lookup.next();
             DclSymbol first;
             if(type instanceof DclSymbol &&
                     (first = (DclSymbol) type).isLocal()){
                 final long localObjOffset = first.getOffset();
                 instructions.add(new Comment("Move pointer of obj " + first.dclName + " in " + nameSymbol.value + " to Accumulator"));
                 instructions.add(new Mov(Register.ACCUMULATOR, new PointerRegister(Register.FRAME, localObjOffset)));
                 // TODO: do null check
 
                 while(lookup.hasNext() && (type = lookup.next()) != lastDcl){
                     DclSymbol dclSymbol = (DclSymbol) type;
                     instructions.add(new Comment("Move reference to field " + dclSymbol.dclName + " in " +
                             nameSymbol.value + " to Accumulator"));
                     PointerRegister from = new PointerRegister(Register.ACCUMULATOR, dclSymbol.getOffset());
                     instructions.add(new Mov(Register.ACCUMULATOR, from));
                 }
 
                 // now type is last in qualified name => target field
                 if(getVal){
                     instructions.add(new Comment("Move value of field " + lastDcl.dclName + " in " + nameSymbol.value + " to Accumulator"));
                     instructions.add(new Mov(Register.ACCUMULATOR, new PointerRegister(Register.ACCUMULATOR, lastDclOffset)));
                 }else{
                     instructions.add(new Comment("Move reference to field " + lastDcl.dclName + " in " + nameSymbol.value + " to Accumulator"));
                     instructions.add(new Add(Register.ACCUMULATOR, new Immediate(Long.toString(lastDclOffset))));
                     lastSize = SizeHelper.getSize(stackSize);
                 }
             }
         }
     }
 
     private void genCodeForLocalVar(NameSymbol nameSymbol, final DclSymbol dcl,
             long offset, long stackSize) {
         if(getVal){
             genCodeForLocalVarGetValue(nameSymbol, dcl, offset, stackSize);
         }else{
             lastOffset = offset;
             lastSize = SizeHelper.getSize(stackSize);
         }
     }
 
     private void genCodeForLocalVarGetValue(NameSymbol nameSymbol,
             final DclSymbol dcl, long offset, long stackSize) {
         Size size = SizeHelper.getSize(stackSize);
         final InstructionArg from = new PointerRegister(Register.FRAME, offset);
         Instruction instruction;
 
         if(size == Size.DWORD){
             instruction = new Mov(Register.ACCUMULATOR, from);
         }else if(JoosNonTerminal.unsigned.contains(dcl.getType().getTypeDclNode().fullName)){
             instruction = new Movzx(Register.ACCUMULATOR, from, size);
         }else{
             instruction = new Movsx(Register.ACCUMULATOR, from, size);
         }
 
         instructions.add(new Comment("getting value of " + nameSymbol.value));
         instructions.add(instruction);
     }
 
     @Override
     public void visit(ATerminal terminal) {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void visit(CastExpressionSymbol symbol) {
         TypeSymbol type = symbol.getType();
         //TODO object cast to primative
         if(!JoosNonTerminal.primativeNumbers.contains(type.getTypeDclNode().fullName)) type.accept(this);
         symbol.getOperandExpression().accept(this);
     }
 
     @Override
     public void visit(NegOpExprSymbol op) {
         op.children.get(0).accept(this);
         instructions.add(new Neg(Register.ACCUMULATOR));
     }
 
     @Override
     public void visit(NotOpExprSymbol op) {
         op.children.get(0).accept(this);
         instructions.add(new Xor(Register.ACCUMULATOR, Immediate.TRUE));
     }
 
     @Override
     public void visit(MultiplyExprSymbol op) {
         binUniOpHelper(op, IMulMaker.maker, false);
     }
 
     @Override
     public void visit(AssignmentExprSymbol op) {
         op.children.get(1).accept(this);
         boolean tmp = getVal;
         getVal = false;
         instructions.add(new Push(Register.ACCUMULATOR));
 
         // LHS
         lastOffset = -1;
         op.children.get(0).accept(this);
 
         instructions.add(new Pop(Register.DATA));
 
         if (lastOffset != -1){
             InstructionArg to = new PointerRegister(Register.FRAME, lastOffset);
             instructions.add(new Mov(to, Register.DATA, lastSize));
         }else{
             InstructionArg to = new PointerRegister(Register.ACCUMULATOR);
             instructions.add(new Mov(to, Register.DATA, lastSize));
         }
 
         instructions.add(new Comment("Move result of assignment expr to Accumulator, so it's available to parent"));
         instructions.add(new Mov(Register.ACCUMULATOR, Register.DATA));
         getVal = tmp;
     }
 
     @Override
     public void visit(DivideExprSymbol op) {
         instructions.add(new Comment("START"));
         binUniOpHelper(op, IDivMaker.maker, true);
         instructions.add(new Comment("END"));
     }
 
     @Override
     public void visit(RemainderExprSymbol op) {
         binUniOpHelper(op, IDivMaker.maker, true);
         instructions.add(new Mov(Register.ACCUMULATOR, Register.DATA));
     }
 
     @Override
     public void visit(AddExprSymbol op) {
         binOpHelper(op, AddOpMaker.maker);
     }
 
     @Override
     public void visit(SubtractExprSymbol op) {
         binOpHelper(op, SubOpMaker.maker);
     }
 
     @Override
     public void visit(LtExprSymbol op) {
         compHelper(op, SetlMaker.maker);
     }
 
     @Override
     public void visit(EqExprSymbol op) {
         compHelper(op, SeteMaker.maker);
     }
 
     @Override
     public void visit(NeExprSymbol op) {
         compHelper(op, SetneMaker.maker);
 
     }
 
     @Override
     public void visit(AndExprSymbol op) {
         String andEnd = "and" + getNewLblNum();
         op.children.get(0).accept(this);
         setupJumpNe(Register.ACCUMULATOR, Immediate.TRUE, andEnd);
         op.children.get(1).accept(this);
         setupJumpNe(Register.ACCUMULATOR, Immediate.TRUE, andEnd);
         instructions.add(new Label(andEnd));
     }
 
     @Override
     public void visit(OrExprSymbol op) {
         String orEnd = "or" + getNewLblNum();
         op.children.get(0).accept(this);
         setupJumpNe(Register.ACCUMULATOR, Immediate.FALSE, orEnd);
         op.children.get(1).accept(this);
         setupJumpNe(Register.ACCUMULATOR, Immediate.FALSE, orEnd);
         instructions.add(new Label(orEnd));
     }
 
     @Override
     public void visit(EAndExprSymbol op) {
         binOpHelper(op, AndOpMaker.maker);
     }
 
     @Override
     public void visit(EOrExprSymbol op) {
         binOpHelper(op, OrOpMaker.maker);
     }
 
     @Override
     public void visit(LeExprSymbol op) {
         compHelper(op, SetleMaker.maker);
     }
 
     @Override
     public void visit(InstanceOfExprSymbol op) {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void visit(IntegerLiteralSymbol intLiteral) {
         instructions.add(new Mov(Register.ACCUMULATOR, new Immediate(String.valueOf(intLiteral.getValue()))));
         lastSize = Size.DWORD;
     }
 
     @Override
     public void visit(NullSymbol nullSymbol) {
         instructions.add(new Mov(Register.ACCUMULATOR, Immediate.NULL));
         lastSize = Size.DWORD;
     }
 
     @Override
     public void visit(BooleanLiteralSymbol boolSymbol) {
         instructions.add(new Mov(Register.ACCUMULATOR, boolSymbol.boolValue ? Immediate.TRUE : Immediate.FALSE));
         lastSize = Size.WORD;
     }
 
     @Override
     public void visit(ThisSymbol thisSymbol) {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void visit(SuperSymbol superSymbol) {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void visit(StringLiteralSymbol stringSymbol) {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void visit(CharacterLiteralSymbol characterSymbol) {
         instructions.add(new Mov(Register.ACCUMULATOR, new Immediate(String.valueOf(characterSymbol.getValue()))));
     }
 
     @Override
     public void visit(ArrayAccessExprSymbol arrayAccess) {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void visit(DclSymbol dclSymbol) {
         if(dclSymbol.children.isEmpty()) new IntegerLiteralSymbol(0).accept(this);
         else dclSymbol.children.get(0).accept(this);
         instructions.add(new Push(Register.ACCUMULATOR, SizeHelper.getSize(dclSymbol.getType().getTypeDclNode().stackSize)));
     }
 
     @Override
     public void visit(ByteLiteralSymbol byteLiteral) {
         instructions.add(new Mov(Register.ACCUMULATOR, new Immediate(String.valueOf(byteLiteral.getValue()))));
     }
 
     @Override
     public void visit(ShortLiteralSymbol shortLiteral) {
         instructions.add(new Mov(Register.ACCUMULATOR, new Immediate(String.valueOf(shortLiteral.getValue()))));
         lastSize = Size.WORD;
     }
 
     @Override
     public void printToFileAndEmpty(PrintStream printer){
         for(Instruction instruction : instructions) printer.println(instruction.generate());
         instructions.clear();
     }
 
     private void binOpHelper(BinOpExpr bin, BinOpMaker maker){
         instructions.add(new Push(Register.BASE));
         bin.children.get(1).accept(this);
         instructions.add(new Mov(Register.BASE, Register.ACCUMULATOR));
         bin.children.get(0).accept(this);
         instructions.add(maker.make(Register.ACCUMULATOR, Register.BASE));
         instructions.add(new Pop(Register.BASE));
     }
 
     private void compHelper(BinOpExpr bin, UniOpMaker uni){
         binOpHelper(bin, CmpMaker.maker);
         instructions.add(uni.make(Register.ACCUMULATOR));
     }
 
     private void binUniOpHelper(BinOpExpr bin, BinUniOpMaker maker, boolean sar){
         instructions.add(new Push(Register.BASE));
         bin.children.get(1).accept(this);
         instructions.add(new Mov(Register.BASE, Register.ACCUMULATOR));
         bin.children.get(0).accept(this);
 
         if(sar){
             instructions.add(new Mov(Register.DATA, Register.ACCUMULATOR));
             instructions.add(new Sar(Register.DATA, Immediate.PREP_EDX));
             String safeDiv = "safeDiv" + getNewLblNum();
             setupJumpNe(Register.BASE, Immediate.ZERO, safeDiv);
             Runtime.throwException(instructions, "Divide by zero");
             instructions.add(new Label(safeDiv));
         }
 
         instructions.add(maker.make(Register.BASE));
         instructions.add(new Pop(Register.BASE));
     }
 
     private void setupJumpNe(Register reg, Immediate when, String lblTo){
         instructions.add(new Cmp(reg, when));
         instructions.add(new Jne(new Immediate(lblTo)));
     }
 }
