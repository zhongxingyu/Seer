 package cu.cs.cpsc215.crazy_mail;
 
 import cu.cs.cpsc215.crazy_mail.data.DataStore;
 import cu.cs.cpsc215.crazy_mail.ui.MainFrame;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 
 /**
  *@author Kevin Jett
  * @author Emmanuel John
  * Main Driver. 
  *This is separated from the window and main frame to allow for easy debugging of other components.
  **/
 public class MainDriver {
 	public static void main(String[] args) {
 		System.out.println("Starting program");
                // DataStore ds = DataStore.initDataStore();//This will happen here so that all contacts are loaded
 		SwingUtilities.invokeLater(new Runnable(){
                     @Override
                     public void run(){
                         try{
                             UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
                         }catch(Exception ex){
                             try {
                                 UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                             } catch (Exception ex1) {
                                 Logger.getLogger(MainDriver.class.getName()).log(Level.SEVERE, null, ex1);
                             } 
                         }
                         //new EmailTransmissionDlg();
                         MainFrame.init();
                     }
                 });
		shutdown();
                 
 	}
 	
 	public static void shutdown()
 	{
 		DataStore.get().saveAll();
 		System.out.println("Shutting down");
 	}
 
 }
