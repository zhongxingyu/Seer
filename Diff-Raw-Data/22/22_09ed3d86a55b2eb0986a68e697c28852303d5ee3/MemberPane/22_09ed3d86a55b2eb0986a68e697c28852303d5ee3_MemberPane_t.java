 package multiStakeholderGUI;
 
 import java.awt.Dimension;
 import java.util.Collection;
 import java.util.LinkedList;
 
 import javax.swing.BoxLayout;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.SpringLayout;
 
 import mainGUI.UpdatePane;
 
 import dataStructures.Role;
 import dataStructures.maps.RoleMap;
 
 /**
  * MemberPane is an UpdatePane which contains a MemberListPane for each
  * Role within the project.
  */
 @SuppressWarnings("serial")
 public class MemberPane extends UpdatePane {
 
 	protected RoleMap map;
 	protected JPanel stakeholderPanel;
 	protected JFrame parentFrame;
 	private SpringLayout layout;
 	private LinkedList<JPanel> rolePanels;
 
 	/**
 	 * create a new MemberPane instance
 	 * @param oldMap
 	 * @param parentFrame
 	 */
 	public MemberPane(RoleMap oldMap, JFrame parentFrame) {
 		this.map = oldMap;
 		this.parentFrame = parentFrame;
 		this.rolePanels = new LinkedList<JPanel>();
 		this.add(initializeGUI());
 		
 		setVisible(true);
 		resize();
 	}
 
 	/**
 	 * @return JPanel
 	 */
 	private JPanel initializeGUI() {
 		JPanel panel = new JPanel();
 		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
 		
 		JPanel label = new JPanel();
 		label.setLayout(new BoxLayout(label, BoxLayout.X_AXIS));
 		JTextField title = new JTextField("Enter Role Members");
 		title.setEditable(false);
 		title.setHorizontalAlignment(JTextField.CENTER);
 		label.add(title);
 		panel.add(label);
 		
 		stakeholderPanel = new JPanel();
 		layout = new SpringLayout();
 		stakeholderPanel.setLayout(layout);
 		
 		JScrollPane stakeholderScrollPane = new JScrollPane(stakeholderPanel);
 		stakeholderScrollPane.setPreferredSize(new Dimension(600, 650));
 		stakeholderScrollPane.setBorder(null);
 		
 		update();
 		
 		panel.add(stakeholderScrollPane);
 		
 		return panel;
 	}
  
	@Override
 	public void update() {
 		
 		//create table header
 		stakeholderPanel.removeAll();
 		rolePanels.clear();
 		
 		// for every role, add a panel to the table
 		Collection<Role> roles = map.values();
 		
 		JPanel lastPanel = null;
 		
 		for (Role role : roles){
 			JPanel rolePanel = new MemberListPane(role, role.getObject(), parentFrame, this);
 			stakeholderPanel.add(rolePanel);
 			
 			if(lastPanel != null ) {
 				layout.putConstraint(SpringLayout.NORTH, rolePanel, 5,
 						SpringLayout.SOUTH, lastPanel);
 				layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, rolePanel, 0,
 						SpringLayout.HORIZONTAL_CENTER, lastPanel);
 			} else {
 				layout.putConstraint(SpringLayout.NORTH, rolePanel, 5,
 						SpringLayout.NORTH, stakeholderPanel);
 				layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, rolePanel, 5,
 						SpringLayout.HORIZONTAL_CENTER, stakeholderPanel);
 			}
 			
 			rolePanels.add(rolePanel);
 			lastPanel = rolePanel;
 		}
 		
 		resize();
 		parentFrame.pack();
 		
 	}
 	
	/**
	 * Resizes the panel holding MemberListPanes so that the scroll bar works properly
	 */
 	public void resize() {
 		double height = 40;
 		
 		for(JPanel panel: rolePanels) {
 			height += panel.getPreferredSize().getHeight();
 		}
 		
 		stakeholderPanel.setPreferredSize(new Dimension(570, (int)height));
 		stakeholderPanel.setMinimumSize(new Dimension(570, (int)height));
 	
 	}
 }
