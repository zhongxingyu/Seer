 import java.awt.Color;
 import java.awt.Dimension;
 
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Toolkit;
 //import java.awt.event.ActionEvent;
 //import java.awt.event.ActionListener;
 //import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.util.LinkedList;
 //import java.awt.event.KeyListener;
 
 //import javax.swing.JLabel;
 import javax.swing.JPanel;
 //import Object.EntityDestroyable;
 
 import Object.EntityDestroyable;
 import Object.EntityMapObject;
 import Object.EntityMovable;
 
 
 
 public class DungeonCrawlerGame extends JPanel implements Runnable {
 
 	/**
 	 * @author Dmytro Shlyakhov
 	 */
 	private static final long serialVersionUID = 1L;
 	//Double buffering
 	private Image dbImage;
 	private Graphics dbg;
 	//Jpanel variables
 	public String windowTitle;
 	static final int GWIDTH =1000, GHEIGHT =600;
 	static final Dimension gameDim = new Dimension(GWIDTH, GHEIGHT);
 	
 	// Game Variales
 	private Thread game;
 	private volatile boolean running = false;
 	private int currentLevel = 0;
 	private int currentRoom = 1;
 	double delta = 0; //Time var
 	private long bulletCoolOf =0;
 	//Game Objects
 	World world;
 	Player p1;
 	MyKeyListener k1;
 	NPC mob1;
 	Bullet b;
 	Shopping shop;
 	private Controller c;
 	private MainWindow mainWindow;
 	public LinkedList<EntityDestroyable> ed;
 	public LinkedList<EntityMovable> em;
 	public LinkedList<EntityMapObject> eMO;
 	
 	// private static boolean hitExit;
 	//Constructor
 	public DungeonCrawlerGame(MainWindow mainWindow){
 		this.mainWindow = mainWindow;
 		c = new Controller(this);
 		
 		world = new World(currentLevel + currentRoom,  this);
 		p1 = new Player(world);
 		ed = c.getEntDestrList();
 		em = c.getEntMovList();
 		eMO = c.getEntMO();
 		this.k1 = new MyKeyListener(); 
 		// mob1 = new NPC( 250, 26, this, p1);
 		b = new Bullet(p1.getX(), p1.getY(), p1,this);
 		addKeyListener(k1);
 		
 		
 		
 		setPreferredSize(gameDim);
 		setBackground(Color.BLACK);
 		setFocusable(true);
 		requestFocus(true);
 		
 		//Handles users key inputs
 	/*	addKeyListener(new KeyAdapter() {
 			public void keyPressed (KeyEvent e){
 				
 				int c = e.getKeyCode();
 			//	System.out.println(c);  //debugging string to see which number gets var c 
 				if(c==KeyEvent.VK_LEFT){
 					if (!p1.checkForCollision())
 					{
 					p1.setXDirection(-1);
 					}
 				}
 				
 				if(c== KeyEvent.VK_RIGHT){
 					if (!p1.checkForCollision())
 					{
 					p1.setXDirection(1);
 					}
 				}
 				if(c==KeyEvent.VK_UP){
 					if (!p1.checkForCollision())
 					{
 					p1.setYDirection(-1);
 					}
 				}
 				if(c==KeyEvent.VK_DOWN){
 					if (!p1.checkForCollision())
 					{
 					p1.setYDirection(1);
 					}
 				}
 				
 			}
 			public void keyReleased (KeyEvent e){
 				p1.setYDirection(0);
 				p1.setXDirection(0);
 			}
 			public void keyTyped (KeyEvent e){
 		
 			}
 		});
 		*/
 	} 
 	
 	public void changelevel(){
 	//	hitExit =true;
 		
 		
 	}
 	
 	public void newWorld(int levelNumber){
 		world = null;
 		world = new World(levelNumber, this);
 		p1.setWorld(world);
 //		p1 = null;
 //		p1 = new Player(world);
 	}
 	
 	public void run(){
 		
 		long lastTime = System.nanoTime();
 		final double ns = 1000000000.0 /200.0; //Make the divisor smaller to increase the SPEED
 		long timer =System.currentTimeMillis(); //Current time for FPS
 		// double delta = 0;
 		int frames =0;
 		int updates =0;
 		
 		while(running){ //We can do everything here : Main LOOP
 			long now = System.nanoTime();
 			
 			delta+= (now-lastTime)/ns;
 			lastTime = now;			
 			if (delta >=1){
 				if (!world.isPaused()) {
 					gameUpdate();
 					updates ++;
 					delta--;
 	/*				if (hitExit) {
               			newWorld(2);
               			hitExit=false;
 					}
 	*/
 					if (p1.isHitFinish()) {
 						mainWindow.showFinish();
 					}
 					if (!p1.isAlive()) {
 						mainWindow.showDCMenue();
 					}
 				}
 				gameRender();
 				paintScreen();
 			}
 			frames++;
 		//		System.out.println(System.currentTimeMillis() - timer );
 			if(System.currentTimeMillis() - timer > 1000){
 				timer +=1000;
 		
 				System.out.println(updates + " ups, "+ frames + " fps");
 			
 				updates=0;
 				frames=0;
 			}
 		}
 	}
 	
 	private void changestate(){
 		
 		
 		if ((p1.isHitShop()) & (k1.isKeyPressed(KeyEvent.VK_S))) {
 			world.pause();
 			shop = new Shopping(world,p1);
 			shop.setFrame(mainWindow);
 			addKeyListener(shop.getMyKeyListener());
 			shop.loadImage("background","shop (1).jpg","papyrus","Pap.png","mana","mana01.png","life","life01.png","weapon","ArmWaffe02.png","munze","Muenze6.png");
 		}
 		if (p1.isHitExit()) {
 			currentRoom++;
 			
 			if(currentRoom >4){
 			
 				currentLevel=currentLevel+currentRoom-2;
 				currentRoom = 1;
 			}
 			if(currentLevel>8)
 				currentLevel = 0;
 			if(currentRoom==4){
 	            newWorld(0);
 			}
 			else {
 	            newWorld(currentLevel+currentRoom);
 			}
             p1.changestate();
 		}
 		if(p1.playerChangeRoom){
 			if(p1.checkpointRoom < currentRoom){
 				currentRoom=p1.checkpointRoom;
 				p1.playerChangeRoom=false;
 				newWorld(currentLevel+currentRoom);
 				p1.changestate();
 			}
 		}
 	}
 	
 	private void shoot(long coolOf){
 		if (bulletCoolOf<System.nanoTime()) {
 			
 			bulletCoolOf = System.nanoTime()+coolOf;
 			if(c.em.size()< 500){
 				c.addEntity(new Bullet(p1.playerRect.getCenterX(), p1.playerRect.getCenterY(), p1, this));
 				bulletCoolOf = System.nanoTime()+coolOf;
 			}
 		}
 	}
 	
 	private void gameUpdate(){
 		if(running && game !=null){
 			//Update state
 			p1.setYDirection(0);
 			p1.setXDirection(0);
 			if(k1.isKeyPressed(KeyEvent.VK_UP)){
 				p1.setYDirection(-1);
 				p1.lastDirection =3;
 			}
 			else if(k1.isKeyPressed(KeyEvent.VK_DOWN)){
 				p1.setYDirection(+1);
 				p1.lastDirection =1;
 			}else{			
 				if(k1.isKeyPressed(KeyEvent.VK_LEFT)){
 						p1.setXDirection(-1);
 						p1.lastDirection =2;
 				}else if(k1.isKeyPressed(KeyEvent.VK_RIGHT)){
 						p1.setXDirection(+1);       
 						p1.lastDirection =0;
 				}
 			}
 			if (k1.isKeyPressed(KeyEvent.VK_SPACE)){
 				//c.addEntity(new Bullet(p1.playerRect.getCenterX(), p1.playerRect.getCenterY(), p1));
 				shoot(250000000);
 			}
 		c.update();	
 		p1.update(); //Updating Player
 		checkForCollision();
 //		if(mob1!=null)
 //		mob1.update();
 		
 		
 		
 //		if (!checkForCollision())
 //		{
 //			p1.update(); //Updating Player
 //		}
 //			if(k1.isKeyPressed(KeyEvent.VK_UP)){
 //				if (!checkForCollision())
 //				{
 //				p1.setYDirection(-1);				
 //				}
 //			}
 //			else if(k1.isKeyPressed(KeyEvent.VK_DOWN)){
 //				if (!checkForCollision())
 //				{
 //				p1.setYDirection(+1);
 //				}
 //			}else{			
 //				p1.setYDirection(0);
 //				if(k1.isKeyPressed(KeyEvent.VK_LEFT)){
 //					if (!checkForCollision())
 //					{
 //						p1.setXDirection(-1);
 //					}
 //				}else if(k1.isKeyPressed(KeyEvent.VK_RIGHT)){
 //					if (!checkForCollision())
 //					{
 //						p1.setXDirection(+1);
 //					}
 //				}else{			
 //					p1.setXDirection(0);
 //					}
 //			}
 //			p1.update(); //Updating Player
 		//	requestFocus(true); //to be able to move the player
 			
 			changestate();
 		}
 	}
 	
 	private void gameRender(){
 		if(dbImage == null){ //Create the Buffer -Image
 			dbImage = createImage(GWIDTH, GHEIGHT);
 			if(dbImage == null){
 				System.err.println("dbImage is still null");
 				return;
 			}else{
 				dbg = dbImage.getGraphics();
 			}
 				
 		}
 		//Clear the screen
 		dbg.setColor(Color.WHITE);
 		dbg.fillRect(0, 0, getWidth(), getHeight());
 		//Draw game elements
 		draw(dbg);
 	}
 	
 	
 	/*Draw all Game content in this method */
 	
 	public void draw (Graphics g){
 		if (!world.isPaused()) {
 			world.draw(g);
 			c.draw(g);
 			p1.draw(g); //Drawing Player
 		}
 //		if(mob1!=null)
 //		mob1.draw(g);
 		if (world.isPaused()) {
 			System.out.println("Zeige Shop");
 			g.drawImage(shop.paint(),0,0,mainWindow.getWidth(),mainWindow.getHeight(),null);
 			
 		}
 		
 	}
 
 	public boolean checkForCollision(){ //Checking for collision
 		boolean colide = false;
 		for(int i=0;i<world.AWIDTH;i++){
 			for(int j=0;j<world.AHIGHT;j++){
 				if(world.isSolid[i][j] && (p1.playerRect.intersects(world.blocks[i][j]))){
 			//		System.out.println("Collision DETECTED at"+p1.playerRect.x +":"+p1.playerRect.y);
 					p1.playerRect.x-=p1.getXDirection();
 					p1.playerRect.y-=p1.getYDirection();
 			//		System.out.println("Collision DETECTED at"+p1.playerRect.x +":"+p1.playerRect.y);
 					p1.setYDirection(0);
 					p1.setXDirection(0);
 					colide =true;
 				
 				}
 				if(world.exits[i][j] && (p1.playerRect.intersects(world.blocks[i][j]))){
 	
 									
 					p1.setHitExit(true);
 					c.ed.clear();
 					c.em.clear();
 					c.eWO.clear(); //loescht die Objekte aus den frheren Levels
 				}
 				if(world.checkpoints[i][j] && (p1.playerRect.intersects(world.blocks[i][j]))){
 					
 					
 					p1.setCheckpoint(currentRoom,i*25,j*25);					
 
 				}
 				
 //				if(world.checkpoint[i][j] && (p1.playerRect.intersects(world.blocks[i][j]))){
 //					p1.setCheckpoint(currentLevel,i,j);
 //				}
 
 				if(world.trap[i][j] && (p1.playerRect.intersects(world.blocks[i][j]))){
 					p1.changePlayerLifepoints(-12,250000000);
 				}
 				
 				if(world.finish[i][j] && (p1.playerRect.intersects(world.blocks[i][j]))){
 					p1.setHitFinish(true);
 				}
 
 				if(p1.playerRect.x<0) //Prevents player to move back from start Point
 					p1.playerRect.x=0;
 				
 			}
 			
 			}
 		if(Physics.CollisionGameObjectList(p1, ed) ){
 			p1.changePlayerLifepoints(-12,250000000);
 			System.out.println("Collision DETECTED PLAYER/MOB");
 			}
 	//	if(Physics.CollisionGameObjectList(p1, eMO)){
 	//		log("MAP OBJECT PLAYER COLLLISION");
 	//		}
 		
 		return colide;	
 	}
 	
 	//	playerRect.x-=1;
 	//	playerRect.y-=1;
 	/*	if (colide) {
 		System.out.println("Collision DETECTED at"+playerRect.x +":"+playerRect.y);
 		}
 		else {
 //		System.out.println(0);
 		}
 	*/	
 		
 		
 	
 	
 	
 	private void paintScreen(){
 		Graphics g;
 		try{
 			g =this.getGraphics();
 			if(dbImage != null && g != null){
 				g.drawImage(dbImage, 0, 0, null);
 			}
 		Toolkit.getDefaultToolkit().sync(); // For Linux
 		}catch (Exception e){
 			System.err.println(e);
 		}
 		
 		
 	}
 	
 	public void addNPC(double x, double y){
 		c.addEntity(new NPC(x, y, this, p1));
 	}
 	public void addHealthPack(double x, double y, int leben, int mana, int geld){
 		c.addEntity(new HealthPack(x, y, this, p1,leben, mana, geld));
 	}
 	public void addElement(double x, double y,Image image, double width, double height){
 		c.addEntity(new Element(x, y, this, image, width, height));
 	}
 	@Override
 	public void addNotify(){
 		super.addNotify();
 		startGame();
 	}
 	
 	//Start game method
 	
 	public synchronized void startGame(){  //Need to check performance if with synchronized is better than without?
 		if(game==null  || ! running )
 		{
 			game = new Thread(this);
 			game.start();
 			running = true;
 		}
 	}
 	
 	public synchronized void stopGame(){   //Private or should I make it public, to call it from other class?
 		if (running){
 			running = false;
 		}
 	}
 
 
 	private void log(String s){
 		System.out.println(s);
 	}
 
 	
 	public void addBOSS1(double x, double y){
 		c.addEntity(new BOSS1(x, y, this, p1));
 	}
 
 	public void addBOSS3(double x, double y){
 		c.addEntity(new BOSS3(x, y, this, p1));
 	}
 
 	public void addNPC2(double x, double y){
 		c.addEntity(new NPC2(x, y, this, p1));
 	}
 
 	public void addNPC3(double x, double y){
 		c.addEntity(new NPC3(x, y, this, p1));
 	}
 
 	public void addBOSS2(double x, double y){
 		c.addEntity(new BOSS2(x, y, this, p1));
 	}
 
 }
