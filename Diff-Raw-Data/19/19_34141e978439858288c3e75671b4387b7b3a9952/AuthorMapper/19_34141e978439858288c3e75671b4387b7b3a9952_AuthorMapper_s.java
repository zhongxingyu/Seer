 package data;
 
 import java.sql.SQLException;
 
 import domain.AbstractDomainClass;
 import domain.Author;
 import static main.Constants.AUTHOR_RETRIEVE;
 import static main.Constants.AUTHOR_RETRIEVE_BY_FIRSTNAME;
 import static main.Constants.AUTHOR_RETRIEVE_BY_LASTNAME;
 import static main.Constants.AUTHOR_ID;
 import static main.Constants.AUTHOR_FIRSTNAME;
 import static main.Constants.AUTHOR_LASTNAME;
 import static main.Constants.AUTHOR_UPDATE;
 import static main.Constants.AUTHOR_INSERT;
 import static main.Constants.AUTHOR_DELETE;
 
 /**
  * DataMapper pattern for the {@link domain.Author} class. Inherits common
  * functionality from {@link data.AbstractDataMapper} and overrides remaining
  * functions with table-specific methods.
  * 
  * NOT tested.
  * @author �sgeir Davidsson
  *
  */
 public class AuthorMapper extends AbstractDataMapper {
 	
 	public AuthorMapper(){
 	}
 	
 	public Author retrieve(int authID) throws SQLException, ClassNotFoundException{
 		start();
 		setRetrieveString(AUTHOR_RETRIEVE);
 		setRetrieveString(getRetrieveString().replaceAll("::id", "" + authID));
 		executeToResultSet(getRetrieveString());
 		return (Author) placeAndReturn();
 	}
 	
 	public int getIDbyFirstName(String name) throws SQLException, ClassNotFoundException{
 		start();
 		setRetrieveString(AUTHOR_RETRIEVE_BY_FIRSTNAME);
 		setRetrieveString(getRetrieveString().replaceAll("::firstname", "" + name));
 		executeToResultSet(getRetrieveString());
 		if (getResult().next()){
 			return getResult().getInt(AUTHOR_ID);	
 		}else {
 			return -1;
 		}
 		
 	}
 	
 	public int getIDbyLastName(String name) throws SQLException, ClassNotFoundException{
 		start();
 		setRetrieveString(AUTHOR_RETRIEVE_BY_LASTNAME);
 		setRetrieveString(getRetrieveString().replaceAll("::firstname", "" + name));
 		executeToResultSet(getRetrieveString());
 		if (getResult().next()){
 			return getResult().getInt(AUTHOR_ID);
 		}else {
 			return -1; 
 		}
 	}
 	
 	public void update(Author auth) throws SQLException, ClassNotFoundException{
 		start();
 		setUpdateString(AUTHOR_UPDATE);
 		loadUpdateString(auth);
 		execute(getUpdateString());
 	}
 	
 	public Author insert(Author auth) throws SQLException, ClassNotFoundException{
 		start();
 		setInsertString(AUTHOR_INSERT);
 		loadInsertString(auth);
 		executeWithKey(auth);
 		return auth;
 	}
 	
 	public void delete(Author auth) throws SQLException, ClassNotFoundException{
 		start();
 		setDeleteString(AUTHOR_DELETE);
 		loadDeleteString(auth);
 		execute(getDeleteString());
 		
 	}
 	
 	@Override
 	AbstractDomainClass placeAndReturn() throws SQLException, ClassNotFoundException {
 		if (getResult().next()){
 			Author author = new Author(getResult().getInt(AUTHOR_ID),
 					(getResult().getString(AUTHOR_FIRSTNAME)),
 					(getResult().getString(AUTHOR_LASTNAME)));
 			return author;
 		}else{
 			return null;
 		}
 		
 	}
 
 	@Override
 	void loadUpdateString(AbstractDomainClass d) throws SQLException, ClassNotFoundException {
 		setUpdateString(getUpdateString().replaceAll("::id", "" + d.getID()));
 		setUpdateString(getUpdateString().replaceAll("::firstname", "" + d.getName()));
 		setUpdateString(getUpdateString().replaceAll("::lastname", "" + d.getLastName()));
 		
 	}
 
 	@Override
 	void loadDeleteString(AbstractDomainClass d) throws SQLException, ClassNotFoundException {
 		setDeleteString(getDeleteString().replaceAll("::id", "" + d.getID()));	
 	}
 
 
 	@Override
 	void loadInsertString(AbstractDomainClass d) throws SQLException, ClassNotFoundException {
 		setInsertString(getInsertString().replaceAll("::firstname", "" + d.getName()));
 		setInsertString(getInsertString().replaceAll("::lastname", "" + d.getLastName()));
 		
 	}
 
 }
