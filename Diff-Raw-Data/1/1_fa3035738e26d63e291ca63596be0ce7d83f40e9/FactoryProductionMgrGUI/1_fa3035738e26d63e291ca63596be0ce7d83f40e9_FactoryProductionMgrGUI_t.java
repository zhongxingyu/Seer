 package GUI;
 
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JScrollPane;
 import javax.swing.JSpinner;
 import javax.swing.JTextArea;
 import javax.swing.SpinnerNumberModel;
 
 import factory.KitConfig;
 import factory.Order;
 
 /**
 *
 *Authorship: Shalynn Ho, Harry Trieu and Matt Zecchini
 */
 
 public class FactoryProductionMgrGUI extends OverlayPanel implements ActionListener {
 	private static final int PANEL_WIDTH = 300;
 	//private final Server server;
 	private KitConfig selectedKit;
 	private int quantity;
 	private JComboBox kitComboBox;
 	private SpinnerNumberModel spinModel;
 	
 	private ArrayList<KitConfig> kitConfigs = new ArrayList<KitConfig>();
 	private ArrayList<Order> queue = new ArrayList<Order>();
 	
 	public FactoryProductionMgrGUI(int height) {
 		super();
 		setPreferredSize(new Dimension(PANEL_WIDTH, height));
 		setMinimumSize(new Dimension(PANEL_WIDTH, height));
 		setMaximumSize(new Dimension(PANEL_WIDTH, height));
 		
 		setLayout(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 		
 		//test array which will be used to fill JComboBox -- feel free to delete/comment out
 		//Integer[] testArray = {0,1,2,3,4,5,6,7,8,9};
 		
 		// TODO: get array of possible kits from Kit Assembly Manager
 		kitComboBox = new JComboBox();
 		// TODO: change this, 
 		kitComboBox.addItem("DEFAULT KIT");
 		kitComboBox.addItem("Option 2");
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
 				
 	}
 
 
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		// TODO: send message to FCS with order info
 		//selectedKit = kitComboBox.getSelectedItem();
 		quantity = (Integer)spinModel.getNumber();
 		System.out.println("Selected: " + kitComboBox.getSelectedItem());
 		System.out.println("Quant: " + quantity);
 		//Order temp = new Order()
 		//server.sendData(new Request(Constants.FCS_ADD_ORDER, Constants.FCS_TARGET, temp));
 		
 	}
 	
 	public void updateKitConfigs(ArrayList<KitConfig> kc) {
 		kitConfigs = kc;
 	}
 	
 	public void updateOrders(ArrayList<Order> o) {
 		queue = o;
 	}
 }
