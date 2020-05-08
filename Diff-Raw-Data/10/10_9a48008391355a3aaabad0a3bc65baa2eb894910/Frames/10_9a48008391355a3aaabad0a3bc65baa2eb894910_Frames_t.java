 package terminator;
 
 import e.gui.*;
 import e.util.*;
 import java.awt.event.*;
 import java.util.*;
 import javax.swing.*;
 
 /**
  * Ensures that, on Mac OS, we always have our menu bar visible, even
  * when there are no terminal windows open. We use a dummy window with
  * a copy of the menu bar attached. When no other window has the focus,
  * but the application is focused, this hidden window gets the focus,
  * and its menu is used for the screen menu bar.
  */
 public class Frames {
     private ArrayList<TerminatorFrame> list = new ArrayList<TerminatorFrame>();
     private JFrame hiddenFrame; // Mac OS X only.
     
     public Frames() {
     }
     
     private synchronized void initHiddenFrame() {
         if (hiddenFrame == null) {
             String name = "Mac OS Hidden Frame";
             hiddenFrame = new JFrame(name);
             hiddenFrame.setName(name);
             hiddenFrame.setJMenuBar(new TerminatorMenuBar());
             hiddenFrame.setUndecorated(true);
             // Move the window off-screen so that when we're forced to setVisible(true) it doesn't actually disturb the user.
             hiddenFrame.setLocation(new java.awt.Point(-100, -100));
         }
     }
     
     private JFrame getHiddenFrame() {
         initHiddenFrame();
         return hiddenFrame;
     }
     
     public void addFrame(final TerminatorFrame frame) {
             list.add(frame);
             // Make the hidden frame invisible so that Mac OS won't give it the focus if the user hits C-` or C-~.
             if (GuiUtilities.isMacOs()) {
                     WindowMenu.getSharedInstance().addWindow(frame);
                     frameStateChanged();
             }
             frame.addWindowListener(new WindowAdapter() {
                     @Override
                     public void windowOpened(WindowEvent event) {
                             frameStateChanged();
                     }
 
                     @Override
                     public void windowClosed(WindowEvent event) {
                             Log.warn("removing frame: " + frame);
                             removeFrame(frame);
                     }
 
                     @Override
                     public void windowIconified(WindowEvent event) {
                             frameStateChanged();
                     }
 
                     @Override
                     public void windowDeiconified(WindowEvent event) {
                             frameStateChanged();
                     }
             });
     }
     
        public void removeFrame(TerminatorFrame frame) {
                list.remove(frame);
                if (GuiUtilities.isMacOs())
                        frameStateChanged();
         }
     
     public void frameStateChanged() {
             if (!GuiUtilities.isMacOs())
                     return;
 
             boolean noFramesVisible = true;
             for (TerminatorFrame frame : list)
                     noFramesVisible = noFramesVisible && !frame.isShowingOnScreen();
             getHiddenFrame().setVisible(noFramesVisible);
     }
     
     public boolean isEmpty() {
         return list.isEmpty();
     }
 
     public boolean closeAll() {
             // We need to copy frames as we will be mutating it.
             for (TerminatorFrame frame : new ArrayList<TerminatorFrame>(list))
                     frame.handleWindowCloseRequestFromUser();
             return isEmpty();
     }
 }
