 package final_project.model;
 
 public class Result {
 	protected int _player1;
 	protected int _player2;
 	protected int _pointsToWin;
 
 	public int[] getPlayers() {
 		return new int[] {_player1, _player2};
 	}
<<<<<<< HEAD:src/jvm/final_project/model/Result.java
	
	//Where is this used?
	public void setPointsToWin(int toWin) {
		_pointsToWin = toWin;
=======
 
 	public int getPlayer1(){
 		return _player1;
>>>>>>> 98c1f13b2de96369628bc02a9b0483514b9db386:src/jvm/final_project/model/Result.java
 	}
 	public int getPlayer2(){
 		return _player2;
 	}
}
