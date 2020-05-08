 package com.twu.biblioteca.Controller;
 
 import com.twu.biblioteca.Domain.Book;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.runners.MockitoJUnitRunner;
 
 import java.util.List;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 @RunWith(MockitoJUnitRunner.class)
 
 public class BookControllerTest {
 
   @Test
   public void shouldGetBookByTitle() {
     BookController bookController = new BookController();
     bookController.addBook(new Book(1L, "HarryPotter", "J.K. Rowling", false));
     bookController.addBook(new Book(2L, "A Journey", "Tony Blair", false));
 
     Book book = bookController.getBookByTitle("HarryPotter");
 
     assertThat(book.getTitle(), is("HarryPotter"));
     assertThat(book.getId(), is(1L));
   }
 
   @Test
   public void shouldGetAllBooks() {
     BookController bookController = new BookController();
     bookController.addBook(new Book(1L, "HarryPotter", "J.K. Rowling", false));
     bookController.addBook(new Book(2L, "A Journey", "Tony Blair", false));
 
     List books = bookController.getAllBooks();
 
    assertThat(books.size(), is(8));
   }
 
   @Test
   public void shouldAddBookToList() {
     BookController bookController = new BookController();
     bookController.addBook(new Book(1L, "HarryPotter", "J.K. Rowling", false));
     bookController.addBook(new Book(2L, "A Journey", "Tony Blair", false));
 
    assertThat(bookController.books.size(), is(10));
   }
 
   @Test
   public void shouldCheckIsIssuedStatus() {
     BookController bookController = new BookController();
     bookController.addBook(new Book(1L, "HarryPotter", "J.K. Rowling", false));
     bookController.addBook(new Book(2L, "A Journey", "Tony Blair", false));
 
     Boolean isIssued = bookController.isBookIssued("HarryPotter");
 
    assertThat(isIssued, is(true));
   }
 
   @Test
   public void shouldReturnIsIssuedStatusFalseIfBookDoesNotExist() {
     BookController bookController = new BookController();
     bookController.addBook(new Book(1L, "HarryPotter", "J.K. Rowling", false));
     bookController.addBook(new Book(2L, "A Journey", "Tony Blair", false));
 
     Boolean isIssued = bookController.isBookIssued("Harry");
 
     assertThat(isIssued, is(false));
   }
 }
