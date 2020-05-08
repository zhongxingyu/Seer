 package pharabus.mods.terrabow.item;
 
import pharabus.mods.terrabow.TerraBowSettings;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
 import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.util.DamageSource;
 import net.minecraft.util.Icon;
 import TFC.Core.TFCTabs;
 import TFC.Items.Pottery.ItemPotteryMold;
 
 public class ItemClayMold extends ItemPotteryMold {
 
     
     Icon CopperIcon;
     Icon BronzeIcon;
     Icon BismuthBronzeIcon;
     Icon BlackBronzeIcon;
     
     public ItemClayMold(int id) {
         super(id);
        setCreativeTab(TFCTabs.TFCPottery);
        
     }
     
     @Override
     public Icon getIconFromDamage(int damage) {
         if (damage == 0) return this.ClayIcon;
         if (damage == 1) return this.CeramicIcon;
         if (damage == 2) return this.CopperIcon;
         if (damage == 3) return this.BronzeIcon;
         if (damage == 4) return this.BismuthBronzeIcon;
         if (damage == 5) return this.BlackBronzeIcon;
         
         return this.ClayIcon;
     }
     
     @SideOnly(Side.CLIENT)
     @Override
     public void registerIcons(IconRegister iconRegister) {
                
         this.ClayIcon = iconRegister.registerIcon(TerraBowSettings.TEXTURE_LOCATION + ":" + "pottery/" + this.MetaNames[0]);
         this.CeramicIcon = iconRegister.registerIcon(TerraBowSettings.TEXTURE_LOCATION + ":" + "pottery/" + this.MetaNames[1]);
         if (this.MetaNames.length > 2) {
           this.CopperIcon = iconRegister.registerIcon(TerraBowSettings.TEXTURE_LOCATION + ":" + "pottery/" + this.MetaNames[2]);
           this.BronzeIcon = iconRegister.registerIcon(TerraBowSettings.TEXTURE_LOCATION + ":" + "pottery/" + this.MetaNames[3]);
           this.BismuthBronzeIcon = iconRegister.registerIcon(TerraBowSettings.TEXTURE_LOCATION + ":" + "pottery/" + this.MetaNames[4]);
           this.BlackBronzeIcon = iconRegister.registerIcon(TerraBowSettings.TEXTURE_LOCATION + ":" + "pottery/" + this.MetaNames[5]);
         }
     }
 
 }
