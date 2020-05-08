 package techguy.mods.undead.client;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.EnumSet;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import techguy.mods.undead.common.ModUndeadMainRegistry;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.gui.GuiScreen;
 import net.minecraft.item.ItemStack;
 import net.minecraft.potion.Potion;
 import net.minecraft.stats.AchievementList;
 import cpw.mods.fml.common.ITickHandler;
 import cpw.mods.fml.common.TickType;
 
 public class ModUndeadClientTickHandler implements ITickHandler
 {
 	@Override
 	public void tickStart(EnumSet<TickType> type, Object... tickData) {}
 
 	@Override
 	public void tickEnd(EnumSet<TickType> type, Object... tickData)
 	{
 		if (type.equals(EnumSet.of(TickType.RENDER)))
 		{
 			onRenderTick();
 		}
 		else if (type.equals(EnumSet.of(TickType.CLIENT)))
 		{
 			GuiScreen guiscreen = Minecraft.getMinecraft().currentScreen;
 			if (guiscreen != null)
 			{
 				onTickInGUI(guiscreen);
 			} 
 			else 
 			{
 				onTickInGame();
 			}
 		}
 	}
 
 	@Override
 	public EnumSet<TickType> ticks()
 	{
 		return EnumSet.of(TickType.RENDER, TickType.CLIENT);
 		// In my testing only RENDER, CLIENT, & PLAYER did anything on the client side.
 		// Read 'cpw.mods.fml.common.TickType.java' for a full list and description of available types
 	}
 
 	@Override
 	public String getLabel() 
 	{ 
 		return null; 
 	}
 
 
 	public void onRenderTick()
 	{
 		//System.out.println("onRenderTick");
 	}
 
 	public void onTickInGUI(GuiScreen guiscreen)
 	{
 		//System.out.println("onTickInGUI");
 
 	}
 
 	public static boolean updatesEnabled;
 	boolean hasCheckedForUpdates = false;
 	boolean hasTriggeredAchieve;
 
 	public void onTickInGame()
 	{
 		checkArmour();
 		minecraft.thePlayer.triggerAchievement(ModUndeadMainRegistry.install);
 		if(updatesEnabled == true && hasCheckedForUpdates == false)
 		{
 			versionCheck();
 			hasCheckedForUpdates = true;
 		}
 
 	}
 
 	int majorVersion = 0;
 	int minorVersion = 9;
 	int build = 8;
 
 	int retrievedMajorVersion;
 	int retrievedMinorVersion;
 	int retrievedBuild;
 
 	public void versionCheck() 
 	{
 
 		try {
 			URL url = new URL("http://undeadplusversion.webs.com/index.html");
 
 			BufferedReader in = new BufferedReader(new InputStreamReader(
 					url.openStream()));
 			Pattern pat = Pattern.compile("majorVersion=\\d+");
 			Matcher matcher;
 			String str;
 			while ((str = in.readLine()) != null) {
 				if (str.contains("majorVersion")) {
 					matcher = pat.matcher(str);
 					if (matcher.find())
 						retrievedMajorVersion = Integer.parseInt(matcher.group(0)
 								.substring(13));
 
 					Pattern pat2 = Pattern.compile("minorVersion=\\d+");
 					while ((str = in.readLine()) != null) {
 						if (str.contains("minorVersion")) {
 							matcher = pat2.matcher(str);
 							if (matcher.find())
 								retrievedMinorVersion = Integer.parseInt(matcher.group(0)
 										.substring(13));
 
 							Pattern pat3 = Pattern.compile("build=\\d+");
 							while ((str = in.readLine()) != null) {
 								if (str.contains("build")) {
 									matcher = pat3.matcher(str);
 									if (matcher.find())
 										retrievedBuild = Integer.parseInt(matcher.group(0)
 												.substring(6));
 									break;
 								}
 							}
 						}
 					}
 				}
 
 			}
 
 
 
 			//System.out.println("Major:" + retrievedMajorVersion);
 			//System.out.println("Minor:" + retrievedMinorVersion);
 			//System.out.println("Build:" + retrievedBuild);
 
 			in.close();
 
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 			Minecraft.getMinecraft().thePlayer.addChatMessage("[Undead+] Update Check Failed!");
 			System.out.println("Undead+ Update Check Failure - MalformedURLException");
 		} catch (IOException e) {
 			e.printStackTrace();
 			Minecraft.getMinecraft().thePlayer.addChatMessage("[Undead+] Update Check Failed!");
 			System.out.println("Undead+ Update Check Failure - IOException");
 		} catch (NumberFormatException e){
 			e.printStackTrace();
 			Minecraft.getMinecraft().thePlayer.addChatMessage("[Undead+] Update Check Failed!");
 			System.out.println("Undead+ Update Check Failure - NumberFormatException");
 		}  catch (IndexOutOfBoundsException e){
 			e.printStackTrace();
 			Minecraft.getMinecraft().thePlayer.addChatMessage("[Undead+] Update Check Failed!");
 			System.out.println("Undead+ Update Check Failure - IndexOutOfBoundsException");
 		}
 
 		if (build < retrievedBuild || minorVersion < retrievedMinorVersion || majorVersion < retrievedMajorVersion) {   
 			Minecraft.getMinecraft().thePlayer.addChatMessage("\u00A76[Undead+] \u00A7fThere is a new version available!");
 			Minecraft.getMinecraft().thePlayer.addChatMessage("Current Version: " + majorVersion + "." + minorVersion + "." +  build);
 			Minecraft.getMinecraft().thePlayer.addChatMessage("New Version: " + retrievedMajorVersion + "." + retrievedMinorVersion + "." + retrievedBuild);
 			System.out.println("Undead+ Update Check - ret>cur");
 		}
 
 		/*if (build > retrievedBuild || minorVersion > retrievedMinorVersion || majorVersion > retrievedMajorVersion) {
 			Minecraft.getMinecraft().thePlayer.addChatMessage("\u00A7b[Undead+] \u00A7fYou have a pre-release version!");
 			Minecraft.getMinecraft().thePlayer.addChatMessage("Version: " + majorVersion + "." + minorVersion + "." + build);
 			System.out.println("Undead+ Update Check - ret<cur");
 		}*/
 
 		if (build == retrievedBuild && minorVersion == retrievedMinorVersion && majorVersion == retrievedMajorVersion) {
 			Minecraft.getMinecraft().thePlayer.addChatMessage("\u00A72[Undead+] \u00A7fUndead+ is up to date!");
 			Minecraft.getMinecraft().thePlayer.addChatMessage("Version: " + retrievedMajorVersion + "." + retrievedMinorVersion + "." + retrievedBuild);
 			System.out.println("Undead+ Update Check - ret=cur");
 		}
 
 	}
 
 	public void checkArmour()
 	{
 		ItemStack helmet = minecraft.thePlayer.inventory.armorInventory[3];
 		ItemStack chestPlate = minecraft.thePlayer.inventory.armorInventory[2];
 		ItemStack legs = minecraft.thePlayer.inventory.armorInventory[1];
 		ItemStack boots = minecraft.thePlayer.inventory.armorInventory[0];
 		if(helmet == null)
 		{
 			return;
 		}
 		if(chestPlate == null)
 		{
 			return;
 		}
 		if(legs == null)
 		{
 			return;
 		}
 		if(boots == null)
 		{
 			return;
 		}
 
 		if(helmet.itemID == ModUndeadMainRegistry.volatiteHelmet.itemID && chestPlate.itemID == ModUndeadMainRegistry.volatiteChestplate.itemID && legs.itemID == ModUndeadMainRegistry.volatiteLeggings.itemID && boots.itemID == ModUndeadMainRegistry.volatiteBoots.itemID)
 		{
 			minecraft.thePlayer.removePotionEffect(Potion.poison.id);
 			minecraft.thePlayer.removePotionEffect(Potion.hunger.id);
 		}
 	}
 
 	protected Minecraft minecraft = Minecraft.getMinecraft();
 
 }
