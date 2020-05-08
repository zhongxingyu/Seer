 package tests.jfmi.dao;
 
 import java.sql.SQLException;
 
 import org.junit.Test;
 import org.junit.Before;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
import jfmi.app.TaggedFile;
 import jfmi.dao.TaggedFileDAO;
 import jfmi.repo.SQLiteRepository;
 
 /** Implements unit tests for the TaggedFileDAO class.
   */
 public class TaggedFileDAOTest {
 	private TaggedFile crudFile;
 	private static TaggedFileDAO dao = new TaggedFileDAO();
 
 	@Before
 	public void setUp()
 	{
 		System.out.println("setup()");
 
 		try {
 			SQLiteRepository.instance().setRepoPath("./jfmi-test.db");
 			SQLiteRepository.instance().initialize();
 
 			crudFile = new TaggedFile(0, "path/to/file", null);
 
 			dao.deleteAll();
 
 		} catch (ClassNotFoundException e) {
 			fail("test failed: " + e.toString());
 		} catch (SQLException e) {
 			fail("test failed: " + e.toString());
 		}
 	}
 
 	/* Tests behaviour when create() is passed a null pointer. */
 	@Test(expected= NullPointerException.class)
 	public void testCreate_NullParams()
 	{
 		System.out.println("testCreate_NullParams()");
 
 		try {
 			dao.create(null);
 			
 		} catch (SQLException e) {
 			fail("test failed: " + e.toString());
 		}
 	}
 
 	/* Tests inserting a TaggedFile into the database. */
 	@Test
 	public void testCreate_NonNullParams()
 	{
 		System.out.println("testCreate_NullParams()");
 
 		try {
 			boolean created = dao.create(crudFile);
 			assertTrue(created);
 			
 		} catch (SQLException e) {
 			System.out.println(e.toString());
 		}
 	}
 
 	/* Tests behaviour when inserting a pre-existing record.*/
 	@Test(expected= SQLException.class)
 	public void testCreate_DuplicateRecord() throws SQLException
 	{
 		System.out.println("testCreate_DuplicateRecord()");
 
 		try {
 			boolean created = dao.create(crudFile);
 			assertTrue(created);
 			
 		} catch (SQLException e) {
 			fail("failed to create first file: " + e.toString());
 		}
 
 		dao.create(crudFile);
 	}
 
 }
