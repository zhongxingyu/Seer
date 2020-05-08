 package library;
 
 import java.io.PrintStream;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 public class Library {
 /* Variable Members */
 	private	Map<String, Set<Item>>
 				bTitleMap, bMakerMap,
 				moTitleMap, moMakerMap, moCastMap,
 				muTitleMap, muMakerMap, muMembMap,
 				kwMap;
 	private	Set<Item>			bookS, movieS, albumS;
 	
 /* Constructors */
 	public Library() {
 		bTitleMap = new HashMap<>();
 		moTitleMap = new HashMap<>();
 		muTitleMap = new HashMap<>();
 		
 		bMakerMap = new HashMap<>();
 		moMakerMap = new HashMap<>();
 		muMakerMap = new HashMap<>();
 		
 		moCastMap = new HashMap<>();
 		muMembMap = new HashMap<>();
 		
 		kwMap = new HashMap<>();
 		
 		bookS = new TreeSet<>();
 		movieS = new TreeSet<>();
 		albumS = new TreeSet<>();
 	}
 	
 /* general methods */
 	
 	// returns all of the items which have the specified keyword
 	public Collection<Item> itemsForKeyword(String keyword) {
 		if (kwMap.containsKey(keyword)) return kwMap.get(keyword);
 		else return null;
 	}
 	
 	// print an item from this library to the output stream provided
 	public void printItem(PrintStream out, Item item) {
 		out.print(item.toString());
 	}
 	
 	private void addToMap(Map<String, Set<Item>> disMap, Item disValue, String... disKey) {
 		for(String k: disKey) {
 			if(!(disMap.containsKey(k))) {
 				Set<Item> temp = new TreeSet<>();
 				temp.add(disValue);
 				disMap.put(k, temp);
 			}
 			else {
 				Set<Item> temp = disMap.get(k);
 				temp.add(disValue);
 				disMap.put(k, temp);
 			}
 		}
 	}
 	
 	
 	
 	
 /* book-related methods */
 	
 	// adds a book to the library
 	public Item addBook(String title, String author, int nPages, String... keywords) {
 		Book disBook = new Book(title, author, nPages, keywords);
 		
 		bookS.add(disBook);
 		
 		addToMap(bTitleMap, disBook, title);
 		addToMap(bMakerMap, disBook, author);
 		addToMap(kwMap, disBook, keywords);
 		
 		
 		return disBook;
 	}
 	
 	// removes a book from the library (only needed for Premium version)
 	public boolean removeBook(String title) {
 		return false;
 	}
 	
 	// returns all of the books by the specified author
 	public Collection<Item> booksByAuthor(String author) {
 		if (bMakerMap.containsKey(author)) return bMakerMap.get(author);
 		else return null;
 	}
 	
 	// returns all of the books in the library
 	public Collection<Item> books() {
 		return bookS;
 	}
 	
 	
 	
 	
 	
 /* music-related methods */
 	
 	// adds a music album to the library
 	public Item addMusicAlbum(String title, String band, int nSongs, String... keywords) {
 		MusicAlbum disAlbum = new MusicAlbum(title, band, nSongs, keywords);
 		
 		albumS.add(disAlbum);
 		
 		addToMap(muTitleMap, disAlbum, title);
 		addToMap(muMakerMap, disAlbum, band);
 		addToMap(kwMap, disAlbum, keywords);
 		
 		// Add it to the all the stuff..... 
 		
 		return disAlbum;
 	}
 
 	// adds the specified band members to a music album
 	public void addBandMembers(Item album, String... members) {
 		((MusicAlbum)album).addMembers(members);
 		
 		for(String m: members) {
 			addToMap(muMembMap, album, m);
 		}
 	}
 	
 	// removes a music album from the library (only needed for Premium version)
 	public boolean removeMusicAlbum(String title) {
 		return false;
 	}
 
 	// returns all of the music albums by the specified band
 	public Collection<Item> musicByBand(String band) {
 		if (muMakerMap.containsKey(band)) return muMakerMap.get(band);
 		else return null;
 	}
 	
 	// returns all of the music albums by the specified musician
 	public Collection<Item> musicByMusician(String musician) {
 		if (muMembMap.containsKey(musician)) return muMembMap.get(musician);
 		else return null;
 	}
 	
 	// returns all of the music albums in the library
 	public Collection<Item> musicAlbums() {
 		return albumS;
 	}
 	
 	
 	
 	
 	
 /* movie-related methods */
 	
 	// adds a movie to the library
 	public Item addMovie(String title, String director, int nScenes, String... keywords) {
 		Movie disMovie = new Movie(title, director, nScenes, keywords);
 		
 		movieS.add(disMovie);
 		
 		addToMap(moTitleMap, disMovie, title);
 		addToMap(moMakerMap, disMovie, director);
 		addToMap(kwMap, disMovie, keywords);
 		
 		// Add it to the all the stuff..... 
 		
 		return disMovie;
 	}
 
 	// adds the specified actors to a movie
 	public void addCast(Item movie, String... members) {
 		((Movie)movie).addtoCast(members);
 		
 		for(String m: members) {
 			addToMap(moCastMap, movie, m);
 		}
 	}
 
 	// removes a movie from the library (only needed for Premium version)
 	public boolean removeMovie(String title) {
 		return false;
 	}
 	
 	// returns all of the movies by the specified director
 	public Collection<Item> moviesByDirector(String director) {
 		if (moMakerMap.containsKey(director)) return moMakerMap.get(director);
 		else return null;
 	}
 	
 	// returns all of the movies by the specified actor
 	public Collection<Item> moviesByActor(String actor) {
 		if (moCastMap.containsKey(actor)) return moCastMap.get(actor);
 		else return null;
 	}
 	
 	// returns all of the movies in the library
 	public Collection<Item> movies() {
 		return movieS;
 	}	
 }
