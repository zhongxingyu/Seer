 package auctionsniper.ui;
 
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JButton;
 import javax.swing.JFormattedTextField;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 
 import auctionsniper.Item;
 import auctionsniper.SniperPortfolio;
 import auctionsniper.UserRequestListener;
 
 public class MainWindow extends JFrame {
 	public static final String NAME = "Auction Sniper Main";
 	public static final String TITLE = "Auction Sniper";
 	
 	public static final String SNIPER_TABLE_NAME = "sniper_table";
 	public static final String NEW_ITEM_ID_NAME = "new_item_input";
 	public static final String STOP_PRICE_NAME = "stop_price_input";
 	public static final String JOIN_BUTTON_NAME = "join_button";
 	
 	private List<UserRequestListener> requestListeners = new ArrayList<UserRequestListener>();
 
 	public MainWindow(SniperPortfolio portfolio) {
 		super(TITLE);
 		setName(NAME);
 		
 		fillContentPane(makeControls(), createSniperTable(portfolio));
 		
 		pack(); // fit to prefered size
 		setVisible(true);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	}
 	
 	private void fillContentPane(JPanel controls, JTable snipersTable) {
 	    final Container contentPane = getContentPane(); 
 	    contentPane.setLayout(new BorderLayout()); 
 	    contentPane.add(controls, BorderLayout.NORTH); 
 	    contentPane.add(new JScrollPane(snipersTable), BorderLayout.CENTER); 
 	}
 
 	private JPanel makeControls() {
 		JPanel controls = new JPanel(new FlowLayout());
 		final JTextField itemIdField = itemIdTextField(controls);
 		final JFormattedTextField stopPriceField = stopPriceField(controls);
 
 		JButton joinAuctionButton = new JButton("Join Auction");
 		joinAuctionButton.setName(JOIN_BUTTON_NAME);
 		joinAuctionButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				for (UserRequestListener requestListener : requestListeners)
 					requestListener.joinAuction(new Item(itemId(), stopPrice()));
 			}
 
 			private String itemId() {
 				return itemIdField.getText();
 			}
 
 			private Integer stopPrice() {
 				Object priceValue = stopPriceField.getValue();
				return priceValue == null ? Integer.MAX_VALUE : ((Number) priceValue).intValue();
 			}
 		});
 		controls.add(joinAuctionButton);
 		return controls;
 	}
 	
 	private JFormattedTextField stopPriceField(JPanel controls) {
 		JFormattedTextField stopPriceField = new JFormattedTextField(NumberFormat.getIntegerInstance());
 		stopPriceField.setColumns(10);
 		stopPriceField.setName(STOP_PRICE_NAME);
 		controls.add(new JLabel("Stop Price"));
 		controls.add(stopPriceField);
 		return stopPriceField;
 	}
 
 	private JTextField itemIdTextField(JPanel controls) {
 		JTextField itemIdField = new JTextField();
 		itemIdField.setColumns(20);
 		itemIdField.setName(NEW_ITEM_ID_NAME);
 		controls.add(new JLabel("Item id"));
 		controls.add(itemIdField);
 		return itemIdField;
 	}
 	
 	private JTable createSniperTable(SniperPortfolio portfolio) {
 		SnipersTableModel snipers = new SnipersTableModel();
 		portfolio.addListener(snipers);
 		JTable table = new JTable(snipers);
 		table.setName(SNIPER_TABLE_NAME);
 		return table;
 	}
 
 	public void addUserRequestListener(UserRequestListener userRequestListener) {
 		requestListeners.add(userRequestListener);
 	}
 }
