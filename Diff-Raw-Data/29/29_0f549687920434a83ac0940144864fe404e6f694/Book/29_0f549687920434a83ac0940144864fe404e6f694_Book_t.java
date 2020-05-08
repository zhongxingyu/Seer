 package gizmo385.librarymanager.types;
 
 import gizmo385.datastructures.sll.SingleLinkedList;
 
 /**
  * Represents a book item
  * @author cachapline8
  *
  */
 public class Book extends Item implements SavableItem
 {
 	private int yearPublished;
 	private String author, publisher;
 	/**
 	 * Creates a book object and passes name, copies, and tags to the super constructor (Item)
 	 * @param name Passed to super constructor
 	 * @param copies Passed to super constructor
 	 * @param tags Passed to super constructor
 	 * @param genre Passed to super constructor
 	 * @param isbn A book's ISBN (International Standard Book Number)
 	 * @param yearPublished The year the book was published
 	 * @param author The book's author
 	 * @param publisher The book's publisher
 	 */
 	public Book( String name, int copies, SingleLinkedList<String> tags , String isbn, int yearPublished, String publisher, String author, String genre )
 	{
 		super( name, copies, tags, genre, isbn ); //call to super constructor (Item)
 		this.author =  author;
 		this.publisher = publisher;
 		this.yearPublished = yearPublished;
 		
 		this.addTag( author );
 		this.addTag( publisher );
 	}
 	
 	/** Returns the year published */
 	public int getYearPublished()
     {
         return this.yearPublished;
     }
 	
 	/** Returns the author */
 	public String getAuthor()
     {
         return this.author;
     }
 	
 	/** Returns the publisher */
 	public String getPublisher()
     {
         return this.publisher;
     }
 	
 	/** Sets the year published */
 	public void setYearPublished( int yearPublished )
     {
         this.yearPublished = yearPublished;
     }
 	
 	/** Sets the author */
 	public void setAuthor( String author )
     {
         this.author = author;
     }
 	
 	/** Sets the publisher */
 	public void setPublisher( String publisher )
     {
         this.publisher = publisher;
     }
 
 	/** Returns a string representation of this item */
 	public String toString()
 	{
        StringBuilder sb = new StringBuilder( super.toString() );
        sb.append( "Author" );
         sb.append( this.getAuthor() );
         sb.append( System.lineSeparator() );
         sb.append( "Genre: " );
         sb.append( this.getGenre() );
         sb.append( System.lineSeparator() );
         sb.append( "Publisher: " );
         sb.append( this.getPublisher() );
         sb.append( System.lineSeparator() );
         sb.append( "Year Published: " );
         sb.append( this.getYearPublished() );
         sb.append( System.lineSeparator() );
         sb.append( "ISBN: " );
         sb.append( super.getId() );
 
         return sb.toString();
 	}
 
     /** Save data for this item */
 	public String toSaveString()
 	{
 		StringBuilder sb = new StringBuilder( "[book]" );
         sb.append( System.lineSeparator() );
 		sb.append( this.getName() );
         sb.append( System.lineSeparator() );
 		sb.append( this.getCopies() );
         sb.append( System.lineSeparator() );
 		sb.append( this.getGenre() );
         sb.append( System.lineSeparator() );
 		
 		for( String s : this.getTags() )
         {
 			sb.append( s.trim() );
             sb.append( ";" );
         }
 		
 		sb.append( System.lineSeparator() );
         sb.append( this.getAuthor() );
         sb.append( System.lineSeparator() );
 		sb.append( this.getPublisher() );
         sb.append( System.lineSeparator() );
 		sb.append( this.getId() );
         sb.append( System.lineSeparator() );
 		sb.append( this.getYearPublished() );
         sb.append( System.lineSeparator() );
 		
 		return sb.toString();
 	}
 
 
 } // end Book
