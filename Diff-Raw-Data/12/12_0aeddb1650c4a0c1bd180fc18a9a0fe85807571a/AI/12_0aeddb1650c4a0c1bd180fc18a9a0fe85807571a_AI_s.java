 package twelve.team;
 
 import twelve.team.Piece.Team;
 
 public class AI implements GameControllerListener{
 	
 	boolean enabled = false;
 	Team AITeam;
 	GameController controller;
 	
 	public AI(GameController controller, Team AITeam, boolean enabled){
 		this.enabled = enabled;
 		this.AITeam = AITeam;
 		this.controller = controller;
 		this.controller.addGameControllerListener(this);
 	}
 
 	@Override
 	public void onNextTurn() {
 		if(!enabled || controller.getTurn() != this.AITeam)
 			return;
 		
 		//AI's turn, lets calculate a move!
 		boolean moveAgain = true;
 		while(moveAgain){
 			Move move = controller.getBoard().getRandomMove(AITeam);
 			if(move == null)
 				return;
 			try {
				moveAgain = controller.move(move.start, move.end);
 			} catch (Exception e) {
				e.printStackTrace();
 			}
 		}
 	}
 
 	@Override
 	public void onTimeUp() {
 		// do nothing.... game will be over
 	}
 
 	@Override
 	public void onGameWin(Team winner) {
 		// do nothing.... game will be over
 		
 	}
 
 	@Override
 	public void newGame() {
 		//call onNextTurn(), just in case its the AI's turn
 		onNextTurn();
 		
 	}
 	
 }
