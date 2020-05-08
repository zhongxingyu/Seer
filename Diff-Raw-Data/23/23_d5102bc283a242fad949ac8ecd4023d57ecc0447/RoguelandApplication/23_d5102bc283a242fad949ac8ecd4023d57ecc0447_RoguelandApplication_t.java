 package org.sankozi.rogueland;
 
import com.google.inject.CreationException;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.Module;
import com.google.inject.spi.Message;
import org.apache.log4j.Logger;
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
    private final static Logger LOG = Logger.getLogger(RoguelandApplication.class);
 
     Module module = new RoguelandModule();
 	Module guiModule = new GuiModule();
 
     /**
      * At startup create and show the main frame of the application.
      */
     protected void startup() {
        try {
            Injector injector = Guice.createInjector(module, guiModule);
 
            injector.getInstance(MainFrame.class).setVisible(true);
        } catch (CreationException ex) {
            LOG.error("Trying to show all guice messages...");
            for(Message message : ex.getErrorMessages()){
                LOG.error(message.getMessage());
            }
            LOG.error("...done. Exception message:");
            LOG.error(ex.getMessage(), ex);
        } catch (Throwable t) {
            LOG.error(t.getMessage(), t);
        }
     }
 
     /**
      * Main method launching the application.
      */
     public static void main(String[] args) {
         PropertyConfigurator.configure(ResourceProvider.getLog4jProperties());
         try {
             UIManager.setLookAndFeel(new SubstanceDustCoffeeLookAndFeel());
         } catch (UnsupportedLookAndFeelException e) {
             throw new RuntimeException();
         }
         SwingUtilities.invokeLater(() -> new RoguelandApplication().startup());
     }
 }
