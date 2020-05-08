 package org.andfRa.mythr.responses;
 
 import org.andfRa.mythr.player.DamageType;
 import org.andfRa.mythr.player.DerivedStats;
 import org.bukkit.Bukkit;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 
 public class DamageEffect extends ResponseEffect {
 
 	/** Attribute used for checks key. */
 	final public static String ATTRIBUTE_KEY = ResponseEffect.ATTRIBUTE_KEY;
 
 	/** Attack score modifier key. */
 	final public static String ATTACK_SCORE_MODIFIER_KEY = ResponseEffect.ATTACK_SCORE_MODIFIER_KEY;
 
 
 	/** Damage type key. */
 	final public static String DAMAGE_TYPE_KEY = "DAMAGE_TYPE";
 
 	/** Bonus damage multiplier key. */
 	final public static String BONUS_DAMAGE_MULTIPLIER_KEY = "BONUS_DAMAGE_MULTIPLIER";
 
 	
 	@Override
 	public String key()
 	 { return "DAMAGE_EFFECT"; }
 	
 	@Override
 	public boolean attackTrigger(Response response, LivingEntity lattacker, LivingEntity ldefender, DerivedStats dsattacker, DerivedStats dsdefender)
 	 {
 		// Check for bonus:
 		boolean bonus = findAttribScoreSuccess(response, dsattacker, dsdefender);
 		
 		// Type:
 		DamageType type = DamageType.match(response.getString(DAMAGE_TYPE_KEY));
 		if(type == null) return false;
 		
 		// Calculate damage:
 		double damage = dsdefender.defend(type, dsattacker);
 		if(bonus) damage*= response.getDouble(BONUS_DAMAGE_MULTIPLIER_KEY);
 		
 		// Send event:
		EntityDamageByEntityEvent bevent = new EntityDamageByEntityEvent(ldefender, lattacker, DamageCause.ENTITY_ATTACK, damage);
 		Bukkit.getServer().getPluginManager().callEvent(bevent);
 		if(bevent.isCancelled()) return false;
 		
 		// Apply damage:
 		ldefender.setLastDamageCause(bevent);
 		ldefender.damage(damage);
 		
 		return true;
 	 }
 	
 }
