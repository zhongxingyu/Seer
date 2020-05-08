 package test;
 
 import static org.junit.Assert.*;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import tables.Book;
 
 public class tablesTest {
 	
 	Book b;
 
 	@Before
 	public void setup(){
 		
 	//clear database
 		
 	//create a book object
 	String cN = "123456789";
 	String isBun = "123412341234ABCD";
 	String titleArg = "The Most Amazing Book";
 	String mainAuthorArg = "The Most Amazing Author";
 	String pub = "the most super publisher";
 	int yr= 2012;
 	ArrayList<String> authrs = new ArrayList<String>();
 	authrs.add("someone boring");
 	authrs.add("an even more boring person");
 	
 	ArrayList<String> subjectsArg = new ArrayList<String>();
 	subjectsArg.add("astrobiology");
 	subjectsArg.add("teutonicEpidemiology");
 	
 	b= new Book(cN,isBun,titleArg,mainAuthorArg,pub,yr,authrs,subjectsArg);
 	
 	
 	//add rest of tables to database
 	}
 	 
 
 	@Test
 	public void testBookInsert() throws SQLException {
 		assertTrue(b.insert());
 	}
 
 }
