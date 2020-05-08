 package edu.upc.dsbw.spring.web;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.HashMap;
 
 import javax.servlet.http.HttpSession;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.multipart.MultipartFile;
 import org.springframework.web.servlet.ModelAndView;
 
 import edu.upc.dsbw.spring.business.model.Book;
 import edu.upc.dsbw.spring.business.model.Author;
 import edu.upc.dsbw.spring.business.model.User;
 import edu.upc.dsbw.spring.business.model.UserBook;
 import edu.upc.dsbw.spring.business.service.BookService;
 import edu.upc.dsbw.spring.business.service.UserBookService;
 import edu.upc.dsbw.spring.business.service.UserService;
 import edu.upc.dsbw.spring.business.service.AuthorService;
 import edu.upc.dsbw.spring.business.service.BookService.ListBooksOrderBy;
 import edu.upc.dsbw.spring.business.service.TimelineService;
 
 @Controller
 @RequestMapping("/books/*")
 public class BooksController {
 
 	@Autowired
 	BookService bookService;
 	@Autowired
 	UserService userService;
 	@Autowired
 	UserBookService userBookService;
 	@Autowired
 	AuthorService authorsService;
     @Autowired
     TimelineService timelineService;	
         
 	@RequestMapping(value = "/uploadImage", method = RequestMethod.POST)
     public ModelAndView uploadImage(@RequestParam("bookId") Integer bookId, @RequestParam("file") MultipartFile file, HttpSession session) {
 		try {
 			Boolean isAdmin = (session.getAttribute("isAdmin") != null && (Boolean)session.getAttribute("isAdmin") == true);
 			
 			String fileName = bookService.setImage((Integer)session.getAttribute("userId"), isAdmin, bookId, file);
 			
 			return new ModelAndView("ajax/ok", "extraData", "\"" + fileName + "\"");
 		}
 		catch (Exception ex) {
 			return new ModelAndView("ajax/error", "extraData", ex.getMessage());
 		}
     }
 
 
 	// TODO falta que ordeni per autor
 	@RequestMapping(value = "/by/{orderBy}", method = RequestMethod.GET)
 	public ModelAndView listBooks(
 			@RequestParam(value = "searchtext", required = false) String filter,
 			@PathVariable("orderBy") ListBooksOrderBy orderBy) {
 		
 		List<Book> books = new LinkedList<Book>();
 		if (filter == null) {
 			books = bookService.listBooks(orderBy);
 		} else {
 			books = bookService.searchBooks(filter, orderBy);
 		}
 		return new ModelAndView("books/list", "books", books);
 	}
 
 	// TODO PROVA LIST READING BOOKS
 	@RequestMapping(value = "/reading/{userId}", method = RequestMethod.GET)
 	public ModelAndView listBooks(@PathVariable("userId") Integer userid) {
 		List<Book> books = bookService.getReadingBooks(userid);
 		return new ModelAndView("books/list", "books", books);
 	}
 
 	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
 	public ModelAndView getBook(@PathVariable("id") Integer id, HttpSession session) {
 		List<UserBook> reviewsList = userBookService.getAcceptedReviews(id);
 
 		Book book = bookService.getBook(id);
 		bookService.incrPopularity(id);
 		if (session.getAttribute("username") != null
 				&& session.getAttribute("username") != "") {
 			UserBook ub = userBookService.getUserBook(id, (Integer)session.getAttribute("userId"));
 			int userId = (Integer) session.getAttribute("userId");
 			int bookId = book.getUploader();
 
 
 			if( bookId ==  userId || (Boolean)session.getAttribute("isAdmin") == true ){
 				List<Author> authors = authorsService.listAuthors();
 
 				ModelAndView mV = new ModelAndView("books/detailModification", "userbook", ub);
 				mV.addObject("reviewsList", reviewsList);
 				mV.addObject("authorsList", authors);
 
 				return mV;
 			}else{
 
 				ModelAndView mV = new ModelAndView("books/detailLoggedIn", "userbook", ub);
 				mV.addObject("reviewsList", reviewsList);
 
 				return mV;	
 			}
 			
 		}
 		else {
 			
 			ModelAndView mV = new ModelAndView("books/detail", "book", book);
 			mV.addObject("reviewsList", reviewsList);
 
 			return mV;
 		}
 	}
 
 	// AJAX
 	@RequestMapping(value = "/setRead", method = RequestMethod.POST)
 	public ModelAndView setReadBook(@RequestParam("bookId") Integer id, @RequestParam("status") boolean status, HttpSession session) {
 		try {
 			Integer userId = (Integer)session.getAttribute("userId");
 			if (userId == null) throw new IllegalArgumentException("Unavailable function for visitors");
 			
 			userBookService.setRead(userId, id, status);
             Book book = bookService.getBook(id);
             User user = userService.getUser((Integer)session.getAttribute("userId"));
             timelineService.addReadBookActivity(user, book, status);
 
 			return new ModelAndView("ajax/ok");
 		}
 		catch (IllegalArgumentException ex) {
 			return new ModelAndView("ajax/error", "extraData", ex.getMessage());
 		}
 	}
 
 	// AJAX
 	@RequestMapping(value = "/setReading", method = RequestMethod.POST)
 	public ModelAndView setReadingBook(@RequestParam("bookId") Integer id, @RequestParam("status") boolean status, HttpSession session) {
 		try {
 			Integer userId = (Integer)session.getAttribute("userId");
 			if (userId == null) throw new IllegalArgumentException("Unavailable function for visitors");
 			
 			userBookService.setReading(userId, id, status);
             Book book = bookService.getBook(id);
             User user = userService.getUser((Integer)session.getAttribute("userId"));
             timelineService.addReadingBookActivity(user, book, status);
 
 			return new ModelAndView("ajax/ok");
 		}
 		catch (IllegalArgumentException ex) {
 			return new ModelAndView("ajax/error", "extraData", ex.getMessage());
 		}
 	}
 	
   @RequestMapping(value="/vote/{id}",method=RequestMethod.POST)
   public ModelAndView voteBook(@PathVariable("id") Integer id, @RequestParam("vote-value") int voteValue, HttpSession session){
     try{
       
       userBookService.setVote((Integer)session.getAttribute("userId"), id, voteValue);
 
       Book book = bookService.getBook(id);
       User user = userService.getUser((Integer)session.getAttribute("userId"));
       
       timelineService.addVoteActivity(user, book, voteValue);
       
       return new ModelAndView("ajax/vote","book", book);
     }
     catch(IllegalArgumentException ex){
       return new ModelAndView("axax/errorVote", "missatge", ex.getMessage());
     }
   }
 
   @RequestMapping(value="/votesByValue/{id}",method=RequestMethod.GET)
   public ModelAndView getVotesByValue(@PathVariable("id") Integer id){
     HashMap<Integer, Integer> votes = userBookService.getVotesByValue(id);
     return new ModelAndView("ajax/votesByValue","votes", votes);
   }
 	
   @RequestMapping(value = "/delete/{id}", method = RequestMethod.GET)
 	public ModelAndView deleteBook(@PathVariable("id") Integer bookId, HttpSession session) {
     if (session.getAttribute("username") != null) {
 			if(userService.isAdmin((Integer)session.getAttribute("userId"))){
 			  
 			  bookService.deleteBook(bookId);
     		return new ModelAndView("redirect:/books/by/insertionDate.html");
     		
       } else {
         return new ModelAndView("session/notLoggedIn", "message",
   					"Error: Necessites permisos d'administrador");
       }
 		} else {
 			return new ModelAndView("session/notLoggedIn", "message",
 					"Error: Has d'estar loguejat per tal de veure aquesta pàgina");
 		}
 	}
 
 	// TODO he provat de crear un nou autor
 	@RequestMapping(value = "/newAuthor/{firstName}/{lastName}", method = RequestMethod.GET)
 	public String addAuthor(@PathVariable("firstName") String firstName,
 			@PathVariable("lastName") String lastName) {
 		bookService.newAuthor(firstName, lastName);
 		List<Book> books = bookService
 				.listBooks(ListBooksOrderBy.insertionDate);
 		return "Author inserted";
 	}
 
 	// TODO busqueda de llibres per filtre --> encara no esta acabat
 	@RequestMapping(value = "/search/{orderBy}", method = RequestMethod.POST)
 	public ModelAndView search(@RequestParam("searchtext") String filter,
 			@PathVariable("orderBy") ListBooksOrderBy orderBy) {
 		List<Book> books = bookService.searchBooks(filter, orderBy);
 		return new ModelAndView("books/list", "books", books);
 	}
 
 	// TODO busqueda de llibres per filtre --> encara no esta acabat
 	@RequestMapping(value = "/eing/{orderBy}", method = RequestMethod.GET)
 	public ModelAndView eing(@RequestParam("searchtext") String filter,
 			@PathVariable("orderBy") ListBooksOrderBy orderBy) {
 		List<Book> books = bookService.searchBooks(filter, orderBy);
 		return new ModelAndView("books/list", "books", books);
 	}
 
 	// TODO llistar els usuaris que han llegit o estan llegins un llibre
 	@RequestMapping(value = "/users/book/{id}", method = RequestMethod.GET)
 	public ModelAndView listUsers(@PathVariable("id") Integer id) {
 		// List<User> users = bookService.listUsers(id);
 		return new ModelAndView("users/list", "users", null);
 	}
 
 
	@RequestMapping(value = "/updateReview", method = RequestMethod.POST)
 	public ModelAndView updateReview(@RequestParam("bookId") Integer id, @RequestParam("review") String review, @RequestParam("action") String action, HttpSession session){
 		if ( action.equals("Esborrar") ){
 			review = "";	
 		} 
 		if (review.length() < 1){
 			review = null;	
 		} 
 		if (session.getAttribute("username") != null && session.getAttribute("username") != "") {
 
                     Book book = bookService.getBook(id);
                     User user = userService.getUser((Integer)session.getAttribute("userId"));

                     timelineService.addReviewActivity(user, book);

                     userBookService.updateReview(id, (Integer)session.getAttribute("userId"), review);
 
 		} else {
 			
 		}
 		return new ModelAndView("redirect:" + id + ".html");
  
 	}
 	
 	@RequestMapping(value = "/admin-reviews", method = RequestMethod.GET)
 	public ModelAndView adminReviews(HttpSession session) {
 		if (session.getAttribute("username") != null) {
 			if(userService.isAdmin((Integer)session.getAttribute("userId"))){
 			  List<UserBook> reviewsList = userBookService.getReviewsToAdministrate();
 			  
 			  return new ModelAndView("books/adminreviews", "reviewsList", reviewsList);
             // .addObject("readBooks", readBooks);
       } else {
         return new ModelAndView("session/notLoggedIn", "message",
   					"Error: Necessites permisos d'administrador");
       }
 		} else {
 			return new ModelAndView("session/notLoggedIn", "message",
 					"Error: Has d'estar loguejat per tal de veure aquesta pàgina");
 		}
 	}
 
 	@RequestMapping(value = "/admin-reviews/accept", method = RequestMethod.POST)
 	public ModelAndView acceptReview(@RequestParam("bookId") Integer bookId, @RequestParam("userId") Integer userId, HttpSession session) {
 		if (session.getAttribute("username") != null) {
 			if(userService.isAdmin((Integer)session.getAttribute("userId"))){
 			  userBookService.acceptReview(bookId, userId);
 			  
 			  return new ModelAndView("ajax/review", "action", 1);
             // .addObject("readBooks", readBooks);
       } else {
         return new ModelAndView("ajax/errorVote", "missatge",
   					"Error: Necessites permisos d'administrador");
       }
 		} else {
 			return new ModelAndView("ajax/errorVote", "missatge",
 					"Error: Has d'estar loguejat per tal de veure aquesta pàgina");
 		}
 	}
 
   @RequestMapping(value = "/admin-reviews/reject", method = RequestMethod.POST)
 	public ModelAndView rejectReview(@RequestParam("bookId") Integer bookId, @RequestParam("userId") Integer userId, HttpSession session) {
 		if (session.getAttribute("username") != null) {
 			if(userService.isAdmin((Integer)session.getAttribute("userId"))){
 			  userBookService.rejectReview(bookId, userId);
 			  
 			  return new ModelAndView("ajax/review", "action", 0);
             // .addObject("readBooks", readBooks);
       } else {
         return new ModelAndView("ajax/errorVote", "missatge",
   					"Error: Necessites permisos d'administrador");
       }
 		} else {
 			return new ModelAndView("ajax/errorVote", "missatge",
 					"Error: Has d'estar loguejat per tal de veure aquesta pàgina");
 		}
 	}
 	
 
 	@RequestMapping(value = "/changeAuthors", method = RequestMethod.POST)
 	public ModelAndView changeAuthors(@RequestParam("authorsId") List<Integer> authorsId, @RequestParam("oldAuthorId") List<Integer> oldAuthorsId, @RequestParam("bookId") Integer bookId, HttpSession session) {
 
 
 		if( authorsId.isEmpty() || oldAuthorsId.isEmpty() ){
 			return new ModelAndView("redirect:" + bookId + ".html");	
 		}
 		bookService.changeAuthors (bookId, authorsId, oldAuthorsId);
 		return new ModelAndView("redirect:" + bookId + ".html");
 	}
 
 	@RequestMapping(value = "/changeTitle", method = RequestMethod.POST)
 	public ModelAndView changeBookTitle(@RequestParam("value") String newTitle, @RequestParam("bookId") Integer bookId, HttpSession session) {
 		bookService.setBookTitle(bookId, newTitle);
 		return new ModelAndView("ajax/changeValue", "value", newTitle);
 	}
 
 	@RequestMapping(value = "/changeYear", method = RequestMethod.POST)
 	public ModelAndView changeBookYear(@RequestParam("value") Integer newYear, @RequestParam("bookId") Integer bookId, HttpSession session) {
 		bookService.setBookYear(bookId, newYear);
 		return new ModelAndView("ajax/changeValue", "value", newYear);
 	}
 
 	@RequestMapping(value = "/changeDescription", method = RequestMethod.POST)
 	public ModelAndView changeBookDescription(@RequestParam("value") String newDescription, @RequestParam("bookId") Integer bookId, HttpSession session) {
 		
 		bookService.setBookDescription(bookId, newDescription);
 		return new ModelAndView("ajax/changeValue", "value", newDescription);
 	}
 
 	@RequestMapping(value = "/createBook", method = RequestMethod.GET)
 	public ModelAndView showCreateBookForm(HttpSession session) {
 			
 		if (session.getAttribute("username") != null) {
 			List<Author> authors = authorsService.listAuthors();
 			
 			return new ModelAndView("books/createBook", "authors", authors);
 		}else{
 			return new ModelAndView("redirect:by/insertionDate.html");
 		}
 			
 
 		
 	}
   
   	@RequestMapping(value = "/createBookForm", method = RequestMethod.POST)
 	public ModelAndView createBook(@RequestParam("authors") List<Integer> author, @RequestParam("title") String title, @RequestParam("year") Integer year, @RequestParam("editorial") String editorial, @RequestParam("description") String description, HttpSession session) {
 
 			if (session.getAttribute("username") != null || (Boolean)session.getAttribute("isAdmin") == true ) {
 				int uploader = (Integer)session.getAttribute("userId");	
 				
 				int bookId = bookService.addBook(uploader ,author, title, year, editorial, description);
 
 				return new ModelAndView("redirect:" + bookId + ".html");
 
 			}else{
 				return new ModelAndView("redirect:by/insertionDate.html");
 			}
 
 			
 			
 	}
 
 }
