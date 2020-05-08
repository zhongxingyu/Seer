 package ph.sm.jewels;
 
import java.util.HashSet;
 import java.util.LinkedList;
 
 import ch.aplu.android.Actor;
 import ch.aplu.android.GGActorCollisionListener;
 import ch.aplu.android.GGPanel;
 import ch.aplu.android.GGStatusBar;
 import ch.aplu.android.GGTouch;
 import ch.aplu.android.GGVector;
 import ch.aplu.android.GameGrid;
 import ch.aplu.android.Location;
 import ch.aplu.android.PointD;
 
 public class JewelsGame extends GameGrid implements GGActorCollisionListener {
 	
 	private GGStatusBar status;
 	private Hexagon hexagon;
 	private GGPanel p;
	private LinkedList<Jewel> jewels = new LinkedList<Jewel>();
 	private HealthPointBar hpBar;
 	private int score;
 
 	
 	public JewelsGame() {
 		super(WHITE, windowZoom(700));
 	    status = addStatusBar(30);
 	}
 	
 	public void main() {
 		getBg().clear(WHITE);
 		p = getPanel(-10, 10, 0.5);
 		p.setAutoRefreshEnabled(false);
 		hexagon = new Hexagon(p);
 		PointD hexagonSpawnPoint = new PointD(0,0);
 		addActorNoRefresh(hexagon, toLocation(p.toPixelPoint(hexagonSpawnPoint)));
 		for (int i = 1; i < 10; i++) {
			Jewel j = new Jewel(jewels);
 			//jewel is added to the jewels list in reset() when added to gamegrid
 			addActorNoRefresh(j, new Location(-100, -100)); //out of sight
 			hexagon.addCollisionActor(j);
 		}
 		hexagon.addActorCollisionListener(this);
 		setSimulationPeriod(30);
		doRun();
 		setPaintOrder(Hexagon.class, Jewel.class);
 		status.setText("Tap the screen to the right/left to turn Hexa-Pacman!");
 		addTouchListener(hexagon, GGTouch.press | GGTouch.release);
 		hpBar = new HealthPointBar(p, 50);
 	}
 	
 	public void act() {
 		if (hpBar.isGameOver()) {
 			doPause();
			showToast("Game Over!");
 		}
		if (!jewels.isEmpty() && Math.random() < 0.05 ) {
 			GGVector v = new GGVector(10, 10);
 			v.rotate(Math.random()*Math.PI*2);
 			PointD spawnPoint = new PointD(v);
			Actor spawningJewel = jewels.pollFirst();
 			spawningJewel.setLocation(toLocation(p.toPixelPoint(spawnPoint)));
 			spawningJewel.setDirection(spawningJewel.getLocation().getDirectionTo(hexagon.getLocation()));
 			spawningJewel.setActEnabled(true);
 		}
 	}
 	
	//not sure if this called before or after reset of reset of actors
 	public void reset() {
 		score = 0;
 		hpBar.setHealth(50);
 		doRun();
 	}
 
 	@Override
 	public int collide(Actor arg1, Actor collisionPartner) {
 		Jewel jewel = (Jewel) collisionPartner;
 		if (headedTowardsHexagonsMouth(jewel)) {
 			hexagon.eat();
 			hpBar.update(10);
 			score++;
 			status.setText("Number of jewels eaten: " + score);
 			jewel.reset();
 		} else 
 			if (!jewel.exploding()) { //only explode if not already exploding
 			hpBar.update(-7);
 			jewel.explode();
 		}
 		return 0;
 	}
 	
 	private boolean headedTowardsHexagonsMouth(Jewel jewel) {
 		double flyingDirection = hexagon.getLocation().getDirectionTo(jewel.getLocation());
 		return Math.abs(hexagon.getDirection() - flyingDirection) < 30;
 	}
 }
