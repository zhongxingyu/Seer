 package Game;
 
 import java.awt.event.KeyEvent;
 import java.util.ArrayList;
 
 import javax.media.opengl.GL2;
 
 import Game.SharedRessources.RessourceType;
 import GameObjects.GameChar;
 import GameObjects.Player;
 import Helpers.Debug;
 import Maths.*;
 import Renderer.*;
 
 public class MainGame 
 {
 
 	public final Menu menu = new Menu();
 	public static boolean inMenu = true;
 
 	//Main Parameters
 	public static String Window_Name		= "Asteroids";
 	public static Vector2 Screen_Size 		= new Vector2(1024,780);
 	
 
 	
 	// Game singletons
 	public static final SharedRessources sharedRessources	= new SharedRessources();
 	public static final Renderer render 					= new Renderer(Window_Name);
 	public static final Controls controls 					= new Controls();
 	public static final Debug debug 						= new Debug();
 
 	
 	ArrayList<GameChar> objectVector;
 	//Player player;
 	GameChar object_2;
 	//GameChar object_3;
 	//GameChar playButton;
 	
 	public static Player player;
 	float test = 0;
 
 
 	public void init(GL2 gl)
 	{
 		sharedRessources.LoadRessources
 		(new Ressource[]
 			{
 				new Ressource("rocket_ship","./resources/textures/rocket_ship.png",RessourceType.Texture),
 				new Ressource("smoke","./resources/textures/SmokeParticle.png",RessourceType.Texture)
 			}
 		);
 
 
 		player = new Player();
 		player.transform.size = new Vector2(3,3);
		
		


		player.rigidBody.frictionCoefficient = 0.1f;
 	}
 
 	public void Update(GL2 gl)
 	{
 		// Put Game Logic here
 		
 		if(controls.isPressed(KeyEvent.VK_ESCAPE))
 		{
 			System.exit(0);
 		}
 
 		//Update object_1 transform, physics, rendering etc...
 
 		//player.Update();
 		//object_2.Update();
 		
 		if (inMenu)
 		{
 			menu.Update();
 			System.out.println("hello");
 		} else {
 			System.out.println("update");
 			player.Update();
 			object_2.Update();
 		}
 		
 	}
 
 	
 	// Main Method. DO NOT touch this. If you do. YOU WILL DIE.
 	public static void main(String[] args)
 	{
 		MainGame game = new MainGame();							// Initiating a new Game.
 		
 		render.mainGame = game;									// Associate this Game to the renderer.
 		render.CreateWindow(new Vector2(1024,780),controls);	// Create a new Frame object and returns its reference.
 	}
 	
 }
