 package my.triviagame.bll;
 
 import com.sun.tools.javac.util.Pair;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * TODO class-level documentation.
  */
 public class MatchQuestion extends Question{
 
     public MatchQuestion(String questionTitle, MatchAnswer answer) {
         super(questionTitle, answer);
         this.left = new ArrayList<String>(4);
         this.right = new ArrayList<String>(4);
         for (Pair<String, String> pair: answer.getPairs()) {
             this.left.add(pair.fst);
             this.right.add(pair.snd);
         }
         Collections.shuffle(this.right);
     }
 
     private List<String> left;
     private List<String> right;
 
     @Override
     public int getScore(IAnswer answer) {
         int score = 0;
         for (Pair<String, String> actualPair: ((MatchAnswer)answer).getPairs()) {
             for (Pair<String, String> expectedPair: (((MatchAnswer)this.correctAnswer).getPairs())) {
                 if (actualPair.fst.equals(expectedPair.fst) && actualPair.snd.equals(expectedPair.snd)) {
                     score++;
                     break;
                 }
             }
 
         }
         return score;
     }
 
     @Override
     public IAnswer getCorrectAnswer() {
         return this.correctAnswer;
     }
 
     public List<String> getLeftItems() {
         return left;
     }
 
     public List<String> getRightItems() {
         return right;
     }
 
 }
