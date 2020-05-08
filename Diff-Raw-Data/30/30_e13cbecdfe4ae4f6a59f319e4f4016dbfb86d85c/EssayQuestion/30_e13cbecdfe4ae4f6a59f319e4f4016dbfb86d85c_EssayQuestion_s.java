 package ups.m2glre.rossf1.question;
 
 public class EssayQuestion extends GenericQuestion {
 
     private boolean answerShuffle;
     private int answerFraction;
    private int answerValue;
 
     /**
      * @return the answerShuffle
      */
     public final boolean isAnswerShuffle() {
         return answerShuffle;
     }
     /**
      * @param answerShuffle the answerShuffle to set
      */
     public final void setAnswerShuffle(boolean answerShuffle) {
         this.answerShuffle = answerShuffle;
     }
     /**
      * @return the answerFraction
      */
     public final int getAnswerFraction() {
         return answerFraction;
     }
     /**
      * @param answerFraction the answerFraction to set
      */
     public final void setAnswerFraction(int answerFraction) {
         this.answerFraction = answerFraction;
     }
     /**
      * @return the answerValue
      */
    public final int getAnswerValue() {
        return answerValue;
     }
     /**
      * @param answerValue the answerValue to set
      */
    public final void setAnswerValue(int answerValue) {
        this.answerValue = answerValue;
     }
 }
