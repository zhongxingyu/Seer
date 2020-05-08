 package com.cs174.starrus.view;
 
 import java.awt.Dimension;
 import java.awt.Point;
 import java.util.Vector;
 import javax.swing.JDialog;
 import javax.swing.JTable;
 import javax.swing.JScrollPane;
 import javax.swing.JLabel;
 
 
 public class MonthlyStatementView extends JDialog implements IView{
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID 	= 1L;
 	private static MonthlyStatementView msView 	= null;
 
 	private	Vector<Vector<String>> row_mAccount			= new Vector<Vector<String>>();
 	private	Vector<Vector<String>> row_sAccount			= new Vector<Vector<String>>();
 	private JLabel lblUsername;
 	private JLabel lblUsernameField;
 	private JScrollPane scrollPane;
 	private JTable table;
 	private JScrollPane scrollPane_1;
 	private JTable table_1;
 	private JLabel lblEmail;
 	private JLabel lblEmailfield;
 	private JLabel lblInitialBalance;
 	private JLabel lblInitialbalancefield;
 	private JLabel lblFinalBalance;
 	private JLabel lblFinalbalancefield;
 	private JLabel lblGainloss;
 	private JLabel lblGainlossfield;
 	private JLabel lblTotalCommission;
 	private JLabel lblCommission;
 	private JLabel lblMoneyTransactions;
 	private JLabel lblStockTransactions;
 	
 	private MonthlyStatementView(){
 		this.setLocation(new Point(350, 200));
 		this.setSize(new Dimension(800, 600));
 		this.setPreferredSize(new Dimension(800, 400));
 	    getContentPane().setLayout(null);
 	    setView();
 	}
 
 	public static MonthlyStatementView getView() {
 		if(msView  == null){
 			msView  = new MonthlyStatementView();
 			msView .setView();
 		}
 		return msView;
 	}
 	public void setView(){
 		
 	    
 	    this.lblUsername = new JLabel("Username:");
 	    this.lblUsername.setBounds(6, 6, 89, 16);
 	    getContentPane().add(this.lblUsername);
 	    
 	    this.lblUsernameField = new JLabel("");
 	    this.lblUsernameField.setBounds(107, 6, 188, 16);
 	    getContentPane().add(this.lblUsernameField);
 	    
 	    this.scrollPane = new JScrollPane();
 	    this.scrollPane.setBounds(6, 131, 387, 441);
 	    getContentPane().add(this.scrollPane);
 	    
 	    // ------------------ market account table--------------------------
 	    Vector<String> col_mAccount = new Vector<String>();
 	    col_mAccount.add("MID");
 	    col_mAccount.add("Date");
 	    col_mAccount.add("Type");
 	    col_mAccount.add("Amount");
 	    col_mAccount.add("Balance");
 	    
 	    this.table = new JTable(row_mAccount, col_mAccount);
 	    this.table.setPreferredSize(new Dimension(800, 1000));
 	    this.scrollPane.setViewportView(this.table);
 	    
 	    this.scrollPane_1 = new JScrollPane();
 	    this.scrollPane_1.setBounds(405, 131, 389, 441);
 	    getContentPane().add(this.scrollPane_1);
 	    
 	    Vector<String> col_sAccount = new Vector<String>();
 	    col_sAccount.add("SID");
 	    col_sAccount.add("Date");
 	    col_sAccount.add("Symbol");
 	    col_sAccount.add("Type");
 	    col_sAccount.add("Shares");
 	    col_sAccount.add("Price");
 	    this.table_1 = new JTable(row_sAccount, col_sAccount);
 	    this.table_1.setPreferredSize(new Dimension(800, 1000));
 	    this.scrollPane_1.setViewportView(this.table_1);
 	    
 	    this.lblEmail = new JLabel("Email:");
 	    this.lblEmail.setBounds(326, 6, 63, 16);
 	    getContentPane().add(this.lblEmail);
 	    
 	    this.lblEmailfield = new JLabel("");
 	    this.lblEmailfield.setBounds(405, 6, 248, 16);
 	    getContentPane().add(this.lblEmailfield);
 	    
 	    this.lblInitialBalance = new JLabel("Initial Blance:  $");
 	    this.lblInitialBalance.setBounds(6, 34, 119, 16);
 	    getContentPane().add(this.lblInitialBalance);
 	    
 	    this.lblInitialbalancefield = new JLabel("");
 	    this.lblInitialbalancefield.setBounds(137, 34, 119, 16);
 	    getContentPane().add(this.lblInitialbalancefield);
 	    
 	    this.lblFinalBalance = new JLabel("Final Balance:  $");
 	    this.lblFinalBalance.setBounds(6, 62, 121, 16);
 	    getContentPane().add(this.lblFinalBalance);
 	    
 	    this.lblFinalbalancefield = new JLabel("");
 	    this.lblFinalbalancefield.setBounds(137, 62, 119, 16);
 	    getContentPane().add(this.lblFinalbalancefield);
 	    
 	    this.lblGainloss = new JLabel("Gain/Loss:  $");
 	    this.lblGainloss.setBounds(326, 34, 103, 16);
 	    getContentPane().add(this.lblGainloss);
 	    
 	    this.lblGainlossfield = new JLabel("");
 	    this.lblGainlossfield.setBounds(441, 34, 120, 16);
 	    getContentPane().add(this.lblGainlossfield);
 	    
 	    this.lblTotalCommission = new JLabel("Total Commission:   $");
 	    this.lblTotalCommission.setBounds(326, 62, 155, 16);
 	    getContentPane().add(this.lblTotalCommission);
 	    
 	    this.lblCommission = new JLabel("");
 	    this.lblCommission.setBounds(493, 62, 122, 16);
 	    getContentPane().add(this.lblCommission);
 	    
 	    this.lblMoneyTransactions = new JLabel("Money Transactions:");
 	    this.lblMoneyTransactions.setBounds(6, 103, 155, 16);
 	    getContentPane().add(this.lblMoneyTransactions);
 	    
 	    this.lblStockTransactions = new JLabel("Stock Transactions:");
 	    this.lblStockTransactions.setBounds(405, 103, 156, 16);
 	    getContentPane().add(this.lblStockTransactions);
 	}
 	@Override
 	public void present(String model) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public Vector<Vector<String>> getRow_mAccount(){
 		return row_mAccount;
 	}
 	
 	public Vector<Vector<String>> getRow_sAccount(){
 		return row_sAccount;
 	}
 	
 	
 
 	public JLabel getLblUsernameField() {
 		return lblUsernameField;
 	}
 
 	public void setLblUsernameField(JLabel lblUsernameField) {
 		this.lblUsernameField = lblUsernameField;
 	}
 
 	public JLabel getLblEmailfield() {
 		return lblEmailfield;
 	}
 
 	public void setLblEmailfield(JLabel lblEmailfield) {
 		this.lblEmailfield = lblEmailfield;
 	}
 
 	public JLabel getLblInitialbalancefield() {
 		return lblInitialbalancefield;
 	}
 
 	public void setLblInitialbalancefield(JLabel lblInitialbalancefield) {
 		this.lblInitialbalancefield = lblInitialbalancefield;
 	}
 
 	public JLabel getLblFinalbalancefield() {
 		return lblFinalbalancefield;
 	}
 
 	public void setLblFinalbalancefield(JLabel lblFinalbalancefield) {
 		this.lblFinalbalancefield = lblFinalbalancefield;
 	}
 
 	public JLabel getLblGainlossfield() {
 		return lblGainlossfield;
 	}
 
 	public void setLblGainlossfield(JLabel lblGainlossfield) {
 		this.lblGainlossfield = lblGainlossfield;
 	}
 
 	public void setRow_mAccount(Vector<Vector<String>> row_mAccount) {
 		this.row_mAccount = row_mAccount;
 	}
 
 	public void setRow_sAccount(Vector<Vector<String>> row_sAccount) {
 		this.row_sAccount = row_sAccount;
 	}
 
 	public void updateView(){
 		this.setView();
 	}

	public JLabel getLblCommission() {
		return lblCommission;
	}

	public void setLblCommission(JLabel lblCommission) {
		this.lblCommission = lblCommission;
	}
 	
 }
