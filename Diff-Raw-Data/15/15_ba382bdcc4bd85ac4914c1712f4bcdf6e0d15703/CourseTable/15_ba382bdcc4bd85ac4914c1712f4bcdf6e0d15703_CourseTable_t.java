 package hu.laborreg.server.gui;
 
 import java.awt.BorderLayout;
 
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 
 public class CourseTable extends JPanel {
 
 	private static final long serialVersionUID = 1207047791312415309L;
 	private JTable table;
 
 	public CourseTable() {
		setLayout(new BorderLayout());
		
 		String[] columnNames = { "Év", "Név" };
 		Object[][] data = { { "1999", "kurzus1" }, { "2005", "kurzus2", }, { "2012", "kurzus3", }, };
 		table = new JTable(data, columnNames);
 		table.setFillsViewportHeight(true);
 		
 		JScrollPane scrollPane = new JScrollPane(table);
		add(scrollPane, BorderLayout.CENTER);
 	}
 
 }
