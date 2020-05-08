 package demo;
 
 import java.awt.Graphics2D;
 import java.util.List;
 
 import com.golden.gamedev.object.background.ImageBackground;
 
 import core.characters.Character;
 import core.characters.ai.MoveState;
 import core.characters.ai.PatrolState;
 import core.configuration.key.KeyAnnotation;
 import core.configuration.key.KeyParser;
 import core.gamestate.Game2D;
 import core.gamestate.GameEngine2D;
 import core.items.AutoInUseAutoNotInUseItem;
 import core.items.AutoNotInUseItem;
 import core.items.CollectibleItem;
 import core.items.SetInUseSetNotInUseItem;
 import core.physicsengine.physicsplugin.PhysicsAttributes;
 import core.playfield.AdvancedPlayField;
 import core.playfield.hud.BarWidget;
 import core.playfield.hud.HUD;
 import core.playfield.hud.IconWidget;
 import core.playfield.hud.InventoryWidget;
 import core.playfield.hud.StringProxy;
 import core.playfield.hud.IntProxy;
 import core.playfield.hud.BarProxy;
 import core.playfield.hud.InventoryProxy;
 import core.playfield.hud.TextWidget;
 import core.playfield.scroller.ShowPlayfieldGameScroller;
 import core.tiles.*;
 import demo.custom.Boo;
 import demo.custom.DemoKeyAdapter;
 import demo.custom.Goomba;
 import demo.custom.Koopa;
 import demo.custom.Mario;
 
 /**
  * @author Glenn Rivkees (grivkees)
  */
 @SuppressWarnings("serial")
 public class DemoHUD extends Game2D {
 
 	private AdvancedPlayField myPlayfield;
 	private double endOfPlatform;
 	private Character mario;
 
 	public DemoHUD(GameEngine2D arg0) {
 		super(arg0);
 	}
 
 	public void initResources() {
 
 		// Playfield Init
 		myPlayfield = new AdvancedPlayField(3300, 500, this.getWidth(),
 		        this.getHeight());
 		myPlayfield.setBackground(new ImageBackground(
 		        getImage("resources/clouds.png")));
 		myPlayfield.setGameScroller(new ShowPlayfieldGameScroller());
 
 		// Sprite Init / Or load funcitonality
 		// SpriteGroups already exist in AdvancedPlayfield
 		// use addItem(sprite), addPlayer(), addCharacter(), or addSetting()
 
 		mario = new Mario(this, new PhysicsAttributes());
 		setKeyList(new KeyParser(this, false, new DemoKeyAdapter("key_type"))
 		        .parseKeyConfig("configurations/keyconfig.json"));
 		// add the element or the game you want the key to control
 		addKeyListeners(mario);
 		addKeyListeners(this);
 		mario.setLocation(25, 400);
 		myPlayfield.addPlayer(mario);
 
 		// HUD must be init after player
 		myPlayfield.addHUDWidget(new TextWidget("Coins", new StringProxy() {
 			public String get() {
 				return myPlayfield.getPlayer().getAttributeValue("points")
 				        .toString();
 			}
 		}), HUD.TOP_LEFT);
 
 		myPlayfield.addHUDWidget(
 		        new IconWidget("Lives", this.getImage("resources/life.png"),
 		                new IntProxy() {
 			                public int get() {
 				                return myPlayfield.getPlayer()
 				                        .getAttributeValue("lives").intValue();
 			                }
 		                }), HUD.TOP_CENTER);
 
 		myPlayfield.addHUDWidget(new BarWidget("HP", new BarProxy() {
 			public double get() {
 				return myPlayfield.getPlayer().getAttributeValue("hitPoints");
 			}
 
 			public double getInit() {
 				return myPlayfield.getPlayer().getBaseValue("hitPoints");
 			}
 		}), HUD.TOP_CENTER);
 
 		myPlayfield.addHUDWidget(new InventoryWidget("Inventory",
 		        new InventoryProxy() {
 			        public List<CollectibleItem> get() {
 				        return myPlayfield.getPlayer().getInventory();
 			        }
 		        }), HUD.TOP_RIGHT);
 
 		Character koopa1 = new Koopa(this, new PhysicsAttributes());
 		koopa1.addPossibleState("Move", new MoveState(koopa1, 1, true));
 		koopa1.setLocation(500, 400);
 		myPlayfield.addCharacter(koopa1);
 
 		Character goomba1 = new Goomba(this, new PhysicsAttributes());
 		goomba1.addPossibleState("Move", new MoveState(goomba1, 1, true));
 		goomba1.setLocation(800, 407);
 		myPlayfield.addCharacter(goomba1);
 
 		Character goomba2 = new Goomba(this, new PhysicsAttributes());
 		goomba2.addPossibleState("Move", new MoveState(goomba2, 1, true));
 		goomba2.setLocation(900, 407);
 		myPlayfield.addCharacter(goomba2);
 
 		Character goomba3 = new Goomba(this, new PhysicsAttributes());
 		goomba3.addPossibleState("Move", new MoveState(goomba3, 1, true));
 		goomba3.setLocation(1000, 407);
 		myPlayfield.addCharacter(goomba3);
 
 		Character goomba4 = new Goomba(this, new PhysicsAttributes());
 		goomba4.addPossibleState("Patrol", new PatrolState(goomba4, 1, 325));
 		goomba4.setLocation(575, 200);
 		myPlayfield.addCharacter(goomba4);
 
 		Tile temp1 = new FrictionlessDecorator(new Tile(this,
 		        new PhysicsAttributes()));
 		temp1.setImages(this.getImages("resources/IceFloor.png", 1, 1));
 		temp1.setLocation(1200, 440);
 		myPlayfield.addSetting(temp1);
 		
 		for(int i=0;i<2;i++){
 		    Tile temp2 = new Tile(this, new PhysicsAttributes());
 		    temp2.setImages(this.getImages("resources/Bar.png", 1, 1));
 		    temp2.setLocation(temp2.getWidth()*i, 440);
 		    myPlayfield.addSetting(temp2);
         }
 
 		ActionDecorator block2 = new BreakableDecorator(new Tile(this,
 		        new PhysicsAttributes()), 1);
 		block2.setBottomAction(true);
 		block2.setImages(this.getImages("resources/Block2Break.png", 8, 1));
 		block2.setLocation(160, 200);
 		myPlayfield.addSetting(block2);
 
 		ItemDecorator block1 = new ItemDecorator(new Tile(this,
 		        new PhysicsAttributes()));
 		block1.setBottomAction(true);
 		block1.setImages(this.getImages("resources/Block1.png", 1, 1));
 		block1.setLocation(100, 200);
 		myPlayfield.addSetting(block1);
 
 		for (int i = 0; i < 10; i++) {
 			AutoInUseAutoNotInUseItem coin = new AutoInUseAutoNotInUseItem(
 			        this, new PhysicsAttributes());
 			coin.setImages(this.getImages("resources/Coin.png", 1, 1));
 			coin.setActive(false);
 			coin.addAttribute("points", 1);
 			block1.addItem(coin);
 			myPlayfield.addItem(coin);
 		}
 
 		CollectibleItem coin2 = new AutoInUseAutoNotInUseItem(this,
 		        new PhysicsAttributes());
 		coin2.setImages(this.getImages("resources/Coin.png", 1, 1));
 		coin2.getPhysicsAttribute().setMovable(false);
 		coin2.setLocation(300, 300);
 		coin2.addAttribute("points", 1);
 		myPlayfield.addItem(coin2);
 
 		CollectibleItem coin3 = new AutoInUseAutoNotInUseItem(this,
 		        new PhysicsAttributes());
 		coin3.setImages(this.getImages("resources/Coin.png", 1, 1));
 		coin3.getPhysicsAttribute().setMovable(false);
 		coin3.setLocation(700, 150);
 		coin3.addAttribute("points", 1);
 		myPlayfield.addItem(coin3);
 
 		CollectibleItem coin4 = new AutoInUseAutoNotInUseItem(this,
 		        new PhysicsAttributes());
 		coin4.setImages(this.getImages("resources/Coin.png", 1, 1));
 		coin4.getPhysicsAttribute().setMovable(false);
 		coin4.setLocation(900, 200);
 		coin4.addAttribute("points", 1);
 		myPlayfield.addItem(coin4);
 
 		CollectibleItem coin5 = new AutoInUseAutoNotInUseItem(this,
 		        new PhysicsAttributes());
 		coin5.setImages(this.getImages("resources/Coin.png", 1, 1));
 		coin5.getPhysicsAttribute().setMovable(false);
 		coin5.setLocation(1300, 300);
 		coin5.addAttribute("points", 1);
 		myPlayfield.addItem(coin5);
 
 		CollectibleItem spike = new AutoInUseAutoNotInUseItem(this,
 		        new PhysicsAttributes());
 		spike.setImages(this.getImages("resources/Spikes.png", 1, 1));
 		spike.getPhysicsAttribute().setMovable(false);
 		spike.setLocation(400, 426);
 		spike.setActive(true);
 		spike.addAttribute("hitPoints", mario.getBaseValue("hitPoints"));
 		myPlayfield.addItem(spike);
 
 		CollectibleItem spike2 = new AutoInUseAutoNotInUseItem(this,
 		        new PhysicsAttributes());
 		spike2.setImages(this.getImages("resources/Spikes.png", 1, 1));
 		spike2.getPhysicsAttribute().setMovable(false);
 		spike2.setLocation(800, 427);
 		spike2.setActive(true);
 		spike2.addAttribute("hitPoints", -1*mario.getBaseValue("hitPoints"));
 		myPlayfield.addItem(spike2);
 
 		CollectibleItem spike3 = new AutoInUseAutoNotInUseItem(this,
 		        new PhysicsAttributes());
 		spike3.setImages(this.getImages("resources/Spikes.png", 1, 1));
 		spike3.getPhysicsAttribute().setMovable(false);
 		spike3.setLocation(2300, 285);
 		spike3.setActive(true);
 		spike3.addAttribute("hitPoints", -1*mario.getBaseValue("hitPoints"));
 		myPlayfield.addItem(spike3);
 
		CollectibleItem fireball = new SetInUseSetNotInUseItem(this, new PhysicsAttributes());
 		fireball.setImages(this.getImages("resources/Fireball.png", 4, 1));
 		fireball.setLoopAnim(true);
 		fireball.setAnimate(true);
 		fireball.getPhysicsAttribute().setMovable(false);
 		fireball.setLocation(350, 400);
 		myPlayfield.addItem(fireball);
 
 		AutoNotInUseItem poison = new AutoInUseAutoNotInUseItem(this,
 		        new PhysicsAttributes());
 		poison.setImages(this.getImages("resources/Poison.png", 1, 1));
 		poison.getPhysicsAttribute().setMovable(false);
 		poison.setLocation(300, 400);
 		poison.setTimerStart(1000);
 		poison.setTimerEnd(4000);
 		poison.addAttribute("hitPoints", -1);
 		myPlayfield.addItem(poison);
 
 		AutoNotInUseItem life = new AutoInUseAutoNotInUseItem(this,
 		        new PhysicsAttributes());
 		life.setImages(this.getImages("resources/life.png", 1, 1));
 		life.getPhysicsAttribute().setMovable(false);
 		life.setLocation(400, 100);
 		life.addAttribute("lives", 1);
 		myPlayfield.addItem(life);
 		
 		AutoNotInUseItem life2 = new AutoInUseAutoNotInUseItem(this,
 		        new PhysicsAttributes());
 		life2.setImages(this.getImages("resources/life.png", 1, 1));
 		life2.getPhysicsAttribute().setMovable(false);
 		life2.setLocation(1600, 120);
 		life2.addAttribute("lives", 1);
 		myPlayfield.addItem(life2);
 		
 		MovingDecorator middleBar = new MovingDecorator(new Tile(this,
 		        new PhysicsAttributes()));
 		middleBar.setLocation(260, 240);
 		middleBar.setEndLocation(700, 60);
 		middleBar.setMoveSpeed(0.05);
 		middleBar.setImages(getImages("resources/SmallBar.png", 1, 1));
 		myPlayfield.addSetting(middleBar);
 
 		Character boo = new Boo(this, new PhysicsAttributes(), mario);
 		boo.setLocation(2500, 150);
 		myPlayfield.addCharacter(boo);
 		
 		for (int i = 0; i < 4; i++){
 			FallingDecorator fallingBar = new FallingDecorator(new Tile(this, new PhysicsAttributes()), 2000);
 			fallingBar.setLocation(1800+i*150, 350);
 			fallingBar.setTopAction(true);
 			fallingBar.setImages(getImages("resources/Bar2.png", 1, 1));
 			myPlayfield.addSetting(fallingBar);
 		}
 		
 		for (int i = 0; i < 3; i++){
 			MovingDecorator movingBar = new MovingDecorator(new Tile(this, new PhysicsAttributes()));
 			movingBar.setLocation(2400 + i*150, 200+i*75);
 			movingBar.setEndLocation(2400+i*150, 400);
 			movingBar.setMoveSpeed(0.05);
 			movingBar.setImages(getImages("resources/Bar2.png", 1, 1));
 			myPlayfield.addSetting(movingBar);
 		}
 
 		for (int i = 0; i < 5; i++) {
 			Tile temp3 = new Tile(this, new PhysicsAttributes());
 			temp3.setImages(this.getImages("resources/Platform.png", 1, 1));
 			temp3.setLocation(2900+temp3.getWidth()*i, 300);
 			myPlayfield.addSetting(temp3);
 		}
 
 		Tile flag = new Tile(this, new PhysicsAttributes());
 		flag.setImages(getImages("resources/Flag.png", 4, 1));
 		flag.setLoopAnim(true);
 		flag.setAnimate(true);
 		flag.setLocation(3100, 300-flag.getHeight());
 		endOfPlatform = flag.getX();
 		myPlayfield.addSetting(flag);
 
 	}
 
 	public void update(long t) {
 		super.update(t);
 		myPlayfield.update(t);
 	}
 
 	public void render(Graphics2D g) {
 		myPlayfield.render(g);
 	}
 
 	@KeyAnnotation(action = "ESC")
 	public void pause() {
 		switchToGameObject(Pause.class);
 	}
 
 	@Override
 	public boolean isWin() {
 		if (mario.getX() >= endOfPlatform) {
 			reset();
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public void registerNextLevel() {
 		setNextLevel(DemoAntigravity.class);
 	}
 
 	@Override
 	public boolean isFail() {
 		if (mario.getBaseValue("lives") == 0) {
 			reset();
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public void registerGameOverEvent() {
 		setGameOverEvent(Menu.class);
 	}
 
 }
