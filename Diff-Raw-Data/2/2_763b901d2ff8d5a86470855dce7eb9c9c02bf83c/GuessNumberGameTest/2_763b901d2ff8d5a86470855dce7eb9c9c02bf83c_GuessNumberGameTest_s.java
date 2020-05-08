 import junit.framework.Assert;
 import static junit.framework.Assert.assertEquals;
 
 import org.junit.Test;
 
 /**
  * Created with IntelliJ IDEA.
  * User: STU
  * Date: 12-10-21
  * Time: 下午4:13
  * To change this template use File | Settings | File Templates.
  */
 public class GuessNumberGameTest {
 
     //todo 4a0b
     @Test
     public void should_return_4a0b_if_input_match_exactly(){
         String serverNumber = "9305";
         GuessNumberGame guessNumber = new GuessNumberGame(serverNumber);
         String userInput = "9305";
 
         String result = guessNumber.validate(userInput);
         Assert.assertEquals("4a0b",result);
     }
 
     //todo 3a0b
     @Test
     public void should_return_3a0b_if_input_has_only_three_correct_numbers_in_right_place(){
         String serverNumber = "1234";
         GuessNumberGame guessNumber = new GuessNumberGame(serverNumber);
         String userInput = "1235";
         String result = guessNumber.validate(userInput);
         assertEquals("3a0b",result);
     }
 
     //todo 2a0b
     @Test
     public void should_return_2a0b_if_input_has_only_three_correct_numbers_in_right_place(){
         String serverNumber = "1234";
         GuessNumberGame guessNumber = new GuessNumberGame(serverNumber);
         String userInput = "1256";
         String result = guessNumber.validate(userInput);
         assertEquals("2a0b",result);
     }
 
     //todo 0a1b
     @Test
     public void should_return_0a1b_if_input_include_one_correct_number_with_wrong_place(){
         String serverNumber = "1234";
         GuessNumberGame guessNumber = new GuessNumberGame(serverNumber);
        String userInput = "1234";
         String result = guessNumber.validate(userInput);
         assertEquals("0a1b",result);
     }
 
     //todo 2a1b
     @Test
     public void should_return_2a1b_if_input_include_three_numbers_with_two_in_right_place_and_one_in_wrong_place(){
         String serverNumber = "1234";
         GuessNumberGame guessNumber = new GuessNumberGame(serverNumber);
         String userInput = "1245";
         String result = guessNumber.validate(userInput);
         assertEquals("2a1b",result);
     }
 
     //todo 2a2b
     @Test
     public void should_return_2a2b_if_input_has_four_correct_numbers_with_half_in_right_place(){
         String serverNumber = "1234";
         GuessNumberGame guessNumber = new GuessNumberGame(serverNumber);
         String userInput = "1243";
         String result = guessNumber.validate(userInput);
         assertEquals("2a2b",result);
     }
 }
