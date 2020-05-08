 import java.io.*;
 import java.awt.*;
 import java.applet.*;
 import javax.swing.*;
 import java.util.*;
 import java.awt.image.*;
 import java.net.*;
 import javax.imageio.*;
 import java.util.Random;
 import java.awt.event.*;
 import java.lang.Math;
 import javax.sound.sampled.*;
 
 public class RPG extends Applet implements KeyListener
 {
 	static int TILESIZE = 20;
 	static int NUMMAPS = 2;
 	static int MAPWIDTH[] = {400,90};
 	static int MAPHEIGHT[] = {300,70};
 	static int TILETYPES = 14;
 	static int NUMSPRITES = 5;
 	static int NUMITEMS = 6;
 	static int NUMICONS = 4;
 	static int NUMMONSTERS = 6;
 	static int NUMSOUNDCLIPS = 2;
 	static int WALKINGDELAY = 125;
 	static int BATTLEFREQUENCY = 2; //percentage of encounter per step
 	static String map;
 	
 	TileData td = new TileData();
 	int appSizeX = 800;
 	int appSizeY = 600;
 	private TileMap[] theMap= new TileMap[2];
 	int startx=800/2/TILESIZE;//MAPWIDTH / 2;
 	int starty=600/2/TILESIZE;//MAPHEIGHT / 2;
 	int maptracker = 0;
 	//WalkingThread walking;
 	int offsetX = 0;
 	int offsetY = 0;
 	int checkx = 0;
 	int checky = 0;
 	int playerposition=1;
 	int mapx=0;
 	int mapy=0;
 	
 	int nearSprite = -1;
 	
 	static boolean music = true;
 	boolean sound = true;	
 	SoundClip[] soundClips = new SoundClip[NUMSOUNDCLIPS];
 	SoundClip hit;
 	SoundClip battlemusic;
 	SoundClip outofbounds;
 	
 	Image[] tileImages = new Image[TILETYPES];
 	Image[] monsterImages = new Image[NUMMONSTERS];
 	Image[] icons = new Image[NUMICONS];
 
 	Image player;
 	Image player1;
 	Image player2;
 	Image player1a;
 	Image player2a;
 	Image u,u1,u2,u3,u4, l,l1,l2,l3,l4, d,d1,d2,d3,d4, r,r1,r2,r3,r4;
 	
 	
 	Player p = new Player(startx, starty,20,5,6,3,2,20,0,1, "Batman");
 	Monster m;
 	Battle b;
 	HUD hud;
 	Sprite[] sp= new Sprite[NUMSPRITES];
 	Sprite pSp;
 	Item[] item= new Item[NUMITEMS];
 	Pointer c = new Pointer(0);
 	
 	Random rand = new Random(); 
  	Font title = new Font("DialogInput",Font.BOLD,20);	
 	
 	BufferedReader br;
 	AudioClip intro;
     URL base;
 	WaitThread wait;
     MediaTracker mt;
 	
 	boolean released = false;
 	boolean key_space=false;
 	boolean key_enter=false;
 	boolean run=false;
 	boolean battle=false;
 	boolean firsttimebattle = true;
 	boolean firststep = false;
 	boolean oktomove = true;
 	boolean showinventory=false;
 	boolean showInteraction=false;
 	boolean withinrangesprite=false;
 
 	 
 	public static void main(String[] args) { } 
 	
 	public void init()
 	{
 		System.out.println("near beginning of init in RPG.java");
 		
 		for (int i=0;i<NUMMAPS;i++)
 			theMap[i] = new TileMap(MAPWIDTH[i], MAPHEIGHT[i], "maps/file"+i+".txt");
 		start();
 		wait = new WaitThread();
 		wait.start();
 		try 
 		{
 			URL soundURL = getClass().getResource("music/glory.wav");
 			intro = Applet.newAudioClip(soundURL);
 		} catch (Exception e) {System.out.println("failed to load clip");}
 		intro.play();
 		
 		for(int i = 0; i < soundClips.length; i++) {
 			soundClips[i] = new SoundClip("clip" + Integer.toString(i));
 		}
 		for(int i = 0; i < item.length; i++) {
 			item[i] = new Item(i,"items/items.txt");
 			p.addItem(item[i]);
 		}
 		
 		hit = new SoundClip("hit");
 		battlemusic = new SoundClip("battlemusic");
 		outofbounds = new SoundClip("goat");
 		
 		try { 
 			base = getDocumentBase();    
 		} catch (Exception e) {} 	
 
         mt = new MediaTracker(this);
 		for(int i = 0; i < tileImages.length; i++) {
 			Image img = getImage(base, "images/tiles/" + Integer.toString(i) + ".png");
 			mt.addImage(img, i+1);
 			tileImages[i] = img;
 		}
 		for(int i = 0; i < monsterImages.length; i++) {
 			Image img = getImage(base, "images/entities/monsters/id"+Integer.toString(i) + ".png");
 			mt.addImage(img, i+1);
 			monsterImages[i] = img;
 		}
 		 for(int i = 0; i < icons.length; i++) {
 			Image img = getImage(base, "images/icons/"+Integer.toString(i) + ".png");
 			mt.addImage(img, i+1);
 			icons[i] = img;
 		}
 		 
 		player = getImage(base,"images/entities/player/player.png");
 		player1 = getImage(base,"images/entities/player/player1.png");
 		player2 = getImage(base,"images/entities/player/player2.png");
 		player1a = getImage(base,"images/entities/player/player1a.png");
 		player2a = getImage(base,"images/entities/player/player2a.png");
         Image[] playerImgs = new Image[] {player, player1, player2, player1a, player2a};
 				
 		u = getImage(base,"images/entities/playertestb/u.png");
 		l = getImage(base,"images/entities/playertestb/l.png");
 		d = getImage(base,"images/entities/playertestb/d.png");
 		r = getImage(base,"images/entities/playertestb/r.png");
 		u1 = getImage(base,"images/entities/playertestb/u1.png");
 		l1 = getImage(base,"images/entities/playertestb/l1.png");
 		d1 = getImage(base,"images/entities/playertestb/d1.png");
 		r1 = getImage(base,"images/entities/playertestb/r1.png");
 		u2 = getImage(base,"images/entities/playertestb/u2.png");
 		l2 = getImage(base,"images/entities/playertestb/l2.png");
 		d2 = getImage(base,"images/entities/playertestb/d2.png");
 		r2 = getImage(base,"images/entities/playertestb/r2.png");
 		u3 = getImage(base,"images/entities/playertestb/u3.png");
 		l3 = getImage(base,"images/entities/playertestb/l3.png");
 		d3 = getImage(base,"images/entities/playertestb/d3.png");
 		r3 = getImage(base,"images/entities/playertestb/r3.png");
 		u4 = getImage(base,"images/entities/playertestb/u4.png");
 		l4 = getImage(base,"images/entities/playertestb/l4.png");
 		d4 = getImage(base,"images/entities/playertestb/d4.png");
 		r4 = getImage(base,"images/entities/playertestb/r4.png");
         Image[] playerDirs = new Image[] {u,u1,u2,u3,u4, r,r1,r2,r3,r4, d,d1,d2,d3,d4, l,l1,l2,l3,l4};
 				
 		
 		pSp = new Sprite(playerDirs, p, TILESIZE);
 		
         for (int i = 0; i < NUMSPRITES; i++)
         {
 			sp[i] = new Sprite(playerImgs, rand.nextInt(10)+3, rand.nextInt(10)+3, TILESIZE, i);
 			// i at the end is the sprite ID, so we can later identify which sprite is which
 			// useful if we have different NPC types
 			sp[i].setSpeed(900+rand.nextInt(700));
 			sp[i].start();
         }
 		 
         try { 
               mt.waitForAll(); 
          } catch (InterruptedException  e) {}
 		  
 		addKeyListener(this);
 		
 		hud = new HUD(p, icons);
 		
 		p.equip(item[5]);	
 			
 	}
 	
 	
   	public void keyPressed(KeyEvent evt) 
 	{
 		int key=0; key = evt.getKeyCode(); 
 		if (oktomove)
 		{
 			if(key==KeyEvent.VK_A)
 			{
 				released = false;
 				c.setPointer(1);
 				if (!battle&&!showinventory) step('l');
 				oktomove=false;
 			}
 			if(key==KeyEvent.VK_S)
 			{
 				released = false;
 				c.setPointer(2);
 				if (!battle&&!showinventory) step('d');
 				oktomove=false;
 			}
 			if(key==KeyEvent.VK_D)
 			{
 				released = false;
 				c.setPointer(3);
 				if (!battle&&!showinventory) step('r');
 				oktomove=false;
 			}
 			if(key==KeyEvent.VK_W)
 			{
 				released = false;
 				c.setPointer(0);
 				if (!battle&&!showinventory) step('u');
 				oktomove=false;
 			}
 			/*if(key==KeyEvent.VK_SPACE)
 			{
 				c.setPointer(10);
 			}*/
 			if(key==KeyEvent.VK_ENTER)
 			{
 				c.setPointer(7);
 				key_enter=true;
 			}
 		}
 	}
 	
 	public class WaitThread extends Thread { //create thread class
 		WaitThread() {//bring in arguments for the thread to use	
 		}
 		public void run() { //executes when thread is called
 			while(true)
 			{
 				delay(WALKINGDELAY);
 				oktomove=true;
 				repaint();	
 			}	
 		}
 	}
 	
 	public void DrawMap(Graphics g)
 	{	
 		
 		for(int x=0; x<MAPWIDTH[maptracker];x++) {
 			for(int y=0; y<MAPHEIGHT[maptracker];y++) {
 				g.drawImage(tileImages[theMap[maptracker].getVal(x,y)],
 						   (startx-p.getX())*TILESIZE + x*TILESIZE,
 						   (starty-p.getY())*TILESIZE + y*TILESIZE, this);
 			}
 		}
 		if (c.getPointer()==7)
 		{
 			if(showinventory) showinventory=false;
 			else showinventory=true;	
 			c.setPointer(5);
 		}
 		
 		if (c.getPointer()==11)
 		{
 			if(showInteraction) {
 				showInteraction = false;
 				nearSprite = -1;
 			}
 			if(nearSprite >= 0) showInteraction=true;
 			
 			System.out.println(c.getPointer());	
 				
 			c.setPointer(5);
 			
 		}
 		
 		repaint();			
 	}
 	public void PlayerMenu(Graphics g)
 	{
 		hud.draw(g);
 		if (showinventory)
 		{
 			hud.drawInventory(g,c);
 		}
 	}
 	
 	public void update(Graphics g) {
 		Graphics offgc;
 		Image offscreen = null;
 		Dimension d = size();
 		offscreen = createImage(d.width, d.height);
 		offgc = offscreen.getGraphics();
 		offgc.setColor(getBackground());
 		offgc.fillRect(0, 0, d.width, d.height);
 		offgc.setColor(getForeground());
 		paint(offgc);
 		g.drawImage(offscreen, 0, 0, this);
 	}
 	
 	public void paint(Graphics g)
 	{	
 	
 		if(!battle)
 		{
 			p.setBattleCondition(false);
 			DrawMap(g);	
 			
			if(showInteraction && withinrangesprite) {
 				hud.drawInteractionPane(g,nearSprite);
 			}
 			
 			pSp.setSpeed(40);
 			pSp.updateAnimationP(g, System.currentTimeMillis());
 			
 			for (int i = 0; i< NUMSPRITES; i++)
          	{
 				if(sp[i].isReady()&&maptracker==1) {
 					sp[i].updateAnimationRand(g, System.currentTimeMillis(), p);
 					sp[i].allowMove(theMap[maptracker].getNeighbors(sp[i].getX(), sp[i].getY()));
 				}
          	}
 			PlayerMenu(g);
 		}
 		
 		else if (battle)
 		{
 			
 			p.setBattleCondition(true);
 			if(firsttimebattle) {
 				wait.suspend();
 				intro.stop();
 				battlemusic.play(true);
 				b = new Battle(g, p, monsterImages,c,hit,sp, icons);
 				firsttimebattle = false;
 			} else {
 				
 				if(!b.BattleSequence(g, key_space, key_enter)) {
 					battle = false;
 					firsttimebattle = true;
 					battlemusic.stop();
 					intro.play();
 					wait.resume();
 				}
 				key_space=false;
 			}	
 		}
 	}
 
 	public void step(char direction) {
 		
 		outofbounds.stop();
 		switch(direction) {
 			case 'l': if(!p.moveLeftB()) outofbounds.play(); break;
 			case 'r': if(!p.moveRightB()) outofbounds.play(); break;
 			case 'u': if(!p.moveUpB()) outofbounds.play(); break;
 			case 'd': if(!p.moveDownB()) outofbounds.play(); break;
 		}
 		
 		int currTile = theMap[maptracker].getVal(p.getX(), p.getY());
 		int facingTile = theMap[maptracker].getFacing(p.getX(), p.getY(), p.getFacing());
 		//{x, y}
 		int[] facingCoords = theMap[maptracker].getFacingCoords(p.getX(), p.getY(), p.getFacing());
 		
 		//keep for future tile debugging
 		
 		//System.out.println("P " + p.getX() + ", " + p.getY());
 		//System.out.println("currtile " + currTile);
 		//System.out.println("facing " + p.getFacing());
 		//System.out.println("facetile " + facingTile);
 		
 			pSp.start();
 		
 		withinrangesprite = false;
 		nearSprite = -1;
 		for (int i=0;i< NUMSPRITES;i++)
 		{
 			
 			if(theMap[maptracker].within(p, sp[i], 3)) {
 				withinrangesprite = true;
 			}
 				
 			if( (sp[i].getX() == facingCoords[0]
 			 &&  sp[i].getY() == facingCoords[1])
 			 || (sp[i].getX() == p.getX())
 			 &&  sp[i].getY() == p.getY() )
 			{
 				System.out.println("NEAR SPRITE id = " + sp[i].getID());
 				nearSprite = sp[i].getID();
 			}
 		}	
 		
 		if (rand.nextInt(1000) < BATTLEFREQUENCY * 10
 			&& !td.isBattleRestricted(currTile) && maptracker == 0)
 		{
 			battle=true;
 		}
 		
 		if (td.isTown(currTile))
 		{
 			if (maptracker==1)
 			{
 				maptracker=0;
 				p.setX(p.getTownX());
 				p.setY(p.getTownY());
 			}
 			else if (maptracker==0)
 			{
 				maptracker=1;
 				p.setTownX(p.getX());
 				p.setTownY(p.getY());	
 			}
 			System.out.println(maptracker);
 			
 			
 			for (int i=0;i< NUMSPRITES;i++)
 			{
 				sp[i].resetOrigin();
 			}	
 		}
 		if (td.isBed(currTile)&&p.getHealth()!=p.getHealthMax()&&p.getGold()>=20)
 		{
 			p.setHealth(p.getHealthMax());
 			p.pay(20);
 		}
 		if(sound) {	
 			int sID = td.getSoundID(currTile);
 			if(sID != -1) {
 				soundClips[sID].stop();
 				soundClips[sID].play();	
 			}
 		}
 		p.allowMove(theMap[maptracker].getNeighbors(p.getX(), p.getY()));
 	}
 
 	public void keyReleased(KeyEvent evt)	
 	{
 		int key=0; key = evt.getKeyCode(); 
 		if (battle)
 		oktomove=true;
 		repaint();
 		if(key==KeyEvent.VK_SPACE)
 		{
 			key_space=true;
 			if (!battle)
 			c.setPointer(10);
 		}
 		if(key==KeyEvent.VK_E)
 		{
 			if (!battle)
 				c.setPointer(11);
 		}
 	} 	
 	
     public void keyTyped(KeyEvent evt)		{
 			int key=0; key = evt.getKeyCode(); 
 			if(key==KeyEvent.VK_ENTER)
 			{
 				key_enter=true;
 			}
 			
     	} 
     public void delay(double n)
 	{
 		long startDelay = System.currentTimeMillis();
 		long endDelay = 0;
 		while (endDelay - startDelay < n)
 			endDelay = System.currentTimeMillis();	
 	}
 }
