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
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.mock;
 import io.ImageLoader;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.github.dozedoff.commonj.io.TextFileReader;
 
 import filter.Filter;
 
 public class PageTest {
 	Page page;
 	
 	Filter mockFilter = mock(Filter.class);
 	ImageLoader mockImageLoader = mock(ImageLoader.class);
 	
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Test
 	public void testGetPageUrl() throws MalformedURLException {
 		page = new Page(new URL("http://foo.bar/"), 1, mockFilter, mockImageLoader);
 		assertThat(page.getPageUrl(), is(new URL("http://foo.bar/1")));
 		
 		page = new Page(new URL("http://foo.bar/"), 2, mockFilter, mockImageLoader);
 		assertThat(page.getPageUrl(), is(new URL("http://foo.bar/2")));
 		
 		page = new Page(new URL("http://foo.bar/"), 3, mockFilter, mockImageLoader);
 		assertThat(page.getPageUrl(), is(new URL("http://foo.bar/3")));
 		
 		page = new Page(new URL("http://foo.bar/"), 0, mockFilter, mockImageLoader);
 		assertThat(page.getPageUrl(), is(new URL("http://foo.bar/")));
 		
 		page = new Page(new URL("http://foo.bar"), 0, mockFilter, mockImageLoader);
 		assertThat(page.getPageUrl(), is(new URL("http://foo.bar/")));
 	}
 
 	@Test
 	public void testProcessPage() throws IOException {
 		String pageTestHtml = new TextFileReader().read(this.getClass().getClassLoader().
				getResourceAsStream("HtmlData/pageTestData")); // load test data
 		pageTestHtml = pageTestHtml.replaceAll("\n", ""); // test file is in human readable format, this is to simulate how the program would receive the data
 		
 		page = new Page(new URL("http://foo.bar/"), 0, mockFilter, mockImageLoader);
 		page.parseHtml(pageTestHtml);
 		
 		assertThat(page.getThreadUrls().size(), is(10));
 		
 		assertThat(page.getThreadUrls().get(0), is(new URL("http://foo.bar/res/1418")));
 		assertThat(page.getThreadUrls().get(9), is(new URL("http://foo.bar/res/1447")));
 	}
 	
 	@Test
 	public void testEquals() throws MalformedURLException {
 		Page page0 = new Page(new URL("http://foo.bar/"), 0, mockFilter, mockImageLoader);
 		Page page1 = new Page(new URL("http://foo.bar/"), 1, mockFilter, mockImageLoader);
 		Page page2 = new Page(new URL("http://foo.bar/"), 2, mockFilter, mockImageLoader);
 		Page page_1 = new Page(new URL("http://foo.bar/"), 1, mockFilter, mockImageLoader);
 		Page page_2 = new Page(new URL("http://foo.bar/"), 2, mockFilter, mockImageLoader);
 		
 		assertThat(page0.equals(page1), is(false));
 		assertThat(page1.equals(page0), is(false));
 		
 		assertThat(page1.equals(page_1), is(true));
 		assertThat(page_1.equals(page1), is(true));
 		assertThat(page_2.equals(page1), is(false));
 		
 		assertThat(page2.equals(page_2), is(true));
 	}
 }
