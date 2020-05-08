 package ohtu.refero.repositories;
 
 import ohtu.refero.models.Book;
 import org.springframework.data.jpa.repository.JpaRepository;
 
 public interface BookRepository extends JpaRepository<Book, Long> {
//    Book findByReferenceId(String id);
 }
