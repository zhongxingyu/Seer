 package LokoiseMod.common;
 
 import net.minecraft.block.Block;
 import net.minecraft.client.Minecraft;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.monster.EntityMob;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.util.DamageSource;
 import net.minecraft.world.World;
 
 
 public class EntityLokoise extends EntityMob
 {
     public static ItemStack equippedItems[];
     public static int equippedItem;
 	public boolean hurted;
 	public static String mobName;
     
     public EntityLokoise(World world)
 
     {
         super(world);
         this.mobName = "Lokoise";
         this.texture = "/mods/TheLokoiseMod/Lokoise.png";
         this.moveSpeed = 5F;
        this.moveStrafing = 5F;
         this.equippedItem = rand.nextInt(this.equippedItems.length);
         this.hurted = false;
     }
     
     @Override
     protected Entity findPlayerToAttack()
     {
     	EntityPlayer player = worldObj.getClosestPlayerToEntity(this, 16D);
     	return player != null ? super.findPlayerToAttack() : null;
     }
     
     @Override
     public boolean attackEntityFrom(DamageSource sourceOfDamage, int i)
     {
     	hurted = true;
     	if(hurted)
     	{
     		moveSpeed = 1F;
     	}
     	return super.attackEntityFrom(sourceOfDamage, i);
     }
     
     public void goToEntity(double d, double d1, double d2)
     {
     	this.moveEntity(d, d1, d2);
     }
     
     public void smoke()
     {
     	for (int l = 0; l< 2; l++)
     	{
     		worldObj.spawnParticle("largesmoke", this.posX, this.posY, this.posZ, 0.0000D, 0.1111D, 0.0D);
     		worldObj.spawnParticle("explode", this.posX, this.posY, this.posZ, 0.0000D, 0.1111D, 0.0D);
     		worldObj.spawnParticle("largesmoke", this.posX, this.posY, this.posZ, 0.0000D, 0.1111D, 0.0D);
     		worldObj.spawnParticle("explode", this.posX, this.posY, this.posZ, 0.0000D, 0.1111D, 0.0D);
     		worldObj.spawnParticle("largesmoke", this.posX, this.posY, this.posZ, 0.0000D, 0.1111D, 0.0D);
     	}
     }
     
     @Override
     public void attackEntity(Entity entity, float f)
     {
     	this.fallDistance = -25F;
     	if(onGround)
     	{
     		this.motionY += 0.40000008565252D;
     		smoke();
     	}
     	if(handleWaterMovement())
     	{
     		this.motionY += 0.40000008565252D;
     	}
     	super.attackEntity(entity, f);
     }
 
     public int getMaxHealth()
     {
         return 30;
     }
 
     @Override
     public int getAttackStrength(Entity entity)
     {
     	return 4;
     }
     protected String getLivingSound()
     {
 		return "lokoisew";
     }
     protected String getHurtSound()
     {
 		return "lokoiseh";
     }
     
     protected String getDeathSound()
     {
 		return "lokoised";
     }
 
     protected float getSoundVolume()
     {
         return 1.5F;
     }
     public ItemStack getHeldItem() 
     {
 		return this.equippedItems[this.equippedItem];
     }
     /**
      * Returns the item ID for the item the mob drops on death.
      */
     protected int getDropItemId()
     {
         
         switch (this.rand.nextInt(9))
         {
         case 1:
             	return Main.CD_TousLesZombies.itemID;
 		case 2:
             	return Main.CD_BugDeChunks.itemID;
 		case 3:
             	return Main.CD_LeJournalDUnNaufragay.itemID;
 		case 4:
             	return Main.CD_JAimeLeCreep.itemID;
 		case 5:
             	return Main.CD_JGeekUnMax.itemID;
 		case 6:
             	return Main.CD_JSuisSeanKevin.itemID;
 		case 7:
             	return Main.CD_Acta.itemID;
 		case 8:
             	return Main.CD_JeMeGive.itemID;
 		case 0:
 			    return Main.CD_JFaitDesPellesEnDiams.itemID;
 		default:
 				return Block.dirt.blockID;
         }          
     }
     
     protected void dropRareDrop(int par1)
     {
         this.dropItem(Item.diamond.itemID, 1);
     }
     
     static {
     	equippedItems = (new ItemStack[] {
     			new ItemStack(Item.diamond, 1), 
     			new ItemStack (Item.emerald, 1),
     			new ItemStack (Item.swordDiamond, 1)
     	});
     }
     
     @Override
     public void writeEntityToNBT(NBTTagCompound nbt)
     {
     	nbt.setInteger("equippedItem", this.equippedItem);
     	super.writeEntityToNBT(nbt);
     }
     
     @Override
     public void readEntityFromNBT(NBTTagCompound nbt)
     {
     	this.equippedItem = nbt.getInteger("equippedItem");
     	super.readEntityFromNBT(nbt);
     }
     
 }
