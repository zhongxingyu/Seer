 package me.botsko.dhmcdeath;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.configuration.file.FileConfiguration;
 
 public class DeathConfig {
 	
 	
 	/**
 	 * 
 	 * @param plugin
 	 */
 	public static FileConfiguration init( DhmcDeath plugin ){
 		
 		FileConfiguration config = plugin.getConfig();
 		
 		// other configs
 		config.addDefault("debug", false );
 		
 		// Base config
 		config.addDefault("messages.allow_cross_world", false );
 		config.addDefault("messages.hear_distance", 50 );
 		config.addDefault("messages.log_deaths", true );
 		
 		config.addDefault("allow_deathpoint_tp_on_pvp", false );
 
 		// Set initial methods as enabled
 		config.addDefault("messages.custom.enabled", true );
 		config.addDefault("messages.cactus.enabled", true );
 		config.addDefault("messages.drowning.enabled", true );
 		config.addDefault("messages.fall.enabled", true );
 		config.addDefault("messages.fire.enabled", true );
 		config.addDefault("messages.lava.enabled", true );
 		config.addDefault("messages.lightning.enabled", true );
 		config.addDefault("messages.mob.enabled", true );
 		config.addDefault("messages.poison.enabled", true );
 		config.addDefault("messages.pvp.enabled", true );
 		config.addDefault("messages.starvation.enabled", true );
 		config.addDefault("messages.suffocation.enabled", true );
 		config.addDefault("messages.suicide.enabled", true );
 		config.addDefault("messages.tnt.enabled", true );
 		config.addDefault("messages.void.enabled", true );
 		config.addDefault("messages.default.enabled", true );
 		
 		List<String> cactus=new ArrayList<String>();
 		cactus.add("&3%d &cdied from a cactus. We know, that's lame.");
 		cactus.add("&3%d &cpoked a cactus, but the cactus poked back.");
 		config.addDefault("messages.cactus.messages", cactus );
 		
 		List<String> drowning=new ArrayList<String>();
 		drowning.add("&3%d &cdrowned.");
 		drowning.add("&3%d &cis swimming with the fishes.");
 		drowning.add("&3%d &ctook a long walk off a short pier.");
 		config.addDefault("messages.drowning.messages", drowning );
 		
 		List<String> fall=new ArrayList<String>();
 		fall.add("&3%d &cfell to his ultimate demise.");
 		fall.add("&3%d &chit the ground too hard.");
 		fall.add("&3%d &cperished from a brutal fall.");
 		fall.add("&3%d &csuccumbed to gravity.");
 		fall.add("&3%d &cfinally experienced terminal velocity.");
 		fall.add("&3%d &cwent skydiving, forgot the parachute.");
 		fall.add("&cWe'll hold a moment of silence while we laugh at your falling death, &3%d.");
 		fall.add("&cAttempting a high-wire stunt yet again, &3%d &cslipped, and died.");
 		fall.add("&cSomehow tree tops are immune to gravity. &3%d &cis not.");
 		fall.add("&cNice going &3%d, &cyou've fallen. You're in a group that includes sand, and gravel - the losers of three.");
 		fall.add("&cWe're here today to mourn the loss of &3%d&c. He is survived by his Nyan Cat and Creeper statues.");
 		fall.add("&cLike everything in life, &3%d &cchose the most average, unexciting, unadventerous death - falling. Whoopie.");
 		fall.add("&cOh man that was hard fall &3%d&c! You ok? &3%d&c? How many fingers dude? Um, dude? Oh sh...");
 		fall.add("&3%d &chad a whoopsie-daisy!");
 		fall.add("&3%d &cwas testing gravity. Yup, still works.");
 		fall.add("&cAlthough &3%d's &cbody lies on the ground somewhere, the question stands. Will it blend?");
 		config.addDefault("messages.fall.messages", fall );
 		
 		List<String> fire=new ArrayList<String>();
 		fire.add("&3%d &cburned to death.");
 		fire.add("&3%d &cforgot how to stop, drop and roll.");
 		fire.add("&3%d &cspontaneiously combusted, or possibly walked into fire.");
 		fire.add("&3%d &cbecame a human torch. Not a very long-lasting one either.");
 		fire.add("&cNot only did you burn up &3%d&c, but you may have started a forest fire. Nice going.");
 		fire.add("&cYou are not a replacement for coal, &3%d&c. I'm not sure that even death can teach you that lesson.");
 		fire.add("&cTaking himself out of the gene pool for us, &3%d &cburned to death. Good job!");
 		config.addDefault("messages.fire.messages", fire );
 		
 		List<String> lava=new ArrayList<String>();
 		lava.add("&3%d &cwas killed by lava.");
 		lava.add("&3%d &cbecame obsidian.");
 		lava.add("&3%d &ctook a bath in a lake of fire.");
 		lava.add("&3%d &clost an entire inventory to lava. He died too, but man, loosing your stuff's a bummer!");
 		lava.add("&cI told you not to dig straight down &3%d&c. Now look what happened.");
 		lava.add("&cLook &3%d&c, I'm sorry I boiled you to death. I just wanted a friend. No one likes me. - Your Best Buddy, Lava.");
 		lava.add("&cThen &3%d &csaid \"Take my picture in front of this pit of boiling, killer lava.\"");
 		config.addDefault("messages.lava.messages", lava );
 		
 		List<String> lightning=new ArrayList<String>();
 		lightning.add("%d was struck with a bolt of inspiration. Wait, nevermind. Lightning.");
 		config.addDefault("messages.lightning.messages", lightning );
 		
 		List<String> mob=new ArrayList<String>();
 		mob.add("&3%d &cwas ravaged by &3%a&c.");
 		mob.add("&3%d &cdied after encountering the fierce &3%a&c.");
 		mob.add("&3%d &cwas killed by an angry &3%a&c.");
 		mob.add("&cIt was a horrible death for &3%d &c- ravaged by a &3%a&c.");
 		mob.add("&cDinner time for &3%a&c. Cooked pork for the main course, &3%d &cfor dessert.");
 		mob.add("&3%d &cwent off into the woods alone and shall never return. Until respawn.");
 		mob.add("&cWhile hunting, &3%d &cwas unaware that a &3%a &cwas hunting him. Rest in pieces.");
 		mob.add("&cWe have unconfirmed reports that &3%d &cwas attacked by an &3%a.");
 		mob.add("&cLook &3%d&c, I'm sorry I killed you. I just wanted a friend. No one likes me. - Your Best Buddy, &3%a&c.");
 		mob.add("&cSomething killed &3%d&c!");
 		mob.add("&cDear &3%d&c, good luck finding your stuff. - &3%a&c.");
 		mob.add("&3%d &cwas ravaged by &3%a&c.");
 		config.addDefault("messages.mob.messages", mob );
 		
 		// MOB SPECIFIC TYPES
 		
 			List<String> zombie=new ArrayList<String>();
 			zombie.add("&cHaving not seen the plethora of zombie movies, &3%d &cwas amazingly unaware of how to escape.");
 			zombie.add("&cPoor &3%d &c- that zombie only wanted a hug! That's why his arms were stretched out.");
 			config.addDefault("messages.mob.zombie.messages", zombie );
 		
 			List<String> creeper=new ArrayList<String>();
 			creeper.add("&3%d &cwas creeper bombed.");
 			creeper.add("&3%d &chugged a creeper.");
 			creeper.add("&cSorry you died &3%d&c, a creeper's gonna creep!");
 			creeper.add("&3%d &cwas testing a new creeper-proof suit. It didn't work.");
 			creeper.add("&3%d &cwas not involved in any explosion, nor are we able to confirm the existence of the \"creeper\". Move along.");
 			creeper.add("&cDue to the laws of physics, the sound of a creeper explosion only reached &3%d &cafter he died from it.");
 			creeper.add("&cHell hath no fury like a creeper scorned. We drink to thy untimely end, lord &3%d&c.");
 			creeper.add("&cI'm sorry &3%d&c, that's the only birthday gift Creepers know how to give. ;(");
 			config.addDefault("messages.mob.creeper.messages", creeper );
 			
 		// posion
 		
 		List<String> pvp=new ArrayList<String>();
 		pvp.add("&3%d &cwas just murdered by &3%a&c, using &3%i&c.");
 		pvp.add("&3%d &cdied, by &3%a's %i.");
 		pvp.add("&3%a &ckilled &3%d &cwielding &3%i");
		pvp.add("&cYou think it was &3%a who &ckilled &3%d&c? Nope, Chuck Testa.");
 		pvp.add("&cIt was a bitter end for &3%d&c, but &3%a &cwon victoriously.");
 		pvp.add("&cEmbarrassingly, &3%d &cdied of fright before &3%a &ccould even raise his weapon.");
 		pvp.add("&3%a &cstruck the mighty blow and ended &3%d&c.");
 		pvp.add("&3%d &cnever saw &3%a &ccoming.");
 		pvp.add("&3%a &cdelivered the fatal blow on &3%d&c.");
 		pvp.add("&3%d's &cinventory now belongs to &3%a&c.");
 		pvp.add("&3%a &ctaught &3%d &cthe true meaning of PVP.");
 		pvp.add("&cIn the case of &3%d &cv. &3%a&c, &3%d &cis suing on charges of voluntary manslaughter. This judge finds &3%a &cguilty of BEING AWESOME!");
 		pvp.add("&cWhat is this, like death number ten for &3%d&c? Ask &3%a&c.");
 		config.addDefault("messages.pvp.messages", pvp );
 		
 		List<String> starvation=new ArrayList<String>();
 		starvation.add("&3%d &cstarved to death.");
 		starvation.add("&3%d &cstarved to death. Because food is *so* hard to find.");
 		config.addDefault("messages.starvation.messages", starvation );
 		
 		List<String> suffocation=new ArrayList<String>();
 		suffocation.add("&3%d &csuffocated.");
 		config.addDefault("messages.suffocation.messages",suffocation );
 		
 		List<String> suicide=new ArrayList<String>();
 		suicide.add("&3%d &ckilled himself.");
 		suicide.add("&3%d &cended it all. Goodbye cruel world!");
 		config.addDefault("messages.suicide.messages", suicide );
 		
 		List<String> tnt=new ArrayList<String>();
 		tnt.add("&3%d &cblew up.");
 		tnt.add("&3%d &cwas blown to tiny bits.");
 		config.addDefault("messages.tnt.messages", tnt );
 		
 		List<String> thevoid=new ArrayList<String>();
 		thevoid.add("&3%d &cceased to exist. Thanks void!");
 		thevoid.add("&3%d &cpassed the event horizon.");
 		config.addDefault("messages.void.messages", thevoid );
 		
 		List<String> defaultmsg=new ArrayList<String>();
 		defaultmsg.add("&3%d &cpossibly died - we're looking into it.");
 		defaultmsg.add("&cNothing happened. &3%d &cis totally ok. Why are you asking?");
 		config.addDefault("messages.default.messages",defaultmsg );
 		
 		
 		
 		// Copy defaults
 		config.options().copyDefaults(true);
 		
 		// save the defaults/config
 		plugin.saveConfig();
 		
 		return config;
 		
 	}
 }
