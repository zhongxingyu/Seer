 package com.pellcorp.android.isohunt;
 
 import static org.junit.Assert.*;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.junit.Test;
 
 public class SearchResultParserTest {
 	@Test
 	public void testResults() throws Exception {
 		SearchResultParser parser = new SearchResultParser();
 		Document doc = Jsoup.parse(ResourceUtils.loadResourceAsString("/search-results.html"));
 		PageResults pageResults = parser.parseResults(doc);
 		assertEquals(1, pageResults.getCurrentPage());
 		assertEquals(8, pageResults.getNumberOfPages());
 		assertEquals(20, pageResults.getSearchResults().size());
 		
 		Result firstResult = pageResults.getSearchResults().get(0);
 		assertEquals(Category.TV, firstResult.getCategory());
		assertEquals("199226013", firstResult.getId());
 		assertEquals("120.6w", firstResult.getAge().toString());
 		assertEquals("Doctor Who 2005 Season 1-5", firstResult.getTitle());
 		assertEquals("+7", firstResult.getRating().getRating());
 		assertEquals(2, firstResult.getRating().getCommentCount());
 		assertEquals("28.45 GB", firstResult.getSize().toString());
 		assertEquals(193, firstResult.getSeeders());
 		assertEquals(340, firstResult.getLeeches());
 		
 		Result fifthResult = pageResults.getSearchResults().get(4);
 		assertEquals(Category.TV, fifthResult.getCategory());
		assertEquals("29210293", fifthResult.getId());
 		assertEquals("285.6w", fifthResult.getAge().toString());
 		assertEquals("The First Five Doctors", fifthResult.getTitle());
 		assertEquals("+6", fifthResult.getRating().getRating());
 		assertEquals(5, fifthResult.getRating().getCommentCount());
 		assertEquals("155.6 GB", fifthResult.getSize().toString());
 		assertEquals(1, fifthResult.getSeeders());
 		assertEquals(4, fifthResult.getLeeches());
 		
 		Result ninthResult = pageResults.getSearchResults().get(8);
 		assertEquals(Category.TV, ninthResult.getCategory());
		assertEquals("409623153", ninthResult.getId());
 		assertEquals("10.2w", ninthResult.getAge().toString());
 		assertEquals("Complete Doctor Who Series 2005 - 2011", ninthResult.getTitle());
 		assertEquals("+1", ninthResult.getRating().getRating());
 		assertEquals(2, ninthResult.getRating().getCommentCount());
 		assertEquals("34.64 GB", ninthResult.getSize().toString());
 		assertEquals(4, ninthResult.getSeeders());
 		assertEquals(57, ninthResult.getLeeches());
 	}
 }
