 package com.fishkees.backend.modules.lists.core;
 
 import static org.junit.Assert.*;
 
 import org.joda.time.DateTime;
 import org.junit.Test;
 
 import com.fishkees.backend.modules.lists.FlashcardListFixtures;
 
 public class FlashcardListTest {
 
 	@Test
 	public void serializesToJson() throws Exception {
		final FlashcardList flashcardList = flashcardList();
		assertEquals(FlashcardListFixtures.single().getCreateDate(),
				flashcardList.getCreateDate());
 	}
 
 	@Test
 	public void deserializesFromJson() throws Exception {
 		FlashcardList fromJson = FlashcardListFixtures.single();
 		final FlashcardList flashcardList = flashcardList();
 
 		assertEquals(fromJson.getTitle(), flashcardList.getTitle());
 		assertEquals(fromJson.getId(), flashcardList.getId());
 		assertEquals(fromJson.getCreateDate(), flashcardList.getCreateDate());
 	}
 
 	@Test
 	public void deserializeWithoutIdFromJson() throws Exception {
 		final FlashcardList fromJson = FlashcardListFixtures.partial();
 		final FlashcardList flashcardList = flashcardListWithoutId();
 
 		assertNull(fromJson.getId());
 		assertEquals(fromJson.getTitle(), flashcardList.getTitle());
 		assertEquals(fromJson.getCreateDate(), flashcardList.getCreateDate());
 
 	}
 
 	private FlashcardList flashcardList() throws Exception {
		DateTime dateTime = new DateTime("1986-07-01T12:00Z");
 		return new FlashcardList(1l, "abcd", dateTime.toDate());
 	}
 
 	private FlashcardList flashcardListWithoutId() throws Exception {
		DateTime dateTime = new DateTime("1986-07-01T12:00Z");
 		return new FlashcardList(null, "abcd", dateTime.toDate());
 	}
 
 }
