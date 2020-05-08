 import javax.swing.JButton;
 import javax.swing.JTabbedPane;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JFrame;
 import java.awt.*;
 import java.awt.event.*;
 import java.io.DataOutputStream;
 import java.io.DataInputStream;
 import java.io.OutputStream;
 import java.io.InputStream;
 
 import lejos.pc.comm.*;
 
 public class GUI extends JPanel{
 
 	static final long serialVersionUID = 1;
 	
 	private static JFrame frame;
 	private static JLabel varSection, errorSection, uplinkSection, sourcecodeSection, breakpointSection, sensorSection;
 	private static NXTComm connection;
 	private static OutputStream os;
 	private static InputStream is;
 	private static DataOutputStream oHandle;
 	private static DataInputStream iHandle;
 	private static NXTInfo[] info;
 	private static int seqNum;
 
 	private static String buildMessage(String opcode) {
 		String ret;
 		if (seqNum > 99) {
 			ret = Integer.toString(seqNum) + " ";
 		}
 		else if (seqNum > 9) {
 			ret = "0" + Integer.toString(seqNum) + " ";
 		}
 		else {
 			ret = "00" + Integer.toString(seqNum) + " ";
 		}
 		ret += "012 ";
 		ret += opcode;
 		return ret;
 	}
 	
 	private static String buildMessage(String opcode, String parameters) {
 		String ret;
 		int len = 13;
 		len += parameters.length();
 		
 		if (seqNum > 99) {
 			ret = Integer.toString(seqNum) + " ";
 		}
 		else if (seqNum > 9) {
 			ret = "0" + Integer.toString(seqNum) + " ";
 		}
 		else {
 			ret = "00" + Integer.toString(seqNum) + " ";
 		}
 		
 		if (len > 99) {
			ret += Integer.toString(seqNum) + " ";
 		}
 		else if (len > 9) {
			ret += "0" + Integer.toString(seqNum) + " ";
 		}
 		else {
			ret += "00" + Integer.toString(seqNum) + " ";
 		}
 		ret += opcode + " " + parameters;
 		return ret;
 	}
 	
 	private static void sensorUpdateLoop() {
 		connection = null;
 		try {
 			connection = NXTCommFactory.createNXTComm(NXTCommFactory.BLUETOOTH);
			info = connection.search("LEAD9",1234);
 
 			connection.open(info[0]);
 
 			os = connection.getOutputStream();
 			is = connection.getInputStream();
 			oHandle = new DataOutputStream(os);
 			iHandle = new DataInputStream(is);
 
 
 			while (true) {
				String s = "";
 				oHandle.write(buildMessage("0011","1").getBytes());
 				s = iHandle.readUTF();
 				oHandle.write(buildMessage("0011", "2").getBytes());
 				s = s + iHandle.readUTF();
 				oHandle.write(buildMessage("0011", "3").getBytes());
 				s = s + iHandle.readUTF();
 				oHandle.write(buildMessage("0011", "4").getBytes());
 				s = s + iHandle.readUTF();
 
 				sensorSection.setText(s);
 
 				try {
 					System.out.println("starting sleep");
 					Thread.sleep(500);
 					System.out.println("ending sleep");
 				} catch (Exception e) {
 					System.out.println(e.toString());
 				}
 			}
 		} catch (Exception e) {
 			System.out.println(e.toString());
 		}
 	}
 
 	private static void makeGUI() {
 		seqNum = 1;
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
 		sensorUpdateLoop();
 	}
 
 	private static JTabbedPane makeTabSection() {
 		JTabbedPane pane = new JTabbedPane();
 		JPanel sensorPanel =  createSensorPanel();
 		pane.addTab("Sensor", sensorPanel);
 		pane.setSelectedIndex(0);
 		JPanel debugPanel = createDebugPanel();
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
 		sensorSection = new JLabel("Sensor Section");
 		panel.add(sensorSection, c);
 		c.gridx = 1;
 		c.ipadx = 100;
 		uplinkSection = new JLabel("Uplink info");
 		panel.add(uplinkSection, c);
 		return panel;
 	}
 
 	private static JPanel createDebugPanel() {
 		JPanel panel = new JPanel();
 		panel.setLayout(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 		c.weightx = 1;
 		c.gridx = 0;
 		c.gridy = 0;
 		c.ipadx = 300;
 		sourcecodeSection = new JLabel("Source Code");
 		panel.add(sourcecodeSection, c);
 		c.gridx = 1;
 		c.ipadx = 100;
 		breakpointSection = new JLabel("Breakpoints & stuff");
 		panel.add(breakpointSection, c);
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
 
 	public void actionPerformed(ActionEvent e) {
 		try {
 			if (e.getActionCommand().equals("Abort")) {
 				oHandle.write(buildMessage("0110").getBytes());
 				oHandle.flush();
 			}
 			else if (e.getActionCommand().equals("E Stop")) {
 				oHandle.write(buildMessage("0101").getBytes());
 				oHandle.flush();
 			}
 			errorSection.setText(e.getActionCommand());
 		} catch (Exception b) {
 			System.out.println(e.toString());
 		}
 	}
 
 	public static void main(String[] args) {
 		makeGUI();
 	}
 }
