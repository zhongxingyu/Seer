 package domain;
 
 import java.util.Observable;
 
 import controll.LibraryEvent;
 
 public class Book extends Observable{
 	
 	private String title, author, publisher;
 	private Shelf shelf;
 	private boolean invalid = false;
 	
 	public Book(String name) {
 		this.title = name;
 		this.author = "";
 		this.publisher = "";
 	}
 
 	public String getName() {
 		return title;
 	}
 
 	public void setName(String name) {
 		this.title = name;
 		fireChange(LibraryEvent.updated);
 	}
 
 	public String getAuthor() {
 		return author;
 	}
 
 	public void setAuthor(String autor) {
 		this.author = autor;
 		fireChange(LibraryEvent.updated);
 	}
 
 	public String getPublisher() {
 		return publisher;
 	}
 
 	public void setPublisher(String publisher) {
 		this.publisher = publisher;
 		fireChange(LibraryEvent.updated);
 	}
 	
 	public Shelf getShelf() {
 		return shelf;
 	}
 	
 	public void setShelf(Shelf shelf) {
 		this.shelf = shelf;
 		fireChange(LibraryEvent.updated);
 	}
 	
 	@Override
 	public String toString() {
 		return title + ", " + author + ", " + publisher;
 	}
 
 	public void fireChange(LibraryEvent libraryEvent) {
 		setChanged();
 		notifyObservers(libraryEvent);	
 	}
 	
 
 	public void setInvalid() {
 		this.invalid  = true;
 		
 	}
 
 	public boolean isInvalid() {
 		return invalid;
 	}
 
 }
