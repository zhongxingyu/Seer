 package com.comphenix.xp;
 
 import static org.junit.Assert.*;
 
 import java.io.File;
 
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.junit.Test;
 
 import com.comphenix.xp.expressions.ParameterProviderSet;
 import com.comphenix.xp.expressions.StandardPlayerService;
 import com.comphenix.xp.messages.ChannelProvider;
 import com.comphenix.xp.parser.ParsingException;
 import com.comphenix.xp.rewards.RewardProvider;
 import com.comphenix.xp.rewards.xp.RewardExperience;
 import com.comphenix.xp.rewards.xp.RewardVirtual;
 
 public class ConfigurationLoaderTest implements Debugger {
 
 	private static String path = "E:\\Games\\Minecraft\\1.3 Server\\plugins\\ExperienceMod";
 	
 	@Test
 	public void test() throws ParsingException {
 		RewardProvider provider = new RewardProvider();
 		provider.register(new RewardExperience());
 		provider.register(new RewardVirtual());
 		
 		ParameterProviderSet parameterProviders = new ParameterProviderSet();
		parameterProviders.registerPlayer(new StandardPlayerService());
 
 		File root = new File(path);
 		ConfigurationLoader loader = new ConfigurationLoader(root, this, provider, new ChannelProvider(), parameterProviders);
 		
 		YamlConfiguration presetConfig = YamlConfiguration.loadConfiguration(new File(root, "presets.yml"));
 		Presets presets = new Presets(presetConfig, loader, 10, this, null);
 
 		// Now, get the two configurations
 		Configuration world = presets.getConfiguration(null, "world");
 		Configuration nether = presets.getConfiguration(null, "world_creative_flat");
 		
 		assertSame(world, nether);
 	}
 
 	@Override
 	public boolean isDebugEnabled() {
 		return false;
 	}
 
 	@Override
 	public void printDebug(Object sender, String message, Object... params) {
 		// Do nothing
 	}
 
 	@Override
 	public void printWarning(Object sender, String message, Object... params) {
 		fail(String.format(sender + ": " + message, params));
 	}
 }
