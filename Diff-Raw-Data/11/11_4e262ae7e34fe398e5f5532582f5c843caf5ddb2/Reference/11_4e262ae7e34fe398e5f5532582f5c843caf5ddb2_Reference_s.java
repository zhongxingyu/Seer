 package de.dustboystudios.utils.cloner;
 
 /**
  * A class representing a reference to an object
  *
  * @author Manuel Vögele
  *
  */
 public class Reference
 {
 	/**
 	 * The object
 	 */
 	private Object object;
 
 	/**
 	 * Constructs a new reference
 	 * 
 	 * @param object the object this instance should represent an reference of
 	 */
 	public Reference(Object object)
 	{
 		this.object = object;
 	}
 	
 	@Override
 	public boolean equals(Object o)
 	{
		return o == object;
 	}
 	
 	@Override
 	public int hashCode()
 	{
 		return object.hashCode();
 	}
 	
 	/**
 	 * Returns the object this reference points to
 	 * 
 	 * @return the object this reference points to
 	 */
 	public Object getObject()
 	{
 		return object;
 	}
 }
