 package manager.panel;
 
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JScrollPane;
 import javax.swing.JSpinner;
 import javax.swing.JTextArea;
 import javax.swing.SpinnerNumberModel;
 
 import manager.util.OverlayPanel;
 import factory.KitConfig;
 import factory.Order;
 
 
 
 /**
 *
 * @author Shalynn Ho, Harry Trieu and Matt Zecchini
 */
 
 public class FactoryProductionManagerPanel extends OverlayPanel implements ActionListener {
 	private static final int PANEL_WIDTH = 300;
 	//private final Server server;
 	private KitConfig selectedKit;
 	private int quantity;
 	private JComboBox kitComboBox;
 	private SpinnerNumberModel spinModel;
 	private manager.FactoryProductionManager fpm;
 	
 	
 	private ArrayList<KitConfig> kitConfigs = new ArrayList<KitConfig>();
 	private ArrayList<Order> queue = new ArrayList<Order>();
 	
 	//receives a Client because Harry and Matt are trying to figure out how
 	//we can create the order and send it to the FactoryProductionManager, which will then
 	//send it to the server (rather than having this GUI class send it directly to server)
 	public FactoryProductionManagerPanel(manager.FactoryProductionManager f, int height) {
 		super();
 		fpm = f;
 		setPreferredSize(new Dimension(PANEL_WIDTH, height));
 		setMinimumSize(new Dimension(PANEL_WIDTH, height));
 		setMaximumSize(new Dimension(PANEL_WIDTH, height));
 		
 		setLayout(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 	
 		
 		// TODO: get array of possible kits from Kit Assembly Manager
 		kitComboBox = new JComboBox();
 		// TODO: change this, 
 		kitComboBox.addItem("DEFAULT KIT");
 		kitComboBox.addItem("Option 2");
 		for(int i = 0; i<Utils.Constants.DEFAULT_KITCONFIGS.size();i++)
 			kitComboBox.addItem(Utils.Constants.DEFAULT_KITCONFIGS.get(i).getName());
 		c.gridx = 0;
 		c.gridy = 0;
 		c.anchor = GridBagConstraints.PAGE_START;
 		add(kitComboBox, c);
 		
 		spinModel = new SpinnerNumberModel(0, 0, 1000, 1);
 		JSpinner numSpinner = new JSpinner(spinModel);
 		c.gridx = 0;
 		c.gridy = 1;
 		c.insets = new Insets(50,0,0,0); //Top Padding
 		c.anchor = GridBagConstraints.CENTER;
 		add(numSpinner, c);
 		
 		JButton orderButton = new JButton("ORDER KITS");
 		orderButton.addActionListener(this);
 		c.gridx = 0;
 		c.gridy = 2;
 		c.anchor = GridBagConstraints.PAGE_END;
 		add(orderButton, c);
 				
 		JTextArea kitSchedule = new JTextArea();
 		kitSchedule.setColumns(20);
 		kitSchedule.setRows(20);
 		kitSchedule.setLineWrap(true);
 		kitSchedule.setEditable(false);
 		kitSchedule.setWrapStyleWord(true);
 		JScrollPane scrollPane = new JScrollPane(kitSchedule);
 		c.gridx = 0;
 		c.gridy = 3;
 		c.gridwidth = GridBagConstraints.REMAINDER;
 		c.gridheight = GridBagConstraints.REMAINDER;
 		c.anchor = GridBagConstraints.PAGE_END;
 		add(scrollPane, c);
 		for(int i = 0; i<queue.size();i++)
 		{	
			JLabel queueItem = new JLabel(queue.get(i).kitConfig + " - Qty. "+ queue.get(i).numKits);
 			scrollPane.add(queueItem);
 		}
 				
 	}
 
 
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		
 		//For loop iterates through ArrayList of KitConfigs to find a match between the selected
 		//ComboBoxItem (presented as a String) and the actual KitConfig with that String. 
 		//Then it sets variable selectedKit equal to that kit, which allows an order 
 		//to be created and sent to server
 		for(int i = 0; i<Utils.Constants.DEFAULT_KITCONFIGS.size();i++)
 		{
 			if(Utils.Constants.DEFAULT_KITCONFIGS.get(i).getName().equals(kitComboBox.getSelectedItem()))
 				selectedKit = Utils.Constants.DEFAULT_KITCONFIGS.get(i);
 		}
 		
 		//Set variable quantity equal to number user enters in SpinModel
 		quantity = (Integer)spinModel.getNumber();
 		
 		//Creates new Order and passes it the kit the User selects and the quantity to make
 		Order temp = new Order(selectedKit, quantity);
 		
 		//sends message to FCS with order info
 		fpm.createOrder(temp);
 		
 		
 		//testing purposes only -- feel free to comment out or delete print statement below
		System.out.println("Order Details: " + temp.kitConfig + " " + temp.numKits);
 	
 		
 	}
 	
 	
 	public void updateKitConfigs(ArrayList<KitConfig> kc) {
 		kitConfigs = kc;
 	}
 	
 	public void updateOrders(ArrayList<Order> o) {
 		queue = o;
 	}
 }
