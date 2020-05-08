 package view;
 
 import javax.swing.JPanel;
 
 import model.TaskModel;
 import net.miginfocom.swing.MigLayout;
 
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JCheckBox;
 import java.awt.Dimension;
 
 import java.awt.Component;
 import javax.swing.JButton;
 import utility.*;
 
 import controller.TaskController;
 import javax.swing.JToggleButton;
 import java.awt.Color;
 
 /**
  * A class to represent a TaskPanel. 
  * 
  * @author Hanna
  */
 
 public class TaskPanel extends JPanel {
 	
 	private TaskModel model;
 	private String taskCheck;
 	private JButton deleteTaskButton;
 	/**
 	 * Create the panel.
 	 */
 	public TaskPanel(TaskModel model) {
 		this.setBackground(GraphicConstants.BACKGROUND);
 		setMinimumSize(new Dimension(290, 60));
 		setPreferredSize(new Dimension(290, 60));
 		setMaximumSize(new Dimension(290, 60));
 //		setSize(new Dimension(30, 20));
 		this.model=model;
 		setLayout(new MigLayout("", "[40.00px,left][100.00][30.00][88.00][93.00]", "[45.00px,center]"));
 		
//		GridBagLayout gridBagLayout = new GridBagLayout();
//		gridBagLayout.columnWidths = new int[]{32, 127, 0};
//		gridBagLayout.rowHeights = new int[]{111, 0};
//		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0};
//		gridBagLayout.rowWeights = new double[]{0.0, Double.MIN_VALUE};
//		setLayout(gridBagLayout);
		
		
 		
 		JToggleButton checkbutton = new JToggleButton("");
 		checkbutton.setBorderPainted(false);
 		checkbutton.setIcon(new ImageIcon(TaskPanel.class.getResource("/utility/icons/checkmark_icon&16.png")));
 		checkbutton.setAlignmentX(Component.RIGHT_ALIGNMENT);
 		checkbutton.setActionCommand(taskCheck);
 		add(checkbutton, "cell 0 0,alignx center,aligny center");
		//GridBagConstraints gbc_chckbx = new GridBagConstraints();
//		gbc_chckbx.gridx = 0;
//		gbc_chckbx.gridy = 0;
//		add(chckbxNewCheckBox, gbc_chckbx);
//		
 		JLabel taskLabel = new JLabel(model.getTitle());
 		taskLabel.setMinimumSize(new Dimension(200, 30));
 		taskLabel.setMaximumSize(new Dimension(200, 30));
 		taskLabel.setPreferredSize(new Dimension(200, 30));
 		taskLabel.setFont(GraphicConstants.SMALLHEADINGFONT);
 		add(taskLabel, "cell 1 0 3 1,alignx left,aligny center");
//		GridBagConstraints gbc_taskLabel = new GridBagConstraints();
//		gbc_taskLabel.gridx =1;
//		gbc_taskLabel.gridy = 0;
//		add(taskLabel, gbc_taskLabel);
 		
 		deleteTaskButton = new JButton();
 		deleteTaskButton.setBorderPainted(false);
 		deleteTaskButton.setIcon(new ImageIcon(CategoryPanel.class.getResource("/utility/icons/delete_icon&16.png")));
 		//deleteTaskButton.setFont(GraphicConstants.REGULARFONT);
 		deleteTaskButton.setActionCommand("deleteTask");
 		add(deleteTaskButton, "cell 4 0,alignx right,aligny center");
 		
//		GridBagConstraints gbc_deleteTaskButton = new GridBagConstraints();
//		gbc_deleteTaskButton.anchor = GridBagConstraints.WEST;
//		gbc_deleteTaskButton.gridx = 2;
//		gbc_deleteTaskButton.gridy = 0;
//		add(deleteTaskButton, gbc_deleteTaskButton);
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
