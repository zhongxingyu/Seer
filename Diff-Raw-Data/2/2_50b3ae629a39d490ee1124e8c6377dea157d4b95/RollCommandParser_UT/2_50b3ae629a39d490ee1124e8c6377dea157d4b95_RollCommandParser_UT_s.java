 package novemberkilo.irc.bot.gaming;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.pircbotx.User;
 
 import java.util.Random;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.mockito.Matchers.anyInt;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 /**
  * Created with IntelliJ IDEA.
  * User: novemberkilo
  * Date: 9/30/13
  * Time: 8:14 PM
  * To change this template use File | Settings | File Templates.
  */
 public class RollCommandParser_UT {
     private RollCommandParser rollCommandParser;
     private Random generator;
     private User user;
 
     @Before
     public void runBeforeEachUnitTest() {
         rollCommandParser = new RollCommandParser();
         user = mock(User.class);
         generator = mock(Random.class);
         when(user.getNick()).thenReturn("novemberkilo");
     }
 
     @Test
     public void shouldReturnSyntaxWhenUserIsNull() {
         assertEquals(RollCommandParser.SYNTAX, rollCommandParser.parse(null, "meh"));
     }
 
     @Test
     public void shouldReturnSyntaxWhenInputIsNull() {
         assertEquals(RollCommandParser.SYNTAX, rollCommandParser.parse(user, null));
     }
 
     @Test
     public void shouldReturnSyntaxWhenInputIsEmpty() {
         assertEquals(RollCommandParser.SYNTAX, rollCommandParser.parse(user, ""));
     }
 
     @Test
     public void shouldNotReturnNullWhenSomeInputExists() {
         assertNotNull(rollCommandParser.parse(user, "meh"));
     }
 
     @Test
     public void shouldReturnSyntaxWhenInvalidInputExists() {
         assertEquals(RollCommandParser.SYNTAX, rollCommandParser.parse(user, "meh"));
     }
 
     @Test
     public void shouldReturnRollResultsForOneDice() {
         when(generator.nextInt(anyInt())).thenReturn(2);
         rollCommandParser = new RollCommandParser(generator);
 
        assertEquals("novemberkilo rolls '1d6': (3 = 3) = 3", rollCommandParser.parse(user, "1d6"));
     }
 }
