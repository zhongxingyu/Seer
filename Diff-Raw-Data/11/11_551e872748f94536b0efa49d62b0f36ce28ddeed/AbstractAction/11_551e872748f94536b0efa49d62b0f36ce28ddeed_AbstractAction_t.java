 package net.codjo.mad.gui.framework;
import javax.swing.Icon;
 import net.codjo.gui.toolkit.util.ErrorDialog;
 import java.awt.event.ActionEvent;
 import java.beans.PropertyVetoException;
 import javax.swing.JInternalFrame;
 import javax.swing.UIManager;
 import javax.swing.event.InternalFrameAdapter;
 import javax.swing.event.InternalFrameEvent;
 import org.apache.log4j.Logger;
 /**
  * Action implmentant l'ouverture d'une fentre.
  */
 public abstract class AbstractAction extends AbstractGuiAction {
     private static final Logger LOG = Logger.getLogger(AbstractAction.class);
     private CleanUpListener cleanUpListener = new CleanUpListener();
     private JInternalFrame frame;
 
 
     protected AbstractAction(GuiContext ctxt, String name, String description) {
         this(ctxt, name, description, null);
     }
 
 
     protected AbstractAction(GuiContext ctxt, String name, String description, String iconId) {
         super(ctxt, name, description, iconId);
     }
 
 
     public void actionPerformed(ActionEvent event) {
         if (frame == null) {
             displayNewWindow();
         }
         else {
             try {
                 frame.setSelected(true);
             }
             catch (PropertyVetoException ex) {
                 LOG.error(ex);
             }
         }
     }
 
 
     protected abstract JInternalFrame buildFrame(GuiContext ctxt) throws Exception;
 
 
     protected void displayNewWindow() {
         try {
             frame = buildFrame(getGuiContext());
             frame.addInternalFrameListener(cleanUpListener);
             getDesktopPane().add(frame);
            frame.setFrameIcon(getDefaultFrameIcon());
             frame.pack();
             frame.setVisible(true);
             frame.setSelected(true);
         }
         catch (Exception ex) {
             LOG.error(ex);
             ErrorDialog.show(getGuiContext().getDesktopPane(), "Impossible d'afficher la fentre !", ex);
         }
     }
 
 
    protected Icon getDefaultFrameIcon() {
        return UIManager.getIcon("icon");
    }

     protected void closeFrame() {
         if (frame != null) {
             frame.dispose();
         }
     }
 
 
     private class CleanUpListener extends InternalFrameAdapter {
         @Override
         public void internalFrameClosed(InternalFrameEvent event) {
             event.getInternalFrame().removeInternalFrameListener(this);
             frame = null;
         }
 
 
         @Override
         public void internalFrameClosing(InternalFrameEvent event) {
             event.getInternalFrame().removeInternalFrameListener(this);
             frame = null;
         }
     }
 }
