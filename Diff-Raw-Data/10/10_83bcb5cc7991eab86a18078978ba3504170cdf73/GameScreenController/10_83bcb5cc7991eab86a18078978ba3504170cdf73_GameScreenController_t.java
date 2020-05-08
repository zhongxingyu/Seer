 package game;
 
 import com.jme3.app.Application;
 import com.jme3.app.state.AbstractAppState;
 import com.jme3.app.state.AppStateManager;
 import de.lessvoid.nifty.Nifty;
 import de.lessvoid.nifty.controls.label.LabelControl;
 import de.lessvoid.nifty.controls.textfield.TextFieldControl;
 import de.lessvoid.nifty.elements.Element;
 import de.lessvoid.nifty.input.NiftyInputEvent;
 import de.lessvoid.nifty.screen.KeyInputHandler;
 import de.lessvoid.nifty.screen.Screen;
 import de.lessvoid.nifty.screen.ScreenController;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  *
  * @author JP
  */
 public class GameScreenController extends AbstractAppState implements ScreenController
 {
     private Nifty nifty;
     private Screen screen;
     private GameClient app;
     
     // =========================================================================
     // AbstractAppState implementation
     // =========================================================================
     
     @Override
     public void initialize(AppStateManager stateManager, Application app)
     {
         super.initialize(stateManager, app);
         this.app = (GameClient)app;
     }
 
     @Override
     public void update(float tpf)
     {}
     
     // =========================================================================
     // ScreenController implementation
     // =========================================================================
    
     private final int MAX_MESSAGES = 10;
     private Element inputMessage;
     private Element[] messageLabels = new Element[MAX_MESSAGES];
     private List<String> messages = new LinkedList<String>();
     
     public void bind(Nifty nifty, Screen screen)
     {
         this.nifty = nifty;
         this.screen = screen;
         
         inputMessage = screen.findElementByName("input_message");
         for(int i = 1; i <= MAX_MESSAGES; i++)
         {
             messageLabels[i - 1] =
                     screen.findElementByName("t" + i);
             messages.add("");
         }
     }
 
     public void onStartScreen()
     {
         inputMessage.addInputHandler(new KeyFocusHandler());
     }
 
     public void onEndScreen()
     {}
     
     private class KeyFocusHandler implements KeyInputHandler
     {   
         @Override
         public boolean keyEvent(NiftyInputEvent inputEvent)
         {
             if(inputEvent == null)
             {
                 return false;
             }
             
             switch(inputEvent)
             {
                 case SubmitText:
                     handleKey();
                     return true;
             }
             
             return false;
         }
     };
     
     public void handleKey()
     {   
         send();
     }
     
     public void send()
     {
         String message =
                 "[" + app.getPlayerName() + "]: " +
                 inputMessage.getControl(TextFieldControl.class).getText();
         inputMessage.getControl(TextFieldControl.class).setText("");
         
        //addMessage(message);
         
         app.sendChatMessage(message);
     }
     
     public void addMessage(String message)
     {
         messages.add(message);
         if(messages.size() > MAX_MESSAGES)
         {
             messages.remove(0);
         }
 
         for(int i = 0; i < MAX_MESSAGES; i++)
         {
             int id = (MAX_MESSAGES - 1) - i - (MAX_MESSAGES - messages.size());
             messageLabels[id].getControl(LabelControl.class).setText(
                     messages.get(i));
         }
     }
     
     // =========================================================================
     // GUI callbacks
     // =========================================================================
     
     public void onSend()
     {
         send();
     }
 }
