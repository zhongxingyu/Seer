 package com.bitspatter.renderers;
 
 import org.newdawn.slick.*;
 
 import com.bitspatter.Warptris;
 import com.bitspatter.states.PlayingState.PlayState;
 
 public class InstructionsRenderer {
     final int buttonSpacing = 4, buttonColumnPadding = 20, buttonInsetY = 8;
     final int lineSpacing = 8;
     final String gameOverString = "GAME OVER!";
 
     Image arrowUp, arrowDown, arrowLeft, arrowRight;
     Image longButton, shortButton;
     Font buttonFont, instructionFont, gameOverFont;
 
     public InstructionsRenderer() throws SlickException {
         arrowUp = new Image("key_up.png");
         arrowDown = new Image("key_down.png");
         arrowLeft = new Image("key_left.png");
         arrowRight = new Image("key_right.png");
 
         longButton = new Image("key_long_blank.png");
         shortButton = new Image("key_blank.png");
 
         buttonFont = Warptris.getFont(10, false);
         instructionFont = Warptris.getFont(18, false);
         gameOverFont = Warptris.getFont(32, false);
     }
 
     int buttonColumnWidth = -1;
 
     int getButtonColumnWidth() {
         if (buttonColumnWidth < 0) {
             buttonColumnWidth = Math.max(2 * shortButton.getWidth(), arrowRight.getWidth() + arrowLeft.getWidth());
             buttonColumnWidth += buttonSpacing;
             buttonColumnWidth = Math.max(buttonColumnWidth, longButton.getWidth());
         }
 
         return buttonColumnWidth;
     }
 
     int lineHeight = -1;
 
     int getLineHeight() {
         if (lineHeight < 0) {
             lineHeight = instructionFont.getLineHeight();
             for (Image i : new Image[] { arrowUp, arrowDown, arrowLeft, arrowRight, longButton, shortButton }) {
                 lineHeight = Math.max(lineHeight, i.getHeight());
             }
         }
 
         return lineHeight;
     }
 
     public float drawLine(Graphics g, float x, float y, String instruction, Image... buttons) {
         return drawLine(g, x, y, instruction, buttons, new String[buttons.length]);
     }
 
     public float drawLine(Graphics g, float x, float y, String instruction, Image[] buttons, String[] buttonText) {
         int buttonsWidth = (buttons.length - 1) + buttonSpacing;
         for (Image button : buttons) {
             buttonsWidth += button.getWidth();
         }
         int columnWidth = getButtonColumnWidth();
 
         float buttonX = x + (columnWidth - buttonsWidth) / 2f;
         for (int i = 0; i < buttons.length; ++i) {
             Image button = buttons[i];
             g.drawImage(button, buttonX, y);
             if (buttonText[i] != null) {
                 drawButtonText(g, buttonX, y, button, buttonText[i]);
             }
 
             buttonX += button.getWidth() + buttonSpacing;
         }
 
         int lineHeight = instructionFont.getLineHeight();
         int fontOffset = (getLineHeight() - lineHeight) / 2;
         g.drawString(instruction, x + columnWidth + buttonColumnPadding, y + fontOffset);
 
         return y + getLineHeight() + lineSpacing;
     }
 
     private void drawButtonText(Graphics g, float x, float y, Image button, String text) {
         float textX = x + (button.getWidth() - buttonFont.getWidth(text)) / 2f;
         g.setColor(Color.black);
         g.setFont(buttonFont);
         g.drawString(text, textX, y + buttonInsetY);
         g.setFont(instructionFont);
         g.setColor(Color.white);
     }
 
     public void render(Graphics g, GameContainer gc, PlayState state, float x, float y) {
         y += lineSpacing;
 
         g.setColor(Color.white);
         g.setFont(instructionFont);
 
         if (state == PlayState.Warping) {
             y = drawLine(g, x, y, "Leave warp mode", new Image[] { longButton }, new String[] { "Space" });
             y += lineSpacing;
             g.drawString("Drag a block with the\nmouse to move it.", x, y);
         } else if (state == PlayState.Ended) {
             y = drawLine(g, x, y, "Return to main menu", new Image[] { shortButton }, new String[] { "Esc" });
             y -= lineSpacing;
 
             float remainingWidth = gc.getWidth() - x, remainingHeight = gc.getHeight() - y;
             g.setFont(gameOverFont);
             x += (remainingWidth - gameOverFont.getWidth(gameOverString)) / 2f;
             y += (remainingHeight - gameOverFont.getHeight(gameOverString)) / 2f;
             g.drawString(gameOverString, x, y);
         } else {
             y = drawLine(g, x, y, "Move", arrowLeft, arrowRight);
             y = drawLine(g, x, y, "Lower one line", arrowDown);
             y = drawLine(g, x, y, "Send to bottom", arrowUp);
            y = drawLine(g, x, y, "Rotate", new Image[] { shortButton, shortButton }, new String[] { "X", "C" });
             y = drawLine(g, x, y, "Enter warp mode", new Image[] { longButton }, new String[] { "Space" });
             y = drawLine(g, x, y, "Toggle pause", new Image[] { shortButton }, new String[] { "Esc" });
         }
     }
 }
