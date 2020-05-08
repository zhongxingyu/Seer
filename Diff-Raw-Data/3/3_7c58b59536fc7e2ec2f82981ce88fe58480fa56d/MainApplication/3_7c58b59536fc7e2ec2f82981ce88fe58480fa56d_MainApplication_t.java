 package jDistsim.main;
 
 import jDistsim.application.designer.DesignerGuiApplication;
 import jDistsim.utils.logging.Logger;
 
 import javax.swing.*;
 
 /**
  * Author: Jirka Pénzeš
  * Date: 18.9.12
  * Time: 23:03
  */
 public class MainApplication {
 
     public static void main(String[] args) {
         Logger.defaultInitialize();
         Logger.log("Start jDistsim application");
         try {
             UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
             new DesignerGuiApplication().Start();
        } catch (Exception exception) {
            Logger.log(exception);
         }
     }
 }
