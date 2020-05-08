 package se.enbohms.hhcib.service.impl;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 import java.net.UnknownHostException;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.mongodb.BasicDBObject;
 
 import se.enbohms.hhcib.entity.Category;
 import se.enbohms.hhcib.entity.Subject;
 import se.enbohms.hhcib.entity.User;
 import se.enbohms.hhcib.service.api.SearchService;
 import se.enbohms.hhcib.web.util.IntegrationTest;
 
 /**
  * Test client for the {@link SearchService}
  */
 @org.junit.experimental.categories.Category(IntegrationTest.class)
 public class SearchServiceTest {
 
	private static final String CRAZY_SEARCH_STRING = "goasdsadasd asd\\sad:-asd\\es";
 	private static final String DESCRIPTION_ONE = "a description goes here...";
 	private static final String DESCRIPTION_TWO = "another description goes here includ vetemjöl ...";
 	private static final String DESCRIPTION_THREE = "another description <p>börjar</b>\r\n here includ vetemjöl\r\n ...";
 	private static final String HEADING = "a heading goes here";
 	private static final User USER = User.creteUser("id", "chuck", null);
 	private MongoCrudService searchService;
 
 	@Before
 	public void setUp() throws UnknownHostException {
 		searchService = new MongoCrudService();
 		MongoDBInitiator dbInitiator = new MongoDBInitiator();
 		dbInitiator.initDB();
 		searchService.setDBInitiator(dbInitiator);
 		BasicDBObject o = new BasicDBObject();
 		o.append("descriptionWithoutHtml", "text").append("heading", "text");
 		BasicDBObject o2 = new BasicDBObject();
 		o2.append("default_language", "swedish");
 		
 		dbInitiator.getMongoDB().getCollection("subject").ensureIndex(o, o2);
 	}
 
 	@Test
 	public void should_return_search_results_from_db() throws Exception {
 		Subject subjectOne = null;
 		Subject subjectTwo = null;
 
 		try {
 			
 			// given
 			subjectOne = searchService.createSubject(HEADING, DESCRIPTION_ONE,
 					Category.FOOD, USER);
 			subjectTwo = searchService.createSubject(HEADING, DESCRIPTION_TWO,
 					Category.FOOD, USER);
 
 			// when
 			List<Subject> result = searchService.search("vetemjöl");
 
 			// then
 			assertThat(result).isNotNull();
 			assertThat(result).isNotEmpty();
 		} finally {
 			if (subjectOne != null) {
 				searchService.delete(subjectOne.getId());
 			}
 			if (subjectTwo != null) {
 				searchService.delete(subjectTwo.getId());
 			}
 		}
 	}
 	
 	@Test
 	public void should_return_search_results_incl_swedish_chars_from_db() throws Exception {
 		Subject subjectOne = null;
 		Subject subjectTwo = null;
 
 		try {
 			
 			// given
 			subjectOne = searchService.createSubject(HEADING, DESCRIPTION_TWO,
 					Category.FOOD, USER);
 			subjectTwo = searchService.createSubject(HEADING, DESCRIPTION_THREE,
 					Category.FOOD, USER);
 
 			// when
 			List<Subject> result = searchService.search("börjar");
 
 			// then
 			assertThat(result).isNotNull();
 			assertThat(result).isNotEmpty();
 		} finally {
 			if (subjectOne != null) {
 				searchService.delete(subjectOne.getId());
 			}
 			if (subjectTwo != null) {
 				searchService.delete(subjectTwo.getId());
 			}
 		}
 	}
 
 	@Test
 	public void should_return_no_search_results_from_db() throws Exception {
 
 		// when
 		List<Subject> result = searchService.search(CRAZY_SEARCH_STRING);
 		// then
 		assertThat(result).isNotNull();
 		assertThat(result).isEmpty();
 	}
 }
