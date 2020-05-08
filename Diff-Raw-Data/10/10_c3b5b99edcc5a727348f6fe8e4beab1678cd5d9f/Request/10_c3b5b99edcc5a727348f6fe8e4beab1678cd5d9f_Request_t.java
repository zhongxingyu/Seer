 import java.awt.Container;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 public class Request extends JFrame {
 	private static String[] locations = {"",
					"CL50 - Class of 1950 Lecture Hall", 
					"EE - Electrical Engineering Building", 
					"LWSN - Lawson Computer Science Building", 
					"PMU - Purdue Memorial Union", 
					"PUSH - Purdue University Student Health Center"};
 	private JComboBox pickLocation;
 	private JButton submitButton;
 	private JLabel statusLabel;
 	
 	public Request() {
 		pickLocation = new JComboBox(locations);
 		pickLocation.setSelectedIndex(0);
 		
 		submitButton = new JButton("Submit");
 		submitButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				locationSubmit();
 			}
 		});
 		
 		statusLabel = new JLabel("");
 		
 		Container controlsPane = new JPanel();
 		controlsPane.setLayout(new FlowLayout());
 		controlsPane.add(pickLocation);
 		controlsPane.add(submitButton);
 		
 		Container labelPane = new JPanel();
 		labelPane.setLayout(new FlowLayout());
 		labelPane.add(statusLabel);
 		
 		Container contentPane = this.getContentPane();
 		contentPane.setLayout(new GridLayout(2,1));
 		contentPane.add(controlsPane);
 		contentPane.add(labelPane);
 		
 		//this.setResizable(false);
 		this.setTitle("Request");
 		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
 		
 		this.pack();
 		this.setVisible(true);
 	}
 	
 	public static void main(String[] args) {
 		new Request();
 	}
 	
 	private void locationSubmit() {
 		if(pickLocation.getSelectedIndex()!=0) {
 			pickLocation.setEnabled(false);
 			submitButton.setEnabled(false);
 			statusLabel.setText("Searching");
 		
 			this.pack();
 			sendRequest(locations[pickLocation.getSelectedIndex()]);
 		}
 	}
 	
 	private void sendRequest(String r) {
 		System.out.println(r);
 	}
 	
 	private void assigned(String team) {
 		statusLabel.setText("Assigned: " + team);
 		pickLocation.setEnabled(true);
 		submitButton.setEnabled(true);
 		
 		this.pack();
 	}
 }
 
