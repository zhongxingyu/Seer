 package ph.sm.sliceIt;
 
 import java.util.Random;
 
 import ch.aplu.android.Actor;
 import ch.aplu.android.Location;
 
 public class FruitFactory extends Actor{
 	
 	private SliceIt gg;
 	private final int yMax = 200;
 	private final int yMin = 0;
 	private int intervall;
 	private Random rnd = new Random();
 	private int counter;
 	private int FruitsNr;
 	
 	/**
 	 * A factory utility that produces random fruits
 	 * in the gamegrid it is added.
 	 * @param gg GameGrid it belongs too
 	 * @param intervall between two created fruits (in SimulationCycles)
 	 * @param amount number of fruits produced
 	 */
 	public FruitFactory(SliceIt gg, int intervall, int amount) {
 		this.gg = gg;
 		this.intervall = intervall;
 		this.counter = 0;
 		this.FruitsNr = amount;
 	}
 	
 	public void act() {
 		if (FruitsNr > 0) {
 			if (counter > intervall) {
 				createRandomFruit();
 				FruitsNr--;
 			} else counter++;
 		}
 	}
 
 	private void createRandomFruit() {
 		float velX = rnd.nextFloat()*20F + 50F;
 		Fruit f;
 		int fruitInt = rnd.nextInt(100);
 		if (fruitInt < 30)
 			f = new Melon(gg, velX);
 		else if (fruitInt < 60)
 			f = new Orange(gg, velX);
 		else f = new Strawberry(gg, velX);
 		int y = rnd.nextInt(yMax-yMin) + yMin;
 		f.addCollisionActor(gg.getSword());
 		f.addActorCollisionListener(gg);
 		gg.addActor(f, new Location(0, y));
 		counter = 0;
 	}
 	
 	public boolean outOfFruits() {
 		return FruitsNr==0;
 	}
 }
 
 
 class Melon extends Fruit {
 	public Melon(SliceIt gg, float xVel) {
 		super("melon", gg, xVel);
 	}
 }
 
 class Orange extends Fruit {
 	public Orange(SliceIt gg, float xVel) {
 		super("orange", gg, xVel);
 	}	
 }
 
 class Strawberry extends Fruit {
 	public Strawberry(SliceIt gg, float xVel) {
 		super("strawberry", gg, xVel);
 	}	
 }
