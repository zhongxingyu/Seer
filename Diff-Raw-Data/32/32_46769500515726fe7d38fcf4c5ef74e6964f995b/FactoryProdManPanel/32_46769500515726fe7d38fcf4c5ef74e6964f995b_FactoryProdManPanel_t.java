 /*
 
 author: Joey Huang
Last edited: 11/15/12 12:02pm
 */
 package factory.swing;
 import java.util.*;
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableColumn;
 
 import factory.managers.*;
 
 
 public class FactoryProdManPanel extends JPanel implements ActionListener {
 	JComboBox kitNameBox; // contain String names of saved kit configurations
 	JSpinner spinner; // quantity of kits to produce
 	JButton submitButton; // submit order
 	JTextArea messageBox; // submission confirmations
 	FactoryProductionManager factoryProductionManager;
 	JTabbedPane tabContainer;
 	JPanel newOrderPanel; // new order panel
 
 	
 	JPanel schedulePanel; // production schedule panel
 	DefaultTableModel model; 
 	JTable table;
 	JButton stopFactory; // terminate all factory operations - close program
 	
 	public FactoryProdManPanel() { 
 		newOrderPanel = new JPanel();
 		kitNameBox = new JComboBox();
 		kitNameBox.setPreferredSize(new Dimension(225,25));
 		
 		newOrderPanel.setLayout(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 		c.gridy = 1;
 		c.gridx = 1;
 		c.gridwidth = 4;
 		c.gridheight = 1;
 		c.insets = new Insets(0,0,25,0); 
 		newOrderPanel.add(new JLabel("Factory Production Manager"),c);
 		
 		
 		c.insets = new Insets(10,0,10,0);
 		c.gridy = 2;
 		c.anchor = GridBagConstraints.LINE_START;
 		newOrderPanel.add(new JLabel("Submit New Batch Order:"),c);
 		
 		c.gridy = 3;
 		c.anchor = GridBagConstraints.CENTER;
 		newOrderPanel.add(kitNameBox,c);
 
 		c.anchor = GridBagConstraints.LINE_END;
 		c.gridy = 4;
 		c.gridx = 2;
 		c.gridwidth = 1;
 		newOrderPanel.add(new JLabel("Quantity"),c);
 		
 		c.gridx = 4;
 		SpinnerNumberModel qntyModel = new SpinnerNumberModel(1,1,500,1);
 		spinner = new JSpinner(qntyModel);
 		newOrderPanel.add(spinner,c);
 		
 		c.gridy = 5;
 		c.gridx = 4;
 		submitButton = new JButton("Submit");
 		submitButton.addActionListener(this);
 		newOrderPanel.add(submitButton,c);
 		
 		c.gridy = 6;
 		c.gridx = 1;
 		c.gridwidth = 4;
 		messageBox = new JTextArea("System Messages\n",10,20);
 		newOrderPanel.add(new JScrollPane(messageBox),c);
 		
 		
 		// tab 2 production schedule
 		schedulePanel = new JPanel();
 		model = new DefaultTableModel();
 
 		model.addColumn("No.");
 		model.addColumn("Kit");
 		model.addColumn("Qnty");
 
 		table=new JTable(model);
 		
 		
 		TableColumn column = null;
 		for (int i = 0; i < 3; i++) {
 		    column = table.getColumnModel().getColumn(i);
 		    if (i == 0) {
 		        column.setPreferredWidth(30); 
 		    } else if (i ==1) {
 		        column.setPreferredWidth(200);
 		    } else {
 		    	column.setPreferredWidth(30);
 		    }
 		}
 	
 		schedulePanel.setLayout(new BorderLayout());
 		JLabel label = new JLabel("<html><p style=\"margin:30px 0 30px 0;\">Production Schedule</html>");
 		label.setHorizontalAlignment(SwingConstants.CENTER);
 		schedulePanel.add(label,BorderLayout.NORTH);
 		
 		JPanel container = new JPanel();
 		container.setPreferredSize(new Dimension(300,400));
 		
 		JScrollPane scrollPane = new JScrollPane(table);
 		scrollPane.setPreferredSize(new Dimension(260,400));
 		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 		container.add(scrollPane);
 		schedulePanel.add(container,BorderLayout.CENTER);
 		
 		
 		stopFactory = new JButton("Terminate All Operations");
 		stopFactory.addActionListener(this);
 		schedulePanel.add(stopFactory, BorderLayout.SOUTH);
 		
 		tabContainer = new JTabbedPane();
 		tabContainer.addTab("New Order",newOrderPanel);
 		tabContainer.addTab("Schedule",schedulePanel);
 		tabContainer.setPreferredSize(new Dimension(290,710));
 		add(tabContainer);
 	}
 		
 	public void actionPerformed(ActionEvent ae) {
 		
 		if (ae.getSource() == submitButton) {		// print messages to be displayed in messageBox
 			if (kitNameBox.getSelectedItem() == null)
 				messageBox.append("No kit selected.\n");
 			else {
				String name = (String)kitNameBox.getSelectedItem();
				String qnty = spinner.getValue().toString();
 				
 				String set = new String("");
				set = "fpm fcsa cmd makekits " + qnty + " " + name;	
			//	factoryProductionManager.sendCommand(set);
 					
				//Object[] rowData = {i+1,"Kit"+(i+1),"10"};
				Object[] rowData = {model.getRowCount()+1,name,qnty};
				model.insertRow(model.getRowCount(),rowData);
				
			messageBox.append("Order Submitted.\n     Details: " + qnty + " units of " + name + "\n" );
 			}
 		}
 		else if (ae.getSource() == stopFactory) { // message that initiates factory shutdown
 			messageBox.append("Terminating all operations...\n");
 			String set = new String("");
 			set = "fpm mcs cmd stopfactory";
 			factoryProductionManager.sendCommand(set);
 		}
 	}
 	
 	public void addKit(String kitName) {	//add new kit name (String) to Jcombobox list - received from kit manager
 		kitNameBox.addItem(kitName);	
		//((JComboBox) kitNameBox.getItemAt(kitNameBox.getItemCount()-1)).addActionListener(this);
 		kitNameBox.setSelectedIndex(0);
 	}
 	
 	public void removeKit(String kitName) { // remove kit from list - received from kit manager
 		kitNameBox.removeItem(kitName);
 	}
 
 	public void setManager(FactoryProductionManager fpm) {
 		factoryProductionManager = fpm;
 	}
 	
 	public void kitProduced() { // update kits remaining
 		String numstr = (String)model.getValueAt(0,2);
 		int num = Integer.parseInt(numstr);
 		if(num >0) {
 			num--;
 			model.setValueAt(Integer.toString(num), 0, 2);
 		}
 		
 		if (num == 0) {
 			model.removeRow(0);
 			for (int i = 0; i < model.getRowCount();i++) {
 				model.setValueAt((i+1)+"",i,0);
 			}
 			table.revalidate();
 		}
 	}
 }
