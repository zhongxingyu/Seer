 package mod.industrialscience.modules.fishing;
 
 
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.item.Item;
 
 public class LobsterItem extends Item {
 
 	
 	public LobsterItem(int id) {
                 super(id);
                
                 setMaxStackSize(64);
                 setUnlocalizedName("Lobster Item");
                 LanguageRegistry.addName(this, "Lobster Item");
         }
        
 	public void updateIcons(IconRegister iconRegister)
 	{
 	     
 		this.iconIndex = iconRegister.registerIcon("industrialscience:lobsteritem");
 	}
 }
