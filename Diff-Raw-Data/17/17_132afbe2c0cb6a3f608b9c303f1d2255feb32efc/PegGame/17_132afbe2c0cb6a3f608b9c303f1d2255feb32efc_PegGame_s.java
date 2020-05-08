 import java.util.Iterator;
 
 
 public class PegGame extends AbstractGame<PegState>{
 
 	public PegGame() {
 		super(9000, 1);
 	}
 
 	@Override
 	public void addChildren() {
 		//create state we want to move to
 		PegState goalstate = new PegState();
 		
 		//create integers to hold the values of other holes on the board
 		int start, end, jumpOver;
 		
 		PegJumpNode node;
 		//iterates through all nodes
 		for (int i = 0; i < 15; i++) {
 			if (currentState.getState()[i]) {
 				Iterator<PegJumpNode> iter = currentState.getMoves().neighbors(i);
 				while (iter.hasNext()) {
 					node = iter.next();
					
 					//checks if the move is possible
 					if (((PegState)currentState).moveIsValid(i,node.getJumpTo(),node.getJumped())) {
 						start = i;
 						end = node.getJumpTo();
 						jumpOver = node.getJumped();
 						
 						//creates a new state that includes that move
 						goalstate = ((PegState) currentState).clone();
 						boolean[] temp = goalstate.getState();
 						temp [start] = false;
 						temp [end] = true;
 						temp [jumpOver] = false;
 						goalstate.setState(temp);
 						
 						//adds the new state
 						if (addNewState(goalstate)){
 							goalstate = new PegState();
 						}
 					}
 				}
 			}
 		}
 	}
 
 
 	@Override
 	public boolean goalState() {
 		int count = 0; // how many trues are in the array
 		for (int i = 0; i < currentState.getState().length; i++) {
 			if (currentState.getState()[i]) {
 				count++;
 			}
 		}
 		if (count == 0) {
 			System.out.println("ERROR, ZERO PEGS");
 		}
 		else if (count == 1) {
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public PegState makeInitialState() {
 		PegState ret = new PegState();
 		return null;
 	}
 
 	@Override
 	public void prettyPicture(PegState st) {
         PegState ourState = (PegState)st;
         boolean[] checking = ourState.getState();
         
         for(int i = 0; i < checking.length; i++){
                 if(i == 0) System.out.print("    ");
                 if(i == 1) System.out.print("   ");
                 if(i == 3) System.out.print("  ");
                 if(i == 6) System.out.print(" ");
                 if(checking[i]) System.out.print("O ");
                 else System.out.print(". ");
                 if(i == 0 || i == 2 || i == 5 || i == 9) System.out.println();
             }
         System.out.println("\n");
         }
 
 	public static void main(String[] args) {
 		PegGame game = new PegGame();
 		PegState.setMoves("PegBoard.txt");
 		game.startGame(new PegState());
		game.search();
		game.printSolution();
 	}
 }
