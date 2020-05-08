 package common.items;
 
import common.Reference;

 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.item.Item;
 
 public class Cross extends Item {
     private String itemName;
 
     public Cross(int id, String name) {
         super(id);
         this.itemName = name;
         this.setUnlocalizedName(name);
     }
 
     @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IconRegister register) {
        this.itemIcon = register.registerIcon(Reference.modid + ":" + (this.getUnlocalizedName().substring(5)));
     }
 
 }
