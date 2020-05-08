 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package UI;
 
 /**
  *
  * @author Stefan, Mak, Jonas og Daniel
  */
 public class MainMenu extends Menu
 {
 
    private static final int EXIT_VALUE = 0;
    
     public MainMenu()
     {
         super("MyTunes", "Administration",
                "Control");
                EXIT_OPTION = EXIT_VALUE;
     }
 
     @Override
     protected void doAction(int option)
     {
         switch (option)
         {
             case 1:
                 doActionOption1();
                 break;
             case 2:
                 doActionOption2();
                 break;
         }
     }
 
     private void doActionOption1()
     {
         new SubMenuAdmin().run();
     }
 
     private void doActionOption2()
     {
         System.out.println("You selected Control");
         new SubMenuCon().run();
     }
 
 }
