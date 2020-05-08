 package combat;
 //Source file: /home/stu2/s22/DevB/classes/KeyDialog.java
 /** 
 * @author DevC
 * @version $Id: Test.java,v 1.2 2012/04/08 03:50:18 DevA Exp $
 *
 * KeyDialog allows users the option to map key commands to their liking
 *
 * Revision History:
 *   $Log: Test.java,v $
 *   Revision 1.2  2012/04/08 03:50:18  DevA
 *   Cleaned up the code to run with Java 1.6: removed unused imports,
 *   fixed some UI focus issues (introduced by new focus "features" in Java since
 *   our original implementation), and made the CommandInterpreter not a Singleton
 *
 *   Revision 1.1  2000/05/09 14:05:46  DevC
 *   Initial revision
 *
 *
 *
 *
 */
 
 import javax.swing.*;
 
 public class Test {
      
       
     /**
      * Constructor
      * creates this dialog
      *
      * @param JFrame  the frame of the game (which I am part of)
      */
     public static void main( String[] argv ) {
       JFrame aframe = new JFrame();
       
      KeyDialog testd = new KeyDialog( aframe, new PlayerManager(0, null, null, null, null), new PlayerManager(0, null, null, null, null) );
       
       aframe.setVisible(true);
       testd.setVisible(true);
       
     }
 
 
 }
