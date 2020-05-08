 package view;
 
 import java.awt.EventQueue;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Observable;
 
 import javax.swing.JFrame;
 
 import domain.Book;
 import domain.Copy;
 import domain.Library;
 
 import java.awt.BorderLayout;
 
 import javax.swing.JPanel;
 
 import java.awt.Dimension;
 import java.awt.GridBagLayout;
 import java.awt.GridBagConstraints;
 import java.awt.Insets;
 import java.awt.ScrollPane;
 
 import javax.swing.border.TitledBorder;
 import javax.swing.border.LineBorder;
 
 import java.awt.Color;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JList;
 import javax.swing.JLabel;
 import javax.swing.JButton;
 import javax.swing.JTextField;
 import javax.swing.JComboBox;
 import javax.swing.DefaultComboBoxModel;
 
 import domain.Shelf;
 
 import java.awt.event.FocusAdapter;
 import java.awt.event.FocusEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 
 import javax.swing.event.ListSelectionListener;
 import javax.swing.event.ListSelectionEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 public class BookDetailView extends Observable{
 
 	private JFrame frame;
 
 	private Library library;
 	private Book book;
 	private JTextField edTitle;
 	private JTextField edAuthor;
 	private JTextField edPublisher;
 	private JComboBox<Shelf> cbShelf;
 	private JList<Copy> listCopies;
 	private JLabel lblCopyCount;
 	private JButton btnDeleteSelected;
 	private JButton btnNewLoan;
 	private DefaultListModel<Copy> listModel = new DefaultListModel<Copy>();  
 	private static HashMap<Book, BookDetailView> openViews = new HashMap<Book, BookDetailView>();
 	
 	// needed for UI-Designer
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					BookDetailView window = new BookDetailView();
 					window.frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 	
 	
 	public static BookDetailView OpenNewBookDetailView(Book book, Library library) {
 		BookDetailView detailView;
 		if(openViews.containsKey(book)) {
 			detailView = openViews.get(book);
 		}
 		else {
 			detailView = new BookDetailView(book, library);
 			openViews.put(book, detailView);
 		}
 		
 		detailView.frame.toFront();
 		detailView.frame.repaint();
 		
 		return detailView;
 	}
 	
 	/**
 	 * Create the application.
 	 */
 	public BookDetailView() {
 		this(new Book(""), new Library());
 	}
 	
 	public BookDetailView(Book book, Library library) {
 		this.book = book;
 		initialize();
 		edTitle.setText(book.getName());
 		edAuthor.setText(book.getAuthor());
 		edPublisher.setText(book.getPublisher());
 		cbShelf.setSelectedItem(book.getShelf());
 		
 		this.library = library;
 		List<Copy> copies = library.getCopiesOfBook(book);
 		for(Copy c : copies) {
 			listModel.addElement(c);
 		}
 		
 		lblCopyCount.setText("Copies: " + library.getCopiesOfBook(book).size());
 		
 		frame.setVisible(true);
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		frame = new JFrame("Book detail view");
		frame.addWindowListener(new WindowAdapter() {
 			@Override
			public void windowDeactivated(WindowEvent e) {
 				if(openViews.containsKey(book))
 					openViews.remove(book);
 			}
 		});
 		frame.setMinimumSize(new Dimension( 450, 400));
 		frame.setBounds(100, 100, 450, 369);
 		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
 		GridBagLayout gridBagLayout = new GridBagLayout();
 		gridBagLayout.columnWidths = new int[]{0, 0};
 		gridBagLayout.rowHeights = new int[]{140, 169, 0};
 		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
 		gridBagLayout.rowWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
 		frame.getContentPane().setLayout(gridBagLayout);
 		
 		JPanel panBookInformation = new JPanel();
 		panBookInformation.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0)), "Book Information", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		GridBagConstraints gbc_panBookInformation = new GridBagConstraints();
 		gbc_panBookInformation.insets = new Insets(0, 0, 5, 0);
 		gbc_panBookInformation.fill = GridBagConstraints.BOTH;
 		gbc_panBookInformation.gridx = 0;
 		gbc_panBookInformation.gridy = 0;
 		frame.getContentPane().add(panBookInformation, gbc_panBookInformation);
 		GridBagLayout gbl_panBookInformation = new GridBagLayout();
 		gbl_panBookInformation.columnWidths = new int[]{70, 0, 0};
 		gbl_panBookInformation.rowHeights = new int[]{0, 0, 0, 0, 0};
 		gbl_panBookInformation.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
 		gbl_panBookInformation.rowWeights = new double[]{1.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
 		panBookInformation.setLayout(gbl_panBookInformation);
 		
 		JLabel lblTitle = new JLabel("Title");
 		GridBagConstraints gbc_lblTitle = new GridBagConstraints();
 		gbc_lblTitle.anchor = GridBagConstraints.WEST;
 		gbc_lblTitle.insets = new Insets(0, 0, 5, 5);
 		gbc_lblTitle.gridx = 0;
 		gbc_lblTitle.gridy = 0;
 		panBookInformation.add(lblTitle, gbc_lblTitle);
 		
 		edTitle = new JTextField();
 		edTitle.addFocusListener(new FocusAdapter() {
 			@Override
 			public void focusLost(FocusEvent arg0) {
 				book.setName(edTitle.getText());
 			}
 		});
 		GridBagConstraints gbc_edTitle = new GridBagConstraints();
 		gbc_edTitle.insets = new Insets(0, 0, 5, 0);
 		gbc_edTitle.fill = GridBagConstraints.HORIZONTAL;
 		gbc_edTitle.gridx = 1;
 		gbc_edTitle.gridy = 0;
 		panBookInformation.add(edTitle, gbc_edTitle);
 		edTitle.setColumns(10);
 		
 		JLabel lblAuthor = new JLabel("Author");
 		GridBagConstraints gbc_lblAuthor = new GridBagConstraints();
 		gbc_lblAuthor.anchor = GridBagConstraints.WEST;
 		gbc_lblAuthor.insets = new Insets(0, 0, 5, 5);
 		gbc_lblAuthor.gridx = 0;
 		gbc_lblAuthor.gridy = 1;
 		panBookInformation.add(lblAuthor, gbc_lblAuthor);
 		
 		edAuthor = new JTextField();
 		edAuthor.addFocusListener(new FocusAdapter() {
 			@Override
 			public void focusLost(FocusEvent e) {
 				book.setAuthor(edAuthor.getText());
 			}
 		});
 		GridBagConstraints gbc_edAuthor = new GridBagConstraints();
 		gbc_edAuthor.insets = new Insets(0, 0, 5, 0);
 		gbc_edAuthor.fill = GridBagConstraints.HORIZONTAL;
 		gbc_edAuthor.gridx = 1;
 		gbc_edAuthor.gridy = 1;
 		panBookInformation.add(edAuthor, gbc_edAuthor);
 		edAuthor.setColumns(10);
 		
 		JLabel lblPublisher = new JLabel("Publisher");
 		GridBagConstraints gbc_lblPublisher = new GridBagConstraints();
 		gbc_lblPublisher.anchor = GridBagConstraints.WEST;
 		gbc_lblPublisher.insets = new Insets(0, 0, 5, 5);
 		gbc_lblPublisher.gridx = 0;
 		gbc_lblPublisher.gridy = 2;
 		panBookInformation.add(lblPublisher, gbc_lblPublisher);
 		
 		edPublisher = new JTextField();
 		edPublisher.addFocusListener(new FocusAdapter() {
 			@Override
 			public void focusLost(FocusEvent e) {
 				book.setPublisher(edPublisher.getText());
 			}
 		});
 		GridBagConstraints gbc_edPublisher = new GridBagConstraints();
 		gbc_edPublisher.insets = new Insets(0, 0, 5, 0);
 		gbc_edPublisher.fill = GridBagConstraints.HORIZONTAL;
 		gbc_edPublisher.gridx = 1;
 		gbc_edPublisher.gridy = 2;
 		panBookInformation.add(edPublisher, gbc_edPublisher);
 		edPublisher.setColumns(10);
 		
 		JLabel lblShelf = new JLabel("Shelf");
 		GridBagConstraints gbc_lblShelf = new GridBagConstraints();
 		gbc_lblShelf.anchor = GridBagConstraints.WEST;
 		gbc_lblShelf.insets = new Insets(0, 0, 0, 5);
 		gbc_lblShelf.gridx = 0;
 		gbc_lblShelf.gridy = 3;
 		panBookInformation.add(lblShelf, gbc_lblShelf);
 		
 		cbShelf = new JComboBox<Shelf>();
 		cbShelf.addFocusListener(new FocusAdapter() {
 			@Override
 			public void focusLost(FocusEvent e) {
 				book.setShelf((Shelf)cbShelf.getSelectedItem());
 			}
 		});
 		cbShelf.setModel(new DefaultComboBoxModel<Shelf>(Shelf.values()));
 		GridBagConstraints gbc_cbShelf = new GridBagConstraints();
 		gbc_cbShelf.fill = GridBagConstraints.HORIZONTAL;
 		gbc_cbShelf.gridx = 1;
 		gbc_cbShelf.gridy = 3;
 		panBookInformation.add(cbShelf, gbc_cbShelf);
 		
 		JPanel panCopies = new JPanel();
 		panCopies.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0)), "Copies", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		GridBagConstraints gbc_panCopies = new GridBagConstraints();
 		gbc_panCopies.fill = GridBagConstraints.BOTH;
 		gbc_panCopies.gridx = 0;
 		gbc_panCopies.gridy = 1;
 		frame.getContentPane().add(panCopies, gbc_panCopies);
 		panCopies.setLayout(new BorderLayout(0, 0));
 		
 		JPanel panel = new JPanel();
 		panCopies.add(panel, BorderLayout.NORTH);
 		GridBagLayout gbl_panel = new GridBagLayout();
 		gbl_panel.columnWidths = new int[]{116, 0, 109, 0, 0, 109, 0, 0, 0};
 		gbl_panel.rowHeights = new int[]{23, 0};
 		gbl_panel.columnWeights = new double[]{0.0, 1.0, 0.0, 1.0, 1.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
 		gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
 		panel.setLayout(gbl_panel);
 		
 		lblCopyCount = new JLabel("Count: 0");
 		GridBagConstraints gbc_lblCopyCount = new GridBagConstraints();
 		gbc_lblCopyCount.anchor = GridBagConstraints.WEST;
 		gbc_lblCopyCount.insets = new Insets(0, 0, 0, 5);
 		gbc_lblCopyCount.gridx = 0;
 		gbc_lblCopyCount.gridy = 0;
 		panel.add(lblCopyCount, gbc_lblCopyCount);
 		
 		btnDeleteSelected = new JButton("Delete Selected");
 		btnDeleteSelected.setEnabled(false);
 		btnDeleteSelected.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				List<Copy> selectedCopies = listCopies.getSelectedValuesList();
 				for(Copy selectedCopy : selectedCopies) {
 					library.deleteCopyOfBook(selectedCopy);
 					listModel.removeElement(selectedCopy);
 				}
 				lblCopyCount.setText("Copies: " + library.getCopiesOfBook(book).size());
 			}
 		});
 		GridBagConstraints gbc_btnDeleteSelected = new GridBagConstraints();
 		gbc_btnDeleteSelected.insets = new Insets(0, 0, 0, 5);
 		gbc_btnDeleteSelected.gridx = 2;
 		gbc_btnDeleteSelected.gridy = 0;
 		panel.add(btnDeleteSelected, gbc_btnDeleteSelected);
 		
 		JButton btnAddCopy = new JButton("Add copy");
 		btnAddCopy.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				Copy newCopy = library.createAndAddCopy(book);
 				lblCopyCount.setText("Copies: " + library.getCopiesOfBook(book).size());
 				listModel.addElement(newCopy);
 			}
 		});
 		GridBagConstraints gbc_btnAddCopy = new GridBagConstraints();
 		gbc_btnAddCopy.insets = new Insets(0, 0, 0, 5);
 		gbc_btnAddCopy.gridx = 5;
 		gbc_btnAddCopy.gridy = 0;
 		panel.add(btnAddCopy, gbc_btnAddCopy);
 		
 		btnNewLoan = new JButton("New Loan");
 		btnNewLoan.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				for(Copy selected : listCopies.getSelectedValuesList()) {
 					LoanDetailView.openNewLoanDetailView(selected, library);
 				}
 			}
 		});
 		GridBagConstraints gbc_btnNewLoan = new GridBagConstraints();
 		gbc_btnNewLoan.gridx = 7;
 		gbc_btnNewLoan.gridy = 0;
 		panel.add(btnNewLoan, gbc_btnNewLoan);
 		
 		listCopies = new JList<Copy>(listModel);
 		listCopies.addListSelectionListener(new ListSelectionListener() {
 			public void valueChanged(ListSelectionEvent arg0) {
 				if(listCopies.getSelectedIndex() == -1) {
 					btnDeleteSelected.setEnabled(false);
 					btnNewLoan.setEnabled(false);
 				}
 				else {
 					btnDeleteSelected.setEnabled(true);
 					btnNewLoan.setEnabled(true);
 				}
 			}
 		});
 		
 		ScrollPane scrollPane = new ScrollPane();
 		scrollPane.add(listCopies);
 		panCopies.add(scrollPane, BorderLayout.CENTER);
 	}
 	
 }
