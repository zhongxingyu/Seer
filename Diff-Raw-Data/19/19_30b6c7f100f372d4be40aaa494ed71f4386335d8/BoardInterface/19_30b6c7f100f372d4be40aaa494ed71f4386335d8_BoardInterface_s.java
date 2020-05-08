 package quoridor;
 
 public interface BoardInterface {
 	
 	//given the board and the x and y coordinates of a move checks if it 
 	//is a legal move
 	boolean isLegalMove(Player P,int x,int y);
 	
 	//places a wall ?assume isLegalMove has been called
 	boolean placeWall(Player p, int x, int y,char d);
 	
 	boolean removeWall(Player p, int x, int y, char d);
 	
 	//moves the player ?assume isLegal Move has been called
	boolean movePlayer(Player p, int x, int y);
 
 	Player checkWinner(Player[] players);
 	//displays the board
 	void displayBoard();
 	
 	
 }
