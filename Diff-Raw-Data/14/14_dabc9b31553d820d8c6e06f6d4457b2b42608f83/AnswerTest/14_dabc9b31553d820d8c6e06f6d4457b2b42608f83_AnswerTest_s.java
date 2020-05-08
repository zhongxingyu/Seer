 package team5customtests;
 
 import static org.junit.Assert.*;
 import play.test.*;
 import play.mvc.*;
 import play.mvc.Http.*;
 import models.*;
 import org.junit.Before;
 import org.junit.*;
 
 public class AnswerTest extends UnitTest{
 	public Answer ans;
 	public IUser use1,use2;
	public MockQuestion quest;
 
 	@Before
 	public void setUp() throws Exception {
 		use1 = new MockUser("Hans");
 		use2 = new MockUser("Peter");
		quest = new MockQuestion(use1,"Will this UnitTest work?");
		ans = new Answer(use1,"This UnitTest will work sometime!",quest);
 	}
 	
 	@Test
 	public void shouldNotBeBestAfterConstruction(){
 		assertFalse(ans.isBest());
 	}
 	
 	@Test
 	public void shouldSetProperlyToBest(){
 		assertFalse(ans.isBest());
 		ans.setIsBest(true);
 		assertTrue(ans.isBest());
 		
 	}
 	
 	@Test
 	public void shouldReturnQuestion(){
 		assertEquals(quest,ans.getQuestion());
 	}
 	
 	@Test
 	public void shouldReturnProperTextRepresentation(){
 		ans.rateUp(use2);
 		assertEquals("rating: 1, This UnitTest will work sometime!",ans.toString());
 	}
 
 }
