 package gui.manager.forms;
 
 import gui.Card;
 
 import java.awt.CardLayout;
 import java.awt.Container;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 import java.util.LinkedList;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTextField;
 
 import system.InventorySystems;
 import system.Item;
 import system.Store;
 import system.TransactionE;
 import system.TransactionItem;
 import system.dao.TransactionDao;
 
 import com.jgoodies.forms.factories.FormFactory;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.RowSpec;
 
 public class ItemSummaryCard 
 {
 	private JSplitPane reportpane;
 	private Container con;
 	private JTextField textFieldItemId;
 	private JTextField textFieldQuantitySold;
 	private JTextField textFieldAmountMade;
 	private JTextField textFieldItemName;
 	
 	private JTextField textFieldStoreId;
 	private JTextField textFieldStock;
 	
 	private JPanel panelItemDetails;
 	
 	private LinkedList<JTextField> itemDetails;
 	
 	public JSplitPane getCard(Container con)
 	{
 		if(reportpane==null)
 		{
 			reportpane = new JSplitPane();
 			this.con = con;
 			init();
 		}
 		return reportpane;
 	}
 	
 	public void init()
 	{
 		reportpane = new JSplitPane();
 		JScrollPane scrollPane = new JScrollPane();
 		reportpane.setRightComponent(scrollPane);
 		itemDetails = new LinkedList<JTextField>();
 		panelItemDetails = new JPanel();
 		scrollPane.setViewportView(panelItemDetails);
 		panelItemDetails.setLayout(new FormLayout(
 		new ColumnSpec[] 
         {
 			FormFactory.RELATED_GAP_COLSPEC,
 			FormFactory.MIN_COLSPEC,
 			FormFactory.RELATED_GAP_COLSPEC,
 			ColumnSpec.decode("min:grow"),
 			FormFactory.RELATED_GAP_COLSPEC,
 			ColumnSpec.decode("default:grow"),
 		},
 		new RowSpec[] 
         {
 			FormFactory.RELATED_GAP_ROWSPEC,
 			FormFactory.DEFAULT_ROWSPEC,
 			FormFactory.RELATED_GAP_ROWSPEC,
 			FormFactory.DEFAULT_ROWSPEC,
 			FormFactory.RELATED_GAP_ROWSPEC,
 			FormFactory.DEFAULT_ROWSPEC,
 			FormFactory.RELATED_GAP_ROWSPEC,
 			FormFactory.DEFAULT_ROWSPEC,
 			FormFactory.RELATED_GAP_ROWSPEC,
 			FormFactory.DEFAULT_ROWSPEC,
 			FormFactory.RELATED_GAP_ROWSPEC,
 			FormFactory.DEFAULT_ROWSPEC,
 			FormFactory.RELATED_GAP_ROWSPEC,
 			FormFactory.DEFAULT_ROWSPEC,
 			FormFactory.RELATED_GAP_ROWSPEC,
 			FormFactory.DEFAULT_ROWSPEC,
 			FormFactory.RELATED_GAP_ROWSPEC,
 			FormFactory.DEFAULT_ROWSPEC,
 			FormFactory.RELATED_GAP_ROWSPEC,
 			FormFactory.DEFAULT_ROWSPEC,
 			FormFactory.RELATED_GAP_ROWSPEC,
 			FormFactory.DEFAULT_ROWSPEC,
 			FormFactory.RELATED_GAP_ROWSPEC,
 			FormFactory.DEFAULT_ROWSPEC,
 			FormFactory.RELATED_GAP_ROWSPEC,
 			FormFactory.DEFAULT_ROWSPEC,
 		}));
 		
 		JLabel lblQuantity = new JLabel("Quantity");
 		panelItemDetails.add(lblQuantity, "2, 2");
 		
 		JLabel lblCustomer = new JLabel("Customer");
 		panelItemDetails.add(lblCustomer, "4, 2");
 		
 		JLabel lblPrice = new JLabel("Price");
 		panelItemDetails.add(lblPrice, "6, 2");
 		
 		JPanel panel = new JPanel();
 		reportpane.setLeftComponent(panel);
 		panel.setLayout(new FormLayout(
 		new ColumnSpec[] 
         {
 			FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
 			FormFactory.MIN_COLSPEC,
 			FormFactory.RELATED_GAP_COLSPEC,
 			ColumnSpec.decode("max(55dlu;default):grow"),
 		},
 		new RowSpec[] 
         {
 			FormFactory.LINE_GAP_ROWSPEC,
 			RowSpec.decode("23px"),
 			FormFactory.RELATED_GAP_ROWSPEC,
 			FormFactory.DEFAULT_ROWSPEC,
 			FormFactory.RELATED_GAP_ROWSPEC,
 			FormFactory.DEFAULT_ROWSPEC,
 			FormFactory.RELATED_GAP_ROWSPEC,
 			FormFactory.DEFAULT_ROWSPEC,
 			FormFactory.RELATED_GAP_ROWSPEC,
 			FormFactory.DEFAULT_ROWSPEC,
 			FormFactory.RELATED_GAP_ROWSPEC,
 			FormFactory.DEFAULT_ROWSPEC,
 			FormFactory.RELATED_GAP_ROWSPEC,
 			FormFactory.DEFAULT_ROWSPEC,
 			FormFactory.RELATED_GAP_ROWSPEC,
 			FormFactory.DEFAULT_ROWSPEC,
 			FormFactory.RELATED_GAP_ROWSPEC,
 			FormFactory.DEFAULT_ROWSPEC,
 		}));
 		
 		JLabel lblItemId = new JLabel("Item ID:");
 		panel.add(lblItemId, "2, 2, right, default");
 		
 		textFieldItemId = new JTextField();
 		panel.add(textFieldItemId, "4, 2, fill, default");
 		textFieldItemId.setColumns(10);
 		
 		JLabel storeId = new JLabel("Store ID:");
 		panel.add(storeId, "2, 4, right, default");
 		
 		textFieldStoreId = new JTextField();
 		panel.add(textFieldStoreId, "4, 4, fill, default");
 		textFieldStoreId.setColumns(10);
 		
 		JButton btnInquire = new JButton("Inquire");
 		btnInquire.addMouseListener(new MouseAdapter() 
 		{
 			@Override
 			public void mousePressed(MouseEvent arg0) 
 			{
 				String itemCode = textFieldItemId.getText();
 				Item i = InventorySystems.getSystem().getItem(itemCode);
 				//ItemDao itedao = new ItemDao();
 				//Item i = itedao.get(itemCode);
 				if(i == null)
 				{
 					JOptionPane.showMessageDialog(reportpane, "Item not found.");
 					return;
 				}
 				textFieldItemName.setText(i.getItemName());
 				int quantitySold = 0;
 				double amountMade = 0;
 				
 				for(JTextField j : itemDetails)
 				{
 					panelItemDetails.remove(j);
 				}
 				itemDetails = new LinkedList<JTextField>();
 
 				Store query = null;
 				if(!textFieldStoreId.getText().equals(""))
 				{
 					int storeId = 0;
 					try
 					{
 						storeId = Integer.parseInt(textFieldStoreId.getText());
 					}
 					catch(NumberFormatException nfe)
 					{
 						JOptionPane.showMessageDialog(reportpane, "Specified Store ID is in an improper format.");
 						return;
 					}
 					query = InventorySystems.getSystem().getStore(storeId);
 					if(query == null)
 					{
 						JOptionPane.showMessageDialog(reportpane, "Store not found.");
 						return;
 					}
 				}
 				
 				int y = 4;
 				TransactionDao transdao = new TransactionDao();
 				ArrayList<TransactionE> q = (ArrayList<TransactionE>) transdao.getTransactions();
 				//if(query != null && !query.equals(s))
 				//if(query != null && !query.equals(s))
 				//{
 				//	continue;
 				//}
 				for(TransactionE t : q)
 				{
					if(query == null || t.getStore().getId()==query.getId()){
 						
 						for(TransactionItem ti : t.getItemsSold())
 						{
 							if(ti.getItem().equals(i))
 							{
 								JTextField quantity = new JTextField();
 								JTextField customer = new JTextField();
 								JTextField price = new JTextField();
 								
 								quantity.setEditable(false);
 								customer.setEditable(false);
 								price.setEditable(false);
 								
 								quantity.setColumns(10);
 								customer.setColumns(10);
 								price.setColumns(10);
 								
 								quantity.setText(ti.getQuantity() + "");
 								customer.setText(t.getCustomer().getId() + ": " + t.getCustomer().getFirstName() + " " + t.getCustomer().getLastName() + ", " + t.getCustomer().getAge() + ", " + t.getCustomer().getGender());
 								price.setText(ti.getPrice() + "");
 	
 								panelItemDetails.add(quantity, "2, " + y + ", fill, default");
 								panelItemDetails.add(customer, "4, " + y + ", fill, default");
 								panelItemDetails.add(price, "6, " + y + ", fill, default");
 								
 								itemDetails.add(quantity);
 								itemDetails.add(customer);
 								itemDetails.add(price);
 								
 								quantitySold += ti.getQuantity();
 								amountMade += ti.getQuantity() * ti.getPrice();
 								
 								y+=2;
 							}
 						}
 					}
 				}
 				
 				panelItemDetails.revalidate();
 				textFieldQuantitySold.setText(quantitySold + "");
 				textFieldAmountMade.setText(amountMade + "");
 				if(query != null)
 					textFieldStock.setText(query.checkInventory(i) + "");
 				else
 					textFieldStock.setText("");
 			}
 		});
 		panel.add(btnInquire, "2, 6");
 		
 		JLabel lblItemName = new JLabel("Item Name:");
 		panel.add(lblItemName, "2, 8, right, default");
 		
 		textFieldItemName = new JTextField();
 		textFieldItemName.setEditable(false);
 		panel.add(textFieldItemName, "4, 8, fill, default");
 		textFieldItemName.setColumns(10);
 		
 		JLabel lblQuantitySold = new JLabel("Quantity Sold:");
 		panel.add(lblQuantitySold, "2, 10, right, default");
 		
 		textFieldQuantitySold = new JTextField();
 		textFieldQuantitySold.setEditable(false);
 		panel.add(textFieldQuantitySold, "4, 10, fill, default");
 		textFieldQuantitySold.setColumns(10);
 		
 		JLabel lblAmountMade = new JLabel("Amount Made:");
 		panel.add(lblAmountMade, "2, 12, right, default");
 		
 		textFieldAmountMade = new JTextField();
 		textFieldAmountMade.setEditable(false);
 		panel.add(textFieldAmountMade, "4, 12, fill, default");
 		textFieldAmountMade.setColumns(10);
 		
 		JLabel lblStock = new JLabel("Available Stock in Store:");
 		panel.add(lblStock, "2, 14, right, default");
 		
 		textFieldStock = new JTextField();
 		textFieldStock.setEditable(false);
 		panel.add(textFieldStock, "4, 14, fill, default");
 		textFieldStock.setColumns(10);
 		
 		
 		JButton btnCancel = new JButton("Cancel");
 		btnCancel.addMouseListener(new MouseAdapter() 
 		{
 			@Override
 			public void mousePressed(MouseEvent arg0) 
 			{
 				resetFields();
 				CardLayout cl = (CardLayout) con.getLayout();
 				cl.show(con, Card.MANAGER.getLabel());
 			}
 		});
 		panel.add(btnCancel, "2, 16");
 		
 	}
 	
 	public void resetFields()
 	{
 		textFieldItemId.setText("");
 		textFieldQuantitySold.setText("");
 		textFieldAmountMade.setText("");
 		textFieldItemName.setText("");
 		textFieldStoreId.setText("");
 		textFieldStock.setText("");
 		for(JTextField j : itemDetails)
 		{
 			panelItemDetails.remove(j);
 		}
 		itemDetails = new LinkedList<JTextField>();
 	}
 }
