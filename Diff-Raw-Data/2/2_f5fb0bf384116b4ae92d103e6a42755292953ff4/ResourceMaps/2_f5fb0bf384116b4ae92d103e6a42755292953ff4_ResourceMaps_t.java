 /*
  * Copyright (C) 2010 Douglas Teoh. Use is subject to license terms.
  */
 package com.dteoh.treasuremap;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import org.jdesktop.application.ResourceMap;
 
 /**
  * Builder for creating {@link ResourceMap}s.
  * 
  * @author Douglas Teoh
  * 
  */
 public final class ResourceMaps {
 
     /** Locale to use for creating resource maps. */
     private final Locale bundleLocale;
     /** Class loader to use. */
     private final ClassLoader cLoader;
     /** Resource bundle names. */
     private final List<String> bundleNames = new ArrayList<String>();
     /** Parent resource map. */
     private ResourceMap parent = null;
 
     /**
      * Creates a new resource map builder. Uses the default locale of the JVM.
      * 
      * @param c
      *            The class to create the resource map for.
      */
     public ResourceMaps(final Class<?> c) {
         this(c, Locale.getDefault());
     }
 
     /**
      * Creates a new resource map builder with the given locale.
      * 
      * @param c
      *            The class to create the resource map for.
      * @param locale
      *            The locale to use when creating resource maps.
      */
     public ResourceMaps(final Class<?> c, final Locale locale) {
         if (c == null) {
             throw new NullPointerException("Class cannot be null.");
         }
         if (locale == null) {
             throw new NullPointerException("Locale cannot be null.");
         }
 
         bundleLocale = locale;
         cLoader = c.getClassLoader();
 
         String baseName = createBundleName(c);
         bundleNames.add(baseName + "_" + locale.toString());
         bundleNames.add(baseName);
     }
 
     /**
      * Uses the given resource map as a parent resource map. The builder will
      * only use the last configured parent resource map as the parent resource
      * map of the resource map to build. The configured locale will not affect
      * the parent resource map.
      * 
      * @param parentMap
      *            Resource map to use as a parent resource map.
      * @return this
      */
     public ResourceMaps withParent(final ResourceMap parentMap) {
         parent = parentMap;
         return this;
     }
 
     /**
      * Adds the given class's resource bundle as part of the resource map to
      * build. Uses the builder's configured locale.
      * 
      * @param c
      *            The class to add to the resource map to be built.
     * @return this
      */
     public ResourceMaps and(final Class<?> c) {
         String baseName = createBundleName(c);
         bundleNames.add(baseName + "_" + bundleLocale.toString());
         bundleNames.add(baseName);
         return this;
     }
 
     /**
      * Creates a {@link ResourceMap} using the currently configured builder.
      * 
      * @return The newly created ResourceMap.
      */
     public ResourceMap build() {
         return new ResourceMap(parent, cLoader, bundleNames);
     }
 
     /**
      * Used to create the bundle name for the given class.
      * 
      * @param c
      *            Class to create the bundle name for.
      * @return Bundle name.
      */
     private static String createBundleName(final Class<?> c) {
         return c.getPackage().getName() + ".resources." + c.getSimpleName();
     }
 
 }
