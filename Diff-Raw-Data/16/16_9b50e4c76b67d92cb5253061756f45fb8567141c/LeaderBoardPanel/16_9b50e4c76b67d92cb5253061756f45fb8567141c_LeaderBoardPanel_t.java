 package gui;
 
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.ScrollPane;
 import java.util.ArrayList;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 
 /**
  *
  * @author Haisin Yip
  */
 
 public class LeaderBoardPanel extends JPanel
 {
     JTable table;
     //ArrayList<String> playerNames = new ArrayList<String>();
     String[] columns = {"Player", "HighScore", "Number of Lives", "Asteroid Destroyed", "Aliens destroyed", "Total deaths", "Kill-Death Ratio", "Level reached", "Bombs Used", "Shooting Accuracy"};
    String[][] rowdata = {{"p1", "highscore", "5", "3", "2", "3", "1.4", "3", "3", "3"}};
     
     JScrollPane scrollPane;
     public LeaderBoardPanel(int width, int height)
     {
         makeComponents(width, height);
         makeLayout();
     }
     
     private void makeComponents(int w, int h)
     {
         table = new JTable(rowdata, columns);
         table.setPreferredScrollableViewportSize(new Dimension(w-40, h-110));
         table.setFillsViewportHeight(true);  
     }
     
     private void makeLayout()
     {
         setLayout(new FlowLayout());
         add(table);
         add(new JScrollPane(table));
     }
 }
