 import javax.swing.*;
 import javax.swing.border.TitledBorder;
 
 import com.sun.tools.javac.util.List;
 
 import model.*;
 
 import java.awt.FlowLayout;
 import java.awt.event.*;
 import java.util.ArrayList;
 /*
  * Class for JPanels and JOptionPanes of some button Functionalities.
  * e.g. Add Method, Add Field
  */
 public class JOFrame extends JPanel {
 	
 	private Design design;
 	private GraphicalPanel graphicalPanel;
 	
 	JOFrame(Design design, GraphicalPanel graphicalPanel)
 	{
 		this.graphicalPanel = graphicalPanel;
 		this.design = design;
 	}
 	
 	public void joPanel(String title) {
 		
 		String[] aModifiers = {"Public", "Protected", "Private"};
 		JPanel panel = new JPanel(); //creates panel
 		JTextField typeField = new JTextField(5); //type text field
 		JTextField nameField = new JTextField(5); //name text field
 		JComboBox list = new JComboBox(aModifiers); //drop-down menu for access Modifier
 		
 		panel.add(new JLabel("Access Modifier: "));
 		panel.add(list);
 		
 		panel.add(new JLabel("Type: "));
 		panel.add(typeField);
 		
 		panel.add(new JLabel("Name: "));
 		panel.add(nameField);
 				
 		//!!!!!!!!!!although result is int, textfield values are returned as String!!!!!!!!!!!!
 		int result = JOptionPane.showConfirmDialog(null, panel, "Add " + title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
 		
 		if(result == JOptionPane.OK_OPTION) {
 			/* Output of the Combobox and textFields.
 			 * You can obtain their values with .getText();
 			 * TextField values are String,
 			 * list values are int...I think.
 			 */
 			if(graphicalPanel.getMouseSelectedClass().getField(nameField.getText())==null){
 				if(title=="Method"){
 					Method addMethod = new Method(nameField.getText());
 					addMethod.setAccessModifier(list.getSelectedIndex());
 					addMethod.setType(typeField.getText());
 					graphicalPanel.getMouseSelectedClass().addField(addMethod);
 				}else{
 					Field addField = new Field(nameField.getText());
 					addField.setAccessModifier(list.getSelectedIndex());
 					addField.setType(typeField.getText());
 					graphicalPanel.getMouseSelectedClass().addField(addField);
 				}
 
 				graphicalPanel.repaint();
 			}
 			else{
 				JOptionPane.showMessageDialog(this, "You already have a " + title + " named " + nameField.getText()  , "Error", JOptionPane.ERROR_MESSAGE);
 			}
 				
 		}
 		
 	}
 	
 	public void joFieldtoMethod(String title) {
		ArrayList<String> methodList = new ArrayList<String>();
 		for(Field theField: graphicalPanel.getMouseSelectedClass().getFields())
 		{
 			if(theField instanceof Method)
 			{
 				methodList.add(theField.getLabel());
 			}
 		}
 		if(!methodList.isEmpty())
 		{
 			JPanel panel = new JPanel(); //creates panel
 			JComboBox methodlist = new JComboBox(methodList.toArray()); //drop-down menu of methods
 			JTextField typeField = new JTextField(5); //type text field
 			JTextField nameField = new JTextField(5); //name text field
 			
 	
 			panel.add(new JLabel("Method "));
 			panel.add(methodlist);
 			
 			
 			panel.add(new JLabel("Type: "));
 			panel.add(typeField);
 			
 			panel.add(new JLabel("Name: "));
 			panel.add(nameField);
 			
 			//!!!!!!!!!!although result is int, textfield values are returned as String!!!!!!!!!!!!
 			int result = JOptionPane.showConfirmDialog(null, panel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
 			
 			if(result == JOptionPane.OK_OPTION) {
 				/* Output of the Combobox and textFields.
 				 * You can obtain their values with .getText();
 				 * TextField values are String,
 				 * list values are int...I think.
 				 */
 				Method selectedMethod = (Method) graphicalPanel.getMouseSelectedClass().getField(methodlist.getSelectedItem().toString());
 				if(selectedMethod.getParameter(nameField.getText())==null){
 					Field fieldToAdd = new Field(nameField.getText());
 					fieldToAdd.setType(typeField.getText());
 					selectedMethod.addParameter(fieldToAdd);
 					graphicalPanel.repaint();
 				}else{
 					JOptionPane.showMessageDialog(this, "You already have a parameter named " + nameField.getText() + " for " 
 							+ methodlist.getSelectedItem().toString(), "Error", JOptionPane.ERROR_MESSAGE);
 				}
 			}
 		}else{
 			JOptionPane.showMessageDialog(this, "You don't have any methods in this class.", "Error", JOptionPane.ERROR_MESSAGE);
 		}
 	}
 	
 	public void joCardinality(String title) {
 		
 		JPanel panel = new JPanel();
 		/*
 		 * 
 		 * panel contents of the JOptionPane.
 		 * 
 		 */
 		//!!!!!!!!!!although result is int, textfield values are returned as String!!!!!!!!!!!!
 		int result = JOptionPane.showConfirmDialog(null, panel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
 		if(result == JOptionPane.OK_OPTION) {
 			/*
 			 * 
 			 * use .getText(); method to retrieve values from panel contents.
 			 * e.g. textfield.getText();
 			 * 
 			 */
 		}
 	}
 	
 	public void joList(String title, String[] fieldList) {
 		
 		JComboBox list = new JComboBox(fieldList); //drop-down menu of fields
 		
 		//!!!!!!!!!!although result is int, textfield values are returned as String!!!!!!!!!!!!
 		int result = JOptionPane.showConfirmDialog(null, list, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
 		if(result == JOptionPane.OK_OPTION) {
 			/*
 			 * 
 			 * use .getText(); method to retrieve values from panel contents.
 			 * e.g. textfield.getText();
 			 * 
 			 */
 		
 		}
 	
 	}
 	
 }
