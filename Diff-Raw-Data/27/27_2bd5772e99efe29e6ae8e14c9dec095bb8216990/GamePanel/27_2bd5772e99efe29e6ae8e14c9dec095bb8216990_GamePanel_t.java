 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.geom.Ellipse2D;
 import java.awt.geom.Line2D;
 import java.util.ListIterator;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 public class GamePanel extends JPanel  implements Runnable, KeyListener{
 	private static final long serialVersionUID = 1L;
 	JFrame frame;
 	JFrame frame2;
 	JFrame frame3;
 	JFrame frame4;
 	JFrame shop2 = new JFrame("Shop");
 	JFrame skills = new JFrame ("Skills");
 	
 	long delta = 0;
 	long last = 0;
 	long fps = 0;
 	long gameover = 0;
 	
 	SpriteLib lib;
 	SoundLib soundlib;
 	Player player;
 	Enemy enemy;
 	Enemy enemy2;
 
 	Enemy boss;
 	Enemy enemy3;
 	Enemy enemy4;
 	Enemy enemy5;
 	Enemy enemy6;
 	Enemy enemy7;
 
 	Item coin;
 	Item coin2;
 	Item coin3;
 	Item coin4;
 	Item coin5;
 	Item mana;
 	Item shop;
 	Item npc;
 	Item schwert;
 	Item schild;
 	Item healthpack;
 	Item healthpack2;
 
 	Quest quest;
 	
 	MapDisplay map;
 	
 	CopyOnWriteArrayList<Object> attacks;	//Liste fr die Attack-Objekte
 	CopyOnWriteArrayList<Sprite> actors; //beinhaltet alle Objekte im Spiel, also Gegner, Items etc.
 
 	boolean up;
 	boolean down;
 	boolean left;
 	boolean right;
 	boolean attack;	//Spieler will attackieren
 	boolean waitingForKeyPress;
 	boolean game_running = true;
 	boolean started = false;
 	boolean shopmode = false; //Ist Spieler im Shop oder nicht?
 	boolean skillmode = false;	//Ist der Spieler im Fertigkeitsbaum, oder nicht?
 	boolean enterShop = false;
 	boolean enterNPC = false;
 	boolean talkwithnpc = false;
 	boolean magic = false; //Will Spieler zaubern?
 	boolean inquest = false;
 
 	int spiel_status = 3; // 0 = Verloren, 1 = Gewonnen, 2 = Pause, 3 = noch nicht gestartet; Ersetzt boolean gamewon, lost
 	int pressCount;
 	int speed = 80;
 	int x = 0;
 	int y = 0;
 	int checkpointx;
 	int checkpointy;
 	int room; //zeigt an im wievielten Raum man sich im Level befindet, mglich sind 1, 2 oder 3
 	int level; //zeigt an im wievielten Lebel man ist, mglich sind 1, 2 oder 3
 	
 	boolean skillsOnce;	//Wurde das Skills-Men bereits initialisiert?
 	JButton t1;			//Buttons fr das Skill-Men
 	JButton t2;
 	JButton t3;
 	JButton t4;
 	JButton t5;
 	
 	static int rows, columns;
 	
 
 	public static void main(String[] args){
 		new GamePanel(790,630); //Sonst grauer Streifen an den Rndern rechts und unten
 	}
 	
 	public GamePanel(int w, int h){
 		
 		this.setPreferredSize(new Dimension(w,h));
 		this.setBackground(Color.darkGray);
 		frame = new JFrame("Dungeon MYS");
 		frame.setLocation(300,50);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.add(this);
 		frame.pack();
 		frame.setVisible(true);
 		frame.setResizable(false);
 		frame.addKeyListener(this);
 		
 		
 		paintMenu();
 				
 		Thread t = new Thread(this);
 		t.start(); //ruft run auf
 		
 	}
 
 	private void doInitializations(JFrame menu){
 		
 		
 		up = false;
 		down = false;
 		left = false;
 		right = false; //sonst luft Spieler nach Neustart einfach los
 		attack = false;
 		
 		level = 1;
 		room = 1;
 		last = System.nanoTime();
 		gameover = 0;
 		
 		actors = new CopyOnWriteArrayList<Sprite>();
 		attacks = new CopyOnWriteArrayList<Object>();
 		
 	
 		lib = SpriteLib.getInstance();
 		
 
		/*soundlib = new SoundLib();
 		soundlib.loadSound("Hintergrund", "sound/Greensleeves.wav");
 		soundlib.loadSound("Angriff", "sound/Angriff.wav");
 		soundlib.loadSound("Zauber", "sound/Zauber.wav");
		soundlib.loopSound("Hintergrund");*/
 		
 		////1 = Coins, 2 = Mana, 3 = Shop, 4 = Rstung, 5 = Waffe, 6 = NPC
 		player = new Player(lib.getSprite("resources/pics/player.gif", 8, 1), 50, 50, 100, this);
 		enemy = new Enemy(lib.getSprite("resources/pics/enemy.gif", 4, 1), 100, 500, 10, 100, this);
 		enemy2 = new Enemy(lib.getSprite("resources/pics/enemy.gif", 4, 1), 300, 200, 10, 100, this);
 		coin = new Item(lib.getSprite("resources/pics/coin.gif", 1, 1), 700, 400, 1, 100, this);
 		coin2 = new Item(lib.getSprite("resources/pics/coin.gif", 1, 1), 670, 400, 1, 100, this);
 		coin3 = new Item(lib.getSprite("resources/pics/coin.gif", 1, 1), 640, 400, 1, 100, this);
 		coin4 = new Item(lib.getSprite("resources/pics/coin.gif", 1, 1), 610, 400, 1, 100, this);
 		coin5 = new Item(lib.getSprite("resources/pics/coin.gif", 1, 1), 580, 400, 1, 100, this);
 		mana = new Item(lib.getSprite("resources/pics/mana.gif", 1, 1), 470, 500, 2, 100, this);
 		shop = new Item(lib.getSprite("resources/pics/shop.gif", 1, 1), 400, 500, 3, 100, this);
 		npc = new Item(lib.getSprite("resources/pics/npc.gif", 1, 1), 100, 100, 6, 100, this);
 		schild = new Item(lib.getSprite("resources/pics/armour.gif", 1, 1), 100, 200, 4, 100, this);
 		schwert = new Item(lib.getSprite("resources/pics/weapon.gif", 1, 1), 100, 150, 5, 100, this);
 		healthpack = new Item(lib.getSprite("resources/pics/healthpack.gif", 1, 1), 500, 100, 7, 100, this);
 		quest = new Quest(lib.getSprite("resources/pics/quest.gif", 1, 1), 500, 200, 2, 100, this);
 
 
 		actors.add(enemy); 
 		actors.add(enemy2); 
 		actors.add(coin); 
 		actors.add(coin2); 
 		actors.add(coin3); 
 		actors.add(coin4); 
 		actors.add(coin5);
 		actors.add(mana);
 		actors.add(shop);
 		actors.add(npc);
 		actors.add(schwert);
 		actors.add(schild);
 		actors.add(healthpack);
 		actors.add(quest);
 		actors.add(player);
 		
 		checkpointx = 50;
 		checkpointy = 50;
 
 		
 		player.setOldCoins(100);
 		player.setOldXP(0);
 		player.setOldMana(0);
 		player.setOldWeapon(false);
 		player.setOldArmour(false);
 		player.setCoins(100);
 		player.setXP(0);
 		player.setOldMaxhealth(100);
 		
 		player.setSkillHealth1(false);
 		player.setSkillHealth2(false);
 		player.setSkillStrength1(false);
 		player.setSkillStrength2(false);
 		
 		player.setOldSkillHealth1(false);
 		player.setOldSkillHealth2(false);
 		player.setOldSkillStrength1(false);
 		player.setOldSkillStrength2(false);
 		
 		skillsOnce = false;
 		
 		//Erstellen der Karte, wobei die ersten 3 Parameter fr die Eingabedateien stehen, die erste Zahl fr die Anzahl der Spalten im TileSet, die zweite fr die Anzahl der Zeilen
 		map = new MapDisplay("resources/level/TileMap_1_1.txt", "resources/pics/tiles_1.gif", "resources/pics/shadow.png", 5, 1, this);
 		
 		initSkills();
 		frame.setVisible(true);
 		frame.add(this);
 		menu.dispose();
 		setStarted(true);
 		//doInitializations(3,2); //Zum Level jumpen
 		
 	}
 	
 	/**
 	 * In der doInitializations-Klasse werden neue Level bzw. Rume geladen und entsprechende Sprites geladen. 
 	 * Auerdem werden teilweise Checkpointwerte gespeichert
 	 */
 	public void doInitializations(int l, int r){ //l = level, r = room
 		level = l;
 		room = r;
 		actors.clear(); //Actors-Liste wird gelscht und dann entsprechend der Information in TileMap_n.txt neu erstellt
 
 		if((level ==1) && (room == 1)){
 			map = new MapDisplay("resources/level/TileMap_1_1.txt", "resources/pics/tiles_1.gif", "resources/pics/shadow.png", 5, 1, this);
 			enemy = new Enemy(lib.getSprite("resources/pics/enemy.gif", 4, 1), 100, 500, 10, 100, this);
 			enemy2 = new Enemy(lib.getSprite("resources/pics/enemy.gif", 4, 1), 300, 200, 10, 100, this);
 			coin = new Item(lib.getSprite("resources/pics/coin.gif", 1, 1), 700, 400, 1, 100, this);
 			mana = new Item(lib.getSprite("resources/pics/mana.gif", 1, 1), 470, 500, 2, 100, this);
 			shop = new Item(lib.getSprite("resources/pics/shop.gif", 1, 1), 400, 500, 3, 100, this);
 			npc = new Item(lib.getSprite("resources/pics/npc.gif", 1, 1), 100, 100, 6, 100, this);
 			schild = new Item(lib.getSprite("resources/pics/armour.gif", 1, 1), 100, 200, 4, 100, this);
 			schwert = new Item(lib.getSprite("resources/pics/weapon.gif", 1, 1), 100, 150, 5, 100, this);
 			healthpack = new Item(lib.getSprite("resources/pics/healthpack.gif", 1, 1), 500, 100, 7, 100, this);
 			
 			actors.add(enemy); 
 			actors.add(enemy2); 
 			actors.add(coin); 
 			actors.add(mana);
 			actors.add(shop);
 			actors.add(npc);
 			actors.add(schwert);
 			actors.add(schild);
 			actors.add(healthpack);
 		}else if((level == 1) && (room == 2)){
 			
 			map = new MapDisplay("resources/level/TileMap_1_2.txt", "resources/pics/tiles_1.gif", "resources/pics/shadow.png", 5, 1, this); 
 			coin = new Item(lib.getSprite("resources/pics/coin.gif", 1, 1), 600, 400, 1, 100, this);
 			shop = new Item(lib.getSprite("resources/pics/shop.gif", 1, 1), 380, 480, 3, 100, this);
 			enemy = new Enemy(lib.getSprite("resources/pics/enemy.gif", 4, 1), 50, 450, 10, 100, this);
 			enemy2 = new Enemy(lib.getSprite("resources/pics/enemy.gif", 4, 1), 490, 100, 10, 100, this);
 			
 			actors.add(enemy); 
 			actors.add(enemy2); 
 			actors.add(coin); 
 			actors.add(shop);
 
 		}
 		else if((level == 1) && (room == 3)){
 			map = new MapDisplay("resources/level/TileMap_1_3.txt", "resources/pics/tiles_1.gif", "resources/pics/shadow.png", 5, 1, this);
 			boss = new Enemy(lib.getSprite("resources/pics/boss.gif", 4, 1), 80, 460, 20, 100, this);
 			boss.reduceHealth(-100); //health = 200 -(-100) = 300
 			actors.add(boss);
 		}
 		else if((level == 2) && (room == 1)){
 			map = new MapDisplay("resources/level/TileMap_2_1.txt", "resources/pics/tiles_2.gif", "resources/pics/shadow.png", 5, 1, this);
 			checkpointx = 90;
 			checkpointy = 400;
 			//Player-Werte sichern fr Checkpoint
 			player.setOldCoins(player.getCoins());
 			player.setOldMaxhealth(player.getMaxhealth());
 			player.setOldXP(player.getXP());
 			player.setOldMana(player.getMana());
 			player.setOldWeapon(player.hasWeapon());
 			player.setOldArmour(player.hasArmour());
 			
 			player.setOldSkillHealth1(player.hasSkillHealth1());
 			player.setOldSkillHealth2(player.hasSkillHealth2());
 			player.setOldSkillStrength1(player.hasSkillStrength1());
 			player.setOldSkillStrength2(player.hasSkillStrength2());
 			
 			
 		}
 		else if((level == 2) && (room == 2)){
 			map = new MapDisplay("resources/level/TileMap_2_2.txt", "resources/pics/tiles_2.gif", "resources/pics/shadow.png", 5, 1, this);
 			
 			coin2 = new Item(lib.getSprite("resources/pics/coin.gif", 1, 1), 50,50, 1, 100, this);
 			enemy3 = new Enemy(lib.getSprite("resources/pics/enemy.gif", 4, 1), 200, 200, 10, 100, this);
 			actors.add(coin2);
 			actors.add(enemy3);
 		}
 		else if((level == 2) && (room == 3)){
 			map = new MapDisplay("resources/level/TileMap_2_3.txt", "resources/pics/tiles_2.gif", "resources/pics/shadow.png", 5, 1, this);
 			boss = new Enemy(lib.getSprite("resources/pics/boss.gif", 4, 1), 120, 80, 20, 100, this);
 			actors.add(boss);
 		}
 		
 		else if((level == 3) && (room == 1)){
 			map = new MapDisplay("resources/level/TileMap_3_1.txt", "resources/pics/tiles_3.gif", "resources/pics/shadow.png", 5, 1, this);
 			
 			checkpointx = 80;
 			checkpointy = 40;
 			player.setOldCoins(player.getCoins());
 			player.setOldMaxhealth(player.getMaxhealth());
 			player.setOldXP(player.getXP());
 			player.setOldMana(player.getMana());
 			player.setOldWeapon(player.hasWeapon());
 			player.setOldArmour(player.hasArmour());
 			
 			player.setOldSkillHealth1(player.hasSkillHealth1());
 			player.setOldSkillHealth2(player.hasSkillHealth2());
 			player.setOldSkillStrength1(player.hasSkillStrength1());
 			player.setOldSkillStrength2(player.hasSkillStrength2());
 			
 			enemy4 = new Enemy(lib.getSprite("resources/pics/enemy.gif", 4, 1), 500, 50, 10, 100, this);
 			enemy5 = new Enemy(lib.getSprite("resources/pics/enemy.gif", 4, 1), 200, 50, 10, 100, this);
 			healthpack2 = new Item(lib.getSprite("resources/pics/healthpack.gif", 1, 1), 720, 150, 7, 100, this);
 			coin3 = new Item(lib.getSprite("resources/pics/coin.gif", 1, 1), 720, 190, 1, 100, this);
 
 			actors.add(enemy4);
 			actors.add(enemy5);
 			actors.add(healthpack2);
 			actors.add(coin3);
 		}
 		else if((level == 3) && (room == 2)){
 			map = new MapDisplay("resources/level/TileMap_3_2.txt", "resources/pics/tiles_3.gif", "resources/pics/shadow.png", 5, 1, this);
 			enemy6 = new Enemy(lib.getSprite("resources/pics/enemy.gif", 4, 1), 300, 300, 10, 100, this);
 			actors.add(enemy6);
 		}
 		else if((level == 3) && (room == 3)){
 			map = new MapDisplay("resources/level/TileMap_3_3.txt", "resources/pics/tiles_3.gif", "resources/pics/shadow.png", 5, 1, this);
 			enemy7 = new Enemy(lib.getSprite("resources/pics/boss.gif", 4, 1), 150, 150, 10, 100, this);
 			actors.add(enemy7);
 		}
 		 //Nur der Player bleibt bestehen
 		actors.add(player);
 		
 	}
 
 	
 	private void paintMenu(){ 
 		//Idee: Vllt. lieber Men auf unsichtbar setzen und immer wieder anzeigen, wenn bentigt? Vllt. auch praktisch fr Pause...Aber was mit entsprechenden Labels?
 		if(spiel_status == 3){ //Spiel noch gar nicht gestartet
 			frame3 = new JFrame("Spiel starten?");
 			frame3.setLocation(650,300);
 			frame3.setSize(100, 100);
 			JButton b1 = new JButton("Spiel starten");
 			b1.setMnemonic(KeyEvent.VK_ENTER);//Shortcut Enter
 			JButton b2 = new JButton("Beenden");
 			b2.setMnemonic(KeyEvent.VK_ESCAPE);//Shortcut Escape
 			
 			frame3.add(BorderLayout.NORTH, b1);
 			frame3.add(BorderLayout.SOUTH, b2);
 			frame3.pack();
 			frame3.setVisible(true);
 			
 			
 			b1.addActionListener(new ActionListener(){
 				public void actionPerformed(ActionEvent arg0){
 					
 					doInitializations(frame3);
 	
 				}
 			});
 			
 			
 			b2.addActionListener(new ActionListener(){
 				public void actionPerformed(ActionEvent arg1){ //bzgl. Schlieen
 					System.exit(0);
 				}
 				
 			});
 			
 			
 		}
 		
 		if(spiel_status == 1|| spiel_status == 0){ //Wenn Spiel gewonnen oder verloren
 			frame2 = new JFrame("Neustart?");
 			frame2.setLocation(500,300);
 			frame2.setSize(100, 100);
 			JLabel label;
 			if(spiel_status == 1){
 				label = new JLabel("Bravo, du hast gewonnen! Mchtest du noch einmal spielen?");
 			}else{
 				label = new JLabel("Schade, du hast verloren. Mchtest du es noch einmal versuchen?");
 			}
 			
 			frame2.add(BorderLayout.NORTH, label);
 			JButton b1 = new JButton("Ich mchte nocheinmal spielen");
 			b1.setMnemonic(KeyEvent.VK_ENTER);//Shortcut Enter
 			b1.addActionListener(new ActionListener(){
 				public void actionPerformed(ActionEvent arg0){ //bzgl. Starten
 
 					doInitializations(frame2);
 	
 				}
 			});
 			
 			
 			JButton b2 = new JButton("Es reicht mir...");
 			b2.setMnemonic(KeyEvent.VK_ESCAPE);//Shortcut Escape
 			b2.addActionListener(new ActionListener(){
 				public void actionPerformed(ActionEvent arg1){ //bzgl. Schlieen
 					System.exit(0);
 				}
 				
 			});
 			frame2.add(BorderLayout.CENTER, b1);
 			frame2.add(BorderLayout.SOUTH, b2);
 			frame2.pack();
 			frame2.setVisible(true);
 			spiel_status =0;
 		}
 		spiel_status = 0; // Daraus folgt, dass wenn man das Spiel per ESC-taste verlsst, man verliert.
 		
 	}
 
 	public void shop(){
 		up = false;
 		down = false;
 		left = false;
 		right = false;
 		shopmode = true;
 		
 		shop2.setLocation(500,300);
 		shop2.setSize(800, 600);
 		shop2.pack();
 		shop2.setVisible(true);
 		
 		JButton b1 = new JButton("1 Manatrank fr 10 Mnzen");
 		b1.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0){ //bzgl. Starten
 				if(player.getCoins() >= 10){
 					player.setMana(player.getMana()+1);
 					player.setCoins(player.getCoins()-10);
 				}else{
 					System.out.println("Du hast nicht genug Mnzen!");
 				}
 				shopmode = false;
 				enterShop = false;
 			}
 		});
 		
 		
 		JButton b2 = new JButton("1 Leben fr 50 Mnzen");
 
 		b2.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg1){ //bzgl. Schlieen
 				if(player.getCoins() >= 50){
 					player.setLifes(player.getLifes()+1);
 					player.setCoins(player.getCoins()-50);
 				}else{
 					System.out.println("Du hast nicht genug Mnzen!");
 				}
 				shopmode = false;
 				enterShop = false;
 			}
 			
 		});
 		
 		JButton b3 = new JButton("Ich will nix - danke");
 		b3.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg1){
 				shopmode = false;
 				enterShop = false;
 			}
 		});
 		
 		shop2.add(BorderLayout.NORTH, b1);
 		shop2.add(BorderLayout.CENTER, b2);
 		shop2.add(BorderLayout.SOUTH, b3);
 		shop2.pack();
 		shop2.setVisible(true);
 		
 	}
 	public void run(){
 		while(game_running){
 			
 			computeDelta(); //Zeit fr vorausgehenden Schleifendurchlauf wird errechnet
 			//Erst Methoden abarbeiten, wenn Spiel gestartet ist
 			if(isStarted()){
 				
 				checkKeys(); //Tastaturabfrage
 				doLogic(); //Ausfhrung der Logik
 				moveObjects(); //Bewegen von Objekten
 				
 			}
 
 			repaint();
 			
 			try{
 				Thread.sleep((1000000000 - (System.nanoTime() - last))/60000000); //Zum flssigen Spiellauf und stabiler FPS-Rate
 				
 			}catch (InterruptedException e){}
 			
 		}	
 	}
 	
 	/*Methode zur Initialisierung des Skill-Mens*/
 	public void initSkills(){
 		up = false;
 		down = false;
 		left = false;
 		right = false;
 		skillmode = true;
 		
 		skills.setLocation(500,300);
 		skills.setSize(800, 600);
 		//skills.pack();
 		//skills.setVisible(true);
 		
 		t1 = new JButton("Dicker Brecher (bentigt 2 Skillpunkte)");
 		t1.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0){ //bzgl. Starten
 				if(player.getXP() >= 2){
 					player.setXP(player.getXP()-2);
 					player.setMaxhealth(player.getMaxhealth() + 50);
 					player.setSkillHealth1(true);
 				}else{
 					System.out.println("Du hast nicht genug Erfahrung");
 				}
 				skillmode = false;
 			}
 		});
 		
 		t3 = new JButton("Kanten-Paule (bentigt 4 Skillpunkte und Dicker Brecher)");
 		t3.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0){ //bzgl. Starten
 				if(player.getXP() >= 4){
 					player.setXP(player.getXP()-4);
 					player.setMaxhealth(player.getMaxhealth() + 50);
 					player.setSkillHealth2(true);
 				}else{
 					System.out.println("Du hast nicht genug Erfahrung");
 				}
 				skillmode = false;
 			}
 		});
 		
 		
 		t2 = new JButton("Brutaler Prgelknabe (bentigt 2 Skillpunkte)");
 		t2.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg1){ //bzgl. Schlieen
 				if(player.getXP() >= 2){
 					player.setXP(player.getXP()-2);
 					player.setDamage(player.getDamage() + 20);
 					player.setSkillStrength1(true);
 				}else{
 					System.out.println("Du hast nicht genug Erfahrung!");
 				}
 				skillmode = false;
 			}
 				
 		});
 		
 		t4 = new JButton("Mega-Mrder brutaler Prgelknabe (bentigt 4 Skillpunkte und Brutaler Prgelknabe)");
 		t4.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg1){ //bzgl. Schlieen
 				if(player.getXP() >= 4){
 					player.setXP(player.getXP()-4);
 					player.setDamage(player.getDamage() + 30);
 					player.setSkillStrength2(true);
 				}else{
 					System.out.println("Du hast nicht genug Erfahrung!");
 				}
 				skillmode = false;
 			}
 			
 		});
 		
 		t5 = new JButton("Ich will nix - danke");
 		t5.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg1){
 				skillmode = false;
 			}
 		});
 		
 		skills.setLayout(new GridLayout(3,2));
 		skills.add(t1);
 		skills.add(t2);
 		skills.add(t3);
 		skills.add(t4);
 		skills.add(t5);
 	
 		if(player.hasSkillHealth1()){
 			t1.setEnabled(false);
 			t3.setEnabled(true);
 		}else{
 			t3.setEnabled(false);
 		}
 		if(player.hasSkillHealth2()){
 			t2.setEnabled(false);
 		}
 		if(player.hasSkillStrength1()){
 			t3.setEnabled(false);
 			t4.setEnabled(true);
 		}else{
 			t4.setEnabled(false);
 		}
 		if(player.hasSkillStrength2()){
 			t4.setEnabled(false);
 		}
 		
 		skills.pack();
 		//skills.setVisible(true);
 		
 	}
 	/*Methode zum Aufruf des Skill-Mens*/
 	public void skills(){
 		up = false;
 		down = false;
 		left = false;
 		right = false;
 		skillmode = true;
 		
 		if(player.hasSkillHealth1()){
 			t1.setEnabled(false);
 			t3.setEnabled(true);
 		}else{
 			t3.setEnabled(false);
 		}
 		if(player.hasSkillHealth2()){
 			t2.setEnabled(false);
 		}
 		if(player.hasSkillStrength1()){
 			t3.setEnabled(false);
 			t4.setEnabled(true);
 		}else{
 			t4.setEnabled(false);
 		}
 		if(player.hasSkillStrength2()){
 			t4.setEnabled(false);
 		}
 		skills.setVisible(true);
 		
 	}
 	
 	
 
 	
 	private void computeDelta(){
 		delta = System.nanoTime() - last; //Errechnung der Zeit fr Schleifendurchlauf in NS
 		last = System.nanoTime(); //Speichern der aktuellen Systemzeit
 		fps = ((long) 1e9)/delta; //Errechnen der Framerate
 	}
 	@Override
 	public void paintComponent(Graphics g){ //paintComponent-Methode berschreiben
 		super.paintComponent(g);
 		
 		
 		if(!isStarted()){
 			return; //es wird erst gezeichnet, wenn Spiel gestartet ist
 		}
 		
 		map.drawVisibleMap(g); //Erst Karte, dann Objekte! Karte wird nur noch einmal gezeichnet, nicht fr jeden Sprite in actors neu
 		
 		if(actors!=null){
 
 			for(ListIterator<Sprite> it = actors.listIterator(); it.hasNext();){
 				
 				Sprite draw = it.next();
 				draw.drawObjects(g);
 
 			}
 		}
 		g.setColor(Color.red);
 		g.drawString("FPS " + Long.toString(fps), 20, 10); //Zur berprfung des flssigen Spiellaufs
 		g.setColor(Color.white);
 		g.drawString("Du hast " + player.getLifes() + " Leben", 20, 610);
 		g.drawString(", " + player.getCoins() + " Mnze(n)", 115, 610);
 		g.drawString(" und " + player.getMana() + " Einheit(en) Mana", 200, 610);
 		g.drawString("Lebenspunkte: " + player.getHealth() + "/" + player.getMaxhealth(), 350, 610);
 		g.drawString("Story: ", 20, 620);
 		g.drawString("XP: " + player.getXP(), 20, 630);
 		if(talkwithnpc == true){
 			g.drawString("Es war einmal in einem weit entfertenten Schloss...blablabla!", 60, 620);
 		}
 		g.drawString("Quest: ", 320, 620);
 		if(inquest == true){
 			int diff = player.getQuest().getQuestCoins() - player.getCollectedCoins();
 			if(diff <= 0){
 				diff = 0;
 			}
 			g.drawString("Du musst noch " + diff + " Mnze(n) sammeln", 360, 620);
 			if(diff == 0){
 				g.drawString(" - Geh zurck zum Quest", 565, 620);
 			}
 		}
 		g.drawString("Schaden: " + player.getDamage(), 485, 610);
 		g.drawString("Enemy: " + enemy2.getHealth(), 560, 610);
 		g.drawString("Rstung: " + player.hasArmour(), 630, 610);
 		g.drawString("Waffe: " + player.hasWeapon(), 715, 610);
 		g.drawString("Level: " + level, 700, 620);
 		g.drawString("Raum: " + room, 750, 620);
 		
 	}
 	
 private void doLogic(){
 		
 		if(attack){	//Wenn der Spieler angreifen will
 			Object angriff;
 			Sprite opfer;
 			angriff = player.getAttackObject();
 			if(angriff != null){
 				actors.add(player.getAttackEffect());	//Effekt wird hinzugefgt zu Actors
				//soundlib.playSound("Angriff");
 				attacks.add(angriff);	
 				for (ListIterator<Object> it1 = attacks.listIterator(); it1.hasNext();){
 					angriff = it1.next();
 					if((angriff instanceof java.awt.geom.Ellipse2D.Double)){ //wenn Angriff Kreis
 						Ellipse2D.Double circle = (Ellipse2D.Double) angriff;
 						for (ListIterator<Sprite> it2 = actors.listIterator(); it2.hasNext();){
 							opfer = it2.next();
 							if(opfer instanceof Enemy){
 								if (circle.intersects(opfer.getX(), opfer.getY(), opfer.getWidth(), opfer.getHeight())){ //falls Kreis Enemy trifft
 									((Enemy)opfer).reduceHealth(player.getDamage()); 
 								}
 							}
 							
 						}
 					}else if (angriff instanceof java.awt.geom.Line2D.Double){ //wenn Angriff Linie
 						Line2D.Double line = (Line2D.Double) angriff;
 						for (ListIterator<Sprite> it2 = actors.listIterator(); it2.hasNext();){
 							opfer = it2.next();
 							if(opfer instanceof Enemy){
 								if (line.intersects(opfer.getX(), opfer.getY(), opfer.getWidth(), opfer.getHeight())){ //falls Linie Enemy trifft
 									((Enemy)opfer).reduceHealth(player.getDamage());
 								}
 							}
 							
 						}
 					}
 					
 				}
 			}
 			attacks.clear();
 					
 		}
 		if(magic){
 			Object magic;
 			Sprite opfer;
 			magic = player.getMagicObject();
 			if(magic != null){
 				actors.add(player.getMagicEffect());	//Effekt wird hinzugefgt zu Actors
				//soundlib.playSound("Zauber");
 				attacks.add(magic);	
 				for (ListIterator<Object> it1 = attacks.listIterator(); it1.hasNext();){
 					magic = it1.next();
 					if((magic instanceof java.awt.geom.Ellipse2D.Double)){ //wenn Angriff Kreis
 						Ellipse2D.Double circle = (Ellipse2D.Double) magic;
 						for (ListIterator<Sprite> it2 = actors.listIterator(); it2.hasNext();){
 							opfer = it2.next();
 							if(opfer instanceof Enemy){
 								if (circle.intersects(opfer.getX(), opfer.getY(), opfer.getWidth(), opfer.getHeight())){ //falls Kreis Enemy trifft
 									((Enemy)opfer).stop();
 								}
 							}
 							
 						}
 					}else if (magic instanceof java.awt.geom.Line2D.Double){ //wenn Angriff Linie
 						Line2D.Double line = (Line2D.Double) magic;
 						for (ListIterator<Sprite> it2 = actors.listIterator(); it2.hasNext();){
 							opfer = it2.next();
 							if(opfer instanceof Enemy){
 								if (line.intersects(opfer.getX(), opfer.getY(), opfer.getWidth(), opfer.getHeight())){ //falls Linie Enemy trifft
 									((Enemy)opfer).stop();
 								}
 							}
 							
 						}
 					}
 					
 				}
 			}
 			
 			attacks.clear();
 		}
 		
 		//Neuerdings mit Iterator, der ist nmlich sicher vor Concurent-Modification-Exception (ist ja ne CopyOnWriteArrayList)
 		for (ListIterator<Sprite> it = actors.listIterator(); it.hasNext();){
 			Sprite r = it.next();
 			r.doLogic(delta);
 		
 			if(r.remove){
 				actors.remove(r);//Lschen von Sprite, remove == true; Wichtig fr z.B. damit eine Mnze nach dem Einsammeln nicht mehr angezeigt wird
 			}
 		}
 		
 		
 		if(gameover == 1){
 			if(System.currentTimeMillis() - gameover > 3000){
 				stopGame();
 			}
 		}
 		
 		for (int n = 0; n < actors.size(); n++){ //Es werden alle weiteren Sprites zur berprfung durchlaufen
 				
 				Sprite s2 = actors.get(n);
 				
 				player.collidedWith(s2); //berprfung ob Spieler kollidiert ist
 			
 		}
 		
 		if (shopmode == false){
 			shop2.setVisible(false);
 		}
 		if (skillmode == false){
 			skills.setVisible(false);
 		}
 	}
 	
 	private void moveObjects(){
 
 		//Neuerdings mit Iterator, der ist nmlich sicher vor Concurent-Modification-Exception (ist ja ne CopyOnWriteArrayList)
 		for (ListIterator<Sprite> it = actors.listIterator(); it.hasNext();){
 			Sprite r = it.next();
 			r.move(delta);
 		}
 	}
 	private void stopGame(){
 		setStarted(false);
 		gameover = 1;
 	}
 	
 	public void wonGame(){
 		System.out.println("Bravo, du hast gewonnen! Mchtest du noch einmal spielen?");
 		stopGame();
 		started = false;
 		spiel_status = 1;
 		paintMenu();
 	}
 	
 	public void lostGame(){
 		System.out.println("Schade, du hast verloren. Mchtest du es noch einmal versuchen?");
 		stopGame();
 		started = false;
 		spiel_status = 0;
 		paintMenu();
 	}
 
 	
     public boolean isStarted(){
     	return started;
     }
     
     public void setStarted(boolean started){
     	this.started = started;
     }
     
 	public void setLevel(int level){
 		this.level = level;
 	}
 	
 	public int getLevel(){
 		return level;
 	}
 	
 	public MapDisplay getMap(){
 		return map; //gibt die Karte zurck
 	}
 	
 	public int getRoom(){
 		return room;
 	}
 	
 	
 	private void checkKeys(){
 		
 		if(left){
 			player.setHorizontalSpeed(-speed);
 		}
 		if(right){
 			player.setHorizontalSpeed(speed);
 		}
 		if(down){
 			player.setVerticalSpeed(speed);
 		}
 		if(up){
 			player.setVerticalSpeed(-speed);
 		}
 		if(!up&&!down){ 						//wenn weder up noch down gedrckt
 			player.setVerticalSpeed(0);
 		}
 		if(!left&&!right){ 						//wenn weder left noch rechts gedrckt
 			player.setHorizontalSpeed(0);
 		}
 		if(attack){
 			player.setAttacking();
 		}
 		if(!attack){
 			player.resetAttacking();
 		}
 		
 	}
 	
 	
 	//Tastaturabfragen zur Steuerung
 	public void keyPressed(KeyEvent e){
 		
 		if (e.getKeyCode() == KeyEvent.VK_LEFT){ //linke Pfeiltaste
 			left = true;
 		}
 		if (e.getKeyCode() == KeyEvent.VK_RIGHT){ //rechte Pfeiltaste
 			right = true;
 		}
 		if (e.getKeyCode() == KeyEvent.VK_UP){ //obere Pfeiltaste
 			up = true;
 		}
 		if (e.getKeyCode() == KeyEvent.VK_DOWN){//untere Pfeiltaste
 			down = true;
 		}
 		if (e.getKeyCode() == KeyEvent.VK_ENTER){
 			enterShop = true;
 			enterNPC = true;
 		}
 		if(e.getKeyCode() == KeyEvent.VK_S){
 			skillmode = true;
 			skills();
 		}
 		if(e.getKeyCode() == KeyEvent.VK_X){
 			attack = true;
 		}
 		if (e.getKeyCode() == KeyEvent.VK_C){
 			magic = true;
 		}
 	}
 	//Taste wieder losgelassen?
 	public void keyReleased(KeyEvent e){
 				
 		if (e.getKeyCode() == KeyEvent.VK_LEFT){//linke Pfeiltaste
 			left = false;
 		}
 		if (e.getKeyCode() == KeyEvent.VK_RIGHT){//rechte Pfeiltaste
 			right = false;
 		}
 		if (e.getKeyCode() == KeyEvent.VK_UP){//obere Pfeiltaste
 			up = false;
 		}
 		if (e.getKeyCode() == KeyEvent.VK_DOWN){//untere Pfeiltaste
 			down = false;
 		}
 		if(e.getKeyCode() == KeyEvent.VK_X){
 			attack = false;
 		}
 		if (e.getKeyCode() == KeyEvent.VK_C){
 			magic = false;
 		}
 		if(e.getKeyCode() == KeyEvent.VK_ENTER){
 			enterNPC = false;
 			enterShop = false;
 		}
 		
 		if (e.getKeyCode() == KeyEvent.VK_ESCAPE){//Escape zum B
 			if(isStarted()){
 				stopGame(); 
 				paintMenu(); 
 			}else {
 				setStarted(false);
 				System.exit(0);
 			}
 		}
 	}
 
 	@Override
 	public void keyTyped(KeyEvent arg0) {
 		
 	}
 
 }
