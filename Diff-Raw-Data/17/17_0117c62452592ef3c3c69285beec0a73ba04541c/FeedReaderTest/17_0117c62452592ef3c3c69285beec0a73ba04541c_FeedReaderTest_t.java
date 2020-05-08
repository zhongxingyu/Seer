 package tests;
 
import feed.FeedReader;
import junit.framework.TestCase;

 /**
  * Created by IntelliJ IDEA.
  * User: mkotsur
  * Date: Dec 13, 2009
  * Time: 9:15:15 PM
  * To change this template use File | Settings | File Templates.
  */
public class FeedReaderTest extends TestCase {

    public void testSmoke() {
        FeedReader feedReader = new FeedReader();
        assertEquals('2', '2');
    }
 
     
 }
