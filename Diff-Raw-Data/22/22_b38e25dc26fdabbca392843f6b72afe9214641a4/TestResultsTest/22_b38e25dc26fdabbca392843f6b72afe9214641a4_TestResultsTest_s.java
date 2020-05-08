 package km81m.say_it_right.logic;
 
 import junit.framework.TestCase;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * User: alexeydushenin
  * Date: 11/3/13
  * Time: 3:18 PM
  */
 public class TestResultsTest extends TestCase {
 
     public void testResultCorrectness() {
         System.out.println("testResultCorrectness");
 
         TestBlock testBlock = new TestBlock();
 
         List<SingleTest> singleTests = new ArrayList<SingleTest>();
         for (int i = 0; i < 10; i++) {
             SingleTest singleTest = new SingleTest();
             singleTest.setAnswer(1);
             singleTests.add(singleTest);
         }
 
         testBlock.setSingleTests(singleTests);
 
         UserAnswer userAnswer = new UserAnswer();
         for (int i = 0; i < 9; i++) {
             userAnswer.addAnswer(i, 1);
         }
         userAnswer.addAnswer(9, 2);
 
         TestResults testResults = new TestResults(testBlock, userAnswer);
        assertEquals(0.9, testResults.getCorrectAnswers(), 0.0001);
     }
 
 }
