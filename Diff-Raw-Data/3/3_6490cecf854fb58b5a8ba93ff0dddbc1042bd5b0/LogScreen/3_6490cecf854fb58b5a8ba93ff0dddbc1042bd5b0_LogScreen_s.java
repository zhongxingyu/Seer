 package oracle.gui;
 
 import java.awt.GridLayout;
 import java.util.ArrayList;
 
 import javax.swing.*;
 
 import oracle.ref.OracleColors;
 
 public class LogScreen
 {
    JFrame window = new JFrame();
    JPanel mainPanel = new JPanel();
    JPanel linesPanel = new JPanel();
    JPanel[] lineHolders;
    JLabel[] lines;
    ArrayList<String> logStrings;
    
    public LogScreen(ArrayList<String> log) {
 
       logStrings = log;
       lineHolders = new JPanel[log.size()];
       mainPanel.setLayout(null);
       
       linesPanel.setLayout(new GridLayout(log.size(),1, 5, 5));
       
       mainPanel.setBackground(OracleColors.PALE_BLUE);
       linesPanel.setBackground(OracleColors.BLACK);
 
       displayLogLines();
 
    }
    
    private void displayLogLines() {
       
       for (int i = 0; i < logStrings.size(); i++) {
 
          lines[i] = new JLabel(logStrings.get(i));
          lineHolders[i] = new JPanel();
          lineHolders[i].add(lines[i]);
 
          if (logStrings.get(i).charAt(0) == 'Q') {
             lineHolders[i].setBackground(OracleColors.GREEN);
          } else if (logStrings.get(i).charAt(0) == 'A') {
             lineHolders[i].setBackground(OracleColors.RED);
          }
       }
    }
 }
