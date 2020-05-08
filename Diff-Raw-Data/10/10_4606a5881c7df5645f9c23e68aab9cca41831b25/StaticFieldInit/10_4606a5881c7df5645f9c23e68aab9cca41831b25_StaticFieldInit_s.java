 package cs444.codegen.x86;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.List;
 
 import cs444.codegen.CodeGenVisitor;
 import cs444.codegen.IPlatform;
 import cs444.codegen.SizeHelper;
 import cs444.codegen.instructions.x86.Comment;
 import cs444.codegen.instructions.x86.Extern;
 import cs444.codegen.instructions.x86.Global;
 import cs444.codegen.instructions.x86.Label;
 import cs444.codegen.instructions.x86.Mov;
 import cs444.codegen.instructions.x86.Ret;
 import cs444.codegen.instructions.x86.Section;
 import cs444.codegen.instructions.x86.Section.SectionType;
 import cs444.codegen.instructions.x86.bases.X86Instruction;
 import cs444.codegen.peephole.InstructionHolder;
 import cs444.codegen.x86.InstructionArg.Size;
 import cs444.codegen.x86_32.linux.Runtime;
 import cs444.parser.symbols.ast.DclSymbol;
 import cs444.types.APkgClassResolver;
 import cs444.types.PkgClassResolver;
 
 //TODO make this as generic as possible and pull to a base class for all platforms
 public class StaticFieldInit {
     public static final String STATIC_FIELD_INIT_LBL = "__init_static_fields";
     private static final String STATIC_FIELD_INIT_FILE = "_static_init.s";
     private final InstructionHolder<X86Instruction> instructions;
     private final X86Platform platform;
 
     public static void generateCode(final List<APkgClassResolver> resolvers, final X86Platform platform,
             final boolean outputFile, final String directory) throws IOException{
 
         final StaticFieldInit fInit = new StaticFieldInit(platform);
 
         fInit.genCode(resolvers);
         if(outputFile){
             final File file = new File(directory + STATIC_FIELD_INIT_FILE);
             file.createNewFile();
             final PrintStream printer = new PrintStream(file);
             fInit.print(printer);
             printer.close();
         }
     }
 
     private StaticFieldInit(final IPlatform<X86Instruction> platform){
         instructions = platform.getInstructionHolder();
         this.platform = (X86Platform) platform;
     }
 
     private void print(final PrintStream printer) {
         instructions.flush(printer);
     }
 
     private void genCode(final List<APkgClassResolver> resolvers) {
         instructions.add(new Section(SectionType.TEXT));
         instructions.add(new Global(STATIC_FIELD_INIT_LBL));
         Runtime.instance.externAll(instructions);
         instructions.add(new Label(STATIC_FIELD_INIT_LBL));
         final SizeHelper<?> sizeHelper = platform.getSizeHelper();
 
         for (final APkgClassResolver aPkgClassResolver : resolvers) {
             if (!(aPkgClassResolver instanceof PkgClassResolver)) continue;
             final PkgClassResolver resolver = (PkgClassResolver) aPkgClassResolver;
             if (!resolver.shouldGenCode()) continue;
             for (final DclSymbol fieldDcl : resolver.getUninheritedStaticFields()){
                 final String fieldNameLbl = PkgClassResolver.getUniqueNameFor(fieldDcl);
                 final Size size = X86SizeHelper.getSize(fieldDcl.getType().getTypeDclNode().getRealSize(sizeHelper));
                 final PointerRegister toAddr = new PointerRegister(new Immediate(fieldNameLbl));
 
                 instructions.add(new Extern(fieldNameLbl));
                 instructions.add(new Mov(toAddr, Immediate.NULL, size, platform.getSizeHelper()));
             }
         }
 
         for (final APkgClassResolver aPkgClassResolver : resolvers) {
             if (!(aPkgClassResolver instanceof PkgClassResolver)) continue;
             final PkgClassResolver resolver = (PkgClassResolver) aPkgClassResolver;
             if (!resolver.shouldGenCode()) continue;
 
             for (final DclSymbol fieldDcl : resolver.getUninheritedStaticFields()){
                 final String fieldNameLbl = PkgClassResolver.getUniqueNameFor(fieldDcl);
                 final Size size = X86SizeHelper.getSize(fieldDcl.getType().getTypeDclNode().getRealSize(sizeHelper));
                 final PointerRegister toAddr = new PointerRegister(new Immediate(fieldNameLbl));
 
                 if(!fieldDcl.children.isEmpty()){
                     instructions.add(new Comment("Initializing static field " + fieldNameLbl + "."));
                    fieldDcl.children.get(0).accept(new CodeGenVisitor(resolver, platform));
                     instructions.add(new Mov(toAddr, Register.ACCUMULATOR, size, platform.getSizeHelper()));
                 }
             }
         }
 
         instructions.add(Ret.RET);
     }
 }
