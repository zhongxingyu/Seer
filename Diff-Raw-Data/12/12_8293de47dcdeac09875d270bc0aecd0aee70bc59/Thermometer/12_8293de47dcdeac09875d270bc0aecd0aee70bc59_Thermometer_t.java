 package heatcraft.items;
 
 import heatcraft.HeatCraft;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.world.World;
 
 public class Thermometer extends Item {
 
     public Thermometer(int id) {
         super(id);
         setMaxStackSize(1);
         setCreativeTab(HeatCraft.tabHeatCraft);
         setItemName("itemThermometer");
         setTextureFile(heatcraft.CommonProxy.ITEMS_PNG);
         setIconIndex(0);
     }
     
     @Override
     public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
         if(!world.isRemote) {
            double dx = player.posX;
            double dz = player.posZ;
            int x = (int) (long)Math.floor(dx);
            int z = (int) (long)Math.floor(dz);
            Float temp2 = world.getBiomeGenForCoords(x, z).temperature;
            String temperature = Float.toString(temp2);
            String msg = "2Your current temperature is 4" + temperature + "2...";
            player.sendChatToPlayer(msg);
         }
        
         return itemStack;
     }
 }
