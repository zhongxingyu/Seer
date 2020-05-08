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
 
 package rpiplanner;
 
 import java.awt.CardLayout;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.SwingConstants;
 
 import org.jdesktop.beansbinding.BeanProperty;
 import org.jdesktop.beansbinding.Bindings;
 import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
 
 import rpiplanner.model.Degree;
 import rpiplanner.model.PlanOfStudy;
 import rpiplanner.view.PlanOfStudyEditor;
 
 import com.jgoodies.forms.factories.FormFactory;
 import com.jgoodies.forms.layout.CellConstraints;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.RowSpec;
 
 public class MainFrame extends JFrame {
 
 	private JComboBox majorComboBox;
 	private JComboBox comboBox;
 	private JPanel introCard;
 	private PlanOfStudyEditor planCard;
 	private JTextField studentIDfield;
 	private JTextField schoolField;
 	private JTextField nameField;
 	private POSController controller;
 
 	public MainFrame() {
 		super();
 		getContentPane().setLayout(new CardLayout());
 		
 		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension windowSize = new Dimension(1200, 800);
 		if(windowSize.width > screenSize.width-200)
 			windowSize.width = screenSize.width-200;
 		setBounds((screenSize.width-windowSize.width)/2, (screenSize.height-windowSize.height)/2, 
 				windowSize.width, windowSize.height);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		introCard = new JPanel();
 		introCard.setLayout(new FormLayout(
 			new ColumnSpec[] {
 				FormFactory.DEFAULT_COLSPEC,
 				ColumnSpec.decode("pref:grow(1.0)"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("pref:grow(1.0)")},
 			new RowSpec[] {
 				RowSpec.decode("top:default"),
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				RowSpec.decode("default"),
 				RowSpec.decode("fill:default:grow(1.0)"),
 				RowSpec.decode("bottom:default")}));
 		introCard.setName("introCard");
 		getContentPane().add(introCard, introCard.getName());
 		
 		planCard = new PlanOfStudyEditor();
 		planCard.setName("plan");
 		getContentPane().add(planCard, planCard.getName());
 
 		final JButton button = new JButton();
 		button.addActionListener(new ActionListener() {
 			public void actionPerformed(final ActionEvent e) {
 				manualSave();
 				goToPlanEditor(true);
 			}
 		});
 		button.setText("Continue");
 		introCard.add(button, new CellConstraints("1, 9, 4, 1, fill, fill"));
 
 		final JLabel welcomeToRpiLabel = new JLabel();
 		welcomeToRpiLabel.setHorizontalAlignment(SwingConstants.CENTER);
 		welcomeToRpiLabel.setText("Welcome to RPI Planner!");
 		introCard.add(welcomeToRpiLabel, new CellConstraints("1, 1, 4, 1, fill, fill"));
 
 		final JLabel pleaseEnterYourLabel = new JLabel();
 		pleaseEnterYourLabel.setText("Please enter your information");
 		introCard.add(pleaseEnterYourLabel, new CellConstraints(1, 2, 4, 1));
 		
 		final JLabel nameLabel = new JLabel();
 		nameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
 		nameLabel.setText("Name:");
 		introCard.add(nameLabel, new CellConstraints(2, 3));
 
 		final JLabel label_1 = new JLabel();
 		label_1.setHorizontalAlignment(SwingConstants.RIGHT);
 		label_1.setText("School:");
 		introCard.add(label_1, new CellConstraints(2, 4));
 
 		final JLabel studentIdLabel = new JLabel();
 		studentIdLabel.setHorizontalAlignment(SwingConstants.RIGHT);
 		studentIdLabel.setText("Student ID:");
 		introCard.add(studentIdLabel, new CellConstraints(2, 5));
 
 		nameField = new JTextField();
 		introCard.add(nameField, new CellConstraints(4, 3));
 
 		schoolField = new JTextField();
 		schoolField.setText("Rensselaer Polytechnic Institute");
 		introCard.add(schoolField, new CellConstraints(4, 4));
 
 		studentIDfield = new JTextField();
 		introCard.add(studentIDfield, new CellConstraints(4, 5));
 
 		final JLabel startingYearLabel = new JLabel();
 		startingYearLabel.setHorizontalAlignment(SwingConstants.RIGHT);
 		startingYearLabel.setText("Starting Year:");
 		introCard.add(startingYearLabel, new CellConstraints(2, 6));
 
 		Integer[] years = {2006, 2007, 2008, 2009, 2010, 2011};
 		comboBox = new JComboBox(years);
 		introCard.add(comboBox, new CellConstraints(4, 6));
 
 		final JLabel majorLabel = new JLabel();
 		majorLabel.setHorizontalAlignment(SwingConstants.RIGHT);
 		majorLabel.setText("Major:");
 		introCard.add(majorLabel, new CellConstraints(2, 7));
 
 		majorComboBox = new JComboBox();
 		introCard.add(majorComboBox, new CellConstraints(4, 7));
 		
 		setTitle("RPI Planner");
 		//
 	}
 	public PlanOfStudyEditor getPlanCard() {
 		return planCard;
 	}
 	public JPanel getIntroPanel() {
 		return introCard;
 	}
 
 	public void setController(POSController controller) {
 		this.controller = controller;
 		setupBindings();
 		manualInit();
 	}
 
 	private void manualInit() {
 		majorComboBox.setModel(controller.getDegreeListModel());
 		if(controller.getPlan().getDegrees() == null || controller.getPlan().getDegrees().size() == 0)
 			;
 		else{
 			Degree major = controller.getPlan().getDegrees().get(0);
 			majorComboBox.setSelectedItem(major);
 		}
 	}
 	
 	private void manualSave() {
 		controller.initializeTerms((Integer)comboBox.getSelectedItem());
 		ArrayList<Degree> degrees = controller.getPlan().getDegrees();
 		if(degrees.contains(majorComboBox.getSelectedItem()) || majorComboBox.getSelectedItem() == null)
 			;
 		else
 			controller.addDegree((Degree)majorComboBox.getSelectedItem());
 	}
 	
 	private void setupBindings() {
 		PlanOfStudy plan = controller.getPlan();
 		BeanProperty<JTextField, String> text = BeanProperty.create("text");
 		BeanProperty<JComboBox, String> selectedItem = BeanProperty.create("selectedItem");
 
 		Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, plan, BeanProperty.create("fullname"), nameField, text).bind();
 		Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, plan, BeanProperty.create("school"), schoolField, text).bind();
 		Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, plan, BeanProperty.create("studentID"), studentIDfield, text).bind();
 		Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, plan, BeanProperty.create("startingYear"), comboBox, selectedItem).bind();
 	}
 	public void goToDetailsPanel() {
 		CardLayout l = (CardLayout)getContentPane().getLayout();
 		l.first(getContentPane());
 	}
 	public void goToPlanEditor(boolean newPlan) {
 		if(newPlan)
 			controller.loadTemplate();
 		controller.validatePlan();
 		CardLayout l = (CardLayout)getContentPane().getLayout();
 		l.last(getContentPane());
 	}
 }
