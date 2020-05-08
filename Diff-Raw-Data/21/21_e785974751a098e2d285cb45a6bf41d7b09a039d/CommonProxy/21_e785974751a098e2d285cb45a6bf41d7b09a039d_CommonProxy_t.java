 package silentAbyss.core.proxy;
 
 import silentAbyss.core.handlers.tick.PlayerClientServerTickHandler;
 import silentAbyss.core.handlers.tick.PlayerServerScheduledTickHandler;
 import silentAbyss.core.handlers.tick.PlayerServerTickHandler;
 import silentAbyss.core.handlers.tick.WorldScheduledTickHandler;
 import silentAbyss.tileentity.TileEntityAbyssTeleporter;
 import silentAbyss.tileentity.TileEntitySigilInfuser;
 import silentAbyss.tileentity.TileTest;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.TickRegistry;
 import cpw.mods.fml.relauncher.Side;
 
 public class CommonProxy {
 
     public CommonProxy() {
 
     }
 
     public void registerRenderers() {
 
     }
 
     public void registerTickHandlers() {
 
         // TickRegistry.registerTickHandler(new ServerTickHandler(),
         // Side.SERVER);
         TickRegistry.registerTickHandler(new PlayerServerTickHandler(), Side.SERVER);
        //TickRegistry.registerTickHandler(new PlayerClientServerTickHandler(), Side.SERVER);
         TickRegistry.registerScheduledTickHandler(new PlayerServerScheduledTickHandler(), Side.SERVER);
         TickRegistry.registerScheduledTickHandler(new WorldScheduledTickHandler(), Side.SERVER);
     }
 
     public void registerTileEntities() {
 
         GameRegistry.registerTileEntity(TileEntityAbyssTeleporter.class, "tileEntityAbyssTeleporter");
         GameRegistry.registerTileEntity(TileEntitySigilInfuser.class, "tileEntitySigilInfuser");
         GameRegistry.registerTileEntity(TileTest.class, "tileTest");
     }
 }
