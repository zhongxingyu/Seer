 package game;
 
 import game.ai.Strategy;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 /**
  * @author seba
  */
 public class State {
 
   public final StaticConfig staticConfig;
 
   public Board board;
   public int robotCol;
   public int robotRow;
   public int lambdasLeft;
   public int collectedLambdas;
   public Ending ending;
 
   // TODO hashCode equals ...?
 
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
   public Set<Strategy> pendingStrategies = new HashSet<Strategy>();
 
   /**
    * The parent state. The parent state can be transformed into this state by
    * {@link #fromParent}. This field is {@code null} if this is the initial
    * state.
    * 
    * <p>
    * Needed for building the final command list.
    */
   public State parent;
 
   /**
    * The commands to reach this state from its parent state.
    */
   public List<Command> fromParent;
 
   /**
    * Return the commands to reach this state from the initial state.
    */
   public List<Command> fromInitial() {
     // collect all states from here to the initial state
     List<State> states = new ArrayList<State>();
     State state = this;
     while (state != null) {
       states.add(state);
      state = state.parent;
     }
 
     // collect the commands to move from each state to the next
     List<Command> result = new ArrayList<Command>();
     ListIterator<State> iterator = states.listIterator(states.size());
     while (iterator.hasPrevious()) {
       result.addAll(iterator.previous().fromParent);
     }
 
     return result;
   }
 
   /*
    * for flooding
    */
   public int waterLevel;
   public int stepsUnderwater;
   public int stepsUntilNextRise;
 
   public State(StaticConfig sconfig, Board board, int score, int robotCol, int robotRow, int lambdasLeft, int collectedLambdas, int steps, int waterLevel, int stepsUnderwater, int stepsUntilNextRise) {
     this.staticConfig = sconfig;
     this.board = board;
     this.score = score;
     ending = Ending.None;
 
     this.robotCol = robotCol;
     this.robotRow = robotRow;
 
     this.collectedLambdas = collectedLambdas;
     this.lambdasLeft = lambdasLeft;
 
     this.steps = steps;
 
     this.waterLevel = waterLevel;
     this.stepsUnderwater = stepsUnderwater;
     this.stepsUntilNextRise = stepsUntilNextRise;
   }
 
   public State(StaticConfig sconfig, Board board) {
     this(sconfig, board, 0);
   }
 
   /**
    * Auto-initialize state from initial board.
    */
   public State(StaticConfig sconfig, Board board, int waterLevel) {
     this.staticConfig = sconfig;
     this.board = board;
     this.score = 0;
     this.collectedLambdas = 0;
     this.ending = Ending.None;
     this.steps = 0;
     this.waterLevel = waterLevel;
     this.stepsUnderwater = 0;
     this.stepsUntilNextRise = sconfig.floodingRate;
 
     int rcol = -1;
     int rrow = -1;
     int lambdas = 0;
 
     for (int col = 0; col < board.width; ++col)
       for (int row = 0; row < board.height; ++row)
         switch (board.grid[col][row]) {
         case Robot:
           rcol = col;
           rrow = row;
           break;
         case Lambda:
           ++lambdas;
           break;
         default:
           ;
         }
 
     this.robotCol = rcol;
     this.robotRow = rrow;
     this.lambdasLeft = lambdas;
   }
 
   public State makeFinal() {
     /*
      * From now on, you should not change any of the fields stored in this
      * object. We don't enforce that, though.
      */
     return this;
   }
 
   public static State parse(String s) {
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
 
     return new State(new StaticConfig(floodingRate, waterResistance), board, waterLevel);
   }
   
   @Override
   public String toString() {
     return board.toString();
   }
 
   @Override
   public int hashCode() {
     final int prime = 31;
     int result = 1;
     result = prime * result + ((board == null) ? 0 : board.hashCode());
     result = prime * result + collectedLambdas;
     result = prime * result + ((ending == null) ? 0 : ending.hashCode());
     result = prime * result + lambdasLeft;
     result = prime * result + robotCol;
     result = prime * result + robotRow;
     result = prime * result + score;
     result = prime * result + steps;
     result = prime * result + stepsUnderwater;
     result = prime * result + stepsUntilNextRise;
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
     if (lambdasLeft != other.lambdasLeft)
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
     if (stepsUntilNextRise != other.stepsUntilNextRise)
       return false;
     if (waterLevel != other.waterLevel)
       return false;
     return true;
   }
   
   
 }
