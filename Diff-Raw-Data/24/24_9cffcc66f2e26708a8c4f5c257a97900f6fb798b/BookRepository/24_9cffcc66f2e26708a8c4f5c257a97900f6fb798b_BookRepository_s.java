 package bookstore.repository;
 
 import java.util.List;
 
import org.junit.experimental.categories.Category;
 import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import bookstore.entity.Book;
 import bookstore.entity.*;
 
 public interface BookRepository extends JpaRepository<Book, String> {
 
 	//List<Book> findBooks();
 	List<Book> findBooksByIsSpecial(Boolean isSpecial);
 	List<Book> findBooksByCategory(Category category);
 	Book findBookByIsbn(String ISBN);
 	//List<Book> findBookByKeyword(String keyword);
 }
