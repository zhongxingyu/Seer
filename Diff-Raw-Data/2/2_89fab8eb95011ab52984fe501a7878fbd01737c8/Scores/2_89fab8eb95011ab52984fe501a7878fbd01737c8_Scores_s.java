 package breakout.br3akout;
 
 import java.util.ArrayList;
 
 /**
  *
  * @author smu
  */
 public class Scores {
 
     private ArrayList<Score> scores;
 
     public Scores() {
         scores = new ArrayList<Score>();
         Score score = new Score();
         score.setName("SMU");
        score.setScore(835);
         addScore(score);
         Score score2 = new Score();
         score2.setName("OLGA");
         score2.setScore(716);
         addScore(score2);
         Score score3 = new Score();
         score3.setName("CTHULHU");
         score3.setScore(12022);
         addScore(score3);
     }
 
     public ArrayList<Score> getScores() {
         return scores;
     }
 
     public void addScore(Score score) {
         for (Score curScore : scores) {
             if (score.getScore() > curScore.getScore()) {
                 scores.add(scores.indexOf(curScore), score);
                 return;
             }
         }
         scores.add(score);
     }
 }
