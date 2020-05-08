 package mta13438;
 
 import static org.lwjgl.opengl.GL11.GL_LINES;
 import static org.lwjgl.opengl.GL11.glBegin;
 import static org.lwjgl.opengl.GL11.glEnd;
 import static org.lwjgl.opengl.GL11.glLineWidth;
 import static org.lwjgl.opengl.GL11.glVertex2i;
 
 import java.util.ArrayList;
 import org.lwjgl.Sys;
 import org.lwjgl.opengl.Display;
 
 public class Loader {
 
 	static Level tutorialLevel = new Level(new ArrayList<Room>(), 0, 0, 0);
 	private static Controls controls = new Controls();
 	private static Player player = new Player(new Point(140,310,10),0.5f,0.01f,10);
 	private static long lastFrame;
 	private static int delta = getDelta();
 	private static int currentRoom;
 	private static boolean renderRoom = false;
 	private static boolean collision = false;
 	
 	//testing
 	private static Sound testsound = new Sound(SOUNDS.HEARTBEAT, player.getPos(), true);
 	private static boolean playing = false;
 	
 	
 	public void start() {
 		DebugInterface.Initialize(800, 600); // Width and Length of display
 		Menu mainMenu = new Menu();
 		getDelta();
 	}
 
 	private static void loadTutorialLevel() {
 		tutorialLevel.addRoomList(new Room(10, 20, 20, new Point(0,5,0), new Point(10, 15, 0), MATERIALS.ROCK));
 		tutorialLevel.addRoomList(new Room(60, 50, 30, new Point(0,25,0), new Point(60, 5, 0), MATERIALS.ROCK));
 		tutorialLevel.addRoomList(new Room(50, 10, 20, new Point(0,5,0), new Point(50, 5, 0), MATERIALS.ROCK));
 		tutorialLevel.addRoomList(new Room(150, 90, 60, new Point(0,45,0), new Point(150, 65, 0), MATERIALS.ROCK));
 		tutorialLevel.addRoomList(new Room(110, 110, 40, new Point(0,55,0), new Point(110, 55, 0), MATERIALS.ROCK));
 		tutorialLevel.addRoomList(new Room(40, 10, 20, new Point(0,5,0), new Point(35, 0, 0), MATERIALS.ROCK));
 		tutorialLevel.addRoomList(new Room(10, 50, 20, new Point(5,50,0), new Point(5, 0, 0), MATERIALS.ROCK));
 		tutorialLevel.addRoomList(new Room(70, 70, 40, new Point(35,70,0), new Point(35,0, 0), MATERIALS.ROCK));
 		tutorialLevel.addRoomList(new Room(10, 20, 20, new Point(5,20,0), new Point(5, 0, 0), MATERIALS.ROCK));
 		tutorialLevel.addRoomList(new Room(50, 80, 40, new Point(25,80,0), new Point(25, 0, 0), MATERIALS.ROCK));
 		tutorialLevel.addRoomList(new Room(30, 20, 40, new Point(15,20,0), new Point(15, 20, 0), MATERIALS.ROCK));
 		tutorialLevel.getRoomList().get(3).addObsList(new Water(new Point(160, 270, 0), 20, 50, 0, 0));
 		tutorialLevel.getRoomList().get(4).addObsList(new Wall(new Point(330, 280, 0), 10, 70, 0, 0));
 		tutorialLevel.getRoomList().get(7).addObsList(new Trap(new Point(410, 210, 0), 30, 30, 0, 0));
 		tutorialLevel.autoLevelGenerator(new Point(10,300,0));
 	}
 
 	public static void playTutorialLevel(){
 		Display.destroy();
 		loadTutorialLevel();
 		DebugInterface.Initialize(800, 600); // Width and Length of display
 		DebugInterface.InitOpenGL(500,500); // Width and Length inside the display (Scaling of perspective here)
 	}
 
 	public static void renderTutorialLevel(){
 		input();
 		collision = player.collisionCheck(tutorialLevel, currentRoom);
 
 		if(collision){
 			for (int i = 0; i < tutorialLevel.getRoomList().get(currentRoom).getObsList().size(); i++) {
 				if(player.getPos().getX() > tutorialLevel.getRoomList().get(currentRoom).getObsList().get(i).getPos().getX() && 
 						player.getPos().getX() < tutorialLevel.getRoomList().get(currentRoom).getObsList().get(i).getPos().getX() + tutorialLevel.getRoomList().get(currentRoom).getObsList().get(i).getDx()){
 					if(player.getPos().getY() > tutorialLevel.getRoomList().get(currentRoom).getObsList().get(i).getPos().getY() &&
 						player.getPos().getY() < tutorialLevel.getRoomList().get(currentRoom).getObsList().get(i).getPos().getY() + tutorialLevel.getRoomList().get(currentRoom).getObsList().get(i).getDy()){
 						tutorialLevel.getRoomList().get(currentRoom).getObsList().get(i).collision(player, tutorialLevel, currentRoom);
 					}
 				}
 			}
 		}
 		//Sound testing
 		testsound.update(new Point(140,310,10));
 		player.setListener();
 		glLineWidth(1.5f);
 		glBegin(GL_LINES);
 		 glVertex2i(140,310);
 		 glVertex2i(140,311);
 		glEnd();
 		glLineWidth(1.0f);
 		
 		
 		if(playing == false){
 			testsound.play();
 			playing = true;
 		}
 		
 		//Draw the Tutorial Levels rooms
 		tutorialLevel.Draw();
 
 		//Draw the obs of every room
 		for (int i = 0; i < tutorialLevel.getRoomList().size(); i++) {
 			for (int j = 0; j < tutorialLevel.getRoomList().get(i).getObsList().size(); j++) {
 				tutorialLevel.getRoomList().get(i).getObsList().get(j).draw();
 			}
 		}
 
 		//Draw the player
 		player.draw();
 	}
 
 	public static void input() {
 
 		controls.takeInput();	
 
 		currentRoom = tutorialLevel.getCurrentRoom(player.getPos());
 		delta = getDelta();
 
 		if(controls.getKEY_UP()){
 			player.foward(delta/10, tutorialLevel, currentRoom);
 		}
 		if(controls.getKEY_DOWN()){
 			player.backward(delta/10, tutorialLevel, currentRoom);
 		}
 		if(controls.getKEY_LEFT()){
 			player.turnLeft(delta/10);
 		}
 		if(controls.getKEY_RIGHT()){
 			player.turnRight(delta/10);
 		}
 	}
 
 
 	public static int getDelta() {
 		long time = getTime();
 		int delta = (int) (time - lastFrame);
 		lastFrame = time;
 		return delta;
 	}
 
 	public static long getTime() {
 		return (Sys.getTime() * 1000) / Sys.getTimerResolution();
 	}
 }
