 package epsilon.game;
 
 /**
  *
  *
  * @author Marius
  */
 public class NetworkPage extends MenuPage {
 
     private static NetworkPage page = new NetworkPage();
 
     private boolean typingString;
     private String[] currentString;
     private String[] originalStrings;
 
     private NetworkPage() {
         super(new String[]{"IP address: ", "Name: ", " Connect", "Back"}, "Network setup");
         originalStrings = new String[]{"IP address: ", "Name: ", "Connect", "Back"};
         typingString = false;
         currentString = new String[]{"", "" , "", ""};
     }
 
     @Override
     public void useSelected() {
         if (selected == 0 || selected == 1) {
             typingString = true;
             Input.get().requestString(currentString[selected]);
         } else if (selected == 2) {
             // TODO: connect to server here.
         } else if (selected == 3) {
             Menu.get().goToPrevious();
         }
     }
 
     public static NetworkPage get() {
          return page;
     }
 
     @Override
     public void update() {
 
         // check if input on any field should be handeled
         if (!typingString) {
             // no inputing, use superclass update
             super.update();
         } else {
 
             // check if the person has finished typing
             if (!Input.get().isTyping()) {
                 // if the typing is finished, indicate this to the class and save the variable
                 typingString = false;
 
                 currentString[selected] = Input.get().getFinalText();
 
                 items[selected] = originalStrings[selected] + Input.get().getFinalText();
             } else {
                 items[selected] = originalStrings[selected] + Input.get().getCurrentText();
             }
         }
     }
 
 }
