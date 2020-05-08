 package edu.gatech.oad.rocket.findmythings.server.util;
 
 import java.util.Set;
 
 /**
  * This interface describes a mutable class that provides searchable content that can be indexes.
  *
  * User: zw
  * Date: 4/8/13
  * Time: 2:36 AM
  *
  * @see SearchableHelper
  */
 public abstract interface Searchable {
	
	abstract boolean canGetSearchableContent();
 
 	abstract String getSearchableContent();
 
 	abstract Set<String> getSearchTokens();
 
 }
