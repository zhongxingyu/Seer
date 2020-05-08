 package gizmo385.librarymanager.types;
 
 import gizmo385.datastructures.sll.SingleLinkedList;
 
 import java.util.Arrays;
 
 /**
  * Represents a music album
  * @author cachapline8
  *
  */
 public class Album extends Item implements SavableItem
 {
 	private String artist, label;
 	private String[] songList;
 	private int yearReleased;
 
 	/**
 	 * 
 	 * @param name Passed to super constructor
 	 * @param copies Passed to super constructor
 	 * @param tags Passed to super constructors
 	 * @param artist The artist or band name
 	 * @param genre The musical genre (i.e: Rock, jazz, pop, rap, etc.)
 	 * @param label The music label that this was released under
 	 * @param songList The songs present on the album
 	 * @param yearReleased The year this album was released
 	 */
 	public Album( String name, int copies, SingleLinkedList<String> tags, String artist, String genre, String label, String[] songList, int yearReleased, String barcodeNumber )
 	{
 		super( name, copies, tags, genre, barcodeNumber );
 		this.setArtist( artist );
 		this.setLabel( label );
 		this.setSongList( songList );
 		this.setYearReleased( yearReleased );
 		
 		this.addTag( artist );
 		this.addTag( label );
 		for( String s : songList )
			this.addTag( s );
 	}
 
 	/** Returns the artist */
 	public String getArtist()
     {
         return this.artist;
     }
 
 	/** Returns the label */
 	public String getLabel()
     {
         return this.label;
     }
 
 	/** Returns the year released */
 	public int getYearReleased()
     {
         return this.yearReleased;
     }
 
 	/** Returns the number of songs */
 	public int getNumberOfSongs()
     {
         return this.songList.length;
     }
 
 	/** Returns a copy of the song list */
 	public String[] getSongList()
     {
         return Arrays.copyOf( this.songList, this.songList.length );
     }
 
 	/** Sets the artist */
 	public void setArtist( String artist )
     {
         this.artist = artist;
     }
 
 	/** Sets the label */
 	public void setLabel( String label )
     {
         this.label = label;
     }
 
 	/** Sets the year released */
 	public void setYearReleased( int yearReleased )
     {
         this.yearReleased = yearReleased;
     }
 
 	/** Copies the passed array */
 	public void setSongList( String[] songList )
     {
         this.songList = Arrays.copyOf( songList, songList.length );
     }
 
 	/** Returns a string representation of this album */
 	public String toString()
 	{
 		StringBuilder sb = new StringBuilder();
 
 		sb.append( super.toString() );
 		sb.append("Artist: " );
         sb.append( this.artist );
         sb.append( System.lineSeparator() );
         sb.append( "Song List: " );
 		//Add songList
 		if( this.songList.length == 1 )
         {
 			sb.append( this.songList[0] );
             sb.append( System.lineSeparator() );
         }
 		else if( this.songList.length == 2 )
         {
 			sb.append( this.songList[0] + " and " + this.songList[1] + "\n" );
             sb.append( " and " );
             sb.append( this.songList[1] );
             sb.append( System.lineSeparator() );
         }
 		else
 		{
 			for( int i = 0; i < this.songList.length - 1; i++ )
             {
 				sb.append( this.songList[i]);
                 sb.append( ", " );
             }
 
 			sb.append( " and " );
             sb.append( this.songList[ this.songList.length - 1 ] );
             sb.append( System.lineSeparator() );
 		}
 
 		sb.append("Genre: " );
         sb.append( this.getGenre() );
         sb.append( System.lineSeparator() );
         sb.append( "Label: " );
         sb.append( this.getLabel() );
         sb.append( System.lineSeparator() );
         sb.append( "Year Released: " );
         sb.append( this.getYearReleased() );
         sb.append( System.lineSeparator() );
         sb.append( "Barcode Number: " );
         sb.append( super.getId() );
 
 		return sb.toString();
 	}
 	
 	public String toSaveString()
 	{
 		StringBuilder sb = new StringBuilder( "[movie]" );
         sb.append( System.lineSeparator() );
 		sb.append( this.getName());
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
 		
 		for( String s : this.songList )
         {
 			sb.append( s.trim() );
             sb.append( "," );
         }
 		
 		sb.append( System.lineSeparator() );
         sb.append( this.getArtist() );
         sb.append( System.lineSeparator() );
 		sb.append( this.getLabel() );
         sb.append( System.lineSeparator() );
 		sb.append( this.getId() );
         sb.append( System.lineSeparator() );
 		sb.append( this.getYearReleased() );
         sb.append( System.lineSeparator() );
 		
 		return sb.toString();
 	}
 }
