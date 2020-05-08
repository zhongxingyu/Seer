 package com.github.customentitylibrary.entities;
 
 import net.minecraft.server.v1_7_R1.EntityPigZombie;
 import net.minecraft.server.v1_7_R1.World;
 
 public class CustomPigZombie extends EntityPigZombie
 {
	private boolean newAi;

 	public CustomPigZombie(World world)
 	{
		this(world, false);
	}

	public CustomPigZombie(World world, boolean newAi)
	{
 		super(world);
		this.newAi = newAi;
 	}
 
 	/**
 	 * Returns true if the newer Entity AI code should be run
 	 */
 	@Override
 	protected boolean bk()
 	{
		return newAi;
 	}
 }
