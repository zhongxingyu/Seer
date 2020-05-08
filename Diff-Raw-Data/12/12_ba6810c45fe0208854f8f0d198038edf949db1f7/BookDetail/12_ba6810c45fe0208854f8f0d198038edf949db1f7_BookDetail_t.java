 package view;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.BoxLayout;
 import javax.swing.DefaultCellEditor;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.KeyStroke;
 import javax.swing.border.TitledBorder;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.TableColumn;
 import javax.swing.table.TableRowSorter;
 
 import tablemodel.TableModelBookDetail;
 import domain.Book;
 import domain.Copy;
 import domain.Copy.Condition;
 import domain.Library;
 import domain.Shelf;
 
 public class BookDetail extends JFrame implements Observer {
 
     private static final long serialVersionUID = -7866393001845760549L;
     
     private Book book;
 	private Library library;
 	private JTextField txtTitle;
 	private JTextField txtAuthor;
 	private JTextField txtPublisher;
 	private JComboBox<Shelf> comboBoxShelf;
 	private JLabel lblCount;
 	private JButton btnRemoveCopy;
 	private JButton btnAddCopy;
 	private JLabel lblError;
 	private JTable table;
 	private TableRowSorter<TableModelBookDetail> sorter;
 
 	/**
 	 * Create the application.
 	 */
 	public BookDetail(Book book, Library library) {
 		this.book = book;
 		this.library = library;
 		initialize();
 		updateFields();
 		library.addObserver(this);
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		setBounds(100, 100, 450, 360);
 		setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
 		setTitle("Buch");
 		setMinimumSize(new Dimension(500, 360));
 		setMaximumSize(new Dimension(749, 659));
 
 		addKeyboardListeners(this);
 
 		JPanel panel_book = new JPanel();
 		panel_book.setBorder(new TitledBorder(null, "Buchinformationen", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		getContentPane().add(panel_book);
 		GridBagLayout gbl_panel_book = new GridBagLayout();
 		gbl_panel_book.columnWidths = new int[] { 0, 0, 0 };
 		gbl_panel_book.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };
 		gbl_panel_book.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
 		gbl_panel_book.rowWeights = new double[] { 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE };
 		panel_book.setLayout(gbl_panel_book);
 
 		JLabel lblTitle = new JLabel("Titel:");
 		GridBagConstraints gbc_lblTitle = new GridBagConstraints();
 		gbc_lblTitle.anchor = GridBagConstraints.WEST;
 		gbc_lblTitle.insets = new Insets(0, 0, 5, 5);
 		gbc_lblTitle.gridx = 0;
 		gbc_lblTitle.gridy = 0;
 		panel_book.add(lblTitle, gbc_lblTitle);
 
 		txtTitle = new JTextField();
 		txtTitle.setEditable(false);
 		GridBagConstraints gbc_txtTitel = new GridBagConstraints();
 		gbc_txtTitel.insets = new Insets(0, 0, 5, 0);
 		gbc_txtTitel.fill = GridBagConstraints.HORIZONTAL;
 		gbc_txtTitel.gridx = 1;
 		gbc_txtTitel.gridy = 0;
 		panel_book.add(txtTitle, gbc_txtTitel);
 		txtTitle.setColumns(10);
 
 		JLabel lblAuthor = new JLabel("Autor:");
 		GridBagConstraints gbc_lblAuthor = new GridBagConstraints();
 		gbc_lblAuthor.anchor = GridBagConstraints.WEST;
 		gbc_lblAuthor.gridheight = 2;
 		gbc_lblAuthor.insets = new Insets(0, 0, 5, 5);
 		gbc_lblAuthor.gridx = 0;
 		gbc_lblAuthor.gridy = 1;
 		panel_book.add(lblAuthor, gbc_lblAuthor);
 
 		txtAuthor = new JTextField();
 		txtAuthor.setEditable(false);
 		GridBagConstraints gbc_txtAutor = new GridBagConstraints();
 		gbc_txtAutor.gridheight = 2;
 		gbc_txtAutor.insets = new Insets(0, 0, 5, 0);
 		gbc_txtAutor.fill = GridBagConstraints.HORIZONTAL;
 		gbc_txtAutor.gridx = 1;
 		gbc_txtAutor.gridy = 1;
 		panel_book.add(txtAuthor, gbc_txtAutor);
 		txtAuthor.setColumns(10);
 
 		JLabel lblPublisher = new JLabel("Verlag:");
 		GridBagConstraints gbc_lblPublisher = new GridBagConstraints();
 		gbc_lblPublisher.anchor = GridBagConstraints.WEST;
 		gbc_lblPublisher.gridheight = 2;
 		gbc_lblPublisher.insets = new Insets(0, 0, 5, 5);
 		gbc_lblPublisher.gridx = 0;
 		gbc_lblPublisher.gridy = 3;
 		panel_book.add(lblPublisher, gbc_lblPublisher);
 
 		txtPublisher = new JTextField();
 		txtPublisher.setEditable(false);
 		GridBagConstraints gbc_txtVerlag = new GridBagConstraints();
 		gbc_txtVerlag.gridheight = 2;
 		gbc_txtVerlag.insets = new Insets(0, 0, 5, 0);
 		gbc_txtVerlag.fill = GridBagConstraints.HORIZONTAL;
 		gbc_txtVerlag.gridx = 1;
 		gbc_txtVerlag.gridy = 3;
 		panel_book.add(txtPublisher, gbc_txtVerlag);
 		txtPublisher.setColumns(10);
 
 		JLabel lblShelf = new JLabel("Regal:");
 		GridBagConstraints gbc_lblShelf = new GridBagConstraints();
 		gbc_lblShelf.anchor = GridBagConstraints.WEST;
 		gbc_lblShelf.gridheight = 2;
 		gbc_lblShelf.insets = new Insets(0, 0, 0, 5);
 		gbc_lblShelf.gridx = 0;
 		gbc_lblShelf.gridy = 5;
 		panel_book.add(lblShelf, gbc_lblShelf);
 
 		comboBoxShelf = new JComboBox<Shelf>();
 		comboBoxShelf.setModel(new DefaultComboBoxModel<Shelf>(Shelf.values()));
 		comboBoxShelf.setEnabled(false);
 		GridBagConstraints gbc_comboBox = new GridBagConstraints();
 		gbc_comboBox.insets = new Insets(0, 0, 5, 0);
 		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
 		gbc_comboBox.gridx = 1;
 		gbc_comboBox.gridy = 5;
 		panel_book.add(comboBoxShelf, gbc_comboBox);
 
 		setInformation();
 
 		JPanel panel_copies = new JPanel();
 		panel_copies.setBorder(new TitledBorder(null, "Exemplare", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		getContentPane().add(panel_copies);
 		GridBagLayout gbl_panel_copies = new GridBagLayout();
 		gbl_panel_copies.columnWidths = new int[] { 0, 0, 0, 0 };
 		gbl_panel_copies.rowHeights = new int[] { 0, 0, 0, 0, 0 };
 		gbl_panel_copies.columnWeights = new double[] { 1.0, 0.0, 0.0, Double.MIN_VALUE };
 		gbl_panel_copies.rowWeights = new double[] { 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
 		panel_copies.setLayout(gbl_panel_copies);
 
 		lblCount = new JLabel();
 		lblCount.setText("Anzahl: " + library.getCopiesOfBook(book).size());
 		GridBagConstraints gbc_lblAnzahl = new GridBagConstraints();
 		gbc_lblAnzahl.anchor = GridBagConstraints.WEST;
 		gbc_lblAnzahl.insets = new Insets(0, 0, 5, 5);
 		gbc_lblAnzahl.gridx = 0;
 		gbc_lblAnzahl.gridy = 0;
 		panel_copies.add(lblCount, gbc_lblAnzahl);
 
 		table = new JTable();
 		table.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseReleased(MouseEvent arg0) {
 				if (table.getSelectedRows().length > 0) {
 					btnRemoveCopy.setEnabled(true);
 				} else {
 					btnRemoveCopy.setEnabled(false);
 				}
 			}
 		});
 		table.getTableHeader().setReorderingAllowed(false);
 		table.setAutoCreateRowSorter(true);
 		String[] header = { "Inventar Nummer", "Verfügbarkeit", "Zustand" };
 		final TableModelBookDetail tableModel = new TableModelBookDetail(library, book, header);
 		table.setModel(tableModel);
 		
 		ImageIcon iconRemoveCopy = new ImageIcon("icons/book_delete.png");
 		btnRemoveCopy = new JButton("Ausgew\u00E4hlte entfernen", iconRemoveCopy);
 		btnRemoveCopy.setToolTipText("Entfernt das in der Tabelle markierte Buch, falls es nicht ausgeliehen ist");
 		btnRemoveCopy.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				int selected[] = table.getSelectedRows();
				// Von hinten nach vorne die Elemente entfernen, ansosnten indexout of bounds exeception!
 				for (int i = selected.length - 1; i >= 0; i--) {
 					Copy copyToDelet = tableModel.getCopyAtRow(table.convertRowIndexToModel(selected[i]));
 					if (!library.isCopyLent(copyToDelet)) {
 						library.removeCopy(copyToDelet);
 					} else {
 						lblError.setForeground(Color.BLACK);
 						lblError.setText("Das Exemplar ist noch ausgeliehen und kann deshalb nicht entfernt werden!");
 					}
 				}
 			}
 		});
 		btnRemoveCopy.setEnabled(false);
 		GridBagConstraints gbc_btnRemoveCopy = new GridBagConstraints();
 		gbc_btnRemoveCopy.insets = new Insets(0, 0, 5, 5);
 		gbc_btnRemoveCopy.gridx = 1;
 		gbc_btnRemoveCopy.gridy = 0;
 		panel_copies.add(btnRemoveCopy, gbc_btnRemoveCopy);
 
 		ImageIcon iconAddCopy = new ImageIcon("icons/book_add.png");
 		btnAddCopy = new JButton(" Exemplar hinzuf\u00FCgen", iconAddCopy);
 		btnAddCopy.setToolTipText("F\u00FCgt der Bibliothek ein neues Exemplar des angezeigten Buches hinzu");
 		btnAddCopy.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				library.createAndAddCopy(book);
 			}
 		});
 		GridBagConstraints gbc_btnAddCopy = new GridBagConstraints();
 		gbc_btnAddCopy.insets = new Insets(0, 0, 5, 0);
 		gbc_btnAddCopy.gridx = 2;
 		gbc_btnAddCopy.gridy = 0;
 		panel_copies.add(btnAddCopy, gbc_btnAddCopy);
 
 		lblError = new JLabel("");
 		GridBagConstraints gbc_lblError = new GridBagConstraints();
 		gbc_lblError.anchor = GridBagConstraints.EAST;
 		gbc_lblError.gridwidth = 3;
 		gbc_lblError.insets = new Insets(0, 0, 5, 0);
 		gbc_lblError.gridx = 0;
 		gbc_lblError.gridy = 1;
 		panel_copies.add(lblError, gbc_lblError);
 
 		JScrollPane scrollPane = new JScrollPane();
 		scrollPane.setViewportView(table);
 		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
 		gbc_scrollPane.gridwidth = 3;
 		gbc_scrollPane.fill = GridBagConstraints.BOTH;
 		gbc_scrollPane.gridx = 0;
 		gbc_scrollPane.gridy = 3;
 		panel_copies.add(scrollPane, gbc_scrollPane);
 
 		setVisible(true);
 		
 		setUpConditionColumn(table, table.getColumnModel().getColumn(2));
 
 		sorter = new TableRowSorter<TableModelBookDetail>((TableModelBookDetail) tableModel);
 		table.setRowSorter(sorter);
 		sorter.setSortsOnUpdates(true);
 		sorter.toggleSortOrder(0);
 	}
 
 	private void setInformation() {
 		txtTitle.setText(book.getName());
 		txtAuthor.setText(book.getAuthor());
 		txtPublisher.setText(book.getPublisher());
 		comboBoxShelf.setSelectedItem(book.getShelf());
 	}
 
 	public void addKeyboardListeners(final JFrame frame) {
 		ActionListener escListener = new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				frame.dispose();
 			}
 		};
		frame.getRootPane().registerKeyboardAction(escListener, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
 
 	}
 
 	public void setUpConditionColumn(JTable table, TableColumn conditionColumn) {
 		// Set up the editor for the sport cells.
 		JComboBox<Condition> comboBox = new JComboBox<Condition>();
 		comboBox.setModel(new DefaultComboBoxModel<Condition>(Condition.values()));
 		conditionColumn.setCellEditor(new DefaultCellEditor(comboBox));
 
 		// Set up tool tips for the sport cells.
 		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
 		renderer.setToolTipText("Klicken um Zustand zu ändern");
 		conditionColumn.setCellRenderer(renderer);
 	}
 
 	@Override
 	public void update(Observable o, Object arg) {
 		updateFields();
 	}
 
 	void updateFields() {
 		lblCount.setText("Anzahl: " + library.getCopiesOfBook(book).size());
 	}
 
 }
