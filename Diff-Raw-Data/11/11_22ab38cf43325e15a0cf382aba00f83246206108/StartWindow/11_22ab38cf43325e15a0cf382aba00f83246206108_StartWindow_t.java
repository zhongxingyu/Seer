 package view;
 
 import javax.swing.JFrame;
 
 import java.awt.Color;
 import java.awt.GridBagLayout;
 import java.awt.BorderLayout;
 
 import javax.swing.BorderFactory;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JLayeredPane;
 import javax.swing.RowFilter;
import javax.swing.UIManager;
 
 import java.awt.FlowLayout;
 
 import javax.swing.JPanel;
 import javax.swing.JLabel;
import javax.swing.border.Border;
 import javax.swing.border.TitledBorder;
 
 import java.awt.GridBagConstraints;
 import java.awt.Insets;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import javax.swing.JButton;
 import javax.swing.border.BevelBorder;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.TableRowSorter;
 import javax.swing.JTextField;
 import javax.swing.JCheckBox;
 import javax.swing.JTable;
 
 import tablemodel.StartWindowBookTableModel;
 import tablemodel.StartWindowLoanTableModel;
 import domain.Library;
 import domain.Loan;
 
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
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
 	private Library library;
 	private JCheckBox onlyAvailable;
 	private JCheckBox onlyOverdues;
 	private JTextField loanSearchTextField;
 	private JTable loanTable;
 	private String bookSearchBoxText = "Bücher suchen";
 	private String loanSearchBoxText = "Ausleihe suchen";
 	private JButton btnSelektierteAnzeigen;
 	private JButton btnSelektierteAusleiheAnzeigen;
	private static Border border;

 	
 	
 	public StartWindow(Library library) {
 		
 		this.library = library;
 		initialize();
 		this.frame.setVisible(true);
 
 	}
 	
 	public void updateFilters(JCheckBox checkBox, String checkBoxCondition, JTextField textfield, TableRowSorter<? extends AbstractTableModel> sorter) {
 		
 		RowFilter<AbstractTableModel, Object> textFilter = getTextFilter(textfield); 
 		RowFilter<AbstractTableModel, Object> checkBoxFilter = (RowFilter<AbstractTableModel, Object>) getCheckBoxFilter(checkBoxCondition);
 	
 		
 		 
 		 ArrayList<RowFilter<AbstractTableModel, Object>> andFilters = new ArrayList<>();
 		 andFilters.add(textFilter);
 		 andFilters.add(checkBoxFilter);
 		 
 		
 		  if (checkBox.isSelected()) {
 		    if (textfield.getText().length() > 0 && !((textfield.getText().equals(bookSearchBoxText)) || (textfield.getText().equals(loanSearchBoxText)) )) {
 		       // Both filters active so construct an and filter.
 		       sorter.setRowFilter(RowFilter.andFilter(andFilters));
 		    } else {
 		       // Checkbox selected but text field empty.
 		       sorter.setRowFilter(checkBoxFilter);
 		    }
 		  } else if (textfield.getText().length() > 0 && !((textfield.getText().equals(bookSearchBoxText)) || (textfield.getText().equals(loanSearchBoxText)) ))  {
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
 	
 	
 	
 	private void openDetailBookWindow(String bookName){
 		
 		domain.Book book = library.findByBookTitle(bookName);
 		if(book != null) {
 			DetailBookWindow detailFrame = new DetailBookWindow(library);
 		    detailFrame.setBook(book);			        	 
 		}else{
 			 System.out.println("Buch nicht gefunden");
 		}
 		
 	}
 	
 	private void openDetailLoanWindow(Loan loan){
 		System.out.println(loan.getCopy().getTitle());
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
 		
 		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
 		frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);
 		
 		JLayeredPane layeredPane = new JLayeredPane();
 		tabbedPane.addTab("Bücher", null, layeredPane, null);
 		layeredPane.setLayout(new BorderLayout(0, 0));
 		
 		JPanel panel = new JPanel();
 		layeredPane.add(panel, BorderLayout.NORTH);
 		panel.setBorder(new TitledBorder(null, "Inventar Statistiken", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
 		
 		JLabel lblAnzahlBcher = new JLabel("Anzahl Bücher:");
 		panel.add(lblAnzahlBcher);
 		
 		JLabel label = new JLabel(library.getBooks().size() + "");
 		panel.add(label);
 		
 		JLabel lblAnzahlExemplare = new JLabel("Anzahl Exemplare:");
 		panel.add(lblAnzahlExemplare);
 		
 		JLabel lblNewLabel = new JLabel(library.getCopies().size() + "");
 		panel.add(lblNewLabel);
 		
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
 					String bookName = (String) bookTable.getValueAt(row, 1);
 					openDetailBookWindow(bookName);
 				}
 		
 			}
 		});
 		
 		GridBagConstraints gbc_btnSelektierteAnzeigen = new GridBagConstraints();
 		gbc_btnSelektierteAnzeigen.insets = new Insets(0, 0, 0, 5);
 		gbc_btnSelektierteAnzeigen.gridx = 13;
 		gbc_btnSelektierteAnzeigen.gridy = 0;
 		panel_2.add(btnSelektierteAnzeigen, gbc_btnSelektierteAnzeigen);
 		
 		JButton btnNeuesBuchHinzufgen = new JButton("Neues Buch hinzufügen");
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
 		
 		 bookTable.addMouseListener(new MouseAdapter() {
 			   public void mouseClicked(MouseEvent e) {
 				   JTable target = (JTable)e.getSource();
 			      if (e.getClickCount() == 2) {			         
 			         int row = target.getSelectedRow();
 			         if (row > 0) {
 				         String bookName= (String) bookTable.getValueAt(row, 1);
 				         openDetailBookWindow(bookName);
 			         }    
 			      } else if(e.getClickCount() == 1) {
 			    	  if (target.getSelectedColumnCount() > 0) {
 			    		  btnSelektierteAnzeigen.setEnabled(true);
 			    	  }
 			      }
 			   }
 			});
 		
 		JLayeredPane layeredPane_2 = new JLayeredPane();
 		tabbedPane.addTab("Ausleihen", null, layeredPane_2, null);
 		layeredPane_2.setLayout(new BorderLayout(0, 0));
 		
 		JPanel panel_4 = new JPanel();
 		panel_4.setBorder(new TitledBorder(null, "Ausleihe Statistiken", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		layeredPane_2.add(panel_4, BorderLayout.NORTH);
 		panel_4.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
 		
 		JLabel lblAnzahlAusleihen = new JLabel("Anzahl Ausleihen:");
 		panel_4.add(lblAnzahlAusleihen);
 		
 		JLabel label_1 = new JLabel("842");
 		panel_4.add(label_1);
 		
 		JLabel lblAktuellAusgeliehen = new JLabel("Aktuell ausgeliehen:");
 		panel_4.add(lblAktuellAusgeliehen);
 		
 		JLabel label_2 = new JLabel("86");
 		panel_4.add(label_2);
 		
 		JLabel lblberflligeAusleihen = new JLabel("Überfällige Ausleihen:");
 		panel_4.add(lblberflligeAusleihen);
 		
 		JLabel label_3 = new JLabel("16");
 		panel_4.add(label_3);
 		
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
 		
 		JButton btnNeueAusleiheErfassen = new JButton("Neue Ausleihe erfassen");
 		btnNeueAusleiheErfassen.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				AddLoanWindow newWindow = new AddLoanWindow(library);
 				newWindow.setVisible(true);
 			}
 		});
 		
 		btnSelektierteAusleiheAnzeigen = new JButton("Selektierte anzeigen");
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
 		scrollPane_1.setViewportView(loanTable);
 		loanTable.setRowSorter(loanSorter);
 		
 		loanTable.addMouseListener(new MouseAdapter() {
 			   public void mouseClicked(MouseEvent e) {
 				   JTable target = (JTable)e.getSource();
 			      if (e.getClickCount() == 2) {			         
 			         int row = target.getSelectedRow();
 			         if (row > 0) {
 				         Loan loan = library.getLoans().get(bookTable.convertRowIndexToModel(row));			         
 				         openDetailLoanWindow(loan);
 			         }    
 			      } else if(e.getClickCount() == 1) {
 			    	  if (target.getSelectedColumnCount() > 0) {
 			    		  btnSelektierteAusleiheAnzeigen.setEnabled(true);
 			    	  }
 			      }
 			   }
 			});
 		
 	}
 
 	@Override
 	public void update(Observable o, Object arg) {
 		
 		// TODO Auto-generated method stub
 		
 	}
 }
