 package ayamitsu.urtsquid.asm;
 
 import java.util.List;
 
 import org.objectweb.asm.ClassReader;
 import org.objectweb.asm.ClassWriter;
 import org.objectweb.asm.Opcodes;
 import org.objectweb.asm.tree.AbstractInsnNode;
 import org.objectweb.asm.tree.ClassNode;
 import org.objectweb.asm.tree.LdcInsnNode;
 import org.objectweb.asm.tree.MethodNode;
 
 import cpw.mods.fml.relauncher.IClassTransformer;
 
 /**
  * transform 1.62 to 0.425
  */
 public class Transformer162Ldc implements IClassTransformer, Opcodes {
 
 	// for 1.4.7
 	private static final String ENTITYFISHFOOK_CLASS_NAME = "rd";// EntityFishFook
 	private static final String ENTITYRENDERER_CLASS_NAME = "ban";// EntityRenderer
 	private static final String ITEM_CLASS_NAME = "up";// Item
 	private static final String ITEMBOAT_CLASS_NAME = "sy";// ItemBoat
 	private static final String ITEMBUCKET_CLASS_NAME = "td";// ItemBucket
 	private static final String ITEMENDEREYE_CLASS_NAME = "uc";// ItemEnderEye
 	private static final String NETSERVERHANDLER_CLASS_NAME = "iv";// NetServerHandler
 
 	@Override
 	public byte[] transform(String name, byte[] bytes) {
 		if (name.equals(ENTITYFISHFOOK_CLASS_NAME)) {
 			return this.transformEntityFishHook(bytes);
 		} else if (name.equals(ENTITYRENDERER_CLASS_NAME)) {
 			return this.transformEntityRenderer(bytes);
 		} else if (name.equals(ITEM_CLASS_NAME)) {
 			return this.transformItem(bytes);
 		} else if (name.equals(ITEMBOAT_CLASS_NAME)) {
 			return this.transformItemBoat(bytes);
 		} else if (name.equals(ITEMBUCKET_CLASS_NAME)) {
 			return this.transformItemBucket(bytes);
 		} else if (name.equals(ITEMENDEREYE_CLASS_NAME)) {
 			return this.transformItemEnderEye(bytes);
 		} else if (name.equals(NETSERVERHANDLER_CLASS_NAME)) {
 			return this.transformNetServerHandler(bytes);
 		}
 
 		return bytes;
 	}
 
 	private byte[] transformEntityFishHook(byte[] bytes) {
 		ClassNode cNode = this.encode(bytes);
 
 		for (MethodNode mNode : (List<MethodNode>)cNode.methods) {
 			// <init> (LWorld;LEntityPlayer;)V
 			if ("<init>".equals(mNode.name) && "(Lyc;Lqx;)V".equals(mNode.desc)) {
 				AbstractInsnNode[] insnList = mNode.instructions.toArray();
 
 				for (int i = 0; i < insnList.length; i++) {
 					if (insnList[i] instanceof LdcInsnNode) {
 						LdcInsnNode liNode = (LdcInsnNode)insnList[i];
 
						if (liNode.cst instanceof Double) {
 							mNode.instructions.set(liNode, new LdcInsnNode(new Double(0.425D)));
 							ASMDebugUtils.info("Override EntityFishHook");
 							break;
 						}
 					}
 				}
 			}
 		}
 
 		return this.decode(cNode);
 	}
 
 	private byte[] transformEntityRenderer(byte[] bytes) {
 		ClassNode cNode = this.encode(bytes);
 
 		for (MethodNode mNode : (List<MethodNode>)cNode.methods) {
 			// orientCamera (F)V
 			if ("g".equals(mNode.name) && "(F)V".equals(mNode.desc)) {
 				AbstractInsnNode[] insnList = mNode.instructions.toArray();
 
 				for (int i = 0; i < insnList.length; i++) {
 					if (insnList[i] instanceof LdcInsnNode) {
 						LdcInsnNode liNode = (LdcInsnNode)insnList[i];
 
 						if (liNode.cst instanceof Float && ((Float)liNode.cst).floatValue() == 1.62F) {
 							mNode.instructions.set(liNode, new LdcInsnNode(new Float(0.425F)));
 							ASMDebugUtils.info("Override EntityRenderer");
 							break;
 						}
 					}
 				}
 
 				break;
 			}
 		}
 
 		return this.decode(cNode);
 	}
 
 	private byte[] transformItem(byte[] bytes) {
 		ClassNode cNode = this.encode(bytes);
 
 		for (MethodNode mNode : (List<MethodNode>)cNode.methods) {
 			// a (LWorld;LEntityPlayer;Z)LMovingObjectPosition;
 			if ("a".equals(mNode.name) && "(Lyc;Lqx;Z)Laoh".equals(mNode.desc)) {
 				AbstractInsnNode[] insnList = mNode.instructions.toArray();
 
 				for (int i = 0; i < insnList.length; i++) {
 					if (insnList[i] instanceof LdcInsnNode) {
 						LdcInsnNode liNode = (LdcInsnNode)insnList[i];
 
 						if (liNode.cst instanceof Double && ((Double)liNode.cst).doubleValue() == 1.62D) {
 							mNode.instructions.set(liNode, new LdcInsnNode(new Double(0.425D)));
 							ASMDebugUtils.info("Override Item");
 							break;
 						}
 					}
 				}
 			}
 		}
 
 		return this.decode(cNode);
 	}
 
 	private byte[] transformItemBoat(byte[] bytes) {
 		ClassNode cNode = this.encode(bytes);
 
 		for (MethodNode mNode : (List<MethodNode>)cNode.methods) {
 			// onItemRightClick (Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/item/ItemStack;
 			if ("a".equals(mNode.name) && "(Lur;Lyc;Lqx;)Lur".equals(mNode.desc)) {
 				AbstractInsnNode[] insnList = mNode.instructions.toArray();
 
 				for (int i = 0; i < insnList.length; i++) {
 					if (insnList[i] instanceof LdcInsnNode) {
 						LdcInsnNode liNode = (LdcInsnNode)insnList[i];
 
 						if (liNode.cst instanceof Double && ((Double)liNode.cst).doubleValue() == 1.62D) {
 							mNode.instructions.set(liNode, new LdcInsnNode(new Double(0.425D)));
 							ASMDebugUtils.info("Override ItemBoat");
 							break;
 						}
 					}
 				}
 			}
 		}
 
 		return this.decode(cNode);
 	}
 
 	private byte[] transformItemBucket(byte[] bytes) {
 		ClassNode cNode = this.encode(bytes);
 
 		for (MethodNode mNode : (List<MethodNode>)cNode.methods) {
 			// onItemRightClick (Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/item/ItemStack;
 			if ("a".equals(mNode.name) && "(Lur;Lyc;Lqx;)Lur".equals(mNode.desc)) {
 				AbstractInsnNode[] insnList = mNode.instructions.toArray();
 
 				for (int i = 0; i < insnList.length; i++) {
 					if (insnList[i] instanceof LdcInsnNode) {
 						LdcInsnNode liNode = (LdcInsnNode)insnList[i];
 
 						if (liNode.cst instanceof Double && ((Double)liNode.cst).doubleValue() == 1.62D) {
 							mNode.instructions.set(liNode, new LdcInsnNode(new Double(0.425D)));
 							ASMDebugUtils.info("Override ItemBucket");
 							break;
 						}
 					}
 				}
 			}
 		}
 
 		return this.decode(cNode);
 	}
 
 	private byte[] transformItemEnderEye(byte[] bytes) {
 		ClassNode cNode = this.encode(bytes);
 
 		for (MethodNode mNode : (List<MethodNode>)cNode.methods) {
 			// onItemRightClick (Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/item/ItemStack;
 			if ("a".equals(mNode.name) && "(Lur;Lyc;Lqx;)Lur".equals(mNode.desc)) {
 				AbstractInsnNode[] insnList = mNode.instructions.toArray();
 
 				for (int i = 0; i < insnList.length; i++) {
 					if (insnList[i] instanceof LdcInsnNode) {
 						LdcInsnNode liNode = (LdcInsnNode)insnList[i];
 
 						if (liNode.cst instanceof Double && ((Double)liNode.cst).doubleValue() == 1.62D) {
 							mNode.instructions.set(liNode, new LdcInsnNode(new Double(0.425D)));
 							ASMDebugUtils.info("Override ItemEnderEye");
 							break;
 						}
 					}
 				}
 			}
 		}
 
 		return this.decode(cNode);
 	}
 
 	private byte[] transformNetServerHandler(byte[] bytes) {
 		ClassNode cNode = this.encode(bytes);
 
 		for (MethodNode mNode : (List<MethodNode>)cNode.methods) {
 			// setPlayerLocation (DDDFF)V
 			if ("a".equals(mNode.name) && "(DDDFF)V".equals(mNode.desc)) {
 				AbstractInsnNode[] insnList = mNode.instructions.toArray();
 
 				for (int i = 0; i < insnList.length; i++) {
 					if (insnList[i] instanceof LdcInsnNode) {
 						LdcInsnNode liNode = (LdcInsnNode)insnList[i];
 
 						if (liNode.cst instanceof Double && ((Double)liNode.cst).doubleValue() == 1.6200000047683716D) {
 							mNode.instructions.set(liNode, new LdcInsnNode(new Double(0.4250000047683716D)));
 							ASMDebugUtils.info("Override NetServerHandler");
 							break;
 						}
 					}
 				}
 			}
 		}
 
 		return this.decode(cNode);
 	}
 
 	protected ClassNode encode(byte[] bytes) {
 		ClassNode cNode = new ClassNode();
 		ClassReader cReader = new ClassReader(bytes);
 		cReader.accept(cNode, 0);
 		return cNode;
 	}
 
 	protected byte[] decode(ClassNode cNode) {
 		ClassWriter cWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
 		cNode.accept(cWriter);
 		return cWriter.toByteArray();
 	}
 }
