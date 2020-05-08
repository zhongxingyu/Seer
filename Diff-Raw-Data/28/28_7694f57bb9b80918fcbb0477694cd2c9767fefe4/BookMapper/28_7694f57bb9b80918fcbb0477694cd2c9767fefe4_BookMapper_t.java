 package data;
 
 import static main.Constants.BOOK_RETRIEVE;
 import static main.Constants.BOOK_UPDATE;
 import static main.Constants.BOOK_INSERT;
 import static main.Constants.BOOK_DELETE;
 
 import static main.Constants.BOOK_ID;
 import static main.Constants.BOOK_PRICE;
 import static main.Constants.AUTHOR_ID;
 import static main.Constants.FORMAT_ID;
 import static main.Constants.GENRE_ID;
 import static main.Constants.PUBLISHER_ID;
 import static main.Constants.BOOK_TITLE;
 
 
 import java.sql.SQLException;
 
 import domain.AbstractDomainClass;
 import domain.Author;
 import domain.Book;
 import domain.Format;
 import domain.Genre;
 import domain.Publisher;
 
 public class BookMapper extends AbstractDataMapper {
 
 	public Book retrieve(int bookID) throws SQLException, ClassNotFoundException{
 		start();
 		setRetrieveString(BOOK_RETRIEVE);
 		setRetrieveString(getRetrieveString().replaceAll("::id", "" + bookID));
 		executeToResultSet(getRetrieveString());
 		return (Book) placeAndReturn();
 	}
 	
 	public void update(Book book) throws SQLException, ClassNotFoundException{
 		start();
 		setUpdateString(BOOK_UPDATE);
 		loadUpdateString(book);
 		execute(getUpdateString());
 	}
 	
	public Boolean insert(Book book) throws SQLException, ClassNotFoundException{
 		start();
 		setInsertString(BOOK_INSERT);
 		loadInsertString(book);
 		System.out.println(getInsertString());
 		executeWithKey(book);
		return true;
 	}
 	
 	public void delete(Book book) throws SQLException, ClassNotFoundException{
 		start();
 		setDeleteString(BOOK_DELETE);
 		loadDeleteString(book);
 		execute(getDeleteString());
 		
 	}
 	
 	@Override
 	AbstractDomainClass placeAndReturn() throws SQLException, ClassNotFoundException {
 		if (getResult().next()){
 			AuthorMapper authMap = new AuthorMapper();
 			Author auth = authMap.retrieve(getResult().getInt(AUTHOR_ID));
 			PublisherMapper pubMap = new PublisherMapper();
 			Publisher pub = pubMap.retrieve(getResult().getShort(PUBLISHER_ID));
 			GenreMapper genMap = new GenreMapper();
 			Genre gen = genMap.retrieve(getResult().getInt(GENRE_ID));
 			FormatMapper forMap = new FormatMapper();
 			Format form = forMap.retrieve(getResult().getInt(FORMAT_ID));
 			
 			
 			Book book = new Book(getResult().getInt(BOOK_ID), 
 					(getResult().getString(BOOK_TITLE)),
 					auth.getName(), 
 					auth.getLastName(), 
 					gen.getName(), 
 					pub.getName(), 
 					form.getName(), 
 					getResult().getInt(BOOK_PRICE));
 			return book;
 		}else{
 			return null;
 		}
 	}
 
 	@Override
 	void loadUpdateString(AbstractDomainClass d) throws SQLException, ClassNotFoundException {
 		Book book = (Book) d;
 		AuthorMapper authMap = new AuthorMapper();
 		PublisherMapper pubMap = new PublisherMapper();
 		GenreMapper genMap = new GenreMapper();
 		FormatMapper forMap = new FormatMapper();
 		
 		setUpdateString(getUpdateString().replaceAll("::title", "" + d.getName()));
 		setUpdateString(getUpdateString().replaceAll("::authorID", "" + authMap.getIDbyFirstName(d.getName())));
 		setUpdateString(getUpdateString().replaceAll("::publisherID", "" + pubMap.getIDbyName(book.getPublisher())));
 		setUpdateString(getUpdateString().replaceAll("::genreID", "" + genMap.getIDbyName(book.getGenre())));
 		setUpdateString(getUpdateString().replaceAll("::formatID", "" + forMap.getIDbyName(book.getFormat())));
 		setUpdateString(getUpdateString().replaceAll("::price", "" + book.getPrice()));
 	}
 
 	@Override
 	void loadDeleteString(AbstractDomainClass d) throws SQLException, ClassNotFoundException {
 		setDeleteString(getDeleteString().replaceAll("::id", "" + d.getID()));
 		
 	}
 
 	@Override
 	void loadInsertString(AbstractDomainClass d) throws SQLException, ClassNotFoundException {
 		Book book = (Book) d;
 		
 		AuthorMapper authMap = new AuthorMapper();
 		PublisherMapper pubMap = new PublisherMapper();
 		GenreMapper genMap = new GenreMapper();
 		FormatMapper forMap = new FormatMapper();
 		
 		int authID = authMap.getIDbyFirstName(book.getAuthor());
 		int pubID = pubMap.getIDbyName(book.getPublisher());
 		int genID = genMap.getIDbyName(book.getGenre());
 		int forID = forMap.getIDbyName(book.getFormat());
 		
 		System.out.println("authmap : " + authID);
 		System.out.println("pumap : " + pubID);
 		System.out.println("genmap : " + genID);
 		System.out.println("formap : " + forID);
 		
 		setInsertString(getInsertString().replaceAll("::ISBN", "" + book.getID()));
 		setInsertString(getInsertString().replaceAll("::title", "" + book.getName()));
 		setInsertString(getInsertString().replaceAll("::price", "" + book.getPrice()));
 		
 		if (authID != -1){
 			setInsertString(getInsertString().replaceAll("::authorID", "" + authID));
 		}else {
 			Author auth = new Author(book.getAuthor(), book.getLastName());
 			authMap.insert(auth);
 			setInsertString(getInsertString().replaceAll("::authorID", "" + auth.getID()));
 		}
 		
 		if (pubID != -1){
 			setInsertString(getInsertString().replaceAll("::publisherID", "" + pubID));
 		}else {
 			Publisher pub = new Publisher(book.getPublisher());
 			pubMap.insert(pub);
 			setInsertString(getInsertString().replaceAll("::publisherID", "" + pub.getID()));
 		}
 		
 		if (genID != -1){
 			setInsertString(getInsertString().replaceAll("::genreID", "" + genID));
 		}else {
 			Genre gen = new Genre(book.getGenre());
 			genMap.insert(gen);
 			setInsertString(getInsertString().replaceAll("::genreID", "" + gen.getID()));
 		}
 		
 		if (forID != -1){
 			setInsertString(getInsertString().replaceAll("::formatID", "" + forID));
 		}else {
 			Format form = new Format(book.getFormat());
 			forMap.insert(form);
 			setInsertString(getInsertString().replaceAll("::formatID", "" + form.getID()));
 		}
 	}
 	
 	
 
 }
