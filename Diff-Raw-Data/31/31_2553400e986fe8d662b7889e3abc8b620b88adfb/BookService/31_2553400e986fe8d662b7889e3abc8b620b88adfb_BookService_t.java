 package ohtu.refero.service;
 
 import java.util.List;
 import ohtu.refero.models.Book;
 
 public interface BookService {
     
     public Book save(Book book);
     public List<Book> findAll();
     public Book findById(Long id);
//    public Book findByReferenceId(String id);
 }
