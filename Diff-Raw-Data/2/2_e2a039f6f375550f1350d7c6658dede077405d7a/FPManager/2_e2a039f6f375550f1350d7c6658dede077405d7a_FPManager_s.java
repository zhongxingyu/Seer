 package net.managers;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.*;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.*;
 
 import net.*;
 import state.*;
 
 @SuppressWarnings("serial")
 public class FPManager extends Manager implements ActionListener {
 	GraphicsPanel gPanel;
 	ControllerPanel cPanel;
 	
 	List<KitConfig> configList;
 	
 	public FPManager() {
 		super(ManagerType.Factory);
 		
 		gPanel = new GraphicsPanel(imageMap, inMap);
 		cPanel = new ControllerPanel();
 		configList = new ArrayList<KitConfig>();
 		
 		Dimension full = new Dimension(1024, 768);
 		gPanel.setMaximumSize(full);
 		gPanel.setMinimumSize(full);
 		gPanel.setPreferredSize(full);
 		
 		this.setContentPane(new JPanel(new BorderLayout()));
 		this.add(cPanel, BorderLayout.WEST);
 		this.add(gPanel, BorderLayout.CENTER);
 		this.setVisible(true);
 		this.pack();
 		
 		outMap.put("orders", new ArrayList<OrderConfig>());
 	}
 		
 	@SuppressWarnings("unchecked")
 	public void actionPerformed(ActionEvent ae) {
 		gPanel.inMap = inMap;
 		
		//cPanel.kitConfigBox.removeAll();
 		configList = (List<KitConfig>)inMap.get("configlist");
 		
 		if(configList != null)
 			for(KitConfig k : configList)
 				cPanel.kitConfigBox.addItem(k.kitName);
 		
 		cPanel.kitConfigBox.revalidate();
 		gPanel.repaint();
 	}
 	
 	public static void main(String[] args) {
 		FPManager m = new FPManager();
 		new Timer(GraphicsPanel.SYNCRATE, m).start();
 	}
 	
 	class ControllerPanel extends Box implements ActionListener {
 		JComboBox kitConfigBox;
 		JTextField numberField;
 		JButton addOrderButton;
 		
 		public ControllerPanel() {
 			super(BoxLayout.Y_AXIS);
 			
 			kitConfigBox = new JComboBox();
 			numberField = new JTextField();
 			addOrderButton = new JButton("Add Order");
 			
 			addOrderButton.addActionListener(this);
 
 			this.add(new JLabel("Production Pane"));
 			
 			Box holdingPane;
 
 			holdingPane = new Box(BoxLayout.X_AXIS);
 			holdingPane.add(new JLabel("Combo Box"));
 			holdingPane.add(kitConfigBox);
 			holdingPane.setMaximumSize(new Dimension(200, 20));
 			this.add(holdingPane);
 			
 			holdingPane = new Box(BoxLayout.X_AXIS);
 			holdingPane.add(new JLabel("Number Field"));
 			holdingPane.add(numberField);
 			holdingPane.setMaximumSize(new Dimension(200, 20));
 			this.add(holdingPane);
 			
 			this.add(addOrderButton);
 			this.add(Box.createVerticalGlue());
 		}
 		
 		@SuppressWarnings("unchecked")
 		public void actionPerformed(ActionEvent ae) {
 			String cmd = ae.getActionCommand();
 			
 			if(cmd.equals("Add Order")) {
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
 				KitConfig selected = null;
 				for(KitConfig k : configList)
 					if(k.kitName.equals(kitName))
 						selected = k;
 				
 				ArrayList<OrderConfig> orders = (ArrayList<OrderConfig>)outMap.get("orders");
 				orders.add(new OrderConfig(selected, number));
 				outMap.put("orders", orders);
 				
 			} else {
 				System.out.println("Undefined action.");
 			}
 		}
 	}
 }
