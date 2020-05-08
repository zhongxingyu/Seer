 package com.medievaltech.models;
 
 import com.medievaltech.utils.*;
 
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.util.Log;
 
 import java.util.Vector;
 import java.util.LinkedList;
 import android.util.*;
 import java.util.*;
 import com.medievaltech.triggers.*;
 
 public class Map {
 	Vector<Location> locations = new Vector<Location>();
 	Vector<Ship> ships = new Vector<Ship>();
 	Hashtable<String, Object> gameConfig = new Hashtable<String, Object>();
 	LinkedList<Trigger> triggers = new LinkedList<Trigger>();
 	int dimX, dimY;
 	int speedMultiple;
 	
 	public Map(int dimX, int dimY) {
 		this.dimX = dimX;
 		this.dimY = dimY;
 		this.speedMultiple = 0;
 		
 		setSpeedMultiple();
 		seedGameConfig();
 		populateTriggers();
 	}
 	
 	public Map(int dimX, int dimY, int numPlanets, int maxShipsPerPlanet) {
 		this.dimX = dimX;
 		this.dimY = dimY;
 		this.speedMultiple = 0;
 		
 		setSpeedMultiple();
 		seedGameConfig();
 		populateTriggers();
 		
 		for(int x=0; x<numPlanets; x++)
 			generateRandomPlanet(maxShipsPerPlanet);
 		
 		for(int x=0; x < (numPlanets*maxShipsPerPlanet)/2; x++)
 			generateRandomFlyingShip();
 	}
 	
 	public Ship getRandomShip() {
 		return ships.get(Utils.randomInt(ships.size()));
 	}
 	
 	public Ship getRandomDockedShip() {
 		Planet p;
 		do {
 			p = getRandomPlanet();
 		}while(p.ships.size() == 0);
 		
 		return  p.ships.elementAt(Utils.randomInt(p.ships.size()));
 	}
 	
 	public Planet getRandomPlanet() {
 		Location location;
 		do {
 			location = locations.elementAt(Utils.randomInt(locations.size()));
 		} while(!(location instanceof Planet));
 		return (Planet)location;
 	}
 	
 	public void addLocation(Location l) {
 		locations.add(l);
 	}
 	
 	public void addShip(Ship s) {
 		ships.add(s);
 	}
 	
 	public void generateRandomPlanet(int maxNumberOfDockedShips) {
 		Paint p = new Paint();
         p.setAntiAlias(true);
         p.setColor(Color.rgb(Utils.randomInt(256), Utils.randomInt(256), Utils.randomInt(256)));
     	
         int radius, x, y;
         boolean collision;
         
         do {
         	radius = Utils.randomInt(10, 50);
             x = Utils.randomInt(dimX-radius*2)+radius;
             y = Utils.randomInt(dimY-radius*2)+radius;
             collision = false;
             
             for(Location l : locations) {
             	if(l.getCoordinates().distanceTo(new DoublePoint(x, y))-l.getCollisionRadius()-radius <= 0) {
             		collision = true;
             		break;
             	}
             }
         } while(collision);
         
 		int numberOfShips = Utils.randomInt(maxNumberOfDockedShips + 1);
 		
 	    Planet planet = new Planet(x, y, radius, p);
 		this.addLocation(planet);
 		
 		for(int i = 0; i < numberOfShips; i++)
 			generateRandomDockedShip(planet);
 	}
 	
 	public void generateRandomDockedShip(Location location) {
 		Paint shipPaint = new Paint();
 		shipPaint.setAntiAlias(true);
 		shipPaint.setColor(Color.rgb(Utils.randomInt(256), Utils.randomInt(256), Utils.randomInt(256)));
 		
 		Ship ship = new Ship(location, 10, shipPaint);
		location.dockShip(ship);
 		addShip(ship);
 	}
 	
 	public void generateRandomFlyingShip() {
 		Paint shipPaint = new Paint();
 		shipPaint.setAntiAlias(true);
 		shipPaint.setColor(Color.rgb(Utils.randomInt(256), Utils.randomInt(256), Utils.randomInt(256)));
 		
 		Ship ship = new Ship(new DoublePoint(Utils.randomInt(2,dimX - 1),Utils.randomInt(2,dimY - 1)),this.getRandomPlanet() ,10, shipPaint);
 		addShip(ship);
 	}
 	
 	public void seedGameConfig() {
		this.gameConfig.put("randomShipChance",0.03);
 		this.gameConfig.put("map",this);
 	}
 	
 	public void populateTriggers() {
 		this.triggers.add(new ShipTrigger(this.gameConfig));
 	}
 	
 	public void draw(Canvas c) {
 		for(Location l : locations) {
 			l.draw(c);
 		}
 		
 		for(Ship s : ships) {
 			s.draw(c);
 		}
 	}
 	
 	public void update(long lastUpdatedAt) {
 		Log.i("SpaceRace", "Inside map.update");
 		
 		for(Ship s : ships) {
         	s.update(this.speedMultiple, lastUpdatedAt);
         }
 		
 		for(Trigger t: this.triggers)
 			t.trigger();
 	}
 	
 	public void createPlanets(int x) {
 
 	}
 	
 	private void setSpeedMultiple() 
 	{
 		if(this.speedMultiple != 0)
 			return;
 		else if(this.dimX == 0 || this.dimY == 0)
 			return;
 		else{
 			Resolution res = new Resolution(dimX, dimY);
 			
 			if(res.getLargestMultiple() != 0)
 				this.speedMultiple = res.getLargestMultiple();
 			else
 				this.speedMultiple = 100;
  		}
 	}
 }
