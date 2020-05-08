 package net.indyjug;
 
 import junit.framework.TestCase;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.util.tester.ITestPageSource;
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
		tester.startPage(new ITestPageSource() {
			public Page getTestPage() {
				PageParameters params = new PageParameters();
				params.add("ms", "1000");
				return new HomePage(params);
			}
        });
 
 		//assert rendered page class
 		tester.assertRenderedPage(HomePage.class);
 
 		//assert rendered label component
		tester.assertLabel("message", "1,000 milliseconds have elapsed since the unix epoch.");
 	}
 }
