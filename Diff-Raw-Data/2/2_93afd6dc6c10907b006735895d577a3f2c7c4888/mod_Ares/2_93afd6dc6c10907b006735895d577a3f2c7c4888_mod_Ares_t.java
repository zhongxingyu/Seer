 //You may not release this source under any condition, it must be linked to this page
 //You may recompile and publish as long as skipperguy12 and Guru_Fraser are given credit
 //You may not claim this to be your own
 //You may not remove these comments
 package net.minecraft.src;
 
 import java.util.ArrayList;
 import java.awt.Color;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.math.*;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 
 import org.lwjgl.input.Keyboard;
 
 import net.minecraft.client.Minecraft;
 
 public class mod_Ares extends BaseMod
 {
 	private String fps;
 
 	public static final String serverDomain = "oc.tc";
 	private String IP;
 	private boolean showGUI;
 	public static World world;
 	protected String username = "Not_Found";
 	protected Minecraft mc = Minecraft.getMinecraft();
 	public int height;
 
 
 	public KeyBinding F6 = new KeyBinding("gui", Keyboard.KEY_F6);
 
 
 	@MLProp(name="showFPS", info="true = Show FPS in Gui, false = Doesn't show it.")
 	public static String showFPS = "true";
 
 	@MLProp(name="showKills", info="true = Show Kills in Gui, false = Doesn't show it.")
 	public static String showKills = "true";
 
 	@MLProp(name="showDeaths", info="true = Show Deaths in Gui, false = Doesn't show it.")
 	public static String showDeaths = "true";
 
 	@MLProp(name="showKilled", info="true = Show Times killed via PVP not PVE in Gui, false = Doesn't show it")
 	public static String showKilled = "true";
 
 	@MLProp(name="showServer", info="true = Show what server you are on in Gui, false = Doesn't show it.")
 	public static String showServer = "true";
 
 	@MLProp(name="showTeam", info="true = Show what team your on in Gui, false = Doesn't show it.")
 	public static String showTeam = "true";
 
 	@MLProp(name="showKD", info="true = Show your Kill Death Ratio in Gui, false = Doesn't show it.")
 	public static String showKD = "true";
 
 	@MLProp(name="showKK", info="true = Show your Kill Killed Ratio in Gui, false = Doesn't show it.")
 	public static String showKK = "true";
 
 	@MLProp(name="showFriends", info="true = Show Friends Online in Gui, false = Doesn't show it.")
 	public static String showFriends = "true";
 
 	@MLProp(name="showMap", info="true = Show the current map you are playing in Gui, false = Doesn't show it.")
 	public static String showMap = "true";
 
 	@MLProp(name="showStreak", info="true = Show your kill streak in Gui, false = Doesn't show it.")
 	public static String showStreak = "true";
 	EntityPlayer player = null;
 
 	// TODO: Check for captured wools
 
 
 	@Override
 	public String getVersion()
 	{
 		return "1.4.7";
 	}
 
 	@Override
 	public void load()
 	{
 		ModLoader.setInGUIHook(this, true, false);
 		ModLoader.setInGameHook(this, true, false);
 
 		this.showGUI = true;
 		new AresVariablesHandler(true);
 		ModLoader.registerKey(this, F6, false);
 		ModLoader.addLocalization("F6","gui");
 	}
 
 
 	public void clientChat(String var1)
 	{
 		Minecraft mc = ModLoader.getMinecraftInstance();
 		World world = mc.theWorld; //get the current world.
 		EntityPlayer player = mc.thePlayer; //get the player entity.
 		String message = StringUtils.stripControlCodes(var1);
 		if (AresVariablesHandler.isPlayingAres())
 		{
 			username = mc.thePlayer.username.toString();
 			if(username == null)
 			{
 				return;
 			}
 			if (!message.startsWith("<") && message.contains(" joined the game")) 
 			{
 				String name;
 				message = message.replace(" joined the game", "");
 				if(message.contains("["))
 					name = message.split(" ")[1];
 				else 
 					name = message;
 				
 				AresVariablesHandler.addFriend(name);
 			}
 
 			else if(!message.startsWith("<") && message.equalsIgnoreCase("welcome to project ares"))
 			{
 				AresVariablesHandler.setMap(AresCustomMethods.methods.getMap());
 			}
 			else if(!message.startsWith("<") && message.contains("Now playing"))
 			{
 				message = message.replace("Now playing ", "");
 				AresVariablesHandler.setMap(message.split(" by ")[0]);
 			}
 			else if (!message.startsWith("<") && message.contains("left the game"))
 			{
 				String name;
 				message = message.replace(" left the game", "");
 				if(message.contains("["))
 				{
 					name = message.split(" ")[1];
 				}
 				else
 				{
 					name = message;
 				}
 				if (AresVariablesHandler.isFriend(name))
 				{
 					AresVariablesHandler.removeFriend(name);
 				}
 			}
 			else if (!message.startsWith("<") && message.startsWith(username + " was"))
 			{
 				AresVariablesHandler.addKilled(1);
 				AresVariablesHandler.addDeaths(1);
 				AresVariablesHandler.setKillstreak(0);
 			}
			else if (!message.startsWith("<") && message.startsWith(username) && !message.contains("scored"))
 			{
 				AresVariablesHandler.setKillstreak(0);
 				AresVariablesHandler.addDeaths(1);
 			}
 
 			else if(message.startsWith("<") && message.contains("was shot by " + username) || message.contains("was blown up by " + username) || message.contains("was slain by " + username))
 			{
 				AresVariablesHandler.addKills(1);
 				AresVariablesHandler.addKillstreak(1);
 			}
 			else if (!message.startsWith("<") && var1.contains("You joined the"))
 			{
 				AresVariablesHandler.setKills(0);
 				AresVariablesHandler.setKilled(0);
 				AresVariablesHandler.setDeaths(0);
 				AresVariablesHandler.setKillstreak(0);
 				AresVariablesHandler.setTeam(message.replace("You joined the ", ""));
 			}
 
 			else if (!message.startsWith("<") && message.toLowerCase().contains("cycling to") && message.contains("1 second"))
 			{
 				player.addChatMessage("-------------- Final Stats --------------");
 				player.addChatMessage("-------------- Kills: " + AresVariablesHandler.getKills() + "--------------");
 				player.addChatMessage("-------------- Deaths: " + AresVariablesHandler.getDeaths() + "--------------");
 				player.addChatMessage("-------------- K/D: " + AresCustomMethods.methods.getKD() + "--------------");
 				AresVariablesHandler.setKills(0);
 				AresVariablesHandler.setKilled(0);
 				AresVariablesHandler.setDeaths(0);
 				AresVariablesHandler.setKillstreak(0);
 				AresVariablesHandler.setTeam("Observers");
 			}
 		}
 	}
 
 	public boolean onTickInGame(float time, Minecraft mc)
 	{
 
 		world = mc.theWorld;
 
 		this.fps = mc.debug.split(",")[0];
 		height = 2;
 		if(showGUI)
 		{
 			if(this.showFPS.toString().equals("true"))
 			{
 				mc.fontRenderer.drawStringWithShadow(this.fps, 2, height, 0xffff);
 				height += 8;
 
 			}
 		}
 
 		if (AresVariablesHandler.isPlayingAres() == true && showGUI == true)
 		{
 			//Server display
 			if(this.showServer.toString().equals("true"))
 			{
 				mc.fontRenderer.drawStringWithShadow("Server: \u00A76"
 						+ AresVariablesHandler.getServer(), 2, height, 16777215);
 				height +=8;
 			}
 
 			//Team display
 			if(this.showTeam.toString().equals("true"))
 			{
 
 
 				if(AresVariablesHandler.getTeam().equalsIgnoreCase("red team"))
 				{
 					mc.fontRenderer.drawStringWithShadow("Team: "
 							+ AresVariablesHandler.getTeam(), 2, height, 0x990000);
 					height +=8;
 				}
 				else if(AresVariablesHandler.getTeam().equalsIgnoreCase("blue team"))
 				{
 					mc.fontRenderer.drawStringWithShadow("Team: "
 							+ AresVariablesHandler.getTeam(), 2, height, 0x0033FF);
 					height +=8;
 				}
 				else if(AresVariablesHandler.getTeam().equalsIgnoreCase("purple team"))
 				{
 					mc.fontRenderer.drawStringWithShadow("Team: "
 							+ AresVariablesHandler.getTeam(), 2, height, 0x9933CC);
 					height +=8;
 				}
 				else if(AresVariablesHandler.getTeam().equalsIgnoreCase("cyan team"))
 				{
 					mc.fontRenderer.drawStringWithShadow("Team: "
 							+ AresVariablesHandler.getTeam(), 2, height, 0x00FFFF);	
 					height +=8;
 				}
 				else if(AresVariablesHandler.getTeam().equalsIgnoreCase("lime team"))
 				{
 					mc.fontRenderer.drawStringWithShadow("Team: "
 							+ AresVariablesHandler.getTeam(), 2, height, 0x00FF00);	
 					height +=8;
 				}
 				else if(AresVariablesHandler.getTeam().equalsIgnoreCase("yellow team"))
 				{
 					mc.fontRenderer.drawStringWithShadow("Team: "
 							+ AresVariablesHandler.getTeam(), 2, height, 0xFFFF00);
 					height +=8;
 				}
 				else if(AresVariablesHandler.getTeam().equalsIgnoreCase("orange team"))
 				{
 					mc.fontRenderer.drawStringWithShadow("Team: "
 							+ AresVariablesHandler.getTeam(), 2, height, 0x006600);
 				}
 				else if(AresVariablesHandler.getTeam().equalsIgnoreCase("orange team"))
 				{
 					mc.fontRenderer.drawStringWithShadow("Team: "
 							+ AresVariablesHandler.getTeam(), 2, height, 0xFF9900);
 					height +=8;
 				}
 				else if(AresVariablesHandler.getTeam().equalsIgnoreCase("Observers"))
 				{
 					mc.fontRenderer.drawStringWithShadow("Team: "
 							+ AresVariablesHandler.getTeam(), 2, 18, 0x00FFFF);
 					height +=8;
 				}
 			}
 			// Friend display:
 			if(this.showFriends.toString().equals("true"))
 			{
 				mc.fontRenderer.drawStringWithShadow("Friends Online: \u00A73"
 						+ AresVariablesHandler.getFriends(), 2, height, 16777215);
 				height +=8;
 			}
 
 
 			// Map fetcher:
 			if(this.showMap.toString().equals("true"))
 			{
 				if (AresVariablesHandler.getMap() != null)
 				{
 					mc.fontRenderer.drawStringWithShadow(
 							"Current Map: \u00A7d" + AresVariablesHandler.getMap(), 2, height, 16777215);
 					height +=8;
 				}
 				else
 				{
 					AresVariablesHandler.setMap("Fetching...");
 					mc.fontRenderer.drawStringWithShadow(
 							"Current Map: \u00A78" + AresVariablesHandler.getMap(), 2, height, 16777215);
 					height +=8;
 				}
 			}
 			// Kills, deaths, K/D and KK
 			if(this.showKD.equals("true"))
 			{
 				mc.fontRenderer.drawStringWithShadow("K/D: \u00A73" + AresCustomMethods.methods.getKD(), 2, height,
 						16777215);
 				height +=8;
 			}
 			if(this.showKK.equals("true"))
 			{
 				mc.fontRenderer.drawStringWithShadow("K/K: \u00A73" + AresCustomMethods.methods.getKK(), 2, height,
 						16777215);
 				height +=8;
 			}
 
 			if(this.showKills.toString().equals("true"))
 			{
 				mc.fontRenderer.drawStringWithShadow("Kills: \u00A7a" + AresVariablesHandler.getKills(), 2, height,
 						16777215);
 				height +=8;
 			}
 			if(this.showDeaths.toString().equals("true"))
 			{
 				mc.fontRenderer.drawStringWithShadow("Deaths: \u00A74" + AresVariablesHandler.getDeaths(), 2, height,
 						16777215);
 				height +=8;
 			}
 
 			if(this.showStreak.toString().equals("true"))
 			{
 
 				//Kill Streak display
 				mc.fontRenderer.drawStringWithShadow("Current Killstreak: \u00A75"
 						+ AresVariablesHandler.getKillstreak(), 2, height, 16777215);
 				height +=8;
 			}
 		}
 		return true;
 	}
 
 	public void clientConnect(NetClientHandler var1)
 	{
 		AresVariablesHandler.setTeam("Observers");
 		System.out.println("Client successfully connected to "
 				+ var1.getNetManager().getSocketAddress().toString());
 
 		if (var1.getNetManager().getSocketAddress().toString().contains(serverDomain))
 		{
 			//What happens if logs into project ares
 			this.showGUI = true;
 			System.out.println("Connected to: " + var1.getNetManager().getSocketAddress().toString() + "Ares mod activated!");
 			AresVariablesHandler.setTeam("Observers");
 			AresVariablesHandler.isPlayingAres(true);
 			AresVariablesHandler.setServer(AresCustomMethods.methods.getServer(var1.getNetManager().getSocketAddress().toString()));
 		}
 		else
 		{
 			this.showGUI = false;
 		}
 
 	}
 
 
 	public void onDisconnect(NetClientHandler handler) 
 	{
 		AresVariablesHandler.setTeam("Observers");
 		if (AresVariablesHandler.isPlayingAres())
 			AresVariablesHandler.isPlayingAres(false);
 		
 		this.showGUI = false;
 		AresVariablesHandler.setTeam("Observers");
 		AresVariablesHandler.setKillstreak(0);
 		AresVariablesHandler.setMap("Attempting to fetch map...");
 	}
 
 	public void keyboardEvent(KeyBinding keybinding)
 	{
 		Minecraft mc = ModLoader.getMinecraftInstance();
 		World world = mc.theWorld;
 		EntityPlayerSP player = mc.thePlayer;
 		if(!(mc.currentScreen instanceof GuiChat))
 		{
 			if (keybinding == F6)
 			{
 				if(showGUI==true)
 				{
 					showGUI = false;
 				}
 				else
 				{
 					showGUI = true;
 				}
 			}
 		}
 	}
 }
