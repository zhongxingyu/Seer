 package sw.app_250grice.pojoTest;
 
 import static org.junit.Assert.*;
 
 import java.util.List;
 
 import sw.app_250grice.Item;
 import sw.app_250grice.ItemNotFoundException;
 import sw.app_250grice.Page;
 import sw.app_250grice.PageHandler;
 import sw.app_250grice.PageNameAlreadyExistsException;
 import sw.app_250grice.PageNotFoundException;
 import sw.app_250grice.Units;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class PageHandlerTest {
 	
 	PageHandler uut;
 
 	public PageHandlerTest() {
 	}
 	
 	@Before
 	public void setUp() throws Exception {
 	
 		uut = PageHandler.getPageHandler();
 		uut.addPageBlank("P1");
 		uut.addPageBlank("P2");
 		uut.addPageBlank("P3");
 		uut.addPageBlank("P4");
 		uut.addPageBlank("P5");
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		uut.dispose();
 	}
 	
 	@Test(expected=PageNameAlreadyExistsException.class)
 	public void testAddExistingPage() throws PageNameAlreadyExistsException{
 		uut.addPageBlank("P1");
 	}
 	
 	@Test
 	public void testContainsPageByName() {
 		boolean toCheck = uut.containsPageByName("P1");
 		
 		assertTrue(toCheck);
 		
 	    toCheck = uut.containsPageByName("P6");
 		
 		assertFalse(toCheck);
 	}
 	
 	@Test
 	public void testDeletePageByName() {
 		try {
 			uut.deletePageByName("P1");
 		} catch (PageNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		boolean toCheck = uut.containsPageByName("P1");
 		
 		assertFalse(toCheck);
 	}
 	
 	@Test(expected=PageNotFoundException.class)
 	public void testDeletePageByNameNonExisting() throws PageNotFoundException {
 		uut.deletePageByName("P6");
 
 	}
 	
 	@Test(expected=ItemNotFoundException.class)
 	public void testGetPages() throws ItemNotFoundException{
 		List<Page> pageList = uut.getPages();
 		
 		Page page = pageList.get(0);
 		Item item = new Item("I1", 0.35);
 		page.addItem(item);
 		
 		pageList.remove(page);
 
 		boolean toCheck = uut.containsPageByName("P1");
 		pageList = uut.getPages();
 		page = pageList.get(0);
 		assertTrue(toCheck);
 		
 		page.getItemByNameAndUnit("I1", Units.NONE);
 	}
 	
 	@Test(expected=PageNotFoundException.class)
 	public void testRenamePageByNameNotFound() throws PageNotFoundException, PageNameAlreadyExistsException {
 		uut.renamePageByName("PX", "PY");
 
 	}
 	
 	@Test(expected=PageNameAlreadyExistsException.class)
 	public void testRenamePageByNameNameAlreadyExists() throws PageNotFoundException, PageNameAlreadyExistsException {
 		uut.renamePageByName("P1", "P2");
 
 	}
 	
 	@Test
 	public void testRenamePageByName() throws PageNotFoundException, PageNameAlreadyExistsException {
 		uut.renamePageByName("P1", "PX");
 		
 		List<Page> pages = uut.getPages();
 		String toCheck = pages.get(0).getName();
 		assertEquals(toCheck, "PX");
 	}
 	
 	@Test
 	public void testAddPageExisting() throws PageNameAlreadyExistsException {
 		Page p = new Page("P6");
 		Item i = new Item("i1", 2);
 		p.addItem(i);
 		
 		uut.addPageExisting(p);
 	}
 	
 	@Test(expected=PageNameAlreadyExistsException.class)
 	public void testAddPageExistingThrow() throws PageNameAlreadyExistsException {
 		Page p = new Page("P1");
 		Item i = new Item("i1", 2);
 		p.addItem(i);
 		
 		uut.addPageExisting(p);
 		
 	}
 
 }
