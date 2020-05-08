 
 package com.pieno.jeu;
 
 import java.awt.Color;
 import java.awt.DisplayMode;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.GridLayout;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.Window;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.io.IOException;
 import java.math.BigInteger;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.security.MessageDigest;
 import java.text.DecimalFormat;
 import java.util.HashMap;
 import java.util.Vector;
 
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.JTextField;
 
 import com.esotericsoftware.kryonet.Client;
 import com.esotericsoftware.kryonet.Connection;
 import com.esotericsoftware.kryonet.Listener;
 import com.esotericsoftware.kryonet.Listener.ThreadedListener;
 import com.esotericsoftware.minlog.Log;
 import com.pieno.jeu.Server2D.PassiveSkill;
 import com.pieno.jeu.Server2D.Skill;
 import com.pieno.jeu.Network.*;
 
 public class Client2D {
 	UI ui;
 	Client client;
 	static String name, pass;
 	static int Y, X, previousI = -1, previousJ = -1, previousItemSlot, classe = -1;
 	static boolean dragging = false, chooseClass = false, authenticated = false;
 	static MenuBar menu;
 	static StatMenu statMenu;
 	static SkillMenu skillMenu;
 	static SaveMenu mainMenu;
 	static Point location;
 	DrawThread draw;
 	Screen S;
 	static ClassMenu classMenu;
 	static Image[] itemIcons = new Image[8], equipIcons = new Image[8];
 	static Character currentChar;
 	static Tooltip tooltip, equippedTooltip;
 	static Map currentMap;
 	static Image spotImage;
 	static Animation[] charAnims = new Animation[17];
 	static HashMap<Integer, ProjectileAnimation> projectiles = new HashMap<Integer, ProjectileAnimation>();
 	static HashMap<Integer, EffectAnimation> effects = new HashMap<Integer, EffectAnimation>();
 	static Vector<FlyingText> flyingText = new Vector<FlyingText>();
 	static HashMap<Integer, DropAnimation> drops = new HashMap<Integer, DropAnimation>();
 	boolean playing = true;
 	static boolean inGame = false;
 	DisplayMode mode;
 	EventHandler listener;
 	int[] keys = new int[5];
 	int[] skillKeys = new int[3];
 	boolean[] moveKeys = new boolean[5];
 	int currentSkillKey = -1;
 	
 	public static final int NONE = 0, SKILL = 1, STAT = 2, INV = 3;
 	
 	protected static final DisplayMode[] modes = {
 		new DisplayMode(1280,960,32,60),
 		new DisplayMode(1280,960,24,60),
 		new DisplayMode(1280,960,16,60),
 		new DisplayMode(1280,768,32,60),
 		new DisplayMode(1280,768,24,60),
 		new DisplayMode(1280,768,16,60),
 	};
 
 	public Client2D () {
 		client = new Client();
 		client.start();
 
 		// For consistency, the classes to be sent over the network are
 		// registered by the same method for both the client and server.
 		Network.register(client);
 
 		// ThreadedListener runs the listener methods on a different thread.
 		client.addListener(new ThreadedListener(new Listener() {
 			public void connected (Connection connection) {
 			}
 
 			public void received (Connection connection, Object object) {
 				if (object instanceof RegistrationRequired) {
 					chooseClass = true;
 					return;
 				}
 				
 				if(object instanceof AuthResponse){
 					if(((AuthResponse)object).accepted){
 						authenticated = true;
 					} else {
 						JOptionPane.showMessageDialog(null, "Wrong password");
 						ui.inputName();
 						Authentication auth = new Authentication();
 						auth.name = name;
 						auth.pass = pass;
 						client.sendTCP(auth);
 					}
 					return;
 				}
 				
 				if(object instanceof Stats){
 					Stats msg = (Stats) object;
 					ui.setStats(msg);
 					return;
 				}
 				
 				if(object instanceof AddExp){
 					AddExp bob = (AddExp) object;
 					currentChar.exp = bob.exp;
 					currentChar.lvl = bob.lvl;
 					return;
 				}
 				
 				if(object instanceof Skills){
 					Skills msg = (Skills) object;
 					ui.setSkills(msg);
 					return;
 				}
 				
 				if (object instanceof UpdateSkill) {
 					UpdateSkill msg = (UpdateSkill) object;
 					ui.updateSkill(msg);
 					return;
 				}
 				
 				if (object instanceof CharStats) {
 					if(currentChar!=null){
 						CharStats stats = (CharStats) object;
 						currentChar.atts = stats.atts;
 						currentChar.MF = stats.itemf;
 						currentChar.RF = stats.rarec;
 						currentChar.defense = stats.def;
 						currentChar.critChance = stats.crit;
 						currentChar.critDamage = stats.critd;
 						currentChar.mastery = stats.mastery;
 						currentChar.wATK = stats.watk;
 					}
 					return;
 				}
 				
 				if (object instanceof Items) {
 					currentChar.inventory.items = ((Items)object).items;
 					listener.generateTooltips();
 					return;
 				}
 				
 				if (object instanceof ChangedMap){
 					ChangedMap cm = (ChangedMap) object;
 					ui.characters.get(cm.id).map = cm.map;
 					if(cm.id==currentChar.id) currentChar.map = cm.map;
 					return;
 				}
 				
 				if(object instanceof Equips) {
 					currentChar.inventory.equip = ((Equips)object).equips;
 					listener.generateTooltips();
 					return;
 				}
 
 				if (object instanceof AddCharacter) {
 					AddCharacter msg = (AddCharacter)object;
 					ui.addCharacter(msg.character);
 					return;
 				}
 				
 				if (object instanceof SavesInfo) {
 					SavesInfo saves = (SavesInfo) object;
 					for(int i = 0; i < 4; i++){
 						mainMenu.saveButtons[i].setClasse(saves.saves[i].classe);
 						mainMenu.saveButtons[i].lvl = saves.saves[i].lvl;
 					}
 					return;
 				}
 
 				if (object instanceof UpdateCharacter) {
 					ui.updateCharacter((UpdateCharacter)object);
 					return;
 				}
 
 				if (object instanceof RemoveCharacter) {
 					RemoveCharacter msg = (RemoveCharacter)object;
 					ui.removeCharacter(msg.id);
 					return;
 				}
 				
 				if(object instanceof UpdateMonster) {
 					UpdateMonster msg = (UpdateMonster)object;
 					ui.updateMonster(msg);
 					return;
 				}
 				
 				if(object instanceof AddProjectile) {
 					AddProjectile add = (AddProjectile) object;
 					ProjectileAnimation proj = new ProjectileAnimation(add.type);
 					proj.x = add.x;
 					proj.y = add.y;
 					proj.vx = add.vx;
 					proj.vy = add.vy;
 					projectiles.put(add.id, proj);
 					return;
 				}
 				
 				if(object instanceof RemoveProjectile) {
 					projectiles.remove(((RemoveProjectile)object).id);
 					return;
 				}
 				
 				if(object instanceof AddDrop) {
 					AddDrop add = (AddDrop) object;
 					DropAnimation drop = new DropAnimation();
 					drop.type = add.type;
 					drop.rarity = add.rarity;
 					drop.x = add.x;
 					drop.y = add.y;
 					drops.put(add.id, drop);
 					return;
 				}
 				
 				if(object instanceof RemoveDrop) {
 					drops.remove(((RemoveDrop) object).id);
 					return;
 				}
 				
 				if(object instanceof AddEffect) {
 					ui.addEffect((AddEffect) object);
 					return;
 				}
 				
 				if(object instanceof DamageText) {
 					DamageText msg = (DamageText) object;
 					ui.damageText(msg);
 					return;
 				}
 			}
 
 			public void disconnected (Connection connection) {
 				playing = false;
 			}
 		}));
 
 		ui = new UI();
 		String host = "";
 		try{
 			host = client.discoverHost(Network.port+1, 1000).getHostAddress();
 		} catch (Exception e){
 			try {
 				new Server2D();
 				host = client.discoverHost(Network.port+1, 1500).getHostAddress();
 			} catch (Exception e1) {
 				try{
 					host = InetAddress.getLocalHost().getHostAddress();
 					host = ui.inputHost(host);
 				} catch (Exception e2){
 					host = ui.inputHost(host);
 				}
 			}
 		}
 		
 		try {
 			client.connect(5000, host, Network.port, Network.port+1);
 			// Server communication after connection can go here, or in Listener#connected().
 		} catch (IOException ex) {
 			JOptionPane.showMessageDialog(null, "Error: "+ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
 			ex.printStackTrace();
 			System.exit(0);
 		}
 
 		ui.inputName();
 		Authentication auth = new Authentication();
 		auth.name = name;
 		auth.pass = pass;
 		client.sendTCP(auth);
 		int tries = 0;
 		while(!authenticated){try{
 			Thread.sleep(50);
 			tries++;
 			if(tries > 200) {
 				JOptionPane.showMessageDialog(null, "Authentication error", "Error", JOptionPane.ERROR_MESSAGE);
 				System.exit(0);
 			}
 			}catch(Exception e){}}
 		
 		mainMenu = new SaveMenu();
 		classMenu = new ClassMenu();
 		
 		RequestSaves request = new RequestSaves();
 		request.name = name;
 		client.sendTCP(request);
 		
 		init();
 		
 		draw = new DrawThread();
 		draw.run();
 		
 		S.restoreScreen();
 		client.close();
 	}
 	
 	void sendKeys(){
 		PressedKeys msg = new PressedKeys();
 		msg.moveKeys = moveKeys;
 		msg.skill = currentSkillKey;
 		if (msg != null) client.sendTCP(msg);
 	}
 	
 	int getClasse(){
 		while(classe == -1){try {Thread.sleep(50);} catch (InterruptedException e) {}}
 		return classe;
 	}
 	
 	void init(){
 		S = new Screen();
 		mode = S.findGoodMode(modes);
 		S.setFullScreen(mode);
 		
 		listener = new EventHandler();
 		
 		Window w = S.getFSWindow();
 		w.setFont(new Font("Arial", Font.PLAIN, 24));
 		w.setBackground(Color.GRAY);
 		w.setForeground(Color.BLACK);
 		w.addKeyListener(listener);
 		w.addMouseListener(listener);
 		w.addMouseMotionListener(listener);
 		w.addFocusListener(listener);
 		w.setFocusTraversalKeysEnabled(false);
 		playing = true;
 		
 		initkeys();
 		loadpics();
 		menu = new MenuBar();
 		statMenu = new StatMenu();
 		skillMenu = new SkillMenu();
 		
 	}
 	
 	
 	void loadpics(){
 		equipIcons[Item.AMULET] = newImage("/item/equipAmu.png");
 		equipIcons[Item.BOOTS] = newImage("/item/equipBoot.png");
 		equipIcons[Item.GLOVES] = newImage("/item/equipGlove.png");
 		equipIcons[Item.HELM] = newImage("/item/equipHelm.png");
 		equipIcons[Item.PANTS] = newImage("/item/equipLeg.png");
 		equipIcons[Item.RING] = newImage("/item/equipRing.png");
 		equipIcons[Item.TORSO] = newImage("/item/equipTorso.png");
 		equipIcons[Item.WEAPON] = newImage("/item/equipWep.png");
 		
 		itemIcons[Item.AMULET] = newImage("/item/amulet.png");
 		itemIcons[Item.BOOTS] = newImage("/item/boots.png");
 		itemIcons[Item.GLOVES] = newImage("/item/gloves.png");
 		itemIcons[Item.HELM] = newImage("/item/helm.png");
 		itemIcons[Item.PANTS] = newImage("/item/pants.png");
 		itemIcons[Item.RING] = newImage("/item/ring.png");
 		itemIcons[Item.TORSO] = newImage("/item/torso.png");
 		itemIcons[Item.WEAPON] = newImage("/item/weapon.png");
 		
 		spotImage = newImage("/spot.png");
 		
 		Image standingR = newImage("/character/walkright1.png"), wizStandingR = newImage("/character/bobwalkwizzardR1.png"), jumpingR = newImage("/character/walkright2.png"),
 				wizJumpingR = newImage("/character/bobwalkwizzardR2.png"), standingL = newImage("/character/walkleft1.png"), wizStandingL = newImage("/character/bobwalkwizzardL1.png"),
 				jumpingL = newImage("/character/walkleft2.png"), wizJumpingL = newImage("/character/bobwalkwizzardL2.png"), standingladder = newImage("/character/bobclimb1.png");
 
 		charAnims[Network.walkR] = new Animation();
 		charAnims[Network.walkL] = new Animation();
 		charAnims[Network.climb] = new Animation();
 		charAnims[Network.wizwalkR] = new Animation();
 		charAnims[Network.wizwalkL] = new Animation();
 		charAnims[Network.wizclimb] = new Animation();
 			
 		charAnims[Network.climbSide] = new Animation();
 		charAnims[Network.climbSide].addScene(newImage("/character/bobclimbL1.png"), 160);
 		charAnims[Network.climbSide].addScene(newImage("/character/bobclimbL2.png"), 160);
 		charAnims[Network.onladder] = new Animation();
 		charAnims[Network.onladder].addScene(standingladder, 200);
 
 		charAnims[Network.standL] = new Animation();
 		charAnims[Network.standL].addScene(standingL, 200);
 		charAnims[Network.standR] = new Animation();
 		charAnims[Network.standR].addScene(standingR, 200);
 		charAnims[Network.jumpR] = new Animation();
 		charAnims[Network.jumpR].addScene(jumpingR, 200);
 		charAnims[Network.jumpL] = new Animation();
 		charAnims[Network.jumpL].addScene(jumpingL, 200);
 		for (int i = 1; i <= 3; i++)
 		{
 			charAnims[Network.climb].addScene(newImage("/character/bobclimb" + i + ".png"),120);
 			charAnims[Network.walkR].addScene(newImage("/character/walkright" + i + ".png"),150);
 			charAnims[Network.walkL].addScene(newImage("/character/walkleft" + i + ".png"),150);
 			charAnims[Network.wizclimb].addScene(newImage("/character/bobclimb" + i + ".png"),120);
 			charAnims[Network.wizwalkR].addScene(newImage("/character/bobwalkwizzardR" + i + ".png"), 150);
 			charAnims[Network.wizwalkL].addScene(newImage("/character/bobwalkwizzardL"+i+ ".png"), 150);
 			if (i == 2)
 			{
 				charAnims[Network.climb].addScene(
 						newImage("/character/bobclimb" + 1 + ".png"), 120);
 				charAnims[Network.walkR].addScene(
 						newImage("/character/walkright" + 1 + ".png"), 150);
 				charAnims[Network.walkL].addScene(
 						newImage("/character/walkleft" + 1 + ".png"), 150);
 				charAnims[Network.wizclimb].addScene(
 						newImage("/character/bobclimb" + 1 + ".png"), 120);
 				charAnims[Network.wizwalkR].addScene(
 						newImage("/character/bobwalkwizzardR"+ 1 + ".png"), 150);
 				charAnims[Network.wizwalkL].addScene(
 						newImage("/character/bobwalkwizzardL"+ 1 + ".png"), 150);
 					
 			}
 		}
 		charAnims[Network.wizstandL] = new Animation();
 		charAnims[Network.wizstandL].addScene(wizStandingL, 200);
 		charAnims[Network.wizstandR] = new Animation();
 		charAnims[Network.wizstandR].addScene(wizStandingR, 200);
 		charAnims[Network.wizjumpR] = new Animation();
 		charAnims[Network.wizjumpR].addScene(wizJumpingR, 200);
 		charAnims[Network.wizjumpL] = new Animation();
 		charAnims[Network.wizjumpL].addScene(wizJumpingL, 200);
 	
 	}
 	
 	public Image newImage(String source) {
 		return new ImageIcon(getClass().getResource(source)).getImage();
 	}
 
 	void initkeys(){
 		keys[Network.UP] = KeyEvent.VK_UP;
 		keys[Network.DOWN] = KeyEvent.VK_DOWN;
 		keys[Network.LEFT] = KeyEvent.VK_LEFT;
 		keys[Network.RIGHT] = KeyEvent.VK_RIGHT;
 		keys[Network.JUMP] = KeyEvent.VK_ALT;
 		skillKeys[0] = KeyEvent.VK_CONTROL;
 		skillKeys[2] = KeyEvent.VK_SHIFT;
 		skillKeys[1] = KeyEvent.VK_Z;
 	}
 	
 	void backToMenu(){
 		chooseClass = false;
 		inGame = false;
 		client.sendTCP(new RemoveCharacter());
 		try {Thread.sleep(50);} catch (InterruptedException e) {}
 		currentMap = null;
 		currentChar = null;
 		projectiles = new HashMap<Integer, ProjectileAnimation>();
 		effects = new HashMap<Integer, EffectAnimation>();
 		drops = new HashMap<Integer, DropAnimation>();
 		flyingText = new Vector<FlyingText>();
 		ui.monsters = new HashMap<Integer, MonsterAnimation>();
 		RequestSaves request = new RequestSaves();
 		request.name = name;
 		client.sendTCP(request);
 	}
 	
 	void stop(){
 		playing = false;
 	}
 
 	static class UI {
 		HashMap<Integer, CharacterAnimation> characters = new HashMap<Integer, CharacterAnimation>();
 		HashMap<Integer, MonsterAnimation> monsters = new HashMap<Integer, MonsterAnimation>();
 
 		public String inputHost (String ip) {
 			
 			String input = (String)JOptionPane.showInputDialog(null, "Host:", "Connect to server", JOptionPane.QUESTION_MESSAGE,
 				null, null, ip);
 			if (input == null || input.trim().length() == 0) System.exit(1);
 			return input.trim();
 		}
 
 		public void inputName () {
 			name = "";
 			
 			JPanel userPanel = new JPanel();
 			userPanel.setLayout(new GridLayout(2,2));
       
 			JLabel usernameLbl = new JLabel("Username:");
 			JLabel passwordLbl = new JLabel("Password:");
 
 			JTextField username = new JTextField();
 			JPasswordField passwordFld = new JPasswordField();
        
 			userPanel.add(usernameLbl);
 			userPanel.add(username);
 			userPanel.add(passwordLbl);
 			userPanel.add(passwordFld);
 
 			try{
 			username.setText(InetAddress.getLocalHost().getHostName());
 			} catch (Exception e){}
 			
 			int input = JOptionPane.showConfirmDialog(null, userPanel, "Enter your password:", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
 			name = new String(username.getText());
 			name = name.trim();
 			pass = new String(passwordFld.getPassword());
 			pass = pass.trim();
 			if(name.length() == 0 || pass.length() == 0 || input != JOptionPane.OK_OPTION){
 				System.exit(0);
 			}
 			MessageDigest md;
 			try {
 				md = MessageDigest.getInstance("MD5");
 				md.update(pass.getBytes("UTF-8"));
 				byte[] digest = md.digest();
 				BigInteger bigInt = new BigInteger(1,digest);
 				pass = bigInt.toString(16);
 			} catch (Exception e) {}
 		}
 		
 		public void updateSkill(UpdateSkill update){
 			CharacterAnimation character = characters.get(update.id);
 			character.setSkill(update.skill, update.facingLeft);
 		}
 		
 		public void setStats(Stats stats){
 			currentChar.baseAtts = stats.atts;
 			currentChar.attStats = stats.attStats;
 		}
 		
 		public void setSkills(Skills skills){
 			currentChar.skillLvls = skills.skillLvls;
 			currentChar.passiveLvls = skills.passiveLvls;
 			currentChar.skillStats = skills.skillStats;
 		}
 		
 		public void damageText(DamageText dmgtxt){
 			boolean created = false;
 			for(int i  = 0; i < flyingText.size() && !created; i++){
 				if(!flyingText.get(i).isActive()) {
 					flyingText.set(i, new FlyingText(dmgtxt.dmg, new Point(dmgtxt.x, dmgtxt.y), dmgtxt.crit));
 					created = true;
 				}
 			}
 			if(!created) flyingText.add(new FlyingText(dmgtxt.dmg, new Point(dmgtxt.x, dmgtxt.y), dmgtxt.crit));
 			
 		}
 		
 		public void addEffect (AddEffect add) {
 			EffectAnimation effect;
 			if(add.cid == -1)
 			effect = new EffectAnimation(add.type);
 			else effect = new EffectAnimation(add.type, characters.get(add.cid));
 			effect.x = add.x;
 			effect.y = add.y;
 			effects.put(add.id, effect);
 		}
 
 		public void addCharacter (Character character) {
 			CharacterAnimation ca = new CharacterAnimation();
 			ca.setClasse(character.classe);
 			ca.x = character.getX();
 			ca.y = character.getY();
 			ca.name = character.name;
 			ca.map = character.map;
 			if(name.equals(character.name)) {
 				inGame = true;
 				currentChar = new Character();
 				currentChar.inventory = new Inventory();
 				currentChar.classe = character.classe;
 				currentChar.inventory.generateSlots();
 				currentChar.x = ca.x;
 				currentChar.y = ca.y;
 				currentChar.id = character.id;
 				currentChar.exp = character.exp;
 				currentChar.lvl = character.lvl;
 				currentChar.name = ca.name;
 				currentChar.map = ca.map;
 				currentChar.inventory = character.inventory;
 				currentMap = new Map(currentChar.map);
 			}
 			characters.put(character.id, ca);
 		}
 		
 		public void updateMonster(UpdateMonster mon){
 			if(monsters == null) monsters = new HashMap<Integer, MonsterAnimation>();
 			if(!monsters.containsKey(mon.id) || monsters.get(mon.id).type != mon.type){
 				MonsterAnimation mona = new MonsterAnimation(mon.type);
 				if(mon.id >= 100) mona.summoned = true;
 				monsters.put(mon.id, mona);
 			}
 			monsters.get(mon.id).lastUpdate = 0;
 			monsters.get(mon.id).x = mon.x;
 			monsters.get(mon.id).y = mon.y;
 			monsters.get(mon.id).canMove = mon.canMove;
 			monsters.get(mon.id).facingLeft = mon.facingLeft;
 			monsters.get(mon.id).lifePercentage = mon.lifePercentage;
 			monsters.get(mon.id).alive = mon.alive;
 			monsters.get(mon.id).lvl = mon.lvl;
 			monsters.get(mon.id).eliteType = mon.eliteType;
 			
 		}
 
 		public void updateCharacter (UpdateCharacter msg) {
 			CharacterAnimation character = characters.get(msg.id);
 			if (character == null) return;
 			character.x = msg.x;
 			character.y = msg.y;
 			if(currentChar!=null)
 			character.map = currentChar.map;
 			character.life = msg.life;
 			if(character.life > 0)
 				character.alive = true;
 			else character.alive = false;
 			character.maxLife = msg.maxLife;
 			character.mana = msg.mana;
 			character.maxMana = msg.maxMana;
 			character.lastUpdate = 0;
 			character.invincible = msg.invincible;
 			
			if(!msg.usingSkill && character.skillFinished)
 				character.setSkill(-1, true);
 			
 			if(character.currentAnim != msg.currentAnim){
 				switch(character.classe)
 				{
 				default: character.setAnimation(charAnims[msg.currentAnim], msg.currentAnim); break;
 				case Character.MAGE: switch(msg.currentAnim){
 					default:  character.setAnimation(charAnims[msg.currentAnim], msg.currentAnim); break;
 					case Network.walkL : character.setAnimation(charAnims[Network.wizwalkL], msg.currentAnim); break;
 					case Network.walkR :character.setAnimation(charAnims[Network.wizwalkR], msg.currentAnim); break;
 					case Network.standL : character.setAnimation(charAnims[Network.wizstandL], msg.currentAnim); break;
 					case Network.standR :character.setAnimation(charAnims[Network.wizstandR], msg.currentAnim); break;
 					case Network.jumpL : character.setAnimation(charAnims[Network.wizjumpL], msg.currentAnim); break;
 					case Network.jumpR :character.setAnimation(charAnims[Network.wizjumpR], msg.currentAnim); break;
 				} break;
 				}
 			}
 			if(currentChar != null && msg.id == currentChar.id) {
 				currentChar.x = msg.x;
 				currentChar.y = msg.y;
 				currentChar.life = msg.life;
 				currentChar.maxLife = msg.maxLife;
 				currentChar.mana = msg.mana;
 				currentChar.maxMana = msg.maxMana;
 				if(currentMap == null || !character.map.equals(currentMap.map)) {
 					currentMap = new Map(character.map);
 					projectiles = new HashMap<Integer, ProjectileAnimation>();
 					effects = new HashMap<Integer, EffectAnimation>();
 					drops = new HashMap<Integer, DropAnimation>();
 					flyingText = new Vector<FlyingText>();
 					monsters = new HashMap<Integer, MonsterAnimation>();
 				}
 			}
 		}
 
 		public void removeCharacter (int id) {
 			CharacterAnimation character = characters.remove(id);
 			if (character != null) System.out.println(character.name + " removed");
 		}
 	}
 
 	public static void main (String[] args) {
 		Log.set(Log.LEVEL_INFO);
 		new Client2D();
 	}
 	
 	public class EventHandler implements KeyListener, MouseListener, MouseMotionListener, FocusListener{
 
 		@Override
 		public void keyPressed(KeyEvent e) {
 			int key = e.getKeyCode();
 			if(key==KeyEvent.VK_ESCAPE) {
 				if(inGame){
 					if(menu.currentMenu != NONE) menu.currentMenu = NONE;
 					else backToMenu();
 				} else stop();
 			}
 			if(inGame){
 				if(key==keys[Network.JUMP]) moveKeys[Network.JUMP] = true;
 				if(key==keys[Network.DOWN]) moveKeys[Network.DOWN] = true;
 				if(key==keys[Network.UP]) moveKeys[Network.UP] = true;
 				if(key==keys[Network.LEFT]) moveKeys[Network.LEFT] = true;
 				if(key==keys[Network.RIGHT]) moveKeys[Network.RIGHT] = true;
 				for(int i = 0; i < skillKeys.length; i++){
 					if(key==skillKeys[i]) currentSkillKey = i;
 				}
 				if(key==KeyEvent.VK_A) if(menu.currentMenu == STAT) menu.currentMenu = NONE; else menu.currentMenu = STAT;
 				if(key==KeyEvent.VK_S) if(menu.currentMenu == SKILL) menu.currentMenu = NONE; else {menu.currentMenu = SKILL; skillMenu.refresh();}
 				if(key==KeyEvent.VK_X) client.sendTCP(new Pickup());
 				if(key==KeyEvent.VK_I) {
 					if(menu.currentMenu == INV) menu.currentMenu = NONE; else {
 						client.sendTCP(new RequestStats());
 						menu.currentMenu = INV;
 					}
 					listener.generateTooltips();
 				}
 				sendKeys();
 			}
 			e.consume();
 		}
 
 		@Override
 		public void keyReleased(KeyEvent e) {
 			int key = e.getKeyCode();
 			if(key==keys[Network.JUMP]) moveKeys[Network.JUMP] = false;
 			if(key==keys[Network.DOWN]) moveKeys[Network.DOWN] = false;
 			if(key==keys[Network.UP]) moveKeys[Network.UP] = false;
 			if(key==keys[Network.LEFT]) moveKeys[Network.LEFT] = false;
 			if(key==keys[Network.RIGHT]) moveKeys[Network.RIGHT] = false;
 			for(int i = 0; i < skillKeys.length; i++){
 				if(key==skillKeys[i] && currentSkillKey == i) currentSkillKey = -1;
 			}
 			sendKeys();
 			e.consume();
 		}
 
 		@Override
 		public void keyTyped(KeyEvent arg0) {
 		}
 		@Override
 		public void mouseDragged(MouseEvent e) {
 			if(!dragging && inGame)
 			if(menu.currentMenu == INV){
 				location = e.getLocationOnScreen();
 				for(int i = 0; i < 8; i++){
 					if(currentChar.inventory.equipSlot[i].getArea().contains(location) && currentChar.inventory.getEquip(i)!=null) {
 						previousI = i;
 						dragging = true;
 						previousItemSlot = i;
 					}
 					for(int j = 0; j < 8; j++){
 						if(currentChar.inventory.itemSlot[i][j].getArea().contains(location) && currentChar.inventory.getItem(i, j)!=null) {
 							previousI = i;
 							previousJ = j;
 							previousItemSlot = currentChar.inventory.getItem(i, j).getSlot();
 							dragging = true;
 						}
 					}
 				}
 				if(dragging) {
 					tooltip = null;
 					equippedTooltip = null;
 				}
 			}
 			location = e.getLocationOnScreen();
 		}
 		@Override
 		public void mouseMoved(MouseEvent e) {
 			location = e.getLocationOnScreen();
 			generateTooltips();
 		}
 		
 		public void generateTooltips(){
 			Point tooltipLocation = location;
 			Point equipTooltipLocation = new Point(location.x + 165,
 					location.y);
 			if (location.x + 346 > S.getWidth())
 			{
 				tooltipLocation = new Point(location.x - 170,
 						location.y);
 				equipTooltipLocation = new Point(location.x - 335,
 						location.y);
 			}
 
 			if (playing && inGame)
 			{
 				boolean onItem = false;
 
 				Item item;
 				if (menu.currentMenu == INV)
 				{
 					for (int i = 0; i < 8; i++)
 					{
 						for (int j = 0; j < 8; j++)
 						{
 							item = currentChar.inventory.getItem(i, j);
 							if (currentChar.inventory.itemSlot[i][j].getArea()
 									.contains(location) && item != null)
 							{
 								if (tooltip == null)
 								{
 									tooltip = new Tooltip(item, tooltipLocation);
 									Item equipped = currentChar.inventory
 											.getEquip(item.getSlot());
 									if (equipped != null)
 										equippedTooltip = new Tooltip(equipped,
 												equipTooltipLocation);
 								} else if (tooltip.getItem() != currentChar.inventory
 										.getItem(i, j))
 								{
 									tooltip = new Tooltip(item, tooltipLocation);
 									Item equipped = currentChar.inventory
 											.getEquip(item.getSlot());
 									if (equipped != null)
 										equippedTooltip = new Tooltip(equipped,
 												equipTooltipLocation);
 								} else
 								{
 									tooltip.setArea(tooltipLocation);
 									if (equippedTooltip != null)
 										equippedTooltip
 												.setArea(equipTooltipLocation);
 								}
 								onItem = true;
 							}
 							/*
 							if (stash.isOpen())
 							{
 								item = stash.getItem(i, j);
 								if (stash.stashSlots[i][j].getArea().contains(
 										location)
 										&& item != null)
 								{
 									if (tooltip == null)
 									{
 										tooltip = new Tooltip(item,
 												tooltipLocation);
 										Item equipped = currentChar.inventory
 												.getEquip(item.getSlot());
 										if (equipped != null)
 											equippedTooltip = new Tooltip(
 													equipped,
 													equipTooltipLocation);
 									} else if (tooltip.getItem() != stash
 											.getItem(i, j))
 									{
 
 										tooltip = new Tooltip(item,
 												tooltipLocation);
 										Item equipped = currentChar.inventory
 												.getEquip(item.getSlot());
 										if (equipped != null)
 											equippedTooltip = new Tooltip(
 													equipped,
 													equipTooltipLocation);
 									} else
 									{
 										tooltip.setArea(tooltipLocation);
 										if (equippedTooltip != null)
 											equippedTooltip
 													.setArea(equipTooltipLocation);
 									}
 									onItem = true;
 								}
 							}
 
 						}*/
 						if (currentChar.inventory.equipSlot[i].getArea().contains(
 								location)
 								&& currentChar.inventory.getEquip(i) != null)
 						{
 							if (tooltip == null)
 								tooltip = new Tooltip(
 										currentChar.inventory.getEquip(i),
 										tooltipLocation);
 							else if (tooltip.getItem() != currentChar.inventory
 									.getEquip(i))
 								tooltip = new Tooltip(
 										currentChar.inventory.getEquip(i),
 										tooltipLocation);
 							else
 								tooltip.setArea(tooltipLocation);
 							onItem = true;
 						}
 					}
 				}
 
 				if (!onItem)
 				{
 					tooltip = null;
 					equippedTooltip = null;
 				}
 				} else {
 					tooltip = null;
 					equippedTooltip = null;
 				}
 			}
 		}
 		@Override
 		public void mouseClicked(MouseEvent e) {
 			location = e.getLocationOnScreen();
 			SwitchItems switchItems = null;
 			if(currentChar != null && !dragging)
 			if(menu.currentMenu == INV){
 				for(int i = 0; i < 8; i++){
 					if(currentChar.inventory.equipSlot[i].getArea().contains(location) && currentChar.inventory.getEquip(i)!=null){
 						switchItems = new SwitchItems();
 						switchItems.previousI = i;
 					}
 					for(int j = 0; j < 8; j++){
 						if(currentChar.inventory.itemSlot[i][j].getArea().contains(location) && currentChar.inventory.getItem(i,j)!=null){
 							switchItems = new SwitchItems();
 							switchItems.previousI = i;
 							switchItems.previousJ = j;
 						}
 					}
 				}
 				if(switchItems != null){
 					if(switchItems.previousJ != -1){
 						if(e.getButton() == MouseEvent.BUTTON1)
 							switchItems.i = currentChar.inventory.getItem(switchItems.previousI, switchItems.previousJ).getSlot();
 					} else {
 						switchItems.add = true;
 					}
 					client.sendTCP(switchItems);
 				}
 			}
 		}
 		@Override
 		public void mouseEntered(MouseEvent arg0) {
 		}
 		@Override
 		public void mouseExited(MouseEvent arg0) {
 		}
 		@Override
 		public void mousePressed(MouseEvent e) {
 			Point location = e.getLocationOnScreen();
 			if(statMenu!=null)
 			if(menu.currentMenu == STAT){
 				for(int i = 0; i < statMenu.statButtons.length; i++)
 					if(statMenu.statButtons[i].getArea().contains(location))
 						statMenu.statButtons[i].activate();
 			}
 			if(skillMenu!=null)
 			if(menu.currentMenu == SKILL){
 				for(int i = 0; i < skillMenu.skillButtons.length; i++)
 					if(skillMenu.skillButtons[i].getArea().contains(location))
 						skillMenu.skillButtons[i].activate();
 			}
 			if(menu!=null){
 				if(menu.currentMenu != NONE){
 					for(int i = 0; i < menu.buttons.length; i++)
 						if(menu.buttons[i].getArea().contains(location))
 							menu.buttons[i].activate();
 				}
 			}
 			if(!inGame){
 				if(chooseClass){
 					for(int i = 0; i < classMenu.classButtons.length; i++){
 						if(classMenu.classButtons[i].getArea().contains(location))
 							classMenu.classButtons[i].activate();
 					}
 				} else {
 					for (SaveButton saveButton : mainMenu.saveButtons)
 					{
 						if (saveButton.getArea().contains(e.getLocationOnScreen()))
 							saveButton.activate();
 						if (saveButton.getDelete().contains(e.getLocationOnScreen()))
 							saveButton.deleteSave();
 					}
 				}
 			}
 		}
 		@Override
 		public void mouseReleased(MouseEvent e) {
 			if(dragging){
 				location = e.getLocationOnScreen();
 				SwitchItems switchItems = null;
 				if(menu.currentMenu == INV){
 					for(int i = 0; i < 8; i++){
 						if(currentChar.inventory.equipSlot[i].getArea().contains(location)){
 							switchItems = new SwitchItems();
 							switchItems.i = i;
 						}
 						for(int j = 0; j < 8; j++){
 							if(currentChar.inventory.itemSlot[i][j].getArea().contains(location)){
 								switchItems = new SwitchItems();
 								switchItems.i = i;
 								switchItems.j = j;
 							}
 						}
 					}
 				}
 				if(switchItems == null && !(currentChar.inventory.getArea().contains(location)&&menu.currentMenu == INV)) switchItems = new SwitchItems();
 				if(switchItems!=null){
 				switchItems.previousI = previousI;
 				switchItems.previousJ = previousJ;
 				
 				if(!(switchItems.previousI == switchItems.i && switchItems.previousJ == switchItems.j))
 				client.sendTCP(switchItems);
 				}
 				
 				previousI = -1; previousJ = -1; dragging = false;
 			}
 		}
 
 		@Override
 		public void focusGained(FocusEvent e) {}
 
 		@Override
 		public void focusLost(FocusEvent e) {
 			moveKeys[Network.UP]=moveKeys[Network.DOWN]=moveKeys[Network.LEFT]=moveKeys[Network.RIGHT]=false;
 			currentSkillKey=-1;
 			sendKeys();
 		}
 		
 	}
 	
 	public class DrawThread extends Thread{
 		long currentTime = System.currentTimeMillis();
 		long timePassed = 0;
 		Vector<MonsterAnimation> monsters;
 		Vector<CharacterAnimation> chars;
 		Vector<FlyingText> damageText;
 		Vector<DropAnimation> drops;
 		Vector<EffectAnimation> effects;
 		Vector<ProjectileAnimation> projectiles;
 		
 		@Override
 		public void run(){
 			while(playing){
 				timePassed = System.currentTimeMillis()-currentTime;
 				currentTime+=timePassed;
 				if(currentMap!=null){
 					updateAnimations(timePassed);
 				}
 					Graphics2D g = S.getGraphics();
 					
 					g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 	                         RenderingHints.VALUE_ANTIALIAS_ON);
 					draw(g);
 					g.dispose();
 					S.update();
 					timePassed = System.currentTimeMillis()-currentTime;
 					if(timePassed < 12)
 				try {Thread.sleep(12-timePassed);} catch (InterruptedException e) {}
 			}
 		}
 		
 		public synchronized void moveMap()
 		{
 			
 			if(currentChar != null && currentMap != null && currentChar.map.equals(currentMap.map)){
 			if(X+S.getWidth()*2/3 < currentChar.x + currentChar.getWidth()) X = currentChar.getX() + currentChar.getWidth() - S.getWidth()*2/3;
 			else if(X+S.getWidth()/3 > currentChar.x) X = currentChar.getX() - S.getWidth()/3;
 			
 			if(Y+S.getHeight()*2/3+20 < currentChar.y+currentChar.getHeight()) Y = currentChar.getY()+currentChar.getHeight() - S.getHeight()*2/3-20;
 			else if(Y+S.getHeight()/3 > currentChar.y) Y = currentChar.getY() - S.getHeight()/3;
 			if(X<0) X = 0;
 			else if(X > currentMap.Xlimit - S.getWidth()) X = currentMap.Xlimit - S.getWidth();
 			if(Y<0) Y = 0;
 			else if(Y > currentMap.Ylimit - S.getHeight()+50) Y = currentMap.Ylimit - S.getHeight()+50;
 			}
 			
 		}
 		
 		private void updateAnimations(long timePassed){
 			chars = new Vector<CharacterAnimation>(ui.characters.values());
 			for(CharacterAnimation c : chars){
 				c.update(timePassed);
 			}
 			if(ui.monsters != null){
 			monsters = new Vector<MonsterAnimation>(ui.monsters.values());
 			for(MonsterAnimation m : monsters){
 				m.update(timePassed);
 			}
 			}
 			damageText = new Vector<FlyingText>(flyingText);
 			for(FlyingText ft : damageText){
 				if(ft.isActive())
 				ft.update(timePassed);
 				else Client2D.flyingText.remove(ft);
 			}
 			drops = new Vector<DropAnimation>(Client2D.drops.values());
 			for(DropAnimation da : drops){
 				da.update(timePassed);
 			}
 			projectiles = new Vector<ProjectileAnimation>(Client2D.projectiles.values());
 			for(ProjectileAnimation pa : projectiles){
 				pa.update(timePassed);
 			}
 			effects = new Vector<EffectAnimation>(Client2D.effects.values());
 			for(EffectAnimation ea : effects){
 				if(ea.active)
 				ea.update(timePassed);
 				else Client2D.effects.remove(ea);
 			}
 		}
 		
 		private void draw(Graphics2D g){
 			if(inGame && currentMap != null && currentChar != null){
 				moveMap();
 				drawMap(g);
 				if(monsters!=null)
 					if(monsters.size()>0)
 						drawMonsters(g);
 				if(projectiles!=null)
 					drawProjectiles(g);
 				if(effects!=null)
 					drawEffects(g);
 				if(drops!=null)
 					drawDrops(g);
 			
 				drawChars(g);
 			
 				drawDamageText(g);
 				
 				if(currentChar==null) return;
 				drawUI(g);
 				
 				if(menu.currentMenu!= NONE)
 				drawCurrentMenu(g);
 			
 				if(tooltip!=null)
 					drawItemTooltip(g);
 				if(equippedTooltip!=null)
 					drawEquippedTooltip(g);
 			
 				if(dragging)
 					drawDraggedItem(g);
 				
 			} else drawMainMenu(g);
 			
 		}
 		
 		private void drawCurrentMenu(Graphics2D g){
 			switch(menu.currentMenu){
 			case INV: drawInventory(g); break;
 			case STAT: drawStatMenu(g); break;
 			case SKILL: drawSkillMenu(g); break;
 			case NONE: return;
 			}
 			drawMenuBar(g);
 		}
 		
 		private void drawMenuBar(Graphics2D g){
 			g.setColor(Color.GRAY);
 			g.fill(menu.area);
 			g.setColor(Color.BLACK);
 			g.draw(menu.area);
 			
 			g.setFont(new Font("Arial", Font.BOLD, 14));
 			g.setColor(Color.DARK_GRAY);
 			g.fill(menu.buttons[0].getArea());
 			g.setColor(Color.RED);
 			g.draw(menu.buttons[0].getArea());
 			g.drawString(menu.buttons[0].name, menu.buttons[0].x+5, menu.buttons[0].y+15);
 			
 			
 			g.setFont(new Font("Arial", Font.PLAIN, 14));
 			for(int i = 1; i < menu.buttons.length; i++){
 				g.setColor(Color.LIGHT_GRAY);
 				g.fill(menu.buttons[i].getArea());
 				g.setColor(Color.BLACK);
 				g.draw(menu.buttons[i].getArea());
 				g.drawString(menu.buttons[i].name, menu.buttons[i].x+5, menu.buttons[i].y+20);
 			}
 			
 			g.setFont(new Font("Arial", Font.PLAIN, 16));
 
 			g.drawString("Damage : " + currentChar.getMinDamage() + " - " + currentChar.getMaxDamage(),
 					menu.area.x + 10, menu.area.y + 178);
 			g.drawString("Defense : " + currentChar.defense, menu.area.x + 10, menu.area.y + 354);
 			g.drawString("Damage reduction : " + new DecimalFormat("#.#").format(currentChar
 								.getDamageReduction()) + "%", menu.area.x + 10, menu.area.y + 420);
 
 			for (int i = 0; i < 10; i++)
 			{
 				if (i != Network.ALLSTATS)
 				{
 					String info = "";
 					switch (i)
 					{
 					case Network.SPIRIT:
 						info = "Spirit : ";
 						break;
 					case Network.POW:
 						info = "Power : ";
 						break;
 					case Network.AGI:
 						info = "Agility : ";
 						break;
 					case Network.VIT:
 						info = "Vitality : ";
 						break;
 					case Network.CRIT:
 						info = "Crit chance : ";
 						break;
 					case Network.CRITDMG:
 						info = "Crit damage : ";
 						break;
 					case Network.MASTERY:
 						info = "Mastery : ";
 						break;
 					case Network.IF:
 						info= "Item find : ";
 						break;
 					case Network.RF:
 						info= "Rare chance : ";
 						break;
 					}
 					if (i == Network.CRIT)
 						info += new DecimalFormat("#.#").format(currentChar.getStat(i));
 					else
 						info += (int) currentChar.getStat(i);
 					if (i == Network.CRIT || i == Network.CRITDMG || i == Network.MASTERY || i == Network.IF || i == Network.RF)
 						info += "%";
 					g.drawString(info, menu.area.x + 10, menu.area.y + 200 + i
 							* 22);
 				}
 			}
 		}
 		
 		private void drawMainMenu(Graphics2D g){
 			if(chooseClass)
 			drawClassMenu(g);
 			else drawSaveMenu(g);
 		}
 		
 		private void drawSaveMenu(Graphics2D g){
 			g.setColor(Color.BLUE);
 			g.fillRect(0,0,1280,960);
 			g.setFont(new Font("Arial", Font.PLAIN, 20));
 			for (SaveButton saveButton : mainMenu.saveButtons)
 			{
 				g.setColor(Color.WHITE);
 				g.fill(saveButton.getArea());
 				g.setColor(Color.BLACK);
 				g.drawString(saveButton.info(), saveButton.infoPos.x,
 						saveButton.infoPos.y);
 				g.setColor(Color.RED);
 				g.fill(saveButton.getDelete());
 			}
 		}
 		
 		private void drawClassMenu(Graphics2D g)
 		{
 			g.setColor(Color.BLUE);
 			g.fillRect(0,0,1280,960);
 			g.setFont(new Font("Arial", Font.PLAIN, 30));
 			for (ClassButton classButton : classMenu.classButtons)
 			{
 				g.setColor(Color.WHITE);
 				g.fill(classButton.getArea());
 				g.setColor(Color.BLACK);
 				g.drawString(classButton.info(), classButton.infoPos.x, classButton.infoPos.y);
 			}
 		}
 		
 		private void drawDraggedItem(Graphics2D g){
 			if(previousItemSlot != -1)
 				g.drawImage(itemIcons[previousItemSlot],location.x, location.y, null);
 		}
 		
 		private void drawEquippedTooltip(Graphics2D g)
 		{
 
 			Tooltip ttip = equippedTooltip;
 			if(ttip != null){
 				g.setColor(Color.WHITE);
 				Rectangle r = ttip.getArea();
 				g.fillRect((int) r.getX(), (int) r.getY() - 20, (int) r.getWidth(), (int) r.getHeight() + 20);
 				g.setColor(Color.BLACK);
 				g.setFont(new Font("Arial", Font.PLAIN, 14));
 				g.drawString("Currently Equipped : ", (int) r.getX() + 7, (int) r.getY() - 5);
 				drawTooltip(g, ttip, r);
 			}
 		}
 		
 		private void drawTooltip(Graphics2D g, Tooltip ttip, Rectangle r)
 		{
 			Item item = ttip.getItem();
 			g.draw(r);
 			g.drawLine((int) r.getX(), (int) r.getY() + 46,
 					(int) (r.getX() + r.getWidth()), (int) r.getY() + 46);
 
 			for (int i = 1; i <= 8; i++)
 			{
 				g.setColor(Color.BLACK);
 				if ((i == 1 && item.getRarity() == Item.MAGIC) || (i == 2 && (item.getEnhancedD() != 0 || 
 						((item.getSlot() == Item.WEAPON && item.getStat(Network.WATK)!=0) || item.getSlot()!=Item.WEAPON && item.getStat(Network.DEFENSE) != 0))))
 					g.setColor(Color.BLUE);
 				if (i == 1 && item.getRarity() == Item.RARE)
 					g.setColor(Color.ORANGE);
 				g.drawString(ttip.getInfo(i), (int) r.getX() + 7,
 						(int) r.getY() + 2 + 20 * i);
 			}
 		}
 
 		private void drawItemTooltip(Graphics2D g)
 		{
 			Tooltip ttip = tooltip;
 			if(ttip != null){
 				g.setColor(Color.WHITE);
 				Rectangle r = ttip.getArea();
 				g.fill(r);
 				g.setColor(Color.BLACK);
 				g.setFont(new Font("Arial", Font.PLAIN, 14));
 				drawTooltip(g, ttip, r);
 			}
 		}
 		
 		private void drawInventory(Graphics2D g)
 		{
 			g.setColor(Color.GRAY);
 			g.fillRect(Inventory.x, Inventory.y, Inventory.width,
 					Inventory.height);
 			g.setColor(Color.BLACK);
 			g.drawRect(Inventory.x, Inventory.y, Inventory.width,
 					Inventory.height);
 
 			Item item;
 
 			for (int i = 0; i < currentChar.inventory.equipSlot.length; i++)
 			{
 				item = currentChar.inventory.getEquip(i);
 				if (item == null || (dragging && i == previousI && previousJ == -1))
 				{
 					g.setColor(Color.WHITE);
 					if (equipIcons[i] != null)
 						g.drawImage(equipIcons[i],
 								currentChar.inventory.equipSlot[i].getArea().x,
 								currentChar.inventory.equipSlot[i].getArea().y, null);
 					else
 						g.fill(currentChar.inventory.equipSlot[i].getArea());
 				} else
 				{
 					switch (item.getRarity())
 					{
 					case Item.COMMON:
 						g.setColor(Color.WHITE);
 						break;
 					case Item.MAGIC:
 						g.setColor(Color.CYAN);
 						break;
 					case Item.RARE:
 						g.setColor(Color.YELLOW);
 						break;
 					}
 					g.fill(currentChar.inventory.equipSlot[i].getArea());
 					g.drawImage(itemIcons[item.getSlot()],
 							currentChar.inventory.equipSlot[i].getArea().x,
 							currentChar.inventory.equipSlot[i].getArea().y, null);
 				}
 
 				for (int j = 0; j < 8; j++)
 				{
 					item = currentChar.inventory.getItem(i, j);
 					if (item == null || (dragging && i == previousI&& j == previousJ))
 						g.setColor(Color.WHITE);
 					else
 					{
 						switch (item.getRarity())
 						{
 						case Item.COMMON:
 							g.setColor(Color.WHITE);
 							break;
 						case Item.MAGIC:
 							g.setColor(Color.CYAN);
 							break;
 						case Item.RARE:
 							g.setColor(Color.YELLOW);
 							break;
 						}
 					}
 					g.fill(currentChar.inventory.itemSlot[i][j].getArea());
 					if (item != null && !(dragging && i == previousI&& j == previousJ))
 						g.drawImage(itemIcons[item.getSlot()],
 								currentChar.inventory.itemSlot[i][j].getArea().x,
 								currentChar.inventory.itemSlot[i][j].getArea().y,
 								null);
 				}
 			}
 		}
 		
 		private void drawDrops(Graphics2D g){
 			g.setFont(new Font("Arial", Font.PLAIN, 14));
 			for(int i = 0; i < drops.size(); i++){
 				DropAnimation drop = drops.get(i);
 				switch(drop.rarity){
 				case Item.MAGIC: g.setColor(Color.BLUE); break;
 				case Item.COMMON: g.setColor(Color.WHITE);  break;
 				case Item.RARE: g.setColor(Color.ORANGE); break;
 				}
 				String item = "";
 				switch(drop.type){
 				case Item.BOOTS: item = "Boots"; break;
 				case Item.GLOVES: item = "Gloves"; break;
 				case Item.AMULET: item = "Amulet"; break;
 				case Item.RING: item = "Ring"; break;
 				case Item.PANTS: item = "Pants"; break;
 				case Item.WEAPON: item = "Weapon"; break;
 				case Item.HELM: item = "Helm"; break;
 				case Item.TORSO: item = "Torso"; break;
 				}
 				g.drawString(item, drop.x-X, drop.y-Y-2);
 				g.drawImage(itemIcons[drop.type], (int)drop.x-X, (int)drop.y-Y, null);
 			}
 		}
 		
 		private void drawSkillMenu(Graphics2D g)
 		{
 			g.setColor(Color.GRAY);
 			g.fill(skillMenu.getArea());
 			g.setColor(Color.BLACK);
 			g.draw(skillMenu.getArea());
 			
 			for (SkillButton skillButton : skillMenu.skillButtons)
 			{
 				if(skillButton == null || skillButton.getArea() == null) continue;
 				g.setColor(Color.WHITE);
 				g.fill(skillButton.getArea());
 
 				if(!skillButton.passive){
 					g.setFont(new Font("Arial", Font.BOLD, 18));
 					switch (currentChar.classe)
 					{
 					case Character.MAGE:
 						g.setColor(Color.BLUE);
 						break;
 					case Character.FIGHTER:
 						g.setColor(Color.RED);
 						break;
 					case Character.ARCHER:
 						g.setColor(Color.ORANGE);
 						break;
 					}
 					g.drawString(""+skillButton.getManaUsed(), skillButton.getNamePos().x - 465,
 							skillButton.getNamePos().y);
 					
 				}
 				
 				
 				
 				g.setFont(new Font("Arial", Font.PLAIN, 14));
 				g.setColor(Color.BLACK);
 				String skillName = skillButton.getName();
 				g.drawString(skillName, skillButton.getNamePos().x + 23 - 4
 						* skillName.length(), skillButton.getNamePos().y);
 				String lvl;
 				if (skillButton.passive){
 					lvl = "" + currentChar.passiveLvls[skillButton.skillSlot];
 				}else{
 					lvl = "" + currentChar.skillLvls[skillButton.skillSlot];
 				}
 				g.setFont(new Font("Arial", Font.PLAIN, 12));
 				g.drawString(lvl, skillButton.getNamePos().x + 15,
 						skillButton.getNamePos().y - 20);
 				g.drawString(skillButton.getInfo(),
 						skillButton.getNamePos().x - 410,
 						skillButton.getNamePos().y - 10);
 			}
 
 			g.setColor(Color.WHITE);
 			g.drawString("Remaining points : " + currentChar.skillStats, 950, 500);
 		}
 		
 		private void drawStatMenu(Graphics2D g)
 		{
 			g.setFont(new Font("Arial", Font.PLAIN, 14));
 			g.setColor(Color.GRAY);
 			g.fill(statMenu.getArea());
 			g.setColor(Color.BLACK);
 			g.draw(statMenu.getArea());
 			
 			for (int i = 0; i < statMenu.statButtons.length; i++)
 			{
 				g.setColor(Color.WHITE);
 				g.fill(statMenu.statButtons[i].getArea());
 				g.setColor(Color.BLACK);
 				g.drawString(statMenu.statButtons[i].getText(),
 						statMenu.statButtons[i].getTextPosition().x,
 						statMenu.statButtons[i].getTextPosition().y);
 				g.drawString(statMenu.statButtons[i].getInfo(),
 						statMenu.statButtons[i].getTextPosition().x + 50,
 						statMenu.statButtons[i].getTextPosition().y - 20);
 
 				g.drawString(Integer.toString(currentChar.atts[i]),
 						statMenu.statButtons[i].getTextPosition().x + 8,
 						statMenu.statButtons[i].getTextPosition().y - 25);
 				g.setColor(Color.WHITE);
 				g.drawString("Remaining points : " + currentChar.attStats, statMenu.getArea().x+100, 480);
 			}
 		}
 		
 		private void drawDamageText(Graphics2D g){
 			g.setFont(new Font("Arial", Font.BOLD, 24));
 			for(FlyingText ft : damageText){
 				if(ft.isActive()){
 					g.setColor(ft.getColor());
 					g.drawString(ft.getText(), ft.getX()-X, ft.getY()-Y);
 				}
 			}
 		}
 		
 		private void drawProjectiles(Graphics2D g){
 			for(ProjectileAnimation proj : projectiles){
 				if(proj.a!=null)
 				g.drawImage(proj.a.getImage(), (int)proj.x-X,(int)proj.y-Y,null);
 			}
 		}
 		
 		private void drawEffects(Graphics2D g){
 			for(EffectAnimation eff : effects){
 				if(eff.a!=null && eff.active)
 				g.drawImage(eff.a.getImage(), eff.x-X,eff.y-Y,null);
 			}
 		}
 		
 		private void drawMap(Graphics2D g){
 			g.drawImage(currentMap.getBackground(),-X,-Y,null);
 			for(Spot spot : currentMap.getSpots()){
 				if(!spot.invisible)
 				g.drawImage(spotImage, spot.getArea().x-X, spot.getArea().y-Y, null);
 			}
 		}
 		
 		private void drawChars(Graphics2D g){
 			g.setFont(new Font("Arial", Font.PLAIN, 20));
 			for(CharacterAnimation c : chars){
 				if(c!=null && currentChar != null && c.getAnimation()!=null)if(c.map.equals(currentChar.map))if(c.alive){
 					
 					if(c.invincible){
 						c.blinkCount++;
 						if(c.blinkCount >= 4) c.blink = !c.blink;
 					} else {
 						c.blink = false;
 						c.blinkCount = 0;
 					}
 					
 					if(c.name.equals(currentChar.name))
 						moveMap();
 					if(!c.blink)
 					g.drawImage(c.getAnimation().getImage(),c.getX()-X, c.getY()-Y, null);
 					g.setColor(Color.RED);
 					g.fillRect(c.x-X,c.y-Y-5,c.getWidth(),5);
 					g.setColor(Color.GREEN);
 					g.fillRect(c.x-X,c.y-Y-5,c.getWidth()*c.life/c.maxLife,5);
 					g.setColor(Color.BLACK);
 					g.drawString(c.name, c.x-X+10, c.y-Y-6);
 				}
 			}
 		}
 		
 		private void drawMonsters(Graphics2D g){
 			for(MonsterAnimation m : monsters){
 				if(m!= null)if(m.alive){
 					if(m.getAnimation()!=null)
 						g.drawImage(m.getAnimation().getImage(), m.x-X, m.y-Y, null);
 					g.setColor(Color.RED);
 					g.fillRect(m.x-X, m.y-5-Y, m.getAnimation().getImage().getWidth(null), 5);
 					g.setColor(Color.GREEN);
 					g.fillRect(m.x-X, m.y-5-Y, (int)(m.getAnimation().getImage().getWidth(null)*m.lifePercentage/1000), 5);
 					g.setColor(Color.WHITE);
 					g.setFont(new Font("Arial", Font.PLAIN, 13));
 					String str = "";
 					if(m.summoned) str = "SUMMON ";  
 					g.drawString(str + m.getEliteType(),m.x-X,m.y-Y-19);
 					if(m.eliteType != -1){
 						g.setColor(Color.ORANGE);
 					} 
 					
 					g.drawString("Lv"+m.lvl+" "+m.getName(),m.x-X,m.y-Y-9);
 					g.setColor(Color.WHITE);
 				}
 			}
 		}
 		
 		private void drawUI(Graphics2D g)
 		{
 			g.setColor(Color.BLACK);
 			g.fillRect(0, S.getHeight() - 55, S.getWidth(), 55);
 			if (currentChar.classe == Character.FIGHTER)
 				g.setColor(Color.GRAY);
 			else
 				g.setColor(Color.RED);
 			g.fillRoundRect(100, S.getHeight() - 50, 400, 20,10,10);
 			g.fillRoundRect(100, S.getHeight() - 25, 400, 20,10,10);
 			g.setColor(Color.GREEN);
 			g.fillRoundRect(100, S.getHeight() - 50,400*currentChar.life/currentChar.maxLife, 20,10,10);
 			if (currentChar.classe == Character.MAGE)
 				g.setColor(Color.BLUE);
 			else if (currentChar.classe == Character.ARCHER)
 				g.setColor(Color.ORANGE);
 			else if (currentChar.classe == Character.FIGHTER)
 				g.setColor(Color.RED);
 			g.fillRoundRect(100, S.getHeight() - 25, 400*currentChar.mana/currentChar.maxMana, 20,10,10);
 			g.setColor(Color.WHITE);
 			g.setFont(new Font("Arial", Font.BOLD, 40));
 			g.drawString(Integer.toString(currentChar.lvl), 20, S.getHeight() - 10);
 			g.setFont(new Font("Arial", Font.PLAIN, 12));
 			g.drawString(
 					Integer.toString(currentChar.life) + " / "
 							+ Integer.toString(currentChar.maxLife), 260,
 					S.getHeight() - 35);
 			g.drawString(
 					Integer.toString(currentChar.mana) + " / "
 							+ Integer.toString(currentChar.maxMana), 260,
 					S.getHeight() - 10);
 			g.setColor(Color.YELLOW);
 			g.setFont(new Font("Arial", Font.PLAIN, 26));
 			g.drawString("Exp: " + Integer.toString(currentChar.exp) + "/"
 					+ Integer.toString(Character.expToLvl(currentChar.lvl)), 520,
 					S.getHeight() - 15);
 
 		}
 	}
 	
 	public class MenuBar
 	{
 		int currentMenu = 0;
 		public Rectangle area = new Rectangle(550,38,200,550);
 		MenuButton[] buttons = new MenuButton[4];
 		
 		public MenuBar(){
 			for(int i = 0; i <= 3; i++){
 				buttons[i] = new MenuButton(i, this);
 			}
 		}
 		
 		public class MenuButton{
 			String name;
 			MenuBar menu;
 			int menuNumber;
 			int x = 600, y, width=100, height=40;
 			
 			MenuButton(int i, MenuBar menu){
 				this.menu = menu;
 				menuNumber = i;
 				y=i*45+10;
 				switch(i){
 				case INV: name = "Inventory"; break;
 				case STAT: name = "Stats"; break;
 				case SKILL: name = "Skills"; break;
 				case NONE: name = "X";
 				y = 38;
 				x = 1230;
 				width = 20;
 				height = 20;
 				break;
 				}
 			}
 			
 			public Rectangle getArea(){return new Rectangle(x,y,width,height);}
 			
 			public void activate(){
 				if(menuNumber == SKILL) skillMenu.refresh();
 				menu.currentMenu = menuNumber;
 			}
 		}
 	}
 	
 	public class SkillButton
 	{
 		int skill, skillSlot;
 		private Rectangle area;
 		Point namePos;
 		public SkillData skillData, nextSkillData;
 		public PassiveData passiveData, nextPassiveData;
 		boolean passive;
 
 		public SkillButton(int i)
 		{
 			skillSlot = i;
 			if(i == 3) {
 				skillSlot = 0;
 				passive = true;
 			}
 			switch(currentChar.classe){
 			case Character.MAGE:
 				switch(i){
 				case 1: skill = Skill.FireBall; break;
 				case 2: skill = Skill.Explosion; break;
 				case 3: skill = PassiveSkill.WandMastery; break;
 				}
 				break;
 			case Character.ARCHER:
 				switch(i){
 				case 1: skill = Skill.DoubleArrow; break;
 				case 2: skill = Skill.ExplosiveArrow; break;
 				case 3: skill = PassiveSkill.BowMastery; break;
 				}
 				break;
 			case Character.FIGHTER:
 				switch(i){
 				case 1: skill = Skill.MultiHit; break;
 				case 2: skill = Skill.Smash; break;
 				case 3: skill = PassiveSkill.MopMastery; break;
 				}
 				break;
 			}
 		}
 
 		public Point getNamePos()
 		{
 			return namePos;
 		}
 
 		public void setArea(Rectangle area)
 		{
 			this.area = area;
 		}
 
 		public Rectangle getArea()
 		{
 			return area;
 		}
 
 		public int getSkill()
 		{
 			return skill;
 		}
 
 		public String getName()
 		{
 			if (passive)
 			{
 				switch (skill)
 				{
 				case PassiveSkill.WandMastery:
 					return "WandMastery";
 				case PassiveSkill.MopMastery:
 					return "MopMastery";
 				case PassiveSkill.BowMastery:
 					return "BowMastery";
 				}
 			} else
 				switch (skill)
 				{
 				case Skill.DoubleArrow:
 					return "DoubleArrow";
 				case Skill.ExplosiveArrow:
 					return "ExplosiveArrow";
 				case Skill.Explosion:
 					return "Explosion";
 				case Skill.MultiHit:
 					return "MultiHit";
 				case Skill.FireBall:
 					return "FireBall";
 				case Skill.Smash:
 					return "Smash";
 				}
 			return "";
 		}
 		
 		public int getManaUsed(){
 			if(passive) return 0;
 			skillData = new SkillData(skill, currentChar.skillLvls[skillSlot]);
 			if(skillData.manaUsed >= 0 ) return skillData.manaUsed;
 			return skillData.manaUsed*(currentChar.atts[Network.SPIRIT]+100)/100;
 		}
 
 		public String getInfo()
 		{
 			String info = "";
 
 			if (passive)
 			{
 				passiveData = new PassiveData(skill, currentChar.passiveLvls[skillSlot]);
 				nextPassiveData = new PassiveData(skill, currentChar.passiveLvls[skillSlot]+1);
 				
 				switch (skill)
 				{
 				case PassiveSkill.WandMastery:
 				case PassiveSkill.MopMastery:
 				case PassiveSkill.BowMastery:
 					info += "Increases weapon damage by "
 							+ passiveData.statBonus[Network.WATK]
 									+" ("+ nextPassiveData.statBonus[Network.WATK]+")"
 							+ " and weapon mastery by "
 							+ passiveData.statBonus[Network.MASTERY]
 								+" ("+ nextPassiveData.statBonus[Network.MASTERY]+")";
 				}
 			} else
 			{
 				skillData = new SkillData(skill, currentChar.skillLvls[skillSlot]);
 				nextSkillData = new SkillData(skill, currentChar.skillLvls[skillSlot]+1);
 	
 				info += "Deals ";
 				info += new DecimalFormat("#.##").format(skillData.dmgMult[0]) + " ("+new DecimalFormat("#.##").format(nextSkillData.dmgMult[0])+")"+" times your damage to ";
 				info += skillData.maxEnemiesHit +" ("+nextSkillData.maxEnemiesHit+")";
 				if (skillData.maxEnemiesHit == 1)
 					info += " enemy.";
 				else
 					info += " enemies.";
 				info += "Hits " + skillData.getMaxHits()+" ("+nextSkillData.getMaxHits()+")";
 				if (skillData.getMaxHits() == 1)
 					info += " time.";
 				else
 					info += " times.";
 			}
 
 			return info;
 		}
 
 		public void activate()
 		{
 			Lvlskill lvlskill = new Lvlskill();
 			lvlskill.skill = skillSlot;
 			lvlskill.passive = passive;
 			lvlskill.up = true;
 			client.sendTCP(lvlskill);
 			
 		}
 
 	}
 
 	public class SkillMenu
 	{
 
 		public SkillButton[] skillButtons = new SkillButton[3];
 		private int x = 750, y = 38, width = 500, height = 550;
 
 		public SkillMenu()
 		{}
 
 		public void refresh()
 		{
 			if(currentChar!=null)
 			for(int i = 0; i < skillButtons.length; i++){
 				skillButtons[i] = new SkillButton(i+1);
 				skillButtons[i].setArea(new Rectangle(x + width - 110, y + 50 + i * 75, 100, 50));
 				skillButtons[i].namePos = new Point(x + width - 80, y + 85 + i * 75);
 			}
 		}
 
 		public Rectangle getArea()
 		{
 			return new Rectangle(x, y, width, height);
 		}
 	}
 
 	public class StatButton
 	{
 
 		private Rectangle area;
 		private int stat;
 		private String text, info = "";
 		private Point textPosition;
 
 		public StatButton(Point position, int stat)
 		{
 			area = new Rectangle(position.x, position.y, 50, 50);
 			textPosition = new Point(position.x + 12, position.y + 40);
 			this.stat = stat;
 			switch (stat)
 			{
 			case Network.SPIRIT:
 				text = "SPI";
 				break;
 			case Network.POW:
 				text = "POW";
 				break;
 			case Network.AGI:
 				text = "AGI";
 				break;
 			case Network.VIT:
 				text = "VIT";
 				break;
 			}
 		}
 
 		public Rectangle getArea()
 		{
 			return area;
 		}
 
 		public Point getTextPosition()
 		{
 			return textPosition;
 		}
 
 		public String getText()
 		{
 			return text;
 		}
 
 		public String getInfo()
 		{
 			if(info != "") return info;
 			
 			switch (stat)
 			{
 			case Network.SPIRIT:
 				info = "Spirit increases total "+currentChar.getResourceName()+" and "+currentChar.getResourceName()+" regen by 1%";
 				break;
 			case Network.POW:
 				info = "Power increases all damage by 1%";
 				break;
 			case Network.AGI:
 				info = "Agility increases your chance to hit by 1%";
 				break;
 			case Network.VIT:
 				info = "Vitality increases your hp and healing/regen by 1%";
 				break;
 			}
 			return info;
 		}
 
 		public void activate()
 		{
 			Lvlstat attlvl = new Lvlstat();
 			attlvl.stat = stat;
 			client.sendTCP(attlvl);
 		}
 	}
 
 	public class StatMenu
 	{
 
 		public StatButton[] statButtons = new StatButton[4];
 		private Rectangle area = new Rectangle(750, 38, 500, 550);
 
 		public StatMenu()
 		{
 			for (int i = 0; i < 4; i++)
 				statButtons[i] = new StatButton(new Point(area.x+10, 80 + i * 100), i);
 
 		}
 
 		public Rectangle getArea()
 		{
 			return area;
 		}
 	}
 	
 	public class ClassMenu
 	{
 		public int saveSlot = 0;
 
 		public ClassButton[] classButtons = new ClassButton[3];
 
 		public ClassMenu()
 		{
 			for (int i = 0; i < 3; i++)
 				classButtons[i] = new ClassButton(i);
 		}
 	}
 
 	public class ClassButton
 	{
 
 		private Rectangle area;
 		private int classe;
 		public Point infoPos;
 
 		public ClassButton(int i)
 		{
 			this.classe = i;
 			area = new Rectangle(300 + i * 225, 300, 200, 200);
 			infoPos = new Point(360 + i * 225, 420);
 		}
 
 		public Rectangle getArea()
 		{
 			return area;
 		}
 
 		public int getClasse()
 		{
 			return classe;
 		}
 
 		public String info()
 		{
 			switch (classe)
 			{
 			case Character.MAGE:
 				return "Mage";
 			case Character.FIGHTER:
 				return "Fighter";
 			case Character.ARCHER:
 				return "Archer";
 			}
 			return "";
 		}
 
 		public void activate()
 		{
 			Register register = new Register();
 			register.name = name ;
 			register.classe = classe;
 			register.saveSlot = classMenu.saveSlot;
 			client.sendTCP(register);
 
 		}
 
 	}
 	
 
 public class SaveMenu
 {
 	public SaveButton[] saveButtons = new SaveButton[4];
 
 	public SaveMenu()
 	{
 		for (int i = 0; i < 4; i++)
 		{
 			saveButtons[i] = new SaveButton(i);
 		}
 	}
 
 }
 
 public class SaveButton
 {
 	public int lvl;
 	public String classe = "";
 	private int slot;
 	private Rectangle area, delete;
 	public Point infoPos;
 
 	public SaveButton(int i)
 	{
 		slot = i;
 		area = new Rectangle(800, 200 + 125 * i, 400, 100);
 		delete = new Rectangle(1200, 200 + 125 * i, 100, 100);
 		infoPos = new Point(825, 250 + 125 * i);
 	}
 
 	public int getSlot()
 	{
 		return slot;
 	}
 
 	public Rectangle getArea()
 	{
 		return area;
 	}
 
 	public Rectangle getDelete()
 	{
 		return delete;
 	}
 
 	public void activate()
 	{
 		Login login = new Login();
 		login.name = name;
 		login.pass = pass;
 		login.saveSlot = slot;
 		classMenu.saveSlot = slot;
 		client.sendTCP(login);
 	}
 	
 	public void setClasse(int i){
 		switch(i){
 		case -1 : 
 			classe = "";
 			break;
 		case Character.MAGE:
 			classe = "MAGE";
 			break;
 		case Character.FIGHTER:
 			classe = "FIGHTER";
 			break;
 		case Character.ARCHER:
 			classe = "ARCHER";
 			break;
 			
 		}
 	}
 
 	public void deleteSave()
 	{
 		DeleteSave delete = new DeleteSave();
 		delete.slot = slot;
 		delete.name = name;
 		client.sendTCP(delete);
 	}
 
 	public String info()
 	{
 		if(classe != "")
 		return classe + " lvl " + lvl;
 		return "New Game";
 	}
 }
 }
