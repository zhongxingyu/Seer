 package tenpin.frame;
 
 public class TenthFrame extends Frame {
     private boolean hasThirdBall = false;
     private final TenthFrameScore frameScore = new TenthFrameScore();
 
     public TenthFrame() {
         super(10);
     }
 
     public void submitBallScore(int ballScore) {
         if (frameScore.getFirstBallScore() == null) {
             handleFirstBall(ballScore);
 
         } else if (frameScore.getSecondBallScore() == null) {
             handleSecondBall(ballScore);
 
 
         } else if (hasThirdBall && frameScore.getThirdBallScore() == null) {
             handleThirdBall(ballScore);
 
         } else {
             throw new UnsupportedOperationException("submitBallScore when frame finished");
         }
     }
 
     public FrameScore getFrameScore() {
         return this.frameScore;
     }
 
     public boolean isFrameInPlay() {
         return frameScore.getTotalScore() == null && frameScore.getFirstBallScore() != null
                 && (frameScore.getSecondBallScore() == null || (hasThirdBall && frameScore.getThirdBallScore() == null));
     }
 
     private void handleFirstBall(int ballScore) {
         frameScore.setFirstBallScore(ballScore);
     }
 
     private void handleSecondBall(int ballScore) {
         Integer totalScore = frameScore.getFirstBallScore() + ballScore;
          validateTotalScoreForTwoPins(totalScore, frameScore.getFirstBallScore() );
         frameScore.setSecondBallScore(ballScore);
        if (frameScore.isAStrike() || frameScore.hasSpare()) {
             hasThirdBall = true;
         } else {
             frameScore.setTotalScore(frameScore.getFirstBallScore() + frameScore.getSecondBallScore());
         }
     }
 
     private void handleThirdBall(int ballScore) {
         Integer totalScore = frameScore.getFirstBallScore() + frameScore.getSecondBallScore() + ballScore;
         validateTotalScoreForThreePins(totalScore);
         frameScore.setThirdBallScore(ballScore);
         frameScore.setTotalScore(frameScore.getFirstBallScore() + frameScore.getSecondBallScore() + frameScore.getThirdBallScore());
     }
 
     private void validateTotalScoreForThreePins(Integer totalScore) {
         if (totalScore > 30) {
             throw new FrameScoreException("Total score greater than maximum possible for 10th Frame i.e. 30 - totalScore:" + totalScore);
         }
     }
 
     private void validateTotalScoreForTwoPins(Integer totalScore, Integer firstBallScore) {
         if (firstBallScore<10 && totalScore > 10) {
             throw new FrameScoreException("Total score greater than maximum possible i.e. 10 - totalScore:" + totalScore);
         }
     }
 }
