 package lab1;
 
 /**
  *
  * @Drew Holloway
  */
 public class StartUp {
 
     public static void main(String[] args) {
 
         //instantiate GUI
         Gui gui = new Gui();
 
         String answer;
 
         //answer=gui.askUser("How are you?");
 
         //gui.showMessage("You said you were "+answer);
 
         //gui.displayErrorMessage(answer);
 
         answer = gui.askYesNo("Would you like to take Intro to Programming?");
         if (answer == "yes") {
             // take Intro to Programming Class
         } else {
             answer = gui.askYesNo("Would you like to take Intro to Java?");
             if (answer == "yes") {
                 // take Intro to Java Class
             } else {
                 gui.showMessage("Then you must be hear for Advanced Java!!!");
             }
         }
 
     }
 }
