 package teamm.mods.virtious.lib;
 
 import net.minecraft.creativetab.CreativeTabs;
 
 public class VirtiousCreativeTab extends CreativeTabs 
 {
 	public VirtiousCreativeTab(int par1, String par2Str) 
 	{
 		super(par1, par2Str);
 	}
 	
    public int getTabIconItemIndex()
     {
     	return VirtiousBlocks.deepStone.blockID;
     }
     
     public String getTranslatedTabLabel()
     {
     	return "Virtious Mod";
     }
 }
