 package cp.mods.core.item;
 
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.item.Item;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public abstract class ItemBase extends Item
 {
     private static final int shiftIndex = 256;
     private String name;
 
     public ItemBase(int par1)
     {
         super(par1 - shiftIndex);
     }
     
     @SideOnly(Side.CLIENT)
     public void updateIcons(IconRegister iconRegister)
     {
        iconIndex = iconRegister.registerIcon(this.getName());
     }
     
     public String getName(){
         return name;
     }
    
    public void setName(String name){
        this.name = name;
        setUnlocalizedName(name.toLowerCase().replace(':', '.'));
    }
 
     @Override
     public Item setUnlocalizedName(String name)
     {
         return super.setUnlocalizedName(name);
     }
 }
