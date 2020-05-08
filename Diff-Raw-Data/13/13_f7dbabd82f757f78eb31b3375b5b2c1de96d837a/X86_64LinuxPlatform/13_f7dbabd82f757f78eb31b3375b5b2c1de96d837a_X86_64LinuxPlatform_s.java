 package cs444.codegen.x86.x86_64.linux;
 
 import java.io.File;
 import java.util.Set;
 
 import cs444.Compiler;
 import cs444.codegen.Addable;
 import cs444.codegen.generic.tiles.helpers.TileHelper;
 import cs444.codegen.x86.Immediate;
 import cs444.codegen.x86.InstructionArg.Size;
 import cs444.codegen.x86.Register;
 import cs444.codegen.x86.StaticFieldInit;
 import cs444.codegen.x86.instructions.*;
 import cs444.codegen.x86.instructions.bases.X86Instruction;
import cs444.codegen.x86.x86_32.tiles.helpers.X86_32TileHelper;
 import cs444.codegen.x86.x86_64.X86_64Platform;
 import cs444.codegen.x86.x86_64.instructions.Syscall;
 
 public class X86_64LinuxPlatform extends X86_64Platform{
     public static class Factory implements X86PlatformFactory<X86_64LinuxPlatform>{
         public static Factory factory = new Factory();
 
         private Factory(){ }
 
         @Override
         public X86_64LinuxPlatform getPlatform(final Set<String> opts){
             return new X86_64LinuxPlatform(opts);
         }
     }
 
     private static final Immediate EXIT = Immediate.SIXTY;
 
     public X86_64LinuxPlatform(final Set<String> opts){
         super(Runtime.instance, opts);
     }
 
     @Override
     public final void genStartInstructions(final String methodName, final Addable<X86Instruction> instructions) {
         instructions.add(new Global("_start"));
         instructions.add(new Label("_start"));
         instructions.add(new Extern(new Immediate(StaticFieldInit.STATIC_FIELD_INIT_LBL)));
         instructions.add(new Call(new Immediate(StaticFieldInit.STATIC_FIELD_INIT_LBL), sizeHelper));
         instructions.add(new Call(new Immediate(methodName), sizeHelper));
         instructions.add(new Mov(Register.DESTINATION, Register.ACCUMULATOR, sizeHelper));
         instructions.add(new Mov(Register.ACCUMULATOR, EXIT, sizeHelper));
         instructions.add(new Syscall());
     }
 
     @Override
     public TileHelper<X86Instruction, Size> getTileHelper() {
        return X86_32TileHelper.instance;
     }
 
     @Override
     public String getOutputDir() {
         return Compiler.OUTPUT_DIRECTORY + "x64l/";
     }
 
     //Test methods
 
     private static final String [] execute = new String[] {"./main"};
 
     @Override
     public String[] getLinkCmd(final String fileName) {
         return new String[] {"bash", "-c", "ld -melf_x86_64 -o main " + fileName + File.separator + "*.o"};
     }
 
     @Override
     public String [] getExecuteCmd() {
         return execute;
     }
 
     @Override
     public String[] getAssembleCmd(final String fileName) {
         //return new String[] {"nasm", "-O1", "-f", "elf64", fileName};
         //This will add debugging to the compiled objects.  Use it for debugging failed tests
         return new String[] {"nasm", "-O1", "-f", "elf64", "-g", "-F", "dwarf", fileName};
     }
 }
