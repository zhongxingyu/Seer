 package tests;
 
 //local imports
 import assets.*;
 import transaction.*;
 //library imports
 import java.util.Vector;
 import java.util.zip.DataFormatException;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;
 import org.junit.Test;
 import static org.junit.Assert.assertEquals;
 
 /**
  * This TestDelete class is a JUnit testing class that exercises the constructor and different class associated methods to test 
  * and assert different scenarios and cases of delete transactions in the Nihil Ticket System
  */
 
 @RunWith(JUnit4.class)
 public class TestDelete {
	public static void main(String[] args){
	}
 
 	@BeforeClass
 	public static void testSetup() {
 	}
 
 	@AfterClass
 	public static void testCleanup() {
 	}
 
 	/**
 	 * DeleteTest 1: Test if a Delete transaction object is constructed correctly with valid username, usertype and credit field values,
 	 * Create parses and extracts lines with format "UUUUUUUUUUUUUUU TT CCCCCCCCC" (U username, T type, C credit)
 	 * @throws DataFormatException
 	 * @result Object of class Delete is constructed with the specified username, type and credit
 	 */
 	@Test 
 	public void testDelete() throws DataFormatException {
 		System.out.println("DeleteTest 1: Test if a Delete transaction object is constructed correctly with valid data...");
 		String line = "manager         AA 001000.00";				
 		Delete test = new Delete( line);
 		assertEquals( "manager", test.username);
 		assertEquals( Account.Admin, test.type);
 		assertEquals( 100000, test.credit);
 	}
 
 	/**
 	 * DeleteTest 2: Tests if Delete constructor catches (rejects) invalid DTF lines of incorrect sizes, 
 	 * line data format that is not "UUUUUUUUUUUUUUU TT CCCCCCCCC" (U username, T type, C credit)
 	 * @throws DataFormatException
 	 * @result Delete constructor catches error in line parsing, DataFormatException thrown
 	 */
 	@Test (expected = DataFormatException.class)
 	public void testDelete_InvalidLine() throws DataFormatException {
 		System.out.println("DeleteTest 2: Tests if constructor rejects catches invalid DTF lines of incorrect sizes...");
 		String line = "invalid DTF line"; 
 		Delete test = new Delete( line);
 	}
 
 	/**
 	 * DeleteTest 3: Test if a Delete transaction rejects object construction of invalid username field values (i.e. special characters)
 	 * @throws DataFormatException
 	 * @result Delete constructor catches invalid username character error in line parsing, DataFormatException is thrown
 	 */
 	@Test (expected = DataFormatException.class)
 	public void testDelete_InvalidUsername() throws DataFormatException {
 		System.out.println("DeleteTest 3: Test if a Delete transaction rejects object construction of invalid username field values...");
 		String line = "m@nag&r         AA 001000.00";
 		Delete test = new Delete( line);
 	}
 
 	/**
 	 * DeleteTest 4: Test if a Delete transaction rejects object construction if username is too large (username exceeds 15 character size limit)
 	 * @throws DataFormatException
 	 * @result Delete constructor extracted username substring is too long, catches username error in line parsing, DataFormatException thrown
 	 */
 	@Test (expected = DataFormatException.class)
 	public void testDelete_UsernameTooLarge() throws DataFormatException {
 		System.out.println("DeleteTest 4: Test if a Delete transaction rejects object construction if username is too large (username exceeds 15 character size limit)...");
 		String line = "username1111222344455555longggggg         AA 001000.00";
 		Delete test = new Delete( line);
 	}
 
 	/**
 	 * DeleteTest 5: Test if a Delete constructor while line parsing catches invalid type values (i.e. values that are not AA, BS, SS, FS) 
 	 * @throws DataFormatException
 	 * @result Delete constructor catches invalid type value 'ZZ' after extracting substring, DataFormatException is thrown
 	 */
 	@Test (expected = DataFormatException.class)
 	public void testDelete_InvalidUserType() throws DataFormatException {
 		System.out.println("DeleteTest 5: Test if a Delete transaction rejects object construction of invalid usertype field values...");
 		String line = "manager         ZZ 001000.00";
 		Delete test = new Delete( line);
 	}
 
 	/**
 	 * DeleteTest 6: Test if a Delete transaction rejects object construction if credit amount has invalid characters (not numeric)
 	 * @throws DataFormatException
 	 * @result Delete constructor catches credit error with invalid amount value (because of invalid characters) from parsing, DataFormatException is thrown
 	 */
 	@Test (expected = DataFormatException.class)
 	public void testDelete_InvalidCredit() throws DataFormatException {
 		System.out.println("DeleteTest 6: Test if a Delete transaction rejects object construction if credit amount has invalid characters (not numeric)...");
 		String line = "manager         AA invalid(.0";
 		Delete test = new Delete( line);
 	}
 
 	/**
 	 * DeleteTest 7: Tests constructor's ability to catch credit amount values in DTFLine that have two or more invalid decimal places
 	 * @throws DataFormatException
 	 * @result Constructor catches erroneous credit amount, DataFormatException thrown
 	 */
 	@Test (expected = DataFormatException.class)
 	public void testDelete_InvalidCreditDecimal() throws DataFormatException {
 		System.out.println("DeleteTest 7: Tests constructor's ability to catch credit amount values in DTFLine that have two or more invalid decimal places...");
 		String line = "manager         AA 00.10.00.00";
 		Delete test = new Delete( line);
 	}
 
 	/**
 	 * DeleteTest 8: Tests constructor's ability to catch and reject credit amount values less than zero or have invalid (minus,-) characters
 	 * @throws DataFormatException
 	 * @result Constructor catches erroneous credit amount in parsing, DataFormatException thrown
 	 */
 	@Test (expected = DataFormatException.class)
 	public void testDelete_NegativeInvalidCredit() throws DataFormatException {
 		System.out.println("DeleteTest 8: Tests constructor's ability to catch and reject credit amount values less than zero or have invalid (i.e.minus,-) characters...");
		String line = "manager         AA -00099.99";
 		Delete test = new Delete( line);
 	}
 
 	/**
 	 * DeleteTest 9: Tests constructor's ability to catch credit amount values that are exceed the maximum size limit
 	 * @throws DataFormatException
 	 * @result Constructor catches erroneous credit amount, DataFormatException thrown
 	 */
 	@Test (expected = DataFormatException.class)
 	public void testDelete_CreditTooLarge() throws DataFormatException {
 		System.out.println("DeleteTest 9: Tests constructor's ability to catch credit amount values that are exceed the maximum size limit...");
 		String line = "manager         AA 9999999.99";
 		Delete test = new Delete( line);
 	}
 
 	/**
 	 * DeleteTest 10: Test the applyTo() method, test if a Delete transaction is applied correctly to delete a specified user account
 	 * @throws DataFormatException, TransactionException
 	 * @result The delete transaction object is constructed (with specified username, type, credit fields), and
 	 * TestAccounts contains account with matching username, hence applyTo() deletes the specified account 
 	 */
 	@Test
 	public void testApplyTo() throws DataFormatException, TransactionException{
 		Vector<Account> TestAccounts = new Vector<Account>();
 		Vector<Ticket> TestTickets = new Vector<Ticket>();
 
 		//NOTE: initial test account 'user123' added to accounts vector to test create transaction
 		TestAccounts.add(new Account( "user123", Account.Sell, 500));
 				
 		System.out.println("DeleteTest 10: Test applyTo(), test if a Delete transaction is applied correctly to delete specified user account...");
 		Delete test = new Delete("user123         SS 000500.00");
 		//TestAccounts contains account with matching username, hence 'user123' account is deleted after Delete transaction is applied
 		test.applyTo(TestAccounts, TestTickets);
 	}
 
 	/**
 	 * DeleteTest 11: Test the applyTo() method, test the delete transaction to delete a specified user account when the username and account do not exist (not found) in the accounts vector
 	 * @throws DataFormatException, TransactionException
 	 * @result The delete transaction object is constructed (with valid account username, type, credit) to delete a specified user,
 	 * but TestAccounts vector is empty (account/username non-existent), hence applyTo catches the error and throws TransactionException
 	 */
 	@Test(expected = TransactionException.class)
 	public void testApplyTo_UserNonExistent() throws DataFormatException, TransactionException{
 		Vector<Account> TestAccounts = new Vector<Account>();
 		Vector<Ticket> TestTickets = new Vector<Ticket>();
 
 		System.out.println("DeleteTest 11: Test the applyTo() method, test the delete transaction to delete a specified user account when the username and account do not exist (not found) in the accounts vector");
 		Delete test = new Delete("newuser         SS 000500.00");
 		//TestAccounts will be empty, thus 'newuser' does not exist
 		test.applyTo(TestAccounts, TestTickets);
 	}
 }
