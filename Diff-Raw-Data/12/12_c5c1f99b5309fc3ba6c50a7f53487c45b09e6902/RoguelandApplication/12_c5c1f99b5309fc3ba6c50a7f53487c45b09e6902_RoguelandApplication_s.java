 package org.sankozi.rogueland;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.Module;
 import org.apache.log4j.PropertyConfigurator;
 import org.pushingpixels.substance.api.skin.SubstanceDustCoffeeLookAndFeel;
 import org.sankozi.rogueland.gui.GuiModule;
 import org.sankozi.rogueland.gui.MainFrame;
 import org.sankozi.rogueland.resources.ResourceProvider;
 
 import javax.swing.*;
 
 /**
  * The main class of the application.
  */
 public class RoguelandApplication {
 
     Module module = new RoguelandModule();
 	Module guiModule = new GuiModule();
 
     /**
      * At startup create and show the main frame of the application.
      */
     protected void startup() {
         Injector injector = Guice.createInjector(module, guiModule);
        try {
            UIManager.setLookAndFeel(new SubstanceDustCoffeeLookAndFeel());
        } catch (UnsupportedLookAndFeelException e) {
            throw new RuntimeException();
        }
         injector.getInstance(MainFrame.class).setVisible(true);
     }
 
     /**
      * Main method launching the application.
      */
     public static void main(String[] args) {
         PropertyConfigurator.configure(ResourceProvider.getLog4jProperties());
         SwingUtilities.invokeLater(() -> new RoguelandApplication().startup());
     }
 }
