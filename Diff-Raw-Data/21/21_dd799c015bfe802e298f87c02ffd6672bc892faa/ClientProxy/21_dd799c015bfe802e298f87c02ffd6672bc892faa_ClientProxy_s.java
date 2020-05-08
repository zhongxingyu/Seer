 package vivadaylight3.myrmecology.client;
 
 import net.minecraftforge.client.model.AdvancedModelLoader;
 import vivadaylight3.myrmecology.client.model.ModelAnt;
 import vivadaylight3.myrmecology.client.renderer.RenderAnt;
 import vivadaylight3.myrmecology.common.CommonProxy;
 import vivadaylight3.myrmecology.common.entity.ant.EntityAntForest;
 import cpw.mods.fml.client.registry.RenderingRegistry;
 
 public class ClientProxy extends CommonProxy {
 
     @Override
     public void registerRenderers() {
 	RenderingRegistry.registerEntityRenderingHandler(EntityAntForest.class, new RenderAnt(new ModelAnt(), 0.5f));
     }
 
 }
