 package ljw.jpodsuck;
 import static org.junit.Assert.*;
 
 import java.io.FileNotFoundException;
 import java.io.StringReader;
 import java.io.Reader;
 import java.io.FileReader;
 import java.util.Map;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 class PodcastTest extends Podcasts {
 	public PodcastTest() {
 
 	}
 
 
 	Map<String, Item> getPodcasts() {
 		return podCasts;
 	}
 }
 
 public class RssXmlParserTest {
 
 	@Test
 	public final void testFn1() {
 		PodcastTest pc = new PodcastTest();
 		try {
 			FileReader f = new FileReader("jpod1.xml");
 			RssXmlParser r = new RssXmlParser((Reader) f, (PodcastsInterface) pc);
 			assertEquals(true, r.parse());
 			Map<String, Item> podc = pc.getPodcasts();
 			assertEquals(152, podc.size());
 			Item item = podc.get("http://www.japanesepod101.com/premium_feed/pdfs/LI_S5L17_082410_jpod101_kanji.pdf");
 			Assert.assertNotNull(item);
 			assertEquals(100473, item.length);
 		} catch (FileNotFoundException e) {
 			Assert.fail("file not found");
 			e.printStackTrace();
 		} 
 	}
 }
