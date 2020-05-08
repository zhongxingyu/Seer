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
            player.sendChatToPlayer("2Your current temperature is 4unknown2...");
         }
        world.getBiomeGenForCoords(player.chunkCoordX, player.chunkCoordZ).temperature();
         return itemStack;
     }
 }
