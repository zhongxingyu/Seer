 package org.i4qwee.chgk.trainer.view.dialogs;
 
 import org.i4qwee.chgk.trainer.controller.brain.manager.MainWindow;
 
 import javax.swing.*;
 import java.awt.*;
 
 /**
  * User: 4qwee
  * Date: 02.11.11
  * Time: 23:39
  */
 public abstract class AbstractDialog extends JDialog
 {
     AbstractDialog()
     {
         super();
 
         setAlwaysOnTop(true);
         setUndecorated(true);
     }
 
     public void showDialog()
     {
        pack();
         setLocationRelativeTo(MainWindow.getMainWindow());
 
         setVisible(true);
     }
 }
