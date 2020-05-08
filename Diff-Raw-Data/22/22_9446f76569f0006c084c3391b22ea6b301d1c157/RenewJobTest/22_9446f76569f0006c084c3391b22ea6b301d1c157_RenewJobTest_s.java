 /**
     This file is part of Bibbla.
 
     Bibbla is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     Bibbla is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Bibbla.  If not, see <http://www.gnu.org/licenses/>.    
  **/
 
 package dat255.grupp06.bibbla.backend.tasks;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import junit.framework.TestCase;
 import dat255.grupp06.bibbla.CredentialsFactory;
 import dat255.grupp06.bibbla.model.Book;
 import dat255.grupp06.bibbla.model.Credentials;
 import dat255.grupp06.bibbla.utils.Message;
 import dat255.grupp06.bibbla.utils.Session;
 
 /**
  * Tests RenewJob by running four tests:
  * 
  *   1. Renew the first book.
  *   2. Try to renew the first book twice (second time should fail)
  *   3. Renews the first and second book.
  *   4. Renews all books.
  *   
  * Checks return values, list sizes, object types etc.
  * 
  * @bypassed if no login details, or not enough loaned books.
  * @depends on MyBooksJob.
  * @author Niklas Logren
  */
 public class RenewJobTest extends TestCase {
 	Credentials credentials;
 	Session session;
 	List<Book> loanedBooks;
 	
 	@Override
 	public void setUp() {
 		// Get a new Credentials object.
 		credentials = CredentialsFactory.getCredentials();
 		// Create a session using these credentials.
 		if(credentials != null){
 		session = new Session(credentials);
 		}
 		// If we have login details,
 		if (credentials != null) {
 			// Get our currently loaned books.
 			MyBooksJob myBooksJob = new MyBooksJob(credentials, session);
 			loanedBooks = (List<Book>)myBooksJob.run().obj;
 			// Assert that MyBooksJob succeeded.
 			assertTrue(loanedBooks instanceof List<?>);
 		}
 	}
 	
 	/**
 	 * Tries to renew the first loaned book.
 	 * Verifies the return value in various ways.
 	 */
 	public void testSingleRenewal() {
 
 		// Run test only if we have login credentials. Otherwise, auto-succeed.
 		if (credentials != null) {
 			
 			// If no loaned books, auto-succeed.
 			int loanedBooksCount =loanedBooks.size(); 
 			if (loanedBooksCount>0) {
 				
 				// Select first book of our loaned books.
 				Book firstBook = loanedBooks.get(0);
 				// Run RenewJob.
 				RenewJob renewJob = new RenewJob(firstBook, credentials,
 						session);
 				Message result = renewJob.run();
 				// Assert that the job at least returned a result.
 				assertNotNull(result);
 				
 				// Result should contain List.
 				assertTrue(result.obj instanceof List<?>);
 				
 				// Get the list of books from the result.
 				List<Book> resultBooks = (List<Book>) result.obj;
 				assertNotNull(resultBooks);
 	
 				// The number of loaned books shouldn't have changed.
 				assertEquals(loanedBooksCount, resultBooks.size());
 	
 				// If the first book has an error set,
 				if (resultBooks.get(0).getError()) {
 					// The result should have an error,
 					assertNotNull(result.error);
 					// and the book should have a message.
 					assertNotNull(resultBooks.get(0).getMessage());
 				}
 				
 				// And vice versa: if the result has an error set, 
 				if (result.error != null) {
 					// the book needs to have its error set as well.
 					assertTrue(resultBooks.get(0).getError());
 				}
 				
 			}
 		}
 	}
 
 	
 	/**
 	 * Tries to renew the first loaned book twice -
 	 * second try should result in error.
 	 * 
 	 * Verifies the return values in various ways.
 	 */
 	public void testSingleRenewalTwice() {
 
 		// Run test only if we have login credentials. Otherwise, auto-succeed.
 		if (credentials != null) {
 			
 			// If no loaned books, auto-succeed.
 			int loanedBooksCount =loanedBooks.size(); 
 			if (loanedBooksCount>0) {
 				
 				// Select first book of our loaned books.
 				Book firstBook = loanedBooks.get(0);
 				
 				// Run RenewJob.
 				RenewJob firstRenewJob = new RenewJob(firstBook, credentials,
 						session);
 				Message firstResult = firstRenewJob.run();
 				// Assert that the job at least returned a result.
 				assertNotNull(firstResult);
 				
 				// Run RenewJob again, on the same book.
 				RenewJob secondRenewJob = new RenewJob(firstBook, credentials,
 						session);
 				Message secondResult = secondRenewJob.run();
 				// Assert that the job at least returned a result.
 				assertNotNull(secondResult);
 				
 				// Both results should contain Lists.
 				assertTrue(firstResult.obj instanceof List<?>);
 				assertTrue(secondResult.obj instanceof List<?>);
 				
 				// Get the list of books from each result.
 				List<Book> firstResultBooks = (List<Book>) firstResult.obj;
 				List<Book> secondResultBooks = (List<Book>) secondResult.obj;
 				// Both should contain books.
 				assertNotNull(firstResultBooks);
 				assertNotNull(secondResultBooks);
 	
 				// The number of loaned books shouldn't have changed.
 				assertEquals(loanedBooksCount, firstResultBooks.size());
 				assertEquals(loanedBooksCount, secondResultBooks.size());
 				
 				// First run: if the book has an error set,
 				if (firstResultBooks.get(0).getError()) {
 					// The first result should have an error,
 					assertNotNull(firstResult.error);
 					// and the book should have a message.
 					assertNotNull(firstResultBooks.get(0).getMessage());
 				}
 				
 				// And vice versa: if the first result has an error set, 
 				if (firstResult.error != null) {
 					// the book needs to have its error set as well.
 					assertTrue(firstResultBooks.get(0).getError());
 				}
				
				// Second run: Everything should have failed.
				assertTrue(secondResultBooks.get(0).getError());
				assertNotNull(secondResultBooks.get(0).getMessage());
				assertNotNull(secondResult.error);
 			}
 		}
 	}
 	
 	
 	/**
 	 * Tries to renew two loaned books.
 	 * Verifies the return values in various ways.
 	 */
 	public void testDoubleRenewal() {
 
 		// Run test only if we have login credentials. Otherwise, auto-succeed.
 		if (credentials != null) {
 			
 			// If less than two loaned books, auto-succeed.
 			int loanedBooksCount =loanedBooks.size();
 			if (loanedBooksCount >= 2) {
 				
 				// Select the first and second book of our loaned books.
 				Book firstBook = loanedBooks.get(0);
 				Book secondBook = loanedBooks.get(1);
 				
 				// Add them to a list.
 				List<Book> booksToRenew = new ArrayList<Book>();
 				booksToRenew.add(firstBook);
 				booksToRenew.add(secondBook);
 				
 				// Run RenewJob on the list.
 				RenewJob renewJob = new RenewJob(booksToRenew, credentials,
 						session);
 				Message result = renewJob.run();
 				// Assert that the job at least returned a result.
 				assertNotNull(result);
 				
 				// Result should contain List.
 				assertTrue(result.obj instanceof List<?>);
 				
 				// Get the list of books from the result.
 				List<Book> resultBooks = (List<Book>) result.obj;
 				assertNotNull(resultBooks);
 	
 				// The number of loaned books shouldn't have changed.
 				assertEquals(loanedBooksCount, resultBooks.size());
 	
 				// Loop through all books in result.
 				boolean foundError = false;
 				for (Book book : resultBooks) {
 					// If any failed,
 					if (book.getError()) {
 						foundError = true;
 						// verify that a message is present.
 						assertNotNull(book.getMessage());
 					}
 				}
 				
 				// If any book failed, the result should have an error set. 
 				assertEquals(foundError, (result.error != null));
 			}
 		}
 	}
 	
 	
 	/**
 	 * Tries to renew all loaned books.
 	 * Verifies the return values in various ways.
 	 */
 	public void testRenewAll() {
 
 		// Run test only if we have login credentials. Otherwise, auto-succeed.
 		if (credentials != null) {
 			
 			// If no loaned books, auto-succeed.
 			int loanedBooksCount = loanedBooks.size();
 			if (loanedBooksCount>0) {
 				
 				// Run RenewJob for all books.
 				RenewJob renewJob = new RenewJob(credentials, session);
 				Message result = renewJob.run();
 				// Assert that the job at least returned a result.
 				assertNotNull(result);
 				
 				// Result should contain List.
 				assertTrue(result.obj instanceof List<?>);
 				
 				// Get the list of books from the result.
 				List<Book> resultBooks = (List<Book>) result.obj;
 				assertNotNull(resultBooks);
 	
 				// The number of loaned books shouldn't have changed.
 				assertEquals(loanedBooksCount, resultBooks.size());
 	
 				// Loop through all books in result.
 				boolean foundError = false;
 				for (Book book : resultBooks) {
 					// If any failed,
 					if (book.getError()) {
 						foundError = true;
 						// verify that a message is present.
 						assertNotNull(book.getMessage());
 					}
 				}
 				
 				// If any book failed, the result should have an error set. 
 				assertEquals(foundError, (result.error != null));
 			}
 		}
 	}
 }
