 package presentation;
 
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 
 import javax.swing.*;
 import javax.swing.border.EmptyBorder;
 
 import domain.Book;
 
 import presentation.action.AddBookListener;
 import presentation.action.EditBookListener;
 import presentation.action.RemoveBookListener;
 import presentation.action.SearchISBNListener;
 import java.awt.event.ActionListener;
 
 /**
  * 
  * Presentation class for add books tab
  * 
  * @author Kristoffer Karlsson
  * 
  */
 
 public class AddBooksPanel extends JPanel {
 
 	private JPanel leftPanel;
 	private JPanel rightPanel;
 	private JPanel upperRightPanel;
 	private JPanel middleRightPanel;
 	private JPanel lowerRightPanel;
 	private JTextField title;
 	private JTextField authorFirstName;
 	private JTextField authorLastName;
 	private JTextField genre;
 	private JTextField publisher;
 	private JTextField format;
 	private JTextField quantity;
 	private JTextField price;
 	private JTextField browseField;
 	private JLabel titleLabel;
 	private JLabel authorFirstNameLabel;
 	private JLabel authorLastNameLabel;
 	private JLabel genreLabel;
 	private JLabel publisherLabel;
 	private JLabel formatLabel;
 	private JLabel quantityLabel;
 	private JLabel priceLabel;
 	private JLabel filler;
 	private JLabel filler2;
 	private JTextField ISBNfield;
 	private JButton OKButton;
 	private JButton addButton;
 	private JButton editButton;
 	private JButton removeButton;
 	private JButton okBrowse;
 	private Book book;
 
 	public AddBooksPanel() {
 
 

 	}
 
 	public void initActions() {
 		this.addOkButtonListener(new SearchISBNListener(this));
 		this.addAddButtonListener(new AddBookListener(this));
 		this.addEditButtonListener(new EditBookListener(this));
 		this.addRemoveButtonListener(new RemoveBookListener(this));
 	}
 
 	public void addOkButtonListener(ActionListener al) {
 		OKButton.addActionListener(al);
 
 	}
 
 	public void addAddButtonListener(ActionListener al) {
 		addButton.addActionListener(al);
 
 	}
 
 	public void addEditButtonListener(ActionListener al) {
 		editButton.addActionListener(al);
 
 	}
 
 	public void addRemoveButtonListener(ActionListener al) {
 		removeButton.addActionListener(al);
 
 	}
 	
 	public String getTitle() {
 		return title.getText();
 	}
 	
 	public String getAuthorFirstName() {
 		return authorFirstName.getText();
 	}
 	
 	public String getAuthorLastName() {
 		return authorLastName.getText();
 	}
 	
 	public String getGenre() {
 		return genre.getText();
 	}
 	
 	public String getPublisher() {
 		return publisher.getText();
 	}
 	
 	public String getFormat() {
 		return format.getText();
 	}
 
 	public int getQuantity() {
 		int q = Integer.parseInt(quantity.getText());
 		return q;
 	}
 	
 	public int getISBN() {
 		int isbn = Integer.parseInt(ISBNfield.getText());
 		return isbn;
 		
 	}
 	
 	public int getPrice() {
 		int p = Integer.parseInt(price.getText());
 		return p;
 	}
 	
 	// Title textfield
 	public JTextField getTitleTextField(){
 		if (book.getName() != null ){
 			return new JTextField(book.getName(), 20);
 		}else {
 			return new JTextField(20);
 		}
 	}
 	// Author firstname textfield
 	public JTextField getFirstnameTextField(){
 		if (book.getAuthor() != null){
 			return new JTextField(book.getAuthor(), 20);
 		}else {
 			return new JTextField(20);
 		}
 	}
 	
 	// Author lastname textfield
 	public JTextField getLastnameField(){
 		if (book.getLastName() != null ){
 			return new JTextField(book.getLastName(), 20);
 		}else {
 			return new JTextField(20);
 		}
 	}
 	
 	// Genre textfield
 	public JTextField getGenreTextField(){
 		if (book.getGenre() != null ){
 			return new JTextField(book.getGenre(), 20);
 		}else {
 			return new JTextField(20);
 		}
 	}
 	// Publisher textfield
 	public JTextField getPublisherTextField(){
 		if (book.getPublisher() != null){
 			return new JTextField(book.getPublisher(), 20);
 		}else {
 			return new JTextField(20);
 		}
 	}
 	
 	// Format textfield
 	public JTextField getFormatTextField(){
 		if (book.getFormat() != null ){
 			return new JTextField(book.getFormat(), 20);
 		}else {
 			return new JTextField(20);
 		}
 	}
 	
 	// Quantity textfield
 	public JTextField getQuantityTextField(){
 		if (book.getQuantity() != 0){
 			return new JTextField(Integer.toString(book.getQuantity(), 20));
 		}else {
 			return new JTextField(20);
 		}
 	}
 	
 	// Price textfield
 	public JTextField getPriceTextField(){
 		if (book.getPrice() != 0){
 			return new JTextField(Integer.toString(book.getPrice(), 20));
 		}else {
 			return new JTextField(20);
 		}
 	}
 	
 	// ISBN textfield
 	public JTextField getISBNTextField(){
 		if (book.getID() != 0){
 			return new JTextField(Integer.toString(book.getID(), 20));
 		}else {
 			return new JTextField(20);
 		}
 	}
 
 	public void initAddBooks() {
 		
 		setBorder(new EmptyBorder(10, 10, 10, 10));
 		setLayout(new BorderLayout());
 		leftPanel = new JPanel(new GridLayout(8, 2, 5, 40));
 		rightPanel = new JPanel(new BorderLayout());
 		upperRightPanel =  new JPanel(new GridLayout(4, 2, 10, 10));
 		lowerRightPanel = new JPanel(new GridLayout(1, 2, 10, 10));
 
 		authorFirstNameLabel = new JLabel("Frnamn");
 		leftPanel.add(authorFirstNameLabel);
 
 		authorFirstName = getFirstnameTextField();
 		leftPanel.add(authorFirstName);
 		//authorFirstName.setColumns(7);
 		
 		authorLastNameLabel = new JLabel("Efternamn");
 	    leftPanel.add(authorLastNameLabel);
 
 		authorLastName = getLastnameField();
 		leftPanel.add(authorLastName);
 		//authorLastName.setColumns(5);
 		
 		titleLabel = new JLabel("Titel");
 		leftPanel.add(titleLabel);
 		
 		title = getTitleTextField();
 		leftPanel.add(title);
 		//title.setColumns(5);
 		
 		genreLabel = new JLabel("Genre");
 		leftPanel.add(genreLabel);
 
 		genre = getGenreTextField();
 		leftPanel.add(genre);
 		//genre.setColumns(5);
 		
 		publisherLabel = new JLabel("Frlggare");
 		leftPanel.add(publisherLabel);
 		
 		publisher = getPublisherTextField();
 		leftPanel.add(publisher);
 		//publisher.setColumns(5);
 		
 		formatLabel =  new JLabel("Format");
 		leftPanel.add(formatLabel);
 		
 		format = getFormatTextField();
 		leftPanel.add(format);
 		//format.setColumns(5);
 		
 		quantityLabel =  new JLabel("Kvantitet");
 		leftPanel.add(quantityLabel);
 		
 		quantity = getQuantityTextField();
 		leftPanel.add(quantity);
 		//quantity.setColumns(5);
 		
 		priceLabel = new JLabel("Pris");
 		leftPanel.add(priceLabel);
 		
 		price = getPriceTextField();
 		leftPanel.add(price);
 		//price.setColumns(5);
 
 		ISBNfield = getISBNTextField();
 		ISBNfield.setText("ISBN");
 		upperRightPanel.add(ISBNfield);
 		//ISBNfield.setColumns(7);
 
 		OKButton = new JButton("Sk");
 		upperRightPanel.add(OKButton);
 
 		addButton = new JButton("Lgg till");
 		upperRightPanel.add(addButton);
 		
 		filler = new JLabel("");
 		upperRightPanel.add(filler);
 
 		editButton = new JButton("Redigera");
 		upperRightPanel.add(editButton);
 		
 	    filler2 = new JLabel("");
 	    upperRightPanel.add(filler2);
 		
 		removeButton = new JButton("Ta bort");
 		upperRightPanel.add(removeButton);
 		
 		browseField = new JTextField("Sk bild");
 		browseField.setColumns(7);
 		lowerRightPanel.add(browseField);
 		
 		okBrowse = new JButton();
 		okBrowse.setText("OK");
 		lowerRightPanel.add(okBrowse);
 		
 		rightPanel.add(upperRightPanel, BorderLayout.NORTH);
 		rightPanel.add(lowerRightPanel, BorderLayout.SOUTH);
 		
 		add(leftPanel, BorderLayout.LINE_START);
 		add(rightPanel, BorderLayout.LINE_END);
 		
 		initActions();
 		
 	}
 	
 	public void setBook(Book book) {
 		this.book = book;
 		title.setText(book.getName());
 		authorFirstName.setText(book.getAuthor());
 		authorLastName.setText(book.getLastName());
 		genre.setText(book.getGenre());
 		publisher.setText(book.getPublisher());
 		format.setText(book.getFormat());
 		quantity.setText(Integer.toString(book.getQuantity()));
 		price.setText(Integer.toString(book.getPrice()));
 		ISBNfield.setText(Integer.toString(book.getID()));
 	}
 }
