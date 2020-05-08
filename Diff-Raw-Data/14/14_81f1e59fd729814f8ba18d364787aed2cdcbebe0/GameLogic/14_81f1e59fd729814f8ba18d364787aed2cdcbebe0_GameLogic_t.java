 package edu.mondragon.eps.jaagirre.game.desktop.tdd.spaceinvaders;
 
 
 
 
 import java.util.ArrayList;
 
 import edu.mondragon.eps.jaagirre.game.desktop.tdd.spaceinvaders.actors.Actor;
 import edu.mondragon.eps.jaagirre.game.desktop.tdd.spaceinvaders.actors.Player;
 import edu.mondragon.eps.jaagirre.game.desktop.tdd.spaceinvaders.actors.Shot;
 import edu.mondragon.eps.jaagirre.game.desktop.tdd.spaceinvaders.utils.RectangleServices;
 import edu.mondragon.eps.jaagirre.game.desktop.tdd.spaceinvaders.utils.Square;
 
 public class GameLogic {
 	protected ArrayList<Actor>  actors;
 	private Player player;
 	private GameBoard board;
 	private boolean gameWin;
 	private boolean gameOver;
 	
 	private ArrayList<Level> levels;
     int actualLevel;
     public static final int DEAD = 0;
     public static final int PLAYIN = 1;
     public static final int LEVEL_DONE = 2;
 	
 	public GameLogic() {
 		super();
 		this.actors = new ArrayList<Actor>();
 		this.gameWin = false;
 		this.gameOver = false;
 		this.actualLevel = 0;
 		this.levels = new ArrayList<Level>();
 	}
 	
 	public void setBoard(GameBoard board) {
 		this.board = board;
 	}
 
 	public void addActor(Actor actor){
 		actors.add(actor);
 		board.getImageBuffer().add(actor);
 	}
 	
 	public void setPlayer(Player player){
 		this.player = player;
 		board.getImageBuffer().add(player);
 		board.getImageBuffer().addAll(player.getMisiles());
 	}
 	
 	public void removeActor(Actor actor){
 		int i = 0;
 		actors.remove(actor);
 		board.getImageBuffer().remove(actor);
 		
 	}
 	
 	public void removePlayer(Player player){
 		player = null;
 		board.getImageBuffer().remove(player);
 	}
 	
 	public void update(){
 		int i = 0;
 		for ( i = 0 ; i < actors.size() ; i++){
 			actors.get(i).executeBehaviour();
 		}
 		player.executeBehaviour();
 		checkCollisions();
 		if ( checkGame() ){
 			this.finishGame();
 		}
 		
 		
 	}
 	/*Adapatdo al API nuevo para textos*/
 	public boolean checkGame(){
 		
 		boolean ret = false;
 		String text = null;
 		int level = 0;
 		level = this.actualLevel+1;
 		if ( actors.size() == 0){
 			this.gameWin= true;
 			text = new String( level +  " maila!!!!!");
 			board.addLevelText( new GameText(text, 200,200) );
 			ret = true;
 		}else if (player.getShields() == 0){
 			board.removeLevelText();
 			this.gameOver = true;
 			text = new String("Game Over.");
 			board.getTextBuffer().add( new GameText(text, 200,200) );
 			//agregamos texto final
 			ret = true;
 		}
 		return ret;
 	
 	}
 	
 	public void checkCollisions(){
 		Actor alien = null;
 		int i  = 0;
 		boolean aurkitua = false;
 		int size =  actors.size();
 		int misilIndex = 0;
 		int alienNumber = 0;
 		//Between Aliens and Misil
 		misilIndex = 0;
 		do{
 			Shot misil = player.getMisiles().get(misilIndex);
 			alienNumber = this.getActorsNumber();
 			i = 0;
 			aurkitua = false;
 			while (!aurkitua &&  i < alienNumber){
 				alien = actors.get(i);
 				RectangleServices rs = new RectangleServices();
 				Square sAlien = new Square( alien.getX() , alien.getY() , alien.getWidth() , alien.getHeight() );
 				Square sMisil = new Square( misil.getX() , misil.getY() , misil.getWidth() , misil.getHeight() );
 				
 				
 				if ( 
 					    rs.intersects( sAlien , sMisil) 
 						&&
 						( misil.isVisible() )
 					){
 						aurkitua = true;
 						this.removeActor(alien);
 						misil.setVisible(false);
 				}
 				i++;
 			}
 			misilIndex++;
 		}while(misilIndex < player.getMisilNumber()  );
 		
 		//Between Player and aliens
 		aurkitua = false;
 		i = 0;
 		size = this.getActorsNumber();
 		while( i < size && !aurkitua ){
 			alien = actors.get(i);
 			RectangleServices rs = new RectangleServices();
 			Square sAlien = new Square( alien.getX() , alien.getY() , alien.getWidth() , alien.getHeight() );
 			Square sPlayer = new Square( player.getX() , player.getY() , player.getWidth() , player.getHeight() );
 			
 			if ( 
 					rs.intersects(sAlien, sPlayer)
 				){
 				aurkitua = true;
 			}
 			i++;			
 		}
 		if (aurkitua){
 			this.removeActor(alien);
 			player.setShields(player.getShields()-1);
 			player.setX(200);
 			player.setY(300);
 		}
 	}
 	
 	public void finishGame(){
 		int i = 0;
 		for (i = 0 ; i < actors.size(); i++){
 			Actor actor = actors.get(i);
 			this.removeActor(actor);
 		}
 		this.removePlayer(this.player);
 		
 	}
 	
 	public Player getPlayer() {
 		return player;
 	}
 
 	public int getActorsNumber(){
 		return actors.size();
 	}
 
 	public boolean isGameWin() {
 		return gameWin;
 	}
 
 	public void setGameWin(boolean gameWin) {
 		this.gameWin = gameWin;
 	}
 
 	public boolean isGameOver() {
 		return gameOver;
 	}
 
 	public void setGameOver(boolean gameOver) {
 		this.gameOver = gameOver;
 	}
 
 	public ArrayList<Actor> getActors() {
 		return actors;
 	}
 	
 	public void addLevel( Level level){
         levels.add(level);
     }
 	
 	public void executeActualLevel(){
 		//Establecemos la pantalla actual
         actors = levels.get( this.actualLevel).getLevel();
 		update();
         board.paintBoard();
         try{
         	Thread.sleep(100);
         }catch(Exception e){}
 	}
	
	/*Con dos niveles cuando ganamos el juego inetnta establecer el siguiente nivel y no existe y 
	 * salta una excepcion.Bug de seguridad*/
 	public void setActorsInBoard(){
         board.getImageBuffer().addAll(levels.get( this.actualLevel).getLevel());
 	}
 
 
 	public void nextLevel(){
         if (this.actualLevel  < levels.size() ){
                this.actualLevel++;
         }
     }
        
     public boolean isGameWon(){
         boolean ret = false;
         if ( this.actualLevel  == levels.size()){
                ret = true;
         }
         return ret;
     }
 	
 	
 }
