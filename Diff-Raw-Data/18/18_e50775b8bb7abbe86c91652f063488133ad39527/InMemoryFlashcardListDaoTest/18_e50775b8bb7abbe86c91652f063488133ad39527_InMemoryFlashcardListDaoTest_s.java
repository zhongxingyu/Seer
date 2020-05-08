 package com.fishkees.backend.modules.lists.dao;
 
 
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.*;
 
 import java.util.Date;
 import java.util.List;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.ArgumentCaptor;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 
 import com.fishkees.backend.modules.lists.FlashcardListFixtures;
 import com.fishkees.backend.modules.lists.core.FlashcardList;
 
 @RunWith(MockitoJUnitRunner.class)
 public class InMemoryFlashcardListDaoTest {
 	private static final String ID1 = "someNiceId1";
 	private static final String ID2 = "someNiceId2";
 
 
 	@InjectMocks
 	private InMemoryFlashcardListDao testObj;
 
 	@Mock
	FlashcardListInMemoryStorage storage;
 	
 	private List<FlashcardList> lists;
 	
 	@Before
 	public void setUp() {
 		lists = FlashcardListFixtures.all();
 
 		when(storage.all()).thenReturn(lists);
 		when(storage.get(ID1)).thenReturn(lists.get(0));
 		when(storage.get(ID2)).thenReturn(lists.get(1));
 		
 		when(storage.remove(ID1)).thenReturn(lists.get(0));
 		when(storage.remove(ID2)).thenReturn(lists.get(1));
 		
 	}
 	
 	@After
 	public void tearDown() {
 		verifyNoMoreInteractions(storage);
 	}
 
 	@Test
 	public void testGetAll() throws Exception {
 		// given
 		
 		// when
 		List<FlashcardList> result = testObj.findAll();
 
 		// then
 		assertEquals(lists, result);
 		verify(storage).all();
 	}
 	
 	@Test
 	public void testCreate() {
 		// given
 		FlashcardList fl = new FlashcardList(null, "abcd", null);
 		when(storage.getNewId()).thenReturn(ID1);
 
 		// when
 		FlashcardList resultFlashcardList = testObj.createNewFromObject(fl);
 		
 		// then
 		verify(storage).getNewId();
 
 		ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
 		ArgumentCaptor<FlashcardList> listCaptor = ArgumentCaptor.forClass(FlashcardList.class);
 		
 		verify(storage).put(stringCaptor.capture(), listCaptor.capture());
 		
 		String id = stringCaptor.getValue();
 		FlashcardList newFlashcardListFromStorage = listCaptor.getValue();
 
 		assertNotNull(id);
 		assertEquals(id, newFlashcardListFromStorage.getId());
 		assertEquals("abcd", newFlashcardListFromStorage.getTitle());
 		assertEquals(newFlashcardListFromStorage, resultFlashcardList);
 		assertNotNull(resultFlashcardList.getCreateDate());
 	}
 	
 	@Test
 	public void testFind() {
 		// when
 		FlashcardList result1 = testObj.findById(ID1);
 		FlashcardList result2 = testObj.findById(ID2);
 
 		// then
 		assertEquals(ID1, result1.getId());
 		assertEquals(ID2, result2.getId());
 		assertEquals("Spanish for beginners", result1.getTitle());
 		assertEquals("Russian for intermediate", result2.getTitle());
 		
 		verify(storage).get(ID1);
 		verify(storage).get(ID2);
 	}
 	
 	@Test
 	public void testRemove_existing() {
 		// when
 		FlashcardList removed = testObj.remove(ID1);
 		
 		// then 
 		assertEquals(ID1, removed.getId());
 		
 		verify(storage).remove(ID1);
 	}
 	
 	@Test
 	public void testRemove_nonExisting() {
 		// when
 		FlashcardList removed = testObj.remove("1000");
 		
 		// then
 		assertNull(removed);
 		
 		verify(storage).remove("1000");
 	}
 	
 	@Test
 	public void testUpdate_nonExisting() {
 		// given
 		FlashcardList flashcardList = new FlashcardList("3", "new title", new Date());
 
 		// when
 		FlashcardList update = testObj.update(flashcardList);
 		
 		// then
 		assertNull(update);
 		verify(storage).update("3", flashcardList);
 	}
 	
 	@Test
 	public void testUpdate_existing() {
 		// given 
 		FlashcardList fl = new FlashcardList(ID1, "new title", new Date());
 		when(storage.update(ID1, fl)).thenReturn(fl);
 		
 		// when
 		FlashcardList updated = testObj.update(fl);
 		
 		assertEquals(fl, updated);
 		verify(storage).update(ID1, fl);
 	}
 }
