 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.Serializable;
 import java.util.Vector;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.TitledBorder;
 
 public class ManualModFrame extends JFrame implements ActionListener,
 		Serializable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 10L;
 	private JPanel contentPane;
 	private JTextField txtFieldFirstName;
 	private JTextField txtFieldLastName;
 	private JTextField txtFieldStudentID;
 	private JTextField txtFieldAge;
 	private JComboBox combBoxBhLevel;
 	private JComboBox combBoxMathAsses;
 	private JComboBox combBoxReadAsses;
 	private JComboBox combBoxLaAsses;
 	private JComboBox combBoxMath;
 	private JComboBox combBoxLA;
 	private JComboBox combBoxRead;
 	private JComboBox combBoxHomeroom;
 	private JComboBox combBoxSpecials;
 	private JButton disenroll;
 	private JButton btnOk;
 	private JButton btnCancel;
 	private ScheduleDisplay sched;
 	private Students std;
 	private Classes mathClass, laClass, readClass, hmrmClass, specClass;
 	private ClassFactory clsFac;
 
 	/**
 	 * Create the frame.
 	 */
 	public ManualModFrame(Students student, ScheduleDisplay sched,
 			ClassFactory cf) {
 		std = student;
 		this.sched = sched;
 		clsFac = cf;
 		buildFrame();
 	}
 
 	private void buildFrame() {
 		mathClass = std.getMathCls();
 		laClass = std.getLACls();
 		readClass = std.getReadCls();
 		hmrmClass = std.getHomeroomCls();
 		specClass = std.getSpecialCls();
		
 
 		// create the frame
 		setAlwaysOnTop(true);
 		setResizable(false);
 		setTitle("Move Student");
 		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		setExtendedState(NORMAL);
 		setMaximizedBounds(new Rectangle(100, 100, 300, 450));
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
 
 		// create the student panel
 		JPanel stdPanel = new JPanel();
 		stdPanel.setBorder(new TitledBorder(null, "Student",
 				TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		stdPanel.setLayout(new GridLayout(4, 2));
 		contentPane.add(stdPanel);
 
 		txtFieldStudentID = new JTextField(std.getId() + "");
 		txtFieldStudentID.setEditable(false);
 
 		txtFieldFirstName = new JTextField(std.getFirstName());
 		txtFieldFirstName.setEditable(false);
 
 		txtFieldLastName = new JTextField(std.getLastName());
 		txtFieldLastName.setEditable(false);
 
 		txtFieldAge = new JTextField("" + (int) std.getAge());
 		txtFieldAge.setEditable(false);
 
 		stdPanel.add(new JLabel("Student ID"));
 		stdPanel.add(txtFieldStudentID);
 		stdPanel.add(new JLabel("First Name"));
 		stdPanel.add(txtFieldFirstName);
 		stdPanel.add(new JLabel("Last Name"));
 		stdPanel.add(txtFieldLastName);
 		stdPanel.add(new JLabel("Age"));
 		stdPanel.add(txtFieldAge);
 
 		// create the assessment panel
 		JPanel assesPanel = new JPanel();
 		assesPanel.setBorder(new TitledBorder(null, "Assessment",
 				TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		assesPanel.setLayout(new GridLayout(4, 2));
 		contentPane.add(assesPanel);
 
 		combBoxBhLevel = new JComboBox();
 		combBoxBhLevel.addItem(new Integer(1));
 		combBoxBhLevel.addItem(new Integer(2));
 		combBoxBhLevel.addItem(new Integer(3));
 		combBoxBhLevel.setSelectedItem(new Integer(std.getBL()));
 
 		String[] validStates = { "K", "1", "2", "3", "4", "5", "6", "7", "8" };
 		combBoxMathAsses = new JComboBox(validStates);
 		combBoxMathAsses.setSelectedItem(new Integer(std.getMath()).toString());
 		combBoxLaAsses = new JComboBox(validStates);
 		combBoxLaAsses.setSelectedItem(new Integer(std.getLA()).toString());
 		combBoxReadAsses = new JComboBox(validStates);
 		combBoxReadAsses.setSelectedItem(new Integer(std.getRead()).toString());
 
 		assesPanel.add(new JLabel("Behavioral Lavel"));
 		assesPanel.add(combBoxBhLevel);
 		assesPanel.add(new JLabel("Math"));
 		assesPanel.add(combBoxMathAsses);
 		assesPanel.add(new JLabel("LA"));
 		assesPanel.add(combBoxLaAsses);
 		assesPanel.add(new JLabel("Reading"));
 		assesPanel.add(combBoxReadAsses);
 
 		// create the schedule panel
 		JPanel schedPanel = new JPanel();
 		schedPanel.setBorder(new TitledBorder(null, "Schedule",
 				TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		schedPanel.setLayout(new GridLayout(5, 2));
 		contentPane.add(schedPanel);
 
 		// add list of current math classes
 		Vector<String> mathClassNames = new Vector<String>();
 		for (int i = 0; i < clsFac.mathClsLst.size(); i++) {
 			mathClassNames.add(clsFac.mathClsLst.get(i).getFormalClassName());
 		}
 		mathClassNames.add("No Class");
 		combBoxMath = new JComboBox(mathClassNames);
 		if (mathClass != null) {
 			combBoxMath.setSelectedItem(mathClass.getFormalClassName());
 		} else {
 			combBoxMath.setSelectedItem("No Class");
 		}
 
 		// add list of current la classes
 		Vector<String> laClassNames = new Vector<String>();
 		for (int i = 0; i < clsFac.laClsLst.size(); i++) {
 			laClassNames.add(clsFac.laClsLst.get(i).getFormalClassName());
 		}
 		laClassNames.add("No Class");
 		combBoxLA = new JComboBox(laClassNames);
 		if (laClass != null) {
 			combBoxLA.setSelectedItem(laClass.getFormalClassName());
 		} else {
 			combBoxLA.setSelectedItem("No Class");
 		}
 
 		// add list of current reading classes
 		Vector<String> readClassNames = new Vector<String>();
 		for (int i = 0; i < clsFac.readClsLst.size(); i++) {
 			readClassNames.add(clsFac.readClsLst.get(i).getFormalClassName());
 		}
 		readClassNames.add("No Class");
 		combBoxRead = new JComboBox(readClassNames);
 		if (readClass != null) {
 			combBoxRead.setSelectedItem(readClass.getFormalClassName());
 		} else {
 			combBoxRead.setSelectedItem("No Class");
 		}
 
 		// add list of current homeroom classes
 		Vector<String> hmrmClassNames = new Vector<String>();
 		for (int i = 0; i < clsFac.homeroomClsLst.size(); i++) {
 			hmrmClassNames.add(clsFac.homeroomClsLst.get(i)
 					.getFormalClassName());
 		}
 		hmrmClassNames.add("No Class");
 		combBoxHomeroom = new JComboBox(hmrmClassNames);
 		if (hmrmClass != null) {
 			combBoxHomeroom.setSelectedItem(hmrmClass.getFormalClassName());
 		} else {
 			combBoxHomeroom.setSelectedItem("No Class");
 		}
 
 		// add list of current special classes
 		Vector<String> specClassNames = new Vector<String>();
 		for (int i = 0; i < clsFac.specialClsLst.size(); i++) {
 			specClassNames
 					.add(clsFac.specialClsLst.get(i).getFormalClassName());
 		}
 		specClassNames.add("No Class");
 		combBoxSpecials = new JComboBox(specClassNames);
 		if (specClass != null) {
 			combBoxSpecials.setSelectedItem(specClass.getFormalClassName());
 		} else {
 			combBoxSpecials.setSelectedItem("No Class");
 		}
 
 		schedPanel.add(new JLabel("Math Class"));
 		schedPanel.add(combBoxMath);
 		schedPanel.add(new JLabel("LA Class"));
 		schedPanel.add(combBoxLA);
 		schedPanel.add(new JLabel("Reading Class"));
 		schedPanel.add(combBoxRead);
 		schedPanel.add(new JLabel("Homeroom"));
 		schedPanel.add(combBoxHomeroom);
 		schedPanel.add(new JLabel("Specials"));
 		schedPanel.add(combBoxSpecials);
 
 		// create button panel
 		JPanel btnPanel = new JPanel();
 		btnPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
 		contentPane.add(btnPanel);
 
 		disenroll = new JButton("Disenroll");
 		disenroll.addActionListener(this);
 
 		btnOk = new JButton("Ok");
 		btnOk.addActionListener(this);
 
 		btnCancel = new JButton("Cancel");
 		btnCancel.addActionListener(this);
 
 		btnPanel.add(disenroll);
 		btnPanel.add(btnOk);
 		btnPanel.add(btnCancel);
 
 		pack();
 		setVisible(true);
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (e.getSource() == disenroll) {
 			int rtn = JOptionPane.showConfirmDialog(this,
 					"You are about to remove this student from the database.\n"
 							+ "Are you sure you want to continue?", "Warning!",
 					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
 			if (rtn == JOptionPane.YES_OPTION) {
 				System.out.println("removing the student " + std.getId());
 				// remove the student from all classes and the database
 				removeStudent();
 				// update the schedule table
 				sched.update();
 				MainFrame.tabbedPane.revalidate();
 				MainFrame.tabbedPane.setVisible(false);
 				MainFrame.tabbedPane.repaint();
 				MainFrame.tabbedPane.setVisible(true);
 				this.dispose();
 			}
 		} else if (e.getSource() == btnOk) {
 			updateStdInfo();
 			moveStudent();
 			// update the schedule table
 			sched.update();
 			MainFrame.tabbedPane.revalidate();
 			MainFrame.tabbedPane.setVisible(false);
 			MainFrame.tabbedPane.repaint();
 			MainFrame.tabbedPane.setVisible(true);
 			this.dispose();
 		} else if (e.getSource() == btnCancel) {
 			this.dispose();
 		}
 
 	}
 
 	private void removeStudent() {
 		System.out.println(std.toString());
 		clsFac.kickout(std);
 	}
 
 	private void updateStdInfo() {
 		int bh, math, la, read;
 		bh = (Integer) combBoxBhLevel.getSelectedItem();
 		String selMath = (String) combBoxMathAsses.getSelectedItem(), selLA = (String) combBoxLaAsses
 				.getSelectedItem(), selRead = (String) combBoxReadAsses
 				.getSelectedItem();
 		if (selMath.equals("K")) {
 			math = 0;
 		} else {
 			math = Integer.parseInt(selMath);
 		}
 		if (selLA.equals("K")) {
 			la = 0;
 		} else {
 			la = Integer.parseInt(selLA);
 		}
 		if (selRead.equals("K")) {
 			read = 0;
 		} else {
 			read = Integer.parseInt(selRead);
 		}
 
 		if (bh != std.getBL()) {
 			std.setBL(bh);
 		}
 		if (math != std.getMath()) {
 			std.setMath(math);
 		}
 		if (la != std.getLA()) {
 			std.setLA(la);
 		}
 		if (read != std.getRead()) {
 			std.setRead(read);
 		}
 	}
 
 	private void moveStudent() {
 		String newMath, newRead, newLA, newHmrm, newSpec;
 
 		newMath = (String) combBoxMath.getSelectedItem();
 		newRead = (String) combBoxRead.getSelectedItem();
 		newLA = (String) combBoxLA.getSelectedItem();
 		newHmrm = (String) combBoxHomeroom.getSelectedItem();
 		newSpec = (String) combBoxSpecials.getSelectedItem();
 
 		// Extra checks if student is on waitlist
 		if (!std.getWlReason().equals("")) {
 			if (newMath.contains("No Class") || newRead.contains("No Class")
 					|| newLA.contains("No Class")
 					|| newHmrm.contains("No Class")
 					|| newSpec.contains("No Class")) {
 				int rtn = JOptionPane
 						.showConfirmDialog(
 								this,
 								std.getFirstName()
 										+ " "
 										+ std.getLastName()
 										+ " was not assigned classes for each subject.\n"
 										+ "Do you want to continue?",
 								"Warning", JOptionPane.YES_NO_OPTION,
 								JOptionPane.WARNING_MESSAGE);
 
 				if (rtn == JOptionPane.NO_OPTION) {
 					return;
 				}
 			}
 		}
 		Classes newMathCls = null, newReadCls = null, newLACls = null, newHmrmCls = null, newSpecCls = null;
 		// get class that corresponds to the newClass name
 		for (int i = 0; i < clsFac.mathClsLst.size(); i++) {
 			//String[] parts = newMath.split("_");
 			//int id = Integer.parseInt(parts[1]);
 			if (newMath.equals(clsFac.mathClsLst.get(i).getFormalClassName())) {
 				newMathCls = clsFac.mathClsLst.get(i);
 				break;
 			}
 		}
 		for (int i = 0; i < clsFac.readClsLst.size(); i++) {
 			if (newRead.equals(clsFac.readClsLst.get(i).getFormalClassName())) {
 				newReadCls = clsFac.readClsLst.get(i);
 				break;
 			}
 		}
 		for (int i = 0; i < clsFac.laClsLst.size(); i++) {
 			if (newLA.equals(clsFac.laClsLst.get(i).getFormalClassName())) {
 				newLACls = clsFac.laClsLst.get(i);
 				break;
 			}
 		}
 		for (int i = 0; i < clsFac.homeroomClsLst.size(); i++) {
 			if (newHmrm.equals(clsFac.homeroomClsLst.get(i).getFormalClassName())) {
 				newHmrmCls = clsFac.homeroomClsLst.get(i);
 				break;
 			}
 		}
 		for (int i = 0; i < clsFac.specialClsLst.size(); i++) {
 			if (newSpec.equals(clsFac.specialClsLst.get(i).getFormalClassName())) {
 				newSpecCls = clsFac.specialClsLst.get(i);
 				break;
 			}
 		}
 		if (newMath == null || newRead == null || newLA == null
 				|| newHmrm == null || newSpec == null) {
 			// System.out.println("Error: new class does not exist");
 			// return;
 			// error
 		}
 		// put the student in the new class
 		if (mathClass != newMathCls) {
 			if (newMath.equals("No Class")) {
 				// TODO: remove the student
 			} else if (check(mathClass, newMathCls)) {
 				clsFac.moveStd(mathClass, newMathCls, std);
 				// System.out.println("student moved from "+mathClass.getFormalClassName()+" to "+newMathCls.getFormalClassName());
 			}
 		}
 		if (readClass != newReadCls) {
 			if (newRead.equals("No Class")) {
 				// TODO: remove the student
 			} else if (check(readClass, newReadCls)) {
 				clsFac.moveStd(readClass, newReadCls, std);
 				// System.out.println("student moved from "+readClass.getFormalClassName()+" to "+newReadCls.getFormalClassName());
 			}
 		}
 		if (laClass != newLACls) {
 			if (newLA.equals("No Class")) {
 				// TODO: remove the student
 			} else if (check(laClass, newLACls)) {
 				clsFac.moveStd(laClass, newLACls, std);
 				// System.out.println("student moved from "+laClass.getFormalClassName()+" to "+newLACls.getFormalClassName());
 			}
 		}
 		if (hmrmClass != newHmrmCls) {
 			if (newHmrm.equals("No Class")) {
 				// TODO: remove the student
 			} else if (check(hmrmClass, newHmrmCls)) {
 				clsFac.moveStd(hmrmClass, newHmrmCls, std);
 				// System.out.println("student moved from "+hmrmClass.getFormalClassName()+" to "+newHmrmCls.getFormalClassName());
 			}
 		}
 		if (specClass != newSpecCls) {
 			if (newSpec.equals("No Class")) {
 				// TODO: remove the student
 			} else if (check(specClass, newSpecCls)) {
 				clsFac.moveStd(specClass, newSpecCls, std);
 				// System.out.println("student moved from "+specClass.getFormalClassName()+" to "+newSpecCls.getFormalClassName());
 			}
 		}
 	}
 
 	private boolean check(Classes fromCls, Classes toCls) {
 		int rtn;
 		if (toCls.getTotal() == 5) {
 			rtn = JOptionPane.showConfirmDialog(this,
 					"Class " + toCls.getFormalClassName() + " already has "
 							+ toCls.getTotal() + " students.\n"
 							+ "Do you want to continue?", "Warning",
 					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
 			if (rtn == JOptionPane.NO_OPTION)
 				return false;
 		} else if (Math.abs(toCls.getLowestAge() - std.getAge()) > 3.92) {
 			rtn = JOptionPane.showConfirmDialog(this, std.getFirstName() + " "
 					+ std.getLastName()
 					+ " is outside of the age range for this class.\n"
 					+ "Do you want to continue?", "Warning",
 					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
 			if (rtn == JOptionPane.NO_OPTION)
 				return false;
 		} else if (!clsFac.BLfit(std, toCls)) {
 			rtn = JOptionPane.showConfirmDialog(this, "There are already "
 					+ toCls.getBL3() + " behavior level 3 students and "
 					+ toCls.getBL2()
 					+ "\nbehavior level 2 students in this class.\n"
 					+ "Do you want to continue?", "Warning",
 					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
 			if (rtn == JOptionPane.NO_OPTION)
 				return false;
 		}
 		if (toCls.getClsName().equals("math")) {
 			if (std.getMath() != toCls.getLvl()) {
 				rtn = JOptionPane.showConfirmDialog(this,
 						std.getFirstName() + " " + std.getLastName()
 								+ " is math level " + std.getMath()
 								+ " and class " + toCls.getFormalClassName()
 								+ " is level " + toCls.getLvl() + ".\n"
 								+ "Do you want to continue?", "Warning",
 						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
 				if (rtn == JOptionPane.NO_OPTION)
 					return false;
 			}
 		} else if (toCls.getClsName().equals("la")) {
 			if (std.getLA() != toCls.getLvl()) {
 				rtn = JOptionPane.showConfirmDialog(this,
 						std.getFirstName() + " " + std.getLastName()
 								+ " is lang arts level " + std.getLA()
 								+ " and class " + toCls.getFormalClassName()
 								+ " is level " + toCls.getLvl() + ".\n"
 								+ "Do you want to continue?", "Warning",
 						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
 				if (rtn == JOptionPane.NO_OPTION)
 					return false;
 			}
 		} else if (toCls.getClsName().equals("read")) {
 			if (std.getRead() != toCls.getLvl()) {
 				rtn = JOptionPane.showConfirmDialog(this,
 						std.getFirstName() + " " + std.getLastName()
 								+ " is reading level " + std.getRead()
 								+ " and class " + toCls.getFormalClassName()
 								+ " is level " + toCls.getLvl() + ".\n"
 								+ "Do you want to continue?", "Warning",
 						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
 				if (rtn == JOptionPane.NO_OPTION)
 					return false;
 			}
 		}
 		return true;
 	}
 }
