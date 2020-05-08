 package domain;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 public class Library extends Observable implements Observer{
 
 	private List<Copy> copies;
 	private List<Customer> customers;
 	private List<Loan> loans;
 	private List<Book> books;
 
 	public Library() {
 		copies = new ArrayList<Copy>();
 		customers = new ArrayList<Customer>();
 		loans = new ArrayList<Loan>();
 		books = new ArrayList<Book>();
 	}
 
 	public Loan createAndAddLoan(Customer customer, Copy copy) {
 		if (!isCopyLent(copy)) {
 			Loan l = new Loan(customer, copy);
 			//l.addObserver(this);
 			loans.add(l);
 			notifyAllObservers();
 			return l;
 		} else {
 			return null;
 		}
 	}
 
 	public Customer createAndAddCustomer(String name, String surname) {
 		Customer c = new Customer(name, surname);
 		customers.add(c);
 		return c;
 	}
 
 	public Book createAndAddBook(String name) {
 		Book b = new Book(name);
 		b.addObserver(this);
 		books.add(b);
 		notifyAllObservers();
 		return b;
 	}
 
 	public void replaceOrAddBook(Book book) {
 		Book b = findByBookTitle(book.getName());
 		if(b != null)
 			books.remove(b);
 		books.add(book);
 		book.addObserver(this);
 		notifyAllObservers();
 	}
 	
 	public void replaceOrAddLoan(Loan loan) {
 		loans.remove(loan);
 		loans.add(loan);
 		//loan.addObserver(this);
 		notifyAllObservers();
 	}
 	
 	public Copy createAndAddCopy(Book title) {
 		Copy c = new Copy(title);
 		copies.add(c);
 		notifyAllObservers();
 		return c;
 	}
 
 	public Book findByBookTitle(String title) {
 		for (Book b : books) {
 			if (b.getName().equals(title)) {
 				return b;
 			}
 		}
 		return null;
 	}
 
 	
 	public Loan findLoanByBookTitleAndCustomerId(String title, String customerId) {
 		for(Loan l : loans) {
 			if(l.getCopy().getTitle().getName().equals(title) && l.getCustomer().getId().equals(customerId))
 				return l;
 		}
 		return null;
 	}
 	public boolean isCopyLent(Copy copy) {
 		for (Loan l : loans) {
 			if (l.getCopy().equals(copy) && l.isLent()) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public List<Copy> getCopiesOfBook(Book book) {
 		List<Copy> res = new ArrayList<Copy>();
 		for (Copy c : copies) {
 			if (c.getTitle().equals(book)) {
 				res.add(c);
 			}
 		}
 
 		return res;
 	}
 	
 	public void deleteCopyOfBook(Copy copy) {
 		copies.remove(copy);
 	}
 
 	public List<Loan> getLentCopiesOfBook(Book book) {
 		List<Loan> lentCopies = new ArrayList<Loan>();
 		for (Loan l : loans) {
 			if (l.getCopy().getTitle().equals(book) && l.isLent()) {
 				lentCopies.add(l);
 			}
 		}
 		return lentCopies;
 	}
 
 	public List<Loan> getCustomerLoans(Customer customer) {
 		List<Loan> lentCopies = new ArrayList<Loan>();
 		for (Loan l : loans) {
 			if (l.getCustomer().equals(customer)) {
 				lentCopies.add(l);
 			}
 		}
 		return lentCopies;
 	}
 	
 	public List<Loan> getOverdueLoans() {
 		List<Loan> overdueLoans = new ArrayList<Loan>();
 		for ( Loan l : getLoans() ) {
 			if (l.isOverdue())
 				overdueLoans.add(l);
 		}
 		return overdueLoans;
 	}
 	
 	public List<Copy> getAvailableCopies(){
 		return getCopies(false);
 	}
 	
 	public List<Copy> getLentOutBooks(){
 		return getCopies(true);
 	}
 
 	private List<Copy> getCopies(boolean isLent) {
 		List<Copy> retCopies = new ArrayList<Copy>();
 		for (Copy c : copies) {
 			if (isLent == isCopyLent(c)) {
 				retCopies.add(c);
 			}
 		}
 		return retCopies;
 	}
 
 	public List<Copy> getCopies() {
 		return copies;
 	}
 
 	public List<Loan> getLoans() {
 		return loans;
 	}
 	
 	public List<Loan> getLentLoans() {
 		List<Loan> lentLoans = new ArrayList<Loan>();
 		for(Loan l : loans) {
 			if(l.isLent())
 				lentLoans.add(l);
 		}
 		return lentLoans;
 	}
 
 	public List<Book> getBooks() {
 		return books;
 	}
 
 	public List<Customer> getCustomers() {
 		return customers;
 	}
 
 	public Customer getCustomerPerID(String id) {
 		for(Customer c : customers) {
			if(c.getId().equals(id))
 				return c;
 		}
 		return null;
 	}
 	
 	@Override
 	public void update(Observable o, Object arg) {
 		notifyAllObservers();
 	}
 
 	private void notifyAllObservers() {
 		setChanged();
 		notifyObservers();
 	}
 }
