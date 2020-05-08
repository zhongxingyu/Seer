 package burrito.services;
 
 import java.util.Date;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import burrito.client.widgets.panels.table.ItemCollection;
 import burrito.client.widgets.panels.table.PageMetaData;
 import burrito.test.crud.AnotherSearchTestEntity;
 import burrito.test.crud.ChildEntity;
 import burrito.test.crud.SearchTestEntity;
 
 public class SearchServiceSearchManagerTest extends SearchManagerTest {
 
 	@Override
 	protected Class<? extends SearchManager> getSearchManagerUnderTest() {
 		return SearchServiceSearchManager.class;
 	}
 	
 	@Test
 	public void searchCanBeSortedAndPaged() {
 		//Start by adding 10 entities to the database 
 		int num = 10;
 		for (int i = 0; i < num; i++) {
 			SearchTestEntity entity = new SearchTestEntity();
 			entity.setName("a " + i);
 			entity.setDisplayableField("display " + (num - i - 1));
 			entity.save();
 		}
 		
 		ItemCollection<SearchHit> entries = searchManager.search(SearchTestEntity.class, "display", new PageMetaData<String>(num, 0, "displayableField", true));
 		Assert.assertEquals("a 9", entries.getItems().get(0).getTitle());
 		
 		entries = searchManager.search(SearchTestEntity.class, "display", new PageMetaData<String>(2, 0, "displayableField", false));
 		Assert.assertEquals("a 0", entries.getItems().get(0).getTitle());
 		Assert.assertTrue(entries.isHasNextPage());
 		
 		entries = searchManager.search(SearchTestEntity.class, "display", new PageMetaData<String>(2, entries.getPage() + 1, "displayableField", false));
 		Assert.assertEquals("a 2", entries.getItems().get(0).getTitle());
 		
 		
 	}
 	
 	@Test
 	public void searchCanBeSortedByDate() {
 		int num = 10;
 		long now = System.currentTimeMillis();
 		for (int i = 0; i < num; i++) {
 			SearchTestEntity entity = new SearchTestEntity();
 			entity.setName("a " + i);
 			entity.setDisplayableField("display " + (num - i - 1));
 			entity.setDate(new Date(now + (i * 1000))); //Search can only deal with second precision
 			entity.save();
 		}
 		
 		//Create another one with null date value
 		SearchTestEntity entity = new SearchTestEntity();
 		entity.setName("a " + num);
 		entity.setDisplayableField("display " + (num - 1));
 		entity.setDate(null); 
 		entity.save();
 	
 		//Create another entity, with same field names but different data types:
 		AnotherSearchTestEntity another = new AnotherSearchTestEntity();
 		another.setName(1000L);
 		another.setLastModified("display");
 		another.setDate("2001-02-22");
 		another.setNumber("12");
 		another.save();
 		
 		ItemCollection<SearchHit> entries = searchManager.search(SearchTestEntity.class, "display", new PageMetaData<String>(num, 0, "date", true));
 		Assert.assertEquals("a 0", entries.getItems().get(0).getTitle());
 		
 		entries = searchManager.search(SearchTestEntity.class, "display", new PageMetaData<String>(num, 0, "date", false));
 		Assert.assertEquals("a 9", entries.getItems().get(0).getTitle());
 		
 		
 	}
 	
 	
 	@Test
 	public void searchCanBeSortedByNumber() {
 		int num = 10;
 		for (int i = 0; i < num; i++) {
 			SearchTestEntity entity = new SearchTestEntity();
 			entity.setName("a " + i);
 			entity.setDisplayableField("display " + (num - i - 1));
 			entity.setNumber((long) (num - i)); 
 			entity.save();
 		}
 		
 		//Create another one with null number value
 		SearchTestEntity entity = new SearchTestEntity();
 		entity.setName("null");
 		entity.setDisplayableField("display " + (num - 1));
 		entity.setNumber(null); 
 		entity.insert();
 	
 		//Create another one with very large number value
 		entity = new SearchTestEntity();
 		entity.setName("Long.MAX_VALUE");
 		entity.setDisplayableField("display " + (num));
 		entity.setNumber(Long.MAX_VALUE); 
 		entity.insert();
 		
 		
 		ItemCollection<SearchHit> entries = searchManager.search(SearchTestEntity.class, "display", new PageMetaData<String>(100, 0, "number", true));
 		Assert.assertEquals("null", entries.getItems().get(0).getTitle());
 		
 		entries = searchManager.search(SearchTestEntity.class, "display", new PageMetaData<String>(100, 0, "number", false));
 		Assert.assertEquals("Long.MAX_VALUE", entries.getItems().get(0).getTitle());
 		
 		
 	}
 	
 	
 
 	@Test
 	public void globalSearchReturnsDecentSnippets() {
 		ChildEntity otherType = new ChildEntity();
 		otherType.setChildProperty("I was at a big pile of something where I met a man who said hello and continued walking a long way back. Refrigerator.");
 		otherType.save();
 		
 		ItemCollection<SearchHit> results = searchManager.search("hello");
 		Assert.assertEquals(2, results.getItems().size());
 		
 		//Due to a current bug in the dev_appserver, snippets are missing. This led to our own snippet creator. 
 		//It works, but will probably have to be updated soon. 
 		Assert.assertEquals("...something where I met a man who said hello and <span class=\"snippet-highlight\">continued</span> walking a long way back....", searchManager.search("continued").getItems().get(0).getSnippetHtml());
 		
 	}
	
	
	@Test
	public void getDateOfLastModified() {
		
		Assert.assertNotNull(searchManager.getLastModified(SearchTestEntity.class));
	}
 
 }
