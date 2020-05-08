 package jk_5.nailed.client.gui.elements;
 
 import jk_5.nailed.client.NailedClient;
 import jk_5.nailed.client.TickHandlerClient;
 import jk_5.nailed.client.render.FixedWidthFontRenderer;
 import jk_5.nailed.map.script.IMachine;
 import jk_5.nailed.map.script.Terminal;
 import lombok.Getter;
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.gui.Gui;
 import net.minecraft.client.gui.GuiScreen;
 import net.minecraft.util.ResourceLocation;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.GL11;
 
 /**
  * No description given
  *
  * @author jk-5
  */
 public class ElementTerminal extends Gui {
 
     private static final ResourceLocation background = new ResourceLocation("nailed", "textures/gui/terminal-bg.png");
     private static int MARGIN = 2;
 
     @Getter private int xPosition;
     @Getter private int yPosition;
     @Getter private int width;
     @Getter private int height;
     private float terminateTimer = 0;
     private float rebootTimer = 0;
     private float shutdownTimer = 0;
     private int lastClickButton = -1;
     private int lastClickX = -1;
     private int lastClickY = -1;
     private IMachine machine;
 
     public ElementTerminal(int x, int y, int termWidth, int termHeight, IMachine machine){
         this.xPosition = x;
         this.yPosition = y;
         this.width = 2 * MARGIN + termWidth * FixedWidthFontRenderer.FONT_WIDTH;
         this.height = 2 * MARGIN + termHeight * FixedWidthFontRenderer.FONT_HEIGHT;
         this.machine = machine;
     }
 
     public void keyTyped(char c, int key){
         if(c == '\026'){
             String clipboard = GuiScreen.getClipboardString();
             if(clipboard != null){
                 int newlineIndex = clipboard.indexOf(System.getProperty("line.seperator"));
                 if(newlineIndex >= 0){
                     clipboard = clipboard.substring(0, newlineIndex);
                 }
                 if(!clipboard.isEmpty()){
                     if(clipboard.length() > 128){
                         clipboard = clipboard.substring(0, 128);
                     }
                     this.machine.queueEvent("paste", clipboard);
                 }
             }
             return;
         }
 
         if(this.terminateTimer < 0.5F && this.rebootTimer < 0.5F && this.shutdownTimer < 0.5F){
             if(key > 0){
                 this.machine.queueEvent("key", key);
             }
             if(FixedWidthFontRenderer.ALLOWED_CHARS.indexOf(c) >= 0){
                 this.machine.queueEvent("char", Character.toString(c));
             }
         }
     }
 
     public void mouseClicked(int x, int y, int button){
         if((x >= this.xPosition) && (x < this.xPosition + this.width) && (y >= this.yPosition) && (y < this.yPosition + this.height) && (button >= 0) && (button <= 2)){
             Terminal term = this.machine.getTerminal();
             if(term != null){
                 int charX = (x - (this.xPosition + MARGIN)) / FixedWidthFontRenderer.FONT_WIDTH;
                 int charY = (y - (this.yPosition + MARGIN)) / FixedWidthFontRenderer.FONT_HEIGHT;
                 charX = Math.min(Math.max(charX, 0), term.getWidth() - 1);
                 charY = Math.min(Math.max(charY, 0), term.getHeight() - 1);
 
                 this.machine.queueEvent("mouse_click", button + 1, charX + 1, charY + 1);
 
                 this.lastClickButton = button;
                 this.lastClickX = charX;
                 this.lastClickY = charY;
             }
         }
     }
 
     public void handleMouseInput(int x, int y){
         if(this.lastClickButton >= 0 && !Mouse.isButtonDown(this.lastClickButton)){
             this.lastClickButton = -1;
         }
 
         int wheelChange = Mouse.getEventDWheel();
         if(wheelChange == 0 && this.lastClickButton == -1){
             return;
         }
 
         if(x >= this.xPosition && x < this.xPosition + this.width && y >= this.yPosition && y < this.yPosition + this.height){
             Terminal term = this.machine.getTerminal();
             if(term != null){
                 int charX = (x - (this.xPosition + MARGIN)) / FixedWidthFontRenderer.FONT_WIDTH;
                 int charY = (y - (this.yPosition + MARGIN)) / FixedWidthFontRenderer.FONT_HEIGHT;
                 charX = Math.min(Math.max(charX, 0), term.getWidth() - 1);
                 charY = Math.min(Math.max(charY, 0), term.getHeight() - 1);
 
                 if(wheelChange < 0){
                     this.machine.queueEvent("mouse_scroll", -1, charX + 1, charY + 1);
                 }else if(wheelChange > 0){
                     this.machine.queueEvent("mouse_scroll", 1, charX + 1, charY + 1);
                 }
 
                 if(this.lastClickButton >= 0 && (charX != this.lastClickX || charY != this.lastClickY)){
                     this.machine.queueEvent("mouse_drag", this.lastClickButton + 1, charX + 1, charY + 1);
                     this.lastClickX = charX;
                     this.lastClickY = charY;
                 }
             }
         }
     }
 
     public void update(){
         if(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)){
             if(Keyboard.isKeyDown(Keyboard.KEY_C)){
                 if(this.terminateTimer < 1.0F){
                    this.terminateTimer += 0.05F;
                     if(this.terminateTimer >= 1.0F){
                         if(this.machine != null){
                             this.machine.queueEvent("terminate");
                         }
                     }
                 }
             }else{
                 this.terminateTimer = 0.0F;
             }
 
             if(Keyboard.isKeyDown(Keyboard.KEY_R)){
                 if(this.rebootTimer < 1.0F){
                     this.rebootTimer += 0.05F;
                     if(this.rebootTimer >= 1.0F){
                         if(this.machine != null){
                             this.machine.reboot();
                         }
                     }
                 }
             }else{
                 this.rebootTimer = 0.0F;
             }
 
             if(Keyboard.isKeyDown(Keyboard.KEY_S)){
                 if(this.shutdownTimer < 1.0F){
                     this.shutdownTimer += 0.05F;
                     if(this.shutdownTimer >= 1.0F){
                         if(this.machine != null){
                             this.machine.shutdown();
                         }
                     }
                 }
             }else{
                 this.shutdownTimer = 0.0F;
             }
         }else{
             this.terminateTimer = 0.0F;
             this.rebootTimer = 0.0F;
             this.shutdownTimer = 0.0F;
         }
     }
 
     public void draw(Minecraft mc, int xOrgin, int yOrgin){
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
 
         int startX = xOrgin + this.xPosition;
         int startY = yOrgin + this.yPosition;
 
         mc.getTextureManager().bindTexture(background);
         drawTexturedModalRect(startX, startY, 0, 0, this.width, this.height);
 
         Terminal terminal = this.machine != null ? this.machine.getTerminal() : null;
         if(terminal != null){
             synchronized(terminal){
                 FixedWidthFontRenderer fontRenderer = NailedClient.getFixedWidthFontRenderer();
                 boolean tblink = (terminal.isCursorBlink()) && TickHandlerClient.blinkOn();
                 int tw = terminal.getWidth();
                 int th = terminal.getHeight();
                 int tx = terminal.getCursorX();
                 int ty = terminal.getCursorY();
 
                 int x = startX + MARGIN;
                 int y = startY + MARGIN;
 
                 String emptyLine = terminal.getLine(-1);
                 fontRenderer.drawString(emptyLine, x, y - FixedWidthFontRenderer.FONT_HEIGHT, terminal.getColorLine(0), MARGIN);
                 fontRenderer.drawString(emptyLine, x, y + th * FixedWidthFontRenderer.FONT_HEIGHT, terminal.getColorLine(th - 1), MARGIN);
 
                 for(int line = 0; line < th; line++){
                     String text = terminal.getLine(line);
                     String colour = terminal.getColorLine(line);
                     fontRenderer.drawString(text, x, y, colour, MARGIN);
                     if(tblink && ty == line && tx >= 0 && tx < tw){
                         String cursorColour = "0123456789abcdef".charAt(terminal.getTextColor()) + "";
                         fontRenderer.drawString("_", x + FixedWidthFontRenderer.FONT_WIDTH * tx, y, cursorColour, 0);
                     }
 
                     y += FixedWidthFontRenderer.FONT_HEIGHT;
                 }
             }
         }
     }
 }
