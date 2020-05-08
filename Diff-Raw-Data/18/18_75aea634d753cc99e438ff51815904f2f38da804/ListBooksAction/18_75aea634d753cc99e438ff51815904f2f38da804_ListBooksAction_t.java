 package com.twu.biblioteca;
 
 import java.util.ArrayList;
 
 public class ListBooksAction implements Action {
 	
 	final static public String LIST_OF_BOOKS_MSG = "List of Books in the library:";
 	
 	private Printer printer;
 	private ArrayList<Book> booksList;
 
 	public ListBooksAction(Printer printer, ArrayList<Book> booksList) {
 		this.printer = printer;
 		this.booksList = booksList;
 	}
 
 	@Override
 	public void execute() {
 		printer.println(LIST_OF_BOOKS_MSG);
 		for (Book book : booksList) {
 			printer.println(book.toString());
 		}
 	}
 
 }
