 package j3chess.gui;
 
 import j3chess.J3ChessApp;
 import j3chess.controller.HumanController;
 import j3chess.controller.Player;
 
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.EnumSet;
 
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JTextField;
 import javax.swing.WindowConstants;
 
 /**
  * Class to create the new GameDialog Starts a new game and sets the player
  * Names.
  */
 public class NewGameDialog extends JFrame {
     /**
      * @Brief Value to put something into the left Column of the Gridbaglayout
      */
     private static final int LEFT = 0;
     /**
      * @Brief Value to put something into the right Column of the Gridbaglayout
      */
     private static final int RIGHT = 1;
     /**
      * @Brief insets for the GridbagLayout
      */
     private static final Insets INSETS = new Insets(3, 3, 3, 3);
     /**
      * @Brief With of the JTextfields
      */
     private static final int TEXTFIELDCOLUMNWIDTH = 15;
 
     /**
      * @Brief Enum of all players
      */
     private final EnumSet<Player> mPlayers = EnumSet.allOf(Player.class);
 
     /**
      * Constructor - Creates the new Gamedialog.
      */
     public NewGameDialog() {
         super("Start new Game");
         setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 
        // Fenstergroesse festlegen
        // setPreferredSize(new Dimension(800,1000));

         // Fensterposition festlegen
        setLocation(150, 150);
 
         // HauptGridLayout 2 columns
         final GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] { 150, 150 };
         setLayout(gridBagLayout);
 
         final JLabel[] playerLabels = new JLabel[mPlayers.size()];
         final JTextField[] playerNameTextFields = new JTextField[mPlayers
                 .size()];
         for (int i = 0; i < playerLabels.length; i++) {
             playerLabels[i] = new JLabel("Player " + (i + 1));
             playerNameTextFields[i] = new JTextField();
             playerNameTextFields[i].setColumns(TEXTFIELDCOLUMNWIDTH);
         }
         final JButton jButtonOk = new JButton("OK");
         final JButton jButtonCancel = new JButton("Cancel");
 
         jButtonOk.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(final ActionEvent e) {
                 for (final Player player : mPlayers) {
                     String playerName = playerNameTextFields[player.ordinal()]
                             .getText();
                     if (playerName.equals("")) {
                         playerName = ("Player " + (player.ordinal() + 1));
                     }
                     player.setName(playerName);
                     player.setPlayerController(new HumanController(player));
                 }
                 // Start Game
                 J3ChessApp.getInstance().startNewGame();
                 dispose();
             }
         });
 
         jButtonCancel.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(final ActionEvent e) {
                 dispose();
             }
         });
 
         int row = 0;
         for (int i = 0; i < playerLabels.length; i++) {
             addComponentToGrid(playerLabels[i], LEFT, row);
             addComponentToGrid(playerNameTextFields[i], RIGHT, row);
             row++;
         }
         addComponentToGrid(jButtonOk, LEFT, row);
         addComponentToGrid(jButtonCancel, RIGHT, row);
         setVisible(true);
         pack();
     }
 
     /**
      * @Brief adds a JComponent to the GridbagLayout
      * @param component
      *            the jComponent to add
      * @param column
      *            column to add - Left or Right
      * @param row
      *            - row to insert the component
      */
     private void addComponentToGrid(final JComponent component,
             final int column, final int row) {
         final GridBagConstraints gbc = new GridBagConstraints();
         gbc.insets = INSETS;
         gbc.gridx = column;
         gbc.gridy = row;
         gbc.fill = GridBagConstraints.BOTH;
         add(component, gbc);
     }
 }
