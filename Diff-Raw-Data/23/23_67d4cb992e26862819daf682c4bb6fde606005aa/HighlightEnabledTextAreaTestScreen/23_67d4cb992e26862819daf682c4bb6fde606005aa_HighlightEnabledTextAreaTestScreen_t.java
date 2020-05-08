 package org.edc.ht.screens;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.microedition.lcdui.Displayable;
 import javax.microedition.lcdui.Font;
 import javax.microedition.lcdui.Graphics;
 import javax.microedition.midlet.MIDlet;
 
 import org.edc.j2me.widget.ArrowButton;
 import org.edc.j2me.widget.HighlightEnabledTextArea;
 
 public class HighlightEnabledTextAreaTestScreen extends AbstractFontResizableCanvas {
 
     // private Font font;
     protected HighlightEnabledTextArea textArea;
     protected Timer timer;
     protected int highlightedTokenIdx = 0;
     private final String text;
 
     public HighlightEnabledTextAreaTestScreen(MIDlet midlet, Displayable mainMenu, String text, Font font) {
         super(midlet, mainMenu, font);
         // We cannot use class.getName() b/c proguard obfuscates the name
         setTitle("Text Highlighting");
         this.text = text;
         updateSlideContents();
     }

     protected void paint(Graphics g) {
         clearScreen(g);
         printMenuIndicator(g, getUpdatedFont());
         textArea.render(g, getWidthPercent(5), y);
         renderToolBar(g, ArrowButton.LEFT, CENTER_BUTTON, ArrowButton.RIGHT);
     }
 
     protected void showNotify() {
         super.showNotify();
         initTokenHighlighting();
     }
 
     protected void initTokenHighlighting() {
         final int tokenCount = textArea.tokenCount();
         timer = new Timer();
         TimerTask task = new TimerTask() {
             public void run() {
                 if (highlightedTokenIdx >= tokenCount) {
                     cancel();
                 } else {
                    textArea.setHighlightedToken(highlightedTokenIdx++);
                     repaint();
                 }
             }
         };
         timer.schedule(task, 200l, 500l);
     }
 
     protected void hideNotify() {
         super.hideNotify();
         cancelTokenHighlighting();
     }
 
     protected void cancelTokenHighlighting() {
         if (timer != null) {
             timer.cancel();
             timer = null;
         }
         highlightedTokenIdx = 0;
     }
 
     protected void updateSlideContents() {
         textArea = new HighlightEnabledTextArea(text, getWidthPercent(80), getUpdatedFont(), 0xAA0000, 0xDDDDDD, this);
         cancelTokenHighlighting();
         if (isShown()) {
             initTokenHighlighting();
         }
     }
 
 }
