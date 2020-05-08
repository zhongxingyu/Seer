 package ai;
 
 import program.Ant;
 import world.Cell;
 import enums.E_Instruction;
 import enums.E_Terrain;
 
 /**
  * Represents a MOVE instruction in the AI state machine.
  * @author JOH
  * @version 1
  */
 public class State_Move extends State_Abstract {
 
 	private int state1;							//	State to go to if move forward OK
 	private int state2;							//	State to go to if move forward blocked
 	
 	/**
 	 * Constructor.
 	 * @param tokens the tokenized String containing one full instruction
 	 */
 	public State_Move(String[] tokens) {
 		super(E_Instruction.MOVE);
 		super.checkCorrectNumberOfTokens(3, tokens.length);
 		state1 = super.tokenToState(tokens[1]);
 		state2 = super.tokenToState(tokens[2]);
 	}
 
 	/**
 	 * Move forward and goto state1; goto state2 if the cell ahead is blocked.
 	 */
 	@Override
 	public void step(Ant ant, Cell cell) {
 		//	Get the adjacent cell in the ant's forward direction
 		Cell forward = cell.getWorld().adjacentCell(cell.getPosition(), ant.getDirection());
 		if (forward == null || forward.getTerrain() == E_Terrain.ROCKY || forward.getAnt() != null) {
 			//	Can't go in this direction if :
 			//	1) there is no forward cell (shouldn't happen since edges are rocky but pays to be sure)
 			//	2) the forward cell is rocky
 			//	3) the forward cell has an ant already in it
 			ant.setCurrentState(state2);
 		} else {
 			//	We can go in this direction - clear this cell & move ant to new cell
 			cell.setAnt(null);
 			forward.setAnt(ant);
 			ant.setCurrentState(state1);
			//	must rest for 14 turns
			ant.setResting(14);
			cell.getWorld().checkForSurroundedAnts(forward.getPosition());
 		}
 	}
 }
