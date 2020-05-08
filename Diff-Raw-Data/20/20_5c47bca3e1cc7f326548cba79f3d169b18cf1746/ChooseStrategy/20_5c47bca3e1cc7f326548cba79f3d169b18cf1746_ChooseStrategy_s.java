 package gui;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.*;
 
 public class ChooseStrategy extends JDialog implements ActionListener {
 
 	private JPanel panel = null;
 	private JButton ok = null;
 	private JButton cancel = null;
 	private JTextField threshold = null;
 	private JTextField stockAmount = null;
 	private JComboBox strategy = null;
 	private String[] strategies = {"Momentum", "Mean Reversion"};
 	private int thresholdno = 5; //default
 	private int stockNumber = 10; //default
 	public boolean run = false;
 	
 	public ChooseStrategy(JFrame frame) {
 		super (frame, true);
		setSize(350,150);
 		setResizable(false);
 		setLocationRelativeTo(frame);
 		setTitle("Run Strategy");
 		
 		panel = new JPanel();
 		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
 		getContentPane().add(panel);
 		strategy = new JComboBox(strategies);
 		JPanel pickpanel = new JPanel();
 		pickpanel.add(new JLabel("Select a strategy to run"));
 		pickpanel.add(strategy);
 		panel.add(pickpanel);
 		JPanel pickpanel2 = new JPanel();
 		threshold = new JTextField("5");
 		pickpanel2.add(new JLabel("Threshold: "));
 		pickpanel2.add(threshold);
 		panel.add(pickpanel2);
 		JPanel pickpanel3 = new JPanel();
 		stockAmount = new JTextField("10");
		pickpanel2.add(new JLabel("stock amount: "));
		pickpanel2.add(threshold);
 		panel.add(pickpanel3);
 		JPanel buttons = new JPanel();
 		ok = new JButton("Select");
 		cancel = new JButton("Cancel");
 		buttons.add(ok);
 		buttons.add(cancel);
 		ok.addActionListener(this);
 		cancel.addActionListener(this);
 		panel.add(buttons);
 		//pack();
 		setVisible(true);
 	}
 
 	public int getThreshold () {
 		return thresholdno;
 	}
 	
 	public int getStockAmount () {
 		return thresholdno;
 	}
 	public String getStrategy() {
 		return (String)strategy.getSelectedItem();
 	
 	}
 	public void actionPerformed(ActionEvent e) {
 		if (ok == e.getSource()){
 			try {
 				Integer.parseInt(threshold.getText());
 			} catch (NumberFormatException e1){
 				threshold.setText("Please enter a number");
 				return;
 			}
 			thresholdno = Integer.parseInt(threshold.getText());
 			try {
 				Integer.parseInt(stockAmount.getText());
 			} catch (NumberFormatException e1){
 				stockAmount.setText("Please enter a number");
 				return;
 			}
 			stockNumber = Integer.parseInt(stockAmount.getText());
 			setVisible(false);
 			run = true;
 		} else if (cancel == e.getSource()){
 			setVisible(false);
 			run = false;
 
 		}
 
 	}
 
 }
