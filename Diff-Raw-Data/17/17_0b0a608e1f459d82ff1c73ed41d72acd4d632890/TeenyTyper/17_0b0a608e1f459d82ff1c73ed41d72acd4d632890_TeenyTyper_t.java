 /**
  * TeenyTyper by Lindsay Bradford is licensed under a 
  * Creative Commons Attribution-ShareAlike 3.0 Unported License.
  *
  * Year: 2012 
  */
 
 package blacksmyth.teenytyper;
 
 import javax.swing.UIManager;
 
public class TeenyTyper {
 
   private static final long serialVersionUID = 1L;
 
   /**
    * @param args
    */
   public static void main(String[] args) {
     try {
       UIManager.setLookAndFeel(
           "javax.swing.plaf.metal.MetalLookAndFeel"
         );
     } catch (Exception e) {
       e.printStackTrace();
     }
     
     TeenyTyperWindowFactory.createJFrame().setVisible(true);
   }
 }
