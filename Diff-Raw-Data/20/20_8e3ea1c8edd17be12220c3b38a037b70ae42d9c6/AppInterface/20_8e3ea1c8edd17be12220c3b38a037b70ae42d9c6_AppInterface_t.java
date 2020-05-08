 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package pcricketstats;
 
 import java.util.*;
 import javax.swing.ImageIcon;
 import javax.swing.JOptionPane;
 import javax.swing.table.DefaultTableModel;
 
 /**
  *
  * @author interkiwiwebdev
  */
 public class AppInterface extends javax.swing.JFrame {
 
     private ArrayList<Player> players;
     private Object[] object;
     private StatUtilities stats;
     
     public String[] countryOptions;
     public String[] compareStatsOptions;
     public String[] sortingOptions;
     
     private ArrayList<String> options;
     
     /**
      * Creates new form AppInterface
      * @param players 
      */
     public AppInterface(ArrayList<Player> players) {
         initComponents();
         this.players = players;
         
         countryOptions = new String[] { "Afghanistan", "Australia", "Bangladesh", "Bermuda", "Canada", "England", "India", "Ireland", "Kenya", "Netherlands", "New Zealand", "Pakistan", "Scotland", "South Africa", "Sri Lanka", "West Indies", "Zimbabwe" };
     
         compareStatsOptions = new String[] { "Compare Average Balls Bowled", "Compare Average Bowling Averages", "Compare Average Career Lengths", "Compare Average Economy Rates", "Compare Average Number of 5 Wicket Innings", "Compare Average Innings Played", "Compare Average Matches Played", "Compare Average Runs Conceded", "Compare Average Strike Rates", "Compare Average Wickets Taken" };
     
         sortingOptions = new String[] {"Balls Bowled", "Bowling Average", "Career Span", "Country Name", "Economy Rate", "5 Wickets/Innings", "Innings Played", "Matches Played", "Player Name", "Runs Conceded", "Strike Rate", "Wickets Taken (default)"};
         
         stats = new StatUtilities();
         playersDataTable.setModel(tableModel);
         
         options = new ArrayList<String>();
         options.addAll(Arrays.asList(compareStatsOptions));
         
         /*
          * This class is borrowed from http://tips4java.wordpress.com/2008/11/10/table-column-adjuster/. I couldn't 
          * find a way to resize certain columns, given the creation of the custom table model (tableModel). When I 
          * have time, I'll create a better table model (create an external class that can be instantiated).
          */
         TableColumnAdjuster columnAdjuster = new TableColumnAdjuster(playersDataTable);
         
         columnAdjuster.adjustColumns();
     }
     
     DefaultTableModel tableModel = new DefaultTableModel(
         new Object [][] {
 
         },
         new String [] {
             "ID", "     Player's Name     ", "      Country      ", "Career Span", "Matches Played", "Innings Played", "Balls Bowled", "Runs Conceded", "Wickets Taken", "Bowling Average", "Economy Rate", "Strike Rate", "5 Wickets/Innings"
         }  
     );
     
     /**
      * This method resets the overall average statistics
      * (for the table data on the Multiple Player Stats tab).
      */
     public void resetOverallAverages()
     {
         aveCareerLength.setText("");
         aveMatchesPlayed.setText("");
         aveInningsPlayed.setText("");
         aveBallsBowled.setText("");
         aveRunsConceded.setText("");
         aveWicketsTaken.setText("");
         aveBowlingAverage.setText("");
         aveEconomyRate.setText("");
         aveStrikeRate.setText("");
         aveFiveWicketsPerInnings.setText("");
     }
     
     /**
      * This method returns the index of a string, in an array of strings.
      * @param options The array of strings
      * @param option The string to find the index of.
      * @return The index of @option in the array @options.
      */
     public int getIndexOf(String[] options, String option)
     {
         List<String> optionsList = Arrays.asList(options);
         return optionsList.indexOf(options);
     }
     
     /**
      * This method is used with the drawTable() methods to display
      * averages from data displayed in the table.
      * @param players The players for which averages data is to be displayed.
      */
     public void addStatsToAveragesPanel(ArrayList<Player> players)
     {
         String averageCareerLength = "";
         String averageMatchesPlayed = "";
         String averageInningsPlayed = "";
         String averageBallsBowled = "";
         String averageRunsConceded = "";
         String averageWicketsTaken = "";
         String averageBowlingAverage = "";
         String averageEconomyRate = "";
         String averageStrikeRate = "";
         String aveFiveWicketsInns = "";
         
         if(stats.aveCareerLength(players) == 1)
         {
             aveCareerLength.setText(averageCareerLength += stats.aveCareerLength(players) + " year");
         }
         else
         {
             aveCareerLength.setText(averageCareerLength += stats.aveCareerLength(players) + " years");
         }
         
         
         aveMatchesPlayed.setText(averageMatchesPlayed += stats.aveMatchesPerPlayer(players));
         aveInningsPlayed.setText(averageInningsPlayed += stats.aveInningsPerPlayer(players));
         aveBallsBowled.setText(averageBallsBowled += stats.aveBallsBowled(players));
         aveRunsConceded.setText(averageRunsConceded += stats.aveRunsConceded(players));
         aveWicketsTaken.setText(averageWicketsTaken += stats.aveWickets(players));
         aveBowlingAverage.setText(averageBowlingAverage += stats.aveBowlingAverage(players));
         aveEconomyRate.setText(averageEconomyRate += stats.aveEconRate(players));
         aveStrikeRate.setText(averageStrikeRate += stats.aveStrikeRate(players));
         aveFiveWicketsPerInnings.setText(aveFiveWicketsInns += stats.aveFiveWicketsInns(players));
     }
     
     /**
     * Re-draws the Table on the first tab by erasing the model and writing personArray to it again
     */
     public void drawTable() {
         
         tableModel.setRowCount(0);
         for (int i = 0; i < players.size(); i++) {
             object = new Object[13];
             object[0] = players.get(i).getPlayerID();
             object[1] = players.get(i).getPlayerName();
             object[2] = players.get(i).getCountryName();
             object[3] = players.get(i).getCareerSpan();
             object[4] = players.get(i).getMatchesPlayed();
             object[5] = players.get(i).getInningsPlayed();
             object[6] = players.get(i).getBallsBowled();
             object[7] = players.get(i).getRunsConceded();
             object[8] = players.get(i).getWicketsTaken();
             object[9] = players.get(i).getBowlingAverage();
             object[10] = players.get(i).getEconomyRate();
             object[11] = players.get(i).getStrikeRate();
             object[12] = players.get(i).getFiveWicketsInnings();
             tableModel.addRow(object);
         }
         
         tableModel.fireTableDataChanged();
         
         
         addStatsToAveragesPanel(players);
         
         
     }
     
     /**
      * This is an overloaded method to display players 
      * between a starting and ending index in the table.
      * @param start The starting index.
      * @param end The ending index.
      */
     public void drawTable(int start, int end) {
         
         if(start < 0)
         {
             resetOverallAverages();
             JOptionPane.showMessageDialog(this,"Starting value must be greater than 0");
         }
         else if(end > players.size())
         {
             resetOverallAverages();
             JOptionPane.showMessageDialog(this,"Starting value must be less than or equal to " + players.size());
         }
         else if(start > end)
         {
             resetOverallAverages();
             JOptionPane.showMessageDialog(this,"Starting \"from\" value must be less than the end \"to\" value.");
         }
         else
         {
             
             ArrayList<Player> tempPlayers = new ArrayList<Player>();
             
             tableModel.setRowCount(0);
             for (int i = start; i <= end; i++) {
                 object = new Object[13];
                 object[0] = players.get(i).getPlayerID();
                 object[1] = players.get(i).getPlayerName();
                 object[2] = players.get(i).getCountryName();
                 object[3] = players.get(i).getCareerSpan();
                 object[4] = players.get(i).getMatchesPlayed();
                 object[5] = players.get(i).getInningsPlayed();
                 object[6] = players.get(i).getBallsBowled();
                 object[7] = players.get(i).getRunsConceded();
                 object[8] = players.get(i).getWicketsTaken();
                 object[9] = players.get(i).getBowlingAverage();
                 object[10] = players.get(i).getEconomyRate();
                 object[11] = players.get(i).getStrikeRate();
                 object[12] = players.get(i).getFiveWicketsInnings();
                 
                 tempPlayers.add(
                         new Player(
                         players.get(i).getPlayerID(), 
                         players.get(i).getPlayerName(), 
                         players.get(i).getCountryName(), 
                         players.get(i).getCareerSpan(), 
                         players.get(i).getMatchesPlayed(), 
                         players.get(i).getInningsPlayed(), 
                         players.get(i).getBallsBowled(), 
                         players.get(i).getRunsConceded(), 
                         players.get(i).getWicketsTaken(), 
                         players.get(i).getBowlingAverage(), 
                         players.get(i).getEconomyRate(), 
                         players.get(i).getStrikeRate(), 
                         players.get(i).getFiveWicketsInnings()));
                 
                 tableModel.addRow(object);
 
             }
             
             tableModel.fireTableDataChanged();
         
             addStatsToAveragesPanel(tempPlayers);
             
         }
     }
     
     /**
      * This is an overloaded method to display the first N players in 
      * the table
      * @param firstNPlayers The number of players to be listed.
      */
     public void drawTable(int firstNPlayers) {
         
         if(firstNPlayers > players.size())
         {
             resetOverallAverages();
             JOptionPane.showMessageDialog(this,"Value must be less than or equal to" + players.size() + ".");
         }
         else
         {
             ArrayList<Player> tempPlayers = new ArrayList<Player>();
             
             tableModel.setRowCount(0);
             for (int i = 0; i < firstNPlayers; i++) {
                 object = new Object[13];
                 object[0] = players.get(i).getPlayerID();
                 object[1] = players.get(i).getPlayerName();
                 object[2] = players.get(i).getCountryName();
                 object[3] = players.get(i).getCareerSpan();
                 object[4] = players.get(i).getMatchesPlayed();
                 object[5] = players.get(i).getInningsPlayed();
                 object[6] = players.get(i).getBallsBowled();
                 object[7] = players.get(i).getRunsConceded();
                 object[8] = players.get(i).getWicketsTaken();
                 object[9] = players.get(i).getBowlingAverage();
                 object[10] = players.get(i).getEconomyRate();
                 object[11] = players.get(i).getStrikeRate();
                 object[12] = players.get(i).getFiveWicketsInnings();
                 
                 tempPlayers.add(
                         new Player(
                         players.get(i).getPlayerID(), 
                         players.get(i).getPlayerName(), 
                         players.get(i).getCountryName(), 
                         players.get(i).getCareerSpan(), 
                         players.get(i).getMatchesPlayed(), 
                         players.get(i).getInningsPlayed(), 
                         players.get(i).getBallsBowled(), 
                         players.get(i).getRunsConceded(), 
                         players.get(i).getWicketsTaken(), 
                         players.get(i).getBowlingAverage(), 
                         players.get(i).getEconomyRate(), 
                         players.get(i).getStrikeRate(), 
                         players.get(i).getFiveWicketsInnings()));
                 
                 tableModel.addRow(object);
             }
             tableModel.fireTableDataChanged();
             
             addStatsToAveragesPanel(tempPlayers);
         }
     }
     
     /**
      * This method displays the last n players in the table 
      * (in reverse order).
      * @param lastXPlayers The amount of players to display.
      */
     public void drawReverseTable(int lastXPlayers) {
         
         if(lastXPlayers > players.size())
         {
             resetOverallAverages();
             JOptionPane.showMessageDialog(this,"Value must be less than or equal to" + players.size() + ".");
         }
         else if(lastXPlayers < 1)
         {
             resetOverallAverages();
             JOptionPane.showMessageDialog(this,"Value must be greater than or equal to 1.");
         }
         else
         {
             ArrayList<Player> tempPlayers = new ArrayList<Player>();
             
             tableModel.setRowCount(0);
             for (int i = players.size(); i > ((lastXPlayers - players.size()) * -1); i--) {
                 object = new Object[13];
                 object[0] = players.get(i - 1).getPlayerID();
                 object[1] = players.get(i - 1).getPlayerName();
                 object[2] = players.get(i - 1).getCountryName();
                 object[3] = players.get(i - 1).getCareerSpan();
                 object[4] = players.get(i - 1).getMatchesPlayed();
                 object[5] = players.get(i - 1).getInningsPlayed();
                 object[6] = players.get(i - 1).getBallsBowled();
                 object[7] = players.get(i - 1).getRunsConceded();
                 object[8] = players.get(i - 1).getWicketsTaken();
                 object[9] = players.get(i - 1).getBowlingAverage();
                 object[10] = players.get(i - 1).getEconomyRate();
                 object[11] = players.get(i - 1).getStrikeRate();
                 object[12] = players.get(i - 1).getFiveWicketsInnings();
                 
                 tempPlayers.add(
                         new Player(
                         players.get(i - 1).getPlayerID(), 
                         players.get(i - 1).getPlayerName(), 
                         players.get(i - 1).getCountryName(), 
                         players.get(i - 1).getCareerSpan(), 
                         players.get(i - 1).getMatchesPlayed(), 
                         players.get(i - 1).getInningsPlayed(), 
                         players.get(i - 1).getBallsBowled(), 
                         players.get(i - 1).getRunsConceded(), 
                         players.get(i - 1).getWicketsTaken(), 
                         players.get(i - 1).getBowlingAverage(), 
                         players.get(i - 1).getEconomyRate(), 
                         players.get(i - 1).getStrikeRate(), 
                         players.get(i - 1).getFiveWicketsInnings()));
                 
                 tableModel.addRow(object);
             }
             tableModel.fireTableDataChanged();
             
             addStatsToAveragesPanel(tempPlayers);
         }
     }
     
     /**
      * 
      * @param country 
      */
     public void drawTable(String country) {
         
         ArrayList<Player> tempPlayers = new ArrayList<Player>();
         
         tableModel.setRowCount(0);
         for (int i = 0; i < players.size(); i++) {
             if(players.get(i).getCountryName().equalsIgnoreCase(country))
             {
             object = new Object[13];
             object[0] = players.get(i).getPlayerID();
             object[1] = players.get(i).getPlayerName();
             object[2] = players.get(i).getCountryName();
             object[3] = players.get(i).getCareerSpan();
             object[4] = players.get(i).getMatchesPlayed();
             object[5] = players.get(i).getInningsPlayed();
             object[6] = players.get(i).getBallsBowled();
             object[7] = players.get(i).getRunsConceded();
             object[8] = players.get(i).getWicketsTaken();
             object[9] = players.get(i).getBowlingAverage();
             object[10] = players.get(i).getEconomyRate();
             object[11] = players.get(i).getStrikeRate();
             object[12] = players.get(i).getFiveWicketsInnings();
             
             tempPlayers.add(
                         new Player(
                         players.get(i).getPlayerID(), 
                         players.get(i).getPlayerName(), 
                         players.get(i).getCountryName(), 
                         players.get(i).getCareerSpan(), 
                         players.get(i).getMatchesPlayed(), 
                         players.get(i).getInningsPlayed(), 
                         players.get(i).getBallsBowled(), 
                         players.get(i).getRunsConceded(), 
                         players.get(i).getWicketsTaken(), 
                         players.get(i).getBowlingAverage(), 
                         players.get(i).getEconomyRate(), 
                         players.get(i).getStrikeRate(), 
                         players.get(i).getFiveWicketsInnings()));
                 
                 tableModel.addRow(object);
             }
             
             tableModel.fireTableDataChanged();
             
         }
         
             addStatsToAveragesPanel(tempPlayers);
     }
     
     /**
      * This method displays a sorted table, according to the 
      * option parsed through.
      * @param sortOption The option number, for which the players are to be sorted.
      */
     public void drawSortedTable(int sortOption) {
         
         ArrayList<Player> tempPlayers = new ArrayList<Player>();
         
         object = new Object[13];
         for (int i = 0; i < tableModel.getRowCount(); i++) {
             
             tempPlayers.add(
                         new Player(
                         ((Integer)(tableModel.getValueAt(i,0))).intValue(),
                         ((String)(tableModel.getValueAt(i, 1))).toString(), 
                         ((String)(tableModel.getValueAt(i, 2))).toString(), 
                         ((String)(tableModel.getValueAt(i, 3))).toString(), 
                         ((Integer)(tableModel.getValueAt(i, 4))).intValue(), 
                         ((Integer)(tableModel.getValueAt(i, 5))).intValue(), 
                         ((Integer)(tableModel.getValueAt(i, 6))).intValue(), 
                         ((Integer)(tableModel.getValueAt(i, 7))).intValue(), 
                         ((Integer)(tableModel.getValueAt(i, 8))).intValue(), 
                         ((Double)(tableModel.getValueAt(i, 9))).doubleValue(), 
                         ((Double)(tableModel.getValueAt(i, 10))).doubleValue(), 
                         ((Double)(tableModel.getValueAt(i, 11))).doubleValue(), 
                         ((Integer)(tableModel.getValueAt(i, 12))).intValue()));
                         
         }
             switch(sortOption)
             {
                 case 0:
                     Collections.sort(tempPlayers, new SortByBallsBowled());
                     break;
                 case 1:
                     Collections.sort(tempPlayers, new SortByBowlingAverage());
                     break;
                 case 2:
                     Collections.sort(tempPlayers, new SortByCareerSpan());
                     break;
                 case 3:
                     Collections.sort(tempPlayers, new SortByCountryName());
                     break;
                 case 4:
                     Collections.sort(tempPlayers, new SortByEconomyRate());
                     break;
                 case 5:
                     Collections.sort(tempPlayers, new SortByFiveWicketsInn());
                     break;
                 case 6:
                     Collections.sort(tempPlayers, new SortByInningsPlayed());
                     break;
                 case 7:
                     Collections.sort(tempPlayers, new SortByMatchesPlayed());
                     break;
                 case 8:
                     Collections.sort(tempPlayers, new SortByPlayerName());
                     break;
                 case 9:
                     Collections.sort(tempPlayers, new SortByRunsConceded());
                     break;
                 case 10:
                     Collections.sort(tempPlayers, new SortByStrikeRate());
                     break;
                 case 11:
                     Collections.sort(tempPlayers, new SortByWicketsTaken());
                     break;
             }
             
             tableModel.setRowCount(0);
             for (int i = 0; i < tempPlayers.size(); i++) {
                 object[0] = tempPlayers.get(i).getPlayerID();
                 object[1] = tempPlayers.get(i).getPlayerName();
                 object[2] = tempPlayers.get(i).getCountryName();
                 object[3] = tempPlayers.get(i).getCareerSpan();
                 object[4] = tempPlayers.get(i).getMatchesPlayed();
                 object[5] = tempPlayers.get(i).getInningsPlayed();
                 object[6] = tempPlayers.get(i).getBallsBowled();
                 object[7] = tempPlayers.get(i).getRunsConceded();
                 object[8] = tempPlayers.get(i).getWicketsTaken();
                 object[9] = tempPlayers.get(i).getBowlingAverage();
                 object[10] = tempPlayers.get(i).getEconomyRate();
                 object[11] = tempPlayers.get(i).getStrikeRate();
                 object[12] = tempPlayers.get(i).getFiveWicketsInnings();
                 tableModel.addRow(object);
                 
             }
             
             
             tableModel.fireTableDataChanged();
     }
     
     
     /**
      * This method returns the value at [row,col] in the 
      * populated table model.
      * @param row The row index in the table model
      * @param col The column index in the table model
      * @return The value at [row,col].
      */
       public Object getValueAt(int row, int col) {
       if (row > tableModel.getRowCount()) {
          // throw an exception
       }
       switch (col) {
       case 0:
          return players.get(row).getPlayerID();
       case 1:
          return players.get(row).getPlayerName();
       case 2:
          return players.get(row).getCountryName();
       case 3:
          return players.get(row).getCareerSpan();
       case 4:
          return players.get(row).getMatchesPlayed();
       case 5:
          return players.get(row).getInningsPlayed();
       case 6:
          return players.get(row).getBallsBowled();
       case 7:
          return players.get(row).getRunsConceded();
       case 8:
          return players.get(row).getWicketsTaken();
       case 9:
          return players.get(row).getBowlingAverage();
       case 10:
          return players.get(row).getEconomyRate();
       case 11:
          return players.get(row).getStrikeRate();
       case 12:
          return players.get(row).getFiveWicketsInnings();
       }
       return null; // or throw an exception
     }
     
     /**
      * This method processes the chart for the "Country vs Country" 
      * player stats, in the "Multi Player Stats" tab.
      * @param option The option for which stats to display.
      */
     public void processCvCchart(int option)
     {
         String title = options.get(option).substring(options.get(option).indexOf(' '));
         String versus = " - Country vs Country";
         CountryVsCountryChart chart = new CountryVsCountryChart(title, "", players, option + 1);
         graphDisplayPanel.add(chart);
         repaint();
     }
     
     /**
      * This method processes the chart for the "Player vs Country vs Overall" 
      * player stats, in the "Single Player Stats" tab.
      * @param option The option for which stats to display.
      */
     
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         mainAppTabPane = new javax.swing.JTabbedPane();
         singlePlayerStatsPanel = new javax.swing.JPanel();
         singlePlayerStatsLabel = new javax.swing.JLabel();
         mainPanelPlayerStats = new javax.swing.JPanel();
         searchPlayerNameLabel = new javax.swing.JLabel();
         getPlayerName = new javax.swing.JTextField();
         getPlayerStatsBtn = new javax.swing.JButton();
         playerStatsPanel = new javax.swing.JPanel();
         playerNameLabel = new javax.swing.JLabel();
         playerName = new javax.swing.JLabel();
         countryNameLabel = new javax.swing.JLabel();
         countryName = new javax.swing.JLabel();
         careerSpanLabel = new javax.swing.JLabel();
         careerSpan = new javax.swing.JLabel();
         careerLengthLabel = new javax.swing.JLabel();
         careerLength = new javax.swing.JLabel();
         matchesPlayedLabel = new javax.swing.JLabel();
         matchesPlayed = new javax.swing.JLabel();
         inningsPlayedLabel = new javax.swing.JLabel();
         inningsPlayed = new javax.swing.JLabel();
         ballsBowledLabel = new javax.swing.JLabel();
         ballsBowled = new javax.swing.JLabel();
         runsConcededLabel = new javax.swing.JLabel();
         runsConceded = new javax.swing.JLabel();
         wicketsTakenLabel = new javax.swing.JLabel();
         wicketsTaken = new javax.swing.JLabel();
         bowlingAverageLabel = new javax.swing.JLabel();
         bowlingAverage = new javax.swing.JLabel();
         economyRateLabel = new javax.swing.JLabel();
         economyRate = new javax.swing.JLabel();
         strikeRateLabel = new javax.swing.JLabel();
         strikeRate = new javax.swing.JLabel();
         fiveWicketsInningsLabel = new javax.swing.JLabel();
         fiveWicketsPerInnings = new javax.swing.JLabel();
         countryFlagPanel = new javax.swing.JPanel();
         flagImageLabel = new javax.swing.JLabel();
         resetPlayerStatsBtn = new javax.swing.JButton();
         infoLabel = new javax.swing.JLabel();
         closeBtn = new javax.swing.JButton();
         multiplePlayerStatsPanel = new javax.swing.JPanel();
         multiplePlayerStatsLabel = new javax.swing.JLabel();
         getAllPlayersBtn = new javax.swing.JButton();
         retrievePlayerDataPanel = new javax.swing.JPanel();
         playersFromLabel = new javax.swing.JLabel();
         getPlayerFrom = new javax.swing.JTextField();
         playersToLabel = new javax.swing.JLabel();
         getPlayerTo = new javax.swing.JTextField();
         getFromToPlayersBtn = new javax.swing.JButton();
         getFirstLabel = new javax.swing.JLabel();
         firstNPlayers = new javax.swing.JTextField();
         nPlayersLabel = new javax.swing.JLabel();
         getFirstNPlayersBtn = new javax.swing.JButton();
         getPlayersFromLabel = new javax.swing.JLabel();
         fromCountryOption = new javax.swing.JComboBox();
         getPlayersFromCountryBtn = new javax.swing.JButton();
         getLastLabel = new javax.swing.JLabel();
         lastNPlayers = new javax.swing.JTextField();
         nPlayersLabel1 = new javax.swing.JLabel();
         getLastNPlayersBtn = new javax.swing.JButton();
         countryVsCountryPanel = new javax.swing.JPanel();
         compareStatsComboBox = new javax.swing.JComboBox();
         comparePlayerStatsLabel = new javax.swing.JLabel();
         comparePlayerStatsBtn = new javax.swing.JButton();
         graphDisplayPanel = new javax.swing.JPanel();
         overallPlayerAverages = new javax.swing.JPanel();
         promptLabel = new javax.swing.JLabel();
         aveCareerLengthLabel = new javax.swing.JLabel();
         aveCareerLength = new javax.swing.JLabel();
         aveMatchesPlayedLabel = new javax.swing.JLabel();
         aveMatchesPlayed = new javax.swing.JLabel();
         aveInningsPlayedLabel = new javax.swing.JLabel();
         aveInningsPlayed = new javax.swing.JLabel();
         aveBallsBowledLabel = new javax.swing.JLabel();
         aveBallsBowled = new javax.swing.JLabel();
         aveRunsConcededLabel = new javax.swing.JLabel();
         aveRunsConceded = new javax.swing.JLabel();
         aveWicketsTakenLabel = new javax.swing.JLabel();
         aveWicketsTaken = new javax.swing.JLabel();
         aveBowlingAverageLabel = new javax.swing.JLabel();
         aveBowlingAverage = new javax.swing.JLabel();
         aveEconomyRateLabel = new javax.swing.JLabel();
         aveEconomyRate = new javax.swing.JLabel();
         aveStrikeRateLabel = new javax.swing.JLabel();
         aveStrikeRate = new javax.swing.JLabel();
         ave5wicketsInningsLabel = new javax.swing.JLabel();
         aveFiveWicketsPerInnings = new javax.swing.JLabel();
         playerAveStatsLabel = new javax.swing.JLabel();
         closeBtn1 = new javax.swing.JButton();
         sortByComboBox = new javax.swing.JComboBox();
         sortByLabel = new javax.swing.JLabel();
         sortBtn = new javax.swing.JButton();
         tableScrollPane = new javax.swing.JScrollPane();
         playersDataTable = new javax.swing.JTable();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setTitle("CricStats App");
         setMaximumSize(new java.awt.Dimension(1350, 671));
         setMinimumSize(new java.awt.Dimension(1350, 671));
         setName("cricStatsFrame"); // NOI18N
         setResizable(false);
 
         mainAppTabPane.setBackground(new java.awt.Color(0, 48, 88));
         mainAppTabPane.setMaximumSize(new java.awt.Dimension(1350, 671));
         mainAppTabPane.setMinimumSize(new java.awt.Dimension(1350, 671));
         mainAppTabPane.setOpaque(true);
         mainAppTabPane.setPreferredSize(new java.awt.Dimension(1350, 671));
 
         singlePlayerStatsPanel.setBackground(new java.awt.Color(153, 153, 153));
         singlePlayerStatsPanel.setMaximumSize(new java.awt.Dimension(1350, 643));
         singlePlayerStatsPanel.setMinimumSize(new java.awt.Dimension(1350, 643));
         singlePlayerStatsPanel.setPreferredSize(new java.awt.Dimension(1350, 643));
 
         singlePlayerStatsLabel.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
         singlePlayerStatsLabel.setText("Single Player Stats");
 
         mainPanelPlayerStats.setBackground(new java.awt.Color(211, 225, 255));
         mainPanelPlayerStats.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 204, 255), 2));
         mainPanelPlayerStats.setMaximumSize(new java.awt.Dimension(412, 350));
         mainPanelPlayerStats.setMinimumSize(new java.awt.Dimension(412, 350));
         mainPanelPlayerStats.setPreferredSize(new java.awt.Dimension(412, 350));
 
         searchPlayerNameLabel.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
         searchPlayerNameLabel.setText("Name of the player you want stats for:");
 
         getPlayerName.setText("getPlayerName");
 
         getPlayerStatsBtn.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
         getPlayerStatsBtn.setText("Get Player Stats");
         getPlayerStatsBtn.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 getPlayerStatsBtnActionPerformed(evt);
             }
         });
 
         playerStatsPanel.setBackground(new java.awt.Color(255, 255, 255));
         playerStatsPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 204, 255), 2));
 
         playerNameLabel.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
         playerNameLabel.setText("Player Name:");
 
         playerName.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
         playerName.setName("playerName"); // NOI18N
 
         countryNameLabel.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
         countryNameLabel.setText("Country:");
 
         countryName.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
         countryName.setName("countryName"); // NOI18N
 
         careerSpanLabel.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
         careerSpanLabel.setText("Career Span:");
 
         careerSpan.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
         careerSpan.setName("careerSpan"); // NOI18N
 
         careerLengthLabel.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
         careerLengthLabel.setText("Career Length:");
 
         careerLength.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
         careerLength.setName("careerLength"); // NOI18N
 
         matchesPlayedLabel.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
         matchesPlayedLabel.setText("Matches Played:");
 
         matchesPlayed.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
         matchesPlayed.setName("matchesPlayed"); // NOI18N
 
         inningsPlayedLabel.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
         inningsPlayedLabel.setText("Innings Played:");
 
         inningsPlayed.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
         inningsPlayed.setName("inningsPlayed"); // NOI18N
 
         ballsBowledLabel.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
         ballsBowledLabel.setText("Balls Bowled:");
 
         ballsBowled.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
         ballsBowled.setName("ballsBowled"); // NOI18N
 
         runsConcededLabel.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
         runsConcededLabel.setText("Runs Conceded:");
 
         runsConceded.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
         runsConceded.setName("runsConceded"); // NOI18N
 
         wicketsTakenLabel.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
         wicketsTakenLabel.setText("Wickets Taken:");
 
         wicketsTaken.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
         wicketsTaken.setName("wicketsTaken"); // NOI18N
 
         bowlingAverageLabel.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
         bowlingAverageLabel.setText("Bowling Average:");
 
         bowlingAverage.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
         bowlingAverage.setName("bowlingAverage"); // NOI18N
 
         economyRateLabel.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
         economyRateLabel.setText("Economy Rate:");
 
         economyRate.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
         economyRate.setName("economyRate"); // NOI18N
 
         strikeRateLabel.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
         strikeRateLabel.setText("Strike Rate:");
 
         strikeRate.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
         strikeRate.setName("strikeRate"); // NOI18N
 
         fiveWicketsInningsLabel.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
         fiveWicketsInningsLabel.setText("5 Wickets / Innings:");
 
         fiveWicketsPerInnings.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
         fiveWicketsPerInnings.setName("5wicketsPerInnings"); // NOI18N
 
         countryFlagPanel.setBackground(new java.awt.Color(211, 225, 255));
         countryFlagPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 204, 255), 2));
         countryFlagPanel.setMaximumSize(new java.awt.Dimension(230, 181));
         countryFlagPanel.setMinimumSize(new java.awt.Dimension(230, 181));
         countryFlagPanel.setPreferredSize(new java.awt.Dimension(230, 181));
         countryFlagPanel.setRequestFocusEnabled(false);
         countryFlagPanel.setVerifyInputWhenFocusTarget(false);
 
         flagImageLabel.setMaximumSize(new java.awt.Dimension(230, 175));
         flagImageLabel.setMinimumSize(new java.awt.Dimension(230, 175));
         flagImageLabel.setPreferredSize(new java.awt.Dimension(230, 175));
 
         javax.swing.GroupLayout countryFlagPanelLayout = new javax.swing.GroupLayout(countryFlagPanel);
         countryFlagPanel.setLayout(countryFlagPanelLayout);
         countryFlagPanelLayout.setHorizontalGroup(
             countryFlagPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(flagImageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         countryFlagPanelLayout.setVerticalGroup(
             countryFlagPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(flagImageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
         );
 
         javax.swing.GroupLayout playerStatsPanelLayout = new javax.swing.GroupLayout(playerStatsPanel);
         playerStatsPanel.setLayout(playerStatsPanelLayout);
         playerStatsPanelLayout.setHorizontalGroup(
             playerStatsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(playerStatsPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(playerStatsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(countryNameLabel)
                     .addComponent(careerLengthLabel)
                     .addComponent(matchesPlayedLabel)
                     .addComponent(inningsPlayedLabel)
                     .addComponent(ballsBowledLabel)
                     .addComponent(runsConcededLabel)
                     .addComponent(wicketsTakenLabel)
                     .addComponent(bowlingAverageLabel)
                     .addComponent(economyRateLabel)
                     .addComponent(strikeRateLabel)
                     .addComponent(fiveWicketsInningsLabel)
                     .addComponent(playerNameLabel)
                     .addComponent(careerSpanLabel))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(playerStatsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(countryName)
                     .addComponent(playerName)
                     .addComponent(careerSpan)
                     .addComponent(careerLength)
                     .addComponent(matchesPlayed)
                     .addComponent(inningsPlayed)
                     .addComponent(ballsBowled)
                     .addComponent(runsConceded)
                     .addComponent(wicketsTaken)
                     .addComponent(bowlingAverage)
                     .addComponent(economyRate)
                     .addComponent(strikeRate)
                     .addComponent(fiveWicketsPerInnings))
                 .addGap(18, 18, Short.MAX_VALUE)
                 .addComponent(countryFlagPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 234, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
         playerStatsPanelLayout.setVerticalGroup(
             playerStatsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(playerStatsPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(playerStatsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(playerStatsPanelLayout.createSequentialGroup()
                         .addGroup(playerStatsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(playerNameLabel)
                             .addComponent(playerName))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addGroup(playerStatsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(countryNameLabel)
                             .addComponent(countryName))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addGroup(playerStatsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(careerSpanLabel)
                             .addComponent(careerSpan, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addGroup(playerStatsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(careerLengthLabel)
                             .addComponent(careerLength))
                         .addGap(13, 13, 13)
                         .addGroup(playerStatsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(matchesPlayedLabel)
                             .addComponent(matchesPlayed))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addGroup(playerStatsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(inningsPlayedLabel)
                             .addComponent(inningsPlayed))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addGroup(playerStatsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(ballsBowledLabel)
                             .addComponent(ballsBowled))
                         .addGap(8, 8, 8)
                         .addGroup(playerStatsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(runsConcededLabel)
                             .addComponent(runsConceded))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, playerStatsPanelLayout.createSequentialGroup()
                         .addComponent(countryFlagPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(22, 22, 22)))
                 .addGroup(playerStatsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(wicketsTakenLabel)
                     .addComponent(wicketsTaken))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(playerStatsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(bowlingAverageLabel)
                     .addComponent(bowlingAverage))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(playerStatsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(economyRateLabel)
                     .addComponent(economyRate))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(playerStatsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(strikeRateLabel)
                     .addComponent(strikeRate))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(playerStatsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(fiveWicketsInningsLabel)
                     .addComponent(fiveWicketsPerInnings))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         resetPlayerStatsBtn.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
         resetPlayerStatsBtn.setText("Reset");
         resetPlayerStatsBtn.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 resetPlayerStatsBtnActionPerformed(evt);
             }
         });
 
         infoLabel.setText("* If player has been knighted, add a space then (Sir)");
 
         javax.swing.GroupLayout mainPanelPlayerStatsLayout = new javax.swing.GroupLayout(mainPanelPlayerStats);
         mainPanelPlayerStats.setLayout(mainPanelPlayerStatsLayout);
         mainPanelPlayerStatsLayout.setHorizontalGroup(
             mainPanelPlayerStatsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(mainPanelPlayerStatsLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(mainPanelPlayerStatsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(playerStatsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addGroup(mainPanelPlayerStatsLayout.createSequentialGroup()
                        .addGap(0, 186, Short.MAX_VALUE)
                         .addGroup(mainPanelPlayerStatsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(infoLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(searchPlayerNameLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addGroup(mainPanelPlayerStatsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                             .addGroup(mainPanelPlayerStatsLayout.createSequentialGroup()
                                 .addComponent(getPlayerStatsBtn)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(resetPlayerStatsBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                             .addComponent(getPlayerName))))
                 .addContainerGap())
         );
         mainPanelPlayerStatsLayout.setVerticalGroup(
             mainPanelPlayerStatsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(mainPanelPlayerStatsLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(mainPanelPlayerStatsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(searchPlayerNameLabel)
                     .addComponent(getPlayerName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(mainPanelPlayerStatsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(mainPanelPlayerStatsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(resetPlayerStatsBtn)
                         .addComponent(getPlayerStatsBtn))
                     .addComponent(infoLabel))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addComponent(playerStatsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
 
         closeBtn.setText("Close Application");
         closeBtn.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 closeBtnActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout singlePlayerStatsPanelLayout = new javax.swing.GroupLayout(singlePlayerStatsPanel);
         singlePlayerStatsPanel.setLayout(singlePlayerStatsPanelLayout);
         singlePlayerStatsPanelLayout.setHorizontalGroup(
             singlePlayerStatsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(singlePlayerStatsPanelLayout.createSequentialGroup()
                 .addGroup(singlePlayerStatsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(singlePlayerStatsPanelLayout.createSequentialGroup()
                         .addContainerGap()
                         .addComponent(closeBtn))
                     .addGroup(singlePlayerStatsPanelLayout.createSequentialGroup()
                         .addGap(282, 282, 282)
                         .addGroup(singlePlayerStatsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                             .addComponent(singlePlayerStatsLabel)
                             .addComponent(mainPanelPlayerStats, javax.swing.GroupLayout.PREFERRED_SIZE, 786, javax.swing.GroupLayout.PREFERRED_SIZE))))
                 .addContainerGap(282, Short.MAX_VALUE))
         );
         singlePlayerStatsPanelLayout.setVerticalGroup(
             singlePlayerStatsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(singlePlayerStatsPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(singlePlayerStatsLabel)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(mainPanelPlayerStats, javax.swing.GroupLayout.PREFERRED_SIZE, 431, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 127, Short.MAX_VALUE)
                 .addComponent(closeBtn)
                 .addContainerGap())
         );
 
         mainAppTabPane.addTab("Single Player Stats", singlePlayerStatsPanel);
 
         multiplePlayerStatsPanel.setBackground(new java.awt.Color(153, 153, 153));
         multiplePlayerStatsPanel.setMaximumSize(new java.awt.Dimension(1350, 643));
         multiplePlayerStatsPanel.setMinimumSize(new java.awt.Dimension(1350, 643));
 
         multiplePlayerStatsLabel.setFont(multiplePlayerStatsLabel.getFont().deriveFont(multiplePlayerStatsLabel.getFont().getStyle() | java.awt.Font.BOLD, multiplePlayerStatsLabel.getFont().getSize()+13));
         multiplePlayerStatsLabel.setText("Multiple Player Stats");
 
         getAllPlayersBtn.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
         getAllPlayersBtn.setText("Get All Players");
         getAllPlayersBtn.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 getAllPlayersBtnActionPerformed(evt);
             }
         });
 
         retrievePlayerDataPanel.setBackground(new java.awt.Color(211, 225, 255));
         retrievePlayerDataPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 204, 255), 2));
 
         playersFromLabel.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
         playersFromLabel.setText("Get player/s");
 
         getPlayerFrom.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 getPlayerFromActionPerformed(evt);
             }
         });
 
         playersToLabel.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
         playersToLabel.setText("to");
 
         getFromToPlayersBtn.setText("Get Players");
         getFromToPlayersBtn.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 getFromToPlayersBtnActionPerformed(evt);
             }
         });
 
         getFirstLabel.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
         getFirstLabel.setText("Get first");
 
         nPlayersLabel.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
         nPlayersLabel.setText("players");
 
         getFirstNPlayersBtn.setText("Get Players");
         getFirstNPlayersBtn.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 getFirstNPlayersBtnActionPerformed(evt);
             }
         });
 
         getPlayersFromLabel.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
         getPlayersFromLabel.setText("Get players from");
 
         fromCountryOption.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Afghanistan", "Australia", "Bangladesh", "Bermuda", "Canada", "England", "India", "Ireland", "Kenya", "Netherlands", "New Zealand", "Pakistan", "Scotland", "South Africa", "Sri Lanka", "West Indies", "Zimbabwe" }));
         fromCountryOption.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 fromCountryOptionActionPerformed(evt);
             }
         });
 
         getPlayersFromCountryBtn.setText("Get Players");
         getPlayersFromCountryBtn.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 getPlayersFromCountryBtnActionPerformed(evt);
             }
         });
 
         getLastLabel.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
         getLastLabel.setText("Get last");
 
         nPlayersLabel1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
         nPlayersLabel1.setText("players");
 
         getLastNPlayersBtn.setText("Get Players");
         getLastNPlayersBtn.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 getLastNPlayersBtnActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout retrievePlayerDataPanelLayout = new javax.swing.GroupLayout(retrievePlayerDataPanel);
         retrievePlayerDataPanel.setLayout(retrievePlayerDataPanelLayout);
         retrievePlayerDataPanelLayout.setHorizontalGroup(
             retrievePlayerDataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(retrievePlayerDataPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(retrievePlayerDataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(retrievePlayerDataPanelLayout.createSequentialGroup()
                         .addGroup(retrievePlayerDataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(playersFromLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                             .addComponent(getFirstLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                             .addComponent(getPlayersFromLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(fromCountryOption, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                     .addGroup(retrievePlayerDataPanelLayout.createSequentialGroup()
                         .addGap(0, 60, Short.MAX_VALUE)
                         .addComponent(getLastLabel)
                         .addGap(18, 18, 18)
                         .addGroup(retrievePlayerDataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(getPlayerFrom)
                             .addComponent(firstNPlayers)
                             .addComponent(lastNPlayers, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addGap(10, 10, 10)
                         .addGroup(retrievePlayerDataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(nPlayersLabel)
                             .addGroup(retrievePlayerDataPanelLayout.createSequentialGroup()
                                 .addComponent(playersToLabel)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                 .addComponent(getPlayerTo, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))
                             .addComponent(nPlayersLabel1))))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(retrievePlayerDataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(getPlayersFromCountryBtn, javax.swing.GroupLayout.Alignment.CENTER, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(getFirstNPlayersBtn, javax.swing.GroupLayout.Alignment.CENTER, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(getFromToPlayersBtn, javax.swing.GroupLayout.Alignment.CENTER, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, retrievePlayerDataPanelLayout.createSequentialGroup()
                         .addGap(0, 0, Short.MAX_VALUE)
                         .addComponent(getLastNPlayersBtn)))
                 .addContainerGap())
         );
 
         retrievePlayerDataPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {firstNPlayers, getPlayerFrom, getPlayerTo, lastNPlayers});
 
         retrievePlayerDataPanelLayout.setVerticalGroup(
             retrievePlayerDataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(retrievePlayerDataPanelLayout.createSequentialGroup()
                 .addGap(19, 19, 19)
                 .addGroup(retrievePlayerDataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(retrievePlayerDataPanelLayout.createSequentialGroup()
                         .addComponent(getFromToPlayersBtn)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(getFirstNPlayersBtn)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                     .addGroup(retrievePlayerDataPanelLayout.createSequentialGroup()
                         .addGroup(retrievePlayerDataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(playersFromLabel)
                             .addComponent(getPlayerFrom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(playersToLabel)
                             .addComponent(getPlayerTo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addGroup(retrievePlayerDataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(getFirstLabel)
                             .addComponent(firstNPlayers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(nPlayersLabel))
                         .addGap(0, 0, Short.MAX_VALUE)))
                 .addGroup(retrievePlayerDataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(getLastLabel)
                     .addComponent(lastNPlayers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(nPlayersLabel1)
                     .addComponent(getLastNPlayersBtn))
                 .addGap(5, 5, 5)
                 .addGroup(retrievePlayerDataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(getPlayersFromLabel)
                     .addComponent(fromCountryOption, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(getPlayersFromCountryBtn))
                 .addGap(44, 44, 44))
         );
 
         countryVsCountryPanel.setBackground(new java.awt.Color(211, 225, 255));
         countryVsCountryPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 204, 255), 2));
 
         compareStatsComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Compare Average Balls Bowled", "Compare Average Bowling Averages", "Compare Average Career Lengths", "Compare Average Economy Rates", "Compare Average Number of 5 Wicket Innings", "Compare Average Innings Played", "Compare Average Matches Played", "Compare Average Runs Conceded", "Compare Average Strike Rates", "Compare Average Wickets Taken" }));
         compareStatsComboBox.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 compareStatsComboBoxActionPerformed(evt);
             }
         });
 
         comparePlayerStatsLabel.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
         comparePlayerStatsLabel.setText("Compare Player Stats - Country vs Country");
 
         comparePlayerStatsBtn.setText("Display Graph");
         comparePlayerStatsBtn.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 comparePlayerStatsBtnActionPerformed(evt);
             }
         });
 
         graphDisplayPanel.setBackground(new java.awt.Color(255, 255, 255));
         graphDisplayPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 204, 255), 2));
         graphDisplayPanel.setLayout(new java.awt.BorderLayout());
 
         javax.swing.GroupLayout countryVsCountryPanelLayout = new javax.swing.GroupLayout(countryVsCountryPanel);
         countryVsCountryPanel.setLayout(countryVsCountryPanelLayout);
         countryVsCountryPanelLayout.setHorizontalGroup(
             countryVsCountryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(countryVsCountryPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(graphDisplayPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addContainerGap())
             .addGroup(countryVsCountryPanelLayout.createSequentialGroup()
                 .addGap(133, 133, 133)
                 .addGroup(countryVsCountryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(countryVsCountryPanelLayout.createSequentialGroup()
                         .addComponent(compareStatsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(comparePlayerStatsBtn))
                     .addGroup(countryVsCountryPanelLayout.createSequentialGroup()
                         .addGap(38, 38, 38)
                         .addComponent(comparePlayerStatsLabel)))
                 .addContainerGap(135, Short.MAX_VALUE))
         );
         countryVsCountryPanelLayout.setVerticalGroup(
             countryVsCountryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(countryVsCountryPanelLayout.createSequentialGroup()
                 .addGap(6, 6, 6)
                 .addComponent(comparePlayerStatsLabel)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(countryVsCountryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(compareStatsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(comparePlayerStatsBtn))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(graphDisplayPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         overallPlayerAverages.setBackground(new java.awt.Color(211, 225, 255));
         overallPlayerAverages.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 204, 255), 2));
         overallPlayerAverages.setMaximumSize(new java.awt.Dimension(469, 392));
 
         promptLabel.setText("(from selection in table above)");
 
         aveCareerLengthLabel.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
         aveCareerLengthLabel.setText("Career Length:");
 
         aveCareerLength.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
         aveCareerLength.setName("careerLength"); // NOI18N
 
         aveMatchesPlayedLabel.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
         aveMatchesPlayedLabel.setText("Matches Played:");
 
         aveMatchesPlayed.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
         aveMatchesPlayed.setName("matchesPlayed"); // NOI18N
 
         aveInningsPlayedLabel.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
         aveInningsPlayedLabel.setText("Innings Played:");
 
         aveInningsPlayed.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
         aveInningsPlayed.setName("inningsPlayed"); // NOI18N
 
         aveBallsBowledLabel.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
         aveBallsBowledLabel.setText("Balls Bowled:");
 
         aveBallsBowled.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
         aveBallsBowled.setName("ballsBowled"); // NOI18N
 
         aveRunsConcededLabel.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
         aveRunsConcededLabel.setText("Runs Conceded:");
 
         aveRunsConceded.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
         aveRunsConceded.setName("runsConceded"); // NOI18N
 
         aveWicketsTakenLabel.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
         aveWicketsTakenLabel.setText("Wickets Taken:");
 
         aveWicketsTaken.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
         aveWicketsTaken.setName("wicketsTaken"); // NOI18N
 
         aveBowlingAverageLabel.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
         aveBowlingAverageLabel.setText("Bowling Average:");
 
         aveBowlingAverage.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
         aveBowlingAverage.setName("bowlingAverage"); // NOI18N
 
         aveEconomyRateLabel.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
         aveEconomyRateLabel.setText("Economy Rate:");
 
         aveEconomyRate.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
         aveEconomyRate.setName("economyRate"); // NOI18N
 
         aveStrikeRateLabel.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
         aveStrikeRateLabel.setText("Strike Rate:");
 
         aveStrikeRate.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
         aveStrikeRate.setName("strikeRate"); // NOI18N
 
         ave5wicketsInningsLabel.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
         ave5wicketsInningsLabel.setText("5 Wickets / Innings:");
 
         playerAveStatsLabel.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
         playerAveStatsLabel.setText("Overall Player Average Stats");
 
         javax.swing.GroupLayout overallPlayerAveragesLayout = new javax.swing.GroupLayout(overallPlayerAverages);
         overallPlayerAverages.setLayout(overallPlayerAveragesLayout);
         overallPlayerAveragesLayout.setHorizontalGroup(
             overallPlayerAveragesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(overallPlayerAveragesLayout.createSequentialGroup()
                 .addGroup(overallPlayerAveragesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(overallPlayerAveragesLayout.createSequentialGroup()
                         .addGap(59, 59, 59)
                         .addGroup(overallPlayerAveragesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(playerAveStatsLabel)
                             .addGroup(overallPlayerAveragesLayout.createSequentialGroup()
                                 .addGap(23, 23, 23)
                                 .addComponent(promptLabel))))
                     .addGroup(overallPlayerAveragesLayout.createSequentialGroup()
                         .addGroup(overallPlayerAveragesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(overallPlayerAveragesLayout.createSequentialGroup()
                                 .addGap(27, 27, 27)
                                 .addGroup(overallPlayerAveragesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                     .addComponent(aveCareerLengthLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                                     .addComponent(aveMatchesPlayedLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                                     .addComponent(aveInningsPlayedLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                                     .addComponent(aveBallsBowledLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                                     .addComponent(aveRunsConcededLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                                     .addComponent(aveWicketsTakenLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                                     .addComponent(aveBowlingAverageLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                                     .addComponent(aveEconomyRateLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                                     .addComponent(aveStrikeRateLabel, javax.swing.GroupLayout.Alignment.TRAILING)))
                             .addGroup(overallPlayerAveragesLayout.createSequentialGroup()
                                 .addContainerGap()
                                 .addComponent(ave5wicketsInningsLabel)))
                         .addGap(10, 10, 10)
                         .addGroup(overallPlayerAveragesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(aveCareerLength)
                             .addComponent(aveMatchesPlayed)
                             .addComponent(aveInningsPlayed)
                             .addComponent(aveBallsBowled)
                             .addComponent(aveRunsConceded)
                             .addComponent(aveWicketsTaken)
                             .addComponent(aveBowlingAverage)
                             .addComponent(aveEconomyRate)
                             .addComponent(aveStrikeRate)
                             .addComponent(aveFiveWicketsPerInnings))))
                 .addContainerGap(18, Short.MAX_VALUE))
         );
         overallPlayerAveragesLayout.setVerticalGroup(
             overallPlayerAveragesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(overallPlayerAveragesLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(playerAveStatsLabel)
                 .addGap(4, 4, 4)
                 .addComponent(promptLabel)
                 .addGap(18, 18, 18)
                 .addGroup(overallPlayerAveragesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(aveCareerLengthLabel)
                     .addComponent(aveCareerLength))
                 .addGap(13, 13, 13)
                 .addGroup(overallPlayerAveragesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(aveMatchesPlayedLabel)
                     .addComponent(aveMatchesPlayed))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(overallPlayerAveragesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(aveInningsPlayedLabel)
                     .addComponent(aveInningsPlayed))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(overallPlayerAveragesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(aveBallsBowledLabel)
                     .addComponent(aveBallsBowled))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(overallPlayerAveragesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(aveRunsConcededLabel)
                     .addComponent(aveRunsConceded))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(overallPlayerAveragesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(aveWicketsTakenLabel)
                     .addComponent(aveWicketsTaken))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(overallPlayerAveragesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(aveBowlingAverageLabel)
                     .addComponent(aveBowlingAverage))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(overallPlayerAveragesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(aveEconomyRateLabel)
                     .addComponent(aveEconomyRate))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(overallPlayerAveragesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(aveStrikeRateLabel)
                     .addComponent(aveStrikeRate))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(overallPlayerAveragesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(aveFiveWicketsPerInnings)
                     .addComponent(ave5wicketsInningsLabel))
                 .addContainerGap(80, Short.MAX_VALUE))
         );
 
         closeBtn1.setText("Close Application");
         closeBtn1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 closeBtn1ActionPerformed(evt);
             }
         });
 
         sortByComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Balls Bowled", "Bowling Average", "Career Span", "Country Name", "Economy Rate", "5 Wickets/Innings", "Innings Played", "Matches Played", "Player Name", "Runs Conceded", "Strike Rate", "Wickets Taken (default)" }));
         sortByComboBox.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 sortByComboBoxActionPerformed(evt);
             }
         });
 
         sortByLabel.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
         sortByLabel.setText("Sort By");
 
         sortBtn.setText("Go");
         sortBtn.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 sortBtnActionPerformed(evt);
             }
         });
 
         tableScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
         tableScrollPane.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
 
         playersDataTable.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 204, 255), 2));
         playersDataTable.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
 
             },
             new String [] {
 
             }
         ));
         playersDataTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_NEXT_COLUMN);
         playersDataTable.setPreferredSize(new java.awt.Dimension(700, 160));
         playersDataTable.getTableHeader().setResizingAllowed(false);
         playersDataTable.getTableHeader().setReorderingAllowed(false);
         playersDataTable.setUpdateSelectionOnSort(false);
         tableScrollPane.setViewportView(playersDataTable);
 
         javax.swing.GroupLayout multiplePlayerStatsPanelLayout = new javax.swing.GroupLayout(multiplePlayerStatsPanel);
         multiplePlayerStatsPanel.setLayout(multiplePlayerStatsPanelLayout);
         multiplePlayerStatsPanelLayout.setHorizontalGroup(
             multiplePlayerStatsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, multiplePlayerStatsPanelLayout.createSequentialGroup()
                 .addGroup(multiplePlayerStatsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addGroup(multiplePlayerStatsPanelLayout.createSequentialGroup()
                         .addGap(7, 7, 7)
                         .addGroup(multiplePlayerStatsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(multiplePlayerStatsPanelLayout.createSequentialGroup()
                                 .addComponent(multiplePlayerStatsLabel)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 321, Short.MAX_VALUE)
                                 .addComponent(getAllPlayersBtn)
                                 .addGap(353, 353, 353)
                                 .addComponent(sortByLabel)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                 .addComponent(sortByComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(sortBtn)
                                 .addGap(27, 27, 27))
                             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, multiplePlayerStatsPanelLayout.createSequentialGroup()
                                 .addGroup(multiplePlayerStatsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                     .addComponent(closeBtn1)
                                     .addComponent(retrievePlayerDataPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(countryVsCountryPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(overallPlayerAverages, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                     .addGroup(javax.swing.GroupLayout.Alignment.LEADING, multiplePlayerStatsPanelLayout.createSequentialGroup()
                         .addContainerGap()
                         .addComponent(tableScrollPane)))
                 .addContainerGap())
         );
         multiplePlayerStatsPanelLayout.setVerticalGroup(
             multiplePlayerStatsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(multiplePlayerStatsPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(multiplePlayerStatsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(multiplePlayerStatsLabel)
                     .addGroup(multiplePlayerStatsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(getAllPlayersBtn)
                         .addComponent(sortByComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(sortByLabel)
                         .addComponent(sortBtn)))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(tableScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(multiplePlayerStatsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(multiplePlayerStatsPanelLayout.createSequentialGroup()
                         .addComponent(retrievePlayerDataPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addComponent(closeBtn1))
                     .addComponent(overallPlayerAverages, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(countryVsCountryPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap())
         );
 
         mainAppTabPane.addTab("Stats Involving Multiple Players", multiplePlayerStatsPanel);
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(mainAppTabPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(mainAppTabPane, javax.swing.GroupLayout.PREFERRED_SIZE, 671, javax.swing.GroupLayout.PREFERRED_SIZE)
         );
 
         getAccessibleContext().setAccessibleName("cricStatsFrame");
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void closeBtn1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeBtn1ActionPerformed
         if(evt.getSource() == closeBtn1)
         {
             System.exit(0);
         }
     }//GEN-LAST:event_closeBtn1ActionPerformed
 
     private void comparePlayerStatsBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comparePlayerStatsBtnActionPerformed
 
         String selectedOption = String.valueOf(compareStatsComboBox.getSelectedItem()).toString();
         int statsOption = options.indexOf(selectedOption);
 
         processCvCchart(statsOption);
 
     }//GEN-LAST:event_comparePlayerStatsBtnActionPerformed
 
     private void getLastNPlayersBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_getLastNPlayersBtnActionPerformed
         try
         {
             int playerList = Integer.parseInt(lastNPlayers.getText());
 
             drawReverseTable(playerList);
         }
         catch(NumberFormatException e)
         {
             JOptionPane.showMessageDialog(this,"The field input must be an integer (e.g 1, 287, 489,...).");
         }
     }//GEN-LAST:event_getLastNPlayersBtnActionPerformed
 
     private void getPlayersFromCountryBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_getPlayersFromCountryBtnActionPerformed
         String country = String.valueOf(fromCountryOption.getSelectedItem()).toString();
         drawTable(country);
     }//GEN-LAST:event_getPlayersFromCountryBtnActionPerformed
 
     private void fromCountryOptionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fromCountryOptionActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_fromCountryOptionActionPerformed
 
     private void getFirstNPlayersBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_getFirstNPlayersBtnActionPerformed
 
         try
         {
             int playerList = Integer.parseInt(firstNPlayers.getText());
 
             drawTable(playerList);
         }
         catch(NumberFormatException e)
         {
             JOptionPane.showMessageDialog(this,"The field input must be an integer (e.g 1, 287, 489,...).");
         }
     }//GEN-LAST:event_getFirstNPlayersBtnActionPerformed
 
     private void getFromToPlayersBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_getFromToPlayersBtnActionPerformed
         try{
             int start = Integer.parseInt(getPlayerFrom.getText()) - 1;
             int end = Integer.parseInt(getPlayerTo.getText()) - 1;
 
             drawTable(start, end);
         }
         catch(NumberFormatException e)
         {
             JOptionPane.showMessageDialog(this,"Both field inputs must be integers (e.g 1, 287, 489, ...).");
         }
     }//GEN-LAST:event_getFromToPlayersBtnActionPerformed
 
     private void getPlayerFromActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_getPlayerFromActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_getPlayerFromActionPerformed
 
     private void getAllPlayersBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_getAllPlayersBtnActionPerformed
 
         drawTable();
     }//GEN-LAST:event_getAllPlayersBtnActionPerformed
 
     private void closeBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeBtnActionPerformed
         if(evt.getSource() == closeBtn)
         {
             System.exit(0);
         }
     }//GEN-LAST:event_closeBtnActionPerformed
 
     private void resetPlayerStatsBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetPlayerStatsBtnActionPerformed
         playerName.setText("");
         countryName.setText("");
         careerSpan.setText("");
         careerLength.setText("");
         matchesPlayed.setText("");
         inningsPlayed.setText("");
         ballsBowled.setText("");
         runsConceded.setText("");
         wicketsTaken.setText("");
         bowlingAverage.setText("");
         economyRate.setText("");
         strikeRate.setText("");
         fiveWicketsPerInnings.setText("");
 
         flagImageLabel.setIcon(null);
     }//GEN-LAST:event_resetPlayerStatsBtnActionPerformed
 
     private void getPlayerStatsBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_getPlayerStatsBtnActionPerformed
 
         String calcCareerSpan = "";
         String matches = "";
         String innings = "";
         String balls = "";
         String runs = "";
         String wickets = "";
         String bowling = "";
         String econRate = "";
         String strRate = "";
         String fiveWickets = "";
         String year = "";
 
         for (int i = 0; i< players.size(); i++){
             if(getPlayerName.getText().equalsIgnoreCase(players.get(i).getPlayerName()))
             {
                 playerName.setText(players.get(i).getPlayerName());
                 countryName.setText(players.get(i).getCountryName());
                 careerSpan.setText(players.get(i).getCareerSpan());
                 
                 if(players.get(i).calcCareerSpan() > 1)
                 { 
                     year = " years";
                 }
                 else
                 {
                     year = " year";
                 }
                 
                 careerLength.setText(calcCareerSpan += players.get(i).calcCareerSpan() + 
                         year  + " (Ave country: " + stats.aveCareerLength(players, players.get(i).getCountryName()) + 
                         ", Ave Overall: " + stats.aveCareerLength(players) + ")");
                 
                 matchesPlayed.setText(matches += players.get(i).getMatchesPlayed() + 
                         " (Ave country: " + stats.aveMatchesPerPlayer(players, players.get(i).getCountryName()) + 
                         ", Ave overall: " + stats.aveMatchesPerPlayer(players) + ")");
                 
                 inningsPlayed.setText(innings += players.get(i).getInningsPlayed() + 
                         " (Ave country: " + stats.aveInningsPerCountry(players, players.get(i).getCountryName()) + 
                         ", Ave overall: " + stats.aveInningsPerPlayer(players) + ")");
                 
                 ballsBowled.setText(balls += players.get(i).getBallsBowled() + 
                         " (Ave country: " + stats.aveBallsBowled(players, players.get(i).getCountryName()) + 
                         ", Ave overall: " + stats.aveBallsBowled(players) + ")");
                 
                 runsConceded.setText(runs += players.get(i).getRunsConceded() + 
                         " (Ave country: " + stats.aveRunsConceded(players, players.get(i).getCountryName()) + 
                         ", Ave overall: " + stats.aveRunsConceded(players) + ")");
                 
                 wicketsTaken.setText(wickets += players.get(i).getWicketsTaken() + 
                         " (Ave country: " + stats.aveWickets(players, players.get(i).getCountryName()) +
                         ", Ave overall: " + stats.aveWickets(players) + ")");
                 
                 bowlingAverage.setText(bowling += players.get(i).getBowlingAverage() + 
                         " (Ave country: " + stats.aveBowlingAverage(players, players.get(i).getCountryName()) + 
                         ", Ave overall: " + stats.aveBowlingAverage(players) + ")");
                 
                 economyRate.setText(econRate += players.get(i).getEconomyRate() + 
                         " (Ave country: " + stats.aveEconRate(players, players.get(i).getCountryName()) + 
                         ", Ave overall: " + stats.aveEconRate(players) + ")");
                 
                 strikeRate.setText(strRate += players.get(i).getStrikeRate() + 
                         " (Ave country: " + stats.aveStrikeRate(players, players.get(i).getCountryName()) + 
                         ", Ave overall: " + stats.aveStrikeRate(players) + ")");
                 
                 fiveWicketsPerInnings.setText(fiveWickets += players.get(i).getFiveWicketsInnings() + 
                         " (Ave country: " + stats.aveFiveWicketsInns(players, players.get(i).getCountryName()) + 
                         ", Ave overall: " + stats.aveFiveWicketsInns(players) + ")");
 
                 String countryPath = "country-flags/" + players.get(i).getCountryName().toLowerCase().replaceAll("\\s","") + ".png";
                 flagImageLabel.setIcon(new ImageIcon(countryPath));
 
             }
 
         }
     }//GEN-LAST:event_getPlayerStatsBtnActionPerformed
 
     private void sortBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sortBtnActionPerformed
         int option = sortByComboBox.getSelectedIndex();
             drawSortedTable(option);
     }//GEN-LAST:event_sortBtnActionPerformed
 
     private void sortByComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sortByComboBoxActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_sortByComboBoxActionPerformed
 
     private void compareStatsComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_compareStatsComboBoxActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_compareStatsComboBoxActionPerformed
 
     /**
      * @param args the command line arguments
      */
     public static void main(String args[]) {
         /* Set the Nimbus look and feel */
         //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
         /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
          * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
          */
         try {
             for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                 if ("Nimbus".equals(info.getName())) {
                     javax.swing.UIManager.setLookAndFeel(info.getClassName());
                     break;
                 }
             }
         } catch (ClassNotFoundException ex) {
             java.util.logging.Logger.getLogger(AppInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             java.util.logging.Logger.getLogger(AppInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             java.util.logging.Logger.getLogger(AppInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex) {
             java.util.logging.Logger.getLogger(AppInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
         //</editor-fold>
 
         /* Create and display the form */
         java.awt.EventQueue.invokeLater(new Runnable() {
             
             
             @Override
             public void run() {
                 ArrayList<Player> players = new ArrayList<Player>();
                 ReadInStats worker = new ReadInStats();
                 worker.load(players);
                 new AppInterface(players).setVisible(true);
                 
                 
                 
             }
         });
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JLabel ave5wicketsInningsLabel;
     private javax.swing.JLabel aveBallsBowled;
     private javax.swing.JLabel aveBallsBowledLabel;
     private javax.swing.JLabel aveBowlingAverage;
     private javax.swing.JLabel aveBowlingAverageLabel;
     private javax.swing.JLabel aveCareerLength;
     private javax.swing.JLabel aveCareerLengthLabel;
     private javax.swing.JLabel aveEconomyRate;
     private javax.swing.JLabel aveEconomyRateLabel;
     private javax.swing.JLabel aveFiveWicketsPerInnings;
     private javax.swing.JLabel aveInningsPlayed;
     private javax.swing.JLabel aveInningsPlayedLabel;
     private javax.swing.JLabel aveMatchesPlayed;
     private javax.swing.JLabel aveMatchesPlayedLabel;
     private javax.swing.JLabel aveRunsConceded;
     private javax.swing.JLabel aveRunsConcededLabel;
     private javax.swing.JLabel aveStrikeRate;
     private javax.swing.JLabel aveStrikeRateLabel;
     private javax.swing.JLabel aveWicketsTaken;
     private javax.swing.JLabel aveWicketsTakenLabel;
     private javax.swing.JLabel ballsBowled;
     private javax.swing.JLabel ballsBowledLabel;
     private javax.swing.JLabel bowlingAverage;
     private javax.swing.JLabel bowlingAverageLabel;
     private javax.swing.JLabel careerLength;
     private javax.swing.JLabel careerLengthLabel;
     private javax.swing.JLabel careerSpan;
     private javax.swing.JLabel careerSpanLabel;
     private javax.swing.JButton closeBtn;
     private javax.swing.JButton closeBtn1;
     private javax.swing.JButton comparePlayerStatsBtn;
     private javax.swing.JLabel comparePlayerStatsLabel;
     private javax.swing.JComboBox compareStatsComboBox;
     private javax.swing.JPanel countryFlagPanel;
     private javax.swing.JLabel countryName;
     private javax.swing.JLabel countryNameLabel;
     private javax.swing.JPanel countryVsCountryPanel;
     private javax.swing.JLabel economyRate;
     private javax.swing.JLabel economyRateLabel;
     private javax.swing.JTextField firstNPlayers;
     private javax.swing.JLabel fiveWicketsInningsLabel;
     private javax.swing.JLabel fiveWicketsPerInnings;
     private javax.swing.JLabel flagImageLabel;
     private javax.swing.JComboBox fromCountryOption;
     private javax.swing.JButton getAllPlayersBtn;
     private javax.swing.JLabel getFirstLabel;
     private javax.swing.JButton getFirstNPlayersBtn;
     private javax.swing.JButton getFromToPlayersBtn;
     private javax.swing.JLabel getLastLabel;
     private javax.swing.JButton getLastNPlayersBtn;
     private javax.swing.JTextField getPlayerFrom;
     private javax.swing.JTextField getPlayerName;
     private javax.swing.JButton getPlayerStatsBtn;
     private javax.swing.JTextField getPlayerTo;
     private javax.swing.JButton getPlayersFromCountryBtn;
     private javax.swing.JLabel getPlayersFromLabel;
     private javax.swing.JPanel graphDisplayPanel;
     private javax.swing.JLabel infoLabel;
     private javax.swing.JLabel inningsPlayed;
     private javax.swing.JLabel inningsPlayedLabel;
     private javax.swing.JTextField lastNPlayers;
     private javax.swing.JTabbedPane mainAppTabPane;
     private javax.swing.JPanel mainPanelPlayerStats;
     private javax.swing.JLabel matchesPlayed;
     private javax.swing.JLabel matchesPlayedLabel;
     private javax.swing.JLabel multiplePlayerStatsLabel;
     private javax.swing.JPanel multiplePlayerStatsPanel;
     private javax.swing.JLabel nPlayersLabel;
     private javax.swing.JLabel nPlayersLabel1;
     private javax.swing.JPanel overallPlayerAverages;
     private javax.swing.JLabel playerAveStatsLabel;
     private javax.swing.JLabel playerName;
     private javax.swing.JLabel playerNameLabel;
     private javax.swing.JPanel playerStatsPanel;
     private javax.swing.JTable playersDataTable;
     private javax.swing.JLabel playersFromLabel;
     private javax.swing.JLabel playersToLabel;
     private javax.swing.JLabel promptLabel;
     private javax.swing.JButton resetPlayerStatsBtn;
     private javax.swing.JPanel retrievePlayerDataPanel;
     private javax.swing.JLabel runsConceded;
     private javax.swing.JLabel runsConcededLabel;
     private javax.swing.JLabel searchPlayerNameLabel;
     private javax.swing.JLabel singlePlayerStatsLabel;
     private javax.swing.JPanel singlePlayerStatsPanel;
     private javax.swing.JButton sortBtn;
     private javax.swing.JComboBox sortByComboBox;
     private javax.swing.JLabel sortByLabel;
     private javax.swing.JLabel strikeRate;
     private javax.swing.JLabel strikeRateLabel;
     private javax.swing.JScrollPane tableScrollPane;
     private javax.swing.JLabel wicketsTaken;
     private javax.swing.JLabel wicketsTakenLabel;
     // End of variables declaration//GEN-END:variables
 }
