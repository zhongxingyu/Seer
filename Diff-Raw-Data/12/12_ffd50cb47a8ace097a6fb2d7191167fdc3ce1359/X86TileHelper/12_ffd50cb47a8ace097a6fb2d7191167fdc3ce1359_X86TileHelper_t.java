 package cs444.codegen.x86.tiles.helpers;
 
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 
 import cs444.codegen.Addable;
 import cs444.codegen.CodeGenVisitor;
 import cs444.codegen.Platform;
 import cs444.codegen.SizeHelper;
 import cs444.codegen.generic.tiles.helpers.TileHelper;
 import cs444.codegen.x86.Immediate;
 import cs444.codegen.x86.InstructionArg;
 import cs444.codegen.x86.InstructionArg.Size;
 import cs444.codegen.x86.Register;
 import cs444.codegen.x86.instructions.*;
 import cs444.codegen.x86.instructions.bases.X86Instruction;
 import cs444.parser.symbols.ISymbol;
 import cs444.parser.symbols.JoosNonTerminal;
 import cs444.parser.symbols.ast.*;
 import cs444.parser.symbols.ast.AModifiersOptSymbol.ProtectionLevel;
 import cs444.parser.symbols.ast.cleanup.SimpleMethodInvoke;
 import cs444.types.APkgClassResolver;
 import cs444.types.exceptions.UndeclaredException;
 
 public abstract class X86TileHelper extends TileHelper<X86Instruction, Size> {
 
     protected X86TileHelper(){ }
 
     public static void genMov(final Size size, final InstructionArg from, final String value,
             final Typeable dcl, final SizeHelper<X86Instruction, Size> sizeHelper, final Addable<X86Instruction> instructions){
 
 
         X86Instruction instruction;
 
         if(size == Size.DWORD){
             instruction = new Mov(Register.ACCUMULATOR, from, sizeHelper);
         }else if(JoosNonTerminal.unsigned.contains(dcl.getType().getTypeDclNode().fullName)){
             instruction = new Movzx(Register.ACCUMULATOR, from, size, sizeHelper);
         }else{
             instruction = new Movsx(Register.ACCUMULATOR, from, size, sizeHelper);
         }
 
         instructions.add(new Comment("getting value of " + value));
         instructions.add(instruction);
     }
 
     @Override
     public final void ifNullJmpCode(final String ifNullLbl,  final SizeHelper<X86Instruction, Size> sizeHelper,
             final Addable<X86Instruction> instructions) {
 
         ifNullJmpCode(Register.ACCUMULATOR, ifNullLbl, sizeHelper, instructions);
     }
 
     public static void ifNullJmpCode(final Register register, final String ifNullLbl,
             final SizeHelper<X86Instruction, Size> sizeHelper, final Addable<X86Instruction> instructions) {
         instructions.add(new Comment("null check"));
         instructions.add(new Cmp(register, Immediate.NULL, sizeHelper));
         instructions.add(new Je(new Immediate(ifNullLbl), sizeHelper));
     }
 
     @Override
     public void methProlog(final MethodOrConstructorSymbol method, final String methodName,
             final SizeHelper<X86Instruction, Size> sizeHelper, final Addable<X86Instruction> instructions){
 
         if(method.getProtectionLevel() != ProtectionLevel.PRIVATE) instructions.add(new Global(methodName));
         instructions.add(new Label(methodName));
         instructions.add(Push.getStackPush(sizeHelper));
         instructions.add(new Mov(Register.FRAME, Register.STACK, sizeHelper));
     }
 
     @Override
     public void methEpilogue(final MethodOrConstructorSymbol method, final Addable<X86Instruction> instructions) {
         //Don't fall though void funcs
         instructions.add(Leave.LEAVE);
         instructions.add(Ret.RET);
 
         instructions.add(new Comment("End of method " + method.dclName));
     }
 
     @Override
     public final void setupJumpNe(final String lblTo,
             final SizeHelper<X86Instruction, Size> sizeHelper, final Addable<X86Instruction> instructions){
 
         setupJumpNe(Register.ACCUMULATOR, Immediate.TRUE, lblTo, sizeHelper, instructions);
     }
 
     @Override
     public final void setupJumpNeFalse(final String lblTo,
             final SizeHelper<X86Instruction, Size> sizeHelper, final Addable<X86Instruction> instructions){
 
         setupJumpNe(Register.ACCUMULATOR, Immediate.FALSE, lblTo, sizeHelper, instructions);
     }
 
     public static void setupJumpNe(final Register reg, final Immediate when,
             final String lblTo, final SizeHelper<X86Instruction, Size> sizeHelper, final Addable<X86Instruction> instructions){
         instructions.add(new Cmp(reg, when, sizeHelper));
         instructions.add(new Jne(new Immediate(lblTo), sizeHelper));
     }
 
     @Override
     public void invokeConstructor(final APkgClassResolver resolver, final List<ISymbol> children,
             final Platform<X86Instruction, Size> platform, final Addable<X86Instruction> instructions) {
 
         final List<String> types = new LinkedList<String>();
         final SizeHelper<X86Instruction, Size> sizeHelper = platform.getSizeHelper();
 
         instructions.add(new Comment("Back up addr of obj in Base so it is safe"));
         instructions.add(new Push(Register.BASE, sizeHelper));
         instructions.add(new Mov(Register.BASE, Register.ACCUMULATOR, sizeHelper));
 
         for(final ISymbol child : children){
             instructions.addAll(platform.getBest(child));
             final Typeable arg = (Typeable) child;
             final TypeSymbol ts = arg.getType();
             final Size lastSize =  sizeHelper.getPushSize(sizeHelper.getSizeOfType(ts.getTypeDclNode().fullName));
            if(ts.value.equals(JoosNonTerminal.LONG)) pushLong(arg, instructions, sizeHelper);
             else instructions.add(new Push(Register.ACCUMULATOR, sizeHelper.getPushSize(lastSize), sizeHelper));
             types.add(ts.getTypeDclNode().fullName);
         }
 
         instructions.add(new Push(Register.BASE, sizeHelper));
 
         ConstructorSymbol cs = null;
         try {
             cs = resolver.getConstructor(types, resolver);
         } catch (final UndeclaredException e) {
             //Should never get here
             e.printStackTrace();
         }
 
         final Immediate arg = new Immediate(APkgClassResolver.generateFullId(cs));
         if(resolver != CodeGenVisitor.<X86Instruction, Size>getCurrentCodeGen(platform).currentFile) instructions.add(new Extern(arg));
 
         instructions.add(new Call(arg, sizeHelper));
         //return value is the new object
         instructions.add(new Pop(Register.ACCUMULATOR, sizeHelper));
 
         final long mySize = cs.getStackSize() - sizeHelper.getDefaultStackSize();
         if(mySize != 0){
             final Immediate by = new Immediate(String.valueOf(mySize));
             instructions.add(new Add(Register.STACK, by, sizeHelper));
         }
         instructions.add(new Comment("Restore Base register"));
         instructions.add(new Pop(Register.BASE, sizeHelper));
     }
 
     @Override
     public void strPartHelper(final ISymbol child, final APkgClassResolver resolver,
             final Addable<X86Instruction> instructions, final Platform<X86Instruction, Size> platform){
 
         final SizeHelper<X86Instruction, Size> sizeHelper = platform.getSizeHelper();
         final String firstType = ((Typeable)child).getType().getTypeDclNode().fullName;
         instructions.addAll(platform.getBest(child));
         AMethodSymbol ms = resolver.safeFindMethod(JoosNonTerminal.TO_STR, true, Arrays.asList(firstType), false);
         if(ms == null) ms = resolver.safeFindMethod(JoosNonTerminal.TO_STR, true, Arrays.asList(JoosNonTerminal.OBJECT), false);
 
 
 
         final Size lastSize = sizeHelper.getPushSize(sizeHelper.getSize(sizeHelper.getByteSizeOfType(firstType)));
         final int pop = sizeHelper.getIntSize(lastSize);
 
         instructions.add(new Push(Register.ACCUMULATOR, lastSize, sizeHelper));
 
         final Immediate arg = new Immediate(APkgClassResolver.generateFullId(ms));
 
         if(ms.dclInResolver != CodeGenVisitor.<X86Instruction, Size>getCurrentCodeGen(platform).currentFile) instructions.add(new Extern(arg));
         instructions.add(new Call(arg, sizeHelper));
 
         instructions.add(new Add(Register.STACK, new Immediate(pop), sizeHelper));
     }
 
     @Override
     public void callStartHelper(final SimpleMethodInvoke invoke,
             final Addable<X86Instruction> instructions, final Platform<X86Instruction, Size> platform){
 
         final MethodOrConstructorSymbol call = invoke.call;
         final SizeHelper<X86Instruction, Size> sizeHelper = platform.getSizeHelper();
 
         if(call.isNative()){
             instructions.add(new Comment("Backing up registers that are to be saved"));
             instructions.add(new Push(Register.BASE, sizeHelper));
             instructions.add(new Push(Register.FRAME, sizeHelper));
             instructions.add(new Push(Register.STACK, sizeHelper));
         }
 
         instructions.add(new Comment("Pushing args"));
 
         for(final ISymbol isymbol : invoke.children){
             final Typeable arg = (Typeable) isymbol;
             final TypeSymbol ts = arg.getType();
             instructions.addAll(platform.getBest(arg));
             final Size lastSize =  sizeHelper.getPushSize(sizeHelper.getSizeOfType(ts.getTypeDclNode().fullName));
            if(ts.value.equals(JoosNonTerminal.LONG)) pushLong(arg, instructions, sizeHelper);
             else instructions.add(new Push(Register.ACCUMULATOR, sizeHelper.getPushSize(lastSize), sizeHelper));
         }
     }
 
     @Override
     public void callEndHelper(final MethodOrConstructorSymbol call,
             final Addable<X86Instruction> instructions, final Platform<X86Instruction, Size> platform){
 
         final SizeHelper<X86Instruction, Size> sizeHelper = platform.getSizeHelper();
         // NOTE: do not use INVOKE in here, invoke gets size from method,
         // but visitor may visit InvokeSymbol before MethodSymbol
         final long mySize =  call.getStackSize();
         if(mySize != 0){
             final Immediate by = new Immediate(mySize);
             instructions.add(new Add(Register.STACK, by, sizeHelper));
         }
 
         if(call.isNative()){
             instructions.add(new Comment("Restoring up registers that are to be saved"));
             instructions.add(new Pop(Register.STACK, sizeHelper));
             instructions.add(new Pop(Register.FRAME, sizeHelper));
             instructions.add(new Pop(Register.BASE, sizeHelper));
         }
 
         instructions.add(new Comment("end invoke"));
     }
 
     @Override
     public void setupJump(final String lblTo, final SizeHelper<X86Instruction, Size> sizeHelper, final Addable<X86Instruction> instructions) {
         instructions.add(new Jmp(new Immediate(lblTo), sizeHelper));
     }
 
     @Override
     public void setupComment(final String comment, final Addable<X86Instruction> instructions) {
         instructions.add(new Comment(comment));
     }
 
     @Override
     public void setupLbl(final String lbl, final Addable<X86Instruction> instructions) {
         instructions.add(new Label(lbl));
     }
 
     @Override
     public void setupExtern(final String extern, final Addable<X86Instruction> instructions) {
         instructions.add(new Extern(extern));
     }
 
     @Override
     public final void loadBool(final boolean bool, final Addable<X86Instruction> instructions,
             final SizeHelper<X86Instruction, Size> sizeHelper){
 
         instructions.add(new Mov(Register.ACCUMULATOR, bool ? Immediate.TRUE : Immediate.FALSE, sizeHelper));
     }
 }
