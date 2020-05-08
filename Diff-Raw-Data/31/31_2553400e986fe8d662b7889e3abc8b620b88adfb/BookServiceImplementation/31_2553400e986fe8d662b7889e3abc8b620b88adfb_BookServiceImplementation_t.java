 package ohtu.refero.service;
 
 import java.util.List;
 import ohtu.refero.models.Book;
 import ohtu.refero.repositories.BookRepository;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 @Service
 public class BookServiceImplementation implements BookService {
 
     @Autowired
     BookRepository bookRepository;
 
     @Transactional
     @Override
     public Book save(Book book) {       
         if (book == null) {
             return null;
         }
         
         book = bookRepository.save(book);
         
         return book;
     }
 
     @Override
     public List<Book> findAll() {
         return bookRepository.findAll();
     }
     
     @Override
     public Book findById(Long id) {
         return bookRepository.findOne(id);
     }

//    @Override
//    public Book findByReferenceId(String id) {
//        return bookRepository.findByReferenceId(id);
//    }
 }
