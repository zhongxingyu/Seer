 package me.malazath.advancedarmory.enchants;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 import com.rit.sucy.CustomEnchantment;
 
 public class EnhancedJumpEnchantment extends CustomEnchantment
 {
	static final PotionEffect enhancedJumpPotionEffect = new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 2);
 	static final Material[] ENHANCED_JUMP_ITEMS = new Material[] { Material.DIAMOND_LEGGINGS };
 	
 	/**
 	 *  Let's take the time set a few default values of our new enchantment.
 	 */
 	public EnhancedJumpEnchantment()
 	{
 		super("Enhanced Jump", ENHANCED_JUMP_ITEMS, 0);
 		
 		setMaxLevel(1);
 		setBase(900);
 	}
 	
 	/**
 	 * Add the enhanced jump effect via a simple addPotionEffect call.
 	 * 
 	 * @param Player user
 	 * @param int level
 	 */
 	@Override
 	public void applyEquipEffect(Player user, int level)
 	{
 		user.addPotionEffect(enhancedJumpPotionEffect, true);
 	}
 	
 	/**
 	 * Remove the enhanced jump effect so players don't have it after they
 	 * unequip or die with the boots.
 	 * 
 	 * @param Player user
 	 * @param int level
 	 */
 	@Override
 	public void applyUnequipEffect(Player user, int level)
 	{
 		if (user.hasPotionEffect(PotionEffectType.JUMP))
 			user.removePotionEffect(PotionEffectType.JUMP);
 	}
 
 }
