 import java.awt.Dimension;
 import java.awt.GridLayout;
 
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 
 
 public class StoriesList extends JPanel {
 
 	//-----Constructors
 	StoriesList() {
 		super(new GridLayout(1,0));
 		//-----Data Members
 		Object[][] data = {
 			    {"SQUEEZE", "PKG", "", "00:00", "00:00"},
 			    {"OPEN", "LIVE", "", "00:00", "00:00"},
 			    {"TROYTAILGATE", "PKG", "", "00:00", "00:00"},
 			    {"DODGEBALL", "PKG", "", "00:00", "00:00"},
 			    {"PINGPONG", "PKG", "", "00:00", "00:00"},
 			    {"STATE", "LIVE", "", "00:00", "00:00"},
 			    {"TEASE", "PKG", "", "00:00", "00:00"},
 			    {"TOSSTOGLOBAL", "LIVE", "", "00:00", "00:00"},
 			    {"WILDFIRES", "PKG", "", "00:00", "00:00"},
 			    {"SYRIA", "PKG", "", "00:00", "00:00"},
			    {"TOSSFROMGLOBAL", "PKG", "", "00:00", "00:00"},
			    {"TOSSTOSPORTS", "PKG", "", "00:00", "00:00"},
 			    {"FOOTBALL", "PKG", "", "00:00", "00:00"},
 			    {"SOCCER", "PKG", "", "00:00", "00:00"},
 			    {"CROSSCOUNTRY", "PKG", "", "00:00", "00:00"},
 			    {"ITSTTIME", "PKG", "", "00:00", "00:00"},
			    {"TOSSTONEWS", "PKG", "", "00:00", "00:00"},
 			    {"TEASE2", "PKG", "", "00:00", "00:00"},
			    {"TOSSTOWEATHER", "PKG", "", "00:00", "00:00"},
 			    {"WEATHER", "PKG", "", "00:00", "00:00"},
			    {"TOSSTONEWS", "PKG", "", "00:00", "00:00"},
 			    {"TTALK", "PKG", "", "00:00", "00:00"},
 			    {"CLOSE", "LIVE", "", "00:00", "00:00"}
 			};
 		String[] columnNames = {"Story", "Type", "Note", "Time", "Show Time"};
 		
 		//-----GUI Constructors
 		
 		JTable stories = new JTable(data, columnNames);
 		stories.setPreferredScrollableViewportSize(new Dimension(500, 70));
 		stories.setFillsViewportHeight(true);
 		JScrollPane tableScrollPane = new JScrollPane(stories);
 		add(tableScrollPane);
 	}
 }
