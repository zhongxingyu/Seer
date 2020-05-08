 package org.csrdu.apps.nugradingtable;
 
 import java.awt.Color;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.sql.ResultSet;
 
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.UIManager;
 
 public class GradingTableGui extends JFrame implements ActionListener {
 	private static final long serialVersionUID = 1L;
 	JTable gradeTable;
 	JPanel optionsPane;
 	private JLabel label;
 	private JTextField txtCourseName;
 	private JTextField txtCourseCode;
 	private JTextField txtAddCurve;
 	private JTextField txtMulCurve;
 	private JCheckBox cbRound;
 	private JPanel actionsPane;
 	private JButton btnImportExcel;
 	private JButton btnSaveOptions;
 	private StudentClassResults classResults;
 	private JButton btnPopulateFinalGrades;
 	private JPanel statsPane;
 	private JLabel lSAP;
 	private JLabel lSA;
 	private JLabel lSAM;
 	private JLabel lSBP;
 	private JLabel lSB;
 	private JLabel lSBM;
 	private JLabel lSCP;
 	private JLabel lSC;
 	private JLabel lSCM;
 	private JLabel lSDM;
 	private JLabel lSD;
 	private JLabel lSDP;
 	private JLabel lSF;
 
 	public void addComponentsToPane() {
 		setLayout(new GridBagLayout());
 
 		GridBagConstraints c = new GridBagConstraints();
 		c.fill = GridBagConstraints.HORIZONTAL;
 
 		// start setting options Pane
 		optionsPane = new JPanel();
 		optionsPane.setSize(getWidth() / 2, 100);
 		optionsPane.setLayout(new GridBagLayout());
 		GridBagConstraints c2 = new GridBagConstraints();
 
 		label = new JLabel("Course Code");
 		c2.fill = GridBagConstraints.HORIZONTAL;
 		c2.gridx = 0;
 		c2.gridy = 0;
 		c2.weightx = 0.25;
 		optionsPane.add(label, c2);
 
 		txtCourseCode = new JTextField("Course Code");
 		c2.fill = GridBagConstraints.HORIZONTAL;
 		c2.gridx = 1;
 		c2.gridy = 0;
 		c2.weightx = 0.75;
 		optionsPane.add(txtCourseCode, c2);
 
 		label = new JLabel("Course Name");
 		c2.fill = GridBagConstraints.HORIZONTAL;
 		c2.gridx = 0;
 		c2.gridy = 1;
 		c2.weightx = 0.25;
 		optionsPane.add(label, c2);
 
 		txtCourseName = new JTextField("Course Name");
 		c2.fill = GridBagConstraints.HORIZONTAL;
 		c2.gridx = 1;
 		c2.gridy = 1;
 		c2.weightx = 0.75;
 		optionsPane.add(txtCourseName, c2);
 
 		label = new JLabel("Additive Curve");
 		c2.fill = GridBagConstraints.HORIZONTAL;
 		c2.gridx = 0;
 		c2.gridy = 2;
 		c2.weightx = 0.25;
 		optionsPane.add(label, c2);
 
 		txtAddCurve = new JTextField("0");
 		c2.fill = GridBagConstraints.HORIZONTAL;
 		c2.gridx = 1;
 		c2.gridy = 2;
 		c2.weightx = 0.75;
 		optionsPane.add(txtAddCurve, c2);
 
 		label = new JLabel("Multiplicative Curve");
 		c2.fill = GridBagConstraints.HORIZONTAL;
 		c2.gridx = 0;
 		c2.gridy = 3;
 		c2.weightx = 0.25;
 		optionsPane.add(label, c2);
 
 		txtMulCurve = new JTextField("1");
 		c2.fill = GridBagConstraints.HORIZONTAL;
 		c2.gridx = 1;
 		c2.gridy = 3;
 		c2.weightx = 0.75;
 		optionsPane.add(txtMulCurve, c2);
 
 		label = new JLabel("Round?");
 		c2.fill = GridBagConstraints.HORIZONTAL;
 		c2.gridx = 0;
 		c2.gridy = 4;
 		c2.weightx = 0.25;
 		optionsPane.add(label, c2);
 
 		cbRound = new JCheckBox();
 		c2.fill = GridBagConstraints.HORIZONTAL;
 		c2.gridx = 1;
 		c2.gridy = 4;
 		c2.weightx = 0.75;
 		optionsPane.add(cbRound, c2);
 
 		// Set the action buttons
 		actionsPane = new JPanel();
 		actionsPane.setLayout(new GridBagLayout());
 		c2 = new GridBagConstraints();
 
 		btnImportExcel = new JButton("Import from Excel");
 		btnImportExcel.addActionListener(this);
 		c2.gridx = 0;
 		c2.gridy = 0;
 		c2.weightx = 0.25;
 		actionsPane.add(btnImportExcel, c2);
 
 		btnSaveOptions = new JButton("Save Options");
 		btnSaveOptions.addActionListener(this);
 		c2.gridx = 0;
 		c2.gridy = 1;
 		c2.weightx = 0.25;
 		actionsPane.add(btnSaveOptions, c2);
 
 		btnPopulateFinalGrades = new JButton("Populate Final Grades");
 		btnPopulateFinalGrades.addActionListener(this);
 		c2.gridx = 0;
 		c2.gridy = 2;
 		c2.weightx = 0.25;
 		actionsPane.add(btnPopulateFinalGrades, c2);
 
 		// Continue with JTable ---------------------
 		c.weightx = 0.25;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridx = 0;
 		c.gridy = 0;
 		this.add(optionsPane, c);
 
 		// add the actions pane
 		c.weightx = 0.75;
 		c.gridx = 1;
 		c.gridy = 0;
 		this.add(actionsPane, c);
 
 		gradeTable = new JTable();
 		c.fill = GridBagConstraints.BOTH;
 		c.weightx = 0.0;
 		c.weighty = 7;
 		c.gridwidth = 3;
 		c.gridheight = 2;
 		c.gridx = 0;
 		c.gridy = 1;
 		gradeTable.setFillsViewportHeight(true);
 		gradeTable.setAutoCreateRowSorter(true); // allow sorting
 		JScrollPane scroller = new JScrollPane(gradeTable);
 		this.add(scroller, c);
 
 		addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent e) {
 				System.exit(0);
 			}
 		});
 	}
 
 	public void populateGradeTable() {
 		classResults = new StudentClassResults();
 		// set model for data retrieval
 		gradeTable.setModel(classResults.getRawData());
 		// get stats
 		getStats();
 	}
 
 	public static void main(String args[]) {
 		try {
 			// Set System L&F
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (Exception e) {
 			// Don't care about this exception
 		}
 		GradingTableGui gtGui = new GradingTableGui();
 		gtGui.addComponentsToPane();
 		gtGui.setSize(800, 600);
 		gtGui.populateGradeTable();
 		gtGui.setVisible(true);
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (e.getSource().equals(this.btnImportExcel)) {
 			System.out.println("Importing from excel ... ");
			classResults.importFromExcel("res/CS303.xls");
 		} else if (e.getSource().equals(this.btnSaveOptions)) {
 			System.out.println("Saving options ... ");
 			classResults.setOptions(txtCourseCode.getText(),
 					txtCourseName.getText(), new Double(txtAddCurve.getText()),
 					new Double(txtMulCurve.getText()), cbRound.isSelected());
 		} else if (e.getSource().equals(this.btnPopulateFinalGrades)) {
 			System.out.println("Populating final grades ... ");
 			classResults.populateFinalGrades();
 		}
 
 		// in any case, refresh the JTable
 		// this.gradeTable.fire
 		this.repaint();
 		this.updateStats();
 	}
 
 	public void getStats() {
 		// Set the stats pane
 		GridBagConstraints c = new GridBagConstraints();
 		c.fill = GridBagConstraints.HORIZONTAL;
 
 		GridBagConstraints c2 = new GridBagConstraints();
 		c2.fill = GridBagConstraints.HORIZONTAL;
 
 		statsPane = new JPanel();
 		statsPane.setLayout(new GridBagLayout());
 		c2 = new GridBagConstraints();
 
 		label = new JLabel("A+");
 		c2.gridx = 0;
 		c2.ipadx = 50;
 		c2.gridy = 0;
 		statsPane.add(label, c2);
 		lSAP = new JLabel("-");
 		c2.gridx = 1;
 		c2.gridy = 0;
 		statsPane.add(lSAP, c2);
 		label = new JLabel("A");
 		c2.gridx = 0;
 		c2.ipadx = 50;
 		c2.gridy = 1;
 		statsPane.add(label, c2);
 		lSA = new JLabel("-");
 		c2.gridx = 1;
 		c2.gridy = 1;
 		statsPane.add(lSA, c2);
 		label = new JLabel("A-");
 		c2.gridx = 0;
 		c2.ipadx = 50;
 		c2.gridy = 2;
 		statsPane.add(label, c2);
 		lSAM = new JLabel("-");
 		c2.gridx = 1;
 		c2.gridy = 2;
 		statsPane.add(lSAM, c2);
 		// --- Bs
 		label = new JLabel("B+");
 		c2.gridx = 4;
 		c2.ipadx = 50;
 		c2.gridy = 0;
 		statsPane.add(label, c2);
 		lSBP = new JLabel("-");
 		c2.gridx = 5;
 		c2.gridy = 0;
 		statsPane.add(lSBP, c2);
 		label = new JLabel("B");
 		c2.gridx = 4;
 		c2.ipadx = 50;
 		c2.gridy = 1;
 		statsPane.add(label, c2);
 		lSB = new JLabel("-");
 		c2.gridx = 5;
 		c2.gridy = 1;
 		statsPane.add(lSB, c2);
 		label = new JLabel("B-");
 		c2.gridx = 4;
 		c2.ipadx = 50;
 		c2.gridy = 2;
 		statsPane.add(label, c2);
 		lSBM = new JLabel("-");
 		c2.gridx = 5;
 		c2.gridy = 2;
 		statsPane.add(lSBM, c2);
 
 		// --- Cs
 		label = new JLabel("C+");
 		c2.gridx = 6;
 		c2.ipadx = 50;
 		c2.gridy = 0;
 		statsPane.add(label, c2);
 		lSCP = new JLabel("-");
 		c2.gridx = 7;
 		c2.gridy = 0;
 		statsPane.add(lSCP, c2);
 		label = new JLabel("C");
 		c2.gridx = 6;
 		c2.ipadx = 50;
 		c2.gridy = 1;
 		statsPane.add(label, c2);
 		lSC = new JLabel("-");
 		c2.gridx = 7;
 		c2.gridy = 1;
 		statsPane.add(lSC, c2);
 		label = new JLabel("C-");
 		c2.gridx = 6;
 		c2.ipadx = 50;
 		c2.gridy = 2;
 		statsPane.add(label, c2);
 		lSCM = new JLabel("-");
 		c2.gridx = 7;
 		c2.gridy = 2;
 		statsPane.add(lSCM, c2);
 
 		// --- Ds
 		label = new JLabel("D+");
 		c2.gridx = 8;
 		c2.ipadx = 50;
 		c2.gridy = 0;
 		statsPane.add(label, c2);
 		lSDP = new JLabel("-");
 		c2.gridx = 9;
 		c2.gridy = 0;
 		statsPane.add(lSDP, c2);
 		label = new JLabel("D");
 		c2.gridx = 8;
 		c2.ipadx = 50;
 		c2.gridy = 1;
 		statsPane.add(label, c2);
 		lSD = new JLabel("-");
 		c2.gridx = 9;
 		c2.gridy = 1;
 		statsPane.add(lSD, c2);
 		label = new JLabel("D-");
 		c2.gridx = 8;
 		c2.ipadx = 50;
 		c2.gridy = 2;
 		statsPane.add(label, c2);
 		lSDM = new JLabel("-");
 		c2.gridx = 9;
 		c2.gridy = 2;
 		statsPane.add(lSDM, c2);
 
 		// --- Fs
 		label = new JLabel("F");
 		c2.gridx = 10;
 		c2.ipadx = 50;
 		c2.gridy = 0;
 		statsPane.add(label, c2);
 		lSF = new JLabel("-");
 		c2.gridx = 11;
 		c2.gridy = 0;
 		statsPane.add(lSF, c2);
 
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.weightx = 0.0;
 		c.weighty = 0.25;
 		c.gridx = 0;
 		c.gridy = 3;
 		this.add(statsPane, c);
 	}
 
 	public void updateStats() {
 		lSAP.setText(Integer.toString(classResults.countGrade("A+")));
 		lSAP.setForeground(Color.BLUE);
 		lSA.setText(Integer.toString(classResults.countGrade("A")));
 		lSAM.setText(Integer.toString(classResults.countGrade("A-")));
 		lSBP.setText(Integer.toString(classResults.countGrade("B+")));
 		lSB.setText(Integer.toString(classResults.countGrade("B")));
 		lSBM.setText(Integer.toString(classResults.countGrade("B-")));
 		lSCP.setText(Integer.toString(classResults.countGrade("C+")));
 		lSC.setText(Integer.toString(classResults.countGrade("C")));
 		lSCM.setText(Integer.toString(classResults.countGrade("C-")));
 		lSDP.setText(Integer.toString(classResults.countGrade("D+")));
 		lSD.setText(Integer.toString(classResults.countGrade("D")));
 		lSDM.setText(Integer.toString(classResults.countGrade("D-")));
 		lSF.setText(Integer.toString(classResults.countGrade("F")));
 		lSF.setForeground(Color.RED);
 	}
 }
