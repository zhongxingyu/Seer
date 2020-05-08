 package com.banished.core.entities.enemies;
 
 import com.banished.core.Direction;
 import com.banished.core.Location;
 import com.banished.core.Tile;
 import com.banished.core.World;
 import com.banished.core.entities.EntityImageSet;
 import com.banished.core.entities.Player;
 import com.banished.graphics.Color;
 import com.banished.graphics.Image;
 
 public class SlimeEntity extends EnemyEntity
 {
 	private static final double SLIME_MAX_HEALTH = 20;
 	private static final double SLIME_RANGE = 1;
 	private static final double SLIME_STRENGTH = 5;
 	private static final double SLIME_DEFENSE = 4;
 	private static final double SLIME_SPEED = .5;
 	private static final double SLIME_SIGHT_RANGE = 3.5;
 	private static final double SLIME_TIME_TO_ATTACK = 1;
 	private static final double SLIME_STABILITY = .25;
 	private static final double SLIME_CRIT_RATE = .1;
 	private static final double SLIME_CRIT_MULT = 1.1;
 	private static final double SLIME_STURDINESS = 0;
 	private static final int SLIME_EXP = 3;
 	
 	private Color COLOR;
 	private Color color;
 
 	private static final float IMG_W = 52, IMG_H = 22;
 	public SlimeEntity(Location location, World world, Color color, double mult)
 	{
 		// image: 52x22 px
		super(location, world, IMG_W/Tile.SIZE, IMG_H/Tile.SIZE, -IMG_H/Tile.SIZE/2, IMG_W/Tile.SIZE/2, IMG_H/2/Tile.SIZE,
 				SLIME_MAX_HEALTH*mult, SLIME_STRENGTH*mult, SLIME_DEFENSE*Math.sqrt(mult), SLIME_SPEED*Math.pow(mult, 1/3), SLIME_RANGE, 
 				SLIME_TIME_TO_ATTACK, SLIME_SIGHT_RANGE, SLIME_EXP*(int)mult, 5, 25);
 		
 		images = new EntityImageSet(new Image[]
 		{ 
 			Image.fromFile("entities/living_entities/slime/0.png"),
 			Image.fromFile("entities/living_entities/slime/1.png"),
 			Image.fromFile("entities/living_entities/slime/2.png")
 		}, .2);
 		
 		COLOR = color;
 		this.color = color;
 		
 	}
 //	public SlimeEntity(SlimeEntity toCopy)
 //	{
 //		super(toCopy);
 //		this.color = new Color(toCopy.color);
 //	}
 
 	public void update(double frameTime)
 	{
 		super.update(frameTime);
 //		System.out.println("Speed: " + this.speed);
 		images.update(frameTime);
 	}
 
 	public Image getImage()
 	{
 		return images.get(Direction.North);
 	}
 	
 	public Color getSlimeColor()
 	{
 		return this.color;
 	}
 
 	@Override
 	public void attackPlayer()
 	{
 		if (Player.get() != null)
 		{
 			if (Player.get().getLocation().distanceTo(this.getLocation()) > this.RANGE)
 				return;
 			this.attack(Player.get());
 		}
 	}
 	protected void calcDamageSets()
 	{ 
 		this.MAX_DAMAGE = SLIME_STRENGTH;
 		this.STABILITY = SLIME_STABILITY;
 		this.CRIT_RATE = SLIME_CRIT_RATE;
 		this.CRIT_MULT = SLIME_CRIT_MULT;
 	}
 	
 	protected void calcDefenseSets()
 	{
 		this.DEFENSE = SLIME_DEFENSE;
 		this.STURDINESS = SLIME_STURDINESS;
 	}
 	
 	public void onDeath()
 	{
 //		System.out.println("Poor slime! It died...");
 		Player.get().incrementExperience(EXP);
 		this.getWorld().addEntityToDie(this);
 	}
 
 	protected Location getTargetDirection() 
 	{
 		Player player = getPlayerAsTarget();
 		if (player != null)
 		{
 			Location playerLoc = player.getLocation();
 			return new Location(playerLoc.getX() - this.getLocation().getX(),
 					playerLoc.getY() - this.getLocation().getY());
 		}
 		
 		return null;
 	}
 	
 	private Player getPlayerAsTarget()
 	{
 		Player player = Player.get();
 		if (player == null) return null;
 		if (player.getLocation().distanceTo(this.getLocation()) > SLIME_SIGHT_RANGE)
 			return null;
 		
 		return player;
 	}
 	
 	public void flashImage()
 	{
 		color = Color.Yellow;
 	}
 	
 	public void revertImage()
 	{
 		color = COLOR;
 	}
 	
 	public String toString(){ return "SLIME";}
 	
 	public String toText()
 	{
 		return super.toText() + " color=" + color;
 	}
 }
