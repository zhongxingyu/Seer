 package test;
 
 import static org.junit.Assert.*;
 
 import java.util.Iterator;
 
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import application.URLFilterExampleImpl;
 
 import webcrawler.URLFilter;
 import webcrawler.URLList;
 import webcrawler.URLListArrayListImpl;
 import webcrawler.URLListElement;
 
 /**
  * Test cases for the URLList class using URLListArrayListImpl
  * 
  * @author Peter Hayes
  * @author Iain Ritchie
  * 
  */
 public class URLListArrayListImplTest {
 	
 	private static URLList urlList;
 	
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 		urlList = new URLListArrayListImpl();
 	}
 	
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@Before
 	public void setUp() throws Exception {
 		urlList = new URLListArrayListImpl();
 	}
 	
 	/**
 	 * Test method for {@link webcrawler.URLList#add(int, java.lang.String)}.
 	 */
 	@Test
 	public void testAddIntString() {
 		urlList.add(1, "TestURL1");
 		urlList.add(2, "TestURL2");
 		assertEquals("Wrong Value Returned", 1, urlList.get(0).getPriority());
 		assertEquals("Wrong Value Returned", "TestURL1", urlList.get(0)
 				.getUrl());
 		assertEquals("Wrong Value Returned", 2, urlList.get(1).getPriority());
 		assertEquals("Wrong Value Returned", "TestURL2", urlList.get(1)
 				.getUrl());
 		
 	}
 	
 	/**
 	 * Test method for {@link webcrawler.URLList#add(webcrawler.URLListElement)}
 	 * .
 	 */
 	@Test
 	public void testAddURLListElement() {
 		URLListElement element1 = new URLListElement(1, "TestURL1");
 		URLListElement element2 = new URLListElement(2, "TestURL2");
 		urlList.add(element1);
 		urlList.add(element2);
 		assertEquals("Wrong Value Returned", element1, urlList.get(0));
 		assertEquals("Wrong Value Returned", element2, urlList.get(1));
 	}
 	
 	/**
 	 * Test method for {@link webcrawler.URLList#toString()}.
 	 */
 	@Test
 	public void testToString() {
 		urlList.add(1, "TestURL1");
 		urlList.add(2, "TestURL2");
 		String expectedValue = "[Priority:1, url:TestURL1, Priority:2, url:TestURL2]";
 		assertEquals("Wrong Value Returned", expectedValue, urlList.toString());
 	}
 	
 	/**
 	 * Test method for {@link webcrawler.URLList#get(int)}.
 	 */
 	@Test
 	public void testGet() {
 		urlList.add(1, "TestURL1");
 		urlList.add(2, "TestURL2");
 		assertEquals("Wrong Value Returned", 1, urlList.get(0).getPriority());
 		assertEquals("Wrong Value Returned", "TestURL1", urlList.get(0)
 				.getUrl());
 		assertEquals("Wrong Value Returned", 2, urlList.get(1).getPriority());
 		assertEquals("Wrong Value Returned", "TestURL2", urlList.get(1)
 				.getUrl());
 	}
 	
 	/**
 	 * Test method for {@link webcrawler.URLList#size()}.
 	 */
 	@Test
 	public void testSize() {
 		urlList.add(1, "TestURL1");
 		urlList.add(2, "TestURL2");
 		assertEquals("Wrong Value Returned", 2, urlList.size());
 	}
 	
 	/**
 	 * Test method for {@link webcrawler.URLList#getuRLFilter()}.
 	 */
 	@Test
 	public void testGetuRLFilter() {
 		URLFilter urlFilter = new URLFilterExampleImpl();
 		urlList.setuRLFilter(urlFilter);
 		assertEquals("Wrong Value Returned", urlFilter, urlList.getuRLFilter());
 	}
 	
 	/**
 	 * Test method for
 	 * {@link webcrawler.URLList#setuRLFilter(webcrawler.URLFilter)}.
 	 */
 	@Test
 	public void testSetuRLFilter() {
 		URLFilter urlFilter = new URLFilterExampleImpl();
 		urlList.setuRLFilter(urlFilter);
 		assertEquals("Wrong Value Returned", urlFilter, urlList.getuRLFilter());
 	}
 	
 	/**
 	 * Test method for {@link webcrawler.URLList#iterator()}.
 	 */
 	@Test
 	public void testIterator() {
 		urlList.add(1, "TestURL1");
 		urlList.add(2, "TestURL2");
 		
 		int iteratorPosition = 0;
 		
 		Iterator<URLListElement> tempURLListIterator = urlList.iterator();
 		
 		while (tempURLListIterator.hasNext()) {
 			URLListElement uRLListElement = tempURLListIterator.next();
 			
 			if (iteratorPosition == 0) {
 				assertEquals("Wrong Value Returned", 1,
 						uRLListElement.getPriority());
 				assertEquals("Wrong Value Returned", "TestURL1", urlList.get(0)
 						.getUrl());
 			}
 			if (iteratorPosition == 1) {
 				assertEquals("Wrong Value Returned", 2, urlList.get(1)
 						.getPriority());
 				assertEquals("Wrong Value Returned", "TestURL2", urlList.get(1)
 						.getUrl());
 			}
 			
 			iteratorPosition++;
 		}
 		
 	}
 	
 }
