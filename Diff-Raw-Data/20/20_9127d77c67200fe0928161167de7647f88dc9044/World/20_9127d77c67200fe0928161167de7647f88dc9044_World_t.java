 package asteroids.model;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;
 
 import asteroids.Collision;
 import asteroids.Util;
 import asteroids.Vector;
 import asteroids.CollisionListener;
 
 public class World {
 	private static final double minSize = 0;
 	private static final double maxSize = Double.MAX_VALUE;
 	private Vector dimensions;
 	private Set<Ship> allShips = new HashSet<Ship>();
 	private Set<Asteroid> allAsteroids = new HashSet<Asteroid>();
 	private Set<Bullet> allBullets = new HashSet<Bullet>();
 
 	public World(Vector dimensions){
 		setDimensions(dimensions);
 	}
 
 	private boolean isValidDimensions(Vector dimensions){
 		return(	dimensions.getX() >= minSize && 
 				dimensions.getX() <= maxSize && 
 				dimensions.getY() >= minSize && 
 				dimensions.getY() <= maxSize);
 	}
 
 	public void setDimensions(Vector dimensions){
 		if (!isValidDimensions(dimensions))
 			throw new IllegalArgumentException("Invalid dimension for world!");
 		this.dimensions = dimensions;
 	}
 
 	public double getWidth(){
 		return dimensions.getX();
 	}
 
 	public double getHeigth(){
 		return dimensions.getY();
 	}
 
 	public Set<Ship> getShips(){
 		return allShips;
 	}
 
 	public Set<Asteroid> getAsteroids(){
 		return allAsteroids;
 	}
 
 	public Set<Bullet> getBullets(){
 		return allBullets;
 	}
 
 	public ArrayList<SpaceObject> getSpaceObjects() {
 		ArrayList<SpaceObject> spaceobjects = new ArrayList<SpaceObject>();
 		spaceobjects.addAll(allShips);
 		spaceobjects.addAll(allAsteroids);
 		spaceobjects.addAll(allBullets);
 		return spaceobjects;
 	}
 
 	public void addShip(Ship ship){
 		ship.setWorld(this);
 		allShips.add(ship);		
 	}
 
 	public void addAsteroid(Asteroid asteroid){
 		asteroid.setWorld(this);
 		allAsteroids.add(asteroid);
 	}
 
 	public void removeShip(Ship ship){
 		ship.setWorld(null);
 		allShips.remove(ship);	
 	}
 
 	public void removeAsteroid(Asteroid asteroid){
 		asteroid.setWorld(null);
 		allAsteroids.remove(asteroid);
 	}
 
 
 	public void evolve(double dt, CollisionListener collisionListener) {
 
 		ArrayList<ArrayList<Collision>> 	collisions 	= createCollisionArray();								// Create a 2D ArrayList of Collisions.
 		double timePassed = 0;
 
 		while (collisions.get(0).get(0).getTime() <= dt)
 		{
 			Collision current = collisions.get(0).get(0);														// Current collision that will happen.
 			double time = current.getTime()-timePassed;
 			for (Ship p 	: getShips()		) 		p.move(time);
 			for (Asteroid p : getAsteroids()	) 		p.move(time);
 			for (Bullet p 	: getBullets()		) 		p.move(time);
 			timePassed =  current.getTime();
 
 			// <-- Hier moet nog thruster aanpassing komen fsoiets.
 			resolveCollision(current);																			// Execute the current collision.
 
 			if (current.getEntity2() != null)																	// Recalculate collisions with Entity2 because of adjusted velocity.
 			{
 				ArrayList<Collision> temp = createCollisionListForEntity(current.getEntity2(), timePassed);
 				int pos = positionEntity(current.getEntity1(), collisions);
 				collisions.set(pos, temp);
 				double timeEntity = collisions.get(pos).get(0).getTime();
 				// Hier kan mogelijk een bug zitten met de randwaardes van de array.
				while (pos+1 < collisions.size() && timeEntity > collisions.get(pos+1).get(0).getTime())
 					Collections.swap(collisions, pos++, pos+1);
				while (pos-1 >= 0 && timeEntity < collisions.get(pos-1).get(0).getTime())
 					Collections.swap(collisions, pos--, pos-1);
 			}
 
 			if (current.getEntity1() != null)																	// Recalculate collision with Entity1 because of adjusted velocity.
 			{
 				ArrayList<Collision> temp = createCollisionListForEntity(current.getEntity1(), timePassed);
 				int pos = 0;
 				collisions.set(pos, temp);
 				double timeEntity = collisions.get(pos).get(0).getTime();
 				while (timeEntity > collisions.get(pos+1).get(0).getTime())
 					Collections.swap(collisions, pos++, pos+1);
 
 			}
 
 		}
 
 		double time = dt-timePassed;
 		for (Ship p 	: getShips()		) 		p.move(time);
 		for (Asteroid p : getAsteroids()	) 		p.move(time);
 		for (Bullet p 	: getBullets()		) 		p.move(time);
 
 
 
 
 	}
 
 	private void resolveCollision (Collision collision) {
 
 		SpaceObject entity1 = collision.getEntity1();
 		SpaceObject entity2 = collision.getEntity2();
 
 		// Option 1: Entity1 collides with boundaries.
 		if (entity2 == null)
 		{
 			Vector newLocation = entity1.getLocation().add(entity1.getVelocity().multiply(collision.getTime()));
 			if (Util.fuzzyEquals(newLocation.getX()+entity1.getRadius(), getWidth())	||	Util.fuzzyEquals(newLocation.getX()-entity1.getRadius(), 0.0) )
 			{
 				Vector temp = entity1.getVelocity();
 				entity1.setVelocity(new Vector(-temp.getX(), temp.getY()));
 			}
 
 			else 
 			{
 				Vector temp = entity1.getVelocity();
 				entity1.setVelocity(new Vector(temp.getX(), -temp.getY()));
 			}
 
 		}
 		// Option 2: Entity1 collides with Entity2.
 		else 
 		{
 			double sigma = Math.sqrt( 	Math.pow(entity1.getRadius()*entity1.getVelocity().getX() - entity2.getRadius()*entity2.getVelocity().getX(), 2)   +
 										Math.pow(entity1.getRadius()*entity1.getVelocity().getY() - entity2.getRadius()*entity2.getVelocity().getY(), 2)
 									);
 			double J = 	(2*entity1.getMass()*entity2.getMass()  * ( (entity2.getVelocity().getNorm()-entity1.getVelocity().getNorm()) * (entity2.getRadius()-entity1.getRadius()) ) )  / 
 						( sigma*(entity1.getMass()+entity2.getMass()) );
 			double JX = (J*(entity2.getLocation().getX()-entity1.getLocation().getX()))		/		sigma;
 			double JY = (J*(entity2.getLocation().getY()-entity1.getLocation().getY()))		/		sigma;
 
 			Vector vel1 = new Vector (entity1.getVelocity().getX() + JX/entity1.getMass(), entity1.getVelocity().getY() + JY/entity1.getMass());
 			entity1.setVelocity(vel1);
 
 			Vector vel2 = new Vector (entity2.getVelocity().getX() - JX/entity2.getMass(), entity2.getVelocity().getY() - JY/entity2.getMass());
 			entity2.setVelocity(vel2);
 		}
 	}
 
 	private int positionEntity (SpaceObject object, ArrayList<ArrayList<Collision>> array) {
 		int currentPos = 0;
 		boolean found = false;
 		while (!found)
 		{
 			if (array.get(currentPos).get(0).getEntity1() == object)
 				found = true;
 			else currentPos++;
 		}
 
 		return currentPos;
 	}
 
 	private ArrayList<Collision> createCollisionListForEntity(SpaceObject object, double timePassed) {
 		ArrayList<Collision> temp = new ArrayList<Collision>();
 
 		int minTimePos = 0;
 		double minTime = object.collisionTimeWithBoundaries();
 		temp.add(new Collision(object, minTime+timePassed));
 
 		for (SpaceObject q : getSpaceObjects())
 			if (object != q)
 			{
 				double time = object.getTimeToCollision(q);
 				temp.add(new Collision(object, q, time+timePassed));
 				if (time < minTime)
 				{
 					minTime = time;
 					minTimePos = temp.size() - 1;
 				}
 			}
 		Collections.swap(temp, 0, minTimePos);
 
 
 		return temp;
 	}
 
 	private ArrayList<ArrayList<Collision>> createCollisionArray() {
 
 		ArrayList<ArrayList<Collision>> 	collisions 	= 	new ArrayList<ArrayList<Collision>>();		// 2D ArrayList with all the collisions.
 		ArrayList<SpaceObject> 				objects 		= 	getSpaceObjects();						// List of all the SpaceObjects in this World.
 
 		int minTimePos1 = 0;																			// Position of Lowest time to collision in first dimension of collisions.
 		double minTime1 = Double.POSITIVE_INFINITY;														// The time of minTimePos1
 
 		for (int i = 0; i < objects.size(); i++)
 		{
			ArrayList<Collision> temp = new ArrayList<Collision>();
 			int minTimePos2 = 0;																		// Position of Lowest time to collision in second dimension of collisions.
 			double minTime2 = objects.get(i).collisionTimeWithBoundaries();								// The time of minTimePos2
			temp.add(new Collision(objects.get(i), minTime2));											// Add collision of Object with the boundaries to 'collisions'.
 
 			for (SpaceObject q : objects)																// Add collision with each other Object to 'collisions'.
 				if (objects.get(i) != q)
 				{
 					double time = objects.get(i).getTimeToCollision(q);
					temp.add(new Collision(objects.get(i), q, time));
 					if (time < minTime2)
 					{
 						minTime2 = time;
						minTimePos2 = temp.size() - 1;
 					}
 				}
			Collections.swap(temp, 0, minTimePos2);														// Put lowest time in front of ArrayList.
			collisions.add(temp);
 			if (minTime2 < minTime1)
 			{
 				minTime1 = minTime2;
 				minTimePos1 = i;
 			}
 		}
 		Collections.swap(collisions, 0, minTimePos1);													// Put lowest time in front of ArrayList in first dimension.
 		return collisions;
 
 	}
 
 
 
 }
