 
 package hell;
 
 import hell.Hell.View;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Image;
import java.awt.Rectangle;
 import java.awt.image.BufferedImage;
 import java.util.List;
 
 import jgame.ButtonState;
 import jgame.Context;
 import jgame.GButton;
 import jgame.GContainer;
 import jgame.GMessage;
 import jgame.GObject;
 import jgame.GSprite;
 import jgame.ImageCache;
 import jgame.listener.ButtonListener;
 import jgame.listener.FrameListener;
 import jgame.listener.TimerListener;
 
 public class GameView extends GContainer {
 	TurretCreation tr;
	private Bank bank;
 	private GMessage setGodBank = new GMessage();
 	int tcounter = 0;
 
 	public GameView() {
 
 
 		BackGround ground = new BackGround();
 		add(ground);
 		ground.setLocation(480, 320);
 
 		setSize(1200, 800);
 		setAnchorTopLeft();
		bank = new Bank(500);
 		
 		BufferedImage img = ImageCache.getImage("tileBank.png");
 		GSprite gb = new GSprite(img);
 		
 		//GSprite bankTile = createSprite ();
 		addAt(gb, 1000, 710);
 		
 		FrameListener fl = new FrameListener() {
 			@Override
 			public void invoke(GObject target ,Context context) {
 				//setGodBank(bank.getMoney());
 				tcounter++;
				setGodBank.setText(setGod());
 			
 			}
 		};
 		addListener(fl);
 		
 		setGodBank.setX(550);
 		setGodBank.setY(130);
 		setGodBank.setColor(Color.getHSBColor(1f / 6f, 0.75f, 1f));
 		setGodBank.setFont(new Font("Times New Roman", Font.BOLD, 48));
 		add(setGodBank);
 		
 		GButton exit = new GButton();
 		GSprite buttonImageThree = new GSprite(ImageCache.forClass(Hell.class)
 				.get("tilenone.png"));
 		GSprite hoveredImageThree = new GSprite(ImageCache.forClass(Hell.class)
 				.get("tilehover.png"));
 		GSprite pressedImageThree = new GSprite(ImageCache.forClass(Hell.class)
 				.get("tilepressed.png"));
 
 		exit.setStateSprite(ButtonState.NONE, buttonImageThree);
 		exit.setStateSprite(ButtonState.HOVERED, hoveredImageThree);
 		exit.setStateSprite(ButtonState.PRESSED, pressedImageThree);
 
 		ButtonListener exitNow = new ButtonListener() {
 			@Override
 			public void mouseClicked(Context context) {
 
 				context.setCurrentGameView(View.GAMEMAIN);
 					
 				
 			}
 		};
 		exit.addListener(exitNow);
 		addAt(exit, 1025, 50);
 		
 		final TimerListener enemyTimer = new TimerListener(40) {
 
 			@Override
 			public void invoke(GObject target, Context context) {
 				addRandomEnemy();
 			}
 		};
 		addListener(enemyTimer);
 
 		for (int i = 0; i < 5; i++) {
 			setTile(i);
 		}
 	}
 
 	List<Image> tileImage = ImageCache.forClass(Hell.class).getSequential(
 			"Tile/image", 1, 5, ".png");
 	
	public String setGod (int bank){
		 return Integer.toString(bank);
//		 setGodBank.setText(Integer.toString(bank));
//		 return setGodBank;
	 }

 	public void setTile(int i) {
 
 		Tile tile = new Tile(new TurretCreation(i), tileImage.get(i));
 		tile.setAnchorTopLeft();
 		tile.setScale(1.05);
 		addAtCenter(tile);
 		tile.setX(i * 150 + 30);
 		tile.setY(650);
 	}
 	
 	
 //	public static GSprite createSprite() {
 //		BufferedImage img = ImageCache.getImage("tileBank.png");
 //		GSprite gb = new GSprite(img);
 //
 //		Rectangle nP = new Rectangle(15, 15, 6, 6);
 //		gb.setNineSliceCenter(nP);
 //		gb.setSize(250, 130);
 //		return gb;
 //	}
 
 	private void addRandomEnemy() {
 
 		int enemyPick = (int) (Math.random() * 4);
 
 		Enemy e = null;
 		switch (enemyPick) {
 		case 0:
 			e = new EnemyOne();
 			break;
 
 		case 1:
 			e = new EnemyTwo();
 			break;
 
 		case 2:
 			e = new EnemyThree();
 			break;
 
 		case 3:
 			e = new EnemyFour();
 			break;
 
 		default:
 			e = new EnemyTwo();
 			break;
 		}
 
 		this.add(e);
 	}
 	 	
 	public boolean turretAfford(int turNum) {
 		boolean bool = false;
 		switch(turNum){
 		case 1:
 		if(TowerOne.getCost() <= Bank.getMoney()) {
 				Bank.takeMoney(TowerOne.getCost());
 				bool = true;
 			}
 			break;
 			
 		case 2:
 			if(TowerTwo.getCost() <= Bank.getMoney()) {
 				Bank.takeMoney(TowerTwo.getCost());
 				bool = true;
 			}
 			break;
 			
 		case 3:
 			if(TowerThree.getCost() <= Bank.getMoney()) {
 				Bank.takeMoney(TowerThree.getCost());
 				bool = true;
 			}
 			break;
 			
 		case 4:
 			if(TowerFour.getCost() <= Bank.getMoney()) {
 				Bank.takeMoney(TowerFour.getCost());
 				bool = true;
 			}
 			break;
 			
 		case 5:
 			if(TowerFive.getCost() <= Bank.getMoney()) {
 				Bank.takeMoney(TowerFive.getCost());
 				bool = true;
 			}
 			break;
 			
 		default:
 			bool = false;
 			break;
 		}
 		return bool;
 	}
 
 }
