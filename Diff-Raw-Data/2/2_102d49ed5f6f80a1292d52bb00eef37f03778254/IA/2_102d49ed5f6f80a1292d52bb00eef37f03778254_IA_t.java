 /**
  * Artificial Intelligence for the n-Puzzle.
  *  
  * @author tdebroc
  * @version 1.0
  */
 
 public class IA {
   static int maxDepth;
   ScoreSequence currentGoalScore = new ScoreSequence (Long.MAX_VALUE, null); 
   Cube cube;
   long savecurrentMinScore = Long.MAX_VALUE;
   int sameCurrentScoreCount = 0;
   boolean randomMode = false;
   int precedentMove = -1;
 
   /**
    * Constructor for the AI.
    * @param cube
    * @param maxDepth
    */
   public IA (Cube cube, int maxDepth) {
     this.cube = cube;
     IA.maxDepth = maxDepth;
   }
   
   /**
    * Gets the move of the IA.
    * @return
    */
   public int getIAMove() {
     if (randomMode || sameCurrentScoreCount > maxDepth) {
       precedentMove = getRandomMove();
     } else {
       findMoveSequence();
       updateCurrentScore();
       precedentMove = getNextMoveFromSequence();      
     }
     return precedentMove;
   }
   
   /**
    * Gets a random move in a Simulated annealing procedure.  
    * @return The index of the move. 
    * @see Cube
    */
   private int getRandomMove() {
     Cube cubeToTestMove = new Cube(cube.grid);
 
     int move;
     do  {
       move = (int) (Math.floor(Math.random() * 4));
       if (Controlleur.printRound)
         System.out.println("Random" + move);
     } while (!cubeToTestMove.move(move));
     if (!randomMode) {
       sameCurrentScoreCount = Cube.SIZE * Cube.SIZE;
     }
     sameCurrentScoreCount--;
     randomMode = (sameCurrentScoreCount > 0); 
     savecurrentMinScore = Long.MAX_VALUE;
     return move;
   }
   
   
   /**
    * Gets the next move in the sequence chosen.
    * @return The next move. 
    */
   public int getNextMoveFromSequence() {
     if (currentGoalScore.getSequence() == null) {
       System.out.println("currentGoalScore.getSequence() == null");
       return 0;
     } else {
       if (currentGoalScore.getSequence().size() == 0) {
         System.out.println("currentGoalScore.getSequence().size() == 0");
         return -1;
       }
       int move = currentGoalScore.getSequence().
           get(currentGoalScore.getSequence().size() - 1);
       currentGoalScore.getSequence().
           remove(currentGoalScore.getSequence().size() - 1);
       return move; 
     }
   }
   
   /**
    * Finds best move sequence which reach to the best game status.
    */
   private void findMoveSequence() {
     ScoreSequence minScore = new ScoreSequence(Long.MAX_VALUE, null);
     if (currentGoalScore.getScore() != 0) {
       minScore = getMinScore(maxDepth, new Cube(cube.grid), -1);
     }
     if (minScore.getScore() < currentGoalScore.getScore()
         || currentGoalScore.getSequence() != null && 
         currentGoalScore.getSequence().size() == 0) {
       currentGoalScore = minScore;
     }
     if (Controlleur.printRound)
       //System.out.format("%,8d%n", n);
     System.out.format("Min score : %,8d%n current : %,8d%n ",
         minScore.getScore() == Long.MAX_VALUE ? 
            0 : minScore.getScore(), cube.getScore());
   }
   
   /**
    * Gets the best score Sequence to follow. 
    * @param depth Depth of the analyze to complete.
    * @param cube Current Cube to analyze.
    * @param move Next move to apply.
    * @return Best score sequence.
    */
   private ScoreSequence getMinScore(int depth, Cube cube, int move) {
     if (move != -1 && !cube.move(move) ) {
       return new ScoreSequence(Long.MAX_VALUE, null);
     }
     if (depth == 0 || cube.getScore() == 0) {
       return new ScoreSequence(cube.getScore(), move);
     }
     ScoreSequence minScore = new ScoreSequence(Long.MAX_VALUE, null);
     for (int i = 0; i < Cube.getMoves().length; i++) {
       if (!(reverse(move) == i || reverse(precedentMove) == i && move == -1)) {
         ScoreSequence score = getMinScore(depth - 1, new Cube(cube.grid), i);                
         
         if (score.getScore() < minScore.getScore()) {
           if (move != -1) {
             score.getSequence().add(move);          
           }        
           minScore = score;
         } else if (score.getScore() == minScore.getScore()) {
           if (score.getSequence() == null) {
             score = new ScoreSequence(score.getScore(), move);
           } else {
             score.getSequence().add(move);  
           }
           
           if (minScore.getSequence() != null && 
               score.getSequence().size() < minScore.getSequence().size()) {
             minScore = score;
           }
         }
       }
     }
     if (move != -1) {
       ScoreSequence currentScore = new ScoreSequence(cube.getScore(), move);
       minScore = (currentScore.getScore() < minScore.getScore() || 
           currentScore.getScore() == minScore.getScore() && 
               currentScore.getSequence().size() < minScore.getSequence().size()) ? 
           currentScore : minScore;      
     }
 
 
     return minScore;
   }
   
   /**
    * Updates current score.
    */
   public void updateCurrentScore() {
     if (savecurrentMinScore == currentGoalScore.getScore()) {
       sameCurrentScoreCount++;
     } else if (savecurrentMinScore > currentGoalScore.getScore()) {
       sameCurrentScoreCount = 0;
     }
     savecurrentMinScore = currentGoalScore.getScore();
   }
   
   /**
    * Reverse the precedent move.
    * @param precedentMove
    * @return 
    */
   public int reverse(int precedentMove) {
     int[] reverseMove = {1,0,3,2};
     return precedentMove >= 0 && precedentMove < 4 ?
         reverseMove[precedentMove] : -1;
   }
 }
