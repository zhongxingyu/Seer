 package GUI;
 
 import java.awt.BorderLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.ArrayList;
 
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.border.TitledBorder;
 
 import com.mongodb.DBObject;
 
 import GUI.MainGUI.SearchBy;
 import main.AccessTracker;
 import main.InputReader;
 import main.Tool;
 import main.User;
 
 public class UserCheckoutToolPanel extends ContentPanel {
 	
 	private JComboBox<SearchBy> searchParameter;
 	private JLabel enterLabel;
 	private JButton checkoutToolsButton;
 	private JButton goButton;
 	private ButtonListener buttonListener;
 	private ComboBoxListener comboBoxListener;
 	private CheckBoxListener checkBoxListener;
 	private JTextField searchField;
 	private JPanel resultsPanel;
 	private SearchBy searchBy;
 	private ArrayList<String> toolsToCheckout;
 	private JPanel home;
 	
 	public UserCheckoutToolPanel() {
 		super("Checkout Tools");
 		
 		this.home = home;
 		toolsToCheckout = new ArrayList<String>();
 		buttonListener = new ButtonListener();
 		comboBoxListener = new ComboBoxListener();
 		checkBoxListener = new CheckBoxListener();
 		
 		searchField = new JTextField();
 		JLabel searchLabel = new JLabel("Search By:");
 		
 		searchLabel.setFont(buttonFont);
 		
 		searchParameter = new JComboBox<SearchBy>();
 		searchParameter.setFont(textFont);
 		
 		searchParameter.addItem(SearchBy.NAME);
 		searchParameter.addItem(SearchBy.UPC);
 		
 		searchParameter.addActionListener(comboBoxListener);
 		
 		enterLabel = new JLabel("Enter Name:");
 		searchBy = SearchBy.NAME;
 		goButton = new JButton("Go");
 		
 		goButton.addActionListener(buttonListener);
 		
 		enterLabel.setFont(buttonFont);
 		searchField.setFont(textFont);
 		goButton.setFont(buttonFont);
 		
 		JPanel parameterPanel = new JPanel(new GridBagLayout());
 		JPanel searchPanel = new JPanel(new GridLayout(1, 3));
 		
 		searchPanel.add(enterLabel);
 		searchPanel.add(searchField);
 		searchPanel.add(goButton);
 		
 		resultsPanel = new JPanel();
 		resultsPanel.setBorder(new TitledBorder("Search Results"));
 		resultsPanel.setLayout(new GridLayout(0,1));
 		
 		checkoutToolsButton = new JButton("Checkout Tools");
 		checkoutToolsButton.setFont(buttonFont);
 		checkoutToolsButton.addActionListener(new ButtonListener());
 		
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.weightx = 0.27;
 		c.gridx = 0;
 		c.gridy = 0;
 		parameterPanel.add(searchLabel, c);
 		
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.weightx = 0.73;
 		c.gridx = 1;
 		c.gridy = 0;
 		parameterPanel.add(searchParameter, c);
 		
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.weightx = 0.1;
 		c.gridx = 0;
 		c.gridy = 0;
 		add(new JPanel(), c);
 		
 		c.fill = GridBagConstraints.NONE;
 		c.weightx = 0.8;
 		c.weighty = 0.1;
 		c.gridx = 1;
 		c.gridy = 0;
 		add(title, c);
 		
 		c.fill = GridBagConstraints.NONE;
 		c.weightx = 0.1;
 		c.gridx = 2;
 		c.gridy = 0;
 		add(new JPanel(), c);
 		
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.weighty = 0.1;
 		c.gridx = 1;
 		c.gridy = 1;
 		add(parameterPanel, c);
 		
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.weighty = 0.1;
 		c.gridx = 1;
 		c.gridy = 2;
 		add(searchPanel, c);
 		
 		c.fill = GridBagConstraints.BOTH;
 		c.weighty = 0.5;
 		c.gridx = 1;
 		c.gridy = 3;
 		add(resultsPanel, c);
 		
 		c.fill = GridBagConstraints.BOTH;
 		c.weighty = 0.1;
 		c.gridx = 1;
 		c.gridy = 4;
 		c.gridwidth = 1;
 		add(checkoutToolsButton, c);
 		
 		c.weighty = 0.1;
 		c.gridy = 5;
 		add(new JPanel(), c);
 		
 	}
 	
 	public void showConfirmPopup() {
 		if (toolsToCheckout.size() == 0){
 			JOptionPane.showConfirmDialog(this, "No users are selected.");
 			return;
 		}
 	}
 
 	public void clear(){
 		resultsPanel.removeAll();
 		toolsToCheckout.clear();
 		searchField.setText("");
 	}
 
 	public void findTools(){
 		ArrayList<DBObject> returnedTools;
 		switch (searchBy){
 		case UPC:
 			String upc = searchField.getText();
 			clear();
 
			returnedTools = Driver.getAccessTracker().searchDatabase("Tools", "upc", upc);
 
 			for (DBObject o : returnedTools) {
 				String show = (String) o.get("name") + " (" + (String) o.get("upc") + ")" ;
 				JCheckBox cb = new JCheckBox(show); 
 				cb.setName((String) o.get("upc"));
 				cb.setFont(textFont);
 				cb.addItemListener(checkBoxListener);
 				resultsPanel.add(cb, BorderLayout.WEST);
 			}
 			
 			break;
 		case NAME:
 			String name = searchField.getText();
 			clear();
			returnedTools = Driver.getAccessTracker().searchDatabase("Tools", "name", name);
 			System.out.println(returnedTools);
 			for (DBObject o : returnedTools) {
 				String show = (String) o.get("name") + " (" + (String) o.get("upc") + ")" ;
 				JCheckBox cb = new JCheckBox(show); 
 				cb.setName((String) o.get("upc"));
 				cb.setFont(textFont);
 				cb.addItemListener(checkBoxListener);
 				resultsPanel.add(cb, BorderLayout.WEST);
 			}
 			break;
 		default:
 			// do nothing
 		}
 	}
 	
 	private class ComboBoxListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			if (e.getSource() == searchParameter) {
 				SearchBy parameter = (SearchBy) searchParameter.getSelectedItem();
 				switch (parameter){
 				case UPC:
 					searchBy = SearchBy.UPC;
 					enterLabel.setText("Enter upc:");
 					break;
 				case NAME:
 					searchBy = SearchBy.NAME;
 					enterLabel.setText("Enter Name:");
 					break;
 				default:
 					break;
 				}
 			}
 		}
 	}
 	
 	private class ButtonListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			if (e.getSource() == checkoutToolsButton) {
 				showConfirmPopup();
 				for (String upc : toolsToCheckout){
 					System.out.println(upc);
 					Tool t = Driver.getAccessTracker().getToolByUPC(upc);
 					System.out.println(t);
 					Driver.getAccessTracker().getCurrentUser().checkoutTool(t);
 				}
 				
 				UserGUI.returnHome();
 			
 			} else if ( e.getSource() == goButton ) {
 				findTools();
 			}
 		}
 	}
 
 	private class CheckBoxListener implements ItemListener {
 		@Override
 		public void itemStateChanged(ItemEvent e) {
 			JCheckBox check = (JCheckBox) e.getSource();
 			String upc = check.getName();
 
 			switch (e.getStateChange()){
 			case ItemEvent.SELECTED:
 				toolsToCheckout.add(upc);
 				break;
 			case ItemEvent.DESELECTED:
 				if (toolsToCheckout.contains(upc)){
 					toolsToCheckout.remove(upc);
 				}
 				break;
 			}
 		}
 	}
 }
 
