 package com.bk.bean;
 
 import java.io.Serializable;
 import java.util.List;

 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;

 import org.primefaces.event.RateEvent;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Component;
 
import com.bk.model.Book;
import com.bk.model.Rating;
import com.bk.repository.RatingRepository;
import com.bk.service.BookService;

 /**
  * User: ph
  * Date: 3/4/13
  */
 @Component
 @Scope("session")
 public class BookBean implements Serializable {
 
 	@Autowired
 	private LoginBean loginBean;
 
 	@Autowired
 	private RatingRepository ratingRepository;
 
 	@Autowired
 	private BookService bookService;
 
 	private Book book;
 	private Rating bookRating;
 	private Long id;
 
 	public void init() {
 		if (id == null) {
 			String message = "Bad request. Please use a link from within the system.";
 			FacesContext.getCurrentInstance().addMessage(null,
 					new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
 			return;
 		}
 
 		book = bookService.findById(id);
 
 		if (book == null) {
 			String message = "Bad request. Unknown book.";
 			FacesContext.getCurrentInstance().addMessage(null,
 					new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
 			return;
 		}
 
 		if (loginBean.getLoggedInUser() != null) {
 			bookRating = ratingRepository.getCustomerRating(book, loginBean.getLoggedInUser());
 			if (bookRating == null) {
				newRating();
 			}
 		}
 		else {
			newRating();
 		}
 	}
 
 	public String getAverageRating() {
 		Double averageRating = ratingRepository.getBookRating(book);
		if (averageRating == null) {
			return null;
		}
 		return String.format("%1$,.2f", averageRating);
 	}
 
 	public Long getNumberOfRatings() {
 		return ratingRepository.getNumberOfBookRatings(book);
 	}
 
 	public void addRating(RateEvent event) {
 		Integer rate = (Integer) event.getRating();
 		if (bookRating.getBook() == null) {
 			bookRating.setBook(book);
 			bookRating.setCustomer(loginBean.getLoggedInUser());
 		}
 		bookRating.setRating(rate);
 		ratingRepository.save(bookRating);
 	}
 
 	public void removeRating() {
 		ratingRepository.delete(bookRating);
 	}
 
 	public List<Book> search(String input) {
 		return bookService.search(input, 0, 10);
 	}
	
	private void newRating() {
		bookRating = new Rating();
		bookRating.setRating(0);
	}
 
 	public Book getBook() {
 		return book;
 	}
 
 	public void setBook(Book book) {
 		this.book = book;
 	}
 
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public Rating getBookRating() {
 		return bookRating;
 	}
 
 	public void setBookRating(Rating bookRating) {
 		this.bookRating = bookRating;
 	}
 }
