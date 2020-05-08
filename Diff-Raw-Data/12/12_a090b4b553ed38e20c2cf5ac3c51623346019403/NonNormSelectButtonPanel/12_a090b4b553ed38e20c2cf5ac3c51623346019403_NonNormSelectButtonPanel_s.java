 package gui.panels.subcontrolpanels;
 
 import gui.panels.ControlPanel;
 import gui.panels.FactoryPanel;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.JSpinner;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import shared.enums.NonNormTarget;
 
 public class NonNormSelectButtonPanel extends JPanel {
 	
 	private ControlPanel controlPanel;
 	private int maxNum;
 	private NonNormTarget name;
 	
 	private JButton breakButton, unbreakButton;
 	private JSpinner spinner;
 	private boolean[] breakState;
 		
 	public NonNormSelectButtonPanel (NonNormTarget name, int n, ControlPanel cp) {
 		super(new GridBagLayout());
 		this.name = name;
 		this.maxNum = n;
 		this.controlPanel = cp;
 		
 		this.setBackground(Color.white);
 		this.setForeground(Color.black);
 		this.setFont(new Font("SansSerif", Font.BOLD, 14));
 		this.setOpaque(true);
 		this.setSize(15, 30);
 		this.setMinimumSize(new Dimension(15, 40));
 		this.setMaximumSize(new Dimension(15, 40));
 		this.setPreferredSize(new Dimension(15, 40));
 		
 		breakState = new boolean[maxNum];
 		for (int i = 0; i < maxNum; i++) {
 			breakState[i] = true;
 		}
 		
 		spinner = new JSpinner(new SpinnerNumberModel(1, 1, maxNum , 1));
 		spinner.addChangeListener(new SpinnerListener());
 		
 		breakButton = new JButton("Break " + name);
 		breakButton.setSize(new Dimension(7, 20));
 		breakButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
 		breakButton.setAlignmentX(Component.LEFT_ALIGNMENT);
 		breakButton.addActionListener((new BreakListener()));
 		
 		GridBagConstraints c = new GridBagConstraints();
 		c.gridx = 0;
 		c.gridy = 0;
 		c.gridwidth = 3;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.anchor = GridBagConstraints.LINE_START;
 		this.add(breakButton, c);
 		
 		c = new GridBagConstraints();
 		c.gridx = 3;
 		c.gridy = 0;
 		c.gridheight = 2;
 		c.fill = GridBagConstraints.VERTICAL;
 		c.anchor = GridBagConstraints.LINE_END;
 		this.add(spinner, c);
 		
 		this.validate();		
 	}
 
 	protected class BreakListener implements ActionListener {
 		/**
 		 * Invoked whenever the button is pressed
 		 */
 		public void actionPerformed(ActionEvent ae) {
 			FactoryPanel factoryPanel = controlPanel.getGuiParent();
 			int id = ((Integer) spinner.getValue()) - 1;
 			switch(name) {
 				case CONVEYOR:
 					factoryPanel.breakConveyor(breakState[id], id);
 					break;
 				case POPUP:
 					factoryPanel.breakPopup(breakState[id], id);
 					break;
 				case OFFLINE:
 					factoryPanel.breakOfflineWorkstation(breakState[id], id);
 					break;
 				case ONLINE:
 					factoryPanel.breakOnlineWorkstation(breakState[id], id);
 					break;
 				case TRUCK:
 					factoryPanel.breakTruck(breakState[id]);
 					break;
 				case SENSOR:
 					factoryPanel.breakSensor(breakState[id], id);
 					break;
 				case GLASS:
 					factoryPanel.breakGlass(breakState[id], id);
 					break;
 				case TEST:
 					// test
 					break;
 			}
 			
 			switchButtonState(id);
 		}
 		
 		private void switchButtonState(int id) {
 			if (breakState[id]) {
 				breakState[id] = false;
 				breakButton.setText("Unbreak " + name);
 			} else {
 				breakState[id] = true;
 				breakButton.setText("Break " + name);
 			}
 		}
 	}
 	
 	protected class SpinnerListener implements ChangeListener {
 
 		@Override
 		public void stateChanged(ChangeEvent e) {
 			JSpinner spinner = (JSpinner) e.getSource();
		    int value = (int) spinner.getValue();
 		    displayButtonState(value - 1);
 		}
 		
 		private void displayButtonState(int id) {
 			if(breakState[id]) {
 				breakButton.setText("Break " + name);
 			} else {
 				breakButton.setText("Unbreak " + name);
 			}
 		}
 		
 	}
 }
