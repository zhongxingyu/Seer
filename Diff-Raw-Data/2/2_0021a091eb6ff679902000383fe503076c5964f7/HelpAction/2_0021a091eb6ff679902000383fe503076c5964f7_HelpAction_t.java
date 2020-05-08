 package de.aidger.controller.actions;
 
 import static de.aidger.utils.Translation._;
 
 import java.awt.Desktop;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.ImageIcon;
 import javax.swing.KeyStroke;
 
 import de.aidger.model.Runtime;
 import de.aidger.view.UI;
 
 /**
  * This action displays the help.
  * 
  * @author aidGer Team
  */
 @SuppressWarnings("serial")
 public class HelpAction extends AbstractAction {
 
     /**
      * Initializes the action.
      */
     public HelpAction() {
         putValue(Action.NAME, _("Help"));
         putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_H,
             ActionEvent.CTRL_MASK));
         putValue(Action.SHORT_DESCRIPTION, _("Display the help"));
         putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource(
             "/de/aidger/view/icons/question.png")));
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
      */
     @Override
     public void actionPerformed(ActionEvent e) {
         InputStream input = getClass().getResourceAsStream(
             "/de/aidger/pdf/Handbuch.pdf");
         try {
            File tempFile = File.createTempFile("manual", ".pdf");
             tempFile.deleteOnExit();
 
             FileOutputStream out = new FileOutputStream(tempFile);
             byte buffer[] = new byte[1024];
             int len;
             while ((len = input.read(buffer)) > 0) {
                 out.write(buffer, 0, len);
             }
 
             out.close();
             input.close();
 
             /*
              * Open the manual in the specified pdf viewer or try the default
              * one.
              */
             try {
                 java.lang.Runtime.getRuntime().exec(
                     new String[] {
                             Runtime.getInstance().getOption("pdf-viewer"),
                             tempFile.getAbsolutePath() });
             } catch (IOException ex) {
                 try {
                     Desktop.getDesktop().open(tempFile);
                 } catch (IOException e1) {
                     UI.displayError(_("No pdf viewer could be found!"));
                 }
             }
         } catch (IOException ex) {
             Logger.getLogger(HelpAction.class.getName()).log(Level.SEVERE,
                 null, ex);
         }
 
     }
 
 }
