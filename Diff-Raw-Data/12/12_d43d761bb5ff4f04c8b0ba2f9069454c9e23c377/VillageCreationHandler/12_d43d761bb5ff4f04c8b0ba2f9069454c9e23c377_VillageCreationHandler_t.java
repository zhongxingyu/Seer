 package bettervillages;
 
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import net.minecraft.util.MathHelper;
 import net.minecraft.world.gen.structure.ComponentVillage;
 import net.minecraft.world.gen.structure.ComponentVillageStartPiece;
 import net.minecraft.world.gen.structure.StructureVillagePieceWeight;
 import cpw.mods.fml.common.registry.VillagerRegistry.IVillageCreationHandler;
 
 public class VillageCreationHandler implements IVillageCreationHandler {
 	private Class<?> piece;
 	private int weight, min, max, multiplier;
 	private static Map<String, Method> methMap = new HashMap<String, Method>();
 
 	public VillageCreationHandler(Class<?> structure, int data, int min, int max, int multi) {
 		this.piece = structure;
 		this.weight = data;
 		this.min = min;
 		this.max = max;
 		this.multiplier = multi;
 	}
 
 	@Override
	public Object buildComponent(StructureVillagePieceWeight villagePiece, ComponentVillageStartPiece startPiece, @SuppressWarnings("rawtypes") List pieces, Random random, int p1, int p2, int p3, int p4, int p5) {
 		Class<?> clazz = villagePiece.villagePieceClass;
 		Object obj = null;
 		if (!methMap.containsKey(clazz.getName())) {
 			Method[] meths = clazz.getDeclaredMethods();
 			for (Method meth : meths) {
 				int mod = meth.getModifiers();
 				if (Modifier.isPublic(mod) && Modifier.isStatic(mod)) {
 					try {
 						obj = meth.invoke(null, startPiece, pieces, random, p1, p2, p3, p4, p5);
 						if (ComponentVillage.class.isInstance(obj)) {
 							methMap.put(clazz.getName(), meth);
 							break;
 						}
 					} catch (ReflectiveOperationException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 		} else {
 			try {
 				obj = methMap.get(clazz.getName()).invoke(null, startPiece, pieces, random, p1, p2, p3, p4, p5);
 			} catch (ReflectiveOperationException e) {
 				e.printStackTrace();
 			}
 		}
 		return obj;
 	}
 
 	@Override
 	public Class<?> getComponentClass() {
 		return piece;
 	}
 
 	@Override
 	public StructureVillagePieceWeight getVillagePieceWeight(Random random, int i) {
 		return new BetterStructureVillagePieceWeight(piece, weight, MathHelper.getRandomIntegerInRange(random, min + i, max + i * multiplier));
 	}
 }
