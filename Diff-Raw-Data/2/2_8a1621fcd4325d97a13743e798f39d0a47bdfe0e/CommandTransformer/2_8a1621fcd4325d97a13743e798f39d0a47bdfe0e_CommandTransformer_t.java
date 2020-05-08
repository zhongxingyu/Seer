 package jk_5.nailed.coremod.transformers;
 
 import cpw.mods.fml.common.Loader;
 import cpw.mods.fml.common.ModContainer;
 import jk_5.nailed.NailedLog;
 import jk_5.nailed.coremod.asm.ASMHelper;
 import net.minecraft.launchwrapper.IClassTransformer;
 import org.objectweb.asm.ClassWriter;
 import org.objectweb.asm.Opcodes;
 import org.objectweb.asm.tree.ClassNode;
 import org.objectweb.asm.tree.MethodNode;
 
 /**
  * No description given
  *
  * @author jk-5
  */
 @SuppressWarnings("unused")
 public class CommandTransformer implements IClassTransformer {
 
     @Override
     public byte[] transform(String name, String transformedName, byte[] bytes){
         ClassNode cnode = ASMHelper.createClassNode(bytes);
 
         if(ClassHeirachyTransformer.classExtends(name, "net.minecraft.command.ICommand")){
             String mod = "minecraft";
             ModContainer registrar = Loader.instance().activeModContainer();
             if(registrar != null){
                 mod = registrar.getModId();
             }
 
             if((Opcodes.ACC_ABSTRACT & cnode.access) == Opcodes.ACC_ABSTRACT){
                 return bytes;
             }
 
             NailedLog.info("Adding permission info to " + name + " by " + mod);
 
             cnode.interfaces.add("jk_5/nailed/server/command/PermissionCommand");
 
             MethodNode methodGetNode = new MethodNode(Opcodes.ACC_PUBLIC, "getPermissionNode", "()Ljava/lang/String;", null, null);
             methodGetNode.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
             methodGetNode.visitInsn(Opcodes.DUP);
             methodGetNode.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V");
             methodGetNode.visitLdcInsn(mod.toLowerCase() + ".commands.");
             methodGetNode.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
             methodGetNode.visitVarInsn(Opcodes.ALOAD, 0);
            methodGetNode.visitMethodInsn(Opcodes.INVOKEINTERFACE, "net/minecraft/command/ICommand", "getCommandName", "()Ljava/lang/String;");
             methodGetNode.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
             methodGetNode.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;");
             methodGetNode.visitInsn(Opcodes.ARETURN);
             cnode.methods.add(methodGetNode);
 
             return ASMHelper.createBytes(cnode, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
         }else{
             return bytes;
         }
     }
 }
