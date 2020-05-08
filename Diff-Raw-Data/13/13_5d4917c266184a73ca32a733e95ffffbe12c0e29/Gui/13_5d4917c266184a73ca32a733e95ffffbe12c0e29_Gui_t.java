 package de.hpi_web.cloudSim.profiling.gui;
 import java.awt.EventQueue;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import java.awt.BorderLayout;
 import java.awt.event.ActionListener;
 import java.util.Calendar;
 import java.util.List;
 
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.LayoutStyle.ComponentPlacement;
 import javax.swing.JButton;
 import javax.swing.JTextField;
 import javax.swing.JLabel;
 
 import org.cloudbus.cloudsim.Cloudlet;
 import org.cloudbus.cloudsim.Datacenter;
 import org.cloudbus.cloudsim.DatacenterBroker;
 import org.cloudbus.cloudsim.Log;
 import org.cloudbus.cloudsim.Vm;
 import org.cloudbus.cloudsim.core.CloudSim;
 
 import de.hpi_web.cloudSim.multitier.staticTier.VmFactory;
 import de.hpi_web.cloudSim.profiling.datacenter.DatacenterBuilder;
 import de.hpi_web.cloudSim.profiling.datacenter.ProfilingBroker;
 import de.hpi_web.cloudSim.profiling.observer.Observable;
 import de.hpi_web.cloudSim.profiling.observer.Observer;
 import de.hpi_web.cloudSim.profiling.utilization.UtilManager;
 import javax.swing.JTextArea;
 
 
 public class Gui implements Observer {
 
 	private JFrame frame;
 	private JTextField textField;
 	private JTextField textField_1;
 	private JPanel panel;
 	private JTextArea textArea;
 	private StartAction start;
 	private JTextField textField_2;
 
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 	
 				try {
 					Gui window = new Gui();
 					window.frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 		
 		
 	}
 
 	/**
 	 * Create the application.
 	 */
 	public Gui() {
 		start = new StartAction(this);
 		initialize();
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		frame = new JFrame();
 		frame.setBounds(100, 100, 835, 555);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		panel = new JPanel();
 		
 		JPanel panel_1 = new JPanel();
 		
 		JPanel panel_2 = new JPanel();
 		
 		JButton btnNewButton = new JButton("Set new values");
 		btnNewButton.addActionListener(new SetValuesAction(this.start, this));
 		
 		textField = new JTextField();
 		textField.setColumns(10);
 		
 		textField_1 = new JTextField();
 		textField_1.setColumns(10);
 		
 		JLabel lblCpuvmMaxTreshold = new JLabel("CPU/VM MAX threshold");
 		
 		JLabel lblCpuvmMixTreshold = new JLabel("CPU/VM MIN threshold");
 		
 		JButton btnStart = new JButton("Start");
 		btnStart.addActionListener(this.start);
 		
 		JButton btnStop = new JButton("Stop");
 		
 		textField_2 = new JTextField();
 		textField_2.setText("1");
 		textField_2.setColumns(10);
 		
 		JLabel lblDelayAfterEach = new JLabel("Delay after each step [s]");
 		
 		JLabel lblTier = new JLabel("Tier 1");
 		
 		JLabel lblTier_1 = new JLabel("Tier 2");
 		
 		JLabel lblTier_2 = new JLabel("Tier 3");
 		GroupLayout groupLayout = new GroupLayout(frame.getContentPane());
 		groupLayout.setHorizontalGroup(
 			groupLayout.createParallelGroup(Alignment.LEADING)
 				.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
 					.addGap(12)
 					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
 						.addGroup(groupLayout.createSequentialGroup()
 							.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
 								.addComponent(lblCpuvmMixTreshold)
 								.addComponent(lblCpuvmMaxTreshold)
 								.addComponent(lblDelayAfterEach))
 							.addGap(4)
 							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 								.addGroup(groupLayout.createSequentialGroup()
 									.addComponent(textField_2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 									.addGap(18)
 									.addComponent(btnNewButton))
 								.addComponent(textField_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 								.addGroup(groupLayout.createSequentialGroup()
 									.addPreferredGap(ComponentPlacement.RELATED)
 									.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))))
 						.addGroup(groupLayout.createSequentialGroup()
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 								.addComponent(btnStart)
 								.addGroup(groupLayout.createSequentialGroup()
 									.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 										.addComponent(panel, GroupLayout.PREFERRED_SIZE, 269, GroupLayout.PREFERRED_SIZE)
 										.addGroup(groupLayout.createSequentialGroup()
 											.addGap(221)
 											.addComponent(lblTier)))
 									.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
 										.addGroup(groupLayout.createSequentialGroup()
 											.addPreferredGap(ComponentPlacement.RELATED)
 											.addComponent(panel_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 										.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
 											.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 											.addComponent(lblTier_1)
 											.addGap(8)))
 									.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
 										.addGroup(groupLayout.createSequentialGroup()
 											.addPreferredGap(ComponentPlacement.RELATED)
 											.addComponent(panel_2, GroupLayout.PREFERRED_SIZE, 254, GroupLayout.PREFERRED_SIZE))
 										.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
 											.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 											.addComponent(lblTier_2)
 											.addGap(12)))))
 							.addPreferredGap(ComponentPlacement.RELATED)))
 					.addGap(21))
 				.addGroup(groupLayout.createSequentialGroup()
 					.addContainerGap()
 					.addComponent(btnStop, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
 					.addContainerGap(760, Short.MAX_VALUE))
 		);
 		groupLayout.setVerticalGroup(
 			groupLayout.createParallelGroup(Alignment.LEADING)
 				.addGroup(groupLayout.createSequentialGroup()
 					.addContainerGap()
 					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
 						.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
 							.addComponent(lblTier_2)
 							.addComponent(lblTier_1))
 						.addGroup(groupLayout.createSequentialGroup()
 							.addComponent(btnStart)
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
 								.addComponent(btnStop)
 								.addComponent(lblTier))))
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 						.addComponent(panel, GroupLayout.PREFERRED_SIZE, 239, GroupLayout.PREFERRED_SIZE)
 						.addComponent(panel_1, GroupLayout.PREFERRED_SIZE, 239, GroupLayout.PREFERRED_SIZE)
 						.addComponent(panel_2, GroupLayout.PREFERRED_SIZE, 239, GroupLayout.PREFERRED_SIZE))
 					.addPreferredGap(ComponentPlacement.RELATED, 116, Short.MAX_VALUE)
 					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
 						.addGroup(groupLayout.createSequentialGroup()
 							.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
 								.addComponent(lblCpuvmMaxTreshold)
 								.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 							.addGap(12)
 							.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
 								.addComponent(lblCpuvmMixTreshold)
 								.addComponent(textField_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 								.addComponent(lblDelayAfterEach)
 								.addComponent(textField_2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
 						.addComponent(btnNewButton))
 					.addContainerGap())
 		);
 		
 		JTextArea textArea_2 = new JTextArea();
 		textArea_2.setRows(15);
 		textArea_2.setEditable(false);
 		textArea_2.setColumns(24);
 		panel_2.add(textArea_2);
 		
 		JTextArea textArea_1 = new JTextArea();
 		textArea_1.setRows(15);
 		textArea_1.setEditable(false);
 		textArea_1.setColumns(24);
 		panel_1.add(textArea_1);
 		
 		textArea = new JTextArea();
 		textArea.setColumns(24);
 		textArea.setEditable(false);
 		textArea.setRows(15);
 		panel.add(textArea);
 		frame.getContentPane().setLayout(groupLayout);
 	}
 	
 	public JTextField getTextField_2() {
 		return textField_2;
 	}
 	
 	public JTextField getTextField_1() {
 		return textField_1;
 	}
 	
 	public JTextField getTextField() {
 		return textField;
 	}
 
 	@Override
 	public void refreshData(Observable subject) {
 		ProfilingBroker broker = (ProfilingBroker) subject;
 		String newText = "";
 		for(Cloudlet cloudlet :broker.getCloudletSubmittedList()) {
 			double util = cloudlet.getUtilizationModelCpu().getUtilization(CloudSim.clock());
 			int vm = cloudlet.getVmId();
 			newText += "VM: " + vm + "\r\n";
 			newText += "-----------\r\n";
 			newText += "CPU util at " + util + "\r\n\r\n";
 			
 		}
 		textArea.setText(newText);
 		
 		
 	}
 }
