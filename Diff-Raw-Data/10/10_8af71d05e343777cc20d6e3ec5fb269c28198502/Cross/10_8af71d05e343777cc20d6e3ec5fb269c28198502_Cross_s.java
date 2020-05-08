 package common.items;
 
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
 
    @Override
     @SideOnly(Side.CLIENT)
    public void registerIcons(IconRegister iconRegister) {
        this.itemIcon = iconRegister.registerIcon("FaithCraft:" + itemName);
     }
 
 }
