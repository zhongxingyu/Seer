 package net.todd.biblestudy;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.List;
 
 import org.junit.Test;
 
 public class BibleDaoTest {
 	@Test
 	public void testAllVersesCall() throws DataException {
 		BibleDao dao = new BibleDao();
 		List<BibleVerse> allVerses = dao.getAllVerses();
		assertEquals(100, allVerses.size());
 	}
 }
