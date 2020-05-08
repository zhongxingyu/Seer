     // Unglitch Start
     // All entities with EntityAISwimming (there doesn't seem to be a clean way to check that client-side)
     private boolean canEntitySwim() {
         return this instanceof EntityChicken
                 || this instanceof EntityCow
                 || this instanceof EntityCreeper
                 || this instanceof EntityHorse
                 || this instanceof EntityOcelot
                 || this instanceof EntityPig
                 || this instanceof EntitySheep
                 || this instanceof EntitySkeleton
                 || this instanceof EntityVillager
                 || this instanceof EntityWitch
                 || this instanceof EntityWither
                 || this instanceof EntityWolf
                 || this instanceof EntityZombie;
     }
     // --
 
     public void onLivingUpdate()
     {
         if (this.jumpTicks > 0)
         {
             --this.jumpTicks;
         }
 
         if (this.newPosRotationIncrements > 0)
         {
             double var1 = this.posX + (this.newPosX - this.posX) / (double)this.newPosRotationIncrements;
             double var3 = this.posY + (this.newPosY - this.posY) / (double)this.newPosRotationIncrements;
             double var5 = this.posZ + (this.field_110152_bk - this.posZ) / (double)this.newPosRotationIncrements;
             double var7 = MathHelper.wrapAngleTo180_double(this.newRotationYaw - (double)this.rotationYaw);
             this.rotationYaw = (float)((double)this.rotationYaw + var7 / (double)this.newPosRotationIncrements);
             this.rotationPitch = (float)((double)this.rotationPitch + (this.newRotationPitch - (double)this.rotationPitch) / (double)this.newPosRotationIncrements);
             --this.newPosRotationIncrements;
             this.setPosition(var1, var3, var5);
             this.setRotation(this.rotationYaw, this.rotationPitch);
         }
         else if (!this.isClientWorld())
         {
             this.motionX *= 0.98D;
             this.motionY *= 0.98D;
             this.motionZ *= 0.98D;
         }
 
         if (Math.abs(this.motionX) < 0.005D)
         {
             this.motionX = 0.0D;
         }
 
         if (Math.abs(this.motionY) < 0.005D)
         {
             this.motionY = 0.0D;
         }
 
         if (Math.abs(this.motionZ) < 0.005D)
         {
             this.motionZ = 0.0D;
         }
 
         this.worldObj.theProfiler.startSection("ai");
 
         if (this.isMovementBlocked())
         {
             this.isJumping = false;
             this.moveStrafing = 0.0F;
             this.moveForward = 0.0F;
             this.randomYawVelocity = 0.0F;
         }
         else if (this.isClientWorld())
         {
             if (this.isAIEnabled())
             {
                 this.worldObj.theProfiler.startSection("newAi");
                 this.updateAITasks();
                 this.worldObj.theProfiler.endSection();
             }
             else
             {
                 this.worldObj.theProfiler.startSection("oldAi");
                 this.updateEntityActionState();
                 this.worldObj.theProfiler.endSection();
                 this.rotationYawHead = this.rotationYaw;
             }
         }
 
         this.worldObj.theProfiler.endSection();
         this.worldObj.theProfiler.startSection("jump");
 
         if (this.isJumping)
         {
             if (!this.isInWater() && !this.handleLavaMovement())
             {
                 if (this.onGround && this.jumpTicks == 0)
                 {
                     this.jump();
                     this.jumpTicks = 10;
                 }
             }
             else
             {
                 this.motionY += 0.03999999910593033D;
             }
         }
         else
         {
             // Unglitch Start - Compensate for missing swimming AI client-side
            if(this.isAIEnabled() && this.canEntitySwim() && (this.isInWater() || this.handleLavaMovement())){
                 this.motionY += 0.03999999910593033D;
             }
             // --
             this.jumpTicks = 0;
         }
 
         this.worldObj.theProfiler.endSection();
         this.worldObj.theProfiler.startSection("travel");
         this.moveStrafing *= 0.98F;
         this.moveForward *= 0.98F;
         this.randomYawVelocity *= 0.9F;
         this.moveEntityWithHeading(this.moveStrafing, this.moveForward);
         this.worldObj.theProfiler.endSection();
         this.worldObj.theProfiler.startSection("push");
 
         if (!this.worldObj.isRemote)
         {
             this.collideWithNearbyEntities();
         }
 
         this.worldObj.theProfiler.endSection();
     }
 
 
     public void moveEntityWithHeading(float par1, float par2)
     {
         double var10;
 
         if (this.isInWater() && (!(this instanceof EntityPlayer) || !((EntityPlayer)this).capabilities.isFlying))
         {
             var10 = this.posY;
             this.moveFlying(par1, par2, this.isAIEnabled() ? 0.04F : 0.02F);
             this.moveEntity(this.motionX, this.motionY, this.motionZ);
             this.motionX *= 0.800000011920929D;
             this.motionY *= 0.800000011920929D;
             this.motionZ *= 0.800000011920929D;
                 this.motionY -= 0.02D;
 
             if (this.isCollidedHorizontally && this.isOffsetPositionInLiquid(this.motionX, this.motionY + 0.6000000238418579D - this.posY + var10, this.motionZ))
             {
                 this.motionY = 0.30000001192092896D;
             }
         }
         else if (this.handleLavaMovement() && (!(this instanceof EntityPlayer) || !((EntityPlayer)this).capabilities.isFlying))
         {
             var10 = this.posY;
             this.moveFlying(par1, par2, 0.02F);
             this.moveEntity(this.motionX, this.motionY, this.motionZ);
             this.motionX *= 0.5D;
             this.motionY *= 0.5D;
             this.motionZ *= 0.5D;
             this.motionY -= 0.02D;
 
             if (this.isCollidedHorizontally && this.isOffsetPositionInLiquid(this.motionX, this.motionY + 0.6000000238418579D - this.posY + var10, this.motionZ))
             {
                 this.motionY = 0.30000001192092896D;
             }
         }
         else
         {
             float var3 = 0.91F;
 
             if (this.onGround)
             {
                 var3 = 0.54600006F;
                 int var4 = this.worldObj.getBlockId(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.boundingBox.minY) - 1, MathHelper.floor_double(this.posZ));
 
                 if (var4 > 0)
                 {
                     var3 = Block.blocksList[var4].slipperiness * 0.91F;
                 }
             }
 
             float var8 = 0.16277136F / (var3 * var3 * var3);
             float var5;
 
             if (this.onGround)
             {
                 var5 = this.getAIMoveSpeed() * var8;
             }
             else
             {
                 var5 = this.jumpMovementFactor;
             }
 
             this.moveFlying(par1, par2, var5);
             var3 = 0.91F;
 
             if (this.onGround)
             {
                 var3 = 0.54600006F;
                 int var6 = this.worldObj.getBlockId(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.boundingBox.minY) - 1, MathHelper.floor_double(this.posZ));
 
                 if (var6 > 0)
                 {
                     var3 = Block.blocksList[var6].slipperiness * 0.91F;
                 }
             }
 
             if (this.isOnLadder())
             {
                 float var11 = 0.15F;
 
                 if (this.motionX < (double)(-var11))
                 {
                     this.motionX = (double)(-var11);
                 }
 
                 if (this.motionX > (double)var11)
                 {
                     this.motionX = (double)var11;
                 }
 
                 if (this.motionZ < (double)(-var11))
                 {
                     this.motionZ = (double)(-var11);
                 }
 
                 if (this.motionZ > (double)var11)
                 {
                     this.motionZ = (double)var11;
                 }
 
                 this.fallDistance = 0.0F;
 
                 if (this.motionY < -0.15D)
                 {
                     this.motionY = -0.15D;
                 }
 
                 boolean var7 = this.isSneaking() && this instanceof EntityPlayer;
 
                 if (var7 && this.motionY < 0.0D)
                 {
                     this.motionY = 0.0D;
                 }
             }
 
             this.moveEntity(this.motionX, this.motionY, this.motionZ);
 
             // Unglitch Change - make spiders ignore isCollidedHorizontal for climbing client-side
             //if (this.isCollidedHorizontally && this.isOnLadder())
             if ((this.isCollidedHorizontally || (this.worldObj.isRemote && this instanceof EntitySpider))
                 && this.isOnLadder())
             {
                 // --
                 this.motionY = 0.2D;
             }
 
             if (this.worldObj.isRemote && (!this.worldObj.blockExists((int)this.posX, 0, (int)this.posZ) || !this.worldObj.getChunkFromBlockCoords((int)this.posX, (int)this.posZ).isChunkLoaded))
             {
                 if (this.posY > 0.0D)
                 {
                     this.motionY = -0.1D;
                 }
                 else
                 {
                     this.motionY = 0.0D;
                 }
             }
             else
             {
                 this.motionY -= 0.08D;
             }
 
             this.motionY *= 0.9800000190734863D;
             this.motionX *= (double)var3;
             this.motionZ *= (double)var3;
         }
 
         this.prevLimbYaw = this.limbYaw;
         var10 = this.posX - this.prevPosX;
         double var9 = this.posZ - this.prevPosZ;
         float var12 = MathHelper.sqrt_double(var10 * var10 + var9 * var9) * 4.0F;
 
         if (var12 > 1.0F)
         {
             var12 = 1.0F;
         }
 
         this.limbYaw += (var12 - this.limbYaw) * 0.4F;
         this.limbSwing += this.limbYaw;
     }
