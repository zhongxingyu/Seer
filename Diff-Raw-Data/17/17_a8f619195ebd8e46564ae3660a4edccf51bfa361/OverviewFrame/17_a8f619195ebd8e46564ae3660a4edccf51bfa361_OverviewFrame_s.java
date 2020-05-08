 /*
  * This file is part of TAberystwyth, a debating competition organiser
  * Copyright (C) 2010, Roberto Sarrionandia and Cal Paterson
  * 
  * This program is free software: you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option)
  * any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
  * more details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package taberystwyth.view;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.io.IOException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Observable;
 import java.util.Observer;
 import javax.swing.*;
//import javax.swing.filechooser.FileNameExtensionFilter;
 
 import taberystwyth.controller.OverviewFrameMenuListener;
 import taberystwyth.db.SQLConnection;
 
 public class OverviewFrame extends JFrame implements Observer {
     
     private static final long serialVersionUID = 1L;
     OverviewFrameMenuListener menuListener = new OverviewFrameMenuListener(
             this);
     
     /*
      * Models
      */
     DefaultListModel speakerModel;
     DefaultListModel judgeModel;
     DefaultListModel locationModel;
     
     private static OverviewFrame instance = new OverviewFrame();
     
     public static OverviewFrame getInstance() {
         return instance;
     }
     
     private JMenuBar menu;
     
     private OverviewFrame() {
         /*
          * Run a "simple" dialog locating the current tab or creating a new one
          */
         boolean problem = true;
         while (problem == true) {
             Object[] options = { "Create a new tab", "Open an existing tab",
                     "Cancel" };
             int n = JOptionPane.showOptionDialog(this,
                     "Create a new tab or open an existing one?",
                     "TAberystwyth", JOptionPane.YES_NO_CANCEL_OPTION,
                     JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
             if (n == 0) {
                 /*
                  * Create a new tab
                  */
 
                 problem = false;
                 JFileChooser jfc = new JFileChooser();
                 /*jfc.setFileFilter(new FileNameExtensionFilter("Tab files", 
                         "tab"));*/
                 jfc.showDialog(this, "Create");
                 File selection = jfc.getSelectedFile();
                 try {
                     SQLConnection.getInstance().create(selection);
                 } catch (Exception e) {
                     e.printStackTrace();
                     JOptionPane.showMessageDialog(this,
                             "Problem reading selected file", "File Error",
                             JOptionPane.ERROR_MESSAGE);
                     problem = true;
                 }
             } else if (n == 1) {
                 /*
                  * Open an existing tab
                  */
                 problem = false;
                 JFileChooser jfc = new JFileChooser();
                 /*jfc.setFileFilter(new FileNameExtensionFilter("Tab files", 
                 "tab"));*/
                 jfc.showDialog(this, "Open");
                 File selection = jfc.getSelectedFile();
                 try {
                     SQLConnection.getInstance().set(selection);
                 } catch (Exception e) {
                     e.printStackTrace();
                     JOptionPane.showMessageDialog(this,
                             "Problem reading selected file", "File Error",
                             JOptionPane.ERROR_MESSAGE);
                     problem = true;
                 }
             } else if (n == 2) {
                 System.exit(0);
             }
         }
         
         setLayout(new BorderLayout());
         setTitle("TAberystwyth");
         /*
          * When the window is closed, exit the program
          */
         setDefaultCloseOperation(EXIT_ON_CLOSE);
         
         /*
          * Set up the models...
          */
         speakerModel = new DefaultListModel();
         judgeModel = new DefaultListModel();
         locationModel = new DefaultListModel();
         
         /*
          * ...and the lists
          */
         JList speakerList = new JList(speakerModel);
         JList judgeList = new JList(judgeModel);
         JList locationList = new JList(locationModel);
         
         /*
          * Add menu bar
          */
         menu = new OverviewFrameMenu(new OverviewFrameMenuListener(this));
         add(menu,
                 BorderLayout.NORTH);
         
         /*
          * Holding panel
          */
         JPanel holdingPanel = new JPanel(new BorderLayout());
         
         /*
          * Title Panel
          */
         JPanel titlePanel = new JPanel(new GridLayout(1, 3));
         titlePanel.add(new JLabel("Judges"));
         titlePanel.add(new JLabel("Teams"));
         titlePanel.add(new JLabel("Locations"));
         holdingPanel.add(titlePanel, BorderLayout.NORTH);
         
         /*
          * View Panel
          */
         JPanel viewPanel = new JPanel(new GridLayout(1, 3));
         viewPanel.add(new JScrollPane(judgeList));
         viewPanel.add(new JScrollPane(speakerList));
         viewPanel.add(new JScrollPane(locationList));
         holdingPanel.add(viewPanel, BorderLayout.CENTER);
         add(holdingPanel, BorderLayout.CENTER);
         pack();
         setSize(new Dimension(500, 500));
         setVisible(true);
         
         /*
          * Add myself as an observer and force and update
          */
         SQLConnection.getInstance().addObserver(this);
         SQLConnection.getInstance().setChanged();
         SQLConnection.getInstance().notifyObservers();
     }
     
     private void refreshTeams() {
         refreshList("teams", speakerModel);
     }
     
     private void refreshJudges() {
         refreshList("judges", judgeModel);
     }
     
     private void refreshLocation() {
         refreshList("locations", locationModel);
     }
     
     private void refreshList(String table, DefaultListModel model) {
         model.removeAllElements();
         ResultSet rs = SQLConnection.getInstance().executeQuery(
                 "select (name) from " + table + ";");
         int index = 0;
         try {
             while (rs.next()) {
                 String entry = rs.getString("NAME");
                 /*
                  * If it's a team, append the institution of the team
                  */
                 if (table.equals("teams")) {
                     entry += " (" + getInstitution(entry) + ")";
                 }
                 model.add(index, entry);
                 ++index;
             }
         } catch (SQLException e) {
             SQLConnection.getInstance().panic(e,
                     "Unable to refresh overview frame");
         }
     }
     
     private String getInstitution(String teamName) {
         String query = null;
         String returnValue = null;
         SQLConnection conn = SQLConnection.getInstance();
         try {
             /*
              * Get the speakers on the team
              */
             // FIXME: small hack here to ensure that the teamname (which might
             // contain a ' is properly escaped:
             teamName = teamName.replaceAll("'", "''");
             query = "select speaker1, speaker2 from teams where teams.name = '"
                     + teamName + "';";
             ResultSet rs = conn.executeQuery(query);
             rs.next();
             String speaker1 = rs.getString("SPEAKER1");
             String speaker2 = rs.getString("SPEAKER2");
             
             /*
              * Get the institution of speaker1
              */
             query = "select (institution) from speakers where speakers.name = '"
                     + speaker1 + "'";
             rs = conn.executeQuery(query);
             rs.next();
             String inst1 = rs.getString("INSTITUTION");
             rs.close();
             
             /*
              * Get the institution of speaker2
              */
             query = "select (institution) from speakers where speakers.name = '"
                     + speaker2 + "'";
             rs = conn.executeQuery(query);
             rs.next();
             String inst2 = rs.getString("INSTITUTION");
             rs.close();
             
             /*
              * Compare them
              */
             if (!inst1.equals(inst2)) {
                 returnValue = "Mixed";
             } else {
                 returnValue = inst1;
             }
             
         } catch (SQLException e) {
             conn.panic(e,
                     "Unable to find what institution two speakers are from.  Query was:\n"
                             + query);
         }
         return returnValue;
     }
     
     public void update(Observable o, Object arg) {
         System.out.println("OverviewFrame.update()");
         refreshTeams();
         refreshJudges();
         refreshLocation();
     }

     /*
      * Returns the menubar being used by this frame (used only to enable the
      * debug menu)
      */
     public JMenuBar getMenu() {
         return menu;
     }
 }
