 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Frame;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.print.PrinterException;
 import java.io.BufferedOutputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectOutput;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.io.Serializable;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTable;
 import javax.swing.JTable.PrintMode;
 import javax.swing.SwingUtilities;
 import javax.swing.table.JTableHeader;
 
 public class MainFrame implements ActionListener, MouseListener, Serializable {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	transient static JTabbedPane tabbedPane;
 	transient static JFrame frame;
 	transient StudentController sc;
 	transient TeacherController tc;
 	transient StudentTable sTab;
 	transient TeacherTable tTab;
 	transient JScrollPane panel1, panel2, panel3;
 	transient ScheduleDisplay sched;
 	StudentDB students;
 	TeacherDB teachers;
 	ClassFactory clsFac;
 	transient Menu menu;
 	transient JPopupMenu rightClickMenu;
 	transient JMenuItem editItem;
 	transient AddStudentFrame addStd;
 
 	public MainFrame() {
 		if (students == null)
 			students = new StudentDB(clsFac);
 		if (teachers == null)
 			teachers = new TeacherDB();
 		clsFac = new ClassFactory();
 		frame = new JFrame("Scheduling Tool");
 
 		update();
 	}
 
 	public void update() {
 		students.reInit();
 		frame.setVisible(false); // Hide the old frame, this is probably NOT
 									// efficient
 		// create the right click menu
 		rightClickMenu = new JPopupMenu();
 		editItem = new JMenuItem("Edit");
 		editItem.addActionListener(this);
 		rightClickMenu.add(editItem);
 		frame = new JFrame("Scheduling Tool");
 		frame.validate();
 		frame.setState(Frame.NORMAL);
 		Toolkit toolkit = Toolkit.getDefaultToolkit();
 		Dimension dimension = toolkit.getScreenSize();
 		frame.setSize(dimension);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setExtendedState(Frame.MAXIMIZED_BOTH);
 
 		tabbedPane = new JTabbedPane();
 
 		sTab = new StudentTable(frame, students, clsFac);
 		panel1 = new JScrollPane(sTab.getStudentTable());
 		tabbedPane.addTab("Student Entry", panel1);
 		tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
 
 		tTab = new TeacherTable(frame, teachers, clsFac);
 		tTab.getTeacherTable().addMouseListener(this);
 		panel2 = new JScrollPane(tTab.getTeacherTable());
 		tabbedPane.addTab("Teacher Entry", panel2);
 		tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);
 
 		addStd = new AddStudentFrame(this);
 
 		sc = new StudentController(frame, students, addStd, clsFac);
 		tc = new TeacherController(frame, teachers, clsFac);
 
 		menu = new Menu(this, students, frame, sc, teachers, tc, clsFac);
 		frame.setJMenuBar(menu.getMenu());
 
 		sched = new ScheduleDisplay(clsFac);
 		sched.getScheduleTable().addMouseListener(this);
 		panel3 = new JScrollPane(sched.getScheduleTable());
 		tabbedPane.addTab("Schedule", panel3);
 		tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);
 
 		// Add the tabbed pane to this panel.
 		frame.add(tabbedPane);
 
 		// The following line enables to use scrolling tabs.
 		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
 		frame.setVisible(true);
 	}
 
 	// All actions that update the table view need to happen here
 	public void actionPerformed(ActionEvent e) {
 		Object obj = e.getSource();
 		JFileChooser chooser = new JFileChooser();
 		if (obj.equals(Menu.sOpen)) {
 			// use chooser.getSelectedFile() to get file
 			// Some code here to parse or call a parser
 			if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION)
 				sc.readFile(chooser.getSelectedFile());
 			sTab.update();
 			tabbedPane.setSelectedIndex(0);
 
 		} else if (obj.equals(Menu.tOpen)) {
 			// use chooser.getSelectedFile() to get file
 			// Some code here to parse or call a parser
 			if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION)
 				tc.readFile(chooser.getSelectedFile());
 			tTab.update();
 			tabbedPane.setSelectedIndex(1);
 
 		} else if (obj.equals(Menu.schedulize)) {
 			// Code here to call schedule algorithm and display schedules
 			Schedulizer.genSchedule(students, clsFac);
 			sched.update();
 			tabbedPane.setSelectedIndex(2);
 		} else if (obj.equals(Menu.assign)) {
 			// TODO: Code here to call schedule algorithm and display schedules
 			ScheduleTeachers.assign(teachers, clsFac);
 			sched.update();
 			tabbedPane.setSelectedIndex(2);
 		} else if (obj.equals(editItem)) {
 			showManualMod();
 			
 		} else if (obj.equals(addStd.add)) {
 			String tmpID = addStd.txtFieldStudentID.getText();
 			int id = -1;
 			try {
 				tmpID.trim();
 				id = Integer.parseInt(tmpID);
 				if (students.hasStudent(id)) {
 					JOptionPane
 							.showMessageDialog(frame,
 									"A student with that ID already exists in the scheduling system.");
 				} else {
 					String fName = addStd.txtFieldFirstName.getText();
 					String lName = addStd.txtFieldLastName.getText();
 					String b = addStd.txtFieldBirthDate.getText();
 					if (Utilities.isBlank(fName)) {
 						JOptionPane.showMessageDialog(frame,
 								"Please Enter Student's First Name.");
 					} else if (Utilities.isBlank(lName)) {
 						JOptionPane.showMessageDialog(frame,
 								"Please Enter Student's Last Name.");
 					} else if (Utilities.isBlank(b)) {
 						JOptionPane.showMessageDialog(frame,
 								"Please Enter Student's Birth Date.");
 					} else {
 						// Everything has been populated
 						// Check birth date
 						DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
 						Date bDate = df.parse(b);
 						Calendar c = new GregorianCalendar();
 						c.setTime(bDate);
 						int year = c.get(Calendar.YEAR);
 						if (year < 1900) {
 							JOptionPane
 									.showMessageDialog(frame,
 											"Invalid Year. Expected Birth Date in the form yyyy-mm-dd");
 						} else {
 							// Everything valid, create student
 							String math = addStd.combBoxMathAsses
 									.getSelectedItem().toString();
 							int m = (math.equals("K")) ? 0 : Integer
 									.parseInt(math);
 
 							String read = addStd.combBoxReadAsses
 									.getSelectedItem().toString();
 							int r = (read.equals("K")) ? 0 : Integer
 									.parseInt(read);
 
 							String LA = addStd.combBoxLaAsses.getSelectedItem()
 									.toString();
 							int l = (LA.equals("K")) ? 0 : Integer.parseInt(LA);
 
 							String bhlevel = addStd.combBoxBhLevel
 									.getSelectedItem().toString();
 							int bh = Integer.parseInt(bhlevel);
 
 							Students s = new Students(id, fName, lName, bDate,
 									m, l, r, bh, clsFac);
 							students.addStudent(s);
 							// TODO: Call to Schedulizer to try to add an
 							// individual student
 							Schedulizer.addNewStd(s, clsFac);
 							sched.update();
 						}
 					}
 				}
 			} catch (NumberFormatException n) {
 				JOptionPane.showMessageDialog(frame,
 						"Student ID should be an integer value.");
 			} catch (ParseException p) {
 				JOptionPane.showMessageDialog(frame,
 						"Expected Birth Date in the form yyyy-mm-dd");
 			} catch (StdClsCompatibleException se) {
 				// TODO Auto-generated catch block
 				addStd.dispose();
 				JOptionPane.showMessageDialog(frame,
 						"Unable to place student in current schedule.");
 				sched.update();
 			}
 
 			addStd.dispose();
 		} else if (obj.equals(Menu.lock)) {
 			menu.lock();
 		} else if (obj.equals(Menu.unlock)) {
 			menu.unlock();
 		} else if (obj.equals(Menu.print)) {
 			try {
 				JTable p = sched.getScheduleTable();
 				int h = p.getRowHeight();
 				Font f = p.getFont();
 				JTableHeader hh = p.getTableHeader();
 				p.setTableHeader(null);
 				p.setRowHeight(9);
 				p.setFont(new Font("Arial", Font.PLAIN, 8));
 				boolean complete = p.print(PrintMode.NORMAL);
 				p.setTableHeader(hh);
 				p.setFont(f);
 				p.setRowHeight(h);
 				if (complete) {
 					/* show a success message */
 
 				} else {
 					/* show a message indicating that printing was cancelled */
 					JOptionPane.showMessageDialog(frame,
 							"Print Job was Cancelled");
 				}
 			} catch (PrinterException pe) {
 				/* Printing failed, report to the user */
 				JOptionPane.showMessageDialog(frame, "Print Job Failed");
 
 			}
 		} else if (obj.equals(Menu.save)) {
 			if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
 				saveObject(this, chooser.getSelectedFile().toString());
 			}
 
 		} else if (obj.equals(Menu.load)) {
 			if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION)
 				SchedulingTool.loadObject(chooser.getSelectedFile().toString());
 
 		}
 		tabbedPane.revalidate();
 		tabbedPane.setVisible(false);
 		tabbedPane.repaint();
 		tabbedPane.setVisible(true);
 	}
 
 	private void showManualMod() {
 		try {
 			int x, y;
 			x = sched.getScheduleTable().getSelectedColumn();
 			y = sched.getScheduleTable().getSelectedRow();
 			Object cell = sched.getScheduleTable().getValueAt(y, x);
 			// make sure x and y correspond to a student or class
 			if (cell.toString().equals("")) {
 				// do nothing
 			} else if (cell.toString().startsWith("Ages")) {
 				// do nothing
 			} else if (cell.toString().startsWith("Math") ||
 					cell.toString().startsWith("Reading") ||
 					cell.toString().startsWith("Lang.") ||
 					cell.toString().startsWith("Homeroom") ||
 					cell.toString().startsWith("Special")) {
 				// figure out what class this is
 				Classes cls = findClass(cell);
 				Teachers t = null;
 				if (cls != null) {
 					t = cls.getTeacher();
 					new TeacherModFrame(t, clsFac, cls, students, sTab, sched);
 				} else {
 					JOptionPane.showMessageDialog(frame, "Class not found.",
 							"Error", JOptionPane.ERROR_MESSAGE);
 				}
 			} else if (x > 0 && y >= 0) {
 				new ManualModFrame((Students) cell, sched, clsFac);
 			}
 		} catch (NullPointerException np) {
 			// Do Nothing
 		} catch (ArrayIndexOutOfBoundsException a) {
 			// Do Nothing
 		}
 	}
 
 	private Classes findClass(Object cell) {
 		String str = cell.toString();
 		/*int index = str.indexOf(' '); // space before level
 		index++;
 		index = str.indexOf(' ', index); // space before id
 		index++;
 		int end = str.indexOf(':');
 		if (end < 0)
 			str = str.substring(index);
 		else
 			str = str.substring(index, end);
 		int id = Integer.parseInt(str);*/
 		// str should now contain the class id, i think
 		
 		String[] fields = str.split(":");
 		String dispName = fields[0].trim();
 		for (Classes c : clsFac.readClsLst) {
 			if (dispName.equals(c.getTableName())) {
 				return c;
 			}
 		}
 		for (Classes c : clsFac.laClsLst) {
 			if (dispName.equals(c.getTableName())) {
 				return c;
 			}
 		}
 		for (Classes c : clsFac.mathClsLst) {
 			if (dispName.equals(c.getTableName())) {
 				return c;
 			}
 		}
 		for (Classes c : clsFac.homeroomClsLst) {
 			if (dispName.equals(c.getTableName())) {
 				return c;
 			}
 		}
 		for (Classes c : clsFac.specialClsLst) {
 			if (dispName.equals(c.getTableName())) {
 				return c;
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseExited(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 		// TODO Auto-generated method stub
 		if (e.getClickCount() == 2) {
 			if (e.getSource() == sched.getScheduleTable()) {
 				int x, y;
 				x = sched.getScheduleTable().getSelectedColumn();
 				y = sched.getScheduleTable().getSelectedRow();
 				Object cell = sched.getScheduleTable().getValueAt(y, x);
 				if (cell.toString().equals("")) {
 					// do nothing
 				} else if (cell.toString().startsWith("Ages")) {
 					// do nothing
 				} else if (cell.toString().startsWith("Math") ||
 						cell.toString().startsWith("Reading") ||
 						cell.toString().startsWith("Lang.") ||
 						cell.toString().startsWith("Homeroom") ||
 						cell.toString().startsWith("Special")) {
 					// figure out what class this is
 					Classes cls = findClass(cell);
 					Teachers t = null;
 					if (cls != null) {
 						t = cls.getTeacher();
 						if (t != null) {
 							new TeacherScheduleFrame(t);
 						} else {
 							JOptionPane.showMessageDialog(frame,
 									"This class does not have a teacher.",
 									"Error", JOptionPane.ERROR_MESSAGE);
 						}
 					} else {
 						JOptionPane.showMessageDialog(frame,
 								"Class not found.", "Error",
 								JOptionPane.ERROR_MESSAGE);
 					}
				} else if (x >= 0 && y >= 0) {
 					new StudentScheduleFrame((Students) cell);
 				}
 			}
 		}
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		// TODO Auto-generated method stub
 		try {
 			if (SwingUtilities.isRightMouseButton(e)) {
 				if (e.getSource() == sched.getScheduleTable()) {
 					rightClickMenu.show(e.getComponent(), e.getX(), e.getY());
 				}
 			}
 		} catch (NullPointerException np) {
 			// Do nothing
 		}
 	}
 
 	/**
 	 * Save object on disk.
 	 * 
 	 * @param mf
 	 * @param fileName
 	 */
 	public void saveObject(MainFrame mf, String fileName) {
 		try {
 			// use buffering
 			OutputStream file = new FileOutputStream(fileName);
 			OutputStream buffer = new BufferedOutputStream(file);
 			ObjectOutput output = new ObjectOutputStream(buffer);
 			try {
 				output.writeObject(mf);
 			} finally {
 				output.close();
 			}
 		} catch (IOException ex) {
 			System.err.println("Unable to write file to disk");
 			ex.printStackTrace();
 		}
 	}
 
 }
