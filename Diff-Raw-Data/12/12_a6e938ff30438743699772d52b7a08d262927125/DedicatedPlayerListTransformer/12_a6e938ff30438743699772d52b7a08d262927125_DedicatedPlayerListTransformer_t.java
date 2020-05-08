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
 
 public class DedicatedPlayerListTransformer implements IClassTransformer {
 
 	public final String className = "iq"; // net.minecraft.server.dedicated.DedicatedPlayerList
 	
 	@Override
 	public byte[] transform(String name, String transformedName, byte[] bytes) {
 		if (className.equals(name)) {
 			FMLRelaunchLog.log(Level.INFO,"Modifying " + name + " which is " + transformedName);
 			ClassReader cr = new ClassReader(bytes);
 			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
 			DedicatedPlayerListVisitor visitor = new DedicatedPlayerListVisitor(Opcodes.ASM4, cw);
 			cr.accept(visitor, 0);
 			bytes = cw.toByteArray();
 		}
 		return bytes;
 	}
 	
 	public class DedicatedPlayerListVisitor extends ClassVisitor {
 
 		public final String name = "<init>"; // constructor;
 		public final String desc = "(Lir;)V";
 		
 		public DedicatedPlayerListVisitor(int api, ClassVisitor cv) {
 			super(api, cv);
 		}
 		
 		@Override
 	    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
 			MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
 			if (this.name.equals(name) && this.desc.equals(desc)) {
 				FMLRelaunchLog.log(Level.INFO,"Visiting constructor ...");
 				mv = new ConstructorVisitor(Opcodes.ASM4, mv);
 			}  			
 			return mv;
 		}		
 	}
 
 	public class ConstructorVisitor extends MethodVisitor {
 
 		public ConstructorVisitor(int api, MethodVisitor mv) {
 			super(api, mv);
 		}
 		
 		@Override
 		/*
         com.github.ttran17.opmods.FinerOps.dedicatedPlayerList = this;
         com.github.ttran17.opmods.FinerOps.load(dedicatedServer);
 		 */
 	    public void visitInsn(int opcode) {
 	        if (opcode == RETURN) {
 	        	mv.visitVarInsn(ALOAD, 0);
	        	mv.visitFieldInsn(PUTSTATIC, "com/github/ttran17/opmods/FinerOps", "dedicatedPlayerList", "Liq;");
 	        	mv.visitVarInsn(ALOAD, 1);
	        	mv.visitMethodInsn(INVOKESTATIC, "com/github/ttran17/opmods/FinerOps", "load", "(Lir;)V");
 	        } 	        
 	        mv.visitInsn(opcode);	       
 	    }
 	}
 	
 }
