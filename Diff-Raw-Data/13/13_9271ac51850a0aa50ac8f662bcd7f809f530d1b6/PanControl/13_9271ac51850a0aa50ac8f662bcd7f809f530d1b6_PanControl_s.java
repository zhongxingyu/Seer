 package view;
 
 import javax.swing.JFrame;
 
 public class PanControl extends JPanel {
 
 	private Library lib;
 	private Book book;
 	private Loan loan;
 	private JFrame masterFrame;
 	
 	public PanControl(Library lib, Book book, Loan loan, JFrame masterFrame) {
 		this();
 		this.lib = lib;
 		this.book = book;
 		this.loan = loan;
 		this.masterFrame = masterFrame;
 	}
 	
 	/**
 	 * Create the panel.
 	 */
 	public PanControl() {
 		GridBagLayout gridBagLayout = new GridBagLayout();
 		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0};
 		gridBagLayout.rowHeights = new int[]{0, 0};
 		gridBagLayout.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
 		gridBagLayout.rowWeights = new double[]{0.0, Double.MIN_VALUE};
 		setLayout(gridBagLayout);
 		
 		JButton btnSave = new JButton("Save");
 		btnSave.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				if(book != null) {
 					lib.replaceOrAddBook(book);
 				}
 				if(loan != null) {
 					lib.replaceOrAddLoan(loan);
 				}
 				
 				masterFrame.setVisible(false);
 			}
 		});
 		GridBagConstraints gbc_btnSave = new GridBagConstraints();
 		gbc_btnSave.insets = new Insets(0, 0, 0, 5);
 		gbc_btnSave.gridx = 0;
 		gbc_btnSave.gridy = 0;
 		add(btnSave, gbc_btnSave);
 		
 		
 		
 		
 		JButton btnCancel = new JButton("Cancel");
 		btnCancel.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				// do nothing
 				masterFrame.setVisible(false);
 			}
 		});
 		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
 		gbc_btnCancel.gridx = 2;
 		gbc_btnCancel.gridy = 0;
 		add(btnCancel, gbc_btnCancel);
 	}
 
 }
