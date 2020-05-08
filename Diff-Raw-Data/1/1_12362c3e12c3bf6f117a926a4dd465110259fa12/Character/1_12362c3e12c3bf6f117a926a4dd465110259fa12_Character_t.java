 /**
  * Class to handle everything associated with Student, Staff, and Couple
  *
  * @author	Jamie of the Javateerz
  */
 
 package org.javateerz.ParkViewProtector;
 
 import java.util.ArrayList;
 
 import org.newdawn.slick.geom.Rectangle;
 
 public abstract class Character extends Movable
 {
 	private int hpRegenRate					= 0;
 	private static final int HP_REGEN		= 1;
 	private static final int HP_REGEN_RATE	= 80;
 	
 	protected int hp;
 	protected int maxHp;
 	
 	protected int invulFrames	= 0;
 	protected int stunFrames	= 0;
 	protected int attackFrames	= 0;
 	protected int againFrames	= 0;
 	
 	protected boolean invul		= false;
 	protected boolean stunned	= false;
 	protected boolean attacking	= false;
 	protected boolean pushing	= false;
 	protected boolean again		= false;
 	protected Character pushee	= null;
 	
 	/*protected GraphicBar bar	= new GraphicBar(game, 
 			"red", (int)(sprite.getWidth()), (double)getHp()/getMaxHp());*/
 	protected Bar bar			= new Bar(ParkViewProtector.STATS_BAR_HP,
 			(int) sprite.getWidth(), (int) sprite.getWidth());
 	
 	protected ArrayList<StatusEffect> effects=new ArrayList<StatusEffect>();
 	
 	public ItemBin bin;
 	
 	private static final long serialVersionUID = 4L;
 	
 	/**
 	 * Constructor
 	 * 
 	 * @param g Instance of Game
 	 * @param x
 	 * @param y
 	 * @param hp
 	 * @param maxHp	
 	 * @param speed
 	 */
 	public Character(Game g, double x, double y, int hp, int maxHp, double speed)
 	{
 		super(g, x, y, speed);
 		
 		this.hp		= hp;
 		this.maxHp	= maxHp;
 		bin = new ItemBin(this);
 	}
 	
 	/**
 	 * @return The HP of the character
 	 */
 	public int getHp()
 	{
 		return hp;
 	}
 	
 	/**
 	 * Sets HP of the character
 	 * 
 	 * @param amount
 	 */
 	public void setHp(int amount)
 	{
 		hp=amount;
 	}
 	
 	/**
 	 * Decrements the amount of frames for all status effects the character has
 	 */
 	public void recover()
 	{
 		if(invulFrames>0)
 			invulFrames--;
 		if(stunFrames>0)
 			stunFrames--;
 		if(attackFrames>0)
 			attackFrames--;
 		if(againFrames>0)
 			againFrames--;
 		
 		if(invulFrames<=0)
 			invul=false;
 		if(stunFrames<=0)
 			stunned=false;
 		if(attackFrames<=0)
 			attacking=false;
 		if(againFrames<=0)
 			again=false;
 	}
 	
 	/**
 	 * Recover HP
 	 */
 	public void regain()
 	{
 		hpRegenRate++;
 		if(hpRegenRate>=HP_REGEN_RATE)
 		{
 			hpRegenRate=0;
 			if(getHp() < getMaxHp())
 				adjustHp(HP_REGEN);
 		}
 	}
 	
 	/**
 	 * Sets time for character to be invulnerable
 	 * 
 	 * @param amount
 	 */
 	public void setInvulFrames(int amount)
 	{
 		invulFrames=ParkViewProtector.secsToFrames(amount) + 1;
 		if(amount>0)
 			invul=true;
 	}
 	
 	/**
 	 * Sets time for character to be stunned
 	 * 
 	 * @param amount
 	 */
 	public void setStunFrames(int amount)
 	{
 		stunFrames=ParkViewProtector.secsToFrames(amount);
 		if(amount>0)
 			stunned=true;
 	}
 	
 	/**
 	 * Sets time for character to be attacking
 	 * 
 	 * @param amount
 	 */
 	public void setAttackFrames(int amount)
 	{
 		attackFrames=ParkViewProtector.secsToFrames(amount);
 		if(amount>0)
 			attacking=true;
 	}
 	
 	/**
 	 * Sets time for the character to attack again
 	 * @param amount
 	 */
 	public void setAgainFrames(int amount)
 	{
 		againFrames=ParkViewProtector.secsToFrames(amount);
 		if(amount>0)
 			again=true;
 	}
 	
 	/**
 	 * @return The number of stunned frames of this character
 	 */
 	public int getStunFrames()
 	{
 		return stunFrames;
 	}
 
 	/**
 	 * @return The number of attacking frames of this character
 	 */
 	public int getAttackFrames()
 	{
 		return attackFrames;
 	}
 
 	/**
 	 * @return The number of invulnerable frames of this character
 	 */
 	public int getInvulFrames()
 	{
 		return invulFrames;
 	}
 
 	/**
 	 * @return The number of frames before the player can attack again
 	 */
 	public int getAgainFrames()
 	{
 		return againFrames;
 	}
 	
 	/**
 	 * @return If the character is vulnerable
 	 */
 	public boolean isVulnerable()
 	{
 		return !invul;
 	}
 	
 	/**
 	 * @return If the character is stunned
 	 */
 	public boolean isStunned()
 	{
 		return stunned;
 	}
 	
 	/**
 	 * @return If the character is attacking
 	 */
 	public boolean isAttacking()
 	{
 		return attacking;
 	}
 
 	/**
 	 * @return If the character is able to attack again
 	 */
 	public boolean isAgain()
 	{
 		return !again;
 	}
 	
 	/**
 	 * @return If the character is pushing
 	 */
 	public boolean isPushing()
 	{
 		return pushing;
 	}
 	
 	/**
 	 * @return The other character that is being pushed
 	 */
 	public Character getPushee()
 	{
 		return pushee;
 	}
 	
 	public int statusIndex(String name)
 	{
 		for(int i=0; i<effects.size(); i++)
 		{
 			if(effects.get(i).getName().equals(name))
 			{
 				return i;
 			}
 		}
 		return -1;
 	}
 	
 	/**
 	 * Decreases the HP by the specified amount
 	 * 
 	 * @param amount Amount of HP to subtract
 	 * @return New amount of HP
 	 */
 	public int adjustHp(int amount)
 	{
 		hp		   += amount;
 		if(hp > maxHp)
 		{
 			hp = maxHp;
 		}
 		
 		return hp;
 	}
 	
 	/**
 	 * @return The maximum HP the character can have
 	 */
 	public int getMaxHp()
 	{
 		return maxHp;
 	}
 	
 	/**
 	 * Drops the entire inventory
 	 */
 	public void dropInv()
 	{
 		bin.dropInv();
 		//code to add the items to the field
 	}
 	
 	/**
 	 * Adds an item to the character's inventory
 	 * 
 	 * @param item Item to add
 	 */
 	public void pickItem(Item item)
 	{
 		bin.add(item);
 		//code to remove from field...
 	}
 	
 	/**
 	 * Runs an item on the character used on, then removes it from the inventory
 	 * 
 	 * @param type Char
 	 */
 	public void useItem(char type)
 	{
 		bin.useItem(type);
 	}
 
 	/**
 	 * Causes character to push other character away
 	 * 
 	 * @param other Character to be pushed away 
 	 */
 	/* FIXME: Yeah, I have no idea how to fix this.
 	 * 			IT's BROKEN!
 	 * 
 	 * 		Does not work when an object is colliding with 2 players! D:
 	 */
 	public void push(Character c)
 	{
 		pushing = true;
 		double	temp1=getSpeed(),
 				temp2=c.getSpeed();
 		
 		switch(getDirection())
 		{
 			case Direction.NORTH:
 				if(c.canMove(c.getNewBounds(0,-1)))
 				{
 					if(c.isPushing() && c.getPushee()==this)
 					{
 						if(getSpeed()>c.getSpeed())
 						{
 							setSpeed(getSpeed()-c.getSpeed());
 							c.setSpeed(getSpeed());
 							move(0,-1);
 							c.move(0,-1,0);
 						}
 					}
 					else if(!c.isPushing())
 					{
 						move(0,-1);
 						c.setSpeed(getSpeed());
 						c.move(0,-1,0);
 					}
 				}
 				break;
 			case Direction.SOUTH:
 				if(c.canMove(c.getNewBounds(0,1)))
 				{
 					if(c.isPushing() && c.getPushee()==this)
 					{
 						if(getSpeed()>c.getSpeed())
 						{
 							setSpeed(getSpeed()-c.getSpeed());
 							c.setSpeed(getSpeed());
 							move(0,1);
 							c.move(0,1,0);
 						}
 					}
 					else if(!c.isPushing())
 					{
 						move(0,1);
 						c.setSpeed(getSpeed());
 						c.move(0,1,0);
 					}
 				}
 				break;
 			case Direction.EAST:
 				if(c.canMove(c.getNewBounds(1,0)))
 				{
 					if(c.isPushing() && c.getPushee()==this)
 					{
 						if(getSpeed()>c.getSpeed())
 						{
 							setSpeed(getSpeed()-c.getSpeed());
 							c.setSpeed(getSpeed());
 							move(1,0);
 							c.move(1,0,0);
 						}
 					}
 					else if(!c.isPushing())
 					{
 						move(1,0);
 						c.setSpeed(getSpeed());
 						c.move(1,0,0);
 					}
 				}
 				break;
 			case Direction.WEST:
 				if(c.canMove(c.getNewBounds(-1,0)))
 				{
 					if(c.isPushing() && c.getPushee()==this)
 					{
 						if(getSpeed()>c.getSpeed())
 						{
 							setSpeed(getSpeed()-c.getSpeed());
 							c.setSpeed(getSpeed());
 							move(-1,0);
 							c.move(-1,0,0);
 						}
 					}
 					else if(!c.isPushing())
 					{
 						move(-1,0);
 						c.setSpeed(getSpeed());
 						c.move(-1,0,0);
 					}
 				}
 				break;
 		}
 		setSpeed(temp1);
 		c.setSpeed(temp2);
 	}
 
 	
 	/**
 	 * Random movement
 	 * 
 	 * @param Movable Object to move
 	 */
 	public void moveRandom()
 	{
 		int speed					= Game.MOVE_SPEED;
 		int changeMoves				= (int) (Math.random() * (Game.MAX_NUM_MOVES - Game.MIN_NUM_MOVES) +
 				Game.MIN_NUM_MOVES + 1);
		changeMoves				   /= ParkViewProtector.getRenderDeltas();
 		
 		// change direction if the move count exceeds the number of moves to change after
 		if(getMoveCount() <= 0 || getMoveCount() > changeMoves)
 		{
 			// choose a new direction
 			setDirection((int) (Math.random() * 4));
 			resetMoveCount();
 		}
 		
 		// change direction if we hit the top or bottom
 		if(getBounds().getY() <= 0 && getDirection() == Direction.NORTH)
 		{
 			while(getDirection()==Direction.NORTH)
 				setDirection((int) (Math.random() * 4));
 			resetMoveCount();
 		}
 		else if(getBounds().getY() >= ParkViewProtector.HEIGHT - getBounds().getHeight()  &&
 				getDirection() == Direction.SOUTH)
 		{
 			while(getDirection()==Direction.SOUTH)
 				setDirection((int) (Math.random() * 4));
 			resetMoveCount();
 		}
 		else if(getBounds().getX() <= 0 && getDirection() == Direction.WEST)
 		{
 			while(getDirection()==Direction.WEST)
 				setDirection((int) (Math.random() * 4));
 			resetMoveCount();
 		}
 		else if(getBounds().getX() >= ParkViewProtector.WIDTH - getBounds().getWidth() &&
 				getDirection() == Direction.EAST)
 		{
 			while(getDirection()==Direction.EAST)
 				setDirection((int) (Math.random() * 4));
 			resetMoveCount();
 		}
 		
 		// check for collisions
 		if(getNewBounds(speed).intersects(game.getPlayer().getBounds())
 				|| !this.canMove(getNewBounds(speed)))
 		{
 			// collision, must choose new direction
 			
 			if(this instanceof Character && getNewBounds(speed).intersects(game.getPlayer().getBounds()))
 			{
 				this.push(game.getPlayer());
 				incrementMoveCount();
 			}
 			else
 				resetMoveCount();
 		}
 		
 		else
 		{
 			move(speed);
 		}
 	}
 	
 	/**
 	 * @return The bounding box of the character
 	 */
 	public Rectangle getBounds()
 	{
 		Rectangle rect = new Rectangle((int) x, (int) y+(sprite.getHeight()-sprite.getWidth())/2,
 				sprite.getWidth(), sprite.getWidth());
 		
 		return rect;
 	}
 	
 	/**
 	 * @param distX x distance
 	 * @param distY y distance
 	 * @return The new bounding box of the character if moved the specified distance
 	 */
 	public Rectangle getNewBounds(int distX, int distY)
 	{
 		if(sprite.getHeight()>sprite.getWidth())
 		{
 			int newX			= (int) x;
 			int newY			= (int) y;
 			
 			newX			   += (int) Math.ceil(distX * speed *
 					ParkViewProtector.getRenderDeltas());
 			newY			   += (int) Math.ceil(distY * speed *
 					ParkViewProtector.getRenderDeltas());
 			
 			Rectangle bounds;
 		
 			bounds = new Rectangle(newX, newY+(sprite.getHeight()-sprite.getWidth())/2,
 					sprite.getWidth(), sprite.getWidth());
 			
 			return bounds;
 		}
 		else {
 			return super.getNewBounds(distX, distY);
 		}
 	}
 	
 	/**
 	 * @param distance
 	 * @return The new bounding box of the character if moved the specified distance
 	 * in the current direction
 	 */
 	public Rectangle getNewBounds(int distance)
 	{
 		if(sprite.getHeight()>sprite.getWidth())
 		{
 			int newX			= (int) x;
 			int newY			= (int) y;
 			
 			int dist			= (int) (distance * speed *
 					ParkViewProtector.getRenderDeltas());
 			
 			// determine and change direction if necessary
 			switch(direction)
 			{
 				case Direction.NORTH:
 					newY	-= dist;
 					break;
 				
 				case Direction.EAST:
 					newX	+= dist;
 					break;
 					
 				case Direction.SOUTH:
 					newY	+= dist;
 					break;
 				
 				case Direction.WEST:
 					newX	-= dist;
 					break;
 			}
 			
 			Rectangle bounds;
 		
 			bounds		= new Rectangle(newX, newY+(sprite.getHeight()-sprite.getWidth())/2,
 					sprite.getWidth(), sprite.getWidth());
 			
 			return bounds;
 		}
 		else {
 			return super.getNewBounds(distance);
 		}
 	}
 
 	protected void validateState()
 	{
 		super.validateState();
 		
 		if(hp > maxHp)
 		{
 			throw new IllegalArgumentException("HP cannot exceed max HP");
 		}
 	}
 	
 	public void showHp()
 	{
 		// determine vertical placement
 		int y;
 		y				= (int) this.y - Bar.BAR_HEIGHT;
 		//y				= this.y - bar.getSprite().getHeight();
 		y				= (y < 0) ? 0 : y;
 		
 		bar.setWidth(this.sprite.getWidth());
 		bar.setFilled((double) hp/maxHp);
 		bar.draw((int) x, (int) y);
 	}
 	
 	public void draw()
 	{
 		for(StatusEffect effect : effects)
 		{
 			effect.draw();
 			if(effect.tick())
 			{
 				effects.remove(effect);
 				break;
 			}
 		}
 		
 		super.draw();
 	}
 }
