 package me.paulrose.lptc.simulator;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.geom.Rectangle;
 
 public abstract class Ant extends MoveableEntity
 {
 	
 	public static final int BODY_SIZE = 10;
 	public static final int VIEW_RADIUS = 25;  
 	
 	public static final float WALK_VELOCITY = 0.1f;
 	public static final float RUN_VELOCITY = 0.14f;
 	
 	public static final int CARRY_CAPACITY = 100;
 	public static final int EAT_SPEED = 2;
 	
 	public static final int WALK_COST = 1;
 	public static final int RUN_COST = 3;
 	public static final int CARRY_COST = 3;
 	public static final int ANT_ATTACK_COST = 15;
 	public static final int COLONY_ATTACK_COST = 10;
 	
 	public static final int ATTACK_DAMAGE = 25;
 	
 	public static final int MAX_ENERGY = 5000;
 	
 	public static final Color ATTACKED_COLOR = new Color(182,45,23);
 	public static final Color ENERGY_TOTAL_COLOR = new Color(255,0,0);
 	public static final Color ENERGY_LEFT_COLOR = new Color(0,255,0);
 	
 	private int processOnTick;
 	
 	private Colony colony, enemyColony;
 	
 	private int energy, maxCarryWeight;
 	
 	protected boolean foundFood, atFood, atColony, atEnemyAnt, atFriendlyAnt, 
 		attacked, fullEnergy, halfEnergy, eating, friendlyAntAttacked, 
 		friendlyAntFoundFood, nearEnemyColony, nearHomeColony, drawViewRadius, 
 		drawCollisions, atEnemyColony , drawEnergyBar;
 	
 	protected Ant nearestAnt, nearestFriendlyAnt, nearestEnemyAnt,  attackerAnt, 
 		friendlyAttacked, friendlyWithFood;
 	
 	protected Food nearestFood, biggestFood, lastFoodPile;
 
 	protected float nearestFoodDistance, biggestFoodAmount;
 	
 	public Ant(String n, float x, float y, Colony c, World wo)
 	{
 		super(n, x, y, VIEW_RADIUS * 2, VIEW_RADIUS * 2, wo);
 
 		// Starting values for the ant
 		colony = c;
 		energy = MAX_ENERGY;
 		maxCarryWeight = 100;
 		sizeRadius = BODY_SIZE / 2;
 
 		drawViewRadius = false;
 		drawCollisions = false;
 		drawEnergyBar = true;
 		
 		processOnTick = world.random.nextInt(4);
 		setRotation(world.random.nextInt(360 + 1));
 	}
 
 	public int getColonyID()
 	{
 		return colony.ID;
 	}
 	
 	public void runLogic()
 	{
 		// User code will go here in the end
 	}
 	
 	public void update(int delta)
 	{
 		super.update(delta);
 		
 		if(world.getTickNumber() == processOnTick
 		|| colony.isFirstRun())
 		{
 	
 			// reset ant details
 			collisions.clear();
 			
 			friendlyAntAttacked = false;
 			friendlyAttacked = null;
 			friendlyAntFoundFood = false;
 			friendlyWithFood = null;
 			
 			nearEnemyColony = false;
 			nearHomeColony = false;
 			
 			nearestAnt = null;
 			nearestFriendlyAnt = null;
 			nearestEnemyAnt = null;
 			
 			atEnemyColony = false;
 			atFriendlyAnt = false;
 			atEnemyAnt = false;
 			atEnemyColony = false;
 			atColony = false;
 			atFood = false;
 			
 			enemyColony = null;
 			
 			// Check if we have full energy before this loops deductions
 			fullEnergy = (energy == MAX_ENERGY);
 			halfEnergy = (energy >= MAX_ENERGY / 2);
 	
 			// Find the nearest food to us
 			nearestFoodDistance = Ant.VIEW_RADIUS;
 			float nearestAntDistance = Ant.VIEW_RADIUS;
 			float nearestColonyDistance = Ant.VIEW_RADIUS;
 	
 			for (Entity e : viewCollisions)
 			{
 				// First see what is around us
 				// is there food on the floor
 				if (e instanceof Food && !e.isBeingCarried())
 				{
 					Food f = (Food) e;
 					// Find nearest food
 					float distance = distanceTo(e);
 					if (distance < nearestFoodDistance)
 					{
 						nearestFood = f;
 						nearestFoodDistance = distance;
 					}
 	
 					// Find biggest food
 					if (f.getTotalFood() > biggestFoodAmount)
 					{
 						biggestFood = f;
 						biggestFoodAmount = f.getTotalFood();
 					}
 				}
 				// is there another ant about?
 				if(e instanceof Ant)
 				{
 					Ant a = (Ant)e;
 					
 					float distance = distanceTo(e);
 					if(distance < nearestAntDistance)
 					{
 						nearestAnt = a;
 						nearestAntDistance  = distance;
 						
 						if(a.colony == colony)
 							nearestFriendlyAnt = a;
 						else
 							nearestEnemyAnt = a;
 					}
 					
 					// is it a friendly ant?
 					if(a.colony == colony)
 					{
 						// is it under attack?
 						if (a.attacked)
 						{
 							friendlyAntAttacked = true;
 							friendlyAttacked = a;
 						}
 						
 						if(a.foundFood)
 						{
 							friendlyAntFoundFood = true;
 							friendlyWithFood = a;
 						}
 						
 					}
 					
 					// is there a colony about?
 					if(e instanceof Colony)
 					{
 						//is it our colony?
 						Colony c = (Colony) e;
 						nearestColonyDistance = distanceTo(e);
 						
 						if(c == colony)
 							nearHomeColony = true;
 						else
 						{
 							nearEnemyColony = true;
 							enemyColony = c;
 						}
 						
 					}
 					
 				}
 				
 				// Check to see if any objets were near us
 				// reset refereces if there were none
 				if (nearestFoodDistance >= Ant.VIEW_RADIUS)
 				{
 					nearestFood = null;
 					atFood = false;
 				}
 
 				if(nearestAntDistance >= Ant.VIEW_RADIUS)
 				{
 					nearestAnt = null;
 					nearestFriendlyAnt = null;
 					nearestEnemyAnt = null;
 					atFriendlyAnt = false;
 					atEnemyAnt = false;
 				}
 				
 				// Second make finer detections to see if we are touching
 				// anything
 	
 				float a = sizeRadius + e.sizeRadius;
 				float dx = bounds.getCenterX() - e.bounds.getCenterX();
 				float dy = bounds.getCenterY() - e.bounds.getCenterY();
 				boolean isCollided = (a * a) > (dx * dx + dy * dy);
 	
 				if (isCollided)
 					collisions.add(e);
 	
 			}
 	
 			// Check the collisions list for flags we can activate
 			for (Entity e : collisions)
 			{
 				if (e instanceof Food && !e.isBeingCarried())
 					atFood = true;
 	
 				if (e instanceof Colony)
 					if ((Colony) e == colony)
 						atColony = true;
 					else
 					{
 						atEnemyColony = true;
 						enemyColony  = (Colony) e;
 					}
 				
 				if (e instanceof Ant)
 				{
 					if(((Ant) e).colony == colony)
 						atFriendlyAnt = true;
 					else
 						atEnemyAnt = true;
 				}
 			}
 	
 			// Check to see if any objets were near us
 			// reset refereces if there were none
 			if (nearestFoodDistance == Ant.VIEW_RADIUS)
 			{
 				nearestFood = null;
 				atFood = false;
 			}
 			
 			// Run user code here
 			runLogic();
 			
 			if(colony.isFirstRun())
 				 colony.firstRun();
 		}
 		
 		// Update all other important things
 		// Deduct energy for various continuous states
 		if(lastFoodPile == null || !lastFoodPile.exists())
 			lastFoodPile = null;
 		
 		if(!stopped)
 			energy -= WALK_COST;
 		
 		if(isCarrying)
 			energy -= CARRY_COST;
 		
 		//Check energy reserves and kill this ant if need be
 		if(energy < 0)
 		{
 			die(this);
 		}
 	}
 	
 	public Food lastFoodPile()
 	{
 		return lastFoodPile;
 	}
 	
 	public boolean remembersLastFoodPile()
 	{
 		return (lastFoodPile != null);
 	}
 	
 	public void die(Entity e)
 	{
 		if((e instanceof Colony && (Colony)e == colony)
 		|| (e instanceof Ant && (Ant)e == this))
 		{
 			
 			//Remove the and from the colony, the colony will remove it from the world
 			colony.removeAnt(this);
 			// drop whatever we are holding
 			drop();
 		}
 		
 	}
 
 	public void draw(Graphics g)
 	{
 
 		super.draw(g);
 
 		float x = bounds.getCenterX();
 		float y = bounds.getCenterY();
 		float w = bounds.getWidth();
 		float h = bounds.getHeight();
 		float hw = w / 2;
 		float hh = h / 2;
 
 		Image sprite = colony.getAntSprite();
 
 		if (world.isRotationEnabled() && g.getWorldClip().contains(x, y))
 		{
 			sprite.setRotation((float) Math.toDegrees(rotation) - 90);
 			g.drawImage(sprite, x - BODY_SIZE / 2, y - BODY_SIZE / 2);
 
 			// Viewing Circle of the Ant
 			if (drawViewRadius)
 			{
 				g.setLineWidth(1.0f);
 				g.setColor(Color.magenta);
 				g.drawOval(bounds.getX(), bounds.getY(), VIEW_RADIUS * 2,
 						VIEW_RADIUS * 2);
 			}
 
 			if (drawCollisions)
 			{
 				g.setColor(Color.blue);
 				for (Entity e : viewCollisions)
 				{
 					g.drawLine(bounds.getCenterX(), bounds.getCenterY(),
 							e.bounds.getCenterX(), e.bounds.getCenterY());
 				}
 
 			}
 
 			// Draw what the ant is carrying
 			if (isCarrying)
 			{
 				g.setColor(carrying.getColour());
 				g.drawOval(bounds.getCenterX() - 3, bounds.getCenterY() - 3, 7,
 						7);
 			}
 			
 			// Draw the ants health bar
 			if(drawEnergyBar)
 			{
 				// draw total energy bar
 				g.setColor(ENERGY_TOTAL_COLOR);
 				g.fillRect(bounds.getCenterX()-VIEW_RADIUS/2, bounds.getCenterY()+10, 
 						VIEW_RADIUS, 2);
 				
 				
 				float energyBarPercent = (float)VIEW_RADIUS / MAX_ENERGY * energy;
 				
 				g.setColor(ENERGY_LEFT_COLOR);
 				g.fillRect(bounds.getCenterX()-VIEW_RADIUS/2, bounds.getCenterY()+10, 
 						 energyBarPercent, 2);
 			}
 			
 			if(attacked)
 			{
 				g.setColor(ATTACKED_COLOR);
 				g.drawString("!", bounds.getCenterX()-2, bounds.getCenterY()-10);
 				
 			}
 
 		} else if (world.drawClip.contains(x, y))
 		{
 			g.drawImage(sprite, x - BODY_SIZE / 2, y - BODY_SIZE / 2);
 		}
 
 		// Draw the viewing circle of the ant
 
 	}
 
 	public void randomDirection()
 	{
 		dest.x = world.random.nextDouble() * world.getWidth();
 		dest.y = world.random.nextDouble() * world.getHeight();
 	}
 
 	public void harvest(Food f)
 	{
 		if (	
 			!isCarrying 
 			&& !f.isBeingCarried() 
 			&& distanceTo(f) <= sizeRadius
 			&& nearestFood != null
 		)
 		{
 			carrying = f.harvest(CARRY_CAPACITY);
 			isCarrying = true;
 			carrying.setBeingCarried(true);
 			lastFoodPile = f;
 		}
 
 	}
 	
 	public void harvestFood()
 	{
 		if (
 			!isCarrying 
 			&& distanceTo(nearestFood) <= sizeRadius
 			&& nearestFood != null
 		)
 		{
 			carrying = nearestFood.harvest(CARRY_CAPACITY);
 			isCarrying = true;
 			carrying.setBeingCarried(true);
 			lastFoodPile = nearestFood;
 		}
 		
 	}
 
 	public void print(String msg)
 	{
 		world.userMessage(msg);
 	}
 
 	public boolean isCarrying()
 	{
 		return (isCarrying);
 	}
 
 	public boolean isNotCarrying()
 	{
 		return (!isCarrying);
 	}
 
 	public boolean isCarryingFood()
 	{
 		if (isCarrying && carrying instanceof Food)
 			return true;
 		else
 			return false;
 	}
 
 	public boolean isCarryingAnt()
 	{
 		return (isCarrying && carrying instanceof Ant);
 
 	}
 
 	public boolean nearFood()
 	{
 		return (nearestFood != null);
 	}
 	
 	public boolean nearAnt()
 	{
 		return (nearestAnt != null);
 	}
 	
 	public boolean nearFriendlyAnt()
 	{
 		return (nearestFriendlyAnt != null);
 	}
 	
 	public boolean nearEnemyAnt()
 	{
 		return (nearestEnemyAnt != null);
 	}
 	
 	public boolean atFood()
 	{
 		return (atFood);
 	}
 	
 	public boolean atFriendlyAnt()
 	{
 		return (atFriendlyAnt);
 	}
 	
 	public boolean atEnemyAnt()
 	{
 		return (atEnemyAnt);
 	}
 	
 	
 	public boolean atColony()
 	{
 		return (atColony);
 	}
 	
 	public void drop()
 	{
 		if(isCarrying)
 		{
 			carrying.dropped();
 			carrying = null;
 		}
 	}
 
 	public void dropOffFood()
 	{
 		if(isCarrying && atColony)
 		{
 			colony.addFood((Food) carrying); 	
 			carrying = null;
 			walkToActive = false;
 			isCarrying = false;
 		}
 	}
 
 	public void attackAnt(Ant a)
 	{
 		if(a != null && distanceTo(a) <= sizeRadius)
 		{
 			a.takeDamage();
 			energy -= ANT_ATTACK_COST;
 		}
 	}
 	
 	public void attackColony()
 	{
 		if(enemyColony != null && distanceTo(enemyColony) <= sizeRadius)
 		{
 			enemyColony.takeDamage();
 			energy -= COLONY_ATTACK_COST;
 		}
 		
 	}
 	
 	public void takeDamage()
 	{
 		attacked = true;
 		energy -= ATTACK_DAMAGE;
 	}
 	
 	public boolean beingAttacked()
 	{
 		return (attacked);
 	}	
 	
 	public void resetAttack()
 	{
 		attacked = false;
 	}
 	
 	public boolean isEating()
 	{
 		return (eating);
 	}
 	
 	public void stopEating()
 	{
 		eating = false;
 	}
 	
 	public void stop()
 	{
 		stopped = true;
 	}
 	
 	public void walk()
 	{
 		setMaxVelocity(WALK_VELOCITY);
 		stopped = false;
 	}
 	
 	public void run()
 	{
 		setMaxVelocity(RUN_VELOCITY);
 		stopped = false;
 	}
 	
 	public void eatFood(Food f)
 	{
		if(distanceTo(f) < sizeRadius)
 		{
 			energy += f.eat(f.getTotalFood());
 			f.setBeingCarried(false);
 			carrying = null;
 			isCarrying = false;
 		}
 	}
 	
 	
 	public void eatCarriedFood()
 	{
		if(isCarrying && carrying instanceof Food)
 		{
 			Food f = (Food) carrying;
 			energy += f.eat(f.getTotalFood());
 			f.setBeingCarried(false);
 			carrying = null;
 			isCarrying = false;
 		}
 	}
 	
 	public void eatAtColony()
 	{
		if(atColony)
 		{
 			eating = true;
 			energy += colony.feed(EAT_SPEED);
 		}
 	}
 	
 	public boolean atEnemyColony()
 	{
 		return (atEnemyColony);
 	}
 	
 	public boolean nearEnemyColony()
 	{
 		return nearEnemyColony;
 	}
 	
 	public void resetColonyAttackFlag()
 	{
 		colony.resetAttack(this);
 	}
 
 	public int getEnergy()
 	{
 		return energy;
 	}
 
 }
