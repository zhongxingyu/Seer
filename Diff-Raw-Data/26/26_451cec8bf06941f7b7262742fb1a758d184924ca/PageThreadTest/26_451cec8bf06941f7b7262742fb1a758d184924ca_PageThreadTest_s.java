 /*  Copyright (C) 2012  Nicholas Wright
 	
 	part of 'Aid', an imageboard downloader.
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package board;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertThat;
 import static org.junit.matchers.JUnitMatchers.hasItems;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.LinkedList;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.github.dozedoff.commonj.io.TextFileReader;
 
 public class PageThreadTest {
 	PageThread pt;
 	
 	String[] testImageNames = {"1341010331300.jpg", "Tidus.jpg", "10941d1312594934-versus-logo-final-fantasy-versus-xiii-wallpaper-versus-logo-wallpaper-versuslogo[1].jpg"};
 	String[] testComments = {"Good night, sweet prince. We can finally stop hoping and talking about this now. http://www.ign.com/articles/2012/07/20/report-final-fantasy-versus-xiii-cancelled", "Welp, so much for that.", "No.", "Stop with this already, it's just a rumour.", "Rumors are fact now apparently", "It's IGN.", "Yeeeeeaaaaaaaaaaaaaaaah, i'll wait for an actual statement to drown my sorrows"};
 	
 	@Before
 	public void setUp() throws Exception{
 			pt  = new PageThread(new URL("http://foo.bar/a/src/12345"));
 	}
 	
 	@Test
 	public void testGetThreadNumber() {
 		String test = pt.getBoardDesignation();
 		assertEquals("a", test);
 	}
 
 	@Test
 	public void testGetBoardDeclaration() {
 		int test = pt.getThreadNumber();
 		assertEquals(12345, test);
 	}
 	
 	@Test
 	public void testProcessThread() throws IOException{
 		String testData = new TextFileReader().read(this.getClass().getClassLoader().
				getResourceAsStream("HtmlData\\pageThreadTestData")); // load test data
 		testData = testData.replaceAll("\n", ""); // test file is in human readable format, this is to simulate how the program would receive the data
 
 		int images = 0, comments = 0;
 		LinkedList<String> commentStrings = new LinkedList<>();
 		LinkedList<String> imageNames = new LinkedList<>();
 		
 		pt.processThread(testData);	// process test data
 		
 		assertThat(pt.getPosts().size(),is(7)); // check for the correct number of posts
 		
 		for(Post p : pt.getPosts()){	// count comments and images
 			if(p.hasComment()){
 				comments++;
 				commentStrings.add(p.getComment());
 			}
 			
 			if(p.hasImage()){
 				images++;
 				imageNames.add(p.getImageName());
 			}
 		}
 		
 		assertThat(comments, is(7)); 	// check number of comments
 		assertThat(images,is(3));		// check number of images
 		
 		assertThat(imageNames,hasItems(testImageNames));
 		assertThat(commentStrings,hasItems(testComments));
 	}	
 }
 
