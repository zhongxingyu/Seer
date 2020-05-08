 package americano.service;
 
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 
 import americano.dao.BookDao;
 import americano.dao.UserDao;
 import americano.model.Book;
 import americano.model.User;
 
 public class UserService
 {
 	@Autowired
 	protected UserDao userDao;
 	
 	@Autowired
 	protected BookDao bookDao;
 	
	public User getUser( final String userId )
 	{
		return userDao.getUser( userId );
 		
 	}
 
	public List<Book> getBooks( String userId )
 	{
		return bookDao.getBooksOf( userId );
 	}
 
 
 }
