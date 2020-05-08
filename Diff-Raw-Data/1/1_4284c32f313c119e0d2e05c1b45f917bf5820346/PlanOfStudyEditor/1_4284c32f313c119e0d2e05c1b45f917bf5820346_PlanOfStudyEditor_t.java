 /* RPI Planner - Customized plans of study for RPI students.
  *
  * Copyright (C) 2008 Eric Allen allene2@rpi.edu
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
 package rpiplanner.view;
 
 import java.awt.CardLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionAdapter;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.border.CompoundBorder;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.EtchedBorder;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.text.BadLocationException;
 
 import rpiplanner.POSController;
 import rpiplanner.SchoolInformation;
 import rpiplanner.model.Course;
 import rpiplanner.model.Degree;
 
 import com.jgoodies.forms.factories.FormFactory;
 import com.jgoodies.forms.layout.CellConstraints;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.RowSpec;
 
 public class PlanOfStudyEditor extends JPanel {
 	private JPanel planPanel;
 	private JTextField creditTotalField;
 	private JPanel detailsPanel;
 	private JList problemsList;
 	private JTextField nameField;
 	private JButton removeDegreeButton;
 	private JButton addDegreeButton;
 	private JList degreeList;
 	private JButton addCourseButton;
 	private JPanel courseDetailsPanel;
 	private JTextArea descriptionTextArea;
 	private JList courseList;
 	private JTextField searchField;
 	private ArrayList<JPanel> semesterPanels = new ArrayList<JPanel>(8);
 	
 	public PlanOfStudyEditor(POSController controller){
 		super();
 	}
 
 	public PlanOfStudyEditor() {
 		super();
 		setLayout(new FormLayout(
 			new ColumnSpec[] {
 				ColumnSpec.decode("200px:grow(1.0)"),
 				ColumnSpec.decode("300px:grow(3.5)"),
 				ColumnSpec.decode("right:80px:grow(1.0)"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("80px:grow(1.0)"),
 				FormFactory.RELATED_GAP_COLSPEC},
 			new RowSpec[] {
 				FormFactory.MIN_ROWSPEC,
 				RowSpec.decode("fill:0dlu:grow(1.0)"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				RowSpec.decode("fill:70dlu")}));
 
 		final JPanel titlePanel = new JPanel();
 		add(titlePanel, new CellConstraints("1, 1, 6, 1, fill, fill"));
 
 		final JLabel rpiPlannerLabel = new JLabel();
 		rpiPlannerLabel.setFont(new Font("Lucida Grande", Font.BOLD, 18));
 		rpiPlannerLabel.setText("RPI Planner");
 		titlePanel.add(rpiPlannerLabel);
 
 		planPanel = new JPanel();
 		planPanel.setLayout(new FormLayout("pref:grow(1.0), pref:grow(1.0)",
 				"top:min, top:min, top:min, top:min, top:min"));
 		add(planPanel, new CellConstraints("2, 2, 1, 1, fill, top"));
 		planPanel.setName("semesters");
 		
 		final JPanel apcreditPanel = new JPanel();
 		apcreditPanel.setBorder(new CompoundBorder(new EmptyBorder(5,5,5,5), new EtchedBorder()));
 		apcreditPanel.setLayout(new BoxLayout(apcreditPanel, BoxLayout.Y_AXIS));
 		apcreditPanel.setName("apcredit");
 		planPanel.add(apcreditPanel, new CellConstraints(1, 1));
 		semesterPanels.add(apcreditPanel);
 		
 		for(int i = 0; i < SchoolInformation.getDefaultSemesterCount(); i++){
 			final JPanel semesterPanel = new JPanel();
 			semesterPanel.setBorder(new CompoundBorder(new EmptyBorder(5,5,5,5), new EtchedBorder()));
 			semesterPanel.setLayout(new BoxLayout(semesterPanel, BoxLayout.Y_AXIS));
 			semesterPanel.setName("semesterPanel"+String.valueOf(i));
 			planPanel.add(semesterPanel, new CellConstraints(i%2+1, i/2+2));
 			semesterPanels.add(semesterPanel);
 		}
 
 		final JPanel searchPanel = new JPanel();
 		searchPanel.setLayout(new FormLayout(
 			new ColumnSpec[] {
 				ColumnSpec.decode("default:grow(1.0)")},
 			new RowSpec[] {
 				FormFactory.MIN_ROWSPEC,
 				RowSpec.decode("fill:0px:grow(1.0)"),
 				FormFactory.DEFAULT_ROWSPEC}));
 		searchPanel.setBorder(new TitledBorder(null, "Find Course", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
 		add(searchPanel, new CellConstraints("1, 2, 1, 4, fill, fill"));
 
 		searchField = new JTextField();
 		searchField.setPreferredSize(new Dimension(150, 28));
 		searchPanel.add(searchField, new CellConstraints("1, 1, 1, 1, fill, fill"));
 
 		final JScrollPane scrollPane = new JScrollPane();
 		searchPanel.add(scrollPane, new CellConstraints("1, 2, 1, 1, fill, fill"));
 
 		courseList = new JList();
 		scrollPane.setViewportView(courseList);
 		courseList.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
 		courseList.setBackground(Color.WHITE);
 
 		addCourseButton = new JButton();
 		addCourseButton.setText("Create New Course");
 		searchPanel.add(addCourseButton, new CellConstraints(1, 3));
 
 		detailsPanel = new JPanel();
 		detailsPanel.setLayout(new CardLayout());
 		add(detailsPanel, new CellConstraints(3, 2, 3, 1));
 
 		final JPanel degreeDetailsPanel = new JPanel();
 		degreeDetailsPanel.setBorder(new TitledBorder(null, "Degree details", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
 		degreeDetailsPanel.setLayout(new FormLayout(
 			new ColumnSpec[] {
 				ColumnSpec.decode("default:grow(1.0)"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("default:grow(1.0)")},
 			new RowSpec[] {
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("fill:default:grow(1.0)")}));
 		degreeDetailsPanel.setName("degreeDetails");
 		detailsPanel.add(degreeDetailsPanel, degreeDetailsPanel.getName());
 
 		final JLabel nameLabel = new JLabel();
 		nameLabel.setText("Name:");
 		degreeDetailsPanel.add(nameLabel, new CellConstraints());
 
 		final JLabel problemsLabel = new JLabel();
 		problemsLabel.setText("Problems:");
 		degreeDetailsPanel.add(problemsLabel, new CellConstraints(1, 3));
 
 		nameField = new JTextField();
 		nameField.setName("name");
 		degreeDetailsPanel.add(nameField, new CellConstraints(3, 1));
 
 		final JScrollPane scrollPane_2 = new JScrollPane();
 		degreeDetailsPanel.add(scrollPane_2, new CellConstraints(1, 5, 3, 1));
 
 		problemsList = new JList();
 		scrollPane_2.setViewportView(problemsList);
 
 		courseDetailsPanel = new JPanel();
 		courseDetailsPanel.setName("courseDetailsPanel");
 		detailsPanel.add(courseDetailsPanel, courseDetailsPanel.getName());
 		courseDetailsPanel.setLayout(new FormLayout(
 			new ColumnSpec[] {
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("default:grow(1.0)")},
 			new RowSpec[] {
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("fill:default:grow(1.0)")}));
 		courseDetailsPanel.setBorder(new TitledBorder(new EtchedBorder(), "Course details"));
 
 		final JLabel titleLabel = new JLabel();
 		titleLabel.setText("Title:");
 		courseDetailsPanel.add(titleLabel, new CellConstraints(1, 3));
 
 		final JLabel departmentLabel = new JLabel();
 		departmentLabel.setText("Department:");
 		courseDetailsPanel.add(departmentLabel, new CellConstraints());
 
 		final JLabel descriptionLabel = new JLabel();
 		descriptionLabel.setText("Description:");
 		courseDetailsPanel.add(descriptionLabel, new CellConstraints(1, 7));
 
 		descriptionTextArea = new JTextArea();
 		descriptionTextArea.setLineWrap(true);
 		descriptionTextArea.setWrapStyleWord(true);
 		descriptionTextArea.setName("description");
 		descriptionTextArea.setEditable(false);
 		courseDetailsPanel.add(descriptionTextArea, new CellConstraints(1, 9, 3, 1));
 
 		final JTextField departmentField = new JTextField();
 		departmentField.setEditable(false);
 		departmentField.setName("department");
 		courseDetailsPanel.add(departmentField, new CellConstraints(3, 1));
 
 		final JTextField titleField = new JTextField();
 		titleField.setEditable(false);
 		titleField.setName("title");
 		courseDetailsPanel.add(titleField, new CellConstraints(3, 3));
 
 		final JTextField catalogField = new JTextField();
 		catalogField.setEditable(false);
 		catalogField.setName("catalogNumber");
 		courseDetailsPanel.add(catalogField, new CellConstraints(3, 5));
 
 		final JLabel catalogNumberLabel = new JLabel();
 		catalogNumberLabel.setText("Catalog number:");
 		courseDetailsPanel.add(catalogNumberLabel, new CellConstraints(1, 5));
 
 		final JPanel degreesPanel = new JPanel();
 		degreesPanel.setLayout(new FormLayout(
 			new ColumnSpec[] {
 				ColumnSpec.decode("default:grow(1.0)"),
 				ColumnSpec.decode("default:grow(1.0)")},
 			new RowSpec[] {
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC}));
 		degreesPanel.setBorder(new TitledBorder(null, "Degrees", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
 		add(degreesPanel, new CellConstraints(2, 4, 1, 2));
 
 		final JScrollPane scrollPane_1 = new JScrollPane();
 		degreesPanel.add(scrollPane_1, new CellConstraints(1, 1, 2, 1));
 
 		degreeList = new JList();
 		degreeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		degreeList.setCellRenderer(new DegreeListCell());
 		scrollPane_1.setViewportView(degreeList);
 
 		addDegreeButton = new JButton();
 		degreesPanel.add(addDegreeButton, new CellConstraints(1, 2));
 		addDegreeButton.setText("Add");
 
 		removeDegreeButton = new JButton();
 		removeDegreeButton.setText("Remove");
 		degreesPanel.add(removeDegreeButton, new CellConstraints(2, 2));
 
 		final JLabel creditTotalLabel = new JLabel();
 		creditTotalLabel.setText("Credit total:");
 		add(creditTotalLabel, new CellConstraints(3, 4));
 
 		creditTotalField = new JTextField();
 		add(creditTotalField, new CellConstraints(5, 4));
 		//
 	}
 	
 	public void setController(final POSController controller){
 		controller.setSemesterPanels(semesterPanels);
 		controller.setDetailsPanel(detailsPanel);
 		controller.setDegreeList(degreeList);
 		degreeList.setModel(controller.getDegreeListModel());
 		controller.addPropertyChangeListener("creditTotal", new PropertyChangeListener(){
 			public void propertyChange(PropertyChangeEvent evt) {
 				creditTotalField.setText(String.valueOf(evt.getNewValue()));
 			}
 		});
 		
 		addCourseButton.addActionListener(new ActionListener() {
 			public void actionPerformed(final ActionEvent e) {
 				CourseEditDialog ncd = new CourseEditDialog();
 				ncd.setController(controller);
 				ncd.setVisible(true);
 			}
 		});
 
 		addDegreeButton.addActionListener(new ActionListener() {
 			public void actionPerformed(final ActionEvent e) {
 				DegreeSelector ds = new DegreeSelector(controller);
 				ds.setVisible(true);
 			}
 		});
 		removeDegreeButton.addActionListener(new ActionListener(){
 			public void actionPerformed(final ActionEvent e) {
 				controller.removeDegree((Degree)degreeList.getSelectedValue());
 			}
 		});
 		degreeList.setModel(controller.getPlanDegreeListModel());
 		
 		courseList.setModel(controller.getCourseListModel());
 		courseList.setTransferHandler(new CourseTransferHandler(controller));
 		courseList.setDragEnabled(true);
 		courseList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 
 		courseList.addMouseMotionListener(new MouseMotionAdapter() {
 			public void mouseMoved(final MouseEvent e) {
 				int index = courseList.locationToIndex(e.getPoint());
 				if(index != -1){
 					Course mouseOver = (Course)courseList.getModel().getElementAt(index);
 					controller.setDetailDisplay(mouseOver);
 				}
 				else{
 					controller.setDetailDisplay((Course)null);
 				}
 			}
 		});
 		courseList.addMouseListener(new MouseAdapter(){
 			@Override
 			public void mouseExited(MouseEvent e) {
 				controller.setDetailDisplay((Course)null);
 			}
 			
 			@Override
 			public void mousePressed(MouseEvent e) {
 				if(e.getButton() == MouseEvent.BUTTON3){
 					// right click
 					int idx = courseList.locationToIndex(e.getPoint()); 
 					courseList.setSelectedIndex(idx); 
 					JPopupMenu contextMenu = new JPopupMenu();
 					contextMenu.add("Edit").addActionListener(new ActionListener(){
 						public void actionPerformed(ActionEvent e) {
 							CourseEditDialog d = new CourseEditDialog((Course)courseList.getSelectedValue());
 							d.setController(controller);
 							d.setVisible(true);
 						}
 					});
 					contextMenu.show(courseList, e.getX(), e.getY());
 				}
 			}
 		});
 		
 		degreeList.addMouseMotionListener(new MouseMotionAdapter() {
 			public void mouseMoved(final MouseEvent e) {
 				int index = degreeList.locationToIndex(e.getPoint());
 				if(index != -1){
 					Degree mouseOver = (Degree)degreeList.getModel().getElementAt(index);
 					controller.setDetailDisplay(mouseOver);
 				}
 				else{
 					controller.setDetailDisplay((Course)null);
 				}
 			}
 		});
 		degreeList.addMouseListener(new MouseAdapter(){
 			@Override
 			public void mouseExited(MouseEvent e) {
 				controller.setDetailDisplay((Course)null);
 			}
 		});
 		
 		searchField.getDocument().addDocumentListener(new DocumentListener(){
 			public void changedUpdate(DocumentEvent e) {
 				try {
 					controller.searchTextChanged(e.getDocument().getText(0, e.getDocument().getLength()));
 				} catch (BadLocationException e1) {}
 			}
 			public void insertUpdate(DocumentEvent e) {
 				try {
 					controller.searchTextChanged(e.getDocument().getText(0, e.getDocument().getLength()));
 				} catch (BadLocationException e1) {}
 			}
 			public void removeUpdate(DocumentEvent e) {
 				try {
 					controller.searchTextChanged(e.getDocument().getText(0, e.getDocument().getLength()));
 				} catch (BadLocationException e1) {}
 			}
 		});
 	}
 	
 	public JPanel getPlanPanel(){
 		return planPanel;
 	}
 }
