 package org.blink.game.input;
 
 import de.lessvoid.nifty.Nifty;
 import de.lessvoid.nifty.controls.Controller;
 import de.lessvoid.nifty.elements.Element;
 import de.lessvoid.nifty.elements.render.TextRenderer;
 import de.lessvoid.nifty.input.NiftyInputEvent;
 import de.lessvoid.nifty.screen.Screen;
 import de.lessvoid.nifty.tools.SizeValue;
 import de.lessvoid.xml.xpp3.Attributes;
 import java.util.Properties;
 
 public class UIHealthbarControl implements Controller {
 
     private Element healthBarElement;
     private Element healthTextElement;
 
     public void onStartScreen() {
     }
 
     public void onFocus(final boolean getFocus) {
     }
 
     public boolean inputEvent(final NiftyInputEvent inputEvent) {
         return false;
     }
 
     public void bind(Nifty nifty, Screen screen, Element elmnt, Properties prprts, Attributes atrbts) {
         healthBarElement = elmnt.findElementByName("health");
         healthTextElement = elmnt.findElementByName("health-text");
     }
 
     public void init(Properties prprts, Attributes atrbts) {
     }
 
     public void setProgress(final float progressValue) {
         float progress = progressValue;
         if (progress < 0.0f) {
             progress = 0.0f;
         } else if (progress > 1.0f) {
             progress = 1.0f;
         }
        final int MIN_WIDTH = 32;
        int pixelWidth = (int) (MIN_WIDTH + (healthBarElement.getParent().getWidth() - MIN_WIDTH - 90) * progress);
 
         healthBarElement.setConstraintWidth(new SizeValue(pixelWidth + "px"));
         healthBarElement.getParent().layoutElements();
     }
     
     public void setHealth(int current, int max) {
         healthTextElement.getRenderer(TextRenderer.class).setText(current + "/" + max);
     }
 }
