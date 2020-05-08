 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 import javax.swing.JLabel;
 import javax.swing.BoxLayout;
 import javax.swing.border.TitledBorder;
 import javax.swing.JOptionPane;
 import javax.swing.JTextField;
 import javax.swing.JComboBox;
 import javax.swing.JButton;
 import java.awt.Component;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Vector;
 
 
 public class ManualModFrame extends JFrame implements ActionListener{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 10L;
 	private JPanel contentPane;
 	private JTextField txtFieldFirstName;
 	private JTextField txtFieldLastName;
 	private JTextField txtFieldStudentID;
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
 
 	/**
 	 * Create the frame.
 	 */
 	public ManualModFrame(Students student, ScheduleDisplay sched) {
 		std = student;
 		this.sched = sched;
 		buildFrame();
 	}
 	
 	private void buildFrame() {
 		mathClass = std.getMathCls();
 		laClass = std.getLACls();
 		readClass = std.getReadCls();
 		hmrmClass = std.getHomeroomCls();
 		specClass = std.getSpecialCls();
 		
 		//create the frame
 		setAlwaysOnTop(true);
 		setResizable(false);
 		setTitle("Move Student");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 230, 400);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
 		
 		//create the student panel
 		JPanel stdPanel = new JPanel();
 		stdPanel.setBorder(new TitledBorder(null, "Student", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		stdPanel.setLayout(new GridLayout(3,2));
 		contentPane.add(stdPanel);
 		
 		txtFieldStudentID = new JTextField(std.getId()+"");
 		txtFieldStudentID.setEditable(false);
 		
 		txtFieldFirstName = new JTextField(std.getFirstName());
 		txtFieldFirstName.setEditable(false);
 		
 		txtFieldLastName = new JTextField(std.getLastName());
 		txtFieldLastName.setEditable(false);
 		
 		stdPanel.add(new JLabel("Student ID"));
 		stdPanel.add(txtFieldStudentID);
 		stdPanel.add(new JLabel("First Name"));
 		stdPanel.add(txtFieldFirstName);
 		stdPanel.add(new JLabel("Last Name"));
 		stdPanel.add(txtFieldLastName);
 		
 		//create the assessment panel
 		JPanel assesPanel = new JPanel();
 		assesPanel.setBorder(new TitledBorder(null, "Assessment", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		assesPanel.setLayout(new GridLayout(4,2));
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
 		
 		//create the schedule panel
 		JPanel schedPanel = new JPanel();
 		schedPanel.setBorder(new TitledBorder(null, "Schedule", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		schedPanel.setLayout(new GridLayout(5,2));
 		contentPane.add(schedPanel);
 		
 		//add list of current math classes
 		Vector<String> mathClassNames = new Vector<String>();
 		for (int i=0; i<ClassFactory.mathClsLst.size(); i++) {
 			mathClassNames.add(ClassFactory.mathClsLst.get(i).getClsName());
 		}
 		mathClassNames.add("No Class");
 		combBoxMath = new JComboBox(mathClassNames);
 		if (mathClass != null) {
 			combBoxMath.setSelectedItem(mathClass.getClsName());
 		} else {
 			combBoxMath.setSelectedItem("No Class");
 		}
 		
 		//add list of current la classes
 		Vector<String> laClassNames = new Vector<String>();
 		for (int i=0; i<ClassFactory.laClsLst.size(); i++) {
 			laClassNames.add(ClassFactory.laClsLst.get(i).getClsName());
 		}
 		laClassNames.add("No Class");
 		combBoxLA = new JComboBox(laClassNames);
 		if (laClass != null) {
 			combBoxLA.setSelectedItem(laClass.getClsName());
 		} else {
 			combBoxLA.setSelectedItem("No Class");
 		}
 		
 		//add list of current reading classes
 		Vector<String> readClassNames = new Vector<String>();
 		for (int i=0; i<ClassFactory.readClsLst.size(); i++) {
 			readClassNames.add(ClassFactory.readClsLst.get(i).getClsName());
 		}
 		readClassNames.add("No Class");
 		combBoxRead = new JComboBox(readClassNames);
 		if (readClass != null) {
 			combBoxRead.setSelectedItem(readClass.getClsName());
 		} else {
 			combBoxRead.setSelectedItem("No Class");
 		}
 		
 		//add list of current homeroom classes
 		Vector<String> hmrmClassNames = new Vector<String>();
 		for (int i=0; i<ClassFactory.homeroomClsLst.size(); i++) {
 			hmrmClassNames.add(ClassFactory.homeroomClsLst.get(i).getClsName());
 		}
 		hmrmClassNames.add("No Class");
 		combBoxHomeroom = new JComboBox();
 		if (hmrmClass != null) {
 			combBoxHomeroom.setSelectedItem(hmrmClass.getClsName());
 		} else {
 			combBoxHomeroom.setSelectedItem("No Class");
 		}
 		
 		//add list of current special classes
 		Vector<String> specClassNames = new Vector<String>();
 		for (int i=0; i<ClassFactory.specialClsLst.size(); i++) {
 			specClassNames.add(ClassFactory.specialClsLst.get(i).getClsName());
 		}
 		specClassNames.add("No Class");
 		combBoxSpecials = new JComboBox();
 		if (specClass != null) {
 			combBoxSpecials.setSelectedItem(specClass.getClsName());
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
 		
 		//create button panel
 		JPanel btnPanel = new JPanel();
 		btnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
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
 					"You are about to remove the student from the database.\n" +
 					"Are you sure you want to continue?", "Warning!",
 					JOptionPane.YES_NO_OPTION,
 					JOptionPane.WARNING_MESSAGE);
 			if (rtn == JOptionPane.YES_OPTION) {
 				System.out.println("removing the student " + std.getId());
 				//remove the student from all classes and the database
 				removeStudent();
 				this.dispose();
 			}
 		} else if (e.getSource() == btnOk) {
 			updateStdInfo();
 			moveStudent();
 			//update the schedule table
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
 		
 	}
 	
 	private void updateStdInfo() {
 		int bh, math, la, read;
 		bh = (Integer)combBoxBhLevel.getSelectedItem();
 		String selMath = (String) combBoxMathAsses.getSelectedItem(),
 				selLA = (String) combBoxLaAsses.getSelectedItem(),
 				selRead = (String) combBoxReadAsses.getSelectedItem();
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
 		
 		Classes newMathCls=null, newReadCls=null, newLACls=null, newHmrmCls=null, newSpecCls=null;
 		//get class that corresponds to the newClass name
 		for(int i=0; i<ClassFactory.mathClsLst.size(); i++) {
 			if (newMath == ClassFactory.mathClsLst.get(i).getClsName()) {
 				newMathCls = ClassFactory.mathClsLst.get(i);
 				break;
 			}
 		}
 		for(int i=0; i<ClassFactory.readClsLst.size(); i++) {
 			if (newRead == ClassFactory.readClsLst.get(i).getClsName()) {
 				newReadCls = ClassFactory.readClsLst.get(i);
 				break;
 			}
 		}
 		for(int i=0; i<ClassFactory.laClsLst.size(); i++) {
 			if (newLA == ClassFactory.laClsLst.get(i).getClsName()) {
 				newLACls = ClassFactory.laClsLst.get(i);
 				break;
 			}
 		}
 		for(int i=0; i<ClassFactory.homeroomClsLst.size(); i++) {
 			if (newHmrm == ClassFactory.homeroomClsLst.get(i).getClsName()) {
 				newHmrmCls = ClassFactory.homeroomClsLst.get(i);
 				break;
 			}
 		}
 		for(int i=0; i<ClassFactory.specialClsLst.size(); i++) {
 			if (newSpec == ClassFactory.specialClsLst.get(i).getClsName()) {
 				newSpecCls = ClassFactory.specialClsLst.get(i);
 				break;
 			}
 		}
 		if (newMath==null || newRead==null || newLA==null || newHmrm==null || newSpec==null) {
 			//System.out.println("Error: new class does not exist");
 			//return;
 			//error
 		}
 		//put the student in the new class
 		if (mathClass != newMathCls) {
 			if (mathClass == null) {
 				//enroll student
 			} else {
 				try {
 					ClassFactory.moveStd(mathClass, newMathCls, std);
 					System.out.println("student moved from "+mathClass.getClsName()+" to "+newMathCls.getClsName());
 				} catch (StdClsCompatibleException e1) {
 				}
 			}
 		}
 		if (readClass != newReadCls) {
 			if (readClass == null) {
 				//enroll student
 			} else {
 				try {
 					ClassFactory.moveStd(readClass, newReadCls, std);
 					System.out.println("student moved from "+readClass.getClsName()+" to "+newReadCls.getClsName());
 				} catch (StdClsCompatibleException e1) {
 				}
 			}
 		}
 		if (laClass != newLACls) {
 			if (laClass == null) {
 				//enroll student
 			} else {
 				try {
 					ClassFactory.moveStd(laClass, newLACls, std);
 					System.out.println("student moved from "+laClass.getClsName()+" to "+newLACls.getClsName());
 				} catch (StdClsCompatibleException e1) {
 				}
 			}
 		}
 //		if (hmrmClass != newHmrmCls) {
 //			if (hmrmClass == null) {
 //				//enroll student
 //			} else {
 //				try {
 //					ClassFactory.moveStd(hmrmClass, newHmrmCls, std);
 //					System.out.println("student moved from "+hmrmClass.getClsName()+" to "+newHmrmCls.getClsName());
 //				} catch (StdClsCompatibleException e1) {
 //				}
 //			}
 //		}
 //		if (specClass != newSpecCls) {
 //			if (specClass == null) {
 //				//enroll student
 //			} else {
 //				try {
 //					ClassFactory.moveStd(specClass, newSpecCls, std);
 //					System.out.println("student moved from "+specClass.getClsName()+" to "+newSpecCls.getClsName());
 //				} catch (StdClsCompatibleException e1) {
 //				}
 //			}
 //		}
 	}
 }
