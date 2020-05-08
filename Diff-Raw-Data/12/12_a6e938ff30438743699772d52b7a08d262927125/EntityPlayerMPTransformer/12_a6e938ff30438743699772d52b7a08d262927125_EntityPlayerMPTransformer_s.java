 package com.github.ttran17.opmods.asm;
 
 import java.util.logging.Level;
 
 import net.minecraft.launchwrapper.IClassTransformer;
 
 import org.objectweb.asm.ClassReader;
 import org.objectweb.asm.ClassVisitor;
 import org.objectweb.asm.ClassWriter;
 import org.objectweb.asm.Label;
 import org.objectweb.asm.MethodVisitor;
 import org.objectweb.asm.Opcodes;
 
 import cpw.mods.fml.relauncher.FMLRelaunchLog;
 import static org.objectweb.asm.Opcodes.*;
 
 public class EntityPlayerMPTransformer implements IClassTransformer {
 
 	public final String className = "ju"; // net.minecraft.entity.player.EntityPlayerMP
 	
 	@Override
 	public byte[] transform(String name, String transformedName, byte[] bytes) {
 		if (className.equals(name)) {
 			FMLRelaunchLog.log(Level.INFO,"Modifying " + name + " which is " + transformedName);
 			ClassReader cr = new ClassReader(bytes);
 			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
 			EntityPlayerMPVisitor visitor = new EntityPlayerMPVisitor(Opcodes.ASM4, cw);
 			cr.accept(visitor, 0);
 			bytes = cw.toByteArray();
 		}
 		return bytes;
 	}
 	
 	public class EntityPlayerMPVisitor extends ClassVisitor {
 
 		public final String name = "a"; // canCommandSenderUseCommand;
 		public final String desc = "(ILjava/lang/String;)Z"; // int par1, String par2Str
 		
 		public EntityPlayerMPVisitor(int api, ClassVisitor cv) {
 			super(api, cv);
 		}
 		
 		@Override
 	    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
 			MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
 			if (this.name.equals(name) && this.desc.equals(desc)) {
 				FMLRelaunchLog.log(Level.INFO,"Visiting canCommandSenderUseCommand ...");
 				newMethod(mv);
 				return null;
 			}  			
 			return mv;
 		}		
 		
 	    /*
 	    public boolean canCommandSenderUseCommand(int par1, String par2Str)
 	    {
 	    	if (com.github.ttran17.servermods.FinerOps.canCommandSenderUseCommand(par1, par2Str, this.username, playerNetServerHandler)) {
 	    		return "seed".equals(par2Str) && !this.mcServer.isDedicatedServer() ? true : (!"tell".equals(par2Str) && !"help".equals(par2Str) && !"me".equals(par2Str) ? (this.mcServer.getConfigurationManager().areCommandsAllowed(this.username) ? this.mcServer.func_110455_j() >= par1 : false) : true);
 	    	} else {
 	    		return false;
 	    	}
 	        //return "seed".equals(par2Str) && !this.mcServer.isDedicatedServer() ? true : (!"tell".equals(par2Str) && !"help".equals(par2Str) && !"me".equals(par2Str) ? (this.mcServer.getConfigurationManager().areCommandsAllowed(this.username) ? this.mcServer.func_110455_j() >= par1 : false) : true);
 	    }
 		*/
 		public void newMethod(MethodVisitor mv) {
 			mv.visitCode();
 			mv.visitVarInsn(ILOAD, 1);
 			mv.visitVarInsn(ALOAD, 2);
 			mv.visitVarInsn(ALOAD, 0);
 			mv.visitFieldInsn(GETFIELD, "ju", "bu", "Ljava/lang/String;");
 			mv.visitVarInsn(ALOAD, 0);
 			mv.visitFieldInsn(GETFIELD, "ju", "a", "Ljz;");
			mv.visitMethodInsn(INVOKESTATIC, "com/github/ttran17/servermods/FinerOps", "canCommandSenderUseCommand", "(ILjava/lang/String;Ljava/lang/String;Ljz;)Z");
 			Label l0 = new Label();
 			mv.visitJumpInsn(IFEQ, l0);
 			mv.visitLdcInsn("seed");
 			mv.visitVarInsn(ALOAD, 2);
 			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z");
 			Label l1 = new Label();
 			mv.visitJumpInsn(IFEQ, l1);
 			mv.visitVarInsn(ALOAD, 0);
 			mv.visitFieldInsn(GETFIELD, "ju", "b", "Lnet/minecraft/server/MinecraftServer;");
 			mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/server/MinecraftServer", "V", "()Z");
 			mv.visitJumpInsn(IFNE, l1);
 			mv.visitInsn(ICONST_1);
 			Label l2 = new Label();
 			mv.visitJumpInsn(GOTO, l2);
 			mv.visitLabel(l1);
 			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
 			mv.visitLdcInsn("tell");
 			mv.visitVarInsn(ALOAD, 2);
 			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z");
 			Label l3 = new Label();
 			mv.visitJumpInsn(IFNE, l3);
 			mv.visitLdcInsn("help");
 			mv.visitVarInsn(ALOAD, 2);
 			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z");
 			mv.visitJumpInsn(IFNE, l3);
 			mv.visitLdcInsn("me");
 			mv.visitVarInsn(ALOAD, 2);
 			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z");
 			mv.visitJumpInsn(IFNE, l3);
 			mv.visitVarInsn(ALOAD, 0);
 			mv.visitFieldInsn(GETFIELD, "ju", "b", "Lnet/minecraft/server/MinecraftServer;");
 			mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/server/MinecraftServer", "af", "()Lhm;");
 			mv.visitVarInsn(ALOAD, 0);
 			mv.visitFieldInsn(GETFIELD, "ju", "bu", "Ljava/lang/String;");
 			mv.visitMethodInsn(INVOKEVIRTUAL, "hm", "e", "(Ljava/lang/String;)Z");
 			Label l4 = new Label();
 			mv.visitJumpInsn(IFEQ, l4);
 			mv.visitVarInsn(ALOAD, 0);
 			mv.visitFieldInsn(GETFIELD, "ju", "b", "Lnet/minecraft/server/MinecraftServer;");
 			mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/server/MinecraftServer", "k", "()I");
 			mv.visitVarInsn(ILOAD, 1);
 			Label l5 = new Label();
 			mv.visitJumpInsn(IF_ICMPLT, l5);
 			mv.visitInsn(ICONST_1);
 			mv.visitJumpInsn(GOTO, l2);
 			mv.visitLabel(l5);
 			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
 			mv.visitInsn(ICONST_0);
 			mv.visitJumpInsn(GOTO, l2);
 			mv.visitLabel(l4);
 			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
 			mv.visitInsn(ICONST_0);
 			mv.visitJumpInsn(GOTO, l2);
 			mv.visitLabel(l3);
 			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
 			mv.visitInsn(ICONST_1);
 			mv.visitLabel(l2);
 			mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {Opcodes.INTEGER});
 			mv.visitInsn(IRETURN);
 			mv.visitLabel(l0);
 			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
 			mv.visitInsn(ICONST_0);
 			mv.visitInsn(IRETURN);
 			mv.visitMaxs(4, 3);
 			mv.visitEnd();
 		}
 	}
 }
