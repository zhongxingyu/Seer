 package presentation;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseListener;
 
 import javax.swing.*;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.LineBorder;
 import javax.swing.table.DefaultTableModel;
 
 import domain.Book;
 
 import presentation.BrowsePanel;
 import presentation.action.BrowseBuyButtonListener;
 import presentation.action.BrowseOkButtonListener;
 import presentation.action.ClickRowListener;
 import presentation.action.DeleteRowBasketListener;
 
 /**
  * Presentation class for the start/browse tab.
  * 
  * @author Kristoffer Karlsson
  * @author Therese Karlander
  *
  */
 
 
 public class BrowsePanel extends JPanel {
 
 	private static final long serialVersionUID = 1L;
 	private JPanel upperPanel, lowerPanel, leftPanel, lowerRightPanel, northPanel;
 	private JTextField theField;
 	private DefaultTableModel upperModel, lowerModel;
 	private JComboBox theComboBox;
 	private JButton OKButton;
 	private JScrollPane firstScrollPane, secondScrollPane;
 	private JLabel totalPriceLabel, priceLabel, headline;
 	private JButton buyButton;
 	private JTable upperTable, lowerTable;
 	private int price;
 	private Object[] bookInfoArray;
 	private Object[] bookBuyArray;
 	
 	public BrowsePanel() {
 	
 	}
 	
 	public void initBrowse() {
 		
 	    setBorder(new EmptyBorder(10, 10, 10, 10));
 		setLayout(new BorderLayout(10, 20));
 		upperPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 		
 		northPanel = new JPanel(new GridLayout(2, 1));
 		
 		lowerPanel = new JPanel(new BorderLayout());
 		lowerRightPanel = new JPanel(new GridLayout(1, 3, 10, 10));
 		leftPanel = new JPanel(new GridLayout(2, 1, 15, 15));
 		
 		theField = new JTextField();
 		upperPanel.add(theField);
 		theField.setColumns(7);
 		
 		String[] stringsForBox = {"ISBN"};
 
 		theComboBox = new JComboBox(stringsForBox);
 		upperPanel.add(theComboBox);
 		
 		OKButton = new JButton("Sk");
 		upperPanel.add(OKButton);
 		
 		headline = new JLabel("<html>" +
 				"Klicka p en bok i versta tabellen fr att lgga den i varukorgen.<br />" + 
 				"Ta bort en bok i varukorgen genom att klicka p den." + "<br /></html>", JLabel.CENTER);
 		
 		priceLabel = new JLabel("Totalt pris :  ");
 		totalPriceLabel = new JLabel();
 		lowerRightPanel.add(priceLabel);
 		lowerRightPanel.add(totalPriceLabel);
 		
 		buyButton = new JButton("Kp");
 		lowerRightPanel.add(buyButton);
 		
 	    /**
 	     * Make the upper table not editable
 	     */
 	    upperModel = new DefaultTableModel(){
 			private static final long serialVersionUID = 1L;
 
 			public boolean isCellEditable(int rowIndex, int mColIndex) {
 	            return false;
 	        }
 	    };
 	    
 	    /**
 	     * Make the lower table not editable
 	     */
 	    lowerModel = new DefaultTableModel(){
 			private static final long serialVersionUID = 1L;
 
 			public boolean isCellEditable(int rowIndex, int mColIndex) {
 	            return false;
 	        }
 	    };
 		
 		upperModel.setColumnIdentifiers(getUpperColumns());
 		lowerModel.setColumnIdentifiers(getLowerColumns());
 		
 		upperTable = new JTable(upperModel);
 		lowerTable = new JTable(lowerModel);
 		
 		
 		firstScrollPane = new JScrollPane(upperTable);
 		firstScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
 		firstScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		firstScrollPane.setViewportBorder(new LineBorder(Color.BLUE));
 		
 		secondScrollPane = new JScrollPane(lowerTable);
 		secondScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
 		secondScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		secondScrollPane.setViewportBorder(new LineBorder(Color.BLUE));
 		
 		leftPanel.add(firstScrollPane);
 		leftPanel.add(secondScrollPane);
 		
 		northPanel.add(headline);
 		northPanel.add(upperPanel);
 		
 		lowerPanel.add(lowerRightPanel, BorderLayout.LINE_END);
 		
 		this.add(northPanel, BorderLayout.NORTH);
 		this.add(lowerPanel, BorderLayout.SOUTH);
 		this.add(leftPanel, BorderLayout.CENTER);
 		
 	    initActions();
 	}
 	
 	/**
 	 * Calls and creates Listeners.
 	 */
 	public void initActions() {
 		this.addOkButtonListener(new BrowseOkButtonListener(this));
 		this.addBuyButtonListener(new BrowseBuyButtonListener(this));
 		this.addClickRowListener(new ClickRowListener(this));
 		this.addClickDeleteRowListener(new DeleteRowBasketListener(this));
 	}
 	/**
 	 * Sets action listeners to the buttons.
 	 * @param al
 	 */
 	public void addOkButtonListener(ActionListener al) {
 		OKButton.addActionListener(al);
 	}
 	
 	public void addBuyButtonListener(ActionListener al) {
 		buyButton.addActionListener(al);
 	}
 	/**
 	 * Sets mouse listeners to the tables.
 	 * @param ml
 	 */
 	public void addClickRowListener(MouseListener ml) {
 		upperTable.addMouseListener(ml);
 	}
 	
 	public void addClickDeleteRowListener(MouseListener ml) {
 		lowerTable.addMouseListener(ml);
 	}
 	
 	/**
 	 * Set what is going to be shown in upper table.
 	 * @param book
 	 */
 	public void setBookInfo(Book book) {
 		this.bookInfoArray = new Object[]{book.getID(), book.getAuthor(), book.getLastName(),
 				book.getName(), book.getPublisher(), book.getFormat(), book.getPrice(), book.getGenre()};
 	}
 	
 	/**
 	 * Set whats is going to be shown in lower table.
 	 * @param book
 	 */
 	public void setBookBuyInfo(Book book) {
 		this.bookBuyArray = new Object[]{book.getID(), book.getAuthor(), book.getLastName(),
 				book.getName(), book.getPrice()};
 	}
 	
 	/**
 	 * Adds new row in upper table.
 	 */
 	public void addRowUpperTable() {
 		upperModel.addRow(getBookInfoArray());
 		upperTable.revalidate();
 	}
 	
 	/**
 	 * Adds new row in lower table.
 	 */
 	public void addRowLowerTable() {
		lowerModel.addRow(getBookBuyArray());
 		lowerTable.revalidate();
 	}
 
 	/**
 	 * Get the text from the search field.
 	 * @return
 	 */
 	public int getTheField() {
 		int isbn = Integer.parseInt(theField.getText());
 		return isbn;
 	}
 	
 	/**
 	 * Get the column names to upper table.
 	 * @return
 	 */
 	public String[] getUpperColumns() {
 		String[] columns = new String[]{"ISBN", "Frnamn","Efternamn","Titel","Utgivare", "Format",
 				"Pris", "Genre"};
 		return columns;
 	}
 	
 	/**
 	 * Get the column names to the lower table
 	 * @return
 	 */
 	public String[] getLowerColumns() {
 		String[] columns = new String[]{"ISBN", "Frnamn","Efternamn","Titel",
 				"Pris"};
 		return columns;
 	}
 	
 	/**
 	 * Gets the object[] with the book info for upper table.
 	 * @return bookInfoArray
 	 */
 	public Object[] getBookInfoArray() {
 		return bookInfoArray;
 	}
 	
 	/**
 	 * Get the object[] with the book info for lower table.
 	 * @return bookBuyArray
 	 */
 	public Object[] getBookBuyArray() {
 		return bookBuyArray;
 	}
 	
 	/**
 	 * Updates the price in label.
 	 * @param s
 	 */
 	public void setTotalPriceText(String s) {
 		totalPriceLabel.setText(s);
 	}
 	
 	/**
 	 * Get number of rows in the lower table.
 	 * @return
 	 */
 	public int getNrOfBooks() {
 		return lowerTable.getRowCount();
 	}
 	
 	/**
 	 * Getters for the tables and models.
 	 * @return
 	 */
 	public DefaultTableModel getLowerModel() {
 		return lowerModel;
 	}
 
 	public JTable getLowerTable() {
 		return lowerTable;
 	}
 	
 	public DefaultTableModel getUpperrModel() {
 		return upperModel;
 	}
 	
 	public JTable getUpperTable() {
 		return upperTable;
 	}
 	
 	/**
 	 * This price is beeing updated when choosing books.
 	 * @param price
 	 */
 	public void setPrice(int price) {
 		this.price = price;
 	}
 	
 	public int getPrice() {
 		return price;
 	}
 }	
