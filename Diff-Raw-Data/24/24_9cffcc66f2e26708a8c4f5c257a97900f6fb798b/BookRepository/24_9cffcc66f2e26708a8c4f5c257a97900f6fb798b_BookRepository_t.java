 package bookstore.repository;
 
 import java.util.List;
 
 import org.springframework.data.jpa.repository.JpaRepository;
 import bookstore.entity.*;
import bookstore.entity.Book.Category;
 
 public interface BookRepository extends JpaRepository<Book, String> {
 
 	//List<Book> findBooks();
 	List<Book> findBooksByIsSpecial(Boolean isSpecial);
 	List<Book> findBooksByCategory(Category category);
 	Book findBookByIsbn(String ISBN);
 	//List<Book> findBookByKeyword(String keyword);
 }
