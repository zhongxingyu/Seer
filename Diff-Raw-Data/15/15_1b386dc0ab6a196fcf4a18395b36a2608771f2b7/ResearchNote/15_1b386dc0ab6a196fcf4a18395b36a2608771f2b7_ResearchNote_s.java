 package industrialscience.modules.research.frontend;
 
import industrialscience.modules.ISAbstractModule;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.item.Item;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 public class ResearchNote extends Item {
 
     public ResearchNote(int id) {
         super(id);
         setMaxStackSize(1);
         setUnlocalizedName("Research Note");
         LanguageRegistry.addName(this, "Research Note");
        setCreativeTab(ISAbstractModule.getCreativeTab());
     }
 
     @Override
     public void registerIcons(IconRegister iconRegister) {
         itemIcon = iconRegister.registerIcon("paper");
     }
 
 }
