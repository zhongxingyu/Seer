 package view;
 
 import controller.AddBookAction;
 import controller.AddLoanAction;
 import controller.WindowController;
 import domain.Book;
 import domain.Customer;
 import domain.Library;
 import domain.Loan;
 import tablemodel.StartWindowBookTableModel;
 import tablemodel.StartWindowCustomerTableModel;
 import tablemodel.StartWindowLoanTableModel;
 
 import javax.swing.*;
 import javax.swing.border.BevelBorder;
 import javax.swing.border.Border;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.TableRowSorter;
 import java.awt.*;
 import java.awt.event.*;
 import java.util.ArrayList;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.regex.PatternSyntaxException;
 
 public class StartWindow implements Observer{
 
 	private JFrame frame;
 	private JTextField bookSearchTextField;
 	private JTable bookTable;
 	private StartWindowBookTableModel tableModel; 
 	private TableRowSorter<StartWindowBookTableModel> bookSorter; 
 	private TableRowSorter<StartWindowLoanTableModel> loanSorter; 
 	private TableRowSorter<StartWindowCustomerTableModel> customerSorter; 
 	private Library library;
 	private JCheckBox onlyAvailable;
 	private JCheckBox onlyOverdues;
 	private JCheckBox onlyOverdueCustomers;
 	private JTextField loanSearchTextField;
 	private JTextField customerSearchTextField;
 	private JTable loanTable;
 	private JTable customerTable;
 	private String bookSearchBoxText = "Bücher suchen";
 	private String loanSearchBoxText = "Ausleihe suchen";
 	private String customerSearchBoxText = "Kunde suchen";
 	private JButton btnSelektierteAnzeigen;
 	private JButton btnSelektierteAusleiheAnzeigen;
 	private JLabel bookCount;
 	private JLabel copyCount;
 	private JLabel loanCount;
 	private JLabel customerCount;
 	private JLabel activeLoanCount;
 	private JLabel overdueLoanCount;
 	private JLabel overdueCustomerCount;
 	private WindowController windowCtrl;
 	private static Border border;
     private JButton btnSelektierteKundenAnzeigen;
 
 	
 	public StartWindow(Library library, WindowController windowCtrl) {
 		this.windowCtrl = windowCtrl;
 		this.library = library;
 		initialize();
 		this.frame.setVisible(true);
 		library.addObserver(this);
 
 	}
 	
 	
 	public void updateFilters(JCheckBox checkBox, String checkBoxCondition, JTextField textfield, TableRowSorter<? extends AbstractTableModel> sorter) {
 		
 		RowFilter<AbstractTableModel, Object> textFilter = getTextFilter(textfield); 
 		RowFilter<AbstractTableModel, Object> checkBoxFilter = (RowFilter<AbstractTableModel, Object>) getCheckBoxFilter(checkBoxCondition);
 		
 	
 		
 		 
 		 ArrayList<RowFilter<AbstractTableModel, Object>> andFilters = new ArrayList<>();
 		 andFilters.add(textFilter);
 		 andFilters.add(checkBoxFilter);
 		 
 		
 		  if (checkBox.isSelected()) {
 		    if (textfield.getText().length() > 0 && !((textfield.getText().equals(bookSearchBoxText)) || (textfield.getText().equals(loanSearchBoxText)) || (textfield.getText().equals(customerSearchBoxText)))) {
 		       // Both filters active so construct an and filter.
 		       sorter.setRowFilter(RowFilter.andFilter(andFilters));
 		    } else {
 		       // Checkbox selected but text field empty.
 		       sorter.setRowFilter(checkBoxFilter);
 		    }
 		  } else if (textfield.getText().length() > 0 && !((textfield.getText().equals(bookSearchBoxText)) || (textfield.getText().equals(loanSearchBoxText))  || (textfield.getText().equals(customerSearchBoxText))))  {
 		    // Checkbox deselected but text field non-empty.
 		    sorter.setRowFilter(textFilter);
 		  } else {
 		    // Neither filter "active" so remove filter from sorter.
 		    sorter.setRowFilter(null); // Will cause table to re-filter.
 		  }
 		}
 	
 	
 	public static RowFilter<AbstractTableModel, Object> getTextFilter(JTextField txtField) { 
 		if (border == null) border = txtField.getBorder(); 
 		try {
 		    txtField.setBorder(border);   
 			return RowFilter.regexFilter("(?i)" + txtField.getText(), 1 ,2 ,3,4,5);    
 		     
 		} catch (PatternSyntaxException e) {
 			txtField.setBorder(BorderFactory.createMatteBorder(2,2,2,2,Color.red));
 			return null;
 		}
 	}  	
 	
 	private RowFilter<? extends AbstractTableModel, Object> getCheckBoxFilter(String condition) {
 		final String cond = condition;
 		RowFilter<AbstractTableModel, Object> filter = new RowFilter<AbstractTableModel, Object>(){
 				@Override
 				public boolean include(
 					RowFilter.Entry<? extends AbstractTableModel, ? extends Object> entry) {
 					for (int i = entry.getValueCount() - 1; i >= 0; i--) {
 					       if (entry.getStringValue(i).startsWith(cond)) {					      
 					         return false;
 					       }
 					     }					    
 					     return true;
 						}
 		};
 		return filter;
 		
 	}
 
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	@SuppressWarnings("rawtypes")
 	private void initialize() {
 		frame = new JFrame();
 		frame.setBounds(100, 100, 980, 534);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.getContentPane().setLayout(new BorderLayout(0, 0));
 
         AddLoanAction addLoanAction = new AddLoanAction("Neue Ausleihe erfassen", windowCtrl);
         AddBookAction addBookAction = new AddBookAction("Neues Buch hinzufügen", windowCtrl);
 
         JMenuBar bar = new JMenuBar();
         JMenu menu = new JMenu("Hinzufügen");
         menu.setMnemonic('h');
         bar.add(menu);
         AcceleratorListener l = new AcceleratorListener();
         JMenuItem mi;
         mi = menu.add(new JMenuItem(addBookAction));
         mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, Event.CTRL_MASK));
         mi = menu.add(new JMenuItem(addLoanAction));
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, Event.CTRL_MASK));
 
 
 
         frame.setJMenuBar(bar);
 		
 		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
 		frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);
 		
 		JLayeredPane layeredPane = new JLayeredPane();
 		tabbedPane.addTab("Bücher", null, layeredPane, null);
 		layeredPane.setLayout(new BorderLayout(0, 0));
 		
 		JPanel panel = new JPanel();
 		layeredPane.add(panel, BorderLayout.NORTH);
 		panel.setBorder(new TitledBorder(null, "Inventar Statistiken", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		GridBagLayout gbl_panel = new GridBagLayout();
 		gbl_panel.columnWidths = new int[]{118, 106, 8, 130, 8, 0, 0, 0};
 		gbl_panel.rowHeights = new int[]{15, 0};
 		gbl_panel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
 		gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
 		panel.setLayout(gbl_panel);
 		
 		JLabel lblAnzahlBcher = new JLabel("Anzahl Bücher:");
 		GridBagConstraints gbc_lblAnzahlBcher = new GridBagConstraints();
 		gbc_lblAnzahlBcher.anchor = GridBagConstraints.NORTHWEST;
 		gbc_lblAnzahlBcher.insets = new Insets(0, 0, 0, 5);
 		gbc_lblAnzahlBcher.gridx = 0;
 		gbc_lblAnzahlBcher.gridy = 0;
 		panel.add(lblAnzahlBcher, gbc_lblAnzahlBcher);
 		
 		bookCount = new JLabel(library.getBooks().size() + "");
 		GridBagConstraints gbc_bookCount = new GridBagConstraints();
 		gbc_bookCount.anchor = GridBagConstraints.NORTHWEST;
 		gbc_bookCount.insets = new Insets(0, 0, 0, 5);
 		gbc_bookCount.gridx = 1;
 		gbc_bookCount.gridy = 0;
 		panel.add(bookCount, gbc_bookCount);
 		
 		JLabel lblAnzahlExemplare = new JLabel("Anzahl Exemplare:");
 		GridBagConstraints gbc_lblAnzahlExemplare = new GridBagConstraints();
 		gbc_lblAnzahlExemplare.anchor = GridBagConstraints.NORTHWEST;
 		gbc_lblAnzahlExemplare.insets = new Insets(0, 0, 0, 5);
 		gbc_lblAnzahlExemplare.gridx = 3;
 		gbc_lblAnzahlExemplare.gridy = 0;
 		panel.add(lblAnzahlExemplare, gbc_lblAnzahlExemplare);
 		
 		copyCount = new JLabel(library.getCopies().size() + "");
 		GridBagConstraints gbc_copyCount = new GridBagConstraints();
 		gbc_copyCount.insets = new Insets(0, 0, 0, 5);
 		gbc_copyCount.anchor = GridBagConstraints.NORTHWEST;
 		gbc_copyCount.gridx = 5;
 		gbc_copyCount.gridy = 0;
 		panel.add(copyCount, gbc_copyCount);
 		
 		JPanel panel_1 = new JPanel();
 		panel_1.setBorder(new TitledBorder(null, "Buch-Inventar", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		layeredPane.add(panel_1, BorderLayout.CENTER);
 		panel_1.setLayout(new BorderLayout(0, 0));
 		
 		JPanel panel_2 = new JPanel();
 		panel_1.add(panel_2, BorderLayout.NORTH);
 		GridBagLayout gbl_panel_2 = new GridBagLayout();
 		gbl_panel_2.columnWidths = new int[]{0, 248, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
 		gbl_panel_2.rowHeights = new int[]{0, 0};
 		gbl_panel_2.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 		gbl_panel_2.rowWeights = new double[]{0.0, Double.MIN_VALUE};
 		panel_2.setLayout(gbl_panel_2);
 		
 		bookSearchTextField = new JTextField();
 		bookSearchTextField.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				if (bookSearchTextField.getText().equals(bookSearchBoxText)) {
 					bookSearchTextField.setText("");
 				} 				
 			}
 		});
 		bookSearchTextField.setText(bookSearchBoxText);
 		bookSearchTextField.getDocument().addDocumentListener(  
 		  new DocumentListener()  
 		   {  
 		      public void changedUpdate(DocumentEvent e)  
 		      {  
 		    	  updateFilters(onlyAvailable, "0", bookSearchTextField, bookSorter);
 		      }  
 		      public void insertUpdate(DocumentEvent e)  
 		      {  
 		    	  updateFilters(onlyAvailable, "0", bookSearchTextField, bookSorter);
 		      }  
 		      public void removeUpdate(DocumentEvent e)  
 		      {  
 		    	  updateFilters(onlyAvailable, "0", bookSearchTextField, bookSorter);
 		      }  
 		   }  
 		);
 		
 		
 		
 		GridBagConstraints gbc_txtSuc = new GridBagConstraints();
 		gbc_txtSuc.insets = new Insets(0, 0, 0, 5);
 		gbc_txtSuc.fill = GridBagConstraints.HORIZONTAL;
 		gbc_txtSuc.gridx = 1;
 		gbc_txtSuc.gridy = 0;
 		panel_2.add(bookSearchTextField, gbc_txtSuc);
 		bookSearchTextField.setColumns(10);
 		
 		onlyAvailable = new JCheckBox("nur verfügbare Bücher anzeigen");
 		onlyAvailable.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				updateFilters(onlyAvailable, "0", bookSearchTextField, bookSorter);
 				
 			}
 		});
 		GridBagConstraints gbc_chckbxNurVerfgbareBcher = new GridBagConstraints();
 		gbc_chckbxNurVerfgbareBcher.insets = new Insets(0, 0, 0, 5);
 		gbc_chckbxNurVerfgbareBcher.gridx = 8;
 		gbc_chckbxNurVerfgbareBcher.gridy = 0;
 		panel_2.add(onlyAvailable, gbc_chckbxNurVerfgbareBcher);
 		
 		btnSelektierteAnzeigen = new JButton("Selektierte Anzeigen");
 		btnSelektierteAnzeigen.setEnabled(false);
 		btnSelektierteAnzeigen.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				int[] rows = bookTable.getSelectedRows();
 				for (int row: rows){
 					Book book = library.getBooks().get(bookTable.convertRowIndexToModel(row));	
 			        windowCtrl.openDetailBookWindow(book);
 				}
 		
 			}
 		});
 		
 		GridBagConstraints gbc_btnSelektierteAnzeigen = new GridBagConstraints();
 		gbc_btnSelektierteAnzeigen.insets = new Insets(0, 0, 0, 5);
 		gbc_btnSelektierteAnzeigen.gridx = 13;
 		gbc_btnSelektierteAnzeigen.gridy = 0;
 		panel_2.add(btnSelektierteAnzeigen, gbc_btnSelektierteAnzeigen);
 		
 		JButton btnNeuesBuchHinzufgen = new JButton(addBookAction);
 		GridBagConstraints gbc_btnNeuesBuchHinzufgen = new GridBagConstraints();
 		gbc_btnNeuesBuchHinzufgen.gridx = 14;
 		gbc_btnNeuesBuchHinzufgen.gridy = 0;
 		panel_2.add(btnNeuesBuchHinzufgen, gbc_btnNeuesBuchHinzufgen);
 		
 		JPanel panel_3 = new JPanel();
 		panel_3.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
 		panel_1.add(panel_3, BorderLayout.CENTER);
 		panel_3.setLayout(new BorderLayout(0, 0));
 		
 		 
 		tableModel= new StartWindowBookTableModel(library);
 		bookSorter = new TableRowSorter<>(tableModel);
 		bookTable = new JTable(tableModel);
 		JScrollPane scrollPane = new JScrollPane(bookTable);
 		bookTable.setFillsViewportHeight(true);
 		bookTable.setRowSorter(bookSorter);  
 		panel_3.add(scrollPane);
 
 
         bookTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
             public void valueChanged(ListSelectionEvent e) {
                 if (bookTable.getSelectedRows().length >= 1) {
                     btnSelektierteAnzeigen.setEnabled(true);
                 } else {
                     btnSelektierteAnzeigen.setEnabled(false);
                 }
             }
         });
 
 		
         bookTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                JTable target = (JTable)e.getSource();
 
               if (e.getClickCount() == 2) {
                  int row = target.getSelectedRow();
                  if (row >= 0) {
 
                      Book book = library.getBooks().get(bookTable.convertRowIndexToModel(row));
                      windowCtrl.openDetailBookWindow(book);
                  }
               }
            }
         });
 //LOAN
 		JLayeredPane layeredPane_2 = new JLayeredPane();
 		tabbedPane.addTab("Ausleihen", null, layeredPane_2, null);
 		layeredPane_2.setLayout(new BorderLayout(0, 0));
 		
 		JPanel panel_4 = new JPanel();
 		panel_4.setBorder(new TitledBorder(null, "Ausleihe Statistiken", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		layeredPane_2.add(panel_4, BorderLayout.NORTH);
 		GridBagLayout gbl_panel_4 = new GridBagLayout();
 		gbl_panel_4.columnWidths = new int[]{100, 127, 24, 129, 16, 131, 16, 0, 0, 0};
 		gbl_panel_4.rowHeights = new int[]{15, 0};
 		gbl_panel_4.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
 		gbl_panel_4.rowWeights = new double[]{0.0, Double.MIN_VALUE};
 		panel_4.setLayout(gbl_panel_4);
 		
 		JLabel lblAnzahlAusleihen = new JLabel("Anzahl Ausleihen:");
 		GridBagConstraints gbc_lblAnzahlAusleihen = new GridBagConstraints();
 		gbc_lblAnzahlAusleihen.anchor = GridBagConstraints.NORTHWEST;
 		gbc_lblAnzahlAusleihen.insets = new Insets(0, 0, 0, 5);
 		gbc_lblAnzahlAusleihen.gridx = 0;
 		gbc_lblAnzahlAusleihen.gridy = 0;
 		panel_4.add(lblAnzahlAusleihen, gbc_lblAnzahlAusleihen);
 		
 		loanCount = new JLabel(String.valueOf(library.getLoans().size()));
 		GridBagConstraints gbc_label_1 = new GridBagConstraints();
 		gbc_label_1.anchor = GridBagConstraints.WEST;
 		gbc_label_1.insets = new Insets(0, 0, 0, 5);
 		gbc_label_1.gridx = 1;
 		gbc_label_1.gridy = 0;
 		panel_4.add(loanCount, gbc_label_1);
 		
 		JLabel lblAktuellAusgeliehen = new JLabel("Aktuell ausgeliehen:");
 		GridBagConstraints gbc_lblAktuellAusgeliehen = new GridBagConstraints();
 		gbc_lblAktuellAusgeliehen.anchor = GridBagConstraints.NORTHWEST;
 		gbc_lblAktuellAusgeliehen.insets = new Insets(0, 0, 0, 5);
 		gbc_lblAktuellAusgeliehen.gridx = 3;
 		gbc_lblAktuellAusgeliehen.gridy = 0;
 		panel_4.add(lblAktuellAusgeliehen, gbc_lblAktuellAusgeliehen);
 		
 		activeLoanCount = new JLabel(String.valueOf(library.getActiveLoans().size()));
 		GridBagConstraints gbc_label_2 = new GridBagConstraints();
 		gbc_label_2.anchor = GridBagConstraints.NORTHWEST;
 		gbc_label_2.insets = new Insets(0, 0, 0, 5);
 		gbc_label_2.gridx = 4;
 		gbc_label_2.gridy = 0;
 		panel_4.add(activeLoanCount, gbc_label_2);
 		
 		JLabel lblberflligeAusleihen = new JLabel("Überfällige Ausleihen:");
 		GridBagConstraints gbc_lblberflligeAusleihen = new GridBagConstraints();
 		gbc_lblberflligeAusleihen.anchor = GridBagConstraints.NORTHWEST;
 		gbc_lblberflligeAusleihen.insets = new Insets(0, 0, 0, 5);
 		gbc_lblberflligeAusleihen.gridx = 6;
 		gbc_lblberflligeAusleihen.gridy = 0;
 		panel_4.add(lblberflligeAusleihen, gbc_lblberflligeAusleihen);
 		
 		overdueLoanCount = new JLabel(String.valueOf(library.getOverdueLoans().size()));
 		GridBagConstraints gbc_label_3 = new GridBagConstraints();
 		gbc_label_3.insets = new Insets(0, 0, 0, 5);
 		gbc_label_3.anchor = GridBagConstraints.NORTHWEST;
 		gbc_label_3.gridx = 7;
 		gbc_label_3.gridy = 0;
 		panel_4.add(overdueLoanCount, gbc_label_3);
 		
 		JPanel panel_5 = new JPanel();
 		panel_5.setBorder(new TitledBorder(null, "Erfasste Ausleihen", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		layeredPane_2.add(panel_5, BorderLayout.CENTER);
 		panel_5.setLayout(new BorderLayout(0, 0));
 		
 		JPanel panel_6 = new JPanel();
 		panel_5.add(panel_6, BorderLayout.NORTH);
 		GridBagLayout gbl_panel_6 = new GridBagLayout();
 		gbl_panel_6.columnWidths = new int[]{248, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
 		gbl_panel_6.rowHeights = new int[]{0, 0};
 		gbl_panel_6.columnWeights = new double[]{1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 		gbl_panel_6.rowWeights = new double[]{0.0, Double.MIN_VALUE};
 		panel_6.setLayout(gbl_panel_6);
 		
 		loanSearchTextField = new JTextField();
 		loanSearchTextField.setText("Ausleihen suchen");
 		loanSearchTextField.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				if (loanSearchTextField.getText().equals(loanSearchBoxText)) {
 					loanSearchTextField.setText("");
 				} 				
 			}
 		});
 		loanSearchTextField.setText(loanSearchBoxText);
 		loanSearchTextField.getDocument().addDocumentListener(  
 		  new DocumentListener()  
 		   {  
 		      public void changedUpdate(DocumentEvent e)  
 		      {  
 		    	  updateFilters(onlyOverdues, "ok", loanSearchTextField, loanSorter);
 		      }  
 		      public void insertUpdate(DocumentEvent e)  
 		      {  
 		    	  updateFilters(onlyOverdues, "ok", loanSearchTextField, loanSorter);  
 		      }  
 		      public void removeUpdate(DocumentEvent e)  
 		      {  
 		    	  updateFilters(onlyOverdues, "ok", loanSearchTextField, loanSorter); 
 		      }  
 		   }  
 		);
 		
 		
 		GridBagConstraints gbc_txtAusleihenSuchen = new GridBagConstraints();
 		gbc_txtAusleihenSuchen.fill = GridBagConstraints.HORIZONTAL;
 		gbc_txtAusleihenSuchen.insets = new Insets(0, 0, 0, 5);
 		gbc_txtAusleihenSuchen.gridx = 0;
 		gbc_txtAusleihenSuchen.gridy = 0;
 		panel_6.add(loanSearchTextField, gbc_txtAusleihenSuchen);
 		loanSearchTextField.setColumns(10);
 		
 		onlyOverdues = new JCheckBox("Nur überfällige");
 		onlyOverdues.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				updateFilters(onlyOverdues, "ok", loanSearchTextField, loanSorter);
 				
 			}
 		});
 		
 		
 		GridBagConstraints gbc_chckbxNurberfllige = new GridBagConstraints();
 		gbc_chckbxNurberfllige.insets = new Insets(0, 0, 0, 5);
 		gbc_chckbxNurberfllige.gridx = 2;
 		gbc_chckbxNurberfllige.gridy = 0;
 		panel_6.add(onlyOverdues, gbc_chckbxNurberfllige);
 		
 		JButton btnNeueAusleiheErfassen = new JButton(addLoanAction);
 		
 		btnSelektierteAusleiheAnzeigen = new JButton("Selektierte anzeigen");
 		btnSelektierteAusleiheAnzeigen.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				int[] rows = loanTable.getSelectedRows();
 				for (int row: rows){
 					Loan loan = library.getLoans().get(loanTable.convertRowIndexToModel(row));			         
 					windowCtrl.openDetailLoanWindow(loan);
 				}
 		
 			}
 		});
 		
 		btnSelektierteAusleiheAnzeigen.setEnabled(false);
 		GridBagConstraints gbc_btnSelektierteAusleiheAnzeigen = new GridBagConstraints();
 		gbc_btnSelektierteAusleiheAnzeigen.insets = new Insets(0, 0, 0, 5);
 		gbc_btnSelektierteAusleiheAnzeigen.gridx = 10;
 		gbc_btnSelektierteAusleiheAnzeigen.gridy = 0;
 		panel_6.add(btnSelektierteAusleiheAnzeigen, gbc_btnSelektierteAusleiheAnzeigen);
 		GridBagConstraints gbc_btnNeueAusleiheErfassen = new GridBagConstraints();
 		gbc_btnNeueAusleiheErfassen.gridx = 11;
 		gbc_btnNeueAusleiheErfassen.gridy = 0;
 		panel_6.add(btnNeueAusleiheErfassen, gbc_btnNeueAusleiheErfassen);
 		
 		JPanel panel_7 = new JPanel();
 		panel_5.add(panel_7, BorderLayout.CENTER);
 		panel_7.setLayout(new BorderLayout(0, 0));
 		
 		JScrollPane scrollPane_1 = new JScrollPane();
 		panel_7.add(scrollPane_1, BorderLayout.CENTER);
 		
 		StartWindowLoanTableModel loanTableModel = new StartWindowLoanTableModel(library);
 		loanSorter = new TableRowSorter<>(loanTableModel);
 		loanTable = new JTable(loanTableModel);
 
         loanTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
             public void valueChanged(ListSelectionEvent e) {
                 if (loanTable.getSelectedRows().length >= 1) {
                     btnSelektierteAusleiheAnzeigen.setEnabled(true);
                 } else {
                     btnSelektierteAusleiheAnzeigen.setEnabled(false);
                 }
             }
          });
 
         loanTable.addMouseListener(new MouseAdapter() {
             public void mouseClicked(MouseEvent e) {
                 JTable target = (JTable)e.getSource();
 
                 if (e.getClickCount() == 2) {
                     int row = target.getSelectedRow();
                     if (row >= 0) {
 
                         Loan loan = library.getLoans().get(loanTable.convertRowIndexToModel(row));
                         windowCtrl.openDetailLoanWindow(loan);
                     }
                 }
             }
         });
 		scrollPane_1.setViewportView(loanTable);
 		loanTable.setRowSorter(loanSorter);
 
 //KUNDE TAB AB HIER:
 		JLayeredPane layeredPane_1 = new JLayeredPane();
 		tabbedPane.addTab("Kunde", null, layeredPane_1, null);
 		layeredPane_1.setLayout(new BorderLayout(0, 0));
 
 		JPanel panel_8 = new JPanel();
 		panel_8.setBorder(new TitledBorder(null, "Kunden Statistiken", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		layeredPane_1.add(panel_8, BorderLayout.NORTH);
 		GridBagLayout gbl_panel_5 = new GridBagLayout();
 		gbl_panel_5.columnWidths = new int[]{100, 127, 24, 129, 16, 131, 16, 0, 0, 0};
 		gbl_panel_5.rowHeights = new int[]{15, 0};
 		gbl_panel_5.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
 		gbl_panel_5.rowWeights = new double[]{0.0, Double.MIN_VALUE};
 		panel_8.setLayout(gbl_panel_5);
 
 		JLabel lblAnzahlKunden = new JLabel("Anzahl Kunden:");
 		GridBagConstraints gbc_lblAnzahlKunden = new GridBagConstraints();
 		gbc_lblAnzahlKunden.anchor = GridBagConstraints.NORTHWEST;
 		gbc_lblAnzahlKunden.insets = new Insets(0, 0, 0, 5);
 		gbc_lblAnzahlKunden.gridx = 0;
 		gbc_lblAnzahlKunden.gridy = 0;
 		panel_8.add(lblAnzahlKunden, gbc_lblAnzahlKunden);
 
 		customerCount = new JLabel(String.valueOf(library.getCustomers().size()));
 		GridBagConstraints gbc_label_11 = new GridBagConstraints();
 		gbc_label_11.anchor = GridBagConstraints.WEST;
 		gbc_label_11.insets = new Insets(0, 0, 0, 5);
 		gbc_label_11.gridx = 1;
 		gbc_label_11.gridy = 0;
 		panel_8.add(customerCount, gbc_label_11);
 
 		JLabel lblberflligeKunden = new JLabel("Überfällige Kunden:");
 		GridBagConstraints gbc_lblberflligeKunden = new GridBagConstraints();
 		gbc_lblberflligeKunden.anchor = GridBagConstraints.NORTHWEST;
 		gbc_lblberflligeKunden.insets = new Insets(0, 0, 0, 5);
 		gbc_lblberflligeKunden.gridx = 6;
 		gbc_lblberflligeKunden.gridy = 0;
 		panel_8.add(lblberflligeKunden, gbc_lblberflligeKunden);
 
 		overdueCustomerCount = new JLabel(String.valueOf(library.getOverdueCustomers().size()));
 		GridBagConstraints gbc_label_31 = new GridBagConstraints();
 		gbc_label_31.insets = new Insets(0, 0, 0, 5);
 		gbc_label_31.anchor = GridBagConstraints.NORTHWEST;
 		gbc_label_31.gridx = 7;
 		gbc_label_31.gridy = 0;
 		panel_8.add(overdueCustomerCount, gbc_label_31);
 
 		JPanel panel_9 = new JPanel();
 		panel_9.setBorder(new TitledBorder(null, "Erfasste Kunden", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		layeredPane_1.add(panel_9, BorderLayout.CENTER);
 		panel_9.setLayout(new BorderLayout(0, 0));
 
 		JPanel panel_10 = new JPanel();
 		panel_9.add(panel_10, BorderLayout.NORTH);
 		GridBagLayout gbl_panel11 = new GridBagLayout();
 		gbl_panel11.columnWidths = new int[]{248, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
 		gbl_panel11.rowHeights = new int[]{0, 0};
 		gbl_panel11.columnWeights = new double[]{1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 		gbl_panel11.rowWeights = new double[]{0.0, Double.MIN_VALUE};
 		panel_10.setLayout(gbl_panel11);
 
 		customerSearchTextField = new JTextField();
 		customerSearchTextField.setText(customerSearchBoxText);
 		customerSearchTextField.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				if (customerSearchTextField.getText().equals(customerSearchBoxText)) {
 					customerSearchTextField.setText("");
 				}
 			}
 		});
 		customerSearchTextField.setText(customerSearchBoxText);
 		customerSearchTextField.getDocument().addDocumentListener(
 				  new DocumentListener()
 				   {
 				      public void changedUpdate(DocumentEvent e)
 				      {
 				    	  updateFilters(onlyOverdueCustomers, "OK" , customerSearchTextField, customerSorter);
 				      }
 				      public void insertUpdate(DocumentEvent e)
 				      {
 				    	  updateFilters(onlyOverdueCustomers, "OK" , customerSearchTextField, customerSorter);
 				      }
 				      public void removeUpdate(DocumentEvent e)
 				      {
 				    	  updateFilters(onlyOverdueCustomers, "OK" , customerSearchTextField, customerSorter);
 				      }
 				   }
 				);
 
 		GridBagConstraints gbc_txtKundeSuchen = new GridBagConstraints();
 		gbc_txtKundeSuchen.fill = GridBagConstraints.HORIZONTAL;
 		gbc_txtKundeSuchen.insets = new Insets(0, 0, 0, 5);
 		gbc_txtKundeSuchen.gridx = 0;
 		gbc_txtKundeSuchen.gridy = 0;
 		panel_10.add(customerSearchTextField, gbc_txtKundeSuchen);
 		customerSearchTextField.setColumns(10);
 
 		onlyOverdueCustomers = new JCheckBox("Nur überfällige");
 		onlyOverdueCustomers.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				updateFilters(onlyOverdueCustomers, "OK" , customerSearchTextField, customerSorter);
 			}
 		});
 
 		GridBagConstraints gbc_onlyOverdueCustomers = new GridBagConstraints();
 		gbc_onlyOverdueCustomers.insets = new Insets(0, 0, 0, 5);
 		gbc_onlyOverdueCustomers.gridx = 2;
 		gbc_onlyOverdueCustomers.gridy = 0;
 		panel_10.add(onlyOverdueCustomers, gbc_onlyOverdueCustomers);
 
 		JButton btnNeuenKundenErfassen = new JButton("Neuen Kunden erfassen");
 
 
         btnSelektierteKundenAnzeigen = new JButton("Selektierte anzeigen");
 
         btnSelektierteKundenAnzeigen.setEnabled(false);
 		GridBagConstraints gbc_btnSelektierteKundenAnzeigen = new GridBagConstraints();
 		gbc_btnSelektierteKundenAnzeigen.insets = new Insets(0, 0, 0, 5);
 		gbc_btnSelektierteKundenAnzeigen.gridx = 10;
 		gbc_btnSelektierteKundenAnzeigen.gridy = 0;
 		panel_10.add(btnSelektierteKundenAnzeigen, gbc_btnSelektierteKundenAnzeigen);
 
 		GridBagConstraints gbc_btnNeuenKundenErfassen = new GridBagConstraints();
 		gbc_btnNeuenKundenErfassen.gridx = 11;
 		gbc_btnNeuenKundenErfassen.gridy = 0;
 		panel_10.add(btnNeuenKundenErfassen, gbc_btnNeuenKundenErfassen);
 
 		JPanel panel_12 = new JPanel();
 		panel_9.add(panel_12, BorderLayout.CENTER);
 		panel_12.setLayout(new BorderLayout(0, 0));
 
 		JScrollPane scrollPane_2 = new JScrollPane();
 		panel_12.add(scrollPane_2, BorderLayout.CENTER);
 
 		StartWindowCustomerTableModel customerTableModel = new StartWindowCustomerTableModel(library);
 		customerSorter = new TableRowSorter<>(customerTableModel);
 		customerTable = new JTable(customerTableModel);
 
         customerTable.addMouseListener(new MouseAdapter() {
             public void mouseClicked(MouseEvent e) {
                 JTable target = (JTable) e.getSource();
 
                 if (e.getClickCount() == 2) {
                     int row = target.getSelectedRow();
                     if (row >= 0) {
 
                         Customer customer = library.getCustomers().get(customerTable.convertRowIndexToModel(row));
                         windowCtrl.openCustomerWindow(customer);
                     }
                 } else if (e.getClickCount() == 1) {
                     if (target.getSelectedColumnCount() > 0) {
                         btnSelektierteKundenAnzeigen.setEnabled(true);
                     }
                 }
             }
         });
 
 		scrollPane_2.setViewportView(customerTable);
 		customerTable.setRowSorter(customerSorter);
 
 
 	}
 
 	@Override
 	public void update(Observable o, Object arg) {
 		copyCount.setText(library.getCopies().size() + "");
 		bookCount.setText(library.getBooks().size() + "");
 		loanCount.setText(String.valueOf(library.getLoans().size()));
 		activeLoanCount.setText(String.valueOf(library.getActiveLoans().size()));
 		overdueLoanCount.setText(String.valueOf(library.getOverdueLoans().size()));
 
 
 		((AbstractTableModel) loanTable.getModel()).fireTableDataChanged();
 		((AbstractTableModel) bookTable.getModel()).fireTableDataChanged();
 
 	}
 }
