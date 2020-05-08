 package edu.tufts.cs.gv.view;
 
 import java.awt.BorderLayout;
import java.awt.Color;
 
 import javax.swing.BorderFactory;
 import javax.swing.DefaultListModel;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.ListSelectionModel;
 import javax.swing.border.CompoundBorder;
 
 import edu.tufts.cs.gv.controller.VizEventType;
 import edu.tufts.cs.gv.controller.VizState;
 import edu.tufts.cs.gv.controller.VizUpdateListener;
 import edu.tufts.cs.gv.util.Colors;
import edu.tufts.cs.gv.view.StudentListCellView;
 
 public class StudentView extends JPanel implements VizUpdateListener {
 	private static final long serialVersionUID = 1L;
 	
 	private JLabel help;
 	private JScrollPane scroll;
 	private JList<String> lstStudents;
 	private DefaultListModel<String> modStudents;
 	private StudentListCellView viewStudent;
 
 	public StudentView() {
 		modStudents = new DefaultListModel<String>();
 		lstStudents = new JList<String>(modStudents);
 		viewStudent = new StudentListCellView(lstStudents);
 		
 		lstStudents.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		lstStudents.setVisibleRowCount(-1);
 		lstStudents.setCellRenderer(viewStudent);
 		
 		scroll = new JScrollPane(lstStudents);
 		scroll.setBorder(BorderFactory.createEmptyBorder());
 		
 		help = new JLabel("<html>A list of students. Hover over a student to see what tests they passed and failed on the right.</html>");
 		help.setOpaque(true);
 		help.setBorder(new CompoundBorder(
 							BorderFactory.createLineBorder(Colors.foreground, 1),
 							BorderFactory.createEmptyBorder(10, 10, 10, 10)));
 		help.setAlignmentY(TOP_ALIGNMENT);
 		help.setBackground(Colors.helpBackground);
 		help.setForeground(Colors.foreground);
 		help.setVisible(false);
 
 		this.setLayout(new BorderLayout());
 		this.add(scroll);
 		this.add(help, BorderLayout.NORTH);
 		this.setBorder(BorderFactory.createEmptyBorder());
 		
 		VizState.getState().addVizUpdateListener(this);
 	}
 	
 	@Override
 	public void vizUpdated(VizEventType eventType) {
 		VizState state = VizState.getState();
 		
 		if (eventType == VizEventType.NEW_DATA_SOURCE) {
 			modStudents.removeAllElements();
 			for (String student : state.getDataset().getAllStudents()) {
 				modStudents.addElement(student);
 			}
 		} else if (eventType == VizEventType.TOGGLE_HELP) {
 			help.setVisible(state.isShowingHelp());
 		}
 	}
 }
