 package com.comphenix.xp;
 
 import static org.junit.Assert.*;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.EntityType;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.junit.Test;
 
 import com.comphenix.xp.lookup.MobQuery;
 import com.comphenix.xp.messages.ChannelProvider;
 import com.comphenix.xp.messages.Message;
 import com.comphenix.xp.parser.ActionParser;
 import com.comphenix.xp.parser.text.ItemNameParser;
 import com.comphenix.xp.parser.text.ItemParser;
 import com.comphenix.xp.parser.text.MobParser;
 import com.comphenix.xp.rewards.RewardProvider;
 import com.comphenix.xp.rewards.RewardTypes;
 import com.google.common.collect.Lists;
 
 public class ConfigurationTest {
 
 	@Test
 	public void testMerging() {
 		
 		Debugger debugger = new MockDebugger();
 		
 		RewardProvider provider = new RewardProvider();
 		provider.register(new MockRewardable(RewardTypes.EXPERIENCE));
 		provider.register(new MockRewardable(RewardTypes.VIRTUAL));
 		provider.register(new MockRewardable(RewardTypes.ECONOMY));
 		provider.setDefaultReward(RewardTypes.EXPERIENCE);
 		String def = "EXPERIENCE";
 		
 		// Reset parser
 		ActionParser.setCurrentID(0);
 		
 		Configuration first = createConfig(
 				"multiplier: 1\n" +
 				"mobs:\n" + 
 				"  ?:\n" +
 				"    experience: 5\n" +
 				"    economy: 1\n" + 
 				"    message: 'hei'\n" + 
 				"    channels: [mining, general]", debugger, provider);
 		
 		Configuration second = createConfig(
 				"multiplier: 1\n" +
 				"mobs:\n" + 
				"  zombie: 0\n", debugger, provider);
 		
 		Configuration result = Configuration.fromMultiple(Lists.newArrayList(first, second), debugger);
 		
 		MobQuery queryBlace = MobQuery.fromAny(EntityType.BLAZE, DamageCause.ENTITY_ATTACK);
 		MobQuery queryZombie = MobQuery.fromAny(EntityType.ZOMBIE, DamageCause.ENTITY_ATTACK);
 		
 		Action blaceAction = new Action();
 		blaceAction.addReward(def, new Range(5));
 		blaceAction.addReward("ECONOMY", new Range(1));
 		blaceAction.setMessage(new Message("hei", "mining", "general"));
 		
 		Action zombieAction = new Action(def, new Range(0));
 		zombieAction.setId(1);
 		
 		assertEquals(blaceAction, result.getExperienceDrop().get(queryBlace));
 		assertEquals(zombieAction, result.getExperienceDrop().get(queryZombie));
 	}
 	
 	// Load configuration from text
 	private Configuration createConfig(String text, Debugger debugger, RewardProvider provider) {
 		Configuration config = new Configuration(debugger, provider, new ChannelProvider());
 		ItemNameParser nameParser = new ItemNameParser();
 		
 		config.setItemParser(new ItemParser(nameParser));
 		config.setMobParser(new MobParser());
 		config.setActionTypes(ActionTypes.Default());
 		config.loadFromConfig(fromText(text));
 		return config;
 	}
 	
 	private YamlConfiguration fromText(String text) {
 		try {
 			InputStream buffer = new ByteArrayInputStream(text.getBytes("UTF-8"));
 			return YamlConfiguration.loadConfiguration(buffer);
 		} catch (UnsupportedEncodingException e) {
 			throw new RuntimeException("No UTF-8 installed.");
 		}
 	}
 }
