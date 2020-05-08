 package com.infinitron.rpg;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Random;
 import android.content.Context;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.view.MotionEvent;
 import android.view.SurfaceView;
 import android.view.SurfaceHolder;
 
 public class Game extends SurfaceView implements SurfaceHolder.Callback {
 
 	private SurfaceHolder surfaceHolder;
 	private GameThread gameThread;
 	private Random randNum = new Random();
 	private Monster[] monsterCollection = new Monster[10];//Would then call createMonsters to fill array
 	public Monster[] levelMonsters = new Monster[4];// filled in init() by randomly picking from monsterCollection
 	private Item[][] itemCollection = new Item[3][10];//Would then call createItems to fill array
 	
 	/* TESTING SPRITE RENDERING */
 	
 	// Test sprite rendering
 	public GridSpriteSheet elaine = new GridSpriteSheet(BitmapFactory.decodeResource(getResources(), R.drawable.walk_elaine), 30, 47);
 	public Sprite elaineSprite = new Sprite(elaine, 2);
 	public GameObject elaineObject = new GameObject("Elaine", elaineSprite, 20, 20);
 
 	// NPC sprite/object Test
 	public GridSpriteSheet speech_bubble = new GridSpriteSheet(BitmapFactory.decodeResource(getResources(), R.drawable.speech_bubble),47, 38);	
 	public Sprite elaine_talk = new Sprite(speech_bubble,0);
 	public Sprite[] npc = {new Sprite(elaine, 0)};	// She's good and bad :O
 	public NPC elaineNPC = new NPC("elaineNPC", npc[0], 100, 200, "Hi, my name is Elaine", elaine_talk, true);
     
 	// Animated Game Object Test
 	public Sprite[] animatedElaineSprites = {new Sprite(elaine, 0), new Sprite(elaine, 1), new Sprite(elaine, 2), new Sprite(elaine, 3), new Sprite(elaine, 4)};
 	public AnimatedGameObject animatedElaineObject = new AnimatedGameObject("Elaine", animatedElaineSprites, 70, 70, 100, GameThread.CYCLE_TIME);
 	
 	// Another bleedin' test -> Animated Player Object Test
 	public GridSpriteSheet p1 = new GridSpriteSheet(BitmapFactory.decodeResource(getResources(), R.drawable.player16x18),16,18);
 	
 	public Sprite[][] playerSprites = { {new Sprite(p1, 0,0), new Sprite(p1, 0,1)},
 										{new Sprite(p1, 1,0), new Sprite(p1, 1,1)},
 										{new Sprite(p1, 2,0), new Sprite(p1, 2,1)},
 										{new Sprite(p1, 3,0), new Sprite(p1, 3,1)},
 										{new Sprite(p1, 4,0), new Sprite(p1, 4,1)},
 										{new Sprite(p1, 5,0), new Sprite(p1, 5,1)},
 										{new Sprite(p1, 6,0), new Sprite(p1, 6,1)},
 										{new Sprite(p1, 7,0), new Sprite(p1, 7,1)} };
 	
 	public Player player = new Player("Elaine", playerSprites, 160, 160, 500, 80, GameThread.CYCLE_TIME);
 
 	//public Level level = new Level(BitmapFactory.decodeResource(getResources(), R.drawable.map_image), readTxt(getResources().openRawResource(R.raw.map_50x50)), 50, 50);
 	public Level level;
 	
 	public Input input = new Input(Main.screen_width, Main.screen_height);
 	
 	private int x_screen_pos, y_screen_pos = 0;
 
 	protected int fps = 0;
 
 	public Game(Context context) {
 		super(context);
 		level = new Level(context);
 		init();
 	}
 
 	public void init() {
 		surfaceHolder = getHolder();
 		surfaceHolder.addCallback(this);
 		setFocusable(true);
 
 		gameThread = new GameThread(this);
		
<<<<<<< HEAD
=======
 		makeMonsterSprites();
 		/* Get monsters to place in level. Done by randomly selecting monsters from the monsterCollection and
 		 * placing them in the array levelMonsters. Selects items from monster array at random with
 		 * max monster being array length exclusive. Only get four monsters for now.
 		 */
 		for(int i = 0; i < levelMonsters.length; i++){
 			levelMonsters[i] = monsterCollection[randNum.nextInt(monsterCollection.length)];
 		}
>>>>>>> b1f4e6315d758bc45afc7d43bdc3aa95bec052c9
 	}
 
 	public void surfaceCreated(SurfaceHolder surfaceHolder) {
 		gameThread.surfaceCreated(surfaceHolder);
 		gameThread.start();
 	}
 
 	public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
 		gameThread.surfaceDestroyed(surfaceHolder);
 
 		boolean retry = true;
 		while (retry) {
 			try {
 				gameThread.join();
 				retry = false;
 			} catch (InterruptedException e) {
 
 			}
 		}
 	}
 	
 	public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
 		gameThread.surfaceChanged(surfaceHolder, format, width, height);
 	}
 
 	@Override
 	public void draw(Canvas canvas) {
<<<<<<< HEAD
 		level.draw(canvas);
		/*for(int i = 0; i < levelMonsters.length;i++){
			if(levelMonsters[i].getHp() == 0)levelMonsters[i] = null;//Delete monsters that die
			if(levelMonsters[i] != null)gObject[i].draw(canvas);//Draws all alive monsters in the level
		}*/
=======
		level.draw(canvas, x_screen_pos, y_screen_pos);
 		
 		for(int i = 0; i < levelMonsters.length; i++){
 			if(levelMonsters[i] != null){
 				levelMonsters[i].draw(canvas);	//Draws all alive monsters in the level
 			}
 		}
 		
>>>>>>> b1f4e6315d758bc45afc7d43bdc3aa95bec052c9
 		animatedElaineObject.draw(canvas);
 		player.draw(canvas);
 	}
 
 	public void update() {
 		animatedElaineObject.update();
 		player.update();
 		for(int i = 0; i < levelMonsters.length; i++){
 			if(levelMonsters[i] != null){
 				levelMonsters[i].update();
 				// Delete dead monsters by setting them to NULL
 				if(((Monster) levelMonsters[i]).getHp() <= 0){
 					levelMonsters[i] = null;
 				}
 			}
 		}
 		
 		if (input.up) {
 			player.moveUp();
 			//y_screen_pos --;
 		} else if (input.down) {
 			player.moveDown();
 			//y_screen_pos ++;
 		} else if (input.left) {
 			player.moveLeft();
 			//x_screen_pos --;
 		} else if (input.right) {
 			player.moveRight();
 			//x_screen_pos ++;
 		}
 	}
 
 	private String readTxt(InputStream is) {
 
 		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
 		int i;
 		try {
 			i = is.read();
 			while (i != -1) {
 				byteArrayOutputStream.write(i);
 				i = is.read();
 			}
 			is.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 	  	return byteArrayOutputStream.toString();
 	}
 
 	//Replaces Monster Sprite Rendering by instead doing it for several monsters, chosen from the levelMonsters array
 	//NOTE parameters are incorrect, have chosen temporary values
 	public void makeMonsterSprites(){
 		GridSpriteSheet monsterSheet = new GridSpriteSheet(BitmapFactory.decodeResource(getResources(), R.drawable.monster_inc_smaller), 25, 25);
 		Sprite[] animatedMonsterSprites = {new Sprite(monsterSheet, 0), new Sprite(monsterSheet, 1),new Sprite(monsterSheet, 2),
 										   new Sprite(monsterSheet, 3), new Sprite(monsterSheet, 4)};
 		createMonsters(animatedMonsterSprites);
 	}
 	
 	// 1d array full of monster objects 
 	//Monster extends GameObject and hence has the following attributes: String name, Sprite sprite, int xPos, int yPos, int _max_hp, int _hp, int _attack, int _defense
 	//The following method will take the array monster collection and manually fill it with monsters
 	public void createMonsters(Sprite[] sprite){
 										  //Name        //Sprite 	//xPos	//yPos	//maxHp	//hp	//att	//def 	//updateTime 	//frameTime
 		monsterCollection[0] = new Monster("Rat",		sprite,		5,		5,		1,		1,		1,		1, 		100, 			GameThread.CYCLE_TIME);
 		monsterCollection[1] = new Monster("Kobold",	sprite,		10,		30,		5,		5,		1,		2, 		100, 			GameThread.CYCLE_TIME);
 		monsterCollection[2] = new Monster("Goblin",	sprite,		20,		60,		5,		5,		2,		2, 		100, 			GameThread.CYCLE_TIME);
 		monsterCollection[3] = new Monster("Zombie",	sprite,		30,		90,		7,		7,		2,		3, 		100, 			GameThread.CYCLE_TIME);
 		monsterCollection[4] = new Monster("Wolf",		sprite,		40,		120,	8,		8,		4,		3, 		100,			GameThread.CYCLE_TIME);
 		monsterCollection[5] = new Monster("Gnome",		sprite,		50,		150,	3,		3,		6,		1, 		100,		 	GameThread.CYCLE_TIME);
 		monsterCollection[6] = new Monster("Orc",		sprite,		60,		180,	8,		8,		3,		4, 		100, 			GameThread.CYCLE_TIME);
 		monsterCollection[7] = new Monster("Skeleton",	sprite,		70,		210,	4,		4,		2,		3, 		100, 			GameThread.CYCLE_TIME);
 		monsterCollection[8] = new Monster("Spider",	sprite,		80,		240,	2,		2,		1,		1, 		100, 			GameThread.CYCLE_TIME);
 		monsterCollection[9] = new Monster("Vampire",	sprite,		90,		270,	9,		9,		5,		5, 		100, 			GameThread.CYCLE_TIME);
 																//monsterCollection[0].getXPos() , monsterCollection[0].getYPos()
 																//was trying to get XPos and YPos of NULL objects
 	}
 
 	// 2d array of items, where the each row is a different type of item and the each column represents a different object of type item where the greater
 	// the column index the greater the item level
 	//Attributes for Items: 	String _name, Sprite _sprite, int _xPos, int _yPos, int _level, int _state, int _type
 	//Attributes for Weapon: 	String _name, Sprite _sprite, int _xPos, int _yPos, int _level, int _state, int _type, int _damage
 	//Attributes for Armor:		String _name, Sprite _sprite, int _xPos, int _yPos, int _level, int _state, int _type, int _defense
 	//Attributes forPotion:		String _name, Sprite _sprite, int _xPos, int _yPos, int _level, int _state, int _type, int _hpBoost
 
 	//Assuming state = 0 for all items and type = 1 for weapons, 2 for armor, 3 for potions
 	public void createItems(Sprite itemSprites[][]){
 											//Name			//Sprite				//XPos								//YPos				//Lvl    State    type dmg
 		itemCollection[0][0] = new Weapon("Rusty Sword",	itemSprites[0][0],	itemCollection[0][0].getXPos(),	itemCollection[0][0].getYPos(),	1,		0,		1,	1);
 		itemCollection[0][1] = new Weapon("Iron Sword",		itemSprites[0][1],	itemCollection[0][1].getXPos(),	itemCollection[0][1].getYPos(),	3,		0,		1,	5);
 		itemCollection[0][2] = new Weapon("Steel Sword",	itemSprites[0][2],	itemCollection[0][2].getXPos(),	itemCollection[0][2].getYPos(),	5,		0,		1,	10);
 																																								  //def
 		itemCollection[1][0] = new Armor("Rusty Armor",		itemSprites[1][0],	itemCollection[1][0].getXPos(),	itemCollection[1][0].getYPos(),	1,		0,		2,	1);
 		itemCollection[1][1] = new Armor("Leather Armor",	itemSprites[1][1],	itemCollection[1][1].getXPos(),	itemCollection[1][1].getYPos(),	2,		0,		2,	3);
 		itemCollection[1][2] = new Armor("Iron Armor",		itemSprites[1][2],	itemCollection[1][2].getXPos(),	itemCollection[1][2].getYPos(),	3,		0,		2,	5);
 																																								  //hpGiven
 		itemCollection[2][0] = new Potion("Minor Potion",	itemSprites[2][0],	itemCollection[2][0].getXPos(),	itemCollection[1][0].getYPos(),	1,		0,		3,	10);
 		itemCollection[2][1] = new Potion("Small Potion",	itemSprites[2][1],	itemCollection[2][1].getXPos(),	itemCollection[1][1].getYPos(),	3,		0,		3,	30);
 		itemCollection[2][2] = new Potion("Medium Potion",	itemSprites[2][2],	itemCollection[2][2].getXPos(),	itemCollection[1][2].getYPos(),	5,		0,		3,	50);
 	}
 
 	//NOTE: use when only a linear section of monsterCollection needs to be copied
 	//A method which takes two position markers and a length parameter. srcPos specifies the start point of
 	//the array monsterCollection from which to start copying. destPos specifies the start position of the
 	//return array. length specifies the number of elements to copy over
 	public Monster[] getSimpleMonsterRange(int srcPos, int destPos, int length){
 		Monster[] result = new Monster[length+1];//Must be length + 1 to avoid out of bounds
 		System.arraycopy(monsterCollection, srcPos, result, destPos, length);
 		return result;
 	}
 	
 	//A method which returns an array of items which can consist of weapons,armor,potions or all
 	//As parameters it takes four integers. 
 	//The first integer specifies the lowest row to select in the item array, while the second integer specifies largest row to select
 	//The third integer specifies the lowest column to select in the item array, while the fourth integer specifies largest column to select
 	//Example: 4x4 array with (0,0) being 0, (0,1) being 1...... and (3,3) being 15 rowMax = 3,rowMin = 1,colMax = 3,colMin = 1 
 	// 0	1	2	3
 	// 4	5	6	7     -->	5	6	7
 	// 8	9	10	11			9	10	11
 	// 12	13	14	15			13	14	15
 	public Item[][] getItemRange(int rowMin, int rowMax, int colMin, int colMax){
 		int i, j, k = 0, h = 0;
 		int rowIndex = rowMax - rowMin + 1;
 		int colIndex = colMax - colMin + 1;
 		Item[][] result = new Item[rowIndex][colIndex];
 		for(i = rowMin; i <= rowMax; i++){
 			for(j = colMin; j <= colMax; j++){
 				result[k][h++] = itemCollection[i][j];
 			}
 			k++;
 			h = 0;
 		}
 		return result;
 	}
 
 	// NOTE: when used with 2d array, copies over number of rows given by length
 	// A method which takes two position markers and a length parameter. srcPos
 	// specifies the start point of the array row itemCollection from which to start copying. destPos
 	// specifies the start position of the return array. length specifies the number of elements to copy over
 	public Item[][] getSimpleItemRange(int srcPos, int destPos, int length) {
 		Item[][] result = new Item[length + 1][3];// SECOND ARRAY BOUND MUST BE SAME AS ITEMCOLLECTION
 		System.arraycopy(itemCollection, srcPos, result, destPos, length);
 		return result;
 	}
 	public void updateNPC(Canvas canvas) {
 		elaineNPC.update(canvas);
 	}
 	
     @Override
     public boolean onTouchEvent(MotionEvent event){
     	/*
     	 * Get area touched
     	 */
     	
     	input.onTouchEvent(event);
     	
     	if (input.pressed_down) {
 //    		elaineNPC.setState(!elaineNPC.isState());
     	}
     	
     	return true;
     }
     
 }
 
