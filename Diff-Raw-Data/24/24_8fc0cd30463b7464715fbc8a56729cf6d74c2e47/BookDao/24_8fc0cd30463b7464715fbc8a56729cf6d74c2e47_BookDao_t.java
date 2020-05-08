 package americano.dao;
 
 import java.text.MessageFormat;
import java.util.List;
 
 import org.springframework.stereotype.Repository;
 
 import americano.model.Book;
 
 @Repository
 public class BookDao
 extends AbstractDao
 {
 	
 	@SuppressWarnings("unchecked")
	public List<Book> getBooksOf( final String userId )
 	{
		return (List<Book>) find( MessageFormat.format( "from Book book where book.owner = '{}", userId ) );
 	}
 	
 
 }
