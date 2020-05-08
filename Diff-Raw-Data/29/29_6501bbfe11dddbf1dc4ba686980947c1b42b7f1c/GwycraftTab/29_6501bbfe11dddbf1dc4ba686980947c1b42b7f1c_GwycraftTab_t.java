 package gwydion0917.gwycraft;
 
 
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
 import net.minecraft.creativetab.CreativeTabs;
 
 public class GwycraftTab extends CreativeTabs
 {
    GwycraftTab(String par2Str)
    {
        super(par2Str);
    }

    @SideOnly(Side.CLIENT)

    /**
     * the itemID for the item to be displayed on the tab
     */
    public int getTabIconItemIndex()
    {
        return ConfigGwycraft.blockGemCompressedID;
    }
 }
