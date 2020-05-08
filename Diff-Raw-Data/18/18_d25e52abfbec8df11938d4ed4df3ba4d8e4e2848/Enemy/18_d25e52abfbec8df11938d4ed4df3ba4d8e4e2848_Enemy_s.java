 package game;
 
 import game.gameplayStates.GamePlayState;
 import game.player.Player;
 
 import org.newdawn.slick.*;
 import org.newdawn.slick.util.pathfinding.AStarPathFinder;
 import org.newdawn.slick.util.pathfinding.Path;
 import org.newdawn.slick.util.pathfinding.Path.Step;
 
 public class Enemy extends MovingObject{
 	protected Animation m_up, m_down, m_left, m_right, m_sprite, m_up_stand, m_down_stand, m_left_stand, m_right_stand;
 	protected AIState m_ai;
 	private boolean m_inTransit, m_patrol, m_lead, m_pause, m_search=false;
 	private int[][] m_patrolPoints;
 	private int[] m_currentSquare, m_destination, m_leadTo;
 	private AStarPathFinder m_finder;
 	private Path m_path;
 	private int m_pathLength, m_currentStep, m_roamCounter, m_searchCounter = 0;
 	private int m_vision = 3;   //default vision
 	private Player m_player;
 	private Direction m_dir;
 	public Animation getAnimation(){return m_sprite;};
 	public  Enemy(GamePlayState room, Player player, float x, float y) throws SlickException{
 		super(room);
 		//set all the important stuff;
 		
 		m_player = player;
 		m_x = x;
 		m_y = y;
 		m_currentSquare = new int[2];
 		m_destination = new int[2];
 		m_leadTo = new int[2];
 		m_currentSquare[0] =(int) (m_x/SIZE);
 		m_currentSquare[1] = (int) (m_y/SIZE);
 		m_currentStep=0;
 		m_path = null;
 		m_inTransit = false;
 		m_patrol = false;
 		m_lead = false;
 		m_pause = false;
 		//make the path finder
 
 		m_finder = new AStarPathFinder(room.getMap(), 50, false);
 		//set sprites- eventually these will be passed in
		System.out.println("ENEMY INITED");
 		Image [] standingUp = {new Image("assets/Sprite2Back.png"), new Image("assets/Sprite2Back.png")};
         Image [] standingDown = {new Image("assets/Sprite2Front.png"), new Image("assets/Sprite2Front.png")};
         Image [] standingLeft = {new Image("assets/Sprite2Left.png"), new Image("assets/Sprite2Left.png")};
         Image [] standingRight = {new Image("assets/Sprite2Right.png"), new Image("assets/Sprite2Right.png")};
 		
 		Image [] movementUp = {new Image("assets/Sprite2Back.png"), new Image("assets/Sprite2Back.png")};
         Image [] movementDown = {new Image("assets/Sprite2Front.png"), new Image("assets/Sprite2Front.png")};
         Image [] movementLeft = {new Image("assets/Sprite2Left.png"), new Image("assets/Sprite2Left.png")};
         Image [] movementRight = {new Image("assets/Sprite2Right.png"), new Image("assets/Sprite2Right.png")};
         int [] duration = {300, 300}; 
         
         //turn sprites into animations
         m_up = new Animation(movementUp, duration, false);
         m_down = new Animation(movementDown, duration, false);
         m_left = new Animation(movementLeft, duration, false);
         m_right = new Animation(movementRight, duration, false);	
         
         m_up_stand = new Animation(standingUp, duration, false);
         m_down_stand = new Animation(standingDown, duration, false);
         m_left_stand = new Animation(standingLeft, duration,false);
         m_right_stand = new Animation(standingRight,duration,false);
         
         // Original orientation of the sprite. It will look right.
         m_dir = Direction.RIGHT;
         m_sprite = m_right_stand;
         //set ai, this should also be passed in
         m_ai = AIState.PATROL;
 	}
 	/*
 	 * Set the square that the AI needs to lead the main character to.
 	 */
 	public void setLeadTo(int x, int y){
 		m_leadTo[0] = x;
 		m_leadTo[1] = y;
 	}
 	public void setPatrolPoints(int[][] patrolPoints){
 		m_patrolPoints = patrolPoints;
 	}
 	//update the enemy, moving it, and perhaps figuring out where to move it next
 	public void update(int delta){
 		//if patrolling does a check to see if the player is insight.
 		
 		if(m_search){
 			if(inSight()){
 				m_searchCounter = 0;
 			}else{
 				m_searchCounter+=delta;
 				if(m_searchCounter>1500){
 					m_ai= AIState.PATROL;
 				}
 			}
 		}
 		if(m_patrol){
 			if(inSight()){
 				m_patrol=false;
 				m_inTransit=false;
 				m_ai = AIState.HUNT;
 				m_search=true;
 			}
 		}
 		
 		if(m_lead){
 			float xDistance = m_x - m_player.getX();
 			float yDistance = m_y - m_player.getY();
 			float dist = xDistance*xDistance+yDistance*yDistance;
 			if(m_pause){
 				if(dist<2*SIZE*SIZE){
 					m_pause=false;
 				}
 			}else{
 			
 				if(dist>16*SIZE*SIZE){
 					m_pause=true;            //just wait until player is in range
 				}
 				if(!m_inTransit){
 					m_lead = false;
 					m_ai = AIState.WAIT;
 				}
 			}
 		}
 		//if it's on it's way to a destination
 		if(m_inTransit&&!m_pause){
 			//when you've moved a square
 			if(Math.abs(m_x/64-m_currentSquare[0])>=1||Math.abs(m_y/64-m_currentSquare[1])>=1){
 				//the new current is now the old destination
 				setCurrent(m_currentStep);
 				m_currentStep+=1;
 				if(m_currentStep>=m_pathLength){
 					m_inTransit = false;
 				}else{
 					setDestination();
 				}
 			}
 			int x = m_destination[0]-m_currentSquare[0];
 			int y = m_destination[1]-m_currentSquare[1];
 			if(!checkCollision(this, m_game.getPlayer())){
				m_x+= x * delta*0.05f;
				m_y+= y * delta*0.05f;
 			} else {
 				this.onPlayerContact();
 			}
 			
 		}else{
 			//if it's reached it's destination
 			switch(m_ai){
 				case PATROL:{
 					m_patrol = true;
 					roamUpdate();
 					break;
 				}
 				case ROAM:{
 					roamUpdate();
 					break;
 				}
 				case HUNT:{
 					huntUpdate();
 					break;
 				}case LEAD:{
 					leadUpdate();
 					break;
 				}
 				case WAIT:{
 					float xDistance = m_x - m_player.getX();
 					float yDistance = m_y - m_player.getY();
 					float dist = xDistance*xDistance+yDistance*yDistance;
 					if(dist<2*SIZE*SIZE){
 						arriveEvent();
 						return;            //just wait until player is in range
 					}
 					break;
 				}
 			}
 		}
 	}
 	//update for roam (a state where it patrols, but also just ignores the player
 	public void roamUpdate(){
 		//finds the path from current point to next point in the roam list
 		if(m_roamCounter>=m_patrolPoints.length){
 			this.arriveEvent();
 			m_roamCounter=0;
 		}
 		//get the next patrol point
 		int xDest = m_patrolPoints[m_roamCounter][0];
 		int yDest = m_patrolPoints[m_roamCounter][1];
 		//if you've arrived start moving to next point
 		if(xDest==m_currentSquare[0]&&yDest==m_currentSquare[1]){
 			m_roamCounter+=1;
 			roamUpdate();
 		}else{
 			//set the next point and go in transit
 			m_path = m_finder.findPath(null, m_currentSquare[0], m_currentSquare[1], xDest, yDest );
 			if(m_path==null){
 				System.out.println(m_game.blocked(xDest, yDest));
 				System.out.println(xDest + " " + yDest);
 			}else{
 				m_pathLength = m_path.getLength();
 				m_currentStep=1;
 				setDestination();
 				m_inTransit=true;
 			}
 		}
 	}
 	//update if it's leading the player to a destination
 	//simply finds the path to the next destination and sets m_lead to true
 	public void leadUpdate(){
 		int xDest = m_leadTo[0];
 		int yDest = m_leadTo[1];
 		m_path = m_finder.findPath(null,  m_currentSquare[0], m_currentSquare[1], xDest, yDest);
 		m_pathLength = m_path.getLength();
 		m_currentStep = 1;
 		setDestination();
 		m_lead = true;
 		m_inTransit = true;
 	}
 	//update for hunting
 	public void huntUpdate(){
 		int playerX = (int)((m_game.getPlayer().getX()+SIZE/2)/SIZE);
 		int playerY = (int)((m_game.getPlayer().getY()+SIZE/2)/SIZE);
 		if(m_currentSquare[0]==playerX&&m_currentSquare[1]==playerY){
 			return;
 		}
 		m_path = m_finder.findPath(null, m_currentSquare[0], m_currentSquare[1], playerX, playerY);
 		m_pathLength = m_path.getLength()/2;
 		m_currentStep=1;
 		setDestination();
 		m_inTransit = true;
 		
 	}
 	private void setDestination(){
 		m_destination[0] = m_path.getX(m_currentStep);
 		m_destination[1] = m_path.getY(m_currentStep);
 		updateSprite();
 	}
 	private void setCurrent( int i){
 		m_currentSquare[0] = m_path.getX(i);
 		m_currentSquare[1] = m_path.getY(i);
 	}
 	protected void updateSprite(){
 		int xDiff = m_destination[0]-m_currentSquare[0];
 		int yDiff = m_destination[1]-m_currentSquare[1];
 		if(xDiff>0){
 			m_dir = Direction.RIGHT;
 			m_sprite=m_right;
 		}else if(xDiff<0){
 			m_dir = Direction.LEFT;
 			m_sprite=m_left;
 		}else if(yDiff>0){
 			m_dir = Direction.DOWN;
 			m_sprite=m_down;
 		}else if(yDiff<0){
 			m_dir = Direction.UP;
 			m_sprite = m_up;
 		}
 	}
 	/*
 	 * checks to see if player is "in sight of enemy"
 	 */
 	private boolean inSight(){
 		//check to see that player is in sight of enemy
 		float playerX = (int) m_game.getPlayer().getX()+SIZE/2;
 		float playerY = (int) m_game.getPlayer().getY()+SIZE/2;
 		int playerSquareX = (int) (playerX/SIZE);
 		int playerSquareY = (int) (playerY/SIZE);
 		Path path = m_finder.findPath(null, m_currentSquare[0], m_currentSquare[1], playerSquareX, playerSquareY);
 		//make sure player isn't behind enemy
 		if(inSightRange(playerX, playerY)){
 			boolean result = lineOfSight(playerX, playerY);
 			if(!result){
 				System.out.println("NOT SEEN");
 			}else{
 				System.out.println("Potentially SEEN");
 			}
 			return result;
 		}
 		return false;
 	}
 	/**
 	 * 
 	 * @param playerX players x position (center pixel)
 	 * @param playerY player's y position (center pixel
 	 * @returns whether the player is "visible" to the enemy
 	 */
 	private boolean inSightRange(float playerX, float playerY){
 		//if colliding, then player definitely in sight range
 		if(checkCollision(this, m_player)){
 			return true;
 		}
 		float centerX = m_x+SIZE/2;
 		float centerY = m_y +SIZE/2;
 		//need a seperate in range call for each direction
 		//this switch statement checks to see that the player is in the "cone of visibility" for the enemy
 		switch(m_dir){
 		case RIGHT:
 			if(playerX>centerX&&playerX<centerX+SIZE*m_vision){
 				float dist = (playerX-centerX)*0.5f;
 				if(playerY<centerY+SIZE+dist&&playerY>centerY-SIZE-dist){
 					return true;
 				}
 			}
 			break;
 		case LEFT:
 			if(playerX<centerX&&playerX>centerX-SIZE*m_vision){
 				float dist = (centerX - playerX)* 0.5f;
 				if(playerY<centerY+SIZE+dist&&playerY>centerY-SIZE-dist){;
 					return true;
 				}
 			}
 			break;
 		case UP:
 			if(playerY< centerY&&playerY>centerY-SIZE*m_vision){
 				float dist = (centerY-playerY) * 0.5f;
 				if(playerX<centerX+SIZE+dist&&playerX>centerX-SIZE-dist){
 					return true;
 				}
 			}
 			break;
 		case DOWN:
 			if(playerY>centerY&&playerY<centerY+SIZE*m_vision){
 				float dist = (playerY-centerY)*0.5f;
 				if(playerX<centerX+SIZE+dist&&playerX>centerX-SIZE-dist){
 					return true;
 				}
 			}
 			break;
 		}
 		
 		return false;
 	}
 	private boolean lineOfSight(float playerX, float playerY){
 		float centerX = m_x+SIZE/2;
 		float centerY = m_y+SIZE/2;
 		//find equation of line
 		float xDiff = playerX - centerX;
 		float yDiff = playerY - centerY;
 		//not enough room for a block to be between the two of them
 		if(Math.abs(xDiff)<SIZE&&Math.abs(yDiff)<SIZE){
 			return true;
 		}
 		int start;
 		int end;
 		float slope = yDiff/xDiff;
 		float intersect = centerY-slope*centerX;
 		//trace along line, checking for intersections w/ objects
 		if(xDiff>yDiff){
 			if(playerX<centerX){
 				start=(int) playerX;
 				end = (int) centerX;
 			}else{
 				start=(int) centerX;
 				end = (int) playerX;
 			}
 			for(int i = start; i<end; i++){
 				int y =(int) (slope*i+intersect);
 				if(m_game.blocked(i/SIZE, y/SIZE)){
 					return false;
 				}
 			}
 		}else{
 			if(playerY<centerY){
 				System.out.println("along y");
 				start = (int) playerY;
 				end = (int) centerY;
 			}else{
 				start = (int) centerY;
 				end = (int) playerY;
 			}
 			for(int i = start; i<end; i++){
 				int x = (int) (((float)i-intersect)/slope);
 				if(m_game.blocked(x/SIZE, i/SIZE)){
 					return false;
 				}
 			}
 		}
 		return true;
 	}
 	//this is the event that fires when the ai doing the leading arrives at it's spot
 	protected void arriveEvent(){
 		m_ai = AIState.WAIT;
 	}
 	
 	/**
 	 * This runs whenever an enemy makes contact with a player. Default
 	 * action is nothing.
 	 */
 	protected void onPlayerContact() {
 		
 	}
 }
