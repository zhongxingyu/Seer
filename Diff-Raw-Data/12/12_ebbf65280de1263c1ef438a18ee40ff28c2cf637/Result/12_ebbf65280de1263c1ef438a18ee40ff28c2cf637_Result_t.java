 package final_project.model;
 
 public class Result {
 	protected int _player1;
 	protected int _player2;
 	protected int _pointsToWin;
 
 	public int[] getPlayers() {
 		return new int[] {_player1, _player2};
 	}
 
 	public int getPlayer1(){
 		return _player1;
 	}
 	public int getPlayer2(){
 		return _player2;
 	}
}
