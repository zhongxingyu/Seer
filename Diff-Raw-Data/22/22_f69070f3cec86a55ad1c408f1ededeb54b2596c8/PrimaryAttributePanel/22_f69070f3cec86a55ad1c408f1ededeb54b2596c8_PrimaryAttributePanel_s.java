 package AttributePanel;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.io.File;
 import java.lang.reflect.Constructor;
 import java.util.TreeMap;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.GroupLayout;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.event.TableModelEvent;
 
 import All.CogAgent;
 import All.Constants;
 import All.DrawingPanel;
 import All.DynamicClassLoader;
 import Data.Edge;
 import Data.Node;
 import Data.Primary;
 import Distributions.ProbabilityDistribution;
 //import Distributions.UniformDistribution;
 
 
 public class PrimaryAttributePanel extends NodeAttributesPanel{
 	public ProbabilityDistribution tempActiveDist, tempInactiveDist;
 	public JLabel activeDistLabel, inactiveDistLabel, selectedChannelLabel;
 	public JComboBox activeDist, inactiveDist;
 	public JButton editActiveDist, editInactiveDist;
 	public JTextArea activeSeed, inactiveSeed;
 	public JTextArea selectedChannel;
 	public Primary selectedNode;
 	public JPanel distPanel;
 	public DrawingPanel drawingPanel;
 	public TreeMap<String, Constructor<ProbabilityDistribution>> distConstructorMap;
 	public JCheckBox virtualCheckBox;
 	public JButton onOffButton;
 	public JComboBox channelList;
	
 	public PrimaryAttributePanel(DrawingPanel dp){
 		this.drawingPanel = dp;
 		setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Primary Attributes Panel"));
 		distConstructorMap = new TreeMap<String, Constructor<ProbabilityDistribution>>();
 		activeDistLabel = new JLabel("Active Distribution");
 		String[] avalDistributions = getAvalDistributions();
 		activeDist = new JComboBox(avalDistributions);
 		activeDist.addItemListener(new ItemListener() {
 			@Override
 			public void itemStateChanged(ItemEvent e) {
 				String s = (String) e.getItem();
 				System.out.println(s);
 				if(e.getStateChange() == e.SELECTED){
 					try {
 						tempActiveDist = distConstructorMap.get((String)activeDist.getSelectedItem()).newInstance();
 					} catch (Exception e1) {
 						e1.printStackTrace();
 					} 
 				}
 			}
 		});
 		editActiveDist = new JButton("Edit");
 		editActiveDist.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				if(tempActiveDist == null)
 					try {
 						tempActiveDist = distConstructorMap.get((String)activeDist.getSelectedItem()).newInstance();
 					} catch (Exception e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					} 
 				tempActiveDist.showPanel();
 			}
 		});
 
 		inactiveDistLabel = new JLabel("Inactive Distribution");
 		inactiveDist = new JComboBox(avalDistributions);
 		inactiveDist.addItemListener(new ItemListener() {
 			@Override
 			public void itemStateChanged(ItemEvent e) {
 				if(e.getStateChange() == e.SELECTED){
 					try {
 						tempInactiveDist = distConstructorMap.get((String)inactiveDist.getSelectedItem()).newInstance();
 					} catch (Exception e1) {
 						e1.printStackTrace();
 					} 
 				}
 			}
 		});
 		editInactiveDist = new JButton("Edit");
 		editInactiveDist.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				if(tempInactiveDist == null){
 					try {
 						tempInactiveDist = distConstructorMap.get((String)inactiveDist.getSelectedItem()).newInstance();
 					} catch (Exception e) {
 						e.printStackTrace();
 					} 
 				}
 				tempInactiveDist.showPanel();
 			}
 		});
 
 		selectedChannelLabel = new JLabel("Selected Channel");
 		selectedChannel = new JTextArea();
 		selectedChannel.setLineWrap(true);
 		selectedChannel.setBorder(BorderFactory.createLoweredBevelBorder());
 
 		setData = new JButton("Set Data");
 		setData.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				if(selectedNode != null){
 					selectedNode.name = name.getText();
 
 					selectedNode.ETH_IP = ETH_IP.getText();
 					selectedNode.ETH_HW = ETH_HW.getText();
 					selectedNode.WLS_IP = parseSeparatedString(WLS_IP.getText());
 //					selectedNode.WLS_HW = parseSeparatedString(WLS_HW.getText());
 
 					selectedNode.channels = parseChannels(channels.getText());
 
 					selectedNode.mobilityOption = mobilityOption.getSelectedItem().toString();
 					selectedNode.topologyOption = topologyOption.getSelectedItem().toString();
 
 					selectedNode.activeDist = tempActiveDist;
 					selectedNode.inactiveDist = tempInactiveDist;
					
 					drawingPanel.repaint();
 
 					System.out.println("Successfully updated the node:\n" + selectedNode);
 				}
 			}
 		});
		
 		String[] channelsList = {"1", "6", "11"};
 		channelList = new JComboBox(channelsList);
 		JPanel all = new JPanel();
 		virtualCheckBox = new JCheckBox("Virtual");
 		onOffButton = new JButton("Off");
 		onOffButton.setBackground(Color.red);
 		onOffButton.addActionListener(new ActionListener() {
			
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				String text = onOffButton.getText();
 				String selectedChannel = (String) channelList.getSelectedItem();
 				for(Edge edge: selectedNode.adjacent) {
 					Node node = edge.to;
 					CogAgent agent = new CogAgent(node);
 					if(text.equals("On")) {
 						agent.setPrimaryUser(selectedChannel, 0);
 						onOffButton.setText("Off");
 						onOffButton.setBackground(Color.red);
 					} else {
 						agent.setPrimaryUser(selectedChannel, 1);
 						onOffButton.setText("On");
 						onOffButton.setBackground(Color.green);
 					}
 				}
 			}
 		});
		
 		onOffButton.setEnabled(false);
 		virtualCheckBox.addActionListener(new ActionListener() {
			
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				if(virtualCheckBox.isSelected()) {
 					System.out.println("Checked");
 					onOffButton.setEnabled(true);
 				} else {
 					onOffButton.setEnabled(false);
 				}
 			}
 		});
 
 		GroupLayout layout = new GroupLayout(all);
 		layout.setAutoCreateGaps(true);
         layout.setAutoCreateContainerGaps(true);
 
 		all.setLayout(layout);
 		add(all);
 
 		Component gap = Box.createRigidArea(new Dimension(Constants.COMPONENTS_GAP, Constants.COMPONENTS_GAP));
 
 		layout.setHorizontalGroup(
 		   layout.createParallelGroup()
 		   		  .addGroup(layout.createSequentialGroup()
 				      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
 				    	   .addComponent(nameLabel)
 				      	   .addComponent(ETH_IPLabel)
 				      	   .addComponent(ETH_HWLabel)
 				      	   .addComponent(WLS_IPLabel)
 				      	   .addComponent(WLS_HWLabel)
 				      	   .addComponent(channelsLabel)
 				      	   .addComponent(mobilityOptionLabel)
 				      	   .addComponent(topologyOptionLabel)
 				      	   .addComponent(gap)
 				           .addComponent(activeDistLabel)
 				           .addComponent(inactiveDistLabel)
 				           .addComponent(selectedChannelLabel))
 			          .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
 		        		   .addComponent(name)
 				      	   .addComponent(ETH_IP)
 				      	   .addComponent(ETH_HW)
 				      	   .addComponent(WLS_IP)
 				      	   .addComponent(ps)
 				      	   .addComponent(channels)
 				      	   .addComponent(mobilityOption)
 				      	   .addComponent(topologyOption)
 				      	   .addComponent(gap)
 				      	   .addGroup(layout.createSequentialGroup()
 				      	   		.addComponent(activeDist)
 				      	   		.addComponent(editActiveDist))
 			      	   	   .addGroup(layout.createSequentialGroup()
 			      	   			.addComponent(inactiveDist)
 			      	   			.addComponent(editInactiveDist))
 			      	   	   .addComponent(selectedChannel)
 			      	   	   .addComponent(setData)
 			      	   	   .addComponent(virtualCheckBox)
 			      	   	   .addComponent(onOffButton)
 			      	   	   .addComponent(channelList))
 		   			  )
 		);
 
 		layout.setVerticalGroup(
 		   layout.createSequentialGroup()
 		   	  .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
 		           .addComponent(nameLabel)
 		           .addComponent(name))
 	          .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
 		           .addComponent(ETH_IPLabel)
 		           .addComponent(ETH_IP))
 	          .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
 		           .addComponent(ETH_HWLabel)
 		           .addComponent(ETH_HW))
 	          .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
 		           .addComponent(WLS_IPLabel)
 		           .addComponent(WLS_IP)
 		           )
 	          .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
 		           .addComponent(WLS_HWLabel)
 		           .addComponent(ps)
 		           )
 	          .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
 		           .addComponent(channelsLabel)
 		           .addComponent(channels))
 	          .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
 		           .addComponent(mobilityOptionLabel)
 		           .addComponent(mobilityOption))
 		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
 		           .addComponent(topologyOptionLabel)
 		           .addComponent(topologyOption))
 		      .addComponent(gap)
 		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
 		    	   .addComponent(activeDistLabel)
 		    	   .addComponent(activeDist)
 		           .addComponent(editActiveDist))
 		       .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
 		    	   .addComponent(inactiveDistLabel)
 		           .addComponent(inactiveDist)
 		           .addComponent(editInactiveDist))
 		       .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
 		    	   .addComponent(selectedChannelLabel)
 		    	   .addComponent(selectedChannel))
 		       .addComponent(setData)
 		       .addComponent(virtualCheckBox)
 		       .addComponent(onOffButton)
 		       .addComponent(channelList)
 		);
 	}
 
 	private String[] getAvalDistributions() {
 		String currDir = System.getProperty("user.dir");
 		System.out.println(currDir);
 		File[] files = new File(currDir).listFiles();
 		DynamicClassLoader classLoader = new DynamicClassLoader();
 	    for (File file : files) {
 	    	if(file.isDirectory())
 	    		continue;
 	    	if(!file.getName().endsWith(".class"))
 	    		continue;
 	    	String fileName = file.getName().substring(0,file.getName().indexOf('.'));
             Class<ProbabilityDistribution> myClass = classLoader.Load(currDir+"\\"+file.getName(), "Distributions."+fileName);
             if(myClass != null && !fileName.contains("$")) {
             	try {
 					distConstructorMap.put(fileName, myClass.getConstructor());
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
             }
 	    }
 	    String[] arr = new String[distConstructorMap.size()];
 	    int index = 0;
 	    for(String s: distConstructorMap.keySet()) 
 	    	arr[index++] = s;
 		return arr;
 	}
 
 	public void setInfo(Primary node){
 		selectedNode = node;
 
 		//Fill the required information.
 		name.setText(node.name);
 
 		ETH_IP.setText(node.ETH_IP);
 		ETH_HW.setText(node.ETH_HW);
 		WLS_IP.setText(node.getWLS_IP());
 		WirelessInterfacesTable t = (WirelessInterfacesTable)WLS_HW_Table.getModel();
 		t.current.clear();
 		for(int i = 0; i < node.WLS_HW.size(); i++){
 			t.current.add(new WirelessTableRowEntry(node.WLS_Name.get(i), node.WLS_HW.get(i)));
 			WLS_HW_Table.getTableHeader().repaint();
 			WLS_HW_Table.tableChanged(new TableModelEvent(t));
 			WLS_HW_Table.repaint();
 		}
 
 		channels.setText(node.getChannels());
 
 		mobilityOption.setSelectedItem(node.mobilityOption);
 		topologyOption.setSelectedItem(node.topologyOption);
		
 		if ((node).activeDist != null) {
 			activeDist.setSelectedItem((node).activeDist.type()+"Distribution");
 			System.out.println("TTTT");
 		}
 		if ((node).inactiveDist != null) {
 			System.out.println("T@");
 			inactiveDist.setSelectedItem((node).inactiveDist.type()+"Distribution");
 		}
 
 		//Delayed Setting. NEED TO BE CLONED.
 		tempActiveDist = (node).activeDist;
 		tempInactiveDist = (node).inactiveDist;
 	}
 }

