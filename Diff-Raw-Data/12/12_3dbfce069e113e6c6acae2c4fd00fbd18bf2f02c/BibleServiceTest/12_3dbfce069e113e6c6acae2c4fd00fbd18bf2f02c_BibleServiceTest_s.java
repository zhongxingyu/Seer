 package net.todd.bible.scripturelookup.server;
 
 import static org.junit.Assert.assertSame;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class BibleServiceTest {
 	private IBibleDao bibleDao;
 
 	@Before
 	public void setUp() {
 		bibleDao = mock(IBibleDao.class);
 	}
 
 	@Test
	public void test() {
 		List<Verse> expectedVerses = new ArrayList<Verse>();
 		when(bibleDao.getAllVerses()).thenReturn(expectedVerses);
 
 		IBibleService bibleService = new BibleService(bibleDao);
 
 		List<Verse> actualVerses = bibleService.search(null);
 
		assertSame(expectedVerses, actualVerses);
 	}
 }
