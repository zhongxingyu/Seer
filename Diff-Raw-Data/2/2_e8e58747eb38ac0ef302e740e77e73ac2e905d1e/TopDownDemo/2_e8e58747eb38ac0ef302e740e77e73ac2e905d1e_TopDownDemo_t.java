 package game;
 
 import gameObjects.Enemy;
 import gameObjects.Player;
 import gameObjects.boss.Boss;
 import gameObjects.boss.FireBossCollision;
 import innerGameGUI.StartGUI;
 
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.List;
 
 import levelLoadSave.EnemyLoadObserver;
 import levelLoadSave.LevelLoader;
 import levelLoadSave.LoadObserver;
 import levelLoadSave.PlayerLoadObserver;
 import levelLoadSave.SimpleLoadObserver;
 import maps.Map;
 import weapons.DamagingProjectile;
 import weapons.Projectile;
 import weapons.ShotPattern;
 import weapons.SinglePattern;
 import weapons.UnlimitedGun;
 import weapons.Weapon;
 import bars.HealthBar;
 
 import com.golden.gamedev.Game;
 import com.golden.gamedev.object.Background;
 import com.golden.gamedev.object.PlayField;
 import com.golden.gamedev.object.Sprite;
 import com.golden.gamedev.object.SpriteGroup;
 
 import decorator.CompanionDecorator;
 //import decorator.MoveUpFastDecorator;
 import decorator.DecoratedShip;
 import decorator.InvisibilityDecorator;
 import decorator.PowerUp;
 import decorator.PowerUpDecorator;
 import decorator.SimplePowerUp;
 import decorator.SimpleShip;
 
 public class TopDownDemo extends Game {
 
 	private Enemy myEnemy;
 	private HealthBar myHealthBar;
 	private Weapon myWeapon;
 
 	//private Player myShip;
 	private Player myCompanion;
 	
 	private Boss myBoss;
 
 	private DecoratedShip decCompanion;
 	private PowerUp decoratedPowerUp;
 
 	private PowerUpDecorator myPowerUpDecorator;
 
 	private SpriteGroup myPlayerGroup;
 	private SpriteGroup myBarrierGroup;
 	private SpriteGroup myEnemyGroup;
 	private SpriteGroup myCompanionGroup;
 	private SpriteGroup myProjectileGroup;
 	private SpriteGroup myEnemyProjectileGroup;
 	private SpriteGroup BossWeakPoints, BossProjectiles;
 	private Background myBackground;
 	private PlayField myPlayfield;
 	private PlayerInfo playerInfo;
 	private int enemySize;
 	private Player myPlayer;
 
 	private BufferedImage myBackImage;
 	private Map myMap;
 	private int count = 0;
 	private List<LoadObserver> myLoadObservers;
 
 	private StartGUI start;
 	private boolean initialScreen, bossLoaded;
 
 	@Override
 	public void initResources() {
 		initialScreen = true;
 		bossLoaded = false;
 		myBarrierGroup = new SpriteGroup("barrier");
 		myPlayerGroup = new SpriteGroup("player");
 
 		myEnemyGroup = new SpriteGroup("enemy");
 		myProjectileGroup = new SpriteGroup("projectile");
 		myEnemyProjectileGroup = new SpriteGroup("enemy projectile");
 		myCompanionGroup = new SpriteGroup("companion");
 
 		
 		// init boss
 		myBoss = new Boss();
 		try {
 			myBoss.load(this);
 		} catch (SecurityException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NoSuchMethodException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		BossWeakPoints = myBoss.getSpriteGroup();
 		BossProjectiles = myBoss.getProjectiles();
 		
 		// init background using the new Map class
 		myBackImage = getImage("resources/BackFinal.png");
 		myMap = new Map(myBackImage, getWidth(), getHeight());
 
 		myMap.setSpeed(10);
 		myBackground = myMap.getMyBack();
 
 		myPlayerGroup.setBackground(myBackground);
 
 		myBarrierGroup.setBackground(myBackground);
 		myEnemyGroup.setBackground(myBackground);
 		myCompanionGroup.setBackground(myBackground);
 		myProjectileGroup.setBackground(myBackground);
 		myEnemyProjectileGroup.setBackground(myBackground);
 		BossWeakPoints.setBackground(myBackground);
 		BossProjectiles.setBackground(myBackground);
 		//myShip = new Player(200, 2700, "resources/ship.png");
 		//myShip.setImage(getImage("resources/ship.png"));
 		//System.out.println("1");
 
 		start = new StartGUI(this);
 		
 		
 		
 //		InvisibilityDecorator myInv = new InvisibilityDecorator(new SimplePowerUp(), myShip); 
 //		myPowerUpDecorator = new CompanionDecorator(myInv, myShip);
 		
 		//decCompanion = new ConstantlyMoveDecorator(new SimpleShip()); 
 		
 		//intit weapons
 		Projectile p = new DamagingProjectile("resources/fire.png",myProjectileGroup,1);
 		p.setImage(getImage("resources/fire.png"));
 		ShotPattern s = new SinglePattern(-1);
 		myWeapon = new UnlimitedGun(300,p,s);
 
 
 		// init playfield
 		myPlayfield = new PlayField(myBackground);
 //		myPlayfield.add(myBoss);
 		myPlayfield.addGroup(myPlayerGroup);
 		myPlayfield.addGroup(myBarrierGroup);
 		myPlayfield.addGroup(myEnemyGroup);
 		myPlayfield.addGroup(myProjectileGroup);
 		//myPlayfield.addGroup(myCompanionGroup);
 		myPlayfield.addGroup(myEnemyProjectileGroup);
 //		myPlayfield.addGroup(BossProjectiles);
 //		myPlayfield.addGroup(BossWeakPoints);
 
 		myPlayfield.addCollisionGroup(myPlayerGroup, myBarrierGroup,
 				new PlayerBarrierCollision());
//		myPlayfield.addCollisionGroup(myProjectileGroup, myEnemyGroup, new ProjectileAnythingCollision());
 		myPlayfield.addCollisionGroup(myProjectileGroup, myBarrierGroup, new ProjectileAnythingCollision());
 		myPlayfield.addCollisionGroup(myEnemyProjectileGroup, myPlayerGroup, new ProjectileAnythingCollision());
 		myPlayfield.addCollisionGroup(myEnemyProjectileGroup, myBarrierGroup, new ProjectileAnythingCollision());
 //		myPlayfield.addCollisionGroup(BossWeakPoints, myProjectileGroup, new FireBossCollision()); 
 //		myPlayfield.addCollisionGroup(BossProjectiles, myPlayerGroup, new ProjectileAnythingCollision());
 
 		// load level data
 		//loadLevelData();
 		
 
 		// initializing PlayerInfo
 		playerInfo = new PlayerInfo();		
 		//HealthBar
 		myHealthBar = new HealthBar(myPlayer); 
 
 
 	}
 
 	private void loadLevelData() {
 		myLoadObservers = new ArrayList<LoadObserver>();
 		myLoadObservers.add(new PlayerLoadObserver(myPlayerGroup,
 				myProjectileGroup, this));
 		myLoadObservers.add(new SimpleLoadObserver(myBarrierGroup));
 
 		myLoadObservers.add(new EnemyLoadObserver(myEnemyGroup,
 				myEnemyProjectileGroup));
 
 		LevelLoader l = new LevelLoader(myLoadObservers);
 		l.loadLevelData("serializeTest.ser");
 		enemySize = myEnemyGroup.getSize();
 
 		// initializing PlayerInfo
 		playerInfo = new PlayerInfo();
 		
 		//HealthBar
 		myHealthBar = new HealthBar(myPlayer); 
 
 	}
 
 	@Override
 	public void render(Graphics2D pen) {
 		if (initialScreen) {
 			start.render(pen);
 			return;
 		}
 		myPlayfield.render(pen);
 		
 		myHealthBar.render(pen);
 		
 
 	}
 
 	@Override
 	public void update(long elapsedTime) {
 		if (initialScreen) {
 			start.update(elapsedTime);
 			String path = start.getLoadPath();
 			if (path != null && path.length() > 0) {
 				initialScreen = false;
 				loadLevelData();
 			}
 			return;
 		}
 		myMap.moveMap(elapsedTime);
 		myMap.movePlayer(elapsedTime, myPlayer);
 		if(myMap.getFrameHeight() == 0 && !bossLoaded){
 			bossLoaded = true;
 			myPlayfield.add(myBoss);
 			myPlayfield.addGroup(BossProjectiles);
 			myPlayfield.addGroup(BossWeakPoints);
 			myPlayfield.addCollisionGroup(BossWeakPoints, myProjectileGroup, new FireBossCollision()); 
 			myPlayfield.addCollisionGroup(BossProjectiles, myPlayerGroup, new ProjectileAnythingCollision());
 		}
 		if (myBoss.transformed())
 			myPlayfield.addCollisionGroup(myBoss.getSpriteGroup(), myProjectileGroup, new FireBossCollision()); 
 		if (myBoss.isDead())
 			finish();
 		playerMovement();
 		myPlayer.move();
 		myPlayfield.update(elapsedTime);
 
 		if (myPlayer != null) {
 			myPlayer.fire(this, elapsedTime);
 		}
 
 		// this is for testing enemy movement
 		count = 0;
 		for (Sprite elem : myEnemyGroup.getSprites()) {
 			if (count >= enemySize)
 				break;
 			Enemy e = (Enemy) elem;
 			e.updateEnemy(elapsedTime);
 			count++;
 		}
 		playerInfo.updatePlayerPosition(myPlayer.getX(), myPlayer.getY());
 
 	}
 
 	public void playerMovement() {
 		// Used for movements or states that need access to info about player's
 		// movement
 		playerInfo.setUpwardMovement(keyDown(java.awt.event.KeyEvent.VK_W));
 		playerInfo.setDownwardMovement(keyDown(java.awt.event.KeyEvent.VK_S));
 		playerInfo.setLeftwardMovement(keyDown(java.awt.event.KeyEvent.VK_A));
 		playerInfo.setRightwardMovement(keyDown(java.awt.event.KeyEvent.VK_D));
 	}
 
 	public void setPlayer(Player g) {
 		myPlayer = g;
 		myPlayerGroup.add(g);
 	}
 	
 	
 
 }
