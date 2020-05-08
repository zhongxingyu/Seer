 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.text.NumberFormat;
 import java.text.ParseException;
 import java.util.Locale;
 
 import javax.swing.AbstractAction;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JTextField;
 
 /**
  * This is the graphical interface for class
  * <code>FinancialHistory</code>.
  * 
 * @author  Erich Ehses
  *
  */
 public class Application extends JFrame {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private FinancialHistory history;
 	private NumberFormat nf;
 	
 	private Application() {
 		super("A financial history");
 		history = new FinancialHistory();
 		Locale.setDefault(Locale.GERMANY);
 		nf = NumberFormat.getInstance();
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 		Container frame = this.getContentPane();
 		frame.setLayout(new BorderLayout());
         initFields();
         frame.add(income(), BorderLayout.WEST);
         frame.add(expenditures(), BorderLayout.EAST);
         frame.add(cash(), BorderLayout.NORTH);
         frame.add(recallTotals(), BorderLayout.SOUTH);
         update();
         pack();
         setResizable(false);
         setVisible(true);
 	}
 	
 	private JTextField source;
 	private JTextField sourceAmount;
 
 	private LabelledItemPanel income() {
 		LabelledItemPanel p = new LabelledItemPanel();
 		p.addItem("Income:", null);
 		p.addItem("Source", source);
 		p.addItem("Amount", sourceAmount);
 		p.addItem("", new JButton(new AbstractAction("accept") {
 			/**
 			 * 
 			 */
 			private static final long serialVersionUID = 1L;
 
 			public void actionPerformed(ActionEvent e) {
 				try {
 					history.receiveFrom(read(source), readValue(sourceAmount));
 					updateTotal();
 				} catch (Exception x) {
 					illegalInput(x.getMessage());
 				}
 			}
 		}));
 		return p;
 	}
 	
 	private void illegalInput(String text) {
 		new ErrorDialog("Illegal Input: " + text);
 	}
 	
 	private JTextField reason;
 	private JTextField reasonAmount;
 
 	private LabelledItemPanel expenditures() {
 		LabelledItemPanel p = new LabelledItemPanel();
 		p.addItem("Expenditure:", null);
 		p.addItem("Reason", reason);
 		p.addItem("Amount", reasonAmount);
 		p.addItem("", new JButton(new AbstractAction("accept") {
 			/**
 			 * 
 			 */
 			private static final long serialVersionUID = 1L;
 
 			public void actionPerformed(ActionEvent e) {
 				try {
 					history.spendFor(read(reason), readValue(reasonAmount));
 					updateTotal();
 				} catch (Exception x) {
 					illegalInput(x.getMessage());
 				}
 			}
 		}));
 		return p;
 	}
 	
 	private JTextField cashOnHand ;
 	
 	private LabelledItemPanel cash() {
 		LabelledItemPanel p = new LabelledItemPanel();
 		p.addItem("Money on hand: ", cashOnHand);
 		return p;
 	}
 	
 	private Component recallTotals() {
 		JButton b = new JButton("recall totals");
 		//b.setEnabled(false);
 		b.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				update();
 			}
 		});
 		return b;
 	}
 	
 	private void update() {
 		String src = read(source);
 		String rsn = read(reason);
 		updateTotal();
 		sourceAmount.setText(nf.format(history.totalReceivedFrom(src)));
 		reasonAmount.setText(nf.format(history.totalSpentFor(rsn)));
 	}
 
 	private void updateTotal() {
 		cashOnHand.setText(nf.format(history.cashOnHand()));
 	}
 	
 	private void initFields() {
 		source = new JTextField();
 		sourceAmount = new JTextField();
 		sourceAmount.setHorizontalAlignment(JTextField.RIGHT);
 		reason = new JTextField();
 		reasonAmount = new JTextField();
 		reasonAmount.setHorizontalAlignment(JTextField.RIGHT);
 		cashOnHand = new JTextField();
 		cashOnHand.setHorizontalAlignment(JTextField.RIGHT);
 		cashOnHand.setText("0");
 		cashOnHand.setEditable(false);
 	}
 
     private String read(JTextField from) {
 		return from.getText().trim();
 	}
 
 	private double readValue(JTextField source) throws ParseException {
 		return nf.parse(read(source)).doubleValue();
 	}
 
 	public static void main(String[] args) {
     	new Application();
     }
 }
