 package assets.levelup;
 
 import java.util.EnumSet;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.entity.EntityClientPlayerMP;
 import net.minecraft.client.settings.KeyBinding;
 
 import org.lwjgl.input.Keyboard;
 
 import cpw.mods.fml.client.registry.KeyBindingRegistry.KeyHandler;
 import cpw.mods.fml.common.TickType;
 
 public class SkillKeyHandler extends KeyHandler {
 
 	private final static KeyBinding[] keys = {new KeyBinding("LvlUpGUI", Keyboard.KEY_L)};
 	private Minecraft mc;
 	
 	public SkillKeyHandler(Minecraft minecraft)
 	{
 		super(keys, new boolean[]{false});
 		mc = minecraft;
 	}
 
 	@Override
 	public String getLabel() 
 	{
 		return "LevelUpKeys";
 	}
 
 	@Override
 	public void keyDown(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd, boolean isRepeat) 
 	{
 		if(tickEnd)
 		{
 			EntityClientPlayerMP player = mc.thePlayer;
			if(kb.keyCode==keys[0].keyCode && mc.currentScreen == null)
 			{
 				if(PlayerExtendedProperties.getPlayerClass(player)!=0)
 				{
 					player.openGui(LevelUp.instance, SkillProxy.SKILLGUI, mc.theWorld,(int) player.posX,(int) player.posY,(int) player.posZ);
 				}
 				else if(player.experienceLevel > 3 || PlayerExtendedProperties.getPlayerDeathLevel(player) > 3)
 					player.openGui(LevelUp.instance, SkillProxy.CLASSGUI, mc.theWorld,(int) player.posX,(int) player.posY,(int) player.posZ);
 			}
 		}
 	}
 
 	@Override
 	public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd) 
 	{
 	}
 
 	@Override
 	public EnumSet<TickType> ticks() 
 	{
 		return EnumSet.of(TickType.CLIENT);
 	}
 
 }
