 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package zed;
 
 // java for file input
 import java.io.File;
 import java.io.FileNotFoundException;
 
 
 // Slick for drawing to screen and input
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.Sound;
 import org.newdawn.slick.SpriteSheet;
 
 /**
  *
  * @author Richard Barella Jr.
  * @author Adam Bennett
  * @author Ryan Slyter
  */
 
 // TODO: Edit Level Manager
 
 public class Level_Manager {   
 
     private static final int TILE_SIZE = 16;
     private static final int ZOMBIE = 0;
     
     private SpriteSheet tileset; // data for tiles
     private SpriteSheet character_sprites; // data for character sprites
     GObject[] objectlist; //this is going to be the array that the Level_Manager instance uses to hold all the objects
     GCharacter[] npclist; // array to hold NPCs in particular TODO:make into list
     GPortal[] portallist; // array to hold portals of the level
     
     Image Full_Heart; //full heart image
     Image Empty_Heart; //empty heart image
     int maxHealth; //players maximum hp
     boolean[] lifeBar; //array representing which HUD images are full hearts or empty hearts
     
     int width; // Number of tile columns
     int height; // Number of tile rows
     int xpos; // x position of top-left of tiles
     int ypos; // y position of top-left of tiles
     int scale; // By how many times is the pixels larger?
     int[][] bot_tile_x; // tileset x index of bot_tile[x][y]
     int[][] bot_tile_y; // tileset y index of bot_tile[x][y]
     //int[][] top_tile_x; // tileset x index of top_tile[x][y]
     //int[][] top_tile_y; // tileset y index of top_tile[x][y]
     File_Manager Files;
     Player_Character player; // data for player character
     
     int current_level_index;
     
     // Default instantiation for Level_Manager
     public Level_Manager() throws SlickException {
     	
     	Init(0, 10, 5);
     }
     
     public void Init(int level_index, int player_x, int player_y) throws SlickException{
 
     	current_level_index = level_index;
     	
         objectlist = null;
         npclist = null;
         portallist = null;
     	
         tileset = new SpriteSheet("images/tileset.png", 16, 16);
         character_sprites = new SpriteSheet("images/spritesheet.png", 16, 32);
         Files = new File_Manager();
         
         Initialize_Player_Information(player_x, player_y);
         Init_NPC(null);
         
         //start of initializing heart images and life bar for HUD display
         Full_Heart = new Image("images/fullheart.png", false, Image.FILTER_NEAREST);
         Empty_Heart = new Image("images/emptyheart.png", false, Image.FILTER_NEAREST);
         Full_Heart.setAlpha(0.5f);
         Empty_Heart.setAlpha(0.5f);
         maxHealth = player.Get_Health(); //change from "final" if '+ heart containers' added to the game as a feature!
         lifeBar = new boolean[maxHealth];
         for (int i = 0; i < maxHealth; i++){
         	lifeBar[i] = true;
         }
         
         //initialize level
         width = 20;
         height = 15;
         xpos = 0;
         ypos = 0;
         scale = 2;
         bot_tile_x = new int[height][width];
         bot_tile_y = new int[height][width];
         File level = new File("levels/" + String.valueOf(level_index) + ".lvl");
         short Tile_List[][] = null;
         short Field_Types = 4;
         try {
 			Tile_List = Files.Scan_LVL(level, Field_Types);
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
         
         // load tiles
         int k = 0;
         for (int i = 0; i < height; i++ ){
         	for(int j = 0; j < width; j++){
 				bot_tile_x[i][j] = Tile_List[0][k];
 				bot_tile_y[i][j] = Tile_List[0][++k];
 				k++;
         	}
         }
         
         // load GObjects
         if (Tile_List[1] != null)
         {
             int number_of_1s = 0;
 	        for (int i = 0; i < Tile_List[1].length; i++)
 	        {
 	        	if (Tile_List[1][i] == 1)
 	        	{
 	        		number_of_1s++;
 	        	}
 	        }
 	        objectlist = new GObject[number_of_1s];
 	        int current_object = 0;
 	        for (int i = 0; i < height; i++)
 	        {
 	        	for (int j = 0; j < width; j++)
 	        	{ 
 	        		if (Tile_List[1][j + width*i] == 1)
 	        		{
 			        	objectlist[current_object++] = new GObject(
 			            		j, i, // tell which tile to start in
 			            		false, // tell whether the object is visible
 			            		true, // tell whether the object is solid for collision
 			            		null, null,  // number of pixels each animation is shifted by
 			            		16, // give size of a tile in pixels
 			            		null, // give preinitialized animations
 			            		0 // tell which animation to start with
 			            		);
 	        		}
 	        	}
 	        }
         }
         if (Tile_List[2] != null)
         {
         	portallist = new GPortal[Tile_List[2].length/5];
         	
         	for (int i = 0; i < Tile_List[2].length/5; i++)
         	{
	        	portallist[0] = new GPortal(Tile_List[2][5*i],
 	        			Tile_List[2][5*i+1], 16, Tile_List[2][5*i+4],
 	        			Tile_List[2][5*i+2], Tile_List[2][5*i+3]);
         	}
         }
         if (Tile_List[3] != null)
         {
         	npclist = new GCharacter[Tile_List[3].length/3];
         	
         	for (int i = 0; i < Tile_List[3].length/3; i++)
         	{
         		if (Tile_List[3][i*3] == ZOMBIE)
         			npclist[i] = new Zombie(Tile_List[3][i*3+1],
         					Tile_List[3][i*3+2], character_sprites);
         	}
         	/*
         	npclist = new GCharacter[10];
         	
         	for (int i = 0; i < 5; i++)
         	{
         		for (int j = 0; j < 2; j++)
         		{
         			npclist[i + 5*j] = new Zombie(8+i, 5+j, character_sprites);//make sure to add new files to the repository.
         		}
         	}
         	*/
         }
     }
     
     // default initialization for the player's animations, position, attack speed, animation speed
     // within the level
     public void Initialize_Player_Information(int player_x, int player_y) throws SlickException
     {
         // spritesheet information for standing and walking animations
         int[] player_spritesheet_index_y = {0,  1,  2,  3,  0,  1,  2,  3}; // row in spritesheet for each animation
         int[] player_spritesheet_index_x = {1,  1,  1,  1,  0,  0,  0,  0}; // starting frame in each animation
         int[] player_animation_length  = {1,  1,  1,  1,  3,  3,  3,  3}; // length for each animation
         // sprite information for all animations
         int[] player_sprite_shift_x    = {0,  0,  0,  0,  0,  0,  0,  0,  16, 16, 16, 16}; // how many pixels to shift animation in x direction
         int[] player_sprite_shift_y    = {16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16}; // how many pixels to shift animation in y direction
         boolean[] player_looping       = {false,  false,  false,  false,  true,  true,  true,  true}; // whether each animation is looping or not
         int player_animation_speed = 200; // speed in milliseconds for the walking animations
         int player_attack_animation_speed = 50; // speed in milliseconds for the attack animations
         
         // start intializing array values for each animation
         Animation[] player_animation_list = new Animation[12]; // intialize animation array length
         for (int i = 0; i < 8; i++) // initialize walking and standing animations
         {
 	        player_animation_list[i] = new Animation(character_sprites, // spritesheet to use
 	        		// location of first sprite in spritesheet
 	                player_spritesheet_index_x[i],                                  player_spritesheet_index_y[i],
 	                // location of last sprite in spritesheet
 	                player_spritesheet_index_x[i] + player_animation_length[i] - 1, player_spritesheet_index_y[i],
 	                false, // horizontalScan value
 	                player_animation_speed, // speed value for animation
 	                true // autoupdate value
 	                );
 	        player_animation_list[i].setLooping(player_looping[i]); // intialize whether animation loops
 	        player_animation_list[i].setPingPong(true); // initialize whether animation ping-pongs between last and first frame
         }
         for (int i = 0; i < 4 ; i++) // initialize each attack animation
         {
         	Image[] frames = new Image[3]; // initialize length of attack animation in frames
         	for (int j = 0; j < 3; j++)
         	{
         		// intialize each frame in attackanimation
         		frames[j] = character_sprites.getSubImage(j*48, 4*32 + i*48, 48, 48);
         	}
         	// initialize animation with created frames
         	player_animation_list[i + 8] = new Animation(frames, player_attack_animation_speed, true);
         	player_animation_list[i + 8].setLooping(false); // set animation to not loop automatically
         	player_animation_list[i + 8].setPingPong(true); // set whether the animation ping-pongs between first and last frame 
         }
         
         player = new Player_Character(
         		player_x, // intialize character's x position w.r.t. tiles
         		player_y, // initialize character's y position w.r.t. tiles
         		true, // character is visible
         		true, // character is solid
         		player_sprite_shift_x, // give character sprite shift value in the x axis
         		player_sprite_shift_y, // give character sprite shift value in the y axis
         		16, // give player_character the size of a tile
         		player_animation_list, // give the created list of animations
         		0, // give which animation to start with
         		5, // give health of character
         		8.0f, // give speed in tiles per second of character
         		0, // set initial x_movement value to 0
         		0 // set initial y_movement value to 0
         		);
     }
     
     // made this for testing initialization for NPCs
     public void Init_NPC(int[] npc_data) throws SlickException{
     	
     	npclist = new GCharacter[0];
     	
     	/*
     	npclist = new GCharacter[10];
     	
     	SpriteSheet enemysheet = new SpriteSheet("images/enemies.png", 16, 24);
 
     	for (int i = 0; i < 5; i++)
     	{
     		for (int j = 0; j < 2; j++)
     		{
     			npclist[i + 5*j] = new Zombie(8+i, 5+j, character_sprites);//make sure to add new files to the repository.
     		}
     	}
     	*/
     }
     
     public void Init_GObject(){
     	
     	objectlist = new GObject[0];
     }
     
     // Edit class variables
     public void setwidth(int newwidth){
         
         width = newwidth;
     }
     public void setheight(int newheight){
         
         height = newheight;
     }
     public void setxpos(int newxpos){
         
         xpos = newxpos;
     }
     public void setypos(int newypos){
         
         ypos = newypos;
     }
     public void setscale(int newscale){
         
         scale = newscale;
     }
     public void settile(Image newtile, int x, int y){
         
         tileset.equals(newtile);
     }
     
     //method update_HUD takes boolean array representing player_character's health,
     //the fixed maximum health of said characters, the hero itself to get its current health,
     //and the full and empty hearts so that they can be drawn on the screen; the method
     //is meant to be called every time the game updates so as to check if the player
     //has been hurt/killed or not so that the HUD in the top left can reflect that
     public void update_HUD(boolean[] lifebar, int maxhealth, Player_Character hero, Image full, Image empty, Graphics g){
     	int current_Health = hero.Get_Health();
     	if (maxhealth == current_Health){
     		for (int i = 0; i < maxhealth; i++){
     			lifebar[i] = true; //supports getting full-health potions this way
     		}
     	}
     	else if (current_Health == 0){
     		for (int i = 0; i < maxhealth; i++){
     			lifebar[i] = false;
     			/**
     			 * code here to handle telling level manager that game is over
     			 * possibly setting a flag so empty hearts can be drawn first
     			 */
     		}
     	}
     	else{
     		for (int i = 0; i <current_Health; i++){
     			lifebar[i] = true;
     		}
     		for (int i = current_Health; i < maxhealth; i++){
     			lifebar[i] = false;
     		}
     	}
     	//I'm *guessing* here (for the sake of performance) that drawing the objects
     	//to the screen is more costly than having more for-loops
     	for (int i = 0; i < maxhealth; i++){
     		if (lifebar[i] == true)
     		{
     			g.drawImage(
     					full, 				// image
     					i*full.getWidth()*scale, 	// x pos
     					0, 					// y pos
     					(i + 1)*full.getWidth()*scale, 	// x2 pos
     					full.getHeight()*scale, 	// y2 pos
     					0, 0,			 	// ???
     					full.getWidth(), 	// width
     					full.getHeight()); 	// height
     		}
     		else
     		{
     			g.drawImage(
     					empty,
     					i*empty.getWidth()*scale,
     					0,
     					(i + 1)*empty.getWidth()*scale, 
     					empty.getHeight()*scale,
     					0, 0,
     					empty.getWidth(),
     					empty.getHeight());
     		}
     	}
     }
     
     // the display function for the Level_Manager that is called every frame
     // to render the level
     public void display(GameContainer gc, Graphics g){
     	
         for (int i = 0; i < height; i++) // display each row
         {
             for (int j = 0; j < width; j++) // display each column
             {
                 g.drawImage(tileset.getSubImage(bot_tile_x[i][j], bot_tile_y[i][j]),
                         xpos + j*TILE_SIZE*scale,
                         ypos + i*TILE_SIZE*scale,
                         xpos + j*TILE_SIZE*scale + TILE_SIZE*scale,
                         ypos + i*TILE_SIZE*scale + TILE_SIZE*scale,
                         0, 0,
                         TILE_SIZE, TILE_SIZE);
             }
         }
         
         sort_render_order(npclist); // sort main list for efficiency
         GObject[] objlist = new GObject[npclist.length + 1]; // create temp list
         for (int i = 0; i < npclist.length; i++)
         {
         	objlist[i + 1] = npclist[i]; // put npcs in temp list
         }
         objlist[0] = player; // put player inside of temp list
         sort_render_order(objlist); // sort temp list for render order
         
         for (int i = 0; i < objlist.length; i++)
         {
             objlist[i].Render(scale, xpos, ypos, gc, g); // render in order
         }
         
         update_HUD(lifeBar, maxHealth, player, Full_Heart, Empty_Heart, g);
     }
     
     // the update funciton that is called each time Slick updates to update the information
     // in the level
     boolean has_ported = false;
     public void update() throws SlickException
     {
         player.Update(objectlist, npclist);
         
         for (int i = 0; i < npclist.length; i++)
         {
         	npclist[i].Update(objectlist, npclist, player);
         }
     	GObject activeportal;
         if ((activeportal = player.Collision(portallist)) != null)
         {
         	if (!has_ported)
         	{
         		for (int i = 0; i < portallist.length; i++)
         		{
         			if (portallist[i] == activeportal)
         			{
         				Init(portallist[i].Get_Dest_Level(),
         						portallist[i].Get_Dest_X_Tile(),
         						portallist[i].Get_Dest_Y_Tile());
         			}
         		}
 	        	has_ported = true;
         	}
         }
         else
         {
         	has_ported = false;
         }
     }
     
     // change the player's X_Movement and Y_Movement values within Zed.java
     public void move_player(int new_x_mov, int new_y_mov)
     {
         player.New_Movement(new_x_mov, new_y_mov);
     }
     
     private void sort_render_order(GObject[] list)
     {
     	GObject temp;
     	boolean flag = true;
     	while (flag)
     	{
     		flag = false;
     		for (int i = 0; i < list.length - 1; i++)
         	{
         		if (list[i].Get_Y_Position() > list[i + 1].Get_Y_Position())
         		{
             		temp = list[i];
             		list[i] = list[i + 1];
             		list[i + 1] = temp;
             		flag = true;
         		}
         	}
     	}
     }
 }
