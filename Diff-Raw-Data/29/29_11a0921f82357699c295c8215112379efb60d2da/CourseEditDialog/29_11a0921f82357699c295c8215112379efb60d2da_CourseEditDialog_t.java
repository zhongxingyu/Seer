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
 
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 
 import org.jdesktop.beansbinding.BeanProperty;
 import org.jdesktop.beansbinding.Bindings;
 import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
 import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
 
 import rpiplanner.POSController;
 import rpiplanner.model.Course;
 import rpiplanner.model.CourseDatabase;
 import rpiplanner.model.EditableCourse;
 import rpiplanner.model.YearPart;
 
 import com.jgoodies.forms.factories.FormFactory;
 import com.jgoodies.forms.layout.CellConstraints;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.RowSpec;
 
 public class CourseEditDialog extends JDialog {
 	private JTextField corequisitesField;
 	private JTextField prerequisitesField;
 	private JPanel offeredDuringPanel;
 	private JComboBox creditsComboBox;
 	private JTextArea descriptionTextArea;
 	private JTextField catalogField;
 	private JTextField titleField;
 	private JTextField departmentField;
 	private EditableCourse toEdit;
 	private CourseDatabase db;
 	private boolean newCourse = true;
 	/**
 	 * Create the dialog
 	 */
 	public CourseEditDialog() {
 		super();
 		this.toEdit = new EditableCourse();
 		toEdit.setCredits(4);
 		initialize();
 		bind();
 	}
 
 	public CourseEditDialog(Course toEdit){
 		super();
 		newCourse = false;
 		this.toEdit = new EditableCourse(toEdit);
 		initialize();
 		bind();
 	}
 
 	protected void initialize() {
 		getContentPane().setLayout(new FormLayout(
 			new ColumnSpec[] {
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("right:default:grow(1.0)"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("default:grow(1.0)"),
 				FormFactory.RELATED_GAP_COLSPEC},
 			new RowSpec[] {
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("fill:default:grow(1.0)"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("fill:default"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("fill:default"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC}));
 		setTitle("New Course");
 		setBounds(100, 100, 447, 600);
 
 		final JLabel courseTitleLabel = new JLabel();
 		courseTitleLabel.setText("Department");
 		getContentPane().add(courseTitleLabel, new CellConstraints(2, 2));
 
 		final JLabel titleLabel = new JLabel();
 		titleLabel.setText("Title");
 		getContentPane().add(titleLabel, new CellConstraints(2, 4));
 
 		final JLabel catalogNumberLabel = new JLabel();
 		catalogNumberLabel.setText("Catalog number");
 		getContentPane().add(catalogNumberLabel, new CellConstraints(2, 6));
 
 		final JLabel descriptionLabel = new JLabel();
 		descriptionLabel.setText("Description");
 		getContentPane().add(descriptionLabel, new CellConstraints(2, 12));
 
 		final JButton saveButton = new JButton();
 		saveButton.addActionListener(new ActionListener() {
 			public void actionPerformed(final ActionEvent e) {
 				if(newCourse)
 					createCourse();
 				else
 					updateCourse();
 				setVisible(false);
 			}
 		});
 		saveButton.setText("Save");
 		getContentPane().add(saveButton, new CellConstraints(2, 18));
 
 		final JButton cancelButton = new JButton();
 		cancelButton.addActionListener(new ActionListener() {
 			public void actionPerformed(final ActionEvent e) {
 				setVisible(false);
 			}
 		});
 		cancelButton.setText("Cancel");
 		getContentPane().add(cancelButton, new CellConstraints(4, 18));
 
 		departmentField = new JTextField();
 		getContentPane().add(departmentField, new CellConstraints(4, 2));
 
 		titleField = new JTextField();
 		getContentPane().add(titleField, new CellConstraints(4, 4));
 
 		catalogField = new JTextField();
 		getContentPane().add(catalogField, new CellConstraints(4, 6));
 
 		final JScrollPane scrollPane = new JScrollPane();
 		getContentPane().add(scrollPane, new CellConstraints(4, 12));
 
 		descriptionTextArea = new JTextArea();
 		scrollPane.setViewportView(descriptionTextArea);
 		descriptionTextArea.setLineWrap(true);
 		descriptionTextArea.setWrapStyleWord(true);
 		
 		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 
 		final JLabel creditsLabel = new JLabel();
 		creditsLabel.setText("Credits");
 		getContentPane().add(creditsLabel, new CellConstraints(2, 8));
 
 		final JLabel offeredDuringLabel = new JLabel();
 		offeredDuringLabel.setText("Offered during");
 		getContentPane().add(offeredDuringLabel, new CellConstraints(2, 10));
 
 		creditsComboBox = new JComboBox(new Integer[] {1, 2, 3, 4});
 		creditsComboBox.setSelectedIndex(3);
 		getContentPane().add(creditsComboBox, new CellConstraints(4, 8));
 
 		offeredDuringPanel = new JPanel();
 		for(YearPart p : YearPart.values()){
 			final JCheckBox termBox = new JCheckBox(p.toString());
 			termBox.setSelected(true);
 			offeredDuringPanel.add(termBox);
 		}
 		
 		getContentPane().add(offeredDuringPanel, new CellConstraints(4, 10));
 
 		final JLabel prerequisitesLabel = new JLabel();
 		prerequisitesLabel.setText("Prerequisites");
 		getContentPane().add(prerequisitesLabel, new CellConstraints(2, 14));
 
 		final JLabel corequisitesLabel = new JLabel();
 		corequisitesLabel.setText("Corequisites");
 		getContentPane().add(corequisitesLabel, new CellConstraints(2, 16));
 
 		prerequisitesField = new JTextField();
 		getContentPane().add(prerequisitesField, new CellConstraints(4, 14));
 
 		corequisitesField = new JTextField();
 		getContentPane().add(corequisitesField, new CellConstraints(4, 16));
 		//
 	}
 	
 	protected void bind(){
 		BeanProperty<JTextField, String> text = BeanProperty.create("text");
 		BeanProperty<JTextArea, String> textArea = BeanProperty.create("text");
 		BeanProperty<JComboBox, String> selectedItem = BeanProperty.create("selectedItem");
 		Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, toEdit, BeanProperty.create("department"), departmentField, text).bind();
 		Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, toEdit, BeanProperty.create("title"), titleField, text).bind();
 		Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, toEdit, BeanProperty.create("catalogNumber"), catalogField, text).bind();
 		Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, toEdit, BeanProperty.create("description"), descriptionTextArea, textArea).bind();
 		Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, toEdit, BeanProperty.create("credits"), creditsComboBox, selectedItem).bind();
 
 		// manual binding
 		StringBuilder builder = new StringBuilder();
 		for(Course c : toEdit.getPrerequisites())
 			builder.append(c.getCatalogNumber());
 		prerequisitesField.setText(builder.toString());
 		builder = new StringBuilder();
 		for(Course c : toEdit.getCorequisites())
 			builder.append(c.getCatalogNumber());
 		corequisitesField.setText(builder.toString());
 
 		for(Component c : offeredDuringPanel.getComponents()){
 			JCheckBox box = (JCheckBox)c;
 			box.setSelected(false);
			if(toEdit.getAvailableTerms() != null){
 				for(YearPart t : toEdit.getAvailableTerms()){
 					if(box.getText().equals(t.toString()))
 						box.setSelected(true);
 				}
            }
            else{
                box.setSelected(true);
            }
 		}
 }
 	
 	protected void createCourse() {
 		updateCourse();
 		db.addOrUpdate(toEdit);
 	}
 	
 	protected void updateCourse() {
 		String[] prereqNames = prerequisitesField.getText().split(" ?, ?");
 		String[] coreqNames = corequisitesField.getText().split(" ?, ?");
 		ArrayList<Course> prerequisites = new ArrayList<Course>(prereqNames.length);
 		for(String name : prereqNames){
 			Course toAdd = Course.get(name);
 			if(toAdd != null)
 				prerequisites.add(toAdd);
 		}
 		toEdit.setPrerequisites(prerequisites);
 		ArrayList<Course> corequisites = new ArrayList<Course>(coreqNames.length);
 		for(String name : coreqNames){
 			Course toAdd = Course.get(name);
 			if(toAdd != null)
 				corequisites.add(toAdd);
 		}
 		toEdit.setCorequisites(corequisites);
 		
 		ArrayList<YearPart> offeredDuring = new ArrayList<YearPart>();
 		for(Component c : offeredDuringPanel.getComponents()){
 			JCheckBox box = (JCheckBox)c;
 			offeredDuring.add(YearPart.valueOf(box.getText().toUpperCase()));
 		}
 		toEdit.setAvailableTerms(offeredDuring.toArray(new YearPart[0]));
 		
 		db.addOrUpdate(toEdit);
 	}
 	
 	public void setController(POSController controller){
 		AutoCompleteDecorator.decorate(departmentField, controller.getCourseDatabase().getDepartments(), false);
 		this.db = controller.getCourseDatabase();
 	}
 }
