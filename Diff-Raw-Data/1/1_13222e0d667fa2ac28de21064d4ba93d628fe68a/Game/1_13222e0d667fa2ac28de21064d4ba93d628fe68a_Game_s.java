 package game;
 
 import org.newdawn.slick.Color;
 
 import com.sun.jndi.url.corbaname.corbanameURLContextFactory;
 
 import cube.Cube;
 import cube.Direction;
 import cube.Square;
 
 public class Game {
 	private static final Color DEFAULT_BACK_COLOR=Color.white;
 	private static final Color DEFAULT_COLOR=Color.yellow;
 	private static final int DEFAULT_LEVEL=0;
 	private Color selectColor=DEFAULT_COLOR;
 	private Square selectedSquare;
 	private boolean leaveTrail=false;
 	private int points=0,maxPoints,level=DEFAULT_LEVEL;
 	private Color previousColor=DEFAULT_BACK_COLOR;
 	private Cube gameCube;
 	
 	public Game() {
 		setLevel(DEFAULT_LEVEL);//Creating cube
 		selectedSquare = gameCube.getSquare(0, 0, 0);
 		selectedSquare.setBackColor(selectColor);
 		
 	}
 	
 	public void update(int delta) {
 		
 	}
 	
 	public Cube getCube(){
 		return gameCube;
 	}
 	public void setSquare(Square s) {
 		if (!leaveTrail) {	
 			selectedSquare.setBackColor(previousColor);
 		}
 
 		selectedSquare = s;
 		previousColor = selectedSquare.getBackColor();
 		if (leaveTrail) {
 			if (s.isEndSquare()) {//must be the right endSquare since it's the only colored square that is possible to trail on
 				s.unSetEndSquare();
 				points++;
 				System.out.println("Points: "+points+"/"+maxPoints);
 				if (points==maxPoints) {
 					youWin();
 				}
 				leaveTrail=false;
 				selectColor=DEFAULT_COLOR;
 			}
 
 		}
 
 		s.setBackColor(selectColor);
 	}
 	/**
 	 * Moves the player, if possible
 	 * @param d
 	 */
 	public void movePlayer(Direction d){
 		Square neighbor=selectedSquare.getNeighbor(d);
 		if (neighbor.isTraversable()&&!leaveTrail||(leaveTrail&&(neighbor.getBackColor()==DEFAULT_BACK_COLOR||(neighbor.isEndSquare()&&neighbor.getBackColor()==selectColor)))) {
 			setSquare(neighbor);
 		}
 	}
 	
 	public Square getSquare() {
 		if(selectedSquare.getBackColor() != previousColor)
 			selectedSquare.setBackColor(previousColor);
 		
 		return selectedSquare;
 	}
 	
 	public void startTrail(){
 		if (selectedSquare.isEndSquare()) {
 			leaveTrail=true;
 			selectColor=selectedSquare.getTrailColor();
 			selectedSquare.setBackColor(selectColor);
 			selectedSquare.unSetEndSquare();
 		}
 	}
 	private void youWin(){
 		System.out.println("YOU WON THIS LEVEL ("+(level+1)+")");
 		levelUp();
 	}
 	private void setLevel(int level){
 		
 		//System.out.println("Setting new level");
 		this.level=level%Level.numberOfLevels();
 		this.maxPoints=Level.maxpoint(this.level);
 		points=0;
 		gameCube=Level.getLevel(this.level);
 		selectedSquare=gameCube.getSquare(0, 0, 0);
 		selectColor=DEFAULT_COLOR;
 		previousColor=DEFAULT_BACK_COLOR;
 		selectedSquare.setBackColor(selectColor);
 	}
 	public void levelUp(){
 		setLevel(++level);
 	}
 	public void reset(){
 		setLevel(level);
 	}
 }
