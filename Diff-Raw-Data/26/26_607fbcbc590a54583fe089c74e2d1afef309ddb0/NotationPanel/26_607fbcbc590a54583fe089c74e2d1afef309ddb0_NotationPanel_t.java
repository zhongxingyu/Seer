 package j3chess;
 
 
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.util.EnumSet;
 
 import javax.swing.JComponent;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
import javax.swing.SwingConstants;
 import javax.swing.table.DefaultTableModel;
 
 /**
  * Class to create the Notationpanel for all moves.
  */
 public class NotationPanel extends JPanel {
 
     private static final int LEFT = 0;
     private static final int MIDDLE = 1;
     private static final int RIGHT = 2;
 
     private static final Insets INSETS = new Insets(3, 3, 3, 3);
 
     int mCurrentPlayer = 0;
 
     /**
      * @brief notationTable to show all moves
      */
     private JTable mNotationTable;
 
 
     /**
      * The constructor for the NotationPanel for all moves.
      * @param sizeX weight of the Notation Panel
      * @param sizeY height of the Notation Panel
      */
     public NotationPanel(final int sizeX, final int sizeY) {
         setPreferredSize(new Dimension(sizeX, sizeY));
 
         // Initialize layout manager
         GridBagLayout gridBagLayout = new GridBagLayout();
 
         setLayout(gridBagLayout);
 
         this.setLayout(new GridBagLayout());
         JComponent notationTable = createNotationTable();
 
         int row = 0;
         int column = LEFT;
         GridBagConstraints gridBagConstraints = new GridBagConstraints();
         gridBagConstraints.insets = INSETS;
         gridBagConstraints.gridx = column;
         gridBagConstraints.gridy = row;
         gridBagConstraints.anchor = GridBagConstraints.NORTH;
         gridBagConstraints.fill = GridBagConstraints.BOTH;
         gridBagConstraints.weightx = sizeX;
         gridBagConstraints.weighty = sizeY;
         add(notationTable, gridBagConstraints);
     }
 
     /** @Brief Creates the NotationTable with Player Names
      *  @return Srcoll pane with the NotationPanel
      * */
     private JComponent createNotationTable() {
         String[] columnNames = {Player.ONE.toString(), Player.TWO.toString(), Player.THREE.toString()};
         mNotationTable = new JTable(new DefaultTableModel(null, columnNames));
         mNotationTable.getTableHeader().setDefaultRenderer(new NotationPanelHeader());
         JScrollPane tableSrcollPane = new JScrollPane(mNotationTable);
         mNotationTable.setFillsViewportHeight(true);
         return tableSrcollPane;
     }
 
     /**
      * @brief adds a move at the end of the NotationPanel.
      * @param move the move to add
      */
     public final void addMove(final String move) {
         DefaultTableModel tableModel = (DefaultTableModel) mNotationTable.getModel();
         int moveCounter = J3ChessApp.getInstance().getGame().getMoveCounter();
         if (moveCounter % J3ChessApp.NUMBEROFPLAYERS == 0) {
             tableModel.addRow(new Object[]{move});
         } else {
             tableModel.setValueAt(move, (Integer) moveCounter / J3ChessApp.NUMBEROFPLAYERS, moveCounter % J3ChessApp.NUMBEROFPLAYERS);
         }
     }
 
     /**
      * @brief Sets the current player to be displayed.
      */
     public final void refreshCurrentPlayer() {
         mCurrentPlayer = J3ChessApp.getInstance().getGame().getCurrentPlayerID();
         mNotationTable.getTableHeader().repaint();
     }
 
     /**
      * @brief Clears out the contents of the NotationPanel.
      */
     public final void reset() {
         DefaultTableModel tableModel = (DefaultTableModel) mNotationTable.getModel();
         tableModel.setColumnCount(0);
         EnumSet<Player> mPlayers = EnumSet.allOf(Player.class);
         tableModel.setColumnIdentifiers(mPlayers.toArray());
         repaint();
     }
 }
