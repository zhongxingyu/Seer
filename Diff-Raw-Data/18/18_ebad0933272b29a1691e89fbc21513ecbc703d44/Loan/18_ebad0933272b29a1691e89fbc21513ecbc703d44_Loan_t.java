 package domain;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.Observable;
 import java.util.Observer;
 
 
 public class Loan extends Observable{
 
 	private Copy copy;
 	private Customer customer;
 	private GregorianCalendar pickupDate, returnDate;
 	private final static int DAYS_TO_RETURN_BOOK = 30;
 
 	public Loan(Customer customer, Copy copy) {
 		
 		this.copy = copy;
 		this.customer = customer;
 		pickupDate = new GregorianCalendar();
 	}
 
 	public boolean isLent() {
 		return returnDate == null;
 	}
 
 	public boolean returnCopy() {
 		try {
 			returnCopy(new GregorianCalendar());
 		} catch (IllegalLoanOperationException e) {
 			return false;
 		}
 		return true;
 	}
 
 	public void returnCopy(GregorianCalendar returnDate)
 			throws IllegalLoanOperationException {
		if (returnDate.before((Calendar)pickupDate) && !(getFormattedDate(returnDate).equals(getFormattedDate(pickupDate)))) {
 			throw new IllegalLoanOperationException(
 					"Return Date is before pickupDate");
 		}
 		this.returnDate = returnDate;
 		setChanged();
 		notifyObservers();
 	}
 
 	public void setPickupDate(GregorianCalendar pickupDate)
 			throws IllegalLoanOperationException {
 		if (!isLent()) {
 			throw new IllegalLoanOperationException("Loan is already returned");
 		}
 		this.pickupDate = pickupDate;
 		setChanged();
 		notifyObservers();
 	}
 	
 	
 
 	public GregorianCalendar getPickupDate() {
 		return pickupDate;
 	}
 	
 	public String getFormattedPickupDate() {
 		return getFormattedDate(getPickupDate());
 	}
 
 	private GregorianCalendar getReturnDate() {
 		return returnDate;
 	}
 	
 	public String getFormattedReturnDate() {
 		return getFormattedDate(getReturnDate());
 	}
 
 	public Copy getCopy() {
 		return copy;
 	}
 
 	public Customer getCustomer() {
 		return customer;
 	}
 
 	@Override
 	public String toString() {
 		return "Loan of: " + copy.getBook().getName() + "\tFrom: "
 				+ customer.getName() + " " + customer.getSurname()
 				+ "\tPick up: " + getFormattedDate(pickupDate) + "\tReturn: "
 				+ getFormattedDate(returnDate) + "\tDays: "
 				+ getDaysOfLoanDuration();
 	}
 
 	private String getFormattedDate(GregorianCalendar date) {
 		if (date != null) {
 			DateFormat f = SimpleDateFormat.getDateInstance();
 			return f.format(date.getTime());
 		}
 		return "00.00.00";
 	}
 
 	public int getDaysOfLoanDuration() {
 		if (returnDate != null)
 			return (int) (returnDate.getTimeInMillis() - pickupDate
 					.getTimeInMillis())
 					/ 1000 / 60 / 60 / 24;
 		return -1;
 	}
 	
 	public int getDaysOverdue() {
 		if ( !isOverdue() )
 			return 0;
 		
 		GregorianCalendar dueDate = (GregorianCalendar) pickupDate.clone();
 		dueDate.add(GregorianCalendar.DAY_OF_YEAR, DAYS_TO_RETURN_BOOK);
 		
 		return (int) (new GregorianCalendar().getTimeInMillis() - 
 				dueDate.getTimeInMillis())/ 1000 /60 /60 /24;
 	}
 	
 	
 	private GregorianCalendar getExpectedReturnDate() {
 
 		GregorianCalendar dueDate = (GregorianCalendar) pickupDate.clone();
 		dueDate.add(GregorianCalendar.DAY_OF_YEAR, DAYS_TO_RETURN_BOOK);
 		dueDate.add(GregorianCalendar.HOUR_OF_DAY, 23);
 		dueDate.add(GregorianCalendar.MINUTE, 59);
		dueDate.add(GregorianCalendar.SECOND, 58);
 		
 		return dueDate;
 		
 		
 	}
 	
 	public String getFormattedExpectedReturnDate() {
 		return getFormattedDate(getExpectedReturnDate());
 	}
 	
 	
 	
 	public boolean isOverdue() {
 		if ( !isLent() )
 			return false;
 		
 		GregorianCalendar dueDate = getExpectedReturnDate();
 		
 		return ( new GregorianCalendar().after(dueDate) );
 	}
 }
