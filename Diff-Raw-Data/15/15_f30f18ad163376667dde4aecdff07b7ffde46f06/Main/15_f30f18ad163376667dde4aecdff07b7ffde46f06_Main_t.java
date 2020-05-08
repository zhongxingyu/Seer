 import javax.swing.UIManager;
 import javax.swing.UIManager.LookAndFeelInfo;
 
 public class Main {
     static GUI2 gui;
 
     public static void main(String[] args) {
         try {
             for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                 if ("Nimbus".equals(info.getName())) { //$NON-NLS-1$
                     UIManager.setLookAndFeel(info.getClassName());
                     break;
                 }
             }
         } catch (Exception e) {
             // If Nimbus is not available, you can set the GUI to another look
             // and feel.
         }
         gui = new GUI2("en"); //$NON-NLS-1$
         gui.initGUI();
     }
 
     public static void redrawGUI(String lang) {
        GUI2 oldgui = gui;
         gui = new GUI2(lang);
         gui.initGUI();
        oldgui.dispose();
 
     }
 }
