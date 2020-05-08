 package org.blink.game.input;
 
 import de.lessvoid.nifty.Nifty;
 import de.lessvoid.nifty.elements.Element;
 import de.lessvoid.nifty.input.NiftyInputEvent;
 import de.lessvoid.nifty.screen.KeyInputHandler;
 import de.lessvoid.nifty.screen.Screen;
 import de.lessvoid.nifty.screen.ScreenController;
 import org.blink.net.Dispatcher;
 import org.blink.util.LocalConfig;
 import org.blink.util.Util;
 
 /**
  *
  * @author cmessel
  */
 public class HUD implements ScreenController {
 
     public static int MAX_CHAT_LINES = 6;
    private Element textInput;
 
     public void bind(Nifty nifty, Screen screen) {
        textInput = screen.findElementByName("chat_text_input");
     }
 
     public String getSpellIcon0() {
         return Util.getIconPath(LocalConfig.getSpell(0));
     }
 
     public String getSpellIcon1() {
         return Util.getIconPath(LocalConfig.getSpell(1));
     }
 
     public String getSpellIcon2() {
         return Util.getIconPath(LocalConfig.getSpell(2));
     }
 
     public String getSpellIcon3() {
         return Util.getIconPath(LocalConfig.getSpell(3));
     }
 
     public String getSpellIcon4() {
         return Util.getIconPath(LocalConfig.getSpell(4));
     }
 
     public String getSpellIcon5() {
         return Util.getIconPath(LocalConfig.getSpell(5));
     }
 
     public String getSpellFrame() {
         return "Interface/Icons/Spells/set3/frame-8-grey.png";
     }
 
     public void onStartScreen() {
        textInput.addInputHandler(new KeyInputHandler() {
 
             @Override
             public boolean keyEvent(NiftyInputEvent inputEvent) {
                 if (inputEvent == null) {
                     return false;
                 }
                 switch (inputEvent) {
                     case SubmitText:
                         sendMessage();
                         return true;
                 }
                 return false;
             }
         });
     }
 
     public void sendMessage() {
        //String text = textInput.getControl(TextFieldControl.class).getText();
        Dispatcher.chat("FUCK THIS SHIT DEPRECATED MOFO");
        //textInput.getControl(TextFieldControl.class).setText("");
     }
 
     public void onEndScreen() {
        //
     }
 }
