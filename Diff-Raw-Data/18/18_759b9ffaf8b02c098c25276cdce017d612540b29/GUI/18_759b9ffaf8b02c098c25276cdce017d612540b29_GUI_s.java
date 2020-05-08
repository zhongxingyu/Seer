 import javax.swing.JButton;
 import javax.swing.JTabbedPane;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JFrame;
 import java.awt.*;
 import java.awt.event.*;
 
 public class GUI extends JPanel{
 	
 	private static JFrame frame;
 	private static JPanel debugPanel, sensorPanel;
 	private static JLabel varSection, errorSection;
 	
 	private static void makeGUI() {
 		frame = new JFrame("Debug GUI");
 		frame.addWindowListener(new WindowAdapter() {
 
 			public void windowClosing(WindowEvent e) {
 				System.exit(0);
 			}
 		});
 		frame.getContentPane().setLayout(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 		
 		c.fill = GridBagConstraints.NONE;
 		//c.weightx = 0.5;
 		//c.weighty = 0.5;
 		c.gridx = 0;
 		c.gridy = 0;
 		c.gridheight = 2;
 		c.ipady = 100;
 		frame.getContentPane().add(makeTabSection(), c);
 		
 		c.ipady = 0;
 		c.gridx = 1;
 		c.gridheight = 1;
 		frame.getContentPane().add(makeAbortButton(), c);
 		
 		c.gridx = 2;
 		frame.getContentPane().add(makeEStopButton(), c);
 		
 		c.gridwidth = 2;
 		c.gridheight = 1;
 		c.gridx = 1;
 		c.gridy = 1;
 		frame.getContentPane().add(makeVarSection(), c);
 		
 		c.gridwidth = 3;
 		c.gridx = 0;
 		c.gridy = 2;
 		frame.getContentPane().add(makeErrorSection(), c);
 		
 		frame.setSize(800, 400);
 		frame.setVisible(true);
 	}
 	
 	private static JTabbedPane makeTabSection() {
 		JTabbedPane pane = new JTabbedPane();
 		sensorPanel =  createSensorPanel();
 		pane.addTab("Sensor", sensorPanel);
 		pane.setSelectedIndex(0);
 		debugPanel = createDebugPanel();
 		pane.addTab("Debug", debugPanel);
 		return pane;
 	}
 	
 	private static JPanel createSensorPanel() {
 		JPanel panel = new JPanel();
 		//panel.setLayout(new GridLayout(1, 0));
 		panel.setLayout(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 		c.weightx = 1;
 		c.gridx = 0;
 		c.gridy = 0;
 		c.ipadx = 300;
 		JLabel sensorInfo = new JLabel("Laura Ipsen");
 		panel.add(sensorInfo, c);
 		c.gridx = 1;
 		c.ipadx = 100;
 		JLabel uplinkInfo = new JLabel("Uplink info");
 		panel.add(uplinkInfo, c);
 		return panel;
 	}
 	
 	private static JPanel createDebugPanel() {
 		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1, 0));
		JLabel sourceCode = new JLabel("Source Code Placeholder text");
		panel.add(sourceCode);
		JLabel debuggingInfo = new JLabel("Single Steps, Breakpoints, etc.");
		panel.add(debuggingInfo);
 		return panel;
 	}
 	
 	private static JButton makeAbortButton() {
 		JButton button = new JButton("Abort");
 		return button;
 	}
 	
 	private static JButton makeEStopButton() {
 		JButton button = new JButton("E Stop");
 		return button;
 	}
 	
 	private static JLabel makeVarSection() {
 		varSection = new JLabel("Variables and stuff");
 		return varSection;
 	}
 	
 	private static JLabel makeErrorSection() {
 		errorSection = new JLabel("Errors... and... like... stuff. Man.");
 		return errorSection;
 	}
 	
 	public static void main(String[] args) {
 		makeGUI(); // A somewhat awkward way of getting around having to static EVERYTHING.
 	}
 }
