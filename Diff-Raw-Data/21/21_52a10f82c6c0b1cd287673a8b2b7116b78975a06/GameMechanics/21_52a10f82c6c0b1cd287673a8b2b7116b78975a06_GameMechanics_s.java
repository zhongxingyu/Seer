 //////////////////////////////////////////////////////////////////////////////////////////
 //                               Broomball Simulator                                    //
 //                                    a game by                                         //
 //                        Spencer Allen and Pierce Jensen                               //
 //////////////////////////////////////////////////////////////////////////////////////////
 
 //This class manages all gameplay mechanics.
 
 import java.awt.*;
 import java.io.*;
 import java.util.*;
 
 public class GameMechanics {
 
 	//arraylist operational definitions
 	public static final int AL_ADD = 0;
 	public static final int AL_READ = 1;
 	public static final int AL_REMOVE = 2;
 
 	//player key boolean table index defines
 	private final int KEY_LMOUSE = 0;
 	private final int KEY_RMOUSE = 1;
 	private final int KEY_W = 2;
 	private final int KEY_A = 3;
 	private final int KEY_S = 4;
 	private final int KEY_D = 5;
 	private final int KEY_SHIFT = 6;
 	private final int KEY_CONTROL = 7;
 	private final int KEY_ALT = 8;
 	private final int KEY_SPACE = 9;
 
 	public final int RED_GOAL_RIGHT = -1;
 	public final int BLUE_GOAL_RIGHT = 1;
 
 	static Polygon[] corner;
 
 	static int colCount = 0;
 
 	public Point topLeftWaypoint;
 	public Point bottomRightWaypoint;
 	public static Point base1Waypoint;
 	public static Point base2Waypoint;
 
 	private static int trigScale = 1;//accuracy of trigonometry tables, in entries per degree
 	public static float[] sin;
 	public static float[] cos;
 	public static float[] tan;
 
 	public static boolean debugInfo = false;
 
 	int mapheight = 6000;
 	int mapwidth = 8000;
 
 	public static int blueScore;
 	public static int redScore;
 	public static int goalPosition;
 
 	final int[] playerStartX = {2500, 2500, 2500, 0, 0};
 	final int[] playerStartY = {2750, 5520, -80, 220, 5220};
 
 	ArrayList<Entity> playerList;
 	ArrayList<int[]> playerArrayList;
 	static int[][] playerArray;
 
 	Entity ball;
 	int[] ballArray;
 
 	public Random generator;
 
 	public double period;
 
 	int[] mouseX;
 	int[] mouseY;
 	boolean[][] keyArray;
 
 	int ballPossessor = -1;
 	double chargeTime;
 	final static double maxChargeTime=1;
 	private final int chargeBaseForce=2000;
 	boolean chargeCanceled;
 
 	//initialization
 	public void init(){
 		generator = new Random();
 
 		playerList = new ArrayList<Entity>();
 
 		/*build trigonometry tables*/
 		double toRadian = Math.PI/(180*trigScale);
 		//sine
 		sin = new float[(90*trigScale) + 1];
 		for(int i=0;i<sin.length;i++){
 			sin[i] = ((float)Math.sin(((double)i) * toRadian));
 		}
 
 		//cosine
 		cos = new float[(90*trigScale) + 1];
 		for(int i=0;i<cos.length;i++){
 			cos[i] = ((float)Math.cos(((double)i) * toRadian));
 		}
 
 		//tangent
 		tan = new float[(90*trigScale) + 1];
 		for(int i=0;i<tan.length;i++){
 			tan[i] = sin[i]/cos[i];
 		}
 
 		corner = new Polygon[4];
 		//ordered counter-clockwise, starting from top-right
 		int[][] cornerPointsX = {{7000,7000,6200},{-200,-1000,-1000},{-1000,-1000,-200},{6200,7000,7000}};
 		int[][] cornerPointsY = {{4920,5720,5720},{5720,5720,4920},{1080,280,280},{280,280,1080}};
 		for(int i=0;i<4;i++){
 			corner[i] = new Polygon();
 			corner[i].npoints = 3;
 			corner[i].xpoints = cornerPointsX[i];
 			corner[i].ypoints = cornerPointsY[i];
 		}
 
 
 		//player init
 		for(int i=0; i<10; i++){
 			Entity player = new Entity();
 
 			//team specific init
 			if(i<5){
 				player.x = playerStartX[i];
 				player.y = playerStartY[i];
 			} else {
 				player.x = 6000 - playerStartX[i - 5];
 				player.y = playerStartY[i - 5];
 			}
 
 			//general init
 			playerList.add(player);
 			player.playerInit();
 		}
 
 		ball = new Entity();
 		ball.x = 3000;
 		ball.y = 2750;
 		ball.ballInit();
 
 		GameState.state=GameState.GAME_RUN;
 		GameState.period=1;
 		GameState.periodLength=600;
 		GameState.numOfPeriods=3;
 		GameState.time=GameState.periodLength;//CRITICAL. SETS TIME TO PERIOD LENGTH BEFORE STARTING GAME
 	}//end initialization
 
 	//main entity operation method
 	public synchronized void operateEntities(double p){
 		period = p;
 
 		/////////////////////////////////
 		//      main entity loop       //
 		/////////////////////////////////
 
 
 		playerArrayList = new ArrayList<int[]>();
 
 		//loops through every entity to perform operations on
 		for(int i=0; i<playerList.size(); i++){
 
 			Entity entity = operateEntityList(AL_READ, i, null);
 
 			int[] entityArray = convertEntToArray(entity);
 
 			double turnRate = 540;
 
 			double bearingToTarget =  Math.toDegrees(Math.atan2(mouseY[i] - entity.y, mouseX[i] - entity.x));
 			bearingToTarget = angDisplacement(bearingToTarget, entity.bearing);
 			if(abs(bearingToTarget) < turnRate*period){
 				entity.radv = 0;
 				entity.bearing -= bearingToTarget;
 			}else if(bearingToTarget >= 0){
 				entity.radv = -turnRate;
 			} else{
 				entity.radv = turnRate;
 			}
 			if(GameState.state == GameState.GAME_RUN)
 			{
 				entity.walking = keyArray[i][KEY_W] | keyArray[i][KEY_S];
 				entity.sideWalking = keyArray[i][KEY_A] | keyArray[i][KEY_D];
 			}else{
 				entity.walking =false;
 				entity.sideWalking =false;
 			}
 
 
 			if(keyArray[i][KEY_W]){
 				entity.walkDirection = 1;
 			} else if(keyArray[i][KEY_S]){
 				entity.walkDirection = -1;
 			}
 
 			if(keyArray[i][KEY_A]){
 				entity.sideWalkDirection = 1;
 			} else if(keyArray[i][KEY_D]){
 				entity.sideWalkDirection = -1;
 			}
 
 			//if a player clicks the right button, checks if that player can possess the ball
 			if(keyArray[i][KEY_RMOUSE]){
 				if(ballPossessor == -1){
 					bearingToTarget =  Math.toDegrees(Math.atan2(ball.y - entity.y, ball.x - entity.x));
 					if(abs(angDisplacement(bearingToTarget, entity.bearing)) < 90 && sqr(ball.x - entity.x) + sqr(ball.y - entity.y) < sqr(500)){
 						ballPossessor = i;
 					}
 				} else if(ballPossessor == i && chargeTime > 0){
 					chargeCanceled = true;
 					chargeTime = 0;
 				}
 			}
 
 			//if a player possesses the ball an left clicks, shoot it
 			if(keyArray[i][KEY_LMOUSE]){
 				if(ballPossessor == i && !chargeCanceled &&chargeTime<maxChargeTime){
 					chargeTime += period;
 				}
 			} else if(ballPossessor == i){//if the button is let go with a ball
 				if(chargeTime > 0){
 					ballPossessor = -1;
 					ball.x = entity.x + cos((int)entity.bearing)*300;
 					ball.y = entity.y + sin((int)entity.bearing)*300;
 					double shootVel= chargeBaseForce*(-Math.pow(chargeTime*(2/maxChargeTime),4)+4*Math.pow(chargeTime*(2/maxChargeTime),2)-.25*chargeTime*(2/maxChargeTime)+1);
 					ball.vx = cos((int)entity.bearing)*shootVel;
 					ball.vy = sin((int)entity.bearing)*shootVel;
 					chargeTime = 0;
 				} else {
 					chargeCanceled = false;
 				}
 			}
 
 			//updates every entity's position. also capable of removing the entity
 			if(entity.move(period)){
 				operateEntityList(AL_REMOVE, i, null);
 				i--;
 			} else {
 				playerArrayList.add(entityArray);
 			}
 		}//end entity loop
 
 		if(ballPossessor == -1){
 			ball.move(period);
 		}
 		//////// RULE AREA ///
 
 		//Time Stuff
 		if(GameState.state==GameState.GAME_RUN)
 		{
 			GameState.time -= period;
 
 			if(GameState.time<=0){
 				GameState.state=GameState.GAME_PERIOD_OVER;
 				GameState.delay=GameState.GAME_PERIOD_DELAY;
 				GameState.time=0;
 			}
 		}
 
 		if(GameState.isGameDelayed()&&GameState.delay>0)
 		{
 			GameState.delay-=period;
 		}
 
 		if(GameState.delay<=0&&GameState.isGameDelayed())
 		{
 			if(GameState.state==GameState.GAME_PERIOD_OVER&&GameState.period<GameState.numOfPeriods){
 				GameState.period++;
 				GameState.time=GameState.periodLength;
 				ballPossessor = -1;
 				ball.x = 3000;
 				ball.y = 2750;
 				ball.vx = 0;
 				ball.vy = 0;	
 
 				repositionPlayers();
 
 				GameState.state=GameState.GAME_RUN;
 
 			}else if(GameState.state==GameState.GAME_PERIOD_OVER&&GameState.period>=GameState.numOfPeriods){
 				GameState.state=GameState.GAME_OVER;
 				GameState.time=0;
 				GameState.period=GameState.numOfPeriods;
 			}else if(GameState.isGoalScored()){
 
 				GameState.state=GameState.GAME_RUN;
 				ballPossessor = -1;
 				ball.x = 3000;
 				ball.y = 2750;
 				ball.vx = 0;
 				ball.vy = 0;	
 
 				repositionPlayers();
 			}
 
 		}
 
 		//Goal Stuff
 		Polygon leftGoal = new Polygon();
 		leftGoal.npoints=0;
 		leftGoal.addPoint(-1440, 3430);
 		leftGoal.addPoint(-1440, 2590);
 		leftGoal.addPoint(-1000, 2590);
 		leftGoal.addPoint(-1000, 3430);
 		Polygon rightGoal = new Polygon();
 		rightGoal.npoints=0;
 		rightGoal.addPoint(7000, 3430);
 		rightGoal.addPoint(7000, 2590);
 		rightGoal.addPoint(7420, 2590);
 		rightGoal.addPoint(7420, 3430);
 
 		if(leftGoal.contains(ball.x+2*ball.size, ball.y)&&GameState.state==GameState.GAME_RUN)
 		{
 			if(goalPosition == BLUE_GOAL_RIGHT){
 				GameState.blueScore += 1;
 				GameState.state=GameState.GAME_BLUE_GOAL_SCORED;
 			}else{
 				GameState.redScore += 1;
 				GameState.state=GameState.GAME_RED_GOAL_SCORED;
 			}
 			
 			GameState.delay=GameState.GAME_GOAL_DELAY;
 		}else if(rightGoal.contains(ball.x-2*ball.size, ball.y)&&GameState.state==GameState.GAME_RUN){
 			if(goalPosition == BLUE_GOAL_RIGHT){
 				GameState.redScore += 1;
 				GameState.state=GameState.GAME_RED_GOAL_SCORED;
 			}else{
 				GameState.blueScore += 1;
 				GameState.state=GameState.GAME_BLUE_GOAL_SCORED;	
 			}
 
 		
 			GameState.delay=GameState.GAME_GOAL_DELAY;
 		}
 
 		//////// END RULE AREA ///
 
 		//now check for collisions
 		for(int i=0;i<11;i++){
 			for(int j=i+1;j<11;j++){
 				Entity entity1;
 				Entity entity2;
 
 				if(i != 10){
 					entity1 = playerList.get(i);
 				} else {
 					if(ballPossessor != -1) continue;
 					entity1 = ball;
 				}
 
 				if(j != 10){
 					entity2 = playerList.get(j);
 				} else {
 					if(ballPossessor != -1) continue;//skips ball collision check if possessed
 					entity2 = ball;
 				}
 
 				//check for intersection
 				if(sqr(entity1.x - entity2.x) + sqr(entity1.y - entity2.y) < sqr(entity1.size + entity2.size)){
 					if(entity1.type == entity2.type){
 						twoBodyCollision(entity1, entity2);
 					} else {
 						
 						//if one of the objects is a ball
 						if(entity1.type == 0){
 							ballPlayerCollision(entity2, entity1);
 						} else {
 							ballPlayerCollision(entity1, entity2);
 						}
 					}
 				}
 			}
 
 		}//end for loop 1
 
 		/* building transmit array */
 		playerArray = new int[playerArrayList.size()][5];
 		for(int i=0; i<playerArrayList.size(); i++){
 			playerArray[i] = playerArrayList.get(i);
 		}
 
 		ballArray = convertBallToArray(ball);
 	}
 
 	void repositionPlayers (){
 		for(int i=0; i<10; i++){
 			Entity entity = operateEntityList(AL_READ, i, null);
 
 			//team specific init
 			if(i<5){
 				entity.x = playerStartX[i];
 				entity.y = playerStartY[i];
 			} else {
 				entity.x = 6000 - playerStartX[i - 5];
 				entity.y = playerStartY[i - 5];
 			}
 			entity.vx=0;
 			entity.vy=0;
 		}
 	}	
 	//custom mathematical square function
 	double sqr(double i){
 		return i*i;
 	}
 
 	/*custom trigonometry functions*/
 	//cosine
 	public double cos(int a){
 		//normalizing
 		if(a>360){
 			a %= 360;
 		}else if(a<0){
 			a %= 360;
 			a += 360;
 		}
 
 		//return
 		if(a <= 90){
 			return cos[a];
 		}else if(a <= 180){
 			return -cos[180 - a];
 		}else if(a <= 270){
 			return -cos[a - 180];
 		}else{
 			return cos[360 - a];
 		}
 	}
 
 	//sine
 	public double sin(int a){
 		//normalizing
 		if(a>360){
 			a %= 360;
 		}else if(a<0){
 			a %= 360;
 			a += 360;
 		}
 
 		//return
 		if(a <= 90){
 			return sin[a];
 		}else if(a <= 180){
 			return sin[180 - a];
 		}else if(a <= 270){
 			return -sin[a - 180];
 		}else{
 			return -sin[360 - a];
 		}
 	}
 
 	//tan
 	public double tan(int a){
 		//normalizing
 		if(a>360){
 			a %= 360;
 		}else if(a<0){
 			a += 360;
 		}
 
 		//return
 		if(a <= 90){
 			return tan[a];
 		}else if(a <= 180){
 			return -tan[a - 90];
 		}else if(a <= 270){
 			return tan[a - 180];
 		}else{
 			return -sin[a - 270];
 		}
 	}
 	
 	public void ballPlayerCollision(Entity ball, Entity player){
 		double theta = Math.toDegrees(Math.atan2(player.y - ball.y, player.x - ball.x));
 		
 		//double bearing = Math.toDegrees(Math.atan2(ball.vy, ball.vx)); //from the old method of angle reflection
 		
 		double nX = cos((int) theta);
 		double nY = sin((int) theta);
 		
 		double vDot = ball.vx*nX+ball.vy*nY;
 		
 		//double finalAngle = bearing-2*angDisplacement(-bearing,theta);
 		//double vMag = .35*Math.sqrt(sqr(ball.vx)+sqr(ball.vy));
 		
 		ball.x = player.x - cos((int) theta)*(ball.size+player.size);
 		ball.y = player.y - sin((int) theta)*(ball.size+player.size);
 		ball.vx = Entity.BALL_ELASTICITY*(ball.vx - 2*vDot*cos((int) theta));
 		ball.vy = Entity.BALL_ELASTICITY*(ball.vy - 2*vDot*sin((int) theta));
 	}
 
 	public void twoBodyCollision(Entity e1, Entity e2){
 		double m1 = e1.mass;
 		double m2 = e2.mass;
 		double v1x = e1.vx;
 		double v1y = e1.vy;
 		double v2x = e2.vx;
 		double v2y = e2.vy;
 		double x1 = e1.x;
 		double y1 = e1.y;
 		double x2 = e2.x;
 		double y2 = e2.y;
 		double size1 = e1.size;
 		double size2 = e2.size;
 
 		double theta = Math.toDegrees(Math.atan2(y2 - y1, x2 - x1));
 
 		double sepModifier = .095;
 
 		if(sqr(v1x) + sqr(v1y) >= sqr(v2x) + sqr(v2y)){
 
 			e1.x = x1 - sepModifier*cos((int)theta)*(size1+size2);
 			e1.y = y1 - sepModifier*sin((int)theta)*(size1+size2);
 
 		}else{
 
 			e2.x = x2 + sepModifier*cos((int)theta)*(size1+size2);
 			e2.y = y2 + sepModifier*sin((int)theta)*(size1+size2);
 
 		}
 
 		e1.vx = (2*v2x*m2 + v1x*(m1 - m2))/(m1 + m2);
 		e1.vy = (2*v2y*m2 + v1y*(m1 - m2))/(m1 + m2);
 
 		e2.vx = (v2x*(m2 - m1) + 2*m1*v1x)/(m1 + m2);
 		e2.vy = (v2y*(m2 - m1) + 2*m1*v1y)/(m1 + m2);
 	}//end two body collision
 
 	//converting entities to integer arrays for transmission
 	private int[] convertEntToArray(Entity e){
 		int[] ent = new int[5];
 
 		ent[0] = (int) e.x;
 		ent[1] = (int) e.y;
 		ent[2] = (int) e.bearing;
 		ent[3] = e.type;
 		ent[4] = (int) e.size;
 		return ent;
 	}
 
 	private int[] convertBallToArray(Entity e){
 		int[] ball = new int[3];
 
 		ball[0] = (int) e.x;
 		ball[1] = (int) e.y;
 		ball[2] = (int) e.bearing;
 		return ball;
 	}
 
 	//custom absolute value function
 	public double abs(double x){
 		if(x<0){
 			return -x;
 		}else{
 			return x;
 		}
 	}
 
 	public double angDisplacement(double a1, double a2){
 		double angle = Math.max(a1 - a2, a2 - a1);
 		if (angle > 180){
 			angle -= 360;
 		} else if(a1 > a2){
 			angle *= -1;
 		}
 
 		return angle;
 	}
 
 	//custom sign method
 	public double sign(double x){
 		if (x > 0){
 			return 1;
 		}else if (x < 0){
 			return -1;
 		}else{
 			return 0;
 		}
 	}
 
 	public synchronized Entity operateEntityList(int op, int index, Entity e){
 		switch(op){
 		case 0 :
 			playerList.add(e);
 			return null;
 		case 1 :
 			return playerList.get(index);
 		case 2 :
 			playerList.remove(index);
 			return null;
 		default :
 			return null;
 		}
 	}
 
 	public void sendData(ObjectOutputStream[] streams, double time){
 		for(int i=0;i<streams.length;i++){
 			if(streams[i] == null) continue;
 
 			try {
 				streams[i].writeDouble(time);
 				streams[i].writeInt(GameState.period);
 				streams[i].writeInt(GameState.state);
 				streams[i].writeObject(playerArray);
 				//write ball info
 				for (int j=0;j<ballArray.length;j++){
 					streams[i].writeInt(ballArray[j]);
 				}
 				streams[i].writeInt(ballPossessor);
 				streams[i].writeDouble(chargeTime);
 				streams[i].writeInt(GameState.blueScore);
 				streams[i].writeInt(GameState.redScore);
 				streams[i].writeInt(goalPosition);
 
 				streams[i].flush();
 			} catch (Exception e) {
 				System.out.println("issue in GameMechanics.sendData");
 				e.printStackTrace();
 			}
 		}
 	}
 }
 
