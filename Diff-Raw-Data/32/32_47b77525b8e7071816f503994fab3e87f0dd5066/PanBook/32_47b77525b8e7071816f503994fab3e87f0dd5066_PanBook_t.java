 package view;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 import javax.swing.RowFilter;
 import javax.swing.RowSorter;
 import javax.swing.SwingConstants;
 import javax.swing.border.LineBorder;
 import javax.swing.border.TitledBorder;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.TableModel;
 import javax.swing.table.TableRowSorter;
 
 import domain.Book;
 import domain.Copy;
 import domain.Library;
 import domain.Loan;
 
 import javax.swing.JTextField;
 
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import javax.swing.JCheckBox;
 import java.awt.event.ItemListener;
 import java.awt.event.ItemEvent;
 
 public class PanBook extends JPanel implements Observer{
 
 	private JTable tableBookInventory;
 	private JButton btnAddNewBook;
 	private JButton btnShowSelection;
 	private Library lib;
 	private JTextField edSearchField;
 	private JCheckBox cbShowAvailable;
 	private JLabel lblCountBooks;
 	private JLabel lblCountCopies;
 	private TableRowSorter<BookTableModel> sorter;
 
 	/**
 	 * Create the panel.
 	 */
 	public PanBook(Library library) {
 		this.lib = library;
 		lib.addObserver(this);
 		setLayout(new BorderLayout(0, 0));
 		JPanel panInventoryStatistics = new JPanel();
 		panInventoryStatistics.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0)), "Inventory statistics", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		add(panInventoryStatistics, BorderLayout.NORTH);
 		GridBagLayout gbl_panInventoryStatistics = new GridBagLayout();
 		gbl_panInventoryStatistics.columnWidths = new int[]{116, 0, 109, 0, 94, 0};
 		gbl_panInventoryStatistics.rowHeights = new int[]{23, 0};
 		gbl_panInventoryStatistics.columnWeights = new double[]{0.0, 1.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
 		gbl_panInventoryStatistics.rowWeights = new double[]{1.0, Double.MIN_VALUE};
 		panInventoryStatistics.setLayout(gbl_panInventoryStatistics);
 		
 		lblCountBooks = new JLabel("Books count: ");
 		GridBagConstraints gbc_lblCountBooks = new GridBagConstraints();
 		gbc_lblCountBooks.anchor = GridBagConstraints.WEST;
 		gbc_lblCountBooks.insets = new Insets(0, 0, 0, 5);
 		gbc_lblCountBooks.gridx = 0;
 		gbc_lblCountBooks.gridy = 0;
 		panInventoryStatistics.add(lblCountBooks, gbc_lblCountBooks);
 		
 		lblCountCopies = new JLabel("Copies count: ");
 		GridBagConstraints gbc_lblCountCopies = new GridBagConstraints();
 		gbc_lblCountCopies.anchor = GridBagConstraints.WEST;
 		gbc_lblCountCopies.insets = new Insets(0, 0, 0, 5);
 		gbc_lblCountCopies.gridx = 2;
 		gbc_lblCountCopies.gridy = 0;
 		panInventoryStatistics.add(lblCountCopies, gbc_lblCountCopies);
 		
 		JPanel panBookInventory = new JPanel();
 		panBookInventory.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0)), "Book-Inventory", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		add(panBookInventory, BorderLayout.CENTER);
 		panBookInventory.setLayout(new BorderLayout(0, 0));
 		
 		JPanel panBookInventoryMenu = new JPanel();
 		panBookInventory.add(panBookInventoryMenu, BorderLayout.NORTH);
 		GridBagLayout gbl_panBookInventoryMenu = new GridBagLayout();
 		gbl_panBookInventoryMenu.columnWidths = new int[]{116, 0, 0, 109, 0, 94, 0};
 		gbl_panBookInventoryMenu.rowHeights = new int[]{23, 0};
 		gbl_panBookInventoryMenu.columnWeights = new double[]{1.0, 0.0, 1.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
 		gbl_panBookInventoryMenu.rowWeights = new double[]{0.0, Double.MIN_VALUE};
 		panBookInventoryMenu.setLayout(gbl_panBookInventoryMenu);
 		
 		btnShowSelection = new JButton("Show selection");
 		btnShowSelection.setEnabled(false);
 		btnShowSelection.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				int[] selectedRows = tableBookInventory.getSelectedRows();
 				for(int row : selectedRows) {
 					BookDetailView.OpenNewBookDetailView(lib.findByBookTitle((String)tableBookInventory.getValueAt(row, 1)), lib);
 				}
 			}
 		});
 		
 		edSearchField = new JTextField();
 		edSearchField.addKeyListener(new KeyAdapter() {
 			@Override
 			public void keyReleased(KeyEvent e) {
 				updateTableFilter();
 			}
 		});
 		
 		GridBagConstraints gbc_edSearchField = new GridBagConstraints();
 		gbc_edSearchField.fill = GridBagConstraints.HORIZONTAL;
 		gbc_edSearchField.insets = new Insets(0, 0, 0, 5);
 		gbc_edSearchField.gridx = 0;
 		gbc_edSearchField.gridy = 0;
 		panBookInventoryMenu.add(edSearchField, gbc_edSearchField);
 		edSearchField.setColumns(10);
 		
 		cbShowAvailable = new JCheckBox("Show Available");
 		cbShowAvailable.addItemListener(new ItemListener() {
 			public void itemStateChanged(ItemEvent arg0) {
 				updateTableFilter();
 			}
 		});
 		GridBagConstraints gbc_cbShowAvailable = new GridBagConstraints();
 		gbc_cbShowAvailable.insets = new Insets(0, 0, 0, 5);
 		gbc_cbShowAvailable.gridx = 1;
 		gbc_cbShowAvailable.gridy = 0;
 		panBookInventoryMenu.add(cbShowAvailable, gbc_cbShowAvailable);
 		GridBagConstraints gbc_btnShowSelection = new GridBagConstraints();
 		gbc_btnShowSelection.anchor = GridBagConstraints.NORTHEAST;
 		gbc_btnShowSelection.insets = new Insets(0, 0, 0, 5);
 		gbc_btnShowSelection.gridx = 3;
 		gbc_btnShowSelection.gridy = 0;
 		panBookInventoryMenu.add(btnShowSelection, gbc_btnShowSelection);
 		
 		tableBookInventory = new JTable();
 		tableBookInventory.setModel(new BookTableModel(lib));
 		
 		
 		sorter = new TableRowSorter<BookTableModel>();
 		sorter.setModel((BookTableModel)tableBookInventory.getModel());
 		tableBookInventory.setRowSorter(sorter);
 		
 		
 		tableBookInventory.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				if(tableBookInventory.getSelectedColumn() == -1) {
 					btnShowSelection.setEnabled(false);
 				}
 				else {
 					btnShowSelection.setEnabled(true);
 				}
 			}
 		});
 		
 		JScrollPane panBookInventoryTable = new JScrollPane(tableBookInventory);
 		
 		panBookInventory.add(panBookInventoryTable, BorderLayout.CENTER);
 		
 		btnAddNewBook = new JButton("Add new book");
 		btnAddNewBook.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				Book b = new Book("");
 				BookDetailView detailView = BookDetailView.OpenNewBookDetailView(b, lib);
 			}
 		});
 		
 		btnAddNewBook.setHorizontalAlignment(SwingConstants.RIGHT);
 		GridBagConstraints gbc_btnAddNewBook = new GridBagConstraints();
 		gbc_btnAddNewBook.anchor = GridBagConstraints.NORTHEAST;
 		gbc_btnAddNewBook.gridx = 5;
 		gbc_btnAddNewBook.gridy = 0;
 		panBookInventoryMenu.add(btnAddNewBook, gbc_btnAddNewBook);
		
		update(null, null);
 	}
 
 	private void updateTableFilter() {
 		try {
 		
 			List<RowFilter<Object, Object>> filters = new ArrayList<RowFilter<Object, Object>>();
 			
 			filters.add( RowFilter.regexFilter("(?i)" + edSearchField.getText()) ); 
 			
 			if(cbShowAvailable.isSelected()) {
 				filters.add( RowFilter.regexFilter("^(?i)Available$"));
 			}
 			
 			RowFilter<Object, Object> rf = RowFilter.andFilter(filters);
 			sorter.setRowFilter(rf);
 
 		} catch (java.util.regex.PatternSyntaxException e) {
 		        return;
 		}	
 	}
 	
 	public void update(Observable sender, Object arg) {
 		lblCountBooks.setText("Books count: " + lib.getBooks().size());
 		lblCountCopies.setText("Copies count: " + lib.getCopies().size());
 	}
 }
