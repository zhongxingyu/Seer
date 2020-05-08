 package km81m.say_it_right.logic;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * User: alexeydushenin
  * Date: 11/3/13
  * Time: 2:27 PM
  * Results of test
  */
 public class TestResults {
 
    private int correctAnswers;
     /*
         for extended results analysis:
         table with correct and users answers
      */
     private TestBlock testBlock;
     private UserAnswer userAnswer;
 
     public TestResults(TestBlock testBlock, UserAnswer userAnswer) {
         this.testBlock = testBlock;
         this.userAnswer = userAnswer;
         this.correctAnswers = calculateResult(testBlock, userAnswer);
     }
 
    private int calculateResult(TestBlock testBlock, UserAnswer userAnswer) {
         List<SingleTest> singleTests = testBlock.getSingleTests();
        int res = 0;
         for (int i = 0; i < 10; i++) {
             if (singleTests.get(i).getAnswer() == userAnswer.getAnswer(i)) {
                 res++;
             }
         }
        return res;
     }
 
    public int getCorrectAnswers() {
         return correctAnswers;
     }
 
     public TestBlock getTestBlock() {
         return testBlock;
     }
 
     public UserAnswer getUserAnswer() {
         return userAnswer;
     }
 }
