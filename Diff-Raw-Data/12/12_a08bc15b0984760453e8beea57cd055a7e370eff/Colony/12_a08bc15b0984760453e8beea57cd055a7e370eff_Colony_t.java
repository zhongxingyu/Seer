 package me.paulrose.lptc.simulator;
 
 import java.util.LinkedList;
 
 import javax.vecmath.Vector2d;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 public class Colony extends Entity
 {
 
 	private static final int BASE_RADIUS = 20;
 	public static final int MAX_HEALTH = 10000;
 	public static final int ENERGY_TO_HEALTH_RATIO = 1;
 	public static final int HEALTH_INCREASE = 10;
 	
 	public static final int COLONY_DAMAGE = 25;
 	
 	public static final int MAX_ANTS = 100;
 	public static final int ANT_COST = 2000;
 	public static final int ANT_ENERGY = 5000;
 	public static final int START_ENERGY = 10000;
 	
 	public static final Color ENERGY_TOTAL_COLOR = new Color(255, 0, 0);
 	public static final Color ENERGY_LEFT_COLOR = new Color(0, 255, 0);
 
 	private String name, antClass;
 	private LinkedList<Ant> ants;
 	private int diameter, maxAnts, antCost, energy, energyMinimum, health,
 			antLimit;
 	private Image antSpriteFull, antSpriteMin;
 	private float centerX, centerY;
 	protected boolean drawEnergyCount, drawAntCount, drawOwnerName,
 			drawHealthBar, autoCreateAnts, firstRun, attacked,
 			createAntProtection;
 
 	Colony(String n, float x, float y, String c, int f, World wo)
 	{
 		super(n, x, y, BASE_RADIUS * 2, BASE_RADIUS * 2, wo);
 
 		sizeRadius = BASE_RADIUS;
 
 		name = n;
 		antClass = c;
 		energy = f;
 
 		// The size of the colony
 		// The color of the colony and its ants
 		int r = (int) (world.random.nextDouble() * 255);
 		int g = (int) (world.random.nextDouble() * 255);
 		int b = (int) (world.random.nextDouble() * 255);
 		colour = new Color(r, b, g);
 
 		createAntSprites();
 
 		redrawSprite = false;
 		drawEnergyCount = true;
 		drawAntCount = true;
 		drawOwnerName = true;
 		drawHealthBar = true;
 		autoCreateAnts = true;
 		createAntProtection = true;
 		firstRun = true;
 
 		attacked = false;
 
 		// List of all ants
 		ants = new LinkedList<Ant>();
 
 		maxAnts = MAX_ANTS;
 		antLimit = MAX_ANTS;
 		antCost = ANT_COST;
 		energy = START_ENERGY;
 
 		health = MAX_HEALTH;
 
 		centerX = bounds.getCenterX();
 		centerY = bounds.getCenterY();
 
 	}
 
 	private void createAntSprites()
 	{
 		antSpriteFull = world.antFactory.createAntSprite(colour, false);
 		antSpriteMin = world.antFactory.createAntSprite(colour, true);
 	}
 
 	public void update(int delta)
 	{
 		super.update(delta);
 		attacked = false;
 
 		if (ants.size() > 0 && health > 0)
 		{
 			// Shall we produce another ant?
 			
 			if(autoCreateAnts)
 				createAnt();
 
 			if (world.getUpdates() % 16 == 0)
 			{
 				energy -= 1;
 			}
 
 			float sizeW = BASE_RADIUS * 2 + (energy / 1000);
 			float sizeH = BASE_RADIUS * 2 + (energy / 1000);
 
 			bounds.setWidth(sizeW);
 			bounds.setHeight(sizeH);
 
 			bounds.setCenterX(centerX);
 			bounds.setCenterY(centerY);
			
			sizeRadius = (int) bounds.getWidth()/2;
 		} else
 		{
 			if (!firstRun)
 			{
 				// There are no more ants the colony is dead, turn into food!
 				world.foodFactory.create("Sugar", energy / 10, (int) centerX,
 						(int) centerY);
 				world.deleteEntity(this);
 				for (Ant a : ants)
 					world.deleteEntity(a);
 			} else
 			{
 				createAnt();
 			}
 		}
 
 		// If we have run out of energy
 		if (energy <= 0)
 		{
 			world.deleteEntity(this);
 			for (Ant a : ants)
 			{
 				a.die(this);
 				world.deleteEntity(a);
 			}
 		}
 
 		if (energy > 0 && health < MAX_HEALTH && health > 0)
 		{
 			energy -= ENERGY_TO_HEALTH_RATIO;
 			health += HEALTH_INCREASE;
 		}
 
 	}
 
 	public void draw(Graphics g)
 	{
 
 		float x = bounds.getX();
 		float y = bounds.getY();
 		float w = bounds.getWidth();
 		float h = bounds.getHeight();
 		float hw = w / 2;
 		float hh = h / 2;
 
 		if (g.getWorldClip().contains(x, y))
 		{
 			g.setColor(colour);
 			g.fillOval(x, y, w, h);
 
 			if (drawEnergyCount)
 			{
 				String amount = "" + energy;
 				g.setColor(Color.black);
 				g.drawString(amount,
 						bounds.getCenterX() - ((8 * amount.length()) / 2),
 						bounds.getY() + bounds.getHeight() + 10);
 			}
 
 			if (drawAntCount)
 			{
 				String antsCount = "" + ants.size();
 				g.setColor(Color.black);
 				g.drawString(antsCount,
 						bounds.getCenterX() - ((9 * antsCount.length()) / 2),
 						bounds.getCenterY() - 6);
 			}
 
 			if (drawOwnerName)
 			{
 				g.setColor(Color.black);
 				g.drawString(antClass,
 						bounds.getCenterX() - ((9 * antClass.length()) / 2),
 						bounds.getCenterY() - 35);
 			}
 
 			if (drawHealthBar)
 			{
 				// draw total energy bar
 				g.setColor(ENERGY_TOTAL_COLOR);
 				g.fillRect(bounds.getCenterX() - bounds.getWidth() / 2,
 						bounds.getCenterY() + bounds.getHeight() / 2 + 5,
 						bounds.getHeight(), 2);
 
 				float healthBarPercent = (float) bounds.getWidth() / MAX_HEALTH
 						* health;
 
 				g.setColor(ENERGY_LEFT_COLOR);
 				g.fillRect(bounds.getCenterX() - bounds.getWidth() / 2,
 						bounds.getCenterY() + bounds.getHeight() / 2 + 5,
 						healthBarPercent, 2);
 
 				g.setColor(Color.black);
 				g.drawString("" + health, bounds.getX(), bounds.getY() - 25);
 			}
 		}
 
 		super.draw(g);
 
 	}
 
 	public void removeAnt(Ant a)
 	{
 		// Check if the ant is dead first
 		// Malicious players could be abusing this
 		ants.remove(a);
 		world.removeEntity(a);
 	}
 
 	public Color getColor()
 	{
 		return colour;
 	}
 
 	public void setColor(int r, int g, int b)
 	{
 		colour = new Color(r, g, b);
 		antSpriteFull = world.antFactory.createAntSprite(colour, false);
 		antSpriteMin = world.antFactory.createAntSprite(colour, true);
 	}
 
 	public Image getAntSprite()
 	{
 		if (world.isRotationEnabled())
 			return antSpriteMin;
 		else
 			return antSpriteFull;
 
 	}
 
 	public void addFood(Food f)
 	{
 		energy += (int) f.getTotalFood();
 		f.delete();
 	}
 
 	public void setMinimumEnergy(int i)
 	{
 		energyMinimum = i;
 	}
 
 	public int feed(int a)
 	{
 		if (energy > a)
 		{
 			energy -= a;
 			return a * 20;
 		} else
 		{
 			return 0;
 		}
 	}
 
 	public void takeDamage()
 	{
 		attacked = true;
 		health -= COLONY_DAMAGE;
 	}
 
 	public int antCount()
 	{
 		return ants.size();
 	}
 
 	public int getEnergy()
 	{
 		return energy;
 	}
 	
 	public boolean isUnderAttack()
 	{
 		return attacked;
 	}
 	public boolean isAutoCreateAntsOn()
 	{
 		return autoCreateAnts;
 	}
 
 	public void setAutoCreateAnts(boolean b)
 	{
 		autoCreateAnts = b;
 	}
 	
 	public void setCreationProtection(boolean b)
 	{
 		createAntProtection = b;
 	}
 	
 	public boolean isCreationProtection()
 	{
 		return createAntProtection;
 	}
 
 	public boolean isFirstRun()
 	{
 		return firstRun;
 	}
 
 	public void firstRun()
 	{
 		firstRun = false;
 	}
 
 	public void setAntLimit(int i)
 	{
 		antLimit = i - 1;
 
 	}
 
 	public void createAnt()
 	{
 		if (ants.size() <= maxAnts 
 			&& ants.size() <= antLimit)
 		{
 			if (createAntProtection 
 				&& (energy - antCost) >= antCost
 				&& (energy - antCost) > energyMinimum)
 			{
 				ants.add(world.antFactory.createAnt(antClass,
 						bounds.getCenterX(), bounds.getCenterY(), this));
 				energy -= antCost;
 				
 			} else if (!createAntProtection)
 			{
 				ants.add(world.antFactory.createAnt(antClass,
 						bounds.getCenterX(), bounds.getCenterY(), this));
 				energy -= antCost;
 			}
 
 		}
 	}
 
 	//TODO checking of energy bounds and disable users from being to edit the values of sensitive variables.
 	public void resetAttack(Entity e)
 	{
 		if (e instanceof Ant && ((Ant) e).colony == this)
 		{
 			attacked = false;
 		}
 	}
 }
