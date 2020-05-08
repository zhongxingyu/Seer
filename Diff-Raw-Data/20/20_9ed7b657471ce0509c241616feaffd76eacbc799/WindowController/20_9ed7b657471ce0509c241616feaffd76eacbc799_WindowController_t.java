 package controller;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.TreeMap;
 
 import javax.swing.JFrame;
 
 import view.AddLoanWindow;
 import view.DetailBookWindow;
 import view.DetailLoanWindow;
 import view.StartWindow;
 import domain.Book;
 import domain.Customer;
 import domain.Library;
 import domain.Loan;
 
 public class WindowController {
 	
 	Library library;
 	Map<Book, JFrame> openBooks = new TreeMap<>();
 	Map<Loan, JFrame> openLoans = new TreeMap<>();
 	Map<Customer, JFrame> openCustomers = new TreeMap<>();
 	
 	
 	public WindowController (Library lib) {
 		this.library = lib;
 		openMainWindow();
 	}
 	
 	public void openMainWindow() {
 		new StartWindow(library, this);
 	}
 	
	public void openDetailBookWindow(Book book){
		
			if (book != null && openBooks.containsKey(book)){
				JFrame frame = openBooks.get(book);
				frame.setVisible(true);
			}else{	
				DetailBookWindow detailFrame = new DetailBookWindow(library, this);
			    JFrame frame = detailFrame.setBook(book);
			    openBooks.put(book, frame);
			}    
 	}
 	
 	public void openAddBookWindow(){
 		DetailBookWindow detailFrame = new DetailBookWindow(library, this);
 	    detailFrame.setBook(null);	
 	}
 
 	public void openDetailLoanWindow(Loan loan){
 		if (openLoans.containsKey(loan)){
 			JFrame frame = openLoans.get(loan);
 			frame.setVisible(true);
 		} else {
 			DetailLoanWindow detailFrame = new DetailLoanWindow(loan, library, this);
 			detailFrame.setVisible(true);
 			openLoans.put(loan, detailFrame);
 		}	
 		
 	
 	}
 	
 	public void remove(JFrame o){
 		for(Entry<Loan, JFrame> e : openLoans.entrySet()) {
 			if (e.getValue().equals(o)){
 				openLoans.remove(e.getKey());
 			}
 		}
 		for(Entry<Book, JFrame> e : openBooks.entrySet()) {
 			if (e.getValue().equals(o)){
 				openBooks.remove(e.getKey());
 			}
 		}
 		for(Entry<Customer, JFrame> e : openCustomers.entrySet()) {
 			if (e.getValue().equals(o)){
 				openCustomers.remove(e.getKey());
 			}
 		}
 		
 		
 	}
 	
 	public void openAddLoanWindow() {
 		AddLoanWindow newWindow = new AddLoanWindow(library, this);
 		newWindow.setVisible(true);
 	}
 	
 	
 	
 	
 	
 
 }
