 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 
 public class PublicationListTest {
 
 	/*
 	 * Test papers for methods
 	 */
	String[] authors={"BBob, Joe", "AppleSeed, Jonny"};
 	String title = "c";
 	String serialTitle = "children's stuff";
 	int[] pageNumbers = {1,100};
 	String date = "September 1994";
 	String digitalIdentifier = "www.thatonesite.com";
 	Paper testPaper1 = new Paper(authors, title, serialTitle, pageNumbers, date, digitalIdentifier);
 
 
	String[] authors2={"ABob, Joe", "BAppleSeed, Jonny"};
 	String title2 = "a";
 	String serialTitle2 = "Achildren's stuff";
 	int[] pageNumbers2 = {2,150};
 	String date2 = "July 1990";
 	String digitalIdentifier2 = "www.thatonesite2.com";
 	Paper testPaper2 = new Paper(authors2, title2, serialTitle2, pageNumbers2, date2, digitalIdentifier2);
 
	String[] authors3={"CBob, ABob", "AppleSeed, AJonny"};
 	String title3 = "b";
 	String serialTitle3 = "cAhildren's stuff";
 	int[] pageNumbers3 = {3,50};
 	String date3 = "January 1989";
 	String digitalIdentifier3 = "www.thatonesite3.com";
 	Paper testPaper3 = new Paper(authors3, title3, serialTitle3, pageNumbers3, date3, digitalIdentifier3);
 
 	/**
 	 * tests the sortByDate method of publicationList
 	 */
 	@Test
 	public void testSortByDate() {
 
 		PublicationList testList = new PublicationList();
 
 		testList.add(testPaper1);
 		testList.add(testPaper2);
 		testList.add(testPaper3);
 
 		testList.sortByDate();
 
 		PublicationList correctList = new PublicationList();
 		correctList.add(testPaper3);
 		correctList.add(testPaper2);
 		correctList.add(testPaper1);
 
 		assertEquals(testList, correctList);
 	}
 
 
 	/**
 	 * tests the sortByDigitalIdentifier method of publicationList
 	 */
 	@Test
 	public void testSortByDigitalIdentifier() {
 
 		PublicationList testList = new PublicationList();
 
 		testList.add(testPaper1);
 		testList.add(testPaper2);
 		testList.add(testPaper3);
 
 		testList.sortByDigitalIdentifier();
 
 		PublicationList correctList = new PublicationList();
 		correctList.add(testPaper1);
 		correctList.add(testPaper2);
 		correctList.add(testPaper3);
 
 		assertEquals(testList, correctList);
 	}
 
 	/**
 	 * tests the sortBySerialTitle method of publicationList
 	 */
 	@Test
 	public void testSortBySerialTitle() {
 
 		PublicationList testList = new PublicationList();
 
 		
 		testList.add(testPaper1);
 		testList.add(testPaper2);
 		testList.add(testPaper3);
 		
 		testList.sortBySerialTitle();
 
 		PublicationList correctList = new PublicationList();
 		correctList.add(testPaper2);
 		correctList.add(testPaper3);
 		correctList.add(testPaper1);
 
 		assertEquals(testList, correctList);
 	}
 
 	/**
 	 * tests the sortByPaperTitle method of publicationList
 	 */
 	@Test
 	public void testSortByPaperTitle() {
 
 		PublicationList testList = new PublicationList();
 
 		testList.add(testPaper1);
 		testList.add(testPaper2);
 		testList.add(testPaper3);
 		
 		testList.sortByPaperTitle();
 		
 		PublicationList correctList = new PublicationList();
 		correctList.add(testPaper2);
 		correctList.add(testPaper3);
 		correctList.add(testPaper1);
 
 		assertEquals(testList, correctList);
 	}
 
 	/**
 	 * tests the sortByAuthor method of publicationList
 	 */
 	@Test
 	public void testSortByAuthor() {
 
 		PublicationList testList = new PublicationList();
 
 		testList.add(testPaper1);
 		testList.add(testPaper2);
 		testList.add(testPaper3);
 
 		testList.sortByAuthor();
 
 		PublicationList correctList = new PublicationList();
 		correctList.add(testPaper2);
 		correctList.add(testPaper1);
 		correctList.add(testPaper3);
 
 		assertEquals(testList, correctList);
 	}
 
 	/**
 	 * tests the sortByBibliographicInfo method of publicationList
 	 */
 	@Test
 	public void testSortByBibliographicInfo() 
 	{
 		PublicationList testList = new PublicationList();
 
 		testList.add(testPaper1);
 		testList.add(testPaper2);
 		testList.add(testPaper3);
 
 		testList.sortByBibliographicInfo();
 
 		PublicationList correctList = new PublicationList();
 		correctList.add(testPaper2);
 		correctList.add(testPaper1);
 		correctList.add(testPaper3);
 
 		assertEquals(testList, correctList);
 	}
 }
