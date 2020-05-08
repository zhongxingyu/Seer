 package bettervillages;
 
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import cpw.mods.fml.common.eventhandler.SubscribeEvent;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.init.Blocks;
 import net.minecraft.world.World;
 import net.minecraft.world.biome.BiomeGenBase;
 import net.minecraft.world.gen.structure.MapGenStructureIO;
 import net.minecraft.world.gen.structure.StructureVillagePieces;
 import net.minecraftforge.common.BiomeManager;
 import net.minecraftforge.common.config.Configuration;
 import net.minecraftforge.common.MinecraftForge;
 import cpw.mods.fml.common.eventhandler.Event.Result;
 import net.minecraftforge.event.terraingen.PopulateChunkEvent.Post;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.registry.VillagerRegistry;
 import cpw.mods.fml.common.registry.VillagerRegistry.IVillageCreationHandler;
 
 @Mod(modid = "bettervillages", name = "Better Villages Mod", version = BetterVillages.VERSION, useMetadata = true)
 public class BetterVillages {
     public static final String VERSION = "0.2";
 	public static final Block FLAG_ID = Blocks.planks;
 	public static Block pathWay = Blocks.planks, fieldFence = Blocks.fence;
 	public static boolean lilies = true, fields = true, gates = true, wells = true, woodHut = true, torch = true, big = true;
 	public static String[] biomeNames = new String[] { BiomeGenBase.desertHills.biomeName, BiomeGenBase.extremeHills.biomeName, BiomeGenBase.extremeHillsEdge.biomeName, BiomeGenBase.jungle.biomeName,
 			BiomeGenBase.jungleHills.biomeName, BiomeGenBase.ocean.biomeName, BiomeGenBase.swampland.biomeName, BiomeGenBase.taiga.biomeName, BiomeGenBase.taigaHills.biomeName,
 			BiomeGenBase.icePlains.biomeName, BiomeGenBase.iceMountains.biomeName, BiomeGenBase.forest.biomeName };
 	public static List<String> villageSpawnBiomes;
 	public static List<IVillageCreationHandler> handlers = new ArrayList<IVillageCreationHandler>();
 	static {
 		handlers.add(new VillageCreationHandler(StructureVillagePieces.House4Garden.class, 4, 2, 4, 2));
 		handlers.add(new VillageCreationHandler(StructureVillagePieces.Church.class, 20, 0, 1, 1));
 		handlers.add(new VillageCreationHandler(StructureVillagePieces.WoodHut.class, 3, 2, 5, 3));
 		handlers.add(new VillageCreationHandler(StructureVillagePieces.Hall.class, 15, 0, 2, 1));
 		handlers.add(new VillageCreationHandler(StructureVillagePieces.Field1.class, 3, 1, 4, 1));
 		handlers.add(new VillageCreationHandler(StructureVillagePieces.Field2.class, 3, 2, 4, 2));
         handlers.add(new VillageCreationHandler(StructureVillagePieces.House1.class, 20, 0, 2, 1));
 		handlers.add(new VillageCreationHandler(StructureVillagePieces.House2.class, 15, 0, 1, 1));
 		handlers.add(new VillageCreationHandler(StructureVillagePieces.House3.class, 8, 0, 3, 2));
 	}
 
 	@EventHandler
 	public void configLoad(FMLPreInitializationEvent event) {
 		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
 		pathWay = (Block)Block.field_149771_c.getObject(config.get("general", "Ocean_villages_path", "planks", "Block used for streets of Villages built in Ocean biome").getString());
         if(pathWay == null){
             pathWay = Blocks.planks;
         }
 		villageSpawnBiomes = Arrays.asList(config.get("general", "Available_biomes", biomeNames, "Biomes where villages should be added, by biome name").getStringList());
 		lilies = config.get("general", "Spawn_waterlily", lilies, "Water lilies can be found on water in villages").getBoolean(lilies);
 		wells = config.get("general", "Decorate_wells", wells, "Village wells should be improved").getBoolean(wells);
 		fields = config.get("general", "Decorate_fields", fields, "Village fields should be improved").getBoolean(fields);
 		woodHut = config.get("general", "Decorate_huts", woodHut, "Village wood huts should be improved").getBoolean(woodHut);
 		gates = config.get("general", "Add_gates", gates, "Fence gates added to village fields").getBoolean(gates);
 		torch = config.get("general", "Add_new_torch", torch, "Better torch has a chance to appear in villages").getBoolean(torch);
 		big = config.get("general", "Bigger_Villages", big, "Villages generates in clusters").getBoolean(big);
 		if (config.hasChanged())
 			config.save();
 	}
 
 	@EventHandler
 	public void load(FMLInitializationEvent event) {
 		if (villageSpawnBiomes != null && !villageSpawnBiomes.isEmpty()) {
 			for (BiomeGenBase biome : BiomeGenBase.func_150565_n()) {
 				if (biome != null && villageSpawnBiomes.contains(biome.biomeName)) {
 					BiomeManager.addVillageBiome(biome, true);//boolean has no effect ?
 				}
 			}
 		}
 		MinecraftForge.EVENT_BUS.register(this);
         if (villageSpawnBiomes.contains(BiomeGenBase.ocean.biomeName)|| villageSpawnBiomes.contains(BiomeGenBase.field_150575_M.biomeName)){
 		    MinecraftForge.TERRAIN_GEN_BUS.register(this);
         }
 		if (torch) {
            try{
                int mod;
                for(Method meth : MapGenStructureIO.class.getDeclaredMethods()){
                    mod = meth.getModifiers();
                    if(!Modifier.isPublic(mod) && !Modifier.isPrivate(mod) && meth.getParameterTypes().length==2 && String.class.isAssignableFrom(meth.getParameterTypes()[1])){
                        meth.setAccessible(true);
                        meth.invoke(null, ComponentBetterVillageTorch.class, "BViT");//func_143031_a
                        break;
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
 			VillagerRegistry.instance().registerVillageCreationHandler(new VillageCreationHandler(ComponentBetterVillageTorch.class, 15, 0, 1, 1));
 		}
 		if (big) {
 			for (IVillageCreationHandler handler : handlers) {
 				VillagerRegistry.instance().registerVillageCreationHandler(handler);
 			}
 		}
 	}
 
 	@SubscribeEvent
 	public void onPopulating(Post event) {
 		if (event.hasVillageGenerated) {
 			int i = event.chunkX * 16;
 			int k = event.chunkZ * 16;
 			BiomeGenBase biome = event.world.getBiomeGenForCoords(i, k);
 			Block borderId = biome == BiomeGenBase.desert ? Blocks.sandstone : Blocks.log;
 			int y, p;
             Block id;
 			int[] field;
 			List<int[]> list;
 			for (int x = i; x < i + 16; x++) {
 				for (int z = k; z < k + 16; z++) {//Search within chunk
 					if (biome == BiomeGenBase.ocean || biome == BiomeGenBase.field_150575_M) {
 						y = event.world.getTopSolidOrLiquidBlock(x, z) - 1;//ignores water
 						id = event.world.func_147439_a(x, y, z);
 						if (id == Blocks.wool && event.world.func_147439_a(x-1, y-1, z) == Blocks.torch && event.world.func_147439_a(x+1, y-1, z) == Blocks.torch && event.world.func_147439_a(x, y-1, z-1) == Blocks.torch && event.world.func_147439_a(x, y-1, z+1) == Blocks.torch) {
 							//Definetly a torch
                             if (isReplaceable(event.world, x, y - 4, z))
 								event.world.func_147449_b(x, y - 4, z, pathWay);
 							continue;
 						}
 						if (id == Blocks.oak_stairs) {
 							do {
 								y--;
 							} while (event.world.func_147437_c(x, y, z) || isWaterId(event.world.func_147439_a(x, y, z)));
 							id = event.world.func_147439_a(x, y, z);
 						}
 						if (id == FLAG_ID) {//Use flag
 							id = event.world.func_147439_a(x, y + 1, z);
 							if (isWaterId(id)) {
 								event.world.func_147465_d(x, y, z, id, 0, 2);//destroy flag
 								while (!event.world.func_147437_c(x, y, z))
 									y++;
 								event.world.func_147449_b(x, y, z, pathWay);//rebuilt pathway
 							}
 							continue;
 						}
 					}
 					y = event.world.getHeightValue(x, z);//block on top of a "solid" block
 					if (y > 1) {
 						y--;
 						id = event.world.func_147439_a(x, y, z);
 						while (id.isAir(event.world, x, y, z) || id.isLeaves(event.world, x, y, z)) {
 							y--;
 							id = event.world.func_147439_a(x, y, z);
 						}
 						if (isWaterId(id)) {//found water in open air
 							if (lilies && event.world.func_147437_c(x, y + 1, z) && event.rand.nextInt(10) == 0)
 								event.world.func_147465_d(x, y + 1, z, Blocks.waterlily, 0, 2);//place waterlily randomly
 							if (gates) {
 								field = new int[] { x, y, z };
 								list = getBorder(event.world, id, field);
 								if (list.size() == 1) {//found 2 water blocks
 									list = getBorder(event.world, borderId, field);
 									if (list.size() == 3) {//found a 3 blocks border, assuming water in a village field
 										field = list.get(1);//get middle border block
 										if (isReplaceable(event.world, field[0], field[1] + 1, field[2])) {
 											//find orientation for fence gate
 											p = 0;//south
 											if (x - field[0] < 0)
 												p = 1;//west
 											else if (x - field[0] > 0)
 												p = 3;//east
 											else if (z - field[2] < 0)
 												p = 2;//north
 											event.world.func_147465_d(field[0], field[1] + 1, field[2], Blocks.fence_gate, p, 2);//place fence gate
 										}
 									}
 								}
 							}
 							continue;
 						}
 						if (fields && id == Blocks.farmland) {//found tilled field in open air, assuming this is a village field
 							field = new int[] { x, y, z };
 							list = getBorder(event.world, borderId, field);
 							if (!list.isEmpty()) {
 								switch (list.size()) {
 								case 3://simple border case
 									field = list.get(1);//get middle border block
 									if (isReplaceable(event.world, field[0], field[1] + 1, field[2]))
 										event.world.func_147465_d(field[0], field[1] + 1, field[2], fieldFence, 0, 2);//place fence
 									break;
 								case 5://corner case
 									field = list.remove(1);
 									if (isReplaceable(event.world, field[0], field[1] + 1, field[2]))
 										event.world.func_147465_d(field[0], field[1] + 1, field[2], fieldFence, 0, 2);
 									field = list.remove(2);
 									if (isReplaceable(event.world, field[0], field[1] + 1, field[2]))
 										event.world.func_147465_d(field[0], field[1] + 1, field[2], fieldFence, 0, 2);
 									for (int[] pos : list) {
 										if (isReplaceable(event.world, pos[0], pos[1] + 1, pos[2])) {
 											event.world.func_147465_d(pos[0], pos[1] + 1, pos[2], fieldFence, 0, 2);
 											if (isReplaceable(event.world, pos[0], pos[1] + 2, pos[2]) && isCorner(event.world, borderId, pos))
 												event.world.func_147465_d(pos[0], pos[1] + 2, pos[2], Blocks.torch, 0, 2);
 										}
 									}
 									break;
 								default:
 									break;
 								}
 							}
 							list = null;
 							continue;
 						}
 						if (wells && id == Blocks.cobblestone) {//found cobblestone in open air
 							id = event.world.func_147439_a(x, y - 4, z);
 							if (isWaterId(id)) {//found water under cobblestone layer
 								y -= 4;
 								field = new int[] { x, y, z };
 								list = getBorder(event.world, id, field);
 								if (list.size() == 3) {//found 4 water blocks
 									list = getBorder(event.world, Blocks.cobblestone, field);
 									if (list.size() == 5) {//found 5 cobblestone surrounding one water block, assuming this is a village well
 										field = list.remove(1);
 										event.world.func_147465_d(field[0], field[1] + 1, field[2], Blocks.stone_slab, 0, 2);
 										field = list.remove(2);
 										event.world.func_147465_d(field[0], field[1] + 1, field[2], Blocks.stone_slab, 0, 2);
 										for (int[] pos : list) {
 											for (int[] posb : getBorder(event.world, Blocks.gravel, pos))
 												event.world.func_147465_d(posb[0], posb[1], posb[2], Blocks.stone_slab, 0, 2);
 										}
 										while (event.world.func_147439_a(x, y, z) == id) {
 											y--;
 										}
 										field = new int[] { x, y, z };
 										list = getBorder(event.world, Blocks.cobblestone, field);
 										for (int[] pos : list)
 											event.world.func_147465_d(pos[0], pos[1], pos[2], Blocks.iron_block, 0, 2);
 										event.world.func_147465_d(field[0], field[1], field[2], Blocks.iron_block, 0, 2);
 									}
 								}
 							}
 							continue;
 						}
 						if (woodHut && id == borderId) {//Found top
 							do {
 								y--;
 								id = event.world.func_147439_a(x, y, z);
 							} while (id.isAir(event.world, x, y, z) || !id.func_149721_r());//not opaque
 							if (id == Blocks.dirt) {//Found dirt floor
 								event.world.func_147449_b(x, y, z, borderId);
 								list = getBorder(event.world, Blocks.cobblestone, new int[] { x, y, z });
 								for (int[] pos : list) {
 									event.world.func_147465_d(pos[0], pos[1], pos[2], Blocks.stone, 0, 2);
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 	@SubscribeEvent
 	public void onSettingGravel(net.minecraftforge.event.terraingen.BiomeEvent.GetVillageBlockID event) {
 		if ((event.biome == BiomeGenBase.ocean || event.biome == BiomeGenBase.field_150575_M) && event.original == Blocks.gravel) {
 			event.replacement = FLAG_ID;//flag used to reconstruct pathway afterward
 			event.setResult(Result.DENY);
 		}
 	}
 
 	private static List<int[]> getBorder(World world, Block id, int[] field) {
 		List<int[]> list = new ArrayList<int[]>();
 		for (int x = field[0] - 1; x < field[0] + 2; x++) {
 			for (int z = field[2] - 1; z < field[2] + 2; z++) {
 				if ((x != field[0] || z != field[2]) && world.func_147439_a(x, field[1], z) == id)
 					list.add(new int[] { x, field[1], z });
 			}
 		}
 		return list;
 	}
 
 	private static boolean isCorner(World world, Block id, int[] pos) {
 		List<int[]> list = getBorder(world, id, pos);
 		if (list.size() < 2)
 			return false;
 		int[] a = list.get(0);
 		int[] b = list.get(1);
 		return a[0] != b[0] && a[2] != b[2];
 	}
 
 	private static boolean isReplaceable(World world, int x, int y, int z) {
 		return world.func_147437_c(x, y, z) || world.func_147439_a(x, y, z).isReplaceable(world, x, y, z);
 	}
 
 	private static boolean isWaterId(Block id) {
 		return id.func_149688_o() == Material.field_151586_h;
 	}
 }
