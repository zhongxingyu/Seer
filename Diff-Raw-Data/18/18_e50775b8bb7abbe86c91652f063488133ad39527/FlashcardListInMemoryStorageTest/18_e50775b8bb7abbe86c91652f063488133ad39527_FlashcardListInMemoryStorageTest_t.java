 package com.fishkees.backend.dataaccess;
 
 import static org.junit.Assert.*;
 
 import java.util.Date;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.fishkees.backend.modules.lists.core.FlashcardList;
 import com.fishkees.backend.modules.lists.dao.FlashcardListInMemoryStorage;
 import com.google.common.collect.Lists;
 
 public class FlashcardListInMemoryStorageTest {
 	private static final String ID1 = "someId1";
 	private static final String ID2 = "someId2";
 	private static final String ID3 = "someId3";
 	private FlashcardListInMemoryStorage testObj;
 
 	@Before
 	public void setUp() {
 		FlashcardList fl1 = new FlashcardList(ID1, "a", new Date());
 		FlashcardList fl2 = new FlashcardList(ID2, "bcde", new Date());
 		FlashcardList fl3 = new FlashcardList(ID3, "c", new Date());
 
 		this.testObj = new FlashcardListInMemoryStorage(fl1, fl2, fl3);
 	}
 	
 	@Test
 	public void testSavingAndRestoring() {
 		// given
 		FlashcardList fl = new FlashcardList("15", "abcd", new Date());
 		
 		// when
 		testObj.put(fl.getId().toString(), fl);
 
 		// then
 		assertEquals(4, testObj.all().size());
 		assertEquals(fl, testObj.get("15"));
 	}
 
 	@Test
 	public void testRestoringNonExistent() {
 		assertNull(testObj.get("0"));
 	}
 
 	@Test
 	public void testGetAll() {
 		// when
 		List<FlashcardList> all = Lists.newArrayList(testObj.all());
 
 		// then
 		assertEquals(3, all.size());
 		assertEquals(ID3, all.get(0).getId());
 		assertEquals(ID1, all.get(1).getId());
 		assertEquals(ID2, all.get(2).getId());
 	}
 
 	@Test
 	public void testFind() {
 		// when
 		FlashcardList flashcardList = testObj.get(ID2);
 		
 		// then
 		assertEquals(ID2, flashcardList.getId());
 		assertEquals("bcde", flashcardList.getTitle());
 	}
 	
 	@Test
 	public void testRandomUUID() {
 		// when
 		String string1 = testObj.getNewId();
 		String string2 = testObj.getNewId();
 
 		assertNotEquals(string1, string2);
 	}
 
 	@Test
 	public void testReset() {
 		assertEquals(3, this.testObj.all().size());
 		
 		this.testObj.put("100", new FlashcardList("100", "qwer", new Date()));
 		assertEquals(4, this.testObj.all().size());
 		
 		// when
 		this.testObj.reset();
 		
 		// then
 		assertEquals(3,  this.testObj.all().size());
 	}
 	
 	@Test
 	public void testRemove_exists() {
 		// given
 		assertEquals(3, this.testObj.all().size());
 
 		// when
 		FlashcardList removed = this.testObj.remove(ID3);
 		
 		// then
 		assertEquals(2, this.testObj.all().size());
 		assertNotNull(removed);
 		assertEquals(ID3, removed.getId());
 	}
 
 	@Test
 	public void testRemove_notExists() {
 		// given
 		assertEquals(3,  this.testObj.all().size());
 		
 		// when
 		FlashcardList removed = this.testObj.remove("1000");
 	
 		// then
 		assertEquals(3,  this.testObj.all().size());
 		assertNull(removed);
 	}
 
 	@Test
 	public void testUpdate_exists() {
 		// given
 		FlashcardList fl = new FlashcardList(ID1, "new title", new Date());
 		
 		// when
 		FlashcardList update = testObj.update(ID1, fl);
 		
 		// then
 		assertNotNull(update);
 		
		assertEquals(fl, update);
 		FlashcardList fromStorage = testObj.get(ID1);
 		assertEquals("new title", fromStorage.getTitle());
 		assertEquals(ID1, fromStorage.getId());
 	}
 	
 	@Test
 	public void testUpdate_not_exists() {
 		// given
 		FlashcardList fl = new FlashcardList("4", "new title", new Date());
 		
 		// when
 		FlashcardList update = testObj.update("4", fl);
 		
 		// then
 		assertNull(update);
 		
 		FlashcardList fromStorage = testObj.get("4");
 		assertNull(fromStorage);
 	}
 
 }
