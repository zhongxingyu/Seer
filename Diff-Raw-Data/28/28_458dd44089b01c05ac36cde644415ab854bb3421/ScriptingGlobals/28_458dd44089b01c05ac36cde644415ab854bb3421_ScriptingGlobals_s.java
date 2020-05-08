 package com.vbitz.MinecraftScript;
 
 import java.util.HashMap;
 
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.src.EntityPlayerMP;
 import net.minecraft.src.ItemStack;
 
 import org.mozilla.javascript.Function;
 
 import com.vbitz.MinecraftScript.exceptions.ScriptErrorException;
 
 import cpw.mods.fml.common.registry.GameRegistry;
 
 public class ScriptingGlobals {
 	
 	private static HashMap<String, Integer> Difficultys = new HashMap<String, Integer>();
 	
 	static {
 		Difficultys.put("peaceful", 0);
 		Difficultys.put("easy", 1);
 		Difficultys.put("normal", 2);
 		Difficultys.put("hard", 3);
 	}
 	
 	public static Vector3f newVectorJS(double x, double y, double z) {
 		return new Vector3f(x, y, z);
 	}
 	
 	public static MinecraftScriptPlayerAPI getScriptRunnerJS() {
 		return new MinecraftScriptPlayerAPI(ScriptingManager.getScriptRunner());
 	}
 	
 	public static MinecraftScriptWorldAPI getWorldJS() {
 		if (ScriptingManager.getScriptRunner() == null) {
 			return null;
 		} else {
 			return new MinecraftScriptWorldAPI(ScriptingManager.getScriptRunner().worldObj, ScriptingManager.getScriptRunner());
 		}
 	}
 	
 	public static MinecraftScriptScriptedBlockAPI getBlockJS(int i) throws ScriptErrorException {
 		if (!MinecraftScriptMod.getInstance().getClientSideEnabled()) {
 			throw new ScriptErrorException("Client side needs to be enabled for ScriptedBlocks to work");
 		}
 		if (i > 128) {
 			return null;
 		} else {
 			return new MinecraftScriptScriptedBlockAPI(i);
 		}
 	}
 	
 	public static void logJS(Object obj) {
		MinecraftScriptMod.getLogger().info(obj.toString());
 	}
 	
 	public static void sendChatJS(String chat) {
		MinecraftServer.getServer().sendChatToPlayer(chat);
 	}
 	
 	public static int getItemIdJS(String name) {
 		return MinecraftItemStore.getBlockByName(name);
 	}
 	
 	public static MinecraftScriptPlayerAPI playerJS(String nick) {
         EntityPlayerMP ply = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(nick);
         
         if (ply == null) {
 			return null;
 		} else {
 			return new MinecraftScriptPlayerAPI(ply);
 		}
 	}
 	
 	public static void registerCommandJS(String name, Function command) {
 		MinecraftScriptCommandManager.addCommand(name, command);
 	}
 	
 	public static void registerWebpointJS(String name, Function func) {
 		JSHTTPServer.addFunction(name, func);
 	}
 	
 	public static void setDifficultyJS(String diff) throws ScriptErrorException {
 		if (Difficultys.containsKey(diff)) {
 			MinecraftServer.getServer().setDifficultyForAllWorlds(Difficultys.get(diff));
 		} else {
 			throw new ScriptErrorException("Difficulty not found");
 		}
 	}
 	
 	public static void addSmeltingRecipeJS(int input, int output, int xp) {
 		GameRegistry.addSmelting(input, new ItemStack(output, 1, 0), xp);
 	}
 }
