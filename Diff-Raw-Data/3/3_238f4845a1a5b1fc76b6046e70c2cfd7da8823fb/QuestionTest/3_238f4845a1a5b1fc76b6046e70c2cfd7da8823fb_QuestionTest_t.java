 package homeworks.week1;
 
 import org.junit.Test;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import static org.junit.Assert.assertEquals;
 
 /**
  * @see <a href="https://class.coursera.org/algo-004/forum/thread?thread_id=52">Test Cases for Programming Assignment #1</a>
  */
 public class QuestionTest {
 
     @Test
     public void testQuestion1() {
         Question q = new Question(Arrays.asList(1,3,5,2,4,6));
         assertEquals(3, q.getAnswer());
     }
 
     @Test
     public void testQuestion2() {
         Question q = new Question(Arrays.asList(1,5,3,2,4));
         assertEquals(4, q.getAnswer());
     }
 
     @Test
     public void testQuestion3() {
         Question q = new Question(Arrays.asList(5,4,3,2,1));
         assertEquals(10, q.getAnswer());
     }
 
     @Test
     public void testQuestion4() {
         Question q = new Question(Arrays.asList(1,6,3,2,4,5));
         assertEquals(5, q.getAnswer());
     }
 
     @Test
     public void testQuestion5() {
         Question q = new Question(Arrays.asList(1,2,3,4,5,6));
         assertEquals(0, q.getAnswer());
     }
 
     @Test
     public void testQuestion6() {
         Question q = new Question(Arrays.asList(6,5,4,3,2,1));
         assertEquals(15, q.getAnswer());
     }
 
     @Test
     public void testQuestion7() {
         Question q = new Question(Arrays.asList(9, 12, 3, 1, 6, 8, 2, 5, 14, 13, 11, 7, 10, 4, 0));
         assertEquals(56, q.getAnswer());
     }
 
     @Test
     public void testQuestion8() {
         Question q = new Question(Arrays.asList(37, 7, 2, 14, 35, 47, 10, 24, 44, 17, 34, 11, 16, 48, 1, 39,
                 6, 33, 43, 26, 40, 4, 28, 5, 38, 41, 42, 12, 13, 21, 29, 18, 3, 19, 0, 32, 46, 27, 31,
                 25, 15, 36, 20, 8, 9, 49, 22, 23, 30, 45));
         assertEquals(590, q.getAnswer());
     }
 
     @Test
     public void testQuestion9() {
         Question q = new Question(Arrays.asList(4, 80, 70, 23, 9, 60, 68, 27, 66, 78, 12, 40, 52, 53, 44, 8,
                 49, 28, 18, 46, 21, 39, 51, 7, 87, 99, 69, 62, 84, 6, 79, 67, 14, 98, 83, 0, 96, 5, 82, 10,
                 26, 48, 3, 2, 15, 92, 11, 55, 63, 97, 43, 45, 81, 42, 95, 20, 25, 74, 24, 72, 91, 35, 86, 19,
                 75, 58, 71, 47, 76, 59, 64, 93, 17, 50, 56, 94, 90, 89, 32, 37, 34, 65, 1, 73, 41, 36, 57,
                 77, 30, 22, 13, 29, 38, 16, 88, 61, 31, 85, 33, 54));
         assertEquals(2372, q.getAnswer());
     }
 
     @Test
     public void testQuestion10() {
         Question q = new Question(Arrays.asList(89, 88, 87, 86, 85, 81, 82, 84, 83, 80, 79, 78, 77, 76, 75, 95, 94, 93, 92, 91));
         assertEquals(110, q.getAnswer());
     }
 
     @Test
     public void testMainQuestion() throws IOException {
         InputStream is = Question.class.getResourceAsStream("IntegerArray.txt");
         BufferedReader br = new BufferedReader(new InputStreamReader(is));
 
         List<Integer> array = new ArrayList<Integer>();
         int count = 0;
         while (true) {
             String strLine = br.readLine();
             if (strLine == null)
                 break;
             array.add(Integer.parseInt(strLine));
             count++;
         };
         assertEquals(100000, count);
 
        br.close();
        is.close();

         Question q = new Question(array);
         assertEquals(2407905288L, q.getAnswer());
     }
 }
