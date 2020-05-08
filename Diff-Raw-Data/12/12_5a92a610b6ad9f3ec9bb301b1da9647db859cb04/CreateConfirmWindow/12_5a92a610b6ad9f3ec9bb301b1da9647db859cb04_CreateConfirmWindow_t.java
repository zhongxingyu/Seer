 package ee.ut.math.tvt.salessystem.ui.tabs;
 
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import ee.ut.math.tvt.salessystem.domain.data.SoldItem;
 import ee.ut.math.tvt.salessystem.domain.data.StockItem;
 import ee.ut.math.tvt.salessystem.ui.model.SalesSystemModel;
 
 public class CreateConfirmWindow {
 	private JTextField TotalSum, ChangeAmount, paymentAmount;
 	private JButton Accept, cancel;
 	private JFrame frame;
 	private JLabel teave;
 	private double summa;
 	private SalesSystemModel model;
 	
 	public CreateConfirmWindow(double summa) {
 		this.summa=summa;
 		frame = new JFrame("Confirm");
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		JPanel panel = new JPanel(new GridLayout(5, 2, 30, 50));
 		
 		panel.add(new JLabel("Total: "));
 		TotalSum=new JTextField(20);
 		TotalSum.setText(String.valueOf(summa));
 		panel.add(TotalSum);
 		
 		panel.add(new JLabel("Enter Payment: "));
 		paymentAmount = new JTextField(20);
 		panel.add(paymentAmount);
 		
 		panel.add(new JLabel("Change Amount: "));
 		ChangeAmount = new JTextField(20);
		ChangeAmount.setText(String.valueOf(summa));
 		panel.add(ChangeAmount);
 		
 		
 		Accept = new JButton("Accept");
 		Accept.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				AddItemButtonClicked();
 			}
 		});
 
 		panel.add(Accept);
 		
 		cancel = new JButton("Cancel");
 
 		cancel.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				cancelAddButtonClicked();
 			}
 		});
 
 		panel.add(cancel);
 		JLabel tekst = new JLabel();
 		panel.add(tekst);
		// panel.add(teave);
 		frame.add(panel);
 		frame.setSize(400, 400);
 		frame.setVisible(true);
 
 	}
 	
 	protected void cancelAddButtonClicked() {
 		frame.dispose();
 	}
 
 	protected void AddItemButtonClicked() {
 		
 		Calendar a= new GregorianCalendar();
 		Date d=a.getTime();
 		long time=a.getTimeInMillis();
 		
 		model.getHistoryTableModel().addPrice(summa);
 			
 		}
 	
 	public double getTotalSum() {
 		return Double.parseDouble(TotalSum.getText());
 	}
 
 	public void setTotalSum(double summa) {
 		TotalSum.setText(String.valueOf(summa));
 	}
 
 	public JTextField getChangeAmount() {
 		return ChangeAmount;
 	}
 
 	public void setChangeAmount(JTextField changeAmount) {
 		ChangeAmount = changeAmount;
 	}
 
 	public JTextField getPaymentAmount() {
 		return paymentAmount;
 	}
 
 	public void setPaymentAmount(JTextField paymentAmount) {
 		this.paymentAmount = paymentAmount;
 	}
 
 	public JButton getAccept() {
 		return Accept;
 	}
 
 	public void setAccept(JButton accept) {
 		Accept = accept;
 	}
 
 	public JButton getCancel() {
 		return cancel;
 	}
 
 	public void setCancel(JButton cancel) {
 		this.cancel = cancel;
 	}
 
 	protected String ChangeAmount(double sum){
 		String back=null;
 		try {
 			if(Integer.parseInt(paymentAmount.getText())>=0){
 				back= String.valueOf(Integer.parseInt(paymentAmount.getText())-sum);
 			}
 			else{
 				JOptionPane.showMessageDialog(null, "Error");
 			}
 		} catch (Exception e) {
 			JOptionPane.showMessageDialog(null, "Error: "+e);
 		}
 		return back;
 		
 	}
 
 
 
 }
