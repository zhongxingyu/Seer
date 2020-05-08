 
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.PriorityQueue;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import sun.rmi.runtime.NewThreadAction;
 
 public class State implements Cloneable, Comparable<State> {
 
     private Position player;
     private PriorityQueue<Position> boxes = new PriorityQueue<>();
     private String current_path;
     private int value;
 
     public State(Position player, PriorityQueue<Position> boxes, String current_path) {
         this.player = player;
         this.boxes = boxes;
         this.current_path = current_path;
         this.value = stateHeuristic();
     }
     
     /**
      * Fills up the given list with all possible new states which involve moving
      * a box
      *
      * @param nextStates
      */
     public void getNextMoves(List<State> nextStates) {
 
         // for each box. Chech which boxes we can reach
         // box : The current box we are looking at
         for (Position box : boxes) {
             // for each empty position adjucent to this box
             // adjucent : the current adjucent position to the box
             for (Position adjucent : Utils.getAdjucentPositions(box, this)) {
                 // find out if we can reach it from our current position w/o
                 // moving any boxes
                 String path_to_adjucent = Utils.findPath(player, adjucent, this);
 
                 // check if there was such path
                 if (path_to_adjucent != null) {
 
                     // try to move the box
                     Movement moved_path = Utils.tryToMoveBox(box, adjucent, this);
 
                     // check if we actually managed to move the box
                     if (moved_path != null) {
 
                         // create a new state
                         State new_state;
                         try {
                             // clone this state
                             new_state = (State) this.clone();
 
                             // update the BoxState we just moved
                             new_state.boxes.remove(box);
                             new_state.boxes.add(moved_path.getBox());
 
                             // update the player state
                             new_state.player = moved_path.getPlayer();
 
                             // update the current path
                             new_state.current_path = new StringBuilder(new_state.current_path).
                                     append(path_to_adjucent).append(moved_path.getPath()).toString();
 
                             new_state.updateValue();
                             // add this state to the list to return
                             nextStates.add(new_state);
 
                         } catch (CloneNotSupportedException ex) {
                             Logger.getLogger(State.class.getName()).log(Level.SEVERE, null, ex);
                         }
                     } // end checking if moved_path is null   
                 } // end checking if path to box is null
             } // end for each adjucent position to a box
         } // end for each box
     }
 
     /**
      * Calculates the hashcode of this state. Based only on the positioning
      * of the boxes
      * @return The hash of this state
      */
     @Override
     public int hashCode() {
         int hash = 3;
         for (Position box : boxes) {
             hash = hash * 91 + box.hashCode();
         }
         return hash;
     }
 
     /**
      * Checks if this State is equal to another. Two states are equal if they
      * share the same box positionings and there is a path between the player
      * positionings which doesn't move any boxes
      * @param obj
      * @return True if the States are "equal"
      */
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final State other = (State) obj;
 
         // if we don't have exactly the same box positions
         // return false
         if (!this.boxes.containsAll(other.boxes)) {
             return false;
         }
 
         // if there is no path between player positions which doesn't move any
         // boxes then it's a different state
         if (Utils.findPath(this.player, other.player, this) == null) {
             return false;
         }
 
         // if we get here the states are equal
         return true;
     }
 
     @Override
     protected Object clone() throws CloneNotSupportedException {
         State result = (State) super.clone();
         result.player = new Position(player.getRow(), player.getCol());
         result.current_path = new String(current_path);
         result.boxes = new PriorityQueue<>();
         for(Position pos : boxes) {
             result.boxes.add(new Position(pos.getRow(), pos.getCol()));
         }
         return result;
    }
     
     
 
     /**
      * Returns a PriorityQueue containing the Positionings of the boxes
      * @return The positioning of this State's boxes
      */
     public PriorityQueue<Position> getBoxes() {
         return this.boxes;
     }
 
     /**
      * Returns this State's positioning of the player
      * @return The positioning of the player
      */
     public Position getPlayer() {
         return player;
     }
 
     /**
      * Returns the total path the player has taken up to this state
      * @return The total path taken by the player
      */
     public String getCurrent_path() {
         return current_path;
     }
 
     public void setPlayer(Position player) {
         this.player = player;
     }
 
     public void setBoxes(PriorityQueue<Position> boxes) {
         this.boxes = boxes;
     }
 
     public void setCurrent_path(String current_path) {
         this.current_path = current_path;
     }
     
     /**
      * (FUTURE WORK, START WITH BFS) Estimate how good is a state
      *
      * @param state
      * @return
      */
     public int stateHeuristic() {
     	int sum=0;
     	List<Position> boxesClone = new ArrayList<Position>();
     	List<Position> goalsClone = new ArrayList<Position>();
     	boxesClone.addAll(boxes);
     	goalsClone.addAll(Main.goals);
 //    	for(Position goal : Main.goals) {
 //    		int min=Integer.MAX_VALUE;
 //    		Position b = null;
 //    		
 //    		for(Position box: boxesClone){
 //    			int d = Position.manhattanDistance(box, goal);
 //    			if(d<min) {
 //    				min=d;
 //    				b=box;
 //    			}
 //    		}
 //    		
 //    		boxesClone.remove(b);
 //    		sum+=min;
 //    	}
     	
     	// With this new implementation two close goals won't take the same box
     	// It's a little more efficient because it'll assign always boxes and goals that are in the same position
     	int min=Integer.MAX_VALUE;
     	for(Position box: boxesClone){
     		if(Main.goals.contains(box)) continue; // We don't have to add any punctuation
     		Position g = null;
     		for(Position goal : goalsClone) {
     			if(boxesClone.contains(goal)) continue; //There is another box in this goal
     			int d = Position.manhattanDistance(box, goal);
     			if(d<min) {
     				min=d;
     				g=goal;
     			}
 			}
     		goalsClone.remove(g);
 		}
     	
         return sum;
     }    
     
     public void updateValue(){
     	this.value = stateHeuristic();
     }
 
     /**
      * Returns true if all boxes are standing on a goal, else false
      * @return True if the game is finished
      */
     public boolean finished() {
         for (Position box : boxes) {
             if (!Main.isGoal(box)) {
                 return false;
             }
         }
         return true;
     }
 
 	@Override
 	public int compareTo(State o) {
 		// TODO Auto-generated method stub
 		return this.value - o.value;
 	}
 }
