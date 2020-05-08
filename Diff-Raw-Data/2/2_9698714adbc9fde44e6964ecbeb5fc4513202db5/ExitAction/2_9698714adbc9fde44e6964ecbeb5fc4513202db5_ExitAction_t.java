 package org.sankozi.rogueland.gui.actions;
 
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 import org.apache.log4j.Logger;
 import org.sankozi.rogueland.gui.MainFrame;
 
 import javax.swing.*;
 import java.awt.event.ActionEvent;
 
 /**
  * ExitAction
  *
  * @author sankozi
  */
 public class ExitAction extends AbstractAction {
     private final static Logger LOG = Logger.getLogger(ExitAction.class);
 
     private final Provider<MainFrame> mainFrameProvider;
 
     {
        this.putValue(Action.NAME, "Exit");
     }
 
     @Inject
     public ExitAction(Provider<MainFrame> mainFrameProvider){
         this.mainFrameProvider = mainFrameProvider;
     }
 
     @Override
     public void actionPerformed(ActionEvent e) {
         mainFrameProvider.get().dispose();
         System.exit(0);
     }
 }
