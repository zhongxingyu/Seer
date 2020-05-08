 package GUI;
 
 import java.awt.BorderLayout;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.ArrayList;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.border.EtchedBorder;
 
 import main.Machine;
 import main.Tool;
 import main.User;
 
 public class AdminGUI extends JPanel {
 
 	private static JPanel centerPanel;
 	private static JPanel contentPanel;
 	private static JPanel buttonPanel;
 	private JPanel machinePermissions;
 	private static JPanel checkedOutTools;
 	private static UserCheckoutToolPanel userCheckoutToolPanel;
 	private static User currentUser;
 
 	private Font buttonFont;
 	
 	private ButtonListener buttonListener;
 	
 	JButton logOut;
 	JButton checkOutTools;
 	JButton selectMachine;
 	JButton returnTools;
 	JButton generateReport;
 	JButton viewToolsAndMachines;
 	JButton viewActiveUsers;
 	JButton done;
 	
 	GridBagConstraints c = new GridBagConstraints();
 	
 	private ArrayList<Machine> selectedMachines;
 	private ArrayList<Tool> toolsToReturn;
 	private static ToolCheckBoxListener toolCheckBoxListener;
 	
 	public AdminGUI(User user) {
 		currentUser = user;
 		
 		setLayout(new BorderLayout());
 		
 		buttonFont = new Font("SansSerif", Font.BOLD, 24);
 		
 		buttonListener = new ButtonListener();
 		
 		centerPanel = new JPanel(new GridBagLayout());
 		contentPanel = new JPanel(new GridLayout(2, 1));
 		buttonPanel = new JPanel(new GridLayout(8, 1));
 		userCheckoutToolPanel = new UserCheckoutToolPanel();
 		
 		machinePermissions = new JPanel();
 		checkedOutTools = new JPanel();
 		
 		selectedMachines = new ArrayList<Machine>();
 		toolsToReturn = new ArrayList<Tool>();
 		
 		toolCheckBoxListener = new ToolCheckBoxListener();
 				
 		machinePermissions.setBorder(BorderFactory.createTitledBorder(new EtchedBorder(), "My Machines"));
 		machinePermissions.setLayout(new GridLayout(10, 5));
 		
 		checkedOutTools.setBorder(BorderFactory.createTitledBorder(new EtchedBorder(), "Checked-out Tools"));
 		checkedOutTools.setLayout(new GridLayout(10, 5));
 		
 		displayUserMachinePermissions();
 		displayUserCheckedOutTools();
 		
 		contentPanel.add(machinePermissions);		
 		contentPanel.add(checkedOutTools);
 		
 		c.fill = GridBagConstraints.BOTH;
 		c.gridx = 0;
 		c.gridy = 0;
 		c.weighty = 1;
 		c.weightx = 0.5;
 		c.anchor = GridBagConstraints.LINE_START;
 		
 		centerPanel.add(contentPanel, c);
 		
 		c.anchor = GridBagConstraints.LINE_END;
 		c.weightx = 0;
 		c.gridx = 1;
 		
 		centerPanel.add(buttonPanel, c);
 		
 		logOut = new JButton();
 		checkOutTools = new JButton();
 		selectMachine = new JButton();
 		returnTools = new JButton();
 		generateReport = new JButton();
 		viewToolsAndMachines = new JButton();
 		viewActiveUsers = new JButton();
 		done = new JButton();
 		
 		logOut.setText("Log Out");
 		selectMachine.setText("Select Machines");
 		checkOutTools.setText("Check Out Tools");
 		returnTools.setText("Return Tools");
 		generateReport.setText("Generate Report");
 		viewToolsAndMachines.setText("View Tools / Machines");
 		viewActiveUsers.setText("View Active Users");
 		done.setText("Done");
 		
 		logOut.setFont(buttonFont);
 		checkOutTools.setFont(buttonFont);
 		selectMachine.setFont(buttonFont);
 		returnTools.setFont(buttonFont);
 		generateReport.setFont(buttonFont);
 		viewToolsAndMachines.setFont(buttonFont);
 		viewActiveUsers.setFont(buttonFont);
 		done.setFont(buttonFont);
 		
 		buttonPanel.add(selectMachine);
 		buttonPanel.add(checkOutTools);
 		buttonPanel.add(returnTools);
 		buttonPanel.add(generateReport);
 		buttonPanel.add(viewActiveUsers);
		buttonPanel.add(viewToolsAndMachines);
 		buttonPanel.add(done);
 		buttonPanel.add(logOut);
 		
 		add(centerPanel, BorderLayout.CENTER);
 		
 		checkOutTools.addActionListener(buttonListener);
 		selectMachine.addActionListener(buttonListener);
 		returnTools.addActionListener(buttonListener);
 		generateReport.addActionListener(buttonListener);
 		viewToolsAndMachines.addActionListener(buttonListener);
 		viewActiveUsers.addActionListener(buttonListener);
 		
 
 		logOut.addActionListener(new ListenerHelpers.LogOutListener());	
 		done.addActionListener(new ListenerHelpers.DoneListener());
 	
 	}
 	
 	private void displayUserMachinePermissions() {
 		machinePermissions.removeAll();
 		ArrayList<Machine> machines = currentUser.getCertifiedMachines();
 		for (Machine m:machines) {
 			String show = m.getName() + " (" + m.getID() + ")";
 			JCheckBox machine = new JCheckBox(show);
 			machine.setName(m.getID());
 			machine.setFont(new Font("SansSerif", Font.BOLD, 20));
 			if (selectedMachines.contains(m)){
 				machine.setEnabled(false);
 			}
 			machine.addItemListener(new MachineCheckBoxListener());
 			machinePermissions.add(machine);
 		}
 	}
 	
 	private static void displayUserCheckedOutTools() {
 		checkedOutTools.removeAll();
 		ArrayList<Tool> tools = currentUser.getToolsCheckedOut();
 		for (Tool t:tools) {
 			String show = t.getName() + " (" + t.getUPC() + ")";
 			JCheckBox tool = new JCheckBox(show);
 			tool.setName(t.getUPC());
 			tool.setFont(new Font("SansSerif", Font.BOLD, 20));
 			tool.addItemListener(toolCheckBoxListener);
 			checkedOutTools.add(tool);
 		}
 	}	
 	
 	public void selectMachines(){
 		for (Machine m : Driver.getAccessTracker().getCurrentUser().getCurrentEntry().getMachinesUsed()) {
 			m.stopUsing();
 		}
 		
 		Driver.getAccessTracker().getCurrentUser().getCurrentEntry().addMachinesUsed(selectedMachines);
 		
 		String message = "You are using:\n\n";
 		for ( Machine m : selectedMachines ) {
 			message += m + "\n";
 		}
 		
 		for (Machine m : Driver.getAccessTracker().getCurrentUser().getCurrentEntry().getMachinesUsed()) {
 			m.use();
 		}		
 		
 		JOptionPane.showMessageDialog(this, message);
 		
 		displayUserMachinePermissions();
 		//selectedMachines.clear();
 	}
 	
 	public void returnTools(){
 		
 		if (toolsToReturn.size() == 0){
 			JOptionPane.showMessageDialog(this, "No tools selected");
 			return;
 		}
 		
 		Driver.getAccessTracker().getCurrentUser().getCurrentEntry().addToolsReturned(toolsToReturn);
 		
 		Driver.getAccessTracker().getCurrentUser().returnTools(toolsToReturn);
 		displayUserCheckedOutTools();
 		centerPanel.repaint();
 		
 		toolsToReturn.clear();
 	}
 	
 	public void switchPanels(JPanel panel) {
 		centerPanel.removeAll();
 		c.anchor = GridBagConstraints.LINE_END;
 		c.weightx = 0;
 		c.gridx = 1;
 		centerPanel.add(buttonPanel, c);
 		c.fill = GridBagConstraints.BOTH;
 		c.gridx = 0;
 		c.gridy = 0;
 		c.weighty = 1;
 		c.weightx = 0.5;
 		c.anchor = GridBagConstraints.LINE_START;
 		centerPanel.add(panel, c);
 		repaint();
 	}
 	
 	private class ButtonListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			if ( e.getSource() == checkOutTools ) {
 				switchPanels(userCheckoutToolPanel);
 			} else if ( e.getSource() == selectMachine ){
 				selectMachines();
 			} else if ( e.getSource() == returnTools ){
 				returnTools();
 			} else if ( e.getSource() == generateReport) {
 				switchPanels(new GenerateReportPanel());
 			} else if ( e.getSource() == viewToolsAndMachines) {
 				switchPanels(new ViewToolsAndMachinesPanel());
 			} else if ( e.getSource() == viewActiveUsers) {
 				switchPanels(new ViewActiveUsersPanel());
 			}
 		}
 	}
 	
 	private class MachineCheckBoxListener implements ItemListener {
 		@Override
 		public void itemStateChanged(ItemEvent e) {
 			JCheckBox check = (JCheckBox) e.getSource();
 			String id = check.getName();
 			
 			Machine m = Driver.getAccessTracker().getMachineByID(id);
 			
 			switch (e.getStateChange()){
 			case ItemEvent.SELECTED:
 				selectedMachines.add(m);
 				break;
 			case ItemEvent.DESELECTED:
 				if (selectedMachines.contains(m)){
 					selectedMachines.remove(m);
 				}
 				break;
 			}
 		}
 	}
 	
 	private class ToolCheckBoxListener implements ItemListener {
 		@Override
 		public void itemStateChanged(ItemEvent e) {
 			JCheckBox check = (JCheckBox) e.getSource();
 			String upc = check.getName();
 			
 			Tool t = Driver.getAccessTracker().getToolByUPC(upc);
 			
 			switch (e.getStateChange()){
 			case ItemEvent.SELECTED:
 				toolsToReturn.add(t);
 				break;
 			case ItemEvent.DESELECTED:
 				if (toolsToReturn.contains(t)){
 					toolsToReturn.remove(t);
 				}
 				break;
 			}
 		}
 	}
 }
