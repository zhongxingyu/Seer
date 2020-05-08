 package util;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.util.Arrays;
 import java.util.List;
 
 import org.junit.Test;
 
 import play.test.UnitTest;
 import util.TCoffeeHelper.ResultHtml;
 
 public class TCoffeeHelperTest extends UnitTest { 
 
 	@Test 
 	public void testParseHtml() {
 		parseHtmlFile( TestHelper.file("/sample-alignment.html") );
 	}
 	
 	public static void parseHtmlFile( File file ) {
 		String TEST_STYLE = "SPAN { font-family: courier new, courier-new, courier, monospace; font-weight: bold; font-size: 11pt;}";
		String TEST_BODY = "<span class=valuedefault>T-COFFEE,&nbsp;Version_9.03";
 
 		ResultHtml result = TCoffeeHelper.parseHtml(file);
 		assertEquals( TEST_STYLE,  result.style.trim() .substring(0,TEST_STYLE.length()) );
 		assertEquals( TEST_BODY, result.body.trim() .substring(0,TEST_BODY.length()) );
 	}
 	
 	@Test
 	public void parseConsensus() { 
 		
 		List<Integer> expected = Arrays.asList( 6, 4, 3, 6, 3,3, -1, 1,1, 3, 4,4,4, 3,3, 4,4,4, 3, 4, 2, 3,3,3, 4,4, 3,3,3,3,3, 1,1,1,1,1,1,1, 2,2,2,2, 1,1,1,1, 2,2, 1,1,1, 2,2, 3, 
 		                                          3, 2, -1,-1, 2,2,2,2,2, 3, 2,2, 4, 5, 6,6, 5,5,5,5, -1,-1,-1,-1,-1, 4, 6, 7, 9 );
 		List<Integer> result = TCoffeeHelper.parseConsensus(TestHelper.file("/sample-alignment.html"));
 		assertEquals( expected, result );
 	}
 	
 	/**
 	 * new FileReader(file)
 	 */
 	@Test 
 	public void testTemplateFile( )  {
 		List<String[]> list = TCoffeeHelper.parseTemplateList( TestHelper.file("/tcoffee.template_list") );
 		assertNotNull(list);
 		assertEquals( 5, list.size() );
 
 		assertEquals( "CO8A1", list.get(0)[0] );
 		assertEquals( "_P_", list.get(0)[1] );
 		assertEquals( "1o91C", list.get(0)[2] );
 
 		assertEquals( "TNFSF2", list.get(1)[0] );
 		assertEquals( "_P_", list.get(1)[1] );
 		assertEquals( "3l9jT", list.get(1)[2] );
 
 		assertEquals( "TNFSF4", list.get(2)[0] );
 		assertEquals( "_P_", list.get(2)[1] );
 		assertEquals( "2hevF", list.get(2)[2] );
 	}
 	
 	@Test
 	public void testStrikeFile( ) {
 		File file = TestHelper.file("/strike.out.txt");
 		assertTrue(file.exists());
 		
 		
 		List<String[]> list = TCoffeeHelper.parseStrikeOutput(file);
 		assertNotNull(list);
 		assertEquals( 5, list.size() );
 		assertEquals( list.get(0)[0], "1g41a");
 		assertEquals( list.get(0)[1], "0g41a");
 		assertEquals( list.get(0)[2], "1.85");
 		
 		assertEquals( list.get(1)[0], "1e94e");
 		assertEquals( list.get(1)[1], "2e94e");
 		assertEquals( list.get(1)[2], "1.59");
 		
 		assertEquals( list.get(2)[0], "1e32a");
 		assertEquals( list.get(2)[1], "3e32a");
 		assertEquals( list.get(2)[2], "1.56");
 		
 		assertEquals( list.get(3)[0], "1d2na");
 		assertEquals( list.get(3)[1], "4d2na");
 		assertEquals( list.get(3)[2], "1.46");
 		
 		assertEquals( list.get(4)[0], "AVG");
 		assertEquals( list.get(4)[1], "-");
 		assertEquals( list.get(4)[2], "1.62");
 		
 	}
  	
 }
