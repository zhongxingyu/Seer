 package simpletools.common.items.tool;
 
 import java.util.List;
 
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.world.World;
 import net.minecraftforge.common.ForgeDirection;
 import simpletools.api.IAttachment;
 import simpletools.api.SimpleToolsItems;
 import simpletools.common.misc.SimpleToolsCreativeTab;
 
 import com.google.common.collect.BiMap;
 import com.google.common.collect.HashBiMap;
 
 public class ItemAttachmentPlasma extends Item implements IAttachment
 {
     private final String[] names = { "tool.0", "tool.1", "tool.2", "tool.3", "plasmaTorch" };
 
     public ItemAttachmentPlasma(int itemID, String name)
     {
         super(itemID);
         this.setCreativeTab(SimpleToolsCreativeTab.INSTANCE);
         this.setUnlocalizedName(name);
         this.setMaxStackSize(16);
        this.setHasSubtypes(true);
     }
 
     @Override
     public String getUnlocalizedName(ItemStack is)
     {
         if (is.getItemDamage() < this.names.length)
             return this.getUnlocalizedName() + "." + this.names[is.getItemDamage()];
         else
             return this.getUnlocalizedName() + ".unknown";
 
     }
 
     @Override
     public String getToolType(ItemStack i)
     {
         switch (i.getItemDamage())
         {
             case 0:
             case 1:
             case 2:
             case 3:
                 return "all";
             case 4:
                 return "";
         }
         return "";
     }
 
     @Override
     public int getToolAttachmentType(ItemStack i)
     {
         return 1;
     }
 
     @Override
     public int getMinimumTier(ItemStack i)
     {
         switch (i.getItemDamage())
         {
             case 0:
             case 1:
             case 2:
             case 3:
                 return i.getItemDamage();
             case 4:
                 return 1;
         }
         return 0;
     }
 
     @Override
     public short getAttachmentUID(ItemStack i)
     {
         return ((Integer) (i.getItemDamage() + 40)).shortValue();
     }
 
     @Override
     public int getAttachmentTier(ItemStack i)
     {
         switch (i.getItemDamage())
         {
             case 0:
                 return 0;
             case 1:
                 return 1;
             case 2:
                 return 2;
             case 3:
                 return 3;
             default:
                 return -1;
         }
     }
 
     @Override
     public float getHarvestSpeed(ItemStack i)
     {
         switch (i.getItemDamage())
         {
             case 0:
                 return 7.0F;
             case 1:
                 return 12.0F;
             case 2:
                 return 17.0F;
             case 3:
                 return 22.0F;
             default:
                 return 1.0F;
         }
     }
 
     @Override
     public BiMap<Entity, Integer> getDamageVsEntities(ItemStack i)
     {
         BiMap<Entity, Integer> toReturn = HashBiMap.create();
         toReturn.put(null, 3);
         return toReturn;
     }
 
     @Override
     public boolean canRightClick(ItemStack i, Object targetData, EntityPlayer player)
     {
         if (i.getItemDamage() == 4 && targetData instanceof Object[])
         {
             Object[] target = (Object[]) targetData;
             int x = (Integer) target[1];
             int y = (Integer) target[2];
             int z = (Integer) target[3];
             int side = (Integer) target[4];
             if (target[0] != null && target[0] instanceof World)
             {
                 World world = (World) target[0];
 
                 if (world.getBlockId(x, y, z) == SimpleToolsItems.blockPlasmaTorch.blockID)
                     return true;
 
                 x += ForgeDirection.getOrientation(side).offsetX;
                 y += ForgeDirection.getOrientation(side).offsetY;
                 z += ForgeDirection.getOrientation(side).offsetZ;
                 if (world.getBlockId(x, y, z) == 0 || world.getBlockId(x, y, z) == SimpleToolsItems.blockPlasmaTorch.blockID)
                     return true;
             }
         }
         return false;
     }
 
     @Override
     public byte onRightClick(ItemStack i, Object targetData, EntityPlayer player)
     {
         if (i.getItemDamage() == 4 && targetData instanceof Object[])
         {
             Object[] target = (Object[]) targetData;
             int x = (Integer) target[1];
             int y = (Integer) target[2];
             int z = (Integer) target[3];
             int side = (Integer) target[4];
             if (target[0] != null && target[0] instanceof World)
             {
                 World world = (World) target[0];
 
                 if (world.getBlockId(x, y, z) == SimpleToolsItems.blockPlasmaTorch.blockID && player != null && !player.isSneaking())
                 {
                     world.setBlock(x, y, z, 0, 0, 0x02);
                     return 1;
                 }
 
                 x += ForgeDirection.getOrientation(side).offsetX;
                 y += ForgeDirection.getOrientation(side).offsetY;
                 z += ForgeDirection.getOrientation(side).offsetZ;
                 if (world.getBlockId(x, y, z) == 0)
                 {
                     world.setBlock(x, y, z, SimpleToolsItems.blockPlasmaTorch.blockID, 0, 0x02);
                     return -1;
                 }
                 if (world.getBlockId(x, y, z) == SimpleToolsItems.blockPlasmaTorch.blockID)
                 {
                     world.setBlock(x, y, z, 0, 0, 0x02);
                     return 1;
                 }
             }
         }
         return 0;
     }
 
     @Override
     @SuppressWarnings({ "unchecked", "rawtypes" })
     public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List)
     {
         for (int i = 0; i < names.length; i++)
         {
             par3List.add(new ItemStack(par1, 1, i));
         }
     }
 
 }
