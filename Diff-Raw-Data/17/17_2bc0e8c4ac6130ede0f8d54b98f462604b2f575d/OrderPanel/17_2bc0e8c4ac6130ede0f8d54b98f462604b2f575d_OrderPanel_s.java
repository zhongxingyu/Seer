 package net.managers;
 
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.*;
 import java.util.*;
 
 import javax.swing.*;
 import javax.swing.Timer;
 
 import state.KitConfig;
 import state.ManagerType;
 import state.OrderConfig;
 import net.GraphicsPanel;
 import net.SyncManager;
 
 @SuppressWarnings("serial")
 public class OrderPanel extends SyncManager implements ActionListener {
 	Map<String, Object> inMap, outMap;
 	FPMControllerPanel cPanel;
 	
 	public OrderPanel() {
 		super(ManagerType.FPMOrder);
 		
 		inMap = new HashMap<String, Object>();
 		outMap = new HashMap<String, Object>();
 		cPanel = new FPMControllerPanel();
 		
 		this.add(cPanel);
 		
 		new Timer(GraphicsPanel.SYNCRATE, this).start();
 	}
 
 	@SuppressWarnings("unchecked")
 	public void actionPerformed(ActionEvent ae) {
 		inMap.clear();
 		inMap.putAll(sendAndReceive(outMap));
 		outMap.clear();
 		
 		cPanel.orderList.setText("");
 		List<OrderConfig> orderList = (List<OrderConfig>)inMap.get("orders");
 		if(orderList != null){
                     
                     for(Object o : orderList.toArray()){
                         cPanel.orderList.append(((OrderConfig)o).toString() + "\n");                         
                     }
                                        
                 }
 		
 		
 		String console = (String)inMap.get("console");
 		if(console != null)
 			cPanel.consoleBox.append(console);
 	}
 	
 	class FPMControllerPanel extends JPanel implements ActionListener {
 			
 		List<KitConfig> configList;
 		JComboBox kitConfigBox;
 		JTextField numberField;
 		JButton addOrderButton, refreshButton;
 		JTextArea consoleBox, orderList;
 		JScrollPane consoleScroll, orderScroll;
 		
 		public FPMControllerPanel() {
 			this.setLayout(new GridBagLayout());
                         GridBagConstraints c1 = new GridBagConstraints();
 			
 			orderList = new JTextArea(8, 25);
                         orderScroll = new JScrollPane(orderList);
                         
                         orderList.setEditable(false);
                         
 			configList = new ArrayList<KitConfig>();
 			kitConfigBox = new JComboBox();
                         kitConfigBox.setPreferredSize(new Dimension(110,25));
 			numberField = new JTextField(5);
 			addOrderButton = new JButton("Add");
 			refreshButton = new JButton("Refresh");
 			consoleBox = new JTextArea(8, 25);
 			consoleScroll = new JScrollPane(consoleBox);
 			
 			addOrderButton.addActionListener(this);
 			refreshButton.addActionListener(this);
                         
                         c1.insets = new Insets(10,5,0,0);
                         c1.anchor = GridBagConstraints.NORTH;
                         c1.fill = GridBagConstraints.HORIZONTAL;
                         c1.gridx = 0;
                         c1.gridy = 0;
                         c1.gridwidth = 3;
                         c1.weighty = 0.1;
 			this.add(new JLabel("PRODUCTION PANE"),c1);
                         c1.anchor = GridBagConstraints.WEST;
                         c1.gridy = 1;
                         this.add(new JLabel("Orders in Queue:"),c1);
                         c1.gridy = 2;
 			this.add(orderScroll,c1);
 			
 			
                         c1.gridy = 3;
                         c1.gridwidth = 1;                        
 			c1.weightx = 0.3;
                        this.add(new JLabel("Kit Config: "),c1);
 			
			kitConfigBox.setPrototypeDisplayValue("Kit Config");
                         c1.gridx = 1;
                         c1.weightx = 0.4;
 			this.add(kitConfigBox,c1);
                         c1.gridx = 2;
                         c1.weightx = 0.3;
 			this.add(refreshButton,c1);
 			
                         c1.gridy = 4;
 			c1.gridx = 0;
 			this.add(new JLabel("Number of Kits: "),c1);
                         c1.gridx = 1;
                         c1.weightx = 0.4;
 			this.add(numberField, c1);		
 			
                         
                         c1.gridx = 2;
                         c1.weightx = 0.3;                        
 			this.add(addOrderButton,c1);
                         
                         c1.gridx = 0;
                         c1.gridwidth = 3;
                         c1.gridy = 5;
                         c1.weighty = 0.5;
                         this.add(new JLabel("Console:"),c1);
                         c1.weighty = 2;
                         c1.gridy = 6;
 			consoleBox.setEditable(false);
 			this.add(consoleScroll, c1);			
 
 		}
 
 		
 		@SuppressWarnings("unchecked")
 		public void actionPerformed(ActionEvent ae) {
 			
 			
 			if(ae.getSource() == addOrderButton) {
 				//Send order here.
 				int number = 0;
 				try {
 					number = Integer.parseInt(numberField.getText());
 				} catch(NumberFormatException e) {
 					JOptionPane.showMessageDialog(null,
 							"You need to enter a number in the number box",
 							"Error",
 							JOptionPane.ERROR_MESSAGE);
 					return;
 				}
 				
 				String kitName = (String)kitConfigBox.getSelectedItem();
                                if(!(kitName.equals(""))){
                                     KitConfig selected = null;
                                     for(KitConfig k : configList){
                                     	if(k.kitName.equals(kitName)){
                                             	selected = k;                                            
                                         }                                   
                                     }			
                                     outMap.put("order", new OrderConfig(selected, number));
                                 }
                                 else{
                                     JOptionPane.showMessageDialog(null,
 							"You need to specify a kit to be made",
 							"Error",
 							JOptionPane.ERROR_MESSAGE);                                    
                                 }
 				
 			} else if(ae.getSource() == refreshButton) {
 				kitConfigBox.removeAllItems();
 				configList = (List<KitConfig>)inMap.get("configlist");
 				
 				if(configList != null)
 					for(KitConfig k : configList)
 						kitConfigBox.addItem(k.kitName);
 				
 				this.revalidate();
 				this.repaint();
 			} else {
 				System.out.println("Undefined action.");
 			}
 		}
 	}
 }
