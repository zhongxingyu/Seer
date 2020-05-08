 package view;
 
 import javax.swing.JPanel;
 import model.*;
 
 import model.TaskModel;
 import net.miginfocom.swing.MigLayout;
 import javax.swing.JLabel;
 import java.awt.Font;
 import javax.swing.JCheckBox;
 import java.awt.Dimension;
 import javax.swing.border.LineBorder;
 import java.awt.Color;
 import java.awt.GridLayout;
 import java.awt.FlowLayout;
 import java.awt.Component;
 import javax.swing.JButton;
 import javax.swing.Box;
 
 import controller.TaskController;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
 
 /**
  * A class to represent a TaskPanel. 
  * 
  * @author Hanna
  */
 
 public class TaskPanel extends JPanel {
 	
 	private TaskModel model;
 	private String taskCheck;
 	private String deleteTask;
 	private JButton deleteTaskButton;
 	/**
 	 * Create the panel.
 	 */
 	public TaskPanel(TaskModel model) {
 		setBorder(new LineBorder(new Color(0, 0, 0), 2, true));
 		setMinimumSize(new Dimension(30, 20));
 		setPreferredSize(new Dimension(397, 53));
 		setSize(new Dimension(30, 20));
 		this.model=model;
		setLayout(new MigLayout("", "[59.00px][211.00][242.00][94.00][108.00,fill]", "[45.00px,center]"));
 		
 		JCheckBox chckbxNewCheckBox = new JCheckBox("");
 		chckbxNewCheckBox.setMinimumSize(new Dimension(35, 30));
 		chckbxNewCheckBox.setMaximumSize(new Dimension(35, 30));
 		chckbxNewCheckBox.setForeground(new Color(50, 205, 50));
 		chckbxNewCheckBox.setAlignmentX(Component.RIGHT_ALIGNMENT);
 		chckbxNewCheckBox.setActionCommand(taskCheck);
 		add(chckbxNewCheckBox, "cell 0 0,alignx center,aligny center");
 		
 		JLabel taskLabel = new JLabel(model.getTitle());
 		taskLabel.setSize(new Dimension(30, 20));
 		taskLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 18));
 		add(taskLabel, "cell 1 0,alignx center,aligny center");
 		
		Component rigidArea = Box.createRigidArea(new Dimension(20, 20));
		add(rigidArea, "cell 2 0,alignx right");
		
 		deleteTaskButton = new JButton("Delete");
		
 		deleteTaskButton.setActionCommand("deleteTask");
		add(deleteTaskButton, "cell 3 0,alignx center,aligny center");
		
		Component rigidArea_1 = Box.createRigidArea(new Dimension(20, 20));
		add(rigidArea_1, "cell 4 0");
 	}
 	
 	public void setController(TaskController controller){
 		deleteTaskButton.addActionListener(controller);
 	}
 	
 	public TaskModel getModel(){
 		return this.model;
 	}
 	
 	public TaskPanel getPanel(){
 		return this;
 	}
 }
