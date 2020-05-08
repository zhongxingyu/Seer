 package rocks6205.editor.mvc;
 
 //~--- non-JDK imports --------------------------------------------------------
 
 import rocks6205.system.properties.OSValidator;
 
 //~--- JDK imports ------------------------------------------------------------
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.swing.JFrame;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 
 /**
  * Execute main function.
  *
  * @author Cheow Yeong Chi
  * @since 1.0
  *
  */
 public class Main {
     public static void main(String[] rcks) {
         LSSplashScreen splash = new LSSplashScreen(5000);
 
         splash.showSplash();
 
         SVGEditorView           v = new SVGEditorView();
         SVGEditorViewController c = new SVGEditorViewController();
 
         setUpLookAndFeel();
         v.setController(c);
         v.setVisible(true);
         v.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 
         if (rcks.length > 0) {
             try {
                 c.fileLoad(new File(rcks[0]));
             } catch (IOException e) {
 
                 // TODO: Display/log error message
             }
         }
     }
 
     /**
      * Setup look and feel for GUI.
      * @param style <code>String</code> containing the name
      */
     private static void setUpLookAndFeel() {
         if (OSValidator.isWindows()) {
             try {
                 for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                     if ("Nimbus".equals(info.getName())) {
                         UIManager.setLookAndFeel(info.getClassName());
                         System.out.println(UIManager.getLookAndFeel().getName());
 
                         break;
                     }
                 }
             } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                      | UnsupportedLookAndFeelException e) {}
         }
     }
 }
