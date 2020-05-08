 // SliceIt.java
 
 package ph.sm.sliceIt;
 
 import ch.aplu.android.Actor;
 import ch.aplu.android.GGActorCollisionListener;
 import ch.aplu.android.GGTouch;
 import ch.aplu.android.GGTouchListener;
 import ch.aplu.android.GameGrid;
import ch.aplu.android.L;
 import ch.aplu.android.Location;
 import ch.aplu.android.TextActor;
 
 public class SliceIt extends GameGrid implements GGTouchListener, GGActorCollisionListener{
 	private int points;
 	private final int FRUITSNR = 100;
 	private final FruitFactory ff = new FruitFactory(this, 35, FRUITSNR);
 	private Sword sword;
 
 	public SliceIt() {
 		super();
 	}
 
 	public void main() {
		setStatusText("FruitSmasher started, GG v " + getVersion());
 		sword = new Sword();
 		addActor(ff, new Location(0,0));
		addActor(sword, new Location(-20, -20));
 		addTouchListener(this, GGTouch.drag | GGTouch.release);
 		setSimulationPeriod(30);
 		doRun();
 	}
 	
 	public void increasePoints() {
 		points++;
 		setStatusText(points + " Points!");
 	}
 	
 	public void act() {
 		if (ff.outOfFruits() && getActors(Fruit.class).isEmpty())
 			gameOver();
 	}
 	
 	public Sword getSword() {
 		return this.sword;
 	}
 	
 	public void gameOver() {
		TextActor text = new TextActor("You smashed " + points + " out of " + FRUITSNR + " fruits");
 		TextActor perfect = new TextActor("Perfect round!");
 		addActor(text, new Location(10, 10));
 		if (points == FRUITSNR)
 			addActor(perfect, new Location(10, 33));
 		doPause();
 	}
 
 	public boolean touchEvent(GGTouch touch) {
 		switch (touch.getEvent()) {
 		case GGTouch.drag:
 			Location loc = new Location(touch.getX(),touch.getY());
 			sword.setLocation(loc);
 			break;
 		case GGTouch.release:
			sword.setLocation(new Location(-20,-20));
 		}
 		return true;
 	}
 	
 	public int collide(Actor actor1, Actor actor2) {
		L.v("collision between " + actor1 +" and "+ actor2 );
 		((Fruit) actor1).splatter();
 		return 1000;
 	}
 }
