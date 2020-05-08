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
 	int mapwidth = 6000;
 	
 	ArrayList<Entity> playerList;
 	ArrayList<int[]> playerArrayList;
 	static int[][] playerArray;
 	
 	Entity ball;
 	
 	public Random generator;
 	
 	public double period;
 	
 	int[] mouseX;
 	int[] mouseY;
 	int[][] keyArray;
 	
 	//initialization
 	public void init(){
 		topLeftWaypoint = new Point();
 		topLeftWaypoint.x = 200;
 		topLeftWaypoint.y = mapheight - 200;
 		bottomRightWaypoint = new Point();
 		bottomRightWaypoint.x = mapwidth - 200;
 		bottomRightWaypoint.y = 200;
 		base1Waypoint = new Point();
 		base1Waypoint.x = 200;
 		base1Waypoint.y = 200;
 		base2Waypoint = new Point();
 		base2Waypoint.x = mapwidth - 200;
 		base2Waypoint.y = mapheight - 200;
 		
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
 		
 		//player init
 		for(int i=0; i<10; i++){
 			Entity player = new Entity();
 			player.x = 3000;
 			player.y = 1000 + i*500;
 			playerList.add(player);
 			player.playerInit();
 		}
 		
 		ball = new Entity();
 		ball.ballInit();
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
 			
			double turnRate = 360;
			
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
 			
 			entity.walking = keyArray[i][KEY_W] == 1 | keyArray[i][KEY_S] == 1;
 			
 			if(keyArray[i][KEY_W] == 1){
 				entity.walkDirection = 1;
 			} else if(keyArray[i][KEY_S] == 1){
 				entity.walkDirection = -1;
 			}
 			
 			for(int j=0;j<10;j++){
 				if(keyArray[i][j] == 1)System.out.println(j + "wooo!");
 			}
 			
 			//updates every entity's position. also capable of removing the entity
 			if(entity.move(period)){
 				operateEntityList(AL_REMOVE, i, null);
 				i--;
 			} else {
 				playerArrayList.add(entityArray);
 			}
 		}//end entity loop
 		
 		ball.move(period);
 		
 		/* building transmit array */
 		playerArray = new int[playerArrayList.size()][5];
 		for(int i=0; i<playerArrayList.size(); i++){
 			playerArray[i] = playerArrayList.get(i);
 		}
 	}
 	
 	public void collisionCheck(ArrayList<Entity> a){
 		for(int i = 0; i < a.size(); i++){
 			
 			Entity entity1 = a.get(i);
 
 		}//end for loop 1
 	}//end method
 	
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
 		
 		double theta = Math.toDegrees(Math.atan2(v2y - v1y, v2x - v1x));
 		
 		double separation = Math.sqrt(sqr(x2 - x1) + sqr(y2 - y1));
 		
 		if(sqr(v1x) + sqr(v1y) >= sqr(v2x) + sqr(v2y)){
 			
 			e1.x = x1 + 2.0*cos((int)theta)*((size1+size2)/2 - separation);
 			e1.y = y1 + 2.0*sin((int)theta)*((size1+size2)/2 - separation);
 			
 		}else{
 			
 			e2.x = x2 -= 2.0*cos((int)theta)*((size1+size2)/2 - separation);
 			e2.y = y2 -= 2.0*sin((int)theta)*((size1+size2)/2 - separation);
 	
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
 		return ent;
 	}
 	
 	private int[] convertBallToAray(Entity e){
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
 				//streams[i].writeObject(obj);
 				
 				streams[i].flush();
 			} catch (Exception e) {
 				System.out.println("issue in GameMechanics.sendData");
 				e.printStackTrace();
 			}
 		}
 	}
 }
 
