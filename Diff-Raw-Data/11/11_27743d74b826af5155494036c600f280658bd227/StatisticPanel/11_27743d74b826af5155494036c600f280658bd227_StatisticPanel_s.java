 //~ Track Controller
 //~ Calvin Souders
 //~ 11/27/2012
 //~ Albion
 
 package TrackController;
 
 //import java.awt.FlowLayout;
 
 
 import java.awt.*;
 
 import javax.swing.*;
 
 @SuppressWarnings("serial")
 public class StatisticPanel extends JPanel{
 
 	private String[] genCols = {"Property", "Data"};
 	private Object[][] genData = {
 		{"Train Line", null},
 		{"Track Block", null},
 		{"Authority", null},
 		{"Speed", null},
 		{"Grade", null},
 		{"Elevation", null}
 	};
 	
 	private String[] advCols = {"Property", "Data"};
 	private Object[][] advData = {
 		{"Train Line", null},
 		{"Track Block", null},
 		{"Authority", null},
 		{"Speed", null},
 		{"Grade", null},
 		{"Elevation", null}
 	};
 	
 		StatisticPanel(){
 			super();
 			
			//this.setPreferredSize(new Dimension(200, 300));
 			
 			//this.setLayout(new BorderLayout());
 			JTabbedPane tabPane = new JTabbedPane();
 			JTable generalTable, advancedTable;
 			
 			generalTable = new JTable(genData, genCols);
 			advancedTable = new JTable(advData, advCols);
 			
 			tabPane.addTab("General", new JScrollPane(generalTable));
 			tabPane.addTab("Advanced", new JScrollPane(advancedTable));
 			
 			this.add(tabPane);
 		}
 		
 		public void changeGeneralData(int row, int col, Object value){
 			genData[row][col] = value;
 		}
 		
 		public void changeAdvancedData(int row, int col, Object value){
 			advData[row][col] = value;
 		}
 		
 }
