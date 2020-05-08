 
 package zed;
 
 import org.newdawn.slick.Animation;
 
 // Slick for drawing to the screen
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SpriteSheet;
 
 /**
  *
  * @author Richard Barella Jr
  * @author Adam Bennett
  * @author Ryan Slyter
  */
 
 // ============
 // Object class
 // ============
 // The most basic out of classes that hold information on in-game
 // objects and can also print itself to the screen.
 public class GObject {
     
     private static final int ANIMATION_SPEED = 100;
     
     // Holds the pixel-location of top-left of Object
     int X_Position;
     int Y_Position;
     
     int Width;
     int Height;
     
     // Holds the animations for the object.
     Animation[] Animation_List;
     Animation Current_Animation;
     int Current_Animation_Index; // Holds index of current playing animation
                                  // so that we don't have to find it every time
     
     int Tilesize;
     
     int[] Sprite_Shift_X;
     int[] Sprite_Shift_Y;
     
     // Holds the visibility of Object
     boolean Visible;
     
     // If true, then all objects collide with it.
     // If false, then it still collides with other Solid objects.
     boolean Solid;
     
     // Amount of damage to deal if player collides with it
     int Damage;
     
     // Default constructor
     public GObject(){
     }
     
     // Constructor given SpriteSheet
     public GObject(
     		int tile_x, int tile_y, // tell which tile to start in
     		int width, int height,
     		boolean visible, // tell whether the object is visible
     		boolean solid,
     		int damage, // tell whether the object damages player
             int[] sprite_shift_x, int[] sprite_shift_y, // number of pixels each animation is shifted by when displaying
             int tilesize, // give size of a tile in pixels
             SpriteSheet sprites, // give the spritesheet used
             int[] spritesheet_index, // give the row of each animation
             int[] animation_length, // give the number of frames in each animation 
             boolean[] looping, // tell which animations auto-loop
             int current_animation // tell which animation to start with
             ){
 
     	Tilesize = (tilesize==0?1:tilesize);
         this.Init(tile_x, tile_y, width, height, visible, solid,
         		damage, sprite_shift_x, sprite_shift_y, tilesize, sprites,
                 spritesheet_index, animation_length, looping, current_animation);
     }
     
     // Constructor given Animation[]
     public GObject(
     		int tile_x, int tile_y, // tell which tile to start in
     		int width, int height,
     		boolean visible, // tell whether the object is visible
     		boolean solid, // tell whether the object is solid for collision
     		int damage, // tell whether the object damages the player
     		int[] sprite_shift_x, int[] sprite_shift_y,  // number of pixels each animation is shifted by
     		int tilesize, // give size of a tile in pixels
     		Animation[] animation_list, // give preinitialized animations
     		int current_animation // tell which animation to start with
     		){
     	
     	Tilesize = (tilesize==0?1:tilesize);
     	// initialize
     	this.Init(tile_x, tile_y, width, height, visible, solid, damage, sprite_shift_x, sprite_shift_y, tilesize, animation_list, current_animation);
     }
     
     // simple constructor for an invisible object that can be placed anywhere
     // and is not constricted by tiles
     public GObject(
     		int xpos, int ypos, int width, int height, Animation animation){
 
     	Tilesize = 16;
     	Animation[] animation_list = {animation};
     	int[] ssx = {0}; // sprite shift x
     	int[] ssy = {0}; // sprite shift y
     	// initialize
     	this.Init(0, 0, width, height, false, false, 0, ssx, ssy, 16, animation_list, 0);
     	
     }
     
     // Initialization function that generates animations based on SpriteSheet
     public void Init(
     		int tile_x, int tile_y, // tell which tile to start in
     		int width, int height,
     		boolean visible, // tell whether the object is visible
     		boolean solid, // tell whether the object is a solid for collision
     		int damage, // tell whether the object damages player
             int[] sprite_shift_x, int[] sprite_shift_y, // number of pixels each animation is shifted by when displaying
             int tilesize, // give size of a tile in pixels
             SpriteSheet sprites, // give the spritesheet used
             int[] spritesheet_index, // give the row of each animation
             int[] animation_length, // give the number of frames in each animation 
             boolean[] looping, // tell which animations auto-loop
             int current_animation // tell which animation to start with
             ){
         
     	// set position based on location and tilesize
         X_Position = tile_x*tilesize;
         Y_Position = tile_y*tilesize;
         
         Height = height;
         Width = width;
         
         Visible = visible; // set visibility
         
         Solid = solid; // set solidity for collision
         
         Damage = damage; // set damage value
         
         Sprite_Shift_X = sprite_shift_x; // set the shift of each animation's display
         Sprite_Shift_Y = sprite_shift_y;
         
         Animation_List = new Animation[spritesheet_index.length]; // set length of animation list
 
         // construct each animation based on parameters given
         for (int i = 0; i < spritesheet_index.length; i++)
         {
             Animation_List[i] = new Animation(
             		sprites, // give SpriteSheet
                     0,                       spritesheet_index[i], // first sprite location
                     animation_length[i] - 1, spritesheet_index[i], // last sprite location
                     false, // horizontalScan true?
                     ANIMATION_SPEED, true /*autoupdate?*/);
             Animation_List[i].setLooping(looping[i]); // set whether animation loops
             Animation_List[i].setPingPong(true); // set whether animation ping-pongs
         }
         Current_Animation = Animation_List[current_animation]; // set current animation
         Current_Animation_Index = current_animation; // set current animation index location
     }
 
     // Initialization given Animation[]
     public void Init(int tile_x, int tile_y, 
     		int width, int height, boolean visible, boolean solid, int damage,
     		int[] sprite_shift_x, int[] sprite_shift_y, int tilesize,
     		Animation[] animation_list, int current_animation)
     {
     	X_Position = tile_x*tilesize;
     	Y_Position = tile_y*tilesize;
     	
     	Width = width;
     	Height = height;
     	
     	Visible = visible;
     	
     	Solid = solid;
     	
     	Damage = damage;
     	
     	Sprite_Shift_X = sprite_shift_x;
     	Sprite_Shift_Y = sprite_shift_y;
     	
     	Animation_List = animation_list;
     	
     	if (Animation_List != null)
     	{
     		Current_Animation = Animation_List[current_animation];
     	}
     }
     
     // Change the position of Object
     public void Move(int new_x_pos, int new_y_pos){
         
         X_Position = new_x_pos;
         Y_Position = new_y_pos;
     }
     
     // Get the object's current x position
     public int Get_X_Position(){
         
         return X_Position;
     }
     
     // Get the object's current y position
     public int Get_Y_Position(){
         
         return Y_Position;
     }
     
     public int Get_Width(){
     	
     	return Width;
     }
     
     public int Get_Height(){
     	
     	return Height;
     }
     
     // can tell if pixel is within GObject or not in x direction of movement
     boolean Pixel_Contained(int x, int y){
     	
     	if (x >= X_Position
     			&& x < X_Position + Width
    			&& y >= Y_Position && y < Y_Position + Height)
     	{
     		return true;
     	}
     	return false;
     }
     
     // Can tell if the object is alligned with the tiles displayed
     // on the screen
     public boolean Alligned_With_Tiles()
     {
         return ((X_Position % Tilesize == 0) &
                 (Y_Position % Tilesize == 0));
     }
     
     // renders the current animation
     public void Render(int zoom,
             int current_tile_x, int current_tile_y, // position of tiles
             GameContainer gc, Graphics g){
         
         if (Visible)
         {
             Current_Animation.draw(
                     X_Position*zoom + current_tile_x - Sprite_Shift_X[Current_Animation_Index]*zoom, // y position to draw
                     Y_Position*zoom + current_tile_y - Sprite_Shift_Y[Current_Animation_Index]*zoom, // x position to draw
                     Current_Animation.getWidth()*zoom, Current_Animation.getHeight()*zoom); // width and height to draw
         }
     }
     // Play a different animation for the object
     public void Change_Animation(int new_animation){
         
         if (new_animation < Animation_List.length
                 && new_animation >= 0)
         {
             Current_Animation = Animation_List[new_animation];
         }
         Current_Animation_Index = new_animation;
     }
     
     // Get the number for the current animation being played
     // has an error if current animation can't be found.
     public int Get_Frame_State(){
         
         for (int i = 0; i < Animation_List.length; i++)
         {
             if (Current_Animation == Animation_List[i])
             {
                 return i;
             }
         }
         return -1;
     }
     
     // Returns whether the object damages the player
     public int Get_Damage(){
     	
     	return Damage;
     }
     
     public int Get_Type(){
     	
     	return -1;
     }
 }
