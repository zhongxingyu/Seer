 package App.view;
 
 import App.model.Game;
 
 import javax.swing.*;
 import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
 import java.util.Map;
 
 /**
  * This class controls the viewing pane. The different screens are controlled
  * by components that use the instance of this Display as a parameter to link
  * to the changeCard() method.
  * @author Mark McDonald, Andrew Wilder
  */
 public class Display extends JFrame {
 
     /**
 	 * Prevents "serializable" warning
 	 */
 	private static final long serialVersionUID = 5472341215748317058L;
 
 	public Display() {
 		this.setup();
 	}
 
     private static JPanel CenterPanel, mainContentPanel;
     private static Game game;
     private static MiniGameScreen MiniGameView;
     // We need this to be able to update cards instead of creating new ones every time
     private static Map<String, Screen> cardMap;
 
     /**
      * set up the initial screen
      */
     public void setup() {
         setTitle("SpaceFarmer 3000");
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         setBounds(100, 100, 800, 600);
         mainContentPanel = new JPanel();
         mainContentPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
         
         setContentPane(mainContentPanel);
         mainContentPanel.setLayout(new CardLayout(0, 0));
         
         JPanel StandardView = new JPanel();
         mainContentPanel.add(StandardView, "Standard");
         StandardView.setLayout(new BorderLayout(0, 0));
         
         JPanel TopPanel = new JPanel();
         StandardView.add(TopPanel, BorderLayout.NORTH);
         TopPanel.setBackground(Color.GREEN);
         TopPanel.setLayout(new BoxLayout(TopPanel, BoxLayout.X_AXIS));
 
         JLabel lblWip = new JLabel("WIP");
         TopPanel.add(lblWip);
 
         Component verticalStrut = Box.createVerticalStrut(80);
         TopPanel.add(verticalStrut);
 
         JPanel BottomPanel = new JPanel();
         StandardView.add(BottomPanel, BorderLayout.SOUTH);
         BottomPanel.setBackground(Color.MAGENTA);
         BottomPanel.setLayout(new BoxLayout(BottomPanel, BoxLayout.X_AXIS));
 
         JLabel label_1 = new JLabel("WIP");
         BottomPanel.add(label_1);
 
         Component verticalStrut_1 = Box.createVerticalStrut(80);
         BottomPanel.add(verticalStrut_1);
 
         JPanel LeftPanel = new NavigationSidePanel();
         StandardView.add(LeftPanel, BorderLayout.WEST);
         LeftPanel.setBackground(Color.CYAN);
 
         Component horizontalStrut = Box.createHorizontalStrut(100);
         LeftPanel.add(horizontalStrut);
 
         JPanel RightPanel = new JPanel();
         StandardView.add(RightPanel, BorderLayout.EAST);
         RightPanel.setBackground(Color.PINK);
         RightPanel.setLayout(new BoxLayout(RightPanel, BoxLayout.Y_AXIS));
 
         JLabel label_2 = new JLabel("WIP");
         label_2.setAlignmentX(Component.CENTER_ALIGNMENT);
         label_2.setAlignmentY(0.0f);
         RightPanel.add(label_2);
 
         Component horizontalStrut_1 = Box.createHorizontalStrut(100);
         RightPanel.add(horizontalStrut_1);
 
         CenterPanel = new JPanel();
         StandardView.add(CenterPanel, BorderLayout.CENTER);
         CenterPanel.setBackground(Color.ORANGE);
         CenterPanel.setLayout(new CardLayout(0, 0));
         
         MiniGameView = new MiniGameScreen();
         mainContentPanel.add(MiniGameView, "MiniGame");
 
         cardMap = new HashMap<String, Screen>();
         // Generate every possible card
         for (CardName name : CardName.values()){
             cardMap.put(name.toString(),name.getScreen());
             CenterPanel.add(name.toString(),cardMap.get(name.toString()));
         }
     }
     
     /**
      * Flips to the specified card in the center panel.
      * @param name The card ID of the panel to flip to.
      */
     public static void changeCard(CardName name) {
     	((CardLayout)CenterPanel.getLayout()).show(CenterPanel, name.toString());
     }
     
     /**
      * Initiate the minigame
      */
     public static void playMiniGame() {
     	MiniGameView.startGame();
     	((CardLayout)mainContentPanel.getLayout()).show(mainContentPanel, "MiniGame");
     	MiniGameView.requestFocus();
     }
     
     /**
      * Return from minigame
      */
     public static void exitGame(boolean won) {
     	((CardLayout)mainContentPanel.getLayout()).show(mainContentPanel, "Standard");
     	if(won) {
     		// TODO code for winning
     	} else {
     		// TODO code for losing
     	}
     }
 
     //--Accessors and Modifiers
 
     public static Screen getCard(String cardName){
         return cardMap.get(cardName);
     }
 
     public static Game getGame() {
         return game;
     }
 
     public static void setGame(Game game) {
         Display.game = game;
     }
 
     public static JPanel getCenterPanel() {
         return CenterPanel;
     }
 
     public static JPanel getMainContentPanel(){
         return mainContentPanel;
     }
 
 }
