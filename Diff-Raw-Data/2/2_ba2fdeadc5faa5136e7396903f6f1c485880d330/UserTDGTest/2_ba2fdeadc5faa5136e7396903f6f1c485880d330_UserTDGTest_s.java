 /**
  * SOEN 490
  * Capstone 2011
  * Test for UserTDG.
  * Team members: 	Sotirios Delimanolis
  * 			Filipe Martinho
  * 			Adam Harrison
  * 			Vahe Chahinian
  * 			Ben Crudo
  * 			Anthony Boyer
  * 
  * @author Capstone 490 Team Moving Target
  *
  */
 
 package tests.foundation;
 
 import java.io.IOException;
 import java.math.BigInteger;
 import java.sql.ResultSet;
 import static org.junit.Assert.*;
 import java.sql.SQLException;
 
 import org.junit.Test;
 
 import foundation.Database;
 import foundation.UserFinder;
 import foundation.UserTDG;
 
 public class UserTDGTest {
 	
 	static BigInteger uid = new BigInteger("158749857934");
 	
 	@Test
 	public void testFunctionality() throws SQLException, IOException
 	{
 		// We put this in here, so that the tests don't disturb the database if it's already present.
 		boolean previousDatabase = Database.isDatabaseCreated();
 		if (!previousDatabase)
 			Database.createDatabase();
 		insert();
 		update();
 		delete();
 		if (!previousDatabase)
 			Database.dropDatabase();
 	}
 	
 	private void insert() throws SQLException, IOException
 	{
 		final String email = "example@example.com";
 		final String password = "password";
 		final int type = 1;
 		final int version = 1;
 		assertFalse(UserFinder.find(uid).next());
 		assertEquals(UserTDG.insert(uid, version, email, password, type), 1);
 		ResultSet rs = UserFinder.find(uid);
 		assertTrue(rs.next());
 		assertEquals(rs.getBigDecimal("u.uid").toBigInteger(), uid);
 		assertEquals(rs.getString("u.email"), email);
 		assertEquals(rs.getString("u.password"), password);
 		assertEquals(rs.getInt("u.version"), version);
 		assertEquals(rs.getInt("u.type"), type);
 	}
 	
 	private void update() throws SQLException, IOException
 	{
 		final String email = "example2@example.com";
 		final String password = "password2";
 		final int type = 0;
 		final int version = 1;
 		assertTrue(UserFinder.find(uid).next());
 		assertEquals(1, UserTDG.update(uid, version, email, password, type));
 		ResultSet rs = UserFinder.find(uid);
 		assertTrue(rs.next());
		assertEquals(rs.getLong("u.uid"), uid);
 		assertEquals(rs.getString("u.email"), email);
 		assertEquals(rs.getString("u.password"), password);
 		assertEquals(rs.getInt("u.version"), version+1);
 		assertEquals(rs.getInt("u.type"), type);
 	}
 	
 	private void delete() throws SQLException, IOException
 	{
 		final int version = 2;
 		assertTrue(UserFinder.find(uid).next());
 		assertEquals(UserTDG.delete(uid, version), 1);
 		assertFalse(UserFinder.find(uid).next());
 	}
 }
