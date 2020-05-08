 package edu.tufts.cs.gv.view;
 
 import java.awt.Component;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.ListCellRenderer;
 
 import edu.tufts.cs.gv.controller.VizEventType;
 import edu.tufts.cs.gv.controller.VizState;
 import edu.tufts.cs.gv.controller.VizUpdateListener;
 import edu.tufts.cs.gv.util.Colors;
 
 public class StudentListCellView extends JLabel implements
 		ListCellRenderer<String>, VizUpdateListener {
 	
 	private static final long serialVersionUID = 1L;
 	
 	private Set<String> tests, students;
 	private String student;
 	private JList<String> lstStudents;
 	private StudentListCellMouseAdapter adpStudents;
 	
 	public StudentListCellView (JList<String> list) {
 		lstStudents = list;
 		
 		adpStudents = new StudentListCellMouseAdapter(list);	
 		list.addMouseListener(adpStudents);
 		list.addMouseMotionListener(adpStudents);
 		
 		VizState.getState().addVizUpdateListener(this);
 	}
 
 	@Override
 	public Component getListCellRendererComponent(JList<? extends String> list,
 			String value, int index, boolean isSelected, boolean cellHasFocus) {
 		
 		setOpaque(true);
 		
 		if (tests != null) {
 			setBackground(students.contains(value) ? Colors.pass : Colors.fail);
 		} else if (student != null && student == value) {
 			setBackground(Colors.hover);
 		} else {
 			setBackground(list.getBackground());
 		}
 		
 		setForeground(Colors.foreground);
 		setText(value);
 		return this;
 	}
 
 	@Override
 	public void vizUpdated(VizEventType eventType) {
 		VizState state = VizState.getState();
 		
 		if (eventType == VizEventType.HOVERING_TESTS) {		
 			tests = state.getMousedOverTests();
 			
 			if (tests != null) {
 				students = new HashSet<>();
 				for (String test : tests) {
 					students.addAll(state.getDataset().getPassersOfTest(test));
 				}
 			} else {
 				students = null;
 			}
 		} else if (eventType == VizEventType.HOVERING_STUDENT) {
 			student = state.getMousedOverStudent();
 		}
 		
 		lstStudents.repaint();
 	}
 	
 	class StudentListCellMouseAdapter extends MouseAdapter {
 		private JList<String> lstStudents;
 		
 		public StudentListCellMouseAdapter(JList<String> list) {
 			lstStudents = list;
 		}
 		
 		@Override
 		public void mouseMoved(MouseEvent e) {
 			int index = lstStudents.locationToIndex(e.getPoint());
 			String student = lstStudents.getModel().getElementAt(index);
 		
 			VizState.getState().setMousedOverStudent(student);
 		}
 		
 		@Override
 		public void mouseExited(MouseEvent e) {
 			VizState.getState().setMousedOverStudent(null);
 		}
 	}
 }
