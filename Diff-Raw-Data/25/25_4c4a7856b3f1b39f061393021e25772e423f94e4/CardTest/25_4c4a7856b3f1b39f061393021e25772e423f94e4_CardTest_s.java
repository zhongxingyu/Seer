 package models;
 import static org.fest.assertions.Assertions.assertThat;
 import static play.test.Helpers.fakeApplication;
 import static play.test.Helpers.running;
 
 import java.util.List;
 
 import lib.Order;
 import lib.OrderedRunner;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 /**
  * Tests the Card model.
  * 
  * @author Tiago Garcia
  * @see http://github.com/tiagorg
  */
 @RunWith(OrderedRunner.class)
 public class CardTest {
 	
 	private static Long projectId;
 	private static Long storyId;
 	
 	/**
 	 * Empties the database before the tests.
 	 */
 	@BeforeClass
 	public static void init() {
 		running(fakeApplication(), new Runnable() {
 			public void run() {
 				List<Card> cardList = Card.allProjects();
 				for (Card card: cardList) {
 					Card.delete(card.getId());
 				}
 			}
 		});
 	}
 	
 	/**
 	 * Tests create method.
 	 */
 	@Test
 	@Order(order=1)
 	public void testCreateCard() {
 		running(fakeApplication(), new Runnable() {
 			public void run() {
 				// Create parent card
 				Card projectCard = new Card();
 				projectCard.setType(Card.Type.PROJECT);
 				projectCard.setTitle("Test project");
 				projectCard.setDescription("Test project description");
 				projectCard.setAssignee("tester");
 				Card.create(projectCard);
 				
 				projectId = projectCard.getId();
 				assertThat(projectId).isNotNull();
 				assertThat(projectCard.getCreatedDate()).isNotNull();
 				assertThat(projectCard.getModifiedDate()).isNotNull();
 				assertThat(projectCard.getParent()).isNull();
 				
 				// Create children card
 				Card storyCard = new Card();
 				storyCard.setType(Card.Type.STORY);
 				storyCard.setTitle("Test story");
 				storyCard.setDescription("Test story from the Test project");
 				storyCard.setAssignee("tester");
 				storyCard.setParent(projectCard);
 				Card.create(storyCard);
 				
 				storyId = storyCard.getId();
 				assertThat(storyId).isNotNull();
 				assertThat(storyCard.getCreatedDate()).isNotNull();
 				assertThat(storyCard.getModifiedDate()).isNotNull();
 				assertThat(storyCard.getParent()).isNotNull();
 			}
 		});
 	}
 
 	/**
 	 * Tests byId method.
 	 */
 	@Test
 	@Order(order=2)
 	public void testById() {
 		running(fakeApplication(), new Runnable() {
 			public void run() {
 				Card card = Card.byId(projectId);
 				assertThat(card).isNotNull();
 				assertThat(card.getId()).isNotNull();
 				assertThat(card.getChildren().size()).isEqualTo(1);
 			}
 		});
 	}
 	
 	/**
 	 * Tests all method.
 	 */
 	@Test
 	@Order(order=3)
 	public void testAllProjects() {
 		running(fakeApplication(), new Runnable() {
 			public void run() {
 				List<Card> cardList = Card.allProjects();
 				assertThat(cardList.size()).isEqualTo(1);
 			}
 		});
 	}
 	
 	/**
 	 * Tests update method.
 	 */
 	@Test
 	@Order(order=4)
 	public void testUpdateCard() {
 		running(fakeApplication(), new Runnable() {
 			public void run() {
 				Card card = Card.byId(projectId);
 				card.setStatus(Card.Status.IN_PROGRESS);
 				Card.update(card);
 				
 				assertThat(card.getModifiedDate()).isNotEqualTo(card.getCreatedDate());
 			}
 		});
 	}	
 	
 	/**
 	 * Tests delete method.
 	 */
 	@Test
 	@Order(order=5)
 	public void testDeleteCard() {
 		running(fakeApplication(), new Runnable() {
 			public void run() {
 				Card.delete(projectId);
 				
 				Card projectCard = Card.byId(projectId);
 				assertThat(projectCard).isNull();
 				
 				Card storyCard = Card.byId(storyId);
 				assertThat(storyCard).isNull();
 			}
 		});
 	}
 
 }
