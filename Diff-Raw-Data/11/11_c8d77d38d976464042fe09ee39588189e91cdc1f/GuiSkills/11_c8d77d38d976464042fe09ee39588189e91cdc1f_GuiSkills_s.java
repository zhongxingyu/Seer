 package assets.levelup;
 
 import net.minecraft.client.gui.GuiButton;
 import net.minecraft.client.gui.GuiScreen;
 import net.minecraft.network.packet.Packet;
 import cpw.mods.fml.common.network.PacketDispatcher;
 
 public class GuiSkills extends GuiScreen
 {
     private boolean closedWithButton;
     private final static int offset = 80;
     private final static String toolTips[] =
     {
         "Every 1: + chance: 2x ore drops", "Every 1: + chance: random crit",
         "Every 1: + chance: 'super block'", "Every 1: + chance: more wood drops",
         "Every 1: + chance: 2x smelt yield", "Every 1: + arrow speed", "Every 1: + sprint speed",
         "Every 1: + chance: 2x cook yield", "Every 1: + sneak speed", "Every 1: + crop grow speed",
         "Every 1: + chance: getting a bite", "Every 1: + chance: digging up loot"
     };
     private final static String toolTips2[] =
     {
         "Every 5: + mining speed", "Every 5: + damage with items",
         "Every 5: - damage from mobs", "Every 5: + chopping speed",
         "Every 5: + smelting speed", "Every 5: + bow drawback speed",
         "Every 5: - falling damage", "Every 5: + cooking speed",
         "Every 5: - mob sight range with sneak", "Every 5: + chance: 2x wheat drops",
         "Every 5: + chance: fish up loot", "Every 5: + chance: flint from gravel"
     };
     private int[] skills = new int[ClassBonus.skillNames.length];
 	private int[] skillsPrev = null;
 	byte cl = -1;
 
    private boolean mouseOverButton(GuiButton guibutton, int i, int j)
    {
        return guibutton.mousePressed(mc, i, j);
    }

     private void updateSkillList()
     {
     	for(int i=0; i< skills.length; i++)
         {
         	skills[i] = PlayerExtendedProperties.getSkillFromIndex(mc.thePlayer, i);
         }
     	if(skillsPrev==null)
     	{
     		skillsPrev = new int[skills.length];
             System.arraycopy(skills, 0, skillsPrev, 0, skills.length);
     	}
     }
     
     @Override
     public void initGui()
     {
         closedWithButton = false;
         buttonList.clear();
         buttonList.add(new GuiButton(0, width / 2 + 96, height / 6 + 168, 96, 20, "Done"));
         buttonList.add(new GuiButton(100, width / 2 - 192, height / 6 + 168, 96, 20, "Cancel"));
        
     	for(int index=0;index<6;index++)
     	{
     		buttonList.add(new GuiButton(1+index, (width / 2 + 44) - offset, 15+32*index, 20, 20, "+"));
     		buttonList.add(new GuiButton(7+index, width / 2 + 44 + offset, 15+32*index, 20, 20, "+"));
     		buttonList.add(new GuiButton(21+index, width / 2 - 64 - offset, 15+32*index, 20, 20, "-"));
             buttonList.add(new GuiButton(27+index, (width / 2 - 64) + offset, 15+32*index, 20, 20, "-"));
     	}
     }
     @Override
     public void actionPerformed(GuiButton guibutton)
     {
         if (guibutton.id == 0)
         {
             closedWithButton = true;
             mc.displayGuiScreen((GuiScreen)null);
             mc.setIngameFocus();
         }
         else if (guibutton.id == 100)
         {
             closedWithButton = false;
             mc.displayGuiScreen((GuiScreen)null);
             mc.setIngameFocus();
         }
         else if (guibutton.id < 21)	
         {
         	if(skills[skills.length-1] > 0 && skills[guibutton.id - 1] < 50)
         	{
         		Packet packet = SkillPacketHandler.getPacket("LEVELUPSKILLS", mc.thePlayer.entityId, (byte)guibutton.id);
             	PacketDispatcher.sendPacketToServer(packet);
         	}
         }
        else if (guibutton.id > 20 && skills[guibutton.id - 21] > 0)
         {
         	Packet packet = SkillPacketHandler.getPacket("LEVELUPSKILLS", mc.thePlayer.entityId, (byte)guibutton.id);
         	PacketDispatcher.sendPacketToServer(packet);
         }
     }
     @Override
     public void onGuiClosed()
     {
         if (!closedWithButton)
         {
         	Packet packet = SkillPacketHandler.getPacket("LEVELUPSKILLS", mc.thePlayer.entityId, (byte) -1, skillsPrev);
         	PacketDispatcher.sendPacketToServer(packet);
         }
     }
     @Override
     public void drawScreen(int i, int j, float f)
     {
         drawDefaultBackground();
         String s = "";
         String s1 = "";
         for (int k = 0; k < buttonList.size(); k++)
         {
             int l = ((GuiButton)buttonList.get(k)).id;
             if (l < 1 || l > 99)
             {
                 continue;
             }
             if (l > 20)
             {
                 l -= 20;
             }
            if (mouseOverButton((GuiButton)buttonList.get(k), i, j))
             {
                 s = toolTips[l - 1];
                 s1 = toolTips2[l - 1];
             }
         }
         updateSkillList();
         if(cl<0)
         	cl = PlayerExtendedProperties.getPlayerClass(mc.thePlayer);
         if (cl > 0)
         {
             drawCenteredString(fontRenderer, "Class: "+GuiClasses.classList[cl], width / 2, 2, 0xffffff);
         }
         drawCenteredString(fontRenderer, "Mining: "+skills[0], width / 2 - offset, 20, 0xffffff);
         drawCenteredString(fontRenderer, "Melee: "+skills[1], width / 2 - offset, 52, 0xffffff);
         drawCenteredString(fontRenderer, "Defense: "+skills[2], width / 2 - offset, 84, 0xffffff);
         drawCenteredString(fontRenderer, "Woodcutting: "+skills[3], width / 2 - offset, 116, 0xffffff);
         drawCenteredString(fontRenderer, "Smelting: "+skills[4], width / 2 - offset, 148, 0xffffff);
         drawCenteredString(fontRenderer, "Marksman: "+skills[5], width / 2 - offset, 180, 0xffffff);
         drawCenteredString(fontRenderer, "Athletics: "+skills[6], width / 2 + offset, 20, 0xffffff);
         drawCenteredString(fontRenderer, "Cooking: "+skills[7], width / 2 + offset, 52, 0xffffff);
         drawCenteredString(fontRenderer, "Stealth: "+skills[8], width / 2 + offset, 84, 0xffffff);
         drawCenteredString(fontRenderer, "Farming: "+skills[9], width / 2 + offset, 116, 0xffffff);
         drawCenteredString(fontRenderer, "Fishing: "+skills[10], width / 2 + offset, 148, 0xffffff);
         drawCenteredString(fontRenderer, "Digging: "+skills[11], width / 2 + offset, 180, 0xffffff);
         drawCenteredString(fontRenderer, s, width / 2, height / 6 + 168, 0xffffff);
         drawCenteredString(fontRenderer, s1, width / 2, height / 6 + 180, 0xffffff);
         super.drawScreen(i, j, f);
     }
     @Override
     public boolean doesGuiPauseGame()
     {
         return false;
     }
 }
