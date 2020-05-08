 package gui.forms.add;
 
 import gui.forms.util.DateTool;
 import gui.forms.util.FormDropdown;
 import gui.forms.util.ISRowPanel;
 import gui.forms.util.PDControlScrollPane;
 import gui.forms.util.SubTableHeaderLabel;
 import gui.forms.util.ViewportDragScrollListener;
 import gui.popup.UtilityPopup;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.MouseInfo;
 import java.awt.Point;
 import java.awt.PointerInfo;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JViewport;
 import javax.swing.ScrollPaneConstants;
 
 import util.DateFormatter;
 import util.MainFormLabel;
 import util.SimplePanel;
 import util.TableHeaderLabel;
 import util.Utility;
 import util.Values;
 import util.soy.SoyButton;
 
 import common.entity.accountreceivable.ARPayment;
 import common.entity.accountreceivable.AccountReceivable;
 import common.entity.cashadvance.CAPayment;
 import common.entity.cashadvance.CashAdvance;
 import common.entity.dailyexpenses.DailyExpenses;
 import common.entity.delivery.Delivery;
 import common.entity.deposit.Deposit;
 import common.entity.discountissue.DiscountIssue;
 import common.entity.inventorysheet.Breakdown;
 import common.entity.inventorysheet.BreakdownLine;
 import common.entity.inventorysheet.Denomination;
 import common.entity.inventorysheet.InventorySheet;
 import common.entity.inventorysheet.InventorySheetData;
 import common.entity.inventorysheet.InventorySheetDataDetail;
 import common.entity.inventorysheet.InventorySheetDetail;
 import common.entity.product.Product;
 import common.entity.pullout.PullOut;
 import common.entity.salary.SalaryRelease;
 import common.entity.sales.Sales;
 import common.manager.Manager;
 
 public class InventorySheetForm extends SimplePanel {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 540257659970499141L;
 	// 780x430
 	private int startX = 15, startY = 17, PANE_WIDTH = 760, PANE_HEIGHT = 390, TOTAL_LABEL_WIDTH = 100, TABLE_GAP = 50, TAB = 150, SECTION_GAP = 100,
 			LABEL_GAP = 30, TOTAL_FORMS_OVERALL = 12;
 	private int ROW_HEIGHT = 35;
 	/*
 	 * private int startX = 15, startY = 17, PANE_WIDTH = 750, PANE_HEIGHT = 380,
 	 * TOTAL_LABEL_WIDTH = 100, TABLE_GAP = 50, TAB = 150, SECTION_GAP = 120,
 	 * LABEL_GAP = 30, TOTAL_INVENTORY_LABEL = 16, TOTAL_FORMS_OVERALL = 12,
 	 * SCROLLBAR_WIDTH = 16;
 	 */
 	public static int SCROLLBAR_WIDTH = 16, PRODUCT_LABEL_WIDTH = 160, SACK_LABEL_WIDTH = 70, SALES_KG_WIDTH = 90, TOTAL_INVENTORY_LABEL = 16,
 			PRODUCT_ROW_WIDTH = 1360, DATE_LABEL_WIDTH = 150, ISSUED_BY_LABEL_WIDTH = 200, GROSS_LABEL_WIDTH = 100, TRANSACTIONS_ROW_WIDTH = 450;
 	private JPanel navigationPanel;
 
 	private ViewportDragScrollListener v1;
 	private JViewport view;
 
 	private List<Date> validDates = new ArrayList<Date>();
 
 	private ArrayList<JLabel> computationLabel = new ArrayList<JLabel>();
 	private ArrayList<ISRowPanel> cashBreakdown = new ArrayList<ISRowPanel>();
 	private ArrayList<ISRowPanel> productsInventory = new ArrayList<ISRowPanel>();
 	private ArrayList<ISRowPanel> productsInventory2 = new ArrayList<ISRowPanel>();
 	private ArrayList<ISRowPanel> salesInventory = new ArrayList<ISRowPanel>();
 	private ArrayList<ISRowPanel> deliveryInventory = new ArrayList<ISRowPanel>();
 	private ArrayList<ISRowPanel> arInventory = new ArrayList<ISRowPanel>();
 	private ArrayList<ISRowPanel> arPaymentInventory = new ArrayList<ISRowPanel>();
 	private ArrayList<ISRowPanel> caPaymentInventory = new ArrayList<ISRowPanel>();
 	private ArrayList<ISRowPanel> pullOutInventory = new ArrayList<ISRowPanel>();
 	private ArrayList<ISRowPanel> expensesInventory = new ArrayList<ISRowPanel>();
 	private ArrayList<ISRowPanel> caInventory = new ArrayList<ISRowPanel>();
 	private ArrayList<ISRowPanel> discountInventory = new ArrayList<ISRowPanel>();
 	private ArrayList<ISRowPanel> salaryInventory = new ArrayList<ISRowPanel>();
 	private ArrayList<ISRowPanel> depositInventory = new ArrayList<ISRowPanel>();
 
 	private ArrayList<JLabel> sectionLabel = new ArrayList<JLabel>();
 	private ArrayList<JLabel> totalInventoryLabel = new ArrayList<JLabel>();
 	private ArrayList<JLabel> formsOverall = new ArrayList<JLabel>();
 	private ArrayList<JLabel> summaryValues = new ArrayList<JLabel>();
 
 	private JPanel isPanel, productsPanel, productsPanel2, salesPanel, delPanel, arPanel, arPaymentPanel, caPaymentPanel, pulloutPanel, expensesPanel,
 			caPanel, discountPanel, ar2Panel, salaryPanel, depositPanel, cashBreakdownPanel;
 	private PDControlScrollPane productsPane, productsPane2, salesPane, delPane, arPane, arPaymentPane, caPaymentPane, pulloutPane, expensesPane,
 			caPane, discountPane, ar2Pane, salaryPane, depositPane;
 
 	private JScrollPane isPane;
 	private JLabel actualCashCount;
 
 	private MainFormLabel dateLabel;
 	private FormDropdown date;
 
 	private SoyButton save;
 
 	private JButton inputPCOH;
 
 	private TableHeaderLabel productLabel2, productLabel, sack1Label, kg1Label, sack2Label, kg2Label, sack3Label, kg3Label, sack4Label, kg4Label,
 			sack5Label, kg5Label, sack6Label, kg6Label, sack7Label, kg7Label, sack8Label, kg8Label, productTotalLabel, productTotalLabel2,
 			dateSaleslabel, cashierLabel, grossSalesLabel, dateDellabel, delReceivedByLabel, grossDelLabel, dateARlabel, arIssuedByLabel, grossARLabel,
 			dateARPaymentlabel, arPaymentIssuedByLabel, grossARPaymentLabel, dateCAPaymentlabel, caPaymentIssuedByLabel, grossCAPaymentLabel,
 			datePulloutlabel, pulloutIssuedByLabel, grossPulloutLabel, dateExpenseslabel, expensesIssuedByLabel, grossExpensesLabel, dateCAlabel,
 			CAIssuedByLabel, grossCALabel, dateDiscountlabel, discountAmountLabel, dateAR2label, ar2IssuedByLabel, grossAR2Label, dateSalarylabel,
 			salaryIssuedForLabel, salaryAmountLabel, dateDepositlabel, depositorLabel, depositAmountLabel;
 	private SubTableHeaderLabel begInvtyLabel, onDisplayLabel, delLabel, poLabel, endInvtyLabel, offtakeLabel, priceLabel, salesLabel, salesFormLabel,
 			overallSalesLabel, arFormLabel, overallARLabel, deliveryFormLabel, overallDelLabel, arPaymentFormLabel, overallARPaymentLabel,
 			caPaymentFormLabel, overallCAPaymentLabel, pullOutFormLabel, overallPulloutLabel, expensesFormLabel, overallExpensesLabel, caFormLabel,
 			overallCALabel, discountFormLabel, overallDiscountLabel, ar2FormLabel, overallAR2Label, salaryFormLabel, overallSalaryLabel,
 			depositFormLabel, overallDepositLabel, assetsLabel, liabilitiesLabel, pcohLabel, cohLabel, summary1Label, summary2Label, summary3Label,
 			accLabel;
 
 	private List<Product> products;
 	private List<Delivery> deliveries;
 	private List<PullOut> pullOuts;
 	private List<Sales> sales;
 	private List<AccountReceivable> accountReceivables;
 	private List<ARPayment> arPayments;
 	private List<CashAdvance> cashAdvances;
 	private List<CAPayment> caPayments;
 	private List<DailyExpenses> dailyExpenses;
 	private List<SalaryRelease> salaryReleases;
 	private List<DiscountIssue> discountIssues;
 	private List<Deposit> deposits;
 	// double previousAcoh =
 	// Manager.inventorySheetDataManager.getPreviousActualCashOnHand();
 
 	private InventorySheet inventorySheet;
 	private DefaultComboBoxModel model;
 
 	public InventorySheetForm() {
 		super("Add Inventory Sheet");
 		init();
 		addComponents();
 	}
 
 	private void init() {
 		inputPCOH = new JButton("+");
 		inputPCOH.setToolTipText("Input previous cash on hand");
 
 		navigationPanel = new JPanel();
 		navigationPanel.setLayout(null);
 		navigationPanel.setOpaque(false);
 
 		save = new SoyButton("Save");
 		save.setBounds(85, 45, 80, 30);
 
 		// navigationPanel.setBackground(Color.red);
 
 		isPanel = new JPanel();
 		isPanel.setLayout(null);
 		isPanel.setOpaque(false);
 
 		isPanel.setPreferredSize(new Dimension(PANE_WIDTH + 335, PANE_HEIGHT + 2385));// 2k
 
 		isPane = new JScrollPane(isPanel);
 		isPane.getVerticalScrollBar().setUnitIncrement(15);
 		// isPane.setWheelScrollingEnabled(false);
 		// isPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
 		// isPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 
 		v1 = new ViewportDragScrollListener(isPanel);
 
 		view = isPane.getViewport();
 		view.addMouseMotionListener(v1);
 		view.addMouseListener(v1);
 		view.addHierarchyListener(v1);
 
 		dateLabel = new MainFormLabel("Date:");
 
 		date = new FormDropdown();
 		// validDates.add(new Date());
 		// model = new DefaultComboBoxModel<Date>();
 		// date.setModel(model);
 		date.addItemListener(new ItemListener() {
 			@Override
 			public void itemStateChanged(ItemEvent arg0) {
 
 				try {
 					build();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 
 		productLabel = new TableHeaderLabel("Products");
 		productLabel2 = new TableHeaderLabel("Products");
 
 		begInvtyLabel = new SubTableHeaderLabel("BEG. INVTY");
 		sack1Label = new TableHeaderLabel("sack");
 		kg1Label = new TableHeaderLabel("kilo");
 
 		onDisplayLabel = new SubTableHeaderLabel("ON DISPLAY");
 		sack2Label = new TableHeaderLabel("sack");
 		kg2Label = new TableHeaderLabel("kilo");
 
 		delLabel = new SubTableHeaderLabel("DEL");
 		sack3Label = new TableHeaderLabel("sack");
 		kg3Label = new TableHeaderLabel("kilo");
 
 		poLabel = new SubTableHeaderLabel("P/O");
 		sack4Label = new TableHeaderLabel("sack");
 		kg4Label = new TableHeaderLabel("kilo");
 
 		endInvtyLabel = new SubTableHeaderLabel("E. INVTY");
 		sack5Label = new TableHeaderLabel("sack");
 		kg5Label = new TableHeaderLabel("kilo");
 
 		offtakeLabel = new SubTableHeaderLabel("OFFTAKE");
 		sack6Label = new TableHeaderLabel("sack");
 		kg6Label = new TableHeaderLabel("kilo");
 
 		priceLabel = new SubTableHeaderLabel("PRICE");
 		sack7Label = new TableHeaderLabel("sack");
 		kg7Label = new TableHeaderLabel("kilo");
 
 		salesLabel = new SubTableHeaderLabel("SALES AMOUNT");
 		sack8Label = new TableHeaderLabel("sack");
 		kg8Label = new TableHeaderLabel("kilo");
 
 		productsPanel = new JPanel();
 		productsPanel.setOpaque(false);
 		productsPanel.setLayout(null);
 		// productsPanel.setPreferredSize(new
 		// Dimension(productsPanel.getWidth(),
 		// PANE_HEIGHT + 500));
 
 		productsPane = new PDControlScrollPane();
 		productsPane.setViewportView(productsPanel);
 		productsPane.setOpaque(false);
 		productsPane.getViewport().setOpaque(false);
 
 		productsPane.getVerticalScrollBar().setUnitIncrement(15);
 
 		productsPanel2 = new JPanel();
 		productsPanel2.setOpaque(false);
 		productsPanel2.setLayout(null);
 
 		productsPane2 = new PDControlScrollPane();
 		productsPane2.setViewportView(productsPanel2);
 		productsPane2.setOpaque(false);
 		productsPane2.getViewport().setOpaque(false);
 
 		productsPane2.getVerticalScrollBar().setUnitIncrement(15);
 
 		/*
 		 * productsPane.getVerticalScrollBar().addAdjustmentListener(new
 		 * AdjustmentListener() {
 		 * 
 		 * @Override public void adjustmentValueChanged(AdjustmentEvent ae) { int
 		 * extent = productsPane.getVerticalScrollBar().getModel().getExtent();
 		 * int value = (productsPane.getVerticalScrollBar().getValue() + extent);
 		 * int max = productsPane.getVerticalScrollBar().getMaximum();
 		 * 
 		 * if (value == max || value == extent)
 		 * isPane.setWheelScrollingEnabled(true); else
 		 * isPane.setWheelScrollingEnabled(false);
 		 * 
 		 * } });
 		 */
 
 		// v2 = new ViewportDragScrollListener(isPanel);
 
 		productTotalLabel = new TableHeaderLabel("TOTAL");
 		productTotalLabel2 = new TableHeaderLabel("TOTAL");
 
 		salesFormLabel = new SubTableHeaderLabel("SALES", 2);
 		dateSaleslabel = new TableHeaderLabel("Date");
 		cashierLabel = new TableHeaderLabel("Cashier");
 		grossSalesLabel = new TableHeaderLabel("Gross");
 
 		salesPanel = new JPanel();
 		salesPanel.setOpaque(false);
 		salesPanel.setLayout(null);
 
 		salesPane = new PDControlScrollPane();
 		salesPane.setViewportView(salesPanel);
 
 		overallSalesLabel = new SubTableHeaderLabel("OVERALL");
 
 		deliveryFormLabel = new SubTableHeaderLabel("DELIVERIES", 2);
 		dateDellabel = new TableHeaderLabel("Date");
 		delReceivedByLabel = new TableHeaderLabel("Received by");
 		grossDelLabel = new TableHeaderLabel("Gross");
 		overallDelLabel = new SubTableHeaderLabel("OVERALL");
 
 		delPanel = new JPanel();
 		delPanel.setOpaque(false);
 		delPanel.setLayout(null);
 
 		delPane = new PDControlScrollPane();
 		delPane.setViewportView(delPanel);
 
 		arFormLabel = new SubTableHeaderLabel("ACCOUNT RECEIVABLES", 2);
 		dateARlabel = new TableHeaderLabel("Date");
 		arIssuedByLabel = new TableHeaderLabel("Customer");
 		grossARLabel = new TableHeaderLabel("Amount");
 		overallARLabel = new SubTableHeaderLabel("OVERALL");
 
 		arPanel = new JPanel();
 		arPanel.setOpaque(false);
 		arPanel.setLayout(null);
 
 		arPane = new PDControlScrollPane();
 		arPane.setViewportView(arPanel);
 
 		arPaymentFormLabel = new SubTableHeaderLabel("ACCOUNT RECEIVABLES PAYMENTS", 2);
 		dateARPaymentlabel = new TableHeaderLabel("Date");
 		arPaymentIssuedByLabel = new TableHeaderLabel("Issued by");
 		grossARPaymentLabel = new TableHeaderLabel("Amount");
 		overallARPaymentLabel = new SubTableHeaderLabel("OVERALL");
 
 		arPaymentPanel = new JPanel();
 		arPaymentPanel.setOpaque(false);
 		arPaymentPanel.setLayout(null);
 
 		arPaymentPane = new PDControlScrollPane();
 		arPaymentPane.setViewportView(arPaymentPanel);
 
 		caPaymentFormLabel = new SubTableHeaderLabel("CASH ADVANCE PAYMENTS", 2);
 		dateCAPaymentlabel = new TableHeaderLabel("Date");
 		caPaymentIssuedByLabel = new TableHeaderLabel("Issued by");
 		grossCAPaymentLabel = new TableHeaderLabel("Amount");
 		overallCAPaymentLabel = new SubTableHeaderLabel("OVERALL");
 
 		caPaymentPanel = new JPanel();
 		caPaymentPanel.setOpaque(false);
 		caPaymentPanel.setLayout(null);
 
 		caPaymentPane = new PDControlScrollPane();
 		caPaymentPane.setViewportView(caPaymentPanel);
 
 		pullOutFormLabel = new SubTableHeaderLabel("PRODUCT PULLOUTS", 2);
 		datePulloutlabel = new TableHeaderLabel("Date");
 		pulloutIssuedByLabel = new TableHeaderLabel("Issued by");
 		grossPulloutLabel = new TableHeaderLabel("Gross");
 		overallPulloutLabel = new SubTableHeaderLabel("OVERALL");
 
 		pulloutPanel = new JPanel();
 		pulloutPanel.setOpaque(false);
 		pulloutPanel.setLayout(null);
 
 		pulloutPane = new PDControlScrollPane();
 		pulloutPane.setViewportView(pulloutPanel);
 
 		expensesFormLabel = new SubTableHeaderLabel("EXPENSES", 2);
 		dateExpenseslabel = new TableHeaderLabel("Date");
 		expensesIssuedByLabel = new TableHeaderLabel("Type");
 		grossExpensesLabel = new TableHeaderLabel("Amount");
 		overallExpensesLabel = new SubTableHeaderLabel("OVERALL");
 
 		expensesPanel = new JPanel();
 		expensesPanel.setOpaque(false);
 		expensesPanel.setLayout(null);
 
 		expensesPane = new PDControlScrollPane();
 		expensesPane.setViewportView(expensesPanel);
 
 		caFormLabel = new SubTableHeaderLabel("CASH ADVANCES", 2);
 		dateCAlabel = new TableHeaderLabel("Date");
 		CAIssuedByLabel = new TableHeaderLabel("Employee");
 		grossCALabel = new TableHeaderLabel("Amount");
 		overallCALabel = new SubTableHeaderLabel("OVERALL");
 
 		caPanel = new JPanel();
 		caPanel.setOpaque(false);
 		caPanel.setLayout(null);
 
 		caPane = new PDControlScrollPane();
 		caPane.setViewportView(caPanel);
 
 		discountFormLabel = new SubTableHeaderLabel("DISCOUNTS", 2);
 		dateDiscountlabel = new TableHeaderLabel("Date");
 		discountAmountLabel = new TableHeaderLabel("Amount");
 		overallDiscountLabel = new SubTableHeaderLabel("OVERALL");
 
 		discountPanel = new JPanel();
 		discountPanel.setOpaque(false);
 		discountPanel.setLayout(null);
 
 		discountPane = new PDControlScrollPane();
 		discountPane.setViewportView(discountPanel);
 
 		ar2FormLabel = new SubTableHeaderLabel("ACCOUNT RECEIVABLES", 2);
 		dateAR2label = new TableHeaderLabel("Date");
 		ar2IssuedByLabel = new TableHeaderLabel("Customer");
 		grossAR2Label = new TableHeaderLabel("Amount");
 		overallAR2Label = new SubTableHeaderLabel("OVERALL");
 
 		ar2Panel = new JPanel();
 		ar2Panel.setOpaque(false);
 		ar2Panel.setLayout(null);
 
 		ar2Pane = new PDControlScrollPane();
 		ar2Pane.setViewportView(ar2Panel);
 
 		salaryFormLabel = new SubTableHeaderLabel("SALARY RELEASE", 2);
 		dateSalarylabel = new TableHeaderLabel("Date");
 		salaryIssuedForLabel = new TableHeaderLabel("Employee");
 		salaryAmountLabel = new TableHeaderLabel("Net Amount");
 		overallSalaryLabel = new SubTableHeaderLabel("OVERALL");
 
 		salaryPanel = new JPanel();
 		salaryPanel.setOpaque(false);
 		salaryPanel.setLayout(null);
 
 		salaryPane = new PDControlScrollPane();
 		salaryPane.setViewportView(salaryPanel);
 
 		depositFormLabel = new SubTableHeaderLabel("DEPOSITS", 2);
 		dateDepositlabel = new TableHeaderLabel("Date");
 		depositorLabel = new TableHeaderLabel("Bank");
 		depositAmountLabel = new TableHeaderLabel("Amount");
 		overallDepositLabel = new SubTableHeaderLabel("OVERALL");
 
 		depositPanel = new JPanel();
 		depositPanel.setOpaque(false);
 		depositPanel.setLayout(null);
 
 		depositPane = new PDControlScrollPane();
 		depositPane.setViewportView(depositPanel);
 
 		pcohLabel = new SubTableHeaderLabel("PREVIOUS COH", Color.CYAN.darker());
 		assetsLabel = new SubTableHeaderLabel("ASSETS", Color.GREEN.darker());
 		liabilitiesLabel = new SubTableHeaderLabel("LIABILITIES", Color.red.brighter());
 		cohLabel = new SubTableHeaderLabel("CASH ON HAND");
 
 		cashBreakdownPanel = new JPanel();
 		cashBreakdownPanel.setLayout(null);
 		cashBreakdownPanel.setOpaque(false);
 
 		// for (int i = 0; i < 4; i++) {
 		// computationLabel.add(new JLabel());
 		// computationLabel.get(i).setBorder(BorderFactory.createMatteBorder(1, 0,
 		// 1, 1, Color.black));
 		// computationLabel.get(i).setHorizontalAlignment(JLabel.CENTER);
 		// // computationLabel.get(i).setText("P 2470.65");
 		//
 		// isPanel.add(computationLabel.get(i));
 		// }
 
 		for (int i = 0; i < 6; i++) {
 			sectionLabel.add(new JLabel());
 			sectionLabel.get(i).setBorder(BorderFactory.createMatteBorder(0, 10, 0, 0, Color.decode("#006400")));
 			sectionLabel.get(i).setHorizontalAlignment(JLabel.CENTER);
 			sectionLabel.get(i).setOpaque(true);
 			sectionLabel.get(i).setBackground(Color.decode("#DAA520"));
 			sectionLabel.get(i).setForeground(Color.white);// Color.decode("#000080"));
 			sectionLabel.get(i).setFont(new Font("Harabara", Font.BOLD, 16));
 			// sectionLabel.get(i).setText("P 2470.65");
 
 			isPanel.add(sectionLabel.get(i));
 		}
 
 		for (int i = 0; i < 4; i++) {
 			computationLabel.add(new JLabel());
 			computationLabel.get(i).setBorder(BorderFactory.createMatteBorder(1, 0, 1, 1, Color.black));
 			computationLabel.get(i).setHorizontalAlignment(JLabel.CENTER);
 			computationLabel.get(i).setOpaque(true);
 			computationLabel.get(i).setBackground(Color.decode("#FFFFE6"));
 			// computationLabel.get(i).setText("P 2470.65");
 
 			isPanel.add(computationLabel.get(i));
 		}
 
 		summary1Label = new SubTableHeaderLabel("CASH ON HAND", Color.decode("#A233A2"));
 		summary2Label = new SubTableHeaderLabel("ACTUAL CASH COUNT", Color.decode("#AE4DAE"));
 		summary3Label = new SubTableHeaderLabel("", Color.decode("#B966B9"));
 
 		for (int i = 0; i < TOTAL_INVENTORY_LABEL; i++) {
 			totalInventoryLabel.add(new JLabel());
 			totalInventoryLabel.get(i).setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.black));
 			totalInventoryLabel.get(i).setHorizontalAlignment(JLabel.CENTER);
 			totalInventoryLabel.get(i).setOpaque(true);
 			totalInventoryLabel.get(i).setBackground(Color.decode("#FFFFE6"));
 			// sectionLabel.get(i).setText("P 2470.65");
 
 			isPanel.add(totalInventoryLabel.get(i));
 		}
 
 		for (int i = 0; i < TOTAL_FORMS_OVERALL; i++) {
 			formsOverall.add(new JLabel());
 			formsOverall.get(i).setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.black));
 			formsOverall.get(i).setHorizontalAlignment(JLabel.CENTER);
 			formsOverall.get(i).setOpaque(true);
 			formsOverall.get(i).setBackground(Color.decode("#FFFFE6"));
 			// sectionLabel.get(i).setText("P 2470.65");
 
 			isPanel.add(formsOverall.get(i));
 		}
 
 		for (int i = 0; i < 3; i++) {
 			summaryValues.add(new JLabel());
 			summaryValues.get(i).setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, Color.black));
 			summaryValues.get(i).setHorizontalAlignment(JLabel.CENTER);
 			summaryValues.get(i).setOpaque(true);
 			summaryValues.get(i).setBackground(Color.decode("#FFFFE6"));
 			// sectionLabel.get(i).setText("P 2470.65");
 
 			isPanel.add(summaryValues.get(i));
 		}
 
 		actualCashCount = new JLabel();
 		actualCashCount.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 1, Color.black));
 		actualCashCount.setHorizontalAlignment(JLabel.CENTER);
 		actualCashCount.setOpaque(true);
 		actualCashCount.setBackground(Color.decode("#FFFFE6"));
 
 		accLabel = new SubTableHeaderLabel("ACTUAL CASH COUNT");
 	}
 
 	private void addComponents() {
 
 		dateLabel.setBounds(startX, startY, 40, 20);
 		date.setBounds(startX + dateLabel.getWidth(), startY, 180, 20);
 
 		sectionLabel.get(0).setText("INVENTORY");
 		sectionLabel.get(0).setBounds(startX, dateLabel.getY() + dateLabel.getHeight() + 30, 150, 20);
 
 		productLabel.setBounds(startX, sectionLabel.get(0).getY() + sectionLabel.get(0).getHeight() + LABEL_GAP, PRODUCT_LABEL_WIDTH, 30);
 
 		begInvtyLabel.setBounds(startX + productLabel.getWidth(), productLabel.getY(), 140, 10);
 		sack1Label.setBounds(startX + productLabel.getWidth() - 1, productLabel.getY() + begInvtyLabel.getHeight(), begInvtyLabel.getWidth() / 2, 20);
 		kg1Label.setBounds(startX + productLabel.getWidth() + sack1Label.getWidth() - 2, productLabel.getY() + begInvtyLabel.getHeight(),
 				begInvtyLabel.getWidth() / 2 + 3, 20);
 
 		onDisplayLabel.setBounds(startX + productLabel.getWidth() + begInvtyLabel.getWidth(), productLabel.getY(), 140, 10);
 		sack2Label.setBounds(startX + productLabel.getWidth() + begInvtyLabel.getWidth(), productLabel.getY() + onDisplayLabel.getHeight(),
 				onDisplayLabel.getWidth() / 2, 20);
 		kg2Label.setBounds(startX + productLabel.getWidth() + begInvtyLabel.getWidth() + sack2Label.getWidth() - 1, productLabel.getY()
 				+ onDisplayLabel.getHeight(), onDisplayLabel.getWidth() / 2 + 2, 20);
 
 		delLabel.setBounds(startX + productLabel.getWidth() + begInvtyLabel.getWidth() + onDisplayLabel.getWidth(), productLabel.getY(), 140, 10);
 		sack3Label
 				.setBounds(onDisplayLabel.getX() + onDisplayLabel.getWidth(), productLabel.getY() + delLabel.getHeight(), delLabel.getWidth() / 2, 20);
 		kg3Label.setBounds(onDisplayLabel.getX() + onDisplayLabel.getWidth() + sack3Label.getWidth() - 1, productLabel.getY() + delLabel.getHeight(),
 				delLabel.getWidth() / 2 + 2, 20);
 
 		poLabel.setBounds(delLabel.getX() + delLabel.getWidth(), productLabel.getY(), 140, 10);
 		sack4Label.setBounds(delLabel.getX() + delLabel.getWidth(), productLabel.getY() + delLabel.getHeight(), delLabel.getWidth() / 2, 20);
 		kg4Label.setBounds(delLabel.getX() + delLabel.getWidth() + sack3Label.getWidth() - 1, productLabel.getY() + delLabel.getHeight(),
 				delLabel.getWidth() / 2 + 2, 20);
 
 		productsPane.setBounds(startX + 1, productLabel.getHeight() + productLabel.getY() - 1,
 				poLabel.getX() + poLabel.getWidth() - productLabel.getX() + SCROLLBAR_WIDTH - 1, 350);
 
 		productTotalLabel.setBounds(startX, productsPane.getY() + productsPane.getHeight(), productLabel.getWidth(), 20);
 
 		productLabel2.setBounds(startX, productTotalLabel.getY() + productTotalLabel.getHeight() + TABLE_GAP, PRODUCT_LABEL_WIDTH, 30);
 
 		endInvtyLabel.setBounds(startX + productLabel2.getWidth(), productLabel2.getY(), 140, 10);
 		sack5Label.setBounds(productLabel2.getX() + productLabel2.getWidth() - 1, productLabel2.getY() + endInvtyLabel.getHeight(),
 				endInvtyLabel.getWidth() / 2, 20);
 		kg5Label.setBounds(productLabel2.getX() + productLabel2.getWidth() + sack5Label.getWidth() - 2,
 				productLabel2.getY() + endInvtyLabel.getHeight(), endInvtyLabel.getWidth() / 2 + 3, 20);
 
 		offtakeLabel.setBounds(endInvtyLabel.getX() + endInvtyLabel.getWidth(), productLabel2.getY(), 140, 10);
 		sack6Label.setBounds(endInvtyLabel.getX() + endInvtyLabel.getWidth(), productLabel2.getY() + endInvtyLabel.getHeight(),
 				endInvtyLabel.getWidth() / 2, 20);
 		kg6Label.setBounds(endInvtyLabel.getX() + endInvtyLabel.getWidth() + sack6Label.getWidth() - 1,
 				productLabel2.getY() + endInvtyLabel.getHeight(), endInvtyLabel.getWidth() / 2 + 2, 20);
 
 		priceLabel.setBounds(offtakeLabel.getX() + offtakeLabel.getWidth(), productLabel2.getY(), 180, 10);
 		sack7Label.setBounds(offtakeLabel.getX() + offtakeLabel.getWidth(), productLabel2.getY() + offtakeLabel.getHeight(), priceLabel.getWidth() / 2,
 				20);
 		kg7Label.setBounds(offtakeLabel.getX() + offtakeLabel.getWidth() + sack7Label.getWidth() - 1, productLabel2.getY() + offtakeLabel.getHeight(),
 				priceLabel.getWidth() / 2 + 2, 20);
 
 		salesLabel.setBounds(priceLabel.getX() + priceLabel.getWidth(), productLabel2.getY(), 180, 10);
 		sack8Label.setBounds(priceLabel.getX() + priceLabel.getWidth(), productLabel2.getY() + priceLabel.getHeight(), salesLabel.getWidth() / 2, 20);
 		kg8Label.setBounds(priceLabel.getX() + priceLabel.getWidth() + sack8Label.getWidth() - 1, productLabel2.getY() + priceLabel.getHeight(),
 				salesLabel.getWidth() / 2 + 2, 20);
 
 		productsPane2.setBounds(startX + 1, productLabel2.getHeight() + productLabel2.getY() - 1, salesLabel.getX() + salesLabel.getWidth()
 				- productLabel2.getX() + SCROLLBAR_WIDTH - 1, 350);
 
 		productTotalLabel2.setBounds(startX, productsPane2.getY() + productsPane2.getHeight(), productLabel2.getWidth(), 20);
 
 		// fillTables();
 		for (int i = 0, x = 0; i < TOTAL_INVENTORY_LABEL - 8; i++, x += sack1Label.getWidth())
 			totalInventoryLabel.get(i).setBounds(productTotalLabel.getX() + productTotalLabel.getWidth() + x - 1, productTotalLabel.getY(),
 					sack1Label.getWidth(), 20);
 
 		for (int i = TOTAL_INVENTORY_LABEL - 8, x = 0; i < TOTAL_INVENTORY_LABEL - 4; i++, x += sack1Label.getWidth())
 			totalInventoryLabel.get(i).setBounds(productTotalLabel2.getX() + productTotalLabel2.getWidth() + x - 1, productTotalLabel2.getY(),
 					sack1Label.getWidth(), 20);
 
 		for (int i = TOTAL_INVENTORY_LABEL - 4, x = 0; i < TOTAL_INVENTORY_LABEL; i++, x += kg7Label.getWidth())
 			totalInventoryLabel.get(i).setBounds(kg6Label.getX() + kg6Label.getWidth() + x - 8, productTotalLabel2.getY(), kg7Label.getWidth(), 20);
 
 		// ====================================================== END OF PRODUCT
 		// INVENTORY TABLE ==============================================
 
 		sectionLabel.get(1).setText("TRANSACTIONS");
 		sectionLabel.get(1).setBounds(startX, productsPane2.getY() + productsPane2.getHeight() + SECTION_GAP, 150, 20);
 
 		salesFormLabel.setBounds(startX, sectionLabel.get(1).getY() + sectionLabel.get(1).getHeight() + LABEL_GAP, 450, 18);
 		dateSaleslabel.setBounds(startX - 1, salesFormLabel.getY() + salesFormLabel.getHeight(), 150, 20);
 		cashierLabel.setBounds(startX + dateSaleslabel.getWidth() - 2, salesFormLabel.getY() + salesFormLabel.getHeight(), salesFormLabel.getWidth()
 				- dateSaleslabel.getWidth() + 2 - TOTAL_LABEL_WIDTH, 20);
 		grossSalesLabel.setBounds(cashierLabel.getX() + cashierLabel.getWidth() - 1, salesFormLabel.getY() + salesFormLabel.getHeight(),
 				salesFormLabel.getWidth() - (dateSaleslabel.getWidth() + cashierLabel.getWidth()) + 4, 20);
 		salesPane.setBounds(startX, dateSaleslabel.getY() + dateSaleslabel.getHeight() - 1, salesFormLabel.getWidth() + SCROLLBAR_WIDTH, 105);
 		overallSalesLabel.setBounds(startX, salesPane.getY() + salesPane.getHeight(), 200, 15);
 		formsOverall.get(0).setText("P 15890.50");
 		formsOverall.get(0).setBounds(overallSalesLabel.getX() + overallSalesLabel.getWidth(), overallSalesLabel.getY(),
 				salesPane.getWidth() - overallSalesLabel.getWidth(), 15);
 
 		deliveryFormLabel.setBounds(startX, overallSalesLabel.getY() + overallSalesLabel.getHeight() + TABLE_GAP, 450, 18);
 		dateDellabel.setBounds(deliveryFormLabel.getX() - 1, deliveryFormLabel.getY() + deliveryFormLabel.getHeight(), 150, 20);
 		delReceivedByLabel.setBounds(deliveryFormLabel.getX() + dateDellabel.getWidth() - 2, deliveryFormLabel.getY() + deliveryFormLabel.getHeight(),
 				deliveryFormLabel.getWidth() - dateDellabel.getWidth() + 2 - TOTAL_LABEL_WIDTH, 20);
 		grossDelLabel.setBounds(delReceivedByLabel.getX() + delReceivedByLabel.getWidth() - 1,
 				deliveryFormLabel.getY() + deliveryFormLabel.getHeight(),
 				deliveryFormLabel.getWidth() - (dateDellabel.getWidth() + delReceivedByLabel.getWidth()) + 4, 20);
 		delPane.setBounds(startX, dateDellabel.getY() + dateDellabel.getHeight() - 1, salesFormLabel.getWidth() + SCROLLBAR_WIDTH, 105);
 		overallDelLabel.setBounds(startX, delPane.getY() + delPane.getHeight(), 200, 15);
 		formsOverall.get(1).setBounds(overallDelLabel.getX() + overallDelLabel.getWidth(), overallDelLabel.getY(),
 				delPane.getWidth() - overallDelLabel.getWidth(), 15);
 
 		expensesFormLabel.setBounds(startX, overallDelLabel.getY() + overallDelLabel.getHeight() + TABLE_GAP, 450, 18);
 		dateExpenseslabel.setBounds(expensesFormLabel.getX() - 1, expensesFormLabel.getY() + expensesFormLabel.getHeight(), 150, 20);
 		expensesIssuedByLabel.setBounds(expensesFormLabel.getX() + dateExpenseslabel.getWidth() - 2,
 				expensesFormLabel.getY() + expensesFormLabel.getHeight(), expensesFormLabel.getWidth() - dateExpenseslabel.getWidth() + 2
 						- TOTAL_LABEL_WIDTH, 20);
 		grossExpensesLabel.setBounds(expensesIssuedByLabel.getX() + expensesIssuedByLabel.getWidth() - 1,
 				expensesFormLabel.getY() + expensesFormLabel.getHeight(), expensesFormLabel.getWidth()
 						- (dateExpenseslabel.getWidth() + expensesIssuedByLabel.getWidth()) + 4, 20);
 		expensesPane.setBounds(expensesFormLabel.getX(), dateExpenseslabel.getY() + dateExpenseslabel.getHeight() - 1, expensesFormLabel.getWidth()
 				+ SCROLLBAR_WIDTH, 105);
 		overallExpensesLabel.setBounds(expensesFormLabel.getX(), expensesPane.getY() + expensesPane.getHeight(), 200, 15);
 		formsOverall.get(6).setBounds(overallExpensesLabel.getX() + overallExpensesLabel.getWidth(), overallExpensesLabel.getY(),
 				expensesPane.getWidth() - overallExpensesLabel.getWidth(), 15);
 
 		pullOutFormLabel.setBounds(startX, overallExpensesLabel.getY() + overallExpensesLabel.getHeight() + TABLE_GAP, 450, 18);
 		datePulloutlabel.setBounds(pullOutFormLabel.getX() - 1, pullOutFormLabel.getY() + pullOutFormLabel.getHeight(), 150, 20);
 		pulloutIssuedByLabel.setBounds(pullOutFormLabel.getX() + datePulloutlabel.getWidth() - 2,
 				pullOutFormLabel.getY() + pullOutFormLabel.getHeight(),
 				pullOutFormLabel.getWidth() - datePulloutlabel.getWidth() + 2 - TOTAL_LABEL_WIDTH, 20);
 		grossPulloutLabel.setBounds(pulloutIssuedByLabel.getX() + pulloutIssuedByLabel.getWidth() - 1,
 				pullOutFormLabel.getY() + pullOutFormLabel.getHeight(),
 				pullOutFormLabel.getWidth() - (datePulloutlabel.getWidth() + pulloutIssuedByLabel.getWidth()) + 4, 20);
 		pulloutPane.setBounds(pullOutFormLabel.getX(), datePulloutlabel.getY() + datePulloutlabel.getHeight() - 1, pullOutFormLabel.getWidth()
 				+ SCROLLBAR_WIDTH, 105);
 		overallPulloutLabel.setBounds(pullOutFormLabel.getX(), pulloutPane.getY() + pulloutPane.getHeight(), 200, 15);
 		formsOverall.get(5).setBounds(overallPulloutLabel.getX() + overallPulloutLabel.getWidth(), overallPulloutLabel.getY(),
 				pulloutPane.getWidth() - overallPulloutLabel.getWidth(), 15);
 
 		arFormLabel.setBounds(startX, overallPulloutLabel.getY() + overallPulloutLabel.getHeight() + TABLE_GAP, 450, 18);
 		dateARlabel.setBounds(arFormLabel.getX() - 1, arFormLabel.getY() + arFormLabel.getHeight(), 150, 20);
 		arIssuedByLabel.setBounds(arFormLabel.getX() + dateARlabel.getWidth() - 2, arFormLabel.getY() + arFormLabel.getHeight(), arFormLabel.getWidth()
 				- dateARlabel.getWidth() + 2 - TOTAL_LABEL_WIDTH, 20);
 		grossARLabel.setBounds(arIssuedByLabel.getX() + arIssuedByLabel.getWidth() - 1, arFormLabel.getY() + arFormLabel.getHeight(),
 				arFormLabel.getWidth() - (dateARlabel.getWidth() + arIssuedByLabel.getWidth()) + 4, 20);
 		arPane.setBounds(arFormLabel.getX(), dateARlabel.getY() + dateARlabel.getHeight() - 1, arFormLabel.getWidth() + SCROLLBAR_WIDTH, 105);
 		overallARLabel.setBounds(arFormLabel.getX(), arPane.getY() + arPane.getHeight(), 200, 15);
 		formsOverall.get(2).setBounds(overallARLabel.getX() + overallARLabel.getWidth(), overallARLabel.getY(),
 				arPane.getWidth() - overallARLabel.getWidth(), 15);
 
 		arPaymentFormLabel.setBounds(startX, overallARLabel.getY() + overallARLabel.getHeight() + TABLE_GAP, 450, 18);
 		dateARPaymentlabel.setBounds(arPaymentFormLabel.getX() - 1, arPaymentFormLabel.getY() + arFormLabel.getHeight(), 150, 20);
 		arPaymentIssuedByLabel.setBounds(arPaymentFormLabel.getX() + dateARPaymentlabel.getWidth() - 2,
 				arPaymentFormLabel.getY() + arPaymentFormLabel.getHeight(), arPaymentFormLabel.getWidth() - dateARPaymentlabel.getWidth() + 2
 						- TOTAL_LABEL_WIDTH, 20);
 		grossARPaymentLabel.setBounds(arPaymentIssuedByLabel.getX() + arPaymentIssuedByLabel.getWidth() - 1, arPaymentFormLabel.getY()
 				+ arPaymentFormLabel.getHeight(),
 				arPaymentFormLabel.getWidth() - (dateARPaymentlabel.getWidth() + arPaymentIssuedByLabel.getWidth()) + 4, 20);
 		arPaymentPane.setBounds(startX, dateARPaymentlabel.getY() + dateARPaymentlabel.getHeight() - 1,
 				arPaymentFormLabel.getWidth() + SCROLLBAR_WIDTH, 105);
 		overallARPaymentLabel.setBounds(startX, arPaymentPane.getY() + arPaymentPane.getHeight(), 200, 15);
 		formsOverall.get(3).setBounds(overallARPaymentLabel.getX() + overallARPaymentLabel.getWidth(), overallARPaymentLabel.getY(),
 				arPaymentPane.getWidth() - overallARPaymentLabel.getWidth(), 15);
 
 		// ======================================END OF FIRST COLUMN TRANSACTION
 		// TABLES==========================================================
 
 		caFormLabel.setBounds(salesFormLabel.getWidth() + TAB, sectionLabel.get(1).getY() + sectionLabel.get(1).getHeight() + LABEL_GAP, 450, 18);
 		dateCAlabel.setBounds(caFormLabel.getX() - 1, caFormLabel.getY() + caFormLabel.getHeight(), 150, 20);
 		CAIssuedByLabel.setBounds(caFormLabel.getX() + dateCAlabel.getWidth() - 2, caFormLabel.getY() + caFormLabel.getHeight(), caFormLabel.getWidth()
 				- dateCAlabel.getWidth() + 2 - TOTAL_LABEL_WIDTH, 20);
 		grossCALabel.setBounds(CAIssuedByLabel.getX() + CAIssuedByLabel.getWidth() - 1, caFormLabel.getY() + caFormLabel.getHeight(),
 				caFormLabel.getWidth() - (dateCAlabel.getWidth() + CAIssuedByLabel.getWidth()) + 4, 20);
 		caPane.setBounds(caFormLabel.getX(), dateCAlabel.getY() + dateCAlabel.getHeight() - 1, caFormLabel.getWidth() + SCROLLBAR_WIDTH, 105);
 		overallCALabel.setBounds(caFormLabel.getX(), caPane.getY() + caPane.getHeight(), 200, 15);
 		formsOverall.get(7).setBounds(overallCALabel.getX() + overallCALabel.getWidth(), overallCALabel.getY(),
 				caPane.getWidth() - overallCALabel.getWidth(), 15);
 
 		caPaymentFormLabel.setBounds(caFormLabel.getX(), overallCALabel.getY() + overallCALabel.getHeight() + TABLE_GAP, 450, 18);
 		dateCAPaymentlabel.setBounds(caPaymentFormLabel.getX() - 1, caPaymentFormLabel.getY() + arFormLabel.getHeight(), 150, 20);
 		caPaymentIssuedByLabel.setBounds(caPaymentFormLabel.getX() + dateCAPaymentlabel.getWidth() - 2,
 				caPaymentFormLabel.getY() + caPaymentFormLabel.getHeight(), caPaymentFormLabel.getWidth() - dateCAPaymentlabel.getWidth() + 2
 						- TOTAL_LABEL_WIDTH, 20);
 		grossCAPaymentLabel.setBounds(caPaymentIssuedByLabel.getX() + caPaymentIssuedByLabel.getWidth() - 1, caPaymentFormLabel.getY()
 				+ arPaymentFormLabel.getHeight(),
 				caPaymentFormLabel.getWidth() - (dateCAPaymentlabel.getWidth() + caPaymentIssuedByLabel.getWidth()) + 4, 20);
 		caPaymentPane.setBounds(caPaymentFormLabel.getX(), dateCAPaymentlabel.getY() + dateCAPaymentlabel.getHeight() - 1,
 				caPaymentFormLabel.getWidth() + SCROLLBAR_WIDTH, 105);
 		overallCAPaymentLabel.setBounds(caPaymentFormLabel.getX(), caPaymentPane.getY() + caPaymentPane.getHeight(), 200, 15);
 		formsOverall.get(4).setBounds(overallCAPaymentLabel.getX() + overallCAPaymentLabel.getWidth(), overallCAPaymentLabel.getY(),
 				caPaymentPane.getWidth() - overallCAPaymentLabel.getWidth(), 15);
 
 		salaryFormLabel.setBounds(caFormLabel.getX(), overallCAPaymentLabel.getY() + overallCAPaymentLabel.getHeight() + TABLE_GAP, 450, 18);
 		dateSalarylabel.setBounds(salaryFormLabel.getX() - 1, salaryFormLabel.getY() + salaryFormLabel.getHeight(), 150, 20);
 		salaryIssuedForLabel.setBounds(salaryFormLabel.getX() + datePulloutlabel.getWidth() - 2, salaryFormLabel.getY() + salaryFormLabel.getHeight(),
 				salaryFormLabel.getWidth() - dateSalarylabel.getWidth() + 2 - TOTAL_LABEL_WIDTH, 20);
 		salaryAmountLabel.setBounds(salaryIssuedForLabel.getX() + salaryIssuedForLabel.getWidth() - 1,
 				salaryFormLabel.getY() + salaryFormLabel.getHeight(),
 				salaryFormLabel.getWidth() - (dateSalarylabel.getWidth() + salaryIssuedForLabel.getWidth()) + 4, 20);
 		salaryPane.setBounds(salaryFormLabel.getX(), dateSalarylabel.getY() + dateSalarylabel.getHeight() - 1, salaryFormLabel.getWidth()
 				+ SCROLLBAR_WIDTH, 105);
 		overallSalaryLabel.setBounds(salaryFormLabel.getX(), salaryPane.getY() + salaryPane.getHeight(), 200, 15);
 		formsOverall.get(10).setBounds(overallSalaryLabel.getX() + overallSalaryLabel.getWidth(), overallSalaryLabel.getY(),
 				salaryPane.getWidth() - overallSalaryLabel.getWidth(), 15);
 
 		depositFormLabel.setBounds(caFormLabel.getX(), overallSalaryLabel.getY() + overallSalaryLabel.getHeight() + TABLE_GAP, 450, 18);
 		dateDepositlabel.setBounds(depositFormLabel.getX() - 1, depositFormLabel.getY() + depositFormLabel.getHeight(), 150, 20);
 		depositorLabel.setBounds(depositFormLabel.getX() + dateDepositlabel.getWidth() - 2, depositFormLabel.getY() + depositFormLabel.getHeight(),
 				depositFormLabel.getWidth() - dateDepositlabel.getWidth() + 2 - TOTAL_LABEL_WIDTH, 20);
 		depositAmountLabel.setBounds(depositorLabel.getX() + depositorLabel.getWidth() - 1, depositFormLabel.getY() + depositFormLabel.getHeight(),
 				depositFormLabel.getWidth() - (dateExpenseslabel.getWidth() + depositorLabel.getWidth()) + 4, 20);
 		depositPane.setBounds(depositFormLabel.getX(), dateDepositlabel.getY() + dateDepositlabel.getHeight() - 1, depositFormLabel.getWidth()
 				+ SCROLLBAR_WIDTH, 105);
 		overallDepositLabel.setBounds(depositFormLabel.getX(), depositPane.getY() + depositPane.getHeight(), 200, 15);
 		formsOverall.get(11).setBounds(overallDepositLabel.getX() + overallDepositLabel.getWidth(), overallDepositLabel.getY(),
 				depositPane.getWidth() - overallDepositLabel.getWidth(), 15);
 
 		discountFormLabel.setBounds(caFormLabel.getX(), overallDepositLabel.getY() + overallDepositLabel.getHeight() + TABLE_GAP, 250, 18);
 		dateDiscountlabel.setBounds(discountFormLabel.getX() - 1, discountFormLabel.getY() + discountFormLabel.getHeight(), 150, 20);
 		discountAmountLabel.setBounds(discountFormLabel.getX() + dateDiscountlabel.getWidth() - 2,
 				discountFormLabel.getY() + discountFormLabel.getHeight(), discountFormLabel.getWidth() - dateDiscountlabel.getWidth() + 3, 20);
 		discountPane.setBounds(discountFormLabel.getX(), dateDiscountlabel.getY() + dateDiscountlabel.getHeight() - 1, discountFormLabel.getWidth()
 				+ SCROLLBAR_WIDTH, 105);
 		overallDiscountLabel.setBounds(discountFormLabel.getX(), discountPane.getY() + discountPane.getHeight(), 100, 15);
 		formsOverall.get(8).setBounds(overallDiscountLabel.getX() + overallDiscountLabel.getWidth(), overallDiscountLabel.getY(),
 				discountPane.getWidth() - overallDiscountLabel.getWidth(), 15);
 
 		// sectionLabel.get(2).setText("LIABILITIES");
 		// sectionLabel.get(2).setBounds(startX, overallCAPaymentLabel.getY() +
 		// overallCAPaymentLabel.getHeight() + SECTION_GAP, 150, 20);
 
 		// ===============================================END OF TRANSACTION
 		// SECTION===========================================================
 
 		sectionLabel.get(3).setText("CASH ON HAND");
 		sectionLabel.get(3).setBounds(startX, overallARPaymentLabel.getY() + overallARPaymentLabel.getHeight() + SECTION_GAP, 150, 20);
 
 		pcohLabel.setBounds(startX, sectionLabel.get(3).getY() + sectionLabel.get(3).getHeight() + LABEL_GAP, 150, 25);
 		assetsLabel.setBounds(pcohLabel.getX(), pcohLabel.getY() + pcohLabel.getHeight(), 150, 25);
 		liabilitiesLabel.setBounds(pcohLabel.getX(), assetsLabel.getY() + assetsLabel.getHeight(), 150, 25);
 		cohLabel.setBounds(pcohLabel.getX(), liabilitiesLabel.getY() + liabilitiesLabel.getHeight(), 150, 30);
 
 		computationLabel.get(0).setBounds(pcohLabel.getX() + pcohLabel.getWidth() - 1, pcohLabel.getY(), 200, 26);
 		computationLabel.get(1).setBounds(computationLabel.get(0).getX(), assetsLabel.getY(), 200, 26);
 		computationLabel.get(2).setBounds(computationLabel.get(0).getX(), liabilitiesLabel.getY(), 200, 26);
 		computationLabel.get(3).setBounds(computationLabel.get(0).getX(), cohLabel.getY(), 200, 30);
 
 		inputPCOH.setBounds(computationLabel.get(0).getX() + computationLabel.get(0).getWidth() + 5, computationLabel.get(0).getY() + 5, 16, 16);
 
 		inputPCOH.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// TODO Auto-generated method stub
 				PointerInfo a = MouseInfo.getPointerInfo();
 				Point b = a.getLocation();
 
 				UtilityPopup uP = new UtilityPopup(b, Values.PCOH);
 				uP.setVisible(true);
 
 				if (uP.getInput().equals("")) {
 					computationLabel.get(0).setText("0.00");
 				} else {
 					computationLabel.get(0).setText(uP.getInput());
 				}
 				inventorySheet.setPreviousAcoh(Double.parseDouble(computationLabel.get(0).getText()));
 				updateCashOnHandSummary();
 			}
 		});
 
 		cashBreakdownPanel.setBounds(computationLabel.get(0).getX() + computationLabel.get(0).getWidth() + TABLE_GAP + TAB + 50, sectionLabel.get(3)
 				.getY() + sectionLabel.get(3).getHeight() + LABEL_GAP, 340, 280);
 
 		sectionLabel.get(4).setText("CASH BREAKDOWN");
 		sectionLabel.get(4).setBounds(cashBreakdownPanel.getX(), overallARPaymentLabel.getY() + overallARPaymentLabel.getHeight() + SECTION_GAP, 150,
 				20);
 
 		for (int i = 0; i < 8; i++) {
 			cashBreakdown.add(new ISRowPanel(this, cashBreakdownPanel, Values.OTHERS));
 			cashBreakdownPanel.add(cashBreakdown.get(i));
 
 			cashBreakdownPanel.updateUI();
 			cashBreakdownPanel.revalidate();
 		}
 
 		accLabel.setBounds(cashBreakdownPanel.getX(), cashBreakdownPanel.getHeight() + cashBreakdownPanel.getY(), 200, 30);
 		actualCashCount.setBounds(accLabel.getX() + accLabel.getWidth(), accLabel.getY(), cashBreakdownPanel.getWidth() - accLabel.getWidth(), 30);
 
 		sectionLabel.get(5).setText("SUMMARY");
 		sectionLabel.get(5).setBounds(startX, cohLabel.getY() + cohLabel.getHeight() + SECTION_GAP, 150, 20);
 
 		summary1Label.setBounds(startX, sectionLabel.get(5).getY() + sectionLabel.get(5).getHeight() + LABEL_GAP, 175, 25);
 		summary2Label.setBounds(summary1Label.getX() + summary1Label.getWidth(), summary1Label.getY(), 175, 25);
 		summary3Label.setBounds(summary2Label.getX() + summary2Label.getWidth(), summary1Label.getY(), 176, 25);
 
 		for (int i = 0, x = 0; i < 3; i++, x += summary1Label.getWidth())
 			summaryValues.get(i).setBounds(summary1Label.getX() + x, summary1Label.getY() + summary1Label.getHeight(), 176, 30);
 
 		isPanel.add(dateLabel);
 		isPanel.add(date);
 
 		isPanel.add(productLabel);
 
 		isPanel.add(begInvtyLabel);
 		isPanel.add(sack1Label);
 		isPanel.add(kg1Label);
 
 		isPanel.add(onDisplayLabel);
 		isPanel.add(sack2Label);
 		isPanel.add(kg2Label);
 
 		isPanel.add(delLabel);
 		isPanel.add(sack3Label);
 		isPanel.add(kg3Label);
 
 		isPanel.add(productsPane);
 		isPanel.add(productTotalLabel);
 
 		isPanel.add(productLabel2);
 
 		isPanel.add(poLabel);
 		isPanel.add(sack4Label);
 		isPanel.add(kg4Label);
 
 		isPanel.add(endInvtyLabel);
 		isPanel.add(sack5Label);
 		isPanel.add(kg5Label);
 
 		isPanel.add(offtakeLabel);
 		isPanel.add(sack6Label);
 		isPanel.add(kg6Label);
 
 		isPanel.add(priceLabel);
 		isPanel.add(sack7Label);
 		isPanel.add(kg7Label);
 
 		isPanel.add(salesLabel);
 		isPanel.add(sack8Label);
 		isPanel.add(kg8Label);
 
 		isPanel.add(productsPane2);
 		isPanel.add(productTotalLabel2);
 
 		isPanel.add(salesFormLabel);
 		isPanel.add(dateSaleslabel);
 		isPanel.add(cashierLabel);
 		isPanel.add(grossSalesLabel);
 		isPanel.add(salesPane);
 		isPanel.add(overallSalesLabel);
 
 		isPanel.add(deliveryFormLabel);
 		isPanel.add(dateDellabel);
 		isPanel.add(delReceivedByLabel);
 		isPanel.add(grossDelLabel);
 		isPanel.add(delPane);
 		isPanel.add(overallDelLabel);
 
 		isPanel.add(expensesFormLabel);
 		isPanel.add(dateExpenseslabel);
 		isPanel.add(expensesIssuedByLabel);
 		isPanel.add(grossExpensesLabel);
 		isPanel.add(expensesPane);
 		isPanel.add(overallExpensesLabel);
 
 		isPanel.add(pullOutFormLabel);
 		isPanel.add(datePulloutlabel);
 		isPanel.add(pulloutIssuedByLabel);
 		isPanel.add(grossPulloutLabel);
 		isPanel.add(pulloutPane);
 		isPanel.add(overallPulloutLabel);
 
 		isPanel.add(arFormLabel);
 		isPanel.add(dateARlabel);
 		isPanel.add(arIssuedByLabel);
 		isPanel.add(grossARLabel);
 		isPanel.add(arPane);
 		isPanel.add(overallARLabel);
 
 		isPanel.add(arPaymentFormLabel);
 		isPanel.add(dateARPaymentlabel);
 		isPanel.add(arPaymentIssuedByLabel);
 		isPanel.add(grossARPaymentLabel);
 		isPanel.add(arPaymentPane);
 		isPanel.add(overallARPaymentLabel);
 
 		isPanel.add(caFormLabel);
 		isPanel.add(dateCAlabel);
 		isPanel.add(CAIssuedByLabel);
 		isPanel.add(grossCALabel);
 		isPanel.add(caPane);
 		isPanel.add(overallCALabel);
 
 		isPanel.add(caPaymentFormLabel);
 		isPanel.add(dateCAPaymentlabel);
 		isPanel.add(caPaymentIssuedByLabel);
 		isPanel.add(grossCAPaymentLabel);
 		isPanel.add(caPaymentPane);
 		isPanel.add(overallCAPaymentLabel);
 
 		isPanel.add(salaryFormLabel);
 		isPanel.add(dateSalarylabel);
 		isPanel.add(salaryIssuedForLabel);
 		isPanel.add(salaryAmountLabel);
 		isPanel.add(salaryPane);
 		isPanel.add(overallSalaryLabel);
 
 		isPanel.add(depositFormLabel);
 		isPanel.add(dateDepositlabel);
 		isPanel.add(depositorLabel);
 		isPanel.add(depositAmountLabel);
 		isPanel.add(depositPane);
 		isPanel.add(overallDepositLabel);
 
 		isPanel.add(discountFormLabel);
 		isPanel.add(dateDiscountlabel);
 		isPanel.add(discountAmountLabel);
 		isPanel.add(discountPane);
 		isPanel.add(overallDiscountLabel);
 
 		isPanel.add(inputPCOH);
 
 		isPanel.add(pcohLabel);
 		isPanel.add(assetsLabel);
 		isPanel.add(liabilitiesLabel);
 		isPanel.add(cohLabel);
 
 		isPanel.add(cashBreakdownPanel);
 
 		isPanel.add(summary1Label);
 		isPanel.add(summary2Label);
 		isPanel.add(summary3Label);
 		//
 		isPanel.add(accLabel);
 		isPanel.add(actualCashCount);
 
 		isPane.setBounds(10, 30, PANE_WIDTH, PANE_HEIGHT);
 		navigationPanel.setBounds(isPane.getWidth() - 190, isPane.getY() + isPane.getHeight() - 100, 200, 100);
 
 		isPane.setOpaque(false);
 		isPane.getViewport().setOpaque(false);
 		isPane.setBorder(BorderFactory.createEmptyBorder());
 
 		save.addMouseListener(new MouseAdapter() {
 			public void mouseClicked(MouseEvent e) {
 
 				Date d = (Date) date.getSelectedItem();
 				inventorySheet.getInventorySheetData().setDate(DateTool.getDateWithoutTime(d));
 				inventorySheet.getInventorySheetData().setPreviousAcoh(Double.parseDouble(computationLabel.get(0).getText()));
 				inventorySheet.getInventorySheetData().setOverAmount(0d);
 				inventorySheet.getInventorySheetData().setShortAmount(0d);
 
 				double coh = Double.parseDouble(summaryValues.get(0).getText());
 				double acc = Double.parseDouble(summaryValues.get(1).getText());
 				if (coh > acc) {
 					inventorySheet.getInventorySheetData().setShortAmount(coh - acc);
 				} else if (coh < acc) {
 					inventorySheet.getInventorySheetData().setOverAmount(acc - coh);
 				}
 
 				inventorySheet.getInventorySheetData().setIssuedBy(Manager.loggedInAccount);
 				inventorySheet.getInventorySheetData().setRemarks("");
 
 				// breakdown
 				Breakdown breakdown = new Breakdown(cashBreakdown.get(6).getCashBreakdownRowQuantity(), cashBreakdown.get(7)
 						.getCashBreakdownRowQuantity(), inventorySheet.getInventorySheetData());
 
 				for (int i = 0; i < 6; i++) {
 
 					try {
 
 						if (cashBreakdown.get(i).getCashBreakdownRowQuantity() != 0) {
 							Denomination den = Manager.inventorySheetDataManager.searchDenomination(cashBreakdown.get(i).getCashBreakdownRowDenomination(i));
 							if (den == null)
 								den = new Denomination(cashBreakdown.get(i).getCashBreakdownRowDenomination(i));
 							breakdown.addBreakdownLine(new BreakdownLine(breakdown, den, cashBreakdown.get(i).getCashBreakdownRowQuantity()));
 						}
 					} catch (Exception e1) {
 						e1.printStackTrace();
 					}
 				}
 
 				inventorySheet.getInventorySheetData().setBreakdown(breakdown);
 				inventorySheet.finalize();
 
 				try {
 					Manager.inventorySheetDataManager.addInventorySheetData(inventorySheet.getInventorySheetData());
 					for (Product p : products) {
 						p.resetBeginningInventory();
 						Manager.productManager.updateProduct(p);
 					}
 
 				} catch (Exception e1) {
 					e1.printStackTrace();
 				}
 
 			}
 		});
 
 		navigationPanel.add(save);
 
 		add(navigationPanel);
 		add(isPane);
 
 	}
 
 	private void fillTables() {
 		// TODO Auto-generated method stub
 		// productsPanel.setPreferredSize(new Dimension(salesLabel.getX() +
 		// salesLabel.getWidth() - productLabel.getX(),
 		// productsPanel.getComponentCount() * ROW_HEIGHT));
 
 		for (int i = 0; i < 28; i++) {
 			productsInventory.add(new ISRowPanel(null, productsPanel, Values.PRODUCTS));
 			productsPanel.add(productsInventory.get(i));
 
 			productsPanel.setPreferredSize(new Dimension(productsPanel.getWidth(), productsPanel.getComponentCount() * ROW_HEIGHT));
 			productsPanel.updateUI();
 			productsPanel.revalidate();
 		}
 
 		alternateRows(productsInventory);
 
 		// System.out.println("productsPanel.getComponentCount(): "+productsPanel.getComponentCount());
 
 		for (int i = 0; i < 3; i++) {
 			salesInventory.add(new ISRowPanel(null, salesPanel, Values.SALES));
 			salesPanel.add(salesInventory.get(i));
 
 			salesPanel.setPreferredSize(new Dimension(salesPanel.getWidth(), salesPanel.getComponentCount() * ROW_HEIGHT));
 			salesPanel.updateUI();
 			salesPanel.revalidate();
 		}
 
 		alternateRows(salesInventory);
 
 	}
 
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	public void loadDatesOfPendingTransactions() throws Exception {
 
 		List<Date> deliveriesDates = Manager.deliveryManager.getDatesOfPendingDeliveries();
 		List<Date> pullOutsDates = Manager.pullOutManager.getDatesOfPendingPullOuts();
 		List<Date> salesDates = Manager.salesManager.getDatesOfPendingSales();
 		List<Date> accountReceivablesDates = Manager.accountReceivableManager.getDatesOfPendingAccountReceivables();
 		List<Date> arPaymentsDates = Manager.accountReceivableManager.getDatesOfPendingARPayments();
 		List<Date> cashAdvancesDates = Manager.cashAdvanceManager.getDatesOfPendingCashAdvances();
 		List<Date> caPaymentsDates = Manager.cashAdvanceManager.getDatesOfPendingCAPayments();
 		List<Date> dailyExpensesDates = Manager.dailyExpenseManager.getDatesOfPendingDailyExpenses();
 		List<Date> salaryReleasesDates = Manager.salaryReleaseManager.getDatesOfPendingSalaryReleases();
 		List<Date> discountIssuesDates = Manager.discountIssueManager.getDatesOfPendingDiscountIssues();
 		List<Date> depositsDates = Manager.depositManager.getDatesOfPendingDeposits();
 
 		validDates = new ArrayList<Date>();
 
 		validDates = DateTool.addUniqueDatesRemoveTime(validDates, deliveriesDates);
 		validDates = DateTool.addUniqueDatesRemoveTime(validDates, pullOutsDates);
 		validDates = DateTool.addUniqueDatesRemoveTime(validDates, salesDates);
 		validDates = DateTool.addUniqueDatesRemoveTime(validDates, accountReceivablesDates);
 		validDates = DateTool.addUniqueDatesRemoveTime(validDates, arPaymentsDates);
 		validDates = DateTool.addUniqueDatesRemoveTime(validDates, cashAdvancesDates);
 		validDates = DateTool.addUniqueDatesRemoveTime(validDates, caPaymentsDates);
 		validDates = DateTool.addUniqueDatesRemoveTime(validDates, dailyExpensesDates);
 		validDates = DateTool.addUniqueDatesRemoveTime(validDates, salaryReleasesDates);
 		validDates = DateTool.addUniqueDatesRemoveTime(validDates, discountIssuesDates);
 		validDates = DateTool.addUniqueDatesRemoveTime(validDates, depositsDates);
 
 		validDates = DateTool.sortDateEarliestFirst(validDates);
 
 		if (validDates.size() > 0) {
 			date.setModel(new DefaultComboBoxModel(validDates.toArray()));
 		}
 	}
 
 	public boolean build() throws Exception {
 
 		Component[] components = isPanel.getComponents();
 		for (Component component : components) {
 			if (component instanceof PDControlScrollPane) {
 				((PDControlScrollPane) component).setOpaque(false);
 				((PDControlScrollPane) component).getViewport().setOpaque(false);
 				((PDControlScrollPane) component).setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 				((PDControlScrollPane) component).setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 			}
 		}
 
 		if (date.getItemCount() > 0) {
 			Date selectedDate = (Date) date.getSelectedItem();
 			System.out.println("selected date: " + selectedDate.toString());
 			fillEntries(selectedDate);
 			return true;
 		}
 
 		return false;
 	}
 
 	@SuppressWarnings("unchecked")
 	public void build(Date d) throws Exception {
 
 		Component[] components = isPanel.getComponents();
 		for (Component component : components) {
 			if (component instanceof PDControlScrollPane) {
 				((PDControlScrollPane) component).setOpaque(false);
 				((PDControlScrollPane) component).getViewport().setOpaque(false);
 				((PDControlScrollPane) component).setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 				((PDControlScrollPane) component).setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 			}
 		}
 
 		date.addItem(d);
 		fillEntries(d);
 
 	}
 
 	private void fillEntries(Date date) throws Exception {
 
 		clearForm();
 
 		double previousAcoh = 0d;
 		InventorySheetData isd = Manager.inventorySheetDataManager.getMostRecentInventorySheetData();
 
 		inputPCOH.setVisible(isd == null);
 		if (isd != null) {
 			InventorySheet is = new InventorySheet(isd);
 			previousAcoh = is.getActualCashOnHand();
 		}
 
 		products = Manager.productManager.getProducts();
 
 		deliveries = Manager.deliveryManager.getPendingDeliveriesOn(date);
 		pullOuts = Manager.pullOutManager.getPendingPullOutsOn(date);
 		sales = Manager.salesManager.getPendingSalesOn(date);
 		accountReceivables = Manager.accountReceivableManager.getPendingAccountReceivablesOn(date);
 		arPayments = Manager.accountReceivableManager.getPendingARPaymentsOn(date);
 		cashAdvances = Manager.cashAdvanceManager.getPendingCashAdvancesOn(date);
 		caPayments = Manager.cashAdvanceManager.getPendingCAPaymentsOn(date);
 		dailyExpenses = Manager.dailyExpenseManager.getPendingDailyExpensesOn(date);
 		salaryReleases = Manager.salaryReleaseManager.getPendingSalaryReleasesOn(date);
 		discountIssues = Manager.discountIssueManager.getPendingDiscountIssuesOn(date);
 		deposits = Manager.depositManager.getPendingDepositsOn(date);
 
 		InventorySheetData inventorySheetData = new InventorySheetData();
 		inventorySheetData = new InventorySheetData(new Date(), 0, 0, 0, Manager.loggedInAccount);
 		for (Product p : products) {
 			inventorySheetData.addInventorySheetDataDetail(new InventorySheetDataDetail(inventorySheetData, p, p.getBeginningInventoryInSack(), p
 					.getBeginningInventoryInKilo(), p.getQuantityOnDisplayInSack(), p.getQuantityInKilo(), p.getCurrentPricePerSack(), p
 					.getCurrentPricePerKilo()));
 		}
 
 		inventorySheet = new InventorySheet(inventorySheetData, new HashSet<Delivery>(deliveries), new HashSet<PullOut>(pullOuts), new HashSet<Sales>(
 				sales), new HashSet<AccountReceivable>(accountReceivables), new HashSet<DiscountIssue>(discountIssues),
 				new HashSet<ARPayment>(arPayments), new HashSet<CAPayment>(caPayments), new HashSet<DailyExpenses>(dailyExpenses),
 				new HashSet<CashAdvance>(cashAdvances), new HashSet<SalaryRelease>(salaryReleases), new HashSet<Deposit>(deposits));
 
 		fillProductInventories(products);
 		fillSales(sales);
 		fillDeliveries(deliveries);
 		fillDiscount(discountIssues);
 		fillAR(accountReceivables);
 		fillARPayment(arPayments);
 		fillCAPayment(caPayments);
 		fillPullOut(pullOuts);
 		fillDailyExpenses(dailyExpenses);
 		fillCashAdvances(cashAdvances);
 		fillSalaryRelease(salaryReleases);
 		fillDeposit(deposits);
 
 		actualCashCount.setText(String.format("%.2f", new Double(0)));
 
 		if (isd != null)
 			pcohLabel.setToolTipText("Cash On Hand from last Inventory Sheet on "
 					+ DateFormatter.getInstance().getFormat(Utility.DMYHMAFormat).format(isd.getDate()));
 		else {
 			pcohLabel.setToolTipText("Cash On Hand from last Inventory Sheet");
 		}
 		assetsLabel.setToolTipText("Previous COH + Sales + Account Receivables + Account Receivables Payments + Cash Advance Payments");
 		liabilitiesLabel.setToolTipText("Expenses + Salary Releases + Cash Advances + Account Receivables + Discounts + Deposits");
 		cohLabel.setToolTipText("Previous COH + Assets - Liabilities");
 
 		computationLabel.get(0).setText(String.format("%.2f", previousAcoh));
 		computationLabel.get(1).setText(String.format("%.2f", inventorySheet.getTotalAssets()));
 		computationLabel.get(2).setText(String.format("%.2f", inventorySheet.getTotalLiabilities()));
 		computationLabel.get(3).setText(String.format("%.2f", inventorySheet.getActualCashOnHand()));
 
 		summaryValues.get(0).setText(String.format("%.2f", inventorySheet.getActualCashOnHand()));
 		summaryValues.get(1).setText(String.format("%.2f", inventorySheet.getActualCashCount()));
 		summaryValues.get(2).setText(String.format("%.2f", new Double(0)));
 		summary3Label.setText("OVER/SHORT");
 
 		inventorySheet.setPreviousAcoh(Double.parseDouble(computationLabel.get(0).getText()));
 		updateCashOnHandSummary();
 		updateActualCashCountAndSummary();
 	}
 
 	private void alternateRows(ArrayList<ISRowPanel> rowPanel) {
 		for (int i = 0; i < rowPanel.size(); i++)
 			if (i % 2 == 0)
 				rowPanel.get(i).getRow().setBackground(Values.row1);
 			else
 				rowPanel.get(i).getRow().setBackground(Values.row2);
 	}
 
 	private void fillProductInventories(List<Product> products) {
 		int i = 0;
 		for (Product p : products) {
 			InventorySheetDetail isd = inventorySheet.getInventorySheetDetail(p.getId());
 			productsInventory.add(new ISRowPanel(isd, productsPanel, Values.PRODUCTS));
 			productsPanel.add(productsInventory.get(i));
 			productsPanel.setPreferredSize(new Dimension(productsPanel.getWidth(), productsPanel.getComponentCount() * ROW_HEIGHT));
 			productsPanel.updateUI();
 			productsPanel.revalidate();
 			i++;
 		}
 		alternateRows(productsInventory);
 
 		i = 0;
 		for (Product p : products) {
 			InventorySheetDetail isd = inventorySheet.getInventorySheetDetail(p.getId());
 			productsInventory2.add(new ISRowPanel(isd, productsPanel2, Values.PRODUCT_TWIN));
 			productsPanel2.add(productsInventory2.get(i));
 			productsPanel2.setPreferredSize(new Dimension(productsPanel2.getWidth(), productsPanel2.getComponentCount() * ROW_HEIGHT));
 			productsPanel2.updateUI();
 			productsPanel2.revalidate();
 			i++;
 		}
 		alternateRows(productsInventory2);
 
 		fillProductInventoriesTotal();
 
 	}
 
 	private void fillProductInventoriesTotal() {
 		totalInventoryLabel.get(0).setText(String.format("%.2f", inventorySheet.getTotalBeginningInventoryInSack()));
 		totalInventoryLabel.get(1).setText(String.format("%.2f", inventorySheet.getTotalBeginningInventoryInKilo()));
 		totalInventoryLabel.get(2).setText(String.format("%.2f", inventorySheet.getTotalOnDisplayInSack()));
 		totalInventoryLabel.get(3).setText(String.format("%.2f", inventorySheet.getTotalOnDisplayInKilo()));
 		totalInventoryLabel.get(4).setText(String.format("%.2f", inventorySheet.getTotalDeliveriesInSack()));
 		totalInventoryLabel.get(5).setText(String.format("%.2f", inventorySheet.getTotalDeliveriesInKilo()));
 		totalInventoryLabel.get(6).setText(String.format("%.2f", inventorySheet.getTotalPulloutsInSack()));
 		totalInventoryLabel.get(7).setText(String.format("%.2f", inventorySheet.getTotalPulloutsInKilo()));
 		totalInventoryLabel.get(8).setText(String.format("%.2f", inventorySheet.getTotalEndingInventoryInSack()));
 		totalInventoryLabel.get(9).setText(String.format("%.2f", inventorySheet.getTotalEndingInventoryInKilo()));
 		totalInventoryLabel.get(10).setText(String.format("%.2f", inventorySheet.getTotalOfftakesInSack()));
 		totalInventoryLabel.get(11).setText(String.format("%.2f", inventorySheet.getTotalOfftakesInKilo()));
 		totalInventoryLabel.get(12).setText(String.format("%.2f", inventorySheet.getTotalPricesInSack()));
 		totalInventoryLabel.get(13).setText(String.format("%.2f", inventorySheet.getTotalPricesInKilo()));
 		totalInventoryLabel.get(14).setText(String.format("%.2f", inventorySheet.getCombinedSalesAmountInSack()));
 		totalInventoryLabel.get(15).setText(String.format("%.2f", inventorySheet.getCombinedSalesAmountInKilo()));
 	}
 
 	private void fillSales(List<Sales> sales) {
 		int i = 0;
 		for (Sales s : sales) {
 			salesInventory.add(new ISRowPanel(s, salesPanel, Values.SALES));
 			salesPanel.add(salesInventory.get(i));
 			salesPanel.setPreferredSize(new Dimension(TRANSACTIONS_ROW_WIDTH, salesPanel.getComponentCount() * ROW_HEIGHT));
 			salesPanel.updateUI();
 			salesPanel.revalidate();
 			i++;
 		}
 		alternateRows(salesInventory);
 
 		// total sales
 
 		formsOverall.get(0).setText(String.format("%.2f", inventorySheet.getOverallCashAndCheckSalesAmount()));
 
 	}
 
 	private void fillDeliveries(List<Delivery> deliveries) {
 		int i = 0;
 		for (Delivery d : deliveries) {
 			deliveryInventory.add(new ISRowPanel(d, delPanel, Values.DELIVERY));
 			delPanel.add(deliveryInventory.get(i));
 			delPanel.setPreferredSize(new Dimension(TRANSACTIONS_ROW_WIDTH, delPanel.getComponentCount() * ROW_HEIGHT));
 			delPanel.updateUI();
 			delPanel.revalidate();
 			i++;
 		}
 
 		alternateRows(deliveryInventory);
 		formsOverall.get(1).setText(String.format("%.2f", inventorySheet.getOverallCostOfDeliveries()));
 	}
 
 	private void fillAR(List<AccountReceivable> ar) {
 		int i = 0;
 		for (AccountReceivable a_r : ar) {
 			arInventory.add(new ISRowPanel(a_r, arPanel, Values.ACCOUNT_RECEIVABLES));
 			arPanel.add(arInventory.get(i));
 			arPanel.setPreferredSize(new Dimension(TRANSACTIONS_ROW_WIDTH, arPanel.getComponentCount() * ROW_HEIGHT));
 			arPanel.updateUI();
 			arPanel.revalidate();
 			i++;
 		}
 
 		alternateRows(arInventory);
 		formsOverall.get(2).setText(String.format("%.2f", inventorySheet.getOverallAccountReceivables()));
 	}
 
 	private void fillARPayment(List<ARPayment> arP) {
 		int i = 0;
 		for (ARPayment arp : arP) {
 			arPaymentInventory.add(new ISRowPanel(arp, arPaymentPanel, Values.AR_PAYMENTS));
 			arPaymentPanel.add(arPaymentInventory.get(i));
 			arPaymentPanel.setPreferredSize(new Dimension(TRANSACTIONS_ROW_WIDTH, arPaymentPanel.getComponentCount() * ROW_HEIGHT));
 			arPaymentPanel.updateUI();
 			arPaymentPanel.revalidate();
 			i++;
 		}
 
 		alternateRows(arPaymentInventory);
 
 		formsOverall.get(3).setText(String.format("%.2f", inventorySheet.getOverallAccountReceivablesPayments()));
 	}
 
 	private void fillCAPayment(List<CAPayment> caP) {
 		int i = 0;
 
 		for (CAPayment cap : caP) {
 			caPaymentInventory.add(new ISRowPanel(cap, caPaymentPanel, Values.CA_PAYMENTS));
 			caPaymentPanel.add(caPaymentInventory.get(i));
 			caPaymentPanel.setPreferredSize(new Dimension(TRANSACTIONS_ROW_WIDTH, caPaymentPanel.getComponentCount() * ROW_HEIGHT));
 			caPaymentPanel.updateUI();
 			caPaymentPanel.revalidate();
 			i++;
 		}
 		alternateRows(caPaymentInventory);
 
 		formsOverall.get(4).setText(String.format("%.2f", inventorySheet.getOverallCashAdvancesPayments()));
 	}
 
 	private void fillPullOut(List<PullOut> pullOut) {
 		int i = 0;
 		for (PullOut po : pullOut) {
 			pullOutInventory.add(new ISRowPanel(po, pulloutPanel, Values.PULLOUT));
 			pulloutPanel.add(pullOutInventory.get(i));
 			pulloutPanel.setPreferredSize(new Dimension(250, pulloutPane.getComponentCount() * ROW_HEIGHT));
 			pulloutPanel.updateUI();
 			pulloutPanel.revalidate();
 			i++;
 		}
 
 		alternateRows(pullOutInventory);
 
 		formsOverall.get(5).setText(String.format("%.2f", inventorySheet.getOverallCostOfPullouts()));
 	}
 
 	private void fillDailyExpenses(List<DailyExpenses> expenses) {
 		int i = 0;
 		for (DailyExpenses de : expenses) {
 			expensesInventory.add(new ISRowPanel(de, expensesPanel, Values.EXPENSES));
 			expensesPanel.add(expensesInventory.get(i));
 			expensesPanel.setPreferredSize(new Dimension(250, expensesPane.getComponentCount() * ROW_HEIGHT));
 			expensesPanel.updateUI();
 			expensesPanel.revalidate();
 			i++;
 		}
 
 		alternateRows(expensesInventory);
 		formsOverall.get(6).setText(String.format("%.2f", inventorySheet.getOverallExpenses()));
 	}
 
 	private void fillCashAdvances(List<CashAdvance> cashAdvances) {
 		int i = 0;
 		for (CashAdvance ca : cashAdvances) {
 			caInventory.add(new ISRowPanel(ca, caPanel, Values.CASH_ADVANCE));
 			caPanel.add(caInventory.get(i));
 			caPanel.setPreferredSize(new Dimension(250, caPane.getComponentCount() * ROW_HEIGHT));
 			caPanel.updateUI();
 			caPanel.revalidate();
 			i++;
 		}
 
 		alternateRows(caInventory);
 		formsOverall.get(7).setText(String.format("%.2f", inventorySheet.getOverallCashAdvances()));
 	}
 
 	private void fillDiscount(List<DiscountIssue> discount) {
 		int i = 0;
 		for (DiscountIssue disc : discount) {
 			discountInventory.add(new ISRowPanel(disc, discountPanel, Values.DISCOUNTS));
 			discountPanel.add(discountInventory.get(i));
 			discountPanel.setPreferredSize(new Dimension(250, discountPanel.getComponentCount() * ROW_HEIGHT));
 			discountPanel.updateUI();
 			discountPanel.revalidate();
 			i++;
 		}
 
 		alternateRows(discountInventory);
 		formsOverall.get(8).setText(String.format("%.2f", inventorySheet.getOverallDiscounts()));
 	}
 
 	private void fillSalaryRelease(List<SalaryRelease> salaryReleases) {
 		int i = 0;
 		for (SalaryRelease sr : salaryReleases) {
 			salaryInventory.add(new ISRowPanel(sr, salaryPanel, Values.SALARY));
 			salaryPanel.add(salaryInventory.get(i));
 			salaryPanel.setPreferredSize(new Dimension(250, salaryPanel.getComponentCount() * ROW_HEIGHT));
 			salaryPanel.updateUI();
 			salaryPanel.revalidate();
 			i++;
 		}
 
 		alternateRows(salaryInventory);
 		formsOverall.get(10).setText(String.format("%.2f", inventorySheet.getOverallSalaryReleases()));
 	}
 
 	private void fillDeposit(List<Deposit> deposits) {
 		int i = 0;
 		for (Deposit d : deposits) {
 			depositInventory.add(new ISRowPanel(d, depositPanel, Values.DEPOSITS));
 			depositPanel.add(depositInventory.get(i));
 			depositPanel.setPreferredSize(new Dimension(250, depositPanel.getComponentCount() * ROW_HEIGHT));
 			depositPanel.updateUI();
 			depositPanel.revalidate();
 			i++;
 		}
 		alternateRows(depositInventory);
 		formsOverall.get(11).setText(String.format("%.2f", inventorySheet.getOverallDeposits()));
 	}
 
 	public void updateCashOnHandSummary() {
 
 		double pcoh = Double.parseDouble(computationLabel.get(0).getText());
 		double tAssets = Double.parseDouble(computationLabel.get(1).getText());
 		double tLiabilities = Double.parseDouble(computationLabel.get(2).getText());
 		double acoh = (pcoh + tAssets) - tLiabilities;
 		computationLabel.get(3).setText(String.format("%.2f", acoh));
 		summaryValues.get(0).setText(String.format("%.2f", acoh));
 		double acc = Double.parseDouble(summaryValues.get(0).getText());
 		summaryValues.get(2).setText(String.format("%.2f", InventorySheet.overOrShortAmount(acoh, acc)));
 		summary3Label.setText(InventorySheet.overOrShortCaps(acoh, acc));
 
 	}
 
 	public void updateActualCashCountAndSummary() {
 
 		double total = 0d;
 		for (int i = 0; i < 8; i++) {
 			double value = 0;
 			try {
 				value = cashBreakdown.get(i).getCashBreakdownRowTotal();
 			} catch (Exception e) {
 			}
 			total += value;
 		}
 
 		actualCashCount.setText(String.format("%.2f", total));
 		summaryValues.get(1).setText(String.format("%.2f", total));
 		double acoh = Double.parseDouble(summaryValues.get(0).getText());
 		summaryValues.get(2).setText(String.format("%.2f", InventorySheet.overOrShortAmount(acoh, total)));
 		summary3Label.setText(InventorySheet.overOrShortCaps(acoh, total));
 
 	}
 
 	private void clearForm() {
 		productsInventory.clear();
 		productsInventory2.clear();
 
 		salesInventory.clear();
 		deliveryInventory.clear();
 		arInventory.clear();
 		arPaymentInventory.clear();
 		caPaymentInventory.clear();
 		pullOutInventory.clear();
 		expensesInventory.clear();
 		caInventory.clear();
 		discountInventory.clear();
 		salaryInventory.clear();
 		depositInventory.clear();
 
 		productsPanel.removeAll();
 		productsPanel2.removeAll();
 		salesPanel.removeAll();
 		delPanel.removeAll();
 		arPanel.removeAll();
 		arPaymentPanel.removeAll();
 		caPaymentPanel.removeAll();
 		pulloutPanel.removeAll();
 		expensesPanel.removeAll();
 		caPanel.removeAll();
 		discountPanel.removeAll();
 		salaryPanel.removeAll();
 		depositPanel.removeAll();
 
 		for (int i = 0; i < cashBreakdown.size(); i++) {
 			cashBreakdown.get(i).getField().setText("0");
 			cashBreakdown.get(i).getTotalLabel().setText("0.0");
 		}
 
 	}
 }
