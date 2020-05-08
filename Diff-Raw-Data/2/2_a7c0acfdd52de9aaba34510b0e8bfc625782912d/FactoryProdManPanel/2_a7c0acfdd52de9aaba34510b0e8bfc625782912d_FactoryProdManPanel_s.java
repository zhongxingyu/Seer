 /*
 
 author: Joey Huang
 Last edited: 11/4/12 5:24pm
 */
 package factory.swing;
 import java.util.*;
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 
 
 public class FactoryProdManPanel extends JPanel implements ActionListener {
 	JComboBox kitNameBox;
 	JSpinner spinner;
 	JButton submitButton;
 	JTextArea messageBox;
 	
 	public FactoryProdManPanel() { // manager has arraylist of kitnames available
 		kitNameBox = new JComboBox();
 		//for (int i = 0; i < kitList.size(); i++)
 		//	kitNameBox.addItem(kitList.get(i));
 		kitNameBox.setPreferredSize(new Dimension(225,25));
 		
 		setLayout(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 		c.gridy = 1;
 		c.gridx = 1;
 		c.gridwidth = 4;
 		c.gridheight = 1;
 		c.insets = new Insets(0,0,25,0); 
 		add(new JLabel("Factory Production Manager"),c);
 		
 		
 		c.insets = new Insets(10,0,10,0);
 		c.gridy = 2;
 		c.anchor = GridBagConstraints.LINE_START;
 		add(new JLabel("Submit New Batch Order:"),c);
 		
 		c.gridy = 3;
 		c.anchor = GridBagConstraints.CENTER;
 		/*for(int i = 0; i < kitList.size();i++) {
 			kitNameBox.setSelectedIndex(i);
 			kitNameBox.addActionListener(this);
 		}*/
 		kitNameBox.setSelectedIndex(0);
 		add(kitNameBox,c);
 
 		c.anchor = GridBagConstraints.LINE_END;
 		c.gridy = 4;
 		c.gridx = 2;
 		c.gridwidth = 1;
 		add(new JLabel("Quantity"),c);
 		
 		c.gridx = 4;
 		SpinnerNumberModel qntyModel = new SpinnerNumberModel(1,1,500,1);
 		spinner = new JSpinner(qntyModel);
 		add(spinner,c);
 		
 		c.gridy = 5;
 		c.gridx = 4;
 		submitButton = new JButton("Submit");
 		submitButton.addActionListener(this);
 		add(submitButton,c);
 		
 		c.gridy = 6;
 		c.gridx = 1;
 		c.gridwidth = 4;
 		messageBox = new JTextArea("System Messages\n",10,20);
 		add(new JScrollPane(messageBox),c);
 				
 	}
 		
 	public void actionPerformed(ActionEvent ae) {
 		
 		if (ae.getSource().equals(submitButton)) {
 			if (kitNameBox.getSelectedItem() == null)
 				messageBox.append("No kit selected.\n");
 			else
 			messageBox.append("Order Submitted.\n     Details: " + spinner.getValue() + " units of " + (String)kitNameBox.getSelectedItem() + "\n" );
 		}
 	}
 	
 	public void addKit(String kitName) {	
 		kitNameBox.addItem(kitName);	
		kitNameBox.getSelectedItem(kitNameBox.getItemCount()-1).addActionListener(this);
 		kitNameBox.setSelectedIndex(0);
 	}
 	
 	public void removeKit(String kitName) {
 		kitNameBox.removeItem(kitName);
 	}
 }
