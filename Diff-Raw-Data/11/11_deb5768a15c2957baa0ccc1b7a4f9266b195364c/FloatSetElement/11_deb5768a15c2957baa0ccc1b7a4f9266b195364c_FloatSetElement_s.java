 /*
  * Copyright 1996-2002 by Andruid Kerne. All rights reserved.
  * CONFIDENTIAL. Use is subject to license terms.
  */
 package ecologylab.generic;
 
 /**
  * A FloatWeightSet element.
  *
  * Basic implementation of SetElement for cases when you can inherit from here
  **/
 public class FloatSetElement
 extends Debug
 // implements SetElement
 {
    public static final int	NOT_A_MEMBER	= -1;
 
    protected int		index		= NOT_A_MEMBER;
 
 /**
  * Cached version of weight. Made accessible for efficiency's sake.
  * Use carefully, at your own risk, and only w inside, deep understanding.
  */
    public float			weight;
 
    protected BasicFloatSet	set;
 
    public FloatSetElement()
    {
    }
    public FloatSetElement(float initial)
    {
       weight	= initial;
    }
    public float getWeight()
    {
       return weight;
    }
    public int getIndex()
    {
       return index;
    }
    public void setIndex(int newIndex)
    {
       index	= newIndex;
    }
    public void setSet(BasicFloatSet setArg)
    {
       set		= setArg;
    }
 /**
  * Delete in the most expedient manner possible.
  */
    public void delete()
    {
      if (set != null)
      	 set.delete(this, BasicFloatSet.NO_RECOMPUTE);
    }
 /**
  * Delete the element from the set.
  * This means changing the set's structure, and also changing slots in 
  * this to reflect its lack of membership.
  * 
  * @param recompute	-1 for absolutely no recomputation of the set's internal structures.
  * 			 0 for recompute upwards from el
  * 			 1 for recompute all
  */
    public synchronized void delete(int recompute)
    {
       if ((set != null) && (index != NOT_A_MEMBER))//prevent double dip deletes
       {
       	set.delete(this, recompute);
         clear();
       }
    }
    /**
     * Only for use by FloatWeightSet.clear(), and delete.
     * This is used to reset state of this, in cases when the state
     * of the FloatWeightSet we were part of is being reset some other way,
     * or if it is being discarded.
     *
     */
    public void clear()
    {
    	  index	= NOT_A_MEMBER;
       set	= null;
    }
    
    /**
     * A synchronized version of clear(), for use in FloatWeightSet.prune().
     *
     */
    synchronized void clearSynch()
    {
       index	= NOT_A_MEMBER;
       set	= null;
    }
 /**
  * Change the weight of the element, without propogating the new
  * weight into the data structure. The result of the change will be
  * propogated later by some other operation (such as delete) which
  * performs a recompute
  */
    public void delaySetWeight(float newWeight)
    {
       weight	= newWeight;
    }
 /**
  * Set the weight slot.
  * 
  * Doesn't effect the set if no longer a member.
  */
    public void setWeight(float newWeight)
    {
       weight	= newWeight;
    }
   public String toString()
   {
      return super.toString() + " " + getIndex() + ": " + weight;
   }
 /**
  * Free resources associated w this element.
  */   
    public boolean recycle()
    {
       return true;
    }
 }
