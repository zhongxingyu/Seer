 /*
  * Copyright (C) 2010 Douglas Teoh. Use is subject to license terms.
  */
 package com.dteoh.treasuremap;
 
 import org.jdesktop.application.ResourceMap;
 
 /**
  * Convenience class for creating {@link ResourceMap}s.
  * 
  * @author Douglas Teoh
  * 
  */
 public final class ResourceMaps {
 
     /**
      * Creates a {@link ResourceMap} containing all resources for the given
      * class.
      * 
      * Equivalent to calling:
      * <p>
      * <code>new ResourceMap(null, c.getClassLoader(), c.getPackage().getName()
      *  + ".resources." + c.getSimpleName());</code>
      * </p>
      * 
      * @param c
      *            Class to create the ResourceMap for.
      * @return The newly created ResourceMap.
      */
     public static ResourceMap create(final Class<?> c) {
         ResourceMap rmap = new ResourceMap(null, c.getClassLoader(), c
                 .getPackage().getName() + ".resources." + c.getSimpleName());
         return rmap;
     }
 
     /**
      * Creates a {@link ResourceMap} containing all resources for the given
      * class as well as the parent resource map.
      * 
      * Equivalent to calling:
      * <p>
     * <code>new ResourceMap(null, c.getClassLoader(), c.getPackage().getName()
      *  + ".resources." + c.getSimpleName());</code>
      * </p>
      * 
      * @param parent
      *            Parent resource map.
      * @param c
      *            Class to create the ResourceMap for.
      * @return The newly created ResourceMap.
      */
     public static ResourceMap create(final ResourceMap parent, final Class<?> c) {
         ResourceMap rmap = new ResourceMap(parent, c.getClassLoader(), c
                 .getPackage().getName() + ".resources." + c.getSimpleName());
         return rmap;
     }
 
 }
