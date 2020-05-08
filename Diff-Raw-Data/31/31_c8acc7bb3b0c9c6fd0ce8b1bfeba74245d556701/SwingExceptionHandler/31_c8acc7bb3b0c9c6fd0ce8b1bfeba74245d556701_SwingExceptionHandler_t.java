 package interiores.core.presentation.swing;
 
 import interiores.core.Debug;
 import javax.swing.JDialog;
 import javax.swing.JOptionPane;
 
 /**
  *
  * @author hector
  */
 public class SwingExceptionHandler
 {
     public static void enable() {
         Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
             @Override
             public void uncaughtException(Thread t, Throwable e) {
                 handleException(e);
             }
         });
     }
     
     public static void handleException(Throwable e) {
         Debug.println("Handling uncaught exception with SwingExceptionHandler");
         JOptionPane exceptionPane = new JOptionPane(e.getMessage(), JOptionPane.ERROR_MESSAGE);
         JDialog exceptionDialog = exceptionPane.createDialog("Something went wrong...");
         
         exceptionDialog.setAlwaysOnTop(true);
         exceptionDialog.setVisible(true);
        
        if(Debug.isEnabled())
            e.printStackTrace();
     }
 }
