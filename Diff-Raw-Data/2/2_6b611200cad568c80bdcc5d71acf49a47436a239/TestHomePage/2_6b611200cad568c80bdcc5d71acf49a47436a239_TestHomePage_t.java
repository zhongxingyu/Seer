 package net.indyjug;
 
 import junit.framework.TestCase;
 import org.apache.wicket.util.tester.WicketTester;
 
 /**
  * Simple test using the WicketTester
  */
 public class TestHomePage extends TestCase
 {
 	private WicketTester tester;
 
 	@Override
 	public void setUp()
 	{
 		tester = new WicketTester(new WicketApplication());
 	}
 
 	public void testRenderMyPage()
 	{
 		//start and render the test page
 		tester.startPage(HomePage.class);
 
 		//assert rendered page class
 		tester.assertRenderedPage(HomePage.class);
 
 		//assert rendered label component
		tester.assertLabel("message", "1,285,778,541,604 milliseconds have elapsed since the unix epoch.");
 	}
 }
