 package loecraftpack.logic.handlers;
 
 import java.util.EnumSet;
 
 import loecraftpack.LoECraftPack;
 import loecraftpack.enums.Race;
 import loecraftpack.gui.DialogGUI;
 import loecraftpack.logic.DialogLogic;
 import loecraftpack.packethandling.PacketHelper;
 import loecraftpack.packethandling.PacketIds;
 import loecraftpack.ponies.spells.projectiles.Fireball;
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.gui.GuiScreen;
 import net.minecraft.client.settings.KeyBinding;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.world.World;
 
 import org.lwjgl.input.Keyboard;
 
 import cpw.mods.fml.client.registry.KeyBindingRegistry.KeyHandler;
 import cpw.mods.fml.common.TickType;
 import cpw.mods.fml.common.network.PacketDispatcher;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 @SideOnly(Side.CLIENT)
 public class KeysHandler extends KeyHandler
 {
 	static KeyBinding renderMonolithKeybind = new KeyBinding("RenderMonolith", Keyboard.KEY_F12);
 	static KeyBinding dialogShortcut = new KeyBinding("DialogShortcut", Keyboard.KEY_F);
 	public static KeyBinding jump = new KeyBinding("Jump", Keyboard.KEY_SPACE);
 	public static boolean renderMonolith = false;
 	
 	public KeysHandler()
 	{
 		super(new KeyBinding[] {renderMonolithKeybind, dialogShortcut, jump}, new boolean[] {false, false, true});
 	}
 
 	@Override
 	public String getLabel()
 	{
 		return "LoECraft Key Handler";
 	}
 
 	@Override
 	public void keyDown(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd, boolean isRepeat)
 	{
		if (tickEnd)
 		{
 			if (kb.equals(jump))
 			{
 				EntityPlayer player = Minecraft.getMinecraft().thePlayer;
 				if (LoECraftPack.StatHandler.isRace(player, Race.Earth) && player.motionY > 0)
 					player.motionY *= 1.175f;
 			}
 			else if (kb.equals(renderMonolithKeybind))
 				renderMonolith = !renderMonolith;
 			else if (kb.equals(dialogShortcut))
 			{
 				if (Minecraft.getMinecraft().currentScreen instanceof DialogGUI)
 				{
 					if (DialogLogic.getButtonOrdinal() == 0)
 					{
 						if (!DialogLogic.ChangeMessage(true))
 							Minecraft.getMinecraft().thePlayer.sendChatMessage("next");
 					}
 					else
 					{
 						Minecraft.getMinecraft().thePlayer.sendChatMessage("done");
 						Minecraft.getMinecraft().displayGuiScreen((GuiScreen)null);
 					}
 				}
				else
 				{
 					PacketDispatcher.sendPacketToServer(PacketHelper.Make("loecraftpack", PacketIds.fireball));
 				}
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
