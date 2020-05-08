 package com.twu.biblioteca;
 
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import java.util.ArrayList;
 
 import org.junit.Test;
 
 public class ListBooksActionTest {
 	
 	@Test
 	public void shouldListBooks() {
 		final String FIRST_BOOK = "1. First Book";
 		final String SECOND_BOOK = "1. Second Book";
 		
 		Printer mockedPrinter = mock(Printer.class);
 		Book mockedBook1 = mock(Book.class);
 		when(mockedBook1.toString()).thenReturn(FIRST_BOOK);
 		Book mockedBook2 = mock(Book.class);
 		when(mockedBook2.toString()).thenReturn(SECOND_BOOK);
 		
 		ArrayList<Book> booksList = new ArrayList<Book>();
 		booksList.add(mockedBook1);
 		booksList.add(mockedBook2);
 		ListBooksAction listBooksAction = new ListBooksAction(mockedPrinter, booksList);
 		listBooksAction.execute();
 
 		verify(mockedPrinter).println(ListBooksAction.LIST_OF_BOOKS_MSG);
 		verify(mockedPrinter).println(FIRST_BOOK);
 		verify(mockedPrinter).println(SECOND_BOOK);
 	}
 
 }
