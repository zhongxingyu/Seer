 package cs444.codegen;
 
 import java.io.PrintStream;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import cs444.codegen.InstructionArg.Size;
 import cs444.codegen.instructions.Add;
 import cs444.codegen.instructions.Call;
 import cs444.codegen.instructions.Cmp;
 import cs444.codegen.instructions.Comment;
 import cs444.codegen.instructions.Extern;
 import cs444.codegen.instructions.Global;
 import cs444.codegen.instructions.Instruction;
 import cs444.codegen.instructions.Int;
 import cs444.codegen.instructions.Je;
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
 import cs444.codegen.instructions.ReserveInstruction;
 import cs444.codegen.instructions.Ret;
 import cs444.codegen.instructions.Sar;
 import cs444.codegen.instructions.Section;
 import cs444.codegen.instructions.Section.SectionType;
 import cs444.codegen.instructions.Shl;
 import cs444.codegen.instructions.Xor;
 import cs444.codegen.instructions.factories.AddOpMaker;
 import cs444.codegen.instructions.factories.AndOpMaker;
 import cs444.codegen.instructions.factories.BinOpMaker;
 import cs444.codegen.instructions.factories.BinUniOpMaker;
 import cs444.codegen.instructions.factories.CmpMaker;
 import cs444.codegen.instructions.factories.IDivMaker;
 import cs444.codegen.instructions.factories.IMulMaker;
 import cs444.codegen.instructions.factories.OrOpMaker;
 import cs444.codegen.instructions.factories.ReserveInstructionMaker;
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
 import cs444.parser.symbols.ast.AModifiersOptSymbol.ImplementationLevel;
 import cs444.parser.symbols.ast.AModifiersOptSymbol.ProtectionLevel;
 import cs444.parser.symbols.ast.BooleanLiteralSymbol;
 import cs444.parser.symbols.ast.ByteLiteralSymbol;
 import cs444.parser.symbols.ast.CharacterLiteralSymbol;
 import cs444.parser.symbols.ast.ConstructorSymbol;
 import cs444.parser.symbols.ast.DclSymbol;
 import cs444.parser.symbols.ast.FieldAccessSymbol;
 import cs444.parser.symbols.ast.IntegerLiteralSymbol;
 import cs444.parser.symbols.ast.MethodInvokeSymbol;
 import cs444.parser.symbols.ast.MethodOrConstructorSymbol;
 import cs444.parser.symbols.ast.MethodSymbol;
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
 import cs444.types.ArrayPkgClassResolver;
 import cs444.types.PkgClassInfo;
 import cs444.types.PkgClassResolver;
 import cs444.types.exceptions.UndeclaredException;
 
 public class CodeGenVisitor implements ICodeGenVisitor {
     private static final String INIT_OBJECT_FUNC = "__init_object";
     private final SelectorIndexedTable selectorITable;
     private final SubtypeIndexedTable subtypeITable;
     private final List<Instruction> instructions;
     private boolean hasEntry = false;
     private boolean getVal = true;
 
     private boolean lastWasFunc = false;
 
     //FFFFFFFF is easy to see if something went wrong
     private long lastOffset = 0xFFFFFFFF;
 
     private Size lastSize = Size.DWORD;
 
     private long nextLblnum = 0;
 
     private APkgClassResolver currentFile;
 
     private boolean isFieldLookup = false;
 
     private long getNewLblNum(){
         return nextLblnum++;
     }
 
     public CodeGenVisitor(SelectorIndexedTable sit, SubtypeIndexedTable subIt) {
         this.instructions = new LinkedList<Instruction>();
         this.selectorITable = sit;
         this.subtypeITable = subIt;
     }
 
     public CodeGenVisitor(SelectorIndexedTable sit, SubtypeIndexedTable subIt, List<Instruction> startInstructions) {
         this(null, sit, subIt, startInstructions);
     }
 
     public CodeGenVisitor(APkgClassResolver resolver,
             SelectorIndexedTable sit, SubtypeIndexedTable subIt, List<Instruction> startInstructions) {
         this.currentFile = resolver;
         this.instructions = startInstructions;
         this.selectorITable = sit;
         this.subtypeITable = subIt;
     }
 
     public void genHeader(APkgClassResolver resolver) {
         this.currentFile = resolver;
 
         Runtime.externAll(instructions);
         instructions.add(new Section(SectionType.TEXT));
 
         instructions.add(new Comment(INIT_OBJECT_FUNC + ": call super default constructor and initialize obj fields." +
                 " eax should contain address of object."));
         instructions.add(new Label(INIT_OBJECT_FUNC));
 
         APkgClassResolver superResolver = null;
 
         superResolver = resolver.getSuper();
 
         if (!resolver.fullName.equals(APkgClassResolver.OBJECT)){
             invokeConstructor(superResolver, Collections.<ISymbol>emptyList());
         }
 
         instructions.add(new Comment("Store pointer to object in edx"));
         instructions.add(new Mov(Register.DATA, Register.ACCUMULATOR));
 
         for (DclSymbol fieldDcl : resolver.getUninheritedNonStaticFields()) {
             Size size = SizeHelper.getSize(fieldDcl.getType().getTypeDclNode().realSize);
 
             PointerRegister fieldAddr = new PointerRegister(Register.DATA, fieldDcl.getOffset());
             if(!fieldDcl.children.isEmpty()){
                 instructions.add(new Comment("Initializing field " + fieldDcl.dclName + "."));
                 // save pointer to object
                 instructions.add(new Push(Register.DATA));
                 fieldDcl.children.get(0).accept(new CodeGenVisitor(currentFile, selectorITable, subtypeITable, instructions));
                 instructions.add(new Comment("Pop the object address to edx"));
                 instructions.add(new Pop(Register.DATA));
                 instructions.add(new Mov(fieldAddr, Register.ACCUMULATOR, size));
             }
         }
 
         instructions.add(Ret.RET);
     }
 
     public void genLayoutForStaticFields(Iterable<DclSymbol> staticFields) {
         if (staticFields.iterator().hasNext()){
             instructions.add(new Comment("Static fields:"));
             instructions.add(new Section(SectionType.BSS));
         }
 
         for (DclSymbol fieldDcl : staticFields) {
             Size size = SizeHelper.getSize(fieldDcl.getType().getTypeDclNode().realSize);
 
             String fieldLbl = APkgClassResolver.getUniqueNameFor(fieldDcl);
             instructions.add(new Global(fieldLbl));
             ReserveInstruction data = ReserveInstructionMaker.make(fieldLbl, size, 1);
             instructions.add(data);
         }
     }
 
     @Override
     public void visit(MethodInvokeSymbol invoke) {
         MethodOrConstructorSymbol call = invoke.getCallSymbol();
         if(!call.isStatic()){
             instructions.add(new Comment("Preping this"));
             final Iterator<Typeable> lookup = invoke.getLookup().dcls.iterator();
             Typeable first = lookup.next();
 
             if(invoke.hasFirst){
                 invoke.children.get(0).accept(this);
                 first = lookup.next();
             }
 
             if(first != call){
                 lookupLink(call.dclName, lookup, first, call, 0, !isFieldLookup && ! invoke.hasFirst);
                 instructions.add(new Mov(Register.COUNTER, Register.ACCUMULATOR));
             }else if(invoke.hasFirst || isFieldLookup){
                 instructions.add(new Mov(Register.COUNTER, Register.ACCUMULATOR));
             }else{
                 instructions.add(new Mov(Register.COUNTER, PointerRegister.THIS));
             }
         }
 
         instructions.add(new Comment("Pushing args"));
         for(ISymbol arg : invoke.getArgs()){
             arg.accept(this);
             instructions.add(new Push(Register.ACCUMULATOR, SizeHelper.getPushSize(lastSize)));
         }
 
         if(!call.isStatic()) instructions.add(new Push(Register.COUNTER));
 
         if(call.isStatic() || call.getImplementationLevel() == ImplementationLevel.FINAL){
             InstructionArg arg = new Immediate(APkgClassResolver.generateFullId(invoke.getCallSymbol()));
             if(invoke.getCallSymbol().dclInResolver != currentFile) instructions.add(new Extern(arg));
             instructions.add(new Call(arg));
         }else{
             // TODO: check why this line breaks some tests. Maybe it uncovers a hidden bug:
             // ifNullJmpCode(Register.COUNTER, Runtime.EXCEPTION_LBL);
             instructions.add(new Comment("get SIT column"));
             instructions.add(new Mov(Register.COUNTER, new PointerRegister(Register.COUNTER)));
 
             PointerRegister methodAddr = null;
             try {
                 methodAddr = new PointerRegister(Register.COUNTER, selectorITable.getOffset(PkgClassResolver.generateUniqueName(call, call.dclName)));
             } catch (UndeclaredException e) {
                 // shouldn't get here
                 e.printStackTrace();
             }
             instructions.add(new Mov(Register.COUNTER,  methodAddr));
             instructions.add(new Call(Register.COUNTER));
         }
 
         // NOTE: do not use INVOKE in here, invoke gets size from method,
         // but visitor may visit InvokeSymbol before MethodSymbol
         long mySize =  call.getStackSize();
         if(mySize != 0){
             Immediate by = new Immediate(String.valueOf(mySize));
             instructions.add(new Add(Register.STACK, by));
         }
 
         instructions.add(new Comment("end invoke"));
     }
 
     @Override
     public void visit(FieldAccessSymbol field) {
         field.children.get(0).accept(this);
         isFieldLookup = true;
         field.children.get(1).accept(this);
         isFieldLookup = false;
     }
 
     @Override
     public void visit(AInterfaceOrClassSymbol aInterfaceOrClassSymbol) {
         // TODO More?
         for(ISymbol child : aInterfaceOrClassSymbol.children) child.accept(this);
     }
 
     @Override
     public void visit(MethodSymbol method){
         String methodName = APkgClassResolver.generateFullId(method);
 
         try{
             if(APkgClassResolver.generateUniqueName(method, method.dclName).equals(JoosNonTerminal.ENTRY)
                     && method.isStatic() && !hasEntry){
 
                 hasEntry = true;
                 instructions.add(new Global("_start"));
                 instructions.add(new Label("_start"));
                 instructions.add(new Extern(new Immediate(StaticFieldInit.STATIC_FIELD_INIT_LBL)));
                 instructions.add(new Call(new Immediate(StaticFieldInit.STATIC_FIELD_INIT_LBL)));
                 instructions.add(new Call(new Immediate(methodName)));
                 instructions.add(new Mov(Register.BASE, Register.ACCUMULATOR));
                 instructions.add(new Mov(Register.ACCUMULATOR, Immediate.EXIT));
                 instructions.add(new Int(Immediate.SOFTWARE_INTERUPT));
             }
         }catch(Exception e){
             //Should never get here.
             e.printStackTrace();
         }
 
         methProlog(method, methodName);
 
         for(ISymbol child : method.children) child.accept(this);
 
         methEpilogue(method);
     }
 
     @Override
     public void visit(ConstructorSymbol constructor) {
         String constrName = APkgClassResolver.generateFullId(constructor);
         methProlog(constructor, constrName);
 
         instructions.add(new Mov(Register.ACCUMULATOR, PointerRegister.THIS));
 
         instructions.add(new Call(new Immediate(INIT_OBJECT_FUNC)));
 
         for(ISymbol child : constructor.children) child.accept(this);
 
         methEpilogue(constructor);
     }
 
     private void methProlog(MethodOrConstructorSymbol method, String methodName) {
         if(method.getProtectionLevel() != ProtectionLevel.PRIVATE) instructions.add(new Global(methodName));
         instructions.add(new Label(methodName));
 
         lastWasFunc = true;
 
         instructions.add(Push.STACK_FRAME);
         instructions.add(new Mov(Register.FRAME, Register.STACK));
     }
 
     private void methEpilogue(MethodOrConstructorSymbol method) {
         //Don't fall though void funcs
         instructions.add(Leave.LEAVE);
         instructions.add(Ret.RET);
 
         instructions.add(new Comment("End of method " + method.dclName));
     }
 
     @Override
     public void visit(CreationExpression creationExpression) {
         APkgClassResolver typeDclNode = creationExpression.getType().getTypeDclNode();
 
         if (!creationExpression.getType().isArray){
             InstructionArg bytes = new Immediate(String.valueOf(typeDclNode.getObjectSize()));
             instructions.add(new Comment("Allocate " + bytes.getValue() + " bytes for " + typeDclNode.fullName));
             instructions.add(new Mov(Register.ACCUMULATOR, bytes));
             Runtime.malloc(bytes, instructions);
         }else{
             instructions.add(new Comment("Getting size for array constuction"));
             creationExpression.children.get(0).accept(this);
             instructions.add(new Comment("Save the size of the array"));
             instructions.add(new Push(Register.ACCUMULATOR));
             //TODO add check that the size is > 0
             instructions.add(new Shl(Register.ACCUMULATOR, Immediate.STACK_SIZE_POWER));
 
             instructions.add(new Comment("Adding space for SIT, cast info, and length" + typeDclNode.fullName));
             final long baseSize = SizeHelper.DEFAULT_STACK_SIZE  * 2 + SizeHelper.getIntSize(Size.DWORD);
             final Immediate sizeI = new Immediate(String.valueOf(baseSize));
             instructions.add(new Add(Register.ACCUMULATOR, sizeI));
             instructions.add(new Comment("Allocate for array" + typeDclNode.fullName));
             Runtime.malloc(Register.ACCUMULATOR, instructions);
             instructions.add(new Comment("Pop the size"));
             instructions.add(new Pop(Register.DATA));
 
             final long lengthIndex = SizeHelper.DEFAULT_STACK_SIZE  * 2;
             Immediate li = new Immediate(String.valueOf(lengthIndex));
 
             instructions.add(new Mov(new PointerRegister(Register.ACCUMULATOR, li), Register.DATA));
         }
 
         ObjectLayout.initialize(typeDclNode, instructions);
 
         final APkgClassResolver resolver = creationExpression.getType().getTypeDclNode();
 
         List<ISymbol> children = creationExpression.children;
 
         instructions.add(new Comment("invoke Constructor"));
         invokeConstructor(resolver, children);
 
         instructions.add(new Comment("Done creating object"));
     }
 
     private void invokeConstructor(final APkgClassResolver resolver, List<ISymbol> children) {
         List<String> types = new LinkedList<String>();
 
         //put object in c
         instructions.add(new Mov(Register.COUNTER, Register.ACCUMULATOR));
 
         for(ISymbol child : children){
             child.accept(this);
             instructions.add(new Push(Register.ACCUMULATOR, SizeHelper.getPushSize(lastSize)));
             Typeable typeable = (Typeable) child;
             TypeSymbol ts = typeable.getType();
             types.add(ts.getTypeDclNode().fullName);
         }
 
         instructions.add(new Push(Register.COUNTER));
 
         ConstructorSymbol cs = null;
         try {
             cs = resolver.getConstructor(types, resolver);
         } catch (UndeclaredException e) {
             //Should never get here
             e.printStackTrace();
         }
 
         InstructionArg arg = new Immediate(APkgClassResolver.generateFullId(cs));
         if(cs.dclInResolver != currentFile) instructions.add(new Extern(arg));
         instructions.add(new Call(arg));
 
         //return value is the new object
         instructions.add(new Pop(Register.ACCUMULATOR));
 
         long mySize = cs.getStackSize() - SizeHelper.DEFAULT_STACK_SIZE;
         if(mySize != 0){
             Immediate by = new Immediate(String.valueOf(mySize));
             instructions.add(new Add(Register.STACK, by));
         }
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
         long mynum = getNewLblNum();
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
         long mynum = getNewLblNum();
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
         long myid = getNewLblNum();
         instructions.add(new Comment("if start" + myid));
         String falseLbl = "false" + myid;
         String trueLbl = "true" + myid;
         ifExprSymbol.getConditionSymbol().accept(this);
 
         setupJumpNe(Register.ACCUMULATOR, Immediate.TRUE, falseLbl);
 
         ifExprSymbol.getifBody().accept(this);
 
         instructions.add(new Jmp(new Immediate(trueLbl)));
         instructions.add(new Label(falseLbl));
 
         final ISymbol elseSymbol = ifExprSymbol.getElseBody();
 
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
 
         if(lastDcl.isLocal){
             genCodeForLocalVar(nameSymbol, lastDcl, lastDclOffset, stackSize);
             return;
         }
         // instance fields called on local var or on Class
         Iterator<Typeable> lookup = nameSymbol.getLookupPath().iterator();
         Typeable type = lookup.next();
         if (!(type instanceof DclSymbol)){
             // Not sure if this is possible:
             System.err.println("WARNING: got a type that is not DclSymbol when visiting a NameSymbol.");
             return;
         }
 
         if(lookupLink(nameSymbol.value, lookup, type, lastDcl, stackSize, !isFieldLookup)) return;
 
         // now type is last in qualified name => target field
         if(getVal){
             Size size = SizeHelper.getSize(lastDcl.getType().getTypeDclNode().realSize);
             instructions.add(new Comment("Move value of field " + lastDcl.dclName + " in " + nameSymbol.value + " to Accumulator"));
             genMov(size, new PointerRegister(Register.ACCUMULATOR, lastDclOffset), lastDcl.dclName, lastDcl);
         }else{
             instructions.add(new Comment("Move reference to field " + lastDcl.dclName + " in " + nameSymbol.value + " to Accumulator"));
             instructions.add(new Add(Register.ACCUMULATOR, new Immediate(Long.toString(lastDclOffset))));
         }
        lastSize = SizeHelper.getSize(stackSize);
     }
 
     private boolean lookupLink(String value, Iterator<Typeable> lookup, Typeable type,
             ISymbol lastDcl,  long stackSize, boolean forceThis){
 
         DclSymbol first = (DclSymbol) type;
         if (first.isLocal){
             final long localObjOffset = first.getOffset();
             instructions.add(new Comment("Move pointer of obj " + first.dclName + " in " + value + " to Accumulator"));
             instructions.add(new Mov(Register.ACCUMULATOR, new PointerRegister(Register.FRAME, localObjOffset)));
 
             ifNullJmpCode(Register.ACCUMULATOR, Runtime.EXCEPTION_LBL);
         }else if (first.getType().isClass){
             // is using static fields
             // TODO: test using chain of types a1.a2.a3...
             while(lookup.hasNext() && (type = lookup.next()).getType().isClass);
             DclSymbol staticField = (DclSymbol) type;
             final String staticFieldLbl = PkgClassResolver.getUniqueNameFor(staticField);
             if(staticField.dclInResolver != currentFile) instructions.add(new Extern(staticFieldLbl));
 
             if (staticField == lastDcl && getVal){
                 Size size = SizeHelper.getSize(staticField.getType().getTypeDclNode().realSize);
                 instructions.add(new Comment("Move value of static field " + staticField.dclName + " to Accumulator"));
                 instructions.add(new Mov(Register.ACCUMULATOR, new PointerRegister(new Immediate(staticFieldLbl)), size));
                 return true;
             }
             // get reference
             instructions.add(new Comment("Move label of static field " + staticField.dclName + " to Accumulator"));
             instructions.add(new Mov(Register.ACCUMULATOR, new Immediate(staticFieldLbl)));
             if (staticField == lastDcl){
                 lastSize = SizeHelper.getSize(stackSize);
                 return true;
             }
         }else if(forceThis){
             instructions.add(new Comment("Force This pointer"));
             instructions.add(new Mov(Register.ACCUMULATOR, PointerRegister.THIS));
 
             // use first as instance field if it's not last one in the chain
             if (type == lastDcl) return false;
             DclSymbol dclSymbol = (DclSymbol) type;
             instructions.add(new Comment("Move reference to field " + dclSymbol.dclName + " in " + value + " to Accumulator"));
             PointerRegister from = new PointerRegister(Register.ACCUMULATOR, dclSymbol.getOffset());
             instructions.add(new Mov(Register.ACCUMULATOR, from));
         }
 
         // the rest are non static fields
         while(lookup.hasNext() && (type = lookup.next()) != lastDcl){
             DclSymbol dclSymbol = (DclSymbol) type;
             instructions.add(new Comment("Move reference to field " + dclSymbol.dclName + " in " + value + " to Accumulator"));
             PointerRegister from = new PointerRegister(Register.ACCUMULATOR, dclSymbol.getOffset());
             instructions.add(new Mov(Register.ACCUMULATOR, from));
         }
         return false;
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
 
     private void genMov(Size size, InstructionArg from, String value, Typeable dcl){
         Instruction instruction;
         if(size == Size.DWORD){
             instruction = new Mov(Register.ACCUMULATOR, from);
         }else if(JoosNonTerminal.unsigned.contains(dcl.getType().getTypeDclNode().fullName)){
             instruction = new Movzx(Register.ACCUMULATOR, from, size);
         }else{
             instruction = new Movsx(Register.ACCUMULATOR, from, size);
         }
         instructions.add(new Comment("getting value of " + value));
         instructions.add(instruction);
     }
 
     private void genCodeForLocalVarGetValue(NameSymbol nameSymbol,
             final DclSymbol dcl, long offset, long stackSize) {
         Size size = SizeHelper.getSize(stackSize);
         final InstructionArg from = new PointerRegister(Register.FRAME, offset);
         genMov(size, from, nameSymbol.value, dcl);
     }
 
     @Override
     public void visit(ATerminal terminal) {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void visit(CastExpressionSymbol symbol) {
         TypeSymbol type = symbol.getType();
 
         type.accept(this);
         symbol.getOperandExpression().accept(this);
 
         String typeName = type.getTypeDclNode().fullName;
         if(!JoosNonTerminal.primativeNumbers.contains(typeName)
                 && !JoosNonTerminal.otherPrimatives.contains(typeName)){
             String castExprEnd = "CastExprEnd" + getNewLblNum();
             ifNullJmpCode(Register.ACCUMULATOR, castExprEnd);
 
             instructions.add(new Push(Register.ACCUMULATOR));
             ObjectLayout.subtypeCheckCode(type, subtypeITable, instructions);
             setupJumpNe(Register.ACCUMULATOR, Immediate.TRUE, Runtime.EXCEPTION_LBL);
             instructions.add(new Pop(Register.ACCUMULATOR));
 
             instructions.add(new Label(castExprEnd));
         }else{
             //TODO object cast to primative and other primitives
         }
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
         op.getLeftOperand().accept(this);
         // eax should have reference to object
         String nullObjectLbl = "nullObject" + getNewLblNum();
         ifNullJmpCode(Register.ACCUMULATOR, nullObjectLbl);
 
         ObjectLayout.subtypeCheckCode((TypeSymbol) op.getRightOperand(), subtypeITable, instructions);
 
         String endLbl = "instanceOfEnd" + getNewLblNum();
         instructions.add(new Jmp(new Immediate(endLbl)));
 
         instructions.add(new Label(nullObjectLbl));
         instructions.add(new Comment("set eax to FALSE"));
         instructions.add(new Mov(Register.ACCUMULATOR, Immediate.FALSE));
 
         instructions.add(new Label(endLbl));
     }
 
     private void ifNullJmpCode(Register register, String ifNullLbl) {
         instructions.add(new Comment("null check"));
         instructions.add(new Cmp(Register.ACCUMULATOR, Immediate.NULL));
         instructions.add(new Je(new Immediate(ifNullLbl)));
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
         instructions.add(new Comment("This pointer"));
         instructions.add(new Mov(Register.ACCUMULATOR, PointerRegister.THIS));
     }
 
     @Override
     public void visit(SuperSymbol superSymbol) {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void visit(StringLiteralSymbol stringSymbol) {
         instructions.add(new Comment("New String!"));
         instructions.add(new Comment("allocate the string at the same time (why not)"));
         final long charsLen = (stringSymbol.strValue.length() + SizeHelper.DEFAULT_STACK_SIZE) * 2 + SizeHelper.getIntSize(Size.DWORD);
         final long length =  charsLen + stringSymbol.getType().getTypeDclNode().getObjectSize();
 
         instructions.add(new Mov(Register.ACCUMULATOR, new Immediate(String.valueOf(length))));
         //no need to zero out, it will be set for sure
         Runtime.malloc(new Immediate(String.valueOf(length)), instructions, false);
         final String charArray = ArrayPkgClassResolver.getArrayName(JoosNonTerminal.CHAR);
         ObjectLayout.initialize(PkgClassInfo.instance.getSymbol(charArray), instructions);
 
         instructions.add(new Mov(new PointerRegister(Register.ACCUMULATOR, 8), new Immediate(String.valueOf(stringSymbol.strValue.length()))));
 
         final char [] cs = stringSymbol.strValue.toCharArray();
         for(int i = 0; i < cs.length; i++){
             final long place = 2 * i + SizeHelper.DEFAULT_STACK_SIZE * 2 + SizeHelper.getIntSize(Size.DWORD);
             final InstructionArg to = new PointerRegister(Register.ACCUMULATOR, new Immediate(String.valueOf(place)));
             instructions.add(new Mov(to, new Immediate((cs[i])), Size.WORD));
         }
         APkgClassResolver resolver = stringSymbol.getType().getTypeDclNode();
         try {
             final String arg = charArray;
             final ConstructorSymbol constructor = resolver.getConstructor(Arrays.asList(arg), resolver);
             instructions.add(new Comment("First arg to new String"));
             instructions.add(new Push(Register.ACCUMULATOR));
             instructions.add(new Comment("This pointer to new string"));
             instructions.add(new Add(Register.ACCUMULATOR, new Immediate(charsLen)));
             ObjectLayout.initialize(resolver, instructions);
             instructions.add(new Push(Register.ACCUMULATOR));
 
             InstructionArg carg = new Immediate(APkgClassResolver.generateFullId(constructor));
             if(constructor.dclInResolver != currentFile) instructions.add(new Extern(carg));
             instructions.add(new Call(carg));
             instructions.add(new Pop(Register.ACCUMULATOR));
             instructions.add(new Add(Register.STACK, new Immediate(SizeHelper.DEFAULT_STACK_SIZE)));
 
         } catch (UndeclaredException e) {
             //Should never get here
             e.printStackTrace();
         }
         instructions.add(new Comment("End of New String!"));
     }
 
     @Override
     public void visit(CharacterLiteralSymbol characterSymbol) {
         instructions.add(new Mov(Register.ACCUMULATOR, new Immediate(characterSymbol.getValue())));
     }
 
     @Override
     public void visit(ArrayAccessExprSymbol arrayAccess) {
         boolean gettingValue = getVal;
         //always want the value of the array when accessing it's member.
         getVal = true;
         instructions.add(new Comment("Accessing array"));
         instructions.add(new Push(Register.BASE));
         arrayAccess.children.get(0).accept(this);
         final Size s = lastSize;
         instructions.add(new Mov(Register.BASE, Register.ACCUMULATOR));
         arrayAccess.children.get(1).accept(this);
         instructions.add(new Shl(Register.ACCUMULATOR, Immediate.getImediateShift(SizeHelper.getPushSize(lastSize))));
         final long offset = SizeHelper.DEFAULT_STACK_SIZE * 2 + SizeHelper.getIntSize(Size.DWORD);
         instructions.add(new Add(Register.ACCUMULATOR, new Immediate(offset)));
         if(gettingValue){
             getVal = true;
             genMov(s, new PointerRegister(Register.ACCUMULATOR, Register.BASE), "array", arrayAccess);
             lastSize = s;
 
         }else{
             instructions.add(new Add(Register.ACCUMULATOR, Register.BASE));
             lastSize = SizeHelper.getSize(SizeHelper.DEFAULT_STACK_SIZE);
         }
         instructions.add(new Pop(Register.BASE));
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
 
         bin.children.get(0).accept(this);
         instructions.add(new Push(Register.ACCUMULATOR));
         bin.children.get(1).accept(this);
         instructions.add(new Pop(Register.BASE));
         instructions.add(maker.make(Register.BASE, Register.ACCUMULATOR));
         instructions.add(new Mov(Register.ACCUMULATOR, Register.BASE));
 
         instructions.add(new Pop(Register.BASE));
     }
 
     private void compHelper(BinOpExpr bin, UniOpMaker uni){
         binOpHelper(bin, CmpMaker.maker);
         instructions.add(new Comment("clear all bits in register"));
         instructions.add(new Mov(Register.ACCUMULATOR, Immediate.ZERO));
         instructions.add(uni.make(Register.ACCUMULATOR));
     }
 
     private void binUniOpHelper(BinOpExpr bin, BinUniOpMaker maker, boolean sar){
         instructions.add(new Push(Register.BASE));
 
         bin.children.get(0).accept(this);
         instructions.add(new Push(Register.ACCUMULATOR));
         bin.children.get(1).accept(this);
 
         instructions.add(new Mov(Register.BASE, Register.ACCUMULATOR));
         // pop first operand
         instructions.add(new Pop(Register.ACCUMULATOR));
 
         // first operand -> eax, second operand -> ebx
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
