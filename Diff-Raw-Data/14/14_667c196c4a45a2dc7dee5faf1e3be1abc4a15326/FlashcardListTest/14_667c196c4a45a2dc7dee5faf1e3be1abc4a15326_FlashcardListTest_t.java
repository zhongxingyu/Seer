 package com.fishkees.backend.modules.lists.core;
 
import static com.yammer.dropwizard.testing.JsonHelpers.*;
 import static org.junit.Assert.*;
 
 import org.joda.time.DateTime;
 import org.junit.Test;
 
 import com.fishkees.backend.modules.lists.FlashcardListFixtures;
 
 public class FlashcardListTest {
	private final DateTime dateTime = new DateTime("1986-07-01T12:00Z");
 
 	@Test
 	public void serializesToJson() throws Exception {
		final String flashcardList = asJson(flashcardList());
		final String fixture = jsonFixture("fixtures/lists/single.json"); 
		assertEquals(flashcardList, fixture);
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
 		return new FlashcardList(1l, "abcd", dateTime.toDate());
 	}
 
 	private FlashcardList flashcardListWithoutId() throws Exception {
 		return new FlashcardList(null, "abcd", dateTime.toDate());
 	}
 
 }
