 package back;
 
 import java.util.Scanner;
 
 public class Game {
 	/***************************/
 	//TODO
 	public static void main(final String[] args){
 		final Game game = new Game();
 		final Scanner systemIn = new Scanner(System.in);
 		String input;
 		do{
 			System.out.println("Level: "+game.getLevel()+"\tTurns: "+game.getTurns()+"\n"+game.board);
 			input = systemIn.nextLine();
 			try{
 				game.swapFirst(Integer.parseInt(input));
 			}catch(Exception e){
 				System.out.println("nope");continue;
 			}
 		}while(!game.isLoss());
 		System.out.println("Game over! You made it to level "+game.getLevel()+".");
 	}
 	/***************************/
 	/***************************/
 	
 	private final static int INIT_TURNS = 2;
 	private final static int INIT_LEVEL= 2;
 	private int turns;
 	private int level;
 	private boolean loss;
 	private PuzzleBoard board;
 	
 	public Game(){
 		turns = INIT_TURNS;
 		level = INIT_LEVEL;
 		loss = false;
 		board = getNewBoard(level);
 	}
 	
 	private static PuzzleBoard getNewBoard(final int level){
 		PuzzleBoard ret;
 		do{ret = new PuzzleBoard(level);}while(ret.isDescending());
 		return ret;
 	}
 	
 	public int getLevel(){
 		return level;
 	}
 
 	public int getTurns(){
 		return turns;
 	}
 	
 	public int[] getNums(){
 		return board.getNums();
 	}
 	
 	public boolean isLoss(){
 		return loss;
 	}
 	
 	public void swapFirst(int index){
 		if(index<=1){return;}
 		if(!loss){
 			board.swapFirst(index);
 			turns--;
 			checkEndgame();
 		}
 	}
 
 	private void checkEndgame() {
 		if(board.isDescending()){
 			turns+=turnIncrease(level);
 			board = getNewBoard(++level);//increment level first!
 		}else if(turns==0){
 			loss=true;
 		}
 	}
 
	//TODO create a play-tested function for this
 	private int turnIncrease(int prevLevel) {
		return (int)Math.round((float)prevLevel*1.5);
 	}
 }
