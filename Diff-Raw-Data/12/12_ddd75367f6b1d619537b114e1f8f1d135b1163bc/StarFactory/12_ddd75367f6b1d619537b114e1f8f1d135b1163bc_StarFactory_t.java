 package com.calumgilchrist.ld23.tinyworld.core;
 
 import static playn.core.PlayN.graphics;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Random;
 
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.BodyDef;
 import org.jbox2d.dynamics.BodyType;
 import org.jbox2d.dynamics.World;
 
 import playn.core.GroupLayer;
 import playn.core.Image;
 
 public class StarFactory extends Factory{
 
 	private static ArrayList<Star> instances;
 	
 	public StarFactory(World world, Image img, GroupLayer layer) {
 		super(world, img, layer);
 		instances = new ArrayList<Star>();
 	}
 	
 	public Star getStar(Vec2 startPosition) {
 		
 		int starWeight = 100000;
 		//Start Vector off screen (This should be random)
 		startPosition = genStartPos(graphics().width(), graphics().height());
 		
 		// Set up an asteroid
 		BodyDef astrBodyDef = new BodyDef();
 		astrBodyDef.type= BodyType.STATIC;
 		
 		//Initial Position
 		astrBodyDef.position.set(startPosition.mul(1/Globals.PHYS_RATIO));
 		
 		startPosition.mulLocal(Globals.globalScale);
 		
 		Star star = new Star(new Sprite((int) startPosition.x, (int) startPosition.y, img), astrBodyDef, world, starWeight);
 		
 		instances.add(star);
 		layer.add(star.getSprite().getImageLayer());
 		
 		return star;
 	}
 	
 	public Vec2 genStartPos(int width, int height) {
 		
 		Random rand = new Random();
 		float x = 0;
 		float y = 0;
 		
 		int maxRand = 0;
 		
 		boolean xOffScreen = rand.nextBoolean();
 		
 		//Select a position off screen, and any other position
 		//Between min and max (-height, height for y)
 		if (xOffScreen) {
 			x = getSpawnBound(-width, width);
 			
 			maxRand = height * 2; 
 			
 			//Get position between height and -height
 			y = rand.nextInt(maxRand) - height;
 		} else {
 			y = getSpawnBound(-height, height);
 			
 			maxRand = height * 2; 
 			
 			//Get position between width and -width
 			x = rand.nextInt(maxRand) - width;
 		}
 
 		Vec2 pos = new Vec2(x, y);
 
 		return pos;
 	}
 	
 	private static int getSpawnBound(int min, int max) {
 
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
 	 * Return the heat from the stars at point
 	 */
 	public static float getHeat(Vec2 point) {
 		float thermalConstant = 1E6f;
 		float heatTotal = 0;
 		
 		//Get from sun
 		float heatSource = 0;
 		float distance;
 		
 		
 		for (Star star : instances) {
 			heatSource = star.getTemp();
			distance = star.getBody().getPosition().sub(point).length();
 			
 			//Constant * heatSource
 			//			 distance^2  		
 			heatTotal += thermalConstant * (heatSource / (distance * distance));
 		}
 		
 		return heatTotal;
 	}
 }
