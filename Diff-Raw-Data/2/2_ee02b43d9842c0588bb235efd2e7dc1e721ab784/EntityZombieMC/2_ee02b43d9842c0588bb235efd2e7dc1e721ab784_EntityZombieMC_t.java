 package net.minecraft.src;
 
 public class EntityZombieMC extends EntityZombie
 {
 	protected boolean attackMode = false;
 	protected boolean miningMode = false;
 	
 	public EntityZombieMC(World par1World)
 	{
 		super(par1World);
 		this.tasks.clearTasks();
 		this.targetTasks.clearTasks();
 		this.tasks.addTask(0, new EntityAISwimming(this));
         //this.tasks.addTask(1, new EntityAIBreakDoor(this));
         //this.tasks.addTask(2, new EntityAIAttackOnCollide(this, EntityPlayer.class, this.moveSpeed, false));
         //this.tasks.addTask(3, new EntityAIAttackOnCollide(this, EntityVillager.class, this.moveSpeed, true));
         //this.tasks.addTask(4, new EntityAIMoveTwardsRestriction(this, this.moveSpeed));
         //this.tasks.addTask(5, new EntityAIMoveThroughVillage(this, this.moveSpeed, false));
 		this.tasks.addTask(1, new EntityAITempt(this, 0.25F, mod_MLZ.defenseBeacon.shiftedIndex, false));
 		this.tasks.addTask(1, new EntityAITempt(this, 0.25F, mod_MLZ.miningBeacon.shiftedIndex, false));
 		//this.tasks.addTask(6, new EntityAIWander(this, this.moveSpeed));
         this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
         this.tasks.addTask(7, new EntityAILookIdle(this));
         this.tasks.addTask(2, new EntityAIAttackOnCollide(this, EntityMob.class, this.moveSpeed, false));
         targetTasks.addTask(2, new EntityAINearestAttackableTargetSmart(this, EntityMob.class, 16.0F, 0, true));
         
         this.texture = "mczombie.png"; 
         this.attackStrength = 10; //tough
 	}
 	
 	public void onLivingUpdate()
     {
 		super.onLivingUpdate();
 		super.extinguish();
 		
 		if(miningMode)
         {
 			if (this.rand.nextInt(3) == 0)
 			{
 	            int x = MathHelper.floor_double(this.posX - 1 + this.rand.nextDouble() * 2.0D);
 	            int y = MathHelper.floor_double(this.posY - 0.75 + this.rand.nextDouble() * 3.0D);
 	            int z = MathHelper.floor_double(this.posZ - 1 + this.rand.nextDouble() * 2.0D);
 	            int blockType = this.worldObj.getBlockId(x, y, z);
	            if(blockType > 0 && blockType < Block.blocksList.length && blockType != mod_MLZ.miningBeaconBlock.blockID && blockType != mod_MLZ.defenseBeaconBlock.blockID && blockType != Block.bedrock.blockID)
 	            {      
 		            Block.blocksList[blockType].dropBlockAsItem(this.worldObj, x, y, z, this.worldObj.getBlockMetadata(x, y, z), 0);
 		            this.worldObj.setBlockWithNotify(x, y, z, 0);
 	            }
             }
         }
     }
 	
 	public void goAttackMode()
 	{
 		attackMode = true;
 	}
 	
 	public void goMiningMode()
 	{
 		miningMode = true;
 	}
 	
 	public EntityLiving getAttackTarget()
 	{
 		if(attackMode)
 		{
 			return super.getAttackTarget();
 		}
 		else
 		{
 			return null;
 		}
 	}
 }
