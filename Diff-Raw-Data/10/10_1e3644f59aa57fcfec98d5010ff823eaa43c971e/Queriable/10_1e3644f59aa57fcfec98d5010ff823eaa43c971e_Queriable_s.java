 /*
  * Created on Sep 24, 2004
  */
 package gov.nih.nci.caintegrator.dto.query;
 
import gov.nih.nci.rembrandt.queryservice.view.Viewable;
 
 import java.io.Serializable;
 
 /**
  * @author SahniH
  */
 public interface Queriable extends Serializable, Cloneable {
 	/**
 	 * IMPORTANT! This class requires a clone method! This requires that any new
 	 * data field that is added to this class also be cloneable and be added to
 	 * clone calls in the clone method.If you do not do this, you will not
 	 * seperate the references of at least one data field when we generate a
 	 * copy of this object.This means that if the data field ever changes in one
 	 * copy or the other it will affect both instances... this will be hell to
 	 * track down if you aren't ultra familiar with the code base, so add those
 	 * methods now! (Not necesary for primitives.)
 	 */
 	public Viewable getAssociatedView();
     public void setAssociatedView(Viewable view);
     public String toString();
     public String getQueryName();
     public void setQueryName(String queryName);
 	public Query[] getAssociatiedQueries();
 	/**
 	 * Overrides the protected Object.clone() method exposing it as public.
 	 * It performs a 2 tier copy, that is, it does a memcopy of the instance
 	 * and then sets all the non-primitive data fields to clones of themselves.
 	 * 
 	 * @return -A minimum 2 deep copy of this object.
 	 */
 	public Object clone();
 }
