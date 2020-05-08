 package com.cs174.starrus.view;
 
 import java.awt.Dimension;
 import java.awt.Point;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JTextField;
 import javax.swing.JButton;
 import com.cs174.starrus.controller.BuyStockSubmitController;
 
 public class BuyStockView extends JDialog implements IView{
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private JLabel lblTicker;
 	private JTextField txtTicker;
 	private JButton btnTicker;
 	private static BuyStockView bsView = null;
 	private JLabel lblWarning;
 	private JLabel lblQuantity;
 	private JTextField textField;
 	
 	private BuyStockView(){
 		this.setLocation(new Point(350, 200));
 		this.setSize(new Dimension(280, 170));
 		this.setPreferredSize(new Dimension(280, 170));
 	    getContentPane().setLayout(null);
		setView();
 	}
 
 	public static BuyStockView getView() {
 		if(bsView == null){
 			bsView = new BuyStockView();
 			bsView.setView();
 		}
 		return bsView;
 	}
 	public void setView(){
 	    this.lblTicker = new JLabel("Ticker: ");
 	    this.lblTicker.setBounds(21, 12, 92, 16);
 	    this.getContentPane().add(this.lblTicker);
 	    
 	    this.txtTicker = new JTextField();
 	    this.txtTicker.setBounds(118, 6, 140, 28);
 	    this.getContentPane().add(this.txtTicker);
 	    this.txtTicker.setColumns(10);
 	    
 	    this.btnTicker = new JButton("Buy");
 	    this.btnTicker.setBounds(75, 99, 117, 29);
 	    listeners.associate(this.btnTicker, new BuyStockSubmitController());
 	    getContentPane().add(this.btnTicker);
 	    
 	    this.lblWarning = new JLabel("");
 	    this.lblWarning.setBounds(6, 71, 268, 16);
 	    getContentPane().add(this.lblWarning);
 	    
 	    this.lblQuantity = new JLabel("Quantity:");
 	    this.lblQuantity.setBounds(21, 40, 92, 16);
 	    getContentPane().add(this.lblQuantity);
 	    
 	    this.textField = new JTextField();
 	    this.textField.setBounds(118, 34, 140, 28);
 	    getContentPane().add(this.textField);
 	    this.textField.setColumns(10);
 	    //this.setVisible(true);
 	}
 	@Override
 	public void present(String model) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public JTextField getTxtTicker() {
 		return txtTicker;
 	}
 
 	public void setTxtTicker(JTextField txtTicker) {
 		this.txtTicker = txtTicker;
 	}
 
 	public JLabel getLblWarning() {
 		return lblWarning;
 	}
 
 	public void setLblWarning(JLabel lblWarning) {
 		this.lblWarning = lblWarning;
 	}
 	
 }
