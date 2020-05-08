 package nl.wiggertloonstra.bible.hibernate;
 
 import static org.hamcrest.Matchers.is;
 import static org.junit.Assert.assertThat;
 
 import java.util.List;
 
 import nl.wiggertloonstra.bible.hibernate.domain.BookDo;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:test-applicationContext.xml"})
 public class BookRepositoryIntegrationTest {
 
     @Autowired
     BookRepository bookRepository;
     
     @Test
     public void getAllBooks() throws Exception {
         List<BookDo> books = bookRepository.getBooks();
        assertThat(books.size(), is(66));
     }
     
     @Test
     public void getBookByName() throws Exception {
         BookDo book = bookRepository.getBookWithName("Leviticus");
         assertThat(book.getName(), is("Leviticus"));
         
     }
     
 }
