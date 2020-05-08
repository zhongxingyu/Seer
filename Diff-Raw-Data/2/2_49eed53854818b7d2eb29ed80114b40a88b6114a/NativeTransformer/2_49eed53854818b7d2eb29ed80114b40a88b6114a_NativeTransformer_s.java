 package fi.lolcatz.profiler;
 
 import org.apache.log4j.Logger;
 import org.objectweb.asm.Opcodes;
 import org.objectweb.asm.tree.*;
 
 import java.lang.instrument.ClassFileTransformer;
 import java.lang.instrument.IllegalClassFormatException;
 import java.security.ProtectionDomain;
 import java.util.List;
 
 public class NativeTransformer implements ClassFileTransformer, Opcodes {
 
     private static Logger logger = Logger.getLogger(ProfilerTransformer.class.getName());
     public String prefix;
     public String className;
 
     public NativeTransformer(String prefix) {
         this.prefix = prefix;
     }
 
     @Override
     public byte[] transform(
             ClassLoader loader,
             String className,
             Class<?> classBeingRedefined,
             ProtectionDomain protectionDomain,
             byte[] classfileBuffer) throws IllegalClassFormatException {
         this.className = className;
         logger.info("Class " + className);
         ClassNode cn = Util.initClassNode(classfileBuffer);
 
         for (MethodNode mn : (List<MethodNode>) cn.methods) {
             logger.info("  Method " + mn.name);
             if ((mn.access & ACC_NATIVE) != 0) {
                 logger.warn("Method " + mn.name + " is native!");
                 mn.access -= ACC_NATIVE;
                 int index = ProfileData.addBasicBlock(100, className + "." + prefix + mn.name);
 
                 mn.instructions = createCounterIncrementInsnList(index);
                 mn.instructions.add(callWrappedNative(mn));
                 mn.instructions.add(new InsnNode(RETURN));
                 logger.trace(Util.getInsnListString(mn.instructions));
             }
         }
 
         ProfileData.initialize();
         return Util.generateBytecode(cn);
     }
 
     private InsnList callWrappedNative(MethodNode mn) {
         InsnList insns = new InsnList();
         int invokeOpcode;
         if ((mn.access & ACC_STATIC) != 0) invokeOpcode = INVOKESTATIC;
         else invokeOpcode = INVOKEVIRTUAL;
         insns.add(new MethodInsnNode(invokeOpcode, className, prefix + mn.name, mn.desc));
         return insns;
     }
 
     /**
      * Creates new InsnList containing bytecode instructions to increment counter.
      *
      * @return Counter increment InsnList
      */
     private InsnList createCounterIncrementInsnList(int basicBlockIndex) {
         InsnList counterIncrementInsnList = new InsnList();
         counterIncrementInsnList.add(intPushInsn(basicBlockIndex));
         counterIncrementInsnList.add(new MethodInsnNode(INVOKESTATIC, "fi/lolcatz/profiler/ProfileData",
                 "incrementCallsToBasicBlock", "(I)V"));
         return counterIncrementInsnList;
     }
 
     /**
      * Creates an instruction that can be used to push any Int value to stack.
      *
      * @param i Int to push to stack.
      * @return Instruction to push <code>i</code> to stack.
      */
     private AbstractInsnNode intPushInsn(int i) {
         if (i < 128) {
             return new IntInsnNode(BIPUSH, i);
         } else if (i < 32768) {
             return new IntInsnNode(SIPUSH, i);
         } else {
             return new LdcInsnNode(i);
         }
     }
 }
