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
 
     public MainMenu()
     {
         super("MyTunes", "Administration",
                "Control",
                "....");
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
            case 3:
                doActionOption3();
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
 
    private void doActionOption3()
    {
        new SubMenuCon().run();
    }
 }
