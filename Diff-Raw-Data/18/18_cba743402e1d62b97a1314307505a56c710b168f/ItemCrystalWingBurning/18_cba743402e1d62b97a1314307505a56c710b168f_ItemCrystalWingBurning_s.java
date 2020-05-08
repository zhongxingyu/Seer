 package bspkrs.crystalwing;
 
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.world.World;
 
 public class ItemCrystalWingBurning extends Item
 {
     
     public ItemCrystalWingBurning(int i)
     {
         super(i);
         maxStackSize = 1;
        // this.setCreativeTab(CreativeTabs.tabTransport);
     }
     
     @Override
     public void onUpdate(ItemStack itemstack, World world, Entity entity, int i, boolean flag)
     {
         if (entity.isInWater())
         {
             if (entity instanceof EntityPlayer)
             {
                 EntityPlayer entityplayer = (EntityPlayer) entity;
                 world.playSoundAtEntity(entity, "random.fizz", 1.0F, 1.0F);
                 entityplayer.addStat(CrystalWing.instance.burnedWing, 1);
                 replaceWings(entityplayer.inventory);
             }
             return;
         }
         if (!entity.isBurning())
         {
             entity.setFire(2);
         }
     }
     
     private void replaceWings(InventoryPlayer inventoryplayer)
     {
         for (int i = 0; i < inventoryplayer.getSizeInventory(); i++)
         {
             if (inventoryplayer.getStackInSlot(i) == null)
                 continue;
             
             ItemStack itemstack = inventoryplayer.getStackInSlot(i);
             if (itemstack.getItem() instanceof ItemCrystalWingBurning)
             {
                 itemstack.itemID = CrystalWing.instance.crystalWingBurned.itemID;
                 inventoryplayer.setInventorySlotContents(i, itemstack);
             }
         }
         
     }
 }
