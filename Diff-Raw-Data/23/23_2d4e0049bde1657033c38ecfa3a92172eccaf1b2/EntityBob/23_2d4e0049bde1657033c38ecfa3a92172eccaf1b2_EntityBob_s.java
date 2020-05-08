 package net.minecraft.src;
 
 import java.util.Random;
  
 public class EntityBob extends EntityCreature
 {
 	public EntityBob(World world)
 	{
 		super(world);
 		texture = "/fantabob/bob.png";
 		attackStrength = 4;
 		hasPlayedBurnSound = false;
 		isImmuneToFire = mod_FantaBob.isImmuneToFire;
 	}
  
 	protected int getDropItemId()
 	{
 		return Block.cobblestone.blockID;
 	}
    
 	public ItemStack getHeldItem()
 	{
 		return (new ItemStack(Item.flintAndSteel));
 	}
 	
 	protected Entity findPlayerToAttack()
 	{
 		EntityJeanKevin kevin = null;
        
 		for(int i = 0; i < worldObj.loadedEntityList.size(); i++)
 		{
 			Entity target = (Entity)worldObj.loadedEntityList.get(i);
            
 			if(target instanceof EntityJeanKevin)
 			{
 				double distance = target.getDistance(posX, posY, posZ);
 				if(distance < 16 && ((EntityJeanKevin)target).canEntityBeSeen(this))
 				{
 					kevin = (EntityJeanKevin)target;
 				}
 			}
 		}
 		return kevin;
 	}
    
 	protected void attackEntity(Entity entity, float f)
 	{
 		if(attackTime <= 0 && f < 2.0F && entity.boundingBox.maxY > boundingBox.minY && entity.boundingBox.minY < boundingBox.maxY)
 		{
 			attackTime = 20;
 			World world = ModLoader.getMinecraftInstance().theWorld;
 			Material material = world.getBlockMaterial((int)entity.posX, (int)entity.posY, (int)entity.posZ);
 			if(material == Material.air && mod_FantaBob.isPyromaniac)
 			{
 				world.setBlockWithNotify((int)entity.posX, (int)entity.posY, (int)entity.posZ, Block.fire.blockID);
 				worldObj.playSoundAtEntity(this, getBurnSound(), getSoundVolume(), 1.0F);
 			}
 			else
 				attackEntityAsMob(entity);
 		}
 	}
 	
 	public boolean attackEntityAsMob(Entity entity)
     {
         return entity.attackEntityFrom(DamageSource.causeMobDamage(this), attackStrength);
     }
 
 	protected String getHurtSound() 
 	{
 		return "fantabob.bobhurt";
 	}
    
 	protected String getDeathSound() 
 	{
 		return "fantabob.bobdeath";
 	}
 	
 	protected String getLivingSound() 
 	{
 		return "fantabob.bob";
 	}
 	
 	private String getBurnSound() 
     {
 		return "fantabob.bobburn";
 	}
 	
 	protected float getSoundVolume() 
 	{
 		return 0.8F;
 	}
     
     public boolean isFollowed()
     {
     	return followed;
     }
     
     public void setFollowed(boolean followed)
     {
     	this.followed = followed;
     }
     
     public void onLivingUpdate()
     {
     	super.onLivingUpdate();
     	if(mod_FantaBob.isPyromaniac)
     	{
     		World world = ModLoader.getMinecraftInstance().theWorld;
 	    	Random rand = new Random();
 	    	int j = rand.nextInt(100);
 	    	if(j <= mod_FantaBob.pyroRate)
 	    	{
	    		Material material = world.getBlockMaterial((int)this.posX + 1, (int)this.posY - 1, (int)this.posZ);
	    		if((mod_FantaBob.canBurnWood && material == Material.wood) || (mod_FantaBob.canBurnWool && material == Material.cloth) || (mod_FantaBob.canBurnTNT && material == Material.tnt) || (mod_FantaBob.canBurnPlants && material == Material.plants) || (mod_FantaBob.canBurnLeaves && material == Material.leaves))
	        	{
	        		world.setBlockWithNotify((int)this.posX + 1, (int)this.posY, (int)this.posZ, Block.fire.blockID);
	        		if(!hasPlayedBurnSound)
	        		{
	        			worldObj.playSoundAtEntity(this, getBurnSound(), getSoundVolume(), 1.0F);
	        			hasPlayedBurnSound = true;
	        		}
	        	}
 	    	}
     	}
     }
     
     public Achievement getKillAch()
     {
     	return mod_FantaBob.killBobAch;
     }
     
     public int getMaxHealth()
 	{
 		return 10;
 	}
 	
 	public boolean attackEntityFrom(DamageSource par1DamageSource, int par2)
     {
 		super.attackEntityFrom(par1DamageSource, par2);
 		entityToAttack = null;
 		return false;
     }
 	
 	public void onDeath(DamageSource par1DamageSource)
     {
     	ModLoader.getMinecraftInstance().thePlayer.triggerAchievement(getKillAch());
     	super.onDeath(par1DamageSource);
     }
     
     private float getSoundPitch()
     {
     	return 1.0F;
     }
     
     public void playLivingSound()
     {
         String s = getLivingSound();
         if(s != null)
         {
             worldObj.playSoundAtEntity(this, s, getSoundVolume(), 1.0F);
         }
     }
 
 	private boolean followed;
 	protected int attackStrength;
 	private boolean hasPlayedBurnSound;
 }
