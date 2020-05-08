 package de.oszimt.gruppe3.bibliotheksverwaltung.persistence_layer;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.jdom2.Document;
 import org.jdom2.Element;
 import org.jdom2.JDOMException;
 import org.jdom2.input.SAXBuilder;
 import org.jdom2.output.Format;
 import org.jdom2.output.XMLOutputter;
 
 import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;
 
 import de.oszimt.gruppe3.bibliotheksverwaltung.model.Book;
 import de.oszimt.gruppe3.bibliotheksverwaltung.model.Customer;
 import de.oszimt.gruppe3.bibliotheksverwaltung.model.Loan;
 
 public class XML implements IDataStorage {
 
 	private Document doc;
 	private File f;
 	
 	public XML() throws JDOMException, IOException{
 		this.f = new File("resources\\data.xml");
 		this.doc = this.openFile(this.f);
 	}
 	
 	private Document openFile(File f) throws JDOMException, IOException{
 		SAXBuilder builder = new SAXBuilder();
 		Document doc = builder.build(f);
 		return doc;
 	}
 	
 
 	private boolean saveToFile(){
 		
 		XMLOutputter xmlop = new XMLOutputter (Format.getPrettyFormat());
         try {
 			xmlop.output(doc, new FileOutputStream(f));
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 		return true;
 		
 	}
 	
 	private void closeFile(BufferedWriter w){
 		try {
 			w.flush();
 			w.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 	}
 	
 	@Override
 	public boolean createBook(Book book) {
 		if(doc == null)
 			return false;
 		Element currRoot = doc.getRootElement();
 		Element parentBooks = currRoot.getChild("books");
 		Element newBook = new Element("book");
 		
 		newBook.addContent(new Element("isbn").setText(book.getIsbn()));
 		newBook.addContent(new Element("title").setText(book.getTitle()));
 		newBook.addContent(new Element("author").setText(book.getAuthor()));
 		newBook.addContent(new Element("price").setText(book.getPrice()+""));
 		
 		parentBooks.addContent(newBook);
 		
 		return this.saveToFile();
 	}
 	
 
 	@Override
 	public boolean createLoan(Loan loan) {
 		if(doc == null)
 			return false;
 		Element currRoot = doc.getRootElement();
 		Element parrentLoan = currRoot.getChild("loanBooks");
 		Element newLoan = new Element("loanBook");
 		
 		int id = 0;
 		for (Loan item : getLoans()) {
 			if(item.getLoanID() > id){
 				id = item.getLoanID();
 			}
 		}
 		id++;
 		newLoan.addContent(new Element("loanID").setText( id+""));
 		newLoan.addContent(new Element("isbn").setText(loan.getBook().getIsbn()));
 		newLoan.addContent(new Element("CustomerID").setText(loan.getCostumer().getCustomerID()+""));
 		newLoan.addContent(new Element("startOfLoan").setText(loan.getStartOfLoan().toString()));
 		newLoan.addContent(new Element("endOfLoan").setText(loan.getEndOfLoan().toString()));
 		
 		parrentLoan.addContent(newLoan);
 		
 		return this.saveToFile();
 	}
 	
 	@Override
 	public boolean createCustomer(Customer customer) {
 		if(doc == null)
 			return false;
 		Element currRoot = doc.getRootElement();
 		Element parrentCusto = currRoot.getChild("customers");
 		Element newCusto = new Element("customer");
 		int id = 0;
 		for (Customer item : getCustomers()) {
 			if(item.getCustomerID() > id)
 				id= item.getCustomerID();
 		}
 		id++;
 		newCusto.addContent(new Element("customerID").setText(id+""));
 		newCusto.addContent(new Element("name").setText(customer.getName()));
 		newCusto.addContent(new Element("surename").setText(customer.getSurname()));
 		newCusto.addContent(new Element("address").setText(customer.getAddress()));
 		
 		parrentCusto.addContent(newCusto);
 		
 		
 		return this.saveToFile();
 	}
 	
 	@Override
 	public boolean updateBook(Book book) {
 		if(doc == null)
 			return false;
 		Element currRoot = doc.getRootElement();
 		Element parentBooks = currRoot.getChild("books");
 		
 		for (Element currBook : parentBooks.getChildren()) {
 			String isbn = currBook.getChild("isbn").getText();
 			if(book.getIsbn().equals(isbn)){
 				currBook.getChild("author").setText(book.getAuthor());
 				currBook.getChild("title").setText(book.getTitle());
 				currBook.getChild("price").setText(book.getPrice()+"");
 				return true;
 			}
 		}		
 		
 		return false;
 	}
 
 	@Override
 	public boolean updateLoan(Loan loan) {
 		Element currRoot = doc.getRootElement();
 		Element loans = currRoot.getChild("loans");
 		List<Element> listLoans = loans.getChildren();
 		
 		for (Element currLoan : listLoans) {
 			int loanID = Integer.parseInt(currLoan.getChild("loanID").getText());
 			if(loan.getLoanID() == loanID){
 				currLoan.getChild("isbn").setText(loan.getBook().getIsbn());
 				currLoan.getChild("CustomerID").setText(loan.getCostumer().getCustomerID()+"");
 				currLoan.getChild("startOfLoan").setText(loan.getStartOfLoan());
 				currLoan.getChild("endOfLoan").setText(loan.getEndOfLoan());
 					return true;						
 			}
 		}
 		return false;
 	}
 	
 
 	@Override
 	public boolean updateCustomer(Customer customer) {
 		if(doc == null)
 			return false;
 		
 		Element currRoot = doc.getRootElement();
 		Element parrentCusto = currRoot.getChild("customers");
 		List<Element> listCusto = parrentCusto.getChildren();
 		
 		for (Element currCusto : listCusto) {
 			int custoID = Integer.parseInt(currCusto.getChild("customerID").getText());
 			if(customer.getCustomerID() == custoID){
 				currCusto.getChild("name").setText(customer.getName());
 				currCusto.getChild("surename").setText(customer.getSurname());
 				currCusto.getChild("address").setText(customer.getAddress());
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public boolean deleteBook(Book book) {
 		if(doc == null)
 			return false;
 		
 		Element currRoot = doc.getRootElement();
 		Element books = currRoot.getChild("books");
 		List<Element> listBooks = books.getChildren();
 		if( book.getLoanList()!= null)
 		for (Loan item : book.getLoanList()) {
 			if(!deleteLoan(item)){
 				return false;
 			}
 		}
 		
 		for (Element currBook : listBooks) {
 			String isbn = currBook.getChild("isbn").getText();
 			if(book.getIsbn().equals(isbn)){
 				books.removeContent(currBook);
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public boolean deleteLoan(Loan loan) {
 		if(doc == null)
 			return false;
 		
 		Element currRoot = doc.getRootElement();
 		Element loans = currRoot.getChild("loans");
 		List<Element> listLoans = loans.getChildren();
 		
 		for (Element currLoan : listLoans) {
 			int loanID = Integer.parseInt(currLoan.getChild("loanID").getText());
 			if(loan.getLoanID() == loanID){
 				loans.removeContent(currLoan);
 				return true;
 			}
 		}
 		
 		
 		return false;
 	}
 	
 	@Override
 	public boolean deleteCustomer(Customer customer) {
 		if(doc == null)
 			return false;
 		
 		Element currRoot = doc.getRootElement();
 		Element parrentCusto = currRoot.getChild("customers");
 		List<Element> listCusto = parrentCusto.getChildren();
 		if( customer.getLoanList()!= null)
 		for (Loan item : customer.getLoanList()) {
 			if(!deleteLoan(item)){
 				return false;
 			}
 		}
 		
 		for (Element currCusto : listCusto) {
 			int custoID = Integer.parseInt(currCusto.getChild("customerID").getText());
 			if(customer.getCustomerID() == custoID){
 				parrentCusto.removeContent(currCusto);
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public Book readBook(String isbn) {
 		if(doc == null)
 			return null;
 		
 		Element currRoot = doc.getRootElement();
 		Element parrentBook = currRoot.getChild("books");
 		List<Element> listBooks = parrentBook.getChildren();
 		
 		for (Element book : listBooks) {
 			String bookIsbn = book.getChild("isbn").getText();
 			if(isbn.equals(bookIsbn)){
 				String title = book.getChild("title").getText();
 				String author = book.getChild("author").getText();
 				double price = Double.parseDouble(book.getChild("price").getText());
 	
 				Book bo = new Book(bookIsbn, title, author, price);
 				return bo;
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public Loan readLoan(String isbn, int costumerID) {
 		if(doc == null)
 			return null;
 		
 		Element currRoot = doc.getRootElement();
 		Element parrentLoan = currRoot.getChild("loanBooks");
 		List<Element> listLoan = parrentLoan.getChildren();
 		
 		for (Element loan : listLoan) {
 			String bookIsbn = loan.getChild("isbn").getText();
 			int custoID = Integer.parseInt(loan.getChild("CustomerID").getText());
 			if(custoID == costumerID && bookIsbn.equals(isbn)){
 				String startOfLoan = null;
 				String endOfLoan = null;
 				int newLoanID = Integer.parseInt(loan.getChild("loanID").getText());
 					startOfLoan = loan.getChild("startOfLoan").getText();
 					endOfLoan = loan.getChild("startOfLoan").getText();
 				return new Loan(newLoanID,this.readBook(bookIsbn), this.readCustomer(costumerID), startOfLoan, endOfLoan);
 			
 			}
 		}	
 		return null;
 	}
 	
 
 	@Override
 	public Loan readLoan(int loanID) {
 		if(doc == null)
 			return null;
 		
 		Element currRoot = doc.getRootElement();
 		Element parrentLoan = currRoot.getChild("loanBooks");
 		List<Element> listLoan = parrentLoan.getChildren();
 		
 		for (Element loan : listLoan) {
 			int newLoanID = Integer.parseInt(loan.getChild("loanID").getText());
 			if(newLoanID == loanID){
 				String startOfLoan = null;
 				String endOfLoan = null; 
 				String bookIsbn = loan.getChild("isbn").getText();
 				int custoID = Integer.parseInt(loan.getChild("CustomerID").getText());
 					startOfLoan = loan.getChild("startOfLoan").getText();
 					endOfLoan = loan.getChild("startOfLoan").getText();
 				
 				return new Loan(newLoanID,this.readBook(bookIsbn), this.readCustomer(custoID), startOfLoan, endOfLoan);
 			}
 			
 		}
 		return null;
 	}
 	
 	@Override
 	public Customer readCustomer(int customerID) {
 		if(doc == null)
 			return null;
 		
 		Element currRoot = doc.getRootElement();
 		Element parrentCusto = currRoot.getChild("customers");
 		List<Element> listCustos = parrentCusto.getChildren();
 		
 		for (Element customer : listCustos) {
 			int newCustomerID = Integer.parseInt(customer.getChild("customerID").getText());
 			if(newCustomerID == customerID){
 				String name = customer.getChild("name").getText();
 				String surename = customer.getChild("surename").getText();
 				String address = customer.getChild("name").getText();
 				Customer cus = new Customer(name, surename, newCustomerID, address);
 				return cus;
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public List<Loan> getLoansByBook(Book book) {
 		List<Loan> loans = new ArrayList<Loan>();
 		for (Loan item : getLoans()) {
 			if(item.getBook().getIsbn().equals(book.getIsbn()))
 				loans.add(readLoan(item.getLoanID()));
 		}
 		return loans;
 	}
 	
 	@Override
 	public List<Loan> getLoansByCustomer(Customer customer) {
 		List<Loan> loans = new ArrayList<Loan>();
 		for (Loan item : getLoans()) {
 			if(item.getCostumer().getCustomerID() == customer.getCustomerID())
 				loans.add(readLoan(item.getLoanID()));
 		}
 		return loans;
 	}
 
 	@Override
 	public int getBookCount() {
 		Element currRoot = doc.getRootElement();
 		List<Element> bookList = currRoot.getChild("books").getChildren();
 		return bookList.size();
 	}
 
 	@Override
 	public int getLoanCount() {
 		Element currRoot = doc.getRootElement();
 		List<Element> loanList = currRoot.getChild("loanBooks").getChildren();
 		return loanList.size();
 	}
 	
 
 	@Override
 	public int getCustomerCount() {
 		Element currRoot = doc.getRootElement();
 		List<Element> custoList = currRoot.getChild("customers").getChildren();
 		return custoList.size();
 	}
 	
 	@Override
 	public void openDataStorage() throws UnsupportedOperationException {
 		throw new UnsupportedOperationException("Not Implemented Method!");
 	}
 
 	@Override
 	public boolean closeDataStorage() {
 		return this.saveToFile();
 	}
 
 	@Override
 	public boolean isAvailable(Book book, String startOfLoan, String endOfLoan) {
 		DateFormat df = new SimpleDateFormat();
 		Date startNew;
 		Date endNew;
 		try {
 			startNew = df.parse(startOfLoan);
 			endNew = df.parse(endOfLoan);
 		} catch (ParseException e) {
 			e.printStackTrace();
 			return false;
 		}
 		
 		for (Loan loan : book.getLoanList()) {
 			Date startOld;
 			Date endOld;
 			try {
 				startOld = df.parse(loan.getStartOfLoan());
 				endOld = df.parse(loan.getEndOfLoan());
 			} catch (ParseException e) {
 				e.printStackTrace();
 				return false;
 			}
 			
 			if(startNew.after(startOld) && startNew.before(endOld))	return false;
 			if(startNew.before(startOld) && endNew.after(endOld) )	return false;
 			if(startNew.after(startOld) && endNew.before(endOld))	return false;
 			if(endNew.after(startOld) && endNew.before(endOld))	return false;
 			
 		}
 		return true;
 	}
 
 	@Override
 	public List<Customer> searchCustomer(String term) {
 		term = "%"+term+"%";
 		List<Customer> match = new ArrayList<Customer>();
 		for (Customer customer : getCustomers()) {
 			if(customer.getAddress().matches(term) || new String(customer.getCustomerID()+"").matches(term) || customer.getName().matches(term) || customer.getSurname().matches(term))
 				match.add(customer);
 		}
 		return match;
 	}
 
 	@Override
 	public List<Book> searchBook(String term) {
 		term = "%"+term+"%";
 		List<Book> match = new ArrayList<Book>();
 		for (Book book : getBooks()) {
 			if(book.getAuthor().matches(term) || book.getIsbn().matches(term) || new String(book.getPrice()+"").matches(term) || book.getTitle().matches(term))
 				match.add(book);
 		}
 		
 		return match;
 	}
 
 	@Override
 	public List<Book> getBooks() {
 		if(doc == null)
 			return null;
 		
 		Element currRoot = doc.getRootElement();
 		Element parrentBook = currRoot.getChild("books");
 		List<Element> listBooks = parrentBook.getChildren();
 		List<Book> books = new ArrayList<Book>();
 		for (Element book : listBooks) {
 			String bookIsbn = book.getChild("isbn").getText();
 				String title = book.getChild("title").getText();
 				String author = book.getChild("author").getText();
 				double price = Double.parseDouble(book.getChild("price").getText());
 	
 				books.add( new Book(bookIsbn, title, author, price));
 		}
 		return books;
 	}
 
 	@Override
 	public List<Customer> getCustomers() {
 		if(doc == null)
 			return null;
 		
 		Element currRoot = doc.getRootElement();
 		Element parrentCusto = currRoot.getChild("customers");
 		List<Element> listCustos = parrentCusto.getChildren();
 		List<Customer> customers = new ArrayList<Customer>();
 		for (Element customer : listCustos) {
 			int newCustomerID = Integer.parseInt(customer.getChild("customerID").getText());
 				String name = customer.getChild("name").getText();
 				String surename = customer.getChild("surename").getText();
				String address = customer.getChild("address").getText();
 				customers.add(new Customer(name, surename, newCustomerID, address));
 			}
 		return customers;
 	}
 
 	@Override
 	public List<Loan> getLoans() {
 		if(doc == null)
 			return null;
 		
 		Element currRoot = doc.getRootElement();
 		Element parrentLoan = currRoot.getChild("loanBooks");
 		List<Element> listLoan = parrentLoan.getChildren();
 		List<Loan> loans = new ArrayList<Loan>();
 		for (Element loan : listLoan) {
 			int newLoanID = Integer.parseInt(loan.getChild("loanID").getText());
 				String startOfLoan = null;
 				String endOfLoan = null; 
 				int custoID = Integer.parseInt(loan.getChild("CustomerID").getText());
 				String bookIsbn = loan.getChild("isbn").getText();
 					startOfLoan = loan.getChild("startOfLoan").getText();
 					endOfLoan = loan.getChild("startOfLoan").getText();
 				
 				loans.add(new Loan(newLoanID,this.readBook(bookIsbn), this.readCustomer(custoID), startOfLoan, endOfLoan));			
 		}
 		return loans;
 	}
 
 
 }
