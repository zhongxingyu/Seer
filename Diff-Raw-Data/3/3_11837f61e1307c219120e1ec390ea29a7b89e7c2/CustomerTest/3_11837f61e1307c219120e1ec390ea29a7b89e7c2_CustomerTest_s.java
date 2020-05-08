 package com.refactoring.first;
 
 import java.util.Properties;
 
 import com.refactoring.first.structures.ChildrensPrice;
 import com.refactoring.first.structures.NewReleasesPrice;
 import com.refactoring.first.structures.RegularPrice;
 import com.refactoring.first.utils.FileAccess;
 
 import junit.framework.TestCase;
 
 public class CustomerTest extends TestCase {
 
 	protected void setUp() throws Exception {
 		super.setUp();
 	}
 
 	protected void tearDown() throws Exception {
 		super.tearDown();
 	}
 
 	public void testCustomer() {
 		Customer c = new Customer("David");
 		assertNotNull(c);	
 	}
 
 	public void testAddRental() {
 		Customer customer2 = new Customer("Sallie");
 		Movie movie1 = new Movie("Gone with the Wind", Movie.REGULAR);
 		Rental rental1 = new Rental(movie1, 3); // 3 day rental
 		customer2.addRental(rental1);
 	}
 
 	public void testGetName() {
 		Customer c = new Customer("David");
 		assertEquals("David", c.getName());
 	}
 
 	public void testStatementForRegularMovie() {
 		Customer customer2 = new Customer("Sallie");
 		Movie movie1 = new Movie("Gone with the Wind", Movie.REGULAR);
 		Rental rental1 = new Rental(movie1, 3); // 3 day rental
 		customer2.addRental(rental1);
 		String expected = "Rental Record for Sallie\n" +
 							"\tGone with the Wind\t3.5\n" +
 							"Amount owed is 3.5\n" +
 							"You earned 1 frequent renter points";
 		String statement = customer2.statement();
 		assertEquals(expected, statement);
 	}
 	
 	public void testStatementForNewReleaseMovieWith3DayRental() {
 		Customer customer2 = new Customer("Sallie");
 		Movie movie1 = new Movie("Star Wars", Movie.NEW_RELEASE);
 		Rental rental1 = new Rental(movie1, 3); // 3 day rental
 		customer2.addRental(rental1);
 		String expected = "Rental Record for Sallie\n" +
 							"\tStar Wars\t9.0\n" +
 							"Amount owed is 9.0\n" +
 							"You earned 2 frequent renter points";
 		String statement = customer2.statement();
 		assertEquals(expected, statement);
 	}
 
 	public void testStatementForNewReleaseMovieWith1DayRental() {
 		Customer customer2 = new Customer("Sallie");
 		Movie movie1 = new Movie("Star Wars", Movie.NEW_RELEASE);
 		Rental rental1 = new Rental(movie1, 1); // 3 day rental
 		customer2.addRental(rental1);
 		String expected = "Rental Record for Sallie\n" +
 							"\tStar Wars\t3.0\n" +
 							"Amount owed is 3.0\n" +
 							"You earned 1 frequent renter points";
 		String statement = customer2.statement();
 		assertEquals(expected, statement);
 	}
 		
 	public void testStatementForChildrensMovie() {
 		Customer customer2 = new Customer("Sallie");
 		Movie movie1 = new Movie("Madagascar", Movie.CHILDRENS);
 		Rental rental1 = new Rental(movie1, 3); // 3 day rental
 		customer2.addRental(rental1);
 		String expected = "Rental Record for Sallie\n" +
 							"\tMadagascar\t1.5\n" +
 							"Amount owed is 1.5\n" +
 							"You earned 1 frequent renter points";
 		String statement = customer2.statement();
 		assertEquals(expected, statement);
 	}
 	
 	public void testStatementForManyMovies() {
 		Customer customer1 = new Customer("David");
 		Movie movie1 = new Movie("Madagascar", Movie.CHILDRENS);
 		Rental rental1 = new Rental(movie1, 6); // 6 day rental
 		Movie movie2 = new Movie("Star Wars", Movie.NEW_RELEASE);
 		Rental rental2 = new Rental(movie2, 2); // 2 day rental
 		Movie movie3 = new Movie("Gone with the Wind", Movie.REGULAR);
 		Rental rental3 = new Rental(movie3, 8); // 8 day rental
 		customer1.addRental(rental1);
 		customer1.addRental(rental2);
 		customer1.addRental(rental3);
 		String expected = "Rental Record for David\n" +
 							"\tMadagascar\t6.0\n" +
 							"\tStar Wars\t6.0\n" +
 							"\tGone with the Wind\t11.0\n" +
 							"Amount owed is 23.0\n" +
 							"You earned 4 frequent renter points";
 		String statement = customer1.statement();
 		assertEquals(expected, statement);
 	}
 	
 	// new tests for in code.
 	public void testChildrensPriceCode(){
 		ChildrensPrice childrensPrice = new ChildrensPrice();
 		assertEquals(Movie.CHILDRENS, childrensPrice.getPriceCode());
 	}
 
 	public void testRegularPriceCode(){
 		RegularPrice regularPrice = new RegularPrice();
 		assertEquals(Movie.REGULAR, regularPrice.getPriceCode());
 	}
 	
 	public void testNewReleasesPriceCode(){
 		NewReleasesPrice newReleasesPrice = new NewReleasesPrice();
 		assertEquals(Movie.NEW_RELEASE, newReleasesPrice.getPriceCode());
 	}
 	
 	public void testRentalCode(){
 		Movie movie1 = new Movie("Madagascar", Movie.CHILDRENS);
 		Rental rental1 = new Rental(movie1, 3); // 3 day rental
 		assertEquals(3, rental1.getDaysRented());
 	}
 
 	public void testMovieCode(){
 		Movie movie1 = new Movie("Madagascar", Movie.CHILDRENS);
 		assertEquals(Movie.CHILDRENS, movie1.getPriceCode());
 	}
 	
 	public void testHtmlStatementForRegularMovie() {
 		Customer customer2 = new Customer("Sallie");
 		Movie movie1 = new Movie("Gone with the Wind", Movie.REGULAR);
 		Rental rental1 = new Rental(movie1, 3); // 3 day rental
 		customer2.addRental(rental1);
 		String expected = "<H1>Rental Record for <EM>Sallie</EM></H1><P>" +
 							"Gone with the Wind 3.5 <BR><P>" +
 							"You owe 3.5 on this rental<BR> you earned <EM>1</EM> frequent renter points";
 		
 		String htmlStatement = customer2.htmlStatement();
 		assertEquals(expected, htmlStatement);
 	}
 	
 	public void testGet(){
 		Properties prop = FileAccess.getValue();
 		assertEquals("1.5", prop.getProperty("oneAndHalf"));
 	}
 
 	public void testForException(){
 		try{
 		Movie movie1 = new Movie("Madagascar", 99);
 		} catch(IllegalArgumentException e){
 			assert(true);
 		}
		assert(false);
 	}
 }
