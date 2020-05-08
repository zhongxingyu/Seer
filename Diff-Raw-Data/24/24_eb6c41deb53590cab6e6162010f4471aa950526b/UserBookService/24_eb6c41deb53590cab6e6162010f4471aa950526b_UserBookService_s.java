 package edu.upc.dsbw.spring.business.service;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.HashMap;
 
 import javax.servlet.http.HttpSession;
 
 import org.hibernate.Criteria;
 import org.hibernate.criterion.Restrictions;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import edu.upc.dsbw.spring.business.dao.intefaces.AuthorDao;
 import edu.upc.dsbw.spring.business.dao.intefaces.BookDao;
 import edu.upc.dsbw.spring.business.dao.intefaces.UserBookDao;
 import edu.upc.dsbw.spring.business.dao.intefaces.UserDao;
 import edu.upc.dsbw.spring.business.model.*;
 
 @Service
 @Transactional
 public class UserBookService {
 	
 	@Autowired
 	private BookDao bookDao;
 	
 	@Autowired
 	private AuthorDao authorDao;
 	
 	@Autowired
 	private BookService bookService;
 	
 	@Autowired
 	private UserService userService;
 	
 	@Autowired
 	private UserDao userDao;
 
 	@Autowired
 	private UserBookDao userBookDao;
 
 	public UserBook getUserBook(Integer bookId, Integer userId) {
 		UserBook userBook = userBookDao.findByUserBook(bookId, userId);
 		
 		if (userBook == null) return new UserBook(bookService.getBook(bookId), userService.getUser(userId));
 		return userBook;
 	}
 
 	public List<UserBook> getReviews(Integer bookId){
 		return userBookDao.getReviews(bookId);
 	}
 	
     public List<UserBook> getAcceptedReviews(Integer bookId){
 		return userBookDao.getAcceptedReviews(bookId);
 	}
 	
     public List<UserBook> getReviewsToAdministrate(){
 		return userBookDao.getReviewsToAdministrate();
 	}
 	
 	public void updateReview(Integer bookId, Integer userId, String review){
 		UserBook userBook = userBookDao.findByUserBook(bookId, userId);
 		
 		if (userBook == null){
		} else {
			userBook.setReview(review);
			userBook.unsetAcceptReview();
			userBookDao.saveOrUpdate(userBook);			
 		}
 		
 	}
 
 	public boolean addUserBook(String username, String firstname, String lastname, String password)  {
 		User user = new User( username, firstname, lastname, password );
 
 		userDao.saveOrUpdate(user);
 
 		return true;
 	}
 	
 	public void setRead(Integer userId, final Integer bookID, boolean status) {
 		UserBook userbook = getUserBook(bookID, userId);
 		if (userbook == null) throw new IllegalArgumentException("bookID doesn't match with any existing book");
 		
 		userbook.setRead(status);
 		if (status) {
 			userbook.setReading(false);
 		}
 		userBookDao.saveOrUpdate(userbook);
 	}
 	
 	public void setReading(Integer userId, final Integer bookID, boolean status) {
 		UserBook userbook = getUserBook(bookID, userId);
 		if (userbook == null) throw new IllegalArgumentException("bookID doesn't match with any existing book");
 		
 		userbook.setReading(status);
 		userBookDao.saveOrUpdate(userbook);
 	}
 	
 	public void setVote(Integer userId, Integer bookID, Integer vote){
 	  if (userId == null) throw new IllegalArgumentException("Unavailable function for visitors");
     UserBook userbook = getUserBook(bookID, userId);
 		if (userbook == null) throw new IllegalArgumentException("bookID doesn't match with any existing book");
 		
 		userbook.setVote(vote);
 		userBookDao.saveOrUpdate(userbook);
 		
 		evalAvRating(bookID);
 	}
         	
   // @Transactional(readOnly = true)
 	public HashMap<Integer, Integer> getVotesByValue(Integer bookId) {
 	    List<UserBook> userBooks = userBookDao.findByBook(bookId);
 	    
 	    HashMap<Integer, Integer> votesByValue = new HashMap<Integer, Integer>();
 
 	    votesByValue.put(1,0);
 	    votesByValue.put(2,0);
 	    votesByValue.put(3,0);
 	    votesByValue.put(4,0);
 	    votesByValue.put(5,0);
 
 	    Integer totalVotesValue = 0;
 	    Integer totalNumVotes = 0;
 	    
 	    Iterator<UserBook> i = userBooks.iterator();
 	    while( i.hasNext() ) {
 	      UserBook entry = i.next();
 	      if(entry.getRead() || entry.getReading()){
 	        if(entry.getVote() != null && entry.getVote() != 0){
 	          Integer vote = entry.getVote();
 	          Integer newVote = votesByValue.get(vote)+1;
 	          votesByValue.put(vote, newVote );
 	          totalVotesValue += entry.getVote();
 	          totalNumVotes+=1;
 	        }
 	      }
 	    }
 	    votesByValue.put(0, totalNumVotes);
 
 	    return votesByValue;
 		
 	}
 	
 	private void evalAvRating(Integer bookId){
 	    List<UserBook> userBooks = userBookDao.findByBook(bookId);
 
 		  Integer totalVotesValue = 0;
 	    Integer totalNumVotes = 0;
 		  
 		  Iterator<UserBook> i = userBooks.iterator();
 	    while( i.hasNext() ) {
 	      UserBook entry = i.next();
 	      if(entry.getRead() || entry.getReading()){
 	        if(entry.getVote() != null && entry.getVote() != 0){
 	          totalVotesValue += entry.getVote();
 	          totalNumVotes+=1;
 	        }
 	      }
 	    }
 	    
 	    Book b = bookDao.findById(bookId);
 	    if(totalNumVotes > 0)
 	      b.setAvRating(totalVotesValue.floatValue()/totalNumVotes.floatValue());
 	    bookDao.saveOrUpdate(b);
 	}
 
 
 	
 	public void acceptReview(Integer bookId, Integer userId){
 	    if (userId == null) throw new IllegalArgumentException("Unavailable function for visitors");
         UserBook userbook = getUserBook(bookId, userId);
 		if (userbook == null) throw new IllegalArgumentException("bookID doesn't match with any existing book");
 		
         userbook.acceptReview();
 		userBookDao.saveOrUpdate(userbook);
 	}
 	
     public void rejectReview(Integer bookId, Integer userId){
 	    if (userId == null) throw new IllegalArgumentException("Unavailable function for visitors");
         UserBook userbook = getUserBook(bookId, userId);
 		if (userbook == null) throw new IllegalArgumentException("bookID doesn't match with any existing book");
 		
         userbook.rejectReview();
 		userBookDao.saveOrUpdate(userbook);
 	}
 	
   
 }
