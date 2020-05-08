 package verityco;
 
 import org.objectweb.asm.MethodVisitor;
 import org.objectweb.asm.Opcodes;
 
 public class LoadStoreVisitor extends MethodVisitor {
 
 	public static final void load(Object o) {
 		System.out.println("Loading object " + o);
 	}
 
 	public static final void loadStatic() {
 		System.out.println("Static load of object ");
 	}
 
 	public static final void store(Object o) {
 		System.out.println("Storing object " + o);
 	}
 
 	public static final void storeStatic() {
 		System.out.println("Static write of object ");
 	}
 
 	public LoadStoreVisitor(MethodVisitor mv) {
 		super(Opcodes.ASM4, mv);
 	}
 
 	@Override
 	public void visitFieldInsn(int opcode, String owner, String name,
 			String desc) {
 		if (opcode == Opcodes.GETFIELD) {
 			mv.visitInsn(Opcodes.DUP);
 			mv.visitMethodInsn(Opcodes.INVOKESTATIC,
 					"verityco/LoadStoreVisitor", "load",
 					"(Ljava/lang/Object;)V");
 		} else if (opcode == Opcodes.GETSTATIC) {
 			mv.visitMethodInsn(Opcodes.INVOKESTATIC,
 					"verityco/LoadStoreVisitor", "loadStatic", "()V");
 		} else if (opcode == Opcodes.PUTSTATIC) {
 			mv.visitMethodInsn(Opcodes.INVOKESTATIC,
 					"verityco/LoadStoreVisitor", "storeStatic", "()V");
 		} else {
			mv.visitInsn(Opcodes.SWAP);
 			mv.visitInsn(Opcodes.DUP);
 			mv.visitMethodInsn(Opcodes.INVOKESTATIC,
 					"verityco/LoadStoreVisitor", "store",
 					"(Ljava/lang/Object;)V");
			mv.visitInsn(Opcodes.SWAP);
 		}
 		mv.visitFieldInsn(opcode, owner, name, desc);
 	}
 	
 }
