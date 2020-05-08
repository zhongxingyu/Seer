 package modJam;
 
 import java.util.List;
 
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.Icon;
 
 public class ItemAwesomeIngot extends Item{
 
 	private Icon[] iconsList = new Icon[16];
 	
 	private static final String[] subNames = { 
 		"white", "orange", "magenta",
 		"lBlue", "yellow", "lime", "pink", "gray", "lGray", "cyan",
 		"purple", "blue", "brown", "green", "red", "black"
 	};
 	
 	public ItemAwesomeIngot(int par1) {
 		super(par1);
 		this.setHasSubtypes(true);
 	}
 	
 	@Override
     public Icon getIconFromDamage(int par1)
     {
        return this.iconsList[par1];
     }
 	
     /**
      * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
      */
     @Override
     public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List)
     {
         par3List.add(new ItemStack(par1, 1, 0));
         par3List.add(new ItemStack(par1, 1, 1));
         par3List.add(new ItemStack(par1, 1, 2));
         par3List.add(new ItemStack(par1, 1, 3));
         par3List.add(new ItemStack(par1, 1, 4));
         par3List.add(new ItemStack(par1, 1, 5));
         par3List.add(new ItemStack(par1, 1, 6));
         par3List.add(new ItemStack(par1, 1, 7));
         par3List.add(new ItemStack(par1, 1, 8));
         par3List.add(new ItemStack(par1, 1, 9));
         par3List.add(new ItemStack(par1, 1, 10));
         par3List.add(new ItemStack(par1, 1, 11));
         par3List.add(new ItemStack(par1, 1, 12));
         par3List.add(new ItemStack(par1, 1, 13));
         par3List.add(new ItemStack(par1, 1, 14));
         par3List.add(new ItemStack(par1, 1, 15));
     }
     
     @Override
     public void updateIcons(IconRegister par1IconRegister)
     {
     	this.iconsList[0] = par1IconRegister.registerIcon("awesomeMod:fuj1n.AwesomeMod.awesomeIngotWhite");
     	this.iconsList[1] = par1IconRegister.registerIcon("awesomeMod:fuj1n.AwesomeMod.awesomeIngotOrange");
     	this.iconsList[2] = par1IconRegister.registerIcon("awesomeMod:fuj1n.AwesomeMod.awesomeIngotMagenta");
     	this.iconsList[3] = par1IconRegister.registerIcon("awesomeMod:fuj1n.AwesomeMod.awesomeIngotLBlue");
     	this.iconsList[4] = par1IconRegister.registerIcon("awesomeMod:fuj1n.AwesomeMod.awesomeIngotYellow");
     	this.iconsList[5] = par1IconRegister.registerIcon("awesomeMod:fuj1n.AwesomeMod.awesomeIngotLime");
     	this.iconsList[6] = par1IconRegister.registerIcon("awesomeMod:fuj1n.AwesomeMod.awesomeIngotPink");
     	this.iconsList[7] = par1IconRegister.registerIcon("awesomeMod:fuj1n.AwesomeMod.awesomeIngotGray");
     	this.iconsList[8] = par1IconRegister.registerIcon("awesomeMod:fuj1n.AwesomeMod.awesomeIngotLGray");
     	this.iconsList[9] = par1IconRegister.registerIcon("awesomeMod:fuj1n.AwesomeMod.awesomeIngotCyan");
     	this.iconsList[10] = par1IconRegister.registerIcon("awesomeMod:fuj1n.AwesomeMod.awesomeIngotPurple");
     	this.iconsList[11] = par1IconRegister.registerIcon("awesomeMod:fuj1n.AwesomeMod.awesomeIngotBlue");
     	this.iconsList[12] = par1IconRegister.registerIcon("awesomeMod:fuj1n.AwesomeMod.awesomeIngotBrown");
     	this.iconsList[13] = par1IconRegister.registerIcon("awesomeMod:fuj1n.AwesomeMod.awesomeIngotGreen");
     	this.iconsList[14] = par1IconRegister.registerIcon("awesomeMod:fuj1n.AwesomeMod.awesomeIngotRed");
     	this.iconsList[15] = par1IconRegister.registerIcon("awesomeMod:fuj1n.AwesomeMod.awesomeIngotBlack");
     }
     
     @Override
     public String getUnlocalizedName(ItemStack par1ItemStack){
    	return getUnlocalizedName() + "." + subNames[par1ItemStack.getItemDamage()];
     }
 
 }
