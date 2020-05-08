 /* 
 	JCavernApplet.java
 
 	Title:			JCavern And Glen
 	Author:			Bill Walker
 	Description:	
 */
 
 package jcavern;
 
 import java.awt.*;
 
 /**
  * Represents a Thing that can participate in combat.
  *
  * @author	Bill Walker
  * @version	$Id$
  */
 public abstract class Combatant extends Thing
 {
 	/** * Denotes one Combatant successfully hitting another Combatant. */
 	protected static final int	HIT = 1;
 	
 	/** * Denotes one Combatant unable to hit another Combatant. */
 	protected static final int	MISS = 2;
 	
 	/** * Denotes one Combatant killing another Combatant. */
 	protected static final int	KILL = 3;
 	
 	/** * How many points does this Combatant have now. */
 	private int					mPoints;
 
 	/** * How many points has this player ever had. */
 	private int					mMaximumPoints;
 	
 	
 	// Combatants can compute and suffer damage they cause
 	
 	/**
 	 * Returns whether this Combatant can attack a given Combatant.
 	 */
 	public abstract boolean canAttack(Combatant aCombatant);
 	
 	/**
 	 * Returns whether this Combatant can make a ranged attack on a given Combatant.
 	 */
 	public abstract boolean canRangedAttack(Combatant aCombatant);
 	
 	/**
 	 * Returns whether this combatant is vulnerable to attack from a given Monster.
 	 */
 	public boolean vulnerableToMonsterAttack(Monster aMonster)
 	{
 		//System.out.println("Combatant.vulnerableToMonsterAttack(Combatant)");
 		return true;
 	}
 	
 	/**
 	 * Returns whether this combatant is vulnerable to attack from a given Player.
 	 */
 	public boolean vulnerableToPlayerAttack(Player aPlayer)
 	{
 		//System.out.println("Combatant.vulnerableToPlayerAttack(Combatant)");
 		return true;
 	}
 	
 	/**
 	 * Returns whether this combatant is vulnerable to attack from a given Monster.
 	 */
 	public boolean vulnerableToMonsterRangedAttack(Monster aMonster)
 	{
 		//System.out.println("Combatant.vulnerableToMonsterRangedAttack(Combatant)");
 		return true;
 	}
 	
 	/**
 	 * Returns whether this combatant is vulnerable to attack from a given Player.
 	 */
 	public boolean vulnerableToPlayerRangedAttack(Player aPlayer)
 	{
 		System.out.println("Combatant.vulnerableToPlayerRangedAttack(Combatant)");
 		return true;
 	}
 	
 	/**
 	 * Computes the damage this combatant does to a given opponent in a sword attack.
 	 *
 	 * @param	opponent	a non-null Combatant on whom the receiver will inflict damage
 	 */
 	public abstract int computeDamageTo(Combatant opponent);
 	
 	/**
 	 * Computes the damage this combatant does to a given opponent in a ranged attack.
 	 *
 	 * @param	opponent	a non-null Combatant on whom the receiver will inflict damage
 	 */
 	public abstract int computeRangedDamageTo(Combatant opponent);
 
 	/**
 	 * Causes this combatant to suffer damage.
 	 *
 	 * @param	theDamage	how much damage this combatant suffers.
 	 */
 	public void sufferDamage(int theDamage)
 	{
 		mPoints -= theDamage;
 	}
 	
 	/**
 	 * Decrements the number of ranged attacks this Combatant can perform.
 	 */
 	public abstract void decrementRangedAttackCount();
 	
 	// Combatants can attack things
 	
 	/**
 	 * Performs an attack on a potential opponent, if appropriate.
 	 *
 	 * @param	aWorld				a non-null World in which the attack occurs
 	 * @param	potentialOpponent	a non-null Thing that the receiver will try to attack
 	 */
 	public void attack(World aWorld, Thing potentialOpponent) throws JCavernInternalError, NonCombatantException
 	{
 		if (! (potentialOpponent instanceof Combatant))
 		{
 			throw new NonCombatantException(potentialOpponent + " is not a combatant!");
 		}
 		
 		Combatant opponent = (Combatant) potentialOpponent;
 
 		if (this.canAttack(opponent))
 		{
 			finishAttack(aWorld, computeDamageTo(opponent), opponent);
 			
 			aWorld.thingChanged(opponent);
 		}
 		else
 		{
 			reportResultAgainst(opponent, Combatant.MISS, 0);
 		}
 	}
 	
 	// this method can go up to Combatant
 	public void attack(World aWorld, int aDirection) throws JCavernInternalError, EmptyLocationException, IllegalLocationException, NonCombatantException
 	{
 		Thing	potentialOpponent = aWorld.getThing(aWorld.getLocation(this).getNeighbor(aDirection));
 	
 		attack(aWorld, potentialOpponent);
 	}
 	
 	/**
 	 * Performs a ranged attack on a potential opponent, if appropriate.
 	 *
 	 * @param	aWorld				a non-null World in which the attack occurs
 	 * @param	potentialOpponent	a non-null Thing that the receiver will try to attack
 	 */
 	public void rangedAttack(World aWorld, Thing potentialOpponent) throws JCavernInternalError, NonCombatantException
 	{
 		System.out.println(this + " ranged attack " + potentialOpponent);
 		
 		if (! (potentialOpponent instanceof Combatant))
 		{
 			throw new NonCombatantException(potentialOpponent + " is not a combatant!");
 		}
 		
 		Combatant opponent = (Combatant) potentialOpponent;
 		
 		decrementRangedAttackCount();
 
 		if (this.canRangedAttack(opponent))
 		{
 			finishAttack(aWorld, computeRangedDamageTo(opponent), opponent);
 			
 			aWorld.thingChanged(opponent);
 		}
 		else
 		{
 			reportResultAgainst(opponent, Combatant.MISS, 0);
 		}
 	}
 		
 	// this method can go up to Combatant
 	public void rangedAttack(World aWorld, int aDirection) throws JCavernInternalError, NonCombatantException, IllegalLocationException
 	{
 		Thing	potentialOpponent = aWorld.getThingToward(this, aDirection);
 	
 		rangedAttack(aWorld, potentialOpponent);
 	}
 	
 	protected void reportResultAgainst(Combatant opponent, int outcome, int damage)
 	{
 		StringBuffer	theBuffer = new StringBuffer();
 		
 		theBuffer.append(getNounPhrase() + " ");
 		
 		switch (outcome)
 		{
 			case Combatant.MISS:
 					theBuffer.append("cannot attack" + " " + opponent.getNounPhrase()); break;
 			case Combatant.HIT:
 					theBuffer.append(opponent.getHitVerb() + " " + opponent.getNounPhrase() + " for " + damage); break;
 			case Combatant.KILL:
 					theBuffer.append(opponent.getKilledVerb() + " " + opponent.getNounPhrase()); break;
 		}
 		
		JCavernApplet.log(theBuffer.toString());
 	}
 	
 	protected String getHitVerb()
 	{
 		return "hit"; 
 	}
 	
 	protected String getKilledVerb()
 	{
 		return "killed";
 	}
 	
 	protected boolean hasProperName()
 	{
 		return false;
 	}
 	
 	protected String getNounPhrase()
 	{
 		if (hasProperName())
 		{
 			return getName();
 		}
 		else
 		{
 			return "the " + getName();
 		}
 	}
 
 	/**
 	 * Concludes an attack by removing dead opponents and awarding experience, if appropriate.
 	 *
 	 * @param	opponent	a non-null Combatant who was attacked by the receiver.
 	 */
 	private void finishAttack(World aWorld, int damage, Combatant opponent) throws JCavernInternalError
 	{
 		opponent.sufferDamage(damage);
 
 		try
 		{
 			if (opponent.isDead())
 			{
 				reportResultAgainst(opponent, Combatant.KILL, damage);
 				gainPoints(opponent);
 				aWorld.remove(opponent);
 			}
 			else
 			{
 				reportResultAgainst(opponent, Combatant.HIT, damage);
 			}
 		}
 		catch (JCavernInternalError jcie)
 		{
 			throw new JCavernInternalError("tried to finish attack, dead opponent not found");
 		}
 	}
 	
 	/**
 	 * Gain points from a victory.
 	 * Augments this Combatant's points by the number of points the loser was worth.
 	 */
 	public void gainPoints(Combatant theVictim)
 	{
 		mPoints += theVictim.getWorth();
 		mMaximumPoints = Math.max(mMaximumPoints, mPoints);
 	}
 	
 	/**
 	 * Returns the number of points this Combatant is worth when it loses a battle.
 	 */
 	public abstract int getWorth();
 	
 	/**
 	 * Returns whether this Combatant is dead.
 	 * 
 	 * @return	<CODE>true</CODE> if this Combatant has fewer than zero points, <CODE>false</CODE> otherwise.
 	 */
 	public boolean isDead()
 	{
 		return mPoints < 0;
 	}
 	
 	/**
 	 * Returns the current number of points for this Combatant.
 	 */
 	public int getPoints()
 	{
 		return mPoints;
 	}
 	
 	/**
 	 * Returns the maximum number of points this Combatant has ever had.
 	 *
 	 * @return	Combatant's lifetime maximum points.
 	 */
 	public int getMaximumPoints()
 	{
 		return mMaximumPoints;
 	}
 	
 	public void paint(Graphics g, int plotX, int plotY)
 	{
 		paint(g, plotX, plotY, true);
 	}
 	
 	public void paint(Graphics g, int plotX, int plotY, boolean drawGauge)
 	{
		final int gaugeThickness = 4;
 		final int gaugeLength = 32;
 		
 		super.paint(g, plotX, plotY);
 		
 		if (drawGauge && (! getInvisible()))
 		{
			double percent = 1.0 * getPoints() / getMaximumPoints();
 			
			g.fillRect(plotX - gaugeLength / 2, plotY - 20, (int) (gaugeLength * percent), gaugeThickness);
			g.drawLine(plotX - gaugeLength / 2, plotY - (20 - gaugeThickness / 2), plotX + gaugeLength / 2, plotY - (20 - gaugeThickness / 2));
 		}
 	}
 
 	public Combatant(String name, String imageName, int points)
 	{
 		this(name, imageName, points, false);
 	}
 	
 	public Combatant(String name, String imageName, int points, boolean invisible)
 	{
 		super(name, imageName, invisible);		
 		mPoints = points;
 		mMaximumPoints = points;
 	}
 }
