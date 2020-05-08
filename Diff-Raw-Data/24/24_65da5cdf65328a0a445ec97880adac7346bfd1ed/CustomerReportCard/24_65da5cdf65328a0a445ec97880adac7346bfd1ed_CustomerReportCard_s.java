 package gui.manager.forms;
 
 import gui.Card;
 
 import java.awt.CardLayout;
 import java.awt.Container;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
import java.util.List;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTextField;
 
 import system.Customer;
 import system.InventorySystems;
import system.Store;
 import system.TransactionE;
 import system.TransactionItem;
 import system.dao.TransactionDao;
 
 import com.jgoodies.forms.factories.FormFactory;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.RowSpec;
 
 public class CustomerReportCard 
 {
 	private JSplitPane reportpane;
 	private Container con;
 	private JTextField textFieldCustomerId;
 	private JComboBox comboBoxTransactions;
 	
 	private HashMap<String, TransactionE> activeTransactions;
 	private LinkedList<JTextField> transactionDetails;
 	
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
 	
 	final JPanel panelTransactionDetails = new JPanel();
 	final JLabel paymentDetails = new JLabel();
 	
 	public void init()
 	{
 		activeTransactions = new HashMap<String, TransactionE>();
 		transactionDetails = new LinkedList<JTextField>();
 		JScrollPane scrollPane = new JScrollPane();
 		reportpane.setRightComponent(scrollPane);
 		
 		scrollPane.setViewportView(panelTransactionDetails);
 		panelTransactionDetails.setLayout(new FormLayout(
 		new ColumnSpec[] 
 		{
 			FormFactory.RELATED_GAP_COLSPEC,
 			FormFactory.MIN_COLSPEC,
 			FormFactory.RELATED_GAP_COLSPEC,
 			ColumnSpec.decode("default:grow"),
 			FormFactory.RELATED_GAP_COLSPEC,
 			ColumnSpec.decode("max(39dlu;default):grow"),
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
 		panelTransactionDetails.add(lblQuantity, "2, 2");
 		
 		JLabel lblItem = new JLabel("Item");
 		panelTransactionDetails.add(lblItem, "4, 2");
 		
 		JLabel lblPrice = new JLabel("Price");
 		panelTransactionDetails.add(lblPrice, "6, 2");
 		
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
 		
 		JLabel lblCustomerId = new JLabel("Customer ID:");
 		panel.add(lblCustomerId, "2, 2, right, default");
 		
 		textFieldCustomerId = new JTextField();
 		panel.add(textFieldCustomerId, "4, 2, fill, default");
 		textFieldCustomerId.setColumns(10);
 		
 		JButton btnInquire = new JButton("Inquire");
 		btnInquire.addMouseListener(new MouseAdapter() 
 		{
 			@Override
 			public void mousePressed(MouseEvent arg0) 
 			{
 				int customerId = 0;
 				try
 				{
 					customerId = Integer.parseInt(textFieldCustomerId.getText());
 				}
 				catch(NumberFormatException nfe)
 				{
 					JOptionPane.showMessageDialog(reportpane, "Specified Customer ID is in an improper format.");
 					return;
 				}
 				Customer c = InventorySystems.getSystem().getCustomer(customerId);
 				if(c == null)
 				{
 					JOptionPane.showMessageDialog(reportpane, "Customer not found.");
 					return;
 				}
 				activeTransactions.clear();
 				comboBoxTransactions.removeAllItems();
 				TransactionDao transdao = new TransactionDao();
 				ArrayList<TransactionE> q = (ArrayList<TransactionE>) transdao.getTransactions();
 				for(TransactionE t : q)
 				{
 					if(t.getCustomer().equals(c))
 					{
 						comboBoxTransactions.addItem(t.getDateTime().toString());
 						activeTransactions.put(t.getDateTime().toString(), t);
 					}
 				}
 				comboBoxTransactions.revalidate();
 				changeTransactionDetails();
 			}
 		});
 		panel.add(btnInquire, "2, 4");
 		
 		JLabel lblSelectTransaction = new JLabel("Select Transaction:");
 		panel.add(lblSelectTransaction, "2, 6, right, default");
 		
 		comboBoxTransactions = new JComboBox();
 		comboBoxTransactions.addItemListener(new ItemListener() 
 		{
 			public void itemStateChanged(ItemEvent arg0) 
 			{
 				changeTransactionDetails();	
 			}
 		});
 		panel.add(comboBoxTransactions, "4, 6, fill, default");
 		
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
 		panel.add(btnCancel, "2, 10");
 		
 	}
 	
 	public void resetFields()
 	{
 		textFieldCustomerId.setText("");
 		activeTransactions.clear();
 		comboBoxTransactions.removeAllItems();
 		paymentDetails.setText("");
 		
 		for(JTextField j : transactionDetails)
 		{
 			panelTransactionDetails.remove(j);
 		}
 		
 		transactionDetails = new LinkedList<JTextField>();
 	}
 	
 	private void changeTransactionDetails() 
 	{
 		TransactionE t;
 		for(JTextField j : transactionDetails)
 		{
 			panelTransactionDetails.remove(j);
 		}
 		paymentDetails.setText("");
 		
 		transactionDetails = new LinkedList<JTextField>();
 		try
 		{
 			t = activeTransactions.get(comboBoxTransactions.getSelectedItem().toString());
 			int transactionDrawingPosition = 4;
 			for(TransactionItem ti : t.getItemsSold())
 			{				
 				JTextField quantity = new JTextField();
 				JTextField item = new JTextField();
 				JTextField price = new JTextField();
 				
 				quantity.setEditable(false);
 				item.setEditable(false);
 				price.setEditable(false);
 				
 				quantity.setText(ti.getQuantity() + "");
 				item.setText(ti.getItem().getItemCode() + ": " + ti.getItem().getItemName() + " at " + ti.getPrice() + " per " + ti.getItem().getUnitName());
 				price.setText(ti.getPrice() * ti.getQuantity() + "");
 				
 				panelTransactionDetails.add(quantity, "2, " + transactionDrawingPosition + ", fill, default");
 				panelTransactionDetails.add(item, "4, " + transactionDrawingPosition + ", fill, default");
 				panelTransactionDetails.add(price, "6, " + transactionDrawingPosition + ", fill, default");
 				
 				transactionDetails.add(quantity);
 				transactionDetails.add(item);
 				transactionDetails.add(price);
 				transactionDrawingPosition += 2;
 			}
 
 			paymentDetails.setText("The total amount due was " + (t.getRevenue() + t.getPointsUsed()) + ", and " + t.getPointsUsed() + " points were used, generating a total revenue of " + t.getRevenue() + ".");
 			panelTransactionDetails.add(paymentDetails, "4, " + transactionDrawingPosition + ", fill, default");
 
 			panelTransactionDetails.revalidate();
 		}
 		catch(NullPointerException npe)
 		{
 			panelTransactionDetails.revalidate();
 		}
 	}
 }
