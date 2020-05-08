 package com.bencvt.minecraft.buildregion.ui.window;
 
 import java.awt.Desktop;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 
 import libshapedraw.primitive.Color;
 import net.minecraft.src.GuiButton;
 import net.minecraft.src.GuiSlot;
 import net.minecraft.src.Tessellator;
 
 import com.bencvt.minecraft.buildregion.Controller;
 
 /**
  * The help screen, which is just a scrollable text area.
  * 
  * @author bencvt
  */
 public class GuiScreenHelp extends GuiScreenBase {
     public static final int MARGIN_X = 48;
     public static final int TEXT_ARGB = Color.WHITE.getARGB();
     public static final URI WEBSITE_URL = URI.create("http://bit.ly/BuildRegion");
 
     private final GuiStandardButton buttonDone = new GuiStandardButton(this, i18n("button.done"));
     private final ArrayList<String> lines = new ArrayList<String>();
     private final HashMap<Integer, String> rightColumnLines = new HashMap<Integer, String>();
     private final Controller controller;
     private int urlLineNum;
     private int leftColumnWidth;
     private GuiSlot contents;
 
     public GuiScreenHelp(GuiScreenBase parentScreen, Controller controller) {
         super(parentScreen);
         this.controller = controller;
     }
 
     private void getLines(Controller controller) {
         lines.clear();
         lines.addAll(fontRenderer.listFormattedStringToWidth(i18n("help.about"), width - MARGIN_X*2));
         lines.add("");
         lines.add(i18n("help.author") + " bencvt");
         urlLineNum = lines.size();
         lines.add(i18n("help.url") + " \u00a79\u00a7n" + WEBSITE_URL);
         lines.add(i18n("help.usage"));
         lines.addAll(Arrays.asList(controller.getInputManager().getUsage("  ").split("\n")));
         lines.add("");
         lines.addAll(fontRenderer.listFormattedStringToWidth(i18n("help.gui"), width - MARGIN_X*2));
         lines.add("");
 
         rightColumnLines.clear();
         leftColumnWidth = 0;
         for (int i = 0; i < lines.size(); i++) {
             String line = lines.get(i);
             int t = line.indexOf('\t');
             if (t >= 0) {
                 String leftColumn = line.substring(0, t);
                 leftColumnWidth = Math.max(leftColumnWidth, fontRenderer.getStringWidth(leftColumn));
                 lines.set(i, leftColumn);
                 rightColumnLines.put(i, line.substring(t + 1));
             }
         }
     }
 
     @Override
     public void initGui() {
         getLines(controller);
 
         buttonDone.setWidth(200).setPositionXY((width - buttonDone.getWidth())/2, height - 30);
         controlList.add(buttonDone);
 
         contents = new GuiSlot(mc, width, height, 16, height - 28, fontRenderer.FONT_HEIGHT + 1) {
             private boolean openedUrl;
 
             @Override
             protected int getSize() {
                 return lines.size();
             }
 
             @Override
             protected void elementClicked(int slotNum, boolean doubleClick) {
                 if (slotNum == urlLineNum && !openedUrl) {
                     try {
                         Desktop.getDesktop().browse(WEBSITE_URL);
                     } catch (Throwable t) {
                         // do nothing
                     }
                     openedUrl = true;
                 }
             }
 
             @Override
             protected boolean isSelected(int slotNum) {
                 return false;
             }
 
             @Override
             protected void drawBackground() {
                 // do nothing
             }
 
             @Override
             protected void drawSlot(int slotNum, int xPos, int yPos, int slotContentHeight, Tessellator tess) {
                 getFontRenderer().drawString(lines.get(slotNum), MARGIN_X, yPos, TEXT_ARGB);
                 if (rightColumnLines.containsKey(slotNum)) {
                     getFontRenderer().drawString(rightColumnLines.get(slotNum),
                             leftColumnWidth + MARGIN_X + 8, yPos, TEXT_ARGB);
                 }
             }
 
            /**
             * This method is defined only by Forge, allowing mods to make a
             * custom background for each slot. By overriding it with an empty
             * method, the original background remains -- in our case, a
             * nice darkened view of the game world.
             * <p>
             * This method will never be called if we're using ModLoader
             * instead, as vanilla Minecraft hard-codes slot backgrounds. So
             * our help screen will have an interesting background when using
             * Forge, and a plain black/dirt background when not.
             */
             protected void drawContainerBackground(Tessellator tess) {
                 // do nothing
             }
 
             @Override
             protected int getScrollBarX() {
                 return width - 10;
             }
         };
     }
 
     @Override
     public void drawScreen(int xMouse, int yMouse, float partialTick) {
         drawDefaultBackground();
         contents.drawScreen(xMouse, yMouse, partialTick);
         drawCenteredString(fontRenderer, controller.getModTitle(), width / 2, 4, TEXT_ARGB);
         super.drawScreen(xMouse, yMouse, partialTick);
     }
 
     @Override
     protected void actionPerformed(GuiButton guiButton) {
         if (guiButton == buttonDone) {
             close();
         }
     }
 }
