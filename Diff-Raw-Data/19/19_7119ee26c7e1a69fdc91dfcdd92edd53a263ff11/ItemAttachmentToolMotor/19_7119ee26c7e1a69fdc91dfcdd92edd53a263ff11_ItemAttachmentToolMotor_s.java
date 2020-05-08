 package simpletools.common.items.tool;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.enchantment.Enchantment;
 import net.minecraft.enchantment.EnchantmentHelper;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.Icon;
 import net.minecraftforge.common.IShearable;
 import simpletools.api.IAttachment;
 import simpletools.common.SimpleTools;
 import simpletools.common.misc.SimpleToolsCreativeTab;
 
 import com.google.common.collect.BiMap;
 import com.google.common.collect.HashBiMap;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class ItemAttachmentToolMotor extends Item implements IAttachment
 {
     private Icon[] icons = new Icon[40];
     
     public ItemAttachmentToolMotor(int itemID, String name)
     {
         super(itemID);
         this.setCreativeTab(SimpleToolsCreativeTab.INSTANCE);
         this.setUnlocalizedName(name);
         this.setMaxStackSize(16);
     }
     
     @Override
     public String getToolType(ItemStack i)
     {
         switch (i.getItemDamage() % 10)
         {
             case 0:
                 return "pickaxe";
             case 1:
                 return "shovel";
             case 2:
                 return "axe";
             case 3:
                 return "sword";
             case 4:
                 return "shears";
             case 5:
             case 6:
             case 7:
             case 8:
             case 9:
             default:
                 return "";
         }
     }
     
     @Override
     public int getToolAttachmentType(ItemStack i)
     {
         return 0;
     }
     
     @Override
     public int getMinimumTier(ItemStack i)
     {
         return i.getItemDamage() / 10;
     }
     
     @Override
     public short getAttachmentUID(ItemStack i)
     {
         return ((Integer) i.getItemDamage()).shortValue();
     }
     
     @Override
     public int getAttachmentTier(ItemStack i)
     {
         return i.getItemDamage() / 10;
     }
     
     @Override
     public String getUnlocalizedName(ItemStack i)
     {
         return this.getUnlocalizedName() + "." + this.getToolType(i) + "." + this.getAttachmentTier(i);
     }
     
     @Override
     @SuppressWarnings({ "unchecked", "rawtypes" })
     public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List)
     {
         for (int i = 0; i < 4; i++)
         {
             for (int k = 0; k < 5; k++)
             {
                 par3List.add(new ItemStack(par1, 1, i * 10 + k));
             }
         }
     }
     
     @Override
     public boolean isDamageable()
     {
         return false;
     }
     
     @Override
     public float getHarvestSpeed(ItemStack i)
     {
         switch (this.getAttachmentTier(i))
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
         
         String type = this.getToolType(i);
         int toolBonus = 0;
         if (type.equals("pickaxe"))
         {
             toolBonus = 2;
         }
         else if (type.equals("shovel"))
         {
             toolBonus = 1;
         }
         else if (type.equals("axe"))
         {
             toolBonus = 3;
         }
         else if (type.equals("sword"))
         {
             toolBonus = 4;
         }
         else if (type.equals("shears"))
         {
             toolBonus = -(this.getAttachmentTier(i) + 1); // shears will hit for 0
         }
         toReturn.put(null, this.getAttachmentTier(i) + toolBonus + 1);
         return toReturn;
     }
     
     @Override
     @SideOnly(Side.CLIENT)
     public void registerIcons(IconRegister iconRegister)
     {
         ItemStack itemStack;
         for (int i = 0; i < 4; i++)
         {
             for (int k = 0; k < 5; k++)
             {
                 itemStack = new ItemStack(this.itemID, 1, i * 10 + k);
                 this.icons[i * 10 + k] = iconRegister.registerIcon(this.getUnlocalizedName(itemStack).replace("item.", SimpleTools.DOMAIN));
             }
         }
     }
     
     @Override
     @SideOnly(Side.CLIENT)
     public Icon getIconFromDamage(int par1)
     {
         return this.icons[par1];
     }
     
     @Override
     public byte onRightClick(ItemStack i, Object target, EntityPlayer player)
     {
         if (target instanceof EntityLiving && this.getToolType(i).toLowerCase().equals("shears"))
         {
             EntityLiving entity = (EntityLiving) target;
             if (entity.worldObj.isRemote)
                 return 0;
             if (entity instanceof IShearable)
             {
                 IShearable interactTarget = (IShearable) entity;
                 if (interactTarget.isShearable(i, entity.worldObj, (int) entity.posX, (int) entity.posY, (int) entity.posZ))
                 {
                     ArrayList<ItemStack> drops = interactTarget.onSheared(i, entity.worldObj, (int) entity.posX, (int) entity.posY, (int) entity.posZ,
                             EnchantmentHelper.getEnchantmentLevel(Enchantment.fortune.effectId, i));
                     
                     Random rand = new Random();
                     for (ItemStack stack : drops)
                     {
                         EntityItem ent = entity.entityDropItem(stack, 1.0F);
                         ent.motionY += rand.nextFloat() * 0.05F;
                         ent.motionX += (rand.nextFloat() - rand.nextFloat()) * 0.1F;
                         ent.motionZ += (rand.nextFloat() - rand.nextFloat()) * 0.1F;
                     }
                 }
             }
         }
         return 0;
     }
     
     @Override
     public boolean canRightClick(ItemStack i, Object target, EntityPlayer player)
     {
         if (target instanceof EntityLiving && this.getToolType(i).toLowerCase().equals("shears"))
             return (EntityLiving) target instanceof IShearable;
         return false;
     }
 }
