 //Stephanie Reagle, Joey Huang, Marc Mendiola
 //CS 200
 // last edited: 10:24 pm 11/28/12
 package factory.swing;
 
 
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import factory.managers.*;
 
 import javax.swing.*;
 import javax.swing.border.*;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 
 public class LaneManPanel extends JPanel{
 
 	private static final long serialVersionUID = -4485912622490446254L;
 
 	String [] stringLanes = { "1", "2", "3", "4", "5", "6", "7", "8"};
 	JComboBox lane = new JComboBox(stringLanes);
 	//ImageIcon red = new ImageIcon("red.png");
 	JButton redButton = new JButton("Red");
 	JButton yellowButton = new JButton("Yellow");
 	JButton greenButton = new JButton("Green");
 	JButton powerButton = new JButton("Power");
 
 
 
 	LaneManager laneManager;
 	JTabbedPane tabbedPane; 
 	JPanel preferencesPanel;
 	LaneNonNormPanel nonnormativePanel;
 
 	public LaneManPanel(LaneManager l){
 		laneManager = l;
 
 		preferencesPanel = new LanePreferencesPanel();
 		nonnormativePanel = new LaneNonNormPanel();
 		tabbedPane = new JTabbedPane();
 
 		GridBagConstraints c = new GridBagConstraints();
 		setLayout(new GridBagLayout());
 
 		tabbedPane.addTab("Preferences", preferencesPanel);
 		tabbedPane.addTab("Non-Normative", nonnormativePanel);
 
 		this.add(tabbedPane);
 
 
 	}
 
 	public int getSelectedLane(){
 		return (Integer) lane.getSelectedItem();
 
 	}
 
 
 
 
 	public class LanePreferencesPanel extends JPanel implements ActionListener{
 
 		JLabel title;
 		JLabel laneLabel;
 		JLabel laneSpeedLabel;
 		JLabel laneAmplitudeLabel;
 		JLabel lanePowerLabel;
 		JLabel feederLabel;
 		JLabel feederPowerLabel;
 		JButton laneOn;
 		JButton laneOff;
 		JButton feederOn;
 		JButton feederOff;
 		JSlider laneSpeed;
 		JSlider laneAmplitude;
 		JComboBox laneSelect;
 		JComboBox feederSelect;
 
 		JPanel feederSection;
 		JPanel laneSection;
 
 		int speedMin;
 		int speedMax;
 		int amplitudeMin;
 		int amplitudeMax;
 
 		public LanePreferencesPanel(){
 
 			speedMin = 1;
 			speedMax = 8;
 			amplitudeMin = 1;
 			amplitudeMax = 8;
 			this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 			title = new JLabel("Lane Manager");
 			title.setAlignmentX(Component.CENTER_ALIGNMENT);
 			title.setFont(new Font("Serif", Font.BOLD, 16));
 			laneLabel = new JLabel("Lane : ");
 			lanePowerLabel = new JLabel("Power : ");
 			laneSpeedLabel = new JLabel("Speed : "); 
 			laneAmplitudeLabel = new JLabel("Amplitude : ");
 			laneSpeed = new JSlider(speedMin, speedMax);
 			laneSpeed.setMinorTickSpacing(1);
 			laneSpeed.setMajorTickSpacing(7);
 			laneSpeed.setPaintTicks(true);
 			laneSpeed.setSnapToTicks(true);
 			laneSpeed.setPaintLabels(true);
 			laneSpeed.setValue(1);
 			laneSpeed.addChangeListener(new SliderDetection());
 			laneAmplitude = new JSlider(amplitudeMin, amplitudeMax);
 			laneAmplitude.setMinorTickSpacing(1);
 			laneAmplitude.setMajorTickSpacing(7);
 			laneAmplitude.setPaintTicks(true);
 			laneAmplitude.setSnapToTicks(true);
 			laneAmplitude.setPaintLabels(true);
 			laneAmplitude.setValue(1);
 			laneAmplitude.addChangeListener(new SliderDetection());
 			laneOn = new JButton("ON");
 			laneOff = new JButton("OFF");
 			laneOn.addActionListener(this);
 			laneOff.addActionListener(this);
 			laneSelect = new JComboBox();
 			for(int i = 1; i <= 8; i++){
 				laneSelect.addItem(i);
 			}
 			laneSelect.addActionListener(this);
 
 			feederLabel = new JLabel("Feeder : ");
 			feederSelect = new JComboBox();
 			for(int i = 1; i <= 4; i++){
 				feederSelect.addItem(i);
 			}
 			feederSelect.addActionListener(this);
 			feederPowerLabel = new JLabel("Power : ");
 			feederOn = new JButton("ON");
 			feederOff = new JButton ("OFF");
 			feederOn.addActionListener(this);
 			feederOff.addActionListener(this);
 
 			TitledBorder feederBorder = BorderFactory.createTitledBorder("Feeder Control");
 
 			feederSection = new JPanel();
 			feederSection.setLayout(new GridBagLayout());
 			feederSection.setBorder(feederBorder);
 			GridBagConstraints c = new GridBagConstraints();
 
 			c.fill = GridBagConstraints.HORIZONTAL;
 			c.gridx = 0;
 			c.gridy = 0;
 			feederSection.add(feederLabel, c);
 			c.gridx = 1;
 			c.gridy = 0;
 			feederSection.add(feederSelect, c);
 			c.gridx = 0;
 			c.gridy = 1;
 			feederSection.add(feederPowerLabel, c);
 			c.gridx = 1;
 			c.gridy = 1;
 			feederSection.add(feederOn, c);
 			c.gridx = 2;
 			c.gridy = 1;
 			feederSection.add(feederOff, c);
 
 
 			TitledBorder laneBorder = BorderFactory.createTitledBorder("Lane Control");
 			laneSection = new JPanel();
 			laneSection.setBorder(laneBorder);
 			laneSection.setLayout(new GridBagLayout());
 
 			c.gridx = 0;
 			c.gridy = 0;
 			laneSection.add(laneLabel, c);
 			c.gridx = 1;
 			c.gridy = 0;
 			laneSection.add(laneSelect, c);
 			c.gridx = 0;
 			c.gridy = 1;
 			laneSection.add(lanePowerLabel, c);
 			c.gridx = 1;
 			c.gridy = 1;
 			laneSection.add(laneOn, c);
 			c.gridx = 2;
 			c.gridy = 1;
 			laneSection.add(laneOff, c);
 			c.gridx = 0;
 			c.gridy = 2;
 			laneSection.add(laneSpeedLabel, c);
 			c.gridwidth = 3;
 			c.gridx = 1;
 			c.gridy = 2;
 			laneSection.add(laneSpeed, c);
 			c.gridwidth = 1;
 			c.gridx = 0;
 			c.gridy = 3;
 			laneSection.add(laneAmplitudeLabel, c);
 			c.gridwidth = 3;
 			c.gridx = 1;
 			c.gridy = 3;
 			laneSection.add(laneAmplitude, c);
 
 
 
 
 			this.add(title);
 			this.add(feederSection);
 			this.add(laneSection);
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent ae) {
 			// TODO Auto-generated method stub
 			if(ae.getSource() == laneOn){
 				int lanenum = (int)laneSelect.getSelectedItem();
 				String set = "lm fcsa set lanepower " + lanenum;
 				try {
 					laneManager.sendCommand(set);
 				} catch (Exception e) {
 					System.out.println("An error occurred trying to send message to power on lane " + lanenum + ".");
 				} 
 			}else if (ae.getSource() == laneOff){
 				int lanenum = (int)laneSelect.getSelectedItem();
 				String set = "lm fcsa set lanepower " + lanenum;
 				try {
 					laneManager.sendCommand(set);
 				} catch (Exception e) {
 					System.out.println("An error occurred trying to send message to power off lane " + lanenum + ".");
 				} 
 			}else if (ae.getSource() == feederOn){
 				int feedernum = (int)feederSelect.getSelectedItem();
 				String set = "lm fcsa set feederpower " + feedernum;
 				try {
 					laneManager.sendCommand(set);
 				} catch (Exception e) {
 					System.out.println("An error occurred trying to send message to power on feeder " + feedernum + ".");
 				} 
 			}else if (ae.getSource() == feederOff){
 				int feedernum = (int)feederSelect.getSelectedItem();
 				String set = "lm fcsa set feederpower " + feedernum;
 				try {
 					laneManager.sendCommand(set);
 				} catch (Exception e) {
 					System.out.println("An error occurred trying to send message to power off feeder " + feedernum + ".");
 				} 
 			}else{  // updates speed and amplitude when JComboBox changes
 				JComboBox cb = (JComboBox)ae.getSource();
 				System.out.println("Get");
 				if(cb == laneSelect){
 					System.out.println(laneManager.getLaneSpeed((Integer)cb.getSelectedItem()));
 					laneSpeed.setValue(laneManager.getLaneSpeed((Integer)cb.getSelectedItem()));
 					laneAmplitude.setValue(laneManager.getLaneAmplitude((Integer)cb.getSelectedItem()));
 				}
 			}
 
 		}
 
 		public class SliderDetection implements ChangeListener{
 
 			@Override
 			public void stateChanged(ChangeEvent ce) {
 				// TODO Auto-generated method stub
 				JSlider source = (JSlider) ce.getSource();
 				if(source == laneSpeed){
 					if (!source.getValueIsAdjusting()) {
 						int speed = (int)source.getValue();
 						// send speed to server
 						int lanenum = (int)laneSelect.getSelectedItem();
 						String set = "lm fcsa set lanespeed " + lanenum + " " + speed;
 						try {
 							laneManager.sendCommand(set);
 						} catch (Exception e) {
 							System.out.println("An error occurred trying to send message to change lanespeed.");
 						} 
 						System.out.println("lane speed : " + speed);
 					}
 				}else if(source == laneAmplitude){
 					if (!source.getValueIsAdjusting()) {
 						int amplitude = (int)source.getValue();
 						// send amplitude to server
 						int lanenum = (int)laneSelect.getSelectedItem();
 						String set = "lm fcsa set laneamplitude " + lanenum + " " + amplitude;
 						try {
 							laneManager.sendCommand(set);
 						} catch (Exception e) {
 							System.out.println("An error occurred trying to send message to change lane amplitude.");
 						} 
 						System.out.println("lane amplitude : " + amplitude);
 					}
 				}
 			}
 
 		}
 
 	}
 
 	public class LaneNonNormPanel extends JPanel implements ActionListener {
 		JComboBox laneBoxList;
		JComboBox feederBoxList;
 		JPanel partsMissingContainer;
 		JPanel partsBadContainer;
 		JButton laneJamButton;
 		JButton diverterButton;
 		JButton badPartsButton;
 		JButton blockingRobotButton;
 		JTextArea messageBox;
 
 		public LaneNonNormPanel() {
 
 			laneJamButton = new JButton("Lane Jam");
 			diverterButton = new JButton("Diverter Too Slow");
 			badPartsButton = new JButton("Bad Parts in Nest");
 			blockingRobotButton = new JButton("Robot Blocking Camera");
 			messageBox = new JTextArea("Actions...\n");
 
 			laneJamButton.addActionListener(this);
 			diverterButton.addActionListener(this);
 			badPartsButton.addActionListener(this);
 			blockingRobotButton.addActionListener(this);
 
 			laneJamButton.setPreferredSize(new Dimension(200,25));
 			diverterButton.setPreferredSize(new Dimension(200,25));
 			badPartsButton.setPreferredSize(new Dimension(200,25));
 			blockingRobotButton.setPreferredSize(new Dimension(200,25));
 
 			setLayout(new FlowLayout());
 			Box boxContainer = Box.createVerticalBox();
 			laneBoxList = new JComboBox();
 			for (int i = 1; i < 9;i++) {
 				laneBoxList.addItem("Lane "+i);
 			}
 			laneBoxList.setSelectedIndex(0);
			feederBoxList = new JComboBox();
			for (int i = 1; i < 5; i++) {
				feederBoxList.addItem("Feeder "+i);
			}
 			partsMissingContainer = new JPanel();
 			partsBadContainer = new JPanel();
 
 
 			partsMissingContainer.setPreferredSize(new Dimension(250,180));
 			partsBadContainer.setPreferredSize(new Dimension(250,180));
 
 			TitledBorder title = BorderFactory.createTitledBorder("Missing Parts in Nest");
 			partsMissingContainer.setBorder(title);	
 
 			title = BorderFactory.createTitledBorder("No Good Parts in Nest");
 			partsBadContainer.setBorder(title);
 
 			partsMissingContainer.add(laneJamButton);
 			partsMissingContainer.add(diverterButton);
 
 			partsBadContainer.add(badPartsButton);
 			partsBadContainer.add(blockingRobotButton);
 			boxContainer.add(Box.createRigidArea(new Dimension(0,30)));
 			JLabel label = new JLabel("Non-Normative Cases");
 			label.setAlignmentX(Component.CENTER_ALIGNMENT);
 			boxContainer.add(label);
 			boxContainer.add(Box.createRigidArea(new Dimension(0,30)));
 			boxContainer.add(laneBoxList);
 			boxContainer.add(Box.createRigidArea(new Dimension(0,30)));
			boxContainer.add(feederBoxList);
			boxContainer.add(Box.createRigidArea(new Dimension(0,30)));
 			boxContainer.add(partsMissingContainer);
 			boxContainer.add(Box.createRigidArea(new Dimension(0,30)));
 			boxContainer.add(partsBadContainer);
 			boxContainer.add(Box.createRigidArea(new Dimension(0,10)));
 			JScrollPane scrollPane = new JScrollPane(messageBox);
 			scrollPane.setPreferredSize(new Dimension(200,100));
 			scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 			scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 			boxContainer.add(scrollPane);
 			add(boxContainer);
 		}
 
 		public void actionPerformed(ActionEvent ae) {
 			if (ae.getSource() == laneJamButton) {
 				messageBox.append("Lane jam initated in " + laneBoxList.getSelectedItem() + ".\n");
 				int lanenum = laneBoxList.getSelectedIndex();
 				lanenum = lanenum+1;
 				String set = "lm fcsa lanejam " + lanenum;
 				try {
 					laneManager.sendCommand(set);
 				} catch (Exception e) {
 					System.out.println("An error occurred trying to initiate non-normative case: lane jam.");
 				} 
 			} else if (ae.getSource() == diverterButton) {
 				messageBox.append("Diverter was too slow switching to " + laneBoxList.getSelectedItem() + ".\n");
 				int feedernum = feederBoxList.getSelectedIndex();
 				feedernum = feedernum+1;
 				String set = "lm fcsa slowdiverter " + feedernum;
 				try {
 					laneManager.sendCommand(set);
 				} catch (Exception e) {
 					System.out.println("An error occurred trying initiate non-normative case: slow diverter change.");
 				} 
 			} else if (ae.getSource() == badPartsButton) {
 				messageBox.append("Bad parts found in " + laneBoxList.getSelectedItem() + "'s nest.\n");
 				int lanenum = laneBoxList.getSelectedIndex();
 				lanenum = lanenum+1;
 				String set = "lm fcsa badparts " + lanenum;
 				try {
 					laneManager.sendCommand(set);
 				} catch (Exception e) {
 					System.out.println("An error occurred trying initiate non-normative case: bad parts in nest.");
 				} 
 			} else if (ae.getSource() == blockingRobotButton) {
 				messageBox.append("A robot is blocking the camera at " + laneBoxList.getSelectedItem() + "'s nest.\n");
 				int lanenum = laneBoxList.getSelectedIndex();
 				lanenum = lanenum+1;
 				String set = "lm fcsa blockingrobot " + lanenum;
 				try {
 					laneManager.sendCommand(set);
 				} catch (Exception e) {
 					System.out.println("An error occurred trying to initiate non-normative case: robot blocking camera.");
 				} 
 			}
 
 		}
 
 
 	}	
 }
