 package com.spacechase0.minecraft.usefulpets.entity;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.spacechase0.minecraft.usefulpets.UsefulPets;
 import com.spacechase0.minecraft.usefulpets.ai.FollowOwnerAI;
 import com.spacechase0.minecraft.usefulpets.ai.OwnerHurtTargetAI;
 import com.spacechase0.minecraft.usefulpets.ai.SitAI;
 import com.spacechase0.minecraft.usefulpets.ai.TargetHurtOwnerAI;
 import com.spacechase0.minecraft.usefulpets.pet.*;
 import com.spacechase0.minecraft.usefulpets.pet.skill.AttackSkill;
 import com.spacechase0.minecraft.usefulpets.pet.skill.FoodSkill;
 import com.spacechase0.minecraft.usefulpets.pet.skill.Skill;
 
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityAgeable;
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.entity.EntityOwnable;
 import net.minecraft.entity.SharedMonsterAttributes;
 import net.minecraft.entity.ai.EntityAIAttackOnCollide;
 import net.minecraft.entity.ai.EntityAIFollowOwner;
 import net.minecraft.entity.ai.EntityAIHurtByTarget;
 import net.minecraft.entity.ai.EntityAILeapAtTarget;
 import net.minecraft.entity.ai.EntityAILookIdle;
 import net.minecraft.entity.ai.EntityAIOwnerHurtByTarget;
 import net.minecraft.entity.ai.EntityAIOwnerHurtTarget;
 import net.minecraft.entity.ai.EntityAISit;
 import net.minecraft.entity.ai.EntityAISwimming;
 import net.minecraft.entity.ai.EntityAIWander;
 import net.minecraft.entity.ai.EntityAIWatchClosest;
 import net.minecraft.entity.ai.attributes.AttributeInstance;
 import net.minecraft.entity.monster.EntityCreeper;
 import net.minecraft.entity.passive.EntityAnimal;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.projectile.EntityArrow;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemFood;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagIntArray;
 import net.minecraft.util.DamageSource;
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
         
         // TEMP
         tasks.addTask(3, new EntityAILeapAtTarget(this, 0.4F));
         tasks.addTask(4, new EntityAIAttackOnCollide(this, 1.0D, true));
         
         tasks.addTask( 5, new FollowOwnerAI( this, 1.0D, 10.0F, 3.5F ) );
         tasks.addTask( 7, new EntityAIWander(this, 1.0D));
         tasks.addTask( 9, new EntityAIWatchClosest( this, EntityPlayer.class, 9.0F ) );
         targetTasks.addTask(1, new TargetHurtOwnerAI( this ) );
         targetTasks.addTask(2, new OwnerHurtTargetAI( this ) );
         targetTasks.addTask(3, new EntityAIHurtByTarget( this, true ) );
 	}
 	
 	public int getLevel()
 	{
 		return dataWatcher.getWatchableObjectInt( DATA_LEVEL );
 	}
 	
 	public void setLevel( int level )
 	{
 		dataWatcher.updateObject( DATA_LEVEL, level );
 	}
 	
 	public int getFreeSkillPoints()
 	{
 		return dataWatcher.getWatchableObjectInt( DATA_FREE_POINTS );
 	}
 	
 	public void setFreeSkillPoints( int points )
 	{
 		dataWatcher.updateObject( DATA_FREE_POINTS, points );
 	}
 	
 	public boolean hasSkill( int id )
 	{
 		//System.out.println("has: "+id+" "+skills.contains(id));
 		return skills.contains( id );
 	}
 	
 	public boolean hasSkillRequirements( int skillId )
 	{
 		Skill skill = Skill.forId( skillId );
 		if ( skill.levelReq > getLevel() )
 		{
 			return false;
 		}
 		
 		if ( skill.skillReqs == null )
 		{
 			return true;
 		}
 		
 		for ( int is = 0; is < skill.skillReqs.length; ++is )
 		{
 			Skill parent = Skill.forId( skill.skillReqs[ is ] );
 			if ( !hasSkill( parent.id ) )
 			{
 				return false;
 			}
 		}
 		
 		return true;
 	}
 	
 	public void addSkill( int id )
 	{
 		if ( hasSkill( id ) || getFreeSkillPoints() < 1 || !hasSkillRequirements( id ) )
 		{
 			return;
 		}
 		
 		skills.add( id );
 		setFreeSkillPoints( getFreeSkillPoints() - 1 );
 		dataWatcher.updateObject( DATA_SKILLS, getSkillsStack() );
 	}
 	
 	public void removeSkill( int id )
 	{
 		if ( hasSkill( id ) && !type.defaultSkills.contains( id ) )
 		{
 			for ( Skill skill : Skill.skills.values() )
 			{
 				for ( int i = 0; skill.skillReqs != null && i < skill.skillReqs.length; ++i )
 				{
 					int reqId = skill.skillReqs[ i ];
 					if ( reqId == id )
 					{
 						removeSkill( skill.id );
 						break;
 					}
 				}
 			}
 			
 			skills.remove( new Integer( id ) );
 			setFreeSkillPoints( getFreeSkillPoints() + 1 );
 		}
 		dataWatcher.updateObject( DATA_SKILLS, getSkillsStack() );
 	}
 	
 	private ItemStack getSkillsStack()
 	{
 		if ( skills == null || skills.size() == 0 )
 		{
 			return new ItemStack( Item.arrow );
 		}
 		
 		int[] theSkills = new int[ skills.size() ];
 		for ( int i = 0; i < skills.size(); ++i )
 		{
 			theSkills[ i ] = skills.get( i );
 		}
 		
 		ItemStack stack = new ItemStack( Item.stick );
 		stack.setTagCompound( new NBTTagCompound() );
 		stack.getTagCompound().setIntArray( "Skills", theSkills );
 		
 		return stack;
 	}
 	
 	// TODO: Test me
 	public void resetSkills()
 	{
 		for ( int id : type.defaultSkills )
 		{
 			skills.remove( id );
 		}
 		
 		setFreeSkillPoints( getFreeSkillPoints() + skills.size() );
 		setPetType( type ); // Resets skills to default
 	}
 	
 	public PetType getPetType()
 	{
 		return type;
 	}
 	
 	public void setPetType( PetType theType )
 	{
 		type = theType;
 		setSize( type.sizeX, type.sizeY );
 		skills.clear();
 		skills.addAll( type.defaultSkills );
 		dataWatcher.updateObject( DATA_TYPE, type.name );
 		dataWatcher.updateObject( DATA_SKILLS, getSkillsStack() );
 	}
 	
 	public boolean isSitting()
 	{
 		return ( dataWatcher.getWatchableObjectByte( DATA_SITTING ) != 0 );
 	}
 	
 	public void setSitting( boolean sitting )
 	{
 		dataWatcher.updateObject( DATA_SITTING, ( byte )( sitting ? 1 : 0 ) );
 	}
 	
 	public float getHunger()
 	{
 		return dataWatcher.func_111145_d( DATA_HUNGER );
 	}
 	
 	public void useHunger( float amount )
 	{
 		float satDiff = Math.min( amount, saturation );
 		saturation -= satDiff;
 		if ( satDiff != amount )
 		{
 			setHunger( getHunger() - ( amount - satDiff ) );
 		}
 	}
 	
 	public void setHunger( float hunger )
 	{
 		if ( hunger < 0.f )
 		{
 			hunger = 0.f;
 		}
 		else if ( hunger > MAX_HUNGER )
 		{
 			hunger = MAX_HUNGER;
 		}
 		
 		dataWatcher.updateObject( DATA_HUNGER, hunger );
 	}
 	
 	public float getSaturation()
 	{
 		return saturation;
 	}
 	
 	public void setSaturation( float theSaturation )
 	{
 		saturation = theSaturation;
 	}
 	
 	public boolean isValidTarget( EntityLivingBase target )
 	{
 		return !( target instanceof EntityCreeper );
 	}
 	
 	// Entity
     @Override
     public void writeEntityToNBT( NBTTagCompound tag)
     {
         super.writeEntityToNBT( tag );
         
         tag.setString( "Owner", getOwnerName() );
         tag.setString( "Type", getPetType().name );
         tag.setInteger( "Level", getLevel() );
         tag.setInteger( "FreeSkillPoints", getFreeSkillPoints() );
         int[] theSkills = new int[ skills.size() ];
         for ( int i = 0; i < skills.size(); ++i )
         {
         	theSkills[ i ] = skills.get( i );
         }
         tag.setTag( "Skills", new NBTTagIntArray( "Skills", theSkills ) );
         
         tag.setBoolean( "Sitting", isSitting() );
         tag.setFloat( "Hunger", getHunger() );
         tag.setFloat( "Saturation", getSaturation() );
     }
 
     @Override
     public void readEntityFromNBT( NBTTagCompound tag )
     {
         super.readEntityFromNBT( tag );
         
         setOwnerName( tag.getString( "Owner" ) );
         setPetType( PetType.forName( tag.getString( "Type" ) ) );
         setLevel( tag.getInteger( "Level" ) );
         setFreeSkillPoints( tag.getInteger( "FreeSkillPoints" ) );
         skills.clear();
         int[] theSkills = ( ( NBTTagIntArray ) tag.getTag( "Skills" ) ).intArray;
         for ( int id : theSkills )
         {
         	skills.add( id );
         }
         dataWatcher.updateObject( DATA_SKILLS, getSkillsStack() );
         
         setSitting( tag.getBoolean( "Sitting" ) );
         setHunger( tag.getFloat( "Hunger" ) );
         setSaturation( tag.getFloat( "Saturation" ) );
     }
     
     @Override
     protected void entityInit()
     {
         super.entityInit();
         
         dataWatcher.addObject( DATA_OWNER, "Player" );
         dataWatcher.addObject( DATA_TYPE, "cat" );
         dataWatcher.addObject( DATA_SITTING, ( byte ) 0 );
         dataWatcher.addObject( DATA_HUNGER, 20.f );
         dataWatcher.addObject( DATA_LEVEL, 1 );
         dataWatcher.addObject( DATA_FREE_POINTS, 1 );
         dataWatcher.addObject( DATA_SKILLS, getSkillsStack() );
     }
     
     @Override
     public void onUpdate()
     {
     	if ( !hasSkill( Skill.COMBAT.id ) )
     	{
     		setTarget( null );
     	}
     	
     	super.onUpdate();
     	
     	isDead = false;
     	deathTime = 0;
     	
     	if ( posY < -4.f )
     	{
     		setPosition( posX, -4.f, posZ );
     	}
     	
     	if ( worldObj.isRemote && ++syncTimer >= 20 )
     	{
     		ownerName = dataWatcher.getWatchableObjectString( DATA_OWNER );
         	type = PetType.forName( dataWatcher.getWatchableObjectString( DATA_TYPE ) );
 
         	skills.clear();
         	ItemStack skillStack = dataWatcher.getWatchableObjectItemStack( DATA_SKILLS );
         	if ( skillStack.itemID == Item.stick.itemID )
         	{
             	int[] newSkills = skillStack.getTagCompound().getIntArray( "Skills" );
             	for ( int id : newSkills )
             	{
             		skills.add( id );
             	}
         	}
         	
         	syncTimer = 0;
     	}
     	else
     	{
     		if ( getHunger() >= MAX_HUNGER / 2 && func_110143_aJ() < func_110138_aP() )
     		{
     			if ( ++regenTicks == 35 )
     			{
     				setEntityHealth( func_110143_aJ() + 1 );
     				useHunger( 0.25f );
     				regenTicks = 0;
     			}
     		}
     	}
     }
     
     @Override
     public void setDead()
     {
     }
 	
 	@Override
     public boolean attackEntityFrom( DamageSource source, float damage )
     {
 		if ( source.getEntity() == getOwner() )
 		{
 			return false;
 		}
 		
         Entity entity = source.getEntity();
         aiSit.setSitting( false );
         setSitting( false );
 
         if (entity != null && !(entity instanceof EntityPlayer) && !(entity instanceof EntityArrow))
         {
         	damage = (damage + 1.0F) / 2.0F;
         }
 
         return super.attackEntityFrom(source, damage);
     }
     
     // EntityLivingBase, EntityLiving
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
     
     @Override
     public void onDeath( DamageSource source )
     {
     }
     
     @Override
     public boolean isEntityAlive()
     {
     	return true;
     }
     
     @Override
     protected boolean isMovementBlocked()
     {
        return false;
     }
     
     @Override
     public boolean interact( EntityPlayer player )
     {
     	if ( player.isSneaking() )
     	{
     		UsefulPets.proxy.setPendingPetForGui( this );
     		player.openGui( UsefulPets.instance, UsefulPets.PET_GUI_ID, worldObj, 0, 0, 0 );
     		return false;
     	}
     	
     	if ( worldObj.isRemote )
     	{
     		return false;
     	}
     	
     	ItemStack held = player.getHeldItem();
     	if ( held != null && held.getItem() instanceof ItemFood )
     	{
     		ItemFood food = ( ItemFood ) held.getItem();
     		
     		boolean canEat = false;
     		for ( int id : skills )
     		{
     			Skill skill = Skill.forId( id );
     			if ( !( skill instanceof FoodSkill ) )
     			{
     				continue;
     			}
     			FoodSkill foodSkill = ( FoodSkill ) skill;
     			
     			if ( foodSkill.type.doesMatch( type, held ) )
     			{
     				canEat = true;
     				break;
     			}
     		}
     		
     		if ( canEat )
     		{
     			setHunger( getHunger() + food.getHealAmount() );
     			setSaturation( getSaturation() + food.getSaturationModifier() );
         		return true;
     		}
     	}
     	else
     	{
     		setSitting( !isSitting() );
     	}
     	
     	return false;
     }
 
     @Override
     public boolean attackEntityAsMob( Entity entity )
     {
     	int damage = 0;
     	for ( int id : skills )
     	{
     		Skill skill = Skill.forId( id );
     		if ( !( skill instanceof AttackSkill ) )
     		{
     			continue;
     		}
     		AttackSkill attack = ( AttackSkill ) skill;
     		
     		damage += attack.damage;
     	}
     	
         return entity.attackEntityFrom( DamageSource.causeMobDamage( this ), (float) damage );
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
 	private List< Integer > skills = new ArrayList< Integer >();
 	
 	// State stuff
 	private float saturation;
 	private int regenTicks = 0;
 	private int syncTimer = 0;
 	
 	// AI stuff
 	private SitAI aiSit = new SitAI( this );
 	
 	public static final int DATA_OWNER = 20;
 	public static final int DATA_TYPE = 21;
 	public static final int DATA_SITTING = 22;
 	public static final int DATA_HUNGER = 23;
 	public static final int DATA_LEVEL = 24;
 	public static final int DATA_FREE_POINTS = 25;
 	public static final int DATA_SKILLS = 26;
 	
 	public static final float MAX_HUNGER = 20;
 }
