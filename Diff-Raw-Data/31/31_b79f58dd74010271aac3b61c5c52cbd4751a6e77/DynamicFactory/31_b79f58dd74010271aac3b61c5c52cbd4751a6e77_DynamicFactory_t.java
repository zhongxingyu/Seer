 package com.calumgilchrist.ld23.tinyworld.core;
 
 import static playn.core.PlayN.assets;
 import static playn.core.PlayN.graphics;
 
 import java.util.Iterator;
 import java.util.Random;
 
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.Body;
 import org.jbox2d.dynamics.BodyDef;
 import org.jbox2d.dynamics.BodyType;
 import org.jbox2d.dynamics.World;
 
 import playn.core.GroupLayer;
 import playn.core.Image;
 
 public class DynamicFactory extends Factory {
 	
 	Image asteroidImage;
 	Image planetoidImage;
 
 	public DynamicFactory(World world, GroupLayer layer) {
 		super(world, 
 				layer);
 		asteroidImage = assets().getImage("images/bubbly-asteroid.png");
 		planetoidImage = assets().getImage("images/comet.png");
 	}
 
 	public Asteroid getAsteroid() {
 		Random rand = new Random();
 
 		int asteroidMaxWeight = 20;
 		int asteroidMaxForce = 100 / 2;
 		float forceFactor = rand.nextInt(asteroidMaxForce) + asteroidMaxForce;
 		
 		float astrWeight = rand.nextInt(asteroidMaxWeight);
 		
 		//TODO: Multiplier should be somewhere else (Reduces mulitplication)
 		// Start Vector off screen (This should be random)
 		Vec2 astrStart = genStartPos(graphics().width(), graphics().height()).mul(1/Globals.globalScale);
 
 		// Set up an asteroid
 		BodyDef astrBodyDef = new BodyDef();
 		astrBodyDef.type = BodyType.DYNAMIC;
 
 		// Initial Position
 		astrBodyDef.position.set(astrStart.mul(1 / Globals.PHYS_RATIO));
 
 		astrStart.mulLocal(Globals.globalScale);
 
 		Asteroid astr = new Asteroid(astrStart, new Sprite((int) astrStart.x,
 				(int) astrStart.y, asteroidImage), astrBodyDef, world, astrWeight);
 
 		astr.applyThrust(astr.getThrustForce(forceFactor));
 
 		instances.add(astr);
 		layer.add(astr.getSprite().getImageLayer());
 
 		return astr;
 	}
 
 	public Comet getComet() {
 
 		Random rand = new Random();
 
 		int cometMaxWeight = 10;
 		int cometMaxForce = 75 / 2;
 		float forceFactor = rand.nextInt(cometMaxForce) + cometMaxForce;
 		
 		float  cometWeight = rand.nextInt(cometMaxWeight);
 		
 		// Start Vector off screen (This should be random)
 		Vec2 cometStart = genStartPos(graphics().width(), graphics().height()).mul(1/Globals.globalScale);
 
 		// Set up an asteroid
 		BodyDef cometBodyDef = new BodyDef();
 		cometBodyDef.type = BodyType.DYNAMIC;
 
 		// Initial Position
 		cometBodyDef.position.set(cometStart.mul(1 / Globals.PHYS_RATIO));
 
 		cometStart.mulLocal(Globals.globalScale);
 
 		Comet comet = new Comet(new Sprite((int) cometStart.x,
 				(int) cometStart.y, planetoidImage), cometBodyDef, world, cometWeight);
 
 		comet.applyThrust(comet.getThrustForce(forceFactor));
 
 		instances.add(comet);
 		setCometCount(getCometCount() + 1);
 		
 		layer.add(comet.getSprite().getImageLayer());
 		
 		return comet;
 	}
 
 	public Vec2 genStartPos(int width, int height) {
 
 		Random rand = new Random();
 		float x = 0;
 		float y = 0;
 
 		int maxRand = 0;
 
 		boolean xOffScreen = rand.nextBoolean();
 
 		// Select a position off screen, and any other position
 		// Between min and max (-height, height for y)
 		if (xOffScreen) {
 			x = getSpawnBound(-width, width);
 
 			maxRand = height * 2;
 
 			// Get position between height and -height
 			y = rand.nextInt(maxRand) - height;
 		} else {
 			y = getSpawnBound(-height, height);
 
 			maxRand = width * 2;
 
 			// Get position between width and -width
 			x = rand.nextInt(maxRand) - width;
 		}
 
 		Vec2 pos = new Vec2(x, y);
 		
 		return pos;
 	}
 
 	/**
 	 * Randomly choose a number NOT between 0 and limit
 	 * 
 	 * @param max
 	 * @return
 	 */
 	private int getSpawnBound(int min, int max) {
 
 		Random rand = new Random();
 
 		int spawnBound = 100;
 		// Set x
 		boolean belowBound = rand.nextBoolean();
 
 		int pos = rand.nextInt(spawnBound) + 50;
 
 		if (belowBound) {
 			pos = -pos + min;
 		} else {
 			pos += max;
 		}
 
 		return pos;
 	}
 	
 	/**
 	 * Find the Asteroid then tell it to be removed
 	 * @param body - Child body to be destroyed
 	 */
 	public void removeByBody(Body body) {
 		Planetoid astr = getFromBody(body);
 		
 		astr.getBody().destroyFixture(astr.getBody().m_fixtureList);
 		
 		if (astr != null) {
 			remove(astr);
 		} 
 		//TODO if null, should probably mention it
 	}
 	
 	/**
 	 * Find the parent Asteroid from the given body
 	 * @param body
 	 * @return
 	 */
 	public Planetoid getFromBody(Body body) {
 		boolean found = false;
 		Planetoid planet = null;
 		
 		/*
 		 * Slow and awful
 		 * TODO: Use something better than O(n)
 		 */
		for (Planetoid p : instances) {

			if (p.getBody().equals(body)) {
				planet = p;
 			}
 		}
 		
		return planet;
 	}
		
 	private void remove(Planetoid astr) {
 		
 		if (astr.getClass().equals(new Comet().getClass())) {
 			//Remove a comet from list
 			setCometCount(getCometCount() - 1);
 		}
 		
 		instances.remove(astr);
 		
 		//Destroy the planet
 		destroyList.add(astr.getBody());
 		
 		astr.getSprite().getImageLayer().destroy();
		astr.setSprite(new Sprite());
		
		System.out.println(astr.getSprite().getImageLayer());
 	}
 	
 	/**
 	 * Generate an equal ratio of asteroids and comets
 	 */
 	public void createDebris(int count) {
 		Random rand = new Random();
 		
 		float ratio = 0;
 		
 		for (int i=0; i < count; i++) {
 			ratio = (float) rand.nextInt(100) / 100;
 			
 			if (ratio < Globals.asteroidRatio) {
 				getAsteroid();
 			} else if (ratio < Globals.cometRatio) {
 				getComet();
 			} else {
 				i--;
 			}
 			int j=0;
 		}
 	}
	
	public void clearAsteroids() {
		instances.clear();
	}
 }
