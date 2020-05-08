 package nz.co.searchwellington.utils;
 
 import junit.framework.TestCase;
 
 public class TextTrimmerTest extends TestCase {
 	
	final String LONG_TEXT = "BROOKLYN Scouts may spend more time on the internet than on knots - but they’re learning values just as their predecessors did 100 years ago. Kea leader Duane Stewart says scouting is just as relevant as ever. They learn about self-reliance and looking after one another, values missing in society today, he says. A few years ago the government was talking about teaching morals in schools There ";
 	
 	public void testShouldTrimToThreeSentences() throws Exception {
 		
 		TextTrimmer trimmer = new TextTrimmer();
 		String result = trimmer.trimToCharacterCount(LONG_TEXT, 200);
 		assertTrue(result.endsWith(" just as their predecessors did 100 years ago."));									
 	}
 
 }
