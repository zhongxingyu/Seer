 package uk.ac.aber.dcs.cs12420.aberpizza.gui;
 
 import java.awt.Container;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.io.IOException;
 
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JToolBar;
 
 import uk.ac.aber.dcs.cs12420.aberpizza.data.Order;
 import uk.ac.aber.dcs.cs12420.aberpizza.data.Till;
 
 public class TillReviewer {
 	private JFrame frame = null;
 	private Till till = null;
 	private JPanel pane = null;
 	private JScrollPane scrollPane = null;
 	private int numberOfOrders = 0;
 
 	public TillReviewer(Till till) {
 		this.till = till;
 		numberOfOrders = till.getOrders().size();
 		
 		frame = new JFrame("Review orders");
 		
 		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 		frame.addWindowListener(new WindowListener() {
 
 			public void windowActivated(WindowEvent arg0) {}
 			public void windowClosed(WindowEvent arg0) {}
 			public void windowClosing(WindowEvent arg0) {
 				frame.dispose();
 				new OrderPlacer();
 			}
 			public void windowDeactivated(WindowEvent arg0) {}
 			public void windowDeiconified(WindowEvent arg0) {}
 			public void windowIconified(WindowEvent arg0) {}
 			public void windowOpened(WindowEvent arg0) {}
 			
 		});
 		
 		addElements();
 		
 		frame.pack();
 		frame.setSize(800,480);
 		frame.setVisible(true);
 		
 	}
 
 	public void addElements() {
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
 		
 		JButton button = new JButton("Open file...");
 		button.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				JFileChooser chooser = new JFileChooser("data");
 				chooser.showOpenDialog(frame);
 				try {
 					Till till = Till.load(chooser.getSelectedFile());
 					new TillReviewer(till);
 					frame.dispose();
 				} catch (Exception e) {
 					e.printStackTrace();
 					JOptionPane.showMessageDialog(frame,
 							"Could not open file as Till",
 							"Error",JOptionPane.ERROR_MESSAGE);
 				}
 			}
 		});
 		toolbar.add(button);
 		
 		pane = new JPanel();
 		pane.setLayout(new GridBagLayout() );
 				
 		c.gridy++;
 		c.weighty = 1;
 		c.fill = GridBagConstraints.BOTH;
 		scrollPane = new JScrollPane(pane);
 		layout.add(scrollPane,c);
 		
 		drawOrders();
 		
 	}
 
 	private void drawOrders() {
 		
 		GridBagConstraints c = new GridBagConstraints();
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridx = 0;
 		c.gridy = 0;
 		c.weightx = 1;
 		c.weighty = 0;
 		
 		JLabel total = new JLabel("Total for day: "+till.getTotalForDay()+", in "+numberOfOrders+" orders.");
 		pane.add(total,c);
 		c.gridy++;
 		
 		
 		for (Order order : till.getOrders() ) {
 			c.gridx = 0;
 			JButton button = new JButton(""+Order.niceDate(order.getDate())+", "+order.getCustomerName()+", "+order.getSubtotal() );
 			button.addActionListener(new ReceiptOpenner(order));
 			pane.add(button,c);
 			c.gridy++;
 		}
		scrollPane.repaint();
 		
 	}
 	
 }
 
 class ReceiptOpenner implements ActionListener {
 	private Order order = null;
 	
 	public ReceiptOpenner(Order order) {
 		this.order = order;
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		new ReceiptViewer(order);
 	}
 	
 }
