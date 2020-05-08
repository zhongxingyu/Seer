 import java.io.*;
 import java.awt.*;
 import java.applet.*;
 import java.net.*;
 import java.util.Random;
 import java.awt.event.*;
 import java.awt.image.BufferedImage;
 import java.awt.image.RescaleOp;
 import java.lang.Math;
 
 public class RPG extends Applet implements KeyListener
 {
 	
 	private static final long serialVersionUID = 2439786621293046662L;
 	static int TILESIZE = 20;
 	static int NUMMAPS = 4;
 	static int MAPWIDTH[] = {400,90,43,80};
 	static int MAPHEIGHT[] = {300,70,50,60};
 	static int TILETYPES = 50;
 	static int NUMSPRITES = 2;
 	static int NUMITEMS = 11;
 	static int NUMICONS = 7;
 	static int NUMMONSTERS = 10;
 	static int NUMSPELLS = 4;
 	static int NUMBOSSES = 1;
 	static int NUMSOUNDCLIPS = 2;
 	static int WALKINGDELAY = 15; //default 125
 	static int BATTLEFREQUENCY = 0; //percentage of encounter per step (default 3)
 	static int FPS = 60;
 	static int STARTMAP = 3;
 	
 	int boss=0;
 	
 	double t = 0.0;
 	double dt = 1.0/FPS;
 	
 	
 	boolean running;// = true;
 	
 	TileData td = new TileData();
 	int appSizeX = 800;
 	int appSizeY = 600;
 	private TileMap[] theMap = new TileMap[NUMMAPS];
 	int startx = appSizeX/2/TILESIZE;
 	int starty = appSizeY/2/TILESIZE;
 	
 	long prevPaint = 0;
 	long currPaint = 0;
 	
 	int nearSprite = -1;
 	int shopkeeperID = 1;
 	
 	static boolean music = true;
 	boolean sound = true;	
 	SoundClip[] soundClips = new SoundClip[NUMSOUNDCLIPS];
 	SoundClip hit, death, battlemusic, bossmusic, outofbounds;
 	
 	Image[] tileImages = new Image[TILETYPES];
 	Image[] monsterImages = new Image[NUMMONSTERS];
 	Image[] icons = new Image[NUMICONS];
 	
 	BufferedImage mapbuff;
 
 	
 	Player p = new Player(startx, starty,20,5,6,3,2,20,0,1, "Batman", STARTMAP);
 	Monster m;
 	Spell[] spell = new Spell[NUMSPELLS];
 	Battle b;
 	HUD hud;
 	Sprite[] sp = new Sprite[NUMSPRITES];
 	Sprite pSp;
 	Item[] item = new Item[NUMITEMS];
 	Item[] shop = new Item[NUMITEMS];
 	Pointer c = new Pointer(0);
 	Minimap mm;
 	
 	Random rand = new Random(); 
  	Font title = new Font("DialogInput",Font.BOLD,20);	
 	
 	BufferedReader br;
 	AudioClip intro;
     URL base;
 	WaitThread wait;
     MediaTracker mt;
 	
 	boolean debug = true; // toggle with f
 	boolean released = false;
 	boolean key_space = false;
 	boolean key_enter = false;
 	boolean run = false;
 	boolean battle = false;
 	boolean firsttimebattle = true;
 	boolean firststep = false;
 	boolean oktomove = true;
 	boolean withinrangesprite = false;
 	
 	boolean showinventory = false;
 	boolean showstats = false;
 	boolean showminimap = false;
 	boolean showinteraction = false;
 	
 	boolean mapupdated = true;
 
 	
 	Message msg;
 	 
 	public static void main(String[] args) { } 
 	
 	public void init()
 	{
 
 		
 		setSize(appSizeX, appSizeY);
 		mapbuff = new BufferedImage(appSizeX, appSizeY, BufferedImage.TYPE_INT_RGB);
 		
 		running = true;
 		
 		msg = new Message(appSizeX, appSizeY);
 		msg.setTextAndStart("Hi BAMasdfAN HW QR YOU D)TAY!(", 2000);
 		
 		
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
 			shop[i] = item[i];	
 		}
 		for(int i = 0; i < spell.length; i++) {
 			spell[i] = new Spell(i,"data/spells.txt");	
 		}
 		p.addItem(item[0]);
 		p.addItem(item[5]);
 		
 		
 		hit = new SoundClip("hit");
 		death = new SoundClip("death");
 		battlemusic = new SoundClip("battlemusic");
 		bossmusic = new SoundClip("bossmusic");
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
 		 
 		Image npc = getImage(base,"images/entities/npc/npc.png");
 		Image npc1 = getImage(base,"images/entities/npc/npc1.png");
 		Image npc2 = getImage(base,"images/entities/npc/npc2.png");
 		Image npc1a = getImage(base,"images/entities/npc/npc1a.png");
 		Image npc2a = getImage(base,"images/entities/npc/npc2a.png");
         Image[] npcImgs = new Image[] {npc, npc1, npc2, npc1a, npc2a};
 				
 		Image u = getImage(base,"images/entities/player/u.png");
 		Image l = getImage(base,"images/entities/player/l.png");
 		Image d = getImage(base,"images/entities/player/d.png");
 		Image r = getImage(base,"images/entities/player/r.png");
 		Image u1 = getImage(base,"images/entities/player/u1.png");
 		Image l1 = getImage(base,"images/entities/player/l1.png");
 		Image d1 = getImage(base,"images/entities/player/d1.png");
 		Image r1 = getImage(base,"images/entities/player/r1.png");
 		Image u2 = getImage(base,"images/entities/player/u2.png");
 		Image l2 = getImage(base,"images/entities/player/l2.png");
 		Image d2 = getImage(base,"images/entities/player/d2.png");
 		Image r2 = getImage(base,"images/entities/player/r2.png");
 		Image u3 = getImage(base,"images/entities/player/u3.png");
 		Image l3 = getImage(base,"images/entities/player/l3.png");
 		Image d3 = getImage(base,"images/entities/player/d3.png");
 		Image r3 = getImage(base,"images/entities/player/r3.png");
 		Image u4 = getImage(base,"images/entities/player/u4.png");
 		Image l4 = getImage(base,"images/entities/player/l4.png");
 		Image d4 = getImage(base,"images/entities/player/d4.png");
 		Image r4 = getImage(base,"images/entities/player/r4.png");
         Image[] playerDirs = new Image[] {u,u1,u2,u3,u4, r,r1,r2,r3,r4, d,d1,d2,d3,d4, l,l1,l2,l3,l4};
 				
 		
 		pSp = new Sprite(playerDirs, p, TILESIZE);
 		
         for (int i = 0; i < NUMSPRITES; i++)
         {
 			sp[i] = new Sprite(npcImgs, rand.nextInt(10)+20, rand.nextInt(10)+20, TILESIZE, i);
 			
 			sp[i].setSpeed(900+rand.nextInt(700));
 			sp[i].start();
         }
 		 
         try { 
               mt.waitForAll(); 
          } catch (InterruptedException  e) {}
 		  
 		addKeyListener(this);
 		
 		hud = new HUD(p, icons, shop);
 		mm = new Minimap(MAPWIDTH, MAPHEIGHT, TILESIZE, theMap, tileImages, p, this.getSize());
 			
 	}
 	
 	public boolean isFree() {
 		return (!battle
 			 && !showinventory
 			 && !showinteraction
 			 && !showstats
 			 && !mm.stopMovement());
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
 				if (isFree()) step('l');
 				oktomove=false;
 			}
 			if(key==KeyEvent.VK_S)
 			{
 				released = false;
 				c.setPointer(2);
 				if (isFree()) step('d');
 				oktomove=false;
 			}
 			if(key==KeyEvent.VK_D)
 			{
 				released = false;
 				c.setPointer(3);
 				if (isFree()) step('r');
 				oktomove=false;
 			}
 			if(key==KeyEvent.VK_W)
 			{
 				released = false;
 				c.setPointer(0);
 				if (isFree()) step('u');
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
 		int xDraw = 0;
 		int yDraw = 0;
 
 		Dimension appletSize = this.getSize();
 		int appSizeY = appletSize.height;
 		int appSizeX = appletSize.width;
 		
 		//System.out.println(mapupdated);
 		if(mapupdated) {
 			mapupdated = false;
 		} else {
 			return;
 		}
 		
 		mapbuff = new BufferedImage(appSizeX, appSizeY, BufferedImage.TYPE_INT_RGB);
 		
 		Graphics gbuff = mapbuff.createGraphics();
 		
 		for(int y=0; y<appSizeY/TILESIZE; y++) {
 			for(int x=0; x<appSizeX/TILESIZE; x++) {
 				gbuff.drawImage(tileImages[3], x*TILESIZE, y*TILESIZE, this);
 			}
 		}
 		
 		for(int y=Math.max(0,p.getY()-starty); y<MAPHEIGHT[p.getMapTracker()] && yDraw <= appSizeY; y++) {
 			for(int x=Math.max(0,p.getX()-startx); x<MAPWIDTH[p.getMapTracker()]; x++) {
 				xDraw = (startx-p.getX())*TILESIZE + x*TILESIZE;
 				yDraw = (starty-p.getY())*TILESIZE + y*TILESIZE;
 				if(xDraw >= appSizeX) break;
 				
 				gbuff.drawImage(tileImages[theMap[p.getMapTracker()].getVal(x,y)],xDraw,yDraw, TILESIZE, TILESIZE,this);
 				
 			}
 		}
 				
 	}
 	
 	public void HandlePointer() {
 	
 		
 		if (c.getPointer()==7)
 		{
 			if(showinventory) showinventory=false;
 			else showinventory=true;	
 			c.setPointer(5);
 		}
 		
 		if (c.getPointer()==11&&nearSprite>=0)
 		{ // c 11 = E
 			if(showinteraction) {
 				showinteraction = false;
 				nearSprite = -1;
 			}
 			else showinteraction=true;
 			if(nearSprite >= 0) showinteraction=true;
 			
 			System.out.println(c.getPointer());	
 				
 			c.setPointer(5);
 			
 		}
 		
 		if (c.getPointer()==12)
 		{ // c 12 = R
 			if(showstats) {
 				showstats = false;
 			}
 			else showstats=true;
 				
 			c.setPointer(5);
 			
 		}
 		
 		if (c.getPointer()==13)
 		{ // c 13 = M
 			mm.toggleState();
 			showminimap = mm.isVisible();
 			//System.out.println(mm.isVisible());
 			c.setPointer(5);
 			
 		}
 		
 	}
 	
 	public void PlayerMenu(Graphics g)
 	{
 		hud.draw(g);
 		if (showinventory&&!showinteraction)
 		{
 			hud.drawInventory(g,c);
 		}
 		if (showinteraction&&nearSprite==shopkeeperID)
 		{
 			hud.drawShop(g,c);
 		}	
 		if (showstats)
 		{
 			hud.drawStats(g,p,c);
 		}
 		if (showminimap)
 		{
 			mm.draw(g,p.getMapTracker());
 		}	
 	}
 	public void update(Graphics g) {
 	
 		if(!running) return;
 		
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
 	
 	 
 	public void destroy() {
 		intro.stop();
 		running = false;
 	}
 	
 	public void stop() {
 		intro.stop();
 		running = false;
 	}
 	
 	public void start() {
 		if(running == false)
 			intro.play();
 		running = true;
 	}
 	
 	public void paint(Graphics g)
 	{
 		Graphics2D g2d = (Graphics2D) g;
 		
 		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
 	
 		try { Thread.sleep((int)(1000.0/FPS)); }
 		catch(InterruptedException e) {}
 		
 
 		prevPaint = System.currentTimeMillis();
 		int dtpaint = (int)(prevPaint - currPaint);
 		float fps = 1000.0F/(float)dtpaint;
 		currPaint = prevPaint;
 		
 		
 		t += dt;
 		if(t > 10) t = 0.0;
 		
 		/////////draw map
 		//could tie this to weather/time/caves maybe?
 		//float scaleFactor = 1.2F; // 0.0F to 1.0F for darkness, if you go higher than 1.0, it makes it brighter
 		float scaleFactor = (float)Math.abs(Math.sin(t));
 		//System.out.println(t + " " + scaleFactor);
 		RescaleOp op = new RescaleOp(scaleFactor, 0, null);
 		BufferedImage mapbuff2 = op.filter(mapbuff, null);
 		//to disable darkness, change mapbuff2 below to mapbuff
 		g.drawImage(mapbuff2, 0, 0, this);
 		////////draw map
 		
 		if(!battle)
 		{
 			p.setBattleCondition(false);
 			DrawMap(g);	
 			HandlePointer();
 			
 			if(showinteraction && nearSprite >= 0) {
 				hud.drawInteractionPane(g,nearSprite);
 			}
 			
 			pSp.setSpeed(40);
 			pSp.updateAnimationP(g, System.currentTimeMillis());
 			
 			
 			msg.update(g, System.currentTimeMillis());
 			
 			for (int i = 0; i< NUMSPRITES; i++)
          	{
 				if(sp[i].isReady()&&p.getMapTracker()==1) {
 					sp[i].updateAnimationRand(g, System.currentTimeMillis(), p);
 					sp[i].allowMove(theMap[p.getMapTracker()].getNeighbors(sp[i].getX(), sp[i].getY()));
 				}
          	}
 			PlayerMenu(g);
 		}
 		
 		else if (battle)
 		{
 			
 			msg.stop();
 			p.setBattleCondition(true);
 			if(firsttimebattle) {
 				wait.suspend();
 				intro.stop();
 				b = new Battle(p, monsterImages,c,icons,hit,death,battlemusic,bossmusic,boss,spell);
 				firsttimebattle = false;
 			} else {
 				
 				if(!b.BattleSequence(g, key_space)) {
 					battle = false;
 					firsttimebattle = true;
 					battlemusic.stop();
 					bossmusic.stop();
 					death.stop();
 					intro.play();
 					wait.resume();
 					boss=0;
 				}
 				key_space=false;
 			}	
 		}
 		
 		////////////////////////
 		/////DEBUG DISPLAY//////
 		////////////////////////
 		if(debug) {
 			int endPaint = (int)(System.currentTimeMillis() - currPaint);
 			Color tempc = g.getColor();
 			Composite original = g2d.getComposite();
 			
 			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.75F));
 			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
 			
 			g.setColor(Color.BLACK);
 			
 			g.fillRoundRect(610,460, 175, 130, 20, 20);
 			g.setColor(Color.WHITE);
 
 			int specTile1 = theMap[p.getMapTracker()].getSpecial1(p.getX(), p.getY());
 			int specTile2 = theMap[p.getMapTracker()].getSpecial2(p.getX(), p.getY());
 			//int specTile3 = theMap[p.getMapTracker()].getSpecial3(p.getX(), p.getY());
 			
 			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85F));
 			g.drawString("curr tile:  " + theMap[p.getMapTracker()].getVal(p.getX(), p.getY()), 620,480);
 			g.drawString("face tile:  " + theMap[p.getMapTracker()].getFacing(p.getX(), p.getY(), p.getFacing()), 620,500);
 			g.drawString("s1: " + specTile1 + " s2: " + specTile2, 620,520);
 			
 			g.drawString("(x,y):     (" + p.getX() + "," + p.getY() + ")", 620,540);
 			g.drawString("fps:        " + Math.round(fps), 620,560);
 			g.drawString("ms to paint:" + endPaint, 620,580);
 			
 			g.setColor(tempc);
 			g2d.setComposite(original);
 		}
 		////////////////////////
 		/////DEBUG DISPLAY//////
 		////////////////////////
 		
 	}
 
 	public void step(char direction) {
 		
 		outofbounds.stop();
 		switch(direction) {
 			case 'l':
 				if(!p.moveLeftB()) {
 					outofbounds.play();
 					msg.setTextAndStart("You can\'t move there!",600);
 				}
 				break;
 			case 'r':
 				if(!p.moveRightB()) {
 					outofbounds.play();
 					msg.setTextAndStart("You can\'t move there!",600);
 				}
 				break;
 			case 'u':
 				if(!p.moveUpB()) {
 					outofbounds.play();
 					msg.setTextAndStart("You can\'t move there!",600);
 				}
 				break;
 			case 'd':
 				if(!p.moveDownB()) {
 					outofbounds.play();
 					msg.setTextAndStart("You can\'t move there!",600);
 				}
 				break;
 		}
 		mapupdated = true;
 		
 		int currTile = theMap[p.getMapTracker()].getVal(p.getX(), p.getY());
 		//int facingTile = theMap[p.getMapTracker()].getFacing(p.getX(), p.getY(), p.getFacing());
 		//{x, y}
 		int[] facingCoords = theMap[p.getMapTracker()].getFacingCoords(p.getX(), p.getY(), p.getFacing());
 
 		int specTile1 = theMap[p.getMapTracker()].getSpecial1(p.getX(), p.getY());
 		int specTile2 = theMap[p.getMapTracker()].getSpecial2(p.getX(), p.getY());
		int specTile3 = theMap[p.getMapTracker()].getSpecial3(p.getX(), p.getY());
		System.out.println("currTile: " + currTile + " spec1: " + specTile1
				+ " spec2: " + specTile2 + " spec3: " + specTile3);
 		
 		pSp.start();
 		
 		//withinrangesprite = false;
 		nearSprite = -1;
 		for (int i=0;i< sp.length;i++)
 		{
 			
 			if( (sp[i].getX() == facingCoords[0]
 			 &&  sp[i].getY() == facingCoords[1])
 			 || (sp[i].getX() == p.getX())
 			 &&  sp[i].getY() == p.getY() )
 			{
 				nearSprite = sp[i].getID();
 				break;
 			}
 		}	
 		
 		if ((rand.nextInt(1000) < BATTLEFREQUENCY * 10||(p.getX()==85&&p.getY()==57))
			&& !td.isBattleRestricted(currTile))
 		{
 			if(p.getX()==85&&p.getY()==57)
 				boss=1; 
 			battle=true;
 		}
 		
 		if (specTile1==1)
 		{
 			System.out.println(specTile2);
 			msg.setTextAndStart("Changing maps", 1500);
 			//if (p.getMapTracker()==1)
 			{
 				if (p.getMapTracker()==0)
 				{
 					hud.updateNPCInfo();
 					
 					p.setTownX(p.getX());
 					p.setTownY(p.getY());
 					p.setX(p.getTownEntranceX());
 					p.setY(p.getTownEntranceY());	
 				}
 				else
 				{
 					p.setX(p.getTownX());
 					p.setY(p.getTownY());
 				}
				BATTLEFREQUENCY=specTile3;
 				p.setMapTracker(specTile2);
 				
 			}
 			/*else if (p.getMapTracker()==0)
 			{
 				p.setMapTracker(1);
 				hud.updateNPCInfo();
 				
 				p.setTownX(p.getX());
 				p.setTownY(p.getY());
 				p.setX(p.getTownEntranceX());
 				p.setY(p.getTownEntranceY());	
 			}*/
 			//System.out.println(p.getMapTracker());
 			
 			
 			for (int i=0;i< NUMSPRITES;i++)
 			{
 				sp[i].resetOrigin();
 			}	
 		}
 		if (td.isBed(currTile))
 		{
 			if( ( p.getHealth()!=p.getHealthMax()
 			   || p.getMana()!=p.getManaMax() ) && p.getGold()>=7 )
 			{
 				p.setHealth(p.getHealthMax());
 				p.setMana(p.getManaMax());
 				p.pay(7);
 			}
 		}
 		if(sound) {	
 			int sID = td.getSoundID(currTile);
 			if(sID != -1) {
 				soundClips[sID].stop();
 				soundClips[sID].play();	
 			}
 		}
 		p.allowMove(theMap[p.getMapTracker()].getNeighbors(p.getX(), p.getY()));
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
 		if(key==KeyEvent.VK_R)
 		{
 			if (!battle)
 				c.setPointer(12);
 		}
 		if(key==KeyEvent.VK_M)
 		{
 			if (!battle)
 				c.setPointer(13);
 		}
 		
 		if(key==KeyEvent.VK_F)
 		{
 			if(debug) debug = false;
 			else debug = true;
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
