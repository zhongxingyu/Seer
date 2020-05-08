 package gui.forms.edit;
 
 import gui.forms.util.DefaultEntryLabel;
 import gui.forms.util.EditFormField;
 import gui.forms.util.FormDropdown;
 import gui.forms.util.FormField;
 import gui.forms.util.FormLabel;
 import gui.forms.util.ISRowPanel;
 import gui.forms.util.SubTableHeaderLabel;
 import gui.popup.SuccessPopup;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import javax.swing.BorderFactory;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JComboBox;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.ScrollPaneConstants;
 
 import common.entity.accountreceivable.AccountReceivable;
 import common.entity.discountissue.DiscountIssue;
 import common.entity.inventorysheet.InventorySheet;
 import common.entity.profile.Person;
 import common.manager.Manager;
 
 import util.EditFormPanel;
 import util.ErrorLabel;
 import util.TableHeaderLabel;
 import util.Tables;
 import util.Values;
 import util.soy.SoyButton;
 
 public class EditCustomerForm extends EditFormPanel {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -1337878469442864579L;
 	private static final int ROW_HEIGHT = 35;
 	private ArrayList<EditFormField> fields = new ArrayList<EditFormField>();
 	private ArrayList<FormLabel> labels = new ArrayList<FormLabel>();
 
 	private SoyButton edit;
 	private FormDropdown acctType;
 	private JComboBox employeeCombo;
 	private JTextField employeesField;
 	private DefaultComboBoxModel model;
 
 	private DefaultEntryLabel ar_balance;
 	private TableHeaderLabel dateLabel, amountLabel;
 
 	private ArrayList<ISRowPanel> rows = new ArrayList<ISRowPanel>();
 	private JScrollPane ar_list_pane;
 	private JPanel ar_panel;
 	private SubTableHeaderLabel ar;
 
 	private JPanel panel;
 	private JScrollPane scrollPane;
 
 	private ErrorLabel error;
 	private String msg = "";
 
 	private final int num = Tables.customerFormLabel.length;
 
 	private Person customer;
 
 	public EditCustomerForm(Person customer) {
 		super("View / Edit Customer");
 		this.customer = customer;
 		addComponents();
 		fillEntries();
 		// Values.accountForm = this;
 	}
 
 	private void fillEntries() {
 		fields.get(0).setText(customer.getLastName());
 		fields.get(1).setText(customer.getFirstName());
 		fields.get(2).setText(customer.getMiddleName());
 		fields.get(3).setText(customer.getAddress());
 		fields.get(4).setText(customer.getContactNo());

 		ar_balance.setToolTip(String.format("%.2f", customer.getTotalAccountReceivables()));
 
 		int i = 0;
 		for (AccountReceivable ar : customer.getAccountReceivables()) {
			rows.add(new ISRowPanel(ar, ar_panel, Values.CUSTOMERS));
 			ar_panel.add(rows.get(i));
 			ar_panel.setPreferredSize(new Dimension(250, ar_panel.getComponentCount() * ROW_HEIGHT));
 			ar_panel.updateUI();
 			ar_panel.revalidate();
 			i++;
 		}
		
		alternateRows(rows);
 
 	}
 
 	private void addComponents() {
 
 		panel = new JPanel();
 		panel.setLayout(null);
 		panel.setOpaque(false);
 
 		scrollPane = new JScrollPane();
 
 		edit = new SoyButton("Edit");
 
 		ar = new SubTableHeaderLabel("Account Receivables", 2);
 
 		ar_balance = new DefaultEntryLabel("", true);
 
 		dateLabel = new TableHeaderLabel("Date");
 		amountLabel = new TableHeaderLabel("Amount");
 
 		ar_panel = new JPanel();
 		ar_panel.setLayout(null);
 		ar_panel.setOpaque(false);
 
 		ar_list_pane = new JScrollPane(ar_panel);
 
 		ar_list_pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		ar_list_pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 
 		ar_list_pane.setOpaque(false);
 		ar_list_pane.getViewport().setOpaque(false);
 
 		error = new ErrorLabel();
 
 		for (int i = 0, y = 0, x1 = 20; i < num; i++, y += 65) {// 290
 
 			if (i == num - 1) {
 				y = 0;
 				x1 = 265;
 
 				labels.add(new FormLabel("A_R Balance"));
 
 				labels.get(i).setBounds(x1, 15 + y, 200, 15);
 				ar_balance.setBounds(x1, 30 + y, 200, 25);
 
 				panel.add(labels.get(i));
 			}
 
 			else {
 				fields.add(new EditFormField(100));
 				labels.add(new FormLabel(Tables.customerFormLabel[i]));
 
 				fields.get(i).setBounds(x1, 30 + y, 200, 25);
 				labels.get(i).setBounds(x1, 15 + y, 200, 15);
 
 				panel.add(fields.get(i));
 				panel.add(labels.get(i));
 			}
 		}
 
 		ar.setBounds(265, 65, 250, 17);
 		dateLabel.setBounds(ar.getX() - 1, ar.getY() + ar.getHeight(), 150, 20);
 		amountLabel.setBounds(ar.getX() + dateLabel.getWidth() - 2, dateLabel.getY(), ar.getWidth() - dateLabel.getWidth() + 3, 20);
 		ar_list_pane.setBounds(dateLabel.getX() + 1, dateLabel.getY() + dateLabel.getHeight() - 1,
 				dateLabel.getWidth() + amountLabel.getWidth() + 14 /*
 																					 * SCROLLBAR
 																					 * WIDTH = 16
 																					 */, 210);
 
 		edit.setBounds(360, 355, 80, 30);
 
 		error.setBounds(510, 335, 200, 22);
 
 		edit.addMouseListener(new MouseAdapter() {
 			public void mouseClicked(MouseEvent e) {
 
 				if (isValidated()) {
 					customer.setLastName(fields.get(0).getText());
 					customer.setFirstName(fields.get(1).getText());
 					customer.setMiddleName(fields.get(2).getText());
 					customer.setAddress(fields.get(3).getText());
 					customer.setContactNo(fields.get(4).getText());
 
 					try {
 						Manager.employeePersonManager.updatePerson(customer);
 
 						Values.editPanel.startAnimation();
 						new SuccessPopup("Edit").setVisible(true);
 						Values.centerPanel.changeTable(Values.CUSTOMERS);
 
 					} catch (Exception e1) {
 						// TODO Auto-generated catch block
 						e1.printStackTrace();
 					}
 				} else
 					error.setToolTip(msg);
 			}
 		});
 
 		panel.add(ar_balance);
 
 		panel.add(ar);
 		panel.add(dateLabel);
 		panel.add(amountLabel);
 		panel.add(ar_list_pane);
 
 		add(edit);
 
 		scrollPane.setViewportView(panel);
 		scrollPane.setOpaque(false);
 		scrollPane.getViewport().setOpaque(false);
 		scrollPane.setBorder(BorderFactory.createEmptyBorder());
 
 		scrollPane.setBounds(130, 11, 550, 330);// w250,x272
 
 		add(error);
 		add(scrollPane);
 
 	}
 
 	private boolean isValidated() {
 
 		if (fields.get(0).getText().equals("")) {
 
 			msg = "Last Name is required";
 
 			return false;
 		}
 
 		if (fields.get(1).getText().equals("")) {
 
 			msg = "First Name is required";
 
 			return false;
 		}
 
 		return true;
 	}
 
 	private void alternateRows(ArrayList<ISRowPanel> rowPanel) {
 		for (int i = 0; i < rowPanel.size(); i++)
 			if (i % 2 == 0)
 				rowPanel.get(i).getRow().setBackground(Values.row1);
 			else
 				rowPanel.get(i).getRow().setBackground(Values.row2);
 	}
 }
