 package net.medsouz.omg.asm;
 
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import org.objectweb.asm.ClassReader;
 import org.objectweb.asm.ClassWriter;
 import org.objectweb.asm.Opcodes;
 import org.objectweb.asm.tree.AbstractInsnNode;
 import org.objectweb.asm.tree.ClassNode;
 import org.objectweb.asm.tree.FieldInsnNode;
 import org.objectweb.asm.tree.InsnList;
 import org.objectweb.asm.tree.MethodInsnNode;
 import org.objectweb.asm.tree.MethodNode;
 import org.objectweb.asm.tree.VarInsnNode;
 
 import net.minecraft.launchwrapper.IClassTransformer;
 import net.minecraft.launchwrapper.LaunchClassLoader;
 
 public class ModelBipedTransformer implements IClassTransformer{
 	//WARNING: http://bit.ly/wX7L1J
 	//Based off of https://github.com/micdoodle8/Crossbow_Mod_2/blob/93ddd08916991ba5466ea37fc944e9ba618c87e5/common/micdoodle8/mods/crossbowmod/asm/CrossbowModTransformer.java
 	//Many thanks to him
 	private boolean isDeobf = true;
 	private HashMap<String, String> classRef = new HashMap<String, String>();
 	
 	@SuppressWarnings("resource")
 	public ModelBipedTransformer(){
 		final URLClassLoader loader = new LaunchClassLoader(((URLClassLoader) this.getClass().getClassLoader()).getURLs());
         final URL classResource = loader.findResource(String.valueOf("net.minecraft.client.model.ModelBiped").replace('.', '/').concat(".class"));
         if(classResource == null){
         	isDeobf = false;
         }else{
         	isDeobf = true;
         }
         if(isDeobf){
         	classRef.put("entityClass", "net/minecraft/entity/Entity");
         	classRef.put("modelBipedClass", "net/minecraft/client/model/ModelBiped");;
         	classRef.put("setRotationAnglesMethod", "setRotationAngles");
         	classRef.put("setRotationAnglesDesc", "(FFFFFFL" + classRef.get("entityClass") + ";)V");
         }else{
         	classRef.put("entityClass", "nm");
 			classRef.put("modelBipedClass", "bbg");
 			classRef.put("setRotationAnglesMethod", "a");
 			classRef.put("setRotationAnglesDesc", "(FFFFFFL" + classRef.get("entityClass") + ";)V");
         }
 	}
 	
 	@Override
 	public byte[] transform(String name, String arg1, byte[] bytes) {
 		if (name.replace('.', '/').equals(classRef.get("modelBipedClass"))){
			System.out.println("INJECTING INTO MODELBIPED");
 			final ClassNode node = new ClassNode();
 	        final ClassReader reader = new ClassReader(bytes);
 	        reader.accept(node, 0);
 
	        int operationCount = 1;
	        int injectionCount = 0;

 	        final Iterator<MethodNode> methods = node.methods.iterator();

 	        label4123:
 	        while (methods.hasNext())
 	        {
 	            final MethodNode methodnode = methods.next();

 	            if (methodnode.name.equals(classRef.get("setRotationAnglesMethod")) && methodnode.desc.equals(classRef.get("setRotationAnglesDesc")))
 	            {
 	                for (int count = 0; count < methodnode.instructions.size(); count++)
 	                {
 	                    final AbstractInsnNode insnNode = methodnode.instructions.get(count);
 
	                    if (insnNode instanceof FieldInsnNode && ((FieldInsnNode) insnNode).name.equals("aimedBow"))
 	                    {
 	                        InsnList toAdd = new InsnList();
 
 	                        toAdd.add(new VarInsnNode(Opcodes.ALOAD, 0));
 	                        toAdd.add(new VarInsnNode(Opcodes.ALOAD, 7));
 	                        toAdd.add(new VarInsnNode(Opcodes.FLOAD, 3));
 	                        toAdd.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "net/medsouz/omg/proxy/ClientProxy", "bipedRotationHook", "(L" + classRef.get("modelBipedClass") + ";L" + classRef.get("entityClass") + ";F)V"));
 
 	                        methodnode.instructions.insertBefore(methodnode.instructions.get(count - 1), toAdd);
	                        injectionCount++;
 	                        break label4123;
 	                    }
 	                }
 	            }
 	        }
 
 	        final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
 	        node.accept(writer);
 	        bytes = writer.toByteArray();
 		}
 		return bytes;
 	}
 
 }
