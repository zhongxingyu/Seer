 package com.cmendenhall;
 
 import javax.swing.*;
 import javax.swing.border.Border;
 import javax.swing.plaf.basic.BasicBorders;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableColumn;
 import javax.swing.table.TableColumnModel;
 import java.awt.*;
 import java.util.Enumeration;
 import java.util.List;
 
 import static com.cmendenhall.TicTacToeSymbols.*;
 
 public class SwingView extends JFrame {
     static final int WIDTH = 350;
     static final int HEIGHT = 700;
 
     public SwingView() {
         setSize(WIDTH, HEIGHT);
         setResizable(false);
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         JPanel contentPane = (JPanel)getContentPane();
         contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
 
         add(new BoardPanel());
         add(new MessagePanel());
         add(new ConfigPanel());

        setVisible(true);
     }
 
     public class MessagePanel extends JPanel {
 
         public MessagePanel() {
             setName("messagePanel");
 
             JLabel label = new JLabel("Welcome to Tic-Tac-Toe");
             label.setName("messagePanelLabel");
 
             add(label);
         }
 
     }
 
     public class BoardPanel extends JPanel {
         private JTable boardTable;
 
         public BoardPanel() {
             setName("boardPanel");
 
             Dimension defaultSize = getSize();
             defaultSize.height = 150;
             setPreferredSize(defaultSize);
 
             boardTable = new JTable();
             loadBoard(new GameBoard());
             boardTable.setName("boardTable");
             boardTable.setShowVerticalLines(true);
             boardTable.setShowHorizontalLines(true);
             boardTable.setGridColor(new Color(0, 0, 0));
             boardTable.setColumnSelectionAllowed(false);
             boardTable.setRowSelectionAllowed(false);
 
             int numberOfColumns = boardTable.getColumnModel().getColumnCount();
             TableColumnModel columnModel = boardTable.getColumnModel();
 
             for (int column=0; column < numberOfColumns; column++) {
                 TableColumn currentColumn = columnModel.getColumn(column);
                 currentColumn.setPreferredWidth(50);
             }
 
             boardTable.setRowHeight(50);
 
             JLabel boardLabel = new JLabel("Welcome to ttt");
             boardLabel.setLabelFor(this);
 
             add(boardTable);
         }
 
         public void loadBoard(Board board) {
             List<Row> rows = board.getRows();
             DefaultTableModel tableData = new DefaultTableModel(rows.size(), rows.size());
 
             for (int row=0; row < rows.size(); row++) {
                 Row boardRow = rows.get(row);
                 int[] squares = boardRow.getSquares();
                 for (int col=0; col < squares.length; col++) {
                     String square = boardRow.intToSymbol(squares[col]);
                     tableData.setValueAt(square, row, col);
                 }
             }
 
             boardTable.setModel(tableData);
 
         }
 
     }
 
     public class ConfigPanel extends JPanel {
 
         public ConfigPanel() {
             setName("configPanel");
 
             Dimension defaultSize = getSize();
             defaultSize.width = 200;
             setPreferredSize(defaultSize);
 
             add(new GameActionPanel());
             add(new BoardConfigPanel());
             add(new PlayerConfigPanel("Player One", "playerOneConfigPanel"));
             add(new PlayerConfigPanel("Player Two", "playerTwoConfigPanel"));
         }
 
         public class GameActionPanel extends JPanel {
 
             public GameActionPanel() {
                 setName("gameActionPanel");
 
                 JButton newGameButton = new JButton("New game");
                 newGameButton.setName("newGameButton");
                 add(newGameButton);
             }
 
         }
 
         public class PlayerConfigPanel extends JPanel {
             private ButtonGroup playerButtons;
             private JRadioButton humanButton;
             private JRadioButton computerButton;
 
             public PlayerConfigPanel(String label, String name) {
                 setName(name);
 
                 playerButtons = new ButtonGroup();
 
                 JLabel playerLabel = new JLabel(label);
                 playerLabel.setName("playerLabel");
 
                 humanButton = new JRadioButton("Human", true);
                 humanButton.setName("humanButton");
 
                 computerButton = new JRadioButton("Computer", false);
                 computerButton.setName("computerButton");
 
                 playerButtons.add(humanButton);
                 playerButtons.add(computerButton);
 
                 add(playerLabel);
                 add(humanButton);
                 add(computerButton);
             }
 
             public boolean humanSelected() {
                 ButtonModel selectedModel = playerButtons.getSelection();
                 ButtonModel humanModel = humanButton.getModel();
                 return selectedModel.equals(humanModel);
             }
         }
 
         public class BoardConfigPanel extends JPanel {
             private SpinnerNumberModel boardSizeSpinnerModel;
 
             public BoardConfigPanel() {
                 setName("boardConfigPanel");
 
                 boardSizeSpinnerModel = new SpinnerNumberModel(3, 3, 10, 1);
 
                 JSpinner boardSizeSpinner = new JSpinner(boardSizeSpinnerModel);
                 boardSizeSpinner.setName("boardSizeSpinner");
 
                 Dimension d = boardSizeSpinner.getPreferredSize();
                 d.width = 50;
                 boardSizeSpinner.setPreferredSize(d);
                 boardSizeSpinner.setValue(3);
 
                 JLabel spinnerLabel = new JLabel("Board size:");
                 spinnerLabel.setLabelFor(boardSizeSpinner);
 
                 add(spinnerLabel);
                 add(boardSizeSpinner);
             }
 
             public int boardSize() {
                 return (Integer)boardSizeSpinnerModel.getValue();
             }
 
 
         }
 
 
 
     }
 }
