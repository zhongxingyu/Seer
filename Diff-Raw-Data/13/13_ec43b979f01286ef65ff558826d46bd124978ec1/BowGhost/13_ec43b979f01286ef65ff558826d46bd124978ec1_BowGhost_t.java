 package ubahRPG;
 
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.enchantment.Enchantment;
 import net.minecraft.enchantment.EnchantmentHelper;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.projectile.EntityArrow;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.Icon;
 import net.minecraft.world.World;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.event.entity.player.ArrowLooseEvent;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class BowGhost extends BowBasic{
 
 	
 	
 	public BowGhost(int par1)
 	{
 	super(par1);
 	this.setMaxDamage(1);
 	}
 	
 	
 	
 	
 @SideOnly(Side.CLIENT)
     
    private Icon[] Texture;
     public void registerIcons(IconRegister iconRegister)
     
     {
     	
              itemIcon = iconRegister.registerIcon(UbahRPG.modid + ":" + "bow/" + (this.getUnlocalizedName().substring(5)) + "_0");
             Texture = new Icon[4];
              for (int N = 0; N < 4; N++)
              {
                      this.Texture[N] = iconRegister.registerIcon(UbahRPG.modid + ":" + "bow/" + (this.getUnlocalizedName().substring(5)) + "_" + N);
                      
              }
     }
     public Icon getIcon(ItemStack stack, int renderPass, EntityPlayer player, ItemStack usingItem, int useRemaining)
     {
              if(player.getItemInUse() == null) return this.itemIcon;
              int time = stack.getMaxItemUseDuration() - useRemaining;
              if (time >= 18)
              {
                      return Texture[3];
              }
              else if (time > 13)
              {
                      return Texture[2];
              }
              else if (time > 0)
              {
                      return Texture[1];
              }             
              return Texture[0];
              }
     
     public void onPlayerStoppedUsing(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer, int par4)
     {
     	int j = this.getMaxItemUseDuration(par1ItemStack) - par4;
 
         ArrowLooseEvent event = new ArrowLooseEvent(par3EntityPlayer, par1ItemStack, j);
         MinecraftForge.EVENT_BUS.post(event);
         if (event.isCanceled())
         {
             return;
         }
         j = event.charge;
 
         boolean flag = par3EntityPlayer.capabilities.isCreativeMode || EnchantmentHelper.getEnchantmentLevel(Enchantment.infinity.effectId, par1ItemStack) > 0;
 
         if (flag || par3EntityPlayer.inventory.hasItem(Item.arrow.itemID))
         {
             float f = (float)j / 20.0F;
             f = (f * f + f * 2.0F) / 3.0F;
 
             if ((double)f < 0.1D)
             {
                 return;
             }
 
             if (f > 1.0F)
             {
                 f = 1.0F;
             }
 
             EntityArrow entityarrow = new EntityArrow(par2World, par3EntityPlayer, f * 2.0F);
 
             if (f == 1.0F)
             {
                 entityarrow.setIsCritical(true);
             }
 
             int k = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, par1ItemStack);
 
             if (k > 0)
             {
                 entityarrow.setDamage(entityarrow.getDamage() + (double)k * 0.5D + 0.5D);
             }
 
             int l = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, par1ItemStack);
 
             if (l > 0)
             {
                 entityarrow.setKnockbackStrength(l);
             }
 
             if (EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, par1ItemStack) > 0)
             {
                 entityarrow.setFire(100);
             }
 
             par1ItemStack.damageItem(0, par3EntityPlayer);
             par2World.playSoundAtEntity(par3EntityPlayer, "random.bow", 1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + f * 0.5F);
 
             if (flag)
             {
                 entityarrow.canBePickedUp = 2;
             }
             else
             {
                 par3EntityPlayer.inventory.consumeInventoryItem(Item.arrow.itemID);
             }
 
             if (!par2World.isRemote)
             {
                 par2World.spawnEntityInWorld(entityarrow);
             }
         }
     }
     
     
     
     }
 	
 	
 
