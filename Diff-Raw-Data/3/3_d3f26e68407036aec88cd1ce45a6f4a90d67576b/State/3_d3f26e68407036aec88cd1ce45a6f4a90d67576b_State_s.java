 package game;
 
 import game.ai.Solution;
 import game.ai.Strategy;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Set;
 import java.util.StringTokenizer;
 import java.util.TreeSet;
 
 import util.Pair;
 
 /**
  * @author seba
  */
 public class State {
 
   public Board board;
   public int robotCol;
   public int robotRow;
   public int collectedLambdas;
 
   /**
    * Positions of lambdas in board.
    */
   public List<Integer> lambdaPositions;
 
   /**
    * Ending configuration.
    */
   public Ending ending;
 
   /**
    * Set of active positions, that is, positions that might require board
    * update.
    */
   public Set<Integer> activePositions;
 
   /**
    * How much score we got so far.
    */
   public int score;
 
   /**
    * Whether our scorer thinks we should explore this state.
    */
   public int fitness;
 
   /**
    * steps taken since start of game.
    */
   public int steps;
 
   /**
    * not yet applied strategies. gets set when state is created in driver.
    */
   public List<Strategy> pendingStrategies;
 
   /**
    * The sequence of commands that leads to this state.
    */
   public Solution solution;
 
 // Try to avoid using it.
 //  /**
 //   * The previous state.
 //   */
 //  public State previousState;
   
   /*
    * for flooding
    */
   public int waterLevel;
   public int stepsUnderwater;
   public int stepsSinceLastRise;
 
   /*
    * used by NextLambdaStrategy
    */
   public int nextLambdaStrategyIndex = 0;
 
   /**
    * Position of next lambda at index position: nextLambda[robot] == position of
    * next lambda.
    */
   private int[] nextLambda;
   private boolean nextLambdaCleared;
 
   public int nextLambda(int position) {
     if (nextLambdaCleared)
       fillNextLambda(lambdaPositions);
     return nextLambda[position];
   }
 
   public int nextLambda(int col, int row) {
     if (nextLambdaCleared)
       fillNextLambda(lambdaPositions);
     return nextLambda[board.position(col, row)];
   }
 
   public int[] getNextLambda() {
     if (nextLambdaCleared)
       fillNextLambda(lambdaPositions);
     nextLambdaShared = true;
     return nextLambda;
   }
 
   /**
    * Whether the {@code nextLambda} and {@code distanceToNextLambda} fields are
    * shared. Used to implement copy-on-write.
    */
   public boolean nextLambdaShared;
 
   public State(State previousState, Board board, Set<Integer> activePositions, int score, int robotCol, int robotRow, List<Integer> lambdaPositions, int collectedLambdas, int steps, int waterLevel, int stepsUnderwater, int stepsSinceLastRise, int[] nextLambda) {
 //    this.previousState = previousState;
     this.board = board.clone();
     this.score = score;
     this.ending = Ending.None;
     this.activePositions = new TreeSet<Integer>(activePositions);
 
     this.robotCol = robotCol;
     this.robotRow = robotRow;
 
     this.collectedLambdas = collectedLambdas;
     this.lambdaPositions = new ArrayList<Integer>(lambdaPositions);
 
     this.steps = steps;
 
     this.waterLevel = waterLevel;
     this.stepsUnderwater = stepsUnderwater;
     this.stepsSinceLastRise = stepsSinceLastRise;
 
     this.nextLambda = nextLambda;
     this.nextLambdaShared = true;
   }
 
   public State(Board board) {
     this(board, 0);
   }
 
   /**
    * Auto-initialize state from initial board.
    */
   public State(Board board, int waterLevel) {
     this.board = board;
     this.activePositions = new TreeSet<Integer>();
     this.score = 0;
     this.collectedLambdas = 0;
     this.ending = Ending.None;
     this.steps = 0;
     this.waterLevel = waterLevel;
     this.stepsUnderwater = 0;
     this.stepsSinceLastRise = 0;
     this.lambdaPositions = new ArrayList<Integer>();
 
     int rcol = -1;
     int rrow = -1;
 
     for (int col = 0; col < board.width; ++col)
       for (int row = 0; row < board.height; ++row)
         switch (board.get(col, row)) {
         case Robot:
           rcol = col;
           rrow = row;
           break;
         case Lambda:
           lambdaPositions.add(col * board.height + row);
           break;
         case Rock:
           activePositions.add(col * board.height + row);
           break;
         default:
           ;
         }
 
     this.robotCol = rcol;
     this.robotRow = rrow;
 
     this.nextLambda = new int[board.length];
    Arrays.fill(nextLambda, -1);
     this.nextLambdaShared = false;
   }
 
   /**
    * Fills the {@link #nextLambda} array
    * given the positions of all lambdas.
    */
   private void fillNextLambda(List<Integer> positions) {
     nextLambdaCleared = false;
     
     // copy on write
     if (nextLambdaShared) {
       nextLambdaShared = false;
       nextLambda = nextLambda.clone();
     }
 
     int[] queue = new int[2 * board.length];
     int head = 0;
     int tail = 0;
 
     // initialize queue with known positions
     for (int position : positions) {
       queue[tail++] = position;
       queue[tail++] = position;
     }
 
     // process queue
     while (head < tail) {
       int current = queue[head++];
       int lambda = queue[head++];
 
       if (nextLambda[current] < 0) {
         nextLambda[current] = lambda;
 
         int neighbor = board.left(current);
         if (nextLambda[neighbor] == -1 && board.get(neighbor) != Cell.Wall && board.get(neighbor) != Cell.Lift) {
           queue[tail++] = neighbor;
           queue[tail++] = lambda;
           nextLambda[neighbor] = -2;
         }
 
         neighbor = board.up(current);
         if (nextLambda[neighbor] == -1 && board.get(neighbor) != Cell.Wall && board.get(neighbor) != Cell.Lift) {
           queue[tail++] = neighbor;
           queue[tail++] = lambda;
           nextLambda[neighbor] = -2;
         }
 
         neighbor = board.right(current);
         if (nextLambda[neighbor] == -1 && board.get(neighbor) != Cell.Wall && board.get(neighbor) != Cell.Lift) {
           queue[tail++] = neighbor;
           queue[tail++] = lambda;
           nextLambda[neighbor] = -2;
         }
 
         neighbor = board.down(current);
         if (nextLambda[neighbor] == -1 && board.get(neighbor) != Cell.Wall && board.get(neighbor) != Cell.Lift) {
           queue[tail++] = neighbor;
           queue[tail++] = lambda;
           nextLambda[neighbor] = -2;
         }
       }
     }
   }
 
   /**
    * Clear the information about nearest lambdas.
    */
   private void clearNextLambda() {
     nextLambdaCleared = true;
     
     // copy on write
     if (nextLambdaShared) {
       nextLambdaShared = false;
       nextLambda = nextLambda.clone();
     }
 
     // clear data
     Arrays.fill(nextLambda, -1);
   }
 
   /**
    * Remove a lambda and update the information about nearest lambdas.
    */
   public void removeLambda(int col, int row) {
     // TODO make incremental
     lambdaPositions.remove((Object) (col * board.height + row));
     clearNextLambda();
   }
   
   /**
    * Shave a beard
    */
   public void shaveBeard(int col, int row) {
     // TODO: Implement.
   }
 
   public State makeFinal() {
     /*
      * From now on, you should not change any of the fields stored in this
      * object. We don't enforce that, though.
      */
     return this;
   }
 
   /**
    * Return an upper bound on the total score we can achieve starting from this
    * state.
    */
   public int achievableScore() {
     return 75 * (collectedLambdas + lambdaPositions.size()) - steps;
   }
 
   public static Pair<StaticConfig, State> parse(String s) {
     s = s.replace("\r", "").replace("/", "\\");
     
     String[] parts = s.split("\n\n");
     Board board = Board.parse(parts[0]);
 
     int waterLevel = 0;
     int floodingRate = 0;
     int waterResistance = 10;
     if (parts.length > 1) {
       StringTokenizer tokenizer = new StringTokenizer(parts[1], "\n");
       while (tokenizer.hasMoreTokens()) {
         String next = tokenizer.nextToken();
         if (next.startsWith("Waterproof"))
           waterResistance = Integer.parseInt(next.substring(11));
         else if (next.startsWith("Water"))
           waterLevel = Integer.parseInt(next.substring(6));
         else if (next.startsWith("Flooding"))
           floodingRate = Integer.parseInt(next.substring(9));
       }
     }
 
     State st = new State(board, waterLevel);
     StaticConfig sconfig = new StaticConfig(st, floodingRate, waterResistance);
 
     return Pair.create(sconfig, st);
   }
 
   @Override
   public String toString() {
     String s = board.toString();
     if (waterLevel > 0)
       s += " water=" + waterLevel;
     return s;
   }
 
   @Override
   public int hashCode() {
     final int prime = 31;
     int result = 1;
     result = prime * result + ((board == null) ? 0 : board.hashCode());
     result = prime * result + collectedLambdas;
     result = prime * result + ((ending == null) ? 0 : ending.hashCode());
     result = prime * result + robotCol;
     result = prime * result + robotRow;
     result = prime * result + score;
     result = prime * result + steps;
     result = prime * result + stepsUnderwater;
     result = prime * result + stepsSinceLastRise;
     result = prime * result + waterLevel;
     return result;
   }
 
   @Override
   public boolean equals(Object obj) {
     if (this == obj)
       return true;
     if (obj == null)
       return false;
     if (getClass() != obj.getClass())
       return false;
     State other = (State) obj;
     if (board == null) {
       if (other.board != null)
         return false;
     } else if (!board.equals(other.board))
       return false;
     if (collectedLambdas != other.collectedLambdas)
       return false;
     if (ending != other.ending)
       return false;
     if (robotCol != other.robotCol)
       return false;
     if (robotRow != other.robotRow)
       return false;
     if (score != other.score)
       return false;
     if (steps != other.steps)
       return false;
     if (stepsUnderwater != other.stepsUnderwater)
       return false;
     if (stepsSinceLastRise != other.stepsSinceLastRise)
       return false;
     if (waterLevel != other.waterLevel)
       return false;
     return true;
   }
 
   public State clone() {
     nextLambdaShared = true;
     return new State(this, board, activePositions, score, robotCol, robotRow, lambdaPositions, collectedLambdas, steps, waterLevel, stepsUnderwater, stepsSinceLastRise, nextLambda);
   }
 }
