 package presentation.action;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JOptionPane;
 
 import domain.Book;
 
 import presentation.AddBooksPanel;
 
 public class AddAddButtonListener implements ActionListener {
 	
 	private AddBooksPanel abp;
 
 	public AddAddButtonListener(AddBooksPanel abp) {
 		this.abp = abp;
 
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		String title = abp.getTitle();
 		String firstname = abp.getAuthorFirstName();
 		String lastname = abp.getAuthorLastName();
 		String genre = abp.getGenre();
 		String publisher = abp.getPublisher();
 		String format = abp.getFormat();
 		int quantity = abp.getQuantity();
 		int price = abp.getPrice();
 		int isbn = abp.getISBN();
 		
 		if(!title.isEmpty() && !firstname.isEmpty() && !lastname.isEmpty() && !genre.isEmpty() && 
 				!publisher.isEmpty() && !format.isEmpty() && quantity >= 1 && price >= 1 && isbn >= 1) {
 			
			Book book = new Book(isbn, title, firstname, lastname, genre, publisher, format, price);
			if(book.saveBook()) {
 
 				JOptionPane.showMessageDialog(null, "Bok inlagd i databasen", "Bok sparad",
 						JOptionPane.INFORMATION_MESSAGE);
 			}
 		}
 		else {
 			JOptionPane.showMessageDialog(null,  "Du mste fylla  alla flten", "Obs", 
 					JOptionPane.ERROR_MESSAGE);
 		}
 	}
 }
