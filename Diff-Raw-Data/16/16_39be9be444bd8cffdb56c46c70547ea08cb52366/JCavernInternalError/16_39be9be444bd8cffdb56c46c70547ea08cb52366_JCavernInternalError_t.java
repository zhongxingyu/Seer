 /* 
	JCavernInternalError.java
 
 	Title:			JCavern And Glen
 	Author:			Bill Walker
 	Description:	
 */
 
 package jcavern;
 
 /**
  * Represents an internal error within JCavern.
  *
  * @author	Bill Walker
  * @version	$Id$
  */
 public class JCavernInternalError extends Exception
 {
 	/**
 	 * Creates a new jcavern internal error.
 	 */
 	public JCavernInternalError(String msg)
 	{
 		super(msg);
 	}
 }
