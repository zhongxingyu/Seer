 /* 
 	JCavernApplet.java
 
 	Title:			JCavern And Glen
 	Author:			Bill Walker
 	Description:	
 */
 
 package jcavern;
 
 import java.util.Observable;
 
 /**
  * A Mission represents the set of Monsters to be defeated
  * in order to complete a particular level.
  */
 public class Mission extends Observable
 {
 	/** * What kind of monster must be defeated? */
 	private Monster		mTarget;
 	
 	/** * How many of these monsters must be defeated? */
 	private int			mQuota;
 	
 	/** * How many of these monsters have been defeated? */
 	private int			mKills;
 	
 	public int getQuota()
 	{
 		return mQuota;
 	}
 	
 	public Monster getTarget()
 	{
 		return mTarget;
 	}
 	
 	public int getKills()
 	{
 		return mKills;
 	}
 	
 	public boolean getCompleted()
 	{
 		return mKills >= mQuota;
 	}
 	
 	public void adjustQuota(Thing theVictim)
 	{
 		if (theVictim.getName().equals(mTarget.getName()))
 		{
 			mKills++;
 			JCavernApplet.log("You mission is now " + this);
 			
 			setChanged();
 			notifyObservers();		
 		}
 	}
 	
 	public String toString()
 	{
 		if (mKills < mQuota)
 		{
 			return "to kill " + (mQuota - mKills) + " of " + mQuota + " " + mTarget.getName() + "s";
 		}
 		else
 		{
 			return "completed";
 		}
 	}
 	
 	public Mission(Monster target, int quota)
 	{
 		mTarget = target;
 		mQuota = quota;
 		mKills = 0;
 	}
 }
