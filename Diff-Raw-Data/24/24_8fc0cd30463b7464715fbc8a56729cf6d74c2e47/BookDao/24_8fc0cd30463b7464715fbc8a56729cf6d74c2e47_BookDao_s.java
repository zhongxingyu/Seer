 package americano.dao;
 
 import java.text.MessageFormat;
import java.util.Collection;
 
 import org.springframework.stereotype.Repository;
 
 import americano.model.Book;
 
 @Repository
 public class BookDao
 extends AbstractDao
 {
 	
 	@SuppressWarnings("unchecked")
	public Collection<Book> getBooksOf( final String userId )
 	{
		return (Collection<Book>) find( MessageFormat.format( "from Book book where book.owner = '{}", userId ) );
 	}
 	
 
 }
