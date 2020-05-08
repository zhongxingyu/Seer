 package com.teammetallurgy.agriculture;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.EnumAction;
 import net.minecraft.item.ItemStack;
 import net.minecraft.world.World;
 
 import com.teammetallurgy.agriculture.hunger.HungerSystem;
 
 public class SubItemFood extends SubItem
 {
 	private final int healAmount;
	private final float saturationModifier;
 	private final boolean isWolfsFavoriteMeat;
 	private boolean alwaysEdible = true;
 
 	public SubItemFood(int id, int damage, int heal)
 	{
 		super(id, damage);
 		healAmount = heal / 4;
 		saturationModifier = 0.5f;
 		isWolfsFavoriteMeat = false;
 	}
 
 	public SubItemFood(int foodid, int i, int j, float f)
 	{
 		this(foodid, i, j);
 	}
 
 	public int getHealAmount()
 	{
 		return healAmount;
 	}
 
 	/**
 	 * returns the action that specifies what animation to play when the items
 	 * is being used
 	 */
 	@Override
 	public EnumAction getItemUseAction(ItemStack par1ItemStack)
 	{
 		return EnumAction.eat;
 	}
 
 	/**
 	 * How long it takes to use or consume an item
 	 */
 	@Override
 	public int getMaxItemUseDuration(ItemStack par1ItemStack)
 	{
 		return 32;
 	}
 
 	/**
 	 * gets the saturationModifier of the ItemFood
 	 */
 	public float getSaturationModifier()
 	{
 		return saturationModifier;
 	}
 
 	/**
 	 * Whether wolves like this food (true for raw and cooked porkchop).
 	 */
 	public boolean isWolfsFavoriteMeat()
 	{
 		return isWolfsFavoriteMeat;
 	}
 
 	@Override
 	public ItemStack onEaten(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
 	{
 		--par1ItemStack.stackSize;
 		par3EntityPlayer.getFoodStats().addStats(getHealAmount(), getSaturationModifier());
 		par2World.playSoundAtEntity(par3EntityPlayer, "random.burp", 0.5F, par2World.rand.nextFloat() * 0.1F + 0.9F);
 		HungerSystem.addPoints(par3EntityPlayer, healAmount);
 		return par1ItemStack;
 	}
 
 	/**
 	 * Called whenever this item is equipped and the right mouse button is
 	 * pressed. Args: itemStack, world, entityPlayer
 	 */
 	@Override
 	public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
 	{
 		if (par3EntityPlayer.canEat(alwaysEdible))
 		{
 			par3EntityPlayer.setItemInUse(par1ItemStack, getMaxItemUseDuration(par1ItemStack));
 		} 
 
 		return par1ItemStack;
 	}
 
 	/**
 	 * Set the field 'alwaysEdible' to true, and make the food edible even if
 	 * the player don't need to eat.
 	 */
 	public SubItemFood setAlwaysEdible()
 	{
 		alwaysEdible = true;
 		return this;
 	}
 }
