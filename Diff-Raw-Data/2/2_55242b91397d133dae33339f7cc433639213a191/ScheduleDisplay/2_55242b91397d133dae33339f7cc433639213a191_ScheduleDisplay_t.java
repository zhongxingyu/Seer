 import java.awt.Color;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JTable;
 import javax.swing.border.Border;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableColumnModel;
 
 public class ScheduleDisplay {
 
 	private JTable table;
 	DefaultTableModel tm;
 	int maxStudentsPerClass;
 	Object[][] data;
 	JButton button = new JButton("Schedule Teachers");
 	// numClasses = #subjects; numRooms = #divisions
 	private int numRooms = ClassFactory.getMaxCls();
 	String[] columnNames;
 
 	public ScheduleDisplay() {
 		data = new Object[(3 * 7) + 1][numRooms + 2];
 		for (int i = 0; i < 22; i++) {
 			for (int j = 0; j < (numRooms + 2); j++)
 				data[i][j] = "";
 		}
 		columnNames = new String[numRooms + 2];
 		columnNames[0] = "Subject";
 		for (int i = 1; i < numRooms + 1; i++) {
 			columnNames[i] = " ";
 		}
 		columnNames[numRooms + 1] = "Unable to Place";
 		tm = new DefaultTableModel(data, columnNames);
 		table = new JTable();
 		table.setModel(tm);
 
 		update();
 	}
 
 	public void update() {
 		populateTable();
 		tm.setDataVector(data, columnNames);
 		// format table
 		table.setShowGrid(true);
 		table.setGridColor(Color.BLACK);
 		table.setRowHeight(20);
 		Border border = BorderFactory.createLineBorder(Color.BLACK, 1);
 		table.setBorder(border);
 		table.setColumnSelectionAllowed(true);
 		// Make table scrollable
 		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
 
 		// Fix Sizing
 		TableColumnModel cm = table.getColumnModel();
 		cm.getColumn(0).setMinWidth(100);
 		for (int i = 1; i < cm.getColumnCount(); i++) {
 			cm.getColumn(i).setMinWidth(150);
 		}
 
 	}
 	
 	public void populateTable() {
 		// TODO: constants for subjects and max students
 		// table.setValueAt("Reading", 1, 0);
 		data[1][0] = "Reading";
 		// table.setValueAt("LA", 8, 0);
 		data[8][0] = "Language Arts";
 		// table.setValueAt("Math", 15, 0);
 		data[15][0] = "Math";
 
 		// TODO: add rows for specials and homeroom (same as math)
 		// TODO: add formatting- times of classes
 
 		// label room numbers
 		// for (int i = 0; i < numRooms; ++i) {
 		// table.setValueAt("Room " + (i + 1), 0, i + 1);
 		// }
 		// table.setValueAt("Unable to place", 0, numRooms + 1);
 
 		// get student names and place classes in table:
 
 		// fill in students for Reading
 		for (int i = 0; i < ClassFactory.readClsLst.size(); ++i) {
 			Classes cls = ClassFactory.readClsLst.get(i);
 			data[0][i+1] = cls.getClsName() + " " + cls.getLvl();
 			List<Students> students = cls.getStudents();
 			for (int j = 1; j < students.size(); j++) {
 				Students std = students.get(j);
 				String stdNameStr = std.getFirstName();
 				stdNameStr += " " + std.getLastName();
 				//table.setValueAt(stdNameStr, j + 1, i + 1);
 				data[j][i+1] = std; //changed from stdNameStr
 			}
 		}
 
 		// fill in students for LA
 		for (int i = 0; i < ClassFactory.getTotalLA(); ++i) {
			Classes cls = ClassFactory.laClsLst.get(i);
 			data[7][i+1] = cls.getClsName() + " " + cls.getLvl();
 			List<Students> students = cls.getStudents();
 			for (int j = 1; j < students.size(); j++) {
 				Students std = students.get(j);
 				String stdNameStr = std.getFirstName();
 				stdNameStr += " " + std.getLastName();
 				//table.setValueAt(stdNameStr, j + 8, i + 1);
 				data[j+7][i+1] = std; //changed from stdNameStr
 
 			}
 		}
 
 		// fill in students for Math
 		for (int i = 0; i < ClassFactory.getTotalMath(); ++i) {
 			Classes cls = ClassFactory.mathClsLst.get(i);
 			data[14][i+1] = cls.getClsName() + " " + cls.getLvl();
 			List<Students> students = cls.getStudents();
 			for (int j = 1; j < students.size(); j++) {
 				Students std = students.get(j);
 				String stdNameStr = std.getFirstName();
 				stdNameStr += " " + std.getLastName();
 				//table.setValueAt(stdNameStr, j + 15, i + 1);
 				data[j+14][i+1] = std; //changed from stdNameStr
 
 			}
 		}
 	}
 
 	public JTable getScheduleTable() {
 		return table;
 	}
 
 }
