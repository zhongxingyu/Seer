 package util;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.List;
 
 import org.junit.Test;
 
 import play.test.UnitTest;
 import util.TcoffeeHelper.ResultHtml;
 
 public class TcoffeeHelperTest extends UnitTest { 
 
 	@Test 
 	public void testParseHtml() {
 		parseHtmlFile( TestHelper.file("/sample-alignment.html") );
 	}
 	
 	public static void parseHtmlFile( File file ) {
 		String TEST_STYLE = "SPAN { font-family: courier new, courier-new, courier, monospace; font-weight: bold; font-size: 11pt;}";
		String TEST_BODY = "<span class=valuedefault>T-COFFEE,&nbsp;Version_9.01&nbsp;";
 
 		ResultHtml result = TcoffeeHelper.parseHtml(file);
 		assertEquals( TEST_STYLE,  result.style.trim() .substring(0,TEST_STYLE.length()) );
 		assertEquals( TEST_BODY, result.body.trim() .substring(0,TEST_BODY.length()) );
 	}
 	
 	@Test
 	public void parseConsensus() { 
 		
 		List<Integer> expected = Arrays.asList( 6, 4, 3, 6, 3,3, -1, 1,1, 3, 4,4,4, 3,3, 4,4,4, 3, 4, 2, 3,3,3, 4,4, 3,3,3,3,3, 1,1,1,1,1,1,1, 2,2,2,2, 1,1,1,1, 2,2, 1,1,1, 2,2, 3, 
 		                                          3, 2, -1,-1, 2,2,2,2,2, 3, 2,2, 4, 5, 6,6, 5,5,5,5, -1,-1,-1,-1,-1, 4, 6, 7, 9 );
 		List<Integer> result = TcoffeeHelper.parseConsensus(TestHelper.file("/sample-alignment.html"));
 		assertEquals( expected, result );
 	}
 	
  	
 }
