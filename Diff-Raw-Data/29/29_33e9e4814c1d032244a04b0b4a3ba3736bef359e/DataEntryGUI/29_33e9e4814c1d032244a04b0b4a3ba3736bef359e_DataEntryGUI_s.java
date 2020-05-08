 package GUI;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 
 import GUI.ListenerHelpers.LogOutListener;
 
 public class DataEntryGUI extends MainPanel {
 	
 	private JButton logOut;
 	private JButton certifications;
 	private JButton users;
 	private JButton tools;
 	private JButton machines;
 	private JButton privileges;
 	private JButton generateReport;
 	private JButton viewActiveUsers;
 	private JButton viewToolsAndMachines;
 	private ButtonListener buttonListener;
 	private Color buttonBackground;
 	
 	public DataEntryGUI() {
 		
 		super(Driver.getAccessTracker().getCurrentUser());
 		
 		buttonListener = new ButtonListener();
 		
 		contentPanel = new JPanel(new BorderLayout());
 		contentPanel.setBorder(BorderFactory.createEtchedBorder());
 		buttonPanel = new JPanel(new GridLayout(0, 1));
 		buttonPanel.setBorder(BorderFactory.createEtchedBorder());
 		
 		certifications = new JButton();
 		users = new JButton();
 		tools = new JButton();
 		machines = new JButton();
 		privileges = new JButton();
 		generateReport = new JButton();
 		viewActiveUsers = new JButton();
 		viewToolsAndMachines = new JButton();
 		logOut = new JButton();
 		
 		certifications.setText("Machine Certifications");
 		users.setText("Edit Users");
 		tools.setText("Edit Tools");
 		machines.setText("Edit Machines");
 		privileges.setText("User Privileges");
 		generateReport.setText("Generate Report");
 		viewActiveUsers.setText("View Active Users");
 		viewToolsAndMachines.setText("View Tools/Machines");
 		logOut.setText("Log Out");
 		
 		ArrayList<JButton> buttons = new ArrayList<JButton>();
 		
 		buttons.add(certifications);
 		buttons.add(users);
 		buttons.add(tools);
 		buttons.add(machines);
 		buttons.add(privileges);
 		buttons.add(generateReport);
 		buttons.add(viewActiveUsers);
 		buttons.add(viewToolsAndMachines);
 		buttons.add(logOut);
 		
 		for ( JButton b : buttons ) {
 			b.setFont(buttonFont);
 			b.addActionListener(buttonListener);
 			buttonPanel.add(b);
 		}
 		
 		logOut.removeActionListener(buttonListener);
 		logOut.addActionListener(new ListenerHelpers.LogOutListener());
 		
 		c.fill = GridBagConstraints.BOTH;
 		c.gridx = 0;
 		c.gridy = 0;
 		c.weighty = 1;
 		c.weightx = 0.5;
 		c.anchor = GridBagConstraints.LINE_START;
 		
 		add(contentPanel, c);
 		
 		c.anchor = GridBagConstraints.LINE_END;
 		c.weightx = 0;
 		c.gridx = 1;
 		
 		add(buttonPanel, c);
 		
 		switchContentPanel(new ViewToolsAndMachinesPanel());
 		
 	}
 	
 	@Override
 	public void setup() {
 		setLayout(new GridBagLayout());
 		buttonBackground = new Color(63, 146, 176);
 		buttonFont = new Font("SansSerif", Font.BOLD, 30);
 	}
 	
 	private class ButtonListener implements ActionListener {
 		
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			
 			if ( e.getSource() == certifications ) {
 				switchContentPanel(new EditCertificationsPanel());
 			} else if ( e.getSource() == users ) {
 				switchContentPanel(new EditUsersPanel());
 			} else if ( e.getSource() == tools ) {
 				switchContentPanel(new EditToolsPanel());
 			} else if ( e.getSource() == machines ) {
 				switchContentPanel(new EditMachinesPanel());
 			} else if ( e.getSource() == privileges ) {
 				switchContentPanel(new EditPrivilegesPanel());
 			} else if ( e.getSource() == generateReport ) {
 				switchContentPanel(new GenerateReportPanel());
 			} else if ( e.getSource() == viewActiveUsers ) {
 				switchContentPanel(new ViewActiveUsersPanel() );
 			} else if ( e.getSource() == viewToolsAndMachines ) {
 				switchContentPanel(new ViewToolsAndMachinesPanel());
 			}
 			JButton current = (JButton) e.getSource();
 			current.setBackground(buttonBackground);
 		}
 	}
 }
