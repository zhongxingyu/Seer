 package kovu.teamstats;
 
 import net.minecraft.client.gui.GuiButton;
 import net.minecraft.client.gui.GuiScreen;
 
 public class GuiTeamInfoConfig extends GuiScreen {
 
     private GuiScreen parentScreen;
 
     public GuiTeamInfoConfig(GuiScreen guiscreen) {
         parentScreen = guiscreen;
     }
 
     public void updateScreen() {
     }
 
     public void drawScreen(int i, int j, float f) {
         drawRect(width / 2 - 165, height / 2 - 20, width / 2 + 165, height / 2 + 60, 0x60000000);
         drawCenteredString(fontRenderer, "\247cConfig:", width / 2, height / 2 - 13, 0xffffff);
         super.drawScreen(i, j, f);
     }
 
     public void initGui() {
         buttonList.clear();
         buttonList.add(new GuiButton(0, width / 2 - 155, height / 2 + 5, 100, 20, (new StringBuilder()).append("Scale ").append((int) (Kovu.scale * 100F)).append("%").toString()));
         buttonList.add(new GuiButton(1, width / 2 - 50, height / 2 + 5, 100, 20, (new StringBuilder()).append("Armor ").append(Kovu.armour ? "On" : "Off").toString()));
         buttonList.add(new GuiButton(2, width / 2 + 55, height / 2 + 5, 100, 20, (new StringBuilder()).append("EmptyArmor ").append(Kovu.noarmour ? "On" : "Off").toString()));
         buttonList.add(new GuiButton(3, width / 2 - 155, height / 2 + 5 + 24, 100, 20, (new StringBuilder()).append("Co-ords ").append(Kovu.coords ? "On" : "Off").toString()));
         buttonList.add(new GuiButton(4, width / 2 - 50, height / 2 + 5 + 24, 100, 20, (new StringBuilder()).append("Hunger ").append(Kovu.hunger ? "On" : "Off").toString()));
         buttonList.add(new GuiButton(5, width / 2 + 55, height / 2 + 5 + 24, 100, 20, "Back"));
     }
 
     public void actionPerformed(GuiButton guibutton) {
         if (guibutton.id == 0) {
             if (Kovu.scale != 1.5F) {
                 Kovu.scale = (float) ((double) Kovu.scale + 0.25D);
             } else {
                 Kovu.scale = 0.5F;
             }
             guibutton.displayString = (new StringBuilder()).append("Scale ").append((int) (Kovu.scale * 100F)).append("%").toString();
         }
         if (guibutton.id == 1) {
             Kovu.armour = !Kovu.armour;
             guibutton.displayString = (new StringBuilder()).append("Armor ").append(Kovu.armour ? "On" : "Off").toString();
         }
         if (guibutton.id == 2) {
             Kovu.noarmour = !Kovu.noarmour;
             guibutton.displayString = (new StringBuilder()).append("EmptyArmor ").append(Kovu.noarmour ? "On" : "Off").toString();
         }
         if (guibutton.id == 3) {
             Kovu.coords = !Kovu.coords;
             guibutton.displayString = (new StringBuilder()).append("Co-ords ").append(Kovu.coords ? "On" : "Off").toString();
         }
         if (guibutton.id == 4) {
             Kovu.hunger = !Kovu.hunger;
             guibutton.displayString = (new StringBuilder()).append("Hunger ").append(Kovu.hunger ? "On" : "Off").toString();
         }
 
         if (guibutton.id == 5) {
             mc.displayGuiScreen(parentScreen);
         }
     }
 }
