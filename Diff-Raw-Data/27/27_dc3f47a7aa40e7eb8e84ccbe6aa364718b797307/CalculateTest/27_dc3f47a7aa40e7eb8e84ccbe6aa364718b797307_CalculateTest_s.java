 package fr.ybonnel.codestory;
 
 
 import com.meterware.httpunit.WebConversation;
 import com.meterware.httpunit.WebResponse;
 import org.junit.Test;
 import org.xml.sax.SAXException;
 
 import java.io.IOException;
 
 import static org.junit.Assert.assertEquals;
 
 public class CalculateTest extends WebServerTestUtil {
 
     @Test
     public void should_answer_to_1plus1() throws IOException, SAXException {
         test_calculate("1+1", "2");
     }
 
     @Test
     public void should_answer_to_allPlus() throws IOException, SAXException {
         for (int number = 1; number < 10; number++) {
             test_calculate(Integer.toString(number) + "+" + number, Integer.toString(number + number));
         }
     }
 
     @Test
     public void should_answer_to_allMultiple() throws IOException, SAXException {
         for (int number = 1; number < 10; number++) {
             test_calculate(Integer.toString(number) + "*" + number, Integer.toString(number * number));
         }
     }
 
     private void test_calculate(String query, String resultExpected) throws IOException, SAXException {
         WebConversation wc = new WebConversation();
         WebResponse response = wc.getResponse(getURL() + "/?q=" + query);
         assertEquals(200, response.getResponseCode());
         assertEquals(resultExpected, response.getText());
     }
 
     @Test
     public void should_answer_to_more_complicated() throws IOException, SAXException {
         test_calculate("1+2*2", "5");
     }
 
     @Test
     public void should_answer_with_parenthesis() throws IOException, SAXException {
         test_calculate("(1+2)*2", "6");
         test_calculate("(1+2+3+4+5+6+7+8+9+10)*2", "110");
     }
 
     @Test
     public void should_answer_divide() throws IOException, SAXException {
         test_calculate("(1+2)/2", "1,5");
     }
 
     @Test
     public void should_answer_with_double_parenthesis() throws IOException, SAXException {
         test_calculate("((1+2)+3+4+(5+6+7)+(8+9+10)*3)/2*5", "272,5");
     }
     @Test
     public void should_answer_with_coma() throws IOException, SAXException {
         test_calculate("1,5*4", "6");
     }
 
     @Test
     public void should_answer_to_a_big_calculate() throws IOException, SAXException {
         test_calculate("((1,1+2)+3,14+4+(5+6+7)+(8+9+10)*4267387833344334647677634)/2*553344300034334349999000", "31878018903828899277492024491376690701584023926880");
 
        test_calculate("((1,1+2)+3,14+4+(5+6+7)+(8+9+10)*4267387833344334647677634)/2*553344300034334349999000/31878018900000000000000000000000000000000000000000", "1,0000000001201109544951058577663585832677");
 
 
     }
 
     @Test
     public void should_answer_to_minus() throws IOException, SAXException {
         test_calculate("(-1)+(1)", "0");
     }
 

 
 
 }
