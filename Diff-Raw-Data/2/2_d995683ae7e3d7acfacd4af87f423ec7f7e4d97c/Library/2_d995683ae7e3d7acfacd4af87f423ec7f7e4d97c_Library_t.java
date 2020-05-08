 package domain;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
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
 		this.setChanged();
 		
 		if (!isCopyLent(copy)) {
 			Loan l = new Loan(customer, copy);
 			l.addObserver(this);
 			loans.add(l);
 			setChanged();
 			notifyObservers();
 			return l;
 		} else {
 			return null;
 		}
 	}
 
 	public Customer createAndAddCustomer(String name, String surname) {
 		this.setChanged();
 		
 		Customer c = new Customer(name, surname);
 		customers.add(c);		
 		
 		setChanged();
 		notifyObservers();
 		
 		return c;
 	}
 
 	public Book createAndAddBook(String name) {
 		
 		Book b = new Book(name);
 		books.add(b);	
 		
 		setChanged();
 		notifyObservers();
 		return b;
 	}
 
 	public Copy createAndAddCopy(Book title) {	
 
 		Copy c = new Copy(title);
 		copies.add(c);
 		
 		setChanged();
 		notifyObservers();
 
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
 
 	public boolean isCopyLent(Copy copy) {
 		for (Loan l : loans) {
 			if (l.getCopy().equals(copy) && l.isLent()) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public Copy getCopyfromId(int id) {
 		for (Copy c : copies) {
 			if (c.getInventoryNumber() == (id)) {
 				return c;
 			}
 		}
 		return null;
 	}
 
 	public List<Copy> getCopiesOfBook(Book book) {
 		List<Copy> res = new ArrayList<Copy>();
 		for (Copy c : copies) {
 			if (c.getBook().equals(book)) {
 				res.add(c);
 			}
 		}
 
 		return res;
 	}
 
 	public List<Loan> getLentCopiesOfBook(Book book) {
 		List<Loan> lentCopies = new ArrayList<Loan>();
 		for (Loan l : loans) {
 			if (l.getCopy().getBook().equals(book) && l.isLent()) {
 				lentCopies.add(l);
 			}
 		}
 		return lentCopies;
 	}
 	
 	public List<Loan> getActiveLoans() {
 		List<Loan> activeLoans = new ArrayList<>();
 		for(Loan loan : getLoans()){
 			if(loan.isLent()) {
 				activeLoans.add(loan);
 			}
 		}
 		return activeLoans;
 	}
 	
 	public int getNoOfAvailableCopiesOfBook(Book book){
 	
 		return getCopiesOfBook(book).size() - getLentCopiesOfBook(book).size();
 		
 		
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
 	
 	public List<Customer> getOverdueCustomers(){
 		List<Customer> overdueCustomers = new ArrayList<Customer>();
 		
 		for ( Customer c : getCustomers() ) {
 			if(this.isCustomerOverdue(c)){
 				overdueCustomers.add(c);
 			}
 		}
 		return overdueCustomers;
 	}
 	
 	public boolean isCustomerOverdue(Customer cust){
 		for(Loan loan : getLoansOfCustomer(cust)){
 			if(loan.isOverdue()){
 				return true;
 			}
 		}
 		return false;
 	}
	 
 	public boolean hasCustomerToManyLoans(Customer cust){
 		int threshold = 3;
 		//System.out.println(cust.toString() + " Number Loans: " + getPendingLoansOfCustomer(cust).size());
 		return getPendingLoansOfCustomer(cust).size() >= threshold;
 	}
 	
 	public CustomerStatus getCustomerStatus(Customer cust){
 		if (this.isCustomerOverdue(cust) && this.hasCustomerToManyLoans(cust)){
 			return CustomerStatus.OVERDUE_TO_MANY; 
 		}
 		if (this.isCustomerOverdue(cust)){
 			return CustomerStatus.OVERDUE;
 		}
 		if (this.hasCustomerToManyLoans(cust)){
 			return CustomerStatus.TO_MANY_BOOKS;
 		}
 		
 		return CustomerStatus.OK;
 	}
 	
 	public List<Loan> getLoansOfCustomer(Customer cust) {
 		List<Loan> customerLoans = new ArrayList<>();
 		
 		for(Loan loan : this.getLoans()) {
 			if(loan.getCustomer().equals(cust)){
 				customerLoans.add(loan);
 			}
 		}
 		return customerLoans;
 		
 		
 	}
 	
 	public List<Loan> getPendingLoansOfCustomer(Customer cust) {
 		List<Loan> pendingCustomerLoans = new ArrayList<>();
 		for(Loan loan : getLoansOfCustomer(cust)) {
 			if(loan.isLent()) {
 				pendingCustomerLoans.add(loan);
 			}
 			
 		}
 		return pendingCustomerLoans;
 		
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
 	
 	public void removeCopy(Copy c){		
 		copies.remove(c);
 		this.setChanged();
 		this.notifyObservers();
 	}
 
 	public List<Loan> getLoans() {
 		return loans;
 	}
 
 	public List<Book> getBooks() {
 		return books;
 	}
 
 	public List<Customer> getCustomers() {
 		return customers;
 	}
 	
 	public String getDateplusDays(Date date, int days){
 		GregorianCalendar cal = (GregorianCalendar) Calendar.getInstance();
         cal.setTime(date);
         cal.add(Calendar.DATE, days); //minus number would decrement the days
         DateFormat f = SimpleDateFormat.getDateInstance();
 		return f.format(cal.getTime());
         
 	}
 
 	@Override
 	public void update(Observable o, Object arg) {
 		setChanged();
 		notifyObservers();
 		
 	}
 
 }
