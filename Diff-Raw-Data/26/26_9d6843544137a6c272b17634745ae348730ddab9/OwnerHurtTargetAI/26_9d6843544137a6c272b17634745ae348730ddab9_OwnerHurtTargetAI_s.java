 package com.spacechase0.minecraft.usefulpets.ai;
 
 import com.spacechase0.minecraft.usefulpets.entity.PetEntity;
 
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.entity.ai.EntityAITarget;
 import net.minecraft.entity.passive.EntityTameable;
 
 public class OwnerHurtTargetAI extends EntityAITarget
 {
 	PetEntity theEntityTameable;
     EntityLivingBase theTarget;
     private int field_142050_e;
 
     public OwnerHurtTargetAI(PetEntity par1EntityTameable)
     {
         super(par1EntityTameable, false);
         this.theEntityTameable = par1EntityTameable;
         this.setMutexBits(1);
     }
 
     /**
      * Returns whether the EntityAIBase should begin execution.
      */
     public boolean shouldExecute()
     {
         if ( false )
         {
             return false;
         }
         else
         {
             EntityLivingBase entitylivingbase = (EntityLivingBase) this.theEntityTameable.getOwner();
 
             if (entitylivingbase == null)
             {
                 return false;
             }
             else
             {
                 this.theTarget = entitylivingbase.func_110144_aD();
                 int i = entitylivingbase.func_142013_aG();
                 return i != this.field_142050_e && this.isSuitableTarget(this.theTarget, false) && this.theEntityTameable.isValidTarget(this.theTarget);
             }
         }
     }
 
     /**
      * Execute a one shot task or start executing a continuous task
      */
     public void startExecuting()
     {
         this.taskOwner.setAttackTarget(this.theTarget);
         EntityLivingBase entitylivingbase = (EntityLivingBase) this.theEntityTameable.getOwner();
 
         if (entitylivingbase != null)
         {
             this.field_142050_e = entitylivingbase.func_142013_aG();
         }
 
         super.startExecuting();
     }
 }
