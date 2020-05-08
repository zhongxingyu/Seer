 import org.junit.Assert;
 import org.junit.Test;
 
 
 public class PaperTest {
	String[] authors={"Bob, Joe", "AppleSeed, Jonny"};
 	String title = "A grand advnture";
 	String serialTitle = "children's stuff";
 	int[] pageNumbers = {1,100};
 	String date = "September 1994";
 	String digitalIdentifier = "www.thatonesite.com";
 	Paper testPaper1 = new Paper(authors, title, serialTitle, pageNumbers, date, digitalIdentifier);
 
 
	String[] authors2={"ABob, Joe", "BAppleSeed, Jonny"};
 	String title2 = "AA Agrand advnture";
 	String serialTitle2 = "Achildren's stuff";
 	int[] pageNumbers2 = {2,150};
 	String date2 = "July 1990";
 	String digitalIdentifier2 = "www.thatonesite2.com";
 	Paper testPaper2 = new Paper(authors2, title2, serialTitle2, pageNumbers2, date2, digitalIdentifier2);
 	Paper testPaper3 = new Paper(authors2, title2, serialTitle2, pageNumbers2, date2, digitalIdentifier2);
 	
 	@Test
 	public void testComparable() {
 		Assert.assertEquals(1, testPaper1.compareTo(testPaper2));
 		Assert.assertEquals(-1, testPaper2.compareTo(testPaper1));
 		Assert.assertEquals(0, testPaper2.compareTo(testPaper3));
 	}
 }
