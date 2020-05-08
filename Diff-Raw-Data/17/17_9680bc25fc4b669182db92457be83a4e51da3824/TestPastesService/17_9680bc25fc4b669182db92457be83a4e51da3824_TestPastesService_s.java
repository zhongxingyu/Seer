 package net.caseydunham.pirate;
 
 import net.caseydunham.pirate.dao.PasteDao;
 import net.caseydunham.pirate.model.Paste;
 import net.caseydunham.pirate.services.PasteService;
 import net.caseydunham.pirate.services.impl.PasteServiceImpl;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mock;
 
 import java.util.Date;
 import java.util.List;
 
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 public class TestPastesService extends BaseTest {
 
 	private PasteService pasteService;
 
 	@Mock
 	private PasteDao pasteDao;
 
 	@Before
 	public void setUp() {
 		pasteService = new PasteServiceImpl();
 		pasteService.setPasteDao(pasteDao);
 	}
 
 	@Test
 	public void testCreatePaste() {
 		Paste p = new Paste();
 		pasteService.create(p);
 		verify(pasteDao).saveOrUpdate(p);
 		assertNotNull(p);
 		assertNotNull(p.getCreated());
 	}
 
 	@Test
 	public void testGetPasteById() {
 		Paste found = new Paste();
 		found.setId(999L);
 		Date created = new Date();
 		found.setCreated(created);
 		found.setTitle("Test");
 		found.setUsername("TestUser");
 		found.setContent("This is a test");
 		when(pasteDao.getById(999L)).thenReturn(found);
 
 		Paste p = pasteService.getById(999L);
 		assertNotNull(p);
 		assertEquals(new Long(999L), p.getId());
 		assertEquals(created, p.getCreated());
 		assertEquals("Test", p.getTitle());
 		assertEquals("TestUser", p.getUsername());
 		assertEquals("This is a test", p.getContent());
 	}
 
 	@Test
 	public void testRecentPastes() {
 		List<Paste> recent = pasteService.getRecentPastes();
 		verify(pasteDao).getRecentPastes();
		assertTrue(recent.size() == 10);
 	}
 
 }
