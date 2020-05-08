 package com.spacechase0.minecraft.usefulpets.entity;
 
 import com.spacechase0.minecraft.usefulpets.ai.FollowOwnerAI;
 import com.spacechase0.minecraft.usefulpets.ai.SitAI;
 import com.spacechase0.minecraft.usefulpets.pet.*;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityAgeable;
 import net.minecraft.entity.EntityOwnable;
 import net.minecraft.entity.SharedMonsterAttributes;
 import net.minecraft.entity.ai.EntityAIFollowOwner;
 import net.minecraft.entity.ai.EntityAISit;
 import net.minecraft.entity.ai.EntityAISwimming;
 import net.minecraft.entity.ai.EntityAIWander;
 import net.minecraft.entity.ai.EntityAIWatchClosest;
 import net.minecraft.entity.passive.EntityAnimal;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.world.World;
 
 public class PetEntity extends EntityAnimal implements EntityOwnable
 {
 	// MINE! :P
 	public PetEntity( World world )
 	{
 		super( world );
 		
 		setSize( type.sizeX, type.sizeY );
 		
 		getNavigator().setAvoidsWater( true );
         tasks.addTask( 1, new EntityAISwimming( this ) );
         tasks.addTask( 2, aiSit );
         tasks.addTask( 5, new FollowOwnerAI( this, 1.0D, 10.0F, 3.5F ) );
         tasks.addTask( 7, new EntityAIWander(this, 1.0D));
         tasks.addTask( 9, new EntityAIWatchClosest( this, EntityPlayer.class, 9.0F ) );
 	}
 	
 	public int getLevel()
 	{
 		return level;
 	}
 	
 	public void setLevel( int theLevel )
 	{
 		level = theLevel;
 	}
 	
 	public PetType getPetType()
 	{
 		return type;
 	}
 	
 	public void setPetType( PetType theType )
 	{
 		type = theType;
 		setSize( type.sizeX, type.sizeY );
 		dataWatcher.updateObject( DATA_TYPE, type.name );
 	}
 	
 	public boolean isSitting()
 	{
 		return ( dataWatcher.getWatchableObjectByte( DATA_SITTING ) != 0 );
 	}
 	
 	public void setSitting( boolean sitting )
 	{
 		dataWatcher.updateObject( DATA_SITTING, ( byte )( sitting ? 1 : 0 ) );
 	}
 	
 	// Entity
     public void writeEntityToNBT( NBTTagCompound tag)
     {
         super.writeEntityToNBT( tag );
         
         tag.setString( "Owner", getOwnerName() );
         tag.setString( "Type", getPetType().name );
         tag.setInteger( "Level", getLevel() );
         
         tag.setBoolean( "Sitting", isSitting() );
     }
     
     public void readEntityFromNBT( NBTTagCompound tag )
     {
         super.readEntityFromNBT( tag );
         
         setOwnerName( tag.getString( "Owner" ) );
         setPetType( PetType.forName( tag.getString( "Type" ) ) );
         setLevel( tag.getInteger( "Level" ) );
         
         setSitting( tag.getBoolean( "Sitting" ) );
     }
     
     protected void entityInit()
     {
         super.entityInit();
         
         dataWatcher.addObject( DATA_OWNER, "Player" );
        dataWatcher.addObject( DATA_TYPE, "dog" );
         dataWatcher.addObject( DATA_SITTING, ( byte ) 0 );
     }
     
     // EntityLivingBase, EntityLiving
     @Override
     public void onEntityUpdate()
     {
     	if ( worldObj.isRemote )
     	{
     		ownerName = dataWatcher.getWatchableObjectString( DATA_OWNER );
         	type = PetType.forName( dataWatcher.getWatchableObjectString( DATA_TYPE ) );
     	}
     }
     
     @Override
     protected boolean canDespawn()
     {
     	return false;
     }
 
     @Override
     public boolean isAIEnabled()
     {
         return true;
     }
 
     @Override
     protected String getLivingSound()
     {
         return type.getLivingSound();
     }
     
     @Override
     protected String getHurtSound()
     {
         return type.getHurtSound();
     }
 
     @Override
     protected float getSoundVolume()
     {
         return 0.4F;
     }
     
     @Override
     public String getEntityName()
     {
         return hasCustomNameTag() ? getCustomNameTag() : ( "entity.pet." + type.name );
     }
     
     @Override
     protected void func_110147_ax()
     {
         super.func_110147_ax();
 
         func_110148_a( SharedMonsterAttributes.field_111267_a ).func_111128_a( 20 );
         func_110148_a( SharedMonsterAttributes.field_111263_d ).func_111128_a( 0.3 );
     }
 
 	// EntityAnimal
 	@Override
 	public EntityAgeable createChild( EntityAgeable entity )
 	{
 		// TODO
 		return null;
 	}
 	
 	// EntityOwnable (+some)
 	@Override
 	public String getOwnerName()
 	{
 		return ownerName;
 	}
 	
 	public void setOwnerName( String theOwnerName )
 	{
 		ownerName = theOwnerName;
 		dataWatcher.updateObject( DATA_OWNER, ownerName );
 	}
 
 	@Override
 	public Entity getOwner()
 	{
         return worldObj.getPlayerEntityByName( getOwnerName() );
 	}
 	
 	// Variables
 	// Pet info
 	private String ownerName = "Player";
 	private PetType type = PetType.CAT;
 	private int level = 1;
 	
 	// AI stuff
 	private SitAI aiSit = new SitAI( this );
 	
 	public static final int DATA_OWNER = 20;
 	public static final int DATA_TYPE = 21;
 	public static final int DATA_SITTING = 22;
 }
