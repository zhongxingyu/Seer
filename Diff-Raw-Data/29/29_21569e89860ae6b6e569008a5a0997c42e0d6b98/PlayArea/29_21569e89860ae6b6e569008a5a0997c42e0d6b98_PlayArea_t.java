 package areas;
 
 import java.awt.Color;
 import java.awt.image.BufferedImage;
 
 import jgame.Context;
 import jgame.GContainer;
 import jgame.GObject;
 import jgame.GSprite;
 import jgame.ImageCache;
 import jgame.listener.TimerListener;
import Enemies.Enemy;
import Enemies.Enemy1;
import Enemies.Enemy2;
import Enemies.Enemy3;
import Enemies.Enemy4;
import Enemies.Enemy5;
 import dtb.Defend;
 
 public class PlayArea extends GContainer {
 	public PlayArea() {
 		setSize(1280, 720);
 		setBackgroundColor(Color.PINK);
 		BufferedImage bg = ImageCache.forClass(Defend.class).get(
 				"Level1fix.png");
 		GSprite g = new GSprite(bg);
 		setBackgroundSprite(g);
 
 		final TimerListener enemyTimer = new TimerListener(120) {
 
 			@Override
 			public void invoke(GObject target, Context context) {
 				addRandomEnemy();
 			}
 
 		};
 		addListener(enemyTimer);
 	}
 
 	private void addRandomEnemy() {
 		int pick = (int) (Math.random() * 5);
 		Enemy e = null;
 		switch (pick) {
 		case 0:
 			e = new Enemy1();
 			break;
 		case 1:
 			e = new Enemy2();
 			break;
 		case 2:
 			e = new Enemy3();
 			break;
 		case 3:
 			e = new Enemy4();
 			break;
 		case 4:
 			e = new Enemy5();
 			break;
 		}
 		this.add(e);
 	}
 }
