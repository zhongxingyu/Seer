 package gui.forms.util;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import common.entity.dailyexpenses.DailyExpensesType;
 import common.entity.dailyexpenses.Expense;
 import common.entity.deposit.BankAccount;
 import common.entity.product.Product;
 import common.entity.salary.Fee;
 import common.manager.Manager;
 
 import util.JNumericField;
 import util.SBButton;
 import util.Tables;
 import util.Values;
 
 public class RowPanel extends JPanel {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 5134097254205041741L;
 	private int y;
 	private String command = "none";
 
 	private JComboBox productsCombo, feesCombo, expensesCombo;
 	private int ROW_WIDTH = 580, ROW_HEIGHT = 35, LABEL_HEIGHT = 25, LABEL_Y = 85, UPPER_Y = 55;
 	private JTextField productsComboField, expensesComboField, feesComboField;
 	private JNumericField quantitySack, quantityKG, amountOfExpense, feeAmount, onDisplayKG;
 
 	private ViewFormField kgpersack, priceSack, priceKG, productOnDisplay, onDisplaySack;
 	private JButton deleteRow, addRow;
 	private JPanel row;
 	private FormField bankAccount;
 	private SBButton sackConvert;
 
 	private RowJLabel date, amount;
 
 	private Object object;
 	private String[] stocks;
 	private int mode;
 	private String label = "";
 	private JPanel srcPanel = new JPanel();
 	private Product product;
 
 	public RowPanel(JPanel srcPanel, String label) {
 		this.srcPanel = srcPanel;
 		this.label = label;
 		init();
 	}
 
 	public RowPanel(Object object, JPanel srcPanel, String label) {
 		this.srcPanel = srcPanel;
 		this.object = object;
 		this.label = label;
 		init();
 	}
 
 	public RowPanel(int y, String command, int mode) {
 		this.y = y;
 		this.command = command;
 		this.mode = mode;
 		init();
 	}
 
 	public RowPanel(JPanel srcPanel, int mode) {
 		this.srcPanel = srcPanel;
 		this.mode = mode;
 		init();
 	}
 
 	private void init() {
 		// TODO Auto-generated method stub
 		if (label.equals(Tables.PRODUCTS))
 			ROW_HEIGHT = 30;
 
 		command = srcPanel.getComponentCount() + "";
 		y = srcPanel.getComponentCount() * ROW_HEIGHT;
 
 		setLayout(new BorderLayout());
 
 		row = new JPanel();
 		row.setLayout(null);
 		row.setBackground(Color.decode("#FFFFE6"));
 
		deleteRow = new SBButton("cancel.png", "cancel.png", "Remove", true);
 
 		try {
 			List<Product> products = new ArrayList<Product>();
 			try {
 				products = Manager.productManager.getProducts();
 			} catch (Exception e1) {
 				e1.printStackTrace();
 			}
 
 			productsCombo = new JComboBox(products.toArray());
 
 			feesCombo = new JComboBox();
 
 			List<Fee> fees = new ArrayList<Fee>();
 			try {
 				fees = Manager.salaryReleaseManager.getFees();
 				for (Fee f : fees) {
 					feesCombo.addItem(f.getName());
 				}
 			} catch (Exception e1) {
 				e1.printStackTrace();
 			}
 
 			expensesCombo = new JComboBox();
 
 			List<Expense> expenses = new ArrayList<Expense>();
 			try {
 				expenses = Manager.dailyExpenseManager.getExpenses();
 				for (Expense expense : expenses) {
 					expensesCombo.addItem(expense.getName());
 				}
 			} catch (Exception e1) {
 				e1.printStackTrace();
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		productsCombo.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
 		productsCombo.setEditable(true);
 		productsComboField = (JTextField) productsCombo.getEditor().getEditorComponent();
 		productsComboField.setText("");
 		productsComboField.addKeyListener(new ComboKeyHandler(productsCombo));
 
 		feesCombo.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
 		feesCombo.setEditable(true);
 		feesComboField = (JTextField) feesCombo.getEditor().getEditorComponent();
 		feesComboField.setText("");
 		feesComboField.addKeyListener(new ComboKeyHandler(feesCombo));
 		feesCombo.addItemListener(new ItemListener() {
 
 			@Override
 			public void itemStateChanged(ItemEvent arg0) {
 
 				String str = (String) feesCombo.getSelectedItem();
 
 				if (str != null && !str.equals("")) {
 					Fee fee = null;
 					try {
 						fee = Manager.salaryReleaseManager.searchFee(str);
 						if (fee != null) {
 							feeAmount.setText(String.format("%.2f", Manager.salaryReleaseManager.getMostRecentAmountForFee(fee.getId())));
 						} else {
 							feeAmount.setText("0");
 						}
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				} else {
 					feeAmount.setText("0");
 				}
 			}
 		});
 
 		expensesCombo.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
 		expensesCombo.setEditable(true);
 		expensesComboField = (JTextField) expensesCombo.getEditor().getEditorComponent();
 		expensesComboField.setText("");
 		expensesComboField.addKeyListener(new ComboKeyHandler(expensesCombo));
 		expensesCombo.addItemListener(new ItemListener() {
 
 			@Override
 			public void itemStateChanged(ItemEvent arg0) {
 
 				String str = (String) expensesCombo.getSelectedItem();
 
 				if (str != null && !str.equals("")) {
 					Expense expense = null;
 					try {
 						expense = Manager.dailyExpenseManager.searchExpense(str);
 						if (expense != null) {
 							amountOfExpense.setText(String.format("%.2f", Manager.dailyExpenseManager.getMostRecentAmountForExpense(expense.getId())));
 						} else {
 							amountOfExpense.setText("0");
 						}
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				} else {
 					amountOfExpense.setText("0");
 				}
 			}
 		});
 
 		quantitySack = new JNumericField(10, JNumericField.DECIMAL, true);
 		quantityKG = new JNumericField(10, JNumericField.DECIMAL, true);
 
 		kgpersack = new ViewFormField("");
 		priceKG = new ViewFormField("");
 		priceSack = new ViewFormField("");
 
 		feeAmount = new JNumericField(10, JNumericField.DECIMAL, true);
 		feeAmount.setText("0");
 
 		amountOfExpense = new JNumericField(10, JNumericField.DECIMAL, true);
 		amountOfExpense.setPrecision(2);
 		amountOfExpense.setText("0");
 
 		quantitySack.setPrecision(2);
 		quantityKG.setPrecision(2);
 
 		quantitySack.setText("0");
 		quantityKG.setText("0");
 
 		bankAccount = new FormField("Account Number", 100);
 		productsCombo.addItemListener(new ItemListener() {
 
 			@Override
 			public void itemStateChanged(ItemEvent arg0) {
 				if (productsCombo.getSelectedItem() != null) {
 					Product p = (Product) productsCombo.getSelectedItem();
 
 					kgpersack.setToolTip(kgpersack, p.getKilosPerSack() + "");
 					priceSack.setToolTip(priceSack, String.format("%.2f", p.getCurrentPricePerSack()) + "");
 					priceKG.setToolTip(priceKG, String.format("%.2f", p.getCurrentPricePerKilo()) + "");
 
 					quantitySack.setToolTipText(p.getQuantityDescription());
 					quantityKG.setToolTipText(p.getQuantityDescription());
 
 					if (Values.tableUtilPanel.getLabel().contains(Tables.SALES)) {
 						if (Values.salesForm.hasMultipleProduct())
 							Values.salesForm.setErrorText("No multiple product entry allowed ");
 					}
 
 					else if (Values.tableUtilPanel.getLabel().contains(Tables.ACCOUNT_RECEIVABLES)) {
 						if (Values.accountReceivablesForm.hasMultipleProduct())
 							Values.accountReceivablesForm.setErrorText("No multiple product entry allowed ");
 					}
 
 					else if (Values.tableUtilPanel.getLabel().contains(Tables.PULLOUTS)) {
 						if (Values.pulloutForm.hasMultipleProduct())
 							Values.pulloutForm.setErrorText("No multiple product entry allowed ");
 					}
 
 					else if (Values.tableUtilPanel.getLabel().contains(Tables.DELIVERIES)) {
 						if (Values.deliveryForm.hasMultipleProduct())
 							Values.deliveryForm.setErrorText("No multiple product entry allowed ");
 					}
 				}
 			}
 		});
 
 		if (mode == Values.EDIT)
 			ROW_WIDTH = 335;
 
 		if (label.equals(Tables.EXPENSES))
 			addExpensesRow();
 		else if (label.equals(Tables.SALARY))
 			addFeesRow();
 		else if (label.equals(Tables.BANK) || label.equals(Tables.BANK_EDIT))
 			addBankAccountRow();
 		else if (label.equals(Tables.PRODUCTS))
 			addOnDisplayRow();
 		else
 			addProductRow();
 
 		if (label.equals(Tables.PRODUCTS))
 			setBounds(-1, y, ROW_WIDTH, ROW_HEIGHT);
 		else
 			setBounds(0, y, ROW_WIDTH, ROW_HEIGHT);
 	}
 
 	public JComboBox getFeesCombo() {
 		return feesCombo;
 	}
 
 	public JComboBox getExpensesCombo() {
 		return expensesCombo;
 	}
 
 	private void addOnDisplayRow() {
 
 		 product = (Product) object;
 		
 		 sackConvert = new SBButton("convert.png", "convert.png", "Display one (1) sack");
 		 
 		 productOnDisplay = new ViewFormField(product.getName());
 		
 		 onDisplayKG = new JNumericField(10, JNumericField.DECIMAL, true);
 		 onDisplayKG.setPrecision(2);
 		 onDisplayKG.setText(product.getKilosOnDisplay()+""); 
 //				 == 0 ? "0" :
 //		 String.format("%.2f", product.getQuantityOnDisplayInKilo()));
 //		 onDisplayKG.setToolTipText(product.getQuantityDescription());
 		
 		 onDisplaySack = new ViewFormField(product.getSacks()+"");
 //		 onDisplaySack.setText(product.getQuantityOnDisplayInSack() == 0 ? "0" :
 //		 String.format("%.2f", product.getQuantityOnDisplayInSack()));
 //		 onDisplaySack.setToolTipText(product.getQuantityDescription());
 		 
 		 
 		 productOnDisplay.setBounds(0, 0, 197, ROW_HEIGHT);
 		 onDisplaySack.setBounds(209, 5, 60, 20);
 		 sackConvert.setBounds(280, 7, 16, 16);
 		 onDisplayKG.setBounds(301, 5, 60, 20); // +33//298
 //		 sackConvert.setBounds(360, 7, 16, 16);
 		
 		 row.add(productOnDisplay);
 		 row.add(onDisplaySack);
 		 row.add(sackConvert);
 		 row.add(onDisplayKG);
 		 
 		 if(product.getSacks() < 1d)
 			 sackConvert.setEnabled(false);
 		 
 		 sackConvert.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				// TODO Auto-generated method stub
 				
 				double val = Double.parseDouble(onDisplaySack.getText()) - 1;
 				onDisplaySack.setToolTip(onDisplaySack, val + "");
 				
 				double val2 = Double.parseDouble(onDisplayKG.getText()) + product.getKilosPerSack();
 				onDisplayKG.setText(val2+"");
 				
 				if(Double.parseDouble(onDisplaySack.getText()) == 0d)
 					 sackConvert.setEnabled(false);
 				
 			}
 		});
 		
 		 // row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,
 		 // Color.LIGHT_GRAY));
 		
 		if (product.getKilosOnDisplay() == 0d) {
 			row.setBackground(Color.decode("#E0EBEB"));
 		} else
 			row.setBackground(Color.decode("#BFFF80"));
 
 		add(row);
 	}
 
 	private void addFeesRow() {
 		deleteRow.setActionCommand(command);
 		deleteRow.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// TODO Auto-generated method stub
 				Values.salaryReleaseForm.removeRow(Integer.parseInt(e.getActionCommand()));
 			}
 		});
 
 		feesCombo.setBounds(11, 7, 148, 20);
 		feeAmount.setBounds(181, 7, 74, 20);
 		deleteRow.setBounds(284, 9, 16, 16); // +33
 
 		row.add(feesCombo);
 		row.add(feeAmount);
 		row.add(deleteRow);
 
 		add(row);
 	}
 
 	private void addBankAccountRow() {
 
 		if (label.equals(Tables.BANK_EDIT)) {
 
 			BankAccount bankAcct = (BankAccount) object;
 			bankAccount.setText(bankAcct.getAccountNo());
 			bankAccount.setEditable(false);
 		}
 
 		deleteRow.setActionCommand(command);
 		deleteRow.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// TODO Auto-generated method stub
 				if (label.equals(Tables.BANK))
 					Values.bankForm.removeRow(Integer.parseInt(e.getActionCommand()));
 				else
 					Values.editBankForm.removeRow(Integer.parseInt(e.getActionCommand()));
 			}
 		});
 
 		bankAccount.setBounds(21, 7, 160, 20);
 		deleteRow.setBounds(214, 9, 16, 16);
 
 		row.add(bankAccount);
 		// if (!label.equals(Tables.BANK_EDIT)) {
 		// row.add(deleteRow);
 		// }
 
 		add(row);
 	}
 
 	private void addExpensesRow() {
 		deleteRow.setActionCommand(command);
 		deleteRow.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// TODO Auto-generated method stub
 				Values.expensesForm.removeRow(Integer.parseInt(e.getActionCommand()));
 			}
 		});
 
 		amountOfExpense.setBounds(10, 7, 52, 20);
 		expensesCombo.setBounds(87, 7, 145, 20);
 		deleteRow.setBounds(257, 9, 16, 16);
 
 		row.add(amountOfExpense);
 		row.add(expensesCombo);
 		row.add(deleteRow);
 
 		add(row);
 	}
 
 	private void addProductRow() {
 		deleteRow.setActionCommand(command);
 		deleteRow.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// TODO Auto-generated method stub
 
 				switch (mode) {
 
 				case Values.ADD:
 					if (Values.tableUtilPanel.getLabel().contains(Tables.SALES))
 						Values.salesForm.removeRow(Integer.parseInt(e.getActionCommand()));
 
 					else if (Values.tableUtilPanel.getLabel().contains(Tables.ACCOUNT_RECEIVABLES))
 						Values.accountReceivablesForm.removeRow(Integer.parseInt(e.getActionCommand()));
 
 					else if (Values.tableUtilPanel.getLabel().contains(Tables.PULLOUTS))
 						Values.pulloutForm.removeRow(Integer.parseInt(e.getActionCommand()));
 
 					else if (Values.tableUtilPanel.getLabel().contains(Tables.DELIVERIES))
 						Values.deliveryForm.removeRow(Integer.parseInt(e.getActionCommand()));
 
 					break;
 
 				case Values.EDIT:
 					break;
 
 				default:
 					break;
 				}
 
 			}
 		});
 
 		quantitySack.setBounds(10, 7, 52, 20);
 		quantityKG.setBounds(87, 7, 52, 20);
 
 		// kgpersack.setBounds(167, 7, 57, 20);
 		//
 		// priceSack.setBounds(167, 7, 57, 20);
 		// priceKG.setBounds(249, 7, 57, 20);
 
 		kgpersack.setBounds(152, 7, 50, 20);
 
 		priceSack.setBounds(202, 7, 85, 20);
 		priceKG.setBounds(287, 7, 77, 20);
 
 		if (mode == Values.ADD)
 			productsCombo.setBounds(373, 7, 150, 20);
 		else
 			productsCombo.setBounds(87, 7, 235, 20);
 
 		deleteRow.setBounds(537, 9, 16, 16);
 
 		row.add(quantitySack);
 		row.add(quantityKG);
 		row.add(kgpersack);
 		row.add(priceSack);
 		row.add(priceKG);
 		row.add(productsCombo);
 
 		if (mode != Values.EDIT)
 			row.add(deleteRow);
 
 		productsCombo.setSelectedIndex(-1);
 
 		boolean found = false;
 		add(row);
 	}
 
 	public JPanel getRow() {
 		return row;
 	}
 
 	public JComboBox getProductCombo() {
 		return productsCombo;
 	}
 
 	public JButton getDeleteRow() {
 		return deleteRow;
 	}
 
 	public void setDeleteRow(JButton deleteRow) {
 		this.deleteRow = deleteRow;
 	}
 
 	public int getY() {
 		return y;
 	}
 
 	public void setY(int y) {
 		this.y = y;
 	}
 
 	public String getCommand() {
 		return command;
 	}
 
 	public void setCommand(String command) {
 		this.command = command;
 	}
 
 	public Product getSelectedProduct() throws ClassCastException {
 		return (Product) productsCombo.getSelectedItem();
 	}
 
 	public String getSelectedExpense() {
 		return expensesComboField.getText();
 	}
 
 	public String getSelectedFee() {
 		return feesComboField.getText();
 	}
 
 	public double getQuantityInSack() {
 
 		if (quantitySack.getText().equals(""))
 			return 0d;
 
 		return Double.parseDouble(quantitySack.getText());
 	}
 
 	public double getQuantityInKilo() {
 
 		if (quantityKG.getText().equals(""))
 			return 0d;
 
 		return Double.parseDouble(quantityKG.getText());
 	}
 
 	public double getExpenseAmount() {
 		return Double.parseDouble(amountOfExpense.getText());
 	}
 
 	public String getAccountNo() {
 		return bankAccount.getText();
 	}
 
 	public double getFeeAmout() {
 		return Double.parseDouble(feeAmount.getText());
 	}
 
 	public Product getOnDisplayProduct() {
 		return (Product) object;
 	}
 
 	public double getOnDisplayInSack() {
 		return Double.parseDouble(onDisplaySack.getText());
 	}
 
 	public double getOnDisplayInKilo() {
 		return Double.parseDouble(onDisplayKG.getText());
 	}
 
 	public FormField getBankAccount() {
 		return bankAccount;
 	}
 
 }
