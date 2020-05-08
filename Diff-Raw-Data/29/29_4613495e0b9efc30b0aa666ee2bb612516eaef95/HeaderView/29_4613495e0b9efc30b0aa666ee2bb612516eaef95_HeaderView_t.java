 package view;
 
 import java.awt.event.*;
 
 import javax.swing.*;
 
import model.TaskModel;

 import controller.ListController;
 
 public class HeaderView extends JPanel{
 
 	//TODO Create button "Ny" and textfield to get taskname
 	private ListController controller;
	private JTextField taskTitleInput;
 
 	
 	public HeaderView(ListController controller) {
 		this.controller = controller;
		taskTitleInput = new JTextField("New task", 50);
 		
 		
 	}
 	
 	private class NewTaskListener implements ActionListener{
 		public void actionPerformed(ActionEvent e){
			if(e.getSource() instanceof JButton){
				if(taskTitleInput.getText() != null){
					TaskModel task = new TaskModel(taskTitleInput.getText());
					//implementation to display a task in ListView?
					repaint();
				}
			}
 		}
 		
 	}
 	
 	//TODO Create action event for button "Ny". 
 		//TO DO Should create a new instance of the TaskModel-class (with the string from the textfield).
 		//TODO Create a new instance from the TaskPanel-class with the new model as parameter.
 		//TODO Use the controller with the method addTask(TaskPanel), to add the new task to the listView.
 
 }
