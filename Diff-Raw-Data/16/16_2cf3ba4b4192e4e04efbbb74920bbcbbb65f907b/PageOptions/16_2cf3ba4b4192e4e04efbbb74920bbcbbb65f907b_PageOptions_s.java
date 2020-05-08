 package org.tilegames.mc.magicchest.client.gui;
 
 public class PageOptions extends Page {
 
     public static final int BUTTON_ID = 101;
     
     public PageOptions (GuiMagicChest gui) {
         super (gui);
     }
 
     @Override
     public String getTitle () {
         return "Options";
     }
     
     @Override
     public int getButtonId () {
         return BUTTON_ID;
     }
     
 
     @Override
     public void draw (int mouseX, int mouseY) {
        gui.renderHelper.bindAndDrawBackgroundTexture ("/MagicChest/pages/Options.png");
     }
 
     @Override
     public void onClick (int x, int y, int button) {
         
     }
 
     @Override
     public void onKeyType (char character, int key) {
         
     }
 
 }
