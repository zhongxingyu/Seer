 package de.schelklingen2008.canasta.client.view;
 
 import javax.swing.BoxLayout;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import de.schelklingen2008.canasta.client.controller.Controller;
 import de.schelklingen2008.canasta.client.controller.GameChangeListener;
 import de.schelklingen2008.canasta.client.model.GameContext;
 import de.schelklingen2008.canasta.model.GameModel;
 import de.schelklingen2008.canasta.model.Player;
 
 /**
  * Displays a list of players and turn change information in a turn-based game.
  */
 public class TurnPanel extends JPanel implements GameChangeListener
 {
 
    private Controller controller;
 
     public TurnPanel(Controller controller)
     {
         this.controller = controller;
         controller.addChangeListener(this);
     }
 
     public void gameChanged()
     {
         removeAll();
 
         if (getGameModel() == null) return;
 
         setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 
         for (Player player : getGameModel().getPlayers())
         {
             String name = player.getName();
            add(new JLabel(name + "       " + player.getTotalScore()));
 
         }
 
         revalidate();
         repaint();
     }
 
     private GameContext getGameContext()
     {
         return controller.getGameContext();
     }
 
     private GameModel getGameModel()
     {
         return getGameContext().getGameModel();
     }
 }
