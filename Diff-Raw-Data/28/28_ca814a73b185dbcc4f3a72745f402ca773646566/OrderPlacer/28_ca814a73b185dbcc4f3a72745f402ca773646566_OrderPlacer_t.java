 package uk.ac.aber.dcs.cs12420.aberpizza.gui;
 
 import java.awt.Container;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.IOException;
 
 import javax.swing.*;

 import java.awt.Component;
 
 import uk.ac.aber.dcs.cs12420.aberpizza.data.Inventory;
 import uk.ac.aber.dcs.cs12420.aberpizza.data.Till;
 
 /**
  * Creates a JFrame within which orders may be created and placed.
  * 
  * @author Jacob Smith
  *
  */
 public class OrderPlacer {
 	/**
 	 * Handle to the model that this view inserts orders into.
 	 */
 	private Till till;
 	private Inventory inventory;
 	private OrderBuilder orderPanel;
 	private JFrame frame = null;
 	
 	/**
 	 * Create, arrange and set visible the elements of the JFrame.
 	 */
 	public OrderPlacer() {
 		
 		try {
 			this.till = Till.load();
 		} catch (IOException e1) {
 			this.till = new Till();
 			//e1.printStackTrace();
 			
 		}
 		
 		try {
 			inventory = Inventory.load(null);
 		} catch (IOException e) {
 			//inventory = new Inventory();
 			e.printStackTrace();
 			System.exit(0);
 		}
 		
 		frame = new JFrame("Order Placer");
 		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 		frame.addWindowListener(new ExitListener(frame,till));
 		
 		Container layout = frame.getContentPane();
 		GridBagLayout gbl = new GridBagLayout();
 		layout.setLayout(gbl);
 		gbl.layoutContainer(layout);
 		
 		
 		GridBagConstraints c = new GridBagConstraints();
 		
 		
 		JToolBar toolbar = new JToolBar("toolbar");
 		toolbar.setFloatable(false);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridx = 0;
 		c.gridy = 0;
 		c.weightx = 1;
 		c.weighty = 0;
 		layout.add(toolbar, c);
 		
 		JButton button = null;
 		
 		button = new JButton("New order");
 		button.addActionListener(new NewOrderListener(this));
 		toolbar.add(button);
 		
 		button = new JButton("Send order");
 		button.addActionListener(new SendOrderListener(this));
 		toolbar.add(button);
 		
 		button = new JButton("Review orders");
 		button.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				try {
 					till.save();
 					frame.dispose();
 					new TillReviewer(till);
 					
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 				
 		});
 		toolbar.add(button);
 		
 		orderPanel = new OrderBuilder(inventory);
 		c.fill = GridBagConstraints.BOTH;
 		c.gridy = 1;
 		c.weighty = 1;
 		layout.add(orderPanel.getComponent(), c);
 		
 		
 		frame.pack();
 		frame.setSize(800,480);
 		frame.setVisible(true);
 	}
 
 	public Component getComponent() {
 		return orderPanel.getComponent();
 	}
 	
 	public OrderBuilder order() { return orderPanel; }
 	public Till getTill() { return till; }
 	
	public static void main (String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		new OrderPlacer();
	}
 	
 
 	
 }
