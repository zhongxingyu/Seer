 package cs444.codegen;
 
 import java.io.PrintStream;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 import cs444.codegen.instructions.x86.*;
 import cs444.codegen.instructions.x86.Section.SectionType;
 import cs444.codegen.instructions.x86.bases.ReserveInstruction;
 import cs444.codegen.instructions.x86.bases.X86Instruction;
 import cs444.codegen.instructions.x86.factories.ReserveInstructionMaker;
 import cs444.codegen.peephole.InstructionHolder;
 import cs444.codegen.tiles.TileSet;
 import cs444.codegen.x86.*;
 import cs444.codegen.x86.InstructionArg.Size;
 import cs444.codegen.x86.tiles.helpers.TileHelper;
 import cs444.codegen.x86_32.X86_32Platform;
 import cs444.codegen.x86_32.linux.Runtime;
 import cs444.parser.symbols.ANonTerminal;
 import cs444.parser.symbols.ISymbol;
 import cs444.parser.symbols.JoosNonTerminal;
 import cs444.parser.symbols.ast.*;
 import cs444.parser.symbols.ast.cleanup.SimpleMethodInvoke;
 import cs444.parser.symbols.ast.cleanup.SimpleNameSymbol;
 import cs444.parser.symbols.ast.expressions.*;
 import cs444.types.APkgClassResolver;
 import cs444.types.PkgClassResolver;
 import cs444.types.exceptions.UndeclaredException;
 
 public class CodeGenVisitor{
     private static CodeGenVisitor currentVisitor;
 
     public static CodeGenVisitor getCurrentCodeGen(){
         return currentVisitor;
     }
 
     public static final String INIT_OBJECT_FUNC = "__init_object";
     public static final String NATIVE_NAME = "NATIVE";
     //TODO generic
     public final X86_32Platform platform;
     private boolean hasEntry = false;
     public boolean getVal = true;
     public boolean lastWasFunc = false;
     //TODO generic
     public Size lastSize = Size.DWORD;
     private long nextLblnum = 0;
     public APkgClassResolver currentFile;
     public boolean isSuper = false;
     public Size lhsSize;
 
     //TODO generic
     private final InstructionHolder<X86Instruction> instructions;
     //TODO generic
     private final X86SizeHelper sizeHelper;
 
     //TODO generic
     private final TileSet<X86Instruction> tiles;
 
     public long getNewLblNum(){
         return nextLblnum++;
     }
 
     public CodeGenVisitor(final Platform<?> platform) {
         this(null, platform);
     }
 
     public CodeGenVisitor(final APkgClassResolver resolver, final Platform<?> platform) {
         this.platform = (X86_32Platform)platform;
         this.currentFile = resolver;
         this.instructions = this.platform.getInstructionHolder();
         this.sizeHelper = this.platform.getSizeHelper();
         this.tiles = this.platform.getTiles();
         currentVisitor = this;
     }
 
     public void genHeader(final APkgClassResolver resolver) {
         this.currentFile = resolver;
 
         Runtime.instance.externAll(instructions);
         instructions.add(new Section(SectionType.TEXT));
 
         instructions.add(new Comment(INIT_OBJECT_FUNC + ": call super default constructor and initialize obj fields." +
                 " eax should contain address of object."));
         instructions.add(new Label(INIT_OBJECT_FUNC));
 
         APkgClassResolver superResolver = null;
 
         superResolver = resolver.getSuper();
 
         if (!resolver.fullName.equals(JoosNonTerminal.OBJECT)){
             //TODO generic
             TileHelper.invokeConstructor(superResolver, Collections.<ISymbol>emptyList(), platform, instructions);
         }
 
         instructions.add(new Comment("Store pointer to object in edx"));
         instructions.add(new Mov(Register.DATA, Register.ACCUMULATOR, sizeHelper));
 
         for (final DclSymbol fieldDcl : resolver.getUninheritedNonStaticFields()) {
             final Size size = X86SizeHelper.getSize(fieldDcl.getType().getTypeDclNode().getRealSize(sizeHelper));
 
             final Memory fieldAddr = new Memory(Register.DATA, fieldDcl.getOffset());
             if(!fieldDcl.children.isEmpty()){
                 instructions.add(new Comment("Initializing field " + fieldDcl.dclName + "."));
                 // save pointer to object
                 instructions.add(new Push(Register.DATA, sizeHelper));
                 final CodeGenVisitor visitor = new CodeGenVisitor(currentFile, platform);
                 currentVisitor = this;
                final ISymbol field = fieldDcl.children.get(0);
                field.accept(visitor);
                instructions.addAll(platform.getBest(field));
                 instructions.add(new Comment("Pop the object address to edx"));
                 instructions.add(new Pop(Register.DATA, sizeHelper));
                 instructions.add(new Mov(fieldAddr, Register.ACCUMULATOR, size, sizeHelper));
             }
         }
 
         instructions.add(Ret.RET);
     }
 
     public void genLayoutForStaticFields(final Iterable<DclSymbol> staticFields) {
         if (staticFields.iterator().hasNext()){
             instructions.add(new Comment("Static fields:"));
             instructions.add(new Section(SectionType.BSS));
         }
 
         for (final DclSymbol fieldDcl : staticFields) {
             final Size size = X86SizeHelper.getSize(fieldDcl.getType().getTypeDclNode().getRealSize(sizeHelper));
 
             final String fieldLbl = APkgClassResolver.getUniqueNameFor(fieldDcl);
             instructions.add(new Global(fieldLbl));
             final ReserveInstruction data = ReserveInstructionMaker.make(fieldLbl, size, 1);
             instructions.add(data);
         }
     }
 
     public void visit(final FieldAccessSymbol field) {
         //generate tiles for children
         final boolean wasGetVal = getVal;
         final boolean wasSuper = isSuper;
         getVal = true;
         field.children.get(0).accept(this);
         //super.x().y() x should be from super but not y
         getVal = wasGetVal;
         if(wasSuper) isSuper = false;
         field.children.get(1).accept(this);
         isSuper = false;
 
         //get the best tile for field
         tiles.<FieldAccessSymbol>addBest(tiles.fieldAccess, field, platform);
     }
 
     public void visit(final MethodSymbol method){
         final String methodName = APkgClassResolver.generateFullId(method);
         //TODO replace try with generic stuff
         try{
             if(APkgClassResolver.generateUniqueName(method, method.dclName).equals(JoosNonTerminal.ENTRY)
                     && method.isStatic() && !hasEntry){
 
                 hasEntry = true;
                 instructions.add(new Global("_start"));
                 instructions.add(new Label("_start"));
                 instructions.add(new Extern(new Immediate(StaticFieldInit.STATIC_FIELD_INIT_LBL)));
                 instructions.add(new Call(new Immediate(StaticFieldInit.STATIC_FIELD_INIT_LBL), sizeHelper));
                 instructions.add(new Call(new Immediate(methodName), sizeHelper));
                 instructions.add(new Mov(Register.BASE, Register.ACCUMULATOR, sizeHelper));
                 instructions.add(new Mov(Register.ACCUMULATOR, Immediate.EXIT, sizeHelper));
                 instructions.add(new Int(Immediate.SOFTWARE_INTERUPT, sizeHelper));
             }
         }catch(final Exception e){
             //Should never get here.
             e.printStackTrace();
         }
 
         for(final ISymbol child : method.children) child.accept(this);
 
         tiles.<MethodSymbol>addBest(tiles.methods, method, platform);
         platform.getBest(method).addToHolder(instructions);
     }
 
     public void visit(final ConstructorSymbol constructor) {
         lastWasFunc = true;
         for(final ISymbol child : constructor.children) child.accept(this);
 
         tiles.<ConstructorSymbol>addBest(tiles.constructors, constructor, platform);
         platform.getBest(constructor).addToHolder(instructions);
     }
 
 
 
     public void visit(final CreationExpression creationExpression) {
 
         final List<String> types = new LinkedList<String>();
 
         for(final ISymbol child : creationExpression.children){
             child.accept(this);
             final Typeable typeable = (Typeable) child;
             final TypeSymbol ts = typeable.getType();
             types.add(ts.getTypeDclNode().fullName);
         }
 
         if (!creationExpression.getType().isArray){
             //TODO generic this
             final APkgClassResolver resolver = creationExpression.getType().getTypeDclNode();
             ConstructorSymbol cs = null;
             try {
                 cs = resolver.getConstructor(types, resolver);
             } catch (final UndeclaredException e) {
                 //Should never get here
                 e.printStackTrace();
             }
             final Immediate arg = new Immediate(APkgClassResolver.generateFullId(cs));
 
             if(cs.dclInResolver != currentFile) instructions.add(new Extern(arg));
         }
 
         //TODO generic this
         lastSize = Size.DWORD;
 
         tiles.<CreationExpression>addBest(tiles.creation, creationExpression, platform);
     }
 
     public void visit(final ANonTerminal aNonTerminal) {
         final boolean lastFunc = lastWasFunc;
         lastWasFunc = false;
         for(final ISymbol child : aNonTerminal.children) child.accept(this);
         lastWasFunc = lastFunc;
         tiles.<ANonTerminal>addBest(tiles.anonTerms, aNonTerminal, platform);
     }
 
     public void visit(final WhileExprSymbol whileExprSymbol) {
         whileExprSymbol.getConditionSymbol().accept(this);
         whileExprSymbol.getBody().accept(this);
         tiles.<WhileExprSymbol>addBest(tiles.whiles, whileExprSymbol, platform);
     }
 
     public void visit(final ForExprSymbol forExprSymbol) {
         forExprSymbol.getForInit().accept(this);
         forExprSymbol.getConditionExpr().accept(this);
         forExprSymbol.getBody().accept(this);
         forExprSymbol.getForUpdate().accept(this);
         tiles.<ForExprSymbol>addBest(tiles.fors, forExprSymbol, platform);
     }
 
     public void visit(final IfExprSymbol ifExprSymbol) {
         ifExprSymbol.getConditionSymbol().accept(this);
         ifExprSymbol.getifBody().accept(this);
         final ISymbol elseSymbol = ifExprSymbol.getElseBody();
         if(elseSymbol != null) elseSymbol.accept(this);
         tiles.<IfExprSymbol>addBest(tiles.ifs, ifExprSymbol, platform);
     }
 
     public void visit(final ReturnExprSymbol retSymbol) {
         if(retSymbol.children.size() == 1) retSymbol.children.get(0).accept(this);
         tiles.<ReturnExprSymbol>addBest(tiles.rets, retSymbol, platform);
     }
 
     public void visit(final CastExpressionSymbol symbol) {
         symbol.getOperandExpression().accept(this);
         tiles.<CastExpressionSymbol>addBest(tiles.casts, symbol, platform);
         //TODO generic
         lastSize = sizeHelper.defaultStack;
     }
 
     public void visit(final NegOpExprSymbol op) {
         op.children.get(0).accept(this);
         tiles.<NegOpExprSymbol>addBest(tiles.negs, op, platform);
     }
 
     public void visit(final NotOpExprSymbol op) {
         op.children.get(0).accept(this);
         tiles.<NotOpExprSymbol>addBest(tiles.nots, op, platform);
     }
 
     public void visit(final MultiplyExprSymbol op) {
         op.children.get(0).accept(this);
         op.children.get(1).accept(this);
 
         tiles.<MultiplyExprSymbol>addBest(tiles.mults, op, platform);
     }
 
     public void visit(final AssignmentExprSymbol op) {
         final ISymbol leftHandSide = op.children.get(0);
         final ISymbol rightHandSide = op.children.get(1);
 
         final boolean tmp = getVal;
         getVal = false;
         leftHandSide.accept(this);
         lhsSize = lastSize;
         getVal = tmp;
         rightHandSide.accept(this);
         tiles.<AssignmentExprSymbol>addBest(tiles.assigns, op, platform);
     }
 
     public void visit(final DivideExprSymbol op) {
         op.children.get(0).accept(this);
         op.children.get(1).accept(this);
 
         tiles.<DivideExprSymbol>addBest(tiles.divs, op, platform);
     }
 
     public void visit(final RemainderExprSymbol op) {
         op.children.get(0).accept(this);
         op.children.get(1).accept(this);
 
         tiles.<RemainderExprSymbol>addBest(tiles.rems, op, platform);
     }
 
     public void visit(final AddExprSymbol op) {
         binOpHelper(op);
         tiles.<AddExprSymbol>addBest(tiles.adds, op, platform);
     }
 
     public void visit(final SubtractExprSymbol op) {
         binOpHelper(op);
         tiles.<SubtractExprSymbol>addBest(tiles.subs, op, platform);
     }
 
     public void visit(final LtExprSymbol op) {
         binOpHelper(op);
         tiles.<LtExprSymbol>addBest(tiles.lts, op, platform);
     }
 
     public void visit(final EqExprSymbol op) {
         binOpHelper(op);
         tiles.<EqExprSymbol>addBest(tiles.eqs, op, platform);
     }
 
     public void visit(final NeExprSymbol op) {
         binOpHelper(op);
         tiles.<NeExprSymbol>addBest(tiles.nes, op, platform);
     }
 
     public void visit(final AndExprSymbol op) {
         op.children.get(0).accept(this);
         op.children.get(1).accept(this);
         tiles.<AndExprSymbol>addBest(tiles.ands, op, platform);
     }
 
     public void visit(final OrExprSymbol op) {
         op.children.get(0).accept(this);
         op.children.get(1).accept(this);
         tiles.<OrExprSymbol>addBest(tiles.ors, op, platform);
     }
 
     public void visit(final EAndExprSymbol op) {
         binOpHelper(op);
         tiles.<EAndExprSymbol>addBest(tiles.eands, op, platform);
     }
 
     public void visit(final EOrExprSymbol op) {
         binOpHelper(op);
         tiles.<EOrExprSymbol>addBest(tiles.eors, op, platform);
     }
 
     public void visit(final LeExprSymbol op) {
         binOpHelper(op);
         tiles.<LeExprSymbol>addBest(tiles.les, op, platform);
     }
 
     public void visit(final InstanceOfExprSymbol op) {
         op.getLeftOperand().accept(this);
         tiles.<InstanceOfExprSymbol>addBest(tiles.insts, op, platform);
     }
 
     public void visit(final IntegerLiteralSymbol intLiteral) {
         tiles.<INumericLiteral>addBest(tiles.numbs, intLiteral, platform);
         lastSize = Size.DWORD;
     }
 
     public void visit(final NullSymbol nullSymbol) {
         tiles.<NullSymbol>addBest(tiles.nulls, nullSymbol, platform);
         lastSize = Size.DWORD;
     }
 
     public void visit(final BooleanLiteralSymbol boolSymbol) {
         tiles.<BooleanLiteralSymbol>addBest(tiles.bools, boolSymbol, platform);
         lastSize = Size.WORD;
     }
 
     public void visit(final ThisSymbol thisSymbol) {
         tiles.<Thisable>addBest(tiles.thisables, thisSymbol, platform);
     }
 
     public void visit(final SuperSymbol superSymbol) {
         tiles.<Thisable>addBest(tiles.thisables, superSymbol, platform);
         isSuper = true;
     }
 
     public void visit(final StringLiteralSymbol stringSymbol) {
         tiles.<StringLiteralSymbol>addBest(tiles.strs, stringSymbol, platform);
         lastSize = Size.DWORD;
     }
 
     public void visit(final CharacterLiteralSymbol characterSymbol) {
         tiles.<INumericLiteral>addBest(tiles.numbs, characterSymbol, platform);
         lastSize = Size.WORD;
     }
 
     public void visit(final ArrayAccessExprSymbol arrayAccess) {
         final boolean gettingValue = getVal;
         //always want the value of the array when accessing it's member.
         getVal = true;
         arrayAccess.children.get(0).accept(this);
         arrayAccess.children.get(1).accept(this);
 
         if(gettingValue){
             final long stackSize = arrayAccess.getType().getTypeDclNode().getRefStackSize(sizeHelper);
             Size elementSize;
             if(stackSize >= sizeHelper.defaultStackSize) elementSize = sizeHelper.defaultStack;
             else elementSize = X86SizeHelper.getSize(stackSize);
             lastSize = elementSize;
             tiles.<ArrayAccessExprSymbol>addBest(tiles.arrayValues, arrayAccess, platform);
         }else{
             tiles.<ArrayAccessExprSymbol>addBest(tiles.arrayRefs, arrayAccess, platform);
             lastSize = X86SizeHelper.getSize(sizeHelper.defaultStackSize);
         }
     }
 
     public void visit(final DclSymbol dclSymbol) {
         if(dclSymbol.children.isEmpty()) new IntegerLiteralSymbol(0).accept(this);
         else dclSymbol.children.get(0).accept(this);
         lastSize = X86SizeHelper.getSize(dclSymbol.getType().getTypeDclNode().getRefStackSize(sizeHelper));
         instructions.add(new Push(Register.ACCUMULATOR, lastSize, sizeHelper));
         tiles.<DclSymbol>addBest(tiles.dcls, dclSymbol, platform);
     }
 
     public void visit(final ByteLiteralSymbol byteLiteral) {
         tiles.<INumericLiteral>addBest(tiles.numbs, byteLiteral, platform);
         lastSize = Size.WORD;
     }
 
     public void visit(final ShortLiteralSymbol shortLiteral) {
         tiles.<INumericLiteral>addBest(tiles.numbs, shortLiteral, platform);
         lastSize = Size.WORD;
     }
 
     public void printToFileAndEmpty(final PrintStream printer){
         instructions.flush(printer);
     }
 
     private void binOpHelper(final BinOpExpr bin){
         bin.children.get(0).accept(this);
         bin.children.get(1).accept(this);
         lastSize = X86SizeHelper.getPushSize(sizeHelper.getSizeOfType(bin.getType().getTypeDclNode().fullName));
     }
 
     public void visit(final SimpleMethodInvoke invoke) {
         for(final ISymbol arg : invoke.children) arg.accept(this);
         tiles.<SimpleMethodInvoke>addBest(tiles.invokes, invoke, platform);
     }
 
     public void visit(final SimpleNameSymbol name) {
         final DclSymbol dcl = name.dcl;
         final String staticFieldLbl = dcl.isStatic() ? PkgClassResolver.getUniqueNameFor(dcl) : null;
 
         //TODO generic next two lines
         if(dcl.isStatic() && dcl.dclInResolver != currentFile) instructions.add(new Extern(staticFieldLbl));
         lastSize = X86SizeHelper.getSize(dcl.getType().getTypeDclNode().getRefStackSize(sizeHelper));
 
         if(getVal){
             tiles.<SimpleNameSymbol>addBest(tiles.nameValues, name, platform);
         }else{
             tiles.<SimpleNameSymbol>addBest(tiles.nameRefs, name, platform);
         }
     }
 
     public void visit(final EmptyStatementSymbol empty){
         tiles.addEmpty(empty, platform);
     }
 
     public void visit(final ISymbol other){
         throw new IllegalArgumentException(other.toString());
     }
 }
