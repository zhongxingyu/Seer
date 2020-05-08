 package info.kulhan.reversi.view;
 
 import info.kulhan.reversi.controller.IController;
 import info.kulhan.reversi.model.GameState;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.SwingUtilities;
 
 /**
  *
  * @author admin
  */
 public class GUIView implements IView {
     /**
      * Game state
      */
     private GameState state;
     
     /**
      * Create new GUI view
      * @param s 
      */
     public GUIView(GameState s) {
         state = s;
     }
 
     /**
      * Show main game window
      * @param controller 
      */
     @Override
     public void showMain(final IController controller) {
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 MainPanel p = new MainPanel(state, controller);
                 
                 JFrame f = new JFrame();
                 
                 f.add(p);
                 f.setTitle("REVERSI");
                 f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                 f.pack();
                 f.setLocationRelativeTo(null);
                 f.setVisible(true);
             }
         });
     }
 
     /**
      * Show cannot save message dialog
      */
     @Override
     public void showCannotSave() {
         JOptionPane.showMessageDialog(null, "Nepodařilo se uložit hru.");
     }
 
     /**
      * Show cannot load message dialog
      */
     @Override
     public void showCannotLoad() {
         JOptionPane.showMessageDialog(null, "Nepodařilo se načíst hru.");
     }
 
    /**
     * Show select game type dialog
     */
     @Override
     public void showSelectGameType(IGameTypeSelector s) {
         String[] options = new String[GameState.Type.values().length];
         options[GameState.Type.ONE_PLAYER.ordinal()] = "Pro jednoho hráče";
         options[GameState.Type.TWO_PLAYER.ordinal()] = "Pro dva hráče";
         
         int n = JOptionPane.showOptionDialog(
             null,
             "Jakou chcete hrát hru?",
             "Typ hry",
             JOptionPane.DEFAULT_OPTION,
             JOptionPane.QUESTION_MESSAGE,
             null,
             (Object[]) options,
             null
         );
         
         if (n == -1) {
             n = GameState.Type.ONE_PLAYER.ordinal();
         }
         
         s.gameTypeSelected(GameState.Type.values()[n]);
     }
     
 }
