 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.awt.GraphicsDevice;
 import java.awt.GraphicsEnvironment;
 
 import javax.swing.JTabbedPane;
 import javax.swing.JOptionPane;
 
 import org.fife.ui.rtextarea.*;
 import org.fife.ui.rsyntaxtextarea.*;
 
 public class JotEventEngine {
     public static boolean lineWrap = true;
     public static boolean fullScreen = false;
     public static boolean menuBarVisible = true;
     public static boolean consoleVisible = true;
 
     public static int currentSpaces = 4;
 
     // what?
     public class JU extends JotUtilities { }
 
     public static void actionEvents(ActionEvent e) {
         // File Menu Items
         if (e.getActionCommand().equalsIgnoreCase("new file")) {
             JotFile.newFile();
         } else if (e.getActionCommand().equalsIgnoreCase("open file")) {
            JotFile.openFile();
         } else if (e.getActionCommand().equalsIgnoreCase("save")) {
             JotFile.saveFile();
         } else if (e.getActionCommand().equalsIgnoreCase("save as")) {
             JotFile.saveFileAs();
         } else if (e.getActionCommand().equalsIgnoreCase("close file")) {
             JotFile.closeFile(1);
         } else if (e.getActionCommand().equalsIgnoreCase("exit")) {
             System.exit(0);
         }
 
         // Edit Menu Items
         JotDocument wdoc = (JotDocument) JotEngine.tabbedPane.getSelectedComponent();
         if (e.getActionCommand().equalsIgnoreCase("undo")) {
             wdoc.getText().undoLastAction();
         } else if (e.getActionCommand().equalsIgnoreCase("redo")) {
             wdoc.getText().redoLastAction();
         } else if (e.getActionCommand().equalsIgnoreCase("copy")) {
             wdoc.getText().copy();
         } else if (e.getActionCommand().equalsIgnoreCase("cut")) {
             wdoc.getText().cut();
         } else if (e.getActionCommand().equalsIgnoreCase("paste")) {
             wdoc.getText().paste();
         } else if (e.getActionCommand().equalsIgnoreCase("select all")) {
             wdoc.getText().selectAll();
         }
 
         // View Menu Items
         if (e.getActionCommand().equalsIgnoreCase("toggle full screen")) {
             if (fullScreen == true) {
                 JotEngine.frame.dispose();
                 JotEngine.frame.setResizable(true);
                 JotEngine.frame.setUndecorated(false);
                 JotEngine.frame.setVisible(true);
 
                 fullScreen = false;
                 JotComponents.fullScreen.setSelected(false);
             } else if (fullScreen == false) {
                 GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
                 GraphicsDevice dev = env.getDefaultScreenDevice();
                 JotEngine.frame.dispose();
                 JotEngine.frame.setResizable(false);
                 JotEngine.frame.setUndecorated(true);
                 dev.setFullScreenWindow(JotEngine.frame);
 
                 fullScreen = true;
                 JotComponents.fullScreen.setSelected(true);
             }
         } else if (e.getActionCommand().equalsIgnoreCase("hide menu")) {
             JotComponents.menuBar.setVisible(false);
         } else if (e.getActionCommand().equalsIgnoreCase("hide console")) {
             if (consoleVisible == true) {
                 consoleVisible = false;
                 JotEngine.console.setVisible(false);
             } else if (consoleVisible == false) {
                 consoleVisible = true;
                 JotEngine.console.setVisible(true);
             }

            // a fix?
            JotEngine.frame.revalidate();
         }
 
         // Option Menu Items
         // JotDocument wdoc = (JotDocument) JotEngine.tabbedPane.getSelectedComponent();
         if (e.getActionCommand().equalsIgnoreCase("toggle linewrap")) {
             if (lineWrap == true) {
                 lineWrap = false;
                 JotComponents.toggleLineWrap.setSelected(false);
                 wdoc.getText().setLineWrap(false);
             } else if (lineWrap == false) {
                 lineWrap = true;
                 JotComponents.toggleLineWrap.setSelected(true);
                 wdoc.getText().setLineWrap(true);
             }
         }
         // Tab Menu
         else if (e.getActionCommand().equalsIgnoreCase("tab width 2")) {
             wdoc.getText().setTabSize(2);
             JotEngine.docInfoStrip.updateStrip(2);
         } else if (e.getActionCommand().equalsIgnoreCase("tab width 4")) {
             wdoc.getText().setTabSize(4);
             JotEngine.docInfoStrip.updateStrip(4);
         } else if (e.getActionCommand().equalsIgnoreCase("tab width 8")) {
             wdoc.getText().setTabSize(8);
             JotEngine.docInfoStrip.updateStrip(8);
         }  else if (e.getActionCommand().equalsIgnoreCase("convert indentation to tabs")) {
             wdoc.getText().convertTabsToSpaces();
         } else if (e.getActionCommand().equalsIgnoreCase("convert indentation to spaces")) {
             wdoc.getText().convertSpacesToTabs();
         } else if (e.getActionCommand().equalsIgnoreCase("hex to rgb")) {
             String text = wdoc.getText().getSelectedText();
             String str = JOptionPane.showInputDialog(
                 JotEngine.frame, "Enter the Hex Color: ", wdoc.getText().getSelectedText());
             if (str != null) {
                 JotEngine.console.setText("(" + JU.hexToR(str) + ", " + JU.hexToG(str) + ", " + JU.hexToB(str) + ")");
                 JOptionPane.showMessageDialog(JotEngine.frame,
                     "(R: " + JU.hexToR(str) + ", G: " + JU.hexToG(str) + ", B: " + JU.hexToB(str) + ")",
                     "RGB From HEX", 1);
             }
         }
 
         // Tab Popup Menu
         else if (e.getActionCommand().equalsIgnoreCase("close")) {
             JotFile.closeFile(1);
         } else if (e.getActionCommand().equalsIgnoreCase("close all except this")) {
             JotFile.closeFile(2);
         } else if (e.getActionCommand().equalsIgnoreCase("close tabs to the right")) {
             JotFile.closeFile(3);
         }
         // While in this context, this has to be case sensitive.. else it will call the File Menu too
         else if (e.getActionCommand().equalsIgnoreCase("new")) {
             JotFile.newFile();
         } else if (e.getActionCommand().equalsIgnoreCase("open")) {
             JotFile.openFile();
         }
     }
 
     public static void mouseClickEvents(MouseEvent e) {
         JotDocument doc = (JotDocument) JotEngine.tabbedPane.getSelectedComponent();
         if (doc.getPath() == null) {
             updateNameJEE(doc.getName() + " - " + JotEngine.windowCaption);
         } else {
             updateNameJEE(doc.getPath() + doc.getName() + " - " + JotEngine.windowCaption);
         }
 
         JotEngine.statusStrip.setText(" " + doc.getName());
     }
 
     // private int oldIx = -1;
     public static void mouseReleasedEvents(MouseEvent e) {
         JTabbedPane tp = (JTabbedPane) e.getSource();
         int ix = tp.indexAtLocation(e.getX(), e.getY());
         // if (ix == oldIx) return;
         // oldIx = ix;
         // if (ix == -1) return;
 
         if (e.getButton() == MouseEvent.BUTTON3) {
             try {
                 String pTitle = tp.getTitleAt(ix);
                 JotComponents.tabPopupMenu.show( (JTabbedPane) e.getComponent(), e.getX(), e.getY());
             } catch (ArrayIndexOutOfBoundsException ae) {
                 return;
            }
         } else if (e.getButton() == MouseEvent.BUTTON2) {
             JotFile.closeFile(1);
         }
     }
 
     public static void keyTypedEvents(KeyEvent e) { }
 
     public static void keyPressedEvents(KeyEvent e) {
         if (e.getKeyCode() == KeyEvent.VK_K && e.isControlDown() == true) {
             if (menuBarVisible == true) {
                 JotComponents.menuBar.setVisible(false);
                 menuBarVisible = false;
             } else if (menuBarVisible == false) {
                 JotComponents.menuBar.setVisible(true);
                 menuBarVisible = true;
             }
         }
     }
 
     public static void updateNameJEE(String n) {
         JotEngine.frame.setTitle(n);
     }
 }
