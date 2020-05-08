 package custom.forms.stockdocuments;
 
 import java.math.BigDecimal;
 import java.text.DecimalFormat;
 import java.util.Date;
 
 import generic.events.TableDataChangedEvent;
 import generic.listeners.TableDataChangedListener;
 import hibernate.entityBeans.StockDocument;
 import hibernate.entityBeans.StockDocumentItem;
 import hibernate.entityBeans.StockDocumentPayment;
 
 import javax.swing.BorderFactory;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import util.DateUtil;
 import utils.GlobalVariables;
 
 import layouts.RiverLayout;
 import localization.Local;
 
 public class DocumentSummaryInfoPanel extends JPanel implements TableDataChangedListener{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 2193111605424423607L;
 	
 	private JLabel totalPaymentsLabel, totalBasicPriceLabel, totalTaxLabel, totalPriceLabel, payPeriodLabel;
 	
 	private JTextField totalPaymentsField, totalBasicPriceField, totalTaxField, totalPriceField, payPeriodField;
 	
 	private StockDocument stockDocument = null;
 	
 	private static int DEFAULT_PAY_PERIOD = 30;
 	
 	private DecimalFormat currencyFormatter = new DecimalFormat(GlobalVariables.SERBIAN_PRICE_FORMAT);
 	
 	public DocumentSummaryInfoPanel(){
 		initComponents();
 		addComponents();
 	}
 	
 	private void initComponents(){
 		totalPaymentsLabel = new JLabel(Local.getString("STOCKDOCUMENT.SUMMARYINFO.TOTALPAYMENTS"));
 		totalBasicPriceLabel = new JLabel(Local.getString("STOCKDOCUMENT.SUMMARYINFO.BASICPRICE"));
 		totalTaxLabel = new JLabel(Local.getString("STOCKDOCUMENT.SUMMARYINFO.TOTALTAX"));
 		totalPriceLabel = new JLabel(Local.getString("STOCKDOCUMENT.SUMMARYINFO.TOTALPRICE"));
 		payPeriodLabel = new JLabel(Local.getString("STOCKDOCUMENT.SUMMARYINFO.PAYPERIOD"));
 		
 		totalBasicPriceField = new JTextField(25);
 		totalBasicPriceField.setEnabled(false);
 		totalBasicPriceField.setHorizontalAlignment(JTextField.RIGHT);
 		totalTaxField = new JTextField(25);
 		totalTaxField.setEnabled(false);
 		totalTaxField.setHorizontalAlignment(JTextField.RIGHT);
 		totalPriceField = new JTextField(25);
 		totalPriceField.setEnabled(false);
 		totalPriceField.setHorizontalAlignment(JTextField.RIGHT);
 		payPeriodField = StockDocumentInputForm.getTextField("Integer", true);
 		totalPaymentsField = new JTextField(25);
 		totalPaymentsField.setEnabled(false);
 		totalPaymentsField.setHorizontalAlignment(JTextField.RIGHT);
 		
 		setLayout(new RiverLayout());
 		setBorder(BorderFactory.createTitledBorder(Local.getString("STOCKDOCUMENT.SUMMARYINFO.TITLE")));
 	}
 	
 	public void setStockDocument(StockDocument stockDocument) {
 		this.stockDocument = stockDocument;
 	}
 	
 	public void bindData(){
		BigDecimal price = new BigDecimal("0.00"), basicprice=new BigDecimal("0.00"), taxprice = new BigDecimal("0.00");
		price.setScale(2, BigDecimal.ROUND_CEILING);
		basicprice.setScale(2, BigDecimal.ROUND_CEILING);
		taxprice.setScale(2, BigDecimal.ROUND_CEILING);
		
 		int payPeriod = DEFAULT_PAY_PERIOD;
 		
 		for(StockDocumentItem si: stockDocument.getItems()){
			basicprice = basicprice.add(new BigDecimal(si.getBasicPrice()));
			taxprice = taxprice.add(new BigDecimal(si.getPdvPrice()));
			price = price.add(new BigDecimal((si.getBasicPrice() + si.getPdvPrice())));
 		}
 		
 		if(stockDocument.getPaymentDay()!=null){
 			payPeriod = DateUtil.getDateDayDifference(stockDocument.getCreationTime(), stockDocument.getPaymentDay());
 		}
 		
 		totalBasicPriceField.setText(currencyFormatter.format(basicprice));
 		totalPriceField.setText(currencyFormatter.format(price));
 		totalTaxField.setText(currencyFormatter.format(taxprice));
 		
		BigDecimal totalPayments = new BigDecimal("0.00");
		totalPayments.setScale(2, BigDecimal.ROUND_CEILING);
		
 		if(stockDocument.getPayments()!=null){
 			for(StockDocumentPayment pay: stockDocument.getPayments()){
 				totalPayments = totalPayments.add(new BigDecimal(pay.getAmount()));
 			}
 		}
 		
 		totalPaymentsField.setText(currencyFormatter.format(totalPayments));
 		
 		payPeriodField.setText(Integer.toString(payPeriod));
 	}
 	
 	public void unbindData(){
 		int payPeriod = DEFAULT_PAY_PERIOD;
 		if(!payPeriodField.getText().equals("")){
 			payPeriod = Integer.parseInt(payPeriodField.getText());
 		}
 		
 		if(stockDocument.getCreationTime()==null){
 			stockDocument.setCreationTime(new Date());
 		}
 		
 		stockDocument.setPaymentDay(DateUtil.addDaysToDate(stockDocument.getCreationTime(), payPeriod));
 	}
 	
 	private void addComponents(){
 		add("p left", totalBasicPriceLabel);
 		add("tab hfill",totalBasicPriceField);
 		add("br", totalTaxLabel);
 		add("tab hfill",totalTaxField);
 		add("br", totalPriceLabel);
 		add("tab hfill",totalPriceField);
 		add("br", payPeriodLabel);
 		add("tab hfill",payPeriodField);
 		add("br", totalPaymentsLabel);
 		add("tab hfill",totalPaymentsField);
 	}
 
 	@Override
 	public void updateTable(TableDataChangedEvent tbce) {
 		bindData();
 	}
 }
