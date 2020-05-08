 package snookerTables;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.SpringLayout;
 import javax.swing.WindowConstants;
 
 public class Settings extends JFrame implements ActionListener {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	JTextField tables, price;
 	Main main;
 
 	public Settings(Main main) {
 
 		this.setVisible(true); //set frame visible
 		this.setSize(new Dimension(260, 320)); //set size of frame
 		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE); //set close action
 
 		this.main = main;
 		this.setLayout(new BorderLayout());
 
 		JLabel title = new JLabel("Settings", JLabel.CENTER);
 		this.add(title, BorderLayout.NORTH);
 
 		JPanel centerBox = new JPanel();
 		SpringLayout layout = new SpringLayout();
 		centerBox.setLayout(layout);
 
 		// JPanel tableSetts = new JPanel();
 		// centerBox.add(tableSetts);
 		JLabel numOfTables = new JLabel("Number of Tables: ");
 		// centerBox.add(numOfTables);
		tables = new JTextField("" + main.getNumberOfTables(), 4);
 		// centerBox.add(tables);
 		// JPanel priceSetts = new JPanel();
 		// centerBox.add(priceSetts);
 		JLabel pricePH = new JLabel("Adjust all price per hours: ");
 		centerBox.add(pricePH);
 		price = new JTextField(4);
 		centerBox.add(price);
 
 		layout.putConstraint(SpringLayout.NORTH, numOfTables, 5,
 				SpringLayout.NORTH, centerBox);
 		layout.putConstraint(SpringLayout.NORTH, tables, 5, SpringLayout.NORTH,
 				centerBox);
 		layout.putConstraint(SpringLayout.WEST, numOfTables, 5,
 				SpringLayout.WEST, centerBox);
 		layout.putConstraint(SpringLayout.WEST, tables, 5, SpringLayout.EAST,
 				numOfTables);
 		layout.putConstraint(SpringLayout.NORTH, pricePH, 5,
 				SpringLayout.SOUTH, numOfTables);
 		layout.putConstraint(SpringLayout.WEST, pricePH, 5, SpringLayout.WEST,
 				centerBox);
 		layout.putConstraint(SpringLayout.NORTH, price, 5, SpringLayout.SOUTH,
 				numOfTables);
 		layout.putConstraint(SpringLayout.WEST, price, 5, SpringLayout.EAST,
 				pricePH);
 		layout.putConstraint(SpringLayout.EAST, centerBox, 5,
 				SpringLayout.EAST, price);
 		layout.putConstraint(SpringLayout.SOUTH, centerBox, 5,
 				SpringLayout.SOUTH, price);
 		this.add(centerBox);
 		this.setResizable(false);
 
 		JPanel bottomBar = new JPanel();
 		this.add(bottomBar, BorderLayout.SOUTH);
 
 		JButton apply = new JButton("Apply");
 		apply.setActionCommand("apply");
 		apply.addActionListener(this);
 		bottomBar.add(apply);
 		JButton close = new JButton("Close");
 		close.setActionCommand("close");
 		close.addActionListener(this);
 		bottomBar.add(close);
 		this.pack();
 
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if ("close".equals(e.getActionCommand())) {
 			this.dispose();
 		}
 		if ("apply".equals(e.getActionCommand())) {
 			boolean success = true;
			if (!(tables.getText().equals(main.getNumberOfTables()))) {
				try {
					if (!(Integer.parseInt(tables.getText()) < 0)) {
						main.newTables(Integer.parseInt(tables.getText()));
					} else {
						JOptionPane.showMessageDialog(this,
								"Number of tables must be positive.");
						success = false;
					}
				} catch (NumberFormatException ex) {
					success = false;
					JOptionPane.showMessageDialog(this,
							"Number of tables must be an integer.");
				}
			}
 			if (!price.getText().equals("")) {
 				try {
 					main.setSnookerAllPrices(Double.parseDouble(price.getText()));
 				} catch (NumberFormatException ex) {
 					success = false;
 					JOptionPane.showMessageDialog(this,
 							"Price has to be a number.");
 				}
 			}
 			if (success == true) {
 				this.dispose();
 			}
 		}
 
 	}
 
 }
