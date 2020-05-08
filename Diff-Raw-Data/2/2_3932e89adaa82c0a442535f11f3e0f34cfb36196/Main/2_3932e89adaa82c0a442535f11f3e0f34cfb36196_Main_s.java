 import view.SLogoView;
 import view.View;
 
 
 /**
  * Runs the SLogo simulation.
  * 
  * @author Yoshida, Sean, Ellango, Ryan, Scott
  */
 public class Main {
 
     private static final String TITLE = "SLogo";
    private static final String LANGUAGE = "Portuguese";
 
     /**
      * Creates the SLogo simulation.
      * 
      * @param args are the command line arguments. This main does not take any arguments.
      */
     public static void main (String[] args) {
         @SuppressWarnings("unused")
         View view = new SLogoView(TITLE, LANGUAGE);
     }
 }
