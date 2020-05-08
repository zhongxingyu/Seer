 package test.unit;
 
 import static org.junit.Assert.*;
 import org.junit.*;
 import java.util.Arrays;
 import java.util.Date;
 
 /**
  * Unit test for {@link app.Citation}.
  */
 public class Citation {
 	app.Citation c;
 
 	@Before
 	@After
 	public void clearDb() {
 		app.GraphDatabase.clearDb();
 	}
 
 	@Test (expected=IllegalArgumentException.class)
 	public void excessiveDescription() {
 		char[] array = new char[257];
 		Arrays.fill(array, 'a');
 		String s = new String(array);
 
 		c = new app.Citation("test", s);
 	}
 
 	@Test (expected=IllegalArgumentException.class)
 	public void excessiveResource() {
 		char[] array = new char[257];
 		Arrays.fill(array, 'a');
 		String r = new String(array);
 
 		c = new app.Citation(r, "test");
 	}
 
	@Test
	public void recoverByID() {
		app.Citation c = new app.Citation("Test","http://example/test");
		app.Token id = c.getId();
		app.Citation b = new app.Citation(id);
		assertEquals( c.getId(), b.getId() );
		assertEquals( c.getInternalNode(), b.getInternalNode() );

	}
 }
