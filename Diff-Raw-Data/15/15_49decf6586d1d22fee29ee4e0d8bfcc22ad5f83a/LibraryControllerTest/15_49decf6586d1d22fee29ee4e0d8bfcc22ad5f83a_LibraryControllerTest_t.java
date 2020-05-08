 package com.twu.biblioteca.Controller;
 
 import com.twu.biblioteca.Domain.Book;
 import com.twu.biblioteca.Domain.Library;
 import org.junit.Test;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import java.util.List;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.*;
 
 public class LibraryControllerTest {
 
   @Test
   public void shouldCreateLibrary() {
     LibraryController libraryController = new LibraryController();
     Library library = libraryController.createLibrary(1L,"Bangalore Library");
     assertThat(library.getId(), is(1L));
     assertThat(library.getName(), is("Bangalore Library"));
   }
 
   @Test
   public void shouldAddBookToList() {
     LibraryController libraryController = new LibraryController();
     Book book = new Book(1L, "HarryPotter", "J.K. Rowling", false);
 //    BookController bookController = mock(BookController.class);
 
     libraryController.addBookToLibrary(book);
     assertThat(libraryController.getAllBooks().size(), is(3));
 //    verify(bookController).addBook(book);
   }
 
   @Test
   public void shouldGetAllBooks() {
     LibraryController libraryController = new LibraryController();
     Book book = new Book(1L, "HarryPotter", "J.K. Rowling", false);
     libraryController.addBookToLibrary(book);
     List books = libraryController.getAllBooks();
     assertThat(books.size(), is(2));
   }
 
 
   @Test
   public void shouldGetIsIssuedStatus() {
     LibraryController libraryController = new LibraryController();
     Book book = new Book(1L, "HarryPotter", "J.K. Rowling", false);
     libraryController.addBookToLibrary(book);
     Boolean isIssued = libraryController.isBookIssued("HarryPotter");
    assertThat(isIssued, is(true));
   }
 
   @Test
   public void shouldUpdateIsIssuedStatus() {
     Book book = new Book(1L, "HarryPotter1", "J.K. Rowling", false);
     LibraryController libraryController = new LibraryController();
     libraryController.addBookToLibrary(book);
 
     libraryController.updateBookStatus("HarryPotter1",true);
 
     assertThat(book.getIsIssued(), is(true));
   }
 }
