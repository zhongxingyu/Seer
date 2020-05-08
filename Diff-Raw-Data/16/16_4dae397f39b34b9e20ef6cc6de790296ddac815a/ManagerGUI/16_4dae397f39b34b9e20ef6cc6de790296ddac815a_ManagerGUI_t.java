package com.cs304.managerUI;
 
 
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.sql.Connection;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 
 
 public class ManagerGUI{
 
 	//UNCOMMENT LATER
 	Connection connection;
 	
 	private JFrame Frame;
 	
 	
 	private JButton addItem;
 	private JButton processDelivery;
 	private JButton salesReport;
 	private JButton topSellers;
 	
 	// constructor 
 	public ManagerGUI() {
 		//Title
 		//super("Clerk View");
 		//setLayout( new FlowLayout());
 
 		//f =frame;
 		
 		Frame = new JFrame("Manager Operations");
 		Frame.setVisible(true);
 		Frame.setSize(400, 300);
 		Dimension d = Frame.getToolkit().getScreenSize();
 		Rectangle r = Frame.getBounds();
 		Frame.setLocation((d.width - r.width) / 2, (d.height - r.height) / 2);
 
 		GridBagLayout gb = new GridBagLayout();
 		GridBagConstraints g = new GridBagConstraints();
 		
 		JPanel panel = new JPanel();
 		
 		addItem = new JButton("Add items to the store");
 		processDelivery = new JButton("Process the delivery of an order");
 		salesReport = new JButton("Show daily sales report");
 		topSellers = new JButton("Best selling items");
 		
 
 		g.gridx = 0;
 		g.gridy = 1;
 		g.gridwidth = 5;
 		g.fill = GridBagConstraints.HORIZONTAL;
 		g.insets = new Insets(10, 10, 10, 10);
 		gb.setConstraints(addItem, g);
 		panel.add(addItem);
 
 		g.gridx = 0;
 		g.gridy = 2;
 		g.insets = new Insets(10, 10, 10, 10);
 		gb.setConstraints(processDelivery, g);
 		panel.add(processDelivery);
 		
 		g.gridx = 0;
 		g.gridy = 3;
 		g.insets = new Insets(10, 10, 10, 10);
 		gb.setConstraints(salesReport, g);
 		panel.add(salesReport);
 		
 		g.gridx = 0;
 		g.gridy = 4;
 		g.insets = new Insets(10, 10, 10, 10);
 		gb.setConstraints(topSellers, g);
 		panel.add(topSellers);
 		
 		panel.setLayout(gb);
 		
 		theHandler Handler = new theHandler();
 		addItem.addActionListener(Handler);
 		processDelivery.addActionListener(Handler);
 		salesReport.addActionListener(Handler);
 		topSellers.addActionListener(Handler);
 		
 		Frame.add(panel);
 	}
 	
 	// takes one method, whenever an event occurs
 	private class theHandler implements ActionListener{
 
 		@Override
 		public void actionPerformed(ActionEvent event) {
 			
 			String string = new String();
 			
 			
 			JOptionPane.showMessageDialog(null, string);
 		}
 		
 		
 	}
 	
 }
