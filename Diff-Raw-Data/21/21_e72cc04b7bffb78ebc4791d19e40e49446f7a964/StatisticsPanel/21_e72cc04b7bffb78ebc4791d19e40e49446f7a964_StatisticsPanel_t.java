 package galapagos;
 
 import java.awt.*;
 import java.util.*;
 import java.util.List;
 
 import javax.swing.*;
 
 public class StatisticsPanel extends JPanel implements Observer {
     //The number of columns in the output-table, computed by the number of StatisticElements
     private static final int COLUMNS;
     
     // A label containing the title "Rounds:"
     private static final JLabel ROUNDS_TITLE_LABEL;
     
     //The top row of the table, containing headers of the table columns.
     private static final JLabel[] TITLE_ROW;
     
     private static final Font font = new Font("InputDialog", Font.BOLD, 14);
     private static final Font roundsFont = new Font("InputDialog", Font.BOLD, 18);
     
     static {
         ROUNDS_TITLE_LABEL = new JLabel("Round number:  ");
         ROUNDS_TITLE_LABEL.setFont(roundsFont);
         
         //the number of columns is the number of statistic elements + one for the behaviornames
         Statistics.StatisticsElement[] statElements = Statistics.StatisticsElement.values();
         COLUMNS = 1 + statElements.length;
         
         TITLE_ROW = new JLabel[COLUMNS];
         
         //First column title
         TITLE_ROW[0] = new JLabel("Behavior");
         TITLE_ROW[0].setFont(font);
         
         //The statisticelement names next
         for(int i = 0; i < statElements.length; ++i) {
             TITLE_ROW[i+1] = new JLabel(statElements[i].name);
             TITLE_ROW[i+1].setHorizontalAlignment(SwingConstants.RIGHT);
             TITLE_ROW[i+1].setFont(font);
         }
 
     }
     
     private JLabel roundsLabel;
     private JLabel[][] informationLabels;
     private Map<Behavior, Color> colorMap;
     private int numberOfInformationRows;
     
     /**
      * Create a new StatisticsPanel for monitoring a Biotope.
     * @param colorMap The StatisticsPanel uses colorMap to color the statistics
     *  for a Behavior in the corresponding color.
      */
     public StatisticsPanel(Map<Behavior, Color> colorMap) {
         roundsLabel = new JLabel();
         roundsLabel.setFont(roundsFont);
         Container roundsBox = Box.createHorizontalBox();
         roundsBox.add(ROUNDS_TITLE_LABEL);
         roundsBox.add(roundsLabel);
         
         JPanel informationPanel = new JPanel(new GridBagLayout());
         
         numberOfInformationRows = colorMap.size() + 2;
         
         informationLabels = new JLabel[numberOfInformationRows][COLUMNS];
         informationLabels[0] = TITLE_ROW;
         
         for(int j = 0; j < COLUMNS; ++j)
         {
             informationPanel.add(informationLabels[0][j], getConstraints(j, 1));
         }
         
         for(int i = 1; i < numberOfInformationRows; ++i)    
         {
             JLabel[] row = new JLabel[COLUMNS];
             
             for(int j = 0; j < COLUMNS; ++j)
             {
                 row[j] = new JLabel();
                 row[j].setFont(font);
                 row[j].setHorizontalAlignment(SwingConstants.RIGHT);
                 informationPanel.add(row[j], getConstraints(j, i+1));
             }
             
             row[0].setHorizontalAlignment(SwingConstants.LEFT);
             
             informationLabels[i] = row;
         }
         
         this.setLayout(new GridBagLayout());
         this.add(roundsBox, getConstraints(0,0));
         this.add(informationPanel, getConstraints(0,1));
         
         this.colorMap = colorMap;
     }
     
     /**
      * A set of GridBagConstraints for use with the GridBagLayout.
      * @param x the horisontal position of the component.
      * @param y the vertical position of the component.
      */
     private GridBagConstraints getConstraints (int x, int y) {
         return new GridBagConstraints(x,y,1,1,1.0,1.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0);
     }
     
     public void update(Observable observableBiotope, Object arg) {
         Biotope biotope = (Biotope) observableBiotope;
         roundsLabel.setText(((Integer) biotope.round()).toString());
         
         List<Behavior> behaviors = biotope.behaviors();
         Statistics.StatisticsElement[] statElements = Statistics.StatisticsElement.values();
 
         int numberOfBehaviors = behaviors.size(); // The number of rows in the output table - including the header row and a row containing totals.
 
         // Array accumulating the data for the "totals"-row of the table.
         int totals[] = new int[statElements.length];
         
         { //iterate through behaviors and create a statistic rows
             int i = 0;
             for (Behavior currentBehavior : behaviors) {
                 // Variables containing data of the current row in the table (and the corresponding Behavior).
                 Statistics currentStat = biotope.statistics(currentBehavior);
                 JLabel[] currentRow = informationLabels[i+1];
                 
                 Color color = colorMap.get(currentBehavior);
                 currentRow[0].setText(currentBehavior.toString());
                 currentRow[0].setToolTipText(currentBehavior.description());
                 currentRow[0].setForeground(color);
                 currentRow[0].setVisible(true);
                 
                 for(Statistics.StatisticsElement element : statElements)
                 {
                     int value = currentStat.getStatByElement(element);
                     currentRow[element.ordinal()+1].setText(Integer.toString(value));
                     currentRow[element.ordinal()+1].setForeground(color);
                     currentRow[element.ordinal()+1].setVisible(true);
                     
                     
                     //The totals are accumulated.
                     totals[element.ordinal()] += value;
                 }
                 
                 if(currentStat.getStatByElement(Statistics.StatisticsElement.POPULATION) == 0)
                     currentRow[1].setText("EXTINCT");
                 
                 i++;
             }
         }
         
         JLabel[] totalsRow = informationLabels[numberOfBehaviors+1];
         totalsRow[0].setText("Totals");
         totalsRow[0].setForeground(Color.BLACK);
         totalsRow[0].setVisible(true);
         
         for(int i = 0; i < totals.length; ++i) {
             totalsRow[i+1].setText(Integer.toString(totals[i]));
             totalsRow[i+1].setForeground(Color.BLACK);
             totalsRow[i+1].setVisible(true);
         }
         
         if(totals[0] == 0)
             totalsRow[1].setText("APOCALYPSE");
         
         // Set the remaining rows to invisible.
         for(int i = numberOfBehaviors+2; i < numberOfInformationRows; ++i)
             for (int j = 0; j < COLUMNS; ++j)
                 informationLabels[i][j].setVisible(false);
     }
 
 }
