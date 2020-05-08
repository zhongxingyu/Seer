 package gui.panels.nonnormpanels;
 
 import gui.components.GUITruck;
 import gui.panels.ControlPanel;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 
 import javax.swing.AbstractButton;
 import javax.swing.BoxLayout;
 import javax.swing.ButtonModel;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import transducer.*;
 
 public class NonNormPanel extends JPanel implements ActionListener {
 
 	ControlPanel parent;
 
 	Transducer transducer;
 
 	JComboBox nonNormSelector;
 
 	JPanel bottomPanel = new JPanel();
 
 	GUITruck truck;
 
 	public NonNormPanel(ControlPanel c, Transducer t) {
 		parent = c;
 		transducer = t;
 
 		this.setBackground(new Color(238, 238, 238));
 
 		nonNormSelector = new JComboBox();
 
 		nonNormSelector.addItem("Select Non-Norm: ");
 		nonNormSelector.addItem("Stop/Start Conveyor (0-14)");
 		nonNormSelector.addItem("Inline Workstation Break/Restart");
 		nonNormSelector.addItem("Workstation Broken/Fixed");
 		nonNormSelector.addItem("Popup Jam/Unjam");
 		nonNormSelector.addItem("Offline Workstation Change Speed");
 		nonNormSelector.addItem("Disable/Enable Offline Workstation");
 		nonNormSelector.addItem("Truck Breaks Down/Fixed");
 		nonNormSelector.addItem("Glass Breaking Offline");
 
 		setLayout(new GridBagLayout());
 		GridBagConstraints gbc = new GridBagConstraints();
 
 		gbc.anchor = GridBagConstraints.NORTHWEST;
 		gbc.ipadx = 400;
 		gbc.gridx = 0;
 		gbc.gridy = 0;
 		gbc.weightx = 1;
 		gbc.weighty = 1;
 		add(nonNormSelector, gbc);
 		gbc.gridy++;
 		add(bottomPanel, gbc);
 		bottomPanel.setBackground(new Color(238, 238, 238));
 
 		nonNormActionListener();
 	}
 
 	public void nonNormActionListener() {
 		nonNormSelector.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent ae) {
 
 				JComboBox option = (JComboBox) ae.getSource();
 
 				String selected = (String) option.getSelectedItem();
 
 				if (selected.equals("Stop/Start Conveyor (0-14)")) {
 					bottomPanel.removeAll();
 					bottomPanel.setLayout(new GridBagLayout());
 					GridBagConstraints gbc = new GridBagConstraints();
 
 					final JSlider conveyorSlider = new JSlider(0, 14);
 					final JButton turnOffConveyor = new JButton("Turn off");
 					final JButton turnOnConveyor = new JButton("Turn on");
 					turnOffConveyor.addActionListener(new ActionListener() {
 						public void actionPerformed(ActionEvent ae) {
 							turnOffConveyor(conveyorSlider.getValue());
 							bottomPanel.remove(turnOffConveyor);
 							bottomPanel.add(turnOnConveyor);
 							repaint();
 							revalidate();
 						}
 
 					});
 
 					turnOnConveyor.addActionListener(new ActionListener() {
 						public void actionPerformed(ActionEvent ae) {
 							turnOnConveyor(conveyorSlider.getValue());
 							bottomPanel.remove(turnOnConveyor);
 							bottomPanel.add(turnOffConveyor);
 							repaint();
 							revalidate();
 						}
 
 					});
 
 					Hashtable<Integer, JLabel> table = new Hashtable<Integer, JLabel>();
 					table.put(0, new JLabel("0"));
 					table.put(1, new JLabel("1"));
 					table.put(2, new JLabel("2"));
 					table.put(3, new JLabel("3"));
 					table.put(4, new JLabel("4"));
 					table.put(5, new JLabel("5"));
 					table.put(6, new JLabel("6"));
 					table.put(7, new JLabel("7"));
 					table.put(8, new JLabel("8"));
 					table.put(9, new JLabel("9"));
 					table.put(10, new JLabel("10"));
 					table.put(11, new JLabel("11"));
 					table.put(12, new JLabel("12"));
 					table.put(13, new JLabel("13"));
 					table.put(14, new JLabel("14"));
 
 					conveyorSlider.setLabelTable(table);
 					conveyorSlider.setMinorTickSpacing(1);
 					conveyorSlider.setPaintLabels(true);
 					conveyorSlider.setPaintTicks(true);
 					conveyorSlider.setSnapToTicks(true);
 
 					gbc.anchor = GridBagConstraints.NORTHWEST;
 					gbc.weightx = 1;
 					gbc.weighty = 5;
 					gbc.gridx = 0;
 					gbc.gridy = 0;
 					bottomPanel.add(new JLabel("Valid Conveyor Numbers: 0-14"));
 					gbc.gridy = 1;
 					gbc.fill = 2;
 					bottomPanel.add(conveyorSlider, gbc);
 					gbc.ipadx = 5;
 					gbc.gridx++;
 					gbc.gridy = 0;
 					bottomPanel.add(turnOffConveyor);
 					repaint();
 					revalidate();
 
 				} else if (selected.equals("Inline Workstation Break/Restart")) {
 					bottomPanel.removeAll();
 					bottomPanel.setLayout(new GridBagLayout());
 					GridBagConstraints gbc = new GridBagConstraints();
 
 					final List<JCheckBox> workstation = new ArrayList<JCheckBox>();
 					
 					for(int i = 0; i < 7; i++) {
 						JCheckBox temp = new JCheckBox();
 						temp.addActionListener(new ActionListener() {
 							public void actionPerformed(ActionEvent ae) {
 								AbstractButton ab = (AbstractButton) ae.getSource();
 								//0,2,3,8,10,11,13
 								int index;
 								for(int i = 0; i < 7; i++) {
 									if(ab == (AbstractButton) workstation.get(i)) {
 										int j = 0;
 										if(i == 0) j = 0;
 										else if(i == 1) j = 2;
 										else if(i == 2) j = 3;
 										else if(i == 3) j = 8;
 										else if(i == 4) j = 10;
 										else if(i == 5) j = 11;
 										else if(i == 6) j = 13;
 										
 										if(ab.getModel().isSelected()) {
 											breakWorkstation(j);
 											System.out.println("breaking " + j);
 										}
 										else {
 											fixWorkstation(j);
 											System.out.println("fixing " + j);
 										}
 									}
 								}							
 							}
 						});
 						
 						workstation.add(temp);
 					}
 					
 					
 					
 					JPanel panel0 = new JPanel();
					panel0.setBackground(new Color(238,238,238));
 					panel0.add(new JLabel("0"));
 					panel0.add(workstation.get(0));
 					
 					JPanel panel1 = new JPanel();
					panel1.setBackground(new Color(238,238,238));
 					panel1.add(new JLabel("2"));
 					panel1.add(workstation.get(1));
 					
 					JPanel panel2 = new JPanel();
					panel2.setBackground(new Color(238,238,238));
 					panel2.add(new JLabel("3"));
 					panel2.add(workstation.get(2));
 					
 					JPanel panel3 = new JPanel();
					panel3.setBackground(new Color(238,238,238));
 					panel3.add(new JLabel("8"));
 					panel3.add(workstation.get(3));
 					
 					JPanel panel4 = new JPanel();
					panel4.setBackground(new Color(238,238,238));
 					panel4.add(new JLabel("10"));
 					panel4.add(workstation.get(4));
 					
 					JPanel panel5 = new JPanel();
					panel5.setBackground(new Color(238,238,238));
 					panel5.add(new JLabel("11"));
 					panel5.add(workstation.get(5));
 					
 					JPanel panel6 = new JPanel();
					panel6.setBackground(new Color(238,238,238));
 					panel6.add(new JLabel("13"));
 					panel6.add(workstation.get(6));
 				
 					gbc.weighty = 2;
 					gbc.gridx = 0;
 					gbc.gridy = 0;
 					gbc.gridwidth = 4;
 					bottomPanel.add(new JLabel("Check Box to Disable. Uncheck to Fix"), gbc);
 					gbc.ipadx = 5;
 					gbc.gridwidth = 4;
 					gbc.gridy++;
 					bottomPanel.add(new JLabel("Workstations"),gbc);
 					
 					JPanel checkBoxPanel = new JPanel();
 					checkBoxPanel.setBackground(new Color(238,238,238));
 					checkBoxPanel.setLayout(new GridBagLayout());
 					GridBagConstraints gbc2 = new GridBagConstraints();
 
 					gbc2.gridx = 0;
 					gbc2.gridy = 0;
 					checkBoxPanel.add(panel0, gbc2);
 					gbc2.gridy++;
 					checkBoxPanel.add(panel1, gbc2);
 					gbc2.gridy++;
 					checkBoxPanel.add(panel2, gbc2);
 					gbc2.gridy++;
 					checkBoxPanel.add(panel3, gbc2);
 					gbc2.gridy=0;
 					gbc2.gridx++;
 					checkBoxPanel.add(panel4, gbc2);
 					gbc2.gridy++;
 					checkBoxPanel.add(panel5, gbc2);
 					gbc2.gridy++;
 					checkBoxPanel.add(panel6, gbc2);
 					
 					gbc.gridy++;
 					bottomPanel.add(checkBoxPanel, gbc);
 					
 					repaint();
 					revalidate();
 				} else if (selected.equals("Workstation Broken/Fixed")) {
 					bottomPanel.removeAll();
 					bottomPanel.setLayout(new GridBagLayout());
 					GridBagConstraints gbc = new GridBagConstraints();
 
 					JSlider conveyorSlider = new JSlider(0, 6);
 					JButton accept = new JButton("Accept");
 					accept.addActionListener(new ActionListener() {
 						public void actionPerformed(ActionEvent ae) {
 
 						}
 					});
 
 					Hashtable<Integer, JLabel> table = new Hashtable<Integer, JLabel>();
 					table.put(0, new JLabel("0"));
 					table.put(1, new JLabel("2"));
 					table.put(2, new JLabel("3"));
 					table.put(3, new JLabel("8"));
 					table.put(4, new JLabel("10"));
 					table.put(5, new JLabel("11"));
 					table.put(6, new JLabel("13"));
 
 					conveyorSlider.setLabelTable(table);
 					conveyorSlider.setMajorTickSpacing(1);
 					conveyorSlider.setPaintLabels(true);
 					conveyorSlider.setSnapToTicks(true);
 					conveyorSlider.setPaintTicks(true);
 
 					gbc.anchor = GridBagConstraints.NORTHWEST;
 					gbc.weightx = 0;
 					gbc.weighty = 5;
 					gbc.gridx = 0;
 					gbc.gridy = 0;
 					bottomPanel.add(new JLabel(
 							"Valid Workstation Conveyor Numbers:"));
 					gbc.gridy++;
 					bottomPanel.add(conveyorSlider, gbc);
 					gbc.ipadx = 5;
 					gbc.gridx++;
 					bottomPanel.add(accept, gbc);
 
 					repaint();
 					revalidate();
 				} else if (selected.equals("Popup Jam/Unjam")) {
 					bottomPanel.removeAll();
 					bottomPanel.setLayout(new GridBagLayout());
 					GridBagConstraints gbc = new GridBagConstraints();
 
 					final JSlider conveyorSlider = new JSlider(0, 2);
 					JButton jamPopup = new JButton("jam");
 					JButton unjamPopup = new JButton("unjam");
 
 					jamPopup.addActionListener(new ActionListener() {
 						public void actionPerformed(ActionEvent ae) {
 							jamPopup(conveyorSlider.getValue());
 						}
 					});
 					unjamPopup.addActionListener(new ActionListener() {
 						public void actionPerformed(ActionEvent ae) {
 							unJamPopup(conveyorSlider.getValue());
 						}
 					});
 
 					Hashtable<Integer, JLabel> table = new Hashtable<Integer, JLabel>();
 					table.put(0, new JLabel("0"));
 					table.put(1, new JLabel("1"));
 					table.put(2, new JLabel("2"));
 
 					conveyorSlider.setLabelTable(table);
 					conveyorSlider.setMajorTickSpacing(1);
 					conveyorSlider.setPaintLabels(true);
 					conveyorSlider.setSnapToTicks(true);
 					conveyorSlider.setPaintTicks(true);
 
 					gbc.anchor = GridBagConstraints.NORTHWEST;
 					gbc.weightx = 0;
 					gbc.weighty = 5;
 					gbc.gridx = 0;
 					gbc.gridy = 0;
 					bottomPanel.add(new JLabel("Valid Popup Index:"));
 					gbc.gridy++;
 					bottomPanel.add(conveyorSlider, gbc);
 					gbc.ipadx = 5;
 					gbc.gridx++;
 					bottomPanel.add(jamPopup, gbc);
 					gbc.gridy++;
 					bottomPanel.add(unjamPopup, gbc);
 					repaint();
 					revalidate();
 				} else if (selected.equals("Offline Workstation Change Speed")) {
 					/**
 					 * The lowest value of the speed slider (corresponds to slow
 					 * factory)
 					 */
 
 					bottomPanel.removeAll();
 					bottomPanel.setLayout(new GridBagLayout());
 					GridBagConstraints gbc = new GridBagConstraints();
 
 					final JSlider speedSlider = new JSlider(1, 10, 10);
 					// setup sliders
 					speedSlider.addChangeListener(new ChangeListener() {
 						public void stateChanged(ChangeEvent arg0) {
 							int newSpeed = speedSlider.getValue();
 							// get to the timer, set new speed
 							try {
 								Object[] args = new Object[1];
 								args[0] = (Integer) speedSlider.getValue();
 								transducer
 										.fireEvent(
 												TChannel.DRILL,
 												TEvent.WORKSTATION_OFFLINE_CHANGE_SPEED,
 												args);
 								transducer
 										.fireEvent(
 												TChannel.CROSS_SEAMER,
 												TEvent.WORKSTATION_OFFLINE_CHANGE_SPEED,
 												args);
 								transducer
 										.fireEvent(
 												TChannel.GRINDER,
 												TEvent.WORKSTATION_OFFLINE_CHANGE_SPEED,
 												args);
 
 							} catch (NullPointerException npe) {
 								System.out.println("No Timer connected!");
 							}
 							speedSlider.setToolTipText("" + newSpeed);
 
 						}
 					});
 					speedSlider.setSnapToTicks(false);
 					speedSlider.setPreferredSize(new Dimension(200, 20));
 					speedSlider.setBackground(Color.black);
 
 					Hashtable<Integer, JLabel> table = new Hashtable<Integer, JLabel>();
 					table.put(0, new JLabel("0"));
 					table.put(1, new JLabel("1"));
 					table.put(2, new JLabel("2"));
 
 					gbc.anchor = GridBagConstraints.NORTHWEST;
 					gbc.weightx = 0;
 					gbc.weighty = 5;
 					gbc.gridx = 0;
 					gbc.gridy = 0;
 					bottomPanel.add(new JLabel("Valid Popup Index"));
 					gbc.gridy++;
 					bottomPanel.add(speedSlider, gbc);
 					gbc.ipadx = 5;
 					gbc.gridx++;
 
 					repaint();
 					revalidate();
 				} else if (selected
 						.equals("Disable/Enable Offline Workstation")) {
 					bottomPanel.removeAll();
 					bottomPanel.setLayout(new GridBagLayout());
 					GridBagConstraints gbc = new GridBagConstraints();
 
 					final JSlider conveyorSlider = new JSlider(0, 5);
 					JButton disable = new JButton("Disable");
 					JButton enable = new JButton("Enable");
 
 					Hashtable<Integer, JLabel> table = new Hashtable<Integer, JLabel>();
 					table.put(0, new JLabel("0"));
 					table.put(1, new JLabel("1"));
 					table.put(2, new JLabel("2"));
 					table.put(3, new JLabel("3"));
 					table.put(4, new JLabel("4"));
 					table.put(5, new JLabel("5"));
 
 					disable.addActionListener(new ActionListener() {
 						public void actionPerformed(ActionEvent ae) {
 							disableWorkStation(conveyorSlider.getValue());
 						}
 					});
 					enable.addActionListener(new ActionListener() {
 						public void actionPerformed(ActionEvent ae) {
 							enableWorkStation(conveyorSlider.getValue());
 						}
 					});
 					conveyorSlider.setLabelTable(table);
 					conveyorSlider.setMajorTickSpacing(1);
 					conveyorSlider.setPaintLabels(true);
 					conveyorSlider.setSnapToTicks(true);
 					conveyorSlider.setPaintTicks(true);
 
 					gbc.anchor = GridBagConstraints.NORTHWEST;
 					gbc.weightx = 0;
 					gbc.weighty = 5;
 					gbc.gridx = 0;
 					gbc.gridy = 0;
 					bottomPanel.add(new JLabel(
 							"Valid Offline Workstation Index Index:"));
 					gbc.gridy++;
 					bottomPanel.add(conveyorSlider, gbc);
 					gbc.ipadx = 5;
 					gbc.gridx++;
 					bottomPanel.add(disable, gbc);
 					gbc.gridy++;
 					bottomPanel.add(enable, gbc);
 
 					repaint();
 					revalidate();
 				} else if (selected.equals("Truck Breaks Down/Fixed")) {
 					bottomPanel.removeAll();
 					bottomPanel.setLayout(new GridBagLayout());
 					GridBagConstraints gbc = new GridBagConstraints();
 					final JButton breakButton = new JButton("Break Truck");
 					final JButton fixButton = new JButton("Fix Truck");
 					bottomPanel.add(breakButton);
 					fixButton.addActionListener(new ActionListener() {
 						public void actionPerformed(ActionEvent ae) {
 							truck.fixTruck();
 							bottomPanel.remove(fixButton);
 							bottomPanel.add(breakButton);
 							repaint();
 							revalidate();
 						}
 					});
 					breakButton.addActionListener(new ActionListener() {
 						public void actionPerformed(ActionEvent ae) {
 							truck.breakTruck();
 							bottomPanel.remove(breakButton);
 							bottomPanel.add(fixButton);
 							repaint();
 							revalidate();
 						}
 					});
 					repaint();
 					revalidate();
 				} else if (selected.equals("Glass Breaking Offline")) {
 					bottomPanel.removeAll();
 					bottomPanel.setLayout(new GridBagLayout());
 					GridBagConstraints gbc = new GridBagConstraints();
 
 					JSlider conveyorSlider = new JSlider(0, 5);
 					JButton accept = new JButton("Accept");
 					accept.addActionListener(new ActionListener() {
 						public void actionPerformed(ActionEvent ae) {
 
 						}
 					});
 
 					Hashtable<Integer, JLabel> table = new Hashtable<Integer, JLabel>();
 					table.put(0, new JLabel("0"));
 					table.put(1, new JLabel("1"));
 					table.put(2, new JLabel("2"));
 					table.put(3, new JLabel("3"));
 					table.put(4, new JLabel("4"));
 					table.put(5, new JLabel("5"));
 
 					conveyorSlider.setLabelTable(table);
 					conveyorSlider.setMajorTickSpacing(1);
 					conveyorSlider.setPaintLabels(true);
 					conveyorSlider.setSnapToTicks(true);
 					conveyorSlider.setPaintTicks(true);
 
 					gbc.anchor = GridBagConstraints.NORTHWEST;
 					gbc.weightx = 0;
 					gbc.weighty = 5;
 					gbc.gridx = 0;
 					gbc.gridy = 0;
 					bottomPanel.add(new JLabel(
 							"Valid Offline Workstation Index:"));
 					gbc.gridy++;
 					bottomPanel.add(conveyorSlider, gbc);
 					gbc.ipadx = 5;
 					gbc.gridx++;
 					bottomPanel.add(accept, gbc);
 
 					repaint();
 					revalidate();
 				}
 
 			}
 		});
 	}
 
 	public void turnOffConveyor(int index) {
 		Integer[] newArgs = new Integer[1];
 		newArgs[0] = (Integer) (index);
 		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP,
 				newArgs);
 	}
 
 	public void turnOnConveyor(int index) {
 		Integer[] newArgs = new Integer[1];
 		newArgs[0] = (Integer) (index);
 		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START,
 				newArgs);
 	}
 
 	public void breakWorkstation(int index) {
 		if (index == 0) {
 			transducer.fireEvent(TChannel.CUTTER,
 					TEvent.WORKSTATION_DISABLE_ONLINE, null);
 		} else if (index == 2) {
 			transducer.fireEvent(TChannel.BREAKOUT,
 					TEvent.WORKSTATION_DISABLE_ONLINE, null);
 		} else if (index == 3) {
 			transducer.fireEvent(TChannel.MANUAL_BREAKOUT,
 					TEvent.WORKSTATION_DISABLE_ONLINE, null);
 		} else if (index == 8) {
 			transducer.fireEvent(TChannel.WASHER,
 					TEvent.WORKSTATION_DISABLE_ONLINE, null);
 		} else if (index == 10) {
 			transducer.fireEvent(TChannel.PAINTER,
 					TEvent.WORKSTATION_DISABLE_ONLINE, null);
 		} else if (index == 11) {
 			transducer.fireEvent(TChannel.UV_LAMP,
 					TEvent.WORKSTATION_DISABLE_ONLINE, null);
 		} else if (index == 13) {
 			transducer.fireEvent(TChannel.OVEN,
 					TEvent.WORKSTATION_DISABLE_ONLINE, null);
 		} else {
 			System.err
 					.println("Passing invalid index in breakWorkstation(int index)");
 		}
 	}
 
 	public void fixWorkstation(int index) {
 		if (index == 0) {
 			transducer.fireEvent(TChannel.CUTTER,
 					TEvent.WORKSTATION_ENABLE_ONLINE, null);
 		} else if (index == 2) {
 			transducer.fireEvent(TChannel.BREAKOUT,
 					TEvent.WORKSTATION_ENABLE_ONLINE, null);
 		} else if (index == 3) {
 			transducer.fireEvent(TChannel.MANUAL_BREAKOUT,
 					TEvent.WORKSTATION_ENABLE_ONLINE, null);
 		} else if (index == 8) {
 			transducer.fireEvent(TChannel.WASHER,
 					TEvent.WORKSTATION_ENABLE_ONLINE, null);
 		} else if (index == 10) {
 			transducer.fireEvent(TChannel.PAINTER,
 					TEvent.WORKSTATION_ENABLE_ONLINE, null);
 		} else if (index == 11) {
 			transducer.fireEvent(TChannel.UV_LAMP,
 					TEvent.WORKSTATION_ENABLE_ONLINE, null);
 		} else if (index == 13) {
 			transducer.fireEvent(TChannel.OVEN,
 					TEvent.WORKSTATION_ENABLE_ONLINE, null);
 		} else {
 			System.err
 					.println("Passing invalid index in fixWorkstation(int index)");
 		}
 	}
 
 	public void jamPopup(int index) {
 		Integer[] newArgs = new Integer[1];
 		newArgs[0] = (Integer) (index);
 
 		transducer.fireEvent(TChannel.POPUP, TEvent.POPUP_JAM, newArgs);
 	}
 
 	public void unJamPopup(int index) {
 		Integer[] newArgs = new Integer[1];
 		newArgs[0] = (Integer) (index);
 		transducer.fireEvent(TChannel.POPUP, TEvent.POPUP_UNJAM, newArgs);
 	}
 
 	public void disableWorkStation(int index) {
 		Integer[] newArgs = new Integer[1];
 		newArgs[0] = (Integer) (index);
 		transducer.fireEvent(TChannel.POPUP,
 				TEvent.WORKSTATION_DISABLE_OFFLINE, newArgs);
 	}
 
 	public void enableWorkStation(int index) {
 		Integer[] newArgs = new Integer[1];
 		newArgs[0] = (Integer) (index);
 		transducer.fireEvent(TChannel.POPUP, TEvent.WORKSTATION_ENABLE_OFFLINE,
 				newArgs);
 	}
 
 	public void setTransducer(Transducer newTransducer) {
 		this.transducer = newTransducer;
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent ae) {
 		// TODO Auto-generated method stub
 
 		JComboBox option = (JComboBox) ae.getSource();
 		String selected = (String) option.getSelectedItem();
 
 		if (selected.equals("Stop/Start Conveyor (0-14)")) {
 
 		} else if (selected.equals("Inline Workstation Break/Restart")) {
 
 		}
 		// TODO- FINISH
 	}
 
 	public void setTruck(GUITruck truck) {
 		// TODO Auto-generated method stub
 		this.truck = truck;
 	}
 
 }
