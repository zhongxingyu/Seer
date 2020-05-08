 package me.thegeekyguy101.TurtleMod.entity;
 
import java.util.Random;

 import net.minecraft.entity.EntityAgeable;
 import net.minecraft.entity.SharedMonsterAttributes;
 import net.minecraft.entity.ai.EntityAIAvoidEntity;
 import net.minecraft.entity.ai.EntityAISwimming;
 import net.minecraft.entity.ai.EntityAIWander;
 import net.minecraft.entity.ai.EntityAIWatchClosest;
 import net.minecraft.entity.passive.EntityAnimal;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.world.World;
 
 public class EntityHelloGuy extends EntityAnimal {
 
 	public EntityHelloGuy(World par1World) {
 		super(par1World);
 		this.setSize(0.6F, 1.8F);
 		this.tasks.addTask(0, new EntityAISwimming(this));
 		this.tasks.addTask(1, new EntityAIAvoidEntity(this, EntityMineTurtle.class, 8.0F, 0.6D, 0.6D));
 		this.tasks.addTask(2, new EntityAIWatchClosest(this, EntityPlayer.class, 3.0F, 1.0F));
 		this.tasks.addTask(3, new EntityAIWander(this, 0.6D));
 	}
 	
 	protected void func_110147_ax()
     {
         super.func_110147_ax();
         this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a(0.5D);
     }
 	
 	/**
      * Returns true if the newer Entity AI code should be run
      */
     public boolean isAIEnabled()
     {
         return true;
     }
     
     /**
      * Returns the sound this mob makes while it's alive.
      */
   	public String getLivingSound()
     {
         return "turtlemod:mob.helloGuy.hg";
     }
 
     /**
      * Returns the sound this mob makes when it is hurt.
      */
     protected String getHurtSound()
     {
        return "turtlemod:mob.helloGuy.hgh";
     }
 
     /**
      * Returns the sound this mob makes on death.
      */
     protected String getDeathSound()
     {
        return "turtlemod:mob.helloGuy.hgd";
     }
     
 	@Override
 	public EntityAgeable createChild(EntityAgeable entityageable) {
 		return null;
 	}
 
 }
