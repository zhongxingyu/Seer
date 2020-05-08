 /* Copyright 2013 Kevin Seiden. All rights reserved.
 
  This works is licensed under the Creative Commons Attribution-NonCommercial 3.0
 
  You are Free to:
     to Share: to copy, distribute and transmit the work
     to Remix: to adapt the work
 
  Under the following conditions:
     Attribution: You must attribute the work in the manner specified by the author (but not in any way that suggests that they endorse you or your use of the work).
     Non-commercial: You may not use this work for commercial purposes.
 
  With the understanding that:
     Waiver: Any of the above conditions can be waived if you get permission from the copyright holder.
     Public Domain: Where the work or any of its elements is in the public domain under applicable law, that status is in no way affected by the license.
     Other Rights: In no way are any of the following rights affected by the license:
         Your fair dealing or fair use rights, or other applicable copyright exceptions and limitations;
         The author's moral rights;
         Rights other persons may have either in the work itself or in how the work is used, such as publicity or privacy rights.
 
  Notice: For any reuse or distribution, you must make clear to others the license terms of this work. The best way to do this is with a link to this web page.
  http://creativecommons.org/licenses/by-nc/3.0/
  */
 
 package io.github.alshain01.flags.area;
 
 /**
  * Interface that defines area types that can be subdivided into parent/child
  * subdivisions.
  */
 public interface Subdivision {
 	/**
 	 * Gets the id of the subdivision.
 	 * 
 	 * @return The subdivision ID, null if not a subdivision
 	 */
 	public String getSystemSubID();
 
     /**
      * Checks if the area is a subdivision.
      *
      * @return True if the are is a subdivision of another area.
      */
     public boolean isSubdivision();
 
     /**
      * Returns whether the provided area is a parent of this area.
      *
      * @param area The potential parent area
      * @return True if the area is a parent.
      */
     public boolean isParent(Area area);
 
     /**
      * Returns the parent of a subdivision area
      *
      * @return The parent of this area
      */
     public Area getParent();
 
     /**
      * Checks if the subdivision is inheriting flags from it's parent
      *
      * @return True if the area is inheriting.
      */
 	public boolean isInherited();
 
 	/**
 	 * Sets if a subdivision is inheriting from it's parent
 	 * 
 	 * @param value
 	 *            True if the area should inherit from the parent area.
 	 */
 	public void setInherited(boolean value);
 }
