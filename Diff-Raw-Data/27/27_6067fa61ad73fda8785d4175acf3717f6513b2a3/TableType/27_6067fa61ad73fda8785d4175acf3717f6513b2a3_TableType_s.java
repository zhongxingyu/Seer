 package model.enums;
 
 /**
  * Represents all table in database in following configuration
  * 
  * <pre>
  * <b>enum_name : string_representation</b>
  * </pre>
  * 
  * @author kornicameister
  */
 public enum TableType{
 	AUDIO_ALBUM("audioAlbum"), AUDIO_USER("audioUser"), AUTHOR("author"), BAND(
 			"band"), BOOK("book"), BOOK_USER("bookUser"), PICTURE("picture"), GENRE(
			"genre"), MOVIE("movie"), MOVIE_USER("movieUser"), USER("user"), UNDEFINED("undefined");
 
 	private String name;
 	TableType(String s) {
 		this.name = s;
 	}	
 
 	/**
 	 * @return returns string representation of table name
 	 */
 	@Override
 	public String toString() {
 		return this.name;
 	}
 
 }
