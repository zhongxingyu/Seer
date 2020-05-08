 package sk.sochuliak.barabasi.gui.newnetworkdialog;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.FlowLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JTextField;
 
 import sk.sochuliak.barabasi.controllers.ControllerService;
 import sk.sochuliak.barabasi.gui.MainGuiConfiguration;
 import sk.sochuliak.barabasi.gui.Strings;
 import sk.sochuliak.barabasi.network.EdgeConnectingMethodRowConfig;
 import sk.sochuliak.barabasi.network.NetworkBuildConfiguration;
 import sk.sochuliak.barabasi.utils.CommonUtils;
 import sk.sochuliak.barabasi.utils.GuiUtils;
 
 public class NewNetworkDialog extends JDialog {
 
 	private static final long serialVersionUID = 1L;
 	
 	private Component owner;
 	
 	private String[] predefinedGrowthMethods = new String[] {
 			Strings.NEW_NETWORK_DIALOG_GROWTH_MANAGEMENT_NODE_DEGREE,
 			Strings.NEW_NETWORK_DIALOG_GROWTH_MANAGEMENT_NODE_CLASTER,
 			Strings.NEW_NETWORK_DIALOG_GROWTH_MANAGEMENT_RANDOM	
 	};
 	
 	private String[] firstEdgeConnectingMethod = new String[] {
 			Strings.NEW_NETWORK_DIALOG_GROWTH_MANAGEMENT_NODE_DEGREE,
 			Strings.NEW_NETWORK_DIALOG_GROWTH_MANAGEMENT_NODE_CLASTER,
 			Strings.NEW_NETWORK_DIALOG_GROWTH_MANAGEMENT_RANDOM	
 	};
 	
 	private ButtonGroup connectingMethodRadioGroup = new ButtonGroup();
 	private JRadioButton predefinedRadioButton = new JRadioButton(Strings.NEW_NETWORK_DIALOG_PREDEFINED, true);
 	private JRadioButton ownRadioButton = new JRadioButton(Strings.NEW_NETWORK_DIALOG_OWN, false);
 	
 	private JTextField networkNameTextField = new JTextField();
 	
 	private JTextField numberOfNodesTextField = new JTextField();
 	
 	private JComboBox<String> predefinedGrowthMethodComboBox = new JComboBox<String>(this.predefinedGrowthMethods);
 	
 	private JTextField numberOfEdgesTextField = new JTextField();
 	
 	private JComboBox<String> firstEdgeConnectingMethodComboBox = new JComboBox<String>(this.firstEdgeConnectingMethod);
 	
 	private EdgeConnectingMethodPanel edgeConnectingMethodPanel = new EdgeConnectingMethodPanel();
 	
 	private JButton addOwnConnectionMethodButton = new JButton(Strings.NEW_NETWORK_DIALOG_ADD);
 
 	public NewNetworkDialog(Component owner) {
 		this.owner = owner;
 		
 		this.setTitle(Strings.NEW_NETWORK_DIALOG_TITLE);
 		this.setSize(MainGuiConfiguration.NEW_NETWORK_DIALOG_SIZE);
 		
 		this.setModalityType(ModalityType.APPLICATION_MODAL);
 		this.setLocationRelativeTo(this.owner);
 		
 		this.getContentPane().setLayout(new BorderLayout());
 		this.getContentPane().add(this.buildContent(), BorderLayout.CENTER);
 		this.getContentPane().add(this.buildControlPanel(), BorderLayout.SOUTH);
 	}
 
 	private JPanel buildContent() {
 		JPanel contentPanel = new JPanel();
 		contentPanel.setLayout(new GridBagLayout());
 		contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
 		
 		connectingMethodRadioGroup.add(this.predefinedRadioButton);
 		connectingMethodRadioGroup.add(this.ownRadioButton);
 		
 		GridBagConstraints constrains = new GridBagConstraints();
 		constrains.ipadx = 10;
 		constrains.ipady = 10;
 		constrains.fill = GridBagConstraints.HORIZONTAL;
 		constrains.anchor = GridBagConstraints.NORTHWEST;
 		constrains.weighty = 1;
 		constrains.weightx = 1;
 		
 		// line 0
 		this.addToPanel(new JLabel(Strings.NEW_NETWORK_DIALOG_BASIC_INFO), contentPanel, constrains, 0, 0, 8, 1);
 		
 		// line 1
 		this.addToPanel(new JLabel(Strings.NEW_NETWORK_DIALOG_NAME), contentPanel, constrains, 0, 1, 4, 1);
 		this.addToPanel(this.networkNameTextField, contentPanel, constrains, 4, 1, 4, 1);
 		
 		// line 2
 		this.addToPanel(new JLabel(Strings.NEW_NETWORK_DIALOG_NUMBER_OF_NODES), contentPanel, constrains, 0, 2, 4, 1);
 		this.addToPanel(this.numberOfNodesTextField, contentPanel, constrains, 4, 2, 4, 1);
 		
 		// line 3
 		this.addToPanel(new JLabel(""), contentPanel, constrains, 0, 3, 8, 1);
 		
 		// line 4
 		this.addToPanel(new JLabel(Strings.NEW_NETWORK_DIALOG_ADVANCED_INFO), contentPanel, constrains, 0, 4, 8, 1);
 		
 		// line 5
 		this.addToPanel(this.predefinedRadioButton, contentPanel, constrains, 0, 5, 8, 1);
 		
 		// line 6
 		this.addToPanel(new JLabel(Strings.NEW_NETWORK_DIALOG_GROWTH_MANAGEMENT), contentPanel, constrains, 0, 6, 4, 1);
 		this.addToPanel(this.predefinedGrowthMethodComboBox, contentPanel, constrains, 4, 6, 4, 1);
 		
 		// line 7
 		this.addToPanel(new JLabel(Strings.NEW_NETWORK_DIALOG_NUMBER_OF_EDGES), contentPanel, constrains, 0, 7, 4, 1);
 		this.addToPanel(this.numberOfEdgesTextField, contentPanel, constrains, 4, 7, 4, 1);
 		
 		// line 8
 		this.addToPanel(this.ownRadioButton, contentPanel, constrains, 0, 8, 8, 1);
 		
 		// line 9
 		this.addToPanel(new JLabel(Strings.NEW_NETWORK_DIALOG_FIRST_EDGE), contentPanel, constrains, 0, 9, 4, 1);
 		this.addToPanel(this.firstEdgeConnectingMethodComboBox, contentPanel, constrains, 4, 9, 4, 1);
 		
 		// line 10
 		this.addToPanel(new JLabel(Strings.NEW_NETWORK_DIALOG_CONNECTING_WITH_SELECTING_NODE), contentPanel, constrains, 0, 10, 7, 1);
 		this.addToPanel(this.addOwnConnectionMethodButton, contentPanel, constrains, 7, 10, 1, 1);
 		
 		// line 11
 		this.addToPanel(this.edgeConnectingMethodPanel, contentPanel, constrains, 0, 11, 8, 1);
 		
 		this.enableOwnConnectingMethod(false);
 		this.createEventsOnContentPanel();
 		
 		return contentPanel;
 	}
 	
 	private void addToPanel(Component component, JPanel panel, GridBagConstraints constrains, int x, int y, int width, int height) {
 		constrains = GuiUtils.setPropsToGridBagContraints(constrains, x, y, width, height);
 		panel.add(component, constrains);
 	}
 	
 	private void enablePredefinedConnectingMethod(boolean enable) {
 		this.predefinedGrowthMethodComboBox.setEnabled(enable);
 		this.numberOfEdgesTextField.setEnabled(enable);
 	}
 	
 	private void enableOwnConnectingMethod(boolean enable) {
 		this.firstEdgeConnectingMethodComboBox.setEnabled(enable);
 		this.edgeConnectingMethodPanel.setEnabled(enable);
 		this.addOwnConnectionMethodButton.setEnabled(enable);
 	}
 	
 	private void createEventsOnContentPanel() {
 		this.predefinedRadioButton.addItemListener(new ItemListener() {
 			
 			@Override
 			public void itemStateChanged(ItemEvent e) {
 				if (e.getStateChange() == ItemEvent.SELECTED) {
 					enablePredefinedConnectingMethod(true);
 				} else if (e.getStateChange() == ItemEvent.DESELECTED) {
 					enablePredefinedConnectingMethod(false);
 				}
 			}
 		});
 		
 		this.ownRadioButton.addItemListener(new ItemListener() {
 
 			@Override
 			public void itemStateChanged(ItemEvent e) {
 				if (e.getStateChange() == ItemEvent.SELECTED) {
 					enableOwnConnectingMethod(true);
 				} else if (e.getStateChange() == ItemEvent.DESELECTED) {
 					enableOwnConnectingMethod(false);
 				}
 			}
 			
 		});
 		
 		this.addOwnConnectionMethodButton.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				EdgeConnectingMethodRow row = new EdgeConnectingMethodRow(edgeConnectingMethodPanel);
 				edgeConnectingMethodPanel.addItem(row);
 			}
 		});
 	}
 	
 	private JPanel buildControlPanel() {
 		JPanel controlPanel = new JPanel();
 		controlPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
 		
 		JButton createButton = new JButton(Strings.OK);
 		createButton.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
				NetworkBuildConfiguration config = new NetworkBuildConfiguration();
 				StringBuffer sb = new StringBuffer();
 				String networkNameText = networkNameTextField.getText();
 				if (networkNameText.equals("")) {
 					sb.append(Strings.WRONG_VALUE).append(": ").append(Strings.NEW_NETWORK_DIALOG_NAME).append("\n");
 				} else if (ControllerService.getAppController().isNetworkWithName(networkNameText)) {
 					sb.append(Strings.WRONG_VALUE).append(": ").append(Strings.NEW_NETWORK_DIALOG_NAME).append("\n");
 				}
 				String numberOfNodesText = numberOfNodesTextField.getText();
 				if (!CommonUtils.isPositiveNumber(numberOfNodesText)) {
 					sb.append(Strings.WRONG_VALUE).append(": ").append(Strings.NEW_NETWORK_DIALOG_NUMBER_OF_NODES).append("\n");
 				}
 				
 				if (predefinedRadioButton.isSelected()) {
 					String predefinedGrowthMethodComboBoxValue = (String) predefinedGrowthMethodComboBox.getSelectedItem();
 					int growthManagement = -1;
 					if (predefinedGrowthMethodComboBoxValue.equals(Strings.NEW_NETWORK_DIALOG_GROWTH_MANAGEMENT_NODE_DEGREE)) {
 						growthManagement = EdgeConnectingMethodRowConfig.DRIVEN_DEGREE;
 					} else if (predefinedGrowthMethodComboBoxValue.equals(Strings.NEW_NETWORK_DIALOG_GROWTH_MANAGEMENT_NODE_CLASTER)) {
 						growthManagement = EdgeConnectingMethodRowConfig.DRIVEN_CLUSTER;
 					} else if (predefinedGrowthMethodComboBoxValue.equals(Strings.NEW_NETWORK_DIALOG_GROWTH_MANAGEMENT_RANDOM)) {
 						growthManagement = EdgeConnectingMethodRowConfig.DRIVEN_RANDOM;
 					}
 					
 					String numberOfEdgesText = numberOfEdgesTextField.getText();
 					if (!CommonUtils.isPositiveNumber(numberOfEdgesText)) {
 						sb.append(Strings.WRONG_VALUE).append(": ").append(Strings.NEW_NETWORK_DIALOG_NUMBER_OF_EDGES).append("\n");
 					}
 					if (sb.toString().equals("")) {
 						int numberOfNodes = Integer.parseInt(numberOfNodesText);
 						int numberOfEdges = Integer.parseInt(numberOfEdgesText);
 						config = NetworkBuildConfiguration.createDefaultConfig(growthManagement, networkNameText, numberOfNodes, numberOfEdges);
 					}
 				} else { // own configuration
 					String firstEdgeConnectingMethodComboBoxValue = (String) firstEdgeConnectingMethodComboBox.getSelectedItem();
 					int firstEdgeConnectingMethod = -1;
 					if (firstEdgeConnectingMethodComboBoxValue.equals(Strings.NEW_NETWORK_DIALOG_GROWTH_MANAGEMENT_NODE_DEGREE)) {
 						firstEdgeConnectingMethod = NetworkBuildConfiguration.DEGREE_DRIVEN;
 					} else if (firstEdgeConnectingMethodComboBoxValue.equals(Strings.NEW_NETWORK_DIALOG_GROWTH_MANAGEMENT_NODE_CLASTER)) {
 						firstEdgeConnectingMethod = NetworkBuildConfiguration.CLUSTER_DRIVEN;
 					} else if (firstEdgeConnectingMethodComboBoxValue.equals(Strings.NEW_NETWORK_DIALOG_GROWTH_MANAGEMENT_RANDOM)) {
 						firstEdgeConnectingMethod = NetworkBuildConfiguration.RANDOM_DRIVEN;
 					}
 					try {
 						List<EdgeConnectingMethodRowConfig> rowConfigs = edgeConnectingMethodPanel.getConfig();
 						if (!rowConfigs.isEmpty()) {
 							config.setEdgeConnectingMethodRowConfigs(rowConfigs);
 						} else {
 							sb.append(Strings.WRONG_VALUE).append(": ").append(Strings.NEW_NETWORK_DIALOG_CONNECTING_WITH_SELECTING_NODE).append("\n");
 						}
 					} catch (NumberFormatException nfe) {
 						sb.append(Strings.WRONG_VALUE).append(": ").append(Strings.NEW_NETWORK_DIALOG_CONNECTING_WITH_SELECTING_NODE).append("\n");
 					}
 					if (sb.toString().equals("")) {
 						config.setName(networkNameText);
 						config.setNumberOfNodes(Integer.parseInt(numberOfNodesText));
 						config.setFirstEdgeConnecting(firstEdgeConnectingMethod);
 					}
 				}
 				
 				if (sb.toString().equals("")) {
 					dispose();
 					ControllerService.getNetworkController().buildNetwork(config);
 				} else {
 					JOptionPane.showMessageDialog(
 							owner,
 							sb.toString(),
 							Strings.ERROR,
 							JOptionPane.ERROR_MESSAGE);
 				}
 			}
 		});
 		
 		JButton cancelButton = new JButton(Strings.CANCEL);
 		cancelButton.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				dispose();
 			}
 		});
 		
 		controlPanel.add(createButton);
 		controlPanel.add(cancelButton);
 		
 		return controlPanel;
 	}
 
 }
