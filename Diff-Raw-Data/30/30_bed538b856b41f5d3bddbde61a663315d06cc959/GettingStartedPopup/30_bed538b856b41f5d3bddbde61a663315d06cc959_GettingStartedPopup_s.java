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
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 
 import org.jdesktop.beansbinding.BeanProperty;
 import org.jdesktop.beansbinding.Bindings;
 import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
 
 import com.jgoodies.forms.factories.FormFactory;
 import com.jgoodies.forms.layout.CellConstraints;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.RowSpec;
 
 import rpiplanner.POSController;
 import rpiplanner.model.Degree;
 import rpiplanner.model.PlanOfStudy;
 
 public class GettingStartedPopup extends JDialog {
 	private JComboBox majorComboBox;
 	private JComboBox startingYearComboBox;
     private JTextArea textArea;
 	private POSController controller;
     private JPanel contentPane;
     private JButton nextButton;
 
     // for Swing Designer
 	public GettingStartedPopup() {
         setContentPane(contentPane);
 		setTitle("Getting Started");
 
         nextButton.addActionListener(new ActionListener() {
             public void actionPerformed(final ActionEvent e) {
                 save();
                 setVisible(false);
             }
         });
     }
 
 	public GettingStartedPopup(POSController controller){
 		this();
 		this.controller = controller;
 		majorComboBox.setModel(controller.getDegreeListModel());
 		if (controller.getPlan().getDegrees() != null
 				&& controller.getPlan().getDegrees().size() > 0) {
 			Degree major = controller.getPlan().getDegrees().get(0);
 			majorComboBox.setSelectedItem(major);
 		}
 		PlanOfStudy plan = controller.getPlan();
 		BeanProperty<JComboBox, String> selectedItem = BeanProperty.create("selectedItem");
 
 		Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, plan,
 				BeanProperty.create("startingYear"), startingYearComboBox,
 				selectedItem).bind();
 	}
 
 	private void save(){
 		controller.initializeTerms(Integer.valueOf((String)startingYearComboBox.getSelectedItem()));
 		ArrayList<Degree> degrees = controller.getPlan().getDegrees();
 		if(degrees.contains(majorComboBox.getSelectedItem()) || majorComboBox.getSelectedItem() == null)
 			;
		else
 			controller.addDegree((Degree)majorComboBox.getSelectedItem());
 		controller.validatePlan();
 	}
 }
