 package tcsgeditor;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.UIManager;
 
 /**
  *
  * @author solkin
  */
 public class TCSGEditor {
 
   public static MainFrame mainFrame;
 
   /**
    * @param args the command line arguments
    */
   public static void main( String[] args ) {
     try {
       /*LookAndFeelInfo[] lafi = UIManager.getInstalledLookAndFeels();
        for ( int c = 0; c < lafi.length; c++ ) {
        if ( lafi[c].getName().equals( "GTK+" ) ) {
        UIManager.setLookAndFeel( lafi[c].getClassName() );
        break;
        }
        }*/
       // UIManager.setLookAndFeel( "com.jgoodies.looks.plastic.PlasticXPLookAndFeel" );
       // UIManager.setLookAndFeel( "com.pagosoft.plaf.PgsLookAndFeel" );
       UIManager.setLookAndFeel( "com.lipstikLF.LipstikLookAndFeel" );
       // UIManager.setLookAndFeel( "net.infonode.gui.laf.InfoNodeLookAndFeel" );
    } catch ( Throwable ex ) {
       Logger.getLogger( TCSGEditor.class.getName() ).log( Level.SEVERE, null, ex );
     }
     mainFrame = new MainFrame();
     mainFrame.setVisible( true );
   }
 }
