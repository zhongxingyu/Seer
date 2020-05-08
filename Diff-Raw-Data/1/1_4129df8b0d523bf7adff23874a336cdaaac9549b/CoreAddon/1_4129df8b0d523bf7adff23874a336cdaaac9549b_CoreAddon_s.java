 package denoflionsx.DenPipes.AddOns.Core;
 
 import buildcraft.api.gates.ActionManager;
 import buildcraft.api.gates.IAction;
 import buildcraft.transport.BlockGenericPipe;
 import static buildcraft.transport.BlockGenericPipe.createPipe;
 import buildcraft.transport.ItemPipe;
 import buildcraft.transport.Pipe;
 import buildcraft.transport.TransportProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import denoflionsx.DenPipes.API.Actions.DenAction;
 import denoflionsx.DenPipes.API.Annotations.PipeName;
 import denoflionsx.DenPipes.API.DenPipesAPI;
 import denoflionsx.DenPipes.API.Interfaces.IDenPipeAddon;
 import denoflionsx.DenPipes.API.Managers.IActionManager;
 import denoflionsx.DenPipes.API.Managers.IPipeManager;
 import denoflionsx.DenPipes.AddOns.Core.Pipe.DenItemPipe;
 import denoflionsx.DenPipes.Config.Tuning;
 import denoflionsx.DenPipes.Core.DenPipesCore;
 import denoflionsx.denLib.Mod.Handlers.WorldHandler.IdenWorldEventHandler;
 import denoflionsx.denLib.Mod.Handlers.WorldHandler.WorldEventHandler;
 import java.lang.annotation.Annotation;
import net.minecraft.item.Item;
 
 public class CoreAddon implements IDenPipeAddon, IPipeManager, IdenWorldEventHandler, IActionManager {
 
     private int startID = 20000;
     private int startActionID = 333;
 
     @Override
     public void preinit(FMLPreInitializationEvent event) {
         DenPipesAPI.manager.Pipes = this;
         DenPipesAPI.manager.Actions = this;
     }
 
     @Override
     public void init(FMLInitializationEvent event) {
     }
 
     @Override
     public void postinit(FMLPostInitializationEvent evt) {
         WorldEventHandler.registerHandler(this);
     }
 
     @Override
     public ItemPipe registerPipe(Class<? extends Pipe> Class) {
         int itemID = this.getIDForPipe(Class);
         DenItemPipe pipe = new DenItemPipe(itemID);
         BlockGenericPipe.pipes.put(pipe.itemID, Class);
         String name = "";
         for (Annotation a : Class.getDeclaredAnnotations()) {
             if (a instanceof PipeName) {
                 PipeName name1;
                 name1 = (PipeName) a;
                 name = name1.name();
             }
         }
         pipe.name = name;
         Pipe dummyPipe = createPipe(pipe.itemID);
         if (dummyPipe != null) {
             pipe.setPipeIconIndex(dummyPipe.getIconIndexForItem());
             TransportProxy.proxy.setIconProviderFromPipe(pipe, dummyPipe);
         }
         DenPipesCore.proxy.registerPipeRendering(pipe);
         return pipe;
     }
 
     @Override
     public int getIDForPipe(Class<? extends Pipe> Class) {
         return Tuning.config.getItem(Class.getName(), startID++).getInt();
     }
 
     @Override
     public void onWorldLoaded() {
         Tuning.config.save();
         WorldEventHandler.unregisterHandler(this);
     }
 
     @Override
     public void onWorldEnded() {
     }
 
     @Override
     public IAction registerAction(Class<? extends DenAction> Class) {
         try {
             int id = this.getIDForAction(Class);
             if (ActionManager.actions[id] == null) {
                 ActionManager.actions[id] = (IAction) Class.getConstructor(new Class[]{int.class}).newInstance(id);
                 return ActionManager.actions[id];
             } else {
                 DenPipesCore.proxy.severe("Action id " + id + " is taken when trying to register " + Class.getName());
             }
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         return null;
     }
 
     @Override
     public int getIDForAction(Class<? extends DenAction> Class) {
         return Tuning.config.get("actions", Class.getName(), startActionID++).getInt();
     }
 }
